package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;

import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PlainMdict;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

/** Full-scan Search Among Explanations */
@SuppressLint("SetTextI18n")
public class IndexBuildingTask extends AsyncTaskWrapper<HashSet<PlaceHolder>, Object, String > {
	private final WeakReference<PDICMainActivity> activity;

	public IndexBuildingTask(PDICMainActivity a) {
		activity = new WeakReference<>(a);
	}
	@Override
	protected void onPreExecute() {
		try {
			PDICMainActivity a;
			if((a=activity.get())==null) return;
			a.OnEnterIndexBuildingTask(this);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.updateFFSearch((BookPresenter) values[0], (int)values[1]);
	}

	@Override
	protected String doInBackground(HashSet<PlaceHolder>... params) {
		//CMN.Log("Find In Background??");
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		if(params.length!=1) return null;
		HashSet<PlaceHolder> toBuild = new HashSet<>(params[0]);
		if(toBuild.size()==0)
			return null;
		MainActivityUIBase.LoadManager loadManager = a.loadManager;
		int size = loadManager.md_size;
		
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			Directory index = FSDirectory.open(new File("/sdcard/PLOD/lucene"));
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
			config.setMaxBufferedDocs(1024);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			IndexWriter writer = new IndexWriter(index, config);
			for(int i=0;i<size;i++){
				try {
					if (toBuild.contains(loadManager.getPlaceHolderAt(i))) {
						BookPresenter mdTmp = loadManager.md_get(i);
						publishProgress(mdTmp, i);//_mega
						if(mdTmp!=a.EmptyBook) {
							PlainMdict md = (PlainMdict) mdTmp.bookImpl;
							md.searchCancled = false;
							md.doForAllRecords(mdTmp, a.fullSearchLayer, new mdict.DoForAllRecords() {
								@Override
								public void doit(Object parm, long position, byte[] data, int from, int len, Charset _charset) {
									try {
										String text = new String(data, from, len, _charset);
										text = org.jsoup.Jsoup.parse(text).text();
										Document doc = new Document();
										doc.add(new StringField("bookName", md.getDictionaryName(), Field.Store.YES));
										doc.add(new TextField("entry", md.getEntryAt(position), Field.Store.YES));
										// CMN.Log(text);
										doc.add(new TextField("content", text, Field.Store.YES));
										writer.addDocument(doc);
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
							}, null);
						}
						//publisResults();
						if(isCancelled()) break;
					}
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
			writer.commit();
			writer.close();
		} catch (IOException e) {
			CMN.debug(e);
		}
		System.gc();
		return null;
	}
	
	@Override
	protected void onCancelled(String String) {
		super.onCancelled();
		harvest(false);
	}

	@Override
	protected void onPostExecute(String String) {
		super.onPostExecute(String);
		harvest(false);
	}

	public void harvest(boolean kill) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		Object currentThreads = a.fullSearchLayer.currentThreads;
		if(kill&&currentThreads!=null){
			//CMN.Log("shutdownNow !!!");
			if(currentThreads instanceof ArrayList)
			for(Thread t:(ArrayList<Thread>)currentThreads){
				t.interrupt();
			}
			else if(currentThreads instanceof ExecutorService){
				((ExecutorService)currentThreads).shutdownNow();
			}
			cancel(true);
		}
		if(a.timer!=null) { a.timer.cancel(); a.timer=null; }
		if(a.taskd!=null) a.taskd.dismiss();
		a.mAsyncTask=null;
		
		a.show(R.string.idxfill, (System.currentTimeMillis()-CMN.stst)*1.f/1000, 0, 0);
		
		CMN.Log((System.currentTimeMillis()-CMN.stst)*1.f/1000, "此即索引时间。");
		
		System.gc();
		a.fullSearchLayer.currentThreads=null;
	}

}

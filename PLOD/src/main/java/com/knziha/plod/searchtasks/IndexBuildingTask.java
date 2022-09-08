package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;

import com.knziha.plod.PlainUI.LuceneHelper;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.searchtasks.lucene.WordBreakFilter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/** Build Lucene Indexes */
@SuppressLint("SetTextI18n")
public class IndexBuildingTask extends AsyncTaskWrapper<Object, Object, String > {
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
	
	static class DocIndex {
		final Document doc = new Document();
		final StringField bookName = new StringField("bookName", "", Field.Store.YES);
		final TextField entry = new TextField("entry", "", Field.Store.YES);
		final TextField content = new TextField("content", "", Field.Store.YES);
		final StoredField position = new StoredField("position", 0);
		DocIndex() {
			doc.add(bookName);
			doc.add(entry);
			doc.add(content);
			doc.add(position);
		}
	}
	
	static class DocIndexChecker {
		final Term termPos = new Term("position", "");
		final Term termBook = new Term("bookName", "");
		BooleanQuery query = new BooleanQuery();
		LuceneHelper luceneHelper;
		DocIndexChecker() {
			query.add(new TermQuery(termBook), BooleanClause.Occur.MUST);
			query.add(new TermQuery(termPos), BooleanClause.Occur.MUST);
		}
	}

	@Override
	protected String doInBackground(Object... params) {
		PDICMainActivity a=activity.get();
		if(a==null) return null;
		if(params.length!=3) return null;
		HashSet<PlaceHolder> toBuild = new HashSet((HashSet)params[0]);
		HashSet<String> built = new HashSet((HashSet)params[1]);
		boolean rebuildIndexes = (Boolean) params[2];
		
		if(toBuild.size()==0)
			return null;
		MainActivityUIBase.LoadManager loadManager = a.loadManager;
		int size = loadManager.md_size;
		
		try {
			//DocIndexChecker checker;
			//checker = new DocIndexChecker();
			final Analyzer analyzer = WordBreakFilter.newAnalyzer();
			File folder = new File(a.opt.pathToMainFolder().append("lucene").toString());
			Directory index = FSDirectory.open(folder);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			//config.setMaxBufferedDocs(-1);
			//config.setUseCompoundFile(false);
			config.setRAMBufferSizeMB(128);
			IndexWriter writer = new IndexWriter(index, config);
			final ConcurrentHashMap<Long, Object> keyBlockOnThreads = new ConcurrentHashMap<>();
			for(int i=0;i<size;i++){
				try {
					if (toBuild.contains(loadManager.getPlaceHolderAt(i))) {
						BookPresenter mdTmp = loadManager.md_get(i);
						publishProgress(mdTmp, i);//_mega
						if(mdTmp!=a.EmptyBook) {
							final String bookName = mdTmp.getDictionaryName();
							if (built.contains(bookName)) {
								if (rebuildIndexes) {
									writer.deleteDocuments(new TermQuery(new Term("bookName", "" + bookName)));
								} else {
									continue;
								}
							}
							UniversalDictionaryInterface md = mdTmp.bookImpl;
							md.setPerThreadKeysCaching(keyBlockOnThreads);
							md.doForAllRecords(mdTmp, a.fullSearchLayer, new DictionaryAdapter.DoForAllRecords() {
								@Override
								public void doit(Object parm, Object tParm, long position, byte[] data, int from, int len, Charset _charset) {
									try {
//										if (luceneHelper.searcher != null) {
//											final Query queryPos = NumericRangeQuery.newIntRange("position", 1, (int) position, (int) position, true, true);
//											final Term termBook = new Term("bookName", "" + md.getDictionaryName());
//											BooleanQuery query = new BooleanQuery();
//											query.add(new TermQuery(termBook), BooleanClause.Occur.MUST);
//											query.add(queryPos, BooleanClause.Occur.MUST);
//											TopDocs hits = luceneHelper.searcher.search(query, 1);
//											if (hits.scoreDocs != null && hits.scoreDocs.length > 0) {
//												return;
//											}
//										}
										String text = new String(data, from, len, _charset);
										text = org.jsoup.Jsoup.parse(text).text();
										DocIndex pDoc = (DocIndex) tParm;
										pDoc.entry.setStringValue(md.getEntryAt(position));
										pDoc.content.setStringValue(text);
										pDoc.position.setIntValue((int) position);
										// CMN.Log(text);
										writer.updateDocument(null, pDoc.doc, analyzer);
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
								@Override
								public Object onThreadSt(Object parm) {
									DocIndex pDoc = new DocIndex();
									pDoc.bookName.setStringValue(bookName);
									return pDoc;
								}
								@Override
								public void onThreadEd(Object parm) {
									keyBlockOnThreads.remove(Thread.currentThread().getId());
								}
							}, null);
							md.setPerThreadKeysCaching(null);
						}
						//publisResults();
						if(isCancelled()) break;
					}
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
			keyBlockOnThreads.clear();
			//CMN.rt("commit::");
			// writer.commit();
			writer.close();
			//CMN.pt("commit::");
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

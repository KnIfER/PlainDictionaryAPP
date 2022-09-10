package com.knziha.plod.searchtasks;

import static com.knziha.plod.dictionary.mdBase.markerReg;

import android.annotation.SuppressLint;

import com.knziha.plod.PlainUI.LuceneHelper;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PlainDSL;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.searchtasks.lucene.WordBreakFilter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

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
public class IndexBuildingTask extends AsyncTaskWrapper<LuceneHelper, Object, String > {
	private final WeakReference<PDICMainActivity> activity;
	private final WeakReference<PDICMainActivity.AdvancedSearchInterface> layerRef;
	private int cnt;
	private long indexSize;
	private long newSize;
	
	public IndexBuildingTask(PDICMainActivity a) {
		activity = new WeakReference<>(a);
		PDICMainActivity.AdvancedSearchInterface layer = new PDICMainActivity.AdvancedSearchInterface(a.opt, a.loadManager.md, -1);
		this.layerRef = new WeakReference<>(layer);
	}
	
	@Override
	protected void onPreExecute() {
		try {
			PDICMainActivity a = activity.get();
			PDICMainActivity.AdvancedSearchInterface layer = layerRef.get();
			if(a==null || layer==null) return;
			a.OnEnterIndexBuildingTask(this, layerRef.get());
		} catch (Exception e) {
			CMN.Log(e);
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		PDICMainActivity a=activity.get();
		if(a==null) return;
		a.updateIndexBuilding((BookPresenter) values[0], (int)values[1]);
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

	@Override
	protected String doInBackground(LuceneHelper... params) {
		PDICMainActivity a = activity.get();
		PDICMainActivity.AdvancedSearchInterface layer = layerRef.get();
		if(a==null || layer==null) return null;
		if(params.length!=1) return null;
		LuceneHelper helper = params[0];
		HashSet<PlaceHolder> toBuild = new HashSet<>(helper.indexingBooks);
		HashSet<String> built = new HashSet<>(helper.indexedbooksMap);
		int rebuild = helper.rebuildIndexes;
		indexSize = helper.indexSize;
		if(indexSize==0) indexSize = helper.statFileSize();
		
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
						if(mdTmp!=a.EmptyBook) {
							publishProgress(mdTmp, cnt+1);//_mega
							final String bookName = mdTmp.getDictionaryName();
							if (built.contains(bookName)) {
								if (rebuild==1) {
									writer.deleteDocuments(new TermQuery(new Term("bookName", "" + bookName)));
								} else if(rebuild==0){
									continue;
								}
							}
							cnt++;
							UniversalDictionaryInterface md = mdTmp.bookImpl;
//							testAddDoc(md, "clap", bookName, analyzer, writer);
//							testAddDoc(md, "unrip", bookName, analyzer, writer);
//							if(true) continue;
							md.setPerThreadKeysCaching(keyBlockOnThreads);
							final boolean hasStyles = mdTmp.isMdict() && mdTmp.getMdict().hasStyleSheets();
							int type = getFormat(mdTmp.getType());
							md.doForAllRecords(mdTmp, layer, new DictionaryAdapter.DoForAllRecords() {
								@Override
								public void doit(Object parm, Object tParm, String entry, long position, String text, byte[] data, int from, int len, Charset _charset) {
									try {
										if (text == null) {
											text = new String(data, from, len, _charset);
										}
										if (type==0) {
											text = org.jsoup.Jsoup.parse(text).text();
											if (hasStyles && text.contains("`")) {
												text = markerReg.matcher(text).replaceAll("").trim();
											}
										}
										else if (type==1) {
											text = PlainDSL.dslTagPattern.matcher(text).replaceAll("");
										}
										DocIndex pDoc = (DocIndex) tParm;
										if (entry == null && position >= 0) {
											entry = md.getEntryAt(position);
										}
										pDoc.entry.setStringValue(entry);
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
			writer.close();
		} catch (IOException e) {
			CMN.debug(e);
		}
		newSize = helper.statFileSize();
		System.gc();
		return null;
	}
	
	/** 调试索引添加 */
	private void testAddDoc(UniversalDictionaryInterface md, String key, String bookName, Analyzer analyzer, IndexWriter writer) throws IOException {
		int position = md.lookUp(key);
		if (position >= 0) {
			String text = md.getRecordAt(position, null, false);
			text = org.jsoup.Jsoup.parse(text).text();
			//text = text.replace("扯开", "扯开扯开扯开扯开扯开扯开扯开扯开扯开扯开扯开扯开扯开扯开扯开扯开开开开开开开开开开开开开开开开开扯开扯开扯开扯开扯开扯开开开开开开开开开开开");
			DocIndex pDoc = new DocIndex();
			pDoc.bookName.setStringValue(bookName);
			pDoc.entry.setStringValue(md.getEntryAt(position));
			pDoc.content.setStringValue(text);
			pDoc.position.setIntValue((int) position);
			CMN.debug("添加文章::", md.getEntryAt(position), text);
			writer.updateDocument(null, pDoc.doc, analyzer);
		}
	}
	
	@Override
	protected void onCancelled(String String) {
		harvest(false);
	}

	@Override
	protected void onPostExecute(String String) {
		harvest(false);
	}

	public void harvest(boolean kill) {
		PDICMainActivity a = activity.get();
		PDICMainActivity.AdvancedSearchInterface layer = layerRef.get();
		if(a==null || layer==null) return;
		Object currentThreads = layer.currentThreads;
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
		
		long delta = newSize - indexSize;
		
		String msg = a.mResource.getString(R.string.idxfill, (CMN.now() - CMN.stst) * 1.f / 1000, cnt
				, (delta<0?"-":"+")+mp4meta.utils.CMN.formatSize(Math.abs(delta))
				, mp4meta.utils.CMN.formatSize(newSize)
		);
		a.showT(msg);
		
		CMN.debug((CMN.now()-CMN.stst)*1.f/1000, "此即索引时间。");
		
		layer.currentThreads=null;
		
		LuceneHelper helper = a.schTools.getLuceneHelper();
		helper.closeIndexReader();
	}
	
	/** -1=text; 0=html; 1=[]; 2=md */
	int getFormat(DictionaryAdapter.PLAIN_BOOK_TYPE dictType) {
		if (dictType == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_MDICT) {
			return 0;
		}
		else if (dictType == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_DSL) {
			return 1;
		}
		else return -1;
	}
}

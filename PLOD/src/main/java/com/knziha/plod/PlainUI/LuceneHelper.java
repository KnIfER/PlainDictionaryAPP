package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

import com.knziha.plod.dictionarymanager.BookManagerMain;
import com.knziha.plod.dictionarymodels.resultRecorderLucene;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.searchtasks.lucene.WordBreakFilter;
import com.knziha.plod.widgets.EditTextmy;
import com.knziha.plod.widgets.ViewUtils;
import com.mobeta.android.dslv.DragSortListView;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class LuceneHelper {
	final PDICMainActivity a;
	final SearchToolsMenu schTools;
	public IndexReader reader;
	public IndexSearcher searcher;
	
	public HashSet<PlaceHolder> indexingBooks = new HashSet<>();
	public ArrayList<IndexedBook> indexedbooks = new ArrayList<>();
	public HashSet<String> indexedbooksMap = new HashSet<>();
	/** 0=skip; 1=rebuild; 2=ignore & build */
	public int rebuildIndexes;
	public int indexingTasks;
	
	public long[] freeSpaces = new long[8];
	
	String CurrentSearchText;
	public Analyzer analyzer;
	TopDocs docs;
	
	public org.apache.lucene.search.highlight.Highlighter highlighter;
	public long indexSize;
	
	public LuceneHelper(PDICMainActivity a, SearchToolsMenu schTools) {
		this.a = a;
		this.schTools = schTools;
		prepareSearch(false);
	}
	
	@AnyThread
	 void do_search(HashMap<String, Integer> map) throws Exception {
		CMN.rt();
		String key = CurrentSearchText;
		Query query = new QueryParser(Version.LUCENE_47, "content", analyzer).parse(key);
		if (map != null) {
			BooleanQuery booleanQuery = new BooleanQuery();
			BooleanQuery dictQueries = new BooleanQuery();
			for (int i = 0; i < indexedbooks.size(); i++) {
				String bookName = indexedbooks.get(i).name;
				if (map.containsKey(bookName)) {
					dictQueries.add(new TermQuery(new Term("bookName", bookName)), BooleanClause.Occur.SHOULD);
				}
			}
			if (dictQueries.clauses().size() == 0) {
				return;
			}
			booleanQuery.add(dictQueries, BooleanClause.Occur.MUST);
			booleanQuery.add(query, BooleanClause.Occur.MUST);
			query = booleanQuery;
		}
		CMN.Log("query::", query);
		int hitsPerPage = 100;
		// 3 do search
		docs = prepareSearch(true).search(query, hitsPerPage);
		ScoreDoc[] hits = docs.scoreDocs;
		CMN.Log("found " + hits.length + " results", docs.totalHits);
		
		CMN.pt("搜索时间::");
		
		org.apache.lucene.search.highlight.QueryScorer scorer=new org.apache.lucene.search.highlight.QueryScorer(query); //显示得分高的片段(摘要)
		org.apache.lucene.search.highlight.Fragmenter fragmenter=new org.apache.lucene.search.highlight.SimpleSpanFragmenter(scorer);
		org.apache.lucene.search.highlight.SimpleHTMLFormatter simpleHTMLFormatter=new org.apache.lucene.search.highlight.SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
		highlighter=new org.apache.lucene.search.highlight.Highlighter(simpleHTMLFormatter,scorer);
		highlighter.setTextFragmenter(fragmenter);
	}
	
	public void closeIndexReader() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				CMN.debug(e);
			}
			reader = null;
		}
		if (searcher!=null) {
			searcher = null;
		}
	}
	
	static class IndexedBook{
		String name;
		long bid;
		public IndexedBook(String name) {
			this.name = name;
		}
	}
	
	public IndexSearcher prepareSearch(boolean sch) {
		try {
			if (analyzer==null) {
				//analyzer = new StandardAnalyzer(Version.LUCENE_47);
				analyzer = WordBreakFilter.newAnalyzer();
			}
			if (reader==null) {
				File folder = new File(a.opt.pathToMainFolder().append("lucene").toString());
				Directory index = FSDirectory.open(folder);
				reader = DirectoryReader.open(index);
				reloadIndexedBookList();
			}
			if (sch && searcher==null) {
				searcher = new IndexSearcher(reader);
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return searcher;
	}
	
	public void reloadIndexedBookList() {
		indexedbooks.clear();
		indexedbooksMap.clear();
		if (reader!=null) {
			try {
				CMN.rt();
				Fields fields = MultiFields.getFields(reader);
				Terms terms = fields.terms("bookName");
				TermsEnum iterator = terms.iterator(null);
				BytesRef byteRef;
				while((byteRef = iterator.next()) != null) {
					String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
					indexedbooks.add(new IndexedBook(term));
					indexedbooksMap.add(term);
				}
				CMN.pt("reloadIndexedBookList::");
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
	
	
	public long statFileSize() {
		File folder = new File(a.opt.pathToMainFolder().append("lucene").toString());
		long ret = 0;
		try {
			if (folder.exists()) {
				File[] files = folder.listFiles();
				for (File f : files) {
					if (f.isFile()) {
						ret += f.length();
					}
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		indexSize = ret;
		return ret;
	}
	
	
	WeakReference<SearchEngine> indexSchDlg = ViewUtils.DummyRef;
	
	public void showSearchEngineDlg() {
		SearchEngine d = indexSchDlg.get();
		if (d == null) {
			d = new SearchEngine(a, this);
			indexSchDlg = new WeakReference<>(d);
		}
		d.show();
	}
}

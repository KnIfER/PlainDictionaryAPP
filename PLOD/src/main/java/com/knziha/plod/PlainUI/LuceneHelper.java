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
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

import com.knziha.plod.dictionarymanager.BookManagerMain;
import com.knziha.plod.dictionarymodels.resultRecorderLucene;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainActivity;
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
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class LuceneHelper extends BaseAdapter implements View.OnClickListener {
	final PDICMainActivity a;
	final SearchToolsMenu schTools;
	public IndexReader reader;
	public IndexSearcher searcher;
	
	public HashSet<PlaceHolder> indexingBooks = new HashSet<>();
	public ArrayList<IndexedBook> indexedbooks = new ArrayList<>();
	public HashSet<String> indexedbooksMap = new HashSet<>();
	/** 0=skip; 1=rebuild; 2=ignore & build */
	public int rebuildIndexes;
	
	public long[] freeSpaces = new long[8];
	
	private int mForegroundColor;
	private PorterDuffColorFilter ForegroundFilter;
	private String CurrentSearchText;
	public Analyzer analyzer;
	TopDocs docs;
	
	public org.apache.lucene.search.highlight.Highlighter highlighter;
	public long indexSize;
	
	public LuceneHelper(PDICMainActivity a, SearchToolsMenu schTools) {
		this.a = a;
		this.schTools = schTools;
		prepareSearch(false);
	}
	
	Toolbar toolbar;
	ViewGroup tools;
	EditTextmy etSearch;
	
	@SuppressLint("ResourceType")
	public View getSchView() {
		View view = a.getLayoutInflater().inflate(R.layout.search_view, null);
		DragSortListView lv = view.findViewById(android.R.id.list);
		lv.setAdapter(this);
		int pad = (int) (GlobalOptions.density*5);
		lv.setPadding(pad,0,pad,0);
		toolbar = view.findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.xml.menu_search);
		toolbar.setNavigationIcon(R.drawable.ic_baseline_history_24);
		toolbar.getNavigationBtn().setAlpha(0.15f);
		tools = view.findViewById(R.id.tools);
		etSearch = toolbar.findViewById(R.id.etSearch);
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
				search();
			}
			return true;
		});
		// 染色
		int foregroundColor = GlobalOptions.isDark ? Color.WHITE : a.MainBackground;
		ForegroundFilter = new PorterDuffColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN);
		mForegroundColor = foregroundColor;
		setTitleForegroundColor(toolbar, true, foregroundColor);
		setTitleForegroundColor(tools, true, foregroundColor);
		return view;
	}
	
	private void search() {
		String key = String.valueOf(etSearch.getText()).trim();
		if(key.length()>0) CurrentSearchText=key;
		a.showT("search::"+key);
		
		try {
			do_search();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		resultRecorderLucene results = new resultRecorderLucene(a, a.loadManager);
		a.adaptermy5.results = results;
		results.invalidate(a, null, docs, this);
		a.mlv2.setAdapter(a.adaptermy5);
		a.adaptermy5.notifyDataSetChanged();
	}
	
	
	@AnyThread
	private void do_search() throws Exception {
		CMN.rt();
		String key = CurrentSearchText;
		Query query = null;
		if (true) {
			//要查找的字符串数组
			String[] stringQuery = {"dollar", "ship"};
			stringQuery = new String[]{"ship's", "dollar"};
			stringQuery = new String[]{"ship's", "dollar"};
			
			String[] fields = {"entry", "content"};
			
			query = new QueryParser(Version.LUCENE_47, "content", analyzer).parse(key);
		} else {
		
		}
		CMN.Log("query: ", query);
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
	
	public void setTitleForegroundColor(ViewGroup v, boolean init, int foregroundColor) {
		LinkedList<ViewGroup> linkedList = new LinkedList<>();
		linkedList.add(v);
		View cI;
		while (!linkedList.isEmpty()) {
			ViewGroup current = linkedList.removeFirst();
			for (int i = 0; i < current.getChildCount(); i++) {
				cI = current.getChildAt(i);
				if (cI instanceof ViewGroup) {
					linkedList.addLast((ViewGroup) current.getChildAt(i));
				} else {
					if (cI instanceof TextView) {
						if(cI instanceof RadioSwitchButton) {
							//((RadioSwitchButton) cI).getButtonDrawable().mutate().setColorFilter(ForegroundFilter);
							((RadioSwitchButton) cI).setTextColor(foregroundColor);
							cI.setOnClickListener(this);
							if (init) {
								((RadioSwitchButton) cI).setButtonDrawable(R.drawable.radio_selector);
							}
						}
						else if (cI instanceof ActionMenuItemView) {
							((ActionMenuItemView) cI).getIcon().mutate().setColorFilter(ForegroundFilter);
							cI.setOnClickListener(this);
						}
						else {
							((TextView) cI).setTextColor(a.AppBlack);
						}
					} else {
						if (init && cI.getId() != -1) {
							cI.setOnClickListener(this);
						}
						if(cI instanceof ImageView){
							if(cI.getBackground() instanceof BitmapDrawable){
								cI.getBackground().mutate().setColorFilter(ForegroundFilter);
							} else {
								((ImageView)cI).setColorFilter(ForegroundFilter);
							}
						} else if(cI.getBackground()!=null){
							cI.getBackground().mutate().setColorFilter(ForegroundFilter);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == android.R.id.button1||v.getId() == R.id.search) {
			search();
		} else {
			BookManagerMain.ViewHolder vh
					= (BookManagerMain.ViewHolder) ViewUtils.getViewHolderInParents(v, BookManagerMain.ViewHolder.class);
			if (vh != null) {
			
			}
		}
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
	
	
	@Override
	public int getCount() {
		return indexedbooks.size();
	}
	@Override
	public Object getItem(int position) {
		return null;
	}
	@Override
	public long getItemId(int position) {
		return 0;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BookManagerMain.ViewHolder vh;
		if (convertView == null) {
			convertView = a.getLayoutInflater().inflate(R.layout.dict_manager_dslitem, parent, false);
			vh = new BookManagerMain.ViewHolder(convertView);
			vh.title.setOnClickListener(this);
			vh.ck.setOnClickListener(this);
		} else {
			vh = (BookManagerMain.ViewHolder) convertView.getTag();
		}
		vh.position = position;
		vh.title.setText(indexedbooks.get(position).name);
		vh.handle.setVisibility(View.GONE);
		return convertView;
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
}

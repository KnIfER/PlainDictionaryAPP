package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.searchtasks.lucene.WordBreakFilter;
import com.knziha.plod.settings.BookOptions;
import com.knziha.plod.settings.Multiview;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.widgets.EditTextmy;
import com.knziha.plod.widgets.ViewUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SettingsSearcher {
	private final MainActivityUIBase a;
	public IndexReader reader;
	public IndexSearcher searcher;
	
	ArrayList<PreferenceScreen> screens = new ArrayList<>();
	ArrayList<DocEntry> results = new ArrayList<>();
	int[] all_pref;
	private Analyzer analyzer;
	private Highlighter highlighter;
	
	public SettingsSearcher(MainActivityUIBase a) {
		all_pref = new int[]{R.xml.pref_main
				, R.xml.pref_book
				, R.xml.pref_dev
				, R.xml.pref_history
				, R.xml.pref_misc
				, R.xml.pref_multiview
				, R.xml.pref_nightmode
				, R.xml.pref_notification
				, R.xml.pref_tapsch
				, R.xml.searchpreferences
				, R.xml.serverpreferences
		};
		this.a = a;
	}
	
	static class DocIndex {
		final Document doc = new Document();
		final StringField realm = new StringField("realm", "", Field.Store.YES);
		final TextField full_text = new TextField("full_text", "", Field.Store.NO);
		final StoredField ids = new StoredField("ids", "");
		final StoredField title = new StoredField("title", "");
		final StoredField summary = new StoredField("summary", "");
		DocIndex() {
			doc.add(realm);
			doc.add(ids);
			doc.add(full_text);
			doc.add(title);
			doc.add(summary);
		}
	}
	
	public static class DocEntry {
		final int realm;
		boolean baked;
		String ids;
		String pid;
		CharSequence title;
		CharSequence summary;
		String path =  "设置 -> 词典设置";
		public DocEntry(Document doc) {
			this.realm = IU.parsint(doc.get("realm"));
			this.ids = doc.get("ids");
			this.title = doc.get("title");
			this.summary = doc.get("summary");
		}
		public void bake(SettingsSearcher sch) {
			if (!baked) {
				baked = true;
				CharSequence text = title;
				try {
					/*把权重高的显示出来*/
					TokenStream tokenStream= sch.analyzer.tokenStream("desc", new StringReader((String) text));
					String best = sch.highlighter.getBestFragment(tokenStream, (String) text);
					if(best!=null) text = Html.fromHtml(best);
				} catch (Exception e) {
					CMN.debug(e);
				}
				if (!TextUtils.isEmpty(text)) this.title = text;
				text = summary;
				try {
					/*把权重高的显示出来*/
					TokenStream tokenStream= sch.analyzer.tokenStream("desc", new StringReader((String) text));
					String best = sch.highlighter.getBestFragment(tokenStream, (String) text);
					if(best!=null) text = Html.fromHtml(best);
				} catch (Exception e) {
					CMN.debug(e);
				}
				if (!TextUtils.isEmpty(text)) this.summary = text;
				try {
					PreferenceGroup pg = sch.screens.get(realm);
					String[] keys = ids.split("/");
					StringBuilder path = new StringBuilder();
					for (int i = 0; i < keys.length-1; i++) {
						String id = keys[i];
						Preference p = pg.findPreference(id);
						if (p!=null) {
							if(path.length()>0)
								path.append(" -> ");
							path.append(p.getTitle());
						}
						if (p instanceof PreferenceGroup) {
							pg = (PreferenceGroup) p;
						}
						else break;
					}
					this.path = path.toString();
					pid = keys[keys.length-1];
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		}
	}
	
	public IndexSearcher prepareSearch(boolean sch) {
		try {
			if (analyzer==null) {
				//analyzer = new StandardAnalyzer(Version.LUCENE_47);
				analyzer = WordBreakFilter.newAnalyzer();
			}
			if (reader==null) {
				File folder = a.getExternalFilesDir("SettingsIndex");
				Directory index = FSDirectory.open(folder);
				reader = DirectoryReader.open(index);
			}
			if (sch && searcher==null) {
				searcher = new IndexSearcher(reader);
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return searcher;
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
	
	boolean built;
	
	public void buildIndex(Context context) throws Exception {
		if (built) {
			return;
		}
		prepareSearch(false);
		File folder = context.getExternalFilesDir("SettingsIndex");
		Directory index = FSDirectory.open(folder);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		config.setRAMBufferSizeMB(128);
		IndexWriter writer = new IndexWriter(index, config);
		
		DocIndex pDoc = new DocIndex();
		PreferenceManager prefMan = new PreferenceManager(context);
		PreferenceScreen screen;
		if (screens.size()==0) {
			//CMN.rt();
			for (int i = 0; i < all_pref.length; i++) {
				screen = prefMan.inflateFromResource(context, all_pref[i], null);
				screens.add(screen);
			}
			//CMN.pt("善始善终::inflate::");
		}
		
		CMN.rt();
		int totalEntries = 0; String realm;
		StringBuilder full_text = new StringBuilder();
		StringBuilder ids = new StringBuilder();
		ArrayList<Preference> preferences = new ArrayList<>(100);
		for (int d = 0; d < all_pref.length; d++) {
			screen = screens.get(d);
			preferences.clear();
			preferences.add(screen);
			realm = ""+d;
			for (int i = 0; i < preferences.size(); i++) {
				Preference p = preferences.get(i);
				if (p instanceof PreferenceGroup) {
					preferences.addAll(((PreferenceGroup)p).getPreferences());
				} else {
					Preference p1 = p;
					full_text.setLength(0);
					ids.setLength(0);
					ArrayList<Preference> pPath = new ArrayList<>();
					while ((p1=p1.getParent()) != null) {
						pPath.add(0, p1);
					}
					pPath.add(p);
					for (int j = 0; j < pPath.size(); j++) {
						p1 = pPath.get(j);
						String title = toString(p1.getTitle());
						String key = toString(p1.getKey());
						if (full_text.length() > 0) full_text.append(" / ");
						full_text.append(title);
						if (ids.length() > 0) ids.append("/");
						ids.append(key);
					}
					String summary = toString(p.getRawSummary());
					String wikiTexi = toString(p.getWikiText());
					if (summary.length() > 0 && wikiTexi.length() > 0) {
						summary += " " + wikiTexi;
					} else {
						summary += wikiTexi;
					}
					full_text.append(" ").append(summary);
					//CMN.debug("索引::", full_text);
					pDoc.realm.setStringValue(realm);
					pDoc.ids.setStringValue(ids.toString());
					pDoc.full_text.setStringValue(full_text.toString());
					pDoc.title.setStringValue(toString(p.getTitle()));
					pDoc.summary.setStringValue(summary);
					writer.updateDocument(null, pDoc.doc, analyzer);
					totalEntries++;
				}
			}
		}
		writer.close();
		CMN.pt("善始善终::索引::", totalEntries); CMN.rt();
		closeIndexReader();
		built = true;
	}
	
	public void do_search() throws Exception {
		prepareSearch(true);
		CMN.rt();
		String keyWords = lastSearch;
		if(TextUtils.isEmpty(lastSearch))
			keyWords = lastSearch = "启动 搜索";
		Query query = new QueryParser(Version.LUCENE_47, "full_text", analyzer).parse(keyWords);
		//CMN.debug("query: ", query);
		int hitsPerPage = 1000;
		// 3 do search
		TopDocs docs = searcher.search(query, hitsPerPage);
		ScoreDoc[] hits = docs.scoreDocs;
		
		CMN.debug("found " + hits.length + " results", docs.totalHits);
		
		org.apache.lucene.search.highlight.QueryScorer scorer = new org.apache.lucene.search.highlight.QueryScorer(query); //显示得分高的片段(摘要)
		org.apache.lucene.search.highlight.Fragmenter fragmenter = new org.apache.lucene.search.highlight.SimpleSpanFragmenter(scorer, 1000);
		org.apache.lucene.search.highlight.SimpleHTMLFormatter simpleHTMLFormatter = new org.apache.lucene.search.highlight.SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
		highlighter = new org.apache.lucene.search.highlight.Highlighter(simpleHTMLFormatter, scorer);
		highlighter.setTextFragmenter(fragmenter);
		
		ArrayList<DocEntry> res = new ArrayList<>(hits.length);
		
		String[] words = split(keyWords);
		int heads = 0;
		for (ScoreDoc hit : hits) {
			int docId = hit.doc;
			Document doc = searcher.doc(docId);
			DocEntry entry = new DocEntry(doc);
			int score = Math.min(2, score(words, entry.title, entry.summary) - words.length);
			if (score >= 0) {
				res.add(heads++, entry);
			} else {
				res.add(entry);
			}
		}
		CMN.pt("搜索时间::", "得"+hits.length+"项目");
		
		results = res;
	}
	
	private int score(String[] words, CharSequence text, CharSequence text1) {
		int ret=0;
		for (String w : words) {
			if (TextUtils.indexOf(text, w)>=0 || TextUtils.indexOf(text1, w)>=0) {
				ret++;
			}
		}
		return ret;
	}
	
	private String[] split(String keyWords) {
		String[] raw = keyWords.split(" ");
		ArrayList<String> ret = new ArrayList<>(raw.length);
		for (String t:raw) {
			if (!TextUtils.isEmpty(t)) {
				ret.add(t);
			}
		}
		return ret.toArray(new String[]{});
	}
	
	private String toString(CharSequence text) {
		return TextUtils.isEmpty(text)?"":text.toString();
	}
	
	SettingsSearcherDlg dialog;
	String lastSearch;
	
	public void show(Activity context, int realm_id) {
		if (dialog == null) {
			dialog = new SettingsSearcherDlg();
		}
		dialog.show(context, lastSearch);
	}
	
	class SettingsSearcherDlg extends BaseAdapter implements View.OnClickListener, AdapterView.OnItemClickListener {
		WeakReference<AlertDialog> dialogRef = ViewUtils.DummyRef;
		WeakReference<Activity> contextRef = ViewUtils.DummyRef;
		Toolbar toolbar;
		EditTextmy etSearch;
		ListView schLv;
		int mForegroundColor;
		PorterDuffColorFilter ForegroundFilter;
		private View mView;
		
		private void performSearch() {
			try {
				lastSearch = etSearch.getText().toString();
				buildIndex(a);
				do_search();
				notifyDataSetChanged();
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		
		@Override
		public void onClick(View v) {
			if (v.getId()==android.R.id.button1 || v.getId()==R.id.search) {
				performSearch();
			}
		}
		
		@SuppressLint("ResourceType")
		public void show(Activity context, String text) {
			if (mView == null) {
				// getSchView
				View view = a.getLayoutInflater().inflate(R.layout.search_settings_view, null);
				ListView lv = view.findViewById(android.R.id.list);
				lv.setAdapter(this);
				lv.setMinimumHeight((int) (GlobalOptions.density*100));
				int pad = (int) (GlobalOptions.density*5);
				lv.setPadding(pad,0,pad,0);
				lv.setOnItemClickListener(this);
				toolbar = view.findViewById(R.id.toolbar);
				toolbar.inflateMenu(R.xml.menu_search);
				toolbar.setNavigationIcon(R.drawable.ic_baseline_history_24);
				toolbar.getNavigationBtn().setAlpha(0.3f);
				schLv = lv;
				etSearch = toolbar.findViewById(R.id.etSearch);
				etSearch.setText(text);
				etSearch.setOnEditorActionListener((v, actionId, event) -> {
					if (actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
						performSearch();
					}
					return true;
				});
				ActionMenuItemView ivSch = toolbar.findViewById(R.id.search);
				if (GlobalOptions.isDark) {
					etSearch.setTextColor(a.AppBlack);
				} else {
					//iv.setIcon(iv.getIcon().getConstantState().newDrawable());
					ivSch.getIcon().mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
				}
				ivSch.setOnClickListener(this);
				// 染色
				int foregroundColor = GlobalOptions.isDark ? Color.WHITE : a.MainBackground;
				ForegroundFilter = new PorterDuffColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN);
				mForegroundColor = foregroundColor;
				mView = view;
				etSearch.setTag((Runnable) () -> {
					InputMethodManager im = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
					im.showSoftInput(etSearch, 0);
				});
			}
			AlertDialog dialog = dialogRef.get();
			boolean b1 = dialog==null || dialog.getContext()!=context;
			if (b1) {
				ViewUtils.removeView(mView);
				if (dialog!=null) dialog.dismiss();
				dialog = new AlertDialog.Builder(context)
						.setPositiveButton("搜索", null)
						.setNegativeButton("取消", null)
						.setTitle("设置搜索")
						//.setWikiText("功能测试中", null)
						.setView(mView)
						.show();
				dialog.findViewById(android.R.id.button1).setOnClickListener(this);
				dialogRef = new WeakReference<>(dialog);
				contextRef = new WeakReference<>(context);
			}
			dialog.show();
			etSearch.requestFocus();
			etSearch.postDelayed((Runnable) etSearch.getTag(), b1?250: 180);
			//dialog.mAlert.wikiBtn.setAlpha(0.3f);
			ViewUtils.ensureWindowType(dialog, a, null);
		}
		
		@Override
		public int getCount() {
			return results.size();
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
			MainActivityUIBase.ViewHolder vh;
			if (convertView == null) {
				vh = new MainActivityUIBase.ViewHolder(a, R.layout.listview_item01, parent);
				ViewUtils.setVisible(vh.subtitle, true);
				ViewUtils.setVisible(vh.preview, true);
			} else {
				vh = (MainActivityUIBase.ViewHolder) convertView.getTag();
			}
			DocEntry entry = results.get(position);
			entry.bake(SettingsSearcher.this);
			vh.title.setText(entry.title/*+entry.pid*/);
			vh.title.setTextColor(a.AppBlack);
			vh.preview.setText(entry.summary);
			ViewUtils.setVisible(vh.preview, !TextUtils.isEmpty(entry.summary));
			vh.subtitle.setText(entry.path);
			return vh.itemView;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DocEntry entry = results.get(position);
			entry.bake(SettingsSearcher.this);
			CMN.debug("entry.realm::", entry.realm);
			int pref_id = all_pref[entry.realm];
			if (pref_id == BookOptions.id) {
				Activity d = contextRef.get();
				if (d instanceof FragmentActivity) {
					a.showDictOptions((FragmentActivity) d);
				}
				return;
			}
			CMN.pHandler = new WeakReference<>(a);
			Intent intent = new Intent()
					.putExtra("realm", pref_id)
					.putExtra("focus", entry.pid)
					.setClass(a, SettingsActivity.class);
			if (pref_id == Multiview.id) {
				intent.putExtra("where", a.weblistHandler.isMultiRecord()?a.weblistHandler.bMergingFrames:-1);
			}
			a.startActivity(intent);
		}
	}
}

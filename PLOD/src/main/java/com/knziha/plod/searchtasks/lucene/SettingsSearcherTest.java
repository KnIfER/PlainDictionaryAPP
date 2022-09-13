package com.knziha.plod.searchtasks.lucene;

import android.content.Context;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.searchtasks.lucene.LuceneTest;
import com.knziha.plod.searchtasks.lucene.WordBreakFilter;

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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;

public class SettingsSearcherTest {
	public IndexReader reader;
	public IndexSearcher searcher;
	
	ArrayList<PreferenceScreen> screens = new ArrayList<>();
	
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
	
	public void buildIndex(Context context) throws Exception {
		final Analyzer analyzer = WordBreakFilter.newAnalyzer();
		File folder = context.getExternalFilesDir("SettingsIndex");
		Directory index = FSDirectory.open(folder);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		config.setRAMBufferSizeMB(128);
		IndexWriter writer = new IndexWriter(index, config);
		
		DocIndex pDoc = new DocIndex();
		
		PreferenceManager mPreferenceManager = new PreferenceManager(context);
		
		PreferenceScreen screen;
		
		int[] all_pref = new int[]{R.xml.pref_main, R.xml.pref_book, R.xml.pref_dev, R.xml.pref_history, R.xml.pref_misc, R.xml.pref_multiview, R.xml.pref_nightmode, R.xml.pref_notification, R.xml.pref_tapsch};
		
		CMN.rt();
		for (int i = 0; i < all_pref.length; i++) {
			screen = mPreferenceManager.inflateFromResource(context, all_pref[i], null);
			screens.add(screen);
		}
		CMN.pt("善始善终::inflate::"); CMN.rt();
		
		int totalEntries = 0;
		
		String realm;
		
		for (int d = 0; d < all_pref.length; d++) {
			screen = screens.get(d);
			realm = screen.getKey();
			
			StringBuilder full_text = new StringBuilder();
			StringBuilder ids = new StringBuilder();
			full_text.setLength(0);
			ids.setLength(0);
			
			ArrayList<Preference> preferences = new ArrayList<>(100);
			preferences.add(screen);
			
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
						if (ids.length() > 0) ids.append(" / ");
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
		
		// search
		if(true)
		{
			CMN.rt();
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			
			String keyWords = "搜索词";
			
			Query query = new QueryParser(Version.LUCENE_47, "full_text", analyzer).parse(keyWords);
			
//			String[] stringQuery = {keyWords, keyWords, keyWords};
//			String[] fields = {"full_text", "title", "summary"};
//			BooleanClause.Occur[] occ={BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
//			query  = MultiFieldQueryParser.parse(Version.LUCENE_47, stringQuery, fields, occ, analyzer);
			
			CMN.Log(LuceneTest.styles);

			CMN.Log("query: ", query);
			
			
			int hitsPerPage = 1000;
			// 3 do search
			TopDocs docs = searcher.search(query, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;
			
			CMN.Log("found " + hits.length + " results", docs.totalHits);
			
			org.apache.lucene.search.highlight.QueryScorer scorer=new org.apache.lucene.search.highlight.QueryScorer(query); //显示得分高的片段(摘要)
			org.apache.lucene.search.highlight.Fragmenter fragmenter=new org.apache.lucene.search.highlight.SimpleSpanFragmenter(scorer, 1000);
			org.apache.lucene.search.highlight.SimpleHTMLFormatter simpleHTMLFormatter=new org.apache.lucene.search.highlight.SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
			org.apache.lucene.search.highlight.Highlighter highlighter=new org.apache.lucene.search.highlight.Highlighter(simpleHTMLFormatter,scorer);
			highlighter.setTextFragmenter(fragmenter);
			
			for(ScoreDoc hit : hits) {
				CMN.Log("<br/><br/>\r\n");
				int docId = hit.doc;
				Document doc = searcher.doc(docId);
				String text = doc.get("title");
				CMN.Log("<h1 class='title'><a href=''>"+text+"</a> </h1>");
				if(text!=null) {
					//if(false)
					try {
						/*把权重高的显示出来*/
						TokenStream tokenStream=analyzer.tokenStream("desc", new StringReader(text));
						String str = highlighter.getBestFragment(tokenStream, text);
						CMN.Log("<div class='preview'>"+str+(" ("+hit.score+") ")+"</div>");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				text = doc.get("summary");
				if(text!=null) {
					//if(false)
					try {
						TokenStream tokenStream=analyzer.tokenStream("desc", new StringReader(text));
						String str = highlighter.getBestFragment(tokenStream, text);
						CMN.Log("<div class='preview'>"+str+(" ("+hit.score+") ")+"</div>");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			CMN.pt("搜索时间::");
			
		}
		CMN.pt("善始善终::搜索::"); CMN.rt();
	}
	
	private String toString(CharSequence text) {
		return TextUtils.isEmpty(text)?"":text.toString();
	}
}

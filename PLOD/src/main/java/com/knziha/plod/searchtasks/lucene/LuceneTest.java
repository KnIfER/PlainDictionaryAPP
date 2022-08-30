package com.knziha.plod.searchtasks.lucene;

import android.text.Html;

import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.knziha.metaline.Metaline;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LuceneTest {
	
	public static void test(MainActivityUIBase a) {
		try {
			// create analyzer and directory
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			Directory index = FSDirectory.open(new File("/sdcard/PLOD/lucene-demo-index"));
//			Directory index = FSDirectory.open(Paths.get("/sdcard/PLOD/lucene-demo-index", new String[0]));
			
			
			// indexing
			// 1 create index-writer
			mdict md = (mdict) a.currentDictionary.bookImpl;
			// 2 write index
			if(true) // 测试 OED2 (24MB) 建立索引耗时。
			{
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
				config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
				CMN.rt();
				IndexWriter writer = new IndexWriter(index, config);
				for (int i = 0; i < md.getNumberEntries(); i++) {
					Document doc = new Document();
					doc.add(new TextField("entry", md.getEntryAt(i), Field.Store.YES));
					doc.add(new StringField("bookName", md.getDictionaryName(), Field.Store.YES));
					String text = md.getRecordAt(i);
					text = org.jsoup.Jsoup.parse(text).text(); // 80s
					// text = org.jsoup.Jsoup.parseBodyFragment(text).text(); //
					//text = Html.fromHtml(html).toString(); // 150s
					// CMN.Log(text);
					doc.add(new TextField("content", text, Field.Store.YES));
					writer.addDocument(doc);
				}
				writer.close();
				CMN.pt("索引时间::");
			}
			
			
			// search
			if(true)
			{
				CMN.rt();
				IndexReader reader = DirectoryReader.open(index);
				IndexSearcher searcher = new IndexSearcher(reader);
				
				
				//要查找的字符串数组
				String[] stringQuery = {"dollar", "ship"};
				stringQuery = new String[]{"ship's", "dollar"};
				stringQuery = new String[]{"ship's", "dollar"};
				
				String[] fields = {"content", "content"};
				
				BooleanClause.Occur[] occ={BooleanClause.Occur.MUST, BooleanClause.Occur.MUST};
				
				Query query;// = MultiFieldQueryParser.parse(Version.LUCENE_47, stringQuery, fields, occ, analyzer);
				
				query = new QueryParser(Version.LUCENE_47, "content", analyzer).parse("美丽国");
				query = new QueryParser(Version.LUCENE_47, "content", analyzer).parse("使更加美丽");
				
				CMN.Log(styles);
				
				CMN.Log("query: ", query);
				
				int hitsPerPage = 100;
				// 3 do search
				TopDocs docs = searcher.search(query, hitsPerPage);
				ScoreDoc[] hits = docs.scoreDocs;
				
				CMN.Log("found " + hits.length + " results", docs.totalHits);
				
				
				org.apache.lucene.search.highlight.QueryScorer scorer=new org.apache.lucene.search.highlight.QueryScorer(query); //显示得分高的片段(摘要)
				org.apache.lucene.search.highlight.Fragmenter fragmenter=new org.apache.lucene.search.highlight.SimpleSpanFragmenter(scorer);
				org.apache.lucene.search.highlight.SimpleHTMLFormatter simpleHTMLFormatter=new org.apache.lucene.search.highlight.SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
				org.apache.lucene.search.highlight.Highlighter highlighter=new org.apache.lucene.search.highlight.Highlighter(simpleHTMLFormatter,scorer);
				highlighter.setTextFragmenter(fragmenter);
				
				
				for(ScoreDoc hit : hits) {
					CMN.Log("<br/><br/>\r\n");
					int docId = hit.doc;
					Document doc = searcher.doc(docId);
					String text = doc.get("content");
					CMN.Log("<h1 class='title'><a href=''>"+doc.get("entry")+"</a> </h1>");
					if(text!=null) {
						//if(false)
						try {
							String bookName = doc.get("bookName");
							//bookName = "简明英汉汉英词典";
							String dt = "<span class='dt'>"+bookName+"</span>";
							/*把权重高的显示出来*/
							TokenStream tokenStream=analyzer.tokenStream("desc", new StringReader(text));
							String str = highlighter.getBestFragment(tokenStream, text);
							CMN.Log("<div class='preview'>"+dt+str+(" ("+hit.score+") ")+"</div>");
							continue;
						} catch (Exception e) {
							e.printStackTrace();
						}
						CMN.Log("<br/>---15字::", text.substring(0, Math.min(15, text.length())));
					}
				}
				CMN.pt("搜索时间:");
				
			}
			
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	
	
	/**<meta name="viewport" content="width=device-width, initial-scale=1" />

<style>
.title {
    line-height: 22px;
	text-transform:capitalize
}
.preview {
    word-wrap: break-word;
    word-break: break-word;
	font-size: 18px;
}
.dt{
	    color: #717994;
}
.dt::after{
	content: ' — ';
	color: #717994;
}
body{
	padding-left:35px
}
</style> */
	@Metaline(trim = false)
	static String styles = "";
}

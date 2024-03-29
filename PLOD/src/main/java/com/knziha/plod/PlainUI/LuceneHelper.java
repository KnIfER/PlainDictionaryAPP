package com.knziha.plod.PlainUI;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.AnyThread;
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymanager.BookViewHolder;
import com.knziha.plod.dictionarymodels.resultRecorderLucene;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.searchtasks.IndexBuildingTask;
import com.knziha.plod.searchtasks.lucene.WordBreakFilter;
import com.knziha.plod.widgets.ViewUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	int PageSize = 100;
	
	public long indexSize;
	public boolean indexChanged;
	
	public LuceneHelper(PDICMainActivity a, SearchToolsMenu schTools) {
		this.a = a;
		this.schTools = schTools;
		prepareSearch(false);
	}
	
	@AnyThread
	public resultRecorderLucene do_search(resultRecorderLucene results, HashMap<String, Integer> map) throws Exception {
		int hitsPerPage = PageSize;
		Analyzer analyzer = this.analyzer;
		String phrase = CurrentSearchText;
		IndexSearcher searcher = prepareSearch(true);
		CMN.rt("搜索引擎 phrase="+phrase);
		Query query;
		TopDocs docs;
		if (results == null) {
			results = new resultRecorderLucene(a, a.loadManager, hitsPerPage);
			query = new QueryParser(Version.LUCENE_47, "content", analyzer).parse(phrase);
			CMN.debug("breaked query::", query, "phrase="+phrase);
			//CMN.debug("test break query::", new QueryParser(Version.LUCENE_47, "content", new SimpleAnalyzer(Version.LUCENE_47)).parse("\uD87E\uDE1D"));
			//CMN.debug("test break query::", new QueryParser(Version.LUCENE_47, "content", new SimpleAnalyzer(Version.LUCENE_47)).parse("\uD880\uDC00"));
			//CMN.debug("test break query::", new QueryParser(Version.LUCENE_47, "content", new SimpleAnalyzer(Version.LUCENE_47)).parse("\uD884\uDF50")); // 汉语大字典
			boolean debug_query = false;// GlobalOptions.debug;
			if (phrase.contains("\"") || debug_query) {
				// https://blog.csdn.net/sinat_27171121/article/details/128172024
				try {
					String raw;
					Pattern p = Pattern.compile("\"(.+?)\"");
					boolean hasWildcardInQuote = false;
					boolean found = false;
					Matcher m = p.matcher(phrase);
					while(m.find()) {
						String raw_term = m.group(1);
						CMN.debug("raw_term::", raw_term);
						if (raw_term.contains("?")||raw_term.contains("*")) {
							hasWildcardInQuote = true;
							break;
						}
					}
					if (hasWildcardInQuote) {
						// 双引号内有通配符，则不能传普通QueryParser解析出来的，因为通配符被QueryParser丢掉了。
						raw = phrase;
					} else {
						p = Pattern.compile("content:\"(.+?)\"");
						raw = query.toString();
						CMN.debug("raw::", raw);
						m = p.matcher(raw);
						StringBuffer sb = new StringBuffer();
						while(m.find()) {
							found = true;
							m.appendReplacement(sb, "");
							String raw_term = m.group(1);
							int now = 0, last=-1;
							// loop in "raw_term"
							while (now < raw_term.length()) {
								if (raw_term.charAt(now)==' ') {
									boolean shouldStrip = true;
									if (now-last-1 <= 1) shouldStrip = false;
									if (shouldStrip) {
										boolean quanYingWen = true;
										for (int i = last+1; i < now; i++) {
											if (WordBreakFilter.isBigram(raw_term.charAt(i))) {
												quanYingWen = false;
												break;
											}
										}
										if(quanYingWen) shouldStrip = false;
									}
									if (shouldStrip) last = now;
									else break;
								}
								now ++ ;
							}
							if (last > 0) raw_term = raw_term.substring(last+1);
							sb.append("content:\"").append(raw_term).append("\"");
						}
						if (found || debug_query) {
							m.appendTail(sb);
							raw = sb.toString();
						}
					}
					
					if (found || debug_query || hasWildcardInQuote)
					{
						CMN.debug("修正前 query::", raw, query, "hasWildcardInQuote="+hasWildcardInQuote);
						Query refQuery;
						if (hasWildcardInQuote) {
							// 双引号内有通配符，直接分析phrase，忽略停止词、分词等的handle。
							refQuery = new ComplexPhraseQueryParser(Version.LUCENE_47, "content", new SimpleAnalyzer(Version.LUCENE_47)).parse(phrase);
						} else {
							refQuery = new QueryParser(Version.LUCENE_47, "content", new SimpleAnalyzer(Version.LUCENE_47)).parse(raw);
						}
//						refQuery = new QueryParser(Version.LUCENE_47, "content", new SimpleAnalyzer(Version.LUCENE_47)).parse("content:\"brie* space\"");
//						refQuery = new ComplexPhraseQueryParser(Version.LUCENE_47, "content", new SimpleAnalyzer(Version.LUCENE_47)).parse("brie* space");
//						phraseQuery.add(new Term("content", "brie?"));
//						phraseQuery.add(new Term("content", "space"));
//						refQuery = phraseQuery;
						
						CMN.debug("修正后 query::", refQuery.equals(new QueryParser(Version.LUCENE_47, "content", new StandardAnalyzer(Version.LUCENE_47)).parse(phrase)), refQuery);
						query = refQuery;
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
//			analyzer = new StandardAnalyzer(Version.LUCENE_47);
//			query = new QueryParser(Version.LUCENE_47, "content", analyzer).parse(phrase);
//			CMN.debug("standard query::", new QueryParser(Version.LUCENE_47, "content", analyzer).parse(phrase));
//			Query reflectQuery = new QueryParser(Version.LUCENE_47, "content", analyzer).parse(query.toString());
//			CMN.debug("reflect query::", reflectQuery, query.equals(reflectQuery));
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
					return results;
				}
				booleanQuery.add(dictQueries, BooleanClause.Occur.MUST);
				booleanQuery.add(query, BooleanClause.Occur.MUST);
				query = booleanQuery;
			}
			CMN.debug("query::", query);
			results.query = query;
			results.helper = this;
			results.analyzer = analyzer;
			results.searcher = searcher;
			docs = searcher.search(query, hitsPerPage);
			
			org.apache.lucene.search.highlight.QueryScorer scorer=new org.apache.lucene.search.highlight.QueryScorer(query); //显示得分高的片段(摘要)
			org.apache.lucene.search.highlight.Fragmenter fragmenter=new org.apache.lucene.search.highlight.SimpleSpanFragmenter(scorer);
			org.apache.lucene.search.highlight.SimpleHTMLFormatter simpleHTMLFormatter=new org.apache.lucene.search.highlight.SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
			results.highlighter=new org.apache.lucene.search.highlight.Highlighter(simpleHTMLFormatter,scorer);
			results.highlighter.setTextFragmenter(fragmenter);
		} else {
			hitsPerPage = results.pageSize;
			searcher = results.searcher;
			query = results.query;
			
			docs = searcher.searchAfter(results.lastDoc(), query, hitsPerPage);
		}
		CMN.pt("搜索时间::", "found " + docs.scoreDocs.length + " results", docs.totalHits, "max="+docs.getMaxScore());
		
		resultRecorderLucene.DocRecord[] newPage = new resultRecorderLucene.DocRecord[docs.scoreDocs.length];
		for (int i = 0; i < newPage.length; i++) {
			newPage[i] = new resultRecorderLucene.DocRecord(docs.scoreDocs[i]);
		}
		results.newPage = newPage;
		return results;
	}
	
	public void closeIndexReader() {
		if (reader != null) {
			try {
				reader.close(); // todo delay
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
		d.show(CurrentSearchText);
	}
	
	public static boolean isIndexable(String pathname) {
		return pathname !=null && (
				pathname.regionMatches(true, pathname.length()-4, ".mdx", 0, 4)
				|| pathname.regionMatches(true, pathname.length()-4, ".txt", 0, 4)
				|| pathname.regionMatches(true, pathname.length()-4, ".dsl", 0, 4)
				|| pathname.regionMatches(true, pathname.length()-7, ".dsl.dz", 0, 7)
				);
	}
	
	
	
	WeakReference<AlertDialog> indexBuilderDlg = ViewUtils.DummyRef;
	
	private void startBuildIndexesTask(int rebuild) {
		AlertDialog dTmp = indexBuilderDlg.get();
		if(dTmp!=null) dTmp.dismiss();
		rebuildIndexes = rebuild;
		if (rebuild != 0) {
			indexingTasks = 0;
		}
		indexingTasks += indexingBooks.size();
		new IndexBuildingTask((PDICMainActivity) a).execute(this);
	}
	
	private void startBuildIndexes() {
		if (indexingBooks.size()>0) {
			final File folder = new File(a.opt.pathToMainFolder().toString());
			rebuildIndexes = 0;
			indexingTasks = 0;
			
			final AlertDialog dTmp = new AlertDialog.Builder(a)
					.setTitle("准备索引程序")
					.setMessage("正在扫描索引…")
					.setPositiveButton("开始！", null)
					.setNeutralButton("只索引新词典", null)
					.setNegativeButton("取消", null)
					.show()
			;
			ArrayList<String> indexedBookNames = new ArrayList<>();
			View.OnClickListener onClickListener = v -> {
				if (v.getId() == android.R.id.button1) {
					if (indexedBookNames.size() > 0) {
						new AlertDialog.Builder(a)
								.setTitle("是否要重建索引？")
								.setMessage(indexedBookNames.get(0))
								.setPositiveButton("立即开始！", (dialog, which) -> {
									if (freeSpaces[0] > freeSpaces[1]) {
										startBuildIndexesTask(1);
									} else {
										a.showT("存储空间不足！读数："+mp4meta.utils.CMN.formatSize(freeSpaces[0]));
									}
									dTmp.dismiss();
								})
								.setNegativeButton("取消", null)
								.setNeutralButton("忽略(不推荐)", (dialog, which) -> {
									if (freeSpaces[0] > freeSpaces[1]) {
										startBuildIndexesTask(2);
									} else {
										a.showT("存储空间不足！读数："+mp4meta.utils.CMN.formatSize(freeSpaces[0]));
									}
									dTmp.dismiss();
								})
								.show()
						;
					}
					else {
						startBuildIndexesTask(0);
						dTmp.dismiss();
					}
				} else if (v.getId() == android.R.id.button2) {
					dTmp.dismiss();
				} else if (v.getId() == android.R.id.button3) {
					startBuildIndexesTask(0);
					dTmp.dismiss();
				}
			};
			ViewGroup btnPanel = (ViewGroup) dTmp.findViewById(R.id.buttonPanel);
			Button btn3 = btnPanel.findViewById(android.R.id.button3);
			Button btn = btnPanel.findViewById(android.R.id.button1);
			
			ViewUtils.setOnClickListenersOneDepth(btnPanel, onClickListener, 999, 0, null);
			btn.setEnabled(false);
			btn3.setEnabled(false);
			// dTmp.setCancelable(false);
			
			prepareSearch(false);
			reloadIndexedBookList();
			MainActivityUIBase.LoadManager loadManager = a.loadManager;
			int size = loadManager.md_size;
			long totalSz = 0, newSz=0, total=0, new_=0;
			for(int i=0;i<size;i++){
				PlaceHolder ph = loadManager.getPlaceHolderAt(i);
				if (indexingBooks.contains(ph)) {
					File f = ph.getPath(a.opt);
					String name = f.getName();
					long len = f.length();
					if(ph.pathname.startsWith(CMN.AssetTag))
						len = IU.parsint(CMN.AssetMap.get(f.getName()), 0);
					totalSz += len;
					total++;
					if (indexedbooksMap.contains(name)) {
						indexedBookNames.add(name);
					} else {
						newSz += len;
						new_++;
					}
				}
			}
			long freeSpace = freeSpaces[0] = folder.getFreeSpace();
			freeSpaces[1] = totalSz*5;
			freeSpaces[2] = newSz*5;
			StringBuilder sb = new StringBuilder();
			int est = (int) Math.ceil(totalSz*1.5/1024/1024/24 + 0.35);
			sb.append("将索引 ").append(total).append(" 本词典(")
					.append(mp4meta.utils.CMN.formatSize(totalSz)).append(")，预计耗时 ")
					.append(est).append(" 分钟，需 ").append(mp4meta.utils.CMN.formatSize(freeSpaces[1])).append(" 存储空间。\n");
			boolean hasIndexed = indexedBookNames.size() > 0;
			indexingTasks = -indexedBookNames.size();
			if (hasIndexed) {
				sb.append("\n以下词典已存在索引：");
				for (String name:indexedBookNames) {
					sb.append(name);
					sb.append(", ");
				}
				sb.setLength(sb.length() - 2);
				est = (int) Math.ceil(newSz*1.5/1024/1024/24 + 0.35);
				sb.append("\n\n若只索引新词典，将索引 ").append(new_).append(" 本词典(")
						.append(mp4meta.utils.CMN.formatSize(newSz)).append(")，预计耗时 ")
						.append(est).append(" 分钟，需 ").append(mp4meta.utils.CMN.formatSize(freeSpaces[2])).append(" 存储空间。\n");
			}
			
			if (freeSpace >= freeSpaces[2]) {
				dTmp.setMessage(sb);
				btn.setEnabled(true);
				btn3.setEnabled(true);
			} else {
				sb.append("\n\n错误：存储空间不足 (读数 ")
						.append(mp4meta.utils.CMN.formatSize(freeSpace))
						.append("）！请清理磁盘空间后重试。")
				;
				dTmp.setMessage(sb);
			}
			if (!hasIndexed) {
				btn.setText("立即开始！");
			} else {
				sb.setLength(0);
				sb.append("将重建以下 ").append(indexedBookNames.size()).append(" 本词典(")
						.append(mp4meta.utils.CMN.formatSize(totalSz - newSz)).append(")的索引！\n");
				for (String name:indexedBookNames) {
					sb.append(name);
					sb.append("\n");
				}
				indexedBookNames.clear();
				indexedBookNames.add(sb.toString());
			}
			ViewUtils.setVisibleV3(btn3, hasIndexed);
		}
	}
	
	public void showBuildIndexDlg() {
		MainActivityUIBase.LoadManager loadMan = a.loadManager;  // 建立全文索引
		AlertDialog dTmp = indexBuilderDlg.get();
		indexingBooks.clear();
		if (!ViewUtils.isVisibleV2(a.lv2))
		{
			indexingBooks.add(a.currentDictionary.placeHolder);
		}
		if (dTmp==null) {
			View.OnClickListener onClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getId() == android.R.id.button1) {
						startBuildIndexes();
					} else {
						BookViewHolder vh
								= (BookViewHolder) ViewUtils.getViewHolderInParents(v, BookViewHolder.class);
						if (vh != null) {
							PlaceHolder ph = a.loadManager.getPlaceHolderAt(vh.position);
							if (ph != null) {
								boolean val = vh.ck.isChecked();
								if (v!=vh.ck) {
									val = !val;
									vh.ck.setChecked(val);
								}
								if (val) indexingBooks.add(ph);
								else indexingBooks.remove(ph);
							}
						}
					}
				}
			};
			dTmp = new AlertDialog.Builder(a)
					.setTitle("建立全文索引")
					.setSingleChoiceItems(new CharSequence[]{}, 0, null)
					.setAdapter(new BaseAdapter() {
						@Override
						public int getCount() {
							return loadMan.lazyMan.chairCount;
						}
						@Override
						public Object getItem(int position) {
							return loadMan.lazyMan.CosyChair[position];
						}
						@Override
						public long getItemId(int position) {
							return 0;
						}
						@Override
						public View getView(int position, View convertView, ViewGroup parent) {
							BookViewHolder vh;
							if (convertView == null) {
								convertView = a.getLayoutInflater().inflate(R.layout.dict_manager_listrow, parent, false);
								vh = new BookViewHolder(convertView);
								vh.title.setOnClickListener(onClickListener);
								vh.ck.setOnClickListener(onClickListener);
							} else {
								vh = (BookViewHolder) convertView.getTag();
							}
							vh.position = position;
							PlaceHolder ph = a.loadManager.getPlaceHolderAt(position);
							boolean enabled = LuceneHelper.isIndexable(ph.pathname);
							vh.title.setText(a.loadManager.md_getName(position, -1));
							ViewUtils.setVisibleV3(vh.ck, enabled);
							vh.ck.setChecked(enabled && indexingBooks.contains(ph));
							vh.title.setEnabled(enabled);
							vh.title.setTextColor(enabled ? a.AppBlack : Color.GRAY);
							if(GlobalOptions.isDark) {
								convertView.getBackground().setColorFilter(0x39ffffff & a.AppBlack, PorterDuff.Mode.SRC_IN);
							}
							vh.handle.setVisibility(View.GONE);
							return convertView;
						}
					}, null)
					.setPositiveButton("开始！", null)
					.setNegativeButton("取消", null)
					.show();
			dTmp.findViewById(android.R.id.button1).setOnClickListener(onClickListener);
			indexBuilderDlg = new WeakReference<>(dTmp);
		}
		ViewUtils.ensureWindowType(dTmp, a, null);
		dTmp.show();
		ListView lv = dTmp.getListView();
		float pad = 2.8f * a.mResource.getDimension(R.dimen._50_) * (a.dm.widthPixels>GlobalOptions.realWidth?1:1.45f);
		View root = a.root;
		((AlertController.RecycleListView) lv).mMaxHeight = root.getHeight()>=2*pad?(int) (root.getHeight() - root.getPaddingTop() - pad):0;
	}
	
	public void deleteIndex(String bookName) throws IOException {
		final Analyzer analyzer = WordBreakFilter.newAnalyzer();
		File folder = new File(a.opt.pathToMainFolder().append("lucene").toString());
		Directory index = FSDirectory.open(folder);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		IndexWriter writer = new IndexWriter(index, config);
		writer.deleteDocuments(new TermQuery(new Term("bookName", bookName)));
		writer.close();
		closeIndexReader();
		prepareSearch(false);
		for(IndexedBook rec:indexedbooks)  {
			if (rec.name.equals(bookName)) {
				//indexedbooks.remove(rec);
				reloadIndexedBookList();
				break;
			}
		}
	}
}

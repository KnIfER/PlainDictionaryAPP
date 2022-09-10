package com.knziha.plod.dictionarymodels;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import com.knziha.plod.PlainUI.LuceneHelper;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.text.RoundedBackgroundSpan;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/** 保存全文搜索的结果。todo 暂存结果至内存 */
public class resultRecorderLucene extends resultRecorderDiscrete {
	/** not a copy of the main loadManager */
	private MainActivityUIBase.LoadManager loadManager;
	private long[] firstLookUpTable = ArrayUtils.EMPTY_LONG_ARRAY;
	private ArrayList<DocRecord[]> results = new ArrayList<>();
	private int result_size;
	private boolean mShouldSaveHistory;
	BookPresenter TargetBook;
	public LuceneHelper helper;
	private boolean bHasNext;
	public Analyzer analyzer;
	public Query query;
	public IndexSearcher searcher;
	public Highlighter highlighter;
	public final int pageSize;
	
	public static class DocRecord {
		public final int did;
		public final float score;
		long bookId = -1;
		/** 词条名 */
		String entry;
		/** 词条位置 */
		int position = -1;
		/** 词条预览 */
		String preview;
		/** 书籍名称 */
		String bookName;
		public DocRecord(ScoreDoc scoreDoc) {
			did = scoreDoc.doc;
			score = scoreDoc.score;
		}
		void parse(resultRecorderLucene res) throws Exception {
			Document document = null;
			if (bookName==null || preview==null || entry==null || position==-1) {
				document = res.searcher.doc(did);
			}
			if (document != null) {
				if (bookName==null) {
					bookName = document.get("bookName");
				}
				if (position==-1) {
					position = IU.parsint(document.get("position"), -2);
				}
				if (preview==null) {
					entry = document.get("entry");
				}
				if (preview==null) {
					String content = document.get("content");
					TokenStream tokenStream=res.analyzer.tokenStream("desc", new StringReader(content));
					String str = res.highlighter.getBestFragment(tokenStream, content).trim();
					if (str.startsWith(entry)) {
						str = str.substring(entry.length()).trim();
					}
					String dt = "<span class='dt'>"+bookName+"</span>";
					preview = str;
				}
			}
		}
		public long getBookId(resultRecorderLucene res) {
			if (bookId==-1) {
				try { parse(res); } catch (Exception e) { CMN.debug(e); }
				bookId = LexicalDBHelper.getInstance().getBookID(null, bookName); //null
			}
			return bookId;
		}
		public String getEntry(resultRecorderLucene res) {
			if (entry==null) {
				try { parse(res); } catch (Exception e) { CMN.debug(e); }
			}
			return entry;
		}
		public String getPreview(resultRecorderLucene res) {
			if (preview==null) {
				try { parse(res); } catch (Exception e) { CMN.debug(e); }
			}
			return preview;
		}
		public String getContent(resultRecorderLucene res) {
			try {
				Document document = res.searcher.doc(did);
				return document.get("content");
			} catch (Exception e) {
				CMN.debug(e);
			}
			return null;
		}
		public int getPosition(resultRecorderLucene res) {
			if (position==-1) {
				try { parse(res); } catch (Exception e) { CMN.debug(e); }
			}
			return position;
		}
	}
	
	public boolean loadNextPage(MainActivityUIBase a, int pos, View view) {
		if (bHasNext && pos == result_size) {
			try {
				newPage = null;
				helper.do_search(this, null);
				
				addPage(a, newPage);
			} catch (Exception e) {
				CMN.debug(e);
			}
			return true;
		}
		return false;
	}
	
	public DocRecord[] newPage;
	public void invalidate(MainActivityUIBase a, BookPresenter targetBook) {
		if (targetBook==a.EmptyBook && TargetBook!=null) {
			targetBook = TargetBook;
		}
		TargetBook = targetBook;
		
		booksSet = new HashSet<>(a.loadManager.md_size);
		addPage(a, newPage);
		
		// loadManager = a.loadManager.clone(); // todo copy onChange
		if (targetBook == null) { // 联合
		}
		else { // 单本
			booksSet.add(targetBook.getId());
		}
//		loadManager.dictPicker.underlined = booksSet;
//		loadManager.dictPicker.dataChanged();
	}
	
	private void addPage(MainActivityUIBase a, DocRecord[] newPage) {
		if (newPage != null && newPage.length > 0) {
			results.add(newPage);
			int ts = results.size() + 1;
			if (firstLookUpTable.length < ts) {
				long[] tmp = new long[Math.max(ts, (int) (ts * 1.5))];
				System.arraycopy(firstLookUpTable, 0, tmp, 0, firstLookUpTable.length);
				firstLookUpTable = tmp;
			}
			result_size += newPage.length;
			firstLookUpTable[ts - 1] = result_size;
			bHasNext = newPage.length == pageSize;
		} else {
			bHasNext = false;
		}
		a.listName(2).setText("搜索引擎"+" ("+result_size+")");
		a.adaptermy5.notifyDataSetChanged();
	}
	
	public resultRecorderLucene(MainActivityUIBase a, MainActivityUIBase.LoadManager loadManager_, int pageSize){
		super(a);
		loadManager=loadManager_;
		this.pageSize = pageSize;
	}
	
	@Override
	public ArrayList<Long> getBooksAt(ArrayList<Long> books, int pos) {
//		if(books==null) books=new ArrayList<>();
//		else {books.clear();}
//		if(results_size<=0 || pos<0 || pos>results_size-1)
//			return books;
//		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
//		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
//			return books;
//		books.add(firstLookUpTable[Rgn+1]);
		return books;
	}
	
	public DocRecord getDocAt(int pos) {
		int rgn = Arrays.binarySearch(firstLookUpTable, 0, results.size(), pos);
		if (rgn < 0) rgn = -2-rgn;
		if (rgn >=0 && rgn< results.size()) {
			int position = (int) (pos - firstLookUpTable[rgn]);
			DocRecord[] page = results.get(rgn);
			if (position >= 0 && position < page.length) {
				return page[position];
			}
		}
		return null;
	}
	
	@Override
	public long getOneDictAt(int pos) {
		DocRecord record = getDocAt(pos);
		return record==null?0:record.getBookId(this);
	}

	@Override
	public CharSequence getResAt(MainActivityUIBase a, long pos) {
		if (pos < 0 || pos >= result_size + 1) {
			if (pos == -1) {
				return "←";
			}
			if (pos == result_size + 1) {
				return "→";
			}
			return "!!! Error: code 1";
		}
		if (pos == result_size) {
			return "— 加载下一页 —";
		}
		DocRecord record = getDocAt((int) pos);
		if (record != null) {
			bookId = record.getBookId(this);
			return record.getEntry(this);
		}
		return "!!! Error: code 2 " + pos + " " + result_size + " " + results.size();
	}
	
	public CharSequence getPreviewAt(BookPresenter book, MainActivityUIBase a, int pos, MainActivityUIBase.ViewHolder vh) {
		if (pos == result_size) {
			DocRecord record0 = getDocAt(0);
			DocRecord record = getDocAt(result_size - 1);
			if (record0==null || record==null) {
				return "!!! Error: code 1";
			}
			StringBuilder str = new StringBuilder();
			str.append("共 ").append(result_size).append(" 项，");
			str.append("当前得分 : ").append(record0.score);
			ViewUtils.trimFloat(str, 2);
			str.append(" ~ ").append(record.score);
			ViewUtils.trimFloat(str, 2);
			str.append("。");
			return str;
		}
		if (PDICMainAppOptions.listPreviewEnabled1()) {
			try {
				DocRecord record = getDocAt(pos);
				if (record==null) {
					return "!!! Error: code 1";
				}
				if(true) return Html.fromHtml(/*record.score+" "+*/record.getPreview(this) );
				SpannableStringBuilder ssb = new SpannableStringBuilder();
				ssb.append(book.getInListName());
				//ssb.setSpan(new ColoredTextSpan1(0xFFb0b0b0), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);0xFFb9b9b9
				ssb.setSpan(new RoundedBackgroundSpan(0, 0x88FFFFFF & a.MainAppBackground, 0, 0, 0, 0), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//				ssb.setSpan(new BookNameSpan(0xFFb0b0b0), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				Spanned preview = Html.fromHtml(record.preview);
				if (preview.length()>0 && preview.charAt(0)!=' ')
					ssb.append(" ");
				ssb.append(preview);
				return ssb;
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		return null;
	}
	
	/** 调试索引分词效果 */
	public void testTokenStream(String content) {
		try {
			CMN.debug(content);
			CMN.debug("\n分析开始……");
			TokenStream tokenStream = null;
			tokenStream = analyzer.tokenStream("test", content);
			//添加一个引用，可以获得每个关键词
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
			//添加一个偏移量的引用，记录了关键词的开始位置以及结束位置
			OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
			//将指针调整到列表的头部
			tokenStream.reset();
			//遍历关键词列表，通过incrementToken方法判断列表是否结束
			while (tokenStream.incrementToken()) {
				CMN.Log(charTermAttribute
						+ " start->" + offsetAttribute.startOffset()
						+ " end->" + offsetAttribute.endOffset());
			}
			tokenStream.close();
			CMN.Log("\n分析结束……");
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	@Override
	public void renderContentAt(long pos, MainActivityUIBase a, BasicAdapter ADA, WebViewListHandler weblistHandler){//ViewGroup X
		getResAt(a, pos);
		if(result_size <=0 || pos<0 || pos>= result_size)
			return;
		DocRecord record = getDocAt((int) pos);
		if(record==null) return;
		try { CMN.Log(searcher.explain(query, record.did)); } catch (Exception ignored) { }
//		testTokenStream(record.getContent(this));
		BookPresenter presenter = a.getBookById(record.getBookId(this));
		if(presenter==a.EmptyBook){
			CMN.Log("!!! Error: lazy load error failed.", record.getBookId(this));
			return;
		}
		// nimp
		//mShouldSaveHistory = !(mdtmp instanceof bookPresenter_txt);
		mShouldSaveHistory = true;
		bookId = presenter.getId();
		int entryPos = record.getPosition(this);
		String entryText = record.getEntry(this);
		if (entryPos >= 0 && entryPos < presenter.bookImpl.getNumberEntries()) {
			String entry = presenter.bookImpl.getEntryAt(entryPos);
			if (!mdict.processText(entry).equals(mdict.processText(entryText))) {
				entryPos = -1;
			}
		} else {
			entryPos = -1;
		}
		if (entryPos < 0) {
			entryPos = presenter.bookImpl.lookUp(entryText);
		}
		if(entryPos >= 0) {
			boolean bUseDictView = presenter.bookImpl.hasVirtualIndex();
			WebViewmy mWebView;
			if(bUseDictView) {
				presenter.initViewsHolder(a);
				mWebView = presenter.mWebView;
			} else {
				mWebView = weblistHandler.getMergedFrame(presenter);
			}
			float desiredScale = a.prepareSingleWebviewForAda(presenter, mWebView, pos, ADA, a.opt.getRemPos(), a.opt.getInheritePageScale());
			
			ViewUtils.addViewToParentUnique(mWebView.rl, a.webSingleholder);
			
			mWebView.weblistHandler = weblistHandler;
			presenter.renderContentAt(desiredScale, BookPresenter.RENDERFLAG_NEW ,1 , mWebView, entryPos);
//				presenter.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
//				mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
			a.contentUIData.PageSlider.setWebview(mWebView, null);
		}
	}

	@Override
	public boolean shouldSaveHistory() {
		return mShouldSaveHistory;
	}

	@Override
	public int size() {
		return bHasNext ? result_size + 1 : result_size;
	}
	
	@Override
	public void shutUp() {
		results.clear();
		result_size = 0;
	}
	
	public boolean hasNextPage(int position) {
		return bHasNext && (position == result_size);
	}
	
	public ScoreDoc lastDoc() {
		if (results.size() > 0) {
			DocRecord[] arr = results.get(0);
			DocRecord doc = arr[arr.length - 1];
			return new ScoreDoc(doc.did, doc.score);
		}
		return null;
	}
}

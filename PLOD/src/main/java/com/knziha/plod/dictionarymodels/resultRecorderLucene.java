package com.knziha.plod.dictionarymodels;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;

import com.knziha.plod.PlainUI.LuceneHelper;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.text.ColoredTextSpan1;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;

/** 保存全文搜索的结果。todo 暂存结果至内存 */
public class resultRecorderLucene extends resultRecorderDiscrete {
	/** not a copy of the main loadManager */
	private MainActivityUIBase.LoadManager loadManager;
	private ArrayList<DocRecord> results = new ArrayList<>();
	private boolean mShouldSaveHistory;
	BookPresenter TargetBook;
	private LuceneHelper helper;
	
	class DocRecord{
		String entry;
		String text;
		String preview;
		String bookName;
		long bookId = -1;
		final ScoreDoc scoreDoc;
		Document document;
		DocRecord(ScoreDoc scoreDoc) {
			this.scoreDoc = scoreDoc;
		}
		void parse() throws Exception {
			if (document==null) {
				document = helper.searcher.doc(scoreDoc.doc);
			}
			if (bookName==null) {
				bookName = document.get("bookName");
			}
			if (preview==null) {
				entry = document.get("entry");
				text = document.get("content");
				TokenStream tokenStream=helper.analyzer.tokenStream("desc", new StringReader(text));
				String str = helper.highlighter.getBestFragment(tokenStream, text);
				str = str.replaceAll("`[0-9azAZ]{1,3}`", "").trim();
				if (str.startsWith(entry)) {
					str = str.substring(entry.length()).trim();
				}
				String dt = "<span class='dt'>"+bookName+"</span>";
				preview = str;
			}
			//CMN.Log(preview);
		}
		
		public long getBookId() {
			if (bookId==-1) {
				try {
					parse();
				} catch (Exception e) {
					CMN.debug(e);
				}
				bookId = LexicalDBHelper.getInstance().getBookID(null, bookName); //null
			}
			return bookId;
		}
		
		public String getEntry() {
			if (entry==null) {
				try {
					parse();
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
			return entry;
		}
	}
	

	public void invalidate(MainActivityUIBase a, BookPresenter targetBook, TopDocs docs, LuceneHelper helper) {
		if (targetBook==a.EmptyBook && TargetBook!=null) {
			targetBook = TargetBook;
		}
		TargetBook = targetBook;
		
		booksSet = new HashSet<>(a.loadManager.md_size);
		this.helper = helper;
		for (ScoreDoc hit : docs.scoreDocs) {
			results.add(new DocRecord(hit));
//			int docId = hit.doc;
//			Document doc = helper.searcher.doc(docId);
//			doc.get("");
		}
		
		// loadManager = a.loadManager.clone(); // todo copy onChange
		if (targetBook == null) { // 联合
		}
		else { // 单本
			booksSet.add(targetBook.getId());
		}
//		loadManager.dictPicker.underlined = booksSet;
//		loadManager.dictPicker.dataChanged();
	}
	
	public resultRecorderLucene(MainActivityUIBase a, MainActivityUIBase.LoadManager loadManager_){
		super(a);
		loadManager=loadManager_;
	}
	
	@Override
	public ArrayList<Long> getBooksAt(ArrayList<Long> books, int pos) {
//		if(books==null) books=new ArrayList<>();
//		else {books.clear();}
//		if(results.size()<=0 || pos<0 || pos>results.size()-1)
//			return books;
//		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
//		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
//			return books;
//		books.add(firstLookUpTable[Rgn+1]);
		return books;
	}
	
	@Override
	public long getOneDictAt(int pos) {
		if(pos<0 || pos >= results.size())
			return 0;
		DocRecord record = results.get(pos);
		return record.getBookId();
	}

	@Override
	public CharSequence getResAt(MainActivityUIBase a, long pos) {
		if ( pos < 0 || pos >= results.size()) {
			if (pos == -1) {
				return "←";
			}
			if (pos == results.size()) {
				return "→";
			}
			return "!!! Error: code 1";
		}
		
		DocRecord record = results.get((int) pos);
		bookId = record.getBookId();
		return record.getEntry();
	};
	
	public CharSequence getPreviewAt(BookPresenter book, MainActivityUIBase a, int pos, MainActivityUIBase.ViewHolder vh) {
		if (PDICMainAppOptions.listPreviewSet01Same()?PDICMainAppOptions.listPreviewEnabled():PDICMainAppOptions.listPreviewEnabled1()) {
			try {
				DocRecord record = results.get((int) pos);
				SpannableStringBuilder ssb = new SpannableStringBuilder();
				ssb.append(book.getInListName());
				ssb.setSpan(new ColoredTextSpan1(0xFFb0b0b0), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
	
	@Override
	public void renderContentAt(long pos, MainActivityUIBase a, BasicAdapter ADA, WebViewListHandler weblistHandler){//ViewGroup X
		getResAt(a, pos);
		if(results.size()<=0 || pos<0 || pos>=results.size())
			return;
		DocRecord record = results.get((int) pos);
		BookPresenter presenter = a.getBookById(record.getBookId());
		if(presenter==a.EmptyBook){
			CMN.Log("!!! Error: lazy load error failed.", record.getBookId());
			return;
		}
		// nimp
		//mShouldSaveHistory = !(mdtmp instanceof bookPresenter_txt);
		mShouldSaveHistory = true;
		bookId = presenter.getId();
		int entryPos = presenter.bookImpl.lookUp(record.getEntry());
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
			presenter.renderContentAt(desiredScale, BookPresenter.RENDERFLAG_NEW ,1 , mWebView , entryPos);
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
	public int size(){
		return results.size(); //todo
	};
	
	@Override
	public void shutUp() {
		results.clear();
	}
}

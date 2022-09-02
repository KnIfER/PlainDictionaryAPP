package com.knziha.plod.dictionarymodels;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.knziha.plod.dictionary.SearchResultBean;
import com.knziha.plod.dictionarymanager.files.BooleanSingleton;
import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 保存全文搜索的结果。todo 暂存结果至内存 */
public class resultRecorderScattered extends resultRecorderDiscrete {
	private final BooleanSingleton TintResult;
	private final com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer layer;
	/** not a copy of the main loadManager */
	private MainActivityUIBase.LoadManager loadManager;
	/** [resCount, bookId, ...] array */
	private long[] firstLookUpTable = ArrayUtils.EMPTY_LONG_ARRAY;
	/** bookId-acc map */
	private HashMap<Long, Integer> bookResAccMap = new HashMap<>();
	private int md_size=0;
	private long size=0;
	private boolean mShouldSaveHistory;
	BookPresenter TargetBook;

	public void invalidate(MainActivityUIBase a, BookPresenter targetBook) {
		ensureTableSz();
		if (targetBook==a.EmptyBook && TargetBook!=null) {
			targetBook = TargetBook;
		}
		TargetBook = targetBook;
		if(md_size==0)
			return;
		
		// loadManager = a.loadManager.clone(); // todo copy onChange
		booksSet = new HashSet<>(a.loadManager.md_size);
		bookResAccMap.clear();
		if (targetBook == null) { // 联合
			long resCount=0;
			long bookId=0;
			//todo respect TargetBook
			for(int i=0;i<md_size;i++){//遍历所有词典
				BookPresenter presenter = loadManager.md_getAt(i);
				if (presenter!=null) {
					ArrayList<SearchResultBean>[] treeBuilder = layer.getTreeBuilt(presenter);
					bookId=presenter.getId();
					if (treeBuilder != null) {
						bookResAccMap.put(bookId, (int) resCount);
						for (int ti = 0; ti < treeBuilder.length; ti++) {//遍历搜索结果
							if (treeBuilder[ti] == null) {
								continue;
							}
							resCount += treeBuilder[ti].size();
						}
						booksSet.add(bookId);
					}
				}
				firstLookUpTable[i*2]=resCount;
				firstLookUpTable[i*2+1]=bookId;
			}
			size=resCount;
		} else { // 单本
			int resCount=0;
			booksSet.add(targetBook.getId());
			ArrayList<SearchResultBean>[] treeBuilt = layer.getTreeBuilt(targetBook);
			if (treeBuilt != null)
				for (int ti = 0; ti < treeBuilt.length; ti++) {//遍历搜索结果
					if (treeBuilt[ti] == null) {
						continue;
					}
					resCount += treeBuilt[ti].size();
				}
			
			//firstLookUpTable[idx]=resCount;
			boolean found = false;
			for(int i=0;i<firstLookUpTable.length/2;i++) {
				BookPresenter mdTmp = loadManager.md_getAt(i);
				if (mdTmp==targetBook) {
					firstLookUpTable[i*2+1] = mdTmp.getId();
					found = true;
				}
				firstLookUpTable[i*2] = found?resCount:0;
			}
			bookResAccMap.put(targetBook.getId(), 0);
			size=resCount;
		}
		loadManager.dictPicker.underlined = booksSet;
		loadManager.dictPicker.dataChanged();
	}
	
	private void ensureTableSz() {
		md_size = loadManager.md_size;
		if(firstLookUpTable.length<md_size*2)
			firstLookUpTable = new long[md_size*2];
	}
	
	public resultRecorderScattered(MainActivityUIBase a, MainActivityUIBase.LoadManager loadManager_, BooleanSingleton _TintResult, com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer _layer){
		super(a);
		TintResult =_TintResult;
		loadManager=loadManager_;
		layer=_layer;
	}
	
	@Override
	public ArrayList<Long> getBooksAt(ArrayList<Long> books, int pos) {
		if(books==null) books=new ArrayList<>();
		else {books.clear();}
		if(size<=0 || pos<0 || pos>size-1)
			return books;
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return books;
		books.add(firstLookUpTable[Rgn+1]);
		return books;
	}
	
	public int findFirstBookPos(long id) {
		Integer ret = bookResAccMap.get(id);
		return ret==null? (int) size :ret;
	}
	
	@Override
	public long getOneDictAt(int pos) {
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return 0;
		return firstLookUpTable[Rgn+1];
	}
	
	@Override
	public CharSequence getPreviewAt(BookPresenter book, MainActivityUIBase a, int pos, MainActivityUIBase.ViewHolder vh) {
		if (PDICMainAppOptions.listPreviewEnabled()) {
			try {
				int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
				if(Rgn<0 || Rgn>firstLookUpTable.length-2)
					return null;
				
				BookPresenter presenter = a.getBookById(firstLookUpTable[Rgn+1]);
				if(presenter==a.EmptyBook) return "!!! Error: lazy load error failed.";
				bookId=presenter.getId();
				if(Rgn!=0)
					pos-=firstLookUpTable[Rgn-2];
				int idxCount = 0;
				BookPresenter mdTmp = getBookByTable(a, firstLookUpTable, Rgn);
				ArrayList<SearchResultBean>[] treeBuilt = layer.getTreeBuilt(mdTmp);
				for(int ti=0;ti<treeBuilt.length;ti++){
					if(treeBuilt[ti]==null)
						continue;
					int max = treeBuilt[ti].size();
					if(max==0)
						continue;
					if(pos-idxCount<max) {
						// ???
//						String record = presenter.bookImpl.getRecordAt(, null, false);
//						if (record!=null) {
//							return record;
//						}
						int position = Math.toIntExact(treeBuilt[ti].get((int) (pos - idxCount)).position);
						return a.getPreviewFor(vh, book, position);
					}
					idxCount+=max;
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		return null;
	}
	
	@Override
	public CharSequence getResAt(MainActivityUIBase a, long pos) {
		if ( pos < 0 || pos >= size) {
			if (pos == -1) {
				return "←";
			}
			if (pos == size) {
				return "→";
			}
			return "!!! Error: code 1";
		}
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return "!!! Error: code 2 Rgn="+Rgn/2+" size="+md_size;
		BookPresenter presenter = a.getBookById(firstLookUpTable[Rgn+1]);
		if(presenter==a.EmptyBook) return "!!! Error: lazy load error failed.";
		bookId=presenter.getId();
		if(Rgn!=0)
			pos-=firstLookUpTable[Rgn-2];
		int idxCount = 0;
		BookPresenter mdTmp = getBookByTable(a, firstLookUpTable, Rgn);
		ArrayList<SearchResultBean>[] treeBuilt = layer.getTreeBuilt(mdTmp);
		for(int ti=0;ti<treeBuilt.length;ti++){
			if(treeBuilt[ti]==null)
				continue;
			int max = treeBuilt[ti].size();
			if(max==0)
				continue;
			if(pos-idxCount<max) {
				// ???
				String text = presenter.bookImpl.getEntryAt(treeBuilt[ti].get((int)(pos-idxCount)).position, mflag);
				if(!TintResult.first) return text;
				SpannableStringBuilder result = new SpannableStringBuilder(text);
				Pattern reg=layer.getBakedPattern();
				if(reg==null) return text;
				Matcher m = reg.matcher(text);
				while(m.find()) {
					result.setSpan(new ForegroundColorSpan(Color.RED), m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				}
				return result;
			}
			idxCount+=max;
		}
		return "!!! Error: code 3 ";
	};

	public String getCurrentKeyText(MainActivityUIBase a, int pos) {
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return null;
		return a.getBookById(firstLookUpTable[Rgn+1]).currentDisplaying;
	}
	
	private BookPresenter getBookByTable(MainActivityUIBase a, long[] firstLookUpTable, int Rgn) {
		BookPresenter mdTmp = loadManager.md_getAt(Rgn/2);
		long bid = firstLookUpTable[Rgn+1];
		if (mdTmp!=null && mdTmp.getId()==bid) {
			return mdTmp;
		}
		return a.getBookById(bid);
	}
	
	@Override
	public void renderContentAt(long pos, MainActivityUIBase a, BasicAdapter ADA, WebViewListHandler weblistHandler){//ViewGroup X
		getResAt(a, pos);
		if(size<=0 || pos<0 || pos>size-1)
			return;
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md_size);
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return;
		BookPresenter presenter = getBookByTable(a, firstLookUpTable, Rgn);
		if(presenter==a.EmptyBook){
			CMN.Log("!!! Error: lazy load error failed.");
			return;
		}
		// nimp
		//mShouldSaveHistory = !(mdtmp instanceof bookPresenter_txt);
		mShouldSaveHistory = true;
		if(Rgn!=0)
			pos-=firstLookUpTable[Rgn-2];
		int idxCount = 0;
		ArrayList<SearchResultBean>[] treeBuilt = layer.getTreeBuilt(presenter);
		for(int ti=0;ti<treeBuilt.length;ti++) {
			if(treeBuilt[ti]==null)
				continue;
			int max = treeBuilt[ti].size();
			if(max==0)
				continue;
			if(pos-idxCount<max) {
				bookId=presenter.getId();
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
				presenter.renderContentAt(desiredScale, BookPresenter.RENDERFLAG_NEW ,1 , mWebView , treeBuilt[ti].get((int) (pos-idxCount)).position);
//				presenter.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
//				mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				a.contentUIData.PageSlider.setWebview(mWebView, null);
				return;
			}
			idxCount+=max;
		}
	}

	@Override
	public boolean shouldSaveHistory() {
		return mShouldSaveHistory;
	}

	@Override
	public int size(){
		return (int) size; //todo
	};
	
	@Override
	public void shutUp() {
		size=0;
	}

	/** 返回索引 */
    public static int  binary_find_closest(long[] array, long val,int iLen){
		iLen*=2;
    	int middle = 0;
    	if(iLen==-1||iLen>array.length)
    		iLen = array.length;
    	int low=0,high=iLen-2;
    	if(array==null || iLen<1){
    		return -1;
    	}
    	if(iLen==1){
    		return 0;
    	}
    	if(val-array[0]<=0){
			return 0;
    	}else if(val-array[iLen-2]>0){
    		return iLen-1;
    	}
    	int counter=0;
    	long cprRes1,cprRes0;
    	while(low<high){
    		//CMN.show(low+"~"+high);
    		counter+=1;
    		//System.out.println(low+":"+high);
    		middle = (low+high)/2;
    		if(middle%2!=0) middle-=1;
    		cprRes1=array[middle+2]-val;
        	cprRes0=array[middle  ]-val;
        	if(cprRes0>=0){
        		high=middle;
        	} else if(cprRes1<=0) {
        		//System.out.println("cprRes1<=0 && cprRes0<0");
        		//System.out.println(houXuan1);
        		//System.out.println(houXuan0);
        		low=middle+2;
        	} else {
        		//System.out.println("asd");
        		//high=middle;
        		low=middle+2;//here
        	}
    	}
		return low;
    }

}

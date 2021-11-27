package com.knziha.plod.dictionarymodels;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;

import com.knziha.plod.dictionary.SearchResultBean;
import com.knziha.plod.dictionarymanager.files.BooleanSingleton;
import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.widgets.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class resultRecorderScattered extends resultRecorderDiscrete {
	private final BooleanSingleton TintResult;
	private final com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer layer;
	private List<BookPresenter> md;
	private long[] firstLookUpTable;
	private long size=0;
	private boolean mShouldSaveHistory;

	@Override
	public void invalidate() {
		if(md.size()==0)
			return;
		if(firstLookUpTable.length<md.size()*2)
			firstLookUpTable = new long[md.size()*2];
		
		long resCount=0;
		long bookId=0;
		for(int i=0;i<md.size();i++){//遍历所有词典
			BookPresenter presenter = md.get(i);
			if (presenter!=null) {
				ArrayList<SearchResultBean>[] treeBuilder = layer.getTreeBuilt(i);
				bookId=presenter.getId();
				if (treeBuilder != null)
					for (int ti = 0; ti < treeBuilder.length; ti++) {//遍历搜索结果
						if (treeBuilder[ti] == null) {
							continue;
						}
						resCount += treeBuilder[ti].size();
					}
			}
			firstLookUpTable[i*2]=resCount;
			firstLookUpTable[i*2+1]=bookId;
		}
		size=resCount;
	}
	
	@Override
	public void invalidate(int idx) {
		if(md.size()==0)
			return;
		if(firstLookUpTable.length<md.size()*2)
			firstLookUpTable = new long[md.size()*2];

		int resCount=0;
		ArrayList<SearchResultBean>[] treeBuilt = layer.getTreeBuilt(idx);
		if (treeBuilt != null)
			for (int ti = 0; ti < treeBuilt.length; ti++) {//遍历搜索结果
				if (treeBuilt[ti] == null) {
					continue;
				}
				resCount += treeBuilt[ti].size();
			}
		
		//firstLookUpTable[idx]=resCount;
		for(int i=0;i<firstLookUpTable.length/2;i++) {
			if(i<idx)
				firstLookUpTable[i*2] = 0;
			else {
				if (i==idx) {
					firstLookUpTable[i*2+1] = md.get(i).getId();
				}
				firstLookUpTable[i*2] = resCount;
			}
		}
		size=resCount;
	}
	
	public resultRecorderScattered(MainActivityUIBase a, List<BookPresenter> md_, BooleanSingleton _TintResult, com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer _layer){
		super(a);
		TintResult =_TintResult;
		md=md_;
		firstLookUpTable = new long[md_.size()];
		layer=_layer;
	}
	
	@Override
	public ArrayList<Long> getBooksAt(ArrayList<Long> books, int pos) {
		if(books==null) books=new ArrayList<>();
		else {books.clear();}
		if(size<=0 || pos<0 || pos>size-1)
			return books;
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return books;
		books.add(firstLookUpTable[Rgn+1]);
		return books;
	}

	@Override
	public long getOneDictAt(int pos) {
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return 0;
		return firstLookUpTable[Rgn+1];
	}

	@Override
	public CharSequence getResAt(MainActivityUIBase a, long pos) {
		if(size<=0 || pos<0 || pos>size-1)
			return "!!! Error: code 1";
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return "!!! Error: code 2 Rgn="+Rgn/2+" size="+md.size();
		BookPresenter presenter = a.getBookById(firstLookUpTable[Rgn+1]);
		if(presenter==a.EmptyBook) return "!!! Error: lazy load error failed.";
		bookId=presenter.getId();
		if(Rgn!=0)
			pos-=firstLookUpTable[Rgn-2];
		int idxCount = 0;
		ArrayList<SearchResultBean>[] _combining_search_tree = layer.getTreeBuilt(Rgn/2);
		for(int ti=0;ti<_combining_search_tree.length;ti++){
			if(_combining_search_tree[ti]==null)
				continue;
			int max = _combining_search_tree[ti].size();
			if(max==0)
				continue;
			if(pos-idxCount<max) {
				// ???
				String text = presenter.bookImpl.getEntryAt(_combining_search_tree[ti].get((int)(pos-idxCount)).position, mflag);
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

	public String getCurrentKeyText(PDICMainActivity a, int pos) {
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return null;
		return a.getBookById(firstLookUpTable[Rgn+1]).currentDisplaying;
	}

	@Override
	public void renderContentAt(long pos, MainActivityUIBase a, BasicAdapter ADA){//ViewGroup X
		getResAt(a, pos);
		if(size<=0 || pos<0 || pos>size-1)
			return;
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>firstLookUpTable.length-2)
			return;
		BookPresenter presenter = a.getBookById(firstLookUpTable[Rgn+1]);
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
		ArrayList<SearchResultBean>[] _combining_search_tree = layer.getTreeBuilt(Rgn/2);
		for(int ti=0;ti<_combining_search_tree.length;ti++) {
			if(_combining_search_tree[ti]==null)
				continue;
			int max = _combining_search_tree[ti].size();
			if(max==0)
				continue;
			if(pos-idxCount<max) {
				bookId=presenter.getId();
				presenter.initViewsHolder(a);
				float desiredScale = a.prepareSingleWebviewForAda(presenter, null, pos, ADA);
				
				Utils.addViewToParentUnique(presenter.rl, a.webSingleholder);
				
				//todododo jcabjsahcbshjaccheck
				//todododo jcabjsahcbshjaccheck
				//todododo jcabjsahcbshjaccheck
				presenter.renderContentAt(desiredScale, BookPresenter.RENDERFLAG_NEW ,1 ,null , _combining_search_tree[ti].get((int) (pos-idxCount)).position);
				presenter.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				presenter.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				return;
			}
			idxCount+=max;
		}
	};

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

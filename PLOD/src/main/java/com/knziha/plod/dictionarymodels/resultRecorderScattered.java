package com.knziha.plod.dictionarymodels;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.knziha.plod.PlainDict.BasicAdapter;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.PDICMainActivity;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionarymanager.files.BooleanSingleton;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;

public class resultRecorderScattered extends resultRecorderDiscrete {
	private final BooleanSingleton TintResult;
	private final com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer layer;
	private List<mdict> md;
	private int[] firstLookUpTable;
	private int size=0;
	
	@Override
	public void invalidate() {
		if(md.size()==0)
			return;
		if(firstLookUpTable.length<md.size())
			firstLookUpTable = new int[md.size()];
		
		int resCount=0;
		for(int i=0;i<md.size();i++){//遍历所有词典
			mdict mdtmp = md.get(i);
			if(mdtmp!=null) {
				ArrayList<Integer>[] _combining_search_tree = layer.getInternalTree(mdtmp);
				if (_combining_search_tree != null)
				for (int ti = 0; ti < _combining_search_tree.length; ti++) {//遍历搜索结果
					if (_combining_search_tree[ti] == null) {
						continue;
					}
					resCount += _combining_search_tree[ti].size();
				}
			}
			firstLookUpTable[i]=resCount;
		}
		
		size=resCount;
	}
	
	@Override
	public void invalidate(int idx) {
		if(md.size()==0)
			return;
		if(firstLookUpTable.length<md.size())
			firstLookUpTable = new int[md.size()];

		int resCount=0;
		mdict mdtmp = md.get(idx);
		if(mdtmp!=null) {
			ArrayList<Integer>[] _combining_search_tree = layer.getInternalTree(mdtmp);
			if (_combining_search_tree != null)
			for (int ti = 0; ti < _combining_search_tree.length; ti++) {//遍历搜索结果
				if (_combining_search_tree[ti] == null) {
					continue;
				}
				resCount += _combining_search_tree[ti].size();
			}
		}
		
		//firstLookUpTable[idx]=resCount;
		for(int i=0;i<firstLookUpTable.length;i++) {
			if(i<idx)
				firstLookUpTable[i] = 0;
			else
				firstLookUpTable[i] = resCount;
		}
		size=resCount;
	}
	
	public resultRecorderScattered(MainActivityUIBase a, List<mdict> md_, BooleanSingleton _TintResult, com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer _layer){
		super(a);
		TintResult =_TintResult;
		md=md_;
		firstLookUpTable = new int[md_.size()];
		layer=_layer;
	}
	
	@Override
	public ArrayList<Integer> getDictsAt(int pos) {
		if(size<=0 || pos<0 || pos>size-1)
			return null;
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>md.size()-1)
			return null;
		ArrayList<Integer> ret = new ArrayList<>();
		ret.add(Rgn);
		return ret;
	}

	@Override
	public int getOneDictAt(int pos) {
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>md.size()-1)
			return 0;
		return Rgn;
	}

	@Override
	public void syncToPeruseArr(ArrayList<Integer> pvdata, int pos) {
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		pvdata.clear();
		if(Rgn<0 || Rgn>md.size()-1)
			return;
		pvdata.add(Rgn);
	}

	@Override
	public CharSequence getResAt(int pos) {
		if(size<=0 || pos<0 || pos>size-1)
			return "!!! Error: code 1";
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>md.size()-1)
			return "!!! Error: code 2 Rgn="+Rgn+" size="+md.size();
		mdict mdtmp = md.get(Rgn);
		if(mdtmp==null) return "!!! Error: lazy load error failed.";
		dictIdx=Rgn;
		if(Rgn!=0)
			pos-=firstLookUpTable[Rgn-1];
		int idxCount = 0;
		ArrayList<Integer>[] _combining_search_tree = layer.getInternalTree(mdtmp);
		for(int ti=0;ti<_combining_search_tree.length;ti++){
			if(_combining_search_tree[ti]==null)
				continue;
			int max = _combining_search_tree[ti].size();
			if(max==0)
				continue;
			if(pos-idxCount<max) {
				String text = mdtmp.getEntryAt(_combining_search_tree[ti].get(pos-idxCount),mflag);
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

	public String getCurrentKeyText(int pos) {
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>md.size()-1 || md.get(Rgn)==null)
			return null;
		return md.get(Rgn).currentDisplaying;
	}

	@Override
	public void renderContentAt(int pos, MainActivityUIBase a, BasicAdapter ADA){//ViewGroup X
		getResAt(pos);
		if(size<=0 || pos<0 || pos>size-1)
			return;
		int Rgn = binary_find_closest(firstLookUpTable,pos+1,md.size());
		if(Rgn<0 || Rgn>md.size()-1)
			return;
		mdict mdtmp = md.get(Rgn);
		if(mdtmp==null){
			CMN.Log("!!! Error: lazy load error failed.");
			return;
		}
		if(Rgn!=0)
			pos-=firstLookUpTable[Rgn-1];
		int idxCount = 0;
		ArrayList<Integer>[] _combining_search_tree = layer.getInternalTree(mdtmp);
		for(int ti=0;ti<_combining_search_tree.length;ti++){
			if(_combining_search_tree[ti]==null)
				continue;
			int max = _combining_search_tree[ti].size();
			if(max==0)
				continue;
			if(pos-idxCount<max) {
				dictIdx=Rgn;
				mdtmp.initViewsHolder(a);
				PDICMainActivity aa= (PDICMainActivity) a;
				float desiredScale = -1;
				if(a.opt.getRemPos()) {
					ScrollerRecord pagerec;
					if (System.currentTimeMillis() - a.lastClickTime > 400) //save our postion
						if ((mdtmp.mWebView == null || !mdtmp.mWebView.isloading) && ADA.lastClickedPosBeforePageTurn >= 0 && a.webSingleholder.getChildCount() != 0) {
							//ADA.avoyager.get(ADA.avoyagerIdx).set(mdtmp.mWebView.getScrollX(), mdtmp.mWebView.getScrollY(), mdtmp.webScale);

							pagerec = ADA.avoyager.get(ADA.lastClickedPosBeforePageTurn);
							if (pagerec == null) {
								pagerec = new ScrollerRecord();
								ADA.avoyager.put(ADA.lastClickedPosBeforePageTurn, pagerec);
							}
							pagerec.set(mdtmp.mWebView.getScrollX(), mdtmp.mWebView.getScrollY(), mdtmp.webScale);
						}
					a.lastClickTime = System.currentTimeMillis();

					pagerec = ADA.avoyager.get(pos);

					if (pagerec != null) {
						mdtmp.mWebView.expectedPos = pagerec.y;
						mdtmp.mWebView.expectedPosX = pagerec.x;
						desiredScale = pagerec.scale;
					} else {
						mdtmp.mWebView.expectedPos = 0;
						mdtmp.mWebView.expectedPosX = 0;
					}
					if(aa.rem_res!=R.string.rem_position_yes){
						aa.iItem_aPageRemember.setTitle(aa.rem_res=R.string.rem_position_yes);
					}
				}else{
					mdtmp.mWebView.expectedPos = 0;
					mdtmp.mWebView.expectedPosX = 0;
					if(aa.rem_res!=R.string.rem_position){
						aa.iItem_aPageRemember.setTitle(aa.rem_res=R.string.rem_position);
					}
				}
				mdtmp.mWebView.fromCombined=0;

				ViewGroup someView = mdtmp.rl;
				if(someView.getParent()!=a.webSingleholder) {
					if(someView.getParent()!=null) ((ViewGroup)someView.getParent()).removeView(someView);
					a.webSingleholder.addView(someView);
				}
				if(a.webSingleholder.getChildCount()>1) {
					for(int i=a.webSingleholder.getChildCount()-1;i>=0;i--)
					if(a.webSingleholder.getChildAt(i)!=someView) a.webSingleholder.removeViewAt(i);
				}

				if(a.opt.getAutoReadEntry() && !PDICMainAppOptions.getTmpIsAudior(mdtmp.tmpIsFlag)){
					mdtmp.mWebView.setTag(R.drawable.voice_ic, false);
				}
				mdtmp.renderContentAt(desiredScale,Rgn,0,null, (int)(long) _combining_search_tree[ti].get(pos-idxCount));
				mdtmp.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				mdtmp.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				return;
			}
			idxCount+=max;
		}
	};
	
	@Override
	public int size(){
		return size;
	};
	
	@Override
	public void shutUp() {
		size=0;
	}

    public static int  binary_find_closest(int[] array,int val,int iLen){
    	int middle = 0;
    	if(iLen==-1||iLen>array.length)
    		iLen = array.length;
    	int low=0,high=iLen-1;
    	if(array==null || iLen<1){
    		return -1;
    	}
    	if(iLen==1){
    		return 0;
    	}
    	if(val-array[0]<=0){
			return 0;
    	}else if(val-array[iLen-1]>0){
    		return iLen-1;
    	}
    	int counter=0;
    	long cprRes1,cprRes0;
    	while(low<high){
    		//CMN.show(low+"~"+high);
    		counter+=1;
    		//System.out.println(low+":"+high);
    		middle = (low+high)/2;
    		cprRes1=array[middle+1]-val;
        	cprRes0=array[middle  ]-val;
        	if(cprRes0>=0){
        		high=middle;
        	}else if(cprRes1<=0){
        		//System.out.println("cprRes1<=0 && cprRes0<0");
        		//System.out.println(houXuan1);
        		//System.out.println(houXuan0);
        		low=middle+1;
        	}else{
        		//System.out.println("asd");
        		//high=middle;
        		low=middle+1;//here
        	}
    	}
		return low;
    }

}

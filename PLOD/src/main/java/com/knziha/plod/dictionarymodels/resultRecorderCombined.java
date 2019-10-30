package com.knziha.plod.dictionarymodels;

import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.knziha.plod.PlainDict.BasicAdapter;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.rbtree.additiveMyCpr1;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/** Recorder rendering search results as : LinearLayout {WebView, WebView, ... }  */
public class resultRecorderCombined extends resultRecorderDiscrete {
	private List<additiveMyCpr1> data;
	public List<additiveMyCpr1> list(){return data;}
	private List<mdict> md;

	@Override
	public void invalidate() {}
	
	public resultRecorderCombined(MainActivityUIBase a, List<additiveMyCpr1> data_, List<mdict> md_){
		super(a);
		data=data_;
		md=md_;
	}
	
	@Override
	public ArrayList<Integer> getDictsAt(int pos) {
		ArrayList<Integer> data = getRecordAt(pos);
		ArrayList<Integer> data2=new ArrayList<>();
		int last=-1;
		for(int i=0;i<data.size();i+=2) {
			if(last!=data.get(i))
				data2.add(last=data.get(i));
		}
		return data2;
	}
	
	@Override
	public CharSequence getResAt(int pos) {
		if(data==null || pos<0 || pos>data.size()-1)
			return "!!! Error: code 1";
		List<Integer> l = ((List<Integer>) data.get(pos).value);
		dictIdx = l.get(0);
		count = String.format("%02d", l.size()/2);
		return data.get(pos).key;
	};

	OnLayoutChangeListener OLCL;

	public boolean scrolled=false;
	int LHGEIGHT;
	
	@Override
	public void renderContentAt(int pos, final MainActivityUIBase a, BasicAdapter ADA){//,ViewGroup webholder
		final ScrollView sv = a.WHP;
		if(expectedPos==0)
			sv.smoothScrollTo(0, 0);
		else
			sv.scrollTo(0, expectedPos);
		
		final List<Integer> vals = (List<Integer>) data.get(pos).value;
		//String xxx=""; for(int i=0;i<vals.size();i+=2) xxx+=vals.get(i)+"-"+vals.get(i+1)+" ";

		int lastVal=-1, valueCount=0;
		int[] exempter = new int[vals.size()/2];
		for(int i=0;i<vals.size();i+=2){
			int nowVal = vals.get(i);
			if(lastVal!=nowVal) {
				exempter[valueCount++]=nowVal;
			}
			lastVal=nowVal;
		}
		final int vc = valueCount;
		
		valueCount=0;//adaptively remove views
		for(int i=0;i<md.size();i++) {
			mdict mdtmp = md.get(i);
			ViewGroup sV = mdtmp.rl;
			if(sV!=null) {
				ViewGroup sVG = (ViewGroup)sV.getParent();
				if(sVG!=null) {
					if(valueCount<vc && i==exempter[valueCount]) {
						if(sVG!=a.webholder) sVG.removeView(sV);
						valueCount++;
					}else
						sVG.removeView(sV);
				}
			}
		}

		if(a.main_progress_bar!=null)
			a.main_progress_bar.setVisibility(expectedPos==0?View.GONE:View.VISIBLE);

		//if(false)
		a.WHP.touchFlag.first=false;
		if(OLCL==null) {
			OLCL = new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
						int oldBottom) {
					if(a.WHP.touchFlag.first) {
						a.main_progress_bar.setVisibility(View.GONE);
						a.webholder.removeOnLayoutChangeListener(this);
						return;
					}
					//if(expectedPos==0) return;
					int HGEIGHT = bottom-top;
					if(HGEIGHT<LHGEIGHT)
						scrolled=false;
					LHGEIGHT=HGEIGHT;
					if(!scrolled) {
						if(HGEIGHT>=expectedPos+sv.getMeasuredHeight()) {
							sv.scrollTo(0, expectedPos);//smooth
							//a.showT(sv.getMeasuredHeight()+  "scrolled"+expectedPos);
							if(sv.getScrollY()==expectedPos) {
								a.main_progress_bar.setVisibility(View.GONE);
								scrolled=true;
							}
						}
					}
				}
			};
		}
		
		LHGEIGHT=0;
		a.webholder.addOnLayoutChangeListener(OLCL);
		scrolled=false;

		ArrayList<Integer> valsTmp = new ArrayList<>();
		valueCount=0;
		for(int i=0;i<vals.size();i+=2){
			valsTmp.clear();
			int toFind=vals.get(i);
			mdict mdtmp = md.get(toFind);
			while(i<vals.size() && toFind==vals.get(i)) {
				valsTmp.add(vals.get(i+1));
				i+=2;
			}
			i-=2;
			
			int[] d = new int[valsTmp.size()];
			for(int i1 = 0;i1<valsTmp.size();i1++){
			    d[i1] = valsTmp.get(i1);
			}
			
			//if(Build.VERSION.SDK_INT>=22)...// because kitkat's webview is not that adaptive for content height
			mdtmp.initViewsHolder(a);
			mdtmp.rl.setTag(toFind);
			if(mdtmp.rl.getParent()==null)
				a.webholder.addView(mdtmp.rl,valueCount>a.webholder.getChildCount()?a.webholder.getChildCount():valueCount);
			//else
			//	a.showT("yes: "+mdtmp._Dictionary_fName);
			mdtmp.rl.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
			//mdtmp.vll=vll;

			/*//for debug usage
			if(mdtmp.mWebView.getLayerType()!=View.LAYER_TYPE_NONE)
				mdtmp.mWebView.setLayerType(View.LAYER_TYPE_NONE, null);
			*/
			//mdtmp.mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			
			mdtmp.renderContentAt(-1,toFind,null,d);
			
			
			mdtmp.mWebView.getSettings().setSupportZoom(false);
			if(mdtmp.mWebView.getVisibility()!=View.VISIBLE)
				mdtmp.mWebView.setVisibility(View.VISIBLE);
			//mdtmp.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
			valueCount++;
		}

		if(a.opt.getHideScroll1())
			a.mBar.setVisibility(View.GONE);
		else {
			a.initWebHolderScrollChanged();
		}
	};

	@Override
	public ArrayList<Integer> getRecordAt(int pos) {
		return (ArrayList<Integer>) data.get(pos).value;
	}

	@Override
	public int size(){
		if(data==null)
			return 0;
		else
		return data.size();
	};
	
	@Override
	public void shutUp() {
		data.clear();
	}
}

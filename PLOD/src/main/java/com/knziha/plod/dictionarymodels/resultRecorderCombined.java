package com.knziha.plod.dictionarymodels;

import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.knziha.plod.PlainDict.BasicAdapter;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.R;
import com.knziha.rbtree.additiveMyCpr1;

import java.util.ArrayList;
import java.util.List;

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
	public int getOneDictAt(int pos) {
		return getRecordAt(pos).get(0);
	}

	@Override
	public boolean checkAllWebs(ArrayList<mdict> md) {
		ArrayList<Integer> data = getRecordAt(0);
		allWebs=true;
		for(int i=0;i<data.size();i+=2) {
			if(!(md.get(data.get(i)) instanceof mdict_web)){
				allWebs=false;
				break;
			}
		}
		return allWebs;
	}

	@Override
	public void syncToPeruseArr(ArrayList<Integer> pvdata, int pos) {
		ArrayList<Integer> data = getRecordAt(pos);
		int last=-1;
		pvdata.clear();
		pvdata.ensureCapacity(data.size()/2);
		for(int i=0;i<data.size();i+=2) {
			if(last!=data.get(i))
				pvdata.add(data.get(i));
		}
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

	public OnLayoutChangeListener OLCL;

	public boolean scrolled=false, toHighLight;
	public int LHGEIGHT;
	
	@Override
	public void renderContentAt(int pos, final MainActivityUIBase a, BasicAdapter ADA){
		final ScrollView sv = a.WHP;
		toHighLight=a.hasCurrentPageKey();
		if(toHighLight || expectedPos==0)
			sv.scrollTo(0, 0);
		else{
			sv.scrollTo(0, expectedPos);
		}

		final additiveMyCpr1 result = data.get(pos);
		final List<Integer> vals = (List<Integer>) result.value;

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
			if(mdtmp!=null) {
				ViewGroup sV = mdtmp.rl;
				if (sV != null) {
					ViewGroup sVG = (ViewGroup) sV.getParent();
					if (sVG != null) {
						if (valueCount < vc && i == exempter[valueCount]) {
							if (sVG != a.webholder) sVG.removeView(sV);
							valueCount++;
						} else
							sVG.removeView(sV);
					}
				}
			}
		}

		//if(false)
		a.WHP.touchFlag.first=false;
		if(OLCL==null) {
			OLCL = new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
						int oldBottom) {
					//CMN.Log("onLayoutChange", expectedPos, sv.getScrollY());
					//CMN.Log("onLayoutChange", a.WHP.touchFlag.first, scrolled, bottom - top >= expectedPos + sv.getMeasuredHeight());
					//CMN.Log("onLayoutChange", bottom - top , expectedPos + sv.getMeasuredHeight());
					if (a.WHP.touchFlag.first) {
						a.main_progress_bar.setVisibility(View.GONE);
						v.removeOnLayoutChangeListener(this);
						return;
					}
					//if(expectedPos==0) return;
					int HGEIGHT = bottom - top;
					if (HGEIGHT < LHGEIGHT)
						scrolled = false;
					LHGEIGHT = HGEIGHT;
					if (!scrolled) {
						if (HGEIGHT >= expectedPos + sv.getMeasuredHeight()) {
							sv.scrollTo(0, expectedPos);//smooth
							//CMN.Log("onLayoutChange scrolled", expectedPos, sv.getMeasuredHeight());
							if (sv.getScrollY() == expectedPos) {
								a.main_progress_bar.setVisibility(View.GONE);
								scrolled = true;
							}
						}
					}
				}
			};
		}
		
		LHGEIGHT=0;
		a.webholder.removeOnLayoutChangeListener(OLCL);
		if(!toHighLight){
			a.webholder.addOnLayoutChangeListener(OLCL);
			if(a.main_progress_bar!=null)
				a.main_progress_bar.setVisibility(expectedPos==0?View.GONE:View.VISIBLE);
			scrolled=false;
		}

		ArrayList<Integer> valsTmp = new ArrayList<>();
		valueCount=0;
		boolean checkReadEntry = a.opt.getAutoReadEntry();
		for(int i=0;i<vals.size();i+=2){
			valsTmp.clear();
			int toFind=vals.get(i);
			mdict mdtmp = md.get(toFind);
			if(mdtmp==null) continue;
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
			if(checkReadEntry){
				mdtmp.mWebView.setTag(R.drawable.voice_ic, false);
				checkReadEntry=false;
			}
			mdtmp.rl.setTag(toFind);
			int frameAt=a.webholder.getChildCount();
			frameAt=valueCount>frameAt?frameAt:valueCount;
			if(mdtmp.rl.getParent()==null)
				a.webholder.addView(mdtmp.rl,frameAt);
			//else
			//	a.showT("yes: "+mdtmp.getPath());
			mdtmp.rl.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
			//mdtmp.vll=vll;

			/*//for debug usage
			if(mdtmp.mWebView.getLayerType()!=View.LAYER_TYPE_NONE)
				mdtmp.mWebView.setLayerType(View.LAYER_TYPE_NONE, null);
			*/
			//mdtmp.mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			mdtmp.mWebView.setTag(R.id.toolbar_action5, i==0&&toHighLight?false:null);
			mdtmp.mWebView.fromCombined=1;
			if(mdtmp instanceof mdict_web){
				mdict_web webx = (mdict_web)mdtmp;
				webx.searchKey = result.key;
			}

			mdtmp.renderContentAt(-1,toFind,frameAt,null, d);

			mdtmp.mWebView.fromCombined=1;
			if(false && mdtmp.mWebView.getVisibility()!=View.VISIBLE)
				mdtmp.mWebView.setVisibility(View.VISIBLE);
			//mdtmp.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
			valueCount++;
		}
		a.RecalibrateWebScrollbar(null);
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

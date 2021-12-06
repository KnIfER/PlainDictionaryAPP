package com.knziha.plod.dictionarymodels;

import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.Utils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.additiveMyCpr1;

import java.util.ArrayList;
import java.util.List;

/** Recorder rendering search results as : LinearLayout {WebView, WebView, ... }  */
public class resultRecorderCombined extends resultRecorderDiscrete {
	private List<additiveMyCpr1> data;
	int firstItemIdx;
	private View scrollTarget;

	public List<additiveMyCpr1> list(){return data;}
	private List<BookPresenter> md;
	
	public resultRecorderCombined(MainActivityUIBase a, List<additiveMyCpr1> data_, List<BookPresenter> md_){
		super(a);
		data=data_;
		md=md_;
	}
	
	public boolean FindFirstIdx(String key, AsyncTask task) {
		int cc=0;
		for (int i = 0; i < data.size(); i++) {
			if(cc++>350) { if(task.isCancelled()) return false; cc=0; }
			if(data.get(i).key.regionMatches(true, 0, key, 0, key.length())) {
				firstItemIdx = i;
				break;
			}
		}
		return true;
	}
	
	private int RemapPos(int pos) {
		if(firstItemIdx>0) {
			pos = (pos+firstItemIdx)%data.size();
		}
		return pos;
	}
	
	@Override
	public ArrayList<Long> getBooksAt(ArrayList<Long> books, int pos) {
		ArrayList<Long> data = getRecordAt(pos);
		if(books==null) books=new ArrayList<>(data.size()/2);
		else {books.clear();books.ensureCapacity(data.size()/2);}
		long last=-1;
		for(int i=0;i<data.size();i+=2) {
			if(last!=data.get(i))
				books.add(last=data.get(i));
		}
		return books;
	}

	@Override
	public long getOneDictAt(int pos) {
		return getRecordAt(pos).get(0);
	}

	@Override
	public boolean checkAllWebs(MainActivityUIBase a, ArrayList<BookPresenter> md) {
		ArrayList<Long> data = getRecordAt(0);
		allWebs=true;
		for(int i=0;i<data.size();i+=2) {
			long toFind=data.get(i);
			if (a.getBookById(toFind).getType()!=DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
				allWebs=false;
				break;
			}
		}
		return allWebs;
	}

	@Override
	public CharSequence getResAt(MainActivityUIBase a, long pos) {
		if(data==null || pos<0 || pos>data.size()-1)
			return "!!! Error: code 1";
		if(firstItemIdx>0) pos = RemapPos((int) pos);
		List<Long> l = ((List<Long>) data.get((int) pos).value); //todo
		bookId = l.get(0);
		count = String.format("%02d", l.size()/2);
		return data.get((int) pos).key;
	};

	public OnLayoutChangeListener OLCL;

	public boolean scrolled=false, toHighLight;
	public int LHGEIGHT;
	
	@Override
	public void renderContentAt(long pos, final MainActivityUIBase a, BasicAdapter ADA){
		scrollTarget=null;
		final ScrollView sv = a.WHP;
		toHighLight=a.hasCurrentPageKey();
		if(toHighLight || expectedPos==0)
			sv.scrollTo(0, 0);
		else{
			sv.scrollTo(0, expectedPos);
		}
		
		if(firstItemIdx>0) pos = RemapPos((int) pos);
		final additiveMyCpr1 result = data.get((int) pos);
		final List<Long> vals = (List<Long>) result.value;
		
		// todo remove adaptively .
		a.webholder.removeAllViews();
		
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
					if(scrollTarget!=null)
						expectedPos=scrollTarget.getTop();
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
		
		a.PageSlider.setIBC(null);
		
		ArrayList<Long> valsTmp = new ArrayList<>();
		int valueCount=0;
		boolean checkReadEntry = a.opt.getAutoReadEntry();
		boolean bNeedExpand=true;
		ViewGroup webholder = a.webholder;
		long toFind;
		for(int i=0;i<vals.size();i+=2){
			valsTmp.clear();
			toFind=vals.get(i);
			while(i<vals.size() && toFind==vals.get(i)) {
				valsTmp.add(vals.get(i+1));
				i+=2;
			}
			i-=2;
			
			BookPresenter presenter = a.getBookById(toFind);
			
			if(presenter==a.EmptyBook) continue;
			
			long[] p = new long[valsTmp.size()];
			for(int i1 = 0;i1<valsTmp.size();i1++){
			    p[i1] = valsTmp.get(i1);
			}
			
			//if(Build.VERSION.SDK_INT>=22)...// because kitkat's webview is not that adaptive for content height
			presenter.initViewsHolder(a);
			ViewGroup rl = presenter.rl;
			WebViewmy mWebView = presenter.mWebView;
			int frameAt=webholder.getChildCount();
			frameAt= Math.min(valueCount, frameAt);
			if(rl.getParent()!=webholder) {
				Utils.removeView(rl);
				webholder.addView(rl,frameAt);
			}
			//else
			//	a.showT("yes: "+mdtmp.getPath());
			//mdtmp.vll=vll;

			/*//for debug usage
			if(mdtmp.mWebView.getLayerType()!=View.LAYER_TYPE_NONE)
				mdtmp.mWebView.setLayerType(View.LAYER_TYPE_NONE, null);
			*/
			//mdtmp.mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			mWebView.setTag(R.id.toolbar_action5, i==0&&toHighLight?false:null);
			mWebView.fromCombined=1;
			if(presenter.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB)
			{
				presenter.SetSearchKey(result.key);
			}
			
			presenter.renderContentAt(-1, BookPresenter.RENDERFLAG_NEW, frameAt,null, p);
			if(!mWebView.awaiting){
				bNeedExpand=false;
				if(checkReadEntry){
					presenter.mWebView.bRequestedSoundPlayback=true;
					checkReadEntry=false;
				}
			}
			mWebView.fromCombined=1;
			valueCount++;
		}
		if(bNeedExpand && PDICMainAppOptions.getEnsureAtLeatOneExpandedPage()){
			View viewById = webholder.findViewById(R.id.toolbar_title);
			if (viewById != null) {
				viewById.performClick();
			}
		}
		a.RecalibrateWebScrollbar(null);
	}

	@Override
	public ArrayList<Long> getRecordAt(int pos) {
		if(firstItemIdx>0) pos = RemapPos(pos);
		return (ArrayList<Long>) data.get(pos).value;
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
		firstItemIdx = 0;
	}

	public void scrollTo(View _scrollTarget, MainActivityUIBase a) {
		a.WHP.touchFlag.first=false;
		scrollTarget=_scrollTarget;
		LHGEIGHT=a.WHP.getHeight();
		a.webholder.removeOnLayoutChangeListener(OLCL);
		a.webholder.addOnLayoutChangeListener(OLCL);
		scrolled=false;
	}
}

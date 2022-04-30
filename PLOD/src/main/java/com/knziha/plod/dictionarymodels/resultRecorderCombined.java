package com.knziha.plod.dictionarymodels;

import static com.knziha.plod.plaindict.WebViewListHandler.WEB_LIST_MULTI;
import static com.knziha.plod.plaindict.WebViewListHandler.WEB_VIEW_SINGLE;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.additiveMyCpr1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Recorder rendering search results as : LinearLayout {WebView, WebView, ... }  */
public class resultRecorderCombined extends resultRecorderDiscrete {
	private List<additiveMyCpr1> data;
	int firstItemIdx;
	public View scrollTarget;
	boolean schKeySaved;
	///** current displaying position */
	public int viewingPos;
	public additiveMyCpr1 jointResult;
	
	public List<additiveMyCpr1> list(){return data;}
	private List<BookPresenter> md;
	
	public resultRecorderCombined(MainActivityUIBase a, List<additiveMyCpr1> data_, List<BookPresenter> md_, String schKey){
		super(a);
		data=data_;
		md=md_;
		this.schKey = schKey;
	}
	
	public boolean FindFirstIdx(String key, AtomicBoolean task) {
		int cc=0;
		for (int i = 0; i < data.size(); i++) {
			if(cc++>350) { if(!task.get()) return false; cc=0; }
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
		if (data == null || pos < 0 || pos > data.size() - 1) {
			if (data!=null) {
				if (pos == -1) {
					return "←";
				}
				if (pos == data.size()) {
					return "→";
				}
			}
			return "!!! Error: code 1";
		}
		if(firstItemIdx>0) pos = RemapPos((int) pos);
		List<Long> l = ((List<Long>) data.get((int) pos).value); //todo
		bookId = l.get(0);
		int sz=l.size()/2;
		count = sz>1?String.format("%02d", sz):null;
		return data.get((int) pos).key;
	};

	public boolean scrolled=false, toHighLight;
	public int LHGEIGHT;
	
	@Override
	public void renderContentAt(long pos, final MainActivityUIBase a, BasicAdapter ADA, WebViewListHandler weblistHandler){
		CMN.Log("renderContentAt::", pos, weblistHandler.bMergeFrames, weblistHandler.bMergingFrames, weblistHandler.getChildCount());
		scrollTarget=null;
		final ScrollView sv = (ScrollView) weblistHandler.getScrollView();
		toHighLight=weblistHandler.hasPageKey();
//		if(toHighLight || expectedPos==0)
//			sv.scrollTo(0, 0);
//		else{
//			sv.scrollTo(0, expectedPos);
//		}
		additiveMyCpr1 jointResult;
		if(pos==-2) {
			if(weblistHandler.bMergingFrames!=0) {
				jointResult = (weblistHandler.dictView!=null?weblistHandler.dictView:weblistHandler.getMergedFrame()).jointResult; //dictView ??
			} else {
				jointResult = weblistHandler.jointResult;
			}
		} else {
			viewingPos = (int) pos;
			if(firstItemIdx>0) pos = RemapPos((int) pos);
			jointResult = data.get((int) pos);
		}
		
		if(jointResult==null) {
			a.showT("ERROR "+pos+" "+weblistHandler.isMergingFramesNum()+" "+weblistHandler.bMergeFrames);
			return;
		}
		List<Long> vals = (List<Long>) jointResult.value;
		//CMN.Log("frameCount::", jointResult.realmCount);
		int mergeFrames = weblistHandler.bMergeFrames;
		if(mergeFrames==-2) {
			mergeFrames = jointResult.realmCount >= a.opt.getMergeUrlForFrames() ? 1 : 0;
		}
		boolean bUseMergedUrl = mergeFrames==1;
		boolean bFoldingScreen = mergeFrames==2;
		//bUseMergedUrl = false;
		boolean bUseDictView = mergeFrames==0;
		if(bUseDictView && jointResult.realmCount==1) {
			if(PDICMainAppOptions.getUseSharedFrame()
				&& !(PDICMainAppOptions.getMergeExemptWebx() && a.getHasVidxByIdNoCreation(vals.get(0)))) {
				// 当只有一个页面，不是在线词典的话，使用公共视图显示。
				bUseDictView = false;
			}
		}
		
		this.jointResult = jointResult;
		if (!weblistHandler.bDataOnly) {
			if(!bUseMergedUrl && !bFoldingScreen /*算了，还是只为旧模式开此门*/  && jointResult.realmCount==1
					/*看注释。*/ && PDICMainAppOptions.getLv2JointOneAsSingle()) {
				// 当只有一个页面，使用旧模式的话，使用单一页面的滚动方式。
				BookPresenter book = a.getBookByIdNoCreation(vals.get(0));
				//if(true) bUseMergedUrl = false; // 只有一页，不通过合并的url加载了
				if(bUseDictView) book.initViewsHolder(a);
				weblistHandler.setViewMode(null, mergeFrames, bUseDictView?book.mWebView:weblistHandler.getMergedFrame());
			} else {
				weblistHandler.setViewMode(this, mergeFrames, null);
			}
			a.viewContent(weblistHandler);
			weblistHandler.initMergedFrame(mergeFrames, weblistHandler.bShowInPopup, bUseMergedUrl);
		}
		if (weblistHandler.multiRecord!=this) {
			weblistHandler.multiRecord = this;
		}
		
		
		if(!bUseMergedUrl) {
//			a.showT("未更新？"+CMN.Log(weblistHandler.jointResult==jointResult, weblistHandler.getChildCount()==weblistHandler.frames.size()
//			, weblistHandler.getChildCount(),weblistHandler.frames.size()));
		}
		if((bUseMergedUrl || !bUseDictView) && weblistHandler.getMergedFrame().jointResult==jointResult
			|| !bUseMergedUrl && weblistHandler.jointResult==jointResult && weblistHandler.getChildCount()==weblistHandler.frames.size()) {
			//weblistHandler.initMergedFrame(bUseMergedUrl, weblistHandler.bShowingInPopup, bUseMergedUrl);
			//a.showT("未更新！");
			//return;
		}
		// [1st pass] Invalidate array
		// [2nd pass] Rearrange array
		ArrayList<Long> valsTmp = new ArrayList<>();
		ArrayList<BookPresenter> frames = weblistHandler.frames;
		ArrayList<long[]> framesDisplaying = weblistHandler.framesDisplaying;
		weblistHandler.frames.clear();
		weblistHandler.framesDisplaying.clear();
		long toFind;
		for(int i=0;i<vals.size();i+=2){
			valsTmp.clear();
			toFind=vals.get(i);
			while(i<vals.size() && toFind==vals.get(i)) {
				valsTmp.add(vals.get(i+1));
				i+=2;
			}
			i-=2;
			BookPresenter presenter = a.getBookByIdNoCreation(toFind);
			if(presenter!=a.EmptyBook) {
				long[] p = new long[valsTmp.size()];
				for(int i1 = 0;i1<valsTmp.size();i1++){
					p[i1] = valsTmp.get(i1);
				}
				frames.add(presenter);
				framesDisplaying.add(p);
			}
		}
		// [3rd pass] Preserve valid frames
		// [4th pass] Erase rest insert-before buff
		// [5th pass] Load frames to dom
		
		if(mergeFrames==0 && bUseDictView) {
			// todo remove adaptively .
			weblistHandler.removeAllViews();
		}
		
		//if(false)
		//yyy
		a.awaiting = true;
		//weblistHandler.installLayoutScrollListener(this);
		
		boolean checkReadEntry = a.opt.getAutoReadEntry();
		boolean bNeedExpand=true;
		View expTbView = null;
		StringBuilder mergedUrl = null;
		ViewGroup webholder = weblistHandler;
		if (bFoldingScreen) {
			weblistHandler.renderFoldingScreen(jointResult.LongestStartWithSeqLength<0?-jointResult.LongestStartWithSeqLength:0);
		}
		else {
			for(int i=0,ln=frames.size();i<ln;i++){
				BookPresenter presenter = frames.get(i);
				long[] displaying = framesDisplaying.get(i);
				if(bUseMergedUrl) {
					if(mergedUrl==null)
						mergedUrl = new StringBuilder("http://mdbr.com/merge.jsp?q=")
							.append(SU.encode(jointResult.key)).append("&exp=");
					else mergedUrl.append("-");
					PlainWeb webx = presenter.getWebx();
					mergedUrl.append(webx!=null?
							"w"
							:"d");
					IU.NumberToText_SIXTWO_LE(presenter.getId(), mergedUrl);
					if(webx!=null) {
						mergedUrl.append("_")
							.append(webx.hasField("synthesis")?"0":"");
					} else {
						for (long val:displaying) {
							mergedUrl.append("_");
							IU.NumberToText_SIXTWO_LE(val, mergedUrl);
						}
					}
				}
				else {
					//if(Build.VERSION.SDK_INT>=22)...// because kitkat's webview is not that adaptive for content height
					WebViewmy mWebView;
					if(bUseDictView) {
						presenter.initViewsHolder(a);
						mWebView = presenter.mWebView;
					} else {
						mWebView = weblistHandler.getMergedFrame(presenter);
					}
					View rl = mWebView.rl;
					int frameAt=i;
					//if(rl.getParent()!=a.weblistHandler.getViewGroup())
					{
						ViewUtils.removeView(rl);
						frameAt=Math.min(frameAt, webholder.getChildCount());
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
					mWebView.fromCombined=weblistHandler.isViewSingle()?0:1;
					if(presenter.getIsWebx())
					{
						presenter.SetSearchKey(jointResult.key);
					}
					mWebView.weblistHandler = weblistHandler;
					//CMN.debug("combining_search_result.renderContentAt::", frameAt);
					presenter.renderContentAt(-1, BookPresenter.RENDERFLAG_NEW, frameAt,mWebView, displaying);
					if(!mWebView.awaiting){
						bNeedExpand=false;
						if(checkReadEntry){
							mWebView.bRequestedSoundPlayback=true;
							checkReadEntry=false;
						}
					} else if(bNeedExpand && !presenter.getNeedsAutoFolding(mWebView.frameAt)) {
						expTbView = mWebView.toolbar_title;
						bNeedExpand = false;
					}
					mWebView.fromCombined=1;
				}
			}
		}
		//weblistHandler.initMergedFrame(bUseMergedUrl, weblistHandler.bShowInPopup, bUseMergedUrl);
		WebViewmy mWebView = weblistHandler.mMergedFrame;
		if(bFoldingScreen) {
			weblistHandler.resetScrollbar(mWebView, true, false);
		}
		else if(bUseMergedUrl) {
			CMN.debug("mergedUrl::", mergedUrl);
			mWebView.getSettings().setSupportZoom(true);
			mWebView.loadUrl(mergedUrl.toString());
//			mWebView.loadUrl("https://en.m.wiktionary.org/wiki/Wiktionary:Word_of_the_day/Archive/2016/September");
			mWebView.jointResult=jointResult;
			weblistHandler.resetScrollbar(mWebView, true, false);
			weblistHandler.contentUIData.PageSlider.setWebview(mWebView, null);
		}
		else {
//			if(bNeedExpand && PDICMainAppOptions.getEnsureAtLeatOneExpandedPage()){
//				//expTbView = webholder.findViewById(R.id.toolbar_title); //yyy!!!
//				expTbView = weblistHandler.getViewGroup().findViewById(R.id.toolbar_title); //yyy!!!
//			}
//			if (expTbView != null) {
//				expTbView.performClick();
//			}
			if(bUseDictView)
				weblistHandler.jointResult=jointResult;
			else
				mWebView.jointResult=jointResult;
			if(!weblistHandler.isViewSingle())
				mWebView=null;
			weblistHandler.contentUIData.PageSlider.setWebview(mWebView, weblistHandler.isViewSingle()?null:weblistHandler);
			weblistHandler.resetScrollbar(mWebView, false, false);
		}
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
		scrollTarget=_scrollTarget;
		a.weblistHandler.NotifyScrollingTo(this);
		scrolled=false;
	}
	
	public boolean shouldAddHistory(MainActivityUIBase a) {
		if(!schKeySaved) {
			schKeySaved=true;
			return true;
		}
		return false;
	}
	
	public void handleNavJump(MainActivityUIBase a, WebViewmy mWebView) {
		/* 接管同一词典不同页面的网页前后跳转 */
		if (!a.weblistHandler.isViewSingle()) {
			a.weblistHandler.WHP.touchFlag.first=false;
			ViewGroup webholder = a.weblistHandler.getViewGroup();
			//WHP.post(() -> {
			//WHP.smoothScrollTo(0, mWebView.expectedPos);
			//WHP.smoothScrollTo(mWebView.expectedPosX, mWebView.expectedPos);
			expectedPos=mWebView.expectedPos;
			LHGEIGHT=0;
			scrolled=false;
			ViewUtils.addOnLayoutChangeListener(webholder, a.weblistHandler.OLCL);
			a.weblistHandler.OLCL.onLayoutChange(webholder,0, webholder.getTop(),0,webholder.getBottom(),0,0,0,0);
			//});
		}
	}
}

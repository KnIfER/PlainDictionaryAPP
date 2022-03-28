package com.knziha.plod.PlainUI;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CrashHandler;
import com.knziha.plod.plaindict.DictPicker;
import com.knziha.plod.plaindict.FloatSearchActivity;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.BottomNavigationBehavior;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.LinearSplitView;
import com.knziha.plod.widgets.PopupGuarder;
import com.knziha.plod.widgets.PopupMoveToucher;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.TwoColumnAdapter;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WordPopup extends PlainAppPanel implements Runnable{
	public WebViewListHandler weblistHandler;
	String popupKey;
	int popupFrame;
	BookPresenter popupForceId;
	public TextView popupTextView;
	protected PopupMoveToucher popupMoveToucher;
	public FlowTextView popupIndicator;
	public RLContainerSlider pageSlider;
	public WebViewmy popupWebView;
	public BookPresenter.AppHandler popuphandler;
	public ImageView popIvBack;
	public View popCover;
	public ViewGroup popupContentView;
	protected ImageView popupStar;
	public ViewGroup popupToolbar;
	protected ViewGroup popupBottombar;
	protected CircleCheckBox popupChecker;
	public WeakReference<ViewGroup> popupCrdCloth;
	public WeakReference<ViewGroup> popupCmnCloth;
	
	public PopupGuarder popupGuarder;
	public String currentClickDisplaying;
	public int currentClickDictionary_currentPos;
	public int[] currentClickDictionary_currentMergedPos;
	public int currentClick_adapter_idx;
	public int CCD_ID;
	@NonNull public BookPresenter CCD;
	public ArrayList<myCpr<String, int[]>> popupHistory = new ArrayList<>();
	public int popupHistoryVagranter=-1;
	private WebViewmy invoker;
	private boolean isPreviewDirty;
	private Runnable harvestCallBack;
	private resultRecorderCombined rec;
	
	DictPicker dictPicker;
	ViewGroup splitter;
	
	public WordPopup(MainActivityUIBase a) {
		super(a, true);
		//this.a = a;
		bAnimate=false;
		bAutoRefresh=false;
		showType=1;
		bottomPadding=0;
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
	}
	
	public void refresh() {
		if(popupWebView!=null){
			if(GlobalOptions.isDark){
				popupContentView.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				popupBottombar.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				popIvBack.setImageResource(R.drawable.abc_ic_ab_white_material);
			} else if(popIvBack.getTag()!=null){
				popupContentView.getBackground().clearColorFilter();
				popupBottombar.getBackground().clearColorFilter();
				popIvBack.setImageResource(R.drawable.abc_ic_ab_back_material_simple_compat);
			}
			if(popupIndicator!=null) {
				popupTextView.setTextColor(GlobalOptions.isDark?a.AppBlack:Color.GRAY);
				popupIndicator.setTextColor(GlobalOptions.isDark?a.AppBlack:0xff2b43c1);
			}
		}
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.cover: {
				if(v==popCover){
					a.getUcc().setInvoker(CCD, popupWebView, null, null);
					a.getUcc().onClick(v);
				}
			} break;
			case R.id.popupBackground: {
				dismissImmediate();
			} break;
			case R.id.popNxtE:
			case R.id.popLstE: {
				if(CCD==null)
					CCD=a.currentDictionary;
				int np = currentClickDictionary_currentPos+(id==R.id.popNxtE?1:-1);
				resetPreviewIdx();
				if(np>=0&&np<CCD.bookImpl.getNumberEntries()){
					popupTextView.setText(currentClickDisplaying=CCD.bookImpl.getEntryAt(np));
					popupHistory.add(++popupHistoryVagranter,new myCpr<>(currentClickDisplaying,new int[]{CCD_ID, np}));
					if (popupHistory.size() > popupHistoryVagranter + 1) {
						popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
					}
					popuphandler.setDict(CCD);
					if(PDICMainAppOptions.getClickSearchAutoReadEntry())
						popupWebView.bRequestedSoundPlayback=true;
					popupWebView.fromCombined=2;
					CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, popupWebView, currentClickDictionary_currentPos=np);
					a.decorateContentviewByKey(popupStar, currentClickDisplaying);
					if(!PDICMainAppOptions.getHistoryStrategy0() && PDICMainAppOptions.getHistoryStrategy8()==0)
						a.insertUpdate_histroy(currentClickDisplaying, 0, pageSlider);
				}
			} break;
			case R.id.popNxtDict:
			case R.id.popLstDict:{
				//SearchNxt(id==R.id.popNxtDict, task, taskVer, taskVersion);
				startSearchTask(id==R.id.popNxtDict?2:1);
			} break;
			//返回
			case R.id.popIvBack:{
				dismissImmediate();
			} break;
			//返回
			case R.id.popIvRecess:{
				popNav(true);
			} break;
			case R.id.popIvForward:{
				popNav(false);
			} break;
			case R.id.popIvSettings:{
				a.launchSettings(9, 999);
			} break;
			case R.id.popChecker:{
				CircleCheckBox checker = (CircleCheckBox) v;
				checker.toggle();
				PDICMainAppOptions.setPinTapTranslator(checker.isChecked());
				popupGuarder.isPinned = checker.isChecked();
				dismissImmediate();
				show();
			} break;
			case R.id.popIvStar:{
				a.collectFavoriteView = popupContentView;
				a.toggleStar(currentClickDisplaying, (ImageView) v, false, pageSlider);
				a.collectFavoriteView = null;
			} break;
			case R.id.popupText1:{
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(v.getTag());
				if(dd==null) {
					RecyclerView rv = new RecyclerView(a);
					GridLayoutManager lm = new GridLayoutManager(a, 2);
					rv.setLayoutManager(lm);
					lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
						@Override
						public int getSpanSize(int position) {
							return position==0?2:1;
						}
					});
					rv.setAdapter(new TwoColumnAdapter(previewEntryData).setMaxLines(2));
					rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
					rv.setPadding(0, (int) (GlobalOptions.density*8),0,0);
					dd = new AlertDialog.Builder(a)
							//.setTitle("翻译当前页面")
							.setView(rv)
							.setPositiveButton("下一页", null)
							.setNegativeButton("上一页", null)
							.setNeutralButton("重置", null)
							.show();
					AlertDialog finalDd = dd;
					ViewUtils.setOnClickListenersOneDepth(dd.findViewById(R.id.buttonPanel), v1 -> {
						int id1 = v1.getId();
						if(id1 ==android.R.id.button1) {
							previewPageIdx++;
						} else if(id1 ==android.R.id.button2){
							previewPageIdx--;
						} else if(id1 ==android.R.id.button3){
							previewPageIdx=0;
						}
						isPreviewDirty = true;
						refillPreviewEntries(finalDd, false);
					}, 999, null);
					dd.tag=rv;
					v.setTag(new WeakReference<>(dd));
				}
				dd.show();
				dd.getWindow().setDimAmount(0);
				refillPreviewEntries(dd, true);
			} break;
			case R.id.popupText2:{
				if(PDICMainAppOptions.getSwichClickSearchDictOnBottom()) {
					dictPicker.toggle();
				}
			} break;
		}
	}
	
	private boolean getPin() {
		return popupChecker==null?PDICMainAppOptions.getPinTapTranslator():popupChecker.isChecked();
	}
	
	public void show() {
		if (!isVisible()) {
			int type=getPin()?0:2;
			toggle(lastTargetRoot, null, type);
			int pad = type==0?0: (int) (GlobalOptions.density * 19);
			if(settingsLayout.getPaddingTop()!=pad)settingsLayout.setPadding(0,pad,0,0);
		}
	}
	
	final private void resetPreviewIdx() {
		previewPageIdx = 0;
		isPreviewDirty = true;
	}
	
	private void refillPreviewEntries(AlertDialog dialog, boolean delay) {
		if(isPreviewDirty)
		{
			int base = currentClickDictionary_currentPos-1;
			RecyclerView rv = (RecyclerView) dialog.tag;
			if(previewPageIdx==0) {
				previewEntryData[1] = CCD.bookImpl.getEntryAt(base);
				previewEntryData[2] = "编辑搜索词";
				previewEntryData[3] = CCD.bookImpl.getEntryAt(base+1);
				previewEntryData[4] = "翻阅模式";
				previewEntryData[5] = CCD.bookImpl.getEntryAt(base+2);
				previewEntryData[6] = "切换上一词典";
			} else {
				if(previewPageIdx<0) base+=previewPageIdx*6;
				else base=currentClickDictionary_currentPos+3+(previewPageIdx-1)*6;
				previewEntryData[1] = CCD.bookImpl.getEntryAt(base);
				previewEntryData[2] = CCD.bookImpl.getEntryAt(base+3);
				previewEntryData[3] = CCD.bookImpl.getEntryAt(base+1);
				previewEntryData[4] = CCD.bookImpl.getEntryAt(base+4);
				previewEntryData[5] = CCD.bookImpl.getEntryAt(base+2);
				previewEntryData[6] = CCD.bookImpl.getEntryAt(base+5);
			}
			rv.getAdapter().postDataSetChanged(rv, delay?180:10);
			((TextView)dialog.findViewById(android.R.id.button3)).setText("重置"+(previewPageIdx==0?"":" ("+previewPageIdx+")"));
		}
	}
	
	int previewPageIdx;
	String[] previewEntryData = new String[]{
			"收藏当前词条"
			, "编辑搜索词"
			, "joy"
			, "切换上一词典"
			, "happy"
			, "翻阅模式"
			, "fun"
	};
	
	public boolean popNav(boolean isGoBack) {
		if(true) {
			
			if (isGoBack) {
				popupWebView.goBack();
			} else {
				popupWebView.goForward();
			}
			
		}
		long tm;
		if((!isGoBack && !popupWebView.isloading && popupHistoryVagranter<popupHistory.size()-1 && popupWebView.canGoForward()
				|| isGoBack && popupHistoryVagranter>0  && popupWebView.canGoBack())
				&& (tm=System.currentTimeMillis())-a.lastClickTime>300) {
			
			if (isGoBack) {
				popupWebView.goBack();
			} else {
				popupWebView.goForward();
			}
			
			try {
				myCpr<String, int[]> record = popupHistory.get(popupHistoryVagranter+=(isGoBack?-1:1));
				popupTextView.setText(currentClickDisplaying=record.key);
				//popupWebView.SelfIdx = CCD_ID = record.value[0];
				currentClickDictionary_currentPos = record.value[1];
				popupIndicator.setText((CCD=a.md.get(CCD_ID)).getDictionaryName());
				popuphandler.setDict(CCD);
			} catch (Exception e) { CMN.Log(e); }
			
			a.lastClickTime=tm;
			resetPreviewIdx();
			return true;
		}
		return false;
	}
	
	public void init_popup_view() {
		if (popupWebView == null) {
			weblistHandler/*faked*/ = new WebViewListHandler(a, a.contentUIData/*faked*/);
			popupContentView = (ViewGroup) a.getLayoutInflater()
					.inflate(R.layout.float_contentview_basic, a.root, false);
			popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
			popupToolbar = (ViewGroup) popupContentView.getChildAt(0);
			LinearSplitView split = (LinearSplitView) popupContentView.getChildAt(1);
			pageSlider = (RLContainerSlider) split.getChildAt(0);
			splitter = (ViewGroup) popupContentView.getChildAt(3);
			dictPicker = new DictPicker(a, split, splitter, 2);
			WebViewmy mPopupWebView = (WebViewmy) pageSlider.getChildAt(0);
			mPopupWebView.getSettings().setTextZoom(118);
			mPopupWebView.fromCombined = 2;
			pageSlider.WebContext = mPopupWebView;
			popupBottombar = (ViewGroup) popupContentView.getChildAt(2);
			popuphandler = new BookPresenter.AppHandler(a.currentDictionary);
			mPopupWebView.addJavascriptInterface(popuphandler, "app");
			mPopupWebView.setBackgroundColor(Color.TRANSPARENT);
			((AdvancedNestScrollWebView)mPopupWebView).setNestedScrollingEnabled(true);
			popCover = pageSlider.getChildAt(1);
			popIvBack = popupToolbar.findViewById(R.id.popIvBack);
			popupStar = popupToolbar.findViewById(R.id.popIvStar);
			ViewUtils.setOnClickListenersOneDepth(popupToolbar, this, 999, null);
			ViewUtils.setOnClickListenersOneDepth(popupBottombar, this, 999, null);
			popupChecker = popupBottombar.findViewById(R.id.popChecker);
			popupChecker.setChecked(PDICMainAppOptions.getPinTapTranslator());
			popupTextView = popupToolbar.findViewById(R.id.popupText1);
			mPopupWebView.IBC = new PhotoBrowsingContext();
			popupIndicator = popupBottombar.findViewById(R.id.popupText2);
			mPopupWebView.toolbar_title = new FlowTextView(popupIndicator.getContext());
			if(GlobalOptions.isDark) {
				popupTextView.setTextColor(Color.WHITE);
				popupIndicator.setTextColor(Color.WHITE);
			}
			
			// 点击背景
			settingsLayoutHolder = settingsLayout = popupGuarder = new PopupGuarder(a.getBaseContext());
			popupGuarder.onPopupDissmissed = this;
			popupGuarder.setId(R.id.popupBackground);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				popupGuarder.setElevation(5 * a.dm.density);
			}
			//popupGuarder.setBackgroundColor(Color.BLUE);
			a.root.addView(popupGuarder, new FrameLayout.LayoutParams(-1, -1));
			// 弹窗搜索移动逻辑， 类似于浮动搜索。
			popupMoveToucher = new PopupMoveToucher(a, popupTextView);
			for (int i = 0; i < popupToolbar.getChildCount(); i++) {
				popupToolbar.getChildAt(i).setOnTouchListener(popupMoveToucher);
			}
			for (int i = 0; i < popupBottombar.getChildCount(); i++) {
				popupBottombar.getChildAt(i).setOnTouchListener(popupMoveToucher);
			}
			// 缩放逻辑
			popupWebView = mPopupWebView;
			mPopupWebView.weblistHandler = weblistHandler;
			
			popupGuarder.setOnTouchListener(popupMoveToucher);
			popupGuarder.setClickable(true);
		}
		if (GlobalOptions.isDark) {
			popupChecker.drawInnerForEmptyState = true;
			popupChecker.circle_shrinkage = 0;
		}
		else {
			popupChecker.drawInnerForEmptyState = false;
			popupChecker.circle_shrinkage = 2;
		}
	}
	
	boolean isInit;
	
	@Override
	public void run() {
		launch();
	}
	
	WordPopupTask wordPopupTask = new WordPopupTask(this);
	
	private void launch() {
		if(RLContainerSlider.lastZoomTime > 0){
			if (System.currentTimeMillis() - RLContainerSlider.lastZoomTime < 500){
				return;
			}
			RLContainerSlider.lastZoomTime=0;
		}
		//CMN.Log("\nmPopupRunnable run!!!");
		int size = a.md.size();
		if (size <= 0) return;
		reInit();
		
		boolean bPeruseViewAttached = a.PeruseViewAttached();
		ViewGroup targetRoot = bPeruseViewAttached? a.peruseView.root:a.root;
		if(lastTargetRoot != targetRoot) {
			if(lastTargetRoot!=null) dismiss();
			lastTargetRoot = targetRoot;
		}
		
		AttachViews();
		
		show();
		
		CMN.Log("popupGuarder::", popupGuarder.getParent());
		
		
		if(harvestCallBack==null)
			harvestCallBack = this::SearchDone;
		popupTextView.setText(popupKey);
		
		//SearchOne(task, taskVer, taskVersion);
		
		startSearchTask(0);
	}
	
	private void startSearchTask(int type) {
		if(!wordPopupTask.start(type)) {
			wordPopupTask.stop();
			wordPopupTask = new WordPopupTask(this);
			boolean ret = wordPopupTask.start(type);
			CMN.Log("新开线程……", ret, CMN.now());
		}
	}
	
	private void reInit() {
		//CMN.Log("popupWord", popupKey, x, y, frameAt);
		boolean isNewHolder;
		// 初始化核心组件
		isInit = isNewHolder = popupWebView == null||popupWebView.fromCombined!=2;
		init_popup_view();
		// 给你换身衣裳
		WeakReference<ViewGroup> holder = (PDICMainAppOptions.getImmersiveClickSearch() ? popupCrdCloth : popupCmnCloth);
		ViewGroup mPopupContentView = popupContentView;
		popupContentView = holder == null ? null : holder.get();
		boolean b1 = popupContentView == null;
		isNewHolder = isNewHolder || b1;
		View cv = (View) this.pageSlider.getParent();
		if (b1 || popupContentView != cv.getParent()) {
			//ViewUtils.removeView(popupToolbar);
			//ViewUtils.removeView(PopupPageSlider);
			//ViewUtils.removeView(popupBottombar);
			ViewUtils.removeView(mPopupContentView);
			ViewUtils.removeView(popupToolbar);
			ViewUtils.removeView(cv);
			ViewUtils.removeView(popupBottombar);
			if (PDICMainAppOptions.getImmersiveClickSearch()) {
				popupContentView = (popupCrdCloth != null && popupCrdCloth.get() != null) ? popupCrdCloth.get()
						: (popupCrdCloth = new WeakReference<>((ViewGroup) a.getLayoutInflater()
						.inflate(R.layout.float_contentview_coord, a.root, false))).get();
				ViewGroup appbar = (ViewGroup) popupContentView.findViewById(R.id.appbar);
				ViewUtils.addViewToParent(popupToolbar, appbar);
				ViewUtils.addViewToParent(cv, popupContentView);
				ViewUtils.addViewToParent(popupBottombar, popupContentView);
				((CoordinatorLayout.LayoutParams) popupBottombar.getLayoutParams()).gravity = Gravity.BOTTOM;
				((CoordinatorLayout.LayoutParams) popupBottombar.getLayoutParams()).setBehavior(new BottomNavigationBehavior(popupContentView.getContext(), null));
				((CoordinatorLayout.LayoutParams) cv.getLayoutParams()).setBehavior(new AppBarLayout.ScrollingViewBehavior(popupContentView.getContext(), null));
				((CoordinatorLayout.LayoutParams) cv.getLayoutParams()).height = MATCH_PARENT;
				((CoordinatorLayout.LayoutParams) cv.getLayoutParams()).topMargin = 0;
				((CoordinatorLayout.LayoutParams) cv.getLayoutParams()).bottomMargin = 0;
				((AppBarLayout.LayoutParams) popupToolbar.getLayoutParams()).setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
			} else {
				popupContentView = (popupCmnCloth != null && popupCmnCloth.get() != null) ? popupCmnCloth.get()
						: (popupCmnCloth = new WeakReference<>((ViewGroup) a.getLayoutInflater()
						.inflate(R.layout.float_contentview_basic_outer, a.root, false))).get();
				ViewUtils.addViewToParent(popupToolbar, popupContentView);
				ViewUtils.addViewToParent(cv, popupContentView);
				ViewUtils.addViewToParent(popupBottombar, popupContentView);
				popupToolbar.setTranslationY(0);
				cv.setTranslationY(0);
				popupBottombar.setTranslationY(0);
				((FrameLayout.LayoutParams) cv.getLayoutParams()).topMargin = (int) (45*GlobalOptions.density);
				((FrameLayout.LayoutParams) cv.getLayoutParams()).bottomMargin = (int) (30*GlobalOptions.density);
				((FrameLayout.LayoutParams) popupBottombar.getLayoutParams()).gravity = Gravity.BOTTOM;
			}
		}
		popupGuarder.popupToGuard = popupContentView;
		popupGuarder.isPinned = getPin();
		ViewUtils.addViewToParent(popupContentView, popupGuarder);
		try {
			ViewUtils.addViewToParent(splitter, popupContentView);
		} catch (Exception e) {
			CMN.Log(e);
		}
		try {
			ViewUtils.addViewToParent(splitter, popupContentView);
		} catch (Exception e) {
			CMN.Log(e);
		}
		
		if (isNewHolder) {
			popupWebView.fromCombined = 2;
			refresh();
			popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
			FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) popupContentView.getLayoutParams());
			lp.height = popupMoveToucher.FVH_UNDOCKED = (int) (a.dm.heightPixels * 7.0 / 12 - a.getResources().getDimension(R.dimen._20_));
			if (mPopupContentView != null && !isInit) {
				popupContentView.setTranslationY(mPopupContentView.getTranslationY());
				lp.height = mPopupContentView.getLayoutParams().height;
			}
		}
	}
	
	ViewGroup lastTargetRoot;
	
	private void AttachViews() {
		// 初次添加请指明方位
		if (!getPin() || !isVisible()) {
			ViewGroup targetRoot = lastTargetRoot;
			if (popupMoveToucher.FVDOCKED && popupMoveToucher.Maximized && PDICMainAppOptions.getResetMaxClickSearch()) {
				popupMoveToucher.Dedock();
			}
			CMN.Log("poping up ::: ", a.ActivedAdapter);
			if (popupKey!=null && (PDICMainAppOptions.getResetPosClickSearch() || isInit) && !popupMoveToucher.FVDOCKED) {
				float ty = 0;
				float now = 0;
				if (a.ActivedAdapter != null || popupFrame<0) {
					//CMN.Log("???", y, targetRoot.getHeight()-popupGuarder.getResources().getDimension(R.dimen.halfpopheader));
					if(popupFrame==-1){
						now = a.mActionModeHeight;
						CMN.Log(now, targetRoot.getHeight() / 2);
					}
					else if(invoker.peruseView!=null){
						now = invoker.peruseView.getWebTouchY();
					}
					else if (invoker.weblistHandler.isViewSingle()) {
						now = invoker.lastY + invoker.getTop();
						//CMN.Log("now",sv.getChildAt(0).getHeight(), ((ViewGroup.MarginLayoutParams) getContentviewSnackHolder().getLayoutParams()).topMargin);
					}
					else {
						now = invoker.rl.getTop() + invoker.lastY + invoker.getTop() - invoker.weblistHandler.WHP.getScrollY();
					}
					if(a.thisActType!= MainActivityUIBase.ActType.MultiShare) {
						try {
							if(PDICMainAppOptions.getEnableSuperImmersiveScrollMode()){
								now += a.contentview.getTop();
							} else {
								now += ((ViewGroup.MarginLayoutParams) a.contentview.getLayoutParams()).topMargin;
							}//333 contentSnackHolder
						} catch (Exception e) {
							CMN.Log(e);
						}
					}
					float pad = 56 * a.dm.density;
					if (a instanceof FloatSearchActivity)
						now += ((FloatSearchActivity) a).getPadHoldingCS();
					CMN.Log("now",now);
					if (now < targetRoot.getHeight() / 2) {
						ty = now + pad;
					} else {
						ty = now - popupMoveToucher.FVH_UNDOCKED - pad;
					}
				}
				//CMN.Log("min", getVisibleHeight()-popupMoveToucher.FVH_UNDOCKED-((ViewGroup.MarginLayoutParams)popupContentView.getLayoutParams()).topMargin*2);
				popupContentView.setTranslationY(Math.min(a.getVisibleHeight() - popupMoveToucher.FVH_UNDOCKED - ((ViewGroup.MarginLayoutParams) popupContentView.getLayoutParams()).topMargin * 2, Math.max(0, ty)));
				//a.showT(popupContentView.getTranslationY());
			}
			
			//ViewUtils.addViewToParent(popupGuarder, targetRoot);
			//if(idx>=0){
			a.fix_full_screen(null);
			//}
		}
		//else popupWebView.loadUrl("about:blank");
		//CMN.recurseLog(popupContentView, null);
	}
	
	@AnyThread
	public void SearchNxt(boolean nxt, AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		resetPreviewIdx();
		int idx=-1, cc=0;
		String key = true?ViewUtils.getTextInView(popupTextView).trim():popupKey;
		if(key.length()>0) {
			ArrayList<PlaceHolder> ph = a.getPlaceHolders();
			String keykey;
			int OldCCD=CCD_ID;
			boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
			int SearchMode = PDICMainAppOptions.getClickSearchMode();
			boolean hasDedicatedSeachGroup = SearchMode==1&&a.bHasDedicatedSeachGroup;
			boolean reject_morph = false;
			//轮询开始
			while(true){
				if(nxt){
					CCD_ID++;
				}else{
					CCD_ID--;
					if(CCD_ID<0)CCD_ID+=a.md.size();
				}
				CCD_ID=CCD_ID%a.md.size();
				
				if(hasDedicatedSeachGroup && CCD_ID<ph.size() && !PDICMainAppOptions.getTmpIsClicker(ph.get(CCD_ID).tmpIsFlag))
					continue;
				CCD=a.md_get(CCD_ID);
				cc++;
				if(cc>a.md.size())
					break;
				
				if (CCD!=a.EmptyBook) {
					if(CCD.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
//						PlainWeb webx = (PlainWeb) CCD.bookImpl;
//						if(webx.takeWord(key)) {
//							CCD.SetSearchKey(key);
//							idx=0;
//						}
						continue;
					} else  {
						idx=CCD.bookImpl.lookUp(key, true);
						if(idx<0){
							if(!reject_morph&&use_morph){
								keykey=a.ReRouteKey(key, true);
								if(keykey!=null)
									idx=CCD.bookImpl.lookUp(keykey, true);
								else
									reject_morph=true;
							}
						}
					}
				}
				
				if(idx>=0 || hasDedicatedSeachGroup && CCD!=a.EmptyBook ||  !PDICMainAppOptions.getSkipClickSearch())
					break;
			}
			
			//应用轮询结果
			if(OldCCD!=CCD_ID && CCD!=a.EmptyBook && task.get()&& taskVer == taskVersion.get()){
				if(PDICMainAppOptions.getSwichClickSearchDictOnNav()){
					currentClick_adapter_idx = CCD_ID;
				}
//				popupIndicator.setText(CCD.getDictionaryName());
				if(idx<0 && hasDedicatedSeachGroup){
					idx = -1-idx;
					popupWebView.setTag(R.id.js_no_match, false);
				}
//				if (idx >= 0) {
//					popupHistory.add(++popupHistoryVagranter,new myCpr<>(currentClickDisplaying,new int[]{CCD_ID, idx}));
//					if (popupHistory.size() > popupHistoryVagranter + 1) {
//						popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
//					}
//					popuphandler.setDict(CCD);
//					if(PDICMainAppOptions.getClickSearchAutoReadEntry())
//						popupWebView.bRequestedSoundPlayback=true;
//					popupWebView.fromCombined=2;
//					CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, popupWebView, currentClickDictionary_currentPos=idx);
//				}
				currentClickDictionary_currentPos=idx;
				harvest();
			}
		}
	}
	
	RBTree_additive _treeBuilder = new RBTree_additive();
	
	@AnyThread
	private void SearchMultiple(AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		int size = a.md.size();
		_treeBuilder.clear();
		int paragraphWords = 9;
		String searchText = popupKey;
		boolean isParagraph = BookPresenter.testIsParagraph(searchText, paragraphWords);
		CMN.Log("isParagraph::", isParagraph);
		_treeBuilder.setKeyClashHandler(searchText);
		for (int i = 0; i < size && task.get(); i++) {
			ArrayList<PlaceHolder> CosyChair = a.getPlaceHolders();
			PlaceHolder phTmp = i<CosyChair.size()?CosyChair.get(i):null;
			if (phTmp != null) {
				BookPresenter book = a.md.get(i);
				if (book == null) {
					try {
						a.md.set(i, book = a.new_book(phTmp, a));
					} catch (Exception e) { }
				}
				try {
					if(book!=null && book.getAcceptParagraph(searchText, isParagraph, paragraphWords)) {
						CrashHandler.hotTracingObject = book;
						book.bookImpl.lookUpRange(searchText, null, _treeBuilder, book.getId(),7, task);
					}
				} catch (Exception e) {
					CMN.Log(CrashHandler.hotTracingObject, e);
				}
			}
		}
		resultRecorderCombined rec = new resultRecorderCombined(a, _treeBuilder.flatten(), a.md, searchText);
		if (rec.FindFirstIdx(searchText, task) && taskVer==taskVersion.get()) {
			this.rec = rec;
			harvest();
		}
	}
	
	@AnyThread
	private void harvest() {
		a.root.removeCallbacks(harvestCallBack);
		a.root.post(harvestCallBack);
	}
	
	@AnyThread
	private void SearchOne(AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		int size = a.md.size();
		int idx = -1, cc = 0;
		resetPreviewIdx();
		if (popupKey != null) {
			String keykey;
			CCD_ID = currentClick_adapter_idx = Math.min(currentClick_adapter_idx, size-1);
			if(popupForceId!=null) {
				CCD = popupForceId;
				CCD_ID = a.md.indexOf(popupForceId);
				if(CCD_ID<0) {
					CCD_ID = a.md.size();
					a.md.add(popupForceId); // todo check???
				}
			}
			//轮询开始
			//nimp
			BookPresenter webx = null;
			boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
			int SearchMode = PDICMainAppOptions.getClickSearchMode();
			CMN.Log("SearchMode", SearchMode);
			boolean bForceJump = false;
			if (SearchMode == 2) {/* 仅搜索当前词典 */
				CCD = a.md_get(CCD_ID);
				if (CCD != a.EmptyBook) {
					if(CCD.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
						webx = CCD;
						if (PDICMainAppOptions.getTapSkipWebxUnlessIsDedicated()
								&& (!PDICMainAppOptions.getTmpIsClicker(CCD.tmpIsFlag)
									&& (!PDICMainAppOptions.getTapTreatTranslatorAsDedicated() || !webx.getWebx().getIsTranslator()))
							|| !((PlainWeb)webx.bookImpl).takeWord(popupKey)) {
							webx = null;
						}
					} else  {
						idx = CCD.bookImpl.lookUp(popupKey, true);
						if (idx < -1 && use_morph) {
							keykey = a.ReRouteKey(popupKey, true);
							if (keykey != null) idx = CCD.bookImpl.lookUp(keykey, true);
						}
					}
				}
			}
			else {
				boolean proceed = true;
				if (SearchMode == 1) {/* 仅搜索指定点译词典 */
					a.bHasDedicatedSeachGroup=false;
					BookPresenter firstAttemp = null;
					FindCSD:
					while(task.get()) {
						BookPresenter mdTmp;
						int CSID;
						for (int i = 0; i < a.md.size(); i++) {
							mdTmp = null;
							CSID = (i + CCD_ID) % a.md.size();
							ArrayList<PlaceHolder> CosyChair = a.getPlaceHolders();
							if (CSID < CosyChair.size()) {
								PlaceHolder phTmp = CosyChair.get(CSID);
								if (phTmp != null) {
									if (PDICMainAppOptions.getTmpIsClicker(phTmp.tmpIsFlag)) {
										mdTmp = a.md.get(CSID);
										if (mdTmp == null) {
											try {
												a.md.set(CSID, mdTmp = a.new_book(phTmp, a));
											} catch (Exception e) { }
										}
									}
								}
							}
							if (mdTmp != null) {
								if (!bForceJump && firstAttemp == null)
									firstAttemp = mdTmp;
								a.bHasDedicatedSeachGroup=true;
								proceed=false;
								if(mdTmp.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
									webx = mdTmp;
									if (PDICMainAppOptions.getTapSkipWebxUnlessIsDedicated()
											&& (!PDICMainAppOptions.getTmpIsClicker(CCD.tmpIsFlag)
											&& (!PDICMainAppOptions.getTapTreatTranslatorAsDedicated() || !webx.getWebx().getIsTranslator()))
											/*|| !((PlainWeb)webx.bookImpl).takeWord(popupKey)*/) {
										webx = null;
									}
									else if (bForceJump || ((PlainWeb)webx.bookImpl).takeWord(popupKey)) {
										break;
									}
									webx = null;
								}
								else
								{
									idx = mdTmp.bookImpl.lookUp(popupKey, true);
									if (idx < -1 && use_morph) {
										keykey = a.ReRouteKey(popupKey, true);
										if (keykey != null)
											idx = mdTmp.bookImpl.lookUp(keykey, true);
									}
									if(idx<0 && bForceJump){
										idx = -1-idx;
									}
									if (idx >= 0) {
										CCD_ID = (i + CCD_ID) % a.md.size();
										CCD = mdTmp;
										break FindCSD;
									}
									if(bForceJump){
										break FindCSD;
									}
								}
							}
						}
						if (firstAttemp != null && a.md.size()>0) {
							bForceJump=true;
							firstAttemp=null;
						} else {
							break;
						}
					}
					
				}
				boolean reject_morph = false;
				if (proceed)/* 未指定点译词典 */
					while (task.get()) {
						if (cc > a.md.size())
							break;
						CCD_ID = CCD_ID % a.md.size();
						CCD = a.md.get(CCD_ID);
						if (CCD == null) {
							ArrayList<PlaceHolder> CosyChair = a.getPlaceHolders();
							if (CCD_ID < CosyChair.size()) {
								PlaceHolder phTmp = CosyChair.get(CCD_ID);
								if (phTmp != null) {
									try {
										a.md.set(CCD_ID, CCD = a.new_book(phTmp, a));
									} catch (Exception e) {
										CMN.Log(e);
									}
								}
							}
						}
						if (CCD == null) {
							CCD = a.EmptyBook;
						}
						if(CCD.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
							webx = CCD;
							if (PDICMainAppOptions.getTapSkipWebxUnlessIsDedicated()
									&& (!PDICMainAppOptions.getTmpIsClicker(CCD.tmpIsFlag)
									&& (!PDICMainAppOptions.getTapTreatTranslatorAsDedicated() || !webx.getWebx().getIsTranslator()))
								/*|| !((PlainWeb)webx.bookImpl).takeWord(popupKey)*/) {
								webx = null;
							}
							else if (((PlainWeb)webx.bookImpl).takeWord(popupKey)) {
								break;
							}
							webx = null;
						} else
						if (CCD != a.EmptyBook) {
							idx = CCD.bookImpl.lookUp(popupKey, true);
							if (idx < 0) {
								if (!reject_morph && use_morph) {
									keykey = a.ReRouteKey(popupKey, true);
									if (keykey != null)
										idx = CCD.bookImpl.lookUp(keykey, true);
									else
										reject_morph = true;
								}
							}
							if (idx >= 0)
								break;
						}
						CCD_ID++;
						cc++;
					}
			}
			
			if (webx != null) {
				webx.SetSearchKey(popupKey);
				idx = 0;
			}
			
			if(idx>=0 && CCD != a.EmptyBook  && task.get()&& taskVer == taskVersion.get()) {
				CMN.Log(CCD, "应用轮询结果", webx, idx);
				if(bForceJump && SearchMode==1)
					popupWebView.setTag(R.id.js_no_match, false);
				currentClickDictionary_currentPos = idx;
				harvest();
			}
			
			if (!PDICMainAppOptions.getHistoryStrategy0()
					&& PDICMainAppOptions.getHistoryStrategy7())
				a.insertUpdate_histroy(popupKey, 0, pageSlider);
		}
	}
	
	public void SearchDone() {
		if(rec!=null) {
			boolean bUseMergedUrl = true;
			//weblistHandler.setViewMode(WEB_VIEW_SINGLE, bUseMergedUrl);
			//weblistHandler.initMergedFrame(false, false, bUseMergedUrl);
			weblistHandler.bMergingFrames = true;
			weblistHandler.mMergedFrame = popupWebView;
			weblistHandler.bMergeFrames = 1;
			weblistHandler.bDataOnly = true;
			popupWebView.presenter = a.weblistHandler.getMergedFrame().presenter; //todo opt
			if(popupWebView.wvclient!=a.myWebClient) {
				popupWebView.setWebChromeClient(a.myWebCClient);
				popupWebView.setWebViewClient(a.myWebClient);
			}
			
			if(rec.size()>0) {
				rec.renderContentAt(0, a, null, weblistHandler);
			}
			
			return;
		}
		if (currentClickDictionary_currentPos >= 0 && CCD != a.EmptyBook) {
			if(CCD.getIsWebx()) {
				popupIndicator.setText(a.md_getName(CCD_ID));
				popupHistory.add(++popupHistoryVagranter, new myCpr<>(popupKey, new int[]{CCD_ID, currentClickDictionary_currentPos}));
				if (popupHistory.size() > popupHistoryVagranter + 1) {
					popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
				}
				popuphandler.setDict(CCD);
				if (PDICMainAppOptions.getClickSearchAutoReadEntry())
					popupWebView.bRequestedSoundPlayback=true;
				popupWebView.IBC = CCD.IBC;
				pageSlider.invalidateIBC();
				CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, popupWebView, currentClickDictionary_currentPos);
			} else {
//				StringBuilder mergedUrl = new StringBuilder("http://MdbR.com/merge.jsp?q=")
//						.append(SU.encode(popupKey)).append("&exp=");
//				mergedUrl.append("d");
//				IU.NumberToText_SIXTWO_LE(CCD.getId(), mergedUrl);
////			for (long val:displaying)
//				{
//					mergedUrl.append("_");
//					IU.NumberToText_SIXTWO_LE(currentClickDictionary_currentPos, mergedUrl);
//				}
//				popupWebView.presenter = a.weblistHandler.getMergedFrame().presenter;
//				if(popupWebView.wvclient!=a.myWebClient) {
//					popupWebView.setWebChromeClient(a.myWebCClient);
//					popupWebView.setWebViewClient(a.myWebClient);
//				}
//				popupWebView.loadUrl(mergedUrl.toString());
				
//				StringBuilder mergedUrl = new StringBuilder("http://MdbR.com/base/");
//				mergedUrl.append("d");
//				IU.NumberToText_SIXTWO_LE(CCD.getId(), mergedUrl);
//				mergedUrl.append("/entry/");
//				IU.NumberToText_SIXTWO_LE(currentClickDictionary_currentPos, mergedUrl);
//				popupWebView.presenter = a.weblistHandler.getMergedFrame().presenter;
//				if(popupWebView.wvclient!=a.myWebClient) {
//					popupWebView.setWebChromeClient(a.myWebCClient);
//					popupWebView.setWebViewClient(a.myWebClient);
//				}
//				popupWebView.loadUrl(mergedUrl.toString());
				
				StringBuilder mergedUrl = new StringBuilder("http://MdbR.com/content/");
				mergedUrl.append("d");
				IU.NumberToText_SIXTWO_LE(CCD.getId(), mergedUrl);
				mergedUrl.append("_");
				IU.NumberToText_SIXTWO_LE(currentClickDictionary_currentPos, mergedUrl);
				popupWebView.presenter = a.weblistHandler.getMergedFrame().presenter;
				if(popupWebView.wvclient!=a.myWebClient) {
					popupWebView.setWebChromeClient(a.myWebCClient);
					popupWebView.setWebViewClient(a.myWebClient);
				}
				popupWebView.loadUrl(mergedUrl.toString());
				
				
			}
		}
		
		currentClickDisplaying = popupKey;
		a.decorateContentviewByKey(popupStar, currentClickDisplaying);
	}
	
	public void popupWord(WebViewmy invoker, String key, BookPresenter forceStartId, int frameAt) {
		CMN.Log("popupWord_frameAt", frameAt, key, a.md.size(), WebViewmy.supressNxtClickTranslator);
		if(key==null || mdict.processText(key).length()>0) {
			if (invoker!=null) this.invoker = invoker;
			if (key!=null) popupKey = key;
			popupFrame = frameAt;
			popupForceId = forceStartId;
			a.root.removeCallbacks(this);
			a.root.post(this);
			//a.root.postDelayed(this, 75);
		}
	}
	
	public void PerformSearch(int mType, AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		if(mType==0)
			//SearchOne(task, taskVer, taskVersion);
			SearchMultiple(task, taskVer, taskVersion);
		else if(mType==1)
			SearchNxt(false, task, taskVer, taskVersion);
		else if(mType==2)
			SearchNxt(true, task, taskVer, taskVersion);
	}

//	public boolean dismiss() {
//		if(isVisible) {
//			ViewUtils.removeView(popupContentView);
//			ViewUtils.removeView(popupGuarder);
//			isVisible=false;
//			return true;
//		}
//		return false;
//	}
	
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		CMN.Log("onDismiss!!!");
		if(invoker!=null) {
			invoker.postDelayed(new Runnable() {
				@Override
				public void run() {
					invoker.evaluateJavascript("window.getSelection().collapseToStart()", null);
				}
			}, 180);
		}
	}
}

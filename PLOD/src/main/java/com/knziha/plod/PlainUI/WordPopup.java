package com.knziha.plod.PlainUI;

import static com.knziha.plod.PlainUI.WordPopupTask.*;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.Utils.IU;
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
import com.knziha.plod.settings.TapTranslator;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.BottomNavigationBehavior;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.LinearSplitView;
import com.knziha.plod.widgets.PageSlide;
import com.knziha.plod.widgets.PopupGuarder;
import com.knziha.plod.widgets.PopupMoveToucher;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.TwoColumnAdapter;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WordPopup extends PlainAppPanel implements Runnable{
	public WebViewListHandler weblistHandler;
	String popupKey;
	int popupFrame;
	BookPresenter popupForceId;
	public TextView entryTitle;
	protected PopupMoveToucher moveView;
	public FlowTextView indicator;
	public WebViewmy mWebView;
	public BookPresenter.AppHandler popuphandler;
	public ImageView popIvBack;
	public ViewGroup popupContentView;
	public ViewGroup toolbar;
	protected ViewGroup pottombar;
	protected CircleCheckBox popupChecker;
	public WeakReference<ViewGroup> popupCrdCloth;
	public WeakReference<ViewGroup> popupCmnCloth;
	
	public PopupGuarder popupGuarder;
	public String displaying;
	public int currentPos;
	public int[] currentClickDictionary_currentMergedPos;
	/** 用户选择的点译上游，从这里开始搜索 */
	public int upstrIdx;
	/** 搜索到的词典idx */
	public int CCD_ID;
	@NonNull public BookPresenter CCD;
	/** tmp */
	public BookPresenter sching;
	private WebViewmy invoker;
	private boolean isPreviewDirty;
	private final Runnable harvestRn = this::SearchDone;
	private final Runnable setAby = () -> setTranslator(sching, currentPos);
	private final Runnable setAby1 = () -> entryTitle.setText(displaying);
	private resultRecorderCombined rec;
	private int schMode;
	private ImageView modeBtn;
	
	DictPicker dictPicker;
	ViewGroup splitter;
	private final Runnable clrSelAby = () -> invoker.evaluateJavascript("window.getSelection().collapseToStart()", null);
	public SearchbarTools etTools;
	
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
		if(mWebView !=null){
			if(GlobalOptions.isDark){
				popupContentView.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				pottombar.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				popIvBack.setImageResource(R.drawable.abc_ic_ab_white_material);
			} else if(popIvBack.getTag()!=null){
				popupContentView.getBackground().clearColorFilter();
				pottombar.getBackground().clearColorFilter();
				popIvBack.setImageResource(R.drawable.abc_ic_ab_back_material_simple_compat);
			}
			if(indicator !=null) {
				entryTitle.setTextColor(GlobalOptions.isDark?a.AppBlack:Color.GRAY);
				indicator.setTextColor(GlobalOptions.isDark?a.AppBlack:0xff2b43c1);
			}
		}
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
	}
	
	@SuppressLint("ResourceType")
	@Override
	// click
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.cover: {
				if(v==weblistHandler.pageSlider.page){
					a.getUtk().setInvoker(CCD, mWebView, null, null);
					a.getUtk().onClick(v);
				}
			} break;
			case R.id.popupBackground: {
				dismissImmediate();
			} break;
			case R.id.popNxtE:
			case R.id.popLstE: {
				if(CCD==a.EmptyBook||CCD==null)
					CCD=a.currentDictionary;
				int delta = id==R.id.popNxtE?1:-1;
				resetPreviewIdx();
				if (weblistHandler.isMultiRecord()) {
					resultRecorderCombined rec = weblistHandler.multiRecord;
					int np = rec.viewingPos + delta;
					if (np>=0 && np<rec.size()) {
						rec.renderContentAt(currentPos=np, a, null, weblistHandler);
						setDisplaying(weblistHandler.getMultiRecordKey());
					}
				} else {
					loadEntry(id==R.id.popNxtE?1:-1);
				}
			} break;
			case R.id.popNxtDict:
			case R.id.popLstDict:{
				//SearchNxt(id==R.id.popNxtDict, task, taskVer, taskVersion);
				String url = mWebView.getUrl();
				int schemaIdx = url.indexOf(":");
				if(url.regionMatches(schemaIdx+3, "mdbr", 0, 4)){
					try {
						if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
							startTask(id==R.id.popNxtDict?TASK_POP_NAV_NXT:TASK_POP_NAV);
						}
						else if (url.regionMatches(schemaIdx+12, "merge", 0, 5)) {
							weblistHandler.bMergingFrames = true;
							weblistHandler.prvnxtFrame(id==R.id.popNxtDict);
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			} break;
			//返回
			case R.id.popIvBack:{
				dismissImmediate();
			} break;
			//返回
			case R.id.popIvRecess:{
				nav(true);
			} break;
			case R.id.popIvForward:{
				nav(false);
			} break;
			case R.id.popIvSettings:{
				a.launchSettings(TapTranslator.id, TapTranslator.requestCode);
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
				a.toggleStar(displaying, (ImageView) v, false, weblistHandler.pageSlider);
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
				dd.getWindow().findViewById(android.R.id.content).setAlpha(0.2f);
				refillPreviewEntries(dd, true);
			} break;
			case R.id.popupText2:{
				if(PDICMainAppOptions.getSwichClickSearchDictOnBottom()) {
					dictPicker.toggle();
				}
			} break;
			case R.id.gTrans:{
				MenuItemImpl mSTd = (MenuItemImpl) ViewUtils.findInMenu(a.AllMenusStamp, R.id.translate);
				mSTd.isLongClicked = false;
				a.onMenuItemClick(mSTd);
				weblistHandler.bMergingFrames=true;
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(mSTd.tag);
				if(dd!=null) dd.tag = this;
			} break;
			case R.id.max:{
				moveView.togMax();
			} break;
			case R.id.mode:{
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(v.getTag());
				if(dd==null) {
					dd = new AlertDialog.Builder(a)
						.setSingleChoiceLayout(R.layout.singlechoice_plain)
						.setSingleChoiceItems(new String[]{
								"词典内搜索"
								, "联合搜索"
						}, 0, (dialog, which) -> {
							opt.tapSchMode(schMode = which%2);
							modeBtn.setImageResource(schMode==0?R.drawable.ic_btn_siglemode:R.drawable.ic_btn_multimode);
							startTask(WordPopupTask.TASK_POP_SCH);
							dialog.dismiss();
						})
						.setTitle("切换搜索模式").create();
					v.setTag(new WeakReference<>(dd));
				}
				dd.show();
				dd.getWindow().setDimAmount(0);
			} break;
		}
	}
	
	private boolean pin() {
		return popupChecker==null?PDICMainAppOptions.getPinTapTranslator():popupChecker.isChecked();
	}
	
	public void show() {
		if (!isVisible()) {
			int type= pin()?0:2;
			toggle(lastTargetRoot, null, type);
			int pad = type==0?0: (int) (GlobalOptions.density * 19);
			if(settingsLayout.getPaddingTop()!=pad)settingsLayout.setPadding(0,pad,0,0);
			if(dictPicker.settingsLayout==null && dictPicker.pinShow()) {
				dictPicker.toggle();
			}
		}
	}
	
	final private void resetPreviewIdx() {
		previewPageIdx = 0;
		isPreviewDirty = true;
	}
	
	private void refillPreviewEntries(AlertDialog dialog, boolean delay) {
		if(isPreviewDirty)
		{
			int base = currentPos -1;
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
				else base= currentPos +3+(previewPageIdx-1)*6;
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
	
	public boolean nav(boolean isGoBack) {
		if (isGoBack && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		} else if (!isGoBack && mWebView.canGoForward()){
			mWebView.goForward();
			return true;
		}
		resetPreviewIdx();
		return false;
	}
	
	public void setTranslator(resultRecorderCombined res, int pos) {
		try {
			if (res.size()>0) {
				displaying=res.getResAt(a, pos).toString();
				entryTitle.setText(displaying);
				indicator.setText(a.getBookNameByIdNoCreation(res.getOneDictAt(pos)));
				texts[0] = 0;
			} else {
				displaying=res.schKey;
				entryTitle.setText(displaying);
				indicator.setText(null);
				//texts[0] = 0;
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public void setTranslator(BookPresenter ccd, int pos) {
		if (CCD!=ccd) {
			CCD=ccd;
			currentPos = pos;
			if(pos<0) pos=-1-pos;
			displaying=ccd.bookImpl.getEntryAt(pos);
			entryTitle.setText(displaying);
			//popupWebView.SelfIdx = CCD_ID = record.value[0];
			indicator.setText(ccd.getDictionaryName());
			popuphandler.setBook(ccd);
			dictPicker.dataChanged();
			dictPicker.scrollThis();
		} else if(currentPos!=pos) {
			currentPos = pos;
			if(pos<0) pos=-1-pos;
			displaying=ccd.bookImpl.getEntryAt(pos);
			entryTitle.setText(displaying);
		}
	}
	
	public void init() {
		if (mWebView == null) {
			WebViewListHandler weblist = this.weblistHandler/*faked*/ = new WebViewListHandler(a, a.contentUIData/*faked*/, SearchUI.TapSch.MAIN);
			popupContentView = (ViewGroup) a.getLayoutInflater()
					.inflate(R.layout.float_contentview_basic, a.root, false);
			popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
			toolbar = (ViewGroup) popupContentView.getChildAt(0);
			LinearSplitView split = (LinearSplitView) popupContentView.getChildAt(1);
			RLContainerSlider pageSlider = weblist.pageSlider = (RLContainerSlider) split.getChildAt(0);
			splitter = (ViewGroup) popupContentView.getChildAt(3);
			dictPicker = new DictPicker(a, split, splitter, -1);
			dictPicker.autoScroll = true;
			PageSlide page = pageSlider.page = (PageSlide) pageSlider.getChildAt(0);
			WebViewmy webview = (WebViewmy) pageSlider.getChildAt(1);;
			pageSlider.weblist = page.weblist = webview.weblistHandler = weblist;
			page.hdl = a.hdl;
			page.setPager(a.getPageListener());
			webview.getSettings().setTextZoom(118);
			webview.fromCombined = 2;
			pottombar = (ViewGroup) popupContentView.getChildAt(2);
			popuphandler = new BookPresenter.AppHandler(a.currentDictionary);
			webview.addJavascriptInterface(popuphandler, "app");
			webview.setBackgroundColor(a.AppWhite);
			((AdvancedNestScrollWebView)webview).setNestedScrollingEnabled(true);
			popIvBack = toolbar.findViewById(R.id.popIvBack);
			ViewUtils.setOnClickListenersOneDepth(toolbar, this, 999, null);
			ViewUtils.setOnClickListenersOneDepth(pottombar, this, 999, null);
			popupChecker = pottombar.findViewById(R.id.popChecker);
			popupChecker.setChecked(PDICMainAppOptions.getPinTapTranslator());
			entryTitle = toolbar.findViewById(R.id.popupText1);
			webview.pBc = new PhotoBrowsingContext();
			//webview.pBc.setDoubleTapZoomPage(true);
			//webview.pBc.setDoubleTapAlignment(4);
			indicator = pottombar.findViewById(R.id.popupText2);
			modeBtn = pottombar.findViewById(R.id.mode);
			modeBtn.setColorFilter(0xff666666);
			schMode = opt.tapSchMode();
			if(schMode==0)
				modeBtn.setImageResource(R.drawable.ic_btn_siglemode);
			webview.toolbar_title = new FlowTextView(indicator.getContext());
			webview.rl = popupContentView;
			popupContentView.setTag(webview);
			if(GlobalOptions.isDark) {
				entryTitle.setTextColor(Color.WHITE);
				indicator.setTextColor(Color.WHITE);
			}
			
			webview.setWebChromeClient(a.myWebCClient);
			webview.setWebViewClient(a.myWebClient);
			webview.setOnScrollChangedListener(a.getWebScrollChanged());
			
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
			moveView = new PopupMoveToucher(a, entryTitle);
			for (int i = 0; i < toolbar.getChildCount(); i++) {
				toolbar.getChildAt(i).setOnTouchListener(moveView);
			}
			for (int i = 0; i < pottombar.getChildCount(); i++) {
				pottombar.getChildAt(i).setOnTouchListener(moveView);
			}
			
			weblist.browserWidget8 = toolbar.findViewById(R.id.popIvStar);
			weblist.browserWidget10 = pottombar.findViewById(R.id.popLstE);
			weblist.browserWidget11 = pottombar.findViewById(R.id.popNxtE);
			
			weblist.mBar = pageSlider.findViewById(R.id.dragScrollBar);
			this.mWebView = weblist.dictView = weblist.mMergedFrame = webview;
			pageSlider.bar = weblist.mBar;
			// 缩放逻辑
			popupGuarder.setOnTouchListener(moveView);
			popupGuarder.setClickable(true);
			pageSlider.setWebview(webview, null);
			
			weblist.bDataOnly = true;
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
		
		entryTitle.setText(popupKey);
		
		//SearchOne(task, taskVer, taskVersion);
		
		startTask(TASK_POP_SCH);
	}
	
	public void startTask(int type) {
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
		isInit = isNewHolder = mWebView == null|| mWebView.fromCombined!=2;
		init();
		// 给你换身衣裳
		WeakReference<ViewGroup> holder = (PDICMainAppOptions.getImmersiveClickSearch() ? popupCrdCloth : popupCmnCloth);
		ViewGroup mPopupContentView = popupContentView;
		popupContentView = holder == null ? null : holder.get();
		boolean b1 = popupContentView == null;
		isNewHolder = isNewHolder || b1;
		View cv = (View) weblistHandler.pageSlider.getParent();
		if (b1 || popupContentView != cv.getParent()) {
			//ViewUtils.removeView(popupToolbar);
			//ViewUtils.removeView(PopupPageSlider);
			//ViewUtils.removeView(popupBottombar);
			ViewUtils.removeView(mPopupContentView);
			ViewUtils.removeView(toolbar);
			ViewUtils.removeView(cv);
			ViewUtils.removeView(pottombar);
			if (PDICMainAppOptions.getImmersiveClickSearch()) {
				popupContentView = (popupCrdCloth != null && popupCrdCloth.get() != null) ? popupCrdCloth.get()
						: (popupCrdCloth = new WeakReference<>((ViewGroup) a.getLayoutInflater()
						.inflate(R.layout.float_contentview_coord, a.root, false))).get();
				ViewGroup appbar = (ViewGroup) popupContentView.findViewById(R.id.appbar);
				ViewUtils.addViewToParent(toolbar, appbar);
				ViewUtils.addViewToParent(cv, popupContentView);
				ViewUtils.addViewToParent(pottombar, popupContentView);
				((CoordinatorLayout.LayoutParams) pottombar.getLayoutParams()).gravity = Gravity.BOTTOM;
				((CoordinatorLayout.LayoutParams) pottombar.getLayoutParams()).setBehavior(new BottomNavigationBehavior(popupContentView.getContext(), null));
				CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) cv.getLayoutParams();
				lp.setBehavior(new AppBarLayout.ScrollingViewBehavior(popupContentView.getContext(), null));
				lp.height = CoordinatorLayout.LayoutParams.MATCH_PARENT;
				lp.topMargin = 0;
				lp.bottomMargin = 0;
				((AppBarLayout.LayoutParams) toolbar.getLayoutParams()).setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
			} else {
				popupContentView = (popupCmnCloth != null && popupCmnCloth.get() != null) ? popupCmnCloth.get()
						: (popupCmnCloth = new WeakReference<>((ViewGroup) a.getLayoutInflater()
						.inflate(R.layout.float_contentview_basic_outer, a.root, false))).get();
				ViewUtils.addViewToParent(toolbar, popupContentView);
				ViewUtils.addViewToParent(cv, popupContentView);
				ViewUtils.addViewToParent(pottombar, popupContentView);
				toolbar.setTranslationY(0);
				cv.setTranslationY(0);
				pottombar.setTranslationY(0);
				((FrameLayout.LayoutParams) cv.getLayoutParams()).topMargin = (int) (45*GlobalOptions.density);
				((FrameLayout.LayoutParams) cv.getLayoutParams()).bottomMargin = (int) (30*GlobalOptions.density);
				((FrameLayout.LayoutParams) pottombar.getLayoutParams()).gravity = Gravity.BOTTOM;
			}
			mWebView.rl = popupContentView;
			popupContentView.setTag(mWebView);
		}
		popupGuarder.popupToGuard = popupContentView;
		popupGuarder.isPinned = pin();
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
			mWebView.fromCombined = 2;
			refresh();
			popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
			FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) popupContentView.getLayoutParams());
			lp.height = moveView.FVH_UNDOCKED = (int) (a.dm.heightPixels * 7.0 / 12 - a.getResources().getDimension(R.dimen._20_));
			if (mPopupContentView != null && !isInit) {
				popupContentView.setTranslationY(mPopupContentView.getTranslationY());
				lp.height = mPopupContentView.getLayoutParams().height;
			}
		}
	}
	
	ViewGroup lastTargetRoot;
	
	private void AttachViews() {
		// 初次添加请指明方位
		if (!pin() && !isVisible()) {
			ViewGroup targetRoot = lastTargetRoot;
			if (moveView.FVDOCKED && moveView.Maximized && PDICMainAppOptions.getResetMaxClickSearch()) {
				moveView.Dedock();
			}
			CMN.Log("poping up ::: ", a.ActivedAdapter);
			if (popupKey!=null && (PDICMainAppOptions.getResetPosClickSearch() || isInit) && !moveView.FVDOCKED) {
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
						ty = now - moveView.FVH_UNDOCKED - pad;
					}
				}
				//CMN.Log("min", getVisibleHeight()-popupMoveToucher.FVH_UNDOCKED-((ViewGroup.MarginLayoutParams)popupContentView.getLayoutParams()).topMargin*2);
				popupContentView.setTranslationY(Math.min(a.getVisibleHeight() - moveView.FVH_UNDOCKED - ((ViewGroup.MarginLayoutParams) popupContentView.getLayoutParams()).topMargin * 2, Math.max(0, ty)));
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
		String key = false?ViewUtils.getTextInView(entryTitle).trim():popupKey;
		CMN.Log("SearchNxt::", key);
		if(key.length()>0) {
			ArrayList<PlaceHolder> ph = a.getPlaceHolders();
			String keykey;
			boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
			int SearchMode = PDICMainAppOptions.getClickSearchMode();
			boolean hasDedicatedSeachGroup = SearchMode==1&&a.bHasDedicatedSeachGroup;
			boolean reject_morph = false;
			//轮询开始
			int CCD_ID = this.CCD_ID;
			BookPresenter CCD = this.CCD;
			while(true){
				if(nxt) {
					CCD_ID++;
				} else {
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
				
				if(idx>=0 || hasDedicatedSeachGroup && CCD!=a.EmptyBook ||  !PDICMainAppOptions.getSkipClickSearch()) {
					//CMN.Log("break::", idx, CCD.getDictionaryName(), CCD.bookImpl.getEntryAt(idx));
					break;
				}
			}
			
			//应用轮询结果
			if(this.CCD_ID!=CCD_ID && CCD!=a.EmptyBook && task.get() && taskVer == taskVersion.get()){
				if(PDICMainAppOptions.getSwichClickSearchDictOnNav()){
					upstrIdx = CCD_ID;
				}
				if(idx<0 && hasDedicatedSeachGroup){
					idx = -1-idx;
					mWebView.setTag(R.id.js_no_match, false);
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
				this.CCD_ID=CCD_ID;
				sching=CCD;
				currentPos = idx;
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
		a.root.removeCallbacks(harvestRn);
		a.root.post(harvestRn);
	}
	
	@AnyThread
	private void SearchOne(AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		int size = a.md.size();
		int idx = -1, cc = 0;
		resetPreviewIdx();
		if (popupKey != null) {
			String keykey;
			CCD_ID = upstrIdx = Math.min(upstrIdx, size-1);
			if(popupForceId!=null) {
				CCD = popupForceId;
				CCD_ID = a.md.indexOf(popupForceId);
				if(CCD_ID<0) {
					CCD_ID = a.md.size();
					a.md.add(popupForceId); // todo check???
				}
			}
			//轮询开始
			BookPresenter webx = null;
			boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
			int SearchMode = PDICMainAppOptions.getClickSearchMode();
			CMN.Log("SearchMode", SearchMode);
			boolean bForceJump = false;
			BookPresenter CCD = this.CCD;
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
			
			if(idx>=0 && CCD != a.EmptyBook  && task.get() && taskVer == taskVersion.get()) {
				CMN.Log(CCD, "应用轮询结果", webx, idx);
				if(bForceJump && SearchMode==1)
					mWebView.setTag(R.id.js_no_match, false);
				currentPos = idx;
				this.rec = null;
				sching = CCD;
				harvest();
			}
			
			if (!PDICMainAppOptions.storeNothing()
					&& PDICMainAppOptions.getHistoryStrategy7())
				a.addHistory(popupKey, SearchUI.TapSch.MAIN, weblistHandler.pageSlider, null);
		}
	}
	
	public void SearchDone() {
		if(rec!=null) {
			boolean bUseMergedUrl = true;
			weblistHandler.setViewMode(rec, bUseMergedUrl, mWebView);
			//weblistHandler.initMergedFrame(false, false, bUseMergedUrl);;
			weblistHandler.bMergingFrames = true;
			weblistHandler.bMergeFrames = 1;
			mWebView.presenter = a.weblistHandler.getMergedBook(); //todo opt
			if(mWebView.wvclient!=a.myWebClient) {
				mWebView.setWebChromeClient(a.myWebCClient);
				mWebView.setWebViewClient(a.myWebClient);
			}
			if(rec.size()>0) {
				rec.renderContentAt(0, a, null, weblistHandler);
				setDisplaying(weblistHandler.getMultiRecordKey());
			}
			weblistHandler.pageSlider.setWebview(mWebView, null);
			dictPicker.filterByRec(rec, 0);
			setTranslator(rec, 0);
			return;
		}
		dictPicker.filterByRec(null, 0);
		if(sching!=null) {
			texts[0]=CMN.id(sching);
			setTranslator(sching, currentPos);
			sching = null;
		}
		if (currentPos >= 0 && CCD != a.EmptyBook) {
			weblistHandler.setViewMode(null, false, mWebView);
			if(CCD.getIsWebx()) {
				weblistHandler.bMergingFrames = false;
				indicator.setText(a.md_getName(CCD_ID));
				popuphandler.setBook(CCD);
				if (PDICMainAppOptions.getClickSearchAutoReadEntry())
					mWebView.bRequestedSoundPlayback=true;
				CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, mWebView, currentPos);
				weblistHandler.pageSlider.setWebview(mWebView, null);
				setDisplaying(mWebView.word);
			} else {
				loadEntry(0);
			}
		}
	}
	
	private void setDisplaying(String key) {
		displaying = key;
		weblistHandler.setStar(key);
	}
	
	private void loadEntry(int d) {
		if (d!=0) {
			currentPos=Math.max(0, Math.min(currentPos+d, (int) CCD.bookImpl.getNumberEntries()));
		}
		weblistHandler.bMergingFrames = true;
		StringBuilder mergedUrl = new StringBuilder("http://mdbr.com/content/");
		mergedUrl.append("d");
		IU.NumberToText_SIXTWO_LE(CCD.getId(), mergedUrl);
		mergedUrl.append("_");
		IU.NumberToText_SIXTWO_LE(currentPos, mergedUrl);
		mWebView.currentPos = currentPos;
		mWebView.presenter = CCD;
		mWebView.loadUrl(mergedUrl.toString());
		weblistHandler.resetScrollbar(mWebView, false, false);
		setDisplaying(mWebView.word=CCD.getBookEntryAt(currentPos));
	}
	
	public void popupWord(WebViewmy invoker, String key, BookPresenter forceStartId, int frameAt) {
		CMN.Log("popupWord_frameAt", frameAt, key, a.md.size(), invoker==null, WebViewmy.supressNxtClickTranslator);
		if(key==null || mdict.processText(key).length()>0) {
			if (invoker!=null) this.invoker = invoker;
			if (key!=null) popupKey = key;
			popupFrame = frameAt;
			popupForceId = forceStartId;
			a.root.removeCallbacks(this);
			if (invoker!=null && invoker.weblistHandler.contentUIData.PageSlider.tapZoom) {
				a.root.postDelayed(this, SearchUI.tapZoomWait); // 支持双击操作会拖慢点译！
			} else {
				a.root.post(this);
			}
		}
	}
	
	public void PerformSearch(int mType, AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		if(mType==TASK_POP_SCH)
			if(schMode==0) SearchOne(task, taskVer, taskVersion);
			else SearchMultiple(task, taskVer, taskVersion);
		else if(mType==TASK_POP_NAV)
			SearchNxt(false, task, taskVer, taskVersion);
		else if(mType==TASK_POP_NAV_NXT)
			SearchNxt(true, task, taskVer, taskVersion);
		else if(mType==TASK_LOAD_HISTORY && etTools!=null) {
			etTools.LoadHistory(task);
			etTools=null;
		}
		else if(mType==TASK_FYE_SCH) {
			a.peruseView.SearchAll(a, task);
		}
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		if(invoker!=null) {
			invoker.postDelayed(clrSelAby, 180);
		}
	}
	
	public void valid(BookPresenter ccd, long pos) {
		int id=CMN.id(ccd);
		if (texts[0]!=id) {
			sching = ccd;
			CCD_ID = a.md.indexOf(ccd); //todo opt
			currentPos = (int) pos;
			texts[0]=id;
			a.root.post(setAby);
		} else {
			currentPos = (int) pos;
		}
	}
	
	public void valid(String text) {
		if (!TextUtils.equals(text, displaying)) {
			displaying = text;
			indicator.setText(null);
			texts[0] = 0;
			a.root.post(setAby1);
		}
	}
	
	int[] texts = new int[2];
	
	public void onPageStart(String url) {
		int schemaIdx = url.indexOf(":");
		//CMN.debug("wordPopup::onPageStarted::", url, url.regionMatches(schemaIdx+3, "mdbr", 0, 4) , url.regionMatches(schemaIdx+12, "content", 0, 7));
		if(url.regionMatches(schemaIdx+3, "mdbr", 0, 4)){
			try {
				if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
					String[] arr = url.substring(24).split("_");
					valid(a.getMdictServer().md_getByURL(arr[0]),IU.TextToNumber_SIXTWO_LE(arr[1]));
				}
				else if (url.regionMatches(schemaIdx+12, "merge", 0, 5)) {
					valid(URLDecoder.decode(url.substring(schemaIdx+24, url.indexOf("&", schemaIdx+24)), "utf8"));
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
	
	public void set() {
		if(PDICMainAppOptions.getImmersiveClickSearch()!=PDICMainAppOptions.getImmersiveClickSearch(a.TFStamp))
			a.popupWord(null,null, 0, null);
		if (mWebView!=null) {
			a.weblist = weblistHandler;
			a.showScrollSet();
		}
	}
	
	public void resetScrollbar() {
		String url = mWebView.getUrl();
		int schemaIdx = url.indexOf(":");
		if(url.regionMatches(schemaIdx+3, "mdbr", 0, 4)){
			try {
				if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
					weblistHandler.resetScrollbar(mWebView, false, false);
				}
				else if (url.regionMatches(schemaIdx+12, "merge", 0, 5)) {
					weblistHandler.bMergingFrames = true;
					weblistHandler.resetScrollbar(mWebView, true, true);
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
}

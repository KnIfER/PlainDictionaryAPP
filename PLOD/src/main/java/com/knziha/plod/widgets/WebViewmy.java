package com.knziha.plod.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.GlobalOptions;

import com.google.android.material.math.MathUtils;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PeruseView;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.rbtree.additiveMyCpr1;

import org.knziha.metaline.Metaline;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class WebViewmy extends WebView implements MenuItem.OnMenuItemClickListener {
	public long currentPos;
	public int frameAt;
	public String toTag;
	//public int SelfIdx;
	/** 标记视图来源。 0=单本搜索; 1=联合搜索; 2=点译模式; 3=翻阅模式。*/
	public int fromCombined;
	//public boolean fromPeruseview;
	public final boolean fromNet(){ return presenter.isWebx; };
	
	public PeruseView peruseView;
	
	public String word;
	public long[] currentRendring;
	public boolean awaiting;
	public boolean bRequestedSoundPlayback;
	public float webScale=1;
	public int expectedPos=-1;
	public int expectedPosX=-1;
	public float lastX;
	public float lastY;
	public int lastLongSX;
	public int lastLongSY;
	public float lastLongScale;
	public float lastLongX;
	public float lastLongY;
	public static Integer ShareString_Id;
	public static Integer SelectString_Id;
	public static Integer CopyString_Id;
	
	public PhotoBrowsingContext pBc;
	
	public GradientDrawable toolbarBG;
	public AdvancedNestScrollLinerView titleBar;
	public final int[] ColorShade = new int[]{0xff4F7FDF, 0xff2b4381};
	public FlowTextView toolbar_title;
	public View recess;
	public View forward;
	public View rl;
	public int AlwaysCheckRange;
	public boolean forbidLoading;
	public boolean active;
	public boolean bPageStarted;
	public boolean bShouldOverridePageResource;
	public additiveMyCpr1 jointResult;
	public int translating = -1;
	public WebViewListHandler weblistHandler;
	public WebViewListHandler.HighlightVagranter hDataPage = new WebViewListHandler.HighlightVagranter();
	public boolean hasFilesTag;
	public int changed;
	public float expectedZoom;
	private int mForegroundColor = 0xffffffff;
	private PorterDuffColorFilter ForegroundFilter;
	
	public long lastSuppressLnkTm;
	
	@SuppressLint("StaticFieldLeak")
	private static BookPresenter EmptyBook;
	static {
		try {
			EmptyBook = new BookPresenter(new File("empty"), null, 1);
		} catch (IOException ignored) { }
	}
	public BookPresenter presenter = EmptyBook;
	
	public boolean drawRect;
	public float highRigkt_X;
	public float highRigkt_Y;
	public float highRigkt_R;
	public float highRigkt_B;
	public static boolean supressNxtClickTranslator;
	
	private final SIDProvider mSimpleId = new SIDProvider(CMN.id(this));
	
	View scrollRect;
	ScrollAbility mScrollAbility;
	
	boolean scrolling;
	int speed = 5;
	int interval = 16;
	
	public WebViewmy(Context context) {
		this(context, null);
	}

	public WebViewmy(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	WidgetsLayout widgetsLayout;
	boolean hasWidgets;
	
	public WebViewmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//setBackgroundColor(Color.parseColor("#C7EDCC"));
		//setBackgroundColor(0);
		//setVerticalScrollBarEnabled(true);
		//setHorizontalScrollBarEnabled(true);
		//setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		//setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		
		//网页设置初始化
		final WebSettings settings = getSettings();
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		//	settings.setSafeBrowsingEnabled(false);
		//}
		
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		//settings.setDefaultTextEncodingName("UTF-8");
		
		//settings.setNeedInitialFocus(false);
		
		//settings.setDefaultFontSize(40);
		//settings.setTextZoom(100);
		//setInitialScale(25);
		
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);
		settings.setMediaPlaybackRequiresUserGesture(false);

		// todo enhance safety
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		
		settings.setAllowFileAccess(true);
		
		settings.setSupportMultipleWindows(true);

		
//		settings.setUseWideViewPort(true);//设定支持viewport
//		settings.setLoadWithOverviewMode(true);
//		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//
		settings.setAllowUniversalAccessFromFileURLs(true);
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		//settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //设置 缓存模式
		
		//setLayerType(View.LAYER_TYPE_HARDWARE, null);
		webScale=GlobalOptions.density;
		
		
		removeJavascriptInterface("searchBoxJavaBridge_");
		removeJavascriptInterface("accessibility");
		removeJavascriptInterface("accessibilityTraversal");
		
		addJavascriptInterface(mSimpleId, "sid");
		
	}
	
	public int simpleId() {
		return mSimpleId.get();
	}
	
	public int getContentHeight(){
		return computeVerticalScrollRange();
	}
	
	public int getContentWidth(){
		return computeHorizontalScrollRange();
	}
	
	public int getContentOffset(){
		return this.computeVerticalScrollOffset();
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOnScrollChangeListener !=null)
			mOnScrollChangeListener.onScrollChange(this,l,t,oldl,oldt);
	}
	public void setOnScrollChangedListener(OnScrollChangedListener onSrollChangedListener) {
		mOnScrollChangeListener =onSrollChangedListener;
	}
	OnScrollChangedListener mOnScrollChangeListener;

	public boolean isloading=false;
	@Override
	public void loadDataWithBaseURL(String baseUrl,String data,String mimeType,String encoding,String historyUrl) {
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		//if(!baseUrl.equals("about:blank"))
		//CMN.Log("loadDataWithBaseURL...");
		drawRect=false;
		isloading=true;
	}
	
//	@Override
//	public void onResume() {
//		super.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		CMN.Log("onPauseonPauseonPause");
//		super.onPause();
//	}
	
//	@Override
//	protected void onSizeChanged(int w, int h, int ow, int oh) {
//		super.onSizeChanged(w, h, ow, oh);
//		//CMN.Log("onSizeChanged  ");
//	}

//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		//CMN.Log("onMeasure  ");
//	}

	@Override
	public void loadUrl(String url) {
		CMN.debug("\n\nloadUrl::", url);
		super.loadUrl(url);
		drawRect=false;
		isloading=true;
		recUrl(url);
	}
	
	public String url;
	public boolean mdbr;
	public boolean merge;
	public ArrayList<BookPresenter> frames = new ArrayList<>();
	public ArrayList<BookPresenter> webx_frames = new ArrayList<>();
	
	public final void recUrl(String url) {
		if (url!=null && !url.equals(this.url)) {
			this.url = url;
			int schemaIdx = url.indexOf(":");
			mdbr = url.regionMatches(schemaIdx+3, "mdbr", 0, 4);
			merge = mdbr && url.regionMatches(schemaIdx + 12, "merge", 0, 5);
			if (mdbr && merge) {
				long bid;
				StringTokenizer tokens = new StringTokenizer(url, "-");
				boolean first = true;
				frames.clear();
				webx_frames.clear();
				MainActivityUIBase a = presenter.a;
				if(a!=null)
				while (tokens.hasMoreTokens()) {
					String tk = tokens.nextToken();
					if (first) {
						int idx = tk.indexOf("&exp=");
						if (idx > 0) {
							tk = tk.substring(idx + 5);
							first = false;
						} else {
							continue;
						}
					}
					char fc = tk.length() == 0 ? 0 : tk.charAt(0);
					if (fc=='d' || fc=='w') {
						int ed1 = tk.indexOf("_"); if(ed1<0) ed1=Integer.MAX_VALUE;
						int ed2 = tk.indexOf("&"); if(ed2<0) ed2=Integer.MAX_VALUE;
						int ed = Math.min(ed1, ed2);
						if(ed<0) ed=tk.length();
						tk = tk.substring(1, ed);
						bid = IU.TextToNumber_SIXTWO_LE(tk);
						BookPresenter book = a.getBookByIdNoCreation(bid);
						if (book!=a.EmptyBook) {
							frames.add(book);
							if (fc=='w') {
								webx_frames.add(book);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void setWebViewClient(WebViewClient client){
		super.setWebViewClient(wvclient=client);
	}
	public WebViewClient wvclient;
	protected boolean MyMenuinversed;

	@Override
	protected void onCreateContextMenu(ContextMenu menu){
		//Toast.makeText(getContext(), "ONCCM", 0).show();
		CMN.Log("webview onCreateContextMenu");
		super.onCreateContextMenu(menu);
	}

	public boolean bIsActionMenuShown;
	public callbackme callmeback;

	public void onFinishedPage() {
		if(wvclient!=null)
			wvclient.onPageFinished(this, "file:///");
	}

	public void shutDown() {
		bPageStarted = false;
		setWebChromeClient(null);
		setWebViewClient(null);
		//todo 空指针
		bPageStarted = false;
		setOnScrollChangedListener(null);
		setOnTouchListener(null);
		setOnLongClickListener(null);
		removeAllViews();
		if(getParent() instanceof ViewGroup)
			((ViewGroup) getParent()).removeView(this);
		stopLoading();
		getSettings().setJavaScriptEnabled(false);
		clearHistory();
		destroy();
	}
	
	public void SafeScrollTo(int x, int y) {
		OnScrollChangedListener mScrollChanged = mOnScrollChangeListener;
		mOnScrollChangeListener =null;
		scrollTo(x, y);
		mOnScrollChangeListener =mScrollChanged;
	}
	
	public void setTitlebarForegroundColor(int foregroundColor) {
		if(mForegroundColor!=foregroundColor){
			LinkedList<ViewGroup> linkedList = new LinkedList<>();
			linkedList.add(titleBar);
			View cI;
			ForegroundFilter = new PorterDuffColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN);
			while (!linkedList.isEmpty()) {
				ViewGroup current = linkedList.removeFirst();
				for (int i = 0; i < current.getChildCount(); i++) {
					cI = current.getChildAt(i);
					if (cI instanceof ViewGroup) {
						linkedList.addLast((ViewGroup) current.getChildAt(i));
					} else {
						if(cI instanceof ImageView){
							if(cI.getBackground() instanceof BitmapDrawable){
								cI.getBackground().mutate().setColorFilter(ForegroundFilter);
							} else {
								((ImageView)cI).setColorFilter(ForegroundFilter);
							}
						} else if(cI instanceof TextView){
							((TextView)cI).setTextColor(foregroundColor);
						} else if(cI instanceof FlowTextView){
							((FlowTextView)cI).setTextColor(foregroundColor);
						}
					}
				}
			}
			mForegroundColor = foregroundColor;
		}
	}
	
	public void highRigkt_set(float x, float y, float r, float b) {
		float pad = 2*GlobalOptions.density;
		highRigkt_X = x-pad;
		highRigkt_Y = y-pad;
		highRigkt_R = r+pad;
		highRigkt_B = b+pad;
		if (Build.VERSION.SDK_INT<21) { //todo webview 版本
			int sX = getScrollX();
			int sY = getScrollY();
			highRigkt_X += sX;
			highRigkt_R += sX;
			highRigkt_Y += sY;
			highRigkt_B += sY;
		}
		drawRect = true;
		postInvalidate();
	}
	
	public void FindBGInTitle(MainActivityUIBase a, ViewGroup toolbar_web) {
		//toolbar_web.setBackground(toolbar_web.getBackground().mutate()); // not work, tint chaos
		Drawable bg = a.titleDrawable();
		toolbar_web.setBackground(bg);
		toolbar_web.setPadding(0,0,0,0);
		toolbarBG = (GradientDrawable) ((LayerDrawable)bg).getDrawable(0);
	}
	
	//public GradientDrawable MutateBGInTitle() {
	//	LayerDrawable d;
	//	d = (LayerDrawable) titleBar.getBackground().mutate();
	//	toolbarBG = (GradientDrawable) d.getDrawable(0);
	//	return toolbarBG;
	//}
	
	public void CheckAlwaysCheckRange() {
		AlwaysCheckRange = computeHorizontalScrollRange() > getWidth()?1:-1;
	}
	
	/** 模拟触摸，暂时关闭 contextmenu */
	public void simulateScrollEffect() {
		final long now = System.currentTimeMillis();
		final MotionEvent motion = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN,
				0.0f, 0.0f, 0);
		dispatchTouchEvent(motion);
		MainActivityUIBase.CustomViewHideTime = now;
		motion.setAction(MotionEvent.ACTION_MOVE);
		motion.setLocation(100, 0);
		dispatchTouchEvent(motion);
		motion.recycle();
	}
	
	/** 恢复 contextmenu */
	public void stopScrollEffect() {
		final MotionEvent motion = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP,
				0.0f, 0.0f, 0);
		dispatchTouchEvent(motion);
		motion.recycle();
	}
	
	public boolean shouldStorePagePos(ScrollerRecord pos) {
		return pos==null && (getScrollY() != 0 || getScrollX() != 0 || webScale != BookPresenter.def_zoom)
				|| pos!=null && (webScale != BookPresenter.def_zoom || getScrollY() != pos.y || getScrollX() != pos.x);
	}
	
	public boolean shouldStoreNewPagePos(ScrollerRecord pos) {
		return pos==null && (getScrollY() != 0 || getScrollX() != 0 || webScale != BookPresenter.def_zoom);
	}
	
	public ScrollerRecord storePagePos(ScrollerRecord pos) {
		ScrollerRecord ret=null;
		if(pos==null && (getScrollY() != 0 || getScrollX() != 0 || webScale != BookPresenter.def_zoom)) {
			ret = pos = new ScrollerRecord();
		}
		if(pos!=null) {
			pos.set(getScrollX(), getScrollY(), webScale);
		}
		return ret;
	}
	
//	@NonNull
//	@Override
//	public TextClassifier getTextClassifier() {
//		if (true) {
//			return UrlFucker;
//		}
//		return super.getTextClassifier();
//	}
//
//	public final static TextClassifier UrlFucker;
//
//	static {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//			UrlFucker = new TextClassifier(){
//				@RequiresApi(api = Build.VERSION_CODES.P)
//				@NonNull
//				@Override
//				public TextSelection suggestSelection(@NonNull TextSelection.Request request) {
//					return new TextSelection.Builder(request.getStartIndex(), request.getEndIndex()).build();
//				}
//			};
//		} else {
//			UrlFucker =null;
//		}
//	}
	
	public long getBookId() {
		return presenter.bookImpl.getBooKID();
	}
	
	public Runnable postFinishedAbility = () -> wvclient.onPageFinished(this, getUrl());
	
	boolean bPostedFinish;
	
	public void postFinished() {
		if (!bPostedFinish) {
			postDelayed(postFinishedAbility, 750);
			bPostedFinish = true;
		}
	}
	
	public void removePostFinished() {
		if (bPostedFinish) {
			removeCallbacks(postFinishedAbility);
			bPostedFinish = false;
		}
	}
	
	public int getBackgroundColor() {
		return mBackground;
	}
	int mBackground;
	@Override
	public void setBackgroundColor(int color) {
		mBackground = color;
		super.setBackgroundColor(color);
	}
	
	public void setPresenter(BookPresenter book) {
		if(presenter!=book) {
//			if (BuildConfig.DEBUG) {
//				try {
//					throw new RuntimeException();
//				} catch (RuntimeException e) {
//					CMN.debug("setPresenter::", e);
//				}
//			}
			if (weblistHandler.bShowingInPopup) {
				if (presenter.isWebx) {
					try {
						String reset = presenter.getWebx().getField("webSetttingsReset");
						if (reset!=null) {
							ViewUtils.execSimple(reset, null, getSettings());
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
				if (book.isWebx) {
					try {
						String reset = book.getWebx().getField("webSetttingsReset");
						if (reset!=null) {
							String set = book.getWebx().getField("webSetttings");
							if(set!=null) ViewUtils.execSimple(set, null, getSettings());
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			}
			presenter=book;
			pBc = book.IBC;
		}
	}
	
	/** displaying entry from the same dictionary */
	public boolean isViewSingle() {
		String url;
		return weblistHandler.isViewSingle()
				&& (jointResult==null
					|| (url = getUrl())!=null && !url.startsWith("merge", url.indexOf(":")+12));
	}
	
	public final void initPos() {
		ScrollerRecord pPos = presenter.avoyager.get((int) currentPos);
		float initialScale = BookPresenter.def_zoom;
		if (pPos != null) {
			expectedPos = pPos.y;
			expectedPosX = pPos.x;
			initialScale = pPos.scale;
		} else {
			expectedPos = 0;
			expectedPosX = 0;
		}
		expectedZoom = initialScale;
	}
	
	public final void initScale() {
		// CMN.debug("initScale::缩放是", expectedZoom);
		if(expectedZoom!=-1)
		{
			setInitialScale((int) (100*(expectedZoom/ BookPresenter.def_zoom)*GlobalOptions.density));//opt.dm.density
			expectedZoom = -1;
		} else {
			//尝试重置页面缩放
			setInitialScale(0);//opt.dm.density
		}
	}

//	/**  reset overshot */
//	public void calcScroll() {
//		scrollTo(computeHorizontalScrollOffset(), computeVerticalScrollOffset());
//	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	private class callbackme extends ActionMode.Callback2 implements OnLongClickListener {
		ActionMode.Callback callback;
		public callbackme callhere(ActionMode.Callback callher) {
			if(callher!=null)
				callback=callher;
			return this;
		}
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			boolean craft = callback.onCreateActionMode(mode, menu);
			if (craft) {
				weblistHandler.textMenu(WebViewmy.this);
			}
			return bIsActionMenuShown=craft;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return callback.onPrepareActionMode(mode, menu);
		}

		@Override
		public boolean onLongClick(View v) {
			switch(v.getId()) {
				case R.id.toolbar_action0: {
					//PopupDecorView s;
				}
				break;
			}
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return WebViewmy.this.onMenuItemClick(mode, item);
		}


		PopupWindow mPopup;
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			weblistHandler.textMenu(null);
			bIsActionMenuShown=false;
			//CMN.Log("onDestroyActionMode");
		}

		@Override
		public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
			if(ActionMode.Callback2.class.isInstance(callback))
				((ActionMode.Callback2)callback).onGetContentRect(mode, view, outRect);
			else
				super.onGetContentRect(mode, view, outRect);
			//CMN.Log("onGetContentRect", (view==WebViewmy.this));
		}
	}

	private boolean onMenuItemClick(ActionMode mode, MenuItem item) {
		//CMN.Log("onMenuItemClick", item.getClass(), item.getTitle(), item.getItemId(), android.R.id.copy);
		int id = item.getItemId();
		switch(id) {
			case R.id.toolbar_action0:{
				if (getContext() instanceof MainActivityUIBase) {
					((MainActivityUIBase) getContext()).Annot(this, R.string.highlight);
				} else {
					evaluateJavascript(getHighLightIncantation(false).toString(), value -> invalidate());
				}
				MyMenuinversed=!MyMenuinversed;
			} return true;
			case R.id.toolbar_action1:{//工具复用，我真厉害啊啊啊啊！
				//evaluateJavascript("document.execCommand('selectAll'); console.log('dsadsa')",null);
				//From normal, from history, from peruse view, [from popup window]
				/**
				 * 收藏选中文本
				 * 全选   | 选择标注颜色
				 * 高亮   | 清除高亮
				 * 下划线 | 清除下划线
				 * 分享#1 | 分享…
				 * 分享#2 | 分享#3
				 */
				//CMN.Log("工具!!!");
				presenter.invokeToolsBtn(this, -1);
			} return false;
			case R.id.toolbar_action3:{//TTS
				evaluateJavascript("if(window.app)app.ReadText(sid.get(), ''+window.getSelection())",null);
			} return false;
		}
		if (mode!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean ret = callmeback.callback.onActionItemClicked(mode, item);
			if(id == 50856071 || id == android.R.id.copy || getCopyText().equals(item.getTitle())){
				clearFocus();
				ret=true;
			}
			return ret;
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return onMenuItemClick(null, item);
	}
//
//	@Override
//	public boolean postDelayed(Runnable action, long delayMillis) {
//		CMN.Log("postDelayed", action, delayMillis);
//		return super.postDelayed(action, delayMillis);
//	}
//	@Override
//	public boolean post(Runnable action) {
//		CMN.Log("post", action);
//		return super.post(action);
//	}
	
	public ActionMode dummyActionMode() {
		return new ActionMode() {
			@Override public void setTitle(CharSequence title) {}
			@Override public void setTitle(int resId) {}
			@Override public void setSubtitle(CharSequence subtitle) {}
			@Override public void setSubtitle(int resId) {}
			@Override public void setCustomView(View view) {}
			@Override public void invalidate() {}
			@Override public void finish() {}
			@Override public Menu getMenu() { return null; }
			@Override public CharSequence getTitle() { return null; }
			@Override public CharSequence getSubtitle() { return null; }
			@Override public View getCustomView() { return null; }
			@Override public MenuInflater getMenuInflater() { return null; }
		};
	}
	
	@Override
	public void clearFocus() {
		CMN.Log("wv::clearFocus");
		super.clearFocus();
	}
	
	//Viva Marshmallow!
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		CMN.debug("wv::startActionMode");
		if(weblistHandler!=null && weblistHandler.a.isFloating()) {
			return this.dummyActionMode();
		}
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
			MyMenuinversed = false;
			if (callmeback == null) callmeback = new callbackme();
			ActionMode mode = super.startActionMode(callmeback.callhere(callback), type);
			if(mode==null) return mode;
			//Toast.makeText(getContext(), mode.getTag()+"ONSACTM"+mode.hashCode(), 0).show();
			//if(true) return mode;
			//mode.setTag(110);
			final Menu menu = mode.getMenu();
			TweakWebviewContextMenu(menu);

			//todo 添加长按事件
			postDelayed(new Runnable() {
				@Override
				public void run() {
					//logAllViews();
					ViewGroup vg;
					List<View> views = /*ViewUtils.*/getWindowManagerViews();
					for(View vI:views){
						//CMN.Log("\n\n\n\n\n::  "+vI);
						//CMN.recurseLog(vI);
						/* 📕📕📕阿西吧折叠空间打开术📕📕📕 */
						if(vI instanceof FrameLayout){
							vg = (ViewGroup) vI;
							vI = vg.getChildAt(0);
							if(vI instanceof FrameLayout){
								vg = (ViewGroup) vI;
								vI = vg.getChildAt(0);
								if(vI instanceof LinearLayout){
									vg = (ViewGroup) vI;
									vI = vg.getChildAt(0);
									if(vI instanceof RelativeLayout){
										vg = (ViewGroup) vI;
										for (int i = 0; i < vg.getChildCount(); i++) {
											vI = vg.getChildAt(i);
											//CMN.Log(vI);
											//CMN.Log(vI instanceof LinearLayout);
											if(vI instanceof LinearLayout){
												vg = (ViewGroup) vI;
												for (int j = 0; j < vg.getChildCount(); j++) {
													vI = vg.getChildAt(j);
													if(vI instanceof LinearLayout){
														ViewGroup vgg = (ViewGroup) vI;
														if(vgg.getChildAt(1) instanceof TextView){
															TextView tv = (TextView) vgg.getChildAt(1);
															//CMN.Log(tv.getText().length()==3, tv.getText().toString().equals("TTS"), tv.getText(),tv, "YES??");
															CharSequence text = tv.getText();
															if(text!=null)
															if(text.length()==3 && tv.getText().toString().equals("TTS")){
																//CMN.Log("yes tts!!!");
																vgg.setOnLongClickListener(new OnLongClickListener() {
																	@Override
																	public boolean onLongClick(View v) {
																		evaluateJavascript("if(window.app)app.setTTS()",null);
																		return true;
																	}
																});
															} else if(tv.getText().length()==2 && tv.getText().toString().equals("高亮")){
																//CMN.Log("yes!!! 高亮");
																vgg.setOnLongClickListener(new OnLongClickListener() {
																	@Override
																	public boolean onLongClick(View v) {
																		evaluateJavascript(getUnderlineIncantation(false).toString(),null);
																		return true;
																	}
																});
															} else if(tv.getText().length()==2 && tv.getText().toString().equals("工具")){
																CMN.Log("yes!!! 工具");
																vgg.setOnLongClickListener(new OnLongClickListener() {
																	@Override
																	public boolean onLongClick(View v) {
																		/* 📕📕📕 微空间内爆术 📕📕📕 */
																		Context c = getContext();
																		//CMN.Log(c);
																		if(c instanceof ContextWrapper && !(c instanceof MainActivityUIBase)){
																			c = ((ContextWrapper)c).getBaseContext();
																		}
																		if(c instanceof MainActivityUIBase){
																			MainActivityUIBase a = (MainActivityUIBase) c;
																			if(MainActivityUIBase.PreferredToolId !=-1){
																				MainActivityUIBase.VerseKit ucc = a.getVtk();
																				ucc.bFromWebView=true;
																				a.weblist = weblistHandler;
																				ucc.setInvoker(presenter, WebViewmy.this, null, null);
																				ucc.onItemClick(null, null, MainActivityUIBase.PreferredToolId, -1, false, true);
																			}
																		}
																		return true;
																	}
																});
															}
														}
													}
												}

											}
										}
									}
								}
							}
						}
					}
				}
			}, 350);
			return mode;
		}
		return super.startActionMode(callback, type);
	}
	
	

	public void TweakWebviewContextMenu(Menu menu) {
		int gid=0;
		if(menu.size()>0) {
			/* remove artificial anti-intelligence */
			MenuItem item0 = menu.getItem(0);
			if(item0.getTitle().toString().startsWith("地") || item0.getTitle().toString().startsWith("Map"))
				menu.removeItem(item0.getItemId());
			if(menu.size()>0) gid=menu.getItem(0).getGroupId();
		}

		int highlightColor=Color.YELLOW;
		String ColorCurse = String.format("%06X", highlightColor&0xFFFFFF);
		Spanned text = Html.fromHtml("<span style='background:#"+ColorCurse+"; color:#"+ColorCurse+";'>高亮</span>");

		if(false) {
			MenuItem MyMenu = menu.add(0, R.id.toolbar_action0, 0, text);
			
			//Toast.makeText(getContext(),""+MyMenu.view,0).show();
			MyMenu = null;
			//MyMenu.get
			
			//Toast.makeText(getContext(),"asd"+menu.findItem(android.R.id.),0).show();
			//Toast.makeText(getContext(), MyMenu.getIntent()+""+MyMenu.getTitle()+" "+MyMenu.getItemId()+getResources().getString(android.R.string.share),0).show();
			//Toast.makeText(getContext(), ""+getResources().getString(getReflactField("com.android.internal.R$string", "share")),0).show();
			//Toast.makeText(getContext(),menu.getItem(3).getItemId()+"="+menu_share_id+"finding menu_share:"+menu.findItem(menu_share_id)+"="+android.R.id.shareText,0).show();
		}
		
		String shareText=getShareText();
		String SelectAllText=getSelectText();
		CMN.Log("SelectAllText", SelectAllText, System.identityHashCode(SelectAllText));
		int findCount=2;
		int ToolsOrder=0;
		for(int i=0;i<menu.size();i++) {
			MenuItem m = menu.getItem(i);
			String title = m.getTitle().toString();
			int id = m.getItemId();
			CMN.Log("menu id::", menu, title, Integer.toHexString(id));
			if(title.equals(shareText)) {
				menu.removeItem(id);//移除 分享
				i--;
				findCount--;
			} else if(title.equals(SelectAllText)) {
				ToolsOrder=m.getOrder();
				menu.removeItem(id);//移除 全选
				i--;
				findCount--;
			}
			//if(findCount==0) break;
		}

		menu.add(0,R.id.toolbar_action1,++ToolsOrder,R.string.tools);

		menu.add(0,R.id.toolbar_action3,++ToolsOrder,"TTS");
	}
	
	
	public String getSelectText() {
		getSharedIds();
		return getResources().getString(SelectString_Id!=0?SelectString_Id:android.R.string.selectAll);
	}
	
	public String getShareText() {
		getSharedIds();
		return getResources().getString(ShareString_Id!=0?ShareString_Id:R.string.share);
	}
	
	public String getCopyText() {
		getSharedIds();
		return getResources().getString(CopyString_Id!=0?CopyString_Id:android.R.string.copy);
	}
	
	private static void getSharedIds() {
		if(ShareString_Id==null) {
			Resources res = Resources.getSystem();
			CopyString_Id=res.getIdentifier("copy","string", "android");
			ShareString_Id=res.getIdentifier("share","string", "android");
			SelectString_Id=res.getIdentifier("selectAll","string", "android");
		}
	}
	
	
	/**
	function getNextNode(b) {
		var a = b.firstChild;
		if (a) {
			return a
		}
		while (b) {
			if ((a = b.nextSibling)) {
				return a
			}
			b = b.parentNode
		}
	}
	function getNodesInRange(c) {
		var b = [];
		var f = c.startContainer;
		var a = c.endContainer;
		var d = c.commonAncestorContainer;
		var e;
		for (e = f.parentNode; e; e = e.parentNode) {
			b.push(e);
			if (e == d) {
				break
			}
		}
		b.reverse();
		for (e = f; e; e = getNextNode(e)) {
			b.push(e);
			if (e == a) {
				break
			}
		}
		return b
	}
	function getNodeIndex(b) {
		var a = 0;
		while ((b = b.previousSibling)) {
			++a
		}
		return a
	}
	function insertAfter(d, b) {
		var a = b.nextSibling,
			c = b.parentNode;
		if (a) {
				c.insertBefore(d, a)
			} else {
				c.appendChild(d)
			}
		return d
	}
	function splitDataNode(c, a) {
		var b = c.cloneNode(false);
		b.deleteData(0, a);
		c.deleteData(a, c.length - a);
		insertAfter(b, c);
		return b
	}
	function isCharacterDataNode(b) {
		var a = b.nodeType;
		return a == 3 || a == 4 || a == 8
	}
	function splitRangeBoundaries(b) {
		var f = b.startContainer,
			e = b.startOffset,
			c = b.endContainer,
			a = b.endOffset;
		var d = (f === c);
		if (isCharacterDataNode(c) && a > 0 && a < c.length) {
				splitDataNode(c, a)
			}
		if (isCharacterDataNode(f) && e > 0 && e < f.length) {
				f = splitDataNode(f, e);
				if (d) {
					a -= e;
					c = f
				} else {
					if (c == f.parentNode && a >= getNodeIndex(f)) {
						++a
					}
				}
				e = 0
			}
		b.setStart(f, e);
		b.setEnd(c, a)
	}
	function getTextNodesInRange(b) {
		var f = [];
		var a = getNodesInRange(b);
		for (var c = 0, e, d; e = a[c++];) {
			if (e.nodeType == 3) {
				f.push(e);
			}
		}
		return f;
	}
	function surroundRangeContents(b, g) {
		splitRangeBoundaries(b);
		var f = getTextNodesInRange(b);
		if (f.length == 0) {
			return;
		}
		for (var c = 0, e, d; e = f[c++];) {
			if (e.nodeType == 3) {
				d = g.cloneNode(false);
				e.parentNode.insertBefore(d, e);
				d.appendChild(e);
			}
		}
		b.setStart(f[0], 0);
		var a = f[f.length - 1];
		b.setEnd(a, a.length);
	}
	*/
    @Metaline(trim=true)
	private final static String commonIcan="COMMONJS";
	private static int commonIcanBaseLen=0;
	private static StringBuilder HighlightBuilder;

	/**
	 (function(t,r){
		var sel = window.getSelection();
		var ret = r?sel.toString():'';
		try {
			var ann = document.createElement("span");
			if(t==0){
				ann.className = "PLOD_HL";
				ann.setAttribute("style", "background:#ffaaaa;");
			}else{
				ann.className = "PLOD_UL";
				//ann.style = "color:#ffaaaa;text-decoration: underline";
				ann.setAttribute("style", "border-bottom:1px solid #ffaaaa");
			}
			var ranges = [];
			var range;
			for (var i = 0, len = sel.rangeCount; i < len; ++i) {
				ranges.push(sel.getRangeAt(i))
			} //sel.removeAllRanges();
			i = ranges.length;
			while (i--) {
				range = ranges[i];
				surroundRangeContents(range, ann)
			}
		} catch (e) {console.log(e)}
		return ret;
	 });*/
	@Metaline(trim=true, compile=true)
	private final static  String HighLightIncantation="HI";
	/**
	function recurseDeWrap(b, t) {
		if (b) {
			for (var e = b.length - 1, d; e >= 0; e--) {
				d = b[e];
				if (d.className == t) {
					var c = 0;
					for (var f = d.childNodes.length - 1; f >= 0; f--) {
						var a = d.childNodes[f];
						if (!c) {
							c = d
						}
						d.parentNode.insertBefore(a, c);
						c = a
					}
					d.parentNode.removeChild(d)
				}
			}
		}
	}
	(function(t){
	 	if (window.getSelection) {
			var ann = document.createElement("span");
			ann.className = "highlight";
			var sel = window.getSelection();
			var ranges = [];
			var range;
			for (var i = 0, len = sel.rangeCount; i < len; ++i) {
				ranges.push(sel.getRangeAt(i))
			} //sel.removeAllRanges();
			i = ranges.length;
			while (i--) {
				range = ranges[i];
				var nodes = getNodesInRange(range);
				recurseDeWrap(nodes, t)
			}
	 	}
	 });
	 */
	@Metaline(trim=true, compile=true)
	private final static  String DeHighLightIncantation="DEHI";

	private StringBuilder prepareHighlightBuilder() {
		if(HighlightBuilder==null){
			HighlightBuilder = new StringBuilder(commonIcan);
			commonIcanBaseLen=HighlightBuilder.length();
		}
		HighlightBuilder.setLength(commonIcanBaseLen);
		return HighlightBuilder;
	}

	public StringBuilder getHighLightIncantation(boolean record) {
		return prepareHighlightBuilder().append(HighLightIncantation)
				.delete(HighlightBuilder.length()-1, HighlightBuilder.length())
				.append("(")
				.append("0")
				.append(",")
				.append(record?"1":"0")
				.append(")");
	}

	public StringBuilder getDeHighLightIncantation() {
		return prepareHighlightBuilder()
				.append(DeHighLightIncantation)
				.delete(HighlightBuilder.length()-1, HighlightBuilder.length())
				.append("(")
				.append("'PLOD_HL');");
	}

	public StringBuilder getUnderlineIncantation(boolean record) {
		prepareHighlightBuilder();
		return HighlightBuilder.append(HighLightIncantation)
				.delete(HighlightBuilder.length()-1, HighlightBuilder.length())
				.append("(")
				.append("1")
				.append(",")
				.append(record?"1":"0")
				.append(")");
	}

	public StringBuilder getDeUnderlineIncantation() {
		return prepareHighlightBuilder()
				.append(DeHighLightIncantation)
				.delete(HighlightBuilder.length()-1, HighlightBuilder.length())
				.append("(")
				.append("'PLOD_UL');");
	}

	/**
		''+window.getSelection()
	 */
	@Metaline
	public static final String CollectWord="CWJS";

	/**
		 var range=window.getSelection().getRangeAt(0);
		 var flmstd = document.getElementById('_PDict_Renderer');
	 	 if(!flmstd){
		 	flmstd = document.createElement('div');
			flmstd.id='_PDict_Renderer';
		 } else {
			flmstd.innerHTML='';
		 }
		flmstd.class='_PDict';
		flmstd.appendChild(range.cloneContents());
		flmstd.innerHTML;
	 */
	@Metaline
	public static final String CollectHtml="CHJS";

	public static final String SelectAll="document.execCommand('selectAll')";

	public static int getReflactField(String className,String fieldName){
		int result = 0;
		try {
			Class<?> clz = Class.forName(className);
			Field field = clz.getField(fieldName);
			field.setAccessible(true);
			result = field.getInt(null);
		} catch (Exception e) {
			CMN.Log(e);
		}
		return result;
	}

	public static int[] getReflactIntArray(String className,String fieldName){
		int[] result = null;
		try {
			Class<?> clz = Class.forName(className);
			Field field = clz.getField(fieldName);
			field.setAccessible(true);
			result = (int[]) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	//@Override
	//public boolean zoomOut(){
	//	CMN.Log("zoomOut");
	//	return false;
	//}

	public static void logAllViews(){
		List<View> views = getWindowManagerViews();
		for(View vI:views){
			CMN.Log("\n\n\n\n\n::  "+vI);
			CMN.recurseLog(vI);
		}
	}

	public static List<View> getWindowManagerViews() {
		try {
			// get the list from WindowManagerGlobal.mViews
			Class wmgClass = Class.forName("android.view.WindowManagerGlobal");
			Object wmgInstance = wmgClass.getMethod("getInstance").invoke(null);
			return viewsFromWM(wmgClass, wmgInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<View>();
	}

	private static List<View> viewsFromWM(Class wmClass, Object wmInstance) throws Exception {

		Field viewsField = wmClass.getDeclaredField("mViews");
		viewsField.setAccessible(true);
		Object views = viewsField.get(wmInstance);

		if (views instanceof List) {
			return (List<View>) viewsField.get(wmInstance);
		} else if (views instanceof View[]) {
			return Arrays.asList((View[])viewsField.get(wmInstance));
		}

		return new ArrayList<View>();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		lastX = event.getX();
		lastY = event.getY();
		if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			supressNxtClickTranslator = bIsActionMenuShown;
			if(fromCombined==1) {
				weblistHandler.setScrollFocus(this, frameAt);
			}
		}
		if (hasWidgets) {
			//setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			widgetsLayout.layoutWidgets();
			if (widgetsLayout.dispatchTouchEvent(event)) {
				return true;
			}
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		if (scrolling && getParent()!=null) {
			int nsy = getScrollY();
			int sy = nsy + speed;
			if (sy<0) sy=0;
			else sy = Math.min(computeVerticalScrollRange()-getHeight(), sy);
			if (nsy!=sy)
				scrollTo(getScrollX(), sy);
			else if(mScrollAbility!=null)
				((View)getParent()).postDelayed(mScrollAbility, 16);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (hasWidgets) {
			widgetsLayout.layoutWidgets();
		}
//		if(drawRect/* && presenter.getDrawHighlightOnTop()*/)
//		{
//			drawHighlightRect(canvas, Build.VERSION.SDK_INT<=25 && !GlobalOptions.isDark);
//		}
//		if (drawRect) {
//			drawHighlightRect(canvas, false);
//			float scale = webScale/ BookPresenter.def_zoom;
//			canvas.drawRect(highRigkt_X*scale, highRigkt_Y*scale, highRigkt_R *scale, highRigkt_B *scale
//					, ViewUtils.getRectPaint());
//		}
	}
	
	private void drawHighlightRect(Canvas canvas, boolean alpha) {
		float scale = webScale / BookPresenter.def_zoom;
		//float roundVal = 10*GlobalOptions.density*scale;
		canvas.drawRect(highRigkt_X*scale, highRigkt_Y*scale, highRigkt_R *scale, highRigkt_B *scale
				, alpha? ViewUtils.getRectPaintAlpha(): ViewUtils.getRectPaint());
	}
	
	/** WebView内布局，无视网页总长，与WebView保持恒定大小 */
	static class WidgetsLayout extends FrameLayout{
		final View scrollableView;
		int lastSx;
		int lastSy;
		public WidgetsLayout(@NonNull View scrollableView) {
			super(scrollableView.getContext());
			this.scrollableView = scrollableView;
		}
		// 强制内布局layout
		void layoutWidgets() {
			int sx = scrollableView.getScrollX(), sy = scrollableView.getScrollY(), sw=scrollableView.getWidth(), sh=scrollableView.getHeight();
			if (getLeft()!=sx||getTop()!=sy||sx!=lastSx||sy!=lastSy||sw!=getWidth()||sh!=getHeight())
			{
				layout(sx, sy, sx+sw, sy+sh);
				lastSx = sx;
				lastSy = sy;
				CMN.Log("layoutWidgets::", lastSx, lastSy, getWidth(), getHeight());
			}
		}
	}
	
	void init_widgets_layout() {
		if (widgetsLayout==null) {
			widgetsLayout = new WidgetsLayout(this);
			//widgetsLayout.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			addView(widgetsLayout);
		}
	}
	
	/** 自滚能力 卷死你们 */
	class ScrollAbility implements OnClickListener, OnLongClickListener, OnTouchListener, Runnable{
		float orgX;
		float orgY;
		float lastX;
		float lastY;
		boolean longScrollEnabled;
		boolean longScrollTriggered;
		boolean thisTouchScrolled;
		@Override
		public void run() {
			if (scrolling && getParent()!=null) {
				computeScroll();
			}
		}
		//GestureDetector gestureDetector=new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
		//	@Override
		//	public boolean onSingleTapConfirmed(MotionEvent e) {
		//		return true;
		//	}
		//});
		int lastPageTarget;
		int lastPageStep;
		long lastPageTime;
		// 单击，自动平滑滚动 类似于 page up / page down
		@Override
		public void onClick(View v) {
			boolean b1 = v.getId()==R.id.auto_scroll_d;
			if (b1||v.getId()==R.id.auto_scroll_u) {
				if (!thisTouchScrolled) {
					long now = CMN.now();
					int step = (int) (0.39*getHeight()/BookPresenter.def_zoom);
					if (!b1) step = -step;
					boolean smooth = true;
					//evaluateJavascript("window.scrollBy({top: "+step+(smooth?", behavior: \"smooth\" })":"})"), null);
					int scrollFrom = (int) (getScrollY()/BookPresenter.def_zoom);
					if (now-lastPageTime<350 && scrollFrom!=lastPageTarget) {
						scrollFrom = lastPageTarget;
						//smooth = false;
					}
					int target = scrollFrom + step;
					evaluateJavascript("window.scrollTo({top: "+target+(smooth?", behavior: \"smooth\" })":"})"), null);
					lastPageTarget = target;
					lastPageTime = now;
				}
			}
		}
		
		// 长按，自动滚动
		@Override
		public boolean onLongClick(View v) {
			if(longScrollEnabled && !thisTouchScrolled && !scrolling) {
				scrolling = thisTouchScrolled = true;
				speed = v.getId()==R.id.auto_scroll_d?15:-15;
				longScrollTriggered = true;
				post(this);
			}
			return false;
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int actionMasked = event.getActionMasked();
			if (v.getId()==R.id.auto_scroll_d||v.getId()==R.id.auto_scroll_u) {
				if (actionMasked==MotionEvent.ACTION_DOWN) {
					//CMN.Log("ScrollAbility::ACTION_DOWN");
					orgX = lastX = event.getRawX();
					orgY = lastY = event.getRawY();
					longScrollTriggered = thisTouchScrolled = false;
					SuppressScrollParent(true);
				}
				if (actionMasked==MotionEvent.ACTION_MOVE) {
					//CMN.Log("ScrollAbility::ACTION_MOVE");
					lastX = event.getRawX();
					lastY = event.getRawY();
					if (longScrollTriggered) {
						int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
						if (MathUtils.dist(lastX, lastY, orgX, orgY)>touchSlop/2) {
							longScrollTriggered = false;
						} else {
							return scrolling;
						}
					}
					// 计算Y轴距离，根据此距离动态改变滚动速度，距离越大滚动速度越大。
					float distance = (lastY-orgY);
					int sign = distance<0?-1:1;
					int minSpd = 1;
					float factor = Math.abs(distance)/Math.max(350, getHeight()/2);
					speed = sign*(int) MathUtils.lerp(minSpd, 180, factor);
					AccelerateInterpolator interpolator = new AccelerateInterpolator();
					speed = sign*(int) (minSpd+275*interpolator.getInterpolation(factor));
					if (speed>5) {
						interval = (int) MathUtils.lerp(8, 2, Math.max(0, Math.min(1, factor)));
					} else {
						interval = 16;
					}
					//CMN.Log("dist::", Math.abs(distance), lastY, orgY);
					if (!scrolling) {
						int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
						if (MathUtils.dist(lastX, lastY, orgX, orgY)>touchSlop/2) {
							scrolling = true;
							thisTouchScrolled = true;
							post(this);
						}
					}
				}
				if (actionMasked==MotionEvent.ACTION_UP||actionMasked==MotionEvent.ACTION_CANCEL) {
					scrolling = false;
					SuppressScrollParent(false);
				}
				return scrolling;
			}
			return false;
		}
		
		// 改变默认滚动行为，解决嵌套滚动时无法获得焦点。
		private void SuppressScrollParent(boolean suppress) {
			if (fromCombined==1) {
				ViewParent vp = getParent();
				if(vp!=null) {
					while((vp=vp.getParent())!=null) {
						if (vp instanceof ScrollView) {
							vp.requestDisallowInterceptTouchEvent(suppress);
							if (true && vp instanceof AdvancedNestScrollView) {
								((AdvancedNestScrollView)vp).setNestedScrollingEnabled(!suppress);
							}
							break;
						}
					}
				}
			}
		}
	}
	
	private static class SIDProvider{
		//final static Random rand = new Random(28517);
		final int id;
		SIDProvider(int id) {
			this.id = id;//((CMN.now()&0xFFFFL)<<32)|(long)rand.nextInt(Integer.MAX_VALUE/2);
		}
		@JavascriptInterface
		public int get(){return id;}
	}
	
	// 显示滚动按钮框
	public void SetupScrollRect(boolean enable) {
		if ((!enable) ^ (scrollRect==null||scrollRect.getVisibility()!=View.VISIBLE)) {
			if (enable) {
				init_widgets_layout();
				if (mScrollAbility == null)
					mScrollAbility = new ScrollAbility();
				if (scrollRect == null) {
					scrollRect = LayoutInflater.from(getContext()).inflate(R.layout.bw_scroll_rect, widgetsLayout, false);
					widgetsLayout.addView(scrollRect);
					((FrameLayout.LayoutParams) scrollRect.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
					scrollRect.setTranslationY(-55 * GlobalOptions.density);
					scrollRect.setTranslationX(-15 * GlobalOptions.density);
					scrollRect.setOnClickListener(ViewUtils.DummyOnClick);
					//evaluateJavascript("window.scrollBy({top: 50, behavior: \"smooth\" });", null);
					//evaluateJavascript("setInterval(()=>{window.scrollBy({top: 1})}, 1);", null);
					ViewUtils.setOnClickListenersOneDepth((ViewGroup) scrollRect, mScrollAbility, 999, null);
				}
				widgetsLayout.setVisibility(View.VISIBLE);
				scrollRect.setVisibility(View.VISIBLE);
				hasWidgets = true;
			} else {
				ViewUtils.removeView(scrollRect);
				scrollRect.setVisibility(View.GONE);
				if (true) { // todo honor other widgets
					widgetsLayout.setVisibility(View.GONE);
					hasWidgets = false;
				}
			}
		}
	}
	
	void layoutWidgets() {
		if (hasWidgets) {
			widgetsLayout.layoutWidgets();
		}
	}
	
	View getScrollRect() {
		if (hasWidgets && scrollRect!=null && scrollRect.getVisibility()==View.VISIBLE)
			return scrollRect;
		return null;
	}
}
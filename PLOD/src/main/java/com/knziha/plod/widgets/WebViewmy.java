package com.knziha.plod.widgets;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.BookPresenter;

import org.adrianwalker.multilinestring.Multiline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WebViewmy extends WebView implements MenuItem.OnMenuItemClickListener {
	public int currentPos;
	public int frameAt;
	public String toTag;
	public int SelfIdx;
	/** 标记视图来源。 0=单本搜索; 1=联合搜索; 2=点译模式; 3=翻阅模式。*/
	public int fromCombined;
	//public boolean fromPeruseview;
	public boolean fromNet;
	public String word;
	public int[] currentRendring;
	public boolean awaiting;
	public boolean bRequestedSoundPlayback;
	public int HistoryVagranter=-1;
	public float webScale=1;
	public int expectedPos=-1;
	public int expectedPosX=-1;
	public ArrayList<myCpr<String, ScrollerRecord>> History = new ArrayList<>();
	public float lastX;
	public float lastY;
	public static Integer ShareString_Id;
	public static Integer SelectString_Id;
	public static Integer CopyString_Id;
	
	public PhotoBrowsingContext IBC;
	
	public GradientDrawable toolbarBG;
	public AdvancedNestScrollLinerView titleBar;
	public final int[] ColorShade = new int[]{0xff4F7FDF, 0xff2b4381};
	public boolean clearHistory;
	public FlowTextView toolbar_title;
	public int AlwaysCheckRange;
	public boolean forbidLoading;
	private int mForegroundColor = 0xffffffff;
	private PorterDuffColorFilter ForegroundFilter;
	
	public boolean drawRect;
	public float highRigkt_X;
	public float highRigkt_Y;
	public float highRigkt_R;
	public float highRigkt_B;
	public static boolean supressNxtClickTranslator;
	
	public WebViewmy(Context context) {
		this(context, null);
	}

	public WebViewmy(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WebViewmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//setWebContentsDebuggingEnabled(true);
		
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

//		settings.setAppCacheEnabled(true);
//		settings.setDatabaseEnabled(true);
//		settings.setDomStorageEnabled(true);
		
		settings.setAllowFileAccess(true);

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
		super.loadUrl(url);
		//CMN.Log("\n\nloadUrl", url);
		drawRect=false;
		isloading=true;
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

	/**
	 *  回退/前进时记录历史 (缩放、位置)
	 *  */
	public ScrollerRecord saveHistory(ViewGroup WHP, long lastClickTime) {
		if(!isloading && System.currentTimeMillis()-lastClickTime>300) {//save our postion
			ScrollerRecord PageState = History.get(HistoryVagranter).value;
			if (PageState == null)
				History.set(HistoryVagranter, new myCpr<>(""+currentPos, PageState=new ScrollerRecord()));
			if(WHP!=null){
				PageState.x = 0;
				PageState.y = WHP.getScrollY();
				PageState.scale = BookPresenter.def_zoom;
			}else{
				PageState.x = getScrollX();
				PageState.y = getScrollY();
				PageState.scale = webScale;
				//CMN.Log("记录位置", PageState.x, PageState.y, webScale);
			}
			return PageState;
		}
		return null;
	}

	public void onFinishedPage() {
		if(wvclient!=null)
			wvclient.onPageFinished(this, "file:///");
	}

	public void addHistoryAt(int idx) {
		//CMN.Log("创造历史！！！");
		History.add(++HistoryVagranter,new myCpr<>(String.valueOf(idx),new ScrollerRecord(expectedPosX, expectedPos, -1)));
		for(int i=History.size()-1;i>=HistoryVagranter+1;i--)
			History.remove(i);
	}

	public void clearIfNewADA(int adapter_idx) {
		if(SelfIdx!=adapter_idx){
			//CMN.Log("清空历史!!!", adapter_idx, SelfIdx);
			History.clear();
			HistoryVagranter=-1;
		}
	}

	public void shutDown() {
		setWebChromeClient(null);
		setWebViewClient(null);
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
		drawRect = true;
		postInvalidate();
	}
	
	public void FindBGInTitle(ViewGroup toolbar_web) {
		toolbarBG = (GradientDrawable) ((LayerDrawable)toolbar_web.getBackground()).getDrawable(0);
	}
	
	
	public GradientDrawable MutateBGInTitle() {
		LayerDrawable d = ((LayerDrawable) titleBar.getBackground().mutate());
		toolbarBG = (GradientDrawable) d.getDrawable(0);
		return toolbarBG;
	}
	
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
	
	public boolean SavePagePosIfNeeded(ScrollerRecord pagerec) {
		boolean ret=false;
		int sx=getScrollX(), sy=getScrollY();
		if(pagerec==null && (sx != 0 || sy != 0 || webScale != BookPresenter.def_zoom)) {
			pagerec = new ScrollerRecord();
			ret=true;
		}
		if(pagerec!=null) {
			pagerec.set(sx, sy, webScale);
		}
		return ret;
	}
	
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
			return bIsActionMenuShown=callback.onCreateActionMode(mode, menu);
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
				evaluateJavascript(getHighLightIncantation().toString(), value -> invalidate());
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
				View cover=((ViewGroup) getParent()).getChildAt(fromCombined==2?1:0).findViewById(R.id.cover);
				if(cover!=null){
					cover.setTag(1);
					cover.performClick();
				}
			} return false;
			case R.id.toolbar_action3:{//TTS
				evaluateJavascript("if(window.app)app.ReadText(''+window.getSelection())",null);
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
	
	//Viva Marshmallow!
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
			MyMenuinversed = false;
			if (callmeback == null) callmeback = new callbackme();
			ActionMode mode = super.startActionMode(callmeback.callhere(callback), type);
			//if(true) return mode;
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
					List<View> views = getWindowManagerViews();
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
																		evaluateJavascript(getUnderlineIncantation().toString(),null);
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
																				BookPresenter invoker = null;
																				if(SelfIdx<a.md.size()){
																					invoker = a.md.get(SelfIdx);
																				}
																				MainActivityUIBase.UniCoverClicker ucc = a.getUcc(); ucc.bFromWebView=true; ucc.bFromPeruseView=WebViewmy.this.fromCombined==3;
																				ucc.setInvoker(invoker, WebViewmy.this, null, null);
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

		MenuItem MyMenu = menu.add(0, R.id.toolbar_action0, 0, text);

		//Toast.makeText(getContext(),""+MyMenu.view,0).show();
		MyMenu = null;
		//MyMenu.get

		//Toast.makeText(getContext(),"asd"+menu.findItem(android.R.id.),0).show();
		//Toast.makeText(getContext(), MyMenu.getIntent()+""+MyMenu.getTitle()+" "+MyMenu.getItemId()+getResources().getString(android.R.string.share),0).show();
		//Toast.makeText(getContext(), ""+getResources().getString(getReflactField("com.android.internal.R$string", "share")),0).show();
		//Toast.makeText(getContext(),menu.getItem(3).getItemId()+"="+menu_share_id+"finding menu_share:"+menu.findItem(menu_share_id)+"="+android.R.id.shareText,0).show();

		String shareText=getShareText();
		String SelectAllText=getSelectText();
		CMN.Log("SelectAllText", SelectAllText, System.identityHashCode(SelectAllText));
		int findCount=2;
		int ToolsOrder=0;
		//if(false)
		for(int i=0;i<menu.size();i++) {
			String title = menu.getItem(i).getTitle().toString();
			if(title.equals(shareText)) {
				menu.removeItem(menu.getItem(i).getItemId());//移除 分享
				i--;
				findCount--;
			} else if(title.equals(SelectAllText)) {
				ToolsOrder=menu.getItem(i).getOrder();
				menu.removeItem(menu.getItem(i).getItemId());//移除 全选
				i--;
				findCount--;
			}
			if(findCount==0) break;
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
   @Multiline(trim=true)
	private final static String commonIcan="COMMONJS";
	private static int commonIcanBaseLen=0;
	private static StringBuilder HighlightBuilder;

	/**
	 (function(t){
	     if (window.getSelection) {
			var ann = document.createElement("span");
			if(t==0){
				ann.className = "PLOD_HL";
	 			ann.setAttribute("style", "background:#ffaaaa;");
	 		}else{
				ann.className = "PLOD_UL";
				//ann.style = "color:#ffaaaa;text-decoration: underline";
	 			ann.setAttribute("style", "border-bottom:1px solid #ffaaaa");
	 		}
			var sel = window.getSelection();
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
	 	}
	 })(
	 */
	@Multiline(trim=true)
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
	 })(
	 */
	@Multiline(trim=true)
	private final static  String DeHighLightIncantation="DEHI";

	private void prepareHighlightBuilder() {
		if(commonIcanBaseLen==0){
			HighlightBuilder = new StringBuilder(commonIcan);
			commonIcanBaseLen=HighlightBuilder.length();
		}
		HighlightBuilder.setLength(commonIcanBaseLen);
	}

	public StringBuilder getHighLightIncantation() {
		prepareHighlightBuilder();
		return HighlightBuilder.append(HighLightIncantation).append("0);");
	}

	public StringBuilder getDeHighLightIncantation() {
		prepareHighlightBuilder();
		return HighlightBuilder.append(DeHighLightIncantation).append("'PLOD_HL');");
	}

	public StringBuilder getUnderlineIncantation() {
		prepareHighlightBuilder();
		return HighlightBuilder.append(HighLightIncantation).append("1);");
	}

	public StringBuilder getDeUnderlineIncantation() {
		prepareHighlightBuilder();
		return HighlightBuilder.append(DeHighLightIncantation).append("'PLOD_UL');");
	}

	/**
		''+window.getSelection()
	 */
	@Multiline
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
	@Multiline
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
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(drawRect){
			float scale = webScale/ BookPresenter.def_zoom;
			//float roundVal = 10*GlobalOptions.density*scale;
			canvas.drawRect(highRigkt_X*scale, highRigkt_Y*scale, highRigkt_R *scale, highRigkt_B *scale, Utils.getRectPaint());
		}
		super.onDraw(canvas);
	}
}
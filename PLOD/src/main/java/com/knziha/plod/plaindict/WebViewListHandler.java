package com.knziha.plod.plaindict;

import static com.knziha.plod.PlainUI.AppUIProject.ContentbarBtnIcons;
import static com.knziha.plod.PlainUI.AppUIProject.RebuildBottombarIcons;
import static com.knziha.plod.dictionary.Utils.IU.NumberToText_SIXTWO_LE;
import static com.knziha.plod.plaindict.CMN.EmptyRef;
import static com.knziha.plod.plaindict.CMN.GlobalPageBackground;
import static com.knziha.plod.plaindict.DBListAdapter.DB_FAVORITE;
import static com.knziha.plod.preference.SettingsPanel.makeInt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;

import com.jess.ui.TwoWayGridView;
import com.knziha.plod.PlainUI.AlloydPanel;
import com.knziha.plod.PlainUI.AppUIProject;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.Utils.Bag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.AdvancedNestScrollView;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowCheckedTextView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.Framer;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.additiveMyCpr1;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/** 页面管理器，曾经尝试过在Listview中放置webview，太卡。现在保留的模式：<br/>
 * 一个webview，显示一本或多本词典内容（合并的多页面模式）。 <br/>
 * 一个webview，一次显示一本词典内容，滑动翻页可切换子词条（屏风模式）。 <br/>
 * 多个webview，放在LinearLayout中，显示多本词典内容。（多页面视图列表）*/
public class WebViewListHandler extends ViewGroup implements View.OnClickListener, View.OnLongClickListener {
	public final MainActivityUIBase a;
	/** Search-On-Page Control Flag. 网页设置标志位 指示是否开启点击翻译、页内搜索设置 */
	public int shezhi;
	public boolean bIsPopup;
	/** Flag Backup. 设置备份，同于比对变化。 */
	int szStash;
	/** see{@link com.knziha.plod.db.SearchUI }*/
	int src;
	/** -2=auto[0-1];0=false;1=true;2=foldingScreen*/
	public int bMergeFrames = 0;
	/** for {@link com.knziha.plod.PlainUI.WordPopup }. final. When set, the default view {@link #contentUIData } won't be initialized. */
	public boolean bDataOnly = false;
	public boolean bShowInPopup = false;
	public int bMergingFrames = -1;
	public boolean bShowingInPopup = false;
	public ScrollViewmy WHP;
	public PDICMainAppOptions opt;
	ViewGroup webholder;
	ViewGroup webSingleholder;
	/** displaying id of batchSearch */
	public long did;
	public additiveMyCpr1 jointResult;
	public WebViewmy mMergedFrame;
	public BookPresenter mMergedBook;
	public ArrayList<BookPresenter> frames = new ArrayList();
	public ArrayList<long[]> framesDisplaying = new ArrayList();
	public ArrayList<PlainWeb> moders = new ArrayList();
	@NonNull
	public final ContentviewBinding contentUIData;
	public ImageView[] ContentbarBtns = new ImageView[ContentbarBtnIcons.length];
	private boolean contentViewSetup;
	private int lastScrollUpdateY;
	Runnable UpdateBookLabelAbility;
	BookPresenter lastScrolledBook;
	public WebViewmy scrollFocus;
	WebViewmy mWebView;
	public boolean tapSch;
	public boolean tapDef;
	public int zhTrans;
	public int tapSel;
	public boolean showNavor;
	
	public View browserWidget8;
	public View browserWidget10;
	public View browserWidget11;
	public RLContainerSlider pageSlider;
	public boolean slideDirty;
	public DragScrollBar mBar;
	public ImageView prv,nxt;
	public SeekBar entrySeek;
	
	private boolean bottomNavWeb;
	/** txtBt 悬浮小按钮 floatBtn */
	public View toolsBtn;
	/** 须在render、前后导航时经由{@link #setStar}更新 */
	public String displaying;
	
	/** 取词模式 1=wordToday  2=wordPopup */
	public int fetchWord;
	public int lastFetchWord = 2;
	public TextView etSearch;
	
	public WebViewListHandler(@NonNull MainActivityUIBase a, @NonNull ContentviewBinding contentUIData, int src) {
		super(a);
		this.a = a;
		this.opt = a.opt;
		setId(R.id.webholder);
		//setUseListView(true);
		this.contentUIData = contentUIData;
		this.WHP = contentUIData.WHP;
		this.webholder = contentUIData.webholder;
		this.webSingleholder = contentUIData.webSingleholder;
		this.src = src;
		hDataSinglePage.webviewHolder = webSingleholder;
		hDataMultiple.webviewHolder = contentUIData.webholder;
		a.yaoji.add(new WeakReference<>(this));
		if(WHP.getScrollViewListener()==null) {
			/** 这里绑定自己到底栏，以获取上下文 see{@link MainActivityUIBase#showScrollSet} */
			contentUIData.bottombar2.setTag(this);
			contentUIData.PageSlider.weblist = this;
			contentUIData.cover.weblist = this;
			contentUIData.cover.hdl = a.hdl;
			contentUIData.navMore.setOnClickListener(this);
			browserWidget8 = contentUIData.browserWidget8;
			browserWidget10 = contentUIData.browserWidget10;
			browserWidget11 = contentUIData.browserWidget11;
			entrySeek = contentUIData.entrySeek;
			entrySeek.setOnSeekBarChangeListener(entrySeekLis);
			pageSlider = contentUIData.PageSlider;
			if (src==SearchUI.MainApp.MAIN || src== SearchUI.FloatSch.MAIN) {
				pageSlider.onSwipeTopListener = a;
			}
			DragScrollBar mBar = this.mBar = contentUIData.dragScrollBar;
			toolsBtn = contentUIData.tools;
			toolsBtn.setOnClickListener(this);
			toolsBtn.setOnLongClickListener(this);
			
			prv = contentUIData.prv;
			nxt = contentUIData.nxt;
			prv.setOnClickListener(this);
			nxt.setOnClickListener(this);
			contentUIData.zoomIn.setOnClickListener(this);
			contentUIData.zoomOut.setOnClickListener(this);
			
			contentUIData.PageSlider.page = contentUIData.cover;
			contentUIData.cover.setPager(a.getPageListener());
			UpdateBookLabelAbility = ()->{
				String name = scrollFocus.presenter.getDictionaryName();
				int idx=name.lastIndexOf(".");
				if(idx>=0)name = name.substring(0, idx);
				contentUIData.dictName.setText(name);
				contentUIData.dictNameStroke.setText(name);
			};
			WHP.scrollbar2guard=mBar;
			WHP.setScrollViewListener((v, x, y, oldX, oldY) -> {
				oldY = y-WHP.oldY;
				if(mViewMode==WEB_LIST_MULTI && oldY!=0
					&& Math.abs(oldY)>8) {
						if(mBar.isHidden()){
							if(Math.abs(oldY)>=10*a.dm.density)
								mBar.fadeIn();
						}
						if(!mBar.isHidden()){
							if(!mBar.isWebHeld)
								mBar.hiJackScrollFinishedFadeOut();
							if(!mBar.isDragging){
								mBar.setMax(webholder.getMeasuredHeight()-WHP.getMeasuredHeight());
								mBar.progress(y);
							}
						}
						if(0==bMergingFrames && Math.abs(lastScrollUpdateY-y)>GlobalOptions.density*50) {
							lastScrollUpdateY=y;
							//CMN.pt("滚动="+Math.abs(oldY)+","+"y="+y+"::");
							//CMN.rt();
							int bot=WHP.getScrollY() + WHP.getHeight()/2;
							for (int i = 0; i < webholder.getChildCount(); i++) {
								if(webholder.getChildAt(i).getBottom()>=bot) {
									WebViewmy wv = (WebViewmy)webholder.getChildAt(i).getTag();
									if(lastScrolledBook !=wv.presenter) {
										setScrollFocus(wv, i);
									}
									break;
								}
							}
						}
				}
				WHP.oldY = y;
				if (WHP.mIsFling && Math.abs(oldY) < 2) {
					WHP.mIsFling = false;
				}
			});
			mBar.fadeOut();
		}
		//WHP.setBackground(null);
		// CMN.debug("shzh::ini::rcsp::", tapSch, BookPresenter.MakePageFlag(this, opt));
		tapSch = src==SearchUI.Fye.MAIN?PDICMainAppOptions.fyeTapSch():opt.tapSch();
		if (src== SearchUI.FloatSch.MAIN || src==SearchUI.MainApp.MAIN ) {
			tapDef = opt.tapViewDefMain();
		}
		tapSel = opt.getInt("tapSel", 0);
		shezhi = BookPresenter.MakePageFlag(this, opt);
		zhTrans = PDICMainAppOptions.webZhTranslate();
	}
	
	public final Runnable entrySeekRn = new Runnable() {
		@Override
		public void run() {
			JumpToFrame(entrySeek.getProgress());
		}
	};
	boolean seeking = false;
	public final SeekBar.OnSeekBarChangeListener entrySeekLis = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (multiRecord!=null) {
				if (fromUser) {
					//CMN.Log("onProgressChanged!!!");
//					a.root.removeCallbacks(entrySeekRn);
//					a.root.postDelayed(entrySeekRn, isFoldingScreens()?100:50);
					entrySeekRn.run();
					toastFrame();
				}
				if (multiRecord.jointResult!=null) {
					multiRecord.jointResult.LongestStartWithSeqLength = -progress;
				}
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			seeking = true;
			toastFrame();
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			seeking = false;
		}
	};
	
	private void toastFrame() {
		if (scrollFocus!=null) {
			a.showT(scrollFocus.presenter.getDictionaryName(), Toast.LENGTH_SHORT);
			a.m_currentToast.setGravity(Gravity.BOTTOM, 0, 175*2);
		}
	}
	
	public void setScrollFocus(WebViewmy wv, int frame) {
		if (!bDataOnly) {
			if(lastScrolledBook != wv.presenter) {
				lastScrolledBook = wv.presenter;
				a.root.postOnAnimationDelayed(UpdateBookLabelAbility, 50);
			}
			scrollFocus = wv;
		}
		if (ViewUtils.isVisible(entrySeek)) {
			entrySeek.setProgress(frame);
		}
	}
	
	public ViewGroup getViewGroup() {
		return mViewMode==WEB_LIST_MULTI?webholder:webSingleholder;
	}
	
	public ViewGroup getDragView() {
		return bDataOnly?dictView:mViewMode==WEB_LIST_MULTI?WHP:webSingleholder;
	}
	
	public View getChildAt(int frameAt) {
		return contentUIData==null?null:contentUIData.webholder.getChildAt(frameAt);
	}
	
	@Override
	public int getChildCount() {
		return contentUIData==null?0:contentUIData.webholder.getChildCount();
	}
	
	public void shutUp() {
		if(WHP.getVisibility()==View.VISIBLE) {
//			if(contentUIData.webholder.getChildCount()!=0)
//				contentUIData.webholder.removeAllViews();
			WHP.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void addView(View child, int index) {
		(mViewMode==WEB_VIEW_SINGLE?webSingleholder:contentUIData.webholder).addView(child, index);
	}
	
	public void removeAllViews() {
//		try {
//			throw new RuntimeException();
//		} catch (RuntimeException e) {
//			CMN.debug("removeAllViews::", e);
//		}
		(mViewMode==WEB_VIEW_SINGLE?webSingleholder:contentUIData.webholder).removeAllViews();
	}
	
	@Override
	public void setVisibility(int visibility) {
		(mViewMode==WEB_VIEW_SINGLE?webSingleholder:WHP).setVisibility(visibility);
	}
	
	@Override
	public int getVisibility() {
		return (mViewMode==WEB_VIEW_SINGLE?webSingleholder:WHP).getVisibility();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) { }
	
	public void setBackgroundColor(int manFt_globalPageBackground) {
		WHP.setBackgroundColor(manFt_globalPageBackground);
	}
	
	public BookPresenter getFrameAt(int pos) {
		if (multiDicts && pos>=0 && pos<frames.size()) {
			return frames.get(pos);
		}
		return a.EmptyBook;
	}
	
	public int calcFrameAt() {
		if (multiDicts) {
			if (isFoldingScreens()) {
				int pos = batchDisplaying().LongestStartWithSeqLength;
				if (pos<0) return -pos;
			}
			else if(!isMergingFrames()) {
				final int currentHeight=WHP.getScrollY();
				for(int i=0;i<webholder.getChildCount();i++) {
					View CI = webholder.getChildAt(i);
					if(CI.getBottom() > currentHeight) {
						return i;
					}
				}
			}
			return 0;
		}
		return -1;
	}
	
	public ViewGroup getScrollView() {
		return WHP;
	}
	
	public void setScrollbar() {
		contentUIData.dragScrollBar.setMax(webholder.getMeasuredHeight()-WHP.getMeasuredHeight());
		contentUIData.dragScrollBar.progress(WHP.getScrollY());
		//a.mBar.onTouch(null, MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,0,0,0));
	}
	
	public OnLayoutChangeListener OLCL;
	public void installLayoutScrollListener(resultRecorderCombined recom) {
		WHP.touchFlag.first=false;
		if(OLCL==null) {
			OLCL = new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,int oldBottom) {
					//CMN.Log("onLayoutChange", expectedPos, sv.getScrollY());
					//CMN.Log("onLayoutChange", a.WHP.touchFlag.first, scrolled, bottom - top >= expectedPos + sv.getMeasuredHeight());
					//CMN.Log("onLayoutChange", bottom - top , expectedPos + sv.getMeasuredHeight());
					if (WHP.touchFlag.first) {
						contentUIData.mainProgressBar.setVisibility(View.GONE);
						v.removeOnLayoutChangeListener(this);
						return;
					}
					//if(expectedPos==0) return;
					int HGEIGHT = bottom - top;
					if (HGEIGHT < recom.LHGEIGHT)
						recom.scrolled = false;
					recom.LHGEIGHT = HGEIGHT;
					if(recom.scrollTarget!=null)
						recom.expectedPos=recom.scrollTarget.getTop();
					ScrollViewmy sv = WHP;
					if (!recom.scrolled) {
						if (HGEIGHT >= recom.expectedPos + sv.getMeasuredHeight()) {
							sv.scrollTo(0, recom.expectedPos);//smooth
							//CMN.Log("onLayoutChange scrolled", expectedPos, sv.getMeasuredHeight());
							if (sv.getScrollY() == recom.expectedPos) {
								contentUIData.mainProgressBar.setVisibility(View.GONE);
								recom.scrolled = true;
							}
						}
					}
				}
			};
		}
		
		recom.LHGEIGHT=0;
		webholder.removeOnLayoutChangeListener(OLCL);
		if(!recom.toHighLight){
			ViewUtils.addOnLayoutChangeListener(webholder, OLCL);
			if(contentUIData.mainProgressBar!=null)
				contentUIData.mainProgressBar.setVisibility(recom.expectedPos==0?View.GONE:View.VISIBLE);
			recom.scrolled=false;
		}
		contentUIData.PageSlider.setWebview(null, this);
	}
	
	public void NotifyScrollingTo(resultRecorderCombined recom) {
		WHP.touchFlag.first=false;
		recom.LHGEIGHT=WHP.getHeight();
		webholder.removeOnLayoutChangeListener(OLCL); // todo save this step ???
		ViewUtils.addOnLayoutChangeListener(webholder, OLCL);
	}
	
	int shFlag;
	public int getScrollHandType() {
		return opt.getTypeFlag_11_AtQF(shFlag);
	}
	
	public void setScrollHandType(int style) {
		opt.setTypeFlag_11_AtQF(style, shFlag);
		resetScrollbar(mWebView, bMergingFrames==1, true);
	}
	
	
	public final void resetScrollbar(){
		resetScrollbar(dictView, false, false);
	}
	
	/**  0=在右; 1=在左; 2=无; 3=系统滚动条  */
	public void resetScrollbar(WebViewmy mWebView, boolean merged, boolean resetMerge){
		int vis = View.VISIBLE;
		boolean vsi = false;
		int gravity = 0;
		int type = a.thisActType==MainActivityUIBase.ActType.FloatSearch?2 //浮动搜索
				:(a.PeruseSearchAttached() && mWebView==a.peruseView.mWebView)?4 // 翻阅
				:(mWebView!=null&& mWebView==a.wordPopup.mWebView)?6 // 点译
				:0 // 主程序
				;
		shFlag=type;
		// 主程序 浮动搜索 点译
		if(merged) {
			vis=View.GONE;
			vsi=false;
			if (resetMerge && mWebView!=null) {
				mWebView.evaluateJavascript("SH_S("+opt.getTypeFlag_11_AtQF(shFlag)+")", null);
			}
		}
		else {
			final int sty=opt.getTypeFlag_11_AtQF(type);
			switch (sty){
				case 0:
					gravity=Gravity.END;
					break;
				case 1:
					gravity=Gravity.START;
					break;
				case 2:
					vis=View.GONE;
					break;
				case 3:
					vis=View.GONE;
					vsi=true;
					break;
			}
		}
		// CMN.Log("resetScrollbar", mWebView!=null, vis, vsi);
		DragScrollBar mBar = this.mBar;
		if(mBar.getVisibility()!=vis)
			mBar.setVisibility(vis);
		if(vis==View.VISIBLE) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mBar.getLayoutParams();
			if(gravity!=0 && lp.gravity!=gravity){
				lp.gravity=gravity;
				mBar.requestLayout();
			}
			mBar.setHandleColorFiler(a.MainAppBackground);
			if(mWebView!=null) {
				mBar.setDelimiter(GlobalOptions.isDark?"<>":"", mWebView);
			} else {
				mBar.setDelimiter(isViewSingle()?
						GlobalOptions.isDark?"<>":null
						:"|||", isViewSingle()?mMergedFrame:contentUIData.WHP);
			}
		}
		(mWebView==null?contentUIData.WHP:mWebView).setVerticalScrollBarEnabled(vsi);
		a.weblist = this;
		this.mWebView = mWebView;
	}
	
	public BookPresenter getMergedBook() {
		if(mMergedBook==null) {
			try {
				mMergedBook = new BookPresenter(new File("empty"), null, 1);
				mMergedBook.isMergedBook(true);
				mMergedBook.a = a;
				mMergedBook.opt = a.opt;
			} catch (IOException ignored) { }
		}
		return mMergedBook;
	}
	
	public WebViewmy getMergedFrame(BookPresenter book) {
		getMergedBook().getWebBridge().setBook(book);
		getMergedFrame().setPresenter(book);
		return mMergedFrame;
	}
	
	public WebViewmy getMergedFrame() {
		if(mMergedFrame==null) {
			getMergedBook().initViewsHolder(a);
			mMergedFrame = mMergedBook.mWebView;
			mMergedFrame.weblistHandler = this;
			mMergedBook.rl.setTag(mMergedFrame);
			mMergedFrame.setWebViewClient(a.myWebClient);
			mMergedFrame.setWebChromeClient(a.myWebCClient);
			//mMergedFrame.setOnScrollChangedListener(null);
			//mMergedFrame.SetupScrollRect(true);
			mMergedFrame.getSettings().setTextZoom(BookPresenter.def_fontsize);
			scrollFocus = mMergedFrame;
		}
		return mMergedFrame;
	}
	
	public WebViewmy newFrame() {
		getMergedFrame();
		WebViewmy mMergedFrame;
		getMergedBook().initViewsHolder(a);
		mMergedFrame = new WebViewmy(a);
		mMergedFrame.weblistHandler = this;
		//mMergedBook.rl.setTag(mMergedFrame);
		mMergedFrame.setWebViewClient(a.myWebClient);
		mMergedFrame.setWebChromeClient(a.myWebCClient);
		//mMergedFrame.setOnScrollChangedListener(null);
		//mMergedFrame.SetupScrollRect(true);
		mMergedFrame.getSettings().setTextZoom(BookPresenter.def_fontsize);
		return mMergedFrame;
	}
	
	boolean webHolderSwapHide = true;
	
	/** 是否将共用的 mMergedFrame 以一定手段塞入webholder列表。但是列表只会显示一个（mMergedFrame 或 mWebView）。 */
	public void initMergedFrame(int mergeWebHolder, boolean popup, boolean bUseMergedUrl) {
		if(bUseMergedUrl && mMergedFrame!=null) {
			mMergedFrame.presenter=mMergedBook;
			//mMergedBook.toolbar.setVisibility(View.GONE);
		}
		if(!popup && bShowingInPopup) { //reset popup
			ViewUtils.removeView(alloydPanel.toolbar);
			contentUIData.webcontentlister.setPadding(0,0,0,0);
			bShowingInPopup = false;
		}
		if(bMergingFrames!=mergeWebHolder || getViewGroup().getChildCount()==0) {
			CMN.debug("reinitMergedFrame::", mergeWebHolder, popup, bUseMergedUrl);
			if(mergeWebHolder==0) {
				contentUIData.webcontentlister.setAlpha(1);
				//if(mMergedBook!=null) {
				//	mMergedBook.toolbar.setVisibility(View.VISIBLE);
				//}
				
//				webholder.getLayoutParams().height = WRAP_CONTENT;
//				ViewUtils.addViewToParent(WHP, contentUIData.PageSlider, 1);
//				if(webHolderSwapHide) {
//					WHP.setVisibility(View.VISIBLE);
//				}
			}
			else {
				WebViewmy mMergedFrame = getMergedFrame();
				ViewUtils.addViewToParent(mMergedFrame.rl, webSingleholder);
				mMergedBook.toolbar.setVisibility(mergeWebHolder==1?View.GONE:View.VISIBLE);
//				contentUIData.navBtns.setVisibility(View.GONE);
//				if(webHolderSwapHide) {
//					WHP.setVisibility(View.GONE);
//				} else {
//					ViewUtils.removeView(WHP);
//				}
			}
			bMergingFrames = mergeWebHolder;
		}
	}
	
	public void toggleFoldAll() {
		if (isMergingFrames()) {
			getMergedFrame().evaluateJavascript("togFoldAll()", null);
		} else {
			int targetVis=View.VISIBLE;
			int cc=getChildCount();
			if(cc>0) {
				for (int i = 0; i < cc; i++) {
					View v = getChildAt(i).findViewById(R.id.webviewmy);
					if (v!=null && v.getVisibility() != View.GONE) {
						targetVis = View.GONE;
						break;
					}
				}
				if(targetVis==View.GONE) {
					a.awaiting = false;
				}
				for (int i = 0; i < cc; i++) {
					View childAt = getChildAt(i);
					WebViewmy targetView = childAt.findViewById(R.id.webviewmy);
					if (targetView != null) {
						if(targetVis==View.GONE) {
							targetView.setVisibility(targetVis);
						} else if(targetView.getVisibility()!=View.VISIBLE){
							childAt.findViewById(R.id.toolbar_title).performClick();
						}
					}
				}
			}
		}
	}
	
	public void prvnxtFrame(boolean nxt) {
		if (isMultiRecord() && multiDicts) {
			if (pageSlider.tapZoom) {
				pageSlider.bSuppressNxtTapZoom = CMN.now();
			}
			if(isFoldingScreens()) {
				JumpToFrame(frameSelection+(nxt?1:-1));
			}
			else if(isMergingFrames()) {
				mMergedFrame.evaluateJavascript(nxt?"prvnxtFrame(1)":"prvnxtFrame()", null);
			} else if(!isViewSingle()){
				final int currentHeight=WHP.getScrollY();
				int cc=contentUIData.webholder.getChildCount();
				int childAtIdx=cc;
				int top;
				for(int i=0;i<cc;i++) {
					top = contentUIData.webholder.getChildAt(i).getTop();
					if(top>=currentHeight){
						childAtIdx=i;
						if(nxt && top!=currentHeight) --childAtIdx;
						break;
					}
				}
				childAtIdx+=nxt?1:-1;
				if(childAtIdx>=cc){
					a.scrollToPageBottom(contentUIData.webholder.getChildAt(cc-1));
				} else {
					a.scrollToWebChild(contentUIData.webholder.getChildAt(childAtIdx));
				}
			}
		}
	}
	
	int frameCount;
	int frameSelection;
	int frameAt;
	WeakReference<AlertDialog> jumpListDlgRef = EmptyRef;
	AlertDialog jumpListDlg;
//	static class TextConfig{
//		int[] ids;
//		float tsz;
//	}
//	TextConfig tf;
	
	
	public void JumpToFrame(int pos) {
		if (multiDicts && pos>=0 && pos<multiRecord.jointResult.realmCount) {
			//CMN.debug("JumpToFrame::", pos, isFoldingScreens());
			frameSelection = pos;
			if(isFoldingScreens()) {
				renderFoldingScreen(pos);
			}
			else if(isMergingFrames()) {
				if(pos<frames.size()) {
					BookPresenter book = frames.get(pos);
					if (book!=null) {
						StringBuilder sb = new StringBuilder(24);
						sb.append("scrollToPosId('d");
						NumberToText_SIXTWO_LE(book.getId(), sb);
						sb.append("',");
						sb.append(frameAt=pos);
						sb.append(")");
						mMergedFrame.evaluateJavascript(sb.toString(), null);
					}
				}
			}
			else {
				View childAt = getChildAt(pos);
				if(childAt!=null) {
					a.scrollToWebChild(childAt);
					a.recCom.scrollTo(childAt, a);
				}
			}
		}
	}
	
	public void showJumpListDialog() {
		if(!isMergingFrames())
			frameAt = calcFrameAt();
		frameCount = frames.size();
		if(jumpListDlg==null){
			jumpListDlg = jumpListDlgRef.get();
		}
		AlertDialog dTmp = jumpListDlg;
		Bag bag;
		if(dTmp==null){
			bag = new Bag(PDICMainAppOptions.getTwoColumnSetView());
			DialogInterface.OnClickListener listener = (dlg, pos) -> {
				if(pos==-1) {
					opt.setTwoColumnJumpList(bag.val=!bag.val);
					((BaseAdapter)bag.tag).notifyDataSetChanged();
					jumpListDlg.getListView().setSelection(bag.val?frameSelection/2:frameSelection);
				}
				else {
					JumpToFrame(pos);
					dlg.dismiss();
				}
			};
			PorterDuffColorFilter cf = new PorterDuffColorFilter(0xFF2196f3, PorterDuff.Mode.SRC_IN);
			dTmp = new AlertDialog.Builder(a/*,R.style.DialogStyle*/)
				.setTitle("跳转")
				.setTitleBtn(R.drawable.ic_two_column, listener)
				.setAdapter((BaseAdapter)(bag.tag=new BaseAdapter() {
						@Override public int getCount() { return bag.val?(int)Math.ceil(frameCount/2.f):frameCount; }
						@Override public Object getItem(int position) { return null; }
						@Override public long getItemId(int pos) { return pos<frames.size()?frames.get(pos).getId():-1;}
						@Override public int getViewTypeCount() { return 2; }
						@Override public int getItemViewType(int position) {
							return bag.val?1:0;
						}
						final View.OnClickListener twoColumnLis = new OnClickListener() {
							public void onClick(View v) {
								ViewGroup sp=(ViewGroup)v.getParent();
								listener.onClick(jumpListDlg, IU.parsint(sp.getTag(), 0)*2+sp.indexOfChild(v));
							}
						};
						@NonNull @Override
						public View getView(int position, View convertView, @NonNull ViewGroup parent) {
							if(bag.val) {
								ViewGroup ret;
								if(convertView!=null){
									ret = (ViewGroup) convertView;
								} else {
									ret = (ViewGroup) a.getLayoutInflater().inflate(R.layout.singlechoice_two_column, parent, false);
								}
								for (int i = 2; i < ret.getChildCount(); i++) {
									FlowCheckedTextView mFlowTextView = (FlowCheckedTextView) ret.getChildAt(i);
									FlowTextView tv = mFlowTextView.mFlowTextView;
									if(tv.getTag()==null)
									{
										ret.getChildAt(i-2).setOnClickListener(twoColumnLis);
										tv.setText("");
									}
									int pos = position*2+i-2;
									BookPresenter book = a.getBookById(getItemId(pos));
									tv.setCompoundDrawables(a.getActiveStarDrawable(), null, null, null);
									tv.setCover(book.getCover());
									tv.setTextColor(a.AppBlack);
									tv.setStarLevel(PDICMainAppOptions.getDFFStarLevel(book.getFirstFlag()));
									mFlowTextView.setChecked(pos==frameSelection);
									mFlowTextView.setText(book==a.EmptyBook?"":book.getDictionaryName());
									boolean b1=pos==frameAt;
									if(b1 ^ mFlowTextView.getTag()!=null) {
										mFlowTextView.setTag(b1?"":null);
										if(b1) { // 下划线
											mFlowTextView.setBackgroundResource(R.drawable.text_underline);
											mFlowTextView.getBackground().setColorFilter(cf);
										} else {
											mFlowTextView.setBackground(null);
										}
									}
								}
								ret.setActivated(false);
								ret.postInvalidateOnAnimation();
								ret.setTag(position);
								convertView = ret;
							}
							else {
								FlowCheckedTextView ret;
								if(convertView!=null){
									ret = (FlowCheckedTextView) convertView;
								} else {
									ret = (FlowCheckedTextView) a.getLayoutInflater().inflate(R.layout.singlechoice_w, parent, false);
								}
								BookPresenter book = a.getBookById(getItemId(position));
								if (book!=a.EmptyBook) {
									FlowTextView tv = ret.mFlowTextView;
									tv.setCompoundDrawables(a.getActiveStarDrawable(), null, null, null);
									tv.setCover(book.getCover());
									tv.setTextColor(a.AppBlack);
									tv.setStarLevel(PDICMainAppOptions.getDFFStarLevel(book.getFirstFlag()));
									ret.setChecked(position==frameSelection);
									tv.setMaxLines(1); //todo opt
									ret.setText(book.getDictionaryName());
								} else {
									ret.setText("Error!!!");
								}
								boolean b1=position==frameAt;
								if(b1 ^ ret.getTag()!=null) {
									ret.setTag(b1?"":null);
									if(b1) {
										ret.setBackgroundResource(R.drawable.text_underline);
										ret.getBackground().setColorFilter(cf);
									} else {
										ret.setBackground(null);
									}
								}
								convertView = ret;
							}
							return convertView;
						}
					})
				, listener)
				.show();
			dTmp.setCanceledOnTouchOutside(true);
			//dTmp.mAlert.wikiBtn.getLayoutParams().width = ((int)GlobalOptions.density*50);
			
			ListView dlv = dTmp.getListView();
			dTmp.show();
			dlv.setChoiceMode(ListView.CHOICE_MODE_NONE);
			
			Window window = dTmp.getWindow();
			//window.setDimAmount(0);
			
			jumpListDlgRef = new WeakReference<>(jumpListDlg=dTmp);
			if(GlobalOptions.isDark) {
				dTmp.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			}
			dTmp.tag = bag;
		}
		else {
			bag = (Bag)dTmp.tag;
			dTmp.show();
		}
		if(!GlobalOptions.isLarge) {
			dTmp.getWindow().setLayout((int) (a.dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor)), -2);
		}
		ListView lv = dTmp.getListView();
		((AlertController.RecycleListView) lv)
				.mMaxHeight = (int) (a.root.getHeight() - a.root.getPaddingTop() - 2.8 * getResources().getDimension(R.dimen._50_) * (a.dm.widthPixels>GlobalOptions.realWidth?1:1.45));
		//d.getWindow().getDecorView().setBackgroundResource(R.drawable.popup_shadow_l);
		//d.getWindow().getDecorView().getBackground().setColorFilter(GlobalOptions.NEGATIVE);
		//d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
		a.imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
		if(isMergingFrames()) {
			mMergedFrame.evaluateJavascript("currentFrame("+frameAt+")", value -> {
				try {
					if(jumpListDlg!=null && jumpListDlg.isShowing()) {
						String[] arr=value.split("-", 2);
						if(arr.length==2) {
							long id = IU.TextToNumber_SIXTWO_LE(arr[0].substring(2));
							int pos = IU.parsint(arr[1], 0);
							//CMN.debug(value, pos, id);
							if(pos>=frames.size() || frames.get(pos).getId()!=id) {
								pos=-1;
							}
							if(pos==-1) {
								pos = frames.indexOf(id);
							}
							if(frameAt!=pos) {
								frameAt = pos;
								if(frameSelection==-1 || frameSelection>=frameCount) { //首次显示自动跳转
									frameSelection = pos;
									jumpListDlg.getListView().setSelection(bag.val?pos/2:pos);
									((BaseAdapter)jumpListDlg.getListView().getAdapter()).notifyDataSetChanged();
								}
							}
						}
					}
					//CMN.debug(value, frameAt);
				} catch (Exception e) {
					CMN.debug(e);
				}
			});
		}
		if(frameSelection==-1 || frameSelection>=frameCount) {
			//首次显示自动跳转
			frameSelection = frameAt;
			lv.setSelection(bag.val?frameAt/2:frameAt);
		}
		
		//a.resetStatusForeground(dTmp.getWindow().getDecorView());
	}
	
	public final static int WEB_LIST_MULTI=0;
	public final static int WEB_VIEW_SINGLE=1;
	int mViewMode;
	public int btmV;
	boolean isMultiRecord;
	public resultRecorderCombined multiRecord;
	public boolean multiDicts;
	public void setViewMode(resultRecorderCombined record, int bMerge, WebViewmy dictView) {
		boolean multi=record!=null;
		boolean multiDicts = multi && record.jointResult!=null && (int) record.jointResult.realmCount > 1;
		int viewMode = multi && bMerge==0? WEB_LIST_MULTI : WEB_VIEW_SINGLE;
		boolean changed = bMerge!=bMergingFrames;
		// CMN.debug("setViewMode:: ", slideDirty);
		if (slideDirty) {
			pageSlider.tapZoomV--;
			entrySeek.setEnabled(true);
			changed = true;
			slideDirty = false;
		}
		if (btmV!=SearchUI.btmV) {
			btmV = SearchUI.btmV;
			changed = true;
		}
		if (multiRecord!=record) {
			multiRecord = record;
			changed = true;
		}
		if (this.multiDicts!=multiDicts) {
			this.multiDicts=multiDicts;
			changed = true;
		}
		//CMN.debug("view::setViewMode:: changed=", changed, bMerge, isFoldingScreens());
		if(changed) {
			isMultiRecord = multi;
			mViewMode = viewMode;
			boolean showSeek;
			if (!bDataOnly) {
				boolean vis = bMerge!=1 && PDICMainAppOptions.showDictName();
				ViewUtils.setVisible(contentUIData.dictNameStroke, vis);
				ViewUtils.setVisible(contentUIData.dictName, vis);
				ViewUtils.setVisible(contentUIData.navBtns, multiDicts && bMerge!=1);
				if (multiDicts && bMerge!=1) {
					ViewUtils.setVisible(contentUIData.browserWidget14, PDICMainAppOptions.showPrvBtn());
					ViewUtils.setVisible(contentUIData.browserWidget13, PDICMainAppOptions.showNxtBtn());
				}
				ViewUtils.setVisible(contentUIData.zoomCtrl, PDICMainAppOptions.showZoomBtn());
				ViewUtils.setVisible(prv, multiDicts && PDICMainAppOptions.showPrvBtnSmall());
				ViewUtils.setVisible(nxt, multiDicts && PDICMainAppOptions.showNxtBtnSmall());
				showSeek = multiDicts && PDICMainAppOptions.showEntrySeek() && (bMerge == 2 ? PDICMainAppOptions.showEntrySeekbarFolding() : PDICMainAppOptions.showEntrySeekbar());
				ViewUtils.setVisible((View) contentUIData.navMore.getParent(), multi && PDICMainAppOptions.showMoreMenuBtnForFrames());
			} else {
				ViewUtils.setVisible(prv, multiDicts && PDICMainAppOptions.showPrvNxtBtnSmallTapSch());
				ViewUtils.setVisible(nxt, multiDicts && PDICMainAppOptions.showPrvNxtBtnSmallTapSch());
				showSeek = multiDicts && (bMerge == 2 ? PDICMainAppOptions.showEntrySeekbarTapSchFolding() : PDICMainAppOptions.showEntrySeekbarTapSch());
				bMergingFrames = bMergeFrames = bMerge;
			}
			if (showSeek) {
				ViewUtils.setVisible(entrySeek, true);
				if (!bDataOnly) {
					int pad = (int) (35 * GlobalOptions.density);
					((MarginLayoutParams) entrySeek.getLayoutParams()).leftMargin = bDataOnly?(int)(pad/2.5f):pad;
					if(bMerge==1) pad = (int) (65 * GlobalOptions.density);
					((MarginLayoutParams) entrySeek.getLayoutParams()).rightMargin = pad;
				}
			} else {
				ViewUtils.setVisible(entrySeek, false);
			}
			if(seeking) {
//				ViewUtils.preventDefaultTouchEvent(entrySeek, 0, 0);
//				seeking = false;
			}
		}
		if(this.dictView!=dictView) {
			this.dictView = dictView;
			if (dictView!=null && this==a.weblistHandler && a.thisActType==MainActivityUIBase.ActType.PlainDict) {
				((AdvancedNestScrollWebView)dictView).setNestedScrollingEnabled(PDICMainAppOptions.getEnableSuperImmersiveScrollMode());
			}
		}
	}
	
	public void changeViewMode(WebViewmy view, String url) {
		CMN.debug("changeViewMode::", isViewSingle());
		if (isViewSingle() && !isFoldingScreens()) {
			boolean vis;
			if (url.contains("merge")) { // todo correct
				//ViewUtils.setVisible(entrySeek, url.indexOf("-d", 15)>0); todo show seekbar dynamically
				vis = false;
				view.showTitleBar(false);
				bMergingFrames = 1;
			} else {
				ViewUtils.setVisible(entrySeek, false);
				vis = PDICMainAppOptions.showDictName();
				view.showTitleBar(true);
				bMergingFrames = 0;
			}
			ViewUtils.setVisible(contentUIData.dictNameStroke, vis);
			ViewUtils.setVisible(contentUIData.dictName, vis);
		}
	}
	
	public final void setViewMode() {
		setViewMode(multiRecord, isMergingFramesNum(), dictView);
	}
	
	int[] versions=new int[8];
	public void checkUI() {
		if(contentbarProject!=null && ViewUtils.checkSetVersion(versions, 0, contentbarProject.version)) {
			contentbarProject.addBar(contentUIData.bottombar2, ContentbarBtns);
			RebuildBottombarIcons(a, a.contentbar_project, a.mConfiguration);
		}
		boolean b1 = ViewUtils.checkSetVersion(versions, 3, GlobalOptions.isDark?1:0);
		if(ViewUtils.checkSetVersion(versions, 1, a.MainAppBackground) || b1) {
			contentUIData.bottombar2.setBackgroundColor(a.MainAppBackground);
			if (pageSchBar != null) {
				pageSchBar.setBackgroundColor(a.MainAppBackground);
			}
		}
		if (ViewUtils.checkSetVersion(versions, 4, a.tintListFilter.sForeground))
		{
			ViewUtils.setForegroundColor(contentUIData.bottombar2, a.tintListFilter);
			if(pageSchBar!=null) ViewUtils.setForegroundColor(pageSchBar, a.tintListFilter);
		}
		if(ViewUtils.checkSetVersion(versions, 2, a.MainPageBackground) || b1) {
			int filteredColor = GlobalOptions.isDark ? ColorUtils.blendARGB(a.MainPageBackground, Color.BLACK, a.ColorMultiplier_Web) : GlobalPageBackground;
			//if(widget12.getTag(R.id.image)==null)
			webSingleholder.setBackgroundColor(filteredColor);
			//contentUIData.webholder.setBackgroundColor(a.MainPageBackground);
			contentUIData.WHP.setBackgroundColor(filteredColor);
		}
	}
	
	public AlloydPanel alloydPanel;
	public void popupContentView(ViewGroup root, String key) {
		CMN.debug("popupContentView::", key);
		if(alloydPanel==null) {
			alloydPanel = new AlloydPanel(a, this, true);
		}
		if (!alloydPanel.isVisible()) {
			alloydPanel.toggle(root, null, -1);
		} else {
			if (!ViewUtils.isTopmost(alloydPanel.dialog, a)) {
				alloydPanel.dismissImmediate();
				alloydPanel.toggle(root, null, -1);
				CMN.debug("reshow!!!");
			}
		}
		if (!alloydPanel.isWordMap) {
			alloydPanel.AllMenus.tag = this;
			alloydPanel.toolbar.setTitle(key);
			alloydPanel.AllMenus.setItems("随机页面".equals(key) ? alloydPanel.RandomMenu : alloydPanel.PopupMenu);
		}
		alloydPanel.refresh();
		//contentUIData.webcontentlister.setPadding(0,0,0,0);
		ViewUtils.addViewToParent(alloydPanel.toolbar, contentUIData.webcontentlister, 0);
		//a.setContentBow(false);
		
		bShowingInPopup = true;
	}
	
	public void dismissPopup() {
		if(alloydPanel!=null) {
			alloydPanel.dismiss();
		}
	}
	
	public final boolean isPopupShowing() {
		return alloydPanel!=null && alloydPanel.isVisible();
	}
	
	public AppUIProject contentbarProject;
	
	public AppUIProject setUpContentView(int cbar_key, AppUIProject project) {
		if(!contentViewSetup) {
			contentViewSetup = true;
			MainActivityUIBase a = this.a;
			contentUIData.browserWidget13.setOnClickListener(this);
			contentUIData.browserWidget14.setOnClickListener(this);
			
			PorterDuffColorFilter phaedrof = new PorterDuffColorFilter(0xff888888, PorterDuff.Mode.SRC_IN);
			prv.setColorFilter(phaedrof);
			nxt.setColorFilter(phaedrof);
			
			SplitView webcontentlister = contentUIData.webcontentlister;
			webcontentlister.multiplier=-1;
			webcontentlister.isSlik=true;
			webcontentlister.setHandle(null);
			
	//		webcontentlister.setPrimaryContentSize(a.CachedBBSize,true);
	//		webcontentlister.setPageSliderInf(a.inf);
			
			contentUIData.bottombar2.setBackgroundColor(a.MainBackground);
			
			boolean tint = PDICMainAppOptions.getTintIconForeground();
			for (int i = 0; i <= 5; i++) {
				ImageView iv = (ImageView) contentUIData.bottombar2.getChildAt(i);
				ContentbarBtns[i]=iv;
				iv.setOnClickListener(a);
				if(tint) iv.setColorFilter(a.ForegroundTint, PorterDuff.Mode.SRC_IN);
				iv.setOnLongClickListener(a);
			}
			String contentkey = "ctnp#"+ cbar_key;
			String appproject = opt.getAppContentBarProject(contentkey);
			if(appproject==null) appproject="0|1|2|3|4|5|6";
			if (project==null) {
				project = a.contentbar_project;
			}
			if(project==null) {
				a.contentbar_project = project = new AppUIProject(a, contentkey, ContentbarBtnIcons, R.array.customize_ctn, appproject, contentUIData.bottombar2, ContentbarBtns);
				project.type = cbar_key;
			}
			project.addBar(contentUIData.bottombar2, ContentbarBtns);
			RebuildBottombarIcons(a, project, a.mConfiguration);
			return contentbarProject = project;
		}
		return contentbarProject;
	}
	
	public boolean isWeviewInUse(ViewGroup someView) {
		ViewParent sp = someView.getParent();
		if(sp==null) return false;
		if(ViewUtils.isVisibleV2(webSingleholder) && sp==webSingleholder) {
			return true;
		}
		if(ViewUtils.isVisibleV2(contentUIData.WHP) && ViewUtils.isVisibleV2(contentUIData.webholder) && sp==contentUIData.webholder) {
			return true;
		}
		return false;
	}
	
	/** false:using old list technique. displaying multiple webviews in the linearlayout <br/>
	 * true : displaying merged multiple results or simply one page. */
	public final boolean isViewSingle() {
		return mViewMode==WEB_VIEW_SINGLE;
	}
	
	/** displaying one or multiple records from multiple dictionary in search-all mode */
	public final boolean isMultiRecord() {
		return isMultiRecord;
	}
	
	int cc;
	boolean inlineJump;
	private WebView jumper;
	
	public Toolbar pageSchBar;
	public EditText pageSchEdit;
	TextWatcher pageSchWat;
	int etFlag;
	TextView pageSchIndicator;
	TextView pageSchDictIndicator;
	String MainPageSearchetSearchStartWord;
	boolean HiFiJumpRequested;
	
	public void jumpHighlight(int d, boolean calcIndicator){
		try {
			cc=0;
			inlineJump=true;
			do_jumpHighlight(d, calcIndicator);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	String schPageDid;
	int schPagePos, schPageSz, schPageTZ;
	@AnyThread
	public void updateInPageSch(WebViewmy wv, String did, int keyIdx, int keyz, int total) {
		CMN.debug("updateInPageSch", "did = [" + did + "], keyIdx = [" + keyIdx + "], keyz = [" + keyz + "], total = [" + total + "]");
		if(did==null) {
			BookPresenter book = (wv==null?getWebContextNonNull():wv).presenter;
			did = book.isMergedBook()?"":book.idStr;
		}
		boolean b1 = !TextUtils.equals(schPageDid, did);
		if (b1 ||  schPagePos!=keyIdx
				|| schPageSz!=keyz
				|| schPageTZ!=total
		) {
			if (b1) schPageDid = did;
			else if(!PDICMainAppOptions.schPageEditShowCurrentPos()
				&& schPageTZ==total) {
				return;
			}
			schPagePos=keyIdx;
			schPageSz=keyz;
			if(total!=-100 && isViewSingle()) schPageTZ=total;
			if (pageSchIndicator.getTag() == null) {
				pageSchIndicator.setTag((Runnable) () -> {
					String text;
					if(schPagePos>=0 && PDICMainAppOptions.schPageEditShowCurrentPos()) {
						if(schPageTZ>schPageSz)
							text = (schPagePos+1)+"/"+schPageSz+" ("+ schPageTZ +")";
						else
							text = (schPagePos+1)+"/"+schPageSz;
					} else {
						text = ""+ schPageTZ;
					}
					pageSchIndicator.setText(text);
					text = (PDICMainAppOptions.schPageEditShowDictName() ? schPageDid : null);
					if (pageSchDictIndicator.getTag()!=text) {
						pageSchDictIndicator.setTag(text);
						try {
							pageSchDictIndicator.setText(TextUtils.isEmpty(text)?text:new File(a.getBookNameByIdNoCreation(IU.TextToNumber_SIXTWO_LE(new CharSequenceKey(text, 1)))).getName());
						} catch (Exception e) {
							pageSchDictIndicator.setText("");
							CMN.debug(e);
						}
					}
				});
			}
			pageSchIndicator.post((Runnable) pageSchIndicator.getTag());
		}
	}
	
	public boolean hasPageKey() {
		return ViewUtils.isVisibleV2(pageSchBar) && TextUtils.getTrimmedLength(pageSchEdit.getText())>0;
	}
	
	/** 页面加载完成后，是否自动开始执行页内搜索，并跳转至第一处高亮。 */
	public boolean schPage(WebViewmy mWebView) {
		return hasPageKey() && (isViewSingle() || mWebView==webholder.getChildAt(0));
	}
	
	public void textMenu(WebViewmy mWebView) {
		if (mWebView==null || (bDataOnly?PDICMainAppOptions.tapSchShowToolsBtn():PDICMainAppOptions.wvShowToolsBtn())) {
			toolsBtn.setTag(mWebView);
			ViewUtils.setVisible(toolsBtn, mWebView!=null);
			initQuickTranslatorsBar(mWebView!=null && true, false);
		}
	}
	
	/** show this hide another */
	public void viewContent() {
		//CMN.Log("viewContent::", mViewMode);
		int mode = mViewMode;
		mViewMode = (mViewMode+1)%2;
		if(ViewUtils.isVisible(this)) {
			setVisibility(View.GONE);
			// removeAllViews();
		}
		mViewMode = mode;
		if(!ViewUtils.isVisible(this)) {
			setVisibility(View.VISIBLE);
		}
	}
	
	public void setFetchWord(int mode) {
		if (mode == -2) {
			if (this != a.weblistHandler) {
				// 是 DBrower 的页面处理者，只有两态切换哦
				boolean v = !PDICMainAppOptions.dbCntFetcingWord();
				PDICMainAppOptions.dbCntFetcingWord(v);
				setFetchWord(v ? 2 : 0);
			}
			else {
				MenuItemImpl tagHolder = a.getMenuSTd(R.id.fetchWord);//alloydPanel.fetchWordMenu;
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(tagHolder.tag);
				if(dd==null) {
					DialogInterface.OnClickListener	listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							WebViewListHandler wlh = (WebViewListHandler)((AlertDialog)dialog).tag;
							if(which==2) which = 0;
							else which++;
							if (which>=0) {
								wlh.setFetchWord(which);
							}
							dialog.dismiss();
						}
					};
					dd = new AlertDialog.Builder(a)
							.setSingleChoiceLayout(R.layout.singlechoice_plain)
							.setSingleChoiceItems(new String[]{
									"直接取词"
									, "点击翻译"
									, "关闭"
							}, 0, listener)
							.setWikiText("在页面上点击链接进行搜索", null)
							.setTitle("设置取词模式").show();
					tagHolder.tag = null;
				}
				a.showMenuDialog(tagHolder, this, dd);
			}
		} else {
			if (mode == -1) {
				mode = lastFetchWord;
			}
			if (fetchWord != mode) {
				fetchWord = mode;
				if (alloydPanel!=null) {
					alloydPanel.fetchWordMenu.setChecked(mode > 0);
				}
				WebViewmy wv = getWebContext();
				if (wv!=null) {
					if (mode > 0) {
						lastFetchWord = mode;
						wv.evaluateJavascript("window.randx_mode=" + mode, null);
						wv.evaluateJavascript(MainActivityUIBase.randx_on, null);
					} else {
						wv.evaluateJavascript(MainActivityUIBase.randx_off, null);
					}
				}
			}
		}
	}
	
	public boolean togTapSch() {
		tapSch = !tapSch;
		if (tapSch) {
			shezhi |= 1;
		} else {
			shezhi &= ~1;
		}
		evalJsAtAllFrames(tapSch?
				// "window.shzh|=1;if(!window.tpshc)loadJs('/mdbr/tapSch.js')"
				"window.shzh|=1;if(!window.tpshc)app.loadJs(sid.get(), 'tapSch.js')"
				:"window.shzh&=~1");
		return tapSch;
	}
	
	public void togZhTrans(int zhTrans, WebViewmy mWebView) {
		this.zhTrans = zhTrans;
		String js = "var w=window;function cb(){zh_tran("+zhTrans+");}if(w.zh_tran)cb();else try{loadJs('//mdbr/zh.js', cb)}catch(e){w.loadJsCb=cb;app.loadJs(sid.get(),'zh.js');}";
		if (mWebView == null) {
			evalJsAtAllFrames(js);
		} else {
			mWebView.evaluateJavascript(js, null);
		}
		if (this==a.weblistHandler) {
			PDICMainAppOptions.webZhTranslate(zhTrans);
		}
	}
	
	public void updateTapSel(int value) {
		if (tapSel != value) {
			opt.putInt("tapSel", value);
			tapSel = value;
			shezhi &= ~6;
			shezhi |= value;
			String eval = "window.shzh&=~6";
			if (value>0) {
				eval += ";window.shzh|="+value+";if(!window.tpshc)app.loadJs(sid.get(), 'tapSch.js')";
			}
			evalJsAtAllFrames(eval);
		}
	}
	
	public void evalJsAtAllFrames(String exp) {
		if (bDataOnly) {
			getMergedFrame().evaluateJavascript(exp, null);
		} else {
			evalJsAtAllFrames_internal(webSingleholder, exp);
			evalJsAtAllFrames_internal(webholder, exp);
		}
	}
	
	private void evalJsAtAllFrames_internal(ViewGroup vg, String exp) {
		for (int index = 0; index < vg.getChildCount(); index++) {
			if(vg.getChildAt(index) instanceof LinearLayout){
				ViewGroup webHolder = (ViewGroup) vg.getChildAt(index);
				View child = webHolder.getChildAt(1);
				if(child instanceof WebViewmy){
					((WebViewmy) child).evaluateJavascript(exp,null);
				}
			}
		}
	}
	
	public void unload() {
		webSingleholder.removeAllViews();
		webholder.removeAllViews();
		if (mMergedFrame != null) {
			mMergedFrame.shutDown();
		}
		if (dictView != null) {
			dictView.shutDown();
		}
		if (scrollFocus != null) {
			scrollFocus.shutDown();
		}
	}
	
	public void showMoreToolsPopup(View v) {
		try {
			WebViewmy wv = getWebContextNonNull();
			if (pageSlider != null) {
				pageSlider.OrgX = v.getWidth();
				pageSlider.OrgY = v.getHeight();
			}
			wv.presenter.showMoreToolsPopup(wv, v);
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	public void vartakelayaTowardsDarkMode() {
		ViewGroup vg = getViewGroup();
		for(int i=0;i<vg.getChildCount();i++) {
			View ca = vg.getChildAt(i);
			if (ca!=null && ca.getTag() instanceof WebViewmy) {
				BookPresenter mdTmp = ((WebViewmy) ca.getTag()).presenter;
				mdTmp.vartakelayaTowardsDarkMode(null);
			}
		}
	}
	
	public void updateContentMenu(List<MenuItemImpl> menuItems) {
		if (menuItems == null) {
			menuItems = contentMenu();
		}
		if (menuItems!=null) {
			MenuItem m = ViewUtils.findInMenu(menuItems, R.id.tapSch);
			if(m!=null) m.setChecked(tapSch && !tapDef);
			m = ViewUtils.findInMenu(menuItems, R.id.tapSch1);
			if(m!=null) m.setChecked(tapSch && tapDef);
		}
	}
	
	public static class HighlightVagranter {
		ViewGroup webviewHolder;
		int HlightIdx;
		int AcrArivAcc;
//		public HighlightVagranter(ViewGroup webviewHolder) {
//			this.webviewHolder = webviewHolder;
//		}
	}
	
	void resetLights(ViewGroup webviewHolder, int d) {
		if(webviewHolder!=null) {
			int max = webviewHolder.getChildCount();
			String exp = "resetLight(" + d + ")";
			for (int index = 0; index < max; index++) {
				if (webviewHolder.getChildAt(index) instanceof LinearLayout) {
					ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
					if (webHolder.getChildAt(1) instanceof WebView) {
						((WebView) webHolder.getChildAt(1))
								.evaluateJavascript(exp, null);
					}
				}
			}
		}
	}
	
	void clearLights(ViewGroup webviewHolder){
		schPageTZ = 0;
		if(webviewHolder!=null){
			int max=webviewHolder.getChildCount();
			String exp="clearHighlights()";
			for (int index = 0; index < max; index++) {
				if(webviewHolder.getChildAt(index) instanceof LinearLayout){
					ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
					if(webHolder.getChildAt(1) instanceof WebView){
						((WebView)webHolder.getChildAt(1))
								.evaluateJavascript(exp,null);
					}
				}
			}
		}
	}
	
	public void onHighlightReady(int idx, int number) {
		final HighlightVagranter hData = getHData();
		ViewGroup webviewHolder = hData.webviewHolder;
		ViewGroup vg = hData.webviewHolder;
		View v = vg.getChildAt(idx);
		if(v!=null){
			v.setTag(R.id.numberpicker, number);
		}
		int all=0;
		for (int i = 0; i < vg.getChildCount(); i++) {
			all+=IU.parseInteger(vg.getChildAt(i).getTag(R.id.numberpicker),0);
		}
		schPageTZ = all;
		String finalAll = all==0?"":""+all;
		CMN.debug("schPageTZ::", schPageTZ);
		pageSchIndicator.post(() -> pageSchIndicator.setText(finalAll));
		if (v != null && HiFiJumpRequested && idx == 0) {
			a.jumpNaughtyFirstHighlight(v.findViewById(R.id.webviewmy));
			HiFiJumpRequested = false;
		}
	}
	
	
	private HighlightVagranter hDataMergedPage = new HighlightVagranter(); // use merged view
	private HighlightVagranter hDataSinglePage = new HighlightVagranter(); // use dict view
	private HighlightVagranter hDataMultiple = new HighlightVagranter();  // use dict view, for multiple frames
	
	public WebViewmy dictView;
	
	/** <br>single merged - hDataMergedPage
	* <br> single dict - hDataPage in the wv
	* <br> multiple merged - hDataMergedPage
	* <br> multiple dict - hDataMultiple**/
	public HighlightVagranter getHData() {
		if(isMergingFrames()) {
			hDataMergedPage.webviewHolder = webSingleholder;
			return hDataMergedPage;
		}
		if(isViewSingle()) {
			if(dictView==null) CMN.Log("ERROR_getHData!");
			HighlightVagranter data = dictView == null ? getMergedFrame().hDataPage : dictView.hDataPage;
			data.webviewHolder = webSingleholder;
			return data;
		} else {
			hDataMultiple.webviewHolder = contentUIData.webholder;
			return hDataMultiple;
		}
	}
	
	/** 汉星照耀，汉水长流！ */
	private void do_jumpHighlight(int d, boolean calcIndicator) {
		final HighlightVagranter hData = getHData();
		CMN.Log("jumpHighlight... dir="+d+" framePos="+hData.HlightIdx);
		ViewGroup webviewHolder = hData.webviewHolder;
		int max = webviewHolder.getChildCount();
		a.fadeSnack();
		boolean b1=hData.HlightIdx>=max,b2=hData.HlightIdx<0;
		if(b1||b2) {
			hData.AcrArivAcc++;
			if(b1&&d==-1) {
				hData.HlightIdx=max-1;
				b1=false;
			}
			else if(b2&&d==1){
				hData.HlightIdx=0;
				b2=false;
			}
			if(hData.AcrArivAcc<=2){
				//CMN.Log("do_jumpHighlight", PDICMainAppOptions.getInPageSearchShowNoNoMatch(), calcIndicator);
				if(PDICMainAppOptions.schPageShowHints() || calcIndicator) {
					String msg = getResources().getString(R.string.search_end, d < 0 ? "⬆" : "", d > 0 ? "⬇" : "");
					a.getTopSnackView().setNextOffsetScale(0.25f);
					a.showTopSnack(null, msg, 0.75f, -1, Gravity.CENTER, 0);
				}
				return;
			} else {
				hData.AcrArivAcc=0;
			}
		} else {
			hData.AcrArivAcc =0;
		}
		if(b1){
			resetLights(webviewHolder, d);
			hData.HlightIdx=0;
			if(d==-1){
				a.evalJsAtFrame(max,"setAsEndLight("+d+");");
			}
		}
		else if(b2){
			resetLights(webviewHolder, d);
			hData.HlightIdx=max-1;
			if(d==1){
				if(hData.HlightIdx>=0) a.evalJsAtFrame(0,"setAsStartLight("+d+");");
			}
		}
		if(hData.HlightIdx<0) hData.HlightIdx=0;
		ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(hData.HlightIdx);
		if(webHolder!=null){
			WebView wv = (WebView) ViewUtils.findViewById(webHolder, R.id.webviewmy);
			if(wv!=null){
				if(jumper!=null && jumper!=wv){
					jumper.evaluateJavascript("quenchLight()",null);
				}
				jumper=wv;
				if(cc>0) inlineJump=false;
				CMN.Log("jumpHighlight_evaluating...", inlineJump);
				jumper.evaluateJavascript(new StringBuilder(28).append("jumpTo(")
						.append(d).append(',')//direction
						.append(-1).append(',')//desired offset
						.append(0).append(',')//frameAt
						.append(0).append(',')//HlightIdx
						.append(cc>0).append(',')//need reset
						.append(0)//topOffset_frameAt
						.append(");").toString(), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						CMN.Log("jumpHighlight_delta_yield : ", value);
						if(value!=null) {
							int d = 0; boolean b1;
							if(!(b1=value.startsWith("\"")))
								d = IU.parsint(value, 0);
							if (d != 0) {
								hData.HlightIdx += d;
								if (hData.HlightIdx < 0 || hData.HlightIdx >= max) {
									hData.AcrArivAcc++;
								}
								do_jumpHighlight(d, calcIndicator);
							}
//							else if(calcIndicator && b1 && webviewHolder!=null) {
//								int all=0;
//								int preAll=IU.parsint(value.substring(1,value.length()-1),0);
//								if(preAll>=0) {
//									for (int i = 0; i < webviewHolder.getChildCount(); i++) {
//										View v = webviewHolder.getChildAt(i);
//										if (v != null) {
//											if (i == hData.HlightIdx)
//												preAll += all;
//											all += IU.parseInteger(v.getTag(R.id.numberpicker), 0);
//										}
//									}
//									//111 (PeruseSearchAttached()? peruseView.PerusePageSearchindicator:MainPageSearchindicator).setText((preAll+1)+"/"+all);
//								}
//							}
						}
					}
				});
				cc++;
			}
		}
	}
	
	void togSchPage(int forceShow) {
		final HighlightVagranter hData = getHData();
		hData.HlightIdx =
		hData.AcrArivAcc = 0;
		ViewGroup webviewHolder = hData.webviewHolder;
		Toolbar bar = pageSchBar;
		if (bar == null) {
			bar = (Toolbar) a.getLayoutInflater().inflate(R.layout.searchbar, a.root, false);
			bar.setNavigationIcon(R.drawable.abc_ic_clear_material);//abc_ic_ab_back_mtrl_am_alpha
			EditText etSearch = bar.findViewById(R.id.etSearch);
			//etSearch.setBackgroundColor(Color.TRANSPARENT);
			etSearch.setText(MainPageSearchetSearchStartWord);
			//if(GlobalOptions.debug) etSearch.setText("观点");
			etSearch.addTextChangedListener(pageSchWat = new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
				@Override
				public void afterTextChanged(Editable s) {
					if (PDICMainAppOptions.schPageOnEdit() || s==null) {
						String text = etSearch.getText().toString().replace("\\", "\\\\");
						HiFiJumpRequested=PDICMainAppOptions.schPageAutoType();
						SearchOnPage(text);
					} else if(etFlag!=1){
						etFlag = 1;
					}
				}
			});
			etSearch.setOnEditorActionListener((v, actionId, event) -> {
				if (!PDICMainAppOptions.schPageOnEdit()) {
					pageSchWat.afterTextChanged(null);
				}
				return true;
			});
			
			ViewUtils.ResizeNavigationIcon(bar);
			
			bar.setContentInsetsAbsolute(0, 0);
			if(!a.isMultiShare())bar.setLayoutParams(a.toolbar.getLayoutParams());
			bar.setBackgroundColor(a.MainAppBackground);
			bar.findViewById(R.id.ivDeleteText).setOnClickListener(this);
			bar.findViewById(R.id.enter).setOnClickListener(this);
			this.pageSchBar = bar;
			this.pageSchEdit = etSearch;
			this.pageSchIndicator = bar.findViewById(R.id.indicator);
			this.pageSchDictIndicator = bar.findViewById(R.id.dictName);
			bar.getChildAt(0).addOnLayoutChangeListener(new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					pageSchDictIndicator.setTranslationX(etSearch.getWidth()+GlobalOptions.density*2);
				}
			});
			ViewUtils.setOnClickListenersOneDepth(bar, this, 999, null);
			bar.setOnDragListener( (v, event) -> {
				if(event.getAction()== DragEvent.ACTION_DROP){
					try {
						ClipData textdata = event.getClipData();
						if(textdata.getItemCount()>0){
							CharSequence text = textdata.getItemAt(0).getText();
							if(text!=null) {
								etSearch.setText(text);
								if(!PDICMainAppOptions.schPageOnEdit()) {
									pageSchWat.afterTextChanged(null);
								}
							}
						}
						return false;
					} catch (Exception e) { }
				}
				return true;
			});
			ViewUtils.setForegroundColor(pageSchBar, a.tintListFilter);
		}
		boolean b1=bar.getParent()==null||forceShow>=1;
		if (b1) {
			CMN.debug("添加到视图中去");
			boolean bottom = PDICMainAppOptions.schpageAtBottom() && a.thisActType == MainActivityUIBase.ActType.PlainDict;
			int idx =  bottom ? 1 : 0;
			if(contentUIData.webcontentlister.indexOfChild(bar)!=idx)
				VU.removeView(bar);
			contentviewAddView(bar, idx);
			if (bottom) {
				a.setSoftInputMode(a.softModeStd=a.softModeResize);
			}
			if((forceShow==0||forceShow==2) && PDICMainAppOptions.schpageAutoKeyboard()) {
				ViewUtils.setVisible(contentUIData.bottombar2, false); // todo 点击edit时总是提前隐藏，避免闪跳。
				pageSchEdit.postDelayed(() -> {
					pageSchEdit.requestFocus();
					a.imm.showSoftInput(pageSchEdit, InputMethodManager.SHOW_IMPLICIT);
				}, 100);
			}
			bar.setTag(pageSchEdit.getText());
			SearchOnPage(null);
			bar.getLayoutParams().height = a.actionBarSize;
		}
		else {
			if (ViewUtils.removeView(bar)) {
				a.imm.hideSoftInputFromWindow(a.root.getWindowToken(), 0);
			}
			clearLights(webviewHolder);
			bar.setTag(null);
			if (PDICMainAppOptions.schpageAutoKeyboard() && contentUIData.bottombar2.getVisibility()!=View.VISIBLE) {
				ViewUtils.setVisible(contentUIData.bottombar2, true);
			}
		}
		
		CMN.debug("bar.getLayoutParams().height", bar.getLayoutParams().height);
		
		if (src==SearchUI.MainApp.MAIN) {
			opt.schPage(b1);
		} else if (src== SearchUI.FloatSch.MAIN) {
			//PDICMainAppOptions.schPageFlt(b1);
		} else if (src==SearchUI.Fye.MAIN) {
			opt.schPageFye(b1);
		}
	}
	
	// click
	@Override
	public void onClick(View v) {
		int id=v.getId();
		switch (id) {
			case R.id.home:
				togSchPage(0);
				a.imm.hideSoftInputFromWindow(pageSchEdit.getWindowToken(), 0);
				break;
			case R.id.ivDeleteText:
				pageSchEdit.setText(null);
				break;
			case R.id.enter:
				showPageSchTweaker();
				break;
			//in page search navigation
			case R.id.recess:
			case R.id.forward:{
				if(v.getTag()!=null){
					return;
				}
				if((etFlag&1)!=0) {
					pageSchWat.afterTextChanged(null);
					etFlag = 0;
				} else {
					boolean nxt=id==R.id.recess;
					//CMN.Log("下一个");
					//111
					if(!PDICMainAppOptions.schpageAtBottom() && PDICMainAppOptions.schPageNavHideKeyboard()){
						//a.imm.hideSoftInputFromWindow((a.PeruseSearchAttached()? a.peruseView.PerusePageSearchetSearch:MainPageSearchetSearch).getWindowToken(), 0);
						a.imm.hideSoftInputFromWindow(pageSchEdit.getWindowToken(), 0);
					}
					jumpHighlight(nxt?1:-1, true);
				}
			} break;
			case R.id.nav_more:{
				WebViewmy wv = getWebContext();
				if (wv!=null) {
					wv.presenter.showMoreToolsPopup(wv, v);
				}
			} break;
			case R.id.nav_back:
			case R.id.nav_forward:{
				NavWeb(id==R.id.nav_back?-1:1);
			} break;
			/* 上下导航 */
			case R.id.prv:
			case R.id.nxt:
			case R.id.browser_widget14:
			case R.id.browser_widget13:{
				prvnxtFrame(id==R.id.browser_widget13||id==R.id.nxt);
			} break;
			/* 工具 */
			case R.id.tools:{
				if (v.getTag() instanceof WebViewmy) {
					final WebViewmy wv = ((WebViewmy) v.getTag());
					wv.presenter.invokeToolsBtn(wv, PDICMainAppOptions.toolsQuick()?PDICMainAppOptions.toolsQuickAction():-1);
				}
			} break;
			case R.id.zoomIn:
			case R.id.zoomOut:{
				if (dictView!=null) {
					pageSlider.bSuppressNxtTapZoom = CMN.now();
					if (id == R.id.zoomIn) {
						dictView.zoomIn();
					} else {
						dictView.zoomOut();
					}
				}
			} break;
		}
	}
	
	// longclick
	@Override
	public boolean onLongClick(View v) {
		if (v==toolsBtn) {
			int act = PDICMainAppOptions.toolsQuickLong();
			if (act==1) {
				invokeToolsBtn(false, PDICMainAppOptions.toolsQuickAction());
				return true;
			} else if (act==2) {
				invokeToolsBtn(true, 0);
			} else if (act==3) {
				invokeToolsBtn(false, -1);
				a.getVtk().bPicking = 2;
			}
			return true;
		}
		switch (v.getId()) {
			case R.id.nav_back:
			case R.id.nav_forward:{
				WebViewmy wv = getWebContext();
				if (wv !=null) {
					wv.presenter.showMoreToolsPopup(wv, v);
				}
			} return true;
		}
		return false;
	}
	
	WeakReference<AlertDialog> SchTweaker = EmptyRef;
	public static int schPageOptPaneSY;
	// 页内搜索设定
	public void showPageSchTweaker() {
		AlertDialog dlg = SchTweaker.get();
		if (dlg == null || dlg.isDark!=GlobalOptions.isDark) {
			a.weblist = this;
			szStash = shezhi;
			final SettingsPanel settings = new SettingsPanel(a, opt
					, new String[][]{
						new String[]{"正则搜索", "使用正则表达式"}
						, new String[]{"普通搜索", "使用通配符", "以空格划分关键词", "通配符不匹配空格", "通搜变音字母"}
						, new String[]{"通用", "区分大小写", "始终自动跳转"/*, "从列表查看内容页面时，自动跳转"*/, "打字时自动搜索", "打字时自动跳转",}
						, new String[]{"视图设置", /*"翻页时自动跳转"*/ "自动弹出键盘"}
						, new String[]{"搜索框位置", "页面顶部", "页面底部"} // 显示位置
						, new String[]{"搜索框旁数字标志", "显示当前高亮序号", "显示词典名称"} // 显示位置
					}
					, new int[][]{new int[]{Integer.MAX_VALUE /** see{@link BookPresenter#MakePageFlag} */
							, makeInt(101, 4, false) // pageSchUseRegex
						}
						, new int[]{Integer.MAX_VALUE
							, makeInt(101, 8, false) // pageSchWild
							, makeInt(101, 6, false) // pageSchSplitKeys
							, makeInt(101, 7, false) // pageSchWildMatchNoSpace
							, makeInt(101, 9, false) // pageSchDiacritic
						}
						, new int[]{Integer.MAX_VALUE
							, makeInt(101, 5, false) // pageSchCaseSensitive
							, makeInt(9, 8, true) // pageSchAutoJump
							, makeInt(6, 27, true) // schPageOnEdit
							, makeInt(2, 56, false) // schPageAutoType
						}
						, new int[]{Integer.MAX_VALUE
//							, makeInt(2, 55, false) // schPageAutoTurn
							//, makeInt(2, 58, false) // schPageNavAudioKey
							, makeInt(8, 24, true) // schpageAutoKeyboard
						}
						, new int[]{Integer.MAX_VALUE
							, makeInt(8, 25, false) // makeDynInt(1, 1, !PDICMainAppOptions.schpageAtBottom())
							, makeInt(8, 25, true) // makeDynInt(1, 2, PDICMainAppOptions.schpageAtBottom())
						}
						, new int[]{Integer.MAX_VALUE
							, makeInt(8, 43, true) // schPageEditShowDictName
							, makeInt(8, 42, true) // schPageEditShowCurrentPos
						}
					}, null);
			settings.init(a, a.root);
			settings.setActionListener(new SettingsPanel.ActionListener() {
				@Override
				public boolean onAction(View v, SettingsPanel settingsPanel, int flagIdx, int flagPos, boolean dynamic, boolean val, int storageInt) {
					if (flagIdx == 101) {
						if (flagPos == 4) {
							for (int i = 6; i <= 9; i++)
								settings.settingsLayout.findViewById(makeInt(101, i, false)).setAlpha(val ? 0.5f : 1);
						}
					}
					if (flagIdx == 8) {
						if (flagPos == 42 || flagPos == 43) {
							updateInPageSch(null, schPageDid, schPagePos, schPageSz, -100);
						} else if (flagPos == 25) {
							val = !PDICMainAppOptions.schpageAtBottom();
							ViewGroup vg = (ViewGroup) v.getParent();
							((Checkable) vg.findViewById(makeInt(8, 25, false))).setChecked(val);
							((Checkable) vg.findViewById(makeInt(8, 25, true))).setChecked(!val);
							togSchPage(1);
						}
					}
					return true;
				}
				
				@Override
				public void onPickingDelegate(SettingsPanel settingsPanel, int flagIdx, int flagPos, int lastX, int lastY) {
				}
			});
			if (PDICMainAppOptions.pageSchUseRegex()) {
				settings.onAction(null, 101, 4, false, true, 0);
			}
			Framer f = new Framer(a);
			f.mMaxHeight = (int) (15 * ((RadioSwitchButton) settings.settingsLayout.findViewById(makeInt(101, 4, false))).getLineHeight() + 15 * GlobalOptions.density);
			f.addView(settings.settingsLayout);
			dlg = new AlertDialog.Builder(a, GlobalOptions.isDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line)
					.setTitle("页内搜索设置")
					.setView(f)
					.setPositiveButton(R.string.confirm, null)
//					.setTitleBtn(R.drawable.ic_search_large, (dialog, which) -> {
//						pageSchWat.afterTextChanged(null);
//						dialog.dismiss();
//					})
					.setOnDismissListener(new DialogInterface.OnDismissListener() {;
						@Override
						public void onDismiss(DialogInterface dialog) {
							//CMN.debug("Flag::页内搜索设置::pageSchSplitKeys::", shezhi&0x40);
							if (szStash != shezhi) {
								ViewGroup webviewHolder = getViewGroup();
								int cc = webviewHolder.getChildCount();
								String val = "window.shzh=" + shezhi + "; _highlight(null);";
								BookPresenter.SavePageFlag(shezhi);
								for (int i = 0; i < cc; i++) {
									if (webviewHolder.getChildAt(i) instanceof LinearLayout) {
										ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(i);
										if (webHolder.getChildAt(1) instanceof WebView) {
											WebViewmy wv = (WebViewmy) webHolder.getChildAt(1);
											wv.evaluateJavascript(val, null);
										}
									}
								}
							}
							schPageOptPaneSY = settings.settingsLayout.getScrollY();
						}
					})
					.create();
			dlg.tag = settings;
			SchTweaker = new WeakReference<>(dlg);
			if (schPageOptPaneSY > 0) {
				ViewGroup v = settings.settingsLayout;
				v.post(() -> v.setScrollY(schPageOptPaneSY));
			}
		} else {
			((SettingsPanel) dlg.tag).refresh();
		}
		dlg.show();
		dlg.getWindow().setDimAmount(0);
		ViewUtils.ensureWindowType(dlg, a, dlg.dismissListener);
		ViewUtils.ensureTopmost(dlg, a, dlg.dismissListener);
	}
	
	void SearchOnPage(String text) {
		final HighlightVagranter hData = getHData();
		ViewGroup vg = hData.webviewHolder;
		{
			if(text!=null)
				try {
					text=true?text: URLEncoder.encode(text,"utf8");
				} catch (UnsupportedEncodingException ignored) { }
			String val = text==null?"_highlight(null)":"_highlight(\""+ text.replace("\"","\\\"")+"\")";
			if(vg!=null){
				int cc = vg.getChildCount();
				for (int i = 0; i < cc; i++) {
					if(vg.getChildAt(i) instanceof LinearLayout){
						ViewGroup webHolder = (ViewGroup) vg.getChildAt(i);
						if(webHolder.getChildAt(1) instanceof WebView){
							((WebView)webHolder.getChildAt(1))
									.evaluateJavascript(val,null);
						}
					}
				}
			}
//			if(popupContentView!=null && popupContentView.getParent()!=null){
//				popupWebView.evaluateJavascript(val,null);
//			}//111
		}
	}
	
	public void scrollHighlight(int o, int d) {
		final HighlightVagranter hData = getHData();
		ViewGroup webviewHolder = hData.webviewHolder;
		//CMN.Log("scrollHighlight",o,d,inlineJump);
		if(webviewHolder!=null && webviewHolder.getChildAt(hData.HlightIdx) instanceof LinearLayout){
			ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(hData.HlightIdx);
			WebViewmy wv = (WebViewmy) ViewUtils.findViewById(webHolder, R.id.webviewmy);
			if(wv!=null){
				int pad=(int) (25*GlobalOptions.density);
				if(!isViewSingle()){
					//CMN.Log("???");
					WHP.performLongClick();
					WHP.onTouchEvent(MotionEvent.obtain( 1000,/*小*/
							1000,/*样，*/
							MotionEvent.ACTION_UP,/*我还*/
							0,/*治*/
							0,/*不了*/
							0));/*你？*/
					if(o==-1){
						if(d==-1 || inlineJump) {
							return;
						}
					}
					o=(int)(o*a.dm.density);
					o+=webHolder.getTop()+wv.getTop();
					//CMN.Log("??????", o);
					if(o<=WHP.getScrollY() || o+pad>=WHP.getScrollY()+WHP.getHeight()){
						//CMN.Log("do_scrollHighlight",o,d,o-pad);
						WHP.smoothScrollTo(0, o-pad);
					}
				}
				else{
					if(o==-1){
						if(d==-1 || inlineJump) {
							return;
						}
					}
					o= (int) ((o*GlobalOptions.density)*(wv.webScale / GlobalOptions.density));
					if(o<=wv.getScrollY() || o+pad>=wv.getScrollY()+wv.getHeight()){
						int finalO=o-pad;
						//CMN.debug("scrolling !!!", finalO, wv.getScrollY(), wv.getScrollY()+wv.getHeight());
						wv.post(() -> {
							//CMN.debug("do scrolling !!!");
							MainActivityUIBase.layoutScrollDisabled=false;
							wv.scrollTo(0, finalO);
							wv.requestLayout();
							a.NaugtyWeb=(WebViewmy) wv;
							if(a.hdl!=null)
								a.hdl.sendEmptyMessage(778898);
						});
					}
				}
			}
		}
	}
	
	public void contentviewAddView(View v, int i) {
		ViewUtils.addViewToParent(v, contentUIData.webcontentlister, i);
	}
	
	public final int getSrc() {
		return src;
	}
	
	public final boolean isMergingFrames() {
		return bMergingFrames==1;
	}
	
	public final int isMergingFramesNum() {
		return bMergingFrames;
	}
	
	public final boolean isFoldingScreens() {
		return bMergingFrames==2;
	}
	
	public void setStar(String key) {
		if (!TextUtils.equals(displaying, key)) {
			displaying = key;
			if (key!=null) {
				if (ViewUtils.isVisibleV2(browserWidget8)) {
					if (a.DBrowser!=null && this==a.DBrowser.weblistHandler && a.DBrowser.getFragmentType()==DB_FAVORITE) {
						browserWidget8.setActivated(!a.DBrowser.toDeleteV2.contains(a.DBrowser.currentRowId));
					} else {
						browserWidget8.setActivated(a.GetIsFavoriteTerm(key));
					}
				}
			}
			boolean b1=key==null;
			if (browserWidget8.isEnabled() == b1) {
				browserWidget8.setEnabled(!b1);
				browserWidget8.setAlpha(b1?0.5f:1);
			}
			if (bShowingInPopup) {
				alloydPanel.toolbar.setTitle(key);
			}
		}
	}
	
	public String getMultiRecordKey() {
		additiveMyCpr1 ret = null;
		if (multiRecord!=null) {
			if (isViewSingle()) {
				ret = getMergedFrame().jointResult;
			} else {
				ret = jointResult;
			}
		}
		return ret == null ? "Error!!!" : ret.key;
	}
	
	public void setBottomNavWeb(boolean nav) {
		if (!bDataOnly && nav!=bottomNavWeb) {
			if (nav) {
				contentUIData.browserWidget10.setImageResource(R.drawable.chevron_recess);
				contentUIData.browserWidget11.setImageResource(R.drawable.chevron_forward);
			} else {
				contentUIData.browserWidget10.setImageResource(R.drawable.chevron_left);
				contentUIData.browserWidget11.setImageResource(R.drawable.chevron_right);
			}
			if (bShowingInPopup) {
				PDICMainAppOptions.bottomNavWeb(nav);
			} else if(this==a.weblistHandler){
				PDICMainAppOptions.bottomNavWeb1(nav);
			}
			bottomNavWeb = nav;
			//if(tv!=null) tv.setText(getResources().getTextArray(R.array.btm_navmode)[type]);
		}
	}
	
	public final boolean getBottomNavWeb() {
		return bottomNavWeb;
	}
	
	void NavWeb(int d) {
		WebViewmy wv = getWebContext();
		if (wv !=null) {
			CMN.debug("NavWeb", wv.getUrl(), d, wv.canGoBack(), wv.canGoForward());
			if (d>0) wv.goForward();
			else wv.goBack();
			// todo sync btn states
		}
	}
	
	public final WebViewmy getWebContext() {
		//CMN.debug("getWebContext::", CMN.idStr(this), src, isViewSingle(), dictView==null);
		return isViewSingle() ?
				/*isMultiRecord()*/dictView==null ? getMergedFrame() : dictView
				: scrollFocus;
	}
	
	public final WebViewmy getWebContextNonNull() {
		WebViewmy ret = getWebContext();
		return ret==null?a.weblistHandler.getMergedFrame():ret;
	}
	
	public final boolean bottomNavWeb() {
		return bottomNavWeb && pageSlider.page.decided==0;
	}
	
	public void invokeToolsBtn(boolean pick, int quick) {
		a.weblist = this;
		WebViewmy wv = dictView;
		if (pick) {
			if (wv != null) {
				wv.presenter.invokeToolsBtn(wv, -1);
			} else {
				// sometime the view not initialized yet
				a.getVtk().setInvoker(a.EmptyBook, null, null, null);
				a.getVtk().bFromWebView = true;
				a.getVtk().onClick(/*trust webview selection*/a.anyView(R.id.tools));
			}
			a.getVtk().bPicking = 1;
			a.showTopSnack("选择快捷功能！");
		} else {
			if (wv!=null) {
				wv.presenter.invokeToolsBtn(wv, quick);
			}
		}
	}
	
	public void renderFoldingScreen(int frame) {
		try {
			BookPresenter presenter = frames.get(frame);
			long[] displaying = framesDisplaying.get(frame);
			boolean shareView;
			if (presenter.getIsWebx()) {
				presenter.SetSearchKey(batchDisplaying().key);
			}
			if (bDataOnly) {
				shareView = true;// 点击翻译始终只有一个webview视图
			} else {
				shareView = PDICMainAppOptions.getUseSharedFrame();
				if (presenter.getIsWebx() && (!shareView || PDICMainAppOptions.getMergeExemptWebx())) {
					shareView = false;
				}
			}
			WebViewmy mWebView;
			if(opt.getRemPos()) {
				savePagePos();
			}
			if (shareView) {
				mWebView = getMergedFrame(presenter);
			} else {
				presenter.initViewsHolder(a);
				mWebView = presenter.mWebView;
				mWebView.weblistHandler = this;
			}
			
			mWebView.fromCombined = bDataOnly?2:0;
			dictView = mWebView;
			presenter.renderContentAt(-2, BookPresenter.RENDERFLAG_NEW, frame, mWebView, displaying);
			if (!bDataOnly) {
				ViewUtils.addViewToParentUnique(mWebView.rl, webSingleholder);
			}
			setScrollFocus(mWebView, frame);
			pageSlider.setWebview(mWebView, null);
			batchDisplaying().LongestStartWithSeqLength = -frame;
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public void savePagePos() {
		if (isViewSingle()) {
			View child = webSingleholder.getChildAt(0);
			if (child!=null) {
				WebViewmy mWebView = child.findViewById(R.id.webviewmy);
				if (mWebView!=null) {
					BookPresenter prev = mWebView.presenter;
					if(!prev.isMergedBook() && !mWebView.isloading && System.currentTimeMillis()-a.lastClickTime>300) {
						if (mWebView.webScale == 0) mWebView.webScale = a.dm.density; //sanity check
						//CMN.Log("savePagePos::保存位置::", prev.getDictionaryName(), (int) mWebView.currentPos);
						ScrollerRecord pPos = prev.avoyager.get((int) mWebView.currentPos);
						if (mWebView.shouldStoreNewPagePos(pPos)) {
							prev.avoyager.put((int) mWebView.currentPos, pPos = new ScrollerRecord());
						}
						if (pPos!=null) {
							pPos.set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
						}
					}
				}
			}
			a.lastClickTime=System.currentTimeMillis();
		}
	}
	
	public final additiveMyCpr1 batchDisplaying() {
		additiveMyCpr1 ret = bMergingFrames == 0 ? jointResult : getMergedFrame().jointResult;
		if (ret==null) ret = multiDisplaying();
		return ret;
	}
	
	public final additiveMyCpr1 multiDisplaying() {
		return multiRecord==null?null:multiRecord.jointResult;
	}
	
	
	String[] transVals = new String[2];
	boolean[] translating = new boolean[2];
	
	public void putTransval(int index, String value) {
		if(index>=0 && index<2 && !TextUtils.isEmpty(value))
			transVals[index] =  value;
	}
	
	public String getTransval(int index) {
		String ret=null;
		if(index>=0 && index<2)
			ret = transVals[index];
		if (ret==null) {
			ret = index==1?"t"
					:"zh-CN";
		}
		return ret;
	}
	
	public void putTranslate(int index, boolean value) {
		if(index>=0 && index<2)
			translating[index] =  value;
	}
	
	public boolean getTranslate(int index) {
		if(index>=0 && index<2)
			return translating[index];
		return false;
	}
	
	@Override
	public String toString() {
		return "WebViewListHandler{" +
				"src=" + Integer.toHexString(src) +
				", id=" + CMN.idStr(this) +
				", isMain=" + (this==a.weblistHandler) +
				'}';
	}
	
	/** 用浏览器打开当前页面 */
	public String getShareUrl(boolean forceMerge) {
		String url = "";
		additiveMyCpr1 records = null;
		if (isViewSingle()) {
			WebViewmy wv = dictView;
			if (wv != null) {
				url = wv.getUrl();
				if (forceMerge && isFoldingScreens()) {
					records = batchDisplaying();
				} else {
					BookPresenter presenter = wv.presenter;
					if (presenter!=null) {
						if (presenter.getIsWebx()) {
							url = wv.getUrl();
						}
						else if (!url.startsWith("http")) { //loadWithBaseUrl 结果是 about:blank
							StringBuilder mergedUrl = new StringBuilder("http://localhost:8080/content/d");
							IU.NumberToText_SIXTWO_LE(presenter.getId(), mergedUrl);
							for (int i = 0; i < wv.currentRendring.length; i++) {
								mergedUrl.append("_");
								IU.NumberToText_SIXTWO_LE(wv.currentRendring[i], mergedUrl);
							}
							url = mergedUrl.toString();
						}
					}
				}
			}
			else {
				url = getMergedFrame().getUrl();
				if (url!=null) {
					url = url.replace("http://mdbr.com", "http://localhost:8080");
				}
			}
		}
		else {
			if(scrollFocus==null && webholder!=null) {
				scrollFocus = webholder.findViewById(R.id.webviewmy);
			}
			if (scrollFocus != null) {
				url = scrollFocus.getUrl();
				if (forceMerge) {
					records = batchDisplaying();
				}
			}
		}
		if (records!=null) {
			List<Long> vals = (List<Long>) records.value;
			StringBuilder mergedUrl = new StringBuilder("http://localhost:8080/merge.jsp?q=")
					.append(SU.encode(records.key)).append("&exp=");
			BookPresenter presenter;
			long toFind;
			for(int i=0;i<vals.size();i+=2){
				toFind=vals.get(i);
				presenter = a.getBookByIdNoCreation(toFind);;
				boolean isWebx = presenter.getIsWebx();
				if(i>0)
					mergedUrl.append("-");
				mergedUrl.append(isWebx?"w":"d");
				IU.NumberToText_SIXTWO_LE(toFind, mergedUrl);
				if(isWebx) {
					mergedUrl.append("_")
						.append(presenter.getWebx().hasField("synthesis")?"0":"");
				}
				while(i<vals.size() && toFind==vals.get(i)) {
					if(!isWebx) {
						mergedUrl.append("_");
						IU.NumberToText_SIXTWO_LE(vals.get(i+1), mergedUrl);
					}
					i+=2;
				}
				i-=2;
			}
			url = vals.toString();
		}
		CMN.debug("getShareUrl::", url);
		return url;
	}
	
	/** collect for 笔记对话框 -> 当前页面. */
	public String collectExpUrl() {
		ViewGroup vg = getViewGroup();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vg.getChildCount(); i++) {
			WebViewmy mWebView = vg.getChildAt(i).findViewById(R.id.webviewmy);
			if (mWebView != null) {
				long[] red = mWebView.currentRendring;
				if (red != null) {
					if (sb.length()>0) sb.append("-");
					sb.append(mWebView.presenter.getId());
					for (int j = 0; j < red.length; j++) {
						sb.append("_"); sb.append(red[j]);
					}
				}
			}
		}
		return sb.toString();
	}
	
	ArrayList<BookPresenter> translators = new ArrayList<>();
	TwoWayGridView txtMenuGrid;
	
	public void initQuickTranslatorsBar(boolean show, boolean animate) {
		CMN.debug("initQuickTranslatorsBar", show);
		if (txtMenuGrid == null) {
			txtMenuGrid = new TwoWayGridView(a);
			txtMenuGrid.setHorizontalSpacing(0);
			txtMenuGrid.setVerticalSpacing(0);
			txtMenuGrid.setHorizontalScroll(true);
			txtMenuGrid.setStretchMode(GridView.NO_STRETCH);
			//txtMenuGrid.setScrollbarFadingEnabled(false); // todo check why crash
			txtMenuGrid.setSelector(a.mResource.getDrawable(R.drawable.listviewselector0));
			//txtMenuGrid.setBackground(null);
			OnTouchListener touchYou = new OnTouchListener() {
				float distance(float x, float y) {
					return (float)Math.sqrt(x*x + y*y);
				}
				float orgX, orgY;
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int actionMasked = event.getActionMasked();
					if (actionMasked==MotionEvent.ACTION_DOWN) {
						orgX = event.getX();
						orgY = event.getY();
						mBar.isDragging = true;
						CMN.debug("click down");
					}
					else if (actionMasked==MotionEvent.ACTION_UP||actionMasked==MotionEvent.ACTION_CANCEL) {
						mBar.isDragging = false;
						CMN.debug("click up");
					}
					else if (v==toolsBtn && actionMasked==MotionEvent.ACTION_MOVE) {
						float x = event.getX();
						float y = event.getY();
						if (distance(x-orgX, y-orgY)>v.getWidth()) {
							CMN.debug("123");
							
						}
					}
					return false;
				}
			};
			toolsBtn.setOnTouchListener(touchYou);
			txtMenuGrid.setOnTouchListener(touchYou);
		}
		if(bDataOnly)
			return;
		if (show) {
			if (translators.size() == 0) {
				try {
					translators.add(a.new_book(a.defDicts[4], a));
					translators.add(a.new_book(a.defDicts[5], a));
					translators.add(a.new_book(a.defDicts[6], a));
					translators.add(a.new_book(a.defDicts[7], a));
					translators.add(a.new_book(a.defDicts[8], a));
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
			if (txtMenuGrid.getAdapter()==null) {
				txtMenuGrid.setAdapter(new BaseAdapter() {
					class MenuItemViewHolder {
						public final TextView tv;
						public MenuItemViewHolder(View convertView) {
							tv = convertView.findViewById(R.id.text);
						}
					}
					@Override
					public int getCount() {
						return translators.size();
					}
					@Override
					public Object getItem(int position) {
						return null;
					}
					@Override
					public long getItemId(int position) {
						return 0;
					}
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						BookPresenter book = translators.get(position);
						if (false) {
//					if(convertView==null) {
//						convertView = new View(a);
//						TwoWayAbsListView.LayoutParams lp = new TwoWayAbsListView.LayoutParams((int) (1.5 * GlobalOptions.density), menu_width);
//						convertView.setLayoutParams(lp);
//						convertView.setBackground(new SearchToolsMenu.TopThumb(0x9fffffff, (int) (8*GlobalOptions.density)));
//					}
						} else {
							MenuItemViewHolder holder;
							if(convertView==null) {
								convertView = a.getLayoutInflater().inflate(R.layout.translator_cover, parent, false);
								convertView.setTag(holder=new MenuItemViewHolder(convertView));
							} else {
								holder = (MenuItemViewHolder) convertView.getTag();
							}
							//convertView.getLayoutParams().width = id==0?1:menu_width;
							holder.tv.setText(""+Character.toUpperCase(book.getDictionaryName().charAt(0)));
							//holder.tv.setTextColor(a.AppBlack);
							int did = R.drawable.ic_view_comfy_2_black_24dp;
						}
						return convertView;
					}
				});
				txtMenuGrid.setOnItemClickListener((parent, view, position, id) -> {
					BookPresenter book = translators.get(position);
					WebViewmy wv = (WebViewmy) toolsBtn.getTag();
					if (wv != null) {
						wv.evaluateJavascript("getSelection().toString()", value -> {
							if (value.length() > 2) {
								value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
								//CMN.debug("initQuickTranslatorsBar::getSelection=", StringEscapeUtils.escapeJava(value));
								if (wv.presenter.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_PDF) {
									value = value.replaceAll("-\n", "");
									value = value.replaceAll("\n(?!\n)", " ");
								}
								if (value.length() > 0) {
									a.popupWord(value, book.getPath().equals("/ASSET2/译.web")?null:book, wv.frameAt, wv, false);
								}
							}
						});
					}
				});
				txtMenuGrid.setOnItemLongClickListener((parent, view, position, id) -> {
					a.popupWord(null, null, 0, null, false);
					return true;
				});
			}
			if (txtMenuGrid.getParent() == null) {
				VU.addViewToParent(txtMenuGrid, (ViewGroup) toolsBtn.getParent(), toolsBtn);
				FrameLayout.LayoutParams lp = VU.newFrameLayoutParams((FrameLayout.LayoutParams) toolsBtn.getLayoutParams());
				txtMenuGrid.setLayoutParams(lp);
				lp.width = -2;
				lp.rightMargin += toolsBtn.getLayoutParams().width;
				lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
				txtMenuGrid.setAlpha(0);
				txtMenuGrid.setTranslationX(GlobalOptions.density*10);
				txtMenuGrid.animate().setDuration(100).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						if(txtMenuGrid.getAlpha()!=1)
							VU.setVisible(txtMenuGrid, false);
					}
				});
				//txtMenuGrid.setTag();
				
			}
			VU.setVisible(txtMenuGrid, show);
		}
		if (show || txtMenuGrid.getAlpha() > 0 && txtMenuGrid.getParent() != null) {
			float tx = show ? 0 : GlobalOptions.density * 10;
			if (animate) {
				txtMenuGrid.animate()
						.alpha(show ? 1 : 0)
						.translationX(tx)
					//.setListener(null)
				;
			} else {
				VU.setVisible(txtMenuGrid, show);
				txtMenuGrid.setAlpha(show ? 1 : 0);
				txtMenuGrid.setTranslationX(tx);
			}
		}
	}
	
	@Override
	public void setNestedScrollingEnabled(boolean enabled) {
		if (this == a.weblistHandler) {
			((AdvancedNestScrollView)WHP).setNestedScrollingEnabled(enabled);
			if (dictView != null) {
				((AdvancedNestScrollWebView)dictView).setNestedScrollingEnabled(enabled);
			}
			if (mMergedFrame != null) {
				((AdvancedNestScrollWebView)mMergedFrame).setNestedScrollingEnabled(enabled);
			}
			for(BookPresenter book:frames) {
				if (book.mWebView != null) {
					((AdvancedNestScrollWebView)book.mWebView).setNestedScrollingEnabled(enabled);
				}
			}
		}
	}
	
	public void togNavor() {
		showNavor = !showNavor;
		VU.setVisible(contentUIData.navigator, showNavor);
		if (showNavor && contentUIData.navigator.getTag() == null) {
			contentUIData.navigator.setTag(this);
			VU.setOnClickListenersOneDepth(contentUIData.navigator, this, 999, null);
			// todo move btns
		}
	}
	
	public boolean isViewInUse(WebViewmy standalone) {
		if (standalone.isAttachedToWindow()) {
			return true;
		}
		return false;
	}
	
	public void announceContent() {
		ViewGroup vg = getViewGroup();
		View view = vg.getChildAt(0);
		if (view != null) {
			view = view.findViewById(R.id.toolbar_title);
		}
		if (view != null) {
			View finalView = view;
			vg.postDelayed(new Runnable() {
				@Override
				public void run() {
					finalView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
				}
			}, 350);
		}
	}
	
	public String getSearchKey() {
		if (etSearch != null) {
			return etSearch.getText().toString();
		}
		return a.getSearchTerm();
	}
	
	public void checkTitlebarHeight() {
		if (isViewSingle() && !isMultiRecord()) {
			WebViewmy wv = getWebContext();
			if (wv!=null) {
				int defTH = 0;
				if (wv.fromCombined!=1 && PDICMainAppOptions.customTitlebarHeight()) {
					defTH = (int) (opt.getInt("ttH", 0)  * GlobalOptions.density);
				}
				wv.titleBarHeight(defTH);
			}
		}
	}
	
	public List<MenuItemImpl> contentMenu() {
		if (this == a.weblistHandler) {
			return a.AllMenusStamp;
		}
		if (isPopupShowing()) {
			return alloydPanel.AllMenus.mItems;
		}
		if (a.peruseView!=null && this==a.peruseView.weblistHandler) {
			return alloydPanel.AllMenus.mItems;
		}
		return a.AllMenusStamp;
	}
	
	public void tapDef(boolean val) {
		if (tapDef != val) {
			tapDef = val;
			if (this == a.weblistHandler) PDICMainAppOptions.tapViewDefMain(val);
			if (isPopupShowing()) PDICMainAppOptions.tapDefPupup(val);
			if (a.peruseView!=null && this==a.peruseView.weblistHandler) PDICMainAppOptions.fyeTapViewDef(val);
			//if (val) a.showT("不弹出，查看定义");
		}
	}
	
	public void tapSch(boolean val) {
		if (tapSch != val) {
			if (this == a.weblistHandler) opt.tapSch(val);
			if (isPopupShowing()) PDICMainAppOptions.tapSchPupup(val);
			if (a.peruseView!=null && this==a.peruseView.weblistHandler) PDICMainAppOptions.fyeTapSch(val);
			tapSch = !val;
			togTapSch();
			//if (val) a.showT("弹出翻译");
		}
	}
	
}
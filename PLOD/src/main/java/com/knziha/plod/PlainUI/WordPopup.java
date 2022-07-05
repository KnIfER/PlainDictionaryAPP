package com.knziha.plod.PlainUI;

import static com.knziha.plod.PlainUI.WordPopupTask.TASK_FYE_SCH;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_LOAD_HISTORY;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_POP_NAV;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_POP_NAV_NXT;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_POP_SCH;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertController;
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
import com.knziha.plod.plaindict.MultiShareActivity;
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
import com.knziha.plod.widgets.PopupTouchMover;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.TwoColumnAdapter;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WordPopup extends PlainAppPanel implements Runnable, View.OnLongClickListener {
	public /*final*/ WebViewListHandler weblistHandler;
	public String popupKey;
	int popupFrame;
	BookPresenter popupForceId;
	public TextView entryTitle;
	protected PopupTouchMover moveView;
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
	public MainActivityUIBase.LoadManager loadManager;
	ViewGroup splitter;
	private final Runnable clrSelAby = () -> invoker.evaluateJavascript("window.getSelection().collapseToStart()", null);
	public SearchbarTools etTools;
	private boolean requestAudio;
	public boolean tapped;
	
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
				int delta = id==R.id.popNxtE?1:-1;
				boolean slided = v.getTag()==v; /** see{@link #getPageListener} */
				if (slided) v.setTag(null);
				WebViewListHandler weblist = weblistHandler;
				if (slided && weblist.isFoldingScreens() && weblist.multiDicts && PDICMainAppOptions.slidePageFd()) {
					int toPos = weblist.multiRecord.jointResult.LongestStartWithSeqLength;
					if (toPos>0) toPos = delta;
					else toPos = delta-toPos;
					if (toPos>=0 && toPos<weblist.frames.size()) {
						weblist.renderFoldingScreen(toPos);
						break;
					}
				}
				if(CCD==a.EmptyBook||CCD==null)
					CCD=a.currentDictionary;
				resetPreviewIdx();
				requestAudio = PDICMainAppOptions.tapSchPageAutoReadEntry();
				if (weblist.isMultiRecord()) {
					resultRecorderCombined rec = weblist.multiRecord;
					int np = rec.viewingPos + delta;
					if (np>=0 && np<rec.size()) {
						mWebView.presenter = a.weblistHandler.getMergedBook();
						rec.renderContentAt(currentPos=np, a, null, weblist);
						dictPicker.filterByRec(rec, np);
						weblist.setViewMode(rec, isMergingFramesNum(), weblist.dictView);
						setDisplaying(weblist.getMultiRecordKey());
					}
				} else {
					loadEntry(id==R.id.popNxtE?1:-1);
				}
			} break;
			case R.id.popNxtDict:
			case R.id.popLstDict:{
				//SearchNxt(id==R.id.popNxtDict, task, taskVer, taskVersion);
				String url = mWebView.getUrl();
				if (url!=null) {
					int schemaIdx = url.indexOf(":");
					if(url.regionMatches(schemaIdx+3, "mdbr", 0, 4)){
						try {
							if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
								startTask(id==R.id.popNxtDict?TASK_POP_NAV_NXT:TASK_POP_NAV);
							}
							else if (url.regionMatches(schemaIdx+12, "merge", 0, 5)) {
								weblistHandler.bMergingFrames = 1;
								weblistHandler.prvnxtFrame(id==R.id.popNxtDict);
							}
						} catch (Exception e) {
							CMN.debug(e);
						}
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
				weblistHandler.btmV = SearchUI.btmV;
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
				a.toggleStar(displaying, (ImageView) v, false, weblistHandler);
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
				dictPicker.toggle();
			} break;
			case R.id.gTrans:{
				CMN.Log("R.id.gTrans::!!!)");
				MenuItemImpl mSTd = a.getMenuSTd(R.id.translate);
				mSTd.isLongClicked = false;
				a.onMenuItemClick(mSTd);
				weblistHandler.bMergingFrames=1;
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(mSTd.tag);
				if(dd!=null) dd.tag = this;
			} break;
			case R.id.max:{
				moveView.togMax();
			} break;
			case R.id.mode:{
				showSchModeDialog(v, false);
			} break;
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		if (!moveView.moveTriggered) {
			int id = v.getId();
			if (id==R.id.mode) {
				showSchModeDialog(v, true);
			}
		}
		return true;
	}
	
	private void showSchModeDialog(View tkMultiV, boolean tkSingle) {
		AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(tkMultiV.getTag());
		if (tkSingle) {
			AlertDialog dlg = dd!=null?(AlertDialog)dd.tag:null;
			if (dlg==null) {
				dlg = new AlertDialog.Builder(a)
						.setTitle("单本词典查询模式 :")
						.setWikiText("亦可长按打开此对话框", null)
						.setSingleChoiceLayout(R.layout.singlechoice_plain)
						.setSingleChoiceItems(R.array.click_search_mode_info, PDICMainAppOptions.getClickSearchMode(), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								PDICMainAppOptions.setClickSearchMode(which);
								if (schMode!=0) { // 设置单本搜索模式
									opt.tapSchMode(schMode = 0);
									modeBtn.setImageResource(R.drawable.ic_btn_siglemode);
									startTask(WordPopupTask.TASK_POP_SCH);
								}
								dialog.dismiss();
							}
						}).create();
				if (dd!=null) dd.tag = dlg;
			}
			ViewUtils.ensureWindowType(dlg, a, null);
			dlg.show();
			dlg.getWindow().setDimAmount(0);
		} else {
			if(dd==null) {
				String[] items = new String[]{
						"单本词典搜索 >> "
						, "联合搜索，屏风模式"
						, "联合搜索，合并多页面"
				};
				DialogInterface.OnClickListener listener = (dialog, which) -> {
					if (which==0) { // find touching span
						ListView lv = ((AlertDialog) dialog).getListView();
						View child = lv.getChildAt(0);
						if (lv.getPositionForView(child)==0) {
							TextView tv = child.findViewById(android.R.id.text1);
							ClickableSpan touching = opt.XYTouchRecorderInstance().getTouchingSpan(tv);
							if (touching!=null) {
								which = -1;
							}
						}
					}
					if (which>=0) {
						// 设置搜索模式
						opt.tapSchMode(schMode = which%3);
						modeBtn.setImageResource(schMode==0?R.drawable.ic_btn_siglemode:R.drawable.ic_btn_multimode);
						startTask(WordPopupTask.TASK_POP_SCH);
						dialog.dismiss();
					}
					if (which==-1) {
						showSchModeDialog(tkMultiV, true);
					}
				};
				dd = new AlertDialog.Builder(a)
						.setSingleChoiceLayout(R.layout.singlechoice_my)
						.setAdapter(new AlertController.CheckedItemAdapter(a, R.layout.singlechoice_my, android.R.id.text1, items, null){
							@NonNull
							@Override
							public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
								view = super.getView(position, view, parent);
								CharSequence ret = getItem(position);
								if (ret!=null && TextUtils.regionMatches(ret, ret.length()-3, ">>", 0, 2)) {
									TextView tv = view.findViewById(android.R.id.text1);
									SpannableString span = new SpannableString(ret);
									ClickableSpan clkSpan = new ClickableSpan() {
										@Override
										public void onClick(@NonNull View widget) {
											//listener.onClick((AlertDialog)ViewUtils.getWeakRefObj(v.getTag()), -(position+1));
										}
									};
									span.setSpan(clkSpan, ret.length()-3, ret.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
									tv.setText(span, TextView.BufferType.SPANNABLE);
									tv.setOnTouchListener(opt.XYTouchRecorderInstance());
								}
								return view;
							}
						}, listener)
						.setSingleChoiceItems(items, 0, listener)
						.setTitle("切换搜索模式").create();
				tkMultiV.setTag(new WeakReference<>(dd));
				dd.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
						if (position==0) {
							listener.onClick((AlertDialog)ViewUtils.getWeakRefObj(tkMultiV.getTag()), -(position+1));
							return true;
						}
						return false;
					}
				});
			}
			ViewUtils.ensureWindowType(dd, a, null);
			dd.show();
			dd.getWindow().setDimAmount(0);
		}
	}
	
	public final boolean pin() {
		return a.mDialogType==WindowManager.LayoutParams.TYPE_APPLICATION
				&& (invoker==null||invoker.weblistHandler!=a.randomPageHandler)
				&& (popupChecker==null?PDICMainAppOptions.getPinTapTranslator():popupChecker.isChecked());
	}
	
	public void show() {
		if (!isVisible()) {
			int type = pin()?0:2;
			toggle(lastTargetRoot, null, type);
			int pad = type==0?0: (int) (GlobalOptions.density * 19);
			if(settingsLayout.getPaddingTop()!=pad)settingsLayout.setPadding(0,pad,0,0);
			if(dictPicker.settingsLayout==null && dictPicker.pinShow()) {
				dictPicker.toggle();
			}
		} else if (getLastShowType()==2) {
			ViewUtils.ensureTopmost(dialog, a, dialogDismissListener);
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
			dictPicker.loadManager = this.loadManager;
			dictPicker.autoScroll = true;
			PageSlide page = pageSlider.page = (PageSlide) pageSlider.getChildAt(0);
			WebViewmy webview = (WebViewmy) pageSlider.getChildAt(1);;
			pageSlider.weblist = page.weblist = webview.weblistHandler = weblist;
			weblist.scrollFocus = webview;
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
			modeBtn.setOnLongClickListener(this);
			schMode = opt.tapSchMode();
			if(schMode==0) modeBtn.setImageResource(R.drawable.ic_btn_siglemode);
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
			moveView = new PopupTouchMover(a, entryTitle);
			for (int i = 0; i < toolbar.getChildCount(); i++) {
				toolbar.getChildAt(i).setOnTouchListener(moveView);
			}
			for (int i = 0; i < pottombar.getChildCount(); i++) {
				pottombar.getChildAt(i).setOnTouchListener(moveView);
			}
			
			weblist.toolsBtn = toolbar.findViewById(R.id.tools);
			weblist.toolsBtn.setTag(webview);
			weblist.toolsBtn.setOnClickListener(weblist);
			weblist.toolsBtn.setOnLongClickListener(weblist);
			weblist.browserWidget8 = toolbar.findViewById(R.id.popIvStar);
			weblist.browserWidget10 = pottombar.findViewById(R.id.popLstE);
			weblist.browserWidget11 = pottombar.findViewById(R.id.popNxtE);
			
			weblist.mBar = pageSlider.findViewById(R.id.dragScrollBar);
			this.mWebView = weblist.dictView = weblist.mMergedFrame = webview;
			BookPresenter.setWebLongClickListener(mWebView, a);
			pageSlider.bar = weblist.mBar;
			
			weblist.entrySeek = pageSlider.findViewById(R.id.entrySeek);
			weblist.entrySeek.setOnSeekBarChangeListener(weblist.entrySeekLis);
			weblist.prv = pageSlider.findViewById(R.id.prv);
			weblist.nxt = pageSlider.findViewById(R.id.nxt);
			PorterDuffColorFilter phaedrof = new PorterDuffColorFilter(0xff888888, PorterDuff.Mode.SRC_IN);
			weblist.prv.setColorFilter(phaedrof);
			weblist.nxt.setColorFilter(phaedrof);
			weblist.prv.setOnClickListener(weblist);
			weblist.nxt.setOnClickListener(weblist);
			
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
		int size = loadManager.md_size;
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
		isInit = isNewHolder = mWebView == null || mWebView.fromCombined!=2;
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
				//int h=mPopupContentView.getLayoutParams().height;
				//if (h>0) lp.height = h;
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
					if(CCD_ID<0)CCD_ID+=loadManager.md_size;
				}
				CCD_ID=CCD_ID%loadManager.md_size;
				
				if(hasDedicatedSeachGroup && CCD_ID<loadManager.md_size && !PDICMainAppOptions.getTmpIsClicker(loadManager.getPlaceHolderAt(CCD_ID).tmpIsFlag))
					continue;
				CCD=loadManager.md_get(CCD_ID);
				cc++;
				if(cc>loadManager.md_size)
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
				harvest(); //下一个！
			}
		}
	}
	
	RBTree_additive _treeBuilder = new RBTree_additive();
	
	@AnyThread
	private void SearchMultiple(AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		_treeBuilder.clear();
		int paragraphWords = 9;
		String searchText = popupKey;
		boolean isParagraph = BookPresenter.testIsParagraph(searchText, paragraphWords);
		CMN.Log("isParagraph::", isParagraph);
		_treeBuilder.setKeyClashHandler(searchText);
		for (int i = 0; i < loadManager.md_size && task.get(); i++) {
			PlaceHolder phTmp = loadManager.getPlaceHolderAt(i);
			if (phTmp != null) {
				BookPresenter book = loadManager.md_get(i);
				try {
					if(book.getAcceptParagraph(searchText, isParagraph, paragraphWords)) {
						CrashHandler.hotTracingObject = book;
						_treeBuilder.resetRealmer(book.getId());
						book.bookImpl.lookUpRange(searchText, null, _treeBuilder, book.getId(),7, task);
					}
				} catch (Exception e) {
					CMN.Log(CrashHandler.hotTracingObject, e);
				}
			}
		}
		resultRecorderCombined rec = new resultRecorderCombined(a, _treeBuilder.flatten(), searchText);
		if (rec.FindFirstIdx(searchText, task) && taskVer==taskVersion.get()) {
			this.rec = rec;
			harvest(); // multiple!
		}
	}
	
	@AnyThread
	private void harvest() {
		a.hdl.removeCallbacks(harvestRn);
		a.hdl.post(harvestRn);
	}
	
	@AnyThread
	private void SearchOne(AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		int idx = -1, cc = 0;
		resetPreviewIdx();
		CMN.debug("SearchOne::", popupKey);
		if (popupKey != null) {
			String keykey;
			int size = loadManager.md_size;
			CCD_ID = upstrIdx = Math.min(upstrIdx, size -1);
			if(popupForceId!=null) {
				CCD = popupForceId;
				CCD_ID = loadManager.md_find(popupForceId);
				if(CCD_ID<0) {
					CCD_ID = size;
					loadManager.md.add(popupForceId); // todo check???
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
				CCD = loadManager.md_get(CCD_ID);
				if (CCD != a.EmptyBook) {
					if(CCD.getIsWebx()){
						webx = CCD;
						if (!webx.getWebx().takeWord(popupKey)) {
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
						for (int i = 0; i < size; i++) {
							mdTmp = null;
							CSID = (i + CCD_ID) % size;
							if (PDICMainAppOptions.getTmpIsClicker(loadManager.getPlaceFlagAt(CSID))) {
								mdTmp = loadManager.md_get(CSID);
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
										CCD_ID = (i + CCD_ID) % size;
										CCD = mdTmp;
										break FindCSD;
									}
									if(bForceJump){
										break FindCSD;
									}
								}
							}
						}
						if (firstAttemp != null && size >0) {
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
						if (cc > size)
							break;
						CCD_ID = CCD_ID % size;
						CCD = loadManager.md_get(CCD_ID);
						if(CCD.getIsWebx()){
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
				if (webx.getWebx().getHasModifiers()) {
					weblistHandler.moders.remove(webx.getWebx());
					weblistHandler.moders.add(webx.getWebx());
				}
				idx = 0;
				//CCD = webx;
			}
			
			//CMN.Log(CCD, "应用轮询结果", webx, idx);
			if(idx>=0 && CCD != a.EmptyBook  && task.get() && taskVer == taskVersion.get()) {
				if(bForceJump && SearchMode==1)
					mWebView.setTag(R.id.js_no_match, false);
				currentPos = idx;
				this.rec = null;
				sching = CCD;
				harvest(); //single!
			}
			
			if (!PDICMainAppOptions.storeNothing()
					&& PDICMainAppOptions.getHistoryStrategy7())
				a.addHistory(popupKey, SearchUI.TapSch.MAIN, weblistHandler, null);
		}
	}
	
	public void SearchDone() {
		requestAudio = PDICMainAppOptions.tapSchAutoReadEntry();
		//CMN.Log("SearchDone::", rec, currentPos, CCD);
		if (rec != null) {
			if (rec.size() > 0) {
				rec.jointResult = rec.getJointResultAt(0);
			}
			weblistHandler.setViewMode(rec, isMergingFramesNum(), mWebView);
			mWebView.presenter = a.weblistHandler.getMergedBook(); //todo opt
			if (mWebView.wvclient != a.myWebClient) {
				mWebView.setWebChromeClient(a.myWebCClient);
				mWebView.setWebViewClient(a.myWebClient);
			}
			if (rec.size() > 0) {
				rec.renderContentAt(0, a, null, weblistHandler);
				setDisplaying(weblistHandler.getMultiRecordKey());
			}
			weblistHandler.pageSlider.setWebview(mWebView, null);
			dictPicker.filterByRec(rec, 0);
			setTranslator(rec, 0);
		}
		else {
			dictPicker.filterByRec(null, 0);
			if(sching!=null) {
				texts[0]=CMN.id(sching);
				setTranslator(sching, currentPos);
				sching = null;
			}
			if (currentPos >= 0 && CCD != a.EmptyBook) {
				weblistHandler.setViewMode(null, 0, mWebView);
				if(CCD.getIsWebx()) { //todo 合并逻辑
					weblistHandler.bMergingFrames = 1;
					indicator.setText(loadManager.md_getName(CCD_ID, -1));
					popuphandler.setBook(CCD);
					CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, mWebView, currentPos);
					weblistHandler.pageSlider.setWebview(mWebView, null);
					setDisplaying(mWebView.word);
				} else {
					loadEntry(0);
				}
			}
		}
	}
	
	private int isMergingFramesNum() {
		return /*PDICMainAppOptions.foldingScreenTapSch()*/schMode==1?2:1;
	}
	
	private void setDisplaying(String key) {
		if (requestAudio)
			mWebView.bRequestedSoundPlayback=true;
		displaying = key;
		weblistHandler.setStar(key);
	}
	
	private void loadEntry(int d) {
		if (d!=0) {
			currentPos=Math.max(0, Math.min(currentPos+d, (int) CCD.bookImpl.getNumberEntries()));
		}
		mWebView.currentPos = currentPos;
		mWebView.presenter = CCD;
		if (CCD.getIsWebx()) { //todo 合并逻辑
			if (currentPos==0) {
				CCD.SetSearchKey(popupKey);
			}
			CCD.renderContentAt(-1, RENDERFLAG_NEW, 0, mWebView, currentPos);
		} else {
			weblistHandler.bMergingFrames = 1;
			StringBuilder mergedUrl = new StringBuilder("http://mdbr.com/content/");
			mergedUrl.append("d");
			IU.NumberToText_SIXTWO_LE(CCD.getId(), mergedUrl);
			mergedUrl.append("_");
			IU.NumberToText_SIXTWO_LE(currentPos, mergedUrl);
			if (CCD==popupForceId && invoker!=null && invoker.toTag!=null) {
				mergedUrl.append("#").append(invoker.toTag);
				invoker.toTag = null;
			}
			mWebView.loadUrl(mergedUrl.toString());
		}
		weblistHandler.resetScrollbar(mWebView, false, false);
		setDisplaying(mWebView.word=CCD.getBookEntryAt(currentPos));
	}
	
	public void popupWord(WebViewmy invoker, String key, BookPresenter forceStartId, int frameAt) {
		CMN.debug("popupWord_frameAt", frameAt, key, loadManager.md_size, invoker==null, WebViewmy.supressNxtClickTranslator);
		if(key==null || mdict.processText(key).length()>0) {
			if (invoker!=null) this.invoker = invoker;
			if (key!=null) popupKey = key;
			popupFrame = frameAt;
			popupForceId = forceStartId;
			a.hdl.removeCallbacks(this);
			if (invoker!=null && invoker.weblistHandler.pageSlider.tapZoom) { //todo ???
				a.hdl.postDelayed(this, SearchUI.tapZoomWait); // 支持双击操作会拖慢点译！
			} else {
				a.hdl.post(this);
			}
		}
	}
	
	public void PerformSearch(int mType, AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		if(mType==TASK_POP_SCH){
			if(schMode==0) SearchOne(task, taskVer, taskVersion);
			else SearchMultiple(task, taskVer, taskVersion);
		}
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
		if (tapped) {
			if(invoker!=null) {
				invoker.postDelayed(clrSelAby, 180);
			}
			tapped = false;
		}
		if (a.thisActType==MainActivityUIBase.ActType.MultiShare) {
			((MultiShareActivity)a).OnPeruseDetached();
		}
	}
	
	public void valid(BookPresenter ccd, long pos) {
		int id=CMN.id(ccd);
		if (texts[0]!=id) {
			sching = ccd;
			if (dictPicker.filtered != null) {
				CCD_ID = dictPicker.filtered.indexOf(ccd.getId());
			} else {
				CCD_ID = loadManager.md_find(ccd); //todo opt
			}
			currentPos = (int) pos;
			texts[0]=id;
			a.hdl.post(setAby);
		} else {
			currentPos = (int) pos;
		}
	}
	
	public void valid(String text) {
		if (!TextUtils.equals(text, displaying)) {
			displaying = text;
			indicator.setText(null);
			texts[0] = 0;
			a.hdl.post(setAby1);
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
	
	public void set(boolean setSH) {
		if(PDICMainAppOptions.getImmersiveClickSearch()!=PDICMainAppOptions.getImmersiveClickSearch(a.TFStamp))
			a.popupWord(null,null, 0, null);
		if (mWebView!=null) {
			if(weblistHandler.btmV!=SearchUI.btmV) {
				SearchUI.btmV = weblistHandler.btmV;
				weblistHandler.btmV--;
				weblistHandler.setViewMode();
			}
			if (setSH) {
				a.weblist = weblistHandler;
				a.showScrollSet();
			}
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
					weblistHandler.bMergingFrames = 1;
					weblistHandler.resetScrollbar(mWebView, true, true);
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
}

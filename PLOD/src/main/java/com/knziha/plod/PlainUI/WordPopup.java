package com.knziha.plod.PlainUI;

import static com.knziha.plod.PlainUI.WordPopupTask.TASK_FYE_SCH;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_LOAD_HISTORY;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_POP_NAV;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_POP_NAV_NXT;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_POP_SCH;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_TTS;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_UPD_SCH;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;
import static com.knziha.plod.plaindict.CMN.GlobalPageBackground;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import androidx.appcompat.view.VU;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.SimpleMorphs;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.SimpleQueryMorphs;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CrashHandler;
import com.knziha.plod.plaindict.DictPicker;
import com.knziha.plod.plaindict.FloatSearchActivity;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.MultiShareActivity;
import com.knziha.plod.plaindict.PDICMainActivity;
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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WordPopup extends PlainAppPanel implements Runnable, View.OnLongClickListener {
	public String popupKey;
	public WordCamera wordCamera;
	int popupFrame;
	BookPresenter popupForceId;
	boolean bFromWebTap;
	public TextView entryTitle;
	protected PopupTouchMover moveView;
	public FlowTextView indicator;
	public WebViewmy mWebView;
	public BookPresenter.AppHandler popuphandler;
	public ImageView popIvBack;
	public ViewGroup popupContentView;
	private AppBarLayout appbar;
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
	/** 0=单本搜索; 1=联合搜索，合并页面; 2=联合搜索，屏风模式。 */
	private int schMode;
	private ImageView modeBtn;
	private AtomicBoolean singleTask = new AtomicBoolean(true);
	private AtomicInteger singleTaskVer = new AtomicInteger(0);
	
	public /*final*/ DictPicker dictPicker;
	public MainActivityUIBase.LoadManager loadManager;
	ViewGroup splitter;
	private final Runnable clrSelAby = () -> invoker.evaluateJavascript("window.getSelection().collapseToStart()", null);
	public SearchbarTools etTools;
	private boolean requestAudio;
	public boolean tapped;
	
	LinearSplitView splitView;
	AppBarLayout.BarSz barSz = new AppBarLayout.BarSz();
	public ArrayList<UniversalDictionaryInterface> forms = new ArrayList<>();
	SimpleMorphs simpleMorphs = new SimpleMorphs();
	public SimpleQueryMorphs queryMorphs = new SimpleQueryMorphs();
	
	public WordPopup(MainActivityUIBase a) {
		super(a, true);
		//this.a = a;
		bAnimate=false;
		bAutoRefresh=false;
		showType=1;
		bottomPadding=0;
		resizeDlg = true;
		forms.add(simpleMorphs);
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
	}
	
	public void refresh() {
		if(weblistHandler != null)
		{
			CMN.debug("wordPopup::refresh");
			if (MainColorStamp != a.MainAppBackground) {
				if (appbar != null) {
					appbar.getBackground().setColorFilter(GlobalOptions.isDark?GlobalOptions.NEGATIVE:null);
				}
				if(GlobalOptions.isDark){
					popupContentView.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
					pottombar.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
					popIvBack.setImageResource(R.drawable.abc_ic_ab_white_material);
					((ImageView)pottombar.findViewById(R.id.popIvSettings)).setColorFilter(GlobalOptions.NEGATIVE);
				} else /*if(popIvBack.getTag()!=null)*/{ //???
					popupContentView.getBackground().setColorFilter(null);
					pottombar.getBackground().setColorFilter(null);
					popIvBack.setImageResource(R.drawable.abc_ic_ab_back_material_simple_compat);
					((ImageView)pottombar.findViewById(R.id.popIvSettings)).setColorFilter(null);
				}
				if(indicator != null) {
					entryTitle.setTextColor(GlobalOptions.isDark?a.AppBlack:Color.GRAY);
					indicator.setTextColor(GlobalOptions.isDark?a.AppBlack:0xff2b43c1);
				}
				MainColorStamp = a.MainAppBackground;
				int filteredColor = GlobalOptions.isDark ? ColorUtils.blendARGB(a.MainPageBackground, Color.BLACK, a.ColorMultiplier_Web) : GlobalPageBackground;
				weblistHandler.dictView.setBackgroundColor(filteredColor);
			}
			if (dictPicker.pinned()) {
				dictPicker.refresh();
			}
		}
		if (wordCamera != null) {
			popupContentView.setAlpha(isMaximized() ? 1 : 0.8f);
		}
	}
	
	@Override
	protected void onShow() {
		super.onShow();
		if (PDICMainAppOptions.getImmersiveClickSearch()) {
			try {
				a.getScrollBehaviour(false).onDependentViewChanged((CoordinatorLayout) appbar.getParent(), splitView, appbar);
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		refresh();
		if (PDICMainAppOptions.revisitOnBackPressed() && mWebView!=null && !bFromWebTap) {
			mWebView.cleanPage = true;
		}
	}
	
	@SuppressLint("ResourceType")
	@Override
	// click
	public void onClick(View v) {
		if (v == null) {
			if (wordCamera != null) {
				if (isMaximized()) {
					wordCamera.onPause();
				} else {
					wordCamera.onResume();
				}
				refresh();
			}
			return;
		}
		int id = v.getId();
		switch (id) {
			case R.id.cover: {
				if(v==weblistHandler.pageSlider.page){
					a.getVtk().setInvoker(CCD, dictView(false), null, null);
					a.getVtk().onClick(v);
				}
			} break;
			case R.id.popupBackground: {
				dismissImmediate();
			} break;
			case R.id.voice: {
				a.findWebList(v);
				a.performReadEntry();
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
					loadEntry(id==R.id.popNxtE?1:-1, false);
				}
			} break;
			case R.id.popNxtDict:
			case R.id.popLstDict:{
				//SearchNxt(id==R.id.popNxtDict, task, taskVer, taskVersion);
				String url = dictView(false).getUrl();
				if (url!=null) {
					int schemaIdx = url.indexOf(":");
					if(url.regionMatches(schemaIdx+3, "mdbr", 0, 4)){
						try {
							if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
								startTask(id==R.id.popNxtDict?TASK_POP_NAV_NXT:TASK_POP_NAV);
								break;
							}
							else if (url.regionMatches(schemaIdx+12, "merge", 0, 5)) {
								weblistHandler.bMergingFrames = 1;
								weblistHandler.prvnxtFrame(id==R.id.popNxtDict);
								break;
							}
						} catch (Exception e) {
							CMN.debug(e);
						}
					}
				}
				startTask(id==R.id.popNxtDict?TASK_POP_NAV_NXT:TASK_POP_NAV);
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
			case R.id.popupText1:{ // showEntryContextDlg
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(v.getTag());
				if(dd==null) {
					RecyclerView rv = new RecyclerView(a);
					GridLayoutManager lm = new GridLayoutManager(a, 2) {
						int flip;
						boolean dragging;
						@Override
						public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
							int scrollRange = super.scrollVerticallyBy(dy, recycler, state);
							if (dy != scrollRange && flip == 0 && scrollRange == 0) {
								//CMN.debug("scrollVerticallyBy::", dy, scrollRange);
								if (Math.abs(dy - scrollRange) > GlobalOptions.density * 27) {
									flip = dy < 0 ? -1 : 1; // 上下滑动翻页
								}
							}
							return scrollRange;
						}
						
						@Override
						public void onScrollStateChanged(int state) {
							//CMN.debug("onScrollStateChanged::", state, flip);
							if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
								if (!dragging) {
									dragging = true;
									flip = 0;
								}
							} else {
								if (dragging)
									dragging = false;
								if (state == RecyclerView.SCROLL_STATE_IDLE) {
									if (flip != 0) {
										int np = previewPageIdx + flip;
										previewPageIdx = np;
										refillPreviewEntries((AlertDialog) ViewUtils.getWeakRefObj(v.getTag()), false);
										flip = 0;
									}
								}
							}
						}
					};
					rv.setLayoutManager(lm);
					lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
						@Override
						public int getSpanSize(int position) {
							return 1;
						}
					});
					TwoColumnAdapter ada = new TwoColumnAdapter(previewEntryData);
					ada.setMaxLines(2);
					ada.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							AlertDialog dialog = (AlertDialog)ViewUtils.getWeakRefObj(v.getTag());
							if(dialog!=null)
								dialog.dismiss();
							if (position==0) { // 收藏词条
								a.keepWordAsFavorite(dictView(false).word(), weblistHandler);
							}
							else if (position==1) { // 切换分组
								dictPicker.onClick(a.anyView(R.id.bundle));
							}
							else {
								int base = previewMidPos;
								if (rec == null) base--;
								if (previewPageIdx == 0) {
									if (position % 2 != 0) {
										if (position == 3) { // 切换上一词典
											onClick(a.anyView(R.id.popLstDict));
										} else if (position == 5) { // 工具…
											MainActivityUIBase.VerseKit tk = a.getVtk();
											tk.setInvoker(CCD, dictView(false), null, String.valueOf(entryTitle.getText()));
											tk.onClick(a.anyView(0));
										} else { // 编辑搜索词
											a.showT("未实现");
										}
										return;
									}
									base = base + (position-2)/2;
								} else {
									if (previewPageIdx < 0) base+=previewPageIdx*6;
									else base+=3+(previewPageIdx-1)*6;
									base = base + (position-2)/2 + (position%2==0?0:3);
								}
								loadEntry(base-currentPos, false);
							}
						}
					});
					rv.setAdapter(ada);
					//rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
							previewPageIdx=0; // 重置
							WebViewmy resetWV = dictView(false);
							int dynamicPos = (int) resetWV.currentPos;
							if (rec == null) {
								if (resetWV.presenter != CCD && resetWV.presenter != a.EmptyBook) {
									previewMidPos = dynamicPos;
									setTranslator(resetWV.presenter, dynamicPos);
								} else {
									// 回不去啦
									resetPreviewMidPos();
								}
							} else {
								resetPreviewMidPos();
							}
						}
						isPreviewDirty = true;
						refillPreviewEntries(finalDd, false);
					}, 999, null);
					dd.tag=rv;
					v.setTag(new WeakReference<>(dd));
				}
				if (wordCamera!=null) {
					wordCamera.onPause();
				}
				dd.show();
				dd.getWindow().setDimAmount(0);
				ViewUtils.ensureTopAndTypedDlg(dd, a);
				refillPreviewEntries(dd, true);
			} break;
			case R.id.popupText2:{
				dictPicker.toggle();
				if (dictPicker.dialog!=null && dictPicker.dialog.isShowing()) {
					ViewUtils.ensureTopmost(dictPicker.dialog, a, dictPicker.dialogDismissListener);
				}
			} break;
			case R.id.gTrans:{
				a.onMenuItemClick(a.anyMenu(R.id.translate, weblistHandler));
				weblistHandler.bMergingFrames=1;
			} break;
			case R.id.max:{
				moveView.togMax();
			} break;
			case R.id.mode:{
				showSchModeDialog(v, false);
			} break;
			case R.id.single_tapsch_opt_1:
			{
				PDICMainAppOptions.singleTapSchMode(0);
				switchToSingleTapSch();
				((AlertDialog)ViewUtils.getViewHolderInParents(v, AlertDialog.class)).dismiss();
			} break;
			case R.id.single_tapsch_opt_2:
			{
				PDICMainAppOptions.singleTapSchMode(1);
				switchToSingleTapSch();
				((AlertDialog)ViewUtils.getViewHolderInParents(v, AlertDialog.class)).dismiss();
			} break;
			case R.id.single_tapsch_opt_3:
			{
				PDICMainAppOptions.singleTapSchMode(2);
				switchToSingleTapSch();
				((AlertDialog)ViewUtils.getViewHolderInParents(v, AlertDialog.class)).dismiss();
			} break;
		}
	}
	
	private void switchToSingleTapSch() {
		if (schMode!=0) { // 设置单本搜索模式
			opt.tapSchMode(schMode = 0);
			modeBtn.setImageResource(R.drawable.ic_btn_siglemode);
			if(isVisible())startTask(WordPopupTask.TASK_POP_SCH);
		}
		dictPicker.filterByRec(null, 0);
	}
	
	@Override
	public boolean onLongClick(View v) {
		if(v.getId()==R.id.popIvBack){
			weblistHandler.showMoreToolsPopup(v);
			return true;
		}
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
			if (dlg==null || dd.isDark!=GlobalOptions.isDark) {
				AlertDialog finalDd = dd;
				dlg = new AlertDialog.Builder(a)
						.setTitle("单本词典查询模式 :")
						.setWikiText("亦可长按打开此对话框", null)
						.setSingleChoiceLayout(R.layout.singlechoice_plain)
						.setSingleChoiceItems(R.array.click_search_mode_info, PDICMainAppOptions.singleTapSchMode(), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								PDICMainAppOptions.singleTapSchMode(which);
								switchToSingleTapSch();
								if(finalDd !=null) ((BaseAdapter) finalDd.getListView().getAdapter()).notifyDataSetChanged();
								dialog.dismiss();
							}
						}).create();
				dlg.isDark = GlobalOptions.isDark;
				if (dd!=null) dd.tag = dlg;
			}
			ViewUtils.ensureWindowType(dlg, a, null);
			dlg.show();
			dlg.getWindow().setDimAmount(0);
		} else {
			if(dd==null || dd.isDark!=GlobalOptions.isDark) {
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
//							ClickableSpan touching = opt.XYTouchRecorderInstance().getTouchingSpan(tv);
//							if (touching!=null) {
//								which = -1;
//							}
						}
					}
					if (which>=0) {
						// 设置搜索模式
						opt.tapSchMode(schMode = which%3);
						if(schMode==0) dictPicker.filterByRec(null, 0);
						modeBtn.setImageResource(schMode==0?R.drawable.ic_btn_siglemode:R.drawable.ic_btn_multimode);
						if(isVisible()) startTask(WordPopupTask.TASK_POP_SCH);
						dialog.dismiss();
					}
					if (which==-1) {
						showSchModeDialog(tkMultiV, true);
					}
				};
				dd = new AlertDialog.Builder(a)
						.setSingleChoiceLayout(R.layout.singlechoice_my)
						.setAdapter(new AlertController.CheckedItemAdapter(a, R.layout.singlechoice_my, android.R.id.text1, items, null){
							@Override
							public int getViewTypeCount() {
								return 2;
							}
							@Override
							public int getItemViewType(int position) {
								if (position==0) {
									return 1;
								}
								return super.getItemViewType(position);
							}
							@NonNull
							@Override
							public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
								View ret;
								TextView tv;
								if (position == 0) {
									if (view == null)
										view = a.getLayoutInflater().inflate(R.layout.single_tapsch_opt, parent, false);
									int tmp = a.tintListFilter.sForeground;
									a.tintListFilter.sForeground = GlobalOptions.isDark ? Color.WHITE:0xFF202020;
									ViewUtils.setForegroundColor((ViewGroup) view, a.tintListFilter);
									a.tintListFilter.sForeground = tmp;
									ViewUtils.setOnClickListenersOneDepth((ViewGroup) view, WordPopup.this, 9, null);
									ret = view;
									ViewGroup vg = (ViewGroup) view;
									tv = (TextView) vg.getChildAt(0);
									vg = (ViewGroup) vg.getChildAt(1);
									if (vg.getTag()==null) vg.setTag(a.mResource.getDrawable(R.drawable.frame_blue_btn));
									((Drawable) (vg.getTag())).setColorFilter(GlobalOptions.isDark ? GlobalOptions.NEGATIVE:null);
									for (int i = 0; i < 3; i++) {
										boolean bBtnSeled = PDICMainAppOptions.singleTapSchMode()==i;
										View _number = vg.getChildAt(i);
										if (_number.getTag()==null) _number.setTag(_number.getBackground());
										if (bBtnSeled ^ (_number.getBackground()==vg.getTag())) {
											_number.setBackground((Drawable) (bBtnSeled?vg.getTag():_number.getTag()));
										}
										//_number.setBackground((Drawable) vg.getTag());
									}
								} else {
									ret = super.getView(position, view, parent);
									tv = (TextView) ret;
								}
								if (schMode==position ^ TextUtils.regionMatches(tv.getText(), 0, " >> ", 0, 4)) {
									if (schMode == position) {
										tv.setText(" >> " + tv.getText());
									} else {
										tv.setText(tv.getText().toString().substring(4));
									}
								}
								return ret;
							}
						}, listener)
						.setSingleChoiceItems(items, 0, listener)
						.setTitle("切换词典模式").create();
				tkMultiV.setTag(new WeakReference<>(dd));
				dd.getListView().setTag(dd);
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
				dd.isDark = GlobalOptions.isDark;
			}
			ViewUtils.ensureWindowType(dd, a, null);
			dd.show();
			dd.getWindow().setDimAmount(0);
		}
	}
	
	public final boolean pin() {
		return bForcePin || a.mDialogType==WindowManager.LayoutParams.TYPE_APPLICATION
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
		resetPreviewMidPos();
	}
	
	private String previewEntryAt(int pos) {
		if (rec == null) {
			CMN.debug("CCD.bookImpl::", CCD, CCD.bookImpl);
			if(pos<0||pos>=CCD.bookImpl.getNumberEntries())
				return "";
			return CCD.bookImpl.getEntryAt(pos);
		} else {
			if(pos<0||pos>=rec.size())
				return "";
			return rec.getResAt(a, pos).toString();
		}
	}
	
	private void refillPreviewEntries(AlertDialog dialog, boolean delay) {
		if(isPreviewDirty)
		{
			int base = previewMidPos;
			if(rec == null) base--;
			if(previewPageIdx==0) {
				previewEntryData[2] = previewEntryAt(base);
				previewEntryData[3] = "切换上一词典";
				previewEntryData[4] = previewEntryAt(base+1);
				previewEntryData[5] = "工具…";
				previewEntryData[6] = previewEntryAt(base+2);
				previewEntryData[7] = "编辑搜索词";
			} else {
				if(previewPageIdx<0) base+=previewPageIdx*6;
				else base+=3+(previewPageIdx-1)*6;
				for (int i = 0; i < 6; i++) {
					previewEntryData[2+i] = previewEntryAt(base + i/2 + (i%2==0?0:3));
				}
			}
			if (dialog != null) {
				RecyclerView rv = (RecyclerView) dialog.tag;
				rv.getAdapter().postDataSetChanged(rv, delay?180:10);
				((TextView)dialog.findViewById(android.R.id.button3)).setText("重置"+(previewPageIdx==0?"":" ("+previewPageIdx+")"));
			}
		}
	}
	
	int previewMidPos;
	int previewPageIdx;
	String[] previewEntryData = new String[]{
			"收藏当前词条"
			, "切换词典分组"
			, "编辑搜索词"
			, "joy"
			, "切换上一词典"
			, "happy"
			, "工具…"
			, "fun"
	};
	
	public boolean nav(boolean isGoBack) {
		WebViewmy navWV = dictView(false);
		if (isGoBack && navWV.canGoBack()) {
			navWV.goBack();
			return true;
		} else if (!isGoBack && navWV.canGoForward()){
			navWV.goForward();
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
	
	public void setTranslator(@NonNull BookPresenter ccd, int pos) {
		if (ccd != null) { // todo
			if (CCD!=ccd) {
				CCD=ccd;
				currentPos = pos;
				resetPreviewMidPos();
				if(pos<0) pos=-1-pos;
				displaying=ccd.getRowTextAt(pos);
				if(displaying==null) displaying = popupKey;
				entryTitle.setText(displaying);
				//popupWebView.SelfIdx = CCD_ID = record.value[0];
				indicator.setText(ccd.getDictionaryName());
				popuphandler.setBook(ccd);
				dictPicker.dataChanged();
				dictPicker.scrollThis();
			} else if(currentPos!=pos) {
				currentPos = pos;
				resetPreviewMidPos();
				if(pos<0) pos=-1-pos;
				displaying=ccd.getRowTextAt(pos);
				if(displaying==null) displaying = popupKey;
				entryTitle.setText(displaying);
			}
		}
	}
	
	private void resetPreviewMidPos() {
		previewMidPos = currentPos>=0?currentPos:-currentPos-2;
	}
	
	public void init() {
		if (mWebView == null) {
			WebViewListHandler weblist = this.weblistHandler/*faked*/ = new WebViewListHandler(a, a.contentUIData/*faked*/, SearchUI.TapSch.MAIN);
			popupContentView = (ViewGroup) a.getLayoutInflater()
					.inflate(R.layout.float_contentview_basic, a.root, false);
			popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
			toolbar = (ViewGroup) popupContentView.getChildAt(0);
			splitView = (LinearSplitView) popupContentView.getChildAt(1);
			RLContainerSlider pageSlider = weblist.pageSlider = (RLContainerSlider) splitView.getChildAt(0);
			splitter = (ViewGroup) popupContentView.getChildAt(3);
			dictPicker = new DictPicker(a, splitView, splitter, -1);
			if (PDICMainAppOptions.wordPopupRemDifferenSet()) {
				dictPicker.planSlot = "WordPlanName";
			} else {
				dictPicker.planSlot = null;
			}
			dictPicker.wordPopup = this;
			dictPicker.loadManager = this.loadManager;
			dictPicker.autoScroll = true;
			PageSlide page = pageSlider.page = (PageSlide) pageSlider.getChildAt(0);
			WebViewmy webview = (WebViewmy) pageSlider.getChildAt(1);
			if (invoker==null) {
				invoker = webview;
			}
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
			toolbar.setTag(weblist);
			pottombar.setTag(weblist);
			popupChecker = pottombar.findViewById(R.id.popChecker);
			popupChecker.setChecked(PDICMainAppOptions.getPinTapTranslator());
			weblist.etSearch = entryTitle = toolbar.findViewById(R.id.popupText1);
			if (Build.VERSION.SDK_INT < 27) {
				entryTitle.setPadding(0, 0, 0, 0);
			}
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
			webview.setFocusable(true);
			webview.setFocusableInTouchMode(true);
			
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
			moveView = new PopupTouchMover(a, entryTitle, popupGuarder, this);
			for (int i = 0; i < toolbar.getChildCount(); i++) {
				toolbar.getChildAt(i).setOnTouchListener(moveView);
			}
			for (int i = 0; i < pottombar.getChildCount(); i++) {
				pottombar.getChildAt(i).setOnTouchListener(moveView);
			}
			
			if (false) {
				weblist.toolsBtn = toolbar.findViewById(R.id.tools);
			}
			weblist.toolsBtn = pageSlider.findViewById(R.id.tools);
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
		launch(true);
	}
	
	WordPopupTask wordPopupTask = new WordPopupTask(this);
	
	private void launch(boolean sch) {
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
		if (forcePinTarget != null) {
			targetRoot = forcePinTarget;
		} else if (invoker!=null && invoker.weblistHandler.isPopupShowing()) {
			targetRoot = invoker.weblistHandler.alloydPanel.settingsLayoutHolder;
		}
		if(lastTargetRoot != targetRoot) {
			if(lastTargetRoot!=null) dismiss();
			lastTargetRoot = targetRoot;
		}
		
		if (bFromWebTap) {
			VU.setVisible(popupGuarder, false);
			dismissImmediate();
			if (!invoker.weblistHandler.bShowingInPopup)
			{
				boolean newWnd = PDICMainAppOptions.tapDefInNewWindow1();
				if (invoker.merge/* && invoker.weblistHandler.isMultiRecord() && !invoker.weblistHandler.isFoldingScreens()*/) {
					newWnd = PDICMainAppOptions.tapDefInNewWindowMerged();
				}
				else if (ViewUtils.getNthParentNonNull(invoker.rl, 1).getId()==R.id.webholder) {
					newWnd = PDICMainAppOptions.tapDefInNewWindow2();
				}
				//CMN.debug("newWnd::", newWnd, invoker.weblistHandler.isMultiRecord(), ViewUtils.getNthParentNonNull(invoker.rl, 1).getId()==R.id.webSingleholder);
				if (newWnd)
				{
					WebViewListHandler wlh = a.getRandomPageHandler(true, false, invoker.presenter);
					wlh.popupContentView(null, null);
					invoker = wlh.getMergedFrame();
				}
			}
		} else {
			AttachViews();
			show();
			entryTitle.setText(popupKey);
		}
		if (popupKey!=null && sch) {
			//SearchOne(task, taskVer, taskVersion);
			boolean singleThread = false;
			if (schMode==0 && PDICMainAppOptions.singleTapSchMode()==2 || popupForceId!=null) {
				singleThread = true;
			}
			if (singleThread) {
				PerformSearch(TASK_POP_SCH, singleTask, 0, singleTaskVer);
			} else {
				startTask(TASK_POP_SCH);
			}
		}
	}
	
	public void startTask(int type) {
		if(!wordPopupTask.start(type)) {
			wordPopupTask.stop();
			wordPopupTask = new WordPopupTask(this);
			boolean ret = wordPopupTask.start(type);
			CMN.Log("新开线程……", ret, CMN.now());
		}
	}
	
	public void stopTask() {
		wordPopupTask.taskRunning.set(false);
	}
	
	private void reInit() {
		//CMN.Log("popupWord", popupKey, x, y, frameAt);
		boolean isNewHolder;
		// 初始化核心组件
		isInit = isNewHolder = mWebView == null || mWebView.fromCombined!=2;
		init();
		WeakReference<ViewGroup> holder = (PDICMainAppOptions.getImmersiveClickSearch() ? popupCrdCloth : popupCmnCloth);
		ViewGroup mPopupContentView = popupContentView;
		popupContentView = holder == null ? null : holder.get();
		boolean b1 = popupContentView == null;
		isNewHolder = isNewHolder || b1;
		View cv = (View) weblistHandler.pageSlider.getParent();
		if (appbar != null) {
			appbar.resetStretchViews();
		}
		if (b1 || popupContentView != cv.getParent()) {
			//CMN.debug("给你换身衣裳!!!");
			ViewUtils.removeView(mPopupContentView);
			ViewUtils.removeView(toolbar);
			ViewUtils.removeView(cv);
			ViewUtils.removeView(pottombar);
			if (PDICMainAppOptions.getImmersiveClickSearch()) {
				popupContentView = (popupCrdCloth != null && popupCrdCloth.get() != null) ? popupCrdCloth.get()
						: (popupCrdCloth = new WeakReference<>((ViewGroup) a.getLayoutInflater()
						.inflate(R.layout.float_contentview_coord, a.root, false))).get();
				appbar = popupContentView.findViewById(R.id.appbar);
				ViewUtils.addViewToParent(toolbar, appbar);
				ViewUtils.addViewToParent(cv, popupContentView);
				ViewUtils.addViewToParent(pottombar, popupContentView);
				((CoordinatorLayout.LayoutParams) pottombar.getLayoutParams()).gravity = Gravity.BOTTOM;
				((CoordinatorLayout.LayoutParams) pottombar.getLayoutParams()).setBehavior(/*PDICMainAppOptions.strechImmersiveMode()?null:*/new BottomNavigationBehavior());
				CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) cv.getLayoutParams();
				AppBarLayout.ScrollingViewBehavior beh = a.getScrollBehaviour(false);
				lp.setBehavior(beh);
				lp.height = CoordinatorLayout.LayoutParams.MATCH_PARENT;
				lp.topMargin = 0;
				lp.bottomMargin = 0;
				((AppBarLayout.LayoutParams) toolbar.getLayoutParams()).setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
				barSz.sz = pottombar.getLayoutParams().height;
				if (beh.strech) {
					//appbar.addStretchView(pottombar, barSz, false);
					appbar.addStretchView(splitView, barSz, 1);
				}
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
				((FrameLayout.LayoutParams) cv.getLayoutParams()).topMargin = (int) a.mResource.getDimension(R.dimen._45_);
				((FrameLayout.LayoutParams) cv.getLayoutParams()).bottomMargin = (int) a.mResource.getDimension(R.dimen._35_);
				((FrameLayout.LayoutParams) pottombar.getLayoutParams()).gravity = Gravity.BOTTOM;
				appbar = null;
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
			CMN.debug(e);
		}
		
		if (isNewHolder) {
			mWebView.fromCombined = 2;
			MainColorStamp = 0;
			refresh();
			popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
			FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) popupContentView.getLayoutParams());
			lp.height = moveView.FVH_UNDOCKED = (int) (a.dm.heightPixels * 7.0 / 12 - a.getResources().getDimension(R.dimen._20_));
			if (mPopupContentView != null && !isInit) {
				popupContentView.setTranslationY(mPopupContentView.getTranslationY());
				//int h=mPopupContentView.getLayoutParams().height;
				//if (h>0) lp.height = h;
			}
			lp = ((FrameLayout.LayoutParams) popupContentView.getLayoutParams());
			if (moveView.bottomGravity = wordCamera != null) {
				lp.gravity = Gravity.BOTTOM;
			} else {
				lp.gravity = Gravity.TOP;
			}
		}
	}
	
	ViewGroup lastTargetRoot;
	private boolean bForcePin;
	private ViewGroup forcePinTarget;
	
	private void AttachViews() {
		// 初次添加请指明方位
		if (/*!pin() && */!isVisible()) {
			if (moveView.FVDOCKED && moveView.Maximized && PDICMainAppOptions.getResetMaxClickSearch()) {
				if (wordCamera==null || true)
				{
					moveView.Dedock();
				}
			}
			if (!pin()) {
				ViewGroup targetRoot = lastTargetRoot;
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
								CMN.debug(e);
							}
						}
						float pad = 56 * a.dm.density;
						if (a instanceof FloatSearchActivity)
							now += ((FloatSearchActivity) a).getPadHoldingCS();
	//					CMN.debug("now",now);
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
		}
		//else popupWebView.loadUrl("about:blank");
		//CMN.recurseLog(popupContentView, null);
	}
	
	@AnyThread
	public void SearchNxt(boolean nxt, AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		resetPreviewIdx();
		int idx=-1, cc=0;
		String key = false?ViewUtils.getTextInView(entryTitle).trim():popupKey;
		CMN.debug("SearchNxt::", key);
		if(!TextUtils.isEmpty(key)) {
			String keykey;
			boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
			int SearchMode = PDICMainAppOptions.singleTapSchMode();
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
					if(CCD.getIsWebx()){
						PlainWeb webx = (PlainWeb) CCD.bookImpl;
						if(webx.takeWord(key)) {
							CCD.SetSearchKey(key);
							idx=0;
							break;
						}
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
				this.CCD_ID=CCD_ID;
				sching=CCD;
				currentPos = idx;
				resetPreviewMidPos();
				harvest(); //下一个！
			}
		}
	}
	
	RBTree_additive _treeBuilder = new RBTree_additive();
	
	@AnyThread
	private void SearchMultiple(@NonNull AtomicBoolean task, int taskVer, @NonNull AtomicInteger taskVersion) {
		_treeBuilder.clear();
		int paragraphWords = 9;
		String searchText = popupKey;
		if(TextUtils.isEmpty(searchText))
			return;
		boolean isParagraph = BookPresenter.testIsParagraph(searchText, paragraphWords);
		//CMN.debug("isParagraph::", isParagraph);
		_treeBuilder.setKeyClashHandler(searchText);
		for (int i = 0; i < loadManager.md_size && task.get(); i++) {
			PlaceHolder phTmp = loadManager.getPlaceHolderAt(i);
			if (phTmp != null) {
				BookPresenter book = loadManager.md_get(i);
				try {
					if(book.getAcceptParagraph(searchText, isParagraph, paragraphWords)) {
						CrashHandler.hotTracingObject = book;
						_treeBuilder.resetRealmer(book.getId());
						book.bookImpl.lookUpRange(searchText, null, _treeBuilder, book.getId(),7, task, false);
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
	private void SearchOne(@NonNull AtomicBoolean task, int taskVer, @NonNull AtomicInteger taskVersion) {
		int idx = -1, cc = 0;
		resetPreviewIdx();
		final MainActivityUIBase.LoadManager loadMan = this.loadManager;
		//CMN.debug("SearchOne::", popupKey);
		if (popupKey != null) {
			BookPresenter bookForce = popupForceId;
			boolean forced;
			if (bookForce != null) {
				CCD = bookForce;
				CCD_ID = loadMan.md_findOrAdd(bookForce);
				popupForceId = null;
				forced = true;
			} else {
				CCD_ID = upstrIdx = Math.min(upstrIdx, loadMan.md_size -1);
				forced = false;
			}
			final int size = loadMan.md_size;
			CMN.debug("轮询开始::", CCD, CCD_ID, loadMan.md_get(CCD_ID));
			BookPresenter webx = null;
			boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
			final int SearchMode = PDICMainAppOptions.singleTapSchMode();
			if(use_morph ^ forms.contains(queryMorphs)) {
				if(use_morph) forms.add(queryMorphs);
				else forms.remove(queryMorphs);
			}
			if(use_morph) {
				queryMorphs.loadMan = a.loadManager;
				queryMorphs.rejected.clear(); // todo version control loadMan
			}
			boolean bForceJump = false;
			BookPresenter CCD = this.CCD;
			/* 仅搜索当前词典 */
			if (SearchMode == 2 || bookForce !=null) {
				CCD = loadMan.md_get(CCD_ID);
				if (CCD != a.EmptyBook) {
					if(CCD.getIsWebx()) webx = CCD; // always takeWord
					else  idx = CCD.bookImpl.lookUp(popupKey, true, forms);
				}
			}
			else {
				boolean proceed = true;
				/* 仅搜索指定点译词典 */
				if (SearchMode == 1) {
					a.bHasDedicatedSeachGroup=false;
					BookPresenter firstAttemp = null;
					FindCSD:
					while(task.get()) {
						BookPresenter mdTmp;
						int CSID;
						for (int i = 0; i < size; i++) {
							mdTmp = null;
							CSID = (i + CCD_ID) % size;
							if (PDICMainAppOptions.getTmpIsClicker(loadMan.getPlaceFlagAt(CSID))) {
								mdTmp = loadMan.md_get(CSID);
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
									idx = mdTmp.bookImpl.lookUp(popupKey, true, forms);
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
				if (proceed)/* 未指定点译词典 */
					while (task.get()) {
						if (cc > size)
							break;
						CCD_ID = CCD_ID % size;
						CCD = loadMan.md_get(CCD_ID);
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
							idx = CCD.bookImpl.lookUp(popupKey, true, forms);
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
			
			CMN.debug(CCD, "应用轮询结果", webx, idx, "SearchMode="+SearchMode);
			if (idx<0 && forced) {
				// start a plain search
				popupWord(invoker, popupKey, null, 0, false);
				return;
			}
			else if (idx >= 0 && CCD != a.EmptyBook && task.get() && taskVer == taskVersion.get()) {
				if (bForceJump && SearchMode == 1)
					mWebView.setTag(R.id.js_no_match, false);
				currentPos = idx;
				this.rec = null;
				sching = CCD;
				harvest(); //single!
			} else {
				currentPos = idx;
				this.rec = null;
				this.CCD = CCD;
			}
			
			resetPreviewMidPos();
		}
	}
	
	public void SearchDone() {
		final boolean b1 = wordCamera!=null && !wordCamera.isVisible();
		if (b1) {
			return;
		}
		requestAudio = PDICMainAppOptions.tapSchAutoReadEntry();
		//CMN.Log("SearchDone::", rec, currentPos, CCD);
		if (rec != null) {
			if(b1 && isMaximized()){
				wordCamera.onPause();
			}
			renderMultiRecordAt(0);
		}
		else {
			dictPicker.filterByRec(null, 0);
			if(sching!=null) {
				texts[0]=CMN.id(sching);
				setTranslator(sching, currentPos);
				sching = null;
			}
			if (currentPos >= 0 && CCD != a.EmptyBook) {
				if(b1 && isMaximized()){ // ??? 无效
					wordCamera.onPause();
				}
				boolean isWebx = CCD.getIsWebx();
				WebViewmy renderingWV = dictView(bFromWebTap);
				WebViewListHandler wlh = renderingWV.weblistHandler;
				if(!bFromWebTap)  renderingWV = getRenderingWV(isWebx ? CCD : null);
				wlh.setViewMode(null, 0, renderingWV);
				if (isWebx) { //todo 合并逻辑
					wlh.bMergingFrames = 1;
					indicator.setText(loadManager.md_getName(CCD_ID, -1));
					popuphandler.setBook(CCD);
					CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, renderingWV, currentPos);
					wlh.pageSlider.setWebview(renderingWV, null);
					setDisplaying(renderingWV.word());
				} else {
					loadEntry(0, true);
				}
				if(bFromWebTap)
					renderingWV.post(new Runnable() {
						@Override
						public void run() {
							wlh.textMenu(null);
						}
					});
			} else if(b1 && isMaximized()){
				dismiss();
			}
		}
		if (popupKey!=null && !PDICMainAppOptions.storeNothing() && PDICMainAppOptions.storeTapsch()) {
			a.addHistory(popupKey, SearchUI.TapSch.MAIN, weblistHandler, null);
		}
	}
	
	private WebViewmy getRenderingWV(BookPresenter webx) {
		WebViewmy nowView = dictView(false);
		WebViewmy standalone = mWebView;
		if (webx != null) {
			if (PDICMainAppOptions.tapschWebStandaloneReversed()) {
				if (webx.tapschWebStandaloneSet())
					standalone = webx.initViewsHolder(a);
			} else if(PDICMainAppOptions.tapschWebStandalone() && webx.tapschWebStandalone()){
				standalone = webx.initViewsHolder(a);
			}
			if (standalone!=mWebView && ViewUtils.isVisibleV2(standalone)
					&& standalone.getParent()!=weblistHandler.pageSlider && standalone.weblistHandler.isViewInUse(standalone)) {
				standalone = mWebView; // 复用失败  todo pooling
			}
			if (standalone != mWebView) {
				standalone.weblistHandler = weblistHandler;
			}
		}
		if (nowView != standalone) {
			ViewUtils.removeView(nowView);
		}
		if (ViewUtils.addViewToParent(standalone, weblistHandler.pageSlider, 1)) {
			((AdvancedNestScrollWebView)standalone).setNestedScrollingEnabled(PDICMainAppOptions.getImmersiveClickSearch());
		}
		return standalone;
	}
	
	private void renderMultiRecordAt(int pos) {
		if (pos < rec.size()) {
			rec.jointResult = rec.getJointResultAt(pos);
		}
		boolean bFromWebTap = pos==0&&this.bFromWebTap&&invoker!=null;
		final WebViewmy multiView = bFromWebTap?invoker:mWebView;
		WebViewListHandler wlh = multiView.weblistHandler;
		if(bFromWebTap) {wlh.bDataOnly = true; wlh.bShowInPopup=wlh.bShowingInPopup;}
		wlh.setViewMode(rec, bFromWebTap?1:isMergingFramesNum(), multiView);
		if(bFromWebTap) wlh.bDataOnly = false;
		multiView.presenter = a.weblistHandler.getMergedBook(); //todo opt
		if (multiView.wvclient != a.myWebClient) {
			multiView.setWebChromeClient(a.myWebCClient);
			multiView.setWebViewClient(a.myWebClient);
		}
		if (pos < rec.size()) {
			rec.renderContentAt(pos, a, null, wlh);
			setDisplaying(wlh.getMultiRecordKey());
		}
		wlh.pageSlider.setWebview(multiView, null);
		if(!bFromWebTap)
			dictPicker.filterByRec(rec, pos);
		setTranslator(rec, pos);
		if(bFromWebTap) {
			wlh.textMenu(null);
			multiView.currentPos = -1;
		}
	}
	
	private int isMergingFramesNum() {
		return /*PDICMainAppOptions.foldingScreenTapSch()*/schMode==1?2:1;
	}
	
	private void setDisplaying(String key) {
		WebViewmy view = dictView(bFromWebTap);
		if (view.weblistHandler!=weblistHandler) {
			view.presenter.setCurrentDis(view, view.currentPos);
		}
		if (requestAudio)
			view.bRequestedSoundPlayback=true;
		displaying = key;
		view.weblistHandler.setStar(key);
	}
	
	private void loadEntry(int d, boolean harvest) { // 翻页
		if (rec == null) {
			if (d!=0)  currentPos=Math.max(0, Math.min(currentPos+d, (int) CCD.bookImpl.getNumberEntries()));
			int pos = currentPos;
			WebViewmy flippingWV = dictView(d==0 && bFromWebTap);
			flippingWV.currentPos = pos;
			flippingWV.presenter = CCD;
			WebViewListHandler wlh = flippingWV.weblistHandler;
			if (CCD.getIsWebx()) { //todo 合并逻辑
				if (pos==0) {
					CCD.SetSearchKey(popupKey);
				}
				CCD.renderContentAt(-1, RENDERFLAG_NEW, 0, flippingWV, pos);
			} else {
				wlh.bMergingFrames = 1;
				StringBuilder mergedUrl = new StringBuilder("http://mdbr.com/content/");
				mergedUrl.append("d");
				IU.NumberToText_SIXTWO_LE(CCD.getId(), mergedUrl);
				mergedUrl.append("_");
				IU.NumberToText_SIXTWO_LE(pos, mergedUrl);
				if (invoker != null && invoker.toTag != null
						// &&  CCD==popupForceId
						&& CCD == invoker.presenter
						&& pos == invoker.currentPos
				) {
					mergedUrl.append("#").append(invoker.toTag);
					invoker.toTag = null;
				} else if(harvest){
					String proKey = mdict.processText(CCD.getBookEntryAt(pos));
					int proPos = pos+1;
					while(proPos < CCD.bookImpl.getNumberEntries()) {
						if (proKey.equals(mdict.processText(CCD.getBookEntryAt(proPos)))) {
							mergedUrl.append("_");
							IU.NumberToText_SIXTWO_LE(proPos, mergedUrl);
						} else {
							break;
						}
						proPos++;
					}
					
				}
				CMN.Log("mergedUrl", mergedUrl);
				flippingWV.loadUrl(mergedUrl.toString());
			}
			wlh.resetScrollbar(flippingWV, false, false);
			String word = CCD.getBookEntryAt(pos);
			flippingWV.word(word);
			setDisplaying(word);
		} else {
			if (d!=0)  currentPos=Math.max(0, Math.min(currentPos+d, (int) rec.size()));
			renderMultiRecordAt(currentPos);
		}
	}
	
	public void popupWord(WebViewmy invoker, String key, BookPresenter forceStartId, int frameAt, boolean bFromWebTap) {
		CMN.debug("popupWord::frameAt", frameAt, key, loadManager.md_size, invoker==null, WebViewmy.supressNxtClickTranslator, forceStartId);
		if(key==null || mdict.processText(key).length()>0)
		{
			if (invoker!=null) this.invoker = invoker;
			if (key!=null) popupKey = key;
			this.bFromWebTap = bFromWebTap;
			popupForceId = forceStartId;
			a.hdl.removeCallbacks(this);
			if (frameAt == -100) {
				launch(false);
			} else {
				if (invoker!=null && invoker.weblistHandler.pageSlider.tapZoom) { //todo ???
					a.hdl.postDelayed(this, SearchUI.tapZoomWait); // 支持双击操作会拖慢点译！
				} else {
					a.hdl.post(this);
				}
			}
		}
	}
	
	public void PerformSearch(int mType, AtomicBoolean task, int taskVer, AtomicInteger taskVersion) {
		if(mType==TASK_POP_SCH){
			if(schMode==0||popupForceId!=null) SearchOne(task, taskVer, taskVersion);
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
		else if(mType==TASK_UPD_SCH) {
			((PDICMainActivity)a).checkUpdate(task);
		}
		else if(mType==TASK_TTS) {
			((MainActivityUIBase)a).ttsHub.doSendText();
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
		if (wordCamera!=null && isMaximized() && wordCamera.isVisible()) {
			wordCamera.onResume();
		}
	}
	
	// sync from naved webview
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
		if(PDICMainAppOptions.getImmersiveClickSearch()!=PDICMainAppOptions.getImmersiveClickSearch(a.flags[2]))
			a.popupWord(null,null, 0, null, false);
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
		final WebViewmy scrollView = dictView(false);
		String url = scrollView.getUrl();
		int schemaIdx = url.indexOf(":");
		if(url.regionMatches(schemaIdx+3, "mdbr", 0, 4)){
			try {
				if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
					weblistHandler.resetScrollbar(scrollView, false, false);
				}
				else if (url.regionMatches(schemaIdx+12, "merge", 0, 5)) {
					weblistHandler.bMergingFrames = 1;
					weblistHandler.resetScrollbar(scrollView, true, true);
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
	
	public boolean isMaximized() {
		return moveView!=null && moveView.Maximized;
	}
	
	final WebViewmy dictView(boolean bFromWebTap) {
		if(bFromWebTap && invoker!=null) return invoker;
		return weblistHandler.dictView != null ? weblistHandler.dictView : mWebView;
	}
	
	public void forcePin(ViewGroup forcePinTarget) {
		this.forcePinTarget = forcePinTarget;
		this.bForcePin = forcePinTarget!=null;
		if (popupChecker != null) {
			ViewUtils.setVisible(popupChecker, !bForcePin);
			if (popupContentView!=null) {
				FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) popupContentView.getLayoutParams());
				if (moveView.bottomGravity = wordCamera != null) {
					popupContentView.setTranslationY(0);
					lp.gravity = Gravity.BOTTOM;
					popupContentView.setAlpha(isMaximized()?1:0.8f);
				} else {
					lp.gravity = Gravity.TOP;
					popupContentView.setAlpha(1);
				}
			}
		}
	}
}

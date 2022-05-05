package com.knziha.plod.plaindict;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.ActionMenuPresenter;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.animation.AnimationUtils;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayAdapterView.OnItemClickListener;
import com.jess.ui.TwoWayGridView;
import com.knziha.plod.PlainUI.SearchbarTools;
import com.knziha.plod.PlainUI.WordPopupTask;
import com.knziha.plod.db.MdxDBHelper;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.SimpleDialog;
import com.knziha.plod.widgets.SimpleTextNotifier;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.SplitView.PageSliderInf;
import com.knziha.plod.widgets.SplitViewGuarder;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.view.View.FOCUSABLE_AUTO;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;
import static com.knziha.plod.dictionarymodels.BookPresenter.def_zoom;
import static com.knziha.plod.plaindict.MainActivityUIBase.init_clickspan_with_bits_at;
import static com.knziha.plod.plaindict.Toastable_Activity.LONG_DURATION_MS;
import static com.knziha.plod.preference.SettingsPanel.makeDynInt;
import static com.knziha.plod.preference.SettingsPanel.makeInt;
import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

/** 翻阅模式，以词典为单位，搜索词为中心，一一览读。<br><br/>  代号：fye
 * This view makes it easier to read among or read against entries of various books. */
public class PeruseView extends DialogFragment implements OnClickListener, OnMenuItemClickListener, OnLongClickListener{
	int MainBackground;
	public ArrayList<Long> bookIds = new ArrayList<>();
	public ArrayList<Long> hidden = new ArrayList<>();
	public ArrayList<Long> schResult = new ArrayList<>();
	//public ViewGroup peruseF;
	public byte mTBtnStates;
	public boolean bSupressingEditing;
	public ArrayList<View> cyclerBin = new ArrayList<>();
	public ArrayList<View> recyclerBin = new ArrayList<>();
	private ArrayList<BookPresenter> md = new ArrayList<>();
	public ArrayList<PlaceHolder> ph = new ArrayList<>();
	BookPresenter BookEmpty;
	BookPresenter currentDictionary;
	ViewGroup main_pview_layout;
	private ViewGroup PeruseTorso;
	SplitView vBox;
	SplitView hBox;
	ViewGroup mlp;
	ViewGroup slp;
	public Toolbar toolbar;
	DBroswer DBrowser;
	/** The weblist who brings up this view */
	public WebViewListHandler invoker;
	
	private ViewGroup bottom;
	private ViewGroup bottombar;
	
	TwoWayGridView gridView;
	ListViewmy lv1;
	ListViewmy lv2;
	EditText etSearch;
	ImageView ivDeleteText;
	String schKey;
	String lastSchKey;
    int HeadlineInitialSize;
    float density;
	boolean fromLv1;
	String lstKey;
	boolean fromData;
	int fromLv1Idx;
	int fromLv1Idx_;

    int lvHeaderItem_length = 65;
    int lvHeaderItem_height = 60;
	public boolean bCallViewAOA=false;
	
	//bookeanMaskks
//	int adapter_idx;
//	int old_adapter_idx = -1;
	long bookId;
	private BookPresenter.AppHandler perusehandler;
	SimpleDialog mDialog;
	
	View selection;

	Toolbar PerusePageSearchbar;
	EditText PerusePageSearchetSearch;
	TextView PerusePageSearchindicator;
	String PerusePageSearchetSearchStartWord;
	private View handle1;
	private View handle2;
	
	public ViewGroup contentview;
	public ContentviewBinding contentUIData;
	public WebViewListHandler weblistHandler;
	private MenuBuilder AllMenus;
	List<MenuItemImpl> MainMenus;
	List<MenuItemImpl> PageMenus;
	private MenuItem firstMenu;
	MenuItem menuShowAll;
	MenuItem menuKeepGroup;
	MenuItem menuKeepBook;
	MenuItem menuPaused;
	
	SearchbarTools etTools;
	//构造
	public PeruseView(int mainBackground){
		super();
		setCancelable(false);
		MainBackground = mainBackground;
	}
	
	public PeruseView(){
		this(0);
	}
	
	int cc;
	float expandTarget =-1;
	float expandFrom;
	PDICMainAppOptions opt;
	int PositionToSelect=0;
	int PositionToCenter=0;
	int TargetRow;
	int NumRows=1;

	ImageView intenToLeft,intenToRight,intenToDown,lineWrap;

    int itemWidth,itemHeight;
    /** 左边的词条列表适配器 */
	LeftViewAdapter entryAdapter;
	/** 右边的词典书签列表适配器 */
	RightViewAdapter bmsAdapter;
	public int bookmarks_size;
	public int[] voyager=new int[0];
	public final int VELESIZE=3;

	Cursor cr = EmptyCursor;
	
	boolean bExpanded=false;
	TextWatcher tw1;
	boolean ToL=false,ToR=false,ToD=false,LnW=false;
	public WebViewmy mWebView;
	public ViewGroup root;
	BasicAdapter ActivedAdapter;

	int lastW;
	// creat dialog -> creat view
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//CMN.Log("----->onCreateView");
		if(container!=null) {
			root=container;
		}
		boolean shunt = main_pview_layout!=null;
		ViewGroup peruse_content = shunt?main_pview_layout:(ViewGroup) inflater.inflate(R.layout.fye_main, root,false);
		if(root==null){
			FrameLayout view = new FrameLayout(inflater.getContext());
			view.setId(R.id.root);
			view.addView(peruse_content);
			root=view;
			container=view;
		} else {
			container=peruse_content;
		}
		if(shunt) {
			//CMN.Log("复用视图");
			ViewUtils.removeIfParentBeOrNotBe(peruse_content, null, false);
			return container;
		} else {
			peruse_content.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					if(right-left!=lastW) {
						lastW=right-left;
						mlp.findViewById(R.id.frameL).getLayoutParams().width=lastW;
						if(true)lv1.getLayoutParams().width=lastW;
					}
					//CMN.Log("onLayoutChange!!!", right-left);
				}
			});
		}
  
		//peruse_content.setOnTouchListener((v, event) -> true);//tofo

        toolbar = peruse_content.findViewById(R.id.toolbar);
		ViewGroup topBar = toolbar.findViewById(R.id.pvSearch);
		etSearch = (EditText) topBar.getChildAt(0);
		ivDeleteText = (ImageView) topBar.getChildAt(1);
		
		ViewGroup PeruseTorso = (ViewGroup) peruse_content.getChildAt(1);
		bottom = (ViewGroup) peruse_content.getChildAt(2);
		bottombar = (ViewGroup) bottom.getChildAt(0);
		this.PeruseTorso = PeruseTorso;
		PeruseTorso.findViewById(R.id.split_view);
		
		TwoWayGridView gv = PeruseTorso.findViewById(R.id.main_dict_lst);
		gv.setHorizontalSpacing(0);
		gv.setVerticalSpacing(0);
		gv.setHorizontalScroll(true);
		gv.setStretchMode(GridView.NO_STRETCH);
		gv.setAdapter(gridAdapter);
		gv.setOnItemClickListener(gridAdapter);
		gv.setScrollbarFadingEnabled(false);
		gv.setSelector(ViewUtils.littleCat?getResources().getDrawable(R.drawable.listviewselector0):null);
		this.gridView = gv;
		
		SplitView vBox = PeruseTorso.findViewById(R.id.split_view);
		SplitView hBox = PeruseTorso.findViewById(R.id.secondary);
		this.vBox = vBox;
		this.hBox = hBox;
		
		if(opt.fyeGridBot()) {
			vBox.SwitchingSides();
		}
		
		SplitViewGuarder svGuard = (SplitViewGuarder) PeruseTorso.getChildAt(1);
		svGuard.SplitViewsToGuard.add(vBox);
		svGuard.SplitViewsToGuard.add(hBox);
		
		handle1  = vBox.findViewById(R.id.handle);
		handle2  = hBox.findViewById(R.id.inner_handle);
		
		hBox.addValve(intenToLeft = (ImageView) PeruseTorso.getChildAt(2));
		hBox.addValve(intenToRight = (ImageView) PeruseTorso.getChildAt(3));
		hBox.addValve(lineWrap = (ImageView) PeruseTorso.getChildAt(5));
		vBox.addValve(intenToLeft);
		vBox.addValve(intenToRight);
		vBox.addValve(intenToDown= (ImageView) PeruseTorso.getChildAt(4));
		vBox.guarded= hBox.guarded=true;
		
        toolbar.inflateMenu(R.xml.menu_fye);
		
		
		AllMenus = (MenuBuilder) toolbar.getMenu();
		MenuCompat.setGroupDividerEnabled(AllMenus, true);
//		MainMenus = ViewUtils.MapNumberToMenu(AllMenus, );
//		PageMenus = ViewUtils.MapNumberToMenu(AllMenus, );
		
		firstMenu = AllMenus.findItem(R.id.multiline);
		menuShowAll = AllMenus.findItem(R.id.showAll);
		menuKeepGroup = AllMenus.findItem(R.id.keepGroup);
		menuKeepBook = AllMenus.findItem(R.id.keepBook);
		menuPaused = AllMenus.findItem(R.id.pause);
		
		ActionMenuPresenter.OverflowMenuButton mp = toolbar.findViewById(R.id.action_menu_presenter);
		mp.beforeOpen = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String title=mWebView.bIsActionMenuShown?"翻阅模式":"多行编辑";
				if (!TextUtils.equals(firstMenu.getTitle(), title)) {
					firstMenu.setTitle(title);
					mp.clearPopup();
				}
			}
		};

		
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
		ViewUtils.ResizeNavigationIcon(toolbar);
        toolbar.setNavigationOnClickListener(this);
		toolbar.addNavigationOnClickListener((v,e) -> {
			if(etTools.isVisible()) {
				etTools.dismiss();
				etTools.hideIM();
				return false;
			}
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			if(a.wordPopup.isVisible()) {
				a.wordPopup.dismiss();
				return false;
			}
			return true;
		});
		ViewUtils.setOnClickListenersOneDepth(bottombar, this, 999, 0, null);
  
		View vTmp = toolbar.findViewById(R.id.action_menu_presenter);
		if(vTmp!=null) {
			vTmp.setOnLongClickListener(this);
		}
		
		mlp = hBox.findViewById(R.id.mlp);
		slp = hBox.findViewById(R.id.slp);
		lv1 = mlp.findViewById(R.id.main_list);
		lv2 = slp.findViewById(R.id.sub_list);
//		lv1.setVerticalScrollBarEnabled(false);//关闭不可控的安卓科技
//		lv1.setHorizontalScrollBarEnabled(true);//关闭不可控的安卓科技
		if(!opt.getShowFScroll()) {
//			lv1.setFastScrollEnabled(false);
//			lv2.setFastScrollEnabled(false);
		}
		lv1.setFastScrollEnabled(true);
		lv2.setFastScrollEnabled(true);
		lv1.setVerticalScrollBarEnabled(true);
		lv1.setHorizontalScrollBarEnabled(true);
		
		//tc
		etSearch.addTextChangedListener(tw1=new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(TextUtils.getTrimmedLength(s)>0) {
					if (etFlag!=1) {
						etFlag=1;
					}
					schKey = s.toString().trim();
					if (!paused() || start==-1) {
						int ret = currentDictionary.bookImpl.lookUp(schKey, false);
						if(ret!=-1) {
							lv1.setSelectionFromTop(ret, (int) (20*density));
							if(ToR && cvpolicy)
								if(count==-1 || mdict.processText(currentDictionary.bookImpl.getEntryAt(ret)).equals(mdict.processText(s.toString())))
									entryAdapter.click(ret,false,false);
						}
					}
					if(etTools.isVisible())
						etTools.dismiss();
				}
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length()>0)
					ivDeleteText.setVisibility(View.VISIBLE);
			}});
	
        ivDeleteText.setOnClickListener(this);
        
        ViewUtils.setOnClickListenersOneDepth(PeruseTorso, this, 1, 2, null);
        
		itemWidth = (int) (lvHeaderItem_length * density);
        itemHeight = (int) (lvHeaderItem_height * density);
        cc = dm.widthPixels/itemWidth; //一行容纳几列
        if(dm.widthPixels - cc*itemWidth>0.85*itemWidth)
        	cc++;

        vBox.setPageSliderInf(new PageSliderInf() {
			@Override
			public void SizeChanged(int newSize,float MoveDelta) {
				View child = gv.getChildAt(0);
				if(child==null) return;
		        cc = dm.widthPixels/itemWidth; //一行容纳几列
		        if(dm.widthPixels - cc*itemWidth>0.85*itemWidth)
		        	cc++;
				if(newSize>2*itemHeight) {
					//a.showT(""+MoveDelta);
					if(!bExpanded) {
						//perform expansion
						//a.showT("expanding...");
						bExpanded=true;

						PositionToSelect = gv.getFirstVisiblePosition();
						if(child.getLeft()<-0.5*itemWidth)
							PositionToSelect++;

						for(int i = 0; i< gv.getChildCount(); i++) {
							View childAt = gv.getChildAt(i);
							if(childAt.getRight()>gv.getWidth()/2) {
								PositionToCenter = ((DictTitleHolder) childAt.getTag()).pos;
								break;
							}
						}
						
						NumPreEmpter = opt.fyeGridPad()?(cc - PositionToSelect%cc)%cc:0;
						for (int i = cyclerBin.size(); i < NumPreEmpter; i++) {
							View vt = new View(getContext());
							int itemWidth = (int) (lvHeaderItem_length * density);
							int itemHeight = (int) (lvHeaderItem_height * density);
							TwoWayGridView.LayoutParams lp = new TwoWayGridView.LayoutParams(itemWidth, itemHeight);
							vt.setLayoutParams(lp);
							vt.setOnClickListener(PeruseView.this);
							cyclerBin.add(vt);
						}
						TargetRow = (PositionToSelect+NumPreEmpter)/cc;
						NumRows =  (int) Math.ceil(((float)bookIds.size()+NumPreEmpter)/cc);
				        gv.setNumColumns(cc);
				        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dm.widthPixels, -1);
				        gv.setLayoutParams(params);
				        gv.setHorizontalScroll(false);
			        	gv.setSelection(PositionToSelect+NumPreEmpter);
						gv.postOnAnimationDelayed(gridAdapter, 100);
					}
				}
				else if(newSize>=1.2*itemHeight) {
					/* transition */
					//a.showT("entering transition...");
					if(!bExpanded) {
//						int fvp = gv.getFirstVisiblePosition();//当前行起始位置
//						if(expandTarget ==-1) {
//							int delta= child.getLeft();
//							expandFrom = (fvp * itemWidth-delta);
//							expandTarget = opt.fyeGridPad()?(fvp+(delta<=-0.5*itemWidth?1:0) * itemWidth)
//									:(float) (Math.floor(fvp*1.f/cc) * cc * itemWidth);
//						}
//						float alpha = (float) ((2*itemHeight - newSize)/(0.8*itemHeight));
//						alpha=Math.max(0, Math.min(1, alpha));
//						float CurrentScrollX = (fvp*(itemWidth)- child.getLeft());
//						//a.showT(CurrentScrollX+" to "+ft+"-"+md.get(data.get(FVP))._Dictionary_fName);
//						//a.showT("firstVisiblePos="+LvHeadline.getFirstVisiblePosition()+" leftOffset="+LvHeadline.getChildAt(0).getLeft());
//
//						float Target = ((1-alpha)* expandTarget +alpha* expandFrom);
//						gv.smoothScrollBy((int) (Target-CurrentScrollX),60);
					}
				}
				else if(newSize<=itemHeight+1){
					/* collapse */
					if(bExpanded) {
						//a.showT("collapsing..."+PositionToSelect);
						bExpanded=false;
						expandTarget =-1;
						NumPreEmpter=0;
						
						
				        gv.setNumColumns(bookIds.size());
				        gv.getLayoutParams().width = -1;
				        gv.getLayoutParams().height = -1;
						gv.requestLayout();

				        gv.setHorizontalScroll(true);
				        gv.setSelection(PositionToSelect);
				        //LvHeadline.postInvalidate();
				        //LvHeadline.invalidate();
				        
				        gv.postDelayed(() -> {
							scrollGridToCenter(PositionToCenter);
//							for(int i = 0; i< gv.getChildCount(); i++) {
//								gv.getChildAt(i).setTop(0);
//								gv.getChildAt(i).setBottom((int) (lvHeaderItem_height * density));
//							}
						},160);
					}
				}
				
				if(bExpanded && newSize>HeadlineInitialSize) {
//					if(MoveDelta<0){//shrinking
//						int FVP = gv.getFirstVisiblePosition();//当前行起始位置
//						int delta= child.getTop();
//						int TargetRowPos = TargetRow*itemHeight;
//						int CurrentScrollYTop = (FVP/cc)*itemHeight - delta;
//						//int maxScroll = CurrentScrollYTop-TargetRowPos;
//						//a.showT(TargetRow+"@"+TargetRowPos+":"+CurrentScrollYTop);
//						int CurrentScrollYBottom = CurrentScrollYTop+ gv.getHeight();
//						if(TargetRowPos<=CurrentScrollYBottom-itemHeight && TargetRowPos>CurrentScrollYTop){ //do regular move
//							if(CurrentScrollYTop-MoveDelta>TargetRowPos)
//								MoveDelta = CurrentScrollYTop-TargetRowPos;
//							gv.smoothScrollBy(-(int) MoveDelta,60);
//						} else if(TargetRowPos>CurrentScrollYBottom-itemHeight) {//bottom up
//							gv.smoothScrollBy(TargetRowPos-CurrentScrollYBottom+itemHeight+10,200);
//						} else if(TargetRowPos<CurrentScrollYTop) {//top down
//							gv.smoothScrollBy(TargetRowPos-CurrentScrollYTop-10,200);
//						}
//						//a.showT(TargetRowPos+":"+CurrentScrollYTop);
//					}
				}
			}

			@Override
			public void onDrop(int size) {
				expandTarget =-1;
			}

			@Override
			public int preResizing(int size) {
				if(size<lvHeaderItem_height * density)
						size = (int) (lvHeaderItem_height * density);
				else if(bExpanded&&NumRows>1) if(size>lvHeaderItem_height * density * NumRows)
					size = (int) (lvHeaderItem_height * density * NumRows);
				return size;
			}

			@Override
			public void onPreparePage(int orgSize) {}

			@Override
			public void onMoving(SplitView s,float val) {}

			@Override
			public void onPageTurn(SplitView s) {}

			@Override
			public void onHesitate() {}});
		
		main_pview_layout = peruse_content;
		
		return container;
	}

	@NonNull @Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		CMN.Log("PeruseView----->onCreateDialog");
		if(mDialog==null){
			mDialog = new SimpleDialog(requireContext(), getTheme());
			mDialog.mBCL = new SimpleDialog.BCL(){
				@Override
				public void onBackPressed() {
					goBack();
				}
				@Override
				public void onActionModeStarted(ActionMode mode) {
					getMainActivity().onActionModeStarted(mode);
				}

				@Override
				public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
					switch (keyCode) {
						case KeyEvent.KEYCODE_VOLUME_DOWN: {
							if(opt.getPeruseUseVolumeBtn()) {
								contentUIData.browserWidget11.performClick();
								return true;
							}
						}
						case KeyEvent.KEYCODE_VOLUME_UP: {
							if(opt.getPeruseUseVolumeBtn()) {
								contentUIData.browserWidget10.performClick();
								return true;
							}
						}
					}
					return false;
				}
			};
		}
		//else CMN.Log("复用dialog");
		Window win = mDialog.getWindow();
		if(win!=null){
			ViewGroup content = win.findViewById(android.R.id.content);
			if(content!=null) {
				root=content;
			}
			Toastable_Activity.setStatusBarColor(win, MainBackground);
			//win.setStatusBarColor(CMN.MainBackground);
			View view = win.getDecorView();
			view.setBackground(null);

			WindowManager.LayoutParams layoutParams = win.getAttributes();
			layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.horizontalMargin = 0;
			layoutParams.verticalMargin = 0;
			layoutParams.dimAmount = 0;
			win.setAttributes(layoutParams);

			Toastable_Activity.setWindowsPadding(view);

			View t = win.findViewById(android.R.id.title);
			if(t!=null) t.setVisibility(View.GONE);
			int id = Resources.getSystem().getIdentifier("titleDivider","id", "android");
			if(id!=0){
				t = win.findViewById(id);
				if(t!=null) t.setVisibility(View.GONE);
			}
			if(t!=null) t.setVisibility(View.GONE);
			win.setSoftInputMode(PDICMainActivity.softModeResize);
		}
		return mDialog;
	}

	public void onViewAttached(MainActivityUIBase a, boolean newSch){
		if(a==null || main_pview_layout==null) return;
		//CMN.Log("onViewAttached", schKey, newSch, fromData);
		hidden.clear();
		WebViewListHandler pPanel = a.weblist;
		if (this.invoker!=pPanel && (pPanel.bShowingInPopup || pPanel.bDataOnly&&!a.wordPopup.pin())) {
			//getMainActivity().showT("onViewAttached::"+pPanel.bDataOnly);
			// re-attach for different dialogs so that this view is always in front of the invoker.
			if (mDialog!=null) {
				//dismiss();
				//show(a.getSupportFragmentManager(), "PeruseView");
				mDialog.setOnDismissListener(null);
				mDialog.dismiss();
				mDialog.show();
				//getMainActivity().showT("onViewAttached::1::区区对话框，装什么大尾巴狼::"+pPanel.bDataOnly);
			}
			this.invoker = pPanel;
		}
		if(!ToD) {
			bmsAdapter.notifyDataSetChanged();
		}
		
		if(newSch) {
			etSearch.setText(schKey);
			etTools.addHistory(schKey);
			boolean b = gridView.getItemCount()!=bookIds.size();
			//CMN.Log("newSch!", fromData, keepBook(), keepGroup(), b);
			if(fromData || b) {
				boolean b1=keepBook(), b2=keepGroup();
				if(!(b1 || b2) || b) {
					if(b1) fromLv1Idx = bookIds.indexOf(bookId);
					resetGrid(a); // newSch
				} else if(!b1 && b2){
					bookId = a.currentDictionary.getId();
					focusGridAuto(false);
				}
			}
		}
	}
	
	public boolean isWindowDetached() {
		return isDetached()||getDialog()==null|| ViewUtils.isWindowDetached(getDialog().getWindow());
	}
	
	public boolean removeContentViewIfAttachedToRoot() {
		if(contentview.getParent()!=null && ViewUtils.removeIfParentBeOrNotBe(contentview, main_pview_layout, false)) {
			cvpolicy=false;
			return true;
		}
		return false;
	}
	
	static class DictTitleHolder
	{
		public long bid;
		public int pos;
		FlowTextView tv;
		ImageView cover;
		TextView word;
		public DictTitleHolder(long pos, View view)
		{
			bid = pos;
			tv = view.findViewById(R.id.text);
			cover = view.findViewById(R.id.image);
			word = view.findViewById(R.id.word);
			view.setTag(this);
		}
		
		public void setTextColor(int ColorInt) {
			tv.setTextColor(ColorInt);
			tv.invalidate();
		}
	}
	
	private void resetGrid(MainActivityUIBase a) {
		CMN.Log("resetGrid!", bookIds.size());
		NumPreEmpter=0;
		int NumToAdd = bookIds.size()-recyclerBin.size();
		for(int i=0;i<NumToAdd;i++) {
			View v = a.getLayoutInflater().inflate(R.layout.fye_dict, gridView, false);
			TwoWayGridView.LayoutParams lp = new TwoWayGridView.LayoutParams(itemWidth, itemHeight);
			v.setLayoutParams(lp);
			((LayerDrawable) v.getBackground()).getDrawable(0).setAlpha(0);
			new DictTitleHolder(bookIds.get(i), v).pos = recyclerBin.size();
			recyclerBin.add(v);
		}
		if(!bExpanded)
			gridView.setSelection(0);
		gridView.getLayoutParams().width = dm.widthPixels;
		gridAdapter.notifyDataSetChanged();
		//gridView.requestLayout();
		cc = dm.widthPixels/itemWidth; //一行容纳几列
		if(dm.widthPixels - cc*itemWidth>0.85*itemWidth) cc+=1;
		if(bExpanded) {
			double size = Math.ceil(1.0 * bookIds.size()/cc)*itemHeight;
			if(vBox.getPrimaryContentSize()>size)
				vBox.setPrimaryContentSize((int)size);
		}

		voyager=new int[bookIds.size()*VELESIZE];
		for(int i=0;i<bookIds.size();i++)
			voyager[i*VELESIZE]=-1;
		//entryAdapter.lastClickedPos=-1;
		gridAdapter.flip=true;
		mWebView.clearIfNewADA(currentDictionary); // a.md_get(off<data.size()?data.get(off):-1)
		focusGridAuto(true);
	}
	
	private long bookIds_get(int pos) {
		return pos >= 0 && pos < bookIds.size() ? bookIds.get(pos) : -1;
	}
	
	void focusGridAuto(boolean list) {
		if (bookIds_get(fromLv1Idx)!=bookId) {
			fromLv1Idx = bookIds.indexOf(bookId);
		}
		int off=fromLv1Idx;
		if(off==-1) off=0;
		gridAdapter.onItemClick(null,null,NumPreEmpter+off, list ? -1 : -2);
		scrollGridToCenterNaive(off);
	}
	
	public void onViewDetached() {
		MainActivityUIBase a = getMainActivity();
		
		currentDictionary.bmCBI=lv2.getFirstVisiblePosition();
		currentDictionary.bmCCI= bmsAdapter.lastClickedPos;
		//currentDictionary = null;
		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
		spsubs = hBox.getPrimaryContentSize()*1.f/dm.widthPixels;
		
		a.opt.defaultReader.edit().putFloat("spsubs", spsubs)
		.putInt("PBBS", contentUIData.webcontentlister.getPrimaryContentSize()).apply();
		
		a.opt.putFirstFlag();
		a.OnPeruseDetached();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		CMN.Log("----->onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		MainActivityUIBase a = getMainActivity();
		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
		syncData(a);
		
		if(contentUIData==null) {
			initViews(a);
		}
		
		refreshUIColors(a.MainBackground);
		
        if(ToL = opt.getPerUseToL())
			intenToLeft.setBackgroundResource(R.drawable.toleft);
        if(ToR = opt.getPerUseToR())
			intenToRight.setBackgroundResource(R.drawable.toright);
		if(!(ToD = opt.getPerUseToD())) {
			intenToDown.setBackgroundResource(R.drawable.stardn1);
			intenToLeft.setVisibility(View.GONE);
		}

		if(opt.getPeruseAddAll()) menuShowAll.setChecked(true);
		if(PDICMainAppOptions.fyeKeepGroup()) menuKeepGroup.setChecked(true);
		if(PDICMainAppOptions.fyeKeepBook()) menuKeepBook.setChecked(true);
		if(PDICMainAppOptions.fyePaused()) menuPaused.setChecked(true);
		
        //a.showT((int) (spsubs*dm.widthPixels)+"~"+spsubs);
		if (etTools==null) {
			gridView.setColumnWidth((int) (lvHeaderItem_length * density));
			vBox.setPrimaryContentSize(HeadlineInitialSize = (int) ((lvHeaderItem_height+5) * density));
			hBox.setPrimaryContentSize((int) (spsubs*dm.widthPixels));
			
			if(Build.VERSION.SDK_INT >= 24)
				if(true) {//opt.is_strict_scroll()
					ViewUtils.listViewStrictScroll(true, lv1, lv2);
				}
			
			etTools = new SearchbarTools(a, etSearch, null, (ViewGroup) PeruseTorso.getParent(), true);
			etTools.initWay = this;
			etTools.schSql = "src&"+SearchUI.Fye.MAIN+"!=0";
		}
		
        if(bCallViewAOA) {
        	onViewAttached(a,true);
        	bCallViewAOA=false;
        }
		
		hBox.setPrimaryContentSize((int) (a.root.getWidth()*0.35f),true);
	}
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
	}
	
	
	// 翻阅设定
	public void showPeruseTweaker() {
		MainActivityUIBase a = getMainActivity();
		final SettingsPanel settings = new SettingsPanel(a, opt
				, new String[][]{new String[]{null, "启用页面点击翻译", "记忆页面位置", "同一词典使用相同的页面缩放", "允许滑动翻页", "使用页面双击缩放"}}
				, new int[][]{new int[]{Integer.MAX_VALUE
				, makeDynInt(100, 0, weblistHandler.tapSch)
				, makeInt(6, 25, true) // fyeRemPos
				, makeInt(6, 26, false) // fyeRemScale
				, makeInt(6, 37, true) // turnPageFye
				, makeInt(6, 35, false) // tapZoomFye
		}}, null);
		settings.init(a, root);
		settings.setActionListener(new SettingsPanel.ActionListener() {
			@Override
			public boolean onAction(SettingsPanel settingsPanel, int flagIdx, int flagPos, boolean dynamic, boolean val, int storageInt) {
				if (dynamic) {
					if (weblistHandler.tapSch != val) {
						PDICMainAppOptions.fyeTapSch(weblistHandler.togTapSch());
					}
				} else {
					if (flagPos==37||flagPos==35) {
						SearchUI.tapZoomV++;
					}
				}
				return true;
			}
			@Override
			public void onPickingDelegate(SettingsPanel settingsPanel, int flagIdx, int flagPos, int lastX, int lastY) {
			
			}
		});
		
		AlertDialog dlg =
				new AlertDialog.Builder(a,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line)
						.setTitle("翻阅设定")
						.setView(settings.settingsLayout)
						.setPositiveButton(R.string.confirm, null)
						.create();
		
		dlg.show();
		
		dlg.getWindow().setDimAmount(0);
	}
	
	public void showPeruseTweaker1() {
		MainActivityUIBase a = getMainActivity();
		String[] DictOpt = getResources().getStringArray(R.array.peruse_spec);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		TextView tv = a.buildStandardConfigDialog(a, true, null, 0, "翻阅设定");
		Dialog configurableDialog = (Dialog) tv.getTag();
		
		if(GlobalOptions.isLarge) tv.setTextSize(tv.getTextSize());

		tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 1, 0x1, 20, 1, 1, -1, true);//opt.getPeruseAddAll()//添加全部


		init_clickspan_with_bits_at(tv, ssb, DictOpt, 7, Coef, 0, 0, 0x1, 19, 1, 1, 25, true);//opt.getInDarkMode()//
		ssb.delete(ssb.length()-4,ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 8, Coef, 0, 0, 0x1, 55, 1, 3, 26, true);//opt.getPeruseTextSelectable()//

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1, 8, 1, 1, 20, true);//opt.getShowBA()//添加书签
		ssb.delete(ssb.length()-4,ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 3, Coef, 0, 0, 0x1, 9, 1, 1, 21, true);//opt.getShowBD()//

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 9, Coef, 0, 0, 0x1, 8, 1, 1, 20, true);//opt.getShowBA()//记忆页面位置
		ssb.delete(ssb.length()-4,ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 10, Coef, 0, 0, 0x1, 9, 1, 1, 21, true);//opt.getShowBD()//允许滑动翻页

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 4, Coef, 0, 0, 0x1, 11, 1, 1, 22, true);//opt.getForceSearch()//
		ssb.delete(ssb.length()-4,ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 5, Coef, 0, 0, 0x1, 10, 1, 1, 23, true);//opt.getShowFScroll()//

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 11, Coef, 0, 0, 0x1, 10, 1, 1, 23, true);//opt.getShowFScroll()//始终以搜索框内容为搜索词
		ssb.delete(ssb.length()-4,ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 6, Coef, 0, 0, 0x1, 54, 1, 3, 24, true);//opt.schPageFye()//


		ssb.delete(ssb.length()-4,ssb.length());
		
		opt.setAsLinkedTextView(tv, true);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);

//		tv.setTextSize(17f);
//		tv.setText(ssb);
//		tv.setMovementMethod(LinkMovementMethod.getInstance());
		configurableDialog.setOnDismissListener(dialog -> a.checkFlags());
		configurableDialog.getWindow().setDimAmount(0);
		configurableDialog.show();
		android.view.WindowManager.LayoutParams lp = configurableDialog.getWindow().getAttributes();  //获取对话框当前的参数值
		lp.height = -2;
		configurableDialog.getWindow().setAttributes(lp);
	}
	
	public boolean isContentViewAttached() {
		if(contentview!=null && contentview.getVisibility()==View.VISIBLE && contentview.getParent()!=null) {
			if(weblistHandler.bShowingInPopup && !weblistHandler.isPopupShowing()) {
				return false;
			}
			return true;
		}
		return false;
	}

	public void initViews(MainActivityUIBase a) {
		//CMN.Log("inflateContentView");
		contentUIData = ContentviewBinding.inflate(a.getLayoutInflater(), root,false);
		weblistHandler = new WebViewListHandler(a, contentUIData, SearchUI.Fye.MAIN);
		lv1.setAdapter(ActivedAdapter = entryAdapter = new LeftViewAdapter());
		lv2.setAdapter(bmsAdapter = new RightViewAdapter());
		
		AllMenus.checkDrawable = a.AllMenus.checkDrawable;
		AllMenus.mOverlapAnchor = PDICMainAppOptions.menuOverlapAnchor();
		AllMenus.tag = weblistHandler;
		contentview = contentUIData.webcontentlister;
		weblistHandler.setUpContentView(1);
		
		boolean mergeBtm = PDICMainAppOptions.getMergePeruseBottombars();
		if(mergeBtm) {
			AllMenus.findItem(R.id.mergeTools).setChecked(mergeBtm);
			resetBottomBar();
		}
		
//		String contentkey = "ctnp#"+1;
//		String appproject = opt.getAppContentBarProject(contentkey);
//		if(appproject!=null) {
//			AppUIProject content_project = a.peruseview_project;
//			if(content_project==null){
//				content_project = new AppUIProject(contentkey, ContentbarBtnIcons, appproject, bottombar2, weblistHandler.ContentbarBtns);
//				content_project.type = 1;
//				a.peruseview_project = content_project;
//			} else {
//				content_project.bottombar = bottombar2;
//				content_project.btns = weblistHandler.ContentbarBtns;
//			}
//			RebuildBottombarIcons(a, content_project, a.mConfiguration);
//		}

//		SplitView webcontentlist = contentUIData.webcontentlister;
//		LinearLayout bottombar2 = contentUIData.bottombar2;
		
		
		//webholder = WHP.findViewById(R.id.webholder);
		
//		(widget13=PageSlider.findViewById(R.id.browser_widget13)).setOnClickListener(this);
//		(widget14=PageSlider.findViewById(R.id.browser_widget14)).setOnClickListener(this);

//		contentUIData.WHP.setVisibility(View.GONE);
		
		((MarginLayoutParams)contentUIData.dragScrollBar.getLayoutParams()).leftMargin+= hBox.getCompensationBottom()/2;
//		webcontentlist.scrollbar2guard=contentUIData.dragScrollBar;
		
		entryAdapter.webviewHolder =
		bmsAdapter.webviewHolder = contentUIData.webSingleholder;
		contentUIData.webSingleholder.setBackgroundColor(CMN.GlobalPageBackground);
		
			mWebView = weblistHandler.getMergedFrame();
			mWebView.setMinimumWidth((int) (100*GlobalOptions.density));
			mWebView.weblistHandler = weblistHandler;
			//mWebView.fromPeruseview = true;
			mWebView.fromCombined=3;
	        mWebView.setOnScrollChangedListener(a.getWebScrollChanged());
	        mWebView.setPadding(0, 0, 18, 0);
			contentUIData.dragScrollBar.setDelimiter("< >", mWebView);
    		mWebView.getSettings().setSupportZoom(true);
			perusehandler = new BookPresenter.AppHandler(a.currentDictionary);
			mWebView.addJavascriptInterface(perusehandler, "app");
			ViewUtils.setOnClickListenersOneDepth(weblistHandler.mMergedBook.toolbar, this, 999, null);
		
			entryAdapter.mWebView = bmsAdapter.mWebView = mWebView;
		
//	        toolbar_web= pageView.lltoolbar;
//			toolbar_cover = pageView.cover;
//			toolbar_cover.setTag(2);

//			toolbar_web.getBackground().mutate();
//			mWebView.FindBGInTitle(toolbar_web);
//			mWebView.toolbarBG.setColors(mWebView.ColorShade);

//			recess = pageView.recess;
//			forward = pageView.forward;
//			BookPresenter.setWebLongClickListener(mWebView, a);
		
		if(ToL||ToR) {
			contentUIData.bottombar2.setBackgroundColor(bottombar2BaseColor);
		} else {
			contentUIData.bottombar2.setBackgroundColor(GlobalOptions.isDark?ColorUtils.blendARGB(a.MainBackground,Color.BLACK,a.ColorMultiplier_Wiget):a.MainBackground);
		}
		mlp.removeView(contentview);
		
//		boolean tint = PDICMainAppOptions.getTintIconForeground();
//		for (int i = 0; i < 6; i++) {
//			ImageView iv = (ImageView) contentUIData.bottombar2.getChildAt(i);
//			weblistHandler.ContentbarBtns[i]=iv;
//			iv.setOnClickListener(this);
//			if(tint) iv.setColorFilter(a.ForegroundTint, PorterDuff.Mode.SRC_IN);
//			iv.setOnLongClickListener(this);
//		}

//		contentUIData.browserWidget8.setOnClickListener(this);
//		contentUIData.browserWidget8.setOnLongClickListener(this);
		
		contentUIData.PageSlider.page = contentUIData.cover;
		contentUIData.PageSlider.slideTurn = opt.slidePage1D();
//		if(a.IMPageCover!=null)//111
//			IMPageCover.setPageSliderInf(a.IMPageCover.inf);

//		webcontentlist.setPageSliderInf(a.inf);
//		webSingleholder.addView(rl);
		ViewUtils.addViewToParent(mWebView.rl, contentUIData.webSingleholder);
		
//		rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
//		mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

		if(opt.schPageFye()){
			toggleSearchPage();
		}
	}
	
	private void resetBottomBar() {
		boolean merge = PDICMainAppOptions.getMergePeruseBottombars();
		ViewUtils.addViewToParent(contentUIData.bottombar2, weblistHandler.isPopupShowing()||!merge?contentUIData.webcontentlister:bottom);
		ViewUtils.setVisible(bottombar, !merge || contentview.getParent()!=slp);
		ViewUtils.setVisible(contentUIData.bottombar2, !merge || contentview.getParent()==slp || weblistHandler.isPopupShowing());
	}
	
	final void toggleSearchPage() {
		weblistHandler.togSchPage();
	}
	
	public void prepareProgressBar(View progressBar) {
		ViewUtils.addViewToParent(progressBar, contentview, 0);
	}
	
	public void refreshUIColors(int MainBackground) {
		MainActivityUIBase a = getMainActivity();
		boolean isDark = GlobalOptions.isDark;

		int filteredColor = isDark?Color.BLACK:MainBackground; //0xff8f8f8f

		if(PerusePageSearchbar!=null)
			PerusePageSearchbar.setBackgroundColor(filteredColor);
		
		if(mDialog!=null)
			Toastable_Activity.setStatusBarColor(mDialog.getWindow(), filteredColor);
		
		int f_b = ColorUtils.blendARGB(filteredColor, Color.WHITE, 0.20f);
		
		handle1.getBackground().setColorFilter(f_b, PorterDuff.Mode.LIGHTEN);
		
		handle2.getBackground().setColorFilter(f_b, PorterDuff.Mode.LIGHTEN);

		mWebView.evaluateJavascript(isDark? MainActivityUIBase.DarkModeIncantation: MainActivityUIBase.DeDarkModeIncantation, null);
		main_pview_layout.setBackgroundColor(filteredColor);
		bottombar.setBackgroundColor(filteredColor);
		contentUIData.bottombar2.setBackgroundColor(bottombar2BaseColor = filteredColor);
		contentUIData.webSingleholder.setBackgroundColor(isDark?Color.BLACK:CMN.GlobalPageBackground);
	}

	float spsubs;

	int NumPreEmpter=0;
	
	/** 上边的词典封面网格适配器 + tadaViewRn */
    GridAdapter gridAdapter = new GridAdapter();
	
	public void prepareJump(MainActivityUIBase a, String key, ArrayList<Long> _data, long _bookId) {
		CMN.Log("fye::prepareJump::", key);
		if(key!=null) {
			syncData(a);
			long bid = bookId = (keepBook()&&currentDictionary!=BookEmpty?currentDictionary:a.currentDictionary).getId();
			schKey = key.trim();
			if ((!keepGroup() || bookIds.size()==0)) {
				if(_data==null) {
					searchAll(schKey, a, false);
				} else {
					bookIds=_data;
					/* 边界检查 */
					fromLv1Idx = -1;
					for (int i=bookIds.size()-1; i>=0; i--) {
						long id = bookIds.get(i);
						if(id<0)
							bookIds.remove(i);
						if(id==bid) {
							fromLv1Idx = i;
						}
					}
					if(fromLv1Idx==-1) {
						if(keepBook() && currentDictionary!=BookEmpty) {
							fromLv1Idx = bookIds.size();
							bookIds.add(bid);
						} else {
							fromLv1Idx = 0;
						}
					}
					fromLv1 = true;
					fromData = true;
				}
			}
		}
	}

	private void syncData(MainActivityUIBase a) {
		md = a.md;
		ph = a.getPlaceHolders();
		if(opt==null)
			opt = a.opt;
		if(currentDictionary==null)
			currentDictionary = BookEmpty = a.EmptyBook;
	}

	public void prepareInPageSearch(String key, boolean bNeedBringUp) {
		if(PerusePageSearchetSearch==null){
			PerusePageSearchetSearchStartWord=key;
		}else{
			PerusePageSearchetSearch.setText(key);
			bNeedBringUp=bNeedBringUp&&PerusePageSearchbar.getParent()==null;
		}
		if(bNeedBringUp){
			toggleSearchPage();
		}
	}
	
	public boolean toggleTurnPageEnabled() {
		opt.slidePageMd(!opt.slidePageMd());
		return contentUIData.PageSlider.slideTurn =opt.slidePageMd();
	}

	/** Hide is not dismiss */
	public void hide() {
		onViewDetached();
		mDialog.hide();
		//mDialog.dismiss();
		//dismiss();
		//CMN.Log("peruse showing...", mDialog.isShowing());
	}

	public void try_go_back(){
		MainActivityUIBase a = getMainActivity();
		if(contentview.getParent()!=null && a!=null){
			DetachContentView(a);
			return;
		}
		goBack();
	}

	public void goBack() {
		MainActivityUIBase a = getMainActivity();
		if(a != null) {
			if(a.settingsPanel!=null) {
				a.hideSettingsPanel(a.settingsPanel);
				return;
			}
			if(!a.AutoBrowsePaused || a.bRequestingAutoReading){
				a.stopAutoReadProcess();
				return;
			}
			if(mDialog!=null && mDialog.getCurrentFocus()==mWebView && mWebView.bIsActionMenuShown) {
				mWebView.clearFocus();
				return;
			}
			if(ViewUtils.removeIfParentBeOrNotBe(a.wordPopup.popupContentView, root, true)){
				a.wordPopup.popupContentView = null;
				a.wordPopup.popupGuarder.setVisibility(View.GONE);
				return;
			}
			if(DBrowser!=null){
				DBrowser.dismiss();
				DBrowser = null;
				return;
			}
		}
		if(ViewUtils.removeIfParentBeOrNotBe(contentview, main_pview_layout, true)){
			return;
		}
		hide();
	}

	//todo optimise
	private void DetachContentView(MainActivityUIBase a) {
		if(//!(currentDictionary instanceof bookPresenter_txt)&& nimp
				 PDICMainAppOptions.storeClick() && !PDICMainAppOptions.storeNothing()
				&& (PDICMainAppOptions.storePageTurn() == 2)) {
			a.addHistory(mWebView.word, SearchUI.Fye.表, contentUIData.webSingleholder, null);
		}
		((ViewGroup)contentview.getParent()).removeView(contentview);
	}

	public boolean isAttached() {
		if(mDialog!=null && mDialog.isShowing()){
			Window win = mDialog.getWindow();
			if(win!=null && root!=null)
				return win.getDecorView().getVisibility()==View.VISIBLE;
		}
		return false;
	}

	public float getWebTouchY() {
		return mWebView.lastY+mWebView.toolbar_title.getHeight()+ gridView.getHeight()+etSearch.getHeight();
	}

	public void dismissDialogOnly() {
		if(mDialog!=null && !isAttached()){
			mDialog.dismiss();
			mDialog.decorBright();
		}
	}

	/** 来自lv1列表点击(一次使用)。来自不带数据的 prepareJump。 */
	public void searchAll(String key, MainActivityUIBase a, boolean addCurrent) {
		if(key!=null) {
			if (addCurrent) {
				syncData(a);
				bookId = (keepBook()&&currentDictionary!=BookEmpty?currentDictionary:a.currentDictionary).getId();
				schKey = key.trim();
			}
			if ((!keepGroup() || bookIds.size()==0)) {
				lastSchKey = null;
				fromLv1 = addCurrent;
				fromData = showAll();
				fromLv1Idx = 0;
				if(showAll()) {
					clearBeforeSch();
					for (int i = 0; i < md.size(); i++)
						bookIds.add(a.getBookIdAt(i));
					if(keepBook()) {
						fromLv1Idx = bookIds.indexOf(currentDictionary.getId());
						if(fromLv1Idx==-1) {
							fromLv1Idx = bookIds.size();
							bookIds.add(currentDictionary.getId());
						}
					} else {
						fromLv1Idx = a.dictPicker.adapter_idx;
					}
				}
				else {
					bookIds.ensureCapacity(md.size());
					startSearch(a);
				}
			}
		}
	}
	
	private void clearBeforeSch() {
		bookIds.clear();
	}
	
	private void startSearch(MainActivityUIBase a) {
		a.wordPopup.startTask(WordPopupTask.TASK_FYE_SCH);
	}
	
	@AnyThread
	public void SearchAll(MainActivityUIBase a, AtomicBoolean task) {
		ArrayList<Long> schResult = new ArrayList<>(a.md.size());
		String schKey = this.schKey;
		String key = mdict.replaceReg.matcher(schKey).replaceAll("").toLowerCase();
		int index=0;
		long bookId = keepBook()?currentDictionary.getId():this.fromLv1?this.bookId:-1;
		try {
			for (int i = 0; i < md.size(); i++) {
				if(!task.get()) {
					break;
				}
				long bid = a.getBookIdAt(i);
				if (bid==bookId) {
					index=schResult.size();
					schResult.add(bid);
					continue;
				}
				BookPresenter book = a.md_get(i);
				if(book==BookEmpty)
					continue;
				int idx = book.bookImpl.lookUp(key);
				//CMN.Log(mdTmp.getEntryAt(idx), idx, text, mdTmp._Dictionary_fName);
				if (idx >= 0){
					if (book.getIsWebx()) {
						if(book.getWebx().takeWord(key)) {
							schResult.add(bid);
						}
						continue;
					}
					String toCompare = mdict.replaceReg.matcher(book.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase();
					int len = key.length();
					int len1 = len;
					int len2 = toCompare.length();
					//CMN.Log("cidx??",mdTmp._Dictionary_fName, toCompare);
					if(len>0 && len2>0/* && len>=toCompare.length()*/ && key.charAt(0)==toCompare.charAt(0)){
						if(len==1){
							schResult.add(bid);
						} else {
							len = Math.min(len, len2);
							int cidx = 1;
							for (; cidx < len; cidx++) {
								if (key.charAt(cidx) != toCompare.charAt(cidx))
									break;
							}
							cidx--;
							//CMN.Log("cidx", cidx, text, toCompare, mdTmp._Dictionary_fName);
							if (cidx > 0) {
								if (cidx>=len1/3 && (len - cidx <= 4 || cidx>=len2/2)) {
									schResult.add(bid);
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			CMN.Log(e);
		}
		this.schResult = schResult;
		lastSchKey = schKey;
		//CMN.Log("fye::SearchAll::", schKey, schResult);
		//harvest
		fromLv1Idx = fromLv1Idx_ = index;
		a.hdl.post(new Runnable() {
			@Override
			public void run() {
				clearBeforeSch();
				bookIds.addAll(schResult);
				if (gridView!=null) {
					resetGrid(a); // harvest
				} else {
					a.hdl.postDelayed(this, 100);
				}
			}
		});
	}
	
	//for top list
    public class GridAdapter extends BaseAdapter implements OnItemClickListener, Runnable
    {
        public boolean flip;
        @Override
        public int getCount() {
        	if(bookIds.size()>0 && recyclerBin.size()>=bookIds.size())
        		return bookIds.size()+NumPreEmpter;
        	return 0;
        }
        @Override
        public View getItem(int position) {
			return null;
		}
        @Override
        public long getItemId(int position) {
          return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	if(position<NumPreEmpter) {
        		return cyclerBin.get(position);
        	}
        	position-=NumPreEmpter;
        	View ItemView = recyclerBin.get(position);
			ArrayList<Long> bookIds = PeruseView.this.bookIds;
			if (position>=0 && position< bookIds.size()) {
				DictTitleHolder holder = (DictTitleHolder) ItemView.getTag();
				MainActivityUIBase a = (MainActivityUIBase) getActivity();
				if (a !=null) {
					BookPresenter presenter = a.getBookByIdNoCreation(bookIds.get(position));
					Drawable cover=null;
					String pathname;
					if(presenter!=BookEmpty) {
						pathname=presenter.getDictionaryName();
						cover=presenter.cover;
					} else {
						pathname=a.getBookNameByIdNoCreation(bookIds.get(position));
					}
					holder.tv.setText(pathname);
					holder.cover.setImageDrawable(cover);
					holder.word.setText(pathname.substring(0,1).toUpperCase());
				}
			}
			else if(BuildConfig.DEBUG){
				((MainActivityUIBase)getActivity()).showT("越界!"+position+"/"+ bookIds.size());
			}
	        return ItemView;
        }
		@Override
		public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
			//CMN.Log("onItemClick::dict::", position, id);
        	MainActivityUIBase a = getMainActivity();
        	if(a==null) return;
			int pos = position-NumPreEmpter;
			
			if(selection!=null) {
				((LayerDrawable) selection.getBackground()).getDrawable(0).setAlpha(0);
		
				if(view!=null && lv1.getChildCount()>0 && System.currentTimeMillis()-lastswicthtime>200) {//record our position
					voyager[SelectedV*VELESIZE] = lv1.getFirstVisiblePosition();
					voyager[SelectedV*VELESIZE+1] = lv1.getChildAt(0).getTop();
					if(entryAdapter.lastClickedPos!=-1)
						voyager[SelectedV*VELESIZE+2] = entryAdapter.lastClickedPos;
					//record page position
					ActivedAdapter.lastClickedPosBefore = ActivedAdapter.lastClickedPos;
					ActivedAdapter.SaveVOA();
			
					lastswicthtime=System.currentTimeMillis();
				}
			}
			
			SelectedV=pos;
			PositionToCenter = pos;
			if(view==null)
				view = recyclerBin.get(pos);
			selection = view;
			((LayerDrawable) view.getBackground()).getDrawable(0).setAlpha(255);
			
			TargetRow = position/cc;
			PositionToSelect = TargetRow*cc;
			if(TargetRow>=1) {
				PositionToSelect-=NumPreEmpter;
			}
			if(!bExpanded && PDICMainAppOptions.fyeDictAutoScroll()) {
				scrollGridToCenter(position);
			}
			//a.showT(cc+"should collapse at: "+PositionToSelect);

			BookPresenter last = currentDictionary;
			bookId = bookIds.get(pos);
			currentDictionary = a.getBookById(bookId);
			
			perusehandler.setBook(currentDictionary);
			
			if (entryAdapter.presenter!=currentDictionary) {
				entryAdapter.setPresenter(currentDictionary);
				entryAdapter.DumpVOA(last, currentDictionary);
				entryAdapter.notifyDataSetChanged();
			}
			//notifyDataSetChanged();
			//LvHeadline.setLayoutParams(LvHeadline.getLayoutParams());
			
			int stPos=pos*VELESIZE;
			/* 初始化 | 自动搜索 */
			if((!paused() || voyager[stPos]<0)
					&& currentDictionary!=BookEmpty
					&& !TextUtils.isEmpty(schKey)
			) {
				voyager[stPos] = currentDictionary.bookImpl.lookUp(schKey,false);
				voyager[stPos+2] = voyager[stPos];
				voyager[stPos+1]=(int) (20*density);
			}

			//a.showT(voyager[pos*3] + "to "+currentDictionary.getEntryAt(voyager[pos*3]));
			
			if(id!=-2 && voyager[stPos]!=-1) {
				// lv1.setSelectionFromTop(0, (int) (20*density));
				// lv1.setSelectionFromTop(voyager[pos*3], voyager[pos*3+1]);
				lv1.setSelection(voyager[stPos]);
				lv1.setSelectionFromTop(voyager[stPos], voyager[stPos+1]);
				//if(lv1.getFirstVisiblePosition()!=voyager[pos*3])//make it right
				if(flip) {
					lv1.post(() -> {
						// lv1.setSelectionFromTop(voyager[pos*3], voyager[pos*3+1]);
						// lv1.setSelection(voyager[pos*3]);
						lv1.setSelectionFromTop(voyager[stPos], voyager[stPos+1]);
						flip=false;
					});
				}
				if(voyager[stPos+2]>=0) {
					if(ToR && cvpolicy && contentview.getVisibility()==View.VISIBLE) {
						//if(mdict.processText(TextToSearch).equals(mdict.processText(currentDictionary.getEntryAt(voyager[pos*3]))))
						entryAdapter.click(voyager[stPos+2],false, false);
					} else {
						entryAdapter.lastClickedPos=voyager[stPos+2];
					}
				} else {
					entryAdapter.lastClickedPos = -1;
				}
			}

			if (id>=0) {
				a.showTopSnack(PeruseTorso, currentDictionary.bookImpl.getDictionaryName()
						, 0.8f, -1, -1, 1);
			}

			ViewUtils.removeIfParentBeOrNotBe(contentview, mlp, true);
        	
        	if(ToD) {
        		if(last!=null) {
        			last.bmCBI=lv2.getFirstVisiblePosition();
        			last.bmCCI= bmsAdapter.lastClickedPos;
        		}
        		pullBookMarks();
        	}
			
			gridView.postInvalidateOnAnimation(); // invalidate
        	
			//oldV=view;
			//a.showT(NumPreEmpter+"-"+(position-NumPreEmpter)+"="+currentDictionary._Dictionary_fName);
		}
		
		// tadaViewRn
		@Override
		public void run() {
			View v = gridView.getChildAt(PositionToCenter-gridView.getFirstVisiblePosition());
			if(v!=null) {
				v.animate()
					.scaleX(1.5f)
					.scaleY(1.5f)
					.setDuration(100)
					.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							v.animate()
								.scaleX(1f)
								.scaleY(1f)
								.setDuration(200)
								.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR)
								.setListener(null);
						}
					})
				;
			}
		}
    }
	
	private void scrollListToCenter(int pos) {
		lv1.setSelection(pos-Math.min(lv1.getChildCount()/2, 2));
	}
	
	private void scrollGridToCenterNaive(int pos) {
		// todo setSelectionWithOffset
		gridView.setSelection(pos-cc/2);
		gridView.postDelayed(() -> scrollGridToCenter(pos), 100);
	}
	
	private void scrollGridToCenter(int position) {
		TwoWayGridView gv = gridView;
		View child = gv.getChildAt(0);
		if (!bExpanded) {
			if (child!=null) {
				final int fvp = gv.getFirstVisiblePosition();//当前行起始位置
				final int dist=position*itemWidth-Math.max(0, (root.getWidth()-itemWidth)/2)-(fvp*itemWidth-child.getLeft());
				//CMN.Log("scrollGridToCenter::", position, fvp, dist);
				gv.smoothScrollBy(dist ,280);
			} else {
				gv.smoothScrollToPosition(position);
			}
		} else {
			gv.smoothScrollToPosition(position);
		}
	}
	
	//for right view
    public class RightViewAdapter extends BasicAdapter {
    	int lastClickedDictPos=-1;
	
		public RightViewAdapter() {
			super(PeruseView.this.contentUIData, PeruseView.this.weblistHandler, null, null);
		}
	
		@Override
		public int getCount() {
			return ToD?cr.getCount():hidden.size();
		}

		@Override
		public View getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		
		@Override
		public String getEntry(int pos) {
			return "NoImpl!!!";
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			viewholder vh;
			if(convertView==null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, parent, false);
				vh = new viewholder();
        		vh.tv=convertView.findViewById(R.id.text1);
        		vh.dv=convertView.findViewById(R.id.del);
        		vh.tv.setTextColor(Color.WHITE);
    			vh.tv.setPadding((int) (16*density), 0, 0, 0);
        		vh.dv.setId(R.id.deld);
        		int p = (int) (10*density);
        		vh.dv.setPadding(p, p, p, p);
        		vh.dv.setColorFilter(Color.RED);
				convertView.setTag(vh);
			}else {
				vh = (viewholder) convertView.getTag();
			}
    		vh.dv.setTag(position);
			if(ToD) {
				cr.moveToPosition(cr.getCount()-position-1);
				vh.tv.setText(currentDictionary.bookImpl.getEntryAt(cr.getInt(0)));//bookmarks.get(position)
				vh.tv.setSingleLine(false);
			} else {
				MainActivityUIBase a = (MainActivityUIBase) getActivity();
				if (a!=null) {
					vh.tv.setText(a.getBookNameByIdNoCreation(hidden.get(position)));
					vh.tv.setSingleLine();
				}
			}
			
			if(position==(ToD?lastClickedPos:lastClickedDictPos)) {//voyager[SelectedV*3+2]
        		//which color?
        		convertView.setBackgroundColor(0xff397CCD);//LB0xff397CCD  HB0xff2b4381
        	} else {
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			
			if(ToD && opt.getShowBD()) {
				vh.dv.setVisibility(View.VISIBLE);
			}else {
				vh.dv.setVisibility(View.GONE);
			}
			return convertView;
		}
		
        @Override
		public void onItemClick(int pos) {
        	click(pos,true);
        }
        
		public void click(int pos,boolean ismachineClick) {//lv2
			MainActivityUIBase a = getMainActivity();
			ActivedAdapter=this;
			WebViewmy mWebView = PeruseView.this.mWebView;
			
        	if(ToD) {
        		//a.setContentBow(false);
        		//super.onItemClick(pos);
            	if(pos<0) {
					a.showTopSnack(a.mainframe, R.string.endendr, -1, -1, -1, 0);
            		return;
            	}
            	//-1放行
            	if(pos>=getCount()) {
            		lastClickedPos = getCount()-1;
            		a.show(R.string.endendr);
            		return;
        		}
            	
            	int f = lv2.getFirstVisiblePosition();
    			int c = lv2.getChildCount();
    			int o=lastClickedPos-f;
    			if(o>=0 && o<c) {
    				lv2.getChildAt(o).setBackgroundColor(Color.TRANSPARENT);
    			}
    			o=pos-f;
    			//boolean proceed=true;
    			if(ismachineClick)
    			if(o==c-1) {
    				int delta = lv2.getHeight() -lv2.getChildAt(o).getTop();
    				float judger = lv2.getChildAt(o).getHeight();
    				if(delta <= judger*2/3) {
    					if(delta<judger/2)
    						lv2.setSelection(pos);
    					else
    						lv2.scrollListBy((int) (judger-(lv2.getHeight()-lv2.getChildAt(o).getTop())));

    					//proceed=false;
    				}
    			}
    			//if(proceed) {
    				if(o>=0 && o<c) {
    					lv2.getChildAt(o).setBackgroundColor(0xff397CCD);
    				}else if(ismachineClick){
    					lv2.setSelection(pos);
    				}
    			//}
    			
    			
            	//doing: adaptively add and remove!
		
				if(ToL && PDICMainAppOptions.fyeTogEntry()
						&& lastClickedPos==pos
						&& contentview.getParent()==mlp) {
					toggleContentVis();
					return;
				}
		
				contentview.setVisibility(View.VISIBLE);
				contentUIData.webSingleholder.setVisibility(View.VISIBLE);
				contentUIData.WHP.setVisibility(View.GONE);
		
		
				SplitView webcontentlist = contentUIData.webcontentlister;
				if(ToL) {
					ViewUtils.addViewToParent(contentview, mlp);
            	} else {
//            		if(a.opt.getBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
//                    	webcontentlist.SwitchingSides();
//					webcontentlist.setPrimaryContentSizeUnChanged(a.CachedBBSize,true);
//            		ViewUtils.addViewToParent(contentview, main_pview_layout);
            	}
    			
            	lastClickedPos = pos;

				cr.moveToPosition(cr.getCount()-lastClickedPos-1);
            	int actualPosition=cr.getInt(0);
		
				mWebView.clearIfNewADA(currentDictionary);

				setCurrentDis(currentDictionary, actualPosition);

				if(a.opt.getAutoReadEntry() && !PDICMainAppOptions.getTmpIsAudior(currentDictionary.tmpIsFlag)){
					mWebView.bRequestedSoundPlayback=true;
				}
		
				currentKeyText = mWebView.word;
    			currentDictionary.renderContentAt(-1, RENDERFLAG_NEW, 0, mWebView, actualPosition);//bookmarks.get(lastClickedPos)
				
    			//voyager[SelectedV*3+2]=pos;
				mWebView.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        	}
			else {
//        		ViewGroup p = (ViewGroup) vb.getParent();
//    			if(p!=null) {
//    				currentDictionary = a.EmptyBook;
//    				voyager[SelectedV*VELESIZE] = lv1.getFirstVisiblePosition();
//    				voyager[SelectedV*VELESIZE+1] = lv1.getChildAt(0).getTop();
//    				if(leftAdapter.lastClickedPos!=-1)
//    					voyager[SelectedV*VELESIZE+2] = leftAdapter.lastClickedPos;
//    				p.removeView(vb);
//    			}
				int f = lv2.getFirstVisiblePosition();
    			int c = lv2.getChildCount();
    			int o=lastClickedDictPos-f;
    			if(o>=0 && o<c) {
    				lv2.getChildAt(o).setBackgroundColor(Color.TRANSPARENT);
    			}
    			o=pos-f;
				if(o>=0 && o<c) {
					lv2.getChildAt(o).setBackgroundColor(0xff397CCD);
				}
				bookId = hidden.get(lastClickedDictPos=pos);
    			currentDictionary = a.getBookById(bookId);
				perusehandler.setBook(currentDictionary);
				entryAdapter.notifyDataSetChanged();
        	}
			
			mWebView.pBc = currentDictionary.IBC;
			contentUIData.PageSlider.setWebview(mWebView, null);
        }

		public void onClick(View v) {
			if (v.getId() == R.id.deld) {
				int id = (int) v.getTag();
				MdxDBHelper con = currentDictionary.getCon(true);
				cr.moveToPosition(cr.getCount() - id - 1);
				if (con.remove(cr.getInt(0)) > 0) {
					//a.showX(R.string.delDone,0);
					pullBookMarks();
				} else {
					getMainActivity().showT("删除失败,数据库出错...", 0);
				}
			}
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			click(position,false);
		}

		@Override
		public int getId() {
			return 6;
		}

		@Override
		public String currentKeyText() {
			return currentKeyText;
		}
	}
	
	private final class viewholder{
    	TextView tv;
    	ImageView dv;
    }
    
    //for left view
    public class LeftViewAdapter extends BasicAdapter
						implements OnClickListener{
		//AbsListView.LayoutParams lp;
        int lastClickedPos;
        //构造
        public LeftViewAdapter() 
        {  
    		//lp = new AbsListView.LayoutParams(-1,-1);
    		//lp.setMargins(280, 0, 0, 0);  
			super(PeruseView.this.contentUIData, PeruseView.this.weblistHandler, null, null);
		}
        
        @Override
        public int getCount() {
        	if(presenter!=null)
        		return (int) currentDictionary.bookImpl.getNumberEntries();
        	else
        		return 0;
        }
	
		@Override
		public String getEntry(int pos) {
			return currentDictionary.getBookEntryAt(pos);
		}
	
		@Override
        public View getView(final int position, View convertView, ViewGroup parent) {//length=1046; index=5173
        	String currentKeyText = currentDictionary.getBookEntryAt(position);
        	viewholder vh;
	        if(convertView==null){
        		convertView =  LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item2, parent, false);
        		convertView.setId(R.id.lvitems);
        		vh = new viewholder();
        		vh.tv=convertView.findViewById(R.id.text);
        		vh.dv=convertView.findViewById(R.id.del);
        		vh.dv.setOnClickListener(this);
                convertView.setTag(vh);
        	} else {
				vh = (viewholder) convertView.getTag();
			}
	        vh.tv.setText(currentKeyText);

	        vh.tv.setSingleLine(!LnW);

	        if(opt.getPeruseTextSelectable()) {
				vh.tv.setTextIsSelectable(true);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
					vh.tv.setFocusable(FOCUSABLE_AUTO);
			} else {
				vh.tv.setTextIsSelectable(false);
			}

    		vh.dv.setTag(position);
        	convertView.setTag(R.id.position,position);
        	if(position==lastClickedPos) {//voyager[SelectedV*3+2]
        		//which color?
        		convertView.setBackgroundColor(0xff397CCD);//LB0xff397CCD  HB0xff2b4381
        	} else {
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			
			
        	MarginLayoutParams lp = (MarginLayoutParams) vh.tv.getLayoutParams();
        	if(opt.getShowBA()) {
        		vh.dv.setVisibility(View.VISIBLE);
				//lp.setMargins(0, (int)(5*density), 0, (int)(2*density));
				convertView.setPadding(0, (int)(5*density), 0, (int)(2*density));
        		//vh.tv.setPadding(0, (int)(5*density), 0, (int)(2*density));
        	}else {
        		vh.dv.setVisibility(View.GONE);
				convertView.setPadding((int)(8*density), (int)(5*density), 0, (int)(2*density));
				//lp.setMargins((int)(15*density), (int)(5*density), 0, (int)(2*density));
        		//vh.tv.setPadding((int)(15*density), (int)(5*density), 0, (int)(2*density));
        	}
			vh.tv.getLayoutParams().width=-1;

	        return convertView;
        }

		@Override
		public void SaveVOA() {
			WebViewmy mWebView = PeruseView.this.mWebView;
			if(!mWebView.isloading && lastClickedPosBefore>=0 && contentUIData.webSingleholder.getChildCount()!=0) {
				if(mWebView.webScale==0) mWebView.webScale=dm.density;//sanity check
				//avoyager.get(avoyagerIdx).set((int) (mWebView.getScrollX()), (int) (mWebView.getScrollY()), webScale);
				ScrollerRecord pagerec = avoyager.get(lastClickedPosBefore);
				if(pagerec==null) {
					pagerec=new ScrollerRecord();
					avoyager.put(lastClickedPosBefore, pagerec);
				}
				pagerec.set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
			}
		}

		@Override
		public void onItemClick(int pos) {//lv1
        	click(pos,true,true);
        }
	
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			click(position,false,true);
			cvpolicy=true;
		}
		
        private void click(int pos,boolean machine,boolean addHistory) {
			//CMN.Log("onItemClick::entryAdapter::", pos, machine, presenter);
			WebViewmy mWebView = PeruseView.this.mWebView;
			int fvp = lv1.getFirstVisiblePosition();
			View child = lv1.getChildAt(lastClickedPos-fvp);
			if(child!=null) child.setBackgroundColor(Color.TRANSPARENT);
			//CMN.Log("lastClickedPos::", lastClickedPos, child);
			child = lv1.getChildAt(pos-fvp);
			if(child !=null) child.setBackgroundColor(0xff397CCD);
			MainActivityUIBase a = getMainActivity();
			if (mWebView.presenter!=currentDictionary || mWebView.currentPos!=pos)
			{
				ActivedAdapter=this;
				lastClickedPosBefore = lastClickedPos;
				super.onItemClick(pos);
				if(pos<-1 || presenter==null)
					return;
				//-1放行
				if(pos>=getCount()) {
					lastClickedPos = getCount()-1;
					a.show(R.string.endendr);
					return;
				}
				
				contentUIData.PageSlider.setWebview(mWebView, null);
				weblistHandler.setViewMode(null, 0, mWebView);
				weblistHandler.viewContent();
				
				if(machine) {
					scrollListToCenter(pos);
				}
				
				float desiredScale=a.prepareSingleWebviewForAda(presenter, mWebView, pos, this, PDICMainAppOptions.fyeRemPos(), PDICMainAppOptions.fyeRemScale());
				
				//doing: adaptively add and remove!
				if(ToR && PDICMainAppOptions.fyeTogEntry()
						&& lastClickedPos==pos
						&& contentview.getParent()==slp) {
					toggleContentVis();
					return;
				}
				
				weblistHandler.initMergedFrame(1, !ToR, false);
				
				if(ToR) {
					ViewUtils.addViewToParent(contentview, slp);
				} else {
					weblistHandler.popupContentView(null, currentKeyText);
				}
				mWebView.clearIfNewADA(presenter);
				
				setCurrentDis(presenter, pos);
				if (pos==0 && presenter.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
					presenter.SetSearchKey(etSearch.getText().toString());
				}
				
				if(a.opt.getAutoReadEntry()
						&& !PDICMainAppOptions.getTmpIsAudior(presenter.tmpIsFlag)){
					mWebView.bRequestedSoundPlayback=true;
				}
				
				presenter.renderContentAt(desiredScale,RENDERFLAG_NEW,0, mWebView, pos);
				String key = currentKeyText = mWebView.word;
				
				contentUIData.PageSlider.setWebview(mWebView, null);
				//voyager[SelectedV*3+2]=pos;
				
				if(addHistory && !TextUtils.equals(key, lstKey)) {
					int stLv = !machine && storeLv1(key)? SearchUI.Fye.MAIN:
							(!machine || PDICMainAppOptions.storePageTurn()==0)?SearchUI.Fye.表
									:-1;
					if((stLv==SearchUI.Fye.MAIN || PDICMainAppOptions.storeClick()) && presenter.store(pos)) {
						//CMN.Log("fye:addHistory!!!");
						a.addHistory(key , stLv, contentUIData.webSingleholder, stLv==SearchUI.Fye.MAIN?etTools:null);
					}
				}
				
			}
	
			lastClickedPos = pos;
	
			resetBottomBar();
	
			SimpleTextNotifier tn = a.getTopSnackView();
			if(!(tn.isVisible() && tn.getText().equals(currentKeyText))) {
				tn.setNextOffsetScale(0.24f);
				a.showTopSnack(PeruseTorso, currentKeyText
						, 0.85f, LONG_DURATION_MS, -1, 0);
			}
		}
        
		@Override
		public void onClick(View v) {
			MainActivityUIBase a = getMainActivity();
			if (v.getId() == R.id.del) {
				int id = (int) v.getTag();
				//currentDictionary.getCon().insert(id)
				currentDictionary.getCon(true).prepareContain();
				int strid = R.string.bmAdded;
				if (currentDictionary.con.contains(id)) {
					strid = R.string.bookmarkup;
				}
				if (currentDictionary.con.insertUpdate(id) != -1) {
					int BKHistroryVagranter = a.opt.getInt("bkHVgrt", -1);
					BKHistroryVagranter = (BKHistroryVagranter + 1) % 20;
					String rec = currentDictionary.getDictionaryName() + "/?Pos=" + id;
					a.opt.putter()//.putString("bkmk", rec)
							.putString("bkh" + BKHistroryVagranter, rec)
							.putInt("bkHVgrt", BKHistroryVagranter)
							.apply();
					if (ToD) {
						pullBookMarks();
					}
					a.showX(strid, 0);
				} else {
					a.showT("添加失败,数据库出错...", 0);
				}
			}
        }

		@Override
		public int getId() {
			return 5;
		}

		@Override
		public String currentKeyText() {
			return currentKeyText;
		}

		HashMap<String, SparseArray<ScrollerRecord>> DumpedVOA = new HashMap<>();

		public void DumpVOA(BookPresenter last, BookPresenter currentDictionary) {
			boolean samePos = false;
			if(last!=BookEmpty){
				if(avoyager != last.avoyager)
					DumpedVOA.put(last.getPath(), avoyager);
				if (PDICMainAppOptions.fyeRemPos()) {
					getMainActivity().DelegateSaveAndRestorePagePos().SaveAndRestoreSinglePageForAdapter(mWebView, lastClickedPos, this);
				}
			}
			if(currentDictionary!=BookEmpty) {
				if (samePos) {
					avoyager = currentDictionary.avoyager;
				} else {
					SparseArray<ScrollerRecord> v = DumpedVOA.get(currentDictionary.getPath());
					if(v==null)
						DumpedVOA.put(currentDictionary.getPath(), v = new SparseArray<>());
					avoyager = v;
				}
			}
			mWebView.webScale=def_zoom;
		}
	}
	
	private void toggleContentVis() {
		contentview.setVisibility(contentview.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
	}
	
	// click
	public boolean cvpolicy=true;
	int SelectedV;
	final static int headerblue=0xFF2b4381;
	@Override
	public void onClick(View v) {
		MainActivityUIBase a = getMainActivity();
		//a.showT(v.getId()+"asdasd"+android.R.id.home);
		switch(v.getId()) {
			case R.id.browser_widget7: // non-final? 谷歌多作怪，安卓快淘汰!
			case R.id.home:
				hide();
			break;
			case R.id.fyeMenu:
				showPeruseTweaker();
			break;
			case R.id.toolbar_title:
			case R.id.cover:
				a.getUtk().setInvoker(currentDictionary, mWebView, null, null);
//				a.getUcc().onClick(toolbar_cover); //111
			break;
			case R.id.action0:
				contentview.setVisibility(View.VISIBLE);
			break;
			//todo ???
			case -1:
				a.onKeyDown(KeyEvent.KEYCODE_BACK, MainActivityUIBase.BackEvent);
			break;
			case R.id.ivDeleteText:
				etSearch.setText(null);
				ivDeleteText.setVisibility(View.GONE);
			break;
			case R.id.valve0:
				if(ToL=!ToL) {
					intenToLeft.setBackgroundResource(R.drawable.toleft);
					contentUIData.bottombar2.setBackgroundColor(bottombar2BaseColor);
				}else {
					intenToLeft.setBackgroundResource(R.drawable.upward);	
					mlp.removeView(contentview);
					contentUIData.bottombar2.setBackgroundColor(GlobalOptions.isDark?ColorUtils.blendARGB(a.MainBackground,Color.BLACK,a.ColorMultiplier_Wiget):a.MainBackground);
				}
				a.opt.setPerUseToL(ToL);
				a.opt.putFirstFlag();
			break;
			case R.id.valve1:
				if(ToR=!ToR) {
					intenToRight.setBackgroundResource(R.drawable.toright);
					contentUIData.bottombar2.setBackgroundColor(bottombar2BaseColor);
				} else {
					intenToRight.setBackgroundResource(R.drawable.downward);
					slp.removeView(contentview);
					contentUIData.bottombar2.setBackgroundColor(GlobalOptions.isDark?ColorUtils.blendARGB(a.MainBackground,Color.BLACK,a.ColorMultiplier_Wiget):a.MainBackground);
				}
				resetBottomBar();
				a.opt.setPerUseToR(ToR);
				a.opt.putFirstFlag();
			break;
			case R.id.valve2:
				if(ToD=!ToD) {
					intenToDown.setBackgroundResource(R.drawable.stardn);
					intenToLeft.setVisibility(View.VISIBLE);
					pullBookMarks();
    				if(contentview.getParent()==slp)
    					contentview.setVisibility(View.INVISIBLE);
				} else {
    				if((System.currentTimeMillis()-lastswicthtime>200)) {
    					//a.showT("saved "+lv2.getFirstVisiblePosition());
    					currentDictionary.bmCBI=lv2.getFirstVisiblePosition();
    					currentDictionary.bmCCI= bmsAdapter.lastClickedPos;
    				}
					intenToDown.setBackgroundResource(R.drawable.stardn1);
					intenToLeft.setVisibility(View.GONE);
				}
				bmsAdapter.notifyDataSetChanged();
				a.opt.setPerUseToD(ToD);
				a.opt.putFirstFlag();
				lastswicthtime=System.currentTimeMillis();
			break;
			case R.id.valve3:
				if(LnW=!LnW) {
					lineWrap.setBackgroundResource(R.drawable.linewrap);
				}else {
					lineWrap.setBackgroundResource(R.drawable.linewrapoff);
				}
				entryAdapter.notifyDataSetChanged();
			break;
			case R.id.undo:
				if(v.getAlpha()==1)mWebView.evaluateJavascript("document.execCommand('Undo')", null);
				break;
			case R.id.redo:
				if(v.getAlpha()==1)mWebView.evaluateJavascript("document.execCommand('Redo')", null);
				break;
			case R.id.save:
				currentDictionary.saveCurrentPage(mWebView);
				break;
			case R.id.tools:
				//mPageView.save.performLongClick(); //111
				break;
			case R.id.recess:
			case R.id.forward:
				boolean isRecess = v.getId() == R.id.recess;
				//CMN.Log("这是网页的前后导航" ,isRecess, mWebView.HistoryVagranter, mWebView.History.size());
				if (isRecess && mWebView.HistoryVagranter > 0 || !isRecess&&mWebView.HistoryVagranter<=mWebView.History.size() - 2) {
					try {
						//CMN.Log("!!!");
						mWebView.saveHistory(null, a.lastClickTime);
						int th = isRecess ? --mWebView.HistoryVagranter : ++mWebView.HistoryVagranter;
						a.lastClickTime = System.currentTimeMillis();
						
						int pos = -1;
						try {
							pos = Integer.parseInt(mWebView.History.get(th).key);
						} catch (NumberFormatException ignored) { }
						
						ScrollerRecord PageState = mWebView.History.get(th).value;
						float initialScale = BookPresenter.def_zoom;
						if (PageState != null) {
							mWebView.expectedPos = PageState.y;
							mWebView.expectedPosX = PageState.x;
							initialScale = PageState.scale;
						}
						
						if(pos>=0 && pos<currentDictionary.bookImpl.getNumberEntries()) {
							setCurrentDis(currentDictionary, pos, 0);
							currentDictionary.renderContentAt_internal(mWebView,initialScale, false, false, false, pos);
						} else {
							mWebView.loadUrl(mWebView.History.get(mWebView.HistoryVagranter).key);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case R.id.schDropdown:{
				etTools.drpdn = PDICMainAppOptions.historyShowFye();
				etTools.flowBtn = toolbar.findViewById(R.id.action_menu_presenter);
				//etTools.topbar = toolbar;
				((RelativeLayout.LayoutParams)etTools.settingsLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.toolbar);
			} break;
			default:
				a.onClick(v);
			break;
		}
	}


	@Override
	public boolean onLongClick(View v) {
		if(v.getId()==R.id.action_menu_presenter){
			showPeruseTweaker();
			return true;
		}
		return getMainActivity().onLongClick(v);
	}

	long lastswicthtime;
	public DisplayMetrics dm;
	
	private void pullBookMarks() {
		//todo close only when necessary
		cr.close();
		if(currentDictionary!=BookEmpty) {
			MdxDBHelper con = currentDictionary.getCon(false);
			if(con==null) {
				cr=EmptyCursor;
			} else {
				cr = currentDictionary.con.getDB().query("t1", null,null,null,null,null,"path");
				bookmarks_size=cr.getCount();
				if(ToD) {
					//a.showT(currentDictionary._Dictionary_fName+" "+currentDictionary.bmCBI);
					lv2.post(() -> {
						if(currentDictionary!=BookEmpty) {
							lv2.setSelection(currentDictionary.bmCBI);
						}
					});
					bmsAdapter.lastClickedPos=currentDictionary.bmCCI;
				}
			}
		}
		bmsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		CMN.Log("-----> !!! onSaveInstanceState");
		//super.onSaveInstanceState(outState);
		outState.putInt("bg", MainBackground);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		CMN.Log("-----> !!! onViewStateRestored");
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState!=null && MainBackground==0) {
			MainBackground = savedInstanceState.getInt("bg", Color.GRAY);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return onMenuItemClickmy(item,true);
	}

	public boolean onMenuItemClickmy(MenuItem m,boolean fromUser) {
		MenuItemImpl trumen = (MenuItemImpl)m;
		boolean isLongClicked=trumen.isLongClicked;
		//CMN.Log("onMenuItemClickmy::", m, isLongClicked);
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		MainActivityUIBase a = getMainActivity();
		boolean checked = !m.isChecked();
		int id=m.getItemId();
		if(ret) {
			if(m.isCheckable())
				m.setChecked(checked);
			switch(id) {
				/* 搜索 */
				case R.id.multiline: {
					if (TextUtils.equals(firstMenu.getTitle(), "翻阅模式")) {
						mWebView.evaluateJavascript("getSelection().toString()", value -> {
							String newKey = "";
							if (value.length() > 2) {
								value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
								if (value.length() > 0) {
									newKey = value;
								}
							}
							a.JumpToPeruseModeWithWord(newKey);
						});
					} else {
					
					}
				} break;
				/* 搜索 */
				case R.id.search:
					int textFlag = 0;
					if (currentDictionary.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB)
						textFlag = -1;
					tw1.onTextChanged(etSearch.getText(), -1, -1, textFlag);
					//a.showT("单本词典列表搜索！");
				break;
				/* 定位列表位置 */
				case R.id.locate:
					if(selection!=null) {
						int gvPos=SelectedV;//((DictTitleHolder)selection.getTag()).pos;
						if (bExpanded)
							gridView.setSelection(NumPreEmpter+gvPos);
						else
							scrollGridToCenterNaive(gvPos);
					}
					scrollListToCenter(entryAdapter.lastClickedPos);
				break;
				/* 页内搜索 */
				case R.id.schPage:
					toggleSearchPage();
				break;
				/* 设置翻阅 */
				case R.id.toolbar_action3:
					showPeruseTweaker();
				break;
				/* 重置搜索词 */
				case R.id.remPagePos:
					etSearch.setText(schKey);
				break;
				case R.id.mergeTools: {
					PDICMainAppOptions.setMergePeruseBottombars(checked);
					resetBottomBar();
				} break;
				/* 添加全部 */
				case R.id.showAll: {
					a.opt.setPeruseAddAll(checked);
					resetAddAll();
				} break;
				/* 保持当前网格不变 */
				case R.id.keepGroup: {
					PDICMainAppOptions.fyeKeepGroup(checked);
					a.showTopSnack(PeruseTorso, checked?"保持词典组合不变，不重新搜索。":"启动时重新搜索或添加全部！", 0.8f, -1, -1, 1);
					closeMenu = false;
				} break;
				/* 保持当前词典不变 */
				case R.id.keepBook: {
					PDICMainAppOptions.fyeKeepBook(checked);
					a.showTopSnack(PeruseTorso, checked?"保持词典不变，不自动切换。":"启动时切换至当前词典！", 0.8f, -1, -1, 1);
					closeMenu = false;
				} break;
				case R.id.translate: {
					MenuItemImpl mSTd = (MenuItemImpl) ViewUtils.findInMenu(a.AllMenusStamp, R.id.translate);
					mSTd.isLongClicked = isLongClicked;
					a.onMenuItemClick(mSTd);
				} break;
				case R.id.pause: {
					PDICMainAppOptions.fyePaused(checked);
					a.showTopSnack(PeruseTorso, checked?"需点击搜索按钮，手动触发搜索":"打字或切换词典时立即自动搜索！", 0.8f, -1, -1, 1);
					closeMenu = false;
				} break;
			}
		}
		else {
			//ret = trumen.isActionButton();
			if(id==R.id.search) {
				fromLv1 = false;
				startSearch(a);
				//a.showT("已执行联合搜索！");
				//ret = true;
			}
		}
		if(closeMenu)
			closeIfNoActionView(trumen);
		return ret;
	}
	
	private void resetAddAll() {
		boolean addAll = showAll();
		this.schKey = etSearch.getText().toString();
		MainActivityUIBase a = getMainActivity();
		if(addAll) {
			clearBeforeSch();
			for (int i = 0; i < md.size(); i++) {
				bookIds.add(a.getBookIdAt(i));
			}
			fromLv1Idx = a.dictPicker.adapter_idx;
			fromData = true;
		} else {
			if(TextUtils.equals(lastSchKey, schKey)){
				clearBeforeSch();
				bookIds.addAll(schResult);
				gridAdapter.notifyDataSetChanged();
				resetGrid(a); // reset
				fromLv1Idx = fromLv1Idx_;
			} else if(schKey!=null) {
				fromLv1 = true;
				startSearch(a);
				fromData = false;
			}
		}
		if(fromData && keepBook()) {
			fromLv1Idx = bookIds.indexOf(currentDictionary.getId());
			if(fromLv1Idx==-1) {
				fromLv1Idx = bookIds.size();
				bookIds.add(currentDictionary.getId());
			}
		}
		onViewAttached(a, true);
	}

	void closeIfNoActionView(MenuItemImpl mi) {
		if(mi!=null && !mi.isActionButton()) toolbar.getMenu().close();
	}

	public String currentDisplaying() {
		return mWebView.word;
	}

	boolean isJumping = false;
	public int bottombar2BaseColor=Constants.DefaultMainBG;
    @SuppressLint("JavascriptInterface")
	void setCurrentDis(BookPresenter invoker, long idx, int...flag) {
		if(flag==null || flag.length==0) {//书签跳转等等
			mWebView.addHistoryAt(idx);
		}
		/*回溯 或 前瞻， 不改变历史*/
		mWebView.currentPos = idx;
		mWebView.word = StringUtils.trim(mWebView.currentPos<invoker.bookImpl.getNumberEntries()?invoker.bookImpl.getEntryAt(mWebView.currentPos):"Error!!!");
		mWebView.toolbar_title.setText(mWebView.word + " - " + invoker.bookImpl.getDictionaryName());

		if(mWebView.History.size()>2){
//			recess.setVisibility(View.VISIBLE);
//			forward.setVisibility(View.VISIBLE);
			//111
		}
	}
	
	@NonNull MainActivityUIBase getMainActivity() {
		return (MainActivityUIBase) getActivity();
	}
	
	/** |0x1=xuyao store| |0x2=zhuanhuan le str|  see {@link MainActivityUIBase#storeLv1}*/
	public int etFlag =0;
	String tw1StrP;
	public boolean storeLv1(String text) {
		if ((etFlag &0x1)!=0) {
			if((etFlag &0x2)==0) {
				tw1StrP = mdict.processText(etSearch.getText());
				etFlag |=0x2;
			}
			if(Math.abs(tw1StrP.length()-text.length())<15 && mdict.processText(text).equals(tw1StrP)) {
				etFlag &=~0x1;
				return true;
			}
		}
		return false;
	}
	
	
	public final boolean showAll() {
		return menuShowAll==null?opt.getPeruseAddAll():menuShowAll.isChecked();
	}
	
	public final boolean keepGroup() {
		return menuKeepGroup==null?PDICMainAppOptions.fyeKeepGroup():menuKeepGroup.isChecked();
	}
	
	public final boolean keepBook() {
		return menuKeepBook==null?PDICMainAppOptions.fyeKeepBook():menuKeepBook.isChecked();
	}
	
	public final boolean paused() {
		return menuPaused==null?PDICMainAppOptions.fyePaused():menuPaused.isChecked();
	}
	
}

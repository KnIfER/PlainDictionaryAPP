package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.Gravity;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.DialogFragment;

import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayAdapterView.OnItemClickListener;
import com.jess.ui.TwoWayGridView;
import com.knziha.plod.db.MdxDBHelper;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.SimpleDialog;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.SplitView.PageSliderInf;
import com.knziha.plod.widgets.SplitViewGuarder;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static android.view.View.FOCUSABLE_AUTO;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;
import static com.knziha.plod.plaindict.MainActivityUIBase.init_clickspan_with_bits_at;
import static com.knziha.plod.plaindict.PDICMainActivity.ResizeNavigationIcon;
import static com.knziha.plod.plaindict.Toastable_Activity.LONG_DURATION_MS;
import static com.knziha.plod.plaindict.WebViewListHandler.WEB_VIEW_SINGLE;
import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

/** 翻阅模式，以词典为单位，搜索词为中心，一一览读。<br><br/> */
public class PeruseView extends DialogFragment implements OnClickListener, OnMenuItemClickListener, OnLongClickListener{
	int MainBackground;
	public ArrayList<Long> bookIds = new ArrayList<>();
	public ArrayList<Long> hidden = new ArrayList<>();
	public ArrayList<Long> bakedGroup = new ArrayList<>();
	//public ViewGroup peruseF;
	public byte mTBtnStates;
	public boolean bSupressingEditing;
	private boolean baked;
	public ArrayList<View> cyclerBin = new ArrayList<>();
	public ArrayList<View> recyclerBin = new ArrayList<>();
	private ArrayList<BookPresenter> md = new ArrayList<>();
	public ArrayList<PlaceHolder> ph = new ArrayList<>();
	BookPresenter BookEmpty;
	BookPresenter currentDictionary;
	ViewGroup main_pview_layout;
	private ViewGroup PeruseTorso;
	SplitView sp_main;
	SplitView sp_sub;
	ViewGroup mlp;
	ViewGroup slp;
	public Toolbar toolbar;
	DBroswer DBrowser;
	
	private ViewGroup bottom;
	private ViewGroup bottombar;
	
	TwoWayGridView LvHeadline;
	ListViewmy lv1;
	ListViewmy lv2;
	EditText etSearch;
	ImageView ivDeleteText;
	String TextToSearch;
    int HeadlineInitialSize;
    float density;

    int lvHeaderItem_length = 65;
    int lvHeaderItem_height = 55;
	public boolean bCallViewAOA=false;
	
	//bookeanMaskks
//	int adapter_idx;
//	int old_adapter_idx = -1;
	long bookId;
	long bookIdPrev;
	private BookPresenter.AppHandler perusehandler;
	SimpleDialog mDialog;

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
	float transitionTarget=-1;
	float transitionStart;
	PDICMainAppOptions opt;
	int PositionToSelect=0;
	int TargetRow;
	int NumRows=1;

	ImageView intenToLeft,intenToRight,intenToDown,lineWrap;

    int itemWidth,itemHeight;
    View vb;
	LeftViewAdapter leftLexicalAdapter;
	RightViewAdapter bookMarkAdapter;
	public int bookmarks_size;
	public int[] voyager;
	public final int VELESIZE=3;

	Cursor cr = EmptyCursor;
	
	boolean bExpanded=false;
	TextWatcher tw1;
	boolean ToL=false,ToR=false,ToD=false,LnW=false;
	boolean showAllDicts;
	public int CachedBBSize=-1;
	public WebViewmy mWebView;
	public ViewGroup root;
	BasicAdapter ActivedAdapter;

	int lastW;
	// creat dialog -> creat view
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//CMN.Log("----->onCreateView");
		if(container!=null) {
			root=container;
		}
		boolean shunt = main_pview_layout!=null;
		ViewGroup peruse_content = shunt?main_pview_layout:(ViewGroup) inflater.inflate(R.layout.main_peruse_view, root,false);
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
					CMN.Log("onLayoutChange!!!", right-left);
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
		LvHeadline = PeruseTorso.findViewById(R.id.main_dict_lst);
		LvHeadline.setHorizontalSpacing(0);
		LvHeadline.setVerticalSpacing(0);
		LvHeadline.setHorizontalScroll(true);
		LvHeadline.setStretchMode(GridView.NO_STRETCH);
		LvHeadline.setAdapter(booksShelfAdapter);
		LvHeadline.setOnItemClickListener(booksShelfAdapter);
		LvHeadline.setScrollbarFadingEnabled(false);
		LvHeadline.setSelector(getResources().getDrawable(R.drawable.listviewselector0));
		
		SplitViewGuarder svGuard = (SplitViewGuarder) PeruseTorso.getChildAt(1);
		svGuard.SplitViewsToGuard.add(sp_main = PeruseTorso.findViewById(R.id.split_view));
		svGuard.SplitViewsToGuard.add(sp_sub = sp_main.findViewById(R.id.secondary));
		
		handle1  = sp_main.findViewById(R.id.handle);
		handle2  = sp_sub.findViewById(R.id.inner_handle);
		
		sp_sub.addValve(intenToLeft = (ImageView) PeruseTorso.getChildAt(2));
		sp_sub.addValve(intenToRight = (ImageView) PeruseTorso.getChildAt(3));
		sp_sub.addValve(lineWrap = (ImageView) PeruseTorso.getChildAt(5));
		sp_main.addValve(intenToLeft);
		sp_main.addValve(intenToRight);
		sp_main.addValve(intenToDown= (ImageView) PeruseTorso.getChildAt(4));
		sp_main.guarded=sp_sub.guarded=true;
		
        toolbar.inflateMenu(R.xml.menu_pview);
		
		AllMenus = (MenuBuilder) toolbar.getMenu();
		MenuCompat.setGroupDividerEnabled(AllMenus, true);
//		MainMenus = ViewUtils.MapNumberToMenu(AllMenus, );
//		PageMenus = ViewUtils.MapNumberToMenu(AllMenus, );

		
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
		ResizeNavigationIcon(toolbar);
        toolbar.setNavigationOnClickListener(this);
		ViewUtils.setOnClickListenersOneDepth(bottombar, this, 999, 0, null);
        
		View vTmp = toolbar.findViewById(R.id.action_menu_presenter);
		if(vTmp!=null) {
			vTmp.setOnLongClickListener(this);
		}
		
		mlp = sp_sub.findViewById(R.id.mlp);
		slp = sp_sub.findViewById(R.id.slp);
		lv1 = mlp.findViewById(R.id.main_list);
		lv2 = slp.findViewById(R.id.sub_list);
		//zig-zaging
		lv1.setVerticalScrollBarEnabled(false);//关闭不可控的安卓科技
		lv1.setHorizontalScrollBarEnabled(true);//关闭不可控的安卓科技
		if(!opt.getShowFScroll()) {
			lv1.setFastScrollEnabled(false);
			lv2.setFastScrollEnabled(false);
		}
		
		//tc
		etSearch.addTextChangedListener(tw1=new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(TextUtils.getTrimmedLength(s)>0) {
					int ret = currentDictionary.bookImpl.lookUp(s.toString(), false);
					CMN.Log("peruseview::onTextChanged::", ret, ToR && cvpolicy, count);
					if(ret!=-1) {
						lv1.setSelectionFromTop(ret, (int) (20*density));
						if(ToR && cvpolicy)
						if(count==-1 || mdict.processText(currentDictionary.bookImpl.getEntryAt(ret)).equals(mdict.processText(s.toString())))
							leftLexicalAdapter.click(ret,false);
					}
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

		vb = new View(inflater.getContext());
		vb.setId(R.id.action0);
		vb.setOnClickListener(this);
		//vb.setBackgroundColor(0xafff0000);
		vb.setBackgroundResource(R.drawable.fravbg);
		//vb.getBackground().setColorFilter(CMN.MainBackground, PorterDuff.Mode.SRC_IN);
		vb.setLayoutParams(new TwoWayGridView.LayoutParams(itemWidth, itemHeight));

        sp_main.setPageSliderInf(new PageSliderInf() {
			@Override
			public void SizeChanged(int newSize,float MoveDelta) {
				if(LvHeadline.getChildCount()<1) return;
		        cc = dm.widthPixels/itemWidth; //一行容纳几列
		        if(dm.widthPixels - cc*itemWidth>0.85*itemWidth)
		        	cc++;
				if(newSize>2*itemHeight) {
					//a.showT(""+MoveDelta);
					if(!bExpanded) {//perform expansion
						//a.showT("expanding...");
						bExpanded=true;

						PositionToSelect = LvHeadline.getFirstVisiblePosition();
						if(LvHeadline.getChildAt(0).getLeft()<-0.5*itemWidth)
							PositionToSelect++;
						
						NumPreEmpter = (cc - PositionToSelect%cc)%cc;
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
				        LvHeadline.setNumColumns(cc);
				        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dm.widthPixels, -1);
				        LvHeadline.setLayoutParams(params);
				        LvHeadline.setHorizontalScroll(false);
			        	LvHeadline.setSelection(PositionToSelect+NumPreEmpter);
					}
				}
				else if(newSize>=1.2*itemHeight) {
					/* transition */
					//a.showT("entering transition...");
					if(!bExpanded) {
						int FVP = LvHeadline.getFirstVisiblePosition();//当前行起始位置
						if(transitionTarget==-1) {
							int delta=LvHeadline.getChildAt(0).getLeft();
							transitionStart = (FVP * (itemWidth)-delta);
							transitionTarget = ((FVP+(delta<=-0.5*itemWidth?1:0)) * (itemWidth));
						}
						float alpha = (float) ((2*itemHeight - newSize)/(0.8*itemHeight));
						alpha=Math.max(0, Math.min(1, alpha));
						float CurrentScrollX = (FVP*(itemWidth)-LvHeadline.getChildAt(0).getLeft());
						//a.showT(CurrentScrollX+" to "+ft+"-"+md.get(data.get(FVP))._Dictionary_fName);
						//a.showT("firstVisiblePos="+LvHeadline.getFirstVisiblePosition()+" leftOffset="+LvHeadline.getChildAt(0).getLeft());
						
						float Target = ((1-alpha)*transitionTarget+alpha*transitionStart);
						LvHeadline.smoothScrollBy((int) (Target-CurrentScrollX),60);
					}
				}
				else if(newSize<=itemHeight+1){
					/* collapse */
					if(bExpanded) {
						//a.showT("collapsing..."+PositionToSelect);
						bExpanded=false;
						transitionTarget=-1;
						NumPreEmpter=0;
						
						
				        LvHeadline.setNumColumns(bookIds.size());
				        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, -1);
				        LvHeadline.setLayoutParams(params);

				        LvHeadline.setHorizontalScroll(true);
				        LvHeadline.setSelection(PositionToSelect);
				        //LvHeadline.postInvalidate();
				        //LvHeadline.invalidate();
				        
				        LvHeadline.postDelayed(() -> {
							for(int i=0;i<LvHeadline.getChildCount();i++) {
								LvHeadline.getChildAt(i).setTop(0);
								LvHeadline.getChildAt(i).setBottom((int) (lvHeaderItem_height * density));
							}
						},160);
					}
				}
				
				if(bExpanded && newSize>HeadlineInitialSize) {
					if(MoveDelta<0){//shrinking
						int FVP = LvHeadline.getFirstVisiblePosition();//当前行起始位置
						int delta=LvHeadline.getChildAt(0).getTop();
						int TargetRowPos = TargetRow*itemHeight;
						int CurrentScrollYTop = (FVP/cc)*itemHeight - delta;
						//int maxScroll = CurrentScrollYTop-TargetRowPos;
						//a.showT(TargetRow+"@"+TargetRowPos+":"+CurrentScrollYTop);
						int CurrentScrollYBottom = CurrentScrollYTop+LvHeadline.getHeight();
						if(TargetRowPos<=CurrentScrollYBottom-itemHeight && TargetRowPos>CurrentScrollYTop){ //do regular move
							if(CurrentScrollYTop-MoveDelta>TargetRowPos)
								MoveDelta = CurrentScrollYTop-TargetRowPos;
							LvHeadline.smoothScrollBy(-(int) MoveDelta,60);
						} else if(TargetRowPos>CurrentScrollYBottom-itemHeight) {//bottom up
							LvHeadline.smoothScrollBy(TargetRowPos-CurrentScrollYBottom+itemHeight+10,200);
						} else if(TargetRowPos<CurrentScrollYTop) {//top down
							LvHeadline.smoothScrollBy(TargetRowPos-CurrentScrollYTop-10,200);
						}
						//a.showT(TargetRowPos+":"+CurrentScrollYTop);
					}
				}
			}

			@Override
			public void onDrop(int size) {
				transitionTarget=-1;
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
		}
		return mDialog;
	}

//	@Override public void onStart() {
//		CMN.Log("----->onStart");
//		super.onStart();
//	}
//	@Override
//	public void onAttach(@NonNull Context context){
//		CMN.Log("----->onAttach");
//		super.onAttach(context);
//	}
//	@Override public void onDetach(){
//		super.onDetach();
//	}
//	@Override public void onStop() {
//		CMN.Log("----->onStop");
//		super.onStop();
//	}
//	@Override public void onDestroy() {
//		CMN.Log("----->onDestroy");
//		super.onDestroy();
//	}

	//instantiate views to populate dicts' cover in
	public void onViewAttached(MainActivityUIBase a, boolean bRefresh){
		if(a==null || main_pview_layout==null) return;
		//CMN.Log("onViewAttached", TextToSearch, data, md);
		if(a.ActivedAdapter!=null&&a.ActivedAdapter.getId()<=4)
			a.PrevActivedAdapter = a.ActivedAdapter;
		a.ActivedAdapter = ActivedAdapter;
		hidden.clear();
		
		if(!ToD) {
			bookMarkAdapter.notifyDataSetChanged();
		}
		
		if(bookIds.size()==0) {
			booksShelfAdapter.notifyDataSetChanged();
			return;
		}

		RecalibrateWebScrollbar();
			
		if(bRefresh) {
			etSearch.setText(TextToSearch);
			RefreshBookShelf(a);
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
		FlowTextView tv;
		ImageView cover;
		public DictTitleHolder(long pos, View view)
		{
			bid = pos;
			tv = view.findViewById(R.id.text);
			cover = view.findViewById(R.id.image);
			view.setTag(this);
		}
		
		public void setTextColor(int ColorInt) {
			tv.setTextColor(ColorInt);
			tv.invalidate();
		}
	}
	
	private void RefreshBookShelf(MainActivityUIBase a) {
		NumPreEmpter=0;
		int NumToAdd = bookIds.size()-recyclerBin.size();

		for(int i=0;i<NumToAdd;i++) {
			View vt = a.getLayoutInflater().inflate(R.layout.main_peruse_dictlet, LvHeadline, false);
			TwoWayGridView.LayoutParams lp = new TwoWayGridView.LayoutParams(itemWidth, itemHeight);
			vt.setLayoutParams(lp);
			new DictTitleHolder(bookIds.get(i), vt);
			recyclerBin.add(vt);
		}
		booksShelfAdapter.notifyDataSetChanged();
		if(!bExpanded)
			LvHeadline.setSelection(0);
		LvHeadline.getLayoutParams().width = dm.widthPixels;
		LvHeadline.requestLayout();
		cc = dm.widthPixels/itemWidth; //一行容纳几列
		if(dm.widthPixels - cc*itemWidth>0.85*itemWidth) cc+=1;
		if(bExpanded) {
			double size = Math.ceil(1.0 * bookIds.size()/cc)*itemHeight;
			if(sp_main.getPrimaryContentSize()>size)
				sp_main.setPrimaryContentSize((int)size);
		}

		if(ToR || ToL) {
			SplitView webcontentlist = contentUIData.webcontentlister;
			if(a.opt.getPeruseBottombarOnBottom() ^ (webcontentlist.getChildAt(webcontentlist.getChildCount()-1).getId()==R.id.bottombar2))
				webcontentlist.SwitchingSides();
			if(CachedBBSize==-1) CachedBBSize=a.opt.getPeruseBottombarSize((int) (35*density));
			CachedBBSize=(int)Math.max(20*dm.density, Math.min(CachedBBSize, 50*dm.density));
			webcontentlist.setPrimaryContentSize(CachedBBSize,true);
		}

		voyager=new int[bookIds.size()*VELESIZE];
		for(int i=0;i<bookIds.size();i++)
			voyager[i*VELESIZE]=-1;
		leftLexicalAdapter.lastClickedPos=-1;
		booksShelfAdapter.flip=true;
		int off=bookIds.indexOf(bookId);
		if(off==-1) off=0;
		mWebView.clearIfNewADA(currentDictionary); // a.md_get(off<data.size()?data.get(off):-1)
		booksShelfAdapter.onItemClick(null,null,NumPreEmpter+off,0);
	}

	void RecalibrateWebScrollbar() {
		int vis = View.VISIBLE;
		boolean vsi = false;
		DragScrollBar mBar = contentUIData.dragScrollBar;
		switch (opt.getScrollbarTypePeruse()){
			case 0:
				((FrameLayout.LayoutParams) mBar.getLayoutParams()).gravity=Gravity.END;
				mBar.requestLayout();
			break;
			case 1:
				((FrameLayout.LayoutParams) mBar.getLayoutParams()).gravity=Gravity.START;
				mBar.requestLayout();
			break;
			case 2:
				vis=View.GONE;
			break;
			case 3:
				vis=View.GONE;
				vsi=true;
			break;
		}
		mBar.setVisibility(vis);
		mWebView.setVerticalScrollBarEnabled(vsi);
	}

	public void onViewDetached() {
		MainActivityUIBase a = getMainActivity();
		a.ActivedAdapter = a.PrevActivedAdapter;
		
		currentDictionary.bmCBI=lv2.getFirstVisiblePosition();
		currentDictionary.bmCCI=bookMarkAdapter.lastClickedPos;
		//currentDictionary = null;
		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
		spsubs = sp_sub.getPrimaryContentSize()*1.f/dm.widthPixels;
		
		a.opt.defaultReader.edit().putFloat("spsubs", spsubs)
		.putInt("PBBS", contentUIData.webcontentlister.getPrimaryContentSize()).apply();
		
		a.opt.putFirstFlag();
		a.OnPeruseDetached();
		//reset views back!
		//if(a.opt.getBottombarOnBottom() ^ (a.webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
		//	a.webcontentlist.SwitchingSides();
        //a.webcontentlist.setPrimaryContentSize(a.CachedBBSize,true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		CMN.Log("----->onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		MainActivityUIBase a = getMainActivity();
		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
		syncData(a);
		opt = a.opt;
		if(currentDictionary==null)
			currentDictionary = BookEmpty = a.EmptyBook;
		
		if(contentUIData==null) {
			initViews(a);
		}
		
		refreshUIColors(a.MainBackground);
		
        if(ToL = a.opt.getPerUseToL())
			intenToLeft.setBackgroundResource(R.drawable.toleft);
        if(ToR = a.opt.getPerUseToR())
			intenToRight.setBackgroundResource(R.drawable.toright);
		if(!(ToD = a.opt.getPerUseToD())) {
			intenToDown.setBackgroundResource(R.drawable.stardn1);
			intenToLeft.setVisibility(View.GONE);
		}

		if(showAllDicts = a.opt.getPeruseAddAll()) {
			AllMenus.findItem(R.id.showAll).setChecked(true);
		}

		if(Build.VERSION.SDK_INT >= 24)
        if(true) {//a.opt.is_strict_scroll()
	        ViewUtils.listViewStrictScroll(true, lv1, lv2);
        }
		
        LvHeadline.setColumnWidth((int) (lvHeaderItem_length * density));
        sp_main.setPrimaryContentSize(HeadlineInitialSize = (int) ((lvHeaderItem_height+5) * density));

        sp_sub.setPrimaryContentSize((int) (spsubs*dm.widthPixels));
        //a.showT((int) (spsubs*dm.widthPixels)+"~"+spsubs);
        
        if(bCallViewAOA) {
        	onViewAttached(a,true);
        	bCallViewAOA=false;
        }
	}

	public void showPeruseTweaker() {
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
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 6, Coef, 0, 0, 0x1, 54, 1, 3, 24, true);//opt.getPeruseInPageSearchVisible()//


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
		weblistHandler = new WebViewListHandler(a, contentUIData);
		lv1.setAdapter(ActivedAdapter = leftLexicalAdapter = new LeftViewAdapter());
		lv2.setAdapter(bookMarkAdapter = new RightViewAdapter());
		
		AllMenus.checkedDrawable = a.AllMenus.checkedDrawable;
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
		
		((MarginLayoutParams)contentUIData.dragScrollBar.getLayoutParams()).leftMargin+=sp_sub.getCompensationBottom()/2;
//		webcontentlist.scrollbar2guard=contentUIData.dragScrollBar;
		
		//tofo
		contentUIData.dragScrollBar.setOnProgressChangedListener(_mProgress -> {
			contentUIData.PageSlider.TurnPageSuppressed = _mProgress==-1;
		});
		leftLexicalAdapter.webviewHolder =
		bookMarkAdapter.webviewHolder = contentUIData.webSingleholder;
		contentUIData.webSingleholder.setBackgroundColor(CMN.GlobalPageBackground);
		
		
			mWebView = weblistHandler.getMergedFrame();
			mWebView.setMinimumWidth((int) (100*GlobalOptions.density));
			mWebView.weblistHandler = weblistHandler;
			//mWebView.fromPeruseview = true;
			mWebView.fromCombined=3;
			a.initWebScrollChanged();//Strategy: use one webscroll listener
	        mWebView.setOnScrollChangedListener(a.onWebScrollChanged);
	        mWebView.setPadding(0, 0, 18, 0);
			contentUIData.dragScrollBar.setDelimiter("< >", mWebView);
    		mWebView.getSettings().setSupportZoom(true);
			perusehandler = new BookPresenter.AppHandler(a.currentDictionary);
			mWebView.addJavascriptInterface(perusehandler, "app");
			ViewUtils.setOnClickListenersOneDepth(weblistHandler.mMergedBook.toolbar, this, 999, null);

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
		

		if(opt.getBottomNavigationMode1()==1)
			setBottomNavigationType(1, null);

//		contentUIData.browserWidget8.setOnClickListener(this);
//		contentUIData.browserWidget8.setOnLongClickListener(this);
		
		contentUIData.PageSlider.IMSlider = contentUIData.cover;
		contentUIData.PageSlider.TurnPageEnabled=opt.getPageTurn3();
//		if(a.IMPageCover!=null)//111
//			IMPageCover.setPageSliderInf(a.IMPageCover.inf);

//		webcontentlist.setPageSliderInf(a.inf);
//		webSingleholder.addView(rl);
		ViewUtils.addViewToParent(mWebView.rl, contentUIData.webSingleholder);
		
//		rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
//		mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

		if(opt.getPeruseInPageSearchVisible()){
			toggleInPageSearch(false);
		}
	}
	
	private void resetBottomBar() {
		boolean merge = PDICMainAppOptions.getMergePeruseBottombars();
		ViewUtils.addViewToParent(contentUIData.bottombar2, weblistHandler.isPopupShowing()||!merge?contentUIData.webcontentlister:bottom);
		ViewUtils.setVisible(bottombar, !merge || contentview.getParent()!=slp);
		ViewUtils.setVisible(contentUIData.bottombar2, !merge || contentview.getParent()==slp || weblistHandler.isPopupShowing());
	}
	
	void toggleInPageSearch(boolean isLongClicked) {
		MainActivityUIBase a = getMainActivity();
		if(isLongClicked){
			a.launchSettings(7, 0);
		}
		else {
			Toolbar InPageSearchbar = PerusePageSearchbar;
			if (InPageSearchbar == null) {
				Toolbar searchbar = (Toolbar) getLayoutInflater().inflate(R.layout.searchbar, null);
				searchbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
				EditText etSearch = searchbar.findViewById(R.id.etSearch);
				//etSearch.setBackgroundColor(Color.TRANSPARENT);
				searchbar.setNavigationOnClickListener(v1 -> {
					toggleInPageSearch(false);
					if (etSearch.hasFocus())
						a.imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					a.fadeSnack();
				});
				etSearch.setText(PerusePageSearchetSearchStartWord);
				etSearch.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

					}

					@Override
					public void afterTextChanged(Editable s) {
						String text = etSearch.getText().toString().replace("\\", "\\\\");
						a.HiFiJumpRequested=PDICMainAppOptions.getPageAutoScrollOnType();
						a.SearchInPage(text);
					}
				});

				View vTmp = searchbar.getChildAt(searchbar.getChildCount() - 1);
				if (vTmp != null && vTmp.getClass() == AppCompatImageButton.class) {
					AppCompatImageButton NavigationIcon = (AppCompatImageButton) vTmp;
					ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) NavigationIcon.getLayoutParams();
					//lp.setMargins(-10,-10,-10,-10);
					lp.width = (int) (45 * dm.density);
					NavigationIcon.setLayoutParams(lp);
				}

				searchbar.setContentInsetsAbsolute(0, 0);
				searchbar.setLayoutParams(toolbar.getLayoutParams());
				searchbar.setBackgroundColor(a.AppWhite==Color.WHITE?a.MainBackground:Color.BLACK);
				searchbar.setBackgroundColor(a.MainBackground);
				searchbar.findViewById(R.id.ivDeleteText).setOnClickListener(v -> etSearch.setText(null));
				View.OnDragListener searchbar_stl = (v, event) -> {
					if(event.getAction()== DragEvent.ACTION_DROP){
						ClipData textdata = event.getClipData();
						if(textdata.getItemCount()>0){
							if(textdata.getItemAt(0).getText()!=null)
								etSearch.setText(textdata.getItemAt(0).getText());
						}
						return false;
					}
					return true;
				};
				InPageSearchbar = this.PerusePageSearchbar = searchbar;
				this.PerusePageSearchetSearch = etSearch;
				this.PerusePageSearchindicator = searchbar.findViewById(R.id.indicator);
				View viewTmp=searchbar.findViewById(R.id.recess);
				viewTmp.setOnDragListener(searchbar_stl);
				viewTmp.setOnClickListener(this);
				viewTmp=searchbar.findViewById(R.id.forward);
				viewTmp.setOnDragListener(searchbar_stl);
				viewTmp.setOnClickListener(this);
			}
			ViewGroup parent = (ViewGroup) InPageSearchbar.getParent();
			boolean b1= parent ==null;
			if (b1) {
				contentview.addView(InPageSearchbar, 0);
				InPageSearchbar.findViewById(R.id.etSearch).requestFocus();
				InPageSearchbar.setTag(PerusePageSearchetSearch.getText());
				a.SearchInPage(null);
			} else {
				parent.removeView(InPageSearchbar);
				mWebView.evaluateJavascript("clearHighlights()", null);
				InPageSearchbar.setTag(null);
			}
			opt.setPeruseInPageSearchVisible(b1);
			//PerusePageSearchbar.post(() -> RecalibrateContentSnacker(opt.isContentBow()));
		}
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

    BSTopAdapter booksShelfAdapter = new BSTopAdapter();
	public boolean bClickToggleView=false;
	HashSet<Long> addAllHashSet = new HashSet<>();
	
	public void prepareJump(MainActivityUIBase a, String content, ArrayList<Long> _data, long _bookId) {
		if(content==null) return;
		if(_data==null) {
			ScanSearchAllByText(content, a, false, a.updateAI);
		}
		else {
			TextToSearch = content;
			syncData(a);
			bookId = _bookId;
			bookIds=_data;
			/* 边界检查 */
			for (int i=bookIds.size()-1; i>=0; i--) {
				if(bookIds.get(i)<0)
					bookIds.remove(i);
			}
		}
		addAllHashSet.clear();
		addAllHashSet.addAll(bookIds);
		if(showAllDicts) {
			for(int i=0;i<md.size();i++) {
				long bid = a.getBookIdAt(i);
				if(!addAllHashSet.contains(bid)) {
					addAllHashSet.add(bid);
					bookIds.add(bid);
				}
			}
		} else {
			for(int i=0;i<md.size();i++) {
				long bid = a.getBookIdAt(i);
				if(!bookIds.contains(bid)) {
					hidden.add(bid);
				}
			}
		}
	}

	private void syncData(MainActivityUIBase a) {
		md = a.md;
		ph = a.getLazyCC();
	}

	public void prepareInPageSearch(String key, boolean bNeedBringUp) {
		if(PerusePageSearchetSearch==null){
			PerusePageSearchetSearchStartWord=key;
		}else{
			PerusePageSearchetSearch.setText(key);
			bNeedBringUp=bNeedBringUp&&PerusePageSearchbar.getParent()==null;
		}
		if(bNeedBringUp){
			toggleInPageSearch(false);
		}
	}
	
	public boolean toggleTurnPageEnabled() {
		return contentUIData.PageSlider.TurnPageEnabled=opt.setPageTurn3(!opt.getPageTurn3());
	}

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
		if(a!=null) {
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
				 PDICMainAppOptions.getHistoryStrategy4() && !PDICMainAppOptions.getHistoryStrategy0()
				&& (PDICMainAppOptions.getHistoryStrategy8() == 2)){
			a.insertUpdate_histroy(mWebView.word, 0, contentUIData.webSingleholder);
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
		return mWebView.lastY+mWebView.toolbar_title.getHeight()+LvHeadline.getHeight()+etSearch.getHeight();
	}

	public void dismissDialogOnly() {
		if(mDialog!=null && !isAttached()){
			mDialog.dismiss();
			mDialog.decorBright();
		}
	}

	/** 来自lv1列表点击(一次使用)。来自不带数据的 prepareJump。 */
	public void ScanSearchAllByText(String text, MainActivityUIBase a, boolean addCurrent, boolean updateAI) {
		if(text==null) return;
		syncData(a);
		opt = a.opt;
		TextToSearch=text;
		bookIds.clear();
		bookIds.ensureCapacity(md.size());
		if(updateAI){
			bookId = a.getBookIdAt(a.adapter_idx);
			a.updateAI = false;
		} else {
			RestoreOldAI();
		}
		text = mdict.replaceReg.matcher(text).replaceAll("").toLowerCase();
		baked = false;
		if(showAllDicts) {
			bookIds.clear();
			for (int i = 0; i < md.size(); i++)
				bookIds.add(a.getBookIdAt(i));
		} else {
			bakeCurrentGroup(a, addCurrent, text);
		}
		booksShelfAdapter.notifyDataSetChanged();
	}

	private void bakeCurrentGroup(MainActivityUIBase a, boolean addCurrent, String text) {
		bookIds.clear();
		bakedGroup.clear();
		for (int i = 0; i < md.size(); i++) {
			if (addCurrent && i == a.adapter_idx) {
				long bid = a.getBookIdAt(i);
				bookIds.add(bid);
				bakedGroup.add(bid);
				continue;
			}
			BookPresenter presenter = a.md_get(i);
			if(presenter==a.EmptyBook)
				continue;
			int idx = presenter.bookImpl.lookUp(text);
			//CMN.Log(mdTmp.getEntryAt(idx), idx, text, mdTmp._Dictionary_fName);
			if (idx >= 0){
				if (presenter.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
					long bid = a.getBookIdAt(i);
					bookIds.add(bid);
					bakedGroup.add(bid);
					continue;
				}
				String toCompare = mdict.replaceReg.matcher(presenter.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase();
				int len = text.length();
				int len1 = len;
				int len2 = toCompare.length();
				//CMN.Log("cidx??",mdTmp._Dictionary_fName, toCompare);
				if(len>0 && len2>0/* && len>=toCompare.length()*/ && text.charAt(0)==toCompare.charAt(0)){
					if(len==1){
						long bid = a.getBookIdAt(i);
						bookIds.add(bid);
						bakedGroup.add(bid);
					} else {
						len = Math.min(len, len2);
						int cidx = 1;
						for (; cidx < len; cidx++) {
							if (text.charAt(cidx) != toCompare.charAt(cidx))
								break;
						}
						cidx--;
						//CMN.Log("cidx", cidx, text, toCompare, mdTmp._Dictionary_fName);
						if (cidx > 0) {
							if (cidx>=len1/3 && (len - cidx <= 4 || cidx>=len2/2)) {
								long bid = a.getBookIdAt(i);
								bookIds.add(bid);
								bakedGroup.add(bid);
							}
						}
					}
				}
			}
		}
		baked = true;
	}

	public void RestoreOldAI() {
		if(opt.getPeruseRestoreOldAI()){
			if(bookIdPrev>=0){
				bookId = bookIdPrev;
			}
		}
	}

	//for top list
    public class BSTopAdapter extends BaseAdapter implements OnItemClickListener
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
			DictTitleHolder holder = (DictTitleHolder) ItemView.getTag();
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			if (a!=null) {
				BookPresenter presenter = a.getBookByIdNoCreation(bookIds.get(position));
				Drawable cover=null;
				String pathname;
				if(presenter!=a.EmptyBook) {
					pathname=presenter.getDictionaryName();
					cover=presenter.cover;
				} else {
					pathname=a.getBookNameByIdNoCreation(bookIds.get(position));
				}
				holder.tv.setText(pathname);
				holder.cover.setImageDrawable(cover);
			}
	        return ItemView;
        }

		//dict
		@Override
		public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
        	MainActivityUIBase a = getMainActivity();
        	if(a==null) return;
			ViewGroup p = (ViewGroup) vb.getParent();
			if(p!=null) {
				if(view!=null && lv1.getChildCount()>0 && System.currentTimeMillis()-lastswicthtime>200) {//record our position
					voyager[SelectedV*VELESIZE] = lv1.getFirstVisiblePosition();
					voyager[SelectedV*VELESIZE+1] = lv1.getChildAt(0).getTop();
					if(leftLexicalAdapter.lastClickedPos!=-1)
						voyager[SelectedV*VELESIZE+2] = leftLexicalAdapter.lastClickedPos;
					//record page position
					ActivedAdapter.lastClickedPosBeforePageTurn = ActivedAdapter.lastClickedPos;
					ActivedAdapter.SaveVOA();

					lastswicthtime=System.currentTimeMillis();
				}
				((DictTitleHolder)p.getTag()).setTextColor(Color.WHITE);
				p.removeView(vb);
			}
			
			leftLexicalAdapter.lastClickedPos=-1;
			
			SelectedV=position-NumPreEmpter;
			if(view==null){
				view = recyclerBin.get(SelectedV);
			} else {
				bookIdPrev = bookIds.get(SelectedV);
			}

			TargetRow = position/cc;
			PositionToSelect = TargetRow*cc;
			if(TargetRow>=1) {
				PositionToSelect-=NumPreEmpter;
			}
			//a.showT(cc+"should collapse at: "+PositionToSelect);
			((ViewGroup) view).addView(vb);

			BookPresenter OldDictionary = currentDictionary;
			bookId = bookIds.get(SelectedV);

			currentDictionary = a.getBookById(bookId);
			perusehandler.setDict(currentDictionary);

			leftLexicalAdapter.DumpVOA(OldDictionary, currentDictionary);
			leftLexicalAdapter.notifyDataSetChanged();
			//todo also right
			((DictTitleHolder)view.getTag()).setTextColor(headerblue);
			//notifyDataSetChanged();
			//LvHeadline.setLayoutParams(LvHeadline.getLayoutParams());

			/* 初始化 | 自动搜索 */
			if((opt.getForceSearch() || voyager[SelectedV*3]<0) && currentDictionary!=BookEmpty) {
				if(!TextUtils.isEmpty(TextToSearch)) {
					voyager[SelectedV*VELESIZE] = currentDictionary.bookImpl.lookUp(TextToSearch,false);
					voyager[SelectedV*VELESIZE+2] = voyager[SelectedV*VELESIZE];
					voyager[SelectedV*VELESIZE+1]=(int) (20*density);
				}
			}

			//a.showT(voyager[SelectedV*3] + "to "+currentDictionary.getEntryAt(voyager[SelectedV*3]));
			
			if(voyager[SelectedV*VELESIZE]!=-1) {
				// lv1.setSelectionFromTop(0, (int) (20*density));
				// lv1.setSelectionFromTop(voyager[SelectedV*3], voyager[SelectedV*3+1]);
				lv1.setSelection(voyager[SelectedV*VELESIZE]);
				lv1.setSelectionFromTop(voyager[SelectedV*VELESIZE], voyager[SelectedV*VELESIZE+1]);
				//if(lv1.getFirstVisiblePosition()!=voyager[SelectedV*3])//make it right
				if(flip)
				lv1.post(() -> {
					// lv1.setSelectionFromTop(voyager[SelectedV*3], voyager[SelectedV*3+1]);
					// lv1.setSelection(voyager[SelectedV*3]);
					lv1.setSelectionFromTop(voyager[SelectedV*VELESIZE], voyager[SelectedV*VELESIZE+1]);
					flip=false;
				});
				if(voyager[SelectedV*VELESIZE+2]>=0) {
					if(ToR && cvpolicy && contentview.getVisibility()==View.VISIBLE) {//water can flow, unless the valve is closed.
						//if(mdict.processText(TextToSearch).equals(mdict.processText(currentDictionary.getEntryAt(voyager[SelectedV*3]))))
						leftLexicalAdapter.click(voyager[SelectedV*VELESIZE+2],false);
					}else {
						leftLexicalAdapter.lastClickedPos=voyager[SelectedV*VELESIZE+2];//WHY CAN U CAN?
					}
				}
			}

			a.showTopSnack(PeruseTorso, currentDictionary.bookImpl.getDictionaryName()
					, 0.8f, -1, -1, 1);

        	mlp.removeView(contentview);
        	
        	if(ToD) {
        		if(OldDictionary!=null) {
        			OldDictionary.bmCBI=lv2.getFirstVisiblePosition();
        			OldDictionary.bmCCI=bookMarkAdapter.lastClickedPos;
        		}
        		pullBookMarks();
        	} 
        	
			//oldV=view;
			//a.showT(NumPreEmpter+"-"+(position-NumPreEmpter)+"="+currentDictionary._Dictionary_fName);
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
			ActivedAdapter=a.ActivedAdapter=this;
			
			WebViewmy mWebView = PeruseView.this.mWebView;
			contentUIData.PageSlider.WebContext = mWebView;
			mWebView.IBC = currentDictionary.IBC;
			contentUIData.PageSlider.invalidateIBC();
			
        	if(ToD) {
        		//a.setContentBow(false);
        		//super.onItemClick(pos);
            	a.ActivedAdapter=this;
            	if(pos<0) {
					a.showTopSnack(a.main_succinct, R.string.endendr, -1, -1, -1, 0);
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
		
				if(ToL&&bClickToggleView && lastClickedPos==pos && contentview.getParent()==mlp) {
					toggleContentVis();
					return;
				}
		
				contentview.setVisibility(View.VISIBLE);
				contentUIData.webSingleholder.setVisibility(View.VISIBLE);
		
				SplitView webcontentlist = contentUIData.webcontentlister;
				if(ToL) {
					if(a.opt.getPeruseBottombarOnBottom() ^ (webcontentlist.getChildAt(webcontentlist.getChildCount()-1).getId()==R.id.bottombar2))
                    	webcontentlist.SwitchingSides();
					webcontentlist.setPrimaryContentSizeUnChanged(CachedBBSize,true);
					ViewUtils.addViewToParent(contentview, mlp);
            	} else {
            		if(a.opt.getBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
                    	webcontentlist.SwitchingSides();
					webcontentlist.setPrimaryContentSizeUnChanged(a.CachedBBSize,true);
            		ViewUtils.addViewToParent(contentview, main_pview_layout);
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
    			a.decorateContentviewByKey(null, mWebView.word);
				mWebView.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        	} else {
        		ViewGroup p = (ViewGroup) vb.getParent();
    			if(p!=null) {
    				currentDictionary = a.EmptyBook;
    				voyager[SelectedV*VELESIZE] = lv1.getFirstVisiblePosition();
    				voyager[SelectedV*VELESIZE+1] = lv1.getChildAt(0).getTop();
    				if(leftLexicalAdapter.lastClickedPos!=-1)
    					voyager[SelectedV*VELESIZE+2] = leftLexicalAdapter.lastClickedPos;
    				((FlowTextView)p.findViewById(R.id.text)).setTextColor(Color.WHITE);
    				p.removeView(vb);
    			}
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
				perusehandler.setDict(currentDictionary);
				leftLexicalAdapter.notifyDataSetChanged();
        	}
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
        	if(currentDictionary!=null)
        		return (int) currentDictionary.bookImpl.getNumberEntries();
        	else
        		return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {//length=1046; index=5173
        	String currentKeyText = currentDictionary.bookImpl.getEntryAt(position);
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
			if(!mWebView.isloading && lastClickedPosBeforePageTurn>=0 && contentUIData.webSingleholder.getChildCount()!=0) {
				if(mWebView.webScale==0) mWebView.webScale=dm.density;//sanity check
				//avoyager.get(avoyagerIdx).set((int) (mWebView.getScrollX()), (int) (mWebView.getScrollY()), webScale);
				ScrollerRecord pagerec = avoyager.get(lastClickedPosBeforePageTurn);
				if(pagerec==null) {
					pagerec=new ScrollerRecord();
					avoyager.put(lastClickedPosBeforePageTurn, pagerec);
				}
				pagerec.set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
			}
		}

		@Override
		public void onItemClick(int pos) {//lv1
        	click(pos,true);
        }
        
        private void click(int pos,boolean ismachineClick) {
			MainActivityUIBase a = getMainActivity();
			ActivedAdapter=a.ActivedAdapter=this;

    		lastClickedPosBeforePageTurn = lastClickedPos;
        	super.onItemClick(pos);
        	if(pos<-1)
        		return;
        	//-1放行
        	if(pos>=getCount()) {
        		lastClickedPos = getCount()-1;
        		a.show(R.string.endendr);
        		return;
    		}
	
			WebViewmy mWebView = PeruseView.this.mWebView;
			contentUIData.PageSlider.WebContext = mWebView;
			mWebView.IBC = currentDictionary.IBC;
			contentUIData.PageSlider.invalidateIBC();
			weblistHandler.setViewMode(WEB_VIEW_SINGLE, false);
        	
//        	if(widget14.getVisibility()==View.VISIBLE) {
//        		widget13.setVisibility(View.GONE);
//        		widget14.setVisibility(View.GONE);
//        	}
        	
			int f = lv1.getFirstVisiblePosition();
			int c = lv1.getChildCount();
			int o=lastClickedPos-f;
			if(o>=0 && o<c) {
				lv1.getChildAt(o).setBackgroundColor(Color.TRANSPARENT);
			}
			o=pos-f;
			//boolean proceed=true;
			if(ismachineClick)
			if(o==c-1) {
				int delta = lv1.getHeight() -lv1.getChildAt(o).getTop();
				float judger = lv1.getChildAt(o).getHeight();
				if(delta <= judger*2/3) {
					if(delta<judger/2)
						lv1.setSelection(pos);
					else
						lv1.scrollListBy((int) (judger-(lv1.getHeight()-lv1.getChildAt(o).getTop())));
					//proceed=false;
				}	
			}
			//if(proceed) {
				if(o>=0 && o<c) {
					if(lv1.getChildAt(o)!=null)//TODO why crash?
						lv1.getChildAt(o).setBackgroundColor(0xff397CCD);
				}else if(ismachineClick){
					lv1.setSelection(pos);
				}
			//}

				float desiredScale=a.prepareSingleWebviewForAda(currentDictionary, mWebView, pos, this);
			
        	//doing: adaptively add and remove!
			if(ToR && bClickToggleView && lastClickedPos==pos && contentview.getParent()==slp) {
				toggleContentVis();
				return;
			}
	
//			contentview.setVisibility(View.VISIBLE);
//			contentUIData.webSingleholder.setVisibility(View.VISIBLE);
			weblistHandler.initMergedFrame(true, !ToR, false);
	
			SplitView webcontentlist = contentUIData.webcontentlister;
    		if(ToR) {
                if(a.opt.getPeruseBottombarOnBottom() ^ (webcontentlist.getChildAt(webcontentlist.getChildCount()-1).getId()==R.id.bottombar2))
                	webcontentlist.SwitchingSides();
				webcontentlist.setPrimaryContentSize/*UnChanged*/(CachedBBSize,true);
        		ViewUtils.addViewToParent(contentview, slp);
        	} else {
				weblistHandler.popupContentView(null, "happy");
//        		if(a.opt.getBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
//                	webcontentlist.SwitchingSides();
//				webcontentlist.setPrimaryContentSize/*UnChanged*/(a.CachedBBSize,true);
//				ViewUtils.addViewToParent(contentview, main_pview_layout);
        	}
			//a.showT(pos+":"+lastClickedPos);
        	lastClickedPos = pos;
	
			mWebView.clearIfNewADA(currentDictionary);
        	
        	setCurrentDis(currentDictionary, lastClickedPos);
        	if (pos==0 && currentDictionary.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
				currentDictionary.SetSearchKey(etSearch.getText().toString());
			}

			if(a.opt.getAutoReadEntry()
					&& !PDICMainAppOptions.getTmpIsAudior(currentDictionary.tmpIsFlag)){
				mWebView.bRequestedSoundPlayback=true;
			}
			
			currentKeyText = mWebView.word;
        	currentDictionary.renderContentAt(desiredScale,RENDERFLAG_NEW,0, mWebView, lastClickedPos);
			
			//voyager[SelectedV*3+2]=pos;
			a.decorateContentviewByKey(contentUIData.browserWidget8, currentKeyText);
			if(//!(currentDictionary instanceof bookPresenter_txt)&& nimp
					PDICMainAppOptions.getHistoryStrategy4() && !PDICMainAppOptions.getHistoryStrategy0()
					&& (!ismachineClick || PDICMainAppOptions.getHistoryStrategy8() == 0)){
				a.insertUpdate_histroy(currentKeyText, 0, contentUIData.webSingleholder);
			}
	
			resetBottomBar();
	
			a.getTopSnackView().setNextOffsetScale(0.24f);
			a.showTopSnack(PeruseTorso, currentKeyText
					, 0.85f, LONG_DURATION_MS, -1, 0);
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
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			click(position,false);
			cvpolicy=true;
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

		public void DumpVOA(BookPresenter oldDictionary, BookPresenter currentDictionary) {
			if(oldDictionary!=null){
				DumpedVOA.put(oldDictionary.getPath(), avoyager);
			} else if(currentDictionary!=BookEmpty){
				SparseArray<ScrollerRecord> _avoyager = DumpedVOA.get(currentDictionary.getPath());
				if(_avoyager==null)
					DumpedVOA.put(currentDictionary.getPath(), _avoyager = new SparseArray<>());
				avoyager = _avoyager;
			}
		}
	}
	
	private void toggleContentVis() {
		contentview.setVisibility(contentview.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
	}
	
	//click
	public boolean cvpolicy=true;
	int SelectedV;
	final static int headerblue=0xFF2b4381;
	@Override
	public void onClick(View v) {
		MainActivityUIBase a = getMainActivity();
		//a.showT(v.getId()+"asdasd"+android.R.id.home);
		switch(v.getId()) {
			case R.id.browser_widget7:
			case R.id.home:
				hide();
			break;
			case R.id.menu:
				a.showMenuGrid(v);
			break;
			case R.id.toolbar_title:
			case R.id.cover:
				a.getUcc().setInvoker(currentDictionary, mWebView, null, null);
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
    					currentDictionary.bmCCI=bookMarkAdapter.lastClickedPos;
    				}
					intenToDown.setBackgroundResource(R.drawable.stardn1);
					intenToLeft.setVisibility(View.GONE);
				}
				bookMarkAdapter.notifyDataSetChanged();
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
				leftLexicalAdapter.notifyDataSetChanged();
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
					bookMarkAdapter.lastClickedPos=currentDictionary.bmCCI;
				}
			}
		}
		bookMarkAdapter.notifyDataSetChanged();
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
		CMN.Log("onMenuItemClickmy::", m);
		MenuItemImpl mmi = (MenuItemImpl)m;
		boolean isLongClicked=mmi.isLongClicked;
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		MainActivityUIBase a = getMainActivity();
		switch(m.getItemId()) {
			/* 搜索 */
			case R.id.toolbar_action1:
				if(isLongClicked){
					TextToSearch = etSearch.getText().toString();
					closeMenu = ret = true;
					if(voyager!=null)
					for (int i = 0; i*VELESIZE < voyager.length; i+=3) {
						voyager[i*VELESIZE] = -1;
					}
					if(!showAllDicts){ //要更新全部哟！
						bakeCurrentGroup(a, false, TextToSearch);
						RefreshBookShelf(a);
						a.showT("已重新搜索全部词典！");
					} else {
						a.showT("已更新搜索词！");
					}
				}
				int textFlag = 0;
				if (currentDictionary.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB)
					textFlag = -1;
				tw1.onTextChanged(etSearch.getText(), 0, 0, textFlag);
			break;
			/* 页内搜索 */
			case R.id.toolbar_action2:
				if(isLongClicked) break;
				toggleInPageSearch(isLongClicked);
			break;
			/* 设置翻阅 */
			case R.id.toolbar_action3:
				if(isLongClicked) break;
				showPeruseTweaker();
			break;
			/* 重置搜索词 */
			case R.id.remPagePos:
				if(isLongClicked) break;
				etSearch.setText(TextToSearch);
			break;
			case R.id.mergeTools: {
				boolean val = !m.isChecked();
				m.setChecked(val);
				PDICMainAppOptions.setMergePeruseBottombars(val);
				resetBottomBar();
			} break;
			/* 添加全部 */
			case R.id.showAll: {
				if(isLongClicked) break;
				showAllDicts =!m.isChecked();
				m.setChecked(showAllDicts);
				//if(fromUser){
					a.opt.setPeruseAddAll(showAllDicts);
					refreshAddAll(showAllDicts);
				//}
			} break;
			case R.id.translate: {
				MenuItemImpl mSTd = (MenuItemImpl) ViewUtils.findInMenu(a.AllMenusStamp, R.id.translate);
				mSTd.isLongClicked = isLongClicked;
				a.onMenuItemClick(mSTd);
			} break;
		}
		if(closeMenu)
			closeIfNoActionView(mmi);
		return ret;
	}

	private void refreshAddAll(boolean addAll) {
		bookIds.clear();
		MainActivityUIBase a = getMainActivity();
		if(addAll){
			for (int i = 0; i < md.size(); i++) {
				bookIds.add(a.getBookIdAt(i));
			}
		} else {
			if(baked){
				for(Long iI:bakedGroup)
					bookIds.add(iI);
			} else if(TextToSearch!=null){
				bakeCurrentGroup(a, true, TextToSearch);
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
	
	void setBottomNavigationType(int type, TextView tv) {
		if (type == 0) {
			contentUIData.browserWidget10.setImageResource(R.drawable.chevron_left);
			contentUIData.browserWidget11.setImageResource(R.drawable.chevron_right);
		} else if (type == 1) {
			contentUIData.browserWidget10.setImageResource(R.drawable.chevron_recess);
			contentUIData.browserWidget11.setImageResource(R.drawable.chevron_forward);
		}
		if(tv!=null) tv.setText(getResources().getTextArray(R.array.btm_navmode)[type]);
	}
	
	@NonNull MainActivityUIBase getMainActivity() {
		return (MainActivityUIBase) getActivity();
	}
	
}

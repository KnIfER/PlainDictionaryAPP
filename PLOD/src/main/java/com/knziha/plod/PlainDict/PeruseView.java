package com.knziha.plod.PlainDict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.androidadvance.topsnackbar.TSnackbar;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayAdapterView.OnItemClickListener;
import com.jess.ui.TwoWayGridView;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.widgets.IMPageSlider;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.SplitView.PageSliderInf;
import com.knziha.plod.widgets.SplitViewGuarder;
import com.knziha.plod.widgets.SumsungLikeScrollBar;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.dictionary.Flag;
import com.knziha.plod.dictionary.myCpr;
import com.knziha.plod.PlainDict.MainActivityUIBase.UniCoverClicker;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict.MJavascriptInterface;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.ColorUtils;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;


public class PeruseView extends Fragment implements OnClickListener, OnMenuItemClickListener, OnLongClickListener{
	//MainActivity a;
	public ArrayList<Integer> data;
	public ArrayList<View> recyclerBin = new ArrayList<>();
	List<mdict> md;
	mdict currentDictionary;
	View main_pview_layout;
	SplitView sp_main;
	SplitView sp_sub;
	ViewGroup mlp;
	ViewGroup slp;
	Toolbar toolbar;
	
	TwoWayGridView LvHeadline;
	ListView lv1;
	ListView lv2;
	EditText etSearch;
	ImageView ivDeleteText;
	String TextToSearch;
    int HeadlineInitialSize;
    float density;

    int lvHeaderItem_length = 65;
    int lvHeaderItem_height = 55;
	public boolean bCallViewAOA=false;
	
	//bookeanMaskks
	public ViewGroup contentview;
	private ImageView favoriteBtn;
	

	public View widget13,widget14;
	private int adapter_idx;
	//构造
	public PeruseView(){
		super();
		//this(null);
	}
	
	//public Fragment_PeruseView(MainActivity a_) {
		
	//}
	
	int cc;
	float transitionTarget=-1;
	float transitionStart;
	int PositionToSelect=0;
	int TargetRow;
	int NumRows=1;

	ImageView intenToLeft,intenToRight,intenToDown,lineWrap;

    int itemWidth,itemHeight;
    View vb;
	ListViewAdapter ada2;
	private BaseAdaptermy2 bookMarkAdapter = new BaseAdaptermy2();
	public int bookmarks_size;
	public ArrayList<Integer> bookmarks = new ArrayList<>();
	public ArrayList<Integer> othermds = new ArrayList<>();
	public int[] voyager;
	public final int VELESIZE=3;

	Cursor cr;	
	
	boolean bExpanded=false;
	TextWatcher tw1;
	boolean ToL=false,ToR=false,ToD=false,LnW=false;
	boolean showBA,showBD,showFastScroll,ForceSearch;
	public int CachedBBSize=-1;
	ViewGroup webSingleholder;
	ViewGroup webholder;
	ScrollView WHP;
	SplitView webcontentlist;
	ViewGroup bottombar2;
	RLContainerSlider PageSlider;
	boolean TurnPageEnabled;
	private ViewGroup rl;
	WebViewmy mWebView;
	float webScale;
	ViewGroup toolbar_web;
	ImageView toolbar_cover;
	TextView toolbar_title;
	UniCoverClicker ucc;
	SumsungLikeScrollBar mBar;
	View recess;
	View forward;
	ViewGroup root;
	private BasicAdapter ActivedAdapter;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root=container;
		main_pview_layout = inflater.inflate(R.layout.main_peruse_view, container,false);
		//contentview = (ViewGroup) inflater.inflate(R.layout.contentview, container,false);
		LvHeadline = (TwoWayGridView) main_pview_layout.findViewById(R.id.main_dict_lst);
        LvHeadline.setHorizontalSpacing(0); 
        LvHeadline.setVerticalSpacing(0);
        LvHeadline.setHorizontalScroll(true);
        LvHeadline.setStretchMode(GridView.NO_STRETCH);
		LvHeadline.setAdapter(ada);
		LvHeadline.setOnItemClickListener(ada);
        LvHeadline.setScrollbarFadingEnabled(false);
        LvHeadline.setSelector(getResources().getDrawable(R.drawable.listviewselector0));
        //LvHeadline.setDrawSelectorOnTop(false);
        
		main_pview_layout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}});

        toolbar = main_pview_layout.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.pview_menu);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
        View vTmp = toolbar.getChildAt(toolbar.getChildCount()-1);
        if(vTmp!=null && vTmp.getClass()==AppCompatImageButton.class) {
        	AppCompatImageButton NavigationIcon=(AppCompatImageButton) vTmp;
        	MarginLayoutParams lp = (MarginLayoutParams) NavigationIcon.getLayoutParams();
        	//lp.setMargins(-10,-10,-10,-10);
        	lp.width=(int) (45*dm.density);
        	NavigationIcon.setLayoutParams(lp);
        	//toolbar.removeView(NavigationIcon);
        	//toolbar.addView(NavigationIcon,1);
        }
        toolbar.setNavigationOnClickListener(this);

		lv1 = main_pview_layout.findViewById(R.id.main_list);
		lv2 = main_pview_layout.findViewById(R.id.sub_list);
		//zig-zaging
		lv1.setVerticalScrollBarEnabled(false);//关闭不可控的安卓科技
		if(!showFastScroll) {
			lv1.setFastScrollEnabled(false);
			lv2.setFastScrollEnabled(false);
		}
		etSearch = main_pview_layout.findViewById(R.id.etSearch);
		ivDeleteText = main_pview_layout.findViewById(R.id.ivDeleteText);
		
		etSearch.addTextChangedListener(tw1=new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length()>0)
				if(currentDictionary!=null) {
					int ret = currentDictionary.lookUp(s.toString(),false);
					if(ret!=-1) {
						lv1.setSelectionFromTop(ret, (int) (20*density));
						if(ToR && cvpolicy)
						if(mdict.processText(currentDictionary.getEntryAt(ret)).equals(mdict.processText(s.toString())))
							ada2.click(ret,false);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length()>0)
					ivDeleteText.setVisibility(View.VISIBLE);
			}});
		
		ada2 = new ListViewAdapter();
		lv1.setAdapter(ada2);
		lv1.setOnItemClickListener(ada2);
		
		lv2.setAdapter(bookMarkAdapter);
		lv2.setOnItemClickListener(bookMarkAdapter);
		
        SplitViewGuarder svGuard = (SplitViewGuarder) main_pview_layout.findViewById(R.id.svGuard);
        svGuard.SplitViewsToGuard.add(sp_main = main_pview_layout.findViewById(R.id.split_view));
        svGuard.SplitViewsToGuard.add(sp_sub = main_pview_layout.findViewById(R.id.secondary));
        sp_sub.addValve(intenToLeft = main_pview_layout.findViewById(R.id.valve0));
        sp_sub.addValve(intenToRight = main_pview_layout.findViewById(R.id.valve1));
        sp_sub.addValve(lineWrap = main_pview_layout.findViewById(R.id.valve3));
        sp_main.addValve(intenToLeft);
        sp_main.addValve(intenToRight);
        sp_main.addValve(intenToDown=main_pview_layout.findViewById(R.id.valve2));
        sp_main.guarded=sp_sub.guarded=true;
        mlp = main_pview_layout.findViewById(R.id.mlp);
        slp = main_pview_layout.findViewById(R.id.slp);
                
        ivDeleteText.setOnClickListener(this);
        intenToLeft.setOnClickListener(this);
        intenToRight.setOnClickListener(this);
        intenToDown.setOnClickListener(this);
        lineWrap.setOnClickListener(this);
        
        
		itemWidth = (int) (lvHeaderItem_length * density);
        itemHeight = (int) (lvHeaderItem_height * density);
        cc = dm.widthPixels/itemWidth; //一行容纳几列
        if(dm.widthPixels - cc*itemWidth>0.85*itemWidth)
        	cc++;


        sp_main.setPageSliderInf(new PageSliderInf() {
			@Override
			public void SizeChanged(int newSize,float MoveDelta) {
				if(LvHeadline.getChildCount()<1) return;
				getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
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
						TargetRow = (PositionToSelect+NumPreEmpter)/cc;
						NumRows =  (int) Math.ceil(((float)data.size()+NumPreEmpter)/cc);
				        LvHeadline.setNumColumns(cc);
				        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dm.widthPixels, -1);
				        LvHeadline.setLayoutParams(params);
				        LvHeadline.setHorizontalScroll(false);
			        	LvHeadline.setSelection(PositionToSelect+NumPreEmpter);
					}
				}else if(newSize>=1.2*itemHeight) {//transition
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
				}else if(newSize<=itemHeight+1){//collapse
					if(bExpanded) {
						//a.showT("collapsing..."+PositionToSelect);
						bExpanded=false;
						transitionTarget=-1;
						NumPreEmpter=0;
						
						
				        LvHeadline.setNumColumns(data.size());
				        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, -1);
				        LvHeadline.setLayoutParams(params);

				        LvHeadline.setHorizontalScroll(true);
				        LvHeadline.setSelection(PositionToSelect);
				        //LvHeadline.postInvalidate();
				        //LvHeadline.invalidate();
				        
				        LvHeadline.postDelayed(new Runnable() {
							@Override
							public void run() {
						        for(int i=0;i<LvHeadline.getChildCount();i++) {
						        	LvHeadline.getChildAt(i).setTop(0);
						        	LvHeadline.getChildAt(i).setBottom((int) (lvHeaderItem_height * density));
						        }
							}},160);
					}
				}
				
				
				if(bExpanded && newSize>HeadlineInitialSize) {
					if(MoveDelta<0){//shrinking
						int FVP = LvHeadline.getFirstVisiblePosition();//当前行起始位置
						int delta=LvHeadline.getChildAt(0).getTop();
						int TargetRowPos = TargetRow*itemHeight;
						int CurrentScrollYTop = (FVP/cc)*itemHeight - delta;
						int maxScroll = CurrentScrollYTop-TargetRowPos;
						//a.showT(TargetRow+"@"+TargetRowPos+":"+CurrentScrollYTop);
						int CurrentScrollYBottom = CurrentScrollYTop+LvHeadline.getHeight();
						if(TargetRowPos<=CurrentScrollYBottom-itemHeight && TargetRowPos>CurrentScrollYTop){ //do regular move
							if(CurrentScrollYTop-MoveDelta>TargetRowPos)
								MoveDelta = CurrentScrollYTop-TargetRowPos;
							LvHeadline.smoothScrollBy(-(int) MoveDelta,60);
						}else if(TargetRowPos>CurrentScrollYBottom-itemHeight) {//bottom up
							LvHeadline.smoothScrollBy(TargetRowPos-CurrentScrollYBottom+itemHeight+10,200);
						}else if(TargetRowPos<CurrentScrollYTop) {//top down
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
        
		return main_pview_layout;
	}
	
	
	
	
	@Override
	public void onStart() {
		super.onStart();

	}
	
	@Override
	public void onAttach(Context context){
		super.onAttach(context);
		//CMN.show("onAttach");
	}
		


    
	//instantiate views to populate in
	public void onViewAttached(MainActivityUIBase a, boolean bRefresh){
		a.PrevActivedAdapter = a.ActivedAdapter;
		a.ActivedAdapter = ActivedAdapter;
		othermds.clear();
		for(int i=0;i<md.size();i++) {
			if(!data.contains(i)) {
				othermds.add(i);
			}
		}
		if(!ToD) bookMarkAdapter.notifyDataSetChanged();
		
		if(data==null || md==null) return;
		if(data.size()==0) return;
        //int itemWidth = (int) (lvHeaderItem_length * density);
        //int itemHeight = (int) (lvHeaderItem_height * density);
		if(a.opt.getHideScroll3())
			mBar.setVisibility(View.GONE);
		else
			mBar.setVisibility(View.VISIBLE);
			
		if(bRefresh) {
			etSearch.setText(TextToSearch);
			NumPreEmpter=0;
	        int NumToAdd = data.size()-recyclerBin.size();
	
			for(int i=0;i<NumToAdd;i++) {
				View vt = a.getLayoutInflater().inflate(R.layout.main_peruse_dictlet, null);
		        TwoWayGridView.LayoutParams lp = new TwoWayGridView.LayoutParams(itemWidth, itemHeight);
		        vt.setLayoutParams(lp);
		        //vt.setOnClickListener(this);
		        vt.setTag(data.get(i));
				recyclerBin.add(vt);
		        //Toast.makeText(MainActivity.this, "asd", Toast.LENGTH_SHORT).show();
				//LvHeadline.invalidate();
	        }
			ada.notifyDataSetChanged();
			if(!bExpanded)
				LvHeadline.setSelection(0);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dm.widthPixels, -1);
	        LvHeadline.setLayoutParams(params);
			cc = dm.widthPixels/itemWidth; //一行容纳几列
	        if(dm.widthPixels - cc*itemWidth>0.85*itemWidth) cc+=1;
	        if(bExpanded) {
	        	double size = Math.ceil(1.0 * data.size()/cc)*itemHeight;
	        	if(sp_main.getPrimaryContentSize()>size) 
	        		sp_main.setPrimaryContentSize((int)size);
	        		
	        }
	        
	        if(ToR || ToL) {
		        if(a.opt.getPeruseBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
	            	webcontentlist.SwitchingSides();
	    		if(CachedBBSize==-1) CachedBBSize=a.opt.getPeruseBottombarSize((int) (35*density));
	    		CachedBBSize=(int)Math.max(20*dm.density, Math.min(CachedBBSize, 50*dm.density));
				webcontentlist.setPrimaryContentSize(CachedBBSize,true);
	        }
	        
			voyager=new int[data.size()*VELESIZE];
	        for(int i=0;i<data.size();i++)
	        	voyager[i*VELESIZE]=-1;
	        ada2.lastClickedPos=-1;
	        ada.flip=true;
	        ada.onItemClick(null,null,NumPreEmpter,0);
	        //CMN.Log("clicked");
		}
        
            
        
        
        if(bRefresh) {
	        
        }
        //contentview.findViewById(R.id.bottombar2).setBackgroundColor(0xFF808080);
	}

	
	
	public void onViewDetached() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		a.ActivedAdapter = a.PrevActivedAdapter;
		
		if(currentDictionary!=null) {
			currentDictionary.bmCBI=lv2.getFirstVisiblePosition();
			currentDictionary.bmCCI=bookMarkAdapter.lastClickedPos;
		}
		//currentDictionary=null;
		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
		spsubs = sp_sub.getPrimaryContentSize()*1.f/dm.widthPixels;
		
		a.opt.defaultReader.edit().putFloat("spsubs", spsubs)
		.putInt("PBBS", webcontentlist.getPrimaryContentSize()).commit();
		
		a.opt.putFirstFlag();
		//reset views back!
		//if(a.opt.getBottombarOnBottom() ^ (a.webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
		//	a.webcontentlist.SwitchingSides();
        //a.webcontentlist.setPrimaryContentSize(a.CachedBBSize,true);
	}
	
	
	@Override
	public void onDetach(){
		super.onDetach();
	}
	
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//CMN.Log("onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
		md = a.md;
		if(contentview ==null)
			inflateContentView();
		
		refreshUIColors();
		
        if(ToL = a.opt.getPerUseToL())
			intenToLeft.setBackgroundResource(R.drawable.toleft);
        if(ToR = a.opt.getPerUseToR())
			intenToRight.setBackgroundResource(R.drawable.toright);
		if(!(ToD = a.opt.getPerUseToD())) {
			intenToDown.setBackgroundResource(R.drawable.stardn1);
			intenToLeft.setVisibility(View.GONE);
		}
		
		if(showBA = a.opt.getShowBA()) {
			onMenuItemClick(toolbar.getMenu().getItem(1));
		}
		if(showBD = a.opt.getShowBD()) {
			onMenuItemClick(toolbar.getMenu().getItem(2));
		}
		if(ForceSearch = a.opt.getForceSearch()) {
			onMenuItemClick(toolbar.getMenu().getItem(3));
		}
		if(showFastScroll = a.opt.getShowFScroll()) {
			onMenuItemClick(toolbar.getMenu().getItem(4));
		}

		vb = new View(a.getApplicationContext());
		vb.setId(R.id.action0);
		vb.setOnClickListener(this);
		
        //vb.setBackgroundColor(0xafff0000);
		vb.setBackgroundResource(R.drawable.fravbg);
		//vb.getBackground().setColorFilter(CMN.MainBackground, PorterDuff.Mode.SRC_IN);
        TwoWayGridView.LayoutParams lp = new TwoWayGridView.LayoutParams(itemWidth, itemHeight);
        vb.setLayoutParams(lp);
        
        
		if(Build.VERSION.SDK_INT >= 24)
        if(true) {//a.opt.is_strict_scroll()
	        a.listViewStrictScroll(lv1,true);
	        a.listViewStrictScroll(lv2,true);
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
  
	public void inflateContentView() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(getView()!=null && getView().getParent()!=null)//already attached.
		if(contentview!=null && contentview.getParent()!=null) ((ViewGroup) contentview.getParent()).removeView(contentview);
		contentview = (ViewGroup) a.inflater.inflate(R.layout.contentview, null,false);
		rl = (ViewGroup) a.inflater.inflate(R.layout.contentview_item, null,false);
		mBar = (SumsungLikeScrollBar)contentview.findViewById(R.id.dragScrollBar);
		webSingleholder = contentview.findViewById(R.id.webSingleholder);
		webSingleholder.setBackgroundColor(a.GlobalPageBackground);
			mWebView = rl.findViewById(R.id.webviewmy);
			webScale = mdict.def_zoom;
			a.initWebScrollChanged();//Strategy: use one webscroll listener
	        mWebView.setOnSrollChangedListener(a.onWebScrollChanged);
	        mWebView.setPadding(0, 0, 18, 0);
        	mBar.setDelimiter("< >");
    		mBar.scrollee=mWebView;
    		mWebView.getSettings().setSupportZoom(true);
    		
	        toolbar_web= (ViewGroup)rl.findViewById(R.id.toolbar);
	        toolbar_title = ((TextView)toolbar_web.findViewById(R.id.toolbar_title));
			toolbar_cover = (ImageView)toolbar_web.findViewById(R.id.cover);
			ucc = a.getUcc();
			toolbar_cover.setOnClickListener(this);
			toolbar_cover.setTag(R.id.position,false);
			toolbar_title.setOnClickListener(this);
			//toolbar.setTitle(this._Dictionary_FName.split(".mdx")[0]);
			recess = toolbar_web.findViewById(R.id.recess);
			recess.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//CMN.show(""+HistoryVagranter+"asd"+mWebView.canGoBack());mWebView.goBack();
					if(HistoryVagranter>0) {
						try {
							if(!mWebView.isloading)
								if(HistoryVagranter>=0) History.get(HistoryVagranter).value=mWebView.getScrollY();
							int pos=-1;
							try {
								pos = Integer.valueOf(History.get(--HistoryVagranter).key);
							} catch (NumberFormatException e) {
								//e.printStackTrace();
							}
							expectedPos = History.get(HistoryVagranter).value;
							if(pos!=-1) {
								setCurrentDis(currentDictionary,pos, 0);
								currentDictionary.htmlBuilder.setLength(currentDictionary.htmlHeader.length());
								mWebView.loadDataWithBaseURL(currentDictionary.baseUrl,
										currentDictionary.htmlBuilder.append(currentDictionary.getRecordsAt(pos))
													.append(currentDictionary.js)
													.append(currentDictionary.htmlTailer).toString()
										,null, "UTF-8", null);
							}else {
								mWebView.loadUrl(History.get(HistoryVagranter).key);//
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}});
			forward=toolbar_web.findViewById(R.id.forward);
			forward.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//CMN.show(""+HistoryVagranter);
					if(HistoryVagranter<=History.size()-2) {
						try {
							if(!mWebView.isloading)
								if(HistoryVagranter>=0) History.get(HistoryVagranter).value=mWebView.getScrollY();
							int pos=-1;
							try {
								pos = Integer.valueOf(History.get(++HistoryVagranter).key);
							} catch (NumberFormatException e) {
								//e.printStackTrace();
							}
							expectedPos = History.get(HistoryVagranter).value;
							//a.showT("expectedPos"+expectedPos);
							if(pos!=-1) {
								setCurrentDis(currentDictionary,pos, 0);
								currentDictionary.htmlBuilder.setLength(currentDictionary.htmlHeader.length());
								mWebView.loadDataWithBaseURL(currentDictionary.baseUrl,
										currentDictionary.htmlBuilder.append(currentDictionary.getRecordsAt(pos))
													.append(currentDictionary.js)
													.append(currentDictionary.htmlTailer).toString()
										,null, "UTF-8", null);
							}else {
								mWebView.loadUrl(History.get(HistoryVagranter).key);//
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}});
			
			
		webholder = contentview.findViewById(R.id.webholder);
		WHP = (ScrollView) webholder.getParent();
		WHP.setVisibility(View.GONE);
		webcontentlist = (SplitView)contentview.findViewById(R.id.webcontentlister);
        webcontentlist.multiplier=-1;
        webcontentlist.isSlik=true;
        bottombar2 = (ViewGroup) webcontentlist.findViewById(R.id.bottombar2);
        if(ToL||ToR) {
			bottombar2.setBackgroundColor(bottombar2BaseColor);
		}else {
			bottombar2.setBackgroundColor(a.opt.getInDarkMode()?ColorUtils.blendARGB(a.MainBackground,Color.BLACK,a.ColorMultiplier_Wiget):a.MainBackground);
		}
		mlp.removeView(contentview);
        
        favoriteBtn = bottombar2.findViewById(R.id.browser_widget7);
        favoriteBtn.setOnClickListener(this);
        favoriteBtn.setOnLongClickListener(this);
        favoriteBtn = bottombar2.findViewById(R.id.browser_widget9);
        favoriteBtn.setOnLongClickListener(this);
        favoriteBtn.setOnClickListener(this);
        bottombar2.findViewById(R.id.browser_widget10).setOnClickListener(this);
        bottombar2.findViewById(R.id.browser_widget11).setOnClickListener(this);
        bottombar2.findViewById(R.id.browser_widget12).setOnClickListener(this);
        favoriteBtn = (ImageView)bottombar2.findViewById(R.id.browser_widget8);
		favoriteBtn.setOnClickListener(this);
		favoriteBtn.setOnLongClickListener(this);
		


        IMPageCover = contentview.findViewById(R.id.cover);
        PageSlider = (RLContainerSlider)  contentview.findViewById(R.id.PageSlider);
        PageSlider.IMSlider = IMPageCover;
        TurnPageEnabled=PageSlider.TurnPageEnabled=true;
        IMPageCover.setPageSliderInf(a.IMPageCover.inf);
        webcontentlist.setPageSliderInf(a.webcontentlist.inf);
		webSingleholder.addView(rl);

        (widget13=PageSlider.findViewById(R.id.browser_widget13)).setOnClickListener(this);
        (widget14=PageSlider.findViewById(R.id.browser_widget14)).setOnClickListener(this);
        
		rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
		mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
	}

	public void refreshUIColors() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		boolean isChecked = a.AppWhite==Color.BLACK;
		
		mWebView.evaluateJavascript(isChecked? MainActivityUIBase.DarkModeIncantation: MainActivityUIBase.DeDarkModeIncantation, null);
		main_pview_layout.setBackgroundColor(isChecked?Color.BLACK:0xff8f8f8f);
		bottombar2.setBackgroundColor(bottombar2BaseColor = !isChecked?0xff8f8f8f:ColorUtils.blendARGB(0xff8f8f8f, Color.BLACK, a.ColorMultiplier_Web));
		webSingleholder.setBackgroundColor(!isChecked?a.GlobalPageBackground:ColorUtils.blendARGB(a.GlobalPageBackground, Color.BLACK, a.ColorMultiplier_Web));
	}

	float spsubs;

	
	int NumPreEmpter=0;

    BaseAdaptermy ada = new BaseAdaptermy();
	public boolean bClickToggleView=false;
	
	//for top list
    public class BaseAdaptermy extends BaseAdapter implements OnItemClickListener
    {
    	
        public boolean flip;

		//构造
        public BaseAdaptermy() 
        {
        	
        }
        @Override
        public int getCount() {
        	if(data.size()!=0)
        		return data.size()+NumPreEmpter;
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
        		View vt = new View(getActivity().getApplicationContext());
                int itemWidth = (int) (lvHeaderItem_length * density);
                int itemHeight = (int) (lvHeaderItem_height * density);
                TwoWayGridView.LayoutParams lp = new TwoWayGridView.LayoutParams(itemWidth, itemHeight);
                vt.setLayoutParams(lp);
                vt.setOnClickListener(PeruseView.this);
        		return vt;
        	}
        	position-=NumPreEmpter;
        	View ItemView = recyclerBin.get(position);
			int mdIdx = data.get(position);
	        mdict mdTmp = md.get(mdIdx);
	        ImageView iv = ItemView.findViewById(R.id.image);
	        TextView tv = ItemView.findViewById(R.id.text);
	        tv.setText(mdTmp._Dictionary_fName);
	        if(mdTmp.cover!=null)
	        	iv.setImageDrawable(mdTmp.cover);
	        else
	        	iv.setImageDrawable(null);//R.drawable.cover

	        //if(position==SelectedV)  tv.setTextColor(headerblue);
	        //else  tv.setTextColor(Color.WHITE);
	        
	        return ItemView;
        }
        
		public void onItemClick(int pos) {
        	
		}
		
		@Override
		public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
			ViewGroup p = (ViewGroup) vb.getParent();
			if(p!=null) {
				if(view!=null) {//record our position
					if(System.currentTimeMillis()-lastswicthtime>200) {
						voyager[SelectedV*VELESIZE] = lv1.getFirstVisiblePosition();
						voyager[SelectedV*VELESIZE+1] = (int) lv1.getChildAt(0).getTop();
						if(ada2.lastClickedPos!=-1)
							voyager[SelectedV*VELESIZE+2] = ada2.lastClickedPos;
						lastswicthtime=System.currentTimeMillis();
					}
				}
				((TextView)p.findViewById(R.id.text)).setTextColor(Color.WHITE);
				p.removeView(vb);
			}
			
			ada2.lastClickedPos=-1;
			
			SelectedV=position-NumPreEmpter;
			if(view==null)
				view = recyclerBin.get(SelectedV);
			TargetRow = position/cc;
			PositionToSelect = TargetRow*cc;
			if(TargetRow>=1) {
				PositionToSelect-=NumPreEmpter;
			}
			//a.showT(cc+"should collapse at: "+PositionToSelect);
			((ViewGroup) view).addView(vb);
			
			//for(int i=0;i<recyclerBin.size();i++) 
			mdict OldDictionary = currentDictionary;
			currentDictionary = md.get(adapter_idx = data.get(SelectedV));
			ada2.notifyDataSetChanged();
			((TextView)view.findViewById(R.id.text)).setTextColor(headerblue);
			//notifyDataSetChanged();
			//LvHeadline.setLayoutParams(LvHeadline.getLayoutParams());
			
			if(ForceSearch || voyager[SelectedV*3]<0) {//初始化，自动搜索
				if(TextToSearch!=null && !"".equals(TextToSearch)) {
					voyager[SelectedV*VELESIZE] = currentDictionary.lookUp(TextToSearch,false);
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
				lv1.post(new Runnable() {
					@Override
					public void run() {
						// lv1.setSelectionFromTop(voyager[SelectedV*3], voyager[SelectedV*3+1]);
						// lv1.setSelection(voyager[SelectedV*3]);
						lv1.setSelectionFromTop(voyager[SelectedV*VELESIZE], voyager[SelectedV*VELESIZE+1]);
						flip=false;
					}
				});
				if(voyager[SelectedV*VELESIZE+2]>=0) {
					if(ToR && cvpolicy && contentview.getVisibility()==View.VISIBLE) {//water can flow, unless the valve is closed.
						//if(mdict.processText(TextToSearch).equals(mdict.processText(currentDictionary.getEntryAt(voyager[SelectedV*3]))))
						ada2.click(voyager[SelectedV*VELESIZE+2],false);
					}else {
						ada2.lastClickedPos=voyager[SelectedV*VELESIZE+2];//WHY CAN U CAN?
					}
				}
			}
			
			mlp.post(new Runnable() {
				//magic crash here.. without 'post' it will say(if you click very fast):
				//Attempt to invoke virtual method 'int android.view.View.getVisibility()' on a null object reference
		        //at android.widget.FrameLayout.layoutChildren
				@Override
				public void run() {
					TSnackbar snack = TSnackbar.makeraw(mlp, currentDictionary._Dictionary_fName,TSnackbar.LENGTH_LONG);
					((TextView)snack.getView().findViewById(R.id.snackbar_text)).setSingleLine();
					snack.getView().setAlpha(0.8f);
					snack.show();
				}
			});
			

        	mlp.removeView(contentview);
        	
        	if(ToD) {
        		if(OldDictionary!=null) {
        			OldDictionary.bmCBI=lv2.getFirstVisiblePosition();
        			OldDictionary.bmCCI=bookMarkAdapter.lastClickedPos;
        		}
        		pullBookMarks();//true
        	} 
        	
			//oldV=view;
			//a.showT(NumPreEmpter+"-"+(position-NumPreEmpter)+"="+currentDictionary._Dictionary_fName);
		}
		
    }
    
    //for right view
    public class BaseAdaptermy2 extends BasicAdapter {
    	int lastClickedDictPos=-1;
		@Override
		public int getCount() {
			return ToD?(cr==null?0:cr.getCount()):othermds.size();
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.drawer_list_item, null);
				vh = new viewholder();
        		vh.tv=convertView.findViewById(R.id.text1);
        		vh.dv=convertView.findViewById(R.id.del);
        		vh.tv.setTextColor(Color.WHITE);
    			vh.tv.setPadding((int) (16*density), 0, 0, 0);
        		vh.dv.setId(R.id.deld);
        		int p = (int) (10*density);
        		vh.dv.setPadding(p, p, p, p);
        		vh.dv.setOnClickListener(this);
        		vh.dv.setColorFilter(Color.RED);
				convertView.setTag(vh);
			}else
				vh = (viewholder) convertView.getTag();
    		vh.dv.setTag(position);
			if(ToD) {
				cr.moveToPosition(cr.getCount()-position-1);
				vh.tv.setText(currentDictionary.getEntryAt(cr.getInt(0)));//bookmarks.get(position)
				vh.tv.setSingleLine(false);
			}else {
				vh.tv.setText(md.get(othermds.get(position))._Dictionary_fName);
				vh.tv.setSingleLine();
			}
			
			if(position==(ToD?lastClickedPos:lastClickedDictPos)) {//voyager[SelectedV*3+2]
        		//which color?
        		convertView.setBackgroundColor(0xff397CCD);//LB0xff397CCD  HB0xff2b4381
        	}else
        		convertView.setBackgroundColor(Color.TRANSPARENT);
			
			if(ToD && showBD) {
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
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
        	if(ToD) {
        		a.setContentBow(false);
        		//super.onItemClick(pos);
            	a.ActivedAdapter=this;
            	if(pos<0) {
            		a.show(R.string.toptopr);
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
    			ViewGroup someView2 = (ViewGroup) contentview.getParent();
        		
            	if(ToL) {
            		if(bClickToggleView)
        			if(lastClickedPos==pos && someView2==mlp) {
        				if(contentview.getVisibility()==View.VISIBLE)
        					contentview.setVisibility(View.INVISIBLE);
        				else
        					contentview.setVisibility(View.VISIBLE);
        				//slp.removeView();
        				return;
        			}
            		if(contentview.getVisibility()!=View.VISIBLE) contentview.setVisibility(View.VISIBLE);
            		if(webSingleholder.getVisibility()!=View.VISIBLE) webSingleholder.setVisibility(View.VISIBLE);
            		
            		
                    if(a.opt.getPeruseBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
                    	webcontentlist.SwitchingSides();
            		if(webcontentlist.getPrimaryContentSize()!=CachedBBSize)//here
            			webcontentlist.setPrimaryContentSize(CachedBBSize,true);
            		
            		if(someView2!=mlp) {
            			if(someView2!=null) someView2.removeView(contentview);
            			mlp.addView(contentview);
            		}
            	}else {
            		if(contentview.getVisibility()!=View.VISIBLE) contentview.setVisibility(View.VISIBLE);
            		if(webSingleholder.getVisibility()!=View.VISIBLE) webSingleholder.setVisibility(View.VISIBLE);
            		
            		
            		if(a.opt.getBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
                    	webcontentlist.SwitchingSides();
            		if(webcontentlist.getPrimaryContentSize()!=a.CachedBBSize)
            			webcontentlist.setPrimaryContentSize(a.CachedBBSize,true);
            		
            		if(someView2!=a.main) {
            			if(someView2!=null) someView2.removeView(contentview);
            			a.main.addView(contentview);
            		}
            	}
    			
            	lastClickedPos = pos;
            	
    			currentDictionary.initViewsHolder(a);
            	currentDictionary.clearWebview();
            	
    			ViewGroup someView = currentDictionary.rl;//adaptively remove views?
    			
				if(someView.getParent()!=webSingleholder) {
    				if(someView.getParent()!=null) ((ViewGroup)someView.getParent()).removeView(someView);
    				webSingleholder.addView(someView);
				}
    			if(webSingleholder.getChildCount()>1) {
    				for(int i=webSingleholder.getChildCount()-1;i>=0;i--)
    					if(webSingleholder.getChildAt(i)!=currentDictionary.rl)
    						((ViewGroup)someView.getParent()).removeViewAt(i);
    			}
    			if(WHP.getVisibility()==View.VISIBLE) {
	    			if(webholder.getChildCount()>0)
	    				webholder.removeAllViews();
	    			WHP.setVisibility(View.GONE);
    			}
    			
    			cr.moveToPosition(cr.getCount()-lastClickedPos-1);
    			currentDictionary.renderContentAt(-1,adapter_idx,mWebView,cr.getInt(0));//bookmarks.get(lastClickedPos)
    			
    			currentKeyText = currentDictionary.currentDisplaying;
    			String key = currentKeyText;
    			
    				int pos1 = currentDictionary.currentPos;
    				while(pos1-1>=0 && currentDictionary.getEntryAt(pos1-1).equals(key)) {
    					pos1--;
    				}
    				pos1 = currentDictionary.currentPos - pos1;
    				if(pos1>0) {
    					for(int i=0;i<pos1;i++)
    						key += "\n";
    				}
    				
    			//voyager[SelectedV*3+2]=pos;
    			a.decorateContentviewByKey(favoriteBtn,key);
    			currentDictionary.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
    			currentDictionary.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        	}else {
        		ViewGroup p = (ViewGroup) vb.getParent();
    			if(p!=null) {
    				currentDictionary=null;
    				voyager[SelectedV*VELESIZE] = lv1.getFirstVisiblePosition();
    				voyager[SelectedV*VELESIZE+1] = (int) lv1.getChildAt(0).getTop();
    				if(ada2.lastClickedPos!=-1)
    					voyager[SelectedV*VELESIZE+2] = ada2.lastClickedPos;
    				((TextView)p.findViewById(R.id.text)).setTextColor(Color.WHITE);
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
    			currentDictionary = md.get(adapter_idx = othermds.get(lastClickedDictPos=pos));
				ada2.notifyDataSetChanged();
        	}
        }
        
		public void onClick(View v) {
			switch(v.getId()) {
    			case R.id.deld:
    				int id = (int) v.getTag();
    				currentDictionary.getCon();
    				cr.moveToPosition(cr.getCount()-id-1);
    				if(currentDictionary.con.remove(cr.getInt(0))>0) {
            			//a.showX(R.string.delDone,0);
            			pullBookMarks();
            		}
            		else
    				((MainActivityUIBase) getActivity()).showT("删除失败,数据库出错...",0);
				break;
			}
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			click(position,false);
		}
	}

    private final class viewholder{
    	TextView tv;
    	ImageView dv;
    }
    
    //for left view
    public class ListViewAdapter extends BasicAdapter
						implements OnClickListener,ListView.OnItemClickListener{
		//AbsListView.LayoutParams lp;
        int lastClickedPos;
        //构造
        public ListViewAdapter() 
        {  
    		//lp = new AbsListView.LayoutParams(-1,-1);
    		//lp.setMargins(280, 0, 0, 0);  
        }
        @Override
        public int getCount() {
        	if(md!=null && md.size()>0 && currentDictionary!=null)
        		return (int) currentDictionary.getNumberEntries();
        	else
        		return 0;
        }
        Flag mflag = new Flag();
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {//length=1046; index=5173
        	String currentKeyText = currentDictionary.getEntryAt(position);
        	viewholder vh;
	        if(convertView==null){
        		convertView = View.inflate(getContext(), R.layout.listview_item2, null);
        		convertView.setId(R.id.lvitems);
        		vh = new viewholder();
        		vh.tv=convertView.findViewById(R.id.text);
        		vh.dv=convertView.findViewById(R.id.del);
        		vh.dv.setOnClickListener(this);
                convertView.setTag(vh);
        	}else
        		vh=(viewholder) convertView.getTag();
	        vh.tv.setText(currentKeyText);
	        //int tagetMaxLn=LnW?-1:1;
	        //if(vh.tv.getMaxLines()!=tagetMaxLn)
	        //	vh.tv.setMaxLines(tagetMaxLn);
	        vh.tv.setSingleLine(LnW?false:true);
	        
    		vh.dv.setTag(position);
        	convertView.setTag(R.id.position,position);
        	if(position==lastClickedPos) {//voyager[SelectedV*3+2]
        		//which color?
        		convertView.setBackgroundColor(0xff397CCD);//LB0xff397CCD  HB0xff2b4381
        	}else
        		convertView.setBackgroundColor(Color.TRANSPARENT);
        	
        	if(showBA) {
        		vh.dv.setVisibility(View.VISIBLE);
        		vh.tv.setPadding(0, (int)(5*density), 0, (int)(2*density));
        	}else {
        		vh.dv.setVisibility(View.GONE);
        		vh.tv.setPadding((int)(15*density), (int)(5*density), 0, (int)(2*density));
        	}
	        return convertView;
        }
        
        @Override
		public void onItemClick(int pos) {//lv1
        	click(pos,true);
        }
        
        private void click(int pos,boolean ismachineClick) {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
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
        	
        	if(widget14.getVisibility()==View.VISIBLE) {
        		widget13.setVisibility(View.GONE);
        		widget14.setVisibility(View.GONE);
        	}
        	
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

				float desiredScale=-1;
				if(a.opt.getRemPos()) {
					ScrollerRecord pagerec;
					if(System.currentTimeMillis()-a.lastClickTime>300)//save our postion
		        	if((!mWebView.isloading) && lastClickedPosBeforePageTurn>=0 && webSingleholder.getChildCount()!=0) {
		        		if(currentDictionary.webScale==0) currentDictionary.webScale=dm.density;//sanity check
		        		//avoyager.get(avoyagerIdx).set((int) (mWebView.getScrollX()), (int) (mWebView.getScrollY()), webScale);
		        		pagerec = avoyager.get(lastClickedPosBeforePageTurn);
		        		if(pagerec==null) {
		        			pagerec=new ScrollerRecord();
		        			avoyager.put(lastClickedPosBeforePageTurn, pagerec);
		        		}
		        		pagerec.set((int) (mWebView.getScrollX()),(int) (mWebView.getScrollY()),webScale);
		        	}
					
					a.lastClickTime=System.currentTimeMillis();
					
					pagerec = avoyager.get(pos);
					if(pagerec!=null) {
		        		expectedPos=pagerec.y;
		        		expectedPosX=pagerec.x;
		        		desiredScale=pagerec.scale;
		        		a.showT(avoyager.size()+"~"+pos+"~取出旧值"+currentDictionary.expectedPos);
	        		}else {
			        	expectedPos=0;
		        		expectedPosX=0;
	        		}
					
		        	//showT(""+currentDictionary.expectedPos);
				}else
					expectedPos=0;
				
			
        	//doing: adaptively add and remove!

    		ViewGroup someView2 = (ViewGroup) contentview.getParent();

    		if(ToR) {
        		if(bClickToggleView)
    			if(lastClickedPos==pos && someView2==slp) {
    				if(contentview.getVisibility()==View.VISIBLE)
    					contentview.setVisibility(View.INVISIBLE);
    				else
    					contentview.setVisibility(View.VISIBLE);
    				//slp.removeView();
    				return;
    			}
        		if(contentview.getVisibility()!=View.VISIBLE) contentview.setVisibility(View.VISIBLE);
        		if(webSingleholder.getVisibility()!=View.VISIBLE) webSingleholder.setVisibility(View.VISIBLE);
        		
        		//adaptively remove contentview?
        		
                if(a.opt.getPeruseBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
                	webcontentlist.SwitchingSides();
        		if(webcontentlist.getPrimaryContentSize()!=CachedBBSize)//here
        			webcontentlist.setPrimaryContentSize(CachedBBSize,true);
        		
        		if(someView2!=slp) {
        			if(someView2!=null) someView2.removeView(contentview);
        			slp.addView(contentview);
        		}
        	}else {
        		if(contentview.getVisibility()!=View.VISIBLE) contentview.setVisibility(View.VISIBLE);
        		if(webSingleholder.getVisibility()!=View.VISIBLE) webSingleholder.setVisibility(View.VISIBLE);
        		
        		if(a.opt.getBottombarOnBottom() ^ (webcontentlist.getChildAt(0).getId()!=R.id.bottombar2))
                	webcontentlist.SwitchingSides();
        		if(webcontentlist.getPrimaryContentSize()!=a.CachedBBSize)
        			webcontentlist.setPrimaryContentSize(a.CachedBBSize,true);
                
        		if(someView2!=a.main) {
        			if(someView2!=null) someView2.removeView(contentview);
        			a.main.addView(contentview);
        		}
        	}
			//a.showT(pos+":"+lastClickedPos);
        	lastClickedPos = pos;
        	
        	setCurrentDis(currentDictionary, lastClickedPos);
        	
        	currentDictionary.renderContentAt(desiredScale,adapter_idx,mWebView,lastClickedPos);
			
			currentKeyText = currentDisplaying;
			String key = currentKeyText;
			
				int pos1 = currentDictionary.currentPos;
				while(pos1-1>=0 && currentDictionary.getEntryAt(pos1-1).equals(key)) {
					pos1--;
				}
				pos1 = currentDictionary.currentPos - pos1;
				if(pos1>0) {
					for(int i=0;i<pos1;i++)
						key += "\n";
				}
				
			//voyager[SelectedV*3+2]=pos;
			a.decorateContentviewByKey(favoriteBtn,key);
			//a.showT(currentDictionary.currentDisplaying);
		}
        
		@Override
		public void onClick(View v) {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
        	switch(v.getId()) {
        		case R.id.del:
        			int id = (int) v.getTag();
        			//currentDictionary.getCon().insert(id)
        			currentDictionary.getCon().prepareContain();
        			int strid = R.string.bmAdded;
        			if(currentDictionary.con.contains(id)) {
        				strid = R.string.bookmarkup;
        			}
        			if(currentDictionary.con.insertUpdate(id)!=-1) {
            			int BKHistroryVagranter = a.opt.getInt("bkHVgrt",-1);
            			BKHistroryVagranter = (BKHistroryVagranter+1)%20;
            			String rec = currentDictionary.f().getAbsolutePath()+"/?Pos="+id;
            			a.opt.putter()//.putString("bkmk", rec)
		                			.putString("bkh"+BKHistroryVagranter, rec)
		                			.putInt("bkHVgrt", BKHistroryVagranter)
		                			.commit();
            			if(ToD) {
	        				pullBookMarks();
	            			bookMarkAdapter.notifyDataSetChanged();
            			}
            			a.showX(strid,0);
            		}
            		else
            			a.showT("添加失败,数据库出错...",0);
    			break;
        	}
        }
        
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	avoyager.clear();
			click(position,false);
			cvpolicy=true;
		}
		
    }
	
    
	public boolean cvpolicy=true;
	int SelectedV;
	final static int headerblue=0xFF2b4381;
	@Override
	public void onClick(View v) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		//a.showT(v.getId()+"asdasd"+android.R.id.home);
		switch(v.getId()) {
			case R.id.toolbar_title:
			case R.id.cover:
				ucc.setInvoker(currentDictionary);
				ucc.onClick(v);
			break;
			case R.id.action0:
				contentview.setVisibility(View.VISIBLE);
			break;
			case -1:
				a.onKeyDown(KeyEvent.KEYCODE_BACK, a.BackEvent);
			break;
			case R.id.ivDeleteText:
				etSearch.setText(null);
				ivDeleteText.setVisibility(View.GONE);
			break;
			case R.id.valve0:
				if(ToL=!ToL) {
					intenToLeft.setBackgroundResource(R.drawable.toleft);
        			bottombar2.setBackgroundColor(bottombar2BaseColor);
				}else {
					intenToLeft.setBackgroundResource(R.drawable.upward);	
					mlp.removeView(contentview);
	        		bottombar2.setBackgroundColor(a.opt.getInDarkMode()?ColorUtils.blendARGB(a.MainBackground,Color.BLACK,a.ColorMultiplier_Wiget):a.MainBackground);
				}
				a.opt.setPerUseToL(ToL);
				a.opt.putFirstFlag();
			break;
			case R.id.valve1:
				if(ToR=!ToR) {
					intenToRight.setBackgroundResource(R.drawable.toright);
	        		bottombar2.setBackgroundColor(bottombar2BaseColor);
				}else {
					intenToRight.setBackgroundResource(R.drawable.downward);
					slp.removeView(contentview);
	        		bottombar2.setBackgroundColor(a.opt.getInDarkMode()?ColorUtils.blendARGB(a.MainBackground,Color.BLACK,a.ColorMultiplier_Wiget):a.MainBackground);
				}
				a.opt.setPerUseToR(ToR);
				a.opt.putFirstFlag();
			break;
			case R.id.valve2:
				if(ToD=!ToD) {
					intenToDown.setBackgroundResource(R.drawable.stardn);
					intenToLeft.setVisibility(View.VISIBLE);
					pullBookMarks();
    				bookMarkAdapter.notifyDataSetChanged();
    				if(contentview.getParent()==slp)
    					contentview.setVisibility(View.INVISIBLE);
				}else {
    				bookMarkAdapter.notifyDataSetChanged();
    				if((System.currentTimeMillis()-lastswicthtime>200) && currentDictionary!=null) {
    					//a.showT("saved "+lv2.getFirstVisiblePosition());
    					currentDictionary.bmCBI=lv2.getFirstVisiblePosition();
    					currentDictionary.bmCCI=bookMarkAdapter.lastClickedPos;
    				}
					intenToDown.setBackgroundResource(R.drawable.stardn1);
					intenToLeft.setVisibility(View.GONE);
				}
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
				ada2.notifyDataSetChanged();
			break;
			default:
				((MainActivityUIBase)getActivity()).onClick(v);
			break;
		}
	}


	@Override
	public boolean onLongClick(View v) {
		return ((MainActivityUIBase)getActivity()).onLongClick(v);
	}
	
	
	
	long lastswicthtime;
	public DisplayMetrics dm;
	public IMPageSlider IMPageCover;
	
	private void pullBookMarks() {
		if(cr!=null)
			cr.close();
		currentDictionary.getCon();
		bookmarks.clear();
		cr = currentDictionary.con.getDB().query("t1", null,null,null,null,null,"path");
		bookmarks_size=cr.getCount();
		//while(cr.moveToNext()){
		//	bookmarks.add(0,cr.getInt(0));
		//}
		//cr.close();
		if(currentDictionary!=null)
		if(ToD) {
			//a.showT(currentDictionary._Dictionary_fName+" "+currentDictionary.bmCBI);
			lv2.post(new Runnable() {
				@Override
				public void run() {
					if(currentDictionary!=null) {
						lv2.setSelection(currentDictionary.bmCBI);
					}
				}
				
			});
			bookMarkAdapter.lastClickedPos=currentDictionary.bmCCI;
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem m) {
		boolean longclick=false;
		if(longclick) return false;
		return onMenuItemClickmy(m,true);
	}

	public boolean onMenuItemClickmy(MenuItem m,boolean fromUser) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		switch(m.getItemId()){
			case R.id.toolbar_action1:
				tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
			break;
			case R.id.toolbar_action2:
				if(fromUser)showBA=!showBA;
				if(showBA) {
					m.setTitle(m.getTitle()+" √");
					ada2.notifyDataSetChanged();
				}else {
					m.setTitle(m.getTitle().subSequence(0, m.getTitle().length()-2));
					ada2.notifyDataSetChanged();
				}
				a.opt.setShowBA(showBA);
				a.opt.putFirstFlag();
			break;
			case R.id.toolbar_action3:
				if(fromUser)showBD=!showBD;
				if(showBD) {
					m.setTitle(m.getTitle()+" √");
					if(ToD)
						bookMarkAdapter.notifyDataSetChanged();
				}else {
					if(ToD)
						bookMarkAdapter.notifyDataSetChanged();
					m.setTitle(m.getTitle().subSequence(0, m.getTitle().length()-2));
				}
				a.opt.setShowBD(showBD);
				a.opt.putFirstFlag();
			break;
			case R.id.toolbar_action4:
				if(fromUser)ForceSearch=!ForceSearch;
				if(ForceSearch) {
					m.setTitle(m.getTitle()+" √");
				}else {
					m.setTitle(m.getTitle().subSequence(0, m.getTitle().length()-2));
				}
				a.opt.setForceSearch(ForceSearch);
				a.opt.putFirstFlag();
			break;
			case R.id.toolbar_action5:
				if(fromUser)showFastScroll=!showFastScroll;
				if(showFastScroll) {
					m.setTitle(m.getTitle()+" √");
					lv1.setFastScrollEnabled(true);
					lv2.setFastScrollEnabled(true);
				}else {
					m.setTitle(m.getTitle().subSequence(0, m.getTitle().length()-2));
					lv1.setFastScrollEnabled(false);
					lv2.setFastScrollEnabled(false);
				}
				a.opt.setShowFScroll(showFastScroll);
				a.opt.putFirstFlag();
			break;
			case R.id.toolbar_action6:
				etSearch.setText(TextToSearch);
			break;
		}
		return false;
	}

	
	public int currentPos;
	String currentDisplaying;
	int expectedPos=-1;
	int expectedPosX;
	ArrayList<myCpr<String,Integer>> History = new ArrayList<myCpr<String,Integer>>();
	
	HashMap<Integer,MJavascriptInterface> ImageHistory = new HashMap<>();
	int HistoryVagranter=-1;

	boolean isJumping = false;
	public int bottombar2BaseColor=0xff8f8f8f;
    void setCurrentDis(mdict invocker, int idx,int...flag) {
		currentPos = idx;
		currentDisplaying = invocker.getEntryAt(currentPos);
    	toolbar_title.setText(new StringBuilder(currentDisplaying.trim()).append(" - ").append(invocker._Dictionary_fName).toString());

		if(flag==null || flag.length==0) {//书签跳转等等
			History.add(++HistoryVagranter,new myCpr<>(String.valueOf(idx),expectedPos));
			for(int i=History.size()-1;i>=HistoryVagranter+1;i--)
				History.remove(i);
		}else {//回溯 或 前瞻， 不改变历史
			//取回
        	MJavascriptInterface js = ImageHistory.get(HistoryVagranter);
        	if(js!=null)
        	mWebView.removeJavascriptInterface("imagelistener");
		    mWebView.addJavascriptInterface(js, "imagelistener");
		}
	}





	
}

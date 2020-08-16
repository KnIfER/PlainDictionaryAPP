package com.knziha.plod.PlainDict;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentTransaction;

import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_txt;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.widgets.SplitView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * 多实例浮动搜索<br/>
 * Multi-Instance Float Activity that adheres to 3rd party intent invokers，<br/>
 * 			and that can be launched by android text process protocol or colordict intents.<br/>
 * Created by KnIfER on 2018
 */
public class FloatSearchActivity extends MainActivityUIBase {
	ViewGroup mainfv;

	boolean FVDOCKED=false;
	int FVW,FVH,FVTX,FVTY,FVW_UNDOCKED,FVH_UNDOCKED;
	final static int FVMINWIDTH=133;
	final static int FVMINHEIGHT=50;

	protected float _50_;
	public static ArrayList<PlaceHolder> CosyChair = new ArrayList<>();
	public static ArrayList<PlaceHolder> CosySofa = new ArrayList<>();
	public static ArrayList<PlaceHolder> HdnCmfrt = new ArrayList<>();
	public static ArrayList<PlaceHolder> mCosyChair;
	public static ArrayList<PlaceHolder> mCosySofa;
	public static ArrayList<PlaceHolder> mHdnCmfrt;
	private String Current0SearchText;
	private boolean fullScreen;
	private boolean hideNavigation;
	private SplitView sp_main;
	protected boolean this_instanceof_FloarActivitySearch;
	private MenuItem iItem_FolderAll;
	private MenuItem iItem_InPageSearch;
	
	@Override
	ArrayList<PlaceHolder> getLazyCC() {
		return mCosyChair;
	}

	@Override
	ArrayList<PlaceHolder> getLazyCS() {
		return mCosySofa;
	}

	@Override
	ArrayList<PlaceHolder> getLazyHC() {
		return mHdnCmfrt;
	}

	@Override
	public void onBackPressed() {
		CMN.Log("onBackPressed!!!", DBrowser);
		if(PDICMainAppOptions.getUseBackKeyClearWebViewFocus() && checkWebSelection()){
			return;
		}
		else if(DetachClickTranslator()){
		
		}
		else if(DBrowser != null){
			if(DBrowser.try_goBack()!=0)
				return;
			//File newFavor = DBrowser.items.get(DBrowser.lastChecked);
			//xxx
//			if(!(DBrowser instanceof DHBroswer))
//				if(!newFavor.equals(new File(favoriteCon.pathName))) {//或许需要重载收藏夹
//					favoriteCon.close();
//					favoriteCon = new LexicalDBHelper(this, newFavor);
//					String name = new File(favoriteCon.pathName).getName();
//					//opt.putString("currFavoriteDBName", opt.currFavoriteDBName=);
//					opt.putCurrFavoriteDBName(favorTag+name.substring(0,name.length()-4));
//					show(R.string.currFavor, DBrowser.boli(newFavor.getName()));
//				}
			if(contentview.getParent()==main){
			
			}
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction().remove(DBrowser);
			transaction.commit();
			
			//getSupportFragmentManager().popBackStack();
			
			//main.removeView(DBroswer.getView());
			//DBroswer.onDetach();
			//todo 增设选项
			if(!TextUtils.isEmpty(DBrowser.currentDisplaying)) {
				//if(!opt.getBrowser_AffectInstant()) etSearch.removeTextChangedListener(tw1);
				//etSearch.setText(DBrowser.currentDisplaying);
				//ivDeleteText.setVisibility(View.VISIBLE);
				//if(!opt.getBrowser_AffectInstant()) etSearch.addTextChangedListener(tw1);
			}
			DBrowser = null;
		} else {
			if(this_instanceof_FloarActivitySearch && PDICMainAppOptions.getFloatClickHideToBackground()){
				moveTaskToBack(false);
				return;
			}
			super.onBackPressed();
		}
	}

	@Override
	boolean isContentViewAttached() {
		return webcontentlist.getVisibility()==View.VISIBLE;
	}
	
	@Override
	public void DetachContentView(boolean leaving) {
		if(DBrowser!=null){
			ViewGroup sp = (ViewGroup) sp_main.getParent();
			if(sp!=main){
				if(sp!=null){
					sp.removeView(sp_main);
				}
				main.addView(sp_main, 2);
			}
		}
	}
	
	@Override
	public boolean isContentViewAttachedForDB() {
		return sp_main.getParent()==root;
	}
	
	@Override
	public void AttachContentViewForDB() {
		//todo preserve context
		ViewGroup somp = (ViewGroup) sp_main.getParent();
		if(somp!=root){
			if(somp!=null) somp.removeView(sp_main);
			root.addView(sp_main);
		}
	}
	
	private int touch_id;

	@Override
	public void NotifyComboRes(int size) {
		if(opt.getNotifyComboRes()) {
			float fval = 0.5f;
			if(bIsFirstLaunch||bWantsSelection) {
				fval=1f;
			}
			String val = recCom.allWebs&&isContentViewAttached()?"回车以搜索网络词典！":getResources().getString(R.string.cbflowersnstr,opt.getLastPlanName(LastPlanName),md.size(),size);
			showTopSnack(main_succinct, val, fval, -1, -1, 0);
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(systemIntialized && hasFocus){
			fix_full_screen(getWindow().getDecorView());
			if(CMN.FloatBackground != MainBackground || CMN.GlobalPageBackground!=GlobalPageBackground ) {
				IMPageCover.setTag(false);
				if(PeruseView!=null) PeruseView.IMPageCover.setTag(false);
				GlobalPageBackground=CMN.GlobalPageBackground;
				MainBackground=CMN.FloatBackground;
				refreshUIColors();
			}
			checkFlags();
			//file-based UI-less command tool
		}
	}

	@Override
	public void fix_full_screen(@Nullable View decorView) {
		if(decorView==null) decorView=getWindow().getDecorView();
		fix_full_screen_global(decorView, fullScreen, hideNavigation);
	}

	@Override
	void switch_dark_mode(boolean val) {
		if(Build.VERSION.SDK_INT<29){
			GlobalOptions.isDark = false;
		}else{
			GlobalOptions.isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
		}
		opt.setInDarkMode(val);
		changeToDarkMode();
	}

	@Override
	public void animateUIColorChanges() {
		fix_pw_color();
		fix_dm_color();
		refreshUIColors();
	}

	@Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
        // Checks the orientation of the screen

        if(chooseDFragment!=null && chooseDFragment.get()!=null) {
			chooseDFragment.clear();
			chooseDFragment=null;
        }
    	if(mainfv.getTranslationY()<0)
    		mainfv.setTranslationY(0);
    	if(mainfv.getTranslationX()<0)
    		mainfv.setTranslationX(0);
    	if(newConfig.screenHeightDp!=Configuration.SCREEN_WIDTH_DP_UNDEFINED)
    	if(mainfv.getTranslationY()>dm.heightPixels-_50_)
    		mainfv.setTranslationY(dm.heightPixels-_50_);
    	if(newConfig.screenWidthDp!=Configuration.SCREEN_WIDTH_DP_UNDEFINED)
    	if(mainfv.getTranslationX()>dm.widthPixels-_50_)
    		mainfv.setTranslationX(dm.widthPixels-_50_);
    	
		if(FVDOCKED) {
        	ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
			lpmy.width=dm.widthPixels-(DockerMarginR+DockerMarginL);
			lpmy.height=(int) (dm.heightPixels-mainfv.getTranslationY())-(DockerMarginB+DockerMarginT);
    		mainfv.setLayoutParams(lpmy);
		}
		GlobalOptions.density = dm.density;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		cbar_key=2;
		defbarcustpos=3;
    	long cur = System.currentTimeMillis();
		if(getClass()==FloatSearchActivity.class) {
			boolean frequentLaunch = cur-CMN.FloatLastInvokerTime<524;
			if(false)
			if(PDICMainAppOptions.getForceFloatSingletonSearch(PDICMainAppOptions.getFourthFlag(this))) {
				Intent thisIntent = getIntent();
				startActivity((thisIntent==null?new Intent():new Intent(getIntent()))
						.setClass(this, FloatActivitySearch.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));//, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
				overridePendingTransition(R.anim.abc_popup_enter, R.anim.abc_popup_exit);
				shunt = true;
			} else if(frequentLaunch) {
				shunt = true;
			}
		}
		super.onCreate(savedInstanceState);
		if(shunt) {
			finish();
			return;
		}
    	CMN.FloatLastInvokerTime=cur;
        bShowLoadErr=false;
		//tc
		tw1 = new TextWatcher() {
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				if(SU.isNotEmpty(cs)) {
					etSearch_ToToolbarMode(3);
					//webcontentlist.setVisibility(View.INVISIBLE);
					if(!bWantsSelection) {
						webholder.removeAllViews();
					}
					if(checkDicts()) {
						if(isCombinedSearching){
							execBunchSearch(cs);
						} else {
							execSingleSearch(cs, count);
						}
					}
				} else {
					if(PDICMainAppOptions.getSimpleMode() && currentDictionary!=null && mdict.class.equals(currentDictionary.getClass()))
						adaptermy.notifyDataSetChanged();
					lv2.setVisibility(View.INVISIBLE);
				}
			}
		
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		
			public void afterTextChanged(Editable s) {
				//if (s.length() == 0) ivDeleteText.setVisibility(View.GONE);
				//else  ivDeleteText.setVisibility(View.VISIBLE);
				if (s.length() != 0) ivDeleteText.setVisibility(View.VISIBLE);
			}
		};
		
    	overridePendingTransition(R.anim.abc_popup_enter, R.anim.abc_popup_enter);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = getIntent();
		fullScreen = intent==null? PDICMainAppOptions.getFloatFullScreen():intent.getBooleanExtra(EXTRA_FULLSCREEN, PDICMainAppOptions.getFloatFullScreen());
		if(fullScreen){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		hideNavigation = intent==null? PDICMainAppOptions.getFloatHideNavigation():intent.getBooleanExtra(EXTRA_HIDE_NAVIGATION, PDICMainAppOptions.getFloatHideNavigation());
		if(hideNavigation) {
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LOW_PROFILE
					| View.SYSTEM_UI_FLAG_IMMERSIVE;
			if(fullScreen) uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
		
        setContentView(R.layout.float_main);
	
		root = findViewById(R.id.root);
		mainfv = root.findViewById(R.id.main);
		toolbar = mainfv.findViewById(R.id.toolbar);
	
		main_succinct = mainfv.findViewById(R.id.mainframe);
		lv = main_succinct.findViewById(R.id.main_list);
		lv2 = main_succinct.findViewById(R.id.sub_list);
		webcontentlist = main_succinct.findViewById(R.id.webcontentlister);
		
		//todo opt
		PageSlider = webcontentlist.findViewById(R.id.PageSlider);
		main_progress_bar = PageSlider.findViewById(R.id.main_progress_bar);
	
		IMPageCover = findViewById(R.id.IMPageCover);
		bottombar2 = (ViewGroup) webcontentlist.getChildAt(1);
	
		mainF = (ViewGroup) root.getChildAt(1);
		
		_50_= (FVMINHEIGHT*dm.density);
        wm = getWindowManager();
	
		contentview = PageSlider;
		sp_main=webcontentlist;

		FVDOCKED=opt.getFVDocked();
		//showT("FVDOCKED"+FVDOCKED);
		SharedPreferences defaultReader = opt.defaultReader;
		FVH= defaultReader.getInt("FVH",(int) (500*dm.density));
		FVW= defaultReader.getInt("FVW",dm.widthPixels);
		FVH_UNDOCKED= defaultReader.getInt("UDFVH",-1);
		FVW_UNDOCKED= defaultReader.getInt("UDFVW",-1);
		FVTX= Math.min(Math.max(defaultReader.getInt("FVTX",0), 0), (int) (dm.widthPixels-_50_));
		FVTY= Math.min(Math.max(defaultReader.getInt("FVTY",(int) (dm.heightPixels-500*dm.density)), 0), (int) (dm.heightPixels-_50_));
		
		mainfv.setTranslationY(FVTY);
		mainfv.setTranslationX(FVTX);
	
		toolbar.inflateMenu(R.menu.float_menu);
		AllMenus = (MenuBuilder) toolbar.getMenu();
		iItem_FolderAll = AllMenus.findItem(R.id.toolbar_action0);
		iItem_InPageSearch = AllMenus.findItem(R.id.toolbar_action5);
		
		hdl = new MyHandler(this);
		checkLog(savedInstanceState);
    }
   
    private void setDocked(boolean docked) {
    	LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) main_succinct.getLayoutParams();
		if(docked) {
			lp.setMargins(0, 0, 0, 0);
		} else {
	    	int margin = (int) (2*dm.density);
			int margin2 = (int) (1*dm.density);
			lp.setMargins(margin2, 0, margin2, margin);
		}
		main_succinct.setLayoutParams(lp);
	}

	@Override
	protected void onDestroy(){
		if(systemIntialized) {
			dumpSettings();
			root.getViewTreeObserver().removeOnGlobalLayoutListener(keyObserver);
			keyObserver=null;
		}
		super.onDestroy();
    }


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//showT("asdasd"+event);
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN: {
				if (opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed() && opt.isAudioPlaying) {
					if (!opt.isAudioActuallyPlaying)
						transitAAdjustment();
					break;
				}
				if (opt.getUseVolumeBtn()) {
					boolean toHighlight=MainPageSearchbar!=null && PDICMainAppOptions.getInPageSearchUseAudioKey() && MainPageSearchbar.getParent()!=null;
					if (webcontentlist.getVisibility()==View.VISIBLE) {
						if(toHighlight) onIdClick(null, R.id.recess);
						else bottombar2.findViewById(R.id.browser_widget11).performClick();
						return true;
					}
				}
			} break;
			case KeyEvent.KEYCODE_VOLUME_UP: {
				if (opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed() && opt.isAudioPlaying) {
					if (!opt.isAudioActuallyPlaying)
						transitAAdjustment();
					break;
				}
				if (opt.getUseVolumeBtn()) {
					boolean toHighlight=MainPageSearchbar!=null && PDICMainAppOptions.getInPageSearchUseAudioKey() && MainPageSearchbar.getParent()!=null;
					if (webcontentlist.getVisibility()==View.VISIBLE) {
						if(toHighlight) onIdClick(null, R.id.forward);
						else bottombar2.findViewById(R.id.browser_widget10).performClick();
						return true;
					}
				}
			} break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void scanSettings(){
		LastPlanName = "FltPlanName";
		LastMdFn = "FltMdFn";
		super.scanSettings();
		CMN.FloatBackground = MainBackground = opt.getFloatBackground();
		isCombinedSearching = opt.isFloatCombinedSearching();
	}

    View IMPageCover;
	private OnGlobalLayoutListener keyObserver;
	@Override
    protected void further_loading(final Bundle savedInstanceState) {
        CachedBBSize=opt.getFloatBottombarSize((int) getResources().getDimension(R.dimen._bottombarheight_));
    	super.further_loading(savedInstanceState);

        main = main_succinct;
		sp_main.scrollbar2guard = mBar;

		if(opt.getInFloatPageSearchVisible())
			toggleInPageSearch(false);
    	
        lv.setAdapter(adaptermy = new ListViewAdapter(webSingleholder));
        lv2.setAdapter(adaptermy2 = new ListViewAdapter2(webholder));

			String keytmp = processIntent(getIntent());
	        etSearch.addTextChangedListener(tw1);
	        bWantsSelection=true;
	        if(keytmp!=null)
	    		tw1.onTextChanged(keytmp, 0, 0, 0);

		//ea
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
				if(d!=null)
					return true;
				String key = String.valueOf(etSearch.getText()).trim();
				if(key.length()>0) Current0SearchText=key;
				bIsFirstLaunch=true;
				tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
			}
			return true;
		});
		
        //manifestTV = (TextViewmy) findViewById(R.id.MANITV);
        //manifestTV.doit();
		
        //mainfv.getBackground().setTint(FloatBackground);
        //IMPageCover.getBackground().setTint(FloatBackground);
        mainfv.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
		
        //键盘监听器
        root.getViewTreeObserver().addOnGlobalLayoutListener(keyObserver=new OnGlobalLayoutListener(){
			boolean keyBoardFlipper=false;
        	@Override
			public void onGlobalLayout() {
				//showT("onGlobalLayout");
        		int kb_height=isKeyboardShown(root);
        		if(keyBoardFlipper) {
        			if(kb_height<=0){
						keyBoardFlipper=false;
						//showT("onGlobalLayout_kn_hide");
					}
        		}else {
					if(kb_height>0) {
						keyBoardFlipper=true;
						//showT("onGlobalLayout_isKeyboardShown");
						FrameLayout.LayoutParams  lpmy = (android.widget.FrameLayout.LayoutParams) mainfv.getLayoutParams();
						wm.getDefaultDisplay().getMetrics(dm);
						if(mainfv.getTranslationY()>dm.heightPixels - kb_height - 50*dm.density) {
							int newTransY = (int) (dm.heightPixels - kb_height - 100*dm.density);
							mainfv.setTranslationY(newTransY);
							if(FVDOCKED) {
								lpmy.height=dm.heightPixels-newTransY-(DockerMarginB+DockerMarginT);
								mainfv.setLayoutParams(lpmy);
							}
							//showT("sdjusted"+dm.heightPixels);
						}
					}
        		}
			}});

		GestureDetector mGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
			public boolean onDoubleTap(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if (true && touch_id!=R.id.move0) {
					exit();
					return true;
				}
				return super.onSingleTapUp(e);
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (false) {
					exit();
					return true;
				}
				return super.onSingleTapConfirmed(e);
			}
		});

		OnTouchListener Toucher = new OnTouchListener(){
        	float lastX;
        	float lastY;
        	boolean wantsMaximize=false;
        	boolean wantedMaximize=false;
        	float DedockTheta;
        	float DedockAcc;
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				DedockTheta=_50_/2;
				touch_id=v.getId();
				mGestureDetector.onTouchEvent(e);
				ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				switch(e.getAction()){
					case MotionEvent.ACTION_DOWN:{
						lastX = e.getRawX();
						lastY = e.getRawY();
						DedockAcc=0;
					} break;
					case MotionEvent.ACTION_MOVE:{
						int dy = (int) (e.getRawY() - lastY);
						int dx = (int) (e.getRawX() - lastX);
						boolean bProceed=true;
						boolean MOT=false,MOB=false,MOL=false,MOR=false;
						wantsMaximize=false;
						if (touch_id == R.id.move0) {
							MOT = true;
							if (FVDOCKED) {//解Dock
								DedockAcc += dx;
							}
							if (DedockAcc > DedockTheta) {
								if (FVDOCKED)
									//if (bREMUDSIZE) {
									if (FVW_UNDOCKED != -1 && FVH_UNDOCKED != -1) {
										lpmy.width = FVW_UNDOCKED;
										lpmy.height = FVH_UNDOCKED;
										mainfv.setLayoutParams(lpmy);
									}
								opt.setFVDocked(FVDOCKED = false);
							}

							if (!FVDOCKED) {//未停靠
								bProceed = false;
								mainfv.setTranslationY(Math.min(dm.heightPixels - _50_, Math.max(mainfv.getTranslationY() + dy, 0)));
								mainfv.setTranslationX(Math.min(dm.widthPixels - _50_, Math.max(mainfv.getTranslationX() + dx + DedockAcc, 0)));//应用累积项
								DedockAcc = 0;
								setDocked(false);
								if (mainfv.getTranslationX() <= 1.45) {
									wantsMaximize = true;
									if (!wantedMaximize) {
										lpmy.width = (int) (lpmy.width + _50_);
										lpmy.height = (int) (lpmy.height + _50_);
										mainfv.setLayoutParams(lpmy);
										wantedMaximize = true;
									}
								} else if (wantedMaximize) {
									lpmy.width = (int) (lpmy.width - _50_);
									lpmy.height = (int) (lpmy.height - _50_);
									setDocked(true);
									mainfv.setLayoutParams(lpmy);
									wantedMaximize = false;
								}
							}
						}
						if(bProceed){
							if(MOT) {

							}else {
								if(lastY<=mainfv.getTranslationY())
									MOT=true;
								if(lastY>=mainfv.getTranslationY()+lpmy.height)
									MOB=true;
								if(lastX<=mainfv.getTranslationX())
									MOL=true;
								if(lastX>=mainfv.getTranslationX()+lpmy.width)
									MOR=true;
							}

							if(MOT) {//move on the top
								if(lpmy.height-dy<=_50_ && dy>0) {//size trim
									dy=(int) (lpmy.height-_50_);
								}
								if(lpmy.height-dy>dm.heightPixels) dy=0;
								int newTransY = (int) (mainfv.getTranslationY()+dy);
								lpmy.height=Math.min(lpmy.height-dy, root.getHeight()-newTransY-(DockerMarginB+DockerMarginT));
								mainfv.setLayoutParams(lpmy);

								//int newTop = (int) (mainfv.getTop() + dy);
								mainfv.setTranslationY(newTransY);
							} else if(MOB) {//move on the bottom
								if(lpmy.height+dy<=_50_ && dy<0) {//size trim
									dy=(int) (_50_-lpmy.height);
								}
								lpmy.height=lpmy.height+dy;
								mainfv.setLayoutParams(lpmy);
							}

							if(MOL){//move on the left
								if(lpmy.width-dx<=FVMINWIDTH*dm.density) {//size trim
									dx=(int) (lpmy.width-FVMINWIDTH*dm.density);
								}
								int newTransX = (int) (mainfv.getTranslationX()+dx);
								lpmy.width=Math.min(lpmy.width-dx, dm.widthPixels-newTransX-(DockerMarginR+DockerMarginL));
								mainfv.setLayoutParams(lpmy);
								mainfv.setTranslationX(newTransX);
							}else if(MOR){//move on the right
								if(lpmy.width+dx<=FVMINWIDTH*dm.density) {//size trim
									dx=(int) (FVMINWIDTH*dm.density-lpmy.width);
								}
								lpmy.width=lpmy.width+dx;
								mainfv.setLayoutParams(lpmy);
							}
						}
						//mainfv.setBottom(dm.heightPixels);
						//ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
						//lpmy.height=newTop;
						//mainfv.setLayoutParams(lpmy);
						//mainfv.postInvalidate();
						lastX = e.getRawX();
						lastY = e.getRawY();
					} break;
					case MotionEvent.ACTION_UP:{
						if(wantsMaximize) {
							FVW_UNDOCKED=(int) (lpmy.width-_50_);
							FVH_UNDOCKED=(int) (lpmy.height-_50_);
							lpmy.width=dm.widthPixels-(DockerMarginR+DockerMarginL);
							lpmy.height=(int) (root.getHeight()-mainfv.getTranslationY()-(DockerMarginB+DockerMarginT));
							mainfv.setTranslationX(0);
							mainfv.setLayoutParams(lpmy);
							setDocked(true);
							wantsMaximize=false;
							wantedMaximize=false;
							opt.setFVDocked(FVDOCKED=true);
						}
					} break;
					default:
					break;
				}
				return true;
			
		}};
		toolbar.findViewById(R.id.move0).setOnTouchListener(Toucher);
        root.setOnTouchListener(Toucher);

        findViewById(R.id.toolbar_action1).setOnLongClickListener(this);

    	systemIntialized=true;
    	
		File additional_config = new File(opt.pathToMainFolder().append("appsettings.txt").toString());
		if(additional_config.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(additional_config));
				String line;
					while((line=in.readLine())!=null) {
						String[] arr = line.split(":", 2);
						if(arr.length==2) {
							if(arr[0].equals("float window margin")||arr[0].equals("浮动窗体边框")) {
								arr = arr[1].split(" ");
								if(arr.length==4) {
									try {
										MarginLayoutParams lp = (MarginLayoutParams) root.getLayoutParams();
										DockerMarginL = lp.leftMargin=Integer.valueOf(arr[2]);
										DockerMarginR = lp.rightMargin=Integer.valueOf(arr[3]);
										DockerMarginT = lp.topMargin=Integer.valueOf(arr[0]);
										DockerMarginB = lp.bottomMargin=Integer.valueOf(arr[1]);
										root.setLayoutParams(lp);
									} catch (Exception ignored) {}
								}
							}
						}
					}
			} catch (Exception ignored) {}
		}

		ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
		if(!FVDOCKED) {
			lpmy.width=FVW_UNDOCKED;
			lpmy.height=FVH_UNDOCKED;
			setDocked(false);
			mainfv.requestLayout();
		}else {
			Rect rect = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			lpmy.width=dm.widthPixels-(DockerMarginR+DockerMarginL);
			if(hideNavigation){
				getWindowManager().getDefaultDisplay().getRealMetrics(dm);
			}
			lpmy.height=dm.heightPixels-(fullScreen||hideNavigation?0:CMN.getStatusBarHeight(this))-FVTY-rect.top-(DockerMarginB+DockerMarginT);
			mainfv.requestLayout();
		}
        if(!opt.getFloatBottombarOnBottom())
        	webcontentlist.SwitchingSides();

		refreshUIColors();
    }

	protected void exit() {
		finish();
	}

	@Override
	protected View getIMPageCover() {
		return IMPageCover;
	}

	@Override
	protected File getStartupFile(File ConfigFile) {
		File suf = new File(ConfigFile, opt.getLastPlanName(LastPlanName));
		if(!suf.exists()) {
			return super.getStartupFile(ConfigFile);
		}
		return suf;
	}

	static long currMdlTime;
	static String lastLoadedModule;
	static boolean lazyLoaded;
	@Override
	protected void LoadLazySlots(File modulePath, boolean lazyLoad, String moduleName) throws IOException {
		long lm = modulePath.lastModified();
		if(lm==currMdlTime
				&& lazyLoaded==lazyLoad
				&& moduleName.equals(lastLoadedModule)
		){
			mCosyChair=CosyChair;
			mCosySofa=CosySofa;
			mHdnCmfrt=HdnCmfrt;
			filter_count = mCosySofa.size();
			CMN.Log("直接返回！！！", filter_count);
			currentFilter.ensureCapacity(filter_count);
			for (int i = 0; i < filter_count; i++) {
				currentFilter.add(null);
				//CMN.Log(mCosySofa.get(i).name);
			}
			return;
		}
		CMN.Log("LoadLazySlots…");
		mCosyChair=new ArrayList<>();
		mCosySofa=new ArrayList<>();
		mHdnCmfrt=new ArrayList<>();
		AgentApplication app = ((AgentApplication) getApplication());
		ReusableBufferedReader in = new ReusableBufferedReader(new FileReader(modulePath), app.get4kCharBuff(), 4096);
		filter_count=hidden_count=0;
		do_LoadLazySlots(in, mCosyChair);
		CosyChair=mCosyChair;
		CosySofa=mCosySofa;
		HdnCmfrt=mHdnCmfrt;
		currMdlTime=lm;
		lastLoadedModule=moduleName;
		lazyLoaded=lazyLoad;
		app.set4kCharBuff(in.cb);
	}

	String processIntent(Intent intent) {
		String keytmp =	intent.getStringExtra("EXTRA_QUERY");
		if(keytmp==null) {
			String type = intent.getType();
			if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
				keytmp = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
				if(keytmp==null) {
					keytmp = intent.getStringExtra(Intent.EXTRA_TEXT);
				}
			}
		}
		if(keytmp!=null && !PDICMainAppOptions.getHistoryStrategy0()&& PDICMainAppOptions.getHistoryStrategy12()){
			prepareHistroyCon().insertUpdate(keytmp);
		}

		if(fullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
		}
		if(etSearch!=null) {
			etSearch.setText(keytmp);
		}
		return keytmp;
	}

	void refreshUIColors() {
		boolean isHalo=!GlobalOptions.isDark;
		int filteredColor = isHalo?MainBackground: ColorUtils.blendARGB(MainBackground, Color.BLACK, ColorMultiplier_Wiget);
		lv2.setBackgroundColor(AppWhite);
		if(GlobalOptions.isDark)
			mainfv.getBackground().setColorFilter(filteredColor, PorterDuff.Mode.SRC_IN);
		else
			mainfv.getBackground().clearColorFilter();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(filteredColor);
		}

		bottombar2.setBackgroundColor(filteredColor);

		filteredColor = isHalo?GlobalPageBackground:ColorUtils.blendARGB(GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
		WHP.setBackgroundColor(filteredColor);
		webSingleholder.setBackgroundColor(filteredColor);
	}


	private int isKeyboardShown(View rootView) {
		Rect r = new Rect();
		rootView.getWindowVisibleDisplayFrame(r);
		return rootView.getBottom() - r.bottom;// > softKeyboardHeight * CMN.dm_density;
	}
	

	private void dumpSettings(){
		if(systemIntialized) {
			android.view.ViewGroup.LayoutParams lp = mainfv.getLayoutParams();

			opt.setFloatBottombarOnBottom(webcontentlist.multiplier==-1);
			Editor putter = opt.defaultputter();
			putter.putLong("MFF", opt.FirstFlag())//FVDOCKED
			.putInt("FVH",lp.height)
			.putInt("FVW",lp.width)
			.putInt("FVTX",(int) mainfv.getTranslationX())
			.putInt("FVTY",(int) mainfv.getTranslationY())
			.putInt("UDFVW",FVW_UNDOCKED)
			.putInt("UDFVH",FVH_UNDOCKED)
			.putInt("FBBS",webcontentlist.getPrimaryContentSize())//FloatBottombarSize
			.commit();
		}
	}

	public float getPadHoldingCS() {
		//CMN.Log("caculation pad...", mainfv.getTranslationY(),toolbar.getHeight(),contentview.getTop());
		return mainfv.getTranslationY()+toolbar.getHeight()+contentview.getTop();
	}

	private static class MyHandler extends BaseHandler {
		private final WeakReference<Toastable_Activity> activity;
		MyHandler(Toastable_Activity a) {
			this.activity = new WeakReference<>(a);
		}
		@Override
		public void clearActivity() {
			activity.clear();
		}
		@Override
		public void handleMessage(@NonNull Message msg) {
			if(activity.get()==null) return;
			FloatSearchActivity a = ((FloatSearchActivity)activity.get());
			switch (msg.what) {
				case 6657:
					removeMessages(6657);
					a.topsnack.offset+=animatorD;
					if(a.topsnack.offset<0)
						sendEmptyMessage(6657);
					else {
						a.topsnack.offset = 0;
						a.animationSnackOut=true;
						sendEmptyMessageDelayed(6658, a.NextSnackLength);
					}
					a.topsnack.setTranslationY(a.topsnack.offset);
					break;
				case 6658:
					removeMessages(6658);
					if(a.animationSnackOut){
						a.topsnack.offset-=animatorD;
						if(a.topsnack.offset>-(a.topsnack.getHeight()+8*a.dm.density))
							sendEmptyMessage(6658);
						else{
							a.removeSnackView();
							break;
						}
						a.topsnack.setTranslationY(a.topsnack.offset);
					}
				break;
			}
	}}

	@Override
    protected void onResume() {
        super.onResume();
        if(systemIntialized) {
	        if(CMN.FloatBackground != MainBackground) {
	        	MainBackground=CMN.FloatBackground;
                mainfv.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
	        }
        }
    }

    public class ListViewAdapter extends BasicAdapter {
        public ListViewAdapter(ViewGroup webSingleholder)
        {
			this.webviewHolder=webSingleholder;
        }
        @Override
        public int getCount() {
			if(md.size()>0 && currentDictionary!=null) {
				if(PDICMainAppOptions.getSimpleMode()&&etSearch.getText().length()==0 && mdict.class.equals(currentDictionary.getClass()))
					return 0;
				return (int) currentDictionary.getNumberEntries();
			}else{
				return 0;
			}
        }
        @Override
        public View getItem(int position) {
			return null;
			}
        @Override
        public long getItemId(int position) {
          return position;
        }
        Flag mFlag = new Flag();
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
        	PDICMainActivity.ViewHolder vh;
        	String currentKeyText = currentDictionary.getEntryAt(position, mFlag);
	        //String keyText = md.get(adapter_idx).getEntryAt(position);
	        if(convertView!=null){
        		vh=(PDICMainActivity.ViewHolder)convertView.getTag();
        	}else{
        		vh=new PDICMainActivity.ViewHolder(getApplicationContext(), R.layout.listview_item0, null);
        	}
			if( vh.title.getTextColors().getDefaultColor()!=AppBlack) {
				//decorateBackground(vh.itemView);
				vh.title.setTextColor(AppBlack);
			}
            vh.title.setText(currentKeyText);
//            if(mFlag.data!=null)
//                vh.subtitle.setText(Html.fromHtml(currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+ mFlag.data+" ></font >"));
//            else
			vh.subtitle.setText(currentDictionary.getDictionaryName());
        	//convertView.setTag(R.id.position,position);
        	return vh.itemView;
        }

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			userCLick=true;
			lastClickedPosBeforePageTurn=-1;
			onItemClick(pos);
		}

        @Override
        public void onItemClick(int position) {//lv1
        	super.onItemClick(position);
        	ActivedAdapter=this;
        	if(position<-1){
				show(R.string.endendr);
				return;
			}
        	if(position>=getCount()) {
        		lastClickedPos = getCount()-1;
        		show(R.string.endendr);
        		return;
    		}
        	
        	if(webSingleholder.getVisibility()!=View.VISIBLE)webSingleholder.setVisibility(View.VISIBLE);
	    	if(WHP.getVisibility()==View.VISIBLE) {
			    if(webholder.getChildCount()!=0)
			    	webholder.removeAllViews();
			    WHP.setVisibility(View.GONE);
	    	}
        	if(widget14.getVisibility()==View.VISIBLE) {
	        	widget13.setVisibility(View.GONE);
	        	widget14.setVisibility(View.GONE);
        	}

			iItem_InPageSearch.setVisible(true);

        	webcontentlist.setVisibility(View.VISIBLE);
			etSearch_ToToolbarMode(1);
        	//CMN.show("onItemClick"+position);
			ViewGroup someView = currentDictionary.rl;
			if(someView!=null && someView.getParent()!=null)
				((ViewGroup)someView.getParent()).removeView(someView);
			
			webholder.removeAllViews();

        	if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(mainfv.getWindowToken(),0);
				etSearch.clearFocus();
        	}
        	
			currentDictionary.initViewsHolder(FloatSearchActivity.this);
			currentDictionary.mWebView.fromCombined=0;
			webSingleholder.addView(md.get(adapter_idx).rl);
	
			/* 仿效 GoldenDict 返回尽可能多的结果 */
			currentDictionary.renderContentAt(-1,adapter_idx,0,null, getMergedClickPositions(position));
			
			currentKeyText = currentDictionary.getEntryAt(position);
			bWantsSelection=true;

			decorateContentviewByKey(null,currentKeyText);
			if(!(currentDictionary instanceof mdict_txt) && !PDICMainAppOptions.getHistoryStrategy0() && PDICMainAppOptions.getHistoryStrategy6() &&(userCLick || PDICMainAppOptions.getHistoryStrategy8()==0)) {
				prepareHistroyCon().insertUpdate(currentKeyText);
				//CMN.Log("浮动点击1", userCLick);
			}
			userCLick=false;
        }

		@Override
		public int getId() {
			return 1;
		}

		@Override
		public String currentKeyText() {
			return currentDictionary.currentDisplaying;
		}

	}
    
    class ListViewAdapter2 extends  BasicAdapter{
    	int itemId = R.layout.listview_item1;
        //构造函数
		public ListViewAdapter2(ViewGroup vg)
		{
			this.webviewHolder=vg;
		}
        @Override
        public int getCount() {
        	if(combining_search_result==null)
        		return 0;
            return combining_search_result.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
        	PDICMainActivity.ViewHolder vh;

        	CharSequence currentKeyText = combining_search_result.getResAt(position);
	        
	        if(convertView!=null){
	        		vh=(PDICMainActivity.ViewHolder)convertView.getTag();
	        	}else{
					vh=new PDICMainActivity.ViewHolder(getApplicationContext(), itemId, null);
					if(itemId==R.layout.listview_item1)
						vh.subtitle.setTag(vh.itemView.findViewById(R.id.counter));
			}
			if(combining_search_result.dictIdx>=md.size()) return vh.itemView;//不要Crash哇
			if( vh.title.getTextColors().getDefaultColor()!=AppBlack) {
				//decorateBackground(vh.itemView);
				vh.title.setTextColor(AppBlack);
			}
			vh.title.setText(currentKeyText);
            mdict _currentDictionary = md.get(combining_search_result.dictIdx);
//            if(_currentDictionary!=null){
//				if(combining_search_result.mflag.data!=null)
//					vh.subtitle.setText(Html.fromHtml(_currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+combining_search_result.mflag.data+" ></font >"));
//				else
//			}
			vh.subtitle.setText(_currentDictionary.getDictionaryName());

			if(combining_search_result.getClass()==resultRecorderCombined.class)
				((TextView)vh.subtitle.getTag()).setText(((resultRecorderCombined)combining_search_result).count);
			//vh.itemView.setTag(R.id.position,position);
			return vh.itemView;
        }

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			if(checkAllWebs(combining_search_result, view, pos)) return;
			userCLick=true;
			lastClickedPosBeforePageTurn=-1;
			onItemClick(pos);
		}

        @Override
        public void onItemClick(int pos){//lv2
        	super.onItemClick(pos);
        	ActivedAdapter=this;
        	webcontentlist.setVisibility(View.VISIBLE);
			etSearch_ToToolbarMode(1);
        	if(pos<0 ){
				show(R.string.endendr);
				return;
			}
        	if(pos>=getCount()) {
        		lastClickedPos = getCount()-1;
        		show(R.string.endendr);
        		return;
    		}

			iItem_FolderAll.setVisible(true);//折叠
			iItem_InPageSearch.setVisible(true);

        	ActivedAdapter=this;
        	
        	if(WHP.getVisibility()!=View.VISIBLE)WHP.setVisibility(View.VISIBLE);
        	//WHP.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
	    	if(webSingleholder.getVisibility()==View.VISIBLE) {
			    if(webSingleholder.getChildCount()!=0)
			    	webSingleholder.removeAllViews();
			    webSingleholder.setVisibility(View.GONE);
	    	}
	    	
        	if(widget14.getVisibility()!=View.VISIBLE) {
	        	widget13.setVisibility(View.VISIBLE);
	        	widget14.setVisibility(View.VISIBLE);
        	}
        	
        	lastClickedPos = pos;
        	if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(mainfv.getWindowToken(),0);
				etSearch.clearFocus();
        	}

			combining_search_result.renderContentAt(lastClickedPos,FloatSearchActivity.this,this);//webholder

			decorateContentviewByKey(null,currentKeyText = combining_search_result.getResAt(pos).toString());
			if(!PDICMainAppOptions.getHistoryStrategy0() && PDICMainAppOptions.getHistoryStrategy5()) {
				if(userCLick||PDICMainAppOptions.getHistoryStrategy8()==0){
					prepareHistroyCon().insertUpdate(currentKeyText);
					//CMN.Log("浮动点击2", userCLick);
				}
			}
			userCLick=false;
			bWantsSelection=true;
        }

		@Override
		public int getId() {
			return 2;
		}

		@Override
		public String currentKeyText() {
			return currentKeyText;
		}
	};


	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(click_handled_not) {
			int id=v.getId();
			onIdClick(v, id);
		}
	}

	public void onIdClick(View v, int id){
		switch(id) {
			case R.id.toolbar_action1:{
				opt.setFloatCombinedSearching(isCombinedSearching = !isCombinedSearching);
				boolean b = webcontentlist.getVisibility() == View.VISIBLE;
				if(isCombinedSearching){
					if(b) adaptermy2.currentKeyText=null;
					lv.setVisibility(View.VISIBLE);
				} else {
					if(b) adaptermy.currentKeyText=null;
					lv2.setVisibility(View.GONE);
				}
				AllMenus.getItem(0).setIcon(isCombinedSearching?R.drawable.ic_btn_multimode:R.drawable.ic_btn_siglemode);
				if(opt.auto_seach_on_switch)
					tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
			} break;
			//返回
			case R.id.ivBack:{
				if((etSearch_toolbarMode&1)==0) {//search
					//bWantsSelection=true;
					if(etSearch.getText().toString().trim().length()>0) {
						bIsFirstLaunch=true;
						tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
					}
				}else {//back
					webcontentlist.setVisibility(View.GONE);
					bWantsSelection=false;
					if(webSingleholder.getChildCount()!=0) {
						webSingleholder.removeAllViews();
					}
					webholder.removeAllViews();
					etSearch_ToToolbarMode(0);
				}
			} break;
		}
	}

	WeakReference<DictPicker> chooseDFragment;
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:null;
		boolean isLongClicked=mmi==null?false:mmi.isLongClicked;
		/* 长按事件默认不处理，因此长按时默认返回false，且不关闭menu。 */
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		switch (item.getItemId()) {
			case R.id.text_tools:{
				if(isLongClicked) break;
				handleTextTools();
			} return true;
			case R.id.toolbar_action0:{
				if(isLongClicked) break;
				toggleFoldAll();
			} break;
            case R.id.toolbar_action2:{//切换词典
				if(isLongClicked) break;
				showChooseDictDialog(0);
			} break;
            case R.id.toolbar_action3:{//切换分组
				if(isLongClicked) break;
				showChooseSetDialog();
			} break;
            case R.id.toolbar_action4:
				if(isLongClicked) break;
            	String keyword = etSearch.getText().toString().trim();
            	if(prepareHistroyCon().insertUpdate(keyword)>0)
            		showT("已收藏！");
            break;
            case R.id.toolbar_action5:
            	toggleInPageSearch(ret=isLongClicked);
            break;
        }
		if(closeMenu)
			closeIfNoActionView(mmi);
		return ret;
	}

	@Override
	void contentviewAddView(View v, int i) {
		webcontentlist.addView(v, i);
	}

	@Override
	public void invalidAllLists() {
		webSingleholder.removeAllViews();
		webholder.removeAllViews();
		bIsFirstLaunch = true;
		//todo 有时白，fowhy.
		CombinedSearchTask_lastKey = null;
		tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
	}

	@Override
	protected int getVisibleHeight() {
		return root.getHeight();
	}

	@Override
	public PlaceHolder getPlaceHolderAt(int idx) {
		if(idx>=0 && idx<mCosyChair.size())
			return mCosyChair.get(idx);
		return null;
	}

	@Override
	public ArrayList<PlaceHolder> getPlaceHolders() {
		return mCosyChair;
	}

	@Override
	void showChooseDictDialog(int reason) {
		boolean needRefresh=pickTarget!=reason;
		pickTarget=reason;
		DictPicker chooseDialog;
		if(chooseDFragment==null || chooseDFragment.get()==null) {
			chooseDFragment = new WeakReference<>(chooseDialog = new DictPicker(this));
			//chooseDFragment.setStyle(R.style.DialogStyle, 0);//DialogFragment.STYLE_NO_TITLE
			chooseDialog.bShouldCloseAfterChoose=true;
			//chooseDFragment.setCancelable(true);
			//chooseDFragment.setOnViewCreatedListener(new OnViewCreatedListener() {
			//	@Override
			//	public void OnViewCreated(Dialog dialog) {
			//		dialog.setCanceledOnTouchOutside(true);
			//		Window window = dialog.getWindow();
			//	}});
			chooseDialog.width=(int) (dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor));
			chooseDialog.mMaxH=(int) (dm.heightPixels-2*getResources().getDimension(R.dimen.diagMarginVer));
			chooseDialog.height=-2;
		}else
			chooseDialog = chooseDFragment.get();
		chooseDialog.show(getSupportFragmentManager(), "PickDictDialog");

		        /*DidialogHolder = (ViewGroup) findViewById(R.id.dialog_);
            	if(dialogHolder.getVisibility()==View.VISIBLE) {
					dialogHolder.setVisibility(View.GONE);
					break;
				}
				if(!isFragInitiated) {
					FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();
					pickDictDialog = new DialogFragment1(this);
		            transaction.add(R.id.dialog_, pickDictDialog);
		            transaction.commit();
		            isFragInitiated=true;
		            //pickDictDialog.mRecyclerView.scrollToPosition(adapter_idx);
				}
				else//没办法..
					pickDictDialog.refresh();*/
		if(needRefresh) chooseDialog.notifyDataSetChanged();
	}
}
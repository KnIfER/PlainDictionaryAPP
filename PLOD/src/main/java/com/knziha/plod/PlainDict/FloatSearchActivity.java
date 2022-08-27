package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.math.MathUtils;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.widgets.PageSlide;
import com.knziha.plod.widgets.ViewUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import static com.knziha.plod.plaindict.CMN.GlobalPageBackground;
import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_FLOAT_SEARCH;


/**
 * 多实例浮动搜索<br/>
 * Multi-Instance Float Activity that adheres to 3rd party intent invokers，<br/>
 * 			and that can be launched by android text process protocol or colordict intents.<br/>
 * Created by KnIfER, 2018
 */
public class FloatSearchActivity extends MainActivityUIBase {
	ViewGroup mainfv;

	boolean FVDOCKED=false;
	int FVW,FVH,FVTX,FVTY,FVW_UNDOCKED,FVH_UNDOCKED;
	final static int FVMINWIDTH=133;
	final static int FVMINHEIGHT=50;

	protected float _50_;
	private String Current0SearchText;
	private boolean fullScreen;
	private boolean hideNavigation;
	protected boolean this_instanceof_FloarActivitySearch;
	
	ViewGroup.LayoutParams mfv_lp;
	private int barSzBot;
	
	@Override
	protected boolean PerFormBackPrevention(boolean bBackBtn) {
		if (super.PerFormBackPrevention(bBackBtn)) {
			return true;
		}
		if(this_instanceof_FloarActivitySearch && PDICMainAppOptions.getFloatClickHideToBackground()) {
			moveTaskToBack(false);
			return true;
		}
		return false;
	}
	
	private int touch_id;

	@Override
	public void NotifyComboRes(int size) {
		if(PDICMainAppOptions.getNotifyComboRes()) {
			float fval = 0.5f;
			if(bIsFirstLaunch||bWantsSelection) {
				fval=1f;
			}
			String val = recCom.allWebs&&isContentViewAttached()?"回车以搜索网络词典！":getResources().getString(R.string.cbflowersnstr,opt.getLastPlanName(LastPlanName),loadManager.md_size,size);
			showTopSnack(mainframe, val, fval, -1, -1, 0);
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(systemIntialized && hasFocus){
			fix_full_screen(getWindow().getDecorView());
			if((CMN.AppColorChangedFlag&thisActMask)!=0)
			{
				MainBackground = MainAppBackground = opt.getFloatBackground();
				CMN.AppColorChangedFlag &= ~thisActMask;
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
	public void animateUIColorChanges() {
		fix_pw_color();
		fix_dm_color();
		refreshUIColors();
	}
	
	void refreshUIColors() {
		boolean isHalo=!GlobalOptions.isDark;
		MainAppBackground = isHalo?MainBackground: ColorUtils.blendARGB(MainBackground, Color.BLACK, ColorMultiplier_Wiget);
		int filteredColor = MainAppBackground;
		lv.setBackgroundColor(AppWhite);
		lv2.setBackgroundColor(AppWhite);
		//CMN.debug("refreshUIColors!!!", Integer.toHexString(filteredColor));
		mainfv.getBackground().setColorFilter(filteredColor, PorterDuff.Mode.SRC_IN);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(filteredColor);
		}
		contentUIData.bottombar2.setBackgroundColor(filteredColor);
		
		filteredColor = isHalo?GlobalPageBackground:ColorUtils.blendARGB(GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
		weblistHandler.setBackgroundColor(filteredColor);
		webSingleholder.setBackgroundColor(filteredColor);
	}
	
	@Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
        // Checks the orientation of the screen

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
        	ViewGroup.LayoutParams  lpmy = mfv_lp;
			lpmy.width=dm.widthPixels-(DockerMarginR+DockerMarginL);
			lpmy.height=(int) (dm.heightPixels-mainfv.getTranslationY())-(DockerMarginB+DockerMarginT);
    		mainfv.setLayoutParams(lpmy);
		}
		GlobalOptions.density = dm.density;
    }
    
    @SuppressLint("ResourceType")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		cbar_key=2;
		defbarcustpos=3;
		thisActType = ActType.FloatSearch;
		if(getClass()==FloatSearchActivity.class) {
			long cur;
			long FF = PDICMainAppOptions.getFourthFlag(this);
			Intent thisIntent = getIntent();
			if(thisIntent==null) {
				thisIntent = new Intent();
			}
			String act = thisIntent.getAction();
			int reTarget = PLAIN_TARGET_FLOAT_SEARCH;
			opt = new PDICMainAppOptions(this);
			if ("colordict.intent.action.SEARCH".equals(act)) {
				reTarget = opt.getColorDictTarget();
			}
			else if ("android.intent.action.PROCESS_TEXT".equals(act)) {
				reTarget = opt.getTextProcessorTarget();
			}
			if(reTarget!=PLAIN_TARGET_FLOAT_SEARCH) {
				thisIntent = new Intent(Intent.ACTION_MAIN)
						.setClass(this, MainShareActivity.class)
						.putExtras(thisIntent)
						.putExtra("force", reTarget)
				;
				startActivity(thisIntent);
				shunt = true;
			} else if(PDICMainAppOptions.getForceFloatSingletonSearch(FF)) {
				startActivity(new Intent(thisIntent)
						.setClass(this, FloatActivitySearch.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));//, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
				overridePendingTransition(R.anim.abc_popup_enter, R.anim.abc_popup_exit);
				shunt = true;
			} else if((cur=System.currentTimeMillis())-CMN.FloatLastInvokerTime<524) {
				shunt = true;
			} else {
				CMN.FloatLastInvokerTime=cur;
			}
		}
		super.onCreate(savedInstanceState);
		if(shunt) {
			finish();
			return;
		}
        bShowLoadErr=false;
		//tc
		execSearchRunnable = () -> {
			//webcontentlist.setVisibility(View.INVISIBLE);
			if(!bWantsSelection) {
				weblistHandler.removeAllViews();
			}
			if(checkDicts()) {
				if(isCombinedSearching){
					execBatchSearch(search_cs);
				} else {
					execSingleSearch(search_cs, search_count);
				}
			}
		};
    	overridePendingTransition(R.anim.abc_popup_enter, R.anim.abc_popup_enter);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
		|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
	
		dictPicker = new DictPicker(this, null, null, 1);
	
		root = findViewById(R.id.root);
		mainfv = root.findViewById(R.id.main);
		toolbar = mainfv.findViewById(R.id.toolbar);
		appbar = toolbar;
	
		mainframe = mainfv.findViewById(R.id.mainframe);
		lv = mainframe.findViewById(R.id.main_list);
		mlv = (ViewGroup) lv.getParent();
		lv2 = mainframe.findViewById(R.id.sub_list);
		
		
		mainF = (ViewGroup) root.getChildAt(1);
		
		_50_= (FVMINHEIGHT*dm.density);
        wm = getWindowManager();

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
	
		toolbar.inflateMenu(R.xml.menu_float);
		AllMenus = (MenuBuilder) toolbar.getMenu();
		AllMenusStamp = Arrays.asList(AllMenus.getItems().toArray(new MenuItemImpl[AllMenus.size()]));
	
		MainMenu = ViewUtils.MapNumberToMenu(AllMenus, 0, 7, 3, 4, 5, 6);
		SingleContentMenu = ViewUtils.MapNumberToMenu(AllMenus, 0, 7, 3, 4, 5, 6);
		Multi_ContentMenu = ViewUtils.MapNumberToMenu(AllMenus, 0, 1, 7, 3, 2, 4, 5, 6);
		
		PeruseListModeMenu = ViewUtils.findInMenu(MainMenu, R.id.peruseList);
		PeruseListModeMenu.setChecked(opt.getInFloatPeruseMode());
		
		hdl = new MyHandler(this);
		checkLog(savedInstanceState);
    }
   
    private void setDocked(boolean docked) {
    	LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) mainframe.getLayoutParams();
		if(docked) {
			lp.setMargins(0, 0, 0, 0);
		} else {
	    	int margin = (int) (2*dm.density);
			int margin2 = (int) (1*dm.density);
			lp.setMargins(margin2, 0, margin2, margin);
		}
		mainframe.setLayoutParams(lp);
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
	protected void onResume() {
		super.onResume();
		((AgentApplication)getApplication()).handles[1] = hdl;
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
					boolean toHighlight=weblistHandler.pageSchBar !=null && PDICMainAppOptions.schPageNavAudioKey() && weblistHandler.pageSchBar.getParent()!=null;
					if (contentUIData.webcontentlister.getVisibility()==View.VISIBLE) {
						if(toHighlight) onIdClick(null, R.id.recess);
						else contentUIData.browserWidget11.performClick();
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
					boolean toHighlight=weblistHandler.pageSchBar !=null && PDICMainAppOptions.schPageNavAudioKey() && weblistHandler.pageSchBar.getParent()!=null;
					if (contentUIData.webcontentlister.getVisibility()==View.VISIBLE) {
						if(toHighlight) onIdClick(null, R.id.forward);
						else contentUIData.browserWidget10.performClick();
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
		MainBackground = MainAppBackground = opt.getFloatBackground();
		CMN.AppColorChangedFlag &= ~thisActMask;
		isCombinedSearching = opt.isFloatCombinedSearching();
	}

	private OnGlobalLayoutListener keyObserver;
	@Override
    protected void further_loading(final Bundle savedInstanceState) {
		barSzBot=opt.getFloatBottombarSize((int) mResource.getDimension(R.dimen.barSzBot));
    	super.further_loading(savedInstanceState);

        main = mainframe;
		contentUIData.webcontentlister.scrollbar2guard = contentUIData.dragScrollBar;

		if(PDICMainAppOptions.schPageFlt())
			weblistHandler.togSchPage();
    	
        lv.setAdapter(adaptermy = new ListViewAdapter(this, AllMenus, SingleContentMenu));
        lv2.setAdapter(adaptermy2 = new ListViewAdapter2(this, weblistHandler, AllMenus, Multi_ContentMenu, R.layout.listview_item1, 2));

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
				tw1.onTextChanged(etSearch.getText(), -1, -1, 0);
			}
			return true;
		});
		
        //manifestTV = (TextViewmy) findViewById(R.id.MANITV);
        //manifestTV.doit();
		
        //mainfv.getBackground().setTint(FloatBackground);
        //IMPageCover.getBackground().setTint(FloatBackground);
        mainfv.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
		mfv_lp = mainfv.getLayoutParams();
		
        //键盘监听器
        root.getViewTreeObserver().addOnGlobalLayoutListener(keyObserver=new OnGlobalLayoutListener(){
			boolean lastIsKeyBoardShown =false;
			int lastHeight;
        	@Override
			public void onGlobalLayout() {
        		int kb_height=isKeyboardShown(root);
				//showT("onGlobalLayout "+kb_height);
				boolean keyBoardShown = kb_height > 100;
				
				if(keyBoardShown!= lastIsKeyBoardShown) {
					lastIsKeyBoardShown =keyBoardShown;
					wm.getDefaultDisplay().getMetrics(dm);
					int TBH = toolbar.getHeight();
					if(mainfv.getTranslationY()>dm.heightPixels - kb_height - TBH) {
						int newTransY = (int) (dm.heightPixels - kb_height - 2*TBH);
						if(FVDOCKED) {
							mfv_lp.height= WindowVisibleR.height() -newTransY-(DockerMarginB+DockerMarginT)+1000;
							//mainfv.requestLayout();
						}
						mainfv.setTranslationY(newTransY);
						//showT("adjusted"+dm.heightPixels);
					}
				}
		
				int height = mainfv.getHeight();
				if(opt.getAutoAdjustFloatBottomBar() && height!=lastHeight) {
					lastHeight=height;
					barSzBot=(int)Math.max(20*dm.density, Math.min(
							MathUtils.lerp(10*dm.density, 50*dm.density, mfv_lp.height*1.25f/dm.heightPixels)
							, mResource.getDimension(R.dimen.barSzBot)));
					contentUIData.webcontentlister.setPrimaryContentSize(barSzBot,true);
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
				ViewGroup.LayoutParams  lpmy = mfv_lp;
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

		ViewGroup.LayoutParams  lpmy = mfv_lp;
		if(!FVDOCKED) {
			lpmy.width=FVW_UNDOCKED;
			lpmy.height=FVH_UNDOCKED;
			setDocked(false);
			mainfv.requestLayout();
		} else {
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
			contentUIData.webcontentlister.SwitchingSides();

		refreshUIColors();
    }
	
	protected void findFurtherViews() {
		schuiMain = SearchUI.FloatApp.MAIN;
		schuiMainPeruse = schuiMain|SearchUI.Fye.MAIN;
		schuiList = SearchUI.FloatApp.表;
		etSearch = findViewById(R.id.etSearch);
		super.findFurtherViews();
		findViewById(R.id.toolbar_action1).setOnClickListener(this);
		ivDeleteText = toolbar.findViewById(R.id.ivDeleteText);
		ivBack = toolbar.findViewById(R.id.ivBack);
		findViewById(R.id.pad).setOnClickListener(ViewUtils.DummyOnClick);
	}
	
	protected void exit() {
		finish();
	}

	String processIntent(Intent intent) {
		CMN.debug("processIntent", intent);
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
		if(keytmp!=null && !PDICMainAppOptions.storeNothing() && PDICMainAppOptions.getHistoryStrategy7()){
			prepareHistoryCon().insertUpdate(this, keytmp, null);
		}

		if(fullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
		}
		if(etSearch!=null) {
			etSearch.setText(keytmp);
		}
		return keytmp;
	}
	
	Rect  WindowVisibleR = new Rect();

	private int isKeyboardShown(View rootView) {
		rootView.getWindowVisibleDisplayFrame(WindowVisibleR);
		return rootView.getBottom() - WindowVisibleR.bottom;// > softKeyboardHeight * CMN.dm_density;
	}
	

	private void dumpSettings(){
		if(systemIntialized) {

			opt.setFloatBottombarOnBottom(contentUIData.webcontentlister.multiplier==-1);
			Editor putter = opt.defaultputter();
			putter.putLong("MFF", opt.FirstFlag())//FVDOCKED
			.putInt("FVH",mfv_lp.height)
			.putInt("FVW",mfv_lp.width)
			.putInt("FVTX",(int) mainfv.getTranslationX())
			.putInt("FVTY",(int) mainfv.getTranslationY())
			.putInt("UDFVW",FVW_UNDOCKED)
			.putInt("UDFVH",FVH_UNDOCKED)
			.putInt("FBBS",contentUIData.webcontentlister.getPrimaryContentSize())//FloatBottombarSize
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
			if (msg.what==3344) {
				((PageSlide)msg.obj).handleMsg(msg);
			}
		}
	}
	
	public void viewContent(WebViewListHandler wlh) {
		ViewUtils.addViewToParent(contentUIData.webcontentlister, mainframe);
		wlh.viewContent();
		
		if(contentUIData.webcontentlister.getVisibility()!=View.VISIBLE) {
			contentUIData.webcontentlister.setVisibility(View.VISIBLE);
			if(opt.getAnimateContents()) {
				contentUIData.webcontentlister.startAnimation(loadCTAnimation());
			}
		}
	}
	
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
				toggleBatchSearch();
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
					contentUIData.webcontentlister.setVisibility(View.GONE);
					bWantsSelection=false;
					if(webSingleholder.getChildCount()!=0) {
						webSingleholder.removeAllViews();
					}
					weblistHandler.removeAllViews();
					etSearch_ToToolbarMode(0);
				}
			} break;
		}
	}

	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:null;
		boolean isLongClicked= mmi != null && mmi.isLongClicked;
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
				weblistHandler.toggleFoldAll();
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
            	//todo impl
            	if(prepareHistoryCon().insertUpdate(this, keyword, null)>0)
            		showT("已收藏！");
            break;
            case R.id.toolbar_action5:
				weblistHandler.togSchPage();
            break;
        }
		if(closeMenu)
			closeIfNoActionView(mmi);
		return ret;
	}

	@Override
	public void invalidAllLists() {
		webSingleholder.removeAllViews();
		weblistHandler.removeAllViews();
		bIsFirstLaunch = true;
		//todo 有时白，fowhy.
		CombinedSearchTask_lastKey = null;
		tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
	}

	@Override
	public int getVisibleHeight() {
		return root.getHeight();
	}

}
package com.knziha.plod.PlainDict;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.androidadvance.topsnackbar.TSnackbar;
import com.knziha.filepicker.view.WindowChangeHandler;
import com.knziha.plod.dictionary.Flag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_asset;
import com.knziha.plod.dictionarymodels.mdict_nonexist;
import com.knziha.plod.dictionarymodels.mdict_prempter;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.dictionarymodels.resultRecorderScattered;
import com.knziha.plod.dictionarymodels.resultRecorderScattered2;
import com.knziha.plod.dictsmanager.dict_manager_activity;
import com.knziha.plod.dictsmanager.files.BooleanSingleton;
import com.knziha.plod.dictsmanager.files.IntegerSingleton;
import com.knziha.plod.searchtasks.CombinedSearchTask;
import com.knziha.plod.searchtasks.FullSearchTask;
import com.knziha.plod.searchtasks.FuzzySearchTask;
import com.knziha.plod.searchtasks.VerbatimSearchTask;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.CheckedTextViewmy;
import com.knziha.plod.widgets.IMPageSlider;
import com.knziha.plod.widgets.IMPageSlider.PageSliderInf;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.NoScrollViewPager;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.additiveMyCpr1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import db.LexicalDBHelper;

@SuppressLint({"SetTextI18n", "ClickableViewAccessibility","PrivateApi","DiscouragedPrivateApi"})
public class PDICMainActivity extends MainActivityUIBase implements OnClickListener, OnLongClickListener, OnMenuItemClickListener{
	private String debugString=null;//x  b  世           界     你好 happy呀happy\"人\"’。，、？
	public static int taskCounter = 0;
	public Timer timer;
	public int currentSearchingDictIdx;
	public SeekBar dvSeekbar;
	public TextView dvProgressFrac;
	public TextView dvResultN;

	ViewGroup mlv;
	public ListView mlv1;
	public ListView mlv2;

	public ListViewAdapter2 adaptermy2 = new ListViewAdapter2(R.layout.listview_item1);
	public ListViewAdapter2 adaptermy4 = new ListViewAdapter2();
	//mdict md = null;
	public String lastFuzzyKeyword;
	public String lastFullKeyword;
	public String lastKeyword;

	ImageView widget0;
	ArrayList<View> viewList;
	NoScrollViewPager viewPager;
	ActionBarDrawerToggle mDrawerToggle;
	DrawerLayout mDrawerLayout;

	private ImageView browser_widget3;
	private ImageView browser_widget4;
	public static long stst;

	private long exitTime = 0;
	private int  CurrentViewPage = 0;

	public boolean bNeedReAddCon;
	public boolean bInPeruseMode=true;
	public boolean bInPeruseModeToggleManifested=false;
	private MyHandler mHandle;
	private AsyncTask<String, Integer, String> mAsyncTask;

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(!systemIntialized) return;
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		//CMN.Log(newConfig);
		if(mConfiguration.orientation!=newConfig.orientation) {
			R.styleable.single[0] = android.R.attr.actionBarSize;
			TypedValue typedValue = new TypedValue();
			getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
			TypedArray array = obtainStyledAttributes(typedValue.resourceId, R.styleable.single);
			actionBarSize = array.getDimensionPixelSize(0, (int) (56*dm.density));
			array.recycle();
			CMN.Log(actionBarSize);

			LayoutParams lp = toolbar.getLayoutParams();
			lp.height=actionBarSize;
			toolbar.setLayoutParams(lp);
			refreshContentBow(opt.isContentBow());

			if(d!=null) {
				if(d instanceof WindowChangeHandler)
					((WindowChangeHandler)d).OnWindowChange(dm);
				else if(d.getWindow().getDecorView().getWidth()>dm.widthPixels) {
					d.getWindow().getAttributes().width = (int) (dm.widthPixels-2.5*getResources().getDimension(R.dimen.diagMarginHor));
					d.getWindow().setAttributes(d.getWindow().getAttributes());
				}

			}
		}
		mConfiguration = getResources().getConfiguration();
	}

	public void exitFFSearch() {
		d = null;
		dvSeekbar = null;
		dvProgressFrac = null;
		dvResultN = null;
		md.get(currentSearchingDictIdx).fuzzyCancled=true;
	}

	public void OnEnterFullSearchTask(AsyncTask task) {
		taskCounter=md.size();
		mdict.fuzzyKeyCounter=0;
		ShowProgressDialog().findViewById(R.id.cancel).setOnClickListener(v13 -> {
			task.cancel(false);
			md.get(currentSearchingDictIdx).searchCancled=true;
		});
		for(int i=0;i<md.size();i++){//遍历所有词典
			mdict mdtmp = md.get(i);
			mdtmp.searchCancled = false;
			mdtmp.dirtyfzPrgCounter=0;
			if(mdtmp.combining_search_tree_4!=null) {
				for (int ti = 0; ti < mdtmp.combining_search_tree_4.length; ti++) {//遍历搜索结果容器
					if (mdtmp.combining_search_tree_4[ti] != null)
						mdtmp.combining_search_tree_4[ti].clear();
				}
			}
		}
		stst=System.currentTimeMillis(); //获取开始时间
	}

	public void OnEnterFuzzySearchTask(AsyncTask task) {
		taskCounter=md.size();
		mdict.fuzzyKeyCounter=0;
		ShowProgressDialog().findViewById(R.id.cancel).setOnClickListener(v13 -> {
			task.cancel(false);
			md.get(currentSearchingDictIdx).searchCancled=true;
		});
		for(int i=0;i<md.size();i++){//遍历所有词典
			mdict mdtmp = md.get(i);
			mdtmp.fuzzyCancled=false;
			if(mdtmp.combining_search_tree2!=null)
				for(int ti=0;ti<mdtmp.combining_search_tree2.length;ti++){//遍历搜索结果
					if(mdtmp.combining_search_tree2[ti]!=null)
						mdtmp.combining_search_tree2[ti].clear();
				}

		}
		stst=System.currentTimeMillis(); //获取开始时间
	}

	private View ShowProgressDialog() {
		View a_dv = inflater.inflate(R.layout.dialog_progress, findViewById(R.id.dialog), false);
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
		builder2.setView(a_dv);
		AlertDialog dTmp = builder2.create();
		dTmp.setCanceledOnTouchOutside(false);
		dTmp.setOnDismissListener(dialog -> { exitFFSearch(); });
		dTmp.show();
		Window win = dTmp.getWindow();
		if (win != null) {
			win.getAttributes().height = (int) (250*dm.density);
			win.setAttributes(win.getAttributes());
		}
		d = dTmp;
		dvSeekbar = a_dv.findViewById(R.id.seekbar);
		dvProgressFrac = a_dv.findViewById(R.id.progressFrac);
		dvResultN = a_dv.findViewById(R.id.resultN);
		a_dv.findViewById(R.id.skip).setOnClickListener(v14 -> md.get(currentSearchingDictIdx).searchCancled=true);

		timer=new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				hdl.removeMessages(1008601, null);
				hdl.sendEmptyMessage(1008601);
			}
		},0,100);
		return a_dv;
	}

	public void updateFFSearch(Integer index) {
		try {
			if(d!=null){
				mdict m = md.get(index);
				currentSearchingDictIdx =index;
				dvSeekbar.setMax((int) m.getNumberEntries());
				((TextView)d.findViewById(R.id.title)).setText(m._Dictionary_fName);
				((TextView)d.findViewById(R.id.tv)).setText(currentSearchingDictIdx+"/"+PDICMainActivity.taskCounter);
			}
		} catch (Exception ignored) { }
	}

	private static class MyHandler extends Handler{
		//HashSet<Integer> invalidatepool = new HashSet(1);
		private final WeakReference<Toastable_Activity> activity;
		float animator = 0.1f;
		float animatorD = 0.15f;
		MyHandler(Toastable_Activity a) {
			this.activity = new WeakReference<>(a);
		}
		@Override
		public void handleMessage(@NonNull Message msg) {
			if(activity.get()==null) return;
			PDICMainActivity a = ((PDICMainActivity)activity.get());
			switch (msg.what) {
				case 1008601:
					//((TextView)dv.findViewById(R.id.tv)).setText("0/"+System.currentTimeMillis());
					if(a.currentSearchingDictIdx>=a.md.size())
						return;
					mdict m = a.md.get(a.currentSearchingDictIdx);
					if(a.dvSeekbar!=null)
					try {
						a.dvSeekbar.setProgress(m.dirtyfzPrgCounter);
						a.dvProgressFrac.setText(m.dirtyfzPrgCounter+"/"+m.getNumberEntries());
						a.dvResultN.setText("已搜索到: "+mdict.fuzzyKeyCounter+" 项条目!");
					} catch (Exception ignored) { }
					break;
				case 10086:

					break;
				case 112233:
					a.mDrawerToggle.onDrawerSlide(a.mDrawerLayout, animator);
					if(!a.triggered)
						animator+=animatorD;
					else
						animator-=animatorD;
					if(animator>=1) {
						a.triggered=true;
						a.mDrawerToggle.onDrawerOpened(a.mDrawerLayout);
					}
					if(animator>0) {
						a.hdl.sendEmptyMessage(112233);
					}else
						a.mDrawerToggle.onDrawerClosed(a.mDrawerLayout);
					break;
				case 331122:
					animator+=animatorD;
					if(animator>=1) {
						a.refreshUIColors();
						//drawerFragment.myAdapter.notifyDataSetChanged();
						//drawerFragment.mDrawerListView.setBackgroundColor(AppWhite==Color.WHITE?0xffe2e2e2:AppWhite);
						//drawerFragment.HeaderView.setBackgroundColor(AppWhite);
						//drawerFragment.FooterView.setBackgroundColor(AppWhite);
					}
					else {
						int filteredColor = a.AppWhite==Color.WHITE?ColorUtils.blendARGB(Color.BLACK,a.MainBackground, animator):ColorUtils.blendARGB(a.MainBackground, Color.BLACK, animator);
						int filteredWhite = ColorUtils.blendARGB(a.AppBlack, a.AppWhite, animator);

						a.root.setBackgroundColor(filteredColor);
						a.viewPager.setBackgroundColor(filteredWhite);
						a.lv2.setBackgroundColor(filteredWhite);
						//drawerFragment.mDrawerListView.setBackgroundColor(filteredWhite);
						//drawerFragment.HeaderView.setBackgroundColor(filteredWhite);
						//drawerFragment.FooterView.setBackgroundColor(filteredWhite);
						a.hdl.sendEmptyMessage(331122);
					}
					break;
				case 778899:
					if(true) {
						a.NaugtyWeb.setLayoutParams(a.NaugtyWeb.getLayoutParams());
						//CMN.Log("handler scroll scale recalibrating ...")
						return;
					}
					break;
				default: break;
			}
		}};
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//showT("asdasd"+event);
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				ViewGroup sm;
				if(opt.get_use_volumeBtn()) {
					if(DBrowser !=null && main.getChildCount()==1){//==1: 内容未加渲染
						if(opt.get_use_volumeBtn()) {
							if(DBrowser.inSearch)
								DBrowser.onClick(DBrowser.main_clister_layout.findViewById(R.id.browser_widget13));
							else {
								View v = new View(this);
								v.setId(R.id.nxt_plain);
								DBrowser.onClick(v);
							}
							return true;
						}
					}else if(PeruseView!=null && PeruseView.getView().getParent() !=null) {
						PeruseView.contentview.findViewById(R.id.browser_widget11).performClick();
						return true;
					}else if(contentview.getParent()!=null) {
						contentview.findViewById(R.id.browser_widget11).performClick();
						return true;
					}else if(!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
						ListView lvlv = lv;
						if(CurrentViewPage==0) lvlv=mlv1;
						else if(CurrentViewPage==2) lvlv=mlv2;
						if(lvlv.getChildCount()>0) {
							int fvp = lvlv.getFirstVisiblePosition();
							lvlv.setSelection(fvp+1);
							return true;
						}
					}
				}
				break;
			case KeyEvent.KEYCODE_VOLUME_UP:
				if(opt.get_use_volumeBtn()) {
					if(DBrowser !=null && main.getChildCount()==1){
						if(DBrowser.inSearch)
							DBrowser.onClick(DBrowser.main_clister_layout.findViewById(R.id.browser_widget14));
						else {
							View v = new View(this);
							v.setId(R.id.lst_plain);
							DBrowser.onClick(v);
						}
						return true;
					}else if(PeruseView!=null && PeruseView.getView().getParent() !=null) {
						PeruseView.contentview.findViewById(R.id.browser_widget10).performClick();
						return true;
					}else if(contentview.getParent()!=null) {
						contentview.findViewById(R.id.browser_widget10).performClick();
						return true;
					}else if(!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
						ListView lvlv = lv;
						if(CurrentViewPage==0) lvlv=mlv1;
						else if(CurrentViewPage==2) lvlv=mlv2;
						if(lvlv.getChildCount()>0) {
							int fvp = lvlv.getFirstVisiblePosition();
							if(lvlv.getChildAt(0).getTop()>-10)
								fvp--;
							lvlv.setSelection(fvp);
							return true;
						}
					}
				}
				break;
			case KeyEvent.KEYCODE_VOLUME_MUTE:
				if(DBrowser !=null){
					return true;
				}
				break;
			case KeyEvent.KEYCODE_BACK:
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					if(removeBlack())
						return true;
					else if(PeruseView!=null && (sm=(ViewGroup) PeruseView.getView().getParent())!=null) {
						if(PeruseView.contentview!=null && PeruseView.contentview.getParent()==main) {
							main.removeView(PeruseView.contentview);
							return true;
						}
						PeruseView.onViewDetached();
						sm.removeView(PeruseView.getView());
					}
					else if(dialogHolder.getVisibility()==View.VISIBLE) {
						dialogHolder.setVisibility(View.GONE);
						if(pickDictDialog!=null) if(pickDictDialog.isDirty)  {opt.putFirstFlag();pickDictDialog.isDirty=false;}
					}else if(main.getChildCount()<=0){//TODO crash
						if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
							mDrawerLayout.closeDrawer(GravityCompat.START);
							return true;
						}
						if((System.currentTimeMillis()-exitTime) > 2000){
							showX(R.string.warn_exit,0);
							exitTime = System.currentTimeMillis();
						} else {
							onDestroy();
							finish();
							System.exit(0);
						}
					}else if(DBrowser !=null){
						if(DBrowser.try_goBack()!=0)
							return true;
						File newFavor = DBrowser.items.get(DBrowser.lastChecked);
						if(!(DBrowser instanceof DHBroswer))
							if(!newFavor.equals(new File(favoriteCon.pathName))) {//或许需要重载收藏夹
								favoriteCon.close();
								favoriteCon = new LexicalDBHelper(this, newFavor);
								String name = new File(favoriteCon.pathName).getName();
								//opt.putString("currFavoriteDBName", opt.currFavoriteDBName=);
								opt.putCurrFavoriteDBName(favorTag+name.substring(0,name.length()-4));
								show(R.string.currFavor, DBrowser.boli(newFavor.getName()));
							}
						webholder.removeAllViews();
						getSupportFragmentManager().beginTransaction().remove(DBrowser).commit();
						//main.removeView(DBroswer.getView());
						//DBroswer.onDetach();
						if(true && DBrowser.currentDisplaying!="") {
							if(!opt.getBrowser_AffectInstant()) etSearch.removeTextChangedListener(tw1);
							if(etSearch_ToToolbarMode(4))
								lastEtString=etSearch.getText().toString();
							etSearch.setText(DBrowser.currentDisplaying);
							ivDeleteText.setVisibility(View.VISIBLE);
							if(!opt.getBrowser_AffectInstant()) etSearch.addTextChangedListener(tw1);
						}
						DBrowser = null;
					}else if(ActivedAdapter!=null && contentview.getParent()!=null) {
						main_progress_bar.setVisibility(View.GONE);

						toolbar.getMenu().findItem(R.id.toolbar_action2).setVisible(true);//Ser
						iItem_PerwordSearch.setVisible(CurrentViewPage==1);
						iItem_PeruseMode.setVisible(true);

						iItem_aPageRemember.setVisible(false);//翻忆
						iItem_FolderAll.setVisible(false);//折叠

						iItem_ComResNotifier.setVisible(CurrentViewPage==1 && isCombinedSearching);
						iItem_HintSerMode.setVisible(CurrentViewPage!=1);
						iItem_TintWildRes.setVisible(CurrentViewPage==0);
						iItem_PickDict.setVisible(false);
						iItem_PickSet.setVisible(false);
						iItem_SaveBookMark.setVisible(false);

						ActivedAdapter.SaveVOA();
						adaptermy2.currentKeyText=null;
						adaptermy.currentKeyText=null;
						webholder.removeAllViews();
						int remcount = webSingleholder.getChildCount();
						if(remcount>0) webSingleholder.removeAllViews();

						if(currentDictionary!=null) currentDictionary.expectedPos=0;
						adaptermy2.expectedPos=0;
						if(drawerFragment.d!=null) {
							drawerFragment.d.show();
						}
						PageSlider.setTranslationX(0);
						PageSlider.setTranslationY(0);
						int lastPos = ActivedAdapter.lastClickedPos;
						if(CurrentViewPage==1){//count==4
							for(int i=0;i<md.size();i++){
								md.get(i).clearWebview();
								//if(md.get(i).mWebView!=null)
								//	md.get(i).mWebView.clearView();
							}
							if(ActivedAdapter==adaptermy2){
								main.removeView(contentview);
								etSearch_ToToolbarMode(0);
								bWantsSelection=false;
								//webholder.removeAllViews();
								if(lastPos<lv2.getFirstVisiblePosition() || lastPos>lv2.getLastVisiblePosition())
									lv2.setSelection(lastPos);
							}else{
								currentDictionary.clearWebview();
								main.removeView(contentview);
								etSearch_ToToolbarMode(0);
								bWantsSelection=false;
								//webholder.removeAllViews();
								if(lastPos<lv.getFirstVisiblePosition() || lastPos>lv.getLastVisiblePosition())
									lv.setSelection(lastPos);
							}
						}else{
							for(int i=0;i<md.size();i++){
								md.get(i).clearWebview();
							}
							main.removeView(contentview);
							etSearch_ToToolbarMode(0);
							bWantsSelection=false;
							//webholder.removeAllViews();
							if(CurrentViewPage==0) {
								if(lastPos<mlv1.getFirstVisiblePosition() || lastPos>mlv1.getLastVisiblePosition())
									mlv1.setSelection(lastPos);
							}else {
								if(lastPos<mlv2.getFirstVisiblePosition() || lastPos>mlv2.getLastVisiblePosition())
									mlv2.setSelection(lastPos);
							}
						}
						//else main.removeViews(3,count-1);
					}else{
						main.removeAllViews();
						etSearch_ToToolbarMode(0);
						bWantsSelection=false;
					}
					return true;

				}
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	DBroswer DBrowser;

	private MenuItem iItem_FolderAll;

	private MenuItem iItem_ComResNotifier;
	private MenuItem iItem_aPageRemember;


	private MenuItem iItem_PickDict;
	private MenuItem iItem_PickSet;
	private MenuItem iItem_SaveBookMark;

	private MenuItem iItem_TintWildRes;
	private MenuItem iItem_HintSerMode;

	private MenuItem iItem_PerwordSearch;
	private MenuItem iItem_PeruseMode;

	//public boolean aRemPos=true,aRemPos2,bSerSnackTip=true;

	BooleanSingleton TintWildResult;
	public String Current0SearchText;


	Configuration mConfiguration;
	private View cb1;
	protected boolean nNeedSaveViewStates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mConfiguration = getResources().getConfiguration();
		R.styleable.constances[0]=0;
		bIsFirstLaunch=false;
		super.onCreate(null);
		//CMN.a = this;
		//FU.printStorage(this, null);
		try {
			setTheme(R.style.PlainAppTheme);
			//disable keyboard auto-coming up feature
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			setContentView(R.layout.activity_main);
			contentview =  findViewById(R.id.webcontent);
			viewPager = findViewById(R.id.viewpager);
			CMN.Log("viewPagerviewPager",viewPager);

			toolbar = findViewById(R.id.toolbar);
			toolbar.inflateMenu(R.menu.menu);

			iItem_FolderAll = toolbar.getMenu().findItem(R.id.toolbar_action0);

			iItem_PerwordSearch = toolbar.getMenu().findItem(R.id.toolbar_action3);
			iItem_PeruseMode = toolbar.getMenu().findItem(R.id.toolbar_action4);

			iItem_ComResNotifier = toolbar.getMenu().findItem(R.id.toolbar_action5);
			iItem_aPageRemember = toolbar.getMenu().findItem(R.id.toolbar_action6);
			iItem_PickDict = toolbar.getMenu().findItem(R.id.toolbar_action7);
			iItem_PickSet = toolbar.getMenu().findItem(R.id.toolbar_action8);
			iItem_SaveBookMark = toolbar.getMenu().findItem(R.id.toolbar_action9);
			iItem_HintSerMode = toolbar.getMenu().findItem(R.id.toolbar_action10);
			TintWildResult=new BooleanSingleton(true);
			iItem_TintWildRes = toolbar.getMenu().findItem(R.id.toolbar_action11);

			mDrawerLayout = findViewById(R.id.drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
			mDrawerToggle.syncState();// 添加按钮
			View vTmp = toolbar.getChildAt(toolbar.getChildCount()-1);
			CMN.Log(vTmp);
			if(vTmp instanceof ImageButton) {
				CMN.Log("dn hooking!");
				//Field f = Toolbar.class.getDeclaredField("mMenuView");
				//f.setAccessible(true);;
				//toolbar_mMenuView = (ActionMenuView) f.get(toolbar);


				//CMN.recurseLog(toolbar);
				ViewGroup Menu=(ViewGroup) toolbar.getChildAt(toolbar.getChildCount()-2);
				View OverflowIcon=Menu.getChildAt(Menu.getChildCount()-1);
				CMN.Log(OverflowIcon);
				OverflowIcon.setClickable(true);
				OverflowIcon.setOnLongClickListener(v -> {
					showT("长按暂未设定1");
					return true;
				});
				if(false)
					OverflowIcon.setOnClickListener(v -> showT("按暂未设定1"));


				ImageButton NavigationIcon=(ImageButton) vTmp;

				MarginLayoutParams lp = (MarginLayoutParams) NavigationIcon.getLayoutParams();
				//lp.setMargins(-10,-10,-10,-10);
				lp.width=(int) (45*dm.density);
				NavigationIcon.setLayoutParams(lp);
				try {
					//拿到mListenerInfo ，可以通过getListenerInfo方法
					Class<?> clazzView = Class.forName("android.view.View");
					Method getListenerInfoMethod = clazzView.getDeclaredMethod("getListenerInfo");
					getListenerInfoMethod.setAccessible(true);
					Object listenerInfo = getListenerInfoMethod.invoke(NavigationIcon);
					//拿到 mOnLongClickListener字段，这里的ListenerInfo是View的内部类，需要用$符号链接。
					Class<?> clazz = Class.forName("android.view.View$ListenerInfo");
					Field field = clazz.getDeclaredField("mOnClickListener");
					field.setAccessible(true);
					//拿到原来的mOnLongClickListener字段的值
					final OnClickListener raw =(OnClickListener) field.get(listenerInfo);
					CMN.Log("dn hooked!");
					NavigationIcon.setOnClickListener(v -> {
						raw.onClick(v);
						if(!mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
							if(contentview.getParent()==main) {
								main.removeView(contentview);
								bNeedReAddCon=true;
							}
							imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
						}

					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				//toolbar.removeView(NavigationIcon);
				//toolbar.addView(NavigationIcon,1);
			}

			setStatusBarColor();


			cb1=findViewById(R.id.cb1);
			cb1.setOnClickListener(this);
			CheckableImageView cTmp = findViewById(R.id.cb2);
			cTmp.setOnClickListener(this);
			cTmp.setChecked(opt.getPinPicDictDialog());
			cTmp = findViewById(R.id.cb3);
			cTmp.setOnClickListener(this);
			cTmp.setChecked(opt.getPicDictAutoSer());


			dialogHolder = findViewById(R.id.dialogHolder);
			dialog_ = findViewById(R.id.dialog_);
			dialogHolder.setOnTouchListener((v, event) -> {
				dialogHolder.setVisibility(View.GONE);
				if(pickDictDialog!=null) if(pickDictDialog.isDirty) {opt.putFirstFlag();pickDictDialog.isDirty=false;}
				return true;
			});

			hdl = mHandle = new MyHandler(this);


			//opt.Search_pathitorys = new File[]{new File("/sdcard/mdictlib/")};

			checkLaunch(savedInstanceState);
		}catch(Exception e) {
			e.printStackTrace();
			Log.e("fatal error",e.toString());
			Toast.makeText(this, "启动出现异常："+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
	}



	protected void further_loading(final Bundle savedInstanceState) {
		IMPageCover = contentview.findViewById(R.id.cover);
		PageSlider = contentview.findViewById(R.id.PageSlider);
		main = findViewById(R.id.mainF);
		webcontentlist = contentview.findViewById(R.id.webcontentlister);
		bottombar2 = contentview.findViewById(R.id.bottombar2);
		browser_widget3 = findViewById(R.id.browser_widget3);
		browser_widget4 = findViewById(R.id.browser_widget4);
		CachedBBSize=opt.getBottombarSize((int) (35*dm.density));
		super.further_loading(savedInstanceState);
        /*
        ((BaseToolbar)toolbar).flingDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
    	    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX,
    	    		final float velocityY) {
    			if(Math.abs(velocityX/velocityY)>2.144) {
    				if(opt.getTopSlidenabled()) {
    					switchToSearchModeDelta((int) Math.signum(velocityX));
    				}
    	            ((BaseToolbar)toolbar).isOnFlingDected=true;
    			}
    	    		return false;
    	}
    	});

        toolbar.setOnTouchListener(new OnTouchListener() {
        	float OrgX;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						OrgX=event.getX();
						viewPager.beginFakeDrag();
					break;
					case MotionEvent.ACTION_UP:
						viewPager.endFakeDrag();
					break;
					case MotionEvent.ACTION_MOVE:
						viewPager.fakeDragBy(event.getX()-OrgX);
					break;
				}
				return false;
			}});
        }*/

		//showT(root.getParent().getClass());

		main.removeView(contentview);
		contentview.setVisibility(View.VISIBLE);

		if(!opt.getBottombarOnBottom())
			webcontentlist.SwitchingSides();
		bInPeruseModeToggleManifested=opt.getInPeruseModeTM();
		bInPeruseMode=opt.getInPeruseMode();
		//SplitViewGuarder_ svGuard = (SplitViewGuarder_) contentview.findViewById(R.id.svGuard);
		//svGuard.SplitViewsToGuard.add(webcontentlister);


		//PageSlider.SCViewToMute = (ScrollViewmy) webholder.getParent();
		TurnPageEnabled=PageSlider.TurnPageEnabled=opt.getTurnPageEnabled();
		PageSlider.IMSlider = IMPageCover;
		IMPageCover.setPageSliderInf(new PageSliderInf() {
			protected Bitmap PageCache;
			@Override
			public void onPreparePage(final IMPageSlider IMPageCover) {
				mPageCanvas.drawColor(Color.WHITE);
				currentPos=currentDictionary.currentPos;
				if(IMPageCover.getTag()==null) {
					if(Build.VERSION.SDK_INT>23) {
						IMPageCover.getForeground().setTint(MainBackground);
					}
					IMPageCover.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
					IMPageCover.setTag(false);
				}
				if(Build.VERSION.SDK_INT>23)
					IMPageCover.getForeground().setAlpha(0);
				IMPageCover.setTranslationY(0);
				RLContainerSlider PageSlider_ = PageSlider;
				if(PeruseView!=null && ActivedAdapter==PeruseView.ada2)
					PageSlider_=PeruseView.PageSlider;
				if(PageCache==null) {
					PageCache = Bitmap.createBitmap(dm.widthPixels,dm.heightPixels, Bitmap.Config.ARGB_8888);
					mPageCanvas.setBitmap(PageCache);
					mPageDrawable = new BitmapDrawable(getResources(), PageCache);

				}
				//IMPageCover.setImageBitmap(PageCache);
				IMPageCover.setScaleType(ImageView.ScaleType.MATRIX);
				HappyMatrix = new Matrix();
				HappyMatrix.setScale(Math.round(1.f*dm.widthPixels/PageSlider_.getWidth()),Math.round(1.f*dm.heightPixels/PageSlider_.getHeight()));
				IMPageCover.setImageMatrix(HappyMatrix);

				IMPageCover.setImageDrawable(mPageDrawable);

				if(PageCache.getWidth()!=PageSlider_.getWidth() || PageCache.getHeight()!=PageSlider.getHeight()) {
					//PageCache.setHeight(PageSlider_.getHeight());
					//PageCache.setWidth(PageSlider_.getWidth());
				}


				mPageCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.SRC_IN);

				if(PeruseView!=null && ActivedAdapter==PeruseView.ada2) {
					PeruseView.webSingleholder.post(() -> {
						//if(PageCache.isRecycled())PageCache = Bitmap.createBitmap(PageCache.getWidth(), PageCache.getHeight(), Bitmap.Config.ARGB_8888);
						PeruseView.webSingleholder.draw(mPageCanvas);
					});
				}else {
					if(webholder.getChildCount()!=0) {
						webholder.post(() -> {
							ScrollView sv = (ScrollView) webholder.getParent();
							mPageCanvas.translate(0, -sv.getScrollY());
							sv.draw(mPageCanvas);
							mPageCanvas.translate(0, sv.getScrollY());
						});
					}else
						webSingleholder.draw(new Canvas(PageCache));
				}

				IMPageCover.setVisibility(View.VISIBLE);
				IMPageCover.setAlpha(1.0f);
				//IMPageCover.setBackgroundDrawable(new BitmapDrawable(getResources(),PageCache));
				LayoutParams lpp = IMPageCover.getLayoutParams();
				if(lpp.height!=-1) {
					lpp.height=-1;
					IMPageCover.setLayoutParams(lpp);
				}
				CMN.Log("drawed");
			}

			@Override
			public void onDecided(boolean Dir,IMPageSlider IMPageCover) {
				//IMPageCover.setAlpha(0.5f);
				//IMPageCover.setForegroundTintList(tint);
				//IMPageCover.getForeground().setAlpha(0);
				if(Build.VERSION.SDK_INT>23)
					IMPageCover.getForeground().setAlpha(255);
			}
			int currentPos=0;

			@Override
			public void onMoving(float val,IMPageSlider IMPageCover) {
				if(ActivedAdapter==adaptermy && currentDictionary.isViewInitialized()) {
					int pos = currentDictionary.currentPos+(Math.abs(val)>20*dm.density?(val<0?1:-1):0);
					if(pos>=-1 && pos<currentDictionary.getNumberEntries()) {
						if(currentPos!=pos)
							currentDictionary.toolbar_title.setText(currentDictionary.getEntryAt(pos).trim()+" - "+currentDictionary._Dictionary_fName);
						currentPos=pos;
					}
				}
			}

			@Override
			public void onHesitate(IMPageSlider IMPageCover) {
				if(Build.VERSION.SDK_INT>23)
					IMPageCover.getForeground().setAlpha(0);
			}

			@Override
			public void onPageTurn(int Dir,IMPageSlider IMPageCover) {
				boolean there = ActivedAdapter instanceof com.knziha.plod.PlainDict.PeruseView.ListViewAdapter;
				ViewGroup contentview_ = contentview;
				if(there)
					contentview_=PeruseView.contentview;
				if(Dir==1) {contentview_.findViewById(R.id.browser_widget11).performClick();}
				else if(Dir==0) contentview_.findViewById(R.id.browser_widget10).performClick();
				else if(ActivedAdapter==adaptermy && !there) {if(currentPos!=currentDictionary.currentPos) currentDictionary.toolbar_title.setText(currentDictionary.currentDisplaying.trim()+" - "+currentDictionary._Dictionary_fName);}
			}});

		viewPager.setOnTouchListener((v, event) -> {
			cancleSnack();
			return false;
		});
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int pos) {
				fetchAddtionalDrawbles();
				final IntegerSingleton msg = new IntegerSingleton(0);
				switch(CurrentViewPage=pos) {
					case 0:
						if(browser_widget4.getTag()!=null) {
							browser_widget4.setImageDrawable(full_search_drawable);
							browser_widget4.setTag(null);
						}
						if(browser_widget3.getTag()==null) {
							browser_widget3.setImageDrawable(fuzzy_search_drawable_pressed);
							browser_widget3.setTag(true);
						}
						if(opt.getHintSearchMode()) msg.first=1;

						//toolbar.getMenu().findItem(R.id.toolbar_action2).setVisible(false);//Ser
						break;
					case 1:
						if(browser_widget4.getTag()!=null) {
							browser_widget4.setImageDrawable(full_search_drawable);
							browser_widget4.setTag(null);
						}
						if(browser_widget3.getTag()!=null) {
							browser_widget3.setImageDrawable(fuzzy_search_drawable);
							browser_widget3.setTag(null);
						}
						break;
					case 2:
						if(browser_widget4.getTag()==null) {
							browser_widget4.setImageDrawable(full_search_drawable_pressed);
							browser_widget4.setTag(true);
						}
						if(browser_widget3.getTag()!=null) {
							browser_widget3.setImageDrawable(fuzzy_search_drawable);
							browser_widget3.setTag(null);
						}
						if(opt.getHintSearchMode()) msg.first=2;
						break;
				}

				iItem_PerwordSearch.setVisible(CurrentViewPage==1);
				iItem_PeruseMode.setVisible(true);
				//iItem_FolderAll.setVisible(false);//折叠
				//iItem_aPageRemember.setVisible(false);//翻忆
				iItem_ComResNotifier.setVisible(CurrentViewPage==1 && lv2.getVisibility()==View.VISIBLE);
				iItem_HintSerMode.setVisible(CurrentViewPage!=1);
				iItem_TintWildRes.setVisible(CurrentViewPage==0);
				//iItem_PickDict.setVisible(false);
				//iItem_PickSet.setVisible(false);
				//iItem_SaveBookMark.setVisible(false);

				if(msg.first>0) {
					viewPager.post(() -> {
						snack = TSnackbar.makeraw(main_succinct, getResources().getString(msg.first==1?R.string.fuzzyret:R.string.fullret),TSnackbar.LENGTH_LONG);
						snack.getView().setAlpha(0.5f);
						snack.show();
					});
				}
			}});

		mlv = (ViewGroup) inflater.inflate(R.layout.mainlistview,null);
		lv = mlv.findViewById(R.id.main_list);
		if(false)//xxx
			lv.setOnScrollChangeListener(new ListViewmy.OnScrollChangeListener() {
				int lastVisible=-1;
				int lastOff=-1;
				@Override
				public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
					if(lastVisible!=-1)
						if(lv.getChildCount()>=0) {
							if(lv.getFirstVisiblePosition()!=lastVisible || lv.getChildAt(0).getTop()!=lastOff) {
								nNeedSaveViewStates=true;
							}
							lastOff=lv.getChildAt(0).getTop();
						}
					lastVisible=lv.getFirstVisiblePosition();
					CMN.Log("onScrollChange");
				}});
		lv2 = mlv.findViewById(R.id.sub_list);
		mlv1 = (ListView) inflater.inflate(R.layout.sublistview,null);mlv1.setId(R.id.sub_list1);
		mlv2 = (ListView) inflater.inflate(R.layout.sublistview,null);mlv2.setId(R.id.sub_list2);
		if(Build.VERSION.SDK_INT >= 24)
			if(true) {//opt.is_strict_scroll()
				listViewStrictScroll(mlv1,true);
				listViewStrictScroll(mlv2,true);
				listViewStrictScroll(lv,true);
			}

		//listViewStrictScroll(lv2,true);

		viewList = new ArrayList<>();
		viewList.add(mlv1);
		viewList.add(mlv);
		viewList.add(mlv2);
		PagerAdapter pagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
				return arg0 == arg1;
			}
			@Override
			public int getCount() {
				return viewList.size();
			}
			@Override
			public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
				//showT("destroyItem");
				container.removeView(viewList.get(position));
			}
			@Override
			public int getItemPosition(@NonNull Object object) {
				return super.getItemPosition(object);
			}
			@NonNull
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(viewList.get(position));
				return viewList.get(position);
			}
		};
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(CurrentViewPage = 1);

		//main_content_succinct = (ViewGroup) findViewById(R.id.main);
		widget0= findViewById(R.id.browser_widget0);
		widget0.setOnClickListener(this);
		widget0.setOnLongClickListener(this);
		//widget0.getBackground().setTint(MainBackground);
		//widget0.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
		//ViewCompat.setBackgroundTintList(widget0, ColorStateList.valueOf(MainBackground));

		View favoriteFolderBtn = findViewById(R.id.browser_widget1);
		favoriteFolderBtn.setOnClickListener(this);
		favoriteFolderBtn = findViewById(R.id.browser_widget2);
		favoriteFolderBtn.setOnClickListener(this);
		favoriteFolderBtn.setOnLongClickListener(v -> {
			((AgentApplication)getApplication()).md=md;
			((AgentApplication)getApplication()).opt=opt;
			((AgentApplication)getApplication()).mdlibsCon=mdlibsCon;
			Intent intent = new Intent();
			intent.setClass(PDICMainActivity.this, dict_manager_activity.class);
			startActivityForResult(intent, 110);
			return false;
		});
		browser_widget3.setOnClickListener(this);
		browser_widget4.setOnClickListener(this);
		favoriteFolderBtn=findViewById(R.id.browser_widget5);
		favoriteFolderBtn.setOnClickListener(this); favoriteFolderBtn.setOnLongClickListener(this);
		favoriteFolderBtn = findViewById(R.id.browser_widget6);
		favoriteFolderBtn.setOnClickListener(this); favoriteFolderBtn.setOnLongClickListener(this);

		if(opt.getRemPos()) iItem_aPageRemember.setTitle(iItem_aPageRemember.getTitle()+"√");
		iItem_aPageRemember.setVisible(false);//翻忆

		if(opt.getNotifyComboRes())  iItem_ComResNotifier.setTitle(iItem_ComResNotifier.getTitle()+"√");

		if(opt.getHintSearchMode())  iItem_HintSerMode.setTitle(iItem_HintSerMode.getTitle()+" √");

		if(TintWildResult.first = opt.getTintWildRes())  iItem_TintWildRes.setTitle(iItem_TintWildRes.getTitle()+" √");


		iItem_PickDict.setVisible(false);
		iItem_PickSet.setVisible(false);

		iItem_ComResNotifier.setVisible(isCombinedSearching);
		iItem_HintSerMode.setVisible(false);
		iItem_TintWildRes.setVisible(false);
		iItem_SaveBookMark.setVisible(false);

		//TintWildResult.setVisible(false);
		//HintSerMode.setVisible(false);



		//mDrawerLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mDrawerLayout.addDrawerListener(mDrawerToggle);// 按钮动画特效
		mDrawerLayout.addDrawerListener(new DrawerListener() {
			@Override
			public void onDrawerOpened(@NonNull View arg0) {
				imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
				drawerFragment.isDirty=false;
				etSearch_ToToolbarMode(1);
			}

			@Override public void onDrawerClosed(@NonNull View arg0) {
				CMN.Log("onDrawerClosed");
			}
			@Override public void onDrawerSlide(@NonNull View arg0, float arg1) {

			}
			@Override public void onDrawerStateChanged(int newState) {
				if(!mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
					if(bNeedReAddCon) {
						main.addView(contentview);
						etSearch_ToToolbarMode(1);
						bNeedReAddCon=false;
					}else
						etSearch_ToToolbarMode(0);
				}
				if(drawerFragment.isDirty) {
					drawerFragment.isDirty=false;
					opt.putFirstFlag();
				}
			}});
		//mDrawerLayout.setScrimColor(0x00ffffff);

		lv.setAdapter(adaptermy= new ListViewAdapter());
		lv2.setAdapter(adaptermy2);
		lv2.setOnItemClickListener(adaptermy2);
		mlv1.setAdapter(adaptermy3 = new ListViewAdapter2());
		mlv1.setOnItemClickListener(adaptermy3);
		mlv2.setAdapter(adaptermy4);
		mlv2.setOnItemClickListener(adaptermy4);

		adaptermy3.combining_search_result = new resultRecorderScattered(this,md,TintWildResult);
		adaptermy4.combining_search_result = new resultRecorderScattered2(this,md);



		etSearch.addTextChangedListener(tw1);
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId==EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
				String key = etSearch.getText().toString().trim();
				if(key.length()>0) Current0SearchText=key;
				int tmp = viewPager.getCurrentItem();
				if(tmp==0 || tmp==2) {
					if(!opt.getHistoryStrategy0())
						if(opt.getHistoryStrategy1())
							historyCon.insertUpdate(etSearch.getText().toString().trim());
					if(!checkDics()) return true;
					//模糊搜索 & 全文搜索
					if(mAsyncTask!=null)
						mAsyncTask.cancel(false);
					imm.hideSoftInputFromWindow(main.getWindowToken(),0);
					(mAsyncTask=tmp==0?new FuzzySearchTask(PDICMainActivity.this)
							:new FullSearchTask(PDICMainActivity.this)).execute(key);
				}
				else tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
			}
			return true;
		});

		//switchToDictIdx(adapter_idx);
		if(currentDictionary!=null) {
			lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
		}

		systemIntialized=true;
		if(!bInPeruseModeToggleManifested) {
			widget0.setVisibility(View.GONE);
		}else {
			widget0.setVisibility(View.VISIBLE);
			if(bInPeruseMode) {
				widget0.setImageResource(R.drawable.peruse_ic_on);
				snack = TSnackbar.makeraw(main_succinct, "翻阅模式",TSnackbar.LENGTH_SHORT);
				snack.show();
			}
		}

		if(savedInstanceState!=null) {
			for(int i=0;i<md.size();i++){//遍历所有词典
				mdict mdtmp = md.get(i);
				if(savedInstanceState.containsKey("sizeOf"+mdtmp._Dictionary_fName)) {
					int size = savedInstanceState.getInt("sizeOf"+mdtmp._Dictionary_fName);
					mdtmp.combining_search_tree2 = new ArrayList[size];
					for(int ti=0;ti<size;ti++){//遍历搜索结果
						if(savedInstanceState.containsKey(mdtmp._Dictionary_fName+"@"+ti)) {
							mdtmp.combining_search_tree2[ti] = savedInstanceState.getIntegerArrayList(mdtmp._Dictionary_fName+"@"+ti);
						}
					}
				}
				if(savedInstanceState.containsKey("sizeOf_4"+mdtmp._Dictionary_fName)) {
					int size = savedInstanceState.getInt("sizeOf_4"+mdtmp._Dictionary_fName);
					mdtmp.combining_search_tree_4 = new ArrayList[size];
					for(int ti=0;ti<size;ti++){//遍历搜索结果
						if(savedInstanceState.containsKey(mdtmp._Dictionary_fName+"@_4"+ti)) {
							mdtmp.combining_search_tree_4[ti] = savedInstanceState.getIntegerArrayList(mdtmp._Dictionary_fName+"@_4"+ti);
						}
					}
				}
			}
			adaptermy3.combining_search_result.invalidate();
			adaptermy3.notifyDataSetChanged();

			adaptermy4.combining_search_result.invalidate();
			adaptermy4.notifyDataSetChanged();

			CurrentViewPage = savedInstanceState.getInt("CVP");
			if(CurrentViewPage==0){//亮A
				fetchAddtionalDrawbles();
				browser_widget3.setImageDrawable(fuzzy_search_drawable_pressed);
			}else if(CurrentViewPage==2){//亮B
				fetchAddtionalDrawbles();
				browser_widget4.setImageDrawable(full_search_drawable_pressed);
			}

			lv.post(() -> {
				int[] arr = savedInstanceState.getIntArray("P_L");
				if(arr!=null) lv.setSelectionFromTop(arr[0], arr[1]);
			});
			lv2.post(() -> {
				int[] arr = savedInstanceState.getIntArray("P_L2");
				if(arr!=null) lv2.setSelectionFromTop(arr[0], arr[1]);
			});
			mlv1.post(() -> {
				int[] arr = savedInstanceState.getIntArray("P_M1");
				if(arr!=null) mlv1.setSelectionFromTop(arr[0], arr[1]);
			});
			mlv2.post(() -> {
				int[] arr = savedInstanceState.getIntArray("P_M2");
				if(arr!=null) mlv2.setSelectionFromTop(arr[0], arr[1]);
			});
		}
		//Toast.makeText(this, ""+(System.currentTimeMillis()-sst), Toast.LENGTH_LONG).show();
		if(false)
			try {
				md.add(new mdict_asset("liba.mdx", this));
				currentDictionary = md.get(adapter_idx = md.size()-1);
				adaptermy.notifyDataSetChanged();
			} catch (IOException e) {
				Log.e("ffatal", "sda");
			}

		File additional_config = new File(opt.pathToMain()+"appsettings.txt");
		if(additional_config.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(additional_config));
				String line;
				while((line=in.readLine())!=null) {
					String[] arr = line.split(":", 2);
					if(arr.length==2) {
						if(arr[0].equals("window margin")||arr[0].equals("窗体边框")) {
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
						}else if(arr[0].equals("edit all")) {
							CMN.editAll=arr[1].length()==3;
						}else if(arr[0].equals("test float search")) {
							CMN.testFLoatSearch=arr[1].length()==3;
						}else if(arr[0].equals("debug string")) {
							debugString=arr[1];
						}
					}
				}
			} catch (Exception ignored) {}

		}

		//test groups
		//showAppTweaker();
		if(CMN.testFLoatSearch)
			startActivity(new Intent(this,FloatSearchActivity.class).putExtra("EXTRA_QUERY", "happy"));
		//Intent i = new Intent(this,dict_manager_activity.class); startActivity(i);
		if(debugString!=null) etSearch.setText(debugString);

		refreshUIColors();

		//lv.setFastScrollEnabled(false);
		//lv.setFastScrollStyle(R.style.AppBright);

		//lv.setVerticalScrollBarEnabled(false);
		//lv.setScrollBarStyle(R.style.AppTheme);
		//lv.setVerticalScrollBarEnabled(true);
		//lv.invalidate();
		if(opt.getInDarkMode())
			try {
				Object FastScroller = FastScrollField.get(lv);
				ImageView ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
				ThumbImage.setColorFilter(0x8a666666);



				FastScroller = FastScrollField.get(mlv1);
				ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
				ThumbImage.setColorFilter(0x8a666666);

				FastScroller = FastScrollField.get(mlv2);
				ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
				ThumbImage.setColorFilter(0x8a666666);

				if(lv2.isFastScrollEnabled()) {//why why why
					FastScroller = FastScrollField.get(lv2);
					ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
					ThumbImage.setColorFilter(0x8a666666);
				}
				//if(pickDictDialog!=null) {
				//	Object Scrollbar = ScrollCacheField.get(pickDictDialog.mRecyclerView);
				//    Drawable ScrollbarDrawable = (Drawable) ScrollBarDrawableField.get(Scrollbar);
				//    ScrollbarDrawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
				//}
			} catch (Exception e) {
				CMN.Log(e);
			}

	}





	@Override
	public String getSearchTerm(){
		return etSearch.getText().toString();
	}

	public int split_dict_thread_number;
	public void countDelta(int delta) {
		Lock lock = new ReentrantLock();
		lock.lock();
		poolEUSize+=delta;
		lock.unlock();
	}
	public volatile int poolEUSize;

	TextWatcher tw1=new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(CurrentViewPage==1 && iItem_ComResNotifier.isVisible()!=isCombinedSearching && webholder.getChildCount()==0)
				iItem_ComResNotifier.setVisible(isCombinedSearching);
			if(s.length()>0){
				etSearch_ToToolbarMode(3);
				if(CurrentViewPage==1) {
					String text = etSearch.getText().toString().trim();
					if(text.length()==0) return;
					if(text.startsWith("<")) {
						String perWSTag = getResources().getString(R.string.perWSTag);
						String firstTag = firstTag(text);
						if(firstTag!=null) {
							String fTCpror = firstTag.replace("~", "");
							if(perWSTag.equals(fTCpror)||"分字".equals(fTCpror)) {
								String input = text.substring(text.indexOf(">")+1).trim();
								iItem_ComResNotifier.setVisible(true);
								if(input.length()!=0)
									launchVerbatimSearch(input,!firstTag.contains("~"));
								return;
							}
						}
					}
					if(isCombinedSearching){
						if(lianHeTask!=null) {
							lianHeTask.cancel(false);
						}
						if(!checkDics()) return;
						lianHeTask = new CombinedSearchTask(PDICMainActivity.this);
						lianHeTask.execute(s.toString());
					}else
						try {
							if(!checkDics()) return;
							String key = s.toString().trim();
							//if(false) {//!currentDictionary.isCompact
							//	int idx=currentDictionary.lookUp(key, true);
							//	if(idx!=-1)
							//		lv.setSelection(idx);
							//}else {
							int idx=currentDictionary.lookUp(key);
							//CMN.show(""+idx);
							if(idx!=-1){
								int tmpIdx = idx;
								String looseKey = mdict.processText(key);
								String looseMatch = mdict.processText(currentDictionary.getEntryAt(idx));
								String LMT = looseMatch;
								if(!mdict.processText(looseMatch).startsWith(looseKey))
									idx--;
								else while(true) {
									if(looseMatch.startsWith(key)) {
										idx=tmpIdx;
										break;
									}
									if(tmpIdx>=currentDictionary.getNumberEntries()-1)
										break;
									looseMatch = currentDictionary.getEntryAt(++tmpIdx);
									if(!mdict.processText(looseMatch).startsWith(looseKey))
										break;
								}
								lv.setSelection(idx);
								if(bIsFirstLaunch||bWantsSelection) {
									if(LMT.equals(looseKey)) {
										boolean proceed = true;
										if(contentview.getParent()==main) {
											proceed = (adaptermy.currentKeyText == null || !looseKey.equals(mdict.processText(adaptermy.currentKeyText)));
										}
										if(proceed) {
											//adaptermy.adelta=idx-adaptermy.lastClickedPos;
											adaptermy.onItemClick(idx);
										}
									}
								}
								bIsFirstLaunch=false;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}else if(lv2.getVisibility()==View.VISIBLE)
				lv2.setVisibility(View.INVISIBLE);
		}

		public void beforeTextChanged(CharSequence s, int start, int count,  int after) {}

		public void afterTextChanged(Editable s) {
			//if (s.length() == 0) ivDeleteText.setVisibility(View.GONE);
			//else ivDeleteText.setVisibility(View.VISIBLE);
			if (s.length() != 0) ivDeleteText.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
		// Save the user's current game state
		if(false)
			try {
				for(int i=0;i<md.size();i++){//遍历所有词典
					mdict mdtmp = md.get(i);
					mdtmp.searchCancled=false;
					if(mdtmp.combining_search_tree2!=null){
						savedInstanceState.putInt("sizeOf"+mdtmp._Dictionary_fName,mdtmp.combining_search_tree2.length);
						for(int ti=0;ti<mdtmp.combining_search_tree2.length;ti++){//遍历搜索结果
							if(mdtmp.combining_search_tree2[ti]==null) {
								continue;
							}
							savedInstanceState.putIntegerArrayList(mdtmp._Dictionary_fName+"@"+ti, mdtmp.combining_search_tree2[ti]);
						}
					}

					if(mdtmp.combining_search_tree_4!=null) {
						savedInstanceState.putInt("sizeOf_4"+mdtmp._Dictionary_fName,mdtmp.combining_search_tree_4.length);
						for(int ti=0;ti<mdtmp.combining_search_tree_4.length;ti++){//遍历搜索结果
							if(mdtmp.combining_search_tree_4[ti]==null) {
								continue;
							}
							savedInstanceState.putIntegerArrayList(mdtmp._Dictionary_fName+"@_4"+ti, mdtmp.combining_search_tree_4[ti]);
						}
					}


				}

				if(lv!=null) {
					savedInstanceState.putInt("CVP", CurrentViewPage);
					View VZero = lv.getChildAt(0);
					if(VZero!=null)
						savedInstanceState.putIntArray("P_L",new int[] {lv.getFirstVisiblePosition(),VZero.getTop()});
					VZero = lv2.getChildAt(0);
					if(VZero!=null)
						savedInstanceState.putIntArray("P_L2",new int[] {lv2.getFirstVisiblePosition(),VZero.getTop()});
					VZero = mlv1.getChildAt(0);
					if(VZero!=null)
						savedInstanceState.putIntArray("P_M1",new int[] {mlv1.getFirstVisiblePosition(),VZero.getTop()});
					VZero = mlv2.getChildAt(0);
					if(VZero!=null)
						savedInstanceState.putIntArray("P_M2",new int[] {mlv2.getFirstVisiblePosition(),VZero.getTop()});
				}
			}catch(Exception ignored) {}
	}

	protected void launchVerbatimSearch(String input,final boolean isStrict) {
		if(!checkDics()) return;
		if(lianHeTask!=null)
			lianHeTask.cancel(false);
		lianHeTask = new VerbatimSearchTask(this, isStrict).execute(input);
	}

	Drawable full_search_drawable;
	Drawable fuzzy_search_drawable;
	Drawable full_search_drawable_pressed;
	Drawable fuzzy_search_drawable_pressed;


	void fetchAddtionalDrawbles(){
		if(full_search_drawable==null){
			full_search_drawable  = browser_widget4.getDrawable();//getResources().getDrawable(R.drawable.full_search);
			fuzzy_search_drawable = browser_widget3.getDrawable();//getResources().getDrawable(R.drawable.fuzzy_search);
			full_search_drawable_pressed = getResources().getDrawable(R.drawable.full_search_pressed);
			fuzzy_search_drawable_pressed = getResources().getDrawable(R.drawable.fuzzy_search_pressed);
		}
	}

	public void switchToSearchModeDelta(int i) {
		fetchAddtionalDrawbles();
		int new_curr = CurrentViewPage-i;
		new_curr = new_curr>2?2:new_curr;
		new_curr = new_curr<0?0:new_curr;
		if(new_curr==CurrentViewPage)
			return;
		viewPager.setCurrentItem(new_curr);
		String msg;
		if(new_curr==1){
			//都灭掉
			if(i>0){
				lastFullKeyword=etSearch.getText().toString();
				browser_widget4.setImageDrawable(full_search_drawable);
			}else{
				lastFuzzyKeyword=etSearch.getText().toString();
				browser_widget3.setImageDrawable(fuzzy_search_drawable);
			}
			//etSearch.setText(lastKeyword);
			CurrentViewPage = new_curr;
			//etSearch.addTextChangedListener(tw1);
			return;
		}
		//etSearch.removeTextChangedListener(tw1);
		lastKeyword = etSearch.getText().toString();
		if(i>0){//亮A
			//etSearch.setText(lastFuzzyKeyword);
			browser_widget3.setImageDrawable(fuzzy_search_drawable_pressed);
			browser_widget4.setImageDrawable(full_search_drawable);
			msg="词条通配搜索";
		}else{//亮B
			//etSearch.setText(lastFullKeyword);
			browser_widget4.setImageDrawable(full_search_drawable_pressed);
			browser_widget3.setImageDrawable(fuzzy_search_drawable);
			msg="全文搜索";
		}
		CurrentViewPage = new_curr;
		snack = TSnackbar.makeraw(main_succinct, msg,TSnackbar.LENGTH_LONG);
		snack.getView().setAlpha(0.5f);
		snack.show();
	}



	private void setStatusBarColor(){
		Window window = getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		if(Build.VERSION.SDK_INT>=21) {
			window.setStatusBarColor(Color.TRANSPARENT);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}

	@Override
	protected void onDestroy(){
		//CMN.show("onDestroy");
		onPause();
		dumpSettiings();
		new File(opt.pathToMain()).setLastModified(System.currentTimeMillis());
		super.onDestroy();
	}

	@Override
	protected void scanSettings(){
		super.scanSettings();
		CMN.MainBackground = MainBackground = opt.getMainBackground();
		//getWindow().setNavigationBarColor(MainBackground);
		CMN.FloatBackground = opt.getFloatBackground();

		new File(opt.pathTo().toString()).mkdirs();
		//文件网络
		//SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
		isCombinedSearching = opt.isCombinedSearching();
		//opt.globalTextZoom = read.getInt("globalTextZoom",dm.widthPixels>900?50:80);
		opt.getLastMdlibPath();
		bInPeruseMode=opt.getInPeruseMode();
		bInPeruseModeToggleManifested=opt.getInPeruseModeTM();
		if(opt.lastMdlibPath==null || !new File(opt.lastMdlibPath).exists()) {
			opt.lastMdlibPath = opt.pathToMain()+"mdicts";
			new File(opt.lastMdlibPath).mkdirs();
		}
	}

	private void dumpSettiings(){
		Editor putter = null;
		if(webcontentlist.isDirty) {
			putter = opt.defaultReader.edit();
			putter.putInt("BBS",webcontentlist.getPrimaryContentSize());
		}
		if(putter!=null) {
			putter.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.menu, menu);
		showT("onCreateOptionsMenu");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.toolbar_action1:
				// do something
				return true;
			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//removeBlack();
		if(nNeedSaveViewStates) {
			currentDictionary.lvPos=lv.getFirstVisiblePosition();
			if(lv.getChildCount()>=0) currentDictionary.lvPosOff=lv.getChildAt(0).getTop();
			currentDictionary.dumpViewStates();
			nNeedSaveViewStates=false;
			CMN.Log("onPause saving dictionary states");
		}
	}

	private int oldTime=-1;
	private boolean removeBlack() {
		if(mView!=null) {
			WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
			wm.removeView(mView);
			mView=null;
			Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, oldTime);
			return true;
		}
		return false;
	}

	String lastPastedContent = "";
	public Drawer drawerFragment;


	@Override
	protected void onResume() {
		super.onResume();
		if(false) {
			ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData data = cm.getPrimaryClip();
			if(data!=null) {
				ClipData.Item item = data.getItemAt(0);
				String content = item.getText().toString();
				if(!lastPastedContent.equals(content)) {
					callDrawerIconAnima();
					drawerFragment.etAdditional.setText(lastPastedContent = content);
				}
			}
		}
		if(systemIntialized) {
			if(CMN.MainBackground != MainBackground || CMN.GlobalPageBackground!=GlobalPageBackground ) {
				IMPageCover.setTag(false);
				if(PeruseView!=null) PeruseView.IMPageCover.setTag(false);
				GlobalPageBackground=CMN.GlobalPageBackground;
				MainBackground=CMN.MainBackground;
				refreshUIColors();
			}
		}
	}

	void refreshUIColors() {
		boolean isHalo=AppWhite==Color.WHITE;
		int filteredColor = isHalo?MainBackground:ColorUtils.blendARGB(MainBackground, Color.BLACK, ColorMultiplier_Wiget);//CU.MColor(MainBackground,ColorMultiplier);
		viewPager.setBackgroundColor(AppWhite);
		lv2.setBackgroundColor(AppWhite);
		root.setBackgroundColor(filteredColor);


		if(!isHalo) {
			dialog_.setBackgroundResource(R.drawable.popup_shadow_l);
			dialog_.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
			MarginLayoutParams lp = (MarginLayoutParams) cb1.getLayoutParams();
			lp.topMargin=(int) (13*dm.density);
			cb1.setLayoutParams(lp);
		}else {
			dialog_.setBackgroundResource(R.drawable.popup_background3);
			MarginLayoutParams lp = (MarginLayoutParams) cb1.getLayoutParams();
			lp.topMargin=(int) (10*dm.density);
			cb1.setLayoutParams(lp);
		}


		bottombar2.setBackgroundColor(filteredColor);
		widget0.getBackground().setColorFilter(filteredColor, PorterDuff.Mode.SRC_IN);

		filteredColor = isHalo?GlobalPageBackground:ColorUtils.blendARGB(GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
		WHP.setBackgroundColor(filteredColor);
		webSingleholder.setBackgroundColor(filteredColor);
		//showT(Integer.toHexString(filteredColor)+" "+Integer.toHexString(GlobalPageBackground));

	}

	public void animateUIColorChanges() {
		mHandle.removeMessages(331122);
		animator = 0.1f;
		animatorD = 0.15f;
		mHandle.sendEmptyMessage(331122);
		boolean isChecked = AppWhite==Color.BLACK;
		ViewGroup[] holders = new ViewGroup[]{webSingleholder,webholder};
		for(ViewGroup hI:holders)
			for(int i=0;i<hI.getChildCount();i++) {
				View vTmp = hI.getChildAt(i).findViewById(R.id.webviewmy);
				if(vTmp instanceof WebViewmy) {
					WebView wv = (WebView) vTmp;
					wv.evaluateJavascript(isChecked?DarkModeIncantation:DeDarkModeIncantation, null);


					Integer selfAtIdx = IU.parseInt(wv.getTag());
					if(selfAtIdx!=null && selfAtIdx>=0 && selfAtIdx<md.size()) {
						if(md.get(selfAtIdx).bUseInternalBG) {
							int bg = md.get(selfAtIdx).bgColor;
							wv.setBackgroundColor(isChecked?ColorUtils.blendARGB(bg, Color.BLACK, ColorMultiplier_Web2):bg);
						}
					}

				}
			}
		if(PeruseView!=null)
			PeruseView.refreshUIColors();
		try {
			if(pickDictDialog!=null) {
				Object Scrollbar = ScrollCacheField.get(pickDictDialog.mRecyclerView);
				Drawable ScrollbarDrawable = (Drawable) ScrollBarDrawableField.get(Scrollbar);
				ScrollbarDrawable.setColorFilter(isChecked?Color.RED:0x8a666666, PorterDuff.Mode.SRC_IN);
			}

			int targetFastColor = isChecked?0x8a666666:0x8a555555;
			Object FastScroller = FastScrollField.get(lv);
			ImageView ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
			ThumbImage.setColorFilter(targetFastColor);


			FastScroller = FastScrollField.get(mlv1);
			ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
			ThumbImage.setColorFilter(targetFastColor);

			FastScroller = FastScrollField.get(mlv2);
			ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
			ThumbImage.setColorFilter(targetFastColor);


			//FastScroller = FastScrollField.get(lv2);
			//ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
			//ThumbImage.setColorFilter(targetFastColor);
		} catch (Exception e) {
			//CMN.Log(e);
		}

	}

	public class ListViewAdapter extends BasicAdapter {
		//AbsListView.LayoutParams lp;
		//构造
		public ListViewAdapter()
		{
			//lp = new AbsListView.LayoutParams(-1,-1);
			//lp.setMargins(280, 0, 0, 0);
		}
		@Override
		public int getCount() {
			if(md.size()>0 && currentDictionary!=null)
				return (int) currentDictionary.getNumberEntries();
			else
				return 0;
		}

		Flag mflag = new Flag();
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			//return lstItemViews.get(position);
			viewHolder vh;
			String currentKeyText = currentDictionary.getEntryAt(position,mflag);

			if(convertView!=null){
				//标题
				vh=(viewHolder)convertView.getTag();
			}else{
				//标题
				convertView = View.inflate(getApplicationContext(), R.layout.listview_item0, null);
				convertView.setId(R.id.lvitems);
				convertView.setOnClickListener(this);
				convertView.setOnLongClickListener(PDICMainActivity.this);
				//convertView.setLayoutParams(lp);
				vh=new viewHolder();
				vh.title = convertView.findViewById(R.id.text);
				vh.subtitle = convertView.findViewById(R.id.subtext);
				convertView.setTag(vh);
			}

			if( vh.title.getTextColors().getDefaultColor()!=AppBlack) {
				//CMN.Log(Integer.toHexString(vh.title.getTextColors().getDefaultColor())+" "+Integer.toHexString(AppBlack));
				if(AppBlack==Color.WHITE)
					convertView.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				else
					convertView.getBackground().setColorFilter(null);
				vh.title.setTextColor(AppBlack);
			}

			vh.title.setText(currentKeyText);
			if(position==0 && mdict_asset.class==currentDictionary.getClass()) {
				vh.subtitle.setText(Html.fromHtml("<font color='#2B4391'> < "+"欢迎使用平典"+packageName()+" ></font >"));
			}else {
				if(mflag.data!=null)
					vh.subtitle.setText(Html.fromHtml(currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+mflag.data+" ></font >"));
				else
					vh.subtitle.setText(currentDictionary._Dictionary_fName);
			}
			convertView.setTag(R.id.position,position);




			return convertView;
		}

		@Override
		public void SaveVOA() {
			if(opt.getRemPos()) {
				ScrollerRecord pagerec;
				WebViewmy current_webview = currentDictionary.mWebView;
				if(System.currentTimeMillis()-lastClickTime>300)//save our postion
					if((current_webview!=null && !current_webview.isloading) && lastClickedPos>=0 && webSingleholder.getChildCount()!=0) {
						if(currentDictionary.webScale==0) currentDictionary.webScale=dm.density;//sanity check
						//avoyager.set(avoyagerIdx,(int) (current_webview.getScrollY()/(currentDictionary.webScale/dm.density)));

						pagerec = avoyager.get(lastClickedPos);
						if(pagerec==null) {
							pagerec=new ScrollerRecord();
							avoyager.put(lastClickedPos, pagerec);
						}
						pagerec.set(current_webview.getScrollX(), current_webview.getScrollY(),currentDictionary.webScale);
						CMN.Log("回退前暂存位置 ", current_webview.getScrollX(), current_webview.getScrollY(),currentDictionary.webScale);
					}
				lastClickTime=System.currentTimeMillis();
			}
			if(!opt.getHistoryStrategy0() && opt.getHistoryStrategy6() && opt.getHistoryStrategy8()==2)
				historyCon.insertUpdate(currentDictionary.currentDisplaying);
		}

		@Override
		public void ClearVOA() {
			super.ClearVOA();
			if(currentDictionary!=null) {
				//CMN.Log("江河湖海",currentDictionary.expectedPosX,currentDictionary.expectedPos,currentDictionary.webScale);
				if(opt.getRemPos())
					avoyager.put(currentDictionary.lvClickPos, new ScrollerRecord(currentDictionary.expectedPosX,currentDictionary.expectedPos,currentDictionary.webScale));
			}
		}

		int currentDictionaryToken;
		boolean userCLick;

		@Override
		public void onClick(View v) {
			userCLick=true;
			bNeedReAddCon=false;
			lastClickedPosBeforePageTurn=-1;
			super.onClick(v);
		}

		@Override
		public void onItemClick(int pos) {//lv1
			if(bInPeruseModeToggleManifested && bInPeruseMode) {
				String currentDisText = currentDictionary.getEntryAt(pos);
				getPeruseView().data = new ArrayList<>();
				getPeruseView().data.add(adapter_idx);
				for(int i=0;i<md.size();i++) {//联合搜索
					int dIdx=i;
					if(dIdx==adapter_idx) continue;
					mdict mdTmp = md.get(dIdx);
					int idx = mdTmp.lookUp(currentDisText);
					if(idx>=0)
						if(mdict.replaceReg.matcher(mdTmp.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisText)) {
							getPeruseView().data.add(dIdx);
						}
				}
				getPeruseView().TextToSearch = currentDictionary.getEntryAt(pos);
				AttachPeruseView(true);
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);

				return;
			}
			etSearch_ToToolbarMode(1);
			setContentBow(opt.isContentBow());
			if(DBrowser!=null) return;
			lastClickedPosBeforePageTurn = lastClickedPos;
			super.onItemClick(pos);
			ActivedAdapter=this;
			if(pos<-1)
				return;
			//-1放行
			if(pos>=getCount()) {
				lastClickedPos = getCount()-1;
				show(R.string.endendr);
				return;
			}


			if(currentDictionary.hashCode()!=currentDictionaryToken) {
				currentDictionaryToken=currentDictionary.hashCode();
				ClearVOA();
			}
			currentDictionary.lvClickPos=pos;

			toolbar.getMenu().findItem(R.id.toolbar_action2).setVisible(false);//Ser
			iItem_PerwordSearch.setVisible(false);
			iItem_PeruseMode.setVisible(false);
			iItem_aPageRemember.setVisible(true);//翻忆
			iItem_ComResNotifier.setVisible(false);
			//iItem_HintSerMode.setVisible(false);
			//iItem_TintWildRes.setVisible(false);
			iItem_PickDict.setVisible(true);
			iItem_PickSet.setVisible(false);
			iItem_SaveBookMark.setVisible(true);


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


			currentDictionary.initViewsHolder(PDICMainActivity.this);
			WebViewmy current_webview = currentDictionary.mWebView;
			float desiredScale=-1;
			if(opt.getRemPos()) {
				ScrollerRecord pagerec;
				if(System.currentTimeMillis()-lastClickTime>300)//save our postion
					OUT:
							if((current_webview!=null && !current_webview.isloading) && lastClickedPosBeforePageTurn>=0 && webSingleholder.getChildCount()!=0) {
								if(currentDictionary.webScale==0) currentDictionary.webScale=dm.density;//sanity check
								//avoyager.set(avoyagerIdx,(int) (current_webview.getScrollY()/(currentDictionary.webScale/dm.density)));

								pagerec = avoyager.get(lastClickedPosBeforePageTurn);
								if(pagerec==null) {
									if(current_webview.getScrollX()!=0 || current_webview.getScrollY()!=0 ||currentDictionary.webScale!=mdict.def_zoom) {
										pagerec=new ScrollerRecord();
										avoyager.put(lastClickedPosBeforePageTurn, pagerec);
									}else
										break OUT;
								}

								pagerec.set(current_webview.getScrollX(), current_webview.getScrollY(),currentDictionary.webScale);
								//showT("保存位置");
								CMN.Log("保存位置 "+ current_webview.getScrollY());
							}

				lastClickTime=System.currentTimeMillis();

				pagerec = avoyager.get(pos);
				if(pagerec!=null) {
					currentDictionary.expectedPos = pagerec.y;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
					currentDictionary.expectedPosX = pagerec.x;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
					desiredScale=pagerec.scale;
					CMN.Log(avoyager.size()+"~"+pos+"~取出旧值"+currentDictionary.expectedPos+" scale:"+avoyager.get(pos).scale);
				}else {
					currentDictionary.expectedPos=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
					currentDictionary.expectedPosX=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				}
				//showT(""+currentDictionary.expectedPos);
			}
			else{
				currentDictionary.expectedPos=0;
				currentDictionary.expectedPosX=0;
			}

			if(opt.getInheritePageScale())
				desiredScale=currentDictionary.webScale;

			contentview.setVisibility(View.VISIBLE);
			currentDictionary.clearWebview();

			lastClickedPos = pos;
			if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				etSearch.clearFocus();
			}

			ViewGroup somp = (ViewGroup) contentview.getParent();//sanity check
			if(somp!=main){
				if(somp!=null) somp.removeView(contentview);
				main.addView(contentview);
			}

			//webholder.addView(md.rl);
			ViewGroup someView = currentDictionary.rl;
			if(someView.getParent()!=webSingleholder) {
				if(someView.getParent()!=null) ((ViewGroup)someView.getParent()).removeView(someView);
				webSingleholder.addView(currentDictionary.rl);
			}
			if(webSingleholder.getChildCount()>1) {
				for(int i=webSingleholder.getChildCount()-1;i>=0;i--)
					if(webSingleholder.getChildAt(i)!=currentDictionary.rl) webSingleholder.removeViewAt(i);
			}

			currentDictionary.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
			currentDictionary.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

			layoutScrollDisabled=true;
			currentDictionary.renderContentAt(desiredScale,adapter_idx,null,lastClickedPos);


			currentKeyText = currentDictionary.currentDisplaying;
			String key = currentKeyText;
			if(ActivedAdapter==adaptermy) {
				int pos1 = currentDictionary.currentPos;
				while(pos1-1>=0 && currentDictionary.getEntryAt(pos1-1).equals(key)) {
					pos1--;
				}
				pos1 = currentDictionary.currentPos - pos1;
				if(pos1>0) {
					for(int i=0;i<pos1;i++)
						key += "\n";
				}
				//CMN.show("pos"+pos1);
			}

			decorateContentviewByKey(null,key);

			if(!opt.getHistoryStrategy0() && opt.getHistoryStrategy6()) {
				if(userCLick)
					historyCon.insertUpdate(key);
				else if(opt.getHistoryStrategy8()==0)
					historyCon.insertUpdate(key);
			}
			CMN.lastHisLexicalEntry = -1;
			//showT("查时: "+(System.currentTimeMillis()-stst));

			bWantsSelection=true;
			userCLick=false;
		}
	}

	public class ListViewAdapter2 extends BasicAdapter {
		int itemId = R.layout.listview_item0;
		public ListViewAdapter2(int resId)
		{
			this();
			itemId = resId;
		}
		//构造
		public ListViewAdapter2()
		{
			combining_search_result = new resultRecorderDiscrete(PDICMainActivity.this);
		}
		@Override
		public int getCount() {
			return combining_search_result.size();
		}

		public int expectedPos;
		private boolean userCLick;
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			//return lstItemViews.get(position);
			viewHolder vh;

			CharSequence currentKeyText = combining_search_result.getResAt(position);

			if(convertView!=null){
				//标题
				vh=(viewHolder)convertView.getTag();
			}else{
				//标题
				convertView = View.inflate(getApplicationContext(), itemId, null);
				convertView.setId(R.id.lvitems);
				//convertView.setOnClickListener(this);
				//convertView.setOnLongClickListener(MainActivity.this);
				vh=new viewHolder();
				vh.title = convertView.findViewById(R.id.text);
				vh.subtitle = convertView.findViewById(R.id.subtext);
				convertView.setTag(vh);
				if(itemId==R.layout.listview_item1)
					vh.subtitle.setTag(convertView.findViewById(R.id.counter));
			}

			if(combining_search_result.dictIdx>=md.size()) return convertView;//不要Crash哇

			vh.title.setText(currentKeyText);
			if( vh.title.getTextColors().getDefaultColor()!=AppBlack) {
				//CMN.Log(Integer.toHexString(vh.title.getTextColors().getDefaultColor())+" "+Integer.toHexString(AppBlack));
				if(AppBlack==Color.WHITE)
					convertView.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				else
					convertView.getBackground().setColorFilter(null);
				vh.title.setTextColor(AppBlack);
			}
			mdict _currentDictionary = md.get(combining_search_result.dictIdx);
			if(combining_search_result.mflag.data!=null)
				vh.subtitle.setText(Html.fromHtml(_currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+combining_search_result.mflag.data+" ></font >"));
			else
				vh.subtitle.setText(_currentDictionary._Dictionary_fName);
			if(combining_search_result.getClass()==resultRecorderCombined.class)
				((TextView)vh.subtitle.getTag()).setText(((resultRecorderCombined)combining_search_result).count);
			//convertView.setTag(R.id.position,position);
			return convertView;
		}
		@Override
		public void shutUp() {
			combining_search_result.shutUp();
			notifyDataSetChanged();
		}

		@Override
		public void SaveVOA() {
			ScrollerRecord pagerec;
			OUT:
			if(System.currentTimeMillis()-lastClickTime>400) {//save our postion
				if(this==adaptermy2) {
					if(lastClickedPos>=0) {
						//avoyager.set(avoyagerIdx, WHP.getScrollY());
						pagerec = avoyager.get(lastClickedPos);
						if(pagerec==null) {
							if(WHP.getScrollY()!=0) {
								pagerec=new ScrollerRecord();
								avoyager.put(lastClickedPos, pagerec);
							}else
								break OUT;
						}
						pagerec.set(0, WHP.getScrollY(), 1);
						//showT("保存位置");
					}
				}else {
					mdict mdtmp = md.get(combining_search_result.getDictsAt(lastClickedPos).get(0));
					if((mdtmp.mWebView!=null) && lastClickedPos>=0 && webSingleholder.getChildCount()!=0) {
						//ADA.avoyager.get(ADA.avoyagerIdx).set(mdtmp.mWebView.getScrollX(), mdtmp.mWebView.getScrollY(), mdtmp.webScale);
						pagerec = avoyager.get(lastClickedPos);
						if(pagerec==null) {
							if(mdtmp.mWebView.getScrollX()!=0 || mdtmp.mWebView.getScrollY()!=0 ||mdtmp.mWebView.webScale!=mdict.def_zoom) {
								pagerec=new ScrollerRecord();
								avoyager.put(lastClickedPos,pagerec);
							}else
								break OUT;
						}
						pagerec.set(mdtmp.mWebView.getScrollX(), mdtmp.mWebView.getScrollY(), mdtmp.webScale);
					}
				}
			}
			lastClickTime=System.currentTimeMillis();
			boolean Kustice = (this!=adaptermy2 &&opt.getHistoryStrategy2()) || (this==adaptermy2 && opt.getHistoryStrategy6());
			if(!opt.getHistoryStrategy0() && Kustice && opt.getHistoryStrategy8()==2)
				historyCon.insertUpdate(currentKeyText);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			//CMN.show("onItemClick00");
			main_progress_bar.setVisibility(View.GONE);
			lastClickedPosBeforePageTurn=-1;
			bNeedReAddCon=false;
			userCLick=true;
			super.onItemClick(parent, view, pos, id);
		}


		@Override
		public void onItemClick(int pos){//lv2 mlv1 mlv2
			WHP.touchFlag.first=true;
			if(bInPeruseModeToggleManifested && bInPeruseMode) {
				getPeruseView().data = combining_search_result.getDictsAt(pos);
				getPeruseView().TextToSearch = combining_search_result.getResAt(pos).toString();

				AttachPeruseView(true);
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				return;
			}
			etSearch_ToToolbarMode(1);
			setContentBow(opt.isContentBow());
			//if(true) return;
			if(DBrowser!=null) return;
			//CMN.show("onItemClick");

			lastClickedPosBeforePageTurn = lastClickedPos;

			super.onItemClick(pos);
			ActivedAdapter=this;
			if(pos<0 )
				return;
			if(pos>=getCount()) {
				lastClickedPos = getCount()-1;
				show(R.string.endendr);
				return;
			}
			toolbar.getMenu().findItem(R.id.toolbar_action2).setVisible(false);//Ser
			iItem_PerwordSearch.setVisible(false);
			iItem_PeruseMode.setVisible(false);
			iItem_FolderAll.setVisible(true);//折叠
			iItem_aPageRemember.setVisible(true);//翻忆
			iItem_ComResNotifier.setVisible(false);
			iItem_HintSerMode.setVisible(false);
			iItem_TintWildRes.setVisible(false);
			iItem_PickDict.setVisible(false);
			iItem_PickSet.setVisible(this==adaptermy2);
			iItem_SaveBookMark.setVisible(this!=adaptermy2);


			if(this==adaptermy2) {
				if(WHP.getVisibility()!=View.VISIBLE)WHP.setVisibility(View.VISIBLE);
				webholder.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
				if(webSingleholder.getVisibility()==View.VISIBLE) {
					if(webSingleholder.getChildCount()!=0)
						webSingleholder.removeAllViews();
					webSingleholder.setVisibility(View.GONE);
				}
			}else {
				if(webSingleholder.getVisibility()!=View.VISIBLE)webSingleholder.setVisibility(View.VISIBLE);
				if(WHP.getVisibility()==View.VISIBLE) {
					if(webholder.getChildCount()!=0)
						webholder.removeAllViews();
					WHP.setVisibility(View.GONE);
				}
			}

			int targetVis = View.VISIBLE;
			if(this!=adaptermy2) {
				targetVis = View.GONE;
			}else
				locateNaviIcon(widget13,widget14);
			if(widget14.getVisibility()!=targetVis) {
				widget13.setVisibility(targetVis);
				widget14.setVisibility(targetVis);
			}
			//showT(""+currentDictionary.expectedPos);
			ScrollerRecord pagerec;
			if(this==adaptermy2) {//save our postion
				OUT:
				if(((resultRecorderCombined)combining_search_result).scrolled && lastClickedPosBeforePageTurn>=0 && System.currentTimeMillis()-lastClickTime>300) {
					//avoyager.set(avoyagerIdx, WHP.getScrollY());
					pagerec = avoyager.get(lastClickedPosBeforePageTurn);
					if(pagerec==null) {
						if(WHP.getScrollY()!=0) {
							pagerec=new ScrollerRecord();
							avoyager.put(lastClickedPosBeforePageTurn, pagerec);
						}else
							break OUT;
					}
					pagerec.set(0, WHP.getScrollY(), 1);
					//showT("保存位置");
				}

				lastClickTime=System.currentTimeMillis();

				pagerec = avoyager.get(pos);
				if(pagerec!=null) {
					combining_search_result.expectedPos=pagerec.y;
					//currentDictionary.mWebView.setScrollY(currentDictionary.expectedPos);
					//showT("取出旧值"+combining_search_result.expectedPos
				}else {
					combining_search_result.expectedPos=0;
				}

			}else {

			}


			contentview.setVisibility(View.VISIBLE);
			if(this!=adaptermy2)
				webholder.removeAllViews();

			lastClickedPos = pos;
			if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				etSearch.clearFocus();
			}

			ViewGroup somp = (ViewGroup) contentview.getParent();//sanity check
			if(somp!=main){
				if(somp!=null) somp.removeView(contentview);
				main.addView(contentview);
			}

			combining_search_result.renderContentAt(lastClickedPos, PDICMainActivity.this,this);//webholder


			favoriteCon.prepareContain();
			if(favoriteCon.contains(currentKeyText = combining_search_result.getResAt(pos).toString())) {
				if(star_ic==null) {
					star_ic = getResources().getDrawable(R.drawable.star_ic_solid);
					star = favoriteBtn.getDrawable();
				}
				favoriteBtn.setImageDrawable(star_ic);
			}else if(star!=null)
				favoriteBtn.setImageDrawable(star);

			if(!opt.getHistoryStrategy0()) {
				boolean Kustice = (this!=adaptermy2 &&opt.getHistoryStrategy2()) || (this==adaptermy2 && opt.getHistoryStrategy6());
				if(Kustice) {
					if(userCLick)
						historyCon.insertUpdate(currentKeyText);
					else {
						if(opt.getHistoryStrategy8()==1)
							historyCon.insertUpdate(currentKeyText);
					}
				}
			}

			CMN.lastHisLexicalEntry = -1;
			userCLick=false;
			bWantsSelection=true;
			//showT("查时: "+(System.currentTimeMillis()-stst));
		}
	}

	static class viewHolder{
		private TextView title;
		private TextView subtitle;
	}



	@Override
	public void onClick(View v) {
		layoutScrollDisabled=false;
		cancleSnack();
		OUT:
		if(DBrowser!=null) {
			switch(v.getId()) {
				case R.id.browser_widget7: break OUT;
				case R.id.browser_widget8:
					DBrowser.toggleFavor();
					break;
				case R.id.browser_widget13:
				case R.id.browser_widget14:
				case R.id.browser_widget9: break OUT;
				case R.id.browser_widget10:
					if(ActivedAdapter instanceof com.knziha.plod.PlainDict.PeruseView.ListViewAdapter) break OUT;
					DBrowser.goBack();
					break;
				case R.id.browser_widget11:
					if(ActivedAdapter instanceof com.knziha.plod.PlainDict.PeruseView.ListViewAdapter) break OUT;
					DBrowser.goQiak();
					break;
			}
			return;
		}
		CheckableImageView cb;
		switch(v.getId()) {
			case R.id.browser_widget0:
				if(main.getChildCount()!=0) return;
				String msg;
				if(bInPeruseMode) {
					widget0.setImageResource(R.drawable.peruse_ic);
					msg="已取消 翻阅模式";
				}else {
					widget0.setImageResource(R.drawable.peruse_ic_on);
					msg="翻阅模式";
				}
				opt.setInPeruseMode(bInPeruseMode=!bInPeruseMode);
				opt.putFirstFlag();
				snack = TSnackbar.makeraw(main_succinct, msg,TSnackbar.LENGTH_SHORT);
				//snackbar.getView().getLayoutParams().height=(int) (dm.density*30);
				//snackbar.getTextView().setTextSize(3);
				snack.show();
				break;
			case R.id.browser_widget1:
				if(dialogHolder.getVisibility()==View.VISIBLE) {
					dialogHolder.setVisibility(View.GONE);
					if(pickDictDialog!=null) if(pickDictDialog.isDirty)  {opt.putFirstFlag();pickDictDialog.isDirty=false;}
					break;
				}
				if(!isFragInitiated) {
					FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();
					pickDictDialog = new DictPicker(this);
					transaction.add(R.id.dialog_, pickDictDialog);
					transaction.commit();
					isFragInitiated=true;
					//pickDictDialog.mRecyclerView.scrollToPosition(adapter_idx);
				}
				else//没办法..
					pickDictDialog.refresh();
				break;
			case R.id.toolbar_action1:
				opt.setCombinedSearching(isCombinedSearching = !isCombinedSearching);
				iItem_ComResNotifier.setVisible(isCombinedSearching);
				opt.putFirstFlag();
				if(isCombinedSearching){
					if(contentview.getParent()==main)
						adaptermy2.currentKeyText=null;
					toolbar.getMenu().findItem(R.id.toolbar_action1).setIcon((getResources().getDrawable(R.drawable.ic_btn_multimode)));
					lv2.setVisibility(View.VISIBLE);
				}else{
					if(contentview.getParent()==main)
						adaptermy.currentKeyText=null;
					toolbar.getMenu().findItem(R.id.toolbar_action1).setIcon((getResources().getDrawable(R.drawable.ic_btn_siglemode)));
					lv2.setVisibility(View.GONE);
				}
				if(opt.auto_seach_on_switch)
					tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
				break;
			case R.id.ivDeleteText:
				if((etSearch_toolbarMode&2)==0) {//delete
					String SearchTmp = etSearch.getText().toString().trim();
					if(SearchTmp.equals("")) {
						ivDeleteText.setVisibility(View.GONE);
					}else {
						lastEtString=SearchTmp;
						etSearch.setText(null);
						etSearch_ToToolbarMode(4);
					}
				}else {//undo
					etSearch.setText(lastEtString);
					//etSearch_ToToolbarMode(3);
				}
				break;
			case R.id.ivBack:
				if((etSearch_toolbarMode&1)==0) {//search
					if(CurrentViewPage==1) {//viewPager
						if(etSearch.getText().toString().trim().length()>0) {
							bIsFirstLaunch=true;
							tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
						}
						if(!opt.getHistoryStrategy0()) {
							if(!etSearch.getText().toString().trim().contains("<分>")) {
								historyCon.insertUpdate(etSearch.getText().toString().trim());
							}else if(opt.getHistoryStrategy1()) {
								historyCon.insertUpdate(etSearch.getText().toString().trim());
							}
						}
					}else
						etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
					mDrawerLayout.closeDrawer(GravityCompat.START);
				}else {//back
					onKeyDown(KeyEvent.KEYCODE_BACK, BackEvent);
					etSearch_ToToolbarMode(0);
				}
				break;
			case R.id.browser_widget2:
				if(d!=null) {
					d.dismiss();d=null;
				}
				showChooseSetDialog();
				break;
			case R.id.browser_widget3:
				fetchAddtionalDrawbles();
				if(CurrentViewPage==0){
					CurrentViewPage = 1;
					viewPager.setCurrentItem(1);
				}
				else{
					CurrentViewPage = 0;
					viewPager.setCurrentItem(0);
				}
				lastKeyword = etSearch.getText().toString();
				break;
			case R.id.browser_widget4:
				fetchAddtionalDrawbles();
				if(CurrentViewPage==2){
					CurrentViewPage = 1;
					viewPager.setCurrentItem(1);
				}
				else{
					CurrentViewPage = 2;
					viewPager.setCurrentItem(2);
				}
				lastFullKeyword=etSearch.getText().toString();
				break;
			case R.id.browser_widget5:
				if(main.getChildCount()!=0) return;
				if(DBrowser==null) {
					DBrowser = new DBroswer();
					FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();
					transaction.add(R.id.mainF, DBrowser);
					transaction.commit();
				}
				break;
			case R.id.browser_widget6:
				if(main.getChildCount()!=0) return;
				if(DBrowser==null) {
					DBrowser = new DHBroswer();
					FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();
					transaction.add(R.id.mainF, DBrowser);
					transaction.commit();
				}
				break;
			case R.id.browser_widget7:
				//if(currentDictionary.mWebView!=null) {
				//currentDictionary.mWebView.evaluateJavascript("ssc=document.getElementsByTagName('style');"
				////+ "console.log(456);for(var i=0;i<ssc.length;i++){console.log(ssc[i].innerHTML);if(ssc[i].innerHTML){console.log(ssc[i].styleSheet.cssText);console.log(new RegExp('^'+'html {-webkit-filter').test(ssc[i].styleSheet.cssText));if(new RegExp('^'+'html {-webkit-filter').test(ssc[i].styleSheet.cssText)){console.log(123);document.getElementsByTagName('head')[0].removeChild(ssc[i]);break;}}}", null);
				//+ "ssc=document.getElementsByTagName('style');for(var i=0;i<ssc.length;i++){if(ssc[i].innerHTML){if(new RegExp('^'+'html {-webkit-filter').test(ssc[i].innerHTML)){document.getElementsByTagName('head')[0].removeChild(ssc[i]);break;}}}", null);
				//return;
				//}
				exitTime=0;
				mDrawerLayout.closeDrawer(GravityCompat.START);
				if(drawerFragment.d!=null) {
					drawerFragment.d.dismiss();
				}
				if(PeruseView!=null && PeruseView.getView().getParent() !=null) {
					if(contentview.getParent()==PeruseView.slp) {
						PeruseView.slp.removeView(contentview);
						PeruseView.cvpolicy=false;
						break;
					}
					if(contentview.getParent()==PeruseView.mlp) {
						PeruseView.mlp.removeView(contentview);
						PeruseView.cvpolicy=false;
						break;
					}
				}
				onKeyDown(KeyEvent.KEYCODE_BACK, BackEvent);
				break;
			case R.id.browser_widget8://favorite
				ImageView favoriteBtn=(ImageView) v;
				if(star_ic==null) {
					star_ic = getResources().getDrawable(R.drawable.star_ic_solid);
					star = favoriteBtn.getDrawable();
				}
				favoriteBtn.setImageDrawable(star);
				String key = ActivedAdapter.currentKeyText;
				if(ActivedAdapter==adaptermy) {
					int pos = currentDictionary.currentPos;
					while(pos-1>=0 && currentDictionary.getEntryAt(pos-1).equals(key)) {
						pos--;
					}
					pos = currentDictionary.currentPos - pos;
					if(pos>0) {
						for(int i=0;i<pos;i++)
							key += "\n";
					}
					//CMN.show("pos"+pos);
				}
				favoriteCon.prepareContain();
				if(favoriteCon.contains(key)) {
					favoriteCon.remove(key);
					favoriteBtn.setImageDrawable(star);
					show(R.string.removed);
				}else {
					favoriteCon.insert(key);
					favoriteBtn.setImageDrawable(star_ic);
					show(R.string.added);
				}
				break;
			case R.id.browser_widget9://view outlinexxx
				if(ActivedAdapter instanceof com.knziha.plod.PlainDict.PeruseView.ListViewAdapter) {
					v.performLongClick();
					break;
				}
				if((DBrowser!=null && opt.isCombinedSearching()) || ActivedAdapter==adaptermy2) {
					imm.hideSoftInputFromWindow(main.getWindowToken(),0);
					final resultRecorderCombined res;
					int idx = 0;
					if(DBrowser!=null)
						res = DBrowser.rec;
					else {
						res = (resultRecorderCombined) adaptermy2.combining_search_result;
						idx = adaptermy2.lastClickedPos;
						if(idx<0 || idx>=res.list().size())
							return;
					}

					additiveMyCpr1 contentIndexs = res.list().get(idx);
					List<Integer> vals = (List<Integer>)contentIndexs.value;
					final CharSequence[] items = new CharSequence[webholder.getChildCount()];//Build.VERSION.SDK_INT>=22
					int c=0;
					int totalHeight=0;
					int selectedPos=-1;
					final int currentHeight=((ScrollView)webholder.getParent()).getScrollY();
					for(int i=0;i<vals.size();i+=2) {
						int lastIdx = vals.get(i);
						i+=2;
						while(i<vals.size() && lastIdx==vals.get(i))
							i+=2;
						i-=2;
						if(selectedPos==-1) {
							totalHeight+=webholder.getChildAt(c).getMeasuredHeight();
							if(totalHeight>currentHeight)
								selectedPos=c;
						}
						mdict mdTmp = md.get(lastIdx);
						if(mdTmp.cover!=null) {
							mdTmp.cover.setBounds(0, 0, 50, 50);
							SpannableStringBuilder ssb = new SpannableStringBuilder("| ").append(mdTmp._Dictionary_fName);
							ssb.setSpan(new ImageSpan(mdTmp.cover), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							items[c] = ssb;
						}else
							items[c] = mdTmp._Dictionary_fName;
						c++;
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(this);//,R.style.DialogStyle
					builder.setTitle("跳转");
					builder.setSingleChoiceItems(items, 0,
							(dialog, pos) -> {
								int totalHeight1 =0;
								for(int i=0;i<pos;i++) {
									totalHeight1 +=webholder.getChildAt(i).getHeight();
								}

								//((ScrollView)webholder.getParent()).setScrollY(totalHeight);
								((ScrollView)webholder.getParent()).smoothScrollTo(0, totalHeight1);
								d.dismiss();
							}).setOnDismissListener(dialog -> {
						for(int idxTmp=md.size()-1;idxTmp>=0;idxTmp--) {
							mdict mdTmp = md.get(idxTmp);
							if(mdTmp.cover!=null)
								mdTmp.cover.setBounds(0, 0, mdTmp.cover.getIntrinsicWidth(),mdTmp.cover.getIntrinsicHeight());
						}
					});
					AlertDialog dTmp = builder.create();
					d=dTmp;
					dTmp.show();
					dTmp.setCanceledOnTouchOutside(true);
					dTmp.getWindow().setLayout((int) (dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor)), -2);

					if(true) {//AppBlack==Color.WHITE
						//d.setTitle(Html.fromHtml("<span style='color:#ffffff;'>"+getResources().getString(R.string.loadconfig)+"</span>"));
						if(AppBlack==Color.WHITE) {
							dTmp.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
							dTmp.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_d);
							View tv = dTmp.getWindow().findViewById(Resources.getSystem().getIdentifier("alertTitle","id", "android"));
							if(tv instanceof TextView)((TextView) tv).setTextColor(Color.WHITE);
						}else
							dTmp.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);

						dTmp.getListView().setAdapter(new ArrayAdapter<CharSequence>(getApplicationContext(),
								R.layout.singlechoice_w, android.R.id.text1, Arrays.asList(items)) {
							@NonNull
							@Override
							public View getView(int position, View convertView,
												@NonNull ViewGroup parent) {
								View ret =  super.getView(position, convertView, parent);
								ret.setMinimumHeight((int) getResources().getDimension(R.dimen._50_));
								CheckedTextViewmy tv;
								if(ret.getTag()==null)
									ret.setTag(tv = ret.findViewById(android.R.id.text1));
								else
									tv = (CheckedTextViewmy)ret.getTag();
								if(AppBlack==Color.WHITE)
									tv.setTextColor(Color.WHITE);
								else
									tv.setTextColor(Color.BLACK);
								tv.setText(items[position]);

								return ret;
							}
						});
						if(selectedPos!=-1) {
							dTmp.getListView().setSelection(selectedPos);
							dTmp.getListView().setItemChecked(selectedPos, true);
						}
						//d.getWindow().getDecorView().setBackgroundResource(R.drawable.popup_shadow_l);
						//d.getWindow().getDecorView().getBackground().setColorFilter(GlobalOptions.NEGATIVE);
					}
					//d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
				}else
					showX(R.string.try_longpress,0);
				break;
			case R.id.browser_widget13:
			case R.id.browser_widget14:
				boolean is_14=v.getId()==R.id.browser_widget14;
				final int currentHeight=((ScrollView)webholder.getParent()).getScrollY();
				int totalHeight=0;
				for(int i=0;i<webholder.getChildCount();i++) {
					totalHeight+=webholder.getChildAt(i).getHeight();
					if(totalHeight+(is_14?1:0)>currentHeight) {
						if(is_14)
							totalHeight-=webholder.getChildAt(i).getHeight();
						break;
					}
				}

				//((ScrollView)webholder.getParent()).setScrollY(totalHeight);
				((ScrollView)webholder.getParent()).smoothScrollTo(0, totalHeight);
				//CMN.show(""+is_14);
				break;
			case R.id.browser_widget10:
			case R.id.browser_widget11://左zuo
				//if(((ScrollViewmy)WHP).touchFlag!=null)((ScrollViewmy)WHP).touchFlag.first=true;
				//adaptermy2.combining_search_result.expectedPos=0;
				//webholder.removeOnLayoutChangeListener(((resultRecorderCombined)adaptermy2.combining_search_result).OLCL);
				if(ActivedAdapter==null) break;//sanity check
				layoutScrollDisabled=false;
				int delta = (v.getId()==R.id.browser_widget10?-1:1);
				if(false && ActivedAdapter==adaptermy) {
					int pos = adaptermy.lastClickedPos+delta;
					if(pos<-1) {
						show(R.string.coverr);
						return;
					}//-1放行
					if(pos>=adaptermy.getCount()) {
						adaptermy.lastClickedPos = adaptermy.getCount()-1;
						show(R.string.endendr);
						return;
					}
					if(currentDictionary.try_goBackOrForward(adapter_idx, pos)) {
						adaptermy.lastClickedPos+=delta;
						break;
					}
				}
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				int toPos = ActivedAdapter.lastClickedPos+delta;
				if(toPos<-1) {
					show(R.string.coverr);
					break;
				}
				if(toPos==-1 && ActivedAdapter instanceof ListViewAdapter2) {
					show(R.string.toptopr);
					break;
				}
				if(CurrentViewPage==1){//count==4
					if(lv2.getVisibility()==View.VISIBLE){
						for(int i=0;i<md.size();i++){
							md.get(i).clearWebview();
						}
					}else{
						webholder.removeAllViews();
						currentDictionary.clearWebview();
					}
				}else{
					for(int i=0;i<md.size();i++){
						md.get(i).clearWebview();
					}
				}
				//Log.e("browser_widget10","browser_widget10"+ActivedAdapter.lastClickedPos);
				//webholder.removeViews(1, webholder.getChildCount()-1);
				ActivedAdapter.onItemClick(toPos);
				break;
			case R.id.browser_widget12:
				if(currentDictionary!=null) {
					boolean played=false;
					if(currentDictionary.mdd!=null) {
						String skey = currentDictionary.currentDisplaying+".mp3";
						int idx = currentDictionary.mdd.lookUp(skey);
						if(idx!=-1) {
							currentDictionary.mWebView.evaluateJavascript("var audio = new Audio(\""+skey+"\");audio.play();", null);
							played=true;
						}
						if(!played) {
							Log.e("dsa_evaluateJavascript","asd");
							currentDictionary.mWebView.evaluateJavascript("(function(){var hrefs = document.getElementsByTagName('a'); for(var i=0;i<hrefs.length;i++){ if(hrefs[i].attributes['href']){ if(hrefs[i].attributes['href'].value.indexOf('sound')!=-1){ hrefs[i].click(); return 10; } } }return null;})();", value -> {
								if(!value.equals("10")) {
									showT("找不到音频："+currentDictionary.currentDisplaying);
								}
							});
						}
					}
				}
				break;
			case R.id.cb1://搜索词典
				etSearch.clearFocus();
				if(searchbar==null) {
					searchbar = (Toolbar) getLayoutInflater().inflate(R.layout.searchbar, null);
					searchbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
					searchbar.setNavigationOnClickListener(v1 -> {
						searchbar.setVisibility(View.GONE);
						imm.hideSoftInputFromWindow(((EditText) searchbar.findViewById(R.id.etSearch)).getWindowToken(),0);
					});

					View vTmp = searchbar.getChildAt(searchbar.getChildCount()-1);
					if(vTmp!=null && vTmp.getClass()==AppCompatImageButton.class) {
						AppCompatImageButton NavigationIcon = (AppCompatImageButton) vTmp;
						MarginLayoutParams lp = (MarginLayoutParams) NavigationIcon.getLayoutParams();
						//lp.setMargins(-10,-10,-10,-10);
						lp.width=(int) (45*dm.density);
						NavigationIcon.setLayoutParams(lp);
					}

					searchbar.setContentInsetsAbsolute(0, 0);
					searchbar.setLayoutParams(toolbar.getLayoutParams());
					searchbar.setBackgroundColor(MainBackground);
					searchbar.findViewById(R.id.recess).setOnClickListener(v12 -> {
						pickDictDialog.SetSearchIncantation(((EditText) searchbar.findViewById(R.id.etSearch)).getText().toString());
						int fvp = pickDictDialog.lman.findFirstVisibleItemPosition();
						int sep = md.size()-fvp;
						boolean found=false;
						for(int i=0,j;i<md.size();i++) {
							if(i>=sep) j=i-sep;
							else j=i+fvp;
							if(pickDictDialog.SearchPattern.matcher(md.get(j)._Dictionary_fName).find()) {
								pickDictDialog.lman.scrollToPositionWithOffset(j, (int) (45*dm.density/3));
								found=true;
								break;
							}
						}
						if(!found) showT("什么都没有找到");
						imm.hideSoftInputFromWindow(searchbar.findViewById(R.id.etSearch).getWindowToken(), 0);
						pickDictDialog.adapter().notifyDataSetChanged();
					});
					searchbar.findViewById(R.id.forward).setOnClickListener(v13 -> {
						pickDictDialog.SetSearchIncantation(((EditText) searchbar.findViewById(R.id.etSearch)).getText().toString());
						int fvp = pickDictDialog.lman.findFirstCompletelyVisibleItemPosition();
						int sep = md.size()-fvp-1;
						boolean found=false;
						for(int i=md.size()-1,j;i>=0;i--) {
							if(i>=sep) j=i-sep;
							else j=i+fvp;
							if(pickDictDialog.SearchPattern.matcher(md.get(j)._Dictionary_fName).find()) {
								pickDictDialog.lman.scrollToPositionWithOffset(j, (int) (45*dm.density/3));
								found=true;
								break;
							}
						}
						if(!found) showT("什么都没有找到");
						imm.hideSoftInputFromWindow(searchbar.findViewById(R.id.etSearch).getWindowToken(), 0);
						pickDictDialog.adapter().notifyDataSetChanged();
					});
					searchbar.findViewById(R.id.ivDeleteText).setOnClickListener(v14 -> {
						pickDictDialog.SearchIncantation = null;
						((EditText) searchbar.findViewById(R.id.etSearch)).setText("");
						imm.hideSoftInputFromWindow(searchbar.findViewById(R.id.etSearch).getWindowToken(), 0);
						pickDictDialog.adapter().notifyDataSetChanged();
					});
					dialogHolder.addView(searchbar);

					searchbar.findViewById(R.id.etSearch).requestFocus();
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				}else {
					if(searchbar.getVisibility()==View.VISIBLE) {
						searchbar.setVisibility(View.GONE);
						imm.hideSoftInputFromWindow(searchbar.findViewById(R.id.etSearch).getWindowToken(),0);
					}else {
						searchbar.setVisibility(View.VISIBLE);
						searchbar.findViewById(R.id.etSearch).requestFocus();
						imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					}
				}

				break;
			case R.id.cb3:
				cb = (CheckableImageView)v;
				cb.toggle();
				opt.setPicDictAutoSer(cb.isChecked());
				if(pickDictDialog!=null) pickDictDialog.isDirty=true;
				break;
			case R.id.cb2:
				cb = (CheckableImageView)v;
				cb.toggle();
				opt.setPinPicDictDialog(cb.isChecked());
				if(pickDictDialog!=null) pickDictDialog.isDirty=true;
				break;
		}
	}

	Toolbar searchbar;







	public com.knziha.plod.PlainDict.PeruseView getPeruseView() {
		if(PeruseView==null) {
			PeruseView = new PeruseView();
			PeruseView.spsubs = opt.defaultReader.getFloat("spsubs", 0.706f);
			PeruseView.dm = dm;
			PeruseView.density = dm.density;
		}
		return PeruseView;
	}

	public void AttachPeruseView(boolean bRefresh) {
		if(PeruseView==null) return;
		if(!PeruseView.isAdded()) {
			PeruseView.bCallViewAOA=true;
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(R.id.mainF, PeruseView);
			transaction.commit();
		}else if(PeruseView.getView().getParent()==null){
			main.addView(PeruseView.getView());
			PeruseView.onViewAttached(this,bRefresh);
		}
	}











	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			case R.id.browser_widget0:
				if(getPeruseView().data==null)
					getPeruseView().data = new ArrayList<>();
				//getPeruseView().TextToSearch = currentDictionary.getEntryAt(pos);
				AttachPeruseView(false);
				break;
			case R.id.browser_widget5:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.pickfavor);
				builder.setPositiveButton(R.string.newfc, null);
				builder.setNeutralButton(R.string.delete, null);
				builder.setItems(new String[] {},null);
				final AlertDialog d=builder.create();
				d.show();
				final DArrayAdapter ada = new DArrayAdapter(this,R.layout.drawer_list_item,R.id.text1, new ArrayList<>());
				d.getListView().setAdapter(ada);
				d.getListView().setOnItemClickListener((parent, view, position, id) -> {
					ada.setSelection(position);
					favoriteCon.close();
					String name = ada.getItem(position).getName();
					//opt.putString("currFavoriteDBName", opt.currFavoriteDBName=favorTag+name.substring(0,name.length()-4));
					opt.putCurrFavoriteDBName(favorTag+name.substring(0,name.length()-4));

					favoriteCon = new LexicalDBHelper(PDICMainActivity.this,opt.currFavoriteDBName);

					view.post(new Runnable() {
						@Override
						public void run() {
							d.dismiss();
							show(R.string.currFavor,opt.currFavoriteDBName.substring(10));
						}
					});
				});
				d.setOnDismissListener(dialog -> {
					if(!favoriteCon.isFileExsits()) {
						opt.currFavoriteDBName = "favorites/favorite";
						favoriteCon = new LexicalDBHelper(PDICMainActivity.this,opt.currFavoriteDBName);
						show(R.string.favorRecon);
					}
				});
				ada.notifyDataSetChanged();
				d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v1 -> {
					ada.showDelete = !ada.showDelete;
					ada.notifyDataSetChanged();
				});
				d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PDICMainActivity.this,R.color.colorHeaderBlue));
				d.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED);
				d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
					ViewGroup dv = (ViewGroup) getLayoutInflater().inflate(R.layout.fp_edittext, null);
					final EditText etNew = dv.findViewById(R.id.edt_input);
					final View btn_Done = dv.findViewById(R.id.done);
					AlertDialog.Builder builder1 = new AlertDialog.Builder(PDICMainActivity.this).setView(dv);
					final AlertDialog dd = builder1.create();
					btn_Done.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v12) {
							new LexicalDBHelper(PDICMainActivity.this,favorTag+etNew.getText().toString()).close();
							dd.dismiss();
							ada.notifyDataSetChanged();
						}});
					etNew.setOnEditorActionListener(new OnEditorActionListener(){

						@Override
						public boolean onEditorAction(TextView v12, int actionId, KeyEvent event) {
							if(actionId == EditorInfo.IME_ACTION_DONE ||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
								btn_Done.performClick();
								return true;
							}
							return false;
						}});

					//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					//imm.showSoftInput(etNew, InputMethodManager.SHOW_FORCED);
					dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

					dd.show();

					dd.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							//imm.showSoftInput(etNew, InputMethodManager.SHOW_FORCED);
							//dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
							imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); //only this works but this is 'TOGGLE',not hide.
						}});

				});
				return true;
			case R.id.browser_widget7:
				if(CurrentViewPage==1){//count==4
					if(lv2.getVisibility()==View.VISIBLE){
						for(int i=0;i<md.size();i++){
							md.get(i).clearWebview();
						}
						webholder.removeAllViews();
					}else{
						currentDictionary.clearWebview();
						webholder.removeAllViews();
						if(webSingleholder.getChildCount()>0)
							webSingleholder.removeAllViews();
					}
				}else{
					for(int i=0;i<md.size();i++){
						md.get(i).clearWebview();
					}
					webholder.removeAllViews();
				}
				ViewGroup sm = (ViewGroup) contentview.getParent();
				if(sm!=null) sm.removeView(contentview);
				//else main.removeViews(3,count-1);
				break;
			case R.id.browser_widget8://long-click favorite
				if(favoriteCon.insertUpdate(ActivedAdapter.currentKeyText)!=-1) {
					show(R.string.bookmarkup);
					if(star_ic==null) {
						star_ic = getResources().getDrawable(R.drawable.star_ic_solid);
						star = favoriteBtn.getDrawable();
					}
					favoriteBtn.setImageDrawable(star_ic);
					return true;
				}
				break;
			case R.id.browser_widget9://long-click view outline
				if(PeruseView!=null && PeruseView.getView().getParent()!=null) {
					PeruseView.toolbar_cover.performClick();
					break;
				}
				if((isCombinedSearching && DBrowser!=null) ||ActivedAdapter==adaptermy2) {
					resultRecorderCombined res;
					int idx = 0;

					if(DBrowser!=null)
						res = DBrowser.rec;
					else {
						res = (resultRecorderCombined) adaptermy2.combining_search_result;
						idx = adaptermy2.lastClickedPos;
						if(idx<0 || idx>=res.list().size())
							return true;
					}

					additiveMyCpr1 contentIndexs = res.list().get(idx);
					int totalHeight=0;
					int selectedPos=-1;
					final int currentHeight=((ScrollView)webholder.getParent()).getScrollY();
					View itemTo = null;
					for(int i=0;i<webholder.getChildCount();i+=1) {
						itemTo = webholder.getChildAt(i);
						totalHeight+=itemTo.getMeasuredHeight();
						if(totalHeight>currentHeight) {
							selectedPos=i;
							break;
						}
					}
					if(selectedPos!=-1)
						itemTo.findViewById(R.id.cover).performClick();

				}else
					currentDictionary.rl.findViewById(R.id.cover).performClick();
				return true;
			case R.id.lvitems:
				callDrawerIconAnima();
				//drawerFragment.etAdditional.setText(((TextView)v.findViewById(R.id.text)).getText());
				return false;
		}
		return false;
	}


	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();
		MenuItemImpl mmi = (MenuItemImpl)item;
		boolean isLongClicked=mmi.isLongClicked;
		if(isLongClicked) {
			switch(id) {
				case R.id.toolbar_action6:
					ActivedAdapter.avoyager.clear();
					showT("已重置页面位置");
					return true;
			}
		}
		switch(id){
			case R.id.toolbar_action0:
				int targetVis=View.VISIBLE;
				for(int i=0;i<webholder.getChildCount();i++) {
					if(webholder.getChildAt(i).findViewById(R.id.webviewmy).getVisibility()!=View.GONE) {
						targetVis=View.GONE;
						break;
					}
				}
				for(int i=0;i<webholder.getChildCount();i++) {
					webholder.getChildAt(i).findViewById(R.id.webviewmy).setVisibility(targetVis);
				}
				break;
			case R.id.toolbar_action5://提示结果
				if(opt.toggleNotifyComboRes()) {
					item.setTitle(item.getTitle()+" √");
				}else {
					item.setTitle(item.getTitle().subSequence(0, item.getTitle().length()-2));
				}

				break;
			case R.id.toolbar_action6://翻页前记忆位置
				boolean val=ActivedAdapter!=adaptermy2?opt.setRemPos(!opt.getRemPos()):opt.setRemPos2(!opt.getRemPos2());
				if(val) {
					item.setTitle(item.getTitle()+" √");
				}else {
					item.setTitle(item.getTitle().subSequence(0, item.getTitle().length()-2));
				}
				break;
			case R.id.toolbar_action7://切换词典
				findViewById(R.id.browser_widget1).performClick();
				break;
			case R.id.toolbar_action8://切换切换分组
				findViewById(R.id.browser_widget2).performClick();
				break;
			case R.id.toolbar_action9://存书签
				if(ActivedAdapter!=null && ActivedAdapter!=adaptermy2) {
					if(webSingleholder.getVisibility()==View.VISIBLE){
						int idx=webSingleholder.getChildCount()-1;
						if(idx>=0) {
							View v = webSingleholder.getChildAt(idx);
							v=v.findViewById(R.id.cover);
							if(v!=null) {
								v.setTag(R.id.toolbar_action1,CMN.OccupyTag);
								v.performClick();
								if(v.getTag(R.id.toolbar_action2)!=null) {
									showX(R.string.bmAdded,0);
									v.setTag(R.id.toolbar_action2,null);
								}else
									showT("添加失败,数据库出错...",0);
							}
						}

					}
				}
				break;
			case R.id.toolbar_action10://切换提示模式
				if(opt.toggleHintSearchMode()) {
					item.setTitle(item.getTitle()+" √");
					snack = TSnackbar.makeraw(main_succinct, getResources().getString(CurrentViewPage==0?R.string.fuzzyret:R.string.fullret),TSnackbar.LENGTH_LONG);
					snack.getView().setAlpha(0.5f);
					snack.show();
				}else {
					item.setTitle(item.getTitle().subSequence(0, item.getTitle().length()-2));
				}
				break;
			case R.id.toolbar_action11://切换着色
				if(TintWildResult.first=opt.toggleTintWildRes()) {
					item.setTitle(item.getTitle()+" √");
				}else {
					item.setTitle(item.getTitle().subSequence(0, item.getTitle().length()-2));
				}
				adaptermy3.notifyDataSetChanged();
				break;
			case R.id.toolbar_action2:
				if(CurrentViewPage==1) {//viewPager
					tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
					if(!opt.getHistoryStrategy0()) {
						if(!etSearch.getText().toString().trim().contains("<分>")) {
							historyCon.insertUpdate(etSearch.getText().toString().trim());
						}else if(opt.getHistoryStrategy1()) {
							historyCon.insertUpdate(etSearch.getText().toString().trim());
						}
					}
				}else
					etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
				mDrawerLayout.closeDrawer(GravityCompat.START);
				break;
			case R.id.toolbar_action3://per-word searching
				//etSearch.getText().toString().startsWith("")
				String text = etSearch.getText().toString().trim();
				String perWSTag = getResources().getString(R.string.perWSTag);
				if(text.startsWith("<")) {
					String firstTag = firstTag(text);
					if(perWSTag.equals(firstTag)||"分字".equals(firstTag)) {
						etSearch.setText(text.substring(text.indexOf(">")+1));
						break;
					}
				}
				etSearch.setText(ToTag(perWSTag)+text);
				break;
			case R.id.toolbar_action4:
				if(bInPeruseModeToggleManifested) {
					findViewById(R.id.browser_widget0).setVisibility(View.GONE);
				}else {
					widget0.setVisibility(View.VISIBLE);
					if(bInPeruseMode) {
						widget0.setImageResource(R.drawable.peruse_ic_on);
						snack = TSnackbar.makeraw(main_succinct, "翻阅模式",TSnackbar.LENGTH_SHORT);snack
								.show();
					}
				}
				opt.setInPeruseModeTM(bInPeruseModeToggleManifested=!bInPeruseModeToggleManifested);
				opt.putFirstFlag();
				break;
		}
		if(!((MenuItemImpl) item).isActionButton())
			toolbar.getMenu().close();
		return false;
	}

	private String firstTag(String text) {
		if(!text.startsWith("<"))return null;
		int idx = text.indexOf(">");
		if(idx==-1)return null;
		return text.substring(1,idx).trim();
	}

	private String ToTag(String input) {
		return "<"+input+">";
	}












	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent duco) {
		super.onActivityResult(requestCode, resultCode, duco);
		switch (requestCode) {
			case 110:
				for (int idxTmp = md.size() - 1; idxTmp >= 0; idxTmp--) {
					mdict mdTmp = md.get(idxTmp);
					if (mdTmp.cover != null)
						mdTmp.cover.setBounds(0, 0, mdTmp.cover.getIntrinsicWidth(), mdTmp.cover.getIntrinsicHeight());
					if (mdict_prempter.class == mdTmp.getClass() || mdict_nonexist.class == mdTmp.getClass()) {
						try {
							md.set(idxTmp, new_mdict(mdTmp.getPath(), this));//实化
						} catch (Exception e) {
							//e.printStackTrace();
							//md.remove(mdTmp);
							md.remove(idxTmp);
							show(R.string.err, new File(mdTmp.getPath()).getName(), mdTmp.getPath(), e.getLocalizedMessage());
						}
					}
				}
				//tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
				adapter_idx = -1;

				for (int idxTmp = md.size() - 1; idxTmp >= 0; idxTmp--) {
					mdict mdTmp = md.get(idxTmp);
					if (mdict_nonexist.class == mdTmp.getClass()) {
						md.remove(mdTmp);
					} else {
						if (mdTmp == currentDictionary || mdTmp._Dictionary_fName.equals(opt.getLastMdFn())) {
							adapter_idx = idxTmp;
						}
					}
				}
				if (adapter_idx == -1 && md.size() > 0) {
					switchToDictIdx(0);
				}

				//if(f1.isDirty)
				//if(a.pickDictDialog!=null) {
				//	a.pickDictDialog.adapter().notifyDataSetChanged();
				//}
				if (duco != null) {
					boolean isDirty = duco.getBooleanExtra("result", false);
					if (isDirty)
						invalidAllLists();
					if (duco.getBooleanExtra("result2", false)) {
						opt.putFirstFlag();
						CMN.Log("保存页码");
					}
				}
				break;
			case 123:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					int i = checkSelfPermission(permissions[0]);
					if (i != PackageManager.PERMISSION_GRANTED) {
						AppSetttingShowDialogReq();
					} else {
						if (d != null && d.isShowing()) {
							d.dismiss();
							d = null;
						}
						Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
						pre_further_loading(null);
					}
				}
				break;
			case 700:
				Uri uri = duco.getData();
				getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				//CMN.show(duco.getIntExtra("asd", -1)+"");
				Log.e("uri", uri.toString());
				//DocumentFile encryptMyFolderf = DocumentFile.fromTreeUri(this, uri);
				//encryptMyFolderf.createDirectory("asd");
				//CMN.show(encryptMyFolderf.exists()+"");
				//DocumentFile.fromFile(new File("/storage/0DE6-2108/123.txt")).createFile("", null);
				break;
			case 111:
				if (duco != null) {
					if (duco.getBooleanExtra("DC", false))
						drawerFragment.myAdapter.notifyDataSetChangedX();
				}
				break;
		}
		//TODO seal it
		//CMN.a = null;
	}


	// 提示用户 去设置界面 手动开启权限
	private void AppSetttingShowDialogReq() {
		//动态申请不成功，转为手动开启权限
		d = new AlertDialog.Builder(this)
				.setTitle("存储权限不可用")
				.setMessage("请在-应用设置-权限-中，允许存储权限来保存用户数据")
				.setPositiveButton("立即开启", (dialog, which) -> {
					// 跳转到应用设置界面
					Intent intent = new Intent();

					intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					Uri uri = Uri.fromParts("package", getPackageName(), null);
					intent.setData(uri);

					startActivityForResult(intent, 123);
				})
				.setNegativeButton("取消", (dialog, which) -> finish()).setCancelable(false).show();

	}

	//权限申请回调
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == 321) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					// 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
					boolean b = shouldShowRequestPermissionRationale(permissions[0]);
					if (!b) {
						// 用户还是想用我的 APP 的
						// 提示用户去应用设置界面手动开启权限
						AppSetttingShowDialogReq();
					} else
						finish();
				} else {
					Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
					pre_further_loading(null);
				}
			}
		}
	}

	@Override
	public void jump(int newLexiEntryPos,mdict mdCurr) {

		if(currentDictionary==mdCurr) {
			lv.setSelection(newLexiEntryPos);
			adaptermy.lastClickedPos = newLexiEntryPos;
		}


	}



	float animator = 0.1f;
	float animatorD = 0.15f;
	boolean triggered=false;

	public void callDrawerIconAnima() {
		triggered=false;
		mDrawerToggle.onDrawerClosed(mDrawerLayout);
		hdl.sendEmptyMessage(112233);

	}

	@Override
	public void invalidAllLists() {
		if(ActivedAdapter!=null) ActivedAdapter.ClearVOA();
		adaptermy.notifyDataSetChanged();
		//adaptermy2.combining_search_result.invalidate();
		adaptermy2.combining_search_result.shutUp();
		adaptermy2.currentKeyText=null;
		adaptermy2.notifyDataSetChanged();

		tw1.onTextChanged(etSearch.getText(), 0, 0, 0);

		adaptermy3.shutUp();adaptermy3.notifyDataSetChanged();
		adaptermy3.combining_search_result.invalidate();adaptermy3.notifyDataSetChanged();
		adaptermy4.shutUp();adaptermy4.notifyDataSetChanged();
		adaptermy4.combining_search_result.invalidate();adaptermy4.notifyDataSetChanged();
		if(pickDictDialog!=null)pickDictDialog.adapter().notifyDataSetChanged();
	}

	public boolean deleteHistory() {
		boolean ret = new File(opt.pathToInternal().append("history").append(".sql").toString()).delete();
		if(ret) {
			historyCon = new LexicalDBHelper(this,"history");
		}
		return ret;
	}

	View mView;
	int acc=0;
	float lastX = -1;
	float lastY = -1;
	int last1,last2;
	int deltaY;

	float lastSX=-1,lastSY;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId()==R.id.webviewmy) {
			WebViewmy vTmp = (WebViewmy) v;
			if(lastSX!=-1)
				if(lastSX!=vTmp.getScrollX() || lastSY!=vTmp.getScrollY()) {
					nNeedSaveViewStates=true;
				}
			lastSX=vTmp.getScrollX();
			lastSY=vTmp.getScrollY();
		}

		if(opt.UseTripleClick())
			switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(webSingleholder.getChildCount()!=0) {
						int _last1 = ((ViewGroup)webSingleholder.getChildAt(1  )).getChildAt(1).getScrollY();
						deltaY = _last1-last1;
						last1 = _last1;
					}else {
						int _last1 = webholder.getScrollY();
						deltaY = _last1-last2;
						last2 = _last1;
					}
					float x = event.getX();
					float y = event.getY();
					if(System.currentTimeMillis()-lastClickTime>500) {
						acc=1;
						lastX=x;
						lastY=y;
						deltaY=0;
						//CMN.show(lastClickTime+"down!"+acc);
					}else{
						if(deltaY==0 && (lastX-x)*(lastX-x)+(lastY-y)*(lastY-y)<1000) {
							acc++;
							//CMN.show(""+acc);
							lastX=x;
							lastY=y;
							if(acc>=3) {
								//CMN.show("tc!");
								if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
									if (!Settings.System.canWrite(this)) {
										Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
										intent.setData(Uri.parse("package:" + this.getPackageName()));
										intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										this.startActivity(intent);
									} else {
										//有了权限，具体的动作
										//WindowManager.LayoutParams params = getWindow().getAttributes();
										//params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
										//getWindow().setAttributes(params);
										if (!Settings.canDrawOverlays(this)) {
											Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
													Uri.parse("package:" + getPackageName()));
											startActivityForResult(intent,10);
										}else {
											int color=(int)Long.parseLong(String.format("%02x%02x%02x%02x", 0, 255, 255, 255),16);
											//getWindow().setBackgroundDrawable(cd);
											mView = new LinearLayout(this);
											mView.setBackgroundColor(Color.BLACK);
											WindowManager.LayoutParams params = new WindowManager.LayoutParams(
													WindowManager.LayoutParams.MATCH_PARENT,
													WindowManager.LayoutParams.MATCH_PARENT,
													Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
													WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
													PixelFormat.TRANSLUCENT);
											WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
											wm.addView(mView, params);

											Window window = getWindow();
											WindowManager.LayoutParams layoutParams = window.getAttributes();
											boolean dimButtons=true;
											float val = dimButtons ? 0 : -1;
											try {
												Field buttonBrightness = layoutParams.getClass().getField(
														"buttonBrightness");
												buttonBrightness.set(layoutParams, val);
											} catch (Exception e) {
												e.printStackTrace();
											}
											window.setAttributes(layoutParams);
											if(oldTime==-1)
												oldTime = Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, 1*60*10);

											Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_OFF_TIMEOUT, 0);
										}
									}
								}

								lastClickTime=0;break;
							}
						}else {
							acc=1;
							lastX=x;
							lastY=y;
						}
					}
					lastClickTime = System.currentTimeMillis();
					break;
			}
		return false;
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		//Toast.makeText(this, mode.getTag()+"ONACTMS"+mode.hashCode(), 0).show();
		//showT(mode.getTag()+" "+mode.getSubtitle()+" "+mode.getTitle());
		//mode.setTag(110);
		//final Menu menu = mode.getMenu();
		//MenuItem MyMenu = menu.add(0, R.id.position, 0, "高亮");

		//Toast.makeText(menu.getItem(0).getTitle()).show();
		//Toast.makeText(this, "\"onActionModeStarted2\"", 0).show();
		//Menu menu = mode.getMenu();
		//menu.clear();
		if(Build.VERSION.SDK_INT<23) {//defalut float context menu comes from android Marshmallow


			int share_id=WebViewmy.getReflactField("com.android.internal.R$string", "share");
			if(share_id!=-1)
				Toast.makeText(this, ""+getResources().getString(share_id),Toast.LENGTH_SHORT).show();

		}
		super.onActionModeStarted(mode);
	}

	@Override
	public void switchToDictIdx(int i){
		if(i<0 || i>=md.size()) return;
		if(currentDictionary!=null) {//save dict position!
			currentDictionary.lvPos=lv.getFirstVisiblePosition();
			if(lv.getChildCount()>0)currentDictionary.lvPosOff=lv.getChildAt(0).getTop();
			currentDictionary.dumpViewStates();
		}
		super.switchToDictIdx(i);

		if(opt.getPicDictAutoSer()) {
			//CMN.Log("changing text!......");
			tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
		}
		if((!opt.getPicDictAutoSer() || isCombinedSearching) && currentDictionary!=null) {
			lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
		}
		if(!opt.getPinPicDictDialog()) {
			if(pickDictDialog.isDirty)  {opt.putFirstFlag();pickDictDialog.isDirty=false;}
			if(objectAnimator!=null) objectAnimator.cancel();
			objectAnimator = ObjectAnimator.ofFloat(dialogHolder,"alpha",1,0.6f);
			objectAnimator.setDuration(240);
			View view = pickDictDialog.getView();
			if(view!=null)view.postDelayed(() -> dialogHolder.setVisibility(View.GONE), 250);
			objectAnimator.start();

		}
	}

	@Override
	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
		//Context menu
		CMN.Log("onCreateContextMenu");
	}

	protected void switch_dark_mode(boolean val) {
		drawerFragment.sw4.setChecked(val);
	}
}
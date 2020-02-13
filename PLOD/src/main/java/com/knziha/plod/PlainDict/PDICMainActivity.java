package com.knziha.plod.PlainDict;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.os.LocaleList;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.bumptech.glide.load.engine.cache.DiskCache;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.filepicker.view.WindowChangeHandler;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionarymanager.dict_manager_activity;
import com.knziha.plod.dictionarymanager.files.BooleanSingleton;
import com.knziha.plod.dictionarymanager.files.IntegerSingleton;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_asset;
import com.knziha.plod.dictionarymodels.mdict_pdf;
import com.knziha.plod.dictionarymodels.mdict_txt;
import com.knziha.plod.dictionarymodels.mdict_web;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.dictionarymodels.resultRecorderScattered;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.searchtasks.CombinedSearchTask;
import com.knziha.plod.searchtasks.FullSearchTask;
import com.knziha.plod.searchtasks.FuzzySearchTask;
import com.knziha.plod.searchtasks.VerbatimSearchTask;
import com.knziha.plod.widgets.ArrayAdapterHardCheckMark;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.IMPageSlider;
import com.knziha.plod.widgets.IMPageSlider.PageSliderInf;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.NoScrollViewPager;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.Utils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 主程序 - 单实例<br/>
 * Our single instanced Main Interface.<br/>
 * Created by KnIfER on 2018.
 */
@SuppressLint({"SetTextI18n", "ClickableViewAccessibility","PrivateApi","DiscouragedPrivateApi"})
public class PDICMainActivity extends MainActivityUIBase implements OnClickListener, OnLongClickListener, OnMenuItemClickListener{
	public String textToSetOnFocus;
	private String debugString=null;//世           界     你好 happy呀happy\"人\"’。，、？
	public static int taskCounter = 0;
	public Timer timer;
	public int currentSearchingDictIdx;
	public TextView dvTitle;
	public SeekBar dvSeekbar;
	public TextView dvProgressFrac;
	public TextView dvResultN;

	ViewGroup mlv;
	public ListView mlv1;
	public ListView mlv2;


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

	public boolean bNeedReAddCon;
	public boolean bOnePageNav;
	private MyHandler mHandle;
	public AsyncTask<String, Integer, String> mAsyncTask;
	boolean focused;
	public int rem_res=R.string.rem_position;
	public static ArrayList<PlaceHolder> CosyChair = new ArrayList<>();
	public static ArrayList<PlaceHolder> CosySofa = new ArrayList<>();
	public static ArrayList<PlaceHolder> HdnCmfrt = new ArrayList<>();
	private Animation animaExit;

	@Override
	ArrayList<PlaceHolder> getLazyCC() {
		return CosyChair;
	}

	@Override
	ArrayList<PlaceHolder> getLazyCS() {
		return CosySofa;
	}

	@Override
	ArrayList<PlaceHolder> getLazyHC() {
		return HdnCmfrt;
	}


	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		//CMN.Log("onConfigurationChanged",mConfiguration==newConfig, dm==getResources().getDisplayMetrics(), !isLocalesEqual(mConfiguration, newConfig));
		if(!systemIntialized) return;
		if(!isLocalesEqual(mConfiguration, newConfig) && "".equals(opt.getLocale())){
			recreate();
			super.onConfigurationChanged(newConfig);
			return;
		}
		super.onConfigurationChanged(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(mConfiguration.orientation!=newConfig.orientation) {
			if(root.getTag()!=null){
				MarginLayoutParams lp = (MarginLayoutParams) root.getLayoutParams();
				int mT=DockerMarginT;
				int mB=DockerMarginB;
				DockerMarginT=DockerMarginL;
				DockerMarginB=DockerMarginR;
				DockerMarginL=mT;
				DockerMarginR=mB;
				lp.leftMargin =   DockerMarginL;
				lp.rightMargin =  DockerMarginR;
				lp.topMargin =    DockerMarginT;
				lp.bottomMargin = DockerMarginB;
				root.setLayoutParams(lp);
			}
			R.styleable.single[0] = android.R.attr.actionBarSize;
			TypedValue typedValue = new TypedValue();
			getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
			TypedArray array = obtainStyledAttributes(typedValue.resourceId, R.styleable.single);
			actionBarSize = array.getDimensionPixelSize(0, (int) (56*dm.density));
			array.recycle();
			//CMN.Log("actionBarSize ; ", actionBarSize);

			LayoutParams lp = toolbar.getLayoutParams();
			lp.height=actionBarSize;
			toolbar.setLayoutParams(lp);
			refreshContentBow(opt.isContentBow());

			if(d!=null) {
				if(d instanceof WindowChangeHandler)
					((WindowChangeHandler)d).OnWindowChange(dm);
				else {
					Window win = d.getWindow();
					if(win!=null && win.getDecorView().getWidth()>dm.widthPixels) {
						d.getWindow().getAttributes().width = (int) (dm.widthPixels-2.5*getResources().getDimension(R.dimen.diagMarginHor));
						d.getWindow().setAttributes(d.getWindow().getAttributes());
					}
				}
			}
		}
		mConfiguration.setTo(newConfig);
		if(Build.VERSION.SDK_INT>=29){
			GlobalOptions.isDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
		}else
			GlobalOptions.isDark = false;
		opt.getInDarkMode();
		//CMN.Log("GlobalOptionsGlobalOptions", GlobalOptions.isDark, isDarkStamp);
		if(GlobalOptions.isDark!=isDarkStamp)
			changeToDarkMode();
		isDarkStamp=GlobalOptions.isDark;
	}

	private boolean isLocalesEqual(Configuration oldConfig, Configuration newConfig) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			LocaleList localeA = oldConfig.getLocales();
			LocaleList localeB = newConfig.getLocales();
			if(localeA.size()==0 || localeB.size()==0) return false;
			return localeA.get(0).equals(localeB.get(0));
		}
		return oldConfig.locale.equals(newConfig.locale);
	}

	public void exitFFSearch() {
		taskd=null;
		if(mAsyncTask!=null)
			mAsyncTask.cancel(true);
		dvSeekbar = null;
		dvProgressFrac = null;
		dvResultN = null;
		currentSearchLayer.IsInterrupted=true;
	}

	public void OnEnterFullSearchTask(AsyncTask task) {
		taskCounter=md.size();
		AdvancedSearchLogicLayer _currentSearchLayer = currentSearchLayer = fullSearchLayer;
		_currentSearchLayer.dirtyProgressCounter=
		_currentSearchLayer.dirtyResultCounter=0;
		_currentSearchLayer.IsInterrupted=false;
		_currentSearchLayer.bakePattern(null, null);
		ShowProgressDialog().findViewById(R.id.cancel).setOnClickListener(v13 -> {
			if(!_currentSearchLayer.IsInterrupted){
				_currentSearchLayer.IsInterrupted=true;
				task.cancel(false);
			}else{
				task.cancel(true);
				((FullSearchTask)task).harvest(true);
				mAsyncTask=null;
				if(taskd!=null){
					taskd.dismiss();
					taskd=null;
				}
				showT("强制关闭");
			}
		});
		for(int i=0;i<md.size();i++){//遍历所有词典
			mdict mdtmp = md.get(i);
			if(mdtmp!=null) {
				mdtmp.searchCancled=false;
				if(mdtmp.combining_search_tree_4!=null){
					for (int ti = 0; ti < mdtmp.combining_search_tree_4.length; ti++) {//遍历搜索结果容器
						if (mdtmp.combining_search_tree_4[ti] != null)
							mdtmp.combining_search_tree_4[ti].clear();
					}
				}
			}
		}
		CMN.stst = System.currentTimeMillis();
	}

	public void OnEnterFuzzySearchTask(AsyncTask task) {
		taskCounter=md.size();
		currentSearchLayer=fuzzySearchLayer;
		fuzzySearchLayer.dirtyProgressCounter=
		fuzzySearchLayer.dirtyResultCounter=0;
		fuzzySearchLayer.IsInterrupted=false;
		fuzzySearchLayer.bakePattern(null, null);
		ShowProgressDialog().findViewById(R.id.cancel).setOnClickListener(v13 -> {
			if(!fuzzySearchLayer.IsInterrupted){
				task.cancel(false);
				fuzzySearchLayer.IsInterrupted=true;
			}else{
				task.cancel(true);
				((FuzzySearchTask)task).harvest();
				CMN.Log("强制关闭");
			}
		});
		for(int i=0;i<md.size();i++){//遍历所有词典
			mdict mdtmp = md.get(i);
			if(mdtmp!=null) {
				mdtmp.searchCancled = false;
				if (mdtmp.combining_search_tree2 != null) {
					for (int ti = 0; ti < mdtmp.combining_search_tree2.length; ti++) {//遍历搜索结果
						if (mdtmp.combining_search_tree2[ti] != null)
							mdtmp.combining_search_tree2[ti].clear();
					}
				}
			}
		}
		CMN.stst = System.currentTimeMillis();
	}

	private View ShowProgressDialog() {
		View a_dv = inflater.inflate(R.layout.dialog_progress, findViewById(R.id.dialog), false);
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
		builder2.setView(a_dv);
		AlertDialog dTmp = builder2.create();
		dTmp.setCanceledOnTouchOutside(false);
		dTmp.setOnDismissListener(dialog -> exitFFSearch());
		dTmp.show();
		Window win = dTmp.getWindow();
		if (win != null) {
			win.getAttributes().height = (int) (250*dm.density);
			win.setAttributes(win.getAttributes());
		}
		taskd = dTmp;
		dvTitle = a_dv.findViewById(R.id.title);
		dvSeekbar = a_dv.findViewById(R.id.seekbar);
		dvProgressFrac = a_dv.findViewById(R.id.progressFrac);
		dvResultN = a_dv.findViewById(R.id.resultN);
		if(currentDictionary!=null)
			dvTitle.setText(currentDictionary._Dictionary_fName);
		/* 跳过 */
		a_dv.findViewById(R.id.skip).setOnClickListener(v14 -> {
			if(currentSearchingDictIdx<md.size()){
				mdict mdTmp = md.get(currentSearchingDictIdx);
				if(mdTmp!=null){
					mdTmp.searchCancled=true;
				}
			}
		});

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
			mdict m = md.get(index);
			currentSearchingDictIdx =index;
			dvSeekbar.setMax((int) m.getNumberEntries());
			dvTitle.setText(m._Dictionary_fName);
			dvProgressFrac.setText(currentSearchingDictIdx+"/"+PDICMainActivity.taskCounter);
		} catch (Exception ignored) { }
	}

	/** Jump to old or new UI with new text.
	 * @param content New text
	 * @param source specifies the source of our text.
	 * 0=intent share; 1=focused paste; 2=unfocused paste
	 * */
	public void JumpToWord(String content, int source) {
		//CMN.Log("JumpToWord", focused, source, PDICMainAppOptions.getPasteTarget(), PDICMainAppOptions.getPasteToPeruseModeWhenFocued());
		if((source>=1)&&PDICMainAppOptions.getPasteTarget()==3 && !(source==1&&PDICMainAppOptions.getPasteToPeruseModeWhenFocued())){
			Intent popup = new Intent().setClassName("com.knziha.plod.plaindict", "com.knziha.plod.PlainDict.FloatActivitySearch").putExtra("EXTRA_QUERY", content);
			//this, FloatActivitySearch.class
			popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(PDICMainAppOptions.isFullScreen()){
				popup.putExtra(EXTRA_FULLSCREEN, true)
				.putExtra(EXTRA_HIDE_NAVIGATION, PDICMainAppOptions.isFullscreenHideNavigationbar());
			}
			//popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//CMN.Log("pop this way!");
			getApplicationContext().startActivity(popup);
			return;
		}

		int PasteTarget=PDICMainAppOptions.getPasteTarget();
		int ShareTarget=PDICMainAppOptions.getShareTarget();
		boolean isPeruseView=PeruseViewAttached();
		boolean toPeruseView =  (source>=1)&&(PasteTarget==2||PasteTarget==0&&isPeruseView) ||
				source == 1 && PDICMainAppOptions.getPasteToPeruseModeWhenFocued() ||
				source == 0 &&(ShareTarget==2||ShareTarget==0&&isPeruseView)
				;
		if(toPeruseView){
			JumpToPeruseModeWithWord(content);
		}else
			etSearch.setText(content);
	}

	public void forceFullscreen(boolean val) {
		drawerFragment.setCheckedForce(drawerFragment.sw1, val);
		drawerFragment.setCheckedForce(drawerFragment.sw2, val);
	}

	private static class MyHandler extends BaseHandler{
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
			PDICMainActivity a = ((PDICMainActivity)activity.get());
			switch (msg.what) {
				case 2020:
					if(msg.obj instanceof String)
					a.showT((String)msg.obj, Toast.LENGTH_LONG);
				break;
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
						if(a.topsnack.offset>-(a.topsnack.getHeight()+5*a.dm.density))
							sendEmptyMessage(6658);
						else{
							a.removeSnackView();
							break;
						}
						a.topsnack.setTranslationY(a.topsnack.offset);
					}
				break;
				case 1008601:
					//((TextView)dv.findViewById(R.id.tv)).setText("0/"+System.currentTimeMillis());
					if(a.currentSearchingDictIdx>=a.md.size())
						return;
					mdict m = a.md.get(a.currentSearchingDictIdx);
					if(a.dvSeekbar!=null)
					try {
						a.dvSeekbar.setProgress(a.currentSearchLayer.dirtyProgressCounter);
						a.dvProgressFrac.setText(a.currentSearchLayer.dirtyProgressCounter+"/"+m.getNumberEntries());
						a.dvResultN.setText("已搜索到: "+a.currentSearchLayer.dirtyResultCounter+" 项条目!");
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
					}
					else {
						int filteredColor = a.AppWhite==Color.WHITE?ColorUtils.blendARGB(Color.BLACK,a.MainBackground, animator):ColorUtils.blendARGB(a.MainBackground, Color.BLACK, animator);
						int filteredWhite = ColorUtils.blendARGB(a.AppBlack, a.AppWhite, animator);

						a.bottombar.setBackgroundColor(filteredColor);
						a.toolbar.setBackgroundColor(filteredColor);
						a.viewPager.setBackgroundColor(filteredWhite);
						a.lv2.setBackgroundColor(filteredWhite);
						a.hdl.sendEmptyMessage(331122);
					}
				break;
				case 778899:
					//a.NaugtyWeb.setLayoutParams(a.NaugtyWeb.getLayoutParams());
					a.NaugtyWeb.requestLayout();
					//CMN.Log("handler scroll scale recalibrating ...");
				break;
			}
	}}

	@Override
	public void onBackPressed() {
		boolean bBackBtn = widget7.getTag()!=null;
		if(bBackBtn) widget7.setTag(null);
		if(!bBackBtn && checkWebSelection())
			return;
		if(removeBlack())
			return;
		cancleSnack();
		ViewGroup sm;
		if(dialogHolder.getVisibility()==View.VISIBLE) {
			dialogHolder.setVisibility(View.GONE);
			if(pickDictDialog!=null) if(pickDictDialog.isDirty)  {opt.putFirstFlag();pickDictDialog.isDirty=false;}
		}
		else if(popupContentView!=null && popupContentView.getParent()!=null){
			DetachClickTranslator();
		}
		else if(mainF.getChildCount()==0 && !isContentViewAttached()){//
			if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
				mDrawerLayout.closeDrawer(GravityCompat.START);
				return;
			}
			boolean b1=PDICMainAppOptions.getBackToHomePage();
			if(!b1||PDICMainAppOptions.getBackToHomePagePreventBack()) {
				int BackPrevention = PDICMainAppOptions.getBackPrevention();
				switch (BackPrevention) {
					default: break;
					case 1:
					case 2:
						if ((System.currentTimeMillis() - exitTime) > 2000) {
							if (BackPrevention == 1) showTopSnack(R.string.warn_exit);
							else showX(R.string.warn_exit, 0);
							exitTime = System.currentTimeMillis();
							return;
						}
						break;
					case 3: drawerFragment.showExitDialog();
						return;
				}
			}
			if(b1) moveTaskToBack(true);
			else /*finish();*/ super.onBackPressed();
		}
		else if(DBrowser !=null){
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
			webholder.removeAllViews();
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
		}
		else if(ActivedAdapter!=null && contentview.getParent()!=null) {
			/* 检查返回键倒退网页 */
			if(opt.getUseBackKeyGoWebViewBack() && !bBackBtn){
				WebViewmy view = getCurrentWebContext();
				if(view!=null && view.canGoBack()){
					view.goBack();
					return;
				}
			}
			main_progress_bar.setVisibility(View.GONE);

			toolbar.getMenu().findItem(R.id.toolbar_action2).setVisible(true);//Ser
			iItem_PerwordSearch.setVisible(CurrentViewPage==1);
			iItem_PeruseMode.setVisible(true);

			iItem_aPageRemember.setVisible(false);//翻忆
			iItem_JumpPeruse.setVisible(false);//翻忆
			iItem_FolderAll.setVisible(false);//折叠

			iItem_InPageSearch.setVisible(!opt.getInPageSearchVisible()&&!opt.isContentBow());
			iItem_ClickSearch.setVisible(false);
			iItem_SaveSearch.setVisible(CurrentViewPage!=1);
			iItem_TintWildRes.setVisible(CurrentViewPage!=1);
			//iItem_PickDict.setVisible(false);
			iItem_PickSet.setVisible(false);
			iItem_SaveBookMark.setVisible(false);

			ActivedAdapter.SaveVOA();
			adaptermy2.currentKeyText=null;
			adaptermy.currentKeyText=null;
			webholder.removeAllViews();
			int remcount = webSingleholder.getChildCount();
			if(remcount>0) webSingleholder.removeAllViews();

			if(currentDictionary!=null && currentDictionary.mWebView!=null ) currentDictionary.mWebView.expectedPos=0;
			((ListViewAdapter2)adaptermy2).expectedPos=0;
			if(drawerFragment.d!=null) {
				drawerFragment.d.show();
			}
			PageSlider.setTranslationX(0);
			PageSlider.setTranslationY(0);
			int lastPos = ActivedAdapter.lastClickedPos;
			if(CurrentViewPage==1){//count==4
				if(ActivedAdapter==adaptermy2){ //clearWebviews
					DetachContentView();
					etSearch_ToToolbarMode(0);
					bWantsSelection=false;
					//webholder.removeAllViews();
					if(lastPos<lv2.getFirstVisiblePosition() || lastPos>lv2.getLastVisiblePosition())
						lv2.setSelection(lastPos);
				}
				else{
					DetachContentView();
					etSearch_ToToolbarMode(0);
					bWantsSelection=false;
					//webholder.removeAllViews();
					if(lastPos<lv.getFirstVisiblePosition() || lastPos>lv.getLastVisiblePosition())
						lv.setSelection(lastPos);
				}
			}
			else{
				DetachContentView();
				etSearch_ToToolbarMode(0);
				bWantsSelection=false;
				//webholder.removeAllViews();
				if(CurrentViewPage==0) {
					if(lastPos<mlv1.getFirstVisiblePosition() || lastPos>mlv1.getLastVisiblePosition())
						mlv1.setSelection(lastPos);
				}
				else {
					if(lastPos<mlv2.getFirstVisiblePosition() || lastPos>mlv2.getLastVisiblePosition())
						mlv2.setSelection(lastPos);
				}
			}
			//else main.removeViews(3,count-1);
			ActivedAdapter=null;
		}
		else if(contentview.getParent()!=null){/* avoid stuck */
			DetachContentView();
		}
		else{
			//mainF.removeAllViews();
			etSearch_ToToolbarMode(0);
			bWantsSelection=false;
		}
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
					if(opt.getAutoReadEntry()){
						forbidVolumeAjustmentsForTextRead =true;
					}
					boolean toHighlight=MainPageSearchbar!=null && PDICMainAppOptions.getInPageSearchUseAudioKey() && MainPageSearchbar.getParent()!=null;
					if (DBrowser != null && main.getChildCount() == 1) {//==1: 内容未加渲染
						if (opt.getUseVolumeBtn()) {
							if (DBrowser.inSearch)
								DBrowser.onClick(DBrowser.main_clister_layout.findViewById(R.id.browser_widget13));
							else {
								View v = new View(this);
								v.setId(R.id.nxt_plain);
								DBrowser.onClick(v);
							}
							return true;
						}
					}
					else if (contentview.getParent() != null) {
						if(toHighlight) onIdClick(null, R.id.recess);
						else contentview.findViewById(R.id.browser_widget11).performClick();
						return true;
					}
					else if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
						ListView lvlv = lv;
						if (CurrentViewPage == 0) lvlv = mlv1;
						else if (CurrentViewPage == 2) lvlv = mlv2;
						if (lvlv.getChildCount() > 0) {
							int fvp = lvlv.getFirstVisiblePosition();
							lvlv.setSelection(fvp + 1);
							return true;
						}
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
					if(opt.getAutoReadEntry()){
						forbidVolumeAjustmentsForTextRead =true;
					}
					boolean toHighlight=MainPageSearchbar!=null && PDICMainAppOptions.getInPageSearchUseAudioKey() && MainPageSearchbar.getParent()!=null;
					if (DBrowser != null && main.getChildCount() == 1) {
						if (DBrowser.inSearch)
							DBrowser.onClick(DBrowser.main_clister_layout.findViewById(R.id.browser_widget14));
						else {
							View v = new View(this);
							v.setId(R.id.lst_plain);
							DBrowser.onClick(v);
						}
						return true;
					} else if (PeruseViewAttached()) {
						PeruseView.widget10.performClick();
						return true;
					} else if (contentview.getParent() != null) {
						if(toHighlight) onIdClick(null, R.id.forward);
						else contentview.findViewById(R.id.browser_widget10).performClick();
						return true;
					} else if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
						ListView lvlv = lv;
						if (CurrentViewPage == 0) lvlv = mlv1;
						else if (CurrentViewPage == 2) lvlv = mlv2;
						if (lvlv.getChildCount() > 0) {
							int fvp = lvlv.getFirstVisiblePosition();
							if (lvlv.getChildAt(0).getTop() > -10)
								fvp--;
							lvlv.setSelection(fvp);
							return true;
						}
					}
				}
			} break;
			case KeyEvent.KEYCODE_VOLUME_MUTE:
				if(DBrowser !=null)
					return true;
				break;
//			case KeyEvent.KEYCODE_BACK:
//
//				return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	public MenuItem iItem_aPageRemember;
	private MenuItem iItem_JumpPeruse;


	//private MenuItem iItem_PickDict;
	private MenuItem iItem_PickSet;
	private MenuItem iItem_SaveBookMark;

	private MenuItem iItem_TintWildRes;
	private MenuItem iItem_SaveSearch;

	private MenuItem iItem_PerwordSearch;
	private MenuItem iItem_PeruseMode;

	BooleanSingleton TintWildResult;
	BooleanSingleton TintFullResult;
	public String Current0SearchText;

	private View cb1;
	protected boolean bNeedSaveViewStates;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		R.styleable.constances[0]=0;
		bIsFirstLaunch=false;
		focused=true;
		super.onCreate(null);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,  WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

		setTheme(R.style.PlainAppTheme);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		contentview = findViewById(R.id.webcontentlister);
		Objects.requireNonNull(contentview);
		viewPager = findViewById(R.id.viewpager);
		snack_holder = findViewById(R.id.snack_holder);

		toolbar = findViewById(R.id.toolbar);
		toolbar.setTag(R.id.action_context_bar, false);
		toolbar.inflateMenu(R.menu.menu);

		iItem_FolderAll = toolbar.getMenu().findItem(R.id.toolbar_action0);

		iItem_PerwordSearch = toolbar.getMenu().findItem(R.id.toolbar_action3);
		iItem_PeruseMode = toolbar.getMenu().findItem(R.id.toolbar_action4);

		iItem_InPageSearch = toolbar.getMenu().findItem(R.id.toolbar_action13);
		iItem_ClickSearch = toolbar.getMenu().findItem(R.id.toolbar_action14);
		iItem_aPageRemember = toolbar.getMenu().findItem(R.id.toolbar_action6);
		iItem_JumpPeruse = toolbar.getMenu().findItem(R.id.toolbar_action12);
		iItem_JumpPeruse.setVisible(false);
		iItem_PickSet = toolbar.getMenu().findItem(R.id.toolbar_action8);
		iItem_SaveBookMark = toolbar.getMenu().findItem(R.id.toolbar_action9);
		iItem_SaveSearch = toolbar.getMenu().findItem(R.id.toolbar_action10);
		TintWildResult=new BooleanSingleton(true);
		TintFullResult=new BooleanSingleton(true);
		iItem_TintWildRes = toolbar.getMenu().findItem(R.id.toolbar_action11);

		mDrawerLayout = findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
		mDrawerToggle.syncState();// 添加按钮

		toolbar.addNavigationOnClickListener(v -> {
			if(!mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
				if(contentview.getParent()==main) {
					DetachContentView();
					bNeedReAddCon=true;
				}
				imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
				cancleSnack();
				if(popupContentView!=null && popupContentView.getParent()!=null)
					popupContentView.setVisibility(View.GONE);
			}
		});

		View vTmp = toolbar.getChildAt(toolbar.getChildCount()-1);
		if(vTmp instanceof ImageButton && vTmp.getId()==R.id.home) {
			ImageButton NavigationIcon=(ImageButton) vTmp;
			NavigationIcon.getLayoutParams().width=(int) (45*dm.density);
			NavigationIcon.setLayoutParams(NavigationIcon.getLayoutParams());

			//ViewGroup Menu=(ViewGroup) toolbar.getChildAt(toolbar.getChildCount()-2);
			//if(Menu!=null){
			//	View OverflowIcon=Menu.getChildAt(Menu.getChildCount()-1);
			//	//OverflowIcon.setClickable(true);
			//}
		}

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
			dismissDictPicker(R.anim.dp_dialog_exit);
			//if(pickDictDialog!=null) if(pickDictDialog.isDirty) {opt.putFirstFlag();pickDictDialog.isDirty=false;}
			return true;
		});

		hdl = mHandle = new MyHandler(this);

		checkLog(savedInstanceState);
		CrashHandler.getInstance(this, opt).TurnOn();
	}

	@Override
	protected int getVisibleHeight() {
		return root.getChildAt(0).getHeight();
	}

	@Override
	public PlaceHolder getPlaceHolderAt(int idx) {
		if(idx>=0 && idx<CosyChair.size())
			return CosyChair.get(idx);
		return null;
	}

	@Override
	public ArrayList<PlaceHolder> getPlaceHolders() {
		return CosyChair;
	}

	private void processIntent(Intent intent) {
		if(intent !=null){
			if(intent.hasExtra(Intent.EXTRA_TEXT))
				debugString = intent.getStringExtra(Intent.EXTRA_TEXT);
		}
		if(debugString!=null)
			JumpToWord(debugString, 0);
	}

	protected void further_loading(final Bundle savedInstanceState) {
		//CMN.Log("Main Ac further_loading!!!");
		IMPageCover = contentview.findViewById(R.id.cover);
		PageSlider = contentview.findViewById(R.id.PageSlider);
		mainF = findViewById(R.id.mainF);
		main = root = findViewById(R.id.root);
		webcontentlist = contentview.findViewById(R.id.webcontentlister);
		bottombar2 = contentview.findViewById(R.id.bottombar2);
		browser_widget3 = findViewById(R.id.browser_widget3);
		browser_widget4 = findViewById(R.id.browser_widget4);
		CachedBBSize=opt.getBottombarSize((int) (50*dm.density));
		super.further_loading(savedInstanceState);

		CheckGlideJournal();

		//showT(root.getParent().getClass());
		DefaultTSView = main_succinct;
		webcontentlist.scrollbar2guard=mBar;
		DetachContentView();
		contentview.setVisibility(View.VISIBLE);

		if(!opt.getBottombarOnBottom())
			webcontentlist.SwitchingSides();
		//SplitViewGuarder_ svGuard = (SplitViewGuarder_) contentview.findViewById(R.id.svGuard);
		//svGuard.SplitViewsToGuard.add(webcontentlister);


		//PageSlider.SCViewToMute = (ScrollViewmy) webholder.getParent();
		PageSlider.IMSlider = IMPageCover;
		IMPageCover.setPageSliderInf(new PageSliderInf() {
			protected Bitmap PageCache;
			@Override
			public void onPreparePage(final IMPageSlider IMPageCover) {
				//CMN.Log("onPreparePage!!!");
				//mPageCanvas.drawColor(Color.WHITE);
				currentPos=0;
				if(currentDictionary!=null&&currentDictionary.mWebView!=null){
					currentPos=currentDictionary.mWebView.currentPos;
				}
				if(Build.VERSION.SDK_INT>23){
					IMPageCover.getForeground().setAlpha(0);
					IMPageCover.setBackground(null);
				}
				if(IMPageCover.getTag()==null) {
					if(Build.VERSION.SDK_INT>23)
						IMPageCover.getForeground().setTint(0x662b4381);
					//IMPageCover.getBackground().setColorFilter(0x662b4381, PorterDuff.Mode.SRC_IN);
					IMPageCover.setTag(false);
				}
				IMPageCover.setTranslationY(0);
				RLContainerSlider PageSlider_ = PageSlider;
				if(PeruseView!=null && ActivedAdapter==PeruseView.leftLexicalAdapter)
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
				HappyMatrix.setScale(Math.round(1.f*PageSlider_.getWidth()/dm.widthPixels),Math.round(1.f*PageSlider_.getHeight()/dm.heightPixels));
				IMPageCover.setImageMatrix(HappyMatrix);

				IMPageCover.setImageDrawable(mPageDrawable);

				if(PageCache.getWidth()!=PageSlider_.getWidth() || PageCache.getHeight()!=PageSlider.getHeight()) {
					//PageCache.setHeight(PageSlider_.getHeight());
					//PageCache.setWidth(PageSlider_.getWidth());
				}

				int painter = 0;
				int painterHash;
				if(PeruseViewAttached()) {
					painter=1;
				} else if(webholder.getChildCount()!=0) {
					painter=2;
				} else {
					painter=3;
				};

				// 重绘
				mPageCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.SRC_IN);

				if(painter==1) {
					PeruseView.webSingleholder.post(() -> {
						//if(PageCache.isRecycled())PageCache = Bitmap.createBitmap(PageCache.getWidth(), PageCache.getHeight(), Bitmap.Config.ARGB_8888);
						PeruseView.webSingleholder.draw(mPageCanvas);
					});
				} else if(painter==2){
					webholder.post(() -> {
						mPageCanvas.translate(0, -WHP.getScrollY());
						WHP.draw(mPageCanvas);
						mPageCanvas.translate(0, WHP.getScrollY());
					});
				} else {
					webSingleholder.draw(new Canvas(PageCache));
				}

				IMPageCover.setVisibility(View.VISIBLE);
				IMPageCover.setAlpha(1.0f);
				if(IMPageCover.getLayoutParams().height!=-1) {
					IMPageCover.getLayoutParams().height=-1;
					IMPageCover.requestLayout();
				}

				//CMN.Log("drawed");
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
					int pos = currentDictionary.mWebView.currentPos+(Math.abs(val)>20*dm.density?(val<0?1:-1):0);
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
				if(Build.VERSION.SDK_INT>23)
					IMPageCover.getForeground().setAlpha(0);
				boolean there = ActivedAdapter instanceof com.knziha.plod.PlainDict.PeruseView.LeftViewAdapter;
				ViewGroup contentview_ = contentview;
				if(there)
					contentview_=PeruseView.contentview;
				if(Dir==1) {contentview_.findViewById(R.id.browser_widget11).performClick();}
				else if(Dir==0) contentview_.findViewById(R.id.browser_widget10).performClick();
				else if(ActivedAdapter==adaptermy && !there) {if(currentPos!=currentDictionary.mWebView.currentPos) currentDictionary.toolbar_title.setText(currentDictionary.currentDisplaying.trim()+" - "+currentDictionary._Dictionary_fName);}
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
						if(PDICMainAppOptions.getHintSearchMode())
							msg.first=PDICMainAppOptions.getUseRegex1()?3:1;

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
						if(PDICMainAppOptions.getHintSearchMode())
							msg.first=PDICMainAppOptions.getUseRegex2()?3:2;
						break;
				}

				iItem_PerwordSearch.setVisible(CurrentViewPage==1);
				iItem_PeruseMode.setVisible(true);
				//iItem_FolderAll.setVisible(false);//折叠
				//iItem_aPageRemember.setVisible(false);//翻忆
				iItem_SaveSearch.setVisible(CurrentViewPage!=1);
				iItem_TintWildRes.setVisible(CurrentViewPage!=1);
				//iItem_PickDict.setVisible(false);
				//iItem_PickSet.setVisible(false);
				//iItem_SaveBookMark.setVisible(false);

				if(msg.first>0) {
					viewPager.post(() ->
						showTopSnack(main_succinct, msg.first==3?R.string.regret:msg.first==1?R.string.fuzzyret:R.string.fullret
								, 0.5f, -1, Gravity.CENTER, false)
					);
				}
			}});

		mlv = (ViewGroup) inflater.inflate(R.layout.mainlistview,null);
		lv = mlv.findViewById(R.id.main_list);
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
			@NonNull @Override
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
		favoriteFolderBtn.setOnLongClickListener(v -> {//长按管理词典
			ReadInMdlibs(new File(opt.pathToMainFolder().append("CONFIG/mdlibs.txt").toString()));
			AgentApplication app = ((AgentApplication) getApplication());
			app.mdict_cache = mdict_cache;
			for(mdict mdTmp:md) {
				if(mdTmp!=null){
					mdict_cache.put(mdTmp.getPath(),mdTmp);
				}
			}
			for(mdict mdTmp:currentFilter) {
				if(mdTmp!=null){
					mdict_cache.put(mdTmp.getPath(),mdTmp);
				}
			}
			if(drawerFragment!=null)
			for(mdict mdTmp:drawerFragment.mdictInternal.values()) {
				if(mdTmp!=null){
					mdict_cache.put(mdTmp.getPath(),mdTmp);
				}
			}
			/* 合符而继统 */
			for(PlaceHolder phI:HdnCmfrt) {
				CosyChair.add(Math.min(phI.lineNumber, CosyChair.size()), phI);
			}
			app.slots=CosyChair;
			app.opt=opt;
			app.mdlibsCon=mdlibsCon;
			app.mdict_cache=mdict_cache;
			CosySofa.clear();
			HdnCmfrt.clear();
			lastLoadedModule=null;
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

		if(opt.getRemPos()) iItem_aPageRemember.setTitle(rem_res=R.string.rem_position_yes);
		if(opt.getClickSearchEnabled()) iItem_ClickSearch.setTitle(iItem_ClickSearch.getTitle()+" √");
		iItem_aPageRemember.setVisible(false);//翻忆
		iItem_JumpPeruse.setVisible(false);//翻忆

		if(TintWildResult.first = opt.getTintWildRes())  iItem_TintWildRes.setTitle(iItem_TintWildRes.getTitle()+" √");

		//iItem_PickDict.setVisible(false);
		iItem_PickSet.setVisible(false);

		iItem_SaveSearch.setVisible(false);
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
				etSearch_ToToolbarMode(1);
			}

			@Override public void onDrawerClosed(@NonNull View arg0) {
				//CMN.Log("onDrawerClosed");
			}
			@Override public void onDrawerSlide(@NonNull View arg0, float arg1) {
			}
			@Override public void onDrawerStateChanged(int newState) {
				if(!mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
					if(bNeedReAddCon) {
						AttachContentView();
						if(contentview.getTag(R.id.image)!=null){
							initPhotoViewPager();
						}
						etSearch_ToToolbarMode(1);
						bNeedReAddCon=false;
					}else
						etSearch_ToToolbarMode(0);
				}
				checkFlags();
			}});
		//mDrawerLayout.setScrimColor(0x00ffffff);

		lv.setAdapter(adaptermy = new ListViewAdapter(webSingleholder));
		lv.setOnItemClickListener(adaptermy);
		lv2.setAdapter(adaptermy2 = new ListViewAdapter2(webholder, R.layout.listview_item1));
		lv2.setOnItemClickListener(adaptermy2);
		mlv1.setAdapter(adaptermy3 = new ListViewAdapter2(webSingleholder));
		mlv1.setOnItemClickListener(adaptermy3);
		mlv2.setAdapter(adaptermy4 = new ListViewAdapter2(webSingleholder));
		mlv2.setOnItemClickListener(adaptermy4);

		fuzzySearchLayer=new AdvancedSearchLogicLayer(opt, (ArrayList<mdict>) md, -1);
		fullSearchLayer=new AdvancedSearchLogicLayer(opt, (ArrayList<mdict>) md, -2);

		adaptermy3.combining_search_result = new resultRecorderScattered(this,md,TintWildResult,fuzzySearchLayer);
		adaptermy4.combining_search_result = new resultRecorderScattered(this,md,TintWildResult,fullSearchLayer);

		etSearch.addTextChangedListener(tw1);
		//ea
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId==EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
				if(taskd!=null){
					return true;
				}
				String key = etSearch.getText().toString().trim();
				if(key.length()>0) Current0SearchText=key;
				int tmp = viewPager.getCurrentItem();
				if(tmp==0 || tmp==2) {
					if(!PDICMainAppOptions.getHistoryStrategy0() /*&& PDICMainAppOptions.getHistoryStrategy1()*/)
						insertUpdate_histroy(key);
					if(!checkDicts()) return true;
					//模糊搜索 & 全文搜索
					if(mAsyncTask!=null)
						mAsyncTask.cancel(true);
					imm.hideSoftInputFromWindow(main.getWindowToken(),0);
					(mAsyncTask=tmp==0?new FuzzySearchTask(PDICMainActivity.this)
							:new FullSearchTask(PDICMainActivity.this)).execute(key);
				}
				else {
					if(!PDICMainAppOptions.getHistoryStrategy0() /*&& (PDICMainAppOptions.getHistoryStrategy2()&&isCombinedSearching|| PDICMainAppOptions.getHistoryStrategy3()&&!isCombinedSearching)*/)
						insertUpdate_histroy(key);
					if(key.length()>0){
						if(!isCombinedSearching && currentDictionary instanceof mdict_web){
							mdict_web webx = ((mdict_web) currentDictionary);
							webx.searchKey = key;
							adaptermy.onItemClick(0);
						}else{
							bIsFirstLaunch=true;
							tw1.onTextChanged(key, 0, 0, 0);
						}
					}
				}
			}
			return true;
		});

		//switch_To_Dict_Idx(adapter_idx);
		if(currentDictionary!=null) {
			lv.post(() -> {
				lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
				setLv1ScrollChanged();
			});
		}else{
			setLv1ScrollChanged();
		}

		systemIntialized=true;
		if(!opt.getInPeruseModeTM()) {
			widget0.setVisibility(View.GONE);
		}else {
			widget0.setVisibility(View.VISIBLE);
			if(opt.getInPeruseMode()) {
				widget0.setImageResource(R.drawable.peruse_ic_on);
				showTopSnack(main_succinct, R.string.peruse_mode
						, 1.f, -1, Gravity.CENTER, false);
			}
		}
		if(PDICMainAppOptions.getSimpleMode() && PDICMainAppOptions.getHintSearchMode())
			showTopSnack(main_succinct, "极简模式"
					, 0.5f, -1, Gravity.CENTER, false);

		if(savedInstanceState!=null) {
			for(int i=0;i<md.size();i++){//遍历所有词典
				mdict mdtmp = md.get(i);
				if(mdtmp!=null) {
					if (savedInstanceState.containsKey("sizeOf" + mdtmp._Dictionary_fName)) {
						int size = savedInstanceState.getInt("sizeOf" + mdtmp._Dictionary_fName);
						mdtmp.combining_search_tree2 = new ArrayList[size];
						for (int ti = 0; ti < size; ti++) {//遍历搜索结果
							if (savedInstanceState.containsKey(mdtmp._Dictionary_fName + "@" + ti)) {
								mdtmp.combining_search_tree2[ti] = savedInstanceState.getIntegerArrayList(mdtmp._Dictionary_fName + "@" + ti);
							}
						}
					}
					if (savedInstanceState.containsKey("sizeOf_4" + mdtmp._Dictionary_fName)) {
						int size = savedInstanceState.getInt("sizeOf_4" + mdtmp._Dictionary_fName);
						mdtmp.combining_search_tree_4 = new ArrayList[size];
						for (int ti = 0; ti < size; ti++) {//遍历搜索结果
							if (savedInstanceState.containsKey(mdtmp._Dictionary_fName + "@_4" + ti)) {
								mdtmp.combining_search_tree_4[ti] = savedInstanceState.getIntegerArrayList(mdtmp._Dictionary_fName + "@_4" + ti);
							}
						}
					}
				}
			}
			adaptermy3.combining_search_result.invalidate();
			adaptermy3.notifyDataSetChanged();

			adaptermy4.combining_search_result.invalidate();
			adaptermy4.notifyDataSetChanged();


			CurrentViewPage = savedInstanceState.getInt("CVP", 1);
			if(CurrentViewPage==0 || CurrentViewPage==2){//亮A
				viewPager.setCurrentItem(CurrentViewPage,true);
			}


			int[] arr2 = savedInstanceState.getIntArray("P_L2");
			if(arr2!=null)
				pendingLv2Pos=arr2;

			int[] arr3 = savedInstanceState.getIntArray("P_M1");
			mlv1.post(() -> {
				if(arr3!=null) mlv1.setSelectionFromTop(arr3[0], arr3[1]);
			});
			int[] arr4 = savedInstanceState.getIntArray("P_M2");
			mlv2.post(() -> {
				if(arr4!=null) mlv2.setSelectionFromTop(arr4[0], arr4[1]);
			});

			boolean canAddPeruseView=true;
			int dbrowser = savedInstanceState.getInt("DB", -1);
			if(dbrowser!=-1){
				(dbrowser==1?findViewById(R.id.browser_widget5)
						:findViewById(R.id.browser_widget6)).performClick();
				dbrowser = savedInstanceState.getInt("DBPos", -1);
				if(dbrowser!=-1) {
					DBrowser.pendingDBClickPos = dbrowser;
					canAddPeruseView=opt.getDBMode()!=DBroswer.SelectionMode_peruseview;
				}
			}
			else if(contentview.getParent()!=null){
				int val = savedInstanceState.getInt("lv_pos",-1);
				if(val>=0) {
					switch (savedInstanceState.getInt("lv_id",-1)){
						case 1:
							ActivedAdapter=adaptermy;
						break;
						case 2:
							pendingLv2ClickPos=val;
						break;
					}
					if(ActivedAdapter!=null)
						ActivedAdapter.onItemClick(val);
				}
			}

//			if(canAddPeruseView){
//				ArrayList<Integer> data = savedInstanceState.getIntegerArrayList("p_data");
//				if(data!=null){
//					getPeruseView().prepareJump(this, savedInstanceState.getString("p_key"), data, savedInstanceState.getInt("p_adaidx"));
//					int val = savedInstanceState.getInt("lvp_pos",-1);
//					if(val!=-1){
//						getPeruseView().prepareClick(val);
//					}
//					AttachPeruseView(true);
//				}
//			}
		}

		File additional_config = new File(opt.pathToMainFolder().append("appsettings.txt").toString());
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

		checkMargin(this);

		if(opt.getInPageSearchVisible())
			toggleInPageSearch(false);

		if(opt.getBottomNavigationMode()==1)
			setBottomNavigationType(1, null);

		if(false) {//按
			WebView wv = new WebView(getBaseContext());
			snack_holder.addView(wv, 0);
			wv.setVisibility(View.INVISIBLE);
			wv.loadUrl("file:///android_asset/load.html");
			wv.postDelayed(new Runnable() {
				@Override
				public void run() {
					((ViewGroup)wv.getParent()).removeView(wv);
					wv.removeAllViews();
					wv.destroy();
				}
			}, 2500);
		}

		//tg
//		try {
//			md.add(new mdict_txt("/sdcard/1.txt", this));
//			switch_To_Dict_Idx(md.size()-1, false, false);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		//startActivity(new Intent().putExtra("realm",8).setClass(this, SettingsActivity.class));
		//popupWord("History of love", 0, 0, -1);

/*		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, "hello"); //Search Query
		intent.putExtra(Intent.EXTRA_HTML_TEXT, "<div style=\"color:red\">hello</div>"); //Search Query
		intent.setPackage("com.ichi2.anki");
		intent.setType("text/plain");
		startActivity(intent);*/

		//bottombar.findViewById(R.id.browser_widget2).performLongClick();
		//bottombar.findViewById(R.id.browser_widget5).performLongClick();

		//etSearch.setText("fair use");
		//if(MainPageSearchbar!=null) MainPageSearchetSearch.setText("15");
		//if(false)
		root.postDelayed(() -> {
			//lv.getChildAt(0).performClick();
//			View cover=((ViewGroup) currentDictionary.mWebView.getParent()).getChildAt(0).findViewById(R.id.cover);
//			cover.setTag(0); cover.performClick();
//			ucc.setInvoker(currentDictionary, currentDictionary.mWebView);

			//ReadText("I've worked with TTS a couple of years ago and remember, that there were not so much configuration possibilities.，中国网科技1月6日讯 针对近期比特大陆大规模裁员消息。比特大陆创始人、第一大股东詹克团今日下午再发公开信，称坚决反对这样裁员，自己必须要站出来。他认为，比特大陆有足够的金支持现有员工成本，在AI市场可以像矿机一样从零做到世界第一。");
			//showTTS();
			//showSoundTweaker();
			//JumpToPeruseModeWithWord("doctrine");
			//myWebCClient.onShowCustomView(v, null);

			//widget12.performLongClick();

			//toolbar.setPopupTheme(R.style.toolbarBaseTheme_dark);
		}, 350);

		//showAppTweaker();
		if(CMN.testFLoatSearch)
			startActivity(new Intent(this,FloatSearchActivity.class).putExtra("EXTRA_QUERY", "happy"));

		//onClick(findViewById(R.id.browser_widget6));
		//JumpToWord("crayon", 1);

		//Intent i = new Intent(this,dict_manager_activity.class); startActivity(i);
		processIntent(getIntent());

		refreshUIColors();

		//lv.setFastScrollEnabled(false);
		//lv.setFastScrollStyle(R.style.AppBright);

		//lv.setVerticalScrollBarEnabled(false);
		//lv.setScrollBarStyle(R.style.AppTheme);
		//lv.setVerticalScrollBarEnabled(true);
		//lv.invalidate();


		if(GlobalOptions.isDark)
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
				//	Object Scrollbar = ScrollCacheFiel
				//yt	d.get(pickDictDialog.mRecyclerView);
				//    Drawable ScrollbarDrawable = (Drawable) ScrollBarDrawableField.get(Scrollbar);
				//    ScrollbarDrawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
				//}
			} catch (Exception e) {
				CMN.Log(e);
			}
	}

	@Override
	protected View getIMPageCover() {
		return IMPageCover;
	}

	@Override
	protected String getLastPlanName() {
		return opt.getLastPlanName();
	}

	@Override
	protected void setLastPlanName(String setName) {
		opt.putLastPlanName(setName);
	}

	@Override
	protected String getLastMdFn() {
		return opt.getLastMdFn();
	}

	@Override
	public void setLastMdFn(String setName) {
		opt.putLastMd(setName);
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
			filter_count = CosySofa.size();
			CMN.Log("直接返回！！！", filter_count);
			currentFilter.ensureCapacity(filter_count);
			for (int i = 0; i < filter_count; i++) {
				currentFilter.add(null);
				//CMN.Log(CosySofa.get(i).name);
			}
			return;
		}
		CMN.Log("LoadLazySlots…");
		AgentApplication app = ((AgentApplication) getApplication());
		ReusableBufferedReader in = new ReusableBufferedReader(new FileReader(modulePath), app.get4kCharBuff(), 4096);
		CosySofa.clear();
		HdnCmfrt.clear();
		filter_count=hidden_count=0;
		do_LoadLazySlots(in, CosyChair);
		HdnCmfrt.ensureCapacity(filter_count+hidden_count);
		currMdlTime=lm;
		lastLoadedModule=moduleName;
		lazyLoaded=lazyLoad;
		app.set4kCharBuff(in.cb);
	}

	private void setLv1ScrollChanged() {
		lv.setOnScrollChangeListener(new ListViewmy.OnScrollChangeListener() {
			int lastVisible=-1;
			int lastOff=-1;
			@Override
		public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if(lastVisible!=-1)
					if(lv.getChildAt(0)!=null) {
						if(lv.getFirstVisiblePosition()!=lastVisible || lv.getChildAt(0).getTop()!=lastOff) {
							bNeedSaveViewStates =true;
						}
						lastOff=lv.getChildAt(0).getTop();
					}
				lastVisible=lv.getFirstVisiblePosition();
				//CMN.Log("onScrollChange");
		}});
	}

	public AdvancedSearchLogicLayer fuzzySearchLayer;
	public AdvancedSearchLogicLayer fullSearchLayer;
	public AdvancedSearchLogicLayer currentSearchLayer;
	public static class AdvancedSearchLogicLayer extends com.knziha.plod.dictionary.mdict.AbsAdvancedSearchLogicLayer {
		public final ArrayList<mdict> md;
		final PDICMainAppOptions opt;
		Pattern currentPattern;
		private String currentSearchText;
		private String currentPlainText;

		public AdvancedSearchLogicLayer(PDICMainAppOptions opt, ArrayList<mdict> md, int type) {
			this.opt = opt;
			this.md = md;
			this.type = type;
		}

		@Override
		public ArrayList<Integer>[] getCombinedTree(int DX) {
			if(combining_search_tree!=null && DX<combining_search_tree.size())
				return combining_search_tree.get(DX);
			return null;
		}

		@Override
		public void setCombinedTree(int DX, ArrayList<Integer>[] _combining_search_tree) {
			combining_search_tree.set(DX, _combining_search_tree);
		}

		@Override
		public ArrayList<Integer>[] getInternalTree(com.knziha.plod.dictionary.mdict md){
			return type==-1?md.combining_search_tree2:(type==-2?md.combining_search_tree_4:null);
		}

		@Override
		public Pattern getBakedPattern() {
			return currentPattern;
		}

		@Override
		public void bakePattern(String plainPattern, String _currentSearchText) {
			currentPlainText=plainPattern;
			currentSearchText=_currentSearchText;
			if(currentSearchText==null)
				currentPattern=null;
			else{
				currentPlainText = VerbatimSearchTask.Pattern_VerbatimDelimiter.matcher(currentPlainText).replaceAll(" ");
				try {
					currentPattern = Pattern.compile(currentSearchText, Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					currentPattern = Pattern.compile(currentSearchText,Pattern.CASE_INSENSITIVE|Pattern.LITERAL);
				}
			}
		}

		@Override
		public String getBakedPatternStr(boolean useInPageRegex) {
			return useInPageRegex?currentSearchText:currentPlainText;
		}
	}

	/** 如有必要，重建日志文件 */
	private void CheckGlideJournal() {
		String path=opt.pathToGlide(getApplicationContext());
		File thumbs_dir=new File(path);
		if(!thumbs_dir.isDirectory())
			thumbs_dir.mkdirs();
		File journal_file = new File(path, "journal");
		if(opt.getUseLruDiskCache()){
			if(!journal_file.exists()){
				Pattern p = Pattern.compile("[a-z0-9_-]+\\.[0-9]{1,3}");
				File[] arr = thumbs_dir.listFiles(name -> !name.isDirectory() && p.matcher(name.getName()).matches());
				if(arr!=null){
					ArrayList<File> as = new ArrayList<>(Arrays.asList(arr));
					Collections.sort(as, (f1, f2) ->
					{long ret=f1.lastModified()-f2.lastModified();if(ret<0)return -1;if(ret>0)return 1;return 0;});
					long size_count=0; int trim_start=-1;
					for (int i = 0; i < as.size(); i++) {
						size_count+=as.get(as.size()-i-1).length();
						if(size_count>= DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE){
							trim_start=i;
							break;
						}
					}
					if(trim_start!=-1)
						as.subList(0, as.size() - trim_start + 1).clear();
					//size_count=0;
					//for (int i = 0; i < as.size(); i++)   size_count+=as.get(i).length();
					//CMN.Log("oh oh",size_count-DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
					try {
						BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(journal_file));
						bo.write("libcore.io.DiskLruCache\n1\n1\n1\n\n".getBytes());
						for (File fn:as) {String name=fn.getName();name=name.substring(0,name.lastIndexOf(".")); bo.write(("DIRTY "+name+  ("\nCLEAN "+name+" "+fn.length())  +"\n").getBytes(StandardCharsets.US_ASCII));}
						bo.flush();bo.close();
					} catch (Exception ignored) {}
				}
			}
		}else{
			journal_file.delete();
		}
	}

	@Override
	public String getSearchTerm(){
		return etSearch.getText().toString();
	}

	TextWatcher tw1=new TextWatcher() { //tw
		//tc
		public void onTextChanged(CharSequence s, int start, int before, int count) {
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
								if(input.length()!=0)
									launchVerbatimSearch(input,!firstTag.contains("~"));
								return;
							}
						}
					}
					if(isCombinedSearching){
						if(lianHeTask!=null)
							lianHeTask.cancel(false);
						if(!checkDicts()) return;
						String key = s.toString();
						if(!key.equals(CombinedSearchTask_lastKey))
							lianHeTask = new CombinedSearchTask(PDICMainActivity.this).execute(key);
						else if(bIsFirstLaunch){
							/* 接管历史纪录 */
							bIsFirstLaunch=false;
							if(recCom.allWebs || !isContentViewAttached() && mdict.processText(key).equals(mdict.processText(String.valueOf(adaptermy2.combining_search_result.getResAt(0)))))
							{
								adaptermy2.onItemClick(null, adaptermy2.getView(0, null, null), 0, 0);
							}
						}
					}
					else try {
						if(!checkDicts()) return;
						String key = s.toString().trim();
						if(PDICMainAppOptions.getSearchUseMorphology())
							key = ReRouteKey(key, false);
						int idx=currentDictionary.lookUp(key);
						//CMN.show("单本搜索 ： "+idx);
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
										/* 接管历史纪录 */
										adaptermy.onItemClick(null, null, idx, 0);
									}
								}
							}
							bIsFirstLaunch=false;
						}
					} catch (Exception e) { e.printStackTrace(); }
				}
			}else {
				if(PDICMainAppOptions.getSimpleMode() && currentDictionary!=null && mdict.class.equals(currentDictionary.getClass()))
					adaptermy.notifyDataSetChanged();
				if(lv2.getVisibility()==View.VISIBLE)
					lv2.setVisibility(View.INVISIBLE);
			}
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
		//CMN.Log("----->onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);

		if(systemIntialized){
			if(DBrowser!=null){
				savedInstanceState.putInt("DB",DBrowser.getFragmentId());
				savedInstanceState.putInt("DBPos",DBrowser.currentPos);
			}

			View VZero = lv2==null?null:lv2.getChildAt(0);
			if(VZero!=null)
				savedInstanceState.putIntArray("P_L2",new int[] {lv2.getFirstVisiblePosition(),VZero.getTop()});

			if(PrevActivedAdapter!=null){
				savedInstanceState.putInt("lv_id", PrevActivedAdapter.getId());
				savedInstanceState.putInt("lv_pos", PrevActivedAdapter.lastClickedPos);
			}else if(ActivedAdapter!=null){
				savedInstanceState.putInt("lv_id", ActivedAdapter.getId());
				savedInstanceState.putInt("lv_pos", ActivedAdapter.lastClickedPos);
			}

			if(PeruseViewAttached()){
				savedInstanceState.putIntegerArrayList("p_data", PeruseView.data);
				savedInstanceState.putString("p_key", PeruseView.etSearch.getText().toString());
				savedInstanceState.putInt("p_adaidx", PeruseView.adapter_idx);
				if(PeruseView.ActivedAdapter != null){
					savedInstanceState.putInt("lvp_id", PeruseView.ActivedAdapter.getId());
					savedInstanceState.putInt("lvp_pos", PeruseView.ActivedAdapter.lastClickedPos);
				}
			}
		}
	}

	protected void launchVerbatimSearch(String input,final boolean isStrict) {
		if(!checkDicts()) return;
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
		int msg;
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
			msg=R.string.fuzzyret;
		}else{//亮B
			//etSearch.setText(lastFullKeyword);
			browser_widget4.setImageDrawable(full_search_drawable_pressed);
			browser_widget3.setImageDrawable(fuzzy_search_drawable);
			msg=R.string.fullret;
		}
		CurrentViewPage = new_curr;
		showTopSnack(main_succinct, msg
				, 0.5f, -1, Gravity.CENTER, false);
	}

	private void setStatusBarColor(){
		Window window = getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		if(Build.VERSION.SDK_INT>=21) {
			window.setStatusBarColor(MainBackground);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}

	@Override
	protected void onDestroy(){
		//CMN.Log("main_onDestroy");
		dumpSettiings();
		new File(opt.pathToMainFolder().toString()).setLastModified(System.currentTimeMillis());
		FilePickerDialog.clearMemory(getBaseContext());
		drawerFragment.onDestroy();
		cancleToast();
		super.onDestroy();
	}

	@Override
	protected void scanSettings(){
		super.scanSettings();
		CMN.MainBackground = MainBackground = opt.getMainBackground();
		//getWindow().setNavigationBarColor(MainBackground);
		CMN.FloatBackground = opt.getFloatBackground();

		new File(opt.pathToDatabases().toString()).mkdirs();
		//文件网络
		//SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
		isCombinedSearching = opt.isCombinedSearching();
		//opt.globalTextZoom = read.getInt("globalTextZoom",dm.widthPixels>900?50:80);
		opt.getLastMdlibPath();
		if(opt.lastMdlibPath==null || !new File(opt.lastMdlibPath).exists()) {
			opt.lastMdlibPath = opt.pathToMainFolder().append("mdicts").toString();
			new File(opt.lastMdlibPath).mkdirs();
		}

		setStatusBarColor();
	}

	private void dumpSettiings(){
		Editor putter = null;
		//CMN.Log(webcontentlist.isDirty,"dumpSettiings", webcontentlist.getPrimaryContentSize());
		if(webcontentlist.isDirty)
			putter = opt.edit().putInt("BBS",webcontentlist.getPrimaryContentSize());
		if(checkFlagsChanged())
			opt.setFlags((putter==null?putter=opt.edit():putter), 0);
		if(putter!=null)
			putter.apply();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.menu, menu);
		CMN.Log("onCreateOptionsMenu");
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
		//CMN.Log("onPause");
		try {
			super.onPause();
		} catch (Exception ignored) { }
		if(PeruseView!=null)
			PeruseView.dismissDialogOnly();
		//removeBlack();
		if(systemIntialized)
			checkDictionaryProject(true);

		//pg
//		CMN.rt();
//		for(mdict mdTmp:md){
//			try {
//				mdTmp.putSates();
//			} catch (IOException e) {
//				CMN.Log(e);
//			}
//		}
//		CMN.pt(md.size(), "put 时间：");
//		CMN.rt();
//		dumpViewStates();
//		CMN.pt(md.size()+" 单典写入时间：");

	}

	@Override
	protected void onResume() {
		super.onResume();
		if(bNeedSaveViewStates && systemIntialized && currentDictionary!=null && !PDICMainAppOptions.getSimpleMode()){
			bNeedSaveViewStates=false;
			currentDictionary.lvPos = lv.getFirstVisiblePosition();
		}
	}

	@Override
	protected void onStop() {
		try {
			super.onStop();
		} catch (Exception ignored) { }
	}

	private void checkDictionaryProject(boolean performSave) {
		if(bNeedSaveViewStates && currentDictionary!=null) {
			int pos = lv.getFirstVisiblePosition();
			if(currentDictionary.lvPos != pos && !PDICMainAppOptions.getSimpleMode()){
				currentDictionary.lvPos = pos;
				if(lv.getChildAt(0)!=null) currentDictionary.lvPosOff=lv.getChildAt(0).getTop();
				currentDictionary.dumpViewStates();
			}
			bNeedSaveViewStates =false;
		}
		if(performSave && dirtyMap.size()>0){
			CMN.rt();
			if(dirtyMap.size()==1 && SolveOneUIProject(dirtyMap.iterator().next())){
				CMN.pt(currentDictionary+" 一典配置保存耗时：");
			} else {
				dumpViewStates();
				CMN.pt("dumpViewStates耗时：");
			}
			dirtyMap.clear();
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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		focused=hasFocus;
		super.onWindowFocusChanged(hasFocus);
		//CMN.Log("onWindowFocusChanged", hasFocus);
		if(systemIntialized && hasFocus){
			fix_full_screen(getWindow().getDecorView());
			if(textToSetOnFocus!=null){
				etSearch.setText(textToSetOnFocus);
				textToSetOnFocus=null;
			}
			if(PDICMainAppOptions.locale==null)
				recreate();
			checkColors();
			//file-based UI-less command tool
		}
	}

	@Override
	public void fix_full_screen(@Nullable View decorView) {
		if(decorView==null) decorView=getWindow().getDecorView();
		do_fix_full_screen(decorView, PDICMainAppOptions.isFullScreen(), PDICMainAppOptions.isFullscreenHideNavigationbar());
	}

	private void checkColors() {
		if(systemIntialized) {
			if(PDICMainAppOptions.getShowPasteBin()!=PDICMainAppOptions.getShowPasteBin(SFStamp)){
				drawerFragment.SetupPasteBin();
			}
			if(PDICMainAppOptions.getKeepScreen()!=PDICMainAppOptions.getKeepScreen(SFStamp)){
				if(PDICMainAppOptions.getKeepScreen()){
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}else{
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			}
			if(PDICMainAppOptions.getSimpleMode()!=PDICMainAppOptions.getSimpleMode(QFStamp)){
				adaptermy.notifyDataSetChanged();
			}
			if(CMN.CheckSettings!=0){
				if(CMN.checkRCSP()){
					if(ActivedAdapter!=null){
						String val = "window.rcsp="+mdict.MakeRCSP(opt)+"; highlight(null);";
						CMN.Log("checkRCSP!!",val, mdict.MakeRCSP(opt)&0x10);
						webviewHolder=ActivedAdapter.webviewHolder;
						if(webviewHolder!=null){
							int cc = webviewHolder.getChildCount();
							for (int i = 0; i < cc; i++) {
								if(webviewHolder.getChildAt(i) instanceof LinearLayout){
									ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(i);
									if(webHolder.getChildAt(1) instanceof WebView){
										((WebView)webHolder.getChildAt(1))
												.evaluateJavascript(val,null);
									}
								}
							}
						}
						if(popupContentView!=null && popupContentView.getParent()!=null){
							popupWebView.evaluateJavascript(val,null);
						}
					}
				}
			}
			if(CMN.MainBackground != MainBackground || CMN.GlobalPageBackground!=GlobalPageBackground ) {
				IMPageCover.setTag(false);
				if(PeruseView!=null) PeruseView.IMPageCover.setTag(false);
				GlobalPageBackground=CMN.GlobalPageBackground;
				MainBackground=CMN.MainBackground;
				refreshUIColors();
			}
			if(drawerFragment.sw4.isChecked()!=GlobalOptions.isDark){
				switch_dark_mode(GlobalOptions.isDark);
			}
			checkFlags();
		}
	}

	void refreshUIColors() {
		boolean isHalo=!GlobalOptions.isDark;
		int filteredColor = isHalo?MainBackground:ColorUtils.blendARGB(MainBackground, Color.BLACK, ColorMultiplier_Wiget);//CU.MColor(MainBackground,ColorMultiplier);
		viewPager.setBackgroundColor(AppWhite);
		lv2.setBackgroundColor(AppWhite);
		bottombar.setBackgroundColor(filteredColor);
		toolbar.setBackgroundColor(filteredColor);

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
		if(MainPageSearchbar!=null)
			MainPageSearchbar.setBackgroundColor(filteredColor);
		widget0.getBackground().setColorFilter(filteredColor, PorterDuff.Mode.SRC_IN);

		filteredColor = isHalo?GlobalPageBackground:ColorUtils.blendARGB(GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
		WHP.setBackgroundColor(filteredColor);
		if(widget12.getTag(R.id.image)==null){
			webSingleholder.setBackgroundColor(filteredColor);
		}
		//showT(Integer.toHexString(filteredColor)+" "+Integer.toHexString(GlobalPageBackground));
	}

	public void animateUIColorChanges() {
		mHandle.removeMessages(331122);
		animator = 0.1f;
		animatorD = 0.15f;
		mHandle.sendEmptyMessage(331122);
		boolean isChecked = AppWhite==Color.BLACK;
		fix_pw_color();
		fix_dm_color();
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

		Flag mflag = new Flag();
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			//return lstItemViews.get(position);
			ViewHolder vh=convertView==null?new ViewHolder(PDICMainActivity.this, R.layout.listview_item0, parent):(ViewHolder)convertView.getTag();
			String currentKeyText = currentDictionary.getEntryAt(position,mflag);
			if(currentDictionary.hasVirtualIndex()){
				int tailIdx=currentKeyText.lastIndexOf(":");
				if(tailIdx>0)
					currentKeyText=currentKeyText.substring(0, tailIdx);
			}

			if( vh.title.getTextColors().getDefaultColor()!=AppBlack) {
				//decorateBackground(vh.itemView);
				vh.title.setTextColor(AppBlack);
			}

			vh.title.setText(currentKeyText);
			if(position==0 && mdict_asset.class==currentDictionary.getClass()) {
				vh.subtitle.setText(Html.fromHtml("<font color='#2B4391'> < "+"欢迎使用平典"+packageName()+" ></font >"));
			}
			else {
				if(mflag.data!=null)
					vh.subtitle.setText(Html.fromHtml(currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+mflag.data+" ></font >"));
				else
					vh.subtitle.setText(currentDictionary._Dictionary_fName);
			}
			vh.itemView.setTag(R.id.position,position);

			return vh.itemView;
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
						//CMN.Log("回退前暂存位置 ", current_webview.getScrollX(), current_webview.getScrollY(),currentDictionary.webScale);
					}
				lastClickTime=System.currentTimeMillis();
			}
			if(!(currentDictionary instanceof mdict_txt) && !opt.getHistoryStrategy0() && opt.getHistoryStrategy6() && opt.getHistoryStrategy8()==2)
				insertUpdate_histroy(currentDictionary.currentDisplaying);
		}

		@Override
		public void ClearVOA() {
			super.ClearVOA();
			if(currentDictionary!=null) {
				//CMN.Log("江河湖海",currentDictionary.expectedPosX,currentDictionary.expectedPos,currentDictionary.webScale);
				if(opt.getRemPos())
					avoyager.put(currentDictionary.lvClickPos, new ScrollerRecord(currentDictionary.mWebView.expectedPosX,currentDictionary.mWebView.expectedPos,currentDictionary.webScale));
			}
		}

		int currentDictionaryToken;

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			userCLick=true;
			lastClickedPosBeforePageTurn=-1;
			bNeedReAddCon=false;
			lastClickedPos = pos;
			super.onItemClick(parent, view, pos, id);

		}

		@Override
		public void onItemClick(int pos) {//lv1
			shuntAAdjustment();
			if(opt.getInPeruseModeTM() && opt.getInPeruseMode()) {
				getPeruseView().ScanSearchAllByText(currentDictionary.getEntryAt(pos), PDICMainActivity.this, true, updateAI);
				AttachPeruseView(true);
				//CMN.Log(PeruseView.data);
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				return;
			}
			etSearch_ToToolbarMode(1);
			setContentBow(opt.isContentBow());
			if(DBrowser!=null) return;
			lastClickedPosBeforePageTurn = lastClickedPos;
			super.onItemClick(pos);
			ActivedAdapter=this;
			boolean shunt=currentDictionary instanceof mdict_web;
			if(pos<0){
				if(pos<-1||shunt){
					show(R.string.endendr);
					return;
				}
			}

			if(!bOnePageNav && pos>=getCount()) {
				lastClickedPos = getCount()-1;
				show(R.string.endendr);
				return;
			}

			currentDictionary.initViewsHolder(PDICMainActivity.this);

			if(currentDictionary.hashCode()!=currentDictionaryToken) {
				currentDictionaryToken=currentDictionary.hashCode();
				ClearVOA();
			}
			currentDictionary.lvClickPos=pos;

			toolbar.getMenu().findItem(R.id.toolbar_action2).setVisible(false);//Ser
			iItem_PerwordSearch.setVisible(false);
			iItem_PeruseMode.setVisible(false);
			iItem_aPageRemember.setVisible(true);//翻忆
			iItem_JumpPeruse.setVisible(true);//翻忆
			iItem_InPageSearch.setVisible(true);
			iItem_ClickSearch.setVisible(true);
			//iItem_SaveSearch.setVisible(false);
			//iItem_TintWildRes.setVisible(false);
			//iItem_PickDict.setVisible(true);
			iItem_PickSet.setVisible(false);
			iItem_SaveBookMark.setVisible(true);


			if(webSingleholder.getVisibility()!=View.VISIBLE)
				webSingleholder.setVisibility(View.VISIBLE);
			if(WHP.getVisibility()==View.VISIBLE) {
				if(webholder.getChildCount()!=0)
					webholder.removeAllViews();
				WHP.setVisibility(View.GONE);
			}
			if(widget14.getVisibility()==View.VISIBLE) {
				widget13.setVisibility(View.GONE);
				widget14.setVisibility(View.GONE);
			}


			WebViewmy current_webview = currentDictionary.mWebView;
			current_webview.fromCombined=0;
			float desiredScale=-1;
			if(opt.getRemPos() && !shunt) {
				ScrollerRecord pagerec;
				OUT:
				if(System.currentTimeMillis()-lastClickTime>300)//save our postion
				if(!current_webview.isloading && lastClickedPosBeforePageTurn>=0 && (webSingleholder.getChildCount()!=0 || false/*todo 开放连续的历史纪录 ?*/)) {
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
					//CMN.Log("保存位置 "+ current_webview.getScrollY());
				}

				lastClickTime=System.currentTimeMillis();

				pagerec = avoyager.get(pos);
				if(pagerec!=null) {
					currentDictionary.mWebView.expectedPos = pagerec.y;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
					currentDictionary.mWebView.expectedPosX = pagerec.x;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
					desiredScale=pagerec.scale;
					//CMN.Log(avoyager.size()+"~"+pos+"~取出旧值"+currentDictionary.expectedPos+" scale:"+avoyager.get(pos).scale);
				}else {
					currentDictionary.mWebView.expectedPos=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
					currentDictionary.mWebView.expectedPosX=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				}
				//showT(""+currentDictionary.expectedPos);
				if(rem_res!=R.string.rem_position_yes){
					iItem_aPageRemember.setTitle(rem_res=R.string.rem_position_yes);
				}
			}
			else{
				currentDictionary.mWebView.expectedPos=0;
				currentDictionary.mWebView.expectedPosX=0;
				if(rem_res!=R.string.rem_position){
					iItem_aPageRemember.setTitle(rem_res=R.string.rem_position);
				}
			}

			if(opt.getInheritePageScale())
				desiredScale=currentDictionary.webScale;

			lastClickedPos = pos;
			if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				etSearch.clearFocus();
			}

			ViewGroup somp = (ViewGroup) contentview.getParent();//sanity check
			if(somp!=main){
				if(somp!=null) DetachContentView();;
				AttachContentView();
			}

			//webholder.addView(md.rl);
			ViewGroup someView = currentDictionary.rl;
			if(someView.getParent()!=webSingleholder) {
				if(someView.getParent()!=null) ((ViewGroup)someView.getParent()).removeView(someView);
				webSingleholder.addView(someView);
			}
			if(webSingleholder.getChildCount()>1) {
				for(int i=webSingleholder.getChildCount()-1;i>=0;i--)
					if(webSingleholder.getChildAt(i)!=someView)
						webSingleholder.removeViewAt(i);
			}
			currentDictionary.mWebView.fromCombined=0;

			TurnPageEnabled=opt.getPageTurn1();
			PageSlider.TurnPageEnabled=TurnPageEnabled&&opt.getTurnPageEnabled();

			layoutScrollDisabled=true;
			if(bOnePageNav)
				desiredScale=111;
			if(opt.getAutoReadEntry() && !PDICMainAppOptions.getTmpIsAudior(currentDictionary.tmpIsFlag))
				currentDictionary.mWebView.setTag(R.drawable.voice_ic, false);
			currentDictionary.renderContentAt(desiredScale,adapter_idx,0,null, lastClickedPos);
			contentview.setTag(R.id.image, PhotoPagerHolder!=null&&PhotoPagerHolder.getParent()!=null?false:null);

			String key = currentKeyText = currentDictionary.currentDisplaying;

			decorateContentviewByKey(null,key);
			if(!(currentDictionary instanceof mdict_txt) && !PDICMainAppOptions.getHistoryStrategy0() && PDICMainAppOptions.getHistoryStrategy6() &&(userCLick || PDICMainAppOptions.getHistoryStrategy8()==0) && (!shunt && pos==0)) {
				insertUpdate_histroy(key);
			}
			//showT("查时: "+(System.currentTimeMillis()-stst));

			bWantsSelection=currentDictionary.mWebView.fromCombined==0;
			userCLick=false;
			if(PDICMainAppOptions.getInPageSearchAutoUpdateAfterClick()){
				prepareInPageSearch(key, true);
			}
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

	public class ListViewAdapter2 extends BasicAdapter {
		int itemId = R.layout.listview_item0;
		public ListViewAdapter2(ViewGroup vg, int resId)
		{
			this(vg);
			itemId = resId;
		}
		//构造
		public ListViewAdapter2(ViewGroup vg)
		{
			this.webviewHolder=vg;
			combining_search_result = new resultRecorderDiscrete(PDICMainActivity.this);
		}
		@Override
		public int getCount() {
			return combining_search_result.size();
		}

		public int expectedPos;
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			//return lstItemViews.get(position);
			ViewHolder vh;

			CharSequence currentKeyText = combining_search_result.getResAt(position);

			if(convertView!=null){
				vh=(ViewHolder)convertView.getTag();
			}else{
				vh=new ViewHolder(getApplicationContext(), itemId, parent);
				//vh.itemView.setOnClickListener(this);
				//vh.itemView.setOnLongClickListener(MainActivity.this);
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
			if(combining_search_result.mflag.data!=null)
				vh.subtitle.setText(Html.fromHtml(_currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+combining_search_result.mflag.data+" ></font >"));
			else
				vh.subtitle.setText(_currentDictionary._Dictionary_fName);
			if(combining_search_result.getClass()==resultRecorderCombined.class)
				((TextView)vh.subtitle.getTag()).setText(((resultRecorderCombined)combining_search_result).count);
			//vh.itemView.setTag(R.id.position,position);
			return vh.itemView;
		}
		@Override
		public void shutUp() {
			combining_search_result.shutUp();
			notifyDataSetChanged();
		}

		@Override
		public void SaveVOA() {
			if(this!=adaptermy2||opt.getRemPos2()) {
				ScrollerRecord pagerec;
				OUT:
				if (System.currentTimeMillis() - lastClickTime > 400) {//save our postion
					if (this == adaptermy2) {
						if (lastClickedPos >= 0) {
							//avoyager.set(avoyagerIdx, WHP.getScrollY());
							pagerec = avoyager.get(lastClickedPos);
							if (pagerec == null) {
								if (WHP.getScrollY() != 0) {
									pagerec = new ScrollerRecord();
									avoyager.put(lastClickedPos, pagerec);
								} else
									break OUT;
							}
							pagerec.set(0, WHP.getScrollY(), 1);
							//CMN.Log("保存位置(回退)", lastClickedPos, WHP.getScrollY());
						}
					}
					else {
						mdict mdtmp = md.get(combining_search_result.getOneDictAt(lastClickedPos));
						if ((mdtmp.mWebView != null) && lastClickedPos >= 0 && webSingleholder.getChildCount() != 0) {
							//ADA.avoyager.get(ADA.avoyagerIdx).set(mdtmp.mWebView.getScrollX(), mdtmp.mWebView.getScrollY(), mdtmp.webScale);
							pagerec = avoyager.get(lastClickedPos);
							if (pagerec == null) {
								if (mdtmp.mWebView.getScrollX() != 0 || mdtmp.mWebView.getScrollY() != 0 || mdtmp.mWebView.webScale != mdict.def_zoom) {
									pagerec = new ScrollerRecord();
									avoyager.put(lastClickedPos, pagerec);
								} else
									break OUT;
							}
							pagerec.set(mdtmp.mWebView.getScrollX(), mdtmp.mWebView.getScrollY(), mdtmp.webScale);
						}
					}
				}
			}
			lastClickTime=System.currentTimeMillis();
			boolean Kustice = (this!=adaptermy2 && PDICMainAppOptions.getHistoryStrategy4() && combining_search_result.shouldSaveHistory()) || (this==adaptermy2 && PDICMainAppOptions.getHistoryStrategy5());
			if(!PDICMainAppOptions.getHistoryStrategy0() && Kustice && PDICMainAppOptions.getHistoryStrategy8()==2)
				insertUpdate_histroy(currentKeyText);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			if(checkAllWebs(combining_search_result, view, pos)) return;
			main_progress_bar.setVisibility(View.GONE);
			lastClickedPosBeforePageTurn=-1;
			bNeedReAddCon=false;
			userCLick=true;
			super.onItemClick(parent, view, pos, id);
		}


		@Override
		public void onItemClick(int pos){//lv2 mlv1 mlv2
			shuntAAdjustment();
			WHP.touchFlag.first=true;
			if(opt.getInPeruseModeTM() && opt.getInPeruseMode()) {
				PeruseView pv = getPeruseView();
				pv.RestoreOldAI();
				combining_search_result.syncToPeruseArr(pv.data, pos);
				pv.TextToSearch = combining_search_result.getResAt(pos).toString();
				AttachPeruseView(true);
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				return;
			}
			etSearch_ToToolbarMode(1);
			setContentBow(opt.isContentBow());
			if(DBrowser!=null) return;

			lastClickedPosBeforePageTurn = lastClickedPos;

			if(pos<0 || pos>=getCount()) {
				show(R.string.endendr);
				return;
			}
			toolbar.getMenu().findItem(R.id.toolbar_action2).setVisible(false);//Ser
			iItem_PerwordSearch.setVisible(false);
			iItem_PeruseMode.setVisible(false);
			iItem_FolderAll.setVisible(this == adaptermy2);//折叠
			iItem_aPageRemember.setVisible(true);//翻忆
			iItem_JumpPeruse.setVisible(true);//翻忆
			iItem_InPageSearch.setVisible(true);
			iItem_ClickSearch.setVisible(true);
			iItem_SaveSearch.setVisible(false);
			iItem_TintWildRes.setVisible(false);
			//iItem_PickDict.setVisible(false);
			iItem_PickSet.setVisible(this==adaptermy2);
			iItem_SaveBookMark.setVisible(this!=adaptermy2);


			if(this==adaptermy2) {
				if(WHP.getVisibility()!=View.VISIBLE)WHP.setVisibility(View.VISIBLE);
				if(webSingleholder.getVisibility()==View.VISIBLE) {
					if(webSingleholder.getChildCount()!=0)
						webSingleholder.removeAllViews();
					webSingleholder.setVisibility(View.GONE);
				}
			}
			else {
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

			ScrollerRecord pagerec;
			if(this==adaptermy2) {
				if(opt.getRemPos2()) {
					OUT:
					if (((resultRecorderCombined) combining_search_result).scrolled
							&& lastClickedPosBeforePageTurn >= 0
							&& System.currentTimeMillis() - lastClickTime > 300) {
						//CMN.Log("save our postion", lastClickedPosBeforePageTurn, WHP.getScrollY());
						pagerec = avoyager.get(lastClickedPosBeforePageTurn);
						if (pagerec == null) {
							if (WHP.getScrollY() != 0) {
								pagerec = new ScrollerRecord();
								avoyager.put(lastClickedPosBeforePageTurn, pagerec);
							} else
								break OUT;
						}
						pagerec.set(0, WHP.getScrollY(), 1);
						//CMN.Log("保存位置", lastClickedPosBeforePageTurn);
					}

					lastClickTime = System.currentTimeMillis();

					pagerec = avoyager.get(pos);
					if (pagerec != null) {
						combining_search_result.expectedPos = pagerec.y;
						//currentDictionary.mWebView.setScrollY(currentDictionary.expectedPos);
						//CMN.Log("取出旧值", combining_search_result.expectedPos, pos, avoyager.size());
					} else {
						combining_search_result.expectedPos = 0;
						//CMN.Log("新建", combining_search_result.expectedPos, pos);
					}
					if(rem_res!=R.string.rem_position_yes){
						iItem_aPageRemember.setTitle(rem_res=R.string.rem_position_yes);
					}
				}
				else{
					combining_search_result.expectedPos = 0;
					if(rem_res!=R.string.rem_position){
						iItem_aPageRemember.setTitle(rem_res=R.string.rem_position);
					}
				}
			}

			ActivedAdapter=this;
			super.onItemClick(pos);

			contentview.setVisibility(View.VISIBLE);
			if(this!=adaptermy2)
				webholder.removeAllViews();

			if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				etSearch.clearFocus();
			}

			ViewGroup somp = (ViewGroup) contentview.getParent();//sanity check
			if(somp!=main){
				if(somp!=null) DetachContentView();
				AttachContentView();
			}

			combining_search_result.renderContentAt(lastClickedPos, PDICMainActivity.this,this);


			decorateContentviewByKey(null,currentKeyText = combining_search_result.getResAt(pos).toString());
			if(!PDICMainAppOptions.getHistoryStrategy0()) {
				boolean Kustice = (this!=adaptermy2 && PDICMainAppOptions.getHistoryStrategy4() && combining_search_result.shouldSaveHistory()) || (this==adaptermy2 && PDICMainAppOptions.getHistoryStrategy5());
				if(Kustice) {
					if(userCLick||PDICMainAppOptions.getHistoryStrategy8()==0)
						insertUpdate_histroy(currentKeyText);
				}
			}

			userCLick=false;
			bWantsSelection=true;
			if(PDICMainAppOptions.getInPageSearchAutoUpdateAfterClick()){
				prepareInPageSearch(currentKeyText, true);
			}
			contentview.setTag(R.id.image, PhotoPagerHolder!=null&&PhotoPagerHolder.getParent()!=null?false:null);
			TurnPageEnabled=this==adaptermy2?opt.getPageTurn2():opt.getPageTurn1();
			PageSlider.TurnPageEnabled=TurnPageEnabled&&opt.getTurnPageEnabled();
		}


		@Override
		public int getId() {
			return this==adaptermy2?2:this==adaptermy3?3:4;
		}

		@Override
		public String currentKeyText() {
			return combining_search_result instanceof resultRecorderScattered?
					((resultRecorderScattered)combining_search_result).getCurrentKeyText(lastClickedPos)
					:currentKeyText;
		}
	}

	static class ViewHolder {
		final View itemView;
		TextView title;
		TextView subtitle;

		public ViewHolder(Context context, int resId, ViewGroup parent) {
			itemView = LayoutInflater.from(context).inflate(resId, parent, false);
			itemView.setId(R.id.lvitems);
			title = itemView.findViewById(R.id.text);
			subtitle = itemView.findViewById(R.id.subtext);
			itemView.setTag(this);
		}
	}



	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getTag(R.id.click_handled)!=null) return;
		onIdClick(v, v.getId());
	}

	public void onIdClick(View v, int id){
		layoutScrollDisabled=false;
		cancleSnack();
		OUT:
		if(DBrowser!=null) {
			switch(id) {
				case R.id.browser_widget7: break OUT;
				case R.id.browser_widget8:
					DBrowser.toggleFavor();
					break;
				case R.id.browser_widget13:
				case R.id.browser_widget14:
				case R.id.browser_widget9: break OUT;
				case R.id.browser_widget10:
					if(ActivedAdapter instanceof PeruseView.LeftViewAdapter) break OUT;
					DBrowser.goBack();
					break;
				case R.id.browser_widget11:
					if(ActivedAdapter instanceof PeruseView.LeftViewAdapter) break OUT;
					DBrowser.goQiak();
				break;
			}
			return;
		}

		CheckableImageView cb;
		switch(id) {
			//切换翻阅
			case R.id.browser_widget0:{
				if(mainF.getChildCount()!=0) return;
				int msg;
				if(opt.setInPeruseMode(!opt.getInPeruseMode())) {
					widget0.setImageResource(R.drawable.peruse_ic_on);
					msg=R.string.canceld_peruse_mode;
				}else {
					widget0.setImageResource(R.drawable.peruse_ic);
					msg=R.string.peruse_mode;
				}
				showTopSnack(main_succinct, msg
						, 0.5f, -1, Gravity.CENTER, false);
			} break;
			//切换词典
			case R.id.browser_widget1:{
				showChooseDictDialog(0);
			} break;
			//切换搜索模式
			case R.id.toolbar_action1:{
				opt.setCombinedSearching(isCombinedSearching = !isCombinedSearching);
				// switch cs mode will interrupt the user's reading process.
				CombinedSearchTask_lastKey = null;
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
					if(currentDictionary instanceof mdict_web)
						adaptermy.notifyDataSetChanged();
				}
				if(opt.auto_seach_on_switch)
					tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
			} break;
			//返回
			case R.id.ivBack:{
				if((etSearch_toolbarMode&1)==0) {//search
					if(CurrentViewPage==1) {//viewPager
						if(etSearch.getText().toString().trim().length()>0) {
							etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
						}
					}else
						etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
					mDrawerLayout.closeDrawer(GravityCompat.START);
				}else {//back
					widget7.setTag(false);
					onBackPressed();
					etSearch_ToToolbarMode(0);
				}
			} break;
			//切换分组
			case R.id.browser_widget2:{
				if(d!=null) {
					d.dismiss();d=null;
				}
				showChooseSetDialog();
			} break;
			//两大搜索
			case R.id.browser_widget3:{
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
			} break;
			case R.id.browser_widget4:{
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
			} break;
			//收藏和历史纪录
			case R.id.browser_widget5:{
				if(mainF.getChildCount()!=0) return;
				if(DBrowser==null) {
					if(DBrowser_holder!=null) DBrowser=DBrowser_holder.get();
					if(DBrowser==null){
						CMN.Log("重建收藏夹");
						DBrowser_holder = new WeakReference<>(DBrowser = new DBroswer());
					}
					AttachDBrowser();
				}
			} break;
			case R.id.browser_widget6:{
				if(mainF.getChildCount()!=0) return;
				if(DBrowser==null) {
					if(DHBrowser_holder!=null) DBrowser=DHBrowser_holder.get();
					if(DBrowser==null){
						//CMN.Log("重建历史纪录");
						DHBrowser_holder = new WeakReference<>(DBrowser = new DHBroswer());
					}
					AttachDBrowser();
				}
			} break;
			//搜索词典
			case R.id.cb1:{
				etSearch.clearFocus();
				if(Searchbar ==null) {
					Toolbar searchbar = (Toolbar) getLayoutInflater().inflate(R.layout.searchbar, null);
					searchbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
					searchbar.setNavigationOnClickListener(v1 -> {
						searchbar.setVisibility(View.GONE);
						imm.hideSoftInputFromWindow(searchbar.findViewById(R.id.etSearch).getWindowToken(),0);
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
						int sep = CosyChair.size()-fvp;
						boolean found=false;
						for(int i=0,j;i<CosyChair.size();i++) {
							if(i>=sep) j=i-sep;
							else j=i+fvp;
							if(pickDictDialog.SearchPattern.matcher(CosyChair.get(j).name).find()) {
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
						int sep = CosyChair.size()-fvp-1;
						boolean found=false;
						for(int i=CosyChair.size()-1,j;i>=0;i--) {
							if(i>=sep) j=i-sep;
							else j=i+fvp;
							if(pickDictDialog.SearchPattern.matcher(CosyChair.get(j).name).find()) {
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
					this.Searchbar =searchbar;
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				}
				else {
					if(Searchbar.getVisibility()==View.VISIBLE) {
						Searchbar.setVisibility(View.GONE);
						imm.hideSoftInputFromWindow(Searchbar.findViewById(R.id.etSearch).getWindowToken(),0);
					}else {
						Searchbar.setVisibility(View.VISIBLE);
						Searchbar.findViewById(R.id.etSearch).requestFocus();
						imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					}
				}
			} break;
			case R.id.cb3:{
				cb = (CheckableImageView)v;
				cb.toggle();
				opt.setPicDictAutoSer(cb.isChecked());
				if(pickDictDialog!=null) pickDictDialog.isDirty=true;
			} break;
			case R.id.cb2:{
				cb = (CheckableImageView)v;
				cb.toggle();
				opt.setPinPicDictDialog(cb.isChecked());
				if(pickDictDialog!=null) pickDictDialog.isDirty=true;
			} break;
		}
	}

	private void AttachDBrowser() {
		if(DBrowser!=null)
		if(!DBrowser.isAdded()) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.setCustomAnimations(R.anim.history_enter, R.anim.history_enter);
			transaction.add(R.id.mainF, DBrowser);
			transaction.commit();
		} else {
			mainF.addView(DBrowser.getView());
		}
	}

	void AttachContentView() {
		root.addView(contentview, PhotoPager!=null&&PhotoPager.getParent()!=null?2:1);
		viewPager.setVisibility(View.INVISIBLE);
		bottombar.setVisibility(View.INVISIBLE);
		if(popupContentView!=null && popupContentView.getParent()!=null && popupContentView.getVisibility()==View.GONE){
			popupContentView.setVisibility(View.VISIBLE);
		}
//		ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(contentview, "alpha", 0, 1);
//		fadeInContents.setDuration(100);
//		fadeInContents.addListener(new Animator.AnimatorListener() {
//			@Override public void onAnimationStart(Animator animation) {}
//			@Override
//			public void onAnimationEnd(Animator animation) {
//				viewPager.setVisibility(View.INVISIBLE);
//				bottombar.setVisibility(View.INVISIBLE);
//			}
//			@Override public void onAnimationCancel(Animator animation) {}
//			@Override public void onAnimationRepeat(Animator animation) {}
//		});
//		fadeInContents.start();
	}

	void DetachContentView() {
		viewPager.setVisibility(View.VISIBLE);
		bottombar.setVisibility(View.VISIBLE);
		root.removeView(contentview);
//		ObjectAnimator fadeOutContents = ObjectAnimator.ofFloat(contentview, "alpha", 1, 0);
//		fadeOutContents.setDuration(100);
//		fadeOutContents.addListener(new Animator.AnimatorListener() {
//			@Override public void onAnimationStart(Animator animation) {}
//			@Override
//			public void onAnimationEnd(Animator animation) {
//			}
//			@Override public void onAnimationCancel(Animator animation) {}
//			@Override public void onAnimationRepeat(Animator animation) {}
//		});
//		fadeOutContents.start();
		if(PhotoPagerHolder!=null&&PhotoPagerHolder.getParent()!=null)
			root.removeView(PhotoPagerHolder);
		webcontentlist.canClickThrough=false;
	}

	boolean isContentViewAttached() {
		return contentview.getParent()!=null;
	}

	@Override
	void contentviewAddView(View v, int i) {
		contentview.addView(v, i);
	}

	//longclick
	@Override
	public boolean onLongClick(View v) {
		boolean ret = super.onLongClick(v);
		if(ret) return ret;
		switch(v.getId()) {
			case R.id.browser_widget0:{
				//getPeruseView().TextToSearch = currentDictionary.getEntryAt(pos);
				AttachPeruseView(false);
			} break;
			case R.id.browser_widget5:{
				showChooseFavorDialog(0);
			} return true;
			case R.id.lvitems:{
				callDrawerIconAnima();
				if(currentDictionary instanceof mdict_pdf) {
					mdict_pdf pdx = (mdict_pdf) currentDictionary;
					AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
					builder2.setTitle("PDF 选项");

					builder2.setSingleChoiceItems(new String[]{}, 0,
							(dialog, pos) -> {
								switch (pos) {
									case 0: {//提取目录
										if (pdx.mWebView == null) {
											showT("目录尚未加载!");
											return;
										}
										((mdict_pdf) pdx).parseContent();
									}
									break;
									case 1: {//保存目录
										if(pdx.pdf_index!=null){
											File path = getExternalFilesDir(".PDF_INDEX");
											if(!path.exists()) path.mkdirs();
											path = new File(path, pdx.getName());
											BU.printFile(StringUtils.join(pdx.pdf_index, "\n").getBytes(), path);
											if(path.exists())
												showT("保存成功");
										}
									}
									break;
									case 2: {//关键词索引

									}
									break;
								}
								dialog.dismiss();
							});

					String[] Menus = getResources().getStringArray(
							R.array.pdf_option);
					List<String> arrMenu = Arrays.asList(Menus);
					AlertDialog d = builder2.create();
					d.show();

					TextView titleView = d.getWindow().getDecorView().findViewById(R.id.alertTitle);
					titleView.setSingleLine(false);
					titleView.setMovementMethod(LinkMovementMethod.getInstance());
					if (!PDICMainAppOptions.isLarge) titleView.setMaxLines(5);


					d.getListView().setAdapter(new ArrayAdapterHardCheckMark<>(this,
							R.layout.singlechoice, android.R.id.text1, arrMenu));
					//drawerFragment.etAdditional.setText(((TextView)v.findViewById(R.id.text)).getText());
				}
			} return false;
		}
		return false;
	}

	@Deprecated
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void showChooseTTSDialog() {
		VoicePicker pickTTSDialog = new VoicePicker(this);
		pickTTSDialog.width=(int) (root.getWidth()-getResources().getDimension(R.dimen.diagMarginHor));
		pickTTSDialog.mMaxH=(int) (root.getHeight()-2*getResources().getDimension(R.dimen.diagMarginVer));
		pickTTSDialog.height=-2;
		pickTTSDialog.show(getSupportFragmentManager(), "PickTTSDialog");
	}

	/** 显示带搜索框的词典选择器。
	 * @param reason 发起理由。0：选择当前词典。<br> 1：选择点译上游词典。*/
	@Override
	public void showChooseDictDialog(int reason) {
		dismissing_dh=false;
		boolean needRefresh=pickTarget!=reason;
		pickTarget=reason;
		if(dialogHolder.getVisibility()==View.VISIBLE) {
			dialogHolder.setVisibility(View.GONE);
			checkFlags();
			return;
		}
		if(reason==1){
			int cc=root.getChildCount();
			if(root.getChildAt(cc)!=dialogHolder){
				root.removeView(dialogHolder);
				root.addView(dialogHolder);
			}
		}
		if(!isFragInitiated) {
			needRefresh=false;
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			pickDictDialog = new DictPicker(this);
			transaction.add(R.id.dialog_, pickDictDialog);
			findViewById(R.id.dialog_).setOnClickListener(new Utils.DummyOnClick());
			transaction.commit();
			isFragInitiated=true;
			//pickDictDialog.mRecyclerView.scrollToPosition(adapter_idx);
		}
		else pickDictDialog.refresh(false);
		if(needRefresh) pickDictDialog.adapter().notifyDataSetChanged();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:null;
		boolean isLongClicked=mmi==null?false:mmi.isLongClicked;
		/* 长按事件默认不处理，因此长按时默认返回false，且不关闭menu。 */
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		switch(id){
			case R.id.text_tools:{
				handleTextTools();
			} return true;
			/* 折叠全部 */
			case R.id.toolbar_action0:{
				if(isLongClicked) break;
				toggleFoldAll();
			} break;
			/* 翻页前记忆位置 */
			case R.id.toolbar_action6:{
				if(isLongClicked){
					ActivedAdapter.avoyager.clear();
					showT("已重置页面位置");
					ret = true;
				}else{
					boolean val=ActivedAdapter!=adaptermy2?opt.setRemPos(!opt.getRemPos()):opt.setRemPos2(!opt.getRemPos2());
					iItem_aPageRemember.setTitle(rem_res=(val?R.string.rem_position_yes:R.string.rem_position));
				}
			} break;
			/* 跳转翻阅模式 */
			case R.id.toolbar_action12:{
				ret = closeMenu = true;
				isLongClicked &= ActivedAdapter!=null;
				String nowKey=isLongClicked?null:(ActivedAdapter.currentKeyText());
				boolean proceed=true;
				if(true && !isLongClicked){
					WebViewmy currentWebFocus;
					if(getCurrentFocus() instanceof WebViewmy)
						currentWebFocus = (WebViewmy) getCurrentFocus();
					else{
						currentWebFocus = getCurrentWebContext();
					}
					if(currentWebFocus != null && currentWebFocus.bIsActionMenuShown) {
						proceed = false;
						currentWebFocus.evaluateJavascript("window.getSelection().toString()", value -> {
							String newKey = nowKey;
							if (value.length() > 2) {
								value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
								if (value.length() > 0) {
									newKey = value;
								}
							}
							JumpToPeruseModeWithWord(newKey);
						});
					}
				}
				if(proceed && ActivedAdapter!=null){
					JumpToPeruseModeWithWord(nowKey);
				}
			} break;
			/* 页内查找 */
			case R.id.toolbar_action13:{
				toggleInPageSearch(ret=isLongClicked);
			} break;
			/* 即点即译 */
			case R.id.toolbar_action14:{
				if(isLongClicked){
					popupWord(null, 0, 0, 0);
					closeMenu=ret=true;
				} else {
					boolean val=opt.setClickSearchEnabled(!opt.getClickSearchEnabled());
					if(val) {
						item.setTitle(item.getTitle()+" √");
					}else {
						item.setTitle(item.getTitle().subSequence(0, item.getTitle().length()-2));
					}
					toggleClickSearch(val);
				}
			} break;
			case R.id.toolbar_action7://切换词典
				if(isLongClicked) break;
				findViewById(R.id.browser_widget1).performClick();
			break;
			case R.id.toolbar_action8://切换切换分组
				if(isLongClicked) break;
				findViewById(R.id.browser_widget2).performClick();
			break;
			case R.id.toolbar_action9:{//存书签
				if(isLongClicked) break;
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
			} break;
			case R.id.toolbar_action10:{//保存搜索
				showTopSnack("功能尚未成功");
			} break;
			case R.id.toolbar_action11:{//切换着色
				if(isLongClicked){ ret=false; break;}
				if(TintWildResult.first=opt.toggleTintWildRes()) {
					item.setTitle(item.getTitle()+" √");
				}else {
					item.setTitle(item.getTitle().subSequence(0, item.getTitle().length()-2));
				}
				adaptermy3.notifyDataSetChanged();
			} break;
			case R.id.toolbar_action2:{
				if(isLongClicked) {
					launchSettings(7);
					ret=true;
				}else{
					if (CurrentViewPage == 1) {//viewPager
						tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
						if (!opt.getHistoryStrategy0()) {
							insertUpdate_histroy(etSearch.getText().toString().trim());
						}
					} else
						etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
					mDrawerLayout.closeDrawer(GravityCompat.START);
				}
			} break;
			case R.id.toolbar_action3:{//per-word searching
				if(isLongClicked){ break;}
				CombinedSearchTask_lastKey=null;
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
			} break;
			case R.id.toolbar_action4:{
				if(isLongClicked){
					AttachPeruseView(false);
					ret=true;
				} else {
					if (opt.getInPeruseModeTM()) {
						findViewById(R.id.browser_widget0).setVisibility(View.GONE);
					} else {
						widget0.setVisibility(View.VISIBLE);
						if (opt.getInPeruseMode()) {
							widget0.setImageResource(R.drawable.peruse_ic_on);
							showTopSnack(main_succinct, R.string.peruse_mode
									, 1f, LONG_DURATION_MS, Gravity.CENTER, false);
						}
					}
					opt.setInPeruseModeTM(!opt.getInPeruseModeTM());
				}
			} break;
		}
		if(closeMenu)
			closeIfNoActionView(mmi);
		return ret;
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
		//CMN.Log("onActivityResult");
		switch (requestCode) {
			case 0: {
				isBrowsingImgs = false;
			} break;
			case 110:{
				boolean changed = duco!=null && duco.getBooleanExtra("changed", false);
				if(!changed){
					PlaceHolder phI;
					for (int i = 0; i < CosyChair.size(); i++) {
						phI = CosyChair.get(i);
						int tif = phI.tmpIsFlag;
						boolean b1;
						if((b1=PDICMainAppOptions.getTmpIsFiler(tif)) || PDICMainAppOptions.getTmpIsHidden(tif)){
							CosyChair.remove(i--);
							HdnCmfrt.add(phI);
							if(b1) CosySofa.add(phI);
						}
					}
					if(CosyChair.size()!=md.size() || CosySofa.size()!=currentFilter.size()){
						CMN.Log("重建后大小不匹配", CosyChair.size(), md.size()," or ", CosySofa.size(),currentFilter.size());
						changed = true;
						for (int i = 0; i < HdnCmfrt.size(); i++) {
							phI = HdnCmfrt.get(i);
							CosyChair.add(Math.min(phI.lineNumber, CosyChair.size()), phI);
						}
					}
				}
				if (changed){
					buildUpDictionaryList(lazyLoaded, mdict_cache);
					if (adapter_idx<0) {
						switch_To_Dict_Idx(0, false, false);
					}
					invalidAllLists();
					CMN.Log("变化了", md.size(), currentFilter.size());
				}
//				if(opt.ChangedMap!=null){
//					for (int i = 0; i < md.size(); i++) {
//						mdict mdTmp = md.get(i);
//						if(mdTmp!=null){
//							try {
//								mdTmp.readInConfigs(true);
//							} catch (IOException ignored) { }
//						} else if(i<CosyChair.size()){
//							PlaceHolder phI = CosyChair.get(i);
//							String path = phI.getPath(opt);
//							if(path.endsWith(".web") && opt.ChangedMap.containsKey(path))
//							try {
//								md.set(i, mdTmp=new_mdict(phI.getPath(opt), this));
//								mdTmp.tmpIsFlag = phI.tmpIsFlag;
//							} catch (Exception ignored) { }
//						}
//					}
//					opt.ChangedMap = null;
//				}
				if (duco!=null && duco.getBooleanExtra("result2", false)) {
					opt.putFirstFlag();
					CMN.Log("保存页码");
				}
				//todo 延时清除
				//mdict_cache.clear();
			} break;
			case 123:{
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
			} break;
			case 700:{
				Uri uri = duco.getData();
				getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				//CMN.show(duco.getIntExtra("asd", -1)+"");
				Log.e("uri", uri.toString());
				//DocumentFile encryptMyFolderf = DocumentFile.fromTreeUri(this, uri);
				//encryptMyFolderf.createDirectory("asd");
				//CMN.show(encryptMyFolderf.exists()+"");
				//DocumentFile.fromFile(new File("/storage/0DE6-2108/123.txt")).createFile("", null);
			} break;
			case 111:
				if (duco != null) {
					if (duco.getBooleanExtra("DC", false))
						drawerFragment.myAdapter.notifyDataSetChangedX();
				}
			break;
			case 1297:
				checkColors();
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
		CombinedSearchTask_lastKey=null;
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
		try {
			return prepareHistroyCon().wipeData();
		} catch (Exception ignored) { }
		return false;
	}

	View mView;
	int acc=0;
	float lastX = -1;
	float lastY = -1;
	int last1,last2;
	int deltaY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
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
//		if(Build.VERSION.SDK_INT<23) {//defalut float context menu comes from android Marshmallow
//			int share_id=WebViewmy.getReflactField("com.android.internal.R$string", "share");
//			if(share_id!=-1)
//				Toast.makeText(this, ""+getResources().getString(share_id),Toast.LENGTH_SHORT).show();
//
//		}
		super.onActionModeStarted(mode);
	}

	@Override
	public void switch_To_Dict_Idx(int i, boolean invalidate, boolean putName){
		if(invalidate) checkDictionaryProject(false);
		super.switch_To_Dict_Idx(i, invalidate, putName);
		if(invalidate) {
			if (opt.getPicDictAutoSer()) {
				//CMN.Log("changing text!......");
				tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
			}
			if ((!opt.getPicDictAutoSer() || isCombinedSearching) && currentDictionary != null) {
				lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
			}
			if (!opt.getPinPicDictDialog())
				dismissDictPicker(R.anim.dp_dialog_exit);
		}
	}

	void dismissDictPicker(int animationRes) {
		if(dismissing_dh) return;
		dismissing_dh=true;
		if(pickDictDialog!=null) {
			if(pickDictDialog.isDirty)  {opt.putFirstFlag();pickDictDialog.isDirty=false;}
			dialogHolder.clearAnimation();
			/*  词典选择器的动画效果(消失)  */
			if(animaExit==null) {
				animaExit = AnimationUtils.loadAnimation(this, animationRes);
				animaExit.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						dialogHolder.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}
				});
			}
			dialogHolder.startAnimation(animaExit);
		}
	}

	@Override
	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
		//Context menu
		CMN.Log("onCreateContextMenu", getCurrentFocus());
	}


	ViewGroup getContentviewSnackHolder() {
		return snack_holder;
	}

	protected void switch_dark_mode(boolean val) {
		drawerFragment.sw4.setChecked(val);
	}

	@Override
	protected void notifyFlagChanged() {
		boolean val;
		super.notifyFlagChanged();
		if((val=opt.getClickSearchEnabled())!=opt.getClickSearchEnabled(TFStamp)){
			String title = "即点即译";
			if(val) title+=" √";
			toolbar.getMenu().findItem(R.id.toolbar_action14).setTitle(title);
		}
	}
}
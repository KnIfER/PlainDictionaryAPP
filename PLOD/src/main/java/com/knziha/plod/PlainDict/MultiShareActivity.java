package com.knziha.plod.plaindict;

import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_INPAGE_SEARCH;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.graphics.ColorUtils;

import com.knziha.plod.PlainUI.FloatBtn;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.PageSlide;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/** 主程序之影。复用词典实例。 */
public class MultiShareActivity extends MainActivityUIBase {
	static boolean receivable = false;
	public boolean NewIntentCalled;
	private boolean startLis;
	public boolean supressNxtPauseLis;
	
	@Override
	public void onBackPressed() {
		if(!PerFormBackPrevention(lastBackBtnAct)) {
			finishOrHide();
		}
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if(allHidden() && (peruseView ==null|| peruseView.isWindowDetached())) {
			finishOrHide();
		}
	}
	
	@Override
	public void populateDictionaryList() {
		super.populateDictionaryList();
		findFurtherViews();
	}
	
	@Override
	protected boolean PerFormBackPrevention(boolean bBackBtn) {
		if (super.PerFormBackPrevention(bBackBtn)) {
			return true;
		}
		if(contentview!=null && contentview.getParent()!=null)
		{
			DetachContentView(true);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CMN.debug("MultiShare::onCreate");
		receivable=true;
		initializeTm=CMN.now();
		thisActType = ActType.MultiShare;
		dictPicker = new DictPicker(this, null, null, 1);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_share);
		further_loading(savedInstanceState);
		
		main = root = findViewById(R.id.root);
		mainframe = main;
		mainF = main.findViewById(R.id.mainF);
		
		hdl  = new MyHandler(this);
		mActionModeHeight = dm.heightPixels/2;
		MainBackground = MainAppBackground = opt.getMainBackground();
		AllMenusStamp = new ArrayList<>();
		processIntent(getIntent());
		systemIntialized=true;
	}
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// Checks the orientation of the screen
		GlobalOptions.density = dm.density;
		if(settingsPanel!=null)
			root.postDelayed(postOnConfigurationChanged, 200);
	}
	
	
	@Override
	protected void findFurtherViews() {
		if (contentUIData==null) {
			AllMenus = new MenuBuilder(this);
			// todo……
			super.findFurtherViews();
			adaptermy = new BasicAdapter(contentUIData, weblistHandler, null, null) {
				@Override
				public int getId() {
					return -1;
				}
				@Override
				public String currentKeyText() {
					return "";
				}
			};
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initializeTm=CMN.now();
		processIntent(intent);
	}
	
	private void processIntent(Intent intent) {
		//CMN.Log(intent.getExtras());
		NewIntentCalled = true;
		String text = null;
		//if(intent != null)
		text = intent.getStringExtra(Intent.EXTRA_TEXT);
		if(text!=null) {
			extraText = text;
			// new Text!
		} else if(extraText==null) {
			extraText = "happy";
		}
		ucc = getVtk();
		ucc.setInvoker(null, null, null, extraText);
		int VSGO=opt.getRememberVSPanelGo()?opt.getLastVSGoNumber():-1;
		//VSGO=-1;
		if(VSGO>=0) {
			supressNxtPauseLis = true;
			ucc.onItemClick(null, null, 0, VSGO, false, false);
		} else {
			ucc.onClick(null);
		}
		if(extraText !=null)
		{
			// 动画！
			try {
				TextView tv = ucc.d.findViewById(R.id.alertTitle);
				tv.setText(extraText ==null?"文本操作": extraText);
			} catch (Exception ignored) {  }
		}
	}
	
	void HandleLocateTextInPage(String content) {
		if(!PeruseViewAttached()/* && !Pop*/) {
			startActivity(new Intent(Intent.ACTION_MAIN)
					.setClass(this, MainShareActivity.class)
					.putExtra("force", PLAIN_TARGET_INPAGE_SEARCH)
					.putExtra(Intent.EXTRA_TEXT, extraText)
			);
		} else {
			super.HandleLocateTextInPage(content);
		}
	}
	
	public void OnPeruseDetached() {
		//CMN.debug("OnPeruseDetached::", allHidden(), (ucc==null||ucc.detached()), PeruseViewAttached());
		//CMN.debug("OnPeruseDetached::", NewIntentCalled, getPinVSDialog());
		if(NewIntentCalled && !getPinVSDialog()) {
			hide();
		} else {
			if(allHidden() && (ucc==null||ucc.detached())) {
				if(NewIntentCalled && !getPinVSDialog()) {
					finishOrHide();
				} else {
					getVtk().setInvoker(null, null, null, extraText);
					getVtk().onClick(null);
				}
			}
		}
	}
	
	@Override
	public boolean DetachClickTranslator() {
		boolean ret = super.DetachClickTranslator();
		if(ret) {
			RestoreUccOrExit(0);
		}
		return ret;
	}
	
	public void RestoreUccOrExit(int force) {
		if(allHidden()
				&& (ucc==null||ucc.detached())
				&& (peruseView ==null|| peruseView.isWindowDetached())) {
			if(NewIntentCalled && !getPinVSDialog()) {
				if(force==0) {
					finishOrHide();
					CMN.debug("hide!!!");
				}
			} else {
				showUcc();
			}
		}
	}
	
	private void showUcc() {
		//CMN.Log("showUcc");
		getVtk().setInvoker(null, null, null, extraText);
		getVtk().onClick(null);
	}
	
	public void finishOrHide() {
		hide();
		//finish();
	}
	
	public void hide() {
		if (!moveTaskToBack(false)) {
			showT("关闭不了么？");
		}
	}
	
	public boolean allHidden() {
		if (PeruseViewAttached()/* && PeruseView.mDialog.isShowing()*/
			|| wordPopup.isVisible() || settingsPanel!=null) {
			return false;
		}
		int cc = root.getChildCount();
		View cI;
		for (int i = 0; i < cc; i++) {
			cI = root.getChildAt(i);
			if(cI!=null && cI.getVisibility()==View.VISIBLE) {
				if(!(cI==mainF&&mainF.getChildCount()==0)) {
					//CMN.Log(cI);
					return false;
				}
			}
		}
		return true;
	}
	
	public void guaranteeBackground(int globalPageBackground) {}
	
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
			MultiShareActivity a = ((MultiShareActivity)activity.get());
			switch (msg.what) {
				case 2020:
					if(msg.obj instanceof String)
						a.showT((String)msg.obj, Toast.LENGTH_LONG);
					break;
				case 3322123:
					a.performReadEntry();
					break;
				case 3322124:
					a.enqueueNextAutoReadProcess();
					break;
				case 332211123:
				case 332211:
					removeMessages(332211);
					a.performAutoReadProcess();
					break;
				case 331122:
					animator+=animatorD;
					if(animator>=1) {
						//a.refreshUIColors();
					}
					else {
						int filteredColor = a.AppWhite== Color.WHITE? ColorUtils.blendARGB(Color.BLACK,a.MainBackground, animator):ColorUtils.blendARGB(a.MainBackground, Color.BLACK, animator);
						int filteredWhite = ColorUtils.blendARGB(a.AppBlack, a.AppWhite, animator);
						
						a.bottombar.setBackgroundColor(filteredColor);
						a.toolbar.setBackgroundColor(filteredColor);
						//a.viewPager.setBackgroundColor(filteredWhite);
						a.lv2.setBackgroundColor(filteredWhite);
						a.hdl.sendEmptyMessage(331122);
					}
					break;
				case 778899:
					//a.NaugtyWeb.setLayoutParams(a.NaugtyWeb.getLayoutParams());
					a.NaugtyWeb.requestLayout();
					CMN.debug("handler scroll scale recalibrating ...");
					break;
				case 7658941:
					CustomViewHideTime=0;
					break;
				case 7658942:
					a.fixVideoFullScreen();
					break;
				case 3344:
					((PageSlide)msg.obj).handleMsg(msg);
					break;
			}
		}}
	
	public int getVisibleHeight() {
		return root.getHeight();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		startLis = true;
		CMN.debug("MultiShare::onPause");
		NewIntentCalled = false;
		checkFlags();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//CMN.debug("onResume", "NewIntentCalled="+NewIntentCalled, systemIntialized&&startLis);
		//CMN.debug("onResume", "allHidden="+allHidden() , (ucc==null||ucc.detached()),  (peruseView ==null|| peruseView.isWindowDetached()));
		if(!NewIntentCalled && systemIntialized && startLis) {
			//RestoreUccOrExit(1);
			if(allHidden() && (ucc==null||ucc.detached()) && (peruseView ==null|| peruseView.isWindowDetached())) {
				showUcc();
			}
		} else {
			//finishOrHide();
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus) {
			supressNxtPauseLis = false;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		NewIntentCalled = false;
		CMN.debug("MultiShare::onStop");
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		CMN.debug("MultiShare::onRestart");
	}
	
	@Override
	protected void onDestroy() {
		CMN.debug("MultiShare::onDestroy");
		receivable=false;
		super.onDestroy();
	}
	
	@Override
	public void fix_full_screen(@Nullable View decorView) {
	
	}
	
	@Override
	public void animateUIColorChanges() {
	
	}
	
	@Override
	public void invalidAllLists() {
	
	}
	
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(click_handled_not) {
			onIdClick(v, v.getId());
		}
	}
	
	@SuppressLint("SourceLockedOrientationActivity")
	public void onIdClick(View v, int id){
		layoutScrollDisabled=false;
		fadeSnack();
		OUT:
		if(DBrowser!=null) {
			switch(id) {
				case R.id.browser_widget8:
					DBrowser.toggleFavor();
				break;
				case R.id.browser_widget10:
					if(ActivedAdapter instanceof PeruseView.LeftViewAdapter) break OUT;
					DBrowser.NavList(-1);
				break;
				case R.id.browser_widget11:
					if(ActivedAdapter instanceof PeruseView.LeftViewAdapter) break OUT;
					DBrowser.NavList(1);
				break;
			}
			return;
		}
		
		CheckableImageView cb;
		switch(id) {
			case R.id.popupBackground:
				RestoreUccOrExit(0);
			break;
		}
	}
	
	@Override
	public void startActivity(Intent intent) {
		intent.putExtra(FloatBtn.EXTRA_Initialize, initializeTm);
		super.startActivity(intent);
	}
}
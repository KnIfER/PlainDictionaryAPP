package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.knziha.plod.plaindict.PDICMainActivity.CosyChair;
import static com.knziha.plod.plaindict.PDICMainActivity.CosySofa;
import static com.knziha.plod.plaindict.PDICMainActivity.HdnCmfrt;
import static com.knziha.plod.plaindict.PDICMainActivity.currMdlTime;
import static com.knziha.plod.plaindict.PDICMainActivity.lastLoadedModule;
import static com.knziha.plod.plaindict.PDICMainActivity.lazyLoaded;
import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_INPAGE_SEARCH;

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
		if(allHidden() && (PeruseView==null||PeruseView.isWindowDetached())) {
			finishOrHide();
		}
	}
	
	@Override
	protected void populateDictionaryList() {
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
		CMN.Log("onCreate...");
		receivable=true;
		thisActType = ActType.MultiShareActivity;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_share);
		further_loading(savedInstanceState);
		
		main = root = findViewById(R.id.root);
		main_succinct = main;
		mainF = main.findViewById(R.id.mainF);
		main_progress_bar = main.findViewById(R.id.main_progress_bar);
		
		hdl  = new MyHandler(this);
		mActionModeHeight = dm.heightPixels/2;
		CMN.MainBackground = MainBackground = MainAppBackground = opt.getMainBackground();
		processIntent(getIntent());
		systemIntialized=true;
	}
	
	
	@Override
	protected void findFurtherViews() {
		if (contentUIData==null) {
			contentUIData = ContentviewBinding.inflate(getLayoutInflater());
			super.findFurtherViews();
			adaptermy = new BasicAdapter() {
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
		processIntent(intent);
	}
	
	private void processIntent(Intent intent) {
		//CMN.Log(intent.getExtras());
		NewIntentCalled = true;
		String text = null;
		//if(intent != null)
		text = intent.getStringExtra(Intent.EXTRA_TEXT);
		if(text!=null) {
			debugString = text;
			// new Text!
		} else if(debugString==null) {
			debugString = StringUtils.EMPTY;
		}
		ucc = getUcc();
		ucc.setInvoker(null, null, null, debugString);
		int VSGO=opt.getRememberVSPanelGo()?opt.getLastVSGoNumber():-1;
		//VSGO=-1;
		if(VSGO>=0) {
			supressNxtPauseLis = true;
			ucc.onItemClick(null, null, 0, VSGO, false, false);
		} else {
			ucc.onClick(null);
		}
		if(debugString!=null)
		{
			// 动画！
			try {
				TextView tv = ucc.d.findViewById(R.id.alertTitle);
				tv.setText(debugString==null?"文本操作":debugString);
			} catch (Exception ignored) {  }
		}
	}
	
	void HandleLocateTextInPage(String content) {
		if(!PeruseViewAttached()/* && !Pop*/) {
			startActivity(new Intent(Intent.ACTION_MAIN)
					.setClass(this, MainShareActivity.class)
					.putExtra("force", PLAIN_TARGET_INPAGE_SEARCH)
					.putExtra(Intent.EXTRA_TEXT, debugString)
			);
		} else {
			super.HandleLocateTextInPage(content);
		}
	}
	
	@Override
	public void OnPeruseDetached() {
		if(NewIntentCalled && getPinVSDialog()) {
			hide();
		} else {
			if(allHidden() && (ucc==null||ucc.detached())) {
				if(NewIntentCalled && getPinVSDialog()) {
					finishOrHide();
				} else {
					getUcc().setInvoker(null, null, null, debugString);
					getUcc().onClick(null);
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
				&& (PeruseView==null||PeruseView.isWindowDetached())) {
			if(NewIntentCalled && !getPinVSDialog()) {
				if(force==0) {
					finishOrHide();
					CMN.Log("hide!!!");
				}
			} else {
				showUcc();
			}
		}
	}
	
	private void showUcc() {
		CMN.Log("showUcc");
		getUcc().setInvoker(null, null, null, debugString);
		getUcc().onClick(null);
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
					//CMN.Log("handler scroll scale recalibrating ...");
					break;
				case 7658941:
					CustomViewHideTime=0;
					break;
				case 7658942:
					a.fixVideoFullScreen();
					break;
			}
		}}
	
	protected int getVisibleHeight() {
		return root.getHeight();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		startLis = true;
		CMN.Log("MultiShare::onPause");
		NewIntentCalled = false;
		checkFlags();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		CMN.Log("onResume", "NewIntentCalled="+NewIntentCalled, systemIntialized&&startLis);
		CMN.Log("onResume", "allHidden="+allHidden() , (ucc==null||ucc.detached()),  (PeruseView==null||PeruseView.isWindowDetached()));
		if(!NewIntentCalled && systemIntialized && startLis) {
			//RestoreUccOrExit(1);
			if(allHidden() && (ucc==null||ucc.detached()) && (PeruseView==null||PeruseView.isWindowDetached())) {
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
		CMN.Log("MultiShare::onStop");
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		CMN.Log("MultiShare::onRestart");
	}
	
	@Override
	protected void onDestroy() {
		CMN.Log("MultiShare::onDestroy");
		receivable=false;
		super.onDestroy();
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
	
	@Override
	protected View getIMPageCover() {
		return null;
	}
	
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
	
	@Override
	public void AttachContentViewForDB() {
		CMN.Log("AttachContentViewForDB");
		if(DBrowser!=null) {
			Utils.addViewToParent(contentview, PeruseViewAttached()?PeruseView.peruseF:root);
		}
	}
	
	@Override
	public void fix_full_screen(@Nullable View decorView) {
	
	}
	
	@Override
	void switch_dark_mode(boolean val) {
	
	}
	
	@Override
	public void animateUIColorChanges() {
	
	}
	
	@Override
	void DetachContentView(boolean leaving) {
		Utils.removeView(contentview);
	}
	
	@Override
	void showChooseDictDialog(int reason) {
	
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}
	
	@Override
	void contentviewAddView(View v, int i) {
	
	}
	
	@Override
	public void invalidAllLists() {
	
	}
	
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
	public void onClick(View v) {
		super.onClick(v);
		if(click_handled_not) {
			onIdClick(v, v.getId());
		}
	}
	
	@SuppressLint("SourceLockedOrientationActivity")
	public void onIdClick(View v, int id){
		layoutScrollDisabled=false;
		cancleSnack();
		OUT:
		if(DBrowser!=null) {
			switch(id) {
				case R.id.browser_widget8:
					DBrowser.toggleFavor();
				break;
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
			case R.id.popupBackground:
				RestoreUccOrExit(0);
			break;
		}
	}
}
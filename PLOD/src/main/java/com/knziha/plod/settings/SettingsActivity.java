package com.knziha.plod.settings;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.GlobalOptions;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.knziha.filepicker.settings.FileChooser;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.plod.PlainUI.DBUpgradeHelper;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CrashHandler;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.APPSettingsActivity;

import java.io.File;

public class SettingsActivity extends Toastable_Activity implements APPSettingsActivity {
	private int realm_id;
	private MainActivityUIBase pHandler;
	
	public PreferenceFragmentCompat fragment;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				checkBack();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void checkBack() {
		if(realm_id==FileChooser.id){
			PDICMainAppOptions.SecondFlag(FilePickerOptions.SecondFlag);
		}
		else if(realm_id==SearchSpecification.id){
			if(opt.CetUseRegex3(SFStamp)
					|opt.CetPageCaseSensitive(SFStamp)
					|opt.CetPageWildcardMatchNoSpace(SFStamp)
					|opt.CetPageWildcardSplitKeywords(SFStamp)
					|opt.CetInPageSearchUseWildcard(TFStamp)
			)
			CMN.setCheckRcsp();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window win = getWindow();
		if(opt.isFullScreen()){
			win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		win.getDecorView().setBackgroundColor(GlobalOptions.isDark? Color.BLACK:Color.WHITE);
		root=win.getDecorView().findViewById(android.R.id.content);
		checkMargin(this);
		if(Build.VERSION.SDK_INT>=21) {
			win.setStatusBarColor(CMN.MainBackground);
			win.setNavigationBarColor(CMN.MainBackground);
		}

		File log=new File(CrashHandler.getInstance(this, opt).getLogFile());
		File lock=new File(log.getParentFile(),"lock");
		if(lock.exists()) lock.delete();

		Bundle args = new Bundle();
		switch (realm_id = getIntent().getIntExtra("realm", 0)){
			default:
			case 0:
				fragment = new MainProgram();
			break;
			case FileChooser.id:
				fragment = new FileChooser();
			break;
			case DevoloperOptions.id:
				fragment = new DevoloperOptions();
			break;
			case Licences.id:
				fragment = new Licences();
			break;
			case SearchSpecification.id:
				fragment = new SearchSpecification();
			break;
			case ViewSpecification.id:
				fragment = new ViewSpecification();
				args.putInt("title", R.string.view_spec);
			break;
			case ClickSearch.id:
				fragment = new ClickSearch();
			break;
			case HistoryPreference.id:
				fragment = new HistoryPreference();
			break;
			case ServerPreference.id:
				fragment = new ServerPreference();
				args.putInt("title", R.string.server_spec);
			break;
		}
		fragment.setArguments(args);
		this.getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, fragment)
				.commit();
		
		pHandler = CMN.pHandler==null?null:CMN.pHandler.get();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		checkFlags();
	}
	
	@Override
	public void notifyChanged(Preference preference) {
		if (pHandler!=null) {
			pHandler.onSettingsChanged(this, preference);
		} else {
			showT("请从主程序打开设置界面！");
		}
	}
}
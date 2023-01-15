package com.knziha.plod.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.knziha.filepicker.settings.FileChooser;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CrashHandler;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.APPSettingsActivity;
import com.knziha.plod.widgets.ViewUtils;

import java.io.File;

public class SettingsActivity extends Toastable_Activity implements APPSettingsActivity {
	private int realm_id;
	private MainActivityUIBase pHandler;
	
	public PreferenceFragmentCompat fragment;
	
	public static void launch(Context context, int fragmentId) {
		Intent intent = new Intent();
		intent.putExtra("realm", fragmentId);
		intent.setClass(context, SettingsActivity.class);
		context.startActivity(intent);
	}
	
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
//		else if(realm_id== SchOpt.id){
//			if(opt.CetUseRegex3(SFStamp)
//					|opt.CetPageCaseSensitive(SFStamp)
//					|opt.CetPageWildcardMatchNoSpace(SFStamp)
//					|opt.CetPageWildcardSplitKeywords(SFStamp)
//					|opt.CetInPageSearchUseWildcard(TFStamp)
//			)
			CMN.setCheckRcsp();
//		}
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
		win.getDecorView().setBackgroundColor(GlobalOptions.isDark?
				ViewUtils.littleCat ?0xFF333333:Color.BLACK
				:0xFFf5f5f5);
		root=win.getDecorView().findViewById(android.R.id.content);
		checkMargin(this);
		MainBackground = opt.getMainBackground();
		MainLumen = GlobalOptions.isDark?0:ColorUtils.calculateLuminance(MainBackground);
		if(Build.VERSION.SDK_INT>=21) {
			win.setStatusBarColor(MainBackground);
			win.setNavigationBarColor(MainBackground);
		}
		resetStatusForeground();

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
			case DevOpt.id:
				fragment = new DevOpt();
			break;
			case Licences.id:
				fragment = new Licences();
			break;
			case SchOpt.id:
				fragment = new SchOpt();
			break;
			case Misc.id:
				fragment = new Misc();
				args.putInt("title", R.string.view_spec);
			break;
			case Misc_exit_dialog.id:
				fragment = new Misc_exit_dialog();
				args.putInt("title", R.string.view_spec);
			break;
			case TapTranslator.id:
				fragment = new TapTranslator();
			break;
			case History.id:
				fragment = new History();
			break;
			case ServerPreference.id:
				fragment = new ServerPreference();
				args.putInt("title", R.string.server_spec);
			break;
			case NotificationSettings.id:
				fragment = new NotificationSettings();
				args.putInt("title", R.string.noti_set);
			break;
			case Multiview.id:
				fragment = new Multiview();
				//args.putString("title", "多页面设置");
			break;
//			case BookOptions.id:
//				fragment = new BookOptions();
//				//args.putString("title", "多页面设置");
//			break;
			case NightMode.id:
				fragment = new NightMode();
			break;
			case MoreColors.id:
				fragment = new MoreColors();
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
		try {
			pHandler.onSettingsChanged(this, preference);
		} catch (Exception e) {
			showT("请从主程序打开设置界面！");
		}
	}
	
	public void showSearchSettingsDlg() {
		try {
			pHandler.showSearchSettingsDlg(this, realm_id);
		} catch (Exception e) {
			showT("请从主程序打开设置界面！");
		}
	}
}
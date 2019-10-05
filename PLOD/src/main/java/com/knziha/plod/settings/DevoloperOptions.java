package com.knziha.plod.settings;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;

import java.io.DataOutputStream;

/** Devoloper Options */
public class DevoloperOptions extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	//执行初始化操作
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findPreference("app_settings").setOnPreferenceClickListener(this);
		findPreference("system_dev").setOnPreferenceClickListener(this);
		init_switch_preference(this, "root", PDICMainAppOptions.getRoot(), null, null);
	}

	/** 应用程序运行命令获取 Root权限 */
	public static boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); //切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()) {
			case "root": {
				if((boolean)newValue){
					if(!upgradeRootPermission(getContext().getPackageCodePath()))
						return false;
				}
				PDICMainAppOptions.setRoot((boolean)newValue);
			}
			return true;
		}
		return super.onPreferenceChange(preference, newValue);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		switch (preference.getKey()){
			case "app_settings":{
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
			break;
			case "system_dev":{
				try {
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
					startActivity(intent);
				}
				catch (Exception e) {
					try {
						ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings");
						Intent intent = new Intent();
						intent.setComponent(componentName);
						intent.setAction("android.intent.action.View");
						startActivity(intent);
					} catch (Exception e1) {
						try {
							Intent intent = new Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS");//部分小米手机采用这种方式跳转
							startActivity(intent);
						} catch (Exception ignored) { }
					}
				}
			}
			break;
		}
		return false;
	}

	//加载
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.devpreferences);
	}
}
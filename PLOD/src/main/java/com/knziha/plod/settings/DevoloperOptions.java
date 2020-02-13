package com.knziha.plod.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;

import java.io.DataOutputStream;

/** Devoloper Options */
public class DevoloperOptions extends SettingsFragment implements Preference.OnPreferenceClickListener {
	private WebView mWebview;

	//执行初始化操作
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findPreference("app_settings").setOnPreferenceClickListener(this);
		findPreference("system_dev").setOnPreferenceClickListener(this);
		findPreference("clear_cache1").setOnPreferenceClickListener(this);
		findPreference("clear_cache2").setOnPreferenceClickListener(this);
		findPreference("clear_cache3").setOnPreferenceClickListener(this);
		findPreference("clear_cache4").setOnPreferenceClickListener(this);
		init_switch_preference(this, "root", PDICMainAppOptions.getRoot(), null, null).setVisible(false);
		init_switch_preference(this, "lazyLoad", PDICMainAppOptions.getLazyLoadDicts(), null, null);
		init_switch_preference(this, "keep_hide", PDICMainAppOptions.getAllowHiddenRecords(), null, null);
		init_switch_preference(this, "sounds_first", PDICMainAppOptions.getUseSoundsPlaybackFirst(), null, null);
		init_switch_preference(this, "enable_web_debug", PDICMainAppOptions.getEnableWebDebug(), null, null);
		init_switch_preference(this, "cache_mp3", PDICMainAppOptions.getCacheSoundResInAdvance(), null, null);
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
			case "lazyLoad": {
				PDICMainAppOptions.setLazyLoadDicts((boolean)newValue);
			}
			return true;
			case "keep_hide": {
				PDICMainAppOptions.setAllowHiddenRecords((boolean)newValue);
			}
			return true;
			case "sounds_first": {
				PDICMainAppOptions.setUseSoundsPlaybackFirst((boolean)newValue);
			}
			return true;
			case "enable_web_debug": {
				PDICMainAppOptions.setEnableWebDebug((boolean)newValue);
			}
			return true;
			case "cache_mp3": {
				PDICMainAppOptions.setCacheSoundResInAdvance((boolean)newValue);
			}
			return true;
		}
		return super.onPreferenceChange(preference, newValue);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference instanceof WarnPreference){
			Context context = getContext();
			AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
			builder2.setTitle("确认清理？")
					.setPositiveButton(com.knziha.filepicker.R.string.delete, (dialog, which) -> {
						switch (preference.getKey()){
							case "clear_cache1": {
								if(mWebview==null) mWebview = new WebView(getContext());
								mWebview.clearCache(false);
							}
							break;
							case "clear_cache2": {
								if(mWebview==null) mWebview = new WebView(getContext());
								mWebview.clearCache(true);
								CookieSyncManager.createInstance(getContext());  //Create a singleton CookieSyncManager within a context
								CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
								cookieManager.removeAllCookie();// Removes all cookies.
								CookieSyncManager.getInstance().sync(); // forces sync manager to sync now
							}
							break;
							case "clear_cache3": {
								if(mWebview==null) mWebview = new WebView(getContext());
								mWebview.clearFormData();
							}
							break;
							case "clear_cache4": {
								if(mWebview==null) mWebview = new WebView(getContext());
								mWebview.clearSslPreferences();
								if(Build.VERSION.SDK_INT>=21)
									WebView.clearClientCertPreferences(null);
							}
							break;
						}
						Toast.makeText(getContext(), "已清理", Toast.LENGTH_LONG).show();
					});
			AlertDialog dTmp = builder2.create();
			dTmp.show();
			((TextView)dTmp.findViewById(com.knziha.filepicker.R.id.alertTitle)).setSingleLine(false);
		}
		else switch (preference.getKey()){
			case "app_settings":{
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//todo remove below 2 lines and crash, why?
				Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
			break;
			case "system_dev":{
				try {
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
				catch (Exception e) {
					try {
						ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings");
						Intent intent = new Intent();
						intent.setComponent(componentName);
						intent.setAction("android.intent.action.View");
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
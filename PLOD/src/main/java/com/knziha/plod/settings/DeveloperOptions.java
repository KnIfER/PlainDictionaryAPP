package com.knziha.plod.settings;

import android.app.Activity;
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

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.AU;
import com.knziha.plod.plaindict.CrashHandler;
import com.knziha.plod.plaindict.FcfrtAppBhUtils;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.ServiceEnhancer;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.ViewUtils;

import java.io.DataOutputStream;
import java.util.HashMap;

/** Devoloper Options */
public class DeveloperOptions extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id=4;
	private WebView mWebview;
	
	private String localeStamp;
	private HashMap<String, String> nym;
	StringBuilder flag_code= new StringBuilder();

	//执行初始化操作
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.pref_dev;
		nym=new HashMap<>(15);
		nym.put("ar", "ae");
		nym.put("zh", "cn");
		nym.put("ja", "jp");
		nym.put("ca", "");
		nym.put("gl", "");
		nym.put("el", "gr");
		nym.put("ko", "kr");
		nym.put("en", "gb\t\tus");
		nym.put("cs", "cz");
		nym.put("da", "dk");
		nym.put("sv", "se");
		nym.put("sl", "si");
		nym.put("nb", "no");
		nym.put("sr", "rs");
		nym.put("uk", "ua");
		
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "locale", null, getNameFlag(localeStamp = PDICMainAppOptions.locale), null);
		
		findPreference("app_settings").setOnPreferenceClickListener(this);
		findPreference("system_dev").setOnPreferenceClickListener(this);
		findPreference("battery").setOnPreferenceClickListener(this);
		findPreference("clear_cache1").setOnPreferenceClickListener(this);
		findPreference("clear_cache2").setOnPreferenceClickListener(this);
		findPreference("clear_cache3").setOnPreferenceClickListener(this);
		findPreference("clear_cache4").setOnPreferenceClickListener(this);
		findPreference("log").setOnPreferenceClickListener(this);
		findPreference("log2").setOnPreferenceClickListener(this);
		init_switch_preference(this, "root", PDICMainAppOptions.getRoot(), null, null).setVisible(false);
		init_switch_preference(this, "lazyLoad", PDICMainAppOptions.getLazyLoadDicts(), null, null);
		init_switch_preference(this, "classical_sort", PDICMainAppOptions.getClassicalKeycaseStrategy(), null, null);
		init_switch_preference(this, "keep_hide", PDICMainAppOptions.getAllowHiddenRecords(), null, null);
		init_switch_preference(this, "sounds_first", PDICMainAppOptions.getUseSoundsPlaybackFirst(), null, null);
		init_switch_preference(this, "enable_web_debug", PDICMainAppOptions.getEnableWebDebug(), null, null);
		init_switch_preference(this, "tts_reader", PDICMainAppOptions.getUseTTSToReadEntry(), null, null);
		init_switch_preference(this, "cache_mp3", PDICMainAppOptions.getCacheSoundResInAdvance(), null, null);
		init_switch_preference(this, "notify", PDICMainAppOptions.getNotificationEnabled(), null, null);
		
		init_switch_preference(this, "plugCss", PDICMainAppOptions.getAllowPlugCss(), null, null);
		init_switch_preference(this, "plugRes", PDICMainAppOptions.getAllowPlugRes(), null, null);
		init_switch_preference(this, "plugResNone", PDICMainAppOptions.getAllowPlugResNone(), null, null);
		init_switch_preference(this, "plugResSame", PDICMainAppOptions.getAllowPlugResSame(), null, null);
		
		init_switch_preference(this, "dbv2", PDICMainAppOptions.getUseDatabaseV2(), null, null);
		findPreference("dbv2_up").setOnPreferenceClickListener(this);
	}
	
	private String getNameFlag(String andoid_country_code) {
		if(andoid_country_code==null || andoid_country_code.length()==0)
			return null;
		String name=andoid_country_code;
		int idx;
		if((idx = name.indexOf("-")) != -1.)
			name=name.substring(0, idx);
		else
			andoid_country_code=andoid_country_code.toUpperCase();
		name=name.toLowerCase();
		if(nym.containsKey(name))
			name=nym.get(name);
		if(flag_code==null)
			flag_code= new StringBuilder();
		flag_code.setLength(0);
		flag_code.append(andoid_country_code).append("\t\t\t\t");
		return getCountryFlag(flag_code, name).toString();
	}
	
	public static StringBuilder getCountryFlag(StringBuilder flag_code, String name) {
		for (int i = 0; i < name.length(); i++) {
			char cI = name.charAt(i);
			if(cI>=0x61 && cI<=0x61+26){
				flag_code.append("\uD83C").append((char) (0xDDE6 + cI - 0x61));
			}else
				flag_code.append(cI);
		}
		return flag_code;
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
				WebView.setWebContentsDebuggingEnabled((boolean)newValue);
			}
			return true;
			case "tts_reader": {
				PDICMainAppOptions.setUseTTSToReadEntry((boolean)newValue);
			}
			return true;
			case "cache_mp3": {
				PDICMainAppOptions.setCacheSoundResInAdvance((boolean)newValue);
			}
			return true;
			case "notify": {
				Context context = getContext();
				if(context==null) return false;
				if(preference.getLeftPartClicked()) {
					SettingsActivity.launch(context, NotificationSettings.id);
					return false;
				} else {
					AU.checkNotificationPermissionAndRequire(context);
					PDICMainAppOptions.setNotificationEnabled((boolean)newValue);
					Intent intent = new Intent(context, ServiceEnhancer.class);
					if(PDICMainAppOptions.getNotificationEnabled()) {
						context.startService(intent);
					} else {
						context.stopService(intent);
					}
				}
			}
			return true;
			
			case "plugCss":
				PDICMainAppOptions.setAllowPlugCss((Boolean) newValue);
			return true;
			case "plugRes":
				PDICMainAppOptions.setAllowPlugRes((Boolean) newValue);
			return true;
			case "plugResNone":
				PDICMainAppOptions.setAllowPlugResNone((Boolean) newValue);
			return true;
			case "plugResSame":
				PDICMainAppOptions.setAllowPlugResSame((Boolean) newValue);
			return true;
			
			case "classical_sort":
				PDICMainAppOptions.setClassicalKeycaseStrategy(mdict.bGlobalUseClassicalKeycase=(Boolean) newValue);
			return true;
			case "locale":
				if(localeStamp!=null)
					PDICMainAppOptions.locale=localeStamp.equals(newValue)?localeStamp:null;
				preference.setSummary(getNameFlag((String) newValue));
			return true;
			case "dbv2":
				PDICMainAppOptions.setUseDatabaseV2((Boolean) newValue);
				preference.setSummary("重启生效*");
			return true;
		}
		return super.onPreferenceChange(preference, newValue);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key=preference.getKey();
		Activity context = getActivity();
		if(context==null) return false;
		if(preference instanceof WarnPreference){
			AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
			builder2.setTitle("确认清理？")
					.setPositiveButton(com.knziha.filepicker.R.string.delete, (dialog, which) -> {
						switch (key){
							case "clear_cache1": {
								if(mWebview==null) mWebview = new WebView(context);
								mWebview.clearCache(false);
							}
							break;
							case "clear_cache2": {
								if(mWebview==null) mWebview = new WebView(context);
								mWebview.clearCache(true);
								CookieSyncManager.createInstance(context);  //Create a singleton CookieSyncManager within a context
								CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
								cookieManager.removeAllCookie();// Removes all cookies.
								CookieSyncManager.getInstance().sync(); // forces sync manager to sync now
							}
							break;
							case "clear_cache3": {
								if(mWebview==null) mWebview = new WebView(context);
								mWebview.clearFormData();
							}
							break;
							case "clear_cache4": {
								if(mWebview==null) mWebview = new WebView(context);
								mWebview.clearSslPreferences();
								if(Build.VERSION.SDK_INT>=21)
									WebView.clearClientCertPreferences(null);
							}
							break;
						}
						Toast.makeText(context, "已清理", Toast.LENGTH_LONG).show();
					});
			AlertDialog dTmp = builder2.create();
			dTmp.show();
			((TextView)dTmp.findViewById(com.knziha.filepicker.R.id.alertTitle)).setSingleLine(false);
		}
		else switch (key){
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
			case "battery":{
				FcfrtAppBhUtils.requestIgnoreBatteryOptimizations(context);
			} break;
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
			} break;
			case "dbv2_up": {
				ViewUtils.notifyAPPSettingsChanged(context, preference);
			} break;
			case "log":
			case "log2": {
				CrashHandler.getInstance(context, ((Toastable_Activity)context).opt)
						.showErrorMessage(context, null, key.length()==3?false:true);
			}
			break;
		}
		return false;
	}
}
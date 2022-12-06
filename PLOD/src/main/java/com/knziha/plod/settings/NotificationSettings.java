package com.knziha.plod.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.knziha.plod.plaindict.AU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.FcfrtAppBhUtils;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.ServiceEnhancer;

public class NotificationSettings extends PlainSettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=R.xml.pref_notification;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.pref_notification;
		
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "always", PDICMainAppOptions.getNotificationEnabled(), null, null, null);
		init_switch_preference(this, "server", PDICMainAppOptions.getAutoEnableNotification(), null, null, null);
		init_switch_preference(this, "music", PDICMainAppOptions.getForceDaemonMusic(), null, null, null);
		init_switch_preference(this, "wifi", PDICMainAppOptions.getForceDaemonWifi(), null, null, null);
		init_switch_preference(this, "daemon", PDICMainAppOptions.getAutoDaemonMW(), null, null, null);
		init_switch_preference(this, "exit", PDICMainAppOptions.getAutoClearNotificationOnExit(), null, null, null);
		init_switch_preference(this, "close", PDICMainAppOptions.getShowNotificationExitBtn(), null, null, null);
		init_switch_preference(this, "subtitle", PDICMainAppOptions.getShowNotificationSubtitle(), null, null, null);
		init_switch_preference(this, "options", PDICMainAppOptions.getShowNotificationSettings(), null, null, null);
		//init_switch_preference(this, "swipe", PDICMainAppOptions.getNotificationSwipeble(), null, null);
		
		findPreference("battery").setOnPreferenceClickListener(this);
		findPreference("batUsage").setOnPreferenceClickListener(this);
		findPreference("batSummary").setOnPreferenceClickListener(this);
		findPreference("settings").setOnPreferenceClickListener(this);
		findPreference("finish").setOnPreferenceClickListener(this);
		findPreference("start").setOnPreferenceClickListener(this);
	}
	
	private void setStatusText() {
		//CMN.Log("setStatusText::", ServiceEnhancer.isMusicPlaying);
		findPreference("music").setWiki((ServiceEnhancer.isMusicPlaying&0x1)!=0?"√":"", true);
		findPreference("wifi").setWiki((ServiceEnhancer.isMusicPlaying&0x2)!=0?"√":"", true);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()) {
			case "always": {
				PDICMainAppOptions.setNotificationEnabled((boolean)newValue);
				SendSetUpDaemon();
			} return true;
			case "server": {
				PDICMainAppOptions.setAutoEnableNotification((boolean)newValue);
				SendSetUpDaemon();
			} return true;
			case "music": {
				PDICMainAppOptions.setForceDaemonMusic((boolean)newValue);
				SendSetUpDaemon();
			} return true;
			case "wifi": {
				PDICMainAppOptions.setForceDaemonWifi((boolean)newValue);
				SendSetUpDaemon();
			} return true;
			case "daemon": {
				boolean val=(boolean)newValue;
				PDICMainAppOptions.setAutoDaemonMW(val);
				val=!val;
				findPreference("music").setEnabled(val);
				findPreference("wifi").setEnabled(val);
				SendSetUpDaemon();
			} return true;
			case "exit": {
				PDICMainAppOptions.setAutoClearNotificationOnExit((boolean)newValue);
			} return true;
			case "close": {
				PDICMainAppOptions.setShowNotificationExitBtn((boolean)newValue);
			} return true;
			case "subtitle": {
				PDICMainAppOptions.setShowNotificationSubtitle((boolean)newValue);
			} return true;
			case "options": {
				PDICMainAppOptions.setShowNotificationSettings((boolean)newValue);
			} return true;
//			case "swipe": {
//				PDICMainAppOptions.setNotificationSwipeble((boolean)newValue);
//				ServiceEnhancer.SendSetUpDaemon(getActivity());
//			} return true;
			case "...": {
			
			} return true;
		}
		return super.onPreferenceChange(preference, newValue);
	}
	
	private void SendSetUpDaemon() {
		Intent intent = new Intent(getActivity(), ServiceEnhancer.class);
		intent.putExtra("realm", true);
		getActivity().startService(intent);
		getView().postDelayed(this::setStatusText, 200);
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		switch (preference.getKey()) {
			case "battery":{
				FcfrtAppBhUtils.requestIgnoreBatteryOptimizations(getContext());
			} break;
			case "batUsage": {
				final String[] batteryUsageManagers = {
						"com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"
						, "com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"
						, "com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
						, "com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"
						, "com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
						, "com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"
						, "com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"
						, "com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"
						, "com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
						, "com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
						, "com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
						, "com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity"
						, "com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"
						, "com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity"
						, "com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"
						, "com.transsion.phonemanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity"
				};
				PackageManager packageManager = getActivity().getPackageManager();
				Intent intent = new Intent();
				for (int i = 0; i < batteryUsageManagers.length; i+=2) {
					intent.setClassName(batteryUsageManagers[i], batteryUsageManagers[i+1]);
					if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
						try {
							startActivity(intent);
							break;
						} catch (Exception e) { CMN.debug(e); }
					}
				}
			} return true;
			case "batSummary": {
				Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
				try {
					startActivity(powerUsageIntent);
				} catch (Exception e) { CMN.debug(e); }
			} return true;
			case "settings": {
				try {
					startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
				} catch (Exception e) { CMN.debug(e); }
			} return true;
			case "finish": {
				AU.stopService(getActivity(), ServiceEnhancer.class);
			} return true;
			case "start": {
				AU.stopService(getActivity(), ServiceEnhancer.class);
				SendSetUpDaemon();
			} return true;
			case "...": {
			
			} return true;
		}
		return false;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (PDICMainAppOptions.getAutoDaemonMW()) {
			findPreference("music").setEnabled(false);
			findPreference("wifi").setEnabled(false);
			SendSetUpDaemon();
		} else {
			setStatusText();
		}
	}
}
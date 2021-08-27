package com.knziha.plod.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.preference.Preference;

import com.knziha.filepicker.settings.FilePickerPreference;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import java.io.File;

public class MainProgram extends SettingsFragment implements Preference.OnPreferenceClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "enable_pastebin", PDICMainAppOptions.getShowPasteBin(), null, null);
		init_switch_preference(this, "keep_screen", PDICMainAppOptions.getKeepScreen(), null, null);
		init_switch_preference(this, "classical_sort", PDICMainAppOptions.getClassicalKeycaseStrategy(), null, null);
		init_switch_preference(this, "GPBC", null, "0x"+Integer.toHexString(CMN.GlobalPageBackground).toUpperCase(), null);
		init_switch_preference(this, "BCM", null, "0x"+Integer.toHexString(CMN.MainBackground).toUpperCase(), null);
		init_switch_preference(this, "BCF", null, "0x"+Integer.toHexString(CMN.FloatBackground).toUpperCase(), null);
		//init_number_info_preference(this, "paste_target", PDICMainAppOptions.getPasteTarget(), R.array.paste_target_info, null);
		//init_switch_preference(this, "f_share_peruse", PDICMainAppOptions.getShareToPeruseModeWhenFocued(), null, null);
		init_switch_preference(this, "f_paste_peruse", PDICMainAppOptions.getPasteToPeruseModeWhenFocued(), null, null);
		init_switch_preference(this, "f_move_bg", PDICMainAppOptions.getFloatClickHideToBackground(), null, null);
		init_switch_preference(this, "f_hide_recent", PDICMainAppOptions.getHideFloatFromRecent(), null, null);
		
		init_switch_preference(this, "dbv2", PDICMainAppOptions.getUseDatabaseV2(), null, null);

		findPreference("f_size").setDefaultValue(GlobalOptions.isLarge?150:125);
		
		findPreference("dev").setOnPreferenceClickListener(this);
		findPreference("sspec").setOnPreferenceClickListener(this);
		findPreference("vspec").setOnPreferenceClickListener(this);
		findPreference("dbv2_up").setOnPreferenceClickListener(this);
	}
	
	//创建
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		int fragmentId=-1;
		switch (preference.getKey()){
			case "dev":
				fragmentId=4;
			break;
			case "sspec":
				fragmentId=7;
			break;
			case "vspec":
				fragmentId=8;
			break;
			case "dbv2_up": {
				ViewUtils.notifyAPPSettingsChanged(getActivity(), preference);
			} break;
			
		}
		if(fragmentId!=-1){
			Intent intent = new Intent();
			intent.putExtra("realm", fragmentId);
			intent.setClass(getContext(), SettingsActivity.class);
			startActivity(intent);
		}
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "enable_pastebin":
				PDICMainAppOptions.setShowPasteBin((Boolean) newValue);
			break;
			case "keep_screen":
				PDICMainAppOptions.setKeepScreen((Boolean) newValue);
			break;
			case "classical_sort":
				PDICMainAppOptions.setClassicalKeycaseStrategy(mdict.bGlobalUseClassicalKeycase=(Boolean) newValue);
			break;
			case "GPBC":
				setColorPreferenceTitle(preference, newValue);
				CMN.GlobalPageBackground=(int) newValue;
			break;
			case "BCM":
				setColorPreferenceTitle(preference, newValue);
				CMN.MainBackground=(int) newValue;
			break;
			case "BCF":
				setColorPreferenceTitle(preference, newValue);
				CMN.FloatBackground=(int) newValue;
			break;
//			case "paste_target":
//				preference.setSummary(getResources().getStringArray(R.array.paste_target_info)[PDICMainAppOptions.setPasteTarget(IU.parsint(newValue))]);
//			break;
//			case "share_target":
//				preference.setSummary(getResources().getStringArray(R.array.paste_target_info)[PDICMainAppOptions.setShareTarget(IU.parsint(newValue))]);
//			break;
			case "f_share_peruse":
				PDICMainAppOptions.setShareToPeruseModeWhenFocued((Boolean) newValue);
			break;
			case "f_paste_peruse":
				PDICMainAppOptions.setPasteToPeruseModeWhenFocued((Boolean) newValue);
			break;
			case "f_move_bg":
				PDICMainAppOptions.setFloatClickHideToBackground((Boolean) newValue);
			break;
			case "f_hide_recent":
				PDICMainAppOptions.setHideFloatFromRecent((Boolean) newValue);
			break;
			case "dbv2":
				PDICMainAppOptions.setUseDatabaseV2((Boolean) newValue);
				preference.setSummary("重启生效*");
			break;
		}
		return true;
	}

	static  void setColorPreferenceTitle(Preference preference, Object newValue) {
		//String name = preference.getTitle().toString();
		//preference.setTitle(name.substring(0, name.indexOf(": ")+2)+Integer.toHexString((int) newValue).toUpperCase());
		preference.setSummary("0x"+Integer.toHexString((int) newValue).toUpperCase());
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		PDICMainAppOptions opt =((SettingsActivity)getActivity()).opt;
		FilePickerPreference fpp = findPreference("fntlb");
		fpp.setDefaultValue(opt.getFontLibPath());
		fpp.properties.opt_dir=new File(opt.pathToDatabases().append("favorite_dirs/").toString());
	}
}
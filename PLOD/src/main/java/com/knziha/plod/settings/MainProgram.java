package com.knziha.plod.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.preference.Preference;

import com.knziha.filepicker.settings.FilePickerPreference;
import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;

import java.io.File;
import java.util.Objects;

public class MainProgram extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		PDICMainAppOptions opt = ((Toastable_Activity) getActivity()).opt;
		mPreferenceId = R.xml.preferences;
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "enable_pastebin", PDICMainAppOptions.getShowPasteBin(), null, null);
		init_switch_preference(this, "keep_screen", PDICMainAppOptions.getKeepScreen(), null, null);
		init_switch_preference(this, "GPBC", null, "0x"+Integer.toHexString(CMN.GlobalPageBackground).toUpperCase(), null);
		init_switch_preference(this, "BCM", null, "0x"+Integer.toHexString(opt.getMainBackground()).toUpperCase(), null);
		init_switch_preference(this, "BCF", null, "0x"+Integer.toHexString(opt.getFloatBackground()).toUpperCase(), null);
		//init_number_info_preference(this, "paste_target", PDICMainAppOptions.getPasteTarget(), R.array.paste_target_info, null);
		//init_switch_preference(this, "f_share_peruse", PDICMainAppOptions.getShareToPeruseModeWhenFocued(), null, null);
		init_switch_preference(this, "f_paste_peruse", PDICMainAppOptions.getPasteToPeruseModeWhenFocued(), null, null);
		init_switch_preference(this, "f_move_bg", PDICMainAppOptions.getFloatClickHideToBackground(), null, null);
		init_switch_preference(this, "f_hide_recent", PDICMainAppOptions.getHideFloatFromRecent(), null, null);
		

		findPreference("f_size").setDefaultValue(GlobalOptions.isLarge?150:125);
		
		findPreference("dev").setOnPreferenceClickListener(this);
		findPreference("sspec").setOnPreferenceClickListener(this);
		findPreference("vspec").setOnPreferenceClickListener(this);
		findPreference("backup").setOnPreferenceChangeListener(this);
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
		}
		if(fragmentId!=-1){
			SettingsActivity.launch(getContext(), fragmentId);
		}
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "backup":
				Toastable_Activity a = (Toastable_Activity) getActivity();
				if (a==null) break;
				int idx = IU.parsint(newValue, -1);
				if(idx==0) {
					try {
						a.opt.backup();
						a.showT("备份成功！");
					} catch (Exception e) {
						a.showT("备份失败，请检查存储权限与空间！"+e);
					}
				} else if(idx==1) {
					try {
						a.opt.restore();
						a.showT("设置已恢复，重启生效！");
					} catch (Exception e) {
						a.showT(""+e);
					}
				}
			break;
			case "enable_pastebin":
				PDICMainAppOptions.setShowPasteBin((Boolean) newValue);
			break;
			case "keep_screen":
				PDICMainAppOptions.setKeepScreen((Boolean) newValue);
			break;
			case "GPBC":
				setColorPreferenceTitle(preference, newValue);
				CMN.GlobalPageBackground=(int) newValue;
			break;
			case "BCM":
				setColorPreferenceTitle(preference, newValue);
				CMN.AppColorChangedFlag|=0x1;
			break;
			case "BCF":
				setColorPreferenceTitle(preference, newValue);
				CMN.AppColorChangedFlag|=0x2;
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
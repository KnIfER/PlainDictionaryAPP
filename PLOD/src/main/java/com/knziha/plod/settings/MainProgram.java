package com.knziha.plod.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.knziha.filepicker.settings.FilePickerPreference;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;

import java.io.File;
import java.util.ArrayList;

public class MainProgram extends PlainSettingsFragment implements Preference.OnPreferenceClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		PDICMainAppOptions opt = ((Toastable_Activity) getActivity()).opt;
		mPreferenceId = R.xml.pref_main;
		super.onCreate(savedInstanceState);
		
		
		ArrayList<Preference> preferences = new ArrayList<>(64);
		PreferenceScreen screen = mPreferenceManager.mPreferenceScreen;
		if(screen!=null){
			preferences.add(screen);
			for (int i = 0; i < preferences.size(); i++) {
				Preference p = preferences.get(i);
				if (p instanceof PreferenceGroup) {
					preferences.addAll(((PreferenceGroup)p).getPreferences());
				} else {
					String key = p.getKey();
					switch (key) {
						case "noext":
							init_switch_preference(this, "noext", PDICMainAppOptions.exitToBackground(), null, null, null);
							break;
						case "back_web":
							init_switch_preference(this, "back_web", PDICMainAppOptions.getUseBackKeyGoWebViewBack(), null, null, null).setVisible(false);
							break;
						case "conext":
							init_number_info_preference(this, "conext", PDICMainAppOptions.getBackPrevention(), R.array.conext_info, null, null);
							p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
								@Override
								public boolean onPreferenceClick(Preference preference) {
									ListPreference lp = ((ListPreference) preference);
									if(PDICMainAppOptions.exitToBackground()){
										lp.setEntries(R.array.conhom_info);
										lp.setEntryValues(R.array.conhom);
										lp.setValue((PDICMainAppOptions.getBackToHomePagePreventBack()?5:4)+"");
									}else{
										lp.setEntries(R.array.conext_info);
										lp.setEntryValues(R.array.conext);
										lp.setValue(PDICMainAppOptions.getBackPrevention()+"");
									}
									return false;
								}
							});
							break;
						case "enable_pastebin":
							init_switch_preference(this, "enable_pastebin", PDICMainAppOptions.getShowPasteBin(), null, null, null);
							break;
						case "keep_screen":
							init_switch_preference(this, "keep_screen", PDICMainAppOptions.getKeepScreen(), null, null, null);
							break;
						case "GPBC":
							init_switch_preference(this, "GPBC", null, "0x"+Integer.toHexString(CMN.GlobalPageBackground).toUpperCase(), null, null);
							break;
						case "BCM":
							init_switch_preference(this, "BCM", null, "0x"+Integer.toHexString(opt.getMainBackground()).toUpperCase(), null, null);
							break;
						case "BCF":
							init_switch_preference(this, "BCF", null, "0x"+Integer.toHexString(opt.getFloatBackground()).toUpperCase(), null, null);
							break;
						case "f_paste_peruse":
							init_switch_preference(this, "f_paste_peruse", PDICMainAppOptions.getPasteToPeruseModeWhenFocued(), null, null, null);
							break;
						case "f_move_bg":
							init_switch_preference(this, "f_move_bg", PDICMainAppOptions.getFloatClickHideToBackground(), null, null, null);
							break;
						case "f_hide_recent":
							init_switch_preference(this, "f_hide_recent", PDICMainAppOptions.getHideFloatFromRecent(), null, null, null);
							break;
						case "stsch":
							init_switch_preference(this, "stsch", PDICMainAppOptions.restoreLastSch(), null, null, null);
							break;
						case "f_size":
							findPreference("f_size").setDefaultValue(GlobalOptions.isLarge?150:125);
							break;
						case "dev":
						case "sspec":
						case "vspec":
						case "night":
						case "multi":
						case "more_color":
							p.setOnPreferenceClickListener(this);
							break;
					}
					p.setOnPreferenceChangeListener(this);
				}
			}
		}
		
		//init_number_info_preference(this, "paste_target", PDICMainAppOptions.getPasteTarget(), R.array.paste_target_info, null);
		//init_switch_preference(this, "f_share_peruse", PDICMainAppOptions.getShareToPeruseModeWhenFocued(), null, null);
		
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		int fragmentId=-1;
		switch (preference.getKey()){
			case "dev":
				fragmentId=DevOpt.id;
			break;
			case "sspec":
				fragmentId=SchOpt.id;
			break;
			case "night":
				fragmentId=NightMode.id;
			break;
			case "more_color":
				fragmentId= MoreColors.id;
			break;
			case "vspec":
				fragmentId=Misc.id;
			break;
			case "multi":
				fragmentId=Multiview.id;
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
			case "back_web":
				PDICMainAppOptions.setUseBackKeyGoWebViewBack((Boolean) newValue);
				break;
			case "conext":
				int val = IU.parsint(newValue);
				if(val>=4){
					PDICMainAppOptions.setBackToHomePagePreventBack(val>4);
				} else {
					PDICMainAppOptions.setBackPrevention(val);
					CMN.debug("setBackPrevention::", val, PDICMainAppOptions.getBackPrevention());
					
					preference.setSummary(getResources().getStringArray(R.array.conext_info)[val]);
				}
				break;
			case "noext":
				PDICMainAppOptions.exitToBackground((Boolean) newValue);
				break;
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
				CMN.AppColorChangedFlag|=1<<MainActivityUIBase.ActType.PlainDict.ordinal();
			break;
			case "BCF":
				setColorPreferenceTitle(preference, newValue);
				CMN.AppColorChangedFlag|=1<<MainActivityUIBase.ActType.FloatSearch.ordinal();
			break;
			case "stsch":
				PDICMainAppOptions.restoreLastSch((Boolean) newValue);
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
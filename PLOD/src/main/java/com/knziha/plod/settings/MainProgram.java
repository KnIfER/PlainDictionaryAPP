package com.knziha.plod.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.knziha.filepicker.settings.FilePickerPreference;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.mdict;

import java.io.File;
import java.util.HashMap;

public class MainProgram extends SettingsFragment implements Preference.OnPreferenceClickListener {
	private String localeStamp;
	private HashMap<String, String> nym;
	StringBuilder flag_code= new StringBuilder();

	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		init_switch_preference(this, "enable_pastebin", PDICMainAppOptions.getShowPasteBin(), null, null);
		init_switch_preference(this, "keep_screen", PDICMainAppOptions.getKeepScreen(), null, null);
		init_switch_preference(this, "classical_sort", PDICMainAppOptions.getClassicalKeycaseStrategy(), null, null);
		init_switch_preference(this, "GPBC", null, "0x"+Integer.toHexString(CMN.GlobalPageBackground).toUpperCase(), null);
		init_switch_preference(this, "BCM", null, "0x"+Integer.toHexString(CMN.MainBackground).toUpperCase(), null);
		init_switch_preference(this, "BCF", null, "0x"+Integer.toHexString(CMN.FloatBackground).toUpperCase(), null);
		init_number_info_preference(this, "paste_target", PDICMainAppOptions.getPasteTarget(), R.array.paste_target_info, null);
		init_number_info_preference(this, "share_target", PDICMainAppOptions.getShareTarget(), R.array.paste_target_info, null);
		//init_switch_preference(this, "f_share_peruse", PDICMainAppOptions.getShareToPeruseModeWhenFocued(), null, null);
		init_switch_preference(this, "f_paste_peruse", PDICMainAppOptions.getPasteToPeruseModeWhenFocued(), null, null);
		init_switch_preference(this, "f_move_bg", PDICMainAppOptions.getFloatClickHideToBackground(), null, null);
		init_switch_preference(this, "f_hide_recent", PDICMainAppOptions.getHideFloatFromRecent(), null, null);

		findPreference("dev").setOnPreferenceClickListener(this);
		findPreference("sspec").setOnPreferenceClickListener(this);
		findPreference("vspec").setOnPreferenceClickListener(this);
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
			case "locale":
				if(localeStamp!=null)
					PDICMainAppOptions.locale=localeStamp.equals(newValue)?localeStamp:null;
				preference.setSummary(getNameFlag((String) newValue));
			break;
			case "paste_target":
				preference.setSummary(getResources().getStringArray(R.array.paste_target_info)[PDICMainAppOptions.setPasteTarget(IU.parsint(newValue))]);
			break;
			case "share_target":
				preference.setSummary(getResources().getStringArray(R.array.paste_target_info)[PDICMainAppOptions.setShareTarget(IU.parsint(newValue))]);
			break;
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

	private void setColorPreferenceTitle(Preference preference, Object newValue) {
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
package com.knziha.plod.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.preference.Preference;

import com.knziha.filepicker.settings.FilePickerPreference;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.MultiShareActivity;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymodels.mdict;

import java.io.File;
import java.util.HashMap;

public class ServerPreference extends SettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=11;

	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findPreference("mdccsp").setOnPreferenceClickListener(this);
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
		addPreferencesFromResource(R.xml.serverpreferences);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		switch (preference.getKey()){
			case "mdccsp":
				startActivity(new Intent(getActivity(), MultiShareActivity.class));
			break;
		}
		return true;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "enable_pastebin":
				//PDICMainAppOptions.setShowPasteBin((Boolean) newValue);
			break;
		}
		return true;
	}
}
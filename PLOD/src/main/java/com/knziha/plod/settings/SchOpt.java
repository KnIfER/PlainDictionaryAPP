package com.knziha.plod.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

public class SchOpt extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id=7;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.searchpreferences;
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "enable_regex1", PDICMainAppOptions.getUseRegex1(), null, null);
		init_switch_preference(this, "enable_regex2", PDICMainAppOptions.getUseRegex2(), null, null);
		//init_switch_preference(this, "joni_head", PDICMainAppOptions.getRegexAutoAddHead(), null, null);
		init_switch_preference(this, "joni_case", PDICMainAppOptions.getJoniCaseSensitive(), null, null);
		init_switch_preference(this, "use_wildcard1", PDICMainAppOptions.getAdvSearchUseWildcard(), null, null);
		init_switch_preference(this, "use_morph", PDICMainAppOptions.getSearchUseMorphology(), null, null);
		findPreference("vspec").setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		Intent intent = new Intent();
		intent.putExtra("realm", 8);
		intent.setClass(getContext(), SettingsActivity.class);
		startActivity(intent);
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "enable_regex1":
				PDICMainAppOptions.setUseRegex1((Boolean) newValue);
			break;
			case "enable_regex2":
				PDICMainAppOptions.setUseRegex2((Boolean) newValue);
			break;
//			case "joni_head":
//				PDICMainAppOptions.setRegexAutoAddHead((Boolean) newValue);
//			break;
			case "joni_case":
				PDICMainAppOptions.setJoniCaseSensitive((Boolean) newValue);
			break;
			case "use_wildcard1":
				PDICMainAppOptions.setAdvSearchUseWildcard((Boolean) newValue);
			break;
			case "use_morph":
				PDICMainAppOptions.setSearchUseMorphology((Boolean) newValue);
			break;
		}
		return true;
	}
}
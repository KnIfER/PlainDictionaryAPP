package com.knziha.plod.settings;

import android.os.Bundle;

import androidx.preference.Preference;

import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.IU;

public class HistoryPreference extends SettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=10;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "rc_nothing", PDICMainAppOptions.getHistoryStrategy0(), null, null);
//		init_switch_preference(this, "rc_enter_ff", PDICMainAppOptions.getHistoryStrategy1(), null, null);
//		init_switch_preference(this, "rc_enter_com", PDICMainAppOptions.getHistoryStrategy2(), null, null);
//		init_switch_preference(this, "rc_enter_cmn", PDICMainAppOptions.getHistoryStrategy3(), null, null);
		init_switch_preference(this, "rc_click_ff", PDICMainAppOptions.getHistoryStrategy4(), null, null);
		init_switch_preference(this, "rc_click_com", PDICMainAppOptions.getHistoryStrategy5(), null, null);
		init_switch_preference(this, "rc_click_cmn", PDICMainAppOptions.getHistoryStrategy6(), null, null);
		init_switch_preference(this, "rc_peruse_key", PDICMainAppOptions.getHistoryStrategy7(), null, null);
		init_switch_preference(this, "rc_peruse_click", PDICMainAppOptions.getHistoryStrategy9(), null, null);
		init_switch_preference(this, "rc_pop_key", PDICMainAppOptions.getHistoryStrategy10(), null, null);
		init_switch_preference(this, "rc_pop_slide", PDICMainAppOptions.getHistoryStrategy11(), null, null);
		init_switch_preference(this, "rc_float_pop", PDICMainAppOptions.getHistoryStrategy12(), null, null);
		init_number_info_preference(this, "rc_slide", PDICMainAppOptions.getHistoryStrategy8(), R.array.record_slide_info, null);
	}

	//创建
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.historypreferences);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "rc_nothing":
				PDICMainAppOptions.setHistoryStrategy0((Boolean) newValue);
			break;
//			case "rc_enter_ff":
//				PDICMainAppOptions.setHistoryStrategy1((Boolean) newValue);
//			break;
//			case "rc_enter_com":
//				PDICMainAppOptions.setHistoryStrategy2((Boolean) newValue);
//			break;
//			case "rc_enter_cmn":
//				PDICMainAppOptions.setHistoryStrategy3((Boolean) newValue);
//			break;
			case "rc_click_ff":
				PDICMainAppOptions.setHistoryStrategy4((Boolean) newValue);
			break;
			case "rc_click_com":
				PDICMainAppOptions.setHistoryStrategy5((Boolean) newValue);
			break;
			case "rc_click_cmn":
				PDICMainAppOptions.setHistoryStrategy6((Boolean) newValue);
			break;
			case "rc_peruse_key":
				PDICMainAppOptions.setHistoryStrategy7((Boolean) newValue);
			break;
			case "rc_peruse_click":
				PDICMainAppOptions.setHistoryStrategy9((Boolean) newValue);
			break;
			case "rc_pop_key":
				PDICMainAppOptions.setHistoryStrategy10((Boolean) newValue);
			break;
			case "rc_pop_slide":
				PDICMainAppOptions.setHistoryStrategy11((Boolean) newValue);
			break;
			case "rc_float_pop":
				PDICMainAppOptions.setHistoryStrategy12((Boolean) newValue);
			break;
			case "rc_slide":
				int val = IU.parsint(newValue);
				preference.setSummary(getResources().getStringArray(R.array.record_slide_info)[PDICMainAppOptions.setHistoryStrategy8(val)]);
			break;
		}
		return true;
	}
}
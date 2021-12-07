package com.knziha.plod.settings;

import android.os.Bundle;

import androidx.preference.Preference;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;

public class HistoryPreference extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id=10;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.historypreferences;
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "rc_nothing", PDICMainAppOptions.getHistoryStrategy0(), null, null);
		
		init_switch_preference(this, "rc_click", PDICMainAppOptions.getHistoryStrategy4(), null, null);
		
		init_switch_preference(this, "rc_query", PDICMainAppOptions.getHistoryStrategy1(), null, null);
		
		init_switch_preference(this, "rc_net", PDICMainAppOptions.getHistoryStrategy2(), null, null);
		
		init_switch_preference(this, "rc_float_pop", PDICMainAppOptions.getHistoryStrategy7(), null, null);
		
		init_number_info_preference(this, "rc_slide", PDICMainAppOptions.getHistoryStrategy8(), R.array.record_slide_info, null);
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
			/* 记录各种点击 */
			case "rc_click":
				PDICMainAppOptions.setHistoryStrategy4((Boolean) newValue);
			break;
			/* 记录各种查询 */
			case "rc_query":
				PDICMainAppOptions.setHistoryStrategy1((Boolean) newValue);
			break;
			/* 记录各种联机 */
			case "rc_net":
				PDICMainAppOptions.setHistoryStrategy2((Boolean) newValue);
			break;
			/* 记录各种弹出 */
			case "rc_float_pop":
				PDICMainAppOptions.setHistoryStrategy7((Boolean) newValue);
			break;
			case "rc_slide":
				int val = IU.parsint(newValue);
				PDICMainAppOptions.setHistoryStrategy8(val);
			break;
		}
		return true;
	}
}
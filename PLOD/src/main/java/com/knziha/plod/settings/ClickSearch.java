package com.knziha.plod.settings;

import android.os.Bundle;

import androidx.preference.Preference;

import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;

public class ClickSearch extends SettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=9;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "coord_view", PDICMainAppOptions.getImmersiveClickSearch(), null, null);

		init_switch_preference(this, "top_resize", PDICMainAppOptions.getTopSnapMaximizeClickSearch(), null, null);
		init_switch_preference(this, "double_resize", PDICMainAppOptions.getDoubleClickMaximizeClickSearch(), null, null);
		//init_switch_preference(this, "multi_cs", PDICMainAppOptions.getMultipleClickSearch(), null, null);
		init_switch_preference(this, "use_morph", PDICMainAppOptions.getClickSearchUseMorphology(), null, null);
		init_number_info_preference(this, "mode_cs", PDICMainAppOptions.getClickSearchMode(), R.array.click_search_mode_info, null);
		init_switch_preference(this, "pin_upstream", PDICMainAppOptions.getPinClickSearch(), null, null);
		//init_switch_preference(this, "skip_nom", PDICMainAppOptions.getSkipClickSearch(), null, null);
		init_switch_preference(this, "reset_pos", PDICMainAppOptions.getResetPosClickSearch(), null, null);
		init_switch_preference(this, "reset_max", PDICMainAppOptions.getResetMaxClickSearch(), null, null);
		init_switch_preference(this, "switch_top", PDICMainAppOptions.getSwichClickSearchDictOnTop(), null, null);
		init_switch_preference(this, "switch_bottom", PDICMainAppOptions.getSwichClickSearchDictOnBottom(), null, null);
		init_switch_preference(this, "switch_nav", PDICMainAppOptions.getSwichClickSearchDictOnNav(), null, null);
		init_switch_preference(this, "delay_diss", PDICMainAppOptions.getClickSearchDismissDelay(), null, null);
		init_switch_preference(this, "click_tts", PDICMainAppOptions.getClickSearchAutoReadEntry(), null, null);
	}

	//创建
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.clicksearchpreferences);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "coord_view":
				PDICMainAppOptions.setImmersiveClickSearch((Boolean) newValue);
			break;
			case "top_resize":
				PDICMainAppOptions.setTopSnapMaximizeClickSearch((Boolean) newValue);
			break;
			case "double_resize":
				PDICMainAppOptions.setDoubleClickMaximizeClickSearch((Boolean) newValue);
			break;
			case "delay_diss":
				PDICMainAppOptions.setClickSearchDismissDelay((Boolean) newValue);
			break;
			case "click_tts":
				PDICMainAppOptions.setClickSearchAutoReadEntry((Boolean) newValue);
			break;
			case "multi_cs":
				PDICMainAppOptions.setMultipleClickSearch((Boolean) newValue);
			break;
			case "mode_cs":
				int val = IU.parsint(newValue, 0);
				PDICMainAppOptions.setClickSearchMode(val);
				preference.setSummary(getResources().getStringArray(R.array.click_search_mode_info)[val]);
			break;
			case "pin_upstream":
				PDICMainAppOptions.setPinClickSearch((boolean) newValue);
			break;
			case "skip_nom":
				PDICMainAppOptions.setSkipClickSearch((boolean) newValue);
			break;
			case "reset_pos":
				PDICMainAppOptions.setResetPosClickSearch((boolean) newValue);
			break;
			case "reset_max":
				PDICMainAppOptions.setResetMaxClickSearch((boolean) newValue);
			break;
			case "switch_top":
				PDICMainAppOptions.setSwichClickSearchDictOnTop((boolean) newValue);
			break;
			case "switch_bottom":
				PDICMainAppOptions.setSwichClickSearchDictOnBottom((boolean) newValue);
			break;
			case "switch_nav":
				PDICMainAppOptions.setSwichClickSearchDictOnNav((boolean) newValue);
			break;
			case "use_morph":
				PDICMainAppOptions.setClickSearchUseMorphology((Boolean) newValue);
			break;
		}
		return true;
	}
}
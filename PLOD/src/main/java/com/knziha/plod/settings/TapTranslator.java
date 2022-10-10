package com.knziha.plod.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.knziha.plod.db.SearchUI;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;

import java.util.ArrayList;

public class TapTranslator extends PlainSettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=R.xml.pref_tapsch;
	public final static int requestCode=id&0xFFFF;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = id;
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
						case "immersive":
							init_switch_preference(this, "immersive", PDICMainAppOptions.getImmersiveClickSearch(), null, null, p);
							break;
						case "top_resize":
							init_switch_preference(this, "top_resize", PDICMainAppOptions.getImmersiveClickSearch(), null, null, p);
							break;
						case "double_resize":
							init_switch_preference(this, "double_resize", PDICMainAppOptions.getDoubleClickMaximizeClickSearch(), null, null, p);
							break;
						case "use_morph":
							init_switch_preference(this, "use_morph", PDICMainAppOptions.getClickSearchUseMorphology(), null, null, p);
							break;
						case "mode_cs":
							//init_switch_preference(this, "multi_cs", PDICMainAppOptions.getMultipleClickSearch(), null, p);
							init_number_info_preference(this, "mode_cs", PDICMainAppOptions.getClickSearchMode(), R.array.click_search_mode_info, null, p);
							break;
						case "pin_upstream":
							//init_switch_preference(this, "skip_nom", PDICMainAppOptions.getSkipClickSearch(), null, p);
							init_switch_preference(this, "pin_upstream", PDICMainAppOptions.getPinClickSearch(), null, null, p);
							break;
						case "reset_pos":
							init_switch_preference(this, "reset_pos", PDICMainAppOptions.getResetPosClickSearch(), null, null, p);
							break;
						case "reset_max":
							init_switch_preference(this, "reset_max", PDICMainAppOptions.getResetMaxClickSearch(), null, null, p);
							break;
						case "switch_nav":
							//init_switch_preference(this, "switch_top", PDICMainAppOptions.getSwichClickSearchDictOnTop(), null, p);
							//init_switch_preference(this, "switch_bottom", PDICMainAppOptions.getSwichClickSearchDictOnBottom(), null, p);
							init_switch_preference(this, "switch_nav", PDICMainAppOptions.getSwichClickSearchDictOnNav(), null, null, p);
							break;
						case "delay_diss":
							init_switch_preference(this, "delay_diss", PDICMainAppOptions.getClickSearchDismissDelay(), null, null, p);
							break;
						case "click_tts":
							init_switch_preference(this, "click_tts", PDICMainAppOptions.tapSchAutoReadEntry(), null, null, p);
							break;
						case "click_tts1":
							init_switch_preference(this, "click_tts1", PDICMainAppOptions.tapSchPageAutoReadEntry(), null, null, p);
							break;
						case "skip_webx":
							init_switch_preference(this, "skip_webx", PDICMainAppOptions.getTapSkipWebxUnlessIsDedicated(), null, null, p);
							break;
						case "exempt_translator":
							init_switch_preference(this, "exempt_translator", PDICMainAppOptions.getTapTreatTranslatorAsDedicated(), null, null, p);
							break;
						case "tz":
							init_switch_preference(this, "tz", PDICMainAppOptions.tapZoomTapSch(), null, null, p);
							break;
						case "turn":
							init_switch_preference(this, "turn", PDICMainAppOptions.turnPageTapSch(), null, null, p);
							break;
						case "tools":
							init_switch_preference(this, "tools", PDICMainAppOptions.tapSchShowToolsBtn(), null, null, p);
							break;
						case "prvnxt":
							init_switch_preference(this, "prvnxt", PDICMainAppOptions.showPrvNxtBtnSmallTapSch(), null, null, p);
							break;
						case "seek":
							init_switch_preference(this, "seek", PDICMainAppOptions.showEntrySeekbarTapSch(), null, null, p);
							break;
						case "seekF":
							init_switch_preference(this, "seekF", PDICMainAppOptions.showEntrySeekbarTapSchFolding(), null, null, p);
							break;
						case "bar":
						case "tz1":
							p.setOnPreferenceClickListener(this);
							break;
						case "difSet":
							init_switch_preference(this, "difSet", PDICMainAppOptions.wordPopupAllowDifferentSet(), null, null, p);
							break;
						case "remSet":
							init_switch_preference(this, "remSet", PDICMainAppOptions.wordPopupRemDifferenSet(), null, null, p);
							break;
					}
					p.setOnPreferenceChangeListener(this);
				}
			}
		}
		

		
		
		
		
		//init_switch_preference(this, "fold", PDICMainAppOptions.foldingScreenTapSch(), null, null);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("bar")) {
			getActivity().setResult(requestCode);
			getActivity().finish();
			return true;
		}
		if (preference.getKey().equals("tz1")) {
			CMN.Log("todo...");
			return true;
		}
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "immersive":
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
				PDICMainAppOptions.tapSchAutoReadEntry((Boolean) newValue);
			break;
			case "click_tts1":
				PDICMainAppOptions.tapSchPageAutoReadEntry((Boolean) newValue);
			break;
			case "skip_webx":
				PDICMainAppOptions.setTapSkipWebxUnlessIsDedicated((Boolean) newValue);
			break;
			case "exempt_translator":
				PDICMainAppOptions.setTapTreatTranslatorAsDedicated((Boolean) newValue);
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
//			case "switch_top":
//				PDICMainAppOptions.setSwichClickSearchDictOnTop((boolean) newValue);
//			break;
//			case "switch_bottom":
//				PDICMainAppOptions.setSwichClickSearchDictOnBottom((boolean) newValue);
//			break;
			case "switch_nav":
				PDICMainAppOptions.setSwichClickSearchDictOnNav((boolean) newValue);
			break;
			case "use_morph":
				PDICMainAppOptions.setClickSearchUseMorphology((Boolean) newValue);
			break;
			case "tz":
				PDICMainAppOptions.tapZoomTapSch((Boolean) newValue);
				SearchUI.tapZoomV++;
			break;
			case "turn":
				PDICMainAppOptions.turnPageTapSch((Boolean) newValue);
				SearchUI.tapZoomV++;
			break;
			case "tools":
				PDICMainAppOptions.tapSchShowToolsBtn((Boolean) newValue);
			break;
			case "fold":
				PDICMainAppOptions.foldingScreenTapSch((Boolean) newValue);
				SearchUI.btmV++;
			break;
			case "prvnxt":
				PDICMainAppOptions.showPrvNxtBtnSmallTapSch((Boolean) newValue);
				SearchUI.btmV++;
			break;
			case "seek":
				PDICMainAppOptions.showEntrySeekbarTapSch((Boolean) newValue);
				SearchUI.btmV++;
			break;
			case "seekF":
				PDICMainAppOptions.showEntrySeekbarTapSchFolding((Boolean) newValue);
				SearchUI.btmV++;
			break;
			case "difSet":
				PDICMainAppOptions.wordPopupAllowDifferentSet((Boolean) newValue);
			break;
			case "remSet":
				PDICMainAppOptions.wordPopupRemDifferenSet((Boolean) newValue);
			break;
		}
		return true;
	}
}
package com.knziha.plod.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.dictionary.Utils.IU;

public class ViewSpecification extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id=8;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init_switch_preference(this, "ps_audio_key", PDICMainAppOptions.getInPageSearchUseAudioKey(), null, null);
		init_switch_preference(this, "ps_hide_key", PDICMainAppOptions.getInPageSearchAutoHideKeyboard(), null, null);
		init_switch_preference(this, "ps_yicha", !PDICMainAppOptions.getInPageSearchShowNoNoMatch(), null, null);
		init_switch_preference(this, "ps_border", PDICMainAppOptions.getInPageSearchHighlightBorder(), null, null);
		init_switch_preference(this, "ap_full", PDICMainAppOptions.getInPageSearchAutoUpdateAfterFulltext(), null, null);
		init_switch_preference(this, "ap_click", PDICMainAppOptions.getInPageSearchAutoUpdateAfterClick(), null, null);
		init_switch_preference(this, "noext", PDICMainAppOptions.getBackToHomePage(), null, null);
		init_switch_preference(this, "clear_sel", PDICMainAppOptions.getUseBackKeyClearWebViewFocus(), null, null);
		init_switch_preference(this, "back_web", PDICMainAppOptions.getUseBackKeyGoWebViewBack(), null, null);
		Preference p = init_number_info_preference(this, "conext", PDICMainAppOptions.getBackPrevention(), R.array.conext_info, null);
		p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
										   @Override
										   public boolean onPreferenceClick(Preference preference) {
											   ListPreference lp = ((ListPreference) preference);
											   if(PDICMainAppOptions.getBackToHomePage()){
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
		init_switch_preference(this, "hint_mod", PDICMainAppOptions.getHintSearchMode(), null, null);
		init_switch_preference(this, "hint_res", PDICMainAppOptions.getNotifyComboRes(), null, null);
		init_switch_preference(this, "simple", PDICMainAppOptions.getSimpleMode(), null, null);
		init_switch_preference(this, "magny", PDICMainAppOptions.getEtSearchNoMagnifier(), null, null);
		init_switch_preference(this, "magny2", PDICMainAppOptions.getHackDisableMagnifier(), null, null);
		init_switch_preference(this, "expand_ao", PDICMainAppOptions.getEnsureAtLeatOneExpandedPage(), null, null);
		init_switch_preference(this, "expand_top", PDICMainAppOptions.getOnlyExpandTopPage(), null, null);
		init_switch_preference(this, "scranima", PDICMainAppOptions.getScrollAnimation(), null, null);
		init_switch_preference(this, "scrautex", PDICMainAppOptions.getScrollAutoExpand(), null, null);
		init_switch_preference(this, "turbo_top", PDICMainAppOptions.getDelaySecondPageLoading(), null, null);
		init_switch_preference(this, "1toast", PDICMainAppOptions.getRebuildToast(), null, null);
		init_switch_preference(this, "rtoast", PDICMainAppOptions.getToastRoundedCorner(), null, null);

	}

	//创建
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.viewpreferences);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "ps_audio_key":
				PDICMainAppOptions.setInPageSearchUseAudioKey((Boolean) newValue);
			break;
			case "ps_hide_key":
				PDICMainAppOptions.setInPageSearchAutoHideKeyboard((Boolean) newValue);
			break;
			case "ps_yicha":
				PDICMainAppOptions.setInPageSearchShowNoNoMatch(!(Boolean) newValue);
			break;
			case "ps_border":
				PDICMainAppOptions.setInPageSearchHighlightBorder((Boolean) newValue);
			break;
			case "ap_full":
				PDICMainAppOptions.setInPageSearchAutoUpdateAfterFulltext((Boolean) newValue);
			break;
			case "ap_click":
				PDICMainAppOptions.setInPageSearchAutoUpdateAfterClick((Boolean) newValue);
			break;
			case "noext":
				PDICMainAppOptions.setBackToHomePage((Boolean) newValue);
			break;
			case "clear_sel":
				PDICMainAppOptions.setUseBackKeyClearWebViewFocus((Boolean) newValue);
			break;
			case "back_web":
				PDICMainAppOptions.setUseBackKeyGoWebViewBack((Boolean) newValue);
			break;
			case "conext":
				int val = IU.parsint(newValue);
				if(val>=4){
					PDICMainAppOptions.setBackToHomePagePreventBack(val>4);
				}else{
					PDICMainAppOptions.setBackPrevention(val);
					preference.setSummary(getResources().getStringArray(R.array.conext_info)[val]);
				}
			break;
			case "hint_mod":
				PDICMainAppOptions.setHintSearchMode((Boolean) newValue);
			break;
			case "hint_res":
				PDICMainAppOptions.setNotifyComboRes((Boolean) newValue);
			break;
			case "simple":
				PDICMainAppOptions.setSimpleMode((Boolean) newValue);
				CMN.Log("simple", newValue, PDICMainAppOptions.getSimpleMode());
			break;
			case "magny":
				PDICMainAppOptions.setEtSearchNoMagnifier((Boolean) newValue);
			break;
			case "magny2":
				PDICMainAppOptions.setHackDisableMagnifier((Boolean) newValue);
			break;
			case "expand_ao":
				PDICMainAppOptions.setEnsureAtLeatOneExpandedPage((Boolean) newValue);
			break;
			case "expand_top":
				PDICMainAppOptions.setOnlyExpandTopPage((Boolean) newValue);
			break;
			case "scranima":
				PDICMainAppOptions.setScrollAnimation((Boolean) newValue);
			break;
			case "scrautex":
				PDICMainAppOptions.setScrollAutoExpand((Boolean) newValue);
			break;
			case "turbo_top":
				PDICMainAppOptions.setDelaySecondPageLoading((Boolean) newValue);
			break;
			case "1toast":
				PDICMainAppOptions.setRebuildToast((Boolean) newValue);
			break;
			case "rtoast":
				PDICMainAppOptions.setToastRoundedCorner((Boolean) newValue);
				((Toastable_Activity)getActivity()).showT("no where, now here");
			break;
		}
		return true;
	}
}
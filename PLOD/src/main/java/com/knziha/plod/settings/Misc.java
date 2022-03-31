package com.knziha.plod.settings;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;

public class Misc extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id=8;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.pref_misc;
		super.onCreate(savedInstanceState);
		
		findPreference("cat_1").setVisible(false);

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
		
		init_switch_preference(this, "1toast", PDICMainAppOptions.getRebuildToast(), null, null);
		init_switch_preference(this, "rtoast", PDICMainAppOptions.getToastRoundedCorner(), null, null);

	}
	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key=preference.getKey();
		if (newValue instanceof String) {
			String str = (String) newValue;
			boolean b1=str.equals("using");
			boolean b2=!b1&&(str.startsWith("use")&&(str.length()==3||str.endsWith("_not")));
			//CMN.Log("onPreferenceChange::", b1, b2, str, key);
			if (b1||b2) {
				String rtrStr= "use_"+key;
				if(TextUtils.equals(rtrStr, "use_expand_top")) {
					if(b1) return PDICMainAppOptions.getOnlyExpandTopPage();
					PDICMainAppOptions.setOnlyExpandTopPage(str.length()==3);
					return true;
				}
			}
		}
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
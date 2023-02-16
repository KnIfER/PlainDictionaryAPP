package com.knziha.plod.settings;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.ViewUtils;

public class Misc extends PlainSettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=R.xml.pref_misc;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.pref_misc;
		super.onCreate(savedInstanceState);
		
		//findPreference("cat_1").setVisible(false);

		init_switch_preference(this, "ps_audio_key", PDICMainAppOptions.schPageNavAudioKey(), null, null, null);
		init_switch_preference(this, "ps_hide_key", PDICMainAppOptions.schPageNavHideKeyboard(), null, null, null);
		init_switch_preference(this, "ps_yicha", !PDICMainAppOptions.schPageShowHints(), null, null, null);
//		init_switch_preference(this, "ps_border", PDICMainAppOptions.getInPageSearchHighlightBorder(), null, null);
		init_switch_preference(this, "ap_full", PDICMainAppOptions.schPageAfterFullSch(), null, null, null);
		init_switch_preference(this, "ap_click", PDICMainAppOptions.schPageAfterClick(), null, null, null);
		init_switch_preference(this, "clear_sel", PDICMainAppOptions.getUseBackKeyClearWebViewFocus(), null, null, null);
		init_switch_preference(this, "hint_mod", PDICMainAppOptions.getHintSearchMode(), null, null, null);
		init_switch_preference(this, "hint_res", PDICMainAppOptions.getNotifyComboRes(), null, null, null);
		init_switch_preference(this, "simple", PDICMainAppOptions.getSimpleMode(), null, null, null);
		init_switch_preference(this, "magny", PDICMainAppOptions.getEtSearchNoMagnifier(), null, null, null);
		init_switch_preference(this, "magny2", PDICMainAppOptions.getHackDisableMagnifier(), null, null, null);
		
		init_switch_preference(this, "menuBelow", !PDICMainAppOptions.menuOverlapAnchor(), null, null, null);
		
		
		init_switch_preference(this, "1toast", ViewUtils.bRebuildToast, null, null, null).setEnabled(false);
		init_switch_preference(this, "rtoast", PDICMainAppOptions.getToastRoundedCorner(), null, null, null);

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
		switch (key){
			case "ps_audio_key":
				PDICMainAppOptions.schPageNavAudioKey((Boolean) newValue);
			break;
			case "ps_hide_key":
				PDICMainAppOptions.schPageNavHideKeyboard((Boolean) newValue);
			break;
			case "ps_yicha":
				PDICMainAppOptions.schPageShowHints(!(Boolean) newValue);
			break;
			case "ap_full":
				PDICMainAppOptions.schPageAfterFullSch((Boolean) newValue);
			break;
			case "ap_click":
				PDICMainAppOptions.schPageAfterClick((Boolean) newValue);
			break;
			case "clear_sel":
				PDICMainAppOptions.setUseBackKeyClearWebViewFocus((Boolean) newValue);
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
			case "menuBelow":
				PDICMainAppOptions.menuOverlapAnchor(!(Boolean) newValue);
			break;
//			case "1toast":
//				PDICMainAppOptions.setRebuildToast((Boolean) newValue);
//			break;
			case "rtoast":
				PDICMainAppOptions.setToastRoundedCorner((Boolean) newValue);
				((Toastable_Activity)getActivity()).showT("no where, now here");
			break;
		}
		return true;
	}
}
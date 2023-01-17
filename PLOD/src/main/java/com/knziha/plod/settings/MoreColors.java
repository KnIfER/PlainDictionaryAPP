package com.knziha.plod.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.knziha.filepicker.settings.TwinkleSwitchPreference;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;

public class MoreColors extends PlainSettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=R.xml.pref_morecolors;
	public final static int requestCode=id&0xFFFF;
	
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
						case "oldColor":
							init_switch_preference(this, key, PDICMainAppOptions.useOldColorsMode(), null, null, p);
							if (PDICMainAppOptions.useOldColorsMode()) {
								findPreference("foreColor").setEnabled(false);
								findPreference("foreColor1").setEnabled(false);
							}
						break;
						case "ripple":
							init_switch_preference(this, key, PDICMainAppOptions.modRipple(), null, null, p);
							if (ViewUtils.littleCake) {
								p.getParent().setVisible(false);
								((TwinkleSwitchPreference)p).setChecked(false);
							}
						break;
					}
					p.setOnPreferenceChangeListener(this);
				}
			}
		}
	}
	
	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key=preference.getKey();
		int bUsing=0; // 1=get 2=set=false 3=set=true
		if (newValue instanceof String) {
			String str = (String) newValue;
			if (str.equals("using")) {
				bUsing = 1;
			} else if (str.startsWith("use")) {
				bUsing = str.length()==3?3
						:str.endsWith("_not")?2
						:0;
			}
		}
		switch (key){
			case "ripple":
				PDICMainAppOptions.modRipple((Boolean) newValue);
				getSettingActivity().showT("重启生效");
			break;
			case "oldColor":
				PDICMainAppOptions.useOldColorsMode((Boolean) newValue);
				findPreference("foreColor").setEnabled(!(Boolean) newValue);
				findPreference("foreColor1").setEnabled(!(Boolean) newValue);
				CMN.AppColorChangedFlag|=1<< MainActivityUIBase.ActType.PlainDict.ordinal();
				CMN.AppColorChangedFlag|=1<<MainActivityUIBase.ActType.FloatSearch.ordinal();
			break;
			case "foreColor":
			case "foreColor1":
				if(bUsing==0) {
					CMN.AppColorChangedFlag|=1<< MainActivityUIBase.ActType.PlainDict.ordinal();
					CMN.AppColorChangedFlag|=1<<MainActivityUIBase.ActType.FloatSearch.ordinal();
				}
				else if(bUsing==1) return PDICMainAppOptions.autoForegroundColor();
				else if(bUsing!=0) PDICMainAppOptions.autoForegroundColor(bUsing==3);
				break;
			case "rippleColor":
			case "rippleColor1":
				if(bUsing==0) {
					CMN.AppColorChangedFlag|=1<< MainActivityUIBase.ActType.PlainDict.ordinal();
					CMN.AppColorChangedFlag|=1<<MainActivityUIBase.ActType.FloatSearch.ordinal();
				}
				else if(bUsing==1) return PDICMainAppOptions.autoRippleColor();
				else if(bUsing!=0) PDICMainAppOptions.autoRippleColor(bUsing==3);
				break;
		}
		return true;
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}
}
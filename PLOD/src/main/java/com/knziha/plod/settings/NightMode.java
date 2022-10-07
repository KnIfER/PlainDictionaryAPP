package com.knziha.plod.settings;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

import java.util.ArrayList;

public class NightMode extends PlainSettingsFragment {
	public final static int id=R.xml.pref_nightmode;
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
						case "sys":
							init_switch_preference(this, key, PDICMainAppOptions.darkSystem(), null, null)
									.setEnabled(Build.VERSION.SDK_INT>=29);
						break;
						case "dkB":
							if (Build.VERSION.SDK_INT<=23) {
//								p.setPersistent(false);
//								p.setDefaultValue(PreferenceManager.getDefaultSharedPreferences(getSettingActivity())
//										.getInt(key, 0xFF333333));
//								p.setPersistent(true);
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
		if ("cat_2".equals(preference.getParent().getKey())) {
			PDICMainAppOptions.darkModeJsVer++;
		}
		switch (key){
			case "sys":
				PDICMainAppOptions.darkSystem((Boolean) newValue);
				// CMN.AppColorChangedFlag|=1<< MainActivityUIBase.ActType.PlainDict.ordinal();
			break;
			case "dkR":
				if(bUsing==1) return PDICMainAppOptions.nightUseInvertFilter();
				else if(bUsing!=0) PDICMainAppOptions.nightUseInvertFilter(bUsing==3);
				break;
			case "dkD":
				if(bUsing==1) return PDICMainAppOptions.nightDimAll();
				else if(bUsing!=0) PDICMainAppOptions.nightDimAll(bUsing==3);
				break;
			case "dkB":
				if(bUsing==1) return PDICMainAppOptions.nightUsePageColor();
				else if(bUsing!=0) PDICMainAppOptions.nightUsePageColor(bUsing==3);
				break;
			case "dkF":
				if(bUsing==1) return PDICMainAppOptions.nightUseFontColor();
				else if(bUsing!=0) PDICMainAppOptions.nightUseFontColor(bUsing==3);
				break;
		}
		return true;
	}
}
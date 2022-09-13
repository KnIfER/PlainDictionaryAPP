package com.knziha.plod.settings;

import android.os.Build;
import android.os.Bundle;

import androidx.preference.Preference;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

public class NightMode extends PlainSettingsFragment {
	public final static int id=R.xml.pref_nightmode;
	public final static int requestCode=id&0xFFFF;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = id;
		super.onCreate(savedInstanceState);
		init_switch_preference(this, "sys", PDICMainAppOptions.darkSystem(), null, null)
				.setEnabled(Build.VERSION.SDK_INT>=29);
	}
	
	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "sys":
				PDICMainAppOptions.darkSystem((Boolean) newValue);
				// CMN.AppColorChangedFlag|=1<< MainActivityUIBase.ActType.PlainDict.ordinal();
			break;
		}
		return true;
	}
}
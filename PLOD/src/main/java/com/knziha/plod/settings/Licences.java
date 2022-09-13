package com.knziha.plod.settings;

import android.os.Bundle;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.R;

public class Licences extends PlainSettingsFragment {
	public final static int id=6;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.pref_licence;
		super.onCreate(savedInstanceState);
		resId=R.string.licence;
	}
}
package com.knziha.plod.settings;

import android.os.Bundle;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.R;

public class Licences extends SettingsFragmentBase {
	public final static int id=6;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resId=R.string.licence;
	}

	//创建
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.gplpreferences);
	}
}
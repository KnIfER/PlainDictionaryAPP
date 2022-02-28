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

public class ViewSpecification_exit_dialog extends ViewSpecification{
	public final static int id=13;
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		findPreference("cat_1").setVisible(false);
	}
}
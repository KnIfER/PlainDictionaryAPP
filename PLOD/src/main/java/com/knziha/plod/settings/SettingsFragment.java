package com.knziha.plod.settings;

import android.view.View;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.PlainDict.R;

public class SettingsFragment extends SettingsFragmentBase {
	@Override
	public void onClick(View view) {
		if(view.getId()== R.id.home){
			SettingsActivity a = (SettingsActivity) getActivity();
			a.checkBack();
			a.finish();
		}
	}
}

package com.knziha.plod.settings;

import android.view.View;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.R;

public class SettingsFragment extends SettingsFragmentBase {
	@Override
	public void onClick(View view) {
		if(view.getId()== R.id.home && getActivity() instanceof SettingsActivity){
			SettingsActivity a = (SettingsActivity) getActivity();
			a.checkBack();
			a.finish();
		}
	}
}

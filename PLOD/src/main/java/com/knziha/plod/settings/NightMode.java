package com.knziha.plod.settings;

import android.content.SharedPreferences;
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
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;

public class NightMode extends PlainSettingsFragment implements Preference.OnPreferenceClickListener {
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
							init_switch_preference(this, key, PDICMainAppOptions.darkSystem(), null, null, p)
									.setEnabled(Build.VERSION.SDK_INT>=29);
						break;
						case "revert":
							p.setOnPreferenceClickListener(this);
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
			case "dkTR":
				if(bUsing==1) return PDICMainAppOptions.nightImgUseInvertFilter();
				else if(bUsing!=0) PDICMainAppOptions.nightImgUseInvertFilter(bUsing==3);
				break;
			case "dkTD":
				if(bUsing==1) return PDICMainAppOptions.nightDimImg();
				else if(bUsing!=0) PDICMainAppOptions.nightDimImg(bUsing==3);
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
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if ("revert".equals(preference.getKey())) {
			PDICMainAppOptions.darkModeJsVer++;
			PDICMainAppOptions.nightUseInvertFilter(true);
			PDICMainAppOptions.nightDimAll(false);
			PDICMainAppOptions.nightImgUseInvertFilter(true);
			PDICMainAppOptions.nightDimImg(true);
			PDICMainAppOptions.nightUsePageColor(false);
			PDICMainAppOptions.nightUseFontColor(false);
			SharedPreferences.Editor ed = getSettingActivity().opt.tmpEdit();
			ed.remove("dkR").remove("dkTR")
					.remove("dkD").remove("dkTD")
					.remove("dkB")
					.remove("dkF")
			;
			if (ViewUtils.isKindleDark()) {
				ed.putInt("dkB", 0xFF333333);
				PDICMainAppOptions.nightUsePageColor(true);
			}
			getSettingActivity().showT("已重置");
			return true;
		}
		return false;
	}
}
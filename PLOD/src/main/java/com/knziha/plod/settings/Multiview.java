package com.knziha.plod.settings;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

public class Multiview extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id=14;
	public final static int requestCode=11;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = R.xml.pref_multiview;
		super.onCreate(savedInstanceState);
		
		init_switch_preference(this, "expand_ao", PDICMainAppOptions.getEnsureAtLeatOneExpandedPage(), null, null);
		findPreference("expand_top").setOnPreferenceChangeListener(this);
		init_switch_preference(this, "scranima", PDICMainAppOptions.getScrollAnimation(), null, null);
		init_switch_preference(this, "scrautex", PDICMainAppOptions.getScrollAutoExpand(), null, null);
		init_switch_preference(this, "turbo_top", PDICMainAppOptions.getDelaySecondPageLoading(), null, null);
		
		init_switch_preference(this, "merge", PDICMainAppOptions.getUseMergedUrl(), null, null);
		findPreference("merge_min").setOnPreferenceChangeListener(this);
		enableCat1();
		
		init_switch_preference(this, "tseyhu", PDICMainAppOptions.remMultiview(), null, null);
		init_switch_preference(this, "1s", PDICMainAppOptions.getLv2JointOneAsSingle(), null, null);
		init_switch_preference(this, "share", PDICMainAppOptions.getUseSharedFrame(), null, null);
		init_switch_preference(this, "exempt", PDICMainAppOptions.getMergeExemptWebx(), null, null);
	
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
				if(TextUtils.equals(key, "expand_top")) {
					if(b1) return PDICMainAppOptions.getOnlyExpandTopPage();
					PDICMainAppOptions.setOnlyExpandTopPage(str.length()==3);
					return true;
				}
				else if(TextUtils.equals(key, "merge_min")) {
					if(b1) return PDICMainAppOptions.mergeUrlMore();
					PDICMainAppOptions.mergeUrlMore(str.length()==3);
					enableCat1();
					return true;
				}
			}
		}
		switch (preference.getKey()){
			case "expand_ao":
				PDICMainAppOptions.setEnsureAtLeatOneExpandedPage((Boolean) newValue);
			break;
			case "scranima":
				PDICMainAppOptions.setScrollAnimation((Boolean) newValue);
			break;
			case "scrautex":
				PDICMainAppOptions.setScrollAutoExpand((Boolean) newValue);
			break;
			case "turbo_top":
				PDICMainAppOptions.setDelaySecondPageLoading((Boolean) newValue);
			break;
			case "merge":
				PDICMainAppOptions.setUseMergedUrl((Boolean) newValue);
				enableCat1();
			break;
			case "tseyhu":
				PDICMainAppOptions.remMultiview((Boolean) newValue);
			break;
			case "1s":
				PDICMainAppOptions.setLv2JointOneAsSingle((Boolean) newValue);
			break;
			case "share":
				PDICMainAppOptions.setUseSharedFrame((Boolean) newValue);
			break;
			case "exempt":
				PDICMainAppOptions.setMergeExemptWebx((Boolean) newValue);
			break;
		}
		return true;
	}
	
	private void enableCat1() {
		findPreference("cat_1").setEnabled(!PDICMainAppOptions.getUseMergedUrl()||PDICMainAppOptions.mergeUrlMore());
	}
}
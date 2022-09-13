package com.knziha.plod.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.filepicker.settings.TwinkleSwitchPreference;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MdictServer;
import com.knziha.plod.plaindict.MdictServerMobile;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;

public class Multiview extends PlainSettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=R.xml.pref_multiview;
	public final static int requestCode=id&0xFFFF;
	private int multiMode;
	
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPreferenceId = id;
		super.onCreate(savedInstanceState);
		
		multiMode = PDICMainAppOptions.multiViewMode();
		
		init_switch_preference(this, "expand_ao", PDICMainAppOptions.getEnsureAtLeatOneExpandedPage(), null, null);
		findPreference("expand_top").setOnPreferenceChangeListener(this);
		init_switch_preference(this, "scranima", PDICMainAppOptions.getScrollAnimation(), null, null);
		init_switch_preference(this, "scrautex", PDICMainAppOptions.getScrollAutoExpand(), null, null).setVisible(false);
		init_switch_preference(this, "turbo_top", PDICMainAppOptions.getDelaySecondPageLoading(), null, null);
		init_switch_preference(this, "neoS", PDICMainAppOptions.popViewEntryMulti(), null, null);
		
		init_switch_preference(this, "merge", multiMode==1, null, null);
		findPreference("merge_min").setOnPreferenceChangeListener(this);
		enableCat1();
		
		init_switch_preference(this, "tseyhu", PDICMainAppOptions.remMultiview(), null, null);
		init_switch_preference(this, "1s", PDICMainAppOptions.getLv2JointOneAsSingle(), null, null);
		init_switch_preference(this, "share", PDICMainAppOptions.getUseSharedFrame(), null, null);
		init_switch_preference(this, "exempt", PDICMainAppOptions.getMergeExemptWebx(), null, null);
		init_switch_preference(this, "neo", PDICMainAppOptions.popViewEntry(), null, null);
		
		init_switch_preference(this, "debug", PDICMainAppOptions.debug(), null, null)
				.setVisible(BuildConfig.isDebug);
		
		init_switch_preference(this, "neo1", PDICMainAppOptions.popViewEntryOne(), null, null);
		
		init_switch_preference(this, "tz", PDICMainAppOptions.tapZoomGlobal(), null, null);
		findPreference("tzby").setOnPreferenceChangeListener(this);
		findPreference("tzlv").setOnPreferenceChangeListener(this);
		findPreference("tz_x").setOnPreferenceChangeListener(this);
		findPreference("dtm").setOnPreferenceChangeListener(this);
		
		init_switch_preference(this, "turn1", PDICMainAppOptions.slidePage1D(), null, null);
		init_switch_preference(this, "turn2", PDICMainAppOptions.slidePageMD(), null, null);
		init_switch_preference(this, "turn3", PDICMainAppOptions.slidePageMd(), null, null);
		init_switch_preference(this, "turnF", PDICMainAppOptions.slidePageFd(), null, null);
		
		init_switch_preference(this, "seek", PDICMainAppOptions.showEntrySeekbar(), null, null);
		init_switch_preference(this, "seekF", PDICMainAppOptions.showEntrySeekbarFolding(), null, null);
		
		init_switch_preference(this, "tools", PDICMainAppOptions.wvShowToolsBtn(), null, null);
		init_switch_preference(this, "boosT", PDICMainAppOptions.toolsBoost(), null, null);
		
		init_number_info_preference(this, "toolsBtnLong", PDICMainAppOptions.toolsQuickLong(), 0, null);
		
		clrAccent = ColorUtils.blendARGB(0xff2b4381, Color.GRAY, 0.35f);
		int where = getActivity().getIntent().getIntExtra("where", 0);
		((PreferenceGroup) findPreference("cat"+where)).drawSideLine = true;
		if(where==2)
			((PreferenceGroup) findPreference("cat"+-1)).drawSideLine = true;
		
		
		init_switch_preference(this, "fold", multiMode==2, null, null);
		findPreference("merge_min").setOnPreferenceChangeListener(this);
		
		findPreference("GPP").setOnPreferenceChangeListener(this);
		
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
				else if(TextUtils.equals(key, "GPP")) {
					if(b1) return PDICMainAppOptions.padBottom();
					PDICMainAppOptions.padBottom(str.length()==3);
					return true;
				}
			}
		}
		//CMN.Log("onPreferenceChange::", key, newValue);
		switch (key){
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
				PDICMainAppOptions.multiViewMode((Boolean) newValue?1:multiMode);
				enableCat1();
				TwinkleSwitchPreference fold = findPreference("fold");
				fold.setChecked(PDICMainAppOptions.multiViewMode()==2);
				if(!((Boolean) newValue) && !fold.isChecked()) PDICMainAppOptions.multiViewMode(multiMode = 0);
			break;
			case "fold":
				PDICMainAppOptions.multiViewMode((Boolean) newValue?2:multiMode);
				TwinkleSwitchPreference merge = findPreference("merge");
				merge.setChecked(PDICMainAppOptions.multiViewMode()==1);
				if(!((Boolean) newValue) && !merge.isChecked()) PDICMainAppOptions.multiViewMode(multiMode = 0);
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
			case "neo":
				PDICMainAppOptions.popViewEntry((Boolean) newValue);
			break;
			case "neo1":
				PDICMainAppOptions.popViewEntryOne((Boolean) newValue);
			break;
			case "neoS":
				PDICMainAppOptions.popViewEntryMulti((Boolean) newValue);
			break;
			case "debug":
				PDICMainAppOptions.debug((Boolean) newValue);
				if (MdictServer.hasRemoteDebugServer = (Boolean) newValue) {
					WebView.setWebContentsDebuggingEnabled(true);
					MdictServerMobile.getRemoteServerRes("/李白全集.0.txt", true);
				}
			break;
			case "tz":
				PDICMainAppOptions.tapZoomGlobal((Boolean) newValue);
			break;
			case "tzby":
				SearchUI.pBc.tapAlignment(IU.parsint((String) newValue, 0));
			break;
			case "tzlv":
				SearchUI.pBc.tapZoomRatio = IU.parseFloat(String.valueOf(newValue), 2);
			case "tz_x":
				SearchUI.pBc.tapZoomXOffset = IU.parseFloat(String.valueOf(newValue), 0);
			case "dtm":
				SearchUI.tapZoomWait = IU.parsint((String) newValue, 100);
			break;
			case "turn1":
				PDICMainAppOptions.slidePage1D((Boolean) newValue);
			break;
			case "turn2":
				PDICMainAppOptions.slidePageMD((Boolean) newValue);
			break;
			case "turn3":
				PDICMainAppOptions.slidePageMd((Boolean) newValue);
			break;
			case "turnF":
				PDICMainAppOptions.slidePageFd((Boolean) newValue);
			break;
			case "seek":
				PDICMainAppOptions.showEntrySeekbar((Boolean) newValue);
				SearchUI.btmV++;
			break;
			case "seekF":
				PDICMainAppOptions.showEntrySeekbarFolding((Boolean) newValue);
				SearchUI.btmV++;
			break;
			case "tools":
				PDICMainAppOptions.wvShowToolsBtn((Boolean) newValue);
			break;
			case "boosT":
				PDICMainAppOptions.toolsBoost((Boolean) newValue);
			break;
			case "toolsBtnLong":
				PDICMainAppOptions.toolsQuickLong(IU.parsint(newValue, 0));
			break;
			case "GPP": {
				try {
					String text = ((String) newValue).trim();
					int ed = text.length();
					if (text.endsWith("%")) {
						ed--;
					} else if (text.endsWith("px")) {
						ed -= 2;
					} else {
						throw new RuntimeException();
					}
					Float.parseFloat(text.substring(0, ed));
					CMN.GlobalPagePadding = text;
					return true;
				} catch (RuntimeException e) {
					((Toastable_Activity) getActivity()).showT("数值不正确！");
					return false;
				}
			}
		}
		if (key.startsWith("tz") || key.startsWith("turn")) {
			SearchUI.tapZoomV++;
			if (key.startsWith("turn") && (Boolean) newValue && !PDICMainAppOptions.getTurnPageEnabled()) {
				// todo...
			}
		}
		return true;
	}
	
	private void enableCat1() {
		findPreference("cat0").setEnabled(0==PDICMainAppOptions.multiViewMode()||PDICMainAppOptions.mergeUrlMore());
	}
}
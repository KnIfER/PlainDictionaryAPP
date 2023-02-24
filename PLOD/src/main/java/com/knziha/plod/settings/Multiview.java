package com.knziha.plod.settings;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

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

import java.util.ArrayList;

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
		int where = getActivity().getIntent().getIntExtra("where", 0);
		String whStr = "cat"+where;
		String whStr1 = where==2?"cat"+-1:null;
		
		ArrayList<Preference> preferences = new ArrayList<>(64);
		PreferenceScreen screen = mPreferenceManager.mPreferenceScreen;
		if(screen!=null) {
			preferences.add(screen);
			for (int i = 0; i < preferences.size(); i++) {
				Preference p = preferences.get(i);
				String key = p.getKey();
				if (p instanceof PreferenceGroup) {
					PreferenceGroup g = (PreferenceGroup)p;
					preferences.addAll(g.getPreferences());
					if (key != null) {
						g.drawSideLine = key.equals(whStr) || key.equals(whStr1);
					}
				} else {
					switch (key) {
						case "sys":
							init_switch_preference(this, key, PDICMainAppOptions.darkSystem(), null, null, p)
									.setEnabled(Build.VERSION.SDK_INT>=29);
							break;
						case "expand_ao":
							init_switch_preference(this, "expand_ao", PDICMainAppOptions.getEnsureAtLeatOneExpandedPage(), null, null, p);
							break;
						case "scranima":
							init_switch_preference(this, "scranima", PDICMainAppOptions.getScrollAnimation(), null, null, p);
							break;
						case "scrautex":
							init_switch_preference(this, "scrautex", PDICMainAppOptions.getScrollAutoExpand(), null, null, p).setVisible(false);
							break;
						case "turbo_top":
							init_switch_preference(this, "turbo_top", PDICMainAppOptions.getDelaySecondPageLoading(), null, null, p);
							break;
						case "neoS":
							init_switch_preference(this, "neoS", PDICMainAppOptions.entryInNewWindowMulti(), null, null, p);
							break;
						case "merge":
							init_switch_preference(this, "merge", multiMode==1, null, null, p);
							break;
						case "tseyhu":
							init_switch_preference(this, "tseyhu", PDICMainAppOptions.remMultiview(), null, null, p);
							break;
						case "1s":
							init_switch_preference(this, "1s", PDICMainAppOptions.getLv2JointOneAsSingle(), null, null, p);
							break;
						case "share":
							init_switch_preference(this, "share", PDICMainAppOptions.getUseSharedFrame(), null, null, p);
							break;
						case "exempt":
							init_switch_preference(this, "exempt", PDICMainAppOptions.getMergeExemptWebx(), null, null, p);
							break;
						case "neo":
							init_switch_preference(this, "neo", PDICMainAppOptions.entryInNewWindowMerge(), null, null, p);
							break;
						case "debug":
							init_switch_preference(this, "debug", PDICMainAppOptions.debug(), null, null, p)
									.setVisible(BuildConfig.DEBUG);
							break;
						case "neo1":
							init_switch_preference(this, "neo1", PDICMainAppOptions.entryInNewWindowSingle(), null, null, p);
							break;
						case "newDef1":
							init_switch_preference(this, "newDef1", PDICMainAppOptions.tapDefInNewWindow1(), null, null, p);
							break;
						case "newDef2":
							init_switch_preference(this, "newDef2", PDICMainAppOptions.tapDefInNewWindow2(), null, null, p);
							break;
						case "newDefM":
							init_switch_preference(this, "newDefM", PDICMainAppOptions.tapDefInNewWindowMerged(), null, null, p);
							break;
						case "tz":
							init_switch_preference(this, "tz", PDICMainAppOptions.tapZoomGlobal(), null, null, p);
							break;
						case "turn1":
							init_switch_preference(this, "turn1", PDICMainAppOptions.slidePage1D(), null, null, p);
							break;
						case "turn2":
							init_switch_preference(this, "turn2", PDICMainAppOptions.slidePageMD(), null, null, p);
							break;
						case "turn3":
							init_switch_preference(this, "turn3", PDICMainAppOptions.slidePageMd(), null, null, p);
							break;
						case "turnF":
							init_switch_preference(this, "turnF", PDICMainAppOptions.slidePageFd(), null, null, p);
							break;
						case "seek":
							init_switch_preference(this, "seek", PDICMainAppOptions.showEntrySeekbar(), null, null, p);
							break;
						case "seekF":
							init_switch_preference(this, "seekF", PDICMainAppOptions.showEntrySeekbarFolding(), null, null, p);
							break;
						case "tools":
							init_switch_preference(this, "tools", PDICMainAppOptions.wvShowToolsBtn(), null, null, p);
							break;
						case "boosT":
							init_switch_preference(this, "boosT", PDICMainAppOptions.toolsBoost(), null, null, p);
							break;
						case "toolsBtnLong":
							init_number_info_preference(this, "toolsBtnLong", PDICMainAppOptions.toolsQuickLong(), 0, null, p);
							break;
						case "fold":
							init_switch_preference(this, "fold", multiMode==2, null, null, p);
							break;
						case "url1":
							init_switch_preference(this, "url1", PDICMainAppOptions.alwaysloadUrl(), null, null, p);
							break;
						case "ignoreSU":
							init_switch_preference(this, "ignoreSU", PDICMainAppOptions.ignoreSameUrlLoading(), null, null, p);
							break;
						case "moreTM":
							init_switch_preference(this, "moreTM", PDICMainAppOptions.showMoreMenuBtnForFrames(), null, null, p);
							break;
						case "turnG":
							init_switch_preference(this, "turnG", !PDICMainAppOptions.getTurnPageEnabled(), null, null, p);
							break;
						case "key":
							init_switch_preference(this, "key", PDICMainAppOptions.swipeTopShowKeyboard(), null, null, p);
							break;
					}
					p.setOnPreferenceChangeListener(this);
				}
			}
		}
		enableCat1();
		
		clrAccent = ColorUtils.blendARGB(0xff2b4381, Color.GRAY, 0.35f);
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
				switch (key) {
					case "expand_top":
						if (b1) return PDICMainAppOptions.getOnlyExpandTopPage();
						PDICMainAppOptions.setOnlyExpandTopPage(str.length() == 3);
						return true;
					case "merge_min":
						if (b1) return PDICMainAppOptions.mergeUrlMore();
						PDICMainAppOptions.mergeUrlMore(str.length() == 3);
						enableCat1();
						return true;
					case "GPP":
						if (b1) return PDICMainAppOptions.padBottom();
						PDICMainAppOptions.padBottom(str.length() == 3);
						return true;
					case "GPL":
						if (b1) return PDICMainAppOptions.padLeft();
						PDICMainAppOptions.padLeft(str.length() == 3);
						return true;
					case "GPR":
						if (b1) return PDICMainAppOptions.padRight();
						PDICMainAppOptions.padRight(str.length() == 3);
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
				PDICMainAppOptions.entryInNewWindowMerge((Boolean) newValue);
			break;
			case "neo1":
				PDICMainAppOptions.entryInNewWindowSingle((Boolean) newValue);
			break;
			case "neoS":
				PDICMainAppOptions.entryInNewWindowMulti((Boolean) newValue);
			break;
			case "newDef1":
				PDICMainAppOptions.tapDefInNewWindow1((Boolean) newValue);
				break;
			case "newDef2":
				PDICMainAppOptions.tapDefInNewWindow2((Boolean) newValue);
				break;
			case "newDefM":
				PDICMainAppOptions.tapDefInNewWindowMerged((Boolean) newValue);
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
			case "url1":
				PDICMainAppOptions.alwaysloadUrl((Boolean) newValue);
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
			case "GPP":
				return (CMN.GlobalPagePadding = parseMarginNumber(newValue))!=null;
			case "GPL":
				return (CMN.GlobalPagePaddingLeft = parseMarginNumber(newValue))!=null;
			case "GPR":
				return (CMN.GlobalPagePaddingRight = parseMarginNumber(newValue))!=null;
			
			case "ignoreSU":
				PDICMainAppOptions.ignoreSameUrlLoading((Boolean) newValue);
			break;
			case "moreTM":
				PDICMainAppOptions.showMoreMenuBtnForFrames((Boolean) newValue);
			break;
			case "turnG":
				PDICMainAppOptions.setTurnPageEnabled(!(Boolean) newValue);
				SearchUI.tapZoomV++;
			break;
			case "key":
				PDICMainAppOptions.swipeTopShowKeyboard((Boolean) newValue);
				SearchUI.tapZoomV++;
			break;
		}
		if (key.startsWith("tz") || key.startsWith("turn")) {
			SearchUI.tapZoomV++;
			if (key.startsWith("turn") && (Boolean) newValue && !PDICMainAppOptions.getTurnPageEnabled()) {
				// todo...
			}
		}
		return true;
	}
	
	private String parseMarginNumber(Object newValue) {
		try {
			final String text = ((String) newValue).trim();
			int ed = text.length();
			if (text.endsWith("%")) {
				ed--;
			} else if (text.endsWith("px")) {
				ed -= 2;
			} else {
				throw new RuntimeException();
			}
			Float.parseFloat(text.substring(0, ed));
			return text;
		} catch (RuntimeException e) {
			((Toastable_Activity) getActivity()).showT("数值不正确！");
			return null;
		}
	}
	
	private void enableCat1() {
		findPreference("cat0").setEnabled(0==PDICMainAppOptions.multiViewMode()||PDICMainAppOptions.mergeUrlMore());
	}
}
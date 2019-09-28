package com.knziha.plod.PlainDict;
import java.io.File;

import com.knziha.plod.PlainDict.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

public class PDICMainAppOptions
{
	//SharedPreferences reader;
	SharedPreferences reader2;
	SharedPreferences defaultReader;
	public PDICMainAppOptions(Context a_){
		reader2 = a_.getSharedPreferences("SizeChangablePrefs",Activity.MODE_PRIVATE);

		defaultReader = PreferenceManager.getDefaultSharedPreferences(a_);
		magicStr=a_.getResources().getString(R.string.defPlan);
	}

	String magicStr;
	//private final dict_Activity_ui_base a;
	//public boolean isCreateWebViewEachTime=false;
	public boolean isFloatVCombinedSearching=false;
	public boolean IVFVCONFIG=false;
	public String FLOATPLAN;
	public String FLOATSERM;

	public boolean FloatView_isCombinedSearching=false;

	public int globalTextZoom;

	public String lastMdlibPath;
	public String lastMdPlanName;
	public boolean auto_seach_on_switch=true;
	public String currFavoriteDBName;
	protected boolean bShouldUseExternalBrowserApp=true;

	public int getInt(String key, int i) {
		return reader2.getInt(key, i);
	}
	public Editor putter() {
		return reader2.edit();
	}
	public void putString(String key, String val) {
		reader2.edit().putString(key, val).commit();
	}
	public String getString(String key) {
		return reader2.getString(key, null);
	}
	/*
		public void putString(String key, String val) {
			reader.edit().putString(key, val).commit();
		}
		public String getString(String key) {
			return reader.getString(key, null);
		}
		public void putBoolean(String key, boolean val) {
			reader.edit().putBoolean(key, val).commit();
		}
		public boolean getBoolean(String key) {
			return reader.getBoolean(key, false);
		}
		public boolean getBoolean(String key,boolean val) {
			return reader.getBoolean(key, val);
		}
		public int getInt(String key, int i) {
			return reader.getInt(key, i);
		}
		public Editor putter() {
			return reader.edit();
		}*/
	public Editor defaultputter() {
		return defaultReader.edit();
	}


	public String getLastMdlibPath() {
		return lastMdlibPath=defaultReader.getString("lastMdlibPath",null);
	}

	public void setLastMdlibPath(String lastMdlibPath) {
		defaultReader.edit().putString("lastMdlibPath",lastMdlibPath).commit();
	}
	public String getCurrFavoriteDBName() {//currFavoriteDBName
		return currFavoriteDBName=defaultReader.getString("CFDBN",null);
	}

	public void putCurrFavoriteDBName(String name) {
		defaultReader.edit().putString("CFDBN",currFavoriteDBName=name).commit();
	}

	public String getLastMdFn() {
		return defaultReader.getString("LastMdFn",null);
	}

	public void putLastMd(String name) {
		defaultReader.edit().putString("LastMdFn", name).commit();
	}
	public void putLastPlanName(String name) {
		defaultReader.edit().putString("LastPlanName", lastMdPlanName=name).commit();
	}
	public String getLastPlanName() {
		return lastMdPlanName=defaultReader.getString("LastPlanName",magicStr);
	}




	public int getGlobalPageBackground() {
		return defaultReader.getInt("GPBC",0xFFFFFFFF);//0xFFC7EDCC
	}
	public void putGlobalPageBackground(int val) {
		defaultReader.edit().putInt("GPBC",val).commit();
	}
	public int getMainBackground() {
		return defaultReader.getInt("BCM",0xFF8f8f8f);
	}
	public int getFloatBackground() {
		return defaultReader.getInt("BCF",0xFF8f8f8f);
	}
	public boolean getClassicalKeycaseStrategy() {
		return defaultReader.getBoolean("GKCS",false);
	}


	public boolean UseTripleClick() {
		return false;
	}



	public int getBottombarSize(int def) {
		return defaultReader.getInt("BBS", def);
	}
	public int getFloatBottombarSize(int def) {
		return defaultReader.getInt("FBBS", def);
	}
	public int getPeruseBottombarSize(int def) {
		return defaultReader.getInt("PBBS", def);
	}



	//////////////

	//public boolean isShowDirectSearch() {
	//	return defaultReader.getBoolean("sh_dir_sear", false);
	//}

	public int getDefaultFontScale(String def) {
		return Integer.valueOf(defaultReader.getString("def_fontscale", def));
	}
	public void putDefaultFontScale(String def) {
		defaultReader.edit().putString("def_fontscale", def).commit();
	}



	//////////First Boolean Flag//////////
	// ||||||||    ||||||||	    |FVDOCKED|PBOB|Ficb|FBOB|ForceSearch|showFSroll|showBD|showBA	|ToD|ToR|ToL|InPeruseTM|InPeruse|SlideTURNP|BOB|icb
	//                              0        1     0    1      0             0         0     0            0 1 0          0   1         1   0   0
	//||||||||    ||||||||CBE
	//
	private static Long FirstFlag=null;
	public long getFirstFlag() {
		if(FirstFlag==null) {
			return FirstFlag=defaultReader.getLong("MFF",98380);//76+32768+65536
		}
		return FirstFlag;
	}
	private void putFirstFlag(long val) {
		defaultReader.edit().putLong("MFF",FirstFlag=val).commit();
	}

	public void putFlags() {
		defaultReader.edit().putLong("MFF",FirstFlag).putLong("MSF",SecondFlag).commit();
	}

	public void putFirstFlag() {
		putFirstFlag(FirstFlag);
	}
	public Long FirstFlag() {
		return FirstFlag;
	}
	private void updateFFAt(int o, boolean val) {
		FirstFlag &= (~o);
		if(val) FirstFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}
	private void updateFFAt(long o, boolean val) {
		FirstFlag &= (~o);
		if(val) FirstFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}

	public boolean isCombinedSearching() {//false
		return (FirstFlag & 1) == 1;
	}
	public boolean setCombinedSearching(boolean val) {
		updateFFAt(1,val);
		return val;
	}
	public boolean getBottombarOnBottom() {//false
		return (FirstFlag & 2) == 2;
	}
	public boolean setBottombarOnBottom(boolean val) {
		updateFFAt(2,val);
		return val;
	}
	public boolean getTurnPageEnabled() {//true
		return (FirstFlag & 4) == 4;
	}
	public boolean setTurnPageEnabled(boolean val) {
		updateFFAt(4,val);
		return val;
	}


	//
	public boolean getInPeruseMode() {//true
		return (FirstFlag & 8) == 8;
	}
	public boolean setInPeruseMode(boolean val) {
		updateFFAt(8,val);
		return val;
	}
	public boolean getInPeruseModeTM() {//false
		return (FirstFlag & 16) == 16;
	}
	public boolean setInPeruseModeTM(boolean val) {
		updateFFAt(16,val);
		return val;
	}

	public boolean getPerUseToL() {//true
		return (FirstFlag & 32) == 32;
	}
	public boolean setPerUseToL(boolean val) {
		updateFFAt(32,val);
		return val;
	}
	public boolean getPerUseToR() {//true
		return (FirstFlag & 64) == 64;
	}
	public boolean setPerUseToR(boolean val) {
		updateFFAt(64,val);
		return val;
	}
	public boolean getPerUseToD() {//false
		return (FirstFlag & 128) == 128;
	}
	public boolean setPerUseToD(boolean val) {
		updateFFAt(128,val);
		return val;
	}

	public boolean getShowBA() {//false
		return (FirstFlag & 0x100) == 0x100;//256
	}
	public boolean setShowBA(boolean val) {
		updateFFAt(0x100,val);
		return val;
	}
	public boolean getShowBD() {//false
		return (FirstFlag & 0x200) == 0x200;
	}
	public boolean setShowBD(boolean val) {
		updateFFAt(0x200,val);
		return val;
	}
	public boolean getShowFScroll() {//false
		return (FirstFlag & 0x400) == 0x400;
	}
	public boolean setShowFScroll(boolean val) {
		updateFFAt(0x400,val);
		return val;
	}
	public boolean getForceSearch() {//false
		return (FirstFlag & 0x800) == 0x800;
	}
	public boolean setForceSearch(boolean val) {
		updateFFAt(0x800,val);
		return val;
	}

	public boolean getFloatBottombarOnBottom() {//false
		return (FirstFlag & 0x1000) == 0x1000;
	}
	public boolean setFloatBottombarOnBottom(boolean val) {
		updateFFAt(0x1000,val);
		return val;
	}
	public boolean isFloatCombinedSearching() {//false
		return (FirstFlag & 0x2000) == 0x2000;
	}
	public boolean setFloatCombinedSearching(boolean val) {
		updateFFAt(0x2000,val);
		return val;
	}

	public boolean getPeruseBottombarOnBottom() {//true
		return (FirstFlag & 0x4000) == 0x4000;
	}
	public boolean setPeruseBottombarOnBottom(boolean val) {
		updateFFAt(0x4000,val);
		return val;
	}
	public boolean getFVDocked() {//true
		return (FirstFlag & 0x8000) == 0x8000;
	}
	public boolean setFVDocked(boolean val) {
		updateFFAt(0x8000,val);
		return val;
	}

	//|pinDialog2|pinDialog|tintWRes|hintComRes|hintSMode|PZSlide|ZSlide|FremPageCom    |FremPageSin|PremPage|remPageCom|remPageSin|Dark|VPager|CBE|isFullScreen
	//

	public boolean isFullScreen() {
		return (FirstFlag & 0x10000) == 0x10000;
	}
	public boolean setFullScreen(boolean val) {
		updateFFAt(0x10000,val);
		return val;
	}
	public boolean isContentBow() {
		return (FirstFlag & 0x20000) == 0x20000;
	}
	public boolean setContentBow(boolean val) {
		updateFFAt(0x20000,val);
		return val;
	}
	public boolean isViewPagerEnabled() {
		return (FirstFlag & 0x40000) == 0x40000;
	}
	public boolean setViewPagerEnabled(boolean val) {
		updateFFAt(0x40000,val);//
		return val;
	}

	public boolean getInDarkMode() {
		return (FirstFlag & 0x80000) == 0x80000;
	}
	public boolean setInDarkMode(boolean val) {
		updateFFAt(0x80000,val);//0x‭80000
		return val;
	}

	public boolean getFanYeQianJiYiWeiZhi_1() {
		return (FirstFlag & 0x100000) == 0x100000;
	}
	public boolean setFanYeQianJiYiWeiZhi_1(boolean val) {
		updateFFAt(0x100000,val);
		return val;
	}

	public boolean getFanYeQianJiYiWeiZhi_1_1() {
		return (FirstFlag & 0x200000) == 0x200000;
	}
	public boolean setFanYeQianJiYiWeiZhi_1_1(boolean val) {
		updateFFAt(0x200000,val);
		return val;
	}

	public boolean getFanYeQianJiYiWeiZhi_P() {
		return (FirstFlag & 0x400000) == 0x400000;
	}
	public boolean setFanYeQianJiYiWeiZhi_P(boolean val) {
		updateFFAt(0x400000,val);
		return val;
	}

	public boolean getFanYeQianJiYiWeiZhi_2() {
		return (FirstFlag & 0x800000) == 0x800000;
	}
	public boolean setFanYeQianJiYiWeiZhi_2(boolean val) {
		updateFFAt(0x800000,val);
		return val;
	}

	////
	public boolean getFanYeQianJiYiWeiZhi_2_2() {
		return (FirstFlag & 0x1000000) == 0x1000000;
	}
	public boolean setFanYeQianJiYiWeiZhi_2_2(boolean val) {
		updateFFAt(0x1000000,val);
		return val;
	}


	public boolean getZoomedInCanSlideTurnPage() {
		return (FirstFlag & 0x2000000) == 0x2000000;
	}
	public boolean setZoomedInCanSlideTurnPage(boolean val) {
		updateFFAt(0x2000000,val);//0x‭80000
		return val;
	}

	public boolean getPeruseZoomedInCanSlideTurnPage() {
		return (FirstFlag & 0x4000000) == 0x4000000;
	}
	public boolean setPeruseZoomedInCanSlideTurnPage(boolean val) {
		updateFFAt(0x4000000,val);
		return val;
	}

	public boolean getHintSearchMode() {
		return (FirstFlag & 0x8000000) == 0x8000000;
	}
	public boolean setHintSearchMode(boolean val) {
		updateFFAt(0x8000000,val);
		return val;
	}
	public boolean toggleHintSearchMode() {
		return setHintSearchMode(!getHintSearchMode());
	}


	public boolean getNotifyComboRes() {
		return (FirstFlag & 0x10000000) == 0x10000000;
	}
	public boolean setNotifyComboRes(boolean val) {
		updateFFAt(0x10000000,val);
		return val;
	}
	public boolean toggleNotifyComboRes() {
		return setNotifyComboRes(!getNotifyComboRes());
	}

	public boolean getTintWildRes() {
		return (FirstFlag & 0x20000000) == 0x20000000;
	}
	public boolean setTintWildRes(boolean val) {
		updateFFAt(0x20000000,val);
		return val;
	}
	public boolean toggleTintWildRes() {
		return setTintWildRes(!getTintWildRes());
	}

	public boolean getPinDialog() {
		return (FirstFlag & 0x40000000) == 0x40000000;
	}
	public boolean setPinDialog(boolean val) {
		updateFFAt(0x40000000,val);
		return val;
	}

	public boolean getPinDialog_1() {//from editor
		return (FirstFlag & 0x80000000) == 0x80000000;
	}
	public boolean setPinDialog_1(boolean val) {
		updateFFAt(0x80000000,val);
		return val;
	}
	/////////////////////End First 32-bit Flag////////////////////////////////////

	/////////////////////Start First Flag Long field///////////////////////////////////
	public boolean getPinPicDictDialog() {
		return (FirstFlag & 0x100000000l) == 0x100000000l;
	}
	public boolean setPinPicDictDialog(boolean val) {
		updateFFAt(0x100000000l,val);
		return val;
	}
	public boolean getPicDictAutoSer() {
		return (FirstFlag & 0x200000000l) == 0x200000000l;
	}
	public boolean setPicDictAutoSer(boolean val) {
		updateFFAt(0x200000000l,val);
		return val;
	}
	public boolean getRemPos() {
		return (FirstFlag & 0x400000000l) == 0x400000000l;
	}
	public boolean setRemPos(boolean val) {
		updateFFAt(0x400000000l,val);
		return val;
	}
	public boolean getRemPos2() {
		return (FirstFlag & 0x800000000l) == 0x800000000l;
	}
	public boolean setRemPos2(boolean val) {
		updateFFAt(0x800000000l,val);
		return val;
	}

	public int getDictManagerTap() {
		return (int) ((FirstFlag >> 36) & 3);
	}
	public int setDictManagerTap(int val) {
		//updateFFAt(0x1000000000l,val);
		FirstFlag &= (~0x1000000000l);
		FirstFlag &= (~0x2000000000l);
		FirstFlag |= ((long)(val & 3)) << 36;
		return val;
	}

	public int getDBMode() {
		return (int) ((FirstFlag >> 38) & 7);
	}
	public int setDBMode(int val) {
		//updateFFAt(0x1000000000l,val);
		FirstFlag &= (~0x4000000000l);
		FirstFlag &= (~0x8000000000l);
		FirstFlag &= (~0x10000000000l);
		FirstFlag |= ((long)(val & 7)) << 38;
		return val;
	}
	public boolean getInRemoveMode() {
		return (FirstFlag & 0x20000000000l) == 0x20000000000l;
	}
	public boolean toggleInRemoveMode() {
		return setInRemoveMode(!getInRemoveMode());
	}
	public boolean setInRemoveMode(boolean val) {
		updateFFAt(0x20000000000l,val);
		return val;
	}
	public boolean getIsCombinedSearching() {
		return (FirstFlag & 0x40000000000l) == 0x40000000000l;
	}
	public boolean setIsCombinedSearching(boolean val) {
		updateFFAt(0x40000000000l,val);
		return val;
	}
	public boolean getToolBarShown1() {
		return (FirstFlag & 0x80000000000l) == 0x80000000000l;
	}
	public boolean setToolBarShown1(boolean val) {
		updateFFAt(0x80000000000l,val);
		return val;
	}
	public boolean getToolBarShown2() {
		return (FirstFlag & 0x100000000000l) == 0x100000000000l;
	}
	public boolean setToolBarShown2(boolean val) {
		updateFFAt(0x100000000000l,val);
		return val;
	}

	public boolean getBrowser_selection_alwaysVisible() {
		return (FirstFlag & 0x200000000000l) == 0x200000000000l;
	}
	public boolean setBrowser_selection_alwaysVisible(boolean val) {
		updateFFAt(0x200000000000l,val);
		return val;
	}
	public boolean get_use_volumeBtn() {
		return (FirstFlag & 0x400000000000l) == 0x400000000000l;
	}
	public boolean set_use_volumeBtn(boolean val) {
		updateFFAt(0x400000000000l,val);
		return val;
	}
	public boolean getSelection_Persists() {
		return (FirstFlag & 0x800000000000l) == 0x800000000000l;
	}
	public boolean setSelection_Persists(boolean val) {
		updateFFAt(0x800000000000l,val);
		return val;
	}
	public boolean getBrowser_AffectInstant() {
		return (FirstFlag & 0x1000000000000l) == 0x1000000000000l;
	}

	public boolean setBrowser_AffectInstant(boolean val) {
		updateFFAt(0x1000000000000l,val);
		return val;
	}
	public boolean getShelfStrictScroll() {
		return (FirstFlag & 0x2000000000000l) == 0x2000000000000l;
	}

	public boolean setShelfStrictScroll(boolean val) {
		updateFFAt(0x2000000000000l,val);
		return val;
	}
	public boolean getScrollShown() {
		return (FirstFlag & 0x4000000000000l) == 0x4000000000000l;
	}

	public boolean setScrollShown(boolean val) {
		updateFFAt(0x4000000000000l,val);
		return val;
	}




	/////////////////////End First Flag////////////////////////////////////
	/////////////////////Start Second Flag////////////////////////////////////
	private static Long SecondFlag=null;
	public long getSecondFlag() {
		if(SecondFlag==null) {
			return SecondFlag=defaultReader.getLong("MSF",98380);//76+32768+65536
		}
		return SecondFlag;
	}
	private void putSecondFlag(long val) {
		defaultReader.edit().putLong("MSF",SecondFlag=val).commit();
	}
	public void putSecondFlag() {
		putFirstFlag(SecondFlag);
	}
	public Long SecondFlag() {
		return SecondFlag;
	}
	private void updateSFAt(int o, boolean val) {
		SecondFlag &= (~o);
		if(val) SecondFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}
	private void updateSFAt(long o, boolean val) {
		SecondFlag &= (~o);
		if(val) SecondFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}


	public boolean getInheritePageScale() {
		return (SecondFlag & 0x1) == 0x1;
	}

	public boolean setInheritePageScale(boolean val) {
		updateSFAt(0x1,val);
		return val;
	}
	public int getNavigationBtnType() {
		return (int) ((SecondFlag >> 1) & 3);
	}

	public int setNavigationBtnType(int val) {
		SecondFlag &= (~0x2l);
		SecondFlag &= (~0x4l);
		SecondFlag |= (val & 3) << 1;
		return val;
	}

	public boolean getHideScroll1() {
		return (SecondFlag & 0x8) == 0x8;
	}

	public boolean setHideScroll1(boolean val) {
		updateSFAt(0x8,val);
		return val;
	}
	public boolean getHideScroll2() {
		return (SecondFlag & 0x10) == 0x10;
	}

	public boolean setHideScroll2(boolean val) {
		updateSFAt(0x10,val);
		return val;
	}
	public boolean getHideScroll3() {
		return (SecondFlag & 0x20) == 0x20;
	}

	public boolean setHideScroll3(boolean val) {
		updateSFAt(0x20,val);
		return val;
	}

	public boolean getPageTurn1() {
		return (SecondFlag & 0x40) == 0x40;
	}

	public boolean setPageTurn1(boolean val) {
		updateSFAt(0x40,val);
		return val;
	}
	public boolean getPageTurn2() {
		return (SecondFlag & 0x80) == 0x80;
	}

	public boolean setPageTurn2(boolean val) {
		updateSFAt(0x80,val);
		return val;
	}
	public boolean getPageTurn3() {
		return (SecondFlag & 0x100) == 0x100;
	}

	public boolean setPageTurn3(boolean val) {
		updateSFAt(0x100,val);
		return val;
	}

	public boolean getHistoryStrategy0() {
		return (SecondFlag & 0x200) == 0x200;
	}

	public boolean setHistoryStrategy0(boolean val) {
		updateSFAt(0x200,val);
		return val;
	}
	public boolean getHistoryStrategy1() {
		return (SecondFlag & 0x400) == 0x400;
	}

	public boolean setHistoryStrategy1(boolean val) {
		updateSFAt(0x400,val);
		return val;
	}
	public boolean getHistoryStrategy2() {
		return (SecondFlag & 0x800) == 0x800;
	}

	public boolean setHistoryStrategy2(boolean val) {
		updateSFAt(0x800,val);
		return val;
	}
	public boolean getHistoryStrategy3() {
		return (SecondFlag & 0x1000) == 0x1000;
	}

	public boolean setHistoryStrategy3(boolean val) {
		updateSFAt(0x1000,val);
		return val;
	}
	public boolean getHistoryStrategy4() {
		return (SecondFlag & 0x2000) == 0x2000;
	}

	public boolean setHistoryStrategy4(boolean val) {
		updateSFAt(0x2000,val);
		return val;
	}
	public boolean XXXgetHistoryStrategy5() {
		return (SecondFlag & 0x4000) == 0x4000;
	}

	public boolean XXXsetHistoryStrategy5(boolean val) {
		updateSFAt(0x4000,val);
		return val;
	}
	public boolean getHistoryStrategy6() {
		return (SecondFlag & 0x8000) == 0x8000;
	}

	public boolean setHistoryStrategy6(boolean val) {
		updateSFAt(0x8000,val);
		return val;
	}
	public boolean getHistoryStrategy7() {
		return (SecondFlag & 0x10000) == 0x10000;
	}

	public boolean setHistoryStrategy7(boolean val) {
		updateSFAt(0x10000,val);
		return val;
	}
	public int getHistoryStrategy8() {
		return (int) ((SecondFlag >> 17) & 3);
	}

	public int setHistoryStrategy8(int val) {
		SecondFlag &= (~0x20000l);
		SecondFlag &= (~0x40000l);
		SecondFlag |= ((long)(val & 3)) << 17;
		return val;
	}



	//start crash handler settings
	public boolean getUseCustomCrashCatcher() {
		return true;//(SecondFlag & 0x80000l) == 0x80000l;
	}

	public boolean setUseCustomCrashCatcher(boolean val) {
		updateSFAt(0x80000l,val);
		return val;
	}

	public boolean getSilentExitBypassingSystem() {
		return (SecondFlag & 0x100000l) != 0x100000l;
	}

	public boolean setSilentExitBypassingSystem(boolean val) {
		updateSFAt(0x100000l,!val);
		return val;
	}
	public boolean getLogToFile() {
		return (SecondFlag & 0x200000l) != 0x200000l;
	}

	public boolean setLogToFile(boolean val) {
		updateSFAt(0x200000l,!val);
		return val;
	}
	//end crash handler settings




	private final StringBuilder pathTo = new StringBuilder();//"/sdcard/PLOD/bmDBs/");
	public String rootPath;
	protected int pathToL = -1;//pathTo.toString().length();
	public boolean isLarge;
	public DisplayMetrics dm;
	public StringBuilder pathTo() {
		if(pathToL==-1) {
			String i = "/PLOD/bmDBs/";
			if(rootPath==null) rootPath=Environment.getExternalStorageDirectory().getPath();
			pathTo.append(rootPath).append(i);
			pathToL = rootPath.length()+i.length();
		}
		pathTo.setLength(pathToL);
		return pathTo;
	}
	public StringBuilder pathToInternal() {
		return pathTo().append("INTERNAL/");
	}
	public String pathToMain() {
		return pathTo().toString().substring(0,pathToL-6);
	}



}
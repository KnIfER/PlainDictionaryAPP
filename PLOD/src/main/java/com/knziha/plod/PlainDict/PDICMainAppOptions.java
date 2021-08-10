package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.plod.PlainUI.AppUIProject;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.mdict_manageable;
import com.knziha.plod.widgets.Utils;
import com.knziha.plod.widgets.XYTouchRecorder;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class PDICMainAppOptions implements MdictServer.AppOptions
{
	public boolean isAudioPlaying;
	public boolean isAudioActuallyPlaying;
	public boolean supressAudioResourcePlaying;
	public static HashSet<String> ChangedMap;
	public File SpecificationFile;
	SharedPreferences reader2;
	SharedPreferences defaultReader;
	public static String locale;

	public PDICMainAppOptions(Context a_){
		reader2 = a_.getSharedPreferences("SizeChangablePrefs",Activity.MODE_PRIVATE);

		defaultReader = PreferenceManager.getDefaultSharedPreferences(a_);
		magicStr=a_.getResources().getString(R.string.defPlan);

		int max = 1<<7;
		byte[]  bytes = new byte[max];
		for (int i = 1; i < max; i++) {
			bytes[i] = (byte) i;
		}
		CMN.Log(max, 0x7e);

		String str = new String(bytes, 1, max-1, StandardCharsets.UTF_8);
		String store = defaultReader.getString("test", null);
		defaultReader.edit().putString("test", null).apply();
		CMN.Log("===", str.equals(store));
	}
	String magicStr;

	public File lastMdlibPath;
	public String lastMdPlanName;
	public boolean auto_seach_on_switch=true;
	protected boolean bShouldUseExternalBrowserApp=true;


	public int getInt(String key, int i) {
		return reader2.getInt(key, i);
	}
	public Editor putter() {
		return reader2.edit();
	}
	public void putString(String key, String val) {
		reader2.edit().putString(key, val).apply();
	}
	public String getString(String key) {
		return reader2.getString(key, null);
	}

	public Editor defaultputter() {
		return defaultReader.edit();
	}

	public String getLocale() {
		return locale!=null?locale:(locale=defaultReader.getString("locale",""));
	}

	public File getLastMdlibPath() {
		String path = defaultReader.getString("lastMdlibPath",null);
		return path==null?null:(lastMdlibPath=new File(path));
	}

	public void setLastMdlibPath(String lastMdlibPath) {
		defaultReader.edit().putString("lastMdlibPath",lastMdlibPath).commit();
	}
	public String getCurrFavoriteDBName() {//currFavoriteDBName
		return defaultReader.getString("DB1",null);
	}

	public void putCurrFavoriteDBName(String name) {
		defaultReader.edit().putString("DB1",name).apply();
	}

	public String getLastMdFn(String key) {
		return defaultReader.getString(key,null);
	}
	public void putLastMdFn(String key, String name) {
		defaultReader.edit().putString(key, name).apply();
	}
	
	public void putLastVSGoNumber(int position) {
		defaultReader.edit().putInt("VSGo", position).apply();
	}
	
	public int getLastVSGoNumber() {
		return defaultReader.getInt("VSGo", -1);
	}
	
	public String getLastPlanName(String key) {
		return SU.legacySetFileName(lastMdPlanName=defaultReader.getString(key,magicStr));
	}
	public void putLastPlanName(String key, String name) {
		defaultReader.edit().putString(key, lastMdPlanName=name).apply();
	}

	public String getFontLibPath() {
		return defaultReader.getString("fntlb",pathToMainFolder().append("Fonts").toString());
	}
	public void setFontLibPath(String name) {
		defaultReader.edit().putString("fntlb", name).apply();
	}

	public String getAppBottomBarProject() {
		return defaultReader.getString("btmprj",null);
	}
	
	
	public String getAppContentBarProject(int idx) {
		return getAppContentBarProject("ctnp#"+idx);
	}
	
	public String getAppContentBarProject(String key) {
		String ret = defaultReader.getString(key, null);
		if(ret!=null && ret.startsWith("ref")){
			int ref = IU.parsint(ret.substring(3));
			if(ref<0||ref>10) return null;
			int ref_tree = 1<<ref;
			return getContextbarProjectRecursive(ref, ref_tree);
		}
		return ret;
	}
	
	private String getContextbarProjectRecursive(int idx, int ref_tree) {
		String current = defaultReader.getString("ctnp#"+idx, null);
		if(current!=null && current.startsWith("ref")){
			int ref = IU.parsint(current.substring(3));
			if(ref<0||ref>10) return null;
			int ref_leaf = 1<<ref;
			if((ref_tree&ref_leaf)!=0) return null;
			ref_tree |= ref_leaf;
			return getContextbarProjectRecursive(ref, ref_tree);
		}
		return current;
	}
	
	private int getContextbarProjectRecursive1(int idx, int ref_tree) {
		String current = defaultReader.getString("ctnp#"+idx, null);
		if(current!=null && current.startsWith("ref")){
			int ref = IU.parsint(current.substring(3));
			if(ref<0||ref>10) return -1;
			int ref_leaf = 1<<ref;
			if((ref_tree&ref_leaf)!=0) return -1;
			ref_tree |= ref_leaf;
			return getContextbarProjectRecursive1(ref, ref_tree);
		}
		return idx;
	}
	
	/** 关联拷贝或数据拷贝 */
	public void linkContentbarProject(int idx, int linkTo) {
		defaultReader.edit()
				.putString("ctnp#"+idx , getLinkContentBarProj()?("ref"+linkTo):getAppContentBarProject(linkTo))
				.apply();
	}
	
	public void putAppProject(AppUIProject projectContext) {
		defaultReader.edit().putString(projectContext.key, projectContext.currentValue).apply();
	}
	
	public void clearAppProjects(String key) {
		Editor editor = defaultReader.edit();
		if(getRestoreAllBottombarProj()){
			editor.putString("btmprj", null);
			for (int i = 0; i < 3; i++) {
				editor.putString("ctnp#"+i, null);
			}
		} else {
			editor.putString(key, null);
		}
		editor.apply();
	}
	
	public boolean isAppContentBarProjectRelative(int idx) {
		String ret = defaultReader.getString("ctnp#"+idx, null);
		return ret!=null && ret.startsWith("ref");
	}
	
	public boolean isAppContentBarProjectReferTo(String key, int ref_idx) {
		String ret = defaultReader.getString(key, null);
		if(ret!=null && ret.startsWith("ref")){
			int ref = IU.parsint(ret.substring(3));
			int ref_tree = 1<<ref;
			return ref_idx == getContextbarProjectRecursive1(ref, ref_tree);
		}
		return false;
	}
	

	public int getGlobalPageBackground() {
		return defaultReader.getInt("GPBC", Color.WHITE); //0xFFC7EDCC
	}
	public void putGlobalPageBackground(int val) {
		defaultReader.edit().putInt("GPBC",val).apply();
	}
	public int getMainBackground() {
		return defaultReader.getInt("BCM",Constants.DefaultMainBG);
	}
	public int getFloatBackground() {
		return defaultReader.getInt("BCF",Constants.DefaultMainBG);
	}
	public int getToastBackground() {
		return defaultReader.getInt("TTB",0xFFBFDEF8);
	}
	public int getToastColor() {
		return defaultReader.getInt("TTT",0xFF0D2F4B);
	}
	public int getTitlebarForegroundColor() {
		return defaultReader.getInt("TIF",0xFFffffff);
	}
	public int getTitlebarBackgroundColor() {
		return defaultReader.getInt("TIB",Constants.DefaultMainBG);
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

	public int getDefaultFontScale(int def) {
		return defaultReader.getInt("f_size", def);
	}
	public void putDefaultFontScale(int def) {
		defaultReader.edit().putInt("f_size", def).apply();
	}


	/** @param CommitOrApplyOrNothing 0=nothing;1=apply;2=commit*/
	public void setFlags(Editor editor, int CommitOrApplyOrNothing) {
		if(editor==null){
			editor = defaultReader.edit();
			CommitOrApplyOrNothing=1;
		}
		editor.putLong("MFF", FirstFlag).putLong("MSF", SecondFlag).putLong("MTF", ThirdFlag)
				.putLong("MQF", FourthFlag).putLong("MVF", FifthFlag);
		if(CommitOrApplyOrNothing==1) editor.apply();
		else if(CommitOrApplyOrNothing==2) editor.commit();
		//CMN.Log("apply changes");
	}
	//////////   Tmp Flag   //////////
	private static long tmpFlag;
	private static void updateTmpAt(int o, boolean val) {
		tmpFlag &= (~o);
		if(val) tmpFlag |= o;
	}
	private void updateTmpAt(long o, boolean val) {
		tmpFlag &= (~o);
		if(val) tmpFlag |= o;
	}
	
	public static boolean getRestartVMOnExit() {
		return (tmpFlag & 0x1) == 0x1;
	}
	
	public static void setRestartVMOnExit(boolean val) {
		updateTmpAt(1,val);
	}
	
	//////////   ET   //////////
	
	//////////   First Boolean Flag   //////////
	private static Long FirstFlag=null;
	public long getFirstFlag() {
		if(FirstFlag==null) {
			return CMNF.FirstFlag=FirstFlag=defaultReader.getLong("MFF",0);
		}
		return FirstFlag;
	}
	private void putFirstFlag(long val) {
		defaultReader.edit().putLong("MFF",FirstFlag=val).apply();
	}

	public void putFlags() {
		defaultReader.edit().putLong("MFF",FirstFlag).putLong("MSF",SecondFlag)
				.putLong("MTF",ThirdFlag).putLong("MVF",FifthFlag).apply();
	}

	public void putFirstFlag() {
		putFirstFlag(FirstFlag);
	}
	public Long FirstFlag() {
		return FirstFlag;
	}
	private static void updateFFAt(int o, boolean val) {
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
	public boolean getBottombarOnBottom() {
		return (FirstFlag & 2) != 2;
	}
	public boolean setBottombarOnBottom(boolean val) {
		updateFFAt(2,!val);
		return val;
	}
	public boolean getTurnPageEnabled() {
		return (FirstFlag & 4) != 4;
	}
	public boolean setTurnPageEnabled(boolean val) {
		updateFFAt(4,!val);
		return val;
	}


	//
	public boolean getInPeruseMode() {
		return (FirstFlag & 8) != 8;
	}
	public boolean setInPeruseMode(boolean val) {
		updateFFAt(8,!val);
		return val;
	}
	public boolean getInPeruseModeTM() {
		return (FirstFlag & 16) == 16;
	}
	public boolean setInPeruseModeTM(boolean val) {
		updateFFAt(16,val);
		return val;
	}

	public boolean getPerUseToL() {
		return (FirstFlag & 32) != 32;
	}
	public boolean setPerUseToL(boolean val) {
		updateFFAt(32,!val);
		return val;
	}
	public boolean getPerUseToR() {
		return (FirstFlag & 64) != 64;
	}
	public boolean setPerUseToR(boolean val) {
		updateFFAt(64,!val);
		return val;
	}
	public boolean getPerUseToD() {
		return (FirstFlag & 128) == 128;
	}
	public boolean setPerUseToD(boolean val) {
		updateFFAt(128,val);
		return val;
	}

	public boolean getShowBA() {
		return (FirstFlag & 0x100) == 0x100;
	}
	public boolean setShowBA(boolean val) {
		updateFFAt(0x100,val);
		return val;
	}
	public boolean getShowBD() {
		return (FirstFlag & 0x200) == 0x200;
	}
	public boolean setShowBD(boolean val) {
		updateFFAt(0x200,val);
		return val;
	}
	public boolean getShowFScroll() {
		return (FirstFlag & 0x400) == 0x400;
	}
	public boolean setShowFScroll(boolean val) {
		updateFFAt(0x400,val);
		return val;
	}
	public boolean getForceSearch() {
		return (FirstFlag & 0x800) == 0x800;
	}
	public boolean setForceSearch(boolean val) {
		updateFFAt(0x800,val);
		return val;
	}

	public boolean getFloatBottombarOnBottom() {
		return (FirstFlag & 0x1000) == 0x1000;
	}
	public boolean setFloatBottombarOnBottom(boolean val) {
		updateFFAt(0x1000,val);
		return val;
	}
	public boolean isFloatCombinedSearching() {
		return (FirstFlag & 0x2000) == 0x2000;
	}
	public boolean setFloatCombinedSearching(boolean val) {
		updateFFAt(0x2000,val);
		return val;
	}

	public boolean getPeruseBottombarOnBottom() {
		return (FirstFlag & 0x4000) != 0x4000;
	}
	public boolean setPeruseBottombarOnBottom(boolean val) {
		updateFFAt(0x4000,!val);
		return val;
	}
	public boolean getFVDocked() {
		return (FirstFlag & 0x8000) != 0x8000;
	}
	public boolean setFVDocked(boolean val) {
		updateFFAt(0x8000,!val);
		return val;
	}

	public static boolean isFullScreen() {
		return (FirstFlag & 0x10000) == 0x10000;
	}
	public boolean setFullScreen(boolean val) {
		updateFFAt(0x10000,val);
		return val;
	}
	public boolean isContentBow() {
		return (FirstFlag & 0x20000) != 0x20000;
	}
	public boolean setContentBow(boolean val) {
		updateFFAt(0x20000,!val);
		return val;
	}
	public boolean isViewPagerEnabled() {
		return (FirstFlag & 0x40000) != 0x40000;
	}
	public boolean setViewPagerEnabled(boolean val) {
		updateFFAt(0x40000,!val);
		return val;
	}

	public boolean getInDarkMode() {
		boolean ret = (FirstFlag & 0x80000) == 0x80000;
		GlobalOptions.isDark |= ret;
		return ret;
	}
	public boolean setInDarkMode(boolean val) {
		GlobalOptions.isDark |= val;
		if(Utils.mRectPaint!=null) {
			Utils.mRectPaint.setColor(GlobalOptions.isDark?0x3fffffff:Utils.FloatTextBG);
		}
		updateFFAt(0x80000,val);//0x‭80000
		return val;
	}

	//
	public boolean getPeruseAddAll() {
		return (FirstFlag & 0x100000) != 0x100000;
	}
	public boolean setPeruseAddAll(boolean val) {
		updateFFAt(0x100000,!val);
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

	public static boolean isFullscreenHideNavigationbar() {
		return (FirstFlag & 0x4000000) != 0x4000000;
	}
	public static boolean setFullscreenHideNavigationbar(boolean val) {
		updateFFAt(0x4000000,val);
		return val;
	}

	public static boolean getHintSearchMode() {
		return (FirstFlag & 0x8000000) != 0x8000000;
	}
	public static boolean setHintSearchMode(boolean val) {
		updateFFAt(0x8000000,!val);
		return val;
	}

	//设置改动
	public static boolean getNotifyComboRes() {
		return (FirstFlag & 0x10000000) != 0x10000000;
	}
	public static boolean setNotifyComboRes(boolean val) {
		updateFFAt(0x10000000,!val);
		return val;
	}
	public boolean getTintWildRes() {
		return (FirstFlag & 0x20000000) != 0x20000000;
	}
	public boolean setTintWildRes(boolean val) {
		updateFFAt(0x20000000,!val);
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
		return (FirstFlag & 0x400000000l) != 0x400000000l;
	}
	public boolean setRemPos(boolean val) {
		updateFFAt(0x400000000l,!val);
		return val;
	}
	public boolean getRemPos2() {
		return (FirstFlag & 0x800000000l) == 0x800000000l;
	}
	public boolean setRemPos2(boolean val) {
		updateFFAt(0x800000000l,val);
		return val;
	}

	
	//0x3 模板
	@Deprecated
	public int getDictManagerTap() {
		return (int) ((FirstFlag >> 36) & 3);
	}
	@Deprecated
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
	public boolean getUseVolumeBtn() {
		return (FirstFlag & 0x400000000000l) == 0x400000000000l;
	}
	public boolean setUseVolumeBtn(boolean val) {
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
		return (FirstFlag & 0x2000000000000l) != 0x2000000000000l;
	}

	public boolean setShelfStrictScroll(boolean val) {
		updateFFAt(0x2000000000000l,!val);
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
	public static Long SecondFlag=null;
	public long getSecondFlag() {
		if(SecondFlag==null) {
			return FilePickerOptions.SecondFlag=SecondFlag=defaultReader.getLong("MSF",0);
		}
		return SecondFlag;
	}
	private void putSecondFlag(long val) {
		defaultReader.edit().putLong("MSF",SecondFlag=val).apply();
	}
	public void putSecondFlag() {
		putFirstFlag(SecondFlag);
	}
	public Long SecondFlag() {
		return SecondFlag;
	}
	public static void SecondFlag(long _SecondFlag) {
		SecondFlag=_SecondFlag;
	}
	private static void updateSFAt(int o, boolean val) {
		SecondFlag &= (~o);
		if(val) SecondFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}
	private static void updateSFAt(long o, boolean val) {
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
	//xxx
//	public boolean getHideScroll3() {
//		return (SecondFlag & 0x20) == 0x20;
//	}

	public boolean setHideScroll3(boolean val) {
		updateSFAt(0x20,val);
		return val;
	}

	public boolean getPageTurn1() {
		return (SecondFlag & 0x40) != 0x40;
	}

//	public boolean setPageTurn1(boolean val) {
//		updateSFAt(0x40,!val);
//		return val;
//	}
	public boolean getPageTurn2() {
		return (SecondFlag & 0x80) != 0x80;
	}

//	public boolean setPageTurn2(boolean val) {
//		updateSFAt(0x80,!val);
//		return val;
//	}

	public boolean getUseLruDiskCache() {
		return (SecondFlag & 0x100) != 0x100;
	}

	/* forbid all history recording */
	public static boolean getHistoryStrategy0() {
		return (SecondFlag & 0x200) == 0x200;
	}
	public static boolean setHistoryStrategy0(boolean val) {
		updateSFAt(0x200,val);
		return val;
	}

	/** 记录各种查询 */
	public static boolean getHistoryStrategy1() {
		return (SecondFlag & 0x400) != 0x400;
	}
	public static boolean setHistoryStrategy1(boolean val) {
		updateSFAt(0x400,!val);
		return val;
	}

	/** 记录各种联机 */
	public static boolean getHistoryStrategy2() {
		return (SecondFlag & 0x800) != 0x800;
	}

	public static boolean setHistoryStrategy2(boolean val) {
		updateSFAt(0x800,!val);
		return val;
	}
	
//	//搜索框->回车时记录普通搜索
//	public static boolean getHistoryStrategy3() {
//		return (SecondFlag & 0x1000) != 0x1000;
//	}
//	public static boolean setHistoryStrategy3(boolean val) {
//		updateSFAt(0x1000,!val);
//		return val;
//	}

	/** 记录各种点击 */
	public static boolean getHistoryStrategy4() {
		return (SecondFlag & 0x2000) != 0x2000;
	}

	public static boolean setHistoryStrategy4(boolean val) {
		updateSFAt(0x2000,!val);
		return val;
	}
	
//	public static boolean getHistoryStrategy5() {
////		return (SecondFlag & 0x4000) != 0x4000;
////	}
////
////	public static boolean setHistoryStrategy5(boolean val) {
////		updateSFAt(0x4000,!val);
////		return val;
////	}
	
//	public static boolean getHistoryStrategy6() {
//		return (SecondFlag & 0x8000) != 0x8000;
//	}
//
//	public static boolean setHistoryStrategy6(boolean val) {
//		updateSFAt(0x8000,!val);
//		return val;
//	}

	/** 记录各种弹出 */
	public static boolean getHistoryStrategy7() {
		return (SecondFlag & 0x10000) == 0x10000;
	}

	public static boolean setHistoryStrategy7(boolean val) {
		updateSFAt(0x10000,val);
		return val;
	}

	/** @return integer 0=always record; 1=don't record; 2=record on exit<br><br>default to 2*/
	public static int getHistoryStrategy8() {
		return (int) ((((SecondFlag >> 17) & 3)+2)%3);
	}

	public static int setHistoryStrategy8(int val) {
		SecondFlag = SecondFlag&(~0x20000l)&(~0x40000l)|(long)((((val+1)%3)&3) << 17);
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

	/** ffmr */
	public boolean getFFmpegThumbsGeneration(){
		return (SecondFlag & 0x200000)!=0;
	}

	public boolean getLogToFile() {
		return (SecondFlag & 0x400000l) != 0x400000l;
	}

	public boolean setLogToFile(boolean val) {
		updateSFAt(0x400000l,!val);
		return val;
	}
	//end crash handler settings

	//start paste bin
	public static boolean getShowPasteBin() {
		return (SecondFlag & 0x800000l) == 0x800000l;
	}
	public static boolean getShowPasteBin(long SecondFlag) {
		return (SecondFlag & 0x800000l) == 0x800000l;
	}
	public static boolean setShowPasteBin(boolean val) {
		updateSFAt(0x800000l,val);
		return val;
	}

	public boolean getPasteBinEnabled() {
		return (SecondFlag & 0x1000000l) != 0x1000000l;
	}
	public boolean setPasteBinEnabled(boolean val) {
		updateSFAt(0x1000000l,!val);
		return val;
	}

	public boolean getPasteBinUpdateDirect() {
		return (SecondFlag & 0x2000000l) != 0x2000000l;
	}
	public boolean setPasteBinUpdateDirect(boolean val) {
		updateSFAt(0x2000000l,!val);
		return val;
	}

	public boolean getPasteBinBringTaskToFront() {
		return (SecondFlag & 0x4000000l) != 0x4000000l;
	}
	public boolean setPasteBinBringTaskToFront(boolean val) {
		updateSFAt(0x4000000l,!val);
		return val;
	}

	/** @return integer: 0=paste_to_main_program <br/>
	 * 2=paste_to_float_search_program<br/>
	 * 3=paste_to_MDCCSP_main_program<br/>
	 * 4=paste_to_MDCCSP_standalone */
	public static int getPasteTarget() {
		return (int) ((SecondFlag >> 29) & 3);
	}
	public static int setPasteTarget(int val) {
		SecondFlag &= (~0x20000000l);
		SecondFlag &= (~0x40000000l);
		SecondFlag |= ((long)(val & 3)) << 29;
		return val;
	}

	//todo 默认关闭
	public static boolean getPasteToPeruseModeWhenFocued() {
		return (SecondFlag & 0x80000000l) != 0x80000000l;
	}
	public static boolean setPasteToPeruseModeWhenFocued(boolean val) {
		updateSFAt(0x80000000l,!val);
		return val;
	}
	
	
	/** @return integer: see {@link #getPasteTarget} */
	public static int getShareTarget() {
		return (int) ((SecondFlag >> 32) & 3);
	}
	public static int setShareTarget(int val) {
		SecondFlag &= (~0x100000000l);
		SecondFlag &= (~0x200000000l);
		SecondFlag |= ((long)(val & 3)) << 32;
		return val;
	}

	public static boolean getShareToPeruseModeWhenFocued() {
		return (SecondFlag & 0x400000000l) != 0x400000000l;
	}
	public static boolean setShareToPeruseModeWhenFocued(boolean val) {
		updateSFAt(0x400000000l,!val);
		return val;
	}

	public static boolean getRoot() {
		return (SecondFlag & 0x800000000l) == 0x800000000l;
	}
	public static boolean setRoot(boolean val) {
		updateSFAt(0x800000000l,val);
		return val;
	}

	//end paste bin

	public static boolean getClassicalKeycaseStrategy() {
		return (SecondFlag & 0x8000000l) != 0x8000000l;
	}
	public static boolean setClassicalKeycaseStrategy(boolean val) {
		updateSFAt(0x8000000l,!val);
		return val;
	}

	public static boolean getKeepScreen() {
		return (SecondFlag & 0x10000000l) != 0x10000000l;
	}
	public static boolean getKeepScreen(long SecondFlag) {
		return (SecondFlag & 0x10000000l) != 0x10000000l;
	}
	public static boolean setKeepScreen(boolean val) {
		updateSFAt(0x10000000l,!val);
		return val;
	}
	public static boolean getHideFloatFromRecent() {
		return getHideFloatFromRecent(SecondFlag);//SecondFlag==null?0:
	}
	public static boolean getHideFloatFromRecent(long SecondFlag) {
		return (SecondFlag & 0x1000000000l) != 0x1000000000l;
	}
	public static boolean setHideFloatFromRecent(boolean val) {
		updateSFAt(0x1000000000l,!val);
		return val;
	}

	//xxx
	public boolean getPeruseUseVolumeBtn() {
		return (SecondFlag & 0x2000000000l) == 0x2000000000l;
	}
	public boolean setPeruseUseVolumeBtn(boolean val) {
		updateSFAt(0x2000000000l,val);
		return val;
	}
	public boolean getDictManager1MultiSelecting() {
		return (SecondFlag & 0x4000000000l) == 0x4000000000l;
	}
	public boolean setDictManager1MultiSelecting(boolean val) {
		updateSFAt(0x4000000000l,val);
		return val;
	}

	public boolean getCheckMdlibs() {
		return (SecondFlag & 0x8000000000l) != 0x8000000000l;
	}
	public boolean setCheckMdlibs(boolean val) {
		updateSFAt(0x8000000000l,!val);
		return val;
	}

	public boolean getHideDedicatedFilter() {
		return (SecondFlag & 0x10000000000l) != 0x10000000000l;
	}
	public boolean setHideDedicatedFilter(boolean val) {
		updateSFAt(0x10000000000l,!val);
		return val;
	}

	public static boolean getShowImageBrowserFlipper() {
		return (SecondFlag & 0x20000000000l) != 0x20000000000l;
	}

	public static boolean getShowSaveImage() {
		return (SecondFlag & 0x40000000000l) == 0x40000000000l;
	}

	public static boolean getClickDismissImageBrowser() {
		return (SecondFlag & 0x80000000000l) != 0x80000000000l;
	}

	public boolean getInPageSearchVisible() {
		return (SecondFlag & 0x100000000000l) == 0x100000000000l;
	}
	public boolean setInPageSearchVisible(boolean val) {
		updateSFAt(0x100000000000l,val);
		return val;
	}

	public static boolean getUseRegex1() {
		return (SecondFlag & 0x200000000000l) == 0x200000000000l;
	}
	public static boolean setUseRegex1(boolean val) {
		updateSFAt(0x200000000000l,val);
		return val;
	}

	public static boolean getUseRegex2() {
		return (SecondFlag & 0x400000000000l) == 0x400000000000l;
	}
	public static boolean setUseRegex2(boolean val) {
		updateSFAt(0x400000000000l,val);
		return val;
	}

	public static boolean getUseRegex3() {
		return (SecondFlag & 0x800000000000l) == 0x800000000000l;
	}
	public int FetUseRegex3() {
		return (SecondFlag & 0x800000000000l)!=0?1:0;
	}
	public boolean CetUseRegex3(long SecondFlag) {
		return (this.SecondFlag & 0x800000000000l)!=(SecondFlag & 0x800000000000l);
	}
	public static boolean setUseRegex3(boolean val) {
		updateSFAt(0x800000000000l,val);
		return val;
	}

	//xxx 废弃
//	public static boolean getRegexAutoAddHead() {
//		return (SecondFlag & 0x1000000000000l) != 0x1000000000000l;
//	}
//	public static boolean setRegexAutoAddHead(boolean val) {
//		updateSFAt(0x1000000000000l,!val);
//		return val;
//	}

	public static boolean getJoniCaseSensitive() {
		return (SecondFlag & 0x2000000000000l) == 0x2000000000000l;
	}
	public static boolean setJoniCaseSensitive(boolean val) {
		updateSFAt(0x2000000000000l,val);
		return val;
	}

	public static boolean getPageCaseSensitive() {
		return (SecondFlag & 0x4000000000000l) == 0x4000000000000l;
	}
	public int FetPageCaseSensitive() {
		return (SecondFlag & 0x4000000000000l)!=0?1<<1:0;
	}
	public boolean CetPageCaseSensitive(long SecondFlag) {
		return (this.SecondFlag & 0x4000000000000l)!=(SecondFlag & 0x4000000000000l);
	}
	public static boolean setPageCaseSensitive(boolean val) {
		updateSFAt(0x4000000000000l,val);
		return val;
	}

	public static boolean getPageWildcardMatchNoSpace() {
		return (SecondFlag & 0x8000000000000l) == 0x8000000000000l;
	}
	public int FetPageWildcardMatchNoSpace() {
		return (SecondFlag & 0x8000000000000l)!=0?1<<3:0;
	}
	public boolean CetPageWildcardMatchNoSpace(long SecondFlag) {
		return (this.SecondFlag & 0x8000000000000l)!=(SecondFlag & 0x8000000000000l);
	}
	public static boolean setPageWildcardMatchNoSpace(boolean val) {
		updateSFAt(0x8000000000000l,val);
		return val;
	}

	public static boolean getPageWildcardSplitKeywords() {
		return (SecondFlag & 0x10000000000000l) != 0x10000000000000l;
	}
	public int FetPageWildcardSplitKeywords() {
		return (SecondFlag & 0x10000000000000l)==0?1<<2:0;
	}
	public boolean CetPageWildcardSplitKeywords(long SecondFlag) {
		return (this.SecondFlag & 0x10000000000000l)!=(SecondFlag & 0x10000000000000l);
	}
	public static boolean setPageWildcardSplitKeywords(boolean val) {
		updateSFAt(0x10000000000000l,!val);
		return val;
	}

	public static boolean getRebuildToast() {
		return (SecondFlag & 0x20000000000000l) == 0x20000000000000l;
	}
	public static boolean setRebuildToast(boolean val) {
		updateSFAt(0x20000000000000l,val);
		return val;
	}


	public static boolean getPageAutoScrollOnTurnPage() {
		return (SecondFlag & 0x80000000000000l) != 0x80000000000000l;
	}
	public static boolean setPageAutoScrollOnTurnPage(boolean val) {
		updateSFAt(0x80000000000000l,!val);
		return val;
	}

	public static boolean getPageAutoScrollOnType() {
		return (SecondFlag & 0x100000000000000l) == 0x100000000000000l;
	}
	public static boolean setPageAutoScrollOnType(boolean val) {
		updateSFAt(0x100000000000000l,val);
		return val;
	}

	public static boolean getInPageSearchAutoHideKeyboard() {
		return (SecondFlag & 0x200000000000000l) != 0x200000000000000l;
	}
	public static boolean setInPageSearchAutoHideKeyboard(boolean val) {
		updateSFAt(0x200000000000000l,!val);
		return val;
	}

	public static boolean getInPageSearchUseAudioKey() {
		return (SecondFlag & 0x400000000000000l) == 0x400000000000000l;
	}
	public static boolean setInPageSearchUseAudioKey(boolean val) {
		updateSFAt(0x400000000000000l,val);
		return val;
	}

	public static boolean getInPageSearchShowNoNoMatch() {
		return (SecondFlag & 0x800000000000000l) == 0x800000000000000l;
	}
	public static boolean setInPageSearchShowNoNoMatch(boolean val) {
		updateSFAt(0x800000000000000l,val);
		return val;
	}

	public static boolean getInPageSearchHighlightBorder() {
		return (SecondFlag & 0x1000000000000000l) == 0x1000000000000000l;
	}
	public static boolean setInPageSearchHighlightBorder(boolean val) {
		updateSFAt(0x1000000000000000l,val);
		return val;
	}

	public static boolean getInPageSearchAutoUpdateAfterFulltext() {
		return (SecondFlag & 0x2000000000000000l) != 0x2000000000000000l;
	}
	public static boolean setInPageSearchAutoUpdateAfterFulltext(boolean val) {
		updateSFAt(0x2000000000000000l,!val);
		return val;
	}

	public static boolean getInPageSearchAutoUpdateAfterClick() {
		return (SecondFlag & 0x4000000000000000l) == 0x4000000000000000l;
	}
	public static boolean setInPageSearchAutoUpdateAfterClick(boolean val) {
		updateSFAt(0x4000000000000000l,val);
		return val;
	}

	public static boolean getBackToHomePage() {
		return (SecondFlag & 0x8000000000000000l) == 0x8000000000000000l;
	}
	public static boolean setBackToHomePage(boolean val) {
		updateSFAt(0x8000000000000000l,val);
		return val;
	}
	///////////////////End second flag///////////////////////
	/////////////// 天高任鸟飞 标志任意写 ///////////////////////
	///////////////////Start Third Flag///////////////////////
	private static Long ThirdFlag=null;
	public long getThirdFlag() {
		if(ThirdFlag==null) {
			return ThirdFlag=defaultReader.getLong("MTF",0);
		}
		return ThirdFlag;
	}
	private void putThirdFlag(long val) {
		defaultReader.edit().putLong("MTF",ThirdFlag=val).apply();
	}
	public Long ThirdFlag() {
		return ThirdFlag;
	}
	private void updateTFAt(int o, boolean val) {
		ThirdFlag &= (~o);
		if(val) ThirdFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}
	private static void updateTFAt(long o, boolean val) {
		ThirdFlag &= (~o);
		if(val) ThirdFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}

	/**
	 *  Get Back Prevention Type. Default to 2<br/>
	 *  @return 0=exit directly; 1=show top snack; 2=toast; 3=dialog;
	 */
	public static int getBackPrevention() {
		return ((int)(ThirdFlag&3)+2)%4;
	}

	/** Set Back Prevention Type <br/> see {@link #getBackPrevention}*/
	public static int setBackPrevention(int val) {
		ThirdFlag = ThirdFlag&~0x1l&~0x2l
				|(long)((val+2)%4 & 3);
		return val;
	}

	public static boolean getBackToHomePagePreventBack() {
		return (ThirdFlag & 0x4l) == 0x4l;
	}
	public static boolean setBackToHomePagePreventBack(boolean val) {
		updateTFAt(0x4l,val);
		return val;
	}

	public static boolean getToastRoundedCorner() {
		return (ThirdFlag & 0x8l) != 0x8l;
	}
	public static boolean setToastRoundedCorner(boolean val) {
		updateTFAt(0x8l,!val);
		return val;
	}

	public static boolean getImmersiveClickSearch() {
		return (ThirdFlag & 0x10l) != 0x10l;
	}
	public static boolean getImmersiveClickSearch(Long ThirdFlag) {
		return (ThirdFlag & 0x10l) != 0x10l;
	}
	public static boolean setImmersiveClickSearch(boolean val) {
		updateTFAt(0x10l,!val);
		return val;
	}

	public static boolean getTopSnapMaximizeClickSearch() {
		return (ThirdFlag & 0x20l) != 0x20l;
	}
	public static boolean setTopSnapMaximizeClickSearch(boolean val) {
		updateTFAt(0x20l,!val);
		return val;
	}

	public static boolean getClickSearchPin() {
		return (ThirdFlag & 0x40l) == 0x40l;
	}
	public static boolean setClickSearchPin(boolean val) {
		updateTFAt(0x40l,val);
		return val;
	}

	public static boolean getClickSearchDismissDelay() {
		return (ThirdFlag & 0x80l) == 0x80l;
	}
	public static boolean setClickSearchDismissDelay(boolean val) {
		updateTFAt(0x80l,val);
		return val;
	}

	public static boolean getInFloatPageSearchVisible() {
		return (ThirdFlag & 0x100l) == 0x100l;
	}

	public static boolean setInFloatPageSearchVisible(boolean val) {
		updateTFAt(0x100l,val);
		return val;
	}

	public static boolean getDoubleClickMaximizeClickSearch() {
		return (ThirdFlag & 0x200l) != 0x200l;
	}
	public static boolean setDoubleClickMaximizeClickSearch(boolean val) {
		updateTFAt(0x200l,!val);
		return val;
	}

	public static boolean getPinClickSearch() {
		return (ThirdFlag & 0x400l) != 0x400l;
	}
	public static boolean setPinClickSearch(boolean val) {
		updateTFAt(0x400l,!val);
		return val;
	}

	public static boolean getMultipleClickSearch() {
		return (ThirdFlag & 0x800l) != 0x800l;
	}
	public static boolean setMultipleClickSearch(boolean val) {
		updateTFAt(0x800l,!val);
		return val;
	}

	public static boolean getSkipClickSearch() {
		return (ThirdFlag & 0x1000l) != 0x1000l;
	}
	public static boolean setSkipClickSearch(boolean val) {
		updateTFAt(0x1000l,!val);
		return val;
	}

	public static boolean getResetPosClickSearch() {
		return (ThirdFlag & 0x2000l) != 0x2000l;
	}
	public static boolean setResetPosClickSearch(boolean val) {
		updateTFAt(0x2000l,!val);
		return val;
	}

	public static boolean getResetMaxClickSearch() {
		return (ThirdFlag & 0x4000l) != 0x4000l;
	}
	public static boolean setResetMaxClickSearch(boolean val) {
		updateTFAt(0x4000l,!val);
		return val;
	}

	/** @return integer: 0=Search in current dictionary group <br/>
	 * 1=Search in dedicated click-search dictionaries in current dictionary group <br/>
	 * 2=Search in current selected dictionary.<br/>*/
	public static int getClickSearchMode() {
		return (int) ((ThirdFlag >> 15) & 3);
	}
	public static int setClickSearchMode(int val) {
		ThirdFlag = (ThirdFlag & ~0x8000l & ~0x10000l) | (((long)(val & 3)) << 15);
		return val;
	}

	public static boolean getSwichClickSearchDictOnTop() {
		return (ThirdFlag & 0x10000l) == 0x10000l;
	}
	public static boolean setSwichClickSearchDictOnTop(boolean val) {
		updateTFAt(0x10000l,val);
		return val;
	}

	public static boolean getSwichClickSearchDictOnBottom() {
		return (ThirdFlag & 0x20000l) != 0x20000l;
	}
	public static boolean setSwichClickSearchDictOnBottom(boolean val) {
		updateTFAt(0x20000l,!val);
		return val;
	}

	public static boolean getSwichClickSearchDictOnNav() {
		return (ThirdFlag & 0x40000l) == 0x40000l;
	}
	public static boolean setSwichClickSearchDictOnNav(boolean val) {
		updateTFAt(0x40000l,val);
		return val;
	}

	public static boolean getInPageSearchUseWildcard() {
		return (ThirdFlag & 0x80000l) != 0x80000l;
	}
	public int FetInPageSearchUseWildcard() {
		return (ThirdFlag & 0x80000l)==0?1<<4:0;
	}
	public boolean CetInPageSearchUseWildcard(long ThirdFlag) {
		return (this.ThirdFlag & 0x80000l)!=(ThirdFlag & 0x80000l);
	}
	public static boolean setInPageSearchUseWildcard(boolean val) {
		updateTFAt(0x80000l,!val);
		return val;
	}

	public static boolean getAdvSearchUseWildcard() {
		return (ThirdFlag & 0x100000l) != 0x100000l;
	}
	public static boolean setAdvSearchUseWildcard(boolean val) {
		updateTFAt(0x100000l,!val);
		return val;
	}

	public static boolean getClickSearchUseMorphology() {
		return (ThirdFlag & 0x200000l) != 0x200000l;
	}
	public static boolean setClickSearchUseMorphology(boolean val) {
		updateTFAt(0x200000l,!val);
		return val;
	}


	/** @return integer: 0=entry page forword/backward <br/>
	 * 1=web page forword/backward <br/>
	 */
	public int getBottomNavigationMode() {
		return (int) ((ThirdFlag >> 22) & 3);
	}
	public int setBottomNavigationMode(int val) {
		ThirdFlag &= (~0x400000l);
		ThirdFlag &= (~0x800000l);
		ThirdFlag |= ((long)(val & 3)) << 22;
		return val;
	}

	/** x-axis fit/snap preference.
	 * @return integer: 0=center 1=left 2=right <br/>
	 */
	public static int getXPhotoPreference() {
		return (int) ((ThirdFlag >> 24) & 3);
	}
	public static int setXPhotoPreference(int val) {
		ThirdFlag &= (~0x1000000l);
		ThirdFlag &= (~0x2000000l);
		ThirdFlag |= ((long)(val & 3)) << 24;
		return val;
	}

	/** @return boolean:true pan image ; false nav text*/
	public static boolean getIsoImgClickThrough() {
		return (ThirdFlag & 0x4000000l) == 0x4000000l;
	}

	public static boolean setIsoImgClickThrough(boolean val) {
		updateTFAt(0x4000000l,val);
		return val;
	}

	public static boolean getIsoImgLimitTextHight() {
		return (ThirdFlag & 0x8000000l) != 0x8000000l;
	}

	public static boolean setIsoImgLimitTextHight(boolean val) {
		updateTFAt(0x8000000l,!val);
		return val;
	}

	//xxx

	public boolean getPageTurn3() {
		return (ThirdFlag & 0x200000000l) != 0x200000000l;
	}

	public boolean setPageTurn3(boolean val) {
		updateTFAt(0x200000000l,!val);
		return val;
	}


	public static  boolean getAllowTintClickSearchBG() {
		return (ThirdFlag & 0x400000000l) == 0x400000000l;
	}

	public static  boolean setAllowTintClickSearchBG(boolean val) {
		updateTFAt(0x400000000l,val);
		return val;
	}

	public static boolean getSearchUseMorphology() {
		return (ThirdFlag & 0x400000000l) != 0x400000000l;
	}

	public static boolean setSearchUseMorphology(boolean val) {
		updateTFAt(0x400000000l,!val);
		return val;
	}

	public boolean getClickSearchEnabled() {
		return (ThirdFlag & 0x800000000l) != 0x800000000l;
	}
	public boolean getClickSearchEnabled(long ThirdFlag) {
		return (ThirdFlag & 0x800000000l) != 0x800000000l;
	}
	public int FetClickSearchEnabled() {
		return (ThirdFlag & 0x800000000l)==0?1<<5:0;
	}
	
	@Multiline(flagPos=35, shift=1) public boolean toggleClickSearchEnabled() { ThirdFlag=ThirdFlag; throw new IllegalArgumentException(); }
	
	public int FetIsDark() {
		return GlobalOptions.isDark?1<<6:0;
	}

	public static boolean getUseTTSToReadEntry() {
		return (ThirdFlag & 0x1000000000l) != 0x1000000000l;
	}
	public static boolean setUseTTSToReadEntry(boolean val) {
		updateTFAt(0x1000000000l,!val);
		return val;
	}

	public boolean getHintTTSReading() {
		return (ThirdFlag & 0x2000000000l) == 0x2000000000l;
	}
	public boolean setHintTTSReading(boolean val) {
		updateTFAt(0x2000000000l,val);
		return val;
	}

	public boolean getAutoReadEntry() {
		return (ThirdFlag & 0x4000000000l) == 0x4000000000l;
	}
	public boolean setAutoReadEntry(boolean val) {
		updateTFAt(0x4000000000l,val);
		return val;
	}

	//todo 我好像弄乱了
	public boolean getMakeWayForVolumeAjustmentsWhenAudioPlayed() {
		return (ThirdFlag & 0x8000000000l) != 0x8000000000l;
	}
	public boolean setMakeWayForVolumeAjustmentsWhenAudioPlayed(boolean val) {
		updateTFAt(0x8000000000l,!val);
		return val;
	}

	public boolean getTTSCtrlPinned() {
		return (ThirdFlag & 0x10000000000l) != 0x10000000000l;
	}
	public boolean setTTSCtrlPinned(boolean val) {
		updateTFAt(0x10000000000l,!val);
		return val;
	}

	public boolean getTTSExpanded() {
		return (ThirdFlag & 0x20000000000l) != 0x20000000000l;
	}
	public boolean setTTSExpanded(boolean val) {
		updateTFAt(0x20000000000l,!val);
		return val;
	}

	public boolean getTTSBackgroundPlay() {
		return (ThirdFlag & 0x40000000000l) != 0x40000000000l;
	}
	public boolean setTTSBackgroundPlay(boolean val) {
		updateTFAt(0x40000000000l,!val);
		return val;
	}

	public boolean getTTSHightlight() {
		return (ThirdFlag & 0x80000000000l) == 0x80000000000l;
	}
	public boolean getTTSHightlight(boolean val) {
		updateTFAt(0x80000000000l,val);
		return val;
	}

	public boolean getTTSHighlightWebView() {
		return (ThirdFlag & 0x100000000000l) == 0x100000000000l;
	}
	public boolean setTTSHighlightWebView(boolean val) {
		updateTFAt(0x100000000000l,val);
		return val;
	}


	public static boolean getClickSearchAutoReadEntry() {
		return (ThirdFlag & 0x200000000000l) == 0x200000000000l;
	}
	public static boolean setClickSearchAutoReadEntry(boolean val) {
		updateTFAt(0x200000000000l,val);
		return val;
	}

	public boolean getUseBackKeyGoWebViewBack() {
		return (ThirdFlag & 0x400000000000l) == 0x400000000000l;
	}
	public boolean setUseBackKeyGoWebViewBack(boolean val) {
		updateTFAt(0x400000000000l,val);
		return val;
	}


	public static boolean getLazyLoadDicts() {
		return (ThirdFlag & 0x800000000000l) != 0x800000000000l;
	}
	public static boolean setLazyLoadDicts(boolean val) {
		updateTFAt(0x800000000000l,!val);
		return val;
	}


	public static boolean getEnableWebDebug() {
		return (ThirdFlag & 0x1000000000000l) != 0x1000000000000l;
	}
	public static boolean setEnableWebDebug(boolean val) {
		updateTFAt(0x1000000000000l,!val);
		return val;
	}

	/** @return integer: 0=entry page forword/backward <br/>
	 * 1=web page forword/backward <br/>
	 */
	public int getBottomNavigationMode1() {
		return (int) ((ThirdFlag >> 49) & 3);
	}
	public int setBottomNavigationMode1(int val) {
		ThirdFlag &= (~0x2000000000000l);
		ThirdFlag &= (~0x4000000000000l);
		ThirdFlag |= ((long)(val & 3)) << 49;
		return val;
	}

	//xxx 废弃？？？
	public boolean getUseBackKeyGoWebViewBack1() {
		return (ThirdFlag & 0x8000000000000l) == 0x8000000000000l;
	}
	public boolean setUseBackKeyGoWebViewBack1(boolean val) {
		updateTFAt(0x8000000000000l,val);
		return val;
	}

	public boolean getPeruseRestoreOldAI() {
		return (ThirdFlag & 0x10000000000000l) == 0x10000000000000l;
	}
	public boolean setPeruseRestoreOldAI(boolean val) {
		updateTFAt(0x10000000000000l,val);
		return val;
	}

	public boolean getToTextShare() {
		return (ThirdFlag & 0x20000000000000l) == 0x20000000000000l;
	}
	public boolean setToTextShare(boolean val) {
		updateTFAt(0x20000000000000l,val);
		return val;
	}

	public boolean getPeruseInPageSearchVisible() {
		return (ThirdFlag & 0x40000000000000l) == 0x40000000000000l;
	}
	public boolean setPeruseInPageSearchVisible(boolean val) {
		updateTFAt(0x40000000000000l,val);
		return val;
	}

	public boolean getPeruseTextSelectable() {
		return (ThirdFlag & 0x80000000000000l) == 0x80000000000000l;
	}
	public boolean setPeruseTextSelectable(boolean val) {
		updateTFAt(0x80000000000000l,val);
		return val;
	}

	public static boolean getUseBackKeyClearWebViewFocus() {
		return (ThirdFlag & 0x100000000000000l) != 0x100000000000000l;
	}
	public static boolean setUseBackKeyClearWebViewFocus(boolean val) {
		updateTFAt(0x100000000000000l,!val);
		return val;
	}

	public boolean getToTextShare2() {
		return (ThirdFlag & 0x200000000000000l) == 0x200000000000000l;
	}
	public boolean setToTextShare2(boolean val) {
		updateTFAt(0x200000000000000l,val);
		return val;
	}

	public boolean getPrintPageSize() {
		return (ThirdFlag & 0x400000000000000l) != 0x400000000000000l;
	}
	public boolean setPrintPageSize(boolean val) {
		updateTFAt(0x400000000000000l,!val);
		return val;
	}

	public static boolean getFloatFullScreen() {
		return (ThirdFlag & 0x800000000000000l) == 0x800000000000000l;
	}
	public static boolean setFloatFullScreen(boolean val) {
		updateTFAt(0x800000000000000l,val);
		return val;
	}

	public static boolean getFloatHideNavigation() {
		return (ThirdFlag & 0x1000000000000000l) == 0x1000000000000000l;
	}
	public static boolean setFloatHideNavigation(boolean val) {
		updateTFAt(0x1000000000000000l,val);
		return val;
	}

	public static boolean getCacheSoundResInAdvance() {
		return (ThirdFlag & 0x2000000000000000l) == 0x2000000000000000l;
	}
	public static boolean setCacheSoundResInAdvance(boolean val) {
		updateTFAt(0x2000000000000000l,val);
		return val;
	}

	public static boolean getAllowHiddenRecords() {
		return (ThirdFlag & 0x4000000000000000l) != 0x4000000000000000l;
	}
	public static boolean setAllowHiddenRecords(boolean val) {
		updateTFAt(0x4000000000000000l,!val);
		return val;
	}

	public static boolean getUseSoundsPlaybackFirst() {
		return (ThirdFlag & 0x8000000000000000l) == 0x8000000000000000l;
	}
	public static boolean setUseSoundsPlaybackFirst(boolean val) {
		updateTFAt(0x8000000000000000l,val);
		return val;
	}

	/////////////////////End Third Flag////////////////////////////////////
	/////////////////////Start Fourth Flag///////////////////////////////////
	private static Long FourthFlag=null;
	public long getFourthFlag() {
		if(FourthFlag==null) {
			return FourthFlag=defaultReader.getLong("MQF",0);
		}
		return FourthFlag;
	}
	public static long getFourthFlag(Context context) {
		if(FourthFlag==null) {
			return FourthFlag= androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getLong("MQF",0);
		}
		return FourthFlag;
	}
	private void putFourthFlag(long val) {
		defaultReader.edit().putLong("MQF",FourthFlag=val).apply();
	}
	public Long FourthFlag() {
		return FourthFlag;
	}
	private void updateQFAt(int o, boolean val) {
		FourthFlag &= (~o);
		if(val) FourthFlag |= o;
	}
	private static void updateQFAt(long o, boolean val) {
		FourthFlag &= (~o);
		if(val) FourthFlag |= o;
	}

	/** flagPos=0;<br></> 0=在右; 1=在左; 2=无; 3=系统滚动条 */
	public int getScrollbarTypeMainA(){
		return (int) (FourthFlag & 3);
	}

	/** flagPos=2;<br></> 0=在右; 1=在左; 2=无; 3=系统滚动条 */
	public int getScrollbarTypeMainB(){
		return (int) ((FourthFlag>>2) & 3);
	}

	/** flagPos=4;<br></> 0=在右; 1=在左; 2=无; 3=系统滚动条 */
	public int getScrollbarTypeFloatA(){
		return (int) ((FourthFlag>>4) & 3);
	}

	/** flagPos=6;<br></> 0=在右; 1=在左; 2=无; 3=系统滚动条 */
	public int getScrollbarTypeFloatB(){
		return (int) ((FourthFlag>>6) & 3);
	}

	/** flagPos=8;<br> 翻阅模式 <br> 0=在右; 1=在左; 2=无; 3=系统滚动条 */
	public int getScrollbarTypePeruse(){
		return (int) ((FourthFlag>>8) & 3);
	}
//	public int setScrollbarTypePeruse(int val) {
//		FourthFlag = ~0x100l & ~0x200l | (long)((val & 3)<<8);
//		return val;
//	}

	public int getTypeFlag_11_AtQF(int flagPos) {
		return (int) ((FourthFlag>>flagPos) & 3);
	}
	public int setTypeFlag_11_AtQF(int val, int flagPos) {
		FourthFlag = FourthFlag & ~(0x3l<<flagPos) | (long)((val & 3)<<flagPos);
		return val;
	}

	public boolean getScrollTypeApplyToAll() {
		return (FourthFlag & 0x400l) == 0x400l;
	}
	public boolean setScrollTypeApplyToAll(boolean val) {
		updateQFAt(0x400l,val);
		return val;
	}

	public boolean getDatabaseRestoreListPosition() {
		return (FourthFlag & 0x800l) == 0x800l;
	}
	public boolean setDatabaseRestoreListPosition(boolean val) {
		updateQFAt(0x800l,val);
		return val;
	}

	public boolean getDatabaseRestoreListPosition1() {
		return (FourthFlag & 0x1000l) == 0x1000l;
	}
	public boolean setDatabaseRestoreListPosition1(boolean val) {
		updateQFAt(0x1000l,val);
		return val;
	}

	public boolean getDatabaseEnterAnimation() {
		return (FourthFlag & 0x2000l) == 0x2000l;
	}
	public boolean setDatabaseEnterAnimation(boolean val) {
		updateQFAt(0x2000l,val);
		return val;
	}

	public boolean getDatabaseEnterAnimation1() {
		return (FourthFlag & 0x4000l) == 0x4000l;
	}
	public boolean setDatabaseEnterAnimation1(boolean val) {
		updateQFAt(0x4000l,val);
		return val;
	}

	public boolean getDatabaseListUseVolumeKeyToNavigate() {
		return (FourthFlag & 0x8000l) == 0x8000l;
	}
	public boolean setDatabaseListUseVolumeKeyToNavigate(boolean val) {
		updateQFAt(0x8000l,val);
		return val;
	}

	public boolean getDatabaseListUseVolumeKeyToNavigate1() {
		return (FourthFlag & 0x10000l) == 0x10000l;
	}
	public boolean setDatabaseListUseVolumeKeyToNavigate1(boolean val) {
		updateQFAt(0x10000l,val);
		return val;
	}

	public boolean getDatabaseDelayPullData() {
		return (FourthFlag & 0x20000l) == 0x20000l;
	}
	public boolean setDatabaseDelayPullData(boolean val) {
		updateQFAt(0x20000l,val);
		return val;
	}

	public boolean getDatabaseDelayPullData1() {
		return (FourthFlag & 0x40000l) == 0x40000l;
	}
	public boolean setDatabaseDelayPullData1(boolean val) {
		updateQFAt(0x40000l,val);
		return val;
	}

	public static boolean getFloatClickHideToBackground() {
		return (FourthFlag & 0x80000l) != 0x80000l;
	}
	public static boolean setFloatClickHideToBackground(boolean val) {
		updateQFAt(0x80000l,!val);
		return val;
	}
	
	@Multiline(flagPos=20) public static boolean getSimpleMode() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Multiline(flagPos=20) public static boolean getSimpleMode(long FourthFlag) { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Multiline(flagPos=20) public static void setSimpleMode(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
 
	public static boolean getEnsureAtLeatOneExpandedPage() {
		return (FourthFlag & 0x200000l) != 0x200000l;
	}
	public static boolean setEnsureAtLeatOneExpandedPage(boolean val) {
		updateQFAt(0x200000l,!val);
		return val;
	}

	public static boolean getOnlyExpandTopPage() {
		return (FourthFlag & 0x400000l) == 0x400000l;
	}
	public static boolean setOnlyExpandTopPage(boolean val) {
		updateQFAt(0x400000l,val);
		return val;
	}

	public static boolean getDelaySecondPageLoading() {
		return (FourthFlag & 0x800000l) != 0x800000l;
	}
	public static boolean setDelaySecondPageLoading(boolean val) {
		updateQFAt(0x800000l,!val);
		return val;
	}

	public static boolean getScrollAnimation() {
		return (FourthFlag & 0x1000000l) != 0x1000000l;
	}
	public static boolean setScrollAnimation(boolean val) {
		updateQFAt(0x1000000l,!val);
		return val;
	}

	public static boolean getScrollAutoExpand() {
		return (FourthFlag & 0x2000000l) != 0x2000000l;
	}
	public static boolean setScrollAutoExpand(boolean val) {
		updateQFAt(0x2000000l,!val);
		return val;
	}


	public static boolean getEnableResumeDebug() {
		return true;
		//return (FourthFlag & 0x4000000l) == 0x4000000l;
	}
	public static boolean setEnableResumeDebug(boolean val) {
		updateQFAt(0x4000000l,val);
		return val;
	}

	public static boolean getEnableSuperImmersiveScrollMode() {
		return (FourthFlag & 0x8000000l) != 0x8000000l;
	}
	public static boolean setEnableSuperImmersiveScrollMode(boolean val) {
		updateQFAt(0x8000000l,!val);
		return val;
	}

	public static boolean getInheritGlobleWebcolorBeforeSwichingToInternal() {
		return (FourthFlag & 0x10000000l) == 0x10000000l;
	}
	public static boolean setInheritGlobleWebcolorBeforeSwichingToInternal(boolean val) {
		updateQFAt(0x10000000l,val);
		return val;
	}

	public static boolean getHackDisableMagnifier() {
		return (FourthFlag & 0x20000000l) == 0x20000000l;
	}
	public static boolean setHackDisableMagnifier(boolean val) {
		updateQFAt(0x20000000l,val);
		return val;
	}

	public static boolean getTintIconForeground() {
		return true;//(FourthFlag & 0x40000000l) == 0x40000000l;
	}
	public static boolean setTintIconForeground(boolean val) {
		updateQFAt(0x40000000l,val);
		return val;
	}

	public static boolean getAutoBrowsingReadEntry() {
		return true;//(FourthFlag & 0x80000000l) == 0x80000000l;
	}
	public static boolean setAutoBrowsingReadEntry(boolean val) {
		updateQFAt(0x80000000l,val);
		return val;
	}

	public static boolean getAutoBrowsingReadContent() {
		return false;//(FourthFlag & 0x100000000l) == 0x100000000l;
	}
	public static boolean setAutoBrowsingReadContent(boolean val) {
		updateQFAt(0x100000000l,val);
		return val;
	}

	public static boolean getAutoBrowsingReadSomething() {
		return getAutoBrowsingReadContent()||getAutoBrowsingReadEntry();//(FourthFlag & 0x80000000l) == 0x80000000l;
	}


	public static boolean getEnableFanjnConversion() {
		return true;//(FourthFlag & 0x200000000l) == 0x200000000l;
	}
	public static boolean setEnableFanjnConversion(boolean val) {
		updateQFAt(0x200000000l,val);
		return val;
	}


	/** Get FullScreen Landscape Mode for h5 video tags.
	 * @return integer: 0=force landscape <br/>
	 * 1=no change <br/>
	 * 2=auto detect <br/>
	 */
	public int getFullScreenLandscapeMode() {
		return (int) ((FourthFlag >> 32) & 3);
	}
	public int setFullScreenLandscapeMode(int val) {
		FourthFlag &= (~0x400000000l);
		FourthFlag &= (~0x800000000l);
		FourthFlag |= ((long)(val & 3)) << 32;
		return val;
	}
	
	public static boolean getEtSearchNoMagnifier() {
		return (FourthFlag & 0x1000000000l) == 0x1000000000l;
	}
	public static boolean setEtSearchNoMagnifier(boolean val) {
		updateQFAt(0x1000000000l,val);
		return val;
	}
	
	public static boolean getTitlebarUseGlobalUIColor() {
		return (FourthFlag & 0x2000000000l) != 0x2000000000l;
	}
	public static boolean setTitlebarUseGlobalUIColor(boolean val) {
		updateQFAt(0x2000000000l,!val);
		return val;
	}
	
	public static boolean getTitlebarUseGradient() {
		return (FourthFlag & 0x4000000000l) != 0x4000000000l;
	}
	public static boolean setTitlebarUseGradient(boolean val) {
		updateQFAt(0x4000000000l,!val);
		return val;
	}
	
	public static boolean getTransitSplashScreen() {
		return (FourthFlag & 0x8000000000l) != 0x8000000000l;
	}
	public static boolean setTransitSplashScreen(boolean val) {
		updateQFAt(0x8000000000l,!val);
		return val;
	}
	
	public boolean getShuntDownVMOnExit() {
		return (FourthFlag & 0x10000000000l) == 0x10000000000l;
	}
	
	/** 是否是关联拷贝 */
	public boolean getLinkContentBarProj() {
		return (FourthFlag & 0x20000000000l) == 0x20000000000l;
	}
	
	public boolean getDeletHistoryOnExit() {
		return (FourthFlag & 0x40000000000l) == 0x40000000000l;
	}
	
	public boolean getRestoreAllBottombarProj() {
		return (FourthFlag & 0x80000000000l) == 0x80000000000l;
	}
	
	public boolean getPhotoViewLockXMovement() {
		return (FourthFlag & 0x100000000000l) == 0x100000000000l;
	}
	
	public boolean getPhotoViewLongclickable() {
		return (FourthFlag & 0x200000000000l) != 0x200000000000l;
	}
	public void setPhotoViewLongclickable(boolean val) {
		updateQFAt(0x200000000000l,!val);
	}
	
	public boolean getPhotoViewShowFloatMenu() {
		return (FourthFlag & 0x400000000000l) != 0x400000000000l;
	}
	public boolean getPhotoViewShowFloatExit() {
		return (FourthFlag & 0x800000000000l) != 0x800000000000l;
	}
	
	public boolean getThenAutoReadContent() {
		return (FourthFlag & 0x1000000000000l) == 0x1000000000000l;
	}
	
	@Multiline(flagPos=49) public static boolean getForceFloatSingletonSearch() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Multiline(flagPos=49) public static boolean getForceFloatSingletonSearch(long FourthFlag) { FourthFlag=FourthFlag; throw new RuntimeException();}
	
	@Multiline(flagPos=50) public static boolean isSingleThreadServer() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Multiline(flagPos=51) public static boolean getServerStarted() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Multiline(flagPos=51) public static void setServerStarted(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
	
	@Multiline(flagPos=52, shift=1) public static boolean checkVersionBefore_4_0() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Multiline(flagPos=52, shift=1) public static void uncheckVersionBefore_4_0(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
	//@Multiline(flagPos=52, shift=1) public static boolean uncheckVersionBefore_4_0() { FourthFlag=FourthFlag; throw new IllegalArgumentException();}
	@Multiline(flagPos=53) public static boolean getClearTasksOnExit() { FourthFlag=FourthFlag; throw new RuntimeException();}

	@Multiline(flagPos=54) public boolean getRememberVSPanelGo(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=54) public void setRememberVSPanelGo(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=55) public boolean getVSPanelGOTransient(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=55) public void setVSPanelGOTransient(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=56) public boolean getPinDialog_2(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=56) public void setPinDialog_2(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=57, shift=1) public boolean getPrvNxtDictSkipNoMatch(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=58) public boolean getDelayContents(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=59, shift=1) public boolean getAnimateContents(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=59, shift=1) public static void setAnimateContents(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=61, shift=1) public boolean getLeaveContentBlank(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=61, shift=1) public static void setLeaveContentBlank(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=60, shift=1) public boolean getDimScrollbarForPrvNxt(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=61, shift=1) public boolean getAutoAdjustFloatBottomBar(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	//EQ
	///////////////////// End Quart Flag////////////////////////////////////
	/////////////////////Start Fifth Flag///////////////////////////////////
	//EQ
	private static Long FifthFlag=null;
	public long getFifthFlag() {
		if(FifthFlag==null) {
			return FifthFlag=defaultReader.getLong("MVF",0);
		}
		return FifthFlag;
	}
	public static long getFifthFlag(Context context) {
		if(FifthFlag==null) {
			return FifthFlag= androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getLong("MVF",0);
		}
		return FifthFlag;
	}
	private void putFifthFlag(long val) {
		defaultReader.edit().putLong("MVF",FifthFlag=val).apply();
	}
	public Long FifthFlag() {
		return FifthFlag;
	}
	
	@Multiline(flagPos=0, max=3, flagSize=5, shift=1) public static int getSendToAppTarget(){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=0, max=3, flagSize=5, shift=1) public static void setSendToAppTarget(int val){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	
	public int getSendToShareTarget(){ return IU.parsint(defaultReader.getString("share_to", null), 1); }
	
	
	@Multiline(flagPos=10, shift=1) public static boolean getAdjustScnShown(){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	@Multiline(flagPos=10, shift=1) public boolean toggleAdjustScnShown() { FifthFlag=FifthFlag; throw new IllegalArgumentException(); }
	
	
	//EF
	///////////////////// End Fifth Flag////////////////////////////////////
	//EF
	
	///////
	///////
	public static void setTmpIsFlag(BookPresenter mdTmp, int val) {
		mdTmp.tmpIsFlag=val;
	}
	public static void setTmpIsFlag(mdict_manageable mmTmp, int val) {
		mmTmp.setTmpIsFlag(val);
	}
	public static boolean getTmpIsFiler(int tmpIsFlag) {
		return (tmpIsFlag&0x1)!=0;
	}
	public static boolean setTmpIsFiler(BookPresenter mdTmp, PlaceHolder placeHolder, boolean val) {
		if(mdTmp!=null){
			mdTmp.tmpIsFlag &= (~0x1);
			if(val) mdTmp.tmpIsFlag |= 0x1;
		} else if(placeHolder!=null){
			placeHolder.tmpIsFlag &= (~0x1);
			if(val) placeHolder.tmpIsFlag |= 0x1;
		}
		return val;
	}
	public static boolean setTmpIsFiler(mdict_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x1);
		if(val) tmpIsFlag |= 0x1;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static boolean toggleTmpIsFiler(mdict_manageable mdTmp) {
		return setTmpIsFiler(mdTmp, !getTmpIsFiler(mdTmp.getTmpIsFlag()));
	}
	public static boolean getTmpIsClicker(int tmpIsFlag) {
		return (tmpIsFlag&0x2)!=0;
	}
	public static boolean setTmpIsClicker(BookPresenter mdTmp, PlaceHolder placeHolder, boolean val) {
		if(mdTmp!=null){
			mdTmp.tmpIsFlag &= (~0x2);
			if(val) mdTmp.tmpIsFlag |= 0x2;
		} else if(placeHolder!=null){
			placeHolder.tmpIsFlag &= (~0x2);
			if(val) placeHolder.tmpIsFlag |= 0x2;
		}
		return val;
	}
	public static boolean setTmpIsClicker(mdict_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x2);
		if(val) tmpIsFlag |= 0x2;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static boolean toggleTmpIsClicker(mdict_manageable mdTmp) {
		return setTmpIsClicker(mdTmp, !getTmpIsClicker(mdTmp.getTmpIsFlag()));
	}
	public static boolean getTmpIsAudior(int tmpIsFlag) {
		return (tmpIsFlag&0x4)!=0;
	}
	public static boolean setTmpIsAudior(BookPresenter mdTmp, PlaceHolder placeHolder, boolean val) {
		if(mdTmp!=null){
			mdTmp.tmpIsFlag &= (~0x4);
			if(val) mdTmp.tmpIsFlag |= 0x4;
		} else if(placeHolder!=null){
			placeHolder.tmpIsFlag &= (~0x4);
			if(val) placeHolder.tmpIsFlag |= 0x4;
		}
		return val;
	}
	public static boolean setTmpIsAudior(mdict_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x4);
		if(val) tmpIsFlag |= 0x4;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static boolean toggleTmpIsAudior(mdict_manageable mdTmp) {
		return setTmpIsAudior(mdTmp,  !getTmpIsAudior(mdTmp.getTmpIsFlag()));
	}
	public static boolean getTmpIsHidden(int tmpIsFlag) {
		return (tmpIsFlag&0x8)!=0;
	}
	public static boolean setTmpIsHidden(BookPresenter mdTmp, PlaceHolder placeHolder, boolean val) {
		if(mdTmp!=null){
			mdTmp.tmpIsFlag &= (~0x8);
			if(val) mdTmp.tmpIsFlag |= 0x8;
		} else if(placeHolder!=null){
			placeHolder.tmpIsFlag &= (~0x8);
			if(val) placeHolder.tmpIsFlag |= 0x8;
		}
		return val;
	}
	public static boolean setTmpIsHidden(mdict_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x8);
		if(val) tmpIsFlag |= 0x8;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static boolean toggleTmpIsHidden(mdict_manageable mdTmp) {
		return setTmpIsHidden(mdTmp,  !getTmpIsHidden(mdTmp.getTmpIsFlag()));
	}

	public static boolean getTmpIsCollapsed(int tmpIsFlag) {
		return (tmpIsFlag&0x10)!=0;
	}
	public static boolean setTmpIsCollapsed(mdict_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x10);
		if(val) tmpIsFlag |= 0x10;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static boolean toggleTmpIsCollapsed(mdict_manageable mdTmp) {
		return setTmpIsCollapsed(mdTmp, !getTmpIsCollapsed(mdTmp.getTmpIsFlag()));
	}
	
	public static int getDFFStarLevel(long tmpIsFlag) {
		return (int) ((tmpIsFlag>>20)&7);
	}
	
	public static long setDFFStarLevel(long flag, int val) {
		long valex = (val&7)<<20;
		long mask = ~(7<<20);
		flag = flag&mask|valex;
		return flag;
	}
	
	//////
	private final StringBuffer pathTo = new StringBuffer(255);
	public File rootPath;
	protected int pathToL = -1;
	public DisplayMetrics dm;
	public StringBuffer pathToDatabases() {
		return pathToMainFolder().append("bmDBs/");
	}
	File FileDatabases;
	public File fileToDatabases() {
		if(FileDatabases==null){
			FileDatabases = new File(pathToMainFolder().append("bmDBs").toString());
		}
		return FileDatabases;
	}
	private String pathToFavoriteDatabases(String name) {
		StringBuffer InternalPath = pathToMainFolder().append("INTERNAL/");
		if(name!=null){
			InternalPath.append("favorites/").append(name);
		} else {
			InternalPath.append("history.sql");
		}
		return InternalPath.toString();
	}
	public String pathToFavoriteDatabase(String name) {
		return pathToFavoriteDatabases(name);
	}
	public File fileToFavoriteDatabases(String name) {
		return new File(pathToFavoriteDatabases(name));
	}
	public File fileToDatabaseFavorites() {
		return new File(pathToFavoriteDatabases(StringUtils.EMPTY));
	}
	
	public StringBuffer pathToMainFolder() {
		if(rootPath!=null){
			pathTo.setLength(0);
			pathTo.append(rootPath).append("/").append(CMN.BrandName).append("/");
			pathToL = pathTo.length();
			rootPath=null;
		} else if(pathToL==-1) {
			rootPath=Environment.getExternalStorageDirectory();
			return pathToMainFolder();
		}
		pathTo.setLength(pathToL);
		//CMN.Log("pathToMainFolder :: ", pathTo);
		return pathTo;
	}
	

	public File fileToConfig() {
		return new File(pathToMainFolder().append("CONFIG").toString());
	}
	
	public void CheckFileToDefaultMdlibs() {
		getLastMdlibPath();
		if(lastMdlibPath==null || !lastMdlibPath.exists()) {
			lastMdlibPath = new File(pathToMainFolder().append("mdicts").toString());
			lastMdlibPath.mkdirs();
		}
	}
	
	public File fileToSet(File ConfigFile, String name) {
		return new File(ConfigFile==null?fileToConfig():ConfigFile, name);
	}
	
	public File fileToDecords(File ConfigFile) {
		if(ConfigFile==null){
			ConfigFile = fileToConfig();
		}
		return new File(ConfigFile, "mdlibs.txt");
	}
	
	public File fileToSecords(File ConfigFile) {
		if(ConfigFile==null){
			ConfigFile = fileToConfig();
		}
		return new File(ConfigFile, "AllModuleSets.txt");
	}
	
	public String pathToGlide(@NonNull Context context) {
		return defaultReader.getString("cache_p", GlideCacheModule.DEFAULT_GLIDE_PATH=context.getExternalCacheDir().getAbsolutePath()+"/thumnails/");
	}

	public Editor edit() {
		return defaultReader.edit();
	}

	public long Flag(int flagIndex) {
		switch (flagIndex){
			case 1:
			return FirstFlag;
			case 2:
			return SecondFlag;
			case 3:
			return ThirdFlag;
			case 4:
			return FourthFlag;
		}
		return tmpFlag;
	}

	public void Flag(int flagIndex, long val) {
		switch (flagIndex){
			case 1:
				FirstFlag=val;
			break;
			case 2:
				SecondFlag=val;
			break;
			case 3:
				ThirdFlag=val;
			break;
			case 4:
				FourthFlag=val;
			break;
			default:
				tmpFlag=val;
			break;
		}
	}

	XYTouchRecorder xyt;
	public XYTouchRecorder XYTouchRecorder() {
		if(xyt==null) xyt = new XYTouchRecorder();
		return xyt;
	}

	public JSONObject getDimensionalSharePatternByIndex(int position) {
		String val = defaultReader.getString("dsp#"+position, null);
		JSONObject ret = null;
		if(val!=null) {
			try {
				ret = new JSONObject(val);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public void putDimensionalSharePatternByIndex(int position, JSONObject json) {
		CMN.Log("保存", position);
		defaultReader.edit().putString("dsp#"+position, json==null||json.length()==0?null:json.toString()).apply();
	}
	
	@SuppressLint("ClickableViewAccessibility")
	public void setAsLinkedTextView(TextView tv, boolean center) {
		if(xyt==null) xyt = new XYTouchRecorder();
		tv.setOnClickListener(xyt);
		tv.setOnTouchListener(xyt);
		tv.setTextSize(GlobalOptions.isLarge?22f:17f);
		if(GlobalOptions.isLarge) {
			tv.setTextSize(tv.getTextSize());
		}
		if(center) {
			tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		}
	}
	
	public String tryGetDomesticFileName(String path) {
		String parent = lastMdlibPath.getPath();
		if(path.startsWith(parent)&&path.length()>parent.length()){
			path = path.substring(parent.length()+1);
		}
		return path;
	}
}
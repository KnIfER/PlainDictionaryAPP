package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
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
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.mngr_agent_manageable;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.XYTouchRecorder;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.knziha.metaline.Metaline;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.knziha.plod.plaindict.MainActivityUIBase.SessionFlag;

public class PDICMainAppOptions implements MdictServer.AppOptions
{
	public boolean isAudioPlaying;
	public boolean isAudioActuallyPlaying;
	public boolean supressAudioResourcePlaying;
	public static HashSet<String> ChangedMap;
	public File SpecificationFile;
	SharedPreferences defaultReader;
	public static String locale;
	
	private final Object mEditorLock = new Object();
	public final HashMap<String, Object> mModified = new HashMap<>();
	public boolean dirty;
	
	public boolean checkModified(long[] flags, boolean commit) {
		boolean fc = isFlagsChanged(flags);
		if (fc || mModified.size() > 0) {
			if (fc) {
				putFlags();
			} else {
				fc = true;
			}
			mModified.clear();
			if (tmpEditor != null) {
				if (commit) tmpEditor.commit();
				else tmpEditor.apply();
				tmpEditor = null;
			}
		}
		dirty = false;
		return fc;
	}
	
	public PDICMainAppOptions(Context a_){
		defaultReader = PreferenceManager.getDefaultSharedPreferences(a_);
		magicStr=a_.getResources().getString(R.string.defPlan);
		if (SearchUI.tapZoomWait==0) {
			SearchUI.tapZoomWait = getInt("dtm", 100);
			SearchUI.pBc.tapAlignment(IU.parsint(getString("tzby", "0"), 0));
			SearchUI.pBc.tapZoomRatio = getFloat("tzlv", 2);
			SearchUI.pBc.tapZoomXOffset = getFloat("tz_x", 0);
			SearchUI.pBc.tapZoom(true);
		}
	}
	String magicStr;
	
	public void backup() throws IOException {
		File file = new File(Environment.getExternalStorageDirectory(), "平典搜索_备份.json");
		Map<String, ?> all = defaultReader.getAll();
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		for(String key:all.keySet()) {
			Object v = all.get(key);
			if (v instanceof Long) {
				json.put("Lng_"+key, v);
			} else {
				json.put(key, v);
			}
		}
		BU.SaveToFile(json.toString(), file);
	}
	
	public void restore() throws IOException {
		File file = new File(Environment.getExternalStorageDirectory(), "平典搜索_备份.json");
		Editor preferences = defaultReader.edit();
		com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(BU.FileToString(file));
		for(String key:json.keySet()) {
			Object value = json.get(key);
			if (key.startsWith("Lng_")) {
				preferences.putLong(key.substring(4), Long.parseLong(""+value));
			}
			else if (value instanceof String) {
				preferences.putString(key, (String) value);
			}
			else if (value instanceof Long) {
				//CMN.Log("putInt::Long::", key);
				preferences.putLong(key, (Long) value);
			}
			else if (value instanceof Integer) {
				//CMN.Log("putInt::", key);
				preferences.putInt(key, (Integer) value);
			}
			else if (value instanceof Float) {
				preferences.putFloat(key, (Float) value);
			}
			else if (value instanceof Boolean) {
				preferences.putBoolean(key, (Boolean) value);
			}
		}
		preferences.apply();
	}

	public File lastMdlibPath;
	public String lastMdPlanName;
	public boolean auto_seach_on_switch=true;
	protected boolean bShouldUseExternalBrowserApp=true;
	
	public static class NightModeConfig{
		public int InvertFilter;
		public int PageColor;
		public int FontColor;
	}
	
	/**}', d=document,
	 head = d.getElementsByTagName('head')[0],
	 sty = d.createElement('style');
	 sty.id = "_PDict_Darken";
	 if(!d.getElementById(sty.id))
	 {
		 sty.class = "_PDict";
		 sty.type = 'text/css';
	 	 sty.innerText = css;
		 head.appendChild(sty);
	 }
	 if(d.body)d._pdkn=1
	 */
	@Metaline
	public final static String sDarkModeIncantation ="DARK";
	
	public String mDarkModeJs = "";
	int mDarkModeJsVer = -1;
	public static int darkModeJsVer;
	
	public String DarkModeIncantation(MainActivityUIBase a) {
		if (mDarkModeJsVer!=darkModeJsVer)
		{
			StringBuilder sb = new StringBuilder(128);
			sb.append("var css='html{");
			int initLen = sb.length();
			if (nightUseInvertFilter()) {
				sb.append("-webkit-filter:invert(")
						.append(defaultReader.getInt("dkR", 100))
						.append("%)");
			}
			if (nightDimAll()) {
				if(sb.length()==initLen)
					sb.append("-webkit-filter:");
				sb.append(" brightness(")
						.append(defaultReader.getInt("dkD", 80))
						.append("%)");
			}
			if(sb.length()>initLen) sb.append(";");
			if (nightUsePageColor()) {
				sb.append("background:#")
						.append(SU.toHexRGB(defaultReader.getInt("dkB", ViewUtils.isKindleDark()?0xFF333333:0xFFFFFFFF)))
						.append(";");
			}
			if (nightUseFontColor()) {
				sb.append("color:#")
						.append(SU.toHexRGB(defaultReader.getInt("dkF", 0xFF000000)))
						.append(";");
			}
//			if (nightUseInvertFilter() && nightPreserveImg()) {
//				int already = nightUseInvertFilter() ? defaultReader.getInt("dkR", 100) : 0;
//				sb.append("}img{-webkit-filter:invert(").append(Math.max(0, already-20)).append("%)");
//			}
			initLen = sb.length();
			if (nightImgUseInvertFilter()) {
				int invertImg = defaultReader.getInt("dkTR", 20);
				if (nightUseInvertFilter()) {
					invertImg = defaultReader.getInt("dkR", 100) - invertImg;
				}
				sb.append("}img{");
				sb.append("-webkit-filter:invert(")
						.append(invertImg)
						.append("%)");
			}
			if (nightDimImg()) {
				if(sb.length()==initLen) {
					sb.append("}img{");
					sb.append("-webkit-filter:");
				}
				sb.append(" brightness(")
						.append(defaultReader.getInt("dkTD", 80))
						.append("%)");
			}
			
			mDarkModeJs = sb.append(sDarkModeIncantation).toString();
			CMN.debug("mDarkModeJs::", mDarkModeJs);
			a.CommonAssets.put("dk.js", mDarkModeJs.getBytes());
			mDarkModeJsVer=darkModeJsVer;
		}
		return mDarkModeJs;
	}
	
	public int annotColor(int k, int val, boolean set) {
		String key = null;
		int ret = 0, def=0;
		if (k==0) {
			key = "_ant_h";
			def = 0xffffaaaa;
		}
		else if (k==1) {
			key = "_ant_u";
			def = Color.BLACK;
		}
		if (set) {
			putInt(key, val);
		} else {
			ret = getInt(key, def);
		}
		return ret;
	}
	
	public int alphaLock(int k, int val, boolean set) {
		if (set) {
			if (k==0) {
				alphaLock0(val);
			}
			else if (k==1) {
				alphaLock1(val);
			}
		} else {
			if (k==0) {
				return alphaLock0();
			}
			else if (k==1) {
				return alphaLock1();
			}
		}
		return 255;
	}
	
	Editor tmpEditor;
	public final Editor tmpEdit() {
		if(tmpEditor==null) tmpEditor = defaultReader.edit();
		return tmpEditor;
	}
	
	public int getInt(String key, int val) {
		if (mModified.size() > 0) {
			Object ret = mModified.get(key);
			if (ret != null) {
				return (Integer) ret;
			}
		}
		return defaultReader.getInt(key, val);
	}
	public PDICMainAppOptions putInt(String key, int val) {
		tmpEdit().putInt(key, val);
		mModified.put(key, val);
		dirty = true;
		return this;
	}
	
	public long getLong(String key, long def) {
		if (mModified.size() > 0) {
			Object ret = mModified.get(key);
			if (ret != null) {
				return (Long) ret;
			}
		}
		try {
			return defaultReader.getLong(key, def);
		} catch (Exception e) {
			CMN.Log(e);
			return (long) defaultReader.getInt(key, (int) def);
		}
	}
	
	public PDICMainAppOptions putLong(String key, long val) {
		try {
			if (defaultReader.getLong(key, val)==val) {
				return this;
			}
		} catch (Exception e) {
			tmpEdit().remove(key);
		}
		tmpEdit().putLong(key, val);
		mModified.put(key, val);
		dirty = true;
		return this;
	}
	
	public float getFloat(String key, float val) {
		if (mModified.size() > 0) {
			Object ret = mModified.get(key);
			if (ret != null) {
				return (Float) ret;
			}
		}
		return defaultReader.getFloat(key, val);
	}
	public PDICMainAppOptions putFloat(String key, float val) {
		tmpEdit().putFloat(key, val);
		mModified.put(key, val);
		dirty = true;
		return this;
	}
	
	public String getString(String key, String defValue) {
		if (mModified.size() > 0) {
			Object ret = null;
			if (Build.VERSION.SDK_INT<=23) {
				ret = mModified.containsKey(key)?mModified.get(key):key;
			} else {
				ret = mModified.getOrDefault(key, key);
			}
			if (ret != key) {
				return (String) ret;
			}
		}
		return defaultReader.getString(key, defValue);
	}
	public PDICMainAppOptions putString(String key, String val) {
		tmpEdit().putString(key, val);
		mModified.put(key, val);
		dirty = true;
		return this;
	}

	public String getLocale() {
		return locale!=null?locale:(locale=getString("locale",""));
	}

	public File getLastMdlibPath() {
		String path = getString("lastMdlibPath",null);
		return path==null?null:(lastMdlibPath=new File(path));
	}

	public void setLastMdlibPath(String lastMdlibPath) {
		putString("lastMdlibPath",lastMdlibPath);
	}
	public String getCurrFavoriteDBName() {//currFavoriteDBName
		return getString("DB1",null);
	}

	public void putCurrFavoriteDBName(String name) {
		putString("DB1",name);
	}

	public long getCurrFavoriteNoteBookId() {
		return getLong("NID", 0);
	}

	public void putCurrFavoriteNoteBookId(long id) {
		putLong("NID", id);
	}

	public String getLastMdFn(String key) {
		return getString(key,null);
	}
	public void putLastMdFn(String key, String name) {
		putString(key, name);
	}
	
	public void putLastVSGoNumber(int position) {
		if(getLastVSGoNumber()!=position) {
			putInt("VSGo", position);
		}
	}
	
	public int getLastVSGoNumber() {
		return getInt("VSGo", -1);
	}
	
	public int getExpandTopPageNum() {
		return getInt("expand_top", 3);
	}
	
	/** 当页面数大于返回数值时，才使用合并的url地址 */
	public int getMergeUrlForFrames() {
		return getInt("merge_min", 1);
	}
	
	public String getLastPlanName(String key) {
		return SU.legacySetFileName(lastMdPlanName=getString(key,magicStr));
	}
	public void putLastPlanName(String key, String name) {
		putString(key, lastMdPlanName=name);
	}

	public String getFontLibPath() {
		return getString("fntlb",pathToMainFolder().append("Fonts").toString());
	}
	public void setFontLibPath(String name) {
		putString("fntlb", name);
	}

	public String getAppBottomBarProject() {
		return getString("btmprj",null);
	}
	
	public String getAppContentBarProject(int idx) {
		return getAppContentBarProject("ctnp#"+idx);
	}
	
	public String getAppContentBarProject(String key) {
		String ret = getString(key, null);
		if(ret!=null && ret.startsWith("ref")){
			int ref = IU.parsint(ret.substring(3));
			if(ref<0||ref>10) return null;
			int ref_tree = 1<<ref;
			return getContextbarProjectRecursive(ref, ref_tree);
		}
		return ret;
	}
	
	private String getContextbarProjectRecursive(int idx, int ref_tree) {
		String current = getString("ctnp#"+idx, null);
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
		String current = getString("ctnp#"+idx, null);
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
		putString("ctnp#"+idx , getLinkContentBarProj()?("ref"+linkTo):getAppContentBarProject(linkTo));
	}
	
	public void putAppProject(AppUIProject projectContext) {
		putString(projectContext.key, projectContext.currentValue);
	}
	
	public void clearAppProjects(String key) {
		if(getRestoreAllBottombarProj()){
			putString("btmprj", null);
			for (int i = 0; i < 3; i++) {
				putString("ctnp#"+i, null);
			}
		} else {
			putString(key, null);
		}
	}
	
	public boolean isAppContentBarProjectRelative(int idx) {
		String ret = getString("ctnp#"+idx, null);
		return ret!=null && ret.startsWith("ref");
	}
	
	public boolean isAppContentBarProjectReferTo(String key, int ref_idx) {
		String ret = getString(key, null);
		if(ret!=null && ret.startsWith("ref")){
			int ref = IU.parsint(ret.substring(3));
			int ref_tree = 1<<ref;
			return ref_idx == getContextbarProjectRecursive1(ref, ref_tree);
		}
		return false;
	}
	

	public int getGlobalPageBackground() {
		return getInt("GPBC", Color.WHITE); //0xFFC7EDCC
	}
	public void putGlobalPageBackground(int val) {
		putInt("GPBC",val);
	}
	public int getMainBackground() {
		return getInt("BCM",Constants.DefaultMainBG);
	}
	public int getFloatBackground() {
		return getInt("BCF",Constants.DefaultMainBG);
	}
	public int getToastBackground() {
		return getInt("TTB",0xFFBFDEF8);
	}
	public int getToastColor() {
		return getInt("TTT",0xFF0D2F4B);
	}
	public int getTitlebarForegroundColor() {
		return getInt("TIF",0xFFffffff);
	}
	public int getTitlebarBackgroundColor() {
		return getInt("TIB",Constants.DefaultMainBG);
	}

	public boolean UseTripleClick() {
		return false;
	}
	
	
	public int getBottombarSize(int def) {
		return getInt("BBS", def);
	}
	public int getFloatBottombarSize(int def) {
		return getInt("FBBS", def);
	}
	public int getPeruseBottombarSize(int def) {
		return getInt("PBBS", def);
	}
	
	public final static int PLAIN_TARGET_APP_AUTO = 0;
	public final static int PLAIN_TARGET_APP_POP = 1;
	public final static int PLAIN_TARGET_APP_PERUSE = 2;
	public final static int PLAIN_TARGET_FLOAT_SEARCH = 3;
	public final static int PLAIN_TARGET_INPAGE_SEARCH = 4;
	public final static int PLAIN_TARGET_MDCCSP_SEARCH = 5;
	public final static int PLAIN_TARGET_MDCCSP_POP = 6;
	public final static int PLAIN_TARGET_MDCCSP_PERUSE = 7;
	
	/** @return integer 目标: 0=主程序 <br/>
	 * 1=主程序（点译弹窗）<br/>
	 * 2=主程序（翻阅模式）<br/>
	 * 3=浮动搜索<br/>
	 * 4=页内搜索<br/>
	 * 5=多维分享<br/>
	 * 6=多维分享（点译弹窗）<br/>
	 * 7=多维分享（翻阅模式）<br/>
	 * */
	public int getPasteTarget()
	{
		return IU.parsint(getString("tgt_paste", "0"));
	}
	/** @return integer: see {@link #getPasteTarget} */
	public int getShareToTarget()
	{
		return IU.parsint(getString("tgt_share", "0"), 0);
	}
	/** @return integer: see {@link #getPasteTarget} */
	public int getColorDictTarget()
	{
		return IU.parsint(getString("tgt_color", "3"), 3);
	}
	/** @return integer: see {@link #getPasteTarget} */
	public int getTextProcessorTarget()
	{
		return IU.parsint(getString("tgt_text", "3"), 3);
	}

	//////////////

	//public boolean isShowDirectSearch() {
	//	return defaultReader.getBoolean("sh_dir_sear", false);
	//}

	public int getDefaultFontScale(int def) {
		return getInt("f_size", def);
	}
	public void putDefaultFontScale(int def) {
		putInt("f_size", def);
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
	private static long FirstFlag=0;
	public long getFirstFlag() {
		if(FirstFlag==0) {
			return CMNF.FirstFlag=FirstFlag=getLong("MFF",0);
		}
		return FirstFlag;
	}
	
	public long FirstFlag() {
		return FirstFlag;
	}
	private static void updateFFAt(int o, boolean val) {
		FirstFlag &= (~o);
		if(val) FirstFlag |= o;
	}
	private void updateFFAt(long o, boolean val) {
		FirstFlag &= (~o);
		if(val) FirstFlag |= o;
	}

	public boolean isCombinedSearching() {//false
		return (FirstFlag & 1) == 1;
	}
	public boolean setCombinedSearching(boolean val) {
		updateFFAt(1,val);
		return val;
	}
	@Metaline(flagPos=1) public static boolean getBottombarOnBottom() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=1) public static void setBottombarOnBottom(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=2, shift=1) public static boolean getTurnPageEnabled() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=2, shift=1) public static void setTurnPageEnabled(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}

	@Metaline(flagPos=3) public static boolean getInPeruseMode() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=3) public static void setInPeruseMode(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}

	public boolean getInFloatPeruseMode() {
		return (FirstFlag & 16) == 16;
	}
	public boolean setInFloatPeruseMode(boolean val) {
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
		return true;
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
		GlobalOptions.isDark = ret;
		return ret;
	}
	public boolean setInDarkMode(boolean val) {
		GlobalOptions.isDark = val;
		if(ViewUtils.mRectPaint!=null) {
			ViewUtils.mRectPaint.setColor(GlobalOptions.isDark?0x3fffffff: ViewUtils.FloatTextBG);
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
	@Metaline(flagPos=32, shift=1) public static boolean pinPDic() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=32, shift=1) public static void pinPDic(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}
	
	//???
	
	@Metaline(flagPos=33, shift=1) public boolean autoSchPDict() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=33, shift=1) public void autoSchPDict(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}
	
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

	/** 0=pan; 1=peruse; 2=fetchWord; 3=select */
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
	public static long SecondFlag=0;
	public long getSecondFlag() {
		if(SecondFlag==0) {
			return FilePickerOptions.SecondFlag=SecondFlag=getLong("MSF",0);
		}
		return SecondFlag;
	}
	public long SecondFlag() {
		return SecondFlag;
	}
	public static void SecondFlag(long _SecondFlag) {
		SecondFlag=_SecondFlag;
	}
	private static void updateSFAt(int o, boolean val) {
		SecondFlag &= (~o);
		if(val) SecondFlag |= o;
	}
	private static void updateSFAt(long o, boolean val) {
		SecondFlag &= (~o);
		if(val) SecondFlag |= o;
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
	
	
	@Metaline(flagPos=3, debug=1) public static boolean getAdjSHShwn(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=3) public boolean togAdjSHShwn() { SecondFlag=SecondFlag; throw new IllegalArgumentException(); }

	
	//getHideScroll2 getHideScroll3 setHideScroll4
	@Metaline(flagPos=4) public static boolean showPrvBtnSmall() { SecondFlag=SecondFlag; throw new RuntimeException();}
	@Metaline(flagPos=4) public static void showPrvBtnSmall(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=5) public static boolean showNxtBtnSmall() { SecondFlag=SecondFlag; throw new RuntimeException();}
	@Metaline(flagPos=5) public static void showNxtBtnSmall(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=6, shift=1) public static boolean slidePage1D() { SecondFlag=SecondFlag; throw new RuntimeException();}
	@Metaline(flagPos=6, shift=1) public static void slidePage1D(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=7, shift=1) public static boolean slidePageMD() { SecondFlag=SecondFlag; throw new RuntimeException();}
	@Metaline(flagPos=7, shift=1) public static void slidePageMD(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException();}

	public boolean getUseLruDiskCache() {
		return (SecondFlag & 0x100) != 0x100;
	}

	/* forbid all history recording */
	@Metaline(flagPos=9) public static boolean storeNothing(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=9) public static void storeNothing(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
	
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
	@Metaline(flagPos=13, shift=1) public static boolean storeClick(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=13, shift=1) public static void storeClick(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }


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
	@Metaline(flagPos=17, flagSize=2, shift=2, max=2) public static int storePageTurn(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=17, flagSize=2, shift=2, max=2) public static void storePageTurn(int val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
	
	
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

	//kk 0x20000000l 0x40000000l
	//todo 默认关闭
	public static boolean getPasteToPeruseModeWhenFocued() {
		return (SecondFlag & 0x80000000l) != 0x80000000l;
	}
	public static boolean setPasteToPeruseModeWhenFocued(boolean val) {
		updateSFAt(0x80000000l,!val);
		return val;
	}
	
	// 0x100000000l   0x200000000l
	
	// todo
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

	@Metaline(flagPos=28) public static boolean getKeepScreen() { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=28) public static boolean getKeepScreen(long SecondFlag) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=28) public static void setKeepScreen(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
	@Metaline(flagPos=36) public static boolean getHideFloatFromRecent() { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=36) public static void setHideFloatFromRecent(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=37) public static boolean adjTBtmShown(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=37) public boolean adjTBtmShownTog() { SecondFlag=SecondFlag; throw new IllegalArgumentException(); }
	
	@Deprecated
	public boolean getPeruseUseVolumeBtn() {
		return false;//(SecondFlag & 0x2000000000l) == 0x2000000000l;
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

//	public boolean getInPageSearchVisible() {
//	public boolean setInPageSearchVisible(boolean val) { 0x100000000000l
	@Metaline(flagPos=44) public boolean schPage(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=44) public void schPage(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
	
	
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
	
	@Metaline(flagPos=47) public static boolean pageSchUseRegex(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=47) public static void pageSchUseRegex(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	//xxx 废弃
//	public static boolean getRegexAutoAddHead() {
//		return (SecondFlag & 0x1000000000000l) != 0x1000000000000l;
//	}
//	public static boolean setRegexAutoAddHead(boolean val) {
//		updateSFAt(0x1000000000000l,!val);
//		return val;
//	}
	
	// 页内搜索相关
	
	@Metaline(flagPos=48, shift=1) public static boolean pageSchWild(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Metaline(flagPos=48, shift=1) public static void pageSchWild(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Metaline(flagPos=49) public static boolean getJoniCaseSensitive(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=49) public static void setJoniCaseSensitive(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=50) public static boolean pageSchCaseSensitive(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=50) public static void pageSchCaseSensitive(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=51) public static boolean pageSchWildMatchNoSpace(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=51) public static void pageSchWildMatchNoSpace(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=52) public static boolean pageSchSplitKeys(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=52) public static void pageSchSplitKeys(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }

	@Metaline(flagPos=53) public static boolean getRebuildToast(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=53) public static void setRebuildToast(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=54) public static boolean pageSchDiacritic(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=54) public static void pageSchDiacritic(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }

	@Metaline(flagPos=55, shift=1) public static boolean schPageAutoTurn(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=55, shift=1) public static void schPageAutoTurn(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=56, shift=1) public static boolean schPageAutoType(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=56, shift=1) public static void schPageAutoType(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=57, shift=1) public static boolean schPageNavHideKeyboard(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=57, shift=1) public static void schPageNavHideKeyboard(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=58) public static boolean schPageNavAudioKey(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=58) public static void schPageNavAudioKey(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=59) public static boolean schPageShowHints(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=59) public static void schPageShowHints(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	// 60 xxx
	@Metaline(flagPos=61, shift=1) public static boolean schPageAfterFullSch(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=61, shift=1) public static void schPageAfterFullSch(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=62) public static boolean schPageAfterClick(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=62) public static void schPageAfterClick(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Metaline(flagPos=63) public static boolean exitToBackground(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=63) public static void exitToBackground(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException(); }
	///////////////////End second flag///////////////////////
	///////////////////Start Third Flag///////////////////////
	private static long ThirdFlag=0;
	public long getThirdFlag() {
		if(ThirdFlag==0) {
			return ThirdFlag=getLong("MTF",0);
		}
		return ThirdFlag;
	}
	public long ThirdFlag() {
		return ThirdFlag;
	}
	private void updateTFAt(int o, boolean val) {
		ThirdFlag &= (~o);
		if(val) ThirdFlag |= o;
	}
	private static void updateTFAt(long o, boolean val) {
		ThirdFlag &= (~o);
		if(val) ThirdFlag |= o;
	}

	/** Get Back Prevention Type. Default to 1<br/>
	 *  @return 0=exit directly; 1=show top snack; 2=toast; 3=dialog;  */
	@Metaline(flagPos=0, flagSize=2, shift=1) public static int getBackPrevention() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	/** Set Back Prevention Type <br/> see {@link #getBackPrevention}*/
	@Metaline(flagPos=0, flagSize=2, shift=1) public static void setBackPrevention(int val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	
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

	public static boolean getPinTapTranslator() {
		return (ThirdFlag & 0x40l) == 0x40l;
	}
	public static boolean setPinTapTranslator(boolean val) {
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

	@Metaline(flagPos=8) public static boolean schPageFlt(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Metaline(flagPos=8) public static void schPageFlt(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException(); }

	public static boolean getDoubleClickMaximizeClickSearch() {
		return false;//(ThirdFlag & 0x200l) != 0x200l;
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

	//xxx
	/** persist 底栏前后切换按钮之功能为网页的前后导航。对主界面等有效。see {@link WebViewListHandler#bShowingInPopup} */
	@Metaline(flagPos=16) public static boolean bottomNavWeb1(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Metaline(flagPos=16) public static void bottomNavWeb1(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException(); }


//	public static boolean getSwichClickSearchDictOnBottom() {
//		return (ThirdFlag & 0x20000l) != 0x20000l;
//	}
//	public static boolean setSwichClickSearchDictOnBottom(boolean val) {
//		updateTFAt(0x20000l,!val);
//		return val;
//	}

	public static boolean getSwichClickSearchDictOnNav() {
		return (ThirdFlag & 0x40000l) == 0x40000l;
	}
	public static boolean setSwichClickSearchDictOnNav(boolean val) {
		updateTFAt(0x40000l,val);
		return val;
	}
	
	@Metaline(flagPos=19, shift=1) public static boolean showPrvBtn() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=19, shift=1) public static void showPrvBtn(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}

	
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

 	// 22 23
	
	public static int calcPseudoCode(int input) {
		//CMN.Log("calcPseudoCode::", input);
		// 1721624788 -> 31
		// -1143300572 ( debug )
		if(input%73==0xf&&input%101==0x63) {
			return input%64+0xb;
		}
		return input%0xf;
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
	
	@Metaline(flagPos=33, shift=1) public static boolean slidePageMd() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=33, shift=1) public static void slidePageMd(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	
	
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

	@Metaline(flagPos=35, shift=1) public boolean tapSch() { ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Metaline(flagPos=35, shift=1) public void tapSch(boolean v) { ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Metaline(flagPos=35, shift=1) public boolean togTapSch() { ThirdFlag=ThirdFlag; throw new IllegalArgumentException(); }
	
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


	@Metaline(flagPos=45) public static boolean tapSchAutoReadEntry() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=45) public static void tapSchAutoReadEntry(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=46) public static boolean getUseBackKeyGoWebViewBack() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=46) public static void setUseBackKeyGoWebViewBack(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=47, shift=1) public static boolean getLazyLoadDicts() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=47, shift=1) public static void setLazyLoadDicts(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=48) public static boolean getEnableWebDebug() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=48) public static void setEnableWebDebug(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=49) public static boolean tapSchPageAutoReadEntry() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=49) public static void tapSchPageAutoReadEntry(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}

	@Metaline(flagPos=50, shift=1) public static boolean wvShowToolsBtn() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=50, shift=1) public static void wvShowToolsBtn(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}

	@Metaline(flagPos=51, shift=1) public static boolean tapSchShowToolsBtn() { ThirdFlag=ThirdFlag; throw new RuntimeException();}
	@Metaline(flagPos=51, shift=1) public static void tapSchShowToolsBtn(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException();}

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

//	public boolean getToTextShare() {
//		return (ThirdFlag & 0x20000000000000l) == 0x20000000000000l;
//	}
//	public boolean setToTextShare(boolean val) {
//		updateTFAt(0x20000000000000l,val);
//		return val;
//	} /// 废弃

	@Metaline(flagPos=54) public boolean schPageFye(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Metaline(flagPos=54) public void schPageFye(boolean val) { ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	
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

//	public boolean getToTextShare2() {
//		return (ThirdFlag & 0x200000000000000l) == 0x200000000000000l;
//	}
//	public boolean setToTextShare2(boolean val) {
//		updateTFAt(0x200000000000000l,val);
//		return val;
//	} // 废弃

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
	private static long FourthFlag=0;
	public long getFourthFlag() {
		if(FourthFlag==0) {
			return FourthFlag=getLong("MQF",0);
		}
		return FourthFlag;
	}
	public static long getFourthFlag(Context context) {
		if(FourthFlag==0) {
			return FourthFlag= androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getLong("MQF",0);
		}
		return FourthFlag;
	}
	public long FourthFlag() {
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

	// 22 23

	public static boolean getFloatClickHideToBackground() {
		return (FourthFlag & 0x80000l) != 0x80000l;
	}
	public static boolean setFloatClickHideToBackground(boolean val) {
		updateQFAt(0x80000l,!val);
		return val;
	}
	
	@Metaline(flagPos=20) public static boolean getSimpleMode() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=20) public static boolean getSimpleMode(long FourthFlag) { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=20) public static void setSimpleMode(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
 
	public static boolean getEnsureAtLeatOneExpandedPage() {
		return (FourthFlag & 0x200000l) != 0x200000l;
	}
	public static boolean setEnsureAtLeatOneExpandedPage(boolean val) {
		updateQFAt(0x200000l,!val);
		return val;
	}
	
	// 0x400000l
	@Metaline(flagPos=22) public static boolean getOnlyExpandTopPage() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=22) public static void setOnlyExpandTopPage(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}


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
		return true;//(FourthFlag & 0x2000000l) != 0x2000000l;
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
	
	@Metaline(flagPos=27) public static boolean getEnableSuperImmersiveScrollMode() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=27) public static void setEnableSuperImmersiveScrollMode(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}

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
	
	@Metaline(flagPos=49, debug=1) public static boolean getForceFloatSingletonSearch() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=49, debug=1) public static boolean getForceFloatSingletonSearch(long FourthFlag) { FourthFlag=FourthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=50) public static boolean isSingleThreadServer() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=51) public static boolean getServerStarted() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=51) public static void setServerStarted(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=52, shift=1) public static boolean checkVersionBefore_4_0() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=52, shift=1) public static void uncheckVersionBefore_4_0(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=52, shift=1) public static boolean checkVersionBefore_5_2() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=52, shift=1) public static void uncheckVersionBefore_5_2(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=52) public static boolean pinPDicWrd() { FourthFlag=FourthFlag; throw new RuntimeException();}
	@Metaline(flagPos=52) public static void pinPDicWrd(boolean val) { FourthFlag=FourthFlag; throw new RuntimeException();}
	
	
	//@Metaline(flagPos=52, shift=1) public static boolean uncheckVersionBefore_4_0() { FourthFlag=FourthFlag; throw new IllegalArgumentException();}
	@Metaline(flagPos=53) public static boolean getClearTasksOnExit() { FourthFlag=FourthFlag; throw new RuntimeException();}

	@Metaline(flagPos=54) public boolean getRememberVSPanelGo(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=54) public void setRememberVSPanelGo(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=55) public boolean getVSPanelGOTransient(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=55) public void setVSPanelGOTransient(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=56, shift=1) public boolean getPinVSDialog(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=56, shift=1) public void setPinVSDialog(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=57, shift=1) public boolean getPrvNxtDictSkipNoMatch(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=58) public boolean getDelayContents(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=58) public void setDelayContents(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=59, shift=1) public boolean getAnimateContents(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=59, shift=1) public static void setAnimateContents(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=61, shift=1) public boolean getLeaveContentBlank(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=61, shift=1) public static void setLeaveContentBlank(boolean val){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=60, shift=1) public boolean getDimScrollbarForPrvNxt(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=61, shift=1) public boolean getAutoAdjustFloatBottomBar(){ FourthFlag=FourthFlag; throw new RuntimeException(); }
	
	//EQ
	///////////////////// End Quart Flag////////////////////////////////////
	/////////////////////Start Fifth Flag///////////////////////////////////
	//SF
	private static long FifthFlag=0;
	public long getFifthFlag() {
		if(FifthFlag==0) {
			return FifthFlag=getLong("MVF",0);
		}
		return FifthFlag;
	}
	public long FifthFlag() {
		return FifthFlag;
	}
	
	@Metaline(flagPos=0, max=3, flagSize=5, shift=1) public static int getSendToAppTarget(){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=0, max=3, flagSize=5, shift=1) public static void setSendToAppTarget(int val){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	
	public int getSendToShareTarget(){ return IU.parsint(getString("share_to", null), 1); }
	
	
	@Metaline(flagPos=10, shift=1) public static boolean getAdjustScnShown(){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=10, shift=1) public boolean togAdjScnShwn() { FifthFlag=FifthFlag; throw new IllegalArgumentException(); }
	
	
	@Metaline(flagPos=11, shift=1) public static boolean checkVersionBefore_4_9() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=11, shift=1) public static void uncheckVersionBefore_4_9(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=11, shift=1) public static boolean checkVersionBefore_5_3() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=11, shift=1) public static void uncheckVersionBefore_5_3(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=11) public static boolean pinPDicFlt() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=11) public static void pinPDicFlt(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	
	@Metaline(flagPos=12) public boolean getFavoritePerceptsRemoveAll() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=13) public boolean getFavoritePerceptsAll() { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=14, shift=0) public static boolean getUseDatabaseV2() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=14, shift=0) public static void setUseDatabaseV2(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	
	@Metaline(flagPos=15, shift=1) public static boolean checkVersionBefore_5_0() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=15, shift=1) public static void uncheckVersionBefore_5_0(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=15) public static boolean pinPDicWrdShow() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=15) public static void pinPDicWrdShow(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	
	@Metaline(flagPos=16) public boolean getAlwaysShowScrollRect() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=16) public void setAlwaysShowScrollRect(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Deprecated @Metaline(flagPos=17, debug=0) public boolean getCacheCurrentGroup() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Deprecated @Metaline(flagPos=17, debug=0) public void setCacheCurrentGroup(boolean value) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=18, debug=0) public boolean getAutoBuildIndex() { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	public static boolean bCheckVersionBefore_5_4;
	@Metaline(flagPos=19, shift=1) public static boolean checkVersionBefore_5_4() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=19, shift=1) public static void uncheckVersionBefore_5_4(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=20, shift=1/*, debug=0*/) public static boolean getPowerSavingPageSideView() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=20, shift=1) public static void setPowerSavingPageSideView(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=21, shift=1) public static boolean getAutoEnableNotification() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=21, shift=1) public static void setAutoEnableNotification(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=22) public static boolean getForceDaemonMusic() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=22) public static void setForceDaemonMusic(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=23) public static boolean getForceDaemonWifi() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=23) public static void setForceDaemonWifi(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	/**自动判断保活方式，在常驻通知栏的基础上，
	 * 当处于省电模式（且开启了局域网服务器）时，自动选择加上后台播放空白音频、防止wifi休眠的保活措施。*/
	@Metaline(flagPos=24) public static boolean getAutoDaemonMW() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=24) public static void setAutoDaemonMW(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=25, shift=1) public static boolean getAutoClearNotificationOnExit() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=25, shift=1) public static void setAutoClearNotificationOnExit(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=26) public static boolean getNotificationEnabled() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=26) public static void setNotificationEnabled(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=27, shift=1) public static boolean showDictName(){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=27, shift=1) public static void showDictName(boolean val){ FifthFlag=FifthFlag; throw new RuntimeException(); }

	@Metaline(flagPos=28) public static boolean getShowNotificationExitBtn() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=28) public static void setShowNotificationExitBtn(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=29) public static boolean getShowNotificationSubtitle() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=29) public static void setShowNotificationSubtitle(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=30, shift=1) public static boolean getShowNotificationSettings() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=30, shift=1) public static void setShowNotificationSettings(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=31, shift=1) public static boolean getAllowPlugCss() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=31, shift=1) public static void setAllowPlugCss(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=32, shift=1) public static boolean getAllowPlugRes() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=32, shift=1) public static void setAllowPlugRes(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=33, shift=1) public static boolean getAllowPlugResNone() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=33, shift=1) public static void setAllowPlugResNone(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=34, shift=1) public static boolean getAllowPlugResSame() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=34, shift=1) public static void setAllowPlugResSame(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=35) public static boolean getLockStartOrientation() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=35) public static void getLockStartOrientation(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=36, flagSize=4, shift=7, max=8) public static int getUserOrientation() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=36, flagSize=4, shift=7, max=8) public static void setUserOrientation(int val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	/** for using the translator and save memory in the combined search mode. 联合搜索列表展示单个页面时使类似单本搜索与模糊搜索 <br>
	 *  旧模式下，单本词典内容通过高度填充界面高度的单个webview显示，多本词典内容通过高度适应内容高度的多个webview显示。 <br>
	 * */
	@Metaline(flagPos=40, shift=1) public static boolean getLv2JointOneAsSingle() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=40, shift=1) public static void setLv2JointOneAsSingle(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=41, shift=1) public static boolean showEntrySeekbar(){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=41, shift=1) public static void showEntrySeekbar(boolean val){ FifthFlag=FifthFlag; throw new RuntimeException(); }
	
	/** webx是否不使用合并的单页面URL。see {@code #getUseMergedFrame()} */
	@Metaline(flagPos=42, shift=1) public static boolean getMergeExemptWebx() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=42, shift=1) public static boolean setMergeExemptWebx(boolean v) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	/** 单本词典是否使用公共的MergedFrame。see {@code getMergeUrlForFrames()} */
	@Metaline(flagPos=43, shift=1) public static boolean getUseSharedFrame() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=43, shift=1) public static void setUseSharedFrame(boolean v) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=44) public static boolean fastPreviewFragile() { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=45, shift=1) public static boolean getShowPinPicBook() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=45, shift=1) public static void setShowPinPicBook(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=46, shift=1) public static boolean getShowSearchTools() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=46, shift=1) public static void setShowSearchTools(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=47, shift=1) public static boolean getMergePeruseBottombars() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=47, shift=1) public static void setMergePeruseBottombars(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	
	@Metaline(flagPos=48, shift=1) public static boolean getTapSkipWebxUnlessIsDedicated() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=48, shift=1) public static void setTapSkipWebxUnlessIsDedicated(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=49, shift=1) public static boolean getTapTreatTranslatorAsDedicated() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=49, shift=1) public static void setTapTreatTranslatorAsDedicated(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=50, shift=1) public static boolean getTwoColumnSetView() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=50, shift=1) public static void setTwoColumnSetView(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=51, shift=1) public static boolean getTwoColumnJumpList() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=51, shift=1) public static void setTwoColumnJumpList(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	/** 0=单本搜索； 1=联合屏风； 2=联合合并 */
	@Metaline(flagPos=52, flagSize=2, max=2) public static int tapSchMode() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=52, flagSize=2, max=2) public static void tapSchMode(int val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=54, shift=1) public static boolean checkVersionBefore_5_7() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=54, shift=1) public static void uncheckVersionBefore_5_7(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=55, shift=1) public static boolean remMultiview() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=55, shift=1) public static void remMultiview(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=56) public static boolean mergeUrlMore() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=56) public static void mergeUrlMore(boolean val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=57, shift=1) public static boolean fastPreview() { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	/** 多页面显示方式：<br/>
	 * 是否使用：0=旧模式  1=合并的多页面模式  2=屏风模式。see {@code #getMergeUrlForFrames()} */
	@Metaline(flagPos=58, flagSize=2, shift=1) public static int multiViewMode() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=58, flagSize=2, shift=1) public static void multiViewMode(int val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
//	/** 0=文本 1=选择 2=网址 3=合并 */
//	@Metaline(flagPos=59, flagSize=2) public static int shareTextOrUrl() { FifthFlag=FifthFlag; throw new RuntimeException();}
	@Metaline(flagPos=59, flagSize=2) public static void shareTextOrUrl(int val) { FifthFlag=FifthFlag; throw new RuntimeException();}
	
	
	//EF
	///////////////////// End Fifth Flag////////////////////////////////////
	/////////////////////Start Sixth Flag///////////////////////////////////
	//SS
	private static long SixthFlag=0;
	public long getSixthFlag() {
		if(SixthFlag==0) {
			return SixthFlag=getLong("MVIF",0);
		}
		return SixthFlag;
	}
	public final long SixthFlag() {
		return SixthFlag;
	}
	
	@Metaline(flagPos=0, shift=1) public static boolean popViewEntry() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=0, shift=1) public static void popViewEntry(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=1) public static boolean popViewEntryOne() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=1) public static void popViewEntryOne(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=2) public static boolean popViewEntryMulti() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=2) public static void popViewEntryMulti(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	/** 调试网页版资源。在此模式下，直接电脑上修改服务器资源文件，手机端刷新即可看到效果。需要JavaFx桌面版本配合，打开asset/liba.mdx，ip地址填对。*/
	@Metaline(flagPos=3, shift=1) public static boolean debug() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=3, shift=1) public static void debug(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=4, shift=1) public static boolean popuploadUrl() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=5) public static boolean alwaysloadUrl() { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=4, shift=1) public static boolean loadUrlPopup() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=5) public static boolean loadUrlOne() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=6) public static boolean loadUrlMulti() { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=7, shift=1) public static boolean restoreLastSch() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=7, shift=1) public static void restoreLastSch(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=8) public static boolean fyeGridPad() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=8) public static void fyeGridPad(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=9) public static boolean fyeGridBot() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=9) public static void fyeGridBot(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	/** 0=始终关闭, 1=始终开启, 2=记忆 */
	@Metaline(flagPos=10, flagSize=2, shift=2) public static int historyAutoShow() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=10, flagSize=2, shift=2) public static void historyAutoShow(int v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=12) public static boolean historyShow() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=12) public static void historyShow(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=13) public static boolean historyShowFye() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=13) public static void historyShowFye(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=14, shift=1) public static boolean fyeDictAutoScroll() { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=15) public static boolean delaySchLvHardSoftUI() { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=16, shift=1) public static boolean etSchAlwaysHard() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=17) public static boolean etSchExitTop() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=17) public static boolean fyeTogEntry() { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	/* forbid all history recording */
	@Metaline(flagPos=18) public static boolean storeNothingButSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=18) public static void storeNothingButSch(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=19, shift=1) public static boolean dbShowIcon(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=19, shift=1) public static void dbShowIcon(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=20, shift=1) public static boolean storeIcon(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=20, shift=1) public static void storeIcon(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=21, shift=1) public static boolean storeAppId(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=21, shift=1) public static void storeAppId(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=22) public static boolean menuOverlapAnchor(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=22) public static void menuOverlapAnchor(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=23) public static boolean fyeKeepGroup(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=23) public static void fyeKeepGroup(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=24) public static boolean fyeKeepBook(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=24) public static void fyeKeepBook(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=25, shift=1) public static boolean fyeRemPos(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=25, shift=1) public static void fyeRemPos(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=26) public static boolean fyeRemScale(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=26) public static void fyeRemScale(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=27, shift=1) public static boolean schPageOnEdit(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=27, shift=1) public static void schPageOnEdit(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=28, shift=1) public static boolean fyeTapSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=28, shift=1) public static void fyeTapSch(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=29) public static boolean fyePaused(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=29) public static void fyePaused(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	
	@Metaline(flagPos=0) public static boolean floatBtn(long flag){ flag=flag; throw new RuntimeException(); }
	
	@Metaline(flagPos=30) public static boolean floatBtn(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=30) public static void floatBtn(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=31) public static boolean floatBtnFye(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=31) public static void floatBtnFlt(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=32) public static boolean floatBtnMtd(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=32) public static void floatBtnMtd(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=33) public static boolean tapZoomGlobal(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=33) public static void tapZoomGlobal(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=34) public static boolean tapZoomTapSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=34) public static void tapZoomTapSch(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=35) public static boolean tapZoomFye(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=35) public static void tapZoomFye(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=36, shift=1) public static boolean turnPageTapSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=36, shift=1) public static void turnPageTapSch(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=37, shift=1) public static boolean turnPageFye(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=37, shift=1) public static void turnPageFye(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	/** persist 底栏前后切换按钮之功能为网页的前后导航。仅对弹出的内容视图有效。see {@link WebViewListHandler#bShowingInPopup} */
	@Metaline(flagPos=38) public static boolean bottomNavWeb(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=38) public static void bottomNavWeb(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=39) public static boolean adjTToolsShown(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=39) public boolean adjTToolsShownTog() { SixthFlag=SixthFlag; throw new IllegalArgumentException(); }
	
	@Metaline(flagPos=40) public static boolean toolsQuick(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=41, flagSize=6) public static int toolsQuickAction(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=41, flagSize=6) public static void toolsQuickAction(int val) { SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	/** 按钮长按操作，0=无 1=直接 2=定制 3=面板 */
	@Metaline(flagPos=47, flagSize=2) public static int toolsQuickLong(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=47, flagSize=2) public static void toolsQuickLong(int val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=49, shift=1) public static boolean toolsBoost(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=49, shift=1) public static void toolsBoost(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=50, shift=1) public static boolean showNxtBtn() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=50, shift=1) public static void showNxtBtn(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException();}

	@Metaline(flagPos=51) public static boolean showZoomBtn() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=51) public static void showZoomBtn(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=52) public static boolean showPrvNxtBtnSmallTapSch() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=52) public static void showPrvNxtBtnSmallTapSch(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
//	@Metaline(flagPos=53) public static boolean foldingScreenTapSch() { SixthFlag=SixthFlag; throw new RuntimeException();}
//	@Metaline(flagPos=53) public static void foldingScreenTapSch(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=54) public static boolean showEntrySeekbarTapSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=54) public static void showEntrySeekbarTapSch(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }

	@Metaline(flagPos=55) public static boolean showEntrySeekbarTapSchFolding(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=55) public static void showEntrySeekbarTapSchFolding(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }

	@Metaline(flagPos=56) public static boolean foldingScreenTapSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=56) public static void foldingScreenTapSch(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	/** 允许先滑动屏风 */
	@Metaline(flagPos=57, shift=1) public static boolean slidePageFd() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=57, shift=1) public static void slidePageFd(boolean val) { SixthFlag=SixthFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=58, shift=1) public static boolean showEntrySeekbarFolding(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=58, shift=1) public static void showEntrySeekbarFolding(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }

	@Metaline(flagPos=59, shift=1) public static boolean showEntrySeek(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=59, shift=1) public static void showEntrySeek(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }

	@Metaline(flagPos=60, shift=1) public static boolean padBottom(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=60, shift=1) public static void padBottom(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=61) public static boolean adjFltBtnShown(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=61) public boolean adjFltBtnTog() { SixthFlag=SixthFlag; throw new IllegalArgumentException(); }

	@Metaline(flagPos=62) public static boolean adjPstBtnShown(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	@Metaline(flagPos=62) public boolean adjPstBtnTog() { SixthFlag=SixthFlag; throw new IllegalArgumentException(); }
	
	@Metaline(flagPos=63) public static boolean showBubbleForFootNote() { SixthFlag=SixthFlag; throw new RuntimeException();}
	@Metaline(flagPos=63) public static void showBubbleForFootNote(boolean v) { SixthFlag=SixthFlag; throw new RuntimeException();}

//	@Metaline(flagPos=51) public static boolean entrySeekbarTapSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
//	@Metaline(flagPos=51) public static void entrySeekbarTapSch(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
//
//	@Metaline(flagPos=52) public static boolean dictName(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
//	@Metaline(flagPos=52) public static void dictName(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
//
//	@Metaline(flagPos=52) public static boolean dictNameFye(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
//	@Metaline(flagPos=52) public static void dictNameFye(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
//
//	@Metaline(flagPos=52) public static boolean dictNameTapSch(){ SixthFlag=SixthFlag; throw new RuntimeException(); }
//	@Metaline(flagPos=52) public static void dictNameTapSch(boolean val){ SixthFlag=SixthFlag; throw new RuntimeException(); }
	
	//EF
	///////////////////// End Sixth Flag////////////////////////////////////
	/////////////////////Start Seven Flag///////////////////////////////////
	//SS
	private static long SevenFlag=0;
	public long getSevenFlag() {
		if(SevenFlag==0) {
			return SevenFlag=getLong("M7F",0);
		}
		return SevenFlag;
	}
	public final long SevenFlag() {
		return SevenFlag;
	}
	
	@Metaline(flagPos=0) public static boolean darkSystem() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=0) public static void darkSystem(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=1) public static boolean dbTextSelectable() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=1) public static void dbTextSelectable(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=2, flagSize=2, shift=2, max=2/*, min=1*/) public static int dbFetchWord() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=2, flagSize=2, shift=2, max=2/*, min=1*/) public static void dbFetchWord(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=4) public static boolean dbCntFetcingWord() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=4) public static void dbCntFetcingWord(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	// 旧搜索列表的预览设置
	/** 是否启在列表中预览词条释义 */
	@Metaline(flagPos=5) public static boolean listPreviewEnabled() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=5) public static void listPreviewEnabled(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 三档预览色 */
	@Metaline(flagPos=6, flagSize=2, shift=1, max=2) public static int listPreviewColor() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=6, flagSize=2, shift=1, max=2) public static void listPreviewColor(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 通读模式 */
	@Metaline(flagPos=8) public static boolean listOverreadMode() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=8) public static void listOverreadMode(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 预览文本可选 */
	@Metaline(flagPos=9) public static boolean listPreviewSelectable() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=9) public static void listPreviewSelectable(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 三档预览字体大小 */
	@Metaline(flagPos=10, flagSize=2, shift=1, max=2) public static int listPreviewFont() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=10, flagSize=2, shift=1, max=2) public static void listPreviewFont(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 新旧列表使用同一套配置 */
	@Metaline(flagPos=12, shift=1) public static boolean listPreviewSet01Same() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=12, shift=1) public static void listPreviewSet01Same(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	// 新搜索列表的预览设置
	/** 是否启在列表中预览词条释义 */
	@Metaline(flagPos=13, shift=1) public static boolean listPreviewEnabled1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=13, shift=1) public static void listPreviewEnabled1(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 是否从原词典获取释义 */
	@Metaline(flagPos=14) public static boolean listPreviewOriginal1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=14) public static void listPreviewOriginal1(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 三档预览色 */
	@Metaline(flagPos=15, flagSize=2, shift=1, max=2) public static int listPreviewColor1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=15, flagSize=2, shift=1, max=2) public static void listPreviewColor1(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 通读模式 */
	@Metaline(flagPos=17) public static boolean listOverreadMode1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=17) public static void listOverreadMode1(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 三档预览长度（字节数） */
	@Metaline(flagPos=18, flagSize=2, shift=1, max=2) public static int listPreviewSize1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=18, flagSize=2, shift=1, max=2) public static void listPreviewSize1(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 预览文本可选 */
	@Metaline(flagPos=20) public static boolean listPreviewSelectable1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=20) public static void listPreviewSelectable1(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 三档预览字体大小 */
	@Metaline(flagPos=21, flagSize=2, shift=1, max=2) public static int listPreviewFont1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=21, flagSize=2, shift=1, max=2) public static void listPreviewFont1(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	// 列表中词典名称显示位置
	/** 必要时显示词典名称 */
	@Metaline(flagPos=23, shift=1) public static boolean listShowBookName() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=23, shift=1) public static void listShowBookName(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	/** 显示位置 0=预览之后 1=预览之前 2=预览之上 3=预览之下 */
	@Metaline(flagPos=24, flagSize=2) public static int listPreviewBookNamePos() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=24, flagSize=2) public static void listPreviewBookNamePos(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=26) public static boolean getAdjustLstPreviewShown(){ SevenFlag=SevenFlag; throw new RuntimeException(); }
	@Metaline(flagPos=26) public boolean togAdjustLstPreviewShown() { SevenFlag=SevenFlag; throw new IllegalArgumentException(); }
	
	@Metaline(flagPos=27, shift=1) public static boolean listShowBookNameBlow() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=27, shift=1) public static void listShowBookNameBlow(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=28, flagSize = 2) public static int schGroup() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=28, flagSize = 2) public static void schGroup(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=30) public static boolean lastUsingInternalStorage() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=30) public static void lastUsingInternalStorage(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=31, flagSize=3, max=2) public static int currentTool() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=31, flagSize=3, max=2) public static void currentTool(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=34, flagSize=8, shift=200) public static int alphaLock0() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=34, flagSize=8, shift=200) public static void alphaLock0(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=42, flagSize=8, shift=255) public static int alphaLock1() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=42, flagSize=8, shift=255) public static void alphaLock1(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=50, shift=1) public static boolean forceAlphaLock() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=50, shift=1) public static void forceAlphaLock(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=51, shift=1) public static boolean alphaLockVisible() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=51, shift=1) public static void alphaLockVisible(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=52, shift=1) public static boolean editNote() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=52, shift=1) public static void editNote(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	/** 0,1=时间 2,3=词典页码段落 4,5=词典页码时间 6,7=词典时间 */
	@Metaline(flagPos=53, flagSize=3) public static int annotDBSortBy() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=53, flagSize=3) public static void annotDBSortBy(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	/** 0,1=时间 2,3=页码段落 4,5=页码时间 */
	@Metaline(flagPos=56, flagSize=3) public static int annotDB1SortBy() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=56, flagSize=3) public static void annotDB1SortBy(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	/** 0,1=时间 2,3=页码段落 4,5=页码时间 */
	@Metaline(flagPos=59, flagSize=3) public static int annotDB2SortBy() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=59, flagSize=3) public static void annotDB2SortBy(int v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=63) public static boolean showBubbleForEmbedNote() { SevenFlag=SevenFlag; throw new RuntimeException();}
	@Metaline(flagPos=63) public static void showBubbleForEmbedNote(boolean v) { SevenFlag=SevenFlag; throw new RuntimeException();}
	
	
	/////////////////////End Seven Flag///////////////////////////////////
	/////////////////////Start Eight Flag///////////////////////////////////
	//SE
	private static long EightFlag=0;
	public long getEightFlag() {
		if(EightFlag==0) {
			return EightFlag=getLong("M8F",0);
		}
		return EightFlag;
	}
	public final long EightFlag() {
		return EightFlag;
	}
	
	@Metaline(flagPos=0, flagSize=3) public static int currentNoteType() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=0, flagSize=3) public static void currentNoteType(int v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	/** display notes on bubble directly  */
	@Metaline(flagPos=4) public static boolean noteInBubble() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=4) public static void noteInBubble(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=5, shift=1) public static boolean colorSameForNoteTypes() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=5, shift=1) public static void colorSameForNoteTypes(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=6, shift=1) public static boolean tapEditAnteNote() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=6, shift=1) public static void tapEditAnteNote(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=7, shift=1) public static boolean strechImmersiveMode() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=7, shift=1) public static void strechImmersiveMode(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=8, shift=1) public static boolean ImmersiveForContentsOnly() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=8, shift=1) public static void ImmersiveForContentsOnly(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=9) public static boolean resetImmersiveScrollOnEnter() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=9) public static void resetImmersiveScrollOnEnter(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=10, shift=1) public static boolean resetImmersiveScrollOnExit() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=10, shift=1) public static void resetImmersiveScrollOnExit(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=11) public static boolean quickTranslatorV1() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=11) public static void quickTranslatorV1(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=12, shift=1) public static boolean nightUseInvertFilter() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=12, shift=1) public static void nightUseInvertFilter(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=13) public static boolean nightUsePageColor() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=13) public static void nightUsePageColor(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=14) public static boolean nightUseFontColor() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=14) public static void nightUseFontColor(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=15) public static boolean nightPreserveImg() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=15) public static void nightPreserveImg(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=16) public static boolean nightDimAll() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=16) public static void nightDimAll(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=17) public static boolean nighAvoidTurnPicFlicker() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=17) public static void nighAvoidTurnPicFlicker(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=18, shift=1) public static boolean wordPopupAllowDifferentSet() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=18, shift=1) public static void wordPopupAllowDifferentSet(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=19) public static boolean wordPopupRemDifferenSet() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=19) public static void wordPopupRemDifferenSet(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=20, shift=1) public static boolean padLeft(){ EightFlag=EightFlag; throw new RuntimeException(); }
	@Metaline(flagPos=20, shift=1) public static void padLeft(boolean val){ EightFlag=EightFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=21, shift=1) public static boolean padRight(){ EightFlag=EightFlag; throw new RuntimeException(); }
	@Metaline(flagPos=21, shift=1) public static void padRight(boolean val){ EightFlag=EightFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=22, shift=1) public static boolean nightImgUseInvertFilter() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=22, shift=1) public static void nightImgUseInvertFilter(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=23, shift=1) public static boolean nightDimImg() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=23, shift=1) public static void nightDimImg(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=24, shift=1) public static boolean schpageAutoKeyboard() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=24, shift=1) public static void schpageAutoKeyboard(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=25) public static boolean schpageAtBottom() { EightFlag=EightFlag; throw new RuntimeException();}
	@Metaline(flagPos=25) public static void schpageAtBottom(boolean v) { EightFlag=EightFlag; throw new RuntimeException();}
	
	
	///////
	///////
	public static void setTmpIsFlag(BookPresenter mdTmp, int val) {
		mdTmp.tmpIsFlag=val;
	}
	public static void setTmpIsFlag(mngr_agent_manageable mmTmp, int val) {
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
	public static boolean setTmpIsFiler(mngr_agent_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x1);
		if(val) tmpIsFlag |= 0x1;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static int setTmpIsFiler(int tmpIsFlag, boolean val) {
		tmpIsFlag &= (~0x1);
		if(val) tmpIsFlag |= 0x1;
		return tmpIsFlag;
	}
	public static boolean toggleTmpIsFiler(mngr_agent_manageable mdTmp) {
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
	public static boolean setTmpIsClicker(mngr_agent_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x2);
		if(val) tmpIsFlag |= 0x2;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static int setTmpIsClicker(int tmpIsFlag, boolean val) {
		tmpIsFlag &= (~0x2);
		if(val) tmpIsFlag |= 0x2;
		return tmpIsFlag;
	}
	public static boolean toggleTmpIsClicker(mngr_agent_manageable mdTmp) {
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
	public static boolean setTmpIsAudior(mngr_agent_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x4);
		if(val) tmpIsFlag |= 0x4;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static int setTmpIsAudior(int tmpIsFlag, boolean val) {
		tmpIsFlag &= (~0x4);
		if(val) tmpIsFlag |= 0x4;
		return tmpIsFlag;
	}
	public static boolean toggleTmpIsAudior(mngr_agent_manageable mdTmp) {
		return setTmpIsAudior(mdTmp,  !getTmpIsAudior(mdTmp.getTmpIsFlag()));
	}
	public static boolean getTmpIsHidden(int tmpIsFlag) {
		return (tmpIsFlag&0x8)!=0;
	}
	public static int setTmpIsHidden(int flag, boolean val) {
		flag &= (~0x8);
		if(val) flag |= 0x8;
		return flag;
	}

	public static boolean getTmpIsCollapsed(int tmpIsFlag) {
		return (tmpIsFlag&0x10)!=0;
	}
	public static boolean setTmpIsCollapsed(mngr_agent_manageable mmTmp, boolean val) {
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		tmpIsFlag &= (~0x10);
		if(val) tmpIsFlag |= 0x10;
		mmTmp.setTmpIsFlag(tmpIsFlag);
		return val;
	}
	public static int setTmpIsCollapsed(int tmpIsFlag, boolean val) {
		tmpIsFlag &= (~0x10);
		if(val) tmpIsFlag |= 0x10;
		return tmpIsFlag;
	}
	public static boolean toggleTmpIsCollapsed(mngr_agent_manageable mdTmp) {
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
	
	@Metaline(flagPos=0) public boolean getIgnoreReloadWarning(){MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=1) public boolean getReloadWebView(){MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=0, flagSize=8) public static int getPseudoInitCode(){MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=2, flagSize=6) public static int getPseudoInitCodeEu(){MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=2, flagSize=6) public static void setPseudoInitCode(int value){MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=12, flagSize=4, shift=7, max=8, log=1) public static int getTmpUserOrientation() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=12, flagSize=4, shift=7, max=8) public static void setTmpUserOrientation(int val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=16, flagSize=4, shift=7, max=8) public static int getTmpUserOrientation1() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=16, flagSize=4, shift=7, max=8) public static void setTmpUserOrientation1(int val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	/** 每次都重建界面与重载数据 */
	@Metaline(flagPos=30, flagSize=2, debug=0) public int debuggingDBrowser() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
//	public static int debuggingDBrowser() { return 0; } //todo
	
	@Metaline(flagPos=32) public static boolean getDelRecApplyAll() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=32) public static void setDelRecApplyAll(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=33, debug=0) public static boolean getDebuggingRemoveRec() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=33, shift=1) public static boolean getWarnLoadModule() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=33, shift=1) public static void setWarnLoadModule(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=34) public static boolean getRevertExitManager() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=34) public static void setRevertExitManager(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=34, shift=1) public static boolean getWarnDisenaddAll() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=34, shift=1) public static void setWarnDisenaddAll(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
		@Metaline(flagPos=35) public static boolean translatePageTS() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=35) public static void translatePageTS(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	public int getPseudoInitCode(int pseudoInit) {
		return (getPseudoInitCode()&~3)|pseudoInit;
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
	
	private String pathToFavoriteDatabases(String name, boolean testDBV2) {
		StringBuffer InternalPath = pathToMainFolder().append("INTERNAL/");
		if (testDBV2) {
			if(name!=null)
			{
				InternalPath.append("favorites/").append(name);
			} else {
				InternalPath.append("databaseV2.sql");
			}
		} else {
			if(name!=null){
				InternalPath.append("favorites/").append(name);
			} else {
				InternalPath.append("history.sql");
			}
		}
		return InternalPath.toString();
	}
	public String pathToFavoriteDatabase(String name, boolean testDBV2) {
		return pathToFavoriteDatabases(name, testDBV2);
	}
	public File fileToFavoriteDatabases(String name, boolean testDBV2) {
		return new File(pathToFavoriteDatabases(name, testDBV2));
	}
	public File fileToDatabaseFavorites(boolean testDBV2) {
		return new File(pathToFavoriteDatabases(StringUtils.EMPTY, testDBV2));
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
		return getString("cache_p", GlideCacheModule.DEFAULT_GLIDE_PATH=context.getExternalCacheDir().getAbsolutePath()+"/thumnails/");
	}

	public long Flag(int flagIndex) {
		switch (flagIndex){
			case -1:
			return SessionFlag;
			case 1:
			return FirstFlag;
			case 2:
			return SecondFlag;
			case 3:
			return ThirdFlag;
			case 4:
			return FourthFlag;
			case 5:
			return FifthFlag;
			case 6:
			return SixthFlag();
			case 7:
			return SevenFlag();
			case 8:
			return EightFlag();
		}
		return tmpFlag;
	}
	
	public final void fillFlags(long[] flags) {
		flags[0] = getFirstFlag();
		flags[1] = getSecondFlag();
		flags[2] = getThirdFlag();
		flags[3] = getFourthFlag();
		flags[4] = getFifthFlag();
		flags[5] = getSixthFlag();
		flags[6] = getSevenFlag();
		flags[7] = getEightFlag();
	}
	
	public final boolean isFlagsChanged(long[] flags) {
		return flags[0] != FirstFlag
				|| flags[1] != SecondFlag
				|| flags[2] != ThirdFlag
				|| flags[3] != FourthFlag
				|| flags[4] != FifthFlag
				|| flags[5] != SixthFlag
				|| flags[6] != SevenFlag
				|| flags[7] != EightFlag
			;
	}
	
	private void putFlags() {
		Editor edit = tmpEdit();
		edit.putLong("MFF",FirstFlag)
			.putLong("MSF",SecondFlag)
			.putLong("MTF",ThirdFlag)
			.putLong("MQF",FourthFlag)
			.putLong("MVF",FifthFlag)
			.putLong("MVIF",SixthFlag)
			.putLong("M7F",SevenFlag)
			.putLong("M8F",EightFlag);
	}
	
	public void Flag(int flagIndex, long val) {
		switch (flagIndex){
			case -1:
				SessionFlag=val;
			break;
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
			case 5:
				FifthFlag=val;
			break;
			case 6:
				SixthFlag=val;
			break;
			case 7:
				SevenFlag=val;
			break;
			case 8:
				EightFlag=val;
			break;
			default:
				tmpFlag=val;
			break;
		}
	}

	XYTouchRecorder xyt;
	public XYTouchRecorder XYTouchRecorderInstance() {
		if(xyt==null) xyt = new XYTouchRecorder();
		return xyt;
	}

	public JSONObject getDimensionalSharePatternByIndex(String savid) {
		String val = getString(savid, null);
		JSONObject ret = null;
		if(val!=null) {
			try {
				ret = new JSONObject(val);
			} catch (JSONException e) {
				CMN.debug(e);
			}
		}
		return ret;
	}

	public void putDimensionalSharePatternByIndex(String savid, JSONObject json) {
		CMN.debug("保存", savid);
		putString(savid, json==null||json.length()==0?null:json.toString());
	}
	
	@SuppressLint("ClickableViewAccessibility")
	public void setAsLinkedTextView(TextView tv, boolean center) {
		if(xyt==null) xyt = new XYTouchRecorder();
		tv.setOnClickListener(xyt);
		tv.setOnTouchListener(xyt);
		tv.setTextSize(GlobalOptions.isLarge?22f:17f);
//		if(GlobalOptions.isLarge) {
//			tv.setTextSize(tv.getTextSize());
//		}
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
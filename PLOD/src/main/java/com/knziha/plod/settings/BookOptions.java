package com.knziha.plod.settings;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jaredrummler.colorpicker.ColorPickerPreference;
import com.knziha.filepicker.settings.FloatPreference;
import com.knziha.filepicker.settings.IntPreference;
import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.MagentTransient;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.OptionProcessor;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.WebViewmy;

import org.knziha.metaline.Metaline;

/** 这个是后后来的词典设置界面，比较高大上。 */
public class BookOptions extends SettingsFragmentBase implements Preference.OnPreferenceClickListener, Preference.OnGetViewListener {
	public final static int id = R.xml.pref_book;
	BookPresenter[] data;
	private boolean bNeedParseData;
	private static int mScrollPos;
	
	BookOptions() {
		bPersist = true;
		bRestoreListPos = true;
		bNavBarBelowList = false;
		bNavBarClickAsIcon = true;
		mNavBarHeight = (int) (35 * GlobalOptions.density);
		mNavBarPaddingTop = (int) (2 * GlobalOptions.density);
		mPreferenceId = id;
		Bundle args = new Bundle();
		args.putInt("title", R.string.dictOpt1);
		setArguments(args);
	}
	
	protected int getLastScrolledPos() {
		return mScrollPos;
	}
	
	protected void setLastScrolledPos(int pos) {
		mScrollPos = pos;
	}
	
	private void init_color(String key) {
		boolean multiple = false;
		Object val = GetSetIntField(data[0], key, true, 0);
		for (int i = 1; i < data.length; i++) {
			if(GetSetIntField(data[i], key, true, 0)!=val){
				multiple = true;
				break;
			}
		}
		
		Preference pref = init_switch_preference(this, key, val, null, null, null);
		
		if(pref instanceof ColorPickerPreference)
			MainProgram.setColorPreferenceTitle(pref, val);
		
		if(multiple) {
			pref.setTitle(pref.getTitle()+getResources().getString(R.string.multiple_vals));
		}
	}
	
	private void init_switcher(String key, boolean def, int position) {
		boolean multiple = false;
		long mask = 1L<<position;
		boolean val = getBooleanFlagAt(data[0], mask, def);
		for (int i = 1; i < data.length; i++) {
			if(getBooleanFlagAt(data[i], mask, def)!=val){
				multiple = true;
				break;
			}
		}
		Preference pref = init_switch_preference(this, key, val, null, null, null);
		pref.getExtras().putInt("flagPos", position);
		if(def)pref.getExtras().putBoolean("def", def);
		if(multiple) {
			pref.setTitle(pref.getTitle()+getResources().getString(R.string.multiple_vals));
		}
	}
	
	private void init_integer(String key, int position, int infoArr, int mask) {
		boolean multiple = false;
		int val = getShortFlagAt(data[0], position, mask);
		for (int i = 1; i < data.length; i++) {
			if(getShortFlagAt(data[i], position, mask)!=val){
				multiple = true;
				break;
			}
		}
		
		Preference pref = init_number_info_preference(this, key, val, infoArr, null, null);
		pref.getExtras().putInt("flagPos", position);
		pref.getExtras().putInt("mask", mask);
		
		if(multiple) {
			pref.setTitle(pref.getTitle()+getResources().getString(R.string.multiple_vals));
		}
	}
	
	private boolean getBooleanFlagAt(BookPresenter datum, long mask, boolean def) {
		boolean val = (datum.getFirstFlag()&mask)!=0;
		if(def) val = !val;
		return val;
	}
	
	private long setBooleanFlagAt(BookPresenter datum, long mask, boolean def, boolean val) {
		long flag = datum.getFirstFlag();
		flag&=~mask;
		if(def ^ val) flag |= mask;
		return flag;
	}
	
	private int getShortFlagAt(BookPresenter datum, int position, int mask) {
		return (int) ((datum.getFirstFlag()>>position)&mask);
	}
	
	private long setShortFlagAt(BookPresenter datum, int position, int val, long m) {
		long flag = datum.getFirstFlag();
		flag&=~(m<<position);
		flag |= (val&m)<<position;
		return flag;
	}
	
	private Object GetSetIntField(BookPresenter datum, String key, boolean get, Object val) {
		CMN.debug("GetSetIntField", key, get, val);
		if (datum instanceof MagentTransient) datum.getFirstFlag();
		if(datum!=null) {
			switch (key){
				case "use_mirrors":
					if(get) return datum.getUseMirrors(); else datum.setUseMirrors((boolean) val);
					break;
				case "bgTitle":
					if(get) return datum.getTitleBackground(); else datum.setTitleBackground((int) val);
					break;
				case "use_bgTitle":
					if(get) return datum.getUseTitleBackground(); else datum.setUseTitleBackground((boolean) val);
					break;
				case "fgTitle":
					if(get) return datum.getInternalTitleForeground(); else datum.setTitleForeground((int) val);
					break;
				case "use_fgTitle":
					if(get) return datum.getUseTitleForeground(); else datum.setUseTitleForeground((boolean) val);
					break;
				case "bgColor":
					if(get) return datum.getContentBackground(); else datum.setContentBackground((int) val);
					break;
				case "use_bgColor":
					if(get) return datum.getUseInternalBG(); else datum.setUseInternalBG((boolean)val);
					break;
				case "min_chars":
					if(get) return (int)datum.getMinMatchChars(); else datum.setMinMatchChars((short)(int)val);
					break;
				case "max_chars":
					if(get) return (int)datum.getMaxMatchChars(); else datum.setMaxMatchChars((short)(int)val);
					break;
				case "p_words":
					if(get) return (int)datum.getMinParagraphWords(); else datum.setMinParagraphWords((short)(int)val);
				break;
				case "use_p_words":
					if(get) return datum.getUseInternalParagraphWords(); else datum.setUseInternalParagraphWords((boolean)val);
					break;
				case "tzlv":
					if(get) return datum.getDoubleClickZoomRatio(); else datum.setDoubleClickZoomRatio((float)val);
					break;
				case "tz_x":
					if(get) return datum.getDoubleClickOffsetX(); else datum.setDoubleClickOffsetX((float)val);
					break;
				case "imdz1":
					if(get) return datum.getImgDoubleClickZoomLv1(); else datum.setImgDoubleClickZoomLv1((float)val);
					break;
				case "imdz2":
					if(get) return datum.getImgDoubleClickZoomLv2(); else datum.setImgDoubleClickZoomLv2((float)val);
					break;
				case "pzoomx":
					if(get) return datum.getImgPresetOffsetX(); else datum.setImgPresetOffsetX((float)val);
					break;
			}
			datum.checkTint();
			if(datum.getIsManagerAgent()>0)datum.isDirty=true;
		}
		return 0;
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (bNeedParseData) parseData();
	}
	
	public void setData(BookPresenter[] data) {
		this.data = data;
		if(bIsCreated) {
			parseData();
		} else {
			bNeedParseData = true;
		}
	}
	
	private void parseData() {
		//CMN.Log("parseData::", data);
		bNeedParseData = false;
		//init_switcher("use_bgt", false, 9);
		init_switcher("tz", false, 10);
		init_switcher("auto_fold", false, 32);
		
		init_switcher("limit_chars", false, 28);
		init_switcher("p_accept", false, 29);
		
		init_switcher("editable", false, 7);
		init_switcher("editing", false, 8);
		init_switcher("notxt", false, 11);
		init_switcher("browse_img", true, 31);
		
		init_switcher("entry_golst", false, 36);
		init_switcher("entry_pop", false, 37);
		
		init_switcher("tools", false, 25);
		init_switcher("hikeys", true, 26);
		init_switcher("offline", false, 27);
		
		init_switcher("hosts", true, 39);
		
		init_integer("pzoom", 15, R.array.pzoom_info, 3);
		init_integer("pzoom_plc", 17, R.array.pzoom_mode_info, 3);
		init_integer("tzby", 12, R.array.d_zoom_mode_info, 7); // getDoubleClickAlignment
		
		init_color("bgTitle");
		init_color("fgTitle");
		init_color("bgColor");
		
		init_color("min_chars");
		init_color("max_chars");
		init_color("p_words");
		
		init_color("tzlv");
		init_color("tz_x");
		
		init_color("pzoomx");
		init_color("imdz1");
		init_color("imdz2");
		init_switcher("dz12", false, 20);
		
		findPreference("reload").setOnPreferenceClickListener(this);
		findPreference("dtm").setOnPreferenceChangeListener(this);
		findPreference("dopt").setmOnGetViewListener(this);
		
		PlainWeb webx = data[0].getWebx();
		findPreference("online").setVisible(webx != null);
		if (webx != null) {
			findPreference("hosts").setVisible(webx.hasField("hosts"));
			try {
				JSONObject dopt = webx.getDopt();
				findPreference("dopt").setVisible(dopt!=null);
				JSONArray sites = webx.getJSONArray("mirrors");
				ListPreference mirrors = findPreference("mirrors");
				mirrors.setVisible(sites!=null);
				if (sites!=null) {
					//mirrors.setOnPreferenceChangeListener(this);
					init_integer("mirrors", 41, 0, 31);
					CharSequence[] mirrorsArr = new CharSequence[sites.size()];
					for (int i = 0; i < mirrorsArr.length; i++) {
						mirrorsArr[i] = sites.getJSONArray(i).getString(1);
					}
					mirrors.setEntries(mirrorsArr);
				}
			} catch (Exception e) {
				data[0].placeHolder.ErrorMsg = e.getLocalizedMessage();
			}
		}
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		try {
			String key=preference.getKey();
			if ("reload".equals(key)) {
				Toastable_Activity a = (Toastable_Activity) getActivity();
				if (a!=null) {
					OptionProcessor optprc = (clickableSpan, widget, processId, val) -> {
						if (processId==1) {
							int cc=0;
							for (BookPresenter datum:data) {
								if (datum.getIsManagerAgent()==0) {
									datum.Reload(a);
									cc++;
								}
							}
							
							a.showT("已重新加载"+cc+"本词典");
						}
					};
					if (a.opt.getIgnoreReloadWarning() || BuildConfig.isDebug) {
						optprc.processOptionChanged(null, null, 1, 0);
					} else {
						final String[] DictOpt = new String[]{"重启前不再提示", "重新加载视图"};
						final String[] Coef = " ×_ √".split("_");
						final SpannableStringBuilder ssb = new SpannableStringBuilder();
						TextView tv = a.buildStandardConfigDialog(optprc, false, 1, 0, "确认重新加载"+(data.length>1?"选中的"+data.length+"本":"当前")+"词典？");
						MainActivityUIBase.init_clickspan_with_bits_at(tv, ssb, DictOpt, 0, Coef, 0, 0, 0x1, 0, 1, -1, -1, true);
						MainActivityUIBase.init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 1, 1, -1, -1, true);
						MainActivityUIBase.showStandardConfigDialog(tv, ssb);
					}
				}
			}
			return false;
		} catch (Exception e) {
			CMN.debug(e);
			//getSettingActivity().showT("Error!"+e);
		}
		return true;
	}
	
	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		try {
			String key=preference.getKey();
			//CMN.Log("onPreferenceChange", preference, key, newValue);
			if (preference instanceof SwitchPreference) {
				int flagPos = preference.getExtras().getInt("flagPos", -1);
				if (flagPos>=0) {
					long mask = 1L<<flagPos;
					for (BookPresenter datum:data) {
						datum.setFirstFlag(setBooleanFlagAt(datum, mask, preference.getExtras().getBoolean("def", false), (Boolean) newValue));
					}
				}
			}
			if (newValue instanceof String) {
				String str = (String) newValue;
				boolean b1=str.equals("using");
				boolean b2=!b1&&(str.startsWith("use")&&(str.length()==3||str.endsWith("_not")));
				if (b1||b2) {
					Object value;
					String rtrStr= "use_"+key;
					for (BookPresenter datum:data) {
						value = GetSetIntField(datum, rtrStr, b1, str.length()==3);
						if(b1) return IU.parseBool(value);
					}
					if (getActivity() instanceof MainActivityUIBase) {
						((MainActivityUIBase)getActivity()).checkTint();
					}
					return true;
				}
			}
			if (preference instanceof IntPreference
					|| preference instanceof ColorPickerPreference) {
				int val = IU.parsint(newValue, 0);
				for (BookPresenter datum:data) {
					GetSetIntField(datum, key, false, val);
				}
				if (getActivity() instanceof MainActivityUIBase) {
					((MainActivityUIBase)getActivity()).checkTint();
				}
				if(preference instanceof ColorPickerPreference)
					MainProgram.setColorPreferenceTitle(preference, val);
			}
			else if (preference instanceof FloatPreference) {
				try {
					float val = Float.parseFloat(String.valueOf(newValue));
					for (BookPresenter datum:data) {
						GetSetIntField(datum, key, false, val);
					}
				} catch (NumberFormatException ignored) {  }
			}
			else if (preference instanceof ListPreference) {
				int flagPos = preference.getExtras().getInt("flagPos", -1);
				if (flagPos>=0) {
					int mask = preference.getExtras().getInt("mask", 3);
					CharSequence[] entries = ((ListPreference) preference).getEntries();
					int val = IU.parsint(newValue, 0) % entries.length;
					for (BookPresenter datum:data) {
						datum.setFirstFlag(setShortFlagAt(datum, flagPos, val, mask));
					}
					preference.setSummary(entries[val]);
				}
			}
			switch (key){
				case "mirrors":{
					PlainWeb webx = data[0].getWebx();
					if (webx!=null) {
						webx.setMirroredHost(IU.parsint(newValue, 0));
					}
				} break;
				case "tzby":{
				} break;
				case "p_words":
				case "min_chars":
				case "max_chars":
				case "fgTitle":
				case "bgTitle":{
				
				} break;
				case "imdz1":
				case "imdz2":
				case "pzoomx":
				case "tzlv":
				case "tz_x":{
				
				} break;
				case "pzoom":{
				
				} break;
				case "pzoom_plc":{
				} break;
				case "dtm":
					SearchUI.tapZoomWait = (int) newValue;
				break;
			}
			if (key.startsWith("tz")) {
				SearchUI.tapZoomV++;
			}
			return true;
		} catch (Exception e) {
			CMN.debug(e);
			//getSettingActivity().showT("Error!"+e);
		}
		return false;
	}
	
	
	
	/**
	 <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=yes">
	<meta charset="utf8">
	<div id="host"> </div>
	<script src='MdbR/settings.js'></script>
	<body>
		<script>
			// 测试
			if(true) {
				var host = document.getElementById('host');
				var dopt = %0;
				function change(id, value, el)
				{
	 				dopt[id] = value;
					console.log('pref_id=', id, 'newValue=', typeof value, value, 'result=');
	 				console.log(value, dopt);
					app.SaveDopt(sid.get(), JSON.stringify(dopt));
					return 1;
				}
				var settings = %1;
				SettingsBuildCard(change, settings, host);
			}
		</script>
	</body>
	 */
	@Metaline
	String webxSettings = "";
	
	WebViewmy mWebView;
	@Override
	public View getView(Preference preference) {
		try {
			if (preference.getKey().equals("dopt")) {
				PlainWeb webx = data[0].getWebx();
				if (mWebView==null || !TextUtils.equals(mWebView.toTag, CMN.idStr(data[0]))) {
					mWebView = new WebViewmy(data[0].a);
					mWebView.toTag = CMN.idStr(data[0]);
					JSONObject dopt = webx.getDopt();
					if (dopt!=null) {
						mWebView.setWebChromeClient(data[0].a.myWebCClient);
						mWebView.setWebViewClient(data[0].a.myWebClient);
						mWebView.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
						mWebView.addJavascriptInterface(data[0].getWebBridge(), "app");
						mWebView.presenter = data[0];
						mWebView.weblistHandler = data[0].a.weblistHandler;
						String settings = webxSettings;
						settings = settings.replace("%0", dopt.toString());
						settings = settings.replace("%1", webx.getField("settingsArray"));
						mWebView.loadDataWithBaseURL(data[0].mBaseUrl,settings, null, "UTF-8", null);
						data[0].getWebBridge().mergeView = mWebView;
					}
				}
				return mWebView;
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return null;
	}
}
package com.knziha.plod.settings;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.jaredrummler.colorpicker.ColorPickerPreference;
import com.knziha.filepicker.settings.FloatPreference;
import com.knziha.filepicker.settings.IntPreference;
import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.MagentTransient;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.OptionProcessor;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;

/** 这个是后后来的词典设置界面，比较高大上。 */
public class BookOptions extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
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
		mPreferenceId = R.xml.pref_book;
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
		
		Preference pref = init_switch_preference(this, key, val, null, null);
		
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
		Preference pref = init_switch_preference(this, key, val, null, null);
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
		
		Preference pref = init_number_info_preference(this, key, val, infoArr, null);
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
		CMN.Log("GetSetIntField", key, get, val);
		if (datum instanceof MagentTransient) datum.getFirstFlag();
		if(datum!=null) {
			switch (key){
				case "bgTitle":
					if(get) return datum.getTitleBackground(); else datum.setTitleBackground((int) val);
					break;
				case "fgTitle":
					if(get) return datum.getInternalTitleForeground(); else datum.setTitleForeground((int) val);
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
				case "dzooml":
					if(get) return datum.getDoubleClickZoomRatio(); else datum.setDoubleClickZoomRatio((float)val);
					break;
				case "dzoomx":
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
		init_switcher("use_bgt", false, 9);
		init_switcher("dzoom", false, 10);
		init_switcher("auto_fold", false, 32);
		
		init_switcher("limit_chars", false, 28);
		init_switcher("p_accept", false, 29);
		
		init_switcher("editable", false, 7);
		init_switcher("editing", false, 8);
		init_switcher("notxt", false, 11);
		init_switcher("browse_img", true, 31);
		
		init_switcher("entry_golst", false, 21);
		init_switcher("entry_pop", false, 22);
		
		init_switcher("tools", false, 25);
		init_switcher("hikeys", true, 26);
		init_switcher("offline", false, 27);
		
		init_integer("pzoom", 15, R.array.pzoom_info, 3);
		init_integer("pzoom_plc", 17, R.array.pzoom_mode_info, 3);
		init_integer("dzoom_plc", 12, R.array.d_zoom_mode_info, 7); // getDoubleClickAlignment
		
		init_color("bgTitle");
		init_color("fgTitle");
		init_color("bgColor");
		
		init_color("min_chars");
		init_color("max_chars");
		init_color("p_words");
		
		init_color("dzooml");
		init_color("dzoomx");
		
		init_color("pzoomx");
		init_color("imdz1");
		init_color("imdz2");
		init_switcher("dz12", false, 20);
		
		findPreference("reload").setOnPreferenceClickListener(this);
		findPreference("dtm").setOnPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
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
				if (a.opt.getIgnoreReloadWarning()) {
					optprc.processOptionChanged(null, null, 1, 0);
				} else {
					final String[] DictOpt = new String[]{"重启前不再确认", "重新加载视图"};
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
	}
	
	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
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
				return true;
			}
		}
		if (preference instanceof IntPreference
				|| preference instanceof ColorPickerPreference) {
			int val = IU.parsint(newValue, 0);
			for (BookPresenter datum:data) {
				GetSetIntField(datum, key, false, val);
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
			case "dzoom_plc":{
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
			case "dzooml":
			case "dzoomx":{
			
			} break;
			case "pzoom":{
			
			} break;
			case "pzoom_plc":{
			} break;
			case "dtm":
				SearchUI.tapZoomWait = (int) newValue;
			break;
		}
		return true;
	}
}
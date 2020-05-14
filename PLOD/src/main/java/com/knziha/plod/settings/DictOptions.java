package com.knziha.plod.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import com.jaredrummler.colorpicker.ColorPickerPreference;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_manageable;
import com.knziha.plod.dictionarymodels.mdict_transient;

public class DictOptions extends SettingsFragment implements Preference.OnPreferenceClickListener {
	mdict_manageable[] data;
	
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
	
	private void init_switcher(String key, int mask, boolean def) {
		boolean multiple = false;
		boolean val = getBooleanFlagAt(data[0], mask, def);
		for (int i = 1; i < data.length; i++) {
			if(getBooleanFlagAt(data[i], mask, def)!=val){
				multiple = true;
				break;
			}
		}
		
		Preference pref = init_switch_preference(this, key, val, null, null);
	
		if(multiple) {
			pref.setTitle(pref.getTitle()+getResources().getString(R.string.multiple_vals));
		}
	}
	
	private void init_disconjuctioner(String key, int position, int infoArr) {
		boolean multiple = false;
		int val = getShortFlagAt(data[0], position);
		for (int i = 1; i < data.length; i++) {
			if(getShortFlagAt(data[i], position)!=val){
				multiple = true;
				break;
			}
		}
		
		Preference pref = init_number_info_preference(this, key, val, infoArr, null);
	
		if(multiple) {
			pref.setTitle(pref.getTitle()+getResources().getString(R.string.multiple_vals));
		}
	}
	
	private boolean getBooleanFlagAt(mdict_manageable datum, int mask, boolean def) {
		boolean val = (datum.getFirstFlag()&mask)!=0;
		if(def) val = !val;
		return val;
	}
	
	private long setBooleanFlagAt(mdict_manageable datum, int mask, boolean def, boolean val) {
		long flag = datum.getFirstFlag();
		flag&=~mask;
		if(def ^ val) flag |= mask;
		return flag;
	}
	
	private int getShortFlagAt(mdict_manageable datum, int position) {
		return (int) ((datum.getFirstFlag()>>position)&3);
	}
	
	private long setShortFlagAt(mdict_manageable datum, int position, int val) {
		long mask = ((long)3)<<position;
		long flag = datum.getFirstFlag();
		flag&=~mask;
		flag |= (val&3)<<position;
		return flag;
	}
	
	private Object GetSetIntField(mdict_manageable datum, String key, boolean get, Object val) {
		mdict m1 = null;
		mdict_transient m2 = null;
		if(datum instanceof mdict) m1 = (mdict) datum;
		else m2 = (mdict_transient) datum;
		switch (key){
			case "TIBG":
				if(m1!=null) { if(get) return m1.TIBGColor; else m1.TIBGColor=(int) val; }
				if(m2!=null) { if(get) return m2.TIBGColor; else m2.TIBGColor=(int) val; }
			break;
			case "TIFG":
				if(m1!=null) { if(get) return m1.TIFGColor; else m1.TIFGColor=(int)val; }
				if(m2!=null) { if(get) return m2.TIFGColor; else m2.TIFGColor=(int)val; }
			break;
			case "dzoomr":
				if(m1!=null) { if(get) return m1.IBC.doubleClickZoomRatio; else m1.IBC.doubleClickZoomRatio=(float)val; }
				if(m2!=null) { if(get) return m2.IBC.doubleClickZoomRatio; else m2.IBC.doubleClickZoomRatio=(float)val; }
			break;
			case "dzoomx":
				if(m1!=null) { if(get) return m1.IBC.doubleClickXOffset; else m1.IBC.doubleClickXOffset=(float)val; }
				if(m2!=null) { if(get) return m2.IBC.doubleClickXOffset; else m2.IBC.doubleClickXOffset=(float)val; }
			break;
			case "pzmx":
				if(m1!=null) { if(get) return m1.IBC.doubleClickPresetXOffset; else m1.IBC.doubleClickPresetXOffset=(float)val; }
				if(m2!=null) { if(get) return m2.IBC.doubleClickPresetXOffset; else m2.IBC.doubleClickPresetXOffset=(float)val; }
			break;
			case "imdz1":
				if(m1!=null) { if(get) return m1.IBC.doubleClickZoomLevel1; else m1.IBC.doubleClickZoomLevel1=(float)val; }
				if(m2!=null) { if(get) return m2.IBC.doubleClickZoomLevel1; else m2.IBC.doubleClickZoomLevel1=(float)val; }
			break;
			case "imdz2":
				if(m1!=null) { if(get) return m1.IBC.doubleClickZoomLevel2; else m1.IBC.doubleClickZoomLevel2=(float)val; }
				if(m2!=null) { if(get) return m2.IBC.doubleClickZoomLevel2; else m2.IBC.doubleClickZoomLevel2=(float)val; }
			break;
		}
		if(m1!=null) { m1.checkTint(); m1.isDirty=true; }
		if(m2!=null) m2.isDirty=true;
		return 0;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(data==null){
			data = (mdict_manageable[]) getActivity().getWindow().getDecorView().getTag();
			init_switcher("GLT", 0x200, false);
			init_switcher("dzoom", 0x400, false);
			init_disconjuctioner("dzoom_mode", 12, R.array.d_zoom_mode_info);
			init_color("TIBG");
			init_color("TIFG");
			
			init_color("dzoomr");
			init_color("dzoomx");
			init_switcher("golst", 0x40000, false);
			init_switcher("pop", 0x80000, false);
			init_switcher("notxt", 0x800, false);
			init_disconjuctioner("pzoom", 14, R.array.pzoom_info);
			init_disconjuctioner("pmed", 16, R.array.pzoom_mode_info);
			init_color("pzmx");
			init_color("imdz1");
			init_color("imdz2");
			init_switcher("dz12", 0x100000, false);
		}
	}
	
	@Override
	public void onClick(View view) {
		if(view.getId()== R.id.home && getParentFragment() instanceof DialogFragment){
			((DialogFragment)getParentFragment()).dismiss();
		}
	}
	
	//创建
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.dictpreferences);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key;
		switch (key=preference.getKey()){
			case "GLT":
				for (mdict_manageable md:data) {
					md.setFirstFlag(setBooleanFlagAt(md, 0x200, false, (Boolean) newValue));
				}
			break;
			case "golst":
				for (mdict_manageable md:data) {
					md.setFirstFlag(setBooleanFlagAt(md, 0x40000, false, (Boolean) newValue));
				}
			break;
			case "pop":
				for (mdict_manageable md:data) {
					md.setFirstFlag(setBooleanFlagAt(md, 0x80000, false, (Boolean) newValue));
				}
			break;
			case "notxt":
				for (mdict_manageable md:data) {
					md.setFirstFlag(setBooleanFlagAt(md, 0x800, false, (Boolean) newValue));
				}
			break;
			case "dzoom":
				for (mdict_manageable md:data) {
					md.setFirstFlag(setBooleanFlagAt(md, 0x400, false, (Boolean) newValue));
				}
			break;
			case "dzoom_mode":{
				int val = IU.parsint(newValue, 0);
				for (mdict_manageable md:data) {
					md.setFirstFlag(setShortFlagAt(md, 12, val));
				}
				preference.setSummary(getResources().getStringArray(R.array.d_zoom_mode_info)[val]);
			} break;
			case "dz12":
				for (mdict_manageable md:data) {
					md.setFirstFlag(setBooleanFlagAt(md, 0x100000, false, (Boolean) newValue));
				}
			break;
			case "TIFG":
			case "TIBG":{
				int val = IU.parsint(newValue, 0);
				for (mdict_manageable md:data) {
					GetSetIntField(md, key, false, val);
				}
				if(preference instanceof ColorPickerPreference)
					MainProgram.setColorPreferenceTitle(preference, val);
			} break;
			case "imdz1":
			case "imdz2":
			case "pzmx":
			case "dzoomr":
			case "dzoomx":{
				try {
					float val = Float.parseFloat(String.valueOf(newValue));
					for (mdict_manageable md:data) {
						GetSetIntField(md, key, false, val);
					}
					if(preference instanceof ColorPickerPreference)
						MainProgram.setColorPreferenceTitle(preference, val);
				} catch (NumberFormatException ignored) {  }
			} break;
			case "pzoom":{
				int val = IU.parsint(newValue, 0);
				for (mdict_manageable md:data) {
					md.setFirstFlag(setShortFlagAt(md, 14, val));
				}
				preference.setSummary(getResources().getStringArray(R.array.pzoom_info)[val]);
			} break;
			case "pmed":{
				int val = IU.parsint(newValue, 0);
				for (mdict_manageable md:data) {
					md.setFirstFlag(setShortFlagAt(md, 16, val));
				}
				preference.setSummary(getResources().getStringArray(R.array.pzoom_mode_info)[val]);
			} break;
		}
		return true;
	}
}
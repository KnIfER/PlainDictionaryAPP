package com.knziha.plod.settings;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.jaredrummler.colorpicker.ColorPickerPreference;
import com.knziha.plod.PlainDict.AgentApplication;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.R;
import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.dictionarymodels.mdict;

public class SettingsFragment extends SettingsFragmentBase {

	
	
	
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

	  
      addPreferencesFromResource(R.xml.preferences);
      Preference pkey0 = (findPreference("GKCS"));
      pkey0.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				mdict.bGlobalUseClassicalKeycase=(boolean) newValue;
				return true;
			}});
      
      ColorPickerPreference def_pagecolor = ((ColorPickerPreference)findPreference("GPBC"));
      def_pagecolor.setTitle(def_pagecolor.getTitle().toString()+Integer.toHexString(CMN.GlobalPageBackground).toUpperCase());
      def_pagecolor.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String name = preference.getTitle().toString();
				preference.setTitle(name.substring(0, name.indexOf(": ")+2)+Integer.toHexString((int) newValue).toUpperCase());
				CMN.GlobalPageBackground=(int) newValue;
				return true;
			}});
      
      def_pagecolor = ((ColorPickerPreference)findPreference("BCM"));
      def_pagecolor.setTitle(def_pagecolor.getTitle().toString()+Integer.toHexString(CMN.MainBackground).toUpperCase());
      def_pagecolor.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String name = preference.getTitle().toString();
				preference.setTitle(name.substring(0, name.indexOf(": ")+2)+Integer.toHexString((int) newValue).toUpperCase());
				CMN.MainBackground=(int) newValue;
				return true;
			}});

      def_pagecolor = ((ColorPickerPreference)findPreference("BCF"));
      def_pagecolor.setTitle(def_pagecolor.getTitle().toString()+Integer.toHexString(CMN.FloatBackground).toUpperCase());
      def_pagecolor.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String name = preference.getTitle().toString();
				preference.setTitle(name.substring(0, name.indexOf(": ")+2)+Integer.toHexString((int) newValue).toUpperCase());
				CMN.FloatBackground=(int) newValue;
				return true;
			}});
      
     
      EditTextPreference def_fontscale = ((EditTextPreference) findPreference("def_fontscale"));
      //EditText editText = def_fontscale.getText()
	  //String name = def_fontscale.getTitle().toString();
	  //def_fontscale.setTitle(name+mdict.def_fontsize);
	  //editText.setKeyListener(new NumberKeyListener() {
	//		@Override
	//		public int getInputType() {return InputType.TYPE_CLASS_NUMBER;}
	//
	//		@Override
	//		protected char[] getAcceptedChars() {
	//			return new String("1234567890").toCharArray();
	//		}
	  //});
	  
	  def_fontscale.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String name = preference.getTitle().toString();
				preference.setTitle(name.substring(0, name.indexOf(": ")+2)+newValue);
				mdict.def_fontsize = Integer.valueOf((String) newValue);
				return true;
			}});
	  
	  
	  
  }

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AgentApplication agent = ((AgentApplication)getActivity().getApplication());
		//findPreference("browse_instant_Srch").setEnabled(agent .opt.isBrowser_AffectEtSearch());
		agent.clearNonsenses();
		
	}
  
  
  
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
      View v = super.onCreateView(inflater, container, savedInstanceState);
      if(v != null) {
          //ListView lv = (ListView) v.findViewById(android.R.id.list);
          //lv.setPadding(0, 0, 0, 0);
         // lv.setBackgroundColor(Color.WHITE);
      }
      
      //if(Build.VERSION.SDK_INT >= 22) {
		  SpannableStringBuilder ssb = new SpannableStringBuilder("| ");
		  Drawable dSettings = ActivityCompat.getDrawable(getActivity(), R.drawable.drawer_menu_icon_setting);
		  dSettings.setBounds(0,0,50,50);
		  ssb.setSpan(new ImageSpan(dSettings), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		  int baseLen = ssb.length();
		  
	      Preference cat_browser = findPreference("cat_prog");
		  ssb.replace(baseLen, ssb.length(), cat_browser.getTitle());
		  cat_browser.setTitle(new SpannedString(ssb));
		  

      return v;
  }
  
  
  
  
}
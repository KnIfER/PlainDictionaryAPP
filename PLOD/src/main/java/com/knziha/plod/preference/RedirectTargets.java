package com.knziha.plod.preference;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
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
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.MagentTransient;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.OptionProcessor;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.settings.MainProgram;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.knziha.metaline.Metaline;

import java.util.ArrayList;

/** 重定向文本菜单。 */
public class RedirectTargets extends SettingsFragmentBase implements Preference.OnPreferenceClickListener {
	public final static int id = R.xml.pref_redirect;
	public final static int requestCode=id&0xFFFF;
	
	RedirectTargets() {
		bPersist = false;
		bRestoreListPos = true;
		bNavBarBelowList = false;
		bNavBarClickAsIcon = true;
		mNavBarHeight = (int) (35 * GlobalOptions.density);
		mNavBarPaddingTop = (int) (2 * GlobalOptions.density);
		mPreferenceId = id;
		Bundle args = new Bundle();
		args.putInt("title", R.string.settings);
		setArguments(args);
	}
	
	public RedirectTargets(Activity activity, ViewGroup vg) {
		this();
		Resources res = activity.getResources();
		if(Build.VERSION.SDK_INT>=29){
			Configuration mConfiguration = res.getConfiguration();
			boolean systemDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
			GlobalOptions.isDark = systemDark;
		}
		setContext(activity);
		onCreate(null);
		View contentView = onCreateView(activity.getLayoutInflater(), vg, null);
		onViewCreated(contentView, null);
		ViewUtils.addViewToParent(contentView, vg);
		vg.setBackgroundColor(GlobalOptions.isDark?Color.BLACK:Color.WHITE);
		int pad = (int) (res.getDisplayMetrics().density*10);
		vg.setPadding(pad,0,pad,0);
		parseData();
	}
	
	private void parseData() {
		ArrayList<Preference> preferences = new ArrayList<>(64);
		PreferenceScreen screen = mPreferenceManager.mPreferenceScreen;
		if(screen!=null){
			preferences.add(screen);
			for (int i = 0; i < preferences.size(); i++) {
				Preference p = preferences.get(i);
				if (p instanceof PreferenceGroup) {
					preferences.addAll(((PreferenceGroup)p).getPreferences());
				} else {
					final String key = p.getKey();
					switch (key) {
						case "dir_0":
							init_switch_preference(this, key, PDICMainAppOptions.allowFZeroDef(), null, null, p);
							break;
					}
					p.setOnPreferenceChangeListener(this);
				}
			}
		}
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		try {
			String key=preference.getKey();
			if ("reload".equals(key)) {
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
			switch (key){
				case "tzby":{
				} break;
			}
			return true;
		} catch (Exception e) {
			CMN.debug(e);
			//getSettingActivity().showT("Error!"+e);
		}
		return false;
	}
	
	Context context;
	
	public Context getContext() {
		if (context!=null) return context;
		return super.getContext();
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
}
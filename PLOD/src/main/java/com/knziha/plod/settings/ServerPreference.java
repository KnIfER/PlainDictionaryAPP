package com.knziha.plod.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.GlobalOptions;
import androidx.preference.Preference;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MultiShareActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;

public class ServerPreference extends SettingsFragment implements Preference.OnPreferenceClickListener {
	public final static int id=11;

	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findPreference("mdccsp").setOnPreferenceClickListener(this);
		
		//init_number_info_preference(this, "share_target", PDICMainAppOptions.getSendToShareTarget(), null, null);
		init_number_info_preference(this, "send_to", PDICMainAppOptions.getSendToAppTarget(), 0, null);
		
		Preference item;
		item = findPreference("keep");
		item.getParent().removePreference(item);
		item = findPreference("keep_screen");
		item.getParent().removePreference(item);
		item = findPreference("port");
		item.getParent().removePreference(item);
		item = findPreference("white_list");
		item.getParent().removePreference(item);
		item = findPreference("black_list");
		item.getParent().removePreference(item);
	}


	//创建
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.serverpreferences);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		switch (preference.getKey()){
			case "mdccsp":
				startActivity(new Intent(getActivity(), MultiShareActivity.class));
			break;
		}
		return true;
	}

	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch (preference.getKey()){
			case "send_to": {
				PDICMainAppOptions.setSendToAppTarget(IU.parsint(newValue, 1));
				CMN.Log("send_to", newValue, PDICMainAppOptions.getSendToAppTarget());
			} break;
		}
		return true;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View ret = super.onCreateView(inflater, container, savedInstanceState);
		
		text1.setText("可以在局域网内访问本机网址（http://ip地址:端口），以下功能处于测试状态请勿使用。");
		
		return  ret;
	}
}
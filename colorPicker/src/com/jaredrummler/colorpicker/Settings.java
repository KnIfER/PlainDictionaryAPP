package com.jaredrummler.colorpicker;

/**
 * Created by liao on 2017/4/13.
 */

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 参见:AnkiHelper->com.mmjang.ankihelper.data.settings.java
 */
public class Settings {

    private static Settings settings = null;

    private final static String PREFER_NAME = "color_settings";    //搴旂敤璁剧疆鍚嶇О
    
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private Settings(Context context) {
        sp = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    /**
     * 鑾峰緱鍗曚緥
     *
     * @return
     */
    public static Settings getInstance(Context context) {
        if (settings == null) {
        	//Log.e("真的有：","真的真的真的真的真的");
            settings = new Settings(context);
        }
        //else Log.e("真的有：","假");看来这是弱的单例。
        return settings;
    }


    public void putBoolean(String key,Boolean val) {
    	editor.putBoolean(key, val)
        .commit();
    }
    
	public boolean getBoolean(String key) {
	    return sp.getBoolean(key, false);
    }

	public String getAllPreset() {
		return sp.getString("Alets", "[]");
	}

	public void setAllPreset(String data) {
		sp.edit().putString("Alets", data).apply();
	}

	public int getInt(String key) {
		return sp.getInt(key, 0);
	}
	public void putInt(String key,int val) {
		editor.putInt(key, val)
        .commit();
	}
}
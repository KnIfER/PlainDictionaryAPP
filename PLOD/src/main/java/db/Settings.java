package db;

/**
 * Created by liao on 2017/4/13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

/**
 * 单例，getInstance()得到实例
 */
public class Settings {

    private static Settings settings = null;

    private final static String PREFER_NAME = "settings";    //应用设置名称
    private final static String MODEL_ID = "model_id";       //应用设置项 模版id
    private final static String DECK_ID = "deck_id";         //应用设置项 牌组id
    private final static String DEFAULT_MODEL_ID = "default_model_id"; //默认模版id，如果此选项存在，则已写入配套模版
    private final static String FIELDS_MAP = "fields_map";   //字段映射
    private final static String MONITE_CLIPBOARD_Q = "show_clipboard_notification_q";   //是否监听剪切板
    private final static String AUTO_CANCEL_POPUP_Q = "auto_cancel_popup";              //点加号后是否退出
    private final static String DEFAULT_PLAN = "default_plan";
    private final static String LAST_SELECTED_PLAN = "last_selected_plan";
    private final static String DEFAULT_TAG = "default_tag";
    private final static String SET_AS_DEFAULT_TAG = "set_as_default_tag";
    private final static String LAST_PRONOUNCE_LANGUAGE = "last_pronounce_language";

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private SharedPreferences dsp;
    
    private Settings(Context context) {
        sp = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        dsp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();
    }

    /**
     * 获得单例
     *
     * @return
     */
    public static Settings getInstance(Context context) {
        if (settings == null) {
            settings = new Settings(context);
        }
        return settings;
    }

    /*************/

    Long getModelId() {
        return sp.getLong(MODEL_ID, 0);
    }

    void setModelId(Long modelId) {
        editor.putLong(MODEL_ID, modelId)
        .commit();
    }

    /**************/

    Long getDeckId() {
        return sp.getLong(DECK_ID, 0);
    }

    void setDeckId(Long deckId) {
        editor.putLong(DECK_ID, deckId)
        .commit();
    }

    /**************/

    Long getDefaultModelId() {
        return sp.getLong(DEFAULT_MODEL_ID, 0);
    }

    void setDefaultModelId(Long defaultModelId) {
        editor.putLong(DEFAULT_MODEL_ID, defaultModelId)
        .commit();
    }

    /**************/

    String getFieldsMap() {
        return sp.getString(FIELDS_MAP, "");
    }

    void setFieldsMap(String filedsMap) {
        editor.putString(FIELDS_MAP, filedsMap)
        .commit();
    }

    /**************/

    public boolean getMoniteClipboardQ() {
        return sp.getBoolean(MONITE_CLIPBOARD_Q, false);
    }

    public void setMoniteClipboardQ(boolean moniteClipboardQ) {
        editor.putBoolean(MONITE_CLIPBOARD_Q, moniteClipboardQ)
        .commit();
    }

    /**************/

    public boolean getAutoCancelPopupQ() {
        return sp.getBoolean(AUTO_CANCEL_POPUP_Q, false);
    }

    public void setAutoCancelPopupQ(boolean autoCancelPopupQ) {
        editor.putBoolean(AUTO_CANCEL_POPUP_Q, autoCancelPopupQ)
        .commit();
    }

    /**************/
    public String getDefaultPlan() {
        return sp.getString(DEFAULT_PLAN, "");
    }

    public void setDefaultPlan(String defaultPlan) {
        editor.putString(DEFAULT_PLAN, defaultPlan)
        .commit();
    }

    /******************/

    public String getLastSelectedPlanName() {
        return sp.getString(LAST_SELECTED_PLAN, "");
    }

    public void setLastSelectedPlanName(String lastSelectedPlan) {
        editor.putString(LAST_SELECTED_PLAN, lastSelectedPlan)
        .commit();
    }
    
    public int getLastSelectedPlanPos() {
        return dsp.getInt(Settings.LAST_SELECTED_PLAN, -1);
    }

    public void setLastSelectedPlanPos(int lastSelectedPlan) {
    	dsp.edit().putInt(LAST_SELECTED_PLAN, lastSelectedPlan)
        .commit();
    }
    /*****************/
    public String getDefaulTag() {
        return sp.getString(DEFAULT_TAG, "");
    }

    public void setDefaultTag(String defaultTag) {
        editor.putString(DEFAULT_TAG, defaultTag)
        .commit();
    }

    /****************/
    public boolean getSetAsDefaultTag() {
        return sp.getBoolean(SET_AS_DEFAULT_TAG, false);
    }

    public void setSetAsDefaultTag(boolean setAsDefaultTag) {
        editor.putBoolean(SET_AS_DEFAULT_TAG, setAsDefaultTag)
        .commit();
    }



    /**************/
    boolean hasKey(String key) {
        return sp.contains(key);
    }


    public void putBoolean(String key,Boolean val) {
    	editor.putBoolean(key, val)
        .commit();
    }
    
    public void putLong(String key,long val) {
    	editor.putLong(key, val)
        .commit();
    }
    
    public void putInt(String key,int val) {
    	editor.putInt(key, val)
        .commit();
    }
    
    public void putString(String key, String val) {
		editor.putString(key, val)
		.commit();
	}
    //只在启动时读取一次，运行时保存数次。
	public boolean getBoolean(String key) {
	    return sp.getBoolean(key, false);
    }

	public long getLong(String key) {
		return sp.getLong(key, -1);
	}
    

    
	public int getInt(String key) {
		return sp.getInt(key, -1);
	}

	public String getString(String key, String def) {
		return sp.getString(key, def);
	}

}
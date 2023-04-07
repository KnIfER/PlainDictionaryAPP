package com.knziha.plod.preference;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.paging.AppIconCover.AppInfoBean;
import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.AppIconsAdapter;
import com.knziha.plod.widgets.RomUtils;
import com.knziha.plod.widgets.ViewUtils;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/** 重定向文本菜单。 */
public class RedirectTargets extends SettingsFragmentBase implements Preference.OnPreferenceClickListener, PopupMenuHelper.PopupMenuListener {
	public final static int id = R.xml.pref_redirect;
	public final static int requestCode=id&0xFFFF;
	String[] dictionaryInterfacesStr = null;
	private View.OnClickListener listener;
	private Activity pluginHost;
	JSONObject options;
	private String tweakingKey;
	private Preference tweakingPref;
	private Resources res;
	
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
	
	public RedirectTargets(Activity activity, Context pluginContext
				, ViewGroup vg
				, JSONObject options
				, String[] arr
				, View.OnClickListener listener) {
		this();
		this.dictionaryInterfacesStr = arr;
		this.listener = listener;
		this.options = options;
		CMN.debug("options::", options);
		try {
			res = pluginContext.getResources();
			if (Build.VERSION.SDK_INT >= 29) {
				Configuration mConfiguration = res.getConfiguration();
				boolean systemDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
				GlobalOptions.isDark = systemDark;
			}
			vg.setBackgroundColor(GlobalOptions.isDark ? Color.BLACK : Color.WHITE);
			pluginHost = activity;
			setContext(new ContextThemeWrapper(pluginContext, R.style.AppTheme));
			onCreate(null);
			View contentView = onCreateView(activity.getLayoutInflater(), vg, null);
			onViewCreated(contentView, null);
			ViewUtils.addViewToParent(contentView, vg);
			DisplayMetrics metrics = res.getDisplayMetrics();
			GlobalOptions.density = metrics.density;
			GlobalOptions.densityDpi = metrics.densityDpi;
			int pad = (int) (GlobalOptions.density * 10);
			vg.setPadding(pad, 0, pad, 0);
			parseData();
			contentView.setBackgroundColor(GlobalOptions.isDark ? Color.BLACK : Color.WHITE);
			contentView.setAlpha(0);
			contentView.animate().alpha(1);
			View finalVg = contentView;
			navBar.getNavigationBtn().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finalVg.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							VU.removeView(finalVg);
						}
					});
				}
			});
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	private Object getPreference(String key) {
		return getPreference(key, null);
	}
	
	private Object getPreference(String key, Object defVal) {
		try {
			return options.get(key);
		} catch (Exception e) {
			return defVal;
		}
	}
	
//	int[] dictionaryInterfaces = new int[]{
//			R.string.dictface_0
//			, R.string.dictface_1
//			, R.string.dictface_2
//			, R.string.dictface_3
//	};
	
	private void parseData() {
		ArrayList<Preference> preferences = new ArrayList<>(15);
		PreferenceScreen screen = mPreferenceManager.mPreferenceScreen;
		if(screen!=null){
			preferences.add(screen);
			for (int i = 0; i < preferences.size(); i++) {
				Preference p = preferences.get(i);
				if (p instanceof PreferenceGroup) {
					preferences.addAll(((PreferenceGroup)p).getPreferences());
				} else {
					final String key = p.getKey();
					p.setOnPreferenceClickListener(this);
					p.setOnPreferenceChangeListener(this);
					switch (key) {
						case "_":
							init_switch_preference(this, key, IU.parsint(getPreference(key, 0))==1, null, null, p);
							break;
						case "0":
						case "1":
							init_switch_preference(this, key, IU.parsint(getPreference(key, 1))==1, null, null, p);
							break;
						case "0_type": // 接口类型
						case "2_type":
						case "1_type":
							p.setSummary(dictionaryInterfacesStr[Math.max(0, Math.min(dictionaryInterfacesStr.length, IU.parsint(getPreference(key), 0)))]);
							break;
						case "0_name": // 指定包名
						case "1_name":
							String value = String.valueOf(getPreference(key, "com.knziha.plod.plaindict"));
							if("".equals(value)) value = "未指定";
							p.setSummary(String.valueOf(value));
							break;
						case "2_name":
							p.setSummary(String.valueOf(getPreference(key, "未指定")));
							break;
					}
				}
			}
		}
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		try {
			String key=preference.getKey();
			tweakingPref = preference;
			tweakingKey = key;
			switch (key) {
				case "0":
				case "1":
					break;
				case "0_type": // 接口类型
				case "2_type":
				case "1_type":
					PopupMenuHelper popupMenu = getPopupMenu();
					popupMenu.initLayout(dictionaryInterfacesStr, this);
					int[] vLocationOnScreen = new int[2];
					mList.getLocationOnScreen(vLocationOnScreen);
					int x=(int)mList.mLastTouchX;
					int y=(int)mList.mLastTouchY;
					popupMenu.show(mList, x+vLocationOnScreen[0], y+vLocationOnScreen[1]);
					ViewUtils.preventDefaultTouchEvent(mList, x, y);
					return true;
				case "0_name": // 指定包名
				case "2_name":
				case "1_name":
					AppIconsAdapter ret = new AppIconsAdapter(pluginHost, getContext(), null);
					ArrayList<AppIconsAdapter.PrefetchedApps> activities = new ArrayList<>();
					PackageManager pm = getContext().getPackageManager();
					Intent intentSch = getIntent(IU.parsint(key, 0));
					//intentSch = new Intent("colordict.intent.action.SEARCH");
					//pm.getInstalledPackages()
					activities.add(new AppIconsAdapter.PrefetchedApps(intentSch, pm.queryIntentActivities(intentSch, PackageManager.MATCH_ALL)));
					ret.headerBtnText = new String[]{"清空", "清空包名"};
					
					ret.pullAvailableApps(getContext(), null, null, Intent.EXTRA_TEXT, activities);
					CMN.debug(pm.queryIntentActivities(intentSch, PackageManager.MATCH_ALL));
					ret.indicator.setText("选择APP：");
					ret.itemClicker = new View.OnClickListener() {
						@Override
						public void onClick(View v1) {
							try {
								AppIconsAdapter.ViewHolder vh = (AppIconsAdapter.ViewHolder) v1.getTag();
								int pos = vh.getLayoutPosition()-ret.headBtnSz;
								String newValue = "";
								if (pos < 0) {
									// 清空包名
									if("2".equals(tweakingKey)) newValue=null;
								}
								else { // 设置…
									AppInfoBean appBean = ret.list.get(pos);
									newValue = appBean.pkgName;
								}
								put(tweakingKey, newValue);
								String value = String.valueOf(getPreference(tweakingKey, "com.knziha.plod.plaindict"));
								if("".equals(value)) value = "未指定";
								tweakingPref.setSummary(value);
								v1.post(new Runnable() {
									@Override
									public void run() {
										ret.shareDialog.dismiss();
									}
								});
							} catch (Exception e) {
								CMN.debug(e);
							}
						}
					};
					return true;
			}
			return false;
		} catch (Exception e) {
			CMN.debug(e);
			//getSettingActivity().showT("Error!"+e);
		}
		return true;
	}
	
	Intent getIntent(int key) {
		int target = IU.parsint(getPreference(key+"_type", 0));
		CMN.debug("getIntent::", key+"-->"+target);
		Intent newTask = new Intent(Intent.ACTION_MAIN);
		if (target == 0) {
			newTask.setAction("colordict.intent.action.SEARCH");
			newTask.putExtra(Intent.EXTRA_TEXT, "");
			newTask.putExtra("EXTRA_QUERY", "");
		}
		else if (target == 1) {
			newTask.setAction(Intent.ACTION_PROCESS_TEXT);
			newTask.putExtra(Intent.EXTRA_PROCESS_TEXT, "");
			newTask.setType("text/plain");
		}
		else if (target == 2) {
			newTask.setAction(Intent.ACTION_SEND);
			newTask.putExtra(Intent.EXTRA_TEXT, "");
			newTask.setType("text/plain");
		}
		else if (target == 3) {
			newTask.setAction(Intent.ACTION_WEB_SEARCH);
			newTask.putExtra(SearchManager.QUERY, "");
		}
		return newTask;
	}
	
	//配置变化
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		try {
			String key=preference.getKey();
			switch (key){
				case "_":
				case "0":
				case "1":
					put(key, ((Boolean)newValue)?1:0);
					break;
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
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		if (isLongClick) return false;
		try {
			if (popupMenuHelper.textsArr == dictionaryInterfacesStr) {
				put(tweakingKey, v.getId());
				tweakingPref.setSummary(dictionaryInterfacesStr[Math.max(0, Math.min(dictionaryInterfacesStr.length, IU.parsint(getPreference(tweakingKey), 0)))]);
				v = mList.findChildViewUnder(mList.mLastTouchX, mList.mLastTouchY);
				if (v != null) {
//					ObjectAnimator animator = new ObjectAnimator()
//					v.setScaleX(1.5f); v.setScaleY(1.5f);
//					v.animate().scaleX(1).scaleY(1).setDuration(400).start();
//					PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X, new Keyframe[]{Keyframe.ofFloat(0.0F, 1.5F), Keyframe.ofFloat(1.0F, 1.0F)});
//					PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y, new Keyframe[]{Keyframe.ofFloat(0.0F, 1.5F), Keyframe.ofFloat(1.0F, 1.0F)});
//					ObjectAnimator.ofPropertyValuesHolder(v, new PropertyValuesHolder[]{pvhScaleX, pvhScaleY}).setDuration(400L).start();
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		v.postDelayed(new Runnable() {
			@Override
			public void run() {
				popupMenuHelper.dismiss();
			}
		}, 110);
		return false;
	}
	
	private void put(String key, Object value) {
		try {
			options.put(key, value);
			listener.onClick(null);
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	WeakReference<PopupMenuHelper> popupMenuRef = ViewUtils.DummyRef;
	public PopupMenuHelper getPopupMenu() {
		PopupMenuHelper ret = popupMenuRef.get();
		if (ret==null) {
			ret  = new PopupMenuHelper(context, null, null);
			popupMenuRef = new WeakReference<>(ret);
		}
		return ret;
	}
}
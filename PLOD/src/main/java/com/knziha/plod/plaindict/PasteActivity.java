package com.knziha.plod.plaindict;

import static com.knziha.plod.plaindict.MainShareActivity.SingleTaskFlags;
import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_FLOAT_SEARCH;
import static com.knziha.plod.plaindict.PDICMainAppOptions.getHideFloatFromRecent;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.RequiresApi;

import com.knziha.plod.PlainUI.FloatBtn;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.widgets.ViewUtils;

import java.util.List;

/**
 * Written by KnIfER on 2022
 * see chaosity/悬浮窗、后台状态获取剪贴板内容.txt
 */
public class PasteActivity extends Activity {
	private ActivityManager.AppTask task;
	private final static boolean hideTask = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(Color.TRANSPARENT);
		}
		paste();
		setTaskHidden();
	}
	
	void setTaskHidden() {
		if (hideTask) {
			if (task == null) {
				ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
				List<ActivityManager.AppTask> tasks = am.getAppTasks();
				int taskId = getTaskId();
				for (int i = 0; i < tasks.size(); i++) {
					if (tasks.get(i).getTaskInfo().id == taskId) {
						task = tasks.get(i);
						break;
					}
				}
			}
			if (task != null) {
				task.setExcludeFromRecents(true);
			}
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		paste();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	Runnable runn = new Runnable() {
		@Override
		public void run() {
			ProcessIntent(getIntent());
			if (hideTask) {
				moveTaskToBack(true);
			} else {
				finish();
			}
		}
	};
	
	public void paste() {
		AgentApplication app = (AgentApplication) getApplication();
		try {
			app.floatApp.a.hdl.postDelayed(runn, 100);
		} catch (Exception ignored) { }
	}
	
	public void ProcessIntent(Intent thisIntent) {
		if (thisIntent.hasExtra(FloatBtn.EXTRA_GETTEXT)) {
			AgentApplication app = (AgentApplication) getApplication();
			boolean floating = app.floatApp != null && app.floatApp.isFloating();
			String debugString = null;
			if (floating) {
				debugString = SU.valueOf(app.floatApp.getFloatBtn().getPrimaryClip());
			} else {
				ClipboardManager clipMan = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData pclip = clipMan.getPrimaryClip();
				if (pclip!=null && pclip.getItemCount()>0) {
					debugString = SU.valueOf(pclip.getItemAt(0).getText());
				}
			}
			CMN.debug("getPrimaryClip::", debugString);
			if (debugString==null && PDICMainAppOptions.storeAppId() && true) {
				debugString = FloatBtn.EXTRA_GETTEXT;
			}
			if (debugString!=null) {
				Intent newTask = new Intent(Intent.ACTION_MAIN);
				newTask.setType(Intent.CATEGORY_DEFAULT);
				newTask.putExtra(Intent.EXTRA_TEXT,debugString);
				newTask.putExtra(FloatBtn.EXTRA_FROMPASTE,true);
				newTask.setClass(this, PDICMainActivity.class);
				newTask.setFlags(SingleTaskFlags);
				if (floating) {
					app.floatApp.a.processIntent(newTask, false);
				} else {
					startActivity(newTask);
				}
			}
		}
	}
}
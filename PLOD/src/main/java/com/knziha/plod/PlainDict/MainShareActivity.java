package com.knziha.plod.plaindict;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;

import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_APP_AUTO;
import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_FLOAT_SEARCH;

import com.knziha.plod.PlainUI.FloatBtn;

/**
 * Created by KnIfER on 2019
 */
public class MainShareActivity extends Activity {
	private String debugString;
	static ActivityManager.AppTask hiddenId;
	public final static int SingleTaskFlags = Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP;
	public static boolean launched;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setTheme(R.style.AppTheme);
		ProcessIntent(getIntent());
		finish();
		launched=true;
	}

	public void ProcessIntent(Intent thisIntent) {
		CMN.debug("processIntent::MainShare");
		debugString=null;
		int forceTarget = -1;
		if(thisIntent!=null) {
			String action = thisIntent.getAction();
			forceTarget = thisIntent.getIntExtra("force", -1);
			CMN.debug("forceTarget", forceTarget, thisIntent.hasExtra(FloatBtn.EXTRA_GETTEXT));
			if (forceTarget==-1) {
				if(Intent.ACTION_MAIN.equals(action)) {
					CMN.debug("主程转发");
					thisIntent.setClass(getBaseContext(),PDICMainActivity.class);
					thisIntent.setFlags(SingleTaskFlags);
					startMainActivity(thisIntent);
					return;
				}
				if(Intent.ACTION_VIEW.equals(action)) {
					Uri url = thisIntent.getData();
					if(url!=null) {
						CMN.debug("ProcessIntent_url", url);
						Intent newTask = new Intent(Intent.ACTION_MAIN);
						newTask.setType(Intent.CATEGORY_DEFAULT);
						newTask.putExtra(Intent.EXTRA_TEXT,debugString);
						newTask.setClass(getBaseContext(),PDICMainActivity.class);
						newTask.setFlags(SingleTaskFlags);
						newTask.setData(url);
						startMainActivity(newTask);
						return;
					}
				}
			}
			debugString = thisIntent.getStringExtra(Intent.EXTRA_TEXT);
			if (debugString==null) {
				if (thisIntent.hasExtra(FloatBtn.EXTRA_GETTEXT)) {
					debugString = FloatBtn.EXTRA_GETTEXT;
					forceTarget = PLAIN_TARGET_APP_AUTO;
				} else {
					debugString = thisIntent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
				}
			}
		}
		CMN.Log("force", forceTarget, debugString);
		if(debugString!=null) {
			PDICMainAppOptions opt = new PDICMainAppOptions(this);
			opt.getSecondFlag();
			int ShareTarget = forceTarget!=-1?forceTarget:opt.getShareToTarget();
			if(ShareTarget==PLAIN_TARGET_FLOAT_SEARCH){//浮动搜索
				//getApplicationContext().startActivity(new Intent(this,FloatSearchActivity.class).putExtra("EXTRA_QUERY", debugString));
				if (PDICMainAppOptions.getHideFloatFromRecent() && hiddenId!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					MainShareActivity.hiddenId.setExcludeFromRecents(false);
				}
				Intent popup = new Intent().setClassName("com.knziha.plod.plaindict", "com.knziha.plod.plaindict.FloatActivitySearch").putExtra("EXTRA_QUERY", debugString);
				//this, FloatActivitySearch.class
				popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				//CMN.Log("pop that way!");
				getApplicationContext().startActivity(popup);
			} else {//主程序
				Intent newTask = new Intent(Intent.ACTION_MAIN);
				newTask.setType(thisIntent.getType());
				if (debugString.equals(FloatBtn.EXTRA_GETTEXT)) {
					newTask.putExtra(FloatBtn.EXTRA_GETTEXT,true);
				} else {
					newTask.putExtra(Intent.EXTRA_TEXT,debugString);
				}
				CMN.debug("主程序", CMN.id(debugString));
				newTask.putExtra(Intent.EXTRA_SHORTCUT_ID, ShareTarget);
				newTask.setClass(getBaseContext(),PDICMainActivity.class);
//				//|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				newTask.setFlags(SingleTaskFlags);
				startMainActivity(newTask);
			}
		}
	}
	
	private void startMainActivity(Intent intent) {
		CMN.debug("startMainActivity::", intent);
		AgentApplication app = (AgentApplication) getApplication();
		if (app.floatApp!=null && app.floatApp.isFloating()) {
			app.floatApp.a.processIntent(intent, false);
			app.floatApp.expand(false);
			app.floatApp.a.moveTaskToBack(false);
		} else {
			intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
	
}
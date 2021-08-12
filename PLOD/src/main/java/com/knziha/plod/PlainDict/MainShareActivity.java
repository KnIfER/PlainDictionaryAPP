package com.knziha.plod.plaindict;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_FLOAT_SEARCH;

/**
 * Recreated by KnIfER on 2019
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
		debugString=null;
		int forceTarget = -1;
		if(thisIntent!=null) {
			String action = thisIntent.getAction();
			forceTarget = thisIntent.getIntExtra("force", -1);
			if (forceTarget==-1)
			if(action!=null && action.equals(Intent.ACTION_MAIN)) {
				//CMN.Log("主程转发");
				thisIntent.setClass(getBaseContext(),PDICMainActivity.class);
				thisIntent.setFlags(SingleTaskFlags);
				startActivity(thisIntent);
				return;
			}
			if(action!=null && action.equals(Intent.ACTION_VIEW)) {
				Uri url = thisIntent.getData();
				if(url!=null) {
					CMN.Log("ProcessIntent_url", url);
					Intent newTask = new Intent(Intent.ACTION_MAIN);
					newTask.setType(Intent.CATEGORY_DEFAULT);
					newTask.putExtra(Intent.EXTRA_TEXT,debugString);
					newTask.setClass(getBaseContext(),PDICMainActivity.class);
					newTask.setFlags(SingleTaskFlags);
					newTask.setData(url);
					startActivity(newTask);
					return;
				}
			}
			debugString = thisIntent.getStringExtra(Intent.EXTRA_TEXT);
		}
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
				newTask.putExtra(Intent.EXTRA_TEXT,debugString);
				CMN.Log("主程序", CMN.id(debugString));
				newTask.putExtra(Intent.EXTRA_SHORTCUT_ID, ShareTarget);
				newTask.setClass(getBaseContext(),PDICMainActivity.class);
//				//|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				newTask.setFlags(SingleTaskFlags);
				startActivity(newTask);
			}
		}
	}

}
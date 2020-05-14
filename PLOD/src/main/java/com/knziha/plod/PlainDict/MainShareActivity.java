package com.knziha.plod.PlainDict;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Recreated by KnIfER on 2019
 */
public class MainShareActivity extends AppCompatActivity {
	private String debugString;
	static ActivityManager.AppTask hiddenId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ProcessIntent(getIntent());
		finish();
	}

	public void ProcessIntent(Intent intent) {
		debugString=null;
		if(intent!=null) {
			debugString = intent.getStringExtra(Intent.EXTRA_TEXT);
		}
		if(debugString!=null){
			PDICMainAppOptions opt = new PDICMainAppOptions(this);
			opt.getSecondFlag();
			int ShareTarget = PDICMainAppOptions.getShareTarget();
			if(ShareTarget==3){//浮动搜索
				//getApplicationContext().startActivity(new Intent(this,FloatSearchActivity.class).putExtra("EXTRA_QUERY", debugString));
				if (PDICMainAppOptions.getHideFloatFromRecent() && hiddenId!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					MainShareActivity.hiddenId.setExcludeFromRecents(false);
				}
				Intent popup = new Intent().setClassName("com.knziha.plod.plaindict", "com.knziha.plod.PlainDict.FloatActivitySearch").putExtra("EXTRA_QUERY", debugString);
				//this, FloatActivitySearch.class
				popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				//CMN.Log("pop that way!");
				getApplicationContext().startActivity(popup);
			}else{//主程序
				Intent newTask = new Intent(Intent.ACTION_MAIN);
				newTask.setType(intent.getType());
				newTask.putExtra(Intent.EXTRA_TEXT,debugString);
				newTask.putExtra(Intent.EXTRA_SHORTCUT_ID,ShareTarget);
				newTask.setClass(getBaseContext(),PDICMainActivity.class);
//				//|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				newTask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(newTask);
			}
		}
	}

}
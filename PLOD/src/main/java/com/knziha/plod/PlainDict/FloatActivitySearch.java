package com.knziha.plod.plaindict;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import java.util.List;


/**
 * 单实例浮动搜索 <br/>
 * Single Instance Float Activity that can be launched by paste bin monitor、<br/>
 * 		by 3rd party share intent to PlainDict and of course <br/>
 * 		by our internal multi-dimensionally customizable central share panel. ( MDCCSP )<br/>
 * Created by 2019 on KnIfER
 */
public class FloatActivitySearch extends FloatSearchActivity {
	boolean hidden;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this_instanceof_FloarActivitySearch = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
					getTitle().toString(),//title
					BitmapFactory.decodeResource(getResources(), R.drawable.move),//图标
					ResourcesCompat.getColor(getResources(), R.color.colorPrimary,
							getTheme()));
			setTaskDescription(taskDesc);
			hidden=PDICMainAppOptions.getHideFloatFromRecent();
			if(hidden)
				setTaskHidden(hidden);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		//CMN.Log("onNewIntent");
		super.onNewIntent(intent);
		
		if(!hasWindowFocus()){
			ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			if(manager!=null) manager.moveTaskToFront(getTaskId(), 0);
		}
		
		bIsFirstLaunch=true;

		processIntent(intent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			boolean newHidden=PDICMainAppOptions.getHideFloatFromRecent();
			if(hidden!=newHidden || MainShareActivity.hiddenId!=null) {
				setTaskHidden(newHidden);
			}
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	void setTaskHidden(boolean hidden_) {
		hidden=hidden_;
		ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.AppTask> tasks = am.getAppTasks();
		for (int i = 0; i < tasks.size(); i++) {
			if(tasks.get(i).getTaskInfo().id==getTaskId()){
				MainShareActivity.hiddenId=tasks.get(i);
				tasks.get(i).setExcludeFromRecents(hidden);
				break;
			}
		}
	}

	@Override
	protected void exit() {
		if(PDICMainAppOptions.getFloatClickHideToBackground()){
			moveTaskToBack(false);
		} else {
			super.exit();
		}
	}

	@Override
	protected void onDestroy() {
		//CMN.Log("onDestroy");
		MainShareActivity.hiddenId=null;
		super.onDestroy();
	}
}
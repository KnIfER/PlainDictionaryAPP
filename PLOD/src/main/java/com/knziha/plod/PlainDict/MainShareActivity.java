package com.knziha.plod.PlainDict;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Recreated by KnIfER on 2019
 */
public class MainShareActivity extends AppCompatActivity {
	private String debugString;

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
			String type = intent.getType();
			if(type!=null)
				if (type.contains("text/plain"))
					debugString = intent.getStringExtra(Intent.EXTRA_TEXT);
		}
		if(debugString!=null){
			PDICMainAppOptions opt = new PDICMainAppOptions(this);
			opt.getSecondFlag();
			int ShareTarget = PDICMainAppOptions.getShareTarget();
			if(ShareTarget==3){//浮动搜索
				startActivity(new Intent(this,FloatSearchActivity.class).putExtra("EXTRA_QUERY", debugString));
			}else{//主程序
				Intent newTask = new Intent(Intent.ACTION_MAIN);
				newTask.setType(intent.getType());
				newTask.putExtra(Intent.EXTRA_TEXT,debugString);
				newTask.putExtra(Intent.EXTRA_SHORTCUT_ID,ShareTarget);
				newTask.setClass(getBaseContext(),PDICMainActivity.class);
				newTask.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(newTask);
			}
		}
	}

}
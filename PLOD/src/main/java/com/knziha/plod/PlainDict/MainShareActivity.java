package com.knziha.plod.PlainDict;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/**
 * Recreated by KnIfER on 2019
 */
public class MainShareActivity extends Toastable_Activity{
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
			Intent newTask = new Intent(Intent.ACTION_MAIN);
			newTask.setType(intent.getType());
			newTask.putExtra(Intent.EXTRA_TEXT,debugString);
			newTask.setClass(getBaseContext(),PDICMainActivity.class);
			newTask.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(newTask);
		}
	}

}
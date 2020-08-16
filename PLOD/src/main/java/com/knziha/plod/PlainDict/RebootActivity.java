package com.knziha.plod.PlainDict;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/**
 * Recreated by KnIfER on 2019
 */
public class RebootActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent newTask = new Intent(Intent.ACTION_MAIN);
		newTask.setClass(getBaseContext(),PDICMainActivity.class);
		newTask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(newTask);
		finish();
	}
}
package com.knziha.plod.PlainDict;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Recreated by KnIfER on 2019
 */
public class FloatSearchHelper extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent popup = new Intent(Intent.ACTION_MAIN);
		popup.setComponent(new ComponentName("com.knziha.plod.plaindict","com.knziha.plod.PlainDict.FloatSearchActivity"));

		popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		getApplicationContext().startActivity(popup);

		finish();
	}

}
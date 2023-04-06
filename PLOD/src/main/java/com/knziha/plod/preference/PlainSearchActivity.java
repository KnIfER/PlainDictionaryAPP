package com.knziha.plod.preference;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.preference.AndroidResources;

import com.knziha.plod.widgets.ViewUtils;

/**
 * Created by KnIfER on 2023
 */
public class PlainSearchActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setTheme(R.style.AppTheme);
		Intent data = new Intent();
		data.putExtra(Intent.EXTRA_TEXT, "happy");
		setResult(100, data);
		FrameLayout vg = new FrameLayout(this); setContentView(vg);
		new RedirectTargets(this, vg);
		//finish();
	}
}
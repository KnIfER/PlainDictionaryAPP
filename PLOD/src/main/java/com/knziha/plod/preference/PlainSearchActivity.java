package com.knziha.plod.preference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.appcompat.app.CMN;

import org.json.JSONObject;

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
		data.putExtra(Intent.EXTRA_TEXT, RedirectTargets.class.getName());
		setResult(100, data);
//		try {
//			FrameLayout vg = new FrameLayout(this); setContentView(vg);
//			new RedirectTargets(this, this, vg, new JSONObject(), new String[]{"", "", "", ""}, null);
//		} catch (Exception e) {
//			CMN.debug(e);
//		}
		finish();
	}
}
package com.knziha.plod.PlainUI;

import android.util.DisplayMetrics;

public class ScreenConfig {
	float widthPixels;
	float heightPixels;
	
	public ScreenConfig(DisplayMetrics dm) {
		widthPixels = dm.widthPixels;
		heightPixels = dm.heightPixels;
	}
	
	public boolean sameScreen(DisplayMetrics dm) {
		return widthPixels == dm.widthPixels && heightPixels == dm.heightPixels;
	}
}

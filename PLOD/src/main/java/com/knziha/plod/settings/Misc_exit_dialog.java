package com.knziha.plod.settings;

import android.os.Bundle;

public class Misc_exit_dialog extends Misc {
	public final static int id=13;
	//初始化
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		findPreference("cat_1").setVisible(false);
	}
}
package com.knziha.plod.PlainUI;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

public class PlainDialog extends Dialog {
	public PlainDialog(@NonNull Context context) {
		super(context);
	}
	
	public interface BackPrevention {
		boolean onBackPressed();
	}
	
	public BackPrevention mBackPrevention;
	
	@Override
	public void onBackPressed() {
		if (mBackPrevention==null || !mBackPrevention.onBackPressed()) {
			super.onBackPressed();
		}
	}
}
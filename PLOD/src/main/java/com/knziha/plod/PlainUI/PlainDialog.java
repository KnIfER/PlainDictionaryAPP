package com.knziha.plod.PlainUI;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

public class PlainDialog extends Dialog {
	public PlainDialog(@NonNull Context context) {
		super(context, R.style.resizeDlgTheme);
	}
	
	public interface BackPrevention {
		boolean onBackPressed(); // return true if handled
	}
	
	public BackPrevention mBackPrevention;
	
	@Override
	public void onBackPressed() {
		if (mBackPrevention==null || !mBackPrevention.onBackPressed()) {
			super.onBackPressed();
		}
	}
	
}

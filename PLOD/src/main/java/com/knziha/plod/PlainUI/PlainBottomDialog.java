package com.knziha.plod.PlainUI;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

public class PlainBottomDialog extends BottomSheetDialog {
	public PlainBottomDialog(@NonNull Context context) {
		super(context, R.style.resizeDlgTheme1);
	}
	
	public PlainDialog.BackPrevention mBackPrevention;
	
	@Override
	public void onBackPressed() {
		if (mBackPrevention==null || !mBackPrevention.onBackPressed()) {
			super.onBackPressed();
		}
	}
	
}

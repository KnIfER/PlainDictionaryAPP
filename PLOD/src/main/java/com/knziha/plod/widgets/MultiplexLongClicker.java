package com.knziha.plod.widgets;

import android.view.View;

import com.knziha.plod.PlainDict.R;

public class MultiplexLongClicker implements View.OnLongClickListener {
	@Override
	public boolean onLongClick(View v) {
		v.setTag(R.id.long_clicked, false);
		v.performClick();
		v.setTag(R.id.long_clicked, null);
		return true;
	}
}

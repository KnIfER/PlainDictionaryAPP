package com.jaredrummler.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class LinearLayoutmy extends LinearLayout {
	public LinearLayoutmy(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public LinearLayoutmy(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public LinearLayoutmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}
	public LinearLayoutmy(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//super.onInterceptTouchEvent(ev);
		//return ((ColorPickerView) getChildAt(0)).wannaTouch();
		return true;
	}



}


package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class XYLinearLayout extends LinearLayout {
	public float lastX;
	public float lastY;
	
	public XYLinearLayout(Context context) {
		this(context, null);
	}
	
	public XYLinearLayout(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public XYLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		lastX = ev.getX();
		lastY = ev.getY();
		return super.dispatchTouchEvent(ev);
	}
}

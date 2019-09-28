package com.jaredrummler.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ProtectiveScrollView extends ScrollView {
	public static boolean dontIntercept = false;
	public ProtectiveScrollView(Context context) {
		super(context);
	}
	public ProtectiveScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ProtectiveScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            ProtectiveScrollView.dontIntercept = false;
        }
		if(dontIntercept) return false;
		return super.onInterceptTouchEvent(ev);
	}
}

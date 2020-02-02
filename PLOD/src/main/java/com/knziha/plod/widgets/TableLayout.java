package com.knziha.plod.widgets;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class TableLayout extends LinearLayout {
	public TableLayout(Context context) {
		super(context);
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		return true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int action = event.getAction();
			//if(action==MotionEvent.ACTION_MOVE ||action==MotionEvent.ACTION_DOWN||child.getLeft()<x && child.getRight()>x)
			MotionEvent ev = MotionEvent.obtain(event);
			ev.setLocation(ev.getX()-child.getLeft(), ev.getY());
			child.dispatchTouchEvent(ev);
		}
		return true;
	}

}

package com.knziha.plod.widgets;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

public class TableLayout extends LinearLayout {
	public TableLayout(Context context) {
		super(context);
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		return true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		//float x = event.getX();
		//int action = event.getAction();
		int size = getChildCount()-1;
		for (int i = size; i >= 0; i--) {
			View child = getChildAt(i);
			if(child instanceof ListView) {
				MotionEvent ev = MotionEvent.obtain(event);
				//if(action==MotionEvent.ACTION_MOVE ||action==MotionEvent.ACTION_DOWN||child.getLeft()<x && child.getRight()>x)
				ev.setLocation(ev.getX()-child.getLeft(), ev.getY());
				child.dispatchTouchEvent(ev);
				ev.recycle();
			}
		}
		return true;
	}

}

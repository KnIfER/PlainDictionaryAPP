package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.core.view.ViewCompat;

import com.knziha.plod.dictionarymanager.files.BooleanSingleton;
import com.knziha.plod.plaindict.CMN;

public class ListViewOverscroll extends ListView {// for mute it's scroll
	boolean ds=true;
	public int dir;
	public float orgX, orgY;
	public OnClickListener overScroll;
    public ListViewOverscroll(Context context) {
        super(context);
    }
 
    public ListViewOverscroll(Context context, AttributeSet attrs,
								int defStyle) {
        super(context, attrs, defStyle);
    }

    public ListViewOverscroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getActionMasked()==MotionEvent.ACTION_DOWN) {
			CMN.Log("ACTION_DOWN");
			orgX = ev.getX();
			orgY = ev.getY();
		}
		if (ev.getActionMasked()==MotionEvent.ACTION_UP || ev.getActionMasked()==MotionEvent.ACTION_CANCEL) {
			CMN.Log("ACTION_UP", getOverScrollMode());
			if (!ds) {
				if(dir==0) {
					dir = (int) (orgY - ev.getY());
				}
				overScroll.onClick(this);
				ds = true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		CMN.debug("onOverScrolled::", scrollX, scrollY, clampedX, clampedY, getScrollY());
    	super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    	if(clampedY && ds) {
			dir = scrollY;
			ds=false;
		}
	}
}
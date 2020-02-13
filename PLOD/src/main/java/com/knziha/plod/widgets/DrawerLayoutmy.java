package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.knziha.plod.PlainDict.CMN;

public class DrawerLayoutmy extends androidx.drawerlayout.widget.DrawerLayout {
    public SamsungLikeScrollBar scrollbar2guard;
    public PopupWindow popupToGuard;

	public DrawerLayoutmy(@NonNull Context context) {
        super(context);
    }

    public DrawerLayoutmy(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayoutmy(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
		if(popupToGuard!=null){
			popupToGuard.dismiss();
			return true;
		}
        int action = ev.getAction();
        return super.onTouchEvent(ev);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
		CMN.Log("onInterceptTouchEvent");
    	if(popupToGuard!=null)
    		return true;

        //if(scrollbar2guard!=null)
        //    if(scrollbar2guard.isDragging)
        //        return false;


        return super.onInterceptTouchEvent(ev);
    }
}

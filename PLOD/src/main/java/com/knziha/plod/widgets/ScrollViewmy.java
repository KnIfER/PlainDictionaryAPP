package com.knziha.plod.widgets;

import com.knziha.plod.dictionarymanager.files.BooleanSingleton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class ScrollViewmy extends ScrollView {// for mute it's scroll
    public static OnTouchListener dummyOntouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}};
	private ScrollViewListener scrollViewListener = null;
    public boolean bScrollEnabled=true;
	public BooleanSingleton touchFlag=new BooleanSingleton(true);
    
    public ScrollViewmy(Context context) {
        super(context);
    }

    public ScrollViewmy(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollViewmy(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
    
    public interface ScrollViewListener { 
    	void onScrollChanged(View scrollView, int x, int y, int oldx, int oldy);

    	}
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	if(touchFlag!=null) touchFlag.first=true;
		return super.onTouchEvent(ev);
    }    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if(touchFlag!=null) touchFlag.first=true;
    	if(bScrollEnabled) {
    		return super.onInterceptTouchEvent(ev);
    	}else {
    		return false;
    	}
    }
}
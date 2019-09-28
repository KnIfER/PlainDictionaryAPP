package com.knziha.plod.widgets;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoScrollViewPager extends ViewPager {  
    private boolean noScroll = false;  
  
    public NoScrollViewPager(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
    }  
  
    public NoScrollViewPager(Context context) {  
        super(context);  
    }  
  
    public void setNoScroll(boolean noScroll) {  
        this.noScroll = noScroll;  
    }  
  
    @Override  
    public void scrollTo(int x, int y) {  
        super.scrollTo(x, y);  
    }  
    
    OnTouchListener L;
    
    @Override  
    public void setOnTouchListener(OnTouchListener l) {
        /* return false;//super.onTouchEvent(arg0); */  
        super.setOnTouchListener(L=l);
        
    }
        
    @Override  
    public boolean onTouchEvent(MotionEvent event) {
        /* return false;//super.onTouchEvent(arg0); */
        if (noScroll) {
            return false;  
        }else  
            return super.onTouchEvent(event);  
    }  
  
    @Override  
    public boolean onInterceptTouchEvent(MotionEvent event) { 
    	boolean ret = false;
        if (noScroll)  {}else {
        	ret = super.onInterceptTouchEvent(event);
        }
        if(!ret) {
        	if(L!=null) L.onTouch(this, event);
        }
        return ret;  
    }  
  
    @Override  
    public void setCurrentItem(int item, boolean smoothScroll) {  
        super.setCurrentItem(item, smoothScroll);  
    }  
  
    @Override  
    public void setCurrentItem(int item) {  
        super.setCurrentItem(item);  
    }  
  
}  
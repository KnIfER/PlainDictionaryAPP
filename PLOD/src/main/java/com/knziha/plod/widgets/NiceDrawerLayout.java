package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.drawerlayout.widget.DrawerLayout;

//https://www.cnblogs.com/kangweifeng/p/5310442.html
//https://blog.csdn.net/weixin_41147026/article/details/81588732
public class NiceDrawerLayout extends DrawerLayout {
	private int mTouchSlop;
	private float mLastMotionX;
	private float mLastMotionY;
	public boolean dragging;
	public boolean dragEnabled = true;
	
	public NiceDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			final float x = ev.getX();
			final float y = ev.getY();
			switch (ev.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					dragging = true;
					mLastMotionX = x;
					mLastMotionY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					int xDiff = (int) Math.abs(x - mLastMotionX);
					int yDiff = (int) Math.abs(y - mLastMotionY);
					final int x_yDiff = xDiff * xDiff + yDiff * yDiff;
					boolean xMoved = x_yDiff > mTouchSlop * mTouchSlop;
					if (xMoved) {
						if (xDiff > yDiff * 4) {
							return true;
						} else {
							return false;
						}
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					dragging = false;
					break;
			}
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException ex) {
		}
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int act = ev.getActionMasked();
		if (act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL) {
			dragging = false;
		}
		try {
			return super.onTouchEvent(ev);
		} catch (Exception ex) {
		}
		return false;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!dragEnabled) {
			View child = getChildAt(1);
			if(child!=null)
				return child.dispatchTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}
}
package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.knziha.plod.PlainDict.R;

public class DomesticSeekDog extends SeekBar {
	public DomesticSeekDog(Context context) {
		this(context, null);
	}
	
	public DomesticSeekDog(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.seekBarStyle);
	}
	
	public DomesticSeekDog(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			MotionEvent evup = MotionEvent.obtain(event);
			evup.setAction(MotionEvent.ACTION_MOVE);
			dispatchTouchEvent(evup);
			evup.recycle();
		}
		return super.onTouchEvent(event);
	}
}
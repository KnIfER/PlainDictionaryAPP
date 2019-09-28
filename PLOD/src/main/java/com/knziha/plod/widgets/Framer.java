package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class Framer extends FrameLayout {
	public Framer(Context context) {
		super(context);
	}
	public Framer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public Framer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public Framer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public int mMaxHeight=-1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(mMaxHeight!=-1) {
            if(heightSize>mMaxHeight) {
            	heightMeasureSpec = mMaxHeight;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

	

}

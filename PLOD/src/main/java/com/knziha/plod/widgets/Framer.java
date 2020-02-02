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
        if(mMaxHeight!=-1) {
			int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			int heightSize = MeasureSpec.getSize(heightMeasureSpec);

			if (heightMode == MeasureSpec.EXACTLY) {
				heightSize = heightSize <= mMaxHeight ? heightSize
						: mMaxHeight;
			}

			if (heightMode == MeasureSpec.UNSPECIFIED) {
				heightSize = heightSize <= mMaxHeight ? heightSize
						: mMaxHeight;
			}
			if (heightMode == MeasureSpec.AT_MOST) {
				heightSize = heightSize <= mMaxHeight ? heightSize
						: mMaxHeight;
			}

			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

	

}

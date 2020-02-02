package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class MaxHeightLinearLayout extends LinearLayout {
	public int mMaxHeight=0;
	public MaxHeightLinearLayout(Context context) {
		super(context);
	}

	public MaxHeightLinearLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public MaxHeightLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	//https://www.cnblogs.com/carbs/p/5142758.html
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(mMaxHeight<=0){
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
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
		int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,heightMode);
		super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec);
	}

}

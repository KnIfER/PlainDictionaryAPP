package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.knziha.plod.PlainDict.CMN;

public class MinHeightLinearLayout extends LinearLayout {
	public MinHeightLinearLayout(Context context) {
		super(context);
	}

	public MinHeightLinearLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public MinHeightLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMinimumHeight(getMeasuredHeight());
	}
}

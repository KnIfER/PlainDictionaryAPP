package com.knziha.plod.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;

import java.util.ArrayList;


public class SplitPadView extends SplitView {
	public SplitPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void requestLayout() {
		super.requestLayout();
		setPadding(0,0,0,0);
	}
}
    

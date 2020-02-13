package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;


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
    

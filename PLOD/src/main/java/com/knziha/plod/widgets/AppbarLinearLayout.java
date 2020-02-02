package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class AppbarLinearLayout extends LinearLayout {
	public View contentViewToGuard;
	public AppbarLinearLayout(Context context) {
		super(context);
	}

	public AppbarLinearLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public AppbarLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	@Override
	public void setTranslationY(float translationY) {
		super.setTranslationY(translationY);
		if(contentViewToGuard!=null)
			contentViewToGuard.setTranslationY(translationY);
	}
}

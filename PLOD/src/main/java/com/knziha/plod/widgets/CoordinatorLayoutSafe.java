package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class CoordinatorLayoutSafe extends CoordinatorLayout {
	public CoordinatorLayoutSafe(@NonNull Context context) {
		super(context);
	}

	public CoordinatorLayoutSafe(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public CoordinatorLayoutSafe(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void removeView(View view) {
		try {
			super.removeView(view);
		} catch (Exception ignored) { /* 无敌 */ }
	}
}

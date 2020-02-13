package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

/**
 * Created by KnIfER on 2017/11/4.
 */

public class HapticSafeTextView extends TextView {
	public HapticSafeTextView(Context context) {
		this(context, null);
	}

	public HapticSafeTextView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HapticSafeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean performHapticFeedback(int feedbackConstant, int flags) {
		try {
			return super.performHapticFeedback(feedbackConstant, flags);
		} catch (Exception e) {
			e.printStackTrace();/* 无敌 */
		}
		return false;
	}
}

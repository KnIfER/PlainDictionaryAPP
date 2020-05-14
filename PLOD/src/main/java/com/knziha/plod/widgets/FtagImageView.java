package com.knziha.plod.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.knziha.plod.PlainDict.R;

public class FtagImageView extends ImageView {
	protected Integer mTag = 0;
	private final static int mTransientMask = ~1;
	public FtagImageView(Context context) {
		super(context);
	}
	
	public FtagImageView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public FtagImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public void setTag(Object tag) {
		int tagNew = (Integer) tag;
		mTag = tagNew;
	}
	
	@Override
	public Object getTag() {
		Integer ret = mTag;
		mTag&=mTransientMask;
		return ret;
	}
}

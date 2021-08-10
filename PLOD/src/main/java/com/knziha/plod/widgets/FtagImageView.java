package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class FtagImageView extends ImageView {
	protected Integer mTag = 0;
	private final static int mTransientMask = ~1;
	
	public FtagImageView(Context context) {
		this(context, null);
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
		mTag |= tagNew;
	}
	
	@Override
	public Object getTag() {
		Integer ret = mTag;
		mTag&=mTransientMask;
		return ret;
	}
}

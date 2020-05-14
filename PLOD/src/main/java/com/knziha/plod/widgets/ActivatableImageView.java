package com.knziha.plod.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.R;

public class ActivatableImageView extends ImageView {
	private Drawable mActiveDrawable;
	private Drawable mDrawable;
	ColorFilter mColorFilter;
	public ActivatableImageView(Context context) {
		super(context);
	}
	
	public ActivatableImageView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ActivatableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mDrawable = getDrawable();
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ActivatableImageView, 0, 0);
		mActiveDrawable = a.getDrawable(0);
		a.recycle();
	}
	
	@Override
	public void setColorFilter(ColorFilter cf) {
		if(!isActivated())
			super.setColorFilter(cf);
		if(cf!=null)
			mColorFilter=cf;
	}
	
	@Override
	public void setActivated(boolean activated) {
		if(mActiveDrawable!=null){
			setColorFilter(activated?null:mColorFilter);
			setImageDrawable(activated?mActiveDrawable:mDrawable);
		} else {
			super.setActivated(activated);
		}
	}
}

package com.knziha.plod.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.R;

public class ActivatableImageView extends ImageView {
	private Drawable mActiveDrawable;
	private Drawable mDrawable;
	private boolean bActivedShowRawColor;
	ColorFilter mColorFilter;
	public ActivatableImageView(Context context) {
		this(context, null);
	}
	
	public ActivatableImageView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ActivatableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mDrawable = getDrawable();
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ActivatableImageViewSty, 0, 0);
		mActiveDrawable = a.getDrawable(R.styleable.ActivatableImageViewSty_src0);
		bActivedShowRawColor = a.getBoolean(R.styleable.ActivatableImageViewSty_activedSR, true);
		a.recycle();
	}
	
	public void setActiveDrawable(Drawable activeDrawable, boolean showRawColor) {
		mDrawable = getDrawable();
		mActiveDrawable = activeDrawable;
		bActivedShowRawColor = showRawColor;
	}
	
	@Override
	public void setColorFilter(ColorFilter cf) {
		if(!bActivedShowRawColor||!isActivated())
			super.setColorFilter(cf);
		if(cf!=null)
			mColorFilter=cf;
	}
	
	@Override
	public void setActivated(boolean activated) {
		if(mActiveDrawable!=null){
			if(bActivedShowRawColor) {
				setColorFilter(activated?null:mColorFilter);
			} else {
				super.setActivated(activated);
			}
			setImageDrawable(activated?mActiveDrawable:mDrawable);
		} else {
			super.setActivated(activated);
		}
	}
}

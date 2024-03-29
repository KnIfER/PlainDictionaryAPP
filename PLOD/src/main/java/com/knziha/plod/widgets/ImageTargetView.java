package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class ImageTargetView extends ImageView {
	public ImageTargetView(Context context) {
		super(context);
	}
	
	public ImageTargetView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ImageTargetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
//	@Override
//	public void setImageResource(int resId) {
//		//super.setImageResource(resId);
//		CMN.Log("setImageResource");
//	}
	
	@Override
	public void setImageDrawable(@Nullable Drawable drawable) {
		if(drawable==FuckGlideDrawable) {
			return;
		}
		if(drawable==null) {
			return;
		}
		//CMN.Log("setImageDrawable", getDrawable(), drawable, this);
		super.setImageDrawable(drawable);
	}
	
//	@Override
//	public void setImageBitmap(Bitmap bm) {
//		if(bm==null) {
//			return;
//		}
//		//CMN.Log("setImageBitmap", bm);
//		super.setImageBitmap(bm);
//	}
	
	public final static ColorDrawable FuckGlideDrawable = new ColorDrawable();
}

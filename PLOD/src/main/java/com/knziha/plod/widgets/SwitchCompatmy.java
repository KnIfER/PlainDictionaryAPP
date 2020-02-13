package com.knziha.plod.widgets;

import com.knziha.plod.PlainDict.R;
import com.knziha.plod.PlainDict.CMN;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import android.util.AttributeSet;

public class SwitchCompatmy extends SwitchCompat {
	
	public SwitchCompatmy(Context context) {
		super(context);
	}
	public SwitchCompatmy(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs,0);
	}
	public SwitchCompatmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context,attrs, defStyleAttr);
	}
	
	Drawable mHintDrawable;
	private int mHintLeftPadding,mHintSurrondingPad;
	private void init(Context context,AttributeSet attrs, int defStyleAttr) {
		if(CMN.ShallowHeaderBlue==0)
			CMN.ShallowHeaderBlue=ContextCompat.getColor(getContext(), R.color.ShallowHeaderBlue);
		//int src_resource = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
		if(R.styleable.constances[0]==0) {
			int[] system_iv_identifiers=WebViewmy.getReflactIntArray("com.android.internal.R$styleable", "ImageView");
			int system_src_id=WebViewmy.getReflactField("com.android.internal.R$styleable", "ImageView_src");
			if(system_iv_identifiers!=null) if(system_src_id>=0 && system_src_id<system_iv_identifiers.length)
				R.styleable.constances[0]=system_iv_identifiers[system_src_id];
			//CMN.Log(R.styleable.constances[0]);
		}
		int[] fetcherHolder=R.styleable.triple;
		fetcherHolder[0]=R.styleable.constances[0];
		fetcherHolder[1]=R.styleable.UIStyles[0];
		fetcherHolder[2]=R.styleable.UIStyles[1];
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, fetcherHolder, defStyleAttr, defStyleAttr);
		mHintDrawable = a.getDrawable(0);
		mHintLeftPadding = (int) a.getDimension(1, 0);
		mHintSurrondingPad = (int) a.getDimension(2, 0);
		//CMN.Log(mHintDrawable);
		a.recycle();

		if(mHintDrawable!=null) {
			mHintDrawable=mHintDrawable.mutate();
			resizeHintDrawable();
			DrawableCompat.setTint(mHintDrawable, CMN.ShallowHeaderBlue);
		}
	}

		
	@Override
	public void setChecked(boolean checked) {
		 super.setChecked(checked);
		 if(mHintDrawable!=null) {
			mHintDrawable.setState(getDrawableState());
			int b=getPaddingTop();
			int a=getPaddingLeft()+(checked?mHintLeftPadding:(-mHintLeftPadding+getWidth()/2));
			resizeHintDrawable();
			 DrawableCompat.setTint(mHintDrawable, checked?Color.WHITE:CMN.ShallowHeaderBlue);
		 }
	}
	 
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(mHintDrawable!=null) {
			resizeHintDrawable();
		}
		//CMN.Log("sw onSizeChanged");
	}
	
	private void resizeHintDrawable() {
		int width = getHeight()-2*mHintSurrondingPad;
		int b=getPaddingTop()+mHintSurrondingPad;
		int a=isChecked()?mHintLeftPadding+mHintSurrondingPad:(getWidth()-width-mHintSurrondingPad-mHintLeftPadding);
		mHintDrawable.setBounds(a,b,a+width,b+width);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mHintDrawable!=null) {
			mHintDrawable.draw(canvas);
		}
	}


}

package com.knziha.plod.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.knziha.filepicker.utils.CMNF;
import com.knziha.plod.PlainDict.R;

public class SwitchCompatBeautiful extends SwitchCompat {
    public static boolean  bForbidRquestLayout;
    Drawable mHintDrawable;
    private int mHintLeftPadding,mHintSurrondingPad;

    public SwitchCompatBeautiful(Context context) {
        this(context, null);
    }
    public SwitchCompatBeautiful(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.switchStyle);
    }
    public SwitchCompatBeautiful(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs, defStyleAttr);
    }

    @SuppressLint("ResourceType")
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
		if(CMNF.ShallowHeaderBlue==0)
			CMNF.ShallowHeaderBlue = ContextCompat.getColor(getContext(), R.color.ShallowHeaderBlue);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleCheckBox, defStyleAttr, 0);
		mHintDrawable = a.getDrawable(R.styleable.CircleCheckBox_src0);
		mHintLeftPadding = (int) a.getDimension(R.styleable.CircleCheckBox_dim0, 0);
		mHintSurrondingPad = (int) a.getDimension(R.styleable.CircleCheckBox_dim1, 0);
		//CMN.Log(mHintDrawable, mHintLeftPadding, mHintSurrondingPad);
		a.recycle();

		if(mHintDrawable!=null) {
			mHintDrawable=mHintDrawable.mutate();
			resizeHintDrawable();
            setDrawableTint(CMNF.ShallowHeaderBlue);
        }
	}
		
	@Override
	public void setChecked(boolean checked) {
		 super.setChecked(checked);
		 if(mHintDrawable!=null) {
			mHintDrawable.setState(getDrawableState());
			resizeHintDrawable();
            setDrawableTint(checked?Color.WHITE: CMNF.ShallowHeaderBlue);
         }
	}

    private void setDrawableTint(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mHintDrawable.setTint(color);
        }else{
            mHintDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void requestLayout() {
        if(!bForbidRquestLayout)
            super.requestLayout();
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
		int width = getHeight()-2*mHintSurrondingPad-getPaddingTop()-getPaddingBottom();
		int b=getPaddingTop()+mHintSurrondingPad;
		boolean b1=isChecked();
		if(getLayoutDirection()== View.LAYOUT_DIRECTION_RTL)b1=!b1;
		int a=b1?mHintLeftPadding+mHintSurrondingPad:(getWidth()-width-mHintSurrondingPad-mHintLeftPadding);
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

package com.knziha.plod.widgets;

import com.knziha.plod.PlainDict.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;

public class CheckableImageView extends ImageView implements Checkable{
	private int CheckedColor=0;

	public CheckableImageView(Context context) {
		super(context);
		init(context, null, 0);
	}
	public CheckableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	public CheckableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}
	public CheckableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr);
	}
	
	
	private void init(Context context,AttributeSet attrs, int defStyleAttr) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CheckableImageview, defStyleAttr, defStyleAttr);
		CheckedColor = a.getColor(0, 0);
		a.recycle();
	}

	private boolean mChecked = false;
	
	@Override
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			refreshDrawableState();
		}
		if(CheckedColor!=0) {
			if(getDrawable()!=null) {
				if(!mChecked) {
					if(getColorFilter()==null) setColorFilter(CheckedColor, PorterDuff.Mode.SRC_IN);
				}else if(mChecked) {
					if(getColorFilter()!=null) setColorFilter(null);
				}
			}
		}
	}
	
	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
	
	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}
 
	@Override
	public boolean isChecked() {
		return mChecked;
	}
 
	@Override
	public void toggle() {
		setChecked(!mChecked);
	}
	
}

package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

public class TextMenuView extends TextView {
	public Drawable leftDrawable;
	public boolean activated;
	public boolean showAtRight;
	
	public TextMenuView(Context context) {
		super(context);
	}

	public TextMenuView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		leftDrawable = getResources().getDrawable(R.drawable.ic_yes_blue);
		leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
		if(!isInEditMode())
			if(GlobalOptions.isDark)
				setTextColor(Color.WHITE);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (leftDrawable!=null && activated) {
			if (showAtRight) {
				int drawableSz = leftDrawable.getIntrinsicWidth();
				int top = (int) ((getMeasuredHeight()-drawableSz)*0.55);
				Layout lay = getLayout();
				int left = getMeasuredWidth() - (int) ((drawableSz)*0.75);
				if (lay != null) {
					left = Math.min(left, (int) lay.getLineRight(0) + drawableSz);
				}
				leftDrawable.setBounds(left, top, left+drawableSz, top+drawableSz);
				leftDrawable.draw(canvas);
			} else {
				int mPaddingLeft = getPaddingLeft();
				int drawableSz = leftDrawable.getIntrinsicWidth();
				int left = (int) ((mPaddingLeft-drawableSz)*0.55);
				int top = (int) ((getMeasuredHeight()-drawableSz)*0.55);
				leftDrawable.setBounds(left, top, left+drawableSz, top+drawableSz);
				leftDrawable.draw(canvas);
			}
		}
	}
	
	@Override
	public void setActivated(boolean activated) {
		if (this.activated!=activated) {
			this.activated = activated;
			invalidate();
		}
	}
	
	@Override
	public boolean isActivated() {
		return activated;
	}
	
	@Override
	public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
		super.onPopulateAccessibilityEvent(event);
		if (activated) {
			try {
				event.getText().add("已勾选");
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
}

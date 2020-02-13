package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class DictionaryTitle extends TextView {
	public boolean isFold;

	public DictionaryTitle(Context context) {
		this(context, null);
	}

	public DictionaryTitle(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DictionaryTitle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(isFold){
			String tail = "<>";
			TextPaint paint = getPaint();
			float baseline=getHeight();
			if(getCompoundDrawables()[2]==null) {
				Paint.FontMetrics fontMetrics = paint.getFontMetrics();
				float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
				baseline = baseline / 2 + distance;
			}
			canvas.drawText(tail, getWidth()-paint.measureText(tail), baseline, paint);
		}
	}
}

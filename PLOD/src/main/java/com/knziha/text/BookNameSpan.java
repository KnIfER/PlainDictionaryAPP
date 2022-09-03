package com.knziha.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.style.ReplacementSpan;

import androidx.appcompat.app.GlobalOptions;

public class BookNameSpan extends ReplacementSpan {
	private final int mBackgroundColor;
	private final int mPaddingLeft;
	private final int mPaddingRight;
	
	public BookNameSpan(int backgroundColor) {
		mBackgroundColor = backgroundColor;
		mPaddingLeft = (int) (GlobalOptions.density*2);
		mPaddingRight = (int) (GlobalOptions.density*3.8);
	}
	
	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end,
					   Paint.FontMetricsInt fm) {
		return (int) (mPaddingLeft +
				paint.measureText(text.subSequence(start, end).toString()) +
				mPaddingRight );
	}
	
	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
					 int bottom, Paint paint) {
		float width = paint.measureText(text.subSequence(start, end).toString());
		RectF rect = new RectF(x, top
				//- paint.getFontMetricsInt().top + paint.getFontMetricsInt().ascent
				, x + width + mPaddingLeft + mPaddingRight, bottom);
		paint.setColor(GlobalOptions.isDark?0x88777777:mBackgroundColor);
		canvas.drawRoundRect(rect, 0, 0, paint);
		
		paint.setColor(GlobalOptions.isDark?0xFFcfcfcf: Color.WHITE);
		//paint.setFakeBoldText(true);
		canvas.drawText(text, start, end, x + mPaddingLeft,
				y , paint);
	}
}
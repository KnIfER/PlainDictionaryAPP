package com.knziha.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.CMN;

public class ColoredTextSpanX extends ReplacementSpan {
	private final int mColor;
	private final int mPaddingLeft;
	private final int mPaddingRight;
	
	public int type;
	public float thickness;
	
	public ColoredTextSpanX(@ColorInt int mColor_, float thickness_, int type_) {
		super();
		mColor = mColor_;
		thickness = thickness_;
		type=type_;
		mPaddingLeft = mPaddingRight = 0;
	}
	
	
	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end,
					   Paint.FontMetricsInt fm) {
		return (int) paint.measureText(text.subSequence(start, end).toString());
	}
	
	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
					 int bottom, Paint paint) {
		CMN.debug("draw");
		float width = paint.measureText(text.subSequence(start, end).toString());
		
		RectF rect = new RectF(x, bottom-thickness
				//- paint.getFontMetricsInt().top + paint.getFontMetricsInt().ascent
				, x + width + mPaddingLeft + mPaddingRight, bottom);
		paint.setColor(GlobalOptions.isDark?0x88777777: mColor);
		canvas.drawRoundRect(rect, 0, 0, paint);
		
		paint.setColor(GlobalOptions.isDark?Color.WHITE: Color.BLACK);
		//paint.setFakeBoldText(true);
		canvas.drawText(text, start, end, x + mPaddingLeft,
				y , paint);
	}
}
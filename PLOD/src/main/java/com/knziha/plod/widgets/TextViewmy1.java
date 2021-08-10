package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.R;

public class TextViewmy1 extends TextView {
	private final Paint bitmapEraser;
	
	public TextViewmy1(Context context) {
		this(context, null);
	}
	
	public TextViewmy1(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.subtitleTextStyle);
	}
	
	public TextViewmy1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		bitmapEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
		bitmapEraser.setColor(0);
		bitmapEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(0, 0, getWidth(), getBottom(), bitmapEraser);
	}
}

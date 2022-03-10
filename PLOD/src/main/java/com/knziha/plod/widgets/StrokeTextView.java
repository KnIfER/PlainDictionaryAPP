package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

public class StrokeTextView extends TextView {
    private int mStrokeColor = Color.WHITE;
    private float mStrokeWidth = 0;

    public StrokeTextView(Context context) {
        this(context, null);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
		mStrokeWidth = GlobalOptions.density*2f;
        setTextColor(mStrokeColor);
		TextPaint paint = getPaint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(mStrokeWidth);
    }
}
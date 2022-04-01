package com.knziha.plod.widgets;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;

@SuppressLint("ViewConstructor")
public class SimpleHandle extends View {
    Paint p;
	Paint deliPainter;
	private int bgColor;
	float delta;
	private String delimiter;
	
	public SimpleHandle(Context c, int m){
        super(c);
		p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

    @Override
    public void setBackgroundColor(int color) {
    	if(bgColor!=color) {
			bgColor = color;
			super.setBackgroundColor(color);
			p.setColor(Color.parseColor("#ffffff"));
			//p.setColor(Color.parseColor("#FF4081"));
			p.setTextSize(15*getResources().getDisplayMetrics().density);//22
			p.setTypeface(Typeface.DEFAULT);
			p.setStrokeWidth(2);
			deliPainter = new Paint(p);
			deliPainter.setColor(Color.WHITE);
			//deliPainter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			//setDelimiter("|||");
			deliPainter.setTextAlign(Align.CENTER);
		}
    }
    
    void setDelimiter(String newTextDelimiter) {
    	if(!TextUtils.equals(newTextDelimiter, delimiter)) {
	    	delimiter = newTextDelimiter;
			if(newTextDelimiter!=null) {
				Rect textBounds = new Rect();
				deliPainter.getTextBounds(newTextDelimiter, 0, newTextDelimiter.length(), textBounds);
				delta = textBounds.exactCenterY();
			} else {
				delta = 0;
			}
	        invalidate();
    	}
    }

    @Override
    protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(!TextUtils.isEmpty(delimiter)) {
			canvas.rotate(90);
			canvas.drawText(delimiter, getHeight()/2,  -getWidth()/2-delta, deliPainter);
			canvas.rotate(-90);
		}
    }
}

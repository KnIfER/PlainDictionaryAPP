package com.knziha.plod.widgets;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

@SuppressLint("ViewConstructor")
public class SamsungLikeHandle extends View {
    Paint p;
	Paint p2;
    public SamsungLikeHandle(Context c, int m){
        super(c);
		p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);

        p.setColor(Color.parseColor("#ffffff"));
        //p.setColor(Color.parseColor("#FF4081"));
        p.setTextSize(15*getResources().getDisplayMetrics().density);//22
        p.setTypeface(Typeface.DEFAULT);
        p.setStrokeWidth(2);
        p2 = new Paint(p);
		p2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		setLayerType(View.LAYER_TYPE_SOFTWARE, null); 
		setDelimiter("|||");
		p2.setTextAlign(Align.CENTER);
    }
    float delta;
    private String delimiter;
    void setDelimiter(String newShield) {
    	if(!newShield.equals(delimiter)) {
	    	delimiter = newShield;
	        Rect textBounds = new Rect();
	        p2.getTextBounds(newShield, 0, newShield.length(), textBounds);
	        delta = textBounds.exactCenterY();
	        invalidate();
    	}
    }

    @Override
    protected void onDraw(Canvas canvas) {
    		super.onDraw(canvas);
			canvas.rotate(90);
	        canvas.drawText(delimiter, getHeight()/2,  -getWidth()/2-delta, p2);
			canvas.rotate(-90);
    }
}

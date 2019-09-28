package com.knziha.ankislicer.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;


public class FloatingActionButtonmy extends FloatingActionButton{
	Paint p,p2;
	public FloatingActionButtonmy(Context context) {
		super(context);
		init();
	}
	public FloatingActionButtonmy(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public FloatingActionButtonmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	void init(){
		p = new Paint();
        p.setColor(Color.parseColor("#ffffff"));
        //p.setColor(Color.parseColor("#FF4081"));
        p.setTextSize(200);
        p.setStrokeWidth(20);
        p2 = new Paint(p);
		p2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		setLayerType(View.LAYER_TYPE_SOFTWARE, null); 
	}
	 
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		FontMetricsInt fm = p2.getFontMetricsInt();

		int startY = getHeight() / 2 - fm.descent + (fm.descent - fm.ascent) / 2;

		
		canvas.drawText("+", getWidth()/2-p2.measureText("+")/2 ,startY,p2);
	}


	
}
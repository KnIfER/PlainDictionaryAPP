package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

public class CheckedTextViewmy extends CheckedTextView {
	public CheckedTextViewmy(Context context) {
		super(context);
	}
	public CheckedTextViewmy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public CheckedTextViewmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public CheckedTextViewmy(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	private String subText;
	private Paint subTextPainter;
	public void setSubText(String _subText){
		subText = _subText;
		subTextPainter = new Paint();
		subTextPainter.setColor(Color.parseColor("#aaaaaa"));
		subTextPainter.setTextSize(subTextPainter.getTextSize()*2);
		subTextPainter.setAntiAlias(true);
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(subText!=null) {
				Paint.FontMetrics fontMetrics = subTextPainter.getFontMetrics();
		        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
		        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
		        float delta;
		        delta = 0 - top/2 - bottom/2;
		        fontMetrics = getPaint().getFontMetrics();
		        top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
		        bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
		        delta += getLineCount()*getLineHeight()/2 + (bottom - top)/3;
		        
		        canvas.drawText(subText,  getTotalPaddingLeft(),  getMeasuredHeight()/2 + delta, subTextPainter);
		}
	}
	
	
	
	
	
	
	
	
	
	

}

package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.icu.text.BreakIterator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ASDZXC on 2017/11/4.
 */

public class TextViewmy extends androidx.appcompat.widget.AppCompatTextView {
	public int selEnd  ;
    public int selStart;
    public float xmy;
    public float ymy;
    Paint p2,p3;
    public int scrolly1,scrolly2,highlighty1,highlighty2;
    public TextViewmy(Context c) {
        super(c);
        init();
    }
    public TextViewmy(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }
    public TextViewmy(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        init();
    }
    @Override
    public boolean isFocused() {
        return true;
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect){

    }
    BreakIterator boundary;
    private void init(){
        p2 = new Paint();
        p3 = new Paint();
        p2.setColor(Color.parseColor("#ffffff"));
        p2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        p3.setColor(Color.YELLOW);
        p3.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		setLayerType(View.LAYER_TYPE_HARDWARE, null); 
		//boundary = BreakIterator.getWordInstance();
        //boundary.setText(""+getText());
        
    }

    @Override
	public boolean onTouchEvent(MotionEvent ev){
    	boolean ret = super.onTouchEvent(ev);
    	switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				getLayout().getLineForOffset(11);
				int line = getLayout().getLineForVertical((int) ev.getY());
				int offset = getLayout().getOffsetForHorizontal(line, ev.getX());
				int end = boundary.following(offset);
				int start = boundary.previous();
				//CMN.show(""+getText().subSequence(start, end));
				//CMN.show(""+getText().subSequence(offset, offset+1));
				
			break;
    	} 
    	
    	
    	return ret;
    }
    
    
	@Override
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawRect(0,scrolly1+getPaddingTop(),CMN.dm.widthPixels,scrolly2+getPaddingTop(), p2);
        canvas.drawRect(0,highlighty1+getPaddingTop(),getResources().getDisplayMetrics().widthPixels,highlighty2+getPaddingTop(), p3);

	}
	public void doit() {
		highlighty1=0;
		highlighty2=100;
	}
	
	
}

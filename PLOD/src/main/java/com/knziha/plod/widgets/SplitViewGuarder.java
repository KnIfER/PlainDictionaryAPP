package com.knziha.plod.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;


public class SplitViewGuarder extends View implements OnTouchListener{
	/*Oh, dear splitview, please let me guard you! */
	public ArrayList<SplitView> SplitViewsToGuard = new ArrayList<>();
	protected int dragIdx=-1;
	
	
    public SplitViewGuarder(Context context) {
        super(context);
        init();
    }

    public SplitViewGuarder(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SplitViewGuarder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

	Paint p;
	
	private void init() {
		setOnTouchListener(this);
		p = new Paint();
		p.setColor(Color.RED);
		p.setStrokeWidth(100);
	}

	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);
		
		for(int i=0;i<SplitViewsToGuard.size();i++) {
			SplitView svI = SplitViewsToGuard.get(i);
			if(isInEditMode()) {
				if(svI.getOrientation()==LinearLayout.VERTICAL)
				c.drawLine(0,svI.getHandleBottom(),
						getResources().getDisplayMetrics().widthPixels,svI.getHandleBottom(),
						p);
			}
		}
	}
	
	@Override
	public boolean onTouch(View v,MotionEvent ev) {
		if(dragIdx!=-1) {
			SplitViewsToGuard.get(dragIdx).onTouch(this, ev);
			return true;
		}

		
		for(int i=0;i<SplitViewsToGuard.size();i++) {
			SplitView svI = SplitViewsToGuard.get(i);
			float judger = svI.getOrientation()==LinearLayout.VERTICAL?ev.getY():ev.getX();
			float framer = svI.getOrientation()==LinearLayout.VERTICAL?ev.getX():ev.getY();
			if(judger>svI.getHandleTop() && judger<svI.getHandleBottom()) {
				if(framer>svI.getHandleLeft() && framer<svI.getHandleRight()) {
					dragIdx=i;
					svI.onTouch(svI, ev);
					return true;
				}
			}
		}
		
		
		switch(ev.getAction()) {
	    	case MotionEvent.ACTION_UP:
	    		dragIdx=-1;
	    	return false;
		}
		
		return false;
	}

}
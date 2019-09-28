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


public class SplitViewGuarder_ extends View implements OnTouchListener{
	/*Oh, dear splitview, please let me guard you! */
	public ArrayList<SplitView> SplitViewsToGuard = new ArrayList<>();
	protected int dragIdx=-1;
	private float lastX,lastY,OrgX,OrgY;
	
	
    public SplitViewGuarder_(Context context) {
        super(context);
        init();
    }

    public SplitViewGuarder_(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SplitViewGuarder_(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

	public SplitViewGuarder_(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
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
	}
	
	boolean moved=false;
	
	@Override
	public boolean onTouch(View v,MotionEvent ev) {
		lastX=ev.getX();
		lastY=ev.getY();
		switch(ev.getAction()) {
	    	case MotionEvent.ACTION_DOWN:
	    		OrgX=ev.getX();
	    		OrgY=ev.getY();
	    	break;
	    	case MotionEvent.ACTION_UP:
	    		dragIdx=-1;
	    		moved=false;
	    	return false;
	    	case MotionEvent.ACTION_MOVE:
	    		if(!moved) {
		    		if(Math.abs(lastX-OrgX)<10)
		    			return false;
		    		else
		    			moved=true;
	    		}
	    	break;
		}

		if(moved) {
			if(dragIdx!=-1) {
				SplitViewsToGuard.get(dragIdx).onTouch(this, ev);
				return true;
			}
	
			for(int i=0;i<SplitViewsToGuard.size();i++) {
				SplitView svI = SplitViewsToGuard.get(i);
				float judger = svI.getOrientation()==LinearLayout.VERTICAL?OrgY:OrgX;
				if(judger>svI.getHandleTop() && judger<svI.getHandleBottom()) {
					//CMN.show(judger+"=="+svI.getHandleTop()+"=="+svI.getHandleBottom());
					dragIdx=i;
					svI.onTouch(svI, ev);
					return true;
				}
			}
		}
		
		return false;
	}

}
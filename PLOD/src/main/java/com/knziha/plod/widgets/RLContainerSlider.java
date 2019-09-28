package com.knziha.plod.widgets;
import com.knziha.plod.PlainDict.MainActivityUIBase;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class RLContainerSlider extends FrameLayout{
	public RLContainerSlider(Context context) {
		super(context);
		init();
	}
	public RLContainerSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public RLContainerSlider(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		init();
	}
	
	public RLContainerSlider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	
	private void init() {
		 detector = new GestureDetector(getContext(), gl);
	}


	public IMPageSlider IMSlider;
	public ScrollViewmy SCViewToMute;
	
	boolean decided=false;
	boolean decidedDir;
	
	boolean bCanSlide=true;
	private float lastX,lastY,OrgX,OrgY;
	boolean dragged;
	float width,height;
	public boolean TurnPageEnabled=false;
	public boolean LeftEndReached=false;
	public boolean RightEndReached=false;
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){
			MainActivityUIBase.layoutScrollDisabled=false;
			if(!TurnPageEnabled && !LeftEndReached && !RightEndReached)
				return false;
			if(dragged){
				if(!TurnPageEnabled) {
					if(LeftEndReached&&IMSlider.getTranslationX()<0 || RightEndReached&&IMSlider.getTranslationX()>0) {
						//LeftEndReached=RightEndReached=false;
						dragged=false;
						IMSlider.decided=false;
		        		IMSlider.RePosition();
		        		return true;
					}
				}
			}else
				return true;
			if(!IMSlider.decided) detector.onTouchEvent(ev);
	        if(isOnFlingDected) {
	        	dragged=
	        	isOnFlingDected=false;
	        	IMSlider.decided=false;
	        	return true;
	        }
	        if(IMSlider!=null)
	        switch (ev.getAction()) {
	        case MotionEvent.ACTION_MOVE:
	            int dx = (int) (ev.getRawX() - lastX);
	            int dy = (int) (ev.getRawY() - lastY);


	            if(!dragged) {
		            int dxdx = (int) (ev.getRawX() - OrgX);
		            int dydy = (int) (ev.getRawY() - OrgY);
		            if(Math.abs(dxdx)>100 && (Math.abs(dxdx/(dydy==0?1:dydy))>1.2)){
		            	dragged=true;
		            	//CMN.show("start dragging...");
		            	Log.e("~~", "start dragging..."); 
		            }
	            }
	            if(dragged){
	            	IMSlider.startdrag(ev);
	            	IMSlider.handleDrag(dx,dy);
	                if(IMSlider.inf!=null) 
	                	IMSlider.inf.onMoving(IMSlider.getTranslationX(),IMSlider);
	            }
	            lastX = ev.getRawX();
	            lastY = ev.getRawY();
            break;
	        case MotionEvent.ACTION_UP:
	        	//CMN.show("RLACTION_UP");
	        	//if(bPerformMove)
	        	if(dragged) {
		        	dragged=false;
	        		IMSlider.RePosition();
	        	}
            break;
	        }
			
			
			return dragged;
	}
	
	boolean isOnFlingDected;
	GestureDetector detector;
	GestureDetector.SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
	    public boolean onFling(MotionEvent ev, MotionEvent e2, final float velocityX,final float velocityY) {
	    	if(System.currentTimeMillis()-lastDownTime<=200) {//事件老死
	            if(!TurnPageEnabled) {
	        		//if(LeftEndReached) {
	        		//	if(ev.getX()>=getWidth()*1.f/3)
	        		//		return false;
	        		//}else if(RightEndReached) {
	        		//	if(ev.getX()<=getWidth()*2.f/3)
	        		//		return false;
	        		//}else 
	            	return false;
	        	}
	            
				if(Math.abs(velocityX)>7 && Math.abs(velocityX/(velocityY==0?0.000001:velocityY))>1.899) {
			    	//CMN.show("onFling");
		            isOnFlingDected=true;
					//IMSlider.startdrag(ev);
		            if(IMSlider.inf!=null) 
		            	IMSlider.inf.onPreparePage(IMSlider);
		            IMSlider.setAlpha(1.f);
					if(velocityX<0) {
						IMSlider.decidedDir=true;
						IMSlider.TargetX=-IMSlider.getWidth();
						IMSlider.TargetY=IMSlider.getTranslationY();
					}else {
						IMSlider.decidedDir=false;
						IMSlider.TargetX=IMSlider.getWidth();
						IMSlider.TargetY=IMSlider.getTranslationY();
					}
					IMSlider.decided=true;
					IMSlider.RePosition();
				}
	    	}
    		return false;
	}
	};
	
	long lastDownTime;
	public boolean enabled;
	private float DragStart;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		MainActivityUIBase.layoutScrollDisabled=false;
		if(!TurnPageEnabled && !LeftEndReached && !RightEndReached) return false;
		//CMN.Log("onInterceptTouchEvent"+System.currentTimeMillis()+" TurnPageEnabled"+TurnPageEnabled+" LeftEndReached"+LeftEndReached+" RightEndReached"+RightEndReached);
		
    	if(ev.getPointerCount()==1)//单指相应
    		detector.onTouchEvent(ev);
        if(isOnFlingDected) {
        	dragged=
        	isOnFlingDected=false;
        	IMSlider.decided=false;
        	return true;
        }
		boolean ret = super.onInterceptTouchEvent(ev);
		if(!(bCanSlide)) {
			return ret;
		}
        if(IMSlider!=null)
        switch (ev.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	        	lastDownTime=System.currentTimeMillis();
	        	OrgX = lastX = ev.getRawX();
	        	OrgY = lastY = ev.getRawY();
	        	if(OrgX>getWidth()/3*2) {
	        		//dragged=true;
					//IMSlider.setAlpha(1.f);
	        	}else {
	        		//dragged=false;
	        	}
	        	
	        	
	        	//layout(0, 0, (int)(width=getWidth()), (int) (height=getHeight()));
	            break;
	            
	        case MotionEvent.ACTION_MOVE:
	            int dx = (int) (ev.getRawX() - lastX);
	            int dy = (int) (ev.getRawY() - lastY);
	            if(!dragged) {
		            int dxdx = (int) (ev.getRawX() - OrgX);
		            int dydy = (int) (ev.getRawY() - OrgY);
		            if(ev.getPointerCount()==1)
		            if((TurnPageEnabled?true:(LeftEndReached&&dxdx>0 || RightEndReached&&dxdx<0)) &&Math.abs(dxdx)>100 && (Math.abs(dxdx/(dydy==0?0.000001:dydy))>1.988)){//3.3
		            	dragged=true;
		            }
	            }
	            if(dragged){
	            	IMSlider.startdrag(ev);
	            	DragStart = ev.getRawX();
	            }
	            lastX = ev.getRawX();
	            lastY = ev.getRawY();
	            break;
	        case MotionEvent.ACTION_UP:
	        	if(dragged) {
		        	dragged=false;
	        		IMSlider.RePosition();
	        	}
        	break;
        }
        
		return dragged;
	}
	



	
}
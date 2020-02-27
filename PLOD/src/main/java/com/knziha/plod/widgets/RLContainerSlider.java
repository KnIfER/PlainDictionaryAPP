package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.knziha.plod.PlainDict.MainActivityUIBase;


public class RLContainerSlider extends FrameLayout{
	private float density;

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

	private void init() {
		detector = new GestureDetector(getContext(), gl);
		density = getContext().getResources().getDisplayMetrics().density;
	}

	public IMPageSlider IMSlider;

	boolean bCanSlide=true;
	private float lastX,lastY,OrgX,OrgY;
	boolean dragged;
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
			dragged=isOnFlingDected=false;
			IMSlider.decided=false;
			return true;
		}
		if(IMSlider!=null)
			switch (ev.getAction()) {
				case MotionEvent.ACTION_MOVE:{
					int dx = (int) (ev.getRawX() - lastX);
					int dy = (int) (ev.getRawY() - lastY);


					if(!dragged) {
						int dxdx = (int) (ev.getRawX() - OrgX);
						int dydy = (int) (ev.getRawY() - OrgY);
						IMSlider.OrgTX = IMSlider.getTranslationX();
						if(Math.abs(dxdx)>100 && (Math.abs(dxdx/(dydy==0?1:dydy))>1.2)){
							dragged=true;
							//CMN.Log("start dragging...");
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
				} break;
				case MotionEvent.ACTION_UP:{
					if(dragged) {
						dragged=false;
						IMSlider.RePosition();
					}
				} break;
			}
		return dragged;
	}

	boolean isOnFlingDected;
	GestureDetector detector;
	GestureDetector.SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
		public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX,final float velocityY) {
			//if(System.currentTimeMillis()-lastDownTime<=200) //事件老死
			{
				if(!TurnPageEnabled) { //todo slide on zoomed page
					return false;
				}

				float vx = velocityX / 8;

				// Y轴幅度
				if (Math.abs(e2.getRawY() - e1.getRawY()) > 35*density) {
					return true;
				}

				//x轴速度
				if (Math.abs(vx) < 3.25*50*density) {
					return true;
				}
				//CMN.Log("SimpleOnGestureListener", velocityX, vx, 3*50*density);
				if(Math.abs(velocityX/(velocityY==0?0.000001:velocityY))>1.699) {
					//CMN.show("onFling");
					isOnFlingDected=true;
					//IMSlider.startdrag(ev);
					//xxx
					/////if(IMSlider.inf!=null)
					/////	IMSlider.inf.onPreparePage(IMSlider);
					IMSlider.startdrag(e2);
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
					dragged=false;
					IMSlider.decided=true;
					IMSlider.RePosition();
				}
			}
			return false;
		}
	};

	long lastDownTime;
	public boolean enabled;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		MainActivityUIBase.layoutScrollDisabled=false;
		if(!TurnPageEnabled && !LeftEndReached && !RightEndReached) return false;
		//CMN.Log("onInterceptTouchEvent"+System.currentTimeMillis()+" TurnPageEnabled"+TurnPageEnabled+" LeftEndReached"+LeftEndReached+" RightEndReached"+RightEndReached);

		if(ev.getPointerCount()==1) detector.onTouchEvent(ev);

		if(isOnFlingDected) {
			dragged=isOnFlingDected=false;
			IMSlider.decided=false;
			return true;
		}
		boolean ret = super.onInterceptTouchEvent(ev);

		if(!bCanSlide) {
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
						//IMSlider.startdrag(ev);
						//DragStart = ev.getRawX();
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
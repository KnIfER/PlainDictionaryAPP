package com.knziha.plod.widgets;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.knziha.plod.PlainDict.CMN;


public class IMPageSlider extends ImageView{
	public IMPageSlider(Context context) {
		super(context);setAlpha(0.f);
	}
	public IMPageSlider(Context context, AttributeSet attrs) {
		super(context, attrs);setAlpha(0.f);
	}
	public IMPageSlider(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);setAlpha(0.f);
	}

	boolean decided=false;
	boolean decidedDir;
	public interface PageSliderInf{
		void onPreparePage(IMPageSlider v);
		void onDecided(boolean Dir,IMPageSlider v);
		void onPageTurn(int Dir,IMPageSlider v);
		void onHesitate(IMPageSlider v);
		void onMoving(float val,IMPageSlider v);
	}public PageSliderInf inf;
	public void setPageSliderInf(PageSliderInf inf_) 
	{
		inf=inf_;
	}
	int leftAcc;
	float lastX,lastY,OrgTX,OrgX,OrgY;
	private boolean dragged=false;
	float width,height;
	
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return dragged;
    }
    
    private float animator = 0.0f;
	private  float animatorD = 0.15f;
	private  boolean triggered=false;
	protected  float TargetX,TargetY,srcX,srcY;
	private long timeStamp;
	private final Handler mHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 3344:
				if((long)msg.obj<timeStamp)
					break;
				
				animator+=animatorD;


				if(animator<=1) {
					if(animator>=.99998) {
						//animator=1;
					}
					if(decided)
					setAlpha(1-animator);
					setTranslationX(animator*TargetX+(1-animator)*srcX);
					setTranslationY(animator*TargetY+(1-animator)*srcY);
					//setAlpha(1-animator);
					Message msgmsg = Message.obtain();
					msgmsg.obj=msg.obj;
					msgmsg.what=3344;
					mHandle.sendMessage(msgmsg);
				}else{
					setAlpha(.0f);
					setTranslationX(0);
					setTranslationY(0);
					
				}
			break;
			default: break;
			}
	}};
	
	void RePosition() {
		//CMN.show("RePosition called");
		dragged=false;
		leftAcc=0;
		timeStamp = System.currentTimeMillis();
		Message msg = new Message();
		msg.what=3344;
		msg.obj = timeStamp+1;
		mHandle.removeMessages(3344);
		srcX = getTranslationX();
		srcY = getTranslationY();
		triggered=false;
		animator = 0.f;
		mHandle.sendMessage(msg);
		if(inf!=null) 
			inf.onPageTurn(decided?(decidedDir?1:0):2,this);
	}
	
	public void startdrag(MotionEvent ev) {
		//CMN.show("startdrag called");
		if(!dragged) {
			leftAcc=0;
			OrgTX = getTranslationX();
			dragged=true;
	    	OrgX = lastX = ev.getRawX();
	    	OrgY = lastY = ev.getRawY();
			TargetX=0;
			if(inf!=null) 
				inf.onPreparePage(this);
		}
	}
	
	public void handleDrag(int dx, int dy) {
		//if(true) return;
    	setAlpha(1.f);
    	timeStamp = System.currentTimeMillis();
    	
        int left = (int) (getTranslationX() + dx);
        int top = (int) (getTranslationY() + dy);
        //layout(left, top, right, bottom);
        setTranslationX(left);
		leftAcc+=dx;
        setTranslationY(top);
        DisplayMetrics dm = getResources().getDisplayMetrics();
		int w = dm.widthPixels;//getWidth();
        if(leftAcc<-(2.0f*w/12)) {
        	TargetX=-getWidth();
        	TargetY = getTranslationY();
			if(inf!=null) 
				inf.onDecided(true,this);
			decided=true;
			decidedDir=true;
        }else if(leftAcc>(2.0f*w/12)){
        	TargetX=getWidth();
        	TargetY = getTranslationY();
			if(inf!=null) 
				inf.onDecided(true,this);
			decided=true;
			decidedDir=false;
        }else{
        	if(decided) {
    			if(inf!=null) 
    				inf.onHesitate(this);
        	}
        	TargetX=0;
        	TargetY = 0;
        	decided=false;
        }
	}



	
}
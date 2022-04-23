package com.knziha.plod.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.animation.AnimationUtils;
import com.knziha.plod.plaindict.WebViewListHandler;


public class PageSlide extends TextView {
	public WebViewListHandler weblist;
	public int MainBackground;
	public Handler hdl;
	/** The content view to drag */
	private ViewGroup dragView;
	public PageSlide(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	boolean decided=false;
	boolean decidedDir;
	public interface Pager {
		void slidePage(int Dir, PageSlide v);
		void onMoving(float val, PageSlide v);
	}
	public Pager listener;
	public final void setPager(Pager listener)
	{
		this.listener = listener;
	}
	int leftAcc;
	float lastX,lastY,OrgTX,OrgX,OrgY;
	private boolean dragged=false;
	
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return dragged;
    }
    
    private float animator = 0.0f;
	private  float animatorD = 0.12f;
	protected  float TargetX,srcX;
	private int dragTm;
	
	public void handleMsg(Message msg) {
		if(msg.arg1<dragTm)
			return;
		
		animator+=animatorD;
		
		if(animator<=1) {
			if(animator>=.99998) {
				//animator=1;
			}
			//if(decided)
				setTranslationX(animator*TargetX+(1-animator)*srcX);
			hdl.obtainMessage(3344,msg.arg1,0,msg.obj).sendToTarget();
		} else {
			RLContainerSlider slide = weblist.contentUIData.PageSlider;
			if (!decided || slide.dragged || slide.aborted) {
				dragView.setAlpha(1);
				setVisibility(GONE);
			} else {
				ViewGroup dv = weblist.getDragView();
				final ViewGroup fdv;
				if (dv != dragView) {
					dv.setVisibility(VISIBLE);
					dragView.setVisibility(GONE);
					fdv = dragView;
					dragView = dv;
				} else {
					fdv = null;
				}
				dv.setAlpha(0.15f);
				dv.animate()
					.alpha(1)
					.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							if (getTranslationX()==0 && !dragged) {
								setVisibility(GONE);
							}
							if (fdv!=null) {
								fdv.setVisibility(GONE);
								fdv.setTranslationX(0);
							}
						}
					})
					.setDuration(115);
			}
			setTranslationX(0);
		}
	}
	
	void RePosition() {
		if (hdl !=null) {
			dragged=false;
			leftAcc=0;
			dragTm = (int) SystemClock.currentThreadTimeMillis();
			srcX = getTranslationX();
			animator = 0.f;
			hdl.removeMessages(3344);
			if(listener !=null) {
				listener.slidePage(decided?(decidedDir?1:0):2,this);
				ViewGroup dv = weblist.getDragView();
				if (dv != dragView) {
					dragView.setVisibility(View.VISIBLE);
					dv.setVisibility(View.INVISIBLE);
				}
			}
			hdl.obtainMessage(3344,dragTm+1,0,this).sendToTarget();
		}
	}
	
	public void startDrag(MotionEvent ev) {
		if(!dragged && hdl !=null) {
			dragView = weblist.getDragView();
			OrgX = lastX = ev.getRawX();
			OrgY = lastY = ev.getRawY();
			ViewGroup svp = (ViewGroup) getParent();
			MotionEvent evt = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL,
					lastX, lastY, 0);
			evt.setSource(100);
			svp.dispatchTouchEvent(evt);
			leftAcc=0;
			OrgTX = getTranslationX();
			dragged=true;
			TargetX=0;
			if(listener !=null) {
				listener.onMoving(Integer.MAX_VALUE, this);
			}
			setVisibility(VISIBLE);
		}
	}
	
	@Override
	public void setTranslationX(float translationX) {
		if (true) {
			dragView.setTranslationX(translationX);
		} else {
			super.setTranslationX(translationX);
		}
	}
	
	@Override
	public float getTranslationX() {
		if (true) {
			return dragView.getTranslationX();
		} else {
			return super.getTranslationX();
		}
	}
	
	public void handleDrag(float dx, float dy) {
		dragTm = (int) SystemClock.currentThreadTimeMillis();
    	
        int left = (int) (getTranslationX() + dx);
        setTranslationX(left);
		leftAcc+=dx;
        DisplayMetrics dm = getResources().getDisplayMetrics();
		int w = dm.widthPixels;//getWidth();
        if(leftAcc<-(2.0f*w/12)) {
        	TargetX=-getWidth();
			decided=true;
			decidedDir=true;
        } else if(leftAcc>(2.0f*w/12)) {
        	TargetX=getWidth();
			decided=true;
			decidedDir=false;
        } else {
        	TargetX=0;
        	decided=false;
        }
	}
	
}
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

import androidx.appcompat.app.GlobalOptions;

import com.google.android.material.animation.AnimationUtils;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
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
	public int prevd;
	public int decided;
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
	boolean dragged=false;
	
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return dragged;
    }
    
    private float animator = 0.0f;
	private  float animatorD = 0.12f;
	protected  float TargetX,srcX;
	private int dragTm;
	
	public void handleMsg(Message msg) {
		if(msg.arg1<dragTm) {
			CMN.debug("not handling...");
			return;
		}
		
		animator+=animatorD;
		
		if(animator<=1) {
			if(animator>=.99998) {
				//animator=1;
			}
			//if(decided)
				setTranslationX(animator*TargetX+(1-animator)*srcX);
			hdl.obtainMessage(3344,msg.arg1,0,msg.obj).sendToTarget();
		} else {
			RLContainerSlider slide = weblist.pageSlider;
			if (prevd==0 || slide.dragged==1 || slide.aborted==1) {
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
				if(!GlobalOptions.isDark || PDICMainAppOptions.nighAvoidTurnPicFlicker()) dv.setAlpha(0.15f);
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
		if (hdl!=null) {
			dragged=false;
			leftAcc=0;
			dragTm = (int) SystemClock.currentThreadTimeMillis();
			hdl.removeMessages(3344);
			animator = 0.f;
			if(listener !=null) {
				listener.slidePage(decided,this);
				ViewGroup dv = weblist.getDragView();
				if (dv != dragView) {
					dragView.setVisibility(View.VISIBLE);
					dv.setVisibility(View.INVISIBLE);
				}
			}
			srcX = getTranslationX();
			TargetX = decided>0?getWidth():decided<0?-getWidth():0;
			if(GlobalOptions.isDark && PDICMainAppOptions.nighAvoidTurnPicFlicker()) TargetX = 0;
			prevd = decided;
			decided = 0;
			hdl.obtainMessage(3344,dragTm+1,0,this).sendToTarget();
		}
	}
	
	public void startDrag(MotionEvent ev) {
		if(!dragged && hdl!=null) {
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
		if (dragView!=null) {
			dragView.setTranslationX(translationX);
		} else {
			super.setTranslationX(translationX);
		}
	}
	
	@Override
	public float getTranslationX() {
		if (dragView!=null) {
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
		float theta = 2.5f*((View)getParent()).getWidth()/12;
        if(leftAcc<-theta) {
			decided=-1;
        } else if(leftAcc>theta) {
			decided=1;
        } else {
			decided=0;
        }
	}
	
}
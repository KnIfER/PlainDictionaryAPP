package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.CMN;

public class LinearSplitView extends LinearLayout {
	public View handle;
	public Runnable resizeAbility;
	
	public LinearSplitView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		handle = new View(context);
		handle.setBackgroundColor(Color.RED);
		handle.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int actionMasked = event.getActionMasked();
				if(actionMasked==MotionEvent.ACTION_DOWN) {
					lastX = orgX = event.getRawX();
					orgW = getChildAt(0).getWidth();
				}
				if(actionMasked==MotionEvent.ACTION_MOVE) {
					float x = event.getRawX();
					float d = x - lastX;
					lastX = x;
					
					View mWin = getChildAt(0);
					float mWeight = 0;
					
					float[] weights = new float[getChildCount()];
					float total = 0;
					int width = getWidth();
					for (int i = 0; i < getChildCount(); i++) {
						float w = ((LayoutParams)getChildAt(i).getLayoutParams()).weight;
						weights[i]=w;
						if(getChildAt(i)!=mWin)total += w;
						else mWeight = w;
						if(w==0) {
							w-=getChildAt(i).getWidth();
						}
					}
					
					float newW = orgW + (lastX-orgX);
					
					CMN.debug("onTouch::", mWin.getWidth(), newW);
					
					if(width>0 && newW!=mWin.getWidth()) {
						float newPercent = newW / width;
						CMN.debug("newPercent::", newPercent);
						float minPercent = 0.2f;
						if(newPercent>1-minPercent) {
							newPercent = 1-minPercent;
						} else if(newPercent<minPercent) {
							newPercent = minPercent;
						}
						float newWeight = total / (1 - newPercent) * newPercent;
						
						if(newWeight>=0) {
							((LayoutParams)mWin.getLayoutParams()).width = 0;
							((LayoutParams)mWin.getLayoutParams()).weight = newWeight;
							if(resizeAbility==null)resizeAbility=mWin::requestLayout;
							//mWin.removeCallbacks(resizeAbility);
							mWin.postOnAnimation(resizeAbility);
							//mWin.postDelayed(resizeAbility, 100);
						}
						
						
						//requestLayout();
					}
				}
				
				return true;
			}
		});
	}
	
	float orgX, orgY, lastX, lastY;
	int orgW;
	
	public void installHandle(ViewGroup vg) {
		ViewUtils.addViewToParent(handle, vg);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		handle.setTranslationX(getChildAt(0).getRight() - handle.getWidth()/2);
		handle.setTranslationY(getTop());
	}
}

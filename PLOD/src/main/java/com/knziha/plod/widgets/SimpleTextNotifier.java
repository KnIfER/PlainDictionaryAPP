package com.knziha.plod.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.knziha.plod.plaindict.CMN;

public class SimpleTextNotifier extends TextView{
	public int msg;
	int offset;
	float mOffsetScale = 1;
	boolean snacking;
	boolean visible;
	private int mBottomMargin;
	private int mDuration;
	private int mDwellation;
	private FrameLayout mCropFrame;
	
	AnimatorListenerAdapter currListener;
	
	// [Animation]show -> dwelling -> [Animation]hide -> remove
	
	AnimatorListenerAdapter toDwellAndHideListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			if(currListener!=null) {
				currListener.onAnimationEnd(animation);
				return;
			}
			removeCallbacks(dwellAbility);
			if(visible) {
				snacking = false;
				// dwelling……
				postDelayed(dwellAbility, mDwellation);
			}
		}
	};
	
	AnimatorListenerAdapter toRemoveListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			removeCallbacks(dwellAbility);
			CMN.Log("snack stopped.");
			ViewUtils.removeView(getSnackView());
			visible = false;
			if(msg!=0) msg = 0;
		}
	};
	
	Runnable dwellAbility = () -> {
		if(!snacking && visible) {
			currListener = toRemoveListener;
			// hiding……
			animate()
				.setListener(toRemoveListener)
				.translationY(getHeight());
		}
	};
	
	AnimatorListenerAdapter fadeListener = new AnimatorListenerAdapter() {
		@Override public void onAnimationEnd(Animator animation) {
			if(!visible) {
				ViewUtils.removeView(getSnackView());
				if(msg!=0) msg = 0;
			}
		}
	};
	
	Runnable postShowAbility = this::show;
	
	public SimpleTextNotifier(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@SuppressLint("MissingSuperCall")
	@Override
	public void draw(Canvas canvas) {
		//canvas.translate(0, offset);
		super.draw(canvas);
	}
	
	
	public void postShow() {
		removeCallbacks(postShowAbility);
		post(postShowAbility);
	}
	
	public void show() {
		currListener=null;
		removeCallbacks(dwellAbility);
		clearAnimation();
		int height = getHeight();
		if(height>0){
			visible=true;
			if(!snacking || offset>height) {
				offset = (int) (height*mOffsetScale);
			} else {
				offset=Math.max(height/3, (int)(offset*mOffsetScale));
			}
			if(mOffsetScale!=1) mOffsetScale = 1;
			setTranslationY(offset);
			//CMN.Log("setTranslationY::", offset);
			setVisibility(View.VISIBLE);
			snacking = visible = true;
			ViewPropertyAnimator animation = animate()
					.setListener(toDwellAndHideListener)
					.translationY(0)
					.alpha(1)
					;
			mDwellation = (int) (mDuration-animation.getDuration()*1.35);
			if(mDwellation<0) mDwellation=800;
		}
	}
	
	public void fadeOut() {
		if(visible) {
			removeCallbacks(dwellAbility);
			removeCallbacks(postShowAbility);
			visible = false;
			snacking = false;
			if(msg!=0) msg = 0;
			currListener=fadeListener;
			animate()
				.setListener(fadeListener)
				.alpha(0)
			;
		}
	}
	
	public void setDuration(int duration) {
		mDuration = duration;
	}
	
	public void setBottomMargin(int bottom) {
		if(mBottomMargin != bottom) {
			mBottomMargin = bottom;
		}
	}
	
	public int getBottomMargin() {
		return mBottomMargin;
	}
	
	public View getSnackView() {
		if(mBottomMargin>0) {
			if(mCropFrame==null) {
				mCropFrame = new FrameLayout(getContext());
				//mCropFrame.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
			}
			ViewUtils.addViewToParent(this, mCropFrame);
			ViewGroup.LayoutParams lp = getLayoutParams();
			((FrameLayout.LayoutParams)lp).gravity= Gravity.BOTTOM;
			return mCropFrame;
		}
		return this;
	}
	
	public boolean isSnacking() {
		return snacking;
	}
	
	public void setNextOffsetScale(float scale) {
		mOffsetScale = scale;
	}
	
	public boolean isVisible() {
		return visible;
	}
}

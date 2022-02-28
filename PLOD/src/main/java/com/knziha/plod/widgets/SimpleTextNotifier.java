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
	public int offset;
	boolean snacking;
	private int mBottomMargin;
	private int mDuration;
	private int mDwellation;
	private FrameLayout mCropFrame;
	
	AnimatorListenerAdapter toHideListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			removeCallbacks(animateHideAbility);
			if(snacking) {
				snacking = false;
				postDelayed(animateHideAbility, mDwellation);
			}
		}
	};
	
	AnimatorListenerAdapter toRemoveListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			removeCallbacks(animateHideAbility);
			if(!snacking) {
				CMN.Log("snack stopped.");
				ViewUtils.removeView(getSnackView());
			}
		}
	};
	
	AnimatorListenerAdapter fadeListener = new AnimatorListenerAdapter() {
		@Override public void onAnimationEnd(Animator animation) {
			animateHideAbility.run();
		}
	};
	
	Runnable animateHideAbility = () -> {
		if(!snacking) {
			animate().setListener(toRemoveListener).translationY(getHeight());
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
		removeCallbacks(postShowAbility);
		int height = getHeight();
		if(height>0){
			if(!snacking || offset>height)
				offset=height;
			else
				offset=Math.max(height/3, offset);
			setTranslationY(offset);
			setVisibility(View.VISIBLE);
			snacking = true;
			removeCallbacks(animateHideAbility);
			ViewPropertyAnimator animation = animate()
					.setListener(toHideListener)
					.translationY(0);
			mDwellation = (int) (mDuration-animation.getDuration()*1.35);
			if(mDwellation<0) mDwellation=800;
		}
		//ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(topsnack, "translationY", -height, 0);
		//fadeInContents.start();
	}
	
	public void fadeOut() {
		if(snacking) {
			snacking = false;
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
}

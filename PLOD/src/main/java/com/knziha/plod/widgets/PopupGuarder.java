package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;

public class PopupGuarder extends View {
	private final float padding;
	public View popupToGuard;
	public PopupGuarder(Context context) {
		super(context);
		padding = 13*context.getResources().getDisplayMetrics().density;
	}

	GestureDetector gesture=new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return (!PDICMainAppOptions.getClickSearchDismissDelay()) && handle(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			return (PDICMainAppOptions.getClickSearchDismissDelay()) && handle(e);
		}

		private boolean handle(MotionEvent e) {
			if(popupToGuard!=null){
				if(popupToGuard.getParent() instanceof ViewGroup)
					((ViewGroup)popupToGuard.getParent()).removeView(popupToGuard);
				popupToGuard=null;
				setVisibility(View.GONE);
				return true;
			}
			return false;
		}
	});

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(popupToGuard!=null && popupToGuard.getParent()!=null){
			float y=event.getY();
			y-=popupToGuard.getTranslationY()-(PDICMainAppOptions.isFullScreen()? 0:CMN.getStatusBarHeight(getContext()));
			if(y>=popupToGuard.getBottom() || y<=popupToGuard.getTop()){//框外
				if(y>=popupToGuard.getTop()+popupToGuard.getHeight()+padding || y<=popupToGuard.getTop()-padding/2){//框外之外

					if(PDICMainAppOptions.getClickSearchPin())
						return false; // 放行
					else
						gesture.onTouchEvent(event);//点击消失
				}
				return true;//拦截
			}
		}
		return false;//放行
	}
}

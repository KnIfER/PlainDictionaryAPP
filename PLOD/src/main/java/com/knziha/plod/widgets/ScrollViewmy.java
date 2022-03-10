package com.knziha.plod.widgets;

import com.knziha.plod.dictionarymanager.files.BooleanSingleton;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import androidx.core.view.ViewCompat;

public class ScrollViewmy extends ScrollView {// for mute it's scroll
	public DragScrollBar scrollbar2guard;
    public static OnTouchListener dummyOntouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}};
	private OnScrollChangedListener scrollViewListener = null;
    public boolean bScrollEnabled=true;
	public boolean mIsFling;
	public int oldY;
	public BooleanSingleton touchFlag=new BooleanSingleton(true);
    
    public ScrollViewmy(Context context) {
        super(context);
    }

    public ScrollViewmy(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollViewmy(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(OnScrollChangedListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }
	
	public OnScrollChangedListener getScrollViewListener() {
		return scrollViewListener;
	}
	
	@Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChange(this, x, y, oldx, oldy);
        }
    }
	
	@Override
	public void fling(int velocityY) {
		super.fling(velocityY);
		mIsFling = true;
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
    	if(touchFlag!=null) touchFlag.first=true;
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:{

			} break;
			case MotionEvent.ACTION_UP:{
				checkBar();
			} break;
		}
		return super.onTouchEvent(ev);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if(touchFlag!=null) touchFlag.first=true;
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:{
				//CMN.Log("!!!ACTION_DOWN");
				if(this instanceof AdvancedNestScrollView){
					AdvancedNestScrollView sv = ((AdvancedNestScrollView) this);
					sv.mLastMotionY = (int) ev.getY();
					sv.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
				}
				if(scrollbar2guard!=null && !scrollbar2guard.isHidden()){
					scrollbar2guard.isWebHeld=true;
					scrollbar2guard.cancelFadeOut();
				}
				if(mIsFling) {
					mIsFling = false;
				}
			} break;
			case MotionEvent.ACTION_UP:{
				checkBar();
			} break;
		}
    	if(bScrollEnabled) {
    		return super.onInterceptTouchEvent(ev);
    	}else {
    		return false;
    	}
    }

	private void checkBar() {
		if(scrollbar2guard!=null && !scrollbar2guard.isHidden()){
			scrollbar2guard.isWebHeld=false;
			scrollbar2guard.fadeOut();
		}
	}

	/** Reject idiot's behaviour. */
	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		return 0;
	}

	public void scrollTo(int bottom) {
	}
}
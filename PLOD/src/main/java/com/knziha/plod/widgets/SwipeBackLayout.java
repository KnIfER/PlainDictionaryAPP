package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.GlobalOptions;
import androidx.customview.widget.ViewDragHelper;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

public class SwipeBackLayout extends FrameLayout {
    private ViewDragHelper mViewDragHelper;
    private View mContentView;
    private int mContentWidth;
    private CallBack mCallBack;//自定义内部的回调函数，下面写
    private Drawable mShadowLeft;
    private static final int FULL_ALPHA = 255;
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private float mScrimOpacity;//滑动剩余边界的百分比，设置透明帷幕Alpha百分比用
    private float mScrollPercent;//滑动宽度的百分比
    private Rect mTmpRect = new Rect();
	private int draggedSt;
	private boolean dragged;
	
	public SwipeBackLayout(Context context) {
        this(context, null);
    }
    
    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
		mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
			@Override
			public void onEdgeDragStarted(int edgeFlags, int pointerId) {
				draggedSt = mContentView.getLeft();
				mViewDragHelper.captureChildView(mContentView, pointerId);
			}
			
			@Override
			public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
				//CMN.Log("onViewPositionChanged::", left);
				mScrollPercent = Math.abs((float) left / Math.max(mContentWidth, 1 ));
				if (left == mContentWidth) {
					if(mCallBack!=null) {
						mCallBack.onFinish();
					}
				}
				if (!dragged && Math.abs(draggedSt-left)>GlobalOptions.density*2.5) {
					dragged = true;
				}
				postInvalidateOnAnimation();
			}
			
			@Override
			public boolean tryCaptureView(View child, int pointerId) {
				//CMN.Log("tryCaptureView::", child);
				return true;
			}
			
			@Override
			public int clampViewPositionHorizontal(View child, int left, int dx) {
				// 拖动限制（大于左边界）
				return Math.max(0, left);
			}
		
			@Override
			public void onViewReleased(View releasedChild, float xvel, float yvel) {
				// 拖动距离大于屏幕的一半右移，拖动距离小于屏幕的一半左移
				int left = releasedChild.getLeft();
				if (left > getWidth() / 2) {
					mViewDragHelper.settleCapturedViewAt(mContentWidth, 0);
				} else {
					mViewDragHelper.settleCapturedViewAt(0, 0);
				}
				invalidate();
			}
		});
		mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
		mShadowLeft = getResources().getDrawable(R.drawable.shadow_left);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mScrimOpacity = 1 - mScrollPercent;
        if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = getChildAt(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mContentWidth = mContentView.getWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//		boolean ret = mViewDragHelper.shouldInterceptTouchEvent(ev);
////		if(!ret)mViewDragHelper.processTouchEvent(ev);
//        return super.onInterceptTouchEvent(ev);
////		return mViewDragHelper.shouldInterceptTouchEvent(ev);
		return dragged || super.onInterceptTouchEvent(ev);
    }
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
    	if (ev.getActionMasked()==MotionEvent.ACTION_DOWN) {
			dragged = false;
		}
		mViewDragHelper.processTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
	
	//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        mViewDragHelper.processTouchEvent(event);
//        invalidate();
//        return true;
//    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (child == mContentView && mViewDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
			//绘制阴影
			final Rect childRect = mTmpRect;
			child.getHitRect(childRect);
			mShadowLeft.setAlpha(Math.max(0, Math.min(255, (int) (mScrimOpacity*255))));
			mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,childRect.left, childRect.bottom);
			mShadowLeft.draw(canvas);
			//绘制阴影背景
			final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
			final int alpha = (int) (baseAlpha * mScrimOpacity);
			final int color = alpha << 24 | (mScrimColor & 0xffffff);
			canvas.clipRect(0, 0, child.getLeft(), getHeight());
			canvas.drawColor(color);
        }
        return ret;
    }

    public interface CallBack {
        void onFinish();
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }
}
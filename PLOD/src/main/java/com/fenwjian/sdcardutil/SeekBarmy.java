/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fenwjian.sdcardutil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;


/**
 * A SeekBar is an extension of ProgressBar that adds a draggable thumb. The user can touch
 * the thumb and drag left or right to set the current progress level or use the arrow keys.
 * Placing focusable widgets to the left or right of a SeekBar is discouraged. 
 * <p>
 * Clients of the SeekBar can attach a {@link SeekBar.OnSeekBarChangeListener} to
 * be notified of the user's actions.
 *
 * @attr ref android.R.styleable#SeekBar_thumb
 */
public class SeekBarmy extends SeekBar {

    private float mLeft; // space between left of track and left of the view
    private float mRight; // space between right of track and left of the view
    private Paint mPaint;
    public  BSTree<Long> tree;
    public long timeLength;
    private float xLeft;
    private float xRight;
    private float mThumbRadiusOnDragging;
    private int thumbDelta;
    private Drawable mThumb; 
    
    
    
    public interface OnSeekBarChangeListener {  
        void onProgressChanged(SeekBarmy VerticalSeekBar, int progress, boolean fromUser);  
  
        void onStartTrackingTouch(SeekBarmy VerticalSeekBar);  
  
        void onStopTrackingTouch(SeekBarmy VerticalSeekBar);  
    }  
    private OnSeekBarChangeListener mOnSeekBarChangeListener; 
    
    public SeekBarmy(Context context) {
		super(context);
		ini();
	}

	public SeekBarmy(Context context, AttributeSet attrs) {
		super(context, attrs);
		ini();
	}


	public SeekBarmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		ini();
	}

	public SeekBarmy(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		ini();
	}

	public void ini(){
		tree=new BSTree<Long>();


        mThumbRadiusOnDragging=25;
		
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);	
        mPaint.setStrokeWidth(100);
	
	}
	
	@Override
	protected
    void onDraw(Canvas canvas) {
		canvas.rotate(-90);// 反转90度，将水平SeekBar竖起来  
		canvas.translate(-getHeight(), 0);// 将经过旋转后得到的VerticalSeekBar移到正确的位置,注意经旋转后宽高值互换  
        super.onDraw(canvas);
        
        xLeft = getPaddingLeft()+thumbDelta/2;
        xRight = getMeasuredWidth() - getPaddingRight() -thumbDelta/2;
		

        mPaint.setColor(Color.RED);
        // sectionMark
       // canvas.drawCircle(x_, yTop, 10.0f, mPaint);
        inOrderDraw(tree.getRoot(),canvas);
	
	}

    private void inOrderDraw(BSTNode<Long> tree,Canvas canvas) {
        if(tree != null && timeLength != 0) {
        	inOrderDraw(tree.left, canvas);
        	canvas.drawCircle((float)xLeft+(float)tree.key/(float)timeLength*(-xLeft + xRight), 25, 10.0f, mPaint);
            inOrderDraw(tree.right, canvas);
        }
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumb = thumb;
        mThumb.setAlpha(0);
        Rect thumbRect = thumb.getBounds();
        thumbDelta=thumb.getIntrinsicWidth();//thumbRect.left-thumbRect.right;
        //System.out.print("this is my delta:"+thumb.getIntrinsicWidth());
    }
    
    /*@Override  
    public void setThumb(Drawable thumb) {  
        mThumb = thumb;  
        super.setThumb(thumb);  
    }  
    
    */ 
    
  
    private void setThumbPos(int w, Drawable thumb, float scale, int gap) {  
        int available = w - getPaddingLeft() - getPaddingRight();  
        int thumbWidth = thumb.getIntrinsicWidth();  
        int thumbHeight = thumb.getIntrinsicHeight();  
        available -= thumbWidth;  
  
        // The extra space for the thumb to move on the track  
        available += getThumbOffset() * 2;  
  
        int thumbPos = (int) (scale * available);  
  
        int topBound, bottomBound;  
        if (gap == Integer.MIN_VALUE) {  
            Rect oldBounds = thumb.getBounds();  
            topBound = oldBounds.top;  
            bottomBound = oldBounds.bottom;  
        } else {  
            topBound = gap;  
            bottomBound = gap + thumbHeight;  
        }  
        thumb.setBounds(thumbPos, topBound*2, thumbPos + thumbWidth, bottomBound);  
    }  
  

    @Override  
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);  
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());// 宽高值互换  
    }  
    
    @Override  
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
        super.onSizeChanged(h, w, oldw, oldh);// 宽高值互换  
    }  
    
    
    void onStartTrackingTouch() {  
        if (mOnSeekBarChangeListener != null) {  
            mOnSeekBarChangeListener.onStartTrackingTouch(this);  
        }  
    }  
  
    void onStopTrackingTouch() {  
        if (mOnSeekBarChangeListener != null) {  
            mOnSeekBarChangeListener.onStopTrackingTouch(this);  
        }  
    }  
    // 与源码完全相同，仅为调用宽高值互换处理的 onStartTrackingTouch()方法  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        if (!isEnabled()) {  
            return false;  
        }  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN: {  
            setPressed(true);  
            onStartTrackingTouch();  
            trackTouchEvent(event);  
            break;  
        }  
  
        case MotionEvent.ACTION_MOVE: {  
            trackTouchEvent(event);  
            attemptClaimDrag();  
            break;  
        }  
  
        case MotionEvent.ACTION_UP: {  
            trackTouchEvent(event);  
            onStopTrackingTouch();  
            setPressed(false);  
            // ProgressBar doesn't know to repaint the thumb drawable  
            // in its inactive state when the touch stops (because the  
            // value has not apparently changed)  
            invalidate();  
            break;  
        }  
  
        case MotionEvent.ACTION_CANCEL: {  
            onStopTrackingTouch();  
            setPressed(false);  
            invalidate(); // see above explanation  
            break;  
        }  
  
        default:  
            break;  
        }  
        return true;  
    }  
  
    private float scale; 
    // 宽高值互换处
    // 理
    private void trackTouchEvent(MotionEvent event) {  
        final int height = getHeight();  
        final int available = height - getPaddingBottom() - getPaddingTop();  
        int Y = (int) event.getY();  
         
        float progress = 0;  
        if (Y > height - getPaddingBottom()) {  
            scale = 0.0f;  
        } else if (Y < getPaddingTop()) {  
            scale = 1.0f;  
        } else {  
            scale = (float) (height - getPaddingBottom() - Y) / (float) available;  
        }  
        final int max = getMax();  
        progress = scale * max;  
        //onProgressRefresh(progress, false);
        setThumbPos(getHeight(), mThumb, scale, 0);
        setProgress((int) progress);  
    }  
  
    private void attemptClaimDrag() {  
        if (getParent() != null) {  
            getParent().requestDisallowInterceptTouchEvent(true);  
        }  
    }  
    
}

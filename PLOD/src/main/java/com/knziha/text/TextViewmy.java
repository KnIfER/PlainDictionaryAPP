package com.knziha.text;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.CMN;

public class TextViewmy extends TextView {
    private Scroller mScroller;
    private int mScrollY;
    private int mMinScroll;
    private int mFlingV;
    private VelocityTracker mVelocityTracker;
    private float mLastMotionY;
    private TextView mText;
    private boolean didMove;
    private int sHeight;

    public TextViewmy(Context context) {
        super(context);
        InitScroller(context);
    }

    public TextViewmy(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        InitScroller(context);
    }

    public TextViewmy(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitScroller(context);
    }

    public void InitScroller(Context context) {
        mScroller = new Scroller(context);       // Get a scroller object
        mScrollY = 0 ;                          // Set beginning of program as top of screen.
        mMinScroll = getLineHeight ()/2;            // Set minimum scroll distance
        mFlingV = 750;                         // Minimum fling velocity
        mText=this;
        sHeight=getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        CMN.Log("tvmymy onDraw");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (mVelocityTracker == null) {                       // If we do not have velocity tracker
            mVelocityTracker = VelocityTracker.obtain();   // then get one
        }
        mVelocityTracker.addMovement(event);               // add this movement to it

        final int action = event.getAction();  // Get action type
        final float y = event.getY();          // Get the displacement for the action

        switch (action) {

            case MotionEvent.ACTION_DOWN:          // User has touched screen
                if (!mScroller.isFinished()) {     // If scrolling, then stop now
                    mScroller.abortAnimation();
                }
                mLastMotionY = y;                  // Save start(or end) of motion
                mScrollY = this.getScrollY();              // Save where we ended up
                mText.setCursorVisible (true);
                didMove = false;

                break;

            case MotionEvent.ACTION_MOVE:          // The user finger is on the move

                break;

            case MotionEvent.ACTION_UP:                       // User finger lifted up
                final VelocityTracker velocityTracker = mVelocityTracker;      // Find out how fast the finger was moving
                velocityTracker.computeCurrentVelocity(mFlingV);
                int velocityY = (int) velocityTracker.getYVelocity();

                if (Math.abs(velocityY) > mFlingV){                                // if the velocity exceeds threshold
                    int maxY = getLineCount() * getLineHeight () - sHeight;        // calculate maximum Y movement
                    mScroller.fling(0, mScrollY, 0, -velocityY, 0, 0, 0, maxY);    // Do the filng
                }else{
                    if (mVelocityTracker != null) {                                // If the velocity less than threshold
                        mVelocityTracker.recycle();                                // recycle the tracker
                        mVelocityTracker = null;
                    }
                }
                break;
        }

        mScrollY = this.getScrollY();              // Save where we ended up

        return true ;                                 // Tell caller we handled the move event
    }


    @Override
    public void computeScroll() {                  // Called while flinging to execute a fling step
        if (mScroller.computeScrollOffset()) {
            mScrollY = mScroller.getCurrY();       // Get where we should scroll to
            scrollTo(0, mScrollY);                 // and do it
            postInvalidate();                      // the redraw the sreem
        }
    }
}

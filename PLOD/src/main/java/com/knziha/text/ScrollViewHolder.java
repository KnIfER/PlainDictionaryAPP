package com.knziha.text;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

public class ScrollViewHolder extends ScrollView {
    SelectableTextView tv2guard;
    public ScrollViewHolder(Context context) {
        super(context);
        init();
    }
    public ScrollViewHolder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollViewHolder(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        //setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        if(tv2guard!=null) {
            SelectableTextView.lastTouchTime = System.currentTimeMillis();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tv2guard.isDragging = true;
                    break;
                case MotionEvent.ACTION_UP:
                    tv2guard.judgeClick();
                    break;
            }
        }
        return ret;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        if(tv2guard!=null && tv2guard.startInDrag){
            return false;
        }
        return ret;
    }
}

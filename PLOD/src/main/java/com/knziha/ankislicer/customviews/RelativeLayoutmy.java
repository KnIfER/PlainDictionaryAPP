package com.knziha.ankislicer.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by KnIfER on 2018/3/30.
 * 监听键盘弹出
 */

public class RelativeLayoutmy extends RelativeLayout {


    public RelativeLayoutmy(Context context) {
        super(context);
    }

    public RelativeLayoutmy(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutmy(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RelativeLayoutmy(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private OnResizeListener mOnResizeListener;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnResizeListener != null) {
            mOnResizeListener.onResize(w, h, oldw, oldh);
        }
    }
    public void setOnResizeListener(OnResizeListener listener) {
        this.mOnResizeListener = listener;
    }
    public interface OnResizeListener {
        void onResize(int w, int h, int oldw, int old);
    }
}

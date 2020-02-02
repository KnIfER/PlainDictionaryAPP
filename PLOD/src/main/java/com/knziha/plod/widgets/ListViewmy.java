package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.knziha.plod.PlainDict.R;

public class ListViewmy extends ListView {
	public interface OnScrollChangeListener {
		void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
	}
	OnScrollChangeListener mOnScrollChangeListener;
	public ListViewmy(Context context) {
		this(context, null);
	}
	public ListViewmy(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public ListViewmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

    //@Override
    //public int getVerticalScrollbarWidth() {
    //    return 50;
    //}

	public void setOnScrollChangeListener(OnScrollChangeListener l) {
		//super.setOnScrollChangeListener(l);
		mOnScrollChangeListener=l;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOnScrollChangeListener!=null)
			mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
	}

}

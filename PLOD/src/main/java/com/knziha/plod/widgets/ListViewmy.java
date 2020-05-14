package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewmy extends ListView {
	OnScrollChangedListener mOnScrollChangeListener;
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

	public void setOnScrollChangedListener(OnScrollChangedListener l) {
		mOnScrollChangeListener=l;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOnScrollChangeListener!=null)
			mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
	}
}

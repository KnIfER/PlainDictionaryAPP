package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.knziha.plod.plaindict.BasicAdapter;

public class ListViewmy extends ListView {
	public boolean dimmed;
	public Object FastScroller;
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
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if(adapter instanceof BasicAdapter) {
			((BasicAdapter)adapter).lava = this;
		}
		if(adapter instanceof OnItemClickListener) {
			setOnItemClickListener((OnItemClickListener) adapter);
		}
	}
	
	public void doDraw(Canvas canvas) {
		dispatchDraw(canvas);
	}
	
	@Override
	public void removeView(View child) {
		if(getAdapter() instanceof ListViewBasicViews.BasicViewsAdapter) {
			((ListViewBasicViews.BasicViewsAdapter)getAdapter()).removeView(child);
		} else {
			super.removeView(child);
		}
	}
	
	@Override
	public void removeViewAt(int index) {
		if(getAdapter() instanceof ListViewBasicViews.BasicViewsAdapter) {
			((ListViewBasicViews.BasicViewsAdapter)getAdapter()).removeViewAt(index);
		} else {
			super.removeViewAt(index);
		}
	}
	
	public void doDraw1(Canvas canvas) {
		onDraw(canvas);
	}
	
	public void doLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed,l,t,r,b);
		super.layoutChildren();
	}
	
	public void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void doMeasureChildren(int widthMeasureSpec, int heightMeasureSpec) {
		super.measureChildren(widthMeasureSpec, heightMeasureSpec);
	}
	
	protected boolean recycleOnMeasure() {
		return false;
	}
}

package com.knziha.plod.widgets;

import static android.widget.AdapterView.ITEM_VIEW_TYPE_IGNORE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.CMN;

import java.util.ArrayList;

public class ListViewBasicViews extends ViewGroup {
	public final ListViewmy mListView;
	public final BasicViewsAdapter mAdapter;
	public static class BasicViewsAdapter extends BaseAdapter {
		public final ArrayList<View> mViews = new ArrayList<>();
		@Override
		public int getCount() {
			return mViews.size();
		}
		
		@Override
		public Object getItem(int position) {
			return position>=0&&position<mViews.size()?mViews.get(position):null;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CMN.Log("getView::", position);
			return mViews.get(position);
		}
		
		public void removeView(View child) {
			mViews.remove(child);
		}
		
		public void removeViewAt(int index) {
			mViews.remove(index);
		}
		
		@Override
		public int getItemViewType(int position) {
			return ITEM_VIEW_TYPE_IGNORE;
		}
	}
	
	public ListViewBasicViews(Context context) {
		this(context, null);
	}
	
	public ListViewBasicViews(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ListViewBasicViews(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		ListViewmy mListView = new ListViewmy(context, attrs, defStyleAttr);
		mListView.setAdapter(mAdapter = new BasicViewsAdapter());
		mListView.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
		super.addView(mListView);
		mListView.setBackgroundColor(Color.GRAY);
		this.mListView = mListView;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom	) {
		final int width = getMeasuredWidth();
		final int height = getMeasuredHeight();
		//mListView.doLayout(changed, 0,0,100,100);
		mListView.layout(left, top, right, bottom);
	}
	
	
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		mListView.measure(widthMeasureSpec, heightMeasureSpec);
//	}
//
//	@Override
//	protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
//		mListView.doMeasureChildren(widthMeasureSpec, heightMeasureSpec);
//	}
	
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		mListView.doDraw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mListView.doDraw1(canvas);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		CMN.Log("mListView.getParent::", mListView.getParent());
		return mListView.dispatchTouchEvent(ev);
	}
	
//	@Override
//	public void addView(View child, int index) {
////		if (index < 0) {
////			index = mAdapter.getCount();
////		}
////		mAdapter.mViews.add(index, child);
////		mAdapter.notifyDataSetChanged();
//	}
//
////	@Override
////	public int getChildCount() {
////		return mAdapter.mViews.size();
////	}
////
////	@Override
////	public View getChildAt(int index) {
////		return mAdapter.mViews.get(index);
////	}
//
//	@Override
//	public void removeAllViews() {
//		mAdapter.mViews.clear();
//		mAdapter.notifyDataSetChanged();
//	}
//
//	@Override
//	public void removeViewAt(int index) {
//		//super.removeViewAt(index);
//		mAdapter.mViews.remove(index);
//		mAdapter.notifyDataSetChanged();
//	}
//
//	@Override
//	public void removeView(View view) {
//
//	}
}

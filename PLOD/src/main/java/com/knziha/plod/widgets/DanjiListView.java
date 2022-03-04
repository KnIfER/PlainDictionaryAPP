package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class DanjiListView extends RecyclerView {
	private boolean wantDanji;
	public DanjiListView(Context context) {
		this(context, null);
	}
	
	public DanjiListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DanjiListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getActionMasked();
		if (action==MotionEvent.ACTION_DOWN)
		{
			//CMN.Log("!!!ACTION_DOWN");
			if (getChildCount()<=2)
			{
				return false;
			}
			View child = getChildAt(Math.max(0, getChildCount()-1-2));
			if (child==null || ev.getY()>child.getBottom())
			{
				wantDanji=true;
			}
		}
		if (action==MotionEvent.ACTION_UP)
		{
			//CMN.Log("!!!ACTION_UP");
			if (wantDanji)
			{
				//CMN.Log("!!!performClick");
				View child = getChildAt(Math.max(0, getChildCount()-1-2));
				if (child==null || ev.getY()>child.getBottom())
				{
					performClick();
				}
				wantDanji=false;
			}
		}
		return super.dispatchTouchEvent(ev);
	}
}

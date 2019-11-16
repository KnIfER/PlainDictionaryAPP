package com.knziha.plod.widgets;

import android.view.View;

public class ListSizeConfiner implements View.OnLayoutChangeListener {//画地为牢
	int maxHeight;
	public ListSizeConfiner(){
	}
	public ListSizeConfiner setMaxHeight(int _maxHeight){
		maxHeight=_maxHeight;
		return this;
	}
	@Override
	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		if (v.getMeasuredHeight() > maxHeight)
			v.getLayoutParams().height=maxHeight;
		//v.removeOnLayoutChangeListener(this);
	}
}

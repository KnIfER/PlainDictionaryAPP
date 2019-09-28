package com.knziha.ankislicer.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class wahahaTextView extends TextView {
	public wahahaTextView(Context context) {
		super(context);
	}
	public wahahaTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public wahahaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public wahahaTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	public static View mR;
	
	@Override
    public View getRootView() {
        return mR!=null?mR:super.getRootView();
    	//if(mR==null) mR=super.getRootView();
    	//return mR;
    }



	
	

}

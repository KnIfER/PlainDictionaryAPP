package com.knziha.plod.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class SelectiveClickableTextView extends TextView {
	public SelectiveClickableTextView(Context context) {
		super(context);
	}
	public SelectiveClickableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public SelectiveClickableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public SelectiveClickableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	
	private int margin=50;
	@Override
	public boolean onTouchEvent(MotionEvent e){
		float x = e.getX();
		if(x> margin && x<=getWidth()-margin)
			return super.onTouchEvent(e);
		return true;
	}
	
	
	
	
	

}

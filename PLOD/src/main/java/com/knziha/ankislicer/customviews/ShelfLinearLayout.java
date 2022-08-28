package com.knziha.ankislicer.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ShelfLinearLayout extends LinearLayout {
	Paint p = new Paint();public Paint getPaint(){return p;}
	Rect r = new Rect();
	public boolean drawRectOver=false;
	
	public ShelfLinearLayout(Context context) {
		super(context);
		init();
	}
	public ShelfLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public ShelfLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public ShelfLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	
	private void init() {
		p.setColor(ShelfDefaultGray);	
	}
	
	public int ShelfDefaultGray = 0xFF4F7FDF;//0xFF808080;
	public void setSCC(int c) {//ShelfClipColor
		if(p.getColor()!=c) {
			p.setColor(c);
			invalidate();
		}
	}
	
	@Override
	public void onDraw(Canvas c) {
		if(!drawRectOver)c.drawRect(r, p);
		super.onDraw(c);
		if(drawRectOver)c.drawRect(r, p);
	}
	
	public View selectedTool;
	public void selectToolView(View v) {
		if (selectedTool!=v) {
			selectedTool = v;
			v.getDrawingRect(r);
			if(getOrientation()==LinearLayout.VERTICAL) {
				r.top += v.getTop();
				r.bottom += v.getTop();
			}else {
				r.left += v.getLeft();
				r.right += v.getLeft();
			}
			invalidate();
		}
	}
	public void selectToolIndex(int i) {
		selectToolView(getChildAt(i));
	}
	
	@Override
	public void setOnClickListener(@Nullable OnClickListener l) {
		int cc = getChildCount();
		for (int i = 0; i < cc; i++) {
			getChildAt(i).setOnClickListener(l);
		}
	}
	
	@Override
	public void setOnLongClickListener(@Nullable OnLongClickListener l) {
		int cc = getChildCount();
		for (int i = 0; i < cc; i++) {
			getChildAt(i).setOnLongClickListener(l);
		}
	}
}

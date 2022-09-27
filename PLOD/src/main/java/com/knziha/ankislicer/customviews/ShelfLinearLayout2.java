package com.knziha.ankislicer.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.knziha.plod.plaindict.CMN;

import java.util.ArrayList;

public class ShelfLinearLayout2 extends LinearLayout {
	public final Paint p = new Paint();
	ArrayList<View> checkedItems = new ArrayList<>();
	public boolean drawRectOver=false;
	
	public ShelfLinearLayout2(Context context, AttributeSet attrs) {
		super(context, attrs);
		p.setColor(0xFF4F7FDF);
	}
	
	public void setCheckedColor(int c) {
		if(p.getColor()!=c) {
			p.setColor(c);
			invalidate();
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		//CMN.debug("dispatchDraw::checkedItems::", checkedItems);
		if(!drawRectOver)
			drawCheck(canvas);
		super.dispatchDraw(canvas);
		if(drawRectOver)
			drawCheck(canvas);
	}
	
	private void drawCheck(Canvas c) {
		View v;
		for (View child:checkedItems) {
			int left=0,top=0;
			v = child;
			while(v!=null && v!=this) {
				MarginLayoutParams lp = null;
				try {
					lp = (MarginLayoutParams) v.getLayoutParams();
				} catch (Exception e) {
					CMN.debug(e);
				}
				if(getOrientation()==LinearLayout.VERTICAL) {
					top += v.getTop();
				} else {
					left += v.getLeft();
				}
				if(lp!=null) top+=lp.topMargin;
				v = (View) v.getParent();
			}
			//CMN.debug("checkedItems::drawRect::", rc.toString());
			c.drawRect(left,top,left+child.getWidth(),top+child.getHeight(), p);
		}
	}
	
	public boolean toggleViewChecked(View v) {
		//CMN.debug("toggleViewChecked::checkedItems::", checkedItems);
		int idx = checkedItems.indexOf(v);
		if (idx >= 0) {
			checkedItems.remove(idx);
			return false;
		} else {
			checkedItems.add(v);
			return true;
		}
	}
	
	public void setViewChecked(View v, boolean checked) {
		//CMN.debug("toggleViewChecked::checkedItems::", checkedItems);
		int idx = checkedItems.indexOf(v);
		if (checked ^ idx>=0) {
			if (checked) {
				checkedItems.add(v);
			} else {
				checkedItems.remove(idx);
			}
		}
	}
	
	public boolean isViewChecked(View v) {
		return checkedItems.indexOf(v) > 0;
	}
}

package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;

/**
 * Created by KnIfER on 2017/11/4.
 */

public class TextViewmy extends TextView {
	public boolean doRescueAM;
	public TextViewmy(Context context) {
		this(context, null);
	}

	public TextViewmy(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextViewmy(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean performHapticFeedback(int feedbackConstant, int flags) {
		try {
			return super.performHapticFeedback(feedbackConstant, flags);
		} catch (Exception e) {
			e.printStackTrace();/* 无敌 */
		}
		return false;
	}

	@Override
	public void scrollTo(int x, int y) {
		//super.scrollTo(x, y);
	}


	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		//CMN.Log("startActionMode");
		return super.startActionMode(callback, type);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//CMN.Log("tvmy", event.toString());
		if(doRescueAM && event.getAction()==MotionEvent.ACTION_CANCEL)
			event.setAction(MotionEvent.ACTION_UP);
		return super.onTouchEvent(event);
	}

	@Override
	public void setTextIsSelectable(boolean selectable) {
		super.setTextIsSelectable(doRescueAM=selectable);
	}
}

package com.knziha.plod.PlainUI;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WindowLayout;

public class FloatApp implements View.OnTouchListener {
	public final WindowManager wMan;
	public final AgentApplication app;
	public WindowManager.LayoutParams lp;
	public WindowLayout view;
	public ViewGroup contentView;
	public ViewGroup appContentView;
	final int[] savedXY = new int[4];
	public PDICMainActivity a;
	
	public FloatApp(PDICMainActivity a) {
		this.a = a;
		Context context = a.getApplicationContext();
		this.app = (AgentApplication) a.getApplication();
		this.wMan = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}
	
	public void floatWindow() {
		if (view != null) {
			if (view.getParent()!=null) {
				wMan.removeView(view);
			}
		} else {
			view = (WindowLayout) a.getLayoutInflater().inflate(R.layout.multiwindow_root, null);
			view.floatApp = this;
			ViewGroup views = (ViewGroup) view.getChildAt(0);
			views.getChildAt(0).setOnTouchListener(this);
			contentView = ((ViewGroup)views.getChildAt(1));
		}
		//view.setOnTouchListener(this);
		View v = a.UIData.root;
		v.setFitsSystemWindows(false);
		v.setPadding(0,0,0,0);
		
		if (appContentView==null) {
			appContentView = (ViewGroup) v.getParent();
		}
		ViewUtils.removeView(v);
		contentView.addView(v);
		a.moveTaskToBack(true);
		
		int sz = (int) (GlobalOptions.density*500);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				(int) (GlobalOptions.density*300) , sz
				, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
				? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
				: WindowManager.LayoutParams.TYPE_PHONE
				, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				, PixelFormat.RGBA_8888);
		
		lp.gravity = Gravity.START | Gravity.TOP;
		this.lp = lp;
		try {
			wMan.addView(view, lp);
			a.mDialogType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} catch (Exception e) {
			CMN.Log(e);
			toggle(true);
		}
	}
	
	public void toggle(boolean close) {
		if (view!=null && view.getParent()!=null) {
			wMan.removeView(view);
			ViewUtils.addViewToParent(a.UIData.root, appContentView);
			a.UIData.root.setFitsSystemWindows(true);
			a.UIData.root.setPadding(0,CMN.getStatusBarHeight(a),0,0);
			a.mDialogType = WindowManager.LayoutParams.TYPE_APPLICATION;
			a.startActivity(new Intent(a, a.getClass()));
		} else if (!close) {
			floatWindow();
		}
	}
	
	float orgX;
	float orgY;
	boolean moved;
	int x;
	int y;
	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		int e=ev.getActionMasked();
		boolean moveX = true;
		boolean moveY = true;
		if (e==MotionEvent.ACTION_OUTSIDE) {
			a.showT("ACTION_OUTSIDE");
			enableKeyBoard(false);
			return false;
		}
		else if (e==MotionEvent.ACTION_DOWN) {
			orgX = ev.getRawX();
			orgY = ev.getRawY();
			x = lp.x;
			y = lp.y;
			moved = false;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				view.suppressLayout(true);
			}
		}
		else if (e==MotionEvent.ACTION_MOVE) {
			if (!moved && Math.max(Math.abs(ev.getRawX() - orgX), Math.abs(ev.getRawY() - orgY))>GlobalOptions.density*1.5) {
				moved = true;
			}
			if (moved) {
				if(moveX) lp.x = (int) (x + ev.getRawX() - orgX);
				if(moveY) lp.y = (int) (y + ev.getRawY() - orgY);
				wMan.updateViewLayout(view, lp);
			}
		}
		else if (e==MotionEvent.ACTION_UP) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				view.suppressLayout(false);
			}
			if (moved) {
				savedXY[0] = lp.x;
				savedXY[1] = lp.y;
			}
		}
		return true;
	}
	
	public final void enableKeyBoard(boolean enable) {
		if (view!=null && view.getParent()!=null) {
			final int mask = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
			if (enable ^ (lp.flags&mask)==0) {
				if (enable) {
					lp.flags &= ~mask;
				} else {
					lp.flags |= mask;
				}
				wMan.updateViewLayout(view, lp);
			}
		}
	}
	
	public final void updateView(RectF frameOffsets) {
		lp.x = (int) frameOffsets.left;
		lp.y = (int) (frameOffsets.top - CMN.statusBarHeight);
		lp.width = (int) frameOffsets.width();
		lp.height = (int) frameOffsets.height();
		wMan.updateViewLayout(view, lp);
	}
}

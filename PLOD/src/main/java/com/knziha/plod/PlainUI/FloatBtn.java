package com.knziha.plod.PlainUI;

import static com.knziha.plod.plaindict.MainActivityUIBase.foreground;
import static com.knziha.plod.plaindict.MainShareActivity.SingleTaskFlags;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.R;

import java.util.List;

public class FloatBtn implements View.OnTouchListener, View.OnDragListener {
	public final WindowManager wMan;
	public final ClipboardManager clipMan;
	public final Context context;
	public final AgentApplication app;
	WindowManager.LayoutParams lp;
	View view;
	int btnType;
	final int[] savedXY = new int[4];
	
	public FloatBtn(Context context, Application application) {
		this.context = context;
		this.app = (AgentApplication) application;
		this.wMan = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		this.clipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
	}
	
	public void reInitBtn(int btnType) {
		if (view!=null) {
			wMan.removeView(view);
		}
		view = btnType==0?(ViewGroup) LayoutInflater.from(context).inflate(R.layout.btn, null):new View(context);
		
		int sz = (int) (GlobalOptions.density*38);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				sz , sz
				, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
				, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				, PixelFormat.RGBA_8888);
		
		lp.gravity = Gravity.START | Gravity.TOP;
		
		if (btnType==0) {
			lp.x = savedXY[0];
			lp.y = savedXY[1];
		}
		else {
			view.setBackgroundResource(R.drawable.progressbar2);
			int szLong = (int) (sz*2.5), szShort = (int) (GlobalOptions.density*15);
			if (btnType == 2 || btnType == 4) {
				lp.x = savedXY[2];
				lp.width = szLong;
				lp.height = szShort;
				lp.gravity = Gravity.CENTER_HORIZONTAL | (btnType==2?Gravity.TOP:Gravity.BOTTOM);
			} else {
				lp.y = savedXY[3];
				lp.width = szShort;
				lp.height = szLong;
				lp.gravity = Gravity.CENTER_VERTICAL | (btnType==1?Gravity.RIGHT:Gravity.LEFT);
			}
		}
		View handle = btnType==0?((ViewGroup)view).getChildAt(0):view;
		handle.setOnTouchListener(this);
		//handle.setOnClickListener(this);
		view.setOnDragListener(this);
		this.btnType = btnType;
		this.lp = lp;
		wMan.addView(view, lp);
	}
	
	float orgX;
	float orgY;
	boolean moved;
	int x;
	int y;
	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		int e=ev.getActionMasked();
		boolean moveX = btnType==0 || btnType==2 || btnType==4;
		boolean moveY = btnType==0 || btnType==1 || btnType==3;
		if (e==MotionEvent.ACTION_DOWN) {
			orgX = ev.getRawX();
			orgY = ev.getRawY();
			x = lp.x;
			y = lp.y;
			moved = false;
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
			if (moved) {
				if (btnType==0) {
					savedXY[0] = lp.x;
					savedXY[1] = lp.y;
				} else if (moveX) {
					savedXY[2] = lp.x;
				} else if (moveY) {
					savedXY[3] = lp.y;
				}
			} else {
				search(null, true);
			}
		}
		return true;
	}
	
	@Override
	public boolean onDrag(View v, DragEvent event) {
		if(event.getAction()== DragEvent.ACTION_DROP){
			try {
				ClipData textdata = event.getClipData();
				if(textdata.getItemCount()>0){
					search(textdata.getItemAt(0).getText(), false);
				}
				return false;
			} catch (Exception e) { }
		}
		return true;
	}
	
	public void search(CharSequence text, boolean checkAct) {
		if (text==null) {
			if (checkAct && foreground!=0) {
//				ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//				List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
//				String packageName = rti.get(0).topActivity.getPackageName();
//				if (packageName.equals(context.getPackageName())) {
//
//				}
				int actId = 0, fore=foreground;
				while((fore>>1)!=0) {
					actId++;
				}
				Handler hdl = app.handles[actId];
				if(hdl!=null) {
					Message msg = hdl.obtainMessage(1024);
					msg.obj = this;
					msg.sendToTarget(); //todo recycle
				}
				return;
			}
			ClipData pclip = clipMan.getPrimaryClip();
			if (pclip!=null && pclip.getItemCount()>0) {
				text = pclip.getItemAt(0).getText();
			}
		}
		if (text!=null) {
			text = text.toString().trim();
		}
		//((MainActivityUIBase)context).showT(text);
		if (TextUtils.isEmpty(text)){
			return;
		}
		Intent newTask = new Intent(Intent.ACTION_MAIN);
		newTask.setType(Intent.CATEGORY_DEFAULT);
		newTask.putExtra(Intent.EXTRA_TEXT,text);
		newTask.setClass(context, PDICMainActivity.class);
		newTask.setFlags(SingleTaskFlags);
		context.startActivity(newTask);
	}
	
	public void close() {
		if (view!=null) {
			wMan.removeView(view);
			view = null;
		}
	}
}

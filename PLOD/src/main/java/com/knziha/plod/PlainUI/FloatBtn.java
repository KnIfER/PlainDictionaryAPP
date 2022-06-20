package com.knziha.plod.PlainUI;

import static com.knziha.plod.plaindict.MainActivityUIBase.foreground;
import static com.knziha.plod.plaindict.MainShareActivity.SingleTaskFlags;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.FloatActivitySearch;
import com.knziha.plod.plaindict.MainShareActivity;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PasteActivity;
import com.knziha.plod.plaindict.R;

public class FloatBtn implements View.OnTouchListener, View.OnDragListener {
	public final WindowManager wMan;
	public final static String EXTRA_GETTEXT = "ext_clip";
	public final static String EXTRA_FROMPASTE = "ext_paste";
	public final Context context;
	public final ClipboardManager clipMan;
	public final AgentApplication app;
	WindowManager.LayoutParams lp;
	public DisplayMetrics dm = new DisplayMetrics();
	final WindowManager.LayoutParams[] layoutParams = new WindowManager.LayoutParams[5];
	final ScreenConfig[] screenConfigs = new ScreenConfig[5];
	public ScreenConfig screenConfig;
	public boolean landScape;
	FrameLayout view;
	int btnType;
	private int btnWidth;
	
	public FloatBtn(Context context, Application application) {
		this.context = context = context.getApplicationContext();
		this.app = (AgentApplication) application;
		this.wMan = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		this.clipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
	}
	
	public void reInitBtn(int btnType) {
		if (btnType==-1) {
			btnType = this.btnType;
		} else if (btnType != this.btnType && view!=null) {
			view.removeAllViews();
		}
		if (view==null) {
			view = new FrameLayout(context);
			view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					if(!moved)
					{
						CMN.debug("onLayoutChange::");
						((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
						if (!screenConfig.sameScreen(dm)) {
							wMan.updateViewLayout(view, calcLayout());
						}
					}
				}
			});
		}
		if (view.getChildAt(0)==null) {
			View btnView, handle;
			if (btnType == 0) {
				btnView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.btn, null);
				handle = ((ViewGroup)btnView).getChildAt(0);
			} else {
				btnView = handle = new View(context);
				btnView.setBackgroundResource(R.drawable.progressbar2);
			}
			view.addView(btnView);
			view.setOnDragListener(this);
			handle.setOnTouchListener(this);
		}
		this.btnType = btnType;
		try {
			if (view.getParent() == null) {
				wMan.addView(view, calcLayout());
			} else {
				wMan.updateViewLayout(view, calcLayout());
			}
		} catch (Exception e) {
			CMN.Log(e);
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
		else if (e==MotionEvent.ACTION_UP || e==MotionEvent.ACTION_CANCEL) {
			if (moved) {
				moved = false;
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
		boolean floating = app.floatApp != null && app.floatApp.isFloating();
		if (floating) {
			app.floatApp.expand(false);
			if (text==null && !PDICMainAppOptions.floatBtn()) {
				return;
			}
		}
		if (text==null) {
			if (checkAct && foreground!=0) {
//				ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//				List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
//				String packageName = rti.get(0).topActivity.getPackageName();
//				if (packageName.equals(context.getPackageName())) {
//
//				} //todo
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
			text = getPrimaryClip();
		}
		if (text!=null) {
			text = text.toString().trim();
		}
		//((MainActivityUIBase)context).showT(text);
		CMN.debug("floatBtn::text::", text);
		Intent newTask = new Intent(Intent.ACTION_MAIN);
		newTask.setType(Intent.CATEGORY_DEFAULT);
		newTask.putExtra(EXTRA_FROMPASTE, true);
		if (TextUtils.isEmpty(text)) {
			newTask.putExtra(EXTRA_GETTEXT, true);
		} else {
			newTask.putExtra(Intent.EXTRA_TEXT,text);
		}
		newTask.setClass(context, PDICMainActivity.class);
		newTask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (floating) {
			if (TextUtils.isEmpty(text)) {
				newTask.setClass(context, PasteActivity.class);
				context.startActivity(newTask);
			} else {
				app.floatApp.a.processIntent(newTask, false);
			}
		} else {
//			if (app.floatApp!=null) {
//				CMN.debug("floatBtn::moveTaskToFront::");
//				app.floatApp.a.moveTaskToFront();
//				app.floatApp.a.processIntent(newTask, false);
//			} else {
				context.startActivity(newTask);
//			}
		}
	}
	
	public CharSequence getPrimaryClip() {
		ClipData pclip = clipMan.getPrimaryClip();
		if (pclip!=null && pclip.getItemCount()>0) {
			return pclip.getItemAt(0).getText();
		}
		return null;
	}
	
	public void close() {
		if (view!=null && view.getParent()!=null) {
			wMan.removeView(view);
		}
	}
	
	/** 初始化坐标布局 */
	private WindowManager.LayoutParams calcLayout() {
		WindowManager.LayoutParams lp;
		((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		ScreenConfig screenConfig = screenConfigs[btnType];
		lp = layoutParams[btnType];
		if (lp==null || !screenConfig.sameScreen(dm)) {
			if (btnWidth ==0) {
				btnWidth = (int) context.getResources().getDimension(R.dimen._38_);
			}
			if (lp==null) {
				layoutParams[btnType] = lp = new WindowManager.LayoutParams(
					btnWidth, btnWidth
					, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
					? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
					: WindowManager.LayoutParams.TYPE_PHONE
					, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					, PixelFormat.RGBA_8888);
				lp.gravity = Gravity.START | Gravity.TOP;
			}
			if (screenConfig == null) {
				screenConfigs[btnType] = screenConfig = new ScreenConfig(dm);
				if (btnType == 0) {
					lp.x = (int) (dm.widthPixels - btnWidth * (GlobalOptions.isLarge?2:1.5f));
					lp.y = (dm.heightPixels - lp.height) / 2;
				} else {
					int szLong = (int) (btnWidth * 2.5), szShort = (int) (GlobalOptions.density * 15);
					if (btnType == 2 || btnType == 4) {
						//lp.x = savedXY[2];
						lp.width = szLong;
						lp.height = szShort;
						lp.gravity = Gravity.CENTER_HORIZONTAL | (btnType == 2 ? Gravity.TOP : Gravity.BOTTOM);
					} else {
						//lp.y = savedXY[3];
						lp.width = szShort;
						lp.height = szLong;
						lp.gravity = Gravity.CENTER_VERTICAL | (btnType == 1 ? Gravity.RIGHT : Gravity.LEFT);
					}
				}
			} else {
				if (btnType == 0) {
					lp.x = (int) (lp.x/screenConfig.widthPixels*dm.widthPixels);
					lp.y = (int) (lp.y/screenConfig.heightPixels*dm.heightPixels);
				} else {
					if (btnType == 2 || btnType == 4) {
						lp.x = (int) (lp.x/screenConfig.widthPixels*dm.widthPixels);
					} else {
						lp.y = (int) (lp.y/screenConfig.heightPixels*dm.heightPixels);
					}
				}
				screenConfig.widthPixels = dm.widthPixels;
				screenConfig.heightPixels = dm.heightPixels;
			}
		}
		this.lp = lp;
		this.screenConfig = screenConfig;
		return lp;
	}
}

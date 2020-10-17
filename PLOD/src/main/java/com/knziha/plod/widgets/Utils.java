/*
 *  Copyright © 2016, Turing Technologies, an unincorporated organisation of Wynne Plaga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.knziha.plod.widgets;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.view.NestedScrollingChildHelper;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.RebootActivity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import static com.knziha.filepicker.utils.FU.bKindButComplexSdcardAvailable;

public class Utils {
	public static float density;
	
	public static Paint mRectPaint;
	
	public static int FloatTextBG = 0xffffff00;
	
	public final static Cursor EmptyCursor=new AbstractWindowedCursor() {
		@Override
		public int getCount() {
			return 0;
		}
		public String[] getColumnNames() {
			return new String[0];
		}
	};
	
    static int getDP(int dp, View v){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, v.getResources().getDisplayMetrics());
    }

    static int getDP(float dp, Context c){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }
	
	public static NestedScrollingChildHelper mNestedScrollingChildHelper;
 
	public static NestedScrollingChildHelper getNestedScrollingChildHelper() {
		if(mNestedScrollingChildHelper==null)
			mNestedScrollingChildHelper=new NestedScrollingChildHelper(null);
		return mNestedScrollingChildHelper;
	}
	
	public static Paint getRectPaint() {
		if(mRectPaint==null) {
			mRectPaint = new Paint();
			mRectPaint.setColor(GlobalOptions.isDark?0x3fffff00:FloatTextBG);
		}
		return mRectPaint;
	}
	
	public static void setFloatTextBG(int colorVal) {
		FloatTextBG = colorVal;
		if(mRectPaint!=null) {
			mRectPaint.setColor(colorVal);
		}
	}
	
	public static boolean DGShowing(AlertDialog dTmp) {
		Window win = dTmp==null?null:dTmp.getWindow();
		return win!=null&&win.getDecorView().getParent()!=null;
	}
	
	public static boolean isWindowDetached(Window window) {
		return window==null || window.getDecorView().getParent()==null || window.getDecorView().getVisibility()!=View.VISIBLE;
	}
	
	public static boolean removeIfParentBeOrNotBe(View view, ViewGroup parent, boolean tobe) {
		if(view!=null) {
			ViewParent svp = view.getParent();
			if(parent==svp ^ !tobe) {
				if(svp!=null) {
					((ViewGroup)svp).removeView(view);
				}
				return true;
			}
		}
		return false;
	}
	
	public static void removeIfChildIsNot(View someView, ViewGroup parent) {
		int cc=parent.getChildCount();
		if(cc>1) {
			for(int i=cc-1;i>=0;i--)
				if(parent.getChildAt(i)!=someView)
					parent.removeViewAt(i);
		}
	}
	
	public static void dimScrollbar(ListViewmy lv, boolean val) {
		lv.dimmed=!val;
		lv.setVerticalScrollBarEnabled(val);
		lv.setFastScrollEnabled(val);
	}
	
	public static void removeAllViews(ViewGroup parent) {
		if(parent.getChildCount()>0) {
			parent.removeAllViews();
		}
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent) {
		if(removeIfParentBeOrNotBe(view2Add, parent, false)) {
			parent.addView(view2Add);
			return true;
		}
		return false;
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent, int idx) {
		if(removeIfParentBeOrNotBe(view2Add, parent, false)) {
			parent.addView(view2Add, idx);
			return true;
		}
		return false;
	}
	
	public static void addViewToParentUnique(View view2Add, ViewGroup parent) {
		addViewToParent(view2Add, parent);
		removeIfChildIsNot(view2Add, parent);
	}
	
	public static boolean ViewIsId(View view, int id) {
		return view!=null && view.getId()==id;
	}
	
	public static CharSequence decorateSuffixTick(CharSequence title, boolean hasTick) {
		int len=title.length();
		boolean b1 = title.charAt(len-1)=='√';
		if(b1 ^ hasTick) {
			return hasTick?title+" √":title.subSequence(0, len-2);
		}
		return title;
	}
	
	public void Destory(){
		mNestedScrollingChildHelper.Destory();
		mNestedScrollingChildHelper = null;
		mRectPaint = null;
	}
	
	
	public static void CleanExitApp(Activity a, boolean restart, boolean clearTasks, boolean shutdownvm) {
		if(restart) {
			PendingIntent restartIntent = PendingIntent.getActivity(a.getApplicationContext(), 175, new Intent(a, RebootActivity.class), PendingIntent.FLAG_ONE_SHOT);
			AlarmManager alarmManager = (AlarmManager) a.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+1350, restartIntent);
			clearTasks=shutdownvm=true;
		}
		
		if(clearTasks) {
			finishTasksIfRemovable(a);
		}
		
		a.finish();
		
		if(shutdownvm) {
			//System.exit(0);
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
	public static void finishTasksIfRemovable(Activity a) {
		if(bKindButComplexSdcardAvailable) {
			ActivityManager am = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.AppTask> appTasks = am.getAppTasks();
			for (ActivityManager.AppTask taskI:appTasks) {
				if(taskI!=null) {
					ActivityManager.RecentTaskInfo tin = taskI.getTaskInfo();
					if (tin != null) {
						ComponentName tinbascomp = tin.baseIntent.getComponent();
						if (tinbascomp != null && tinbascomp.getPackageName().equals(a.getPackageName())) {
							taskI.finishAndRemoveTask();
						}
					}
				}
			}
		}
	}
	
	private static class DummyOnClick implements View.OnClickListener {
		@Override
		public void onClick(View v) { }
	}
	public static View.OnClickListener DummyOnClick = new DummyOnClick();
	
	
	public static View getViewItemByPath(Object obj, int...path) {
		int cc=0;
		while(cc<path.length) {
			//CMN.Log(cc, obj);
			if(obj instanceof ViewGroup) {
				obj = ((ViewGroup)obj).getChildAt(path[cc]);
			} else {
				obj = null;
				break;
			}
			cc++;
		}
		return Objects.requireNonNull((View)obj);
	}
	
	
	public static void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, int depth, int idxStart, Object[] viewFetcher) {
		int cc = vg.getChildCount();
		View ca;
		for (int i = idxStart; i < cc; i++) {
			ca = vg.getChildAt(i);
			//CMN.Log("setOnClickListenersOneDepth", ca, (i+1)+"/"+(cc), ca.isEnabled());
			if(ca instanceof ViewGroup) {
				if(--depth>0) {
					setOnClickListenersOneDepth((ViewGroup) ca, clicker, depth, 0, viewFetcher);
				}
			} else {
				int id = ca.getId();
				if(ca.getId()!=View.NO_ID){
					if(!(ca instanceof EditText) && ca.isEnabled()) {
						ca.setOnClickListener(clicker);
						if(clicker instanceof View.OnLongClickListener && ca.isLongClickable()) {
							ca.setOnLongClickListener((View.OnLongClickListener) clicker);
						}
					}
					if(viewFetcher!=null) {
						for (int j = 0; j < viewFetcher.length; j++) {
							if(viewFetcher[j] instanceof Integer && (int)viewFetcher[j]==id) {
								viewFetcher[j]=ca;
								break;
							}
						}
					}
				}
			}
		}
	}
	
	
	public static class BaseAnimationListener implements Animation.AnimationListener {
		@Override public void onAnimationStart(Animation animation) { }
		@Override public void onAnimationEnd(Animation animation) {  }
		@Override public void onAnimationRepeat(Animation animation) {  }
	}
	
	public static class BaseAnimatorListener implements Animator.AnimatorListener {
		@Override public void onAnimationStart(Animator animation) { }
		@Override public void onAnimationEnd(Animator animation) {  }
		@Override public void onAnimationCancel(Animator animation) { }
		@Override public void onAnimationRepeat(Animator animation) { }
	}
	
	
	static Field FastScrollField;
	static Field TrackDrawableField;
	static Field ThumbImageViewField;
	static Field ScrollCacheField;
	static Field ScrollBarDrawableField;
	
	public static void setListViewScrollbarColor(View mListView, boolean red) {
		try {
			ensureScrollbarFields();
			Object Scrollbar = ScrollCacheField.get(mListView);
			Drawable ScrollbarDrawable = (Drawable) ScrollBarDrawableField.get(Scrollbar);
			ScrollbarDrawable.setColorFilter(red?RED:GREY);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	static ColorFilter RED = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
	static ColorFilter GREY = new PorterDuffColorFilter(0x8a666666, PorterDuff.Mode.SRC_IN);
	
	private static void ensureScrollbarFields() throws Exception {
		if(ScrollCacheField==null) {
			ScrollCacheField = View.class.getDeclaredField("mScrollCache");
			ScrollCacheField.setAccessible(true);
			ScrollBarDrawableField = Class.forName("android.view.View$ScrollabilityCache").getDeclaredField("scrollBar");
			ScrollBarDrawableField.setAccessible(true);
		}
	}
	
	private static void ensureFastscrollFields() throws Exception{
		if(FastScrollField==null) {
			Class FastScrollerClass = Class.forName("android.widget.FastScroller");
			FastScrollField = AbsListView.class.getDeclaredField("mFastScroll");
			FastScrollField.setAccessible(true);
			TrackDrawableField = FastScrollerClass.getDeclaredField("mTrackDrawable");
			TrackDrawableField.setAccessible(true);
			
			ThumbImageViewField = FastScrollerClass.getDeclaredField("mThumbImage");
			ThumbImageViewField.setAccessible(true);
		}
	}
	
	public static void setListViewFastColor(View...mListViews) {
		try {
			ensureFastscrollFields();
			for(View mListView:mListViews) {
				Object FastScroller = FastScrollField.get(mListView);
				if(FastScroller!=null) {
					ImageView ThumbImage = (ImageView) ThumbImageViewField.get(FastScroller);
					if (ThumbImage != null) {
						ThumbImage.setColorFilter(GREY);
					}
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public static void listViewStrictScroll(boolean IsStrictSCroll, ListViewmy...mListViews) {
		try {
			ensureFastscrollFields();
			for(ListViewmy mListView:mListViews) {
				if(IsStrictSCroll) {
					Object FastScroller = FastScrollField.get(mListView);
					if(FastScroller!=null && TrackDrawableField!=null) {
						mListView.FastScroller = FastScroller;
						TrackDrawableField.set(FastScroller, null);
					}
				} else {
					TrackDrawableField.set(mListView, mListView.FastScroller);
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
}

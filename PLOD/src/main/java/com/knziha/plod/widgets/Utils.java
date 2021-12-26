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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.RebootActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.knziha.filepicker.utils.FU.bKindButComplexSdcardAvailable;
import static com.knziha.plod.plaindict.CMN.AssetTag;

public class Utils {
	public static float density;
	
	public static Paint mRectPaint;
	public static Paint mRectPaintAlpha;
	
	public static int FloatTextBG = 0xffffff00;
	public static int FloatTextBGAlpha = 0x7fffff00;
	
	public final static Cursor EmptyCursor=new AbstractWindowedCursor() {
		@Override
		public int getCount() {
			return 0;
		}
		public String[] getColumnNames() {
			return new String[0];
		}
	};
	public static final boolean littleCat = Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT;
	public static final boolean littleCake = Build.VERSION.SDK_INT<=21;
	public static final boolean bigMountain = Build.VERSION.SDK_INT>22;
	public static final boolean hugeHimalaya = Build.VERSION.SDK_INT>=Build.VERSION_CODES.P;
	
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
			if(GlobalOptions.isDark) {
				mRectPaint.setColor(0x3fffff00);
			} else {
				mRectPaint.setColor(FloatTextBG);
				mRectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
			}
		}
		return mRectPaint;
	}
	
	public static Paint getRectPaintAlpha() {
		if(mRectPaintAlpha==null) {
			mRectPaintAlpha = new Paint();
			mRectPaintAlpha.setColor(FloatTextBGAlpha);
			mRectPaintAlpha.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
		}
		return mRectPaintAlpha;
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
	
	
	static Object instance_WindowManagerGlobal;
	static Class class_WindowManagerGlobal;
	static Field field_mViews;
	
	public static void logAllViews(){
		List<View> views = getWindowManagerViews();
		for(View vI:views){
			CMN.Log("\n\n\n\n\n::  "+vI);
			CMN.recurseLog(vI);
		}
	}
	
	/* get the list from WindowManagerGlobal.mViews */
	public static List<View> getWindowManagerViews() {
		if(instance_WindowManagerGlobal instanceof Exception) {
			return new ArrayList<>();
		}
		try {
			if(instance_WindowManagerGlobal==null) {
				class_WindowManagerGlobal = Class.forName("android.view.WindowManagerGlobal");
				field_mViews = class_WindowManagerGlobal.getDeclaredField("mViews");
				field_mViews.setAccessible(true);
				Method method_getInstance = class_WindowManagerGlobal.getMethod("getInstance");
				instance_WindowManagerGlobal = method_getInstance.invoke(null);
			}
			Object views = field_mViews.get(instance_WindowManagerGlobal);
			if (views instanceof List) {
				return (List<View>) views;
			} else if (views instanceof View[]) {
				return Arrays.asList((View[])views);
			}
		} catch (Exception e) {
			CMN.Log(e);
			instance_WindowManagerGlobal = new Exception();
		}
		
		return new ArrayList<>();
	}
	
	public static int indexOf(CharSequence text, char cc, int now) {
		for (int i = now; i < text.length(); i++) {
			if(text.charAt(i)==cc){
				return i;
			}
		}
		return -1;
	}
	
	public static View getViewItemByPath(View obj, int...path) {
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
		return (View)obj;
	}
	
	
	public static void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, int depth, Object[] viewFetcher) {
		int cc = vg.getChildCount();
		View ca;
		boolean longClickable = clicker instanceof View.OnLongClickListener;
		boolean touhable = clicker instanceof View.OnTouchListener;
		if(vg.isClickable()) {
			click(vg, clicker, longClickable, touhable);
		}
		for (int i = 0; i < cc; i++) {
			ca = vg.getChildAt(i);
			//CMN.Log("setOnClickListenersOneDepth", ca, (i+1)+"/"+(cc));
			if(ca instanceof ViewGroup) {
				if(--depth>0) {
					if(ca.isClickable()) {
						click(ca, clicker, longClickable, touhable);
					} else {
						setOnClickListenersOneDepth((ViewGroup) ca, clicker, depth, viewFetcher);
					}
				}
			} else {
				int id = ca.getId();
				if(ca.getId()!=View.NO_ID){
					if(!(ca instanceof EditText) && ca.isEnabled()) {
						click(ca, clicker, longClickable, touhable);
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
	
	public static void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, SparseArray<View> viewFetcher, int depth) {
		int cc = vg.getChildCount();
		View ca;
		boolean longClickable = clicker instanceof View.OnLongClickListener;
		boolean touhable = clicker instanceof View.OnTouchListener;
		if(vg.isClickable()) {
			click(vg, clicker, longClickable, touhable);
		}
		for (int i = 0; i < cc; i++) {
			ca = vg.getChildAt(i);
			//CMN.Log("setOnClickListenersOneDepth", ca, (i+1)+"/"+(cc));
			if(ca instanceof ViewGroup) {
				if(--depth>0) {
					if(ca.isClickable()) {
						click(ca, clicker, longClickable, touhable);
					} else {
						setOnClickListenersOneDepth((ViewGroup) ca, clicker, viewFetcher, depth);
					}
				}
			} else {
				int id = ca.getId();
				if(ca.getId()!=View.NO_ID){
					if(!(ca instanceof EditText) && ca.isEnabled()) {
						click(ca, clicker, longClickable, touhable);
					}
					if(viewFetcher!=null) {
						viewFetcher.put(ca.getId(), ca);
					}
				}
			}
		}
	}
	
	private static void click(View ca, View.OnClickListener clicker, boolean longClickable, boolean touhable) {
		ca.setOnClickListener(clicker);
		if(longClickable&&ca.isLongClickable()) {
			ca.setOnLongClickListener((View.OnLongClickListener) clicker);
		}
		if(touhable) {
			ca.setOnTouchListener((View.OnTouchListener) clicker);
		}
	}
	
	public static void removeView(View viewToRemove) {
		removeIfParentBeOrNotBe(viewToRemove, null, false);
	}
	
	public static boolean removeIfParentBeOrNotBe(View view, ViewGroup parent, boolean tobe) {
		if(view!=null) {
			ViewParent svp = view.getParent();
			if((parent!=svp) ^ tobe) {
				if(svp!=null) {
					((ViewGroup)svp).removeView(view);
					//CMN.Log("removing from...", svp, view.getParent(), view);
					return view.getParent()==null;
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent, int index) {
		if(removeIfParentBeOrNotBe(view2Add, parent, false)) {
			int cc=parent.getChildCount();
			if(index<0) {
				index = cc+index;
				if(index<0) {
					index = 0;
				}
			} else if(index>cc) {
				index = cc;
			}
			parent.addView(view2Add, index);
			return true;
		}
		return false;
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent, View index) {
		return addViewToParent(view2Add, parent, parent.indexOfChild(index)+1);
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent) {
		if(removeIfParentBeOrNotBe(view2Add, parent, false)) {
			parent.addView(view2Add);
			return true;
		}
		return false;
	}
	
	public static void postInvalidateLayout(View view) {
		view.post(view::requestLayout);
	}
	
	static int resourceId=-1;
	public static int getStatusBarHeight(Resources resources) {
		if(resourceId==-1)
			try {
				resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
			} catch (Exception ignored) { }
		if (resourceId != -1) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
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
	
	
	public static void embedViewInCoordinatorLayout(View v, boolean setBehaviour) {
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		if (lp instanceof CoordinatorLayout.LayoutParams) {
			CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) lp;
			params.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
			params.width = -1;
			params.height = -1;
			//params.topMargin = UIData.appbar.getHeight()-TargetTransY;
			//root.setForegroundGravity();
			if (setBehaviour) {
				params.setBehavior(new AppBarLayout.ScrollingViewBehavior(v.getContext(), null));
			} else {
				params.setBehavior(null);
			}
		}
	}
	
	public static String fileToString(Context context, File f) {
		if (f.getPath().startsWith("/ASSET")) {
			String errRinfo = null;
			boolean b1=f.getPath().startsWith("/", 6);
			if(GlobalOptions.debug || b1)
			try {
				InputStream fin = context.getResources().getAssets().open(f.getPath().substring(AssetTag.length()+(!b1?1:0)));
				ReusableByteOutputStream bout = new ReusableByteOutputStream(fin.available());
				byte[] buffer = new byte[4096];
				int read;
				while((read=fin.read(buffer))>0) {
					bout.write(buffer, 0, read);
				}
				return new String(bout.getBytes(), 0, bout.getCount(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				errRinfo = CMN.Log(e);
			}
			try {
				UniversalDictionaryInterface asset = BookPresenter.getBookImpl(context instanceof MainActivityUIBase ?(MainActivityUIBase)context:null, new File(AssetTag+"webx"), 0);
				Objects.requireNonNull(asset);
				int idx = asset.lookUp(""+BookPresenter.hashCode(f.getPath().substring(8), 0));
				CMN.Log("val::", asset.getRecordAt(idx, null, true), f.getPath(), asset.getEntryAt(0), asset.getNumberEntries());
				return asset.getRecordAt(idx, null, true);
			} catch (IOException e) {
				errRinfo = CMN.Log(e);
			}
			return errRinfo;
		}
		return BU.fileToString(f);
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
	
	
	
	public static boolean actualLandscapeMode(Context c) {
		int angle = ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		return angle== Surface.ROTATION_90||angle==Surface.ROTATION_270;
	}
	
	public static String getTextInView(View view) {
		CharSequence ret = ((TextView)view).getText();
		return ret==null?"":ret.toString();
	}
	
	public static String getFieldInView(View view) {
		return ((TextView)view).getText().toString().trim().replaceAll("[\r\n]", "");
	}
	
	public static String getTextInView(View view, int id) {
		return ((TextView)view.findViewById(id)).getText().toString();
	}
	
	public static void setTextInView(View view, CharSequence cs) {
		((TextView)view).setText(cs);
	}
	
	public static View replaceView(View viewToAdd, View viewToRemove) {
		return replaceView(viewToAdd, viewToRemove, true);
	}
	
	public static View replaceView(View viewToAdd, View viewToRemove, boolean layoutParams) {
		ViewGroup.LayoutParams lp = viewToRemove.getLayoutParams();
		ViewGroup vg = (ViewGroup) viewToRemove.getParent();
		if(vg!=null) {
			int idx = vg.indexOfChild(viewToRemove);
			removeView(viewToRemove);
			removeView(viewToAdd);
			if (layoutParams) {
				vg.addView(viewToAdd, idx, lp);
			} else {
				vg.addView(viewToAdd, idx);
			}
		}
		return viewToAdd;
	}
	
	public static Drawable getThemeDrawable(Context context, int attrId) {
		int[] attrs = new int[] { attrId };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		Drawable drawableFromTheme = ta.getDrawable(0);
		ta.recycle();
		return drawableFromTheme;
	}
	
	public static int getViewIndex(View sv) {
		ViewGroup svp = (ViewGroup) sv.getParent();
		if (svp!=null) {
			return svp.indexOfChild(sv);
		}
		return -1;
	}
	
	public static void blinkView(View blinkView, boolean post) {
		Animation anim = new AlphaAnimation(0.1f, 1.0f);
		anim.setDuration(50);
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(2);
		if (post) {
			blinkView.post(() -> blinkView.startAnimation(anim));
		} else {
			blinkView.startAnimation(anim);
		}
	}
	
	public static void preventDefaultTouchEvent(View view, int x, int y) {
		MotionEvent evt = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, x, y, 0);
		if (view!=null) view.dispatchTouchEvent(evt);
		evt.recycle();
	}
	
	public static void performClick(View view, float x, float y) {
		MotionEvent evt = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, x, y, 0);
		view.dispatchTouchEvent(evt);
		evt.setAction(MotionEvent.ACTION_UP);
		view.dispatchTouchEvent(evt);
		evt.recycle();
	}
	
	public static RecyclerView.ViewHolder getViewHolderInParents(View v) {
		ViewParent vp;
		Object tag;
		while(v!=null) {
			if ((tag = v.getTag()) instanceof RecyclerView.ViewHolder) {
				return (RecyclerView.ViewHolder) tag;
			}
			vp = v.getParent();
			v = vp instanceof View?(View) vp:null;
		}
		return null;
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

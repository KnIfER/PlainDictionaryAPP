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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.databinding.ViewDataBinding;
import androidx.preference.Preference;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.RebootActivity;
import com.knziha.plod.plaindict.Toastable_Activity;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.knziha.metaline.StripMethods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import static com.knziha.filepicker.utils.FU.bKindButComplexSdcardAvailable;
import static com.knziha.plod.plaindict.CMN.AssetTag;

public class ViewUtils {
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
	
	
	public static void logAllViews(){
		List<View> views = getWindowManagerViews();
		CMN.debug("logAllViews::", views);
		for(View vI:views){
			CMN.Log("\n\n\n\n\nlogAllViews::  "+vI);
			CMN.recurseLog(vI);
		}
	}
	/* get the list from WindowManagerGlobal.mViews */
	public static List<View> getWindowManagerViews() {
		try {
			//  Class.forName("android.view.WindowManagerGlobal")
			//  ------>mViews
			//  ------>getInstance
			Object views = execSimple("{android.view.WindowManagerGlobal}.getInstance().mViews", reflectionPool);
			//CMN.debug("logAllViews::views::", views);
			if (views instanceof List) {
				return (List<View>) views;
			} else if (views instanceof View[]) {
				return Arrays.asList((View[])views);
			}
		} catch (Exception e) {
			CMN.debug("logAllViews::", e);
			//instance_WindowManagerGlobal = new Exception();
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
	
	public static InputStream fileToStream(Context context, File f) {
		if (f.getPath().startsWith("/ASSET")) {
			String errRinfo;
			boolean b1=f.getPath().startsWith("/", 6);
			if(GlobalOptions.debug || b1)
			try {
				return context.getResources().getAssets().open(f.getPath().substring(AssetTag.length()+(!b1?1:0)));
			} catch (IOException e) {
				errRinfo = CMN.Log(e);
			}
			try {
				UniversalDictionaryInterface asset = BookPresenter.getBookImpl(context instanceof MainActivityUIBase ?(MainActivityUIBase)context:null, new File(AssetTag+"webx"), 0);
				Objects.requireNonNull(asset);
				int idx = asset.lookUp(""+BookPresenter.hashCode(f.getPath().substring(8), 0));
				CMN.Log("val::", asset.getRecordAt(idx, null, true), f.getPath(), asset.getEntryAt(0), asset.getNumberEntries());
				return new ByteArrayInputStream(asset.getRecordData(idx));
			} catch (IOException e) {
				errRinfo = CMN.Log(e);
			}
			return new ByteArrayInputStream(errRinfo.getBytes());
		}
		return BU.fileToStream(f);
	}
	
	// todo 缓存
	public static WebResourceResponse KikLetToVar(String url, String accept, String refer, String origin,
												  WebResourceRequest request, PlainWeb webx) throws Exception {
		OutputStream[] rev = new OutputStream[]{null};
		ViewUtils.downloadToStream(url, rev, null
				, accept, refer, origin, request, webx);
		if (rev[0]!=null) {
			String ret = rev[0].toString().replaceAll("\\blet\\b", "var");
			//CMN.Log("KikLetToVar", ret);
			return new WebResourceResponse("text/js", "UTF-8", new ByteArrayInputStream(ret.getBytes()));
		}
		return null;
	}
	
	public static void downloadToStream(String url
			, OutputStream[] outputStreams
			, String path, String accept, String refer, String origin,
										WebResourceRequest request, PlainWeb webx) throws Exception {
		URL requestURL = new URL(url);
		OutputStream fout = outputStreams[0];
		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[]{new PlainWeb.MyX509TrustManager()}, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
		} catch (Exception ignored) { }
		HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setConnectTimeout(10000);
		if(accept!=null) urlConnection.setRequestProperty("Accept",accept);
		if(refer!=null) urlConnection.setRequestProperty("Refer", refer);
		if(origin!=null) urlConnection.setRequestProperty("Origin", origin);
		if(request!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			Map<String, String> headers = request.getRequestHeaders();
			urlConnection.setRequestProperty("X-Requested-With", headers.get("X-Requested-With"));
			urlConnection.setRequestProperty("Content-Type", headers.get("Content-Type"));
			urlConnection.setRequestMethod(request.getMethod());
		}
		urlConnection.setRequestProperty("Charset", "UTF-8");
		urlConnection.setRequestProperty("Connection", "Keep-Alive");
		urlConnection.setRequestProperty("User-Agent", webx.computerFace?"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
				:"Mozilla/5.0 (Linux; Android 9; VTR-AL00 Build/HUAWEIVTR-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36");
		urlConnection.connect();
		InputStream is = urlConnection.getInputStream();
		byte[] buffer = new byte[4096];
		int len;
		while ((len = is.read(buffer)) > 0) {
			if (fout == null)  {
				fout = path==null?new ByteArrayOutputStream():new FileOutputStream(path);
				outputStreams[0] = fout;
			}
			fout.write(buffer, 0, len);
		}
		if(fout!=null) {
			fout.flush();
			fout.close();
		}
		urlConnection.disconnect();
		is.close();
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
	
	public static HashMap<String, Object> reflectionPool = new HashMap<>();
	
	//View.class.getDeclaredField("mScrollCache");
	//Class.forName("android.view.View$ScrollabilityCache").getDeclaredField("scrollBar")
	
	public static void setListViewScrollbarColor(View mListView, boolean red) {
		try {
			Drawable ScrollbarDrawable = (Drawable) execSimple("$.mScrollCache.scrollBar", reflectionPool, mListView);
			//CMN.debug("setListViewScrollbarColor::", ScrollbarDrawable, mListView);
			ScrollbarDrawable.setColorFilter(red?RED:GREY);
		} catch (Exception e) {
			CMN.debug("setListViewScrollbarColor::", e);
		}
	}
	
	static ColorFilter RED = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
	static ColorFilter GREY = new PorterDuffColorFilter(0x8a666666, PorterDuff.Mode.SRC_IN);
	
	
	//Class.forName("android.widget.FastScroller");
	//--->getDeclaredField("mTrackDrawable");
	//--->getDeclaredField("mThumbImage");
	//AbsListView.class.getDeclaredField("mFastScroll");
	
	public static void setListViewFastColor(View...mListViews) {
		try {
			for(View mListView:mListViews) {
				// https://github1s.com/aosp-mirror/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/AbsListView.java#L584
				String eval = "$.mFastScroll.mThumbImage";
				if (Build.VERSION.SDK_INT<21) {
					eval = eval.substring(0,13)+"er"+eval.substring(13);
				}
				ImageView ThumbImage = (ImageView) execSimple(eval, reflectionPool, mListView);
				//CMN.debug("setListViewFastColor::", ThumbImage);
				if (ThumbImage != null) ThumbImage.setColorFilter(GREY);
			}
		} catch (Exception e) {
			CMN.debug("setListViewFastColor::", e);
		}
	}
	
	public static void listViewStrictScroll(boolean IsStrictSCroll, ListViewmy...mListViews) {
		try {
			for(ListViewmy mListView:mListViews) {
				String eval = "$.mFastScroll.mTrackDrawable";
				if(IsStrictSCroll) {
					if (mListView.FastScroller==null) {
						mListView.FastScroller = execSimple(eval, reflectionPool, mListView);
					}
					execSimple(eval+"=n", reflectionPool, mListView);
				} else {
					execSimple(eval+"=$1", reflectionPool, mListView, mListView.FastScroller);
				}
			}
		} catch (Exception e) {
			CMN.debug("listViewStrictScroll::", e);
		}
	}
	
	@StripMethods(strip = !BuildConfig.isDebug)
	public static void setWebDebug(Toastable_Activity a) {
		WebView.setWebContentsDebuggingEnabled(true);
		a.showT("调试网页！");
	}
	
	public static class ViewDataHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder{
		public T data;
		public long position;
		public Object tag;
		public ViewDataHolder(T data){
			super(data.getRoot());
			itemView.setTag(this);
			this.data = data;
		}
	}
	
	/**
	 * Get center child in X Axes
	 */
	public static View getCenterXChild(RecyclerView recyclerView) {
		int childCount = recyclerView.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View child = recyclerView.getChildAt(i);
				if (isChildInCenterX(recyclerView, child)) {
					return child;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get position of center child in X Axes
	 */
	public static int getCenterXChildPosition(RecyclerView recyclerView) {
		int childCount = recyclerView.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View child = recyclerView.getChildAt(i);
				if (isChildInCenterX(recyclerView, child)) {
					return recyclerView.getChildAdapterPosition(child);
				}
			}
		}
		return ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
	}
	
	/**
	 * Get center child in Y Axes
	 */
	public static View getCenterYChild(RecyclerView recyclerView) {
		int childCount = recyclerView.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View child = recyclerView.getChildAt(i);
				if (isChildInCenterY(recyclerView, child)) {
					return child;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get position of center child in Y Axes
	 */
	public static int getCenterYChildPosition(RecyclerView recyclerView) {
		int childCount = recyclerView.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View child = recyclerView.getChildAt(i);
				if (isChildInCenterY(recyclerView, child)) {
					return recyclerView.getChildAdapterPosition(child);
				}
			}
		}
		return childCount;
	}
	
	public static boolean isChildInCenterX(RecyclerView recyclerView, View view) {
		int childCount = recyclerView.getChildCount();
		int[] lvLocationOnScreen = new int[2];
		int[] vLocationOnScreen = new int[2];
		recyclerView.getLocationOnScreen(lvLocationOnScreen);
		int middleX = lvLocationOnScreen[0] + recyclerView.getWidth() / 2;
		if (childCount > 0) {
			view.getLocationOnScreen(vLocationOnScreen);
			if (vLocationOnScreen[0] <= middleX && vLocationOnScreen[0] + view.getWidth() >= middleX) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isChildInCenterY(RecyclerView recyclerView, View view) {
		int childCount = recyclerView.getChildCount();
		int[] lvLocationOnScreen = new int[2];
		int[] vLocationOnScreen = new int[2];
		recyclerView.getLocationOnScreen(lvLocationOnScreen);
		int middleY = lvLocationOnScreen[1] + recyclerView.getHeight() / 2;
		if (childCount > 0) {
			view.getLocationOnScreen(vLocationOnScreen);
			if (vLocationOnScreen[1] <= middleY && vLocationOnScreen[1] + view.getHeight() >= middleY) {
				return true;
			}
		}
		return false;
	}
	
	public static void notifyAPPSettingsChanged(Activity activity, Preference preference) {
		if (activity instanceof APPSettingsActivity) {
			((APPSettingsActivity) activity).notifyChanged(preference);
		}
	}
	
	public static void notifyDataSetChanged(ListAdapter adapter) {
		if (adapter instanceof BaseAdapter) {
			((BaseAdapter) adapter).notifyDataSetChanged();
		} else if (adapter instanceof WrapperListAdapter) {
			notifyDataSetChanged(((WrapperListAdapter) adapter).getWrappedAdapter());
		}
	}
	
	public static void addOnLayoutChangeListener(View view, View.OnLayoutChangeListener layoutChangeListener) {
		if (layoutChangeListener!=null)
			view.addOnLayoutChangeListener(layoutChangeListener);
	}
	
	public static Field getField(Class<?> aClass, String name) throws Exception {
//		CMN.debug("getField::"+aClass+"->"+name);
		if (aClass==ObjectUtils.NULL.getClass()) {
			return null;
		}
		Field ret=null;
		try {
			ret = aClass.getDeclaredField(name);
			ret.setAccessible(true);
		} catch (NoSuchFieldException e) {
			try {
				ret = aClass.getField(name);
			} catch (NoSuchFieldException ex) {
				while((aClass=aClass.getSuperclass())!=null) {
					try {
						ret = aClass.getDeclaredField(name);
						break;
					} catch (NoSuchFieldException ignored) { }
				}
			}
		}
		Objects.requireNonNull(ret);
		ret.setAccessible(true);
		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(ret, ret.getModifiers() & ~Modifier.FINAL);
		} catch (Exception ignored) { }
		return ret;
	}
	
	public static Method getMethod(Class<?> aClass, String name, Class<?>[] types) throws Exception {
		if (aClass==ObjectUtils.NULL.getClass()) {
			return null;
		}
		Method ret;
		try {
			ret = aClass.getMethod(name, types);
		} catch (NoSuchMethodException e) {
			ret = aClass.getDeclaredMethod(name, types);
			ret.setAccessible(true);
		}
		return Objects.requireNonNull(ret);
	}
	
	public static Object execSimple(String simplet, HashMap<String, Object> reflectionPool, Object...vars) throws Exception {
		StringTokenizer array = new StringTokenizer(simplet, ";\r\n");
		HashMap<String, Object> variables = new HashMap<>();
		int st=0;
		for (int i = st; i < vars.length-0; i++) {
			if(i==st && vars[i] instanceof String && vars[i].toString().endsWith("::")) {
				st++; continue;
			}
			//if(vars[i]==simplet) break;
			variables.put("$"+(i==st?"":i), vars[i+0]);
		}
		Object ret = null;
		while(array.hasMoreTokens()){
			String ln = array.nextToken();
//			CMN.debug("ln::"+ln);
			int eqIdx = ln.indexOf('=');
			if (eqIdx>0) {
				Class sClazz = null;
				Class zClazz = null;
				Object object = null;
				Object newObj = null;
				String varName=null;
				String valName=null;
				Field fieldToSet=null;
				varName = ln.substring(0, eqIdx);
				// 左值
				if (varName.startsWith("var")) {
					varName = varName.substring(4);
				} else {
					if (varName.contains(".")) {
						if (varName.startsWith("{")) {
							int idx=varName.lastIndexOf("}");
							sClazz = Class.forName(varName.substring(1, idx));
							varName = varName.substring(idx+1);
							String[] expsLeft = ("ex"+varName).trim().split("\\.(?![0-9])");
							fieldToSet = (Field) evalFieldMethod(sClazz, object, expsLeft, variables, reflectionPool);
						} else {
							int idx=varName.lastIndexOf(".");
							String[] expsLeft = varName.substring(0, idx).split("\\.(?![0-9])");
							object = evalFieldMethod(sClazz, object, expsLeft, variables, reflectionPool);
							expsLeft = ("ex"+varName.substring(idx)).split("\\.(?![0-9])");
							fieldToSet = (Field) evalFieldMethod(null, object, expsLeft, variables, reflectionPool);
						}
						varName = null;
					} else { // 取出局部变量缓存值，作为左值
						object = variables.get(varName);
					}
				}
				// 左值
				// eq[0] = eq[1]
				//         ...exps
				// 右值
				valName = ln.substring(eqIdx+1);
//				CMN.debug("右值::"+valName);
				String[] expsRight=null;
				if (valName.startsWith("{")) {
					int idx=valName.lastIndexOf("}");
					zClazz = Class.forName(valName.substring(1, idx));
					if (idx+1>=valName.length()) {
						newObj = zClazz; // 直接变成类
					} else {
						expsRight = valName.substring(idx).split("\\.(?![0-9])");
					}
				} else if (valName.startsWith("'")) {
					newObj = valName.substring(1, valName.length()-1);
				}  else {
					expsRight = valName.split("\\.(?![0-9])");
					valName = expsRight[0];
					newObj = variables.get(valName);
				}
				if (newObj==null || expsRight!=null && expsRight.length>1) {
					newObj = evalFieldMethod(zClazz, newObj, expsRight, variables, reflectionPool);
				}
				ret = newObj;
				// 右值
				// 赋值
				if (fieldToSet!=null) {
//					CMN.debug("field赋值::", object, fieldToSet, newObj);
					fieldToSet.set(object, newObj);
				}
				if (varName!=null) {
//					CMN.debug("var赋值::", varName, newObj);
					variables.put(varName, newObj);
				}
			}
			else if(ln.length()>2){
				if (ln.startsWith("{")) {
					int idx=ln.indexOf("}");
					Class<?> sClazz = Class.forName(ln.substring(1, idx));
					ln = ln.substring(idx+1);
					String[] expsLeft = (ln).trim().split("\\.(?![0-9])");
					ret = evalFieldMethod(sClazz, null, expsLeft, variables, reflectionPool);
				} else {
					String[] exps = ln.split("\\.(?![0-9])");
					ret = evalFieldMethod(null, null, exps, variables, reflectionPool);
				}
			}
		}
		return ret;
	}
	
	final static HashMap<String, Class> typeHash = new HashMap<>();
	
	//	public static Object evalSimple(Object object, String exps) {
//		try {
//			return evalFieldMethod(null, object, exps.split("\\."));
//		} catch (Exception e) {
//			return e;
//		}
//	}
	public static Object evalFieldMethod(Class zClazz, Object object
			, String[] exps, HashMap<String, Object> variables, HashMap<String, Object> reflectionPool) throws Exception {
//		CMN.debug("evalFieldMethod::", zClazz, object, "exp::"+Arrays.toString(exps), variables);
		boolean extract=exps.length>1&& "ex".equals(exps[0]);
		Object fMd = null;
		if (typeHash.size()==0) {
			// byte、short、int、long、float、double
			//typeHash.put("byte", byte.class);  typeHash.put("Byte", Byte.class);
			//typeHash.put("double", double.class);  typeHash.put("Double", Double.class);
			//typeHash.put("float", float.class);  typeHash.put("Float", Float.class);
			//typeHash.put("long", long.class); typeHash.put("Long", Long.class);
			typeHash.put("int", int.class); typeHash.put("Int", Integer.class);
			//typeHash.put("short", short.class);  typeHash.put("Short", Short.class);
			typeHash.put("String", String.class);
		}
		if (zClazz==null && object!=null) {
			zClazz = object instanceof Class?(Class) object :object.getClass();
		}
		Object[] parameters; Class[] methodTypes;
		for (int i = (object==null&&zClazz==null)?0:1; i < exps.length; i++) {
			String fdMd=exps[i];
			parameters = ArrayUtils.EMPTY_OBJECT_ARRAY;
			methodTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
			if (i==0 && variables!=null) {
				//CMN.debug("init extraction from variables::"+fdMd, zClazz);
				object = variables.get(fdMd);
				zClazz = object==null? ObjectUtils.Null.class:
						object instanceof Class?(Class) object :object.getClass();
				continue;
			}
			int zh = fdMd.indexOf("[");
			int xi = fdMd.indexOf("(");
			int which=-1;
			if(xi>0) {
				String[] arr;
				int where=xi;
				if(zh>0) {
					if (zh<xi) {
						where = zh;
					} else {
						which = IU.parsint(fdMd.substring(zh+1, fdMd.indexOf("]", zh)), 0);
					}
				}
				String methodName = fdMd.substring(0, where);
				fMd = null;
				String storeKey=null;
				if (zClazz!=null && reflectionPool!=null) {
					storeKey = zClazz.hashCode()+methodName;
					fMd = reflectionPool.get(storeKey);
				}
				if (fMd==null) {
					if (where!=xi) {
						String types = fdMd.substring(zh+1, fdMd.indexOf("]", zh));
						arr=null;
						if (types.contains(",")) {
							arr = types.split(",");
						} else if(types.length()>0) {
							arr = new String[]{types};
						}
						if (arr!=null) {
							methodTypes = new Class[arr.length];
							for (int j = 0; j < arr.length; j++) { // ..分析参数类型
								String typeName = arr[j].trim();
								//CMN.debug("typeName::", typeName);
								Class type = typeHash.get(typeName);
								if (type==null) {
									if (!typeName.contains(".")) {
									
									} else {
										type = Class.forName(typeName);
									}
								}
								methodTypes[j] = type;
							}
						}
					}
				} //else CMN.Log("复用反射::"+storeKey);
				//if (methodTypes.length>0) {
				String params = fdMd.substring(xi+1, fdMd.indexOf(")", xi));
				arr=null;
				if (params.contains(",")) {
					arr = params.split(",");
				} else if(params.length()>0) {
					arr = new String[]{params};
				}
				if (arr!=null) {
					parameters = new Object[arr.length];
					if(methodTypes.length==0) methodTypes = new Class[arr.length];
					for (int j = 0; j < arr.length; j++) { // ..分析参数
						String paramName = arr[j].trim();
//						CMN.debug("paramName::", paramName);
						try {
							//CMN.debug("paramName::", paramName, methodTypes[j]==null?null:methodTypes[j].getSimpleName());
							if (paramName.startsWith("'")) {
								parameters[j] = paramName.substring(1, paramName.length()-1);
							} else if (paramName.startsWith("0x")) {
								parameters[j] = IU.parsint(paramName);
							} else {
								Integer.parseInt(paramName.substring(0, 1));
								if (methodTypes[j]!=null) {
									char c =  methodTypes[j].getSimpleName().toUpperCase().charAt(0);
									if(c=='B') parameters[j] = Boolean.valueOf(paramName);
									else if(c=='D') parameters[j] = Double.valueOf(paramName);
									else if(c=='F') parameters[j] = Float.valueOf(paramName);
									else if(c=='L') parameters[j] = Long.valueOf(paramName);
									else if(c=='I') parameters[j] = IU.parsint(paramName);
									else if(c=='S') parameters[j] = Short.parseShort(paramName);
								} else {
									parameters[j] = IU.parsint(paramName);
								}
							}
						} catch (NumberFormatException e) { // ...objHash
							//e.printStackTrace();
							if (variables!=null) {
								parameters[j] = variables.get(paramName);
							}
						}
						if (parameters[j]!=null && methodTypes[j]==null) {
							methodTypes[j] = parameters[j].getClass();
						}
					}
				}
				//}
//				CMN.debug("methodTypes::", methodTypes.length, Arrays.toString(methodTypes));
//				CMN.debug("parameters::", parameters.length, Arrays.toString(parameters));
//				CMN.debug("fdMd::", zClazz, methodName);
				if (fMd==null) {
					fMd = getMethod(zClazz, methodName, methodTypes);
					if (reflectionPool!=null && fMd!=null) reflectionPool.put(storeKey, fMd);
				}
				if(fMd==null) return null;
				//Objects.requireNonNull(fMd);
				object = ((Method)fMd).invoke(object, parameters);
//				CMN.debug("result::", object);
			}
			else {
				if(zh>0) {
					which = IU.parsint(fdMd.substring(zh+1, fdMd.indexOf("]", zh)), 0);
					fdMd = fdMd.substring(0, zh);
				}
//				CMN.debug("getField::", zClazz, object, fdMd);
				fMd = null;
				if (zClazz.isArray()) {
					object = Array.getLength(object);
				} else {
					String storeKey=null;
					if (reflectionPool!=null) {
						storeKey = zClazz.hashCode()+fdMd;
						fMd = reflectionPool.get(storeKey);
					}
					if (fMd==null) {
						fMd = getField(zClazz, fdMd);
						if (reflectionPool!=null && fMd!=null) reflectionPool.put(storeKey, fMd);
					} //else CMN.Log("复用反射::"+storeKey);
					if(fMd==null) return null;
					Objects.requireNonNull(fMd);
					object = ((Field)fMd).get(object);
				}
			}
			if (which>=0) { // 是数组
				try {
					object = ((Object[])object)[which];
				} catch (Exception e) {
					object = null;
				}
			}
			zClazz = object==null?ObjectUtils.Null.class:object.getClass();
		}
		return extract?fMd:object;
	}
	
	
}

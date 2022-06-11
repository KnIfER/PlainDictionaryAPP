package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.PlainUI.FloatApp;
import com.knziha.plod.plaindict.CMN;

public class WindowLayout extends FrameLayout {
	public FloatApp floatApp;
	public boolean resizing;
	
	/** 边框四个边的坐标，相对于屏幕左上角，每边只保留x, y中有效的一个坐标: <br> left (x), top (y), right (x), bottom (y) */
	public final RectF frameOffsets = new RectF();
	
	/** 用户按下的点
	 * 点阵示意： <br>
	 * 0   1 <br>
	 * 2   3  <br>
	 * <b>或者</b>用户按下的边 4=left, 5=top, 6=right, 7=bottom*/
	int activePntBdr = -1;
	
	float orgX, orgY, lastX, lastY;
	
	private float theta;
	
	public WindowLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		theta = GlobalOptions.density * 9;
	}
	
	
	private boolean checkOffset(float tmpValue, float deviation, float maxValue) {
		return deviation > GlobalOptions.density*3 *2 && tmpValue>0 && tmpValue < maxValue && deviation<maxValue;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getActionMasked()==MotionEvent.ACTION_DOWN) {
			float x = ev.getX();
			float y = ev.getY();
			float pad = theta*3;
			if (x<pad || y<pad || getWidth()-x<pad || getHeight()-y<pad)
			{
				int[] loc = new int[2];
				floatApp.view.getLocationOnScreen(loc);
				frameOffsets.left = loc[0];
				frameOffsets.top = loc[1];
				CMN.statusBarHeight = frameOffsets.top - floatApp.lp.y;
				frameOffsets.right = frameOffsets.left+getWidth();
				frameOffsets.bottom = frameOffsets.top+getHeight();
				x = ev.getRawX();
				y = ev.getRawY();
				activePntBdr = getActivePntBdr(x, y);
				CMN.Log("activePntBdr::", activePntBdr);
				resizing = activePntBdr>=0;
				orgX = lastX = x;
				orgY = lastY = y;
				//return resizing;
				if (resizing) {
					getChildAt(1).setVisibility(View.VISIBLE);
				}
			}
		}
		if (resizing) {
			if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
				CMN.Log("ACTION_MOVE::activePntBdr::", activePntBdr);
				float x = ev.getRawX();
				float y = ev.getRawY();
				float offsetX = x - lastX;
				float offsetY = y - lastY;
				lastX = x;
				lastY = y;
				float tmpX, tmpY;
				switch (activePntBdr) {
					// 0-3 : 角缩放状态下, 按住某一个点，该点的坐标改变，其他2个点坐标跟着改变，对点坐标不变
					case 0:
						tmpX = frameOffsets.left + offsetX;
						tmpY = frameOffsets.top + offsetY;
						if (checkOffset(tmpX, frameOffsets.right - tmpX, floatApp.a.dm.widthPixels))
							frameOffsets.left = tmpX;
						if (checkOffset(tmpY, frameOffsets.bottom - tmpY, floatApp.a.dm.heightPixels))
							frameOffsets.top = tmpY;
						break;
					case 1:
						tmpX = frameOffsets.right + offsetX;
						tmpY = frameOffsets.top + offsetY;
						if (checkOffset(tmpX, tmpX - frameOffsets.left, floatApp.a.dm.widthPixels))
							frameOffsets.right = tmpX;
						if (checkOffset(tmpY, frameOffsets.bottom - tmpY, floatApp.a.dm.heightPixels))
							frameOffsets.top = tmpY;
						break;
					case 2:
						tmpX = frameOffsets.left + offsetX;
						tmpY = frameOffsets.bottom + offsetY;
						if (checkOffset(tmpX, frameOffsets.right - tmpX, floatApp.a.dm.widthPixels))
							frameOffsets.left = tmpX;
						if (checkOffset(tmpY, tmpY - frameOffsets.top, floatApp.a.dm.heightPixels))
							frameOffsets.bottom = tmpY;
						break;
					case 3:
						tmpX = frameOffsets.right + offsetX;
						tmpY = frameOffsets.bottom + offsetY;
						if (checkOffset(tmpX, tmpX - frameOffsets.left, floatApp.a.dm.widthPixels))
							frameOffsets.right = tmpX;
						if (checkOffset(tmpY, tmpY - frameOffsets.top, floatApp.a.dm.heightPixels))
							frameOffsets.bottom = tmpY;
						break;
					// 4-7 拖动哪一条边
					case 4: {
						tmpX = frameOffsets.left + offsetX;
						if (checkOffset(tmpX, frameOffsets.right - tmpX, floatApp.a.dm.widthPixels))
							frameOffsets.left = tmpX;
					}
					break;
					case 5: {
						tmpY = frameOffsets.top + offsetY;
						if (checkOffset(tmpY, frameOffsets.bottom - tmpY, floatApp.a.dm.heightPixels))
							frameOffsets.top = tmpY;
					}
					break;
					case 6: {
						tmpX = frameOffsets.right + offsetX;
						if (checkOffset(tmpX, tmpX - frameOffsets.left, floatApp.a.dm.widthPixels))
							frameOffsets.right = tmpX;
					}
					break;
					case 7: {
						tmpY = frameOffsets.bottom + offsetY;
						if (checkOffset(tmpY, tmpY - frameOffsets.top, floatApp.a.dm.heightPixels))
							frameOffsets.bottom = tmpY;
					}
					break;
				}
				floatApp.updateView(frameOffsets);
			}
			if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
				resizing = false;
				getChildAt(1).setVisibility(View.INVISIBLE);
			}
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}
	
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		return super.onInterceptTouchEvent(ev);
//	}
	
	
	private double dist(float x, float y) {
		return Math.sqrt(x*x+y*y);
	}
	
	/**
	 * 判断按下的点在圆圈内
	 * 点阵示意： <br>
	 * 0   1 <br>
	 * 2   3 <br>
	 * <br> <b>或者</b>检查是否在操作某条边
	 * <br> see {@link #activePntBdr}
	 * @param x 按下的X坐标
	 * @param y 按下的Y坐标
	 * @return 返回按到的是哪个角点或边, 没有则返回-1
	 */
	private int getActivePntBdr(float x, float y) {
		// 角
		float theta = this.theta * 1.8f;
		//if(theta >= dist(x- frameOffsets.left, y- frameOffsets.top)) {
		if(x - frameOffsets.left <= theta && y - frameOffsets.top <= theta) {
			return 0;
		}
		//if(theta >= dist(x- frameOffsets.right, y- frameOffsets.top)) {
		if(frameOffsets.right - x <= theta && y - frameOffsets.top <= theta) {
			return 1;
		}
		//if(theta >= dist(x- frameOffsets.left, y- frameOffsets.bottom)) {
		if(x - frameOffsets.left <= theta && frameOffsets.bottom - y <= theta) {
			return 2;
		}
		//if(theta >= dist(x- frameOffsets.right, y- frameOffsets.bottom)) {
		if(frameOffsets.right - x <= theta && frameOffsets.bottom - y <= theta) {
			return 3;
		}
		theta = this.theta * 1.5f;
		// 边
		{
			if(theta  >= (y-frameOffsets.top))
				return 1+4;
			else if(theta  >= (frameOffsets.bottom-y))
				return 3+4;
		}
		{
			if(theta >= (x-frameOffsets.left))
				return 0+4;
			else if(theta >= (frameOffsets.right-x))
				return 2+4;
		}
		return -1;
	}
	
}

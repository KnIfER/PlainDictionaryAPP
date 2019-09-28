package com.knziha.plod.widgets;
import android.content.Context;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
public class CustomDrawerLayout extends DrawerLayout {
 public CustomDrawerLayout(Context context) {
  this(context, null);
 }
 public CustomDrawerLayout(Context context, AttributeSet attrs) {
  this(context, attrs, 0);
 }
 public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
  super(context, attrs, defStyle);
  final ViewConfiguration configuration = ViewConfiguration
    .get(getContext());
  mTouchSlop = configuration.getScaledTouchSlop();
 }
 private int mTouchSlop;
 private float mLastMotionX;
 private float mLastMotionY;
 
 @Override
 public boolean onInterceptTouchEvent(MotionEvent ev) {
  try {
   switch (ev.getAction()) {
   case MotionEvent.ACTION_MOVE:
        if (ev.getRawX()<=100) {
            return true;
           } else {
            return false;
           }
    
   default:
    break;
   }
   return super.onInterceptTouchEvent(ev);
  } catch (IllegalArgumentException ex) {
  }
  return false;
 }
 @Override
 public boolean onTouchEvent(MotionEvent ev) {
  try {
   return super.onTouchEvent(ev);
  } catch (IllegalArgumentException ex) {
  }
  return false;
 }
}
 
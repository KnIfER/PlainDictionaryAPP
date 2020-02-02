/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.EdgeEffectCompat;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

/**
 * NestedScrollView is just like {@link android.widget.ScrollView}, but it supports acting
 * as both a nested scrolling parent and child on both new and old versions of Android.
 * Nested scrolling is enabled by default.
 */
public class AdvancedNestScrollWebView1 extends WebViewmy implements NestedScrollingParent3,
		NestedScrollingChild3 {

	private static final String TAG = "NestedScrollView";
	private int OrgTop;
	private OverScroller mScroller;
	private EdgeEffect mEdgeGlowTop;
	private EdgeEffect mEdgeGlowBottom;

	/**
	 * Position of the last motion event.
	 */
	private int mLastMotionY;
	private int mLastMotionX;


	/**
	 * True if the user is currently dragging this ScrollView around. This is
	 * not the same as 'is being flinged', which can be checked by
	 * mScroller.isFinished() (flinging begins when the user lifts his finger).
	 */
	private boolean mIsBeingDragged = false;

	/**
	 * Determines speed during touch scrolling
	 */
	private VelocityTracker mVelocityTracker;

	private int mTouchSlop;
	private int mMinimumVelocity;
	private int mMaximumVelocity;

	/**
	 * ID of the active pointer. This is used to retain consistency during
	 * drags/flings if multiple pointers are used.
	 */
	private int mActivePointerId = INVALID_POINTER;

	/**
	 * Used during scrolling to retrieve the new offset within the window.
	 */
	private final int[] mScrollOffset = new int[2];
	private final int[] mScrollConsumed = new int[2];
	private int mNestedYOffset;

	private int mLastScrollerY;

	/**
	 * Sentinel value for no current active pointer.
	 * Used by {@link #mActivePointerId}.
	 */
	private static final int INVALID_POINTER = -1;

	private final NestedScrollingParentHelper mParentHelper;
	private final NestedScrollingChildHelper mChildHelper;

	private float mVerticalScrollFactor;
	private boolean ruined;

	public AdvancedNestScrollWebView1(@NonNull Context context) {
		this(context, null);
	}

	public AdvancedNestScrollWebView1(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AdvancedNestScrollWebView1(@NonNull Context context, @Nullable AttributeSet attrs,
									  int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initScrollView();

		mParentHelper = new NestedScrollingParentHelper(this);
		mChildHelper = new NestedScrollingChildHelper(this);

		// ...because why else would you be using this widget?
		setNestedScrollingEnabled(true);

		setOnLongClickListener(lc);
	}

	// NestedScrollingChild3

	@Override
	public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
									 int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {
		mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
				offsetInWindow, type, consumed);
	}

	// NestedScrollingChild2

	@Override
	public boolean startNestedScroll(int axes, int type) {
		return mChildHelper.startNestedScroll(axes, type);
	}

	@Override
	public void stopNestedScroll(int type) {
		mChildHelper.stopNestedScroll(type);
	}

	@Override
	public boolean hasNestedScrollingParent(int type) {
		return mChildHelper.hasNestedScrollingParent(type);
	}

	@Override
	public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
										int dyUnconsumed, int[] offsetInWindow, int type) {
		return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
				offsetInWindow, type);
	}

	@Override
	public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow,
										   int type) {
		return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
	}

	// NestedScrollingChild

	@Override
	public void setNestedScrollingEnabled(boolean enabled) {
		mChildHelper.setNestedScrollingEnabled(enabled);
	}

	@Override
	public boolean isNestedScrollingEnabled() {
		return mChildHelper.isNestedScrollingEnabled();
	}

	@Override
	public boolean startNestedScroll(int axes) {
		return startNestedScroll(axes, ViewCompat.TYPE_TOUCH);
	}

	@Override
	public void stopNestedScroll() {
		stopNestedScroll(ViewCompat.TYPE_TOUCH);
	}

	@Override
	public boolean hasNestedScrollingParent() {
		return hasNestedScrollingParent(ViewCompat.TYPE_TOUCH);
	}

	@Override
	public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
										int dyUnconsumed, int[] offsetInWindow) {
		return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
				offsetInWindow);
	}

	@Override
	public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
		return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH);
	}

	@Override
	public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
		return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
	}

	@Override
	public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
		return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
	}

	// NestedScrollingParent3

	@Override
	public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
							   int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
		onNestedScrollInternal(dyUnconsumed, type, consumed);
	}

	private void onNestedScrollInternal(int dyUnconsumed, int type, @Nullable int[] consumed) {
		final int oldScrollY = getScrollY();
		scrollBy(0, dyUnconsumed);
		final int myConsumed = getScrollY() - oldScrollY;

		if (consumed != null) {
			consumed[1] += myConsumed;
		}
		final int myUnconsumed = dyUnconsumed - myConsumed;

		mChildHelper.dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null, type, consumed);
	}

	// NestedScrollingParent2

	@Override
	public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes,
									   int type) {
		return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
	}

	@Override
	public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes,
									   int type) {
		mParentHelper.onNestedScrollAccepted(child, target, axes, type);
		startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type);
	}

	@Override
	public void onStopNestedScroll(@NonNull View target, int type) {
		mParentHelper.onStopNestedScroll(target, type);
		stopNestedScroll(type);
	}

	@Override
	public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
							   int dxUnconsumed, int dyUnconsumed, int type) {
		onNestedScrollInternal(dyUnconsumed, type, null);
	}

	@Override
	public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed,
								  int type) {
		dispatchNestedPreScroll(dx, dy, consumed, null, type);
	}

	// NestedScrollingParent

	@Override
	public boolean onStartNestedScroll(
			@NonNull View child, @NonNull View target, int nestedScrollAxes) {
		return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
	}

	@Override
	public void onNestedScrollAccepted(
			@NonNull View child, @NonNull View target, int nestedScrollAxes) {
		onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
	}

	@Override
	public void onStopNestedScroll(@NonNull View target) {
		onStopNestedScroll(target, ViewCompat.TYPE_TOUCH);
	}

	@Override
	public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
							   int dxUnconsumed, int dyUnconsumed) {
		onNestedScrollInternal(dyUnconsumed, ViewCompat.TYPE_TOUCH, null);
	}

	@Override
	public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
		onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
	}

	@Override
	public boolean onNestedFling(
			@NonNull View target, float velocityX, float velocityY, boolean consumed) {
		if (!consumed) {
			dispatchNestedFling(0, velocityY, true);
			fling((int) velocityY);
			return true;
		}
		return false;
	}

	@Override
	public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
		return dispatchNestedPreFling(velocityX, velocityY);
	}

	@Override
	public int getNestedScrollAxes() {
		return mParentHelper.getNestedScrollAxes();
	}

	// ScrollView import

	@Override
	protected float getTopFadingEdgeStrength() {
		final int length = getVerticalFadingEdgeLength();
		final int scrollY = getScrollY();
		if (scrollY < length) {
			return scrollY / (float) length;
		}

		return 1.0f;
	}

	@Override
	protected float getBottomFadingEdgeStrength() {
		return 0.0f;
	}


	private void initScrollView() {
		mScroller = new OverScroller(getContext());
		setFocusable(true);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setWillNotDraw(false);
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	private boolean inChild(int x, int y) {
		return true;
	}

	private void initOrResetVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTrackerIfNotExists() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	@Override
	public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		if (disallowIntercept) {
			recycleVelocityTracker();
		}
		super.requestDisallowInterceptTouchEvent(disallowIntercept);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onMotionEvent will be called and we do the actual
		 * scrolling there.
		 */

		/*
		 * Shortcut the most recurring case: the user is in the dragging
		 * state and he is moving his finger.  We want to intercept this
		 * motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
			return true;
		}

		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_MOVE: {
				/*
				 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
				 * whether the user has moved far enough from his original down touch.
				 */

				/*
				 * Locally do absolute value. mLastMotionY is set to the y value
				 * of the down event.
				 */
				final int activePointerId = mActivePointerId;
				if (activePointerId == INVALID_POINTER) {
					// If we don't have a valid id, the touch down wasn't on content.
					break;
				}

				final int pointerIndex = ev.findPointerIndex(activePointerId);
				if (pointerIndex == -1) {
					Log.e(TAG, "Invalid pointerId=" + activePointerId
							+ " in onInterceptTouchEvent");
					break;
				}

				final int y = (int) ev.getY(pointerIndex);
				final int x = (int) ev.getX(pointerIndex);
				final int yDiff = Math.abs(y - mLastMotionY);
				final int xDiff = Math.abs(x - mLastMotionX);
				if (yDiff > mTouchSlop
						&& (getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == 0) {
					mIsBeingDragged = true;
					mLastMotionY = y;
					mLastMotionX = x;
					initVelocityTrackerIfNotExists();
					mVelocityTracker.addMovement(ev);
					mNestedYOffset = 0;
					final ViewParent parent = getParent();
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
				}
				break;
			}

			case MotionEvent.ACTION_DOWN: {
				final int y = (int) ev.getY();
				final int x = (int) ev.getX();
				if (!inChild(x, y)) {
					mIsBeingDragged = false;
					recycleVelocityTracker();
					break;
				}

				/*
				 * Remember location of down touch.
				 * ACTION_DOWN always refers to pointer index 0.
				 */
				mLastMotionY = y;
				mLastMotionX = x;
				mActivePointerId = ev.getPointerId(0);

				initOrResetVelocityTracker();
				mVelocityTracker.addMovement(ev);
				/*
				 * If being flinged and user touches the screen, initiate drag;
				 * otherwise don't. mScroller.isFinished should be false when
				 * being flinged. We need to call computeScrollOffset() first so that
				 * isFinished() is correct.
				 */
				mScroller.computeScrollOffset();
				mIsBeingDragged = !mScroller.isFinished();
				startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				/* Release the drag */
				mIsBeingDragged = false;
				mActivePointerId = INVALID_POINTER;
				recycleVelocityTracker();
				if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange())) {
					ViewCompat.postInvalidateOnAnimation(this);
				}
				stopNestedScroll(ViewCompat.TYPE_TOUCH);
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp(ev);
				break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(ev.getPointerCount()>=2) ruined=true;

		final int actionMasked = ev.getActionMasked();
		if (actionMasked == MotionEvent.ACTION_DOWN) {
			mNestedYOffset = 0;
			ruined = false;
		}

		if(ruined) return super.onTouchEvent(ev);

		initVelocityTrackerIfNotExists();

		MotionEvent vtev = MotionEvent.obtain(ev);
		vtev.offsetLocation(0, mNestedYOffset);
		switch (actionMasked) {
			case MotionEvent.ACTION_DOWN: {
				OrgTop = getTop();
				if ((mIsBeingDragged = !mScroller.isFinished())) {
					final ViewParent parent = getParent();
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
				}

				/*
				 * If being flinged and user touches, stop the fling. isFinished
				 * will be false if being flinged.
				 */
				if (!mScroller.isFinished()) {
					abortAnimatedScroll();
				}

				// Remember where the motion event started
				mLastMotionY = (int) ev.getY();
				mLastMotionX = (int) ev.getX();
				mActivePointerId = ev.getPointerId(0);
				startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
				break;
			}
			case MotionEvent.ACTION_MOVE:
				final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
				if (activePointerIndex == -1) {
					Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
					break;
				}

				final int y = (int) ev.getY(activePointerIndex);
				final int x = (int) ev.getX(activePointerIndex);
				int deltaY = mLastMotionY - y;
				int deltaX = mLastMotionX - x;
				if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset,
						ViewCompat.TYPE_TOUCH)) {
					deltaY -= mScrollConsumed[1];
					mNestedYOffset += mScrollOffset[1];
				}
				if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
					final ViewParent parent = getParent();
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
					mIsBeingDragged = true;
					if (deltaY > 0) {
						deltaY -= mTouchSlop;
					} else {
						deltaY += mTouchSlop;
					}
				}
				if (mIsBeingDragged) {
					// Scroll to follow the motion event
					mLastMotionY = y - mScrollOffset[1];
					mLastMotionX = x;

					final int oldY = getScrollY();
					final int oldX = getScrollX();
					final int range = getScrollRange();
					final int rangeX = getHRange();
					final int overscrollMode = getOverScrollMode();
					boolean canOverscroll = overscrollMode == View.OVER_SCROLL_ALWAYS
							|| (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

					// Calling overScrollByCompat will call onOverScrolled, which
					// calls onScrollChanged if applicable.
					if (overScrollByCompat(deltaX, deltaY, getScrollX(), getScrollY(), rangeX, range, 0,
							0, true) && !hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)) {
						// Break our velocity if we hit a scroll barrier.
						mVelocityTracker.clear();
					}

					final int scrolledDeltaY = getScrollY() - oldY;
					final int scrolledDeltaX = getScrollX() - oldX;
					final int unconsumedY = deltaY - scrolledDeltaY;
					final int unconsumedX = deltaX - scrolledDeltaX;

					mScrollConsumed[1] = 0;

					dispatchNestedScroll(scrolledDeltaX, scrolledDeltaY, unconsumedX, unconsumedY, mScrollOffset,
							ViewCompat.TYPE_TOUCH, mScrollConsumed);

					mLastMotionY -= mScrollOffset[1];
					mNestedYOffset += mScrollOffset[1];

					if (canOverscroll) {
						deltaY -= mScrollConsumed[1];
						ensureGlows();
						final int pulledToY = oldY + deltaY;
						if (pulledToY < 0) {
							EdgeEffectCompat.onPull(mEdgeGlowTop, (float) deltaY / getScrollRange(),
									ev.getX(activePointerIndex) / getWidth());
							if (!mEdgeGlowBottom.isFinished()) {
								mEdgeGlowBottom.onRelease();
							}
						} else if (pulledToY > range) {
							EdgeEffectCompat.onPull(mEdgeGlowBottom, (float) deltaY / getScrollRange(),
									1.f - ev.getX(activePointerIndex)
											/ getWidth());
							if (!mEdgeGlowTop.isFinished()) {
								mEdgeGlowTop.onRelease();
							}
						}
						if (mEdgeGlowTop != null
								&& (!mEdgeGlowTop.isFinished() || !mEdgeGlowBottom.isFinished())) {
							ViewCompat.postInvalidateOnAnimation(this);
						}
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
				if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
					if (!dispatchNestedPreFling(0, -initialVelocity)) {
						dispatchNestedFling(0, -initialVelocity, true);
						fling(-initialVelocity);
					}
				} else if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
						getScrollRange())) {
					ViewCompat.postInvalidateOnAnimation(this);
				}
				mActivePointerId = INVALID_POINTER;
				endDrag();
				break;
			case MotionEvent.ACTION_CANCEL:
				if (mIsBeingDragged) {
					if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
							getScrollRange())) {
						ViewCompat.postInvalidateOnAnimation(this);
					}
				}
				mActivePointerId = INVALID_POINTER;
				endDrag();
				break;
			case MotionEvent.ACTION_POINTER_DOWN: {
				final int index = ev.getActionIndex();
				mLastMotionY = (int) ev.getY(index);
				mLastMotionX = (int) ev.getX(index);
				mActivePointerId = ev.getPointerId(index);
				break;
			}
			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp(ev);
				mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
				mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
				break;
		}

		if (mVelocityTracker != null) {
			mVelocityTracker.addMovement(vtev);
		}
		vtev.recycle();
		if(!mIsBeingDragged && OrgTop-getTop()==0) super.onTouchEvent(ev);
		interceptLongClick=interceptLongClick||OrgTop-getTop()!=0;
		return true;
	}

	boolean interceptLongClick;
	OnLongClickListener lc = v -> interceptLongClick;

	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = ev.getActionIndex();
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionY = (int) ev.getY(newPointerIndex);
			mActivePointerId = ev.getPointerId(newPointerIndex);
			if (mVelocityTracker != null) {
				mVelocityTracker.clear();
			}
		}
	}

	boolean overScrollByCompat(int deltaX, int deltaY,
							   int scrollX, int scrollY,
							   int scrollRangeX, int scrollRangeY,
							   int maxOverScrollX, int maxOverScrollY,
							   boolean isTouchEvent) {
		final int overScrollMode = getOverScrollMode();
		final boolean canScrollHorizontal =
				computeHorizontalScrollRange() > computeHorizontalScrollExtent();
		final boolean canScrollVertical =
				getScrollRange() > computeVerticalScrollExtent();
		final boolean overScrollHorizontal = overScrollMode == View.OVER_SCROLL_ALWAYS
				|| (overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
		final boolean overScrollVertical = overScrollMode == View.OVER_SCROLL_ALWAYS
				|| (overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

		int newScrollX = scrollX + deltaX;
		if (!overScrollHorizontal) {
			maxOverScrollX = 0;
		}

		int newScrollY = scrollY + deltaY;
		if (!overScrollVertical) {
			maxOverScrollY = 0;
		}

		// Clamp values if at the limits and record
		final int left = -maxOverScrollX;
		final int right = maxOverScrollX + getWidth();
		final int top = -maxOverScrollY;
		final int bottom = maxOverScrollY + scrollRangeY;

		boolean clampedX = false;
		if (newScrollX > right) {
			newScrollX = right;
			clampedX = true;
		} else if (newScrollX < left) {
			newScrollX = left;
			clampedX = true;
		}

		boolean clampedY = false;
		if (newScrollY > bottom) {
			newScrollY = bottom;
			clampedY = true;
		} else if (newScrollY < top) {
			newScrollY = top;
			clampedY = true;
		}

		if (clampedY && !hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
			mScroller.springBack(newScrollX, newScrollY, 0, getWidth(), 0, getScrollRange());
		}

		onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

		return clampedX || clampedY;
	}

	int getScrollRange() {
		return computeVerticalScrollRange();
	}
	int getHRange() {
		return computeHorizontalScrollRange();
	}

	/** @hide */
	@RestrictTo(LIBRARY_GROUP_PREFIX)
	@Override
	public int computeVerticalScrollOffset() {
		return Math.max(0, super.computeVerticalScrollOffset());
	}


	@Override
	public void computeScroll() {

		if (mScroller.isFinished()) {
			return;
		}

		mScroller.computeScrollOffset();
		final int y = mScroller.getCurrY();
		int unconsumed = y - mLastScrollerY;
		mLastScrollerY = y;

		// Nested Scrolling Pre Pass
		mScrollConsumed[1] = 0;
		dispatchNestedPreScroll(0, unconsumed, mScrollConsumed, null,
				ViewCompat.TYPE_NON_TOUCH);
		unconsumed -= mScrollConsumed[1];

		final int range = getScrollRange();

		if (unconsumed != 0) {
			// Internal Scroll
			final int oldScrollY = getScrollY();
			overScrollByCompat(0, unconsumed, getScrollX(), oldScrollY, 0, range, 0, 0, false);
			final int scrolledByMe = getScrollY() - oldScrollY;
			unconsumed -= scrolledByMe;

			// Nested Scrolling Post Pass
			mScrollConsumed[1] = 0;
			dispatchNestedScroll(0, scrolledByMe, 0, unconsumed, mScrollOffset,
					ViewCompat.TYPE_NON_TOUCH, mScrollConsumed);
			unconsumed -= mScrollConsumed[1];
		}

		if (unconsumed != 0) {
			final int mode = getOverScrollMode();
			final boolean canOverscroll = mode == OVER_SCROLL_ALWAYS
					|| (mode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);
			if (canOverscroll) {
				ensureGlows();
				if (unconsumed < 0) {
					if (mEdgeGlowTop.isFinished()) {
						mEdgeGlowTop.onAbsorb((int) mScroller.getCurrVelocity());
					}
				} else {
					if (mEdgeGlowBottom.isFinished()) {
						mEdgeGlowBottom.onAbsorb((int) mScroller.getCurrVelocity());
					}
				}
			}
			abortAnimatedScroll();
		}

		if (!mScroller.isFinished()) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	private void runAnimatedScroll(boolean participateInNestedScrolling) {
		if (participateInNestedScrolling) {
			startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
		} else {
			stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
		}
		mLastScrollerY = getScrollY();
		ViewCompat.postInvalidateOnAnimation(this);
	}

	private void abortAnimatedScroll() {
		mScroller.abortAnimation();
		stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
	}



	/**
	 * When looking for focus in children of a scroll view, need to be a little
	 * more careful not to give focus to something that is scrolled off screen.
	 *
	 * This is more expensive than the default {@link android.view.ViewGroup}
	 * implementation, otherwise this behavior might have been made the default.
	 */
	@Override
	protected boolean onRequestFocusInDescendants(int direction,
												  Rect previouslyFocusedRect) {

		// convert from forward / backward notation to up / down / left / right
		// (ugh).
		if (direction == View.FOCUS_FORWARD) {
			direction = View.FOCUS_DOWN;
		} else if (direction == View.FOCUS_BACKWARD) {
			direction = View.FOCUS_UP;
		}

		final View nextFocus = previouslyFocusedRect == null
				? FocusFinder.getInstance().findNextFocus(this, null, direction)
				: FocusFinder.getInstance().findNextFocusFromRect(
				this, previouslyFocusedRect, direction);

		if (nextFocus == null) {
			return false;
		}


		return nextFocus.requestFocus(direction, previouslyFocusedRect);
	}



	/**
	 * Fling the scroll view
	 *
	 * @param velocityY The initial velocity in the Y direction. Positive
	 *                  numbers mean that the finger/cursor is moving down the screen,
	 *                  which means we want to scroll towards the top.
	 */
	public void fling(int velocityY) {
		mScroller.fling(getScrollX(), getScrollY(), // start
				0, velocityY, // velocities
				0, 0, // x
				Integer.MIN_VALUE, Integer.MAX_VALUE, // y
				0, 0); // overscroll
		runAnimatedScroll(true);
	}

	private void endDrag() {
		mIsBeingDragged = false;

		recycleVelocityTracker();
		stopNestedScroll(ViewCompat.TYPE_TOUCH);

		if (mEdgeGlowTop != null) {
			mEdgeGlowTop.onRelease();
			mEdgeGlowBottom.onRelease();
		}
	}


	private void ensureGlows() {
		if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
			if (mEdgeGlowTop == null) {
				Context context = getContext();
				mEdgeGlowTop = new EdgeEffect(context);
				mEdgeGlowBottom = new EdgeEffect(context);
			}
		} else {
			mEdgeGlowTop = null;
			mEdgeGlowBottom = null;
		}
	}


}

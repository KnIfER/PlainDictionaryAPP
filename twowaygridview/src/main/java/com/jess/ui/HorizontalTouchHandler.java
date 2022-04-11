package com.jess.ui;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  Horizontal Touch Handler
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
import static com.jess.ui.TwoWayAbsListView.*;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.widget.Scroller;

class HorizontalTouchHandler extends TouchHandler {
	/**
	 * The offset to the top of the mMotionPosition view when the down motion event was received
	 */
	int mMotionViewOriginalLeft;

	/**
	 * X value from on the previous motion event (if any)
	 */
	int mLastX;

	/**
	 * The desired offset to the top of the mMotionPosition view after a scroll
	 */
	int mMotionViewNewLeft;
	
	HorizontalTouchHandler(TwoWayAbsListView v) {
		super(v);
	}
	
	
	@Override
	protected FlingRunnable getFlingRunnable() {
		return new HorizontalFlingRunnable();
	}


	@Override
	protected PositionScroller getPositionScroller() {
		return new HorizontalPositionScroller();
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();

		/*
		if (mFastScroller != null) {
			boolean intercepted = mFastScroller.onInterceptTouchEvent(ev);
			if (intercepted) {
				return true;
			}
		}*/

		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				int touchMode = v.mTouchMode;

				final int x = (int) ev.getX();
				final int y = (int) ev.getY();
				
				int motionPosition = v.findMotionRowX(x);
				if (touchMode != TOUCH_MODE_FLING && motionPosition >= 0) {
					// User clicked on an actual view (and was not stopping a fling).
					// Remember where the motion event started
					View child = v.getChildAt(motionPosition - v.mFirstPosition);
					mMotionViewOriginalLeft = child.getLeft();
					v.mMotionX = x;
					v.mMotionY = y;
					v.mMotionPosition = motionPosition;
					v.mTouchMode = TOUCH_MODE_DOWN;
					clearScrollingCache();
				}
				mLastX = Integer.MIN_VALUE;
				v.initOrResetVelocityTracker();
				v.mVelocityTracker.addMovement(ev);
				if (touchMode == TOUCH_MODE_FLING) {
					return true;
				}
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				switch (v.mTouchMode) {
				case TOUCH_MODE_DOWN:
					final int x = (int) ev.getX();
					if (startScrollIfNeeded(x - v.mMotionX)) {
						return true;
					}
					break;
				}
				break;
			}

			case MotionEvent.ACTION_UP: {
				v.mTouchMode = TOUCH_MODE_REST;
				v.mActivePointerId = INVALID_POINTER;
				reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
				break;
			}

		}

		return false;
	}


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!v.isEnabled()) {
			// A disabled view that is clickable still consumes the touch
			// events, it just doesn't respond to them.
			return v.isClickable() || v.isLongClickable();
		}

		/*
		if (mFastScroller != null) {
			boolean intercepted = mFastScroller.onTouchEvent(ev);
			if (intercepted) {
				return true;
			}
		}*/

		final int action = ev.getAction();

		int deltaX;

		if (v.mVelocityTracker == null) {
			v.mVelocityTracker = VelocityTracker.obtain();
		}
		v.mVelocityTracker.addMovement(ev);

		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				final int x = (int) ev.getX();
				final int y = (int) ev.getY();
				int motionPosition = v.pointToPosition(x, y);
				if (!v.mDataChanged) {
					if ((v.mTouchMode != TOUCH_MODE_FLING) && (motionPosition >= 0)
							&& (v.getAdapter().isEnabled(motionPosition))) {
						// User clicked on an actual view (and was not stopping a fling). It might be a
						// click or a scroll. Assume it is a click until proven otherwise
						v.mTouchMode = TOUCH_MODE_DOWN;
						// FIXME Debounce
						v.postDelayed(v.mPendingCheckForTap, ViewConfiguration.getTapTimeout());
					} else {
						if (ev.getEdgeFlags() != 0 && motionPosition < 0) {
							// If we couldn't find a view to click on, but the down event was touching
							// the edge, we will bail out and try again. This allows the edge correcting
							// code in ViewRoot to try to find a nearby view to select
							return false;
						}

						if (v.mTouchMode == TOUCH_MODE_FLING) {
							// Stopped a fling. It is a scroll.
							createScrollingCache();
							v.mTouchMode = TOUCH_MODE_SCROLL;
							mMotionCorrection = 0;
							motionPosition = v.findMotionRowX(x);
							if(mFlingRunnable!=null)
								mFlingRunnable.flywheelTouch();
						}
					}
				}

				if (motionPosition >= 0) {
					// Remember where the motion event started
					View child = v.getChildAt(motionPosition - v.mFirstPosition);
					mMotionViewOriginalLeft = child.getLeft();
				}
				v.mMotionX = x;
				v.mMotionY = y;
				v.mMotionPosition = motionPosition;
				mLastX = Integer.MIN_VALUE;
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				final int x = (int) ev.getX();
				deltaX = x - v.mMotionX;
				switch (v.mTouchMode) {
				case TOUCH_MODE_DOWN:
				case TOUCH_MODE_TAP:
				case TOUCH_MODE_DONE_WAITING:
					// Check if we have moved far enough that it looks more like a
					// scroll than a tap
					startScrollIfNeeded(deltaX);
					break;
				case TOUCH_MODE_SCROLL:
					if (PROFILE_SCROLLING) {
						if (!v.mScrollProfilingStarted) {
							Debug.startMethodTracing("JessAbsListViewScroll");
							v.mScrollProfilingStarted = true;
						}
					}

					if (x != mLastX) {
						deltaX -= mMotionCorrection;
						int incrementalDeltaX = mLastX != Integer.MIN_VALUE ? x - mLastX : deltaX;

						// No need to do all this work if we're not going to move anyway
						boolean atEdge = false;
						if (incrementalDeltaX != 0) {
							atEdge = trackMotionScroll(deltaX, incrementalDeltaX);
						}

						// Check to see if we have bumped into the scroll limit
						if (atEdge && v.getChildCount() > 0) {
							// Treat this like we're starting a new scroll from the current
							// position. This will let the user start scrolling back into
							// content immediately rather than needing to scroll back to the
							// point where they hit the limit first.
							int motionPosition = v.findMotionRowX(x);
							if (motionPosition >= 0) {
								final View motionView = v.getChildAt(motionPosition - v.mFirstPosition);
								mMotionViewOriginalLeft = motionView.getLeft();
							}
							v.mMotionX = x;
							v.mMotionPosition = motionPosition;
							v.invalidate();
						}
						mLastX = x;
					}
					break;
				}

				break;
			}

			case MotionEvent.ACTION_UP: {
				switch (v.mTouchMode) {
				case TOUCH_MODE_DOWN:
				case TOUCH_MODE_TAP:
				case TOUCH_MODE_DONE_WAITING:
					final int motionPosition = v.mMotionPosition;
					final View child = v.getChildAt(motionPosition - v.mFirstPosition);
					if (child != null && !child.hasFocusable()) {
						if (v.mTouchMode != TOUCH_MODE_DOWN) {
							child.setPressed(false);
						}

						final TwoWayAbsListView.PerformClick performClick = v.mPerformClick;
						performClick.mChild = child;
						performClick.mClickMotionPosition = motionPosition;
						performClick.rememberWindowAttachCount();
						
						v.mResurrectToPosition = motionPosition;

						if (v.mTouchMode == TOUCH_MODE_DOWN || v.mTouchMode == TOUCH_MODE_TAP) {
							final Handler handler = v.getHandler();
							if (handler != null) {
								handler.removeCallbacks(v.mTouchMode == TOUCH_MODE_DOWN ?
										v.mPendingCheckForTap : v.mPendingCheckForLongPress);
							}
							v.mLayoutMode = LAYOUT_NORMAL;
							if (!v.mDataChanged && v.mAdapter.isEnabled(motionPosition)) {
								v.mTouchMode = TOUCH_MODE_TAP;
								v.setSelectedPositionInt(v.mMotionPosition);
								v.layoutChildren();
								child.setPressed(true);
								v.positionSelector(child);
								v.setPressed(true);
								if (v.mSelector != null) {
									Drawable d = v.mSelector.getCurrent();
									if (d != null && d instanceof TransitionDrawable) {
										((TransitionDrawable) d).resetTransition();
									}
								}
								v.postDelayed(new Runnable() {
									public void run() {
										child.setPressed(false);
										v.setPressed(false);
										if (!v.mDataChanged) {
											v.post(performClick);
										}
										v.mTouchMode = TOUCH_MODE_REST;
									}
								}, ViewConfiguration.getPressedStateDuration());
							} else {
								v.mTouchMode = TOUCH_MODE_REST;
							}
							return true;
						} else if (!v.mDataChanged && v.mAdapter.isEnabled(motionPosition)) {
							v.post(performClick);
						}
					}
					v.mTouchMode = TOUCH_MODE_REST;
					break;
				case TOUCH_MODE_SCROLL:
					final int childCount = v.getChildCount();
					if (childCount > 0) {
						if (v.mFirstPosition == 0 && v.getChildAt(0).getLeft() >= v.mListPadding.left &&
								v.mFirstPosition + childCount < v.mItemCount &&
								v.getChildAt(childCount - 1).getRight() <=
										v.getWidth() - v.mListPadding.right) {
							v.mTouchMode = TOUCH_MODE_REST;
							reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
						} else {
							final VelocityTracker velocityTracker = v.mVelocityTracker;
							velocityTracker.computeCurrentVelocity(1000);
							final int initialVelocity = (int) velocityTracker.getXVelocity();

							if (Math.abs(initialVelocity) > v.mMinimumVelocity) {
								if (mFlingRunnable == null) {
									mFlingRunnable = new HorizontalFlingRunnable();
								}
								reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);

								mFlingRunnable.start(-initialVelocity);
							} else {
								v.mTouchMode = TOUCH_MODE_REST;
								reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
							}
						}
					} else {
						v.mTouchMode = TOUCH_MODE_REST;
						reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
					}
					break;
				}
				
				v.setPressed(false);

				// Need to redraw since we probably aren't drawing the selector anymore
				v.invalidate();

				final Handler handler = v.getHandler();
				if (handler != null) {
					handler.removeCallbacks(v.mPendingCheckForLongPress);
				}

				if (v.mVelocityTracker != null) {
					v.mVelocityTracker.recycle();
					v.mVelocityTracker = null;
				}
				
				v.mActivePointerId = INVALID_POINTER;

				if (PROFILE_SCROLLING) {
					if (v.mScrollProfilingStarted) {
						Debug.stopMethodTracing();
						v.mScrollProfilingStarted = false;
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL: {
				v.mTouchMode = TOUCH_MODE_REST;
				v.setPressed(false);
				View motionView = v.getChildAt(v.mMotionPosition - v.mFirstPosition);
				if (motionView != null) {
					motionView.setPressed(false);
				}
				clearScrollingCache();

				final Handler handler = v.getHandler();
				if (handler != null) {
					handler.removeCallbacks(v.mPendingCheckForLongPress);
				}

				if (v.mVelocityTracker != null) {
					v.mVelocityTracker.recycle();
					v.mVelocityTracker = null;
				}
				
				v.mActivePointerId = INVALID_POINTER;
				break;
			}
		}

		return true;
	}


	@Override
	boolean resurrectSelection() {
		final int childCount = v.getChildCount();

		if (childCount <= 0) {
			return false;
		}

		int selectedLeft = 0;
		int selectedPos;
		int childrenLeft = v.mListPadding.top;
		int childrenRight = v.getRight() - v.getLeft() - v.mListPadding.right;
		final int firstPosition = v.mFirstPosition;
		final int toPosition = v.mResurrectToPosition;
		boolean down = true;

		if (toPosition >= firstPosition && toPosition < firstPosition + childCount) {
			selectedPos = toPosition;

			final View selected = v.getChildAt(selectedPos - v.mFirstPosition);
			selectedLeft = selected.getLeft();
			int selectedRight = selected.getRight();

			// We are scrolled, don't get in the fade
			if (selectedLeft < childrenLeft) {
				selectedLeft = childrenLeft + v.getHorizontalFadingEdgeLength();
			} else if (selectedRight > childrenRight) {
				selectedLeft = childrenRight - selected.getMeasuredWidth()
				- v.getHorizontalFadingEdgeLength();
			}
		} else {
			if (toPosition < firstPosition) {
				// Default to selecting whatever is first
				selectedPos = firstPosition;
				for (int i = 0; i < childCount; i++) {
					final View child = v.getChildAt(i);
					final int left = child.getLeft();

					if (i == 0) {
						// Remember the position of the first item
						selectedLeft = left;
						// See if we are scrolled at all
						if (firstPosition > 0 || left < childrenLeft) {
							// If we are scrolled, don't select anything that is
							// in the fade region
							childrenLeft += v.getHorizontalFadingEdgeLength();
						}
					}
					if (left >= childrenLeft) {
						// Found a view whose top is fully visisble
						selectedPos = firstPosition + i;
						selectedLeft = left;
						break;
					}
				}
			} else {
				final int itemCount = v.mItemCount;
				down = false;
				selectedPos = firstPosition + childCount - 1;

				for (int i = childCount - 1; i >= 0; i--) {
					final View child = v.getChildAt(i);
					final int left = child.getLeft();
					final int right = child.getRight();

					if (i == childCount - 1) {
						selectedLeft = left;
						if (firstPosition + childCount < itemCount || right > childrenRight) {
							childrenRight -= v.getHorizontalFadingEdgeLength();
						}
					}

					if (right <= childrenRight) {
						selectedPos = firstPosition + i;
						selectedLeft = left;
						break;
					}
				}
			}
		}
		
		v.mResurrectToPosition = INVALID_POSITION;
		v.removeCallbacks(mFlingRunnable);
		v.mTouchMode = TOUCH_MODE_REST;
		clearScrollingCache();
		v.mSpecificTop = selectedLeft;
		selectedPos = v.lookForSelectablePosition(selectedPos, down);
		if (selectedPos >= firstPosition && selectedPos <= v.getLastVisiblePosition()) {
			v.mLayoutMode = LAYOUT_SPECIFIC;
			v.setSelectionInt(selectedPos);
			v.invokeOnItemScrollListener();
		} else {
			selectedPos = INVALID_POSITION;
		}
		reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);

		return selectedPos >= 0;
	}


	@Override
	boolean trackMotionScroll(int delta, int incrementalDelta) {
		if (DEBUG) Log.i(TAG, "trackMotionScroll() - deltaX: " + delta + " incrDeltaX: " + incrementalDelta);
		final int childCount = v.getChildCount();
		if (childCount == 0) {
			return true;
		}

		final int firstLeft = v.getChildAt(0).getLeft();
		final int lastRight = v.getChildAt(childCount - 1).getRight();

		final Rect listPadding = v.mListPadding;

		// FIXME account for grid horizontal spacing too?
		final int spaceAbove = listPadding.left - firstLeft;
		final int end = v.getWidth() - listPadding.right;
		final int spaceBelow = lastRight - end;

		final int width = v.getWidth() - v.getPaddingRight() - v.getPaddingLeft();
		if (delta < 0) {
			delta = Math.max(-(width - 1), delta);
		} else {
			delta = Math.min(width - 1, delta);
		}

		if (incrementalDelta < 0) {
			incrementalDelta = Math.max(-(width - 1), incrementalDelta);
		} else {
			incrementalDelta = Math.min(width - 1, incrementalDelta);
		}

		final int firstPosition = v.mFirstPosition;

		if (firstPosition == 0 && firstLeft >= listPadding.left && delta >= 0) {
			// Don't need to move views right if the top of the first position
			// is already visible
			if (DEBUG) Log.i(TAG, "trackScrollMotion returning true");
			return true;
		}

		if (firstPosition + childCount == v.mItemCount && lastRight <= end && delta <= 0) {
			// Don't need to move views left if the bottom of the last position
			// is already visible
			if (DEBUG) Log.i(TAG, "trackScrollMotion returning true");
			return true;
		}

		final boolean down = incrementalDelta < 0;

		final boolean inTouchMode = v.isInTouchMode();
		if (inTouchMode) {
			v.hideSelector();
		}

		final int headerViewsCount = v.getHeaderViewsCount();
		final int footerViewsStart = v.mItemCount - v.getFooterViewsCount();

		int start = 0;
		int count = 0;

		if (down) {
			final int left = listPadding.left - incrementalDelta;
			for (int i = 0; i < childCount; i++) {
				final View child = v.getChildAt(i);
				if (child.getRight() >= left) {
					break;
				} else {
					count++;
					int position = firstPosition + i;
					if (position >= headerViewsCount && position < footerViewsStart) {
						v.mRecycler.addScrapView(child);

						if (ViewDebug.TRACE_RECYCLER) {
							ViewDebug.trace(child,
									ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP,
									firstPosition + i, -1);
						}
					}
				}
			}
		} else {
			final int right = v.getWidth() - listPadding.right - incrementalDelta;
			for (int i = childCount - 1; i >= 0; i--) {
				final View child = v.getChildAt(i);
				if (child.getLeft() <= right) {
					break;
				} else {
					start = i;
					count++;
					int position = firstPosition + i;
					if (position >= headerViewsCount && position < footerViewsStart) {
						v.mRecycler.addScrapView(child);

						if (ViewDebug.TRACE_RECYCLER) {
							ViewDebug.trace(child,
									ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP,
									firstPosition + i, -1);
						}
					}
				}
			}
		}

		mMotionViewNewLeft = mMotionViewOriginalLeft + delta;
		
		v.mBlockLayoutRequests = true;

		if (count > 0) {
			v.detachViews(start, count);
		}
		v.offsetChildrenLeftAndRight(incrementalDelta);

		if (down) {
			v.mFirstPosition += count;
		}
		
		v.invalidate();

		final int absIncrementalDelta = Math.abs(incrementalDelta);
		if (spaceAbove < absIncrementalDelta|| spaceBelow < absIncrementalDelta) {
			v.fillGap(down);
		}

		if (!inTouchMode && v.mSelectedPosition != INVALID_POSITION) {
			final int childIndex = v.mSelectedPosition - v.mFirstPosition;
			if (childIndex >= 0 && childIndex < v.getChildCount()) {
				v.positionSelector(v.getChildAt(childIndex));
			}
		}
		
		v.mBlockLayoutRequests = false;
		
		v.invokeOnItemScrollListener();
		//awakenScrollBars();
		if (DEBUG) Log.i(TAG, "trackScrollMotion returning false - mFirstPosition: " + v.mFirstPosition);
		return false;
	}


	/**
	 * Responsible for fling behavior. Use {@link #start(int)} to
	 * initiate a fling. Each frame of the fling is handled in {@link #run()}.
	 * A FlingRunnable will keep re-posting itself until the fling is done.
	 *
	 */
	class HorizontalFlingRunnable extends FlingRunnable {
		/**
		 * X value reported by mScroller on the previous fling
		 */
		protected int mLastFlingX;
		
		@Override
		void start(int initialVelocity) {
			int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingX = initialX;
			mScroller.fling(initialX, 0, initialVelocity, 0,
					0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			v.mTouchMode = TOUCH_MODE_FLING;
			v.post(this);
			//postOnAnimation(this);

			if (PROFILE_FLINGING) {
				if (!v.mFlingProfilingStarted) {
					Debug.startMethodTracing("AbsListViewFling");
					v.mFlingProfilingStarted = true;
				}
			}
		}

		@Override
		void startScroll(int distance, int duration) {
			int initialX = distance < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingX = initialX;
			mScroller.startScroll(initialX, 0, distance, 0, duration);
			v.mTouchMode = TOUCH_MODE_FLING;
			v.post(this);
		}

		@Override
		public void run() {
			switch (v.mTouchMode) {
			default:
				break;

			case TOUCH_MODE_FLING: {
				if (v.mItemCount == 0 || v.getChildCount() == 0) {
					endFling();
					break;
				}

				final Scroller scroller = mScroller;
				boolean more = scroller.computeScrollOffset();
				final int x = scroller.getCurrX();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the top)
				int delta = mLastFlingX - x;

				// Pretend that each frame of a fling scroll is a touch scroll
				if (delta > 0) {
					// List is moving towards the top. Use first view as mMotionPosition
					v.mMotionPosition = v.mFirstPosition;
					final View firstView = v.getChildAt(0);
					mMotionViewOriginalLeft = firstView.getLeft();

					// Don't fling more than 1 screen
					delta = Math.min(v.getWidth() - v.getPaddingRight() - v.getPaddingLeft() - 1, delta);
				} else {
					// List is moving towards the bottom. Use last view as mMotionPosition
					int offsetToLast = v.getChildCount() - 1;
					v.mMotionPosition = v.mFirstPosition + offsetToLast;

					final View lastView = v.getChildAt(offsetToLast);
					mMotionViewOriginalLeft = lastView.getLeft();

					// Don't fling more than 1 screen
					delta = Math.max(-(v.getWidth() - v.getPaddingRight() - v.getPaddingLeft() - 1), delta);
				}

				final boolean atEnd = trackMotionScroll(delta, delta);

				if (more && !atEnd) {
					v.invalidate();
					mLastFlingX = x;
					v.post(this);
					//postOnAnimation(this);
				} else {
					endFling();

					if (PROFILE_FLINGING) {
						if (v.mFlingProfilingStarted) {
							Debug.stopMethodTracing();
							v.mFlingProfilingStarted = false;
						}
					}
				}
				break;
			}
			}
			if(mScroller.isFinished() && v.mTouchMode != TOUCH_MODE_REST) {
				endFling();
			}
		}
		
		
		static final int FLYWHEEL_TIMEOUT = 40; // milliseconds

		public void flywheelTouch() {
			if(mCheckFlywheel == null) {
				mCheckFlywheel = new Runnable() {
					public void run() {
						final VelocityTracker vt = v.mVelocityTracker;
						if (vt == null) {
							return;
						}

						vt.computeCurrentVelocity(1000, v.mMaximumVelocity);
						final float xvel = -vt.getXVelocity();

						if (Math.abs(xvel) >= v.mMinimumVelocity
								&& isScrollingInDirection(0, xvel)) {
							// Keep the fling alive a little longer
							v.postDelayed(this, FLYWHEEL_TIMEOUT);
						} else {
							endFling();
							v.mTouchMode = TOUCH_MODE_SCROLL;
							reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
						}
					}
				};
			}
			v.postDelayed(mCheckFlywheel, FLYWHEEL_TIMEOUT);
		}
	}


	class HorizontalPositionScroller extends PositionScroller {
		@Override
		public void run() {
			final int listWidth = v.getWidth();
			final int firstPos = v.mFirstPosition;

			switch (mMode) {
			case MOVE_DOWN_POS: {
				final int lastViewIndex = v.getChildCount() - 1;
				final int lastPos = firstPos + lastViewIndex;

				if (lastViewIndex < 0) {
					return;
				}

				if (lastPos == mLastSeenPos) {
					// No new views, let things keep going.
					v.post(this);
					return;
				}

				final View lastView = v.getChildAt(lastViewIndex);
				final int lastViewWidth = lastView.getWidth();
				final int lastViewLeft = lastView.getLeft();
				final int lastViewPixelsShowing = listWidth - lastViewLeft;
				final int extraScroll = lastPos < v.mItemCount - 1 ? mExtraScroll : v.mListPadding.right;

				smoothScrollBy(lastViewWidth - lastViewPixelsShowing + extraScroll,
						mScrollDuration);

				mLastSeenPos = lastPos;
				if (lastPos < mTargetPos) {
					v.post(this);
				}
				break;
			}

			case MOVE_DOWN_BOUND: {
				final int nextViewIndex = 1;
				final int childCount = v.getChildCount();

				if (firstPos == mBoundPos || childCount <= nextViewIndex
						|| firstPos + childCount >= v.mItemCount) {
					return;
				}
				final int nextPos = firstPos + nextViewIndex;

				if (nextPos == mLastSeenPos) {
					// No new views, let things keep going.
					v.post(this);
					return;
				}

				final View nextView = v.getChildAt(nextViewIndex);
				final int nextViewWidth = nextView.getWidth();
				final int nextViewLeft = nextView.getLeft();
				final int extraScroll = mExtraScroll;
				if (nextPos < mBoundPos) {
					smoothScrollBy(Math.max(0, nextViewWidth + nextViewLeft - extraScroll),
							mScrollDuration);

					mLastSeenPos = nextPos;
					
					v.post(this);
				} else  {
					if (nextViewLeft > extraScroll) {
						smoothScrollBy(nextViewLeft - extraScroll, mScrollDuration);
					}
				}
				break;
			}

			case MOVE_UP_POS: {
				if (firstPos == mLastSeenPos) {
					// No new views, let things keep going.
					v.post(this);
					return;
				}

				final View firstView = v.getChildAt(0);
				if (firstView == null) {
					return;
				}
				final int firstViewLeft = firstView.getLeft();
				final int extraScroll = firstPos > 0 ? mExtraScroll : v.mListPadding.left;

				smoothScrollBy(firstViewLeft - extraScroll, mScrollDuration);

				mLastSeenPos = firstPos;

				if (firstPos > mTargetPos) {
					v.post(this);
				}
				break;
			}

			case MOVE_UP_BOUND: {
				final int lastViewIndex = v.getChildCount() - 2;
				if (lastViewIndex < 0) {
					return;
				}
				final int lastPos = firstPos + lastViewIndex;

				if (lastPos == mLastSeenPos) {
					// No new views, let things keep going.
					v.post(this);
					return;
				}

				final View lastView = v.getChildAt(lastViewIndex);
				final int lastViewWidth = lastView.getWidth();
				final int lastViewLeft = lastView.getLeft();
				final int lastViewPixelsShowing = listWidth - lastViewLeft;
				mLastSeenPos = lastPos;
				if (lastPos > mBoundPos) {
					smoothScrollBy(-(lastViewPixelsShowing - mExtraScroll), mScrollDuration);
					v.post(this);
				} else {
					final int right = listWidth - mExtraScroll;
					final int lastViewRight = lastViewLeft + lastViewWidth;
					if (right > lastViewRight) {
						smoothScrollBy(-(right - lastViewRight), mScrollDuration);
					}
				}
				break;
			}

			default:
				break;
			}
		}
	}

}


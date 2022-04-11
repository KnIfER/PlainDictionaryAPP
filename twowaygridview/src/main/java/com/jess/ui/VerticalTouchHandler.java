package com.jess.ui;
////////////////////////////////////////////////////////////////////////////////////////
	//  Vertical Touch Handler
	////////////////////////////////////////////////////////////////////////////////////////

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

class VerticalTouchHandler extends TouchHandler {
	/**
	 * The offset to the top of the mMotionPosition view when the down motion event was received
	 */
	int mMotionViewOriginalTop;

	/**
	 * Y value from on the previous motion event (if any)
	 */
	int mLastY;

	/**
	 * The desired offset to the top of the mMotionPosition view after a scroll
	 */
	int mMotionViewNewTop;
	
	VerticalTouchHandler(TwoWayAbsListView v) {
		super(v);
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

		int deltaY;

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
					v.mTouchMode = v.TOUCH_MODE_DOWN;
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
						motionPosition = v.findMotionRowY(y);
						if(mFlingRunnable!=null)
							mFlingRunnable.flywheelTouch();
					}
				}
			}

			if (motionPosition >= 0) {
				// Remember where the motion event started
				View child = v.getChildAt(motionPosition - v.mFirstPosition);
				mMotionViewOriginalTop = child.getTop();
			}
			v.mMotionX = x;
			v.mMotionY = y;
			v.mMotionPosition = motionPosition;
			mLastY = Integer.MIN_VALUE;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final int y = (int) ev.getY();
			deltaY = y - v.mMotionY;
			switch (v.mTouchMode) {
			case TOUCH_MODE_DOWN:
			case TOUCH_MODE_TAP:
			case TOUCH_MODE_DONE_WAITING:
				// Check if we have moved far enough that it looks more like a
				// scroll than a tap
				startScrollIfNeeded(deltaY);
				break;
			case TOUCH_MODE_SCROLL:
				if (PROFILE_SCROLLING) {
					if (!v.mScrollProfilingStarted) {
						Debug.startMethodTracing("JessAbsListViewScroll");
						v.mScrollProfilingStarted = true;
					}
				}

				if (y != mLastY) {
					deltaY -= mMotionCorrection;
					int incrementalDeltaY = mLastY != Integer.MIN_VALUE ? y - mLastY : deltaY;

					// No need to do all this work if we're not going to move anyway
					boolean atEdge = false;
					if (incrementalDeltaY != 0) {
						atEdge = trackMotionScroll(deltaY, incrementalDeltaY);
					}

					// Check to see if we have bumped into the scroll limit
					if (atEdge && v.getChildCount() > 0) {
						// Treat this like we're starting a new scroll from the current
						// position. This will let the user start scrolling back into
						// content immediately rather than needing to scroll back to the
						// point where they hit the limit first.
						int motionPosition = v.findMotionRowY(y);
						if (motionPosition >= 0) {
							final View motionView = v.getChildAt(motionPosition - v.mFirstPosition);
							mMotionViewOriginalTop = motionView.getTop();
						}
						v.mMotionY = y;
						v.mMotionPosition = motionPosition;
						v.invalidate();
					}
					mLastY = y;
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
					if (v.mFirstPosition == 0 && v.getChildAt(0).getTop() >= v.mListPadding.top &&
							v.mFirstPosition + childCount < v.mItemCount &&
							v.getChildAt(childCount - 1).getBottom() <=
									v.getHeight() - v.mListPadding.bottom) {
						v.mTouchMode = TOUCH_MODE_REST;
						reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
					} else {
						final VelocityTracker velocityTracker = v.mVelocityTracker;
						velocityTracker.computeCurrentVelocity(1000);
						final int initialVelocity = (int) velocityTracker.getYVelocity();

						if (Math.abs(initialVelocity) > v.mMinimumVelocity) {
							if (mFlingRunnable == null) {
								mFlingRunnable = new VerticalFlingRunnable();
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
				
				int motionPosition = v.findMotionRowY(y);
				if (touchMode != TOUCH_MODE_FLING && motionPosition >= 0) {
					// User clicked on an actual view (and was not stopping a fling).
					// Remember where the motion event started
					View child = v.getChildAt(motionPosition - v.mFirstPosition);
					mMotionViewOriginalTop = child.getTop();
					v.mMotionX = x;
					v.mMotionY = y;
					v.mMotionPosition = motionPosition;
					v.mTouchMode = TOUCH_MODE_DOWN;
					clearScrollingCache();
				}
				mLastY = Integer.MIN_VALUE;
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
					final int y = (int) ev.getY();
					if (startScrollIfNeeded(y - v.mMotionY)) {
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

	/**
	 * Track a motion scroll
	 *
	 * @param deltaY Amount to offset mMotionView. This is the accumulated delta since the motion
	 *        began. Positive numbers mean the user's finger is moving down the screen.
	 * @param incrementalDeltaY Change in deltaY from the previous event.
	 * @return true if we're already at the beginning/end of the list and have nothing to do.
	 */
	@Override
	boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
		if (DEBUG) Log.i(TAG, "trackMotionScroll() - deltaY: " + deltaY + " incrDeltaY: " + incrementalDeltaY);
		final int childCount = v.getChildCount();
		if (childCount == 0) {
			return true;
		}

		final int firstTop = v.getChildAt(0).getTop();
		final int lastBottom = v.getChildAt(childCount - 1).getBottom();

		final Rect listPadding = v.mListPadding;

		// FIXME account for grid vertical spacing too?
		final int spaceAbove = listPadding.top - firstTop;
		final int end = v.getHeight() - listPadding.bottom;
		final int spaceBelow = lastBottom - end;

		final int height = v.getHeight() - v.getPaddingBottom() - v.getPaddingTop();
		if (deltaY < 0) {
			deltaY = Math.max(-(height - 1), deltaY);
		} else {
			deltaY = Math.min(height - 1, deltaY);
		}

		if (incrementalDeltaY < 0) {
			incrementalDeltaY = Math.max(-(height - 1), incrementalDeltaY);
		} else {
			incrementalDeltaY = Math.min(height - 1, incrementalDeltaY);
		}

		final int firstPosition = v.mFirstPosition;

		if (firstPosition == 0 && firstTop >= listPadding.top && deltaY >= 0) {
			// Don't need to move views down if the top of the first position
			// is already visible
			return true;
		}

		if (firstPosition + childCount == v.mItemCount && lastBottom <= end && deltaY <= 0) {
			// Don't need to move views up if the bottom of the last position
			// is already visible
			return true;
		}

		final boolean down = incrementalDeltaY < 0;

		final boolean inTouchMode = v.isInTouchMode();
		if (inTouchMode) {
			v.hideSelector();
		}

		final int headerViewsCount = v.getHeaderViewsCount();
		final int footerViewsStart = v.mItemCount - v.getFooterViewsCount();

		int start = 0;
		int count = 0;

		if (down) {
			final int top = listPadding.top - incrementalDeltaY;
			for (int i = 0; i < childCount; i++) {
				final View child = v.getChildAt(i);
				if (child.getBottom() >= top) {
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
			final int bottom = v.getHeight() - listPadding.bottom - incrementalDeltaY;
			for (int i = childCount - 1; i >= 0; i--) {
				final View child = v.getChildAt(i);
				if (child.getTop() <= bottom) {
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

		mMotionViewNewTop = mMotionViewOriginalTop + deltaY;
		
		v.mBlockLayoutRequests = true;

		if (count > 0) {
			v.detachViews(start, count);
		}
		v.offsetChildrenTopAndBottom(incrementalDeltaY);

		if (down) {
			v.mFirstPosition += count;
		}
		
		v.invalidate();

		final int absIncrementalDeltaY = Math.abs(incrementalDeltaY);
		if (spaceAbove < absIncrementalDeltaY || spaceBelow < absIncrementalDeltaY) {
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

		return false;
	}

	/**
	 * Attempt to bring the selection back if the user is switching from touch
	 * to trackball mode
	 * @return Whether selection was set to something.
	 */
	@Override
	boolean resurrectSelection() {
		final int childCount = v.getChildCount();

		if (childCount <= 0) {
			return false;
		}

		int selectedTop = 0;
		int selectedPos;
		int childrenTop = v.mListPadding.top;
		int childrenBottom = v.getBottom() - v.getTop() - v.mListPadding.bottom;
		final int firstPosition = v.mFirstPosition;
		final int toPosition = v.mResurrectToPosition;
		boolean down = true;

		if (toPosition >= firstPosition && toPosition < firstPosition + childCount) {
			selectedPos = toPosition;

			final View selected = v.getChildAt(selectedPos - v.mFirstPosition);
			selectedTop = selected.getTop();
			int selectedBottom = selected.getBottom();

			// We are scrolled, don't get in the fade
			if (selectedTop < childrenTop) {
				selectedTop = childrenTop + v.getVerticalFadingEdgeLength();
			} else if (selectedBottom > childrenBottom) {
				selectedTop = childrenBottom - selected.getMeasuredHeight()
				- v.getVerticalFadingEdgeLength();
			}
		} else {
			if (toPosition < firstPosition) {
				// Default to selecting whatever is first
				selectedPos = firstPosition;
				for (int i = 0; i < childCount; i++) {
					final View child = v.getChildAt(i);
					final int top = child.getTop();

					if (i == 0) {
						// Remember the position of the first item
						selectedTop = top;
						// See if we are scrolled at all
						if (firstPosition > 0 || top < childrenTop) {
							// If we are scrolled, don't select anything that is
							// in the fade region
							childrenTop += v.getVerticalFadingEdgeLength();
						}
					}
					if (top >= childrenTop) {
						// Found a view whose top is fully visisble
						selectedPos = firstPosition + i;
						selectedTop = top;
						break;
					}
				}
			} else {
				final int itemCount = v.mItemCount;
				down = false;
				selectedPos = firstPosition + childCount - 1;

				for (int i = childCount - 1; i >= 0; i--) {
					final View child = v.getChildAt(i);
					final int top = child.getTop();
					final int bottom = child.getBottom();

					if (i == childCount - 1) {
						selectedTop = top;
						if (firstPosition + childCount < itemCount || bottom > childrenBottom) {
							childrenBottom -= v.getVerticalFadingEdgeLength();
						}
					}

					if (bottom <= childrenBottom) {
						selectedPos = firstPosition + i;
						selectedTop = top;
						break;
					}
				}
			}
		}
		
		v.mResurrectToPosition = INVALID_POSITION;
		v.removeCallbacks(mFlingRunnable);
		v.mTouchMode = TOUCH_MODE_REST;
		clearScrollingCache();
		v.mSpecificTop = selectedTop;
		selectedPos = v.lookForSelectablePosition(selectedPos, down);
		if (selectedPos >= firstPosition && selectedPos <= v.getLastVisiblePosition()) {
			v.mLayoutMode = v.LAYOUT_SPECIFIC;
			v.setSelectionInt(selectedPos);
			v.invokeOnItemScrollListener();
		} else {
			selectedPos = INVALID_POSITION;
		}
		reportScrollStateChange(TwoWayAbsListView.OnScrollListener.SCROLL_STATE_IDLE);

		return selectedPos >= 0;
	}



	@Override
	protected PositionScroller getPositionScroller() {
		return new VerticalPositionScroller();
	}

	@Override
	protected FlingRunnable getFlingRunnable() {
		return new VerticalFlingRunnable();
	}


	/**
	 * Responsible for fling behavior. Use {@link #start(int)} to
	 * initiate a fling. Each frame of the fling is handled in {@link #run()}.
	 * A FlingRunnable will keep re-posting itself until the fling is done.
	 *
	 */
	private class VerticalFlingRunnable extends FlingRunnable {
		/**
		 * Y value reported by mScroller on the previous fling
		 */
		protected int mLastFlingY;

		@Override
		void start(int initialVelocity) {
			int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingY = initialY;
			mScroller.fling(0, initialY, 0, initialVelocity,
					0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			v.mTouchMode = TOUCH_MODE_FLING;
			v.post(this);

			if (PROFILE_FLINGING) {
				if (!v.mFlingProfilingStarted) {
					Debug.startMethodTracing("AbsListViewFling");
					v.mFlingProfilingStarted = true;
				}
			}
		}

		@Override
		void startScroll(int distance, int duration) {
			int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingY = initialY;
			mScroller.startScroll(0, initialY, 0, distance, duration);
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
				final int y = scroller.getCurrY();

				// Flip sign to convert finger direction to list items direction
				// (e.g. finger moving down means list is moving towards the top)
				int delta = mLastFlingY - y;

				// Pretend that each frame of a fling scroll is a touch scroll
				if (delta > 0) {
					// List is moving towards the top. Use first view as mMotionPosition
					v.mMotionPosition = v.mFirstPosition;
					final View firstView = v.getChildAt(0);
					mMotionViewOriginalTop = firstView.getTop();

					// Don't fling more than 1 screen
					delta = Math.min(v.getHeight() - v.getPaddingBottom() - v.getPaddingTop() - 1, delta);
				} else {
					// List is moving towards the bottom. Use last view as mMotionPosition
					int offsetToLast = v.getChildCount() - 1;
					v.mMotionPosition = v.mFirstPosition + offsetToLast;

					final View lastView = v.getChildAt(offsetToLast);
					mMotionViewOriginalTop = lastView.getTop();

					// Don't fling more than 1 screen
					delta = Math.max(-(v.getHeight() - v.getPaddingBottom() - v.getPaddingTop() - 1), delta);
				}

				final boolean atEnd = trackMotionScroll(delta, delta);

				if (more && !atEnd) {
					v.invalidate();
					mLastFlingY = y;
					v.post(this);
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
		
		private static final int FLYWHEEL_TIMEOUT = 40; // milliseconds
		
		public void flywheelTouch() {
			if(mCheckFlywheel == null) {
				mCheckFlywheel = new Runnable() {
					public void run() {
						final VelocityTracker vt = v.mVelocityTracker;
						if (vt == null) {
							return;
						}

						vt.computeCurrentVelocity(1000, v.mMaximumVelocity);
						final float yvel = -vt.getYVelocity();

						if (Math.abs(yvel) >= v.mMinimumVelocity
								&& isScrollingInDirection(0, yvel)) {
							// Keep the fling alive a little longer
							v.postDelayed(this, FLYWHEEL_TIMEOUT);
						} else {
							endFling();
							v.mTouchMode = TOUCH_MODE_SCROLL;
							reportScrollStateChange(TwoWayAbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
						}
					}
				};
			}
			v.postDelayed(mCheckFlywheel, FLYWHEEL_TIMEOUT);
		}
	}


	class VerticalPositionScroller extends PositionScroller {
		@Override
		public void run() {
			final int listHeight = v.getHeight();
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
				final int lastViewHeight = lastView.getHeight();
				final int lastViewTop = lastView.getTop();
				final int lastViewPixelsShowing = listHeight - lastViewTop;
				final int extraScroll = lastPos < v.mItemCount - 1 ? mExtraScroll : v.mListPadding.bottom;

				smoothScrollBy(lastViewHeight - lastViewPixelsShowing + extraScroll,
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
				final int nextViewHeight = nextView.getHeight();
				final int nextViewTop = nextView.getTop();
				final int extraScroll = mExtraScroll;
				if (nextPos < mBoundPos) {
					smoothScrollBy(Math.max(0, nextViewHeight + nextViewTop - extraScroll),
							mScrollDuration);

					mLastSeenPos = nextPos;
					
					v.post(this);
				} else  {
					if (nextViewTop > extraScroll) {
						smoothScrollBy(nextViewTop - extraScroll, mScrollDuration);
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
				final int firstViewTop = firstView.getTop();
				final int extraScroll = firstPos > 0 ? mExtraScroll : v.mListPadding.top;

				smoothScrollBy(firstViewTop - extraScroll, mScrollDuration);

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
				final int lastViewHeight = lastView.getHeight();
				final int lastViewTop = lastView.getTop();
				final int lastViewPixelsShowing = listHeight - lastViewTop;
				mLastSeenPos = lastPos;
				if (lastPos > mBoundPos) {
					smoothScrollBy(-(lastViewPixelsShowing - mExtraScroll), mScrollDuration);
					v.post(this);
				} else {
					final int bottom = listHeight - mExtraScroll;
					final int lastViewBottom = lastViewTop + lastViewHeight;
					if (bottom > lastViewBottom) {
						smoothScrollBy(-(bottom - lastViewBottom), mScrollDuration);
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


package com.jess.ui;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  Touch Handler
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import static android.view.ViewGroup.PERSISTENT_SCROLLING_CACHE;
import static com.jess.ui.TwoWayAbsListView.TOUCH_MODE_OFF;
import static com.jess.ui.TwoWayAbsListView.TOUCH_MODE_ON;
import static com.jess.ui.TwoWayAbsListView.TOUCH_MODE_REST;
import static com.jess.ui.TwoWayAbsListView.TOUCH_MODE_UNKNOWN;
import static com.jess.ui.TwoWayAdapterView.INVALID_POSITION;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public abstract class TouchHandler {
	final TwoWayAbsListView v;
	/**
	 * Handles scrolling between positions within the list.
	 */
	protected PositionScroller mPositionScroller;

	/**
	 * Handles one frame of a fling
	 */
	protected FlingRunnable mFlingRunnable;

	/**
	 * How far the finger moved before we started scrolling
	 */
	int mMotionCorrection;
	
	protected TouchHandler(TwoWayAbsListView v) {
		this.v = v;
	}
	
	public void onWindowFocusChanged(boolean hasWindowFocus) {

			final int touchMode = v.isInTouchMode() ? TOUCH_MODE_ON : TOUCH_MODE_OFF;

			if (!hasWindowFocus) {
				v.drawCacheEnabled(false);
				if (mFlingRunnable != null) {
					v.removeCallbacks(mFlingRunnable);
					// let the fling runnable report it's new state which
					// should be idle
					mFlingRunnable.endFling();
					//TODO: this doesn't seem the right way to do this.
					if (v.getScrollY() != 0) {
						v.scrollTo(v.getScrollX(), 0);
						//mScrollY = 0;
						v.invalidate();
					}
				}
				// Always hide the type filter
				//dismissPopup();

				if (touchMode == TOUCH_MODE_OFF) {
					// Remember the last selected element
					v.mResurrectToPosition = v.mSelectedPosition;
				}
			} else {
				//if (mFiltered && !mPopupHidden) {
				// Show the type filter only if a filter is in effect
				//    showPopup();
				//}

				// If we changed touch mode since the last time we had focus
				if (touchMode != v.mLastTouchMode && v.mLastTouchMode != TOUCH_MODE_UNKNOWN) {
					// If we come back in trackball mode, we bring the selection back
					if (touchMode == TOUCH_MODE_OFF) {
						// This will trigger a layout
						resurrectSelection();

						// If we come back in touch mode, then we want to hide the selector
					} else {
						v.hideSelector();
						v.mLayoutMode = v.LAYOUT_NORMAL;
						v.layoutChildren();
					}
				}
			}
			
			v.mLastTouchMode = touchMode;
		}


		public boolean startScrollIfNeeded(int delta) {
			// Check if we have moved far enough that it looks more like a
			// scroll than a tap
			final int distance = Math.abs(delta);
			if (distance > v.mTouchSlop) {
				createScrollingCache();
				v.mTouchMode = v.TOUCH_MODE_SCROLL;
				mMotionCorrection = delta;
				final Handler handler = v.getHandler();
				// Handler should not be null unless the TwoWayAbsListView is not attached to a
				// window, which would make it very hard to scroll it... but the monkeys
				// say it's possible.
				if (handler != null) {
					handler.removeCallbacks(v.mPendingCheckForLongPress);
				}
				v.setPressed(false);
				View motionView = v.getChildAt(v.mMotionPosition - v.mFirstPosition);
				if (motionView != null) {
					motionView.setPressed(false);
				}
				reportScrollStateChange(TwoWayAbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
				// Time to start stealing events! Once we've stolen them, don't let anyone
				// steal from us
				v.requestDisallowInterceptTouchEvent(true);
				return true;
			}

			return false;
		}


		public void onTouchModeChanged(boolean isInTouchMode) {
			if (isInTouchMode) {
				// Get rid of the selection when we enter touch mode
				v.hideSelector();
				// Layout, but only if we already have done so previously.
				// (Otherwise may clobber a LAYOUT_SYNC layout that was requested to restore
				// state.)
				if (v.getHeight() > 0 && v.getChildCount() > 0) {
					// We do not lose focus initiating a touch (since TwoWayAbsListView is focusable in
					// touch mode). Force an initial layout to get rid of the selection.
					v.layoutChildren();
				}
			}
		}

		/**
		 * Fires an "on scroll state changed" event to the registered
		 * {@link com.jess.ui.TwoWayAbsListView.OnScrollListener}, if any. The state change
		 * is fired only if the specified state is different from the previously known state.
		 *
		 * @param newState The new scroll state.
		 */
		void reportScrollStateChange(int newState) {
			if (newState != v.mLastScrollState) {
				if (v.mOnScrollListener != null) {
					v.mOnScrollListener.onScrollStateChanged(v, newState);
					v.mLastScrollState = newState;
				}
			}
		}

		/**
		 * Smoothly scroll to the specified adapter position. The view will
		 * scroll such that the indicated position is displayed.
		 * @param position Scroll to this adapter position.
		 */
		public void smoothScrollToPosition(int position) {
			if (mPositionScroller == null) {
				mPositionScroller = getPositionScroller();
			}
			mPositionScroller.start(position);
		}

		/**
		 * Smoothly scroll to the specified adapter position. The view will
		 * scroll such that the indicated position is displayed, but it will
		 * stop early if scrolling further would scroll boundPosition out of
		 * view.
		 * @param position Scroll to this adapter position.
		 * @param boundPosition Do not scroll if it would move this adapter
		 *          position out of view.
		 */
		public void smoothScrollToPosition(int position, int boundPosition) {
			if (mPositionScroller == null) {
				mPositionScroller = getPositionScroller();
			}
			mPositionScroller.start(position, boundPosition);
		}

		/**
		 * Smoothly scroll by distance pixels over duration milliseconds.
		 * @param distance Distance to scroll in pixels.
		 * @param duration Duration of the scroll animation in milliseconds.
		 */
		public void smoothScrollBy(int distance, int duration) {
			if (mFlingRunnable == null) {
				mFlingRunnable = getFlingRunnable();
			} else {
				mFlingRunnable.endFling();
			}
			mFlingRunnable.startScroll(distance, duration);
		}

		protected void createScrollingCache() {
			if (v.mScrollingCacheEnabled && !v.mCachingStarted) {
				v.drawnWithCacheEnabled(true);
				v.drawCacheEnabled(true);
				v.mCachingStarted = true;
			}
		}

		protected void clearScrollingCache() {
			if (v.mClearScrollingCache == null) {
				v.mClearScrollingCache = new Runnable() {
					public void run() {
						if (v.mCachingStarted) {
							v.mCachingStarted = false;
							v.drawnWithCacheEnabled(false);
							if ((v.getPersistentDrawingCache() & PERSISTENT_SCROLLING_CACHE) == 0) {
								v.drawCacheEnabled(false);
							}
							if (!v.isAlwaysDrawnWithCacheEnabled()) {
								v.invalidate();
							}
						}
					}
				};
			}
			v.post(v.mClearScrollingCache);
		}

		/**
		 * Track a motion scroll
		 *
		 * @param delta Amount to offset mMotionView. This is the accumulated delta since the motion
		 *        began. Positive numbers mean the user's finger is moving down or right on the screen.
		 * @param incrementalDelta Change in delta from the previous event.
		 * @return true if we're already at the beginning/end of the list and have nothing to do.
		 */
		abstract boolean trackMotionScroll(int delta, int incrementalDelta);

		/**
		 * Attempt to bring the selection back if the user is switching from touch
		 * to trackball mode
		 * @return Whether selection was set to something.
		 */
		abstract boolean resurrectSelection();


		public abstract boolean onTouchEvent(MotionEvent ev);


		public abstract boolean onInterceptTouchEvent(MotionEvent ev);

		protected abstract PositionScroller getPositionScroller();

		protected abstract FlingRunnable getFlingRunnable();

		/**
		 * Responsible for fling behavior. Use {@link #start(int)} to
		 * initiate a fling. Each frame of the fling is handled in {@link #run()}.
		 * A FlingRunnable will keep re-posting itself until the fling is done.
		 *
		 */
		protected abstract class FlingRunnable implements Runnable {
			/**
			 * Tracks the decay of a fling scroll
			 */
			protected final Scroller mScroller;

			protected Runnable mCheckFlywheel;
			
			public boolean isScrollingInDirection(float xvel, float yvel) {
				final int dx = mScroller.getFinalX() - mScroller.getStartX();
				final int dy = mScroller.getFinalY() - mScroller.getStartY();
				return !mScroller.isFinished() && Math.signum(xvel) == Math.signum(dx) &&
						Math.signum(yvel) == Math.signum(dy);
			}


			FlingRunnable() {
				mScroller = new Scroller(v.getContext());
			}

			abstract void flywheelTouch();

			abstract void start(int initialVelocity);

			abstract void startScroll(int distance, int duration);

			protected void endFling() {
				v.mTouchMode = TOUCH_MODE_REST;

				reportScrollStateChange(TwoWayAbsListView.OnScrollListener.SCROLL_STATE_IDLE);
				clearScrollingCache();
				
				v.removeCallbacks(this);
				
				if (mCheckFlywheel != null) {
					v.removeCallbacks(mCheckFlywheel);
				}
				if (mPositionScroller != null) {
					v.removeCallbacks(mPositionScroller);
				}

				mScroller.abortAnimation();
			}

			public abstract void run();
		}

		abstract class PositionScroller implements Runnable {
			protected static final int SCROLL_DURATION = 400;

			protected static final int MOVE_DOWN_POS = 1;
			protected static final int MOVE_UP_POS = 2;
			protected static final int MOVE_DOWN_BOUND = 3;
			protected static final int MOVE_UP_BOUND = 4;

			protected boolean mVertical;
			protected int mMode;
			protected int mTargetPos;
			protected int mBoundPos;
			protected int mLastSeenPos;
			protected int mScrollDuration;
			protected final int mExtraScroll;

			PositionScroller() {
				mExtraScroll = ViewConfiguration.get(v.mContext).getScaledFadingEdgeLength();
			}

			void start(int position) {
				final int firstPos = v.mFirstPosition;
				final int lastPos = firstPos + v.getChildCount() - 1;

				int viewTravelCount = 0;
				if (position <= firstPos) {
					viewTravelCount = firstPos - position + 1;
					mMode = MOVE_UP_POS;
				} else if (position >= lastPos) {
					viewTravelCount = position - lastPos + 1;
					mMode = MOVE_DOWN_POS;
				} else {
					// Already on screen, nothing to do
					return;
				}

				if (viewTravelCount > 0) {
					mScrollDuration = SCROLL_DURATION / viewTravelCount;
				} else {
					mScrollDuration = SCROLL_DURATION;
				}
				mTargetPos = position;
				mBoundPos = INVALID_POSITION;
				mLastSeenPos = INVALID_POSITION;
				
				v.post(this);
			}

			void start(int position, int boundPosition) {
				if (boundPosition == INVALID_POSITION) {
					start(position);
					return;
				}

				final int firstPos = v.mFirstPosition;
				final int lastPos = firstPos + v.getChildCount() - 1;

				int viewTravelCount = 0;
				if (position <= firstPos) {
					final int boundPosFromLast = lastPos - boundPosition;
					if (boundPosFromLast < 1) {
						// Moving would shift our bound position off the screen. Abort.
						return;
					}

					final int posTravel = firstPos - position + 1;
					final int boundTravel = boundPosFromLast - 1;
					if (boundTravel < posTravel) {
						viewTravelCount = boundTravel;
						mMode = MOVE_UP_BOUND;
					} else {
						viewTravelCount = posTravel;
						mMode = MOVE_UP_POS;
					}
				} else if (position >= lastPos) {
					final int boundPosFromFirst = boundPosition - firstPos;
					if (boundPosFromFirst < 1) {
						// Moving would shift our bound position off the screen. Abort.
						return;
					}

					final int posTravel = position - lastPos + 1;
					final int boundTravel = boundPosFromFirst - 1;
					if (boundTravel < posTravel) {
						viewTravelCount = boundTravel;
						mMode = MOVE_DOWN_BOUND;
					} else {
						viewTravelCount = posTravel;
						mMode = MOVE_DOWN_POS;
					}
				} else {
					// Already on screen, nothing to do
					return;
				}

				if (viewTravelCount > 0) {
					mScrollDuration = SCROLL_DURATION / viewTravelCount;
				} else {
					mScrollDuration = SCROLL_DURATION;
				}
				mTargetPos = position;
				mBoundPos = boundPosition;
				mLastSeenPos = INVALID_POSITION;
				
				v.post(this);
			}

			void stop() {
				v.removeCallbacks(this);
			}

			public abstract void run();
		}


	}


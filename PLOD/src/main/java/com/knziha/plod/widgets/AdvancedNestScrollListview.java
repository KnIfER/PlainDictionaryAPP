/*
 * Copyright (C) 2016 Tobias Rohloff
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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

/**
 * https://github.com/tobiasrohloff/NestedScrollWebView/edit/master/lib/src/main/java/com/tobiasrohloff/view/NestedScrollWebView.java
 */
public class AdvancedNestScrollListview extends ListViewmy implements NestedScrollingChild {
	boolean mNestedScrollEnabled;

	private NestedScrollingChildHelper mChildHelper;

	public AdvancedNestScrollListview(Context context) {
		this(context, null);
	}

	public AdvancedNestScrollListview(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public AdvancedNestScrollListview(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mChildHelper = Utils.getNestedScrollingChildHelper();
		//mChildHelper = new NestedScrollingChildHelper(null);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if(mNestedScrollEnabled) {
			mChildHelper.onTouchEvent(this, event);
			super.dispatchTouchEvent(event);
			return true;
		}
		return super.dispatchTouchEvent(event);
	}

	// NestedScrollingChild

	@Override
	public void setNestedScrollingEnabled(boolean enabled) {
		mNestedScrollEnabled=enabled;
		mChildHelper.setCurrentView(this);
		//ViewCompat.stopNestedScroll(this);
		mChildHelper.setNestedScrollingEnabled(enabled);
	}

	@Override
	public boolean isNestedScrollingEnabled() {
		return mNestedScrollEnabled&&mChildHelper.isNestedScrollingEnabled();
	}

	@Override
	public boolean startNestedScroll(int axes) {
		return mNestedScrollEnabled&&mChildHelper.startNestedScroll(axes, this);
	}

	@Override
	public void stopNestedScroll() {
		mChildHelper.stopNestedScroll();
	}

	@Override
	public boolean hasNestedScrollingParent() {
		return mNestedScrollEnabled&&mChildHelper.hasNestedScrollingParent();
	}

	@Override
	public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
		return mNestedScrollEnabled&&mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
	}

	@Override
	public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
		return mNestedScrollEnabled&&mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
	}

	@Override
	public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
		return mNestedScrollEnabled&&mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
	}

	@Override
	public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
		return mNestedScrollEnabled&&mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
	}
}

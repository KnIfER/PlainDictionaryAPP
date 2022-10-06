package com.knziha.plod.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

/**
 * Created by Meiji on 2016/12/12.
 */

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {
//    private ObjectAnimator outAnimator, inAnimator;
//
//    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    // 垂直滑动
//    @Override
//    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
//        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
//    }
//
//    @Override
//    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
//        if (dy > 0) {// 上滑隐藏
//            if (outAnimator == null) {
//                outAnimator = ObjectAnimator.ofFloat(child, "translationY", 0, child.getHeight());
//                outAnimator.setDuration(200);
//            }
//            if (!outAnimator.isRunning() && child.getTranslationY() <= 0) {
//                outAnimator.start();
//            }
//        } else if (dy < 0) {// 下滑显示
//            if (inAnimator == null) {
//                inAnimator = ObjectAnimator.ofFloat(child, "translationY", child.getHeight(), 0);
//                inAnimator.setDuration(200);
//            }
//            if (!inAnimator.isRunning() && child.getTranslationY() >= child.getHeight()) {
//                inAnimator.start();
//            }
//        }
//    }

	public BottomNavigationBehavior() {
	}

	public BottomNavigationBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
		//说明子控件依赖AppBarLayout
		return dependency instanceof AppBarLayout;
	}


	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
		child.setTranslationY(Math.abs(dependency.getTop()));
		return true;
	}
}
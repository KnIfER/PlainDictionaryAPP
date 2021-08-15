package com.knziha.plod.PlainUI;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.CallSuper;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.Utils;

public class PlainAppPanel extends SettingsPanel {
	protected MainActivityUIBase a;
	protected boolean bShouldInterceptClickListener = true;
	protected boolean showPopOnAppbar = true;
	
	public PlainAppPanel(MainActivityUIBase a) {
		super(a, a.root, a.app_panel_bottombar_height/2, a.opt, a);
		this.a = a;
		if (!showInPopWindow) {
			Utils.embedViewInCoordinatorLayout(settingsLayout, !showPopOnAppbar);
		}
	}
	
	@Override
	protected void showPop() {
		if (pop==null) {
			pop = new PopupWindow(a);
			pop.setContentView(settingsLayout);
		}
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		a.embedPopInCoordinatorLayout(pop);
	}
	
	@CallSuper
	@Override
	protected void onDismiss() {
		if(bShouldInterceptClickListener) {
			if (a.mInterceptorListener==this) {
				a.mInterceptorListener = null;
			}
			decorateInterceptorListener(false);
		}
	}
	
	protected void decorateInterceptorListener(boolean install) { }
	
	@Override
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss) {
		if(!bIsShowing && bShouldInterceptClickListener) {
			a.mInterceptorListener = this;
			decorateInterceptorListener(true);
		}
		boolean ret = super.toggle(root, parentToDismiss);
		if (ret) {
			a.HideSelectionWidgets(true);
			a.settingsPanel = this;
		} else if(a.settingsPanel == this){
			a.hideSettingsPanel();
		}
		return ret;
	}
	
	@Override
	public void onAnimationEnd(Animator animation) {
		super.onAnimationEnd(animation);
		ValueAnimator va = (ValueAnimator) animation;
		if (!bIsShowing && (va==null || va.getAnimatedFraction()==1)) {
			boolean b1 = a.settingsPanel == this;
			if (b1) {
				a.hideSettingsPanel();
			}
			if (b1 || a.settingsPanel==null) {
				a.HideSelectionWidgets(false);
			}
		}
	}
	
}

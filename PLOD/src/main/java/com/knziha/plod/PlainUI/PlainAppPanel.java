package com.knziha.plod.PlainUI;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.CallSuper;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.ViewUtils;

public class PlainAppPanel extends SettingsPanel {
	protected MainActivityUIBase a;
	protected boolean bShouldInterceptClickListener = true;
	protected boolean showPopOnAppbar = true;
	int MainColorStamp;
	View bgView;
	
	public PlainAppPanel(MainActivityUIBase a) {
		super(a, a.root, a.app_panel_bottombar_height/2, a.opt, a);
		this.a = a;
		if (!showInPopWindow) {
			ViewUtils.embedViewInCoordinatorLayout(settingsLayout, !showPopOnAppbar);
		}
		MainColorStamp = a.MainAppBackground;
	}
	
	@Override
	public void refresh() {
		super.refresh();
		// refresh colors
		if (MainColorStamp!=a.MainAppBackground) {
			setPresetBgColorType(mBackgroundColorType);
			if(bgView!=null) {
				bgView.getBackground().setColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
			}
			settingsLayout.setBackgroundColor(mBackgroundColor);
			MainColorStamp = a.MainAppBackground;
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

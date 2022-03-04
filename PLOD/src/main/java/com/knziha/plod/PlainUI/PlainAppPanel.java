package com.knziha.plod.PlainUI;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.CallSuper;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.ViewUtils;

public class PlainAppPanel extends SettingsPanel {
	protected MainActivityUIBase a;
	protected boolean bShouldInterceptClickListener = true;
	protected boolean showPopOnAppbar = true;
	int MainColorStamp;
	View bgView;
	protected boolean bPopIsFocusable;
	protected ViewGroup settingsLayoutHolder;
	public View bottombar;
	
	public PlainAppPanel(MainActivityUIBase a) {
		super(a, a.root, a.app_panel_bottombar_height/2, a.opt, a);
		this.a = a;
		if (showType==0 && settingsLayout!=null) {
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
	protected void showPop(ViewGroup root) {
		if (pop==null) {
			pop = new PopupWindow(a);
			pop.setContentView(settingsLayout);
			if(bPopIsFocusable) {
				pop.setFocusable(true);
				pop.setOnDismissListener(() -> {
					bSuppressNxtAnimation=true;
					dismiss();
				});
			}
		}
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		a.embedPopInCoordinatorLayout(this, pop, bottomPadding, root);
	}
	
	public int getNavbarHeight() {
		int result = 0;
		try {
			int sid = a.mResource.getIdentifier("config_showNavigationBar", "bool", "android");
			if (a.mResource.getBoolean(sid)){
				int resourceId = a.mResource.getIdentifier("navigation_bar_height", "dimen", "android");
				result = a.mResource.getDimensionPixelSize(resourceId);
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return result;
	}
	
	protected void showDialog() {
		if (dialog==null) {
			dialog = new Dialog(a);
			dialog.setOnDismissListener(dialog -> dismissImmediate());
			settingsLayoutHolder = new FrameLayout(a);
			settingsLayoutHolder.setOnClickListener(v -> dismiss());
		}
		ViewUtils.addViewToParent(settingsLayout, settingsLayoutHolder);
		dialog.setContentView(settingsLayoutHolder);
		dialog.show();
		
		int padbot = bottomPadding;
		if(padbot!=0) {
			if(bottombar!=null) {
				padbot = bottombar.getHeight();
			} else {
				padbot = a.bottombar!=null?a.bottombar.getHeight():a.app_panel_bottombar_height;
			}
			settingsLayoutHolder.setPadding(0,0,0,padbot+getNavbarHeight());
			bottomPadding = padbot;
		}
		
		Window window = dialog.getWindow();
		window.setDimAmount(0);
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.width = MATCH_PARENT;
		layoutParams.height = MATCH_PARENT;
		layoutParams.horizontalMargin = 0;
		layoutParams.verticalMargin = 0;
		window.setAttributes(layoutParams);
		window.getDecorView().setBackground(null);
		window.getDecorView().setPadding(0,0,0,0);
		
		
		Toastable_Activity.setStatusBarColor(window, a.MainAppBackground);
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		
		View t = window.findViewById(android.R.id.title);
		if(t!=null) t.setVisibility(View.GONE);
		int id = Resources.getSystem().getIdentifier("titleDivider","id", "android");
		if(id!=0){
			t = window.findViewById(id);
			if(t!=null) t.setVisibility(View.GONE);
		}
		if(t!=null) t.setVisibility(View.GONE);
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
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss, int forceShowType) {
		if(!bIsShowing && bShouldInterceptClickListener) {
			a.mInterceptorListener = this;
			decorateInterceptorListener(true);
		}
		if(forceShowType==-2 && !bIsShowing && a.settingsPanel!=null) {
			forceShowType = a.settingsPanel.lastShowType;
		}
		if(!bIsShowing && root==null) {
			if(a.settingsPanel!=null) {
				root = a.settingsPanel.getViewRoot();
				//a.showT("auto_find_root::"+root+","+(root==a.root)+","+a.settingsPanel);
			}
			if(root==null)
				root = a.root;
		}
		boolean ret = super.toggle(root, parentToDismiss, forceShowType);
		if (ret) {
			a.HideSelectionWidgets(true);
			a.settingsPanel = this;
			a.settingsPanels.add(this);
		}
		else a.hideSettingsPanel(this);
		return ret;
	}
	
	private ViewGroup getViewRoot() {
		ViewParent vp = settingsLayout.getParent();
		if(vp instanceof ViewGroup) return (ViewGroup) vp;
		return null;
	}
	
	@Override
	public void onAnimationEnd(Animator animation) {
		super.onAnimationEnd(animation);
		ValueAnimator va = (ValueAnimator) animation;
		if (!bIsShowing && (va==null || va.getAnimatedFraction()==1)) {
			boolean b1 = a.settingsPanel == this;
			a.hideSettingsPanel(this);
			if (b1 || a.settingsPanel==null) {
				a.HideSelectionWidgets(false);
			}
		}
	}
	
}

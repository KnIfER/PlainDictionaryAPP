package com.knziha.plod.PlainUI;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.CallSuper;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

public class PlainAppPanel extends SettingsPanel implements PlainDialog.BackPrevention{
	public /*final*/ WebViewListHandler weblistHandler;
	protected MainActivityUIBase a;
	protected boolean bShouldInterceptClickListener = true;
	protected boolean showPopOnAppbar = true;
	protected int MainColorStamp;
	protected View bgView;
	protected boolean bPopIsFocusable;
	protected boolean resizeDlg = false;
	protected ViewGroup settingsLayoutHolder;
	public View bottombar;
	protected int MainAppBackground;
	protected boolean tweakDlgScreen = true;
	
	public PlainAppPanel() {
		super(null, null, null, null, null);
		lastShowType = -1;
	}
	
	public PlainAppPanel(MainActivityUIBase a, boolean init) {
		super(init?a:null, a.root, a.app_panel_bottombar_height/2, a.opt, a);
		this.a = a;
//		if (showType==0 && settingsLayout!=null) {
//			ViewUtils.embedViewInCoordinatorLayout(settingsLayout, !showPopOnAppbar);
//		}
		if (init) {
			MainColorStamp = a.MainAppBackground;
		}
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
				pop.setOnDismissListener(this::dismissImmediate);
			}
		}
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		a.embedPopInCoordinatorLayout(this, pop, true, root);
	}
	
	public int getNavbarHeight() {
		int result = 0;
		try {
			int sid = a.mResource.getIdentifier("config_showNavigationBar", "bool", "android");
			if (a.mResource.getBoolean(sid)){ // wrong
				int resourceId = a.mResource.getIdentifier("navigation_bar_height", "dimen", "android");
				result = a.mResource.getDimensionPixelSize(resourceId);
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return result;
	}
	
	protected void showDialog() {
		if (dialogDismissListener==null) {
			dialogDismissListener = dialog -> dismissImmediate();
		}
		if (dialog==null) {
			final PlainDialog d = new PlainDialog(a);
			d.mBackPrevention = this;
			if(settingsLayoutHolder==null) {
				settingsLayoutHolder = new FrameLayout(a);
				settingsLayoutHolder.setOnClickListener(v -> dismiss());
			} else {
				ViewUtils.removeView(settingsLayoutHolder);
			}
			d.setOnDismissListener(dialogDismissListener);
			dialog = d;
		}
		if(settingsLayoutHolder!=settingsLayout)
			ViewUtils.addViewToParent(settingsLayout, settingsLayoutHolder);
		if (tweakDlgScreen)
			dialog.setContentView(settingsLayoutHolder);
		ViewUtils.ensureWindowType(dialog, a, dialogDismissListener);
//		if (resizeDlg)
//			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		
		dialog.show();
		
		if (tweakDlgScreen) {
			int padbot = bottomPadding;
			if(padbot!=0) {
				if(bottombar!=null) {
					padbot = bottombar.getHeight();
				} else {
					padbot = a.bottombar!=null?a.bottombar.getHeight():a.app_panel_bottombar_height;
				}
				settingsLayoutHolder.setPadding(0,a.root.getPaddingTop(),0,padbot);
				bottomPadding = padbot;
			}
			
			Window window = dialog.getWindow();
			ViewUtils.makeFullscreenWnd(window);
			
			Toastable_Activity.setStatusBarColor(window, a.MainAppBackground);
			//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
			
			View t = window.findViewById(android.R.id.title);
			if(t!=null) t.setVisibility(View.GONE);
			int id = Resources.getSystem().getIdentifier("titleDivider","id", "android");
			if(id!=0){
				t = window.findViewById(id);
				if(t!=null) t.setVisibility(View.GONE);
			}
		}
		
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
	
	
	public void toggleDummy(MainActivityUIBase a) {
		if (a!=null) {
			if (bIsShowing=!bIsShowing) {
				a.settingsPanel = this;
				a.settingsPanels.add(this);
			}
			else {
				a.hideSettingsPanel(this);
			}
		}
	}
	
	@Override
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss, int forceShowType) {
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
			if(bShouldInterceptClickListener) {
				a.mInterceptorListener = this;
				decorateInterceptorListener(true);
			}
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
	
	public void refreshSoftMode(int height) {
	
	}
	
	public void resize() {
		if (isVisible()) {
			if (lastShowType == 2) {
				Window window = dialog.getWindow();
				ViewUtils.makeFullscreenWnd(window);
			}
			else if (lastShowType == 1) {
				a.embedPopInCoordinatorLayout(this, pop, false, a.root);
			}
			else if (lastShowType==0) {
//				View v = settingsLayout.getChildAt(0);
//				v.setPadding(0, 0, 0, mInnerBottomPadding = padding);
				// setInnerBottomPadding
			}
		}
	}
	
	
	
	@Override
	public boolean onBackPressed() {
		if (lastShowType==2 && a!=null && a.settingsPanel != this) { // todo ???
			int idx = a.settingsPanels.indexOf(this);
			if (idx < a.settingsPanels.size()-1) {
				a.onBackPressed();
				return true;
			}
		}
		if (weblistHandler!=null && PDICMainAppOptions.getUseBackKeyClearWebViewFocus()) {
			WebViewmy wv = weblistHandler.dictView;
			//CMN.debug("onBackPressed::wv==", wv);
			if (wv!=null && (wv.bIsActionMenuShown||ViewUtils.isVisibleV2(weblistHandler.toolsBtn))) {
				wv.clearFocus();
				if (wv.bIsActionMenuShown) {
					wv.evaluateJavascript("getSelection().collapseToStart()", null);
				} else {
					wv.weblistHandler.initQuickTranslatorsBar(false, false);
				}
				return true;
			}
		}
		return false;
	}
	
	public void onResume() {
	
	}
	
	public void onPause() {
	
	}
}

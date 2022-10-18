package com.knziha.plod.PlainUI;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.view.VU;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.AdvancedNestFrameView;
import com.knziha.plod.widgets.AdvancedNestScrollLinerView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

public class NewTitlebar {
	AppBarLayout.LayoutParams rawToolbarParams;
	LinearLayout.LayoutParams newToolbarParams;
	LinearLayout.LayoutParams newTitlebarParams;
	public LinearLayout toolbarHolder;
	public LinearLayout titleBarHolder;
	public FrameLayout titleFrame;
	public AdvancedNestScrollLinerView titleBar;
	MainActivityUIBase a;
	float frac = 0.8f;
	public boolean isActived;
	public NewTitlebar(MainActivityUIBase a) {
		this.a = a;
	}
	
	public void init() {
		if (toolbarHolder == null) {
			toolbarHolder = new LinearLayout(a);
			titleBarHolder = new LinearLayout(a);
			titleBarHolder.setOrientation(LinearLayout.VERTICAL);
			
			View v = a.getLayoutInflater().inflate(R.layout.contentview_item_t, null);
			titleBar = v.findViewById(R.id.titleBar);
			titleBar.setNestedScrollingEnabled(true);
			titleFrame = new FrameLayout(a);
			titleFrame.setLayoutParams(newTitlebarParams=new LinearLayout.LayoutParams(-1, (int) (titleBar.getLayoutParams().height*frac)));
			titleBar.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
			ViewUtils.addViewToParent(titleBar, titleFrame);
			
			titleBarHolder.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 5));
		}
	}
	
	public void Activate() {
		init();
		ViewUtils.replaceView(toolbarHolder, a.toolbar);
		
		if (rawToolbarParams == null) {
			rawToolbarParams = (AppBarLayout.LayoutParams) a.toolbar.getLayoutParams();
			newToolbarParams = new LinearLayout.LayoutParams(rawToolbarParams);
		}
		
		a.toolbar.setLayoutParams(newToolbarParams);
		
		ViewGroup schTools = a.schTools.rootPanel;
		ViewUtils.addViewToParent(schTools, toolbarHolder);
		ViewUtils.addViewToParent(titleBarHolder, toolbarHolder);
		ViewUtils.addViewToParent(a.toolbar, titleBarHolder);
		ViewUtils.removeView(titleFrame);
		ViewUtils.addViewToParent(titleFrame, titleBarHolder);
		
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) schTools.getLayoutParams();
		lp.height = -1;
		lp.width = 0;
		lp.weight = 2;
		
		int barSz = (int) (a.actionBarSize * frac);
		a.appbar.getLayoutParams().height = barSz + newTitlebarParams.height;
		a.toolbar.getLayoutParams().height = barSz;
		
		toolbarHolder.getLayoutParams().width = -1;
		toolbarHolder.getLayoutParams().height = -1;
		
		isActived = true;
	}
	
	public void resize() {
		if (isActived) {
			int barSz = (int) (a.actionBarSize * frac);
			newTitlebarParams.height = (int) (a.mResource.getDimension(R.dimen.dictitle) * frac * a.barSzRatio);
			a.appbar.getLayoutParams().height = (int) (barSz * a.barSzRatio) + newTitlebarParams.height;
			a.toolbar.getLayoutParams().height = (int) (barSz * a.barSzRatio);
		} else {
			int barSz = (int) (a.actionBarSize * a.barSzRatio);
			a.toolbar.getLayoutParams().height=-1;
			a.appbar.getLayoutParams().height=barSz;
		}
		a.refreshContentBow(a.opt.isContentBow(), a.appbar.getLayoutParams().height);
		a.appbar.requestLayout();
	}
	
	public void setTitlebar(WebViewmy mWebView) {
		AdvancedNestFrameView titleBarReal = mWebView.titleBar;
		if(titleBarReal!=null) {
			CMN.recurseLog(titleBarReal);
			CMN.Log();
			CMN.Log();
			CMN.Log();
			CMN.recurseLog(titleBar);
			titleBar.setBackground(titleBarReal.getBackground());
			//titleBar.setPadding(0,0,0,0);
			for (int i = 0; i < titleBar.getChildCount(); i++) {
				try {
					View realView = titleBarReal.getChildAt(i);
					View thisView = titleBar.getChildAt(i);
					VU.setVisible(thisView, VU.isVisible(realView));
					if (thisView instanceof FlowTextView) {
						((FlowTextView) thisView).setText(((FlowTextView) realView).getText());
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
			//VU.setVisible(titleBarReal, false);
		}
	}
}

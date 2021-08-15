package com.knziha.plod.PlainUI;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.DescriptiveImageView;
import com.knziha.plod.widgets.Utils;

public class MenuGrid extends PlainAppPanel {
	MainActivityUIBase a;
	DisplayMetrics dm;
	
	private ScrollView menu_grid;
	private boolean MenuClicked;
	private int lastWidth;
	private int lastHeight;
	
	public MenuGrid(MainActivityUIBase a) {
		super(a);
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		a=(MainActivityUIBase) context;
		showPopOnAppbar = true;
		
		showInPopWindow = true;
		mBackgroundColor = 0x20FFEEEE; // 0x3E8F8F8F
		dm = a.dm;
		
		DescriptiveImageView.createTextPainter(true);
		
		settingsLayout = (ViewGroup) LayoutInflater.from(a).inflate(R.layout.menu_grid, root, false);
		settingsLayout.setOnClickListener(this);
		
		menu_grid = settingsLayout.findViewById(R.id.menu_grid);
		
		menu_grid.getBackground().setColorFilter(a.MainBackground, PorterDuff.Mode.SRC_IN);
		
		ViewGroup svp = (ViewGroup) menu_grid.getChildAt(0);
		//menu_grid.setTranslationY(TargetTransY + legalMenuTransY);
		Utils.setOnClickListenersOneDepth(svp, a, 999, null);
		//refreshMenuGridSize(true);
	}
	
	@Override
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss) {
		boolean ret = super.toggle(root, parentToDismiss);
		//menu_grid.focusable=ret;
		if (lastWidth!=a.root.getWidth() || lastHeight!=a.root.getHeight()) {
			refreshMenuGridSize(true);
		}
		return ret;
	}
	
	@Override
	protected void decorateInterceptorListener(boolean install) {
	
	}
	
	@Override
	public void onClick(View v) {
		if(MenuClicked) {
			return;
		}
		dismiss();
		if (v.getTag() instanceof String) {
			a.showT(v.getTag());
		}
		//MenuClicked = true;
		switch (v.getId()) {
			case R.drawable.ic_menu_24dp: {
				a.mInterceptorListenerHandled=true;
				dismiss();
			} break;
			case R.id.browser_widget9: {
				a.mInterceptorListenerHandled=true;
				a.moveTaskToBack(false);
				hide();
			} break;
			/* 历史 */
			case R.id.root: {
				dismiss();
			} break;
		}
		//v.postDelayed(() -> toggleMenuGrid(true), 250);
	}
	
	public void refreshMenuGridSize(boolean init) {
		if (bIsShowing||init) {
			lastWidth=a.root.getWidth();
			lastHeight=a.root.getHeight();
			int w = lastWidth;
			if(w>lastHeight) {
				w -= GlobalOptions.density*18;
			}
			int maxWidth = Math.min(w, (int) (GlobalOptions.density*560));
			if(Utils.actualLandscapeMode(a)) {
				maxWidth = Math.min(lastHeight, maxWidth);
			}
			//menu_grid.setBackgroundResource(R.drawable.frame_top_rounded);
			ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menu_grid.getLayoutParams();
			int maxHeight = a.root.getHeight();
			layoutParams.width=maxWidth;
			layoutParams.height= GlobalOptions.density*200>maxHeight?maxHeight+mInnerBottomPadding:-2;
			//layoutParams.height= (int) (GlobalOptions.density*100)+mInnerBottomPadding;
			//layoutParams.height= 721;
			//CMN.Log("refreshMenuGridSize::", layoutParams.width, layoutParams.height, maxHeight, mInnerBottomPadding);
			if(GlobalOptions.isLarge) {
				layoutParams.setMarginEnd((int) (15* GlobalOptions.density));
				int pad = (int) (GlobalOptions.density*8);
				int HPad = (int) (pad*2.25);
				menu_grid.setPadding(HPad, pad/2, HPad, pad*2+mInnerBottomPadding);
			} else {
				menu_grid.setPadding(0, 0, 0, mInnerBottomPadding);
			}
			menu_grid.requestLayout();
		}
	}
}

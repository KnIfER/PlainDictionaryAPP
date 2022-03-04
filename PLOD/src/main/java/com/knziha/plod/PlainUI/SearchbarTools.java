package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.ActivityMainBinding;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchbarTools extends PlainAppPanel {
	protected PDICMainActivity a;
	
	public SearchbarTools(PDICMainActivity a) {
		super(a);
		this.bottomPadding = 0;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.bAutoRefresh = true;
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(PDICMainActivity) context;
			mBackgroundColor = 0;
		}
		if (settingsLayout==null) {
			ActivityMainBinding uiData = a.UIData;
			ViewUtils.setOnClickListenersOneDepth(uiData.etSearchBar, this, 999, 0, null);
			settingsLayout = uiData.etSearchBar;
			
			if(true) {
				((ViewGroup)uiData.etBack.getParent()).setBackgroundColor(a.MainAppBackground);
				int fc = ColorUtils.blendARGB(Color.WHITE,a.MainBackground, 0.45f) & 0xf0ffffff;
				uiData.etSearchBar.getChildAt(0).setBackgroundColor(fc);
				uiData.etSearchBar.getChildAt(2).setBackgroundColor(fc);
				LayerDrawable ld = (LayerDrawable)uiData.showSearchHistoryDropdown.getBackground();
				PorterDuffColorFilter cf = new PorterDuffColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
				for (int i = 0; i < ld.getNumberOfLayers()-1; i++) {
					ld.getDrawable(i).setColorFilter(cf);
				}
			}
		}
	}
	
	@Override
	public void refresh() {
		if (settingsLayout!=null) {
			ViewGroup.LayoutParams lp = settingsLayout.getLayoutParams();
			if(lp instanceof ViewGroup.MarginLayoutParams) {
				((ViewGroup.MarginLayoutParams) lp).topMargin = a.UIData.toolbar.getHeight();
			}
			
		}
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
		CMN.Log("onclick::", v);
		if (v.getId() == R.id.show_search_history_dropdown_bg || v.getId() == R.id.etBack) {
			dismiss();
		} else {
//			dismiss();
//			if (v.getId() != R.drawable.ic_menu_24dp) {
//				a.mInterceptorListenerHandled = true;
//			}
		}
	}
	
	@Override
	protected void onDismiss() {
		CMN.Log("onDismiss::");
		super.onDismiss();
		a.imm.hideSoftInputFromWindow(a.UIData.etSearch.getWindowToken(),0);
	}
}

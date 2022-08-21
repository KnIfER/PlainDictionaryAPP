package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.DarkToggleButton;
import com.knziha.plod.widgets.SwipeBackLayout;

public class NightModeSwitchPanel extends PlainAppPanel {
	protected MainActivityUIBase a;
	protected int mSettingsChanged;
	protected static int mScrollY;
	private DarkToggleButton dayNightArt;
	
	public NightModeSwitchPanel(MainActivityUIBase a) {
		super(a, true);
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		a=(MainActivityUIBase) context;
		mBackgroundColor = 0;
		setShowInPop();
		if (settingsLayout==null) {
			SwipeBackLayout view = (SwipeBackLayout) LayoutInflater.from(context).inflate(R.layout.night_mode_pane, root, false);
			dayNightArt = view.findViewById(R.id.dayNightArt);
			dayNightArt.setAnimationListener(progress -> {
				if (progress>=0.97) {
					dayNightArt.stopAnimation();
					dismiss();
				}
			});
			view.findViewById(R.id.dayNightBtn).setOnClickListener(this);
			view.setCallBack(() -> {
				bSuppressNxtAnimation = true;
				dismiss();
				bSuppressNxtAnimation = false;
			});
			settingsLayout = view;
		}
	}
	
	@Override
	public void refresh() {
		if (dayNightArt.stateIsNightMode()!=GlobalOptions.isDark) {
			dayNightArt.toggle(false);
		} else {
			dayNightArt.abortAnimation();
		}
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dayNightBtn) {
			dayNightArt.toggle(true);
			a.toggleDarkMode();
		} else {
			dismiss();
			if (v.getId() != R.drawable.ic_menu_24dp) {
				a.mInterceptorListenerHandled = true;
			}
		}
	}
	
	@Override
	protected void onDismiss() {
		//CMN.Log("onDismiss::", mSettingsChanged);
		super.onDismiss();
		if (mSettingsChanged!=0) {
			//a.currentViewImpl.checkSettings(true, true);
			mSettingsChanged=0;
		}
		mScrollY = settingsLayout.getScrollY();
	}
}

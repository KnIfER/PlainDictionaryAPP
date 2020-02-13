package com.knziha.plod.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.knziha.plod.PlainDict.Toastable_Activity;

public class SimpleDialog extends Dialog {
	public void decorBright() {
		if(getWindow()!=null)
			getWindow().getDecorView().setVisibility(View.VISIBLE);
	}

	public interface BCL{
		void onBackPressed();
		void onActionModeStarted(ActionMode mode);
		public boolean onKeyDown(int keyCode, @NonNull KeyEvent event);
	}
	public BCL mBCL;
	public SimpleDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		super.onActionModeStarted(mode);
		if(mBCL!=null) mBCL.onActionModeStarted(mode);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(mBCL!=null){
			mBCL.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
		if(mBCL!=null && mBCL.onKeyDown(keyCode, event))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void show() {
		super.show();
		fix_m_full_screen();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		fix_m_full_screen();
	}

	private void fix_m_full_screen() {
		Window win = getWindow();
		if(win!=null) {
			boolean fullScreen=false;
			boolean hideNavigation=false;
			Toastable_Activity.do_fix_full_screen(getWindow().getDecorView(), fullScreen, hideNavigation);
			if (fullScreen)
				win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			else
				win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

}

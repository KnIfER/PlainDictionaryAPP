package com.knziha.plod.tesseraction;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.knziha.plod.plaindict.Toastable_Activity;

public /*final*/ class QRActivity extends Toastable_Activity {
	private boolean systemInitialized;
	public Manager mManager;
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//dm.setTo(getResources().getDisplayMetrics());
		if (mManager != null) {
			mManager.readScreenOrientation(this, true);
		}
		//CMN.Log("onConfigurationChanged", dm.widthPixels+"x"+dm.heightPixels, mManager.isPortrait);
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
//		try {
//			new Tesseraction().init(this);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
		
		int type = 1;//mManager.opt.getLaunchCameraType();
		mManager = new Manager(opt);
		mManager.init(this, null, null);
		mManager.showMainMenu(this, 1);
		
		
		//mManager.suspensed = type==0||type==2&&!mManager.opt.getRememberedLaunchCamera();
		
//		if(!mManager.suspensed) {
//			mManager.checkPermission(this, 1);
//		}
		
		Window win = getWindow();
		
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		if(opt.getLaunchCameraType()==2&&opt.getRememberedLaunchCamera()) {
//			ui_camera_btn_vis(2);
//		}
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//		if(mManager.suspensed) {
//			mManager.suspenseCameraUI();
//		} else {
//			mManager.openCamera();
//		}
//		mManager.refreshUI();
		
		updateOrientation();
		systemInitialized =true;
		
		setStatusBarColor(getWindow());
	}
	
	private void setStatusBarColor(Window window){
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		if(Build.VERSION.SDK_INT>=21) {
			window.setStatusBarColor(Color.TRANSPARENT);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}
	
	private void updateOrientation() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==3) { //ACTION_GET_CONTENT
			if(resultCode==Activity.RESULT_OK && data!=null) {
				mManager.openImage(data.getData());
			}
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		//super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		boolean hasPermission=false;
		for (int i=0; i<grantResults.length; i++) {
			if(grantResults[i]==0) {
				hasPermission=true;
				break;
			}
		}
		if(requestCode==1) {
			if(!hasPermission) {
				finish();
			}
		} else if(requestCode==2) {
			if(hasPermission) {
				mManager.openCamera();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(systemInitialized) {
			mManager.resumeCamera();
		}
	}
	
	@Override
	protected void onPause() {
		mManager.pauseCamera();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mManager.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(mManager.onBack()) {
			return;
		}
		super.onBackPressed();
	}
	
	private void showT(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
//	@Override
//	public void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val) {
//		CMN.Log("processOptionChanged", processId, val);
//		switch (processId) {
//			case 1:
//				syncQRFrameSettings(true);
//			break;
//			case 2:
//				ui_camera_btn_vis(val);
//			break;
//			case 3:
//				requestedResetHints=true;
//			break;
//		}
//	}
	
	// ui_camera_btn_vis
}

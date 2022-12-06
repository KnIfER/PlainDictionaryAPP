package com.knziha.plod.plaindict;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.drawerlayout.widget.DrawerLayout;

import com.knziha.plod.plaindict.databinding.ActivityMainBinding;
import com.knziha.plod.widgets.RomUtils;
import com.knziha.plod.widgets.ViewUtils;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.util.HashMap;

public class VersionUtils {
	public static final boolean AnnotOff = false;
	public static boolean firstInstall = false;
	public static final int UpgradeCode = 1024;
	
	public static void checkVersion(PDICMainAppOptions opt) {
		if(PDICMainAppOptions.checkVersionBefore_4_0()) {
//			PDICMainAppOptions.setSimpleMode(false);
//			PDICMainAppOptions.uncheckVersionBefore_4_0(false);
//			PDICMainAppOptions.setLeaveContentBlank(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT);
//			PDICMainAppOptions.setAnimateContents(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT);
//			PDICMainAppOptions.setRebuildToast(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P);
		}
		if(PDICMainAppOptions.checkVersionBefore_4_9()) {
//			PDICMainAppOptions.uncheckVersionBefore_4_9(false);
//			opt.setPinDialog_2(true);
		}
		if(PDICMainAppOptions.checkVersionBefore_5_0()) {
		
		}
		PDICMainAppOptions.setUseDatabaseV2(true);
		if(PDICMainAppOptions.checkVersionBefore_5_7()) {
			CMN.debug("初始化版本!!!");
			firstInstall = true;
			PDICMainAppOptions.uncheckVersionBefore_4_0(true);
			PDICMainAppOptions.uncheckVersionBefore_4_9(true);
			PDICMainAppOptions.uncheckVersionBefore_5_0(true);
			PDICMainAppOptions.uncheckVersionBefore_5_2(true);
			PDICMainAppOptions.uncheckVersionBefore_5_3(true);
			PDICMainAppOptions.uncheckVersionBefore_5_4(true);
			opt.setTypeFlag_11_AtQF(0, 0);
			opt.setTypeFlag_11_AtQF(0, 2);
			opt.setTypeFlag_11_AtQF(0, 4);
			opt.setTypeFlag_11_AtQF(0, 6);
			opt.setTypeFlag_11_AtQF(0, 8);
			opt.setTypeFlag_11_AtQF(0, 22);
			opt.setInPeruseMode(false);
			opt.menuOverlapAnchor(GlobalOptions.isSmall);
			PDICMainAppOptions.setUseBackKeyGoWebViewBack(false);
			opt.slidePage1D(true);
			opt.slidePageMD(true);
			opt.slidePageMd(true);
			opt.setTurnPageEnabled(true);
			opt.schPageNavAudioKey(false);

			opt.setUseBackKeyGoWebViewBack(false);
			opt.setUseBackKeyGoWebViewBack1(false);
			opt.tapSchPageAutoReadEntry(false);

			PDICMainAppOptions.pageSchWild(true);
			PDICMainAppOptions.uncheckVersionBefore_5_7(false);

			PDICMainAppOptions.showPrvBtn(true);
			PDICMainAppOptions.showNxtBtn(true);
			PDICMainAppOptions.showNxtBtnSmall(false);
			PDICMainAppOptions.showPrvBtnSmall(false);

			PDICMainAppOptions.bottomNavWeb1(false);

			PDICMainAppOptions.pinPDic(true);
			PDICMainAppOptions.setShowPinPicBook(true);
			PDICMainAppOptions.darkSystem(Build.VERSION.SDK_INT>=29 && !RomUtils.isMIUI());
			PDICMainAppOptions.setEnableSuperImmersiveScrollMode(GlobalOptions.isSmall);

			PDICMainAppOptions.restoreLastSch(false); // 默认不恢复
			PDICMainAppOptions.setAllowPlugResSame(false); // 默认允许加载
			
			PDICMainAppOptions.padLeft(!GlobalOptions.isSmall);
			PDICMainAppOptions.padRight(!GlobalOptions.isSmall);
		}
		opt.setBottombarOnBottom(true);
		opt.setFloatBottombarOnBottom(true);
		opt.setPeruseBottombarOnBottom(true);
		opt.setCacheCurrentGroup(false);
		opt.shareTextOrUrl(0);
		opt.setInPeruseMode(false);
		opt.setInFloatPeruseMode(false);
		PDICMainAppOptions.setUseSoundsPlaybackFirst(false);
		
		if (ViewUtils.isKindleDark()) {
			if(!opt.defaultReader.contains("dkB")) {
				opt.tmpEdit().putInt("dkB", 0xFF333333);
				PDICMainAppOptions.nightUsePageColor(true);
			}
		}

//		opt.setPageTurn1(true);
//		opt.setPageTurn2(true);
//		opt.setPageTurn3(true);
//		opt.setTurnPageEnabled(true);
	}
	
	@SuppressLint("ResourceType")
	public static void openIntro(PDICMainActivity a) {
		ActivityMainBinding UIData = a.UIData;
		a.root.postDelayed(new Runnable() {
			@Override
			public void run() {
				UIData.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
					MotionEvent evt;
					View view;
					@Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
					@Override
					public void onDrawerOpened(@NonNull View drawerView) {
						Runnable fin = () -> {
							UIData.drawerLayout.close();
							if (evt!=null) evt.recycle();
							if (view != null) {
								try {
									((TextView) view.findViewById(R.id.subtext)).setText("无限打开 MDX/DSL.DZ/PDF");
								} catch (Exception e) {
									CMN.debug(e);
								}
							}
						};
						UIData.drawerLayout.removeDrawerListener(this);
						view = a.drawerFragment.mDrawerList.findViewById(R.string.addd);
						View view1 = a.drawerFragment.mDrawerList.findViewById(R.string.pick_main);
						evt = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, -100, -100, 0);
						if (view1 != null) {
							view1.dispatchTouchEvent(evt);
							view1.postDelayed(() -> ViewUtils.preventDefaultTouchEvent(view1, -100, -100), 1000);
						}
						if (view != null) {
							view1.postDelayed(() -> {
								view.dispatchTouchEvent(evt);
								view.postDelayed(() -> ViewUtils.preventDefaultTouchEvent(view, -100, -100), 1233);
							}, view1==null?0:1233);
						}
						a.root.postDelayed(() -> {
							ObjectAnimator td = ViewUtils.tada(a.drawerFragment.menu_item_setting, 2);
							if (td != null) {
								td.start();
								td.setDuration(1233);
							}
						}, 2000);
						a.root.postDelayed(fin::run, view1==null?2700:4500);
					}
					@Override public void onDrawerClosed(@NonNull View drawerView) { }
					@Override public void onDrawerStateChanged(int newState) { }
				});
				UIData.drawerLayout.open();
			}
		}, 500);
	}
}

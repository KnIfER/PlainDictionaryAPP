package com.knziha.plod.plaindict;

import android.os.Build;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.model.GlideOptions;

public class VersionUtils {
	
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
			CMN.Log("初始化版本!!!");
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
			opt.menuOverlapAnchor(!GlobalOptions.isLarge||GlobalOptions.isSmall);
			PDICMainAppOptions.setUseBackKeyGoWebViewBack(false);
			opt.setPageTurn1(true);
			opt.setPageTurn2(true);
			opt.setPageTurn3(true);
			opt.setTurnPageEnabled(true);
			opt.schPageNavAudioKey(false);
			
			opt.setUseBackKeyGoWebViewBack(false);
			opt.setUseBackKeyGoWebViewBack1(false);
			opt.tapSchPageAutoReadEntry(false);
			
			PDICMainAppOptions.pageSchWild(true);
			PDICMainAppOptions.pageSchWildDeprecated(false);
			PDICMainAppOptions.uncheckVersionBefore_5_7(false);
		}
//		opt.setBottombarOnBottom(true);
//		opt.setFloatBottombarOnBottom(true);
//		opt.setPeruseBottombarOnBottom(true);

//		opt.setPageTurn1(true);
//		opt.setPageTurn2(true);
//		opt.setPageTurn3(true);
//		opt.setTurnPageEnabled(true);
	}
}

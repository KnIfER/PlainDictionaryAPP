package com.knziha.plod.plaindict;

import android.os.Build;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.widgets.RomUtils;
import com.knziha.plod.widgets.ViewUtils;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.util.HashMap;

public class VersionUtils {
	public static final boolean AnnotOff = true && !BuildConfig.isDebug;
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
}

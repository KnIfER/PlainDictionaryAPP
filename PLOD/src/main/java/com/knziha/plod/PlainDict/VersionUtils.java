package com.knziha.plod.plaindict;

import android.os.Build;

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
		PDICMainAppOptions.uncheckVersionBefore_4_0(false);
		PDICMainAppOptions.uncheckVersionBefore_4_9(false);
		PDICMainAppOptions.uncheckVersionBefore_5_0(false);
		PDICMainAppOptions.uncheckVersionBefore_5_2(false);
		PDICMainAppOptions.uncheckVersionBefore_5_3(false);
		PDICMainAppOptions.uncheckVersionBefore_5_4(false);
		if(PDICMainAppOptions.checkVersionBefore_5_7()) {
			opt.setTypeFlag_11_AtQF(0, 0);
			opt.setTypeFlag_11_AtQF(0, 2);
			opt.setTypeFlag_11_AtQF(0, 4);
			opt.setTypeFlag_11_AtQF(0, 6);
			opt.setTypeFlag_11_AtQF(0, 8);
			opt.setTypeFlag_11_AtQF(0, 22);
			PDICMainAppOptions.uncheckVersionBefore_5_7(false);
		}
	}
}

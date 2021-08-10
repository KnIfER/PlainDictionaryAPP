package com.knziha.plod.plaindict;

import android.os.Build;

public class VersionUtils {
	
	public static void checkVersion() {
		if(true||PDICMainAppOptions.checkVersionBefore_4_0()) {
			PDICMainAppOptions.setSimpleMode(false);
			PDICMainAppOptions.uncheckVersionBefore_4_0(false);
			PDICMainAppOptions.setLeaveContentBlank(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT);
			PDICMainAppOptions.setAnimateContents(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT);
//			PDICMainAppOptions.setShareTarget(0);
//			PDICMainAppOptions.setPasteTarget(1);
//			PDICMainAppOptions.setNotifyComboRes(false);
//			PDICMainAppOptions.setRebuildToast(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P);
//			PDICMainAppOptions.setBackPrevention(0);
//			PDICMainAppOptions.setFloatHideNavigation(false);
		}
	}
}

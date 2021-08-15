package com.knziha.plod.plaindict;

import android.os.Build;

public class VersionUtils {
	
	public static void checkVersion(PDICMainAppOptions opt) {
		if(PDICMainAppOptions.checkVersionBefore_4_0()) {
			PDICMainAppOptions.setSimpleMode(false);
			PDICMainAppOptions.uncheckVersionBefore_4_0(false);
			PDICMainAppOptions.setLeaveContentBlank(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT);
			PDICMainAppOptions.setAnimateContents(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT);
			PDICMainAppOptions.setRebuildToast(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P);
		}
		if(PDICMainAppOptions.checkVersionBefore_4_9()) {
			PDICMainAppOptions.uncheckVersionBefore_4_9(false);
			opt.setPinDialog_2(true);
		}
	}
}

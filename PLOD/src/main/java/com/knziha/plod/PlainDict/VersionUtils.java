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
	
	public static class UpdateDebugger {
		private static boolean gEnabled = false; /* true false */
		public static boolean logProgress = true; /* true false */
		/*  使用缓存的更新信息 */
		public static String fakeUpdateDetect() {
			if (gEnabled && true) { /* true false */
				return "{\"code\":0,\"message\":\"\",\"data\":{\"buildKey\":\"dbdf29a1116bdf279dee013dd76a717a\",\"buildType\":\"2\",\"buildIsFirst\":\"0\",\"buildIsLastest\":\"1\",\"buildFileSize\":\"13804054\",\"buildName\":\"PlainDict\",\"buildPassword\":\"\",\"buildVersion\":\"6.8.1\",\"buildVersionNo\":\"90\",\"buildQrcodeShowAppIcon\":\"1\",\"buildVersionType\":\"1\",\"buildBuildVersion\":\"2\",\"buildIdentifier\":\"com.knziha.plod.plaindict\",\"buildIcon\":\"e66e6ef88277de3996891a9cd13b4477\",\"buildDescription\":\"\",\"buildUpdateDescription\":\"# v 6.8.1 == iRMPH0g2n6fi\\n- \\u517c\\u5bb9Collins2020.mdx\\uff0c\\u539f\\u5148\\u4e0d\\u80fd\\u5c55\\u793a\\u540e\\u534a\\u672c\\u8bcd\\u6761\\u5185\\u5bb9\\n- \\u4fee\\u590d\\u5c4f\\u98ce\\u9875\\u9762\\u6a21\\u5f0f\\u4e0b\\u62d6\\u52a8\\u6761\\u7684\\u5f02\\u5e38\\n- \\u4fee\\u590d\\u5206\\u5b57\\u641c\\u7d22\\uff0c\\u548c\\u70b9\\u8bd1\\u4f7f\\u7528\\u4e00\\u6837\\u7684\\u6784\\u8bcd\\u5e93\\u3001\\u81ea\\u52a8\\u53bb\\u540e\\u7f00\",\"buildScreenshots\":\"dd71216c9fc758e2b8b09e6e39bf77cf\",\"buildShortcutUrl\":\"PLOD\",\"buildSignatureType\":\"0\",\"buildIsAcceptFeedback\":\"1\",\"buildIsUploadCrashlog\":\"1\",\"buildIsOriginalBuildInHouse\":\"1\",\"buildAdhocUuids\":\"[]\",\"buildTemplate\":\"colorful\",\"buildInstallType\":\"1\",\"buildManuallyBlocked\":\"2\",\"buildIsPlaceholder\":\"2\",\"buildCates\":\"\",\"buildCreated\":\"2022-11-17 11:30:07\",\"buildUpdated\":\"2022-11-17 11:30:07\",\"buildQRCodeURL\":\"https:\\/\\/www.pgyer.com\\/app\\/qrcodeHistory\\/1b60f56e74c7792df3fa833ee4f80e6e61b761c3a813d9a5c943f21c1b9b8831\",\"isOwner\":1,\"isJoin\":2,\"buildFollowed\":\"0\",\"appExpiredDate\":\"2024-11-16 11:30:07\",\"isImmediatelyExpired\":false,\"appExpiredStatus\":2,\"otherApps\":[{\"buildKey\":\"4ffb95f0b155e860ef655e3e6e37d004\",\"buildName\":\"PlainDict\",\"buildVersion\":\"6.8\",\"buildBuildVersion\":\"1\",\"buildIdentifier\":\"com.knziha.plod.plaindict\",\"buildCreated\":\"1\\u5929\\u524d\",\"buildUpdateDescription\":\"- \\u65b0\\u589e\\u9875\\u9762\\u7e41\\u7b80\\u8f6c\\u6362\\u529f\\u80fd\\n- \\u589e\\u5f3a\\u9ed1\\u6697\\u6a21\\u5f0f\\u7684\\u517c\\u5bb9\\u6027\\n- \\u4f18\\u5316\\u91cd\\u590d\\u8bcd\\u6761\\u5904\\u7406\"}],\"otherAppsCount\":\"1\",\"todayDownloadCount\":0,\"appKey\":\"33b6416b2018de0335e62633fde573d4\",\"appAutoSync\":\"1\",\"appShowPgyerCopyright\":\"1\",\"appDownloadPay\":\"0\",\"appDownloadDescription\":\"\",\"appGameLicenseStatus\":\"99\",\"appLang\":\"3\",\"appIsTestFlight\":\"2\",\"appIsInstallDate\":\"2\",\"appInstallStartDate\":\"0000-00-00\",\"appInstallEndDate\":\"0000-00-00\",\"appInstallQuestion\":\"\",\"appInstallAnswer\":\"\",\"appFeedbackStatus\":\"1\",\"isMerged\":2,\"mergeAppInfo\":null,\"canPayDownload\":1,\"iconUrl\":\"https:\\/\\/cdn-app-icon.pgyer.com\\/e\\/6\\/6\\/e\\/6\\/e66e6ef88277de3996891a9cd13b4477?x-oss-process=image\\/resize,m_lfit,h_120,w_120\\/format,jpg\",\"buildScreenshotsUrl\":[\"https:\\/\\/cdn-app-screenshot.pgyer.com\\/d\\/d\\/7\\/1\\/2\\/dd71216c9fc758e2b8b09e6e39bf77cf?x-oss-process=image\\/format,jpg\"]}}\\n- \\u517c\\u5bb9Collins2020.mdx\\uff0c\\u539f\\u5148\\u4e0d\\u80fd\\u5c55\\u793a\\u540e\\u534a\\u672c\\u8bcd\\u6761\\u5185\\u5bb9\\n- \\u4fee\\u590d\\u5c4f\\u98ce\\u9875\\u9762\\u6a21\\u5f0f\\u4e0b\\u62d6\\u52a8\\u6761\\u7684\\u5f02\\u5e38\\n- \\u4fee\\u590d\\u5206\\u5b57\\u641c\\u7d22\\uff0c\\u548c\\u70b9\\u8bd1\\u4f7f\\u7528\\u4e00\\u6837\\u7684\\u6784\\u8bcd\\u5e93\\u3001\\u81ea\\u52a8\\u53bb\\u540e\\u7f00\",\"buildScreenshots\":\"dd71216c9fc758e2b8b09e6e39bf77cf\",\"buildShortcutUrl\":\"PLOD\",\"buildSignatureType\":\"0\",\"buildIsAcceptFeedback\":\"1\",\"buildIsUploadCrashlog\":\"1\",\"buildIsOriginalBuildInHouse\":\"1\",\"buildAdhocUuids\":\"[]\",\"buildTemplate\":\"colorful\",\"buildInstallType\":\"1\",\"buildManuallyBlocked\":\"2\",\"buildIsPlaceholder\":\"2\",\"buildCates\":\"\",\"buildCreated\":\"2022-11-17 11:30:07\",\"buildUpdated\":\"2022-11-17 11:30:07\",\"buildQRCodeURL\":\"https:\\/\\/www.pgyer.com\\/app\\/qrcodeHistory\\/1b60f56e74c7792df3fa833ee4f80e6e61b761c3a813d9a5c943f21c1b9b8831\",\"isOwner\":1,\"isJoin\":2,\"buildFollowed\":\"0\",\"appExpiredDate\":\"2024-11-16 11:30:07\",\"isImmediatelyExpired\":false,\"appExpiredStatus\":2,\"otherApps\":[{\"buildKey\":\"4ffb95f0b155e860ef655e3e6e37d004\",\"buildName\":\"PlainDict\",\"buildVersion\":\"6.8\",\"buildBuildVersion\":\"1\",\"buildIdentifier\":\"com.knziha.plod.plaindict\",\"buildCreated\":\"1\\u5929\\u524d\",\"buildUpdateDescription\":\"- \\u65b0\\u589e\\u9875\\u9762\\u7e41\\u7b80\\u8f6c\\u6362\\u529f\\u80fd\\n- \\u589e\\u5f3a\\u9ed1\\u6697\\u6a21\\u5f0f\\u7684\\u517c\\u5bb9\\u6027\\n- \\u4f18\\u5316\\u91cd\\u590d\\u8bcd\\u6761\\u5904\\u7406\"}],\"otherAppsCount\":\"1\",\"todayDownloadCount\":0,\"appKey\":\"33b6416b2018de0335e62633fde573d4\",\"appAutoSync\":\"1\",\"appShowPgyerCopyright\":\"1\",\"appDownloadPay\":\"0\",\"appDownloadDescription\":\"\",\"appGameLicenseStatus\":\"99\",\"appLang\":\"3\",\"appIsTestFlight\":\"2\",\"appIsInstallDate\":\"2\",\"appInstallStartDate\":\"0000-00-00\",\"appInstallEndDate\":\"0000-00-00\",\"appInstallQuestion\":\"\",\"appInstallAnswer\":\"\",\"appFeedbackStatus\":\"1\",\"isMerged\":2,\"mergeAppInfo\":null,\"canPayDownload\":1,\"iconUrl\":\"https:\\/\\/cdn-app-icon.pgyer.com\\/e\\/6\\/6\\/e\\/6\\/e66e6ef88277de3996891a9cd13b4477?x-oss-process=image\\/resize,m_lfit,h_120,w_120\\/format,jpg\",\"buildScreenshotsUrl\":[\"https:\\/\\/cdn-app-screenshot.pgyer.com\\/d\\/d\\/7\\/1\\/2\\/dd71216c9fc758e2b8b09e6e39bf77cf?x-oss-process=image\\/format,jpg\"]}}";
			} else {
				return null;
			}
		}
		
		/*  总是当成更新 */
		public static boolean fakeUpdateVerdict(boolean val) {
			return !gEnabled ? val : true; /* true false */
		}
		
		/*  调试webview */
		public static boolean fakeShowWebview() {
			return !(gEnabled && false); /* true false */
		}
		
		/** 不解析webview */
		public static HashMap<String, String> fakeCachedDownloadStart() {
			if (gEnabled && false) { /* true false */
				HashMap<String, String> cachedUpdate = new HashMap();
				cachedUpdate.put("url", "https://i82.lanzoug.com/1117160089190988bb/2022/11/16/10f4b916116368e1c4389e69c1cba5d5.apk?st=2oMWep3xzeUdMgrvAak1vg&e=1668674387&b=CL9e51PgU7NQgAPtVuABlVGYCrEHsgKmBwsMd1ZnUHxUPll3BDVUfwA0UHYEPw_c_c&fi=89190988&pid=153-34-122-253&up=2&mp=1&co=1");
				cachedUpdate.put("desc", "attachment; filename= %E5%B9%B3%E5%85%B8%E6%90%9C%E7%B4%A2_v6.8.1.apk");
				cachedUpdate.put("len", ""+13000000);
				return cachedUpdate;
			} else {
				return null;
			}
		}
		
		/** 不禁下载 */
		public static File fakeCachedVer(File f) {
			if (gEnabled && true) { /* true false */
				return new File("/sdcard/ver");
			} else {
				return f;
			}
		}
		
		/** 测试下载 */
		public static String fakeDownloadUrl(String url) {
			if (gEnabled && true) { /* true false */
				return "http://192.168.0.100:8080/base/0/%E5%B9%B3%E5%85%B8%E6%90%9C%E7%B4%A2_v6.8.1.apk";
			} else {
				return url;
			}
		}
		public static String fakeLanYunUrl(String url) {
			// 开启后，测试解析失效
			if (gEnabled && false) { /* true false */
				return "https://pan.baidu.com/share/init?surl=pEAtfW6JHYX8G1CLnYyNbg";
			} else {
				return url;
			}
		}
	}
}

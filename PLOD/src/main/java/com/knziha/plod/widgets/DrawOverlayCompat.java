/*
 * Copyright 2016 czy1121
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.plod.widgets;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.knziha.plod.plaindict.CMN;

/**
 * Created by yy on 2020/6/13.
 * function: 权限判断与跳转
 */
// https://github.com/Alonsol/PerfectFloatWindow/blob/16b1674c57e778406beaeeb2bc6807ce84a93ea3/floatserver/src/main/java/com/yy/floatserver/utils/SettingsCompat.java
public class DrawOverlayCompat {
    public static boolean manage(Context context) {
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
			Intent permission = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
			permission.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(permission);
		} else {
			switch (RomUtils._HX) {
				// 小米
				case 2: {
					Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
					intent.putExtra("extra_pkgname", context.getPackageName());
					intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
					if (startSafe(context, intent)) {
						return true;
					}
					intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
					if (startSafe(context, intent)) {
						return true;
					}
					// miui v5 的支持的android版本最高 4.x
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
						Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						intent1.setData(Uri.fromParts("package", context.getPackageName(), null));
						return startSafe(context, intent1);
					}
					return false;
				}
				// 华为
				case 1: {
					final String HUAWEI_PACKAGE = "com.huawei.systemmanager";
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
						if (startSafe(context, intent)) {
							return true;
						}
					}
					Intent intent = new Intent();
					// Huawei Honor P6|4.4.4|3.0
					intent.setClassName(HUAWEI_PACKAGE, "com.huawei.notificationmanager.ui.NotificationManagmentActivity");
					intent.putExtra("showTabsNumber", 1);
					if (startSafe(context, intent)) {
						return true;
					}
					intent.setClassName(HUAWEI_PACKAGE, "com.huawei.permissionmanager.ui.MainActivity");
					if (startSafe(context, intent)) {
						return true;
					}
					return false;
				}
				// OPPO
				case 3: {
					Intent intent = new Intent();
					intent.putExtra("packageName", context.getPackageName());
					// OPPO A53|5.1.1|2.1
					intent.setAction("com.oppo.safe");
					intent.setClassName("com.oppo.safe", "com.oppo.safe.permission.floatwindow.FloatWindowListActivity");
					if (startSafe(context, intent)) {
						return true;
					}
					// OPPO R7s|4.4.4|2.1
					intent.setAction("com.color.safecenter");
					intent.setClassName("com.color.safecenter", "com.color.safecenter.permission.floatwindow.FloatWindowListActivity");
					if (startSafe(context, intent)) {
						return true;
					}
					intent.setAction("com.coloros.safecenter");
					intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity");
					return startSafe(context, intent);
				}
				// 锤子
				case 4: {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						return false;
					}
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						// 锤子 坚果|5.1.1|2.5.3
						Intent intent = new Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS_NEW");
						intent.setClassName("com.smartisanos.security", "com.smartisanos.security.SwitchedPermissions");
						intent.putExtra("index", 17);
						return startSafe(context, intent);
					} else {
						// 锤子 坚果|4.4.4|2.1.2
						Intent intent = new Intent("com.smartisanos.security.action.SWITCHED_PERMISSIONS");
						intent.setClassName("com.smartisanos.security", "com.smartisanos.security.SwitchedPermissions");
						intent.putExtra("permission", new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW});
						return startSafe(context, intent);
					}
				}
				// VIVO
				case 5: {
					// 不支持直接到达悬浮窗设置页，只能到 i管家 首页
					try {
						Intent intent = new Intent();
						intent.setComponent(ComponentName.unflattenFromString("com.vivo.permissionmanager/.activity.PurviewTabActivity"));
						context.startActivity(intent);
						return true;
					} catch (Exception e) {
						// startVivoSafy
						try {
							Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
							context.startActivity(appIntent);
							return true;
						} catch (Exception ignored) {
						}
						return false;
					}
				}
				case 6:
					break;
				case 7:
					break;
				case 8:
					break;
				// 360
				case 9: {
					Intent intent = new Intent();
					intent.setClassName("com.android.settings", "com.android.settings.Settings$OverlaySettingsActivity");
					if (startSafe(context, intent)) {
						return true;
					}
					intent.setClassName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
					return startSafe(context, intent);
				}
				// 魅族
				case 10: {
					Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
					intent.addCategory(Intent.CATEGORY_DEFAULT);
					intent.putExtra("packageName", context.getPackageName());
					return startSafe(context, intent);
				}
			}
		}
        return false;
    }

    private static boolean startSafe(Context context, Intent intent) {
        try {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			return true;
        } catch (Exception e){
			CMN.debug(e);
        }
        return false;
    }
	
}
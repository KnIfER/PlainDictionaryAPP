package com.knziha.plod.widgets;

import android.os.Build;
import android.text.TextUtils;

import com.knziha.plod.plaindict.CMN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class RomUtils {
	public static final String manufacturer = Build.MANUFACTURER.toLowerCase();
	/** 0=未检测； 1=华为； 2=小米; 3=oppo; 4=chuizi; 5=vivo; 6=金立amigo; 7=乐视eui; 8=酷派yulong; 9=360; 10=flyme;  */
	public static int _HX = readHX();
	
	public static int readHX() {
		int ret=0;
		if (ViewUtils.checkRom) {
			/* 检查系统 : （有省略）
				https://github.com/Alonsol/PerfectFloatWindow/blob/eb2bec2a4a8b994bf2565ebf3f1380d17ed914ba/floatserver/src/main/java/com/yy/floatserver/utils/RomUtil.java */
			ret = hasProp("ro.miui.ui.version.name")?2
					:hasProp("ro.build.version.emui")?1
					:hasProp("ro.build.version.opporom")?3
					:hasProp("ro.smartisan.version")?4
					:hasProp("ro.ro.vivo.os.version")?5
					:hasProp("ro.gn.gnromvernumber")/* || hasProp("ro.gn.amigo.systemui.support")*/?6
					:hasProp("ro.letv.release.version") || hasProp("ro.product.letv_model")?7
					:hasProp("ro.yulong.version.release") || hasProp("ro.yulong.version.tag")?8
					:hasProp("360") || hasProp("360")?9
					:0;
		}
		if (ret==0) {
			/* 检查品牌 :
			 	https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/src/main/java/com/blankj/utilcode/util/RomUtils.java */
			switch (manufacturer) {
				case "xiaomi": ret=2; break;
				case "huawei": ret=1; break;
				case "oppo": ret=3; break;
				case "smartisan": ret=4; break;
				case "vivo": ret=5; break;
				case "gionee":
				case "amigo": ret=6; break;
				case "leeco":
				case "letv": ret=7; break;
				case "coolpad":
				case "yulong": ret=8; break;
				case "360":
				case "qiku": ret=9; break;
				case "flyme": ret=10; break;
			}
		}
		return ret;
	}
	
	public static boolean hasProp(String name) {
		String line = null;
		BufferedReader input = null;
		try {
			Process p = Runtime.getRuntime().exec("getprop " + name);
			input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
			line = input.readLine();
			input.close();
		} catch (Exception ex) {
			CMN.Log("Unable to read prop ", name, ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
		}
		return line!=null && line.trim().length()>0;
	}
	
	public static boolean isMIUI() {
		return _HX==2;
	}
}

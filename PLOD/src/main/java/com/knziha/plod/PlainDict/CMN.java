package com.knziha.plod.PlainDict;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.knziha.plod.dictionary.Utils.SU;

import org.adrianwalker.multilinestring.Multiline;


//common
public class CMN{
	public final static String replaceReg =  " |:|\\.|,|-|\'|(|)";
	public final static String emptyStr = "";
	public static volatile int instanceCount;
	public static final HashMap<String, String> AssetMap = new HashMap<>();
	public static final String AssetTag = "/ASSET/";
	public static final Boolean OccupyTag = true;

	public static int GlobalPageBackground = 0;
	public static int MainBackground = 0;
	public static int FloatBackground;
	public static boolean touchThenSearch=true;
	public static int actionBarHeight;
	//static Boolean module_set_invalid = true;
	//public static dictionary_App_Options opt;
	//public static LayoutInflater inflater;
	//protected static ViewPager viewPager;
	public static int dbVersionCode = 1;
	public static long FloatLastInvokerTime=-1;
	public static int ShallowHeaderBlue;




	///*[!0] Start debug flags and methods
	public static boolean testFLoatSearch;
	public static boolean editAll;
	public static boolean darkRequest=true;
	public static int CheckSettings;

	/** Is it not like the king? */
	@Multiline
	public static final String TestText="Happy";
	public static boolean bForbidOneSpecFile;
	public static long LastConfigReadTime;

	public static String Log(Object... o) {
		String msg="";
		if(o!=null)
			for(int i=0;i<o.length;i++) {
				if(Exception.class.isInstance(o[i])) {
					ByteArrayOutputStream s = new ByteArrayOutputStream();
					PrintStream p = new PrintStream(s);
					((Exception)o[i]).printStackTrace(p);
					msg+=s.toString();
				}
				msg+=o[i]+" ";
			}

		if(SU.UniversalObject instanceof Exception)
			System.out.println(msg);
		else
			android.util.Log.d("fatal poison",msg);
		return msg;
	}
	public static void recurseLog(View v,String... depths) {
		String depth = depths!=null && depths.length>0?depths[0]:"- ";
		String depth_plus_1=depth+"- ";
		if(!ViewGroup.class.isInstance(v)) return;
		ViewGroup vg = (ViewGroup) v;
		for(int i=0;i<vg.getChildCount();i++) {
			View CI = vg.getChildAt(i);
			String CIS = "";
			if(CI instanceof TextView) {
				CIS = ((TextView)CI).getText().toString();
				if(CIS.length()>10) CIS = CIS.substring(0, 10);
			}
			CIS = CIS+CI;
			Log(depth+CIS+" == "+Integer.toHexString(CI.getId())+"/"+CI.getBackground()+"\\"+CI.getTag());
			if(ViewGroup.class.isInstance(CI))
				recurseLog(CI,depth_plus_1);
		}
	}
	public static void recurseLogCascade(View now) {
		if(now==null) return;
		while(now.getParent()!=null) {
			if(!View.class.isInstance(now.getParent())) {
				Log("-!-reached none view object or null : "+now.getParent());
				break;
			}
			now=(View) now.getParent();
		}
		Log("Cascade Start Is : "+now+" == "+Integer.toHexString(now.getId())+"/"+now.getBackground());
		recurseLog(now);
		//now.setBackgroundResource(R.drawable.popup_shadow);
	}
	//[!1] End debug flags and methods*/

	public static long stst;
	public static long stst_add;
	public static void rt() {
		stst = System.currentTimeMillis();
	}
	public static void pt(Object...args) {
		CMN.Log(listToStr(args)+" "+(System.currentTimeMillis()-stst));
	}
	public static void tp(long stst, Object...args) {
		long time = (System.currentTimeMillis() - stst);
		CMN.Log(time+" "+listToStr(args));
		stst_add+=time;
	}

	private static String listToStr(Object...args) {
		String ret="";
		for (int i = 0; i < args.length; i++) {
			ret+=args[i];
		}
		return ret;
	}

	static Integer resourceId;
	public static int getStatusBarHeight(Context a) {
		if(resourceId==null)
			try {
				resourceId = a.getResources().getIdentifier("status_bar_height", "dimen", "android");
			} catch (Exception e) {
				resourceId=0;
			}
		if (resourceId > 0) {
			return a.getResources().getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	public static boolean checkRCSP() {
		boolean ret = (CheckSettings&0x1)!=0;
		if(ret) CheckSettings&=~0x1;
		return ret;
	}

	public static void setCheckRcsp() {
		CheckSettings|=0x1;
	}

	public static String unwrapDatabaseName(String name) {
		if(name.endsWith(".sql")) return name.substring(0, name.length()-4);
		if(name.endsWith(".sql.db")) return name.substring(0, name.length()-7);
		return name;
	}
}
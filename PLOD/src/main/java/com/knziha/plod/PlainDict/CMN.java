package com.knziha.plod.PlainDict;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import android.view.View;
import android.view.ViewGroup;


//common
public class CMN{
	public final static String replaceReg =  " |:|\\.|,|-|\'|(|)";
	public final static String emptyStr = "";
	public static final HashMap<String, String> AssetMap = new HashMap<>();
	public static final Boolean OccupyTag = true;

	public static int GlobalPageBackground = 0;
	public static int MainBackground = 0;
	public static int FloatBackground;
	public static boolean touchThenSearch=true;
	public static int actionBarHeight;
	public static int lastFavorLexicalEntry = -1;
	public static int lastHisLexicalEntry = -1;
	public static int lastFavorLexicalEntryOff = 0;
	public static int lastHisLexicalEntryOff = 0;
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
	public static void Log(Object... o) {
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
		android.util.Log.d("fatal poison",msg);
	}
	public static void recurseLog(View v,String... depths) {
		String depth = depths!=null && depths.length>0?depths[0]:"- ";
		String depth_plus_1=depth+"- ";
		if(!ViewGroup.class.isInstance(v)) return;
		ViewGroup vg = (ViewGroup) v;
		for(int i=0;i<vg.getChildCount();i++) {
			View CI = vg.getChildAt(i);
			Log(depth+CI+" == "+Integer.toHexString(CI.getId())+"/"+CI.getBackground());
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
	public static void rt() {
		stst = System.currentTimeMillis();
	}
	public static void pt(Object...args) {
		CMN.Log(listToStr(args)+" "+(System.currentTimeMillis()-stst));
	}

	private static String listToStr(Object...args) {
		String ret="";
		for (int i = 0; i < args.length; i++) {
			ret+=args[i];
		}
		return ret;
	}
}
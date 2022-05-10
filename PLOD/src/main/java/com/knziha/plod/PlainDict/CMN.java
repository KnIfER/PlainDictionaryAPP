package com.knziha.plod.plaindict;

import android.content.Context;
import android.graphics.Color;
import android.util.AndroidException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.widgets.ViewUtils;

import org.knziha.metaline.Metaline;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;

import static com.knziha.plod.plaindict.TestHelper.RotateEncrypt;


//common
public class CMN{
	public static /*volatile*/ int instanceCount;
	public static final HashMap<String, String> AssetMap = new HashMap<>();
	public static final String AssetTag = "/ASSET/";
	public static final Boolean OccupyTag = true;
	public static final WeakReference EmptyRef = new WeakReference(null);
	
	public static long mid;
	
	public static int AppBackground = Color.GRAY;
	public static int GlobalPageBackground = Color.WHITE;
	/** 0x1=main; 0x2=float */
	public static int AppColorChangedFlag;
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
	@Metaline
	public static final String TestText="Happy";
	public static boolean bForbidOneSpecFile;
	public static long LastConfigReadTime;
	
	public static String sep = " ";
	public static String BrandName = "PLOD";
	
	public static int HonestCredits=100;
	public static WeakReference<MainActivityUIBase> pHandler;
	
	public static String Log(Object... o) {
		StringBuilder msg = new StringBuilder(1024);
		for(int i=0;i<o.length;i++) {
				Object o1 = o[i];
				if (o1 != null) {
					if (o1 instanceof Exception) {
						Exception e = ((Exception)o[i]);
						msg.append(e);
						if (o1 instanceof AndroidException) {
							HonestCredits-=25;
						}
						if(!GlobalOptions.debug && o.length==1) {
							e.printStackTrace();
						} else {
							ByteArrayOutputStream s = new ByteArrayOutputStream();
							PrintStream p = new PrintStream(s);
							e.printStackTrace(p);
							msg.append(s);
							continue;
						}
					} else if(o1 instanceof String) {
						if (((String)o1).startsWith("$")) {
							try {
								o1 = ViewUtils.execSimple(((String)o1).startsWith("$",1)?
										RotateEncrypt((String)o1, true):(String)o1, null, o);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					if (o1!=null && o1.getClass().isArray()) {
						String classname = o1.getClass().getName();
						if (classname.length()==2) {
							switch (classname.charAt(1)) {
								case 'B': {
									o1 = msg.append(Arrays.toString((byte[]) o1));
								} break;
								case 'D': {
									o1 = msg.append(Arrays.toString((double[]) o1));
								} break;
								case 'F': {
									o1 = msg.append(Arrays.toString((float[]) o1));
								} break;
								case 'L': {
									o1 = msg.append(Arrays.toString((long[]) o1));
								} break;
								case 'I': {
									o1 = msg.append(Arrays.toString((int[]) o1));
								} break;
								case 'S': {
									o1 = msg.append(Arrays.toString((short[]) o1));
								} break;
							}
						} else {
							o1 = Arrays.toString((Object[]) o1);
						}
					}
				}
				msg.append(o1);
				msg.append(sep);
			}
		String message = msg.toString();
		if(SU.UniversalObject instanceof Exception){
			System.out.println(message);
		} else {
			android.util.Log.d("fatal poison",message);
		}
		sep = " ";
		return message;
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
			Log(depth+CIS+" == "+Integer.toHexString(CI.getId())+"/"+CI.getBackground()+"\\"+CI.getTag()+(CI instanceof TextView?("@font"+((TextView)CI).getTextSize()):""));
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
	public static long ststrt;
	public static long stst_add;
	public static long rt(Object... o) {
		CMN.Log(o);
		return ststrt = System.currentTimeMillis();
	}
	public static long pt(Object...args) {
		long ret=System.currentTimeMillis()-ststrt;
		CMN.Log(listToStr(args)+" "+ret);
		return ret;
	}
	public static void tp(long stst, Object...args) {
		long time = (System.currentTimeMillis() - stst);
		CMN.Log(time+" "+listToStr(args));
		stst_add+=time;
	}
	
	public static String listToStr(Object...args) {
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
	
	public static int Visible(boolean vis) {
		return vis?View.VISIBLE:View.GONE;
	}
	
	public static int id(Object object) {
		return System.identityHashCode(object);
	}
	
	public static String idStr(Object object) {
		return Integer.toHexString(System.identityHashCode(object));
	}
	
	public static long now() {
		return System.currentTimeMillis();
	}
	
	public static Object elapsed(long st) {
		return now()-st;
	}
	
	public static String getAssetName(String name) {
		String ret = name;
		try {
			ret = AssetMap.get(name.substring(AssetTag.length(), name.lastIndexOf(".")));
		} catch (Exception ignored) { }
		return ret==null?name:ret;
	}
	
	public static String debug(Object...o) {
		if (BuildConfig.isDebug) return Log(o);
		return null;
	}
	
}
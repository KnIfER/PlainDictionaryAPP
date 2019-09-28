package com.jaredrummler.colorpicker;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;


//common
public class CMNF {
    public static HashMap<String, Integer> AssetMap;
	public static int ShallowHeaderBlue;
    public static Object UniversalObject;
    public static HashMap<String, Object> UniversalHashMap;

	///*[!0] Start debug flags and methods
    public static int constants =0;
    public static Long FirstFlag;

    @SuppressLint("LongLogTag")
    public static void Log(Object... o) {
        StringBuilder msg= new StringBuilder();
        if(o!=null)
        for (Object o1 : o) {
                //android.util.Log.d("fatal",o[i].getClass().getName());
                if (o1 != null) {
                    if (o1 instanceof Exception) {
                        ByteArrayOutputStream s = new ByteArrayOutputStream();
                        PrintStream p = new PrintStream(s);
                        ((Exception) o1).printStackTrace(p);
                        msg.append(s.toString());
                        continue;
                    }

                    List oi = null;
                    String classname = o1.getClass().getName();
                    switch (classname) {
                        case "[I": {
                            int[] arr = (int[]) o1;
                            for (int os : arr) {
                                msg.append(os);
                                msg.append(", ");
                            }
                            continue;
                        }
                        case "[Ljava.lang.String;": {
                            String[] arr = (String[]) o1;
                            for (String os : arr) {
                                msg.append(os);
                                msg.append(", ");
                            }
                            continue;
                        }
                        case "[S": {
                            short[] arr = (short[]) o1;
                            for (short os : arr) {
                                msg.append(os);
                                msg.append(", ");
                            }
                            continue;
                        }
                        case "[B": {
                            byte[] arr = (byte[]) o1;
                            for (byte os : arr) {
                                msg.append(Integer.toHexString(os));
                                msg.append(", ");
                            }
                            continue;
                        }
                    }

                }


                msg.append(o1).append(" ");
            }
        android.util.Log.d("fatal poison - filepicker", msg.toString());
    }
	public static void recurseLog(View v,String... depths) {
		String depth = depths!=null && depths.length>0?depths[0]:"- ";
		String depth_plus_1=depth+"- ";
		if(!(v instanceof ViewGroup)) return;
		ViewGroup vg = (ViewGroup) v;
		for(int i=0;i<vg.getChildCount();i++) {
			View CI = vg.getChildAt(i);
			Log(depth+CI+" == "+Integer.toHexString(CI.getId())+"/"+CI.getBackground());
			if(CI instanceof ViewGroup)
				recurseLog(CI,depth_plus_1);
		}
	}
	public static void recurseLogCascade(View now) {
		if(now==null) return;
		while(now.getParent()!=null) {
	    	if(!(now.getParent() instanceof View)) {
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
	
	
}
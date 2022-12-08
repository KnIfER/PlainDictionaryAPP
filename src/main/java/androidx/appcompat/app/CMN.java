package androidx.appcompat.app;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


//common
public class CMN {
    public static final HashMap<String, Integer> AssetMap = new HashMap<>(8);
    public static final String APPTAG = "ODPlayer";
    public static int dbVersionCode = 1;
	///*[!0] Start debug flags and methods\
	public static boolean debugingVideoFrame;
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
                    if (classname.equals("[I")) {
                        int[] arr = (int[]) o1;
                        for (int os : arr) {
                            msg.append(os);
                            msg.append(", ");
                        }
                        continue;
                    } else if (classname.equals("[Ljava.lang.String;")) {
                        String[] arr = (String[]) o1;
                        for (String os : arr) {
                            msg.append(os);
                            msg.append(", ");
                        }
                        continue;
                    } else if (classname.equals("[S")) {
                        short[] arr = (short[]) o1;
                        for (short os : arr) {
                            msg.append(os);
                            msg.append(", ");
                        }
                        continue;
                    } else if (classname.equals("[B")) {
                        byte[] arr = (byte[]) o1;
                        for (byte os : arr) {
                            msg.append(Integer.toHexString(os));
                            msg.append(", ");
                        }
                        continue;
                    }

                }


                msg.append(o1).append(" ");
            }
		android.util.Log.d("fatal poison", msg.toString());
	}
	public static void recurseLog(View v,String... depths) {
		String depth = depths!=null && depths.length>0?depths[0]:"- ";
		String depth_plus_1=depth+"- ";
		if(!(v instanceof ViewGroup)) return;
		ViewGroup vg = (ViewGroup) v;
		for(int i=0;i<vg.getChildCount();i++) {
			View CI = vg.getChildAt(i);
			Log(depth+CI+" == "+Integer.toHexString(CI.getId())+"/"+CI.getBackground()+"/"+CI.isLongClickable());
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


    public static String FormTime(long timeMs,int type){
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        boolean isNeg = false;
        if(timeMs<0){
            timeMs=-timeMs;
            isNeg=true;
        }

        long totalSeconds = timeMs / 1000;

        long ms = timeMs%1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours   = totalSeconds / 3600;
        float seconds_dot_ms = seconds+ms*1.f/100;
        mFormatBuilder.setLength(0);
        //mFormatBuilder.setLength(0);
        //0 : [%02d\r\n%02d\r\n%02d\r\n%03d]
        //1 : [(%02d:)%02d:%02d]
        //2 :  [+-%01d:%02d]
        switch (type){
            case 0:default:
                return mFormatter.format("%02d\n%02d\n%02d\n%03d", hours, minutes, seconds,ms).toString();
            case 1:
                if(hours>0)
                    return mFormatter.format("%02d:%02d:%02d",hours,minutes,seconds).toString();
                else
                    return mFormatter.format("%02d:%02d",minutes,seconds).toString();
            case 2:
                if(minutes==0 && seconds==0)
                    return mFormatter.format("%02d:%02d",minutes,seconds).toString();
                else if(isNeg)
                    return mFormatter.format("-%01d:%02d",minutes,seconds).toString();
                else
                    return mFormatter.format("+%01d:%02d",minutes,seconds).toString();
        }
    }


    static int StatusBarHeight = 0;
    public static int getStatusBarHeight(Activity a) {
        if(StatusBarHeight==0){
            int resourceId = a.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                StatusBarHeight = a.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return StatusBarHeight;
    }

    public static int getNavigationBarHeight(Activity c) {
        DisplayMetrics metrics = new DisplayMetrics();
        c.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        c.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }

    public static int scale(float value, Context c) {
        if(c==null)
            return (int) value;
        DisplayMetrics mDisplayMetrics = c.getResources().getDisplayMetrics();
        float scale = c.getResources().getDisplayMetrics().density;

        float scaleWidth = (float) mDisplayMetrics.widthPixels / 720;
        float scaleHeight = (float) mDisplayMetrics.heightPixels / 1280;

        return Math.round(value *
                Math.min(scaleWidth, scaleHeight) * scale
                * 0.5f);
    }
	
	public static String id(Object o) {
		return Integer.toHexString(Objects.hashCode(o));
	}
}
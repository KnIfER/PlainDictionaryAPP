package com.knziha.plod.PlainDict;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.Application;

import com.knziha.plod.dictionarymodels.mdict;

public class AgentApplication extends Application {
    private List<Activity> activities = new ArrayList<Activity>();  
    public List<mdict> md;
    public PDICMainAppOptions opt;
	public HashSet<String> mdlibsCon;

	static {
	//	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	//            .detectAll()//监测所有内容
	 //           .penaltyLog()//违规对log日志
	//            .penaltyDeath()//违规Crash
	//            .build());
		CMN.AssetMap.clear();
		CMN.AssetMap.put("/ASSET/liba.mdx", "李白全集-内置");
		CMN.AssetMap.put("/ASSET/", "【内置】");
	}
	
        public void addActivity(Activity activity) {  
            activities.add(activity);  
        }  

        @Override  
        public void onTerminate() {  
            super.onTerminate();  

            for (Activity activity : activities) {  
                activity.finish();  
            }  


            System.exit(0);  
        }

		public void clearNonsenses() {
			md=null;
			opt=null;
			mdlibsCon=null;
		}  
}
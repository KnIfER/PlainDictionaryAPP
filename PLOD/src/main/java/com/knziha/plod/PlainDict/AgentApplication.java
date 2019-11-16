package com.knziha.plod.PlainDict;

import android.app.Application;

import com.bumptech.glide.Registry;
import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.slideshow.MddPic;
import com.knziha.plod.slideshow.MddPicLoaderFactory;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

public class AgentApplication extends Application {
	public List<mdict> md;
	public List<mdict> filters;
	public PDICMainAppOptions opt;
	public HashSet<String> mdlibsCon;

	static {
		GlideCacheModule.mOnGlideRegistry =
				registry -> registry.append(MddPic.class, InputStream.class, new MddPicLoaderFactory());
		CMNF.settings_class= SettingsActivity.class.getName();
		//	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		//            .detectAll()//监测所有内容
		//           .penaltyLog()//违规对log日志
		//            .penaltyDeath()//违规Crash
		//            .build());
		CMN.AssetMap.clear();
		CMN.AssetMap.put("/ASSET/liba.mdx", "李白全集-内置");
		CMN.AssetMap.put("/ASSET/", "【内置】");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		System.exit(0);
	}

	public void clearNonsenses() {
		md=null;
		filters=null;
		opt=null;
		mdlibsCon=null;
	}
}
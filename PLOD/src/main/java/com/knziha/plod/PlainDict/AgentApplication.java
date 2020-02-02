package com.knziha.plod.PlainDict;

import android.app.Application;
import android.graphics.Bitmap;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_manageable;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.slideshow.MddPic;
import com.knziha.plod.slideshow.MddPicLoaderFactory;
import com.knziha.plod.slideshow.PdfPic;
import com.knziha.plod.slideshow.PdfPicLoaderFactory;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AgentApplication extends Application {
	public HashMap<String,mdict> mdict_cache = new HashMap<>();
	public PDICMainAppOptions opt;
	public HashSet<String> mdlibsCon;

	static {
		GlideCacheModule.mOnGlideRegistry =
				registry -> {
					registry.append(MddPic.class, InputStream.class, new MddPicLoaderFactory());
					registry.append(PdfPic.class, Bitmap.class, new PdfPicLoaderFactory());
				};
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
	public SoftReference<char[]> _4kCharBuff;
	public ArrayList<PlaceHolder> slots;

	@Override
	public void onTerminate() {
		super.onTerminate();
		System.exit(0);
	}

	public void clearNonsenses() {
		mdict_cache=null;
		opt=null;
		mdlibsCon=null;
	}

	public char[] get4kCharBuff() {
		//if((_4kCharBuff==null?null:_4kCharBuff.get())!=null) CMN.Log("复用缓存!!!"); else CMN.Log("新建缓存!!!");
		return _4kCharBuff==null?null:_4kCharBuff.get();
	}

	public void set4kCharBuff(char[] cb) {
		_4kCharBuff=new SoftReference<>(cb);
	}
}
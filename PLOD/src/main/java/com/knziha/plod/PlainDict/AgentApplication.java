package com.knziha.plod.PlainDict;

import android.app.Application;
import android.graphics.Bitmap;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.plod.dictionary.Utils.MyIntPair;
import com.knziha.plod.dictionary.Utils.MyPair;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.settings.DictOptions;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.slideshow.MddPic;
import com.knziha.plod.slideshow.MddPicLoaderFactory;
import com.knziha.plod.slideshow.PdfPic;
import com.knziha.plod.slideshow.PdfPicLoaderFactory;
import com.knziha.rbtree.RashMap;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import db.LexicalDBHelper;

public class AgentApplication extends Application {
	/** transient */
	public HashMap<String,mdict> mdict_cache = new HashMap<>();
	/** per-dictionary configurations */
	public HashMap<CharSequence,byte[]> UIProjects = new HashMap<>();
	public HashSet<CharSequence> dirtyMap = new HashSet<>();
	public HashMap<String,String> fontNames = new HashMap<>();
	public PDICMainAppOptions opt;
	public HashSet<String> mdlibsCon;
	/** 控制所有实例只扫描一遍收藏夹 */
	public boolean bNeedPullFavorites =true;
	/** 退出全部实例时关闭、清理 */
	ArrayList<MyPair<String, LexicalDBHelper>> AppDatabases = new ArrayList<>();
	/** 退出全部实例时保留 */
	HashSet<Integer> selectedPositions;
	/** 退出全部实例时仍然保留 */
	HashMap<String, MyIntPair> databaseConext = new HashMap<>();
	/** 退出全部实例时关闭、清理 */
	LexicalDBHelper historyCon;
	
	public List<mdictRes> mdd;
	public PhotoBrowsingContext IBC;
	public String[] Imgs;
	public int currentImg;

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
		mdlibsCon=null;
		opt=null;
		mdd=null;
		IBC=null;
		Imgs=null;
	}

	public char[] get4kCharBuff() {
		//if((_4kCharBuff==null?null:_4kCharBuff.get())!=null) CMN.Log("复用缓存!!!"); else CMN.Log("新建缓存!!!");
		return _4kCharBuff==null?null:_4kCharBuff.get();
	}

	public void set4kCharBuff(char[] cb) {
		_4kCharBuff=new SoftReference<>(cb);
	}

	public HashSet<Integer> selectedPositions() {
		return selectedPositions!=null?selectedPositions:(selectedPositions=new HashSet<>(AppDatabases.size()));
	}

	public MyIntPair getLastContextualIndexByDatabaseFileName(String database) {
		return databaseConext.get(database);
	}

	public void putLastContextualIndexByDatabaseFileName(String database, int idx, int offset) {
		MyIntPair val = databaseConext.get(database);
		if(val!=null)
			val.set(idx, offset);
		else {
			val = new MyIntPair(idx, offset);
			databaseConext.put(database, val);
		}
	}

	public void closeDataBases() {
		//CMN.Log("关闭数据库");
		LexicalDBHelper vI;
		for(MyPair<String, LexicalDBHelper> itemI:AppDatabases){
			vI = itemI.value;
			if(vI!=null){
				itemI.value=null;
				vI.close();
			}
		}
	}
}
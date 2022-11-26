package com.knziha.plod.plaindict;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.paging.AppIconCover.AppIconCover;
import com.knziha.paging.AppIconCover.AppIconCoverLoaderFactory;
import com.knziha.plod.PlainUI.FloatApp;
import com.knziha.plod.PlainUI.FloatBtn;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.MyIntPair;
import com.knziha.plod.dictionary.Utils.MyPair;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.slideshow.MddPic;
import com.knziha.plod.slideshow.MddPicLoaderFactory;
import com.knziha.plod.slideshow.PdfPic;
import com.knziha.plod.slideshow.PdfPicLoaderFactory;
import com.knziha.plod.widgets.ViewUtils;

import org.knziha.metaline.Metaline;
import org.nanohttpd.protocols.http.ServerRunnable;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AgentApplication extends Application {
	/** transient */
	public HashMap<String, BookPresenter> mdict_cache = new HashMap<>();
	/** per-dictionary configurations */
	public HashMap<CharSequence,byte[]> BookProjects = new HashMap<>();
	public HashSet<CharSequence> dirtyMap = new HashSet<>();
	public HashMap<String,String> fontNames = new HashMap<>();
	public PDICMainAppOptions opt;
	public MainActivityUIBase.LoadManager loadManager;
	public HashSet<String> mdlibsCon;
	/** 控制所有实例只扫描一遍收藏夹 */
	public boolean bNeedPullFavorites =true;
	public MdictServer mServer;
	public Handler[] handles = new Handler[3];
	public FloatApp floatApp;
//	public FloatBtn floatBtn;
	/** 退出全部实例时关闭、清理 */
	ArrayList<MyPair<String, LexicalDBHelper>> AppDatabases = new ArrayList<>();
	ArrayList<MyPair<String, Long>> AppDatabasesV2 = new ArrayList<>();
	/** 退出全部实例时仍然保留 */
	HashMap<String, MyIntPair> databaseConext = new HashMap<>();
	public final static WeakReference<MainActivityUIBase>[] activities = new WeakReference[3];
	/** 退出全部实例时关闭、清理 */
	LexicalDBHelper historyCon;
	
	public BookPresenter book;
	public String[] Imgs;
	public int currentImg;
	
	public static class BufferAllocator {
		byte[] buffer = new byte[0];
		byte[] buffer_server = new byte[0];
		public byte[] AcquireCompressedBlockOfSize(int compressedSize, long max) {
			long tid = Thread.currentThread().getId();
			if(tid==CMN.mid) {
				if(buffer.length<max) {
					//CMN.Log("扩容", max, (int) (max*1.2f), currentDictionary.getDictionaryName());
					buffer = new byte[(int) (max*1.2f)];
				} else {
					//CMN.Log("复用缓存", max);
				}
				return buffer;
			} else if(tid == ServerRunnable.tid) {
				if(buffer_server.length<max) {
					//CMN.Log("服 务 器 扩容", max, (int) (max*1.2f), currentDictionary.getDictionaryName());
					buffer_server = new byte[(int) (max*1.2f)];
				} else {
					//CMN.Log("服 务 器 复用缓存", max);
				}
				return buffer_server;
			} else {
				//CMN.Log("复异步缓存", tid);
				return new byte[compressedSize];
			}
		}
		
		byte[] buffer1 = new byte[0];
		byte[] buffer1_server = new byte[0];
		public byte[] AcquireDeCompressedKeyBlockOfSize(int decompressedSize, long max) {
			long tid = Thread.currentThread().getId();
			if(tid==CMN.mid) {
				if(buffer1.length<max) {
					//CMN.Log("扩容 1", max, (int) (max*1.2f), currentDictionary.getDictionaryName());
					buffer1 = new byte[(int) (max*1.2f)];
				} else {
					//CMN.Log("复用缓存 1", max);
				}
				return buffer1;
			} else if(tid == ServerRunnable.tid) {
				if(buffer1_server.length<max) {
					//CMN.Log("服 务 器 扩容 1", max, (int) (max*1.2f), currentDictionary.getDictionaryName());
					buffer1_server = new byte[(int) (max*1.2f)];
				} else {
					//CMN.Log("服 务 器 复用缓存 1", max);
				}
				return buffer1_server;
			} else {
				//CMN.Log("复异步缓存 1", tid);
				return new byte[decompressedSize];
			}
		}
	}
	public final static BufferAllocator BufferAllocatorInst = new BufferAllocator();
	
	@Metaline(file = "src\\main\\assets\\李白全集.mdx")
	final static int liba_filesize = 0;
	
	static {
		GlideCacheModule.mOnGlideRegistry =
				registry -> {
					registry.append(MddPic.class, InputStream.class, new MddPicLoaderFactory());
					registry.append(PdfPic.class, Bitmap.class, new PdfPicLoaderFactory());
					registry.append(AppIconCover.class, Drawable.class, new AppIconCoverLoaderFactory());
				};
		CMNF.settings_class= SettingsActivity.class.getName();
		//	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		//            .detectAll()//监测所有内容
		//           .penaltyLog()//违规对log日志
		//            .penaltyDeath()//违规Crash
		//            .build());
		//CMN.AssetMap.put("liba", "李白全集");
		CMN.AssetMap.put("李白全集.mdx", (long) liba_filesize);
		//CMN.AssetMap.put("", "【内置】");
	}
	public SoftReference<char[]> _4kCharBuff;

	@Override
	public void onTerminate() {
		super.onTerminate();
		CMN.debug("onTerminate");
		System.exit(0);
	}

	public void clearTmp() {
		mdict_cache=null;
		mdlibsCon=null;
		opt=null;
		book=null;
		Imgs=null;
	}

	public char[] get4kCharBuff() {
		//if((_4kCharBuff==null?null:_4kCharBuff.get())!=null) CMN.Log("复用缓存!!!"); else CMN.Log("新建缓存!!!");
		return _4kCharBuff==null?null:_4kCharBuff.get();
	}

	public void set4kCharBuff(char[] cb) {
		_4kCharBuff=new SoftReference<>(cb);
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
		CMN.debug("关闭数据库");
		LexicalDBHelper vI;
		for(MyPair<String, LexicalDBHelper> itemI:AppDatabases){
			vI = itemI.value;
			if(vI!=null){
				itemI.value=null;
				vI.close();
			}
		}
		if (historyCon!=null) {
			historyCon.close();
			historyCon = null;
		}
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		for (int i = 0; i < 3; i++) {
			if (activities[i] == null) {
				activities[i] = ViewUtils.DummyRef;
			}
		}
		if (SDK_INT<=20) {
//			androidx.multidex.MultiDex.install(this);
			com.bytedance.boost_multidex.BoostMultiDex.install(base);
		}
		else if (SDK_INT >= Build.VERSION_CODES.P) {
			try {
				Method forName = Class.class.getDeclaredMethod("forName", String.class);
				Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
				Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
				Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
				Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
				Object sVmRuntime = getRuntime.invoke(null);
				setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
				//CMN.Log("reflect bootstrap :");
			} catch (Throwable e) {
				//CMN.Log("reflect bootstrap failed:", e);
			}
		}
	}
}
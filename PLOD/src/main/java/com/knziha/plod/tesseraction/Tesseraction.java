package com.knziha.plod.tesseraction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import com.knziha.plod.plaindict.CMN;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

 /** 桥阶层，反射调用插件代码 */
public class Tesseraction {
	private Object plugin;
	private Context pluginContext;
	public boolean inited;
	
	private static Method mOcrInit;
	private static Method mOcrBitmap;
	private static Method mOcrData;
	private static Method mWordRects;
	private static Method mRectangle;
	private static Method mHOCRText;
	private static Method mUTF8Text;
	private static Method mStop;
	
	private static Method mQrBitmap;
	private static Method mQrData;
	private static Method mQrReset;
	
	public static Class<?> PluginClazz;
	
	public void init(Context context) throws Exception {
		String pluginPkg = "com.googlecode.tesseraction";
		if (PluginClazz == null) {
			context = context.createPackageContext(pluginPkg, Context.CONTEXT_INCLUDE_CODE
					| Context.CONTEXT_IGNORE_SECURITY
					| Context.CONTEXT_RESTRICTED
			);
			Class clazz = context.getClassLoader().loadClass("com.googlecode.tesseract.android.Tesseraction");
			
			mOcrInit = clazz.getMethod("initTessdata", Context.class, Activity.class, String.class, String.class);
			mOcrData = clazz.getMethod("setImage", byte[].class, int.class, int.class, int.class, int.class);
			mOcrBitmap = clazz.getMethod("setImage", Bitmap.class);
			
			mWordRects = clazz.getMethod("getWordRects");
			mRectangle = clazz.getMethod("setRectangle", int.class, int.class, int.class, int.class);
			mHOCRText = clazz.getMethod("getHOCRText", int.class);
			mUTF8Text = clazz.getMethod("getUTF8Text");
			mStop = clazz.getMethod("stop");
			
			mQrBitmap = clazz.getMethod("decodeQrBitmap", Bitmap.class);
			mQrData = clazz.getMethod("decodeQrData", byte[].class, int.class, int.class, int.class, int.class, int.class, int.class, boolean.class, boolean.class, boolean.class);
			mQrReset = clazz.getMethod("resetQrDecoder");
			
			PluginClazz = clazz;
			CMN.debug("融合图文之心成功……", context.getPackageName());
			
			pluginContext = context;
		}
		plugin = PluginClazz.getConstructors()[0].newInstance();
	}
	
	public void initTessdata(Activity activity, String path, String languages) throws InvocationTargetException, IllegalAccessException {
		mOcrInit.invoke(plugin, pluginContext, activity, path, languages);
	}
	
	public void setImage(byte[] data, int w, int h, int bpp, int bpl) throws InvocationTargetException, IllegalAccessException {
		mOcrData.invoke(plugin, data, w, h, bpp, bpl);
	}
	
	public void setImage(Bitmap bitmap) throws InvocationTargetException, IllegalAccessException {
		mOcrBitmap.invoke(plugin, bitmap);
	}
	
	public ArrayList<Rect> getWordRects() throws InvocationTargetException, IllegalAccessException {
		return (ArrayList<Rect>) mWordRects.invoke(plugin);
	}
	
	public void setRectangle(int left, int top, int width, int height) throws InvocationTargetException, IllegalAccessException {
		mRectangle.invoke(plugin, left, top, width, height);
	}
	
	public String getHOCRText(int page) throws InvocationTargetException, IllegalAccessException {
		return (String) mHOCRText.invoke(plugin, page);
	}
	
	public String getUTF8Text() throws InvocationTargetException, IllegalAccessException {
		return (String) mUTF8Text.invoke(plugin);
	}
	
	public String decodeQrBitmap(Bitmap bitmap) throws InvocationTargetException, IllegalAccessException {
		return (String) mQrBitmap.invoke(plugin, bitmap);
	}
	
	public String decodeQrData(byte[] data, int sWidth, int sHeight, int left, int top, int widthwidth, int heightheight, boolean rotate, boolean invert, boolean rotated) throws InvocationTargetException, IllegalAccessException {
		return (String) mQrData.invoke(plugin, data, sWidth, sHeight, left, top, widthwidth, heightheight, rotate, invert, rotated);
	}
	
	public void resetQrDecoder() throws InvocationTargetException, IllegalAccessException {
		mQrReset.invoke(plugin);
	}
	
	public void stop() throws InvocationTargetException, IllegalAccessException {
		mStop.invoke(plugin);
	}
	
	public void resetZxingArgs() {
		if (plugin!=null) {
			try {
				//mQrReset.invoke(plugin);
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
}

package com.knziha.plod.PlainDict;

import java.lang.reflect.Method;

public class TestHelper {
	static{
		CMN.Log("IC_LOADED!!!");
	}
	
	
	static void testClassLoading(Class loader){
		
		try {
//			Method f=ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
//			f.setAccessible(true);
//			CMN.Log("IC_", f.invoke(loader,
//					"com.knziha.plod.PlainDict.MainActivityUIBase$SaveAndRestorePagePosDelegate"));
//
//			CMN.Log("IC_2", f.invoke(loader, "com.knziha.plod.PlainDict.MainActivityUIBase$ICTest"));
		
		
		} catch (Exception e) {
			CMN.Log("IC_",e);
		}
	}
}

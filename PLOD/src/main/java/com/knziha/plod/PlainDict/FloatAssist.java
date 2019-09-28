package com.knziha.plod.PlainDict;

import com.knziha.plod.dictionarymodels.mdict;

import java.util.HashMap;

@Deprecated
public class FloatAssist {  
  
    /** 
     * 内部类实现单例模式 
     * 延迟加载，减少内存开销 
     *  
     * @author xuzhaohu 
     *  
     */  
    private static class SingletonHolder {  
        private static FloatAssist instance = new FloatAssist();  
    }

	protected static String A = null;

	public HashMap<String, mdict> tmpHash_0_mdictCache;
  
    /** 
     * 私有的构造函数 
     */  
    private FloatAssist() {  
    	tmpHash_0_mdictCache=new HashMap<String,mdict>();
    	A="a";
    }  
  
    public static FloatAssist getInstance() {  
        return SingletonHolder.instance;  
    }  
  
    protected void method() {  
        System.out.println("SingletonInner");  
    }  
}
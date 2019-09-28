package com.knziha.plod.dictionarymodels;

import java.io.File;

import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.knziha.plod.PlainDict.PDICMainAppOptions;

/*
 ui side of mdict
 data:2018.07.30
 author:KnIfER
*/


public class mdict_prempter extends mdict_nonexist {
	//构造
	public mdict_prempter(String fn, PDICMainAppOptions opt_) {
		super();
		opt=opt_;
		f = new File(fn);
        _Dictionary_fName = f.getName();
    	int tmpIdx = _Dictionary_fName.lastIndexOf(".");
    	if(tmpIdx!=-1) {
	    	_Dictionary_fSuffix = _Dictionary_fName.substring(tmpIdx+1);
	    	_Dictionary_fName = _Dictionary_fName.substring(0, tmpIdx);
    	}
        String fnTMP = f.getName();
        
        File f2 = new File(f.getParentFile().getAbsolutePath()+"/"+fnTMP.substring(0,fnTMP.lastIndexOf("."))+".mdd");
    	if(f2.exists()){
			mdd=new mdictRes_prempter(f2.getAbsolutePath());
    	}
	}
	
	
	WebChromeClient myWebCClient = null;
	WebViewClient myWebClient = null;
	public boolean isAsset=false;
}

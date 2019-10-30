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
		this(fn, opt_, false);
	}

	public mdict_prempter(String fn, PDICMainAppOptions opt_, boolean isF) {
		super(fn, opt_, isF);
		String fnTMP = f.getName();

		File f2 = new File(f.getParentFile().getAbsolutePath()+"/"+fnTMP.substring(0,fnTMP.lastIndexOf("."))+".mdd");
		if(f2.exists()){
			mdd=new mdictRes_prempter(f2.getAbsolutePath());
		}
	}


	WebChromeClient myWebCClient = null;
	WebViewClient myWebClient = null;
}

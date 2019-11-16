package com.knziha.plod.dictionarymodels;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.knziha.plod.PlainDict.PDICMainAppOptions;

/*
 UI side of mdict
 data:2018.07.30
 author:KnIfER
*/
public class mdict_prempter extends mdict_nonexist {
	//构造
	public mdict_prempter(String fn, PDICMainAppOptions opt_) throws IOException {
		this(fn, opt_, false);
	}

	public mdict_prempter(String fn, PDICMainAppOptions opt_, boolean isF) throws IOException {
		super(fn, opt_, isF);
		String fnTMP = f.getName();

		File f2 = new File(f.getParentFile().getAbsolutePath()+"/"+fnTMP.substring(0,fnTMP.lastIndexOf("."))+".mdd");
		if(f2.exists()){
			mdd= Collections.singletonList(new mdictRes_prempter(f2.getAbsolutePath()));
		}
	}
}

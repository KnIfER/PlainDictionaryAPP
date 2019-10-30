package com.knziha.plod.dictionarymodels;

import java.io.File;
import java.io.IOException;

import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.knziha.plod.PlainDict.PDICMainAppOptions;

/*
 ui side of mdict
 data:2018.07.30
 author:KnIfER
*/


public class mdict_nonexist extends mdict {	
	//构造
	public mdict_nonexist(String fn, PDICMainAppOptions opt_) {
		this(fn, opt_, false);
	}

	public mdict_nonexist(String fn, PDICMainAppOptions opt_, boolean isF) {
		opt=opt_;
		fn = new File(fn).getAbsolutePath();
		f = new File(fn);
		_Dictionary_fName = f.getName();
		int tmpIdx = _Dictionary_fName.lastIndexOf(".");
		if(tmpIdx!=-1) {
			_Dictionary_fSuffix = _Dictionary_fName.substring(tmpIdx+1);
			_Dictionary_fName = _Dictionary_fName.substring(0, tmpIdx);
		}
		tmpIsFilter=isF;

		_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
		_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");

		try {
			readInConfigs();
		} catch (IOException ignored) { }
	}

	@Override
	public boolean moveFileTo(File newF) {
		File fP = newF.getParentFile();
		fP.mkdirs();
		boolean ret = false;
		//boolean pass = !f.exists();
		if(fP.exists() && fP.isDirectory()) {
			ret=true;
			_Dictionary_fName = newF.getName();
	    	int tmpIdx = _Dictionary_fName.lastIndexOf(".");
	    	if(tmpIdx!=-1) {
		    	_Dictionary_fSuffix = _Dictionary_fName.substring(tmpIdx+1);
		    	_Dictionary_fName = _Dictionary_fName.substring(0, tmpIdx);
	    	}
		}
		String _Dictionary_fName_InternalOld = _Dictionary_fName_Internal;
		if(ret) {
			f=newF;
			String fn = newF.getAbsolutePath();
			_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
			_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");
		}
		new File(opt.pathTo().append(_Dictionary_fName_InternalOld).toString()).renameTo(new File(opt.pathTo().append(_Dictionary_fName_Internal).toString()));

		return ret;
	}
	
	WebChromeClient myWebCClient = null;
	WebViewClient myWebClient = null;
	public boolean isAsset=false;
}

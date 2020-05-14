package com.knziha.plod.dictionarymodels;

import android.app.Activity;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import com.knziha.plod.PlainDict.PDICMainAppOptions;

/*
 UI side of mdict
 data:2018.07.30
 author:KnIfER
*/
public class mdict_prempter extends mdict_transient {
	public boolean isAsset;

	//构造
	public mdict_prempter(Activity a, String fn, PDICMainAppOptions opt_, mdict_nonexist mninstance) throws IOException {
		this(a, fn, opt_, 0, mninstance);
	}

	public mdict_prempter(Activity a,String fn, PDICMainAppOptions opt_, int isF, mdict_nonexist mninstance) throws IOException {
		super(a, fn, opt_, isF, mninstance);
		String fnTMP = f.getName();

		File f2 = new File(f.getParentFile().getAbsolutePath()+"/"+fnTMP.substring(0,fnTMP.lastIndexOf("."))+".mdd");
		if(f2.exists()){
			mdd = Collections.singletonList(new mdictRes_prempter(f2.getAbsolutePath()));
		}
	}
}

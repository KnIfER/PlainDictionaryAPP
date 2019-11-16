package com.knziha.plod.dictionarymodels;

import com.knziha.plod.PlainDict.MainActivityUIBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/*
 mdict from android asset.
 date:2019.03.11
 author:KnIfER
*/
public class mdict_asset extends mdict {

	public static final String AsstPrefix="/ASSET/";

	//构造
	public mdict_asset(String fn, MainActivityUIBase a_) throws IOException {
		super(processFileName(fn), a_);
		_Dictionary_fName=new File(fn).getName();
		init();
	}

	private static String processFileName(String fn) {
		if(!fn.startsWith(AsstPrefix)) fn=AsstPrefix+fn;
		return fn;
	}

	@Override
	protected InputStream mOpenInputStream() throws IOException {
		return a.getResources().getAssets().open(f.getAbsolutePath().substring(AsstPrefix.length()));
	}

	@Override
	protected boolean StreamAvailable() {
		return a!=null;
	}
}

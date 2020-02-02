package com.knziha.plod.dictionarymodels;

import com.knziha.plod.PlainDict.MainActivityUIBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.knziha.plod.PlainDict.CMN.AssetTag;

/*
 mdict from android asset.
 date:2019.03.11
 author:KnIfER
*/
public class mdict_asset extends mdict {

	//构造
	public mdict_asset(String fn, MainActivityUIBase a_) throws IOException {
		super(processFileName(fn), a_);
		_Dictionary_fName=new File(fn).getName();
		init(getStreamAt(0));
	}

	private static String processFileName(String fn) {
		if(!fn.startsWith(AssetTag)) fn= AssetTag +fn;
		return fn;
	}

	@Override
	protected InputStream mOpenInputStream() throws IOException {
		return a.getResources().getAssets().open(f.getAbsolutePath().substring(AssetTag.length()));
	}

	@Override
	protected boolean StreamAvailable() {
		return a!=null;
	}
}

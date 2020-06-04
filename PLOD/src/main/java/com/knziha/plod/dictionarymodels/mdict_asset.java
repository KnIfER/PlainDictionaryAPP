package com.knziha.plod.dictionarymodels;

import android.content.Context;

import com.knziha.plod.PlainDict.MainActivityUIBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.knziha.plod.PlainDict.CMN.AssetMap;
import static com.knziha.plod.PlainDict.CMN.AssetTag;

/*
 mdict from android asset.
 date:2019.03.11
 author:KnIfER
*/
public class mdict_asset extends mdict {
	//构造
	public mdict_asset(File fn, MainActivityUIBase a_) throws IOException {
		super(fn, a_, 0, a_);
	}

	@Override
	protected InputStream mOpenInputStream() throws IOException {
		return a.getResources().getAssets().open(f.getAbsolutePath().substring(AssetTag.length()));
	}
	
	@Override
	protected boolean StreamAvailable() {
		if(tag instanceof MainActivityUIBase) {
			a = (MainActivityUIBase) tag;
		}
		return a!=null;
	}
	
	@Override
	public String getDictionaryName() {
		String name = AssetMap.get(f.getPath());
		if(name!=null) {
			return name;
		}
		return super.getDictionaryName();
	}
}

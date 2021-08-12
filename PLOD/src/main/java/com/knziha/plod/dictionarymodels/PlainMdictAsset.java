package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.MainActivityUIBase;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.knziha.plod.plaindict.CMN.AssetMap;
import static com.knziha.plod.plaindict.CMN.AssetTag;

/*
 mdict from android asset.
 date:2019.03.11
 author:KnIfER
*/
public class PlainMdictAsset extends PlainMdict {
	private MainActivityUIBase a;
	
	public PlainMdictAsset(File fn, int pseudoInit, StringBuilder buffer, Object tag) throws IOException {
		super(fn, pseudoInit, buffer, tag, false);
	}
	//构造
	
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

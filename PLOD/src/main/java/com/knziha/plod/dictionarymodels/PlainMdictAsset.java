package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.mdict;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.knziha.plod.plaindict.CMN.AssetMap;

/*
 mdict from android asset.
 date:2019.03.11
 author:KnIfER
*/
public class PlainMdictAsset extends PlainMdict {
	public PlainMdictAsset(String fn) throws IOException {
		super(fn);
	}
	
	public PlainMdictAsset(File fn, int pseudoInit, StringBuilder buffer, Object tag) throws IOException {
		super(fn, pseudoInit, buffer, tag);
	}
	
	protected PlainMdictAsset(mdict master, DataInputStream data_in, long _ReadOffset) throws IOException {
		super(master, data_in, _ReadOffset);
	}
	//构造
	
	@Override
	protected InputStream mOpenInputStream() throws IOException {
		//return a.getResources().getAssets().open(f.getAbsolutePath().substring(AssetTag.length()));
		return null;
	}
	
	@Override
	protected boolean StreamAvailable() {
		//if(tag instanceof MainActivityUIBase) {
		//	a = (MainActivityUIBase) tag;
		//}
		//return a!=null;
		return false;
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

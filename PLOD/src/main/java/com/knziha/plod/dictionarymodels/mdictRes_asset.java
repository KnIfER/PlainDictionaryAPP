package com.knziha.plod.dictionarymodels;

import android.content.Context;

import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.dictionary.mdictRes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.knziha.plod.PlainDict.CMN.AssetTag;

/*
 mdict from android asset.
 date:2019.03.11
 author:KnIfER
*/
public class mdictRes_asset extends mdictRes {
	Context context;

	//构造
	public mdictRes_asset(File fn, MainActivityUIBase a_) throws IOException {
		super(fn);
		context=a_.getBaseContext();
		init(getStreamAt(0, true));
	}

	@Override
	protected InputStream mOpenInputStream() throws IOException {
		return context.getResources().getAssets().open(f.getAbsolutePath().substring(AssetTag.length()));
	}

	@Override
	protected boolean StreamAvailable() {
		return context!=null;
	}
}

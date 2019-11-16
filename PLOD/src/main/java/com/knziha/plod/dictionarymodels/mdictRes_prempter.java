package com.knziha.plod.dictionarymodels;

import java.io.File;
import java.io.IOException;

import com.knziha.plod.dictionary.mdictRes;

/*
 ui side of mdict
 data:2018.07.30
 author:KnIfER
*/


public class mdictRes_prempter extends mdictRes {
	//构造
	public mdictRes_prempter(String fn) throws IOException {
		super(null);
		f = new File(fn);
	}
}

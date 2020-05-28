package com.knziha.plod.dictionarymodels;

import java.io.File;
import java.io.IOException;

public class mdict_nonexist extends mdict {
	//构造
	public mdict_nonexist(File fn) throws IOException {
		super(fn, null, true);
	}
}

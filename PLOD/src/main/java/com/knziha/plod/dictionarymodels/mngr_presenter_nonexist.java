package com.knziha.plod.dictionarymodels;

import java.io.File;
import java.io.IOException;

public class mngr_presenter_nonexist extends BookPresenter {
	//构造
	public mngr_presenter_nonexist(File fn) throws IOException {
		super(fn, null, 3);
		bookImpl = new DictionaryAdapter(fn, null);
	}
}

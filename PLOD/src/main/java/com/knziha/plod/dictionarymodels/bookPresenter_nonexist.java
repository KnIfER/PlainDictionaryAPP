package com.knziha.plod.dictionarymodels;

import java.io.File;
import java.io.IOException;

public class bookPresenter_nonexist extends BookPresenter {
	//构造
	public bookPresenter_nonexist(File fn) throws IOException {
		super(fn, null, 1, null);
	}
}

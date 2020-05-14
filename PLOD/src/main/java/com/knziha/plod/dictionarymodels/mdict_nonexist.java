package com.knziha.plod.dictionarymodels;

import java.io.IOException;

public class mdict_nonexist extends mdict {
	//构造
	public mdict_nonexist() throws IOException {
		super(null, null);
	}

	@Override
	protected void initLogically() {
		_num_record_blocks=-1;
	}
}

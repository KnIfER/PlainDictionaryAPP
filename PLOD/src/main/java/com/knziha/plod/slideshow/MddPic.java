package com.knziha.plod.slideshow;

import com.knziha.plod.dictionary.mdictRes;

import java.util.List;

public class MddPic {
	final List<mdictRes> mdd;
	final String path;

	public MddPic(List<mdictRes> mdd, String key) {
		this.mdd = mdd;
		this.path = key;
	}
}
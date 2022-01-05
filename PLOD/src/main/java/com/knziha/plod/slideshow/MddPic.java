package com.knziha.plod.slideshow;

import com.knziha.plod.dictionary.UniversalDictionaryInterface;

import java.io.InputStream;

public class MddPic {
	final UniversalDictionaryInterface book;
	final String path;

	public MddPic(UniversalDictionaryInterface book, String key) {
		this.book = book;
		this.path = key;
	}
	
	public InputStream load() {
		return book.getResourceByKey(path);
	}
}
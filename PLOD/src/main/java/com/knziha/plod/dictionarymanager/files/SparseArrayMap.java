package com.knziha.plod.dictionarymanager.files;

import android.util.SparseArray;

import com.knziha.plod.dictionary.Utils.GetIndexedString;

public class SparseArrayMap extends SparseArray<String> implements GetIndexedString {
	public SparseArrayMap(int i) {
		super(i);
	}
}

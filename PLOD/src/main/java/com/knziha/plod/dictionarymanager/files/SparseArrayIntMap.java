package com.knziha.plod.dictionarymanager.files;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.knziha.plod.dictionary.Utils.GetIndexedInteger;

public class SparseArrayIntMap extends SparseArray implements GetIndexedInteger {
	public SparseArrayIntMap(int i) {
		super(i);
	}

	@Override
	public boolean contains(int key) {
		return super.indexOfKey(key)>0;
	}
}

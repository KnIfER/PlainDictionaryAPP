package com.knziha.plod.dictionary.Utils;

import java.util.LinkedHashMap;

public class LinkastReUsageHashMap<K,V> extends LinkedHashMap<K,V> {
	int mCapacity;
	public LinkastReUsageHashMap(int Capacity) {
		super(Capacity, 1, true);
		mCapacity = Capacity-6;
	}

	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size()>mCapacity;
	}
}

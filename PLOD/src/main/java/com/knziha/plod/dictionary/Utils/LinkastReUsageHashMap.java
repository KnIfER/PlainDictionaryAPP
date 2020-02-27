package com.knziha.plod.dictionary.Utils;


import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkastReUsageHashMap<K,V> extends LinkedHashMap<K,V> {
	int mCapacity;
	public AtomicInteger accommodation;
	private Field f_accessOrder;

	public LinkastReUsageHashMap(int Capacity) {
		super(Capacity, 1, true);
		mCapacity = Capacity-6;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size()>mCapacity;
	}

	public boolean filled() {
		return size()>mCapacity;
	}

	public void syncAccommodationSize() {
		if(accommodation==null)
			accommodation = new AtomicInteger(0);
		accommodation.set(mCapacity-size());
	}

	public V getSafe(Object key) {
		try {
			if(f_accessOrder==null) {
				f_accessOrder = LinkedHashMap.class.getDeclaredField("accessOrder");
				f_accessOrder.setAccessible(true);
			}
			f_accessOrder.set(this, false);
			V val = get(key);
			f_accessOrder.set(this, true);
			return val;
		} catch (Exception ignored) {
			SU.Log(ignored);
		}
		return super.get(key);
	}
}

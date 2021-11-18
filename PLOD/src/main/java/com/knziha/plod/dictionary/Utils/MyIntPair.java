package com.knziha.plod.dictionary.Utils;

public class MyIntPair implements Comparable<MyIntPair> {
	public int key;
	public int value;
	public MyIntPair(int k, int v){
		key=k;value=v;
	}

	public void set(int k, int v){
		key=k;value=v;
	}
	
	@Override
	public int compareTo(MyIntPair o) {
		return o.key - key;
	}
}
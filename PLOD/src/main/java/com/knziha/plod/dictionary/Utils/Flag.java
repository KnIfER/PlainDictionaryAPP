package com.knziha.plod.dictionary.Utils;

public class Flag extends F1ag implements Comparable<Flag>{
    public String data;
	
	@Override
	public int compareTo(Flag o) {
		int cmp = val - o.val;
		return cmp;
	}
}

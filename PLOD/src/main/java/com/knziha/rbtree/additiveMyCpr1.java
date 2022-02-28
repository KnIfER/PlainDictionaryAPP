package com.knziha.rbtree;
import com.knziha.plod.dictionary.mdict;

import java.util.ArrayList;


public class additiveMyCpr1 implements Comparable<additiveMyCpr1>{
	public String key;
	public Object value;
	public long realm;
	public long realmCount;
	public int LongestStartWithSeqLength;
	public additiveMyCpr1(String k,Object v){
		key=k;value=v;
	}
	public additiveMyCpr1(){
	}
	public int compareTo(additiveMyCpr1 other) {
		//CMN.show(this.key.replaceAll(CMN.replaceReg,CMN.emptyStr));
		return mdict.processText(this.key.toLowerCase()).compareTo(mdict.processText(other.key.toLowerCase()));
	}
	public String toString(){
		String str = ""; for(Object i:(ArrayList)value) str+="@"+i;
		return key+"____"+str;
	}
	
	/** dangerous !  searchKey 必须小写。
	 * 	newKey 值须经由 mdict.processText 相等。*/
	public void handleKeyClash(String searchKey, String newKey){
		if(LongestStartWithSeqLength==0) {
			LongestStartWithSeqLength = calcLongestStartWithSeqLength(searchKey, key);
		}
		int lswqLen = calcLongestStartWithSeqLength(searchKey, newKey);
		if (lswqLen>LongestStartWithSeqLength) {
			key = newKey;
			LongestStartWithSeqLength = lswqLen;
		}
	}
	
	private int calcLongestStartWithSeqLength(String searchKey, String key) {
		int offset=0;
		int length=Math.min(searchKey.length(), key.length());
		while (offset<length) {
			if (Character.toLowerCase(key.charAt(offset)) != searchKey.charAt(offset)) {
				return offset;
			}
			offset++;
		}
		return offset-1;
	}
}
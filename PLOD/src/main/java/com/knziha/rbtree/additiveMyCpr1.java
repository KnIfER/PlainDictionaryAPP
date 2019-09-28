package com.knziha.rbtree;
import com.knziha.plod.dictionary.mdict;




public class additiveMyCpr1 implements Comparable<additiveMyCpr1>{
	public String key;
	public Object value;
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
		String str = ""; //for(Integer i:value) str+="@"+i;
		return key+"____"+str;
	}
}
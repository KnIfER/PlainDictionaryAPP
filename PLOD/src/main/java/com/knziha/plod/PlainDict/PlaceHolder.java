package com.knziha.plod.PlainDict;

import com.knziha.plod.dictionarymodels.mdict;

import java.util.ArrayList;

public class PlaceHolder {
	public String pathname;
	public String name;
	public int tmpIsFlag;
	public int lineNumber;

	public PlaceHolder(String line) {
		pathname = line;
		int start = line.lastIndexOf("/");
		if(start<0) start=0; else start++;
		int end = line.length();
		int tmpIdx = pathname.length()-4;
		if(tmpIdx>0
			&& pathname.charAt(tmpIdx)=='.' && pathname.regionMatches(true, tmpIdx+1, "mdx" ,0, 3)){
			end -= 4;
		}
		name = pathname.substring(start, end);
	}

	public PlaceHolder(String name, ArrayList<PlaceHolder> md) {
		this(name);
		if(md.size()>0){
			lineNumber = md.get(md.size()-1).lineNumber+1;
		}
	}


	public String getPath(PDICMainAppOptions opt) {
		if (!pathname.startsWith("/"))
			pathname = opt.lastMdlibPath + "/" + pathname;
		return pathname;
	}
}

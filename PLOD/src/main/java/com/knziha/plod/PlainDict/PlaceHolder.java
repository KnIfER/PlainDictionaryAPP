package com.knziha.plod.PlainDict;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

public class PlaceHolder {
	public String pathname;
	public String ErrorMsg;
	private CharSequenceKey name;
	public int tmpIsFlag;
	public int lineNumber;

	public PlaceHolder(String line) {
		pathname = line;
	}

	public PlaceHolder(String name, ArrayList<PlaceHolder> md) {
		this(name);
		if(md.size()>0){
			lineNumber = md.get(md.size()-1).lineNumber+1;
		}
	}

	public PlaceHolder() {

	}

	public File getPath(PDICMainAppOptions opt) {
		File ret;
		if (!pathname.startsWith("/")){
			ret = new File(opt.lastMdlibPath, pathname);
		} else {
			ret = new File(pathname);
		}
		pathname = ret.getPath();
		return ret;
	}

	@NonNull
	@Override
	public String toString() {
		return pathname+" : "+hashCode();
	}
	
	public CharSequence getName() {
		if(name==null || !name.equalsInternal(pathname)){
			name = new CharSequenceKey().setAsMdName(pathname);
		}
		return name;
	}
	
	public void Rebase(File f) {
		pathname=f.getPath();
		name=null;
	}
	
	public CharSequence getSoftName() {
		return CharSequenceKey.Instance.setAsMdName(pathname);
	}
}

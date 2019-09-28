package com.knziha.plod.dictsmanager.files;

import java.io.File;
import java.net.URI;

public class mAssetFile extends mFile{
	public mAssetFile(String pathname) {
		super(pathname);
	}

	public mAssetFile(File to) {
		super(to);
	}
	
	public mAssetFile(String pathname,boolean isDir) {
		super(pathname,isDir);
	}
	
	public mAssetFile(String parent, String child) {
		super(parent, child);
	}

	
	public mAssetFile(URI uri) {
		super(uri);
	}
	
	
	//public boolean exists() {
	//	return 
	//}

}

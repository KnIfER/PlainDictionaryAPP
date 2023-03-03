package com.knziha.plod.dictionarymanager.files;

import com.knziha.plod.dictionarymanager.BookManagerWebsites;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.widgets.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class mFile extends File{
	public List<mFile> children = ViewUtils.EmptyArray; // the collpaed children folder holder
	public String CSTR;//子系文件标识符
	public boolean bInIntrestedDir=false;
	protected boolean isDirectory=false;
	public BookManagerWebsites.WebAssetDesc webAsset;
	public mFile(File parent, String child) {
		super(parent, child);
	}
	
	public boolean getIsDirectory() {
		return isDirectory;
	}
	
	public mFile(String pathname) {
		super(pathname);
	}

	public mFile(String pathname, BookManagerWebsites.WebAssetDesc webAsset) {
		super(pathname);
		this.webAsset = webAsset;
		if (!pathname.endsWith(".webx")) {
			children = new ArrayList<>();
		}
	}
	
	public mFile(File to) {
		super(getEUPath(to));
	}
	
	public mFile(String pathname,boolean isDir) {
		super(pathname);
		isDirectory=isDir;
		if (isDir) {
			children = new ArrayList<>();
		}
	}
	
	public mFile(String parent, String child) {
		super(parent, child);
	}

	
	public mFile(URI uri) {
		super(uri);
	}
	
	boolean cpr1,cpr2;
	public mFile init(PDICMainAppOptions opt) {
		//Log.e("init__fatal",getAbsolutePath());
		isDirectory=isDirectory();
		if (isDirectory && children == ViewUtils.EmptyArray) {
			children = new ArrayList<>();
		}
		if(getPath().length()>1) {
			String ParentPath = getParent();
			if (ParentPath!=null) {
				boolean isS,isDS;
				String parent = opt.lastMdlibPath.getPath();
				if(ParentPath.startsWith(parent)) {
					isS=true;
					isDS=ParentPath.length()==parent.length();
					cpr1=(!isDS||isDirectory);
					cpr2=isDS&&!isDirectory;
				}
			}
		}
		return this;
	}

	private static String getEUPath(File fn) {
		try {
			return fn.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fn.getAbsolutePath();
	}

	@Override
	public boolean isDirectory() {
		if(isDirectory) return isDirectory;
		return super.isDirectory();
	}
	
	@Override
	public int compareTo(File other) {
		mFile THIS = this;
		//if(other.getClass()==mFile.class) {
			mFile otherF = (mFile)other;
			if(otherF.CSTR!=null && CSTR!=null)
				return CSTR.compareToIgnoreCase(otherF.CSTR);
			
			if(cpr1 && otherF.cpr2) {
				return -1;
			}
			if(cpr2 && otherF.cpr1) {
				return 1;
			}
		//}
		
		boolean b1=other.isDirectory(),b2=this.isDirectory();
		if(!b1 && !b2 ){
			int ret=THIS.getParentFile().compareTo(other.getParentFile());
			if(ret!=0)
				return ret;//先比较父文件夹
		}else if(b1 && ! b2) {
			if(!isScionOf(this, other))//若无父子关系，则比价父文件夹
				THIS=THIS.getParentFile();
		} else if(!b1 && b2) {
			if(!isScionOf(other, this))
				other=other.getParentFile();
		}
		
		try {
			return THIS.getCanonicalPath().compareToIgnoreCase(other.getCanonicalPath());
		}catch(Exception e) {}
		return THIS.getAbsolutePath().compareToIgnoreCase(other.getAbsolutePath());
	}
	
	
	private static boolean isScionOf(File fin, File other) {
		try {return fin.getParentFile().getCanonicalPath().toLowerCase().startsWith(other.getCanonicalPath().toLowerCase());}catch(Exception e) {}
		return fin.getParentFile().getAbsolutePath().toLowerCase().startsWith(other.getAbsolutePath().toLowerCase());
	}

	public static boolean isScionOf(File fin,String parentFile) {
		return isScionOf(fin,new File(parentFile));
	}
	public static String removeFolderPrefix(File fin, File parentFile) {
		try {
			String f1=fin.getCanonicalPath();
			String f2=parentFile.getCanonicalPath();
			if(f1.toLowerCase().startsWith(f2.toLowerCase()))
				return  f1.substring(f2.length()+1);
		}catch(Exception e) {}
		String f1=fin.getAbsolutePath();
		String f2=parentFile.getAbsolutePath();
		if(f1.toLowerCase().startsWith(f2.toLowerCase()) &&f1.length()>f2.length())
			return  f1.substring(f2.length()+1);
		return f1;
	}
	public static String removeFolderPrefix(File fin, String parentFile) {
		return removeFolderPrefix(fin,new File(parentFile));
	}
	
	public static boolean isDirScionOf(File fin,File parentFile) {
		File pf = fin.getParentFile();
		if (pf!=null) {
			try {return pf.getCanonicalPath().toLowerCase().equals(parentFile.getCanonicalPath().toLowerCase());}catch(Exception e) {}
			return pf.getAbsolutePath().toLowerCase().equals(parentFile.getAbsolutePath().toLowerCase());
		}
		return false;
	}

	public static boolean isDirScionOf(File other, String parentFile) {
		return isDirScionOf(other,new File(parentFile));
	}


	public mFile getParentFile() {
        String p = this.getParent();
        if (p == null) return null;
        return new mFile(p,true);
	}
	
	public mFile getRealPath() {
		return webAsset==null?this:webAsset.realPath;
	}
}

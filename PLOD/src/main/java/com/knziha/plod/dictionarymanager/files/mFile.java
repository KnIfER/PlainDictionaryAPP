package com.knziha.plod.dictionarymanager.files;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class mFile extends File{
	public int shrinked;
	public String CSTR;//子系文件标识符
	public boolean bInIntrestedDir=false;
	protected boolean isDirectory=false;
	public mFile(File parent, String child) {
		super(parent, child);
	}

	public mFile(String pathname) {
		super(pathname);
	}

	public mFile(File to) {
		super(getEUPath(to));
	}
	
	public mFile(String pathname,boolean isDir) {
		super(pathname);
		isDirectory=isDir;
	}
	
	public mFile(String parent, String child) {
		super(parent, child);
	}

	
	public mFile(URI uri) {
		super(uri);
	}
	
	public String CanonicalParentPath;
	boolean cpr1,cpr2;
	public mFile init() {
		//Log.e("init__fatal",getAbsolutePath());
		isDirectory=isDirectory();
		if(getAbsolutePath().length()>1) {
			CanonicalParentPath=try_get_cannical().toLowerCase();
			boolean isS,isDS;
			if(CanonicalParentPath.startsWith(parentPath.toLowerCase())) {
				isS=true;
				isDS=CanonicalParentPath.length()==parentPath.length();
				cpr1=(!isDS||isDirectory);
				cpr2=isDS&&!isDirectory;
			}
		}
		return this;
	}

	private String try_get_cannical() {
		try{return getParentFile().getCanonicalPath();}catch(Exception e) {
			//Log.e("try_get_cannicalfatal",getAbsolutePath());
		}
		return getParentFile().getAbsolutePath();
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
	
	public static String parentPath="/storage/emulated/0/Download";
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
		
		if(false) {
			//制定的策略是：孙系文件比子系文件小，任意系的后代文件夹比子系文件小。
			if(isScionOf(THIS,parentPath)&&isDirScionOf(other,parentPath)&&!other.isDirectory()&&(!isDirScionOf(THIS,parentPath)||THIS.isDirectory())) {
				return -1;
			}
			if(isScionOf(other,parentPath)&&isDirScionOf(THIS,parentPath)&&!THIS.isDirectory()&&(!isDirScionOf(other,parentPath)||other.isDirectory())) {
				return 1;
			}
		}

		
		boolean b1=other.isDirectory(),b2=this.isDirectory();
		if(!b1 && !b2 ){
			int ret=THIS.getParentFile().compareTo(other.getParentFile());
			if(ret!=0)
				return ret;//先比较父文件夹
		}else if(b1 && ! b2) {
			if(!isScionOf(this, other))//若无父子关系，则比价父文件夹
				THIS=THIS.getParentFile();
		}else if(!b1 && b2) {
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
	public static String tryDeScion(File fin, File parentFile) {
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
	public static String tryDeScion(File fin, String parentFile) {
		return tryDeScion(fin,new File(parentFile));
	}
	
	
	
	public static boolean isDirScionOf(File fin,File parentFile) {
		try {return fin.getParentFile().getCanonicalPath().toLowerCase().equals(parentFile.getCanonicalPath().toLowerCase());}catch(Exception e) {}
		return fin.getParentFile().getAbsolutePath().toLowerCase().equals(parentFile.getAbsolutePath().toLowerCase());
	}

	public static boolean isDirScionOf(File other, String parentFile) {
		return isDirScionOf(other,new File(parentFile));
	}


	public mFile getParentFile() {
        String p = this.getParent();
        if (p == null) return null;
        return new mFile(p,true);
	}

}

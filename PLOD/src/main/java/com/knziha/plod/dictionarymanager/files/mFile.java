package com.knziha.plod.dictionarymanager.files;

import com.knziha.plod.dictionarymanager.BookManager;
import com.knziha.plod.dictionarymanager.BookManagerWebsites;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.widgets.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class mFile extends File{
	public static WeakReference<BookManager> bmRef = CMN.EmptyRef;
	public List<mFile> children = ViewUtils.EmptyArray; // the collpaed children folder holder
	public boolean bInIntrestedDir=false;
	protected boolean isDirectory=false;
	public BookManagerWebsites.WebAssetDesc webAsset;
	public mFile(File parent, String child) {
		super(parent, child);
		init();
	}
	
	public boolean getIsDirectory() {
		return isDirectory;
	}
	
	public mFile(String pathname) {
		super(pathname);
			init();
	}

	public mFile(String pathname, BookManagerWebsites.WebAssetDesc webAsset) {
		super(pathname);
		this.webAsset = webAsset;
		if (webAsset.isFolder) {
			isDirectory=true;
			children = new ArrayList<>();
		}
			init();
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
		init();
	}
	
	
	private void init() {
//		BookManager dm = bmRef.get();
//		String library_path = dm.opt.lastMdlibPath.getPath();
//		String get_path = getPath();
//		if(get_path.length()>1) {
//			String getParent_path = getParent();
//			weight_in_library = get_path.startsWith(library_path);
//			if (getParent_path!=null) {
//				boolean is_this_under_library;
//				if(getParent_path.startsWith(library_path)) {
//					is_this_under_library = getParent_path.length()==library_path.length();
//					weight_directroy_or_library_inner_file =(!is_this_under_library||isDirectory);
//					weight_library_dir_file = is_this_under_library&&!isDirectory;
//				}
//			}
//		}
	}
	
	boolean weight_directroy_or_library_inner_file, weight_library_dir_file, weight_in_library;
	public mFile init(PDICMainAppOptions opt) {
		//Log.e("init__fatal",getAbsolutePath());
		if(webAsset!=null) {
			return this;
		}
		isDirectory=webAsset!=null&&webAsset.isFolder || isDirectory();
		if (isDirectory && children == ViewUtils.EmptyArray) {
			children = new ArrayList<>();
		}
		String get_path = getPath();
		if(get_path.length()>1) {
			String getParent_path = getParent();
			String library_path = opt.lastMdlibPath.getPath();
			weight_in_library = get_path.startsWith(library_path);
			if (getParent_path!=null) {
				boolean is_this_under_library;
				if(getParent_path.startsWith(library_path)) {
					is_this_under_library = getParent_path.length()==library_path.length();
					weight_directroy_or_library_inner_file =(!is_this_under_library||isDirectory);
					weight_library_dir_file = is_this_under_library&&!isDirectory;
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
			if(this.webAsset!=null ^ otherF.webAsset!=null) {
				if (super.compareTo(otherF)==0) {
					return 0;
				}
				return this.webAsset==null?1:-1;
			}
			
			if(weight_in_library ^ otherF.weight_in_library) {
				return weight_in_library?-1:1;
			}
			
			if(weight_directroy_or_library_inner_file && otherF.weight_library_dir_file) {
				return -1;
			}
			if(weight_library_dir_file && otherF.weight_directroy_or_library_inner_file) {
				return 1;
			}
		//}
		
		boolean thatIsDir=other.isDirectory(), thisIsDir=this.isDirectory();
		int compare0 = -1;
		if(!thatIsDir && !thisIsDir /*both is file*/){
			int ret=THIS.getParentFile().compareTo(other.getParentFile());
			if(ret!=0) { // not the same parent
				return ret;//先比较父文件夹
			} else {
				// same parent file
				compare0 = 0;
				BookManager bm = bmRef.get();
				if (bm!=null) {
					String n0 = getName();
					String n1 = other.getName();
					for (int i=0, len=Math.min(n0.length(), n1.length()); i < len && compare0==0; i++) {
						char ch0 = n0.charAt(i);
						char ch1 = n1.charAt(i);
						if (ch0>=0x4e00&&ch0<=0x9fa5) {
							ch0 = (char)bm.getHzh()[ch0-0x4e00];
						}
						else ch0 = Character.toLowerCase(ch0);
						if (ch1>=0x4e00&&ch1<=0x9fa5) {
							ch1 = (char)bm.getHzh()[ch1-0x4e00];
						}
						else ch1 = Character.toLowerCase(ch1);
						compare0 = ch0-ch1;
					}
					if (compare0==0) {
						compare0 = n0.length() - n1.length();
					}
					return compare0;
				}
			}
		} else if(!thisIsDir /*this is file*/ && thatIsDir) {
			if(!isChildrenOf(this, other))//若无父子关系，则比价父文件夹
				THIS=THIS.getParentFile();
		} else if(!thatIsDir /*that is file*/ && thisIsDir) {
			if(!isChildrenOf(other, this))
				other=other.getParentFile();
		}
		try {
			return THIS.getCanonicalPath().compareToIgnoreCase(other.getCanonicalPath());
		} catch(Exception e) {}
		return THIS.getAbsolutePath().compareToIgnoreCase(other.getAbsolutePath());
	}
	
	
	private static boolean isChildrenOf(File fin, File other) {
		try {return fin.getParentFile().getCanonicalPath().toLowerCase().startsWith(other.getCanonicalPath().toLowerCase());}catch(Exception e) {}
		return fin.getParentFile().getAbsolutePath().toLowerCase().startsWith(other.getAbsolutePath().toLowerCase());
	}

	public static boolean isChildrenOf(File fin, String parentFile) {
		return isChildrenOf(fin,new File(parentFile));
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
	
	public static boolean isDirectChildrenOf(File fin, File parentFile) {
		File pf = fin.getParentFile();
		if (pf!=null) {
			try {return pf.getCanonicalPath().toLowerCase().equals(parentFile.getCanonicalPath().toLowerCase());}catch(Exception e) {}
			return pf.getAbsolutePath().toLowerCase().equals(parentFile.getAbsolutePath().toLowerCase());
		}
		return false;
	}

	public static boolean isDirectChildrenOf(File other, String parentFile) {
		return isDirectChildrenOf(other,new File(parentFile));
	}


	public mFile getParentFile() {
        String p = this.getParent();
        if (p == null) return null;
		if (webAsset!=null) {
			return new mFile(p, new BookManagerWebsites.WebAssetDesc("","",""));
		}
		return new mFile(p, true);
	}
	
	public mFile getRealPath() {
		return webAsset==null||isDirectory?this:webAsset.realPath;
	}
}

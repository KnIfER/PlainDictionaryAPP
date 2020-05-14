package com.knziha.plod.dictionarymodels;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.utils.FU;
import com.knziha.plod.PlainDict.AgentApplication;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.PlaceHolder;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.widgets.WebViewmy;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 Swift representation for management of mdicts.
 data:2020.01.12
 author:KnIfER
*/
public class mdict_transient implements mdict_manageable{
	public PlaceHolder mPhI;
	protected File f;
	public String _Dictionary_fName_Internal;
	PDICMainAppOptions opt;
	int TIFStamp;
	long FFStamp;
	long firstFlag;
	List<mdictRes_prempter> mdd;
	boolean keepOrgHolder=true;
	
	public final PhotoBrowsingContext IBC = new PhotoBrowsingContext();
	public Integer bgColor=null;
	public int TIBGColor;
	public int TIFGColor;
	public int internalScaleLevel=-1;
	public int lvPos,lvClickPos,lvPosOff;
	private float webScale;
	public SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
	final mdict_nonexist MNINSTANCE;
	public boolean isDirty;
	
	public void Rebase(File f){
		CMN.Log("MT0Rebase!!!");
		if(keepOrgHolder){
			PlaceHolder phI = new PlaceHolder();
			phI.lineNumber = mPhI.lineNumber;
			phI.name = mPhI.name;
			phI.tmpIsFlag = mPhI.tmpIsFlag;
			mPhI=phI;
			keepOrgHolder=false;
		}
		String line = f.getPath();
		int start = line.lastIndexOf("/");
		if(start<0) start=0; else start++;
		int end = line.length();
		int tmpIdx = line.length()-4;
		if(tmpIdx>0
				&& line.charAt(tmpIdx)=='.' && line.regionMatches(true, tmpIdx+1, "mdx" ,0, 3)){
			end -= 4;
		}
		mPhI.pathname = line;
		mPhI.name = line.substring(start, end);
	}

	//构造
	public mdict_transient(Activity a, String fn, PDICMainAppOptions opt_, mdict_nonexist mninstance) {
		this(a, fn, opt_, 0, mninstance);
	}

	public mdict_transient(Activity a, String fn, PDICMainAppOptions opt_, int isF, mdict_nonexist mninstance) {
		this(a, new PlaceHolder(fn), opt_, mninstance);
		mPhI.tmpIsFlag=TIFStamp=isF;
	}

	public mdict_transient(Activity a, PlaceHolder phI, PDICMainAppOptions opt_, mdict_nonexist mninstance) {
		opt=opt_;
		mPhI = phI;
		MNINSTANCE = mninstance;
		String fn = mPhI.getPath(opt);
		f = new File(fn);
		_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
		_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");

		justifyInternal(a, "."+mPhI.name);
		
		try {
			mninstance.opt=opt;
			MNINSTANCE._Dictionary_fName_Internal=_Dictionary_fName_Internal;
			MNINSTANCE._Dictionary_fName=mPhI.name;
			MNINSTANCE.IBC=IBC;
			MNINSTANCE.updateFile(f);
			MNINSTANCE.avoyager=avoyager;
			MNINSTANCE.readInConfigs(((AgentApplication)a.getApplication()).UIProjects);
			bgColor=MNINSTANCE.bgColor;
			TIBGColor=MNINSTANCE.TIBGColor;
			TIFGColor=MNINSTANCE.TIFGColor;
			internalScaleLevel=MNINSTANCE.internalScaleLevel;
			lvPos=MNINSTANCE.lvPos;
			lvClickPos=MNINSTANCE.lvClickPos;
			lvPosOff=MNINSTANCE.lvPosOff;
			webScale=MNINSTANCE.webScale;
			firstFlag=MNINSTANCE.firstFlag;
		} catch (IOException e) {
			if(GlobalOptions.debug) CMN.Log(e);
		}
		FFStamp=firstFlag;
		TIFStamp=mPhI.tmpIsFlag;
	}

	protected void justifyInternal(Context a, String dictionary_fName) {
		String path = opt.pathToDatabases().append(_Dictionary_fName_Internal).toString();
		File from = new File(path);
		File to = new File( opt.pathToDatabases().append(_Dictionary_fName_Internal=dictionary_fName).toString());
		//CMN.Log("移动??", from, to, from.exists());
		if(from.exists()){
			FU.move3(a, from, to);
		}
	}

	@Override
	public boolean renameFileTo(Context c, File to) {
		if(FU.rename5(c, f, to)>=0) {
			f = to;
			String filename = to.getName();
			Rebase(to);
			int tmpIdx = filename.length()-4;
			if(tmpIdx>0){
				if(filename.charAt(tmpIdx)=='.' && filename.regionMatches(true, tmpIdx+1, "md" ,0, 2)){
					boolean isResourceFile = Character.toLowerCase(filename.charAt(tmpIdx + 3)) == 'd';
					if(!isResourceFile){
						mPhI.name = filename.substring(0, tmpIdx);
					}
				}
			}
			String _Dictionary_fName_InternalOld = _Dictionary_fName_Internal;
			_Dictionary_fName_Internal = "."+mPhI.name;
			new File(opt.pathToDatabases().append(_Dictionary_fName_InternalOld).toString()).renameTo(new File(opt.pathToDatabases().append(_Dictionary_fName_Internal).toString()));
			return true;
		}
		return false;
	}

	@Override
	public boolean moveFileTo(Context c, File to) {
		File fP = to.getParentFile();
		fP.mkdirs();
		int ret = FU.move3(c, f, to);
		if(ret>=0) {
			f=to;
			mPhI.pathname = to.getPath();
			return true;
		}
		return false;
	}

	@Override
	public void unload() {

	}

	@Override
	public Drawable getCover() {
		return null;
	}

	@Override
	public String getName() {
		String ret = mPhI.pathname.startsWith(CMN.AssetTag)?CMN.AssetMap.get(mPhI.pathname):null;
		if(ret==null)
			ret=mPhI.name;
		return ret;
	}

	@Override
	public int getTmpIsFlag() {
		return mPhI.tmpIsFlag;
	}
	
	public boolean isMdictFile() {
		return !mPhI.pathname.endsWith(mPhI.name);
	}

	@Override
	public boolean isMddResource() {
		String filename = mPhI.pathname;
		int tmpIdx = filename.length()-4;
		if(tmpIdx>0){
			if(filename.charAt(tmpIdx)=='.' && filename.regionMatches(true, tmpIdx+1, "md" ,0, 2)){
				if(Character.toLowerCase(filename.charAt(tmpIdx+3))=='d'){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void setTmpIsFlag(int val) {
		mPhI.tmpIsFlag=val;
	}

	@Override
	public File f() {
		return f;
	}

	@Override
	public void checkFlag(Activity context) {
		if(isDirty||firstFlag!=FFStamp){
			String path = getPath();
			if(PDICMainAppOptions.ChangedMap ==null) PDICMainAppOptions.ChangedMap =new HashSet<>();
			PDICMainAppOptions.ChangedMap.add(path);
			
			MNINSTANCE._Dictionary_fName_Internal=_Dictionary_fName_Internal;
			MNINSTANCE._Dictionary_fName=mPhI.name;
			MNINSTANCE.IBC=IBC;
			MNINSTANCE.updateFile(f);
			MNINSTANCE.avoyager=avoyager;
			
			MNINSTANCE.bgColor=bgColor;
			MNINSTANCE.TIBGColor=TIBGColor;
			MNINSTANCE.internalScaleLevel=internalScaleLevel;
			MNINSTANCE.lvPos=lvPos;
			MNINSTANCE.lvClickPos=lvClickPos;
			MNINSTANCE.lvPosOff=lvPosOff;
			MNINSTANCE.webScale=webScale;
			MNINSTANCE.firstFlag=firstFlag;
			MNINSTANCE.dumpViewStates(((AgentApplication)context.getApplication()).UIProjects);
			FFStamp=firstFlag;
			isDirty=false;
		}
	}

	@Override
	public long getFirstFlag() {
		return firstFlag;
	}
	
	@Override
	public void setFirstFlag(long val){
		firstFlag = val;
	}
	
	@Override
	public void validifyValueForFlag(WebViewmy view, int val, int mask, int flagPosition, int pid) {
		firstFlag &= ~(mask << flagPosition);
		firstFlag |= (val << flagPosition);
	}

	@Override
	public PDICMainAppOptions getOpt() {
		return opt;
	}

	@Override
	public boolean exists() {
		return CMN.AssetMap.containsKey(mPhI.pathname)||f.exists();
	}

	@Override
	public boolean equalsToPlaceHolder(PlaceHolder other) {
		return TIFStamp==mPhI.tmpIsFlag && (mPhI==other ||
				mPhI.tmpIsFlag==other.tmpIsFlag &&
						mPhI.pathname.equals(other.pathname));
	}

	public static String goodNull(String fn) {
		SU.UniversalObject=fn;
		return null;
	}

	@Override
	public String getPath() {
		return f.getPath();
	}
}
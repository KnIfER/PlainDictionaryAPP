package com.knziha.plod.dictionarymodels;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.knziha.filepicker.utils.FU;
import com.knziha.plod.PlainDict.AgentApplication;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.PlaceHolder;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;

/**
 Swift representation for management of mdicts.
 data:2020.01.12
 author:KnIfER
*/
public class mdict_transient implements mdict_manageable {
	public PlaceHolder mPhI;
	protected File f;
	public String _Dictionary_fName_Internal;
	PDICMainAppOptions opt;
	int TIFStamp;
	long FFStamp;
	long firstFlag;
	List<mdictRes_prempter> mdd;
	boolean keepOrgHolder=true;

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
	public mdict_transient(Activity a, String fn, PDICMainAppOptions opt_) {
		this(a, fn, opt_, 0);
	}

	public mdict_transient(Activity a,String fn, PDICMainAppOptions opt_, int isF) {
		this(a, new PlaceHolder(fn), opt_);
		mPhI.tmpIsFlag=TIFStamp=isF;
	}

	public mdict_transient(Activity a,PlaceHolder phI, PDICMainAppOptions opt_) {
		opt=opt_;
		mPhI = phI;
		String fn = mPhI.getPath(opt);
		f = new File(fn);
		_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
		_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");

		justifyInternal(a, "."+mPhI.name);
		
		try {
			readInConfigs(((AgentApplication)a.getApplication()).UIProjects);
		} catch (IOException ignored) { }
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
	
	protected void readInConfigs(HashMap<String,byte[]> UIProjects) throws IOException {
		DataInputStream data_in1 = null;
		try {
			File SpecificationFile = new File(opt.pathToDatabases().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
			if(SpecificationFile.exists()) {
				//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
				if(CMN.bForbidOneSpecFile  && SpecificationFile.exists()){
					data_in1 = new DataInputStream(new FileInputStream(SpecificationFile));
				} else {
					SpecificationFile.delete();
					File parentFile = SpecificationFile.getParentFile();
					if(ArrayUtils.isEmpty(parentFile.list()))
						parentFile.delete();
					byte[] data = UIProjects.get(f.getName());
					if(data!=null){
						int extra = MainActivityUIBase.ConfigExtra;
						data_in1 = new DataInputStream(new ByteArrayInputStream(data, extra, data.length-extra));
					}
				}
				if(data_in1!=null) {
					int size = data_in1.readShort();
					if (CMN.bForbidOneSpecFile && size != 12) {
						data_in1.close();
						SpecificationFile.delete();
						return;
					}
					byte _firstFlag = data_in1.readByte();
					if (_firstFlag != 0) {
						firstFlag |= _firstFlag;
					}
					//readinoptions
					//a.showT("getUseInternalBG():"+getUseInternalBG()+" getUseInternalFS():"+getUseInternalFS()+" KeycaseStrategy:"+KeycaseStrategy);
					if (data_in1.available() > 4) {
						if (data_in1.skipBytes(32) == 32 && data_in1.available() > 0) {
							CMN.Log("摄政傀儡");
							firstFlag |= data_in1.readLong();
						}
					}
					//CMN.Log(_Dictionary_fName+"页面位置",expectedPosX,expectedPos,webScale);
					//CMN.Log(_Dictionary_fName+"单典配置加载耗时",System.currentTimeMillis()-time);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			FFStamp = firstFlag;
			if(data_in1!=null) data_in1.close();
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

	@Override
	public boolean getIsDedicatedFilter() {
		return (firstFlag & 0x40) == 0x40;
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
	public void checkFlag() {
		if(FFStamp!=firstFlag){
			String path = getPath();
			if(!path.endsWith(".web"))
				WriteConfigFF();
//			if(opt.ChangedMap==null) opt.ChangedMap=new HashMap<>();
//			opt.ChangedMap.put(path, FFStamp=firstFlag);
		}
	}

	protected void WriteConfigFF() {
		//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM lv[int int int] page[int int float]
		try {
			File SpecificationFile = new File(opt.pathToDatabases().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
			CMN.Log("保存标志容器中…", mPhI.name, SpecificationFile.getAbsolutePath());
			if(!SpecificationFile.getParentFile().exists())
				SpecificationFile.getParentFile().mkdirs();
			RandomAccessFile outputter = new RandomAccessFile(SpecificationFile, "rw");
			outputter.seek(35);
			if(outputter.getFilePointer()==35){
				outputter.writeLong(FFStamp=firstFlag);
			}
			outputter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getFirstFlag() {
		return firstFlag;
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
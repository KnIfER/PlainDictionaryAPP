package com.knziha.plod.dictionarymodels;

import android.graphics.drawable.Drawable;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.PlaceHolder;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.widgets.WebViewmy;

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
	public final PlaceHolder mPhI;
	protected File f;
	public String _Dictionary_fName_Internal;
	PDICMainAppOptions opt;
	int TIFStamp;
	long FFStamp;
	long firstFlag;
	List<mdictRes_prempter> mdd;

	//构造
	public mdict_transient(String fn, PDICMainAppOptions opt_) {
		this(fn, opt_, 0);
	}

	public mdict_transient(String fn, PDICMainAppOptions opt_, int isF) {
		this(new PlaceHolder(fn), opt_);
		mPhI.tmpIsFlag=TIFStamp=isF;
	}

	public mdict_transient(PlaceHolder phI, PDICMainAppOptions opt_) {
		opt=opt_;
		mPhI = phI;
		String fn = mPhI.getPath(opt);
		f = new File(fn);
		_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
		_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");
		try {
			readInConfigs();
		} catch (IOException ignored) { }
		TIFStamp=mPhI.tmpIsFlag;
	}

	protected void readInConfigs() throws IOException {
		String path = getPath();
		if(path.endsWith(".web")){
			Long ff = PDICMainAppOptions.ChangedMap.get(path);
			FFStamp = firstFlag = ff==null?0:ff;
		}
		else {
			DataInputStream data_in1 = null;
			try {
				File SpecificationFile = new File(opt.pathToDatabases().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
				if(SpecificationFile.exists()) {
					long time=System.currentTimeMillis();
					//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
					data_in1 = new DataInputStream(new FileInputStream(SpecificationFile));
					int size = data_in1.readShort();
					if(size!=12) {
						data_in1.close();
						SpecificationFile.delete();
						return;
					}
					byte _firstFlag = data_in1.readByte();
					if(_firstFlag!=0){
						firstFlag |= _firstFlag;
					}
					//readinoptions
					//a.showT("getUseInternalBG():"+getUseInternalBG()+" getUseInternalFS():"+getUseInternalFS()+" KeycaseStrategy:"+KeycaseStrategy);
					if(data_in1.available()>4) {
//					bgColor = data_in1.readInt();
//					internalScaleLevel = data_in1.readInt();
//
//
//					lvPos = data_in1.readInt();
//					lvClickPos = data_in1.readInt();
//					lvPosOff = data_in1.readInt();
//
//					//CMN.Log(_Dictionary_fName+"列表位置",lvPos,lvClickPos,lvPosOff);
//					if(data_in1.available()>0) {
//						initArgs = new int[]{data_in1.readInt(), data_in1.readInt()}; //28
//						webScale = data_in1.readFloat();//4
//					}
						if(data_in1.skipBytes(32)==32 && data_in1.available()>0) {
							//CMN.Log("摄政傀儡");
							firstFlag |= data_in1.readLong();
						}
					}

					//CMN.Log(_Dictionary_fName+"页面位置",expectedPosX,expectedPos,webScale);

					data_in1.close();
					//CMN.Log(_Dictionary_fName+"单典配置加载耗时",System.currentTimeMillis()-time);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				FFStamp = firstFlag;
				if(data_in1!=null) data_in1.close();
			}
		}
	}

	@Override
	public boolean moveFileTo(File newF) {
		File fP = newF.getParentFile();
		fP.mkdirs();
		boolean ret = false;
		//boolean pass = !f.exists();
		if(fP.exists() && fP.isDirectory()) {
			ret=true;
			String filename = newF.getName();
			mPhI.pathname = newF.getAbsolutePath();
			mPhI.name = filename;
		}
		String _Dictionary_fName_InternalOld = _Dictionary_fName_Internal;
		if(ret) {
			f=newF;
			String fn = newF.getAbsolutePath();
			_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
			_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");
		}
		new File(opt.pathToDatabases().append(_Dictionary_fName_InternalOld).toString()).renameTo(new File(opt.pathToDatabases().append(_Dictionary_fName_Internal).toString()));

		return ret;
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
			if(opt.ChangedMap==null) opt.ChangedMap=new HashMap<>();
			opt.ChangedMap.put(path, FFStamp=firstFlag);
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
	public boolean renameFileTo(File to) {
		return moveFileTo(to);
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
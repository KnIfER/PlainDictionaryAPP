package com.knziha.plod.dictionarymodels;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.knziha.filepicker.utils.FU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.Toastable_Activity;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 For management of books.
 data:2020.01.12
 author:KnIfER
*/
public class MagentTransient extends BookPresenter {
	public PlaceHolder mPhI;
	protected File f;
	public String _Dictionary_fName_Internal;
	int TIFStamp;
	boolean keepOrgHolder=true;
	
	private boolean changeMap=true;
	Toastable_Activity context;
	
	public void Rebase(File f){
		CMN.Log("MT0Rebase!!!");
		if(keepOrgHolder){
			PlaceHolder phI = new PlaceHolder();
			phI.lineNumber = mPhI.lineNumber;
			phI.tmpIsFlag = mPhI.tmpIsFlag;
			mPhI=phI;
			keepOrgHolder=false;
		}
		mPhI.Rebase(f);
	}
	

	public MagentTransient(Toastable_Activity a, Object PlaceHolderString, PDICMainAppOptions opt_, Integer isF, boolean bIsPreempter) throws IOException {
		super(new File(PlaceHolderString instanceof String?(String)PlaceHolderString:PlaceHolderString instanceof PlaceHolder?((PlaceHolder) PlaceHolderString).pathname:"/N/A"), null, 3);
		PlaceHolder phI=PlaceHolderString instanceof String?new PlaceHolder((String) PlaceHolderString):PlaceHolderString instanceof PlaceHolder?(PlaceHolder) PlaceHolderString:null;
		Objects.requireNonNull(phI);
		if(bookImpl==null) {
			File f = new File(phI.pathname);
			bookImpl = new DictionaryAdapter(f, null);
			Long bid = bookImplsNameMap.get(f.getName());
			if(bid==null) bid=-1L; // todo check
			bookImpl.setBooKID(bid);
		}
		bIsManagerAgent = 1;
		opt=opt_;
		mPhI = phI;
		f = mPhI.getPath(opt);
		_Dictionary_fName_Internal = "."+mPhI.getName();
		
		setDictionaryName(mPhI.getName().toString());
		updateFile(f);
		
		if (isF!=null) {
			mPhI.tmpIsFlag=TIFStamp=isF;
		}
		TIFStamp=mPhI.tmpIsFlag;
		context = a;
	}
	
	@Override
	public long getFirstFlag() {
		if (!bReadConfig) {
			try {
				readConfigs(context, context.prepareHistoryCon());
				FFStamp=firstFlag;
			} catch (IOException e) { CMN.Log(e); }
		}
		return super.getFirstFlag();
	}
	
	@Override
	public boolean renameFileTo(Context c, File to) {
		if(FU.rename5(c, f, to)>=0) {
			f = to;
			Rebase(to);
			String _Dictionary_fName_InternalOld = _Dictionary_fName_Internal;
			_Dictionary_fName_Internal = "."+mPhI.getName();
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
	public String getDictionaryName() {
//		String ret = mPhI.pathname.startsWith(CMN.AssetTag)?CMN.AssetMap.get(mPhI.pathname):null;
//		if(ret==null)
//			ret=mPhI.getName().toString();
		return mPhI.getName().toString();
	}

	@Override
	public int getTmpIsFlag() {
		return mPhI.tmpIsFlag;
	}
	
	@Override
	public void setTmpIsFlag(int val) {
		mPhI.tmpIsFlag = val;
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
	
//	public void dumpViewStates(Toastable_Activity context, int tmpIsFlag) {
//		bookDelegate.isDirty = true;
//		bookDelegate.firstFlag = tmpIsFlag;
//		changeMap = false;
//		checkFlag(context);
//	}
	
	@Override
	public boolean checkFlag(Toastable_Activity context) {
		boolean ret = isDirty||firstFlag!=FFStamp;
		if(ret){
			if(changeMap){
				String path = getPath();
				if(PDICMainAppOptions.ChangedMap ==null) PDICMainAppOptions.ChangedMap =new HashSet<>();
				PDICMainAppOptions.ChangedMap.add(path);
			}
			
			setDictionaryName(mPhI.getName().toString());
			updateFile(f);
			
			saveStates(context, context.prepareHistoryCon());
			FFStamp=firstFlag;
			isDirty=false;
		}
		return ret;
	}

	@Override
	public boolean exists() {
		// CMN.AssetMap.containsKey(mPhI.pathname)
		return mPhI.pathname.startsWith("/ASSET")||f.exists();
	}

	@Override
	public boolean equalsToPlaceHolder(PlaceHolder other) {
		return TIFStamp==mPhI.tmpIsFlag && (mPhI==other ||
				mPhI.tmpIsFlag==other.tmpIsFlag &&
						mPhI.pathname.equals(other.pathname));
	}

	@Override
	public String getPath() {
		return f.getPath();
	}
	
	public PlaceHolder getPlaceHolder() {
		return mPhI;
	}
}
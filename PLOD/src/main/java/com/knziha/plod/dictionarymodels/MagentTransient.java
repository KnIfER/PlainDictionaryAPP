package com.knziha.plod.dictionarymodels;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.utils.FU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.text.StringEscapeUtils;

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
	PDICMainAppOptions opt;
	int TIFStamp;
	List<mngr_mdictRes_prempter> mdd;
	boolean keepOrgHolder=true;
	
	private boolean changeMap=true;
	
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

	public MagentTransient(Toastable_Activity a, Object PlaceHolderString, PDICMainAppOptions opt_, DictionaryAdapter bookInstance, Integer isF, boolean bIsPreempter) throws IOException {
		super(new File("/N/A"), null, 1, null);
		
		PlaceHolder phI=null;
		if (PlaceHolderString instanceof String) {
			phI = new PlaceHolder((String) PlaceHolderString);
		}
		else if (PlaceHolderString instanceof PlaceHolder) {
			phI = (PlaceHolder) PlaceHolderString;
		}
		Objects.requireNonNull(phI);
		bookImpl = bookInstance;
		bIsManagerAgent = 1;
		opt=opt_;
		mPhI = phI;
		f = mPhI.getPath(opt);
		_Dictionary_fName_Internal = "."+mPhI.getName();
		
		setDictionaryName(mPhI.getName().toString());
		updateFile(f);
		readConfigs(a, a.prepareHistoryCon());
		
		if (isF!=null) {
			mPhI.tmpIsFlag=TIFStamp=isF;
		}
		
		FFStamp=firstFlag;
		TIFStamp=mPhI.tmpIsFlag;
		
		if (bIsPreempter) {
			String fnTMP = f.getName();
			
			File f2 = new File(f.getParentFile().getAbsolutePath()+"/"+fnTMP.substring(0,fnTMP.lastIndexOf("."))+".mdd");
			if(f2.exists()){
				mdd = Collections.singletonList(new mngr_mdictRes_prempter(f2));
			}
			bIsManagerAgent = 2;
		}
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
		String ret = mPhI.pathname.startsWith(CMN.AssetTag)?CMN.AssetMap.get(mPhI.pathname):null;
		if(ret==null)
			ret=mPhI.getName().toString();
		return ret;
	}

	@Override
	public int getTmpIsFlag() {
		return mPhI.tmpIsFlag;
	}
	
	@Override
	public void setTmpIsFlag(int val) {
		mPhI.tmpIsFlag = val;
	}
	
	public boolean isMdictFile() {
		String line = mPhI.pathname;
		int tmpIdx = line.length()-4;
		return tmpIdx>0
				&& line.charAt(tmpIdx)=='.' && line.regionMatches(true, tmpIdx+1, "mdx" ,0, 3);
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
	public void checkFlag(Toastable_Activity context) {
		if(isDirty||firstFlag!=FFStamp){
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
}
package com.knziha.plod.dictionarymanager;

import android.os.Environment;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionarymanager.files.ArrayListBookTree;
import com.knziha.plod.dictionarymanager.files.mAssetFile;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.widgets.ViewUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class BookManagerFolderlike extends BookManagerFolderAbs {
//	@Override
//	public void setUserVisibleHint(boolean isVisibleToUser) {
//		if (isVisibleToUser) {
//			pullData();
//		}
//		super.setUserVisibleHint(isVisibleToUser);
//	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(!dataPrepared) pullData();
	}

	private void pullData() {
		dataPrepared = true;
		File rec = a.fRecord;
		try {
			PDICMainAppOptions opt = getBookManager().opt;
			topParent.add(new mFile(opt.lastMdlibPath));
			topParent.add(new mFile(opt.lastMdlibPath.getPath().replace(GlobalOptions.extPath, "/sdcard")));
		} catch (Exception e) {
			CMN.debug(e);
		}
		if(CMN.sData!=null) {
			data = (ArrayListBookTree<mFile>) CMN.sData;
			dataTree = (ArrayList<mFile>) CMN.sDataTree;
		} else {
			CMN.debug("拉取数据！", rec);
			try {
				BufferedReader in = new BufferedReader(new FileReader(rec));
				String line;
				//int idx=0;
				String lastMdlibPath = a.opt.lastMdlibPath.getPath();
				while((line=in.readLine())!=null){
					if (line.startsWith("/sdcard/")) {
						line = GlobalOptions.extPath + line.substring(7);
					}
					boolean isForeign=false;
					mFile fI;
					if(!line.startsWith("/")) {//是相对路径
						if(line.startsWith("[:")){
							int idx = line.indexOf("]",2);
							if(idx>=2){
								line = line.substring(idx+1);
							}
						}
						if(line.contains("/")) {
							//wtf???
							if(line.startsWith("/")) {
								fI = new mFile(line);
							} else {
								fI = new mFile(a.opt.lastMdlibPath, line);
								fI.bInIntrestedDir=true;
							}
							isForeign=true;
						} else {
							fI = new mFile(a.opt.lastMdlibPath, line);
							fI.bInIntrestedDir=true;
						}
					} else {
						if(!line.startsWith(lastMdlibPath))
							isForeign=true;
						fI = new mFile(line);
					}
					
					if(data.insert(fI.init(a.opt))==-1)
						isDirty=true;
					else if(isForeign)
						data.insertOverFlow(fI.getParentFile().init(a.opt));
					//idx++;
				}
				in.close();
			}
			catch (Exception e) {
				CMN.debug(e);
			}
			dataTree = new ArrayList<>(data.getList());
			CMN.sData = data;
			CMN.sDataTree = dataTree;
		}
		adapter = new MyAdapter(dataTree);
		adapterAll = new MyAdapter(data.getList());
		super.setListAdapter(adapter);
		if(a!=null) ViewUtils.restoreListPos(listView, BookManager.listPos[a.fragments.indexOf(this)]);
	}
	
	@Override
	public void rebuildDataTree() {
		super.rebuildDataTree();
		CMN.sData = data;
		CMN.sDataTree = dataTree;
	}
	
	//构造
	public BookManagerFolderlike(){
		super();
		mName = "全部词典";
	}
}

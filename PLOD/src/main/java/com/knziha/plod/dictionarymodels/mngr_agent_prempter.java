package com.knziha.plod.dictionarymodels;

import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.Toastable_Activity;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/*
 UI side of mdict
 data:2018.07.30
 author:KnIfER
*/
public class mngr_agent_prempter extends mngr_agent_transient {
	public boolean isAsset;

	//构造
	public mngr_agent_prempter(Toastable_Activity a, String fn, PDICMainAppOptions opt_, mngr_presenter_nonexist mninstance) throws IOException {
		this(a, fn, opt_, 0, mninstance);
	}

	public mngr_agent_prempter(Toastable_Activity a, String fn, PDICMainAppOptions opt_, int isF, mngr_presenter_nonexist mninstance) throws IOException {
		super(a, fn, opt_, isF, mninstance);
		String fnTMP = f.getName();

		File f2 = new File(f.getParentFile().getAbsolutePath()+"/"+fnTMP.substring(0,fnTMP.lastIndexOf("."))+".mdd");
		if(f2.exists()){
			mdd = Collections.singletonList(new mngr_mdictRes_prempter(f2));
		}
	}
}

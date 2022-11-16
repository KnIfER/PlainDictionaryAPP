package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleQueryMorphs extends DictionaryAdapter{
	public MainActivityUIBase.LoadManager loadMan;
	
	public final int lookUp(UniversalDictionaryInterface d, String keyword) {
		return d.lookUp(keyword, true);
	}
	
	public SimpleQueryMorphs() {
	}
	
	public ConcurrentHashMap<String, String> rejected = new ConcurrentHashMap<>();
	
	@Override
	public int guessRootWord(UniversalDictionaryInterface d, String keyword) {
		//CMN.debug("guessRootWord::", keyword);
		if(rejected.contains(keyword)) {
			return -1;
		}
		int size = loadMan.lazyMan.filterCount;
		boolean nothing = true;
		if (size>0) {
			//CMN.Log("ReRouteKey ??" , key);
			for (int i = 0; i < size; i++) {
				BookPresenter mdTmp = loadMan.getFilterAt(i);
				if(mdTmp!=loadMan.EmptyBook)
					try {
						Object found = mdTmp.bookImpl.ReRoute(keyword);
						//CMN.Log(key, " >> " , rerouteTarget, mdTmp.getName(), mdTmp.getIsDedicatedFilter());
						if (found instanceof String) {
							int idx = d.lookUp((String) found, true);
							if(idx >= 0)
								return idx;
							if(nothing) nothing = false;
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
			}
		}
		if(nothing) {
			rejected.put(keyword, keyword);
		}
		return -1;
	}
	
}

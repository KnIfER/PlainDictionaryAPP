package com.knziha.plod.dictionary;

import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public interface UniversalDictionaryInterface {
	String getEntryAt(int position, Flag mflag);
	String getEntryAt(int position);
	long getNumberEntries();
	
	String getRecordAt(int position, GetRecordAtInterceptor getRecordAtInterceptor, boolean allowJump) throws IOException;
	String getRecordsAt(GetRecordAtInterceptor getRecordAtInterceptor, int... positions) throws IOException;
	byte[] getRecordData(int position) throws IOException;
	void setCaseStrategy(int val);
	
	File getFile();
	String getDictionaryName();
	boolean hasVirtualIndex();
	StringBuilder AcquireStringBuffer(int capacity);
	
	boolean hasMdd();
	
	String getRichDescription();
	String getDictInfo();
	
	boolean getIsResourceFile();
	
	Object[] getSoundResourceByName(String canonicalName) throws IOException;
	
	String getCharsetName();
	
	void Reload();
	
	int lookUp(String keyword,boolean isSrict);
	
	int lookUp(String keyword);
	
	void lookUpRange(String keyword, ArrayList<myCpr<String, Integer>> combining_search_list, RBTree_additive combining_search_tree, int SelfAtIdx, int theta);
	
	InputStream getResourceByKey(String key);
	
	Object ReRoute(String key) throws IOException;
	
	String getVirtualRecordAt(Object presenter, int vi) throws IOException;
	
	String getVirtualRecordsAt(Object presenter, int[] list2) throws IOException;
	
	String getVirtualTextValidateJs(Object presenter, WebViewmy mWebView, int position);
	
	String getVirtualTextEffectJs(int[] positions);
	
	long getBooKID();
	
	void setBooKID(long id);
	
	void flowerFindAllContents(String key, int selfAtIdx, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException;
	void flowerFindAllKeys(String key, int SelfAtIdx, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException;
	
	String getResourcePaths();
	
	byte[] getOptions();
	void setOptions(byte[] options);
	int getType();
}

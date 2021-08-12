package com.knziha.plod.dictionary;

import com.knziha.plod.dictionary.Utils.Flag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface UniversalDictionaryInterface {
	String getEntryAt(int position, Flag mflag);
	String getEntryAt(int position);
	long getNumberEntries();
	
	String getRecordAt(int position) throws IOException;
	String getRecordsAt(int... positions) throws IOException;
	byte[] getRecordData(int position) throws IOException;
	void setCaseStrategy(int val);
	
	File getFile();
	String getDictionaryName();
	boolean hasVirtualIndex();
	StringBuilder AcquireStringBuffer(int capacity);
	
	boolean hasMdd();
	
	String getRichDescription();
	
	boolean getIsResourceFile();
	
	Object[] getSoundResourceByName(String canonicalName) throws IOException;
	
	String getCharsetName();
	
	void Reload();
	
	int lookUp(String keyword,boolean isSrict);
	
	int lookUp(String keyword);
	
	InputStream getResourceByKey(String key);
	
	Object ReRoute(String key) throws IOException;
	
	String getVirtualRecordAt(int vi) throws IOException;
	
	String getVirtualRecordsAt(int[] list2) throws IOException;
	
	String getVirtualTextValidateJs();
	
	String getVirtualTextEffectJs(int[] positions);
	
	long getBooKID();
	
	void setBooKID(long id);
	
	void flowerFindAllContents(String key, int selfAtIdx, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException;
	void flowerFindAllKeys(String key, int SelfAtIdx, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException;
}

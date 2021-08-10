package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.plaindict.PDICMainAppOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class DictionaryAdapter implements UniversalDictionaryInterface {
	File f;
	int _num_entries;
	String _Dictionary_fName;
	PDICMainAppOptions opt;
	Charset _charset;
	
	@Override
	public String getEntryAt(int position, Flag mflag) {
		return null;
	}
	
	@Override
	public String getEntryAt(int position) {
		return null;
	}
	
	@Override
	public long getNumberEntries() {
		return _num_entries;
	}
	
	@Override
	public String getRecordAt(int position) throws IOException {
		return null;
	}
	
	@Override
	public String getRecordsAt(int... positions) throws IOException {
		return null;
	}
	
	@Override
	public byte[] getRecordData(int position) throws IOException {
		return null;
	}
	
	@Override
	public void setCaseStrategy(int val) {
	
	}
	
	@Override
	public File getFile() {
		return f;
	}
	
	@Override
	public String getDictionaryName() {
		return _Dictionary_fName;
	}
	
	@Override
	public boolean hasVirtualIndex() {
		return false;
	}
	
	@Override
	public StringBuilder AcquireStringBuffer(int capacity) {
		return new StringBuilder(capacity);
	}
	
	@Override
	public boolean hasMdd() {
		return false;
	}
	
	@Override
	public String getRichDescription() {
		return null;
	}
	
	@Override
	public boolean getIsResourceFile() {
		return false;
	}
	
	@Override
	public Object[] getSoundResourceByName(String canonicalName) throws IOException {
		return null;
	}
	
	@Override
	public String getCharsetName() {
		return _charset==null?null:_charset.name();
	}
	
	@Override
	public void Reload() {
	
	}
	
	@Override
	public int lookUp(String keyword, boolean isSrict) {
		return 0;
	}
	
	@Override
	public int lookUp(String keyword) {
		return 0;
	}
	
	@Override
	public InputStream getResourceByKey(String key) {
		return null;
	}
	
	@Override
	public Object ReRoute(String key) throws IOException {
		return null;
	}
	
	@Override
	public String getVirtualRecordAt(int vi) throws IOException {
		return null;
	}
	
	@Override
	public String getVirtualRecordsAt(int[] list2) throws IOException {
		return null;
	}
	
	public String getSimplestInjection() {
		return BookPresenter.SimplestInjection;
	}
	
	public String getLexicalEntryAt(int position) {
		return null;
	}
}

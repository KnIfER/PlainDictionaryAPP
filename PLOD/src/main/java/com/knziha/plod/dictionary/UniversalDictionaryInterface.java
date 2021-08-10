package com.knziha.plod.dictionary;

import com.knziha.plod.dictionary.Utils.Flag;

public interface UniversalDictionaryInterface {
	String getEntryAt(int position, Flag mflag);
	long getNumberEntries();
	String getRecordsAt(int... positions);
	byte[] getRecordData(int position);
}

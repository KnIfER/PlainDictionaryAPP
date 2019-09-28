package com.knziha.plod.dictionarymodels;

import java.util.ArrayList;

import com.knziha.plod.PlainDict.BasicAdapter;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.dictionary.Flag;

public class resultRecorderDiscrete {
	public String count;
	public String SearchText;

	//dict_Activity_ui_base a;
	
	public Flag mflag = new Flag();
	
	public resultRecorderDiscrete(MainActivityUIBase a_){
		//a=a_;
	};
	
	public CharSequence getResAt(int pos) {return "";}
	
	public void renderContentAt(int pos, MainActivityUIBase a_, BasicAdapter ADA) {}
	
	public int size() {return 0;}
	
	public void invalidate() {}
	
	public void invalidate(int adapter_idx) {}

	public void shutUp() {}
	
	public int dictIdx=0;

	public int expectedPos;

	public ArrayList<Integer> getRecordAt(int pos) {
		
		return new ArrayList<>();
	}

	public ArrayList<Integer> getDictsAt(int pos) {
		return new ArrayList<>();
	}

}

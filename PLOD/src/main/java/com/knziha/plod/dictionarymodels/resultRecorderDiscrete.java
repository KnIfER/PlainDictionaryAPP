package com.knziha.plod.dictionarymodels;

import java.util.ArrayList;

import com.knziha.plod.plaindict.BasicAdapter;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.dictionary.Utils.Flag;

public class resultRecorderDiscrete {
	public String count;
	public String SearchText;

	//dict_Activity_ui_base a;

	public Flag mflag = new Flag();
	public boolean allWebs;

	public resultRecorderDiscrete(MainActivityUIBase a_){
		//a=a_;
	};

	public CharSequence getResAt(MainActivityUIBase a, long pos) {return "";}

	public void renderContentAt(long pos, MainActivityUIBase a_, BasicAdapter ADA) {}

	public int size() {return 0;}

	public void invalidate() {}

	public void invalidate(int adapter_idx) {}

	public void shutUp() {}

	//public int dictIdx=0;
	public long bookId=0;

	public int expectedPos;

	public ArrayList<Long> getRecordAt(int pos) {
		return new ArrayList<>();
	}

	public long getOneDictAt(int pos) {
		return 0;
	}

	public ArrayList<Long> getBooksAt(ArrayList<Long> books, int pos) {
		return new ArrayList<>();
	}

	public boolean checkAllWebs(MainActivityUIBase mainActivityUIBase, ArrayList<BookPresenter> md) {
		return false;
	}

	public boolean shouldSaveHistory() {
		return true;
	}
}

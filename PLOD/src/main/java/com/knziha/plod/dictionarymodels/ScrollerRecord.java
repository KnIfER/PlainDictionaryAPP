package com.knziha.plod.dictionarymodels;

public class ScrollerRecord {
	public int x,y;
	public float scale=1;
	public ScrollerRecord(){
		scale=-1;//mdict.def_zoom;
	}
	public ScrollerRecord(int scrollX, int scrollY, float scale_){
		set(scrollX, scrollY, scale_);
	}
	public void set(int scrollX, int scrollY, float scale_) {
		x=scrollX;
		y=scrollY;
		scale=scale_;
	}
}
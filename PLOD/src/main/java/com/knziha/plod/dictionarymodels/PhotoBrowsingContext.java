package com.knziha.plod.dictionarymodels;

public class PhotoBrowsingContext {
	public long firstFlag;
	public float doubleClickZoomRatio = 2.25f;
	public float doubleClickXOffset = 0f;
	public float doubleClickZoomLevel1 = 2f;
	public float doubleClickZoomLevel2 = 0f;
	public float doubleClickPresetXOffset = 0f;
	
	public boolean getUseDoubleClick(){
		return (firstFlag & 0x400) != 0;
	}
	/** 0=左右分栏; 1=靠左; 2=靠右; 3=居中 */
	public int getDoubleClickAlignment(){
		return (int) ((firstFlag >> 12) & 3);
	}
	
	/** 0=none;1=Level1; 2=Level2 */
	public int getPresetZoomLevel(){
		return (int) ((firstFlag >> 14) & 3);
	}
	
	/** 0=居中; 1=靠左; 2=靠右 */
	public int getPresetZoomAlignment(){
		return (int) ((firstFlag >> 16) & 3);
	}
	
	public boolean getDoubleClick12(){
		return (firstFlag & 0x100000) != 0;
	}
}

package com.knziha.plod.dictionarymodels;

import org.adrianwalker.multilinestring.Multiline;

public class PhotoBrowsingContext {
	public boolean lockX;
	public long firstFlag;
	public float doubleClickZoomRatio = 2.25f;
	public float doubleClickXOffset = 0f;
	public float doubleClickZoomLevel1 = 0f;
	public float doubleClickZoomLevel2 = 0f;
	public float doubleClickPresetXOffset = 0f;
	
	public boolean getUseDoubleClick(){
		return (firstFlag & 0x400) != 0;
	}
	/** 0=左右分栏; 1=靠左; 2=靠右; 3=居中; 4=跟随鼠标位置 */
	@Multiline(flagPos=12, flagSize=3, max=3) public int getDoubleClickAlignment(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	/** 0=none;1=Level1; 2=Level2 */
	@Multiline(flagPos=15, flagSize=3, max=3) public int getPresetZoomLevel(){ firstFlag=firstFlag; throw new RuntimeException(); }
	//
	/** 0=居中; 1=靠左; 2=靠右 */
	@Multiline(flagPos=18, flagSize=2, max=2) public int getPresetZoomAlignment(){ firstFlag=firstFlag; throw new RuntimeException(); }
	//
	@Multiline(flagPos=20) public boolean getDoubleClick12(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
}

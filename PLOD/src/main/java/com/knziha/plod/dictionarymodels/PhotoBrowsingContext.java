package com.knziha.plod.dictionarymodels;

import org.knziha.metaline.Metaline;

public class PhotoBrowsingContext {
	public boolean lockX;
	public long firstFlag;
	public float tapZoomRatio = 2.25f;
	public float tapZoomXOffset = 0f;
	public float doubleClickZoomLevel1 = 0f;
	public float doubleClickZoomLevel2 = 0f;
	public float doubleClickPresetXOffset = 0f;
	public float lastX;
	public float lastY;
	
	/** page. 是否双击放大网页 */
	@Metaline(flagPos=10) public boolean tapZoom(){ firstFlag=firstFlag; throw new RuntimeException(); }
	/** page. 是否双击放大网页 */
	@Metaline(flagPos=10) public void tapZoom(boolean val){ firstFlag=firstFlag; throw new RuntimeException(); }
	/** page. 0=靠左; 1=左右分栏; 2=靠右; 3=居中; 4=跟随鼠标位置（拖拽放大） */
	@Metaline(flagPos=12, flagSize=3, max=4) public int tapAlignment(){ firstFlag=firstFlag; throw new RuntimeException(); }
	/** page. 0=靠左; 1=左右分栏; 2=靠右; 3=居中; 4=跟随鼠标位置（拖拽放大） */
	@Metaline(flagPos=12, flagSize=3, max=4) public void tapAlignment(int val){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	/** image. 0=none;1=Level1; 2=Level2 */
	@Metaline(flagPos=15, flagSize=2, max=2) public int getPresetZoomLevel(){ firstFlag=firstFlag; throw new RuntimeException(); }
	//
	/** image. 0=居中; 1=靠左; 2=靠右; 3=智能计算 */
	@Metaline(flagPos=17, flagSize=3, max=3) public int getPresetZoomAlignment(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=17, flagSize=3, max=3) public void setPresetZoomAlignment(int value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	//
	@Metaline(flagPos=20) public boolean getDoubleClick12(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
}

package com.knziha.plod.PlainUI;

public class AppIconData {
	int number;
	/** 0=hide; 1=normal; 2=only in horizontal mode */
	int tmpIsFlag;
	final static int tmpFlagLen=2;
	AppIconData(int _number, int _tmpIsFlag){
		number = _number;
		tmpIsFlag = _tmpIsFlag;
		//CMN.Log("最大值：", 1<<6-1);
	}
	@Override
	public String toString() {
		String ret = Integer.toString(number);
		if(tmpIsFlag==0)
			ret = "\\"+ret;
		else if(tmpIsFlag==2)
			ret = "\\\\"+ret;
		return ret;
	}
	
	public void addString(StringBuilder sb) {
		if(tmpIsFlag==0)
			sb.append("\\");
		else if(tmpIsFlag==2)
			sb.append("\\\\");
		sb.append(number);
	}
}
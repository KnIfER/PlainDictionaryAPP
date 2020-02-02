package com.knziha.text;


import android.os.Build;

public class BreakIteratorHelper {
	android.icu.text.BreakIterator BreakIteratorI;
	java.text.BreakIterator BreakIteratorJ;
	static boolean isAndroidBreakerAvailable= Build.VERSION.SDK_INT>=Build.VERSION_CODES.N;

	public BreakIteratorHelper(){
		if(isAndroidBreakerAvailable){
			BreakIteratorI=android.icu.text.BreakIterator.getWordInstance();
		}else{
			BreakIteratorJ=java.text.BreakIterator.getWordInstance();
		}
	}

	public void setText(String text) {
		if(isAndroidBreakerAvailable){
			BreakIteratorI.setText(text);
		}else{
			BreakIteratorJ.setText(text);
		}
	}

	public int following(int offset) {
		if(isAndroidBreakerAvailable){
			return BreakIteratorI.following(offset);
		}else{
			return BreakIteratorJ.following(offset);
		}
	}

	public int previous() {
		if(isAndroidBreakerAvailable){
			return BreakIteratorI.previous();
		}else{
			return BreakIteratorJ.previous();
		}
	}
}

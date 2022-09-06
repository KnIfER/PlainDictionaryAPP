package com.knziha.plod.searchtasks.lucene;
 
import java.text.BreakIterator;
import java.util.Locale;
 
import android.app.Activity;
import android.os.Bundle;

import com.knziha.plod.plaindict.CMN;

public class ICUTestActivity {
    public static void test() {
        final String testStr = "残忍で破壊的な性格がチャームポイントな本作の主人公[6][7]。" +
            	"真尋にひと目惚れし、それ以来、" + 
            	"下心を隠そうともせず猛アタックを続けている。";
        CMN.Log("日文范围划分");
        test(Locale.JAPAN, testStr);
		CMN.Log("中文范围划分");
        test(Locale.CHINA, testStr);
    }
	
    private static void test(Locale where, String text) {
        BreakIterator boundary = BreakIterator.getWordInstance(where);
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); 
        	end != BreakIterator.DONE; 
        		start = end, end = boundary.next()) {
			CMN.Log(text.substring(start, end));
        }
    }
	

}
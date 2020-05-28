/*
 *  Copyright © 2016, Turing Technologies, an unincorporated organisation of Wynne Plaga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

import androidx.core.view.NestedScrollingChildHelper;

import com.knziha.plod.PlainDict.CMN;

public class Utils {
	public static float density;
	
	private static Paint mRectPaint;
	
	private static int FloatTextBG = 0xffffff00;
	
	
	/**
     * @param dp Desired size in dp (density-independent pixels)
     * @param v View
     * @return Number of corresponding density-dependent pixels for the given device
     */
    static int getDP(int dp, View v){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, v.getResources().getDisplayMetrics());
    }

    static int getDP(float dp, Context c){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }
	
	public static NestedScrollingChildHelper mNestedScrollingChildHelper;
 
	public static NestedScrollingChildHelper getNestedScrollingChildHelper() {
		if(mNestedScrollingChildHelper==null)
			mNestedScrollingChildHelper=new NestedScrollingChildHelper(null);
		return mNestedScrollingChildHelper;
	}
	
	public static Paint getRectPaint() {
		if(mRectPaint==null) {
			mRectPaint = new Paint();
			mRectPaint.setColor(FloatTextBG);
		}
		return mRectPaint;
	}
	
	public static void setFloatTextBG(int colorVal) {
		FloatTextBG = colorVal;
		if(mRectPaint!=null) {
			mRectPaint.setColor(colorVal);
		}
	}
	
	public void Destory(){
		mNestedScrollingChildHelper.Destory();
		mNestedScrollingChildHelper = null;
		mRectPaint = null;
	}
	
	public static class DummyOnClick implements View.OnClickListener {
		@Override
		public void onClick(View v) {

		}
	}
}

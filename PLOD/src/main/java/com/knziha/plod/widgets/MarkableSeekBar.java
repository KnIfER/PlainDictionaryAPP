/*
 * Copyright (C) 2019 KnIfER
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.R;

import java.util.TreeSet;

public class MarkableSeekBar extends SeekBar {
    private Paint mPaint;
    TreeSet<Long> tree;
    private float xLeft;
    private float xRight;

    public MarkableSeekBar(Context context) {
        this(context, null);
    }

    public MarkableSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);//guess what, change it to super-call and surprise!
    }

    public MarkableSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ini();
    }

    public void ini(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(100);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        xLeft = getPaddingLeft();//+thumbDelta/2;
        xRight = getMeasuredWidth() - getPaddingRight() ;//-thumbDelta/2;
		// Section Marks
        // canvas.drawCircle(x_, yTop, 10.0f, mPaint);
		boolean isRTL = getLayoutDirection()== View.LAYOUT_DIRECTION_RTL;
        if(tree!=null)
        for(long time:tree){
        	float progress=(float)time/(float)getMax();
        	if(isRTL) progress=1-progress;
            float tmp3 = xLeft +progress*(-xLeft + xRight);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				canvas.drawOval(tmp3-1,0,tmp3+1,getHeight() , mPaint);
			}else{
				canvas.drawRect(tmp3-1,0,tmp3+1,getHeight() , mPaint);
			}
		}
    }
}

package com.knziha.text;

import android.graphics.Color;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.knziha.plod.plaindict.R;

/**
 * Base annotation span class
 */
public class SubtitlesTextSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {

    public SubtitlesTextSpan() {
    }

    @Override
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }


    public int getSpanTypeIdInternal() {
        return R.id.position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
		//textPaint.setFlags(textPaint.getFlags() | 0x80000000);
		if(textPaint.bgColor!=0){
			textPaint.bgColor= ColorUtils.blendARGB(textPaint.bgColor, Color.BLACK, 0.5f);
		}

    }
}
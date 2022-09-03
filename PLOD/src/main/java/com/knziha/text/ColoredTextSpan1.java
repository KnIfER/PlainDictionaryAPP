package com.knziha.text;

import android.graphics.Color;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.logger.CMN;
import com.knziha.plod.plaindict.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base annotation span class
 */
public class ColoredTextSpan1 extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {
    public int mColor;
    public int type;
    public float thickness;

    public ColoredTextSpan1(@ColorInt int mColor_) {
        super();
        init();
        thickness = 8.f;
        mColor=mColor_;
        type=1;
    }

    public ColoredTextSpan1(@ColorInt int mColor_, float thickness_, int type_) {
        super();
        init();
        mColor = mColor_;
        thickness = thickness_;
        mColor=mColor_;
        type=type_;
    }
    static Method setUnderlineText_m_;
    private void init() {
        if(setUnderlineText_m_==null)
        try {
            setUnderlineText_m_ = TextPaint.class.getMethod("setUnderlineText", int.class,float.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
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
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(@NonNull Parcel dest, int flags) {
        dest.writeInt(mColor);
    }

    /**
     * @param textPaint
     */
    @Override
    public void updateDrawState(TextPaint textPaint) {
        if((textPaint.getFlags() & 0x10000)!=0)
            textPaint.setFakeBoldText(true);
        if((type & 2) != 0 && setUnderlineText_m_!=null)
        try {
            textPaint.setFlags(textPaint.getFlags() | 0x10000);
            setUnderlineText_m_.invoke(textPaint, mColor, thickness);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        if((type & 1) != 0) {
            if (bOverrideBG || textPaint.bgColor == 0)
                textPaint.bgColor = GlobalOptions.isDark?0x88777777:mColor; //todo opt
            else if(!bOverrideBG){
                textPaint.setFakeBoldText(true);
                //textPaint.bgColor = 0xa82b43e1;
            }
        }
	
		textPaint.setColor(GlobalOptions.isDark?0xFFcfcfcf:Color.WHITE);
    }
    protected boolean bOverrideBG=false;
}
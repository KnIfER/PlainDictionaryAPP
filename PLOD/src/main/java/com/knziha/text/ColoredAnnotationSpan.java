package com.knziha.text;

import android.text.TextPaint;

import androidx.annotation.ColorInt;

/**
 * Annotation span
 */
public class ColoredAnnotationSpan extends ColoredTextSpan {
    public ColoredAnnotationSpan(@ColorInt int mColor_) {
        super(mColor_);
        bOverrideBG=true;
    }

    public ColoredAnnotationSpan(@ColorInt int mColor_,float thickness_,  int type_) {
        super(mColor_,thickness_,type_);
        bOverrideBG=true;
    }
    @Override
    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
    }
}
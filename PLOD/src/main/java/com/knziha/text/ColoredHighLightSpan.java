package com.knziha.text;

import android.text.TextPaint;

import androidx.annotation.ColorInt;

/**
 * Background highlight
 */
public class ColoredHighLightSpan extends ColoredTextSpan {
    public ColoredHighLightSpan(@ColorInt int mColor_) {
        super(mColor_);
    }

    public ColoredHighLightSpan(@ColorInt int mColor_,float thickness_,  int type_) {
        super(mColor_,thickness_,type_);
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
    }
}
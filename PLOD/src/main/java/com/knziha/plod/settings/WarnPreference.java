package com.knziha.plod.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;


/**
 * A preference type that allows a user to click and view an url
 *
 * @author KnIfER
 */
public class WarnPreference extends Preference
{
    public WarnPreference(Context context) {
		this(context, null);
    }

    public WarnPreference(Context context, AttributeSet attrs) {
		this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.preferenceStyle,
				android.R.attr.preferenceStyle));
    }

    public WarnPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPersistent(false);
    }
}
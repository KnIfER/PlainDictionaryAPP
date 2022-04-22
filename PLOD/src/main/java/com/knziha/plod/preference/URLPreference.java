package com.knziha.plod.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;


/**
 * A preference type that allows a user to click and view an url
 *
 * @author KnIfER
 */
public class URLPreference extends Preference implements Preference.OnPreferenceClickListener
{
    public URLPreference(Context context) {
		this(context, null);
    }

    public URLPreference(Context context, AttributeSet attrs) {
		this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.preferenceStyle,
				android.R.attr.preferenceStyle));
    }

    public URLPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnPreferenceClickListener(this);
        setPersistent(false);
    }

	@Override
    public boolean onPreferenceClick(Preference preference) {
    	String url = getKey();
    	if(url.toLowerCase().startsWith("http")){
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			try {
				getContext().startActivity(intent);
				return true;
			} catch (Exception ignored) {  }
		}
		return false;
    }
}
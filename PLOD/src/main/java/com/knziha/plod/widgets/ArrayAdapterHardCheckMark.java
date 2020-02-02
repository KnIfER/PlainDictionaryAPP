package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import java.util.List;

public class ArrayAdapterHardCheckMark<T>  extends ArrayAdapter<T>  {
	public ArrayAdapterHardCheckMark(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<T> objects) {
		super(context, resource, textViewResourceId, objects);
	}


	@NonNull @Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		View ret =  super.getView(position, convertView, parent);
		CheckedTextViewmy tv;
		if(ret.getTag()==null)
			ret.setTag(tv = ret.findViewById(android.R.id.text1));
		else
			tv = (CheckedTextViewmy)ret.getTag();
		tv.setCheckMarkDrawable(null);
		if(GlobalOptions.isDark)
			tv.setTextColor(Color.WHITE);
		return ret;
	}
}

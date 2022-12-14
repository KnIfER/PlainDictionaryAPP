package com.knziha.plod.dictionarymanager;

import android.os.Build;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.FlowTextView;

public class ViewHolder{
		public int position;
		public View itemView;
		public ImageView handle;
		public FlowTextView title;
		public CheckBox ck;
		boolean isDark;
		
		public ViewHolder(View v) {
			itemView = v;
			handle = v.findViewById(R.id.drag_handle);
			title = v.findViewById(R.id.text);
			ck = v.findViewById(R.id.check1);
			v.setTag(this);
		}
		
		public void tweakCheck() {
			if (isDark!= GlobalOptions.isDark) {
				isDark = isDark!=GlobalOptions.isDark;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					ck.getButtonDrawable().setColorFilter(isDark?GlobalOptions.NEGATIVE_1:null);
				}
			}
		}
	}

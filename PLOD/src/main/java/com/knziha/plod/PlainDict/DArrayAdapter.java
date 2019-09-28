package com.knziha.plod.PlainDict;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class DArrayAdapter extends ArrayAdapter<File>{
	ArrayList<File> items;
	public boolean showDelete;
	int selectedPos = -1;
	public DArrayAdapter(PDICMainActivity a, int resource, int textViewResourceId, ArrayList<File> objects) {
		this((Context)a,resource,textViewResourceId,objects);
	}

	public DArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull ArrayList<File> objects) {
		super(context, resource, textViewResourceId, objects);
		items=objects;
	}

	@Override
	public View getView(final int position, @Nullable View convertView,
						@NonNull ViewGroup parent) {
		View ret = super.getView(position,convertView,parent);
		if(position==selectedPos)
			ret.setActivated(true);
		else
			ret.setActivated(false);

		String name = items.get(position).getName();
		((TextView)(ret.findViewById(R.id.text1))).setText(name.substring(0,name.length()-4));
		View remove = ret.findViewById(R.id.del);
		if(showDelete)
			remove.setVisibility(View.VISIBLE);
		else
			remove.setVisibility(View.GONE);
		remove.setOnClickListener(v -> {
			//con.remove(getItem(position));
			File fi = items.get(position);
			if(new File(((PDICMainActivity)getContext()).favoriteCon.pathName).getAbsolutePath().equals(fi.getAbsolutePath())) {
			}
			fi.delete();
			new File(fi.getAbsolutePath()+"-journal").delete();
			items.remove(position);
			DArrayAdapter.super.notifyDataSetChanged();
		});
		return ret;
	}

	@Override
	public void notifyDataSetChanged(){
		File[] fl = new File(((PDICMainActivity)getContext()).opt.pathToInternal().append("favorites/").toString()).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.getPath().endsWith(".sql")) return true;
				return false;
			}});

		for(File i:fl) {
			if(!items.contains(i))
				items.add(i);
		}
		for(File fi:items) {
			if(new File(((PDICMainActivity)getContext()).favoriteCon.pathName).equals(fi)) {
				selectedPos = items.indexOf(fi);
				break;
			}
		}
		super.notifyDataSetChanged();
	}

	public void setSelection(int position) {
		selectedPos = position;
		super.notifyDataSetChanged();
	}
}

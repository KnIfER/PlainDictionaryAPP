package com.knziha.plod.PlainUI;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;
import androidx.core.view.ViewCompat;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.searchtasks.BuildIndexTask;
import com.knziha.plod.widgets.AdvancedNestScrollListview;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class BuildIndexInterface extends BaseAdapter
			implements AdapterView.OnItemClickListener, View.OnClickListener{
		public final ViewGroup buildIndexLayout;
		public final ListView listview;
		final ArrayList<Long> IndexingBooks;
		final MainActivityUIBase a;
		public BuildIndexInterface(MainActivityUIBase a, ArrayList<Long> indexingBooks) {
			this.a = a;
			IndexingBooks = indexingBooks;
			buildIndexLayout = (ViewGroup) a.getLayoutInflater().inflate(R.layout.build_index, a.root, false);
			listview = buildIndexLayout.findViewById(R.id.listview);
			listview.setAdapter(this);
			listview.setOnItemClickListener(this);
			((AdvancedNestScrollListview)listview).setNestedScrollingEnabled(PDICMainAppOptions.getEnableSuperImmersiveScrollMode());
			ViewGroup bottombar = buildIndexLayout.findViewById(R.id.bottombar);
			Utils.setOnClickListenersOneDepth(bottombar, this, 999, null);
			ColorStateList tint = ColorStateList.valueOf(a.MainBackground);
			for (int i = 0; i < bottombar.getChildCount(); i++) {
				ViewCompat.setBackgroundTintList(bottombar.getChildAt(i), tint);
			}
		}
		@Override public int getCount() { return IndexingBooks.size(); }
		@Override public Object getItem(int position) { return null; 	}
		@Override public long getItemId(int position) { return 0; }
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null) convertView = a.getLayoutInflater().inflate(R.layout.listview_item0, parent, false);
			CharSequence name=null, path=null;
			boolean selected = false;
			try {
				long value = IndexingBooks.get(position);
				PlaceHolder placeHolder = a.getPlaceHolderAt((int)value);
				selected = value>>32==1;
				name = placeHolder.getName();
				File f = placeHolder.getPath(a.opt);
				path = mp4meta.utils.CMN.formatSize(f.length())+" "+f.getPath();
			} catch (Exception ignored) { }
			TextView tv = convertView.findViewById(R.id.text);
			tv.setText(name);
			tv.setTextColor(selected?a.AppBlack:0xFF888888);
			FlowTextView tv1 = convertView.findViewById(R.id.subtext);
			tv1.trim=false;
			tv1.setTextColor(GlobalOptions.isDark?0xFF9999aa:0xFF333333);
			tv1.setText((String)path);
			return convertView;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				long value = IndexingBooks.get(position);
				boolean selected = value>>32==1;
				value&=0xFFFFFFFFL;
				if(!selected) {
					value |= 0x1L<<32;
				}
				IndexingBooks.set(position, value);
				((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();
			} catch (Exception ignored) { }
		}
		
		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.start) {
				new BuildIndexTask((PDICMainActivity) a, IndexingBooks).execute("");
			} else {
				HashSet<Integer> done = new HashSet<>();
				for (Long val:IndexingBooks) {
					done.add((int)(long)val);
				}
				for (int i = 0; i < a.md.size(); i++) {
					if(!done.contains(i) && a.md.get(i)==null) {
						PlaceHolder placeHolder = a.getPlaceHolderAt(i);
						if (placeHolder!=null) {
							if (placeHolder.NeedsBuildIndex()) {
								IndexingBooks.add((0x1L<<32)|i);
							} else {
								String name = new File(placeHolder.pathname).getName();
								String nameLower = name.toLowerCase();
								if (nameLower.endsWith("dsl")||nameLower.endsWith("dsl.dz")) {
									if (!new File(a.getExternalFilesDir("DzIndex"), name + ".idx").exists()) {
										IndexingBooks.add((0x1L<<32)|i);
									}
								}
							}
						}
					}
				}
				notifyDataSetChanged();
			}
		}
	}
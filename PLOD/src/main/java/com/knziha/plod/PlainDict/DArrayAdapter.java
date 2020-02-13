package com.knziha.plod.PlainDict;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionary.Utils.MyPair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import db.LexicalDBHelper;

public class DArrayAdapter extends BaseAdapter {
	final ArrayList<MyPair<String, LexicalDBHelper>> items;
	final HashSet<Integer> selectedPositions;
	public boolean showDelete;
	final MainActivityUIBase a;

	public DArrayAdapter(MainActivityUIBase a) {
		this.a = a;
		AgentApplication app = ((AgentApplication) a.getApplication());
		items = app.AppDatabases;
		if(app.bNeedPullFavorites){
			loadInFavorites();
			app.bNeedPullFavorites =false;
		}
		selectedPositions = app.selectedPositions();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public String getItem(int position) {
		return items.get(position).key;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void remove(int position) {
		items.remove(position);
		notifyDataSetChanged();
	}

	public void setChecked(int position, boolean checked) {
		if(checked)
			selectedPositions.add(position);
		else
			selectedPositions.remove(position);
	}

	public static class ViewHolder{
		int type;
		int position;
		CheckedTextView title;
		ImageView ivDel;
		Drawable checkMark;
		ViewHolder(MainActivityUIBase a, View itemView){
			itemView.setTag(this);
			title = itemView.findViewById(android.R.id.text1);
			ivDel = itemView.findViewById(R.id.ivDeleteText_ADA);
			checkMark = title.getCompoundDrawablesRelative()[0];
			ivDel.setOnClickListener(a);
		}

		/** @param _type : 0=single&multiple selector; 1=delete&single selector  */
		void AdaptToType(int _position, int _type, DArrayAdapter ada){
			position = _position;
			if(type!=_type){
				if(_type==0){
					title.setCompoundDrawablesRelative(checkMark, null, null, null);
					ivDel.setVisibility(View.VISIBLE);
					ivDel.setImageResource(R.drawable.abc_ic_go_search_api_material);
				} else {
					title.setCompoundDrawablesRelative(null, null, null, null);
					ivDel.setImageResource(R.drawable.icon_denglu_close);
				}
				type=_type;
			}
			if(type==0)
				title.setChecked(ada.selectedPositions.contains(this.position));
			else
				ivDel.setVisibility(ada.showDelete?View.VISIBLE:View.GONE);
		}
	}

	@Override
	public View getView(final int position, @Nullable View convertView,
						@NonNull ViewGroup parent) {
		ViewHolder vh = convertView==null?
				new ViewHolder(a, convertView = a.inflater.inflate(R.layout.listview_check_select, parent, false))
				: (ViewHolder) convertView.getTag();
		String name = items.get(position).key;
		boolean actived = name.equals(a.opt.getCurrFavoriteDBName());
		if(actived && selectedPositions.size()==0)    //todo 精确添加模式，自动勾选收藏有该文本的数据库，而取消勾选后则从数据库删除。
			selectedPositions.add(position);
		convertView.setActivated(actived);
		vh.title.setText(CMN.unwrapDatabaseName(name));
		vh.title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
		vh.AdaptToType(position, parent.getId()==R.id.favorList?0:1, this);
		return convertView;
	}

	public void loadInFavorites(){
		CMN.Log("扫描所有收藏夹……");
		PDICMainAppOptions opt = a.opt;
		HashMap<String, MyPair<String, LexicalDBHelper>> map = new HashMap<>();
		for(MyPair<String, LexicalDBHelper> itemI:items){
			map.put(itemI.key, itemI);
		}
		items.clear();
		new File(opt.pathToFavoriteDatabases().toString()).list((path, name) -> {
			if(name.endsWith(".sql") || name.endsWith(".sql.db")) {//MIMU will add db suffix
				MyPair<String, LexicalDBHelper> item = map.get(name);
				if(item==null)
					item = new MyPair<>(name, null);
				items.add(item);
			}
			return false;
		});
		if(items.size()>0 && opt.getCurrFavoriteDBName()==null){
			opt.putCurrFavoriteDBName(items.get(0).key);
		}
	}

	public void createNewDatabase(String name) {
		if(!name.endsWith(".sql"))
			name+=".sql";
		for(MyPair<String, LexicalDBHelper> mpI:items){
			if(mpI.key.equals(name)){
				a.showT("已存在！");
				return;
			}
		}
		items.add(new MyPair<>(name, new LexicalDBHelper(a, a.opt,name)));
		notifyDataSetChanged();
	}
}

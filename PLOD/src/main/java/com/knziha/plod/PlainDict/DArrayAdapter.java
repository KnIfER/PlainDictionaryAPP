package com.knziha.plod.plaindict;

import android.content.ContentValues;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import db.LexicalDBHelper;

import static com.knziha.plod.plaindict.PDICMainAppOptions.testDBV2;
import static db.LexicalDBHelper.TABLE_FAVORITE_FOLDER_v2;

public class DArrayAdapter extends BaseAdapter {
	final ArrayList<MyPair<String, Long>> notebooksV2;
	final ArrayList<MyPair<String, LexicalDBHelper>> notebooks;
	final HashSet<Long> selectedPositions;
	public boolean showDelete;
	final MainActivityUIBase a;
	public Long[] selectedPositionsArr;
	
	public DArrayAdapter(MainActivityUIBase a) {
		this.a = a;
		AgentApplication app = ((AgentApplication) a.getApplication());
		notebooks = app.AppDatabases;
		notebooksV2 = app.AppDatabasesV2;
		if(app.bNeedPullFavorites){
			loadInFavorites();
			app.bNeedPullFavorites =false;
		}
		selectedPositions = new HashSet<>();
	}

	@Override
	public int getCount() {
		return testDBV2?notebooksV2.size()
				:notebooks.size();
	}

	@Override
	public String getItem(int position) {
		return testDBV2?notebooksV2.get(position).key
				:notebooks.get(position).key;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void remove(int position) {
		if (testDBV2) {
			notebooksV2.remove(position);
		} else {
			notebooks.remove(position);
		}
		notifyDataSetChanged();
	}

	public void setChecked(int position, boolean checked) {
		if (testDBV2) {
			if(checked)
				selectedPositions.add(notebooksV2.get(position).value);
			else
				selectedPositions.remove(notebooksV2.get(position).value);
		} else {
			if(checked)
				selectedPositions.add((long)position);
			else
				selectedPositions.remove((long)position);
		}
	}
	
	public void adaptToMultipleCollections(String lex) {
		selectedPositions.clear();
		for (MyPair<String, Long> notebook:notebooksV2) {
			if (a.prepareHistroyCon().containsPrecise(lex, notebook.value)) {
				selectedPositions.add(notebook.value);
			}
		}
		selectedPositionsArr = selectedPositions.toArray(new Long[selectedPositions.size()]);
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
		void AdaptToType(int _position, int _type, DArrayAdapter ada, long pos_id){
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
				title.setChecked(ada.selectedPositions.contains(pos_id));
			else
				ivDel.setVisibility(ada.showDelete?View.VISIBLE:View.GONE);
		}
	}

	@Override
	public View getView(final int position, @Nullable View convertView,
						@NonNull ViewGroup parent) {
		ViewHolder vh = convertView==null?
				new ViewHolder(a, convertView = a.getLayoutInflater().inflate(R.layout.listview_check_select, parent, false))
				: (ViewHolder) convertView.getTag();
		String name = getItem(position);
		boolean actived;
		if (testDBV2) {
			actived = notebooksV2.get(position).value == a.opt.getCurrFavoriteNoteBookId();
		} else {
			actived = name.equals(a.opt.getCurrFavoriteDBName());
		}
		if (!testDBV2) {
			if(actived && selectedPositions.size()==0)
				selectedPositions.add((long)position);
		}
		convertView.setActivated(actived);
		vh.title.setText(CMN.unwrapDatabaseName(name));
		vh.title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
		vh.AdaptToType(position, parent.getId()==R.id.favorList?0:1, this, testDBV2?notebooksV2.get(position).value:position);
		return convertView;
	}

	public void loadInFavorites(){
		PDICMainAppOptions opt = a.opt;
		CMN.Log("扫描所有收藏夹……");
		if (testDBV2) {
			notebooksV2.clear();
			boolean defaultRecorded = false;
			try (Cursor cursor = a.prepareHistroyCon().getDB().rawQuery("select id,lex from favfolder order by creation_time", null)){
				while (cursor.moveToNext()) {
					long id = cursor.getLong(0);
					if (id==0) {
						defaultRecorded = true;
					}
					notebooksV2.add(new MyPair<>(cursor.getString(1), id));
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
			if (!defaultRecorded) {
				CMN.Log("添加默认::");
				notebooksV2.add(new MyPair<>("默认收藏夹", 0L));
				ContentValues contentValues = new ContentValues();
				contentValues.put("id", 0);
				contentValues.put("lex", "默认收藏夹");
				contentValues.put("creation_time", CMN.now());
				try {
					a.prepareHistroyCon().getDB().insert(TABLE_FAVORITE_FOLDER_v2, null, contentValues);
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}
		else {
			HashMap<String, MyPair<String, LexicalDBHelper>> map = new HashMap<>(notebooks.size());
			for(MyPair<String, LexicalDBHelper> nb: notebooks){
				map.put(nb.key, nb);
			}
			notebooks.clear();
			
			String[] names = opt.fileToDatabaseFavorites().list();
			if(names!=null){
				for (String nI:names) {
					if(nI.endsWith(".sql") || nI.endsWith(".sql.db")) {//MIMU will add db suffix
						MyPair<String, LexicalDBHelper> item = map.get(nI);
						if(item==null){
							item = new MyPair<>(nI, null);
						}
						notebooks.add(item);
					}
				}
			}
			map.clear();
			
			if(notebooks.size()>0 && opt.getCurrFavoriteDBName()==null){
				opt.putCurrFavoriteDBName(notebooks.get(0).key);
			}
		}
	}

	public void createNewDatabase(String name) {
		try {
			if (testDBV2) {
				for(MyPair<String, Long> mpI: notebooksV2){
					if(mpI.key.equals(name)){
						a.showT("已存在！");
						return;
					}
				}
				notebooksV2.add(new MyPair<>(name, a.prepareHistroyCon().newFavFolder(name)));
			} else {
				if(!name.endsWith(".sql"))
					name+=".sql";
				for(MyPair<String, LexicalDBHelper> mpI: notebooks){
					if(mpI.key.equals(name)){
						a.showT("已存在！");
						return;
					}
				}
				notebooks.add(new MyPair<>(name, new LexicalDBHelper(a, a.opt,name)));
			}
		} catch (Exception e) {
			a.showT("创建失败！");
		}
		notifyDataSetChanged();
	}
}

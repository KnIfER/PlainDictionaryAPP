package com.knziha.plod.plaindict;

import static com.knziha.plod.db.LexicalDBHelper.TABLE_FAVORITE_FOLDER_v2;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.plod.dictionary.Utils.StrId;

import java.util.ArrayList;

public class FavFolderAdapter extends BaseAdapter {
	public final ArrayList<StrId> folders;
	public final LongSparseArray<Long> selected = new LongSparseArray<>();
	public final LongSparseArray<Long> re_selected = new LongSparseArray<>();
	public LongSparseArray<Long> oldFidAndLvls = new LongSparseArray<>();
	
	public boolean showDelete;
	final MainActivityUIBase a;
	
	public FavFolderAdapter(MainActivityUIBase a) {
		this.a = a;
		AgentApplication app = ((AgentApplication) a.getApplication());
		folders = app.AppDatabasesV2;
		if(app.bNeedPullFavorites){
			loadInFavorites();
			app.bNeedPullFavorites =false;
		}
	}

	@Override
	public int getCount() {
		return folders.size();
	}

	@Override
	public String getItem(int position) {
		return folders.get(position).data;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void remove(int position) {
		folders.remove(position);
		notifyDataSetChanged();
	}

	public void setChecked(int position, boolean checked) {
		long fid = folders.get(position).id;
		if(checked)
			selected.put(fid, oldFidAndLvls.get(fid, 0L));
		else
			selected.remove(fid);
	}
	
	/** refresh Already Added Folders id */
	public void refreshAddedFoldersForText(String lex) {
		selected.clear();
		for (StrId folder: folders) {
			long lev = a.prepareHistoryCon().GetFavoriteLevel(lex, folder.id);
			if (lev!=Long.MIN_VALUE) {
				selected.put(folder.id, lev);
			}
		}
		oldFidAndLvls = selected.clone();
	}
	
	public static class ViewHolder{
		int type;
		int position;
		CheckedTextView title;
		TextView subtitle;
		ImageView ivDel;
		Drawable checkMark;
		ViewHolder(MainActivityUIBase a, View itemView){
			itemView.setTag(this);
			title = itemView.findViewById(android.R.id.text1);
			subtitle = itemView.findViewById(android.R.id.text2);
			ivDel = itemView.findViewById(R.id.ivDeleteText_ADA);
			checkMark = title.getCompoundDrawablesRelative()[0];
			ivDel.setOnClickListener(a);
			subtitle.setOnClickListener(a);
		}

		/**
		 * @param a
		 * @param _type : 0=single&multiple selector; 1=delete&single selector   */
		void AdaptToType(MainActivityUIBase a, int _position, int _type, FavFolderAdapter ada, long fid){
			position = _position;
			if(type!=_type){
				if(_type==0){
					title.setCompoundDrawablesRelative(checkMark, null, null, null);
					ivDel.setVisibility(View.VISIBLE);
					ivDel.setImageResource(R.drawable.abc_ic_go_search_api_material);
				} else {
					title.setCompoundDrawablesRelative(null, null, null, null);
					ivDel.setImageResource(R.drawable.icon_denglu_close);
					subtitle.setVisibility(View.GONE);
				}
				type=_type;
			}
			if(type==0) {
				Long lev = ada.selected.get(fid, Long.MIN_VALUE);
				boolean selected = lev!=Long.MIN_VALUE;
				title.setChecked(selected);
				subtitle.setVisibility(View.VISIBLE);
				subtitle.setTextColor(a.AppBlack);
				if (selected) {
					if (lev == 0) {
						subtitle.setText("无等级");
						subtitle.setAlpha(.5f);
					} else {
						subtitle.setText(lev + "星");
						subtitle.setAlpha(1);
					}
				} else {
					subtitle.setText("无等级");
					subtitle.setAlpha(.5f);
				}
			}
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
		boolean actived = folders.get(position).id == a.opt.getCurrFavoriteNoteBookId();
		convertView.setActivated(actived);
		vh.title.setText(name);
		vh.title.setTextColor(a.AppBlack);
		vh.AdaptToType(a, position, parent.getId()==R.id.favorList?0:1, this, folders.get(position).id);
		return convertView;
	}

	public void loadInFavorites(){
		CMN.Log("扫描所有收藏夹……");
		folders.clear();
		boolean defaultRecorded = false;
		try (Cursor cursor = a.prepareHistoryCon().getDB().rawQuery("select id,lex from favfolder order by creation_time", null)){
			while (cursor.moveToNext()) {
				long id = cursor.getLong(0);
				if (id==0) {
					defaultRecorded = true;
				}
				folders.add(new StrId(cursor.getString(1), id));
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		if (!defaultRecorded) {
			CMN.Log("添加默认::");
			folders.add(new StrId("默认收藏夹", 0L));
			ContentValues contentValues = new ContentValues();
			contentValues.put("id", 0);
			contentValues.put("lex", "默认收藏夹");
			contentValues.put("creation_time", CMN.now());
			try {
				a.prepareHistoryCon().getDB().insert(TABLE_FAVORITE_FOLDER_v2, null, contentValues);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
	}

	public void createNewDatabase(String name) {
		try {
			for(StrId folder: folders){
				if(folder.data.equals(name)){
					a.showT("已存在！");
					return;
				}
			}
			folders.add(new StrId(name, a.prepareHistoryCon().newFavFolder(name)));
		} catch (Exception e) {
			CMN.debug(e);
			a.showT("创建失败！");
		}
		notifyDataSetChanged();
	}
}

package com.knziha.plod.widgets;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.bookPresenter_pdf;
import com.knziha.plod.dictionarymodels.bookPresenter_web;

import db.MdxDBHelper;

/**
 * Created by KnIfER on 2018/3/26.
 */

public class ArrayAdaptermy extends BaseAdapter{
	MdxDBHelper con;
	public Cursor cr;
	BookPresenter md;
	boolean isWeb;
	public boolean showDelete;
	private final SparseArray<String> DataRecord = new SparseArray<>();
	int resourceID;
	int textViewResourceID;
	Context c;
	public boolean darkMode;
	public int StorageLevel=2;
   public ArrayAdaptermy(Context a, int resource, int textViewResourceId, BookPresenter md_, MdxDBHelper con_, int l) {
		//this(a,resource,textViewResourceId,objects);
		c=a;
		resourceID=resource;
		textViewResourceID=textViewResourceId;
		md=md_;
		con = con_;
		StorageLevel=l;
		isWeb = md instanceof bookPresenter_web;
		cr = con.getDB().rawQuery(isWeb?"select * from t3 ":"select * from t1 ", null);
   }

	public void refresh(BookPresenter invoker, MdxDBHelper con_) {
		if(invoker!=md || con!=con_ || cr==null) {
	    	md=invoker;
	    	isWeb = md instanceof bookPresenter_web;
	    	con = con_;
	    	if(cr!=null) cr.close();
	    	cr = con.getDB().rawQuery(isWeb?"select * from t3 ":"select * from t1 ", null);
			DataRecord.clear();
			notifyDataSetChanged();
		}
	}

	public static class ViewHolder_TvAndDel {
		TextView title;
		ImageView ivDel;
		ViewHolder_TvAndDel(ArrayAdaptermy a, View itemView) {
			itemView.setTag(this);
			title = itemView.findViewById(R.id.text1);
			ivDel = itemView.findViewById(R.id.del);
			ivDel.setOnClickListener(v -> {
				int position = (int) v.getTag();
				Cursor cr = a.cr;
				MdxDBHelper con = a.con;
				cr.moveToPosition(position);
				a.DataRecord.clear();
				if(a.isWeb)
					con.remove(cr.getInt(0));
				else
					con.removeUrl(cr.getString(0));
				cr.close();
				a.cr = con.getDB().rawQuery("select * from t1 ", null);
				a.notifyDataSetChanged();
			});
		}
	}

    @Override
    public View getView(final int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {
    	String LexicalText = DataRecord.get(position);
    	if(LexicalText==null) {
    		cr.moveToPosition(position);
    		if(md instanceof bookPresenter_pdf){
				LexicalText="第"+cr.getInt(0)+"页";
			}else if(isWeb){
				LexicalText=cr.getString(1);
			}
    		else try {
				LexicalText=md.getEntryAt(cr.getInt(0));
			} catch (Exception e) {
				LexicalText="!!!Error: "+e.getLocalizedMessage();
			}
    		if(StorageLevel==2) DataRecord.put(position, LexicalText);
    	}

		ViewHolder_TvAndDel vh = (ViewHolder_TvAndDel) (convertView==null?null:convertView.getTag());
		if(vh==null)
			vh = new ViewHolder_TvAndDel(this, convertView=LayoutInflater.from(parent.getContext()).inflate(resourceID, parent, false));
    	TextView tv = vh.title;
    	tv.setText(LexicalText);
    	if(darkMode)
    		tv.setTextColor(Color.WHITE);
        View remove = vh.ivDel;
		remove.setVisibility(showDelete?View.VISIBLE:View.GONE);

        return convertView;
    }

	@Override
	public int getCount() {
		return cr==null?0:cr.getCount();
	}

	@Override
	public Object getItem(int position) {
		cr.moveToPosition(position);
		return cr.getInt(0);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void clear() {
		DataRecord.clear();
		cr.close();
		cr=null;
	}
}

package com.knziha.plod.widgets;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import db.MdxDBHelper;

import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionarymodels.mdict;

/**
 * Created by ASDZXC on 2018/3/26.
 */

public class ArrayAdaptermy extends BaseAdapter{
	MdxDBHelper con;
	public Cursor cr;
	mdict md;
	public boolean showDelete;
	private final SparseArray<String> DataRecord = new SparseArray<>();
	int resourceID;
	int textViewResourceID;
	Context c;
	public boolean darkMode;
	public int StorageLevel=2;
   public ArrayAdaptermy(Context a, int resource,int textViewResourceId, mdict md_,MdxDBHelper con_, int l) {
    	//this(a,resource,textViewResourceId,objects);
	    c=a;
    	resourceID=resource;
    	textViewResourceID=textViewResourceId;
    	md=md_;
    	con = con_;
    	StorageLevel=l;
    	cr = con.getDB().rawQuery("select * from t1 ", null);
   }

	public void refresh(mdict invoker, MdxDBHelper con_) {
		if(invoker!=md || con!=con_ || cr==null) {
	    	md=invoker;
	    	con = con_;
	    	if(cr!=null) cr.close();
	    	cr = con.getDB().rawQuery("select * from t1 ", null);
			DataRecord.clear();
			notifyDataSetChanged();
		}
	}
	

    @Override
    public View getView(final int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {
    	
    	String LexicalText = DataRecord.get(position);
    	if(LexicalText==null) {
    		cr.moveToPosition(position);
    		try {
				LexicalText=md.getEntryAt(cr.getInt(0));
			} catch (Exception e) {
				LexicalText="!!!Error: "+e.getLocalizedMessage();
			}
    		if(StorageLevel==2) DataRecord.put(position, LexicalText);
    		//CMN.Log("putting new vals");
    	}
    	//else CMN.Log("using old vals");
    		
    	convertView = LayoutInflater.from(c).inflate(resourceID, parent, false);
    	TextView tv = ((TextView)(convertView.findViewById(R.id.text1)));
    	tv.setText(LexicalText);
    	if(darkMode)
    		tv.setTextColor(Color.WHITE);

        View remove = convertView.findViewById(R.id.del);
        if(showDelete)
        	remove.setVisibility(View.VISIBLE);
        else
        	remove.setVisibility(View.GONE);
        
        remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cr.moveToPosition(position);
				DataRecord.clear();
				con.remove(cr.getInt(0));
				cr.close();
				cr = con.getDB().rawQuery("select * from t1 ", null);
				//items.remove(position);
				notifyDataSetChanged();
			}});
        
        /*((TextView)(ret.findViewById(R.id.text1))).setText(md.getEntryAt(getItem(position)));
        View remove = ret.findViewById(R.id.del);
        if(showDelete)
        	remove.setVisibility(View.VISIBLE);
        else
        	remove.setVisibility(View.GONE);
        remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cr.moveToPosition(position);
				con.remove(cr.getInt(0));
				//items.remove(position);
				notifyDataSetChanged();
			}});*/
    	
        //ret.setBackgroundColor(Color.parseColor("#000000"));
        return convertView;
    }

	@Override
	public int getCount() {
		return cr.getCount();
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
	}
}

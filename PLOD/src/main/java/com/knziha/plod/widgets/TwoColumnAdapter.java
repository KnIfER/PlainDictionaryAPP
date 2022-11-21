package com.knziha.plod.widgets;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

public class TwoColumnAdapter extends RecyclerView.Adapter {
	int[] data;
	String[] dataStr;
	int length;
	private AdapterView.OnItemClickListener listener;
	private AdapterView.OnItemLongClickListener longlistener;
	private int mMaxLines;
	
	private SparseIntArray imageViews = new SparseIntArray();
	private boolean hasImages = false;
	
	public void putImage(int pos, int src) {
		if (pos >= 0) {
			imageViews.put(pos, src);
			hasImages = true;
		} else if(hasImages){
			hasImages = false;
			imageViews.clear();
		}
	}
	
	public TwoColumnAdapter(int[] data) {
		this.data = data;
		length = data.length;
	}

	public TwoColumnAdapter(String[] dataStr) {
		this.dataStr = dataStr;
		length = dataStr.length;
	}

	public void setOnItemClickListener(AdapterView.OnItemClickListener _listener) {
		listener=_listener;
	}

	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener _longlistener) {
		longlistener=_longlistener;
	}

	public void setItems(int[] _data) {
		if(data != _data || _data.length!=length){
			this.data = _data;
			this.dataStr = null;
			length = _data.length;
			//notifyItemRangeChanged(0, data.length);
			notifyDataSetChanged();
		}
	}
	
	public void setItems(String[] data) {
		if (data != null) {
			this.dataStr = data;
			this.data = null;
			length = data.length;
			notifyDataSetChanged();
		}
	}
	
	public RecyclerView.Adapter setMaxLines(int maxLines) {
		mMaxLines = maxLines;
		return this;
	}
	
	static class ViewHolder extends RecyclerView.ViewHolder{
		int position;
		TextView title;
		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			itemView.setTag(this);
			title = itemView.findViewById(android.R.id.text1);
		}
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ViewHolder vh;
		if (viewType == 1) {
			ImageView iv = new ImageView(parent.getContext());
			iv.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			vh = new ViewHolder(iv);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, (int) GlobalOptions.density * 18);
			iv.setLayoutParams(lp);
			iv.setBackground(VU.getListChoiceBackground(parent.getContext()));
			int pad = (int) (GlobalOptions.density * 10);
			iv.setPadding(pad, 0, pad, 0);
			lp.leftMargin = pad/2;
		} else {
			vh = new ViewHolder(
					LayoutInflater.from(parent.getContext()).inflate(
							R.layout.select_dialog_item_material_bg, parent, false)
			
			);
			if(GlobalOptions.isLarge) {
				vh.title.setTextSize(23);
				int pad = (int) (GlobalOptions.density*20);
				vh.title.setPadding(vh.title.getPaddingLeft(), pad, vh.title.getPaddingRight(), pad);
			}
			
			if(mMaxLines>0) {
				vh.title.setMaxLines(mMaxLines);
				vh.title.setVerticalScrollBarEnabled(false);
			}
		}
		vh.itemView.setOnClickListener(v -> {
			if (listener != null)
				listener.onItemClick(null, v, ((ViewHolder)v.getTag()).position, v.getId());
		});
		vh.itemView.setOnLongClickListener(v -> {
			if(longlistener!=null)
				return longlistener.onItemLongClick(null, v, ((ViewHolder)v.getTag()).position, v.getId());
			return false;
		});
		return vh;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		ViewHolder vh = (ViewHolder) holder;
		if (vh.title != null) {
			if (data != null) {
				vh.title.setText(data[position]);
				vh.itemView.setId(data[position]);
			} else {
				vh.title.setText(dataStr[position]);
				vh.itemView.setId(0);
			}
			vh.title.setTextColor(GlobalOptions.isDark ? Color.WHITE : Color.BLACK);
		} else {
			final int src = imageViews.get(position);
			((ImageView)vh.itemView).setImageResource(src);
		}
		vh.position = position;
	}

	@Override
	public int getItemCount() {
		return length;
	}
	
	public boolean isData(Object test) {
		if (test != null) {
			return test == data || test == dataStr;
		}
		return false;
	}
	
	@Override
	public int getItemViewType(int position) {
		if (hasImages && imageViews.get(position)!=0) {
			return 1;
		}
		return 0;
	}
	
}

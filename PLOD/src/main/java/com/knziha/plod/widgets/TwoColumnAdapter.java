package com.knziha.plod.widgets;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.PlainDict.R;

public class TwoColumnAdapter extends RecyclerView.Adapter {
	String[] data;
	private AdapterView.OnItemClickListener listener;
	private AdapterView.OnItemLongClickListener longlistener;

	public TwoColumnAdapter(String[] data) {
		this.data = data;
	}

	public void setOnItemClickListener(AdapterView.OnItemClickListener _listener) {
		listener=_listener;
	}

	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener _longlistener) {
		longlistener=_longlistener;
	}

	public void setItems(String[] _data) {
		if(data != _data){
			data = _data;
			//notifyItemRangeChanged(0, data.length);
			notifyDataSetChanged();
		}
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
		ViewHolder vh = new ViewHolder(
				LayoutInflater.from(parent.getContext()).inflate(
						R.layout.select_dialog_item_material_bg, parent, false)

		);
		if(GlobalOptions.isLarge) {
			vh.title.setTextSize(23);
			int pad = (int) (GlobalOptions.density*20);
			vh.title.setPadding(vh.title.getPaddingLeft(), pad, vh.title.getPaddingRight(), pad);
		}
		vh.itemView.setOnClickListener(v -> {
			if (listener != null) listener.onItemClick(null, v, ((ViewHolder)v.getTag()).position, 0);
		});
		vh.itemView.setOnLongClickListener(v -> {
			if(longlistener!=null) return longlistener.onItemLongClick(null, v, ((ViewHolder)v.getTag()).position, 0);
			return false;
		});
		return vh;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		ViewHolder vh = (ViewHolder) holder;
		vh.title.setText(data[position]);
		vh.position = position;
		vh.title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
	}

	@Override
	public int getItemCount() {
		return data.length;
	}
}

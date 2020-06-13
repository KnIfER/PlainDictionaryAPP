package com.knziha.plod.widgets;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.knziha.plod.PlainDict.R;

import java.util.ArrayList;

/* Custom Share Adapter */
public class CustomShareAdapter extends BaseAdapter {
	ArrayList<String> data;
	private View.OnClickListener ocl=new OnClickListenermy();
	public int nameWidth;

	public CustomShareAdapter(ArrayList<String> _data){
		data = _data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private static class TargetViewHolder{
		View itemView;
		TextView name;
		EditText title;
		View deletText;
		int position;
		final ArrayList<String> data;
		TextWatcher tw;
		TargetViewHolder(ViewGroup parent, View.OnClickListener onc, ArrayList<String> data){
			this.data = data;
			itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_targets, parent, false);
			itemView.setTag(this);
			name = itemView.findViewById(R.id.text2);
			title = itemView.findViewById(R.id.text1);
			deletText = itemView.findViewById(R.id.ivDeleteText);
			title.addTextChangedListener(tw = new TextWatcher(){
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					String text = s.toString().trim();
					data.set(position, text.length()>0?text:null);
				}
			});

			deletText.setOnClickListener(onc);
		}
	}

	private static class OnClickListenermy implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			TargetViewHolder tvh = (TargetViewHolder) ((View)v.getParent()).getTag();
			tvh.title.setText(null);

		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TargetViewHolder tvh = convertView == null ? new TargetViewHolder(parent, ocl, data) : (TargetViewHolder) convertView.getTag();
		/** 至少六个可自定义的字段:<br/>
		 * {p:程序包名 m:活动名称 a:举措名称 t:MIME类型 k1:字段1键名 v1:字段1键值…} */
		String dataVal = data.get(position);
		if (dataVal == null)
		switch (position) {
			case 0:
			case 1:
			break;
			case 2:
				//value = Intent.ACTION_VIEW;
			break;
			case 3:
				//value = "text/plain";
			break;
			case 4:
				//value = Intent.EXTRA_TEXT;
			break;
			case 5:
				//value = "%s";
			break;
			default:
				//if (position % 2 != 0) {
				//	value = "%s";
				//}
			break;
		}

		TextView text = tvh.title;
		text.clearFocus();
		text.removeTextChangedListener(tvh.tw);
		text.setText(dataVal);
		text.addTextChangedListener(tvh.tw);
		tvh.position = position;
		
		int value = 0;
		text = tvh.name;
		ViewGroup.LayoutParams lp = text.getLayoutParams();
		if(lp.width!=nameWidth) {
			lp.width=nameWidth;
		}
		switch (position) {
			case 0:
				value = R.string.package_name;
			break;
			case 1:
				value = R.string.activity_name;
			break;
			case 2:
				value = R.string.action_name;
			break;
			case 3:
				value = R.string.mime_type;
			break;
			default:
				text.setText(parent.getContext().getString(position%2==0?R.string.extra_key_name:R.string.extra_key_value, (position-4)/2+1));
			break;
		}
		
		if(value!=0) {
			text.setText(value);
		}

		return tvh.itemView;
	}
}

package com.knziha.plod.PlainUI;

import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.DescriptiveImageView;

import java.util.ArrayList;

//for menu list
public class SearchToolsMenu extends BaseAdapter implements TwoWayAdapterView.OnItemClickListener
{
	MainActivityUIBase a;
	private TextPaint menu_grid_painter;
	ArrayList<String> menuList = new ArrayList<>();
	public static class MenuItemViewHolder {
		public final DescriptiveImageView tv;
		public MenuItemViewHolder(View convertView) {
			tv = convertView.findViewById(R.id.text);
		}
	}
	
	public SearchToolsMenu(MainActivityUIBase a, TwoWayGridView mainMenuLst) {
		menuList.add("繁简转换");
		menuList.add("翻阅模式");
		menuList.add("多行编辑");
		//menuList.add("二维扫描");
		//menuList.add("文字识别");
		
		this.a = a;
		
		mainMenuLst.setHorizontalSpacing(0);
		mainMenuLst.setVerticalSpacing(0);
		mainMenuLst.setHorizontalScroll(true);
		mainMenuLst.setStretchMode(GridView.NO_STRETCH);
		mainMenuLst.setAdapter(this);
		mainMenuLst.setOnItemClickListener(this);
		mainMenuLst.setScrollbarFadingEnabled(false);
		mainMenuLst.setSelector(a.mResource.getDrawable(R.drawable.listviewselector0));
		mainMenuLst.setBackgroundColor(a.MainAppBackground);
		menu_grid_painter = DescriptiveImageView.createTextPainter(false);
	}
	
	@Override
	public int getCount() {
		return menuList.size();
	}
	
	@Override
	public View getItem(int position) {
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuItemViewHolder holder;
		if(convertView==null) {
			convertView = a.getLayoutInflater().inflate(R.layout.menu_item, parent, false);
			convertView.setTag(holder=new MenuItemViewHolder(convertView));
			holder.tv.textPainter = menu_grid_painter;
		} else {
			holder = (MenuItemViewHolder) convertView.getTag();
		}
		holder.tv.setText(menuList.get(position));
		return convertView;
	}
	
	@Override
	public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case 0:{
			} break;
		}
	}
}

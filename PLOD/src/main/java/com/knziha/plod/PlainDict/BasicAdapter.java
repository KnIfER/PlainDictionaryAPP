package com.knziha.plod.plaindict;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.WebViewmy;

import java.util.List;

public abstract class BasicAdapter extends BaseAdapter
    						implements OnItemClickListener
    {
		WebViewmy mWebView;
		public final ContentviewBinding contentUIData;
		public final WebViewListHandler weblistHandler;
		MenuBuilder allMenus;
		List<MenuItemImpl> contentMenus;
		public ViewGroup webviewHolder;
		public ListView lava;
		int lastClickedPos=-1;
		public int lastClickedPosBefore;
		boolean userCLick;
		public boolean shunt;
		public boolean Kustice;
		public boolean bOnePageNav;

		public SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
		protected BookPresenter presenter;
		//int adelta=0;

		public void ClearVOA() {
			avoyager.clear();
			//adelta=0;
		}
		
		public void SaveVOA() {
		
		}
    	
    	public void setResHolder(resultRecorderDiscrete r) {
    		results =r;
    	}
        //public ArrayList<additiveMyCpr1> combining_search_result = new ArrayList<additiveMyCpr1>();//additiveMyCpr1
        public resultRecorderDiscrete results;
		public String currentKeyText;
        
        //构造
        public BasicAdapter(ContentviewBinding contentUIData, WebViewListHandler weblistHandler, MenuBuilder allMenus, List<MenuItemImpl> contentMenu)
        {
			this.contentUIData = contentUIData;
			this.weblistHandler = weblistHandler;
			this.allMenus=allMenus;
			this.contentMenus=contentMenu;
		}
        @Override
        public int getCount() {
          return results.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
	          return convertView;
        }
		public void shutUp() {
		
		}
		public void onItemClick(int pos) {
        	lastClickedPos = Math.min(getCount(), Math.max(0, pos));
//			weblistHandler.highlightVagranter.HlightIdx=0;
//			weblistHandler.highlightVagranter.AcrArivAcc =0;
			//ActivedAdapter=this;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			onItemClick(pos);
		}

		public abstract int getId();

		public abstract String currentKeyText();
	
		public void setPresenter(BookPresenter presenter) {
			this.presenter = presenter;
		}
	}
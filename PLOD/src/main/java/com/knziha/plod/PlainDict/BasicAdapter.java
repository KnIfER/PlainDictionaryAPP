package com.knziha.plod.plaindict;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;

public abstract class BasicAdapter extends BaseAdapter
    						implements OnItemClickListener
    {
		public ViewGroup webviewHolder;
		public ListView lava;
		int lastClickedPos=-1;
		public int lastClickedPosBeforePageTurn;
		boolean userCLick;
		int HlightIdx;
		int AcrArivAcc;
		public boolean shunt;
		public boolean Kustice;

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
    		combining_search_result=r;
    	}
        //public ArrayList<additiveMyCpr1> combining_search_result = new ArrayList<additiveMyCpr1>();//additiveMyCpr1
        public resultRecorderDiscrete combining_search_result;
		public String currentKeyText;
        
        //构造
        public BasicAdapter()
        {
        
        }
        @Override
        public int getCount() {
          return combining_search_result.size();
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
        	lastClickedPos = Math.min(getCount(), Math.max(-1, pos));
			HlightIdx=0;
			AcrArivAcc =0;
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
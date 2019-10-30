package com.knziha.plod.PlainDict;

import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;

public abstract class BasicAdapter extends BaseAdapter
    						implements OnClickListener,OnItemClickListener
    {
		int lastClickedPos=-1;
		public int lastClickedPosBeforePageTurn;
		int HlightIdx;
		int acrarivacc;

		public final SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
    	//int adelta=0;

		public void ClearVOA() {
			CMN.Log("clearing!!!");
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
        @Override
        public void onClick(View v){
        	lastClickedPos = (int)v.getTag(R.id.position);
        	onItemClick(lastClickedPos);
		}
		public void onItemClick(int pos) {
        	lastClickedPos = Math.max(-1, pos);
        	lastClickedPos = Math.min(getCount(), lastClickedPos);
			HlightIdx=0;
			acrarivacc=0;
			//ActivedAdapter=this;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			//CMN.show("onItemClick00");
			onItemClick(lastClickedPos =pos);
		}

		public abstract int getId();
	}
package com.knziha.plod.PlainDict;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import db.LexicalDBHelper;

public class DHBroswer extends DBroswer {
	public DHBroswer(){
		super();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//CMN.Log("onCreateViewonCreateView");
		View ret = super.onCreateView(inflater, container, savedInstanceState);
		main_clister_layout.findViewById(R.id.choosed).setVisibility(View.GONE);
		main_clister_layout.findViewById(R.id.changed).setVisibility(View.GONE);
		fastScroller.setBarColor(Color.parseColor("#2b4381"));
		return ret;
	}

	@Override
	public void onDetach(){
		super.onDetach();
		if(cr!=null && lm.findFirstVisibleItemPosition()>=1 && mCards_size>=0) {
			CMN.lastHisLexicalEntry = lm.findFirstVisibleItemPosition();
			CMN.lastHisLexicalEntryOff = lv.getChildAt(0).getTop();
		}else {
			CMN.lastHisLexicalEntry=-1;
			CMN.lastHisLexicalEntryOff = 0;
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		mLexiDB = a.historyCon;
		fastScroller.setHandleBackground(a.getResources().getDrawable(R.drawable.ghour));
		lastChecked=0;
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	protected void loadInDataBase(PDICMainActivity a) {
		final File fi = new File(a.historyCon.pathName);
		items.add(fi);
	}

	protected void loadInAll(File filename) {
		mLexiDB = new LexicalDBHelper((MainActivityUIBase)getActivity(),filename);
		cr = mLexiDB.getDB().query("t1", null,null,null,null,null,"date desc");
		mCards_size = cr.getCount();

		show(R.string.maniFavor2,boli(items.get(lastChecked).getName()),mCards_size);
		mAdapter.notifyDataSetChanged();
		lm.scrollToPositionWithOffset(lastFirst,CMN.lastHisLexicalEntryOff);

		hideProgressBar();
	}





	@Override
	public void toggleFavor() {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		String text = mCards.get(currentPos).name;
		long time = mCards.get(currentPos).time;
		if(text==null) {
			cr.moveToPosition(currentPos);
			try {
				text=cr.getString(0);
				time=cr.getLong(1);
			} catch (Exception e) {
				text="!!!Error: "+e.getLocalizedMessage();
			}
		}

		a.favoriteCon.prepareContain();
		if(a.favoriteCon.contains(text)) {//删除
			a.favoriteCon.remove(text);
			a.favoriteBtn.setImageResource(R.drawable.star_ic);
			a.show(R.string.removed);
		}else {//添加
			a.favoriteCon.insert(text);
			a.favoriteBtn.setImageResource(R.drawable.star_ic_solid);
			a.show(R.string.added);
		}


	}

	@Override
	public void processFavorite(int position,String key) {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		a.favoriteCon.prepareContain();
		if(a.favoriteCon.contains(key)) {
			a.favoriteBtn.setImageResource(R.drawable.star_ic_solid);
		}else
			a.favoriteBtn.setImageResource(R.drawable.star_ic);
	}

	@Override
	public int getFragmentId() {
		return 2;
	}
}

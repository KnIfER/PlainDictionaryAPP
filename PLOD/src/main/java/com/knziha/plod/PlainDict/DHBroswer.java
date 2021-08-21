package com.knziha.plod.plaindict;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static db.LexicalDBHelper.TABLE_HISTORY_v2;

public class DHBroswer extends DBroswer {
	public DHBroswer(){
		super();
		mtableName = TABLE_HISTORY_v2;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(main_clister_layout!=null)
			return main_clister_layout;
		View _main_clister_layout = super.onCreateView(inflater, container, savedInstanceState);
		_main_clister_layout.findViewById(R.id.choosed).setVisibility(View.GONE);
		_main_clister_layout.findViewById(R.id.changed).setVisibility(View.GONE);
		fastScroller.setBarColor(Color.parseColor("#2b4381"));
		return main_clister_layout=_main_clister_layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if(!initialized){
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			mLexiDB = a.prepareHistroyCon();
			fastScroller.setHandleBackground(a.getResources().getDrawable(R.drawable.ghour));
			lastChecked=0;
		}
		super.onActivityCreated(savedInstanceState);
	}

	protected void loadInAll(MainActivityUIBase a) {
		CMN.Log("load in all!!!");
		super.loadInAll(a);
//		int offset = 0;
//		lastFirst = 0;
//		if(false){
//			MyIntPair lcibdfn = ((AgentApplication) a.getApplication()).getLastContextualIndexByDatabaseFileName(mLexiDB.DATABASE);
//			if(lcibdfn!=null){
//				lastFirst = Math.min(lcibdfn.key, mCards_size);
//				offset =  lcibdfn.value;
//			}
//		}
		String name = CMN.unwrapDatabaseName(mLexiDB.DATABASE);
		toolbar.setTitle(name);
		show(R.string.maniFavor2, name , dataAdapter.getCount());
//		mAdapter.notifyDataSetChanged();
//		lm.scrollToPositionWithOffset(lastFirst,offset);
		mLexiDB.lastAdded = false;
	}

	@Override
	public void toggleFavor() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null) return;
		String text = currentDisplaying;
		if(a.prepareFavoriteCon().contains(text)) {//删除
			a.favoriteCon.remove(text, opt.getFavoritePerceptsRemoveAll()?-1:opt.getCurrFavoriteNoteBookId());
			a.favoriteBtn.setActivated(false);
			a.show(R.string.removed);
		} else {//添加
			a.favoriteCon.insert(a, text, opt.getCurrFavoriteNoteBookId());
			a.favoriteBtn.setActivated(true);
			a.show(R.string.added);
		}
	}

	@Override
	public void processFavorite(int position,String key) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a!=null && a.favoriteBtn!=null)
			a.favoriteBtn.setActivated(a.prepareFavoriteCon().contains(key));
	}

	@Override
	public int getFragmentId() {
		return DB_HISTORY;
	}
}

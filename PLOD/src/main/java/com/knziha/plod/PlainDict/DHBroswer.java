package com.knziha.plod.PlainDict;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.knziha.plod.dictionary.Utils.MyIntPair;

public class DHBroswer extends DBroswer {
	public DHBroswer(){
		super();
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
			PDICMainActivity a = (PDICMainActivity) getActivity();
			mLexiDB = a.prepareHistroyCon();
			fastScroller.setHandleBackground(a.getResources().getDrawable(R.drawable.ghour));
			lastChecked=0;
		}
		super.onActivityCreated(savedInstanceState);
	}

	protected void loadInAll(MainActivityUIBase a) {
		cr = mLexiDB.getDB().query("t1", null,null,null,null,null,"date desc");
		mCards_size = cr.getCount();
		itemCount = mCards_size;
		//itemCount=Math.min(lastFirst, mCards_size);
		//lv.post(mPullViewsRunnable);
		int offset = 0;
		lastFirst = 0;
		if(true){
			MyIntPair lcibdfn = ((AgentApplication) a.getApplication()).getLastContextualIndexByDatabaseFileName(mLexiDB.DATABASE);
			if(lcibdfn!=null){
				lastFirst = Math.min(lcibdfn.key, mCards_size);
				offset =  lcibdfn.value;
			}
		}
		if(getDelayPullData()) {
			itemCount = lastFirst;
			lv.post(mPullViewsRunnable);
		} else {
			itemCount = mCards_size;
		}
		String name = CMN.unwrapDatabaseName(mLexiDB.DATABASE);
		toolbar.setTitle(name);
		show(R.string.maniFavor2,name,mCards_size);
		mAdapter.notifyDataSetChanged();
		lm.scrollToPositionWithOffset(lastFirst,offset);
	}

	@Override
	public void toggleFavor() {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		String text = currentDisplaying;
		if(a.prepareFavoriteCon().contains(text)) {//删除
			a.favoriteCon.remove(text);
			a.favoriteBtn.setActivated(false);
			a.show(R.string.removed);
		}else {//添加
			a.favoriteCon.insert(text);
			a.favoriteBtn.setActivated(true);
			a.show(R.string.added);
		}
	}

	@Override
	public void processFavorite(int position,String key) {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		a.favoriteBtn.setActivated(a.prepareFavoriteCon().contains(key));
	}

	@Override
	public int getFragmentId() {
		return 2;
	}
}

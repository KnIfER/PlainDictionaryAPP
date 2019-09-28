package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.knziha.plod.PlainDict.PDICMainActivity;
import com.knziha.plod.PlainDict.R;

import java.lang.ref.WeakReference;

@SuppressLint("SetTextI18n")
public class FuzzySearchTask extends AsyncTask<String, Integer, String> {
	private final WeakReference<PDICMainActivity> activity;
	private String CurrentSearchText;

	public FuzzySearchTask(PDICMainActivity a) {
		activity = new WeakReference<>(a);
	}
	@Override
	protected void onPreExecute() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.OnEnterFuzzySearchTask(this);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.updateFFSearch(values[0]);
	}


	@Override
	protected String doInBackground(String... params) {
		if(params.length==0) return null;
		if((CurrentSearchText=params[0])==null || CurrentSearchText.length()==0) 
			return null;
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		if(a.isCombinedSearching){
			for(int i=0;i<a.md.size();i++){
				try {
					publishProgress(i);
					a.md.get(i).flowerFindAllKeys(CurrentSearchText,i,30);
					//publisResults();
					if(isCancelled()) break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.gc();
		}else {
			try {
				publishProgress(a.adapter_idx);
				a.currentDictionary.flowerFindAllKeys(CurrentSearchText,a.adapter_idx,30);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onCancelled(String bitmap) {
		harvest();
	}

	@Override
	protected void onPostExecute(String bitmap) {
		harvest();
	}

	void harvest() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		if(a.timer!=null) a.timer.cancel(); a.timer=null;
		if(a.d!=null) a.d.dismiss();
		//if(adaptermy3.combining_search_result==null)
		//	adaptermy3.combining_search_result = new resultRecoderScattered(md);

		a.adaptermy3.combining_search_result.SearchText=CurrentSearchText;
		if(a.isCombinedSearching){
			a.adaptermy3.combining_search_result.invalidate();
			System.gc();
		}else{//单独搜索
			a.adaptermy3.combining_search_result.invalidate(a.adapter_idx);
			System.gc();
		}

		a.show(R.string.fuzzyfill,(System.currentTimeMillis()-PDICMainActivity.stst)*1.f/1000,a.adaptermy3.getCount());

		a.adaptermy3.notifyDataSetChanged();
		a.mlv1.setSelection(0);
	}
}

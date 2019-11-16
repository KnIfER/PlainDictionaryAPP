package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainActivity;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionarymodels.mdict;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

@SuppressLint("SetTextI18n")
public class FullSearchTask extends AsyncTask<String, Integer, String > {
	private final WeakReference<PDICMainActivity> activity;
	private String CurrentSearchText;

	public FullSearchTask(PDICMainActivity a) {
		activity = new WeakReference<>(a);
	}
	@Override
	protected void onPreExecute() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.OnEnterFullSearchTask(this);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.updateFFSearch(values[0]);
	}

	@Override
	protected String doInBackground(String... params) {
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		if(params.length==0) return null;
		if((CurrentSearchText=params[0])==null || CurrentSearchText.length()==0)
			return null;
		if(a.isCombinedSearching){
			for(int i=0;i<a.md.size();i++){
				try {
					publishProgress(i);//_mega
					a.md.get(i).flowerFindAllContents(CurrentSearchText,i,a.fullSearchLayer);
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
				a.currentDictionary.flowerFindAllContents(CurrentSearchText,a.adapter_idx,a.fullSearchLayer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onCancelled(String String) {
		super.onCancelled();
		harvest();
	}

	@Override
	protected void onPostExecute(String String) {
		super.onPostExecute(String);
		harvest();
	}

	void harvest() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		if(a.timer!=null) { a.timer.cancel(); a.timer=null; }
		if(a.d!=null) a.d.dismiss();

		if(a.isCombinedSearching){
			a.adaptermy4.combining_search_result.invalidate();
		}else{//单独搜索
			a.adaptermy4.combining_search_result.invalidate(a.adapter_idx);
		}
		a.show(R.string.fullfill
				,(System.currentTimeMillis()-CMN.stst)*1.f/1000,a.adaptermy4.getCount());

		a.fullSearchLayer.bakePattern(PDICMainAppOptions.getUseRegex2()?CurrentSearchText:CurrentSearchText.replace("*", ".+?"));
		System.gc();
		a.adaptermy4.ClearVOA();
		a.adaptermy4.notifyDataSetChanged();
		a.mlv2.setSelection(0);
		if(PDICMainAppOptions.getInPageSearchAutoUpdateAfterFulltext())
			a.prepareInPageSearch(a.fullSearchLayer.getBakedPatternStr(), true);
	}

}

package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionarymodels.BookPresenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
		a.fuzzySearchLayer.setCurrentPhrase(CurrentSearchText);

		ArrayList<BookPresenter> md = a.md;

		String SearchTerm = CurrentSearchText;

		if(!PDICMainAppOptions.getJoniCaseSensitive())
			SearchTerm = SearchTerm.toLowerCase();

		if(PDICMainAppOptions.getEnableFanjnConversion())
			a.ensureTSHanziSheet(a.fuzzySearchLayer);

		a.fuzzySearchLayer.flowerSanLieZhi(SearchTerm);

		if(a.isCombinedSearching){
			for(int i=0;i<md.size();i++){
				try {
					BookPresenter mdTmp = md.get(i);
					if(mdTmp==null){
						PlaceHolder phI = a.getPlaceHolderAt(i);
						if(phI!=null) {
							try {
								md.set(i, mdTmp= MainActivityUIBase.new_mdict(phI.getPath(a.opt), a));
								mdTmp.tmpIsFlag = phI.tmpIsFlag;
							} catch (Exception ignored) { }
						}
					}
					publishProgress(i);
					if(mdTmp!=null) // to impl
						((mdict)a.currentDictionary.bookImpl).flowerFindAllKeys(SearchTerm,i,a.fuzzySearchLayer);
					//publisResults();
					if(isCancelled()) break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.gc();
		}else {
			try {
				if(a.checkDicts()){
					publishProgress(a.adapter_idx);
					// to impl
					((mdict)a.currentDictionary.bookImpl).flowerFindAllKeys(SearchTerm,a.adapter_idx,a.fuzzySearchLayer);
				}
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

	public void harvest() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		if(a.timer!=null) a.timer.cancel(); a.timer=null;
		if(a.taskd!=null) a.taskd.dismiss();
		a.mAsyncTask=null;

		a.adaptermy3.combining_search_result.SearchText=CurrentSearchText;
		if(a.isCombinedSearching){
			a.adaptermy3.combining_search_result.invalidate();
		}else{//单独搜索
			a.adaptermy3.combining_search_result.invalidate(a.adapter_idx);
		}

		a.show(R.string.fuzzyfill,(System.currentTimeMillis()-CMN.stst)*1.f/1000
				,a.adaptermy3.getCount());

		CMN.Log((System.currentTimeMillis()-CMN.stst)*1.f/1000, "此即搜索时间。", a.adaptermy3.getCount());

		System.gc();
		a.adaptermy3.notifyDataSetChanged();
		a.mlv1.setSelection(0);
	}
}

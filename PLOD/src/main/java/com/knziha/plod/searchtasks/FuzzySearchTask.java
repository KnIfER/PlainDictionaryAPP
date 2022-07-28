package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;
import android.widget.TextView;

import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.dictionarymodels.resultRecorderScattered;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

import java.lang.ref.WeakReference;

/** Full-scan Search Among Entry Texts */
@SuppressLint("SetTextI18n")
public class FuzzySearchTask extends AsyncTaskWrapper<String, Object, String> {
	private final WeakReference<PDICMainActivity> activity;
	private String CurrentSearchText;
	private BookPresenter TargetBook;

	public FuzzySearchTask(PDICMainActivity a) {
		activity = new WeakReference<>(a);
	}
	@Override
	protected void onPreExecute() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.OnEnterFuzzySearchTask(this);
		TargetBook = a.isCombinedSearching?null:a.currentDictionary;
	}
	
	@Override
	protected void onProgressUpdate(Object... values) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.updateFFSearch((BookPresenter) values[0], (int)values[1]);
	}


	@Override
	protected String doInBackground(String... params) {
		if(params.length==0) return null;
		if((CurrentSearchText=params[0])==null || CurrentSearchText.length()==0) 
			return null;
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		a.fuzzySearchLayer.setCurrentPhrase(CurrentSearchText);
		
		MainActivityUIBase.LoadManager loadManager = a.loadManager;

		String SearchTerm = CurrentSearchText;

		if(!PDICMainAppOptions.getJoniCaseSensitive())
			SearchTerm = SearchTerm.toLowerCase();

		if(PDICMainAppOptions.getEnableFanjnConversion())
			a.ensureTSHanziSheet(a.fuzzySearchLayer);

		a.fuzzySearchLayer.flowerSanLieZhi(SearchTerm);

		if(TargetBook==null){
			for(int i=0;i<loadManager.md_size;i++){
				try {
					BookPresenter mdTmp = loadManager.md_get(i);
					publishProgress(mdTmp, i);
					if(mdTmp!=a.EmptyBook) // to impl
						mdTmp.findAllNames(SearchTerm, mdTmp, a.fuzzySearchLayer);
					//publisResults();
					if(isCancelled()) break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.gc();
		} else {
			try {
				if(a.checkDicts()){
					publishProgress(TargetBook, a.dictPicker.adapter_idx);
					// to impl
					TargetBook.findAllNames(SearchTerm, TargetBook, a.fuzzySearchLayer);
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
		
		resultRecorderDiscrete results = a.adaptermy3.results;
		results.SearchText=CurrentSearchText;
		results.storeRealm = SearchUI.MainApp.ENTRYTEXT;
		results.storeRealm1 = SearchUI.MainApp.表et;
		
		((resultRecorderScattered)results).invalidate(a, TargetBook);

		a.show(R.string.fuzzyfill,(System.currentTimeMillis()-CMN.stst)*1.f/1000
				,a.adaptermy3.getCount());
		
		((TextView)a.viewList[0].getTag()).setText(a.mResource.getString(R.string.fuzzyret2)+" ("+a.adaptermy3.getCount()+")");

		CMN.Log((System.currentTimeMillis()-CMN.stst)*1.f/1000, "此即搜索时间。", a.adaptermy3.getCount());

		System.gc();
		a.adaptermy3.notifyDataSetChanged();
		a.mlv1.setSelection(0);
	}
}

package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;

import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/** Full-scan Search Among Explanations */
@SuppressLint("SetTextI18n")
public class FullSearchTask extends AsyncTaskWrapper<String, Object, String > {
	private final WeakReference<PDICMainActivity> activity;
	private String CurrentSearchText;

	public FullSearchTask(PDICMainActivity a) {
		activity = new WeakReference<>(a);
	}
	@Override
	protected void onPreExecute() {
		try {
			PDICMainActivity a;
			if((a=activity.get())==null) return;
			a.OnEnterFullSearchTask(this);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.updateFFSearch((BookPresenter) values[0], (int)values[1]);
	}

	@Override
	protected String doInBackground(String... params) {
		//CMN.Log("Find In Background??");
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		if(params.length==0) return null;
		if((CurrentSearchText=params[0])==null || CurrentSearchText.length()==0)
			return null;
		a.fullSearchLayer.setCurrentPhrase(CurrentSearchText);

		String SearchTerm = CurrentSearchText;

		if(!PDICMainAppOptions.getJoniCaseSensitive())
			SearchTerm = SearchTerm.toLowerCase();

		if(PDICMainAppOptions.getEnableFanjnConversion())
			a.ensureTSHanziSheet(a.fullSearchLayer);

		a.fullSearchLayer.flowerSanLieZhi(SearchTerm);
		
		MainActivityUIBase.LoadManager loadManager = a.loadManager;
		int size = loadManager.md_size;
		
		if(a.isCombinedSearching){
			for(int i=0;i<size;i++){
				try {
					BookPresenter mdTmp = loadManager.md_get(i);
					publishProgress(mdTmp, i);//_mega
					if(mdTmp!=a.EmptyBook)
						mdTmp.findAllTexts(SearchTerm,i,a.fullSearchLayer);
					//publisResults();
					if(isCancelled()) break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.gc();
		} else {
			try {
				if(a.checkDicts()) {
					publishProgress(a.dictPicker.adapter_idx);
					//CMN.Log("Find In All Conten??");
					a.currentDictionary.findAllTexts(SearchTerm, a.dictPicker.adapter_idx, a.fullSearchLayer);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onCancelled(String String) {
		super.onCancelled();
		harvest(false);
	}

	@Override
	protected void onPostExecute(String String) {
		super.onPostExecute(String);
		harvest(false);
	}

	public void harvest(boolean kill) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		Object currentThreads = a.fullSearchLayer.currentThreads;
		if(kill&&currentThreads!=null){
			//CMN.Log("shutdownNow !!!");
			if(currentThreads instanceof ArrayList)
			for(Thread t:(ArrayList<Thread>)currentThreads){
				t.interrupt();
			}
			else if(currentThreads instanceof ExecutorService){
				((ExecutorService)currentThreads).shutdownNow();
			}
			cancel(true);
		}
		if(a.timer!=null) { a.timer.cancel(); a.timer=null; }
		if(a.taskd!=null) a.taskd.dismiss();
		a.mAsyncTask=null;
		
		resultRecorderDiscrete results = a.adaptermy4.results;
		results.storeRealm = SearchUI.MainApp.FULLTEXT;
		results.storeRealm1 = SearchUI.MainApp.表ft;
		if(a.isCombinedSearching){
			results.invalidate();
		} else {//单独搜索
			results.invalidate(a.dictPicker.adapter_idx);
		}
		a.show(R.string.fullfill
				,(System.currentTimeMillis()-CMN.stst)*1.f/1000,a.adaptermy4.getCount());

		CMN.Log((System.currentTimeMillis()-CMN.stst)*1.f/1000, "此即搜索时间。", a.adaptermy4.getCount());
		
		System.gc();
		a.adaptermy4.ClearVOA();
		a.adaptermy4.notifyDataSetChanged();
		a.mlv2.setSelection(0);
		//准备页内搜索
		if(PDICMainAppOptions.schPageAfterFullSch()){
			a.fullSearchLayer.getBakedPattern();
			a.prepareInPageSearch(a.fullSearchLayer.getPagePattern(), true);
		}
		a.fullSearchLayer.currentThreads=null;
	}

}

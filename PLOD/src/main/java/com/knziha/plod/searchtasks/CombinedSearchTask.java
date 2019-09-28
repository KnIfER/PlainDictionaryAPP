package com.knziha.plod.searchtasks;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;

import com.androidadvance.topsnackbar.TSnackbar;
import com.knziha.plod.PlainDict.PDICMainActivity;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.myCpr;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.rbtree.RBTree_additive;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CombinedSearchTask extends AsyncTask<String, Integer, resultRecorderCombined> {
	private final WeakReference<PDICMainActivity> activity;
	String CurrentSearchText;
	RBTree_additive additive_combining_search_tree = new RBTree_additive();
	
	public CombinedSearchTask(PDICMainActivity a) {
		activity = new WeakReference<>(a);
	}

	@Override
	protected void onPreExecute() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		for(mdict mdTmp:a.md) {
			mdTmp.combining_search_list = new ArrayList<>();
		}
		additive_combining_search_tree.clear();
	}
	
	@Override
	protected resultRecorderCombined doInBackground(String... params) {
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		PDICMainActivity.stst=System.currentTimeMillis();
		CurrentSearchText=params[0];

		a.split_dict_thread_number = a.md.size()<6?1: (a.md.size()/6);
		a.split_dict_thread_number = a.split_dict_thread_number>16?6:a.split_dict_thread_number;
		a.split_dict_thread_number = 2;

		final int thread_number = Runtime.getRuntime().availableProcessors()/2*2-1;Math.min(Runtime.getRuntime().availableProcessors()/2*2+0, a.split_dict_thread_number);

		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(thread_number);
		final int step = a.md.size()/a.split_dict_thread_number;
		final int yuShu= a.md.size()%a.split_dict_thread_number;

		for(int i=0;i<a.split_dict_thread_number;i++){
			if(isCancelled()) break;
			if(a.split_dict_thread_number>thread_number) while (a.poolEUSize>=thread_number) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			final int it=i;
			if(a.split_dict_thread_number>thread_number) a.countDelta(1);

			fixedThreadPool.execute(() -> {
				int jiaX=0;
				if(it==a.split_dict_thread_number-1) jiaX=yuShu;
				for(int i1 = it*step; i1 <it*step+step+jiaX; i1++) {
					if(isCancelled()) break;
					a.md.get(i1).size_confined_lookUp5(CurrentSearchText,null, i1,15);
				}
				if(a.split_dict_thread_number>thread_number) a.countDelta(-1);
			});

			if(isCancelled()) break;
			//long sl = System.currentTimeMillis();
			//Log.e("sadasd",md.get(i)._Dictionary_fName+"time: "+(System.currentTimeMillis()-sl));
		}
		fixedThreadPool.shutdown();
		try {
			fixedThreadPool.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(resultRecorderCombined rec) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		additive_combining_search_tree = new RBTree_additive();
		for(int i=0; i<a.md.size(); i++) {
			for(myCpr<String, Integer> dataI:a.md.get(i).combining_search_list) {
				additive_combining_search_tree.insert(dataI.key, i, dataI.value);
			}
		}
		rec =  new resultRecorderCombined(a,additive_combining_search_tree.flatten(),a.md);

		//CMN.show("联合搜索 时间： "+(System.currentTimeMillis()-stst)+"ms "+rec.size());

		if(a.lv2.getVisibility()!= View.VISIBLE)
			a.lv2.setVisibility(View.VISIBLE);
		a.adaptermy2.combining_search_result = rec;
		a.adaptermy2.notifyDataSetChanged();
		a.lv2.setSelection(0);
		//showT(""+bWantsSelection);
		ViewGroup sv;
		float fval = 0.5f;
		if(a.bIsFirstLaunch||a.bWantsSelection) {
			if(mdict.processText(rec.getResAt(0)).equals(mdict.processText(CurrentSearchText))) {
				boolean proceed = true;
				if(a.contentview.getParent()==a.main) {
					proceed = (a.adaptermy2.currentKeyText == null || !mdict.processText(CurrentSearchText).equals(mdict.processText(a.adaptermy2.currentKeyText)));
				}
				if(proceed)
					a.lv2.performItemClick(a.adaptermy2.getView(0, null, null), 0, a.lv2.getItemIdAtPosition(0));
			}
			sv=a.contentview;
			fval=.8f;
		}else {
			sv=a.main_succinct;
		}
		a.bIsFirstLaunch=false;

		if(a.opt.getNotifyComboRes()) {
			a.snack = TSnackbar.makeraw(sv, a.getResources().getString(R.string.cbflowersnstr,a.opt.lastMdPlanName,a.md.size(),rec.size()),TSnackbar.LENGTH_LONG);
			a.snack.getView().setAlpha(fval);
			a.snack.show();
		}
	}
}

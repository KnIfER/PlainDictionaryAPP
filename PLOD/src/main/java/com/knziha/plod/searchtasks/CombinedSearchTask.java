package com.knziha.plod.searchtasks;

import android.view.View;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.SearchType;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.rbtree.RBTree_additive;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Batch Binary Search Task */
public class CombinedSearchTask extends AsyncTaskWrapper<String, Object, resultRecorderCombined> {
	private final WeakReference<MainActivityUIBase> activity;
	private RBTree_additive _treeBuilder = new RBTree_additive();
	String CurrentSearchText;
	String CurrentSearchText2;

	public CombinedSearchTask(MainActivityUIBase a) {
		activity = new WeakReference<>(a);
	}

	@Override
	protected void onPreExecute() {
		//CMN.Log("开始联合搜索！");
		MainActivityUIBase a;
		if((a=activity.get())==null) return;
		BookPresenter bookPresenter;
		for(int i=0; i<a.loadManager.md_size; i++) {
			bookPresenter = a.loadManager.md_getAt(i);
			if (bookPresenter != null) {
				bookPresenter.range_query_reveiver = new ArrayList<>();
			}
		}
		_treeBuilder.clear();
		if (BuildConfig.DEBUG) {
			CMN.stst = CMN.now();
		}
	}
	
	@Override
	protected resultRecorderCombined doInBackground(String... params) {
		MainActivityUIBase a;
		if((a=activity.get())==null) return null;
		String searchText = params[0].trim();
		int paragraphWords = 9;
		boolean isParagraph = BookPresenter.testIsParagraph(searchText, paragraphWords);
		//CMN.debug("isParagraph::", isParagraph);
		CurrentSearchText=searchText;
		CurrentSearchText2=PDICMainAppOptions.getSearchUseMorphology()?
				a.ReRouteKey(searchText, true):null;
		
		MainActivityUIBase.LoadManager loadManager = a.loadManager;
		int size = loadManager.md_size;
		
		a.split_dict_thread_number = size<6?1: (size/6);
		a.split_dict_thread_number = a.split_dict_thread_number>16?6:a.split_dict_thread_number;
		a.split_dict_thread_number = 2;

		final int thread_number = Runtime.getRuntime().availableProcessors()/2*2-1;Math.min(Runtime.getRuntime().availableProcessors()/2*2+0, a.split_dict_thread_number);

		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(thread_number);
		final int step = size/a.split_dict_thread_number;
		final int yuShu= size%a.split_dict_thread_number;

		a.poolEUSize.set(0);

		for(int i=0;i<a.split_dict_thread_number;i++){
			if(isCancelled()) break;
			if(a.split_dict_thread_number>thread_number) while (a.poolEUSize.get()>=thread_number) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			final int it=i;
			if(a.split_dict_thread_number>thread_number) a.poolEUSize.addAndGet(1);

			fixedThreadPool.execute(() -> {
				try {
					int jiaX=0;
					if(it==a.split_dict_thread_number-1) jiaX=yuShu;
					for(int i1 = it*step; i1 <it*step+step+jiaX; i1++) {
						if(isCancelled()) break;
						BookPresenter bookPresenter = loadManager.md_get(i1);
						if(bookPresenter!=a.EmptyBook) {
							try {
								bookPresenter.QueryByKey(CurrentSearchText, SearchType.Range, isParagraph, paragraphWords, running);
							} catch (Exception e) {
								if(GlobalOptions.debug)
									CMN.Log("搜索出错！！！", bookPresenter.bookImpl.getDictionaryName(), e);
							}
						}
					}
					if(a.split_dict_thread_number>thread_number) a.poolEUSize.addAndGet(-1);
				} catch (Exception e){
					if(GlobalOptions.debug)
						CMN.Log("搜索出错！！！",e);
				}
			});

			if(isCancelled()) break;
		}
		fixedThreadPool.shutdown();
		try {
			fixedThreadPool.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			if(GlobalOptions.debug) CMN.Log(e);
		}
		
		_treeBuilder = new RBTree_additive();
		// 替换key值为searchText近似值
		_treeBuilder.setKeyClashHandler(searchText);
		ArrayList<myCpr<String, Long>> combining_search_list;
		BookPresenter bookPresenter;
		long bid;
		HashSet<Long> booksSet = new HashSet<>(size);
		for(int i=0; i<size; i++) {
			bookPresenter = loadManager.md_getAt(i);
			if(bookPresenter!=null){ // to impl
				if(isCancelled()) break;
				bid = bookPresenter.bookImpl.getBooKID();
				_treeBuilder.resetRealmer(bid);
				combining_search_list = bookPresenter.range_query_reveiver;
				boolean adding = true; //todo
				if(combining_search_list!=null)
				{
					try {
						for (int j = 0; j < combining_search_list.size(); j++) {
							myCpr<String, Long> dataI = combining_search_list.get(j);
							if(dataI!=null) { // to check
								if(adding){
									booksSet.add(bid);
									adding=false;
								}
								_treeBuilder.insert(dataI.key, bid, dataI.value);
							}
						}
					} catch (Exception ignored) { }
				}
			}
		}
		if(isCancelled()) return null;
		resultRecorderCombined rec = new resultRecorderCombined(a, _treeBuilder.flatten(), CurrentSearchText);
		rec.booksSet = booksSet;
		if(rec.FindFirstIdx(searchText, running)) return rec;
		return null;
	}

	@Override
	protected void onPostExecute(resultRecorderCombined rec) {
		MainActivityUIBase a;
		if((a=activity.get())==null) return;

//		CMN.debug("联合搜索 时间： " + (System.currentTimeMillis() - CMN.stst) + " ms " + rec.size());
		if(rec==null) rec = new resultRecorderCombined(a, new ArrayList<>(), CurrentSearchText);
		rec.storeRealm = a.schuiMain;
		rec.storeRealm1 = a.thisActType==MainActivityUIBase.ActType.PlainDict?SearchUI.MainApp.表1: SearchUI.FloatSch.表1;
		if(a.lv2.getVisibility()!= View.VISIBLE)
			a.lv2.setVisibility(View.VISIBLE);
		a.adaptermy2.results = a.recCom = rec;
		a.adaptermy2.notifyDataSetChanged();
		a.lv2.setSelection(0);
		//showT(""+bWantsSelection);

		if(a.bIsFirstLaunch||a.bWantsSelection) {
			if(mdict.processText(rec.getResAt(a, 0)).equals(mdict.processText(CurrentSearchText))) {
				boolean proceed = true;
				if(a.contentview.getParent()==a.main) {
					proceed = (a.adaptermy2.currentKeyText == null || !mdict.processText(CurrentSearchText).equals(mdict.processText(a.adaptermy2.currentKeyText)));
				}
				if(proceed){
					a.bRequestedCleanSearch=a.bIsFirstLaunch;
					/* 接管历史纪录 */
					a.adaptermy2.onItemClick(null, null, 0, 0);
				}
			}
		}

		a.NotifyComboRes(rec.size());
		a.bIsFirstLaunch=false;

		if(a.pendingLv2Pos!=null)
			a.restoreLv2States();

		a.CombinedSearchTask_lastKey=CurrentSearchText;
		
		a.dictPicker.setUnderLined(rec.booksSet);
	}
}

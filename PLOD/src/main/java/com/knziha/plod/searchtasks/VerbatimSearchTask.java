package com.knziha.plod.searchtasks;

import android.view.View;
import android.view.ViewGroup;

import com.knziha.plod.PlainUI.WordPopup;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PlainMdict;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.rbtree.RBTree_additive;
import com.knziha.rbtree.additiveMyCpr1;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class VerbatimSearchTask extends AsyncTaskWrapper<String, Object, resultRecorderCombined> {
	public static final String RegExp_VerbatimDelimiter = "[ `~!@#$%^&*()+=—|{}\":;.,\\[\\]<>/?！￥…（）【】‘；：”“’。，、？|-·]|((?<=[\\u4e00-\\u9fa5])|(?=[\\u4e00-\\u9fa5]))";
	public static final Pattern Pattern_VerbatimDelimiter = Pattern.compile(RegExp_VerbatimDelimiter);
	private final WeakReference<PDICMainActivity> activity;
	String CurrentSearchText;
	private final boolean isStrict;
	ArrayList<additiveMyCpr1> resultList = new ArrayList<>();
	int verbatimCount;

	public VerbatimSearchTask(PDICMainActivity a, boolean _isStrict) {
		activity = new WeakReference<>(a);
		isStrict=_isStrict;
	}

	@Override
	protected void onPreExecute() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.contentUIData.mainProgressBar.setVisibility(View.VISIBLE);
		verbatimCount=0;
		if(!isStrict) {
			for(BookPresenter bookPresenter:a.md) {
				if(bookPresenter!=null) // to impl
					bookPresenter.range_query_reveiver = new ArrayList<>();
			}
		}
	}


	@Override
	protected resultRecorderCombined doInBackground(String...params) {
		try {
			PDICMainActivity a = activity.get();
			if(a==null) return null;
			CMN.stst = CMN.now();
			CurrentSearchText=params[0];
			String[] arrKeysToSch = CurrentSearchText.split(RegExp_VerbatimDelimiter);
			
			MainActivityUIBase.LoadManager loadManager = a.loadManager;
			for(int i=0; i<arrKeysToSch.length; i++) {
				if("".equals(arrKeysToSch[i])) continue;
				verbatimCount++;
				boolean created = false;
				String proKey = null;
				boolean batchSch = a.isCombinedSearching;
				
				boolean use_morph = true;
				WordPopup wordPopup = a.wordPopup;
				ArrayList<UniversalDictionaryInterface> forms = wordPopup.forms;
				if(use_morph ^ forms.contains(wordPopup.queryMorphs)) {
					if(use_morph) forms.add(wordPopup.queryMorphs);
					else forms.remove(wordPopup.queryMorphs);
				}
				if(use_morph) {
					wordPopup.queryMorphs.loadMan = a.loadManager;
					wordPopup.queryMorphs.rejected.clear(); // todo version control loadMan
				}
				
				for(int j=0; j<loadManager.md_size; j++) {
					BookPresenter mdTmp = batchSch?loadManager.md_get(j):a.currentDictionary;
					if(mdTmp!=a.EmptyBook) {
						if(isStrict) {
							int result = mdTmp.bookImpl.lookUp(arrKeysToSch[i], true, forms);
							if(result>=0) {
								additiveMyCpr1 wordRes;
								if(!created) {
									resultList.add(wordRes=new additiveMyCpr1(arrKeysToSch[i], new ArrayList<>()));
									proKey = PlainMdict.processText(arrKeysToSch[i]);
									created=true;
								} else {
									wordRes = resultList.get(resultList.size()-1);;
								}
								ArrayList<Long> arr = (ArrayList<Long>) wordRes.value;
								wordRes.realmCount++;
								arr.add(mdTmp.getId()); arr.add((long) result);
								result++;
								while(result<mdTmp.bookImpl.getNumberEntries() && proKey.equals(PlainMdict.processText(mdTmp.bookImpl.getEntryAt(result)))) {
									arr.add(mdTmp.getId()); arr.add((long) result);
									result++;
								}
							}
						} else {
							if(isCancelled()) break; // to impl
							mdTmp.bookImpl.lookUpRange(arrKeysToSch[i], mdTmp.range_query_reveiver, null,i,15, null, false);
						}
					}
					if (!batchSch) {
						break;
					}
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(resultRecorderCombined rec) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		MainActivityUIBase.LoadManager loadManager = a.loadManager;
		if(!isStrict) {
			RBTree_additive additive_combining_search_tree_haha = new RBTree_additive();
			for(int i=0; i<loadManager.md_size; i++) {
				BookPresenter book = loadManager.md_getAt(i);
				if (book!=null) {
					for(myCpr<String, Long> dataI:book.range_query_reveiver) {
						additive_combining_search_tree_haha.insert(dataI.key, i, dataI.value);
					}
				}
			}
			resultList =additive_combining_search_tree_haha.flatten();
		}
		rec = new resultRecorderCombined(a, resultList, CurrentSearchText);
		//CMN.debug("逐字搜索 时间： "+(CMN.now()-CMN.stst)+"ms "+rec.size());

		a.contentUIData.mainProgressBar.setVisibility(View.GONE);

		if(a.lv2.getVisibility()!=View.VISIBLE)
			a.lv2.setVisibility(View.VISIBLE);
		a.adaptermy2.results = rec;
		a.adaptermy2.notifyDataSetChanged();
		a.lv2.setSelection(0);

		ViewGroup sv;
		float fval = 0.5f;
		if(a.contentview.getParent()==a.main) {
			sv=a.contentview;
			fval=.8f;
		}else {
			sv=a.mainframe;
		}

		if(a.opt.getNotifyComboRes()) {
			//a.snack = TSnackbar.makeraw(sv, a.getResources().getString(R.string.vbflowersnstr,verbatimCount,rec.size()),TSnackbar.LENGTH_LONG);
			//a.snack.getView().setAlpha(fval);
			//a.snack.show();
		}

	}
}

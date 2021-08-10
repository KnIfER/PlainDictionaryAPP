package com.knziha.plod.searchtasks;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.rbtree.RBTree_additive;
import com.knziha.rbtree.additiveMyCpr1;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class VerbatimSearchTask extends AsyncTask<String, Integer, resultRecorderCombined> {
	public static final String RegExp_VerbatimDelimiter = "[ `~!@#$%^&*()+=—|{}\":;.,\\[\\]<>/?！￥…（）【】‘；：”“’。，、？|-·]|((?<=[\\u4e00-\\u9fa5])|(?=[\\u4e00-\\u9fa5]))";
	public static final Pattern Pattern_VerbatimDelimiter = Pattern.compile(RegExp_VerbatimDelimiter);
	private final WeakReference<PDICMainActivity> activity;
	String CurrentSearchText;
	private final boolean isStrict;
	ArrayList<additiveMyCpr1> additive_combining_search_tree = new ArrayList<>();
	int verbatimCount;

	public VerbatimSearchTask(PDICMainActivity a, boolean _isStrict) {
		activity = new WeakReference<>(a);
		isStrict=_isStrict;
	}

	@Override
	protected void onPreExecute() {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.main_progress_bar.setVisibility(View.VISIBLE);
		verbatimCount=0;
		if(!isStrict) {
			for(BookPresenter mdTmp:a.md) {
				if(mdTmp!=null)
					mdTmp.combining_search_list = new ArrayList<>();
			}
		}
	}


	@Override
	protected resultRecorderCombined doInBackground(String...params) {
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		CMN.stst=System.currentTimeMillis();
		CurrentSearchText=params[0];
		String[] inputArray = CurrentSearchText.split(RegExp_VerbatimDelimiter);

		ArrayList<BookPresenter> md = a.md;

		for(int i=0; i<inputArray.length; i++) {
			if("".equals(inputArray[i])) continue;
			verbatimCount++;
			boolean created = false;
			int start=0;
			int end = md.size();
			if(!a.isCombinedSearching) {
				if(a.checkDicts()){
					start = md.indexOf(a.currentDictionary);
					end = start+1;
				}
			}
			for(int j=start; j<end; j++) {
				BookPresenter mdTmp = md.get(j);
				if(mdTmp==null){
					PlaceHolder phI = a.getPlaceHolderAt(j);
					if(phI!=null) {
						try {
							md.set(j, mdTmp= MainActivityUIBase.new_mdict(phI.getPath(a.opt), a));
							mdTmp.tmpIsFlag = phI.tmpIsFlag;
							mdTmp.combining_search_list = new ArrayList<>();
						} catch (Exception ignored) { }
					}
				}
				if(mdTmp!=null){
					if(isStrict) {
						int result = mdTmp.lookUp(inputArray[i], true);
						if(result>=0) {
							if(!created) {
								ArrayList<Integer> arr = new ArrayList<>();
								arr.add(j);arr.add(result);
								additive_combining_search_tree.add(new additiveMyCpr1(inputArray[i],arr));
								created=true;
							}else {
								ArrayList<Integer> arr = (ArrayList<Integer>) additive_combining_search_tree.get(additive_combining_search_tree.size()-1).value;
								arr.add(j);arr.add(result);
							}
						}
					}else {
						if(isCancelled()) break;
						mdTmp.size_confined_lookUp5(inputArray[i],null,i,15);
					}
				}
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(resultRecorderCombined rec) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		ArrayList<BookPresenter> md = a.md;
		if(!isStrict) {
			RBTree_additive additive_combining_search_tree_haha = new RBTree_additive();
			for(int i=0; i<md.size(); i++) {
				if(md.get(i)!=null)
				for(myCpr<String, Integer> dataI:md.get(i).combining_search_list) {
					additive_combining_search_tree_haha.insert(dataI.key, i, dataI.value);
				}
			}
			additive_combining_search_tree=additive_combining_search_tree_haha.flatten();
		}
		rec = new resultRecorderCombined(a,additive_combining_search_tree,md);
		//CMN.show("逐字搜索 时间： "+(System.currentTimeMillis()-stst)+"ms "+rec.size());

		a.main_progress_bar.setVisibility(View.GONE);

		if(a.lv2.getVisibility()!=View.VISIBLE)
			a.lv2.setVisibility(View.VISIBLE);
		a.adaptermy2.combining_search_result = rec;
		a.adaptermy2.notifyDataSetChanged();
		a.lv2.setSelection(0);

		ViewGroup sv;
		float fval = 0.5f;
		if(a.contentview.getParent()==a.main) {
			sv=a.contentview;
			fval=.8f;
		}else {
			sv=a.main_succinct;
		}

		if(a.opt.getNotifyComboRes()) {
			//a.snack = TSnackbar.makeraw(sv, a.getResources().getString(R.string.vbflowersnstr,verbatimCount,rec.size()),TSnackbar.LENGTH_LONG);
			//a.snack.getView().setAlpha(fval);
			//a.snack.show();
		}

	}
}

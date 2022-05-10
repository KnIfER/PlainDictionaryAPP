package com.knziha.plod.searchtasks;

import android.annotation.SuppressLint;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PlaceHolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

@SuppressLint("SetTextI18n")
public class BuildIndexTask extends AsyncTaskWrapper<String, Object, String > {
	private final WeakReference<PDICMainActivity> activity;
	private final ArrayList<Long> IndexingBooks;

	public BuildIndexTask(PDICMainActivity a, ArrayList<Long> indexingBooks) {
		activity = new WeakReference<>(a);
		IndexingBooks = indexingBooks;
	}
	@Override
	protected void onPreExecute() {
		try {
			PDICMainActivity a;
			if((a=activity.get())==null) return;
			a.OnEnterBuildIndexTask(this);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		PDICMainActivity a;
		if((a=activity.get())==null) return;
		a.updateIndexBuilding((int)values[0], (int)values[1]);
	}

	@Override
	protected String doInBackground(String... params) {
		//CMN.Log("Find In Background??");
		PDICMainActivity a;
		if((a=activity.get())==null) return null;
		if(params.length==0) return null;

		ArrayList<BookPresenter> md = a.md;
		synchronized (IndexingBooks) {
			ArrayList<Integer> done = new ArrayList<>();
			for (int i = 0,len=IndexingBooks.size(); i < len; i++) {
				long value = IndexingBooks.get(i);
				int position = (int) value;
				boolean selected = value>>32==1;
				if(selected) {
					BookPresenter mdTmp = md.get(position);
					if(mdTmp==null){
						PlaceHolder phI = a.getPlaceHolderAt(position);
						if(phI!=null) {
							publishProgress(position, i);
							try {
								md.set(position, MainActivityUIBase.new_book(phI, a));
								done.add(i);
							} catch (Exception ignored) { }
						}
					}
					if(isCancelled()) break;
				}
				System.gc();
			}
			for (int i = done.size()-1; i>=0; i--) {
				IndexingBooks.remove((int)done.get(i));
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

		System.gc();
		a.fullSearchLayer.currentThreads=null;
		
		a.switch_Dict(a.dictPicker.adapter_idx, true, false, null);
	}

}

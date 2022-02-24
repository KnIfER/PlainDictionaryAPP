package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.knziha.ankislicer.customviews.ArrayAdaptermy;
import com.knziha.ankislicer.customviews.WahahaTextView;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.databinding.DeckBrowserBinding;
import com.knziha.plod.widgets.RecyclerViewmy;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTNode;
import com.knziha.rbtree.RashSet;
import com.knziha.rbtree.additiveMyCpr1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import com.knziha.plod.db.LexicalDBHelper;

import static com.knziha.plod.db.LexicalDBHelper.TABLE_HISTORY_v2;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;
import static com.knziha.plod.plaindict.MainActivityUIBase.ActType;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_FAVORITE_v2;
import static com.knziha.plod.plaindict.DeckListAdapter.*;

@SuppressLint("SetTextI18n")
public class DBroswer extends Fragment implements
		View.OnClickListener, OnLongClickListener, RecyclerViewmy.OnItemClickListener, OnItemLongClickListener {
	public int pendingDBClickPos=-1;
	public int type;
	public int pendingType;
	ViewGroup webviewHolder;
	protected boolean initialized;
	private boolean isDarkStamp;
	private boolean bIsCombinedSearch;
	DeckBrowserBinding UIData;
	WebViewListHandler webViewListHandler;
	
	SparseArray<Long> lastVisiblePositionMap = new SparseArray<>();
	
	RecyclerView lv;
	int lastDragPos=-1;
	
	DeckListAdapter mAdapter;
	ImageView pageAsyncLoader;
	Date date = new Date();

	protected PDICMainAppOptions opt;

	public String currentDisplaying = "";
	public int currentPos=-1;
	public long currentRowId=-1;
	
	SparseArray<String> toDelete = new SparseArray<>();
	HashSet<Long> toDeleteV2 = new HashSet<>();
	HashSet<Long> Selection = new HashSet<>();

	boolean isToDel = false;

	LinearLayoutManager lm;

	InputMethodManager imm;
	private int MainAppBackground;
	
	public int try_goBack(){
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null || !initialized) return 0;
		if(a.isContentViewAttachedForDB()) {
			a.DetachContentView(true);
			SparseArray<String> deleting = this.toDelete;
			Long[] deletingV2 = this.toDeleteV2.toArray(new Long[this.toDeleteV2.size()]);
			if(!isToDel || deletingV2.length==0) return 1;

			//删除收藏 to impl
			SQLiteDatabase db = mLexiDB.getDB();
			{
				String sql = "delete from "+TABLE_FAVORITE_v2+" where id = ? ";
				SQLiteStatement preparedDeleteExecutor = db.compileStatement(sql);
				db.beginTransaction();  //开启事务
				int count = 0;
				int toDelete_size = deletingV2.length;
				try {
					for(Long rowId:deletingV2) {//delete
						preparedDeleteExecutor.bindLong(1, rowId);
						if(preparedDeleteExecutor.executeUpdateDelete()!=-1) {
							count++;
						}
						this.toDeleteV2.remove(rowId);
					}
					preparedDeleteExecutor.close();
					Selection.clear();
					db.setTransactionSuccessful();  //控制回滚
				} catch (Exception e) {
					CMN.Log(e);
				} finally {
					db.endTransaction();  //事务提交
					mAdapter.rebuildCursor(a);
					show(R.string.maniDel,count,toDelete_size);
				}
			}
			return 1;
		}
		if(sharedPopup!=null && sharedPopup.isShowing()) {
			sharedPopup.dismiss();
			return 1;
		}
		if(inSearch) {
			UIData.search.performClick();
			return 1;
		}
		if(Selection.size()>0) {//SelectionMode==SelectionMode_select
			Selection.clear();
			notifyDataSetChanged();
			UIData.counter.setText(Selection.size()+"/"+ getItemCount());
			if(SelectionMode==SelectionMode_select) {
				int taregtID=0;
				switch(lastFallBackTarget) {
					case SelectionMode_peruseview:
						taregtID=R.id.tools001;
					break;
					case SelectionMode_txtdropper:
						taregtID=R.id.toolbar_action2;
					break;
					case SelectionMode_pan:
						taregtID=R.id.tools0;
					break;
				}
				if(taregtID!=0) {// taregtID=R.id.tools0;
					View target = UIData.getRoot().findViewById(taregtID);
					target.setTag(false);
					target.performClick();
				}
			}else {
				UIData.counter.setText(Selection.size()+"/"+ getItemCount());
			}
			return 1;
		}
		return 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(UIData==null) {
			UIData = DeckBrowserBinding.inflate(inflater, container, false);
			//CMN.Log("onCreateView!!!");
			lv = UIData.mainList;
			UIData.fastScroller.setRecyclerView(lv);
			lv.addOnScrollListener(UIData.fastScroller.getOnScrollListener());
			lv.setLayoutManager(lm = new LinearLayoutManager(inflater.getContext()));
			
			ViewUtils.setOnClickListenersOneDepth(UIData.sideBar, this, 2, 0, null);
			
			UIData.toolbarAction1.setColorFilter(GlobalOptions.BLACK);
			
			UIData.browserWidget15.setOnClickListener(ViewUtils.DummyOnClick);
			UIData.browserWidget14.setOnClickListener(this);
			UIData.browserWidget13.setOnClickListener(this);
			
			UIData.toolbar.inflateMenu(R.xml.menu_dbrowser);
			
			UIData.toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
			UIData.toolbar.setNavigationOnClickListener(v1 -> {
			
			});
			
			if (container.getContext() instanceof MainActivityUIBase) {
//			toolbar.setBackgroundColor(0xcc000000|(((MainActivityUIBase) container.getContext()).MainBackground&0xffffff));
			}
			//CMN.Log("咿呀咿呀", container.getContext());
			Menu toolbarmenu = UIData.toolbar.getMenu();
			MenuItem searchItem = toolbarmenu.getItem(0);
			//  new SearchCardsHandler2().execute(query);
			
			UIData.counter.setVisibility(View.GONE);
			newStart = true;
		}
		return UIData.getRoot();
	}
	boolean newStart;
	long last_listHolder_tt;

	int lastFirst = 0;
	LexicalDBHelper mLexiDB;
	
	@Override
	public void onDetach(){
		super.onDetach();
		//CMN.Log("on browser detach", cr!=null && lm.findFirstVisibleItemPosition()>=1 && mCards_size>=0);
		View ca = lv.getChildAt(0);
		if (ca!=null) {
			ViewUtils.ViewDataHolder holder = (ViewUtils.ViewDataHolder) ca.getTag();
			DeckListAdapter.HistoryDatabaseReader reader = (DeckListAdapter.HistoryDatabaseReader) holder.tag;
			lastVisiblePositionMap.append(getFragmentType(), reader.sort_number);
//			a.showT(new Date(last_visible_entry_time).toLocaleString());
		}
	}
	
	protected void loadInAll(MainActivityUIBase a) {
		CMN.Log("FAV load in all!!!", initialized, type, getTableName());
		if(initialized) {
			if(mAdapter!=null && mAdapter.resetDataCache(type)) {
				mAdapter.notifyDataSetChanged();
//			lv.postDelayed(mAdapter::notifyDataSetChanged, 180);
			}
			mLexiDB = a.prepareHistoryCon();
			mAdapter.rebuildCursor(a);
			
			String foldername;
			if(type==DB_FAVORITE) {
				long fid = a.opt.getCurrFavoriteNoteBookId();
				mAdapter.data.fid = fid;
				mAdapter.data.ver = mLexiDB.getDBVersion(fid);
				foldername = mLexiDB.getFavoriteNoteBookNameById(fid);
			} else {
				mAdapter.data.ver = mLexiDB.getDBVersion(mAdapter.data.fid = -1L);
				foldername = "历史记录";
			}
			setTitle(foldername);
			//show(type==DB_FAVORITE?R.string.maniFavor:R.string.maniFavor2, foldername, getItemCount());
			//progressBar.setVisibility(View.GONE);
		}
	}
	
	private void setTitle(String title) {
		UIData.toolbar.setTitle(title);
		UIData.smallLabel.setText(title);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(!initialized) {
			webViewListHandler = new WebViewListHandler(a);
			DeckListAdapter adapter = new DeckListAdapter(a, this);
			lv.setAdapter(adapter);
			RecyclerView.RecycledViewPool pool = lv.getRecycledViewPool();
			pool.setMaxRecycledViews(0,10);
			for(int index =0;index < 10;index++) {
				pool.putRecycledView(adapter.createViewHolder(lv,0));
			}
			adapter.setOnItemClickListener(this);
			adapter.setOnItemLongClickListener(this);
			mAdapter = adapter;
			if(pendingType!=0) {
				setType(a, pendingType, false);
				pendingType=0;
			}
			
			mLexiDB = a.prepareHistoryCon();
			opt = a.opt;
			imm = a.imm;
			initialized = true;
			
			//取消更新item时闪烁
			RecyclerView.ItemAnimator anima = lv.getItemAnimator();
			if(anima instanceof DefaultItemAnimator)
				((DefaultItemAnimator)anima).setSupportsChangeAnimations(false);
			anima.setChangeDuration(0);
			anima.setAddDuration(0);
			anima.setMoveDuration(0);
			anima.setRemoveDuration(0);
			
			UIData.fastScroller.setConservativeScroll(opt.getShelfStrictScroll());
			bIsCombinedSearch = opt.getIsCombinedSearching();
			UIData.toolbarAction1.setActivated(bIsCombinedSearch);
			
			if(opt.getScrollShown()) {
				UIData.tg2.setChecked(true);
				UIData.fastScroller.setVisibility(View.GONE);
			}
			
			//WahahaTextView.mR=UIData.root;
			loadInAll(a);
			checkColors();
			
			SelectionMode = opt.getDBMode();
			UIData.getRoot().post(() -> UIData.sideBar.setRbyPos(opt.getDBMode()));
			if(opt.getDBMode()==3 && opt.getInRemoveMode()) {
				UIData.sideBar.setSCC(opt.getInRemoveMode()?getResources().getColor(R.color.ShallowHeaderBlue):UIData.sideBar.ShelfDefaultGray);
			}
			if(pendingDBClickPos!=-1){
				onItemClick(null, pendingDBClickPos);
				pendingDBClickPos=-1;
			}
		}
		else {
			boolean bNeedInvalidate=false;
			if(mAdapter!=null && mAdapter.resetDataCache(type)) {
				bNeedInvalidate = true;
			}
			boolean pull = mLexiDB==null || mAdapter.data.type==0
					|| getAutoRefreshOnAttach() && (
						type==DB_FAVORITE && mAdapter.data.fid!=a.opt.getCurrFavoriteNoteBookId()
							|| mAdapter.data.ver!=mLexiDB.getDBVersion(mAdapter.data.fid)
						);
			if(pull) {
				loadInAll(a);
				bNeedInvalidate = false;
			} else {
				if (toastV!=null && toastV.getVisibility()==View.VISIBLE) {
					maskOn = true;
					toastTv.startAnimation(fadeAnima);
				}
			}
			if(bNeedInvalidate) {
				//lv.postDelayed(mAdapter::notifyDataSetChanged, 150);
				mAdapter.notifyDataSetChanged();
				String foldername;
				if(type==DB_FAVORITE) {
					foldername = mLexiDB.getFavoriteNoteBookNameById(mAdapter.data.fid);
				} else {
					foldername = "历史记录";
				}
				setTitle(foldername);
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		checkColors();
	}
	
	public void checkColors() {
		if(initialized && GlobalOptions.isDark!=isDarkStamp) {
			CMN.Log("dbr_checkColor...");
			ColorFilter cs_dbr_sidbr = null;
			isDarkStamp = GlobalOptions.isDark;
			int AppWhite = Color.WHITE;
			if(isDarkStamp) {
				AppWhite = Color.BLACK;
				cs_dbr_sidbr = GlobalOptions.WHITE;
			}
			UIData.root.setBackgroundColor(AppWhite);
			AppWhite=isDarkStamp?0xff2b4381:Color.WHITE;
			UIData.counter.setTextColor(AppWhite);
			UIData.smallLabel.setTextColor(AppWhite);
			//UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray=0xFF4F7FDF);
			
			for(int i=0;i<UIData.sideBar.getChildCount();i++) {
				View cI = UIData.sideBar.getChildAt(i);
				Drawable bg = cI.getBackground();
				if(bg!=null) {
					bg.setColorFilter(cs_dbr_sidbr);
				}
				if(cI==UIData.toolbarAction1 && cs_dbr_sidbr==null) {
					UIData.toolbarAction1.setColorFilter(GlobalOptions.BLACK);
				} else if(cI instanceof ImageView) {
					((ImageView)cI).setColorFilter(cs_dbr_sidbr);
				}
			}
			if(initialized) {
				notifyDataSetChanged();
			}
		}
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a!=null && a.MainAppBackground!= MainAppBackground) {
			MainAppBackground =a.MainAppBackground;
			UIData.toolbar.setBackgroundColor(MainAppBackground);
			UIData.bottombar.setBackgroundColor(MainAppBackground);
		}
	}
	
	protected boolean getAutoRefreshOnAttach() {
		return true;
	}

	@Deprecated
	public void moveSelectedardsToDataBase(LexicalDBHelper toDB) {
		if(Selection.size()==0) {
			show(R.string.noseletion);
			return;
		}
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null) return;
		new AlertDialog.Builder(a)
			.setMessage(getResources().getString(R.string.warn_move, Selection.size(),CMN.unwrapDatabaseName(a.favoriteCon.DATABASE),CMN.unwrapDatabaseName(toDB.DATABASE)))
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
				//final long[] ids = new long[l.size()];
				String sql = "delete from t1 where lex = ? ";
				SQLiteStatement preparedDeleteExecutor = mLexiDB.getDB().compileStatement(sql);
				mLexiDB.getDB().beginTransaction();  //开启事务
				String sql2 = "insert into t1(lex, date) values(?,?)";
				SQLiteStatement preparedInsertExecutor = toDB.getDB().compileStatement(sql2);
				toDB.getDB().beginTransaction();  //开启事务
				try {
					for(Long position1 :Selection) {//移动
						HistoryDatabaseReader reader = mAdapter.getReaderAt((int)(long)position1);
						preparedInsertExecutor.bindString(1, reader.record);
						preparedInsertExecutor.bindLong(2, reader.sort_number);
						if(preparedInsertExecutor.executeInsert()!=-1) {
							preparedDeleteExecutor.bindString(1, reader.record);
							preparedDeleteExecutor.executeUpdateDelete();
						}
					}
					Selection.clear();
					preparedDeleteExecutor.close();
					preparedInsertExecutor.close();
					mLexiDB.getDB().setTransactionSuccessful();  //控制回滚
					toDB.getDB().setTransactionSuccessful();  //控制回滚
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mLexiDB.getDB().endTransaction();  //事务提交
					toDB.getDB().endTransaction();  //事务提交
				}
				mAdapter.rebuildCursor(a);
				UIData.counter.setText(0 +"/"+ getItemCount());
			}).show();
	}
	
	public void moveSelectedCardsToFolder(String name, long nid) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(Selection.size()==0) {
			show(R.string.noseletion);
			return;
		}
		if(a==null) return;
		new AlertDialog.Builder(a)
			.setMessage(getResources().getString(R.string.warn_move, Selection.size()
					, mLexiDB.getFavoriteNoteBookNameById(a.opt.getCurrFavoriteNoteBookId())
					, name))
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
				String sql = "UPDATE "+TABLE_FAVORITE_v2+" SET folder=? where id=?";
				SQLiteDatabase database = mLexiDB.getDB();
				SQLiteStatement preparedMoveExecutor = database.compileStatement(sql);
				preparedMoveExecutor.bindLong(1, nid);
				database.beginTransaction();  //开启事务
				try {
					for(Long rowId : Selection) {//移动
						preparedMoveExecutor.bindLong(2, rowId);
						preparedMoveExecutor.execute();
					}
					Selection.clear();
					preparedMoveExecutor.close();
					database.setTransactionSuccessful();  //控制回滚
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					database.endTransaction();  //事务提交
				}
				mAdapter.rebuildCursor(a);
				UIData.counter.setText(0 +"/"+ getItemCount());
			}).show();
	}
		

	RashSet<Integer> mSearchResTree = new RashSet<>();
	
	public void setType(MainActivityUIBase a, int type, boolean checkCache) {
		if(this.type!=type) {
			if(initialized) {
				if(this.type==DB_HISTORY) {
					UIData.fastScroller.setHandleBackground(a.mResource.getDrawable(R.drawable.ic_pen));
					UIData.fastScroller.setBarColor(Color.parseColor("#8f8f8f"));
					UIData.choosed.setVisibility(View.GONE);
					UIData.changed.setVisibility(View.GONE);
				} else {
					UIData.fastScroller.setHandleBackground(a.mResource.getDrawable(R.drawable.ghour));
					UIData.fastScroller.setBarColor(Color.parseColor("#2b4381"));
					UIData.choosed.setVisibility(View.VISIBLE);
					UIData.changed.setVisibility(View.VISIBLE);
				}
			} else {
				pendingType=type;
			}
			this.type=type;
		}
	}
	
	class SearchCardsHandler2 extends AsyncTask<String,Integer,Void>{
		SearchCardsHandler2(){
		}

		@Override
		protected Void doInBackground(String... keys) {
			String key = keys[0];
			mSearchResTree.clear();
//			for(int position=0;position<dataAdapter.getCount();position++) {
//				String text;
//				{
//					cr.moveToPosition(position);
//					try {
//						text=cr.getString(0);
//					} catch (Exception e) {
//						text="!!!Error: "+e.getLocalizedMessage();
//					}
//				}
//
//				if(text.contains(key)) {
//					mSearchResTree.insert(position);
//				}
//			}

			return null;
		}

		@Override
		public void onProgressUpdate(Integer... values) {}


		@Override
		public void onPreExecute() {
			UIData.progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPostExecute(Void result) {
			UIData.progressBar.setVisibility(View.GONE);
			if(mSearchResTree.getRoot()!=null)
				lm.scrollToPositionWithOffset(mSearchResTree.minimum(), UIData.toolbar.getHeight());
			UIData.fastScroller.setTree(mSearchResTree);
			UIData.fastScroller.timeLength = mAdapter.displaying.getCount();
			UIData.fastScroller.invalidate();
			notifyDataSetChanged();
			show(R.string.resCount,mSearchResTree.size());
		}

		@Override
		public void onCancelled(){
			Log.i("","doInBackgroundSearchCards onCancelled() called");
		}
	};
	
	boolean inSearch = false;
	private int lastFallBackTarget=-100;
	int SelectionMode;
	/*神之显/隐体系*/
	int revertage=0;
	boolean should_hide_cd1,should_hide_cd2;
	//private boolean isDirty;
	/*hide/show system by a God*/
	//click
	@Override
	public void onClick(final View v) {
		int pos;
		int offset;
		String msg=null;
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		switch(v.getId()) {
			case R.id.tg2://.ver
				boolean ck = ((ToggleButton)v).isChecked();
				UIData.fastScroller.setVisibility(opt.setScrollShown(ck)?View.GONE:View.VISIBLE);
			break;
			case R.id.toolbar_action1:
				v.setActivated(bIsCombinedSearch = !bIsCombinedSearch);
				opt.setIsCombinedSearching(bIsCombinedSearch);
				break;
			case R.id.tools0://pan
				if(!opt.getSelection_Persists())
					Selection.clear();
				opt.setDBMode(0);
				UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode=SelectionMode_pan;
				UIData.sideBar.setRbyView(v);
				notifyDataSetChanged();
				if(v.getTag()==null)
					msg = "点击查词模式";
				else
					v.setTag(null);
				break;
			case R.id.tools1://选择模式
				opt.setDBMode(3);
				UIData.sideBar.setSCC(opt.getInRemoveMode()?getResources().getColor(R.color.ShallowHeaderBlue):UIData.sideBar.ShelfDefaultGray);
				//if(lastFallBackTarget!=SelectionMode_select)
				//	lastFallBackTarget=SelectionMode;
				lastFallBackTarget=-100;
				if(SelectionMode!=SelectionMode_select) {
					SelectionMode=SelectionMode_select;
					UIData.sideBar.setRbyView(v);
					notifyDataSetChanged();
					if(UIData.counter.getVisibility()!=View.VISIBLE) {
						UIData.counter.setText(Selection.size()+"/"+ getItemCount());
						UIData.counter.setVisibility(View.VISIBLE);
					}
					if(v.getTag()==null)
						msg = "选择模式";//
					else
						v.setTag(null);
				}
				break;
			case R.id.tools001://peruse
				if(!opt.getSelection_Persists())
					Selection.clear();
				opt.setDBMode(1);
				UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode=SelectionMode_peruseview;
				UIData.sideBar.setRbyView(v);
				notifyDataSetChanged();
				if(v.getTag()==null)
					msg = "点击翻阅模式";
				else
					v.setTag(null);
				break;
			case R.id.toolbar_action2://eyedropper
				opt.setDBMode(2);
				UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode=SelectionMode_txtdropper;
				UIData.sideBar.setRbyView(v);
				notifyDataSetChanged();
				if(v.getTag()==null)
					msg = "取词模式";
				else
					v.setTag(null);
				break;
			case R.id.tools3://删除
				if(Selection.size()==0) {
					show(R.string.noseletion);
					return;
				}
//				final boolean hasKeyBoard = imm.hideSoftInputFromWindow(searchView.getWindowToken(),0);
//				//CMN.show(""+hasKeyBoard); //111
//				if(!hasKeyBoard) searchView.clearFocus();
				AlertDialog d = new AlertDialog.Builder(getActivity())
						.setMessage(getResources().getString(R.string.warn_delete, Selection.size()))
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
							String sql = "DELETE FROM "+ getTableName()+" WHERE id = ? ";
							SQLiteDatabase database_mod_delete = mLexiDB.getDB();
							SQLiteStatement preparedDeleteExecutor = database_mod_delete.compileStatement(sql);
							database_mod_delete.beginTransaction();  //开启事务
							try {
								for(Long position:Selection) {//删除记录
									try {
										preparedDeleteExecutor.bindLong(1, position);
										preparedDeleteExecutor.executeUpdateDelete();
									} catch (Exception e) {
										CMN.Log(e);
									}
								}
								Selection.clear();
								preparedDeleteExecutor.close();
								database_mod_delete.setTransactionSuccessful();  //控制回滚
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								database_mod_delete.endTransaction();  //事务提交
								mAdapter.rebuildCursor(a);
								notifyDataSetChanged();
								UIData.counter.setText(Selection.size()+"/"+ getItemCount());
							}
							notifyDataSetChanged();
						}).setOnDismissListener(dialog -> {
//							if(hasKeyBoard) {
//								imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//							}
						}).show();
				d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
				break;
			case R.id.choosed: {//choose deck
				if(System.currentTimeMillis()-last_listHolder_tt<150 && revertage==1) {//too fast from last hide
					should_hide_cd1=true;
				}
				a.showChooseFavorDialog(1);
			} break;
			case R.id.bookmark: {
				onLongClick(((ViewGroup)v.getParent()).getChildAt(0));
			} break;
			case R.id.changed: {//change deck
				if(System.currentTimeMillis()-last_listHolder_tt<150 && revertage==2) {//too fast from last hide
					should_hide_cd2=true;
				}
				a.showChooseFavorDialog(2);
			} break;
			case R.id.search:
//				UIData.toolbar.setVisibility(UIData.toolbar.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
//				if(UIData.toolbar.getVisibility()==View.VISIBLE) {
//					inSearch=true;
//					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//				}else {
//					//automatically hides im ,GREAT!
//					inSearch=false;
//				}
//				notifyDataSetChanged();//TODO min DB IO
//				UIData.fastScroller.showBoolMark(inSearch);
				break;
			case R.id.browser_widget14:
				if(mSearchResTree==null || mSearchResTree.getRoot()==null) {}
				else {
					lv.scrollBy(0, +UIData.toolbar.getHeight());
					RBTNode<Integer> searchTmp = mSearchResTree.xxing_samsara(lm.findFirstVisibleItemPosition());
					if(searchTmp==null) {
						show(R.string.endendr);
						lv.scrollBy(0, -UIData.toolbar.getHeight());
						break;
					}
					pos = searchTmp.getKey();
					lm.scrollToPositionWithOffset(pos, UIData.toolbar.getHeight());
					break;
				}
			case R.id.lst_plain:
				//show("lll");
				offset = 0;
				if(inSearch) {
					lv.scrollBy(0, offset = UIData.toolbar.getHeight());
				}
				pos = lm.findFirstVisibleItemPosition()-1;
				if(pos<0) {
					show(R.string.endendr);
				}else
					lm.scrollToPositionWithOffset(pos,offset);
				break;
			//wwwwwwwwww
			case R.id.browser_widget13:
				if(mSearchResTree==null || mSearchResTree.getRoot()==null) {}
				else {
					lv.scrollBy(0, +UIData.toolbar.getHeight());
					RBTNode<Integer> searchTmp1 = mSearchResTree.sxing_samsara(lm.findFirstVisibleItemPosition());
					if(searchTmp1==null) {
						show(R.string.endendr);
						lv.scrollBy(0, -UIData.toolbar.getHeight());
						break;
					}
					pos = searchTmp1.getKey();
					lm.scrollToPositionWithOffset(pos, UIData.toolbar.getHeight());
					break;
				}
			case R.id.nxt_plain:
				//show("nnn");
				offset = 0;
				if(inSearch) {
					lv.scrollBy(0, offset = UIData.toolbar.getHeight());
				}
				pos = lm.findFirstVisibleItemPosition()+1;
				if(pos>=getItemCount()) {
					show(R.string.endendr);
				}else
					lm.scrollToPositionWithOffset(pos,offset);
				break;
		}

		if(msg!=null) {
			a.showTopSnack(UIData.snackRoot, msg, 0.5f, -1, -1, 0);
		}
	}
	
	void notifyDataSetChanged() {
		if(initialized)mAdapter.notifyDataSetChanged();
	}
	
	
	private void try_exit_selection() {
		if(SelectionMode==SelectionMode_select)
			if(!opt.getSelection_Persists()) {//清空选择
				Selection.clear();
				UIData.counter.setText(Selection.size()+"/"+ getItemCount());
				UIData.counter.setVisibility(View.GONE);
			}
	}
	//lazy strategy. reuse as much as possible.
	PopupWindow sharedPopup;
	int menuResId = -1;
	int onclickBase=0;
	int lastPopupId=-1;
	ArrayAdaptermy<String> shareListAda;
	void initPopup(){
		View view = getActivity().getLayoutInflater().inflate(R.layout.popup_more_tools, null);
		sharedPopup = new PopupWindow(view,
				(int)(160 * getResources().getDisplayMetrics().density), LayoutParams.WRAP_CONTENT);

		sharedPopup.setOnDismissListener(() -> {
		});
		final ListView shareList = view.findViewById(R.id.share_list);
		shareListAda = new ArrayAdaptermy<>(getActivity(),
				R.layout.popup_list_item);
		shareList.setAdapter(shareListAda);
		shareList.setOnItemClickListener((parent, view1, position, id) -> {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			if(a==null) return;
			switch(position+onclickBase) {//处理点击事件
				case 10://全选
					//TODO develop more efficient and elegant algorithm.
					for(int i = 0; i< getItemCount(); i++) {
						Selection.add(mAdapter.getReaderAt(i).row_id);
					}
					UIData.counter.setText(Selection.size()+"/"+ getItemCount());
					notifyDataSetChanged();
					break;
				case 11:
					Selection.clear();
					UIData.counter.setText(Selection.size()+"/"+ getItemCount());
					notifyDataSetChanged();
					break;
				case 12://反选
					for(int i = 0; i< getItemCount(); i++) {
						long rowId = mAdapter.getReaderAt(i).row_id;
						if(!Selection.remove(rowId))
							Selection.add(rowId);
					}
					
					UIData.counter.setText(Selection.size()+"/"+ getItemCount());
					notifyDataSetChanged();
					break;
				case 13://反向选择-toggle
					if(opt.toggleInRemoveMode()) {
						//main_clister_layout.findViewById(R.id.tools1).getBackground().setColorFilter(Color.parseColor("#FF4081"), PorterDuff.Mode.SRC_IN);
						UIData.sideBar.setSCC(getResources().getColor(R.color.ShallowHeaderBlue));
					}else {
						//main_clister_layout.findViewById(R.id.tools1).getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
						UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
					}
					break;
				case 30://show all decks
					break;
				case 31://刷新
					break;
				case 40://更新书签
					// toimpl
//					if(cr!=null && lm.findFirstVisibleItemPosition()>=1 && dataAdapter.getCount()>=0) {
//						if(lv.getChildAt(0)!=null) {
//							String key = String.valueOf(((TextView) lv.getChildAt(0).findViewById(android.R.id.text1)).getText());
//							if(mLexiDB.updateBookMark(key)!=-1)
//								show(R.string.bookmarkupdated, key);
//						}
//					}
					break;
				case 41://跳转书签
					String key = mLexiDB.getLastBookMark();
//					if(key!=null) {
//						int cc=0;
//						for(int i=0;i<dataAdapter.getCount();i++) {
//							String text;
//							{
//								cr.moveToPosition(position);
//								try {
//									text=cr.getString(0);
//								} catch (Exception e) {
//									text="!!!Error: "+e.getLocalizedMessage();
//								}
//							}
//
//							if(text.equals(key)) {
//								lm.scrollToPositionWithOffset(cc, 0);
//								show(R.string.bookmarkjumped, key);
//								return;
//							}
//							cc++;
//						}
//					}
					show(R.string.bookmarkfailed, key);
					break;
				case 50://严格模式
					UIData.fastScroller.setConservativeScroll(true);
					opt.setShelfStrictScroll(true);
					break;
				case 51://宽松模式
					UIData.fastScroller.setConservativeScroll(false);
					opt.setShelfStrictScroll(false);//.putBoolean("strictscroll", false);
					break;
			}
			TextView tv = (TextView) view1;
			tv.setText(tv.getText()+"...");
			getActivity().getWindow().getDecorView().postDelayed(() -> sharedPopup.dismiss(), 150);
		});
	}
	
	private int getItemCount() {
		return mAdapter==null?0:mAdapter.getItemCount();
	}
	
	@Override
	public boolean onLongClick(final View v) {
		menuResId = -1;
		onclickBase=0;
		boolean interceptClick = false;
		switch(v.getId()) {
			case R.id.tools1:
				menuResId=R.array.selection_tweak;
				onclickBase=10;
				interceptClick=true;
			break;
			case R.id.choosed:
				menuResId=R.array.choosed_tweak;
				onclickBase=30;
				interceptClick=true;
			break;
			case R.id.bookmark:
				v.performClick();
			return true;
			case R.id.bookmark0:
				menuResId=R.array.bookmark0_tweak;
				onclickBase=40;
				interceptClick=true;
			break;
			case R.id.tg2:
				menuResId=R.array.ver_tweak;
				onclickBase=50;
				interceptClick=true;
			break;
		}
//		if(sharePopup!=null && sharePopup.isShowing()) {
//			sharePopup.dismiss();
//			return true;
//		}
		if(sharedPopup ==null)
			initPopup();

		if(lastPopupId!=v.getId()) {//need re-populate
			shareListAda.setArray(Arrays.asList(getResources().getStringArray(menuResId)));
			lastPopupId=v.getId();
		}

		sharedPopup.setFocusable(false);
		sharedPopup.setOutsideTouchable(true);
		sharedPopup.setBackgroundDrawable(null);
		sharedPopup.showAsDropDown(v, v.getWidth(), -v.getHeight());
		return interceptClick;
	}

	TextView toastTv;
	View toastV;
	AlphaAnimation fadeAnima;
	public void show(int ResId,Object...args) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a!=null) {
			a.cancleToast();
			show(getResources().getString(ResId,args));
		}
	}
	boolean maskOn;
	float inputBase=-1;
	public resultRecorderCombined rec;
	public void show(String text)
	{
		if(getActivity()==null) return;
		if(toastV == null) {
			toastV = UIData.toastLayoutRootmy;//a.getLayoutInflater().inflate(R.layout.toast,null);
			toastV.setOnTouchListener((v, event) -> {
				if(inputBase==-1)
					maskOn = true;
				return false;
			});
			//toastV.setBackgroundColor(ContextCompat.getColor(a,R.color.colorHeaderBlue));
			toastTv = toastV.findViewById(R.id.message);
			fadeAnima = new AlphaAnimation(1, 0);
			fadeAnima.setDuration(1600);
			fadeAnima.setRepeatCount(0);
			fadeAnima.setFillAfter(true);
			fadeAnima.setInterpolator(input -> {
				if(maskOn) {//提前结束
					inputBase = input;
					maskOn = false;
				}
				if(inputBase!=-1)
					if(inputBase<0.8)
						input = (input-inputBase)+0.8f;
				if(input<0.7f)
					return 0;
				else {
					float ret =  (float) ((input-0.7)*(1/(0.3)));//线性陡升
					if(ret>=1) fadeAnima.cancel();//禁止超射
					return ret;
				}

			});
			fadeAnima.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {inputBase=-1;maskOn=false;}

				@Override
				public void onAnimationEnd(Animation animation) {
					toastV.setVisibility(View.GONE);
					toastTv.setAlpha(1);
				}

				@Override public void onAnimationRepeat(Animation animation) {
					//CMN.show("re");
				}
			});
		}
		toastV.setVisibility(View.VISIBLE);
		toastTv.setText(text);
		toastTv.startAnimation(fadeAnima);
	}

	public final SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
	int avoyagerIdx=0;
	int adelta=0;

	public void resetPageMemorization(){
		avoyager.clear();
		avoyagerIdx=adelta=0;
	}
	
	@Override
	public boolean onItemLongClick(View view, int position) {
		//if(lastDragPos!=-1)((DragSelectRecyclerView)lv).setDragSelectActive(false, lastDragPos);
		((DragSelectRecyclerView)lv).setDragSelectActive(true, lastDragPos = position);
		if(SelectionMode!=SelectionMode_select) {
			int tmpVal = SelectionMode;
			View target = UIData.tools1;
			target.setTag(false);
			target.performClick();
			lastFallBackTarget=tmpVal;
			boolean alreadyselected = Selection.contains(mAdapter.getReaderAt(position).row_id);
			if(!alreadyselected) {
				view.performClick();
			}
			return false;
		}
		return true;
	}
	
	public void onItemClick(View view, int position) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if (a == null) return;
		if (a instanceof PDICMainActivity) {
			((PDICMainActivity) a).setContentBow(false);
		}
		if (view != null) {
			adelta = 0;
			//TODO retrieve from sibling views
			currentDisplaying = String.valueOf(((TextView) view.findViewById(android.R.id.text1)).getText());
		}
		
		int lastClickedPosBeforePageTurn = position - adelta;
		ScrollerRecord pagerec = null;
		DeckListAdapter.HistoryDatabaseReader reader = mAdapter.getReaderAt(currentPos = position);
		long rowId = currentRowId = reader.row_id;
		currentDisplaying = reader.record;
		
		switch (SelectionMode) {
			case SelectionMode_select: {
				if (!Selection.remove(rowId)) {
					Selection.add(rowId);
				}
				UIData.counter.setText(Selection.size() + "/" + getItemCount());
				UIData.counter.setVisibility(View.VISIBLE);
				mAdapter.notifyItemChanged(position);
			}
			break;
			case SelectionMode_pan: {
				//toimpl
				boolean rendered = false;
				{
					String texts = reader.books;
					CMN.Log("复活::", texts);
					if (texts != null) {
						String[] textsArr = texts.split(";");
						if (textsArr.length == 1) {
							rendered = queryAndShowOneDictionary(a.getBookById(IU.parseLong(textsArr[0], -1)), currentDisplaying, position, false);
						} else if (textsArr.length > 1) {
							rendered = queryAndShowMultipleDictionary(textsArr, currentDisplaying, position, false);
						}
					}
					if (rendered) {
						break;
					}
				}
				if (bIsCombinedSearch) {
					rendered = queryAndShowMultipleDictionary(null, currentDisplaying, position, true);
					if (!rendered) {
						if (a.main.getChildCount() == 1) {
							show(R.string.searchFailed, currentDisplaying);
						} else {
							a.show(R.string.searchFailed, currentDisplaying);
							webviewHolder.removeAllViews();
							ViewGroup anothorHolder = a.webSingleholder;
							int remcount = anothorHolder.getChildCount() - 1;
							if (remcount > 0) anothorHolder.removeViews(1, remcount);
						}
					}
				}
				else {
					//CMN.Log("单独搜索模式");
					rendered = queryAndShowOneDictionary(a.currentDictionary, currentDisplaying, position, true);
					if (!rendered) {
						if (a.main.getChildCount() == 1) {
							show(R.string.searchFailed, currentDisplaying);
						} else {
							a.show(R.string.searchFailed, currentDisplaying);
							ViewGroup anothorHolder = a.weblistHandler;
							anothorHolder.removeAllViews();
							int remcount = webviewHolder.getChildCount() - 1;
							if (remcount > 0) webviewHolder.removeViews(1, remcount);
						}
					}
				}
			}
			break;
			case SelectionMode_peruseview: {
				ArrayList<Long> records = new ArrayList<>();
				additiveMyCpr1 datalet = new additiveMyCpr1(currentDisplaying, records);
				ArrayList<additiveMyCpr1> data = new ArrayList<>();
				data.add(datalet);
				String currentDisplaying__ = mdict.replaceReg.matcher(currentDisplaying).replaceAll("").toLowerCase();
				boolean reorded = false;
				if (a.peruseView != null && a.peruseView.peruseF.getChildCount() > 0) {
					a.DetachDBrowser();
				}
				long bid;
				{
					String texts = reader.books;
					CMN.Log("复活::", texts);
					if (texts != null) {
						String[] textsArr = texts.split(";");
						for (String strId : textsArr) {
							if ((bid = IU.parseLong(strId, -1)) >= 0) records.add(bid);
						}
					}
				}
				CMN.Log(records);
				Collection<Long> avoidLet = null;
				if (records.size() > 0)
					avoidLet = a.md.size() >= 32 ? new HashSet<>(records) : records;
				for (int i = 0; i < a.md.size(); i++) {//联合搜索
					int dIdx = i;
					bid = a.getBookIdAt(i);
					if (avoidLet != null && avoidLet.contains(bid)) {
						continue;
					}
					if (opt.getPeruseAddAll()) {
						records.add(bid);
						continue;
					}
					if (!bIsCombinedSearch) {
						if (dIdx == 0) if (a.adapter_idx > 0 && a.adapter_idx < a.md.size()) {
							dIdx = a.adapter_idx;
							reorded = true;
						} else if (reorded) if (dIdx <= a.adapter_idx) {
							dIdx -= 1;
						}
					}
					BookPresenter presenter = a.md_get(dIdx);
					{
						int idx = presenter.bookImpl.lookUp(currentDisplaying__);
						if (idx >= 0)
							while (idx < presenter.bookImpl.getNumberEntries()) {
								if (mdict.replaceReg.matcher(presenter.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
									records.add(presenter.getId());
								} else
									break;
								idx++;
							}
					}
				}
				a.JumpToPeruseMode(currentDisplaying, records, -2, true);
			}
			break;
			case SelectionMode_txtdropper: {
				EditText target = null;
				if (a.thisActType == ActType.MultiShare) {
					if (a.peruseView != null) {
						target = a.peruseView.etSearch;
					} else {
						a.getUcc().setInvoker(null, null, null, currentDisplaying);
					}
				} else {
					a.lastEtString = String.valueOf(a.etSearch.getText());
					target = a.etSearch;
					a.etSearch_ToToolbarMode(2);
				}
				if (target != null) {
					target.setText(currentDisplaying);
				}
				a.DetachDBrowser();
			}
			break;
			default:
				throw new IllegalStateException("Unexpected value: " + SelectionMode);
		}
	}
	
	private boolean queryAndShowMultipleDictionary(String[] texts, String currentDisplaying, int position, boolean queryAll) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		
		int lastClickedPosBeforePageTurn = position - adelta;
		
		ArrayList<Long> records = new ArrayList<>();
		additiveMyCpr1 datalet = new additiveMyCpr1(currentDisplaying,records);
		ArrayList<additiveMyCpr1> data = new ArrayList<>();
		data.add(datalet);
		String currentDisplaying__ = mdict.replaceReg.matcher(currentDisplaying).replaceAll("").toLowerCase();
		a.bShowLoadErr=false;
		long st = CMN.rt();
		if (texts!=null) {
			for(int dIdx=0;dIdx<texts.length;dIdx++) {//联合搜索
				long bid = IU.parseLong(texts[dIdx], -1);
				BookPresenter bookPresenter = a.getBookById(bid);
				{
					long idx = bookPresenter.bookImpl.lookUp(currentDisplaying__);
					if (idx >= 0) {
						if(idx==0 && bookPresenter.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
							bookPresenter.SetSearchKey(currentDisplaying__);
							records.add(bid);
							records.add(idx);
						} else
						while (idx < bookPresenter.bookImpl.getNumberEntries()) {
							if (mdict.replaceReg.matcher(bookPresenter.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
								records.add(bid);
								records.add(idx);
							} else {
								break;
							}
							idx++;
						}
					}
				}
			}
		}
		else {
			for(int dIdx=0;dIdx<a.md.size();dIdx++) {//联合搜索
				BookPresenter mdTmp = a.md_get(dIdx);
				{
					long idx = mdTmp.bookImpl.lookUp(currentDisplaying__);
					if (idx >= 0)
						while (idx < mdTmp.bookImpl.getNumberEntries()) {
							if (mdict.replaceReg.matcher(mdTmp.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
								records.add(a.getBookIdAt(dIdx));
								records.add(idx);
							} else
								break;
							idx++;
						}
				}
			}
		}
		CMN.Log("联合搜索 - 同步延时 : ", CMN.elapsed(st));
		a.bShowLoadErr=true;
		webviewHolder = a.weblistHandler;
		ViewGroup anothorHolder = a.webSingleholder;
		CMN.Log("SelectionMode_pan", records.size());
		if(records.size()>0) {
			//yyy
			a.recCom = rec = new resultRecorderCombined(a,data,a.md);
			ScrollViewmy WHP = (ScrollViewmy) a.weblistHandler.getScrollView();
			ScrollerRecord pagerec = null;
			OUT:
			if(adelta!=0 && System.currentTimeMillis()-a.lastClickTime>300) {//save our postion
				pagerec = avoyager.get(lastClickedPosBeforePageTurn);
				if (pagerec == null) {
					if (WHP.getScrollY() != 0) {
						pagerec = new ScrollerRecord();
						avoyager.put(lastClickedPosBeforePageTurn, pagerec);
					} else
						break OUT;
				}
				pagerec.set(0, WHP.getScrollY(), 1);
			}

			adelta=0;
			a.lastClickTime=System.currentTimeMillis();

			pagerec = avoyager.get(position);
			if (pagerec != null) {
				rec.expectedPos = pagerec.y;
				//currentDictionary.mWebView.setScrollY(currentDictionary.expectedPos);
				//CMN.Log("取出旧值", combining_search_result.expectedPos, pos, avoyager.size());
			} else {
				rec.expectedPos = 0;
				//CMN.Log("新建", combining_search_result.expectedPos, pos);
			}

			WHP.setVisibility(View.VISIBLE);
			if(anothorHolder.getVisibility()==View.VISIBLE) {
				if(anothorHolder.getChildCount()!=0)
					anothorHolder.removeAllViews();
				anothorHolder.setVisibility(View.GONE);
			}

			a.widget13.setVisibility(View.VISIBLE);
			a.widget14.setVisibility(View.VISIBLE);
			a.contentview.setVisibility(View.VISIBLE);
			imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);

			a.AttachContentViewForDB();

			rec.renderContentAt(0, a,null);

			processFavorite(position, currentDisplaying);
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean queryAndShowOneDictionary(BookPresenter currentDictionary, String currentDisplaying, int position, boolean queryAll) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		
		int lastClickedPosBeforePageTurn = position - adelta;
		
		float desiredScale=-1;
		a.TransientIntoSingleExplanation();
		
		String key = currentDisplaying;
		int offset = mdict.offsetByTailing(key);
		int idx;
		if(offset>0)
			key = key.substring(0,key.length()-offset);
		if (currentDictionary.getType()== DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
			currentDictionary.SetSearchKey(key);
			idx = 0;
		} else {
			idx = currentDictionary.bookImpl.lookUp(key,true);
		}
		int adapter_idx=a.adapter_idx;
		
		if(idx<0 && queryAll) {
			for(adapter_idx=0;adapter_idx<a.md.size();adapter_idx++) {
				if(adapter_idx!=a.adapter_idx) {
					currentDictionary = a.md_get(adapter_idx);
					idx=currentDictionary.bookImpl.lookUp(key,true);
					if(idx>=0) break;
				}
			}
		}
		
		webviewHolder = a.webSingleholder;
		ViewGroup anothorHolder = a.weblistHandler;
		if(idx>=0) {
			currentDictionary.initViewsHolder(a);
			ScrollerRecord pagerec = null;
			if(opt.getRemPos()) {
				OUT:
				if(System.currentTimeMillis()-a.lastClickTime>300)//save our postion
					if(webviewHolder.getChildCount()!=0) {
						View s_rl = webviewHolder.getChildAt(0);
						int tag= IU.parsint(s_rl.getTag(), -1);
						if(tag!=-1) {
							BookPresenter lastDictionary = a.md_get(tag);
							{
								WebViewmy current_webview = lastDictionary.mWebView;
								if (adelta != 0 && current_webview != null && !current_webview.isloading) {
									if (current_webview.webScale == 0)
										current_webview.webScale = a.dm.density;//sanity check
									//CMN.Log("保存位置", lastDictionary._Dictionary_fName, tag);
									
									pagerec = avoyager.get(lastClickedPosBeforePageTurn);
									if (pagerec == null) {
										if (current_webview.getScrollX() != 0 || current_webview.getScrollY() != 0 || current_webview.webScale != BookPresenter.def_zoom) {
											pagerec = new ScrollerRecord();
											avoyager.put(lastClickedPosBeforePageTurn, pagerec);
										} else
											break OUT;
									}
									
									pagerec.set(current_webview.getScrollX(), current_webview.getScrollY(), current_webview.webScale);
								}
							}
						}
					}
				
				adelta=0;
				a.lastClickTime=System.currentTimeMillis();
				
				pagerec = avoyager.get(position);
				//a.showT(""+currentDictionary.expectedPos);
			}
			
			if(pagerec!=null) {
				currentDictionary.mWebView.expectedPos = pagerec.y;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				currentDictionary.mWebView.expectedPosX = pagerec.x;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				desiredScale=pagerec.scale;
				CMN.Log(avoyager.size()+"~"+position+"~取出旧值"+currentDictionary.mWebView.expectedPos+" scale:"+pagerec.scale);
			} else {
				currentDictionary.mWebView.expectedPos=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				currentDictionary.mWebView.expectedPosX=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
			}
			
			imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
			a.AttachContentViewForDB();
			
			if(offset>0)//apply tailing offset
				if(currentDictionary.bookImpl.getEntryAt(idx+offset).equals(key))
					idx+=offset;
			int tmpIdx=idx;
			while(tmpIdx+1<currentDictionary.bookImpl.getNumberEntries() && mdict.processText(currentDictionary.bookImpl.getEntryAt(tmpIdx)).equals(mdict.processText(currentDictionary.bookImpl.getEntryAt(tmpIdx+1)))) {
				tmpIdx++;
				if(currentDictionary.bookImpl.getEntryAt(tmpIdx).trim().equals(key.trim())) {
					idx=tmpIdx;
					break;
				}
			}
			
			ViewGroup someView = currentDictionary.rl;
			if(someView.getParent()!=webviewHolder) {
				if(someView.getParent()!=null) ((ViewGroup)someView.getParent()).removeView(someView);
				webviewHolder.addView(currentDictionary.rl);
			}
			if(webviewHolder.getChildCount()>1) {
				for(int i=webviewHolder.getChildCount()-1;i>=0;i--)
					if(webviewHolder.getChildAt(i)!=currentDictionary.rl) webviewHolder.removeViewAt(i);
			}
			
			currentDictionary.renderContentAt(desiredScale,RENDERFLAG_NEW,0,null, idx);
			
			currentDictionary.mWebView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			currentDictionary.rl.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			processFavorite(position, currentDisplaying);
			return true;
		}
		else {
			return false;
		}
	}
	
	
	public void toggleFavor() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if (a == null) return;
		if(type==DB_FAVORITE) {
			if (toDeleteV2.remove(currentRowId)) {
				a.favoriteBtn.setActivated(true);
				a.show(R.string.added);
			} else {
				a.favoriteBtn.setActivated(false);
				toDeleteV2.add(currentRowId);
				isToDel = true;
				a.show(R.string.toRemove);
			}
		} else {
			String text = currentDisplaying;
			if(a.GetIsFavoriteTerm(text)) {//删除
				a.removeFavoriteTerm(text);
				a.favoriteBtn.setActivated(false);
				a.show(R.string.removed);
			} else {//添加
				a.favoriteCon.insert(a, text, opt.getCurrFavoriteNoteBookId(), webviewHolder);
				a.favoriteBtn.setActivated(true);
				a.show(R.string.added);
			}
		}
	}


	protected void processFavorite(int position,String key) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null || a.favoriteBtn==null) return;
		if(type==DB_FAVORITE) {
			a.favoriteBtn.setActivated(!toDeleteV2.contains(currentRowId));
		} else {
			a.favoriteBtn.setActivated(a.GetIsFavoriteTerm(key));
		}
	}

	public void goBack() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null) return;
		if(opt.getBottomNavigationMode()==0) {
			if (currentPos - 1 < 0) {
				a.showTopSnack(a.main_succinct, R.string.endendr, -1, -1, -1, 0);
				return;
			}
			//int first = lm.findFirstVisibleItemPosition();
			if (currentPos < lm.findFirstVisibleItemPosition())
				lm.scrollToPositionWithOffset(currentPos, 0);
			adelta = -1;
			onItemClick(null, --currentPos);
		} else {
			a.GoBackOrForward(webviewHolder, -1);
		}
	}

	public void goQiak() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null) return;
		if(opt.getBottomNavigationMode()==0) {
			if (currentPos + 1 > getItemCount() - 1) {
				a.show(R.string.endendr);
				return;
			}
			currentPos += 1;
			int last = lm.findLastVisibleItemPosition();
			boolean hei = false;
			if (currentPos == last) {
				if (lv.getChildAt(last) != null) {
					hei = lv.getHeight() - lv.getChildAt(last).getTop() < lv.getChildAt(last).getHeight() * 2 / 3;
				}
			} else
				hei = currentPos > last;

			if (hei) {
				lm.scrollToPositionWithOffset(currentPos, 0);
			}
			adelta = 1;
			onItemClick(null, currentPos);
		} else {
			a.GoBackOrForward(webviewHolder, 1);
		}
	}

	public String getTableName() {
		return type==DB_FAVORITE?TABLE_FAVORITE_v2:TABLE_HISTORY_v2;
	}
	
	public int getFragmentType() {
		return type;
	}
}

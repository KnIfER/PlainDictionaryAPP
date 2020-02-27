package com.knziha.plod.PlainDict;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.IDragSelectAdapter;
import com.knziha.ankislicer.customviews.ArrayAdaptermy;
import com.knziha.ankislicer.customviews.ShelfLinearLayout;
import com.knziha.ankislicer.customviews.VerticalRecyclerViewFastScrollermy;
import com.knziha.ankislicer.customviews.wahahaTextView;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.MyIntPair;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.Utils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTNode;
import com.knziha.rbtree.RashSet;
import com.knziha.rbtree.additiveMyCpr1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import db.LexicalDBHelper;

@SuppressLint("SetTextI18n")
public class DBroswer extends Fragment implements
		View.OnClickListener, OnLongClickListener{
	public int pendingDBClickPos=-1;
	ViewGroup webviewHolder;
	protected boolean initialized;
	protected int itemCount;
	protected Runnable mPullViewsRunnable = new Runnable() {
		@Override
		public void run() {
			if(mCards_size>itemCount){
				itemCount+=1;
				mAdapter.notifyItemChanged(itemCount);
				View lc = lv.getChildAt(lv.getChildCount()-2);
				if(lc!=null && lc.getBottom()>=lv.getHeight()){
					CMN.Log("停止拉取", ((MyViewHolder)lc.getTag()).webView.getText());
					itemCount = mCards_size;
				}
				if(mCards_size>itemCount){
					lv.postDelayed(mPullViewsRunnable, 15);
				}
			}
		}
	};

	public DBroswer(){
		super();
		mAdapter = new main_list_Adapter();
	}

	protected PDICMainAppOptions opt;

	public String currentDisplaying = "";
	public int currentPos=-1;

	RecyclerView lv;

	main_list_Adapter mAdapter;

	View main_clister_layout;
	View progressBar;
	ToggleButton tg2;
	ImageView toolbar_action1;
	Toolbar toolbar;
	ShelfLinearLayout sideBar;
	TextView counter;
	View shelfright;

	protected final SparseArray<ItemCard> mCards = new SparseArray<>();
	SparseArray<String> toDelete = new SparseArray<>();
	HashSet<Integer> Selection = new HashSet<>();

	public int StoragePolicy;
	boolean isToDel = false;

	LinearLayoutManager lm;
	VerticalRecyclerViewFastScrollermy fastScroller;
	ViewGroup snack_root;

	SearchView searchView;
	InputMethodManager imm;
	private View bookmark;

	public int try_goBack(){
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return 0;
		if(a.isContentViewAttached()) {
			if(opt.getUseBackKeyGoWebViewBack()){
				WebViewmy view = a.getCurrentWebContext();
				if(view!=null && view.canGoBack()){
					view.goBack();
					return 1;
				}
			}
			a.DetachContentView();
			if(!isToDel || toDelete.size()==0) return 1;

			//删除收藏
			String sql = "delete from t1 where lex = ? ";
			SQLiteStatement preparedDeleteExecutor = mLexiDB.getDB().compileStatement(sql);
			mLexiDB.getDB().beginTransaction();  //开启事务
			int count = 0;
			int toDelete_size = toDelete.size();
			try {
				for(int i=0;i<toDelete.size();i++) {//delete
					String key = toDelete.valueAt(i);
					int deleI = toDelete.keyAt(i);
					preparedDeleteExecutor.bindString(1, key);
					if(preparedDeleteExecutor.executeUpdateDelete()!=-1) {
						//Selection.remove(deleI);
						mCards.remove(deleI);
						count++;
					}
					toDelete.removeAt(i);
				}
				preparedDeleteExecutor.close();
				Selection.clear();
				mLexiDB.getDB().setTransactionSuccessful();  //控制回滚
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mLexiDB.getDB().endTransaction();  //事务提交
				cr.close();
				cr = mLexiDB.getDB().query("t1", null,null,null,null,null,"date desc");
				mCards.clear();
				show(R.string.maniDel,count,toDelete_size);
				mAdapter.notifyDataSetChanged();
			}


			return 1;
		}
		if(inSearch) {
			main_clister_layout.findViewById(R.id.search).performClick();
			return 1;
		}
		if(Selection.size()>0) {//SelectionMode==SelectionMode_select
			Selection.clear();
			mAdapter.notifyDataSetChanged();
			counter.setText(Selection.size()+"/"+mCards_size);
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
					View target = main_clister_layout.findViewById(taregtID);
					target.setTag(false);
					target.performClick();
				}
			}else {
				counter.setText(Selection.size()+"/"+mCards_size);
			}
			return 1;
		}
		if(isDirty) {
			opt.putFirstFlag();
			//CMN.Log("DBROWSER写配置……");
		}
		return 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(main_clister_layout!=null)
			return main_clister_layout;
		//CMN.Log("onCreateView!!!");
		View _main_clister_layout= inflater.inflate(R.layout.card_lister, container,false);
		progressBar = _main_clister_layout.findViewById(R.id.progress_bar);
		progressBar.setVisibility(View.GONE);
		lv = _main_clister_layout.findViewById(R.id.main_list);
		snack_root = _main_clister_layout.findViewById(R.id.snack_root);
		fastScroller = _main_clister_layout.findViewById(R.id.fast_scroller);
		fastScroller.setRecyclerView(lv);
		lv.addOnScrollListener(fastScroller.getOnScrollListener());
		lv.setLayoutManager(lm = new LinearLayoutManager(inflater.getContext()));
		lv.setAdapter(mAdapter);

		RecyclerView.RecycledViewPool pool = lv.getRecycledViewPool();
		pool.setMaxRecycledViews(0,10);
		for(int index =0;index < 10;index++) {
			 pool.putRecycledView(mAdapter.createViewHolder(lv,0));
		}

		mAdapter.setOnItemClickListener(mainClicker);
		mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
			int lastDragPos=-1;
			@Override
			public boolean onItemLongClick(View view, int position) {
				//if(lastDragPos!=-1)((DragSelectRecyclerView)lv).setDragSelectActive(false, lastDragPos);
				((DragSelectRecyclerView)lv).setDragSelectActive(true, lastDragPos = position);
				if(SelectionMode!=SelectionMode_select) {
					int tmpVal = SelectionMode;
					View target = _main_clister_layout.findViewById(R.id.tools1);
					target.setTag(false);
					target.performClick();
					lastFallBackTarget=tmpVal;
					if(!Selection.contains(position)) {
						view.performClick();
					}
					return false;
				}
				return true;
			}});

		toolbar_action1=_main_clister_layout.findViewById(R.id.toolbar_action1);
		//toolbar_action1.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
		toolbar_action1.setOnClickListener(this);
		toolbar_action1.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

		_main_clister_layout.findViewById(R.id.tools0).setOnClickListener(this);
		_main_clister_layout.findViewById(R.id.tools001).setOnClickListener(this);
		_main_clister_layout.findViewById(R.id.toolbar_action2).setOnClickListener(this);
		View select_cards = _main_clister_layout.findViewById(R.id.tools1);
		select_cards.setOnClickListener(this);
		select_cards.setOnLongClickListener(this);
		_main_clister_layout.findViewById(R.id.tools3).setOnClickListener(this);

		_main_clister_layout.findViewById(R.id.choosed).setOnClickListener(this);
		_main_clister_layout.findViewById(R.id.changed).setOnClickListener(this);
		bookmark = _main_clister_layout.findViewById(R.id.bookmark);
		bookmark.setOnClickListener(v -> onLongClick(_main_clister_layout.findViewById(R.id.bookmark0)));
		_main_clister_layout.findViewById(R.id.search).setOnClickListener(this);

		tg2 = _main_clister_layout.findViewById(R.id.tg2);
		tg2.setOnClickListener(checkViewOnClick);
		tg2.setOnLongClickListener(this);
		//main_clister_layout.findViewById(R.id.choosed).setOnLongClickListener(this);

		_main_clister_layout.findViewById(R.id.browser_widget15).setOnClickListener(new Utils.DummyOnClick());
		_main_clister_layout.findViewById(R.id.browser_widget14).setOnClickListener(this);
		_main_clister_layout.findViewById(R.id.browser_widget13).setOnClickListener(this);

		toolbar = _main_clister_layout.findViewById(R.id.toolbar);
		//sideBarPopHolder = (Toolbar) main_clister_layout.findViewById(R.id.sideBarPopHolder);
		toolbar.inflateMenu(R.menu.menu_search_view);
		toolbar.setBackgroundColor(0xcc000000|(CMN.MainBackground&0xffffff));
		Menu toolbarmenu = toolbar.getMenu();
		MenuItem searchItem = toolbarmenu.getItem(0);

		searchView = (SearchView) searchItem.getActionView();

		//ImageView iv_submit = searchView.findViewById(R.id.search_go_btn);
		////这样就可以修改图片了
		//iv_submit.setImageResource(R.drawable.ic_directions_black_24dp);//enter

		searchView.setIconified(false);//设置searchView处于展开状态
		searchView.onActionViewExpanded();// 当展开无输入内容的时候，没有关闭的图标
		searchView.setIconifiedByDefault(false);//默认为true在框内，设置false则在框外
		searchItem.setShowAsAction(2);
		searchView.setSubmitButtonEnabled(true);//显示提交按钮
		searchView.findViewById(R.id.search_src_text).setOnFocusChangeListener((v, hasFocus) -> {
			//dont hide, nothing to be shy about.
			v.postDelayed(() -> {
				searchView.findViewById(R.id.search_go_btn).setVisibility(View.VISIBLE);
				searchView.findViewById(R.id.submit_area).setVisibility(View.VISIBLE);
			}, 50);
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				//Toast.makeText(getActivity(), query, Toast.LENGTH_SHORT).show();



				searchView.clearFocus();
				//InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

				new SearchCardsHandler2().execute(query);

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				//当输入框内容改变的时候回调
				//Log.i(TAG,"内容: " + newText);
				return true;
			}
		});
		searchView.clearFocus();

		sideBar = _main_clister_layout.findViewById(R.id.sideBar);
		counter =  _main_clister_layout.findViewById(R.id.counter);
		counter.setVisibility(View.GONE);
		shelfright = _main_clister_layout.findViewById(R.id.shelfright);
		newStart = true;
		return main_clister_layout = _main_clister_layout;
	}
	boolean newStart;

	long last_listHolder_tt;
	private OnClickListener checkViewOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			boolean ck = ((ToggleButton)v).isChecked();
				switch(v.getId()) {
					case R.id.tg2://.ver
						if(opt.setScrollShown(ck))
							fastScroller.setVisibility(View.GONE);
						else
							fastScroller.setVisibility(View.VISIBLE);
						isDirty=true;
				break;
			}
		}};

	String mRestrictOnDeck;
	int lastFirst = 0;
	LexicalDBHelper mLexiDB;
	Cursor cr;

	int lastChecked = 0;

	protected int mCards_size;
	@Override
	public void onDetach(){
		super.onDetach();
		if(cr!=null && lm.findFirstVisibleItemPosition()>=1 && mCards_size>=0) {
			View v = lv.getChildAt(0);
			((AgentApplication)getActivity().getApplication()).putLastContextualIndexByDatabaseFileName(mLexiDB.DATABASE, lm.findFirstVisibleItemPosition(), v==null?0:v.getTop());
		}
	}

	private void rebuildCursor(MainActivityUIBase a) {
		mCards.clear();
		cr = mLexiDB.getDB().query("t1", null,null,null,null,null,"date desc");
		mCards_size = cr.getCount();
		//todo 记忆 lastFirst
		int offset = 0;
		lastFirst = 0;
		if(true){
			MyIntPair lcibdfn = ((AgentApplication) a.getApplication()).getLastContextualIndexByDatabaseFileName(mLexiDB.DATABASE);
			if(lcibdfn!=null){
				lastFirst = Math.min(lcibdfn.key, mCards_size);
				offset =  lcibdfn.value;
			}
		}
		if(getDelayPullData()) {
			itemCount = lastFirst;
			lv.post(mPullViewsRunnable);
		} else {
			itemCount = mCards_size;
		}
		mAdapter.notifyDataSetChanged();
		lm.scrollToPositionWithOffset(lastFirst,offset);
		lm.setInitialPrefetchItemCount(10);
	}

	protected void loadInAll(MainActivityUIBase a) {
		mLexiDB = a.prepareFavoriteCon();
		rebuildCursor(a);
		String name = CMN.unwrapDatabaseName(mLexiDB.DATABASE);
		toolbar.setTitle(name);
		show(R.string.maniFavor,name,mCards_size);
		progressBar.setVisibility(View.GONE);
		mLexiDB.lastAdded = false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(!initialized){
			opt = a.opt;
			imm = a.imm;
			if(opt.getIsCombinedSearching()) {
				toolbar_action1.setImageResource(R.drawable.ic_btn_multimode);
			}
			//((DefaultItemAnimator) lv.getItemAnimator()).setSupportsChangeAnimations(false);//取消更新item时闪烁
			fastScroller.setConservativeScroll(opt.getShelfStrictScroll());
			if(opt.getScrollShown()) {
				tg2.setChecked(true);
				fastScroller.setVisibility(View.GONE);
			}
			mRestrictOnDeck = "";
			wahahaTextView.mR=main_clister_layout.getRootView();
			loadInAll(a);
			ColorMatrixColorFilter NEGATIVE = GlobalOptions.NEGATIVE;
			final boolean inDark=a.AppWhite==Color.BLACK;
			if(inDark) {
				main_clister_layout.setBackgroundColor(Color.BLACK);
				lv.setBackgroundColor(Color.BLACK);
				for(int i=0;i<sideBar.getChildCount();i++) {
					Drawable bg = sideBar.getChildAt(i).getBackground();
					if(bg!=null)
						bg.setColorFilter(NEGATIVE);
				}
				toolbar_action1.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
				toolbar_action1.getBackground().setColorFilter(NEGATIVE);
				bookmark.getBackground().setColorFilter(NEGATIVE);
				sideBar.setSCC(sideBar.ShelfDefaultGray=0xFF4F7FDF);
				counter.setTextColor(Color.WHITE);
			}
			else {
				for(int i=0;i<sideBar.getChildCount();i++) {
					Drawable bg = sideBar.getChildAt(i).getBackground();
					if(bg!=null)
						bg.setColorFilter(null);
				}
				bookmark.getBackground().setColorFilter(null);
			}
			SelectionMode = opt.getDBMode();
			main_clister_layout.post(() -> sideBar.setRbyPos(opt.getDBMode()));
			if(opt.getDBMode()==3 && opt.getInRemoveMode()) {
				sideBar.setSCC(opt.getInRemoveMode()?getResources().getColor(R.color.ShallowHeaderBlue):sideBar.ShelfDefaultGray);
			}
			if(pendingDBClickPos!=-1){
				mainClicker.onItemClick(null, pendingDBClickPos);
				pendingDBClickPos=-1;
			}
			initialized = true;
		}
		else{
			boolean pull = mLexiDB==null || getAutoRefreshOnAttach() && mLexiDB.lastAdded
					|| /*保证一致性*/ getFragmentId()==1 && a.favoriteCon!=mLexiDB;
			if(pull)
				loadInAll(a);
		}
	}

	protected boolean getAutoRefreshOnAttach() {
		return true;
	}

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
					for(int position1 :Selection) {//移动
						String text;long time = 0;
						ItemCard mItemcard = mCards.get(position1);
						if(mItemcard==null) {
							cr.moveToPosition(position1);
							try {
								text=cr.getString(0);
								time=cr.getLong(1);
							} catch (Exception e) {
								text="!!!Error: "+e.getLocalizedMessage();
							}
						}else{
							text=mItemcard.name;
							time=mItemcard.time;
						}
						preparedInsertExecutor.bindString(1, text);
						preparedInsertExecutor.bindLong(2, time);
						if(preparedInsertExecutor.executeInsert()!=-1) {
							preparedDeleteExecutor.bindString(1, text);
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
				rebuildCursor(a);
				counter.setText(0 +"/"+mCards_size);
			}).show();
	}

	//for main list
	//参见：live down
	//参见：P.L.O.D -> float search view -> HomeAdapter
	class main_list_Adapter extends RecyclerView.Adapter<MyViewHolder> implements IDragSelectAdapter
	{
		SimpleDateFormat date;
		Date day_;
		//构造
		main_list_Adapter(){
			date = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
			day_ = new Date();
		}

		@Override
		public int getItemCount()
		{
			return getDelayPullData()?itemCount+1:itemCount;
		}

		private OnItemClickListener mOnItemClickListener;
		private OnItemLongClickListener mOnItemLongClickListener;
		private OnLongClickListener longClicker = new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				return mOnItemLongClickListener.onItemLongClick(v, (Integer) v.getTag(R.id.position));
			}};

		//单机
		public void setOnItemClickListener(OnItemClickListener mOnItemClickListener)
		{
			this.mOnItemClickListener = mOnItemClickListener;
		}
		//长按
		public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener)
		{
			this.mOnItemLongClickListener = mOnItemLongClickListener;
		}

		//Create
		@NonNull
		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			MyViewHolder holder = new MyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.card_list_item, parent,false));
			//holder.setIsRecyclable(false);
			//if Recyclable, then setText in onBindViewHolder makes textviews unSelectable.
			//details on this bug:
			//https://blog.csdn.net/huawuque183/article/details/78563977
			//issue solved.
			holder.itemView.setTag(holder);
			holder.itemView.setOnLongClickListener(longClicker);
			holder.p.setOnLongClickListener(longClicker);
			return holder;
		}

		@Override
		public int getItemViewType(int position) {
			if(position==itemCount && getDelayPullData())
				return 1;
			return 0;
		}

		@Override
		public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
		{
			holder.itemView.setTag(R.id.position, position);
			holder.p.setTag(R.id.position, position);
			if(position==itemCount && getDelayPullData()){
				holder.itemView.getLayoutParams().height=-1;
				holder.itemView.setVisibility(View.GONE);
				return;
			}
			//if(true) return;
			String text;long time = 0;
			ItemCard mItemcard = mCards.get(position);
			if(mItemcard==null) {
				cr.moveToPosition(position);
				try {
					text=cr.getString(0);
					time=cr.getLong(1);
				} catch (Exception e) {
					text="!!!Error: "+e.getLocalizedMessage();
				}
				if(StoragePolicy==2) mCards.put(position,new ItemCard(text,time));
				//CMN.Log("putting new vals");
			}else{
				text=mItemcard.name;
				time=mItemcard.time;
			}

			holder.webView.setText(text.trim());

			PDICMainActivity a = (PDICMainActivity) getActivity();

			if(GlobalOptions.isDark) {
				if(holder.webView.getTextColors().getDefaultColor()!=a.AppBlack) {
					holder.itemView.findViewById(R.id.sub_list).getBackground().setColorFilter(GlobalOptions.NEGATIVE);
					holder.webView.setTextColor(a.AppBlack);
				}
			}

			if(time==0)
				holder.time.setText("N.A.");
			else {
				day_.setTime(time);
				holder.time.setText(date.format(day_));
			}

			if(Selection.contains(position))//
				holder.itemView.setBackgroundColor(GlobalOptions.isDark?0xFF4F7FDF:0xa04F5F6F);//FF4081 4F7FDF
			else
				holder.itemView.setBackgroundColor(0x00a0f0f0);//aaa0f0f0

			if(inSearch && mSearchResTree!=null && mSearchResTree.contains(position))
				holder.webView.setBackgroundResource(R.drawable.xuxian2);
			else
				holder.webView.setBackground(null);


			if(SelectionMode==SelectionMode_select) {
				holder.p.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, position));
				holder.p.setVisibility(View.VISIBLE);
			}else{
				holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, position));
				holder.p.setVisibility(View.GONE);
			}
			holder.p.setTag(position);
		}

		@Override
		public void setSelected(int index, boolean selected) {
			boolean alreadyselected=Selection.contains(index);
			boolean needUpdate = false;
			if(opt.getInRemoveMode()) {//bIsInverseSelecting
				if(selected && alreadyselected) {
					Selection.remove(index);
					needUpdate=true;
				}else if(!selected && !alreadyselected) {
					Selection.add(index);
					needUpdate=true;
				}
			}else {
				if(selected && !alreadyselected) {
					Selection.add(index);
					needUpdate=true;
				}else if(!selected && alreadyselected) {
					Selection.remove(index);
					needUpdate=true;
				}
			}


			if(needUpdate) {
				notifyItemChanged(index);
				counter.setVisibility(View.VISIBLE);
				counter.setText(Selection.size()+"/"+mCards_size);
			}
		}

		@Override
		public boolean isIndexSelectable(int index) {
			return true;
		}

		//@Override
		//public boolean isIndexSelected(int index) {
		//	return true;
		//}
	}

	protected boolean getDelayPullData() {
		return opt.getDatabaseDelayPullData();
	}


	public interface OnItemClickListener{
		void onItemClick(View view,int position);}
	public interface OnItemLongClickListener{
		boolean onItemLongClick(View view,int position);}

	class MyViewHolder extends ViewHolder
	{
		TextView webView;
		TextView time;
		View p;
		public MyViewHolder(View view)
		{
			super(view);
			p = view.findViewById(R.id.p);
			webView = view.findViewById(android.R.id.text1);
			time = view.findViewById(R.id.subtext1);
			webView.setTextIsSelectable(true);
		}
	}

	RashSet<Integer> mSearchResTree = new RashSet<>();

	class SearchCardsHandler2 extends AsyncTask<String,Integer,Void>{
		SearchCardsHandler2(){
		}

		@Override
		protected Void doInBackground(String... keys) {
			String key = keys[0];
			mSearchResTree.clear();
			for(int position=0;position<mCards_size;position++) {
				String text;
				ItemCard mItemcard = mCards.get(position);
				if(mItemcard==null) {
					cr.moveToPosition(position);
					try {
						text=cr.getString(0);
					} catch (Exception e) {
						text="!!!Error: "+e.getLocalizedMessage();
					}
					if(StoragePolicy==2) mCards.put(position,new ItemCard(text,cr.getLong(1)));
					//CMN.Log("putting new vals");
				}else{
					text=mItemcard.name;
				}

				if(text.contains(key)) {
					mSearchResTree.insert(position);
				}
			}

			return null;
		}

		@Override
		public void onProgressUpdate(Integer... values) {}


		@Override
		public void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}


		@Override
		public void onPostExecute(Void result) {
			progressBar.setVisibility(View.GONE);
			if(mSearchResTree.getRoot()!=null)
				lm.scrollToPositionWithOffset(mSearchResTree.minimum(), toolbar.getHeight());
			fastScroller.setTree(mSearchResTree);
			fastScroller.timeLength = mCards_size;
			fastScroller.invalidate();
			mAdapter.notifyDataSetChanged();
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
	final static int SelectionMode_pan=0;
	final static int SelectionMode_peruseview=1;
	final static int SelectionMode_txtdropper=2;
	final static int SelectionMode_select=3;
	/*神之显/隐体系*/
	int revertage=0;
	boolean should_hide_cd1,should_hide_cd2;
	private boolean isDirty;
	/*hide/show system by a God*/
	//click
	@Override
	public void onClick(final View v) {
		int pos;
		int offset;
		String msg=null;
		switch(v.getId()) {
			case R.id.toolbar_action1:
				if(!opt.getSelection_Persists())
					Selection.clear();
				if(opt.setIsCombinedSearching(!opt.getIsCombinedSearching())) {
					toolbar_action1.setImageResource(R.drawable.ic_btn_multimode);
				}else {
					toolbar_action1.setImageResource(R.drawable.ic_btn_siglemode);
				}
				isDirty=true;
				break;
			case R.id.tools0://pan
				if(!opt.getSelection_Persists())
					Selection.clear();
				opt.setDBMode(0);
				sideBar.setSCC(sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode=SelectionMode_pan;
				sideBar.setRbyView(v);
				mAdapter.notifyDataSetChanged();
				if(v.getTag()==null)
					msg = "点击查词模式";
				else
					v.setTag(null);
				isDirty=true;
				break;
			case R.id.tools1://选择模式
				opt.setDBMode(3);
				sideBar.setSCC(opt.getInRemoveMode()?getResources().getColor(R.color.ShallowHeaderBlue):sideBar.ShelfDefaultGray);
				//if(lastFallBackTarget!=SelectionMode_select)
				//	lastFallBackTarget=SelectionMode;
				lastFallBackTarget=-100;
				if(SelectionMode!=SelectionMode_select) {
					SelectionMode=SelectionMode_select;
					sideBar.setRbyView(v);
					mAdapter.notifyDataSetChanged();
					if(counter.getVisibility()!=View.VISIBLE) {
						counter.setText(Selection.size()+"/"+mCards_size);
						counter.setVisibility(View.VISIBLE);
					}
					if(v.getTag()==null)
						msg = "选择模式";//
					else
						v.setTag(null);
				}
				isDirty=true;
				break;
			case R.id.tools001://peruse
				if(!opt.getSelection_Persists())
					Selection.clear();
				opt.setDBMode(1);
				sideBar.setSCC(sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode=SelectionMode_peruseview;
				sideBar.setRbyView(v);
				mAdapter.notifyDataSetChanged();
				if(v.getTag()==null)
					msg = "点击翻阅模式";
				else
					v.setTag(null);
				isDirty=true;
				break;
			case R.id.toolbar_action2://eyedropper
				opt.setDBMode(2);
				sideBar.setSCC(sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode=SelectionMode_txtdropper;
				sideBar.setRbyView(v);
				mAdapter.notifyDataSetChanged();
				if(v.getTag()==null)
					msg = "取词模式";
				else
					v.setTag(null);
				isDirty=true;
				break;
			case R.id.tools3://删除
				if(Selection.size()==0) {
					show(R.string.noseletion);
					return;
				}
				//no possible ways to detect keyboard hiden,when you hide it by the top-right button.
				//hate hate hate
				//strange strange strange
				//dmAroid dmAroid dmAroid
				//v.requestFocus();//dis-focus searchView
				//boolean hasKeyBoard = imm.isActive(searchView.findViewById(R.id.search_src_text));
				//CMN.show(""+hasKeyBoard);
				//if(!hasKeyBoard) searchView.clearFocus();//give some respect to the keyboard shown
				//卧槽草泥马的 AlertDialog 不要碰我的输入法 不要碰我的输入法 不要碰我的输入法
				//算了。。
				final boolean hasKeyBoard = imm.hideSoftInputFromWindow(searchView.getWindowToken(),0);
				//CMN.show(""+hasKeyBoard);
				if(!hasKeyBoard) searchView.clearFocus();
				AlertDialog d = new AlertDialog.Builder(getActivity())
						.setMessage(getResources().getString(R.string.warn_delete, Selection.size()))
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

							String sql = "delete from t1 where lex = ? ";
							SQLiteStatement preparedDeleteExecutor = mLexiDB.getDB().compileStatement(sql);
							mLexiDB.getDB().beginTransaction();  //开启事务

							try {
								//List delList = new ArrayList();
								for(int position:Selection) {//移动
									String text;
									ItemCard mItemcard = mCards.get(position);
									if(mItemcard==null) {
										cr.moveToPosition(position);
										try {
											text=cr.getString(0);
										} catch (Exception e) {
											text="!!!Error: "+e.getLocalizedMessage();
										}
									}else{
										text=mItemcard.name;
									}

									preparedDeleteExecutor.bindString(1, text);
									if(preparedDeleteExecutor.executeUpdateDelete()!=-1) {
										//delList.add(position);
										mCards.remove(position);
										mCards_size--;
									}
								}
								Selection.clear();
								//delList.clear();

								preparedDeleteExecutor.close();
								mLexiDB.getDB().setTransactionSuccessful();  //控制回滚
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								mLexiDB.getDB().endTransaction();  //事务提交
								cr.close();
								cr = mLexiDB.getDB().query("t1", null,null,null,null,null,"date desc");
								mCards.clear();
								mAdapter.notifyDataSetChanged();
								counter.setText(Selection.size()+"/"+mCards_size);
							}

							mAdapter.notifyDataSetChanged();
						}).setOnDismissListener(dialog -> {
							if(hasKeyBoard) {
								imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
							}
						}).show();
				d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
				break;
			case R.id.choosed:{//choose deck
				if(System.currentTimeMillis()-last_listHolder_tt<150 && revertage==1) {//too fast from last hide
					should_hide_cd1=true;
				}
				MainActivityUIBase a = (MainActivityUIBase) getActivity();
				a.showChooseFavorDialog(1);
			} break;
			case R.id.changed:{//change deck
				if(System.currentTimeMillis()-last_listHolder_tt<150 && revertage==2) {//too fast from last hide
					should_hide_cd2=true;
				}
				MainActivityUIBase a = (MainActivityUIBase) getActivity();
				a.showChooseFavorDialog(2);
			} break;
			case R.id.search:
				toolbar.setVisibility(toolbar.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
				if(toolbar.getVisibility()==View.VISIBLE) {
					inSearch=true;
					searchView.requestFocus();
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				}else {
					//automatically hides im ,GREAT!
					inSearch=false;
				}
				mAdapter.notifyDataSetChanged();//TODO min DB IO
				fastScroller.showBoolMark(inSearch);
				break;
			case R.id.browser_widget14:
				if(mSearchResTree==null || mSearchResTree.getRoot()==null) {}
				else {
					lv.scrollBy(0, +toolbar.getHeight());
					RBTNode<Integer> searchTmp = mSearchResTree.xxing_samsara(lm.findFirstVisibleItemPosition());
					if(searchTmp==null) {
						show(R.string.endendr);
						lv.scrollBy(0, -toolbar.getHeight());
						break;
					}
					pos = searchTmp.getKey();
					lm.scrollToPositionWithOffset(pos, toolbar.getHeight());
					break;
				}
			case R.id.lst_plain:
				//show("lll");
				offset = 0;
				if(inSearch) {
					lv.scrollBy(0, offset = toolbar.getHeight());
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
					lv.scrollBy(0, +toolbar.getHeight());
					RBTNode<Integer> searchTmp1 = mSearchResTree.sxing_samsara(lm.findFirstVisibleItemPosition());
					if(searchTmp1==null) {
						show(R.string.endendr);
						lv.scrollBy(0, -toolbar.getHeight());
						break;
					}
					pos = searchTmp1.getKey();
					lm.scrollToPositionWithOffset(pos, toolbar.getHeight());
					break;
				}
			case R.id.nxt_plain:
				//show("nnn");
				offset = 0;
				if(inSearch) {
					lv.scrollBy(0, offset = toolbar.getHeight());
				}
				pos = lm.findFirstVisibleItemPosition()+1;
				if(pos>=mAdapter.getItemCount()) {
					show(R.string.endendr);
				}else
					lm.scrollToPositionWithOffset(pos,offset);
				break;
		}

		if(msg!=null) {
			((MainActivityUIBase)getActivity()).showTopSnack(snack_root, msg, 0.5f, -1, -1, false);
		}

	}


	private void try_exit_selection() {
		if(SelectionMode==SelectionMode_select)
			if(!opt.getSelection_Persists()) {//清空选择
				Selection.clear();
				counter.setText(Selection.size()+"/"+mCards_size);
				counter.setVisibility(View.GONE);
			}
	}
	//lazy strategy. reuse as much as I can.
	PopupWindow sharePopup;
	int menuResId = -1;
	int onclickBase=0;
	int lastPopupId=-1;
	ArrayAdaptermy<String> shareListAda;
	void initPopup(){
		View view = getActivity().getLayoutInflater().inflate(R.layout.popup_more_tools, null);
		sharePopup = new PopupWindow(view,
				(int)(160 * getResources().getDisplayMetrics().density), LayoutParams.WRAP_CONTENT);

		sharePopup.setOnDismissListener(() -> {
		});
		final ListView shareList = view.findViewById(R.id.share_list);
		shareListAda = new ArrayAdaptermy<>(getActivity(),
				R.layout.popup_list_item);
		shareList.setAdapter(shareListAda);
		shareList.setOnItemClickListener((parent, view1, position, id) -> {
			PDICMainActivity a = (PDICMainActivity) getActivity();
			if(a==null) return;
			switch(position+onclickBase) {//处理点击事件
				case 10://全选
					//TODO develop more efficient and elegant algorithm.
					for(int i=0;i<mCards_size;i++) {
						Selection.add(i);
					}
					counter.setText(Selection.size()+"/"+mCards_size);
					mAdapter.notifyDataSetChanged();
					break;
				case 11:
					Selection.clear();
					counter.setText(Selection.size()+"/"+mCards_size);
					mAdapter.notifyDataSetChanged();
					break;
				case 12://反选
					for(int i=0;i<mCards_size;i++) {
						if(!Selection.remove(i))
							Selection.add(i);
					}
					counter.setText(Selection.size()+"/"+mCards_size);
					mAdapter.notifyDataSetChanged();
					break;
				case 13://反向选择-toggle
					if(opt.toggleInRemoveMode()) {
						//main_clister_layout.findViewById(R.id.tools1).getBackground().setColorFilter(Color.parseColor("#FF4081"), PorterDuff.Mode.SRC_IN);
						sideBar.setSCC(getResources().getColor(R.color.ShallowHeaderBlue));
					}else {
						//main_clister_layout.findViewById(R.id.tools1).getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
						sideBar.setSCC(sideBar.ShelfDefaultGray);
					}
					isDirty=true;
					break;
				case 30://show all decks
					break;
				case 31://刷新
					break;
				case 40://更新书签
					if(mCards.size()<1) break;
					if(cr!=null && lm.findFirstVisibleItemPosition()>=1 && mCards_size>=0) {
						if(lv.getChildAt(0)!=null) {
							String key = ((TextView) lv.getChildAt(0).findViewById(android.R.id.text1)).getText().toString();
							if(mLexiDB.updateBookMark(key)!=-1)
								show(R.string.bookmarkupdated, key);
						}
					}
					break;
				case 41://跳转书签
					String key = mLexiDB.getLastBookMark();
					if(key!=null) {
						int cc=0;
						for(int i=0;i<mCards_size;i++) {
							String text;
							ItemCard mItemcard = mCards.get(position);
							if(mItemcard==null) {
								cr.moveToPosition(position);
								try {
									text=cr.getString(0);
								} catch (Exception e) {
									text="!!!Error: "+e.getLocalizedMessage();
								}
								if(StoragePolicy==2 || StoragePolicy==1) mCards.put(position,new ItemCard(text,cr.getLong(1)));
								//CMN.Log("putting new vals");
							}else{
								text=mItemcard.name;
							}


							if(text.equals(key)) {
								lm.scrollToPositionWithOffset(cc, 0);
								show(R.string.bookmarkjumped, key);
								return;
							}
							cc++;
						}


					}
					show(R.string.bookmarkfailed, key);
					break;
				case 50://严格模式
					fastScroller.setConservativeScroll(true);
					opt.setShelfStrictScroll(true);
					isDirty=true;
					break;
				case 51://宽松模式
					fastScroller.setConservativeScroll(false);
					opt.setShelfStrictScroll(false);//.putBoolean("strictscroll", false);
					isDirty=true;
					break;
			}
			TextView tv = (TextView) view1;
			tv.setText(tv.getText()+"...");
			getActivity().getWindow().getDecorView().postDelayed(() -> sharePopup.dismiss(), 150);
		});
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
				break;
			case R.id.choosed:
				menuResId=R.array.choosed_tweak;
				onclickBase=30;
				interceptClick=true;
				break;
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
		if(sharePopup!=null && sharePopup.isShowing()) {
			sharePopup.dismiss();
			return true;
		}
		if(sharePopup==null)
			initPopup();

		if(lastPopupId!=v.getId()) {//need re-populate
			shareListAda.setArray(Arrays.asList(getResources().getStringArray(menuResId)));
			lastPopupId=v.getId();
		}

		sharePopup.setFocusable(false);
		sharePopup.setOutsideTouchable(true);
		sharePopup.setBackgroundDrawable(null);
		sharePopup.showAsDropDown(v, v.getWidth(), -v.getHeight());
		return interceptClick;
	}

	TextView toastTv;
	View toastV;
	AlphaAnimation fadeAnima;
	public void show(int ResId,Object...args) {
		if(getActivity()!=null)
			show(getResources().getString(ResId,args));
	}
	boolean maskOn;
	float inputBase=-1;
	public resultRecorderCombined rec;
	public void show(String text)
	{
		if(getActivity()==null) return;
		if(toastV != null) {}
		else {
			toastV = main_clister_layout.findViewById(R.id.toast_layout_rootmy);//a.getLayoutInflater().inflate(R.layout.toast,null);
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

	OnItemClickListener mainClicker = new OnItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			PDICMainActivity a = (PDICMainActivity) getActivity();
			if(a==null) return;
			a.setContentBow(false);
			if(view!=null) {
				adelta=0;
				//TODO retrieve from sibling views
				currentDisplaying = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
			}
			else {
				String text = null;long time = 0;
				ItemCard mItemcard = mCards.get(position);
				if(mItemcard==null) {
					cr.moveToPosition(position);
					try {
						currentDisplaying=cr.getString(0);
						time=cr.getLong(1);
					} catch (Exception e) {
						currentDisplaying="!!!Error: "+e.getLocalizedMessage();
					}
					if(StoragePolicy==2) mCards.put(position,new ItemCard(text,time));
				}else{
					currentDisplaying=mItemcard.name;
					time=mItemcard.time;
				}
			}

			int lastClickedPosBeforePageTurn = position - adelta;
			ScrollerRecord pagerec=null;
			currentPos = position;

			switch(SelectionMode) {
				case SelectionMode_select:{
					if(!Selection.remove(position)) {
						Selection.add(position);
					}
					counter.setText(Selection.size()+"/"+mCards_size);
					counter.setVisibility(View.VISIBLE);
					mAdapter.notifyItemChanged(position);
				} break;
				case SelectionMode_pan:{
					if(opt.getIsCombinedSearching()) {
						ArrayList<Integer> records = new ArrayList<>();
						additiveMyCpr1 datalet = new additiveMyCpr1(currentDisplaying,records);
						ArrayList<additiveMyCpr1> data = new ArrayList<>();
						data.add(datalet);
						String currentDisplaying__ = mdict.replaceReg.matcher(currentDisplaying).replaceAll("").toLowerCase();
						a.bShowLoadErr=false;
						for(int dIdx=0;dIdx<a.md.size();dIdx++) {//联合搜索
							mdict mdTmp = a.md_get(dIdx);
							if(mdTmp!=null) {
								int idx = mdTmp.lookUp(currentDisplaying__);
								if (idx >= 0)
									while (idx < mdTmp.getNumberEntries()) {
										if (mdict.replaceReg.matcher(mdTmp.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
											records.add(dIdx);
											records.add(idx);
										} else
											break;
										idx++;
									}
							}
						}
						a.bShowLoadErr=true;
						webviewHolder = a.webholder;
						ViewGroup anothorHolder = a.webSingleholder;
						if(records.size()>0) {
							a.recCom = rec = new resultRecorderCombined(a,data,a.md);
							ScrollViewmy WHP = a.WHP;
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

							if(WHP.getVisibility()!=View.VISIBLE) WHP.setVisibility(View.VISIBLE);
							if(anothorHolder.getVisibility()==View.VISIBLE) {
								if(anothorHolder.getChildCount()!=0)
									anothorHolder.removeAllViews();
								anothorHolder.setVisibility(View.GONE);
							}

							a.widget13.setVisibility(View.VISIBLE);
							a.widget14.setVisibility(View.VISIBLE);
							a.contentview.setVisibility(View.VISIBLE);
							ViewGroup somp = (ViewGroup) a.contentview.getParent();
							if(somp!=a.main){
								if(somp!=null) somp.removeView(a.contentview);
								a.main.addView(a.contentview);
							}
							imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);

							rec.renderContentAt(0, a,null);

							processFavorite(position, currentDisplaying);
						}
						else {
							if(a.main.getChildCount()==1) {
								show(R.string.searchFailed, currentDisplaying);
							}else {
								a.show(R.string.searchFailed, currentDisplaying);
								webviewHolder.removeAllViews();
								int remcount = anothorHolder.getChildCount()-1;
								if(remcount>0) anothorHolder.removeViews(1, remcount);
							}
						}
					}
					else {
						//CMN.Log("单独搜索模式");
						float desiredScale=-1;
						a.TransientIntoSingleExplanation();

						String key = currentDisplaying;
						int offset = mdict.offsetByTailing(key);
						int idx;
						if(offset>0)
							key = key.substring(0,key.length()-offset);
						mdict currentDictionary = a.currentDictionary;
						idx = currentDictionary.lookUp(key,true);
						int adapter_idx=a.adapter_idx;
						if(idx<0) {
							for(adapter_idx=0;adapter_idx<a.md.size();adapter_idx++) {
								if(adapter_idx!=a.adapter_idx) {
									currentDictionary=a.md_get(adapter_idx);
									if(currentDictionary!=null)
										idx=currentDictionary.lookUp(key,true);
									if(idx>=0) break;
								}
							}
						}

						webviewHolder = a.webSingleholder;
						ViewGroup anothorHolder = a.webholder;
						if(idx>=0){
							if(opt.getRemPos()) {
								currentDictionary.initViewsHolder(a);
								currentDictionary.rl.setTag(adapter_idx);
								OUT:
								if(System.currentTimeMillis()-a.lastClickTime>300)//save our postion
								if(webviewHolder.getChildCount()!=0) {
									View s_rl = webviewHolder.getChildAt(0);
									int tag= IU.parsint(s_rl.getTag(), -1);
									if(tag!=-1) {
										mdict lastDictionary = a.md_get(tag);
										if(lastDictionary!=null) {
											WebViewmy current_webview = lastDictionary.mWebView;
											if (adelta != 0 && current_webview != null && !current_webview.isloading) {
												if (lastDictionary.webScale == 0)
													lastDictionary.webScale = a.dm.density;//sanity check
												//CMN.Log("保存位置", lastDictionary._Dictionary_fName, tag);

												pagerec = avoyager.get(lastClickedPosBeforePageTurn);
												if (pagerec == null) {
													if (current_webview.getScrollX() != 0 || current_webview.getScrollY() != 0 || currentDictionary.webScale != mdict.def_zoom) {
														pagerec = new ScrollerRecord();
														avoyager.put(lastClickedPosBeforePageTurn, pagerec);
													} else
														break OUT;
												}

												pagerec.set(current_webview.getScrollX(), current_webview.getScrollY(), lastDictionary.webScale);
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
							}else {
								currentDictionary.mWebView.expectedPos=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
								currentDictionary.mWebView.expectedPosX=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
							}

							imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
							ViewGroup somp = (ViewGroup) a.contentview.getParent();
							if(somp!=a.main){
								if(somp!=null) somp.removeView(a.contentview);
								a.main.addView(a.contentview);
							}

							if(offset>0)//apply tailing offset
								if(currentDictionary.getEntryAt(idx+offset).equals(key))
									idx+=offset;
							int tmpIdx=idx;
							while(tmpIdx+1<currentDictionary.getNumberEntries() && mdict.processText(currentDictionary.getEntryAt(tmpIdx)).equals(mdict.processText(currentDictionary.getEntryAt(tmpIdx+1)))) {
								tmpIdx++;
								if(currentDictionary.getEntryAt(tmpIdx).trim().equals(key.trim())) {
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

							currentDictionary.renderContentAt(desiredScale,adapter_idx,0,null, idx);

							currentDictionary.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
							currentDictionary.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
							processFavorite(position, currentDisplaying);
						}
						else {
							if(a.main.getChildCount()==1) {
								show(R.string.searchFailed, currentDisplaying);
							}else {
								a.show(R.string.searchFailed, currentDisplaying);
								anothorHolder.removeAllViews();
								int remcount = webviewHolder.getChildCount()-1;
								if(remcount>0) webviewHolder.removeViews(1, remcount);
							}
						}
					}
				} break;
				case SelectionMode_peruseview:{
					ArrayList<Integer> records = new ArrayList<>();
					additiveMyCpr1 datalet = new additiveMyCpr1(currentDisplaying,records);
					ArrayList<additiveMyCpr1> data = new ArrayList<>();
					data.add(datalet);
					String currentDisplaying__ = mdict.replaceReg.matcher(currentDisplaying).replaceAll("").toLowerCase();
					boolean reorded=false;
					for(int i=0;i<a.md.size();i++) {//联合搜索
						int dIdx=i;
						if(opt.getPeruseAddAll()){
							records.add(dIdx);
							continue;
						}
						if(!opt.getIsCombinedSearching()) {
							if(dIdx==0)if(a.adapter_idx>0 && a.adapter_idx<a.md.size()) {
								dIdx=a.adapter_idx;
								reorded=true;
							}else if(reorded) if(dIdx<=a.adapter_idx) {
								dIdx-=1;
							}
						}
						mdict mdTmp = a.md_get(dIdx);
						if(mdTmp!=null) {
							int idx = mdTmp.lookUp(currentDisplaying__);
							if (idx >= 0)
								while (idx < mdTmp.getNumberEntries()) {
									if (mdict.replaceReg.matcher(mdTmp.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
										records.add(dIdx);
									} else
										break;
									idx++;
								}
						}
					}
					a.getPeruseView().data = records;
					a.getPeruseView().TextToSearch = currentDisplaying;
					a.AttachPeruseView(true);
				} break;
				case SelectionMode_txtdropper:{
					a.lastEtString=a.etSearch.getText().toString();
					a.etSearch.setText(currentDisplaying);
					a.etSearch_ToToolbarMode(2);
					a.getSupportFragmentManager().beginTransaction().remove(DBroswer.this).commit();
					a.DBrowser=null;
					if(isDirty) {
						opt.putFirstFlag();
						//CMN.Log("DBROWSER写配置……");
					}
				} break;
			}
		}};


	public void toggleFavor() {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		a.favoriteBtn.setImageResource(R.drawable.star_ic);
		if(toDelete.get(currentPos)==null) {
			a.favoriteBtn.setImageResource(R.drawable.star_ic);
			toDelete.put(currentPos,currentDisplaying);
			isToDel=true; a.show(R.string.toRemove);
		}else {
			toDelete.remove(currentPos);
			a.favoriteBtn.setImageResource(R.drawable.star_ic_solid);
			a.show(R.string.added);
		}
	}


	protected void processFavorite(int position,String key) {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		if(toDelete.get(currentPos)==null) {
			a.favoriteBtn.setImageResource(R.drawable.star_ic_solid);
		}else
			a.favoriteBtn.setImageResource(R.drawable.star_ic);
	}

	public void goBack() {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		if(opt.getBottomNavigationMode()==0) {
			if (currentPos - 1 < 0) {
				a.showTopSnack(a.main_succinct, R.string.endendr, -1, -1, -1, false);
				return;
			}
			//int first = lm.findFirstVisibleItemPosition();
			if (currentPos < lm.findFirstVisibleItemPosition())
				lm.scrollToPositionWithOffset(currentPos, 0);
			adelta = -1;
			mainClicker.onItemClick(null, --currentPos);
		} else {
			a.GoBackOrForward(webviewHolder, -1);
		}
	}

	public void goQiak() {
		PDICMainActivity a = (PDICMainActivity) getActivity();
		if(a==null) return;
		if(opt.getBottomNavigationMode()==0) {
			if (currentPos + 1 > mCards_size - 1) {
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
			mainClicker.onItemClick(null, currentPos);
		} else {
			a.GoBackOrForward(webviewHolder, 1);
		}
	}

	class ItemCard {
		String name;
		long time;


		public ItemCard(String text, long time_) {
			time=time_;
			name=text;
		}

	}

	public int getFragmentId() {
		return 1;
	}
}

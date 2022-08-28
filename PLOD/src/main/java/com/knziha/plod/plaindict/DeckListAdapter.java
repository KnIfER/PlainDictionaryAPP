package com.knziha.plod.plaindict;

import static com.knziha.plod.db.LexicalDBHelper.FIELD_VISIT_TIME;
import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.dragselectrecyclerview.IDragSelectAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.knziha.ankislicer.customviews.WahahaTextView;
import com.knziha.paging.AppIconCover.AppIconCover;
import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReader;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.paging.PagingCursorAdapter;
import com.knziha.plod.PlainUI.AppInfoDBBean;
import com.knziha.plod.plaindict.databinding.CardListItemBinding;
import com.knziha.plod.widgets.RecyclerViewmy;
import com.knziha.plod.widgets.ViewUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

//for main list
//参见：live down
//参见：P.L.O.D -> float search view -> HomeAdapter  main_list_Adapter
class DeckListAdapter extends RecyclerView.Adapter<ViewUtils.ViewDataHolder<CardListItemBinding>> implements IDragSelectAdapter, PagingCursorAdapter.OnLoadListener {
	@NonNull WeakReference<DBroswer> browserHolder;
	public final static int DB_FAVORITE = 1;
	public final static int DB_HISTORY = 2;
	final static int SelectionMode_pan=0;
	final static int SelectionMode_peruseview=1;
	final static int SelectionMode_fetchWord =2;
	final static int SelectionMode_select=3;
	ConstructorInterface<HistoryDatabaseReader> HistoryDatabaseReaderConstructor = length -> new DeckListAdapter.HistoryDatabaseReader();
	private RequestBuilder<Drawable> iconLoader;
	
	public final WahahaTextView.ViewRootHolder viewRootHolder = new WahahaTextView.ViewRootHolder();
	
	public interface OnItemLongClickListener{
		boolean onItemLongClick(View view,int position);
	}
	public static class HistoryDatabaseReader implements CursorReader {
		long row_id;
		long sort_number;
		String books;
		String record;
		String time_text;
		long ivk;
		@Override
		public void ReadCursor(Cursor cursor, long rowID, long sortNum) {
			record = cursor.getString(2);
			books = cursor.getString(3);
			ivk = cursor.getLong(4);
			//CMN.Log("ReadCursor::ivk::", record, ivk); null return zero
			if (rowID!=-1) {
				row_id = rowID;
				sort_number = sortNum;
			} else {
				row_id = cursor.getLong(0);
				sort_number = cursor.getLong(1);
			}
		}
		
		@Override
		public String toString() {
			return "WebAnnotationCursorReader{" +
					"lex='" + record + '\'' +
					'}';
		}
	}
	
	final MainActivityUIBase a;
	private final PackageManager pm;
	
	static class DeckListData{
		public @NonNull PagingAdapterInterface<HistoryDatabaseReader> dataAdapter;
		public PagingAdapterInterface<HistoryDatabaseReader> searchedData;
		int[] lvPos = new int[4];
		int type;
		long fid;
		long ver=-1;
		DeckListData(int type) {
			dataAdapter = new CursorAdapter<>(EmptyCursor, new HistoryDatabaseReader());
			this.type = type;
		}
		public void close() {
			if(searchedData!=null) dataAdapter.close();
			if(searchedData!=null) searchedData.close();
		}
	}
	
	@NonNull PagingAdapterInterface<HistoryDatabaseReader> displaying;
	@NonNull DeckListData data;
	
	SimpleDateFormat date;
	Date day_;
	
	//构造
	DeckListAdapter(MainActivityUIBase a, DBroswer broswer){
		date = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
		day_ = new Date();
		this.a = a;
		browserHolder = new WeakReference<>(broswer);
		resetDataCache(broswer.type);
		pm = a.getPackageManager();
	}
	
	public boolean resetDataCache(int type) {
		if(data==null || data.type!=type) {
			int idx=type-1;
			data = a.DBrowserDatas[idx];
			if(data==null || a.opt.debuggingDBrowser()>0) {
				data = a.DBrowserDatas[idx] = new DeckListData(type);
			}
			displaying = data.dataAdapter;
			return true;
		}
		return false;
	}
	
	public void setFragment(DBroswer broswer) {
		browserHolder = new WeakReference<>(broswer);
	}
	
	public void close() {
		data.close();
	}
	
	@Override
	public int getItemCount()
	{
		return displaying.getCount();
	}
	
	private RecyclerViewmy.OnItemClickListener mOnItemClickListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	private OnLongClickListener longClicker = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			//return mOnItemLongClickListener.onItemLongClick(v, (Integer) v.getTag(R.id.position));
			return true;
		}
	};

	//点击
	public void setOnItemClickListener(RecyclerViewmy.OnItemClickListener mOnItemClickListener)
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
	public ViewUtils.ViewDataHolder<CardListItemBinding> onCreateViewHolder(ViewGroup parent, int viewType)
	{
		ViewUtils.ViewDataHolder<CardListItemBinding> holder = new ViewUtils.ViewDataHolder<>(CardListItemBinding.inflate(a.getLayoutInflater(), parent, false));
		//holder.setIsRecyclable(false);
		//if Recyclable, then setText in onBindViewHolder makes textviews unSelectable.
		//details on this bug:
		//https://blog.csdn.net/huawuque183/article/details/78563977
		//issue solved.
		
//		CMN.Log("dbr_onCreateViewHolder", CMN.now()); // todo
		
		holder.itemView.setOnLongClickListener(longClicker);
		holder.data.text1.mR = viewRootHolder;
		holder.data.p.setOnLongClickListener(longClicker);

//			webView = view.findViewById(android.R.id.text1);
//			time = view.findViewById(R.id.subtext1);
		
		holder.colorStates=new int[3];
		return holder;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}
	
	@Override
	public void onBindViewHolder(@NonNull final ViewUtils.ViewDataHolder<CardListItemBinding> holder, int position)
	{
		CardListItemBinding viewdata = holder.data;
		//if(true) return;
		String text;long time, ivk=-1;
		
		long rowId = position;
		
		HistoryDatabaseReader reader = displaying.getReaderAt(position);
		holder.tag = reader;
		try {
			text=reader.record;
			time=reader.sort_number;
			rowId = reader.row_id;
			ivk = reader.ivk;
			String books = reader.books;
			day_.setTime(time);
			viewdata.subtext1.setText(date.format(day_) + "  " + a.retrieveDisplayingBooks(books));
		} catch (Exception e) {
			text="!!!Error: "+e.getLocalizedMessage();
		}

		if(ivk!=0 && PDICMainAppOptions.dbShowIcon()) {
			if(iconLoader==null) {
				RequestOptions options = new RequestOptions()
						.format(DecodeFormat.PREFER_ARGB_8888)//DecodeFormat.PREFER_ARGB_8888
						.skipMemoryCache(false)
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.fitCenter()
						.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
						;
				iconLoader = Glide.with(a)
						.load(new AppIconCover(new AppInfoDBBean(-1, pm), false))
						.apply(options);
			}
			try {
				iconLoader.load(new AppIconCover(new AppInfoDBBean(ivk, pm), false))
						.into(viewdata.icon);
			} catch (Exception e) {
				CMN.debug(ivk, text, e); //
			}
			viewdata.icon.setVisibility(View.VISIBLE); //todo optimize
		} else {
			viewdata.icon.setImageDrawable(null);
			viewdata.icon.setVisibility(View.INVISIBLE);
		}
		
		//viewdata.icon
		
		viewdata.text1.setTextIsSelectable(PDICMainAppOptions.dbTextSelectable());
		viewdata.text1.setText(text.trim());
		if(a.opt.debuggingDBrowser()>1) {
			viewdata.text1.setText(position+" ::"+text.trim());
			CMN.debug("onBindViewHolder::", position, text.trim(), ivk);
		}
		
		int textColor=a.AppBlack, backgroundColor=0;
		if(holder.colorStates[0]!=textColor){
			holder.itemView.findViewById(R.id.sub_list)
					.getBackground().setColorFilter(GlobalOptions.isDark?GlobalOptions.NEGATIVE:null);
			holder.colorStates[0]=textColor;
		}
		
		DBroswer browser = browserHolder.get();
		if(browser.Selection.contains(rowId)) {
			if(!GlobalOptions.isDark)textColor=a.AppWhite;
			backgroundColor=0xFF4F7FDF;//GlobalOptions.isDark?0xFF4F7FDF:0xa04F5F6F;
		}
		if (browser.type==DB_FAVORITE && browser.toDeleteV2.contains(rowId)) {
			viewdata.subList.setAlpha(0.5f);
			textColor = Color.GRAY;
		} else {
			viewdata.subList.setAlpha(1);
		}
		if(holder.colorStates[1]!=textColor) {
			viewdata.text1.setTextColor(holder.colorStates[1]=textColor);
			viewdata.subtext1.setTextColor(GlobalOptions.isDark||textColor==a.AppWhite?Color.WHITE:0xff2b4391);
		}
		if(holder.colorStates[2]!=backgroundColor) {
			holder.itemView.setBackgroundColor(holder.colorStates[2]=backgroundColor);//FF4081 4F7FDF
		}

//		if(browser.inSearch && browser.mSearchResTree!=null && browser.mSearchResTree.contains(position))
//			viewdata.text1.setBackgroundResource(R.drawable.xuxian2);
//		else
//			viewdata.text1.setBackground(null);

		if(browser.SelectionMode==SelectionMode_select) {
			viewdata.p.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, 0));
			viewdata.p.setVisibility(View.VISIBLE);
		} else {
			holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, 0));
			viewdata.p.setVisibility(View.GONE);
		}
	}

	@Override
	public void setSelected(int index, boolean selected) {
		long posId = displaying.getReaderAt(index).row_id;
		DBroswer browser = browserHolder.get();
		boolean alreadyselected=browser.Selection.contains(posId);
		boolean needUpdate = false;
		if(a.opt.getInRemoveMode()) {//bIsInverseSelecting
			if(selected && alreadyselected) {
				browser.Selection.remove(posId);
				needUpdate=true;
			} else if(!selected && !alreadyselected) {
				browser.Selection.add(posId);
				needUpdate=true;
			}
		} else {
			if(selected && !alreadyselected) {
				browser.Selection.add(posId);
				needUpdate=true;
			} else if(!selected && alreadyselected) {
				browser.Selection.remove(posId);
				needUpdate=true;
			}
		}

		if(needUpdate) {
			notifyItemChanged(index);
			browser.UIData.counter.setVisibility(View.VISIBLE);
			browser.UIData.counter.setText(browser.Selection.size()+"/"+ displaying.getCount());
		}
	}

	@Override
	public boolean isIndexSelectable(int index) {
		return true;
	}
	
	public HistoryDatabaseReader getReaderAt(int i) {
		return displaying.getReaderAt(i);
	}
	
	//@Override
	//public boolean isIndexSelected(int index) {
	//	return true;
	//}
	
	
	@Override
	public void onLoaded(PagingCursorAdapter adapter) {
		DBroswer browser = browserHolder.get();
		if (browser!=null) {
			browser.lv.suppressLayout(false);
		}
	}
	
	void rebuildCursor(MainActivityUIBase a) {
		boolean bSingleThreadLoadAll = false;
		DBroswer browser = browserHolder.get();
		SQLiteDatabase db = browser.mLexiDB.getDB();
		data.dataAdapter.close();
		if (bSingleThreadLoadAll) {
			Cursor cursor;
			if (browser.type==DB_FAVORITE) {
				cursor = db.rawQuery("SELECT id,"+FIELD_VISIT_TIME+",lex,books,ivk FROM "+browser.getTableName()+" where folder=? ORDER BY "+FIELD_VISIT_TIME+" desc", new String[]{a.opt.getCurrFavoriteNoteBookId()+""});
			} else {
				cursor = db.rawQuery("SELECT id,"+FIELD_VISIT_TIME+",lex,books,ivk FROM "+browser.getTableName()+" ORDER BY "+FIELD_VISIT_TIME+" desc", null);
			}
			CMN.Log("查询个数::"+cursor.getCount());
			data.dataAdapter = displaying = new CursorAdapter<>(cursor, new HistoryDatabaseReader());
			browser.notifyDataSetChanged();
		} else {
			if (browser.pageAsyncLoader==null) {
				browser.pageAsyncLoader = new ImageView(a);
			}
			PagingCursorAdapter<HistoryDatabaseReader> dataAdapter = new PagingCursorAdapter<>(db
					//, new SimpleClassConstructor<>(HistoryDatabaseReader.class)
					, HistoryDatabaseReaderConstructor
					, HistoryDatabaseReader[]::new);
			data.dataAdapter = displaying = dataAdapter;
			dataAdapter.bindTo(browser.lv)
					.setAsyncLoader(a, browser.pageAsyncLoader)
					.sortBy(browser.getTableName(), FIELD_VISIT_TIME, true, "lex, books, ivk");
			if (browser.getFragmentType()==DB_FAVORITE) {
				dataAdapter.where("folder=?", new String[]{a.opt.getCurrFavoriteNoteBookId()+""});
			}
			long[] pos = browser.savedPositions.get(browser.getFragmentType());
			long lastTm=0, offset=0;
			if (pos!=null) {
				lastTm = pos[0];
				offset = pos[1];
			}
			dataAdapter.startPaging(lastTm, offset, 20, 15, this);
			//CMN.debug("savedPositions::read::", browser.getFragmentType()+" "+new Date(lastTm).toLocaleString());
		}
		//CMN.Log("mAdapter.rebuildCursor!!!");
		//todo 记忆 lastFirst
//		int offset = 0;
//		lastFirst = 0;
//		if(true)
//		{
//			MyIntPair lcibdfn = ((AgentApplication) a.getApplication()).getLastContextualIndexByDatabaseFileName(mLexiDB.DATABASE);
//			if(lcibdfn!=null)
//			{
//				lastFirst = Math.min(lcibdfn.key, dataAdapter.getCount());
//				offset =  lcibdfn.value;
//			}
//		}
//		notifyDataSetChanged();
//		lm.scrollToPositionWithOffset(lastFirst,offset);
//		lm.setInitialPrefetchItemCount(10);
	}
}
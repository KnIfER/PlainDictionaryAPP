package com.knziha.plod.plaindict;

import static com.knziha.plod.db.LexicalDBHelper.FIELD_VISIT_TIME;
import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.dragselectrecyclerview.IDragSelectAdapter;
import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReader;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.paging.PagingCursorAdapter;
import com.knziha.plod.plaindict.databinding.CardListItemBinding;
import com.knziha.plod.widgets.RecyclerViewmy;
import com.knziha.plod.widgets.ViewUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

//for main list
//参见：live down
//参见：P.L.O.D -> float search view -> HomeAdapter  main_list_Adapter
class DeckListAdapter extends RecyclerView.Adapter<ViewUtils.ViewDataHolder<CardListItemBinding>> implements IDragSelectAdapter
{
	@NonNull WeakReference<DBroswer> browserHolder;
	public final static int DB_FAVORITE = 1;
	public final static int DB_HISTORY = 2;
	final static int SelectionMode_pan=0;
	final static int SelectionMode_peruseview=1;
	final static int SelectionMode_txtdropper=2;
	final static int SelectionMode_select=3;
	ConstructorInterface<HistoryDatabaseReader> HistoryDatabaseReaderConstructor = length -> new DeckListAdapter.HistoryDatabaseReader();
	
	public interface OnItemLongClickListener{
		boolean onItemLongClick(View view,int position);
	}
	public static class HistoryDatabaseReader implements CursorReader {
		long row_id;
		long sort_number;
		String books;
		String record;
		String time_text;
		@Override
		public void ReadCursor(Cursor cursor, long rowID, long sortNum) {
			record = cursor.getString(2);
			books = cursor.getString(3);
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
	}
	
	public boolean resetDataCache(int type) {
		if(data==null || data.type!=type) {
			int idx=type-1;
			data = a.DBrowserDatas[idx];
			if(data==null) {
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
			return mOnItemLongClickListener.onItemLongClick(v, (Integer) v.getTag(R.id.position));
		}
	};

	//单机
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
		ViewUtils.ViewDataHolder<CardListItemBinding> holder = new ViewUtils.ViewDataHolder<>(CardListItemBinding.inflate(LayoutInflater.from(a), parent, false));
		//holder.setIsRecyclable(false);
		//if Recyclable, then setText in onBindViewHolder makes textviews unSelectable.
		//details on this bug:
		//https://blog.csdn.net/huawuque183/article/details/78563977
		//issue solved.
		CMN.Log("dbr_onCreateViewHolder", CMN.now()); // todo
		holder.itemView.setOnLongClickListener(longClicker);
		holder.data.p.setOnLongClickListener(longClicker);
//			webView = view.findViewById(android.R.id.text1);
//			time = view.findViewById(R.id.subtext1);
		holder.data.text1.setTextIsSelectable(true);
		
		return holder;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}
	
	@Override
	public void onBindViewHolder(@NonNull final ViewUtils.ViewDataHolder<CardListItemBinding> holder, final int position)
	{
		holder.itemView.setTag(R.id.position, position);
		holder.data.p.setTag(R.id.position, position);
		//if(true) return;
		String text;long time = 0;
		
		long rowId = position;
		
		HistoryDatabaseReader reader = displaying.getReaderAt(position);
		holder.tag = reader;
		try {
			text=reader.record;
			time=reader.sort_number;
			rowId = reader.row_id;
			String books = reader.books;
			day_.setTime(time);
			holder.data.subtext1.setText(date.format(day_) + "  " + a.retrieveDisplayingBooks(books));
		} catch (Exception e) {
			text="!!!Error: "+e.getLocalizedMessage();
		}
		
		holder.data.text1.setText(text.trim());
		
		if(GlobalOptions.isDark) {
			if(holder.data.text1.getTextColors().getDefaultColor()!=a.AppBlack) {
				holder.itemView.findViewById(R.id.sub_list).getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				holder.data.text1.setTextColor(a.AppBlack);
			}
		}
		
		DBroswer browser = browserHolder.get();

		if(browser.Selection.contains(rowId))
			holder.itemView.setBackgroundColor(GlobalOptions.isDark?0xFF4F7FDF:0xa04F5F6F);//FF4081 4F7FDF
		else
			holder.itemView.setBackgroundColor(0x00a0f0f0);//aaa0f0f0

		if(browser.inSearch && browser.mSearchResTree!=null && browser.mSearchResTree.contains(position))
			holder.data.text1.setBackgroundResource(R.drawable.xuxian2);
		else
			holder.data.text1.setBackground(null);


		if(browser.SelectionMode==SelectionMode_select) {
			holder.data.p.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, position));
			holder.data.p.setVisibility(View.VISIBLE);
		}else{
			holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, position));
			holder.data.p.setVisibility(View.GONE);
		}
		holder.data.p.setTag(position);
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
	
	
	void rebuildCursor(MainActivityUIBase a) {
		boolean bSingleThreadLoading = false;
		DBroswer browser = browserHolder.get();
		SQLiteDatabase db = browser.mLexiDB.getDB();
		data.dataAdapter.close();
		if (bSingleThreadLoading) {
			Cursor cursor;
			if (browser.type==DB_FAVORITE) {
				cursor = db.rawQuery("SELECT id,"+FIELD_VISIT_TIME+",lex,books FROM "+browser.getTableName()+" where folder=? ORDER BY "+FIELD_VISIT_TIME+" desc", new String[]{a.opt.getCurrFavoriteNoteBookId()+""});
			} else {
				cursor = db.rawQuery("SELECT id,"+FIELD_VISIT_TIME+",lex,books FROM "+browser.getTableName()+" ORDER BY "+FIELD_VISIT_TIME+" desc", null);
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
					.sortBy(browser.getTableName(), FIELD_VISIT_TIME, true, "lex, books");
			if (browser.getFragmentType()==DB_FAVORITE) {
				dataAdapter.where("folder=?", new String[]{a.opt.getCurrFavoriteNoteBookId()+""});
			}
			dataAdapter.startPaging(browser.lastVisiblePositionMap.get(browser.getFragmentType(), 0L), 20, 15);
		}
		CMN.Log("mAdapter.rebuildCursor!!!");
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
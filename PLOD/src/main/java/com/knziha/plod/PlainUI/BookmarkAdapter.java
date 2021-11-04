package com.knziha.plod.PlainUI;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReader;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;

import db.LexicalDBHelper;

import static com.knziha.plod.widgets.Utils.EmptyCursor;

/**
 * Created by KnIfER on 2018/3/26.
 */

public class BookmarkAdapter extends BaseAdapter{
	BookPresenter presenter;
	boolean isWeb;
	public boolean showDelete;
	int resourceID;
	int textViewResourceID;
	Context a;
	public boolean darkMode;
	public final boolean testDBV2;
	
	private PagingAdapterInterface<BookmarkDatabaseReader> DummyReader = new CursorAdapter<>(EmptyCursor, new BookmarkDatabaseReader());
	PagingAdapterInterface<BookmarkDatabaseReader> dataAdapter = DummyReader;
	ImageView pageAsyncLoader;
	
	public BookmarkAdapter(MainActivityUIBase a, int resource, int textViewResourceId, BookPresenter md_, SQLiteDatabase database, int l, boolean testDBV2) {
		//this(a,resource,textViewResourceId,objects);
		this.a=a;
		resourceID=resource;
		textViewResourceID=textViewResourceId;
		presenter=md_;
		isWeb = false; // nimp md instanceof bookPresenter_web;
		this.testDBV2 = testDBV2;
		refresh(md_, database);
   }
   
	public /*static*/ class BookmarkDatabaseReader implements CursorReader {
		public long row_id;
		public long sort_number;
		public String entryName;
		public int position;
		@Override
		public void ReadCursor(Cursor cursor, long rowID, long sortNum) {
			if (testDBV2) {
				position = (int) cursor.getLong(2);
				entryName = cursor.getString(3);
			} else {
				position = (int) Long.parseLong(cursor.getString(2));
			}
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
					"lex='" + entryName + '\'' +
					'}';
		}
	}
	ConstructorInterface<BookmarkDatabaseReader> BookmarkDatabaseReaderConstructor = length -> new BookmarkDatabaseReader();
	
	
	public void refresh(BookPresenter invoker, SQLiteDatabase database) {
		//if(invoker!=md || con!=con_ || cr==null)
		try {
	    	presenter =invoker;
	    	isWeb = false;// nimp md instanceof bookPresenter_web;
			dataAdapter.close();
			dataAdapter = DummyReader;
			if (testDBV2) {
				boolean bSingleThreadLoading = true;
				if (bSingleThreadLoading) {
					Cursor cursor = database.rawQuery("select id,last_edit_time,pos,lex from "
									+LexicalDBHelper.TABLE_BOOK_NOTE_v2 +" where bid=? order by last_edit_time desc"
							, new String[]{presenter.bookImpl.getBooKID()+""});
					CMN.Log("查询个数::"+cursor.getCount());
					dataAdapter = new CursorAdapter<>(cursor, new BookmarkDatabaseReader());
					notifyDataSetChanged();
				} else {
//					if (pageAsyncLoader==null) {
//						pageAsyncLoader = new ImageView(a);
//					}
//					PagingCursorAdapter<BookmarkDatabaseReader> dataAdapter = new PagingCursorAdapter<>(database
//							//, new SimpleClassConstructor<>(HistoryDatabaseReader.class)
//							, BookmarkDatabaseReaderConstructor
//							, BookmarkDatabaseReader[]::new);
//					this.dataAdapter = dataAdapter;
//					dataAdapter.bindTo(lv)
//							.setAsyncLoader(a, pageAsyncLoader)
//							.sortBy(mtableName, FIELD_VISIT_TIME, true, "lex, books");
//					if (getFragmentId()==DB_FAVORITE) {
//						dataAdapter.where("folder=?", new String[]{a.opt.getCurrFavoriteNoteBookId()+""});
//					}
//					dataAdapter.startPaging(lastVisiblePositionMap.get(getFragmentId(), 0L), 20, 15);
				}
			} else {
				Cursor cursor = database.rawQuery(isWeb?"select rowid,date,_id from t3 ":"select * from t1 ", null);
				CMN.Log("查询个数::"+cursor.getCount());
				dataAdapter = new CursorAdapter<>(cursor, new BookmarkDatabaseReader());
				notifyDataSetChanged();
			}
		} catch (Exception e) {
			dataAdapter = DummyReader;
		}
	}

	public class ViewHolder {
		TextView title;
		ImageView ivDel;
		int entry_position;
		ViewHolder(BookmarkAdapter a, View itemView) {
			itemView.setTag(this);
			title = itemView.findViewById(R.id.text1);
			ivDel = itemView.findViewById(R.id.del);
			ivDel.setOnClickListener(v -> {
				if (testDBV2) {
					presenter.deleteBookMark(entry_position, v1 -> {
						refresh(presenter, presenter.a.prepareHistoryCon().getDB());
						notifyDataSetChanged();
					});
				} else {
					presenter.a.showT("已弃用旧版数据库，请尽快升级。");
				}
			});
		}
	}

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		ViewHolder vh;
		if(convertView==null) {
			vh = new ViewHolder(this, convertView=LayoutInflater.from(parent.getContext()).inflate(resourceID, parent, false));
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		String LexicalText;
		// nimp
		//if(md instanceof bookPresenter_pdf){
		//	LexicalText="第"+cr.getInt(0)+"页";
		//}else if(isWeb){
		//	LexicalText=cr.getString(1);
		//}
		//else
		try {
			//LexicalText=md.bookImpl.getEntryAt(testDBV2?cr.getInt(2):cr.getInt(0));
			if (testDBV2) {
				BookmarkDatabaseReader reader = dataAdapter.getReaderAt(position);
				vh.entry_position = reader.position;
				LexicalText=reader.entryName;
			} else {
				LexicalText= presenter.bookImpl.getEntryAt(DummyReader.getReaderAt(position).position);
			}
		} catch (Exception e) {
			LexicalText="!!!Error: "+e.getLocalizedMessage();
		}
		
		vh.title.setText(LexicalText);
    	if(darkMode)
			vh.title.setTextColor(Color.WHITE);
		
    	vh.ivDel.setVisibility(showDelete?View.VISIBLE:View.GONE);
        return convertView;
    }

	@Override
	public int getCount() {
		return dataAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		try {
			return dataAdapter.getReaderAt(position);
		} catch (Exception e) {
			CMN.Log(e);
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void clear() {
		dataAdapter.close();
		dataAdapter=DummyReader;
	}
}

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

import com.knziha.plod.db.LexicalDBHelper;

import static com.knziha.plod.widgets.Utils.EmptyCursor;

/**
 * Created by KnIfER on 2021/11/16.
 */

public class AnnotAdapter extends BaseAdapter{
	BookPresenter presenter;
	boolean isWeb;
	public boolean showDelete;
	int resourceID;
	int textViewResourceID;
	Context a;
	public boolean darkMode;
	
	private PagingAdapterInterface<AnnotationReader> DummyReader = new CursorAdapter<>(EmptyCursor, new AnnotationReader());
	PagingAdapterInterface<AnnotationReader> dataAdapter = DummyReader;
	ImageView pageAsyncLoader;
	
	public AnnotAdapter(MainActivityUIBase a, int resource, int textViewResourceId, BookPresenter md_, SQLiteDatabase database, int l) {
		//this(a,resource,textViewResourceId,objects);
		this.a=a;
		resourceID=resource;
		textViewResourceID=textViewResourceId;
		presenter=md_;
		isWeb = md_!=null && md_.getIsWebx();
		refresh(md_, database);
   }
   
	public /*static*/ class AnnotationReader implements CursorReader {
		public long row_id;
		public long sort_number;
		public String entryName;
		public String annotText;
		public long position;
		
		@Override
		public void ReadCursor(Cursor cursor, long rowID, long sortNum) {
			if (rowID!=-1) {
				row_id = rowID;
				sort_number = sortNum;
			} else {
				row_id = cursor.getLong(0);
				sort_number = cursor.getLong(1);
			}
			entryName = cursor.getString(2);
			annotText = cursor.getString(3);
			position = cursor.getLong(4);
		}
		
		@Override
		public String toString() {
			return "WebAnnotationCursorReader{" +
					"lex='" + annotText + '\'' +
					"entry='" + entryName + '\'' +
					'}';
		}
	}
	ConstructorInterface<AnnotationReader> BookmarkDatabaseReaderConstructor = length -> new AnnotationReader();
	
	public void refresh(BookPresenter invoker, SQLiteDatabase database) {
		//if(invoker!=md || con!=con_ || cr==null)
		try {
	    	presenter =invoker;
	    	isWeb = invoker!=null && invoker.getIsWebx();
			dataAdapter.close();
			dataAdapter = DummyReader;
			{
				boolean bSingleThreadLoading = true;
				if (bSingleThreadLoading) {
					Cursor cursor = database.rawQuery("select id,last_edit_time,entry,lex,pos from "
									+LexicalDBHelper.TABLE_BOOK_ANNOT_v2 +" where bid=? order by last_edit_time desc"
							, new String[]{presenter.getId()+""});
					CMN.Log("查询个数::"+cursor.getCount());
					dataAdapter = new CursorAdapter<>(cursor, new AnnotationReader());
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
			}
		} catch (Exception e) {
			dataAdapter = DummyReader;
		}
	}

	public class ViewHolder {
		TextView title;
		ImageView ivDel;
		ViewHolder(AnnotAdapter a, View itemView) {
			itemView.setTag(this);
			title = itemView.findViewById(R.id.text1);
			ivDel = itemView.findViewById(R.id.del);
			ivDel.setOnClickListener(v -> {
//				presenter.deleteBookMark(entry_position, v1 -> {
//					refresh(presenter, presenter.a.prepareHistoryCon().getDB());
//					notifyDataSetChanged();
//				});
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
		try {
			AnnotationReader reader = dataAdapter.getReaderAt(position);
			LexicalText=reader.annotText;
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

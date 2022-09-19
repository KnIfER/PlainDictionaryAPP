package com.knziha.plod.PlainUI;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;

import com.alibaba.fastjson.JSONObject;
import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReader;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.MainActivityUIBase.ViewHolder;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.text.ColoredTextSpan;

import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

/**
 * Created by KnIfER on 2021/11/16.
 */

public class AnnotAdapter extends BaseAdapter{
	BookPresenter presenter;
	boolean isWeb;
	public boolean showDelete;
	int resourceID;
	int textViewResourceID;
	MainActivityUIBase a;
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
		public long bid;
		public JSONObject annot;
		
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
			bid = cursor.getLong(5);
			try {
				annot = JSONObject.parseObject(cursor.getString(6));
			} catch (Exception e) {
				annot = new JSONObject();
				CMN.debug();
			}
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
					Cursor cursor = database.rawQuery("select id,last_edit_time,entry,lex,pos,bid,annot from "
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

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		ViewHolder vh;
		if(convertView==null) {
			vh = new ViewHolder(a, R.layout.listview_item01, parent);
			ViewUtils.setPadding(vh.itemView, (int) (GlobalOptions.density*15), -1, -1, -1);
			vh.title.setMaxLines(3);
			vh.title.setEllipsize(TextUtils.TruncateAt.END);
			vh.subtitle.setMaxLines(2);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		String lex, entry, bookName;
		AnnotationReader reader = null;
		try {
			reader = dataAdapter.getReaderAt(position);
			lex=reader.annotText;
			entry=reader.entryName;
			bookName=a.getBookInLstNameByIdNoCreation(reader.bid);
		} catch (Exception e) {
			lex="!!!Error: "+e.getLocalizedMessage();
			entry="";
			bookName="";
		}
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append(" ");
		ssb.append(lex);
		ssb.append(" ");
		
		vh.title.setTextColor(a.AppBlack);
		
		ViewUtils.setVisible(vh.subtitle, true);
	
	
		int color=0xffffaaaa, type=0;
		if(reader!=null) {
			String note = reader.annot.getString("note");
			try {
				color = reader.annot.getInteger("clr");
			} catch (Exception e) {
				CMN.debug(e);
			}
			try {
				type = reader.annot.getInteger("typ");
			} catch (Exception e) {
				CMN.debug(e);
			}
			ViewUtils.setVisible(vh.preview, note!=null);
			
			int tmp = 2;
			int c = ColorUtils.blendARGB(a.AppWhite, a.AppBlack, tmp==0?0.08f:tmp==1?0.5f:0.8f);
			tmp = PDICMainAppOptions.listPreviewFont();
			int size = tmp==0?12:tmp==1?14:17;
			
			vh.preview.setText(note);
			vh.preview.setTextColor(c);
			vh.preview.setTextSize(size);
		} else {
			ViewUtils.setVisible(vh.preview, false);
		}
		ssb.setSpan(new ColoredTextSpan(color, 8.f, type==1?2:1), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		vh.title.setText(ssb);
	
		ssb.clear();
		ssb.clearSpans();
		ssb.append(bookName);
		if (!TextUtils.isEmpty(entry)) {
			ssb.append(" | ").
					//ssb.append(" — ").
							append(Character.toUpperCase(entry.charAt(0)))
					.append(entry, 1, entry.length());
		}
		vh.subtitle.setText(ssb);
		
    	//vh.ivDel.setVisibility(showDelete?View.VISIBLE:View.GONE);
        return vh.itemView;
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

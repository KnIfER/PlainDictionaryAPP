package com.knziha.plod.PlainUI;

import static com.knziha.plod.db.LexicalDBHelper.FIELD_CREATE_TIME;
import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.knziha.filepicker.widget.MaterialCheckbox;
import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReader;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.paging.PagingCursorAdapter;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.DBroswer;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.MainActivityUIBase.ViewHolder;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.text.ColoredTextSpan;

import java.lang.ref.WeakReference;

/**
 * Created by KnIfER on 2021/11/16.
 */

public class AnnotAdapter extends RecyclerView.Adapter<AnnotAdapter.VueHolder> implements View.OnClickListener, PagingCursorAdapter.OnLoadListener {
	BookPresenter presenter;
	public boolean showDelete;
	int resourceID;
	int textViewResourceID;
	MainActivityUIBase a;
	public boolean darkMode;
	WeakReference<BookNotes> bookNotesRef = ViewUtils.DummyRef;
	RecyclerView lv;
	final int scope;
	
	private PagingAdapterInterface<AnnotationReader> DummyReader = new CursorAdapter<>(EmptyCursor, new AnnotationReader());
	PagingAdapterInterface<AnnotationReader> dataAdapter = DummyReader;
	ImageView pageAsyncLoader;
	
	public AnnotAdapter(MainActivityUIBase a, int resource, int textViewResourceId, BookPresenter md_
			, SQLiteDatabase database, int scope
			, RecyclerView lv) {
		//this(a,resource,textViewResourceId,objects);
		this.a=a;
		this.lv=lv;
		resourceID=resource;
		textViewResourceID=textViewResourceId;
		presenter=md_;
		this.scope = scope;
		refresh(md_, database);
	}
	
	@Override
	public void onClick(View v) {
		BookNotes notes = bookNotesRef.get();
		if (notes != null) {
			notes.click(this, v, (VueHolder)v.getTag());
		}
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
	
	final static SparseArray<long[]> savedPositions = new SparseArray();
	
	public void refresh(BookPresenter invoker, SQLiteDatabase database) {
		//if(invoker!=md || con!=con_ || cr==null)
		try {
			presenter = invoker;
			dataAdapter.close();
			dataAdapter = DummyReader;
			{
				boolean bSingleThreadLoading = false;
				if (bSingleThreadLoading) {
					String sql = "select id,last_edit_time,entry,lex,pos,bid,annot from "
							+LexicalDBHelper.TABLE_BOOK_ANNOT_v2;
					if(scope == 1) {
						sql += " where bid=?";
					}
					Cursor cursor = database.rawQuery(sql+" order by last_edit_time desc"
							, scope == 1?new String[]{presenter.getId()+""}:null);
					CMN.Log("查询个数::"+cursor.getCount());
					dataAdapter = new CursorAdapter<>(cursor, new AnnotationReader());
					notifyDataSetChanged();
				} else {
					if (pageAsyncLoader==null) {
						pageAsyncLoader = new ImageView(a);
					}
					PagingCursorAdapter<AnnotationReader> dataAdapter = new PagingCursorAdapter<>(database
							//, new SimpleClassConstructor<>(HistoryDatabaseReader.class)
							, BookmarkDatabaseReaderConstructor
							, AnnotationReader[]::new);
					this.dataAdapter = dataAdapter;
					dataAdapter.bindTo(lv)
							.setAsyncLoader(a, pageAsyncLoader)
							.sortBy(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, FIELD_CREATE_TIME, true, "entry,lex,pos,bid,annot");
					if (scope == 1) {
						dataAdapter.where("bid=?", new String[]{presenter.getId()+""});
					}
					// savedPositions.get(getFragmentId(), 0L)
					long lastTm=0, offset=0;
					dataAdapter.startPaging(lastTm, offset, 20, 15, this);
				}
			}
		} catch (Exception e) {
			dataAdapter = DummyReader;
		}
	}
	
	
	@NonNull
	@Override
	public AnnotAdapter.VueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		VueHolder vh = new VueHolder(a, parent, LayoutInflater.from(a).inflate(R.layout.listview_item01_book_notes, parent, false));
		vh.itemView.setOnClickListener(this);
		return vh;
	}
	
	@Override
	public void onBindViewHolder(@NonNull AnnotAdapter.VueHolder holder, int position) {
		ViewHolder vh = holder.vh;
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
				//CMN.debug(e);
			}
			try {
				type = reader.annot.getInteger("typ");
			} catch (Exception e) {
				//CMN.debug(e);
			}
			ViewUtils.setVisible(vh.preview, note!=null);
			ViewUtils.setVisibility(holder.dotVue, note!=null);
			
			int tmp = 2;
			int c = ColorUtils.blendARGB(a.AppWhite, a.AppBlack, tmp==0?0.08f:tmp==1?0.5f:0.8f);
			tmp = PDICMainAppOptions.listPreviewFont();
			int size = tmp==0?12:tmp==1?14:17;
			
			vh.preview.setText(note);
			vh.preview.setTextColor(c);
			vh.preview.setTextSize(size);
		} else {
			ViewUtils.setVisible(vh.preview, false);
			ViewUtils.setVisible(holder.dotVue, false);
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
	}
	
	@Override
	public int getItemCount() {
		return dataAdapter.getCount();
	}

	@Override
	public long getItemId(int position) {
		try {
			return dataAdapter.getReaderAt(position).row_id;
		} catch (Exception e) {
			CMN.debug(e);
			return 0;
		}
	}
	
	public void clear() {
		dataAdapter.close();
		dataAdapter=DummyReader;
	}
	
	static class VueHolder extends RecyclerView.ViewHolder{
		final MainActivityUIBase.ViewHolder vh;
		MaterialCheckbox dotVue;
		View typVue;
		VueHolder(MainActivityUIBase a, ViewGroup parent, View view) {
			super(view);
			view.setTag(this);
			ViewGroup vg = (ViewGroup)view;
			ViewHolder vh = new ViewHolder(a, 0, (ViewGroup)vg.getChildAt(1));
			dotVue = vg.getChildAt(0).findViewById(R.id.dotVue);
			dotVue.bgFrame = 0xff999999;
			typVue = vg.getChildAt(2);
			this.vh = vh;
			//ViewUtils.setPadding(vh.itemView, (int) (GlobalOptions.density*15), -1, -1, -1);
			vh.title.setMaxLines(3);
			vh.title.setEllipsize(TextUtils.TruncateAt.END);
			vh.subtitle.setMaxLines(2);
		}
	}
	
	public void setBookNotes(BookNotes bookNotes) {
		this.bookNotesRef = new WeakReference<>(bookNotes);
	}
	
	@Override
	public void onLoaded(PagingCursorAdapter adapter) {
		BookNotes notes = bookNotesRef.get();
		if (notes!=null) {
			lv.suppressLayout(false);
		}
	}
}

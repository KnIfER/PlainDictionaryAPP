package com.knziha.plod.PlainUI;

import static com.knziha.plod.db.LexicalDBHelper.FIELD_EDIT_TIME;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.knziha.filepicker.widget.MaterialCheckbox;
import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReader;
import com.knziha.paging.CursorReaderMultiSortNum;
import com.knziha.paging.MultiFieldPagingCursorAdapter;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.paging.PagingCursorAdapter;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CharSequenceKey;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.plaindict.MainActivityUIBase.ViewHolder;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.Javelin.DecorativeTextview;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.text.ColoredTextSpan;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

public class InfiniteAdapter extends RecyclerView.Adapter<InfiniteAdapter.VueHolder> implements View.OnClickListener, PagingCursorAdapter.OnLoadListener, View.OnLongClickListener {
	Toastable_Activity a;
	public boolean darkMode;
	final String tableName;
	final String tag;
	final RecyclerView lv;
	
	private PagingAdapterInterface<InfiniteReader> DummyReader = new CursorAdapter<>(EmptyCursor, new InfiniteReader());
	PagingAdapterInterface<? extends InfiniteReader> dataAdapter = DummyReader;
	ImageView pageAsyncLoader;
	long dbVer;
	private long bid;
	String expUrl = "";
	public int mViewVer = -1;
	final static String data_fields = "url,title,subtitle,userurl,username,tag0,length,thumbnailurl";
	
	public InfiniteAdapter(Toastable_Activity a
			, SQLiteDatabase database, String tableName, String tag
			, RecyclerView lv) {
		//this(a,resource,textViewResourceId,objects);
		this.a=a;
		this.tableName = tableName;
		this.tag = tag;
		this.lv = lv;
		rebuildCursor(database, null); /*构造刷新*/
	}
	
	@Override
	public void onClick(View v) {
	}
	
	@Override
	public boolean onLongClick(View v) {
		return true;
	}
	
	
	public static class InfiniteReader implements CursorReader, CursorReaderMultiSortNum{
		public long row_id;
		public long sort_number;
		public long[] sort_numbers;
		/** the marked range */
		public String url;
		public String title;
		int multiSorts = 0;
		@Override
		public void ReadCursor(PagingAdapterInterface adapter, Cursor cursor, long rowID, long[] sortNums) {
			multiSorts = sortNums.length - 1;
			sort_numbers = sortNums;
			//sort_numbers = new long[sortNums.length];
			//System.arraycopy(sortNums, 0, sort_numbers, 0, sortNums.length);
			ReadCursor(adapter, cursor, rowID, sortNums[0]);
		}
		//static ConstructorInterface<InfiniteReader> readerMaker = length -> new InfiniteReader();
		@Override
		public void ReadCursor(PagingAdapterInterface adapter, Cursor cursor, long rowID, long sortNum) {
			if (rowID!=-1) {
				row_id = rowID;
				sort_number = sortNum;
			} else {
				row_id = cursor.getLong(0);
				sort_number = cursor.getLong(1);
			}
			url = cursor.getString(multiSorts+2);
			title = cursor.getString(multiSorts+3);
		}
		
		@Override
		public String toString() {
			return "WebInfiniteCursorReader{" +
					"lex='" + title + '\'' +
					'}';
		}
		static ConstructorInterface<InfiniteReader> readerMaker = length -> new InfiniteReader();
	}

	public void resumeListPos(RecyclerView lv) {
//		long[] pos = savedPositions.get(getFragmentId());
//		LinearLayoutManager lm = (LinearLayoutManager) lv.getLayoutManager();
//		if (lm!=null) {
//			if (pos != null) {
//				CMN.debug("resume listPos::1", (int) pos[6], (int) pos[5]);
//				lm.scrollToPositionWithOffset((int)pos[6], (int)pos[5]);
//			} else {
//				// 无法拓展 notifyItemRangeInserted
//				lv.postTop(200);
//			}
//		}
	}
	
	public void rebuildCursor(SQLiteDatabase database, View sortView) {
		CMN.debug("rebuildCursor::", "table="+tableName);
		try {
			dataAdapter.close();
			dataAdapter = DummyReader;
			//dbVer = LexicalDBHelper.annotDbVer;
			if (pageAsyncLoader == null) {
				pageAsyncLoader = new ImageView(a);
			}
			{
				//sortType = 2;
				boolean bSingleThreadLoading = false;
				if (bSingleThreadLoading) {
//					String sql = "select id,last_edit_time,entry,lex,pos,bid,annot from "
//							+LexicalDBHelper.TABLE_BOOK_ANNOT_v2;
//					if(scope == 1) {
//						sql += " where bid=?";
//					}
//					Cursor cursor = database.rawQuery(sql+" order by last_edit_time desc"
//							, scope == 1?new String[]{presenter.getId()+""}:null);
//					CMN.Log("查询个数::"+cursor.getCount());
//					dataAdapter = new CursorAdapter<>(cursor, new InfiniteReader());
//					notifyDataSetChanged();
				}
				// 组合排序
				else {
					saveListPosition(sortView);
					MultiFieldPagingCursorAdapter<InfiniteReader> dataAdapter = new MultiFieldPagingCursorAdapter(database
							//, new SimpleClassConstructor<>(HistoryDatabaseReader.class)
							, InfiniteReader.readerMaker
							, InfiniteReader[]::new);
					this.dataAdapter = dataAdapter;
					String[] sortBy= new String[]{"time", "id"};
					boolean desc = true;
					//CMN.debug("组合排序 sortBy::", sortBy);
					dataAdapter.bindTo(lv)
							.setAsyncLoader(a, pageAsyncLoader)
							.sortBy(tableName, sortBy, desc, data_fields);
					long[] sorts = null;
					//long[] pos = savedPositions.get(getFragmentId());
					dataAdapter.startPaging(sorts, 0, 20, 15, this);
					notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
			dataAdapter = DummyReader;
		}
	}
	
	/** update timed page table by expurl */
	private void updateByExpUrl(PagingCursorAdapter<InfiniteReader> dataAdapter, String value, long finalLastTm, long finalOffset) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> args = new ArrayList<>();
		CMN.debug("updateByExpUrl:: exp=", value);
		if(value==null) return;
		this.expUrl = value;
		try {
			if (value.startsWith("\"")) {
				value = value.substring(1, value.length()-1);
			}
			boolean encoded = value.startsWith("d") || value.startsWith("w");
			String[] arr = value.split("-");
			for (int i = 0; i < arr.length; i++) {
				String[] dp = arr[i].split("_");
				//CMN.debug("dp::", value, dp);
				long bid = encoded?IU.TextToNumber_SIXTWO_LE(new CharSequenceKey(dp[0], 1)):IU.parseLong(dp[0]);
				long pos1 = encoded?IU.TextToNumber_SIXTWO_LE(dp[1]):IU.parseLong(dp[1]);
				if(sb.length()>0)
					sb.append(" or ");
				sb.append("(");
				if (dp.length > 2) {
					sb.append("bid=? and (");
					args.add(""+bid);
					for (int j = 0; j < dp.length - 1; j++) {
						if(j>0) sb.append(" or ");
						sb.append("pos=?");
						pos1 = encoded?IU.TextToNumber_SIXTWO_LE(dp[1+j]):IU.parseLong(dp[1+j]);
						args.add(""+ pos1);
					}
					sb.append(")");
				} else {
					sb.append("bid=? and pos=?");
					args.add(""+bid);
					args.add(""+ pos1);
				}
				sb.append(")");
			}
			CMN.debug("updateByExpUrl:: sql=", sb, args);
			dataAdapter.where(sb.toString(), args.toArray(new String[0]));
			dataAdapter.startPaging(finalLastTm, finalOffset, 20, 15, InfiniteAdapter.this);
			notifyDataSetChanged();
		} catch (Exception e) {
			this.dataAdapter.close();
			InfiniteAdapter.this.dataAdapter = DummyReader;
			CMN.debug(e);
		}
	}
	
	/** update multi-columned page table by expurl */
	private void updateByExpUrl(MultiFieldPagingCursorAdapter<InfiniteReader> dataAdapter, String value, long[] finalLastTm, long finalOffset) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> args = new ArrayList<>();
		CMN.debug("exp=::", value);
		if(value==null) return;
		this.expUrl = value;
		try {
			if (value.startsWith("\"")) {
				value = value.substring(1, value.length()-1);
			}
			boolean encoded = value.startsWith("d") || value.startsWith("w");
			String[] arr = value.split("-");
			for (int i = 0; i < arr.length; i++) {
				String[] dp = arr[i].split("_");
				//CMN.debug("dp::", value, dp);
				long bid = encoded?IU.TextToNumber_SIXTWO_LE(new CharSequenceKey(dp[0], 1)):IU.parseLong(dp[0]);
				long pos1 = encoded?IU.TextToNumber_SIXTWO_LE(dp[1]):IU.parseLong(dp[1]);
				if(sb.length()>0)
					sb.append(" or ");
				sb.append("(");
				if (dp.length > 2) {
					sb.append("bid=? and (");
					args.add(""+bid);
					for (int j = 0; j < dp.length - 1; j++) {
						if(j>0) sb.append(" or ");
						sb.append("pos=?");
						pos1 = encoded?IU.TextToNumber_SIXTWO_LE(dp[1+j]):IU.parseLong(dp[1+j]);
						args.add(""+ pos1);
					}
					sb.append(")");
				} else {
					sb.append("bid=? and pos=?");
					args.add(""+bid);
					args.add(""+ pos1);
				}
				sb.append(")");
			}
			CMN.debug("exp=::", value, sb, args);
			dataAdapter.where(sb.toString(), args.toArray(new String[0]));
			dataAdapter.startPaging(finalLastTm, finalOffset, 20, 15, InfiniteAdapter.this);
			notifyDataSetChanged();
		} catch (Exception e) {
			this.dataAdapter.close();
			InfiniteAdapter.this.dataAdapter = DummyReader;
			CMN.debug(e);
		}
	}
	
	/** type[act|ui|db], long[]{pos, view offset} */
	public final static SparseArray<long[]> savedPositions = new SparseArray();
	
	public void saveListPosition(View view) {
//		try {
//			boolean b1 = view==null;
//			if(b1) {
//				view = lv.getChildAt(0);
//			}
//			if (view!=null) {
//				VueHolder holder = (VueHolder) view.getTag();
//				InfiniteReader reader = (InfiniteReader) holder.tag;
//				//CMN.debug("saveListPosition::", holder.getLayoutPosition());
//				if (holder.getLayoutPosition() > 0 || !b1) {
//					Cursor cursor = notes.a.prepareHistoryCon().getDB().rawQuery("select bid,pos," + FIELD_EDIT_TIME + ",id,tPos from " + LexicalDBHelper.TABLE_BOOK_ANNOT_v2 + " where id=? limit 1"
//							, new String[]{"" + reader.row_id});
//					if (cursor.moveToNext()) {
//						long[] sorts = new long[7];
//						for (int i = 0; i < 5; i++) {
//							sorts[i] = cursor.getLong(i);
//						}
//						sorts[5] = view.getTop(); // !b1其实没用。。
//						sorts[6] = holder.getLayoutPosition();
//						savedPositions.put(getFragmentId(), sorts);
//					}
//					cursor.close();
//				} else {
//					savedPositions.remove(getFragmentId());
//				}
//				CMN.debug("savedPositions::save::", scope+" "+reader.entryName+" "+new Date(reader.sort_number), holder.vh.title.getText());
//			}
//		} catch (Exception e) {
//			CMN.debug(e);
//		}
	}
	
	@NonNull
	@Override
	public InfiniteAdapter.VueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		VueHolder vh = new VueHolder(a, parent, LayoutInflater.from(a).inflate(R.layout.listview_item01_book_notes, parent, false));
		vh.itemView.setOnClickListener(this);
		vh.itemView.setOnLongClickListener(this);
		return vh;
	}
	
	@Override
	public void onBindViewHolder(@NonNull InfiniteAdapter.VueHolder holder, int position) {
		ViewHolder vh = holder.vh;
		String title;
		InfiniteReader reader = null;
		try {
			reader = dataAdapter.getReaderAt(position, true);
			title = reader.title;
		} catch (Exception e) {
			title = "!!!Error: " + e.getLocalizedMessage();
		}
		holder.tag = reader; //???
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append(" ");
		ssb.append(title == null ? title + "" : title);
		ssb.append(" ");
		
		vh.title.setTextColor(a.AppBlack);
		
		ViewUtils.setVisible(vh.subtitle, true);
		
		int color = 0xffffaaaa, type = 0;
		if (false) {
			//SimpleDateFormat timemachine = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			//CMN.debug("onBindViewHolder::", lex, timemachine.format(new Date(reader.sort_numbers[1]))); // maybe null
		} else {
			ViewUtils.setVisible(vh.preview, false);
			ViewUtils.setVisible(holder.dotVue, false);
		}
		
		vh.title.setText(ssb);
		
		//vh.ivDel.setVisibility(showDelete?View.VISIBLE:View.GONE);
	}
	
	@Override
	public int getItemCount() {
		return dataAdapter.getCount();
	}

	@Override
	public long getItemId(int position) {
		try {
			return dataAdapter.getReaderAt(position, false).row_id;
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
		final ViewHolder vh;
		MaterialCheckbox dotVue;
		View typVue;
		InfiniteReader tag;
		VueHolder(Toastable_Activity a, ViewGroup parent, View view) {
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
	
	@Override
	public void onLoaded(PagingAdapterInterface adapter) {
		CMN.debug("onLoaded::", adapter, adapter.getCount());
		if (lv!=null) {
			lv.suppressLayout(false);
		}
	}
}

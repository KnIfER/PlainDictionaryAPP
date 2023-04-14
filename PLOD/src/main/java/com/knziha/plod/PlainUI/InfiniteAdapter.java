package com.knziha.plod.PlainUI;

import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.VU;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.knziha.paging.AppIconCover.AppIconCover;
import com.knziha.paging.AppIconCover.AppLoadableBean;
import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReader;
import com.knziha.paging.CursorReaderMultiSortNum;
import com.knziha.paging.MultiFieldPagingCursorAdapter;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.paging.PagingCursorAdapter;
import com.knziha.plod.db.FFDB;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CharSequenceKey;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.plaindict.MainActivityUIBase.ViewHolder;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
	final static String data_fields = "url,title,subtitle,userurl,username,tag0,length,thumbnailurl,thumbnail is not null";
	
	ArrayList<String> tags = new ArrayList<>();
	
	public InfiniteAdapter(Toastable_Activity a
			, SQLiteDatabase database, String tableName, String tag
			, RecyclerView lv) {
		//this(a,resource,textViewResourceId,objects);
		this.a=a;
		this.tableName = tableName;
		this.tag = tag;
		this.lv = lv;
		try {
			database.execSQL("CREATE INDEX if not exists " + tableName + "_tag0_index ON " + tableName + " (tag0, time, id)"); // 分类视图
			CMN.debug("indexed built::业精于勤分类、善总结、多把握");
		} catch (Exception e) {
			CMN.debug(e);
		}
		//database.execSQL("Drop INDEX if exists " + tableName + "_tag0_index"); // 分类视图
		
		CMN.rt();
		String lastTag = "";
		Cursor cursor;
		while ((cursor = database.rawQuery("select tag0 from " + tableName + " where (tag0,time)>(?,?) order by tag0 ASC,time ASC,id ASC limit 1", new String[]{lastTag, Long.MAX_VALUE+""})).moveToNext()) {
			lastTag = cursor.getString(0);
			tags.add(lastTag);
			cursor.close();
		}
		CMN.pt("扫描tag耗时::");
		CMN.debug(tags);
		rebuildCursor(database, null); /*构造刷新*/
		
		VU.TintListFilter tintListFilter = a.tintListFilter;
		if (tintListFilter.sRipple==null) {
			tintListFilter.sRipple = new RippleDrawable(ColorStateList.valueOf(0xfffa87a9), null, null);
			//rippleBGrippleBG.setColor(ColorStateList.valueOf(Color.WHITE));
			try {
				tintListFilter.sRippleState = ViewUtils.execSimple("$.mState", ViewUtils.reflectionPool, tintListFilter.sRipple);
				tintListFilter.sRippleStateField = (Field) ViewUtils.evalFieldMethod(tintListFilter.sRipple.getClass(), tintListFilter.sRipple, new String[]{"ex", "mState"}, new HashMap<>(), ViewUtils.reflectionPool);
				TypedArray ta = a.obtainStyledAttributes(new int[]{android.R.attr.actionBarItemBackground});
				tintListFilter.sRippleToolbar = (RippleDrawable) ta.getDrawable(0);
				ta.recycle();
				CMN.debug("mState::", tintListFilter.sRippleState, tintListFilter.sRippleStateField);
				tintListFilter.sRippleStateToolbar = ViewUtils.execSimple("$.mState", ViewUtils.reflectionPool, tintListFilter.sRippleToolbar);
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
	
	// click
	@Override
	public void onClick(View v) {
		VueHolder holder = (VueHolder) v.getTag();
		
		try {
			InfiniteReader reader = dataAdapter.getReaderAt(holder.getLayoutPosition(), false);
			String url = reader.url;
			if(!url.startsWith("http"))
				url = "https://www.bilibili.com/video/"+url;
			a.showT(url);
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			a.startActivity(intent);
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		return true;
	}
	
	public void setAdapter(RecyclerView lv, ListView tagList) {
		lv.setAdapter(this);
		tagList.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() {
				CMN.debug("getCount::", tags.size());
				return tags.size();
			}
			@Override
			public Object getItem(int position) {
				return null;
			}
			@Override
			public long getItemId(int position) {
				return 0;
			}
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = a.getLayoutInflater().inflate(R.layout.inifinite_tag_left, parent, false);
					VU.TintListFilter tintListFilter = a.tintListFilter;
					tintListFilter.ModRippleColor(convertView.getBackground(), tintListFilter.sRippleState);
				}
				TextView tv = (TextView) convertView;
				String tagName = tags.get(position);
				tv.setText(tagName);
				return convertView;
			}
		});
	}
	
	
	public static class InfiniteReader implements CursorReader, CursorReaderMultiSortNum{
		public long row_id;
		public long sort_number;
		public long[] sort_numbers;
		/** the marked range */
		public String url;
		public String title;
		public String username;
		public boolean hasThumbnail;
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
			
			username = cursor.getString(multiSorts+6);
			
			hasThumbnail = cursor.getInt(multiSorts+10)==1;
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
		VueHolder vh = new VueHolder(a, parent, LayoutInflater.from(a).inflate(R.layout.inifinite_list, parent, false));
		vh.itemView.setOnClickListener(this);
		vh.itemView.setOnLongClickListener(this);
		VU.TintListFilter tintListFilter = a.tintListFilter;
		tintListFilter.ModRippleColor(vh.itemView.getBackground(), tintListFilter.sRippleState);
		return vh;
	}
	
	public static class DBThumbnailFetecher implements AppLoadableBean {
		final long rowId;
		final String tableName;
		
		public DBThumbnailFetecher(long rowId, String tableName) {
			this.rowId = rowId;
			this.tableName = tableName;
		}
		
		public Drawable load() {
			Drawable ret = null;
			boolean save = PDICMainAppOptions.storeIcon();
			{
				//CMN.rt("package::"+appid);
				Cursor c = FFDB.getInstance(null).getDB().rawQuery("select thumbnail from " + tableName + " where id=? limit 1", new String[]{"" + rowId});
				if(!c.moveToNext()) return null; // todo
				if (save) {
					byte[] tmp = c.getBlob(0);
					try {
						if (tmp != null) {
							Bitmap bm = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
							if(bm!=null)
								ret = new BitmapDrawable(null, bm);
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
					//CMN.pt("获取成功 package::"+ret);
				}
				c.close();
				//CMN.debug("package::"+pkgName);
			}
			return ret;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DBThumbnailFetecher that = (DBThumbnailFetecher) o;
			return rowId==that.rowId && tableName.equals(that.tableName);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(rowId);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull InfiniteAdapter.VueHolder holder, int position) {
		ViewHolder vh = holder.vh;
		String title;
		String thumbnailurl = null;
		boolean hasThumbnail = false;
		InfiniteReader reader = null;
		try {
			reader = dataAdapter.getReaderAt(position, true);
			title = reader.title;
			hasThumbnail = reader.hasThumbnail;
		} catch (Exception e) {
			// todo null
			title = "!!!Error: " + e.getLocalizedMessage();
		}
		holder.tag = reader; //???
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append(" ");
		ssb.append(title == null ? title + "" : title);
		ssb.append(" ");
		
		//ssb.append(hasThumbnail?"hasThumbnail":"");
		
		vh.title.setTextColor(a.AppBlack);
		
		vh.subtitle.setText(reader.username);
		
		ViewUtils.setVisible(vh.subtitle, true);
		
		int color = 0xffffaaaa, type = 0;
		if (false) {
			//SimpleDateFormat timemachine = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			//CMN.debug("onBindViewHolder::", lex, timemachine.format(new Date(reader.sort_numbers[1]))); // maybe null
		} else {
			ViewUtils.setVisible(vh.preview, false);
		}
		
		if (hasThumbnail) {
			RequestOptions options = new RequestOptions()
					.format(DecodeFormat.PREFER_ARGB_8888)//DecodeFormat.PREFER_ARGB_8888
					.skipMemoryCache(false)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					//.onlyRetrieveFromCache(true)
					.fitCenter()
					.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					;
			
			Glide.with(a)
					.load(new AppIconCover(new DBThumbnailFetecher(reader.row_id, tableName), false))
					.apply(options)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
							return false;
						}
						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//							DescriptiveImageView medium_thumbnail = (DescriptiveImageView) ((ImageViewTarget<?>) target).getView();
//							//todo check glide
//							medium_thumbnail.setText(((AppInfoBean)((AppIconCover)model).getBeanInMemory()).appName);
							return false;
						}
					})
					.into(holder.iv);
			//holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
		ImageView iv;
		View typVue;
		InfiniteReader tag;
		VueHolder(Toastable_Activity a, ViewGroup parent, View view) {
			super(view);
			view.setTag(this);
			ViewGroup vg = (ViewGroup)view;
			ViewHolder vh = new ViewHolder(a, 0, (ViewGroup)vg.getChildAt(1));
			iv = vg.findViewById(R.id.dotVue);
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

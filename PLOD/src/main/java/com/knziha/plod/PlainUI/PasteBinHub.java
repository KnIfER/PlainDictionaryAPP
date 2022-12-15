package com.knziha.plod.PlainUI;

import static com.knziha.plod.db.LexicalDBHelper.FIELD_CREATE_TIME;
import static com.knziha.plod.widgets.ViewUtils.EmptyCursor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorAdapter;
import com.knziha.paging.CursorReaderMultiSortNum;
import com.knziha.paging.MultiFieldPagingCursorAdapter;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.paging.PagingCursorAdapter;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.ViewUtils;

@SuppressLint("ResourceType")
public class PasteBinHub extends PlainAppPanel implements PopupMenuHelper.PopupMenuListener, PagingCursorAdapter.OnLoadListener {
	Toastable_Activity a;
	RecyclerView recyclerView;
	private boolean hubExpanded = false;
	private PagingAdapterInterface<PasteBinEntryReader> DummyReader = new CursorAdapter<>(EmptyCursor, new PasteBinEntryReader());
	PagingAdapterInterface<PasteBinEntryReader> dataAdapter = DummyReader;
	ImageView pageAsyncLoader;
	private RecyclerView.Adapter adapter;
	
	public PasteBinHub(Toastable_Activity a) {
		super();
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.tweakDlgScreen = false;
		this.a = a;
		setShowInDialog();
	}
	
	public static class PasteBinEntryReader implements CursorReaderMultiSortNum {
		public long row_id;
		public long sort_number;
		public long[] sort_numbers; // fav, time, id
		String content;
		@Override
		public void ReadCursor(Cursor cursor, long l, long l1) {
		}
		@Override
		public void ReadCursor(Cursor cursor, long rowID, long[] sortNums) {
			sort_numbers = sortNums;
			content = cursor.getString(4);
		}
		static ConstructorInterface<PasteBinEntryReader> readerMaker = length -> new PasteBinEntryReader();
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		if (a!=null && settingsLayout==null) {
			opt = a.opt;
			//View layout = a.getLayoutInflater().inflate(R.layout.tts_sound_control, a.root, false);
			recyclerView = new RecyclerView(a);
			recyclerView.setLayoutManager(new GridLayoutManager(a, 2));
			
			if (pageAsyncLoader == null) {
				pageAsyncLoader = new ImageView(a);
			}
			
			MultiFieldPagingCursorAdapter<PasteBinEntryReader> dataAdapter = new MultiFieldPagingCursorAdapter(LexicalDBHelper.getInstance().getDB()
					, PasteBinEntryReader.readerMaker
					, PasteBinEntryReader[]::new);
			this.dataAdapter = dataAdapter;
			String[] sortBy = new String[]{"fav", FIELD_CREATE_TIME, "id"};
			recyclerView.setAdapter(adapter = new RecyclerView.Adapter() {
				@NonNull
				@Override
				public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
					RecyclerView.ViewHolder vh = new RecyclerView.ViewHolder(new TextView(a)){};
					return vh;
				}
				@Override
				public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
					PasteBinEntryReader reader = null;
					String content;
					try {
						reader = dataAdapter.getReaderAt(position);
						content=reader.content;
					} catch (Exception e) {
						content="!!!Error: "+e.getLocalizedMessage();
					}
					((TextView)viewHolder.itemView).setText(content);
				}
				@Override
				public int getItemCount() {
					return dataAdapter.getCount();
				}
			});
			
			final String data_fields = "content";
			
			dataAdapter.bindTo(recyclerView)
					.setAsyncLoader(a, pageAsyncLoader)
					.sortBy(LexicalDBHelper.TABLE_PASTE_BIN, sortBy, true, data_fields);
			
			settingsLayout = (ViewGroup) recyclerView;
			
			dataAdapter.startPaging(null, 0, 20, 15, this);
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onLoaded(PagingAdapterInterface pagingAdapterInterface) {
	
	}
	
	private void refreshExpand() {
		View v = settingsLayout;
		DisplayMetrics dm2 = a.dm;
		if (hubExpanded)
			v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * 0.85f);
		else
			v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * ((BottomSheetDialog) dialog).getBehavior().getHalfExpandedRatio());
		v.requestLayout();
	}
	
	@Override
	protected void onShow() {
		refresh();
		
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
	}
	
	@Override
	public void refresh() {
		//CMN.debug("bookNotes::refresh");
		if (MainAppBackground != a.MainAppBackground)
		{
			// 刷新颜色变化（黑暗模式或者设置更改）
			//toolbar.setTitleTextColor(a.AppWhite);
			MainAppBackground = a.MainAppBackground;
		}
		if (ViewUtils.ensureTopmost(dialog, a, dialogDismissListener)
				|| ViewUtils.ensureWindowType(dialog, a, dialogDismissListener)) {
			ViewUtils.makeFullscreenWnd(dialog.getWindow());
		}
	}
	
	// click
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//			case R.id.ttsPin: {
//				CircleCheckBox checker = (CircleCheckBox) v;
//				checker.toggle();
//				opt.setTTSCtrlPinned(checker.isChecked());
//				TTSController_controlBar.setVisibility(checker.isChecked()?View.VISIBLE:View.GONE);
//			} break;
//			case R.id.ttsHighlight: {
//				CircleCheckBox checker = (CircleCheckBox) v;
//				checker.toggle(false);
//				opt.setTTSHighlightWebView(checker.isChecked());
//			} break;
		}
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		if (!isLongClick) {
			popupMenuHelper.dismiss();
			switch (v.getId()) {
			}
			return true;
		}
		return false;
	}
	
	public void show() {
		if (!isVisible()) {
			toggle(a.root, null, -1);
		} else if (getLastShowType()==2) {
			ViewUtils.ensureTopmost(dialog, a, dialogDismissListener);
		}
	}
	
	@Override
	protected void showDialog() {
		//super.showDialog();
		BottomSheetDialog bPane = (BottomSheetDialog) dialog;
		if(bPane==null) {
			CMN.debug("重建底部弹出");
			dialog = bPane = new BottomSheetDialog(a);
			bPane.setContentView(settingsLayout);
			bPane.getWindow().setDimAmount(0.2f);
			//CMN.recurseLogCascade(lv);
			bPane.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);// 展开
		}
		refreshExpand();
		super.showDialog();
	}
}
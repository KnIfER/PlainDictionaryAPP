package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.CancellationSignal;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.ActivityMainBinding;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchbarTools extends PlainAppPanel implements View.OnTouchListener, View.OnFocusChangeListener {
	protected PDICMainActivity a;
	protected ViewGroup rootView;
	private int fc;
	ViewGroup lv;
	private RecyclerView mRecycler;
	private RecyclerView.Adapter mAdapter;
	private boolean isDirty;
	private boolean loaded;
	
	/** 清空文本前记录一下。 */
	private String lastTx;
	
	/** 搜索记录，是动态记载与数据库结合。数据库只在开始时加载一次。 */
	ArrayList<String> history = new ArrayList<>(1024);
	/** 搜索记录最大条目数。 */
	int historyMax=512;
	/** 搜索记录的Hash索引计数。 */
	SparseIntArray hIdx = new SparseIntArray(1024);
	
	/** 0=始终关闭, 1=始终开启, 2=记忆 */
	int bAutoDrpdn=2;
	boolean drpdn;
	View drpBtn;
	
	/** 添加搜索记录。 */
	public void addHistory(String text) {
		int rmIdx = -1;
		if (text!=null && loaded) {
			boolean ndp = true;
			if (ndp) {
				int ln=text.length();
				int hash = (ln<<(32-IU.bitCnt(ln)))|text.hashCode();
				int cnt = hIdx.get(hash);
				if (cnt<0) {
					hIdx.put(hash, 1);
				} else {
					rmIdx = history.lastIndexOf(text);
					if(rmIdx<0)
						hIdx.put(hash, cnt+1);
					else if(rmIdx==history.size()-1) {
						CMN.debug("一毛一样！");
						return;
					}
				}
			}
			if (rmIdx>0)
				history.remove(rmIdx);
			history.add(text);
			if (history.size()>historyMax*2) {
				history.subList(0, history.size()-historyMax).clear();
			} else if(ndp) {
				untrackDp();
			}
			if (mAdapter != null) {
				if (isVisible()) {
					if (rmIdx>=0) {
						mAdapter.notifyItemMoved(history.size()-2-rmIdx, 0);
					} else {
						mAdapter.notifyItemInserted(0);
					}
					//mAdapter.notifyDataSetChanged();
				} else {
					isDirty = true;
				}
			}
		}
	}
	
	private void untrackDp() {
		int idx=history.size()-historyMax;
		if (idx>=0) {
			String text = history.get(idx);
			int ln=text.length();
			int hash = (ln<<(32-IU.bitCnt(ln)))|text.hashCode();
			idx = hIdx.indexOfKey(hash);
			if (idx>0) {
				int cnt = hIdx.valueAt(idx)-1;
				if (cnt>0)
					hIdx.put(hash, cnt);
				else
					hIdx.removeAt(idx);
			}
		}
	}
	
	public String getHistoryAt(int pos) {
		try {
			pos = history.size()-pos-1;
			return history.get(pos);
		} catch (Exception e) {
			return null;
		}
	}
	
	public int getHistorySz() {
		return Math.min(historyMax, history.size());
	}
	
	public SearchbarTools(PDICMainActivity a, EditText etSearch, ViewGroup rv) {
		super(a, true);
		this.bottomPadding = 0;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.bAutoRefresh = true;
		this.etSearch=etSearch;
		this.rootView=rv;
		//showType = 2;
		etSearch.setOnTouchListener(this);
		etSearch.setOnClickListener(this);
		etSearch.setOnFocusChangeListener(this);
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(PDICMainActivity) context;
			mBackgroundColor = 0;
		}
		if (settingsLayout==null) {
			ActivityMainBinding uiData = a.UIData;
			ViewUtils.setOnClickListenersOneDepth(uiData.etSearchBar, this, 999, 0, null);
			settingsLayout = uiData.etSearchBar;
			flowBtn = uiData.toolbar.findViewById(R.id.action_menu_presenter);
			drpBtn = uiData.schDropdown;
			if(true) {
				((ViewGroup)uiData.etBack.getParent()).setBackgroundColor(a.MainAppBackground);
				fc = ColorUtils.blendARGB(Color.WHITE,a.MainBackground, 0.45f) & 0xf0ffffff;
				uiData.etSearchBar.getChildAt(0).setBackgroundColor(fc);
				uiData.etSearchBar.getChildAt(2).setBackgroundColor(fc);
				LayerDrawable ld = (LayerDrawable)drpBtn.getBackground();
				PorterDuffColorFilter cf = new PorterDuffColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
				for (int i = 0; i < ld.getNumberOfLayers()-1; i++) {
					ld.getDrawable(i).setColorFilter(cf);
				}
			}
		}
	}
	
	private void initList() {
		if (mRecycler==null) {
			lv = (ViewGroup) a.getLayoutInflater().inflate(R.layout.recyclerview, a.root, false);
			RecyclerView rv = (RecyclerView) lv.getChildAt(0);
			View backBtn = lv.findViewById(R.id.etBack);
			ViewUtils.setOnClickListenersOneDepth((ViewGroup) backBtn.getParent(), this, 1, 0, null);
			int spanSz = 3;
			GridLayoutManager lm = new GridLayoutManager(a, spanSz);
			rv.setLayoutManager(lm);
			TypedArray ta = a.obtainStyledAttributes(new int[] {R.attr.listChoiceBackgroundIndicator});
			Drawable draw = ta.getDrawable(0);
			ta.recycle();
			rv.addItemDecoration(new RecyclerView.ItemDecoration(){
				final ColorDrawable mDivider = new ColorDrawable(fc);
				final int mDividerHeight = (int) (GlobalOptions.density*1);
				@Override
				public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
					//int rows = (int) Math.ceil(a.schHistory.size()*1.f/spanSz)-1;
					if (mDivider != null) {
						final int childCount = parent.getChildCount();
						final int width = parent.getWidth();
						final int height = parent.getHeight();
						for (int i = 0; i < childCount; i++) {
							final View view = parent.getChildAt(i);
							int pos = parent.getChildViewHolder(view).getLayoutPosition();
							if (pos%spanSz==0/* && pos/spanSz!=rows*/){
								int top = (int) view.getY() + view.getHeight();
								mDivider.setBounds(0, top, width, top + mDividerHeight);
								mDivider.draw(c);
							}
							if ((pos+1)%spanSz!=0 && i/spanSz==0){
								int left = (int) view.getX() + view.getWidth();
								mDivider.setBounds(left, 0, left + mDividerHeight, height);
								mDivider.draw(c);
							}
						}
					}
				}
				@Override
				public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
					int pos = parent.getChildViewHolder(view).getLayoutPosition();
					if (true){//shouldDrawDividerBelow(view, parent)) {
						outRect.bottom = mDividerHeight;
					}
					if ((pos+1)%spanSz!=0){
						outRect.right = mDividerHeight/2;
					}
				}
			});
			rv.setNestedScrollingEnabled(false);
			lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					return 1;
				}
			});
			int pad = (int) (9.5*GlobalOptions.density);
			View.OnClickListener vc = v -> {
				RecyclerView.ViewHolder vh = (RecyclerView.ViewHolder) v.getTag(R.id.views_holder);
				String text = getHistoryAt(vh.getLayoutPosition());
				if (text!=null){
					etSearch.setText(text);
					etSearch.setSelection(text.length());
					hideIM();
					dismiss();
				}
			};
			rv.setAdapter(mAdapter=new RecyclerView.Adapter() {
				@NonNull @Override
				public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
					//CMN.Log("onCreateView!!!",CMN.now());
					TextView tv = new TextView(parent.getContext());
					tv.setMaxLines(2);
					tv.setMinHeight((int) (GlobalOptions.density*48));
					tv.setGravity(Gravity.CENTER_VERTICAL);
					tv.setEllipsize(TextUtils.TruncateAt.END);
					tv.setPadding(pad, 0, pad/4, 0);
					tv.setTextSize(GlobalOptions.isLarge?19:17);
					tv.setTextColor(Color.WHITE);
					RecyclerView.ViewHolder ret = new ViewHolder(tv);
					tv.setBackground(draw.getConstantState().newDrawable());
					tv.setClickable(true);
					tv.setTag((Runnable) () -> {
						if (tv.getLineCount()>1)
							tv.setTextSize(GlobalOptions.isLarge?17:15);
						else
							tv.setTextSize(GlobalOptions.isLarge?19:17);
					});
					tv.setTag(R.id.views_holder, ret);
					tv.setOnClickListener(vc);
					return ret;
				}
				@Override
				public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
					TextView tv = ((TextView)holder.itemView);
					tv.setText(getHistoryAt(position));
					tv.post((Runnable) tv.getTag());
				}
				@Override
				public int getItemCount() {
					return getHistorySz();
				}
			});
			//rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
			//rv.setPadding(0, (int) (GlobalOptions.density*8),0,0);
			ViewUtils.addViewToParent(lv, (ViewGroup) drpBtn.getParent());
			lv.setBackgroundColor(a.MainAppBackground);
			lv.setPadding(pad/4,0,pad/4,0);
			lv.getLayoutParams().height=-2;
			mRecycler = rv;
		}
		ViewUtils.setVisible(drpBtn, false);
		if (!ViewUtils.isVisible(lv)) {
			ViewUtils.setVisible(lv, true);
		}
		if(true) {
			lv.setAnimation(AnimationUtils.loadAnimation(a, R.anim.item_animation_fall_down));
		}
		LoadHistory(null);
		//for (int i = 0; i < 1000; i++) {
		//	addHistory("happy");
		//	addHistory("1024");
		//	addHistory("joy");
		//	addHistory("fun");
		//	addHistory("minecraft");
		//	addHistory("duty");
		//	addHistory("destiny of the huawei device");
		//}
	}
	
	public void hideIM() {
		a.imm.hideSoftInputFromWindow(a.UIData.etSearch.getWindowToken(),0);
	}
	
	private boolean shouldOpenDrpDwn() {
		return bAutoDrpdn==1 || bAutoDrpdn==2&&drpdn;
	}
	
	@Override
	public void refresh() {
		if(shouldOpenDrpDwn()) {
			initList();
		} else if(ViewUtils.isVisibleV2(lv)) {
			ViewUtils.setVisible(lv, false);
		}
		if (settingsLayout!=null) {
			((ViewGroup.MarginLayoutParams)settingsLayout.getLayoutParams()).topMargin = a.UIData.appbar.getHeight();
			if(isDirty)
				mAdapter.notifyDataSetChanged();
		}
	}
	
	public void LoadHistory(AtomicBoolean task) {
		if(task==null) {
			if(!loaded) {
				loaded = true;
				a.wordPopup.startTask(WordPopupTask.TASK_LOAD_HISTORY);
			}
			return;
		}
		CancellationSignal cs = new CancellationSignal();
		a.root.postDelayed(cs::cancel, 250); // 防止过度读取
		String[] items = null;
		Cursor cursor = null;
		try {
			cursor = a.prepareHistoryCon().getDB().rawQuery(
				"select lex from " + LexicalDBHelper.TABLE_HISTORY_v2
				+ " where src>=128 order by " + LexicalDBHelper.FIELD_VISIT_TIME + " desc limit 512"
				, null, cs);
			int cc=0,len=cursor.getCount();
			items = new String[len];
			len--;
			while (cursor.moveToNext()) {
				//CMN.Log("LoadHistory::", cursor.getString(0));
				items[len-cc++] = cursor.getString(0);
			}
		} catch (Exception e) {
			CMN.debug(e);
			if (cursor!=null)
				cursor.close();
			throw e;
		}
		if (items!=null) {
			String[] its = items;
			a.root.post(() -> { //harvest
				history.clear();
				hIdx.clear();
				history.addAll(Arrays.asList(its));
				for (int i = 0; i < its.length; i++) {
					String text = its[i];
					if (text!=null) {
						int ln=text.length();
						int hash = (ln<<(32-IU.bitCnt(ln)))|text.hashCode();
						hIdx.put(hash, 1);
					}
				}
				if (mAdapter!=null)
					mAdapter.notifyDataSetChanged();
				if(a.startLastSch) {
					a.setSearchTerm(its[its.length-1]);
					a.startLastSch=false;
				}
			});
		}
	}
	
	
	static class ViewHolder extends RecyclerView.ViewHolder
	{
		public ViewHolder(View view)
		{
			super(view);
		}
	}
	
	@SuppressLint({"ResourceType", "NonConstantResourceId"})
	@Override
	// click
	public void onClick(View v) {
		CMN.Log("onclick::", v);
		if (v==etSearch) {
			CMN.Log("click!!!"+etSearch.getScrollX());
			if(etSearch.getScrollX()== etScrollStart) {
				if(!isVisible()) {
					show();
				}
			}
		}
		else switch (v.getId()) {
			case R.id.show_search_history_dropdown_bg:
			case R.id.etBack:
			case R.id.action_menu_presenter:
				hideIM();
				dismiss();
			break;
			case R.id.foldBtn:
				ViewUtils.setVisibleV2(lv, false);
				ViewUtils.setVisible(drpBtn, true);
				drpdn = false;
			break;
			case R.id.etClear:{
				Editable tx = etSearch.getText();
				if(TextUtils.getTrimmedLength(tx)>0) {
					lastTx = tx.toString();
				}
				etSearch.setText("");
				a.imm.showSoftInput(etSearch, 0);
			} break;
			case R.id.etPaste:{
				ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
				if(cm!=null) {
					ClipData pclip = cm.getPrimaryClip();
					ClipData.Item firstItem = pclip.getItemAt(0);
					CharSequence content = firstItem.getText();
					a.bIsFirstLaunch=true;
					a.bWantsSelection=false;
					etSearch.setText(content);
					etSearch.setSelection(content.length());
				}
			} break;
			case R.id.etCopy:{
				CharSequence tx = etSearch.getText();
				if(TextUtils.getTrimmedLength(tx)>0) {
					ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
					if(cm!=null){
						cm.setPrimaryClip(ClipData.newPlainText(null, tx));
					}
				} else {
					etSearch.setText(lastTx);
				}
			} break;
			case R.id.schDropdown:{
				initList();
				drpdn=true;
			} break;
		}
	}
	
	public void show() {
		if (!isVisible()) {
			toggle(rootView!=null?rootView:PDICMainAppOptions.getEnableSuperImmersiveScrollMode()?a.UIData.webcoord:a.root, null, -1);
			refresh();
		}
	}
	
	@Override
	protected void onShow() {
		flowBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		if (!shouldOpenDrpDwn() && ViewUtils.isVisibleV2(lv)) {
			ViewUtils.setVisible(lv, false);
		}
		flowBtn.setOnClickListener(null);
	}
	
	int etScrollStart;
	long checkNxtFocus = 0;
	EditText etSearch;
	View flowBtn;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			etScrollStart = etSearch.getScrollX();
			checkNxtFocus = event.getEventTime();
			CMN.Log("touch!!!"+ etScrollStart);
		}
		return false;
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		//CMN.Log("onFocusChange::", hasFocus, a.systemIntialized);
		if(hasFocus) {
			if(checkNxtFocus!=0) {
				if(SystemClock.uptimeMillis()-checkNxtFocus<250) {
					if(!isVisible())
						show();
				}
				checkNxtFocus=0;
			}
		} else {
			dismiss();
		}
	}
	
	
}

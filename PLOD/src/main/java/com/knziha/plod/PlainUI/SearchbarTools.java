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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchbarTools extends PlainAppPanel implements View.OnTouchListener, View.OnFocusChangeListener {
	protected ViewGroup rootView;
	private int fc;
	ViewGroup lv;
	private RecyclerView mRecycler;
	private RecyclerView.Adapter mAdapter;
	private boolean isDirty;
	private boolean loaded;
	private View initView;
	public View.OnClickListener initWay;
	
	/** 清空文本前记录一下。 */
	private String lastTx;
	
	/** 搜索记录，是动态记载与数据库结合。数据库只在开始时加载一次。 */
	ArrayList<String> history = new ArrayList<>(1024);
	/** 搜索记录最大条目数。 */
	int historyMax=512;
	/** 搜索记录的Hash索引计数。 */
	SparseIntArray hIdx = new SparseIntArray(1024);
	
	public boolean drpdn;
	ImageView drpBtn;
	public View topbar;
	public String schSql = "src==128";
	private View etSchExit;
	private FrameLayout.LayoutParams etSchExitLP;
	
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
	
	public SearchbarTools(MainActivityUIBase a, EditText etSearch, View initView, ViewGroup rv, boolean scrollHideIM) {
		super(a, false);
		this.bottomPadding = 0;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.bAutoRefresh = true;
		this.rootView=rv;
		this.bShouldInterceptClickListener=false;
		this.initView=initView;
		this.scrollHideIM=scrollHideIM;
		//showType = 2;
		setEtSch(etSearch);
	}
	
	public void setEtSch(EditText e) {
		if (e!=null) {
			this.etSearch=e;
			e.setOnTouchListener(this);
			e.setOnClickListener(this);
			e.setOnFocusChangeListener(this);
		}
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(context==null)
			return;
		if(a==null) {
			a=(MainActivityUIBase) context;
			mBackgroundColor = 0;
		}
		if (settingsLayout==null) {
			ViewGroup etBar = (ViewGroup) initView;
			if(etBar==null) etBar = (ViewGroup) a.getLayoutInflater().inflate(R.layout.etsch_tools, root, false);
			ViewUtils.setOnClickListenersOneDepth(etBar, this, 999, 0, null);
			settingsLayout = etBar;
			drpBtn = etBar.getChildAt(etBar.getChildCount()-1).findViewById(R.id.schDropdown);
			{
				fc = ColorUtils.blendARGB(Color.WHITE,a.MainBackground, 0.45f) & 0xf0ffffff;
				etBar.getChildAt(0).setBackgroundColor(fc);
				etBar.getChildAt(1).setBackgroundColor(a.MainAppBackground);
				etBar.getChildAt(2).setBackgroundColor(fc);
				LayerDrawable ld = (LayerDrawable)drpBtn.getBackground().mutate();
				PorterDuffColorFilter cf = new PorterDuffColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
				for (int i = 0; i < ld.getNumberOfLayers()-1; i++) {
					ld.getDrawable(i).setColorFilter(cf);
				}
			}
			if(initWay!=null) {
				initWay.onClick(drpBtn);
			}
		}
	}
	
	boolean IMHidden;
	final boolean scrollHideIM;
	boolean scrollHideIM() {
		return scrollHideIM;
	}
	
	private void initList() {
		if (mRecycler==null) {
			lv = (ViewGroup) a.getLayoutInflater().inflate(R.layout.recyclerview, a.root, false);
			RecyclerView rv = (RecyclerView) lv.getChildAt(0);
			View backBtn = lv.findViewById(R.id.etBack);
			ViewUtils.setOnClickListenersOneDepth((ViewGroup) backBtn.getParent(), this, 1, 0, null);
			int spanSz = 3; GridLayoutManager lm;
			if(scrollHideIM) {
				lm = new GridLayoutManager(a, spanSz) {
					@Override
					public int scrollVerticallyBy ( int dx, RecyclerView.Recycler recycler, RecyclerView.State state ) {
						int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
						if(!IMHidden && dx!=scrollRange && scrollHideIM()) {
							hideIM();
							IMHidden=true;
						}
						return scrollRange;
					}
				};
				rv.setOnScrollChangedListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
					//CMN.Log("scroll chaned!!!");
					if(!IMHidden
							&& rv.getScrollState()==RecyclerView.SCROLL_STATE_DRAGGING
							&& scrollHideIM()
					) {
						hideIM();
						IMHidden=true;
					}
				});
			} else {
				lm = new GridLayoutManager(a, spanSz);
			}
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
					if(spanSz>3)
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
			etSchExit = lv.getChildAt(1);
			etSchExitLP = (FrameLayout.LayoutParams) etSchExit.getLayoutParams();
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
		resizeModeSoft(true);
		
		refreshSoftMode(GlobalOptions.softInputHeight);
		
//		if(PDICMainAppOptions.etSchExitTop()!=((etSchExitLP.gravity&Gravity.BOTTOM)==0)) {
//			etSchExitLP.gravity &= ~(Gravity.BOTTOM|Gravity.TOP);
//			etSchExitLP.gravity |= PDICMainAppOptions.etSchExitTop()?Gravity.TOP:Gravity.BOTTOM;
//		}
	}
	
	private void resizeModeSoft(boolean soft) {
		if (initView!=null && !PDICMainAppOptions.etSchAlwaysHard()) {
			((PDICMainActivity)a).setSoftInputMode(soft?PDICMainActivity.softModeResize:PDICMainActivity.softModeNothing);
		}
	}
	
	public void hideIM() {
		a.imm.hideSoftInputFromWindow(etSearch.getWindowToken(),0);
	}
	
	private boolean shouldOpenDrpDwn() {
		final int bAutoDrpdn = PDICMainAppOptions.historyAutoShow();
		return bAutoDrpdn==1 || bAutoDrpdn==2&&drpdn;
	}
	
	@Override
	public void refresh() {
		if(shouldOpenDrpDwn()) {
			if(initView!=null && PDICMainAppOptions.delaySchLvHardSoftUI()) {
				resizeModeSoft(true);
				lv.setVisibility(View.INVISIBLE);
				a.hdl.postDelayed(this::initList, 230); // 自动，稍后
			}
			else initList(); // 自动，立即。
		} else if(ViewUtils.isVisibleV2(lv)) {
			ViewUtils.setVisible(lv, false);
		}
		((ViewGroup.MarginLayoutParams)settingsLayout.getLayoutParams()).topMargin = topbar==null?0:topbar.getHeight();
		if(isDirty && mAdapter!=null)
			mAdapter.notifyDataSetChanged();
	}
	
	@AnyThread
	public void LoadHistory(AtomicBoolean task) {
		if(task==null) {
			if(!loaded) {
				loaded = true;
				a.wordPopup.etTools = this;
				a.wordPopup.startTask(WordPopupTask.TASK_LOAD_HISTORY);
			}
			return;
		}
		CancellationSignal cs = new CancellationSignal();
		a.hdl.postDelayed(cs::cancel, 250); // 防止过度读取
		String[] items = null;
		Cursor cursor = null;
		try {
			cursor = a.prepareHistoryCon().getDB().rawQuery(
				"select lex from " + LexicalDBHelper.TABLE_HISTORY_v2
				+ " where "+schSql+" order by " + LexicalDBHelper.FIELD_VISIT_TIME + " desc limit 512"
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
			a.hdl.post(() -> { //harvest
				history.clear();
				hIdx.clear();
				history.addAll(Arrays.asList(its));
				//CMN.Log("LoadHistory::harvest::", getHistorySz(), its);
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
				if(a.restLastSch) {
					if(its.length>0) {
						boolean ns = !a.opt.autoSchPDict();
						if(ns) a.getEdit().removeTextChangedListener(a.tw1);
						a.getEdit().setText(its[its.length-1]);
						if(ns) a.getEdit().addTextChangedListener(a.tw1);
					}
					a.restLastSch =false;
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
		if (v==etSearch) {
			if(etSearch.getScrollX()==etScrollStart) {
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
				drpdn(false);
				resizeModeSoft(false);
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
					if (pclip!=null) {
						ClipData.Item firstItem = pclip.getItemAt(0);
						CharSequence content = firstItem.getText();
						a.bIsFirstLaunch=true;
						a.bWantsSelection=false;
						etSearch.setText(content);
						etSearch.setSelection(content.length());
					}
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
				initList(); // 手动
				drpdn(true);
			} break;
		}
	}
	
	private void drpdn(boolean b) {
		drpdn = b;
		if(initView!=null) {
			PDICMainAppOptions.historyShow(b);
		} else {
			PDICMainAppOptions.historyShowFye(b);
		}
	}
	
	public void show() {
		if (!isVisible()) {
			//rootView = PDICMainAppOptions.getEnableSuperImmersiveScrollMode()?a.UIData.webcoord:a.root;
			toggle(rootView, null, -1);
			refresh();
		}
	}
	
	@Override
	protected void onShow() {
		if(flowBtn!=null)
			flowBtn.setOnClickListener(this);
		if (this==a.etTools) {
			a.textFlag |= 0x4;
		}
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		if (!shouldOpenDrpDwn() && ViewUtils.isVisibleV2(lv)) {
			ViewUtils.setVisible(lv, false);
		}
		if(flowBtn!=null)
			flowBtn.setOnClickListener(null);
		resizeModeSoft(false);
	}
	
	int etScrollStart;
	long checkNxtFocus = 0;
	EditText etSearch;
	public View flowBtn;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			etScrollStart = etSearch.getScrollX();
			checkNxtFocus = event.getEventTime();
			//CMN.Log("touch!!!"+ etScrollStart);
			if(scrollHideIM && IMHidden) {
				IMHidden = false;
			}
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
			if(scrollHideIM && IMHidden) {
				IMHidden = false;
			}
		} else {
			dismiss();
		}
	}
	
	@Override
	public void refreshSoftMode(int height) {
		if(etSchExitLP!=null && ViewUtils.isVisible(lv)) {
			boolean hard = PDICMainAppOptions.etSchAlwaysHard();
			if (etSchExit.getParent()==lv ^ !hard) {
				ViewUtils.addViewToParent(etSchExit, hard? (ViewGroup) lv.getParent() :lv);
				etSchExitLP = (FrameLayout.LayoutParams) etSchExit.getLayoutParams();
			}
			if(!hard) height=0;
			if (etSchExitLP.bottomMargin != height) {
				etSchExitLP.bottomMargin = height;
				etSchExit.requestLayout();
			}
		}
	}
}

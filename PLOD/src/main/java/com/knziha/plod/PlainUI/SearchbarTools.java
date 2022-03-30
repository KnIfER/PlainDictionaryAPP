package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
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

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.ActivityMainBinding;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;

public class SearchbarTools extends PlainAppPanel implements View.OnTouchListener, View.OnFocusChangeListener {
	protected PDICMainActivity a;
	protected ViewGroup rootView;
	private int fc;
	ViewGroup lv;
	private RecyclerView mRecycler;
	private boolean isDirty;

	ArrayList<String> history = new ArrayList<>(1024);
	int historyMax=512;
	SparseIntArray hIdx = new SparseIntArray(1024);
	private String lastTx;
	
	public void addHistory(String text) {
		boolean ndp = true;
		int rmIdx = -1;
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
		if (mRecycler != null) {
			if (isVisible()) {
				//mRecycler.getAdapter().notifyItemInserted(0);
				mRecycler.getAdapter().notifyDataSetChanged();
			} else {
				isDirty = true;
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
		ActivityMainBinding uiData = a.UIData;
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
					//hideIM();
					dismiss();
				}
			};
			rv.setAdapter(new RecyclerView.Adapter() {
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
	}
	
	public void hideIM() {
		a.imm.hideSoftInputFromWindow(a.UIData.etSearch.getWindowToken(),0);
	}
	
	/** 0=始终关闭, 1=始终开启, 2=记忆 */
	int bAutoDrpdn=2;
	boolean drpdn;
	View drpBtn;
	
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
//			ViewGroup.LayoutParams lp = settingsLayout.getLayoutParams();
//			if(lp instanceof ViewGroup.MarginLayoutParams) {
//				((ViewGroup.MarginLayoutParams) lp).topMargin = a.UIData.toolbar.getHeight();
//			}
			if (isDirty) {
				mRecycler.getAdapter().notifyDataSetChanged();
			}
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
//			if (v.getId() != R.drawable.ic_menu_24dp) {
//				a.mInterceptorListenerHandled = true;
//			}
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
//	public void setEt(EditText etSearch, ViewGroup rv) {
//		this.etSearch=etSearch;
//		this.rootView=rv;
//		etSearch.setOnTouchListener(this);
//		etSearch.setOnClickListener(this);
//	}
	
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
		CMN.Log("onFocusChange::", hasFocus, a.systemIntialized);
//				ViewUtils.findInMenu(AllMenusStamp, R.id.toolbar_action2)
//						.setIcon(hasFocus?R.drawable.ic_search_24k:R.drawable.ic_back_material);
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

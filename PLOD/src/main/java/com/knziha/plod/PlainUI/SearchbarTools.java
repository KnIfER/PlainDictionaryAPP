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
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
		pos = history.size()-pos-1;
		return history.get(pos);
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
			
			if(true) {
				((ViewGroup)uiData.etBack.getParent()).setBackgroundColor(a.MainAppBackground);
				fc = ColorUtils.blendARGB(Color.WHITE,a.MainBackground, 0.45f) & 0xf0ffffff;
				uiData.etSearchBar.getChildAt(0).setBackgroundColor(fc);
				uiData.etSearchBar.getChildAt(2).setBackgroundColor(fc);
				LayerDrawable ld = (LayerDrawable)uiData.schDropdown.getBackground();
				PorterDuffColorFilter cf = new PorterDuffColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
				for (int i = 0; i < ld.getNumberOfLayers()-1; i++) {
					ld.getDrawable(i).setColorFilter(cf);
				}
			}
		}
	}
	
	/** 0=始终关闭, 1=始终开启, 2=记忆 */
	int bAutoDrpdn=0;
	boolean drpdn;
	
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
	public void onClick(View v) {
		CMN.Log("onclick::", v);
		if (v==etSearch) {
			CMN.Log("click!!!"+etSearch.getScrollX());
			if(etSearch.getScrollX()==etTouchScrollStart) {
				if(!isVisible()) {
					show();
				}
			}
		}
		else switch (v.getId()) {
			case R.id.show_search_history_dropdown_bg:
			case R.id.etBack:
				dismiss();
			break;
			case R.id.etClear:
				etSearch.setText("");
				a.imm.showSoftInput(etSearch, 0);
			break;
			case R.id.etPaste:{
				ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
				if(cm!=null){
					ClipData pclip = cm.getPrimaryClip();
					ClipData.Item firstItem = pclip.getItemAt(0);
					CharSequence content = firstItem.getText();
					etSearch.setText(content);
					etSearch.setSelection(content.length());
				}
			} break;
			case R.id.etCopy:{
				ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
				if(cm!=null){
					cm.setPrimaryClip(ClipData.newPlainText(null, etSearch.getText()));
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
	
	private void show() {
		toggle(rootView!=null?rootView:PDICMainAppOptions.getEnableSuperImmersiveScrollMode()?a.UIData.webcoord:a.root, null, -1);
		refresh();
	}
	
	private void initList() {
		ActivityMainBinding uiData = a.UIData;
		if (mRecycler==null) {
			lv = (ViewGroup) a.getLayoutInflater().inflate(R.layout.recyclerview, a.root, false);
			RecyclerView rv = (RecyclerView) lv.getChildAt(0);
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
					return ret;
				}
				@Override
				public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
					TextView tv = ((TextView)holder.itemView);
					tv.setTextSize(GlobalOptions.isLarge?19:17);
					tv.setText(getHistoryAt(position));
					tv.post(new Runnable() {
						@Override
						public void run() {
							if (tv.getLineCount()>1)
								tv.setTextSize(GlobalOptions.isLarge?17:15);
						}
					});
				}
				@Override
				public int getItemCount() {
					return getHistorySz();
				}
			});
			//rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
			//rv.setPadding(0, (int) (GlobalOptions.density*8),0,0);
			ViewUtils.addViewToParent(lv, (ViewGroup) uiData.schDropdown.getParent());
			lv.setBackgroundColor(a.MainAppBackground);
			lv.setPadding(pad/4,0,pad/4,0);
			lv.getLayoutParams().height=-2;
			mRecycler = rv;
		}
		//uiData.etSearch.setVisibility(View.INVISIBLE);
		if (!ViewUtils.isVisible(lv)) {
			ViewUtils.setVisible(lv, true);
		}
		if(true) {
			lv.setAnimation(AnimationUtils.loadAnimation(a, R.anim.item_animation_fall_down));
		}
	}
	
	@Override
	protected void onDismiss() {
		CMN.Log("onDismiss::");
		super.onDismiss();
		a.imm.hideSoftInputFromWindow(a.UIData.etSearch.getWindowToken(),0);
		if (!shouldOpenDrpDwn() && ViewUtils.isVisibleV2(lv)) {
			ViewUtils.setVisible(lv, false);
		}
	}
	
	int etTouchScrollStart;
	long checkNxtFocus = 0;
	EditText etSearch;
//	public void setEt(EditText etSearch, ViewGroup rv) {
//		this.etSearch=etSearch;
//		this.rootView=rv;
//		etSearch.setOnTouchListener(this);
//		etSearch.setOnClickListener(this);
//	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			etTouchScrollStart = etSearch.getScrollX();
			checkNxtFocus = event.getEventTime();
			CMN.Log("touch!!!"+etTouchScrollStart);
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

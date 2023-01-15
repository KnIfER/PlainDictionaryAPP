package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.net.Uri;
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
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PeruseView;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.tesseraction.Tesseraction;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchbarTools extends PlainAppPanel implements View.OnTouchListener, View.OnFocusChangeListener {
	protected ViewGroup rootView;
	private int fc;
	private boolean etHistoryExpanded;
	ViewGroup lv;
	private ImageView expandBtn;
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
	private View RvTools;
	private FrameLayout.LayoutParams lpRvTools;
	private FrameLayout.LayoutParams lpRv;
	public WordCamera wordCamera;
	
	/** 添加搜索记录。 */
	public void addHistory(String text) {
//		try {
//			throw new RuntimeException();
//		} catch (RuntimeException e) {
//			CMN.debug(e);
//		}
		int rmIdx = -1;
		if (text!=null && loaded) {
			boolean ndp = true;
			if (ndp) {
				int ln=text.length();
				int hash = (ln<<(32-IU.bitCnt(ln)))|text.hashCode();
				int cnt = hIdx.get(hash);
				if (cnt<0) {
					hIdx.put(hash, 1); // 铁定收录
				} else {
					rmIdx = history.lastIndexOf(text);
					if(rmIdx<0)
						hIdx.put(hash, cnt+1); // 撞hash了
					else if(rmIdx==history.size()-1) {
						CMN.debug("一毛一样！");
						return;
					}
				}
			}
			// 先移除，再添加
			if (rmIdx>0) history.remove(rmIdx);
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
		int pos=history.size()-historyMax; // 你要出去
		if (pos>=0) {
			untrackText(history.get(pos), false);
		}
	}
	
	private void untrackText(String text, boolean del) {
		int ln=text.length();
		int hash = (ln<<(32-IU.bitCnt(ln)))|text.hashCode();
		int idx = hIdx.indexOfKey(hash);
		if (idx>=0) {
			int cnt = hIdx.valueAt(idx)-1;
			if (cnt>0)
				hIdx.put(hash, cnt); // 苟延残喘
			else
				hIdx.removeAt(idx);
		}
		if (del && true) {
			a.prepareHistoryCon().getDB().delete(LexicalDBHelper.TABLE_HISTORY_v2, "lex=?", new String[]{text});
			a.prepareHistoryCon().incrementDBHistory();
		}
	}
	
	private void retrackDp() {
		int idx=history.size()-historyMax; // 你要进来
		if (idx>=0) {
			String text = history.get(idx);
			int ln=text.length();
			int hash = (ln<<(32-IU.bitCnt(ln)))|text.hashCode();
			idx = hIdx.indexOfKey(hash);
			if (idx < 0) {
				hIdx.put(hash, 1); // 死灰复燃
			} else {
				hIdx.put(hash, hIdx.valueAt(idx)+1); // 东山再起
			}
		}
	}
	
	private void deleteAt(int pos, boolean deleteAll) {
		try {
			pos = history.size()-pos-1;
			if (pos >= 0) {
				if (deleteAll) {
					for (int i = pos; i >= 0; i--) {
						retrackDp();
						untrackText(history.remove(i), true);
					}
				} else {
					retrackDp();
					untrackText(history.remove(pos), true);
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
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
		bindEdit(etSearch);
	}
	
	public void bindEdit(EditText e) {
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
			refreshColors();
			if(initWay!=null) {
				a.mInterceptorListener = null;
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
			etHistoryExpanded = PDICMainAppOptions.etHistoryExpanded();
			lv = (ViewGroup) a.getLayoutInflater().inflate(R.layout.etsch_recyclerview, a.root, false);
			ViewUtils.setForegroundColor(lv, a.getForegroundColor(), VU.sForegroundFilter, VU.sForegroundTint);
			RecyclerView rv = (RecyclerView) lv.getChildAt(0);
			expandBtn = lv.findViewById(R.id.more);
			ViewUtils.setOnClickListenersOneDepth((ViewGroup) expandBtn.getParent(), this, 1, 0, null);
			int spanSz = PDICMainAppOptions.schHistorySpanSize(); GridLayoutManager lm;
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
						int spanSz = PDICMainAppOptions.schHistorySpanSize();
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
					int spanSz = PDICMainAppOptions.schHistorySpanSize();
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
			final int pad = (int) (9.5*GlobalOptions.density);
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
			View.OnLongClickListener vc1 = v -> {
				RecyclerView.ViewHolder vh = (RecyclerView.ViewHolder) v.getTag(R.id.views_holder);
				int pos = vh.getLayoutPosition();
				//String text = getHistoryAt(pos);
				boolean b1 = a.keyboardShown;
				PopupMenuHelper popupMenu = a.getPopupMenu();
				int[] vLocationOnScreen = new int[2];
				popupMenu.initLayout(new int[]{
						R.layout.page_lieshu
						//,R.string.page_history_scope
						,R.string.page_del_this
						,R.string.page_del_prev
						, R.layout.page_lnk_fanyi1
				}, new PopupMenuHelper.PopupMenuListener() {
					long deleting;
					@Override
					public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View view, boolean isLongClick) {
						final int id = view.getId();
						boolean changed = true;
						switch (id) {
							case R.id.btn1:
							case R.id.btn2:
							case R.id.btn3:
								GridLayoutManager lm = (GridLayoutManager) rv.getLayoutManager();
								PDICMainAppOptions.schHistorySpanSize(id == R.id.btn1 ? 1 : id == R.id.btn2 ? 2 : 3);
								lm.setSpanCount(PDICMainAppOptions.schHistorySpanSize());
							break;
							case R.string.page_del_this: // 删除此项
								deleteAt(pos, false);
							break;
							case R.string.page_del_prev:
								if (CMN.now() - deleting > 350) {
									deleting = CMN.now();
									PopupMenuHelper pop = new PopupMenuHelper(a, null, null);
									pop.initLayout(new int[]{
											R.string.page_del_conf
									}, (popupMenuHelper1, v1, isLongClick1) -> {
										deleteAt(pos, true);
										rv.getAdapter().notifyDataSetChanged();
										popupMenuHelper.dismiss();
										popupMenuHelper1.dismiss();
										return true;
									});
									int[] x = new int[2];
									view.getLocationOnScreen(x);
									pop.showAt(v, vLocationOnScreen[0], x[1]+view.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
								}
							return true;
							case R.id.page_lnk_fye:
								a.JumpToPeruseModeWithWord(getHistoryAt(pos));
								changed = false;
								break;
							case R.id.page_lnk_tapSch:
								a.popupWord(getHistoryAt(pos), null, 0, null);
								changed = false;
								break;
							case R.id.page_lnk_share:
								a.getVtk().setInvoker(null, null, null, getHistoryAt(pos));
								a.getVtk().onClick(null);
								changed = false;
								break;
						}
						if (changed) {
							rv.getAdapter().notifyDataSetChanged();
						}
						popupMenuHelper.dismiss();
						return true;
					}
				});
				v.getLocationOnScreen(vLocationOnScreen); //todo 校准弹出位置
				popupMenu.showAt(v, vLocationOnScreen[0], vLocationOnScreen[1]+v.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
				popupMenu.mPopupWindow.setOnDismissListener(a.keyboardShown?new PopupWindow.OnDismissListener() {
					@Override
					public void onDismiss() {
						etSearch.postDelayed(new Runnable() {
							@Override
							public void run() {
								etSearch.requestFocus();
								a.imm.showSoftInput(etSearch, 0);
							}
						}, 64);
					}
				}:null);
				return true;
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
					tv.setTag((Runnable) () -> {
						if (tv.getLineCount()>1)
							tv.setTextSize(GlobalOptions.isLarge?17:15);
						else
							tv.setTextSize(GlobalOptions.isLarge?19:17);
					});
					tv.setTag(R.id.views_holder, ret);
					tv.setOnClickListener(vc);
					tv.setOnLongClickListener(vc1);
					return ret;
				}
				@Override
				public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
					TextView tv = ((TextView)holder.itemView);
					tv.setText(getHistoryAt(position));
					int spanSz = PDICMainAppOptions.schHistorySpanSize();
					int padLeft = spanSz == 1 ? pad*3 : spanSz == 2 ? pad*3/2 : pad;
					tv.setPadding(padLeft, 0, pad/4, 0);
					if(spanSz>3)
						tv.post((Runnable) tv.getTag());
					tv.setTextColor(a.MainLumen>0.65?Color.BLACK:Color.WHITE);
				}
				@Override
				public int getItemCount() {
					return getHistorySz();
				}
			});
			//rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
			//rv.setPadding(0, (int) (GlobalOptions.density*8),0,0);
			ViewUtils.addViewToParent(lv, (ViewGroup) drpBtn.getParent());
			rv.setBackgroundColor(a.MainAppBackground);
			rv.setPadding(pad/4,0,pad/4,0);
			lv.getLayoutParams().height=-1;
			RvTools = lv.getChildAt(1);
			lpRvTools = (FrameLayout.LayoutParams) RvTools.getLayoutParams();
			lpRv = (FrameLayout.LayoutParams) rv.getLayoutParams();
			mRecycler = rv;
		}
		ViewUtils.setVisible(drpBtn, false);
		redrawExpandBtn();
		
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
			((PDICMainActivity)a).setSoftInputMode(soft?PDICMainActivity.softModeResize:a.softModeStd);
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
				if(lv!=null)lv.setVisibility(View.INVISIBLE);
				a.hdl.postDelayed(this::initList, 230); // 自动，稍后
			}
			else initList(); // 自动，立即。
		} else if(ViewUtils.isVisibleV2(lv)) {
			ViewUtils.setVisible(lv, false);
		}
		((ViewGroup.MarginLayoutParams)settingsLayout.getLayoutParams()).topMargin = topbar==null?0:topbar.getHeight();
		if(isDirty && mAdapter!=null)
			mAdapter.notifyDataSetChanged();
		refreshColors();
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
		//CMN.debug("LoadHistory::"+schSql);
		try {
			cursor = a.prepareHistoryCon().getDB().rawQuery(
				"select lex from " + LexicalDBHelper.TABLE_HISTORY_v2
				+ " where "+schSql+" order by " + LexicalDBHelper.FIELD_VISIT_TIME + " desc limit 512"
				, null, cs);
			int cc=0,len=cursor.getCount();
			items = new String[len];
			len--;
			while (cursor.moveToNext()) {
				//CMN.debug("LoadHistory::", cursor.getString(0));
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
				if (a.floatApp!=null) {
					a.floatApp.enableKeyBoard(true);
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
			case R.id.more:
				PDICMainAppOptions.etHistoryExpanded(etHistoryExpanded = !etHistoryExpanded);
				redrawExpandBtn();
				mRecycler.requestLayout();
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
			case R.id.etCamera:{
				if (wordCamera == null) {
					// 检查插件是否安装
					if (!ViewUtils.isInstalled(a, Tesseraction.pluginPkg)) {
						final String url = "https://www.imdodo.com/channel/157568/889299/385849130804637696";
						new AlertDialog.Builder(a)
								.setTitle("插件安装指引")
								.setMessage("请使用浏览器安装插件：图文之心.apk，安装后将获得ocr光学识别能力，可从相机或图片拾取单词。\n安装插件需要消耗流量，之后可完全离线使用。")
								.setNegativeButton("立即前往安装", (dialog, which) -> {
									try {
										Intent intent = new Intent(Intent.ACTION_VIEW)
												.setData(Uri.parse(url));
										a.startActivity(intent);
									} catch (Exception e) {
										CMN.debug(e);
									}
								})
								.setNeutralButton("复制网址", (dialog, which) -> {
									a.copyText(url, false);
									a.showT("已复制网址！");
								})
								.setPositiveButton("取消", null)
								.show()
								;
						a.showT("未安装图文之心.apk，无法启动相机取词服务！");
						break;
					}
					wordCamera = new WordCamera(a, this);
				}
				wordCamera.show();
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
		if(!PDICMainAppOptions.hideSchTools()) {
			//rootView = PDICMainAppOptions.getEnableSuperImmersiveScrollMode()?a.UIData.webcoord:a.root;
			if (!isVisible()) {
				toggle(rootView, null, -1);
				refresh();
			}
		}
		if (this==a.etTools && a.ivBack.getId()!=R.id.multiline) {
			a.ivBack.setImageResource(R.drawable.ic_menu_material);
			a.ivBack.setId(R.id.multiline);
		}
	}
	
	public void forceShow() {
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
		if (a.floatApp!=null) {
			a.floatApp.enableKeyBoard(true);
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
		a.etSearch_ToToolbarMode(0);
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
			a.etSearch_ToToolbarMode(0);
		}
	}
	
	@Override
	public void refreshSoftMode(int height) {
		if(lpRvTools !=null && ViewUtils.isVisible(lv)) {
//			boolean hard = PDICMainAppOptions.etSchAlwaysHard();
//			if (RvTools.getParent()==lv ^ !hard) {
//				ViewUtils.addViewToParent(RvTools, hard? (ViewGroup) lv.getParent() :lv);
//				lpRvTools = (FrameLayout.LayoutParams) RvTools.getLayoutParams();
//			}
//			if(!hard) height=0;
//			if (lpRvTools.bottomMargin != height) {
//				lpRvTools.bottomMargin = height;
//				RvTools.requestLayout();
//			}
		}
	}
	
	int MainAppBackground;
	int ForegroundColor = Color.WHITE;
	
	private void refreshColors() {
		if(MainAppBackground!=a.MainAppBackground && settingsLayout!=null){
			MainAppBackground = a.MainAppBackground;
			ViewGroup etBar = settingsLayout;
			fc = ColorUtils.blendARGB(Color.WHITE,MainAppBackground, 0.45f) & 0xf0ffffff;
			etBar.getChildAt(0).setBackgroundColor(fc);
			etBar.getChildAt(1).setBackgroundColor(MainAppBackground);
			etBar.getChildAt(2).setBackgroundColor(fc);
			LayerDrawable ld = (LayerDrawable)drpBtn.getBackground().mutate();
			PorterDuffColorFilter cf = new PorterDuffColorFilter(MainAppBackground, PorterDuff.Mode.SRC_IN);
			for (int i = 0; i < ld.getNumberOfLayers()-1; i++) {
				ld.getDrawable(i).setColorFilter(cf);
			}
			if (mRecycler != null) {
				mRecycler.setBackgroundColor(MainAppBackground);
			}
		}
		int color = a.getForegroundColor();
		if (ForegroundColor != color) {
			ForegroundColor = color;
			ViewUtils.setForegroundColor(settingsLayout, color, VU.sForegroundFilter, VU.sForegroundTint);
			if(lv!=null) ViewUtils.setForegroundColor(lv, color, VU.sForegroundFilter, VU.sForegroundTint);
		}
	}
	
	private void redrawExpandBtn() {
		expandBtn.setImageResource(etHistoryExpanded?R.drawable.ic_baseline_unfold_less_24:R.drawable.ic_baseline_unfold_more_24);
		lpRv.height = etHistoryExpanded?-1:(int) ((int) (GlobalOptions.density*48) * 3.5);
	}
}

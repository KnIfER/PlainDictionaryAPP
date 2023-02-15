package com.knziha.plod.plaindict;

import static com.knziha.plod.plaindict.CMN.AssetTag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.plod.PlainUI.PlainAppPanel;
import com.knziha.plod.PlainUI.PlainBottomDialog;
import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.PlainUI.WordPopup;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.LinearSplitView;
import com.knziha.plod.widgets.TextMenuView;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DictPicker extends PlainAppPanel implements View.OnClickListener, PopupMenuHelper.PopupMenuListener, View.OnLongClickListener {
	public boolean PostEnabled=true;
	public int bForcePin;
	private AnimationSet animation;
	
	public boolean isDirty=false;
	public ViewGroup root;
	
	RecyclerView mRecyclerView;
	LinearLayoutManager lman;
	public boolean bShouldCloseAfterChoose=true;
	public HomeAdapter mAdapter;
	
	public String SearchIncantation;
	public Pattern SearchPattern;
	public int LastSearchScrollItem;
	
	LinearLayout.LayoutParams dockParms;
	ViewGroup.LayoutParams bottomDlgParms = new FrameLayout.LayoutParams(-1, -1);
	ViewGroup.LayoutParams undockParms = new FrameLayout.LayoutParams(-1, -2);
	boolean bottomDlg = true;
	private PlainBottomDialog dialogBtm;
	private FrameLayout bottomDlgLayout;
	private FrameLayout dialogLayout;
	private LinearLayout dialogContent;
	private ViewGroup pdictBtm;
	private Drawable pdictBtmBG;
	private LinearSplitView splitView;
	private ViewGroup splitter;
	private int type;
	
	public WordPopup wordPopup;
	
	Toolbar Searchbar;
	private EditText etSearchDict;
	private boolean SearchDictPatternChanged;
	private IBinder wtSearch;
	private ImageView tweakBtn;
	private CheckableImageView pinBtn;
	private View exitBtn;
	private CheckableImageView autoBtn;
	
	public ArrayList<Long> filtered;
	public HashSet<Long> underlined;
	
	public int adapter_idx;
	private Runnable showImmAby;
	
	public boolean autoScroll;
	
	/** where to store the book group name */
	public String planSlot;
	
	public MainActivityUIBase.LoadManager loadManager;
	private int pressedPos;
	
	public DictPicker(MainActivityUIBase a_, LinearSplitView splitView, ViewGroup splitter, int reason){
		super(a_, false);
		if(a_!=null){
			a = a_;
		}
		this.showType = 2;
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		this.splitView = splitView;
		this.splitter = splitter;
		this.type = reason;
		this.loadManager = a.loadManager;
		this.resizeDlg = true;
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root)
	{
		if (settingsLayout==null && a!=null) {
			planSlot = a.LastPlanName;
			root = (ViewGroup) a.getLayoutInflater().inflate(R.layout.dict_picker, root, false);
			//view.setMinimumWidth(getResources().getDisplayMetrics().widthPixels*2/3);
			//view.setLayoutParams(new LayoutParams(-2,-1));
			//getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			mRecyclerView = root.findViewById(R.id.choose_dict);
			mRecyclerView.setNestedScrollingEnabled(PDICMainAppOptions.exitDictPickerOnTop());
			lman = new LinearLayoutManager(a.getApplicationContext());
			mRecyclerView.setLayoutManager(lman);
			mRecyclerView.setAdapter(mAdapter = new HomeAdapter(a));
			//RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
			//if(itemAnimator instanceof SimpleItemAnimator)
			//	((SimpleItemAnimator)itemAnimator).setSupportsChangeAnimations(false);
			mRecyclerView.setItemAnimator(null);
			bottomDlg = wordPopup==null?PDICMainAppOptions.pickDictOnBottom():PDICMainAppOptions.pickDictOnBottomTapSch();
			
			mRecyclerView.setMinimumWidth(a.dm.widthPixels*2/3);
			mRecyclerView.setVerticalScrollBarEnabled(true);
			int LIP = lman.findLastVisibleItemPosition();
			int adapter_idx= type==-1?
					filtered==null?a.wordPopup.upstrIdx :a.wordPopup.weblistHandler.frameSelection
					:this.adapter_idx;
			if(adapter_idx>LIP) {
				int target = Math.max(0, adapter_idx-5);
				lman.scrollToPositionWithOffset(target, 0);
			}
			//view.setBackgroundResource(R.drawable.popup_shadow_l);
			//root.setOnClickListener(this);
			ViewUtils.setOnClickListenersOneDepth(root, this, 999, null);
			dialogLayout = (FrameLayout) root.getChildAt(0);
			dialogContent = (LinearLayout) dialogLayout.getChildAt(0);
			
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dialogLayout.getLayoutParams();
			dialogLayout.setTag(new int[]{lp.topMargin, lp.bottomMargin});
			View bottombar = pdictBtm = (ViewGroup) dialogContent.getChildAt(dialogContent.getChildCount()-1);
			pdictBtmBG = pdictBtm.getBackground();
			settingsLayout = root;
			//bgView = dialogLayout;
			this.root = root;
			pinBtn = bottombar.findViewById(R.id.pinBtn);
			exitBtn = bottombar.findViewById(R.id.exit);
			tweakBtn = bottombar.findViewById(R.id.tweakBtn);
			pinBtn_setChecked(pin());
			
			autoBtn = bottombar.findViewById(R.id.autoBtn);
			autoBtn.setChecked(opt.autoSchPDict());
			autoBtn.getDrawable().setAlpha(opt.autoSchPDict()?255:100);
			
			if (type==-1) {
				ViewUtils.setVisible(autoBtn, false);
			}
			
			if (type==1) {
				ViewUtils.setVisible(bottombar, false);
			}
		}
	}
	
	private void pinBtn_setChecked(boolean pin) {
		pinBtn.setChecked(pin);
		pinBtn.setContentDescription(pin?"取消钉住词典列表":"钉住词典列表");
	}
	
	@Override
	public void refresh() {
		if (MainColorStamp!=a.MainAppBackground)
		{
			if(GlobalOptions.isDark) {
				//dialogLayout.getBackground().setColorFilter(a.AppWhite, PorterDuff.Mode.SRC_IN);
				if(Build.VERSION.SDK_INT<=23)dialogLayout.setBackgroundColor(Color.BLACK);
				else dialogLayout.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				pdictBtmBG.setColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
				if (dialogContent.getBackground() != null) {
					if(Build.VERSION.SDK_INT<=23)dialogContent.setBackgroundColor(Color.BLACK);
					else dialogContent.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				}
			} else {
				if(Build.VERSION.SDK_INT<=23)dialogLayout.setBackgroundResource(R.drawable.popup_background3);
				else dialogLayout.getBackground().setColorFilter(null);
				pdictBtmBG.setColorFilter(null);
				if (dialogContent.getBackground() != null) {
					if(Build.VERSION.SDK_INT<=23)dialogContent.setBackgroundResource(R.drawable.popup_background3_split);
					else dialogContent.getBackground().setColorFilter(null);
				}
			}
			if(bottomDlgLayout!=null)
				bottomDlgLayout.setBackgroundColor(GlobalOptions.isDark?0xFF454545:a.AppWhite);
			if (Searchbar != null) {
				Searchbar.setBackgroundColor(a.MainAppBackground);
				ViewUtils.setForegroundColor(Searchbar, a.tintListFilter);
			}
			MainColorStamp = a.MainAppBackground;
			dataChanged();
		}
	}
	
	public void reform(boolean firstAttach, int bForcePin) {
		CMN.debug("reform", "firstAttach = [" + firstAttach + "], bForcePin = [" + bForcePin + "]");
		//if(!isDirty) return;
		//isDirty=false;
		scrollThis();
		
//		//123123
//
//		private View cb1;
//		cb1 = UIData.schBook;
//		UIData.pinPicBook.setChecked(opt.getPinPicDictDialog());
//		UIData.autoSchSw.setChecked(opt.getPicDictAutoSer());
		
		//
		if(splitView!=null) {
			/*  词典选择器的动画效果(显示)  */
			boolean pin = pinBtn.isChecked();//pin();
			if(bForcePin!=0) {
				if(bForcePin==-1) pin=false;
				else if(bForcePin==1) pin=true;
				bForcePin = 0;
			}
			final LinearLayout pane = this.dialogContent;
			LinearSplitView splitView = this.splitView;
			if(pin ^ pane.getParent()==splitView) {
				MainColorStamp = 0; //todo opt
				//TextViewmy rcntSchList = settingsLayout.findViewById(R.id.rcntSchList);
				//ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) rcntSchList.getLayoutParams();
				if (pin) {
					if(ViewUtils.removeIfParentBeOrNotBe(pane, splitView, false)) {
						splitView.addView(pane, 0, dockParms ==null? undockParms : dockParms);
					}
					if(dockParms !=null) {
						pane.setLayoutParams(dockParms);
					} else {
						dockParms = (LinearLayout.LayoutParams) pane.getLayoutParams();
						dockParms.height = -1;
						dockParms.width = 0;
						dockParms.weight = (float) (1 / (1-0.375) * 0.375);
						//dockLayout.width = (int) (135 * GlobalOptions.density);
						//CMN.Log("黄金比例？", (int) (135 * GlobalOptions.density), a.root.getWidth());
						dockParms.bottomMargin = dockParms.topMargin = 0;
					}
					splitView.installHandle(splitter);
					View handle = splitView.handle;
					handle.getLayoutParams().width = (int) (28 * GlobalOptions.density);
					handle.getLayoutParams().height = (int) (28 * GlobalOptions.density);
					handle.setBackgroundResource(R.drawable.ic_split_handle);
					pane.setBackgroundResource(R.drawable.popup_background3_split);
					handle.getBackground().setColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
					//rcntSchList.setPadding(0,0,0,0);
					//rcntSchList.setMaxLines(1);
					int pad = (int) (5*GlobalOptions.density);
					//layoutParams.leftMargin=pad;
					//layoutParams.rightMargin=pad;
				}
				else {
					ViewGroup layout =  bottomDlg && bottomDlgLayout != null ? bottomDlgLayout : dialogLayout;
					if(ViewUtils.removeIfParentBeOrNotBe(pane, layout, false)) {
						layout.addView(pane, 0, bottomDlg?bottomDlgParms:undockParms);
					}
					if (!bottomDlg) {
						ViewUtils.addViewToParent(layout, root);
					}
					pane.setBackground(null);
					int pad = (int) (5*GlobalOptions.density);
					//rcntSchList.setPadding(0,pad*2,0,pad);
					//rcntSchList.setMaxLines(3);
					pad = (int) (25*GlobalOptions.density);
					//layoutParams.leftMargin=pad;
					//layoutParams.rightMargin=pad;
				}
				dataChanged();
				pinBtn_setChecked(pin);
				refresh();
				ViewUtils.setVisible(tweakBtn, !pin);
			}
			ViewUtils.setVisible(exitBtn, bottomDlg && !pin);
			ViewUtils.setVisible(splitter, pin);
			if(pin) {
//				a.dialogHolder.setVisibility(View.GONE);
				dismissImmediate();
				pdictBtmBG.setAlpha(255);
			}
			else {
				if(animation==null)
					animation = (AnimationSet) AnimationUtils.loadAnimation(a, R.anim.dp_dialog_enter);
				animation.getAnimations().get(0).setDuration(firstAttach ? 200 : 200);
//				a.dialogHolder.startAnimation(animation);
//				a.dialogHolder.setVisibility(View.VISIBLE);
				if(!isVisible())
					toggle(a.root, null, -1);
				pdictBtmBG.setAlpha(bottomDlg?0:255);
				if(bottomDlg) ViewUtils.addViewToParent(dialogContent, bottomDlgLayout);
				else ViewUtils.addViewToParent(dialogContent, dialogLayout);
				if (bottomDlg) {
					pane.getLayoutParams().height = -1;
				}
			}
			if(bottomDlgLayout!=null)
				bottomDlgLayout.setBackgroundColor(GlobalOptions.isDark?0xFF454545:a.AppWhite);
		}
		
		//mAdapter.notifyDataSetChanged();
		
		//autoBtn.setAlpha(a.isCombinedSearching?0.2f:1);
	}
	
	public HomeAdapter adapter(){
		return mAdapter;
	}
	
	
	public void toggle() {
		if (pinBtn==null?pin():pinBtn.isChecked()){
			if (dialogLayout==null) {
				init(a, a.root);
			}
			boolean show=dialogContent.getParent()==splitView;
			if(show) {
				ViewUtils.removeView(dialogContent);
				ViewUtils.setVisible(splitter, false);
			} else {
				reform(false, 0);
				if (autoScroll) {
					scrollThis();
				}
			}
			pinShow(!show);
			dismiss();
		} else {
			toggle(a.root, null, -1);
			if (isVisible()) {
				if(autoScroll)scrollThis();
				refresh();
			}
		}
	}
	
	
	@Override
	protected void onShow() {
		resize();
		if (Searchbar!=null && (wordPopup!=null || a.accessMan.isEnabled()) && !pinned()) {
			ViewUtils.setVisibility(Searchbar, false);
		}
	}
	
	final boolean act() {
		return isVisible() || pinBtn.isChecked();
	}
	
	public boolean pinShow() {
		return type==0?PDICMainAppOptions.getShowPinPicBook() // 主程序
				:type==-1?PDICMainAppOptions.pinPDicWrdShow() // 点译
				:false
				;
	}
	
	void pinShow(boolean v) {
		if (type==0) PDICMainAppOptions.setShowPinPicBook(v);
		else if (type==-1) PDICMainAppOptions.pinPDicWrdShow(v);
		//else if (type==1) PDICMainAppOptions.pinPDicFlt(v);
	}
	
	boolean pin() {
		if (splitView!=null) {
			return type==0?PDICMainAppOptions.pinPDic() // 主程序
				:type==-1?PDICMainAppOptions.pinPDicWrd() // 点译
				:PDICMainAppOptions.pinPDicFlt() // 浮动搜索
				;
		}
		return false;
	}
	
	void pin(boolean v) {
		if (type==0) PDICMainAppOptions.pinPDic(v);
		else if (type==-1) PDICMainAppOptions.pinPDicWrd(v);
		else if (type==1) PDICMainAppOptions.pinPDicFlt(v);
	}
	
	public boolean pinned() {
		return pinBtn!=null && pinBtn.isChecked();
	}
	
	public final boolean autoSchPDict() {
		return autoBtn!=null?autoBtn.isChecked():opt.autoSchPDict();
	}
	
	public final void dataChanged() {
		if(mAdapter!=null) {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public void resize() {
		if (!pinBtn.isChecked() && false) {
			Resources res = a.mResource;
			int littleIdeal  = Math.min(a.dm.widthPixels, (int)res.getDimension(R.dimen.idealdpdp)*55/45);
			int factor=1;
			View dialogLayout = this.dialogLayout;
			ViewGroup.MarginLayoutParams mlarp = (ViewGroup.MarginLayoutParams) dialogLayout.getLayoutParams();
			int[] margins = (int[]) dialogLayout.getTag();
			mlarp.width = littleIdeal - (int) (1 * res.getDimension(R.dimen._28_));
			mlarp.topMargin= (int) (margins[0]/factor*1.5);
			mlarp.bottomMargin= (int) (margins[1]/factor*1.5);
			if (GlobalOptions.isSmall) {
				mlarp.bottomMargin= mlarp.topMargin;
			}
		}
		if (bottomDlg) {
			refreshExpand();
		}
	}
	
	public void filterByRec(resultRecorderCombined rec, int pos) {
		if (rec!=null) {
			ArrayList<Long> ids = pos>=0&&pos<rec.size()?rec.getBooksAt(null, pos):new ArrayList<>();
			filtered = new ArrayList<>(ids);
			dataChanged();
		} else if (filtered != null) {
			filtered = null;
			dataChanged();
		}
	}
	
	public int adapterIdx() {
		if((adapter_idx<0||adapter_idx>=loadManager.lazyMan.chairCount))
			adapter_idx=0;
		return adapter_idx;
	}
	
	public class HomeAdapter extends RecyclerView.Adapter<MyViewHolder>
	{
		final Drawable underln;
		final ColorFilter selecf;
		
		HomeAdapter(Context context) {
			this.underln = context.getResources().getDrawable(R.drawable.text_underline1);
			selecf = new PorterDuffColorFilter(0xff4F7FDF, PorterDuff.Mode.SRC_IN);
			underln.setColorFilter(new PorterDuffColorFilter(0xff3F5F9F, PorterDuff.Mode.SRC_IN));
		}
		
		@Override
		public int getItemCount() {
			return filtered==null?loadManager.lazyMan.chairCount:filtered.size();
		}
		@NonNull
		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			//CMN.Log("onCreateViewHolder...");
			MyViewHolder ret = new MyViewHolder(LayoutInflater.from(parent.getContext())
							.inflate(R.layout.diag1_fc_list_item, parent, false), DictPicker.this);
			ret.tv.bNeedPostLayout = true;
			ret.tv.earHintAhead = "词典";
			ret.tv.earHintAfter = "点击切换词典";
			return ret;
		}
		
		@Override
		public void onBindViewHolder(@NonNull MyViewHolder holder, int position) { }
		
		@Override
		public void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull List<Object> payloads) {
			long bid=-1;
			if (filtered!=null) {
				bid = filtered.get(position);
			}
			FlowTextView tv = holder.tv;
			if(payloads.size()==0) {
				//CMN.Log("onBindViewHolder::刷新全部!!!");
				//todo 应该将adapter_idx 放到这个类中
				boolean rowSelected;
				if (bid==-1) {
					rowSelected = position == (type==-1?a.wordPopup.upstrIdx:DictPicker.this.adapter_idx);
				} else {
					rowSelected = a.wordPopup.weblistHandler!=null
							&& a.wordPopup.weblistHandler.getFrameAt(a.wordPopup.weblistHandler.frameSelection).getId()==bid;
				}
				holder.itemView.setBackgroundColor(rowSelected?0xff4F7FDF:Color.TRANSPARENT);
				tv.setTextColor(GlobalOptions.isDark||rowSelected?Color.WHITE:Color.BLACK);
				tv.PostEnabled = PostEnabled;
				tv.setStarLevel(loadManager.md_get_StarLevel(position, bid));
				tv.setCompoundDrawables(a.getActiveStarDrawable(), null, null, null);
				
				String text = /*position+":"+*/loadManager.md_getName(position, bid);
				tv.SetSearchPattern(SearchPattern, text);
				
				Drawable cover = loadManager.md_getCover(position, bid);
				ViewUtils.setVisibility((View) holder.cover.getParent(), cover!=null);
				ViewGroup.LayoutParams lp = holder.cover.getLayoutParams();
				lp.width = (int) (lp.height/(cover==null?
						pinBtn.isChecked()?999:1.5f
						:1));
				holder.cover.setImageDrawable(cover);
				tv.setText(text);
			}
			tv.earHintAheadMode = 2;
			tv.earHintAfterMode = 1;
			//else CMN.Log("onBindViewHolder::刷新部分!!!", payloads);
			if (bid==-1) {
				bid = loadManager.md_getNoCreate(position, bid).getId();
			}
			boolean under = type==-1 && a.wordPopup.CCD_ID==position
					||type!=-1 && underlined!=null && underlined.contains(bid);
			if(under ^ tv.getBackground()!=null) // 下划线
				tv.setBackground(under?underln:null);
		}
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		switch (v.getId()) {
			case R.string.centre_dlg:{
				if (wordPopup == null) PDICMainAppOptions.pickDictOnBottom(bottomDlg = !bottomDlg);
				else PDICMainAppOptions.pickDictOnBottomTapSch(bottomDlg = !bottomDlg);
				dismissImmediate();
				reform(false, 0);
				if (!isVisible())
					toggle();
			} break;
			case R.string.exit_on_top:{
				PDICMainAppOptions.exitDictPickerOnTop(!PDICMainAppOptions.exitDictPickerOnTop());
				mRecyclerView.setNestedScrollingEnabled(PDICMainAppOptions.exitDictPickerOnTop() && !a.keyboardShown);
			} break;
			case R.string.locate_dman:{
				try {
					a.locateDictInManager(loadManager, pressedPos, isLongClick, this);
				} catch (Exception e) {
					CMN.debug(e);
				}
			} break;
		}
		popupMenuHelper.dismiss();
		return false;
	}
	
	// longclick
	@Override
	public boolean onLongClick(View v) {
		Object tag = v.getTag();
		if(tag instanceof MyViewHolder){
			int position = ((MyViewHolder) tag).getLayoutPosition();
			pressedPos = position;
			if(!act()) return true;
			if(type==-1){ //点译搜索
				int tmpPos;
				if (filtered==null) { //点译上游
				
				} else { //跳转多页面
				
				}
			}
			else {//当前词典
			
			}
			PopupMenuHelper popupMenu = a.getPopupMenu();
			int[] texts = new int[]{
					R.string.locate_dman
					//, R.string.disable
			};
			popupMenu.initLayout(texts, this);
			int[] vLocationOnScreen = new int[2];
			v.getLocationOnScreen(vLocationOnScreen);
			int x=0, y=0;
			popupMenu.showAt(v, x+vLocationOnScreen[0], y+vLocationOnScreen[1], Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		}
		return true;
	}
	
	// click
	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if(tag instanceof MyViewHolder){
			int position = ((MyViewHolder) tag).getLayoutPosition();
			if(!act()) return;
			if(type==-1){ //点译搜索
				int tmpPos;
				if (filtered==null) { //点译上游
					tmpPos = a.wordPopup.upstrIdx;
					a.wordPopup.CCD=loadManager.md_get(position);
					a.wordPopup.CCD_ID=a.wordPopup.upstrIdx = position;
					//a.popupWord(ViewUtils.getTextInView(a.wordPopup.entryTitle), null, -1, null); todo
					a.popupWord(a.wordPopup.popupKey, null, -1, null, false);
				} else { //跳转多页面
					tmpPos = a.wordPopup.weblistHandler.frameSelection;
					position = ((MyViewHolder) tag).getLayoutPosition();
					a.wordPopup.weblistHandler.JumpToFrame(position);
				}
				mAdapter.notifyItemChanged(tmpPos);
				mAdapter.notifyItemChanged(position);
				dismissImmediate();
			}
			else {//当前词典
				int tmpPos = adapter_idx;
				adapter_idx = position;
				a.switch_Dict(position, true, true, null);
				ViewUtils.setVisibleV3(a.lv2, false);
				mAdapter.notifyItemChanged(tmpPos);
				if(tmpPos!=position){
					mAdapter.notifyItemChanged(position);
				}
				if (bShouldCloseAfterChoose) {
					v.post(this::dismissImmediate);
				}
			}
		}
		switch (v.getId()) {
			case R.id.dialogHolder:
				if(wtSearch!=null && ViewUtils.isVisibleV2(Searchbar)) {
					a.imm.hideSoftInputFromWindow(wtSearch, 0);
				}
				// 点击界面背景
				dismiss();
				break;
			case R.id.dictName:
				scrollThis();
				break;
			case R.id.tweakBtn:
				PopupMenuHelper popupMenu = a.getPopupMenu();
				int[] texts = new int[]{
					R.string.exit_on_top
					, R.string.centre_dlg
				};
				popupMenu.initLayout(texts, this);
				((TextMenuView) popupMenu.lv.findViewById(R.string.exit_on_top)).setActivated(PDICMainAppOptions.exitDictPickerOnTop());
				int[] vLocationOnScreen = new int[2];
				v.getLocationOnScreen(vLocationOnScreen);
				int x=0, y=0;
				popupMenu.show(v, x+vLocationOnScreen[0], y+vLocationOnScreen[1]);
				break;
			case R.id.exit:
				dismissImmediate();
				if(dialog!=null)
					dialog.dismiss();
				break;
			case R.id.pinBtn:
				CheckableImageView cb = (CheckableImageView) v;
				cb.toggle();
				pin(cb.isChecked());
				pinShow(cb.isChecked());
				//if(pickDictDialog!=null) pickDictDialog.isDirty=true;
				reform(false, 0);
				if(!cb.isChecked() && !isVisible())
					toggle(a.root, null, -1);
				break;
			case R.id.autoBtn:{
				cb = (CheckableImageView)v;
				cb.toggle();
				opt.autoSchPDict(cb.isChecked());
				autoBtn.getDrawable().setAlpha(cb.isChecked()?255:100);
				if(cb.isChecked())
					a.showTopSnack("自动搜索√");
				else
					a.fadeSnack();
			} break;
			case R.id.bundle:{
				a.showChooseSetDialog(PDICMainAppOptions.wordPopupAllowDifferentSet()?wordPopup:null);
			} break;
			case R.id.schBook:
				if (Searchbar == null) {
					Toolbar searchbar = (Toolbar) ((ViewStub) root.getChildAt(root.getChildCount() - 1)).inflate();
					searchbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
					searchbar.mNavButtonView.setId(R.id.schBook);
					searchbar.mNavButtonView.setOnClickListener(this);
					ViewUtils.ResizeNavigationIcon(searchbar);
					//searchbar.setContentInsetsAbsolute(0, 0);
					searchbar.setBackgroundColor(a.MainAppBackground);
					ViewUtils.setForegroundColor(searchbar, a.tintListFilter);
					ViewGroup VG = (ViewGroup) searchbar.getChildAt(0);
					SetImageClickListener(VG, true);
					etSearchDict = (EditText) VG.findViewById(R.id.etSearch);
					etSearchDict.requestFocus();
					etSearchDict.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						}
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
						}
						@Override
						public void afterTextChanged(Editable s) {
							SearchDictPatternChanged = true;
						}
					});
					etSearchDict.setOnEditorActionListener((v12, actionId, event) -> searchbar.findViewById(R.id.forward).performClick());
					this.Searchbar = searchbar;
					wtSearch = etSearchDict.getWindowToken();
					showImmAby = ()->{
						etSearchDict.requestFocus();
						a.imm.showSoftInput(etSearchDict, InputMethodManager.SHOW_IMPLICIT);
					};
					etSearchDict.postDelayed(showImmAby, 200);
				}
				else {
					if (v == Searchbar.getNavigationBtn()) {
						ViewUtils.setVisible(Searchbar, false);
						a.imm.hideSoftInputFromWindow(wtSearch, 0);
						if (pinBtn.isChecked()) dismiss();
					} else {
						ViewUtils.setVisible(Searchbar, true);
						etSearchDict.postDelayed(showImmAby, 200);
					}
				}
				if (pinned()) {
					if(etSearchDict.getTag()==null) {
						etSearchDict.setTag(etSearchDict);
						root.setOnTouchListener(new View.OnTouchListener() {
							int diffY = 0, orgX, orgY;
							boolean checkMove = false;
							View scrollView;
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								if (pinned()) {
									if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
										int[] vLocation = new int[2];
										mRecyclerView.getLocationOnScreen(vLocation);
										diffY = vLocation[1];
										root.getLocationOnScreen(vLocation);
										diffY = vLocation[1] - diffY;
										//debug("offser", diffY);
										int y = (int) (event.getY() + diffY);
										scrollView = null;
										if (y >= 0 && y < mRecyclerView.getHeight()) {
											if (event.getX() > mRecyclerView.getWidth()) {
												//scrollView = a.thisActType == MainActivityUIBase.ActType.PlainDict ? ((PDICMainActivity)a).UIData.viewpagerPH : a.mlv;
												scrollView = mRecyclerView;
												checkMove = true;
											} else {
												scrollView = mRecyclerView;
												checkMove = false;
											}
											orgX = (int) event.getRawX();
											orgY = (int) event.getRawY();
										}
									}
									if (scrollView != null) {
										event.offsetLocation(0, diffY);
										float x = event.getX(); boolean b1=event.getX() > mRecyclerView.getWidth();
										if (b1) event.setLocation(-100, event.getY());
										scrollView.dispatchTouchEvent(event);
										if (b1) event.setLocation(x, event.getY());
										if (checkMove) {
											if (ViewUtils.distance(event.getRawX() - orgX, event.getRawY() - orgY) > 45 * GlobalOptions.density) {
												checkMove = false;
												return true;
											}
											return false;
										}
										return true;
									}
								}
								return false;
							}
						});
					}
					if (wordPopup != null && wordPopup.isVisible() && wordPopup.getLastShowType()==2) {
						try {
							wordPopup.dialog.getWindow().setSoftInputMode(wordPopup.isMaximized()?MainActivityUIBase.softModeResize:MainActivityUIBase.softModeHold);
							//CMN.debug("wordPopup.isMaximized()::", wordPopup.isMaximized());
						} catch (Exception e) {
							CMN.debug(e);
						}
					}
					if(bottomDlg){
						ViewUtils.removeView(dialogLayout);
					}
					ViewUtils.addViewToParent(Searchbar, root);
				}
				else if(bottomDlg){
					ViewUtils.addViewToParent(Searchbar, bottomDlgLayout);
				} else {
					ViewUtils.addViewToParent(Searchbar, root);
				}
				if (!isVisible())
					toggle(a.root, null, -1);
				break;
			case R.id.recess:
			case R.id.forward:{
				if(Searchbar!=null) {
					RecyclerView mRecyclerView = this.mRecyclerView;
					mRecyclerView.stopScroll();
					if (mRecyclerView.getTag()==null) {
						mRecyclerView.setOnTouchListener((v1, event) -> {
							LastSearchScrollItem = -1;
							return false;
						});
						mRecyclerView.setTag(false);
					}
					if(SearchDictPatternChanged) {
						SetSearchIncantation(etSearchDict.getText().toString());
						SearchDictPatternChanged=false;
					}
					//pickDictDialog.SetSearchIncantation("中");
					int[] chairs = loadManager.lazyMan.CosyChair;
					int fullSize = loadManager.md_size;
					int pad = (int) (10*a.dm.density);
					int delta = v.getId()==R.id.recess?1:-1;
					int fvp;
					if(LastSearchScrollItem>=0) {
						fvp = LastSearchScrollItem+delta;
					} else {
						fvp = lman.findFirstVisibleItemPosition()+delta;
						if(mRecyclerView.getChildAt(1).getTop()<=pad){
							fvp++;
						}
					}

					int sep=fullSize-fvp;
					int st=0;
					if(delta<0) {
						sep -= 1;
						st = fullSize-1;
					}
					int msg = R.string.fn;
					try {
						for(int i=st,j;delta<0?i>=0:i<fullSize;i+=delta) {
							if(i>=sep) {
								j=i-sep;
							} else {
								j=i+fvp;
							}
							//CMN.Log(i, j, CosyChair.get(j).pathname);
							//						if(delta>0&&j==fullSize-1||delta<0&&j==0) {
							//							break;
							//						}
							if (j>=0 && j<fullSize) {
								String name = loadManager.getPlaceHolderAt(j).pathname;
								if(name!=null && name.startsWith(AssetTag)) {
									name = CMN.getAssetName(name);
								}
								if(SearchPattern==null || SearchPattern.matcher(name).find()) {
									msg = 0;
									lman.scrollToPositionWithOffset(j, pad);
									LastSearchScrollItem=j;
									break;
								}
							}
						}
					} catch (Exception e) {  }
					if(msg!=0) {
						a.show(msg);
					}
					a.imm.hideSoftInputFromWindow(wtSearch, 0);
					dataChanged();
				}
			} break;
			case R.id.ivDeleteText:{
				if(Searchbar!=null) {
					SearchIncantation = null;
					etSearchDict.setText("");
					//imm.hideSoftInputFromWindow(etSearchDict_getWindowToken, 0);
					dataChanged();
				}
			} break;
		}
	}
	
	public void scrollThis() {
		if(mAdapter!=null) {
			int vdx=this.adapter_idx;
			if (type==-1) {
				vdx = a.wordPopup.CCD_ID;
				if(vdx<0||vdx>=loadManager.lazyMan.chairCount)
					vdx = a.wordPopup.upstrIdx;
			}
			if(lman!=null) {
				//if(vdx>lman.findLastVisibleItemPosition() || vdx<lman.findFirstVisibleItemPosition())
				{
					int target = Math.max(0, vdx-(Math.max(0, Math.min(pinBtn.isChecked()?3:5, mRecyclerView.getChildCount()/2))));
					lman.scrollToPositionWithOffset(type==-1?a.wordPopup.CCD_ID:target, 0);
					// CMN.Log("scrolled");
				}
			}
		}
	}
	
	ArrayList<String> recentDictFilters = new ArrayList<>();
	private void refreshRecentDictsSpan() {
		SpannableStringBuilder ssbRcntSch = new SpannableStringBuilder();
//		ssbRcntSch.append("[最近词典列表]");
//		ssbRcntSch.setSpan(new ClickableSpan() {
//			@Override
//			public void onClick(@NonNull View widget) {
//				showT("...");
//			}
//		}, 0, ssbRcntSch.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if(recentDictFilters.size()==0) {
			recentDictFilters.add("[隐藏]");
			recentDictFilters.add("[切换分组]");
			recentDictFilters.add("[原列表]");
		}
		for (int i = 0; i < recentDictFilters.size(); i++) {
			String sch = recentDictFilters.get(i);
			if(ssbRcntSch.length()>0)
				ssbRcntSch.append("   ");
			int st = ssbRcntSch.length(),ed;
			ssbRcntSch.append(sch);
			ed = ssbRcntSch.length();
			
			ssbRcntSch.setSpan(new RecentDictClickableSpan(i, 0), st, ed, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		ssbRcntSch.append("   ");
//		UIData.rcntSchList.setText(ssbRcntSch);//111
	}
	
	
	class RecentDictClickableSpan extends ClickableSpan implements Runnable{
		final int index;
		final int type;
		
		RecentDictClickableSpan(int index, int type) {
			this.index = index;
			this.type = type;
		}
		
		@Override
		public void onClick(@NonNull View widget) {
			if(type==0) {
				//showT(recentDictFilters.get(index));
					String dictFilter = recentDictFilters.get(index);
				//showTopSnack(dialogHolder, dictFilter);
				if(Searchbar!=null && Searchbar.getVisibility()==View.VISIBLE) {
					etSearchDict.setText(dictFilter);
				} else {
//					UIData.rcntSch.setText(dictFilter);//111
				
				}
				
			} else {
				root.postDelayed(this, 200);
			}
		}
		
		@Override
		public void run() {
			try {
				String removed = recentDictFilters.remove(index);
				recentDictFilters.add(type==1?recentDictFilters.size():0, removed);
				refreshRecentDictsSpan();
			} catch (Exception ignored) { }
		}
	}
	
	
	private void SetImageClickListener(ViewGroup VG, boolean Tag) {
		int cc = VG.getChildCount();
		View view;
		for (int i = 0; i < cc; i++) {
			view = VG.getChildAt(i);
			if(view instanceof ImageView) {
				if(Tag) {
					view.setTag(false);
				}
				view.setOnClickListener(this);
			}
		}
	}
	
	public void SetSearchIncantation(String pattern) {
		if(!pattern.equals(SearchIncantation)){
			SearchIncantation = pattern;
			if(pattern.length()==0) {
				SearchPattern=null;
			} else {
				try {
					SearchPattern = Pattern.compile(SearchIncantation,Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					SearchPattern = Pattern.compile(SearchIncantation, Pattern.CASE_INSENSITIVE|Pattern.LITERAL);
				}
			}
			LastSearchScrollItem=-1;
		}
	}
	
	static class MyViewHolder extends ViewHolder
	{
		FlowTextView tv;
		ImageView cover;
		public MyViewHolder(View view, DictPicker onclick)
		{
			super(view);
			tv = view.findViewById(R.id.id_num);
			cover = view.findViewById(R.id.cover);
			itemView.setTag(this);
			itemView.setOnClickListener(onclick);
			itemView.setOnLongClickListener(onclick);
			View coveronclick = view.findViewById(R.id.coverp);
			coveronclick.setTag(this);
			coveronclick.setOnClickListener(onclick);
		}
	}
	
	public void setUnderLined(HashSet<Long> booksSet) {
		underlined = booksSet;
		if(pinned()) {
			mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount(), "payload");
		}
	}
	
	@Override
	public boolean onBackPressed() {
		if (ViewUtils.isVisibleV2(Searchbar)) {
			ViewUtils.setVisible(Searchbar, false);
			if (pinned()) {
				dismissImmediate();
			}
			return true;
		}
		return super.onBackPressed();
	}
	
	private void refreshExpand() {
		final View v = bottomDlgLayout;
		if (v==null) return;
		DisplayMetrics dm2 = a.dm;
		int h = -1;
		if (!a.keyboardShown)
		{
			if (false)
				h = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * 0.85f);
			else
				h = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * dialogBtm.getBehavior().getHalfExpandedRatio() + a.getResources().getDimension(R.dimen._45_) * 1.75);
		}
		if (h > dm2.heightPixels) {
			h = -1;
		}
		if (v.getLayoutParams().height != h) {
			v.getLayoutParams().height = h;
			v.requestLayout();
		}
		mRecyclerView.setNestedScrollingEnabled(PDICMainAppOptions.exitDictPickerOnTop() && !a.keyboardShown);
	}
	
	@Override
	protected void showDialog() {
		//super.showDialog();
		if (!pin() && bottomDlg) {
			tweakDlgScreen = false;
			BottomSheetDialog bPane = dialogBtm;
			if (bPane == null) {
				CMN.debug("重建底部弹出");
				bottomDlgLayout = new FrameLayout(a);
				bPane = dialogBtm = new PlainBottomDialog(a);
				dialogBtm.getWindow().setDimAmount(0);
				dialogBtm.mBackPrevention = this;
				pdictBtmBG.setAlpha(0);
				ViewUtils.addViewToParent(dialogContent, bottomDlgLayout);
				dialogContent.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
				bPane.setContentView(bottomDlgLayout);
				bPane.getWindow().setDimAmount(0.2f);
				//CMN.recurseLogCascade(lv);
			}
			bPane.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);// 展开
			dialog = bPane;
			refreshExpand();
		} else {
			if(dialog==dialogBtm)
				dialog = null;
			tweakDlgScreen = true;
		}
		super.showDialog();
	}
}
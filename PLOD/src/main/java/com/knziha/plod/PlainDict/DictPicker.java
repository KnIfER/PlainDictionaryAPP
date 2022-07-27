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
import android.os.IBinder;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.knziha.plod.PlainUI.PlainAppPanel;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.LinearSplitView;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DictPicker extends PlainAppPanel implements View.OnClickListener
{
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
	
	LinearLayout.LayoutParams dockLayout;
	ViewGroup.LayoutParams undockLayout;
	private LinearLayout dialogLayout;
	private ViewGroup pdictBtm;
	private LinearSplitView splitView;
	private ViewGroup splitter;
	private int type;
	
	Toolbar Searchbar;
	private EditText etSearchDict;
	private boolean SearchDictPatternChanged;
	private IBinder wtSearch;
	private CheckableImageView pinBtn;
	private CheckableImageView autoBtn;
	
	public ArrayList<Long> filtered;
	public HashSet<Long> underlined;
	
	public int adapter_idx;
	private Runnable showImmAby;
	
	public boolean autoScroll;
	
	public MainActivityUIBase.LoadManager loadManager;
	
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
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root)
	{
		if (settingsLayout==null && a!=null) {
			root = (ViewGroup) a.getLayoutInflater().inflate(R.layout.dict_picker, root, false);
			//view.setMinimumWidth(getResources().getDisplayMetrics().widthPixels*2/3);
			//view.setLayoutParams(new LayoutParams(-2,-1));
			//getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			mRecyclerView = root.findViewById(R.id.choose_dict);
			lman = new LinearLayoutManager(a.getApplicationContext());
			mRecyclerView.setLayoutManager(lman);
			mRecyclerView.setAdapter(mAdapter = new HomeAdapter(a));
			//RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
			//if(itemAnimator instanceof SimpleItemAnimator)
			//	((SimpleItemAnimator)itemAnimator).setSupportsChangeAnimations(false);
			mRecyclerView.setItemAnimator(null);
			
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
			dialogLayout = (LinearLayout) root.getChildAt(0);
			pdictBtm = (ViewGroup) dialogLayout.getChildAt(dialogLayout.getChildCount()-1);
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dialogLayout.getLayoutParams();
			dialogLayout.setTag(new int[]{lp.topMargin, lp.bottomMargin});
			View bottombar = dialogLayout.getChildAt(2);
			settingsLayout = root;
			//bgView = dialogLayout;
			this.root = root;
			pinBtn = bottombar.findViewById(R.id.pinBtn);
			pinBtn.setChecked(pin());
			
			autoBtn = bottombar.findViewById(R.id.autoBtn);
			autoBtn.setChecked(opt.autoSchPDict());
			autoBtn.getDrawable().setAlpha(opt.autoSchPDict()?255:100);
			
			if (type==1) {
				ViewUtils.setVisible(bottombar, false);
			}
		}
	}
	
	@Override
	public void refresh() {
		//if (MainColorStamp!=a.MainAppBackground)
		{
			if(GlobalOptions.isDark) {
				dialogLayout.getBackground().setColorFilter(a.AppWhite, PorterDuff.Mode.SRC_IN);
				pdictBtm.getBackground().setColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
			} else {
				dialogLayout.getBackground().setColorFilter(null);
				pdictBtm.getBackground().setColorFilter(null);
			}
			MainColorStamp = a.MainAppBackground;
		}
	}
	
	public void refresh(boolean firstAttach, int bForcePin) {
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
			LinearLayout dialogLayout = this.dialogLayout;
			LinearSplitView splitView = this.splitView;
			if(pin ^ dialogLayout.getParent()==splitView) {
				//TextViewmy rcntSchList = settingsLayout.findViewById(R.id.rcntSchList);
				//ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) rcntSchList.getLayoutParams();
				if (pin) {
					if(undockLayout==null) undockLayout = dialogLayout.getLayoutParams();
					if(ViewUtils.removeIfParentBeOrNotBe(dialogLayout, splitView, false)) {
						splitView.addView(dialogLayout, 0, dockLayout==null?undockLayout:dockLayout);
					}
					if(dockLayout!=null) {
						dialogLayout.setLayoutParams(dockLayout);
					} else {
						dockLayout = (LinearLayout.LayoutParams) dialogLayout.getLayoutParams();
						dockLayout.height = -1;
						dockLayout.width = 0;
						dockLayout.weight = (float) (1 / (1-0.375) * 0.375);
						//dockLayout.width = (int) (135 * GlobalOptions.density);
						//CMN.Log("黄金比例？", (int) (135 * GlobalOptions.density), a.root.getWidth());
						dockLayout.bottomMargin = dockLayout.topMargin = 0;
					}
					splitView.installHandle(splitter);
					View handle = splitView.handle;
					handle.getLayoutParams().width = (int) (28 * GlobalOptions.density);
					handle.getLayoutParams().height = (int) (28 * GlobalOptions.density);
					handle.setBackgroundResource(R.drawable.ic_split_handle);
					handle.getBackground().setColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
					dialogLayout.setBackgroundResource(R.drawable.popup_background3_split);
					//rcntSchList.setPadding(0,0,0,0);
					//rcntSchList.setMaxLines(1);
					int pad = (int) (5*GlobalOptions.density);
					//layoutParams.leftMargin=pad;
					//layoutParams.rightMargin=pad;
				}
				else {
					if(ViewUtils.removeIfParentBeOrNotBe(dialogLayout, settingsLayout, false)) {
						settingsLayout.addView(dialogLayout, 0, undockLayout);
					}
					dialogLayout.setBackgroundResource(R.drawable.popup_background3);
					int pad = (int) (5*GlobalOptions.density);
					//rcntSchList.setPadding(0,pad*2,0,pad);
					//rcntSchList.setMaxLines(3);
					pad = (int) (25*GlobalOptions.density);
					//layoutParams.leftMargin=pad;
					//layoutParams.rightMargin=pad;
				}
				dataChanged();
				pinBtn.setChecked(pin);
				refresh();
			}
			ViewUtils.setVisible(splitter, pin);
			if(pin) {
//				a.dialogHolder.setVisibility(View.GONE);
				dismiss();
			} else {
				if(animation==null)
					animation = (AnimationSet) AnimationUtils.loadAnimation(a, R.anim.dp_dialog_enter);
				animation.getAnimations().get(0).setDuration(firstAttach ? 200 : 200);
//				a.dialogHolder.startAnimation(animation);
//				a.dialogHolder.setVisibility(View.VISIBLE);
				if(!isVisible())
					toggle(a.root, null, -1);
			}
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
			boolean show=dialogLayout.getParent()==splitView;
			if(show) {
				ViewUtils.removeView(dialogLayout);
				ViewUtils.setVisible(splitter, false);
			} else {
				refresh(false, 0);
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
		Resize();
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
	
	private void Resize() {
		if (!pinBtn.isChecked()) {
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
	}
	
	public void filterByRec(resultRecorderCombined rec, int pos) {
		if (rec!=null) {
			ArrayList<Long> ids = pos>=0&&pos<rec.size()?rec.getBooksAt(null, pos):new ArrayList<>();
			filtered = new ArrayList<>(ids);
		} else {
			filtered = null;
		}
		dataChanged();
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
					a.popupWord(a.wordPopup.popupKey, null, -1, null);
				} else { //跳转多页面
					tmpPos = a.wordPopup.weblistHandler.frameSelection;
					position = ((MyViewHolder) tag).getLayoutPosition();
					a.wordPopup.weblistHandler.JumpToFrame(position);
				}
				mAdapter.notifyItemChanged(tmpPos);
				mAdapter.notifyItemChanged(position);
				dismiss();
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
					v.post(this::dismiss);
				}
			}
		}
		switch (v.getId()) {
			case R.id.dialogHolder:
				if(wtSearch!=null && ViewUtils.isVisibleV2(Searchbar)) {
					a.imm.hideSoftInputFromWindow(wtSearch, 0);
				}
				dismiss();
				break;
			case R.id.dictName:
				scrollThis();
				break;
			case R.id.pinBtn:
				CheckableImageView cb = (CheckableImageView) v;
				cb.toggle();
				pin(cb.isChecked());
				pinShow(cb.isChecked());
				//if(pickDictDialog!=null) pickDictDialog.isDirty=true;
				refresh(false, 0);
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
			case R.id.schBook:
				if (Searchbar == null) {
					Toolbar searchbar = (Toolbar) ((ViewStub) root.getChildAt(root.getChildCount() - 1)).inflate();
					searchbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
					searchbar.mNavButtonView.setId(R.id.schBook);
					searchbar.mNavButtonView.setOnClickListener(this);
					ViewUtils.ResizeNavigationIcon(searchbar);
					//searchbar.setContentInsetsAbsolute(0, 0);
					searchbar.setBackgroundColor(a.MainBackground);
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
				} else {
					if (v == Searchbar.getNavigationBtn()) {
						ViewUtils.setVisible(Searchbar, false);
						a.imm.hideSoftInputFromWindow(wtSearch, 0);
						if (pinBtn.isChecked()) dismiss();
					} else {
						ViewUtils.setVisible(Searchbar, true);
						etSearchDict.postDelayed(showImmAby, 200);
					}
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
					CMN.Log("scrolled");
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
		public MyViewHolder(View view, View.OnClickListener onclick)
		{
			super(view);
			tv = view.findViewById(R.id.id_num);
			cover = view.findViewById(R.id.cover);
			itemView.setTag(this);
			itemView.setOnClickListener(onclick);
			View coveronclick = view.findViewById(R.id.coverp);
			coveronclick.setTag(this);
			coveronclick.setOnClickListener(onclick);
		}
	}
}
package com.knziha.plod.plaindict;

import static androidx.appcompat.app.GlobalOptions.realWidth;

import static com.knziha.plod.plaindict.CMN.AssetTag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.knziha.plod.PlainUI.PlainAppPanel;
import com.knziha.plod.widgets.CheckableImageView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.Framer;
import com.knziha.plod.widgets.LinearSplitView;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
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
	private HomeAdapter mAdapter;
	
	public String SearchIncantation;
	public Pattern SearchPattern;
	public int LastSearchScrollItem;
	
	LinearLayout.LayoutParams dockLayout;
	ViewGroup.LayoutParams undockLayout;
	private LinearLayout dialogLayout;
	private LinearSplitView splitView;
	private ViewGroup splitter;
	private int reason;
	
	Toolbar Searchbar;
	private EditText etSearchDict;
	private boolean SearchDictPatternChanged;
	private IBinder etSearchDict_getWindowToken;
	
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
		this.reason = reason;
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
			mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
			//RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
			//if(itemAnimator instanceof SimpleItemAnimator)
			//	((SimpleItemAnimator)itemAnimator).setSupportsChangeAnimations(false);
			mRecyclerView.setItemAnimator(null);
			
			mRecyclerView.setMinimumWidth(a.dm.widthPixels*2/3);
			mRecyclerView.setVerticalScrollBarEnabled(true);
			int LIP = lman.findLastVisibleItemPosition();
			int adapter_idx=reason==2?a.wordPopup.currentClick_adapter_idx:a.adapter_idx;
			if(adapter_idx>LIP) {
				int target = Math.max(0, adapter_idx-5);
				lman.scrollToPositionWithOffset(target, 0);
			}
			//view.setBackgroundResource(R.drawable.popup_shadow_l);
			//root.setOnClickListener(this);
			ViewUtils.setOnClickListenersOneDepth(root, this, 999, null);
			dialogLayout = (LinearLayout) root.getChildAt(0);
			View bottombar = dialogLayout.getChildAt(2);
			settingsLayout = root;
			this.root = root;
			
			if (reason!=0) {
				ViewUtils.setVisible(bottombar, false);
			}
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
			boolean pinPicDictDialog = pin();
			if(bForcePin!=0) {
				if(bForcePin==-1) pinPicDictDialog=false;
				else if(bForcePin==1) pinPicDictDialog=true;
				bForcePin = 0;
			}
			LinearLayout dialogLayout = this.dialogLayout;
			LinearSplitView splitView = this.splitView;
			if(pinPicDictDialog ^ dialogLayout.getParent()==splitView) {
				//TextViewmy rcntSchList = settingsLayout.findViewById(R.id.rcntSchList);
				//ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) rcntSchList.getLayoutParams();
				if (pinPicDictDialog) {
					if(undockLayout==null) undockLayout = dialogLayout.getLayoutParams();
					if(ViewUtils.removeIfParentBeOrNotBe(dialogLayout, splitView, false)) {
						splitView.addView(dialogLayout, 0, dockLayout==null?undockLayout:dockLayout);
					}
					if(dockLayout!=null) {
						dialogLayout.setLayoutParams(dockLayout);
					} else {
						dockLayout = (LinearLayout.LayoutParams) dialogLayout.getLayoutParams();
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
				} else {
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
				notifyDataSetChanged();
			}
			ViewUtils.setVisible(splitter, pinPicDictDialog);
			if(pinPicDictDialog) {
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
	}


//	@Override
//	public void onAttach(@NonNull Context context){
//		//CMN.Log("dict picker onAttach");
//		super.onAttach(context);
//		refresh(true, bForcePin);
//	}
	
	public HomeAdapter adapter(){
		return mAdapter;
	}
	
	
	public void toggle() {
		if (pin()){
			if (dialogLayout==null) {
				init(a, a.root);
			}
			if(dialogLayout.getParent()==splitView) {
				ViewUtils.removeView(dialogLayout);
				ViewUtils.setVisible(splitter, false);
			} else {
				refresh(false, 0);
			}
			dismiss();
		} else {
			toggle(a.root, null, -1);
		}
	}
	
	
	@Override
	protected void onShow() {
		Resize();
	}
	
	final boolean act() {
		return isVisible() || pin();
	}
	
	boolean pin() {
		if (splitView!=null) {
			return PDICMainAppOptions.getPinPicDictDialog();
		}
		return false;
	}
	
	public void notifyDataSetChanged() {
		if(mAdapter!=null) {
			mAdapter.notifyDataSetChanged();
		}
	}

//	int  width=-1,height=-1,mMaxH=-1;
//	public void onResume()
//	{
//		super.onResume();
//		if(width!=-1 || height!=-1)//
//			if(getDialog()!=null) {
//				Window window = getDialog().getWindow();
//				if(window!= null) {
//					WindowManager.LayoutParams  attr = window.getAttributes();
//					if(attr.width!=width || attr.height!=height) {
//						//CMN.Log("onResume_");
//						window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//						window.setDimAmount(0.1f);
//						window.setBackgroundDrawableResource(R.drawable.popup_shadow_l);
//						root.mMaxHeight=mMaxH;
//						window.setLayout(width,height);
//
//						getView().post(() -> refresh(false, bForcePin));
//					}
//				}
//				getDialog().setCanceledOnTouchOutside(true);
//			}
//	}

//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setStyle(STYLE_NO_FRAME, 0);
//	}
	
	private void Resize() {
		int littleIdeal = realWidth;
		Resources res = a.mResource;
		int factor=1;
		if(a.dm.widthPixels>littleIdeal) {
			littleIdeal = Math.min(a.dm.widthPixels, Math.max(realWidth, (int)res.getDimension(R.dimen.idealdpdp))*55/45);
			factor=2;
		}
		View dialogLayout = this.dialogLayout;
		ViewGroup.MarginLayoutParams mlarp = (ViewGroup.MarginLayoutParams) dialogLayout.getLayoutParams();
		int[] margins;
		if(dialogLayout.getTag()==null) {
			dialogLayout.setTag(margins=new int[2]);
			margins[0] = mlarp.topMargin;
			margins[1] = mlarp.bottomMargin;
		} else {
			margins = (int[]) dialogLayout.getTag();
		}
		mlarp.width = littleIdeal - (int) (1 * res.getDimension(R.dimen._28_));
		mlarp.topMargin= (int) (margins[0]/factor*1.5);
		mlarp.bottomMargin= (int) (margins[1]/factor*1.5);
	}
	
	class HomeAdapter extends RecyclerView.Adapter<MyViewHolder>
	{
		@Override
		public int getItemCount() {
			return a.md.size();
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
		public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
			holder.position = position;
			int adapter_idx=reason==2?a.wordPopup.currentClick_adapter_idx:a.adapter_idx;
			FlowTextView tv = holder.tv;
			
			boolean isThisSelected = adapter_idx==position;
			
			holder.itemView.setBackgroundColor(isThisSelected?0xff4F7FDF:Color.TRANSPARENT);
			
			tv.setTextColor(GlobalOptions.isDark||isThisSelected?Color.WHITE:Color.BLACK);
			
			tv.PostEnabled = PostEnabled;
			
			tv.setStarLevel(a.md_get_StarLevel(position));
			
			tv.setCompoundDrawables(a.getActiveStarDrawable(), null, null, null);
			
			String text = a.md_getName(position);
			
			tv.SetSearchPattern(SearchPattern, text);
			
			
			Drawable cover = a.md_getCover(position);
			ViewUtils.setVisibility((View) holder.cover.getParent(), cover!=null);
			ViewGroup.LayoutParams lp = holder.cover.getLayoutParams();
			lp.width = (int) (lp.height/(cover==null?1.5f:1));
			holder.cover.setImageDrawable(cover);
			
			tv.setText(text);
		}
	}
	
	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if(tag instanceof MyViewHolder){
			int position = ((MyViewHolder) tag).position;
//			if(v.getId()==R.id.coverp) {
//				a.showAboutDictDialogAt(position);
//			} else {
				if(!act()) return;
				if(reason==2){//点译上游
					int tmpPos = a.wordPopup.currentClick_adapter_idx;
					a.wordPopup.CCD=a.md_get(position);
					a.wordPopup.CCD_ID=a.wordPopup.currentClick_adapter_idx = position;
					a.popupWord(ViewUtils.getTextInView(a.wordPopup.popupTextView), null, -1, null);
					mAdapter.notifyItemChanged(tmpPos);
					mAdapter.notifyItemChanged(position);
					if(a instanceof PDICMainActivity){
						((PDICMainActivity)a).dismissDictPicker(R.anim.dp_dialog_exit);
					}else {
						dismiss();
					}
				}
				else {//当前词典
					int tmpPos = a.adapter_idx;
					a.adapter_idx = position;
					a.switch_To_Dict_Idx(position, true, true, null);
					mAdapter.notifyItemChanged(tmpPos);
					if(tmpPos!=position){
						mAdapter.notifyItemChanged(position);
					}
					if (bShouldCloseAfterChoose) {
						v.post(this::dismiss);
					}
				}
//			}
		}
		switch (v.getId()) {
			case R.id.dialogHolder:
				dismiss();
				break;
			case R.id.dictName:
				scrollThis();
				break;
			case R.id.pinPicBook:
				CheckableImageView cb = (CheckableImageView) v;
				cb.toggle();
				opt.setPinPicDictDialog(cb.isChecked());
				//if(pickDictDialog!=null) pickDictDialog.isDirty=true;
				refresh(false, 0);
				break;
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
					etSearchDict_getWindowToken = etSearchDict.getWindowToken();
					a.imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					if (!isVisible())
						toggle(a.root, null, -1);
				} else {
					if (v == Searchbar.getNavigationBtn()) {
						ViewUtils.setVisible(Searchbar, false);
						a.imm.hideSoftInputFromWindow(etSearchDict_getWindowToken, 0);
						if (pin()) dismiss();
					} else {
						a.etSearch.clearFocus();
						ViewUtils.setVisible(Searchbar, true);
						//ViewUtils.setVisible(UIData.dialogHolder, true);
						etSearchDict.requestFocus();
						a.imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					}
				}
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
					ArrayList<PlaceHolder> CosyChair = a.lazyLoadManager.CosyChair;
					int fullSize = CosyChair.size();
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
							if (j>=0 && j<CosyChair.size()) {
								String name = CosyChair.get(j).pathname;
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
					a.imm.hideSoftInputFromWindow(etSearchDict_getWindowToken, 0);
					notifyDataSetChanged();
				}
			} break;
			case R.id.ivDeleteText:{
				if(Searchbar!=null) {
					SearchIncantation = null;
					etSearchDict.setText("");
					//imm.hideSoftInputFromWindow(etSearchDict_getWindowToken, 0);
					notifyDataSetChanged();
				}
			} break;
		}
	}
	
	private void scrollThis() {
		int adapter_idx=reason==2?a.wordPopup.currentClick_adapter_idx:a.adapter_idx;
		if(lman!=null) {
			if(adapter_idx>lman.findLastVisibleItemPosition() || adapter_idx<lman.findFirstVisibleItemPosition()) {
				int target = Math.max(0, adapter_idx-5);
				lman.scrollToPositionWithOffset(reason==2?a.wordPopup.CCD_ID:target, 0);
				CMN.Log("scrolled");
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
		public int position;
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
	
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		a=(MainActivityUIBase) getActivity();
//		if(GlobalOptions.isDark) {
//			ViewUtils.setListViewScrollbarColor(mRecyclerView, true);
//		}
//	}
}
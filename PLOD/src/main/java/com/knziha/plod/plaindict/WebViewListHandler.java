package com.knziha.plod.plaindict;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.knziha.plod.PlainUI.AppUIProject.ContentbarBtnIcons;
import static com.knziha.plod.PlainUI.AppUIProject.RebuildBottombarIcons;
import static com.knziha.plod.dictionary.Utils.IU.NumberToText_SIXTWO_LE;
import static com.knziha.plod.plaindict.CMN.EmptyRef;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.PlainUI.AlloydPanel;
import com.knziha.plod.PlainUI.AppUIProject;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowCheckedTextView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.additiveMyCpr1;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WebViewListHandler extends ViewGroup {
	final MainActivityUIBase a;
	/** -2=auto;0=false;1=true*/
	public int bMergeFrames = 0;
	public boolean bShowInPopup = false;
	public boolean bMergingFrames = false;
	public boolean bShowingInPopup = false;
	ScrollViewmy WHP;
	ViewGroup webholder;
	public additiveMyCpr1 jointResult;
	public WebViewmy mMergedFrame;
	BookPresenter mMergedBook;
	public ArrayList<BookPresenter> frames = new ArrayList();
	public ArrayList<long[]> framesDisplaying = new ArrayList();
	public ArrayList<PlainWeb> moders = new ArrayList();
	@NonNull
	public final ContentviewBinding contentUIData;
	public ImageView[] ContentbarBtns = new ImageView[ContentbarBtnIcons.length];
	private boolean contentViewSetup;
	
	public WebViewListHandler(@NonNull MainActivityUIBase a, @NonNull ContentviewBinding contentUIData) {
		super(a);
		this.a = a;
		setId(R.id.webholder);
		//setUseListView(true);
		this.contentUIData = contentUIData;
		this.WHP = contentUIData.WHP;
		this.webholder = contentUIData.webholder;
	}
	
	public ViewGroup getViewGroup() {
		return mViewMode==WEB_LIST_MULTI?webholder:contentUIData.webSingleholder;
	}
	
	public ViewGroup getAnotherViewGroup() {
		return mViewMode==WEB_LIST_MULTI?contentUIData.webSingleholder:webholder;
	}
	
	public View getChildAt(int frameAt) {
		return contentUIData.webholder.getChildAt(frameAt);
	}
	
	@Override
	public int getChildCount() {
		return contentUIData.webholder.getChildCount();
	}
	
	public void shutUp() {
		if(WHP.getVisibility()==View.VISIBLE) {
//			if(contentUIData.webholder.getChildCount()!=0)
//				contentUIData.webholder.removeAllViews();
			WHP.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void addView(View child, int index) {
		(mViewMode==WEB_VIEW_SINGLE?contentUIData.webSingleholder:contentUIData.webholder).addView(child, index);
	}
	
	public void removeAllViews() {
		CMN.Log("removeAllViews!!!");
		(mViewMode==WEB_VIEW_SINGLE?contentUIData.webSingleholder:contentUIData.webholder).removeAllViews();
	}
	
	@Override
	public void setVisibility(int visibility) {
		(mViewMode==WEB_VIEW_SINGLE?contentUIData.webSingleholder:WHP).setVisibility(visibility);
	}
	
	@Override
	public int getVisibility() {
		return (mViewMode==WEB_VIEW_SINGLE?contentUIData.webSingleholder:WHP).getVisibility();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) { }
	
	public void setBackgroundColor(int manFt_globalPageBackground) {
		WHP.setBackgroundColor(manFt_globalPageBackground);
	}
	
	public int getFrameAt() {
		if(!bMergingFrames) {
			final int currentHeight=WHP.getScrollY();
			for(int i=0;i<webholder.getChildCount();i++) {
				View CI = webholder.getChildAt(i);
				if(CI.getBottom() > currentHeight) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public ViewGroup getScrollView() {
		return WHP;
	}
	
	public void setScrollbar() {
		contentUIData.dragScrollBar.setMax(webholder.getMeasuredHeight()-WHP.getMeasuredHeight());
		contentUIData.dragScrollBar.setProgress(WHP.getScrollY());
		//a.mBar.onTouch(null, MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,0,0,0));
	}
	
	public OnLayoutChangeListener OLCL;
	public void installLayoutScrollListener(resultRecorderCombined recom) {
		WHP.touchFlag.first=false;
		if(OLCL==null) {
			OLCL = new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,int oldBottom) {
					//CMN.Log("onLayoutChange", expectedPos, sv.getScrollY());
					//CMN.Log("onLayoutChange", a.WHP.touchFlag.first, scrolled, bottom - top >= expectedPos + sv.getMeasuredHeight());
					//CMN.Log("onLayoutChange", bottom - top , expectedPos + sv.getMeasuredHeight());
					if (WHP.touchFlag.first) {
						contentUIData.mainProgressBar.setVisibility(View.GONE);
						v.removeOnLayoutChangeListener(this);
						return;
					}
					//if(expectedPos==0) return;
					int HGEIGHT = bottom - top;
					if (HGEIGHT < recom.LHGEIGHT)
						recom.scrolled = false;
					recom.LHGEIGHT = HGEIGHT;
					if(recom.scrollTarget!=null)
						recom.expectedPos=recom.scrollTarget.getTop();
					ScrollViewmy sv = WHP;
					if (!recom.scrolled) {
						if (HGEIGHT >= recom.expectedPos + sv.getMeasuredHeight()) {
							sv.scrollTo(0, recom.expectedPos);//smooth
							//CMN.Log("onLayoutChange scrolled", expectedPos, sv.getMeasuredHeight());
							if (sv.getScrollY() == recom.expectedPos) {
								contentUIData.mainProgressBar.setVisibility(View.GONE);
								recom.scrolled = true;
							}
						}
					}
				}
			};
		}
		
		recom.LHGEIGHT=0;
		webholder.removeOnLayoutChangeListener(OLCL);
		if(!recom.toHighLight){
			ViewUtils.addOnLayoutChangeListener(webholder, OLCL);
			if(contentUIData.mainProgressBar!=null)
				contentUIData.mainProgressBar.setVisibility(recom.expectedPos==0?View.GONE:View.VISIBLE);
			recom.scrolled=false;
		}
		contentUIData.PageSlider.setIBC(null, this);
	}
	
	public void NotifyScrollingTo(resultRecorderCombined recom) {
		WHP.touchFlag.first=false;
		recom.LHGEIGHT=WHP.getHeight();
		webholder.removeOnLayoutChangeListener(OLCL); // todo save this step ???
		ViewUtils.addOnLayoutChangeListener(webholder, OLCL);
	}
	
	public void initWebHolderScrollChanged() {
		DragScrollBar mBar = contentUIData.dragScrollBar;
		if(mBar.getVisibility()==View.VISIBLE){
			if(a.onWebHolderScrollChanged==null){
				WHP.scrollbar2guard=mBar;
				WHP.setScrollViewListener(a.onWebHolderScrollChanged=(v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
					if(mBar.isHidden()){
						if(Math.abs(oldScrollY-scrollY)>=10*a.dm.density)
							mBar.fadeIn();
					}
					if(!mBar.isHidden()){
						if(!mBar.isWebHeld)
							mBar.hiJackScrollFinishedFadeOut();
						if(!mBar.isDragging){
							mBar.setMax(webholder.getMeasuredHeight()-WHP.getMeasuredHeight());
							mBar.setProgress(WHP.getScrollY());
						}
					}
				});
			}
			mBar.fadeOut();
		}
		mBar.setDelimiter("|||", bMergingFrames?mMergedFrame:getViewGroup());
	}
	
	public WebViewmy getMergedFrame() {
		if(mMergedFrame==null) {
			try {
				mMergedBook = new BookPresenter(new File("empty"), null, 1, 2);
			} catch (IOException ignored) { }
			mMergedBook.initViewsHolder(a);
			mMergedFrame = mMergedBook.mWebView;
			mMergedFrame.weblistHandler = this;
			mMergedBook.rl.setTag(this);
			mMergedFrame.setWebViewClient(a.myWebClient);
			mMergedFrame.setWebChromeClient(a.myWebCClient);
			//mMergedFrame.setOnScrollChangedListener(null);
			//mMergedFrame.SetupScrollRect(true);
			mMergedFrame.getSettings().setTextZoom(BookPresenter.def_fontsize);
		}
		return mMergedFrame;
	}
	
	boolean webHolderSwapHide = true;
	
	/** 是否将共用的 mMergedFrame 以一定手段塞入webholder列表。但是列表只会显示一个（mMergedFrame 或 mWebView）。 */
	public void initMergedFrame(boolean mergeWebHolder, boolean popup, boolean bUseMergedUrl) {
		if(bUseMergedUrl && mMergedFrame!=null) {
			mMergedFrame.presenter=mMergedBook;
			mMergedBook.toolbar.setVisibility(View.GONE);
		}
		if(!popup && bShowingInPopup) {
			ViewUtils.removeView(alloydPanel.toolbar);
			contentUIData.webcontentlister.setPadding(0,0,0,0);
			bShowingInPopup = false;
		}
		if(bMergingFrames!=mergeWebHolder) {
			CMN.Log("reinitMergedFrame::", mergeWebHolder, popup, bUseMergedUrl);
			if(mergeWebHolder) { // 替换scrollview为framelayout、webholder高度充满视图
//				if(WHP1==null) {
//					WHP1 = new FrameLayout(a);
//				}
//				this.webholder = WHP1;
//				ViewGroup webholder = this.WHP1;
//				WebViewmy mMergedFrame = getMergedFrame();
//				//ViewUtils.addViewToParent(webholder, WHP1);
//				ViewUtils.addViewToParent(mMergedFrame.rl, webholder);
//				//mMergedBook.rl.getLayoutParams().height = MATCH_PARENT;
//				mMergedBook.toolbar.setVisibility(View.GONE);
//				contentUIData.navBtns.setVisibility(View.GONE);
//				ViewUtils.addViewToParent(WHP1, contentUIData.PageSlider, 1);
//				if(webHolderSwapHide) {
//					WHP1.setVisibility(View.VISIBLE);
//					WHP.setVisibility(View.GONE);
//				} else {
//					ViewUtils.removeView(WHP);
//				}
				
				WebViewmy mMergedFrame = getMergedFrame();
				ViewUtils.addViewToParent(mMergedFrame.rl, contentUIData.webSingleholder);
				mMergedBook.toolbar.setVisibility(View.GONE);
				contentUIData.navBtns.setVisibility(View.GONE);
				if(webHolderSwapHide) {
					WHP.setVisibility(View.GONE);
				} else {
					ViewUtils.removeView(WHP);
				}
			}
			else {
				contentUIData.webcontentlister.setAlpha(1);
				if(mMergedBook!=null) {
					mMergedBook.toolbar.setVisibility(View.VISIBLE);
				}
				webholder.getLayoutParams().height = WRAP_CONTENT;
				ViewUtils.addViewToParent(WHP, contentUIData.PageSlider, 1);
				if(webHolderSwapHide) {
					WHP.setVisibility(View.VISIBLE);
				}
			}
			bMergingFrames = mergeWebHolder;
		}
	}
	
	public void toggleFoldAll() {
		int targetVis=View.VISIBLE;
		int cc=getChildCount();
		if(cc>0) {
			for (int i = 0; i < cc; i++) {
				if (getChildAt(i).findViewById(R.id.webviewmy).getVisibility() != View.GONE) {
					targetVis = View.GONE;
					break;
				}
			}
			if(targetVis==View.GONE) {
				a.awaiting = false;
			}
			for (int i = 0; i < cc; i++) {
				View childAt = getChildAt(i);
				WebViewmy targetView = childAt.findViewById(R.id.webviewmy);
				if(targetVis==View.GONE) {
					targetView.setVisibility(targetVis);
				} else if(targetView.getVisibility()!=View.VISIBLE){
					childAt.findViewById(R.id.toolbar_title).performClick();
				}
			}
		}
	}
	
	public void prvnxtFrame(boolean nxt) {
		if(bMergingFrames) {
			mMergedFrame.evaluateJavascript(nxt?"prvnxtFrame(1)":"prvnxtFrame()", null);
		} else {
			final int currentHeight=WHP.getScrollY();
			int cc=contentUIData.webholder.getChildCount();
			int childAtIdx=cc;
			int top;
			for(int i=0;i<cc;i++) {
				top = contentUIData.webholder.getChildAt(i).getTop();
				if(top>=currentHeight){
					childAtIdx=i;
					if(nxt && top!=currentHeight) --childAtIdx;
					break;
				}
			}
			childAtIdx+=nxt?1:-1;
			if(childAtIdx>=cc){
				a.scrollToPagePosition(contentUIData.webholder.getChildAt(cc-1).getBottom());
			} else {
				a.scrollToWebChild(contentUIData.webholder.getChildAt(childAtIdx));
			}
		}
	}
	
	int frameCount;
	int frameAt;
	WeakReference<AlertDialog> jumpListDlgRef = EmptyRef;
	AlertDialog jumpListDlg;
//	static class TextConfig{
//		int[] ids;
//		float tsz;
//	}
//	TextConfig tf;
	
	public void showJumpListDialog() {
		if(!bMergingFrames) frameAt = getFrameAt();
		frameCount = frames.size();
		if(jumpListDlg==null){
			jumpListDlg = jumpListDlgRef.get();
		}
		AlertDialog dialog = jumpListDlg;
		if(dialog==null){
			dialog = new AlertDialog.Builder(a/*,R.style.DialogStyle*/)
					.setTitle("跳转")
					.setAdapter(new BaseAdapter() {
									public int getCount() { return frameCount; }
									public Object getItem(int position) { return null; }
									public long getItemId(int pos) { return pos<frames.size()?frames.get(pos).getId():-1;}
									public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
										FlowCheckedTextView ret;
										if(convertView!=null){
											ret = (FlowCheckedTextView) convertView;
										}
										else {
											CMN.rt();
											ret = (FlowCheckedTextView) a.getLayoutInflater().inflate(R.layout.singlechoice_w, parent, false);
//											if(tf==null) {
//												tf = new textConfig();
//												tf.ids = new int[]{
//														android.R.attr.textAppearanceListItemSmall
//														, android.R.attr.textColorAlertDialogListItem
//														, android.R.attr.listChoiceIndicatorSingle
//												};
//												TypedArray a = getTheme().obtainStyledAttributes(R.style.AppTheme, tf.ids);
//												tf.ids[0]=a.getResourceId(0, 0);
//												tf.ids[1]=a.getResourceId(1, 0);
//												tf.ids[2]=a.getResourceId(2, 0);
//												a.recycle();
//												tf.tsz = mResource.getDimension(R.dimen.lvtextsize);
//											}
//											ret = new FlowCheckedTextView(MainActivityUIBase.this);
//											ret.setTextAppearance(tf.ids[0]);
//											ret.setTextColor(tf.ids[1]);
//											ret.setCheckMarkDrawable(tf.ids[2]);
//											ret.setTextSize(tf.tsz);
//											ret.setGravity(Gravity.CENTER_VERTICAL);
//											ret.setId(android.R.id.text1);
//											ret.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//											ret.setPadding((int) (16*GlobalOptions.density), 0, (int) (7*GlobalOptions.density), 0);
											ret.setMinimumHeight((int) getResources().getDimension(R.dimen._50_));
											CMN.pt("创建视图!!");
										}
										BookPresenter book = a.getBookById(getItemId(pos));
										if (book!=a.EmptyBook) {
											FlowTextView tv = ret.mFlowTextView;
											tv.setCompoundDrawables(a.getActiveStarDrawable(), null, null, null);
											tv.setCover(book.getCover());
											tv.setTextColor(GlobalOptions.isDark?Color.WHITE: Color.BLACK);
											tv.setStarLevel(PDICMainAppOptions.getDFFStarLevel(book.getFirstFlag()));
											ret.setChecked(pos == frameAt);
											ret.setText(book.getDictionaryName());
										} else {
											ret.setText("Error!!!");
										}
										return ret;
									}
								}
							, (dlg, pos) -> {
								if(bMergingFrames) {
									if(pos<frames.size()) {
										BookPresenter book = frames.get(pos);
										if (book!=null) {
											StringBuilder sb = new StringBuilder(24);
											sb.append("scrollToPosId('d");
											NumberToText_SIXTWO_LE(book.getId(), sb);
											sb.append("',");
											sb.append(frameAt=pos);
											sb.append(")");
											mMergedFrame.evaluateJavascript(sb.toString(), null);
										}
									}
								} else {
									View childAt = getChildAt(pos);
									if(childAt!=null) {
										a.scrollToWebChild(childAt);
										a.recCom.scrollTo(childAt, a);
									}
								}
								//dlg.dismiss();
							})
					.show();
			dialog.setCanceledOnTouchOutside(true);
			jumpListDlgRef = new WeakReference<>(jumpListDlg=dialog);
			if(GlobalOptions.isDark) {
				dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			}
		}
		else {
			CMN.Log("resue dkg");
			dialog.show();
		}
		if(!GlobalOptions.isLarge) {
			dialog.getWindow().setLayout((int) (a.dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor)), -2);
		}
		//d.getWindow().getDecorView().setBackgroundResource(R.drawable.popup_shadow_l);
		//d.getWindow().getDecorView().getBackground().setColorFilter(GlobalOptions.NEGATIVE);
		//d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
		a.imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
		if(bMergingFrames) {
			mMergedFrame.evaluateJavascript("currentFrame("+frameAt+")", value -> {
				try {
					if(jumpListDlg!=null && jumpListDlg.isShowing()) {
						String[] arr=value.split("-", 2);
						if(arr.length==2) {
							long id = IU.TextToNumber_SIXTWO_LE(arr[0].substring(2));
							int pos = IU.parsint(arr[1], 0);
							//CMN.debug(value, pos, id);
							if(pos>=frames.size() || frames.get(pos).getId()!=id) {
								pos=-1;
							}
							if(pos==-1) {
								pos = frames.indexOf(id);
							}
							if(frameAt!=pos) {
								frameAt = pos;
								((BaseAdapter)jumpListDlg.getListView().getAdapter()).notifyDataSetChanged();
							}
						}
					}
					//CMN.debug(value, frameAt);
				} catch (Exception e) {
					CMN.debug(e);
				}
			});
		}
	}
	
	public final static int WEB_LIST_MULTI=0;
	public final static int WEB_VIEW_SINGLE=1;
	int mViewMode=WEB_LIST_MULTI;
	public void setViewMode(int mode, boolean bUseMergedUrl) {
		if(mViewMode!=mode || bMergingFrames!=bUseMergedUrl) {
			mViewMode = mode;
			int vis = mode == WEB_VIEW_SINGLE || bUseMergedUrl ? View.GONE : View.VISIBLE;
			contentUIData.WHP.setVisibility(vis);
			contentUIData.navBtns.setVisibility(vis);
			
			vis = mode==WEB_VIEW_SINGLE || bUseMergedUrl ? View.VISIBLE : View.GONE;
			contentUIData.webSingleholder.setVisibility(vis);
		}
	}
	
	int getViewMode() {
		return mViewMode;
	}
	
	int[] versions=new int[8];
	
	public void checkUI() {
		if(a.contentbar_project!=null && ViewUtils.checkSetVersion(versions, 0, a.contentbar_project.version)) {
			a.contentbar_project.bottombar = contentUIData.bottombar2;
			RebuildBottombarIcons(a, a.contentbar_project, a.mConfiguration);
		}
		
		if(ViewUtils.checkSetVersion(versions, 1, a.MainAppBackground)) {
			contentUIData.bottombar2.setBackgroundColor(a.MainAppBackground);
		}
		if(ViewUtils.checkSetVersion(versions, 2, a.MainPageBackground)) {
			//if(widget12.getTag(R.id.image)==null)
			contentUIData.webSingleholder.setBackgroundColor(a.MainPageBackground);
			contentUIData.webholder.setBackgroundColor(a.MainPageBackground);
		}
	}
	
	AlloydPanel alloydPanel;
	public void popupContentView(ViewGroup root, String key) {
		if(alloydPanel==null) {
			alloydPanel = new AlloydPanel(a, contentUIData);
		}
		if(!alloydPanel.isVisible()) {
			alloydPanel.toggle(root, null, -1);
		}
		alloydPanel.AllMenus.tag = this;
		alloydPanel.refresh();
		alloydPanel.toolbar.setTitle(key);
		alloydPanel.AllMenus.setItems(alloydPanel.RandomMenu);
		//contentUIData.webcontentlister.setPadding(0,0,0,0);
		ViewUtils.addViewToParent(alloydPanel.toolbar, contentUIData.webcontentlister, 0);
		//a.setContentBow(false);
		
		bShowingInPopup = true;
	}
	
	public void dismissPopup() {
		if(alloydPanel!=null) {
			alloydPanel.dismiss();
		}
	}
	
	public void setUpContentView() {
		if(!contentViewSetup) {
			contentViewSetup = true;
			MainActivityUIBase a = this.a;
			contentUIData.browserWidget13.setOnClickListener(a);
			contentUIData.browserWidget14.setOnClickListener(a);
			
			contentUIData.dragScrollBar.setOnProgressChangedListener(_mProgress -> {
				contentUIData.PageSlider.TurnPageSuppressed = _mProgress==-1;
			});
			
			SplitView webcontentlister = contentUIData.webcontentlister;
			webcontentlister.multiplier=-1;
			webcontentlister.isSlik=true;
			
	//		webcontentlister.setPrimaryContentSize(a.CachedBBSize,true);
	//		webcontentlister.setPageSliderInf(a.inf);
			
			contentUIData.bottombar2.setBackgroundColor(a.MainBackground);
			
			boolean tint = PDICMainAppOptions.getTintIconForeground();
			for (int i = 0; i <= 5; i++) {
				ImageView iv = (ImageView) contentUIData.bottombar2.getChildAt(i);
				ContentbarBtns[i]=iv;
				iv.setOnClickListener(a);
				if(tint) iv.setColorFilter(a.ForegroundTint, PorterDuff.Mode.SRC_IN);
				iv.setOnLongClickListener(a);
			}
			String contentkey = "ctnp#"+ a.cbar_key;
			String appproject = a.opt.getAppContentBarProject(contentkey);
			if(appproject==null) appproject="0|1|2|3|4|5";
			if(a.contentbar_project==null) {
				a.contentbar_project = new AppUIProject(contentkey, ContentbarBtnIcons, appproject, contentUIData.bottombar2, ContentbarBtns);
				a.contentbar_project.type = a.cbar_key;
			}
			a.contentbar_project.bottombar = contentUIData.bottombar2;
			a.contentbar_project.btns = ContentbarBtns;
			RebuildBottombarIcons(a, a.contentbar_project, a.mConfiguration);
		}
	}
}
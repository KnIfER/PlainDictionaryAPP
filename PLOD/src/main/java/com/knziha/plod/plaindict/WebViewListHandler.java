package com.knziha.plod.plaindict;

import static com.knziha.plod.PlainUI.AppUIProject.ContentbarBtnIcons;
import static com.knziha.plod.PlainUI.AppUIProject.RebuildBottombarIcons;
import static com.knziha.plod.dictionary.Utils.IU.NumberToText_SIXTWO_LE;
import static com.knziha.plod.plaindict.CMN.EmptyRef;

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;

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
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;

public class WebViewListHandler extends ViewGroup implements View.OnClickListener {
	final MainActivityUIBase a;
	/** -2=auto;0=false;1=true*/
	public int bMergeFrames = 0;
	public boolean bDataOnly = false;
	public boolean bShowInPopup = false;
	public boolean bMergingFrames = false;
	public boolean bShowingInPopup = false;
	public ScrollViewmy WHP;
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
	private int lastScrollUpdateY;
	BookPresenter lastScrollBook;
	WebViewmy lastScrollBookView;
	
	public WebViewListHandler(@NonNull MainActivityUIBase a, @NonNull ContentviewBinding contentUIData) {
		super(a);
		this.a = a;
		setId(R.id.webholder);
		//setUseListView(true);
		this.contentUIData = contentUIData;
		this.WHP = contentUIData.WHP;
		this.webholder = contentUIData.webholder;
		hDataSinglePage.webviewHolder = contentUIData.webSingleholder;
		hDataMultiple.webviewHolder = contentUIData.webholder;
		
		DragScrollBar mBar = contentUIData.dragScrollBar;
		if(WHP.getScrollViewListener()==null) {
			WHP.scrollbar2guard=mBar;
			WHP.setScrollViewListener((v, x, y, oldX, oldY) -> {
				oldY = y-WHP.oldY;
				if(mViewMode==WEB_LIST_MULTI && oldY!=0
					&& Math.abs(oldY)>8) {
						if(mBar.isHidden()){
							if(Math.abs(oldY)>=10*a.dm.density)
								mBar.fadeIn();
						}
						if(!mBar.isHidden()){
							if(!mBar.isWebHeld)
								mBar.hiJackScrollFinishedFadeOut();
							if(!mBar.isDragging){
								mBar.setMax(webholder.getMeasuredHeight()-WHP.getMeasuredHeight());
								mBar.setProgress(y);
							}
						}
						if(Math.abs(lastScrollUpdateY-y)>GlobalOptions.density*50) {
							lastScrollUpdateY=y;
							//CMN.pt("滚动="+Math.abs(oldY)+","+"y="+y+"::");
							//CMN.rt();
							int bot=WHP.getScrollY() + WHP.getHeight()/2;
							for (int i = 0; i < webholder.getChildCount(); i++) {
								if(webholder.getChildAt(i).getBottom()>=bot) {
									WebViewmy wv = (WebViewmy)webholder.getChildAt(i).getTag();
									if(lastScrollBook!=wv.presenter) {
										setLastScrollBook(wv);
									}
									break;
								}
							}
						}
				}
				WHP.oldY = y;
				if (WHP.mIsFling && Math.abs(oldY) < 2) {
					WHP.mIsFling = false;
				}
			});
			mBar.fadeOut();
		}
		//WHP.setBackground(null);
	}
	
	public void setLastScrollBook(WebViewmy wv) {
		if(lastScrollBook!=wv.presenter) {
			lastScrollBook = wv.presenter;
			String name = wv.presenter.getDictionaryName();
			name = name.substring(0, name.lastIndexOf("."));
			contentUIData.dictName.setText(name);
			contentUIData.dictNameStroke.setText(name);
		}
		lastScrollBookView = wv;
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
		//mBar.setDelimiter("|||", bMergingFrames?mMergedFrame:getViewGroup());
	}
	
	public WebViewmy getMergedFrame() {
		if(mMergedFrame==null) {
			try {
				mMergedBook = new BookPresenter(new File("empty"), null, 1, 2);
			} catch (IOException ignored) { }
			mMergedBook.initViewsHolder(a);
			mMergedFrame = mMergedBook.mWebView;
			mMergedFrame.weblistHandler = this;
			mMergedBook.rl.setTag(mMergedFrame);
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
			if(mergeWebHolder) {
				WebViewmy mMergedFrame = getMergedFrame();
				ViewUtils.addViewToParent(mMergedFrame.rl, contentUIData.webSingleholder);
				mMergedBook.toolbar.setVisibility(View.GONE);
//				contentUIData.navBtns.setVisibility(View.GONE);
//				if(webHolderSwapHide) {
//					WHP.setVisibility(View.GONE);
//				} else {
//					ViewUtils.removeView(WHP);
//				}
			}
			else {
				contentUIData.webcontentlister.setAlpha(1);
				if(mMergedBook!=null) {
					mMergedBook.toolbar.setVisibility(View.VISIBLE);
				}
//				webholder.getLayoutParams().height = WRAP_CONTENT;
//				ViewUtils.addViewToParent(WHP, contentUIData.PageSlider, 1);
//				if(webHolderSwapHide) {
//					WHP.setVisibility(View.VISIBLE);
//				}
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
	public void setViewMode(int mode, boolean bUseMergedUrl, WebViewmy dictView) {
		if(mViewMode!=mode || bMergingFrames!=bUseMergedUrl) {
			mViewMode = mode;
			int vis = mode == WEB_VIEW_SINGLE || bUseMergedUrl ? View.GONE : View.VISIBLE;
			contentUIData.WHP.setVisibility(vis);
			contentUIData.navBtns.setVisibility(vis);
			
			vis = mode==WEB_VIEW_SINGLE || bUseMergedUrl ? View.VISIBLE : View.GONE;
			contentUIData.webSingleholder.setVisibility(vis);
		}
		this.dictView = dictView;
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
			//contentUIData.webholder.setBackgroundColor(a.MainPageBackground);
			contentUIData.WHP.setBackgroundColor(a.MainPageBackground);
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
	
	public boolean isPopupShowing() {
		return alloydPanel!=null && alloydPanel.isVisible();
	}
	
	public void setUpContentView(int cbar_key) {
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
			String contentkey = "ctnp#"+ cbar_key;
			String appproject = a.opt.getAppContentBarProject(contentkey);
			if(appproject==null) appproject="0|1|2|3|4|5";
			if(a.contentbar_project==null) {
				a.contentbar_project = new AppUIProject(contentkey, ContentbarBtnIcons, appproject, contentUIData.bottombar2, ContentbarBtns);
				a.contentbar_project.type = cbar_key;
			}
			a.contentbar_project.bottombar = contentUIData.bottombar2;
			a.contentbar_project.btns = ContentbarBtns;
			RebuildBottombarIcons(a, a.contentbar_project, a.mConfiguration);
		}
	}
	
	public boolean isWeviewInUse(ViewGroup someView) {
		ViewParent sp = someView.getParent();
		if(sp==null) return false;
		if(ViewUtils.isVisibleV2(contentUIData.webSingleholder) && sp==contentUIData.webSingleholder) {
			return true;
		}
		if(ViewUtils.isVisibleV2(contentUIData.WHP) && ViewUtils.isVisibleV2(contentUIData.webholder) && sp==contentUIData.webholder) {
			return true;
		}
		return false;
	}
	
	final public boolean isViewSingle() {
		return mViewMode==WEB_VIEW_SINGLE;
	}
	
	int cc;
	boolean inlineJump;
	private WebView jumper;
	
	public Toolbar MainPageSearchbar;
	public EditText MainPageSearchetSearch;
	TextView MainPageSearchindicator;
	TextView MainPageSearchDictionaryIndi;
	String MainPageSearchetSearchStartWord;
	boolean HiFiJumpRequested;
	
	public void jumpHighlight(int d, boolean calcIndicator){
		try {
			cc=0;
			inlineJump=true;
			do_jumpHighlight(d, calcIndicator);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateInPageSch(String did, int keyIdx, int keyz, int total) {
		String text;
		String dictName=null;
		if(!TextUtils.equals((String)MainPageSearchindicator.getTag(),did)) {
			dictName = new File(a.getBookNameByIdNoCreation(IU.TextToNumber_SIXTWO_LE(new CharSequenceKey(did, 1)))).getName();
			MainPageSearchindicator.setTag(did);
		}
		if(keyIdx>=0) {
			keyIdx+=1;
			if(total>keyz)
				text = keyIdx+"/"+keyz+" ("+total+")";
			else
				text = keyIdx+"/"+keyz;
		} else {
			text = ""+total;
		}
		String dictName1=dictName;
		MainPageSearchindicator.post(() -> {
			MainPageSearchindicator.setText(text);
			if(dictName1!=null)MainPageSearchDictionaryIndi.setText(dictName1);
		});
	}
	
	public static class HighlightVagranter {
		ViewGroup webviewHolder;
		int HlightIdx;
		int AcrArivAcc;
//		public HighlightVagranter(ViewGroup webviewHolder) {
//			this.webviewHolder = webviewHolder;
//		}
	}
	
	void resetLights(ViewGroup webviewHolder, int d) {
		if(webviewHolder!=null) {
			int max = webviewHolder.getChildCount();
			String exp = "resetLight(" + d + ")";
			for (int index = 0; index < max; index++) {
				if (webviewHolder.getChildAt(index) instanceof LinearLayout) {
					ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
					if (webHolder.getChildAt(1) instanceof WebView) {
						((WebView) webHolder.getChildAt(1))
								.evaluateJavascript(exp, null);
					}
				}
			}
		}
	}
	
	void clearLights(ViewGroup webviewHolder){
		if(webviewHolder!=null){
			int max=webviewHolder.getChildCount();
			String exp="clearHighlights()";
			for (int index = 0; index < max; index++) {
				if(webviewHolder.getChildAt(index) instanceof LinearLayout){
					ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
					if(webHolder.getChildAt(1) instanceof WebView){
						((WebView)webHolder.getChildAt(1))
								.evaluateJavascript(exp,null);
					}
				}
			}
		}
	}
	
	
	public void onHighlightReady(int idx, int number) {
		final HighlightVagranter hData = getHData();
		ViewGroup webviewHolder = hData.webviewHolder;
		ViewGroup vg = hData.webviewHolder;
		View v = vg.getChildAt(idx);
		if(v!=null){
			v.setTag(R.id.numberpicker, number);
		}
		int all=0;
		for (int i = 0; i < vg.getChildCount(); i++) {
			all+=IU.parseInteger(vg.getChildAt(i).getTag(R.id.numberpicker),0);
		}
		String finalAll = all==0?"":""+all;
		MainPageSearchindicator.post(() -> MainPageSearchindicator.setText(finalAll));
		if (v != null && HiFiJumpRequested && idx == 0) {
			a.jumpNaughtyFirstHighlight(v.findViewById(R.id.webviewmy));
			HiFiJumpRequested = false;
		}
	}
	
	
	private HighlightVagranter hDataMergedPage = new HighlightVagranter(); // use merged view
	private HighlightVagranter hDataSinglePage = new HighlightVagranter(); // use dict view
	private HighlightVagranter hDataMultiple = new HighlightVagranter();  // use dict view, for multiple frames
	
	public WebViewmy dictView;
	
	/** <br>single merged - hDataMergedPage
	* <br> single dict - hDataPage in the wv
	* <br> multiple merged - hDataMergedPage
	* <br> multiple dict - hDataMultiple**/
	public HighlightVagranter getHData() {
		if(bMergingFrames) {
			hDataMergedPage.webviewHolder = contentUIData.webSingleholder;
			return hDataMergedPage;
		}
		if(isViewSingle()) {
			if(dictView==null) CMN.Log("ERROR_getHData!");
			HighlightVagranter data = dictView == null ? getMergedFrame().hDataPage : dictView.hDataPage;
			data.webviewHolder = contentUIData.webSingleholder;
			return data;
		} else {
			hDataMultiple.webviewHolder = contentUIData.webholder;
			return hDataMultiple;
		}
	}
	
	/** 汉星照耀，汉水长流！ */
	private void do_jumpHighlight(int d, boolean calcIndicator) {
		final HighlightVagranter hData = getHData();
		CMN.Log("jumpHighlight... dir="+d+" framePos="+hData.HlightIdx);
		ViewGroup webviewHolder = hData.webviewHolder;
		int max = webviewHolder.getChildCount();
		a.fadeSnack();
		boolean b1=hData.HlightIdx>=max,b2=hData.HlightIdx<0;
		if(b1||b2) {
			hData.AcrArivAcc++;
			if(b1&&d==-1) {
				hData.HlightIdx=max-1;
				b1=false;
			}
			else if(b2&&d==1){
				hData.HlightIdx=0;
				b2=false;
			}
			if(hData.AcrArivAcc<=2){
				//CMN.Log("do_jumpHighlight", PDICMainAppOptions.getInPageSearchShowNoNoMatch(), calcIndicator);
				if(PDICMainAppOptions.getInPageSearchShowNoNoMatch() || calcIndicator) {
					String msg = getResources().getString(R.string.search_end, d < 0 ? "⬆" : "", d > 0 ? "⬇" : "");
					a.getTopSnackView().setNextOffsetScale(0.25f);
					a.showTopSnack(null, msg, 0.75f, -1, Gravity.CENTER, 0);
				}
				return;
			} else {
				hData.AcrArivAcc=0;
			}
		} else {
			hData.AcrArivAcc =0;
		}
		if(b1){
			resetLights(webviewHolder, d);
			hData.HlightIdx=0;
			if(d==-1){
				a.evalJsAtFrame(max,"setAsEndLight("+d+");");
			}
		}
		else if(b2){
			resetLights(webviewHolder, d);
			hData.HlightIdx=max-1;
			if(d==1){
				if(hData.HlightIdx>=0) a.evalJsAtFrame(0,"setAsStartLight("+d+");");
			}
		}
		if(hData.HlightIdx<0) hData.HlightIdx=0;
		ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(hData.HlightIdx);
		if(webHolder!=null){
			WebView wv = (WebView) ViewUtils.findViewById(webHolder, R.id.webviewmy);
			if(wv!=null){
				if(jumper!=null && jumper!=wv){
					jumper.evaluateJavascript("quenchLight()",null);
				}
				jumper=wv;
				if(cc>0) inlineJump=false;
				CMN.Log("jumpHighlight_evaluating...", inlineJump);
				jumper.evaluateJavascript(new StringBuilder(28).append("jumpTo(")
						.append(d).append(',')//direction
						.append(-1).append(',')//desired offset
						.append(0).append(',')//frameAt
						.append(0).append(',')//HlightIdx
						.append(cc>0).append(',')//need reset
						.append(0)//topOffset_frameAt
						.append(");").toString(), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						CMN.Log("jumpHighlight_delta_yield : ", value);
						if(value!=null) {
							int d = 0; boolean b1;
							if(!(b1=value.startsWith("\"")))
								d = IU.parsint(value, 0);
							if (d != 0) {
								hData.HlightIdx += d;
								if (hData.HlightIdx < 0 || hData.HlightIdx >= max) {
									hData.AcrArivAcc++;
								}
								do_jumpHighlight(d, calcIndicator);
							}
							else if(calcIndicator && b1 && webviewHolder!=null) {
								int all=0;
								int preAll=IU.parsint(value.substring(1,value.length()-1),0);
								if(preAll>=0) {
									for (int i = 0; i < webviewHolder.getChildCount(); i++) {
										View v = webviewHolder.getChildAt(i);
										if (v != null) {
											if (i == hData.HlightIdx)
												preAll += all;
											all += IU.parseInteger(v.getTag(R.id.numberpicker), 0);
										}
									}
									//111 (PeruseSearchAttached()? peruseView.PerusePageSearchindicator:MainPageSearchindicator).setText((preAll+1)+"/"+all);
								}
							}
						}
					}
				});
				cc++;
			}
		}
	}
	
	void toggleInPageSearch(boolean isLongClicked) {
		final HighlightVagranter hData = getHData();
		hData.HlightIdx =
		hData.AcrArivAcc = 0;
		ViewGroup webviewHolder = hData.webviewHolder;
		if(isLongClicked){
			a.launchSettings(7, 0);
		}
		else {
			Toolbar searchbar = MainPageSearchbar;
			if (searchbar == null) {
				searchbar = MainPageSearchbar = (Toolbar) a.getLayoutInflater().inflate(R.layout.searchbar, null);
				searchbar.setNavigationIcon(R.drawable.abc_ic_clear_material);//abc_ic_ab_back_mtrl_am_alpha
				EditText etSearch = searchbar.findViewById(R.id.etSearch);
				//etSearch.setBackgroundColor(Color.TRANSPARENT);
				searchbar.setNavigationOnClickListener(v1 -> {
					toggleInPageSearch(false);
					if (etSearch.hasFocus())
						a.imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					a.fadeSnack();
				});
				etSearch.setText(MainPageSearchetSearchStartWord);
				etSearch.addTextChangedListener(new TextWatcher() {
					@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
					@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
					@Override
					public void afterTextChanged(Editable s) {
						String text = etSearch.getText().toString().replace("\\", "\\\\");
						HiFiJumpRequested=PDICMainAppOptions.getPageAutoScrollOnType();
						SearchInPage(text);
					}
				});
				
				ViewUtils.ResizeNavigationIcon(searchbar);
				
				searchbar.setContentInsetsAbsolute(0, 0);
				searchbar.setLayoutParams(a.toolbar.getLayoutParams());
				searchbar.setBackgroundColor(a.AppWhite==Color.WHITE?a.MainBackground:Color.BLACK);
				searchbar.findViewById(R.id.ivDeleteText).setOnClickListener(this);
				View.OnDragListener searchbar_stl = (v, event) -> {
					if(event.getAction()== DragEvent.ACTION_DROP){
						ClipData textdata = event.getClipData();
						if(textdata.getItemCount()>0){
							if(textdata.getItemAt(0).getText()!=null)
								etSearch.setText(textdata.getItemAt(0).getText());
						}
						return false;
					}
					return true;
				};
				this.MainPageSearchbar = searchbar;
				this.MainPageSearchetSearch = etSearch;
				this.MainPageSearchindicator = searchbar.findViewById(R.id.indicator);
				this.MainPageSearchDictionaryIndi = searchbar.findViewById(R.id.dictName);
				searchbar.getChildAt(0).addOnLayoutChangeListener(new OnLayoutChangeListener() {
					@Override
					public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
						MainPageSearchDictionaryIndi.setTranslationX(etSearch.getWidth()+GlobalOptions.density*2);
					}
				});
				View viewTmp=searchbar.findViewById(R.id.recess);
				viewTmp.setOnDragListener(searchbar_stl);
				viewTmp.setOnClickListener(this);
				viewTmp=searchbar.findViewById(R.id.forward);
				viewTmp.setOnDragListener(searchbar_stl);
				viewTmp.setOnClickListener(this);
			}
			boolean b1=searchbar.getParent()==null;
			if (b1) {
				contentviewAddView(searchbar, 0);
				searchbar.findViewById(R.id.etSearch).requestFocus();
				searchbar.setTag(MainPageSearchetSearch.getText());
				SearchInPage(null);
			}
			else {
				ViewUtils.removeView(searchbar);
				clearLights(webviewHolder);
				searchbar.setTag(null);
			}
			
			if(a.thisActType==MainActivityUIBase.ActType.PlainDict)
				a.opt.setInPageSearchVisible(b1);
			else{
				PDICMainAppOptions.setInFloatPageSearchVisible(b1);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		int id=v.getId();
		switch (id) {
			case R.id.ivDeleteText:
				MainPageSearchetSearch.setText(null);
				break;
			//in page search navigation
			case R.id.recess:
			case R.id.forward:{
				if(v.getTag()!=null){
					return;
				}
				boolean next=id==R.id.recess;
				//CMN.Log("下一个");
				//111
				if(PDICMainAppOptions.getInPageSearchAutoHideKeyboard()){
					//a.imm.hideSoftInputFromWindow((a.PeruseSearchAttached()? a.peruseView.PerusePageSearchetSearch:MainPageSearchetSearch).getWindowToken(), 0);
					a.imm.hideSoftInputFromWindow(MainPageSearchetSearch.getWindowToken(), 0);
				}
				jumpHighlight(next?1:-1, true);
			} break;
		}
	}
	
	void SearchInPage(String text) {
		final HighlightVagranter hData = getHData();
		ViewGroup webviewHolder = hData.webviewHolder;
		{
			if(text!=null)
				try {
					text=true?text: URLEncoder.encode(text,"utf8");
				} catch (UnsupportedEncodingException ignored) { }
			String val = text==null?"_highlight(null)":"_highlight(\""+ text.replace("\"","\\\"")+"\")";
			if(webviewHolder!=null){
				int cc = webviewHolder.getChildCount();
				for (int i = 0; i < cc; i++) {
					if(webviewHolder.getChildAt(i) instanceof LinearLayout){
						ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(i);
						if(webHolder.getChildAt(1) instanceof WebView){
							((WebView)webHolder.getChildAt(1))
									.evaluateJavascript(val,null);
						}
					}
				}
			}
//			if(popupContentView!=null && popupContentView.getParent()!=null){
//				popupWebView.evaluateJavascript(val,null);
//			}//111
		}
	}
	
	public void scrollHighlight(int o, int d) {
		final HighlightVagranter hData = getHData();
		ViewGroup webviewHolder = hData.webviewHolder;
		//CMN.Log("scrollHighlight",o,d,inlineJump);
		if(webviewHolder!=null && webviewHolder.getChildAt(hData.HlightIdx) instanceof LinearLayout){
			ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(hData.HlightIdx);
			WebView wv = (WebView) ViewUtils.findViewById(webHolder, R.id.webviewmy);
			if(wv!=null){
				int pad=(int) (25*a.dm.density);
				if(!isViewSingle()){
					//CMN.Log("???");
					WHP.performLongClick();
					WHP.onTouchEvent(MotionEvent.obtain( 1000,/*小*/
							1000,/*样，*/
							MotionEvent.ACTION_UP,/*我还*/
							0,/*治*/
							0,/*不了*/
							0));/*你？*/
					if(o==-1){
						if(d==-1 || inlineJump) {
							return;
						}
					}
					o=(int)(o*a.dm.density);
					o+=webHolder.getTop()+wv.getTop();
					//CMN.Log("??????", o);
					if(o<=WHP.getScrollY() || o+pad>=WHP.getScrollY()+WHP.getHeight()){
						//CMN.Log("do_scrollHighlight",o,d,o-pad);
						WHP.smoothScrollTo(0, o-pad);
					}
				}
				else{
					if(o==-1){
						if(d==-1 || inlineJump) {
							return;
						}
					}
					o=(int)(o*a.dm.density);
					if(o<=wv.getScrollY() || o+pad>=wv.getScrollY()+wv.getHeight()){
						int finalO=o-pad;
						//CMN.Log("scrolling !!!", finalO, wv.getScrollY(), wv.getScrollY()+wv.getHeight());
						wv.post(() -> {
							//CMN.Log("do scrolling !!!");
							MainActivityUIBase.layoutScrollDisabled=false;
							wv.scrollTo(0, finalO);
							wv.requestLayout();
							a.NaugtyWeb=(WebViewmy) wv;
							if(a.hdl!=null)
								a.hdl.sendEmptyMessage(778899);
						});
					}
				}
			}
		}
	}
	
	
	public void contentviewAddView(View v, int i) {
		ViewUtils.addViewToParent(v, a.contentview, i);
	}
}
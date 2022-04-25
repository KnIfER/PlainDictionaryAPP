package com.knziha.plod.plaindict;

import static com.knziha.plod.PlainUI.AppUIProject.ContentbarBtnIcons;
import static com.knziha.plod.PlainUI.AppUIProject.RebuildBottombarIcons;
import static com.knziha.plod.dictionary.Utils.IU.NumberToText_SIXTWO_LE;
import static com.knziha.plod.dictionarymodels.BookPresenter.baseUrl;
import static com.knziha.plod.plaindict.CMN.EmptyRef;
import static com.knziha.plod.preference.SettingsPanel.makeInt;

import android.content.ClipData;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;

import com.knziha.plod.PlainUI.AlloydPanel;
import com.knziha.plod.PlainUI.AppUIProject;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.Utils.Bag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowCheckedTextView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.Framer;
import com.knziha.plod.widgets.RLContainerSlider;
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

/** 页面管理器，曾经尝试过在Listview中放置webview，太卡。现在保留的模式：<br/>
 * 一个webview，显示一本或多本词典内容（合并的多页面模式）。 <br/>
 * 多个webview，放在LinearLayout中，显示多本词典内容。（多页面视图列表）*/
public class WebViewListHandler extends ViewGroup implements View.OnClickListener {
	final MainActivityUIBase a;
	/** Search-On-Page Control Flag. 网页设置标志位 指示是否开启点击翻译、页内搜索设置 */
	public int shezhi;
	/** Flag Backup. 设置备份，同于比对变化。 */
	int szStash;
	/** see{@link com.knziha.plod.db.SearchUI }*/
	int src;
	/** -2=auto;0=false;1=true*/
	public int bMergeFrames = 0;
	/** for {@link com.knziha.plod.PlainUI.WordPopup } */
	public boolean bDataOnly = false;
	public boolean bShowInPopup = false;
	public boolean bMergingFrames = false;
	public boolean bShowingInPopup = false;
	public ScrollViewmy WHP;
	public PDICMainAppOptions opt;
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
	Runnable UpdateBookLabelAbility;
	BookPresenter lastScrolledBook;
	public WebViewmy scrollFocus;
	WebViewmy mWebView;
	public boolean tapSch;
	
	public View browserWidget8;
	public View browserWidget10;
	public View browserWidget11;
	public RLContainerSlider pageSlider;
	public DragScrollBar mBar;
	
	private boolean bottomNavWeb;
	public View toolsBtn;
	
	public WebViewListHandler(@NonNull MainActivityUIBase a, @NonNull ContentviewBinding contentUIData, int src) {
		super(a);
		this.a = a;
		this.opt = a.opt;
		setId(R.id.webholder);
		//setUseListView(true);
		this.contentUIData = contentUIData;
		this.WHP = contentUIData.WHP;
		this.webholder = contentUIData.webholder;
		this.src = src;
		hDataSinglePage.webviewHolder = contentUIData.webSingleholder;
		hDataMultiple.webviewHolder = contentUIData.webholder;
		if(WHP.getScrollViewListener()==null) {
			/** 这里绑定自己到底栏，以获取上下文 see{@link MainActivityUIBase#showScrollSet} */
			contentUIData.bottombar2.setTag(this);
			contentUIData.PageSlider.weblist = this;
			contentUIData.cover.weblist = this;
			contentUIData.cover.hdl = a.hdl;
			browserWidget8 = contentUIData.browserWidget8;
			browserWidget10 = contentUIData.browserWidget10;
			browserWidget11 = contentUIData.browserWidget11;
			pageSlider = contentUIData.PageSlider;
			DragScrollBar mBar = this.mBar = contentUIData.dragScrollBar;
			toolsBtn = contentUIData.tools;
			toolsBtn.setOnClickListener(this);
			
			contentUIData.PageSlider.page = contentUIData.cover;
			contentUIData.cover.setPager(a.getPageListener());
			UpdateBookLabelAbility = ()->{
				String name = scrollFocus.presenter.getDictionaryName();
				int idx=name.lastIndexOf(".");
				if(idx>=0)name = name.substring(0, idx);
				contentUIData.dictName.setText(name);
				contentUIData.dictNameStroke.setText(name);
			};
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
								mBar.progress(y);
							}
						}
						if(!bMergingFrames && Math.abs(lastScrollUpdateY-y)>GlobalOptions.density*50) {
							lastScrollUpdateY=y;
							CMN.pt("滚动="+Math.abs(oldY)+","+"y="+y+"::");
							//CMN.rt();
							int bot=WHP.getScrollY() + WHP.getHeight()/2;
							for (int i = 0; i < webholder.getChildCount(); i++) {
								if(webholder.getChildAt(i).getBottom()>=bot) {
									WebViewmy wv = (WebViewmy)webholder.getChildAt(i).getTag();
									if(lastScrolledBook !=wv.presenter) {
										setScrollFocus(wv);
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
		CMN.Log("shzh::ini::", tapSch, BookPresenter.MakePageFlag(this, opt));
		tapSch = src==SearchUI.Fye.MAIN?PDICMainAppOptions.fyeTapSch():opt.tapSch();
		shezhi = BookPresenter.MakePageFlag(this, opt);
	}
	
	public void setScrollFocus(WebViewmy wv) {
		if(lastScrolledBook != wv.presenter) {
			lastScrolledBook = wv.presenter;
			a.root.postOnAnimationDelayed(UpdateBookLabelAbility, 50);
		}
		scrollFocus = wv;
	}
	
	public ViewGroup getViewGroup() {
		return mViewMode==WEB_LIST_MULTI?webholder:contentUIData.webSingleholder;
	}
	
	public ViewGroup getDragView() {
		return bDataOnly?dictView:mViewMode==WEB_LIST_MULTI?WHP:contentUIData.webSingleholder;
	}
	
	public View getChildAt(int frameAt) {
		return contentUIData==null?null:contentUIData.webholder.getChildAt(frameAt);
	}
	
	@Override
	public int getChildCount() {
		return contentUIData==null?0:contentUIData.webholder.getChildCount();
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
		contentUIData.dragScrollBar.progress(WHP.getScrollY());
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
		contentUIData.PageSlider.setWebview(null, this);
	}
	
	public void NotifyScrollingTo(resultRecorderCombined recom) {
		WHP.touchFlag.first=false;
		recom.LHGEIGHT=WHP.getHeight();
		webholder.removeOnLayoutChangeListener(OLCL); // todo save this step ???
		ViewUtils.addOnLayoutChangeListener(webholder, OLCL);
	}
	
	int shFlag;
	public int getScrollHandType() {
		return opt.getTypeFlag_11_AtQF(shFlag);
	}
	
	public void setScrollHandType(int style) {
		opt.setTypeFlag_11_AtQF(style, shFlag);
		resetScrollbar(mWebView, bMergingFrames, true);
	}
	
	
	public final void resetScrollbar(){
		resetScrollbar(dictView, false, false);
	}
	
	/**  0=在右; 1=在左; 2=无; 3=系统滚动条  */
	public void resetScrollbar(WebViewmy mWebView, boolean merged, boolean resetMerge){
		int vis = View.VISIBLE;
		boolean vsi = false;
		int gravity = 0;
		int type = a.thisActType==MainActivityUIBase.ActType.FloatSearch?2 //浮动搜索
				:(a.PeruseSearchAttached() && mWebView==a.peruseView.mWebView)?4 // 翻阅
				:(mWebView!=null&& mWebView==a.wordPopup.mWebView)?6 // 点译
				:0 // 主程序
				;
		shFlag=type;
		// 主程序 浮动搜索 点译
		if(merged) {
			vis=View.GONE;
			vsi=false;
			if (resetMerge && mWebView!=null) {
				mWebView.evaluateJavascript("SH_S("+opt.getTypeFlag_11_AtQF(shFlag)+")", null);
			}
		}
		else {
			final int sty=opt.getTypeFlag_11_AtQF(type);
			switch (sty){
				case 0:
					gravity=Gravity.END;
					break;
				case 1:
					gravity=Gravity.START;
					break;
				case 2:
					vis=View.GONE;
					break;
				case 3:
					vis=View.GONE;
					vsi=true;
					break;
			}
		}
		CMN.Log("resetScrollbar", mWebView!=null, vis, vsi);
		DragScrollBar mBar = this.mBar;
		if(mBar.getVisibility()!=vis)
			mBar.setVisibility(vis);
		if(vis==View.VISIBLE) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mBar.getLayoutParams();
			if(gravity!=0 && lp.gravity!=gravity){
				lp.gravity=gravity;
				mBar.requestLayout();
			}
			mBar.setHandleColorFiler(a.MainAppBackground);
			if(mWebView!=null) {
				mBar.setDelimiter("", mWebView);
			} else {
				mBar.setDelimiter(isViewSingle()?null:"|||", isViewSingle()?mMergedFrame:contentUIData.WHP);
			}
		}
		(mWebView==null?contentUIData.WHP:mWebView).setVerticalScrollBarEnabled(vsi);
		a.weblist = this;
		this.mWebView = mWebView;
	}
	
	public BookPresenter getMergedBook() {
		if(mMergedBook==null) {
			try {
				mMergedBook = new BookPresenter(new File("empty"), null, 1);
				mMergedBook.isMergedBook(true);
				mMergedBook.opt = a.opt;
			} catch (IOException ignored) { }
		}
		return mMergedBook;
	}
	
	public WebViewmy getMergedFrame(BookPresenter book) {
		getMergedBook().getWebBridge().setBook(book);
		getMergedFrame().setPresenter(book);
		return mMergedFrame;
	}
	
	public WebViewmy getMergedFrame() {
		if(mMergedFrame==null) {
			getMergedBook().initViewsHolder(a);
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
			//mMergedBook.toolbar.setVisibility(View.GONE);
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
		if(isMultiRecord())
		if(bMergingFrames) {
			mMergedFrame.evaluateJavascript(nxt?"prvnxtFrame(1)":"prvnxtFrame()", null);
		} else if(!isViewSingle()){
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
				a.scrollToPageBottom(contentUIData.webholder.getChildAt(cc-1));
			} else {
				a.scrollToWebChild(contentUIData.webholder.getChildAt(childAtIdx));
			}
		}
	}
	
	int frameCount;
	int frameSelection;
	int frameAt;
	WeakReference<AlertDialog> jumpListDlgRef = EmptyRef;
	AlertDialog jumpListDlg;
//	static class TextConfig{
//		int[] ids;
//		float tsz;
//	}
//	TextConfig tf;
	
	
	public void JumpToFrame(int pos) {
		frameSelection = pos;
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
	}
	
	public void showJumpListDialog() {
		if(!bMergingFrames) frameAt = getFrameAt();
		frameCount = frames.size();
		if(jumpListDlg==null){
			jumpListDlg = jumpListDlgRef.get();
		}
		AlertDialog dTmp = jumpListDlg;
		Bag bag;
		if(dTmp==null){
			bag = new Bag(PDICMainAppOptions.getTwoColumnSetView());
			DialogInterface.OnClickListener listener = (dlg, pos) -> {
				if(pos==-1) {
					opt.setTwoColumnJumpList(bag.val=!bag.val);
					((BaseAdapter)bag.tag).notifyDataSetChanged();
					jumpListDlg.getListView().setSelection(bag.val?frameSelection/2:frameSelection);
				}
				else {
					JumpToFrame(pos);
					dlg.dismiss();
				}
			};
			PorterDuffColorFilter cf = new PorterDuffColorFilter(0xFF2196f3, PorterDuff.Mode.SRC_IN);
			dTmp = new AlertDialog.Builder(a/*,R.style.DialogStyle*/)
				.setTitle("跳转")
				.setTitleBtn(R.drawable.ic_two_column, listener)
				.setAdapter((BaseAdapter)(bag.tag=new BaseAdapter() {
						@Override public int getCount() { return bag.val?(int)Math.ceil(frameCount/2.f):frameCount; }
						@Override public Object getItem(int position) { return null; }
						@Override public long getItemId(int pos) { return pos<frames.size()?frames.get(pos).getId():-1;}
						@Override public int getViewTypeCount() { return 2; }
						@Override public int getItemViewType(int position) {
							return bag.val?1:0;
						}
						final View.OnClickListener twoColumnLis = new OnClickListener() {
							public void onClick(View v) {
								ViewGroup sp=(ViewGroup)v.getParent();
								listener.onClick(jumpListDlg, IU.parsint(sp.getTag(), 0)*2+sp.indexOfChild(v));
							}
						};
						@NonNull @Override
						public View getView(int position, View convertView, @NonNull ViewGroup parent) {
							if(bag.val) {
								ViewGroup ret;
								if(convertView!=null){
									ret = (ViewGroup) convertView;
								} else {
									ret = (ViewGroup) a.getLayoutInflater().inflate(R.layout.singlechoice_two_column, parent, false);
								}
								for (int i = 2; i < ret.getChildCount(); i++) {
									FlowCheckedTextView mFlowTextView = (FlowCheckedTextView) ret.getChildAt(i);
									FlowTextView tv = mFlowTextView.mFlowTextView;
									if(tv.getTag()==null)
									{
										ret.getChildAt(i-2).setOnClickListener(twoColumnLis);
										tv.setText("");
									}
									int pos = position*2+i-2;
									BookPresenter book = a.getBookById(getItemId(pos));
									tv.setCompoundDrawables(a.getActiveStarDrawable(), null, null, null);
									tv.setCover(book.getCover());
									tv.setTextColor(a.AppBlack);
									tv.setStarLevel(PDICMainAppOptions.getDFFStarLevel(book.getFirstFlag()));
									mFlowTextView.setChecked(pos==frameSelection);
									mFlowTextView.setText(book==a.EmptyBook?"":book.getDictionaryName());
									boolean b1=pos==frameAt;
									if(b1 ^ mFlowTextView.getTag()!=null) {
										mFlowTextView.setTag(b1?"":null);
										if(b1) {
											mFlowTextView.setBackgroundResource(R.drawable.text_underline);
											mFlowTextView.getBackground().setColorFilter(cf);
										} else {
											mFlowTextView.setBackground(null);
										}
									}
								}
								ret.setActivated(false);
								ret.postInvalidateOnAnimation();
								ret.setTag(position);
								convertView = ret;
							}
							else {
								FlowCheckedTextView ret;
								if(convertView!=null){
									ret = (FlowCheckedTextView) convertView;
								} else {
									ret = (FlowCheckedTextView) a.getLayoutInflater().inflate(R.layout.singlechoice_w, parent, false);
								}
								BookPresenter book = a.getBookById(getItemId(position));
								if (book!=a.EmptyBook) {
									FlowTextView tv = ret.mFlowTextView;
									tv.setCompoundDrawables(a.getActiveStarDrawable(), null, null, null);
									tv.setCover(book.getCover());
									tv.setTextColor(a.AppBlack);
									tv.setStarLevel(PDICMainAppOptions.getDFFStarLevel(book.getFirstFlag()));
									ret.setChecked(position==frameSelection);
									tv.setMaxLines(1); //todo opt
									ret.setText(book.getDictionaryName());
								} else {
									ret.setText("Error!!!");
								}
								boolean b1=position==frameAt;
								if(b1 ^ ret.getTag()!=null) {
									ret.setTag(b1?"":null);
									if(b1) {
										ret.setBackgroundResource(R.drawable.text_underline);
										ret.getBackground().setColorFilter(cf);
									} else {
										ret.setBackground(null);
									}
								}
								convertView = ret;
							}
							return convertView;
						}
					})
				, listener)
				.show();
			dTmp.setCanceledOnTouchOutside(true);
			//dTmp.mAlert.wikiBtn.getLayoutParams().width = ((int)GlobalOptions.density*50);
			
			ListView dlv = dTmp.getListView();
			dTmp.show();
			dlv.setChoiceMode(ListView.CHOICE_MODE_NONE);
			
			Window window = dTmp.getWindow();
			window.setDimAmount(0);
			
			jumpListDlgRef = new WeakReference<>(jumpListDlg=dTmp);
			if(GlobalOptions.isDark) {
				dTmp.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			}
			dTmp.tag = bag;
		}
		else {
			bag = (Bag)dTmp.tag;
			dTmp.show();
		}
		if(!GlobalOptions.isLarge) {
			dTmp.getWindow().setLayout((int) (a.dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor)), -2);
		}
		ListView lv = dTmp.getListView();
		((AlertController.RecycleListView) lv)
				.mMaxHeight = (int) (a.root.getHeight() - a.root.getPaddingTop() - 2.8 * getResources().getDimension(R.dimen._50_) * (a.dm.widthPixels>GlobalOptions.realWidth?1:1.45));
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
								if(frameSelection==-1 || frameSelection>=frameCount) { //首次显示自动跳转
									frameSelection = pos;
									jumpListDlg.getListView().setSelection(bag.val?pos/2:pos);
									((BaseAdapter)jumpListDlg.getListView().getAdapter()).notifyDataSetChanged();
								}
							}
						}
					}
					//CMN.debug(value, frameAt);
				} catch (Exception e) {
					CMN.debug(e);
				}
			});
		}
		if(frameSelection==-1 || frameSelection>=frameCount) {
			//首次显示自动跳转
			frameSelection = frameAt;
			lv.setSelection(bag.val?frameAt/2:frameAt);
		}
	}
	
	public final static int WEB_LIST_MULTI=0;
	public final static int WEB_VIEW_SINGLE=1;
	int mViewMode;
	boolean isMultiRecord;
	public resultRecorderCombined multiRecord;
	public void setViewMode(resultRecorderCombined record, boolean bUseMergedUrl, WebViewmy dictView) {
		boolean multi=record!=null;
		if (multiRecord!=record) {
			multiRecord = record;
		}
		int viewMode = multi && !bUseMergedUrl? WEB_LIST_MULTI : WEB_VIEW_SINGLE;
		if(mViewMode!=viewMode || bMergingFrames!=bUseMergedUrl || isMultiRecord!=multi) {
			isMultiRecord = multi;
			mViewMode = viewMode;
			if (!bDataOnly) {
				ViewUtils.setVisible(contentUIData.navBtns, multi && !bUseMergedUrl);
				ViewUtils.setVisible(contentUIData.dictNameStroke, !bUseMergedUrl);
				ViewUtils.setVisible(contentUIData.dictName, !bUseMergedUrl);
			}
		}
		if(this.dictView!=dictView) {
			this.dictView = dictView;
		}
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
			alloydPanel = new AlloydPanel(a, this);
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
			contentUIData.browserWidget13.setOnClickListener(this);
			contentUIData.browserWidget14.setOnClickListener(this);
			
			PorterDuffColorFilter phaedrof = new PorterDuffColorFilter(0xff888888, PorterDuff.Mode.SRC_IN);
			contentUIData.prv.setColorFilter(phaedrof);
			contentUIData.nxt.setColorFilter(phaedrof);
			
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
			String appproject = opt.getAppContentBarProject(contentkey);
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
	
	/** false:using old list technique. displaying multiple webviews in the linearlayout */
	public final boolean isViewSingle() {
		return mViewMode==WEB_VIEW_SINGLE;
	}
	
	/** displaying records from multiple dictionary in search-all mode */
	public final boolean isMultiRecord() {
		return isMultiRecord;
	}
	
	int cc;
	boolean inlineJump;
	private WebView jumper;
	
	public Toolbar pageSchBar;
	public EditText pageSchEdit;
	TextWatcher pageSchWat;
	int etFlag;
	TextView pageSchIndicator;
	TextView pageSchDictIndicator;
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
		if(!TextUtils.equals((String) pageSchIndicator.getTag(),did)) {
			dictName = new File(a.getBookNameByIdNoCreation(IU.TextToNumber_SIXTWO_LE(new CharSequenceKey(did, 1)))).getName();
			pageSchIndicator.setTag(did);
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
		pageSchIndicator.post(() -> {
			pageSchIndicator.setText(text);
			if(dictName1!=null) pageSchDictIndicator.setText(dictName1);
		});
	}
	
	public boolean hasPageKey() {
		return ViewUtils.isVisibleV2(pageSchBar) && TextUtils.getTrimmedLength(pageSchEdit.getText())>0;
	}
	
	/** 页面加载完成后，是否自动开始执行页内搜索，并跳转至第一处高亮。 */
	public boolean schPage(WebViewmy mWebView) {
		return hasPageKey() && (isViewSingle() || mWebView==webholder.getChildAt(0));
	}
	
	public void textMenu(WebViewmy mWebView) {
		if (mWebView==null || (bDataOnly?PDICMainAppOptions.tapSchShowToolsBtn():PDICMainAppOptions.wvShowToolsBtn())) {
			toolsBtn.setTag(mWebView);
			ViewUtils.setVisible(toolsBtn, mWebView!=null);
		}
	}
	
	/** show this hide another */
	public void viewContent() {
		//CMN.Log("viewContent::", mViewMode);
		int mode = mViewMode;
		mViewMode = (mViewMode+1)%2;
		if(ViewUtils.isVisible(this)) {
			setVisibility(View.GONE);
			// removeAllViews();
		}
		mViewMode = mode;
		if(!ViewUtils.isVisible(this)) {
			setVisibility(View.VISIBLE);
		}
	}
	
	public boolean togTapSch() {
		tapSch = !tapSch;
		evalJsAtAllFrames(tapSch?
				"window.shzh|=1;if(!window.tpshc)loadJs('/mdbr/tapSch.js')"
				:"window.shzh&=~1");
		return tapSch;
	}
	
	private void evalJsAtAllFrames(String exp) {
		evalJsAtAllFrames_internal(contentUIData.webSingleholder, exp);
		evalJsAtAllFrames_internal(webholder, exp);
//		if(peruseView !=null && peruseView.mWebView!=null){
//			peruseView.mWebView.evaluateJavascript(exp,null);
//		}
	}
	
	private void evalJsAtAllFrames_internal(ViewGroup vg, String exp) {
		for (int index = 0; index < vg.getChildCount(); index++) {
			if(vg.getChildAt(index) instanceof LinearLayout){
				ViewGroup webHolder = (ViewGroup) vg.getChildAt(index);
				if(webHolder.getChildAt(1) instanceof WebView){
					((WebView)webHolder.getChildAt(1))
							.evaluateJavascript(exp,null);
				}
			}
		}
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
		pageSchIndicator.post(() -> pageSchIndicator.setText(finalAll));
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
				if(PDICMainAppOptions.schPageShowHints() || calcIndicator) {
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
	
	void togSchPage() {
		final HighlightVagranter hData = getHData();
		hData.HlightIdx =
		hData.AcrArivAcc = 0;
		ViewGroup webviewHolder = hData.webviewHolder;
		Toolbar bar = pageSchBar;
		if (bar == null) {
			bar = (Toolbar) a.getLayoutInflater().inflate(R.layout.searchbar, null);
			bar.setNavigationIcon(R.drawable.abc_ic_clear_material);//abc_ic_ab_back_mtrl_am_alpha
			EditText etSearch = bar.findViewById(R.id.etSearch);
			//etSearch.setBackgroundColor(Color.TRANSPARENT);
			etSearch.setText(MainPageSearchetSearchStartWord);
			etSearch.addTextChangedListener(pageSchWat = new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
				@Override
				public void afterTextChanged(Editable s) {
					if (PDICMainAppOptions.schPageOnEdit() || s==null) {
						String text = etSearch.getText().toString().replace("\\", "\\\\");
						HiFiJumpRequested=PDICMainAppOptions.schPageAutoType();
						SearchOnPage(text);
					} else if(etFlag!=1){
						etFlag = 1;
					}
				}
			});
			etSearch.setOnEditorActionListener((v, actionId, event) -> {
				if (!PDICMainAppOptions.schPageOnEdit()) {
					pageSchWat.afterTextChanged(null);
				}
				return true;
			});
			
			ViewUtils.ResizeNavigationIcon(bar);
			
			bar.setContentInsetsAbsolute(0, 0);
			bar.setLayoutParams(a.toolbar.getLayoutParams());
			bar.setBackgroundColor(a.AppWhite==Color.WHITE?a.MainBackground:Color.BLACK);
			bar.findViewById(R.id.ivDeleteText).setOnClickListener(this);
			bar.findViewById(R.id.enter).setOnClickListener(this);
			this.pageSchBar = bar;
			this.pageSchEdit = etSearch;
			this.pageSchIndicator = bar.findViewById(R.id.indicator);
			this.pageSchDictIndicator = bar.findViewById(R.id.dictName);
			bar.getChildAt(0).addOnLayoutChangeListener(new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					pageSchDictIndicator.setTranslationX(etSearch.getWidth()+GlobalOptions.density*2);
				}
			});
			ViewUtils.setOnClickListenersOneDepth(bar, this, 999, null);
			bar.setOnDragListener( (v, event) -> {
				if(event.getAction()== DragEvent.ACTION_DROP){
					try {
						ClipData textdata = event.getClipData();
						if(textdata.getItemCount()>0){
							CharSequence text = textdata.getItemAt(0).getText();
							if(text!=null) {
								etSearch.setText(text);
								if(!PDICMainAppOptions.schPageOnEdit()) {
									pageSchWat.afterTextChanged(null);
								}
							}
						}
						return false;
					} catch (Exception e) { }
				}
				return true;
			});
		}
		boolean b1=bar.getParent()==null;
		if (b1) {
			contentviewAddView(bar, 0);
			bar.findViewById(R.id.etSearch).requestFocus();
			bar.setTag(pageSchEdit.getText());
			SearchOnPage(null);
		}
		else {
			ViewUtils.removeView(bar);
			clearLights(webviewHolder);
			bar.setTag(null);
		}
		
		if (src==SearchUI.MainApp.MAIN) {
			opt.schPage(b1);
		} else if (src==SearchUI.FloatApp.MAIN) {
			PDICMainAppOptions.schPageFlt(b1);
		} else if (src==SearchUI.Fye.MAIN) {
			opt.schPageFye(b1);
		}
	}
	
	// click
	@Override
	public void onClick(View v) {
		int id=v.getId();
		switch (id) {
			case R.id.home:
				togSchPage();
				a.imm.hideSoftInputFromWindow(pageSchEdit.getWindowToken(), 0);
				break;
			case R.id.ivDeleteText:
				pageSchEdit.setText(null);
				break;
			case R.id.enter:
				showPageSchTweaker();
				break;
			//in page search navigation
			case R.id.recess:
			case R.id.forward:{
				if(v.getTag()!=null){
					return;
				}
				if((etFlag&1)!=0) {
					pageSchWat.afterTextChanged(null);
					etFlag = 0;
				} else {
					boolean nxt=id==R.id.recess;
					//CMN.Log("下一个");
					//111
					if(PDICMainAppOptions.schPageNavHideKeyboard()){
						//a.imm.hideSoftInputFromWindow((a.PeruseSearchAttached()? a.peruseView.PerusePageSearchetSearch:MainPageSearchetSearch).getWindowToken(), 0);
						a.imm.hideSoftInputFromWindow(pageSchEdit.getWindowToken(), 0);
					}
					jumpHighlight(nxt?1:-1, true);
				}
			} break;
			/* 上下导航 */
			case R.id.browser_widget13:
			case R.id.browser_widget14:{
				prvnxtFrame(id == R.id.browser_widget13);
			} break;
			/* 工具 */
			case R.id.tools:{
				if (v.getTag() instanceof WebViewmy) {
					final WebViewmy wv = ((WebViewmy) v.getTag());
					wv.presenter.invokeToolsBtn(wv);
				}
			} break;
		}
	}
	
	// 页内搜索设定
	public void showPageSchTweaker() {
		a.weblist = this;
		szStash = shezhi;
		final SettingsPanel settings = new SettingsPanel(a, opt
				, new String[][]{
						new String[]{"搜索选项", "使用正则表达式", "使用通配符", "区分大小写", "以空格分割关键词", "通配符不匹配空格"}
						,new String[]{"视图设置", "打字时自动搜索", "翻页时自动跳转", "打字时自动跳转"/*, "使用音量键"*/}
					}
				, new int[][]{new int[]{Integer.MAX_VALUE /** see{@link BookPresenter#MakePageFlag} */
					, makeInt(101, 4, false) // pageSchUseRegex
					, makeInt(101, 8, false) // pageSchWild
					, makeInt(101, 5, false) // pageSchCaseSensitive
					, makeInt(101, 6, false) // pageSchSplitKeys
					, makeInt(101, 7, false) // pageSchWildMatchNoSpace
				}
				, new int[]{Integer.MAX_VALUE
					, makeInt(6, 27, true) // schPageOnEdit
					, makeInt(2, 55, false) // schPageAutoTurn
					, makeInt(2, 56, false) // schPageAutoType
					//, makeInt(2, 58, false) // schPageNavAudioKey
				}
		}, null);
		settings.init(a, a.root);
		settings.setActionListener(new SettingsPanel.ActionListener() {
			@Override
			public boolean onAction(SettingsPanel settingsPanel, int flagIdx, int flagPos, boolean dynamic, boolean val, int storageInt) {
				if (flagIdx==101) {
					if (flagPos==4) {
						for (int i = 6; i <= 8; i++)
							settings.settingsLayout.findViewById(makeInt(101, i, false)).setAlpha(val?0.5f:1);
					}
				}
				return true;
			}
			@Override
			public void onPickingDelegate(SettingsPanel settingsPanel, int flagIdx, int flagPos, int lastX, int lastY) {
			}
		});
		if ((shezhi&0x4)!=0) {
			settings.onAction(101, 4, false, true, 0);
		}
		Framer f = new Framer(a);
		f.mMaxHeight = (int) (1.25f*8*((RadioSwitchButton)settings.settingsLayout.findViewById(makeInt(101, 4, false))).getLineHeight()+15*GlobalOptions.density);
		f.addView(settings.settingsLayout);
		AlertDialog dlg =
				new AlertDialog.Builder(a,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line)
						.setTitle("页内搜索设置")
						.setView(f)
						.setPositiveButton(R.string.confirm, null)
//						.setTitleBtn(R.drawable.ic_search_large, (dialog, which) -> {
//							pageSchWat.afterTextChanged(null);
//							dialog.dismiss();
//						})
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								//CMN.debug("Flag::页内搜索设置::pageSchSplitKeys::", shezhi&0x40);
								if (szStash!=shezhi) {
									ViewGroup webviewHolder = getViewGroup();
									int cc = webviewHolder.getChildCount();
									String val = "window.shzh="+shezhi+"; _highlight(null);";
									BookPresenter.SavePageFlag(shezhi);
									for (int i = 0; i < cc; i++) {
										if(webviewHolder.getChildAt(i) instanceof LinearLayout){
											ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(i);
											if(webHolder.getChildAt(1) instanceof WebView){
												WebViewmy wv = (WebViewmy) webHolder.getChildAt(1);
												wv.evaluateJavascript(val,null);
											}
										}
									}
								}
							}
						})
						.create();
		
		dlg.show();
		
		dlg.getWindow().setDimAmount(0);
	}
	
	void SearchOnPage(String text) {
		final HighlightVagranter hData = getHData();
		ViewGroup vg = hData.webviewHolder;
		{
			if(text!=null)
				try {
					text=true?text: URLEncoder.encode(text,"utf8");
				} catch (UnsupportedEncodingException ignored) { }
			String val = text==null?"_highlight(null)":"_highlight(\""+ text.replace("\"","\\\"")+"\")";
			if(vg!=null){
				int cc = vg.getChildCount();
				for (int i = 0; i < cc; i++) {
					if(vg.getChildAt(i) instanceof LinearLayout){
						ViewGroup webHolder = (ViewGroup) vg.getChildAt(i);
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
		ViewUtils.addViewToParent(v, contentUIData.webcontentlister, i);
	}
	
	public final int getSrc() {
		return src;
	}
	
	public boolean isMergingFrames() {
		return bMergingFrames;
	}
	
	public void setStar(String key) {
		if (ViewUtils.isVisibleV2(browserWidget8)) {
			browserWidget8.setActivated(a.GetIsFavoriteTerm(key));
		}
	}
	
	public String getMultiRecordKey() {
		additiveMyCpr1 ret = null;
		if (multiRecord!=null) {
			if (isViewSingle()) {
				ret = getMergedFrame().jointResult;
			} else {
				ret = jointResult;
			}
		}
		return ret == null ? "Error!!!" : ret.key;
	}
	
	void setBottomNavWeb(boolean nav) {
		if (!bDataOnly && nav!=bottomNavWeb) {
			if (nav) {
				contentUIData.browserWidget10.setImageResource(R.drawable.chevron_recess);
				contentUIData.browserWidget11.setImageResource(R.drawable.chevron_forward);
			} else {
				contentUIData.browserWidget10.setImageResource(R.drawable.chevron_left);
				contentUIData.browserWidget11.setImageResource(R.drawable.chevron_right);
			}
			if (bShowingInPopup) {
				PDICMainAppOptions.bottomNavWeb(nav);
			}
			bottomNavWeb = nav;
			//if(tv!=null) tv.setText(getResources().getTextArray(R.array.btm_navmode)[type]);
		}
	}
	
	public final boolean getBottomNavWeb() {
		return bottomNavWeb;
	}
	
	void NavWeb(int d) {
		if (scrollFocus !=null) {
			if(String.valueOf(scrollFocus.getUrl()).startsWith(baseUrl)
				&& scrollFocus.forward!=null) {
				(d>0?scrollFocus.forward:scrollFocus.recess).performClick();
			} else {
				if (d>0) scrollFocus.goForward();
				else scrollFocus.goBack();
			}
		}
	}
	
	public final boolean bottomNavWeb() {
		return bottomNavWeb && pageSlider.page.decided==0;
	}
}
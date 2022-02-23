package com.knziha.plod.plaindict;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.knziha.plod.dictionary.Utils.IU.NumberToText_SIXTWO_LE;
import static com.knziha.plod.plaindict.CMN.EmptyRef;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowCheckedTextView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ListViewBasicViews;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WebViewListHandler extends ViewGroup {
	final MainActivityUIBase a;
	boolean bUseListView;
	public boolean bMergeFrames = true;
	ListViewmy mListView;
	ListViewBasicViews.BasicViewsAdapter mAdapter;
	ScrollViewmy WHP;
	FrameLayout WHP1;
	ViewGroup webholder;
	public WebViewmy mMergedFrame;
	BookPresenter mMergedBook;
	public ArrayList<Long> frames = new ArrayList();
	
	public void setUseListView(boolean use) {
		if(this.bUseListView = use) {
		} else {
		
		}
	}
	
	public WebViewListHandler(MainActivityUIBase a) {
		super(a);
		this.a = a;
		setId(R.id.webholder);
		//setUseListView(true);
	}
	
	public ViewGroup getViewGroup() {
		return bUseListView?mListView:webholder;
	}
	
	public View getChildAt(int frameAt) {
		if(bUseListView) {
			return (View) mAdapter.getItem(frameAt);
		}
		return webholder.getChildAt(frameAt);
	}
	
	@Override
	public int getChildCount() {
		return bUseListView?mAdapter.getCount():webholder.getChildCount();
	}
	
	public void shutUp() {
		if(WHP.getVisibility()==View.VISIBLE) {
			if(webholder.getChildCount()!=0)
				webholder.removeAllViews();
			WHP.setVisibility(View.GONE);
		}
	}
	
	public void init(ViewGroup whp, ViewGroup webholder) {
		this.WHP = (ScrollViewmy) whp;
		this.webholder = webholder;
		if(bUseListView) {
			if(mListView==null && whp.getParent()!=null) {
				mListView = new ListViewmy(WHP.getContext());
				mAdapter = new ListViewBasicViews.BasicViewsAdapter();
				mListView.setAdapter(mAdapter);
				ViewUtils.replaceView(mListView, WHP);
				CMN.Log("替换::", mListView.getParent(), WHP.getParent());
				
				mListView.setNestedScrollingEnabled(false);
//				mListView.setFastScrollAlwaysVisible(true);
//				mListView.setFastScrollEnabled(true);
				mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
					@Override
					public void onMovedToScrapHeap(View view) {
					
					}
				});
			}
		}
	}
	
	@Override
	public void addView(View child, int index) {
		if (bUseListView) {
			CMN.Log("添加::", index, mAdapter.mViews.size());
			if (index < 0) {
				index = mAdapter.getCount();
			}
			mAdapter.mViews.add(index, child);
			mAdapter.notifyDataSetChanged();
		} else {
			webholder.addView(child, index);
		}
	}
	
	public void removeAllViews() {
		if (bUseListView) {
			mAdapter.mViews.clear();
			mAdapter.notifyDataSetChanged();
		} else {
			webholder.removeAllViews();
		}
	}
	
	@Override
	public void setVisibility(int visibility) {
		(bUseListView?mListView:WHP).setVisibility(visibility);
	}
	
	@Override
	public int getVisibility() {
		return (bUseListView?mListView:WHP).getVisibility();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) { }
	
	public void setBackgroundColor(int manFt_globalPageBackground) {
		(bUseListView?mListView:WHP).setBackgroundColor(manFt_globalPageBackground);
	}
	
	public int getFrameAt() {
		if(!bMergeFrames) {
			if (bUseListView) {
				return mListView.getFirstVisiblePosition();
			}
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
		a.mBar.setMax(webholder.getMeasuredHeight()-WHP.getMeasuredHeight());
		a.mBar.setProgress(WHP.getScrollY());
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
						a.main_progress_bar.setVisibility(View.GONE);
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
								a.main_progress_bar.setVisibility(View.GONE);
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
			if(a.main_progress_bar!=null)
				a.main_progress_bar.setVisibility(recom.expectedPos==0?View.GONE:View.VISIBLE);
			recom.scrolled=false;
		}
		a.PageSlider.setIBC(null, this);
	}
	
	public void NotifyScrollingTo(resultRecorderCombined recom) {
		WHP.touchFlag.first=false;
		recom.LHGEIGHT=WHP.getHeight();
		webholder.removeOnLayoutChangeListener(OLCL); // todo save this step ???
		ViewUtils.addOnLayoutChangeListener(webholder, OLCL);
	}
	
	public void initWebHolderScrollChanged() {
		DragScrollBar mBar = a.mBar;
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
		mBar.setDelimiter("|||", bMergeFrames?mMergedFrame:getViewGroup());
	}
	
	public WebViewmy getMergedFrame() {
		if(mMergedFrame==null) {
			try {
				mMergedBook = new BookPresenter(new File("empty"), null, 1, 2);
			} catch (IOException ignored) { }
			mMergedBook.initViewsHolder(a);
			mMergedFrame = mMergedBook.mWebView;
			mMergedFrame.setWebViewClient(a.myWebClient);
			mMergedFrame.setWebChromeClient(a.myWebCClient);
			//mMergedFrame.setOnScrollChangedListener(null);
			//mMergedFrame.SetupScrollRect(true);
			mMergedFrame.getSettings().setTextZoom(BookPresenter.def_fontsize);
		}
		return mMergedFrame;
	}
	
	public WebViewmy initMergedFrame() {
		WebViewmy mMergedFrame = getMergedFrame();
		if(WHP1==null) WHP1 = new FrameLayout(a);
		if(WHP1.getParent()==null) {
			ViewUtils.replaceView(WHP1, WHP);
		}
		webholder.getLayoutParams().height = MATCH_PARENT;
		ViewUtils.addViewToParent(webholder, WHP1);
		ViewUtils.addViewToParent(mMergedBook.rl, webholder);
		if(webholder.getChildCount()>1)
			for (int i = webholder.getChildCount()-1; i>=0; i--)
				if(webholder.getChildAt(i)!=mMergedFrame)
					webholder.removeViewAt(i);
		mMergedFrame.getLayoutParams().height = MATCH_PARENT;
		mMergedBook.rl.getLayoutParams().height = MATCH_PARENT;
		mMergedBook.toolbar.setVisibility(View.GONE);
		a.widget13.setVisibility(View.GONE);
		a.widget14.setVisibility(View.GONE);
		return mMergedFrame;
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
		if(bMergeFrames) {
			mMergedFrame.evaluateJavascript(nxt?"prvnxtFrame(1)":"prvnxtFrame()", null);
		} else {
			final int currentHeight=WHP.getScrollY();
			int cc=webholder.getChildCount();
			int childAtIdx=cc;
			int top;
			for(int i=0;i<cc;i++) {
				top = webholder.getChildAt(i).getTop();
				if(top>=currentHeight){
					childAtIdx=i;
					if(nxt && top!=currentHeight) --childAtIdx;
					break;
				}
			}
			childAtIdx+=nxt?1:-1;
			if(childAtIdx>=cc){
				a.scrollToPagePosition(webholder.getChildAt(cc-1).getBottom());
			} else {
				a.scrollToWebChild(webholder.getChildAt(childAtIdx));
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
		if(!bMergeFrames) frameAt = getFrameAt();
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
									public long getItemId(int pos) { return pos<frames.size()?frames.get(pos):-1;}
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
								if(bMergeFrames) {
									if(pos<frames.size()) {
										BookPresenter book = a.getBookById(frames.get(pos));
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
		if(bMergeFrames) {
			mMergedFrame.evaluateJavascript("currentFrame("+frameAt+")", value -> {
				try {
					if(jumpListDlg!=null && jumpListDlg.isShowing()) {
						String[] arr=value.split("-", 2);
						if(arr.length==2) {
							long id = IU.TextToNumber_SIXTWO_LE(arr[0].substring(2));
							int pos = IU.parsint(arr[1], 0);
							//CMN.debug(value, pos, id);
							if(pos>=frames.size() || frames.get(pos)!=id) {
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
}
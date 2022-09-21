package com.knziha.plod.PlainUI;

import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.knziha.ankislicer.customviews.ShelfLinearLayout;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.NiceDrawerLayout;
import com.knziha.plod.widgets.NoScrollViewPager;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

public class BookNotes extends PlainAppPanel implements DrawerLayout.DrawerListener, PopupMenuHelper.PopupMenuListener {
	MainActivityUIBase a;
	NiceDrawerLayout drawer;
	int drawerStat;
	boolean drawerOpen;
	NoScrollViewPager viewPager;
	RecyclerView[] viewList;
	ViewGroup bar;
	ShelfLinearLayout btns;
	Toolbar toolbar;
	
	PopupMenuHelper popupMenu;
	
	public BookNotes(MainActivityUIBase a) {
		super(a, true);
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		this.bAnimate = false;
		setShowInDialog();
	}
	
	@Override
	public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
		//CMN.debug("onDrawerSlide::", slideOffset); // 0.085
		if (drawerOpen) {
			if (slideOffset < 0.01 && drawerStat==DrawerLayout.STATE_SETTLING) {
				dismissImmediate();
			}
		} else if (slideOffset > 0.01 && drawerStat==DrawerLayout.STATE_SETTLING) {
			drawerOpen = true;
		}
	}
	
	@Override
	public void onDrawerOpened(@NonNull View drawerView) {
		drawerOpen = true;
	}
	
	@Override
	public void onDrawerClosed(@NonNull View drawerView) {
		drawerOpen = false;
		dismissImmediate();
	}
	
	@Override
	public void onDrawerStateChanged(int newState) {
		drawerStat = newState;
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(MainActivityUIBase) context;
			mBackgroundColor = 0;
			setShowInDialog();
		}
		if (settingsLayout==null) {
			drawer = (NiceDrawerLayout) a.getLayoutInflater().inflate(R.layout.book_notes_view, a.root, false);
			drawer.addDrawerListener(this);
			viewPager = drawer.findViewById(R.id.viewpager);
			btns = drawer.findViewById(R.id.btns);
			viewList = new RecyclerView[3];
			bar = drawer.findViewById(R.id.bar);
			for (int i = 0; i < 3; i++) {
				RecyclerView lv = viewList[i] = new RecyclerView(new ContextThemeWrapper(a, R.style.RecyclerViewStyle));
				lv.setLayoutManager(new LinearLayoutManager(a));
				//RecyclerView.RecycledViewPool pool = viewList[i].getRecycledViewPool();
				//pool.setMaxRecycledViews(0,10);
				
				DividerItemDecoration divider = new DividerItemDecoration(a, LinearLayout.VERTICAL);
				divider.setDrawable(a.mResource.getDrawable(R.drawable.divider4));
				lv.addItemDecoration(divider);
				
				
				//取消更新item时闪烁
				RecyclerView.ItemAnimator anima = lv.getItemAnimator();
				if(anima instanceof DefaultItemAnimator)
					((DefaultItemAnimator)anima).setSupportsChangeAnimations(false);
				anima.setChangeDuration(0);
				anima.setAddDuration(0);
				anima.setMoveDuration(0);
				anima.setRemoveDuration(0);
				
				btns.getChildAt(i).setOnClickListener(this);
			}
			viewPager.setAdapter(new PagerAdapter() {
				@Override public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
					return arg0 == arg1;
				}
				@Override public int getCount() { return 3; }
				@Override public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
					RecyclerView child = viewList[position];
					container.removeView(child);
					child.stopScroll();
				}
				@NonNull @Override
				public Object instantiateItem(ViewGroup container, int position) {
					View child = viewList[position];
					container.addView(child);
					return child;
				}
			});
			btns.post(() -> btns.selectToolIndex(1));
			viewPager.setNoScroll(true);
			viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				int lastPos;
				@Override public void onPageScrollStateChanged(int arg0) { }
				@Override public void onPageScrolled(int arg0, float arg1, int arg2) { }
				@Override
				public void onPageSelected(int i) {
					RecyclerView lv = viewList[lastPos];
					if (lv != null) {
						// fix Inconsistency detected. Invalid view holder adapter position
						lv.stopScroll();
					}
					btns.selectToolIndex(lastPos = i);
					lv = viewList[i];
					if (i < 2) {
						if (lv.getAdapter()==null) {
							lv.setAdapter(getAnnotationAdapter(false, a.currentDictionary,lv, i));
						} else {
							a.annotAdapters[i].refresh(a.currentDictionary, a.prepareHistoryCon().getDB(), lv);
						}
					}
				}
			});
			viewPager.setCurrentItem(1);
			toolbar = drawer.findViewById(R.id.toolbar);
			toolbar.setNavigationIcon(com.knziha.filepicker.R.drawable.abc_ic_ab_back_material);
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					drawer.close();
				}
			});
			settingsLayout = drawer;
		}
	}
	
	public AnnotAdapter getAnnotationAdapter(boolean darkMode, BookPresenter invoker, RecyclerView lv, int scope) {
		AnnotAdapter adapter=a.annotAdapters[scope];
		if (adapter == null) {
			adapter = a.annotAdapters[scope] = new AnnotAdapter(a, R.layout.drawer_list_item, R.id.text1, invoker, a.prepareHistoryCon().getDB(), scope, lv);
		}
		adapter.darkMode=darkMode;
		adapter.setBookNotes(this);
		return adapter;
	}
	
	
	@Override
	protected void onShow() {
		//drawer.open();
		//drawer.openDrawer(GravityCompat.START);
		drawerOpen = false;
		drawer.post(() -> drawer.open());
		refresh();
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		drawerOpen = false;
		drawer.closeDrawer(GravityCompat.START, false);
	}
	
	@Override
	public void refresh() {
		if (MainAppBackground != a.MainAppBackground)
		{
			// 刷新颜色变化（黑暗模式或者设置更改）
			toolbar.setTitleTextColor(a.AppWhite);
			MainAppBackground = a.MainAppBackground;
			bar.setBackgroundColor(MainAppBackground);
			btns.setBackgroundColor(MainAppBackground);
			viewPager.setBackgroundColor(a.AppWhite);
			for (int i = 0; i < 3; i++) {
				//((TextView)btns.getChildAt(i)).setTextColor(a.AppBlack);
				((TextView)btns.getChildAt(i)).setTextColor(a.AppWhite);
			}
			int gray = 0x55888888;
			//if(Math.abs(0x888888-(a.MainAppBackground&0xffffff)) < 0x100000)
				gray = ColorUtils.blendARGB(a.MainAppBackground, Color.WHITE, 0.1f);
			btns.setSCC(btns.ShelfDefaultGray=gray);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (ViewUtils.getNthParentNonNull(v, 1)==btns) {
			RecyclerView lv = viewList[viewPager.getCurrentItem()];
			if (lv != null) {
				lv.stopScroll();
			}
			int k = btns.indexOfChild(v);
			viewPager.setCurrentItem(k, true);
		}
	}
	
	WebViewListHandler weblistHandler;
	ContentviewBinding contentUIData;
	
	private void setUpContentView() {
		if(contentUIData==null) {
			MainActivityUIBase a = (MainActivityUIBase) this.a;
			contentUIData = ContentviewBinding.inflate(a.getLayoutInflater());
			weblistHandler = new WebViewListHandler(a, contentUIData, a.schuiMain);
			weblistHandler.setBottomNavWeb(PDICMainAppOptions.bottomNavWeb());
			weblistHandler.setUpContentView(a.cbar_key);
			weblistHandler.setFetchWord(PDICMainAppOptions.dbCntFetcingWord()?2:0, null);
		}
		weblistHandler.checkUI();
	}
	
	public final SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
	int avoyagerIdx=0;
	int adelta=0;
	long pressedRowId;
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		if (!isLongClick) {
			popupMenuHelper.dismiss();
			try {
				SQLiteDatabase database = a.prepareHistoryCon().getDB();
				int cnt = database.delete(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, "id=?", new String[]{pressedRowId + ""});
				if (cnt > 0) {
					for (int i = 0; i < 2; i++) {
						RecyclerView lv = viewList[i];
						if (lv.getAdapter() != null) {
							lv.suppressLayout(true);
							AnnotAdapter adapter = getAnnotationAdapter(false, a.currentDictionary, lv, i);
							adapter.refresh(a.currentDictionary, database, lv);
						}
					}
					a.showT("删除成功");
					return true;
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
			a.showT("删除失败！");
			return true;
		}
		return false;
	}
	
	public void click(AnnotAdapter annotAdapter, View itemView, AnnotAdapter.VueHolder vh, boolean isLongClick) {
		int pos = vh.getLayoutPosition();
		AnnotAdapter.AnnotationReader reader = annotAdapter.dataAdapter.getReaderAt(pos);
		pressedRowId = reader.row_id;
		if (isLongClick) {
			PopupMenuHelper popupMenu = a.getPopupMenu();
			
			int[] texts = new int[]{
					R.string.delete};
			popupMenu.initLayout(texts, this);
			
			int[] vLocationOnScreen = new int[2];
			viewPager.getLocationOnScreen(vLocationOnScreen);
			int x=(int)viewPager.lastX;
			int y=(int)viewPager.lastY;
			popupMenu.show(drawer, x+vLocationOnScreen[0], y+vLocationOnScreen[1]);
			ViewUtils.preventDefaultTouchEvent(drawer, x, y);
			
			return;
		}
		setUpContentView();
		BookPresenter currentDictionary = a.getBookById(reader.bid);
		int idx = (int) reader.position;
		String key = reader.entryName;
		
		float desiredScale=-1;
		
		if (currentDictionary != null) {
			setUpContentView();
			final boolean bUseMergedUrl = false;
			boolean bUseDictView = /*currentDictionary.rl!=null || */!opt.getUseSharedFrame() || opt.getMergeExemptWebx()&&currentDictionary.getIsWebx();
			weblistHandler.setViewMode(null, 0, bUseDictView?currentDictionary.mWebView:weblistHandler.mMergedFrame);
			weblistHandler.viewContent();
			if(!bUseDictView) weblistHandler.initMergedFrame(0, true, false);
			
			WebViewmy webview = null;
			ViewGroup someView = null;
			if(bUseDictView) {
				currentDictionary.initViewsHolder(a);
				webview = currentDictionary.mWebView;
				someView = currentDictionary.rl;
				if(webview.weblistHandler==a.weblistHandler && a.weblistHandler.isWeviewInUse(someView)) {
					a.DetachContentView(false);
				}
			} else {
				webview = weblistHandler.getMergedFrame();
				someView = weblistHandler.mMergedBook.rl;
			}
			webview.weblistHandler = weblistHandler;
			if (weblistHandler.dictView==null) {
				weblistHandler.dictView = webview;
			}
			
			
			ScrollerRecord pPos = null;
			//if(opt.getRemPos())
			{
				SparseArray<ScrollerRecord> avoyager = webview.presenter.avoyager;
				ViewGroup webviewHolder = weblistHandler.getViewGroup();
				if(System.currentTimeMillis()-a.lastClickTime>300 && webviewHolder.getChildCount()!=0) {
					//save our postion
					View child = webviewHolder.getChildAt(0);
					BookPresenter book = ((WebViewmy)child.findViewById(R.id.webviewmy)).presenter;
					{
//						if (adelta!=0 && webview != null && !webview.isloading) {
//							if (webview.webScale == 0)
//								webview.webScale = a.dm.density;//sanity check
//							CMN.Log("dbrowser::保存位置::", book.getDictionaryName(), (int) webview.currentPos);
//							pPos = avoyager.get((int) webview.currentPos);
//							if (pPos == null
//									&& (webview.getScrollX() != 0 || webview.getScrollY() != 0
//									|| webview.webScale != BookPresenter.def_zoom)) {
//								avoyager.put((int) webview.currentPos, pPos = new ScrollerRecord());
//							}
//							if (pPos!=null) {
//								pPos.set(webview.getScrollX(), webview.getScrollY(), webview.webScale);
//							}
//						}
					}
				}
				
				adelta=0;
				a.lastClickTime=System.currentTimeMillis();
				
				pPos = currentDictionary.avoyager.get(idx);
				//a.showT(""+currentDictionary.expectedPos);
			}
			if(pPos!=null) {
				webview.expectedPos = pPos.y;
				webview.expectedPosX = pPos.x;
				desiredScale=pPos.scale;
				//CMN.Log(avoyager.size()+"~"+position+"~取出旧值"+webview.expectedPos+" scale:"+pPos.scale);
			} else {
				webview.expectedPos=0;
				webview.expectedPosX=0;
			}
			
			weblistHandler.popupContentView(null, key);
			
			ViewGroup webviewHolder = weblistHandler.getViewGroup();
			ViewUtils.addViewToParent(someView, webviewHolder);
			if(webviewHolder.getChildCount()>1) {
				for(int i=webviewHolder.getChildCount()-1;i>=0;i--)
					if(webviewHolder.getChildAt(i)!=someView) webviewHolder.removeViewAt(i);
			}
			
			currentDictionary.renderContentAt(desiredScale,RENDERFLAG_NEW,0,webview, idx);
			webview.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
			
			contentUIData.PageSlider.setWebview(webview, null);
			someView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
		}
	}
}
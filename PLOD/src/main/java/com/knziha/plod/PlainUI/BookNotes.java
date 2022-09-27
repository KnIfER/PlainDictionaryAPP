package com.knziha.plod.PlainUI;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
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
import com.knziha.plod.widgets.TextMenuView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BookNotes extends PlainAppPanel implements DrawerLayout.DrawerListener, PopupMenuHelper.PopupMenuListener, Toolbar.OnMenuItemClickListener {
	public int[] sortTypes;
	MainActivityUIBase a;
	NiceDrawerLayout drawer;
	int drawerStat;
	boolean drawerOpen;
	NoScrollViewPager viewPager;
	boolean[] inited;
	RecyclerView[] viewList;
	ViewGroup bar;
	ShelfLinearLayout bottomShelf;
	Toolbar toolbar;
	BookPresenter invoker;
	
	public BookNotes(MainActivityUIBase a) {
		super(a, false);
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.a = a;
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
		if (a!=null && settingsLayout==null) {
			drawer = (NiceDrawerLayout) a.getLayoutInflater().inflate(R.layout.book_notes_view, a.root, false);
			drawer.addDrawerListener(this);
			viewPager = drawer.findViewById(R.id.viewpager);
			bottomShelf = drawer.findViewById(R.id.btns);
			viewList = new RecyclerView[3];
			bar = drawer.findViewById(R.id.bar);
			sortTypes = new int[]{-1, -1, -1};
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
				
				bottomShelf.getChildAt(i).setOnClickListener(this);
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
			bottomShelf.post(() -> bottomShelf.selectToolIndex(1));
			viewPager.setNoScroll(true);
			viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				int lastPos;
				@Override public void onPageScrollStateChanged(int arg0) { }
				@Override public void onPageScrolled(int arg0, float arg1, int arg2) { }
				@Override
				public void onPageSelected(int i) {
					CMN.debug("onPageSelected::", i);
					RecyclerView lv = viewList[lastPos];
					if (lv != null) {
						// fix Inconsistency detected. Invalid view holder adapter position
						lv.stopScroll();
					}
					bottomShelf.selectToolIndex(lastPos = i);
					lv = viewList[i];
					
					AnnotAdapter ada = getAnnotationAdapter(false, lv, i);
					if (lv.getAdapter()==null) {
						lv.setAdapter(ada);
						ada.resumeListPos(lv);
					} else {
						/*切页刷新*/ada.refresh(a.prepareHistoryCon().getDB(), BookNotes.this, lv);
					}
					//ada.dataAdapter.getReaderAt(0);
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
			toolbar.inflateMenu(R.xml.menu_book_note);
			toolbar.setOnMenuItemClickListener(this);
			settingsLayout = drawer;
		}
	}
	
	public AnnotAdapter getAnnotationAdapter(boolean darkMode, RecyclerView lv, int scope) {
		AnnotAdapter adapter=a.annotAdapters[scope];
		if (adapter == null) {
			adapter = a.annotAdapters[scope] = new AnnotAdapter(a, R.layout.drawer_list_item, R.id.text1
					, a.prepareHistoryCon().getDB(), scope, lv, this);
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
		int k = viewPager.getCurrentItem();
		RecyclerView lv = viewList[k];
		if (lv.getAdapter() != null) {
			AnnotAdapter adapter = getAnnotationAdapter(false, lv, k);
			adapter.saveListPosition(null);
		}
	}
	
	@Override
	public void refresh() {
		if (MainAppBackground != a.MainAppBackground)
		{
			// 刷新颜色变化（黑暗模式或者设置更改）
			toolbar.setTitleTextColor(a.AppWhite);
			MainAppBackground = a.MainAppBackground;
			bar.setBackgroundColor(MainAppBackground);
			bottomShelf.setBackgroundColor(MainAppBackground);
			viewPager.setBackgroundColor(a.AppWhite);
			for (int i = 0; i < 3; i++) {
				//((TextView)btns.getChildAt(i)).setTextColor(a.AppBlack);
				((TextView) bottomShelf.getChildAt(i)).setTextColor(a.AppWhite);
			}
			int gray = 0x55888888;
			//if(Math.abs(0x888888-(a.MainAppBackground&0xffffff)) < 0x100000)
				gray = ColorUtils.blendARGB(a.MainAppBackground, Color.WHITE, 0.1f);
			bottomShelf.setSCC(bottomShelf.ShelfDefaultGray=gray);
		}
	}
	
	public void checkBoundary() {
		int k = viewPager.getCurrentItem();
		RecyclerView lv = viewList[k];
		AnnotAdapter ada = getAnnotationAdapter(false, lv, k);
		if (lv.getAdapter()!=null
				/*&& (k==2 || ada.dbWriteVer != LexicalDBHelper.annotDbWriteVer
				|| ada.dbVer != LexicalDBHelper.annotDbVer)*/) {
			ada.refresh(a.prepareHistoryCon().getDB(), BookNotes.this, lv); CMN.debug("/*启动刷新*/");
		}
	}
	
	@Override
	public void onClick(View v) {
		if (ViewUtils.getNthParentNonNull(v, 1)== bottomShelf) {
			RecyclerView lv = viewList[viewPager.getCurrentItem()];
			if (lv != null) {
				lv.stopScroll();
			}
			int k = bottomShelf.indexOfChild(v);
			viewPager.setCurrentItem(k, true);
			return;
		}
		int sortBy = -1;
		switch (v.getId()) {
			case R.string.st_time:
				sortBy = 0;
			break;
			case R.string.st_dpp:
				sortBy = 2;
			break;
			case R.string.st_dpt:
				sortBy = 4;
			break;
			case R.string.st_d_tm:
				sortBy = 6;
			break;
		}
		if (sortBy >= 0) {
			PopupMenuHelper ret = popupMenuRef.get();
			if (ret!=null) {
				ret.dismiss();
				ret.tag=-1;
			}
			if(v instanceof ImageView)
				sortBy++;
			int i = viewPager.getCurrentItem();
			if (i == 0) {
				PDICMainAppOptions.annotDBSortBy(sortBy);
			} else if(i==1){
				PDICMainAppOptions.annotDB1SortBy(sortBy);
			} else {
				PDICMainAppOptions.annotDB2SortBy(sortBy);
			}
			sortTypes[i] = sortBy;
			RecyclerView lv = viewList[i];
			if (lv.getAdapter() != null) {
				lv.suppressLayout(true);
				AnnotAdapter adapter = getAnnotationAdapter(false, lv, i);
				/*变换排序规则*/adapter.rebuildCursor(a.prepareHistoryCon().getDB(), pressedV.get(), this, null);
			}
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
			switch (v.getId()) {
				case R.string.copy:{
					View iteView = pressedV.get();
					CharSequence tx = ((AnnotAdapter.VueHolder) iteView.getTag()).vh.title.getText();
					ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
					if(cm!=null){
						cm.setPrimaryClip(ClipData.newPlainText(null, tx));
						a.showT(tx);
					}
				} break;
				case R.string.send_dot:{
					View iteView = pressedV.get();
					String tx = ((AnnotAdapter.VueHolder) iteView.getTag()).vh.title.getText().toString();
					a.getVtk().setInvoker(null, null, null, tx);
					a.getVtk().onClick(null);
				} break;
				case R.string.delete:{
					try {
						SQLiteDatabase database = a.prepareHistoryCon().getDB();
						Cursor cursor = database.rawQuery("select * from " + LexicalDBHelper.TABLE_BOOK_ANNOT_v2 + " where id=? limit 1", new String[]{pressedRowId + ""});
						ContentValues cv = null;
						if (cursor.moveToNext()) {
							cv = ViewUtils.dumpCursorValues(cursor);
							CMN.debug("cv::", cv);
							if (cv != null) {
								ArrayList<ContentValues> stack = a.annotUndoStack;
								stack.add(cv);
								if (stack.size() >= 150) {
									stack.subList(0, stack.size()-100).clear();
								}
							}
						}
						cursor.close();
						int cnt = database.delete(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, "id=?", new String[]{pressedRowId + ""});
						if (cnt > 0) {
							int k = viewPager.getCurrentItem();
							RecyclerView lv = viewList[k];
							if (lv.getAdapter() != null) {
								lv.suppressLayout(true);
								AnnotAdapter adapter = getAnnotationAdapter(false, lv, k);
								LexicalDBHelper.increaseAnnotDbVer();
								/*删除*/adapter.rebuildCursor(database, null, this, null);
							}
							a.showT("删除成功");
							return true;
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
					a.showT("删除失败！");
				} break;
				case R.string.sortby:{
					if (viewPager.getCurrentItem()<=2) {
						PopupMenuHelper popupMenu = getSortByPopupMenu();
						
						int[] vLocationOnScreen = new int[2];
						viewPager.getLocationOnScreen(vLocationOnScreen);
						int x=(int)viewPager.lastX;
						int y=(int)viewPager.lastY;
						popupMenu.show(drawer, x+vLocationOnScreen[0], y+vLocationOnScreen[1]);
						ViewUtils.preventDefaultTouchEvent(drawer, x, y);
					}
				} break;
			}
			return true;
		}
		return false;
	}
	
	
	WeakReference<PopupMenuHelper> popupMenuRef = ViewUtils.DummyRef;
	WeakReference<View> pressedV = ViewUtils.DummyRef;
	public PopupMenuHelper getSortByPopupMenu() {
		PopupMenuHelper ret = popupMenuRef.get();
		if (ret==null) {
			ret  = new PopupMenuHelper(a, null, null);
			//ret.lv.removeAllViews();
			int[] texts = new int[]{
				R.string.st_time
				,R.string.st_d_tm
				,R.string.st_dpp
				,R.string.st_dpt
			};
			ret.tag = 0;
			for (int i = 0; i < texts.length; i++) {
				View view = a.getLayoutInflater().inflate(R.layout.menu_with_btn_view, ret.lv, false);
				TextMenuView tv = view.findViewById(R.id.text);
				tv.setId(texts[i]);
				tv.setText(texts[i]);
				tv.setOnClickListener(this);
				tv.leftDrawable = ret.leftDrawable;
				View v = view.findViewById(R.id.btn);
				v.setId(texts[i]);
				v.setOnClickListener(this);
				ret.lv.addView(view);
			}
			popupMenuRef = new WeakReference<>(ret);
		}
		int k = viewPager.getCurrentItem();
		AnnotAdapter ada = a.annotAdapters[k];
		if (ada != null) {
			final int sortType = ada.sortType(this), rowAct;
			if (ret.tag != k) {
				boolean b1 = sortType % 2 == 0;
				if (sortType > 5) rowAct = 1;
				else if (sortType > 3) rowAct = 3;
				else if (sortType > 1) rowAct = 2;
				else rowAct = 0;
				CMN.debug("activate::", rowAct, ada.sortType(this));
				for (int i = 0; i < 4; i++) {
					View view = ret.lv.getChildAt(i);
					TextMenuView tv = (TextMenuView) ((ViewGroup) view).getChildAt(0);
					if (i == 0) {
						ViewUtils.setVisible(view, k != 1);
					}
					tv.activated = rowAct == i;
					tv.setTextColor(rowAct != i || b1 ? a.AppBlack : Color.BLUE);
					if (tv.getTag()==null) {
						tv.setTag(tv.getText());
					}
					String text = (String) tv.getTag();
					tv.setText(k==0?text:text.substring(text.indexOf("+")+1));
				}
				ret.tag = (sortType << 2) | k;
			}
		}
		return ret;
	}
	
	public void click(AnnotAdapter annotAdapter, View itemView, AnnotAdapter.VueHolder vh, boolean isLongClick) {
		int pos = vh.getLayoutPosition();
		AnnotAdapter.AnnotationReader reader = annotAdapter.dataAdapter.getReaderAt(pos);
		pressedRowId = reader.row_id;
		pressedV = new WeakReference<>(itemView);
		if (isLongClick) {
			PopupMenuHelper popupMenu = a.getPopupMenu();
			
			int[] texts = new int[]{
					R.string.copy
					, R.string.send_dot
					, R.string.sortby
					, R.string.delete
			};
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
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:a.getDummyMenuImpl(id);
		MenuBuilder menu = (MenuBuilder) mmi.mMenu;
		boolean isLongClicked= mmi!=null && mmi.isLongClicked;
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		if (item.getItemId()==R.drawable.ic_sort_path_asc) {
			pressedV.clear();
			if (viewPager.getCurrentItem()<=2) {
				PopupMenuHelper popupMenu = getSortByPopupMenu();
				
				int[] vLocationOnScreen = new int[2];
				toolbar.getLocationOnScreen(vLocationOnScreen);
				int x = toolbar.getWidth(), y = toolbar.getHeight();
				popupMenu.show(toolbar, x+vLocationOnScreen[0], y+vLocationOnScreen[1]);
				ViewUtils.preventDefaultTouchEvent(toolbar, x, y);
			}
		}
		if (item.getItemId()==R.drawable.ic_baseline_undo_24) {
			if (a.annotUndoStack.size() > 0) {
				ContentValues undo = a.annotUndoStack.remove(a.annotUndoStack.size() - 1);
				
				SQLiteDatabase database = a.prepareHistoryCon().getDB();
				long res = database.insertWithOnConflict(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, null, undo, CONFLICT_FAIL);
				if (res==-1) {
					undo.remove("id");
					res = database.insertWithOnConflict(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, null, undo, CONFLICT_ABORT);
				}
				
				if (res != -1) {
					a.showT("撤销成功!");
					int i = viewPager.getCurrentItem();
					RecyclerView lv = viewList[i];
					if (lv.getAdapter() != null) {
						lv.suppressLayout(true);
						AnnotAdapter adapter = getAnnotationAdapter(false, lv, i);
						/*撤销删除*/adapter.rebuildCursor(a.prepareHistoryCon().getDB(), null, this, null);
					}
				}
			}
		}
		if(closeMenu)
			a.closeIfNoActionView(mmi);
		return ret;
	}
	
	public void setInvoker(BookPresenter invoker) {
		//CMN.debug("setInvoker::", invoker);
		this.invoker = invoker;
	}
}
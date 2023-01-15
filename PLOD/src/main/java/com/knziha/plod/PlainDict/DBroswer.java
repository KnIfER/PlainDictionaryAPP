package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.knziha.ankislicer.customviews.ArrayAdaptermy;
import com.knziha.ankislicer.customviews.WahahaTextView;
import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.plaindict.databinding.DbBrowserBinding;
import com.knziha.plod.plaindict.databinding.DbCardListItemBinding;
import com.knziha.plod.settings.History;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.SimpleDialog;
import com.knziha.plod.widgets.TitleItemDecoration;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTNode;
import com.knziha.rbtree.RashSet;
import com.knziha.rbtree.additiveMyCpr1;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import com.knziha.plod.db.LexicalDBHelper;

import static com.knziha.plod.db.LexicalDBHelper.TABLE_HISTORY_v2;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;
import static com.knziha.plod.plaindict.MainActivityUIBase.ActType;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_FAVORITE_v2;
import static com.knziha.plod.plaindict.DBListAdapter.*;
import static java.time.temporal.ChronoUnit.DAYS;

//  todo 分表显示
@SuppressLint("SetTextI18n")
public class DBroswer extends DialogFragment implements
		View.OnClickListener, OnLongClickListener, Toolbar.OnMenuItemClickListener, PopupMenuHelper.PopupMenuListener {
	public int pendingDBClickPos=-1;
	public int type;
	public int pendingType;
	protected boolean initialized;
	private boolean isDarkStamp;
	private boolean bIsCombinedSearch;
	DbBrowserBinding UIData;
	WebViewListHandler weblistHandler;
	ContentviewBinding contentUIData;
	PeruseView peruseView;
	TitleItemDecoration mDecorator;
	
	/** type[act|ui|db], long[]{pos, view offset} */
	public final static LongSparseArray<long[]> savedPositions = new LongSparseArray();
	
	DragSelectRecyclerView lv;
	int lastDragPos=-1;
	//long favFolderId=-1;
	
	DBListAdapter mAdapter;
	ImageView pageAsyncLoader;
	Date date = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");
	
	protected PDICMainAppOptions opt;

	public String currentDisplaying = "";
	public int currentPos=-1;
	public long currentRowId=-1;
	
	HashSet<Long> toDeleteV2 = new HashSet<>();
	HashSet<Long> Selection = new HashSet<>();

	boolean isToDel = false;

	LinearLayoutManager lm;

	InputMethodManager imm;
	private int MainAppBackground;
	private int ForegroundColor = Color.WHITE;
	private int pressedRow;
	private View pressedView;
	private LocalDateTime today;

//	private static long _24hMillis;
//	static {
//		try {
//			Calendar calender = Calendar.getInstance();
//			calender.set(0, 0, 0);
//			long day1 = calender.getTimeInMillis();
//			calender.set(0, 0, 1);
//			_24hMillis = Math.abs(day1 - calender.getTimeInMillis());
//		} catch (Exception e) {
//			_24hMillis = 24*60*60*1000;
//			CMN.debug(e);
//		}
//	}
	
	/** see {@link #toggleFavor}*/
	public int try_goBack(){
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null || !initialized) return 0;
//		if(a.isContentViewAttachedForDB()) { //111
//			a.DetachContentView(true);
//			SparseArray<String> deleting = this.toDelete;
//			Long[] deletingV2 = this.toDeleteV2.toArray(new Long[this.toDeleteV2.size()]);
//			if(!isToDel || deletingV2.length==0) return 1;
//
//			//删除收藏 to impl
//			SQLiteDatabase db = mLexiDB.getDB();
//			{
//				String sql = "delete from "+TABLE_FAVORITE_v2+" where id = ? ";
//				SQLiteStatement preparedDeleteExecutor = db.compileStatement(sql);
//				db.beginTransaction();  //开启事务
//				int count = 0;
//				int toDelete_size = deletingV2.length;
//				try {
//					for(Long rowId:deletingV2) {//delete
//						preparedDeleteExecutor.bindLong(1, rowId);
//						if(preparedDeleteExecutor.executeUpdateDelete()!=-1) {
//							count++;
//						}
//						this.toDeleteV2.remove(rowId);
//					}
//					preparedDeleteExecutor.close();
//					Selection.clear();
//					db.setTransactionSuccessful();  //控制回滚
//				} catch (Exception e) {
//					CMN.Log(e);
//				} finally {
//					db.endTransaction();  //事务提交
//					mAdapter.rebuildCursor(a);
//					show(R.string.maniDel,count,toDelete_size);
//				}
//			}
//			return 1;
//		}
		if(menuPopup !=null && menuPopup.isShowing()) {
			menuPopup.dismiss();
			return 1;
		}
		if(inSearch) {
			UIData.search.performClick();
			return 1;
		}
		if(Selection.size()>0) {//SelectionMode==SelectionMode_select
			Selection.clear();
			dataSetChanged();
			UIData.counter.setText(Selection.size()+"/"+ getItemCount());
			if(SelectionMode==SelectionMode_select) {
				int taregtID=0;
				switch(lastFallBackTarget) {
					case SelectionMode_learncard:
						taregtID=R.id.tools001;
					break;
					case SelectionMode_fetchWord:
						taregtID=R.id.toolbar_action2;
					break;
					case SelectionMode_pan:
						taregtID=R.id.tools0;
					break;
				}
				if(taregtID!=0) {// taregtID=R.id.tools0;
					View target = UIData.getRoot().findViewById(taregtID);
					target.setTag(false);
					target.performClick();
				}
			}else {
				UIData.counter.setText(Selection.size()+"/"+ getItemCount());
			}
			return 1;
		}
		if (type==DB_FAVORITE && toDeleteV2.size()>0) {
			int size = toDeleteV2.size();
			Iterator<Long> iter = toDeleteV2.iterator();
			SQLiteDatabase database = mLexiDB.getDB();
			database.beginTransaction();
			SQLiteStatement state = null;
			int cc=0;
			try {
				state = database.compileStatement("DELETE FROM " + TABLE_FAVORITE_v2 + " where id=?");
				// see
				while (iter.hasNext()) {
					state.bindLong(1, iter.next());
					if (state.executeUpdateDelete()>0) {
						cc++;
					}
				}
				database.setTransactionSuccessful();
				toDeleteV2.clear();
			} finally {
				database.endTransaction();
				try {
					if (state!=null) {
						state.close();
					}
				} catch (Exception ignored) { }
				a.showT("已删除 "+cc+"/"+size+" 项收藏！");
				mAdapter.data.ver = 0;
			}
		}
		return 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(UIData==null) {
			UIData = DbBrowserBinding.inflate(inflater, container, false);
			//CMN.Log("onCreateView!!!");
			lv = UIData.mainList;
			UIData.fastScroller.setRecyclerView(lv);
			lv.addOnScrollListener(UIData.fastScroller.getOnScrollListener());
			lv.setLayoutManager(lm = new LinearLayoutManager(inflater.getContext()));
			
			ViewUtils.setOnClickListenersOneDepth(UIData.sideBar, this, 2, 0, null);
			
			UIData.toolbarAction1.setColorFilter(GlobalOptions.BLACK);
			
			UIData.browserWidget15.setOnClickListener(ViewUtils.DummyOnClick);
			UIData.browserWidget14.setOnClickListener(this);
			UIData.browserWidget13.setOnClickListener(this);
			UIData.browserWidget1.setOnClickListener(this);
			
			Toolbar toolbar = UIData.toolbar;
			toolbar.inflateMenu(R.xml.menu_dbrowser);
			toolbar.setOnMenuItemClickListener(this);
			
			toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
			toolbar.setNavigationOnClickListener(v1 -> {
				if(true && this==getMainActivity().DBrowser)  {
					getMainActivity().DetachDBrowser();
				} else {
					dismiss();
				}
			});
			
			if (PDICMainAppOptions.dbTextSelectable()) {
				toolbar.getMenu().findItem(R.id.text).setChecked(true);
			}
			menuTapsch = toolbar.getMenu().findItem(R.id.tapSch);
			menuPeruse = toolbar.getMenu().findItem(R.id.peruse);
			menuFetchWord = toolbar.getMenu().findItem(R.id.fetchWord);
			menuLearnCard = toolbar.getMenu().findItem(R.id.learnCard);
			
//			if (container.getContext() instanceof MainActivityUIBase) {
////			toolbar.setBackgroundColor(0xcc000000|(((MainActivityUIBase) container.getContext()).MainBackground&0xffffff));
//			} //xxx
			//CMN.Log("咿呀咿呀", container.getContext());
			//  new SearchCardsHandler2().execute(query);
			
			UIData.counter.setVisibility(View.GONE);
			newStart = true;
		} else {
			ViewUtils.removeView(UIData.getRoot());
		}
		return UIData.getRoot();
	}
	boolean newStart;
	long last_listHolder_tt;

	LexicalDBHelper mLexiDB;
	
	@Override
	public void onDetach(){
		super.onDetach();
		//CMN.Log("on browser detach", cr!=null && lm.findFirstVisibleItemPosition()>=1 && mCards_size>=0);
		saveListPosition(); // onDetach
	}
	
	private void saveListPosition() {
		try {
			View view = lv.getChildAt(0);
			if (view!=null) {
				ViewUtils.ViewDataHolder holder = (ViewUtils.ViewDataHolder) view.getTag();
				HistoryDatabaseReader reader = (HistoryDatabaseReader) holder.tag;
				// CMN.debug("saveListPosition::", holder.getLayoutPosition());
				long view_savid = getFragmentId();
				if (holder.getLayoutPosition() > 0) {
					long[] sortBy = new long[reader.sort_numbers.length+2];
					System.arraycopy(reader.sort_numbers, 0, sortBy, 0, sortBy.length-2);
					sortBy[sortBy.length-2] = view.getTop();
					sortBy[sortBy.length-1] = holder.getLayoutPosition();
					savedPositions.put(view_savid, sortBy);
				} else {
					savedPositions.remove(view_savid);
				}
				CMN.debug("savedPositions::save::", getFragmentId()+" "+reader.record+" "+new Date(reader.sort_number).toLocaleString());
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	protected void loadInAll(MainActivityUIBase a) {
		// CMN.debug("FAV load in all!!!", initialized, type, getTableName());
		if(initialized) {
			if(mAdapter!=null && mAdapter.resetDataCache(type)) {
				mAdapter.notifyDataSetChanged();
//			lv.postDelayed(mAdapter::notifyDataSetChanged, 180);
			}
			mLexiDB = a.prepareHistoryCon();
			mAdapter.rebuildCursor(a, getFragmentId());
			
			String foldername;
			if(type==DB_FAVORITE) {
				long fid = a.opt.getCurrFavoriteNoteBookId();
				mAdapter.data.fid = fid;
				mAdapter.data.ver = mLexiDB.getDBVersion(fid);
				foldername = mLexiDB.getFavoriteNoteBookNameById(fid);
			} else {
				mAdapter.data.ver = mLexiDB.getDBVersion(mAdapter.data.fid = -1L);
				foldername = "历史记录";
			}
			setTitle(foldername);
			//show(type==DB_FAVORITE?R.string.maniFavor:R.string.maniFavor2, foldername, getItemCount());
			//progressBar.setVisibility(View.GONE);
		}
	}
	
	private void setTitle(String title) {
		UIData.toolbar.setTitle(title);
		UIData.smallLabel.setText(title);
	}
	
	private void setUpContentView() {
		if(contentUIData==null) {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			contentUIData = ContentviewBinding.inflate(getLayoutInflater());
			weblistHandler = new WebViewListHandler(a, contentUIData, a.schuiMain);
			weblistHandler.setBottomNavWeb(PDICMainAppOptions.bottomNavWeb());
			weblistHandler.setUpContentView(a.cbar_key);
			weblistHandler.setFetchWord(PDICMainAppOptions.dbCntFetcingWord()?2:0);
		}
		weblistHandler.checkUI();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(!initialized) {
			DBListAdapter adapter = new DBListAdapter(a, this);
			lv.setAdapter(adapter);
			RecyclerView.RecycledViewPool pool = lv.getRecycledViewPool();
			pool.setMaxRecycledViews(0,10);
			for(int index =0;index < 10;index++) {
				pool.putRecycledView(adapter.createViewHolder(lv,0));
			}
			mAdapter = adapter;
			
			mLexiDB = a.prepareHistoryCon();
			opt = a.opt;
			imm = a.imm;
			initialized = true;
			
			Instant instant1 = Instant.EPOCH.plusMillis(CMN.now());
			today = LocalDateTime.ofInstant(instant1, ZoneId.systemDefault());
			
			mDecorator = new TitleItemDecoration(a, new TitleItemDecoration.TitleDecorationCallback() {
				@Override
				public boolean isSameGroup(int prvPos, int thPos) {
					//return prvPos/5==thPos/5;
					
					try {
						HistoryDatabaseReader reader1 = mAdapter.getReaderAt(prvPos);
						HistoryDatabaseReader reader2 = mAdapter.getReaderAt(thPos);
						//CMN.debug("isSameGroup??", thPos, reader1.record, reader2.record);
						
						if(reader1.fav > 0 ^ reader2.fav>0) {
							//CMN.debug("reader1.fav > 0 ^ reader2.fav>0", reader1.fav  ,  reader2.fav);
							return false;
						}
						
						if(reader1.fav > 0 && reader1.fav==reader2.fav && false) { // dbMergeSameLevelSepDays
							return true;
						}
						
						Instant instant1 = Instant.EPOCH.plusMillis(reader1.sort_number);
						Instant instant2 = Instant.EPOCH.plusMillis(reader2.sort_number);
						
						LocalDateTime t1 = LocalDateTime.ofInstant(instant1, ZoneId.systemDefault());
						LocalDateTime t2 = LocalDateTime.ofInstant(instant2, ZoneId.systemDefault());
						
						if (t1.getYear() == t2.getYear()) {
							//if(!(t1.getDayOfYear() == t2.getDayOfYear()))
							//CMN.debug("t1.getDayOfYear() ! t2.getDayOfYear()", t1.getDayOfYear(), t2.getDayOfYear(), reader1.record, reader2.record);
							return t1.getDayOfYear() == t2.getDayOfYear();
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
					return true;
				}
				@Override
				public String getGroupName(int position) {
					//return "group"+position;
					try {
						HistoryDatabaseReader reader = mAdapter.getReaderAt(position);
						//if(reader.fav > 0 && true) return "";
						return dateFormat.format(new Date(reader.sort_number));
					} catch (Exception e) {
						CMN.debug(e);
						return "error "+e;
					}
				}
				@Override
				public void postDrawText(Canvas canvas, float top, float bottom, float x, float y, float labelSz, Paint textPaint, int position, String name) {
					try {
						HistoryDatabaseReader reader = mAdapter.getReaderAt(position);
						Instant instant1 = Instant.EPOCH.plusMillis(reader.sort_number);
						LocalDateTime t1 = LocalDateTime.ofInstant(instant1, ZoneId.systemDefault());
						long daysBefore = DAYS.between(t1, today);
						if(labelSz==-1) {
							labelSz = textPaint.measureText(name);
						}
						x = x + labelSz;
						int height = mDecorator.getHeight();
						float padY = height/8;
						String label;
						if(true) {
							boolean drawBackground = daysBefore <= 30;
							//CMN.debug("daysBefore::", daysBefore, t1.getDayOfWeek(), today.getDayOfWeek());
							label  = daysBefore+"天前";
							if(daysBefore<3) {
								int deltaDay = Math.abs(t1.getDayOfWeek().ordinal()-today.getDayOfWeek().ordinal());
								//CMN.debug("deltaDay::", deltaDay);
								if(deltaDay > 3) {
									deltaDay = 7-deltaDay; // 好烦
								}
								if (deltaDay==0) {
									label = "今天";
								}
								if (deltaDay==1) {
									label = "昨天";
								}
							}
							int bg = GlobalOptions.isDark ? 0xFFc17d33 : a.MainAppBackground;
							int fontColor = GlobalOptions.isDark ? Color.WHITE : a.AppWhite;
							if(!drawBackground) {
								fontColor = GlobalOptions.isDark ? 0xFFc17d33 : Color.RED;
								bg = 0;
							}
							x = mDecorator.drawLabel(canvas, label, x+padY*3, y, top, bottom, fontColor, bg);
							
							if (daysBefore<7) {
								int day = t1.getDayOfWeek().getValue();
								label  = day==7? "周天" : ("周"+SU.days.charAt(day-1));
								x = mDecorator.drawLabel(canvas, label, x+padY*3, y, top, bottom, fontColor, bg);
							}
							if (daysBefore == 0) {
								float rad = GlobalOptions.density*4.1f;
								mDecorator.textPaint1.setColor(bg);
								canvas.drawCircle(rad/2 + mDecorator.paddingLeft/2, height/2+top, rad, mDecorator.textPaint1);
							}
						}
						if (reader.fav > 0) {
							/** 画星星 see {@link com.knziha.plod.widgets.FlowTextView#drawStars} */
							Drawable mActiveDrawable = a.getStarDrawable();
							int mStarWidth = (int) (GlobalOptions.density*25);
							final int starTop = (int) (top + (height - mStarWidth)/2);
							final int starBottom = starTop+mStarWidth;
							final int padding = (int) (mStarWidth*2/3+2*GlobalOptions.density);
							int i = 0, starLeft, lvl = Math.min(9, reader.fav); // 九星强者恐怖如斯
							starLeft = (int) (x + padY);
							for (; i < lvl; i++) {
								mActiveDrawable.setBounds(starLeft, starTop, starLeft+mStarWidth, starBottom);
								mActiveDrawable.draw(canvas);
								starLeft += padding;
							}
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			}, isDarkStamp?0xFFc17d33:Color.RED, a.AppWhite);
			lv.addItemDecoration(mDecorator);
			
			mDecorator.bPinTitle = true;
			//decoration.textBackground = a.MainAppBackground;
			mDecorator.textCorner = GlobalOptions.density * 3;
			mDecorator.paddingLeft = GlobalOptions.density*30;
			
			if(pendingType!=0) {
				setType(a, pendingType, true);
				pendingType=0;
			}
			
			//取消更新item时闪烁
			RecyclerView.ItemAnimator anima = lv.getItemAnimator();
			if(anima instanceof DefaultItemAnimator)
				((DefaultItemAnimator)anima).setSupportsChangeAnimations(false);
			anima.setChangeDuration(0);
			anima.setAddDuration(0);
			anima.setMoveDuration(0);
			anima.setRemoveDuration(0);
			
			UIData.fastScroller.setConservativeScroll(opt.getShelfStrictScroll());
			bIsCombinedSearch = opt.getIsCombinedSearching();
			UIData.toolbarAction1.setActivated(bIsCombinedSearch);
			
			if(opt.getScrollShown()) {
				UIData.tg2.setChecked(true);
				UIData.fastScroller.setVisibility(View.GONE);
			}
			
			mAdapter.viewRootHolder.view=a.root.getRootView();
			loadInAll(a);
			checkColors();
			
			SelectionMode = opt.getDBMode();
			UIData.getRoot().post(() -> UIData.sideBar.selectToolIndex(SelectionMode));
			if(SelectionMode==3 && opt.getInRemoveMode()) {
				UIData.sideBar.setSCC(opt.getInRemoveMode()?getResources().getColor(R.color.ShallowHeaderBlue):UIData.sideBar.ShelfDefaultGray);
			}
			else if (SelectionMode==2) {
				refreshFetchWordUI();
			}
			if(pendingDBClickPos!=-1){
				onItemClick(null, pendingDBClickPos);
				pendingDBClickPos=-1;
			}
			
			MenuBuilder menu = (MenuBuilder) UIData.toolbar.getMenu();
			MenuItem searchItem = menu.getItem(0);
			menu.checkActDrawable = a.mResource.getDrawable(R.drawable.frame_checked_whiter);
			menu.checkDrawable = a.AllMenus.checkDrawable;
			menu.mOverlapAnchor = false;
			menu.contentDescriptor = new MenuBuilder.ContentDescriptor(){
				public CharSequence describe(MenuItemImpl menuItem, CharSequence value){
					if (menuItem.isCheckable()) {
						value = menuItem.getTitle() + (menuItem.isChecked()?" 已激活":" 未激活");
					}
					return value;
				}
			};
			if(PDICMainAppOptions.dbShowIcon())
				menu.findItem(R.id.icon).setChecked(true);
			if(PDICMainAppOptions.dbLongPressSelect())
				menu.findItem(R.id.longPressSelect).setChecked(true);
		}
		else {
			boolean bNeedInvalidate=false;
			if(mAdapter!=null && mAdapter.resetDataCache(type)) {
				bNeedInvalidate = true;
			}
			boolean pull = mLexiDB==null || mAdapter.data.type==0
					|| getAutoRefreshOnAttach() && (
						type==DB_FAVORITE && mAdapter.data.fid!=a.opt.getCurrFavoriteNoteBookId()
							|| mAdapter.data.ver!=mLexiDB.getDBVersion(mAdapter.data.fid)
						);
			if(pull) {
				loadInAll(a);
				bNeedInvalidate = false;
			} else {
				if (toastV!=null && toastV.getVisibility()==View.VISIBLE) {
					maskOn = true;
					toastTv.startAnimation(fadeAnima);
				}
			}
			if(bNeedInvalidate) {
				//lv.postDelayed(mAdapter::notifyDataSetChanged, 150);
				mAdapter.notifyDataSetChanged();
				long[] pos = savedPositions.get(getFragmentId());
				//CMN.debug("getFragmentId()::", getFragmentId(), pos);
				if (pos != null && pos.length>3) {
					((LinearLayoutManager)lv.getLayoutManager()).scrollToPositionWithOffset((int)pos[pos.length-1], (int)pos[pos.length-2]);
				}
				String foldername;
				if(type==DB_FAVORITE) {
					foldername = mLexiDB.getFavoriteNoteBookNameById(mAdapter.data.fid);
				} else {
					foldername = "历史记录";
				}
				setTitle(foldername);
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		checkColors();
	}
	
	public void checkColors() {
		if(initialized && GlobalOptions.isDark!=isDarkStamp) {
			CMN.Log("dbr_checkColor...");
			ColorFilter cs_dbr_sidbr = null;
			isDarkStamp = GlobalOptions.isDark;
			int AppWhite = Color.WHITE;
			if(isDarkStamp) {
				AppWhite = Color.BLACK;
				cs_dbr_sidbr = GlobalOptions.WHITE;
			}
			UIData.root.setBackgroundColor(AppWhite);
			AppWhite=isDarkStamp?0xff2b4381:Color.WHITE;
			UIData.counter.setTextColor(AppWhite);
			UIData.smallLabel.setTextColor(AppWhite);
			//UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray=0xFF4F7FDF);
			
			for(int i=0;i<UIData.sideBar.getChildCount();i++) {
				View cI = UIData.sideBar.getChildAt(i);
				Drawable bg = cI.getBackground();
				if(bg!=null) {
					bg.setColorFilter(cs_dbr_sidbr);
				}
				if(cI==UIData.toolbarAction1 && cs_dbr_sidbr==null) {
					UIData.toolbarAction1.setColorFilter(GlobalOptions.BLACK);
				} else if(cI instanceof ImageView) {
					((ImageView)cI).setColorFilter(cs_dbr_sidbr);
				}
			}
			if(initialized) {
				dataSetChanged();
				mDecorator.bgPaint.setColor(AppWhite);
				mDecorator.textPaint.setColor(isDarkStamp?0xFFc17d33:Color.RED);
			}
		}
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a!=null && a.MainAppBackground!= MainAppBackground) {
			MainAppBackground =a.MainAppBackground;
			UIData.toolbar.setBackgroundColor(MainAppBackground);
			UIData.bottombar.setBackgroundColor(MainAppBackground);
		}
		int color = a.tintListFilter.sForeground;
		if (ForegroundColor != color) {
			color = ForegroundColor;
			ViewUtils.setForegroundColor(UIData.toolbar, a.tintListFilter);
			ViewUtils.setForegroundColor(UIData.bottombar, a.tintListFilter);
		}
		if (mDialog!=null && mDialog.isShowing()) {
			a.resetStatusForeground(mDialog.getWindow().getDecorView());
		}
	}
	
	protected boolean getAutoRefreshOnAttach() {
		return true;
	}

	@Deprecated
	public void moveSelectedardsToDataBase(LexicalDBHelper toDB) {
		if(Selection.size()==0) {
			show(R.string.noseletion);
			return;
		}
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null) return;
		new AlertDialog.Builder(a)
			.setMessage(getResources().getString(R.string.warn_move, Selection.size(),CMN.unwrapDatabaseName(a.favoriteCon.DATABASE),CMN.unwrapDatabaseName(toDB.DATABASE)))
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
				saveListPosition();
				//final long[] ids = new long[l.size()];
				String sql = "delete from t1 where lex = ? ";
				SQLiteStatement preparedDeleteExecutor = mLexiDB.getDB().compileStatement(sql);
				mLexiDB.getDB().beginTransaction();  //开启事务
				String sql2 = "insert into t1(lex, date) values(?,?)";
				SQLiteStatement preparedInsertExecutor = toDB.getDB().compileStatement(sql2);
				toDB.getDB().beginTransaction();  //开启事务
				try {
					for(Long position1 :Selection) {//移动
						HistoryDatabaseReader reader = mAdapter.getReaderAt((int)(long)position1);
						preparedInsertExecutor.bindString(1, reader.record);
						preparedInsertExecutor.bindLong(2, reader.sort_number);
						if(preparedInsertExecutor.executeInsert()!=-1) {
							preparedDeleteExecutor.bindString(1, reader.record);
							preparedDeleteExecutor.executeUpdateDelete();
						}
					}
					Selection.clear();
					preparedDeleteExecutor.close();
					preparedInsertExecutor.close();
					mLexiDB.getDB().setTransactionSuccessful();  //控制回滚
					toDB.getDB().setTransactionSuccessful();  //控制回滚
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mLexiDB.getDB().endTransaction();  //事务提交
					toDB.getDB().endTransaction();  //事务提交
				}
				restartPaging();
				UIData.counter.setText(0 +"/"+ getItemCount());
			}).show();
	}
	
	public void moveSelectedCardsToFolder(String name, long nid) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(Selection.size()==0) {
			show(R.string.noseletion);
			return;
		}
		if(a==null) return;
		new AlertDialog.Builder(a)
			.setMessage(getResources().getString(R.string.warn_move, Selection.size()
					, mLexiDB.getFavoriteNoteBookNameById(a.opt.getCurrFavoriteNoteBookId())
					, name))
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
				saveListPosition();
				String sql = "UPDATE "+TABLE_FAVORITE_v2+" SET folder=? where id=?";
				SQLiteDatabase database = mLexiDB.getDB();
				SQLiteStatement preparedMoveExecutor = database.compileStatement(sql);
				preparedMoveExecutor.bindLong(1, nid);
				database.beginTransaction();  //开启事务
				try {
					for(Long rowId : Selection) {//移动
						preparedMoveExecutor.bindLong(2, rowId);
						preparedMoveExecutor.execute();
					}
					Selection.clear();
					preparedMoveExecutor.close();
					database.setTransactionSuccessful();  //控制回滚
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					database.endTransaction();  //事务提交
				}
				restartPaging();
				UIData.counter.setText(0 +"/"+ getItemCount());
			}).show();
	}
		

	RashSet<Integer> mSearchResTree = new RashSet<>();
	
	public void setType(MainActivityUIBase a, int type, boolean force) {
		if(this.type!=type || force) {
			if(initialized) {
				if(type==DB_HISTORY) {
					UIData.fastScroller.setHandleBackground(a.mResource.getDrawable(R.drawable.ghour));
					UIData.fastScroller.setBarColor(Color.parseColor("#2b4381"));
					UIData.choosed.setVisibility(View.GONE);
					UIData.changed.setVisibility(View.GONE);
				} else {
					UIData.fastScroller.setHandleBackground(a.mResource.getDrawable(R.drawable.ic_pen));
					UIData.fastScroller.setBarColor(Color.parseColor("#8f8f8f"));
					UIData.choosed.setVisibility(View.VISIBLE);
					UIData.changed.setVisibility(View.VISIBLE);
				}
			} else {
				pendingType=type;
			}
			this.type=type;
		}
	}
	
	public String getEntryAt(int pos) {
		if (pos<0 || pos>=mAdapter.getItemCount()) {
			if (pos==-1) {
				return "←";
			}
			if (pos==mAdapter.getItemCount()) {
				return "→";
			}
		}
		return mAdapter.getReaderAt(pos).record;
	}
	
	class SearchCardsHandler2 extends AsyncTask<String,Integer,Void>{
		SearchCardsHandler2(){
		}

		@Override
		protected Void doInBackground(String... keys) {
			String key = keys[0];
			mSearchResTree.clear();
//			for(int position=0;position<dataAdapter.getCount();position++) {
//				String text;
//				{
//					cr.moveToPosition(position);
//					try {
//						text=cr.getString(0);
//					} catch (Exception e) {
//						text="!!!Error: "+e.getLocalizedMessage();
//					}
//				}
//
//				if(text.contains(key)) {
//					mSearchResTree.insert(position);
//				}
//			}

			return null;
		}

		@Override
		public void onProgressUpdate(Integer... values) {}


		@Override
		public void onPreExecute() {
			UIData.progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPostExecute(Void result) {
			UIData.progressBar.setVisibility(View.GONE);
			if(mSearchResTree.getRoot()!=null)
				lm.scrollToPositionWithOffset(mSearchResTree.minimum(), UIData.toolbar.getHeight());
			UIData.fastScroller.setTree(mSearchResTree);
			UIData.fastScroller.timeLength = mAdapter.displaying.getCount();
			UIData.fastScroller.invalidate();
			dataSetChanged();
			show(R.string.resCount,mSearchResTree.size());
		}

		@Override
		public void onCancelled(){
			Log.i("","doInBackgroundSearchCards onCancelled() called");
		}
	};
	
	boolean inSearch = false;
	private int lastFallBackTarget=-100;
	int SelectionMode;
	/*神之显/隐体系*/
	int revertage=0;
	boolean should_hide_cd1,should_hide_cd2;
	//private boolean isDirty;
	/*hide/show system by a God*/
	//click
	@Override
	public void onClick(final View v) {
		int pos;
		int offset;
		String msg=null;
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		switch(v.getId()) {
			case R.id.tg2://.ver
				boolean ck = ((ToggleButton)v).isChecked();
				UIData.fastScroller.setVisibility(opt.setScrollShown(ck)?View.GONE:View.VISIBLE);
			break;
			case R.id.toolbar_action1:
				v.setActivated(bIsCombinedSearch = !bIsCombinedSearch);
				opt.setIsCombinedSearching(bIsCombinedSearch);
				break;
			case R.id.tools0://pan
				if(!opt.getSelection_Persists())
					Selection.clear();
				opt.setDBMode(0);
				UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode=SelectionMode_pan;
				UIData.sideBar.selectToolView(v);
				dataSetChanged();
				if(v.getTag()==null)
					msg = "点击查词模式";
				else
					v.setTag(null);
				refreshFetchWordUI();
				break;
			case R.id.tools1://选择模式
				opt.setDBMode(3);
				UIData.sideBar.setSCC(opt.getInRemoveMode()?getResources().getColor(R.color.ShallowHeaderBlue):UIData.sideBar.ShelfDefaultGray);
				//if(lastFallBackTarget!=SelectionMode_select)
				//	lastFallBackTarget=SelectionMode;
				lastFallBackTarget=-100;
				if(SelectionMode!=SelectionMode_select) {
					SelectionMode=SelectionMode_select;
					UIData.sideBar.selectToolView(v);
					dataSetChanged();
					if(UIData.counter.getVisibility()!=View.VISIBLE) {
						UIData.counter.setText(Selection.size()+"/"+ getItemCount());
						UIData.counter.setVisibility(View.VISIBLE);
					}
					if(v.getTag()==null)
						msg = "选择模式";//
					else
						v.setTag(null);
					refreshFetchWordUI();
				}
				break;
			case R.id.tools001://peruse
				if(!opt.getSelection_Persists())
					Selection.clear();
				opt.setDBMode(1);
				UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode= SelectionMode_learncard;
				UIData.sideBar.selectToolView(v);
				dataSetChanged();
				if(v.getTag()==null)
					msg = "学习卡片模式";
				else
					v.setTag(null);
				refreshFetchWordUI();
				break;
			case R.id.toolbar_action2://eyedropper
				opt.setDBMode(2);
				UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
				try_exit_selection();
				SelectionMode= SelectionMode_fetchWord;
				UIData.sideBar.selectToolView(v);
				dataSetChanged();
				if(v.getTag()==null)
					msg = "取词模式 - " + (PDICMainAppOptions.dbFetchWord()==2?"点击翻译":"直接取词");
				else
					v.setTag(null);
				refreshFetchWordUI();
				break;
			case R.id.tools3:
				if(Selection.size()==0) {
					show(R.string.noseletion);
					return;
				}
//				final boolean hasKeyBoard = imm.hideSoftInputFromWindow(searchView.getWindowToken(),0);
//				//CMN.show(""+hasKeyBoard); //111
//				if(!hasKeyBoard) searchView.clearFocus();
				AlertDialog d = new AlertDialog.Builder(getActivity())
						.setTitle(getResources().getString(R.string.warn_delete, Selection.size()))
						.setMessage("不可撤销!")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
							lv.suppressLayout(true); // important
							lv.setDragSelectActive(false, lastDragPos);
							//PagingCursorAdapter.simulateSlowIO = true;
							saveListPosition(); // 删除
							String sql = "DELETE FROM "+ getTableName()+" WHERE id = ? ";
							SQLiteDatabase database_mod_delete = mLexiDB.getDB();
							SQLiteStatement preparedDeleteExecutor = database_mod_delete.compileStatement(sql);
							database_mod_delete.beginTransaction();  //开启事务
							try {
								for(Long position:Selection) {//删除记录
									try {
										preparedDeleteExecutor.bindLong(1, position);
										preparedDeleteExecutor.executeUpdateDelete();
									} catch (Exception e) {
										CMN.Log(e);
									}
								}
								Selection.clear();
								preparedDeleteExecutor.close();
								database_mod_delete.setTransactionSuccessful();  //控制回滚
							} catch (Exception e) {
								CMN.Log(e);
							} finally {
								database_mod_delete.endTransaction();  //事务提交
								restartPaging(); // 删除
								UIData.counter.setText(Selection.size()+"/"+ getItemCount());
							}
							//notifyDataSetChanged();
						}).setOnDismissListener(dialog -> {
//							if(hasKeyBoard) {
//								imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//							}
						}).show();
				d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
				break;
			case R.id.choosed: {//choose deck
				if(System.currentTimeMillis()-last_listHolder_tt<150 && revertage==1) {//too fast from last hide
					should_hide_cd1=true;
				}
				a.showChooseFavorDialog(1);
			} break;
			case R.id.bookmark: {
				onLongClick(((ViewGroup)v.getParent()).getChildAt(0));
			} break;
			case R.id.changed: {//change deck
				if(System.currentTimeMillis()-last_listHolder_tt<150 && revertage==2) {//too fast from last hide
					should_hide_cd2=true;
				}
				a.showChooseFavorDialog(2);
			} break;
			case R.id.search:
//				UIData.toolbar.setVisibility(UIData.toolbar.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
//				if(UIData.toolbar.getVisibility()==View.VISIBLE) {
//					inSearch=true;
//					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//				}else {
//					//automatically hides im ,GREAT!
//					inSearch=false;
//				}
//				notifyDataSetChanged();//TODO min DB IO
//				UIData.fastScroller.showBoolMark(inSearch);
				break;
			case R.id.browser_widget14:
				if(mSearchResTree==null || mSearchResTree.getRoot()==null) {}
				else {
					lv.scrollBy(0, +UIData.toolbar.getHeight());
					RBTNode<Integer> searchTmp = mSearchResTree.xxing_samsara(lm.findFirstVisibleItemPosition());
					if(searchTmp==null) {
						show(R.string.endendr);
						lv.scrollBy(0, -UIData.toolbar.getHeight());
						break;
					}
					pos = searchTmp.getKey();
					lm.scrollToPositionWithOffset(pos, UIData.toolbar.getHeight());
					break;
				}
			case R.id.lst_plain:
				//show("lll");
				offset = 0;
				if(inSearch) {
					lv.scrollBy(0, offset = UIData.toolbar.getHeight());
				}
				pos = lm.findFirstVisibleItemPosition()-1;
				if(pos<0) {
					show(R.string.endendr);
				}else
					lm.scrollToPositionWithOffset(pos,offset);
				break;
			//wwwwwwwwww
			case R.id.browser_widget13:
				if(mSearchResTree==null || mSearchResTree.getRoot()==null) {}
				else {
					lv.scrollBy(0, +UIData.toolbar.getHeight());
					RBTNode<Integer> searchTmp1 = mSearchResTree.sxing_samsara(lm.findFirstVisibleItemPosition());
					if(searchTmp1==null) {
						show(R.string.endendr);
						lv.scrollBy(0, -UIData.toolbar.getHeight());
						break;
					}
					pos = searchTmp1.getKey();
					lm.scrollToPositionWithOffset(pos, UIData.toolbar.getHeight());
					break;
				}
			case R.id.nxt_plain:
				//show("nnn");
				offset = 0;
				if(inSearch) {
					lv.scrollBy(0, offset = UIData.toolbar.getHeight());
				}
				pos = lm.findFirstVisibleItemPosition()+1;
				if(pos>=getItemCount()) {
					show(R.string.endendr);
				}else
					lm.scrollToPositionWithOffset(pos,offset);
				break;
			case R.id.browser_widget1:
				if (a.opt.debuggingDBrowser() > 0) {
					TestHelper.PagingVerdict(a, mAdapter.data.dataAdapter, getFragmentId());
				} else {
					UIData.toolbar.getNavigationBtn().performClick();
				}
				break;
		}

		if(msg!=null) {
			a.showTopSnack(UIData.snackRoot, msg, 0.5f, -1, -1, 0);
		}
	}
	
	private void restartPaging() {
		lv.suppressLayout(true);
		mAdapter.rebuildCursor(getMainActivity(), getFragmentId());
		//notifyDataSetChanged();
		//lv.scrollToPosition(0);
	}
	
	void dataSetChanged() {
		if(initialized)mAdapter.notifyDataSetChanged();
	}
	
	private void try_exit_selection() {
		if(SelectionMode==SelectionMode_select)
			if(!opt.getSelection_Persists()) {//清空选择
				Selection.clear();
				UIData.counter.setText(Selection.size()+"/"+ getItemCount());
				UIData.counter.setVisibility(View.GONE);
			}
	}
	//lazy strategy. reuse as much as possible.
	PopupWindow menuPopup;
	int menuResId = -1;
	int onclickBase=0;
	int lastPopupId=-1;
	ArrayAdaptermy<String> shareListAda;
	void initMenuPopup(){
		View view = getActivity().getLayoutInflater().inflate(R.layout.popup_more_tools, null);
		menuPopup = new PopupWindow(view,
				(int)(160 * GlobalOptions.density), LayoutParams.WRAP_CONTENT);
		menuPopup.setOnDismissListener(() -> {
		});
		final ListView shareList = view.findViewById(R.id.share_list);
		shareListAda = new ArrayAdaptermy<>(getActivity(),
				R.layout.popup_list_item);
		shareList.setAdapter(shareListAda);
		shareList.setOnItemClickListener((parent, view1, position, id) -> {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			if(a==null) return;
			switch(position+onclickBase) {//处理点击事件
				case 10://全选
					//TODO develop more efficient and elegant algorithm.
					for(int i = 0; i< getItemCount(); i++) {
						Selection.add(mAdapter.getReaderAt(i).row_id);
					}
					UIData.counter.setText(Selection.size()+"/"+ getItemCount());
					dataSetChanged();
					break;
				case 11:
					Selection.clear();
					UIData.counter.setText(Selection.size()+"/"+ getItemCount());
					dataSetChanged();
					break;
				case 12://反选
					for(int i = 0; i< getItemCount(); i++) {
						long rowId = mAdapter.getReaderAt(i).row_id;
						if(!Selection.remove(rowId))
							Selection.add(rowId);
					}
					
					UIData.counter.setText(Selection.size()+"/"+ getItemCount());
					dataSetChanged();
					break;
				case 13://反向选择-toggle
					if(opt.toggleInRemoveMode()) {
						//main_clister_layout.findViewById(R.id.tools1).getBackground().setColorFilter(Color.parseColor("#FF4081"), PorterDuff.Mode.SRC_IN);
						UIData.sideBar.setSCC(getResources().getColor(R.color.ShallowHeaderBlue));
					}else {
						//main_clister_layout.findViewById(R.id.tools1).getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
						UIData.sideBar.setSCC(UIData.sideBar.ShelfDefaultGray);
					}
					break;
				case 30://show all decks
					break;
				case 31://刷新
					break;
				case 40://更新书签
					// toimpl
//					if(cr!=null && lm.findFirstVisibleItemPosition()>=1 && dataAdapter.getCount()>=0) {
//						if(lv.getChildAt(0)!=null) {
//							String key = String.valueOf(((TextView) lv.getChildAt(0).findViewById(android.R.id.text1)).getText());
//							if(mLexiDB.updateBookMark(key)!=-1)
//								show(R.string.bookmarkupdated, key);
//						}
//					}
					break;
				case 41://跳转书签
					String key = mLexiDB.getLastBookMark();
//					if(key!=null) {
//						int cc=0;
//						for(int i=0;i<dataAdapter.getCount();i++) {
//							String text;
//							{
//								cr.moveToPosition(position);
//								try {
//									text=cr.getString(0);
//								} catch (Exception e) {
//									text="!!!Error: "+e.getLocalizedMessage();
//								}
//							}
//
//							if(text.equals(key)) {
//								lm.scrollToPositionWithOffset(cc, 0);
//								show(R.string.bookmarkjumped, key);
//								return;
//							}
//							cc++;
//						}
//					}
					show(R.string.bookmarkfailed, key);
					break;
				case 50://严格模式
					UIData.fastScroller.setConservativeScroll(true);
					opt.setShelfStrictScroll(true);
					break;
				case 51://宽松模式
					UIData.fastScroller.setConservativeScroll(false);
					opt.setShelfStrictScroll(false);//.putBoolean("strictscroll", false);
					break;
			}
			TextView tv = (TextView) view1;
			tv.setText(tv.getText()+"...");
			getActivity().getWindow().getDecorView().postDelayed(() -> menuPopup.dismiss(), 150);
		});
	}
	
	private int getItemCount() {
		return mAdapter==null?0:mAdapter.getItemCount();
	}
	
	@Override
	public boolean onLongClick(final View v) {
		menuResId = -1;
		onclickBase=0;
		boolean interceptClick = false;
		switch(v.getId()) {
			case R.id.tools1:
				menuResId=R.array.selection_tweak;
				onclickBase=10;
				interceptClick=true;
			break;
			case R.id.choosed:
				menuResId=R.array.choosed_tweak;
				onclickBase=30;
				interceptClick=true;
			break;
			case R.id.bookmark:
				v.performClick();
			return true;
			case R.id.bookmark0:
				menuResId=R.array.bookmark0_tweak;
				onclickBase=40;
				interceptClick=true;
			break;
			case R.id.tg2:
				menuResId=R.array.ver_tweak;
				onclickBase=50;
				interceptClick=true;
			break;
		}
//		if(sharePopup!=null && sharePopup.isShowing()) {
//			sharePopup.dismiss();
//			return true;
//		}
		if(menuPopup ==null)
			initMenuPopup();

		if(lastPopupId!=v.getId()) {//need re-populate
			shareListAda.setArray(Arrays.asList(getResources().getStringArray(menuResId)));
			lastPopupId=v.getId();
		}

		menuPopup.setFocusable(false);
		menuPopup.setOutsideTouchable(true);
		menuPopup.setBackgroundDrawable(null);
		menuPopup.showAsDropDown(v, v.getWidth(), -v.getHeight());
		return interceptClick;
	}

	TextView toastTv;
	View toastV;
	AlphaAnimation fadeAnima;
	public void show(int ResId,Object...args) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a!=null) {
			a.cancelToast();
			show(getResources().getString(ResId,args));
		}
	}
	boolean maskOn;
	float inputBase=-1;
	public resultRecorderCombined rec;
	public void show(String text)
	{
		if(getActivity()==null) return;
		if(toastV == null) {
			toastV = UIData.toastLayoutRootmy;//a.getLayoutInflater().inflate(R.layout.toast,null);
			toastV.setOnTouchListener((v, event) -> {
				if(inputBase==-1)
					maskOn = true;
				return false;
			});
			//toastV.setBackgroundColor(ContextCompat.getColor(a,R.color.colorHeaderBlue));
			toastTv = toastV.findViewById(R.id.message);
			fadeAnima = new AlphaAnimation(1, 0);
			fadeAnima.setDuration(1600);
			fadeAnima.setRepeatCount(0);
			fadeAnima.setFillAfter(true);
			fadeAnima.setInterpolator(input -> {
				if(maskOn) {//提前结束
					inputBase = input;
					maskOn = false;
				}
				if(inputBase!=-1)
					if(inputBase<0.8)
						input = (input-inputBase)+0.8f;
				if(input<0.7f)
					return 0;
				else {
					float ret =  (float) ((input-0.7)*(1/(0.3)));//线性陡升
					if(ret>=1) fadeAnima.cancel();//禁止超射
					return ret;
				}

			});
			fadeAnima.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {inputBase=-1;maskOn=false;}

				@Override
				public void onAnimationEnd(Animation animation) {
					toastV.setVisibility(View.GONE);
					toastTv.setAlpha(1);
				}

				@Override public void onAnimationRepeat(Animation animation) {
					//CMN.show("re");
				}
			});
		}
		toastV.setVisibility(View.VISIBLE);
		toastTv.setText(text);
		toastTv.startAnimation(fadeAnima);
	}

	public final SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
	int avoyagerIdx=0;
	int adelta=0;

	public void resetPageMemorization(){
		avoyager.clear();
		avoyagerIdx=adelta=0;
	}
	
	private void initLongPressSelect(View view, int position) {
		try {
			lv.setDragSelectActive(true, lastDragPos = position);
			if (SelectionMode != SelectionMode_select) {
				int tmpVal = SelectionMode;
				View target = UIData.tools1;
				target.setTag(false);
				target.performClick();
				lastFallBackTarget = tmpVal;
				boolean alreadyselected = Selection.contains(mAdapter.getReaderAt(position).row_id);
				if (!alreadyselected) {
					view.performClick();
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	private String getRowText() {
		ViewUtils.ViewDataHolder<DbCardListItemBinding> vh = (ViewUtils.ViewDataHolder<DbCardListItemBinding>) ViewUtils.getViewHolderInParents(pressedView);
		if (vh!=null) {
			return vh.data.text1.getText().toString();
		}
		return "sunshine";
	}
	
	private void modifyFavLevel(int i) {
		saveListPosition();
		if (mAdapter.data.fid!=-1) {
			HistoryDatabaseReader reader = mAdapter.getReaderAt(pressedRow);
			ContentValues contentValues = new ContentValues();
			int level = reader.fav + i;
			contentValues.put("level", level);
			int ret = LexicalDBHelper.getInstance().getDB().update(TABLE_FAVORITE_v2, contentValues, "id=?", new String[]{"" + reader.row_id});
			CMN.debug("modifyFavLevel", ret);
			getMainActivity().showT("已"+(i>0?"升":"降")+"为"+level+"颗星");
			restartPaging(); // 修改星级
		}
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		try {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			switch (v.getId()) {
				case R.string.start_longpress_sel: {
					initLongPressSelect(pressedView, pressedRow);
				}
				break;
				case R.string.tapSch:
					a.popupWord(getRowText(), null, -1, null);
					break;
				case R.string.page_ucc:
					if (isLongClick) {
						a.copyText(getRowText(), true);
					} else {
						a.getVtk().setInvoker(null, null, null, getRowText());
						a.getVtk().onClick(null);
					}
					break;
				case R.string.quci:
					try {
						currentDisplaying = getRowText();
						quci(mAdapter.getReaderAt(pressedRow), 0);
					} catch (Exception e) {
						CMN.debug(e);
					}
					break;
				case R.string.copy:
					a.copyText(getRowText(), true);
					break;
				case R.string.peruse_mode:
					a.JumpToPeruseModeWithWord(getRowText());
					break;
				case R.string.view:
					if (isLongClick) {
						a.copyText(getRowText(), true);
					} else {
						int prem = SelectionMode;
						SelectionMode = SelectionMode_pan;
						onItemClick(pressedView, -1);
						SelectionMode = prem;
					}
					break;
				case R.string.elevate_fav: {
					modifyFavLevel(1);
				} break;
				case R.string.decrease_fav: {
					modifyFavLevel(-1);
				} break;
			}
			popupMenuHelper.dismiss();
		} catch (Exception e) {
			CMN.debug(e);
		}
		return true;
	}
	
	// longclick
	public boolean onItemLongClick(View view) {
		ViewUtils.ViewDataHolder<DbCardListItemBinding> vh = (ViewUtils.ViewDataHolder<DbCardListItemBinding>) ViewUtils.getViewHolderInParents(view);
		int position = vh.getLayoutPosition();
		//CMN.debug("onItemLongClick", "view = [" + view + "], position = [" + position + "]");
		//if(lastDragPos!=-1)(lv.setDragSelectActive(false, lastDragPos);
		if (PDICMainAppOptions.dbLongPressSelect()) {
			initLongPressSelect(view, position);
			return false;
		} else if(!lv.getDragSelectActive()) {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			if (a == null) return false;
			pressedRow = position;
			pressedView = view;
			PopupMenuHelper popupMenu = a.getPopupMenu();
			boolean b1 = getFragmentId()==-1;
			popupMenu.initLayout(new int[]{
					R.layout.poplist_quci_fuzhi
					, R.string.tapSch
					, R.string.peruse_mode
					, R.string.page_ucc
					, b1?0:R.string.elevate_fav
					, b1?0:R.string.decrease_fav
					//, R.string.copy
					, R.string.start_longpress_sel
			}, this);
			int[] vLocationOnScreen = new int[2];
			view.getLocationOnScreen(vLocationOnScreen); //todo 校准弹出位置
			popupMenu.showAt(view, vLocationOnScreen[0], vLocationOnScreen[1]+view.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
		}
		return true;
	}
	
	// click
	public void onItemClick(View view, int position) {
		ViewUtils.ViewDataHolder<DbCardListItemBinding> vh = (ViewUtils.ViewDataHolder<DbCardListItemBinding>) ViewUtils.getViewHolderInParents(view);
		if(position==-1 && vh!=null)
			position = vh.getLayoutPosition();
		if (position>=0 && position<mAdapter.getItemCount()) {
			MainActivityUIBase a = (MainActivityUIBase) getActivity();
			if (a == null) return;
			/*if (true) */{ // 点灭选区！
				View v = mDialog!=null && mDialog.isShowing()?mDialog.getCurrentFocus():a.getCurrentFocus();
				if (v!=null && v.getClass()==WahahaTextView.class && ((WahahaTextView) v).hasSelection()) {
					v.clearFocus();
					return;
				}
			}
			if (vh != null) {
				adelta = 0;
				currentDisplaying = vh.data.text1.getText()+"";
			}
			int lastClickedPosBefore = position - adelta;
			ScrollerRecord pagerec = null;
			DBListAdapter.HistoryDatabaseReader reader = mAdapter.getReaderAt(currentPos = position);
			long rowId = currentRowId = reader.row_id;
			currentDisplaying = reader.record;
			
			switch (SelectionMode) {
				case SelectionMode_select: {
					if (!Selection.remove(rowId)) {
						Selection.add(rowId);
					}
					UIData.counter.setText(Selection.size() + "/" + getItemCount());
					UIData.counter.setVisibility(View.VISIBLE);
					mAdapter.notifyItemChanged(position);
				}
				break;
				case SelectionMode_pan: {
					//toimpl
					boolean rendered = false;
					{
						String texts = reader.books;
						CMN.Log("复活::", texts);
						if (texts != null) {
							String[] textsArr = texts.split(";");
							if (textsArr.length == 1) {
								rendered = queryAndShowOneDictionary(a.getBookById(IU.parseLong(textsArr[0], -1)), currentDisplaying, position, false);
							} else if (textsArr.length > 1) {
								rendered = queryAndShowMultipleDictionary(textsArr, currentDisplaying, position, false);
							}
						}
						if (rendered) {
							break;
						}
					}
					if (bIsCombinedSearch) {
						rendered = queryAndShowMultipleDictionary(null, currentDisplaying, position, true);
						if (!rendered) {
							if (a.main.getChildCount() == 1) {
								show(R.string.searchFailed, currentDisplaying);
							} else {
								a.show(R.string.searchFailed, currentDisplaying);
								ViewGroup anothorHolder = a.webSingleholder;
								int remcount = anothorHolder.getChildCount() - 1;
								if (remcount > 0) anothorHolder.removeViews(1, remcount);
							}
						}
					}
					else {
						CMN.debug("单独搜索模式");
						rendered = queryAndShowOneDictionary(a.currentDictionary, currentDisplaying, position, true);
						if (!rendered) {
							if (a.main.getChildCount() == 1) {
								show(R.string.searchFailed, currentDisplaying);
							} else {
								a.show(R.string.searchFailed, currentDisplaying);
								ViewGroup anothorHolder = weblistHandler;
								//anothorHolder.removeAllViews();
							}
						}
					}
				}
				break;
				case SelectionMode_learncard: {
					// todo///
					
				}
				break;
				case SelectionMode_fetchWord: {
					quci(reader, -1);
				}
				break;
				default:
					throw new IllegalStateException("Unexpected value: " + SelectionMode);
			}
		}
	}
	
	private void quci(HistoryDatabaseReader reader, int force) {
		EditText tv = null;
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if (a == null) return;
		if(force==-1) force=PDICMainAppOptions.dbFetchWord();
		if (force==2) {
			a.popupWord(currentDisplaying, null, -1, null);
		} else if (force==3) {
			enterPeruseMode(a, reader);
		} else {
			if (a.thisActType == ActType.MultiShare) {
				if (a.peruseView != null) {
					tv = a.peruseView.etSearch;
				} else {
					a.getVtk().setInvoker(null, null, null, currentDisplaying);
				}
			} else {
				a.lastEtString = String.valueOf(a.etSearch.getText());
				tv = a.etSearch;
				a.etSearch_ToToolbarMode(2);
			}
			if (tv != null) {
				tv.setText(currentDisplaying);
			}
			a.DetachDBrowser();
		}
	}
	
	private void enterPeruseMode(MainActivityUIBase a, HistoryDatabaseReader reader) {
		ArrayList<Long> records = new ArrayList<>();
		additiveMyCpr1 datalet = new additiveMyCpr1(currentDisplaying, records);
		ArrayList<additiveMyCpr1> data = new ArrayList<>();
		data.add(datalet);
		String currentDisplaying__ = mdict.replaceReg.matcher(currentDisplaying).replaceAll("").toLowerCase();
		boolean reorded = false;
		if (peruseView != null) {
			dismiss();
		}
		long bid;
		{
			String texts = reader.books;
			CMN.Log("复活::", texts);
			if (texts != null) {
				String[] textsArr = texts.split(";");
				for (String strId : textsArr) {
					if ((bid = IU.parseLong(strId, -1)) >= 0) records.add(bid);
				}
			}
		}
		CMN.Log(records);
		Collection<Long> avoidLet = null;
		
		MainActivityUIBase.LoadManager loadManager = a.loadManager;
		int size = loadManager.md_size;
		
		if (records.size() > 0)
			avoidLet = size >= 32 ? new HashSet<>(records) : records;
		for (int i = 0; i < size; i++) {//联合搜索
			int dIdx = i;
			bid = a.loadManager.getBookIdAt(i);
			if (avoidLet != null && avoidLet.contains(bid)) {
				continue;
			}
			if (opt.getPeruseAddAll()) {
				records.add(bid);
				continue;
			}
			if (!bIsCombinedSearch) {
				if (dIdx == 0)
					if (a.dictPicker.adapter_idx > 0 && a.dictPicker.adapter_idx < size) {
						dIdx = a.dictPicker.adapter_idx;
						reorded = true;
					} else if (reorded) if (dIdx <= a.dictPicker.adapter_idx) {
						dIdx -= 1;
					}
			}
			BookPresenter presenter = a.loadManager.md_get(dIdx);
			{
				int idx = presenter.bookImpl.lookUp(currentDisplaying__);
				if (idx >= 0)
					while (idx < presenter.bookImpl.getNumberEntries()) {
						if (mdict.replaceReg.matcher(presenter.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
							records.add(presenter.getId());
						} else
							break;
						idx++;
					}
			}
		}
		a.JumpToPeruseMode(currentDisplaying, records, -2, true);
	}
	
	private boolean queryAndShowMultipleDictionary(String[] texts, String currentDisplaying, int position, boolean queryAll) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();

		int lastClickedPosBefore = position - adelta;

		ArrayList<Long> records = new ArrayList<>();
		additiveMyCpr1 datalet = new additiveMyCpr1(currentDisplaying,records);
		ArrayList<additiveMyCpr1> data = new ArrayList<>();
		data.add(datalet);
		String currentDisplaying__ = mdict.replaceReg.matcher(currentDisplaying).replaceAll("").toLowerCase();
		a.bShowLoadErr=false;
		long st = CMN.rt();
		if (texts!=null) {
			for(int dIdx=0;dIdx<texts.length;dIdx++) {//联合搜索
				long bid = IU.parseLong(texts[dIdx], -1);
				BookPresenter bookPresenter = a.getBookById(bid);
				{
					long idx = bookPresenter.bookImpl.lookUp(currentDisplaying__);
					if (idx >= 0) {
						if(idx==0 && bookPresenter.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
							bookPresenter.SetSearchKey(currentDisplaying__);
							records.add(bid);
							records.add(idx);
						} else
						while (idx < bookPresenter.bookImpl.getNumberEntries()) {
							if (mdict.replaceReg.matcher(bookPresenter.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
								records.add(bid);
								records.add(idx);
							} else {
								break;
							}
							idx++;
						}
					}
				}
			}
		}
		else {
			for(int dIdx=0;dIdx<a.loadManager.md_size;dIdx++) {//联合搜索
				BookPresenter mdTmp = a.loadManager.md_get(dIdx);
				{
					long idx = mdTmp.bookImpl.lookUp(currentDisplaying__);
					if (idx >= 0)
						while (idx < mdTmp.bookImpl.getNumberEntries()) {
							if (mdict.replaceReg.matcher(mdTmp.bookImpl.getEntryAt(idx)).replaceAll("").toLowerCase().equals(currentDisplaying__)) {
								records.add(a.loadManager.getBookIdAt(dIdx));
								records.add(idx);
							} else
								break;
							idx++;
						}
				}
			}
		}
		CMN.Log("联合搜索 - 同步延时 : ", CMN.elapsed(st));
		a.bShowLoadErr=true;
		
		boolean bUseMergedUrl = true;
		CMN.Log("SelectionMode_pan", records.size());
		if(records.size()>0) {
			setUpContentView();
			//weblistHandler.setViewMode(WEB_LIST_MULTI, bUseMergedUrl, null);
			//yyy
			a.recCom = rec = new resultRecorderCombined(a,data, null);
			ScrollViewmy WHP = (ScrollViewmy) weblistHandler.getScrollView();
//			ScrollerRecord pagerec = null;
//			OUT:
//			if(adelta!=0 && System.currentTimeMillis()-a.lastClickTime>300) {//save our postion
//				pagerec = avoyager.get(lastClickedPosBefore);
//				if (pagerec == null) {
//					if (WHP.getScrollY() != 0) {
//						pagerec = new ScrollerRecord();
//						avoyager.put(lastClickedPosBefore, pagerec);
//					} else
//						break OUT;
//				}
//				pagerec.set(0, WHP.getScrollY(), 1);
//			}
//
//			adelta=0;
//			a.lastClickTime=System.currentTimeMillis();
//
//			pagerec = avoyager.get(position);
//			if (pagerec != null) {
//				rec.expectedPos = pagerec.y;
//				//currentDictionary.mWebView.setScrollY(currentDictionary.expectedPos);
//				//CMN.Log("取出旧值", combining_search_result.expectedPos, pos, avoyager.size());
//			} else {
//				rec.expectedPos = 0;
//				//CMN.Log("新建", combining_search_result.expectedPos, pos);
//			}
			imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
			
			weblistHandler.popupContentView(null, currentDisplaying__);
			
			weblistHandler.bShowInPopup = true;
			weblistHandler.bMergeFrames = 1;//a.mergeFrames();
			rec.renderContentAt(0, a,null, weblistHandler);
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean queryAndShowOneDictionary(BookPresenter currentDictionary, String currentDisplaying, int position, boolean queryAll) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		
		float desiredScale=-1;
		a.TransientIntoSingleExplanation();
		
		String key = currentDisplaying;
		int offset = mdict.offsetByTailing(key);
		int idx;
		if(offset>0)
			key = key.substring(0,key.length()-offset);
		if (currentDictionary.getIsWebSch()) {
			currentDictionary.SetSearchKey(key);
			idx = 0;
		} else {
			idx = currentDictionary.bookImpl.lookUp(key,true);
		}
		int adapter_idx=a.dictPicker.adapter_idx;
		
		if(idx<0 && queryAll) {
			for(adapter_idx=0;adapter_idx<a.loadManager.md_size;adapter_idx++) {
				if(adapter_idx!=a.dictPicker.adapter_idx) {
					currentDictionary = a.loadManager.md_get(adapter_idx);
					idx=currentDictionary.bookImpl.lookUp(key,true);
					if(idx>=0) break;
				}
			}
		}
		
		if(idx>=0) {
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
			try {
				SparseArray<ScrollerRecord> avoyager = webview.presenter.avoyager;
				ViewGroup webviewHolder = weblistHandler.getViewGroup();
				if(System.currentTimeMillis()-a.lastClickTime>300 && webviewHolder.getChildCount()!=0) {
					//save our postion
					View child = webviewHolder.getChildAt(0);
					BookPresenter book = ((WebViewmy)child.findViewById(R.id.webviewmy)).presenter;
					{
						if (adelta!=0 && webview != null && !webview.isloading) {
							if (webview.webScale == 0)
								webview.webScale = a.dm.density;//sanity check
							CMN.Log("dbrowser::保存位置::", book.getDictionaryName(), (int) webview.currentPos);
							pPos = avoyager.get((int) webview.currentPos);
							if (pPos == null
								&& (webview.getScrollX() != 0 || webview.getScrollY() != 0
									|| webview.webScale != BookPresenter.def_zoom)) {
									avoyager.put((int) webview.currentPos, pPos = new ScrollerRecord());
							}
							if (pPos!=null) {
								pPos.set(webview.getScrollX(), webview.getScrollY(), webview.webScale);
							}
						}
					}
				}

				adelta=0;
				a.lastClickTime=System.currentTimeMillis();
				
				pPos = currentDictionary.avoyager.get(idx);
				//a.showT(""+currentDictionary.expectedPos);
			} catch (Exception e) {
				CMN.debug(e);
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
			
			imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
			
			weblistHandler.popupContentView(null, key);
			
			UniversalDictionaryInterface book = currentDictionary.bookImpl;
			if(offset>0)//apply tailing offset
				if(book.getEntryAt(idx+offset).equals(key))
					idx+=offset;
			int tmpIdx=idx;
			while(tmpIdx+1< book.getNumberEntries() && mdict.processText(book.getEntryAt(tmpIdx)).equals(mdict.processText(book.getEntryAt(tmpIdx+1)))) {
				tmpIdx++;
				if(book.getEntryAt(tmpIdx).trim().equals(key.trim())) {
					idx=tmpIdx;
					break;
				}
			}
			
			ViewGroup webviewHolder = weblistHandler.getViewGroup();
			ViewUtils.addViewToParent(someView, webviewHolder);
			if(webviewHolder.getChildCount()>1) {
				for(int i=webviewHolder.getChildCount()-1;i>=0;i--)
					if(webviewHolder.getChildAt(i)!=someView) webviewHolder.removeViewAt(i);
			}
			
			currentDictionary.renderContentAt(desiredScale,RENDERFLAG_NEW,0,webview, idx);
			webview.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			
			contentUIData.PageSlider.setWebview(webview, null);
			someView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			return true;
		}
		else {
			return false;
		}
	}
	
	public View favoriteBtn() {
		return contentUIData.browserWidget8;
	}
	
	/** see {@link #try_goBack} */
	public void toggleFavor() {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if (a == null) return;
		if(type==DB_FAVORITE) {
			if (toDeleteV2.remove(currentRowId)) {
				favoriteBtn().setActivated(true);
				a.showTopSnack(R.string.added);
			} else {
				favoriteBtn().setActivated(false);
				toDeleteV2.add(currentRowId);
				isToDel = true;
				// 提示待移除
				a.showTopSnack(R.string.toRemove);
			}
			mAdapter.notifyItemChanged(currentPos);
		} else {
			String text = currentDisplaying;
			if(a.GetIsFavoriteTerm(text)) {//删除
				a.removeFavoriteTerm(text);
				favoriteBtn().setActivated(false);
				a.showT(text+" "+a.mResource.getString(R.string.removed));
			} else {//添加
				if (a.favoriteCon.insert(a, text, opt.getCurrFavoriteNoteBookId(), weblistHandler) >= 0) {
					favoriteBtn().setActivated(true);
					a.showT(text + " " + a.mResource.getString(R.string.added));
				} else {
					a.showT("收藏失败！");
				}
			}
		}
	}

	public void NavList(int delta) {
		if (weblistHandler!=null) {
			if (delta < 0) {
				MainActivityUIBase a = (MainActivityUIBase) getActivity();
				if (a == null) return;
				if (!weblistHandler.bottomNavWeb()) {
					if (currentPos - 1 < 0) {
						a.showTopSnack(R.string.endendr);
						return;
					}
					//int first = lm.findFirstVisibleItemPosition();
					if (currentPos < lm.findFirstVisibleItemPosition())
						lm.scrollToPositionWithOffset(currentPos, 0);
					adelta = -1;
					onItemClick(null, --currentPos);
				} else {
					weblistHandler.NavWeb(-1);
				}
			}
			else {
				MainActivityUIBase a = (MainActivityUIBase) getActivity();
				if(a==null) return;
				if(!weblistHandler.bottomNavWeb()) {
					if (currentPos + 1 > getItemCount() - 1) {
						a.showTopSnack(R.string.endendr);
						return;
					}
					currentPos += 1;
					int last = lm.findLastVisibleItemPosition();
					boolean hei = false;
					if (currentPos == last) {
						if (lv.getChildAt(last) != null) {
							hei = lv.getHeight() - lv.getChildAt(last).getTop() < lv.getChildAt(last).getHeight() * 2 / 3;
						}
					} else
						hei = currentPos > last;
					
					if (hei) {
						lm.scrollToPositionWithOffset(currentPos, 0);
					}
					adelta = 1;
					onItemClick(null, currentPos);
				} else {
					weblistHandler.NavWeb(1);
				}
			}
		}
	}

	public String getTableName() {
		return type==DB_FAVORITE?TABLE_FAVORITE_v2:TABLE_HISTORY_v2;
	}
	
	public int getFragmentType() {
		return type;
	}
	
	public long getFragmentId() {
		return type==DB_FAVORITE?opt.getCurrFavoriteNoteBookId():-1;
	}
	
	
	@NonNull
	public final MainActivityUIBase getMainActivity() {
		return (MainActivityUIBase) getActivity();
	}
	
	SimpleDialog mDialog;
	ViewGroup root;
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		CMN.Log("DBrowser----->onCreateDialog");
		if(mDialog==null){
			mDialog = new SimpleDialog(requireContext(), getTheme());
			
			mDialog.mBCL = new SimpleDialog.BCL(){
				@Override
				public void onBackPressed() {
					//NavList(-1);
					if (getMainActivity().DBrowser==DBroswer.this) {
						getMainActivity().DBrowser=null;
					}
				}
				@Override
				public void onActionModeStarted(ActionMode mode) {
					getMainActivity().onActionModeStarted(mode, mDialog);
				}
				@Override
				public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
					switch (keyCode) {
						case KeyEvent.KEYCODE_VOLUME_DOWN: {
							if(opt.getPeruseUseVolumeBtn()) {
								contentUIData.browserWidget11.performClick();
								return true;
							}
						}
						case KeyEvent.KEYCODE_VOLUME_UP: {
							if(opt.getPeruseUseVolumeBtn()) {
								contentUIData.browserWidget10.performClick();
								return true;
							}
						}
					}
					return false;
				}
			};
		}
		//else CMN.Log("复用dialog");
		Window win = mDialog.getWindow();
		if(win!=null){
			win.setWindowAnimations(com.knziha.filepicker.R.style.fp_dialog_animation);
			
			ViewGroup content = win.findViewById(android.R.id.content);
			if(content!=null) {
				root=content;
			}
			Toastable_Activity.setStatusBarColor(win, getMainActivity().MainAppBackground);
			//win.setStatusBarColor(CMN.MainBackground);
			View view = win.getDecorView();
			view.setBackground(null);
			
			WindowManager.LayoutParams layoutParams = win.getAttributes();
			layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.horizontalMargin = 0;
			layoutParams.verticalMargin = 0;
			layoutParams.dimAmount = 0;
			win.setAttributes(layoutParams);
			
			Toastable_Activity.setWindowsPadding(view);
			
			View t = win.findViewById(android.R.id.title);
			if(t!=null) t.setVisibility(View.GONE);
			int id = Resources.getSystem().getIdentifier("titleDivider","id", "android");
			if(id!=0){
				t = win.findViewById(id);
				if(t!=null) t.setVisibility(View.GONE);
			}
			if(t!=null) t.setVisibility(View.GONE);
		}
		//ViewUtils.ensureWindowType(mDialog, getMainActivity(), this);
		return mDialog;
	}
	
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		MainActivityUIBase a = (MainActivityUIBase) getActivity();
		if(a==null) return true;
		int id = item.getItemId();
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:a.getDummyMenuImpl(id);
		boolean isLongClicked = mmi.isLongClicked;
		/* 长按事件默认不处理，因此长按时默认返回false，且不关闭menu。 */
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		String msg = null;
		switch(id) {
			case R.id.back: {
				if (isLongClicked) {
					ret = true;
					item.setEnabled(!item.isEnabled());
				} else {
					dismiss();
				}
			} break;
			case R.id.settings: {
				a.launchSettings(History.id, 0);
			} break;
			case R.id.text: {
				boolean v = !mmi.isChecked();
				mmi.setChecked(v);
				PDICMainAppOptions.dbTextSelectable(v);
				dataSetChanged();
				if (v) {
					a.showTopSnack(UIData.snackRoot, "自由选择列表中的文本", 0.5f, -1, -1, 0);
				} else {
					a.showTopSnack(UIData.snackRoot, "", 0.5f, -1, -1, 0);
				}
			} break;
			case R.id.tapSch: {
				opt.setDBMode(SelectionMode = SelectionMode_fetchWord);
				PDICMainAppOptions.dbFetchWord(2);
				refreshFetchWordUI();
				msg = "取词模式 - " + "点击翻译";
			} break;
			case R.id.peruse: {
				opt.setDBMode(SelectionMode = SelectionMode_fetchWord);
				PDICMainAppOptions.dbFetchWord(3);
				refreshFetchWordUI();
				msg = "取词模式 - " + "翻阅模式";
			} break;
			case R.id.fetchWord: {
				opt.setDBMode(SelectionMode = SelectionMode_fetchWord);
				PDICMainAppOptions.dbFetchWord(1);
				refreshFetchWordUI();
				msg = "直接取词";
			} break;
			case R.id.learnCard: {
				opt.setDBMode(SelectionMode = SelectionMode_learncard);
				refreshFetchWordUI();
				msg = "学习卡片模式";
			} break;
			case R.id.icon: {
				item.setChecked(!item.isChecked());
				PDICMainAppOptions.dbShowIcon(item.isChecked());
				dataSetChanged();
			} break;
			case R.id.longPressSelect: {
				item.setChecked(!item.isChecked());
				PDICMainAppOptions.dbLongPressSelect(item.isChecked());
				a.showT(item.isChecked()?"长按列表直接开启选择模式":"长按列表弹出菜单");
			} break;
		}
		if(closeMenu)
			a.closeIfNoActionView(mmi);
		if (msg != null) {
			a.showTopSnack(UIData.snackRoot, msg, 0.5f, -1, -1, 0);
		}
		return ret;
	}
	
	private void refreshFetchWordUI() {
		boolean b1 = SelectionMode == SelectionMode_fetchWord;
		menuPeruse.setChecked(b1 && PDICMainAppOptions.dbFetchWord()==3);
		menuTapsch.setChecked(b1 && PDICMainAppOptions.dbFetchWord()==2);
		menuFetchWord.setChecked(b1 && PDICMainAppOptions.dbFetchWord()==1);
		menuLearnCard.setChecked(SelectionMode == SelectionMode_learncard);
		if (UIData.sideBar.getSelectedToolIdx()!=SelectionMode) {
			UIData.sideBar.selectToolIndex(SelectionMode);
		}
	}
	
	WebViewListHandler weblist;
	private boolean lastShowType;
	/** @return 0=nothing  1=dialog 2=view  */
	public int preShow(WebViewListHandler wlh) {
		if (weblist != wlh) {
			weblist = wlh;
		}
		boolean dialog = wlh.a.isFloating() || wlh.src==SearchUI.Fye.MAIN || wlh.bShowingInPopup;
		//if (true) dialog = true;
		final boolean visible = UIData != null && UIData.getRoot().getParent() == wlh.a.mainF
				|| (mDialog != null && mDialog.isShowing());
		if (lastShowType != dialog) {
			lastShowType = dialog;
			if (visible) {
				detach();
			}
			if (!dialog && UIData!=null) {
				UIData.getRoot().setPadding(0,0,0,0);
			}
		} else if (visible) {
			if (dialog) {
				ViewUtils.ensureTopmost(mDialog, getMainActivity(), this);
			}
			return 0;
		}
		return dialog?1:2;
	}
	
	public void detach() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		} else {
			try {
				getFragmentManager()
						.beginTransaction()
						.remove(this)
						.commit();
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		ViewUtils.removeView(getView());
	}
	
	
	private MenuItem menuLearnCard;
	private MenuItem menuFetchWord;
	private MenuItem menuTapsch;
	private MenuItem menuPeruse;
	
	public void setFetchWord(int mode) {
		if (mode == -1) {
			mode = PDICMainAppOptions.dbFetchWord();
		}
		int fetchWord = SelectionMode == 2 ? PDICMainAppOptions.dbFetchWord() : 0;
		if (fetchWord != mode) {
			int newMode;
			if (mode > 0) {
				newMode = 2;
				PDICMainAppOptions.dbFetchWord(mode);
			} else {
				newMode = 0;
			}
			if (SelectionMode != newMode) {
				opt.setDBMode(SelectionMode = newMode);
				if (UIData.sideBar.selectedTool!=UIData.sideBar.getChildAt(newMode)) {
					UIData.sideBar.getChildAt(newMode).performClick();
				}
			}
		} else if (mode==0 && SelectionMode!=0) {
			UIData.sideBar.getChildAt(0).performClick();
		}
	}
}

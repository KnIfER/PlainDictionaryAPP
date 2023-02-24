package com.knziha.plod.dictionarymanager;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.PlainUI.PasteBinHub;
import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedWriter;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.MagentTransient;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CharSequenceKey;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.FIlePickerOptions;
import com.knziha.plod.widgets.TextMenuView;
import com.knziha.plod.widgets.ViewUtils;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class BookManagerMain extends BookManagerFragment<BookPresenter>
		implements BookManagerFragment.SelectableFragment, OnItemLongClickListener, DragSortListView.DropListener, View.OnClickListener, View.OnLongClickListener {
	HashSet<PlaceHolder> Selection = new HashSet<>();
	BookManager aaa;
	private boolean bDictTweakerOnceShowed;
	public MainActivityUIBase.LoadManager loadMan;
	AlertDialog d;
	private Drawable mActiveDrawable;
	private Drawable mFilterDrawable;
	private Drawable mAudioDrawable;
	private Drawable mWebDrawable;
	private Drawable mPDFDrawable;
	private Drawable mRightDrawable;
	private boolean tweakedDict;
	
	public BookManagerMain(){
		super();
		checkChanged=(buttonView, isChecked) -> {
			try {
				BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(buttonView, BookViewHolder.class);
				int pos = vh.position;
				ListView lv = (ListView) ViewUtils.getParentByClass(vh.itemView, ListView.class);
				if (lv != this.listView) {
					pos = filtered.keyAt(pos);
				}
				if (lastClickedPos[lastClickedPosIndex % 2] != pos) {
					lastClickedPos[(++lastClickedPosIndex) % 2] = pos;
				}
				setPlaceSelected(pos, !getPlaceSelected(pos));
				if (ViewUtils.getParentOf(vh.itemView, ListView.class) != listView) {
					dataSetChangedAt(pos);
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		};
	}
	
	public void refreshSize(){
		a.mTabLayout.getTabAt(0).setText(getResources().getString(R.string.currentPlan,loadMan.lazyMan.chairCount));
		ViewUtils.setForegroundColor(a.mTabLayout, a.tintListFilter); // todo avoid
	}
	
	@Override
	public void setListAdapter() {
		adapter = new MyAdapter(loadMan.md);
		setListAdapter(adapter);
		if(a!=null)  ViewUtils.restoreListPos(listView, BookManager.listPos[a.fragments.indexOf(this)]);
	}

	@Override
	public DragSortController buildController(DragSortListView dslv) {
		return new MyDSController(dslv);
	}

	public void add(String mmTmp) {
		markDirty(-1);
		loadMan.md.add(null);
		loadMan.lazyMan.placeHolders.add(new PlaceHolder(mmTmp));
		loadMan.lazyMan.chairCount++;
		dataSetChanged(true);
		refreshSize();
	}

	@Override
	public boolean exitSelectionMode() {
		if((this!=getBookManager().f1 || PDICMainAppOptions.dictManager1MultiSelecting()) && selected_size()>0){
			for (PlaceHolder ph : Selection) {
				setPlaceSelectedInter(ph, false);
			}
			Selection.clear();
			dataSetChanged(false);
//			for (int i = 0; i < loadMan.md.size(); i++) {
//				setPlaceSelected(i, false);
//			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (position >= listView.getHeaderViewsCount()) {//词典选项
			pressedPos = position - listView.getHeaderViewsCount();
			if (view!=null) pressedV = view;
			if (PDICMainAppOptions.dictManagerClickPopup() && false) {
				boolean start = mController.startDrag(position,0, view.getHeight()/2);
				return false;
			} else {
				if (PDICMainAppOptions.dictManagerClickPopup()) {
					PopupMenuHelper popupMenu = getPopupMenu();
					popupMenu.getListener().onMenuItemClick(popupMenu, getBookManager().anyView(R.string.more_actions), false);
				} else {
					showPopup(view, null);
				}
			}
		}
		return true;
	}
	
	public void performLastItemLongClick() {
		if(adapter.getCount()>0){
			int idx = lastClickedPos[(lastClickedPosIndex+1)%2];
			int hc = listView.getHeaderViewsCount();
			// CMN.debug("performLastItemLongClick::", idx, hc);
			if(idx<hc||idx>=adapter.getCount()){
				idx = hc;
				BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(listView.getChildAt(0), BookViewHolder.class);
				if (vh != null) {
					idx = vh.position + hc;
				}
			}
			onItemLongClick(null, null, idx, 0);
		}
	}
	
	@Override
	DragSortListView.DropListener getDropListener() {
		return this;
	}
	
	@Override
	public void drop(int from, int to) {
		CMN.debug("drop", "from = [" + from + "], to = [" + to + "]");
		//if(true) return;
		boolean b1 = to<0;
		if(b1) to=-to;
		int pos=-1, top=0;
		BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(listView.getChildAt(0), BookViewHolder.class);
		if (vh != null) {
			pos = vh.position;
			top = ViewUtils.getNthParentNonNull(vh.itemView, 1).getTop();
		}
		int cc=0, initialTo=to;
		String fromPath = getPathAt(from);
		if(a.opt.dictManager1MultiSelecting() && (getPlaceSelected(from) || b1)){
			ArrayList<BookPresenter> md_selected = new ArrayList<>(selected_size());
			ArrayList<PlaceHolder> ph_selected = new ArrayList<>(selected_size());
			if(to>from || b1) to++;
			for (int i = manager_group().size()-1; i >= 0; i--) {
				if(getPlaceSelected(i)){
					md_selected.add(0, loadMan.md.remove(i));
					ph_selected.add(0, loadMan.lazyMan.placeHolders.remove(i));
					if(i<to) {
						to--;
					}
					if (i < pos) {
						pos--;
					}
					cc++;
				}
			}
			loadMan.md.addAll(to, md_selected);
			loadMan.lazyMan.placeHolders.addAll(to, ph_selected);
			dataSetChanged(true);
		}
		else if (from != to && !b1) {
			replace(from, to);
			dataSetChanged(true);
			if (from < pos) {
				pos--;
			}
			cc = 1;
		}
		if (pos>=0) {
			listView.setSelectionFromTop(pos + listView.getHeaderViewsCount(), top);
		}
		if (a.accessMan.isEnabled()) {
			if (cc > 0) {
				int finalCc = cc, fvp = 0;
				for (int i = 0, sz = manager_group().size(); i < sz; i++) {
					if (getPathAt(i).equals(fromPath)) {
						fvp = i;
						break;
					}
				}
				View child = ViewUtils.findChild(listView, fvp + listView.getHeaderViewsCount());
				if (child != null) {
					((BookViewHolder) child.getTag()).handle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
				}
				a.accessMan.interrupt();
				a.root.postDelayed(() -> {
					a.accessMan.interrupt();
					a.root.announceForAccessibility("已拖拽 " + finalCc + "本词典，从" + from + "到" + initialTo);
				}, 250);
			} else {
				View child = ViewUtils.findChild(listView, from+listView.getHeaderViewsCount());
				if (child != null) {
					((BookViewHolder)child.getTag()).handle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
				}
			}
		}
	}
	
	public void deleteSelOrOne(boolean one, boolean useFilter) {
		int szf1 = manager_group().size();
		int selSz = 1;
		if (!one) {
			if (!useFilter) {
				selSz = Selection.size();
			} else {
				selSz = 0;
				for (int i = szf1 - 1; i >= 0; i--) {
					if (getPlaceSelected(i) && filtered.get(i)!=null) {
						selSz++;
					}
				}
			}
		}
		new AlertDialog.Builder(getBookManager())
				.setWikiText("可长按弹出菜单中的“禁用”达到同样效果", null)
				.setTitle(getBookManager().mResource.getString(R.string.surerrecords, selSz).replace("彻底", ""))
				.setMessage("从当前分组中删除记录，注意不可撤销。")
				.setPositiveButton(R.string.confirm, (dialog, which) -> {
					int cc=0;
					if (one) {
						remove(pressedPos);
						cc=1;
					} else {
						for (int i = szf1 - 1; i >= 0; i--) {
							if (getPlaceSelected(i) && (!useFilter || filtered.get(i)!=null)) {
								remove(i);
								cc++;
							}
						}
					}
					refreshSize();
					dataSetChanged(cc > 0);
					dialog.dismiss();
				})
				.create().show();
	}
	
	
	private View.AccessibilityDelegate acessAgent = new View.AccessibilityDelegate() {
		@Override
		public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
			super.onPopulateAccessibilityEvent(host, event);
			try {
				if (host.getId() == R.id.text) {
					BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(host, BookViewHolder.class);
					if (vh != null) {
						if (getPlaceSelected(vh.position)) {
							event.getText().add("已选中");
						}
						BookPresenter magent = getMagentAt(vh.position);
						if (magent.getIsDedicatedFilter()) {
							event.getText().add("已设为点击翻译词库");
						}
						if (magent.getAutoFold()) {
							event.getText().add("默认折叠");
						}
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	};
	
	
	@Override
	public boolean onLongClick(View v) {
		try {
			if (v.getId() == R.id.text) {
				BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(v, BookViewHolder.class);
				if (vh != null) {
					ListView lv = ((ListView) ViewUtils.getParentByClass(vh.itemView, ListView.class));
					return lv.getOnItemLongClickListener().onItemLongClick(listView, vh.itemView, vh.position + lv.getHeaderViewsCount(), 0);
				}
				return true;
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return false;
	}
	
	// click
	@Override
	public void onClick(View v) {
		try {
			if (v.getId() == R.id.text) {
				BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(v, BookViewHolder.class);
				if (vh != null) {
					ListView lv = ((ListView) ViewUtils.getParentByClass(vh.itemView, ListView.class));
					lv.getOnItemClickListener().onItemClick(listView, vh.itemView, vh.position + lv.getHeaderViewsCount(), 0);
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	private class MyAdapter extends ArrayAdapter<BookPresenter> {
		public MyAdapter(List<BookPresenter> mdicts) {
			super(getActivity(), getItemLayout(), R.id.text, mdicts);
		}

		@NonNull
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			BookViewHolder vh;
			if (parent != listView && convertView!=null) {
				if (!(convertView.getTag() instanceof BookViewHolder)) {
					CMN.debug("他乡异客");
					convertView = null;
				}
			}
			boolean access = a.accessMan.isEnabled();
			if(convertView==null){
				convertView = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(), parent, false);
				vh = new BookViewHolder(convertView);
				if(parent != listView) {
					ViewUtils.setVisible(vh.handle, false);
					convertView.setBackground(null);
				}
//				if (parent==listView) {
					vh.title.setOnClickListener(BookManagerMain.this);
					vh.title.setOnLongClickListener(BookManagerMain.this);
//				}
				vh.title.setAccessibilityDelegate(acessAgent);
				vh.title.setMaxLines(1);
				vh.title.trimStart = false;
				vh.title.earHintAhead = "词典";
				if (access) {
					ViewUtils.removeView(vh.handle);
					ViewUtils.addViewToParent(vh.handle, (ViewGroup) vh.itemView, 1);
				}
			} else {
				vh = (BookViewHolder) convertView.getTag();
			}
			vh.handle.setFocusable(access);
			vh.title.setClickable(access);
			vh.title.setLongClickable(access);
			vh.position=position;
			//v.getBackground().setLevel(1000);
			//position = position - mDslv.getHeaderViewsCount();
			//mngr_agent_manageable mdTmp = adapter.getItem(position);
			
			if (parent == listView) {
				if(query!=null && filtered.get(position)!=null)
					vh.title.setBackgroundResource(GlobalOptions.isDark?R.drawable.xuxian2_d:R.drawable.xuxian2);
				else
					vh.title.setBackground(null);
				if(ViewUtils.setVisibleV4(vh.handle, PDICMainAppOptions.sortDictManager()?0:1)) {
					((ViewGroup.MarginLayoutParams)vh.title.getLayoutParams()).leftMargin = PDICMainAppOptions.sortDictManager()?0: (int) (GlobalOptions.density * 6);
				}
				if(GlobalOptions.isDark) {
					convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
				}
			}

			final String key = getPathAt(position);
			final String name = CMN.getAssetName(key);
			final String suffix = CMN.getSuffix(key);

			if(a.opt.dictManager1MultiSelecting()){
				vh.ck.setVisibility(View.VISIBLE);
				vh.ck.setOnCheckedChangeListener(null);
				vh.ck.setChecked(getPlaceSelected(position));
				vh.ck.setOnCheckedChangeListener(checkChanged);
				vh.tweakCheck();
			} else {
				vh.ck.setVisibility(View.GONE);
			}

			int color = 0xaaaaaa; //一样的亮兰色aafafa
			boolean disabled = getPlaceRejected(position);
			if(!disabled)
				color = GlobalOptions.isDark?0xEEEEEE:0x000000;
			vh.title.leftDrawableAlpha = disabled?127:255;
			if(!key.startsWith("/ASSET") && !new File(key).exists()) {
				// if(GlobalOptions.isDark && !disabled) color = 0x888888;
				if(GlobalOptions.isDark) color = 0xaaaaaa;
				color = 0xff0000 | (color>>8);
			}
			vh.title.setTextColor(0xff000000 | color);
			vh.title.setPadding((int) (GlobalOptions.density*25),0,0,0);
			
			//int tmpFlag = getPlaceFlagAt(position);
			
			BookPresenter magent = getMagentAt(position);
			
			Drawable mLeftDrawable=null;
//			if(PDICMainAppOptions.getTmpIsFiler(tmpFlag)){
//				mLeftDrawable=mFilterDrawable;
//			}
//			if(PDICMainAppOptions.getTmpIsAudior(tmpFlag)){
//				mLeftDrawable=mAudioDrawable;
//			}
			if(suffix.equals(".web")){
				mLeftDrawable=mWebDrawable;
			}
			else if(suffix.equals(".pdf")){
				mLeftDrawable=mPDFDrawable;
			}
			
			BookPresenter thereYouAre = a.app_mdict_cache.get(new File(key).getName());
			
			vh.title.setCover(thereYouAre==null?null:thereYouAre.getCover());
			
			vh.title.setCompoundDrawables(mActiveDrawable,
					mLeftDrawable,
					magent.getIsDedicatedFilter()?mRightDrawable:null,
					magent.getAutoFold()?"<>":null);
			
			if (disabled) {
				vh.title.setText("// "+name);
			} else {
				vh.title.setText(name);
			}
			
			vh.title.setStarLevel(0);
			return convertView;
		}
	}
	
	public PopupMenuHelper getPopupMenu() {
		if (mPopup==null) {
			mPopup  = new PopupMenuHelper(getActivity(), null, null);
			mPopup.initLayout(new int[]{
					R.layout.poplist_shewei_gouciku
			}, new PopupMenuHelper.PopupMenuListener() {
				@Override
				public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View view, boolean isLongClick) {
					int position = pressedPos;
					ListView lv = (ListView) ViewUtils.getParentOf(pressedV, ListView.class);
					boolean b1 = lv!=listView;
					boolean isOnSelected = !b1 && getPlaceSelected(position);
					BookPresenter magent;
					if (isLongClick) { // 长按
						if (view.getId() == R.id.disable) {
							deleteSelOrOne(!getPlaceSelected(position), b1);
							popupMenuHelper.dismiss();
						}
						if (view.getId() == R.id.move_sel && !b1) {
							// 移动选中项至此
							ArrayList<mFile> paths = new ArrayList<>();
							for (int i = 0, sz = manager_group().size(); i < sz; i++) {
								if (getPlaceSelected(i)) {
									paths.add(new mFile(getPathAt(i)));
								}
							}
							a.addElementsToF1(null, paths.toArray(new mFile[0]), false, true, pressedPos+1, null);
							popupMenuHelper.dismiss();
						}
						return false;
					}
					switch (view.getId()) {
						/* 启用 禁用 */
						case R.id.enable:
						case R.id.disable:  {
							disEna(getPlaceSelected(position), view.getId()==R.id.disable, position, b1);
						} break;
						case R.string.rename: {
							//renameFile();
						} break;
						case R.id.move_sel: {
							if(b1) return true;
							int from = -1;
							if (PDICMainAppOptions.dictManager1MultiSelecting()) {
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										from = i;
										break;
									}
								}
							}
							if (from == -1) {
								a.showT("无操作");
							} else {
								drop(from, -pressedPos);
							}
						} break;
						case R.string.multi_select: {//多选
							a.opt.dictManager1MultiSelecting(!a.opt.dictManager1MultiSelecting());
							dataSetChanged(false);
							a.f2.dataSetChanged(false);
							d.dismiss();
						} break;
						case R.id.jianxuan: {//间选
							if (b1) {
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (filtered.get(i)!=null) {
										setPlaceSelected(i, true);
									}
								}
								dataSetChanged(false);
								if (getBookManager().popupAdapter!=null) {
									getBookManager().popupAdapter.notifyDataSetChanged();
								}
							} else {
								MenuItemImpl menu = (MenuItemImpl) ViewUtils.findInMenu(a.Menu1, R.id.toolbar_action1);
								menu.isLongClicked = PDICMainAppOptions.dictManagerFlipMenuCloumn()?-1:0;
								a.onMenuItemClick(menu);
							}
						} break;
						// 添加全部词典
						case R.string.addAllHere: {
							if(b1) return true;
							a.addElementsToF1(a.f3, null, true, true, pressedPos+1, null);
						} break;
						// 添加网络词典
						case R.string.addWebHere: {
							if(b1) return true;
							a.addElementsToF1(a.f4, null, true, true, pressedPos+1, null);
						} break;
						// 收入剪贴板
						case R.string.addToPasteBin: {
							try {
								if (b1) return true;
								int lines=0;
								String pfx = null;
								String content = "", line, tPath = opt.lastMdlibPath.getPath()+"/";
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									line = null;
									if (isOnSelected) {
										if (getPlaceSelected(i)) {
											line = getPathAt(i);
										}
									} else {
										line = getPathAt(i=position);
									}
									if (line != null) {
										if (content.length() > 0) {
											content += "\n";
										}
										if(line.startsWith(tPath)) {
											line = line.substring(tPath.length());
										}
										if (PDICMainAppOptions.getTmpIsHidden(getPlaceFlagAt(i))) {
											line = "[:H]"+line;
										}
										lines++;
										content += line;
									}
									if (!isOnSelected) {
										break;
									}
								}
								ContentValues cv = new ContentValues();
								cv.put("chn", 0);
								cv.put(LexicalDBHelper.FIELD_CREATE_TIME, CMN.now());
								cv.put("content", content);
								LexicalDBHelper.getInstance().getDB().insert(LexicalDBHelper.TABLE_PASTE_BIN, null, cv);
								getBookManager().showT("已添加" + lines + "行模板至剪剪贴板");
							} catch (Exception e) {
								CMN.debug(e);
							}
						} break;
						// 剪贴板列表
						case R.string.addPasteHere: {
							if(b1) return true;
							getPastBin().show();
						} break;
						// 最近剪贴板
						case R.string.addRecentPasteHere: {
							if(b1) return true;
							String content = "";
							try {
								Cursor cursor = LexicalDBHelper.getInstance().getDB().rawQuery("select seq from SQLITE_SEQUENCE where name=?", new String[]{LexicalDBHelper.TABLE_PASTE_BIN});
								if (cursor.moveToNext()) {
									long rowId = cursor.getLong(0);
									Cursor cursor1 = LexicalDBHelper.getInstance().getDB().rawQuery("select content from " + LexicalDBHelper.TABLE_PASTE_BIN + " where id=?", new String[]{"" + rowId});
									if (cursor1.moveToNext()) {
										content = cursor1.getString(0);
									}
									cursor1.close();
									if (TextUtils.isEmpty(content)) {
										cursor1 = LexicalDBHelper.getInstance().getDB().rawQuery("select content from " + LexicalDBHelper.TABLE_PASTE_BIN + " where id<=? order by id desc limit 1", new String[]{"" + rowId});
										if (cursor1.moveToNext()) {
											content = cursor1.getString(0);
										}
										cursor1.close();
									}
								}
								cursor.close();
							} catch (Exception e) {
								CMN.debug(e);
							}
							addElementsFromPasteBin(content);
						} break;
						// 更多操作
						case R.string.more_actions: {
							if(b1) return true;
							PopupMenuHelper popup = a.getPopupMenu();
							popup.initLayout(new int[]{
								R.string.addWebHere
								, R.string.addAllHere
								, R.layout.poplist_quanzhong_jinxuan
								, R.string.addRecentPasteHere
								, R.string.addPasteHere
								, R.string.addToPasteBin
							}, this);
							int[] vLocationOnScreen = new int[2];
							pressedV.getLocationOnScreen(vLocationOnScreen);
							popup.showAt(a.root, vLocationOnScreen[0], vLocationOnScreen[1]+pressedV.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
							TextMenuView tv = popup.lv.findViewById(R.id.zh1);
							tv.showAtRight = true; tv.setActivated(PDICMainAppOptions.dictManagerTianXuan());
							tv = popup.lv.findViewById(R.id.zh2);
							tv.showAtRight = true; tv.setActivated(PDICMainAppOptions.dictManagerTianJinXuan());
						} break;
						case R.string.move_top: {//移至顶部
							markDirty(-1);
							replace(position, 0);
							dataSetChanged(true);
							getListView().setSelection(0);
						} break;
						case R.string.move_bottom: {//移至底部
							markDirty(-1);
							int last = manager_group().size() - 1;
							replace(position, last);
							dataSetChanged(true);
							getListView().setSelection(last);
						} break;
						case R.id.openFolder: {//在外部管理器打开路径
							StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
							try {
								startActivity(new Intent(Intent.ACTION_VIEW)
										.setDataAndType(Uri.fromFile(new File(getRecordedPathAt(position)).getParentFile()), "resource/folder")
										.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								);
							} catch (Exception e) {
								a.show(R.string.no_suitable_app);
							}
						} break;
						case R.id.openFolderInner: {//在内置管理器打开路径
							magent = getMagentAt(position);
							DialogProperties properties = new DialogProperties();
							properties.selection_mode = DialogConfigs.SINGLE_MULTI_MODE;
							properties.selection_type = DialogConfigs.FILE_SELECT;
							properties.root = new File("/");
							properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
							properties.offset = new File(getRecordedPathAt(position)).getParentFile(); // here
							properties.opt_dir = new File(getOpt().pathToDatabases() + "favorite_dirs/");
							properties.dedicatedTarget = magent.f().getName();
							properties.opt_dir.mkdirs();
							properties.extensions = new HashSet<>();
							properties.extensions.add(".mdx");
							properties.extensions.add(".mdd");
							properties.title_id = R.string.app_name;
							properties.isDark = GlobalOptions.isDark;
							properties.opt = new FIlePickerOptions();
							FilePickerDialog fdialog = new FilePickerDialog(a, properties);
							fdialog.show();
						} break;
						case R.id.tweak1://词典设置
							magent = getMagentAt(position);
							if (isOnSelected) {
								ArrayList<BookPresenter> arr = new ArrayList<>(selected_size());
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										arr.add(getMagentAt(i));
									}
								}
								BookPresenter[] data = arr.toArray(new BookPresenter[0]);
								BookPresenter.showDictTweaker(null, (Toastable_Activity) getActivity(), data);
							} else {
								BookPresenter.showDictTweaker(null, (Toastable_Activity) getActivity(), magent);
							}
							bDictTweakerOnceShowed = true;
							break;
						case R.id.tweak2://词典设置
							if (isOnSelected) {
								ArrayList<BookPresenter> arr = new ArrayList<>(selected_size());
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										arr.add(getMagentAt(i));
									}
								}
								BookPresenter[] data = arr.toArray(new BookPresenter[0]);
								getBookManager().showBookPreferences(data);
							} else {
								getBookManager().showBookPreferences(getMagentAt(position));
							}
							break;
						/* —— 设为滤器等等… —— */
						/* 设为点译词库 */
						case R.id.tapSch: {
							magent = getMagentAt(position);
							boolean isF = !magent.getIsDedicatedFilter();
							magent.setIsDedicatedFilter(isF);
							int cc=0;
							if (isOnSelected) {
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i) && !getPathAt(i).endsWith(".mdd")) {
										// setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsFiler(getPlaceFlagAt(i), isF));
										getMagentAt(i).setIsDedicatedFilter(isF);
										cc++;
									}
								}
								dataSetChanged(false);
							} else {
								cc=1;
								dataSetChangedAt(position);
							}
							tweakedDict = true;
							if (cc > 1) {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "等" + cc + "个词典为点译词库" : "已取消" + magent.getDictionaryName() + "等" + cc + "个点译库");
							} else {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "为点译词库" : "已取消" + magent.getDictionaryName() + "点译词库");
							}
						} break;
						/* 设为默认折叠 */
						case R.id.fold: {
							magent = getMagentAt(position);
							boolean isF = !magent.getAutoFold();
							magent.setAutoFold(isF);
							int cc=0;
							if (isOnSelected) {
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										getMagentAt(i).setAutoFold(isF);
										cc++;
									}
								}
							} else {
								cc=1;
							}
							tweakedDict = true;
							adapter.notifyDataSetChanged();
							if (cc > 1) {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "等" + cc + "个词典为默认折叠" : "已取消" + magent.getDictionaryName() + "等" + cc + "默认折叠");
							} else {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "为默认折叠" : "已取消" + magent.getDictionaryName() + "默认折叠");
							}
						} break;
						/* 设为点译词库 */
//						case 1: {
//							markDirty(actualPosition);
//							boolean isCS = !PDICMainAppOptions.getTmpIsClicker(getPlaceFlagAt(actualPosition));
//							setPlaceFlagAt(actualPosition, PDICMainAppOptions.setTmpIsClicker(getPlaceFlagAt(actualPosition), isCS));
//							if (isOnSelected) {
//								for (int i = 0; i < manager_group().size(); i++) {
//									if (getPlaceSelected(i))
//										setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsClicker(getPlaceFlagAt(i), isCS));
//								}
//							}
//							adapter.notifyDataSetChanged();
//							a.showT(isCS ? "已设为译词库" : "已取消点译词库");
//						} break;
						/* 设为发音库( mdd 专有 ) */
//						case 3: {
//							if (isMddResourceAt(actualPosition)) {
//								markDirty(actualPosition);
//								boolean isCS = !PDICMainAppOptions.getTmpIsAudior(getPlaceFlagAt(actualPosition));
//								setPlaceFlagAt(actualPosition, PDICMainAppOptions.setTmpIsAudior(getPlaceFlagAt(actualPosition), isCS));
//								if(isCS) setPlaceFlagAt(actualPosition, PDICMainAppOptions.setTmpIsFiler(getPlaceFlagAt(actualPosition), false));
//								if (isOnSelected) {
//									for (int i = 0; i < manager_group().size(); i++) {
//										if (getPlaceSelected(i)) {
//											setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsAudior(getPlaceFlagAt(i), isCS));
//											if(isCS) setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsFiler(getPlaceFlagAt(actualPosition), false));
//										}
//									}
//								}
//								adapter.notifyDataSetChanged();
//								a.showT(isCS ? "已设为发音库" : "已取消发音库");
//							}
//						} break;
					}
					popupMenuHelper.dismiss();
					if (b1) {
						dataSetChangedAt(position);
						if (lv != null) {
							((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
						}
					}
					return true;
				}
			});
		}
		TextView tv = mPopup.lv.findViewById(R.id.jianxuan);
		View lv = ViewUtils.getParentByClass(pressedV, ListView.class);
		tv.setText(lv == listView ? "间选" : "全选");
		return mPopup;
	}
	
	private PasteBinHub getPastBin() {
		PasteBinHub pasteBin = null;
		if (pasteBin == null) {
			pasteBin = new PasteBinHub(getBookManager());
			pasteBin.wrapLns = false;
			Pattern p = Pattern.compile("^.*/|.mdx$", Pattern.MULTILINE);
			pasteBin.setListener(new PasteBinHub.PasteBinListener() {
				@Override
				public boolean doPaste(String val) {
					addElementsFromPasteBin(val);
					return false;
				}
				@Override
				public String text(String val) {
					return p.matcher(val).replaceAll("");
				}
			});
		}
		return pasteBin;
	}
	
	private void addElementsFromPasteBin(String val) {
		String[] arr = val.split("\n");
		ArrayList<mFile> list = new ArrayList<>();
		SparseIntArray rejected = new SparseIntArray();
		for(String str:arr) {
			str = str.trim();
			if (str.length() > 0) {
				mFile ret;
				if (str.startsWith("[:H")) {
					str = str.substring(str.indexOf("]") + 1);
					rejected.append(list.size(), 0);
				}
				if (!str.startsWith("/")){
					ret = new mFile(opt.lastMdlibPath, str);
				} else {
					ret = new mFile(str);
				}
				list.add(ret);
			}
		}
		getBookManager().addElementsToF1(null, list.toArray(new mFile[0]), false, true, pressedPos+1, rejected);
	}
	
	public void disEna(boolean useSelection, boolean off, int position, boolean useFilter) {
		int cc=0;
		if (useSelection) {
			for (int i = 0, sz = manager_group().size(); i < sz; i++) {
				if (getPlaceSelected(i) && (!useFilter || filtered.get(i)!=null)) {
					markDirty(i);
					setPlaceRejected(i, off);
					cc++;
					if(position==-1) position = i;
				}
			}
			dataSetChanged(false);
		} else if(position!=-1){
			markDirty(position);
			setPlaceRejected(position, off);
			cc = 1;
			dataSetChangedAt(position);
		}
		refreshSize();
		if (cc > 1) {
			a.showT(!off ? "已启用" + getNameAt(position) + "等" + cc + "个词典" : "已禁用" + getNameAt(position) + "等" + cc + "个词典");
		} else if (cc > 0) {
			a.showT(!off ? "已启用" + getNameAt(position) : "已禁用" + getNameAt(position));
		} else {
			a.showT("当前分组无选中词典。");
		}
	}
	
	private void renameFile() {
		BookPresenter magent = getMagentAt(pressedPos);
		boolean isOnSelected = getPlaceSelected(pressedPos);
		
		View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
		final ListView lv = dialog1.findViewById(R.id.lv);
		final EditText et = dialog1.findViewById(R.id.et);
		ImageView iv = dialog1.findViewById(R.id.confirm);
		et.setText(getNameAt(pressedPos));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(dialog1);
		builder.setIcon(R.mipmap.ic_directory_parent);
		final AlertDialog dd = builder.create();
		
		iv.setOnClickListener(v -> {
			boolean suc = false;
			String newName = et.getText().toString();
			String newPath = newName;
			if(!newPath.contains("/")){
				if(newName.endsWith(".mdx"))
					newName = newName.substring(0, newName.length()-4);
				else if(magent.isMdict())
					newPath+=".mdx";
				if(newName.length()>0){
					String oldPath = magent.getPath();
					File oldf = magent.f();
					String oldFn = oldf.getName();
					String OldFName = magent.getDictionaryName();
					int oldFnLen = oldFn.length();
					
					File to = new File(magent.f().getParent(), newPath);
					String toFn = getOpt().tryGetDomesticFileName(to.getPath());
					if (to.equals(magent.f())) {//就是自己
						suc = true;
					} else if (new File(magent.getPath()).exists()) {//正常重命名
						if (to.exists()) {
							a.showT("文件已存在，重命名失败！");
						} else if (magent.renameFileTo(getActivity(), to)) {//正常重命名成功
							suc = true;
						}
					} else {
						if (to.exists() && !a.mdict_cache.containsKey(to.getAbsolutePath())) {//关联已存在的文件
							magent.renameFileTo(getActivity(), to);
							CMN.Log("重命名", magent.getDictionaryName());
							MagentTransient mdTmp = a.new_MagentTransient(to.getAbsolutePath(), getOpt(), null, true);
							loadMan.md.set(pressedPos, mdTmp);
							loadMan.lazyMan.placeHolders.set(pressedPos, mdTmp.getPlaceHolder());
							suc = true;
						}
					}
					if (suc) {
						a.RebasePath(oldf, OldFName, to, newName, oldFn);
						adapter.notifyDataSetChanged();
						markDirty(-1);
						d.dismiss();
						dd.dismiss();
						a.show(R.string.renD);
						ArrayList<File> moduleFullScannerArr = a.ScanInModlueFiles(true, true);
						AgentApplication app = ((AgentApplication) getActivity().getApplication());
						char[] cb = app.get4kCharBuff();
						for (File fI : moduleFullScannerArr) {
							StringBuilder sb = new StringBuilder();
							String line;
							
							try {
								ReusableBufferedReader br = new ReusableBufferedReader(new FileReader(fI), cb, 4096);
								boolean bNeedReWrite = false;
								while ((line = br.readLine()) != null) {
									/* 含名俱重 */
									try {
										if (line.endsWith(oldFn)) {
											int ll = line.length();
											if (ll == oldFnLen || line.charAt(ll - oldFnLen) == '/') {
												line = ll == oldFnLen ? toFn : (line.substring(0, ll - oldFnLen) + toFn);
												bNeedReWrite = true;
											}
										}
									} catch (Exception ignored) {
									}
									sb.append(line).append("\n");
								}
								br.close();
								cb = br.cb;
								if (bNeedReWrite) {
									ReusableBufferedWriter bw = new ReusableBufferedWriter(new FileWriter(fI), cb, 4096);
									bw.write(sb.toString());
									bw.flush();
									bw.close();
									cb = br.cb;
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						app.set4kCharBuff(cb);
						BookManagerFolderlike f3 = getBookManager().f3;
						if (f3.dataPrepared) {
							int idx = f3.data.remove(new mFile(oldPath));
							if (idx != -1) {
								f3.data.insert(new mFile(to).init(getOpt()));
							}
						}
					}
				}
			}
			if(!suc) {
				a.showT("重命名失败!");
			}
		});
		dd.show();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(BookManager) getActivity();
		if(a!=null) {
			Resources resource = a.getResources();
			mActiveDrawable = resource.getDrawable(R.drawable.star_ic_solid);
			mFilterDrawable = resource.getDrawable(R.drawable.filter);
			mAudioDrawable = resource.getDrawable(R.drawable.voice_ic_big);
			mWebDrawable = resource.getDrawable(R.drawable.ic_web);
			mPDFDrawable = resource.getDrawable(R.drawable.file_pdf_box);
			mRightDrawable = resource.getDrawable(R.drawable.ic_click_search);
			loadMan = a.loadMan;
			//TODO 省去这一步的IO？
			//策略：缓存 placeholder，建立transient管理中间体时直接引用 placeholder。
			a.mdict_cache.clear();
//			for (PlaceHolder phI : slots) {
//				MagentTransient magent = a.new_MagentTransient(phI, a.opt, null, false);
//				if (!magent.isMddResource()) PDICMainAppOptions.setTmpIsAudior(magent, false);
//				if(PDICMainAppOptions.getTmpIsHidden(magent.getTmpIsFlag()))
//					rejector.add(magent.getPath());
//				manager_group.add(magent);
//				a.mdict_cache.put(phI.getPath(a.opt).getPath(), magent);
//			}

			aaa = (BookManager) getActivity();
			listView.setOnItemClickListener((parent, v, position, id) -> {
				//CMN.show(""+(adapter==null)+" "+(((dict_manager_activity)getActivity()).f1.adapter==null));
				//adapter.getItem(position).value = !adapter.getItem(position).value;//TODO optimize
				if (position >= listView.getHeaderViewsCount()) {
					pressedPos = position - listView.getHeaderViewsCount();
					pressedV = v;
					if (PDICMainAppOptions.dictManagerClickPopup()) {
						showPopup(v, null);
					} else {
						markDirty(pressedPos);
						setPlaceRejected(pressedPos, !getPlaceRejected(pressedPos));
						dataSetChangedAt(pressedPos);
						refreshSize();
					}
				}
			});

			listView.setOnItemLongClickListener(this);
			setListAdapter();
			refreshSize();
			
			if (loadMan.managePos>=0 && loadMan.managePos<loadMan.lazyMan.chairCount) {
				int manPos = loadMan.lazyMan.CosyChair[loadMan.managePos];
				//CMN.debug("manPos::", manPos);
				listView.post(new Runnable() {
					@Override
					public void run() {
						selectPos(manPos, loadMan.managePressed);
						loadMan.managePressed = false;
					}
				});
				loadMan.managePos = -1;
			}
			checkDuplication(true);
		}
	}
	
	public void checkDuplication(boolean init) {
		HashMap<String, Integer> map = new HashMap<>();
		boolean cleared = false;
		for (int i = 0; i < loadMan.lazyMan.placeHolders.size(); i++)
		{
			PlaceHolder ph = loadMan.lazyMan.placeHolders.get(i);
			String key = new File(ph.pathname).getName();
			if (init && getPlaceSelected(i)) {
				setPlaceSelected(i, true);
			}
			if (!map.containsKey(key)) {
				map.put(key, i);
			} else {
				if(!cleared) {
					filtered.clear();
					cleared = true;
				}
				Integer pos = map.get(key);
				if (pos!=null) {
					map.put(key, null);
					filtered.put(pos, loadMan.lazyMan.placeHolders.get(pos).pathname);
				}
				filtered.put(i, ph.pathname);
			}
		}
		if (cleared) {
			//getBookManager().showT("发现当前分组存在"+filtered.size()+"条同名词典记录，请删除!");
			getBookManager().showTopSnack("发现当前分组存在"+filtered.size()+"条同名词典记录，请删除!");
			ObjectAnimator td = ViewUtils.tada(getBookManager().searchbar, 2);
			getBookManager().schIndicator_setText(filtered);
			this.query = null;
			//getBookManager().etSearch.setText(null);
			if (td != null) {
				td.start();
				td.setDuration(1233);
			}
		}
	}
	
	private class MyDSController extends DragSortController {
		DragSortListView mDslv;
		public MyDSController(DragSortListView dslv) {
			super(dslv);
			setDragHandleId(R.id.drag_handle);
			mDslv = dslv;

		}

		@Override
		public View onCreateFloatView(int position) {
			View v=adapter.getView(position, null, mDslv);
			BookViewHolder vh = ((BookViewHolder)v.getTag());
			vh.ck.jumpDrawablesToCurrentState();
			//v.getBackground().setLevel(500);
			mDslv.setFloatAlpha(1.0f);
			v.setBackgroundColor(GlobalOptions.isDark?0xFFc17d33:0xFFffff00);//TODO: get primary color
			markDirty(-1);
			int sel = getPlaceSelected(vh.position)?Selection.size():1;
			if (a.accessMan.isEnabled()) {
				a.root.announceForAccessibility("正在拖拽 "+(sel>1?sel+"本":"")+"词典"+getNameAt(vh.position)+" 当前处于列表第"+vh.position+"项");
			}
			return v;
		}

		@Override
		public void onDestroyFloatView(View floatView) {
			//do nothing; block super from crashing
		}

	}

	public void refreshDicts(boolean bUnfinished) {
		if(bUnfinished) {
			adapter.notifyDataSetChanged();
		}
	}

	boolean getPlaceSelected(int position) {
		return (loadMan.lazyMan.placeHolders.get(position).lineNumber & 0x80000000)!=0;
	}
	
	void setPlaceSelected(int position, boolean val) {
		PlaceHolder ph = loadMan.lazyMan.placeHolders.get(position);
		setPlaceSelectedInter(ph, val);
		if (val) {
			Selection.add(ph);
		} else {
			Selection.remove(ph);
		}
	}
	
	private void setPlaceSelectedInter(PlaceHolder ph, boolean val) {
		ph.lineNumber &= ~0x80000000;
		if(val) ph.lineNumber |= 0x80000000;
	}
	
	boolean getPlaceRejected(int position) {
		return PDICMainAppOptions.getTmpIsHidden(getPlaceFlagAt(position));
	}
	
	void setPlaceRejected(int position, boolean val) {
		int flag = getPlaceFlagAt(position);
		if (PDICMainAppOptions.getTmpIsHidden(flag)!=val) {
			setPlaceFlagAt(position, PDICMainAppOptions.setTmpIsHidden(flag,val));
			loadMan.lazyMan.chairCount += val?-1:1;
		}
	}
	
	final ArrayList<BookPresenter> manager_group() {
		return loadMan.md;
	}
	
	final ArrayList<PlaceHolder> place_group() {
		return loadMan.lazyMan.placeHolders;
	}
	
	public int selected_size() {
		return Selection.size();
	}
	
	public void remove(int i) {
		markDirty(-1);
		replace(i, -1);
	}
	
	public void replace(int from, int to) {
		if (to < 0) {
			BookPresenter rmd = loadMan.md.remove(from);
			if(rmd!=null) a.mdict_cache.put(rmd.getPath(), rmd);
			PlaceHolder ph = loadMan.lazyMan.placeHolders.remove(from);
			loadMan.lazyMan.chairCount--;
			Selection.remove(ph);
			if (filtered.size()>0 && this==getBookManager().getFragment() && filtered.get(from)!=null) {
				filtered.remove(from);
				if (getBookManager().popupAdapter!=null) {
					getBookManager().popupAdapter.notifyDataSetChanged();
				}
			}
		} else {
			loadMan.md.add(to, loadMan.md.remove(from));
			loadMan.lazyMan.placeHolders.add(to, loadMan.lazyMan.placeHolders.remove(from));
		}
	}
	
	private BookPresenter getMagentAt(int position) {
		return getBookManager().getMagentAt(position);
	}
	
	public String getPathAt(int position) {
		BookPresenter mdTmp = loadMan.md.get(position);
		if (mdTmp!=null) {
			return mdTmp.getPath();
		}
		return loadMan.lazyMan.placeHolders.get(position).getPath(getOpt()).toString();
	}
	
	public String getRecordedPathAt(int position) {
		return loadMan.lazyMan.placeHolders.get(position).getPath(getOpt()).toString();
	}
	
	public int getPlaceFlagAt(int position) {
//		BookPresenter mdTmp = loadMan.md.get(position);
//		if (mdTmp!=null) {
//			return mdTmp.tmpIsFlag;
//		}
//		mdTmp = getBookManager().getMagentAt(position, false);
//		if (mdTmp!=loadMan.EmptyBook) {
//			return mdTmp.tmpIsFlag;
//		}
		return loadMan.lazyMan.placeHolders.get(position).tmpIsFlag;
	}
	
	private void setPlaceFlagAt(int position, int flag) {
		markDirty(position);
//		BookPresenter mdTmp = loadMan.md.get(position);
//		if (mdTmp!=null) {
//			mdTmp.tmpIsFlag = flag;
//		}
//		mdTmp = getBookManager().getMagentAt(position, false);
//		if (mdTmp!=loadMan.EmptyBook) {
//			mdTmp.tmpIsFlag = flag;
//		}
		loadMan.lazyMan.placeHolders.get(position).tmpIsFlag = flag;
	}
	
	public CharSequence getNameAt(int position) {
		String path = getPathAt(position);
		int idx = path.lastIndexOf('/');
		if(idx>0)
			return new CharSequenceKey(path, idx+1);
		return path;
	}
	
	private boolean isMddResourceAt(int actualPosition) {
		String path = getPathAt(actualPosition);
		return path.length()>4 && path.substring(path.length()-4).equalsIgnoreCase(".mdd");
	}
	
	public void markDirty(int attrPos) {
		if (attrPos >= 0/* && !isDataDirty()*/) {
			PlaceHolder ph = loadMan.lazyMan.placeHolders.get(attrPos);
			Integer val = dirtyAttrArray.get(ph);
			if (val==null) {
				dirtyAttrArray.put(ph, ph.tmpIsFlag);
			}
		}
		else markDataDirty(true);
		if (!isDirty) {
			isDirty = true;
			a.markDirty();
		}
	}
	
	public final boolean isDataDirty(){ return placeArray!=null; };
	private PlaceHolder[] placeArray;
	HashMap<PlaceHolder, Integer> dirtyAttrArray = new HashMap<>();
	public PlaceHolder[] markDataDirty(boolean dirty) {
		if (dirty!=isDataDirty()) {
			if (dirty) {
				placeArray = new PlaceHolder[loadMan.lazyMan.placeHolders.size()];
				for (int i = 0; i < placeArray.length; i++) {
					placeArray[i] = loadMan.lazyMan.placeHolders.get(i).clone();
				}
			} else {
				placeArray = null;
				dirtyAttrArray.clear();
			}
		}
		return placeArray;
	}
	
	public int schFilter(String query, boolean shouldInval) {
		if (!query.equals(this.query)) {
			getBookManager().popupPos[0] = 0;
			this.query = query;
		}
		int sz = filtered.size();
		filtered.clear();
		if (!TextUtils.isEmpty(query)) {
			for (int i = 0; i < manager_group().size(); i++) {
				String name = getNameAt(i).toString();
				int suffixIdx = name.lastIndexOf("."), sch=name.toLowerCase().indexOf(query);
				if (sch>=0 && (suffixIdx==-1 || sch<suffixIdx)) {
					filtered.put(i, name);
				}
			}
		}
		if (shouldInval && !(sz==0 && filtered.size()==0)) {
			dataSetChanged(false);
		}
		return filtered.size();
	}
	
	
	public void checkTweakedDict() {
		CMN.debug("checkTweakedDict", tweakedDict);
		if (tweakedDict) {
			for (int i = 0, sz = manager_group().size(); i < sz; i++) {
				a.getMagentAt(i, false).checkFlag(a);
			}
			tweakedDict = false;
		}
	}
}

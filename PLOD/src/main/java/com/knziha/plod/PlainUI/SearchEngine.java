package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

import com.knziha.plod.dictionarymanager.BookManagerMain;
import com.knziha.plod.dictionarymodels.resultRecorderLucene;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.widgets.EditTextmy;
import com.knziha.plod.widgets.ViewUtils;
import com.mobeta.android.dslv.DragSortListView;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class SearchEngine extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener, PopupMenuHelper.PopupMenuListener {
	final MainActivityUIBase a;
	final LuceneHelper helper;
	
	AlertDialog dialog;
	Toolbar toolbar;
	ViewGroup tools, group, inputs;
	EditTextmy etSearch;
	DragSortListView schLv;
	String pressedBookName;
	
	int mForegroundColor;
	PorterDuffColorFilter ForegroundFilter;
	
	int schGroup;
	HashMap<String, Integer> schMap = new HashMap<>();
	
	public SearchEngine(MainActivityUIBase a, LuceneHelper helper) {
		this.a = a;
		this.helper = helper;
	}
	
	private void performSearch() {
		//etSearch.setText("trip over");
		String phrase = String.valueOf(etSearch.getText()).trim();
		//phrase = "开心";
		if(phrase.length()>0) helper.CurrentSearchText=phrase;
		// a.showT("search::"+phrase);
		a.switchSearchEngineLst(true);
		dialog.dismiss();
		
		resultRecorderLucene results = null;
		try {
			results = helper.do_search(null, schGroup == 0 ? a.loadManager.map()
					: schGroup == 2 ? schMap : null);
		} catch (Exception e) {
			CMN.debug(e);
		}
		
		if (results != null) {
			a.adaptermy5.results = results;
			results.invalidate(a, null);
			a.adaptermy5.notifyDataSetChanged();
			
			if(PDICMainAppOptions.schPageAfterFullSch()){
				// a.fullSearchLayer.getBakedPattern();
				a.autoSchPage(getPagePattern(), true);
			}
		}
	}
	
	public String getPagePattern() {
		String val = helper.CurrentSearchText;
		if(val==null) val="";
		String ret=val;
		return val;
	}
	
	public void setTitleForegroundColor(ViewGroup v, boolean init, int foregroundColor) {
		LinkedList<ViewGroup> linkedList = new LinkedList<>();
		linkedList.add(v);
		View cI;
		while (!linkedList.isEmpty()) {
			ViewGroup current = linkedList.removeFirst();
			for (int i = 0; i < current.getChildCount(); i++) {
				cI = current.getChildAt(i);
				if (cI instanceof ViewGroup) {
					linkedList.addLast((ViewGroup) current.getChildAt(i));
				} else {
					if (cI instanceof TextView) {
						if(cI instanceof RadioSwitchButton) {
							//((RadioSwitchButton) cI).getButtonDrawable().mutate().setColorFilter(ForegroundFilter);
							((RadioSwitchButton) cI).setTextColor(foregroundColor);
							cI.setOnClickListener(this);
							if (init) {
								((RadioSwitchButton) cI).setButtonDrawable(R.drawable.radio_selector);
							}
						}
						else if (cI instanceof ActionMenuItemView) {
							((ActionMenuItemView) cI).getIcon().mutate().setColorFilter(ForegroundFilter);
							cI.setOnClickListener(this);
						}
						else {
							((TextView) cI).setTextColor(a.AppBlack);
						}
					} else {
						if (init && cI.getId() != -1) {
							cI.setOnClickListener(this);
						}
						if(cI instanceof ImageView){
							if(cI.getBackground() instanceof BitmapDrawable){
								cI.getBackground().mutate().setColorFilter(ForegroundFilter);
							} else {
								((ImageView)cI).setColorFilter(ForegroundFilter);
							}
						} else if(cI.getBackground()!=null){
							cI.getBackground().mutate().setColorFilter(ForegroundFilter);
						}
					}
				}
			}
		}
	}
	
	@Override
	public int getCount() {
		return helper.indexedbooks.size();
	}
	@Override
	public Object getItem(int position) {
		return null;
	}
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BookManagerMain.ViewHolder vh;
		if (convertView == null) {
			convertView = a.getLayoutInflater().inflate(R.layout.dict_manager_dslitem, parent, false);
			vh = new BookManagerMain.ViewHolder(convertView);
			vh.title.setOnClickListener(this);
			vh.title.setOnLongClickListener(this);
			vh.ck.setOnClickListener(this);
		} else {
			vh = (BookManagerMain.ViewHolder) convertView.getTag();
		}
		vh.position = position;
		String bookName = helper.indexedbooks.get(position).name;
		vh.title.setText(bookName);
		vh.handle.setVisibility(View.GONE);
		if(GlobalOptions.isDark) {
			convertView.getBackground().setColorFilter(0x39ffffff & a.AppBlack, PorterDuff.Mode.SRC_IN);
		}
		vh.title.setTextColor(a.AppBlack);
		boolean checked = false;
		if (schGroup == 0) {
			checked = a.loadManager.map().containsKey(bookName);
		}
		else if (schGroup == 1) {
			checked = true;
		}
		else if (schGroup == 2) {
			checked = schMap.containsKey(bookName);
		}
		vh.ck.setChecked(checked);
		return convertView;
	}
	
	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		if (id == R.id.text|| id == R.id.check1) {
			BookManagerMain.ViewHolder vh
					= (BookManagerMain.ViewHolder) ViewUtils.getViewHolderInParents(v, BookManagerMain.ViewHolder.class);
			if (vh != null) {
				PopupMenuHelper popupMenu = a.getPopupMenu();
				pressedBookName = vh.title.getText();
				if(popupMenu.getListener()!=this) {
					int[] texts = new int[]{
						R.string.delete
					};
					popupMenu.initLayout(texts, this);
				}
				View rv = dialog.getWindow().getDecorView();
				View vp = (View) v.getParent();
				popupMenu.showAt(rv, 0, vp.getHeight()-v.getTop()-v.getHeight(), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
				ViewUtils.preventDefaultTouchEvent(v, 0, 0);
			}
		}
		return false;
	}
	
	@SuppressLint("ResourceType")
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		if(v.getId()==R.string.delete) {
			popupMenuHelper.dismiss();
			try {
				helper.deleteIndex(pressedBookName);
				notifyDataSetChanged();
				a.showT("索引<"+pressedBookName+">删除成功！");
			} catch (IOException e) {
				a.showT("删除失败……");
			}
		}
		return true;
	}
	
	// click
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == android.R.id.button1|| id == R.id.search) {
			performSearch();
		} else if (id == R.id.text|| id == R.id.check1) {
			BookManagerMain.ViewHolder vh
					= (BookManagerMain.ViewHolder) ViewUtils.getViewHolderInParents(v, BookManagerMain.ViewHolder.class);
			if (vh != null) {
				if (schGroup != 2) {
					if (id == R.id.check1) {
						vh.ck.setChecked(!vh.ck.isChecked());
					}
				} else {
					if (id != R.id.check1) {
						vh.ck.setChecked(!vh.ck.isChecked());
					}
					try {
						String bookName = helper.indexedbooks.get(vh.position).name;
						if (vh.ck.isChecked()) {
							schMap.put(bookName, null);
						} else {
							schMap.remove(bookName);
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			}
		} else {
			View p = ViewUtils.getNthParentNonNull(v, 1);
			if (p == group) {
				for (int i = 0; i < group.getChildCount(); i++) {
					RadioSwitchButton b = (RadioSwitchButton) group.getChildAt(i);
					if (b == v) {
						b.setChecked(true);
						PDICMainAppOptions.schGroup(schGroup = i);
					} else {
						b.setChecked(false);
					}
				}
				if (schGroup == 2 && schMap.size() == 0) {
					schMap.put(a.currentDictionary.getDictionaryName(), null);
				}
				notifyDataSetChanged();
				return;
			}
			p = ViewUtils.getNthParentNonNull(v, 2);
			if (p == inputs) {
				return;
			}
		}
	}
	
	public void refresh() {
		int pad = (int) (2.8f * a.mResource.getDimension(R.dimen._50_) * (a.dm.widthPixels>GlobalOptions.realWidth?1:1.45f));
		//CMN.debug("pad::", pad, root.getHeight());
		if (a.root.getHeight() >= 2 * pad) {
			schLv.mMaxHeight = a.root.getHeight()
					- a.root.getPaddingTop() - pad * 2;
		} else {
			schLv.mMaxHeight = 0;
		}
	}
	
	@SuppressLint("ResourceType")
	public void show(String text) {
		if (dialog == null) {
			// getSchView
			View view = a.getLayoutInflater().inflate(R.layout.search_view, null);
			DragSortListView lv = view.findViewById(android.R.id.list);
			lv.setAdapter(this);
			int pad = (int) (GlobalOptions.density*5);
			lv.setPadding(pad,0,pad,0);
			toolbar = view.findViewById(R.id.toolbar);
			toolbar.inflateMenu(R.xml.menu_search);
			toolbar.setNavigationIcon(R.drawable.ic_baseline_history_24);
			toolbar.getNavigationBtn().setAlpha(0.3f);
			tools = (ViewGroup) ViewUtils.findViewById((ViewGroup) view, R.id.tools);
			group = tools.findViewById(R.id.group);
			inputs = tools.findViewById(R.id.inputs);
			((RadioSwitchButton)group.getChildAt(schGroup = PDICMainAppOptions.schGroup())).setChecked(true);
			if (schGroup == 2 && schMap.size() == 0) {
				schMap.put(a.currentDictionary.getDictionaryName(), null);
			}
			schLv = lv;
			etSearch = toolbar.findViewById(R.id.etSearch);
			etSearch.setText(text);
			etSearch.setOnEditorActionListener((v, actionId, event) -> {
				if (actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
					performSearch();
				}
				return true;
			});
			// 染色
			int foregroundColor = GlobalOptions.isDark ? Color.WHITE : a.MainBackground;
			ForegroundFilter = new PorterDuffColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN);
			mForegroundColor = foregroundColor;
			setTitleForegroundColor(toolbar, true, foregroundColor);
			setTitleForegroundColor(group, true, foregroundColor);
			if (GlobalOptions.isDark) {
				setTitleForegroundColor(inputs, true, foregroundColor);
			}
			dialog = new AlertDialog.Builder(a)
					.setPositiveButton("搜索", null)
					.setNegativeButton("取消", null)
					.setTitle("搜索引擎")
					.setWikiText("功能测试中", null)
					.setView(view)
					.show();
			dialog.findViewById(android.R.id.button1).setOnClickListener(this);
		}
		refresh();
		ViewUtils.ensureWindowType(dialog, a, null);
		dialog.show();
		dialog.mAlert.wikiBtn.setAlpha(0.3f);
		if (helper.indexChanged) {
			helper.prepareSearch(false);
			helper.reloadIndexedBookList();
			notifyDataSetChanged();
			helper.indexChanged = false;
		}
	}
}

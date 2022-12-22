package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.plod.dictionary.Utils.MyPair;
import com.knziha.plod.dictionary.Utils.StrId;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.FavFolderAdapter;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;

@SuppressLint("ResourceType")
public class FavoriteHub extends PlainAppPanel implements PopupMenuHelper.PopupMenuListener {
	//MainActivityUIBase a;
	private ListView listView;
	private String text;
	
	public FavoriteHub(MainActivityUIBase a) {
		super(a, false);
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.tweakDlgScreen = false;
		this.a = a;
		this.bShouldInterceptClickListener = true;
		setShowInDialog();
	}
	
	public void show(String text) {
		this.text = text;
		a.FavoriteNoteBooksAdapter().refreshAddedFoldersForText(text);
		if (!isVisible()) {
			toggle(null, null, -1);
			refresh();
		}
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		if (a!=null && settingsLayout==null) {
			opt = a.opt;
			//View layout = a.getLayoutInflater().inflate(R.layout.tts_sound_control, a.root, false);
			ViewGroup layout = (ViewGroup) a.getLayoutInflater().inflate(R.layout.bottom_favorite_sheet, null);
			listView = (ListView) layout.getChildAt(1);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				listView.setNestedScrollingEnabled(true); //  todo为啥不能列表不联动，只联动标题呢？
			}
			listView.setAdapter(a.FavoriteNoteBooksAdapter());
			listView.setOnItemClickListener((parent, view, position, id) -> {
				CheckedTextView tv = view.findViewById(android.R.id.text1);
				tv.toggle();
				tv.jumpDrawablesToCurrentState();
				a.favFolderAdapter.setChecked(position, tv.isChecked());
			});
			ViewUtils.setOnClickListenersOneDepth(layout, this, 999, null);
			settingsLayout = layout;
		}
	}
	
	private void refreshExpand() {
//		View v = settingsLayout;
//		DisplayMetrics dm2 = a.dm;
//		if (hubExpanded)
//			v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * 0.85f);
//		else
//			v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * ((BottomSheetDialog) dialog).getBehavior().getHalfExpandedRatio() + a.getResources().getDimension(R.dimen._45_) * 1.75);
//		v.requestLayout();
	}
	
	@Override
	protected void onShow() {
		refresh();
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
	}
	
	@Override
	public void refresh() {
		//CMN.debug("FavHub::refresh", a.MainAppBackground, GlobalOptions.isDark);
		if (MainAppBackground != a.MainAppBackground)
		{
			// 刷新颜色变化（黑暗模式或者设置更改）
			//toolbar.setTitleTextColor(a.AppWhite);
			MainAppBackground = a.MainAppBackground;
			settingsLayout.setBackgroundColor(a.AppWhite);
			((TextView)settingsLayout.findViewById(R.id.title)).setTextColor(a.AppBlack);
			settingsLayout.findViewById(R.id.bottombar).getBackground().setColorFilter(GlobalOptions.isDark?GlobalOptions.NEGATIVE:null);
			if (listView!=null) {
				((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
			}
		}
		if (ViewUtils.ensureTopmost(dialog, a, dialogDismissListener)
				|| ViewUtils.ensureWindowType(dialog, a, dialogDismissListener)) {
			ViewUtils.makeFullscreenWnd(dialog.getWindow());
		}
	}
	
	// click
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			/* 修改星级 */
			case android.R.id.text2: {
				a.showT("未实现");
				a.mInterceptorListenerHandled = true;
			} break;
			case R.id.cancel:
				dismissImmediate();
				break;
			case R.id.confirm:
				LongSparseArray<Long> tempFolderIds;
				final FavFolderAdapter mAdapter = a.favFolderAdapter;
				final ArrayList<StrId> folders = mAdapter.folders;
				LongSparseArray<Long> selected = mAdapter.selected;
				if(selected.size()>0) {
					int cnt_del=0, num_del=0, cnt_add=0, num_add=0;
					// first 删除
					tempFolderIds = mAdapter.oldFidAndLvls;
					for (int i = 0, sz=tempFolderIds.size(); i < sz; i++) {
						long fidOld = tempFolderIds.keyAt(i);
						if (selected.indexOfKey(fidOld) < 0) { // 不选了
							num_del++;
							try {
								if(a.prepareHistoryCon().remove(text, fidOld)>=0) {
									cnt_del++;
								}
							} catch (Exception e) { CMN.debug(e); }
						}
						else { // 还是选了
							// 排除已经添加过的文件夹，避免刷新旧的收藏
							//else if (!mAdapter.re_selected.contains(fidOld)) { // 如果不是再次选中，即排除
							selected.remove(fidOld);
							//}
						}
					}
					// second 开始添加
					num_add = selected.size();
					tempFolderIds = selected;
					for (int i = 0, sz=tempFolderIds.size(); i < sz; i++) {
						long fidNew = tempFolderIds.keyAt(i);
						try {
							if(a.prepareHistoryCon().insert(a, text, fidNew, a.weblist)>=0){
								cnt_add++;
							}
						} catch (Exception e) { CMN.debug(e); }
					}
					String msg = "";
					if (num_add>0) {
						msg += " 添加完毕！(" + cnt_add + "/" + num_add + ")";
					}
					if (num_del>0) {
						if (!TextUtils.isEmpty(msg)) {
							msg += "\t";
						}
						msg += " 移除完毕！(" + cnt_del + "/" + num_del + ")";
					}
					if (!TextUtils.isEmpty(msg)) {
						a.showT(msg);
					}
					selected.clear();
				}
				dismissImmediate();
				break;
			case R.id.new_folder:
				a.showCreateNewFavoriteDialog((int) (settingsLayout.getWidth() - a.mResource.getDimension(R.dimen._35_)));
				break;
		}
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		if (!isLongClick) {
			popupMenuHelper.dismiss();
			switch (v.getId()) {
			}
			return true;
		}
		return false;
	}
	
	public void show() {
		if (!isVisible()) {
			toggle(a.root, null, -1);
		} else if (getLastShowType()==2) {
			ViewUtils.ensureTopmost(dialog, a, dialogDismissListener);
		}
	}
	
	@Override
	protected void showDialog() {
		//super.showDialog();
		BottomSheetDialog bPane = (BottomSheetDialog) dialog;
		if(bPane==null) {
			CMN.debug("重建底部弹出");
			dialog = bPane = new BottomSheetDialog(a);
			bPane.setContentView(settingsLayout);
			bPane.getWindow().setDimAmount(0.2f);
			//CMN.recurseLogCascade(lv);
			bPane.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);// 展开
		}
		refreshExpand();
		super.showDialog();
		
		listView.getLayoutParams().height = (int) (Math.max(opt.dm.heightPixels, opt.dm.widthPixels) * bPane.getBehavior().getHalfExpandedRatio() - a.mResource.getDimension(R.dimen._45_) * 1.75);
		//listView.requestLayout();
	}
}
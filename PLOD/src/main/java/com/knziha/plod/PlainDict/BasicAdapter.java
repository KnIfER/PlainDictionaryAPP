package com.knziha.plod.plaindict;

import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.viewpager.widget.ViewPager;

import com.knziha.ankislicer.customviews.WahahaTextView;
import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import java.util.List;

public abstract class BasicAdapter extends BaseAdapter
		implements OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener, PopupMenuHelper.PopupMenuListener {
		WebViewmy mWebView;
		public final ContentviewBinding contentUIData;
		public final WebViewListHandler weblistHandler;
		MenuBuilder allMenus;
		List<MenuItemImpl> contentMenus;
		public ViewGroup webviewHolder;
		public ListView lava;
		int lastClickedPos=-1;
		public int lastClickedPosBefore;
		boolean userCLick;
		public boolean shunt;
		public boolean browsed;
		public boolean bOnePageNav;

		public SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
		protected BookPresenter presenter;
		//int adelta=0;
		public int zhTrans;

		public void ClearVOA() {
			avoyager.clear();
			//adelta=0;
		}
		
		public void SaveVOA() {
		
		}
    	
    	public void setResHolder(resultRecorderDiscrete r) {
    		results=r;
    	}
        //public ArrayList<additiveMyCpr1> combining_search_result = new ArrayList<additiveMyCpr1>();//additiveMyCpr1
        public resultRecorderDiscrete results;
		public String currentKeyText;
        
        //构造
        public BasicAdapter(ContentviewBinding contentUIData, WebViewListHandler weblistHandler, MenuBuilder allMenus, List<MenuItemImpl> contentMenu)
        {
			this.contentUIData = contentUIData;
			this.weblistHandler = weblistHandler;
			this.allMenus=allMenus;
			this.contentMenus=contentMenu;
		}
        @Override
        public int getCount() {
          return results.size();
        }
        @Override
        public View getItem(int position) {
			return null;
		}
        public String getEntry(int pos) {
			return null;
		}
        @Override
        public long getItemId(int position) {
          return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
	          return convertView;
        }
		public void shutUp() {
		
		}
		public void onItemClick(int pos) {
        	lastClickedPos = Math.min(getCount(), Math.max(0, pos));
//			weblistHandler.highlightVagranter.HlightIdx=0;
//			weblistHandler.highlightVagranter.AcrArivAcc =0;
			//ActivedAdapter=this;
		}
	
		public void enterPeruseMode(int pos) {
		
		}
		
		int pressedRow = 0;

		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
			pressedRow = position;
			MainActivityUIBase a = presenter.a;
			boolean b1 = (a.thisActType==MainActivityUIBase.ActType.PlainDict && ViewUtils.getParentByClass(lava, ViewPager.class)!=null);
			PopupMenuHelper popupMenu = a.getPopupMenu();
			popupMenu.initLayout(new int[]{
					R.layout.poplist_fanyi_sch
					, R.string.tapSch
					, R.string.peruse_mode
					, b1 ? R.string.lock_viewpage_lst : 0
					, R.string.page_ucc
					, R.string.copy
			}, this);
			if (b1) {
				popupMenu.lv.findViewById(R.string.lock_viewpage_lst).setActivated(PDICMainAppOptions.lockViewPageScroll());
			}
			int[] vLocationOnScreen = new int[2];
			v.getLocationOnScreen(vLocationOnScreen); //todo 校准弹出位置
			popupMenu.showAt(v, vLocationOnScreen[0], vLocationOnScreen[1]+v.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
			return true;
		}
	
		public String getRowText(int position) {
			return "";
		}
		
		@Override
		public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
			MainActivityUIBase a = presenter.a;
			if(isLongClick) return false;
			switch (v.getId()) {
				case R.id.zh1:
					PDICMainAppOptions.listZhTranslate(1);
					notifyDataSetChanged();
				break;
				case R.id.zh2:
					PDICMainAppOptions.listZhTranslate(2);
					notifyDataSetChanged();
				break;
				case R.id.zh0:
					PDICMainAppOptions.listZhTranslate(0);
					notifyDataSetChanged();
				break;
				case R.string.copy:
					a.copyText(getRowText(pressedRow), true);
				break;
				case R.string.tapSch:
				case R.id.page_lnk_tapSch:
					a.popupWord(getRowText(pressedRow), null, -1, null, false);
				break;
				case R.string.peruse_mode:
				case R.id.page_lnk_fye:
					enterPeruseMode(pressedRow);
				break;
				case R.string.page_ucc:
					a.getVtk().setInvoker(null, null, null, getRowText(pressedRow));
					a.getVtk().onClick(null);
				break;
				case R.string.lock_viewpage_lst:
					PDICMainAppOptions.lockViewPageScroll(!v.isActivated());
					((PDICMainActivity)a).UIData.viewpager.setNoScroll(!v.isActivated());
					break;
			}
			popupMenuHelper.postDismiss(150);
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			onItemClick(pos);
		}

		public abstract int getId();

		public abstract String currentKeyText();
	
		public void setPresenter(BookPresenter presenter) {
			this.presenter = presenter;
		}
	
		public BookPresenter getPresenter() {
			return presenter;
		}
	
		@Override
		public void onClick(View v) {
			View f = presenter==null?null:presenter.a.getCurrentFocus();
			if (f!=null && f.getClass()== WahahaTextView.class && ((WahahaTextView) f).hasSelection()) {
				f.clearFocus();
				return;
			}
			MainActivityUIBase.ViewHolder vh = (MainActivityUIBase.ViewHolder) ViewUtils.getViewHolderInParents(v, MainActivityUIBase.ViewHolder.class);
			if (vh != null) {
				onItemClick(vh.position);
			}
		}
}
package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import java.util.List;

 /** 用于弹出显示词典内容（新窗口） */
public class AlloydPanel extends PlainAppPanel {
	public Toolbar toolbar;
	public MenuBuilder AllMenus;
	public List<MenuItemImpl> RandomMenu;
	public List<MenuItemImpl> PopupMenu;
	public MenuItemImpl fetchWordMenu;
	public boolean isWordMap;
	protected int menuResId = R.xml.menu_popup_content;
	
	public AlloydPanel(MainActivityUIBase a, @NonNull WebViewListHandler weblistHandler, boolean init) {
		super(a, init);
		this.weblistHandler = weblistHandler;
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		setShowInDialog();
		if (!init) {
			this.a = a;
		}
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(MainActivityUIBase) context;
			mBackgroundColor = 0;
			setShowInDialog();
		}
		if (settingsLayout==null && weblistHandler !=null) {
			addToolbar();
			// tabTranslateEach
			//AllMenus.getItems().set(4, a.getMenuSTd(R.id.translator));
			if(weblistHandler.tapSch) {
				if(weblistHandler.tapDef) ViewUtils.findInMenu(AllMenus.getItems(), R.id.tapSch1).setChecked(true);
				else ViewUtils.findInMenu(AllMenus.getItems(), R.id.tapSch).setChecked(true);
			}
			
			RandomMenu = ViewUtils.MapNumberToMenu(AllMenus, 0, 1, 7, 2, 3, 4, 9, 8, 5);
			PopupMenu = ViewUtils.MapNumberToMenu(AllMenus, 6, 1, 7, 2, 3, 4, 9, 8, 5);//new ArrayList<>(AllMenus.mItems);
			toolbar.setNavigationOnClickListener(v -> dismiss());
			toolbar.setOnMenuItemClickListener(a);
			toolbar.getNavigationBtn().setOnLongClickListener(v -> {
				weblistHandler.showMoreToolsPopup(v);
				return true;
			});
			
			fetchWordMenu = (MenuItemImpl) ViewUtils.findInMenu(RandomMenu, R.id.fetchWord);
			//refresh();
			if (weblistHandler.fetchWord>0) {
				fetchWordMenu.setChecked(true);
			}
		}
	}
	
	 @SuppressLint("ResourceType")
	 protected void addToolbar() {
		 if (settingsLayout==null) {
			 SplitView linearView = weblistHandler.contentUIData.webcontentlister;
			 Toolbar toolbar = this.toolbar = new Toolbar(linearView.getContext());
			 linearView.addView(toolbar, 0);
			 toolbar.getLayoutParams().height = (int) (GlobalOptions.density * 45);
			 toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
			 toolbar.inflateMenu(menuResId);
			 toolbar.getLayoutParams().height = (int) a.mResource.getDimension(R.dimen.barSize);
			 AllMenus = (MenuBuilder) toolbar.getMenu();
			 AllMenus.tag = weblistHandler;
			 AllMenus.multiColumn = 1|2;
			 AllMenus.checkActDrawable = a.mResource.getDrawable(R.drawable.frame_checked);
			 AllMenus.checkDrawable = a.AllMenus.checkDrawable;
			 AllMenus.mOverlapAnchor = PDICMainAppOptions.menuOverlapAnchor();
			 settingsLayout = linearView;
		 }
	 }
	
	 @Override
	public void refresh() {
		boolean check = false;
		if (MainAppBackground != a.MainAppBackground) {
			check = true;
			MainAppBackground = a.MainAppBackground;
			toolbar.setBackgroundColor(MainAppBackground);
			toolbar.setTitleTextColor(Color.WHITE);
			double lumen = ColorUtils.calculateLuminance(MainAppBackground);
			int bc = lumen > 0.5 ? Color.BLACK : Color.WHITE;
			AllMenus.checkActDrawable.setColorFilter(ColorUtils.blendARGB(MainAppBackground, bc, 0.5f)&0xB9FFFFFF, PorterDuff.Mode.SRC_IN);
		}
		int color = a.tintListFilter.sForeground;
		if (ForegroundColor != color) {
			check = true;
			ForegroundColor = color;
			ViewUtils.setForegroundColor(toolbar, a.tintListFilter);
		}
		if (check && weblistHandler!=null) {
			weblistHandler.checkUI();
		}
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
//		if (v.getId() == R.id.dayNightBtn) {
//			dayNightArt.toggle(true);
//			a.toggleDarkMode();
//		} else {
//			dismiss();
//			if (v.getId() != R.drawable.ic_menu_24dp) {
//				a.mInterceptorListenerHandled = true;
//			}
//		}
	}
	
	@SuppressLint("MissingSuperCall")
	@Override
	protected void onDismiss() {
//		CMN.Log("onDismiss::");
//		super.onDismiss();
//		if (mSettingsChanged!=0) {
//			//a.currentViewImpl.checkSettings(true, true);
//			mSettingsChanged=0;
//		}
//		mScrollY = settingsLayout.getScrollY();
		//if (opt.getRemPos())
		if(weblistHandler!=null)
		{
			WebViewmy mWebView = weblistHandler.dictView;
			if (mWebView!=null && mWebView.isViewSingle()/* && mWebView.currentRendring.length==1*/) {
				BookPresenter book = mWebView.presenter;
				if (!book.getIsWebx()) {
					ScrollerRecord pPos = book.avoyager.get((int) mWebView.currentPos);
					pPos = mWebView.storePagePos(pPos);
					if (pPos!=null) {
						book.avoyager.put((int) mWebView.currentPos, pPos);
					}
				}
			}
		}
		if (a.wordCamera!=null && a.settingsPanels.indexOf(a.wordCamera)>=a.settingsPanels.size()-2) {
			a.wordCamera.onResume();
		}
	}
	
	 @Override
	 protected void onShow() {
		CMN.debug("onShow");
		 if (a.wordCamera!=null) {
			 a.wordCamera.onPause();
		 }
		 if (weblistHandler!=null) {
			 if (PDICMainAppOptions.revisitOnBackPressed()/* && (pop==null || !pop.isShowing())*/) {
				 weblistHandler.getMergedFrame().cleanPage = true;
			 }
			 weblistHandler.checkTitlebarHeight();
		 }
	 }
 }

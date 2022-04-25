package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;

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

import java.util.ArrayList;
import java.util.List;

public class AlloydPanel extends PlainAppPanel {
	public WebViewListHandler handler;
	public Toolbar toolbar;
	public MenuBuilder AllMenus;
	public List<MenuItemImpl> RandomMenu;
	
	public AlloydPanel(MainActivityUIBase a, @NonNull WebViewListHandler handler) {
		super(a, true);
		this.handler=handler;
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		setShowInDialog();
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(MainActivityUIBase) context;
			mBackgroundColor = 0;
			setShowInDialog();
		}
		if (settingsLayout==null && handler!=null) {
			SplitView linearView = handler.contentUIData.webcontentlister;
			Toolbar toolbar = this.toolbar = new Toolbar(context);
			linearView.addView(toolbar, 0);
			toolbar.getLayoutParams().height = (int) (GlobalOptions.density * 45);
			toolbar.setBackgroundColor(a.MainAppBackground);
			toolbar.setTitleTextColor(Color.WHITE);
			toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
			toolbar.inflateMenu(R.xml.menu_popup_content);
			toolbar.getLayoutParams().height = (int) a.mResource.getDimension(R.dimen.barSize);
			AllMenus = (MenuBuilder) toolbar.getMenu();
			AllMenus.checkDrawable = a.AllMenus.checkDrawable;
			AllMenus.mOverlapAnchor = PDICMainAppOptions.menuOverlapAnchor();
			// tabTranslateEach
			//AllMenus.getItems().set(4, (MenuItemImpl) ViewUtils.findInMenu(a.AllMenusStamp, R.id.tapTranslator));
			if(handler.tapSch) {
				ViewUtils.findInMenu(AllMenus.getItems(), R.id.tapSch).setChecked(true);
			}
			RandomMenu = new ArrayList<>(AllMenus.mItems);
			toolbar.setNavigationOnClickListener(v -> dismiss());
			toolbar.setOnMenuItemClickListener(a);
			
			
			settingsLayout = linearView;
		}
	}
	
	@Override
	public void refresh() {
	
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
	
	@Override
	protected void onDismiss() {
		CMN.Log("onDismiss::");
//		super.onDismiss();
//		if (mSettingsChanged!=0) {
//			//a.currentViewImpl.checkSettings(true, true);
//			mSettingsChanged=0;
//		}
//		mScrollY = settingsLayout.getScrollY();
		//if (opt.getRemPos())
		{
			WebViewmy mWebView = handler.dictView;
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
	}
}

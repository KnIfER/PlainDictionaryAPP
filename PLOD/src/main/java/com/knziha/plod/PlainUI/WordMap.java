package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import java.util.List;

/** 词链 mindmap */
public class WordMap extends AlloydPanel {
	public WordMap(MainActivityUIBase a) {
		super(a, null, false);
		isWordMap = true;
	}
	
	public WebViewListHandler getPageHandler(boolean initPopup) {
		if (this.weblistHandler==null) {
			WebViewListHandler weblistHandler = this.weblistHandler
					= new WebViewListHandler(a, ContentviewBinding.inflate(a.getLayoutInflater())
					, a.schuiMain);
			weblistHandler.alloydPanel = this;
			if (!weblistHandler.bIsPopup) {
				weblistHandler.bIsPopup = true;
				weblistHandler.tapSch = PDICMainAppOptions.tapSchPupup();
				weblistHandler.tapDef = PDICMainAppOptions.tapDefPupup();
			}
			weblistHandler.setUpContentView(a.cbar_key, null);
			weblistHandler.checkUI();
		}
		if(initPopup) {
			WebViewmy wv = weblistHandler.getMergedFrame();
			//weblistHandler.setUpContentView(cbar_key);
			weblistHandler.popupContentView(null, "单词导图");
			
			wv.setPresenter(weblistHandler.mMergedBook);
			weblistHandler.setViewMode(null, 1, wv);
			weblistHandler.initMergedFrame(1, true, false);
			
			weblistHandler.setBottomNavWeb(PDICMainAppOptions.bottomNavWeb());
			wv.active = true;
			wv.jointResult = null;
		}
		return weblistHandler;
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if (settingsLayout==null && a!=null) {
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
	
	@Override
	public void refresh() {
		super.refresh();
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
	
	public void show() {
		getPageHandler(true);
		weblistHandler.viewContent();
		weblistHandler.getMergedFrame().loadUrl("https://jv7pl7wn15.csb.app/");
		weblistHandler.getMergedFrame().loadUrl("http://192.168.0.102:8080/base/3/MdbR/mindmap.html");
	}
}

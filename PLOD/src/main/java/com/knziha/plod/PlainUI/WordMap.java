package com.knziha.plod.PlainUI;

import static com.knziha.plod.plaindict.MainActivityUIBase.closeIfNoActionView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

/** 词链 mindmap */
public class WordMap extends AlloydPanel implements Toolbar.OnMenuItemClickListener, PopupMenuHelper.PopupMenuListener {
	MapHandler mapHandler;
	
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
			opt = a.opt;
			menuResId = R.xml.menu_word_map;
			addToolbar();
			// tabTranslateEach
			//AllMenus.getItems().set(4, a.getMenuSTd(R.id.translator));
			if(weblistHandler.tapSch) {
//				if(weblistHandler.tapDef) ViewUtils.findInMenu(AllMenus.getItems(), R.id.tapSch1).setChecked(true);
//				else ViewUtils.findInMenu(AllMenus.getItems(), R.id.tapSch).setChecked(true);
			}
			weblistHandler.tapSch = false;
			//weblistHandler.tapDef = false;
			// ViewUtils.MapNumberToMenu(AllMenus, 6, 1, 7, 2, 3, 4, 9, 8, 5);//
			RandomMenu = PopupMenu = new ArrayList<>(AllMenus.mItems);
			toolbar.setNavigationOnClickListener(v -> dismiss());
			toolbar.setOnMenuItemClickListener(this);
			toolbar.getNavigationBtn().setOnLongClickListener(v -> {
				weblistHandler.showMoreToolsPopup(v);
				return true;
			});
			
			fetchWordMenu = (MenuItemImpl) ViewUtils.findInMenu(RandomMenu, R.id.fetchWord);
			//refresh();
			if (weblistHandler.fetchWord>0) {
				fetchWordMenu.setChecked(true);
			}
			
			if (BuildConfig.DEBUG) {
				ViewUtils.findInMenu(RandomMenu, R.id.refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}
			
			weblistHandler.getMergedFrame().addJavascriptInterface(mapHandler = new MapHandler(this), "word");
		}
	}
	
	public static class MapHandler {
		WordMap map;
		public MapHandler(WordMap wordPopup) {
			this.map = wordPopup;
		}
		@JavascriptInterface
		public void add(String text, int posX, int posY) {

		}
		
		@JavascriptInterface
		public String createLnk(int sid, String A, String B) {
			if (map!=null) {
				return map.createLnk(A, B);
			}
			return "";
		}
		
		@JavascriptInterface
		public boolean removeNode(String id) {
			if (map!=null) {
				return map.deleteNode(id);
			}
			return false;
		}
		
		@JavascriptInterface
		public boolean moveNode(String id, int x, int y) {
			if (map!=null) {
				return map.moveNode(id, x, y);
			}
			return false;
		}
		
		@JavascriptInterface
		public void lockScroll(int sid) {
			if (map!=null) {
				MainActivityUIBase.CustomViewHideTime = CMN.now();
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
		a.showT("功能测试中，数据不会保存！");
		getPageHandler(true);
		weblistHandler.viewContent();
//		weblistHandler.getMergedFrame().getSettings().setLoadWithOverviewMode(false);
//		weblistHandler.getMergedFrame().getSettings().setUseWideViewPort(true);
//		weblistHandler.getMergedFrame().setInitialScale((int) (100 * (1000 / BookPresenter.def_zoom) * opt.dm.density));
		weblistHandler.getMergedFrame().loadUrl("https://jv7pl7wn15.csb.app/");
//		weblistHandler.getMergedFrame().loadUrl("http://192.168.0.102:8080/base/3/MdbR/wordmap.html");
		weblistHandler.getMergedFrame().loadUrl("http://192.168.0.102:8080/base/4/wordmap.html");
		
//		VU.setVisible(weblistHandler.contentUIData.bottombar2, false);
		
		weblistHandler.getMergedFrame().setHorizontalScrollBarEnabled(true);
		weblistHandler.resetScrollbar();
		weblistHandler.getMergedFrame().pBc.tapZoom(true);
		weblistHandler.getMergedFrame().pBc.tapAlignment(4);
//		ViewUtils.setVisible(weblistHandler.contentUIData.zoomCtrl, true);
		weblistHandler.pageSlider.onSwipeTopListener = null;
		weblistHandler.pageSlider.slideTurn = false;
		weblistHandler.pageSlider.setWebview(weblistHandler.getMergedFrame(), null);
		
//		weblistHandler.getMergedFrame().getSettings().setLoadWithOverviewMode(false);
//		weblistHandler.getMergedFrame().getSettings().setUseWideViewPort(true);
//		WebView.enableSlowWholeDocumentDraw();
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:a.getDummyMenuImpl(id);
		MenuBuilder menu = (MenuBuilder) mmi.mMenu;
		boolean isLongClicked= mmi!=null && mmi.isLongClicked!=0;
		WebViewListHandler wlh = this.weblistHandler;
		/* 长按事件默认不处理，因此长按时默认返回false，且不关闭menu。 */
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		switch(id){
			case R.id.refresh:{
				wlh.getMergedFrame().reload();
			} break;
			case R.id.add:{
				String nodeHTML = "&emsp;&emsp;";
				createNode(nodeHTML, wlh.getMergedFrame());
			} break;
			case R.id.del:{
				wlh.getMergedFrame().evaluateJavascript("delSel()", null);
			} break;
			case R.id.grid:{
				View actionView = toolbar.findViewById(mmi.getItemId());
				if (actionView!=null) {
					PopupMenuHelper popupMenu = a.getPopupMenu();
					popupMenu.initLayout(new int[]{
							R.string.expand_map_hor
							, R.string.expand_map_ver
					}, this);
					int[] vLocationOnScreen = new int[2];
					actionView.getLocationOnScreen(vLocationOnScreen); //todo 校准弹出位置
					popupMenu.showAt(actionView, vLocationOnScreen[0], vLocationOnScreen[1]+actionView.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
				}
			} break;
		}
		if(closeMenu)
			closeIfNoActionView(mmi);
		return ret;
	}
	
	public void createNode(String text, WebViewmy wv) {
		wv.evaluateJavascript("getPos()", new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				try {
					value = value.substring(1);
					String[] arr = value.split(",");
					ContentValues cv = new ContentValues();
					cv.put("text", text);
					cv.put("x", "" + IU.parsint(arr[0], 0));
					cv.put("y", "" + IU.parsint(arr[1], 0));
					cv.put(LexicalDBHelper.FIELD_EDIT_TIME, CMN.now());
					cv.put(LexicalDBHelper.FIELD_CREATE_TIME, CMN.now());
					SQLiteDatabase db = a.prepareHistoryCon().getDB();
					long rowId = db.insert(LexicalDBHelper.TABLE_WORD_MAP, null, cv);
					wv.evaluateJavascript("add(\"" + StringEscapeUtils.escapeJava(text) + "\", \"" + rowId + "\")", null);
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		});
	}
	
	public String remap(int i) {
		JSONObject node = new JSONObject();
		SQLiteDatabase db = a.prepareHistoryCon().getDB();
		StringBuilder sb = new StringBuilder();
		try (Cursor cursor = db.rawQuery("select id,text,type,x,y,w,h,a,b from "+LexicalDBHelper.TABLE_WORD_MAP+" order by type asc, last_edit_time asc", null)){
			while (cursor.moveToNext()) {
				node.clear();
				node.put("id", ""+cursor.getInt(0));
				node.put("text", cursor.getString(1));
				node.put("type", cursor.getInt(2));
				node.put("x", cursor.getInt(3));
				node.put("y", cursor.getInt(4));
				node.put("w", cursor.getInt(5));
				node.put("h", cursor.getInt(6));
				node.put("a", cursor.getInt(7));
				node.put("b", cursor.getInt(8));
				CMN.debug("node.toJSONString()::", node.toJSONString());
				sb.append(node.toJSONString());
				sb.append("\0");
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return sb.toString();
	}
	
	public String createLnk(String a, String b) {
		CMN.debug("createLnk="+a,b);
		try {
			ContentValues cv = new ContentValues();
			//cv.put("text", text);
			cv.put("type", 150);
			cv.put("a", IU.parseLong(a));
			cv.put("b", IU.parseLong(b));
			cv.put("x", 0);
			cv.put("y", 0);
			cv.put(LexicalDBHelper.FIELD_EDIT_TIME, CMN.now());
			cv.put(LexicalDBHelper.FIELD_CREATE_TIME, CMN.now());
			SQLiteDatabase db = this.a.prepareHistoryCon().getDB();
			long rowId = db.insert(LexicalDBHelper.TABLE_WORD_MAP, null, cv);
			CMN.debug("rowId="+rowId);
			return rowId + "";
		} catch (Exception e) {
			CMN.debug(e);
		}
		return "";
	}
	
	private boolean deleteNode(String id) {
		try {
			SQLiteDatabase db = this.a.prepareHistoryCon().getDB();
			return db.delete(LexicalDBHelper.TABLE_WORD_MAP, "id=?", new String[]{id}) > 0;
		} catch (Exception e) {
			CMN.debug(e);
			return false;
		}
	}
	
	private boolean moveNode(String id, int x, int y) {
		try{
			ContentValues cv = new ContentValues();
			//cv.put("text", text);
			cv.put("x", x);
			cv.put("y", y);
			cv.put(LexicalDBHelper.FIELD_EDIT_TIME, CMN.now());
			SQLiteDatabase db = this.a.prepareHistoryCon().getDB();
			return db.update(LexicalDBHelper.TABLE_WORD_MAP, cv, "id=?", new String[]{id}) > 0;
		} catch (Exception e) {
			CMN.debug(e);
			return false;
		}
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		WebViewListHandler wlh = this.weblistHandler;
		switch (v.getId()) {
			case R.string.expand_map_hor:
			case R.string.expand_map_ver:
				wlh.getMergedFrame().evaluateJavascript(v.getId()==R.string.expand_map_hor?"expandMap(1500, 0)":"expandMap(0, 2500)", new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						try {
							CMN.debug("onReceiveValue::", value);
							value = value.substring(1);
							String[] arr = value.split(",");
							int w = IU.parsint(arr[0]);
							int h = IU.parsint(arr[1]);
							CMN.debug("wh", w, h);
							long sheetId = -1;
							SQLiteDatabase db = a.prepareHistoryCon().getDB();
							try (Cursor cursor = db.rawQuery("select id from " + LexicalDBHelper.TABLE_WORD_MAP + " where type=-1 and sheet=0 limit 1", null)) {
								if (cursor.moveToNext()) {
									sheetId = cursor.getLong(0);
								}
							} catch (Exception e) {
								CMN.debug(e);
							}
							ContentValues cv = new ContentValues();
							//cv.put("text", text);
							cv.put("w", w);
							cv.put("h", h);
							if (sheetId == -1) {
								cv.put(LexicalDBHelper.FIELD_EDIT_TIME, CMN.now());
								cv.put(LexicalDBHelper.FIELD_CREATE_TIME, CMN.now());
								cv.put("type", -1);
								db.insert(LexicalDBHelper.TABLE_WORD_MAP, null, cv);
								CMN.debug("创建");
							} else {
								db.update(LexicalDBHelper.TABLE_WORD_MAP, cv, "id=?", new String[]{"" + sheetId});
								CMN.debug("修改");
							}
						} catch (Exception e) {
							CMN.debug(e);
						}
					}
				});
			break;
		}
		popupMenuHelper.dismiss();
		return false;
	}
}

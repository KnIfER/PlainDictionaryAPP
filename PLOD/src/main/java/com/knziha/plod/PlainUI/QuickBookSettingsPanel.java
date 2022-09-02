package com.knziha.plod.PlainUI;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.widget.HorizontalNumberPicker;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.plaindict.databinding.QuickSettingsPanelBinding;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.DrawOverlayCompat;
import com.knziha.plod.widgets.SwitchCompatBeautiful;
import com.knziha.plod.widgets.ViewUtils;

/** 一些页面选项的快捷入口 */
public class QuickBookSettingsPanel extends PlainAppPanel implements SettingsPanel.ActionListener {
	protected MainActivityUIBase a;
	public WebViewListHandler weblist;
	protected QuickSettingsPanelBinding UIData;
	protected View sysVolEq;
	protected View webSiteInfo;
	protected View.OnClickListener webSiteInfoListener;
	protected LayoutTransition transition;
	protected ViewGroup root;
	protected Drawable[] drawables;
	protected int mSettingsChanged;
	protected HorizontalNumberPicker mTextZoomNumberPicker;
	protected static int mScrollY;
	private boolean lstPreviewChanged;
	
	public QuickBookSettingsPanel(MainActivityUIBase a) {
		super(a, true);
		mActionListener = this;
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		if(context==null) return;
		a=(MainActivityUIBase) context;
		weblist = a.weblist;
		
		setShowInPop();
		setPresetBgColorType(1);
		
		Resources mResource = a.mResource;
		drawables = new Drawable[]{
			mResource.getDrawable(R.drawable.ic_peruse)
		};
		for (Drawable drawable:drawables) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		
		inflateUIData(context, root);
		LinearLayout lv = linearLayout = UIData.root;
		
		transition = lv.getLayoutTransition();
		lv.setLayoutTransition(null);
		
		ScrollView sv = new ScrollView(context);
		
		root.postDelayed(() -> lv.setLayoutTransition(transition), 450);
		
		sv.setLayoutParams(lv.getLayoutParams());
		sv.addView(lv, new ViewGroup.LayoutParams(-1, -2));
		sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		settingsLayout = sv;
		sv.setBackgroundColor(mBackgroundColor);
		
		this.root = UIData.root;
		ViewUtils.setOnClickListenersOneDepth(UIData.root, this, 999, null);
		
		if (opt.getAdjustScnShown())
		{
			UIData.scnArrow.setRotation(90);
			initScreenPanel();
		}
		
		if (opt.getAdjustLstPreviewShown())
		{
			UIData.lstArrow.setRotation(90);
			initListPanel();
		}
		
		if (opt.getAdjSHShwn())
		{
			UIData.shArrow.setRotation(90);
			initScrollHandle();
		}
		
		if (PDICMainAppOptions.floatBtn(opt.SixthFlag()>>(30+a.thisActType.ordinal())))
		{
			UIData.floatSwitch.setChecked(true);
		}
		
		if (opt.adjTToolsShown())
		{
			UIData.ttArrow.setRotation(90);
			initTextTools();
		}
		
		if (a.thisActType==MainActivityUIBase.ActType.PlainDict) {
			if (opt.adjFltBtnShown())
			{
				initFloatBtn();
			}
		} else {
			ViewUtils.setVisible(UIData.floatPanel, false);
		}
		
		if (opt.adjPstBtnShown())
		{
			UIData.pasteArrow.setRotation(90);
			initPaste();
		}
		ViewUtils.setVisible(UIData.pastePanel, false);
		
		if (opt.adjTBtmShown())
		{
			UIData.btmArrow.setRotation(90);
			initBtmBars();
		}
		
		if(GlobalOptions.isDark) {
			View v, tv, iv;
			for (int i = 0; (v=UIData.root.getChildAt(i++))!=null; ) {
				iv = ViewUtils.findViewByClassPath(v, 0, ViewGroup.class, ImageView.class);
				if (iv!=null) {
					tv = ViewUtils.findViewByClassPath(v, 0, ViewGroup.class, TextView.class);
					if (tv!=null) {
						((TextView)tv).setTextColor(Color.WHITE);
						if(iv!=UIData.floatArrow) {
							((ImageView)iv).setColorFilter(Color.WHITE);
						}
					}
				}
			}
		}
		
		if(mScrollY>0) {
			root.post(() -> sv.scrollTo(0, mScrollY));
			//CMN.debug("重新滚动唷！", mScrollY);
		}
	}
	
	protected void inflateUIData(Context context, ViewGroup root) {
		if (UIData==null) {
			UIData = QuickSettingsPanelBinding.inflate(LayoutInflater.from(context), root, false);
		}
	}
	
	public void refresh() {
		CMN.debug("刷新全部数据...");
		this.weblist = a.weblist;
		if (_screen !=null) _screen.refresh();
		if (_lstPrv !=null) _lstPrv.refresh();
		if (_sHandle !=null) initScrollHandle();
		if (_tTools !=null) initTextTools();
		if (_btmBars !=null) initBtmBars();
		if (_fltBtn !=null) initFloatBtn();
		if (webSiteInfoListener!=null) webSiteInfoListener.onClick(null);
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
		boolean checked = v instanceof SwitchCompatBeautiful && ((SwitchCompatBeautiful) v).isChecked();
		switch (v.getId()) {
			case R.id.scn: {
				boolean show = opt.togAdjScnShwn();
				UIData.scnArrow.animate().rotation(show?90:0);
				setPanelVis(initScreenPanel(), show);
			}  break;
			case R.id.lst: {
				boolean show = opt.togAdjustLstPreviewShown();
				UIData.lstArrow.animate().rotation(show?90:0);
				setPanelVis(initListPanel(), show);
			}  break;
			case R.id.sh: {
				boolean show = opt.togAdjSHShwn();
				UIData.shArrow.animate().rotation(show?90:0);
				setPanelVis(initScrollHandle(), show);
			}  break;
			case R.id.tt: {
				boolean show = opt.adjTToolsShownTog();
				UIData.ttArrow.animate().rotation(show?90:0);
				setPanelVis(initTextTools(), show);
			}  break;
			case R.id.btm: {
				boolean show = opt.adjTBtmShownTog();
				UIData.btmArrow.animate().rotation(show?90:0);
				setPanelVis(initBtmBars(), show);
			}  break;
			case R.id.info:{
				//boolean show = opt.toggleAdjustWebsiteInfoShown();
				//UIData.infoArrow.animate().rotation(show?90:0);
				//setPanelVis(initInfoPanel(), show);
			} break;
			case R.id.flt:{
				boolean show = opt.adjFltBtnTog();
				setPanelVis(initFloatBtn(), show);
			} break;
			case R.id.float_switch:{
				boolean check = v.getTag()==null;
				if (check) v.setTag(v);
				if(check && !ViewUtils.canDrawOverlays(a)) {
					DrawOverlayCompat.manage(a);
					UIData.floatSwitch.setChecked(false);
					a.showT("需要权限“显示在其他应用上层”！");
					v.setTag(v);
				} else {
					boolean isChecked = UIData.floatSwitch.isChecked();
					if (a.thisActType==MainActivityUIBase.ActType.PlainDict) {
						PDICMainAppOptions.floatBtn(isChecked);
					} else if (a.thisActType==MainActivityUIBase.ActType.FloatSearch) {
						PDICMainAppOptions.floatBtnFlt(isChecked);
					} else {
						PDICMainAppOptions.floatBtnMtd(isChecked);
					}
					a.checkFloatBtn();
				}
			} break;
			default:
				if (v instanceof RadioSwitchButton)
					super.onClick(v);
				else {
					if (v!=root) {
						dismiss();
					}
					if (v.getId()!=R.drawable.ic_menu_24dp) {
						a.mInterceptorListenerHandled = true;
					}
				}
		}
	}
	
	protected void setPanelVis(SettingsPanel p, boolean show) {
		p.settingsLayout.setVisibility(show?View.VISIBLE:View.GONE);
	}
	
	@Override
	public boolean onAction(View v, SettingsPanel settingsPanel, int flagIdxSection, int flagPos, boolean dynamic, boolean val, int storageInt) {
		CMN.debug("onAction", flagIdxSection, flagPos, dynamic, val, makeInt(5, 35, false));
		if (settingsPanel==_btmBars) {
			SearchUI.btmV++;
			weblist.setViewMode();
			return true;
		}
		if (flagIdxSection!=0) {
			if (dynamic) {
//				if (mFlagAdapter.getDynamicFlagIndex(flagIdxSection)<6) {
//					WebFrameLayout.GlobalSettingsVersion ++;
//				}
//				if (flagIdxSection==LockSettings) {
//					ResetLockSettings();
//				} else if (flagIdxSection==TextSettings) {
//					ResetTextSettings(null, 0);
//				} else if (flagIdxSection==ImmersiveSettings) {
//					a.ResetIMSettings();
//				} else {
//					mSettingsChanged|=flagIdxSection;
//				}
			} else if(flagIdxSection==1) {
//				if (flagPos==8) {
//					if (val) {
//						a.acquireWakeLock();
//					} else {
//						a.releaseWakeLock();
//					}
//				}
				if (storageInt==makeInt(3,35,false)) {
					opt.setUserOrientation(opt.getTmpUserOrientation());
				}
			}
			if (storageInt==makeInt(6, 40, false)) {
				MainActivityUIBase.VerseKit tk = a.getVtk();
				if (PDICMainAppOptions.toolsQuick()) { // hint
					int action =  PDICMainAppOptions.toolsQuickAction();
					a.showTopSnack(action<tk.arraySelUtils.length?tk.arraySelUtils[action]:tk.arraySelUtils2[action-tk.arraySelUtils.length]);
				}
			}
			if (v!=null && _lstPrv !=null && v.getParent()==_lstPrv.linearLayout) {
				if (storageInt==makeInt(7, 12, false)) {
					initListPanel();
				}
				lstPreviewChanged = true;
			}
		}
		RadioSwitchButton btn = v == null || v.getClass() != RadioSwitchButton.class ? null : (RadioSwitchButton) v;
		if (dynamic && (flagIdxSection==NONE_SETTINGS_GROUP1 || flagIdxSection==NONE_SETTINGS_GROUP2)) {
			ActionGp_1 var = ActionGp_1.values()[flagPos];
			//CMN.debug("NONE_SETTINGS_GROUP1::", var.name());
			switch (var) {
				// 缩放值预设
//				case zoom: {
//					PopupMenuHelper popupMenu = a.getPopupMenu();
//					if (popupMenu.tag!=R.string.ts_100) {
//						int[] texts = new int[] {
//							R.string.ts_75
//							,R.string.ts_90
//							,R.string.ts_100
//							,R.string.ts_110
//							,R.string.ts_125
//							,R.string.ts_150
//						};
//						popupMenu.leftDrawable = a.mResource.getDrawable(R.drawable.ic_yes_blue);
//						popupMenu.initLayout(texts, (popupMenuHelper, v, isLongClick) -> {
//							int value = 110;
//							switch (v.getId()) {
//								case R.string.ts_75: {
//									value = 75;
//								} break;
//								case R.string.ts_90: {
//									value = 90;
//								} break;
//								case R.string.ts_100: {
//									value = 100;
//								} break;
//								case R.string.ts_125: {
//									value = 125;
//								} break;
//								case R.string.ts_150: {
//									value = 150;
//								} break;
//							}
//							ResetTextSettings(null, value);
//							setTextZoomNumber();
//							popupMenuHelper.postDismiss(80);
//							return true;
//						});
//						popupMenu.tag=R.string.ts_100;
//					}
//					int[] vLocationOnScreen = new int[2];
//					settingsLayout.getLocationOnScreen(vLocationOnScreen);
//					XYLinearLayout xy = UIData.root;
//					popupMenu.show(a.root, vLocationOnScreen[0]+(int) (60* GlobalOptions.density), vLocationOnScreen[1]+(int) (xy.lastY-settingsLayout.getScrollY()));
//				} break;
				// 切换屏幕方向
				case hengping1:
				case hengping2:
				case hengping3:
				case shuping1:
				case shuping2:
				case shuping3:
				case zhongli:
				case xitong:
				case lock: {
					a.setScreenOrientation(var.ordinal()-1);
					a.root.post(this::initScreenPanel);
				} break;
				case kaoyou:
				case kaozuo:
				case hide:
				case system: {
					weblist.setScrollHandType(var.ordinal()-ActionGp_1.kaoyou.ordinal());
					a.root.post(this::initScrollHandle);
				} break;
				case ttools: {
					weblist.invokeToolsBtn(true, 0);
					dismiss();
				} break;
				case floatBtn: {
					DrawOverlayCompat.manage(a);
				} break;
				case floatApp: {
					if (a instanceof PDICMainActivity) {
						((PDICMainActivity)a).toggleMultiwindow();
						dismiss();
					}
				} break;
				case pFontClr1:
				case pFontClr2:
				case pFontClr3:
				{
					PDICMainAppOptions.listPreviewColor(var.ordinal()-ActionGp_1.pFontClr1.ordinal());
				} break;
				case pFontSz1:
				case pFontSz2:
				case pFontSz3:
				{
					PDICMainAppOptions.listPreviewFont(var.ordinal()-ActionGp_1.pFontSz1.ordinal());
				} break;
				case p1FontClr1:
				case p1FontClr2:
				case p1FontClr3:
				{
					PDICMainAppOptions.listPreviewColor1(var.ordinal()-ActionGp_1.p1FontClr1.ordinal());
				} break;
				case p1FontSz1:
				case p1FontSz2:
				case p1FontSz3:
				{
					PDICMainAppOptions.listPreviewFont1(var.ordinal()-ActionGp_1.p1FontSz1.ordinal());
				} break;
				case p1ViewSz1:
				case p1ViewSz2:
				case p1ViewSz3:
				{
					PDICMainAppOptions.listPreviewSize1(var.ordinal()-ActionGp_1.p1ViewSz1.ordinal());
				} break;
			}
			if (btn!=null && (var.ordinal()>=ActionGp_1.pFontClr1.ordinal() && var.ordinal()<=ActionGp_1.p1ViewSz3.ordinal())) {
				ViewGroup vg = (ViewGroup) btn.getParent();
				for (int i = 1; i < vg.getChildCount(); i++) {
					v = vg.getChildAt(i);
					((RadioSwitchButton)v).setChecked(v==btn);
				}
				lstPreviewChanged = true;
			}
		}
		return true;
	}
	
	@Override
	public void onPickingDelegate(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, int lastX, int lastY) {
	
	}
	
	private int getIconResForDynamicFlagBySection(int section) {
		return R.drawable.ic_peruse;
	}
	
	@Override
	protected Drawable getIconForDynamicFlagBySection(int section) {
		return drawables[0];
	}
	
	SettingsPanel _screen;
	SettingsPanel _lstPrv;
	SettingsPanel _sHandle;
	SettingsPanel _tTools;
	SettingsPanel _fltBtn;
	SettingsPanel _paste;
	SettingsPanel _btmBars;
	
	int shType;
	
	public final static int NONE_SETTINGS_GROUP1=0;
	public final static int NONE_SETTINGS_GROUP2=1;
	
	enum ActionGp_1 {
		zoom
		,hengping1
		,hengping2
		,hengping3
		,shuping1
		,shuping2
		,shuping3
		,zhongli
		,xitong
		,lock
		,kuodaxuanze
		,fanyixuanze
		,xuanduanfanyi
		,quanwenfanyi
		,quanxuan
		,dakaipdf
		,tianjiapdflnk
		,dakaipdfwenjian
		,pad1
		,pad2
		,pad3
		,kaoyou
		,kaozuo
		,hide
		,system
		,ttools
		,floatBtn
		,floatApp
		,pFontClr1
		,pFontClr2
		,pFontClr3
		,pFontSz1
		,pFontSz2
		,pFontSz3
		,p1FontClr1
		,p1FontClr2
		,p1FontClr3
		,p1FontSz1
		,p1FontSz2
		,p1FontSz3
		,p1ViewSz1
		,p1ViewSz2
		,p1ViewSz3
	}
	
	private SettingsPanel initListPanel() {
		if (_lstPrv ==null) {
			final SettingsPanel lstSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{"<font color='#3185F7'><u>旧列表设置</u></font>：", "在列表中预览词条释义", "通读模式", "使列表文本可选", "新旧列表使用同一套配置"}
			, new String[]{"<font color='#3185F7'><u>新列表设置（搜索引擎）</u></font>：", "预览词条释义", "从原词典获取预览", "通读模式", "使列表文本可选"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeInt(7, 5, false) // listPreviewEnabled
					, makeInt(7, 8, false) // listOverreadMode
					, makeInt(7, 9, false) // listPreviewSelectable
					, makeInt(7, 12, false) // listPreviewSet01Same
			}, new int[]{Integer.MAX_VALUE
					, makeInt(7, 13, false) // listPreviewEnabled1
					, makeInt(7, 14, false) // listPreviewOriginal1
					, makeInt(7, 17, false) // listOverreadMode1
					, makeInt(7, 20, false) // listPreviewSelectable1
			}}, null);
			lstSettings.setEmbedded(this);
			lstSettings.init(a, root);
			int level;
			{
				level = PDICMainAppOptions.listPreviewColor();
				final SettingsPanel fontClr = new SettingsPanel(a, opt
						, new String[][]{new String[]{"预览色：", "浅", "中", "深"}}
						, new int[][]{new int[]{Integer.MAX_VALUE
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.pFontClr1.ordinal(), level==0)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.pFontClr2.ordinal(), level==1)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.pFontClr3.ordinal(), level==2)
				}}, null);
				fontClr.setHorizontalItems(true);
				fontClr.setEmbedded(this);
				fontClr.init(a, root);
				ViewUtils.setPadding(fontClr.settingsLayout, (int) (GlobalOptions.density*4), 0, 0, 0);
				
				level = PDICMainAppOptions.listPreviewFont();
				final SettingsPanel fontSz = new SettingsPanel(a, opt
						, new String[][]{new String[]{"预览字体：", "小", "中", "大"}}
						, new int[][]{new int[]{Integer.MAX_VALUE
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.pFontSz1.ordinal(), level==0)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.pFontSz2.ordinal(), level==1)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.pFontSz3.ordinal(), level==2)
				}}, null);
				fontSz.setHorizontalItems(true);
				fontSz.setEmbedded(this);
				fontSz.init(a, root);
				ViewUtils.setPadding(fontSz.settingsLayout, (int) (GlobalOptions.density*4), 0, 0, 0);
				
//				((RadioSwitchButton)fontClr.linearLayout.getChildAt(1+PDICMainAppOptions.listPreviewColor())).setChecked(true);
//				((RadioSwitchButton)fontSz.linearLayout.getChildAt(1+PDICMainAppOptions.listPreviewFont())).setChecked(true);
				ViewUtils.addViewToParent(fontClr.settingsLayout, lstSettings.settingsLayout, 3);
				ViewUtils.addViewToParent(fontSz.settingsLayout, lstSettings.settingsLayout, 4);
			}
			{
				level = PDICMainAppOptions.listPreviewColor1();
				final SettingsPanel fontClr = new SettingsPanel(a, opt
						, new String[][]{new String[]{"预览色：", "浅", "中", "深"}}
						, new int[][]{new int[]{Integer.MAX_VALUE
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1FontClr1.ordinal(), level==0)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1FontClr2.ordinal(), level==1)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1FontClr3.ordinal(), level==2)
				}}, null);
				fontClr.setHorizontalItems(true);
				fontClr.setEmbedded(this);
				fontClr.init(a, root);
				ViewUtils.setPadding(fontClr.settingsLayout, (int) (GlobalOptions.density*4), 0, 0, 0);
				
				level = PDICMainAppOptions.listPreviewFont1();
				final SettingsPanel fontSz = new SettingsPanel(a, opt
						, new String[][]{new String[]{"预览字体：", "小", "中", "大"}}
						, new int[][]{new int[]{Integer.MAX_VALUE
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1FontSz1.ordinal(), level==0)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1FontSz2.ordinal(), level==1)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1FontSz3.ordinal(), level==2)
				}}, null);
				fontSz.setHorizontalItems(true);
				fontSz.setEmbedded(this);
				fontSz.init(a, root);
				ViewUtils.setPadding(fontSz.settingsLayout, (int) (GlobalOptions.density*4), 0, 0, 0);
				
				level = PDICMainAppOptions.listPreviewSize1();
				final SettingsPanel viewSz = new SettingsPanel(a, opt
						, new String[][]{new String[]{"预览长度：", "短", "中", "长"}}
						, new int[][]{new int[]{Integer.MAX_VALUE
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1ViewSz1.ordinal(), level==0)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1ViewSz2.ordinal(), level==1)
						, makeDynInt(NONE_SETTINGS_GROUP2, ActionGp_1.p1ViewSz3.ordinal(), level==2)
				}}, null);
				viewSz.setHorizontalItems(true);
				viewSz.setEmbedded(this);
				viewSz.init(a, root);
				ViewUtils.setPadding(viewSz.settingsLayout, (int) (GlobalOptions.density*4), 0, 0, 0);
				
//				((RadioSwitchButton)fontClr.linearLayout.getChildAt(1+PDICMainAppOptions.listPreviewColor1())).setChecked(true);
//				((RadioSwitchButton)fontSz.linearLayout.getChildAt(1+PDICMainAppOptions.listPreviewFont1())).setChecked(true);
//				((RadioSwitchButton)viewSz.linearLayout.getChildAt(1+PDICMainAppOptions.listPreviewSize1())).setChecked(true);
				ViewUtils.addViewToParent(fontClr.settingsLayout, lstSettings.settingsLayout, 3+8);
				ViewUtils.addViewToParent(fontSz.settingsLayout, lstSettings.settingsLayout,  4+8);
				ViewUtils.addViewToParent(viewSz.settingsLayout, lstSettings.settingsLayout,  5+8);
			}
			
			this._lstPrv = lstSettings;
			addPanelViewBelow(lstSettings.settingsLayout, UIData.lstPanel);
		}
		LinearLayout lv = _lstPrv.linearLayout;
		if (PDICMainAppOptions.listPreviewSet01Same() ^ !ViewUtils.isVisibleV2(lv.getChildAt(10))) {
			int vis = PDICMainAppOptions.listPreviewSet01Same() ? View.GONE : View.VISIBLE;
			for (int i = 10; i < lv.getChildCount(); i++) {
				lv.getChildAt(i).setVisibility(vis);
			}
		}
		return _lstPrv;
	}
	
	private SettingsPanel initScreenPanel() {
		if (_screen ==null) {
			final SettingsPanel screenSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "重力感应方向", "跟随系统方向", "锁定当前方向", "锁定启动方向"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.zhongli.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.xitong.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.lock.ordinal(), true)
					, makeInt(5, 35, false) // getLockStartOrientation
			}}, null);
			screenSettings.setEmbedded(this);
			screenSettings.init(a, root);
			
			final SettingsPanel hengping = new SettingsPanel(a, opt
					, new String[][]{new String[]{"切换横屏：", "重力感应", "正向", "反向"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping1.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping2.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping3.ordinal(), true)
			}}, null);
			final SettingsPanel shuping = new SettingsPanel(a, opt
					, new String[][]{new String[]{"切换竖屏：", "重力感应", "正向", "反向"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping1.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping2.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping3.ordinal(), true)
			}}, null);
			hengping.setEmbedded(this);
			hengping.init(a, root);
			shuping.setEmbedded(this);
			shuping.init(a, root);
			LinearLayout ll = new LinearLayout(a);
			View sep = new View(a);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ViewUtils.addViewToParent(hengping.settingsLayout, ll);
			ViewUtils.addViewToParent(sep, ll);
			ViewUtils.addViewToParent(shuping.settingsLayout, ll);
			hengping.settingsLayout.setBackgroundResource(R.drawable.frame);
			shuping.settingsLayout.setBackgroundResource(R.drawable.frame);
			((LinearLayout.LayoutParams)hengping.settingsLayout.getLayoutParams()).weight = 1;
			((LinearLayout.LayoutParams)shuping.settingsLayout.getLayoutParams()).weight = 1;
			((LinearLayout.LayoutParams)sep.getLayoutParams()).width = (int) (GlobalOptions.density*15);
			int pad = (int) (GlobalOptions.density*5);
			ll.setPadding(pad, 0, pad, 0);
			ViewUtils.addViewToParent(ll, screenSettings.settingsLayout, 0);
			
			addPanelViewBelow(screenSettings.settingsLayout, UIData.scnPanel);
			this._screen = screenSettings;
		}
		//if(opt.getLockStartOrientation() || fromSettingsChange)
		{
//			,hengping1
//			,hengping2
//			,hengping3
//			,shuping1
//			,shuping2
//			,shuping3
//			,zhongli
//			,xitong
			RadioSwitchButton btn;
			int lastIdx = opt.getLockStartOrientation()?opt.getUserOrientation():opt.getTmpUserOrientation1();
			int id = makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.values()[ActionGp_1.hengping1.ordinal()+lastIdx].ordinal(), true);
			int idLast = makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.values()[ActionGp_1.hengping1.ordinal()+opt.getTmpUserOrientation()].ordinal(), true);
			//if(idLast!=id)
			{
				btn = _screen.settingsLayout.findViewById(idLast);
				btn.setChecked(false);
			}
			CMN.debug(idLast, btn.getText());
			btn = _screen.settingsLayout.findViewById(id);
			btn.setChecked(true);
			CMN.debug(id, btn.getText());
			opt.setTmpUserOrientation(lastIdx);
		}
		return _screen;
	}
	
	private SettingsPanel initScrollHandle() {
		shType = weblist.getScrollHandType();
		if (_sHandle ==null) {
			final SettingsPanel settings = new SettingsPanel(a, opt
					, new String[][]{new String[]{"滚动条样式", "靠右", "靠左", "隐藏滚动条", "使用系统滚动条"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.kaoyou.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.kaozuo.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hide.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.system.ordinal(), true)
			}}, null);
			settings.setEmbedded(this);
			settings.init(a, root);
			
			addPanelViewBelow(settings.settingsLayout, UIData.shPanel);
			_sHandle = settings;
		}
		RadioSwitchButton btn;
		for (int i=0,id; i < 4; i++) {
			id = makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.kaoyou.ordinal()+i, true);
			btn = _sHandle.settingsLayout.findViewById(id);
			btn.setChecked(shType==i);
		}
		return _sHandle;
	}
	
	private SettingsPanel initTextTools() {
		if (_tTools ==null) {
			final SettingsPanel settings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "启用", "直接使用选中的功能"/*, "长按使用选择的功能"*/, "选择功能…"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeInt(3, 50, true) // wvShowToolsBtn
					, makeInt(6, 40, false) // toolsQuickAction
					//, makeInt(6, 47, true) // toolsQuickLong
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.ttools.ordinal(), true)
			}}, null);
			settings.setEmbedded(this);
			settings.init(a, root);
			
			addPanelViewBelow(settings.settingsLayout, UIData.ttPanel);
			_tTools = settings;
		} else {
			_tTools.refresh();
		}
		return _tTools;
	}
	
	private SettingsPanel initFloatBtn() {
		if (_fltBtn ==null) {
			final SettingsPanel settings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "管理悬浮窗权限", "切换小窗模式"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.floatBtn.ordinal(), false)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.floatApp.ordinal(), false)
			}}, null);
			settings.setEmbedded(this);
			settings.init(a, root);
			
			addPanelViewBelow(settings.settingsLayout, UIData.floatPanel);
			_fltBtn = settings;
		} else {
			_fltBtn.refresh();
		}
		return _fltBtn;
	}
	
	private SettingsPanel initPaste() {
		if (_paste ==null) {
			final SettingsPanel settings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "", "切换小窗模式"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.floatBtn.ordinal(), false)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.floatApp.ordinal(), false)
			}}, null);
			settings.setEmbedded(this);
			settings.init(a, root);
			
			addPanelViewBelow(settings.settingsLayout, UIData.pastePanel);
			_paste = settings;
		} else {
			_paste.refresh();
		}
		return _paste;
	}
	
	private SettingsPanel initBtmBars() {
		if (_btmBars==null) {
			final SettingsPanel settings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "拖动条（联合搜索）", "词典名称"
					, "切换上一个（联合搜索）", "切换下一个（联合搜索）", "切换上一个（小）", "切换下一个（小）"
					, "缩放按钮"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeInt(6, 59, true) // showEntrySeek
					, makeInt(5, 27, true) // showDictName
					//, makeInt(6, 47, true) // toolsQuickLong
					, makeInt(3, 19, true) // showPrvBtn
					, makeInt(6, 50, true) // showNxtBtn
					, makeInt(2, 4, false) // showPrvBtnSmall
					, makeInt(2, 5, false) // showNxtBtnSmall
					, makeInt(6, 51, false) // showZoomBtn
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.ttools.ordinal(), true)
			}}, null);
			settings.setEmbedded(this);
			settings.init(a, root);
			
			addPanelViewBelow(settings.settingsLayout, UIData.btmPanel);
			_btmBars = settings;
		} else {
			_btmBars.refresh();
		}
		return _btmBars;
	}
	
	
	protected void addPanelViewBelow(View settingsLayout, LinearLayout panelTitle) {
		ViewUtils.addViewToParent(settingsLayout, root, panelTitle);
	}
	
	
	@Override
	protected void onDismiss() {
		//CMN.debug("onDismiss::", mSettingsChanged);
		super.onDismiss();
		if (mSettingsChanged!=0) {
			//a.currentViewImpl.checkSettings(true, true);
			mSettingsChanged=0;
		}
		if (lstPreviewChanged) {
			a.invalidAllListsReal();
		}
		mScrollY = settingsLayout.getScrollY();
	}
	
	@Override
	protected void decorateInterceptorListener(boolean install) {
		if (install) {
			//a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess_ic_back);
			//a.UIData.browserWidget8.setImageResource(R.drawable.chevron_forward_settings);
		} else {
			//a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess);
			//a.UIData.browserWidget8.setImageResource(R.drawable.chevron_forward);
		}
	}
}

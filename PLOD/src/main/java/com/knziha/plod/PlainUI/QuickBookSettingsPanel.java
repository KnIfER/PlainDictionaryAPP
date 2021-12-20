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
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.widget.HorizontalNumberPicker;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.QuickSettingsPanelBinding;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.SwitchCompatBeautiful;
import com.knziha.plod.widgets.Utils;

public class QuickBookSettingsPanel extends PlainAppPanel implements SettingsPanel.ActionListener {
	protected MainActivityUIBase a;
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
	
	public QuickBookSettingsPanel(MainActivityUIBase a) {
		super(a);
		mActionListener = this;
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		a=(MainActivityUIBase) context;
		
		showInPopWindow = true;
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
		
		this.root = UIData.root;
		Utils.setOnClickListenersOneDepth(UIData.root, this, 999, null);
		
		if (opt.getAdjustScnShown())
		{
			UIData.scnArrow.setRotation(90);
			initScreenPanel();
		}
		
		if(GlobalOptions.isDark) {
			UIData.scn.setTextColor(Color.WHITE);
		}
		
		if(mScrollY>0) {
			root.post(() -> sv.scrollTo(0, mScrollY));
			//CMN.Log("重新滚动唷！", mScrollY);
		}
	}
	
	protected void inflateUIData(Context context, ViewGroup root) {
		if (UIData==null) {
			UIData = QuickSettingsPanelBinding.inflate(LayoutInflater.from(context), root, false);
		}
	}
	
	public void refresh() {
		CMN.Log("刷新全部数据...");
		if (screenSettings!=null) screenSettings.refresh();
		if (webSiteInfoListener!=null) webSiteInfoListener.onClick(null);
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
		boolean checked = v instanceof SwitchCompatBeautiful && ((SwitchCompatBeautiful) v).isChecked();
		switch (v.getId()) {
			case R.id.scn: {
				boolean show = opt.toggleAdjustScnShown();
				UIData.scnArrow.animate().rotation(show?90:0);
				setPanelVis(initScreenPanel().settingsLayout, show);
			}  break;
			case R.id.info:{
				//boolean show = opt.toggleAdjustWebsiteInfoShown();
				//UIData.infoArrow.animate().rotation(show?90:0);
				//setPanelVis(initInfoPanel(), show);
			} break;
			default:
				if (v instanceof RadioSwitchButton)
					super.onClick(v);
				else {
					dismiss();
					if (v.getId()!=R.drawable.ic_menu_24dp) {
						a.mInterceptorListenerHandled = true;
					}
				}
		}
	}
	
	protected void setPanelVis(View settingsLayout, boolean show) {
		settingsLayout.setVisibility(show?View.VISIBLE:View.GONE);
	}
	
	@Override
	public void onAction(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, boolean dynamic, boolean val) {
		CMN.Log("onAction", flagIdxSection, flagPos, dynamic, val);
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
			}
		}
		if (flagIdxSection==NONE_SETTINGS_GROUP1) {
			ActionGp_1 var = ActionGp_1.values()[flagPos];
			//CMN.Log("NONE_SETTINGS_GROUP1::", var.name());
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
				} break;
			}
		}
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
	
	
	SettingsPanel screenSettings;
	
	public final static int NONE_SETTINGS_GROUP1=0;
	
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
	}
	
	private SettingsPanel initScreenPanel() {
		if (screenSettings==null) {
			final SettingsPanel screenSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "重力感应方向", "跟随系统方向", "锁定当前方向"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.zhongli.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.xitong.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.lock.ordinal(), true)
					//, makeInt(1, 8, false) // getLockScreenOn
			}}, null);
			screenSettings.setEmbedded(this);
			screenSettings.init(a, root);
			
			final SettingsPanel hengping = new SettingsPanel(a, opt
					, new String[][]{new String[]{"切换横屏：", "重力感应", "正向", "反向"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping1.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping2.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping3.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping1.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping2.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping3.ordinal(), true)
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
			Utils.addViewToParent(hengping.settingsLayout, ll);
			Utils.addViewToParent(sep, ll);
			Utils.addViewToParent(shuping.settingsLayout, ll);
			hengping.settingsLayout.setBackgroundResource(R.drawable.frame);
			shuping.settingsLayout.setBackgroundResource(R.drawable.frame);
			((LinearLayout.LayoutParams)hengping.settingsLayout.getLayoutParams()).weight = 1;
			((LinearLayout.LayoutParams)shuping.settingsLayout.getLayoutParams()).weight = 1;
			((LinearLayout.LayoutParams)sep.getLayoutParams()).width = (int) (GlobalOptions.density*15);
			int pad = (int) (GlobalOptions.density*5);
			ll.setPadding(pad, 0, pad, 0);
			Utils.addViewToParent(ll, screenSettings.settingsLayout, 0);
			
			addPanelViewBelow(screenSettings.settingsLayout, UIData.scnPanel);
			this.screenSettings = screenSettings;
		}
		return screenSettings;
	}
	
	protected void addPanelViewBelow(View settingsLayout, LinearLayout panelTitle) {
		Utils.addViewToParent(settingsLayout, root, panelTitle);
	}
	
	
	@Override
	protected void onDismiss() {
		//CMN.Log("onDismiss::", mSettingsChanged);
		super.onDismiss();
		if (mSettingsChanged!=0) {
			//a.currentViewImpl.checkSettings(true, true);
			mSettingsChanged=0;
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

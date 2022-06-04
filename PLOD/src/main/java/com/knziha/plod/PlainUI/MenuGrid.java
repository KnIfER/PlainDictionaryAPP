package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.widgets.DescriptiveImageView;
import com.knziha.plod.widgets.ViewUtils;

public class MenuGrid extends PlainAppPanel {
	MainActivityUIBase a;
	DisplayMetrics dm;
	
	private ScrollView menu_grid;
	private boolean MenuClicked;
	private int lastWidth;
	private int lastHeight;
	private DescriptiveImageView menu_icon5;
	private int btnPaddingH;
	private int btnShareBundleResId = R.drawable.abc_ic_menu_share_mtrl_alpha;
	
	public MenuGrid(MainActivityUIBase a) {
		super(a, true);
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		a=(MainActivityUIBase) context;
		showPopOnAppbar = true;
		
		setShowInPop();
		setPresetBgColorType(0);
		dm = a.dm;
		
		DescriptiveImageView.createTextPainter(true);
		
		settingsLayout = (ViewGroup) LayoutInflater.from(a).inflate(R.layout.menu_grid, root, false);
		settingsLayout.setOnClickListener(this);
		
		bgView = menu_grid = settingsLayout.findViewById(R.id.menu_grid);
		
		menu_grid.getBackground().setColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
		
		ViewGroup svp = (ViewGroup) menu_grid.getChildAt(0);
		//menu_grid.setTranslationY(TargetTransY + legalMenuTransY);
		ViewUtils.setOnClickListenersOneDepth(svp, a, 999, null);
		//refreshMenuGridSize(true);
		
		menu_icon5 = settingsLayout.findViewById(R.drawable.abc_ic_menu_share_mtrl_alpha);
	}
	
	public boolean show(ViewGroup root, boolean contentview, int forceShowType) {
		dismissImmediate();
		boolean ret = super.toggle(root, null, forceShowType);
		//menu_grid.focusable=ret;
		if (lastWidth!=a.root.getWidth() || lastHeight!=a.root.getHeight()) {
			refreshMenuGridSize(true);
		}
		if(btnPaddingH==0){
			btnPaddingH = menu_icon5.getPaddingLeft();
		}
		if (ret && ((btnShareBundleResId==R.drawable.abc_ic_menu_share_mtrl_alpha) ^ contentview)) {
			int paddingH = btnPaddingH;
			if(contentview) {
				menu_icon5.setImageResource(btnShareBundleResId=R.drawable.abc_ic_menu_share_mtrl_alpha);
				menu_icon5.setAlpha(.2f);
				menu_icon5.setText("分享至…");
			} else {
				menu_icon5.setImageResource(btnShareBundleResId=R.drawable.book_bundle2);
				menu_icon5.setAlpha(1.f);
				menu_icon5.setText("管理词典");
				paddingH -= GlobalOptions.density*1.5;
			}
			menu_icon5.setPadding(paddingH, menu_icon5.getPaddingTop(), paddingH, menu_icon5.getPaddingBottom());
		}
		return ret;
	}
	
	@Override
	public void onClick(View v) {
		if(MenuClicked) {
			return;
		}
		dismiss();
		//MenuClicked = true;
		switch (v.getId()) {
			case R.drawable.ic_menu_24dp: {
				a.mInterceptorListenerHandled=true;
				dismiss();
			} break;
			case R.id.browser_widget9: {
				a.mInterceptorListenerHandled=true;
				a.moveTaskToBack(false);
				hide();
			} break;
			/* 历史 */
			case R.id.root: {
				dismiss();
			} break;
			case R.drawable.abc_ic_menu_share_mtrl_alpha: {
				if (btnShareBundleResId==R.drawable.abc_ic_menu_share_mtrl_alpha) {
				
				} else {
					a.showDictionaryManager();
					a.mInterceptorListenerHandled = true;
					break;
				}
			}
			default:
				if (v.getTag() instanceof String) {
					a.showT(v.getTag());
				}
			break;
		}
		//v.postDelayed(() -> toggleMenuGrid(true), 250);
	}
	
	public void refreshMenuGridSize(boolean init) {
		if (bIsShowing||init) {
			lastWidth=a.root.getWidth();
			lastHeight=a.root.getHeight();
			int w = lastWidth;
			if(w>lastHeight) {
				w -= GlobalOptions.density*18;
			}
			int maxWidth = Math.min(w, (int) (GlobalOptions.density*560));
			if(ViewUtils.actualLandscapeMode(a)) {
				maxWidth = Math.min(lastHeight, maxWidth);
			}
			//menu_grid.setBackgroundResource(R.drawable.frame_top_rounded);
			ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menu_grid.getLayoutParams();
			int maxHeight = a.root.getHeight();
			layoutParams.width=maxWidth;
			int BPad = mInnerBottomPadding;
			//BPad = a.bottombar.getHeight()*2;
			layoutParams.height= GlobalOptions.density*200>maxHeight?maxHeight+BPad:-2;
			//layoutParams.height= (int) (GlobalOptions.density*100)+mInnerBottomPadding;
			//layoutParams.height= 721;
			//CMN.Log("refreshMenuGridSize::", layoutParams.width, layoutParams.height, maxHeight, mInnerBottomPadding);
			if(GlobalOptions.isLarge) {
				layoutParams.setMarginEnd((int) (15* GlobalOptions.density));
				int pad = (int) (GlobalOptions.density*8);
				int HPad = (int) (pad*2.25);
				menu_grid.setPadding(HPad, pad/2, HPad, pad*2+BPad);
			} else {
				menu_grid.setPadding(0, 0, 0, BPad);
			}
			menu_grid.requestLayout();
		}
	}
}

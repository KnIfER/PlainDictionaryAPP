package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.ActivityQrBinding;
import com.knziha.plod.tesseraction.QRActivity;
import com.knziha.plod.widgets.ViewUtils;

public class WordCamera extends PlainAppPanel {
	ActivityQrBinding UIData;
	public WordCamera(MainActivityUIBase a) {
		super(a, false);
		this.a = a;
		bAnimate=false;
		bAutoRefresh=false;
		showType=1;
		bottomPadding=0;
		resizeDlg = true;
	}
	
	public void show() {
		if (!isVisible()) {
			toggle(null, null, 2);
		} else if (getLastShowType()==2) {
			ViewUtils.ensureTopmost(dialog, a, dialogDismissListener);
		}
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		if (settingsLayout == null && a!=null) {
			settingsLayout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.word_camera, a.root, false);
			
		}
	}
	
	public void refresh() {
//		if(weblistHandler != null)
//		{
//			CMN.debug("wordPopup::refresh");
//			if (MainColorStamp != a.MainAppBackground) {
//				if (appbar != null) {
//					appbar.getBackground().setColorFilter(GlobalOptions.isDark?GlobalOptions.NEGATIVE:null);
//				}
//				if(GlobalOptions.isDark){
//					popupContentView.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
//					pottombar.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
//					popIvBack.setImageResource(R.drawable.abc_ic_ab_white_material);
//					((ImageView)pottombar.findViewById(R.id.popIvSettings)).setColorFilter(GlobalOptions.NEGATIVE);
//				} else /*if(popIvBack.getTag()!=null)*/{ //???
//					popupContentView.getBackground().setColorFilter(null);
//					pottombar.getBackground().setColorFilter(null);
//					popIvBack.setImageResource(R.drawable.abc_ic_ab_back_material_simple_compat);
//					((ImageView)pottombar.findViewById(R.id.popIvSettings)).setColorFilter(null);
//				}
//				if(indicator !=null) {
//					entryTitle.setTextColor(GlobalOptions.isDark?a.AppBlack:Color.GRAY);
//					indicator.setTextColor(GlobalOptions.isDark?a.AppBlack:0xff2b43c1);
//				}
//				MainColorStamp = a.MainAppBackground;
//				int filteredColor = GlobalOptions.isDark ? ColorUtils.blendARGB(a.MainPageBackground, Color.BLACK, a.ColorMultiplier_Web) : GlobalPageBackground;
//				weblistHandler.dictView.setBackgroundColor(filteredColor);
//			}
//			if (dictPicker.pinned()) {
//				dictPicker.refresh();
//			}
//		}
	}
	
	@Override
	protected void onShow() {
		super.onShow();
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
	}
	
	@SuppressLint("ResourceType")
	@Override
	// click
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.cover: {
			} break;
		}
	}
}

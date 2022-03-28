package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.core.graphics.ColorUtils;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.ActivityMainBinding;
import com.knziha.plod.widgets.ViewUtils;

public class SearchbarTools extends PlainAppPanel implements View.OnTouchListener, View.OnFocusChangeListener {
	protected PDICMainActivity a;
	protected ViewGroup rootView;
	
	public SearchbarTools(PDICMainActivity a, EditText etSearch, ViewGroup rv) {
		super(a, true);
		this.bottomPadding = 0;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.bAutoRefresh = true;
		this.etSearch=etSearch;
		this.rootView=rv;
		etSearch.setOnTouchListener(this);
		etSearch.setOnClickListener(this);
		etSearch.setOnFocusChangeListener(this);
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(PDICMainActivity) context;
			mBackgroundColor = 0;
		}
		if (settingsLayout==null) {
			ActivityMainBinding uiData = a.UIData;
			ViewUtils.setOnClickListenersOneDepth(uiData.etSearchBar, this, 999, 0, null);
			settingsLayout = uiData.etSearchBar;
			
			if(true) {
				((ViewGroup)uiData.etBack.getParent()).setBackgroundColor(a.MainAppBackground);
				int fc = ColorUtils.blendARGB(Color.WHITE,a.MainBackground, 0.45f) & 0xf0ffffff;
				uiData.etSearchBar.getChildAt(0).setBackgroundColor(fc);
				uiData.etSearchBar.getChildAt(2).setBackgroundColor(fc);
				LayerDrawable ld = (LayerDrawable)uiData.showSearchHistoryDropdown.getBackground();
				PorterDuffColorFilter cf = new PorterDuffColorFilter(a.MainAppBackground, PorterDuff.Mode.SRC_IN);
				for (int i = 0; i < ld.getNumberOfLayers()-1; i++) {
					ld.getDrawable(i).setColorFilter(cf);
				}
			}
		}
	}
	
	@Override
	public void refresh() {
		if (settingsLayout!=null) {
			ViewGroup.LayoutParams lp = settingsLayout.getLayoutParams();
			if(lp instanceof ViewGroup.MarginLayoutParams) {
				((ViewGroup.MarginLayoutParams) lp).topMargin = a.UIData.toolbar.getHeight();
			}
			
		}
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void onClick(View v) {
		CMN.Log("onclick::", v);
		if (v==etSearch) {
			CMN.Log("click!!!"+etSearch.getScrollX());
			if(etSearch.getScrollX()==etTouchScrollStart) {
				if(!isVisible())
					toggle(rootView!=null?rootView:PDICMainAppOptions.getEnableSuperImmersiveScrollMode()?a.UIData.webcoord:a.root, null, -1);
			}
		}
		else switch (v.getId()) {
			case R.id.show_search_history_dropdown_bg:
			case R.id.etBack:
				dismiss();
			break;
			case R.id.etClear:
				etSearch.setText("");
				a.imm.showSoftInput(etSearch, 0);
			break;
			case R.id.etPaste:{
				ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
				if(cm!=null){
					ClipData pclip = cm.getPrimaryClip();
					ClipData.Item firstItem = pclip.getItemAt(0);
					CharSequence content = firstItem.getText();
					etSearch.setText(content);
					etSearch.setSelection(content.length());
				}
			} break;
			case R.id.etCopy:{
				ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
				if(cm!=null){
					cm.setPrimaryClip(ClipData.newPlainText(null, etSearch.getText()));
				}
			} break;
		}
//			if (v.getId() != R.drawable.ic_menu_24dp) {
//				a.mInterceptorListenerHandled = true;
//			}
	}
	
	@Override
	protected void onDismiss() {
		CMN.Log("onDismiss::");
		super.onDismiss();
		a.imm.hideSoftInputFromWindow(a.UIData.etSearch.getWindowToken(),0);
	}
	
	int etTouchScrollStart;
	long checkNxtFocus = 0;
	EditText etSearch;
//	public void setEt(EditText etSearch, ViewGroup rv) {
//		this.etSearch=etSearch;
//		this.rootView=rv;
//		etSearch.setOnTouchListener(this);
//		etSearch.setOnClickListener(this);
//	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			etTouchScrollStart = etSearch.getScrollX();
			checkNxtFocus = event.getEventTime();
			CMN.Log("touch!!!"+etTouchScrollStart);
		}
		return false;
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		CMN.Log("onFocusChange::", hasFocus, a.systemIntialized);
//				ViewUtils.findInMenu(AllMenusStamp, R.id.toolbar_action2)
//						.setIcon(hasFocus?R.drawable.ic_search_24k:R.drawable.ic_back_material);
		if(hasFocus) {
			if(checkNxtFocus!=0) {
				if(SystemClock.uptimeMillis()-checkNxtFocus<250) {
					if(!isVisible())
						toggle(rootView!=null?rootView:PDICMainAppOptions.getEnableSuperImmersiveScrollMode()?a.UIData.webcoord:a.root, null, -1);
				}
				checkNxtFocus=0;
			}
		} else {
			dismiss();
		}
	}
}

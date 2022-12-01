package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.QuCiQiBinding;
import com.knziha.plod.tesseraction.Manager;
import com.knziha.plod.widgets.TextMenuView;
import com.knziha.plod.widgets.ViewUtils;

import org.apache.lucene.analysis.util.CharacterUtils;

/** 相机取词 */
public class WordCamera extends PlainAppPanel implements Manager.OnSetViewRect {
	
	private final SearchbarTools schTools;
	QuCiQiBinding UIData;
	public final Manager mManager;
	public boolean paused;
	
	public WordCamera(MainActivityUIBase a, SearchbarTools searchbarTools) {
		super(a, false);
		this.a = a;
		bAnimate=false;
		bAutoRefresh=false;
		showType=2;
		bottomPadding=0;
		resizeDlg = true;
		mManager = new Manager(opt);
		wordPopup = a.wordPopup;
		this.schTools = searchbarTools;
		//wordPopup = new WordPopup(a);
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
		if (UIData == null && a!=null) {
			UIData = QuCiQiBinding.inflate(LayoutInflater.from(context), a.root, false);
			settingsLayout = (ViewGroup) UIData.getRoot();
			
			int type = 1;//mManager.opt.getLaunchCameraType();
			mManager.init(a, null, UIData);
			mManager.setRequestCode(10, 10);
			mManager.setWordCamera(this);
			wordPopup.forcePin(UIData.root);
			
			
			if (PDICMainAppOptions.wordCameraRealtime()) {
				mManager.toggleRealTime();
				setViewChecked(UIData.realtime, true);
			}
			if (PDICMainAppOptions.wordCameraAutoSch()) {
				mManager.toggleAutoSch();
				setViewChecked(UIData.autoSch, true);
			}
			
			mManager.tryOpenCamera(0, realtime(), a, mManager.permissionCode);
			
			//mManager.showMainMenu(a, 10);
			
//			try {
//				mManager.dMan.getTess();
//			} catch (Exception e) {
//				CMN.debug(e);
//			}
			
			updateOrientation();
			
		}
	}
	
	private void setViewChecked(TextView v, boolean checked) {
		if(checked ^ v.isActivated()) {
			LayerDrawable ld;
			v.setActivated(checked);
			if (checked) {
				ld = (LayerDrawable) v.getTag();
				if (ld == null) {
					Drawable d = a.mResource.getDrawable(R.drawable.frame_checked);
					d.setAlpha(127);
					v.setTag(ld = new LayerDrawable(new Drawable[]{d, v.getBackground()}));
				}
				v.setBackground(ld);
			} else {
				ld = (LayerDrawable) v.getTag();
				v.setBackground(ld.getDrawable(1));
			}
			v.setTextColor(checked?Color.WHITE:0xff3B7DB1);
		}
	}
	
	private void updateOrientation() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		if (a != null) {
			a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
	
	@Override
	protected void onShow() {
		if (wordPopup == a.wordPopup) {
			wordPopup.wordCamera = this;
			wordPopup.forcePin(UIData.root);
		}
		a.wordCamera = this;
		UIData.frameView.resume(); // todo
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		mManager.pauseCamera();
		if (wordPopup == a.wordPopup) {
			wordPopup.stopTask();
			wordPopup.wordCamera = null;
			wordPopup.forcePin(null);
		}
		a.wordCamera = null;
	}
	
	private void dispose() {
		if (schTools != null) {
			schTools.wordCamera = null;
		}
		mManager.dispose();
	}
	
	public void onResume() {
		if(isVisible()) {
			if (!(UIData!=null && wordPopup.isMaximized()
					&& ViewUtils.getNthParentNullable(wordPopup.popupContentView, 1)==UIData.root)) {
				mManager.resumeCamera();
			}
		}
		paused = false;
	}
	
	public void onPause() {
		paused = true;
		mManager.pauseCamera();
	}
	
	public PopupMenuHelper popupMenu;
	int[] mainMenus = new int[]{
			R.string.ocr
			, R.string.qr
	};
	
	@SuppressLint("ResourceType")
	@Override
	// click
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
//			case R.string.qr: {
//			} break;
			case R.id.realtime: {
				boolean val = mManager.toggleRealTime();
				PDICMainAppOptions.wordCameraRealtime(val);
				setViewChecked((TextView) v, val);
			} break;
			case R.id.autoSch: {
				boolean val = mManager.toggleAutoSch();
				PDICMainAppOptions.wordCameraAutoSch(val);
				setViewChecked((TextView) v, val);
			} break;
			case R.string.qr:
			case R.string.ocr:
			{
				if (id == R.string.ocr) {
					mManager.tryOpenCamera(0, realtime(), a, mManager.permissionCode);
				}
				if (id == R.string.qr) {
					mManager.tryOpenCamera(1, realtime(), a, mManager.permissionCode);
				}
				//tryOpenCamera(1, activity, requestCode);
				if (popupMenu != null) {
					popupMenu.dismiss();
				}
			} break;
			case R.id.title: {
				boolean init = popupMenu==null;
				if(init) {
					popupMenu = new PopupMenuHelper(a, null, null);
					ListView lv = new AlertController.RecycleListView(a);
					popupMenu.lv.addView(lv);
					View.AccessibilityDelegate acess = new View.AccessibilityDelegate() {
						@Override
						public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
							super.onPopulateAccessibilityEvent(host, event);
						}
					};
					lv.setAdapter(new BaseAdapter() {
						@Override
						public int getCount() {
							return 2;
						}
						@Override
						public Object getItem(int position) {
							return null;
						}
						@Override
						public long getItemId(int position) {
							return 0;
						}
						@Override
						public View getView(int position, View convertView, ViewGroup parent) {
							TextMenuView tv;
							if (convertView == null) {
								int padding = (int) (11 * GlobalOptions.density);
								int padding1 = (int) (32.8 * GlobalOptions.density);
								Context context = parent.getContext();
								tv = new TextMenuView(context);
								tv.setPadding(padding1, padding, padding1, padding);
								tv.setGravity(Gravity.CENTER_VERTICAL);
								tv.setSingleLine(true);
								tv.setOnClickListener(WordCamera.this);
								//tv.setOnLongClickListener(this);
								tv.setBackground(ViewUtils.getThemeDrawable(context, R.attr.listChoiceBackgroundIndicator));
								convertView = tv;
								convertView.setAccessibilityDelegate(acess);
							} else {
								tv = (TextMenuView) convertView;
							}
							tv.setTextColor(GlobalOptions.isDark? Color.WHITE: Color.BLACK);
							tv.setId(mainMenus[position]);
							tv.setText(mainMenus[position]);
							return convertView;
						}
					});
					popupMenu.tag1 = lv;
				}
				mManager.pauseCamera();
				popupMenu.dismiss();
				popupMenu.showAt(UIData.title, 0, 0, Gravity.TOP);
				popupMenu.sv.getBackground().setColorFilter(GlobalOptions.isDark?GlobalOptions.NEGATIVE_1:null);
				AlertController.RecycleListView lv = (AlertController.RecycleListView) popupMenu.tag1;
				lv.getLayoutParams().width = opt.dm.widthPixels/2;
				lv.getLayoutParams().height = opt.dm.heightPixels/2;
				if(init) popupMenu.mPopupWindow.setOnDismissListener(() -> {
					mManager.tada(UIData.title);
					mManager.resumeCamera();
				});
			} break;
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
	public void resize() {
		super.resize();
		mManager.readScreenOrientation(a, true);
	}
	
	@Override
	public boolean onSetViewRect(RectF rect) {
		//rect.set(0,0,100,100);
		DisplayMetrics dm = opt.dm;
		if (mManager.isPortrait) {
			int width = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.92);
			int height = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.45);
			int leftOffset = (dm.widthPixels - width) / 2;
			int topOffset = (int) (dm.density * 90);
			rect.set(leftOffset, topOffset, leftOffset + width, topOffset + height);
		} else {
			int width = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.7);
			int height = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.7);
			int leftOffset = (dm.widthPixels - width) / 2;
			int topOffset = (dm.heightPixels - height) / 2;
			rect.set(leftOffset, topOffset, leftOffset + width, topOffset + height);
		}
		CMN.debug("Calculated framing rect: " + rect);
		return true;
	}
	
	@Override
	public boolean onBackPressed() {
		weblistHandler = wordPopup.isVisible()?wordPopup.weblistHandler:null;
		if (mManager.onBack()) {
			return true;
		}
		return super.onBackPressed();
	}
	
	private boolean realtime() {
		return mManager.isRealTime();
	}
	
	WordPopup wordPopup;
	
	public boolean isRelevantWord(String text) {
		if (text.length() == 0) {
			return false;
		}
		if (text.length() == 1) {
			char c0 = text.charAt(0);
			int gc = Character.getType(c0);
			if (gc>=Character.NON_SPACING_MARK&&gc<=Character.COMBINING_SPACING_MARK) {
				return false;
			}
		}
		return true;
	}
	
	public void popupWord(@NonNull String centerWord) {
		centerWord = centerWord.trim();
		if (isRelevantWord(centerWord) && isVisible()) {
			if (wordPopup.loadManager == null || wordPopup.mWebView==null) {
				if (wordPopup.mWebView == null) {
					a.hdl.post(() -> {
						wordPopup.wordCamera = this;
						wordPopup.init();
						wordPopup.forcePin(UIData.root);
						wordPopup.refresh();
					});
				}
				if (wordPopup.loadManager == null) {
					wordPopup.loadManager = a.loadManager;
				}
			} else if (!wordPopup.isVisible() || !centerWord.equalsIgnoreCase(wordPopup.popupKey)) {
				wordPopup.popupWord(null, centerWord, null, 0);
			}
		}
	}
	
	public void openImage(Uri data) {
		mManager.openImage(data);
	}
}

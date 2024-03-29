package com.knziha.plod.PlainUI;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.widgets.ActivatableImageView;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;

public class ButtonUIProject extends ButtonUIData{
	public int type=-1;
	public final String key;
	public boolean bNeedCheckOrientation;
	public String currentValue;
	final ArrayList<ViewGroup> barStack = new ArrayList<>();
	final ArrayList<View[]> btnsStack = new ArrayList<>();
	public int version;
	final int[] icons;
	final String[] titles;
	public ViewGroup.LayoutParams btnLayout;
	public ColorFilter mColorFilter;
	
	ArrayList<AppIconData> iconData;
	public View.OnClickListener onClickListener;
	public View.OnLongClickListener onLongClickListener;
	public View.OnTouchListener onTouchListener;
	MainActivityUIBase a;
	private boolean smallIcon;
	
	public ButtonUIProject(MainActivityUIBase a, String _key, int[] _icons, int titlesRes, String customize_str, ViewGroup bar, View[] _btns) {
		key = _key;
		icons = _icons;
		currentValue = customize_str;
		if (_btns==null) {
			_btns = new View[_icons.length];
			for (int i = 0; i < _btns.length; i++) {
				_btns[i] = bar.getChildAt(i);
			}
		}
		addBar(bar, _btns);
		titles = a.mResource.getStringArray(titlesRes);
		this.a = a;
	}
	
	public ButtonUIProject(MainActivityUIBase a, int idx, PDICMainAppOptions opt, int[] _icons, int titlesRes, ViewGroup bar, View[] _btns) {
		key = "ctnp#"+idx;
		type = idx;
		icons = _icons;
		currentValue = opt.getAppContentBarProject(key);
		//CMN.Log("重新读取", key);
		if (_btns==null && bar!=null) {
			_btns = new View[_icons.length];
			for (int i = 0; i < _btns.length; i++) {
				_btns[i] = bar.getChildAt(i);
			}
		}
		addBar(bar, _btns);
		titles = a.mResource.getStringArray(titlesRes);
		this.a = a;
	}
	
	public void addBar(ViewGroup bar, View[] btns) {
		int idx = barStack.indexOf(bar);
		if (idx != -1) {
			barStack.remove(idx);
			btnsStack.remove(idx);
		}
		barStack.add(bar);
		btnsStack.add(btns);
	}
	
	public void instantiate() {
		int projectSize = icons.length;
		ArrayList<AppIconData> _iconData = new ArrayList<>(projectSize);
		if(currentValue!=null) {
			String[] arr = currentValue.split("\\|");
			for (int i = 0; i < arr.length; i++) {
				String val = arr[i];
				int start = 0;
				int end = val.length();
				if (end > 0) {
					while (start < end && val.charAt(start) == '\\') ++start;
					if (start > 0) val = val.substring(start, end);
					int id = IU.parsint(val, -1);
					_iconData.add(new AppIconData(id, start==0?1:start==1?0:start));
				}
			}
		}
		if(_iconData.size()<projectSize){// 查漏补缺
			ArrayList<Integer> iconDataExtra = new ArrayList<>();
			for (int i = 0; i < projectSize; i++) {
				iconDataExtra.add(i);
			}
			for (int i = 0; i < _iconData.size(); i++) {
				iconDataExtra.remove((Integer) _iconData.get(i).number);
			}
			boolean initialise = _iconData.size()==0;
			for (int i = 0; i < iconDataExtra.size(); i++) {
				int number = iconDataExtra.get(i);
				int start = initialise&&number<6?0:1;
				_iconData.add(new AppIconData(number, start==0?1:0));
			}
		}
		iconData = _iconData;
	}

	public boolean getTint() {
		return false;
	}

	public void clear(MainActivityUIBase a) {
		if(iconData!=null){
			iconData=null;
			if(a!=null){
				currentValue=null;
				RebuildBottombarIcons();
			}
		}
	}
	
	/** Rebuild Bottom Icons<br/>
	 * 定制底栏：一  见 {@link #BottombarBtnIcons}<br/>
	 * 定制底栏：二 见 {@link #ContentbarBtnIcons}<br/>
	 * 定制底栏：三 见 {@link WordPopup}<br/>
	 */
	public void RebuildBottombarIcons() {
		MainActivityUIBase this_ = a;
		Configuration config = this_.mConfiguration;
		View.OnClickListener onClickListener = this.onClickListener;
		View.OnLongClickListener onLongClickListener = this.onLongClickListener;
		View.OnTouchListener onTouchListener = this.onTouchListener;
		if (onClickListener == null) {
			onClickListener = a;
			onLongClickListener = a;
		}
		ArrayList<ViewGroup> bars = barStack;
		if(bars.size()==0) {
			return;
		}
		String appproject = this.currentValue;
		boolean tint = this.getTint();
		if(appproject==null) appproject="0|1|2|3|4|5|6";
		//appproject="0|1|2|3|4|5|6|7|8|9|10|11|13|14|\\\\15";
		//appproject="0|1|2|3|4|5|6";
		//appproject="9|10|11|13|14|15";
		final boolean smallIcon = PDICMainAppOptions.shrinkIcons();
		final boolean smallIconSet = true; // smallIcon != this.smallIcon;
		final int smallIconPad = smallIcon? (int) (5 * GlobalOptions.density) :0;
		this.smallIcon = smallIcon;
		for (int j = 0; j < bars.size(); j++)
		{
			ViewGroup bottombar = bars.get(j);
			if (bottombar == null) {
				continue;
			}
			bottombar.removeAllViews();
			boolean isHorizontal = config.orientation==Configuration.ORIENTATION_LANDSCAPE;
			String[] arr = appproject.split("\\|");
			View[] presetBtns = this.btnsStack.get(j);
			int[] btnIcons = this.icons;
			CMN.rt();
	//		((RippleDrawable)a.getDrawable(rippleBG)).setColor(ColorStateList.valueOf(Color.WHITE));
			int rippleBG = R.drawable.abc_action_bar_item_background_material;
			boolean modRipple = PDICMainAppOptions.modRipple();
			final ViewGroup.LayoutParams lp = this.btnLayout!=null?this.btnLayout:this_.contentUIData.browserWidget10.getLayoutParams();
			for (int i = 0; i < arr.length; i++) {
				String val = arr[i];
				int st = 0;
				int ed = val.length();
				if(ed>0) {
					while (st<ed && val.charAt(st)=='\\') ++st;
					if(st>0){
						val = val.substring(st, ed);
						if(st==2) this.bNeedCheckOrientation=true;
					}
					if(st==0||st==2&&isHorizontal){
						int id = IU.parsint(val, -1);
						if (id >= 0 && id < presetBtns.length) {
							View btn = presetBtns[id];
							if (btn == null) {
								ImageView iv;
								int bid = btnIcons[id];
								if (bid==R.drawable.fuzzy_search || bid==R.drawable.full_search || bid==R.drawable.star_ic_grey) {
									ActivatableImageView avt = new ActivatableImageView(this_);
									avt.setImageResource(bid);
									if (bid==R.drawable.fuzzy_search) {
										avt.setActiveDrawable(a.mResource.getDrawable(R.drawable.fuzzy_search_pressed), false);
									}
									else if(bid==R.drawable.full_search)
									{
										avt.setActiveDrawable(a.mResource.getDrawable(R.drawable.full_search_pressed), false);
									}
									else
									{
										avt.setActiveDrawable(a.mResource.getDrawable(R.drawable.star_ic_solid_framed), false);
									}
									iv = avt;
								} else {
									iv = new ImageView(this_);
									iv.setImageResource(bid);
								}
								iv.setContentDescription(this.titles[i]);
								//iv.setBackgroundResource(R.drawable.surrtrip1);
								iv.setBackgroundResource(rippleBG);
								iv.setLayoutParams(lp);
								iv.setId(btnIcons[id]);
								iv.setOnClickListener(onClickListener);
								if(tint) iv.setColorFilter(a.tintListFilter.sForegroundFilter);
								if (LongclickableMap.contains(btnIcons[id])) {
									iv.setOnLongClickListener(onLongClickListener);
								} else {
									iv.setLongClickable(false);
								}
								if (onTouchListener != null) {
									iv.setOnTouchListener(onTouchListener);
								}
								presetBtns[id] = iv;
								if (modRipple) {
									a.tintListFilter.ModRippleColor(iv.getBackground(), a.tintListFilter.sRippleState);
								}
								if (this.mColorFilter!=null) {
									iv.setColorFilter(this.mColorFilter);
								}
								bottombar.addView(btn=iv);
							}
							else {
								ViewGroup svp = (ViewGroup) btn.getParent();
								btn.setBackgroundResource(rippleBG);
								if (LongclickableMap.contains(btn.getId())) {
									btn.setOnLongClickListener(onLongClickListener);
								}
								if (svp != null) svp.removeView(btn);
								btn.setContentDescription(this.titles[i]);
								if (modRipple) {
									a.tintListFilter.ModRippleColor(btn.getBackground(), a.tintListFilter.sRippleState);
								}
								btn.setOnClickListener(onClickListener);
								if (onTouchListener != null) {
									btn.setOnTouchListener(onTouchListener);
								}
								bottombar.addView(btn);
							}
							if(smallIconSet) {
								ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) btn.getLayoutParams();
								layoutParams.bottomMargin = smallIconPad;
								layoutParams.topMargin =    smallIconPad;
								if(bottombar!=a.wordPopup.pottombar && bottombar!=a.wordPopup.toolbar) {
									layoutParams.leftMargin =   smallIconPad;
									layoutParams.rightMargin =  smallIconPad;
								}
							}
						}
					}
				}
			}
			ViewUtils.setForegroundColor(bottombar, a.tintListFilter);
		}
		//CMN.pt("重排耗时", bottombar_project);
	}
	
	@Override
	public String toString() {
		return "AppUIProject{" +
				"key='" + key + '\'' +
				", bottombar=" + barStack +
				'}';
	}
}
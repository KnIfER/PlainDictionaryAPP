package com.knziha.plod.PlainUI;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.widgets.ActivatableImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class AppUIProject {
	public static HashSet<Integer> LongclickableMap = new HashSet<>(); // todo optimise
	static{
		LongclickableMap.addAll(Arrays.asList(
				R.drawable.star_ic
				, R.drawable.ic_prv_dict_chevron
				, R.drawable.ic_nxt_dict_chevron
				, R.drawable.ic_fulltext_reader
				, R.drawable.book_bundle
				, R.drawable.favoriteg
				, 4
				, 6
				, 9
				, 10
				, 16
				, 17, 106, 107, 108, 109, 110, 111, 112, 114, 118, 119
		));
	}
	/**  定制底栏一：<br/>
	 * 返回列表7 收藏词条8 跳转词典9 上一词条10 下一词条11 发音按钮12 <br/>
	 * 退离程序13 打开侧栏14 随机词条15 上一词典16 下一词典17  自动浏览18 全文朗读19 进入收藏20 进入历史21 调整亮度22 夜间模式23 切换横屏24 定制颜色25 定制底栏26 切换沉浸 切换全屏 多维分享 空格键 方向键⬅ 方向键➡ 方向键⬆ 方向键⬇ W键 A键 S键 D键 C键 Z键 CTRL键 SHIFT键 鼠标左击 鼠标右击 <br/>*/
	public final static int[] ContentbarBtnIcons = new int[]{  // todo use drawable id as view id
			R.drawable.back_ic,
			R.drawable.star_ic,
			R.drawable.list_ic,
			R.drawable.chevron_left,
			R.drawable.chevron_right,
			R.drawable.voice_ic,
			R.drawable.ic_menu_24dp,
			R.drawable.ic_exit_app,//13
			R.drawable.ic_menu_drawer_24dp,//14
			R.drawable.ic_random_shuffle,//15
			R.drawable.ic_prv_dict_chevron,//16
			R.drawable.ic_nxt_dict_chevron,//17
			R.drawable.ic_autoplay,//18
			R.drawable.ic_fulltext_reader,//19
			R.drawable.favoriteg,//20
			R.drawable.historyg,//21
			R.drawable.ic_brightness_low_black_bk,//22
			R.drawable.ic_darkmode_small,//23
			R.drawable.ic_swich_landscape_orientation,//24
			R.drawable.ic_options_toolbox_small,//25
			R.drawable.customize_bars,//26
			R.drawable.ic_keyboard_show_24,
			R.drawable.ic_edit_booknotes_btn,
	};
	
	public int type=-1;
	public final String key;
	public boolean bNeedCheckOrientation;
	public String currentValue;
	public ViewGroup bottombar;
	public ImageView[] btns;
	public int version;
	final int[] icons;
	final String[] titles;

	ArrayList<AppIconData> iconData;

	public AppUIProject(Context context, String _key, int[] _icons, int titlesRes, String customize_str, ViewGroup _bottombar, ImageView[] _btns) {
		key = _key;
		icons = _icons;
		currentValue = customize_str;
		bottombar = _bottombar;
		btns = _btns;
		titles = context.getResources().getStringArray(titlesRes);
	}
	
	public AppUIProject(Context context, int idx, PDICMainAppOptions opt, int[] _icons, int titlesRes, ViewGroup _bottombar, ImageView[] _btns) {
		key = "ctnp#"+idx;
		type = idx;
		icons = _icons;
		currentValue = opt.getAppContentBarProject(key);
		//CMN.Log("重新读取", key);
		bottombar = _bottombar;
		btns = _btns;
		titles = context.getResources().getStringArray(titlesRes);
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
				RebuildBottombarIcons(a, this, a.mConfiguration);
			}
		}
	}
	
	/** Rebuild Bottom Icons<br/>
	 * 定制底栏：一  见 {@link PDICMainActivity#BottombarBtnIcons}<br/>
	 * 定制底栏：二 见 {@link #ContentbarBtnIcons}<br/>
	 */
	public static void RebuildBottombarIcons(MainActivityUIBase a, AppUIProject bottombar_project, Configuration config) {
		MainActivityUIBase this_ = a;
		ViewGroup bottombar;
		if(bottombar_project==null || (bottombar = bottombar_project.bottombar)==null) {
			return;
		}
		String appproject = bottombar_project.currentValue;
		boolean tint = bottombar_project.getTint();
		if(tint&&this_.ForegroundFilter==null)
			this_.ForegroundFilter = new PorterDuffColorFilter(this_.ForegroundTint, PorterDuff.Mode.SRC_IN);
		if(appproject==null) appproject="0|1|2|3|4|5";
		//appproject="0|1|2|3|4|5|6|7|8|9|10|11|13|14|\\\\15";
		//appproject="0|1|2|3|4|5|6";
		//appproject="9|10|11|13|14|15";
		int idStart=0;
		bottombar.removeAllViews();
		if(bottombar.getId()== R.id.bottombar2)
			idStart=107;
		boolean isHorizontal = config.orientation==Configuration.ORIENTATION_LANDSCAPE;
		String[] arr = appproject.split("\\|");
		ImageView[] BottombarBtns = bottombar_project.btns;
		int[] btnIcons = bottombar_project.icons;
		CMN.rt();
		for (int i = 0; i < arr.length; i++) {
			String val = arr[i];
			int start = 0;
			int end = val.length();
			if(end>0) {
				while (start<end && val.charAt(start)=='\\') {
					++start;
				}
				if(start>0){
					val = val.substring(start, end);
					if(start==2) bottombar_project.bNeedCheckOrientation=true;
				}
				if(start==0||start==2&&isHorizontal){
					int id = IU.parsint(val, -1);
					if (id >= 0 && id < BottombarBtns.length) {
						ImageView iv = BottombarBtns[id];
						if (iv == null) {
							int bid = btnIcons[id];
							if (bid==R.drawable.fuzzy_search || bid==R.drawable.full_search) {
								ActivatableImageView avt = new ActivatableImageView(this_);
								avt.setImageResource(bid);
								if (bid==R.drawable.fuzzy_search) {
									avt.setActiveDrawable(a.mResource.getDrawable(R.drawable.fuzzy_search_pressed), false);
								}
								else
								{
									avt.setActiveDrawable(a.mResource.getDrawable(R.drawable.full_search_pressed), false);
								}
								iv = avt;
							} else {
								iv = new ImageView(this_);
								iv.setImageResource(bid);
							}
							iv.setContentDescription(bottombar_project.titles[i]);
							//iv.setBackgroundResource(R.drawable.surrtrip1);
							iv.setBackgroundResource(R.drawable.abc_action_bar_item_background_material);
							iv.setLayoutParams(this_.contentUIData.browserWidget10.getLayoutParams());
							iv.setId(btnIcons[id]);
							iv.setOnClickListener(this_);
							if(tint) iv.setColorFilter(this_.ForegroundFilter);
							if (LongclickableMap.contains(btnIcons[id])){
								iv.setOnLongClickListener(this_);
							} else {
								iv.setLongClickable(false);
							}
							BottombarBtns[id] = iv;
						}
						else {
							ViewGroup svp = (ViewGroup) iv.getParent();
							iv.setBackgroundResource(R.drawable.abc_action_bar_item_background_material);
							if (svp != null) svp.removeView(iv);
							iv.setContentDescription(bottombar_project.titles[i]);
						}
						bottombar.addView(iv);
					}
				}
			}
		}
		//CMN.pt("重排耗时", bottombar_project);
	}
	
	@Override
	public String toString() {
		return "AppUIProject{" +
				"key='" + key + '\'' +
				", bottombar=" + bottombar +
				'}';
	}
}
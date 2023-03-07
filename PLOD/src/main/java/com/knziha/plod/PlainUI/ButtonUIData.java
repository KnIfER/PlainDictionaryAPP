package com.knziha.plod.PlainUI;

import com.knziha.plod.plaindict.R;

import java.util.Arrays;
import java.util.HashSet;

public class ButtonUIData {
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
				, 17, 106, 107, 108, 109, 110, 111, 112, 114, 118
				, 119
				, R.drawable.ic_baseline_find_in_page_24
		));
	}
	
	
	int btns_desc_app_bottombar = R.array.customize_btm;
	/** 定制底栏一：<br/>
	 * 1 选择分组2 词条搜索3 全文搜索4 进入收藏5 进入历史6 <br/>
	 * 退离程序7 打开侧栏8 随机词条9 上一词典10 下一词典11 调整亮度12 定制底栏13 定制颜色14 管理词典15 进入设置16<br/>*/
	public final static int[] BottombarBtnIcons = new int[]{
			R.drawable.book_list,
			R.drawable.book_bundle,
			R.drawable.fuzzy_search,
			R.drawable.full_search,
			R.drawable.favoriteg,
			R.drawable.customize_bars,
			R.drawable.ic_menu_24dp,
			R.drawable.historyg,
			R.drawable.ic_exit_app,
			R.drawable.ic_menu_drawer_24dp,
			R.drawable.ic_shuffle_black_24dp,
			R.drawable.ic_prv_dict_chevron,
			R.drawable.ic_nxt_dict_chevron,
			R.drawable.ic_brightness_low_black_24dp,
			R.drawable.ic_swich_landscape_orientation,
			R.drawable.ic_options_toolbox_small,
			R.drawable.book_bundle2,
			R.drawable.ic_settings_black_24dp,
			R.drawable.ic_keyboard_show_24,
			R.drawable.ic_edit_booknotes_btn,
			R.drawable.ic_baseline_mindmap,
	};
	
	int btns_desc_contentbar = R.array.customize_ctn;
	/**  定制底栏二：<br/>
	 * 返回列表7 收藏词条8 跳转词典9 上一词条10 下一词条11 发音按钮12 <br/>
	 * 退离程序13 打开侧栏14 随机词条15 上一词典16 下一词典17  自动浏览18 全文朗读19 进入收藏20 进入历史21 调整亮度22 夜间模式23 切换横屏24 定制颜色25 定制底栏26 切换沉浸 切换全屏 多维分享 空格键 方向键⬅ 方向键➡ 方向键⬆ 方向键⬇ W键 A键 S键 D键 C键 Z键 CTRL键 SHIFT键 鼠标左击 鼠标右击 <br/>*/
	public final static int[] ContentbarBtnIcons = new int[]{
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
			R.drawable.ic_baseline_mindmap,
			R.drawable.ic_baseline_find_in_page_24,
	};
	
	
	public final static int wp_toolbar = R.array.customize_popup_ctn;
	public final static int[] PopupToolbarIcons = new int[]{
			R.drawable.back_ic                        // 0
			, R.drawable.text_underline               // 1
			, R.drawable.text_underline               // 2
			, R.drawable.star_ic_grey                 // 3
			, R.drawable.ic_peruse_pan_svg            // 4
			, R.drawable.ic_g_translate_black_24dp    // 5
			, R.drawable.voice_ic                     // 6
			, R.drawable.ic_fullscreen_black_24dp     // 7
			, R.drawable.chevron_top22                // 8
			, R.drawable.chevron_bottom22             // 9
			, 0                                       // 10
			, R.drawable.ic_btn_multimode             // 11
			, R.drawable.chevron_bottom2              // 12
			, R.drawable.chevron_top2                 // 13
			, R.drawable.ic_outline_settings_24     // 14
			, R.drawable.chevron_recess                       // 15
			, R.drawable.chevron_forward                      // 16
			, 0                                       // 17
			, R.drawable.customize_bars               // 18
	};
	
	public final static int wp_bottombar = R.array.customize_popup_bottom;
	public final static int[] PopupBottombarIcons = new int[]{
			R.drawable.clippin                         //0
			, R.drawable.text_underline                //1
			, R.drawable.text_underline                //2
			, R.drawable.ic_btn_multimode              //3
			, R.drawable.chevron_bottom2               //4
			, R.drawable.chevron_top2                  //5
			, R.drawable.drawer_menu_icon_setting      //6
			, R.drawable.chevron_recess                        //7
			, R.drawable.chevron_forward                       //8
			, 0                                        //9
			, R.drawable.back_ic                       //10
			, R.drawable.star_ic_grey                  //11
			, R.drawable.ic_peruse_pan_svg             //12
			, R.drawable.ic_g_translate_black_24dp     //13
			, R.drawable.voice_ic                      //14
			, R.drawable.ic_fullscreen_black_24dp      //15
			, R.drawable.chevron_top22                 //16
			, R.drawable.chevron_bottom22              //17
			, 0                                        //18
			, R.drawable.customize_bars                //19
	};
	
	
	
}
package com.knziha.plod.slideshow;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.knziha.plod.plaindict.OptionProcessor;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

import java.util.ArrayList;
import java.util.Arrays;

import static com.knziha.plod.plaindict.MainActivityUIBase.init_clickspan_with_bits_at;

public class PhotoPagerAjuster extends PagerAdapter {
	final OptionProcessor opr;
	final PDICMainAppOptions opt;
	final ViewPager viewPager;
	View[] items = new View[3];
	SeekBar[] seekbars_scale;
	SeekBar[] seekbars_hue;
	ArrayList<SeekBar> seekbars_hue_rgb;
	static ColorMatrix colorMatrix;
	ColorMatrix colorMatrixTmp;
	static int PBC_tvScroll;
	static int PBC_tab;
	
	DialogInterface.OnDismissListener OnDismissListener = new DialogInterface.OnDismissListener(){
		@Override
		public void onDismiss(DialogInterface dialog) {
			PBC_tab = viewPager.getCurrentItem();
			if(items[0]!=null){
				PBC_tvScroll = items[0].getScrollY();
			}
		}
	};
	
	public PhotoPagerAjuster(OptionProcessor opr, PDICMainAppOptions opt, ViewPager viewPager){
		this.opr = opr;
		this.viewPager = viewPager;
		if(colorMatrix==null){
			colorMatrix = new ColorMatrix();
		}
		this.opt = opt;
	}
	@Override
	public int getCount() {
		return items.length;
	}
	
	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view==object;
	}
	
	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		View item = items[position];
		if(item==null){
			Context context = (Context) opr;
			switch (position){
				/* 基础开关 */
				case 0:{
					String[] DictOpt = context.getResources().getStringArray(R.array.picture_spec);
					final String[] Coef = DictOpt[0].split("_");
					SpannableStringBuilder ssb = new SpannableStringBuilder();
					final ScrollView sv = new ScrollView(context);
					final TextView tv = new TextView(context);
					tv.setLayoutParams(new ScrollView.LayoutParams(-1, -2));
					tv.setTag(new Object[]{opt, opr});
					sv.addView(tv);
					ssb.append("\n");
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1,44,1,4, 0,true); //锁定X
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 2, Coef, 0, 1, 0x1,45,1,4, 12,true);//长按
					ssb.delete(ssb.length()-4,ssb.length());
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 3, Coef, 0, 1, 0x1,46,1,4, 1,true);//菜单按钮
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 4, Coef, 0, 1, 0x1,43,1,2, -1,true);//单击
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 5, Coef, 0, 1, 0x1,41,1,2, 2,true);//前后an
					
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 6, null, 0, 1, 0x1,-1,1,-1, 10,false);//保存
					ssb.delete(ssb.length()-4,ssb.length()); ssb.append(" ");
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 7, Coef, 0, 0, 0x1,42,1,2, 3,true);//保存an
					
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 8, null, 0, 1, 0x1,-1,1,-1, 11,false);//返回
					ssb.delete(ssb.length()-4,ssb.length()); ssb.append(" ");
					init_clickspan_with_bits_at(tv, ssb, DictOpt, 9, Coef, 0, 1, 0x1,47,1,4, 4,true);//返回an
					
					for (int i = 0; i < 8; i++) {
						ssb.append("\n");
					}
					
					opt.setAsLinkedTextView(tv, true);
					
					tv.setLinkTextColor(Color.WHITE);
					tv.setText(ssb, TextView.BufferType.SPANNABLE);
					if(false){
						tv.post(() -> sv.scrollTo(0, PBC_tvScroll));
					}
					item = sv;
				} break;
				/* 色彩偏向 */
				case 1:{
					View color_matrix_view = LayoutInflater.from(context).inflate(R.layout.color_matrix_scale, null);
					seekbars_scale = new SeekBar[]{
						color_matrix_view.findViewById(R.id.bar_R),
						color_matrix_view.findViewById(R.id.bar_G),
						color_matrix_view.findViewById(R.id.bar_B),
						color_matrix_view.findViewById(R.id.bar_A)
					};
					View colorView = color_matrix_view.findViewById(R.id.color_view);
					SeekBar.OnSeekBarChangeListener seeker = new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							colorView.setBackgroundColor(Color.argb( seekbars_scale[3].getProgress(),
									seekbars_scale[0].getProgress(),
									seekbars_scale[1].getProgress(),
									seekbars_scale[2].getProgress()));
							CallOnColorFilterChanged();
						}
						
						@Override public void onStartTrackingTouch(SeekBar seekBar) { }
						
						@Override public void onStopTrackingTouch(SeekBar seekBar) { }
					};
					for (int i = 0; i < 4; i++) {
						seekbars_scale[i].setOnSeekBarChangeListener(seeker);
					}
					item = color_matrix_view;
				} break;
				/* 色相调整 */
				case 2:{
					View color_matrix_view = LayoutInflater.from(context).inflate(R.layout.color_matrix_hue, null);
					seekbars_hue = new SeekBar[]{
						color_matrix_view.findViewById(R.id.bar_lum),
						color_matrix_view.findViewById(R.id.bar_sat),
						color_matrix_view.findViewById(R.id.bar_hue),
						color_matrix_view.findViewById(R.id.bar_hue_r),
						color_matrix_view.findViewById(R.id.bar_hue_g),
						color_matrix_view.findViewById(R.id.bar_hue_b),
					};
					seekbars_hue_rgb = new ArrayList<>(Arrays.asList(seekbars_hue[3], seekbars_hue[4], seekbars_hue[5]));
					SeekBar.OnSeekBarChangeListener seeker = new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							CallOnColorFilterChanged();
						}
						@Override public void onStartTrackingTouch(SeekBar seekBar) { }
						
						@Override public void onStopTrackingTouch(SeekBar seekBar) { }
					};
					for (int i = 0; i < 6; i++) {
						seekbars_hue[i].setOnSeekBarChangeListener(seeker);
						if(i>=3){
							seekbars_hue[i].setTag(i-3);
						}
					}
					item = color_matrix_view;
				} break;
			}
			items[position] = item;
		}
		else {
			ViewGroup vp = (ViewGroup) item.getParent();
			if(vp!=null){
				vp.removeView(item);
			}
		}
		container.addView(item);
		return item;
	}
	
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
	public interface OnColorFilterChangedListener{
		void OnColorFilterChanged(ColorMatrix colorMatrix);
	}
	OnColorFilterChangedListener mOnColorFilterChangedListener;
	
	public void setOnColorFilterChangedListener(OnColorFilterChangedListener mOnColorFilterChangedListener) {
		this.mOnColorFilterChangedListener = mOnColorFilterChangedListener;
	}
	
	boolean universal_hue_adj = false;
	
	private void CallOnColorFilterChanged() {
		/* https://www.jianshu.com/p/ebda27f061e3 */
		colorMatrix.reset();
		/* 色彩 */
		if(seekbars_scale !=null){
			colorMatrix.setScale(seekbars_scale[0].getProgress()/128.f,
					seekbars_scale[1].getProgress()/128.f,
					seekbars_scale[2].getProgress()/128.f,
					seekbars_scale[3].getProgress()/128.f
			);
		}
		/* 色相 */
		if(seekbars_hue !=null){
			if(colorMatrixTmp==null){
				colorMatrixTmp=new ColorMatrix();
			}
			/* Brightness */
			colorMatrixTmp.reset();
			float val = seekbars_hue[0].getProgress() / 128.f;
			colorMatrixTmp.setScale(val, val, val, 1);
			colorMatrix.postConcat(colorMatrixTmp);
			/* Saturation */
			colorMatrixTmp.setSaturation(seekbars_hue[1].getProgress() / 128.f);
			colorMatrix.postConcat(colorMatrixTmp);
			/* Hue */
			for (int i = 0; i < 3; i++) {
				SeekBar bar = seekbars_hue_rgb.get(i);
				colorMatrixTmp.setRotate((int) bar.getTag(), (bar.getProgress()-128) / 128f * 180);
				colorMatrix.postConcat(colorMatrixTmp);
			}
		}
		if(mOnColorFilterChangedListener!=null){
			mOnColorFilterChangedListener.OnColorFilterChanged(colorMatrix);
		}
	}
	
}

package com.knziha.plod.slideshow;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.widgets.SimpleClickableSpan;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.knziha.plod.PlainDict.Toastable_Activity.checkMargin;
import static com.knziha.plod.dictionarymodels.mdict.indexOf;

/** Photo View Activity.<br/> Original Author : someone on the internet. */
public class PhotoViewActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {
	public static List<mdictRes> mdd;
	public static PhotoBrowsingContext IBC;
	public List<mdictRes> mdd_;
	public PhotoBrowsingContext IBC_;
	public static final int OffScreenViewPagerSize=5;
	LinkedList<PhotoView> mViewCache = new LinkedList<>();
	private String[] imageUrls;
	private int curPosition = -1;
	private ViewPager mPager;

	private HashSet<Integer> processedRec = new HashSet<>();
	private TextView indicator;
	private View forward;
	private View backward;
	public static RequestListener<Drawable> mGlideListener=new RequestListener<Drawable>() {
		@Override
		public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
			ImageView medium_thumbnail = ((ImageViewTarget<?>) target).getView();
			medium_thumbnail.setImageResource(R.drawable.load_error);
			return true;
		}
		@Override
		public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
			return false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if(PDICMainAppOptions.isFullScreen())
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_photo_browser);
		mdd_=mdd;
		IBC_=IBC;
		IBC=null;
		mdd=null;
		imageUrls = getIntent().getStringArrayExtra("images");
		curPosition = getIntent().getIntExtra("current", 0);
		indicator = findViewById(R.id.indicator);
		backward = findViewById(R.id.browser_widget13);
		forward = findViewById(R.id.browser_widget14);
		if(imageUrls.length<2){
			indicator.setVisibility(View.GONE);
		}
		mPager = findViewById(R.id.pager);
		mPager.setPageTransformer(false, new DepthPageTransformer(), View.LAYER_TYPE_NONE);
		mPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
		forward.setOnClickListener(this);
		backward.setOnClickListener(this);
		findViewById(R.id.browser_widget12).setOnClickListener(this);
		//mPager.setOffscreenPageLimit(5);
		mPager.setAdapter(new PagerAdapter() {
			@Override
			public int getCount() {
				return imageUrls.length;
			}

			@Override
			public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
				return view == object;
			}

			@NonNull
			@Override
			public Object instantiateItem(@NonNull ViewGroup container, int position) {
				PhotoView pv;
				if (mViewCache.size() > 0) {
					pv = mViewCache.removeFirst();
				} else {
					pv = new PhotoView(container.getContext());
					pv.setOnClickListener(PhotoViewActivity.this);
					pv.setOnLongClickListener(PhotoViewActivity.this);
				}
				if (pv.getParent() != null)
					((ViewGroup) pv.getParent()).removeView(pv);
				container.addView(pv);
				pv.setTag(position);
				if(!processedRec.contains(position)){
					imageUrls[position] = ProcessUrl(imageUrls[position]);
					processedRec.add(position);
				}
				pv.IBC = IBC_;
				String key=imageUrls[position];
				try {
					Glide.with(PhotoViewActivity.this)
							.load(key.startsWith("/pdfimg/")?new PdfPic(key, getBaseContext()):new MddPic(mdd_, key))
							.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							.fitCenter()
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.listener(mGlideListener).into(pv);
				}catch (Exception e){e.printStackTrace();}
				return pv;
			}


			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView((View) object);
				mViewCache.add((PhotoView) object);
			}
		});
		mPager.setCurrentItem(curPosition, false);
		setIndicator();
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				curPosition = position;
				setIndicator();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		setFlipperVis();
		View downloadBtn=findViewById(R.id.browser_widget15);
		downloadBtn.setVisibility(PDICMainAppOptions.getShowSaveImage()?View.VISIBLE:View.GONE);
		downloadBtn.setOnClickListener(this);
		checkMargin(this);
	}

	private String ProcessUrl(String imageUrl) {
		String SepWindows = "\\";
		String FileTag = "file://";
		int start = imageUrl.indexOf(FileTag);
		if(start==-1){
			if(imageUrl.startsWith("./"))
				imageUrl = imageUrl.substring(1).replace("/", SepWindows);
			else
				imageUrl = imageUrl.replace("/", SepWindows);
		}else{
			imageUrl = imageUrl.substring(start+FileTag.length()).replace("/", SepWindows);
		}
		if(!imageUrl.startsWith(SepWindows)){
			imageUrl=SepWindows+imageUrl;
		}
		try {
			imageUrl=URLDecoder.decode(imageUrl,"UTF-8");
		} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		return imageUrl;
	}

	void setIndicator() {
		indicator.setText((curPosition + 1) + "/" + imageUrls.length);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.browser_widget1:
				if(PDICMainAppOptions.getClickDismissImageBrowser())
					finish();
			break;
			case R.id.browser_widget13:
				mPager.setCurrentItem(curPosition-1, false);
			break;
			case R.id.browser_widget14:
				mPager.setCurrentItem(curPosition+1, false);
			break;
			case R.id.browser_widget15:
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		String[] DictOpt = getResources().getStringArray(R.array.picture_spec);
		final String[] Coef = DictOpt[0].split("_");
		final View dv = LayoutInflater.from(this).inflate(R.layout.dialog_about,null);
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = dv.findViewById(R.id.title);
		title.setText("图片");
		title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);

		if(GlobalOptions.isLarge) tv.setTextSize(tv.getTextSize());
		tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

		init_clickspan(ssb, DictOpt, 1, new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 3, Coef, 0, 0, 0x1,41,1,1);
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1,42,1,0);
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 4, Coef, 0, 1, 0x1,43,1,0);

		ssb.delete(ssb.length()-4,ssb.length());
		tv.setText(ssb);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		AlertDialog d = new AlertDialog.Builder(this, GlobalOptions.isDark?R.style.DialogStyle3NP:R.style.DialogStyle4NP)
				.setView(dv).create();
		d.setCanceledOnTouchOutside(true);
		dv.findViewById(R.id.cancel).setOnClickListener(v1 -> d.dismiss());
		//tofo
		android.view.WindowManager.LayoutParams lp = d.getWindow().getAttributes();  //获取对话框当前的参数值
		lp.height = -2; lp.width=(int) (getResources().getDisplayMetrics().widthPixels*0.8);
		d.getWindow().setAttributes(lp);
		d.show();
		return true;
	}


	private static void init_clickspan(SpannableStringBuilder text,
						   String[] dictOpt, int titleOff, View.OnClickListener mClicker) {
		int start = text.length();
		text.append("[").append(dictOpt[titleOff]).append("]");
		text.setSpan(new SimpleClickableSpan(0xFFFF4081) {
			@Override
			public void onClick(@NonNull View widget) {
				mClicker.onClick(widget);
			}},start,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.append("\r\n").append("\r\n");
	}

	private void init_clickspan_with_bits_at(TextView tv, SpannableStringBuilder text,
													String[] dictOpt, int titleOff,
											 String[] coef, int coefOff, int coefShift,
											 long mask, int flagPosition, int flagMax, int valOff) {
		long val = (PDICMainAppOptions.SecondFlag>>flagPosition)&mask;
		int index= (((int)val+valOff)%(flagMax+1));
		int start = text.length();
		int now = start+dictOpt[titleOff].length();
		text.append("[").append(dictOpt[titleOff]).append(coef[coefOff+(index+coefShift)%(flagMax+1)]).append("]");
		text.setSpan(new SimpleClickableSpan(0xFFFF4081) {
			@Override
			public void onClick(@NonNull View widget) {
				long SFStamp = PDICMainAppOptions.SecondFlag;
				long val = (PDICMainAppOptions.SecondFlag>>flagPosition)&mask;
				val=(val+1)%(flagMax+1);
				PDICMainAppOptions.SecondFlag &= ~(mask << flagPosition);
				PDICMainAppOptions.SecondFlag |= (val << flagPosition);
				validifyStates(SFStamp);
				int fixedRange = indexOf(text, ':', now);
				int index= (((int)val+valOff)%(flagMax+1));
				text.delete(fixedRange+1, indexOf(text, ']', fixedRange));
				text.insert(fixedRange+1,coef[coefOff+(index+coefShift)%(flagMax+1)]);
				tv.setText(text);
			}},start,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.append("\r\n").append("\r\n");
	}

	public void validifyStates(long SFStamp){
		if(PDICMainAppOptions.getShowImageBrowserFlipper()!=PDICMainAppOptions.getShowImageBrowserFlipper(SFStamp)){
			setFlipperVis();
		}
		if(PDICMainAppOptions.getShowSaveImage()!=PDICMainAppOptions.getShowSaveImage(SFStamp)){
			findViewById(R.id.browser_widget15).setVisibility(PDICMainAppOptions.getShowSaveImage()?View.VISIBLE:View.GONE);
		}
	}

	private void setFlipperVis() {
		int vis = imageUrls.length>2&&PDICMainAppOptions.getShowImageBrowserFlipper()?View.VISIBLE:View.GONE;
		findViewById(R.id.browser_widget12).setVisibility(vis);
		forward.setVisibility(vis);
		backward.setVisibility(vis);
	}

	@Override
	protected void onDestroy() {
		if (mPager != null) {
			mPager.removeAllViews();
			mPager = null;
		}
		super.onDestroy();
	}
}

package com.knziha.plod.slideshow;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.OptionProcessor;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.knziha.plod.PlainDict.CMN.Visible;
import static com.knziha.plod.PlainDict.Toastable_Activity.checkMargin;

/** Photo View Activity based on Subsampling-Scale-Image-View */
public class PhotoViewActivity extends Activity implements View.OnClickListener,
		View.OnLongClickListener,
		OptionProcessor {
	public View background;
	public View float_menu;
	public View float_exit;
	public static List<mdictRes> mdd;
	public static PhotoBrowsingContext IBC;
	public List<mdictRes> mdd_;
	public PhotoBrowsingContext IBC_;
	public static final int OffScreenViewPagerSize=5;
	private String[] imageUrls;
	private int curPosition = -1;
	PDICMainAppOptions opt;

	LinkedList<PhotoHolder> mViewCache = new LinkedList<>();
	private ViewPager viewPager;

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
		opt = new PDICMainAppOptions(this);
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
		background = findViewById(R.id.background);
		float_menu = findViewById(R.id.float_menu);
		float_exit = findViewById(R.id.float_back);
		ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(background, "alpha", 0, 1);
		fadeInContents.start();
		viewPager = findViewById(R.id.pager);
		viewPager.setPageTransformer(false, new DepthPageTransformer(), View.LAYER_TYPE_NONE);
		viewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
		forward.setOnClickListener(this);
		backward.setOnClickListener(this);
		float_menu.setOnClickListener(this);
		float_exit.setOnClickListener(this);
		
		findViewById(R.id.browser_widget12).setOnClickListener(this);
		//mPager.setOffscreenPageLimit(5);
		viewPager.setAdapter(new PagerAdapter() {
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
				PhotoHolder vh;
				if (mViewCache.size() > 0) {
					vh = mViewCache.removeFirst();
				} else {
					vh = new PhotoHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.photo_view_pager_page, container, false));
					//pv.setMaxTileSize(1024);
					vh.subview.dm = getResources().getDisplayMetrics();
					vh.subview.view_pager_toguard = viewPager;
					vh.subview.setOnClickListener(PhotoViewActivity.this);
					vh.subview.setOnLongClickListener(PhotoViewActivity.this);
				}
				if (vh.itemView.getParent() != null)
					((ViewGroup) vh.itemView.getParent()).removeView(vh.itemView);
				container.addView(vh.itemView);
				vh.position=position;
				vh.pv.setTranslationX(0);
				vh.pv.setTranslationY(0);
				vh.pv.setScaleX(1);
				vh.pv.setScaleY(1);
				vh.pv.setRotation(0);
				vh.pv.setTag(R.id.home, false);
				if(!processedRec.contains(position)){
					imageUrls[position] = ProcessUrl(imageUrls[position]);
					processedRec.add(position);
				}
				String key=imageUrls[position];
				vh.path = key;
				//pv.IBC = IBC_;
				try {
					vh.subview.recycle();
					vh.subview.dm = container.getContext().getResources().getDisplayMetrics();
					if(CMCF!=null){
						vh.pv.setColorFilter(CMCF);
					}
					Glide.with(PhotoViewActivity.this)
							.asBitmap()
							.load(key.startsWith("/pdfimg/")?new PdfPic(key, getBaseContext()):new MddPic(mdd_, key))
							.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							.fitCenter()
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.listener(new RequestListener<Bitmap>() {
								@Override
								public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
									ImageView iv = ((ImageViewTarget<?>) target).getView();
									//iv.setImageDrawable(getResources().getDrawable(R.drawable.sky_background));
									getDimensionsAndOpenQuickScale(null, iv);
									CMN.Log("onLoadFailed");
									return true;
								}
								
								@Override
								public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
									ImageView iv = ((ImageViewTarget<?>) target).getView();
									if(iv.getTag(R.id.home)==null){//true ||
										return true;
									}
									getDimensionsAndOpenQuickScale(resource, iv);
									return false;
								}
							}).into(vh.pv);//mGlideListener
				}catch (Exception e){ CMN.Log(e); }
				return vh.itemView;
			}


			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView((View) object);
				mViewCache.add((PhotoHolder) ((View) object).getTag());
			}
		});
		viewPager.setCurrentItem(curPosition, false);
		setIndicator();
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
	
	private void getDimensionsAndOpenQuickScale(Bitmap resource, ImageView iv) {
		PhotoHolder vh = (PhotoHolder) iv.getTag();
		int[] dimension = null;
		SparseArray<int[]> bitmapDimensions = Glide.bitmapDimensions;
		if(resource==null){
			iv.setVisibility(View.GONE);
		} else {
			iv.setVisibility(View.VISIBLE);
			if(bitmapDimensions!=null){
				dimension=bitmapDimensions.get(System.identityHashCode(resource));
				CMN.Log("获得了", dimension);
			}
		}
		if(dimension==null && resource!=null){
			dimension = new int[]{resource.getWidth(), resource.getHeight()};
			CMN.Log("手动获取", dimension);
		}
		if(dimension!=null){
			
			DisplayMetrics dm = getResources().getDisplayMetrics();
			//vh.subview.orientation = rotation;
			vh.subview.setProxy(dimension, -1, resource, vh.path);
			vh.subview.ImgSrc =vh.path;
			//vh.pv.setVisibility(View.VISIBLE);
			if(false)
				if(dimension[0]*dimension[1]>2096*2096){
					vh.subview.setMaxTileSize(true? Integer.MAX_VALUE : 4096);
					vh.subview.setMaxTileSize(1080);
					float averageDpi = (dm.xdpi + dm.ydpi)/2;
					vh.subview.setMinimumTileDpi(averageDpi>400?280:(averageDpi>300?220:160));
					vh.subview.setMinimumTileDpi((int) (averageDpi/2));
					vh.subview.setMinimumTileDpi(280);
					CMN.Log(averageDpi, vh.subview.minimumTileDpi, "minimumTileDpi");
					vh.subview.setImage(ImageSource.uri(Uri.fromFile(new File(vh.path))));
				}
			
			vh.subview.resetScaleAndCenter();
		}
	}
	
	
	public static class PhotoHolder
			//extends RecyclerView.ViewHolder
	{
		public String path;
		ViewGroup itemView;
		int position;
		ImageView pv;
		TilesGridLayout pg;
		SubsamplingScaleImageView subview;
		PhotoHolder(View v){
			//super(v);
			itemView = (ViewGroup) v;
			pv = v.findViewById(R.id.imageView);
			//pg = v.findViewById(R.id.grid);
			subview = v.findViewById(R.id.subView);
			itemView.setTag(this);
			pv.setTag(this);
			//pv.setColorFilter(SubsamplingScaleImageView.sample_fileter);
			subview.view_to_guard = pv;
			//subview.view_to_paint = pg;
		}
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
			case R.id.float_menu:
				showImageTweakerDialog();
			break;
			case R.id.subView:
				if(PDICMainAppOptions.getClickDismissImageBrowser())
					finish();
			break;
			case R.id.browser_widget13:
				viewPager.setCurrentItem(curPosition-1, false);
			break;
			case R.id.browser_widget14:
				viewPager.setCurrentItem(curPosition+1, false);
			break;
			case R.id.browser_widget15:
			break;
		}
	}
	
	@Override
	public boolean onLongClick(View view) {
		switch (view.getId()) {
			case R.id.subView:
				if (opt.getPhotoViewLongclickable())
					float_menu.performClick();
			break;
		}
		return true;
	}
	
	ColorMatrixColorFilter CMCF;

	void showImageTweakerDialog(){
		final ViewPager dv = new ViewPager(this);
		PhotoPagerAjuster ppa = new PhotoPagerAjuster(this);
		ppa.setOnColorFilterChangedListener(colorMatrix -> {
			CMCF = new ColorMatrixColorFilter(colorMatrix);
			int cc = viewPager.getChildCount();
			for (int i = 0; i < cc; i++) {
				View ca = viewPager.getChildAt(i);
				ImageView iv = ca.findViewById(R.id.imageView);
				if(iv!=null){
					iv.setColorFilter(CMCF);
				}
			}
		});
		dv.setAdapter(ppa);
		
		BottomSheetDialog d = new BottomSheetDialog(this);
		d.setContentView(dv);
		Window win = d.getWindow();
		win.setDimAmount(0.2f);
		win.findViewById(R.id.design_bottom_sheet)
			.setBackgroundResource(R.drawable.half_round_corner_frame);
		d.setCanceledOnTouchOutside(true);
		
		d.show();
		
		//View dv = (View) d.getWindow().getDecorView().getTag();
		DisplayMetrics dm2 = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm2);
		dv.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * d.getBehavior().getHalfExpandedRatio() - getResources().getDimension(R.dimen._45_) * 1.75);
		
		CMN.recurseLogCascade(dv);
	}
	
	@Override
	public PDICMainAppOptions getOpt() {
		return opt;
	}
	
	@Override
	public void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val) {
		switch (processId){
			case 0://长按
				float_menu.setVisibility(Visible(opt.getPhotoViewShowFloatMenu()));
			break;
			case 1://菜单按钮
				float_menu.setVisibility(Visible(opt.getPhotoViewShowFloatMenu()));
			break;
			case 2:
				setFlipperVis();
			break;
			case 10://保存
			break;
			case 3://保存按钮
				findViewById(R.id.browser_widget15).setVisibility(Visible(PDICMainAppOptions.getShowSaveImage()));
			break;
			case 11://返回
			break;
			case 4://返回按钮
				float_exit.setVisibility(Visible(opt.getPhotoViewShowFloatExit()));
			break;
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
		if (viewPager != null) {
			viewPager.removeAllViews();
			viewPager = null;
		}
		super.onDestroy();
	}
}

package com.knziha.plod.slideshow;

import static com.knziha.plod.plaindict.CMN.Visible;
import static com.knziha.plod.plaindict.MainActivityUIBase.fix_full_screen_global;
import static com.knziha.plod.plaindict.Toastable_Activity.checkMargin;
import static com.knziha.plod.plaindict.Toastable_Activity.setStatusBarColor;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.plod.dictionary.Utils.Bag;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.OptionProcessor;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;

/** 精致小巧的图片浏览器 */
public class PhotoViewActivity extends AppCompatActivity implements View.OnClickListener,
		View.OnLongClickListener,
		OptionProcessor {
	public View background;
	public View float_menu;
	public View float_exit;
	private BookPresenter book;
	private String[] imageUrls;
	private String[] imageUrlKeys;
	private String[] imageUrl = new String[1];
	private int curPosition = -1;
	private int startPosition = -1;
	private long startBid = -1;
	private DisplayMetrics dm;
	PDICMainAppOptions opt;
	
	public final Bag lockX = new Bag(false);

	LinkedList<PhotoHolder> mViewCache = new LinkedList<>();
	private ViewPager viewPager;

	private TextView indicator;
	private View forward;
	private View backward;
	boolean backgounded;
	private RequestListener<Bitmap> mGListener = new RequestListener<Bitmap>() {
		@Override
		public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
			ImageView iv = ((ImageViewTarget<?>) target).getView();
			//iv.setImageDrawable(getResources().getDrawable(R.drawable.sky_background));
			getDimensionsAndOpenQuickScale(null, iv);
			iv.setImageResource(R.drawable.load_error);
			return true;
		}
		
		@Override
		public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
			ImageView iv = ((ImageViewTarget<?>) target).getView();
			//if(iv.getTag()==null) return true;
			//PhotoHolder vh = (PhotoHolder) ((ViewGroup)iv.getParent()).getTag();
			if(!backgounded){
				backgounded = true;
				ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(background, "alpha", 0, 1);
				fadeInContents.start();
			}
			getDimensionsAndOpenQuickScale(resource, iv);
			return false;
		}
	};
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus){
			fix_full_screen_global(getWindow().getDecorView(), true, PDICMainAppOptions.isFullscreenHideNavigationbar());
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		AgentApplication agent = ((AgentApplication)getApplication());
		book = agent.book;
		imageUrls = agent.Imgs;
		imageUrlKeys = new String[imageUrls.length];
		opt=agent.opt;
		curPosition = agent.currentImg;
		agent.clearTmp();
		
		startBid = book.getId();
		startPosition = curPosition;
		
		if(imageUrls==null){
			finish();
			return;
		}
		
		setStatusBarColor(getWindow(), Color.TRANSPARENT);
		setContentView(R.layout.activity_photo_browser);
		dm = opt.dm;
		indicator = findViewById(R.id.indicator);
		backward = findViewById(R.id.browser_widget13);
		forward = findViewById(R.id.browser_widget14);
		if(imageUrls.length<2){
			indicator.setVisibility(View.GONE);
		}
		background = findViewById(R.id.background);
		background.setBackgroundColor(GlobalOptions.isDark?0x33333333:Color.WHITE);
		float_menu = findViewById(R.id.float_menu);
		float_exit = findViewById(R.id.float_back);
		viewPager = findViewById(R.id.pager);
		viewPager.setPageTransformer(false, new DepthPageTransformer(), View.LAYER_TYPE_NONE);
		viewPager.setPageMargin((int) (dm.density * 15));
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
					vh.subview.dm = dm;
					vh.subview.view_pager_toguard = viewPager;
					vh.subview.setOnClickListener(PhotoViewActivity.this);
					vh.subview.setOnLongClickListener(PhotoViewActivity.this);
					vh.subview.lockX = lockX;
				}
				if (vh.itemView.getParent() != null)
					((ViewGroup) vh.itemView.getParent()).removeView(vh.itemView);
				container.addView(vh.itemView);
				vh.position=position;
				ImageView imageView = vh.pv;
				imageView.setTranslationX(0);
				imageView.setTranslationY(0);
				imageView.setScaleX(1);
				imageView.setScaleY(1);
				imageView.setRotation(0);
				imageView.setColorFilter(GlobalOptions.isDark?GlobalOptions.NEGATIVE:null);
				String url=imageUrls[position];
				vh.path = url;
				try {
					vh.subview.recycle();
					vh.subview.dm = dm;
					PhotoBrowserContext IBC = vh.subview.IBC;
					IBC.setIBC(book.IBC);
					IBC.pza = IBC.presetAlignment;
					if (IBC.presetAlignment==3) {
						if (book.getId() == startBid && position == startPosition) {
							IBC.lastX = book.a.weblist.scrollFocus.lastX;
							IBC.lastY = book.a.weblist.scrollFocus.lastY;
						} else {
							IBC.pza=0;
						}
					}
					if(CMCF!=null) {
						imageView.setColorFilter(CMCF);
					}
					String key = imageUrlKeys[position];
					imageUrl[0] = url;
					book = ViewUtils.getBookFromImageUrl(book, imageUrl, key==null);
					if (key==null) {
						imageUrlKeys[position]=key=ProcessUrl(imageUrl[0]);
					}
					Glide.with(PhotoViewActivity.this)
							.asBitmap()
							//.load(key.startsWith("/pdfimg/")?new PdfPic(key, getBaseContext()):new MddPic(resProvider, key))
							.load(new MddPic(book.bookImpl, key))
							.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							.fitCenter()
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.listener(mGListener).into(imageView);//mGlideListener
				} catch (Exception e){ CMN.Log(e); }
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
		for (int i = 0; i <= 4; i++) {
			processOptionChanged(null, null,  i, 0);
		}
	}
	
	private void getDimensionsAndOpenQuickScale(Bitmap resource, ImageView iv) {
		PhotoHolder vh = (PhotoHolder) ((ViewGroup)iv.getParent()).getTag();
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
		SubsamplingScaleImageView subview;
		PhotoHolder(View v){
			//super(v);
			itemView = (ViewGroup) v;
			pv = v.findViewById(R.id.imageView);
			//pg = v.findViewById(R.id.grid);
			subview = v.findViewById(R.id.subView);
			itemView.setTag(this);
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
		} else {
			imageUrl = imageUrl.substring(start+FileTag.length()).replace("/", SepWindows);
		}
		if(!imageUrl.startsWith(SepWindows)){
			imageUrl=SepWindows+imageUrl;
		}
		if(imageUrl.endsWith(SepWindows)){
			imageUrl=imageUrl.substring(0, imageUrl.length()-1);
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
			case R.id.float_back:
				finish();
			break;
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
				SaveCurrentImg();
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
		final ViewPager viewPager = new ViewPager(this);
		PhotoPagerAjuster ppa = new PhotoPagerAjuster(this, opt, viewPager);
		ppa.setOnColorFilterChangedListener(colorMatrix -> {
			CMCF = new ColorMatrixColorFilter(colorMatrix);
			int cc = this.viewPager.getChildCount();
			for (int i = 0; i < cc; i++) {
				View ca = this.viewPager.getChildAt(i);
				ImageView iv = ca.findViewById(R.id.imageView);
				if(iv!=null){
					iv.setColorFilter(CMCF);
				}
			}
		});
		viewPager.setAdapter(ppa);
		
		BottomSheetDialog d = new BottomSheetDialog(this);
		Window win = d.getWindow();
		win.setDimAmount(0.2f);
		d.setCanceledOnTouchOutside(true);
		
		boolean hideNav = PDICMainAppOptions.isFullscreenHideNavigationbar();
		if(hideNav){
			fix_full_screen_global(win.getDecorView(), false, true);
		}
		
		d.setOnDismissListener(ppa.OnDismissListener);
		
		d.setContentView(viewPager);
		
		win.findViewById(R.id.design_bottom_sheet)
				.setBackgroundResource(R.drawable.half_round_corner_frame);
		
		try {
			d.show();
		} catch (Exception ignored) {  }
		
		viewPager.setCurrentItem(PhotoPagerAjuster.PBC_tab, false);
		
		viewPager.getLayoutParams().height = (int) (Math.max(dm.heightPixels, dm.widthPixels) * d.getBehavior().getHalfExpandedRatio() - getResources().getDimension(R.dimen._45_) * 1.75);
	}
	
	@Override
	public void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val) {
		boolean bool;
		switch (processId){
			case 0://锁定X
				lockX.val = opt.getPhotoViewLockXMovement();
				break;
			case 1://菜单按钮
				bool = opt.getPhotoViewShowFloatMenu();
				if(!bool && !opt.getPhotoViewLongclickable()){
					if(widget!=null) {
						TextView tv = (TextView) widget;
						Spannable span = (Spannable) tv.getText();
						ClickableSpan[] spans = span.getSpans(0, span.getSpanStart(clickableSpan), ClickableSpan.class);
						spans[spans.length - 1].onClick(tv);
					} else {
						opt.setPhotoViewLongclickable(true);
					}
				}
				float_menu.setVisibility(Visible(bool));
			break;
			case 2://切换按钮
				setFlipperVis();
			break;
			case 10://保存
				SaveCurrentImg();
				break;
			case 3://保存按钮
				findViewById(R.id.browser_widget15).setVisibility(Visible(PDICMainAppOptions.getShowSaveImage()));
			break;
			case 11://返回
				finish();
			break;
			case 4://返回按钮
				float_exit.setVisibility(Visible(opt.getPhotoViewShowFloatExit()));
			break;
			case 12://长按
				bool = opt.getPhotoViewLongclickable();
				if(!bool && !opt.getPhotoViewShowFloatMenu()){
					TextView tv = (TextView) widget;
					Spannable span = (Spannable) tv.getText();
					if (span!=null) {
						ClickableSpan[] spans = span.getSpans(span.getSpanEnd(clickableSpan), span.length(), ClickableSpan.class);
						if(spans.length>0)spans[0].onClick(tv);
					}
				}
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
	
	private void SaveCurrentImg() {
//		String PicPath = imageUrls[viewPager.getCurrentItem()];
//		try {
//			InputStream resTmp = resProvider.getResourceByKey(PicPath);
//			if (resTmp != null) {
//				File f = new File("/sdcard/download", PicPath);
//				if(!f.exists()){
//					BU.SaveToFile(resTmp, f);
//					Toast.makeText(this, f.getName()+" 写入成功！", Toast.LENGTH_LONG).show();
//				}
//			}
//		} catch (Exception ignored) {  }
		//todo 123
	}
}

/*
Copyright 2013-2015 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.knziha.plod.slideshow;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.viewpager.widget.ViewPager;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.slideshow.decoder.CompatDecoderFactory;
import com.knziha.plod.slideshow.decoder.DecoderFactory;
import com.knziha.plod.slideshow.decoder.ImageDecoder;
import com.knziha.plod.slideshow.decoder.ImageRegionDecoder;
import com.knziha.plod.slideshow.decoder.SkiaImageDecoder;
import com.knziha.plod.slideshow.decoder.SkiaImageRegionDecoder;
import com.knziha.plod.widgets.FtagImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Displays an image subsampled as necessary to avoid loading too much image data into memory. After a pinch to zoom in,
 * a set of image tiles subsampled at higher resolution are loaded and displayed over the base layer. During pinch and
 * zoom, tiles off screen or higher/lower resolution than required are discarded from memory.
 *
 * Tiles are no larger than the max supported bitmap size, so with large images tiling may be used even when zoomed out.
 *
 * v prefixes - coordinates, translations and distances measured in screen (view) pixels
 * s prefixes - coordinates, translations and distances measured in source image pixels (scaled)
 */
@SuppressWarnings({"unused", "IntegerDivisionInFloatingPointContext"})
public class SubsamplingScaleImageView extends View {
	public FtagImageView view_to_guard;
	public TilesGridLayout view_to_paint;
	public boolean paint_to_grid=false;
	public ViewPager view_pager_toguard;
	
	private static final String TAG = SubsamplingScaleImageView.class.getSimpleName();
	
	/** Attempt to use EXIF information on the image to rotate it. Works for external files only. */
	public static final int ORIENTATION_USE_EXIF = -1;
	/** Display the image file in its native orientation. */
	public static final int ORIENTATION_0 = 0;
	/** Rotate the image 90 degrees clockwise. */
	public static final int ORIENTATION_90 = 90;
	/** Rotate the image 180 degrees. */
	public static final int ORIENTATION_180 = 180;
	/** Rotate the image 270 degrees clockwise. */
	public static final int ORIENTATION_270 = 270;
	
	/** Quadratic ease out. Not recommended for scale animation, but good for panning. */
	public static final int EASE_OUT_QUAD = 1;
	/** Quadratic ease in and out. */
	public static final int EASE_IN_OUT_QUAD = 2;
	
	/** State change originated from animation. */
	public static final int ORIGIN_ANIM = 1;
	/** State change originated from touch gesture. */
	public static final int ORIGIN_TOUCH = 2;
	/** State change originated from a fling momentum anim. */
	public static final int ORIGIN_FLING = 3;
	/** State change originated from a double tap zoom anim. */
	public static final int ORIGIN_DOUBLE_TAP_ZOOM = 4;
	
	public PhotoBrowsingContext IBC;
	
	// Uri of full size image
	private Uri uri;
	
	// Sample size used to display the whole image when fully zoomed out
	private int fullImageSampleSize;
	
	// Map of zoom level to tile grid
	//private LinkedHashMap<Integer, List<Tile>> tileMap;
	private TileMap[] tileMipMaps;
	
	// Overlay tile boundaries and other info
	private boolean SSVD=false;//debug
	
	private boolean SSVDF=false;//debug
	
	private boolean SSVDF2=false;//debug
	
	// Image orientation setting
	private int orientation = ORIENTATION_0;
	
	// Max scale allowed (prevent infinite zoom)
	public float maxScale = 1.5F;
	
	// Min scale allowed (prevent infinite zoom)
	private float minScale = minScale();
	
	// Density to reach before loading higher resolution tiles
	public int minimumTileDpi = -1;
	
	// overrides for the dimensions of the generated tiles
	public static final int TILE_SIZE_AUTO = Integer.MAX_VALUE;
	private int maxTileWidth = TILE_SIZE_AUTO;
	private int maxTileHeight = TILE_SIZE_AUTO;
	
	// An executor service for loading of images
	private Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;
	
	// Whether tiles should be loaded while gestures and animations are still in progress
	private boolean eagerLoadingEnabled = true;
	
	// Gesture detection settings
	private boolean panEnabled = true;
	private boolean zoomEnabled = true;
	private boolean rotationEnabled = true;
	private boolean quickScaleEnabled = true;
	
	// Double tap zoom behaviour
	private float[] quickZoomLevels = new float[3];
	private int quickZoomLevelCount = 2;
	
	// Current scale and scale at start of zoom
	public float scale;
	private float scaleStart;
	
	// Rotation parameters
	private float rotation = 0;
	// Stored to avoid unnecessary calculation
	private double cos = 1;//Math.cos(0);
	private double sin = 0;//Math.sin(0);
	
	// Screen coordinate of top-left corner of source image
	public PointF vTranslate = new PointF();
	PointF vTranslateOrg = new PointF();
	private PointF vTranslateStart = new PointF();
	private PointF vTranslateBefore = new PointF();
	
	// Source coordinate to center on, used when new position is set externally before view is ready
	private float pendingScale;
	private PointF sPendingCenter;
	private PointF sRequestedCenter;
	
	private AnimationBuilder pendingAnimation;
	
	// Source image dimensions and orientation - dimensions relate to the unrotated image
	private int sWidth;
	private int sHeight;
	private int sOrientation;
	private Rect sRegion;
	private Rect pRegion;
	
	// Is two-finger zooming in progress
	private boolean isZooming;
	// Is one-finger panning in progress
	private boolean isPanning;
	private boolean isRotating;
	// Is quick-scale gesture in progress
	private boolean isQuickScaling;
	// Max touches used in current gesture
	private int maxTouchCount;
	
	// Fling detector
	private GestureDetectorCompat flingdetector;
	
	// Tile and image decoding
	private ImageRegionDecoder decoder;
	private final ReadWriteLock decoderLock = new ReentrantReadWriteLock(true);
	public DecoderFactory<? extends ImageDecoder> bitmapDecoderFactory = new CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder.class);
	public DecoderFactory<? extends ImageRegionDecoder> regionDecoderFactory = new CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder.class);
	
	private boolean doubleTapDetected;
	private PointF doubleTapFocus = new PointF();
	// Debug values
	private final PointF vCenterStart = new PointF();
	private PointF sCenterStart = new PointF();
	private PointF vCenterStartNow = new PointF();
	private float vDistStart;
	private float lastAngle;
	
	// Current quickscale state
	private final float quickScaleThreshold;
	private float quickScaleLastDistance;
	private float quickScaleStart;
	private boolean quickScaleMoved;
	private PointF quickScaleVLastPoint;
	private PointF quickScaleSCenter;
	private PointF quickScaleVStart;
	private boolean startTouchWithAnimation;
	
	// Scale and center animation tracking
	private Anim anim;
	
	// Whether a ready notification has been sent to subclasses
	private boolean readySent;
	// Whether a base layer loaded notification has been sent to subclasses
	private boolean imageLoadedSent;
	
	// Long click listener
	private OnLongClickListener onLongClickListener;
	
	// Long click handler
	private final Handler handler;
	private static final int MESSAGE_LONG_CLICK = 1;
	
	// Paint objects
	private Paint bitmapPaint;
	private Paint debugTextPaint;
	private Paint debugLinePaint;
	private Paint tileBgPaint;
	
	// Volatile fields used to reduce object creation
	private ScaleTranslateRotate strTemp = new ScaleTranslateRotate(0, new PointF(0, 0), 0);
	private Matrix matrix = new Matrix();
	private RectF sRect;
	private final float[] srcArray = new float[8];
	private final float[] dstArray = new float[8];
	
	//The logical density of the display
	private final float density;
	
	// A global preference for bitmap format, available to decoder classes that respect it
	private static Bitmap.Config preferredBitmapConfig;
	public DisplayMetrics dm;
	public boolean isProxy;
	public String ImgSrc =null;
	private Runnable mAnimationRunnable = this::handle_animation;
	
	private Runnable flingRunnable = new Runnable() {
		@Override
		public void run() {
			//removeCallbacks(this);
			if(flingScroller.computeScrollOffset()){
				int cfx = flingScroller.getCurrX();
				int cfy = flingScroller.getCurrY();
				//CMN.Log("fling...", cfx - mLastFlingX, cfy - mLastFlingY, flingScroller.getCurrVelocity());
				
				float x = cfx - mLastFlingX;
				float y = cfy - mLastFlingY;
				
				mLastFlingX = cfx;
				mLastFlingY = cfy;
				
				int flag;
				
				if(!IBC.lockX){
					vTranslate.x = vTranslate.x+(flingVx>0?x:-x);// fingStartX + cfx-flingScroller.getStartX();
				}
				vTranslate.y = vTranslate.y+(flingVy>0?y:-y);// fingStartY + cfy-flingScroller.getStartY();
				
				//if(isProxy)
					handle_proxy_simul(scale, null, rotation);
				//handle_proxy_view(view_pager_toguard, scale, null, rotation);
				
				//handle_proxy_view(view_to_paint, scale, null, rotation);
				if(!isProxy){
					refreshRequiredTiles(true); // flingScroller.getCurrVelocity()<2500
					invalidate();
				}
				
				post(this);
			}
			//else scroll ended
		}
	};
	private float fingStartX;
	private float fingStartY;
	private float flingVx;
	private float flingVy;
	private boolean waitingNextTouchResume;
	private boolean fakeScrolled;
	private float pre_sx;
	private float view_pager_toguard_StartX;
	private float view_pager_toguard_StartY;
	private float fakeDragBeginX;
	private float view_pager_toguard_lastX;
	private float view_pager_toguard_lastY;
	private float delta5;
	private float lastCompensation;
	private boolean dragOnRight;
	private int maxImageSampleSize;
	private PointF quickScalesStart;
	private long lastDrawTime;
	public static ColorFilter sample_fileter = new ColorMatrixColorFilter(new float[]{
			0.310f, 0.000f, 0.690f, 0.000f, 0.139f,
			0.310f, 0.172f, 0.000f, 0.000f, 0.139f,
			0.448f, 0.000f, -0.052f, 0.000f, 0.139f,
			0.000f, 0.000f, 0.000f, 1.000f, 0.139f,
	});
	private float vtParms_sew;
	private float vtParms_seh;
	private float vtParms_se_delta;
	private float vtParms_se_SWidth;
	private int vtParms_dragDir;
	private int vtParms_dir;
	private boolean vtParms_b1;
	private boolean vtParms_b2;
	private boolean vtParms_b3;
	private double vtParms_cos = Math.cos(0);
	private double vtParms_sin = Math.sin(0);
	private boolean isDown;
	private OnClickListener mClickListener;
	private boolean LongClickDetected;
	private boolean clickable;
	private boolean UseUserLevels;
	
	public SubsamplingScaleImageView(Context context) {
		this(context, null);
	}
	
	public SubsamplingScaleImageView(Context context, AttributeSet attr) {
		super(context, attr);
		flingScroller = new OverScroller(getContext());
		//flingScroller.setInterpolator(new LinearInterpolator());
		density = getResources().getDisplayMetrics().density;
		//setMinimumDpi(160)(ViewGroup;
		setMinimumTileDpi(320);
		setGestureDetector(context);
		createPaints();
		this.handler = new Handler(new Handler.Callback() {
			public boolean handleMessage(Message message) {
				if (message.what == MESSAGE_LONG_CLICK && onLongClickListener != null) {
					//maxTouchCount = 0;
					LongClickDetected = true;
					SubsamplingScaleImageView.super.setOnLongClickListener(onLongClickListener);
					performLongClick();
					SubsamplingScaleImageView.super.setOnLongClickListener(null);
				}
				return true;
			}
		});
		quickScaleThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
	}
	
	
	/**
	 * Get the current preferred configuration for decoding bitmaps. {@link ImageDecoder} and {@link ImageRegionDecoder}
	 * instances can read this and use it when decoding images.
	 * @return the preferred bitmap configuration, or null if none has been set.
	 */
	public static Bitmap.Config getPreferredBitmapConfig() {
		return preferredBitmapConfig;
	}
	
	/**
	 * Set a global preferred bitmap config shared by all view instances and applied to new instances
	 * initialised after the call is made. This is a hint only; the bundled {@link ImageDecoder} and
	 * {@link ImageRegionDecoder} classes all respect this (except when they were constructed with
	 * an instance-specific config) but custom decoder classes will not.
	 * @param preferredBitmapConfig the bitmap configuration to be used by future instances of the view. Pass null to restore the default.
	 */
	public static void setPreferredBitmapConfig(Bitmap.Config preferredBitmapConfig) {
		SubsamplingScaleImageView.preferredBitmapConfig = preferredBitmapConfig;
	}
	
	/**
	 * Sets the image orientation. It's best to call this before setting the image file or asset, because it may waste
	 * loading of tiles. However, this can be freely called at any time.
	 * @param orientation orientation to be set. See ORIENTATION_ static fields for valid values.
	 */
	public final void setOrientation(int orientation) {
		if(orientation>=0&&orientation<=270 && orientation%90==0){
			this.orientation = orientation;
			reset(false);
			invalidate();
			//requestLayout();
		}
	}
	
	/**
	 * Set the image source from a bitmap, resource, asset, file or other URI.
	 * @param imageSource Image source.
	 */
	public final void setImage(@NonNull ImageSource imageSource) {
		setImage(imageSource, null);
	}
	
	/**
	 * Set the image source from a bitmap, resource, asset, file or other URI, providing a preview image to be
	 * displayed until the full size image is loaded, starting with a given orientation setting, scale and center.
	 * This is the best method to use when you want scale and center to be restored after screen orientation change;
	 * it avoids any redundant loading of tiles in the wrong orientation.
	 *
	 * You must declare the dimensions of the full size image by calling {@link ImageSource#dimensions(int, int)}
	 * on the imageSource object. The preview source will be ignored if you don't provide dimensions,
	 * and if you provide a bitmap for the full size image.
	 * @param imageSource Image source. Dimensions must be declared.
	 * @param state State to be restored. Nullable.
	 */
	public final void setImage(@NonNull ImageSource imageSource, ImageViewState state) {
		//noinspection ConstantConditions
		if (imageSource == null) {
			throw new NullPointerException("imageSource must not be null");
		}
		
		//reset(true);
		if (state != null) { restoreState(state); }
		
		if (imageSource.getBitmap() != null && imageSource.getSRegion() != null) {
			onImageLoaded(ORIENTATION_0, false);
		} else if (imageSource.getBitmap() != null) {
			onImageLoaded(ORIENTATION_0, imageSource.isCached());
		} else {
			sRegion = imageSource.getSRegion();
			uri = imageSource.getUri();
			if (uri == null && imageSource.getResource() != null) {
				uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getContext().getPackageName() + "/" + imageSource.getResource());
			}
			// Load the bitmap using tile decoding.
			try {
				//CMN.Log("TilesInitTask.doInBackground");
//				decoder = regionDecoderFactory.make();
//				Point dimensions = decoder.init(getContext(), uri);
////					int sWidth = dimensions.x;
////					int sHeight = dimensions.y;
////					if (sRegion != null) {
////						if(sRegion.left<0) sRegion.left = 0;
////						if(sRegion.top<0) sRegion.top = 0;
////						if(sRegion.right>sWidth) sRegion.right = sWidth;
////						if(sRegion.right>sHeight) sRegion.bottom = sHeight;
////						sWidth = sRegion.width();
////						sHeight = sRegion.height();
////					}
//				onTilesInited();
				TilesInitTask task = new TilesInitTask(this);
				execute(task);
				CMN.Log("setImage getExifOrientation", sOrientation, sOrientation, rotation, ImgSrc);
			} catch (Exception e) { CMN.Log(e);  }
		}
		//imgsrc = imageSource.toString();
	}
	
	/**
	 * Async task used to get image details without blocking the UI thread.
	 */
	private static class TilesInitTask extends AsyncTask<Void, Void, Void> {
		private final WeakReference<SubsamplingScaleImageView> viewRef;
		
		TilesInitTask(SubsamplingScaleImageView view) {
			viewRef = new WeakReference<>(view);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				SubsamplingScaleImageView view = viewRef.get();
				if (view != null) {
					view.debug("TilesInitTask.doInBackground");
					ImageRegionDecoder decoder = view.regionDecoderFactory.make();
					Point dimensions = decoder.init(view.getContext(), view.uri);
					view.decoder = decoder;
//					int sWidth = dimensions.x;
//					int sHeight = dimensions.y;
//					if (sRegion != null) {
//						if(sRegion.left<0) sRegion.left = 0;
//						if(sRegion.top<0) sRegion.top = 0;
//						if(sRegion.right>sWidth) sRegion.right = sWidth;
//						if(sRegion.right>sHeight) sRegion.bottom = sHeight;
//						sWidth = sRegion.width();
//						sHeight = sRegion.height();
//					}
				}
			} catch (Exception e) {
				//CMN.Log(TAG, "Failed to initialise bitmap decoder", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void xyo) {
			final SubsamplingScaleImageView view = viewRef.get();
			if (view != null) {
				view.onTileInited();
			}
		}
	}
	
	/**
	 * Reset all state before setting/changing image or setting new rotation.
	 */
	private void reset(boolean newImage) {
		debug("reset newImage=" + newImage);
		pendingScale = 0f;
		isZooming = false;
		isPanning = false;
		isQuickScaling = false;
		maxTouchCount = 0;
		fullImageSampleSize = 0;
		vDistStart = 0;
		quickScaleLastDistance = 0f;
		quickScaleMoved = false;
		if (newImage) {
			uri = null;
			decoderLock.writeLock().lock();
			try {
				if (decoder != null) {
					decoder.recycle();
					decoder = null;
				}
			} finally {
				decoderLock.writeLock().unlock();
			}
			sWidth = 0;
			sHeight = 0;
			sOrientation = 0;
			readySent = false;
			imageLoadedSent = false;
		}
		if (tileMipMaps != null) {
			for (TileMap tmI : tileMipMaps) {
				for (Tile tile : tmI.tiles) {
					tile.visible = false;
					if (tile.bitmap != null) {
						tile.bitmap.recycle();
						tile.bitmap = null;
					}
				}
			}
			tileMipMaps = null;
		}
	}
	
	public void refresh() {
		pendingScale = 0f;
		sPendingCenter = null;
		sRequestedCenter = null;
		isZooming = false;
		isPanning = false;
		isQuickScaling = false;
		maxTouchCount = 0;
		fullImageSampleSize = 0;
		vDistStart = 0;
		quickScaleLastDistance = 0f;
		quickScaleMoved = false;
		quickScaleSCenter = null;
		quickScaleVLastPoint = null;
		quickScaleVStart = null;
		anim = null;
		sRect = null;
	}
	
	int mLastFlingX;
	int mLastFlingY;
	private boolean onFlingDetected;
	private int MAX_FLING_OVER_SCROLL = (int) (30*getContext().getResources().getDisplayMetrics().density);
	@SuppressWarnings("SuspiciousNameCombination")
	private void setGestureDetector(final Context context) {
		this.flingdetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				onFlingDetected =true;
				if(isQuickScaling||scale <= minScale()){
					return true;
				}
				if (panEnabled && (readySent||isProxy)
						//&& (Math.abs(e1.getX() - e2.getX()) > 50 || Math.abs(e1.getY() - e2.getY()) > 50)
						&& (Math.abs(velocityX) > 500 || Math.abs(velocityY) > 500)
						&& !isZooming) {
					
					float vxDelta = vTranslate.x;
					float vyDelta = vTranslate.y;

					float scaleWidth = scale * sWidth;
					float scaleHeight = scale * sHeight;
					float screenWidth = getScreenWidth();
					float screenHeight = getScreenHeight();

					float widthDelta;
					float heightDelta;
					float minX,maxX,minY,maxY;
					
					if(vtParms_dir==1){
						widthDelta=screenWidth-scaleWidth;
						heightDelta=screenHeight-scaleHeight;
						minX = screenWidth-scaleWidth;
						maxX = 0;
						if(minX>0) minX=maxX=minX/2;
						
						minY = screenHeight-scaleHeight;
						maxY = 0;
						if(minY>0) minY=maxY=minY/2;
						//CMN.Log("x限制·1", minX, maxX, minY, maxY);
					} else {
						widthDelta=screenWidth-scaleHeight;
						heightDelta=screenHeight-scaleWidth;
						minX = heightDelta;
						maxX = 0;
						if(minX>0) maxX=minX=minX/2;
						minX -= vtParms_se_delta*vtParms_dir;
						maxX -= vtParms_se_delta*vtParms_dir;
						
						minY = widthDelta;
						maxY = 0;
						if(minY>0) minY=maxY=minY/2;
						minY += vtParms_se_delta*vtParms_dir;
						maxY += vtParms_se_delta*vtParms_dir;
						//CMN.Log("x限制·2", minX, maxX, minY, maxY);
					}
					
					int distanceX = 0;
					int distanceY = 0;
					
					float vX = (float) (velocityX * cos - velocityY * -sin);
					float vY = (float) (velocityX * -sin + velocityY * cos);
					if(vxDelta<=minX || vxDelta>=maxX){
						vX = 0;
					} else {
						distanceX = Math.abs((int) (vX>0?(maxX-vxDelta):(vxDelta-minX)));
					}
					if(vyDelta<=minY || vyDelta>=maxY){
						vY = 0;
					} else {
						distanceY = Math.abs((int) (vY>0?(maxY-vyDelta):(vyDelta-minY)));
					}
					if(vX!=0 || vY!=0) {
						// Account for rotation
						boolean SameDir = Math.signum(vX) == Math.signum(flingVx) && Math.signum(vY) == Math.signum(flingVy);

						flingVx = vX;
						flingVy = vY;
						
						if(vtParms_dir==1) {
							distanceX = minX >= 0 ? 0 : (int) (vX > 0 ? Math.abs(vxDelta) : vxDelta - widthDelta);
							distanceY = minY >= 0 ? 0 : (int) (vY > 0 ? Math.abs(vyDelta) : vyDelta - heightDelta);
						}
						//CMN.Log("fling 初始参数(vs, vy, distX, distY)", flingVx, flingVy, distanceX, distanceY);
						
						vX = Math.abs(vX);
						vY = Math.abs(vY);

						if (vX==0 || distanceX < 0) distanceX = 0;
						if (vY==0 || distanceY < 0) distanceY = 0;

						int overX = distanceX < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL;
						int overY = distanceY < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL;

						if (velocityX == 0) distanceX = 0;
						if (velocityY == 0) distanceY = 0;

						mLastFlingY = mLastFlingX = 0;
						//CMN.Log("fling 最终参数(vs, vy, distX, distY)", flingVx, flingVy, distanceX, distanceY);
						if(distanceX!=0 || distanceY!=0)
							clickable = false;
						
						double distMultY = 2.8;
						double distMultX = 2.8;
						if(sHeight>sWidth){
							distMultX = distMultY*sWidth/sHeight;
						}
						
						flingScroller.fling(mLastFlingX, mLastFlingY, (int) vX, (int) vY,
								0, distanceX, 0, distanceY, overX, overY, SameDir, distMultX, distMultY);
						
						post(flingRunnable);
						return true;
					}
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (mClickListener != null && !LongClickDetected) {
					int tol = 6;
					if(Math.abs(view_pager_toguard.lastX-view_pager_toguard_StartX)<=tol&&
							Math.abs(view_pager_toguard.lastY-view_pager_toguard_StartY)<=tol){
						mClickListener.onClick(SubsamplingScaleImageView.this);
						return true;
					}
				}
				return true;
			}
			
			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				if (zoomEnabled && (readySent||isProxy) && e.getActionMasked()==MotionEvent.ACTION_DOWN) {
					CMN.Log("kiam 双击");
					//setGestureDetector(context);
					doubleTapDetected=true;
					//doubleTapFocus.set(e.getX(), e.getY());
					doubleTapFocus.set(vCenterStart.x, vCenterStart.y);
					// Store quick scale params. This will become either a double tap zoom or a
					// quick scale depending on whether the user swipes.
					//vCenterStart = new PointF(e.getX(), e.getY());
					//vTranslateStart = new PointF(vTranslate.x, vTranslate.y);
					scaleStart = scale;
					isQuickScaling = true;
					isZooming = true;
					quickScaleLastDistance = -1F;
					quickScaleStart = scale;
					//
					quickScaleSCenter = viewToSourceCoord(vCenterStart);
					quickScaleVStart = new PointF(vCenterStart.x, vCenterStart.y);
					quickScaleVLastPoint = new PointF(quickScaleSCenter.x, quickScaleSCenter.y);
					quickScaleMoved = false;
					// We need to get events in onTouchEvent after this.
					return false;
				}
				return true;
			}
			
		});
		
		flingdetector.setIsLongpressEnabled(false);
	}
	
	/**
	 * On resize, preserve center and scale. Various behaviours are possible, override this method to use another.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		debug("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h);
		PointF sCenter = getCenter();
		if (readySent && sCenter != null) {
			this.anim = null;
			this.pendingScale = scale;
			this.sPendingCenter = sCenter;
		}
	}
	
//	/**
//	 * Measures the width and height of the view, preserving the aspect ratio of the image displayed if wrap_content is
//	 * used. The image will scale within this box, not resizing the view as it is zoomed.
//	 */
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
//		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
//		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
//		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
//		boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
//		boolean resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
//		int width = parentWidth;
//		int height = parentHeight;
//		if (sWidth > 0 && sHeight > 0) {
//			if (resizeWidth && resizeHeight) {
//				width = sWidth();
//				height = sHeight();
//			} else if (resizeHeight) {
//				height = (int)((((double)sHeight()/(double)sWidth()) * width));
//			} else if (resizeWidth) {
//				width = (int)((((double)sWidth()/(double)sHeight()) * height));
//			}
//		}
//		width = Math.max(width, getSuggestedMinimumWidth());
//		height = Math.max(height, getSuggestedMinimumHeight());
//		setMeasuredDimension(width, height);
//	}
	
	/**
	 * Handle touch events. One finger pans, and two finger pinch and zoom plus panning.
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		//CMN.Log("onTouchEvent");
		// During non-interruptible anims, ignore all touch events
		int touch_type = event.getAction() & MotionEvent.ACTION_MASK;
		boolean isDown = touch_type==MotionEvent.ACTION_DOWN||touch_type==MotionEvent.ACTION_POINTER_DOWN;

		if(waitingNextTouchResume){
			if(!isDown){
				return true;
			}
			waitingNextTouchResume=false;
			//从善如流
			touch_partisheet.add(0);
		}
		
		if(isDown) {
			flingdetector.mImpl.clickable = flingScroller.isFinished();
			LongClickDetected=false;
			isQuickScaling=quickScaleMoved=false;
			if(touch_type==MotionEvent.ACTION_DOWN){
				vCenterStart.set(event.getX(), event.getY());
			}
			startTouchWithAnimation = anim != null;
		}
		
		// Abort if not ready
		// Detect flings, taps and double taps
		onFlingDetected =false;
		boolean flingEvent = flingdetector.onTouchEvent(event);
		
		//if(!(onflingdetected && scale <= minScale())){
		//	if (!isQuickScaling && flingEvent) {
		//		isZooming = false;
		//		isPanning = false;
		//		maxTouchCount = 0;
		//		return true;
		//	}
		//}
		
		
		if (anim != null){
			if(!anim.interruptible) {
				//requestDisallowInterceptTouchEvent(true);
				return true;
			} else {
				anim = null;
			}
		}
		
		// Store current values so we can send an event if they change
		float scaleBefore = scale;
		vTranslateBefore.set(vTranslate);
		float rotationBefore = rotation;
		
		boolean handled = onTouchEventInternal(event);
		//sendStateChanged(scaleBefore, vTranslateBefore, rotationBefore, ORIGIN_TOUCH);
		return handled||super.onTouchEvent(event);
	}
	
	HashSet<Integer> touch_partisheet = new HashSet<>();
	boolean is_classical_pan_shrinking=false;
	PointF tmpCenter = new PointF();
	private boolean onTouchEventInternal(@NonNull MotionEvent event) {
		int touchCount = event.getPointerCount();
		int touch_type = event.getAction() & MotionEvent.ACTION_MASK;
		int touch_id = event.getPointerId(event.getActionIndex());
		switch (touch_type) {
			case MotionEvent.ACTION_DOWN:{
				isDown = true;
				touch_partisheet.clear();
				view_pager_toguard_lastX =
				view_pager_toguard_StartX = view_pager_toguard.lastX;
				view_pager_toguard_StartY = view_pager_toguard.lastY;
				view_pager_toguard_lastY = view_pager_toguard_StartY;
				lastCompensation = 0;
				isRotating = false;
			}
			case MotionEvent.ACTION_POINTER_DOWN:{
				//CMN.Log("ACTION_DOWN", touchCount);
				doubleTapDetected=false;
				flingScroller.abortAnimation();
				if(touch_partisheet.size()==0 && touch_type==MotionEvent.ACTION_POINTER_DOWN) {
					break;
				}
				int touch_seat_count = 2-touch_partisheet.size();
				for(int i=0;i<Math.min(touch_seat_count, touchCount);i++) {
					if(touch_partisheet.contains(event.getPointerId(i))) {
						touch_seat_count++;
					}else
						touch_partisheet.add(event.getPointerId(i));
				}
				
				anim = null;
				requestDisallowInterceptTouchEvent(true);
				maxTouchCount = Math.max(maxTouchCount, touchCount);
				vTranslateStart.set(vTranslate.x, vTranslate.y);
				if (touchCount >= 2) {
					// Start pinch to zoom. Calculate distance between touch points and center point of the pinch.
					float distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
					scaleStart = scale;
					vDistStart = distance;
					vCenterStart.set((event.getX(0) + event.getX(1))/2, (event.getY(0) + event.getY(1))/2);
					viewToSourceCoord(vCenterStart, sCenterStart);
					
					if (!zoomEnabled && !rotationEnabled) {
						// Abort all gestures on second touch
						maxTouchCount = 0;
					}
					
					if (rotationEnabled) {
						lastAngle = (float) Math.atan2((event.getY(0) - event.getY(1)), (event.getX(0) - event.getX(1)));
					}
					
					// Cancel long click timer
					handler.removeMessages(MESSAGE_LONG_CLICK);
				}
				else if (!isQuickScaling) {
					// Start one-finger pan
					vCenterStart.set(event.getX(), event.getY());
					float mold =  ((ViewGroup)getParent()).getLeft() - view_pager_toguard.getScrollX();
					//CMN.Log(" mold on down:::", vTranslateStart.x);
					vTranslateStart.x += mold;
					
					// Start long click timer
					handler.sendEmptyMessageDelayed(MESSAGE_LONG_CLICK, 450);
				}
			} return true;
			case MotionEvent.ACTION_MOVE:{
				//CMN.Log("ACTION_MOVE", touchCount);
				if(!isDown){
					MotionEvent ev = MotionEvent.obtain(event);
					ev.setAction(MotionEvent.ACTION_DOWN);
					onTouchEventInternal(ev);
				}
				float preXCoord = vTranslate.x;
				float preYCoord = vTranslate.y;
				if(!touch_partisheet.contains(touch_id))
					return true;
				boolean consumed = false;
				float scaleStamp=scale,rotationStamp=rotation;
				PointF tranlationStamp = new PointF(vTranslate.x, vTranslate.y);
				PointF centerAt = new PointF(sWidth/2, sHeight/2);
				PointF result = new PointF();
				sourceToViewCoord(centerAt, result);
				
				boolean isDragging = view_pager_toguard.isFakeDragging();
				
				if (maxTouchCount > 0) {
					if (touch_partisheet.size() >= 2) {
						//if(view_pager_toguard.isFakeDragging()) view_pager_toguard.endFakeDrag();
						// Calculate new distance between touch points, to scale and pan relative to start values.
						float vDistEnd = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
						float vCenterEndX = (event.getX(0) + event.getX(1))/2;
						float vCenterEndY = (event.getY(0) + event.getY(1))/2;
						
						if(rotationEnabled && !isQuickScaling){
							float angle = (float) Math.atan2((event.getY(0) - event.getY(1)), (event.getX(0) - event.getX(1)));
							
							if(!isRotating){
								//CMN.Log("isRotating?", Math.abs(angle - lastAngle)/Math.PI*180);
								if(/*!isDragging && */Math.abs(angle - lastAngle)/Math.PI*180>22){
									isRotating = true;
									lastAngle = angle;
								}
							} else {
								setRotationInternal(rotation + angle - lastAngle);
								lastAngle = angle;
								consumed = true;
								//if(view_to_guard!=null)view_to_guard.setRotation((float) (rotation/Math.PI*180));
							}
						}
						
						if (isPanning || distance(vCenterStart.x, vCenterEndX, vCenterStart.y, vCenterEndY) > 5 || Math.abs(vDistEnd - vDistStart) > 5) {
							isZooming = true;
							isPanning = true;
							consumed = true;
							
							double previousScale = scale;
							if (zoomEnabled) {
								scale = (vDistEnd / vDistStart) * scaleStart;//Math.min(maxScale, );
							}
							//android.util.Log.e("fatal","scale"+scale);
							
							if (panEnabled) {
								// Translate to place the source image coordinate that was at the center of the pinch at the start
								// at the center of the pinch now, to give simultaneous pan + zoom.
								//viewToSourceCoord(vCenterStart, sCenterStart);
								sourceToViewCoord(sCenterStart, vCenterStartNow);
								
								final float dx = (vCenterEndX - vCenterStartNow.x);
								final float dy = (vCenterEndY - vCenterStartNow.y);
								
								float dxR = (float) (dx * cos - dy * -sin);
								float dyR = (float) (dx * -sin + dy * cos);
								
								vTranslate.x += dxR;
								vTranslate.y += dyR;
								
								// TODO: Account for rotation
								boolean b1 = true || scale * sHeight() >= getScreenHeight();
								boolean b2 = true || scale * sWidth() >= getScreenWidth();
								boolean b3 = true || previousScale * sHeight() < getScreenHeight();
								boolean b4 = true || previousScale * sWidth() < getScreenWidth();
								if (true) {//(b3 && b1) || (b4 && b2)
									//fitToBounds(true,true);
//									vCenterStart.set(vCenterEndX, vCenterEndY);
//									vTranslateStart.set(vTranslate);
//									scaleStart = scale;
//									vDistStart = vDistEnd;
								}
								
							} else if (sRequestedCenter != null) {
								// With a center specified from code, zoom around that point.
//								vTranslate.x = (getScreenWidth()/2) - (scale * sRequestedCenter.x);
//								vTranslate.y = (getScreenHeight()/2) - (scale * sRequestedCenter.y);
							} else {
								// With no requested center, scale around the image center.
//								vTranslate.x = (getScreenWidth()/2) - (scale * (sWidth()/2));
//								vTranslate.y = (getScreenHeight()/2) - (scale * (sHeight()/2));
							}
							
							//fitToBounds(true,true);
							//fitCenter();
							refreshRequiredTiles(eagerLoadingEnabled);
						}
					}
					else if (isQuickScaling) {
						float rawY = event.getY();
						float rawDist = quickScaleVStart.y - rawY;
						float dist = rawDist;
						if(dist<0) dist=-dist;
						if(!quickScaleMoved){
							if(dist > quickScaleThreshold){
								dist -= quickScaleThreshold;
								quickScaleVStart.y += quickScaleThreshold*(rawDist<0?1:-1);
								quickScaleMoved = true;
								quickScaleLastDistance = 0;
								quickScalesStart = viewToSourceCoord(quickScaleVStart);
							}
						}
						
						if (quickScaleMoved) {
							float multiplier = 1+dist/quickScaleThreshold;
							
							if(rawY < quickScaleVStart.y)
								multiplier=1/multiplier;
							
							float previousScale = scale;
							
							scale = Math.max(0, quickScaleStart * multiplier);
							//CMN.Log("isQuickScaling",Math.abs(quickScaleLastDistance-dist), scale, dist);
							
							if (panEnabled) {
								vTranslate.x = vTranslateStart.x;
								vTranslate.y = vTranslateStart.y;

								PointF quickScaleVStartNow = sourceToViewCoord(quickScalesStart);
								float xd = quickScaleVStart.x - quickScaleVStartNow.x;
								float yd = quickScaleVStart.y - quickScaleVStartNow.y;

								float dx =  (float) (xd * cos - yd * -sin);
								float dy = (float) (xd * -sin + yd * cos);

								vTranslate.x = vTranslateStart.x + dx;
								vTranslate.y = vTranslateStart.y + dy;
								
								////////////////////////////////////////////////////

//								float startTx = vTranslate.x;
//								float startTy = vTranslate.y;
//
//								float asdasdX =  (float) (vTranslateStart.x * cos - vTranslateStart.y * -sin);
//								float asdasdY = (float) (vTranslateStart.x * -sin + vTranslateStart.y * cos);
//
//								float vLeftStart = vTranslateStart.x - quickScaleVStart.x;
//								float vTopStart = vTranslateStart.y - quickScaleVStart.y;
//
//
//								float vLeftNow = vLeftStart * (scale/scaleStart);
//								float vTopNow = vTopStart * (scale/scaleStart);
//
//								CMN.Log("vector_delta", vLeftNow, vTopNow);
//
//								float tdxR =  (float) (vLeftNow * cos - vTopNow * -sin);
//								float tdyR = (float) (vLeftNow * -sin + vTopNow * cos);
//
//								vTranslate.x = quickScaleVStart.x + tdxR;
//								vTranslate.y = quickScaleVStart.y + tdyR;
//
//								tdxR =  (float) (vTranslate.x * cos - vTranslate.y * sin);
//								tdyR = (float) (vTranslate.x * sin + vTranslate.y * cos);
//								vTranslate.x = tdxR;
//								vTranslate.y = tdyR;

//								float baseTx = vTranslateStart.x;
//								float baseTy = vTranslateStart.y;
//
//								float tdx = vTranslate.x - baseTy;
//								float tdy = vTranslate.y - baseTy;
//								float tdxR =  (float) (tdx * cos - tdy * -sin);
//								float tdyR = (float) (tdx * -sin + tdy * cos);
//								vTranslate.x = baseTx + tdxR;
//								vTranslate.y = baseTy + tdyR;


//								float tdx1 = vTranslate.x - vTranslateStart.x;
//								float tdy1 = vTranslate.y - vTranslateStart.y;
//
//								float tdxR1 =  (float) (tdx1 * cos - tdy1 * -sin);
//								float tdyR1 = (float) (tdx1 * -sin + tdy1 * cos);
//
//								vTranslate.x = vTranslateStart.x + tdxR1;
//								vTranslate.y = vTranslateStart.y + tdyR1;
								
								if ((previousScale * sHeight() < getScreenHeight() && scale * sHeight() >= getScreenHeight()) || (previousScale * sWidth() < getScreenWidth() && scale * sWidth() >= getScreenWidth())) {
									//nono fitToBounds(true,true);
									
//									vCenterStart.set(sourceToViewCoord(quickScaleSCenter));
//									vTranslateStart.set(vTranslate);
//									scaleStart = scale;
								}
							} else if (sRequestedCenter != null) {
								// With a center specified from code, zoom around that point.
								vTranslate.x = (getScreenWidth()/2) - (scale * sRequestedCenter.x);
								vTranslate.y = (getScreenHeight()/2) - (scale * sRequestedCenter.y);
							} else {
								// With no requested center, scale around the image center.
								vTranslate.x = (getScreenWidth()/2) - (scale * (sWidth()/2));
								vTranslate.y = (getScreenHeight()/2) - (scale * (sHeight()/2));
							}
						}
						
						//nono fitToBounds(true,true);
						//fitToBounds(false,false);
						
						refreshRequiredTiles(Math.abs(quickScaleLastDistance-dist)<10);
						
						quickScaleLastDistance = dist;
						//refreshRequiredTiles(eagerLoadingEnabled);
						
						consumed = true;
					}
					else if (!isZooming) {
						// One finger pan - translate the image. We do this calculation even with pan disabled so click
						// and long click behaviour is preserved.
						
						float offset = density * 15;
						if (isPanning || Math.abs(view_pager_toguard.lastX-view_pager_toguard_StartX) > offset || Math.abs(event.getY() - vCenterStart.y) > offset) {
							isPanning =
							consumed = true;
							
							float dxRaw = view_pager_toguard.lastX - view_pager_toguard_lastX;
							float dyRaw = view_pager_toguard.lastY - view_pager_toguard_lastY;
							// Using negative angle cos and sin
							float dxR = (float) (dxRaw * cos - dyRaw * -sin);
							float dyR = (float) (dxRaw * -sin + dyRaw * cos);
							
							if(!IBC.lockX) {
								vTranslate.x = vTranslate.x + dxR;
							}
							vTranslate.y = vTranslate.y + dyR;
							//vTranslate.x = vTranslateStart.x + (event.getX() - vCenterStart.x);
							//vTranslate.y = vTranslateStart.y + (event.getY() - vCenterStart.y);

							refreshRequiredTiles(eagerLoadingEnabled);
						}
					}
				}
				
				if(!isRotating){
					vtParms_sew = getScreenWidth();
					vtParms_seh = getScreenExifHeight();
					vtParms_se_delta = (getScreenWidth() - getScreenExifWidth())/2;
					vtParms_se_SWidth = exifSWidth();
					
					double r = rotation % doublePI;
					vtParms_b1=false;vtParms_b2=false;vtParms_b3=false;
					if(!(vtParms_b1=r>halfPI*2.5 && r<halfPI*3.5))
						vtParms_b2=r>halfPI*0.5 && r<halfPI*1.5;
					vtParms_b3 = vtParms_b1 || vtParms_b2;
					vtParms_dir = vtParms_b3?-1:1;
					vtParms_dragDir = 1;
					if(vtParms_b2 || r>halfPI*1.5&&r<halfPI*2.5){
						vtParms_dragDir = -1;
					}
					vtParms_cos = cos;
					vtParms_sin = sin;
				}
				
				//if(false)
				if(!isQuickScaling) {
					float delta_parent = view_pager_toguard.lastX - view_pager_toguard_lastX;
					PointF resultAfter = new PointF();
					sourceToViewCoord(centerAt, resultAfter);
					if(touchCount>1){
						//delta_parent = resultAfter.x - result.x;
					}
					
					float afterXCoord = vtParms_b3?vTranslate.y:vTranslate.x;
					float scaleWidth = scale * vtParms_se_SWidth;
					float minX = vtParms_sew - scaleWidth;
					float maxX = 0;
					boolean bOverScrollable=false;
					if(minX>=0) {
						minX=minX/2;
						maxX=minX;
					}
					else {
						bOverScrollable = true;
						//minX= -se_delta*dir;
						//maxX = se_delta*dir;
					}
					minX += vtParms_se_delta*vtParms_dir;
					maxX += vtParms_se_delta*vtParms_dir;
					//minX += se_delta*dir;
					//CMN.Log("minX, maxX", minX, maxX, vtParms_b1, vtParms_b2, vtParms_sew - scaleWidth, "screen size:", getScreenWidth() , getScreenExifWidth());
					float mold =  ((ViewGroup)getParent()).getLeft() - view_pager_toguard.getScrollX();
					if(bOverScrollable && isDragging){
						//if(mold>100) dragOnRight=true;
						//else if(mold<-100) dragOnRight=false;
						//CMN.Log(preXCoord, minX, maxX);
//						if(touchCount==1){
//							dragOnRight = Math.abs(preXCoord-minX)>=Math.abs(preXCoord-maxX);
//						}
						if(mold>0 && dragOnRight) {
							if(vtParms_dragDir==-1) maxX=minX;
							else minX=maxX=vtParms_se_delta*vtParms_dir;
						}
						else if(mold<0 && !dragOnRight) {
							if(vtParms_dragDir==-1) minX=maxX;
							else maxX=minX;
						}
					}
					
					float compensationL=0;//<--
					float compensationR=0;//-->
					if(afterXCoord<minX){
						compensationL=minX-afterXCoord;
						if(vtParms_b3) vTranslate.y=minX;
						else vTranslate.x=minX;
					}
					if(afterXCoord>maxX){
						compensationR=afterXCoord-maxX;
						if(vtParms_b3) vTranslate.y=maxX;
						else vTranslate.x=maxX;
					}
					
					float compensation = afterXCoord - (vtParms_b3?vTranslate.y:vTranslate.x);
					//CMN.Log("compensation?", compensation, compensationL, compensationR,"dragDir", vtParms_dragDir, "minX, maxX", minX, maxX, afterXCoord, "mLastMotionX"+view_pager_toguard.mLastMotionX, "mold", mold, " dragOnRight:"+dragOnRight, "se_delta:"+vtParms_se_delta);
					
					
					boolean endFakeDrag=false, startDrag=false;
					if(isDragging){
						//if(touchCount>1&&Math.abs(view_pager_toguard.mLastMotionX)<quickScaleThreshold){
							endFakeDrag = true;
						//} else {
							//mold = view_pager_toguard.mBaseMotionX + view_pager_toguard.mLastMotionX;
							
							//mold =  view_pager_toguard.mBaseMotionX==0?view_pager_toguard.mLastMotionX:((ViewGroup)getParent()).getLeft();
							//mold =  ((ViewGroup)getParent()).getLeft() - view_pager_toguard.getScrollX();
						
							float referencePoint = preXCoord;
							if(compensation!=0) referencePoint+=compensation;
							//CMN.Log(" mold:", mold, " refer:", referencePoint/*, " VT.start:", vTranslateStart.x*//*, " dxRaw:", view_pager_toguard.lastX - view_pager_toguard_StartX*/,
							//		" compensation:", compensation, " leftAndScroll:", ((ViewGroup)getParent()).getLeft(), view_pager_toguard.getScrollX(),
							//		" VT.x:", vTranslate.x+delta_parent, " minmax:", minX, maxX, " dragOnRight:", dragOnRight);
							//mold = 0;
						
							boolean nxt_in_left_frame = viewToSourceX(-mold, referencePoint) >= 0;
							boolean nxt_in_right_frame = viewToSourceX(getScreenWidth()-mold, referencePoint) <= getSWidth();
						
							if(vtParms_b3){
								preYCoord += compensation;
								preXCoord = vTranslate.x;
							} else {
								preXCoord += compensation;
								preYCoord = vTranslate.y;
							}
							
							nxt_in_left_frame = viewToSourceFrameX(-mold*vtParms_dragDir, getScreenHeight()/2, preXCoord, preYCoord, vtParms_b3) >= 0;
							nxt_in_right_frame = viewToSourceFrameX(getScreenWidth()-mold*vtParms_dragDir, getScreenHeight()/2, preXCoord, preYCoord, vtParms_b3) <= (vtParms_b3?getSHeight():getSWidth());
							
							
							endFakeDrag = isDragging
									//&&view_pager_toguard.getScrollX()%getScreenWidth()==0
							//&&(nxt_in_left_frame&&nxt_in_right_frame)
									&& viewFramesToSourceInSource(-mold, preXCoord, preYCoord, vtParms_b3)
							//&&compensation==0
//								&&((!nxt_out_of_left_frame||delta3<0)
//								&&(!nxt_out_of_right_frame||delta3>0))
							//&&(out_of_left_frame&&delta3<0||out_of_right_frame&&delta3>0)
							;
						//}
						if(endFakeDrag){
							CMN.Log("endFakeDrag1", nxt_in_left_frame, nxt_in_right_frame, "referencePoint", preXCoord, preYCoord);
							CMN.Log("endFakeDrag111 v_0", viewToSourceFrameX(-mold*vtParms_dragDir, getScreenHeight()/2, preXCoord, preYCoord, vtParms_b3));
							CMN.Log("endFakeDrag111 v_w", viewToSourceFrameX(getScreenWidth()-mold*vtParms_dragDir, getScreenHeight()/2, preXCoord, preYCoord, vtParms_b3));
							
						}
					}
					else if(touchCount==1){
						float dragDirRef = vtParms_b3?compensationR:compensationL;
						dragOnRight = vtParms_dragDir==1?compensationR>0:compensationL>0;
						startDrag=(compensationL>0||compensationR>0)
						;//touchCount==1&&
					}
					
					
					if(isDragging){
						//lastCompensation-compensation
						lastCompensation = compensation;
						if(endFakeDrag){
							CMN.Log("endFakeDrag1");
							view_pager_toguard.skipScroll=2;
							view_pager_toguard.endFakeDrag();
							view_pager_toguard.skipScroll=0;
							//vTranslateStart.x-=(((ViewGroup)getParent()).getLeft() - view_pager_toguard.getScrollX());
							//vTranslate.x=afterXCoord;
							//vTranslate.x=preXCoord;
							//vTranslate.x+=compensation;
						} else { //(isRotating?0.02f:1)*
							view_pager_toguard.fakeDragBy(compensation*vtParms_dragDir, touchCount==1);//
							//vTranslate.x=preXCoord;
//							scaleWidth = scale * exifSWidth();
//							minX = sew-scaleWidth+se_delta;
//							maxX = 0;
//							if(minX>0) maxX=minX=minX/2;
//							else maxX = -se_delta;
							minX = vtParms_sew - scaleWidth;
							maxX = 0;
							bOverScrollable=false;
							if(minX>=0) {
								minX=minX/2;
								maxX=minX;
							}
							else {
								bOverScrollable = true;
								//minX= -se_delta*dir;
								//maxX = se_delta*dir;
							}
							minX += vtParms_se_delta*vtParms_dir;
							maxX += vtParms_se_delta*vtParms_dir;
							
							dragOnRight = Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?minX:maxX))>=Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?maxX:minX));
							
							//CMN.Log("minX, maxX", minX, maxX,"dragDir", vtParms_dragDir, "vx, vy", vTranslate.x, vTranslate.y, "A>?B", Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?minX:maxX)), Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?maxX:minX)), " ! ! !dragOnRight : : :"+dragOnRight);
						}
					} else if(startDrag){
						CMN.Log("startDrag", "mLastMotionX", view_pager_toguard.mLastMotionX);
						view_pager_toguard.beginFakeDrag();
						//view_pager_toguard.fakeDragBy(0);
						//view_pager_toguard.fakeDragBy(0);
						//vTranslateStart.x+=compensation;
					}
					
					//CMN.Log("drag on right ::: ", Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?minX:maxX))>=Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?maxX:minX)));
					//if(false)
					if(!isRotating && touchCount==1 && !startTouchWithAnimation){
						float scaleHeight = scale * sHeight;
						scaleWidth = scale * sWidth;
						float screenHeight = getScreenHeight();
						
						if(vtParms_dir==1){
							float minY = screenHeight-scaleHeight;
							float maxY = 0;
							if(minY>0) minY=maxY=minY/2;
							
							if(vTranslate.y<minY) vTranslate.y=minY;
							else if(vTranslate.y>maxY) vTranslate.y=maxY;
						} else {
							minX = screenHeight-scaleWidth;
							maxX = 0;
							if(minX>0) maxX=minX=minX/2;
							minX -= vtParms_se_delta*vtParms_dir;
							maxX -= vtParms_se_delta*vtParms_dir;
							CMN.Log("minX, maxX", minX, maxX);
							if(vTranslate.x<minX) vTranslate.x=minX;
							else if(vTranslate.x>maxX) vTranslate.x=maxX;
						}
					}
					
				}
				
				view_pager_toguard_lastX = view_pager_toguard.lastX;
				view_pager_toguard_lastY = view_pager_toguard.lastY;
				
				handle_proxy_view(view_to_paint, scaleStamp, tranlationStamp, rotationStamp);
				handle_proxy_simul(scaleStamp,tranlationStamp,rotationStamp);
				if (consumed) {
					handler.removeMessages(MESSAGE_LONG_CLICK);
					invalidate();
					return true;
				}
			} break;
			case MotionEvent.ACTION_UP:
				isDown = false;
			case MotionEvent.ACTION_POINTER_UP:{
				//final float vX = (float) (velocityX * cos - velocityY * -sin);
				//final float vY = (float) (velocityX * -sin + velocityY * cos);
				//touch_partisheet.remove(touch_id);
				//if(is_classical_pan_shrinking) {
				//	if(touch_partisheet.contains(touch_id))
				//		touch_partisheet.clear();
				//}else {
				//	touch_partisheet.remove(touch_id);
				//}
				
				touch_partisheet.clear();
				waitingNextTouchResume = true;
				
				if(view_pager_toguard.isFakeDragging()){
					//CMN.Log("endFakeDrag2");
					//view_pager_toguard.skipScroll=touchCount>1?1:0;
					view_pager_toguard.skipScroll=0;
					view_pager_toguard.endFakeDrag();
					//view_pager_toguard.skipScroll=0;
				}
				
				if(!(doubleTapDetected && !quickScaleMoved)){
					float toRotation = SnapRotation(rotation);
					boolean shouldAnimate = toRotation!=rotation;
					float toScale = currentMinScale();
					
					tmpCenter.set(getScreenWidth()/2, getScreenHeight()/2);
					
					boolean resetScale=false;
					if(scale>toScale) {
						toScale=scale;
						if(scale>maxScale){
							toScale=maxScale;
							shouldAnimate = true;
						}
					} else if(scale<toScale){
						resetScale = shouldAnimate = true;
						tmpCenter.set(getSWidth() / 2, getSHeight() / 2);
					}
					
					strTemp.scale = scale;
					strTemp.vTranslate.set(vTranslate);
					strTemp.rotate = rotation;
					fitToBounds_internal2(true, strTemp);
					if(strTemp.vTranslate.x!=vTranslate.x || strTemp.vTranslate.y!=vTranslate.y){
						if (!resetScale) {
							centerToSourceCoord(tmpCenter, strTemp.vTranslate);
						}
						CMN.Log("strTemp.vTranslate.x!=vTranslate.x || strTemp.vTranslate.y!=vTranslate.y", strTemp.vTranslate.x, vTranslate.x, strTemp.vTranslate.y, vTranslate.y);
						shouldAnimate = true;
					} else if (!resetScale) {
						centerToSourceCoord(tmpCenter, vTranslate);
					}
					
					if(shouldAnimate) {
						computeQuickZoomLevels();
						//Take after everything.
						new AnimationBuilder(toScale, tmpCenter, toRotation)
								.withEasing(EASE_OUT_QUAD).withPanLimited(false)
								.withOrigin(ORIGIN_ANIM)
								.withInterruptible(!quickScaleMoved)
								.withDuration(250)
								.start();
					}
				}
				
				handler.removeMessages(MESSAGE_LONG_CLICK);
				if (isQuickScaling) {
					isQuickScaling=false;
					if (!quickScaleMoved) {
						doubleTapZoom(quickScaleSCenter/*, vCenterStart*/);
					}
				}
				
				if (maxTouchCount > 0 && (isZooming || isPanning) || touch_partisheet.size()==1) {
					if (isZooming && touchCount == 2 || touch_partisheet.size()==1) {
						// Convert from zoom to pan with remaining touch
//						isPanning = true;
//						vTranslateStart.set(vTranslate.x, vTranslate.y);
//						if(!is_classical_pan_shrinking) {
//							for(int i=0;i<touchCount;i++) {
//								if(touch_partisheet.contains(event.getPointerId(i))) {
//									vCenterStart.set(event.getX(i), event.getY(i));
//									break;
//								}
//							}
//						}
					}
					if (touchCount < 3) {
						// End zooming when only one touch point
						isZooming = false;
					}
					if (touchCount < 2) {
						// End panning when no touch points
						isPanning = false;
						isRotating = false;
						maxTouchCount = 0;
					}
					// Trigger load of tiles now required
					refreshRequiredTiles(true);
					return true;
				}
				if (touchCount == 1) {
					isZooming = false;
					isPanning = false;
					maxTouchCount = 0;
				}
			} return true;
		}
		return false;
	}
	
	double halfPI = Math.PI / 2;
	double doublePI = Math.PI * 2;
	private float SnapRotation(float rotation) {
		while(rotation>doublePI){
			rotation-=doublePI;
		}
		if(rotation>halfPI*3.5) rotation= (float) doublePI;
		else if(rotation>halfPI*2.5) rotation= (float) (halfPI*3);
		else if(rotation>halfPI*1.5) rotation= (float) (Math.PI);
		else if(rotation>halfPI*0.5) rotation= (float) (halfPI*1);
		else rotation= 0;
		return rotation;
	}
	
	private void handle_proxy_view(View proxy_view, float scaleStamp, PointF translationStamp, float rotationStamp) {
		if(proxy_view==null) return;
		if(rotation!=rotationStamp)
			proxy_view.setRotation((float) (rotation/Math.PI*180));
		if(scale!=scaleStamp){
			proxy_view.setScaleX(scale/getMinScale());
			proxy_view.setScaleY(scale/getMinScale());
		}
		if(scale!=scaleStamp || rotation!=rotationStamp || translationStamp==null || !translationStamp.equals(vTranslate)){
			float deltaX = scale*sWidth/2 +vTranslate.x-getScreenWidth()*1.0f/2;
			float deltaY = scale*sHeight/2+vTranslate.y-getScreenHeight()*1.0f/2;
			PointF vTranslateDelta = new PointF();
			vTranslateDelta.x = (float) (deltaX * cos + deltaY * -sin - deltaX);
			vTranslateDelta.y = (float) (deltaX * sin + deltaY * cos - deltaY);
			vTranslateOrg.x = getScreenWidth()*1.0f/2-scale*sWidth/2;
			vTranslateOrg.y = getScreenHeight()*1.0f/2-scale*sHeight/2;
			float targetTransX = vTranslate.x - vTranslateOrg.x + vTranslateDelta.x;
			float targetTransY = vTranslate.y - vTranslateOrg.y + vTranslateDelta.y;
			if(proxy_view.getTranslationX()!=targetTransX)proxy_view.setTranslationX(targetTransX);
			if(proxy_view.getTranslationY()!=targetTransY)proxy_view.setTranslationY(targetTransY);
		}
		proxy_view.forceLayout();
	}
	
	private void handle_proxy_simul(float scaleStamp, PointF translationStamp, float rotationStamp) {
		if (view_to_guard != null) {
			if(false) {
				view_to_guard.setScaleType(ImageView.ScaleType.MATRIX);
				Matrix mat = new Matrix();
				mat.postScale(scale / getMinScale(), scale / getMinScale());
				//mat.postRotate(getRequiredRotation());
				mat.postTranslate(vTranslate.x, vTranslate.y);
				//mat.postRotate((float) Math.toDegrees(rotation), getScreenWidth() / 2, getScreenHeight() / 2);
				//re-paint
				view_to_guard.setImageMatrix(mat);
				view_to_guard.invalidate();
			}else{
				if(rotation!=rotationStamp)
					view_to_guard.setRotation((float) ((rotation)/Math.PI*180)-sOrientation);
				if(scale!=scaleStamp){
					view_to_guard.setScaleX(scale/getMinScale());
					view_to_guard.setScaleY(scale/getMinScale());
				}
				if(scale!=scaleStamp || rotation!=rotationStamp || translationStamp==null || !translationStamp.equals(vTranslate)){
					float deltaX = scale*sWidth/2 +vTranslate.x-getScreenWidth()*1.0f/2;
					float deltaY = scale*sHeight/2+vTranslate.y-getScreenHeight()*1.0f/2;
					PointF vTranslateDelta = new PointF();
					vTranslateDelta.x = (float) (deltaX * cos + deltaY * -sin - deltaX);
					vTranslateDelta.y = (float) (deltaX * sin + deltaY * cos - deltaY);
					vTranslateOrg.x = getScreenWidth()*1.0f/2-scale*sWidth/2;
					vTranslateOrg.y = getScreenHeight()*1.0f/2-scale*sHeight/2;
					float targetTransX = vTranslate.x - vTranslateOrg.x + vTranslateDelta.x;
					float targetTransY = vTranslate.y - vTranslateOrg.y + vTranslateDelta.y;
					view_to_guard.setTranslationX(targetTransX);
					view_to_guard.setTranslationY(targetTransY);
				}
			}
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}
	
	private void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		ViewParent parent = getParent();
		if (parent != null) {
			parent.requestDisallowInterceptTouchEvent(disallowIntercept);
		}
	}
	
	/** Compute Dynamic Quick Double Tap Zoom Levels. see {@link #currentMinScale} */
	private void computeQuickZoomLevels() {
		int vPadding = getPaddingBottom() + getPaddingTop();
		int hPadding = getPaddingLeft() + getPaddingRight();
		int sw = sWidth;
		int sh = sHeight;
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5){
			sw = sHeight;
			sh = sWidth;
		}
		float scaleMin1 = (getScreenWidth() - hPadding) / (float) sw;
		float scaleMin2 = (getScreenHeight() - vPadding) / (float) sh;
		float MaxTol = 10;
		if(IBC.doubleClickZoomLevel1>1){
			/* User Levels */
			UseUserLevels = true;
			float scaleMin = Math.min(scaleMin1, scaleMin2);
			quickZoomLevels[0] = scaleMin;
			quickZoomLevels[1] = scaleMin*IBC.doubleClickZoomLevel1;
			if(IBC.doubleClickZoomLevel2>IBC.doubleClickZoomLevel1){
				quickZoomLevels[2] = scaleMin*IBC.doubleClickZoomLevel2;
				maxScale = quickZoomLevels[2]*MaxTol;
				quickZoomLevelCount = 3;
			} else {
				maxScale = quickZoomLevels[1]*MaxTol;
				quickZoomLevelCount = 2;
			}
		} else {
			/* Auto Levels */
			UseUserLevels = false;
			float zoomInLevel = 2.5f;
			float level2;
			if(scaleMin1<scaleMin2){
				quickZoomLevels[0] = scaleMin1;
				level2 = scaleMin2;
			} else {
				quickZoomLevels[0] = scaleMin2;
				level2 = scaleMin1;
			}
			if(level2<zoomInLevel*quickZoomLevels[0]){
				quickZoomLevels[1] = level2*zoomInLevel;//1.2f*zoomInLevel*quickZoomLevels[0];
				quickZoomLevelCount = 2;
				maxScale = quickZoomLevels[1]*MaxTol;
			} else {
				quickZoomLevels[1] = level2;
				quickZoomLevels[2] = level2*zoomInLevel;
				quickZoomLevelCount = 3;
				maxScale = quickZoomLevels[2]*MaxTol;
			}
		}
	}
	
	/**
	 * Double tap zoom handler triggered from gesture detector or on touch, depending on whether
	 * quick scale is enabled.
	 */
	private void doubleTapZoom(PointF sCenter) {
		float scaleMin = currentMinScale();
		float targetScale;
		float padding = 0.01f;
		float currentScale = scale;
		computeQuickZoomLevels();
		if(anim!=null){
			if(anim.origin == ORIGIN_DOUBLE_TAP_ZOOM){
				currentScale = anim.scaleEnd;
			}
			anim=null;
		}
		
		if(quickZoomLevelCount==2){
			CMN.Log("两级放大");
			targetScale = currentScale >= quickZoomLevels[1]?quickZoomLevels[0]:quickZoomLevels[1];
		} else {
			if(currentScale <= quickZoomLevels[1]-padding){
				targetScale = quickZoomLevels[1];
			} else if(currentScale <= quickZoomLevels[2]-padding){
				targetScale = quickZoomLevels[2];
			} else {
				if(IBC.getDoubleClick12()){
					targetScale = quickZoomLevels[1];
				} else {
					targetScale = quickZoomLevels[0];
				}
			}
		}
		
		new AnimationBuilder(targetScale, sCenter)
				.withInterruptible(false)
				.withDuration(250) //doubleTapZoomDuration
				.withOrigin(ORIGIN_DOUBLE_TAP_ZOOM)
				.start();
	}
	
	/** Count number of set bits */
	static int CountBits(int w)
	{
		int e = 0;
		while (w > 0)
		{
			w &= w - 1;
			e++;
		}
		return e;
	}
	
	/** n === 2^ret */
	static int log2(int n)
	{
		return CountBits(n-1);
	}
	
	/**
	 * Draw method should not be called until the view has dimensions so the first calls are used as triggers to calculate
	 * the scaling and tiling required. Once the view is setup, tiles are displayed as they are loaded.
	 */
	@SuppressLint("DefaultLocale")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int cc=0,sampleSize=-1;
		// If image or view dimensions are not known yet, abort.
		int mScreenWidth = getScreenWidth();
		int mScreenHeight = getScreenHeight();
		boolean hasMissingTiles = false;
		if (sWidth == 0 || sHeight == 0 || mScreenWidth == 0 || mScreenHeight == 0 ) {
			preDraw();
			handle_animation();
			//return;
		}
		else{
			// When using tiles, on first render with no tile map ready, initialise it and kick off async base image loading.
			if (tileMipMaps == null && decoder != null) {
				initialiseBaseLayer(getMaxBitmapDimensions(canvas));
			}
			
			// If image has been loaded or supplied as a bitmap, onDraw may be the first time the view has
			// dimensions and therefore the first opportunity to set scale and translate. If this call returns
			// false there is nothing to be drawn so return immediately.
			if (!isProxy && !checkReady()) {
				return;
			}
			
			// Set scale and translate before draw.
			//preDraw();
			
			// If animating scale, calculate current scale and center with easing equations
			if (anim != null)
				handle_animation();
			if (tileMipMaps != null && readySent) {
				// Optimum sample size for current scale
				sampleSize = calcSampleSize(scale);
				if(sampleSize>fullImageSampleSize) sampleSize=fullImageSampleSize;
				// First check for missing tiles - if there are any we need the base layer underneath to avoid gaps
				int currentLayerId = log2(sampleSize);
				int layerSize = tileMipMaps.length;
				if(currentLayerId<layerSize){
					Tile[] tatgetArr = tileMipMaps[currentLayerId].tiles;//.get(sampleSize);
					for (Tile tile : tatgetArr) {
						if (tile.visible && tile.bitmap == null) {
							hasMissingTiles = true;
							break;
						}
					}
				}
				//CMN.Log("currentLayerId", currentLayerId, tileMipMaps[currentLayerId].sampleSize, tileMipMaps[currentLayerId].tiles.length==0?-1:tileMipMaps[currentLayerId].tiles[0].sampleSize);
				
				if(hasMissingTiles){
					// coarser layers
					for (int layerId = currentLayerId+1; layerId < layerSize; layerId++) {
						TileMap coarserLayer = tileMipMaps[layerId];
						coarserLayer.patchTiles.clear();
						//iterate over all tiles in this coarse layer
						for (int tileId = 0; tileId < coarserLayer.tiles.length; tileId++) {
							Tile coarserTileI = coarserLayer.tiles[tileId];
							//CMN.Log("draw patches??????draw patches??????", coarserTileI.sampleSize, sampleSize);
							//矩形按层级碰撞
							if(coarserTileI.visible){
								//CMN.Log("draw patches??????", coarserTileI.sampleSize, sampleSize);
								int x1 = coarserTileI.sRect.left;
								int y1 = coarserTileI.sRect.top;
								int x3 = coarserTileI.sRect.right;
								int y3 = coarserTileI.sRect.bottom;
								boolean drawAsPatch = true;
								for (int lastLayerId = layerId-1; lastLayerId >= currentLayerId; lastLayerId--) {
									TileMap lastLayer = tileMipMaps[lastLayerId];
									Tile[] lastLayerTiles = lastLayer.tiles;
									
									int v1 = x1/lastLayer.sTileWidth;
									int v2 = y1/lastLayer.sTileHeight;
									int v3 = x3/lastLayer.sTileWidth;
									int v4 = y3/lastLayer.sTileHeight;
									if(lastLayerId==currentLayerId){
										boolean draw = lastLayerTiles[v2*lastLayer.xTiles+v1].missing()
												|| lastLayerTiles[Math.min(v4*lastLayer.xTiles+v3, lastLayerTiles.length-1)].missing()
												|| lastLayerTiles[Math.min(v4*lastLayer.xTiles+v1, lastLayerTiles.length-1)].missing()
												|| lastLayerTiles[Math.min(v2*lastLayer.xTiles+v3, lastLayerTiles.length-1)].missing()
												;
										if(!draw){
											drawAsPatch = false;
											break;
										}
									} else {
										boolean draw = lastLayer.missing(v2*lastLayer.xTiles+v1)
												|| lastLayer.missing(v4*lastLayer.xTiles+v3)
												|| lastLayer.missing(v4*lastLayer.xTiles+v1)
												|| lastLayer.missing(v2*lastLayer.xTiles+v3)
												;
										if(!draw){
											drawAsPatch = false;
											break;
										}
									}
								}
								//drawAsPatch = true;
								if(drawAsPatch){
									//CMN.Log("draw patches???", coarserTileI.sampleSize, sampleSize);
									coarserTileI.patchId = coarserLayer.patchTiles.size();
									coarserLayer.patchTiles.add(coarserTileI);
								}
							}
						}
					}
				}
				//hasMissingTiles = false;
				
//
				// Render all loaded tiles. LinkedHashMap used for bottom up rendering - lower res tiles underneath.
				
				if(!paint_to_grid){
					long timeStart = System.currentTimeMillis();
					float degree = (float) Math.toDegrees(rotation);
					mScreenWidth/=2;
					mScreenHeight/=2;
					int drawAreaCount=0;
					//策略：优先绘制当前层已存在的，若缺，则先从高采样层补，再从低采样层补。
					//for (Map.Entry<Integer, List<Tile>> tileMapEntry : entrys) {
					for (int i = 0; i < tileMipMaps.length; i++) {
						TileMap layerI = tileMipMaps[tileMipMaps.length-1-i];
						boolean isCurrent = layerI.sampleSize == sampleSize;
						if (hasMissingTiles ||isCurrent) {//
							Tile[] TilesToDraw = isCurrent?layerI.tiles:layerI.patchTiles.elementData;
							int tileSize = isCurrent?layerI.tiles.length:layerI.patchTiles.size();
							for (int j = 0; j < tileSize; j++) {
								Tile tile = TilesToDraw[j];
								if (tile.visible && tile.bitmap != null) {//
									if(!isCurrent){
										//CMN.Log("draw patches!!!", tile.sampleSize, sampleSize);
									}
									Bitmap bm = tile.bitmap;
									Rect VR = tile.vRect;
									sourceToViewRect(tile.sRect, VR);
									if (tileBgPaint != null) {
										canvas.drawRect(VR, tileBgPaint);
									}
									matrix.reset();
									int bmWidth = bm.getWidth();
									int bmHeight = bm.getHeight();
//									if(drawAreaCount+bmWidth*bmHeight>6*1800*1800){
//										continue;
//									}
//									drawAreaCount+=bmWidth*bmHeight;
									setMatrixArray(srcArray, 0, 0,bmWidth , 0, bmWidth, bmHeight, 0, bmHeight);
									setMatrixArray(dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);

									matrix.setPolyToPoly(srcArray, 0, dstArray, 0, 4);
									matrix.postRotate(degree, mScreenWidth, mScreenHeight);
									canvas.drawBitmap(bm, matrix, bitmapPaint);
									cc++;
									//samplesize=tile.sampleSize;
								}
								if (SSVD && SSVDF2 && layerI.sampleSize == sampleSize) {
									//fff
									//canvas.drawRect(tile.vRect, debugLinePaint);
									int y=0;
									canvas.drawText("ISS_" + tile.sampleSize + "@" + tile.sRect.left + "," + tile.sRect.top + "," + tile.sRect.right + "," + tile.sRect.bottom, tile.vRect.left + px(5), tile.vRect.top + px(15+y), debugTextPaint);
									if (tile.visible && tile.bitmap!=null) {
										y+=35;
										canvas.drawText("bitmap："+tile.bitmap.getWidth()+"x"+tile.bitmap.getHeight(), tile.vRect.left + px(5), tile.vRect.top + px(y), debugTextPaint);
									}
									if (tile.loading || tile.bitmap==null) {
										y+=35;
										canvas.drawText("LOADING", tile.vRect.left + px(5), tile.vRect.top + px(y), debugTextPaint);
									}
								}
							}
						}
					}
					//CMN.Log("绘制个数：", cc);
					lastDrawTime = System.currentTimeMillis()-timeStart;
				}
			}
		}
		
		if(SSVD) {
			//debugTextPaint.setColor(0xff6666ff);
			int y=15;
			int x=px(5);
			canvas.drawText(String.format("Scale: %.4f (%.4f-%.4f) drawTime:%d", scale, minScale(), maxScale, lastDrawTime), px(5), px(y), debugTextPaint);y+=15;
			canvas.drawText(String.format("Translate: [%.2f:%.2f]", vTranslate.x, vTranslate.y), x, px(y), debugTextPaint);y+=15;
			
			PointF center = getCenter();
			canvas.drawText(String.format("Source center: %.2f:%.2f", center.x, center.y), px(5), px(y), debugTextPaint);y+=15;
			
			canvas.drawText(String.format("[SampleSize:%d] [cc:%d] [scale:%.2f] [preview:%d]",sampleSize, cc, scale, maxImageSampleSize), x, px(y), debugTextPaint);y+=15;
			
			//if(imgsrc!=null)canvas.drawText("path = " + imgsrc.substring(imgsrc.lastIndexOf("/")+1), x, px(y), debugTextPaint);y+=15;
			
			canvas.drawText(String.format("size = [%dx%d] [%dx%d]", getScreenWidth(), getScreenHeight(), sWidth, sHeight), x, px(y), debugTextPaint);y+=15;
			
			if (hasMissingTiles) canvas.drawText(String.format("(Missing Tiles)"), x, px(y), debugTextPaint);y+=15;
			//if(center!=null)canvas.drawText("Source center2: " + String.format("%.2f", scale*sWidth/2) + ":" + String.format("%.2f", scale*sHeight/2), x, y, debugTextPaint);y+=15;
			
			debugTextPaint.setColor(Color.MAGENTA);
			
//			if (anim != null) {
//				PointF vCenterStart = sourceToViewCoord(anim.sCenterStart);
//				PointF vCenterEndRequested = sourceToViewCoord(anim.sCenterEndRequested);
//				PointF vCenterEnd = sourceToViewCoord(anim.sCenterEnd);
//
//				if(vCenterStart!=null)canvas.drawCircle(vCenterStart.x, vCenterStart.y, px(10), debugLinePaint);
//				debugLinePaint.setColor(Color.RED);
//
//				if(vCenterEndRequested!=null)canvas.drawCircle(vCenterEndRequested.x, vCenterEndRequested.y, px(20), debugLinePaint);
//				debugLinePaint.setColor(Color.BLUE);
//
//				if(vCenterEnd!=null)canvas.drawCircle(vCenterEnd.x, vCenterEnd.y, px(25), debugLinePaint);
//			}
			//debugLinePaint.setColor(Color.RED);
			//canvas.drawCircle(vCenterStart.x, vCenterStart.y, px(20), debugLinePaint);
//			if (quickScaleSCenter != null) {
//				debugLinePaint.setColor(Color.BLUE);
//				final PointF quickScaleVCenter = sourceToViewCoord(quickScaleSCenter);
//				if(quickScaleVCenter!=null)canvas.drawCircle(quickScaleVCenter.x, quickScaleVCenter.y, 35, debugLinePaint);
//			}
//			if (quickScaleVStart != null) {
//				debugLinePaint.setColor(Color.CYAN);
//				if(quickScaleVStart!=null)canvas.drawCircle(quickScaleVStart.x, quickScaleVStart.y, 30, debugLinePaint);
//			}
			debugLinePaint.setColor(Color.MAGENTA);
		}
	}
	
	private void handle_animation() {
		//if(true) return;
		if (anim != null && anim.vFocusStart != null) {
			// Store current values so we can send an event if they change
			float scaleBefore = scale;
			float rotationBefore = rotation;
			if (vTranslateBefore == null) { vTranslateBefore = new PointF(0, 0); }
			vTranslateBefore.set(vTranslate);
			
			long scaleElapsed = System.currentTimeMillis() - anim.time;
			boolean finished = scaleElapsed > anim.duration;
			scaleElapsed = Math.min(scaleElapsed, anim.duration);
			scale = ease(anim.easing, scaleElapsed, anim.scaleStart, anim.scaleEnd - anim.scaleStart, anim.duration);
			//Log.e("anim_scalanim", anim.scaleStart+","+(anim.scaleEnd - anim.scaleStart)+","+scale);
			
			// Apply required animation to the focal point
			float vFocusNowX = ease(anim.easing, scaleElapsed, anim.vFocusStart.x, anim.vFocusEnd.x - anim.vFocusStart.x, anim.duration);
			float vFocusNowY = ease(anim.easing, scaleElapsed, anim.vFocusStart.y, anim.vFocusEnd.y - anim.vFocusStart.y, anim.duration);
			
			if (rotationEnabled) {
				float target = ease(anim.easing, scaleElapsed, anim.rotationStart, anim.rotationEnd - anim.rotationStart, anim.duration);
				//Log.e("ani_rotanim", anim.rotationStart+","+(anim.rotationEnd - anim.rotationStart)+","+target);
				setRotationInternal(target);
			}
			
			// Find out where the focal point is at this scale and adjust its position to follow the animation path
			PointF animVCenterEnd = sourceToViewCoord(anim.sCenterEnd);
			final float dX = animVCenterEnd.x - vFocusNowX;
			final float dY = animVCenterEnd.y - vFocusNowY;
			vTranslate.x -= (dX * cos + dY * sin);
			vTranslate.y -= (-dX * sin + dY * cos);
			//vTranslate.x -= sourceToViewX(anim.sCenterEnd.x) - vFocusNowX;
			//vTranslate.y -= sourceToViewY(anim.sCenterEnd.y) - vFocusNowY;
			
			// For translate anims, showing the image non-centered is never allowed, for scaling anims it is during the animation.
			//nono fitToBounds(finished || (anim.scaleStart == anim.scaleEnd),false);
			fitToBounds(false,false);
			
			//sendStateChanged(scaleBefore, vTranslateBefore, rotationBefore, anim.origin);
			refreshRequiredTiles(finished);
			if (finished) {
				//if (anim.listener != null) anim.listener.onComplete();
				anim = null;
			}
			
			handle_proxy_view(view_to_paint, scaleBefore, vTranslateBefore, rotationBefore);
			handle_proxy_simul(scaleBefore, vTranslateBefore, rotationBefore);
			if(!finished){
				if(isProxy) postSimulate();
				else invalidate();
			}
		}
	}
	
	private void postSimulate() {
		post(mAnimationRunnable);
	}
	
	/**
	 * Helper method for setting the values of a tile matrix array.
	 */
	private void setMatrixArray(float[] array, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
		array[0] = f0;
		array[1] = f1;
		array[2] = f2;
		array[3] = f3;
		array[4] = f4;
		array[5] = f5;
		array[6] = f6;
		array[7] = f7;
	}
	
	/**
	 * Checks whether the base layer of tiles or full size bitmap is ready.
	 */
	private boolean isBaseLayerReady() {
		if(true) return true;
//		if (tileMap != null) {
//			boolean baseLayerReady = true;
//			for (Map.Entry<Integer, List<Tile>> tileMapEntry : tileMap.entrySet()) {
//				if (tileMapEntry.getKey() == fullImageSampleSize) {
//					for (Tile tile : tileMapEntry.getValue()) {
//						if (tile.loading || tile.bitmap == null) {
//							baseLayerReady = false;
//						}
//					}
//				}
//			}
//			return baseLayerReady;
//		}
		return false;
	}
	
	/**
	 * Check whether view and image dimensions are known and either a preview, full size image or
	 * base layer tiles are loaded. First time, send ready event to listener. The next draw will
	 * display an image.
	 */
	private boolean checkReady() {
		boolean ready = getScreenWidth() > 0 && getScreenHeight() > 0 && sWidth > 0 && sHeight > 0 && (isBaseLayerReady());
		if (!readySent && ready) {
			preDraw();
			readySent = true;
			//if (onImageEventListener != null) onImageEventListener.onReady();
			// Restart an animation that was waiting for the view to be ready
			if (pendingAnimation != null) {
				pendingAnimation.start();
				pendingAnimation = null;
			}
		}
		return ready;
	}
	
	/**
	 * Check whether either the full size bitmap or base layer tiles are loaded. First time, send image
	 * loaded event to listener.
	 */
	private boolean checkImageLoaded() {
		boolean imageLoaded = isBaseLayerReady();
		if (!imageLoadedSent && imageLoaded) {
			preDraw();
			imageLoadedSent = true;
			onImageLoaded();
			//if (onImageEventListener != null) onImageEventListener.onImageLoaded();
		}
		return imageLoaded;
	}
	
	/**
	 * Creates Paint objects once when first needed.
	 */
	private void createPaints() {
		if (bitmapPaint == null) {
			bitmapPaint = new Paint();
			bitmapPaint.setAntiAlias(false);
			bitmapPaint.setFilterBitmap(true);
			bitmapPaint.setDither(true);
			//bitmapPaint.setColorFilter(sample_fileter);
		}
		if (SSVD) {
			debugTextPaint = new Paint();
			debugTextPaint.setTextSize(px(12));
			debugTextPaint.setColor(Color.MAGENTA);
			debugTextPaint.setStyle(Style.FILL);
			debugLinePaint = new Paint();
			debugLinePaint.setColor(Color.MAGENTA);
			debugLinePaint.setStyle(Style.STROKE);
			debugLinePaint.setStrokeWidth(px(1));
		}
	}
	
	/**
	 * Called on first draw when the view has dimensions. Calculates the initial sample size and starts async loading of
	 * the base layer image - the whole source subsampled as necessary.
	 */
	private synchronized void initialiseBaseLayer(@NonNull Point maxTileDimensions) {
		//fitToBounds_internal(true, strTemp);
		
		// Load double resolution - next level will be split into four tiles and at the center all four are required,
		// so don't bother with tiling until the next level 16 tiles are needed.
		fullImageSampleSize = maxImageSampleSize;//calcSampleSize(strTemp.scale);
//		if (fullImageSampleSize > 1) {
//			fullImageSampleSize /= 2;
//		}
		
		CMN.Log("initialiseBaseLayer", maxTileDimensions, fullImageSampleSize);
		
		if (fullImageSampleSize == 1 && sRegion == null && sWidth() < maxTileDimensions.x && sHeight() < maxTileDimensions.y) {
			
			// Whole image is required at native resolution, and is smaller than the canvas max bitmap size.
			// Use BitmapDecoder for better image support.
			decoder.recycle();
			decoder = null;
			//BitmapLoadTask task = new BitmapLoadTask(this, getContext(), bitmapDecoderFactory, uri, false);
			//execute(task);
			
		} else {
			
			initialiseTileMap(maxTileDimensions);
			
			TileMap baseGrid = tileMipMaps[tileMipMaps.length-1];
			for (Tile baseTile : baseGrid.tiles) {
				TileLoadTask task = new TileLoadTask(this, decoder, baseTile);
				execute(task);
				break;
			}
			refreshRequiredTiles(true);
		}
	}
	
	/**
	 * Loads the optimum tiles for display at the current scale and translate, so the screen can be filled with tiles
	 * that are at least as high resolution as the screen. Frees up bitmaps that are now off the screen.
	 * @param load Whether to load the new tiles needed. Use false while scrolling/panning for performance.
	 */
	private void refreshRequiredTiles(boolean load) {
		if (decoder == null || tileMipMaps == null) { return; }
		
		int sampleSize = Math.min(fullImageSampleSize, calcSampleSize(scale));
		// Load tiles of the correct sample size that are on screen. Discard tiles off screen, and those that are higher
		// resolution than required, or lower res than required but not the base layer, so the base layer is always present.
		//CMN.Log("refreshRequiredTiles\n\nrefreshRequiredTiles");int i=0;
		for (TileMap layerI : tileMipMaps) {
			//CMN.Log("refreshRequiredTiles::", "("+i+")"+"/"+tileMap.entrySet().size(), "sampleSize_0", tileMapEntry.getKey(), "sampleSize", sampleSize, "fullImageSampleSize", fullImageSampleSize);i++;int j=0;
			for (Tile tile : layerI.tiles) {
				//CMN.Log("-->", j+"/"+tileMapEntry.getValue().size(), "sampleSize_"+j, tile.sampleSize
				//		, "tileVisible："+tileVisible(tile), "loading："+tile.loading, "bitmap："+tile.bitmap);j++;
				boolean unload=false;
				if(tile.sampleSize == fullImageSampleSize){
					continue;
				}
				if (tile.sampleSize < sampleSize || (tile.sampleSize > sampleSize && tile.sampleSize != fullImageSampleSize)) {
					//if (tile.sampleSize != sampleSize) {
					tile.visible = false;
					unload = true;
				}
				if (tile.sampleSize == sampleSize) {
					if (tileVisible(tile, 0)) {
						tile.visible = true;
						
						if (!tile.loading && tile.bitmap == null && load
								// && tile.sampleSize==1
						) {
							TileLoadTask task = new TileLoadTask(this, decoder, tile);
							execute(task);
						}
					} else if (tile.sampleSize != fullImageSampleSize) {
						tile.visible = false;
						unload = true;
					}
				}
				else if (tile.sampleSize == fullImageSampleSize) {
					tile.visible = true;
				}
				
				if (unload && tile.bitmap != null) {
					if(
							//tile.sampleSize != sampleSize
							//||
							!tileVisible(tile, 350)
					){
						//!tileVisible(tile, 1200)
						tile.clean();
						//tile.bitmap.recycle();
						//tile.bitmap = null;
					}
				}
				
				tile.visible = tileVisible(tile, 0);
			}
		}
		
	}
	
	/**
	 * Determine whether tile is visible.
	 */
	private boolean tileVisible(Tile tile, int pardon) {
		//if(true) return true;
		int sw = getScreenWidth()+pardon;
		int sh = getScreenHeight()+pardon;
		pardon=-pardon;
		if (this.rotation == 0f) {
			float sVisLeft = viewToSourceX(pardon),
					sVisRight = viewToSourceX(sw),
					sVisTop = viewToSourceY(pardon),
					sVisBottom = viewToSourceY(sh);
			return !(sVisLeft > tile.sRect.right || tile.sRect.left > sVisRight || sVisTop > tile.sRect.bottom || tile.sRect.top > sVisBottom);
		}
		
		PointF[] corners = new PointF[]{
				sourceToViewCoord(tile.sRect.left, tile.sRect.top),
				sourceToViewCoord(tile.sRect.right, tile.sRect.top),
				sourceToViewCoord(tile.sRect.right, tile.sRect.bottom),
				sourceToViewCoord(tile.sRect.left, tile.sRect.bottom),
		};
		
//		for (PointF pointF: corners) {
//			if (pointF == null) {
//				return false;
//			}
//		}
		final double rotation = this.rotation % (Math.PI * 2);
		
		if (rotation < Math.PI / 2) {
			return !(corners[0].y > sh || corners[1].x < pardon
					|| corners[2].y < pardon || corners[3].x > sw);
		} else if (rotation < Math.PI) {
			return !(corners[3].y > sh || corners[0].x < pardon
					|| corners[1].y < pardon || corners[2].x > sw);
		} else if (rotation < Math.PI * 3/2) {
			return !(corners[2].y > sh || corners[3].x < pardon
					|| corners[0].y < pardon || corners[1].x > sw);
		} else {
			return !(corners[1].y > sh || corners[2].x < pardon
					|| corners[3].y < pardon || corners[0].x > sw);
		}
		
	}
	
	/**
	 * Sets scale and translate ready for the next draw.
	 */
	private void preDraw() {
		CMN.Log("preDraw");
		if (getScreenWidth() == 0 || getScreenHeight() == 0 || sWidth <= 0 || sHeight <= 0) {
			return;
		}
		
		// If waiting to translate to new center position, set translate now
		if (sPendingCenter != null && pendingScale > 0) {
			scale = pendingScale;
			CMN.Log("kiam preDraw scale="+scale+"  getWidth="+getScreenWidth()+"  getHeight="+getScreenHeight()+" width="+getWidth());
			vTranslate.x = (getScreenWidth()/2) - (scale * sPendingCenter.x);
			vTranslate.y = (getScreenHeight()/2) - (scale * sPendingCenter.y);
			sPendingCenter = null;
			pendingScale = 0;
			fitToBounds(true,true);
			refreshRequiredTiles(true);
		}
		
		// On first display of base image set up position, and in other cases make sure scale is correct.
		fitToBounds(false,false);
		if(view_to_guard!=null){
			//view_to_guard.setScaleX(scale/getMinScale());
			//view_to_guard.setScaleY(scale/getMinScale());
			//vTranslateOrg.x = getScreenWidth()/2-scale*sWidth/2;
			//vTranslateOrg.y = getScreenHeight()/2-scale*sHeight/2;
			//view_to_guard.setTranslationX(vTranslate.x - vTranslateOrg.x);
			//view_to_guard.setTranslationY(vTranslate.y - vTranslateOrg.y);
		}
	}
	
	public void preDraw2(PointF _sPendingCenter, Float _pendingScale) {
		CMN.Log("fatal","preDraw2");
		
		if (sWidth <= 0 || sHeight <= 0) {
			return;
		}
		//Log.e("fatal poison", "_pendingScale "+_pendingScale);
		
		// If waiting to translate to new center position, set translate now
		if (_sPendingCenter != null && _pendingScale != null) {
			scale = getMinScale();
			vTranslate.x = (getScreenWidth()*1.0f/2) - (scale * _sPendingCenter.x);
			vTranslate.y = (getScreenHeight()*1.0f/2) - (scale * _sPendingCenter.y);
			
			int pzl = IBC.getPresetZoomLevel();
			if(pzl!=0){
				vTranslate.y = 0;
				int pza = IBC.getPresetZoomAlignment();
				if(pzl==1&&IBC.doubleClickZoomLevel1>1){
					scale=scale*IBC.doubleClickZoomLevel1;
					if(pza!=0){
						float offset = dm.widthPixels*IBC.doubleClickPresetXOffset;
						if(pza==1){
							vTranslate.x = -offset;
						} else {
							vTranslate.x = getScreenWidth() - scale*sWidth + offset;
						}
					}
				}
			}
			//pendingScale = scale;
			//sPendingCenter = _sPendingCenter;
			
			CMN.Log("kiam preDraw2", "scale",scale, vTranslate.x);
			
			vTranslateOrg.set(vTranslate);
			//Log.e("fatal poison", ""+getScreenWidth()+" x "+getScreenHeight());
			Log.e("fatal","preDraw2 fitToBounds1");
			fitToBounds(true,true);
			//refreshRequiredTiles(true);
		}
		
		// On first display of base image set up position, and in other cases make sure scale is correct.
		fitToBounds(false,true);
		
		handle_proxy_simul(0,null,rotation);
	}
	
	/**
	 * Calculates sample size to fit the source image in given bounds.
	 */
	private int calcSampleSize(float scale) {
		if (minimumTileDpi > 0) {
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			float averageDpi = (metrics.xdpi + metrics.ydpi)/2;
			float preScale = scale;
			scale = (minimumTileDpi/averageDpi) * scale;
			if(preScale>0.5){
				scale = (220/averageDpi) * preScale;
			}
		}
		
//		float sWidth = sWidth();
//		float sHeight = sHeight();
//		int reqWidth = (int)(sWidth * scale);
//		int reqHeight = (int)(sHeight * scale);
//		if (reqWidth == 0 || reqHeight == 0) return 8;
		
		int inSampleSize = 1;
		
		if (scale<1) {
		
//			// Calculate ratios of height and width to requested height and width
//			final int heightRatio = Math.round(sHeight / (float) reqHeight);
//			final int widthRatio = Math.round(sWidth / (float) reqWidth);
//
//			// Choose the smallest ratio as inSampleSize value, this will guarantee
//			// a final image with both dimensions larger than or equal to the
//			// requested height and width.
//			inSampleSize = Math.min(heightRatio, widthRatio);
//
//			//if(inSampleSize!=Math.round(1/scale)) CMN.Log("真的假的", inSampleSize, Math.round(1/scale), scale);
			
			inSampleSize = (int) (1/scale);
			inSampleSize=Math.round(1/scale);
		}
		
//		// We want the actual sample size that will be used, so round down to nearest power of 2.
//		int power = 1;
//		while (power * 2 < inSampleSize) {
//			power = power * 2;
//		}
//		//if(power>1) power = power / 2;
//		//CMN.Log(inSampleSize, RoundDown_pow2(inSampleSize), power);
		return RoundDown_pow2(inSampleSize);
	}
	
	static int RoundDown_pow2(int cap) {
		int n = cap - 1;
		n >>= 1;
		n |= n >> 1;
		n |= n >> 2;
		n |= n >> 4;
		n |= n >> 8;
		n |= n >> 16;
		return (n < 0) ? 1 : n+1;
	}
	
	
	/**
	 * Adjusts hypothetical future scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
	 * is set so one dimension fills the view and the image is centered on the other dimension. Used to calculate what the target of an
	 * animation should be.
	 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
	 * @param sat The scale we want and the translation we're aiming for. The values are adjusted to be valid.
	 */
	private void fitToBounds_internal2(boolean center, ScaleTranslateRotate sat) {
		//if(true) return;
		// TODO: Rotation
		PointF vTranslate = sat.vTranslate;
		float scale = limitedScale(sat.scale);
		float scaleWidth = scale * sWidth;
		float scaleHeight = scale * sHeight;
		
		float sew = getScreenExifWidth();
		float seh = getScreenExifHeight();
		float se_delta = (getScreenWidth() - sew)/2;
		if (center) {
			//CMN.Log("minHeight=", getScreenExifWidth() - scaleWidth, se_delta);
			vTranslate.x = Math.max(vTranslate.x, sew + se_delta - scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, seh - se_delta - scaleHeight);
		} else {
			vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
		}
		
		// Asymmetric padding adjustments
		float xPaddingRatio = getPaddingLeft() > 0 || getPaddingRight() > 0 ? getPaddingLeft()/(float)(getPaddingLeft() + getPaddingRight()) : 0.5f;
		float yPaddingRatio = getPaddingTop() > 0 || getPaddingBottom() > 0 ? getPaddingTop()/(float)(getPaddingTop() + getPaddingBottom()) : 0.5f;
		
		float maxTx;
		float maxTy;
		if (center) {
			maxTx = Math.max(se_delta, (getScreenWidth() - scaleWidth) * xPaddingRatio);
			maxTy = Math.max(-se_delta, (getScreenHeight() - scaleHeight) * yPaddingRatio);
		} else {
			maxTx = Math.max(0, getScreenWidth());
			maxTy = Math.max(0, getScreenHeight());
		}
		
		vTranslate.x = Math.min(vTranslate.x, maxTx);
		vTranslate.y = Math.min(vTranslate.y, maxTy);
		
		sat.scale = scale;
	}
	
	/**
	 * Adjusts hypothetical future scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
	 * is set so one dimension fills the view and the image is centered on the other dimension. Used to calculate what the target of an
	 * animation should be.
	 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
	 * @param sat The scale we want and the translation we're aiming for. The values are adjusted to be valid.
	 */
	private void fitToBounds_internal(boolean center, ScaleTranslateRotate sat) {
		//if(true) return;
		if(true) {
			fitToBounds_internal2(center, sat);
			return;
		}
		// TODO: Rotation
		PointF vTranslate = sat.vTranslate;
		float scale = limitedScale(sat.scale);
		float scaleWidth = scale * sWidth();
		float scaleHeight = scale * sHeight();
		
		if (center) {
			vTranslate.x = Math.max(vTranslate.x, getScreenWidth() - scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, getScreenHeight() - scaleHeight);
		} else {
			vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
		}
		
		// Asymmetric padding adjustments
		float xPaddingRatio = getPaddingLeft() > 0 || getPaddingRight() > 0 ? getPaddingLeft()/(float)(getPaddingLeft() + getPaddingRight()) : 0.5f;
		float yPaddingRatio = getPaddingTop() > 0 || getPaddingBottom() > 0 ? getPaddingTop()/(float)(getPaddingTop() + getPaddingBottom()) : 0.5f;
		
		float maxTx;
		float maxTy;
		if (center) {
			maxTx = Math.max(0, (getScreenWidth() - scaleWidth) * xPaddingRatio);
			maxTy = Math.max(0, (getScreenHeight() - scaleHeight) * yPaddingRatio);
		} else {
			maxTx = Math.max(0, getScreenWidth());
			maxTy = Math.max(0, getScreenHeight());
		}
		
		vTranslate.x = Math.min(vTranslate.x, maxTx);
		vTranslate.y = Math.min(vTranslate.y, maxTy);
		
		sat.scale = scale;
	}
	
	/**
	 * Adjusts current scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
	 * is set so one dimension fills the view and the image is centered on the other dimension.
	 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
	 */
	private void fitToBounds(boolean center, boolean bFixScale) {
		//CMN.Log("fitToBounds ", bFixScale);
		boolean init = false;
//		if (vTranslate == null) {
//			init = true;
//			vTranslate = new PointF(0, 0);
//		}
		strTemp.scale = scale;
		strTemp.vTranslate.set(vTranslate);
		strTemp.rotate = rotation;
		fitToBounds_internal(center, strTemp);
		if(bFixScale)scale = strTemp.scale;
		vTranslate.set(strTemp.vTranslate);
		setRotationInternal(strTemp.rotate);
		if (init) {
			vTranslate.set(vTranslateForSCenter(sWidth()/2, sHeight()/2, scale));
		}
	}
	
	private void fitTmpToBounds(boolean center) {
		//CMN.Log("fitToBounds ", bFixScale);
		if (strTemp == null) {
			strTemp = new ScaleTranslateRotate(0, new PointF(0, 0), 0);
		}
		strTemp.scale = scale;
		strTemp.vTranslate.set(vTranslate);
		strTemp.rotate = rotation;
		fitToBounds_internal(center, strTemp);
	}
	
	
	private void fitCenter() {
	
	}
	
	/**
	 * Once source image and view dimensions are known, creates a map of sample size to tile grid.
	 */
	private void initialiseTileMap(Point maxTileDimensions) {
		//CMN.Log("initialiseTileMap maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y);
		int sampleSize = fullImageSampleSize;
		int xTiles = 1;
		int yTiles = 1;
		boolean bUseSmallTiles=false;
		ArrayList<TileMap> tileMipMapsArr = new ArrayList<>(10);
		while (sampleSize >= 1) {
			int sTileWidth = sWidth()/xTiles;
			int sTileHeight = sHeight()/yTiles;
			int subTileWidth = sTileWidth/sampleSize;
			int subTileHeight = sTileHeight/sampleSize;
			while (subTileWidth + xTiles + 1 > maxTileDimensions.x || (subTileWidth > getScreenWidth() * 1.25 && sampleSize < fullImageSampleSize)) {
				xTiles += 1;
				sTileWidth = sWidth()/xTiles;
				subTileWidth = sTileWidth/sampleSize;
			}
			while (subTileHeight + yTiles + 1 > maxTileDimensions.y || (subTileHeight > getScreenHeight() * 1.25 && sampleSize < fullImageSampleSize)) {
				yTiles += 1;
				sTileHeight = sHeight()/yTiles;
				subTileHeight = sTileHeight/sampleSize;
			}
			if(!bUseSmallTiles){
				tileMipMapsArr.add(MakeTilesGrid(sampleSize, xTiles, yTiles, sTileWidth, sTileHeight));
			}
			sampleSize >>= 1;
		}
		if(bUseSmallTiles){
			sampleSize = fullImageSampleSize;
			while (sampleSize >= 1) {
				tileMipMapsArr.add(MakeTilesGrid(sampleSize, xTiles, yTiles, sWidth()/xTiles, sHeight()/yTiles));
				sampleSize >>= 1;
			}
		}
		tileMipMaps = new TileMap[tileMipMapsArr.size()];
		for (int i = 0; i < tileMipMaps.length; i++) {
			tileMipMaps[i] = tileMipMapsArr.get(tileMipMaps.length-i-1);
		}
	}
	
	
	private TileMap MakeTilesGrid(int sampleSize, int xTiles, int yTiles, int sTileWidth, int sTileHeight) {
		CMN.Log("MakeTilesGrid", sampleSize, xTiles, yTiles);
		if(maxImageSampleSize<=0 || sampleSize<maxImageSampleSize) {
			Tile[] tileGrid = new Tile[xTiles * yTiles];
			//boolean visible = sampleSize == fullImageSampleSize;
//			if(sampleSize<=0){
//				int factor = 2;
//				xTiles*=factor;
//				yTiles*=factor;
//				sTileWidth/=factor;
//				sTileHeight/=factor;
//			}
			int cc=0;
			for (int x = 0; x < xTiles; x++) {
				for (int y = 0; y < yTiles; y++) {
					Tile tile = new Tile();
					tile.sampleSize = sampleSize;
					tile.visible = false;
					tile.sRect = new Rect(
							x * sTileWidth,
							y * sTileHeight,
							x == xTiles - 1 ? sWidth() : (x + 1) * sTileWidth,
							y == yTiles - 1 ? sHeight() : (y + 1) * sTileHeight
					);
					tile.vRect = new Rect(0, 0, 0, 0);
					tile.fileSRect = new Rect(tile.sRect);
					tileGrid[cc] = tile;
					cc++;
				}
			}
			return new TileMap(tileGrid, sampleSize, xTiles, yTiles, sTileWidth, sTileHeight);
		}
		return new TileMap(new Tile[0], sampleSize, xTiles, yTiles, sTileWidth, sTileHeight);
	}
	
	public void setProxy(int[] dimension, int orientation, Bitmap proxyBm, String path) {
		removeCallbacks(mAnimationRunnable);
		long time = System.currentTimeMillis();
		sWidth = dimension[0];
		sHeight = dimension[1];
		//sOrientation = 0;
		ImgSrc = path;
		sOrientation = getExifOrientation(getContext(), path);
		rotation = (float) Math.toRadians(sOrientation);
		CMN.Log("setProxy getExifOrientation", sOrientation, rotation, path);
		
		isProxy=true;
		scale = currentMinScale();
		CMN.Log("proxy set min scale = "+scale);
		//minScale = getMinScale();
		preDraw2(new PointF(dimension[0]*1.0f/2, dimension[1]*1.0f/2),scale);
//		ViewGroup.LayoutParams lp = view_to_paint.getLayoutParams();
//		lp.width=(int) (sWidth*scale);
//		lp.height=(int) (sHeight*scale);
		if(proxyBm!=null){
			float scale = proxyBm.getWidth()/(float)sWidth;
			maxImageSampleSize = calcSampleSize(scale);
		}
		computeQuickZoomLevels();
		CMN.Log("sampleSize：", calcSampleSize(scale), fullImageSampleSize, "dimension：", sWidth, sHeight, "setProxy time:"+(System.currentTimeMillis()-time));
		
	}
	
	/**
	 * Called by worker task when decoder is ready and image size and EXIF orientation is known.
	 */
	private void onTileInited(/*int sWidth, int sHeight, int sOrientation*/) {
		// If actual dimensions don't match the declared size, reset everything.
		if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != sWidth || this.sHeight != sHeight)) {
			reset(false);
		}
		this.sWidth = sWidth;
		this.sHeight = sHeight;
		//this.sOrientation = sOrientation;
		CMN.Log("onTilesInited sWidth, sHeight, sOrientation", sWidth, sHeight, orientation, scale);
		checkReady();
		if (!checkImageLoaded() && maxTileWidth > 0 && maxTileWidth != TILE_SIZE_AUTO && maxTileHeight > 0 && maxTileHeight != TILE_SIZE_AUTO && getScreenWidth() > 0 && getScreenHeight() > 0) {
			initialiseBaseLayer(new Point(maxTileWidth, maxTileHeight));
		}
		invalidate();
		requestLayout();
		isProxy = false;
	}
	
	private void postTileInited(/*int sWidth, int sHeight, int sOrientation*/) {
		post(this::onTileInited);
	}
	
	
	private void postGetMinScale() {
		post(() -> scale = currentMinScale());
	}
	
	/**
	 * Async task used to load images without blocking the UI thread.
	 */
	private static class TileLoadTask extends AsyncTask<Void, Void, Void> {
		private final WeakReference<SubsamplingScaleImageView> viewRef;
		private final WeakReference<ImageRegionDecoder> decoderRef;
		private final WeakReference<Tile> tileRef;
		private Exception exception;
		
		TileLoadTask(SubsamplingScaleImageView view, ImageRegionDecoder decoder, Tile tile) {
			this.viewRef = new WeakReference<>(view);
			this.decoderRef = new WeakReference<>(decoder);
			this.tileRef = new WeakReference<>(tile);
			tile.loading = true;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			SubsamplingScaleImageView view = viewRef.get();
			ImageRegionDecoder decoder = decoderRef.get();
			Tile tile = tileRef.get();
			if (tile != null) {
				if (decoder != null && view != null && decoder.isReady() && tile.visible) {
					try {
						if(tile.sampleSize==view.fullImageSampleSize && tile.sampleSize>1){
							return null;
						}
	//					if(tile.bitmapStore!=null && tile.bitmapStore.get()!=null){
	//						Bitmap bm = tile.bitmapStore.get();
	//						if(bm.isRecycled()){
	//							tile.bitmapStore.clear();
	//						} else {
	//							tile.loading=false;
	//							CMN.Log("原貌返回！");
	//							return bm;
	//						}
	//					}
						//CMN.Log("TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d", tile.sRect, tile.sampleSize);
						//线程锁
						//view.decoderLock.readLock().lock();
						try {
							//if (decoder.isReady()) {
								// Update tile's file sRect according to rotation
								view.fileSRect(tile.sRect, tile.fileSRect);
								if (view.sRegion != null) {
									tile.fileSRect.offset(view.sRegion.left, view.sRegion.top);
								}
							Bitmap ret = decoder.decodeRegion(tile.fileSRect, tile.sampleSize);
							tile.bitmap = ret;
							if(view.paint_to_grid && tile.sampleSize==1){
								view.view_to_paint.post(new Runnable() {
									@Override
									public void run() {
										ImageView iv = tile.iv;
										if(iv==null){
											tile.iv = iv = new ImageView(view.getContext());
											view.view_to_paint.addView(iv);
										}
										iv.setVisibility(View.VISIBLE);
										iv.setImageBitmap(ret);
										ViewGroup.LayoutParams lp = iv.getLayoutParams();
										lp.width=(int) (tile.sRect.width()*view.minScale());
										lp.height=(int) (tile.sRect.height()*view.minScale());
										iv.setLayoutParams(lp);
										iv.setTranslationX((int)(tile.sRect.left*view.minScale()));
										iv.setTranslationY((int)(tile.sRect.top*view.minScale()));
										
										CMN.Log("设置了");
									}
								});
							}
							//} else {
							//	tile.loading = false;
							//}
						} finally {
							//view.decoderLock.readLock().unlock();
						}
					} catch (Exception e) {
						//CMN.Log("Failed to decode tile", e);
						exception = e;
					}
				}
				tile.loading = false;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void bitmap) {
			final SubsamplingScaleImageView subsamplingScaleImageView = viewRef.get();
			final Tile tile = tileRef.get();
			if (subsamplingScaleImageView != null /*&& tile != null*/) {
				//if (tile.bitmap == null && exception != null && subsamplingScaleImageView.onImageEventListener != null) {
				//	subsamplingScaleImageView.onImageEventListener.onTileLoadError(exception);
				//}
				subsamplingScaleImageView.onTileLoaded(tile);
			}
		}
	}
	
	/**
	 * Called by worker task when a tile has loaded. Redraws the view.
	 * @param tile
	 */
	private synchronized void onTileLoaded(Tile tile) {
		//CMN.Log("onTileLoaded");
		checkReady();
		checkImageLoaded();
		//if(tile.visible && tile.sampleSize==calculateInSampleSize(scale))
			invalidate();
	}
	
	/**
	 * Called by worker task when full size image bitmap is ready (tiling is disabled).
	 */
	@Deprecated
	private synchronized void onImageLoaded(int sOrientation, boolean bitmapIsCached) {
		debug("onImageLoaded");
		// If actual dimensions don't match the declared size, reset everything.
		//if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != bitmap.getWidth() || this.sHeight != bitmap.getHeight())) {
		//	reset(false);
		//}
		this.sOrientation = sOrientation;
		boolean ready = checkReady();
		boolean imageLoaded = checkImageLoaded();
		if (ready || imageLoaded) {
			invalidate();
			requestLayout();
		}
	}
	
	/**
	 * Helper method for load tasks. Examines the EXIF info on the image file to determine the orientation.
	 * This will only work for external files, not assets, resources or other URIs.
	 */
	@AnyThread
	private int getExifOrientation(Context context, String sourceUri) {
		if (sourceUri.startsWith(ContentResolver.SCHEME_CONTENT)) {
			Cursor cursor = null;
			try {
				String[] columns = { MediaStore.Images.Media.ORIENTATION };
				cursor = context.getContentResolver().query(Uri.parse(sourceUri), columns, null, null, null);
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						int orientation = cursor.getInt(0);
						if (/*VALID_ORIENTATIONS.contains(orientation) && */orientation != ORIENTATION_USE_EXIF) {
							return orientation;
						}
						//CMN.Log( "Unsupported orientation: " + orientation);
					}
				}
			} catch (Exception e) {
				//CMN.Log(TAG, "Could not get orientation of image from media store");
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return ORIENTATION_0;
		}
		try {
			ExifInterface exifInterface;
			if (sourceUri.startsWith(ImageSource.ASSET_SCHEME) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				//sourceUri = sourceUri.substring(ImageSource.ASSET_SCHEME.length() - 1);
				exifInterface = new ExifInterface(getContext().getAssets().open(sourceUri));
			} else {
				if (sourceUri.startsWith(ImageSource.FILE_SCHEME)) {
					sourceUri =  sourceUri.substring(ImageSource.FILE_SCHEME.length() - 1);
				}
				exifInterface = new ExifInterface(sourceUri);
			}
			int orientationAttr = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientationAttr) {
				case ExifInterface.ORIENTATION_NORMAL:
				case ExifInterface.ORIENTATION_UNDEFINED:
					return ORIENTATION_0;
				case ExifInterface.ORIENTATION_ROTATE_90:
					return ORIENTATION_90;
				case ExifInterface.ORIENTATION_ROTATE_180:
					return ORIENTATION_180;
				case ExifInterface.ORIENTATION_ROTATE_270:
					return ORIENTATION_270;
				default:
					//CMN.Log("Unsupported EXIF orientation: " + orientationAttr);
				break;
			}
		} catch (Exception e) {
			//CMN.Log("Could not get EXIF orientation of image");
		}
		return ORIENTATION_0;
	}
	
	private void execute(AsyncTask<Void, Void, ?> asyncTask) {
		try {
			//asyncTask.executeOnExecutor(executor);
			asyncTask.execute();
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	static ArrayListGood.ArrayCreator<Tile> TileArrayCreator_Instance = initialCapacity -> new Tile[initialCapacity];
	
	private static class TileMap {
		Tile[] tiles;
		ArrayListGood<Tile> patchTiles = new ArrayListGood<>(10, TileArrayCreator_Instance);
		int sampleSize;
		int xTiles;
		int yTiles;
		int sTileWidth;
		int sTileHeight;
		
		public TileMap(Tile[] tileGrid, int sampleSize, int xTiles, int yTiles, int sTileWidth, int sTileHeight) {
			tiles = tileGrid;
			this.sampleSize = sampleSize;
			this.xTiles = xTiles;
			this.yTiles = yTiles;
			this.sTileWidth = sTileWidth;
			this.sTileHeight = sTileHeight;
		}
		
		public boolean missing(int i) {
			if(i>=tiles.length) return true;
			int PI = tiles[i].patchId;
			return PI>=patchTiles.size()||patchTiles.get(PI)!=tiles[i];
		}
	}
	
	private static class Tile {
		public ImageView iv;
		public int patchId;
		private Rect sRect;
		private int sampleSize;
		private Bitmap bitmap;
		//private WeakReference<Bitmap> bitmapStore;
		private boolean loading;
		private boolean visible;
		
		// Volatile fields instantiated once then updated before use to reduce GC.
		private Rect vRect;
		private Rect fileSRect;
		
		public void clean() {
			//bitmapStore = new WeakReference<>(bitmap);
			bitmap.recycle();
			bitmap = null;
			if(iv!=null){
				iv.setImageBitmap(null);
				iv.setVisibility(View.GONE);
			}
		}
		
		public boolean missing() {
			return !visible||bitmap==null;
		}
	}
	
	private static class Anim {
		private float scaleStart; // Scale at start of anim
		private float scaleEnd; // Scale at end of anim (target)
		private float rotationStart; // Rotation at start of anim
		private float rotationEnd; // Rotation at end o anim
		private PointF sCenterStart; // Source center point at start
		private PointF sCenterEnd; // Source center point at end, adjusted for pan limits
		private PointF sCenterEndRequested; // Source center point that was requested, without adjustment
		private PointF vFocusStart; // View point that was double tapped
		private PointF vFocusEnd; // Where the view focal point should be moved to during the anim
		private long duration = 500; // How long the anim takes
		private boolean interruptible = true; // Whether the anim can be interrupted by a touch
		private int easing = EASE_IN_OUT_QUAD; // Easing style
		private int origin = ORIGIN_ANIM; // Animation origin (API, double tap or fling)
		private long time = System.currentTimeMillis(); // Start time
	}
	
	private static class ScaleTranslateRotate {
		private ScaleTranslateRotate(float scale, PointF vTranslate, float rotate) {
			this.scale = scale;
			this.vTranslate = vTranslate;
			this.rotate = rotate;
		}
		private float scale;
		private PointF vTranslate;
		private float rotate;
	}
	
	/**
	 * Set scale, center and orientation from saved state.
	 */
	private void restoreState(ImageViewState state) {
		if (state != null) {
			//this.orientation = state.getOrientation();
			this.pendingScale = state.getScale();
			this.sPendingCenter = state.getCenter();
			setRotationInternal(0);
			invalidate();
		}
	}
	
	/**
	 * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing {@link #TILE_SIZE_AUTO} will re-enable the default behaviour.
	 *
	 * @param maxPixels Maximum tile size X and Y in pixels.
	 */
	public void setMaxTileSize(int maxPixels) {
		this.maxTileWidth = maxPixels;
		this.maxTileHeight = maxPixels;
	}
	
	/**
	 * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing {@link #TILE_SIZE_AUTO} will re-enable the default behaviour.
	 *
	 * @param maxPixelsX Maximum tile width.
	 * @param maxPixelsY Maximum tile height.
	 */
	public void setMaxTileSize(int maxPixelsX, int maxPixelsY) {
		this.maxTileWidth = maxPixelsX;
		this.maxTileHeight = maxPixelsY;
	}
	
	/**
	 * Use canvas max bitmap width and height instead of the default 2048, to avoid redundant tiling.
	 */
	@NonNull
	private Point getMaxBitmapDimensions(Canvas canvas) {
		return new Point(Math.min(canvas.getMaximumBitmapWidth(), maxTileWidth), Math.min(canvas.getMaximumBitmapHeight(), maxTileHeight));
	}
	
	/**
	 * Get source width taking rotation into account.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	private int sWidth() {
		return sWidth;
	}
	
	@SuppressWarnings("SuspiciousNameCombination")
	private int exifWidth() {
		if (sOrientation == 90 || sOrientation == 270) {
			return sHeight;
		} else {
			return sWidth;
		}
	}
	
	@SuppressWarnings("SuspiciousNameCombination")
	private int exifSWidth() {
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5 ){
			return sHeight;
		} else {
			return sWidth;
		}
	}
	
	public int getScreenExifWidth() {
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5 ){
			//return (isProxy && dm!=null)?dm.heightPixels:getHeight();
			return view_pager_toguard==null?getHeight():view_pager_toguard.getMeasuredHeight();
		} else {
			//return (isProxy && dm!=null)?dm.widthPixels:getWidth();
			return view_pager_toguard==null?getWidth():view_pager_toguard.getMeasuredWidth();
		}
	}
	
	public int getScreenExifHeight() {
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5 ){
			//return (isProxy && dm!=null)?dm.widthPixels:getWidth();
			return view_pager_toguard==null?getWidth():view_pager_toguard.getMeasuredWidth();
		} else {
			//return (isProxy && dm!=null)?dm.heightPixels:getHeight();
			return view_pager_toguard==null?getHeight():view_pager_toguard.getMeasuredHeight();
		}
	}
	/**
	 * Get source height taking rotation into account.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	private int sHeight() {
		return sHeight;
	}
	
	@SuppressWarnings("SuspiciousNameCombination")
	private int exifHeight() {
		if (sOrientation == 90 || sOrientation == 270) {
			return sWidth;
		} else {
			return sHeight;
		}
	}
	/**
	 * Converts source rectangle from tile, which treats the image file as if it were in the correct orientation already,
	 * to the rectangle of the image that needs to be loaded.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	@AnyThread
	private void fileSRect(Rect sRect, Rect target) {
		target.set(sRect);
	}
	
	/**
	 * Pythagoras distance between two points.
	 */
	private float distance(float x0, float x1, float y0, float y1) {
		float x = x0 - x1;
		float y = y0 - y1;
		return (float) Math.sqrt(x * x + y * y);
	}
	
	/**
	 * Releases all resources the view is using and resets the state, nulling any fields that use significant memory.
	 * After you have called this method, the view can be re-used by setting a new image. Settings are remembered
	 * but state (scale and center) is forgotten. You can restore these yourself if required.
	 */
	public void recycle() {
		reset(true);
	}
	
	/**
	 * Convert screen to source x coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float viewToSourceX(float vx) {
		return (vx - vTranslate.x)/scale;
	}
	
	private float viewToSourceX(float vx,float tx) {
		return (vx - tx)/scale;
	}
	
	private float viewToSourceX(float vx,float tx,float scale) {
		return (vx - tx)/scale;
	}
	
	private float viewToSourceX(float vx, PointF vTranslate) {
		return (vx - vTranslate.x)/scale;
	}
	
	private float viewToSourceY(float vy, PointF vTranslate) {
		return (vy - vTranslate.y)/scale;
	}
	
	private float viewToSourceY(float vy,float ty,float scale) {
		return (vy - ty)/scale;
	}
	/**
	 * Convert screen to source y coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float viewToSourceY(float vy) {
		return (vy - vTranslate.y)/scale;
	}
	
	/**
	 * Converts a rectangle within the view to the corresponding rectangle from the source file, taking
	 * into account the current scale, translation, orientation and clipped region. This can be used
	 * to decode a bitmap from the source file.
	 *
	 * This method will only work when the image has fully initialised, after {@link #isReady()} returns
	 * true. It is not guaranteed to work with preloaded bitmaps.
	 *
	 * The result is written to the fRect argument. Re-use a single instance for efficiency.
	 * @param vRect rectangle representing the view area to interpret.
	 * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
	 */
	public void viewToFileRect(Rect vRect, Rect fRect) {
		if (!readySent) {
			return;
		}
		fRect.set(
				(int)viewToSourceX(vRect.left),
				(int)viewToSourceY(vRect.top),
				(int)viewToSourceX(vRect.right),
				(int)viewToSourceY(vRect.bottom));
		fileSRect(fRect, fRect);
		fRect.set(
				Math.max(0, fRect.left),
				Math.max(0, fRect.top),
				Math.min(sWidth, fRect.right),
				Math.min(sHeight, fRect.bottom)
		);
		if (sRegion != null) {
			fRect.offset(sRegion.left, sRegion.top);
		}
	}
	
	/**
	 * Find the area of the source file that is currently visible on screen, taking into account the
	 * current scale, translation, orientation and clipped region. This is a convenience method; see
	 * {@link #viewToFileRect(Rect, Rect)}.
	 * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
	 */
	public void visibleFileRect(Rect fRect) {
		if (!readySent) {
			return;
		}
		fRect.set(0, 0, getScreenWidth(), getScreenHeight());
		viewToFileRect(fRect, fRect);
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vxy view X/Y coordinate.
	 * @return a coordinate representing the corresponding source coordinate.
	 */
	@Nullable
	public final PointF viewToSourceCoord(PointF vxy) {
		return viewToSourceCoord(vxy.x, vxy.y, new PointF());
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vx view X coordinate.
	 * @param vy view Y coordinate.
	 * @return a coordinate representing the corresponding source coordinate.
	 */
	@Nullable
	public final PointF viewToSourceCoord(float vx, float vy) {
		return viewToSourceCoord(vx, vy, new PointF());
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vxy view coordinates to convert.
	 * @param sTarget target object for result. The same instance is also returned.
	 * @return source coordinates. This is the same instance passed to the sTarget param.
	 */
	@Nullable
	public final PointF viewToSourceCoord(PointF vxy, @NonNull PointF sTarget) {
		return viewToSourceCoord(vxy.x, vxy.y, sTarget);
	}
	
	
	public final float viewToSourceFrameX(float vx, float vy, float tx, float ty,  boolean getY) {
		
		float sXPreRotate = viewToSourceX(vx, tx);
		float sYPreRotate = viewToSourceX(vy, ty);
		
		if (rotation == 0f) {
			return sXPreRotate;
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(getScreenWidth() / 2, tx);
			final float sourceVCenterY = viewToSourceX(getScreenHeight() / 2, ty);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			return getY?((float) (-sXPreRotate * sin + sYPreRotate * cos) + sourceVCenterY)
					:((float) (sXPreRotate * cos + sYPreRotate * sin) + sourceVCenterX);
		}
		
	}
	
	public final boolean viewFramesToSourceInSource(float vx_delta, float tx, float ty, boolean getY) {
		int scw = getScreenWidth();
		float vx1 = vx_delta;
		float vx2 = scw+vx_delta;
		float vy = getScreenHeight()/2;
		
		int maxX = getY?sHeight:sWidth;
		
		float sXPreRotate = viewToSourceX(vx1, tx);
		float sYPreRotate = viewToSourceX(getScreenHeight() / 2, ty);
		
		float sXAfterRotate;
		
		if (rotation == 0f) {
			if(sXPreRotate<0||sXPreRotate>maxX) return false;
			sXAfterRotate = viewToSourceX(vx2, tx);
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(getScreenWidth() / 2, tx);
			final float sourceVCenterY = viewToSourceX(getScreenHeight() / 2, ty);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			sXAfterRotate = getY?((float) (-sXPreRotate * vtParms_sin /*+ sYPreRotate * cos*/) + sourceVCenterY)
					:((float) (sXPreRotate * vtParms_cos /*+ sYPreRotate * sin*/) + sourceVCenterX);
			if(sXAfterRotate<0||sXAfterRotate>maxX) return false;
			sXPreRotate = viewToSourceX(vx2, tx);
			sXPreRotate -= sourceVCenterX;
			sXAfterRotate = getY?((float) (-sXPreRotate* vtParms_sin /* + sYPreRotate * cos*/) + sourceVCenterY)
					:((float) (sXPreRotate * vtParms_cos /*+ sYPreRotate * sin*/) + sourceVCenterX);
		}
		//CMN.Log("sXAfterRotate", sXAfterRotate, ty, vx_delta);
		return sXAfterRotate>=0 && sXAfterRotate<=maxX;
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vx view X coordinate.
	 * @param vy view Y coordinate.
	 * @param sTarget target object for result. The same instance is also returned.
	 * @return source coordinates. This is the same instance passed to the sTarget param.
	 */
	@Nullable
	public final PointF viewToSourceCoord(float vx, float vy, @NonNull PointF sTarget) {
		
		float sXPreRotate = viewToSourceX(vx);
		float sYPreRotate = viewToSourceY(vy);
		
		if (rotation == 0f) {
			sTarget.set(sXPreRotate, sYPreRotate);
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(getScreenWidth() / 2);
			final float sourceVCenterY = viewToSourceY(getScreenHeight() / 2);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			sTarget.x = (float) (sXPreRotate * cos + sYPreRotate * sin) + sourceVCenterX;
			sTarget.y = (float) (-sXPreRotate * sin + sYPreRotate * cos) + sourceVCenterY;
		}
		
		return sTarget;
	}
	
	
	public void centerToSourceCoord(PointF tmpCenter, @NonNull PointF vTranslate) {
		float cx = tmpCenter.x;
		float cy = tmpCenter.y;
		
		float sXPreRotate = viewToSourceX(cx, vTranslate);
		float sYPreRotate = viewToSourceY(cy, vTranslate);
		
		if (rotation == 0f) {
			tmpCenter.set(sXPreRotate, sYPreRotate);
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(cx, vTranslate);
			final float sourceVCenterY = viewToSourceY(cy, vTranslate);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			tmpCenter.x = (float) (sXPreRotate * cos + sYPreRotate * sin) + sourceVCenterX;
			tmpCenter.y = (float) (-sXPreRotate * sin + sYPreRotate * cos) + sourceVCenterY;
		}
	}
	
	/**
	 * Convert source to screen x coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float sourceToViewX(float sx) {
		return (sx * scale) + vTranslate.x;
	}
	
	/**
	 * Convert source to view y coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float sourceToViewY(float sy) {
		return (sy * scale) + vTranslate.y;
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sxy source coordinates to convert.
	 * @return view coordinates.
	 */
	@Nullable
	public final PointF sourceToViewCoord(PointF sxy) {
		return sourceToViewCoord(sxy.x, sxy.y, new PointF());
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sx source X coordinate.
	 * @param sy source Y coordinate.
	 * @return view coordinates.
	 */
	@Nullable
	public final PointF sourceToViewCoord(float sx, float sy) {
		return sourceToViewCoord(sx, sy, new PointF());
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sxy source coordinates to convert.
	 * @param vTarget target object for result. The same instance is also returned.
	 * @return view coordinates. This is the same instance passed to the vTarget param.
	 */
	@SuppressWarnings("UnusedReturnValue")
	@Nullable
	public final PointF sourceToViewCoord(PointF sxy, @NonNull PointF vTarget) {
		return sourceToViewCoord(sxy.x, sxy.y, vTarget);
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sx source X coordinate.
	 * @param sy source Y coordinate.
	 * @param vTarget target object for result. The same instance is also returned.
	 * @return view coordinates. This is the same instance passed to the vTarget param.
	 */
	@Nullable
	public final PointF sourceToViewCoord(float sx, float sy, @NonNull PointF vTarget) {
		float xPreRotate = sourceToViewX(sx);
		float yPreRotate = sourceToViewY(sy);
		
		if (rotation == 0f) {
			vTarget.set(xPreRotate, yPreRotate);
		} else {
			// Calculate offset by rotation
			final float vCenterX = getScreenWidth() / 2;
			final float vCenterY = getScreenHeight() / 2;
			xPreRotate -= vCenterX;
			yPreRotate -= vCenterY;
			vTarget.x = (float) (xPreRotate * cos - yPreRotate * sin) + vCenterX;
			vTarget.y = (float) (xPreRotate * sin + yPreRotate * cos) + vCenterY;
		}
		
		return vTarget;
	}
	
	/**
	 * Convert source rect to screen rect, integer values.
	 */
	private void sourceToViewRect(@NonNull Rect sRect, @NonNull Rect vTarget) {
		// NOTE: Arbitrary rotation makes this impossible to implement literally, due to how Rect
		// is represented, but as this is used before rotation is applied, it doesn't matter
//		vTarget.set(
//				(int)sourceToViewX(sRect.left),
//				(int)sourceToViewY(sRect.top),
//				(int)sourceToViewX(sRect.right),
//				(int)sourceToViewY(sRect.bottom)
//		);
		vTarget.set(
				(int) (sRect.left * scale + vTranslate.x),
				(int) (sRect.top * scale + vTranslate.y),
				(int) (sRect.right * scale + vTranslate.x),
				(int) (sRect.bottom * scale + vTranslate.y)
		);
		
	}
	
	/**
	 * Get the translation required to place a given source coordinate at the center of the screen, with the center
	 * adjusted for asymmetric padding. Accepts the desired scale as an argument, so this is independent of current
	 * translate and scale. The result is fitted to bounds, putting the image point as near to the screen center as permitted.
	 */
	@NonNull
	private PointF vTranslateForSCenter(float sCenterX, float sCenterY, float scale) {
		int vxCenter = getPaddingLeft() + (getScreenWidth() - getPaddingRight() - getPaddingLeft())/2;
		int vyCenter = getPaddingTop() + (getScreenHeight() - getPaddingBottom() - getPaddingTop())/2;
		// TODO: Rotation
		strTemp.scale = scale;
		strTemp.vTranslate.set(vxCenter - (sCenterX * scale), vyCenter - (sCenterY * scale));
		fitToBounds_internal(true, strTemp);
		return strTemp.vTranslate;
	}
	
	/**
	 * Given a requested source center and scale, calculate what the actual center will have to be to keep the image in
	 * pan limits, keeping the requested center as near to the middle of the screen as allowed.
	 */
	@NonNull
	private PointF limitedSCenter(float sCenterX, float sCenterY, float scale, @NonNull PointF sTarget) {
		PointF vTranslate = vTranslateForSCenter(sCenterX, sCenterY, scale);
		int vxCenter = getPaddingLeft() + (getScreenWidth() - getPaddingRight() - getPaddingLeft())/2;
		int vyCenter = getPaddingTop() + (getScreenHeight() - getPaddingBottom() - getPaddingTop())/2;
		float sx = (vxCenter - vTranslate.x)/scale;
		float sy = (vyCenter - vTranslate.y)/scale;
		sTarget.set(sx, sy);
		return sTarget;
	}
	
	/**
	 * Returns the minimum allowed scale.
	 */
	private float minScale() {//
		int vPadding = getPaddingBottom() + getPaddingTop();
		int hPadding = getPaddingLeft() + getPaddingRight();
		float ret = Math.min((getScreenWidth() - hPadding) / (float) exifWidth(), (getScreenHeight() - vPadding) / (float) exifHeight());
		if(ret<=0 || ret==Float.NaN) ret = 0.05f;
		return ret;
	}
	
	/**
	 * Returns the minimum allowed scale.
	 */
	private float currentMinScale() {//
		int vPadding = getPaddingBottom() + getPaddingTop();
		int hPadding = getPaddingLeft() + getPaddingRight();
		int sw = sWidth;
		int sh = sHeight;
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5){
			sw = sHeight;
			sh = sWidth;
		}
		return Math.min((getScreenWidth() - hPadding) / (float) sw, (getScreenHeight() - vPadding) / (float) sh);
	}
	
	/**
	 * Adjust a requested scale to be within the allowed limits.
	 */
	private float limitedScale(float targetScale) {
		//Log.e("anim_limitedScale",""+targetScale);
		//targetScale = Math.max(minScale(), targetScale);
		////targetScale = Math.min(maxScale, targetScale);
		return targetScale;
	}
	
	/**
	 * Apply a selected type of easing.
	 * @param type Easing type, from static fields
	 * @param time Elapsed time
	 * @param from Start value
	 * @param change Target value
	 * @param duration Anm duration
	 * @return Current value
	 */
	private float ease(int type, long time, float from, float change, long duration) {
		switch (type) {
			case EASE_IN_OUT_QUAD:
				return easeInOutQuad(time, from, change, duration);
			case EASE_OUT_QUAD:
				return easeOutQuad(time, from, change, duration);
			default:
				throw new IllegalStateException("Unexpected easing type: " + type);
		}
	}
	
	/**
	 * Quadratic easing for fling. With thanks to Robert Penner - http://gizma.com/easing/
	 * @param time Elapsed time
	 * @param from Start value
	 * @param change Target value
	 * @param duration Anm duration
	 * @return Current value
	 */
	private float easeOutQuad(long time, float from, float change, long duration) {
		float progress = (float)time/(float)duration;
		return -change * progress*(progress-2) + from;
//		float progress = (float)time/(float)duration;
//		return +change * fluidInterpolator.getInterpolation(progress) + from;
	}
	
	OverScroller flingScroller;
	DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(1f);
	ViscousFluidInterpolator fluidInterpolator = new ViscousFluidInterpolator();
	
	static class ViscousFluidInterpolator implements Interpolator {
		/** Controls the viscous fluid effect (how much of it). */
		private static final float VISCOUS_FLUID_SCALE = 8.0f;
		
		private static final float VISCOUS_FLUID_NORMALIZE;
		private static final float VISCOUS_FLUID_OFFSET;
		
		static {
			
			// must be set to 1.0 (used in viscousFluid())
			VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f);
			// account for very small floating-point error
			VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f);
		}
		
		private static float viscousFluid(float x) {
			x *= VISCOUS_FLUID_SCALE;
			if (x < 1.0f) {
				x -= (1.0f - (float)Math.exp(-x));
			} else {
				float start = 0.36787944117f;   // 1/e == exp(-1)
				x = 1.0f - (float)Math.exp(1.0f - x);
				x = start + x * (1.0f - start);
			}
			return x;
		}
		
		@Override
		public float getInterpolation(float input) {
			final float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
			if (interpolated > 0) {
				return interpolated + VISCOUS_FLUID_OFFSET;
			}
			return interpolated;
		}
	}
	
	/**
	 * Quadratic easing for scale and center animations. With thanks to Robert Penner - http://gizma.com/easing/
	 * @param time Elapsed time
	 * @param from Start value
	 * @param change Target value
	 * @param duration Anm duration
	 * @return Current value
	 */
	private float easeInOutQuad(long time, float from, float change, long duration) {
		float timeF = time/(duration/2f);
		if (timeF < 1) {
			return (change/2f * timeF * timeF) + from;
		} else {
			timeF--;
			return (-change/2f) * (timeF * (timeF - 2) - 1) + from;
		}
	}
	
	/**
	 * Debug logger
	 */
	@AnyThread
	private void debug(String message, Object... args) {
		if (SSVD) {
			Log.d(TAG, String.format(message, args));
		}
	}
	
	/**
	 * For debug overlays. Scale pixel value according to screen density.
	 */
	private int px(int px) {
		return (int)(density * px);
	}
	
	public final void setRegionDecoderClass(@NonNull Class<? extends ImageRegionDecoder> regionDecoderClass) {
		this.regionDecoderFactory = new CompatDecoderFactory<>(regionDecoderClass);
	}
	
	public final void setRegionDecoderFactory(@NonNull DecoderFactory<? extends ImageRegionDecoder> regionDecoderFactory) {
		this.regionDecoderFactory = regionDecoderFactory;
	}
	
	public final void setBitmapDecoderClass(@NonNull Class<? extends ImageDecoder> bitmapDecoderClass) {
		this.bitmapDecoderFactory = new CompatDecoderFactory<>(bitmapDecoderClass);
	}
	
	public final void setBitmapDecoderFactory(@NonNull DecoderFactory<? extends ImageDecoder> bitmapDecoderFactory) {
		this.bitmapDecoderFactory = bitmapDecoderFactory;
	}
	
	/**
	 * Returns the minimum allowed scale.
	 * @return the minimum scale as a source/view pixels ratio.
	 */
	public final float getMinScale() {
		return minScale();
	}
	
	/**
	 * By default, image tiles are at least as high resolution as the screen. For a retina screen this may not be
	 * necessary, and may increase the likelihood of an OutOfMemoryError. This method sets a DPI at which higher
	 * resolution tiles should be loaded. Using a lower number will on average use less memory but result in a lower
	 * quality image. 160-240dpi will usually be enough. This should be called before setting the image source,
	 * because it affects which tiles get loaded. When using an untiled source image this method has no effect.
	 * @param minimumTileDpi Tile loading threshold.
	 */
	public void setMinimumTileDpi(int minimumTileDpi) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float averageDpi = (metrics.xdpi + metrics.ydpi)/2;
		this.minimumTileDpi = (int)Math.min(averageDpi, minimumTileDpi);
		if (isReady()) {
			reset(false);
			invalidate();
		}
	}
	
	/**
	 * Returns the source point at the center of the view.
	 * @return the source coordinates current at the center of the view.
	 */
	@Nullable
	public final PointF getCenter() {
		int mX = getScreenWidth()/2;
		int mY = getScreenHeight()/2;
		return viewToSourceCoord(mX, mY);
	}
	
	/**
	 * Returns the current scale value.
	 * @return the current scale as a source/view pixels ratio.
	 */
	public final float getScale() {
		return scale;
	}
	
	/**
	 * Externally change the scale and translation of the source image. This may be used with getCenter() and getScale()
	 * to restore the scale and zoom after a screen rotate.
	 * @param scale New scale to set.
	 * @param sCenter New source image coordinate to center on the screen, subject to boundaries.
	 */
	public final void setScaleAndCenter(float scale, @Nullable PointF sCenter) {
		this.anim = null;
		this.pendingScale = scale;
		this.sPendingCenter = sCenter;
		this.sRequestedCenter = sCenter;
		invalidate();
	}
	
	/**
	 * Returns the current rotation value in degrees
	 */
	public final float getRotationDeg() {
		return (float) Math.toDegrees(rotation);
	}
	
	/**
	 * Returns the current rotation value in radians.
	 */
	public final float getRotationRad() {
		return rotation;
	}
	
	/**
	 * Externally change the rotation around the view center
	 * @param rot Rotation around view center in degrees
	 */
	public final void setRotationDeg(float rot) {
		setRotationInternal((float) Math.toRadians(rot));
		invalidate();
	}
	
	/**
	 * Externally change the rotation around the view center
	 * @param rot Rotation around view center in radians
	 */
	public final void setRotationRad(float rot) {
		setRotationInternal(rot);
		invalidate();
	}
	
	/**
	 * Sets rotation without invalidation
	 */
	private void setRotationInternal(float rot) {
		// Normalize rotation between 0..2pi
		this.rotation = rot % (float) (Math.PI * 2);
		if (this.rotation < 0) this.rotation += Math.PI * 2;
		
		this.cos = Math.cos(rot);
		this.sin = Math.sin(rot);
	}
	
	
	/**
	 * Fully zoom out and return the image to the middle of the screen. This might be useful if you have a view pager
	 * and want images to be reset when the user has moved to another page.
	 */
	public final void resetScaleAndCenter() {
		this.anim = null;
		this.pendingScale = limitedScale(0);
		if (isReady()) {
			this.sPendingCenter = new PointF(sWidth()/2, sHeight()/2);
		} else {
			this.sPendingCenter = new PointF(0, 0);
		}
		
		invalidate();
	}
	
	/**
	 * Call to find whether the view is initialised, has dimensions, and will display an image on
	 * the next draw. If a preview has been provided, it may be the preview that will be displayed
	 * and the full size image may still be loading. If no preview was provided, this is called once
	 * the base layer tiles of the full size image are loaded.
	 * @return true if the view is ready to display an image and accept touch gestures.
	 */
	public final boolean isReady() {
		return readySent;
	}
	
	/**
	 * Call to find whether the main image (base layer tiles where relevant) have been loaded. Before
	 * this event the view is blank unless a preview was provided.
	 * @return true if the main image (not the preview) has been loaded and is ready to display.
	 */
	public final boolean isImageLoaded() {
		return imageLoadedSent;
	}
	
	/**
	 * Called once when the full size image or its base layer tiles have been loaded.
	 */
	@SuppressWarnings("EmptyMethod")
	protected void onImageLoaded() {
	
	}
	
	/**
	 * Get source width, ignoring orientation. If {@link #orientation} returns 90 or 270, you can use {@link #getSHeight()}
	 * for the apparent width.
	 * @return the source image width in pixels.
	 */
	public final int getSWidth() {
		return sWidth;
	}
	
	/**
	 * Get source height, ignoring orientation. If {@link #orientation} returns 90 or 270, you can use {@link #getSWidth()}
	 * for the apparent height.
	 * @return the source image height in pixels.
	 */
	public final int getSHeight() {
		return sHeight;
	}
	
	/**
	 * Get the current state of the view (scale, center, orientation) for restoration after rotate. Will return null if
	 * the view is not ready.
	 * @return an {@link ImageViewState} instance representing the current position of the image. null if the view isn't ready.
	 */
	@Nullable
	public final ImageViewState getState() {
		if (sWidth > 0 && sHeight > 0) {
			//noinspection ConstantConditions
			return new ImageViewState(getScale(), getCenter(), rotation);
		}
		return null;
	}
	
	/**
	 * Returns true if zoom gesture detection is enabled.
	 * @return true if zoom gesture detection is enabled.
	 */
	public final boolean isZoomEnabled() {
		return zoomEnabled;
	}
	
	/**
	 * Enable or disable zoom gesture detection. Disabling zoom locks the the current scale.
	 * @param zoomEnabled true to enable zoom gestures, false to disable.
	 */
	public final void setZoomEnabled(boolean zoomEnabled) {
		this.zoomEnabled = zoomEnabled;
	}
	
	/**
	 <<<<<<< HEAD
	 * Returns true if rotation gesture detection is enabled
	 */
	public final boolean isRotationEnabled() {
		return rotationEnabled;
	}
	
	/**
	 * Enable or disable rotation gesture detection. Disabling locks the current rotation.
	 */
	public final void setRotationEnabled(boolean rotationEnabled) {
		this.rotationEnabled = rotationEnabled;
	}
	
	/**
	 * Returns true if double tap & swipe to zoom is enabled.
	 =======
	 * Returns true if double tap &amp; swipe to zoom is enabled.
	 >>>>>>> master
	 */
	public final boolean isQuickScaleEnabled() {
		return quickScaleEnabled;
	}
	
	/**
	 * Enable or disable double tap &amp; swipe to zoom.
	 * @param quickScaleEnabled true to enable quick scale, false to disable.
	 */
	public final void setQuickScaleEnabled(boolean quickScaleEnabled) {
		this.quickScaleEnabled = quickScaleEnabled;
	}
	
	/**
	 * Returns true if pan gesture detection is enabled.
	 */
	public final boolean isPanEnabled() {
		return panEnabled;
	}
	
	/**
	 * Enable or disable pan gesture detection. Disabling pan causes the image to be centered. Pan
	 * can still be changed from code.
	 * @param panEnabled true to enable panning, false to disable.
	 */
	public final void setPanEnabled(boolean panEnabled) {
		this.panEnabled = panEnabled;
		if (!panEnabled) {
			// TODO: Rotation?
			vTranslate.x = (getScreenWidth()/2) - (scale * (sWidth()/2));
			vTranslate.y = (getScreenHeight()/2) - (scale * (sHeight()/2));
			if (isReady()) {
				refreshRequiredTiles(true);
				invalidate();
			}
		}
	}
	
	/**
	 * Set a solid color to render behind tiles, useful for displaying transparent PNGs.
	 * @param tileBgColor Background color for tiles.
	 */
	public final void setTileBackgroundColor(int tileBgColor) {
		if (Color.alpha(tileBgColor) == 0) {
			tileBgPaint = null;
		} else {
			tileBgPaint = new Paint();
			tileBgPaint.setStyle(Style.FILL);
			tileBgPaint.setColor(tileBgColor);
		}
		invalidate();
	}
	
	/**
	 * <p>
	 * Provide an {@link Executor} to be used for loading images. By default, {@link AsyncTask#THREAD_POOL_EXECUTOR}
	 * is used to minimise contention with other background work the app is doing. You can also choose
	 * to use {@link AsyncTask#SERIAL_EXECUTOR} if you want to limit concurrent background tasks.
	 * Alternatively you can supply an {@link Executor} of your own to avoid any contention. It is
	 * strongly recommended to use a single executor instance for the life of your application, not
	 * one per view instance.
	 * </p><p>
	 * <b>Warning:</b> If you are using a custom implementation of {@link ImageRegionDecoder}, and you
	 * supply an executor with more than one thread, you must make sure your implementation supports
	 * multi-threaded bitmap decoding or has appropriate internal synchronization. From SDK 21, Android's
	 * {@link android.graphics.BitmapRegionDecoder} uses an internal lock so it is thread safe but
	 * there is no advantage to using multiple threads.
	 * </p>
	 * @param executor an {@link Executor} for image loading.
	 */
	public void setExecutor(@NonNull Executor executor) {
		this.executor = executor;
	}
	
	/**
	 * Enable or disable eager loading of tiles that appear on screen during gestures or animations,
	 * while the gesture or animation is still in progress. By default this is enabled to improve
	 * responsiveness, but it can result in tiles being loaded and discarded more rapidly than
	 * necessary and reduce the animation frame rate on old/cheap devices. Disable this on older
	 * devices if you see poor performance. Tiles will then be loaded only when gestures and animations
	 * are completed.
	 * @param eagerLoadingEnabled true to enable loading during gestures, false to delay loading until gestures end
	 */
	public void setEagerLoadingEnabled(boolean eagerLoadingEnabled) {
		this.eagerLoadingEnabled = eagerLoadingEnabled;
	}
	
	/**
	 * Enables visual debugging, showing tile boundaries and sizes.
	 * @param debug true to enable debugging, false to disable.
	 */
	public final void setDebug(boolean debug) {
		this.SSVD = debug;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
		this.onLongClickListener = onLongClickListener;
	}
	
	/**
	 * Creates a panning animation builder, that when started will animate the image to place the given coordinates of
	 * the image in the center of the screen. If doing this would move the image beyond the edges of the screen, the
	 * image is instead animated to move the center point as near to the center of the screen as is allowed - it's
	 * guaranteed to be on screen.
	 * @param sCenter Target center point
	 * @return {@link AnimationBuilder} instance. Call {@link SubsamplingScaleImageView.AnimationBuilder#start()} to start the anim.
	 */
	@Nullable
	public AnimationBuilder animateCenter(PointF sCenter) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(sCenter);
	}
	
	/**
	 * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
	 * beyond the panning limits, the image is automatically panned during the animation.
	 * @param scale Target scale.
	 * @return {@link AnimationBuilder} instance. Call {@link SubsamplingScaleImageView.AnimationBuilder#start()} to start the anim.
	 */
	@Nullable
	public AnimationBuilder animateScale(float scale) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(scale);
	}
	
	/**
	 * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
	 * beyond the panning limits, the image is automatically panned during the animation.
	 * @param scale Target scale.
	 * @param sCenter Target source center.
	 * @return {@link AnimationBuilder} instance. Call {@link SubsamplingScaleImageView.AnimationBuilder#start()} to start the anim.
	 */
	@Nullable
	public AnimationBuilder animateScaleAndCenter(float scale, PointF sCenter) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(scale, sCenter);
	}
	
	public AnimationBuilder animateSTR(float scale, PointF sCenter, float rotation) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(scale, sCenter, rotation);
	}
	
	/**
	 * Builder class used to set additional options for a scale animation. Create an instance using {@link #animateScale(float)},
	 * then set your options and call {@link #start()}.
	 */
	public final class AnimationBuilder {
		private final float targetScale;
		private final PointF targetSCenter;
		private final float targetRotation;
		private final PointF vFocus;
		private long duration = 500;
		private int easing = EASE_IN_OUT_QUAD;
		private int origin = ORIGIN_ANIM;
		private boolean interruptible = true;
		private boolean panLimited = true;
		
		private AnimationBuilder(PointF sCenter) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale) {
			this.targetScale = scale;
			this.targetSCenter = getCenter();
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale, PointF sCenter) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale, PointF sCenter, PointF vFocus) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = vFocus;
		}
		
		private AnimationBuilder(PointF sCenter, float rotation) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale, PointF sCenter, float rotation) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		/**
		 * Desired duration of the anim in milliseconds. Default is 500.
		 * @param duration duration in milliseconds.
		 * @return this builder for method chaining.
		 */
		@NonNull
		public AnimationBuilder withDuration(long duration) {
			this.duration = duration;
			return this;
		}
		
		/**
		 * Whether the animation can be interrupted with a touch. Default is true.
		 * @param interruptible interruptible flag.
		 * @return this builder for method chaining.
		 */
		@NonNull
		public AnimationBuilder withInterruptible(boolean interruptible) {
			this.interruptible = interruptible;
			return this;
		}
		
		/**
		 * Set the easing style. See static fields. {@link #EASE_IN_OUT_QUAD} is recommended, and the default.
		 * @param easing easing style.
		 * @return this builder for method chaining.
		 */
		@NonNull
		public AnimationBuilder withEasing(int easing) {
			this.easing = easing;
			return this;
		}
		
		/**
		 * Only for internal use. When set to true, the animation proceeds towards the actual end point - the nearest
		 * point to the center allowed by pan limits. When false, animation is in the direction of the requested end
		 * point and is stopped when the limit for each axis is reached. The latter behaviour is used for flings but
		 * nothing else.
		 */
		@NonNull
		private AnimationBuilder withPanLimited(boolean panLimited) {
			this.panLimited = panLimited;
			return this;
		}
		
		/**
		 * Only for internal use. Indicates what caused the animation.
		 */
		@NonNull
		private AnimationBuilder withOrigin(int origin) {
			this.origin = origin;
			return this;
		}
		
		/**
		 * Starts the animation.
		 */
		public void start() {
			// Make sure the view has dimensions and something to draw before starting animations.
			// Otherwise for example calls to sourceToViewCoord() may return null and cause errors.
			if (!isProxy && !checkReady()) {
				pendingAnimation = AnimationBuilder.this;
				anim = null;
				return;
			}
			//if (anim != null && anim.listener != null) anim.listener.onInterruptedByNewAnim();
			
			int vxCenter = getPaddingLeft() + (getScreenWidth() - getPaddingRight() - getPaddingLeft())/2;
			int vyCenter = getPaddingTop() + (getScreenHeight() - getPaddingBottom() - getPaddingTop())/2;
			float targetScale = limitedScale(this.targetScale);
			PointF targetSCenter = panLimited ? limitedSCenter(this.targetSCenter.x, this.targetSCenter.y, targetScale, new PointF()) : this.targetSCenter;
			anim = new Anim();
			anim.scaleStart = scale;
			anim.scaleEnd = targetScale;
			anim.rotationStart = rotation;
			anim.rotationEnd = targetRotation;
			anim.time = System.currentTimeMillis();
			anim.sCenterEndRequested = targetSCenter;
			anim.sCenterStart = getCenter();
			anim.sCenterEnd = targetSCenter;
			anim.vFocusStart = sourceToViewCoord(targetSCenter);
			anim.vFocusEnd = new PointF(
					vxCenter,
					vyCenter
			);
			anim.duration = duration;
			anim.interruptible = interruptible;
			anim.easing = easing;
			anim.origin = origin;
			anim.time = System.currentTimeMillis();
			
			if (vFocus != null) {
				// Calculate where translation will be at the end of the anim
				float vTranslateXEnd = vFocus.x - (targetScale * anim.sCenterStart.x);
				float vTranslateYEnd = vFocus.y - (targetScale * anim.sCenterStart.y);
				ScaleTranslateRotate satEnd = new ScaleTranslateRotate(targetScale, new PointF(vTranslateXEnd, vTranslateYEnd), targetRotation);
				// Fit the end translation into bounds
				fitToBounds_internal(true, satEnd);
				
				// Adjust the position of the focus point at end so image will be in bounds
				anim.vFocusEnd = new PointF(
						vFocus.x + (satEnd.vTranslate.x - vTranslateXEnd),
						vFocus.y + (satEnd.vTranslate.y - vTranslateYEnd)
				);
			}
			
			if(isProxy){
				postSimulate();
			} else {
				invalidate();
			}
		}
		
	}
	
	public int getScreenWidth() {
		int ret = view_pager_toguard == null ? getWidth() : view_pager_toguard.getMeasuredWidth();
		if(ret==0&&dm!=null) ret = dm.widthPixels;
		return ret;
	}
	
	public int getScreenHeight() {
		int ret = view_pager_toguard==null?getHeight():view_pager_toguard.getMeasuredHeight();
		if(ret==0&&dm!=null) ret = dm.heightPixels;
		return ret;
	}
	
	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
		mClickListener = l;
	}
	
}
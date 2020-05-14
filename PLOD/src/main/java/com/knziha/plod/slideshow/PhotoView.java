package com.knziha.plod.slideshow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.Scroller;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;

/** Photo View for traditional viewpager.<br/> Original Author : liuheng(bmme@vip.qq.com) on 2015/6/21.*/
public class PhotoView extends ImageView {
    private final static int ANIMA_DURING = 800;//1340
    private final static float MAX_SCALE = 10.0f;
	public PhotoBrowsingContext IBC;
	
    private int mAnimaDuring;
    private float mMaxScale;

    private int MAX_FLING_OVER_SCROLL;
    private int MAX_OVER_RESISTANCE;

    private Matrix mBaseMatrix = new Matrix();
    private Matrix mAnimaMatrix = new Matrix();
    private Matrix mSynthesisMatrix = new Matrix();
    private Matrix mTmpMatrix = new Matrix();

    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;
    private OnClickListener mClickListener;

    private ScaleType mScaleType;

    private boolean hasMultiTouch;
    private boolean hasDrawable;
    private boolean isKnowSize;
    private boolean hasOverTranslate;
    private boolean isInit;
    private boolean mAdjustViewBounds;
    private boolean isZoomIn;

    private boolean imgLargeWidth;
    private boolean imgLargeHeight;

    private float mDegrees;
    private float mScale = 1.0f;
    private int mTranslateX;
    private int mTranslateY;

    private float mHalfBaseRectWidth;
    private float mHalfBaseRectHeight;

    private RectF mWidgetRect = new RectF();
    private RectF mBaseRect = new RectF();
    private RectF mImgRect = new RectF();
    private RectF mTmpRect = new RectF();
    private RectF mCommonRect = new RectF();

    private PointF mScreenCenter = new PointF();
    private PointF mScaleCenter = new PointF();
    private PointF mRotateCenter = new PointF();

    private Transform mTranslate = new Transform();

    private RectF mClip;
    private Runnable mCompleteCallBack;

    private OnLongClickListener mLongClick;

    public PhotoView(Context context) {
        super(context);
		super.setScaleType(ScaleType.MATRIX);
		setId(R.id.browser_widget1);
		mScaleType = ScaleType.CENTER_INSIDE;
		mDetector = new GestureDetector(getContext(), mGestureListener);
		mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
		float density = getResources().getDisplayMetrics().density;
		MAX_FLING_OVER_SCROLL = (int) (density * 30);
		MAX_OVER_RESISTANCE = (int) (density * 140);

		mAnimaDuring = ANIMA_DURING;
		mMaxScale = MAX_SCALE;
		setScaleType(ScaleType.FIT_CENTER);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mClickListener = l;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == ScaleType.MATRIX) return;
        if (scaleType != mScaleType) {
            mScaleType = scaleType;

            if (isInit) {
                initBase();
            }
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClick = l;
    }
    
    @Override
    public void setImageResource(int resId) {
        setImageDrawable(getResources().getDrawable(resId));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        if (drawable == null) {
            hasDrawable = false;
            return;
        }

        if (!hasSize(drawable))
            return;

        if (!hasDrawable) {
            hasDrawable = true;
        }

        initBase();
    }

    private boolean hasSize(Drawable d) {
        if ((d.getIntrinsicHeight() <= 0 || d.getIntrinsicWidth() <= 0)
                && (d.getMinimumWidth() <= 0 || d.getMinimumHeight() <= 0)
                && (d.getBounds().width() <= 0 || d.getBounds().height() <= 0)) {
            return false;
        }
        return true;
    }

    private static int getDrawableWidth(Drawable d) {
        int width = d.getIntrinsicWidth();
        if (width <= 0) width = d.getMinimumWidth();
        if (width <= 0) width = d.getBounds().width();
        return width;
    }

    private static int getDrawableHeight(Drawable d) {
        int height = d.getIntrinsicHeight();
        if (height <= 0) height = d.getMinimumHeight();
        if (height <= 0) height = d.getBounds().height();
        return height;
    }

    private void initBase() {
        if (!hasDrawable) return;
        if (!isKnowSize) return;

        mBaseMatrix.reset();
        mAnimaMatrix.reset();

        isZoomIn = false;

        Drawable img = getDrawable();

        int w = getWidth();
        int h = getHeight();
        int imgw = getDrawableWidth(img);
        int imgh = getDrawableHeight(img);

        mBaseRect.set(0, 0, imgw, imgh);

        // 以图片中心点居中位移
        int tx = (w - imgw) / 2;
        int ty = (h - imgh) / 2;

        float sx = 1;
        float sy = 1;

        // 缩放，默认不超过屏幕大小
        if (imgw > w) {
            sx = (float) w / imgw;
        }

        if (imgh > h) {
            sy = (float) h / imgh;
        }

        float scale_min = Math.min(sx, sy);

        mBaseMatrix.reset();
        mBaseMatrix.postTranslate(tx, ty);
        mBaseMatrix.postScale(scale_min, scale_min, mScreenCenter.x, mScreenCenter.y);

		mBaseMatrix.mapRect(mBaseRect);

        mHalfBaseRectWidth = mBaseRect.width() / 2;
        mHalfBaseRectHeight = mBaseRect.height() / 2;

        mScaleCenter.set(mScreenCenter);
        mRotateCenter.set(mScaleCenter);

        executeTranslate();
	
		//initCenterCrop();
		initCenterInside();
		
		int preset = IBC.getPresetZoomLevel();
		float scale=preset==0?0:(preset==1?IBC.doubleClickZoomLevel1:IBC.doubleClickZoomLevel2);
		if(scale>1){
			mAnimaMatrix.postScale(scale, scale, mScreenCenter.x, mScreenCenter.y);
			executeTranslate();
			mScale=scale;
			isZoomIn=true;
		}
		
        int x_prefer = IBC.getPresetZoomAlignment();
        if(x_prefer!=0 && scale>1){
			tx = (int) ((mWidgetRect.width() - mImgRect.width()) / 2);
			ty = - (int) ((mWidgetRect.height() - mImgRect.height()) / 2);
			int pad = 0;
			if(IBC.doubleClickPresetXOffset!=0){
				pad = (int) (getResources().getDisplayMetrics().widthPixels*IBC.doubleClickPresetXOffset);
			}
			tx += pad;
			if(x_prefer==1) tx=-tx;
			mTranslateX = tx;
			mTranslateY = ty;
			mAnimaMatrix.postTranslate(tx, ty);
			executeTranslate();
		}

        isInit = true;
    }
    
    private void initCenterCrop() {
        if (mImgRect.width() < mWidgetRect.width() || mImgRect.height() < mWidgetRect.height()) {
			float scaleX = mWidgetRect.width() / mImgRect.width();
			float scaleY = mWidgetRect.height() / mImgRect.height();
			mScale = scaleX > scaleY ? scaleX : scaleY;
            mAnimaMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y);
            executeTranslate();
            resetBase();
        }
    }

    private void initCenterInside() {
        if (mImgRect.width() > mWidgetRect.width() || mImgRect.height() > mWidgetRect.height()) {
            float scaleX = mWidgetRect.width() / mImgRect.width();
            float scaleY = mWidgetRect.height() / mImgRect.height();

            mScale = scaleX < scaleY ? scaleX : scaleY;

            mAnimaMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y);

            executeTranslate();
            resetBase();
        }
    }

    private void resetBase() {
        Drawable img = getDrawable();
        int imgw = getDrawableWidth(img);
        int imgh = getDrawableHeight(img);
        mBaseRect.set(0, 0, imgw, imgh);
        mBaseMatrix.set(mSynthesisMatrix);
        mBaseMatrix.mapRect(mBaseRect);
        mHalfBaseRectWidth = mBaseRect.width() / 2;
        mHalfBaseRectHeight = mBaseRect.height() / 2;
        mScale = 1;
        mTranslateX = 0;
        mTranslateY = 0;
        mAnimaMatrix.reset();
    }

    private void executeTranslate() {
        mSynthesisMatrix.set(mBaseMatrix);
        mSynthesisMatrix.postConcat(mAnimaMatrix);
        setImageMatrix(mSynthesisMatrix);

        mAnimaMatrix.mapRect(mImgRect, mBaseRect);

        imgLargeWidth = mImgRect.width() > mWidgetRect.width();
        imgLargeHeight = mImgRect.height() > mWidgetRect.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!hasDrawable) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        Drawable d = getDrawable();
        int drawableW = getDrawableWidth(d);
        int drawableH = getDrawableHeight(d);

        int pWidth = MeasureSpec.getSize(widthMeasureSpec);
        int pHeight = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width;
        int height;

        ViewGroup.LayoutParams p = getLayoutParams();

        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (p.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                width = drawableW;
            } else {
                width = pWidth;
            }
        } else {
            if (widthMode == MeasureSpec.EXACTLY) {
                width = pWidth;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = drawableW > pWidth ? pWidth : drawableW;
            } else {
                width = drawableW;
            }
        }

        if (p.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                height = drawableH;
            } else {
                height = pHeight;
            }
        } else {
            if (heightMode == MeasureSpec.EXACTLY) {
                height = pHeight;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = drawableH > pHeight ? pHeight : drawableH;
            } else {
                height = drawableH;
            }
        }

        if (mAdjustViewBounds && (float) drawableW / drawableH != (float) width / height) {

            float hScale = (float) height / drawableH;
            float wScale = (float) width / drawableW;

            float scale = hScale < wScale ? hScale : wScale;
            width = p.width == ViewGroup.LayoutParams.MATCH_PARENT ? width : (int) (drawableW * scale);
            height = p.height == ViewGroup.LayoutParams.MATCH_PARENT ? height : (int) (drawableH * scale);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        super.setAdjustViewBounds(adjustViewBounds);
        mAdjustViewBounds = adjustViewBounds;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidgetRect.set(0, 0, w, h);
        mScreenCenter.set(w / 2, h / 2);

        if (!isKnowSize) {
            isKnowSize = true;
            initBase();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mClip != null) {
            canvas.clipRect(mClip);
            mClip = null;
        }
        super.draw(canvas);
    }

	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
		final int Action = event.getActionMasked();
		if (event.getPointerCount() >= 2)
			hasMultiTouch = true;
		mDetector.onTouchEvent(event);
		mScaleDetector.onTouchEvent(event);
		if (Action == MotionEvent.ACTION_UP || Action == MotionEvent.ACTION_CANCEL)
			onUp();
		return true;
    }

    private void onUp() {
        if (mTranslate.isRuning) return;

        float scale = mScale;

		float cx = mImgRect.left + mImgRect.width() / 2 ;
		float cy = mImgRect.top + mImgRect.height() / 2 ;

        if (mScale < 1) {
            scale = 1;
            mTranslate.withScale(mScale, 1);
        } else if (mScale > mMaxScale) {
            //scale = mMaxScale;
            //mTranslate.withScale(mScale, mMaxScale);
        }


        mScaleCenter.set(cx, cy);
        mRotateCenter.set(cx, cy);

        mTranslateX = 0;
        mTranslateY = 0;

        mTmpMatrix.reset();
        mTmpMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top);
        mTmpMatrix.postTranslate(cx - mHalfBaseRectWidth, cy - mHalfBaseRectHeight);
        mTmpMatrix.postScale(scale, scale, cx, cy);
        mTmpMatrix.postRotate(mDegrees, cx, cy);
        mTmpMatrix.mapRect(mTmpRect, mBaseRect);

        doTranslateReset(mTmpRect, false);//mScale > mMaxScale
        mTranslate.start();
    }

    private void doTranslateReset(RectF imgRect, boolean isShrinking) {
        int tx = 0;
        int ty = 0;

        if (imgRect.width() <= mWidgetRect.width()) {
            if (!isImageCenterWidth(imgRect))
                tx = -(int) ((mWidgetRect.width() - imgRect.width()) / 2 - imgRect.left);
        } else {
			//CMN.Log(imgRect.left, mWidgetRect.left, "||", imgRect.right, mWidgetRect.right);
			float widthDelta=0;
			if(isShrinking){
				//CMN.Log("keep pos shrinking>>>", imgRect.left > mWidgetRect.left, imgRect.right < mWidgetRect.right);
				tx = (int) (((imgRect.left+imgRect.width()/2)-(mWidgetRect.left+mWidgetRect.width()/2))*(1-mMaxScale/mScale));
			}else {
				if (imgRect.left > mWidgetRect.left) {
					tx = (int) (imgRect.left - mWidgetRect.left);
				} else if (imgRect.right < mWidgetRect.right) {
					tx = (int) (imgRect.right - mWidgetRect.right);
				}
			}
        }

		//tx=0;
        if (imgRect.height() <= mWidgetRect.height()) {
            if (!isImageCenterHeight(imgRect))
                ty = -(int) ((mWidgetRect.height() - imgRect.height()) / 2 - imgRect.top);
        } else {
			if(isShrinking){
				ty = (int) (((imgRect.top+imgRect.height()/2)-(mWidgetRect.top+mWidgetRect.height()/2))*(1-mMaxScale/mScale));
			}else{
				if (imgRect.top > mWidgetRect.top) {
					ty = (int) (imgRect.top - mWidgetRect.top);
				} else if (imgRect.bottom < mWidgetRect.bottom) {
					ty = (int) (imgRect.bottom - mWidgetRect.bottom);
				}
			}
        }

        if (tx != 0 || ty != 0) {
            if (!mTranslate.mFlingScroller.isFinished()) mTranslate.mFlingScroller.abortAnimation();
            mTranslate.withTranslate(mTranslateX, mTranslateY, -tx, -ty);
        }
    }

    private boolean isImageCenterHeight(RectF rect) {
        return Math.abs(Math.round(rect.top) - (mWidgetRect.height() - rect.height()) / 2) < 1;
    }

    private boolean isImageCenterWidth(RectF rect) {
        return Math.abs(Math.round(rect.left) - (mWidgetRect.width() - rect.width()) / 2) < 1;
    }

    private ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
			//CMN.Log("scale suck", scaleFactor);
            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                return false;
            mScale *= scaleFactor;
            isZoomIn=mScale>1;
//            mScaleCenter.set(detector.getFocusX(), detector.getFocusY());
            mAnimaMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            executeTranslate();
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    };

    private float resistanceScrollByX(float overScroll, float detalX) {
		return detalX * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / (float) MAX_OVER_RESISTANCE);
    }

    private float resistanceScrollByY(float overScroll, float detalY) {
		return detalY * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / (float) MAX_OVER_RESISTANCE);
    }

    /**
     * 匹配两个Rect的共同部分输出到out，若无共同部分则输出0，0，0，0
     */
    private void mapRect(RectF r1, RectF r2, RectF out) {
        float l, r, t, b;
        l = r1.left > r2.left ? r1.left : r2.left;
        r = r1.right < r2.right ? r1.right : r2.right;
        if (l > r) {
            out.set(0, 0, 0, 0);
            return;
        }
        t = r1.top > r2.top ? r1.top : r2.top;
        b = r1.bottom < r2.bottom ? r1.bottom : r2.bottom;
        if (t > b) {
            out.set(0, 0, 0, 0);
            return;
        }
        out.set(l, t, r, b);
    }

    private void checkRect() {
        if (!hasOverTranslate) {
            mapRect(mWidgetRect, mImgRect, mCommonRect);
        }
    }

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            if (mLongClick != null) {
                mLongClick.onLongClick(PhotoView.this);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            hasOverTranslate = false;
            hasMultiTouch = false;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //if(true) return true;
        	if (hasMultiTouch) return false;
            if (!imgLargeWidth && !imgLargeHeight) return false;
            if (mTranslate.isRuning) return false;

            float vx = velocityX;
            float vy = velocityY;

            if (Math.round(mImgRect.left) >= mWidgetRect.left || Math.round(mImgRect.right) <= mWidgetRect.right) {
                vx = 0;
            }
            if (Math.round(mImgRect.top) >= mWidgetRect.top || Math.round(mImgRect.bottom) <= mWidgetRect.bottom) {
                vy = 0;
            }
			//CMN.Log("onFling", vx, vy,e1.getX()-e2.getX(), e1.getY()-e2.getY());
            if(Math.abs(vx)<10&&Math.abs(vy)<10)
            	return true;

            doTranslateReset(mImgRect, false);

            mTranslate.withFling(vx, vy);
            mTranslate.start();
            // onUp(e2);
            return true;//super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mTranslate.isRuning) {
                mTranslate.stop();
            }
            //CMN.Log(canScrollHorizontallySelf(distanceX), "canScrollHorizontallySelf", distanceX);
            if (canScrollHorizontallySelf(distanceX)) {
				//todo deprecate those code
//                if (distanceX < 0 && mImgRect.left - distanceX > mWidgetRect.left)
//                    distanceX = mImgRect.left;
//                if (distanceX > 0 && mImgRect.right - distanceX < mWidgetRect.right)
//                    distanceX = mImgRect.right - mWidgetRect.right;

                mAnimaMatrix.postTranslate(-distanceX, 0);
                mTranslateX -= distanceX;
            }
            else if (imgLargeWidth || hasMultiTouch || hasOverTranslate) {
                checkRect();
                if (!hasMultiTouch) {
                    if (distanceX < 0 && mImgRect.left - distanceX > mCommonRect.left)
                        distanceX = resistanceScrollByX(mImgRect.left - mCommonRect.left, distanceX);
                    if (distanceX > 0 && mImgRect.right - distanceX < mCommonRect.right)
                        distanceX = resistanceScrollByX(mImgRect.right - mCommonRect.right, distanceX);
                }

                mTranslateX -= distanceX;
                mAnimaMatrix.postTranslate(-distanceX, 0);
                hasOverTranslate = true;
            }

            if (canScrollVerticallySelf(distanceY)) {
                if (distanceY < 0 && mImgRect.top - distanceY > mWidgetRect.top)
                    distanceY = mImgRect.top;
                if (distanceY > 0 && mImgRect.bottom - distanceY < mWidgetRect.bottom)
                    distanceY = mImgRect.bottom - mWidgetRect.bottom;

                mAnimaMatrix.postTranslate(0, -distanceY);
                mTranslateY -= distanceY;
            }
            else if (imgLargeHeight || hasOverTranslate || hasMultiTouch) {
                checkRect();
                if (!hasMultiTouch) {
                    if (distanceY < 0 && mImgRect.top - distanceY > mCommonRect.top)
                        distanceY = resistanceScrollByY(mImgRect.top - mCommonRect.top, distanceY);
                    if (distanceY > 0 && mImgRect.bottom - distanceY < mCommonRect.bottom)
                        distanceY = resistanceScrollByY(mImgRect.bottom - mCommonRect.bottom, distanceY);
                }

                mAnimaMatrix.postTranslate(0, -distanceY);
                mTranslateY -= distanceY;
                hasOverTranslate = true;
            }

            executeTranslate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mClickListener != null) {
				mClickListener.onClick(PhotoView.this);
				return true;
			}
			return false;
		}

		@Override
        public boolean onDoubleTap(MotionEvent e) {
            mTranslate.stop();

            float from = mScale;// = mImgRect.width()/mBaseRect.width();
            float to = 1;

            float imgcx = mImgRect.centerX();
            float imgcy = mImgRect.centerY();
			
			IBC.doubleClickZoomLevel1=2;
			IBC.doubleClickZoomLevel2=0;
			//CMN.Log("imgcx", imgcx, imgcy);
			//CMN.Log("mImgRect::", mImgRect.left, mImgRect.top, mImgRect.width(), mImgRect.height());
			CMN.Log("mImgRect::", mImgRect.centerX(), mImgRect.centerY());
			CMN.Log("Event::", e.getX(), e.getY(), e.getRawX(), e.getRawY());
			CMN.Log("Translation::", mTranslateX, mTranslateY);
            if (isZoomIn) {
                if(IBC.doubleClickZoomLevel2>0){
					if(IBC.doubleClickZoomLevel2>IBC.doubleClickZoomLevel1){
						//if(mTmpRect.width()/mBaseRect.width()+0.001<IBC.doubleClickZoomLevel2)
						if(mScale+0.2<IBC.doubleClickZoomLevel2)
						{
							if(true){
								
								//return false;
							}
							
							
							to = IBC.doubleClickZoomLevel2;
							imgcx=e.getRawX();
							imgcy=e.getRawY();
							mTranslateX = (int) ((imgcx-mImgRect.centerX())*to/from-imgcx);
							mTranslateY = (int) ((imgcy-mImgRect.centerY())*to/from-imgcy);
							
							
							
							mTranslateX = 0;
							mTranslateY = 0;
							imgcx = mImgRect.centerX()+500;
							imgcy = mImgRect.centerY();
							//mTranslateY = (int) (mImgRect.centerY());
							mTranslateX = 500;
							
							//duiduidui
							mTranslateX = (int) ((e.getRawX()-mImgRect.centerX())/from);
							mTranslateY = (int) ((e.getRawY()-mImgRect.centerX())/from);
							imgcx = mImgRect.centerX() + mTranslateX;
							imgcy = mImgRect.centerY() + mTranslateY;
							
						}
//						else if(IBC.getDoubleClick12()){
//						}
					}
				}
            } else {
				to = IBC.doubleClickZoomLevel1;
				mTranslateX = 0;
				mTranslateY = 0;
				imgcx=e.getX();
				imgcy=e.getY();
            }
			
			CMN.Log("onDoubleTap", "mScale", mScale, "to", to, IBC.doubleClickZoomLevel2, mTmpRect.left, mTmpRect.top);
			
			
			mRotateCenter.set(mImgRect.centerX(), mImgRect.centerY());
			mScaleCenter.set(imgcx, imgcy);
			mScale = to;
			
			
			if(true){
				mTmpMatrix.reset();
				mTmpMatrix.postTranslate(-mBaseRect.left+mRotateCenter.x-mHalfBaseRectWidth, -mBaseRect.top+mRotateCenter.y-mHalfBaseRectHeight);
				mTmpMatrix.postRotate(mDegrees, mRotateCenter.x, mRotateCenter.y);
				mTmpMatrix.postScale(to, to, mScaleCenter.x, mScaleCenter.y);
				mTmpMatrix.postTranslate(mTranslateX, mTranslateY);
				mTmpMatrix.mapRect(mTmpRect, mBaseRect);
				doTranslateReset(mTmpRect, false);
				
				isZoomIn=to>1;
				mTranslate.withScale(from, to);
				mTranslate.start();
			}

            return false;
        }
    };

    public boolean canScrollHorizontallySelf(float direction) {
        if (mImgRect.width() <= mWidgetRect.width()) return false;
//        if (direction < 0 && Math.round(mImgRect.left) - direction >= mWidgetRect.left)
//            return false;
//        if (direction > 0 && Math.round(mImgRect.right) - direction <= mWidgetRect.right)
//            return false;
        return true;
    }

    public boolean canScrollVerticallySelf(float direction) {
        if (mImgRect.height() <= mWidgetRect.height()) return false;
        if (direction < 0 && Math.round(mImgRect.top) - direction >= mWidgetRect.top)
            return false;
        if (direction > 0 && Math.round(mImgRect.bottom) - direction <= mWidgetRect.bottom)
            return false;
        return true;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (hasMultiTouch) return true;
        return canScrollHorizontallySelf(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (hasMultiTouch) return true;
        return canScrollVerticallySelf(direction);
    }

    private class InterpolatorProxy implements Interpolator {

        private Interpolator mTarget;

        private InterpolatorProxy() {
            mTarget = new DecelerateInterpolator();
        }

        public void setTargetInterpolator(Interpolator interpolator) {
            mTarget = interpolator;
        }

        @Override
        public float getInterpolation(float input) {
            if (mTarget != null) {
                return mTarget.getInterpolation(input);
            }
            return input;
        }
    }

    private class Transform implements Runnable {

        boolean isRuning;

        OverScroller mTranslateScroller;
        OverScroller mFlingScroller;
        Scroller mScaleScroller;
        
        int mLastFlingX;
        int mLastFlingY;

        int mLastTranslateX;
        int mLastTranslateY;

        RectF mClipRect = new RectF();

        InterpolatorProxy mInterpolatorProxy = new InterpolatorProxy();

        Transform() {
            Context ctx = getContext();
            mTranslateScroller = new OverScroller(ctx, mInterpolatorProxy);
            mScaleScroller = new Scroller(ctx, mInterpolatorProxy);
            mFlingScroller = new OverScroller(ctx, mInterpolatorProxy);
        }

        void withTranslate(int startX, int startY, int deltaX, int deltaY) {
            mLastTranslateX = 0;
            mLastTranslateY = 0;
            mTranslateScroller.startScroll(0, 0, deltaX, deltaY, mAnimaDuring);
        }

        void withScale(float form, float to) {
            mScaleScroller.startScroll((int) (form * 10000), 0, (int) ((to - form) * 10000), 0, mAnimaDuring);
        }

        void withFling(float velocityX, float velocityY) {
            mLastFlingX = velocityX < 0 ? Integer.MAX_VALUE : 0;
            int distanceX = (int) (velocityX > 0 ? Math.abs(mImgRect.left) : mImgRect.right - mWidgetRect.right);
            distanceX = velocityX < 0 ? Integer.MAX_VALUE - distanceX : distanceX;
            int minX = velocityX < 0 ? distanceX : 0;
            int maxX = velocityX < 0 ? Integer.MAX_VALUE : distanceX;
            int overX = velocityX < 0 ? Integer.MAX_VALUE - minX : distanceX;

            mLastFlingY = velocityY < 0 ? Integer.MAX_VALUE : 0;
            int distanceY = (int) (velocityY > 0 ? Math.abs(mImgRect.top) : mImgRect.bottom - mWidgetRect.bottom);
            distanceY = velocityY < 0 ? Integer.MAX_VALUE - distanceY : distanceY;
            int minY = velocityY < 0 ? distanceY : 0;
            int maxY = velocityY < 0 ? Integer.MAX_VALUE : distanceY;
            int overY = velocityY < 0 ? Integer.MAX_VALUE - minY : distanceY;

            if (velocityX == 0) {
                maxX = 0;
                minX = 0;
            }

            if (velocityY == 0) {
                maxY = 0;
                minY = 0;
            }

            mFlingScroller.fling(mLastFlingX, mLastFlingY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY, Math.abs(overX) < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL, Math.abs(overY) < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL);
        }

        void start() {
            isRuning = true;
            postExecute();
        }

        void stop() {
            removeCallbacks(this);
            mTranslateScroller.abortAnimation();
            mScaleScroller.abortAnimation();
            mFlingScroller.abortAnimation();
			onStop();
		}

        @Override
        public void run() {

            if (!isRuning) return;

            boolean endAnima = true;

            if (mScaleScroller.computeScrollOffset()) {
                mScale = mScaleScroller.getCurrX() / 10000f;
                endAnima = false;
            }

            if (mTranslateScroller.computeScrollOffset()) {
                int tx = mTranslateScroller.getCurrX() - mLastTranslateX;
                int ty = mTranslateScroller.getCurrY() - mLastTranslateY;
                mTranslateX += tx;
                mTranslateY += ty;
                mLastTranslateX = mTranslateScroller.getCurrX();
                mLastTranslateY = mTranslateScroller.getCurrY();
                endAnima = false;
            }

            if (mFlingScroller.computeScrollOffset()) {
                int x = mFlingScroller.getCurrX() - mLastFlingX;
                int y = mFlingScroller.getCurrY() - mLastFlingY;

                mLastFlingX = mFlingScroller.getCurrX();
                mLastFlingY = mFlingScroller.getCurrY();

                mTranslateX += x;
                mTranslateY += y;
                endAnima = false;
            }
            
            if (!endAnima) {
                applyAnima();
                postExecute();
            } else {
                onStop();
            }
        }
	
		private void onStop() {
			isRuning = false;
			
			// 修复动画结束后边距有些空隙，
			boolean needFix = false;
			
			if (imgLargeWidth) {
				if (mImgRect.left > 0) {
					mTranslateX -= mImgRect.left;
				} else if (mImgRect.right < mWidgetRect.width()) {
					mTranslateX -= (int) (mWidgetRect.width() - mImgRect.right);
				}
				needFix = true;
			}
			
			if (imgLargeHeight) {
				if (mImgRect.top > 0) {
					mTranslateY -= mImgRect.top;
				} else if (mImgRect.bottom < mWidgetRect.height()) {
					mTranslateY -= (int) (mWidgetRect.height() - mImgRect.bottom);
				}
				needFix = true;
			}
			
			if (needFix) {
				applyAnima();
			}
			
			invalidate();
			
			if (mCompleteCallBack != null) {
				mCompleteCallBack.run();
				mCompleteCallBack = null;
			}
		}
	
		private void applyAnima() {
            mAnimaMatrix.reset();
            mAnimaMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top);
            mAnimaMatrix.postTranslate(mRotateCenter.x, mRotateCenter.y);
            mAnimaMatrix.postTranslate(-mHalfBaseRectWidth, -mHalfBaseRectHeight);
            mAnimaMatrix.postRotate(mDegrees, mRotateCenter.x, mRotateCenter.y);
            mAnimaMatrix.postScale(mScale, mScale, mScaleCenter.x, mScaleCenter.y);
            mAnimaMatrix.postTranslate(mTranslateX, mTranslateY);
            executeTranslate();
        }


        private void postExecute() {
            if (isRuning) post(this);
        }
    }
    
    private void reset() {
        mAnimaMatrix.reset();
        executeTranslate();
        mScale = 1;
        mTranslateX = 0;
        mTranslateY = 0;
    }

}

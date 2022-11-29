package com.knziha.plod.tesseraction;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 模仿三星s7屏幕截图的编辑视图 ( Sumsung like Crop View )
 * control : *you can scale the frame by dragging the four corner,
 *            or just edit position of it's four Borders.
 *           *you can move the frame by panning on it's inner space.
 *           *you can move the backside view(mSurfaceView) by panning on the outer space.
 *           *pan && zoom simultaneously is enabled on the backside view.
 * by KnIfER on 2018/2/16.
 * Based on blog : (original author nangua)
 *    http://blog.csdn.net/qq_22770457/article/details/52006764
 * 2022.3.17 与 QRFrameView 合并
 */
public class CropView extends View {
	private static final long ANIMATION_DELAY = 25L;
	private final int SCAN_VELOCITY = 8;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private static final int POINT_SIZE = 6;
	private int density = (int) this.getResources().getDisplayMetrics().density;  //屏幕像素密度
	/** 设置方框的宽度，默认200dp */
	private float mFrameSizeX = 0;
	/** 设置方框的高度 ，默认200dp*/
	private float mFrameSizeY = 0;
	
	/** 框四边的宽度 */
	private final int mBorderStrokeWidth = 3 * density;
	/** 框四角的宽度 */
	private final int mCornerStrokeWidth = 6 * density;
	/** 框四角的高度 */
	private final int mCornerSize = 20 * density;
	
	/** 边框四个边的坐标，相对于屏幕左上角，每边只保留x, y中有效的一个坐标: <br> left (x), top (y), right (x), bottom (y) */
	public final RectF frameOffsets = new RectF();
	
	/** 视图的总宽与总高 */
	public final RectF viewFrame = new RectF();
	
	/** 是否同时允许双指分别调整边框大小  */
	public boolean b2FingerFrame = true;
	
	/** moving states flag. |0x1=InnerMoveX|  |0x2=InnerMoveY|  |0x4=OuterMove|  |0x8=双指操作|  |0x10=再初始化| |0x20=双指操作，免疫单指| <br>
	 **/
	private int MoveStates;
	
	/** 0=不显示，1=总是显示，2=触摸时显示 */
	private int mGridShowType = 0;
	
	/** 外部触摸处理方式 0=不处理，1=移动/缩放背后的view，2=拖动边框的某一边，3=移动边框 */
	private int tOutMode = 1;
	
	/** true=框内触摸同框外，false=框内移框 */
	private boolean tInOut = false;
	
	/** 双指操作时，储存第二指的ID。安卓多点触摸，落点 index 会变化，而 ID 不变。  */
	private int PinchTouchId;
	
	public View mView;
	
	Paint framePaint;
	Paint frameEraser;
	private final Paint rectPaint;
	
	private int resultPointColor; // 特征点的颜色
//	private final List<ResultPoint> possibleResultPoints = new ArrayList<>(5);
	private ArrayList<Rect> textRects_ = new ArrayList<>(1);
	public List<Rect> textRects;
//	private final List<ResultPoint> lastPossibleResultPoints = new ArrayList<>(5);
//	private ResultPointCollector mResultPointCollector;
	
	
	
	private final Paint paint;
	Bitmap laserBit;
	private int laserTop;
	private RectF laserRect = new RectF();
	private int lowerLaserLimit;
	
	public boolean drawLocations;
	public boolean drawLaser;
	public boolean drawCrossSight = true;
	
	private boolean animating=false;
	private OnClickListener mClickListener;
	
	GestureDetector detector;
	
	
	/**  构造   */
	public CropView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		framePaint = new Paint();
		framePaint.setColor(Color.WHITE);
		framePaint.setAntiAlias(true);
		
		frameEraser = new Paint(framePaint);
		frameEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		frameEraser.setStyle(Paint.Style.FILL);
		
		rectPaint = new Paint();
		rectPaint.setStyle(Paint.Style.STROKE);
		rectPaint.setStrokeWidth(1.3f);
		rectPaint.setColor(Color.WHITE);
		//rectPaint.setAlpha(CURRENT_POINT_OPACITY);
		
		Resources resources = getResources();
		resultPointColor = 0xFFFF25F;
		laserBit = BitmapFactory.decodeResource(resources, R.drawable.scan_light);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0x900f0f0f);
		paint.setAlpha(CURRENT_POINT_OPACITY);
		
		mskColor = getBackground();
		
		detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if(mClickListener!=null) {
					mClickListener.onClick(CropView.this);
				}
				return super.onSingleTapUp(e);
			}
		});
	}
	
	/**  初始化布局   */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if(changed) {
			CMN.debug("初始化布局");
			viewFrame.right = getWidth();
			viewFrame.bottom = getHeight();
			//初始化四个边的坐标
			if(frameOffsets.width()<=0) {
				float mFrameSize = mFrameSizeX<=0?200 * density :mFrameSizeX;
				frameOffsets.left = (viewFrame.right - mFrameSize) / 2;
				frameOffsets.right = (viewFrame.right + mFrameSize) / 2;
			}
			if(frameOffsets.height()<=0) {
				float mFrameSize = mFrameSizeY<=0?200 * density :mFrameSizeY;
				frameOffsets.top = (viewFrame.bottom - mFrameSize) / 2;
				frameOffsets.bottom = (viewFrame.bottom + mFrameSize) / 2;
			}
		}
	}
	
	public void setViewDelegation(View viewToManipulate) {
		mView = viewToManipulate;
		if(viewToManipulate!=null) {
			viewToManipulate.setPivotX(0);
			viewToManipulate.setPivotY(0);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		RectF frame = cropping?frameOffsets:viewFrame;
		if(cropping) {
			framePaint.setStrokeWidth(fixed?mBorderStrokeWidth:mCornerStrokeWidth);
			framePaint.setStyle(Paint.Style.STROKE);
			// 画边框
			canvas.drawRect(frameOffsets, framePaint);
			canvas.drawRect(frameOffsets, frameEraser);
			// 画激光
			long delay = ANIMATION_DELAY;
			if(drawLaser) {
				// laser blade hen hen hah hyi！
				lowerLaserLimit = (int) (frame.bottom-15);
				drawLaser(canvas, frame);
				if(laserTop==lowerLaserLimit) {
					laserTop =(int) frame.top;
					//delay=350L;
					delay=650L;
				}
			} else {
				delay = 160L;
			}
//			if(drawLocations && delay<300) {
//				// 绘制点云
//				synchronized (possibleResultPoints) {
//					if (!lastPossibleResultPoints.isEmpty()) {
//						drawPointsCloud(frame, canvas, lastPossibleResultPoints, POINT_SIZE / 2.0f);
//						lastPossibleResultPoints.clear();
//					}
//					if (!possibleResultPoints.isEmpty()) {
//						drawPointsCloud(frame, canvas, possibleResultPoints, POINT_SIZE);
//						lastPossibleResultPoints.addAll(possibleResultPoints);
//						possibleResultPoints.clear();
//					}
//				}
//			}
			if(animating&&(drawLocations||drawLaser)) {
				// Request another update at the animation interval
				// , but only repaint the laser line, not the entire viewfinder mask.
				postInvalidateDelayed(delay, (int)(frame.left - POINT_SIZE),
						(int)(frame.top - POINT_SIZE), (int)(frame.right + POINT_SIZE),
						(int)(frame.bottom + POINT_SIZE));
			}
		}
		
		// 画词框
		if(drawLocations && textRects !=null) {
			if(!cropping) {
				viewFrame.left = mView.getTranslationX();
				viewFrame.top = mView.getTranslationY();
			}
			for (Rect rect : textRects) {
				float scale = /*a.scale * */mView.getScaleY();
				canvas.drawRect(
						frame.left+rect.left*scale
						,frame.top+rect.top*scale
						,frame.left+rect.right*scale
						,frame.top+rect.bottom*scale
						, rectPaint);
			}
		}
		
		if(!cropping) return;
		
		final int temp1 = (mCornerStrokeWidth - mBorderStrokeWidth) / 2;
		float x,y;
		// 画准星
		if(drawCrossSight) {
			framePaint.setStrokeWidth(mCornerStrokeWidth/3);
			int tmp = mCornerSize / 2;
			x = frameOffsets.left + frameOffsets.width() / 2 - tmp;
			y = frameOffsets.top + frameOffsets.height() / 2;
			canvas.drawLine(x, y, x+mCornerSize, y, framePaint);
			x += tmp;
			y -= tmp;
			canvas.drawLine(x, y, x, y+mCornerSize, framePaint);
		}
		// 画角
		if(!fixed) {
			final int temp2 = (mCornerStrokeWidth + mBorderStrokeWidth) / 2;
			final int temp3 = temp1-temp2;
			float sizeX=mCornerSize-temp1,sizeY=temp2+sizeX;
			framePaint.setStrokeWidth(mCornerStrokeWidth);
			//左上横
			x=frameOffsets.left;
			y=frameOffsets.top-temp1;
			canvas.drawLine(x, y, x+sizeX, y, framePaint);
			//左上竖
			y += temp3;
			canvas.drawLine(x-temp1, y, x-temp1, y+sizeY, framePaint);
			//左下横
			y=frameOffsets.bottom + temp1;
			canvas.drawLine(x, y, x+sizeX, y, framePaint);
			//左下竖
			y -= temp3;
			canvas.drawLine(x-temp1, y, x-temp1, y-sizeY, framePaint);
			//右上横
			x=frameOffsets.right;
			y=frameOffsets.top - temp1;
			canvas.drawLine(x, y, x-sizeX, y, framePaint);
			//右上竖
			y += temp3;
			canvas.drawLine(x+temp1, y, x+temp1, y+sizeY, framePaint);
			//右下横
			y=frameOffsets.bottom + temp1;
			canvas.drawLine(x, y, x-sizeX, y, framePaint);
			//右下竖
			y -= temp3;
			canvas.drawLine(x+temp1, y, x+temp1, y-sizeY, framePaint);
		}
		
		// 画格子，共四根线
		if (mGridShowType==1 || mGridShowType==2&&MoveStates!=0) {
			framePaint.setStrokeWidth(1);
			framePaint.setStyle(Paint.Style.STROKE);
			float temp2 = frameOffsets.width() / 3;
			// 用temp1空出一些空间，网格边缘没有顶着边框。
			//竖线
			canvas.drawLine(frameOffsets.left + temp2 , frameOffsets.top + temp1
					, frameOffsets.left + temp2 , frameOffsets.bottom - temp1
					, framePaint);
			canvas.drawLine(frameOffsets.right - temp2 , frameOffsets.top + temp1
					, frameOffsets.right - temp2 , frameOffsets.bottom - temp1
					, framePaint);
			//横线
			temp2 = frameOffsets.height() / 3;
			canvas.drawLine(frameOffsets.left + temp1 , frameOffsets.top + temp2
					, frameOffsets.right - temp1 , frameOffsets.top + temp2
					, framePaint);
			canvas.drawLine(frameOffsets.left + temp1 , frameOffsets.bottom - temp2
					, frameOffsets.right - temp1 , frameOffsets.bottom - temp2
					, framePaint);
		}
		
	}
	
	/** 上次按下的X、Y位置 */
	float lastX, lastY, orgX, orgY;
	
	/** 用户按下的点
	 * 点阵示意： <br>
	 * 0   1 <br>
	 * 2   3  <br>
	 * <b>或者</b>用户按下的边 4=left, 5=top, 6=right, 7=bottom*/
	int activePntBdr = -1;
	
	/** 第二指操作的角点或边。see {@link #activePntBdr} */
	int activePntBdr2 = -1;
	
	float x2, y2, lastX2, lastY2;
	
	float baseDist=-1;
	float scaleT=1,baseScaleT=1;
	float mPinchOrgX = -1, mPinchOrgY = -1;
	float baseViewX, baseViewY,w,h;
	float mPinchPosX, mPinchPosY;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pc = event.getPointerCount()
				, pix = event.getActionIndex()
				, pid = event.getPointerId(pix);
		float x = event.getX(), y = event.getY();
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:{
				orgX = lastX = x;
				orgY = lastY = y;
				int touchMode = tOutMode;
				boolean fnc = fixed || !cropping;
				if(fnc) {
					touchMode = 1;
				} else {
					if(touchMode==1 && mView ==null)
						touchMode = 3;
					//检查内移动、外移动
					if(x>= frameOffsets.left && x<= frameOffsets.right)
						MoveStates|=0x1;
					if(y>= frameOffsets.top && y<= frameOffsets.bottom)
						MoveStates|=0x2;
				}
				if(fnc || (MoveStates&0x3)!=0x3 &&
						( x<= frameOffsets.left
								|| x>= frameOffsets.right
								|| y<= frameOffsets.top
								|| y>= frameOffsets.bottom )) {
					if(touchMode==3) {
						MoveStates|=0x3;
					} else if(touchMode!=0){
						// 在框外
						MoveStates|=0x4;
						if(mView !=null) {
							baseViewX = mView.getTranslationX();
							baseViewY = mView.getTranslationY();
						}
						if(mGridShowType==2) {
							invalidate();
						}
					}
				}
				if(fnc) {
					activePntBdr = -1;
				}
				else {
					activePntBdr = getActivePntBdr(MoveStates, x, y);
					if(activePntBdr==-1 && (MoveStates&0x4)!=0 && touchMode==2) {
						if((MoveStates&0x1)!=0) {
							if(y<=frameOffsets.top) {
								activePntBdr = 5;
							} else if(y>=frameOffsets.bottom) {
								activePntBdr = 7;
							}
						} else if((MoveStates&0x2)!=0) {
							if(x<=frameOffsets.left) {
								activePntBdr = 4;
							} else if(x>=frameOffsets.right) {
								activePntBdr = 6;
							}
						}
						if(activePntBdr==-1) {
							float signXY = Math.min(3, Math.signum(x - frameOffsets.centerX()) + Math.signum(y - frameOffsets.centerY()) + 2);
							if(signXY==2 && frameOffsets.centerY()>y) {
								signXY--;
							}
							activePntBdr = (int) signXY;
							MoveStates&=~0x4;
						}
					}
				}
				if(tInOut && activePntBdr==-1 && (MoveStates&0x3)==0x3) {
					//框内触摸同框外
					MoveStates &= ~0x3;
					MoveStates |= 0x4;
					if(mView !=null) {
						baseViewX = mView.getTranslationX();
						baseViewY = mView.getTranslationY();
					}
				}
			} break;
			case MotionEvent.ACTION_POINTER_UP:
				//CMN.Log("ACTION_POINTER_UP::", pc, pix, PinchTouchId, pid);
				if((MoveStates&0x8)!=0) {
					if(pc==2) {
						PinchTouchId = -1; // 双指操作切换单指操作
						MoveStates &= ~0x8;
						MoveStates |= 0x10;
					} else if(pc>2 && (pix==0 || PinchTouchId==pid)){
						PinchTouchId = -1; // 操作屏幕的手指变化了
						MoveStates |= 0x10;
					}
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				//CMN.Log("ACTION_POINTER_DOWN::", pc, pix, pid);
				if(pc==2){
					PinchTouchId = pid;
					MoveStates |= 0x8 | 0x10;
				}
				break;
			case MotionEvent.ACTION_MOVE: {
				// 触摸点起起落落时，在这里初始化第二指的ID与位置比较好。
				if((MoveStates&0x8)!=0) {
					if(PinchTouchId==-1) {
						PinchTouchId = event.getPointerId(pix=1);
					} else {
						for (int i = 0; i < event.getPointerCount(); i++) {
							if(event.getPointerId(i)==PinchTouchId) {
								pix = i;
								break;
							}
						}
					}
					x2 = event.getX(pix);
					y2 = event.getY(pix);
					if((MoveStates & 0x10) != 0) {
						if (activePntBdr>=0) {
							lastX2 = x2;
							lastY2 = y2;
							int ms=0;
							if(x>= frameOffsets.left && x<= frameOffsets.right)
								ms|=0x1;
							if(y>= frameOffsets.top && y<= frameOffsets.bottom)
								ms|=0x2;
							activePntBdr2 = getActivePntBdr(ms, x2, y2);
							if(activePntBdr2<0)
								MoveStates |= 0x20;
						}
						else activePntBdr2=-1;
					}
				}
				// 初始化移动、缩放操作的起始状态
				if((MoveStates&0x10)!=0) {
					if((MoveStates&0x8)==0&&pc==1 || (MoveStates&0x8)!=0&&pc>1) { //sanity check
						MoveStates &= ~0x10;
						baseScaleT = scaleT;
						if(mView !=null) {
							if((MoveStates&0x8)!=0&&pc>1) {
								baseScaleT = mView.getScaleX();
								CMN.Log("初始化baseScaleT::", baseScaleT);
							}
							baseViewX = mView.getTranslationX();
							baseViewY = mView.getTranslationY();
							w = mView.getWidth();
							h = mView.getHeight();
						}
						if((MoveStates&0x8)!=0) {
							mPinchOrgX = (x+x2)/2.f;
							mPinchOrgY = (y+y2)/2.f;
							baseDist = distSQ(x - x2, y - y2);
						} else {
							lastX = orgX = event.getX();
							lastY = orgY = event.getY();
						}
					} else {
						break;
					}
				}
				//CMN.Log("ACTION_MOVE::", pc, "pix::", pix, PinchTouchId);
				float offsetX = x - lastX;
				float offsetY = y - lastY;
				lastX = x;
				lastY = y;
				int actPntBdr = activePntBdr;
				int actFlag=0; // 临时标志，|0x1 继续处理双指调整边框大小|  |0x2 需要重绘|
				do {
					boolean b1F = (MoveStates&0x20)==0 || activePntBdr2>=0;
					/* 缩放边框的边与角 */
					if(b1F && activePntBdr>=0) {
						float tmpX, tmpY;
						switch (actPntBdr) {
							// 0-3 : 角缩放状态下, 按住某一个点，该点的坐标改变，其他2个点坐标跟着改变，对点坐标不变
							case 0:
								tmpX = frameOffsets.left + offsetX;
								tmpY = frameOffsets.top + offsetY;
								if (checkOffset(tmpX, frameOffsets.right - tmpX, viewFrame.right))
									frameOffsets.left = tmpX;
								if (checkOffset(tmpY, frameOffsets.bottom - tmpY, viewFrame.bottom))
									frameOffsets.top = tmpY;
								break;
							case 1:
								tmpX = frameOffsets.right + offsetX;
								tmpY = frameOffsets.top + offsetY;
								if (checkOffset(tmpX, tmpX - frameOffsets.left, viewFrame.right))
									frameOffsets.right = tmpX;
								if (checkOffset(tmpY, frameOffsets.bottom - tmpY, viewFrame.bottom))
									frameOffsets.top = tmpY;
								break;
							case 2:
								tmpX = frameOffsets.left + offsetX;
								tmpY = frameOffsets.bottom + offsetY;
								if (checkOffset(tmpX, frameOffsets.right - tmpX, viewFrame.right))
									frameOffsets.left = tmpX;
								if (checkOffset(tmpY, tmpY - frameOffsets.top, viewFrame.bottom))
									frameOffsets.bottom = tmpY;
								break;
							case 3:
								tmpX = frameOffsets.right + offsetX;
								tmpY = frameOffsets.bottom + offsetY;
								if (checkOffset(tmpX, tmpX - frameOffsets.left, viewFrame.right))
									frameOffsets.right = tmpX;
								if (checkOffset(tmpY, tmpY - frameOffsets.top, viewFrame.bottom))
									frameOffsets.bottom = tmpY;
								break;
							// 4-7 拖动哪一条边
							case 4: {
								tmpX = frameOffsets.left + offsetX;
								if (checkOffset(tmpX, frameOffsets.right - tmpX, viewFrame.right))
									frameOffsets.left = tmpX;
							} break;
							case 5: {
								tmpY = frameOffsets.top + offsetY;
								if (checkOffset(tmpY, frameOffsets.bottom - tmpY, viewFrame.bottom))
									frameOffsets.top = tmpY;
							} break;
							case 6: {
								tmpX = frameOffsets.right + offsetX;
								if (checkOffset(tmpX, tmpX - frameOffsets.left, viewFrame.right))
									frameOffsets.right = tmpX;
							} break;
							case 7: {
								tmpY = frameOffsets.bottom + offsetY;
								if (checkOffset(tmpY, tmpY - frameOffsets.top, viewFrame.bottom))
									frameOffsets.bottom = tmpY;
							} break;
						}
						if (b2FingerFrame && (MoveStates & 0x8) != 0
								&& activePntBdr2>=0
								&& (actFlag & 0x1) == 0
						) { // 再来一次！
							actPntBdr = activePntBdr2;
							offsetX = x2 - lastX2;
							offsetY = y2 - lastY2;
							lastX2 = x2;
							lastY2 = y2;
							actFlag |= 0x1;
							continue;
						}
						if (mListener != null) //通知回调接口
							mListener.onCropFrameChanged(this, true);
						actFlag |= 0x2;
					}
					/* 两指缩放 ( Pinch ) */
					else if ((MoveStates&0x8)!=0){
						if(pc <=1 || pix >1) break;
						// 新的两点的距离^2
						float Dist = distSQ(x - x2, y - y2);
						if (Dist - baseDist >= 100 || Dist - baseDist <= -100) {
							scaleT = Dist / baseDist * baseScaleT;
						}
						//新的两点间中点
						mPinchPosX = (x+x2)/2.f ;// 计算两点的中点
						mPinchPosY = (y+y2)/2.f ;// 计算两点的中点
						if(mView !=null) {
							mView.setTranslationX(baseViewX + mPinchPosX - mPinchOrgX +(scaleT/baseScaleT-1)* baseViewX -(scaleT/baseScaleT-1)* mPinchOrgX);
							mView.setTranslationY(baseViewY + mPinchPosY - mPinchOrgY +(scaleT/baseScaleT-1)* baseViewY -(scaleT/baseScaleT-1)* mPinchOrgY);
							mView.setScaleX(scaleT); // infinity
							mView.setScaleY(scaleT);
						}
					}
					/* 框内移动 */
					else if(b1F && (MoveStates&0x3)==0x3){
						//防止溢出
						if(frameOffsets.top + offsetY<0 || frameOffsets.bottom + offsetY> viewFrame.bottom)
							offsetY=0;
						if(frameOffsets.left + offsetX<0 || frameOffsets.right + offsetX> viewFrame.right)
							offsetX=0;
						//更新四个边的坐标
						frameOffsets.left += offsetX;
						frameOffsets.right += offsetX;
						frameOffsets.top += offsetY;
						frameOffsets.bottom += offsetY;
						if(mListener !=null) //通知回调接口
							mListener.onCropFrameChanged(this, false);
						actFlag |= 0x2;
					}
					/* 框外移动 */
					else if((!b1F || (MoveStates&0x4)!=0) && mView !=null){
						mView.setTranslationX(baseViewX + event.getX() - orgX);
						mView.setTranslationY(baseViewY + event.getY() - orgY);
						if(textRects!=null)
							actFlag |= 0x2;
					}
					break;
				} while ((actFlag&0x1)!=0);
				if((actFlag&0x2)!=0) {
					invalidate();
				}
			} break;
			case MotionEvent.ACTION_UP:
				MoveStates = 0;
				if(mGridShowType==2) {
					invalidate();
				}
				if(mClickListener!=null) {
					mClickListener.onClick(CropView.this);
				}
				break;
		}
		return true;
	}
	
	private boolean checkOffset(float tmpValue, float deviation, float maxValue) {
		return deviation > mCornerSize *2 && tmpValue>0 && tmpValue < maxValue;
	}
	
	private double dist(float x, float y) {
		return Math.sqrt(x*x+y*y);
	}
	
	private float distSQ(float x, float y) {
		return x*x+y*y;
	}
	
	/**
	 * 判断按下的点在圆圈内
	 * 点阵示意： <br>
	 * 0   1 <br>
	 * 2   3 <br>
	 * <br> <b>或者</b>检查是否在操作某条边
	 * <br> see {@link #activePntBdr}
	 * @param x 按下的X坐标
	 * @param y 按下的Y坐标
	 * @return 返回按到的是哪个角点或边, 没有则返回-1
	 */
	private int getActivePntBdr(int moveStates,float x, float y) {
		// 落于方框的中点圆免疫边缩放、角缩放。(方框过小时有效)
		if (mCornerSize-1.5f >= dist(x- frameOffsets.centerX(), y- frameOffsets.centerY())) {
			return -100;
		}
		// 角
		float theta = mCornerSize*1.25f;
		if(theta >= dist(x- frameOffsets.left, y- frameOffsets.top)) {
			return 0;
		}
		if(theta >= dist(x- frameOffsets.right, y- frameOffsets.top)) {
			return 1;
		}
		if(theta >= dist(x- frameOffsets.left, y- frameOffsets.bottom)) {
			return 2;
		}
		if(theta >= dist(x- frameOffsets.right, y- frameOffsets.bottom)) {
			return 3;
		}
		theta = mCornerSize;
		float theta1 = theta*2/3;
		// 边
		if((moveStates&0x1)!=0) {
			if(theta + (frameOffsets.top<theta1?theta1:0)  >= Math.abs(frameOffsets.top-y))
				return 1+4;
			else if(theta + (frameOffsets.bottom+theta1>getHeight()?theta1:0)  >= Math.abs(frameOffsets.bottom-y))
				return 3+4;
		}
		if((moveStates&0x2)!=0) {
			if(theta + (frameOffsets.left<theta1?theta1:0) >= Math.abs(frameOffsets.left-x))
				return 0+4;
			else if(theta + (frameOffsets.right+theta1>getWidth()?theta1:0) >= Math.abs(frameOffsets.right-x))
				return 2+4;
		}
		return -1;
	}
	
	int mPreset = -1;
	
	private boolean fixed = true;
	private boolean cropping = true;
	private Drawable mskColor;
	
	/** 0=static ocr; 1=dynamic ocr; 2=static qr; 3=dynamic qr;  */
	public void preset(int preset) {
		if(mPreset!=preset) {
			mPreset = preset;
			fixed = preset==3;
			if(preset==0 || preset==1) {
				drawLaser = false;
				drawCrossSight = true;
				drawLocations = true;
			}
			if(preset==2) {
				drawLaser = false;
				drawCrossSight = false;
				drawLocations = false;
			}
			if(preset==3) {
				drawLaser = true;
				drawCrossSight = false;
				drawLocations = true;
			}
			invalidate();
		}
		CMN.Log("preset::", preset, fixed);
	}
	
	public void setCropping(boolean cropping) {
		if (this.cropping != cropping) {
			this.cropping = cropping;
			setBackground(cropping?mskColor:null);
		}
	}
	
	public boolean isCropping() {
		return cropping;
	}
	
	public void setTextRect(Rect rc) {
		if(rc==null) {
			textRects = null;
		} else {
			textRects_.clear();
			textRects_.add(rc);
			textRects = textRects_;
			rectPaint.setStyle(Paint.Style.FILL);
			rectPaint.setColor(0x55FFFF00);
		}
		postInvalidate();
	}
	
	public interface CropViewListener {
		void onCropFrameChanged(CropView cropView, boolean resize);
	}
	
	public CropViewListener mListener;
	
	public void setCropViewListener(CropViewListener listener) {
		this.mListener = listener;
	}
	
	public RectF getFrameRect() {
		return frameOffsets;
	}
	
	/** 0=不显示，1=总是显示，2=触摸时显示 */
	public void setGridShowType(int showType) {
		this.mGridShowType = showType;
		invalidate();
	}
	
	/** 外部触摸处理方式 0=不处理，1=移动/缩放背后的view，2=拖动边框的某一边，3=移动边框 */
	public void setOutsideTouchMode(int mode) {
		this.tOutMode = mode;
	}
	
	public void pause() {
		animating=false;
	}
	
	public void suspense() {
		animating=false;
		invalidate();
	}
	
	public void resume() {
		animating=true;
		if(drawLaser||drawLocations) {
			invalidate();
		}
	}
	
//	public ResultPointCallback getPointsCollector() {
//		if(mResultPointCollector==null) {
//			mResultPointCollector=new ResultPointCollector(this);
//		}
//		return mResultPointCollector;
//	}
//
//	public void addPossibleResultPoint(ResultPoint point) {
//		synchronized (possibleResultPoints) {
//			possibleResultPoints.add(point);
//			int size = possibleResultPoints.size();
//			if (size > MAX_RESULT_POINTS) {
//				possibleResultPoints.subList(0, size - MAX_RESULT_POINTS / 2).clear();
//			}
//		}
//	}
	
//	static class ResultPointCollector implements ResultPointCallback {
//		public final WeakReference<CropView> view;
//
//		public ResultPointCollector(CropView view) {
//			this.view = new WeakReference<>(view);
//		}
//
//		@Override
//		public void foundPossibleResultPoint(ResultPoint point) {
//			CropView view=this.view.get();
//			if(view!=null) {
//				view.addPossibleResultPoint(point);
//			}
//		}
//	}
//
	//private final Paint pointpainter;
//	private void drawPointsCloud(RectF frame, Canvas canvas, List<ResultPoint> pointsCloud, float radius) {
//		float frameHeightPlusLeft = frame.left+frame.height();
//		for (ResultPoint point : pointsCloud) {
////			float x = point.getX() * a.scale;
////			float y = point.getY() * a.scale;
////			if(a.isPortrait) {
////				canvas.drawCircle(frameHeightPlusLeft-y, frame.top+x, radius, pointpainter);
////			} else {
////				canvas.drawCircle(frame.left+x, frame.top+y, radius, pointpainter);
////			}
//		}
//	}
	
	private void drawLaser(Canvas canvas, RectF frame) {
		//CMN.Log("drawScanLight", scanLineTop);
		if(!animating) {
			laserTop =(int) (frameOffsets.top+frameOffsets.height()/2);
		}
		if (laserTop == 0) {
			laserTop = (int) frame.top;
		}
		if(animating) {
			laserTop += SCAN_VELOCITY;
		}
		if (laserTop >= lowerLaserLimit-5) {
			laserTop = lowerLaserLimit;
			return;
		}
		laserRect.set(frame.left, laserTop, frame.right, laserTop + 30);
		canvas.drawBitmap(laserBit, null, laserRect, paint);
	}
	
	@Override
	public void setOnClickListener(@Nullable OnClickListener listener) {
		super.setOnClickListener(this.mClickListener=listener);
	}
}
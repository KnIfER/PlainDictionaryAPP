package com.knziha.plod.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.plaindict.MainActivityUIBase;


public class RLContainerSlider extends FrameLayout{
	public boolean TurnPageSuppressed;
	public WebViewmy WebContext;
	public ViewGroup ScrollerView;
	private float density;
	private int move_index;
	private boolean bZoomOut;
	private boolean bZoomOutCompletely;
	private boolean bNoDoubleClick;
	private boolean bNoTurnPage;
	private int WebContextWidth;
	private boolean aborted;
	private float dragInitDx;
	private float abortedOffsetX;
	private float abortedOffsetY;
	private float fastTapStX;
	private float fastTapStY;
	private boolean fastTapZoom;
	private float fastTapZoomSt;
	private float minZoom;
	private float fastTapScrollX;
	private float fastTapScrollY;
	private boolean fastTapMoved;
	private float quickScaleThreshold;
	
	public RLContainerSlider(Context context) {
		this(context, null);
	}
	public RLContainerSlider(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public RLContainerSlider(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		detector = new GestureDetector(getContext(), gl);
		density = GlobalOptions.density;
		quickScaleThreshold = 20*density;
	}

	public IMPageSlider IMSlider;

	int first_touch_id=-1;

	boolean bCanSlide=true;
	private float lastX,lastY,OrgX,OrgY;
	boolean dragged;
	public boolean TurnPageEnabled=false;

	boolean isOnFlingDected;
	public boolean isOnZoomingDected;
	public static long lastZoomTime;
	GestureDetector detector;
	GestureDetector.SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(WebContext!=null && !bNoDoubleClick){
				PhotoBrowsingContext ibc = WebContext.IBC;
				float targetZoom = ibc.doubleClickZoomRatio;
				//CMN.Log("onDoubleTap::", targetZoom, WebContext.webScale/BookPresenter.def_zoom);
				int zoomInType = ibc.getDoubleTapAlignment();
				//zoomInType = 4;
				if(zoomInType<3) {
					if(WebContext.webScale/ BookPresenter.def_zoom<targetZoom){
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
							int sY = WebContext.getScrollY();
							WebContext.zoomBy(0.02f);
							WebContext.zoomBy(targetZoom);
							//((MainActivityUIBase)getContext()).showT("双击放大");
							int pad = WebContext.getWidth();
							float ratio = ibc.doubleClickXOffset;
							if(ratio<-1) ratio=-1;
							if(zoomInType==0){
								pad = (int) (ratio * pad);
							} else if(zoomInType==1){
								pad = (OrgX>pad/2?pad:0)+(int) (ratio * pad);
							} else if(zoomInType==2){
								pad = (int) (WebContext.getContentWidth()-pad*(1+ratio));
							}
							WebContext.setScrollX(pad);
							WebContext.setScrollY(sY);
						}
						else {
							WebContext.zoomIn();
							WebContext.zoomIn();
						}
					}
					else {
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
							WebContext.zoomBy(0.02f);
						else{
							WebContext.zoomOut();
							WebContext.zoomOut();
						}
					}
				}
				else if (fastTapScrollX==WebContext.getScrollX()
							&& fastTapScrollY==WebContext.getScrollY()) {
					fastTapStX = e.getX();
					fastTapStY = e.getY();
					fastTapZoomSt = WebContext.webScale;
					minZoom = Math.min(fastTapZoomSt, BookPresenter.def_zoom);
					fastTapMoved = false;
					fastTapZoom = true;
				}
				WebContext.evaluateJavascript("getSelection().empty()", null);
			}
			isOnZoomingDected=true;
			lastZoomTime=System.currentTimeMillis();
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
			//if(System.currentTimeMillis()-lastDownTime<=200) //事件老死
			if(bZoomOutCompletely && !bNoTurnPage)
			{
				if(!TurnPageEnabled || e2.getPointerCount()>1) { //todo slide on zoomed page
					return false;
				}

				float vx = velocityX / 8;

				// Y轴幅度
				if (Math.abs(e2.getRawY() - e1.getRawY()) > 35*density) {
					return true;
				}

				//x轴速度
				if (Math.abs(vx) < 3.25*50*density) {
					return true;
				}
				//CMN.Log("SimpleOnGestureListener", velocityX, vx, 3*50*density);
				if(Math.abs(velocityX/(velocityY==0?0.000001:velocityY))>1.699) {
					//CMN.show("onFling");
					isOnFlingDected=true;
					//IMSlider.startdrag(ev);
					//xxx
					/////if(IMSlider.inf!=null)
					/////	IMSlider.inf.onPreparePage(IMSlider);
					IMSlider.startdrag(e2);
					IMSlider.setAlpha(1.f);
					if(velocityX<0) {
						IMSlider.decidedDir=true;
						IMSlider.TargetX=-IMSlider.getWidth();
						IMSlider.TargetY=IMSlider.getTranslationY();
					}else {
						IMSlider.decidedDir=false;
						IMSlider.TargetX=IMSlider.getWidth();
						IMSlider.TargetY=IMSlider.getTranslationY();
					}
					dragged=false;
					IMSlider.decided=true;
					IMSlider.RePosition();
				}
			}
			return false;
		}
	};

	public boolean enabled;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(MainActivityUIBase.layoutScrollDisabled)
			MainActivityUIBase.layoutScrollDisabled=false;
		if(fastTapZoom) {
			handleFastZoom(ev);
			return fastTapZoom;
		}
		if(!TurnPageEnabled) return false;
		if(!dragged && !aborted) return true;
		int actual_index = ev.getActionIndex();
		int touch_id = ev.getPointerId(actual_index);
		int actionMasked = ev.getActionMasked();
		if(touch_id==first_touch_id && detector!=null) {
			detector.onTouchEvent(ev);
		}
		
		if(isOnFlingDected) {
			if(WebContext!=null && !bNoDoubleClick) {
				detector = new GestureDetector(getContext(), gl);
			}
			dragged=isOnFlingDected=false;
			IMSlider.decided=false;
			return true;
		}
		if(IMSlider!=null){
			switch (actionMasked) {
				case MotionEvent.ACTION_MOVE: {
					//CMN.Log("ACTION_MOVE", touch_id, actual_index);
					if(first_touch_id==-1) {
						first_touch_id=touch_id;
						OrgX = lastX = ev.getX(actual_index);
						OrgY = lastY = ev.getY(actual_index);
					}
					move_index=0;
					int pc = ev.getPointerCount();
					if(pc>1) {
						for (int i = 0; i < pc; i++) {
							if(ev.getPointerId(i)==first_touch_id){
								move_index = i;
							}
						}
					}
					float nowX = ev.getX(move_index);
					float nowY = ev.getY(move_index);
//						if(!dragged) {
//							float dx = nowX - OrgX;
//							IMSlider.OrgTX = IMSlider.getTranslationX();
//							if((dx>100||dx<-100)){
//								float dy = nowY - OrgY; if(dy==0) dy=0.1f; dx = dx/dy;
//								if(dx>1.2 || dx<=-1.2){
//									dragged=true;
//									//CMN.Log("start dragging...");
//								}
//							}
//						}
					if(dragged) {
						IMSlider.startdrag(ev);
						IMSlider.handleDrag(nowX-lastX,nowY-lastY);
						if(IMSlider.inf!=null)
							IMSlider.inf.onMoving(IMSlider.getTranslationX(),IMSlider);
//						CMN.Log("IMSlider.getTranslationX()", dragInitDx, IMSlider.getTranslationX());
						if (//Math.abs(IMSlider.getTranslationX())<3.5*GlobalOptions.density &&
								dragInitDx*IMSlider.getTranslationX()<=0
								//&& Math.abs(IMSlider.getTranslationY())<20*GlobalOptions.density
						) {
							dragged = false;
							aborted = true;
							IMSlider.decided=false;
							IMSlider.RePosition();
							first_touch_id = -1;
//							CMN.Log("abort!!!");
							ViewUtils.preventDefaultTouchEvent(this, (int)lastX, (int)lastY);
//							abortedOffsetX = WebContext.lastX-nowX;
//							abortedOffsetY = WebContext.lastY-nowY;
							if(ScrollerView!=null) {
								ev.setAction(MotionEvent.ACTION_DOWN);
								ScrollerView.dispatchTouchEvent(ev);
							}
						}
					}
					else if(aborted) {
						onInterceptTouchEvent(ev);
						if(!dragged && ScrollerView!=null) {
							//ev.setLocation(nowX/*-WebContext.getLeft()*/+abortedOffsetX, nowY/*-WebContext.getTop()*/+abortedOffsetY);
							ev.setLocation(nowX, nowY);
							ScrollerView.dispatchTouchEvent(ev);
						}
					}
					lastX = nowX;
					lastY = nowY;
				} break;
				case MotionEvent.ACTION_UP: {
					onActionUp();
				} break;
				case MotionEvent.ACTION_POINTER_UP: {
					//CMN.Log("ACTION_POINTER_UP??", touch_id, first_touch_id);
					if(touch_id==first_touch_id){
						//CMN.Log("ACTION_POINTER_UP!!!", touch_id, first_touch_id);
						first_touch_id=-1;
					}
				} break;
			}
		}
		return dragged;
	}
	
	private void handleFastZoom(MotionEvent ev) {
		int actionMasked = ev.getActionMasked();
		if (actionMasked==MotionEvent.ACTION_MOVE) {
			float rawY = ev.getY();
			float rawDist = fastTapStY - rawY;
			float dist = rawDist;
			if(dist<0) dist=-dist;
			if(!fastTapMoved){
				if(dist > quickScaleThreshold){
					dist -= quickScaleThreshold;
					fastTapStY += quickScaleThreshold*(rawDist<0?1:-1);
					fastTapMoved = true;
				}
			}
			if (fastTapMoved) { // 双击拖动，快速放大。
				float multiplier = 1 + dist / quickScaleThreshold;
				
				if(rawY < fastTapStY)
					multiplier=1/multiplier;
				
				float targetZoom = fastTapZoomSt*multiplier;
				if (targetZoom>BookPresenter.max_zoom) {
					targetZoom = BookPresenter.max_zoom;
					multiplier = targetZoom/fastTapZoomSt;
				}
				else if (targetZoom<minZoom) {
					targetZoom = minZoom;
					multiplier = targetZoom/fastTapZoomSt;
				}
				WebContext.zoomBy(targetZoom/WebContext.webScale);
				//multiplier = WebContext.webScale/fastTapZoomSt;
				WebContext.scrollTo((int) ((fastTapScrollX +fastTapStX)*multiplier - fastTapStX)
					, (int) ((fastTapScrollY +fastTapStY)*multiplier - fastTapStY));
				//CMN.Log("fastZoom::", WebContext.webScale, WebContext.getScrollX(), WebContext.getScrollY());
			}
		}
		else if (actionMasked==MotionEvent.ACTION_UP) {
			fastTapZoom = false;
			fastTapMoved = false;
			float multiplier = WebContext.webScale/fastTapZoomSt;
			WebContext.scrollTo((int) ((fastTapScrollX +fastTapStX)*multiplier - fastTapStX)
					, (int) ((fastTapScrollY +fastTapStY)*multiplier - fastTapStY));
		}
	}
	
	private void onActionUp() {
		//CMN.Log("ACTION_UP", touch_id);
		first_touch_id=-1;
		if(dragged) {
			//detector = new GestureDetector(getContext(), gl);
			dragged=false;
			IMSlider.RePosition();
		}
		checkBar();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		if (bar !=null) {
			int masked = ev.getActionMasked();
			if (masked==MotionEvent.ACTION_DOWN) {
				if(!bar.isHidden()){
					bar.isWebHeld=true;
					bar.cancelFadeOut();
				}
			}
			if (masked==MotionEvent.ACTION_UP) {
				checkBar();
			}
		}
		
		if (MainActivityUIBase.layoutScrollDisabled) {
			MainActivityUIBase.layoutScrollDisabled=false;
		}
		
		bNoTurnPage = !TurnPageEnabled || TurnPageSuppressed;
		
		if(bNoTurnPage && bNoDoubleClick) {
			return false;
		}
		
		bZoomOut = bZoomOutCompletely = true;
		
		if(WebContext!=null) {
			bZoomOut = WebContext.webScale <= BookPresenter.def_zoom;
			
			if(WebContext.AlwaysCheckRange==0) {
				WebContext.CheckAlwaysCheckRange();
			}
			
			bZoomOutCompletely = bZoomOut && (WebContext.AlwaysCheckRange==-1
					||WebContext.getScrollX()==0  && WebContext.getScrollX()+WebContext.getWidth()>=WebContextWidth);
		}
		
		
		int masked = ev.getActionMasked();
		
		int touch_id=ev.getPointerId(ev.getActionIndex());
		
		if(masked==MotionEvent.ACTION_DOWN){
			OrgX = lastX = ev.getX();
			isOnZoomingDected = false;
		}
		
		if(bNoTurnPage || touch_id==first_touch_id) {
			detector.onTouchEvent(ev);
			if(isOnFlingDected) {
				dragged=isOnFlingDected=false;
				IMSlider.decided=false;
				return true;
			}
		}
		
		if (masked==MotionEvent.ACTION_DOWN
				&& WebContext!=null
				&& WebContext.IBC.getDoubleTapAlignment()==4
				&& !bNoDoubleClick
				&& !isOnZoomingDected) {
			fastTapScrollX = WebContext.getScrollX();
			fastTapScrollY = WebContext.getScrollY();
		}
		
		if (fastTapZoom) {
			return true;
		}
		
		if(bNoTurnPage) {
			return false;
		}

		boolean ret = super.onInterceptTouchEvent(ev);

		if(!bCanSlide) {
			return ret;
		}
		if(IMSlider!=null){
			switch (masked) {
				case MotionEvent.ACTION_DOWN:
					if(WebContext!=null) {
						WebContextWidth = WebContext.getContentWidth();
					}
					isOnZoomingDected=false;
					//CMN.Log("ACTION_DOWN");
					first_touch_id=touch_id;
					detector.onTouchEvent(ev);
					OrgY = lastY = ev.getY();
					aborted = false;
				break;
				case MotionEvent.ACTION_MOVE:
					if(ev.getPointerCount()==1) {
						lastX = ev.getX();
						lastY = ev.getY();
						if (!dragged) {
							float dx = lastX - OrgX;
							if (WebContext==null
									|| (WebContext.AlwaysCheckRange==-1 && bZoomOut && (dx > 100 || dx < -100))
									|| (WebContext.AlwaysCheckRange==1||!bZoomOut)
										&& (dx > 100 && WebContext.getScrollX()==0
												|| dx < -100 && WebContext.getScrollX()+WebContext.getWidth()==WebContextWidth)) {
								dragInitDx = dx;
								float dy = lastY - OrgY;
								if (dy == 0) dy = 0.000001f;
								dx = dx / dy;
								if (dx > 1.988 || dx <= -1.988) {//3.3
									dragged = true;
									//CMN.Log("start dragging...");
								}
							}
						}
					}
				break;
				case MotionEvent.ACTION_UP:
					onActionUp();
				break;
			}
		}
		return dragged;
	}
	
	public DragScrollBar bar;
	private void checkBar() {
		if(bar !=null && !bar.isHidden()){
			bar.isWebHeld=false;
			bar.fadeOut();
		}
	}
	
	public void invalidateIBC() {
		bNoDoubleClick = WebContext==null||!WebContext.IBC.getDoubleTapZoomPage();
	}
	
	public void setIBC(WebViewmy IBCN, ViewGroup scrollerView) {
		if(WebContext!=IBCN) {
			WebContext = IBCN;
			invalidateIBC();
			if (WebContext!=null) {
				this.ScrollerView = WebContext;
			}
		}
		if(this.ScrollerView!=scrollerView && scrollerView!=null) {
			this.ScrollerView = scrollerView;
		}
	}
}
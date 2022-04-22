package com.knziha.plod.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.WebViewListHandler;


public class RLContainerSlider extends FrameLayout{
	public boolean TurnPageSuppressed;
	public WebViewListHandler weblist;
	public WebViewmy WebContext;
	private PhotoBrowsingContext pBc;
	private PhotoBrowsingContext tapCtx = SearchUI.pBc;
	public ViewGroup scrollView;
	private float density;
	private int move_index;
	private boolean bZoomOut;
	private boolean bZoomOutCompletely;
	private boolean bNoDoubleClick;
	private int WebContextWidth;
	boolean aborted;
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
	private int tapZoomV;
	private boolean nothing = true;
	
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

	public PageSlide page;

	int first_touch_id=-1;

	private float lastX,lastY,OrgX,OrgY;
	boolean dragged;
	public boolean slideTurn = false;
	public boolean tapZoom;

	boolean flingDeteced;
	/** Tap Twice Deteced */
	public boolean twiceDetected;
	public static long lastZoomTime;
	GestureDetector detector;
	GestureDetector.SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(tapZoom){
				PhotoBrowsingContext ctx = tapCtx;
				float targetZoom = ctx.tapZoomRatio;
				//CMN.Log("onDoubleTap::", targetZoom, WebContext.webScale/BookPresenter.def_zoom);
				int zoomInMode = ctx.tapAlignment();
				//zoomInType = 4;
				if(zoomInMode<3) {
					if(WebContext.webScale/ BookPresenter.def_zoom<targetZoom){
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
							int sY = WebContext.getScrollY();
							WebContext.zoomBy(0.02f);
							WebContext.zoomBy(targetZoom);
							//((MainActivityUIBase)getContext()).showT("双击放大");
							int pad = WebContext.getWidth();
							float ratio = ctx.tapZoomXOffset;
							if(ratio<-1) ratio=-1;
							if(zoomInMode==0){
								pad = (int) (ratio * pad);
							} else if(zoomInMode==1){
								pad = (OrgX>pad/2?pad:0)+(int) (ratio * pad);
							} else if(zoomInMode==2){
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
				else if (fastTapScrollX==WebContext.getScrollX() && fastTapScrollY==WebContext.getScrollY()) {
					fastTapStX = e.getX();
					fastTapStY = e.getY();
					fastTapZoomSt = WebContext.webScale;
					minZoom = Math.min(fastTapZoomSt, BookPresenter.def_zoom);
					fastTapMoved = false;
					fastTapZoom = true;
				}
				WebContext.evaluateJavascript("getSelection().empty()", null);
			}
			twiceDetected =true;
			lastZoomTime=System.currentTimeMillis();
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
			//if(System.currentTimeMillis()-lastDownTime<=200) //事件老死
			if(slideTurn && bZoomOutCompletely)
			{
				if(e2.getPointerCount()>1
					|| Math.signum(velocityX)!=Math.signum(page.getTranslationX())) {
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
					flingDeteced =true;
					//IMSlider.startdrag(ev);
					//xxx
					/////if(IMSlider.inf!=null)
					/////	IMSlider.inf.onPreparePage(IMSlider);
					page.startDrag(e2);
					page.setAlpha(1.f);
					if(velocityX<0) {
						page.decidedDir=true;
						page.TargetX=-page.getWidth();
					}else {
						page.decidedDir=false;
						page.TargetX= page.getWidth();
					}
					dragged=false;
					page.decided=true;
					page.RePosition();
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
		if(!slideTurn) return false;
		if(!dragged && !aborted) return true;
		int actual_index = ev.getActionIndex();
		int touch_id = ev.getPointerId(actual_index);
		int masked = ev.getActionMasked();
		if(touch_id==first_touch_id && detector!=null) {
			detector.onTouchEvent(ev);
		}
		
		if(flingDeteced) {
			if(tapZoom) {
				detector = new GestureDetector(getContext(), gl);
			}
			dragged= flingDeteced =false;
			page.decided=false;
			return true;
		}
		if(page !=null){
			switch (masked) {
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
						page.startDrag(ev);
						page.handleDrag(nowX-lastX,nowY-lastY);
						if(page.listener !=null)
							page.listener.onMoving(page.getTranslationX(), page);
//						CMN.Log("IMSlider.getTranslationX()", dragInitDx, IMSlider.getTranslationX());
						if (//Math.abs(IMSlider.getTranslationX())<3.5*GlobalOptions.density &&
								dragInitDx* page.getTranslationX()<=0
								//&& Math.abs(IMSlider.getTranslationY())<20*GlobalOptions.density
						) {
							dragged = false;
							aborted = true;
							page.decided=false;
							page.RePosition();
							first_touch_id = -1;
//							CMN.Log("abort!!!");
							ViewUtils.preventDefaultTouchEvent(this, (int)lastX, (int)lastY);
//							abortedOffsetX = WebContext.lastX-nowX;
//							abortedOffsetY = WebContext.lastY-nowY;
							if(scrollView !=null) {
								ev.setAction(MotionEvent.ACTION_DOWN);
								scrollView.dispatchTouchEvent(ev);
							}
						}
					}
					else if(aborted) {
						onInterceptTouchEvent(ev);
						if(!dragged && scrollView !=null) {
							//ev.setLocation(nowX/*-WebContext.getLeft()*/+abortedOffsetX, nowY/*-WebContext.getTop()*/+abortedOffsetY);
							ev.setLocation(nowX, nowY);
							scrollView.dispatchTouchEvent(ev);
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
		int masked = ev.getActionMasked();
		if (masked==MotionEvent.ACTION_MOVE) {
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
		else if (masked==MotionEvent.ACTION_UP) {
			fastTapZoom = false;
			fastTapMoved = false;
			//float multiplier = WebContext.webScale/fastTapZoomSt;
			//WebContext.scrollTo((int) ((fastTapScrollX +fastTapStX)*multiplier - fastTapStX)
			//		, (int) ((fastTapScrollY +fastTapStY)*multiplier - fastTapStY));
		}
	}
	
	private void onActionUp() {
		//CMN.Log("ACTION_UP", touch_id);
		first_touch_id=-1;
		if(dragged) {
			//detector = new GestureDetector(getContext(), gl);
			dragged=false;
			page.RePosition();
		}
		checkBar();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		int masked = ev.getActionMasked();
		if (bar!=null) {
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
		
		if (masked==MotionEvent.ACTION_DOWN) {
			if (tapZoomV != SearchUI.tapZoomV)
			{
				quoTapZoom();
			}
			if (MainActivityUIBase.layoutScrollDisabled) {
				MainActivityUIBase.layoutScrollDisabled=false;
			}
		}
		
		if(nothing) {
			return false;
		}
		
		//bZoomOut = bZoomOutCompletely = true;
		
		int touch_id=ev.getPointerId(ev.getActionIndex());
		
		if(masked==MotionEvent.ACTION_DOWN){
			OrgX = lastX = ev.getX();
			twiceDetected = false;
		}
		
		if(!slideTurn || touch_id==first_touch_id) {
			detector.onTouchEvent(ev);
			if(flingDeteced) {
				dragged=flingDeteced=false;
				page.decided=false;
				return true;
			}
		}
		
		if (masked==MotionEvent.ACTION_DOWN
				&& tapZoom
				&& tapCtx.tapAlignment()==4
				&& !twiceDetected) {
			fastTapScrollX = WebContext.getScrollX();
			fastTapScrollY = WebContext.getScrollY();
		}
		
		if (fastTapZoom) {
			return true;
		}
		
		if(!slideTurn) {
			return false;
		}

		boolean ret = super.onInterceptTouchEvent(ev);

		if(page!=null){
			switch (masked) {
				case MotionEvent.ACTION_DOWN:
					if(tapZoom) {
						quoSlideZoom();
					}
					twiceDetected =false;
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
//				case MotionEvent.ACTION_POINTER_UP:
//					quoSlideZoom();
//				break;
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
	
	private void quoSlideZoom() {
		bZoomOut = WebContext.webScale <= BookPresenter.def_zoom;
		
		if(WebContext.AlwaysCheckRange==0) {
			WebContext.CheckAlwaysCheckRange();
		}
		
		WebContextWidth = WebContext.getContentWidth();
		bZoomOutCompletely = bZoomOut && (WebContext.AlwaysCheckRange==-1
				||WebContext.getScrollX()==0  && WebContext.getScrollX()+WebContext.getWidth()>=WebContextWidth);
	}
	
	private void quoTapZoom() {
		final int src = weblist.getSrc();
		if (WebContext!=null) {
			tapZoom = pBc.tapZoom();
			if (!tapZoom) {
				if (src==SearchUI.TapSch.MAIN?PDICMainAppOptions.tapZoomTapSch()
						:src==SearchUI.Fye.MAIN?PDICMainAppOptions.tapZoomFye()
						:PDICMainAppOptions.tapZoomGlobal()) {
					tapZoom = true;
					tapCtx = SearchUI.pBc;
				}
			}
		} else if(tapZoom) {
			tapZoom = false;
		}
		if (tapZoomV != SearchUI.tapZoomV) {
			tapZoomV = SearchUI.tapZoomV;
		}
		if (PDICMainAppOptions.getTurnPageEnabled()) {
			if (weblist.isMultiRecord()) {
				if (weblist.bMergingFrames) {
					slideTurn = PDICMainAppOptions.getPageTurn3();
					//CMN.Log("slideTurn::1", slideTurn);
				} else {
					slideTurn = PDICMainAppOptions.getPageTurn2();
					//CMN.Log("slideTurn::2", slideTurn);
				}
			} else {
				slideTurn = PDICMainAppOptions.getPageTurn1();
				//CMN.Log("slideTurn::3", slideTurn);
			}
		} else if(slideTurn) {
			slideTurn = false;
		}
		nothing = !slideTurn && !tapZoom;
	}
	
	public void setWebview(WebViewmy webview, ViewGroup scrollView) {
		if(WebContext != webview) {
			WebContext = webview;
		}
		if (webview==null) {
			if (pBc!=null) {
				pBc = null;
				tapCtx = SearchUI.pBc;
				quoTapZoom();
			}
		} else {
			if (pBc!=webview.pBc) {
				pBc = webview.pBc;
				tapCtx = pBc;
				quoTapZoom();
			}
		}
		if(this.scrollView!=scrollView && scrollView!=null) {
			this.scrollView = scrollView;
		} else if (WebContext!=null) {
			this.scrollView = WebContext;
		}
	}
}
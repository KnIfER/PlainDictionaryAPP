package com.knziha.plod.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import static android.view.MotionEvent.*;
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


/** 原本是RelativeLayout，故名。 */
public class RLContainerSlider extends FrameLayout{
	public PageSlide page;
	public WebViewListHandler weblist;
	private WebViewmy WebContext;
	private PhotoBrowsingContext pBc;
	private PhotoBrowsingContext tapCtx = SearchUI.pBc;
	public ViewGroup scrollView;
	private float density;
	private int move_index;
	public boolean bZoomOut;
	private boolean bZoomOutCompletely;
	private int WebContextWidth;
	boolean aborted;
	private float dragInitDx;
	private float fastTapStX;
	private float fastTapStY;
	/** Tap twice detected and quick zoom is functioning  */
	private boolean fastTapZoom;
	private float fastTapZoomInit;
	private PointerCoords[] fastTapZoomCoords;
	private PointerProperties[] fastTapZoomProps;
	private float fastTapZoomSt;
	private float minZoom;
	private float fastTapScrollX;
	private float fastTapScrollY;
	private boolean fastTapMoved;
	private float quickScaleThreshold;
	public int tapZoomV;
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

	int first_touch_id=-1;

	private float lastX;
	private float lastY;
	public float OrgX;
	public float OrgY;
	boolean dragged;
	/** Slide to turn page enabled  */
	public boolean slideTurn = false;
	/** Tap twice and quick zoom enabled  */
	public boolean tapZoom;

	boolean flingDeteced;
	/** Tap Twice Deteced */
	public boolean twiceDetected;
	public static long lastZoomTime;
	public long bSuppressNxtTapZoom;
	GestureDetector detector;
	GestureDetector.SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(tapZoom){
				if (bSuppressNxtTapZoom!=0) {
					if (CMN.now()-bSuppressNxtTapZoom<500) {
						return true;
					}
					bSuppressNxtTapZoom = 0;
				}
				PhotoBrowsingContext ctx = tapCtx;
				float targetZoom = ctx.tapZoomRatio;
				//CMN.Log("onDoubleTap::", targetZoom, WebContext.webScale/BookPresenter.def_zoom);
				int zoomInMode = ctx.tapAlignment();
				//zoomInType = 4;
				if(zoomInMode<3) {
					if(WebContext.webScale/BookPresenter.def_zoom<targetZoom){
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
							int sY = WebContext.getScrollY();
							//WebContext.zoomBy(0.02f);
							float before = WebContext.webScale;
							WebContext.zoomBy(1/WebContext.webScale*BookPresenter.def_zoom);
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
							WebContext.setScrollY((int) (sY*targetZoom));
						}
						else {
							WebContext.zoomIn();
							WebContext.zoomIn();
						}
					}
					else {
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							int sY = WebContext.getScrollY();
							float before = WebContext.webScale;
							//WebContext.zoomBy(0.02f);
							float zoom = 1/WebContext.webScale*BookPresenter.def_zoom;
							WebContext.zoomBy(zoom);
							WebContext.setScrollY((int) (sY*zoom));
						}
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
					fastTapZoomInit = 0;
				}
				//WebContext.evaluateJavascript("getSelection().empty()", null);
			}
			twiceDetected = true;
			lastZoomTime=System.currentTimeMillis();
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
			//if(System.currentTimeMillis()-lastDownTime<=200) //事件老死
			//CMN.debug("onfling!!!");
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
					//CMN.Log("onFling");
					flingDeteced =true;
					//IMSlider.startdrag(ev);
					//xxx
					/////if(IMSlider.inf!=null)
					/////	IMSlider.inf.onPreparePage(IMSlider);
					page.startDrag(e2);
					page.setAlpha(1.f);
					if(velocityX<0) {
						page.decided=-1;
					} else {
						page.decided=1;
					}
					dragged=false;
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
			page.decided=0;
			return true;
		}
		if(page !=null){
			switch (masked) {
				case ACTION_MOVE: {
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
								break;
							}
						}
					}
					//CMN.Log("ACTION_MOVE", touch_id, actual_index, move_index);
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
						if (//Math.abs(IMSlider.getTranslationX())<3.5*GlobalOptions.density &&
								dragInitDx* page.getTranslationX()<=0
								//&& Math.abs(IMSlider.getTranslationY())<20*GlobalOptions.density
						) {
							dragged = false;
							aborted = true;
							page.decided=0;
							page.RePosition();
							first_touch_id = -1;
							ViewUtils.preventDefaultTouchEvent(this, (int)lastX, (int)lastY);
							if(scrollView !=null
								&& (WebContext==null || WebContext.AlwaysCheckRange!=0)) {
								ev.setAction(ACTION_DOWN);
								//((WebView)scrollView).getSettings().setSupportZoom(false);
								scrollView.dispatchTouchEvent(ev);
							}
						}
					}
					else if(aborted) {
						onInterceptTouchEvent(ev);
						if(!dragged && scrollView !=null
								&& (WebContext==null || WebContext.AlwaysCheckRange!=0)) {
							ev.setLocation(nowX, nowY);
							if (ev.getPointerCount()==1) // 权宜之计
							scrollView.dispatchTouchEvent(ev);
						}
					}
					lastX = nowX;
					lastY = nowY;
				} break;
				case ACTION_CANCEL:
					if (ev.getSource()==100) {
						break;
					}
					page.decided=0;
				case ACTION_UP: {
					onActionUp();
				} break;
				case ACTION_POINTER_UP: {
					//CMN.Log("ACTION_POINTER_UP??", touch_id, first_touch_id);
					if(touch_id==first_touch_id){
						//CMN.Log("ACTION_POINTER_UP!!!", touch_id, first_touch_id);
						first_touch_id=-1;
					}
				} break;
			}
		}
		return dragged || aborted;
	}
	
	private void handleFastZoom(MotionEvent ev) {
		int masked = ev.getActionMasked();
		if (masked==ACTION_MOVE) {
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
				if (false) {
					// 基于 zoomBy + scrollTo，页面短效果还可以；但页面长时，缩放与滚动不同步，导致延迟严重。
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
					float factor = multiplier;
					WebContext.scrollTo((int) ((fastTapScrollX +fastTapStX)*factor - fastTapStX)
							, (int) ((fastTapScrollY +fastTapStY)*factor - fastTapStY));
					//CMN.Log("fastZoom::", WebContext.webScale, WebContext.getScrollX(), WebContext.getScrollY());
				} else {
					// 虚拟双指缩放。
					MotionEvent evt;
					PointerProperties[] props = fastTapZoomProps;
					PointerCoords[] coords = fastTapZoomCoords;
					if (fastTapZoomInit==0) {
						fastTapZoomInit = fastTapStY+getHeight();
						if (props==null) {
							PointerProperties pp1 = new PointerProperties();
							pp1.id = 0; pp1.toolType = TOOL_TYPE_FINGER;
							PointerProperties pp2 = new PointerProperties();
							pp2.id = 1; pp2.toolType = TOOL_TYPE_FINGER;
							props = fastTapZoomProps = new PointerProperties[]{pp1, pp2};
							PointerCoords pc1 = new PointerCoords();
							
							pc1.x = fastTapStX; pc1.y = fastTapStY;
							pc1.pressure = 1; pc1.size = 1;
							PointerCoords pc2 = new PointerCoords();
							pc2.x = fastTapStX; pc2.y = fastTapZoomInit;
							pc2.pressure = 1; pc2.size = 1;
							coords = fastTapZoomCoords = new PointerCoords[]{pc1, pc2};
						} else {
							coords[0].x = fastTapStX;
							coords[0].y = fastTapStY;
							coords[1].x = fastTapStX;
							coords[1].y = fastTapZoomInit;
						}
						evt = obtain(0, 0, ACTION_DOWN, 2, props, coords, 0,  0, 1, 1, 0, 0, 0, 0);
						WebContext.dispatchTouchEvent(evt); evt.recycle();
						evt = obtain(0, 0, ACTION_POINTER_DOWN, 2, props, coords, 0, 0, 1, 1, 0, 0, 0, 0);
						WebContext.dispatchTouchEvent(evt); evt.recycle();
					}
					final float newPos = fastTapZoomInit + dist * (rawDist < 0 ? 1 : -1) * 3.5f;
					coords[1].y = newPos;
					evt = obtain(0, 0,ACTION_MOVE, 2, props, coords, 0, 0, 1, 1, 0, 0, 0, 0);
					WebContext.dispatchTouchEvent(evt); evt.recycle();
				}
			}
		}
		else if (masked==ACTION_UP) {
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
			if(page.dragged) page.RePosition();
		}
		//if (twiceDetected) twiceDetected = false;
		checkBar();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		int masked = ev.getActionMasked();
		
		if (masked==ACTION_DOWN) {
			if (tapZoomV != SearchUI.tapZoomV)
			{
				quoTapZoom();
			}
			if (MainActivityUIBase.layoutScrollDisabled) {
				MainActivityUIBase.layoutScrollDisabled=false;
			}
			if(bar!=null && !bar.isHidden()){
				bar.isWebHeld=true;
				bar.cancelFadeOut();
			}
			OrgX = ev.getX();
			OrgY = ev.getY();
			if(twiceDetected) twiceDetected = false;
		}
		if (masked==ACTION_UP) {
			checkBar();
		}
		
		if(nothing) {
			return false;
		}
		
		int touch_id=ev.getPointerId(ev.getActionIndex());
		
		if(weblist.mBar.isDragging) {
			return false;
		}
		
		if(!slideTurn || touch_id==first_touch_id) {
			detector.onTouchEvent(ev);
			if (masked==ACTION_DOWN
					&& tapZoom
					&& tapCtx.tapAlignment()==4
					&& !twiceDetected) {
				fastTapScrollX = WebContext.getScrollX();
				fastTapScrollY = WebContext.getScrollY();
			}
			if(flingDeteced) {
				dragged=flingDeteced=false;
				page.decided=0;
				return true;
			}
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
				case ACTION_DOWN:
					if (WebContext!=null) {
						calcWebWidth();
					}
					CMN.debug("ACTION_DOWN");
					if (touch_id!=first_touch_id) {
						detector.onTouchEvent(ev);
						if (tapZoom
								&& tapCtx.tapAlignment()==4
								&& !twiceDetected) {
							fastTapScrollX = WebContext.getScrollX();
							fastTapScrollY = WebContext.getScrollY();
						}
						first_touch_id=touch_id;
					}
					lastX = OrgX;
					lastY = OrgY;
					aborted = false;
				break;
				case ACTION_MOVE:
					if (twiceDetected) { // 双击放大后不要翻页了
						return false;
					}
					if(ev.getPointerCount()==1) {
						lastX = ev.getX();
						lastY = ev.getY();
						if (!dragged) {
							float dx = lastX - OrgX;
							//f(twiceDetected) {
							//	calcWebWidth();
							//	twiceDetected = false;
							//}
							//todo touch slope when WebContext==null
							int theta = 50;
							if (WebContext==null
									|| ((WebContext.scrollLck==0 || (WebContext.scrollLck&1)==0&&dx>0 || (WebContext.scrollLck&2)==0&&dx<0) && (
									 WebContext.AlwaysCheckRange==0
										|| (WebContext.AlwaysCheckRange==-1 && bZoomOut && (dx > GlobalOptions.density*theta || dx < -GlobalOptions.density*theta))
										|| (WebContext.AlwaysCheckRange==1||!bZoomOut)
											&& (dx > GlobalOptions.density*theta && WebContext.getScrollX()==0
													|| dx < -GlobalOptions.density*theta && WebContext.getScrollX()+WebContext.getWidth()==WebContextWidth)
							))
							) {
								dragInitDx = dx;
								float dy = lastY - OrgY;
								if (dy == 0) dy = 0.000001f;
								dx = dx / dy;
								if(WebContext!=null && WebContext.weblistHandler.isViewSingle() && (WebContext.getContentHeight() <= WebContext.getHeight())) {
									theta = (int) (GlobalOptions.density);
									//CMN.debug("减半");
								} else {
									theta = (int) (3*GlobalOptions.density);
									if (dx > 1000 || dx <= -1000) {//3.3
										theta = Integer.MAX_VALUE;
									}
								}
								if (dx > theta || dx <= -theta) {//3.3
									dragged = true;
								}
							}
							CMN.debug("theta", theta, dx);
						}
					}
				break;
				case ACTION_UP:
					onActionUp();
				break;
			}
		}
		return dragged || aborted;
	}
	
	public DragScrollBar bar;
	private void checkBar() {
		if(bar !=null && !bar.isHidden()){
			bar.isWebHeld=false;
			bar.fadeOut();
		}
	}
	
	private void calcWebWidth() {
		WebContextWidth = WebContext.getContentWidth();
		bZoomOut = WebContext.webScale <= BookPresenter.def_zoom;
		WebContext.CheckAlwaysCheckRange();
		bZoomOutCompletely = bZoomOut && (WebContext.AlwaysCheckRange==-1
				||WebContext.getScrollX()==0  && WebContext.getScrollX()+WebContext.getWidth()>=WebContextWidth);
	}
	
	public void quoTapZoom() {
		final int src = weblist.getSrc();
		if (WebContext!=null) {
			tapZoom = pBc.tapZoom();
			if (!tapZoom) {
				if (src == SearchUI.TapSch.MAIN ? PDICMainAppOptions.tapZoomTapSch()
						: src == SearchUI.Fye.MAIN ? PDICMainAppOptions.tapZoomFye()
						: PDICMainAppOptions.tapZoomGlobal()) {
					tapZoom = true;
					tapCtx = SearchUI.pBc;
				}
			} else {
				tapCtx = pBc;
			}
		} else if(tapZoom) {
			tapZoom = false;
		}
		if (tapZoomV != SearchUI.tapZoomV) {
			tapZoomV = SearchUI.tapZoomV;
		}
		if (PDICMainAppOptions.getTurnPageEnabled()) {
			if (weblist.bDataOnly) {
				slideTurn = PDICMainAppOptions.turnPageTapSch();
				//CMN.debug("slideTurn::-1", slideTurn, tapZoom);
			} else if(src==SearchUI.Fye.MAIN){
				slideTurn = PDICMainAppOptions.turnPageFye();
				//CMN.debug("slideTurn::-2", slideTurn, tapZoom);
			} else {
				if (weblist.isMultiRecord()) {
					if (weblist.isViewSingle()) {
						if (weblist.isMergingFrames()) {
							slideTurn = PDICMainAppOptions.slidePageMd();
							//CMN.debug("slideTurn::1", slideTurn, tapZoom);
						} else if (weblist.isFoldingScreens()) {
							slideTurn = PDICMainAppOptions.slidePage1D() || PDICMainAppOptions.slidePageFd();
							//CMN.debug("slideTurn::2", slideTurn, tapZoom);
						} else {
							slideTurn = PDICMainAppOptions.slidePage1D();
							//CMN.debug("slideTurn::3", slideTurn, tapZoom);
						}
					} else {
						slideTurn = PDICMainAppOptions.slidePageMD();
						//CMN.debug("slideTurn::4", slideTurn, tapZoom);
					}
				} else {
					slideTurn = PDICMainAppOptions.slidePage1D();
					//CMN.debug("slideTurn::3", slideTurn, tapZoom);
				}
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
			}
			quoTapZoom();
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
	
	public final WebViewmy getWebContext() {
		return WebContext;
	}
	
}
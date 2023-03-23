package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import static android.view.MotionEvent.*;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.plod.PlainUI.WordPopup;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;


/** 原本是RelativeLayout，故名。 */
public class RLContainerSlider extends FrameLayout {
	public PageSlide page;
	public WebViewListHandler weblist;
	public AppBarLayout.BarSz barSz;
	private WebViewmy WebContext;
	private PhotoBrowsingContext pBc;
	private PhotoBrowsingContext tapCtx = SearchUI.pBc;
	public ViewGroup scrollView;
	private float density;
	private int move_index;
	public boolean bZoomOut;
	private boolean bZoomOutCompletely;
	private int WebContextWidth;
	int aborted;
	private float dragInitDx;
	private float dragInitDy;
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
	public boolean fastTapMoved;
	private float quickScaleThreshold;
	public int tapZoomV;
	private boolean nothing = true;
	public boolean scrollLocked;
	
	ImageView swipeRefreshIcon;
	float swipeRefreshDy;
	int swipeRefreshTheta;
	public View.OnClickListener onSwipeTopListener;
	public WordPopup wordPopup;
	
	public RLContainerSlider(Context context) {
		this(context, null);
	}
	public RLContainerSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(isInEditMode()) return;
		newDetector();
		density = GlobalOptions.density;
		quickScaleThreshold = 20*density;
		
		swipeRefreshIcon = new ImageView(context);
		int iconSz = (int) (50*GlobalOptions.density);
		swipeRefreshTheta = (int) (99*GlobalOptions.density);
		LayoutParams lp = new LayoutParams(iconSz, iconSz);
		lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
		swipeRefreshIcon.setAlpha(0.f);
		swipeRefreshIcon.setLayoutParams(lp);
		swipeRefreshIcon.setImageResource(R.drawable.ic_keyboard_show_24);
	}
//	public RLContainerSlider(Context arg0, AttributeSet arg1, int arg2) {
//		super(arg0, arg1, arg2);
//	}
	
	private void resetSwipeIcon() {
		swipeRefreshIcon.animate().translationY(0).alpha(0);
		swipeRefreshed = false;
	}
	
	int first_touch_id=-1;

	private float lastX;
	private float lastY;
	public float OrgX;
	public float OrgY;
	/** 1==drag to turn page left/right  2==drag to swipe refresh  */
	int dragged;
	/** Slide to turn page enabled  */
	public boolean slideTurn = false;
	//public boolean slideImmersiveAllow = false;
	public AppBarLayout appbar;
	/** Slide to show/hide toolbar and bottombar  */
	public boolean slideImmersive = true;
	/** Tap twice and quick zoom enabled  */
	public boolean tapZoom;
	/** 下拉刷新  */
	public boolean swipeRefresh = false;
	public boolean swipeRefreshAllow = false;
	public boolean swipeRefreshed = false;

	boolean flingDeteced;
	boolean flingDetecedY;
	/** Tap Twice Deteced */
	public boolean twiceDetected;
	public static long lastZoomTime;
	public long bSuppressNxtTapZoom;
	GestureDetector detector;
	GestureDetector.SimpleOnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			//CMN.debug("onDoubleTap::", swipeRefreshIcon.getAlpha());
			if(swipeRefreshIcon.getAlpha()>0) return false;
			if(tapZoom){
				if (bSuppressNxtTapZoom!=0) {
					if (CMN.now()-bSuppressNxtTapZoom<500) {
						return true;
					}
					bSuppressNxtTapZoom = 0;
				}
				PhotoBrowsingContext ctx = tapCtx;
				float targetZoom = ctx.tapZoomRatio;
				//CMN.debug("onDoubleTap::", targetZoom, WebContext.webScale/BookPresenter.def_zoom);
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
			//((MainActivityUIBase)getContext()).showT("onfling!!!");
			CMN.debug("onFling::", e1, e2);
			if(e1==null || e2==null) return false;
			if(slideImmersive && (dragged==0 && aborted==0))
			{
				float vy = velocityY / 8;
				CMN.Log("onFling Y ??? ", vy);
				if (Math.abs(vy) < 15*density) {
					//return true;
				}
				else if(Math.abs(velocityY/(velocityX==0?0.000001:velocityX))>0.57) {
					CMN.Log("onFling Y !!! ");
					flingDeteced = flingDetecedY = true;
					if (appbar.getExpanded() ^ velocityY>0 ) {
						appbar.postOnAnimation(new Runnable() {
							@Override
							public void run() {
								appbar.setExpanded(!appbar.getExpanded(), true);
							}
						}/*, 10*/);
					}
					return true;
				}
			}
			if(slideTurn /*&& bZoomOutCompletely*/)
			{
				float vx = velocityX / 8;
				CMN.debug("x轴速度/8=", vx, e2.getPointerCount());
				CMN.debug("WebContextWidth", WebContextWidth);
				CMN.debug("WebContext.scrollLck", WebContext.scrollLck);
				if(e2.getPointerCount()>1
					|| page.getTranslationX()!=0 && Math.signum(velocityX)!=Math.signum(page.getTranslationX())) {
					return false;
				}
				float dx = vx;
				if (WebContext == null
						|| ((WebContext.scrollLck == 0 || (WebContext.scrollLck & 1) == 0 && dx > 0 || (WebContext.scrollLck & 2) == 0 && dx < 0) && (
					bZoomOutCompletely || WebContext.AlwaysCheckRange == 0
								|| (WebContext.AlwaysCheckRange == -1 && bZoomOut
								|| (WebContext.AlwaysCheckRange == 1 || !bZoomOut)
								&& (dx > 0 && WebContext.getScrollX() == 0
								|| dx < 0 && WebContext.getScrollX() + WebContext.getWidth() >= WebContextWidth)
				))))
				{
					// Y轴幅度
					if (Math.abs(e2.getRawY() - e1.getRawY()) > 35*density) {
						//return true;
					}
					CMN.debug("SimpleOnGestureListener", velocityX, vx, 3*50*density);
					if (Math.abs(vx) < 3.25*50*density) {
						return true;
					}
					if(Math.abs(velocityX/(velocityY==0?0.000001:velocityY))>1.699) {
						//CMN.Log("onFling");
						flingDeteced =true;
						flingDetecedY = false;
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
						dragged=0; //???
						if(dragged==2) resetSwipeIcon();
						page.RePosition();
					}
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
			//CMN.debug("handleFastZoom::", ev.getActionMasked());
			handleFastZoom(ev);
			return fastTapZoom;
		}
		if (scrollLocked) {
			if (ev.getActionMasked() == ACTION_UP) {
				WebContext.evaluateJavascript("releaseLck()", null);
			} else {
				int actual_index = ev.getActionIndex();
				//if(!enabled1)ViewUtils.preventDefaultTouchEvent(weblist.getWebContextNonNull(), 0, 0);
				float x = (ev.getX(actual_index)-OrgX)/WebContext.webScale;
				float y = (ev.getY(actual_index)-OrgY)/WebContext.webScale;
				x = ((int)(x*100))/100;
				y = ((int)(y*100))/100;
				WebContext.evaluateJavascript("fakeScroll("+(float)x+", "+(float)y+")", null);
			}
			return true;
		}
		if(!slideTurn && !swipeRefreshAllow) return false;
		if(dragged==0 && aborted==0) return true;
		int actual_index = ev.getActionIndex();
		int touch_id = ev.getPointerId(actual_index);
		int masked = ev.getActionMasked();
		if(touch_id==first_touch_id && detector!=null) {
			detector.onTouchEvent(ev);
		}
		
		if(flingDeteced) {
			if(tapZoom) {
				newDetector();
			}
			flingDeteced =false;
			if(dragged==2) resetSwipeIcon();
			dragged=page.decided=0;
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
					if(dragged==1) {
						page.startDrag(ev);
						page.handleDrag(nowX-lastX,nowY-lastY);
						if(page.listener !=null)
							page.listener.onMoving(page.getTranslationX(), page);
						if (//Math.abs(IMSlider.getTranslationX())<3.5*GlobalOptions.density &&
								dragInitDx* page.getTranslationX()<=0
								//&& Math.abs(IMSlider.getTranslationY())<20*GlobalOptions.density
						) {
							aborted = 1;
							dragged = 0;
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
					else if(dragged==2) {
						VU.addViewToParent(swipeRefreshIcon, this);
						swipeRefreshDy += nowY-lastY;
						if(swipeRefreshDy>swipeRefreshTheta) swipeRefreshDy = swipeRefreshTheta;
						swipeRefreshIcon.setTranslationY(swipeRefreshDy);
						swipeRefreshIcon.setAlpha(swipeRefreshDy/swipeRefreshTheta);
						if (swipeRefreshDy <= 0) {
							aborted = 2;
							dragged = 0;
							first_touch_id = -1;
							ViewUtils.preventDefaultTouchEvent(this, (int) lastX, (int) lastY);
							if (scrollView != null
									&& (WebContext == null || WebContext.AlwaysCheckRange != 0)) {
								ev.setAction(ACTION_DOWN);
								//((WebView)scrollView).getSettings().setSupportZoom(false);
								scrollView.dispatchTouchEvent(ev);
							}
						} else if (swipeRefreshDy >= swipeRefreshTheta) {
							swipeRefreshed = true;
						} else if(swipeRefreshDy <= swipeRefreshTheta - 10){
							swipeRefreshed = false;
						}
					}
					else if(aborted!=0) {
						onInterceptTouchEvent(ev);
						if(dragged==0 && scrollView !=null
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
		return dragged!=0 || aborted!=0;
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
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						WebContext.zoomBy(targetZoom/WebContext.webScale);
					}
					//multiplier = WebContext.webScale/fastTapZoomSt;
					float factor = multiplier;
					WebContext.scrollTo((int) ((fastTapScrollX +fastTapStX)*factor - fastTapStX)
							, (int) ((fastTapScrollY +fastTapStY)*factor - fastTapStY));
					//CMN.Log("fastZoom::", WebContext.webScale, WebContext.getScrollX(), WebContext.getScrollY());
				}
				else {
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
					if (newPos >= fastTapStY)
					{
						if (WebContext.webScale <= BookPresenter.def_zoom
								&& rawDist > 0
								&& WebContext.getContentWidth()<=WebContext.getWidth()) {
							return;
						}
						coords[1].y = newPos;
						evt = obtain(0, 0,ACTION_MOVE, 2, props, coords, 0, 0, 1, 1, 0, 0, 0, 0);
						WebContext.dispatchTouchEvent(evt); evt.recycle();
					}
				}
			}
		}
		else if (masked==ACTION_UP) {
			fastTapZoom = false;
			if (fastTapMoved) {
				fastTapMoved = false;
			}
			newDetector();
			//float multiplier = WebContext.webScale/fastTapZoomSt;
			//WebContext.scrollTo((int) ((fastTapScrollX +fastTapStX)*multiplier - fastTapStX)
			//		, (int) ((fastTapScrollY +fastTapStY)*multiplier - fastTapStY));
		}
	}
	
	private void newDetector() {
		detector = new GestureDetector(getContext(), gl);
		try {
			ViewUtils.execSimple("$.mDoubleTapSlopSquare=$1", ViewUtils.reflectionPool, detector, (int) (GlobalOptions.density * 1200));
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	private void onActionUp() {
		//CMN.debug("ACTION_UP");
		first_touch_id=-1;
		if(dragged!=0) {
			if(dragged==2) {
				if (swipeRefreshed && onSwipeTopListener!=null) {
					onSwipeTopListener.onClick(this);
				}
				resetSwipeIcon();
			}
			dragged=0;
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
			if (swipeRefresh) {
				int pad = (int) (GlobalOptions.density*34);
				if (OrgX<pad || OrgX>getWidth()-pad) {
					swipeRefreshAllow = false;
				} else {
					if (!PDICMainAppOptions.swipeTopShowKeyboardStrict() || WebContext==null) {
						swipeRefreshAllow = true;
					} else {
						swipeRefreshAllow = WebContext.getScrollY()==0;
						if (swipeRefreshAllow && WebContext.merge && PDICMainAppOptions.swipeTopShowKeyboardStrict()) {
							WebContext.evaluateJavascript("defP.scrollTop==0", value -> {
								if ("false".equals(value)) {
									swipeRefreshAllow = false;
								}
							});
						}
					}
				}
				if(dragged!=0) dragged=aborted=0;
			}
			if(scrollLocked)
				scrollLocked = WebContext.scrollLocked = false;
		}
		if (masked==ACTION_UP/*||masked==ACTION_CANCEL*/) {
			checkBar();
			if (appbar!=null && PDICMainAppOptions.immersiveWhen()==1
					&& getImmersiveScrollingEnabled()
			)
			{
				int delta = (int) (ev.getY() - OrgY);
				int theta = (int) GlobalOptions.density;
				if (delta <= -theta || delta >= theta) {
					appbar.postOnAnimation(() -> appbar.setExpanded(delta>0, true)/*, 10*/);
				}
			}
		}
		if (scrollLocked) {
			return true;
		}
		
		if(nothing) {
			return false;
		}
		
		int touch_id=ev.getPointerId(ev.getActionIndex());
		
		if(weblist.mBar.isDragging) {
			return false;
		}
		//CMN.debug("onInterceptTouchEvent", masked, touch_id==first_touch_id);
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
				flingDeteced=false;
				dragged=page.decided=0;
				if(flingDetecedY) return false;
				return true;
			}
		}
		
		if (fastTapZoom) {
			return true;
		}
		
		if(!slideTurn && !swipeRefreshAllow) return false;

		boolean ret = super.onInterceptTouchEvent(ev);

		if(page!=null){
			switch (masked) {
				case ACTION_DOWN:
					if (WebContext!=null) {
						calcWebWidth();
					}
					//CMN.debug("ACTION_DOWN");
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
					aborted = 0;
				break;
				case ACTION_MOVE:
					// 双击放大后不要翻页了
					if (twiceDetected) return false;
					if(ev.getPointerCount()==1) {
						lastX = ev.getX();
						lastY = ev.getY();
						if (dragged==0) {
							float dx = lastX - OrgX;
							//f(twiceDetected) {
							//	calcWebWidth();
							//	twiceDetected = false;
							//}
							//todo touch slope when WebContext==null
							int theta = 50;
							if (slideTurn && PDICMainAppOptions.slowDragTurnPage() && aborted!=2) {
								if (WebContext == null
										|| ((WebContext.scrollLck == 0 || (WebContext.scrollLck & 1) == 0 && dx > 0 || (WebContext.scrollLck & 2) == 0 && dx < 0) && (
										WebContext.AlwaysCheckRange == 0
												|| (WebContext.AlwaysCheckRange == -1 && bZoomOut && (dx > GlobalOptions.density * theta || dx < -GlobalOptions.density * theta))
												|| (WebContext.AlwaysCheckRange == 1 || !bZoomOut)
												&& (dx > GlobalOptions.density * theta && WebContext.getScrollX() == 0
												|| dx < -GlobalOptions.density * theta && WebContext.getScrollX() + WebContext.getWidth() >= WebContextWidth)
								))
								) {
									dragInitDx = dx;
									float dy = lastY - OrgY;
									if (dy == 0) dy = 0.000001f;
									dx = dx / dy;
									if (WebContext != null && WebContext.weblistHandler.isViewSingle() && (WebContext.getContentHeight() <= WebContext.getHeight())) {
										theta = (int) (GlobalOptions.density);
										//CMN.debug("减半");
									} else {
										theta = (int) (3 * GlobalOptions.density);
										if (dx > 1000 || dx <= -1000) {//3.3
											theta = Integer.MAX_VALUE;
										}
									}
									if (dx > theta || dx <= -theta) {//3.3
										dragged = 1;
									}
								}
							}
							//CMN.debug("theta", theta, dx);
							
							if(swipeRefreshAllow && dragged==0 && aborted!=1 && (appbar==null || appbar.getTop()==0))
							{
								float dy = lastY - OrgY;
								dx = lastX - OrgX;
								if(dy>GlobalOptions.density)
								{
									boolean drg = WebContext == null || !WebContext.scrollLckVer && WebContext.getScrollY()==0;
									if (scrollView!=null) {
										if (scrollView!=WebContext && scrollView instanceof WebViewListHandler) {
											drg &= ((WebViewListHandler) scrollView).WHP.getScrollY() == 0;
										} else {
											drg &= scrollView.getScrollY()==0;
										}
									}
									if (drg)
									{
										dragInitDy = dy;
										if (dx == 0) dx = 0.000001f;
										dx = dy / dx;
										float theta1 = 2.5f;
										//CMN.debug("dx::", dx);
										if (dx > theta1 || dx <= -theta1) {//3.3
											swipeRefreshDy = dy;
											dragged = 2;
											if (GlobalOptions.isDark) {
												swipeRefreshIcon.setColorFilter(null);
											} else {
												swipeRefreshIcon.setColorFilter(0xff03a9f4, PorterDuff.Mode.SRC_IN);
//												swipeRefreshIcon.setColorFilter(GlobalOptions.NEGATIVE_1);
											}
										}
									}
								}
							}
						}
					}
				break;
				case ACTION_UP:
					onActionUp();
				break;
			}
		}
		return dragged!=0 || aborted!=0;
	}
	
	public boolean getImmersiveScrollingEnabled() {
		return wordPopup==null?PDICMainAppOptions.getEnableSuperImmersiveScrollMode():wordPopup.immersiveScroll;
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
				||WebContext.getScrollX()==0  && WebContext.getWidth()>=WebContextWidth);
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
		if (swipeRefresh != (onSwipeTopListener!=null && PDICMainAppOptions.swipeTopShowKeyboard())) {
			swipeRefresh = !swipeRefresh;
			swipeRefreshAllow = false;
		}
		slideImmersive = appbar!=null && PDICMainAppOptions.immersiveWhen()==0 && getImmersiveScrollingEnabled() ;
		//CMN.debug("quoTapZoom", swipeRefresh, slideTurn, tapZoom);
		nothing = !swipeRefresh && !slideTurn && !tapZoom && !slideImmersive;
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
	
	public boolean fastTapZoomIn() {
		return fastTapZoomCoords[1].y > fastTapStY;
	}
	
	public boolean fastTapZoomInStopped(float newScale) {
		return fastTapZoomCoords[1].y > fastTapStY ^ newScale > fastTapZoomSt;
	}
}
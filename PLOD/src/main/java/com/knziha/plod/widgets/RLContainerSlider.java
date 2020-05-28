package com.knziha.plod.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.mdict;


public class RLContainerSlider extends FrameLayout{
	public boolean TurnPageSuppressed;
	public WebViewmy WebContext;
	private float density;
	private int move_index;
	private boolean bZoomOut;
	private boolean bNoDoubleClick;
	private boolean bNoTurnPage;
	
	public RLContainerSlider(Context context) {
		this(context, null);
	}
	public RLContainerSlider(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public RLContainerSlider(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		detector = new GestureDetector(getContext(), gl);
		density = getContext().getResources().getDisplayMetrics().density;
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
				//CMN.Log(targetZoom, WebContext.webScale/mdict.def_zoom);
				if(WebContext.webScale/mdict.def_zoom<targetZoom){
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
						WebContext.zoomBy(0.02f);
						WebContext.zoomBy(targetZoom);
						int zoomInType = ibc.getDoubleClickAlignment();
						//((MainActivityUIBase)getContext()).showT("双击放大");
						if(zoomInType<3){
							int pad = WebContext.getWidth();
							float ratio = ibc.doubleClickXOffset;
							if(ratio<-1) ratio=-1;
							if(zoomInType==0){
								pad = (OrgX>pad/2?pad:0)+(int) (ratio * pad);
							} else if(zoomInType==1){
								pad = (int) (ratio * pad);
							} else if(zoomInType==2){
								pad = (int) (WebContext.getContentWidth()-pad*(1+ratio));
							}
							WebContext.setScrollX(pad);
						}
					} else {
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
				isOnZoomingDected=true;
				lastZoomTime=System.currentTimeMillis();
			}
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
			//if(System.currentTimeMillis()-lastDownTime<=200) //事件老死
			if(bZoomOut && !bNoTurnPage)
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

	long lastDownTime;
	public boolean enabled;

	@Override
	public boolean onTouchEvent(MotionEvent ev){
		MainActivityUIBase.layoutScrollDisabled=false;
		if(!TurnPageEnabled) return false;
		if(!dragged) return true;
		int actual_index = ev.getActionIndex();
		int touch_id = ev.getPointerId(actual_index);
		if(!IMSlider.decided && touch_id==first_touch_id) detector.onTouchEvent(ev);
		if(isOnFlingDected) {
			dragged=isOnFlingDected=false;
			IMSlider.decided=false;
			return true;
		}
		if(IMSlider!=null){
			switch (ev.getActionMasked()) {
				case MotionEvent.ACTION_MOVE:{
					//CMN.Log("ACTION_MOVE", touch_id, actual_index);
					if(first_touch_id==-1){
						first_touch_id=touch_id;
						OrgX = lastX = ev.getX(actual_index);
						OrgY = lastY = ev.getY(actual_index);
					}
					move_index=0;
					int pc = ev.getPointerCount();
					if(pc>1)
						for (int i = 0; i < pc; i++) {
							if(ev.getPointerId(i)==first_touch_id){
								move_index = i;
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
					if(dragged){
						IMSlider.startdrag(ev);
						IMSlider.handleDrag(nowX-lastX,nowY-lastY);
						if(IMSlider.inf!=null)
							IMSlider.inf.onMoving(IMSlider.getTranslationX(),IMSlider);
					}
					lastX = nowX;
					lastY = nowY;
				} break;
				case MotionEvent.ACTION_UP:{
					//CMN.Log("ACTION_UP", touch_id);
					first_touch_id=-1;
					if(dragged) {
						dragged=false;
						IMSlider.RePosition();
					}
				} break;
				case MotionEvent.ACTION_POINTER_UP:{
					//CMN.Log("ACTION_POINTER_UP??", touch_id, first_touch_id);
					if(touch_id==first_touch_id){
						//CMN.Log("ACTION_POINTER_UP!!!", touch_id, first_touch_id);
						first_touch_id=-1;
					}
				}break;
			}
		}
		return dragged;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		MainActivityUIBase.layoutScrollDisabled=false;
		
		bNoTurnPage = !TurnPageEnabled || TurnPageSuppressed;
		
		if(bNoTurnPage && bNoDoubleClick) {
			return false;
		}
		
		bZoomOut = WebContext==null || WebContext.webScale <= mdict.def_zoom;
		
		int actionMasked = ev.getActionMasked();
		
		int touch_id=ev.getPointerId(ev.getActionIndex());
		
		if(bNoTurnPage || touch_id==first_touch_id) {
			detector.onTouchEvent(ev);
			if(isOnFlingDected) {
				dragged=isOnFlingDected=false;
				IMSlider.decided=false;
				return true;
			}
		}
		
		if(actionMasked==MotionEvent.ACTION_DOWN){
			lastDownTime=System.currentTimeMillis();
			OrgX = lastX = ev.getX();
		}
		
		if(bNoTurnPage){
			return false;
		}

		boolean ret = super.onInterceptTouchEvent(ev);

		if(!bCanSlide) {
			return ret;
		}
		if(IMSlider!=null){
			switch (actionMasked) {
				case MotionEvent.ACTION_DOWN:
					isOnZoomingDected=false;
					//CMN.Log("ACTION_DOWN");
					first_touch_id=touch_id;
					detector.onTouchEvent(ev);
					OrgY = lastY = ev.getY();
				break;
				case MotionEvent.ACTION_MOVE:
					if(ev.getPointerCount()==1) {
						lastX = ev.getX();
						lastY = ev.getY();
						if (!dragged) {
							float dx = lastX - OrgX;
							if (bZoomOut && (dx > 100 || dx < -100) || !bZoomOut && (dx > 100 && WebContext.getScrollX()==0 || dx < -100 && WebContext.getScrollX()+WebContext.getWidth()==WebContext.getContentWidth())) {
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
					first_touch_id=-1;
					if(dragged) {
						dragged=false;
						IMSlider.RePosition();
					}
				break;
			}
		}
		return dragged;
	}
	
	public void invalidateIBC() {
		bNoDoubleClick = WebContext==null||!WebContext.IBC.getUseDoubleClick();
	}
}
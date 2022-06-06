package com.knziha.plod.widgets;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

public class PopupTouchMover implements View.OnTouchListener {
	public float lastX;
	public float lastY;
	public float OrgY;
	public float lastTY;
	private boolean doubleTapDetected;
	public boolean moveTriggered=false;
	public boolean wantsMaximize=false;
	public boolean wantedMaximize=false;
	public float DedockTheta;
	public float DedockAcc;
	public boolean FVDOCKED=false;
	public boolean Maximized;
	public float FVTY;
	public int FVH,FVH_UNDOCKED;
	public final static int FVMINWIDTH=133;
	public final static int FVMINHEIGHT=50;
	final float _50_;
	final PopupGuarder popupGuarder;
	final View textView;
	private boolean ruinedDoubleClick;
	MainActivityUIBase a;

	public PopupTouchMover(MainActivityUIBase a, View _textView){
		this.a = a;
		_50_ = a.getResources().getDimension(R.dimen._50_)*a.dm.density;
		popupGuarder=a.wordPopup.popupGuarder;
		textView=_textView;
		topgesture=new GestureDetector(a.getBaseContext(), new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return (!PDICMainAppOptions.getDoubleClickMaximizeClickSearch()) && handle(e);
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				return (PDICMainAppOptions.getDoubleClickMaximizeClickSearch()) && handle(e);
			}

			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				if(PDICMainAppOptions.getDoubleClickMaximizeClickSearch() && !ruinedDoubleClick){
					doubleTapDetected=true;
					if(e.getAction()!=MotionEvent.ACTION_UP) return false;
					if(popupGuarder.popupToGuard==null) return false;
						//CMN.Log("onDoubleTap");
						togMax();
						return true;
				}
				return false;
			}

			private boolean handle(MotionEvent e) {
				ViewGroup popupContentView = (ViewGroup) popupGuarder.popupToGuard;
				if(popupContentView==null) return false;
				if(true){
					textView.performClick();
					return true;
				}
				return false;
			}
		});
	}
	
	public void togMax() {
		ViewGroup popupContentView = (ViewGroup) popupGuarder.popupToGuard;
		ViewGroup.MarginLayoutParams lpmy = (ViewGroup.MarginLayoutParams) popupContentView.getLayoutParams();
		if(Maximized){
			popupContentView.setTranslationY(FVTY);
			lpmy.height=FVH_UNDOCKED;
			popupContentView.setLayoutParams(lpmy);
			Maximized=FVDOCKED=false;
		}
		else{
			FVTY=popupContentView.getTranslationY();
			popupContentView.setTranslationY(0);
			onMaximised();
			FVH_UNDOCKED=popupContentView.getHeight();
			lpmy.height=calcMaxedH(lpmy);
			popupContentView.setLayoutParams(lpmy);
		}
	}
	
	GestureDetector topgesture;

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		boolean b1=v==textView && PDICMainAppOptions.getDoubleClickMaximizeClickSearch();
		if(b1)
			topgesture.onTouchEvent(e);
		ViewGroup popupContentView = (ViewGroup) popupGuarder.popupToGuard;
		if(popupContentView==null) return false;
		DedockTheta=_50_/2;
		if(FVDOCKED && !Maximized){
			DedockTheta=0;
		}
		ViewGroup.MarginLayoutParams lpmy = (ViewGroup.MarginLayoutParams) popupContentView.getLayoutParams();
		switch(e.getActionMasked()){
			case MotionEvent.ACTION_DOWN:{
				//CMN.Log("DOWN");
				lastX = e.getRawX();
				OrgY = lastY = e.getRawY();
				lastTY = popupContentView.getTranslationY();
				DedockAcc=0;
				moveTriggered=
				ruinedDoubleClick=
				doubleTapDetected=false;
			} break;
			case MotionEvent.ACTION_MOVE:{
				int dy = (int) (e.getRawY() - lastY);
				final ViewConfiguration configuration = ViewConfiguration.get(popupGuarder.getContext());
				int ssp = configuration.getScaledTouchSlop();
				if(!moveTriggered){
					//CMN.Log("getScaledTouchSlop",  configuration.getScaledTouchSlop()/popupGuarder.getResources().getDisplayMetrics().density);
					moveTriggered=Math.abs(OrgY-e.getRawY())>ssp;
				}
				boolean bProceed=moveTriggered;
				boolean MOT=false;
				wantsMaximize=false;
				if (FVDOCKED) {//解Dock
					DedockAcc += dy;
					bProceed = true;
				}
				if (Math.abs(DedockAcc) > DedockTheta) {
					if (FVDOCKED) {
						if (FVH_UNDOCKED != -1) {
							lpmy.height = FVH_UNDOCKED;
							popupContentView.setLayoutParams(lpmy);
						}
						FVDOCKED = false;
					}
					if(doubleTapDetected){
						ruinedDoubleClick=true;
					}
					Maximized=false;
					//opt.setFVDocked(FVDOCKED = false);
				}
				if(bProceed)
				if (!FVDOCKED && DedockTheta>0) {//未停靠
					bProceed = false;
					popupContentView.setTranslationY(Math.min(popupGuarder.getHeight()-popupGuarder.getResources().getDimension(R.dimen.halfpopheader), Math.max(popupContentView.getTranslationY() + dy, PDICMainAppOptions.getTopSnapMaximizeClickSearch()?0:-popupContentView.getHeight()*2/3)));
					DedockAcc = 0;
					if(PDICMainAppOptions.getTopSnapMaximizeClickSearch())
						if (!doubleTapDetected && popupContentView.getTranslationY() <= 1.45) {
							wantsMaximize = true;
							if (!wantedMaximize) {
								lpmy.height = (int) (lpmy.height + _50_);
								popupContentView.setLayoutParams(lpmy);
								wantedMaximize = true;
							}
						}
						else if (wantedMaximize) {
							lpmy.height = (int) (lpmy.height - _50_);
							popupContentView.setLayoutParams(lpmy);
							wantedMaximize = false;
						}
				}
				lastX = e.getRawX();
				lastY = e.getRawY();
			} break;
			case MotionEvent.ACTION_UP:{
				doubleTapDetected=false;
				if(wantsMaximize) {
					FVH_UNDOCKED=(int) (lpmy.height-_50_);
					popupContentView.setTranslationY(0);
					lpmy.height=calcMaxedH(lpmy);
					popupContentView.setLayoutParams(lpmy);
					wantsMaximize=
					wantedMaximize=false;
					onMaximised();
					//opt.setFVDocked(FVDOCKED=true);
				}
				if(v.getBackground()!=null) {
					v.getBackground().setState(new int[] {});
					v.getBackground().invalidateSelf();
//					v.setOnTouchListener(dummyOntouch);
//					MotionEvent ev = MotionEvent.obtain(e);
//					ev.setLocation(-100, -100);
//					v.onTouchEvent(ev);
//					v.setOnTouchListener(this);


				}
			} break;
			default: break;
		}
		boolean ret = b1 ? true : lastTY != popupContentView.getTranslationY();
		//if(e.getActionMasked()==MotionEvent.ACTION_UP)lastTY=popupContentView.getTranslationY();
		return ret;
	}
	
	private void onMaximised() {
		Maximized=FVDOCKED=true;
		if(a.ucc!=null) { // todo opt
			a.ucc.clearTextFocus();
		}
	}
	
	private int calcMaxedH(ViewGroup.MarginLayoutParams lpmy) {
		return popupGuarder.getHeight()-lpmy.topMargin-lpmy.bottomMargin-(PDICMainAppOptions.isFullScreen()?0: CMN.getStatusBarHeight(popupGuarder.getContext()));
	}

	public void Dedock() {
		Maximized=false;
		FVDOCKED = false;
		ViewGroup popupContentView = (ViewGroup) popupGuarder.popupToGuard;
		if(popupContentView==null) return;
		ViewGroup.LayoutParams lpmy = popupContentView.getLayoutParams();
		lpmy.height = FVH_UNDOCKED;
		popupContentView.setLayoutParams(lpmy);
		popupContentView.setTranslationY(FVTY);
	}
}

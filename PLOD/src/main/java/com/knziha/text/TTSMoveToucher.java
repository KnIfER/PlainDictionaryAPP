package com.knziha.text;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

public class TTSMoveToucher implements View.OnTouchListener {
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
	final View textView;
	final View TTSController_;
	private boolean ruinedDoubleClick;
	final PDICMainAppOptions opt;
	public final int _45_;

	public TTSMoveToucher(MainActivityUIBase a, TextView _textView, View contentView, PDICMainAppOptions opt){
		_50_ = a.getResources().getDimension(R.dimen._50_)*a.dm.density;
		TTSController_=contentView;
		textView=_textView;
		topgesture=new GestureDetector(a.getBaseContext(), new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				if(!ruinedDoubleClick){
					doubleTapDetected=true;
					if(e.getAction()!=MotionEvent.ACTION_UP) return false;
					ViewGroup popupContentView = (ViewGroup) TTSController_;
					if(popupContentView==null) return false;
						//CMN.Log("onDoubleTap");
						ViewGroup.MarginLayoutParams lpmy = (ViewGroup.MarginLayoutParams) popupContentView.getLayoutParams();
						if(Maximized){
							popupContentView.setTranslationY(FVTY);
							lpmy.height=opt.getTTSExpanded()?FVH_UNDOCKED:_45_;
							popupContentView.setLayoutParams(lpmy);
							Maximized=FVDOCKED=false;
						}
						else{
							FVTY=popupContentView.getTranslationY();
							popupContentView.setTranslationY(0);
							Maximized=FVDOCKED=true;
							if(opt.getTTSExpanded())
								FVH_UNDOCKED=popupContentView.getHeight();
							lpmy.height=calcMaxedH(lpmy);
							popupContentView.setLayoutParams(lpmy);
						}
						return true;
				}
				return false;
			}
		});
		this.opt = opt;
		_45_ = (int) a.getResources().getDimension(R.dimen._45_);
	}

	GestureDetector topgesture;

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if(v==textView)
			topgesture.onTouchEvent(e);
		ViewGroup popupContentView = (ViewGroup) TTSController_;
		if(popupContentView==null) return false;
		DedockTheta=_50_/2;
		if(FVDOCKED && !Maximized){
			DedockTheta=0;
		}
		ViewGroup.MarginLayoutParams lpmy = (ViewGroup.MarginLayoutParams) popupContentView.getLayoutParams();
		switch(e.getAction()){
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
				final ViewConfiguration configuration = ViewConfiguration.get(TTSController_.getContext());
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
				if (DedockAcc > DedockTheta) {
					if (FVDOCKED)
					if (FVH_UNDOCKED != -1) {
						lpmy.height = opt.getTTSExpanded()?FVH_UNDOCKED:_45_;
						popupContentView.setLayoutParams(lpmy);
					}
					if(doubleTapDetected){
						ruinedDoubleClick=true;
					}
					Maximized=false;
					FVDOCKED = false;
					//opt.setFVDocked(FVDOCKED = false);
				}
				if(bProceed)
				if (!FVDOCKED && DedockTheta>0) {//未停靠
					bProceed = false;
					ViewGroup root = ((ViewGroup) TTSController_.getParent());
					popupContentView.setTranslationY(Math.min(root==null?TTSController_.getHeight():root.getHeight()-TTSController_.getResources().getDimension(R.dimen.halfpopheader), Math.max(popupContentView.getTranslationY() + dy, 0)));
					DedockAcc = 0;
					if(true)
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
					if(opt.getTTSExpanded())
						FVH_UNDOCKED=(int) (lpmy.height-_50_);
					popupContentView.setTranslationY(0);
					lpmy.height=calcMaxedH(lpmy);
					popupContentView.setLayoutParams(lpmy);
					wantsMaximize=
					wantedMaximize=false;
					Maximized=
					FVDOCKED=true;
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
		return v==textView?true:lastTY!=popupContentView.getTranslationY();
	}

	private int calcMaxedH(ViewGroup.MarginLayoutParams lpmy) {
		ViewGroup root = ((ViewGroup) TTSController_.getParent());
		return root==null?TTSController_.getHeight():root.getHeight()-lpmy.topMargin-lpmy.bottomMargin-(PDICMainAppOptions.isFullScreen()?0: CMN.getStatusBarHeight(TTSController_.getContext()));
	}

	public void Dedock() {
		Maximized=false;
		FVDOCKED = false;
		ViewGroup popupContentView = (ViewGroup) TTSController_;
		if(popupContentView==null) return;
		ViewGroup.LayoutParams lpmy = popupContentView.getLayoutParams();
		lpmy.height = opt.getTTSExpanded()?FVH_UNDOCKED:_45_;
		popupContentView.setLayoutParams(lpmy);
		popupContentView.setTranslationY(FVTY);
	}
}

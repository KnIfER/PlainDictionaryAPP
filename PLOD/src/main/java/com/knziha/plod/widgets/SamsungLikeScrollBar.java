package com.knziha.plod.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.R;

import java.util.Timer;
import java.util.TimerTask;


public class SamsungLikeScrollBar extends RelativeLayout{
	SamsungLikeHandle handleThumb;

	int handleColour;
	int mMax=100;
	int mProgress;
	float lastY;
	public boolean isHeld;
	public boolean isDragging = false;
	private TypedArray a;
	public View scrollee;
	SwipeRefreshLayout swipeRefreshLayout;

	public boolean isHidden(){
		return handleThumb.getVisibility()!=View.VISIBLE;
	}

	//CHAPTER I - INITIAL SETUP

	//构造
	public SamsungLikeScrollBar(Context context, AttributeSet attributeSet){
		this(context, attributeSet, 0);
	}

	//构造
	public SamsungLikeScrollBar(Context context, AttributeSet attributeSet, int defStyle){
		super(context, attributeSet, defStyle);

		setUpProps(context, attributeSet); //Discovers and applies some XML attributes

		addView(setUpHandle(context, a.getBoolean(R.styleable.MaterialScrollBar_msb_lightOnTouch, true))); //Adds the handle
	}

	public void setProgress(int val) {
		mProgress=val;
		//ViewCompat.setY(handleThumb, 1.f*val/mMax*getHeight());
		int newTop = (int) (1.0f*val*(getHeight()-handleThumb.getHeight())/mMax);
		newTop = Math.max(newTop, 0);
		newTop = Math.min(newTop,  getHeight()-handleThumb.getHeight());
		handleThumb.setBottom(newTop+handleThumb.getHeight());
		handleThumb.setTop(newTop);
		handleThumb.postInvalidate();

	}
	public void setMax(int contentHeight) {
		mMax=contentHeight;
	}

	//Unpacks XML attributes and ensures that no mandatory attributes are missing, then applies them.
	void setUpProps(Context context, AttributeSet attributes){
		a = context.getTheme().obtainStyledAttributes(
				attributes,
				R.styleable.MaterialScrollBar,
				0, 0);
	}



	int desiredWidth=18;
	//设置拉杆
	SamsungLikeHandle setUpHandle(Context context, Boolean lightOnTouch){
		handleThumb = new SamsungLikeHandle(context, 0);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(Utils.getDP(desiredWidth, this),
				Utils.getDP(38, this));
		lp.addRule(ALIGN_PARENT_RIGHT);
		handleThumb.setLayoutParams(lp);
		handleThumb.setVisibility(View.GONE);
		int colorToSet;
		handleColour = 0;
		if(lightOnTouch){
			colorToSet = Color.parseColor("#9c9c9c");
		} else {
			colorToSet = handleColour;
		}
		handleThumb.setBackgroundColor(colorToSet);
		if(!isInEditMode())
			handleThumb.setBackground(getResources().getDrawable(R.drawable.shape1));
		return handleThumb;
	}


	//Waits for all of the views to be attached to the window and then implements general setup.
	//Waiting must occur so that the relevant recyclerview can be found.
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if(!initialized)
			generalSetup();
	}
	boolean initialized;
	//General setup.
	private void generalSetup(){
		initialized=true;

		a.recycle();

		setTouchIntercept(); // catches touches on the bar

		identifySwipeRefreshParents();

		checkCustomScrolling();

		//Hides the view
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_SELF, 2.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		anim.setDuration(0);
		anim.setFillAfter(true);
		//startAnimation(anim);
	}

	//Identifies any SwipeRefreshLayout parent so that it can be disabled and enabled during scrolling.
	void identifySwipeRefreshParents(){
		boolean cycle = true;
		ViewParent parent = getParent();
		if(parent != null){
			while(cycle){
				if(parent instanceof SwipeRefreshLayout){
					swipeRefreshLayout = (SwipeRefreshLayout)parent;
					cycle = false;
				} else {
					if(parent.getParent() == null){
						cycle = false;
					} else {
						parent = parent.getParent();
					}
				}
			}
		}
	}

	// Makes the bar render correctly for XML
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		desiredWidth = Utils.getDP(desiredWidth, this);
		int desiredHeight = 100;

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		//Measure Width
		if (widthMode == MeasureSpec.EXACTLY) {
			//Must be this size
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			//Can't be bigger than...
			width = Math.min(desiredWidth, widthSize);
		} else {
			//Be whatever you want
			width = desiredWidth;
		}

		//Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			//Must be this size
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			//Can't be bigger than...
			height = Math.min(desiredHeight, heightSize);
		} else {
			height = desiredHeight;
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(width, height);
	}

	//CHAPTER II - ABSTRACTION FOR FLAVOUR DIFFERENTIATION



	//CHAPTER III - CUSTOMISATION METHODS

	private void checkCustomScrollingInterface(){

	}



	/**
	 * The scrollBar should attempt to use dev provided scrolling logic and not default logic.
	 *
	 * The adapter must implement {@link ICustomScroller}.
	 */
	private void checkCustomScrolling(){
		if (ViewCompat.isAttachedToWindow(this))
			checkCustomScrollingInterface();
		else
			addOnLayoutChangeListener(new OnLayoutChangeListener()
			{
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
				{
					SamsungLikeScrollBar.this.removeOnLayoutChangeListener(this);
					checkCustomScrollingInterface();
				}
			});
	}

	Runnable fadeOut=() -> {
		handleThumb.setVisibility(View.GONE);
		currentRunnable=null;
		//CMN.Log("fadeOut called !!!");
	};
	Runnable NautyFadeOut=new Runnable() {
		@Override
		public void run() {
			if(scrollee!=null){
				float _lastY = scrollee.getScrollY();
				if(lastY!=_lastY){
					handleThumb.postDelayed(currentRunnable=this, 100);
					lastY=_lastY;
				}
				else {
					handleThumb.removeCallbacks(this);
					fadeOut();
				}
			}
		}
	};

	Runnable currentRunnable;

	public void fadeOut(){
		//CMN.Log("fadeOut ?");
		if(currentRunnable!=fadeOut){
			//CMN.Log("fadeOut prepared successfully ...");
			handleThumb.postDelayed(currentRunnable=fadeOut, 1000);
		}
	}

	public void cancelFadeOut(){
		if(currentRunnable!=null){// fade out task is already assigned.
			//CMN.Log(" cancelFadeOut ");
			handleThumb.removeCallbacks(fadeOut);
			handleThumb.removeCallbacks(NautyFadeOut);
			currentRunnable=null;
		}
	}

	public void hiJackScrollFinishedFadeOut(){
		//CMN.Log("hiJack ?", currentRunnable);
		//if(currentRunnable==fadeOut){
			//CMN.Log("hiJacked successfully ...");
			cancelFadeOut();
			handleThumb.postDelayed(currentRunnable=NautyFadeOut, 200);
		//}
	}

	public void fadeIn(){
		handleThumb.setVisibility(View.VISIBLE);
	}

	void setTouchIntercept() {
		OnTouchListener otl = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				switch(e.getAction()){
					case MotionEvent.ACTION_DOWN:
						isHeld =true;
						isDragging =true;
						lastY = e.getRawY();
						if(opc!=null)
							opc.OnProgressChanged(-1);
					break;
					case MotionEvent.ACTION_MOVE:
						float dy = e.getRawY() - lastY;
						//ViewCompat.setY(handleThumb,e.getRawY());
						int newTop = (int) (handleThumb.getTop()+dy);
						newTop = Math.max(newTop, 0);
						newTop = Math.min(newTop,  getHeight()-handleThumb.getHeight());
						handleThumb.setBottom(newTop+handleThumb.getHeight());
						handleThumb.setTop(newTop);
						handleThumb.postInvalidate();
						int progress = (int) (1.0f*mMax*newTop/(getHeight()-handleThumb.getHeight()));
						if(opc!=null)
							opc.OnProgressChanged(progress);
						if(scrollee!=null) {
							if(scrollee instanceof ScrollView)
								((ScrollView)scrollee).smoothScrollTo(0, progress);
							else
								scrollee.setScrollY(progress);
						}
						lastY = e.getRawY();
					break;
					case MotionEvent.ACTION_UP:
						isHeld =false;
						isDragging =false;
						if(opc!=null)
							opc.OnProgressChanged(-2);
					break;
					default:
					break;
				}
				return true;
			}
		};
		handleThumb.setOnTouchListener(otl);
	}

	public void setOnProgressChangedListener(
			OnProgressChangedListener onProgressChangedListener) {
		opc = onProgressChangedListener;
	}public interface OnProgressChangedListener {
		void OnProgressChanged(int _mProgress);
	}OnProgressChangedListener opc;

	public boolean isWebHeld;
	public Timer timer;

	public void setDelimiter(String newShield) {
		handleThumb.setDelimiter(newShield);
	}

	public SamsungLikeHandle getHandle(){
		return handleThumb;
	}
}
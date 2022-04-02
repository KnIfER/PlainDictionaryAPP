package com.knziha.plod.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.knziha.plod.plaindict.R;

import java.util.Timer;


public class DragScrollBar extends RelativeLayout{
	public SimpleHandle handleThumb;

	int handleColour;
	int mMax=100;
	int mProgress;
	float lastY;
	public boolean isHeld;
	public boolean isDragging = false;
	private TypedArray a;
	public View scrollee;
	public ScrollView scrollView;
	SwipeRefreshLayout swipeRefreshLayout;
	private int bgColor;
	
	public int desiredWidth;
	int desiredHeight=36;
	
	public boolean isHidden(){
		return getVisibility()!=View.VISIBLE || handleThumb.getVisibility()!=View.VISIBLE;
	}

	//CHAPTER I - INITIAL SETUP

	//构造
	public DragScrollBar(Context context, AttributeSet attributeSet){
		this(context, attributeSet, 0);
	}

	//构造
	public DragScrollBar(Context context, AttributeSet attributeSet, int defStyle){
		super(context, attributeSet, defStyle);

		setUpProps(context, attributeSet); //Discovers and applies some XML attributes

		desiredWidth = (int) getResources().getDimension(R.dimen.scrollBarWidth);

		addView(setUpHandle(context, a.getBoolean(R.styleable.MaterialScrollBar_msb_lightOnTouch, true))); //Adds the handle
	}

	//xxx
	public void progress(int val) {
		mProgress=val;
		//ViewCompat.setY(handleThumb, 1.f*val/mMax*getHeight());
		int newTop = topByProgress(mProgress);
		handleThumb.setTop(newTop);
		handleThumb.setBottom(newTop+desiredHeight);
		//handleThumb.postInvalidate();
	}
	
	public void postprog(int val) {
		mProgress=val;
		//ViewCompat.setY(handleThumb, 1.f*val/mMax*getHeight());
		post(ppaby);
	}
	
	Runnable ppaby = ()->{
		int newTop = topByProgress(mProgress);
		handleThumb.setTop(newTop);
		handleThumb.setBottom(newTop+desiredHeight);
		handleThumb.postInvalidate();
	};

	public int progress() {
		return mProgress;
	}

	private int topByProgress(int mProgress) {
		final int max = getHeight()-desiredHeight;
		final int newTop = (int) (1.0*mProgress*max/mMax);
		return Math.max(0, Math.min(newTop,  max));
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

	//设置拉杆
	SimpleHandle setUpHandle(Context context, Boolean lightOnTouch){
		handleThumb = new SimpleHandle(context, 0);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(desiredWidth,
				desiredHeight= (int) context.getResources().getDimension(R.dimen._35_));
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
	
	public void setHandleColorFiler(int color) {
		if(bgColor!=color) {
			bgColor = color;
			handleThumb.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
		}
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


	/**
	 * The scrollBar should attempt to use dev provided scrolling logic and not default logic.
	 *
	 * The adapter must implement {@link ICustomScroller}.
	 */
	private void checkCustomScrolling(){
		if (!ViewCompat.isAttachedToWindow(this))
			addOnLayoutChangeListener(new OnLayoutChangeListener()
			{
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
				{
					DragScrollBar.this.removeOnLayoutChangeListener(this);
				}
			});
	}

	Runnable fadeOutRn=() -> {
		handleThumb.setVisibility(View.GONE);
		stupidRn=null;
		//CMN.Log("fadeOut called !!!");
	};
	Runnable fadeRnNty=new Runnable() {
		@Override
		public void run() {
			if(scrollee!=null && !isHeld){
				float _lastY = scrollee.getScrollY();
				if(lastY!=_lastY){
					handleThumb.postDelayed(stupidRn=this, 100);
					lastY=_lastY;
				}
				else {
					handleThumb.removeCallbacks(this);
					fadeOut();
				}
			}
		}
	};

	// 谁能改？
	Runnable stupidRn;

	public void fadeOut(){
		//CMN.Log("fadeOut ?");
		if(stupidRn!= fadeOutRn){
			//CMN.Log("fadeOut prepared successfully ...");
			handleThumb.postDelayed(stupidRn= fadeOutRn, 1000);
		}
	}

	public void cancelFadeOut(){
		if(stupidRn!=null){// fade out task is already assigned.
			//CMN.Log(" cancelFadeOut ");
			handleThumb.removeCallbacks(fadeOutRn);
			handleThumb.removeCallbacks(fadeRnNty);
			stupidRn=null;
		}
	}

	public void hiJackScrollFinishedFadeOut(){
		//CMN.Log("hiJack ?", currentRunnable);
		//if(currentRunnable==fadeOut){
			//CMN.Log("hiJacked successfully ...");
			cancelFadeOut();
			handleThumb.postDelayed(stupidRn= fadeRnNty, 200);
		//}
	}

	public void fadeIn(){
		handleThumb.setVisibility(View.VISIBLE);
	}
	
	public void postfin(){
		post(pfaby);
	}
	
	Runnable pfaby = this::fadeIn;

	private boolean synced;
	void setTouchIntercept() {
		OnTouchListener otl = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				float y = e.getRawY();
				//CMN.Log("y::", y);
				switch(e.getActionMasked()){
					case MotionEvent.ACTION_DOWN:
						isHeld =true;
						isDragging =true;
						lastY = y;
						synced=false;
						//scrollee.startNestedScroll(SCROLL_AXIS_VERTICAL);
					break;
					case MotionEvent.ACTION_MOVE://xxx
						float dy = y - lastY;
						int max = getHeight()-desiredHeight;
						int progress = (int) Math.max(0, Math.min(mProgress + dy*mMax/max, mMax));
						int newTop = topByProgress(progress);
						//CMN.Log("ACTION_MOVE::", e.getRawY(), dy, progress, newTop);
						handleThumb.setTop(newTop);
						handleThumb.setBottom(newTop+desiredHeight);
						//handleThumb.postInvalidate();
						if(progress!=mProgress) {
							mProgress = progress;
							//if (opc != null) opc.OnProgressChanged(progress);
							if (scrollee != null) {
								if (scrollView!=null) {
									scrollView.smoothScrollTo(0, progress);
									if(!synced){
										synced=true;
									}
								} else {
									scrollee.setScrollY(progress);
								}
							}
						}
						lastY = y;
					break;
					case MotionEvent.ACTION_UP:
						isHeld =false;
						isDragging =false;
					break;
					default:
					break;
				}
				return true;
			}
		};
		handleThumb.setOnTouchListener(otl);
	}

	public boolean isWebHeld;
	public Timer timer;

	public void setDelimiter(String newShield, View _scrollee) {
		handleThumb.setDelimiter(newShield);
		scrollee = _scrollee;
		scrollView = scrollee instanceof ScrollView? (ScrollView) scrollee :null;
	}

	public SimpleHandle getHandle(){
		return handleThumb;
	}
}
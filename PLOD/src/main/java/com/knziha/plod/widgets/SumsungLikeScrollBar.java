package com.knziha.plod.widgets;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.knziha.plod.PlainDict.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.ScrollView;


public class SumsungLikeScrollBar extends RelativeLayout implements View.OnTouchListener{


	//Associated Objects
    ScrollingUtilities scrollUtils = new ScrollingUtilities(this);
    
    //Component Views
    //private View handleTrack;
    Handle handleThumb;

    //Characteristics
    int handleColour;
    int handleOffColour = Color.parseColor("#9c9c9c");
    int mMax=100;
    int mProgress;
    float lastY;
    boolean held;public boolean isHeld() {return held;}
    protected boolean hidden = true;
    private int textColour = ContextCompat.getColor(getContext(), android.R.color.white);
    boolean lightOnTouch;
    private TypedArray a; //XML attributes
    private Boolean rtl = false;
    boolean hiddenByUser = false;
    private float fastScrollSnapPercent = 0;
    public View scrollee;
    //Associated Objects
    //RecyclerView recyclerView;
    private int seekId = 0; //ID of the associated RecyclerView
    SwipeRefreshLayout swipeRefreshLayout;

    //Misc
    private OnLayoutChangeListener indicatorLayoutListener;
    private float previousScrollPercent = 0;
    Boolean draggableFromAnywhere = false;
    ArrayList<Runnable> onAttach = new ArrayList<>();
    private boolean attached = false;

    //CHAPTER I - INITIAL SETUP


    //构造
    public SumsungLikeScrollBar(Context context, AttributeSet attributeSet){
        this(context, attributeSet, 0);
    }

    //构造
    public SumsungLikeScrollBar(Context context, AttributeSet attributeSet, int defStyle){
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


        if(!isInEditMode()){
            seekId = a.getResourceId(R.styleable.MaterialScrollBar_msb_recyclerView, 0); //Discovers and saves the ID of the recyclerView
        }
    }



    int desiredWidth=18;
    //设置拉杆
    Handle setUpHandle(Context context, Boolean lightOnTouch){
        handleThumb = new Handle(context, 0);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(Utils.getDP(desiredWidth, this),
                Utils.getDP(38, this));
        lp.addRule(ALIGN_PARENT_RIGHT);
        handleThumb.setLayoutParams(lp);

        int colorToSet;
        handleColour = fetchAccentColour(context);
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

    //Implements optional attributes.
    void implementPreferences(){
        if(a.hasValue(R.styleable.MaterialScrollBar_msb_barColor)){
        }
        if(a.hasValue(R.styleable.MaterialScrollBar_msb_handleColor)){
        }
        if(a.hasValue(R.styleable.MaterialScrollBar_msb_handleOffColor)){
        }
        if(a.hasValue(R.styleable.MaterialScrollBar_msb_textColor)){
        }
        if(a.hasValue(R.styleable.MaterialScrollBar_msb_barThickness)){
        }
        if(a.hasValue(R.styleable.MaterialScrollBar_msb_rightToLeft)){
        }
    }



    //Waits for all of the views to be attached to the window and then implements general setup.
    //Waiting must occur so that the relevant recyclerview can be found.
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        attached = true;
        if(!initialized)
        	generalSetup();
    }
    boolean initialized;
    //General setup.
    private void generalSetup(){
    	initialized=true;
        implementPreferences();

        a.recycle();

        setTouchIntercept(); // catches touches on the bar

        identifySwipeRefreshParents();

        checkCustomScrolling();

        for(int i = 0; i < onAttach.size(); i++) {
            onAttach.get(i).run();
        }

        //Hides the view
        TranslateAnimation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_SELF, 2.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        anim.setDuration(0);
        anim.setFillAfter(true);
        hidden = true ;
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

    boolean isScrollChangeLargeEnoughForFastScroll(float currentScrollPercent) {
        return Math.abs(currentScrollPercent - previousScrollPercent) > fastScrollSnapPercent;
    }

    boolean sizeUnchecked = true;

    //Checks each time the bar is laid out. If there are few enough view that
    //they all fit on the screen then the bar is hidden. If a view is added which doesn't fit on
    //the screen then the bar is unhidden.
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(!isInEditMode()){
           //throw new RuntimeException("You need to set a recyclerView for the scroll bar, either in the XML or using setRecyclerView().");
        }


        //handleThumb.setVisibility(VISIBLE);

        
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
     * With very long lists, it may be advantageous to put a buffer on the drag bar to give the
     * user some time to actually see the scroll handle and the content. This will make the
     * bar less "smooth scrolling" and instead, snap to specific scroll percents. This could
     * be useful for the {@link DateAndTimeIndicator} style scrollbars, where you don't need to see
     * every single date available.
     *
     * @param snapPercent percentage that the fast scroll bar should snap to.
     */


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
                    SumsungLikeScrollBar.this.removeOnLayoutChangeListener(this);
                    checkCustomScrollingInterface();
                }
            });
    }







    private void setHandleColour(){

            handleThumb.setBackgroundColor(handleColour);

    }

    //CHAPTER IV - MISC METHODS

    //Fetch accent color.
    static int fetchAccentColour(Context context) {
        TypedValue typedValue = new TypedValue();
        //TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        //int color = a.getColor(0, 0);

       // a.recycle();

        return 0;
    }

    /**
     * Animates the bar out of view
     */
    public void fadeOut(){
    		hidden = true;
    		handleThumb.post(new Runnable() {
				@Override
				public void run() {
		            handleThumb.setVisibility(View.INVISIBLE);
				}});
    }

    /**
     * Animates the bar into view
     */
    public void fadeIn(){
    		if(hidden){
	            hidden = false;
    		}
            handleThumb.setVisibility(View.VISIBLE);
    }
    
	float getHandleOffset() {
		return 0;
	}

    protected void onDown(MotionEvent event){
    	
        float currentScrollPercent = event.getY();
        if (isScrollChangeLargeEnoughForFastScroll(currentScrollPercent) ||
                currentScrollPercent == 0 || currentScrollPercent == 1) {
            previousScrollPercent = currentScrollPercent;
            ViewCompat.setY(handleThumb,event.getRawY()-lastY);
            handleThumb.invalidate();
        }
		
        //if (lightOnTouch) 
           // handleThumb.setBackgroundColor(handleColour);
    }

    protected void onUp(){

            handleThumb.setBackgroundColor(handleOffColour);
    }


    void setTouchIntercept() {
        OnTouchListener otl = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
            	SumsungLikeScrollBar.this.onTouch(v,e);
    				switch(e.getAction()){
    					case MotionEvent.ACTION_DOWN:
    						held=true;
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
    							if(ScrollView.class.isInstance(scrollee))
    								((ScrollView)scrollee).smoothScrollTo(0, progress);
    							else
    								scrollee.setScrollY(progress);
    						}
    						lastY = e.getRawY();
							break;
    					case MotionEvent.ACTION_UP:
    						held=false;
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
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_DOWN) {
			isWebHeld=true;
			//fadeIn();
		}
		if(event.getAction()==MotionEvent.ACTION_UP) {
			isWebHeld=false;
			if(timer!=null)
				timer.cancel();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if(!isWebHeld && ! isHeld())
						fadeOut();
				}}, 500);
		}
		return false;
	}
    private long delayMillis = 100;
    private long lastScrollUpdate = -1;
    private Runnable scrollerTask = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastScrollUpdate) > 100) {
                lastScrollUpdate = -1;
                //CMN.show("stopped");
                //((ScrollView)a.webholder.getParent()).onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
                if(!isWebHeld)
                	onTouch(null, MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
            } else {
            	v.postDelayed(this, delayMillis);
            }
        }
    };
    View v;
	public void updateScrollState(View v_) {
		v=v_;
		if (lastScrollUpdate == -1) {
            v.postDelayed(scrollerTask, delayMillis);
		}
        lastScrollUpdate = System.currentTimeMillis();
	}

	public void setDelimiter(String newShield) {
    	handleThumb.setDelimiter(newShield);
    }

}
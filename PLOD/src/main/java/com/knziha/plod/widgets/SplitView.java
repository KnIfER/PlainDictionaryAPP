package com.knziha.plod.widgets;

import java.util.ArrayList;

import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class SplitView extends LinearLayout implements OnTouchListener {
	public DragScrollBar scrollbar2guard;
    private int mHandleId;
    private View mHandle;

    
    private int mPrimaryContentId;
    private View mPrimaryContent;

    private int mSecondaryContentId;
    private View mSecondaryContent;


	private int CompensationTop;
	private int CompensationBottom;
	private int ReservationLeft;
	private int ReservationRight;
	
    private boolean mDragging;
    //private long mDraggingStarted;
    private float mDragStartX;
    private float mDragStartY;
    private int mLastPrimaryContentSize;
    ArrayList<View> valves=new ArrayList<>();

    private float mPointerOffset;
    public boolean isSlik=false;
	public boolean canClickThrough;
	private boolean clickThroughEntered;
	
	public interface PageSliderInf{
		void onPreparePage(int orgSize);
		void onMoving(SplitView webcontentlist,float val);
		void onPageTurn(SplitView webcontentlist);
		void onHesitate();
		
    	void SizeChanged(int newSize,float delta);
    	void onDrop(int size);
    	int preResizing(int size);
	}
	public PageSliderInf inf;
	public void setPageSliderInf(PageSliderInf inf_) 
	{
		inf=inf_;
	}
	
    private float lastPosX,lastPosY;
    private float sz_hdl,sz_valv;
    public int multiplier=1;
    //构造
    public SplitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
	        TypedArray StyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.SplitViewSty);
	        
	        RuntimeException e = null;

	        sz_hdl = getResources().getDimension(R.dimen.sz_handle);
	        sz_valv = getResources().getDimension(R.dimen.sz_valve);
	        
	    	CompensationTop = StyledAttributes.getDimensionPixelSize(R.styleable.SplitViewSty_CompensationTop,0);
	    	CompensationBottom = StyledAttributes.getDimensionPixelSize(R.styleable.SplitViewSty_CompensationBottom,0);
	    	ReservationLeft = StyledAttributes.getDimensionPixelSize(R.styleable.SplitViewSty_ReservationLeft,0);
	    	ReservationRight  = StyledAttributes.getDimensionPixelSize(R.styleable.SplitViewSty_ReservationRight,0);
	    	
	        mHandleId = StyledAttributes.getResourceId(R.styleable.SplitViewSty_handle, 0);
	        if (mHandleId == 0) {
	            e = new IllegalArgumentException(StyledAttributes.getPositionDescription() +
	                                             ": Handle attribute must refer to a valid child.");
	        }
	
	        mPrimaryContentId = StyledAttributes.getResourceId(R.styleable.SplitViewSty_primaryContent, 0);
	        if (mPrimaryContentId == 0 ) {
	            e = new IllegalArgumentException(StyledAttributes.getPositionDescription() +
	                                             ": Handle attribute must refer to a valid child.");
	        }
	
	
	        mSecondaryContentId = StyledAttributes.getResourceId(R.styleable.SplitViewSty_secondaryContent, 0);
	        if (mSecondaryContentId == 0 ) {
	            e = new IllegalArgumentException(StyledAttributes.getPositionDescription() +
	                                             ": Secondary content must refer to a valid child.");
	        }
	
	        StyledAttributes.recycle();
	
	        if (e != null) {
	            throw e;
	        }
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
        	
	        mHandle = findViewById(mHandleId);
	        
	        mPrimaryContent = findViewById(mPrimaryContentId);
	        if (mPrimaryContent == null ) {
	            String name = getResources().getResourceEntryName(mPrimaryContentId);
	            throw new RuntimeException("Panel must have a child of id " + name + "'");
	
	        }
	
	        mLastPrimaryContentSize = getPrimaryContentSize();
	
	        mSecondaryContent = findViewById(mSecondaryContentId);
	        if (mSecondaryContent == null ) {
	            String name = getResources().getResourceEntryName(mSecondaryContentId);
	            if(!isInEditMode())
	            throw new RuntimeException("Panel must have a child of id" + name + "'");
	
	        }
	
	        //mHandle.setOnTouchListener(this);
        }

    }
    
    @Override
	public boolean onTouch(View view, MotionEvent ev) {
        if (mHandle==null) return false;
    	//Toast.makeText(view.getContext(), "onTouch", Toast.LENGTH_SHORT).show();

		//if(true) return false;
    	
        //Log.v("foo", "at "+SystemClock.elapsedRealtime()+" got touch event " + ev);
        switch(ev.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	        	 mDragging = true;
	             //mDraggingStarted = SystemClock.elapsedRealtime();
	             mDragStartX = ev.getX();
	             mDragStartY = ev.getY();
	        	lastPosX=ev.getRawX();
	        	lastPosY=ev.getRawY();
	             if (getOrientation() == VERTICAL) {
	                 mPointerOffset = multiplier*ev.getRawY() - getPrimaryContentSize();
	             } else {
	                 mPointerOffset = multiplier*ev.getRawX() - getPrimaryContentSize();
	             }
        	break;
	        case MotionEvent.ACTION_UP:
	        	//CMN.Log("!!!1");
				checkBar();
	        	mDragging = false;
	        	if(view.getClass()==SplitViewGuarder.class) {
	        		((SplitViewGuarder)view).dragIdx=-1;
	        	}
	            if(inf!=null)
	            	inf.onDrop(getPrimaryContentSize());
        	break;
	        case MotionEvent.ACTION_MOVE:
	        	int ret;
	        	OUT:
	        	if (getOrientation() == VERTICAL) {
		        	float deltaY=ev.getRawY()-lastPosY;
	        		ret = (int)(multiplier*ev.getRawY() - mPointerOffset);
		            if(inf!=null) ret=inf.preResizing(ret);
		            if(isSlik) {
		            	if(ret==getPrimaryContentSize()) {
		            		 if (getOrientation() == VERTICAL) {
		    	                 mPointerOffset = multiplier*ev.getRawY() - getPrimaryContentSize();
		    	             } else {
		    	                 mPointerOffset = multiplier*ev.getRawX() - getPrimaryContentSize();
		    	             }
		            	}
		            }
		            if(ret<0) break OUT;
	        		setPrimaryContentHeight(ret);
		            if(inf!=null) inf.SizeChanged(ret,deltaY);
	        	}
	        	else {
		        	float deltaX=ev.getRawX()-lastPosX;
	        		ret = (int)(multiplier*ev.getRawX() - mPointerOffset);
		            if(inf!=null) ret=inf.preResizing(ret);
		            if(ret<0) break OUT;
	                setPrimaryContentWidth(ret);
		            if(inf!=null) inf.SizeChanged(ret,deltaX);
	            }
	        	lastPosX=ev.getRawX();
	        	lastPosY=ev.getRawY();
        	break;
        }
        return true;
    }
    
    public boolean guarded=false,draged=false;

	private float lastX,lastY,OrgX,OrgY;
	int OrgSize;
	private static final float _5o12_=0.4166f;
	public boolean judger;
	
    public boolean onInterceptTouchEvent(MotionEvent ev) {
		lastX=ev.getX();
		lastY=ev.getY();
    	boolean ret = super.onInterceptTouchEvent(ev);
    	if(mHandle!=null && !guarded) {
    		switch(ev.getAction()) {
    			case MotionEvent.ACTION_POINTER_DOWN:
    			break;
		    	case MotionEvent.ACTION_DOWN:
		    		//CMN.Log("SPLITVIEW ???");
					if(scrollbar2guard!=null && !scrollbar2guard.isHidden()){
						scrollbar2guard.isWebHeld=true;
						scrollbar2guard.cancelFadeOut();
					}
		    		OrgSize = getPrimaryContentSize();
		    		OrgX=ev.getX();
		    		OrgY=ev.getY();
		    		
		    		float judgelet = getOrientation()==LinearLayout.VERTICAL?OrgY:OrgX;
		    		judger = judgelet>getHandleTop() && judgelet<getHandleBottom();
		    		//CMN.a.showT("getPointerCount"+ev.getPointerCount());
		    	break;
		    	case MotionEvent.ACTION_UP:
					//CMN.Log("!!!2");
		    		checkBar();
		    		if(draged) {
						draged = false;
					}
		    	return ret;
		    	case MotionEvent.ACTION_MOVE:
					//CMN.Log("SPLITVIEW ACTION_MOVE ", judger, getHandleTop(), getHandleBottom(), getOrientation()==LinearLayout.VERTICAL?OrgY:OrgX);
					if(judger) {
						//CMN.Log("!!!3");
			    		if(!draged) {
			    			draged=Math.abs(lastY-OrgY)>100;
			    			if(draged) {
			    				draged=true;
								ev.setAction(MotionEvent.ACTION_DOWN);//hahhaha
								onTouch(this, ev);
								//return ret;
			    			}
			    		}
					}
		    	break;
			}
    		if(draged) {
    			isDirty=true;
				//CMN.Log(Math.abs(OrgY - lastY), "asd", getHeight() * _5o12_);
    			if(draged) {//已经移出一定距离moved，而且判正draged
					onTouch(this, ev);
    				return ret;
    			}
    		}
    	}
		return ret;
    }

	public boolean dispatchTouchEvent(MotionEvent ev) {
    	//if(true) return super.dispatchTouchEvent(ev);
		//CMN.Log("dispatchTouchEvent",clickThrough, getAlpha(),ev.getY()<mPrimaryContent.getTop());
    	if(canClickThrough){
			if(ev.getAction()==MotionEvent.ACTION_DOWN){
				clickThroughEntered=false;
				float ey = ev.getY();
				if (ey < mPrimaryContent.getTop()) {
					if (PDICMainAppOptions.getIsoImgClickThrough() || getAlpha() == 0) {
						clickThroughEntered = true;
						return false;
					}

//					View sv = ((ViewGroup) findViewById(R.id.webSingleholder)).getChildAt(0);
//					if (sv != null) {
//						CMN.Log("sv", sv.getBottom(), ey);
//						if (ey > sv.getBottom()) {
//							clickThroughEntered = true;
//							return false;
//						}
//					}
				}
			}
			if(clickThroughEntered){
				return false;
			}
		}
    	return super.dispatchTouchEvent(ev);
	}

	private void checkBar() {
		if(scrollbar2guard!=null && !scrollbar2guard.isHidden()){
			scrollbar2guard.isWebHeld=false;
			scrollbar2guard.fadeOut();
		}
	}


	public View getHandle() {
        return mHandle;
    }

    public int getPrimaryContentSize() {
		if (getOrientation() == VERTICAL) {
			return mPrimaryContent.getMeasuredHeight();
		} else {
		 	return mPrimaryContent.getMeasuredWidth();
		}
    }

    public int getPrimaryContentSetSize() {
        if (getOrientation() == VERTICAL) {
            return mPrimaryContent.getHeight();
        } else {
         return mPrimaryContent.getWidth();
        }
    }
	
	
	public void setPrimaryContentSizeUnChanged(int newSize, boolean b) {
		if(getPrimaryContentSize()!=newSize) {
			setPrimaryContentSize(newSize, b);
		}
	}
	
    public boolean setPrimaryContentSize(int newSize,boolean ...x) {
    	//if(getPrimaryContentSize()!=newSize) {
			boolean ret;
			if (getOrientation() == VERTICAL) {
				ret = setPrimaryContentHeight(newSize,x);
			} else {
				ret = setPrimaryContentWidth(newSize,x);
			}
		
			if(inf!=null)
				inf.SizeChanged(newSize,0);
			return ret;
		//}
		//return false;
    }

	public boolean isDirty=false;
    
    private boolean setPrimaryContentHeight(int newHeight,boolean ...x) {
        // top handler always visible
        newHeight = Math.max(0, newHeight);
        // bottom handler always visible 
        //newHeight = Math.min(newHeight, getMeasuredHeight() - mHandle.getMeasuredHeight());
		if(mPrimaryContent.getParent()==this) {
			LayoutParams params = (LayoutParams) mPrimaryContent.getLayoutParams();
			if (x.length == 0)
				if (mSecondaryContent.getMeasuredHeight() < 1 && newHeight > params.height) {
					return false;
				}
			params.height = newHeight;
			params.weight = 0;
			requestLayout();
			//postOnAnimation(this::requestLayout);
			//mPrimaryContent.setLayoutParams(params);
			for (View VI : valves) {
				VI.setTranslationY(newHeight - sz_valv / 2 + sz_hdl / 2);
			}
		}
        return true;
    }

    private boolean setPrimaryContentWidth(int newWidth,boolean ...x) {
        // handler always visible
    	newWidth = Math.max(0, newWidth);
        // width minus handler width to make the handler always visible 
    	//newWidth = Math.min(newWidth, getMeasuredWidth() - mHandle.getMeasuredWidth());
		if(mPrimaryContent.getParent()==this) {
			LayoutParams params = (LayoutParams) mPrimaryContent.getLayoutParams();
			if (x.length == 0)
				if (mSecondaryContent.getMeasuredWidth() < 1 && newWidth > params.width) {
					return false;
				}
			params.width = newWidth;
			// set the primary content parameter to do not stretch anymore and use the width specified in the layout params
			params.weight = 0;
			mPrimaryContent.setLayoutParams(params);
			for (View VI : valves) {
				VI.setTranslationX(newWidth - sz_valv / 2 + sz_hdl / 2);
			}
		}
        return true;
    }

	public int getHandleTop() {
		//int fixedVal = getOrientation()==LinearLayout.VERTICAL?mHandle.getTop()+getTop():mHandle.getLeft()+getLeft();
		int fixedVal = getOrientation()==LinearLayout.VERTICAL?mHandle.getTop():mHandle.getLeft();
		return fixedVal-CompensationTop;
	}

	public int getHandleBottom() {
		//int fixedVal = getOrientation()==LinearLayout.VERTICAL?mHandle.getBottom()+getTop():mHandle.getRight()+getLeft();
		int fixedVal = getOrientation()==LinearLayout.VERTICAL?mHandle.getBottom():mHandle.getRight();
		return fixedVal+CompensationBottom;
	}

	public int getCompensationBottom() {
		return CompensationBottom;
	}

	public float getHandleLeft() {
		int fixedVal = getOrientation()==LinearLayout.VERTICAL?mHandle.getLeft()+getLeft():mHandle.getTop()+getTop();
		return fixedVal+ReservationLeft;
	}

	public float getHandleRight() {
		int fixedVal = getOrientation()==LinearLayout.VERTICAL?mHandle.getRight()+getLeft():mHandle.getBottom()+getTop();
		return fixedVal-ReservationRight;
	}

	public void addValve(View v) {
		valves.add(v);
	}

	public void SwitchingSides() {
		int cc = getChildCount();
		if(cc==2) {
			View last = getChildAt(cc - 1);
			removeView(last);
			addView(last, cc - 2);
			multiplier *= -1;
		}
		else if(cc==3) {
			View first = getChildAt(0);
			View last = getChildAt(cc - 1);
			removeView(first);
			removeView(last);
			addView(last, 0);
			addView(first);
			multiplier *= -1;
		}
	}
	
	public void setHandle(View handle) {
		mHandle = handle;
	}
}
    

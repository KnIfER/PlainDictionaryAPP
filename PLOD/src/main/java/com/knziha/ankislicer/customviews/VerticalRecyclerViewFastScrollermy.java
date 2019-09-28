package com.knziha.ankislicer.customviews;

import com.knziha.rbtree.RBTNode;
import com.knziha.rbtree.RBTree;
import com.knziha.rbtree.RashSet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class VerticalRecyclerViewFastScrollermy extends VerticalRecyclerViewFastScroller {
	protected RBTree<Integer> tree;//bookMarkTree;
	private Paint mPaint;
	public int timeLength=0;
	private float xLeft;
    private float xRight;
    
    //laggy,decrept.
	
	public VerticalRecyclerViewFastScrollermy(Context context) {
		super(context);
		ini();
	}
	
	public VerticalRecyclerViewFastScrollermy(Context context, AttributeSet attrs) {
		super(context, attrs);
		ini();
	}
	
	public VerticalRecyclerViewFastScrollermy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		ini();
	}
	
	
	public void ini(){
		setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);	
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.parseColor("#afFF0000"));
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}
	
	
	@Override
	public void onDraw(Canvas c) {
		//CMN.show(getPaddingTop()+":"+(getMeasuredHeight())+":"+getMeasuredWidth());
		if(showbm && tree!=null && timeLength != 0) {
			xLeft = getPaddingTop()+mHandle.getHeight()/2+mFakeRoot.getPaddingTop();
	        xRight = getMeasuredHeight() - getPaddingBottom()-mFakeRoot.getPaddingBottom()-mHandle.getHeight()/2;
			inOrderDraw(tree.getRoot(),c);
		}
		
		super.onDraw(c);
	}
	
    private void inOrderDraw(RBTNode<Integer> node,Canvas canvas) {
        if(node != null) {
        	
        	inOrderDraw(node.getLeft(), canvas);
        	//canvas.drawCircle((float)xLeft+(float)tree.key/(float)timeLength*(-xLeft + xRight), 25, 10.0f, mPaint);
        	//canvas.drawArc((float)xLeft+(float)tree.key/(float)timeLength*(-xLeft + xRight)-25,0,(float)xLeft+(float)tree.key/(float)timeLength*(-xLeft + xRight)+25,50,0,180,false,mPaint);
            canvas.drawLine(
            		0,
            		xLeft+(float)node.getKey()/(float)timeLength*(-xLeft + xRight),
            		getMeasuredWidth(), 
            		xLeft+(float)node.getKey()/(float)timeLength*(-xLeft + xRight),
            		mPaint);

            inOrderDraw(node.getRight(), canvas);
        }
    }
    boolean showbm;
	public void showBoolMark(boolean inSearch) {
		showbm = inSearch;
		invalidate();
	}

	public void setTree(RashSet<Integer> mSearchResTree) {
		tree = mSearchResTree;
		showbm = true;
	}


}

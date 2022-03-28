package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.R;

public class FlowCheckedTextView extends CheckedTextView {
	public final FlowTextView mFlowTextView;
	public int prejudice;
	
	public FlowCheckedTextView(Context context) {
		this(context, null);
	}
	public FlowCheckedTextView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.checkedTextViewStyle);
	}
	public FlowCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mFlowTextView = new FlowTextView(context, attrs, defStyleAttr);
		mFlowTextView.pad_right = (int) (GlobalOptions.density*8);
		if(getCheckMarkDrawable()!=null) {
			mFlowTextView.setPadding(
					(int) context.getResources().getDimension(R.dimen._18_)*2/3 + getPaddingLeft()
					,0
					,getCheckMarkDrawable().getIntrinsicWidth()
					,0);
		}
	}
	
//	@Override
//	public void setCheckMarkDrawable(int resId) {
//		super.setCheckMarkDrawable(resId);
//		if(getCheckMarkDrawable()!=null) {
//			mFlowTextView.setPadding(
//					(int) getContext().getResources().getDimension(R.dimen._18_)*2/3
//					,0
//					,getCheckMarkDrawable().getIntrinsicWidth()
//					,0);
//		}
//	}
//
//	@Override
//	public void setCheckMarkDrawable(@Nullable Drawable d) {
//		super.setCheckMarkDrawable(d);
//		if(getCheckMarkDrawable()!=null) {
//			mFlowTextView.setPadding(
//					(int) getContext().getResources().getDimension(R.dimen._18_)*2/3
//					,0
//					,getCheckMarkDrawable().getIntrinsicWidth()
//					,0);
//		}
//	}
	
	public void onDraw(Canvas canvas) {
		if(isChecked())super.onDraw(canvas); // 神奇，竟然还有博文背景
		if(prejudice>0){
			canvas.translate(0, prejudice);
		}
		mFlowTextView.draw(canvas);
		//CMN.Log("画了画了", mFlowTextView.getMeasuredHeight(), mFlowTextView.getMeasuredWidth());
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mFlowTextView.measure(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height = mFlowTextView.getMeasuredHeight();
		prejudice=0;
		int mH = getMeasuredHeight();
		if(height<mH) {
			prejudice = (mH-height)/2;
			height=mH;
		}
		setMeasuredDimension(mFlowTextView.getMeasuredWidth(), height);
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(null, BufferType.NORMAL);
		if(mFlowTextView!=null) {
			mFlowTextView.setText(text==null?null:text.toString());
		}
	}
	
	@Override
	public void setTextSize(float size) {
		mFlowTextView.setTextSize(size);
	}
}

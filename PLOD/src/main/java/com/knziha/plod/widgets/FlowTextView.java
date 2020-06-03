/*	Copyright 2014 Dean Wild. Copyright 2020 KnIfER.
*
*	Licensed under the Apache License, Version 2.0 (the "License");
*	you may not use this file except in compliance with the License.
*	You may obtain a copy of the License at
*
*	http://www.apache.org/licenses/LICENSE-2.0
*
*	Unless required by applicable law or agreed to in writing, software
*	distributed under the License is distributed on an "AS IS" BASIS,
*	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*	See the License for the specific language governing permissions and
*	limitations under the License.
*/

package com.knziha.plod.widgets;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.knziha.plod.ArrayList.ArrayListHolder;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.R;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simplified version for https://github.com/deano2390/FlowTextView/blob/master/flowtextview/src/main/java/uk/co/deanwild/flowtextview/FlowTextView.java <br/>
 * ———— The goal is to acquire full control over the drawing process within minimal cost, <br/>
 * 			which should yield the simplest resizable multi-line TextView. <br/>
 * Original comments:
 * FlowTextView is basically a TextView that has children that it avoids while laying itself out.
 * Considering that it is a TextView at heart, it makes sense for it to honor the TextView attributes
 * provided by Android. In its latest incarnation, it handles android:lineSpacingExtra and
 * android:lineSpacingMultiplier. I also adjusted the layout rules to attempt to account for the new
 * descendance and ascendance of the line height, but I'm not certain that is correct.
 * <p/>
 * This version also has some demorgan interestingness that I turned around to make less interesting,
 * and I added respect for children's margins when calculating text-drawing bounds.
 * <p/>
 * I also made minor performance improvements, as the original author had stated that they didn't
 */
public class FlowTextView extends View {
	public float margin=0;
	public int maxLines=-1;
	ArrayListHolder lineObjects = new ArrayListHolder(3);
	public int pad_right;
	private Drawable mActiveDrawable;
	public Drawable mRatingDrawable;
	private Drawable mLeftDrawable;
	private Drawable mRightDrawable;
	public Bitmap mCoverBitmap;
	private TextPaint mTextPaint;
	private TextPaint mFocusPaint;
	private float mTextsize;
	private int mTextColor = Color.BLACK;
	private Typeface typeFace;
	private int mDesiredHeight = 100; // height of the whole view
	private String mText = StringUtils.EMPTY;
	private String mTail;
	private int mGravity;
	
	private float mSpacingMult=1;
	private float mSpacingAdd;
	private int mLength;
	private int mStart;
	private Matcher SearchMatcher;
	private boolean postedCalcLayout;
	public boolean PostEnabled=false;
	private int mStarWidth;
	private final Paint.FontMetrics mTextMetrics;
	private float mTailLength;
	private RectF mCoverRect;
	private boolean Rating;
	private int lastMeasuredWidth;
	private int mLineHeight;
	private boolean mTextsize_MinueOne;
	private boolean bNeedInvalidate;
	
	public FlowTextView(Context context) {
		this(context, null);
	}
	
	public FlowTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public FlowTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowTextView);
			mSpacingAdd = ta.getDimensionPixelSize(R.styleable.FlowTextView_android_lineSpacingExtra, 0);
			mSpacingMult = ta.getFloat(R.styleable.FlowTextView_android_lineSpacingMultiplier, 1.0f);
			mTextsize = ta.getDimension(R.styleable.FlowTextView_android_textSize, 0);
			mTextColor = ta.getColor(R.styleable.FlowTextView_android_textColor, Color.BLACK);
			mStarWidth = ta.getDimensionPixelSize(R.styleable.FlowTextView_starWidth, 0);
			maxLines = ta.getInteger(R.styleable.FlowTextView_android_maxLines, -1);
			margin = ta.getDimension(R.styleable.FlowTextView_margin, 0);
			Rating = ta.getBoolean(R.styleable.FlowTextView_rating, false);
			mGravity = ta.getInteger(R.styleable.FlowTextView_android_gravity, 0);
			if(isInEditMode()){
				mActiveDrawable = ta.getDrawable(R.styleable.FlowTextView_android_src);
				setText(ta.getString(R.styleable.FlowTextView_android_text));
			}
			ta.recycle();
		}
		if(mTextsize==0) {
			mTextsize = dm.scaledDensity*16;
		}
		if(mStarWidth==0) {
			mStarWidth = (int) (dm.density*35);
		}
		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.density = dm.density;
		mTextPaint.setTextSize(mTextsize);
		mTextPaint.setColor(mTextColor);
		
		mTextMetrics = mTextPaint.getFontMetrics();
		
		onTextSizeChanged();
	}
	
	private void onTextSizeChanged() {
		mLineHeight = (int)((mTextMetrics.bottom-mTextMetrics.top) * mSpacingMult + mSpacingAdd);
	}
	
	/* text content */
	public void setText(String text) {
		if(text==null){
			text = StringUtils.EMPTY;
		}
		if(!mText.equals(text)) {
			mText = text;
			mLength = text.length();
			int suffix_index = text.lastIndexOf(".");
			if(suffix_index>0 && text.regionMatches(true,suffix_index+1, "mdx", 0, 3)){
				mLength-=4;
			}
			mStart=0;
			suffix_index = text.lastIndexOf("/", suffix_index>=0?suffix_index:text.length());
			if(suffix_index>0){
				mStart = suffix_index+1;
			}
			postCalcTextLayout();
		} else if(bNeedInvalidate){
			invalidate();
		}
	}
	
	public CharSequence getText() {
		return mText;
	}
	
	private int splitChunk(String text, int start, float maxWidth) {
		int break_length = start + mTextPaint.breakText(text, start, mLength,true, maxWidth, null);
		// if it's 0 or less, we can't fit any more chars on this line
		// if it's >= text length, everything fits, we're done
		// if the break character is a space, we're set
		if (maxLines>0 || break_length <= start || (break_length >= mLength || text.charAt(break_length - 1) == ' ')) {
			return break_length;
		} else if (text.charAt(break_length) == ' ') {
			return break_length + 1; // or if the following char is a space then return this length - it is fine
		}
		
		// otherwise, count back until we hit a space and return that as the break length
		int tempLength = break_length - 1;
		while (text.charAt(tempLength) != ' ') {
			tempLength--;
			if (tempLength <= start)
				return break_length; // if we count all the way back to 0 then this line cannot be broken, just return the original break length
		}
		
		return tempLength + 1; // return the nicer break length which doesn't split a word up
		
	}
	
	private void postCalcTextLayout() {
		if(getMeasuredWidth()==0){
			if(PostEnabled){
				post(this::calcTextLayout);
			} else {
				postedCalcLayout = true;
			}
		} else {
			calcTextLayout();
		}
	}
	
	private void calcTextLayout() {
		//CMN.Log("calcTextLayout", getText());
		float space_width = getMeasuredWidth();
		if(space_width==0){
			postDelayed(this::calcTextLayout, 350);
		}
		float paddingStart = getPaddingStart();
		float paddingEnd = getPaddingEnd();
		space_width -= paddingStart + paddingEnd + pad_right;
		postedCalcLayout = false;
		// set up some counter and helper variables we will us to traverse through the string to be rendered
		int charOffsetStart = mStart; // tells us where we are in the original string
		int charOffsetEnd; // tells us where we are in the original string
		int lineIndex = 0;
		float yOffset = 0;
		float xOffset = 0;
		int lineHeight = mLineHeight;// getLineHeight(); // get the height in pixels of a line for our current TextPaint
		//int paddingTop = getPaddingTop();
		float ascent = mTextPaint.getFontMetrics().ascent;
		
		if(mTail!=null||mRightDrawable!=null){
			space_width-=lineHeight;
		}
		
		if(mLeftDrawable!=null){
			space_width-=lineHeight;
		}
		
		if(mCoverBitmap !=null){
			space_width-=lineHeight;
		}
		
		lineObjects.clear(); // this will get populated with special html objects we need to render
		
		if (mLength > 0) { // is some actual text
			int textLeft = getPaddingLeft();
			if(mLeftDrawable!=null){
				textLeft+=mLineHeight;
			}
			if(mCoverBitmap !=null){
				textLeft+=mLineHeight;
			}
			while (charOffsetStart < mLength && (maxLines<=0||lineIndex<maxLines)) { // churn through the block spitting it out onto seperate lines until there is nothing left to render
				lineIndex++; // we need a new line
				yOffset =  (lineIndex-1) * lineHeight - ascent; // calculate our new y position based on number of lines * line height
				
				charOffsetEnd = splitChunk(mText, charOffsetStart, space_width);
				
				xOffset = textLeft;
				
				if(mGravity == Gravity.CENTER) {
					//CMN.Log("居中");
					float len = mTextPaint.measureText(mText, charOffsetStart, charOffsetEnd);
					if(len<space_width) {
						xOffset = (int) (xOffset + (space_width-len)/2 - paddingStart);
					}
				}
				
				lineObjects.add(charOffsetStart, charOffsetEnd, xOffset, yOffset);

				//if(htmlLine.end>mLength) htmlLine.end=mLength;
				
				charOffsetStart = charOffsetEnd;
			}
			if(maxLines==2) {
				if(maxLines==lineIndex) {
					float pad = 2.5f * mTextPaint.density;
					mTextPaint.setTextSize(mTextsize-pad);
					mTextsize_MinueOne = true;
					lineObjects.get(1).yOffset -= pad;
				} else if(mTextsize_MinueOne){
					mTextPaint.setTextSize(mTextsize);
					mTextsize_MinueOne = false;
				}
			}
		}
		
		int mDesiredHeight = (int) yOffset + lineHeight/2;
		if(mDesiredHeight != this.mDesiredHeight){
			//CMN.Log("变化了", mDesiredHeight, this.mDesiredHeight, mText, "<"+hashCode()+">");
			this.mDesiredHeight = mDesiredHeight;
			requestLayout();
		} else {
			invalidate();
		}
	}
	
	private boolean find_m(Matcher m) {
		while(m.find()) {
			if(m.start()>=mStart){
				return true;
			}
		}
		return false;
	}
	
	private boolean find_m_nonnull(Matcher m) {
		while(m.find()) {
			if(m.end()>m.start()){
				return true;
			}
		}
		return false;
	}
	
	private int StarLevel = 3;
	public final int MaxStarLevel = 5;
	/* INTERESTING DRAWING STUFF */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int size = lineObjects.size();
		int paddingTop = getPaddingTop();
		int paddingLeft = getPaddingLeft();
		int height = getMeasuredHeight();
		int width = getMeasuredWidth();
		
		if(mCoverBitmap !=null) {
			int RWidth = mLineHeight*5/6;
			int RStart = paddingLeft;
			int RTop = (height-RWidth)/2;
			mCoverRect.set(RStart, RTop, RStart+RWidth, RTop+RWidth);
			canvas.drawBitmap(mCoverBitmap, null, mCoverRect, mTextPaint);
		}
		
		if(mLeftDrawable!=null) {
			int RWidth = mLineHeight*5/6;
			int RStart = paddingLeft;
			int RTop = (height-RWidth)/2;
			if(mCoverBitmap!=null){
				RStart += mLineHeight;
			}
			mLeftDrawable.setBounds(RStart, RTop, RStart+RWidth, RTop+RWidth);
			mLeftDrawable.draw(canvas);
		}
		
		if(mRightDrawable!=null) {
			int RWidth = mLineHeight*5/6;
			int RStart = width - RWidth;
			int RTop = (height-RWidth)/2;
			if(mTail!=null){
				RTop -= mLineHeight/6;
			}
			mRightDrawable.setBounds(RStart, RTop, RStart+RWidth, RTop+RWidth);
			mRightDrawable.draw(canvas);
		}
		
		/* tail */
		if(mTail!=null) {
			float baseline=height;
			if(mRightDrawable==null) {
				float distance = (mTextMetrics.bottom - mTextMetrics.top) / 2 - mTextMetrics.bottom;
				baseline = baseline / 2 + distance;
			} else {
				baseline -= mLineHeight/6;
			}
			canvas.drawText(mTail, width-mTailLength, baseline, mTextPaint);
		}
		
		/* stars */
		if(mActiveDrawable!=null && (mRatingDrawable==null||!Rating) && StarLevel>0){
			drawStars(canvas, width, height, paddingTop, mLineHeight);
		}
		
		/* text */
		if(SearchMatcher!=null){
			initFocusedTextPainter();
		}
		LineObject htmlLine;
		int start;
		int i=0;
		float xOffset;
		float textTop = paddingTop + (height - (mLineHeight-(2*Utils.density))*size)/2;
		for (; i < size; i++) {
			htmlLine = lineObjects.get(i);
			start=htmlLine.start;
			xOffset = htmlLine.xOffset;
			if(SearchMatcher!=null) {
				int lastFind = -1;
				if((i==0&&find_m(SearchMatcher))||lastFind>=0)
				do {
					int now = SearchMatcher.start();
					int end = SearchMatcher.end();
					if(now>=htmlLine.start && now<htmlLine.end){// 如若落在这一行
						canvas.drawText(mText, start, now, xOffset, textTop+htmlLine.yOffset, mTextPaint);
						xOffset += mTextPaint.measureText(mText, start, now);
						start=now;
						if(end<htmlLine.end) {// 如若在这一行内结束
							canvas.drawText(mText, start, end, xOffset, textTop+htmlLine.yOffset, mFocusPaint);
							xOffset += mTextPaint.measureText(mText, start, end);
							start=end;
						} else {// 覆盖这一行的剩余部分 且 包含下一行的部分或全部
							canvas.drawText(mText, start, htmlLine.end, xOffset, textTop+htmlLine.yOffset, mFocusPaint);
							start=htmlLine.end;
							break;
						}
					} else if (start==htmlLine.start && end>=htmlLine.start && end<htmlLine.end){// 承接上文
						canvas.drawText(mText, start, end, xOffset, textTop+htmlLine.yOffset, mFocusPaint);
						xOffset += mTextPaint.measureText(mText, start, end);
						start=end;
					} else if (now<htmlLine.start&&end>=htmlLine.end){// 覆盖全部
						canvas.drawText(mText, htmlLine.start, htmlLine.end, 0, textTop+htmlLine.yOffset, mFocusPaint);
						start=htmlLine.end;
						break;
					} else {// 如若落在下一行或下下行
						lastFind = now;
						break;
					}
				} while(find_m_nonnull(SearchMatcher));
			}
			if(start<htmlLine.end && htmlLine.end<=mText.length()){
				canvas.drawText(mText, start, htmlLine.end, xOffset, textTop+htmlLine.yOffset, mTextPaint);
			}
		}
		
		/* stars */
		if(Rating && mRatingDrawable!=null && mActiveDrawable!=null){
			drawStars(canvas, width, height, paddingTop, mLineHeight);
		}
	}
	
	private void initFocusedTextPainter() {
		CMN.Log("initFocusedTextPainter");
		if(mFocusPaint==null) {
			mFocusPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			mFocusPaint.density = mTextPaint.density;
			mFocusPaint.setTextSize(mTextsize);
			mFocusPaint.setColor(Color.RED);
		}
		SearchMatcher.reset();
	}
	
	private void drawStars(Canvas canvas, int width, int height, int paddingTop, int lineHeight) {
		int starLeft = width - mStarWidth - getPaddingRight();
		if(mTail!=null||mRightDrawable!=null){
			starLeft -= lineHeight/2;
		}
		int starTop = paddingTop + (height - mStarWidth)/2;
		int starBottom = starTop+mStarWidth;
		int padding = (int) (mStarWidth*2/3+2*Utils.density);
		for (int i = 0; i < StarLevel; i++) {
			mActiveDrawable.setBounds(starLeft, starTop, starLeft+mStarWidth, starBottom);
			mActiveDrawable.draw(canvas);
			if(mRatingDrawable!=null) {
				mRatingDrawable.setBounds(mActiveDrawable.getBounds());
				mRatingDrawable.draw(canvas);
			}
			starLeft -= padding;
		}
		if(mRatingDrawable!=null&&Rating){
			for (int i = StarLevel; i < MaxStarLevel; i++) {
				mRatingDrawable.setBounds(starLeft, starTop, starLeft+mStarWidth, starBottom);
				mRatingDrawable.draw(canvas);
				starLeft -= padding;
			}
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		mTextPaint.getFontMetrics(mTextMetrics);
		bNeedInvalidate = false;
	}
	
	/* MINOR VIEW EVENTS */
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		postCalcTextLayout();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int width;
		int height;
		
		if (widthMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			width = widthSize;
			if(postedCalcLayout && width!=lastMeasuredWidth) {
				calcTextLayout();
			}
		} else {
			width = getMeasuredWidth();
		}
		
		if (heightMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			height = heightSize;
		} else {
			height = mDesiredHeight;
		}
		
		setMeasuredDimension(lastMeasuredWidth=width, height);
	}
	
	// GETTERS AND SETTERS
	// text size
	public float getTextsize() {
		return mTextsize;
	}
	
	public void setTextSize(float textSize) {
		this.mTextsize = textSize;
		mTextPaint.setTextSize(mTextsize);
		mFocusPaint.setTextSize(mTextsize);
		invalidate();
	}
	
	public int getTextColor() {
		return mTextColor;
	}
	
	public void setTextColor(int color) {
		if(mTextColor!=color){
			mTextColor = color;
			mTextPaint.setColor(mTextColor);
			bNeedInvalidate = true;
		}
	}
	
	public void setStarLevel(int NewStarLevel) {
		if(StarLevel!=NewStarLevel){
			StarLevel = NewStarLevel;
			bNeedInvalidate = true;
		}
	}
	
	/* typeface */
	public Typeface getTypeFace() {
		return typeFace;
	}
	
	public void setTypeface(Typeface type) {
		this.typeFace = type;
		mTextPaint.setTypeface(typeFace);
		invalidate();
	}
	
	/* line height */
	public int getLineHeight() {
		return Math.round(mTextPaint.getFontMetricsInt(null) * mSpacingMult
				+ mSpacingAdd);
	}
	
	public void SetSearchPattern(Pattern SearchPattern) {
		SearchMatcher = SearchPattern==null?null:SearchPattern.matcher(mText);
	}
	
	public void setCompoundDrawables(Drawable StarDrawable, Drawable LeftDrawable, Drawable RightDrawable, String Tail) {
		mActiveDrawable = StarDrawable;
		mLeftDrawable = LeftDrawable;
		mRightDrawable = RightDrawable;
		mTail = Tail;
		if(mTail!=null){
			mTailLength = mTextPaint.measureText(mTail);
		}
	}
	
	public void setCover(Drawable cover) {
		if(cover instanceof BitmapDrawable){
			mCoverBitmap = ((BitmapDrawable)cover).getBitmap();
			if(mCoverRect==null){
				mCoverRect = new RectF();
			}
		} else {
			mCoverBitmap = null;
		}
	}
	
	public void setStarLevelByClickOffset(float x0, float x) {
		int sw = (int) (mStarWidth*2/3+2*Utils.density);
		if(x0>=(sw+1)*MaxStarLevel){
			int newLevel = Math.round((getMeasuredWidth() - x)/sw);
			if(newLevel<0){
				newLevel=0;
			} else if(newLevel>MaxStarLevel) {
				newLevel=MaxStarLevel;
			}
			if(newLevel!=StarLevel){
				StarLevel=newLevel;
				invalidate();
			}
		} else if(x0<(sw+2)*MaxStarLevel){
			Context ctx = getContext();
			ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData mClipData = ClipData.newPlainText("PLOD", getText());
			cm.setPrimaryClip(mClipData);
			if(ctx instanceof MainActivityUIBase){
				((MainActivityUIBase)ctx).showT("已复制词典名称!");
			}
		}
	}
	
	public void setStarDrawables(Drawable activeStarDrawable, Drawable ratingStarDrawable) {
		mActiveDrawable = activeStarDrawable;
		mRatingDrawable = ratingStarDrawable;
	}
	
	public int getStarLevel() {
		return StarLevel;
	}
	
	public static class LineObject {
		public int start;
		public int end;
		public float yOffset;
		public float xOffset;
		public LineObject(int start, int end, float xOffset, float yOffset) {
			set(start, end, xOffset, yOffset);
		}
		
		public void set(int start, int end, float xOffset, float yOffset) {
			this.start = start;
			this.end = end;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e){
		if(margin==0){
			return super.onTouchEvent(e);
		} else {
			float x = e.getX();
			if(x> margin && x<=getWidth()-margin)
				return super.onTouchEvent(e);
			return true;
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		calcTextLayout();
	}
}

package com.knziha.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;

import java.lang.reflect.Field;

public class SelectableTextView extends TextView implements View.OnClickListener, View.OnLongClickListener {
    public ScrollViewHolder sv;
    public boolean bIntervalSelectionIntented;
    boolean RelocateRightHandleRequested;
    int overshoot;
    public SelectableTextViewCover textCover;
    public SelectableTextViewBackGround textCover2;
    //public FrameCover textCover3;
    Drawable handleLeft;
    Drawable handleRight;
    int selEnd;
    int selStart=-1;
    float selStartPosX,
          selStartPosY;
    float   selEndPosX,
            selEndPosY;
    BreakIteratorHelper boundary;
    int BackGroundColor;
    public boolean bNeedInvalidate;
    public int mSEnd;
    public int mSStart;
    private final Runnable mRunRedraw = new Runnable(){
        @Override
        public void run() {
            if(draggingHandle!=null) {
                float posX;
                float posY;
                boolean isLeft = draggingHandle==handleLeft;
                if(isLeft){
                    posX = selStartPosXStamp + lastX - orgX;
                    posY = selStartPosYStamp + lastY - orgY;
                }else{
                    posX = selEndPosXStamp + lastX - orgX;
                    posY = selEndPosYStamp + lastY - orgY;
                }

                //CMN.Log(getOffsetForPosition(posX,posY), selEnd);

                int line = getLayout().getLineForVertical((int) posY - getPaddingTop());
                float lineRight = getLayout().getLineRight(line);
                float lineLeft = getLayout().getLineLeft(line);
                if (posX < lineLeft|| posX > lineRight || getOffsetForPosition(posX,posY)!=(isLeft?selStart:selEnd)){//offset != selStart) {
                    if(RelocateRightHandleRequested){
                        selEndPosXStamp=lastX;
                        selEndPosYStamp=lastY;
                        RelocateRightHandleRequested=false;
                    }

                    int offset = getLayout().getOffsetForHorizontal(line, posX - getPaddingLeft());

                    //@hide
                    //line<getLineCount() &&  won't crash?
                    if(posX > lineRight && getLayout().getLineEnd(line)==getLayout().getLineStart(line+1)) {//posX > getLayout().getLineRight(line) &&
                        offset += 1;
                        overshoot=-1;
                    }else if(line>0 && posX < lineLeft  && getLayout().getLineEnd(line-1)==getLayout().getLineStart(line)){
                        offset -= 1;
                        overshoot=1;
                    }else
                        overshoot=0;

                    if(isLeft)
                        selStart = offset;
                    else
                        selEnd = offset;

                    int y = getPaddingTop()+getLayout().getLineBottom(line);
                    int trimA = getLayout().getLineStart(line);
                    //CMN.Log("trimA2",trimA, offset, overshoot);
                    if(offset>getText().length())offset=getText().length();
                    if(offset<trimA)offset=trimA;
                    float x = getPaddingLeft() + getPaint().measureText(getText(), trimA, offset);

                    if(isLeft){
                        selStartPosX = x;
                        selStartPosY = y - getLineHeight() / 2;
                        x -= draggingHandle.getIntrinsicWidth() * 1.f * 9 / 12*drawableScale;
                    }else{
                        selEndPosX = x;
                        selEndPosY = y - getLineHeight() / 2;
                        x -= draggingHandle.getIntrinsicWidth() * 1.f * 3 / 12*drawableScale;
                    }

                    draggingHandle.setBounds((int) x, y, (int) (x + draggingHandle.getIntrinsicWidth()*drawableScale), (int) (y + draggingHandle.getIntrinsicHeight()*drawableScale));

//                        orgX=lastX;
//                        orgY=lastY;
                    inflateSelectionPool();
                    if(textCover2!=null)textCover2.invalidate();//important?
                    textCover.invalidate();
                    //System.gc();
                }else{
                    //float x = handleLeft.getBounds().left+deltaX;
                    //int y = (int) (handleLeft.getBounds().top+deltaY);
//
                    //handleLeft.setBounds((int) x, y, (int) (x + handleLeft.getIntrinsicWidth()), y + handleLeft.getIntrinsicHeight());
                    //textCover.invalidate();
                }
            }
        }
    };

    public interface TextViewListener{
        void requestJudgeText(SelectableTextView selectableTextView);
    }
    TextViewListener mTextViewListener;
    boolean bIsInnerDecorater=true;
    public void setTextViewListener(TextViewListener mTextViewListener_){
        mTextViewListener=mTextViewListener_;
    }

    private void inflateSelectionPool() {
        if(selStart!=selEnd){
            mSStart=selStart;
            mSEnd=selEnd;
            if(mSStart>mSEnd){
                mSStart=selEnd;
                mSEnd=selStart;
            }
            if(textCover2!=null){
                textCover2.rectPool.clear();
                int lineStart = getLayout().getLineForOffset(mSStart);
                int lineEnd = getLayout().getLineForOffset(mSEnd);
                int cc=0;
                boolean isDrawSimple = true;//VICMainAppOptions.isDrawSimpleSelection();
                for (int currProcline = lineStart; currProcline <= lineEnd; currProcline++) {
                    //左
                    if(currProcline==lineStart) {
                        int left = getLayout().getLineStart(lineStart);
                        if(left<mSStart && left>=0)
                            left = (int) (getPaddingLeft() + getLayout().getLineLeft(lineStart) + getPaint().measureText(getText(),left, mSStart));
                        else
                            left = (int) (getPaddingLeft() +getLayout().getLineLeft(lineStart));

                        textCover2.rectPool.put(cc,left);
                    }else{
                        if(isDrawSimple)
                            textCover2.rectPool.put(cc,0);
                        else
                            textCover2.rectPool.put(cc,(int)(getPaddingLeft()+getLayout().getLineLeft(currProcline)));
                    }
                    cc++;
                    //顶
                    textCover2.rectPool.put(cc, getPaddingTop()+getLayout().getLineTop(currProcline));
                    cc++;
                    //右
                    if(currProcline==lineEnd) {
                        //textCover2.rectPool.put(cc,(int)(.5f + getPaddingLeft() + getLayout().getLineRight(lineEnd) - getPaint().measureText(getText().toString(),mSEnd,getLayout().getLineEnd(lineEnd))));
                        int right = getLayout().getLineStart(lineEnd);
                        if(right<mSEnd && mSEnd<=getText().length())
                            right = (int) (getPaddingLeft() + getLayout().getLineLeft(lineEnd) + getPaint().measureText(getText(),right, mSEnd));
                        else
                            right = (int) (getPaddingLeft() + getLayout().getLineLeft(lineEnd));
                        textCover2.rectPool.put(cc,right);
//                        textCover2.rectPool.put(cc,(int)(getPaddingLeft()+getLayout().getLineLeft(lineEnd)+getPaint().measureText(getText(),
//                                getLayout().getOffsetForHorizontal(lineEnd,0),mSEnd)));
                        //CMN.Log("measureText end ", getText().subSequence(mSEnd,getLayout().getLineEnd(lineEnd)));
                    }else {
                        if(isDrawSimple)
                            textCover2.rectPool.put(cc,getWidth());
                        else
                            textCover2.rectPool.put(cc, (int) (getPaddingLeft() + getLayout().getLineRight(currProcline)));
                    }
                    cc++;
                    //底
                    textCover2.rectPool.put(cc, getPaddingTop()+getLayout().getLineBottom(currProcline));
                    cc++;
                    if(isDrawSimple)
                    if(currProcline==lineStart && lineEnd>lineStart){
                        currProcline = lineEnd-1;
                    }
                }
                if(isDrawSimple && cc==8 && lineEnd>lineStart+1){
                    textCover2.rectPool.put(8,  0);
                    textCover2.rectPool.put(9,  textCover2.rectPool.get(3));
                    textCover2.rectPool.put(10, getWidth());
                    textCover2.rectPool.put(11, textCover2.rectPool.get(5));
                }
            }
        }
    }

	public void inflateHighLightPoolPool(int startOff, int endOff, int highLightBg){
		if(endOff>startOff){
			if(textCover2!=null){
				textCover2.rectPool2.clear();
				int lineStart = getLayout().getLineForOffset(startOff);
				int lineEnd = getLayout().getLineForOffset(endOff);
				int cc=0;
				for (int currProcline = lineStart; currProcline <= lineEnd; currProcline++) {
					//左
					if(currProcline==lineStart) {
						int left = getLayout().getLineStart(lineStart);
						if(left<startOff && left>=0)
							left = (int) (getPaddingLeft() + getLayout().getLineLeft(lineStart) + getPaint().measureText(getText(),left, startOff));
						else
							left = (int) (getPaddingLeft() +getLayout().getLineLeft(lineStart));

						textCover2.rectPool2.put(cc,left);
					}else{
						textCover2.rectPool2.put(cc,(int)(getPaddingLeft()+getLayout().getLineLeft(currProcline)));
					}
					cc++;
					//顶
					textCover2.rectPool2.put(cc, getPaddingTop()+getLayout().getLineTop(currProcline));
					cc++;
					//右
					if(currProcline==lineEnd) {
						int right = getLayout().getLineStart(lineEnd);
						if(right<endOff && endOff<=getText().length())
							right = (int) (getPaddingLeft() + getLayout().getLineLeft(lineEnd) + getPaint().measureText(getText(),right, endOff));
						else
							right = (int) (getPaddingLeft() + getLayout().getLineLeft(lineEnd));
						textCover2.rectPool2.put(cc,right);
					}else {
						textCover2.rectPool2.put(cc, (int) (getPaddingLeft() + getLayout().getLineRight(currProcline)));
					}
					cc++;
					//底
					textCover2.rectPool2.put(cc, getPaddingTop()+getLayout().getLineBottom(currProcline));
					cc++;
				}
			}
			textCover2.highLightBg2=highLightBg;
			textCover2.invalidate();
		}
	}

    public SelectableTextView(Context context) {
        super(context);
        init();
    }
    public SelectableTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    float drawableScale=1.f;

    private void init() {
        boundary = new BreakIteratorHelper();
        //CMN.Log(boundary,"boundary");
        handleLeft=getResources().getDrawable(R.drawable.abc_text_select_handle_left_mtrl_dark);
        handleRight=getResources().getDrawable(R.drawable.abc_text_select_handle_right_mtrl_dark);
        setOnClickListener(this);
        setOnLongClickListener(this);
        if(PDICMainAppOptions.isLarge){
            drawableScale=1.5f;
        }
        handleLeft.setAlpha(200);
        handleRight.setAlpha(200);
    }

    public void instantiate(SelectableTextViewCover mSelectableTextViewCover, SelectableTextViewBackGround mSelectableTextViewBackGround, ScrollViewHolder scroller, FrameCover cover3) {
        textCover=mSelectableTextViewCover;
        textCover2=mSelectableTextViewBackGround;
        sv=scroller;
        textCover.tv = this;
        textCover.tvb = textCover2;
        if(textCover2!=null) textCover2.tv = this;
        if(textCover2!=null) textCover2.sv = sv;
        textCover.magH = (int) (getLineHeight()*3.f/2);
        textCover.magW = (int) (83*getResources().getDisplayMetrics().density*(PDICMainAppOptions.isLarge?2:1));
        sv.tv2guard = this;
    }

    public void selectAll() {
        setSelection(0, getText().length()-1);
    }

    public void setSelection(int start, int end) {
        selStart=start;
        selEnd=end;

        int line = getLayout().getLineForOffset(selStart);
        int y = getPaddingTop()+getLayout().getLineBottom(line);
        int trimA = getLayout().getLineStart(line);
        if(trimA>selStart)
            trimA=selStart;
        float x = getPaddingLeft()+getPaint().measureText(getText(), trimA, selStart);//TODO crash
        selStartPosX=x;
        selStartPosY=y-getLineHeight()/2;

        x-=handleLeft.getIntrinsicWidth()*1.f*9/12*drawableScale;
        handleLeft.setBounds((int)x,y,(int)(x+handleLeft.getIntrinsicWidth()*drawableScale), (int) (y+handleLeft.getIntrinsicHeight()*drawableScale));

        line = getLayout().getLineForOffset(selEnd);
        y = getPaddingTop()+getLayout().getLineBottom(line);
        trimA = getLayout().getLineStart(line);
        if(trimA>getText().length()-1)trimA=getText().length()-1;
        x = getPaddingLeft()+getPaint().measureText(getText(), trimA, selEnd);

        selEndPosX=x;
        selEndPosY=y-getLineHeight()/2;

        x-=handleLeft.getIntrinsicWidth()*1.f*3/12*drawableScale;
        handleRight.setBounds((int)x,y,(int)(x+handleRight.getIntrinsicWidth()*drawableScale), (int) (y+handleRight.getIntrinsicHeight()*drawableScale));

        inflateSelectionPool();

        textCover.postInvalidate();
        textCover2.postInvalidate();
    }

    @Override
    public void onClick(View v){
            if(startInDrag)
                return;
            bIntervalSelectionIntented=false;
            int line = getLayout().getLineForVertical((int) lastY - getPaddingTop());
            int offset = getLayout().getOffsetForHorizontal(line, lastX - getPaddingLeft());
            int wordEnd = boundary.following(offset);
            int wordStart = boundary.previous();
            if(wordStart<0 || wordEnd<0)
                return;
            //CMN.Log(getText().subSequence(wordStart, wordEnd));

            int y = getPaddingTop()+getLayout().getLineBottom(line);
            int trimA = getLayout().getLineStart(line);
            //CMN.Log("onClick trimA",getText().length() ,trimA, wordStart);
            if(trimA>wordStart)
                trimA=wordStart;
            float x = getPaddingLeft()+getPaint().measureText(getText(), trimA, wordStart);//TODO crash
            //CMN.Log("measureText",getText().subSequence(trimA, wordStart));

            selStart = wordStart;
            selStartPosX=x;
            selStartPosY=y-getLineHeight()/2;
            //CMN.Log("wordStart",wordStart, getOffsetForPosition(x,y));

            x-=handleLeft.getIntrinsicWidth()*1.f*9/12*drawableScale;
            handleLeft.setBounds((int)x,y,(int)(x+handleLeft.getIntrinsicWidth()*drawableScale), (int) (y+handleLeft.getIntrinsicHeight()*drawableScale));


            line = getLayout().getLineForOffset(wordEnd);
            y = getPaddingTop()+getLayout().getLineBottom(line);
            trimA = getLayout().getLineStart(line);
            if(wordEnd>getText().length())wordEnd=getText().length();
            if(trimA>getText().length()-1)wordEnd=trimA=getText().length()-1;
            x = getPaddingLeft()+getPaint().measureText(getText(), trimA, wordEnd);

            selEnd = wordEnd;
            selEndPosX=x;
            selEndPosY=y-getLineHeight()/2;
            //CMN.Log("wordEnd",wordEnd, getOffsetForPosition(x,y));

            x-=handleLeft.getIntrinsicWidth()*1.f*3/12*drawableScale;
            handleRight.setBounds((int)x,y,(int)(x+handleRight.getIntrinsicWidth()*drawableScale), (int) (y+handleRight.getIntrinsicHeight()*drawableScale));

            inflateSelectionPool();

            int alpha = 200;
            if(getText().subSequence(mSStart,mSEnd).toString().trim().equals("")){
                bNeedInvalidate=true;
                if(mTextViewListener!=null){
                    mTextViewListener.requestJudgeText(this);
                }
                alpha=20;
            }else
                bNeedInvalidate=false;
            if(handleLeft.getAlpha()!=alpha) {
                handleLeft.setAlpha(alpha);
                handleRight.setAlpha(alpha);
            }

//            textCover2.postInvalidate();//important
//            textCover.postInvalidate();
            if(textCover2!=null) textCover2.invalidate();//important
            textCover.invalidate();

            //System.gc();
    }

    @Override
    public boolean onLongClick(View v) {
        if(startInDrag)
            return false;
        if(selStart!=-1 && bIntervalSelectionIntented){
            bIntervalSelectionIntented=false;
            int line = getLayout().getLineForVertical((int) lastY - getPaddingTop());
            int offset = getLayout().getOffsetForHorizontal(line, lastX - getPaddingLeft());
            if(offset<mSStart && mSStart==selStart || offset>mSEnd && mSStart!=selStart){
                draggingHandle=handleLeft;
                selStartPosXStamp=lastX;
                selStartPosYStamp=lastY;
            }else{
                draggingHandle=handleRight;
                selEndPosXStamp=lastX;
                selEndPosYStamp=lastY;
            }
            mRunRedraw.run();
        }else{
            performClick();
            judgeDown();
            RelocateRightHandleRequested=true;
            draggingHandle=handleRight;
        }
        startInDrag=true;
        return true;
    }

    private void judgeDown() {
        overshoot=0;
        selStartPosXStamp=selStartPosX;
        selStartPosYStamp=selStartPosY;
        selEndPosXStamp=selEndPosX;
        selEndPosYStamp=selEndPosY;
        startInDrag=false;
        orgX=lastX;
        orgY=lastY;
    }

    boolean proceed;
    public static long lastTouchTime = 0;


    public float lastX,lastY,orgX,orgY,deltaX,deltaY;
    public Drawable draggingHandle;
    public static boolean isDragging;
    public boolean startInDrag;
    float selStartPosXStamp,selStartPosYStamp;
    float selEndPosXStamp
         ,selEndPosYStamp;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        deltaX = event.getX()-lastX;
        deltaY = event.getY()-lastY;
        lastX=event.getX();
        lastY=event.getY();
        int action = event.getAction();
        lastTouchTime= System.currentTimeMillis();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                isDragging=true;
                orgX=event.getX();
                orgY=event.getY();
                judgeDown();
                if(selStart>=0) {
                    if (handleLeft.getBounds().contains((int) orgX, (int) orgY)) {
                        startInDrag = true;
                        draggingHandle = handleLeft;
                        textCover.postInvalidate();
                    } else if (handleRight.getBounds().contains((int) orgX, (int) orgY)) {
                        startInDrag = true;
                        draggingHandle = handleRight;
                        textCover.postInvalidate();
                    }
                }
            break;
            case MotionEvent.ACTION_MOVE:
//                    removeCallbacks(mRunRedraw);
//                    post(mRunRedraw);
                //处理拖动选择
                mRunRedraw.run();
                int speed=10;
                if(draggingHandle!=null) {
                    if (sv.getHeight() - (lastY - sv.getScrollY()) <= 0) {//向下滚动
                        proceed = true;
//                    mHandler.sendEmptyMessage(0);
                        sv.scrollBy(0, speed);
//                    onTouchEvent(event);
                    } else if ((lastY - sv.getScrollY()) <= 0) {//向上滚动
                        sv.scrollBy(0, -speed);
                    } else
                        proceed = false;
                }
//                    int line = getLayout().getLineForVertical((int) selStartPosY);
            break;
            case MotionEvent.ACTION_UP:
                judgeClick();
            break;
        }
        return true;
    }


    public void judgeClick() {
        proceed=false;
        //@hide
        if(draggingHandle!=null){
            draggingHandle=null;
            textCover.invalidate();
        }
        isDragging=false;
        //System.gc();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //CMN.Log("tmy    onDraw"+getHeight());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if(boundary!=null && text!=null)
            boundary.setText(text.toString());
    }

    public void setTextField(CharSequence text) {
        try {
            Field mText_f_ = TextView.class.getDeclaredField("mText");
            mText_f_.setAccessible(true);
            mText_f_.set(this, text);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public void setTheme(int bgColor, int textColor, int heightLightColor, int heightLightColor2) {
        BackGroundColor = bgColor;
        setTextColor(Color.BLACK);
        if(textCover2!=null)textCover2.highLightBg=heightLightColor;
        handleLeft.setColorFilter(heightLightColor2, PorterDuff.Mode.SRC_IN);
        handleRight.setColorFilter(heightLightColor2, PorterDuff.Mode.SRC_IN);
    }

    @Override public int getSelectionStart() {
        return mSStart;
    }

    @Override public int getSelectionEnd() {
        return mSEnd;
    }
    @Override
    public boolean hasSelection() {
        return selStart!=-1;
    }

    public String getSelectedText() {
        if (!hasSelection()) {
            return null;
        }
       return  getText().subSequence(mSStart, mSEnd).toString();
    }

    /** clear text selection<p>
     * @return boolean whether selection is cleared or not*/
    public boolean clearSelection() {
        if(selStart!=-1){
            selStart=-1;
            selEnd=-1;
//            textCover.postInvalidate();
//            textCover2.postInvalidate();
            textCover.invalidate();
            if(textCover2!=null)textCover2.invalidate();
            if(textCover2!=null)textCover2.rectPool.clear();
            System.gc();
            setLayoutParams(getLayoutParams());
            sv.setLayoutParams(sv.getLayoutParams());
            postInvalidate();
            return true;
        }
        return false;
    }


}

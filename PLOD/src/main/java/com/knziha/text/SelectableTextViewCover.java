package com.knziha.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.R;

/** Cover for SelectableTextView which will draw selection handles and it's magnifier. */
public class SelectableTextViewCover extends View {
    SelectableTextView tv;
    /** highlight background */
    View tvb;
    /** Small Canvas for magnifier.
     * {@link Canvas#clipPath ClipPath} fails if the canvas it too high.
     * see <a href="https://issuetracker.google.com/issues/132402784">issuetracker</a>) */
    Canvas cc;
    Bitmap PageCache;
    BitmapDrawable PageCacheDrawable;

    Path magClipper;
    RectF magClipperR;
    float magFactor=1.5f;
    int magW=250;
    int magH=200;
    /** output image*/
    Drawable frameDrawable;
    private float framew;

    public SelectableTextViewCover(Context context) {
        super(context);
    }

    public SelectableTextViewCover(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectableTextViewCover(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        setLayerType(LAYER_TYPE_NONE,null);
        cc = new Canvas(PageCache=Bitmap.createBitmap(magW,magH,Bitmap.Config.ARGB_8888));
        PageCacheDrawable = new BitmapDrawable(getResources(), PageCache);
        frameDrawable = getResources().getDrawable(R.drawable.frame);
        framew=getResources().getDimension(R.dimen.framew);
        magClipper = new Path();
        magClipperR = new RectF(PageCacheDrawable.getBounds());
        magClipper.reset();
        magClipperR.set(0,0,magW,magH);
        magClipper.addRoundRect(magClipperR,framew/magFactor+5,framew/magFactor+5,Path.Direction.CW);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //if(true) return;
        if(tv!=null && tv.selStart!=-1){
            tv.handleLeft.draw(canvas);
            tv.handleRight.draw(canvas);
            if(tv.draggingHandle!=null && !tv.RelocateRightHandleRequested){
                if(PageCache==null)
                    init();
                // int x = (int) (tv.handleLeft.getBounds().left + tv.handleLeft.getIntrinsicWidth()-magW/2*magFactor+tv.getPaddingLeft());
                // int y = (int) (tv.handleLeft.getBounds().top - magH*magFactor -tv.getLineHeight());

                // int x = (int) (magOrgX + tv.lastX - tv.orgX);
                float selInterestPosX;
                int selInterestOff;
                // int y = (int) (magOrgY + tv.lastY - tv.orgY);
                if(tv.draggingHandle==tv.handleLeft){
                    selInterestPosX=tv.selStartPosX;
                    selInterestOff=tv.selStart;
                }else{
                    selInterestPosX=tv.selEndPosX;
                    selInterestOff=tv.selEnd;
                }
                selInterestOff+=tv.overshoot;

                int attachTarget;
                int LineInInterest = tv.getLayout().getLineForOffset(selInterestOff);
                if(LineInInterest>0)
                    attachTarget = tv.getLayout().getLineBaseline(LineInInterest-1);
                else
                    attachTarget = tv.getLayout().getLineTop(LineInInterest);

                int x = (int) (selInterestPosX - magW/2*magFactor );
                //int y = (int) (tv.draggingHandle.getBounds().top - magH*magFactor -tv.getLineHeight());
                int y = (int) (tv.getPaddingTop() + attachTarget - magH*magFactor -tv.getLineHeight()/3);
                if(y-tv.sv.getScrollY()<0)
                    y = (int) (tv.sv.getScrollY());

                cc.save();
                cc.clipPath(magClipper);//为了圆角
                cc.setMatrix(null);
                //cc.translate(-selInterestPosX+magW/2,-tv.getLayout().getLineTop(tv.getLayout().getLineForOffset(selInterestOff))-tv.getPaddingTop());
                cc.translate(-selInterestPosX+magW/2,-attachTarget-tv.getPaddingTop());

                cc.drawColor(tv.BackGroundColor);
                tvb.draw(cc);
                tv.draw(cc);
				tv.draggingHandle.draw(cc);
                cc.restore();

                PageCacheDrawable.setBounds(x,y, (int) (x+magW*magFactor), (int) (y+magH*magFactor));
                frameDrawable.setBounds(PageCacheDrawable.getBounds());

                PageCacheDrawable.draw(canvas);
                frameDrawable.draw(canvas);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}

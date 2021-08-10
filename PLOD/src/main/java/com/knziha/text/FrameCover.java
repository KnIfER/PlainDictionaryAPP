package com.knziha.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.CMN;

public class FrameCover extends FrameLayout {
    SelectableTextView tv;
    Paint highLightPainter;
    SparseIntArray rectPool = new SparseIntArray();

    public FrameCover(Context context) {
        super(context);
        init();
    }

    public FrameCover(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameCover(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        highLightPainter = new Paint();
        highLightPainter.setAntiAlias(false);
        highLightPainter.setStyle(Paint.Style.FILL);
        highLightPainter.setColor(Color.YELLOW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //if(true) return;
        CMN.Log("frame bgview    onDraw"+getHeight());
        super.onDraw(canvas);
        if(tv!=null){
            if(tv.selStart!=-1)
                for (int i = 0; i+3 < rectPool.size(); i+=4) {
                    canvas.drawRect(rectPool.get(i),rectPool.get(i+1),rectPool.get(i+2),rectPool.get(i+3),highLightPainter);
                }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}

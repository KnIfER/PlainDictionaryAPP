package com.knziha.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;

import androidx.annotation.Nullable;

public class SelectableTextViewBackGround extends View {
	public int highLightBg;
	public int highLightBg2;
	SelectableTextView tv;
    ScrollViewHolder sv;
	public Paint highLightPainter;
    SparseIntArray rectPool = new SparseIntArray();
    SparseIntArray rectPool2 = new SparseIntArray();

    public SelectableTextViewBackGround(Context context) {
        super(context);
        init();
    }

    public SelectableTextViewBackGround(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableTextViewBackGround(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_NONE,null);
        highLightPainter = new Paint();
        highLightPainter.setAntiAlias(false);
        highLightPainter.setStyle(Paint.Style.FILL);
        highLightPainter.setColor(Color.YELLOW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //if(true) return;
        if(tv!=null){
            if(tv.selStart!=-1){
				highLightPainter.setColor(highLightBg);
				for (int i = 0; i+3 < rectPool.size(); i+=4) {
					int svsy = sv.getScrollY();
					int y = tv.bIsInnerDecorater?0:svsy;
					int top  = rectPool.get(i+1);
					//if(top>svsy-tv.getLineHeight() && top<svsy+sv.getHeight())
					canvas.drawRect(rectPool.get(i), top - y, rectPool.get(i + 2), rectPool.get(i + 3) - y, highLightPainter);
				}
			}

			highLightPainter.setColor(highLightBg2);
			for (int i = 0; i+3 < rectPool2.size(); i+=4) {
				int svsy = sv.getScrollY();
				int y = tv.bIsInnerDecorater?0:svsy;
				int top  = rectPool2.get(i+1);
				//if(top>svsy-tv.getLineHeight() && top<svsy+sv.getHeight())
					canvas.drawRect(rectPool2.get(i), top - y, rectPool2.get(i + 2), rectPool2.get(i + 3) - y, highLightPainter);
			}
        }
    }
}

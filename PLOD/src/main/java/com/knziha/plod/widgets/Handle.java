package com.knziha.plod.widgets;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

@SuppressLint("ViewConstructor")
public class Handle extends View {

    RectF rectF;
    Paint p = new Paint();
    Integer mode;
    boolean expanded = false;
    Context context;
    Boolean rtl = false;

    public Handle(Context c, int m){
        super(c);

        context = c;
        mode = m;
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
}

    void setRightToLeft(boolean rtl){
        this.rtl = rtl;
    }
	Paint p2;
    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);

        p.setColor(Color.parseColor("#ffffff"));
        //p.setColor(Color.parseColor("#FF4081"));
        p.setTextSize(23);//22
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setStrokeWidth(2);
        p2 = new Paint(p);
		p2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		setLayerType(View.LAYER_TYPE_SOFTWARE, null); 
		setDelimiter("|||");
		p2.setTextAlign(Align.CENTER);
    }
    float delta;
    private String delimiter;
    void setDelimiter(String newShield) {
    	if(!newShield.equals(delimiter)) {
	    	delimiter = newShield;
	        Rect textBounds = new Rect();
	        p2.getTextBounds(newShield, 0, newShield.length(), textBounds);
	        delta = textBounds.exactCenterY();
	        invalidate();
    	}
    }
    public void collapseHandle(){
        expanded = true;
        rectF = new RectF(new Rect(getRight(),getTop(),getRight(),getBottom()));
        invalidate();
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
    }

    public void expandHandle(){
        expanded = false;
        rectF = makeRect();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if(mode == 0){
            rectF = makeRect();
        }
    }

    private RectF makeRect(){
        if(rtl){
            return new RectF(new Rect(getRight() - Utils.getDP(6, context),getTop(),getRight()+Utils.getDP(4, context),getBottom()));

        } else {
            return new RectF(new Rect(getLeft() - Utils.getDP(4, context),getTop(),getLeft() + Utils.getDP(6, context),getBottom()));
        }
    }

    Rect boundRect = new Rect();
    @Override
    protected void onDraw(Canvas canvas) {
    		super.onDraw(canvas);
            //canvas.getClipBounds(boundRect);
            //boundRect.inset(-Utils.getDP(30, context), 0); //make the rect larger

            //canvas.clipRect(boundRect, Region.Op.REPLACE);

            //canvas.drawArc(rectF, rtl ? 270F : 90F, 180F, false, p); //335
            //p.setColor(Color.parseColor("#ff0000ff"));
            //canvas.drawLine(0, 12, 12, 12, p);
            //canvas.drawLine(12, 12, 12, 24, p);
			canvas.rotate(90);

			//canvas.drawText("< >", getHeight()*7/24 ,-getWidth()/3,p2);
	        //canvas.drawText("|||", getHeight()/2,  -getWidth()/3, p2);
			
	        canvas.drawText(delimiter, getHeight()/2,  -getWidth()/2-delta, p2);

			
			//canvas.drawText("|||", getHeight()*7/24 ,-getWidth()/3,p2);
			canvas.rotate(-90);
    		


    }
}

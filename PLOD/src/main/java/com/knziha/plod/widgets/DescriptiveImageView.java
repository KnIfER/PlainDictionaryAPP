package com.knziha.plod.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;

import com.knziha.plod.plaindict.R;

import org.apache.commons.lang3.StringUtils;

public class DescriptiveImageView extends ImageView {
	private final float offsetY;
	public String mText;
	public TextPaint textPainter;
	public static Paint shadowPainter;
	public boolean bNeedShadow=false;
	public boolean bDrawShadow=false;
	public RectF bShadowRect;
	private ColorFilter foregroundFilter;
	public boolean tint = true;
	
	
	public DescriptiveImageView(Context context) {
		this(context, null);
	}
	
	public DescriptiveImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DescriptiveImageViewSty);
		mText = a.getString(R.styleable.DescriptiveImageViewSty_android_text);
		setContentDescription(mText);
		offsetY = a.getDimension(R.styleable.DescriptiveImageViewSty_android_layout_y, 0);
		textPainter = createTextPainter(false);
		a.recycle();
	}
	
	
	static TextPaint global_painter;
	
	public static TextPaint createTextPainter(boolean upd) {
		if (global_painter==null||upd) {
			if (global_painter==null) global_painter = new TextPaint();
			global_painter.setColor(Color.WHITE);
			global_painter.setAntiAlias(true);
			global_painter.setTextSize(GlobalOptions.density*(GlobalOptions.isLarge?19:12));
		}
		return global_painter;
	}
	
	public static TextPaint newTextPainter() {
		TextPaint global_painter = null;
		if (global_painter==null) {
			if (global_painter==null) global_painter = new TextPaint();
			global_painter.setColor(Color.WHITE);
			global_painter.setAntiAlias(true);
			global_painter.setTextSize(GlobalOptions.density*(GlobalOptions.isLarge?19:12));
		}
		return global_painter;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (tint && VU.sForegroundFilter!=foregroundFilter) {
			foregroundFilter = VU.sForegroundFilter;
			setColorFilter(foregroundFilter);
			textPainter.setColorFilter(foregroundFilter);
			//postInvalidate();
		}
		if(bDrawShadow) {
			float round = 25;
			canvas.drawRoundRect(bShadowRect, round, round, shadowPainter);
			//CMN.Log("Shadow_Painted…");
		}
		super.onDraw(canvas);
		if(mText!=null&&textPainter!=null) {
			Paint.FontMetrics fontMetrics = textPainter.getFontMetrics();
			canvas.drawText(mText,(getWidth()-textPainter.measureText(mText))/2
					,offsetY==-1?
					(getMeasuredHeight()-fontMetrics.bottom-fontMetrics.top)/2
					:(offsetY+getHeight()-getPaddingBottom()-getPaddingTop()+fontMetrics.bottom-fontMetrics.ascent)
			, textPainter);
		}
	}
	
	@Override
	public void setImageDrawable(@Nullable Drawable drawable) {
		boolean check = getDrawable()!=drawable;
		super.setImageDrawable(drawable);
		if(bNeedShadow && check && Build.VERSION.SDK_INT>21 && drawable instanceof BitmapDrawable) {
			bDrawShadow = false;
			if(!GlobalOptions.isDark) {
				Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
				int w=bm.getWidth();
				int h=bm.getHeight();
				bDrawShadow = bm.getPixel(w/2,h/10)==Color.WHITE
					&&bm.getPixel(w/10,h/2)==Color.WHITE;
				//CMN.Log("bDrawShadow?", Integer.toHexString(bm.getPixel(bm.getWidth()/2,bm.getHeight()/10)));
			}
			if(bDrawShadow) {
				/* 绘制阴影，用于衬托白色背景的图标 */
				if(shadowPainter==null) {
					shadowPainter = new Paint();
					shadowPainter.setColor(0xffffffff);
					shadowPainter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
					shadowPainter.setShadowLayer(20f, 6.0f, 6.0f, 0x8affffff);
				}
				if(bShadowRect==null) {
					bShadowRect = new RectF();
					AdjustShadowRect();
				}
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if(bDrawShadow) {
			AdjustShadowRect();
		}
	}
	
	private void AdjustShadowRect() {
		int H = getMeasuredHeight()-getPaddingBottom()-getPaddingTop();
		int h = (int) (H*0.76);
		int left = (getMeasuredWidth() - h) / 2;
		int top = (H-h)/2+getPaddingTop();
		bShadowRect.set(left,top,left+h,top+h);
	}
	
	public void setText(String text){
		if(!StringUtils.equals(text, mText)) {
			mText = text;
			setContentDescription(text);
			invalidate();
		}
	}
}

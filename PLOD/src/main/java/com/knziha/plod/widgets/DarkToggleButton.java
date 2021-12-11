package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

import com.knziha.plod.plaindict.CMN;

import org.apache.commons.lang3.ArrayUtils;

public class DarkToggleButton extends View {
	private final static int rotation=0;
	private final static int maskCxRatio=1;
	private final static int maskCyRatio=2;
	private final static int maskRadiusRatio=3;
	private final static int circleRadiusRatio=4;
	private final int animatePropsCnt = 5 + 8;
	private final float[] values = new float[animatePropsCnt];
	private final float[] targetValues = new float[animatePropsCnt];
	private final float[] lastValues = new float[animatePropsCnt];
	boolean stateIsSun;
	
	int size;
	Paint paint;
	Paint mskPaint;
	Path mskPath = new Path();
	
	public static class SpringInterpolator implements Interpolator {
		private float factor;
		public SpringInterpolator(float factor) {
			this.factor = factor;
		}
		@Override
		public float getInterpolation(float input) {
			return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
		}
	}
	
	Interpolator interpolator = new SpringInterpolator(0.9f);
	float progress;
	boolean animating;
	long animatorTime;
	long duration = 1500;
	
	private final int SurroundCircleNum = 8;
	
	public DarkToggleButton(Context context) {
		this(context, null);
	}
	
	public DarkToggleButton(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.YELLOW);
		paint.setFilterBitmap(true);
		mskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mskPaint.setColor(Color.BLACK);
		//mskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // need to use software layer type.
		stateIsSun = true;
		values[rotation] = 180;
		values[maskCxRatio] = 1;
		values[maskCyRatio] = 0;
		values[maskRadiusRatio] = 0.125f;
		values[circleRadiusRatio] = 0.2f;
		for (int i = 0; i < 8; i++) {
			values[5+i] = 1;
		}
	}
	
	
	public void toggle() {
		stateIsSun = !stateIsSun;
		if (stateIsSun) {
			targetValues[rotation] = 180;
			targetValues[maskCxRatio] = 1;
			targetValues[maskCyRatio] = 0;
			targetValues[maskRadiusRatio] = 0.125f;
			targetValues[circleRadiusRatio] = 0.2f;
		} else {
			targetValues[rotation]        =  45;
			targetValues[maskCxRatio]     =  0.5f;
			targetValues[maskCyRatio]     =  0.18f;
			targetValues[maskRadiusRatio] =  0.35f;
			targetValues[circleRadiusRatio] = 0.35f;
		}
		System.arraycopy(values, 0, lastValues, 0, animatePropsCnt);
		int idx; float val = stateIsSun?1:0;
		for (int i = 0; i < 8; i++) {
			targetValues[5+i] = val;
		}
		animating=true;
		animatorTime = System.currentTimeMillis();
		invalidate();
	}
	//PaintFlagsDrawFilter mSetfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.BLACK);
		int W = getWidth();
		int H = getHeight();
		int CX = W/2;
		int CY = H/2;
		canvas.save();
		canvas.rotate(values[rotation], CX, CX);
		//canvas.setDrawFilter( mSetfil );
		
		canvas.save();
		mskPath.reset();
		mskPath.addCircle(W*values[maskCxRatio], H*values[maskCyRatio], W*values[maskRadiusRatio], Path.Direction.CCW);
		canvas.clipPath(mskPath, Region.Op.DIFFERENCE);
		canvas.drawCircle(CX, CY, W*values[circleRadiusRatio], paint);
		canvas.restore();
		
		//canvas.drawCircle(W*values[maskCxRatio], H*values[maskCyRatio], W*values[maskRadiusRatio], mskPaint);
		
		if (animating) {
			handleAnimation();
		}
		for (int i = 0; i < SurroundCircleNum; i++) {
			canvas.save();
			canvas.scale(values[i+5], values[i+5], CX, CY);
			float radians = (float) (Math.PI / 2 - i * 2 * Math.PI / SurroundCircleNum);
			float d = W / 3;
			float cx = (float) (CX + d * Math.cos(radians));
			float cy = (float) (CY - d * Math.sin(radians));
			canvas.drawCircle(cx, cy, W*0.05f, paint);
			canvas.restore();
		}
		canvas.restore();
	}
	
	private void handleAnimation() {
		long now = System.currentTimeMillis();
		long elapsed = now - animatorTime;
		if(elapsed>=duration) animating = false;
		progress = Math.min(1, elapsed*1.f/duration);
		float interp = interpolator.getInterpolation(progress);
		boolean toSun = stateIsSun;
		for (int i = 0; i < 5+(toSun?0:8); i++) {
			values[i] =  (1-interp)*lastValues[i] + interp*targetValues[i];
			//values[i] =  values[i] + interp*(targetValues[i]-values[i]);
		}
		if (toSun) {
			int idx;
			// SpringForce.STIFFNESS_MEDIUM
			float delayUnit = Math.max(5, Math.min(50, 1500f * 0.067f + 55));
			for (int i = 0; i < 8; i++) {
				idx = 5+i;
				interp = interpolator.getInterpolation(Math.max(0, Math.min(1, (now-animatorTime-delayUnit*i)*1.f/duration)));
				//interp = (Math.max(0, Math.min(1, (now-animatorTime-delayUnit*i)*1.f/duration)));
				values[idx] =  Math.min(1, (1-interp)*lastValues[idx] + interp*targetValues[idx]);
			}
		}
		if (animating) {
			postInvalidateOnAnimation();
		}
	}
	
	
}

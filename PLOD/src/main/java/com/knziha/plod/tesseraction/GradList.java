package com.knziha.plod.tesseraction;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class GradList extends LinearLayout {
	Paint paint;
	public GradList(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		
		int[] mColors = {0xFFF0F5F9, 0xFFD0D7DC};
		float density = context.getResources().getDisplayMetrics().density;
		int pad=(int) (10*density);
		LinearGradient linearGradient = new LinearGradient(0, pad, 0, 65*density-pad, mColors, null, Shader.TileMode.CLAMP);
		paint = new Paint();
		//paint.setColor(Color.YELLOW);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
		paint.setShader(linearGradient);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		canvas.drawRect(0,0,getWidth(),getHeight(),paint);
	}
	//https://stackoverflow.com/questions/10613581/android-draw-shadow-below-view-dispatchdraw-called-often-any-cache-missed
}

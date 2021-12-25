package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.R;

import java.util.Random;

public class DarkToggleButton extends View {
	/** 画布旋转角度 */
	private final static int rotation=0;
	/** 月蚀面X轴偏移比例 */
	private final static int maskCxRatio=1;
	/** 月蚀面Y轴偏移比例 */
	private final static int maskCyRatio=2;
	/** 月蚀面半径比例 */
	private final static int maskRadiusRatio=3;
	/** 日月面半径比例 */
	private final static int circleRadiusRatio=4;
	/** 云朵alpha */
	private final static int cloudAlpha = 5;
	/** 天空渐变色alpha */
	private final static int skyAlpha = 6;
	/** 上面的变量个数。 */
	private final static int BasicPropsNum = 7;
	/** 动画属性数量。上面几个个变量 + 太阳8方圆点的画布缩放数值 */
	private final static int animatePropsCnt = BasicPropsNum + 8;
	/** 太阳周围旋绕8方圆点，代表光芒。 */
	private final static int SurroundCircleNum = 8;
	
	/** 动画属性数组，存储当前状态的变量，等于|切换前|与|切换后|变量之间的插值。 */
	private final float[] values = new float[animatePropsCnt];
	/** 动画属性数组，存储切换后的变量。 */
	private final float[] targetValues = new float[animatePropsCnt];
	/** 动画属性数组，存储切换前的变量。 */
	private final float[] lastValues = new float[animatePropsCnt];
	/** 表示正在切换为或已切换至太阳状态。 */
	private boolean stateIsSun;
	
	/** 日月面最大直径（包围盒的宽度和高度） */
	private int size;
	private Paint sunPaint;
	private Paint shinePaint;
	//Paint mskPaint;
	private Path mskPath = new Path();
	private Paint bgPaint = new Paint();
	private Paint bmPaint = new Paint();
	
	Drawable drawableCloud;
	Bitmap bmCloud;
	private boolean bAnimationSuppressed;
	
	public interface AnimationUpdateListener {
		void onAnimationUpdate(float progress);
	}
	AnimationUpdateListener animatorListener;
	
	/** 弹簧插值器。
	 * https://blog.csdn.net/l474297694/article/details/79916864
	 * http://inloop.github.io/interpolator/
	 * pow(2, -10 * x) * sin((x - factor / 4) * (2 * PI) / factor) + 1 */
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
	
	/** 插值器 */
	private Interpolator springInterpolator = new SpringInterpolator(0.9f);
	/** progress大于此数值时，提前结束动画。通过观察曲线得知。 */
	private float springStopFactor = 0.705f;
	/** 0~1动画进程。 */
	private float progress;
	/** 表示动画正在播放 */
	private boolean animating;
	/** 记录动画开始时间 */
	private long animatorTime;
	/** 动画时长 */
	private long duration = 1720;
	
	/** 随机数生成器 */
	private Random rand = new Random(28517);
	/** 随机种子 */
	private int seed = 28517;
	/** 星空密度插值器 */
	private Interpolator starInterpolator = new AccelerateInterpolator();
	
	private RectF dstRect = new RectF();
	private int lastH;
	private float[] HSV = new float[]{0, 0.3f, 0.8f};
	
	public DarkToggleButton(Context context) {
		this(context, null);
	}
	
	public DarkToggleButton(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sunPaint.setColor(Color.YELLOW);
		// 若用 mskPaint 清除绘制的月面显示出月牙，则没有 clipPath 锯齿缺点，但是需要给视图关闭硬件加速。
		//mskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//mskPaint.setColor(Color.BLACK);
		//mskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // need to use software layer type.
		shinePaint = new Paint();
		shinePaint.setColor(Color.YELLOW);
		shinePaint.setShadowLayer(2, 5, 2, 0x7ffeed86);
		// 初始化数值
		stateIsSun = true;
		values[rotation] = 180;
		values[maskCxRatio] = 1;
		values[maskCyRatio] = 0;
		values[maskRadiusRatio] = 0.125f;
		values[circleRadiusRatio] = 0.2f;
		values[cloudAlpha] = 1f;
		values[skyAlpha] = 255;
		for (int i = 0; i < 8; i++) {
			values[BasicPropsNum+i] = 1;
		}
		drawableCloud = context.getResources().getDrawable(R.drawable.cloud10);
		bmCloud = ((BitmapDrawable)drawableCloud).getBitmap();
	}
	
	/** 切换日月状态，并开始播放动画。 */
	public void toggle(boolean animate) {
		stateIsSun = !stateIsSun;
		// 根据日月状态，设置插值终点。
		if (stateIsSun) {
			targetValues[rotation] = 180;
			targetValues[maskCxRatio] = 1;
			targetValues[maskCyRatio] = 0;
			targetValues[maskRadiusRatio] = 0.125f;
			targetValues[circleRadiusRatio] = 0.2f;
			targetValues[cloudAlpha] = 1.0f;
			targetValues[skyAlpha] = 255;
		} else {
			targetValues[rotation]        =  45;
			targetValues[maskCxRatio]     =  0;
			targetValues[maskCyRatio]     =  -0.32f;
			targetValues[maskRadiusRatio] =  0.35f;
			targetValues[circleRadiusRatio] = 0.35f;
			targetValues[cloudAlpha] = 0.0f;
			targetValues[skyAlpha] = 12;
		}
		float val = stateIsSun?1:0;
		for (int i = 0; i < 8; i++) {
			targetValues[BasicPropsNum+i] = val;
		}
		if (animate) {
			// 拷贝当前状态数值，作为插值起点。
			System.arraycopy(values, 0, lastValues, 0, animatePropsCnt);
			animating = true;
			animatorTime = System.currentTimeMillis();
		} else {
			System.arraycopy(targetValues, 0, values, 0, animatePropsCnt);
			animating = false;
		}
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int H, int oldw, int oldh) {
		super.onSizeChanged(w, H, oldw, oldh);
		if (lastH!=H) {
			bgPaint.setShader(new LinearGradient(0,0,0,(int)(H*0.96),new int[] {0xFF6fbfd6, 0xFFd2d5da},null,Shader.TileMode.CLAMP));
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		size = (int) (GlobalOptions.density * (GlobalOptions.isLarge?300:200));
		int R = size/2;
		int W = getWidth();
		int H = getHeight();
		if(R==0) R=W/2;
		int CX = (int) (W*0.25f);
		int CY = (int) (H*0.25f);
		//CMN.Log("DarkToggleButton::onDraw::", CMN.now(), W, H);
		// 天空渐变色
		canvas.drawColor(Color.BLACK);
		bgPaint.setAlpha((int) Math.max(0, Math.min(255, values[skyAlpha])));
		canvas.drawRect(0, 0, W, H, bgPaint);
		
		// 绘制星空
		rand.setSeed(seed); // 重设随机数生成器n
		float cloudAlphaVal = Math.max(0, values[cloudAlpha]);
		float starDensityVal = 1-cloudAlphaVal;
		if (starDensityVal>0) {
			float xMax = W*0.4f*starDensityVal;
			float yMax = H*0.25f*starDensityVal;
			int hmTimes = (int) (xMax + yMax);
			bmPaint.setAlpha(255);
			for(int i=0; i<=hmTimes; i++) {
				float randomX = (int) ((rand.nextFloat()* W)+1);
				int randomY = (int) Math.floor((starInterpolator.getInterpolation(rand.nextFloat())* H)+1);
				int randomSize = (int) Math.floor(rand.nextFloat()*2+2);
				float randomOpacityOne = rand.nextFloat()*9+1;
				float randomOpacityTwo = rand.nextFloat()*9+1;
				HSV[0] = rand.nextFloat()*360+1; // randomHue
				bmPaint.setColor(Color.HSVToColor((int) ((randomOpacityOne+randomOpacityTwo*255)), HSV));
				canvas.drawRect(randomX, randomY, randomX+randomSize, randomY+randomSize, bmPaint);
			}
			
		}
		
		// 绘制日月
		canvas.save();
		canvas.rotate(values[rotation], CX, CY); //旋转画布
		//sunPaint.setColor(Color.YELLOW);
		
		//sunPaint.setColor(ColorUtils.blendARGB(0xfffeed86, Color.YELLOW, starDensityVal));
		//sunPaint.setDither(true);
		
//		sunPaint.setShader(new RadialGradient(CX, CY, R*0.2f*2,
//				new int[]{
//						Color.YELLOW,
//						0xfffeed86,
//						0xfff9fba8}, new float[]{0.25f,0.35f,2.0f}, Shader.TileMode.CLAMP));
		
		
		canvas.save();
		
		// 绘制太阳的8方圆点
		float SurroundCircleScale;
		for (int i = 0; i < SurroundCircleNum; i++) {
			SurroundCircleScale = values[BasicPropsNum+i];
			if(SurroundCircleScale<0.2) break;
			canvas.save();  // 8方太阳圆点，次第缩放画布
			canvas.scale(SurroundCircleScale, SurroundCircleScale, CX, CY);
			float radians = (float) (Math.PI / 2 - i * 2 * Math.PI / SurroundCircleNum);
			float d = R / 3;
			float cx = (float) (CX + d * Math.cos(radians));
			float cy = (float) (CY - d * Math.sin(radians));
			canvas.drawCircle(cx, cy, R*0.05f, shinePaint);
			canvas.restore();
		}
		
		// 绘制日月圆面
		mskPath.reset();
		mskPath.addCircle(CX+R*values[maskCxRatio], CY+R*values[maskCyRatio], R*values[maskRadiusRatio], Path.Direction.CCW);
		canvas.clipPath(mskPath, Region.Op.DIFFERENCE); // 月蚀，遮挡月面显示出月牙。clipPath 有锯齿。
		canvas.drawCircle(CX, CY, R*values[circleRadiusRatio], sunPaint);
		canvas.restore();
		// 绘制月蚀，遮挡月面显示出月牙。使用 PorterDuff.Mode.CLEAR 需关闭硬件加速。
		//canvas.drawCircle(CX+R*values[maskCxRatio], CY+R*values[maskCyRatio], R*values[maskRadiusRatio], paint);
		
		if (animating) {
			handleAnimation();
		}
		
		canvas.restore();
		
		// 绘制云层
		if (cloudAlphaVal>0) {
			rand.setSeed(seed);  // 重设随机数生成器
			//rand = new Random();
			bmPaint.setAlpha((int) (127*cloudAlphaVal));
			for (int i = 0; i < 100; i++ ) {
				float imgW=256*0.55f*rand.nextFloat()*2;
				float y=H*0.9f + - rand.nextFloat() * rand.nextFloat() * H * 0.4f - 30;
				float x= -20 + rand.nextFloat() * W * 1.05f;
				dstRect.set(x, y, x+imgW, y+imgW);
				canvas.drawBitmap(bmCloud, null, dstRect, bmPaint);
			}
			
			bmPaint.setAlpha((int) ((stateIsSun?200:127)*cloudAlphaVal));
			for (int i = 0; i < 100; i++ ) {
				float imgW= 256*0.45f;
				float y= H*0.9682f + - rand.nextFloat() * rand.nextFloat() * H * 0.08f - 30;
				float x= -50 + rand.nextFloat() * W * 1.15f;
				dstRect.set(x, y, x+imgW, y+imgW);
				canvas.drawBitmap(bmCloud, null, dstRect, bmPaint);
			}
		}
	}
	
	/** 处理动画属性 */
	private void handleAnimation() {
		long now = System.currentTimeMillis();
		long elapsed = now - animatorTime;
		progress = Math.min(1, elapsed*1.f/duration);
		if(animatorListener!=null) {
			animatorListener.onAnimationUpdate(progress/springStopFactor);
		}
		if(elapsed>=duration || progress>=springStopFactor) animating = false;
		float interp = springInterpolator.getInterpolation(progress);
		boolean toSun = stateIsSun;
		// 5个动画属性的interp值是一致的，若正在切换为月亮，则8方圆点也使用一致的interp值。
		for (int i = 0; i < BasicPropsNum+(toSun?0:8); i++) {
			// 计算插值。
			values[i] =  (1-interp)*lastValues[i] + interp*targetValues[i];
		}
		if (toSun) {
			int idx;
			// 8方太阳圆点，用delayUnit次第缩放画布，分别计算interp值。
			float delayUnit = Math.max(5, Math.min(50, 1500f * 0.067f + 55)); // SpringForce.STIFFNESS_MEDIUM 等于 1500f
			for (int i = 0; i < 8; i++) {
				idx = BasicPropsNum+i;
				// 计算插值。
				interp = springInterpolator.getInterpolation(Math.max(0, Math.min(1, (now-animatorTime-delayUnit*i)*1.f/duration)));
				values[idx] =  Math.min(1, (1-interp)*lastValues[idx] + interp*targetValues[idx]);
			}
		}
		if (animating && !bAnimationSuppressed) {
			// onDraw中触发重绘。应该比直接invalidate更节省性能，类比于js的requestAnimationFrame和setTimeout。
			postInvalidateOnAnimation();
		}
	}
	
	/** 动画进度监听器。比如切换到一半的时候可以作些什么事情。 */
	public void setAnimationListener(AnimationUpdateListener animatorListener_) {
		animatorListener = animatorListener_;
	}
	
	/** 获取当前状态。 */
	public boolean stateIsNightMode() {
		return !stateIsSun;
	}
	
	/** 停止动画，跳转至结束状态。 */
	public void abortAnimation() {
		if (animating) {
			System.arraycopy(targetValues, 0, values, 0, animatePropsCnt);
			animating=false;
			invalidate();
		}
		bAnimationSuppressed = false;
	}
	
	public void stopAnimation() {
		bAnimationSuppressed = true;
	}
}

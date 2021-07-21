package androidx.appcompat.app;

import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.view.Surface;

public class GlobalOptions {
	public static boolean isLarge;
	public static boolean isDark;
	private static final float[] NEGATIVEMATRIX = {
			-1.0f,     0,     0,    0, 255, // red
			0, -1.0f,     0,    0, 255, // green
			0,     0, -1.0f,    0, 255, // blue
			0,     0,     0, 1.0f,   0  // alpha
	};
	public static final ColorMatrixColorFilter NEGATIVE = new ColorMatrixColorFilter(NEGATIVEMATRIX);
	public static final PorterDuffColorFilter WHITE = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
	public static final PorterDuffColorFilter BLACK = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);


	public static boolean debug;
	
	public final static boolean chromium=Build.MANUFACTURER.equals("chromium");
	
	public static float density;
	
	public static float densityDpi;

	public static int realWidth;
	
	public static int rotation;
	
	public static boolean shouldRecordMenuView;
	
	public static boolean screenIsVertical() {
		return rotation==Surface.ROTATION_0||rotation==Surface.ROTATION_180;
	}
}

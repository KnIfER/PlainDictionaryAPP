package androidx.appcompat.app;

import android.graphics.ColorMatrixColorFilter;

public class GlobalOptions {
	public static boolean isDark;
	private static final float[] NEGATIVEMATRIX = {
			-1.0f,     0,     0,    0, 255, // red
			0, -1.0f,     0,    0, 255, // green
			0,     0, -1.0f,    0, 255, // blue
			0,     0,     0, 1.0f,   0  // alpha
	};
	public static final ColorMatrixColorFilter NEGATIVE = new ColorMatrixColorFilter(NEGATIVEMATRIX);


	public static boolean debug;
}

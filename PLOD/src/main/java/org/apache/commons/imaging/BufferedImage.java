

package org.apache.commons.imaging;


import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Properties;

public class BufferedImage{
	public Canvas canvas;
	public Bitmap bitmap;
	public Bitmap.Config config;
	public BufferedImage(Bitmap raster) {
		bitmap=raster;
		config=bitmap.getConfig();
		//canvas=new Canvas(bitmap);

	}

	public int getWidth() {
		return bitmap.getWidth();
	}

	public int getHeight() {
		return bitmap.getHeight();
	}

	public int getRGB(int x, int y) {
		return bitmap.getPixel(x,y);
	}

	public void setRGB(int x, int y, int val) {
		bitmap.setPixel(x,y,val);
	}

	public void getRGB(int x, int y, int width, int i1, int[] row, int i2, int width1) {
	}
}

package org.apache.commons.imaging;

import android.graphics.Bitmap;

import org.apache.commons.imaging.common.BufferedImageFactory;

public class ManagedImageBufferedImageFactory implements
		BufferedImageFactory {

		@Override
		public BufferedImage getColorBufferedImage(final int width, final int height,
												   final boolean hasAlpha) {
			return new BufferedImage(Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888));
		}

		@Override
		public BufferedImage getGrayscaleBufferedImage(final int width, final int height,
													   final boolean hasAlpha) {
			return getColorBufferedImage(width, height, hasAlpha);
		}
	}
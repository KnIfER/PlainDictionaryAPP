package com.knziha.plod.slideshow;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class PdfPicLoaderFactory implements ModelLoaderFactory<PdfPic, Bitmap> {

	@NonNull
	@Override
	public ModelLoader<PdfPic, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
		return new PdfModelLoader();
	}

	@Override
	public void teardown() {

	}
}
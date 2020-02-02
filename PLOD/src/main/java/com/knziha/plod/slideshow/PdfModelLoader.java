package com.knziha.plod.slideshow;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.InputStream;

public class PdfModelLoader implements ModelLoader<PdfPic, Bitmap> {
	@Nullable
	@Override
	public LoadData<Bitmap> buildLoadData(@NonNull PdfPic mddPic, int width, int height, @NonNull Options options) {
		return new LoadData<>(new PdfPicSignature(mddPic.path), new PdfPicFetcher(mddPic));
	}

	@Override
	public boolean handles(@NonNull PdfPic mddPic) {
		return true;
	}
}
package com.knziha.plod.slideshow;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class MddPicLoaderFactory implements ModelLoaderFactory<MddPic, InputStream> {

	@NonNull
	@Override
	public ModelLoader<MddPic, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
		return new MddModelLoader();
	}

	@Override
	public void teardown() {

	}
}
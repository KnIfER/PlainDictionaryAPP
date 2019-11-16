package com.knziha.plod.slideshow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.InputStream;

public class MddModelLoader implements ModelLoader<MddPic, InputStream> {
	@Nullable
	@Override
	public LoadData<InputStream> buildLoadData(@NonNull MddPic mddPic, int width, int height, @NonNull Options options) {
		return new LoadData<>(new MddPicSignature(mddPic.path), new MddPicFetcher(mddPic));
	}

	@Override
	public boolean handles(@NonNull MddPic mddPic) {
		return true;
	}
}
package com.knziha.plod.slideshow;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;


public class PdfPicFetcher implements DataFetcher<Bitmap> {

	private final PdfPic model;

	public PdfPicFetcher(PdfPic model) {
		this.model = model;
	}

	public PdfPic getModel() {
		return model;
	}

	@Override
	public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
		String err=null;
		try {
			Bitmap res = model.createBitMap();
			if(res!=null) callback.onDataReady(res);
		} catch (Exception e){
			err=e.toString();
		}
		callback.onLoadFailed(new Exception("load mdd picture fail"+err));
	}

	@Override public void cleanup() {
	}
	@Override public void cancel() {
	}

	@NonNull
	@Override
	public Class<Bitmap> getDataClass() {
		return Bitmap.class;
	}

	@NonNull
	@Override
	public DataSource getDataSource() {
		return DataSource.LOCAL;
	}
}
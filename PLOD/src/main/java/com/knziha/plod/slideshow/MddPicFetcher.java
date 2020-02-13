package com.knziha.plod.slideshow;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.dictionary.mdictRes;

import org.apache.commons.imaging.BufferedImage;
import org.apache.commons.imaging.Imaging;
import org.xiph.speex.ByteArrayRandomOutputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MddPicFetcher implements DataFetcher<InputStream> {

	private final MddPic model;

	public MddPicFetcher(MddPic model) {
		this.model = model;
	}

	public MddPic getModel() {
		return model;
	}

	@Override
	public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
		String err=null;
		try {
			ByteArrayInputStream resTmp = null;
			for(mdictRes mddTmp:model.mdd){
				int idx = mddTmp.lookUp(model.path);
				if(idx!=-1) {
					resTmp = mddTmp.getResourseAt(idx);
					if (resTmp != null) break;
				}
			}
			if (resTmp != null) {
				if(model.path.endsWith(".tif")||model.path.endsWith(".tiff")){
					BufferedImage image = Imaging.getBufferedImage(resTmp, MainActivityUIBase.getTifConfig());
					ByteArrayRandomOutputStream bos = new ByteArrayRandomOutputStream((int) (resTmp.available()*2.5));
					image.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					resTmp=new ByteArrayInputStream(bos.toByteArray());
				}
				callback.onDataReady(resTmp);
				return;
			}
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
	public Class<InputStream> getDataClass() {
		return InputStream.class;
	}

	@NonNull
	@Override
	public DataSource getDataSource() {
		return DataSource.LOCAL;
	}
}
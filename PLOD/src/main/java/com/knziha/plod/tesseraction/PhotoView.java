package com.knziha.plod.tesseraction;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/** 双指调Zoom、//单指Focus */
public class PhotoView extends FrameLayout {
	private boolean tCamera;
	private Camera camera;
	private Camera.Parameters parms;
	private List<Integer> ratios;
	private int baseZoom;
	
	public PhotoView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setCameraMode(Camera camera) {
		if(tCamera = camera!=null) {
			this.camera = camera;
			parms = camera.getParameters();
			ratios = parms.getZoomRatios();
		}
	}
	
	@Override
	public void setTranslationX(float translationX) {
		if(!tCamera) super.setTranslationX(translationX);
	}
	
	@Override
	public void setTranslationY(float translationY) {
		if(!tCamera) super.setTranslationY(translationY);
	}
	
	@Override
	public void setScaleX(float scaleX) {
		if(!tCamera) super.setScaleX(scaleX);
		else {
			//int zoom = (int) scaleX;//(int) (baseZoom*scaleX);
			int zoom = (int) (baseZoom + (scaleX-baseZoom)/2);
			for (int i = 0; i < ratios.size(); i++) {
				if (ratios.get(i)>=zoom) {
					parms.setZoom(i);
					camera.setParameters(parms);
					//CMN.Log("setZoom::", i+"/"+parms.getMaxZoom(), ratios.get(i), zoom, scaleX);
					break;
				}
			}
		}
	}
	
	@Override
	public void setScaleY(float scaleY) {
		if(!tCamera) super.setScaleY(scaleY);
	}
	
	@Override
	public float getScaleX() {
		if(tCamera){
			baseZoom = ratios.get(parms.getZoom());
			//CMN.Log("baseZoom::", parms.getZoom(), baseZoom);
			return baseZoom;
		}
		return super.getScaleX();
	}
}

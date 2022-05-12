package com.knziha.plod.slideshow;

import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;

public class PhotoBrowserContext {
	PhotoBrowsingContext IBC;
	public float doubleClickZoomLevel1 = 0f;
	public float doubleClickZoomLevel2 = 0f;
	public float doubleClickPresetXOffset = 0f;
	public float lastX;
	public float lastY;
	public int presetZoom;
	public int pza;
	public int presetAlignment;
	public boolean doubleClk12;
	
	public void setIBC(PhotoBrowsingContext IBC) {
		if (this.IBC!=IBC) {
			this.IBC=IBC;
			if (IBC!=null) {
				doubleClickZoomLevel1 = IBC.doubleClickZoomLevel1;
				doubleClickZoomLevel2 = IBC.doubleClickZoomLevel2;
				doubleClickPresetXOffset = IBC.doubleClickPresetXOffset;
				presetZoom = IBC.getPresetZoomLevel();
				presetAlignment = IBC.getPresetZoomAlignment();
				doubleClk12 = IBC.getDoubleClick12();
			}
		}
	}
}

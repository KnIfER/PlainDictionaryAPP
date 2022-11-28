package com.knziha.plod.tesseraction;

public final class Options {
	/** not in power saving mode */
	boolean isPoweringMode = true;
	boolean tourchLight = false;
	public final boolean getTorchLight(){
		return tourchLight;
	}
	public final boolean toggleTorchLight(){
		return tourchLight=!tourchLight;
	}
	public final boolean getRemberTorchLight(){
		return false;
	}
	
	public final boolean getQRFrameDrawLaser(){
		return isPoweringMode;
	}
	public final boolean getQRFrameDrawLocations(){
		return isPoweringMode;
	}
	
	public final boolean getContinuousFocus(){
		return false;
	}
	public final boolean getLoopAutoFocus(){
		return false;
	}
	public final boolean getSensorAutoFocus(){
		return true;
	}
	
	
	public final boolean getTryAgainWithRotatedData(){
		return isPoweringMode;
	}
	public final boolean getTryAgainImmediately(){
		return false;
	}
	
	public final boolean getOneShotAndReturn(){
		return false;
	}
	
	public final boolean getRememberedLaunchCamera(){
		return true;
	}
	
	public final void setRememberedLaunchCamera(boolean val){
	}
	
	public final boolean getTryAgainWithInverted(){
		return isPoweringMode;
	}
	public final int getLaunchCameraType(){
		return 1;
	}
	
	
	public final boolean getDecode1DBar(){
		return true;
	}
	public final boolean getDecode2DBar(){
		return true;
	}
	public final boolean getDecode2DMatrixBar(){
		return true;
	}
	public final boolean getDecodeUPCBar(){
		return true;
	}
	public final boolean getDecodeAZTECBar(){
		return true;
	}
	public final boolean getDecodePDF417Bar(){
		return true;
	}
	
}

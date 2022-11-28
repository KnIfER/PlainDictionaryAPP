/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.plod.tesseraction;

import static com.knziha.plod.tesseraction.QRCameraUtils.findBestPreviewSizeValue;
import static com.knziha.plod.tesseraction.QRCameraUtils.getContinuousFocusing;
import static com.knziha.plod.tesseraction.QRCameraUtils.openDriver;
import static com.knziha.plod.tesseraction.QRCameraUtils.setBestExposure;
import static com.knziha.plod.tesseraction.QRCameraUtils.setFocusMode;
import static com.knziha.plod.tesseraction.QRCameraUtils.setTorch;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

import java.io.IOException;

/** This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * @author dswitkin@google.com (Daniel Switkin) */
public final class QRCameraManager implements SensorEventListener {
	public Camera camera;
	public int screenRotation;
	private ImageListener imageListener;
	
	private boolean previewing;
	
	/** false=save battery power, take a static photo then recognize! */
	public boolean realtime = true;
	
	private int requestedCameraId = -1;
	private int requestedFramingRectWidth;
	private int requestedFramingRectHeight;
	
	private boolean ContinuousFocusing;
	
	Manager mManager;
	
	/** Preview frames are delivered here, which we pass on to the registered
	 * handler. Make sure to clear the handler so it will only receive one
	 * message. */
	private Handler previewHandler;
	
	private SensorManager sensorManager;
	
	private Sensor directionSensor;
	
	private boolean focusing;
	
	private boolean registeredSensorListener;
	
	float mLastX;
	float mLastY;
	float mLastZ;
	
	public Point screenResolution = new Point();
	public Point cameraResolution = new Point();
	
	/** 摄像机参数 */
	Camera.Parameters parameters;
	
	public QRCameraManager(Manager manager, View rootView) {
		this.mManager=manager;
		imageListener = new ImageListener(rootView);
		sensorManager = (SensorManager) manager.getContext().getSystemService(Context.SENSOR_SERVICE);
		directionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		manager.getFramingRect(true);
	}
	
	/** Opens the camera driver and initializes the hardware parameters. */
	public synchronized void open() throws IOException {
		Camera new_camera = camera;
		if(new_camera!=null) {
			close();
		}
		new_camera = openDriver(requestedCameraId);
		
		if (new_camera == null) {
			throw new IOException();
		}
		camera = new_camera;
		
		imageListener.ready();
		
		ResetCameraSettings();
	}
	
	public void ResetCameraSettings() {
		Camera reset_camera = camera;
		DisplayMetrics dm = mManager.dm;
		screenResolution.x = dm.widthPixels;
		screenResolution.y = dm.heightPixels;
		if(mManager.isPortrait) { //竖屏更改4 preview size is always something like 480*320, other 320*480
			screenResolution.x = dm.heightPixels;
			screenResolution.y = dm.widthPixels;
		}
		findBestPreviewSizeValue(reset_camera.getParameters(), cameraResolution, screenResolution);
		//CMN.Log("Camera_resolution: " + cameraResolution, dm.widthPixels+"x"+dm.heightPixels);
		
		if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
			mManager.applyManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
			requestedFramingRectWidth=requestedFramingRectHeight=0;
		} else {
			mManager.getFramingRect(true);
		}
		
		parameters = reset_camera.getParameters();
		
		String parmsBackup = parameters.flatten();
		
		try {
			setDesiredCameraParameters(reset_camera, false);
		} catch (RuntimeException e) {
			// Driver failed. Reset:
			//CMN.Log("Camera_rejected!!!", e);
			if (parmsBackup != null) {
				parameters.unflatten(parmsBackup);
				try {
					reset_camera.setParameters(parameters);
					setDesiredCameraParameters(reset_camera, true);
				} catch (RuntimeException e1) {
					parameters = reset_camera.getParameters();
					// Well, darn. Give up
					//CMN.Log("Camera_rejected even in safe-mode !!!", e1);
				}
			}
		}
		ContinuousFocusing = getContinuousFocus() && getContinuousFocusing(parameters.getFocusMode());
		mManager.applyPreviewSize();
		decorateCameraSettings();
		imageListener.ready();
	}
	
	private boolean getContinuousFocus() {
		return true;
	}
	
	private boolean getSensorAutoFocus() {
		return true;
	}
	
	private boolean getTorchLight() {
		return false;
	}
	
	//todo opt
	public void decorateCameraSettings() {
		Camera decor_camera = camera;
		if(decor_camera!=null) {
			Camera.Parameters params = parameters;
			if(params==null) {
				params = parameters = decor_camera.getParameters();
			}
			setBestExposure(params, true);
			setTorch(params, getTorchLight());
			//setBestPreviewFPS(params);
			//setBarcodeSceneMode(params);
			decor_camera.setParameters(params);
//			if(!previewing) {
//				// it's a feature!
//				decor_camera.startPreview();
//				decor_camera.stopPreview();
//			}
		}
	}
	
	
	void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		//CMN.Log( ":::Initial camera parameters: " + parameters.flatten());
		
		if (safeMode) {
			CMN.Log("safe mode");
		}
		
		setFocusMode(parameters, getContinuousFocus(), safeMode);
		
		//parameters.setPreviewFormat(ImageFormat.JPEG);
		
		parameters.setRotation(90);
		
		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		//CMN.Log("setPreviewSize==", cameraResolution.x+"x"+cameraResolution.y);
		if(previewing) camera.stopPreview();
		
		camera.setParameters(parameters);
		
		if(previewing) camera.startPreview();
		
		
		/****************** 竖屏更改2 *********************/
		camera.setDisplayOrientation(mManager.isPortrait?90:0);
		
		Camera.Parameters afterParameters = camera.getParameters();
		
		Camera.Size afterSize = afterParameters.getPreviewSize();
		
		if (afterSize != null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			//CMN.Log("Camera said it supported preview size ", cameraResolution.x , 'x', cameraResolution.y, "| but after setting it, preview size is " , afterSize.width, 'x', afterSize.height);
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}
		//CMN.Log( ":::Final camera parameters: " + parameters.flatten());
	}
	
	public synchronized boolean isOpen() {
		return camera != null;
	}
	
	/** Closes the camera driver if still in use. */
	public void close() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}
	
	public void destroy() {
		pause();
		close();
		mManager.root.removeCallbacks(imageListener.FocusRunnable);
		imageListener.root=null;
		mManager=null;
	}
	
	/** Asks the camera hardware to begin drawing preview frames to the screen. */
	public void startPreview(SurfaceTexture texture) {
		Camera preview_camera = camera;
		if (preview_camera != null && !previewing) {
			previewing = true;
			if(texture!=null) {
				try {
					preview_camera.setPreviewTexture(texture);
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
			preview_camera.startPreview();
			imageListener.start();
		}
	}
	
	/**Tells the camera to stop drawing preview frames.*/
	public void pause() {
		if (camera != null) {
			previewing = false;
			imageListener.stop();
			decorateCameraSettings();
			try {
				camera.stopPreview();
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		pauseSensor();
	}
	
	public boolean isPreviewing() {
		return previewing;
	}
	
	/** A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the message.obj field */
	public synchronized void requestPreviewFrame() {
		if (previewing && realtime && camera!=null) {
			camera.setOneShotPreviewCallback(imageListener.ready(mManager.handler));
		}
	}
	

	/** Allows third party apps to specify the camera ID, rather than determine
	 * it automatically based on available cameras and their orientation.. */
	public synchronized void setManualCameraId(int cameraId/* -1 */) {
		requestedCameraId = cameraId;
	}
	
	public void autoFocus() {
		imageListener.start();
	}
	
	public void resumeSensor() {
		CMN.Log("resumeSensor::", getSensorAutoFocus());
		if(/*!ContinuousFocusing && */mManager.opt.getSensorAutoFocus()) {
			registeredSensorListener=true;
			sensorManager.registerListener(this, directionSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	public void pauseSensor() {
		if(registeredSensorListener) {
			registeredSensorListener=false;
			sensorManager.unregisterListener(this);
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		//CMN.Log("onSensorChanged::");
		//CMN.Log("onSensorChanged");
		/*if(ContinuousFocusing) {
			pauseSensor();
		} else */if(camera!=null) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			float theta=0.23f;
			if ((Math.abs(mLastX - x) > theta || Math.abs(mLastY - y) > theta || Math.abs(mLastZ - z) > theta)) {
				//CMN.Log("onSensorChanged");
				
				if (!focusing) {
					autoFocus();
				}
				
				mLastX = x;
				mLastY = y;
				mLastZ = z;

				try {
					mManager.stopTessRealTimeDecoding();
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do something here if sensor accuracy changes.
		// You must implement this callback in your code.
		//if (sensor == )
		{
			switch (accuracy) {
				case 0:
					System.out.println("onAccuracyChanged::Unreliable");
					break;
				case 1:
					System.out.println("onAccuracyChanged::Low Accuracy");
					break;
				case 2:
					System.out.println("onAccuracyChanged::Medium Accuracy");
					break;
				case 3:
					System.out.println("onAccuracyChanged::High Accuracy");
					break;
			}
		}
	}
	
	/** 摄像机回调 */
	class ImageListener implements Camera.AutoFocusCallback, Camera.PreviewCallback {
		private View root;
		private boolean useAutoFocus;
		private Runnable FocusRunnable=this::start;
		
		ImageListener(View root) {
			this.root = root;
		}
		
		public void ready() {
			String currentFocusMode = camera.getParameters().getFocusMode();
			useAutoFocus = /*Prefs*/ currentFocusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || currentFocusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO);
			//CMN.Log("Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
		}
		
		@Override
		public synchronized void onAutoFocus(boolean success, Camera theCamera) {
			//CMN.Log("onAutoFocus::", success);
			focusing=false;
			if(useAutoFocus) {
				if(mManager.opt.getLoopAutoFocus()) {
					postAutoFocus();
				}
			}
		}
		
		private void postAutoFocus() {
			View root=this.root;
			if(root!=null) {
				root.removeCallbacks(FocusRunnable);
				if(previewing && !ContinuousFocusing) {
					root.postDelayed(FocusRunnable, 1200L);
				}
			}
		}
		
		public synchronized void start() {
			if (useAutoFocus) {
				try {
					camera.autoFocus(this);
					focusing=true;
				} catch (Exception e) {
					CMN.Log(e);
					postAutoFocus();
				}
			}
		}
		
		public synchronized void stop() {
			postAutoFocus();
			if (useAutoFocus) {
				// Doesn't hurt to call this even if not focusing
				try {
					camera.cancelAutoFocus();
				} catch (Exception re) {
					// Have heard RuntimeException reported in Android 4.0.x+; continue?
					CMN.Log("Unexpected exception while cancelling focusing", re);
				}
			}
			ready(null);
		}
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			//CMN.Log("onPreviewFrame", CMN.tid());
			if (previewHandler != null) {
				Message message = previewHandler.obtainMessage(R.id.decode, data);
				message.arg1=0;
//				try {
//					mManager.dMan.tess.stop();
//				} catch (Exception e) {
//					CMN.Log(e);
//				}
				//CMN.Log("data_sent::", CMN.id(data));
				message.sendToTarget();
				//previewHandler = null;
			} else {
				CMN.Log("Got preview callback, but no handler or resolution available");
			}
		}
		
		public Camera.PreviewCallback ready(Handler handler) {
			previewHandler=handler;
			return this;
		}
	}
}

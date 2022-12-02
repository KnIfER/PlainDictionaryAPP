/*
 * Copyright (C) 2014 ZXing authors
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

import android.graphics.Point;
import android.hardware.Camera;

import com.knziha.plod.plaindict.CMN;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**Utility methods for configuring the Android camera. @author Sean Owen*/
public final class QRCameraUtils {
	private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
	private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
	private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
	private static final double MAX_ASPECT_DISTORTION = 0.15;
	private static final int MIN_FPS = 10;
	private static final int MAX_FPS = 20;
	private static final int AREA_PER_1000 = 400;

	public static void setFocusMode(Camera.Parameters parameters, boolean enableContinuous, boolean safeMode) {
		List<String> supportedFocusModes = parameters.getSupportedFocusModes();
		String focusMode = null;
		if(enableContinuous) {
			focusMode = findSettableValue(supportedFocusModes,
					Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
					Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		} else {
			focusMode = findSettableValue(supportedFocusModes, Camera.Parameters.FOCUS_MODE_AUTO);
		}
		
		// Maybe selected auto-focus but not available, so fall through here:
		if (!safeMode && focusMode == null) {
			focusMode = findSettableValue(supportedFocusModes, Camera.Parameters.FOCUS_MODE_MACRO, Camera.Parameters.FOCUS_MODE_EDOF);
		}
		
		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}
	}

	public static boolean getContinuousFocusing(String text) {
		return Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(text) || Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(text);
	}
	
	public static void setTorch(Camera.Parameters parameters, boolean on) {
		List<String> supportedFlashModes = parameters.getSupportedFlashModes();
		String flashMode;
		if (on) {
			flashMode = findSettableValue( supportedFlashModes, Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(supportedFlashModes, Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null && !flashMode.equals(parameters.getFlashMode())) {
			parameters.setFlashMode(flashMode);
		}
	}

	public static void setBestExposure(Camera.Parameters parameters, boolean lightOn) {
		int minExposure = parameters.getMinExposureCompensation();
		int maxExposure = parameters.getMaxExposureCompensation();
		float step = parameters.getExposureCompensationStep();
		if ((minExposure != 0 || maxExposure != 0) && step > 0.0f) {
			// Set low when light is on
			float targetCompensation = lightOn ? MIN_EXPOSURE_COMPENSATION
					: MAX_EXPOSURE_COMPENSATION;
			int compensationSteps = Math.round(targetCompensation / step);
			float actualCompensation = step * compensationSteps;
			// Clamp value:
			compensationSteps = Math.max(Math.min(compensationSteps, maxExposure), minExposure);
			
			if (parameters.getExposureCompensation() != compensationSteps) {
				//CMN.Log("Setting exposure compensation to " + compensationSteps + " / " + actualCompensation);
				parameters.setExposureCompensation(compensationSteps);
			}
		}
		// else { CMN.Log("Camera does not support exposure compensation"); }
	}

	public static void setBestPreviewFPS(Camera.Parameters parameters) {
		setBestPreviewFPS(parameters, MIN_FPS, MAX_FPS);
	}

	public static void setBestPreviewFPS(Camera.Parameters parameters, int minFPS, int maxFPS) {
		List<int[]> supportedPreviewFpsRanges = parameters
				.getSupportedPreviewFpsRange();
		//CMN.Log("Supported FPS ranges: " + toString(supportedPreviewFpsRanges));
		if (supportedPreviewFpsRanges != null && !supportedPreviewFpsRanges.isEmpty()) {
			int[] suitableFPSRange = null;
			for (int[] fpsRange : supportedPreviewFpsRanges) {
				int thisMin = fpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
				int thisMax = fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
				if (thisMin >= minFPS * 1000 && thisMax <= maxFPS * 1000) {
					suitableFPSRange = fpsRange;
					break;
				}
			}
			if (suitableFPSRange != null) {
				int[] currentFpsRange = new int[2];
				parameters.getPreviewFpsRange(currentFpsRange);
				if (!Arrays.equals(currentFpsRange, suitableFPSRange)) {
					//CMN.Log("Setting FPS range to " + Arrays.toString(suitableFPSRange));
					parameters.setPreviewFpsRange(suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
				}
			}
			//else { CMN.Log("No suitable FPS range?"); }
		}
	}

//	public static void setFocusArea(Camera.Parameters parameters) {
//		if (parameters.getMaxNumFocusAreas() > 0) {
//			Log.i(TAG,
//					"Old focus areas: " + toString(parameters.getFocusAreas()));
//			List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
//			Log.i(TAG, "Setting focus area to : " + toString(middleArea));
//			parameters.setFocusAreas(middleArea);
//		} else {
//			Log.i(TAG, "Device does not support focus areas");
//		}
//	}
//
//	public static void setMetering(Camera.Parameters parameters) {
//		if (parameters.getMaxNumMeteringAreas() > 0) {
//			Log.i(TAG, "Old metering areas: " + parameters.getMeteringAreas());
//			List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
//			Log.i(TAG, "Setting metering area to : " + toString(middleArea));
//			parameters.setMeteringAreas(middleArea);
//		} else {
//			Log.i(TAG, "Device does not support metering areas");
//		}
//	}
//
//	private static List<Camera.Area> buildMiddleArea(int areaPer1000) {
//		return Collections.singletonList(new Camera.Area(new Rect(-areaPer1000,
//				-areaPer1000, areaPer1000, areaPer1000), 1));
//	}
//
//	public static void setVideoStabilization(Camera.Parameters parameters) {
//		if (parameters.isVideoStabilizationSupported()) {
//			if (parameters.getVideoStabilization()) {
//				Log.i(TAG, "Video stabilization already enabled");
//			} else {
//				Log.i(TAG, "Enabling video stabilization...");
//				parameters.setVideoStabilization(true);
//			}
//		} else {
//			Log.i(TAG, "This device does not support video stabilization");
//		}
//	}

	public static void setBarcodeSceneMode(Camera.Parameters parameters) {
		if (!Camera.Parameters.SCENE_MODE_BARCODE.equals(parameters.getSceneMode())) {
			String sceneMode = findSettableValue(parameters.getSupportedSceneModes(), Camera.Parameters.SCENE_MODE_BARCODE);
			if (sceneMode != null) {
				CMN.Log("setBarcodeSceneMode!!!");
				parameters.setSceneMode(sceneMode);
			}
		}
		//else { CMN.Log("Barcode scene mode already set"); }
	}

	public static void setZoom(Camera.Parameters parameters, double targetZoomRatio) {
		if (parameters.isZoomSupported()) {
			Integer zoom = indexOfClosestZoom(parameters, targetZoomRatio);
			if (zoom == null) {
				return;
			}
			if (parameters.getZoom() != zoom) {
				//CMN.Log("Setting zoom to " + zoom);
				parameters.setZoom(zoom);
			}
		}
		//else { CMN.Log("Zoom is not supported"); }
	}

	private static Integer indexOfClosestZoom(Camera.Parameters parameters,
                                              double targetZoomRatio) {
		List<Integer> ratios = parameters.getZoomRatios();
		//CMN.Log("Zoom ratios: " + ratios);
		int maxZoom = parameters.getMaxZoom();
		if (ratios == null || ratios.isEmpty() || ratios.size() != maxZoom + 1) {
			//CMN.Log("Invalid zoom ratios!");
			return null;
		}
		double target100 = 100.0 * targetZoomRatio;
		double smallestDiff = Double.POSITIVE_INFINITY;
		int closestIndex = 0;
		for (int i = 0; i < ratios.size(); i++) {
			double diff = Math.abs(ratios.get(i) - target100);
			if (diff < smallestDiff) {
				smallestDiff = diff;
				closestIndex = i;
			}
		}
		//CMN.Log("Chose zoom ratio of " + (ratios.get(closestIndex) / 100.0));
		return closestIndex;
	}

	public static void setInvertColor(Camera.Parameters parameters) {
		if (!Camera.Parameters.EFFECT_NEGATIVE.equals(parameters .getColorEffect())) {
			String colorMode = findSettableValue( parameters.getSupportedColorEffects(), Camera.Parameters.EFFECT_NEGATIVE);
			if (colorMode != null) {
				parameters.setColorEffect(colorMode);
			}
		}
		//else { CMN.Log( "Negative effect already set"); }
	}

	
	public static List<Camera.Size> allPreviewSizeValue(Camera.Parameters parameters) {
		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		if (rawSupportedSizes != null) {
			// Sort by size, descending
			List<Camera.Size> supportedPreviewSizes = new ArrayList<>(rawSupportedSizes);
			Collections.sort(supportedPreviewSizes, (a, b) -> Integer.compare(b.height * b.width, a.height * a.width));
			return supportedPreviewSizes;
		}
		return new ArrayList<>();
	}

	
	public static void findBestPreviewSizeValue(Camera.Parameters parameters, Point cameraResolution, Point screenResolution) {
		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			CMN.Log("Device returned no supported preview sizes; using default");
			Camera.Size defaultSize = parameters.getPreviewSize();
			cameraResolution.set(defaultSize.width, defaultSize.height);
			return;
		}

		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<>(rawSupportedSizes);
		Collections.sort(supportedPreviewSizes, (a, b) -> Integer.compare(b.height * b.width, a.height * a.width));

		double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;

		// Remove sizes that are unsuitable
		Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
		while (it.hasNext()) {
			
			Camera.Size supportedPreviewSize = it.next();
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			
			CMN.Log("findBestPreviewSizeValue::", realWidth, realHeight);
			
			if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
				it.remove();
				continue;
			}

			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
			double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
			double distortion = Math.abs(aspectRatio - screenAspectRatio);
			if (distortion > MAX_ASPECT_DISTORTION) {
				it.remove();
				continue;
			}

			if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
				cameraResolution.set(realWidth, realHeight);
				//CMN.Log("Found preview size exactly matching screen size: " + exactPoint);
				return;
			}
		}
		
//		if (true) {
//			// 1920, 1088,
//			// 1920, 1080,
//			// 1600, 1200,
//			// 1440, 1080,
//			cameraResolution.set(1600, 1200);
//			return;
//		}

		// If no exact match, use largest preview size. This was not a great
		// idea on older devices because
		// of the additional computation needed. We're likely to get here on
		// newer Android 4+ devices, where
		// the CPU is much more powerful.
		if (!supportedPreviewSizes.isEmpty()) {
			Camera.Size largestPreview = supportedPreviewSizes.get(0);
			cameraResolution.set(largestPreview.width, largestPreview.height);
			//CMN.Log("Using largest suitable preview size: " + largestSize);
			return;
		}

		// If there is nothing at all suitable, return current preview size
		Camera.Size defaultPreview = parameters.getPreviewSize();
		//CMN.Log("No suitable preview sizes, using default: " + defaultPreview);
		cameraResolution.set(defaultPreview.width, defaultPreview.height);
	}

	private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					return desiredValue;
				}
			}
		}
		return null;
	}
	
//	public static String collectStats(CharSequence flattenedParams) {
//		return new StringBuilder(1000)
//			.append("BOARD=").append(Build.BOARD).append('\n')
//			.append("BRAND=").append(Build.BRAND).append('\n')
//			.append("CPU_ABI=").append(Build.CPU_ABI).append('\n')
//			.append("DEVICE=").append(Build.DEVICE).append('\n')
//			.append("DISPLAY=").append(Build.DISPLAY).append('\n')
//			.append("FINGERPRINT=").append(Build.FINGERPRINT).append('\n')
//			.append("HOST=").append(Build.HOST).append('\n')
//			.append("ID=").append(Build.ID).append('\n')
//			.append("MANUFACTURER=").append(Build.MANUFACTURER).append('\n')
//			.append("MODEL=").append(Build.MODEL).append('\n')
//			.append("PRODUCT=").append(Build.PRODUCT).append('\n')
//			.append("TAGS=").append(Build.TAGS).append('\n')
//			.append("TIME=").append(Build.TIME).append('\n')
//			.append("TYPE=").append(Build.TYPE).append('\n')
//			.append("USER=").append(Build.USER).append('\n')
//			.append("VERSION.CODENAME=").append(Build.VERSION.CODENAME).append('\n')
//			.append("VERSION.INCREMENTAL=").append(Build.VERSION.INCREMENTAL).append('\n')
//			.append("VERSION.RELEASE=").append(Build.VERSION.RELEASE).append('\n')
//			.append("VERSION.SDK_INT=").append(Build.VERSION.SDK_INT).append('\n')
//			.append(flattenedParams == null ? "" : flattenedParams.toString().replace(",", "\r"))
//			.toString();
//	}
	
	/** Opens the requested camera with {@link Camera#open(int)}, if one exists.
	 * @param cameraId camera ID of the camera to use. A negative value means "no preference"
 *                 -1: Opens a rear-facing camera with {@link Camera#open(int)}, if one exists, or opens camera 0.
	 * @return handle to {@link Camera} that was opened */
	public static Camera openDriver(int cameraId) {
		int numCameras = Camera.getNumberOfCameras();
		//CMN.Log("camera##"+numCameras);
		//if(true) return null;
		if (numCameras == 0) return null;
		
		boolean explicitRequest = cameraId >= 0;
		
		if (!explicitRequest) {
			// Select a camera if no explicit camera requested
			int index = 0;
			while (index < numCameras) {
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo(index, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					cameraId = index;
					break;
				}
				index++;
			}
		}
		if(cameraId<0) {
			cameraId=0;
		}
		if(cameraId>=numCameras) {
			cameraId=numCameras-1;
		}
		//CMN.Log("Opening camera #" + cameraId);
		return Camera.open(cameraId);
	}
}

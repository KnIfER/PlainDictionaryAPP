package com.knziha.plod.tesseraction;

import android.Manifest;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.knziha.plod.PlainUI.WordCamera;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.databinding.QuCiQiBinding;
import com.knziha.plod.widgets.ViewUtils;

import static com.knziha.plod.tesseraction.DecodeManager.*;
import static com.knziha.plod.widgets.ViewUtils.setOnClickListenersOneDepth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/** Main Menu :   <br>
 * QR Scanner <br>
 * QR Scanner RealTime<br>
 * Other 2d codebar Scanner <br>
 * OCR Scanner  <br>
 * OCR Scanner RealTime<br>
 * Tessdata Manager <br>
 * */
public class Manager implements View.OnClickListener {
	private final static String[] permissions = new String[]{Manifest.permission.CAMERA};
	
	Activity activity;
	public DisplayMetrics dm;
	
	PDICMainAppOptions opt;
	public boolean isPortrait=true;
	public int screenRotation;
	public boolean suspensed = true;
	QuCiQiBinding UIData;
	public QRCameraManager cameraManager;
	public QRActivityHandler handler;
	
	public View root;
	
	RectF framingRect=new RectF();
	
	public final DecodeManager dMan;
	int lastType=-1;
	boolean lastRealTime=false;
	private DecodeThread handlerThread;
	private boolean requestedResetHints;
	
	public int permissionCode;
	public int openCode;
	boolean viewingImg;
	
	public boolean autoSch = false;
	public boolean failed;
	
	public Manager(PDICMainAppOptions opt) {
		dMan = new DecodeManager(this);
		dMan.framingRect = framingRect;
		this.opt = opt;
	}
	
	public void readScreenOrientation(Context context, boolean change) {
		WindowManager windowService = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		screenRotation = windowService.getDefaultDisplay().getRotation();
		isPortrait = screenRotation== Surface.ROTATION_0 || screenRotation == Surface.ROTATION_180;
		if(change && cameraManager.isOpen()) {
			cameraManager.ResetCameraSettings();
		}
		dMan.setScrOrient(screenRotation, isPortrait);
	}
	
	/** @param activity the host activity
	 * @param parentView if null, then activity must not be null and setContentView will be called.
	 *                      otherwise the manager views will be inflated to it. */
	public void init(Activity activity, ViewGroup parentView, QuCiQiBinding UIData) {
		this.activity = activity;
		if(UIData==null) {
			if(parentView==null) {
				UIData = DataBindingUtil.setContentView(activity, R.layout.qu_ci_qi);
			} else {
				UIData = QuCiQiBinding.inflate(LayoutInflater.from(activity), parentView, true);
			}
		}
		this.UIData = UIData;
		//postInit();
		
		dm = opt.dm;
		root = UIData.getRoot();
		readScreenOrientation(activity, false);
		
		UIData.frameView.setViewDelegation(UIData.photoView);
		
		cameraManager = new QRCameraManager(this, UIData.getRoot());
		setRect(UIData.frameView.getFrameRect());
		
		setOnClickListenersOneDepth(UIData.toolbarContent, this, 1, null);
		setOnClickListenersOneDepth(UIData.toast, this, 1, null);
		setOnClickListenersOneDepth(UIData.fltTools, this, 999, null);
		UIData.frameView.setOnClickListener(this);
	}
	
	private void postInit() {
	}
	
	public void showMainMenu(Activity a, int requestCode) {
		permissionCode = requestCode;
		if (mWordCamera != null) {
			mWordCamera.onClick(UIData.title);
		}
	}
	
	public boolean onBack() {
		if(viewingImg && lastType!=-1) {
			applyImageSize(false); // ??? todo fix 打开图片后文本方框偏移
			suspensed = false;
			resumeCamera();
			applyPreviewSize();
			viewingImg = false;
			//tryOpenCamera(lastType, lastRealTime, activity, permissionCode);
			return true;
		}
		return false;
	}
	
	int[] UIStates = new int[15];
	
	int[] BtnImg = new int[] {
		R.drawable.ic_baseline_crop_24
		,R.drawable.ic_baseline_play_arrow_24
		,R.drawable.ic_rects
	};
	int[] BtnSt = new int[] {
		R.drawable.ic_baseline_crop_24
		,R.drawable.ic_baseline_photo_camera_24
	};
	int[] BtnDy = new int[] {
		R.drawable.ic_baseline_crop_24
		,R.drawable.ic_baseline_photo_camera_24
		,R.drawable.ic_rects
	};
	
	public static void setVisible(View v, boolean vis){
		v.setVisibility(vis?View.VISIBLE:View.GONE);
	}
	
	public void resetBtns() {
		int decodeType = dMan.decodeType;
//		setVisible(UIData.cropBtn, true);
//		setVisible(UIData.playBtn, viewingImg);
//		setVisible(UIData.cameraBtn, !viewingImg);
//		setVisible(UIData.rectsBtn, decodeType==0 && (viewingImg||cameraManager.realtime));
//		setVisible(UIData.laserBtn, decodeType==1 && !viewingImg && cameraManager.realtime);
	}
	
	public void refreshUI() {
		int alpha=cameraManager.isPreviewing()?255:128;
		if(UIStates[0]!=alpha) {
			UIData.camera.getDrawable().setAlpha(alpha);
			UIStates[0]=alpha;
		}
		int decodeType = dMan.decodeType;
		int realTime_QROCR = Math.max(0, decodeType) | ((cameraManager.realtime?1:0)<<1);
		if (ViewUtils.checkSetVersion(UIStates, 1, realTime_QROCR)) {
			UIData.title.setText(decodeType==0?"文本识别":"条码识别");
		}
		int ui_pr=decodeType<<1|(isRealTime()?1:0);
		UIData.frameView.preset(ui_pr);
		resetBtns();
//		LayerDrawable ld = (LayerDrawable) UIData.torch.getDrawable();
//		ld.getDrawable(0).setAlpha(torchLight?255:64);
//		ld.getDrawable(1).setAlpha(torchLight?255:128);
	}
	
	
	// click
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
//			case R.id.tv1:{
////					ui_camera_btn_vis(2);
////					opt.setRememberedLaunchCamera(true);
//				resumeCamera();
//				refreshUI();
//			} break;
			default:{
				if (mWordCamera != null) mWordCamera.onClick(v);
			} break;
			case R.id.folder: {
				if(activity!=null) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					try {
						activity.startActivityForResult(intent, openCode);
					} catch (Exception e) {
						CMN.Log(e);
						Toast.makeText(activity, "打开失败", Toast.LENGTH_SHORT).show();
					}
				}
			} break;
			case R.id.frame_view: {
				CMN.Log("click!!!", UIData.frameView.lastX, UIData.frameView.lastY);
				if (cameraManager.isPreviewing()) {
					cameraManager.autoFocus();
				}
				if (mWordCamera.paused) {
					mWordCamera.onResume();
				}
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						dMan.decodeWord();
//					}
//				}).start();

//				getHandler().removeMessages(R.id.decode);
//				Message message = getHandler().ready().obtainMessage(R.id.decode, bitmap);
//				message.arg1=R.id.decode3;
//				message.sendToTarget();
			} break;
			/* turn on/off camera */
			case R.id.camera: {
				if(cameraManager.isPreviewing()) {
					suspensed = true;
					pauseCamera();
					if(bitmap!=null) {
						applyImageSize(false);
						viewingImg = true;
					}
				} else {
					suspensed = false;
					resumeCamera();
					if(viewingImg) {
						applyPreviewSize();
						viewingImg = false;
					}
				}
				refreshUI();
				//111opt.setRememberedLaunchCamera(false);
			} break;
			case R.id.cameraBtn:{
				CMN.Log("takePicture!!");
				try {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						cameraManager.camera.enableShutterSound(false);
					}
				} catch (Exception ignored) { }
				try {
					cameraManager.camera.takePicture(new Camera.ShutterCallback() {
						@Override
						public void onShutter() {
							suspensed = true;
						}
					}, null, null, new Camera.PictureCallback() {
						@Override
						public void onPictureTaken(byte[] data, Camera camera) {
							Bitmap bm = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
							int bw=bm.getWidth(),bh=bm.getHeight();
							if(bw>0 && bh>0) {
								if(bw>bh ^ sWidth>sHeight) {
									Matrix matrix = new Matrix();
									matrix.postRotate(90);
									bm = Bitmap.createBitmap(bm, 0, 0, bw, bh, matrix, true);
								}
							}
							setImage(bm, true);
						}
					});
				} catch (Exception e) {
					CMN.debug(e);
				}
			} break;
			case R.id.playBtn:{
				decode();
			} break;
			case R.id.cropBtn:{
				UIData.frameView.setCropping(!UIData.frameView.isCropping());
			} break;
			case R.id.ivBack:{
//				finish();
			} break;
			case R.id.torch: {
				if(cameraManager.isOpen()) {
					lightOn = !lightOn;
					cameraManager.decorateCameraSettings();
					refreshUI();
				}
			} break;
//			case R.id.tools:{
//				pauseCamera();
//
//				StandardConfigDialog holder = new StandardConfigDialog(getResources(), opt);
//
//				StandardConfigDialog.buildStandardConfigDialog(this, holder, null, R.string.qr_settings);
//
//				holder.init_qr_configs(this);
//
//				holder.dlg.setOnDismissListener(dialog -> {
//					resumeCamera();
//					if(requestedResetHints&&FFStamp!=opt.FirstFlag()) {
//						setHints();
//						FFStamp=opt.FirstFlag();
//						requestedResetHints=false;
//					}
//				});
//
//				holder.dlg.show();
//			} break;
		}
	}
	
	boolean lightOn = false;
	
	public boolean getTorchLight() {
		return lightOn;
	}
	
	private void decode() {
		Message message = getHandler().ready().obtainMessage(R.id.decode, bitmap);
		message.arg1=R.id.decode2;
		message.sendToTarget();
	}
	
	private void close_camera() {
		pauseCamera();
		cameraManager.close();
		suspensed=true;
		suspenseCameraUI();
	}
	
	public void suspenseCameraUI() {
		UIData.frameView.suspense();
		refreshUI();
		syncQRFrameSettings(true);
		//UIData.tv1.setVisibility(View.VISIBLE);
//		((ViewGroup.MarginLayoutParams)UIData.tv1.getLayoutParams())
//				.setMargins(0, (int) (qr_frame.scanLineTop-dm.density*45), 0, 0);
	}
	
	private void syncQRFrameSettings(boolean inval) {
		UIData.frameView.drawLaser = !suspensed && lastType == 1 && lastRealTime;
		UIData.frameView.drawLocations = lastRealTime;
		if(inval) {
			if(suspensed) {
				UIData.frameView.invalidate();
			} else {
				UIData.frameView.resume();
			}
		}
	}
	
	public boolean checkPermission(Activity activity, int requestCode) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid()) != PackageManager.PERMISSION_GRANTED) {
			activity.requestPermissions(permissions, requestCode);
			return false;
		}
		return true;
	}
	
	/** try with camera permission.
	 * @param decodeType  see {@link DecodeManager#decodeType} */
	public void tryOpenCamera(int decodeType, boolean realTime, Activity activity, int requestCode) {
		CMN.debug("tryOpenCamera", decodeType, realTime, activity, requestCode);
		lastType = decodeType;
		dMan.decodeType = decodeType & 0xF;
		this.permissionCode = requestCode;
		cameraManager.realtime = lastRealTime = realTime;
		UIData.frameView.textRects = null;
		if(!viewingImg || realTime) {
			if(activity==null) activity=this.activity;
			if(activity==null) {
				openCamera();
			} else {
				if(checkPermission(activity, requestCode)) {
					openCamera();
				}
			}
			if(realTime) {
				try {
					startPreview(null);
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
			if(viewingImg) {
				UIData.imageView.setVisibility(View.GONE);
				viewingImg = false;
			}
		}
		refreshUI();
	}
	
	public void openCamera() {
		//UIData.tv1.setVisibility(View.GONE);
		suspensed=false;
		UIData.frameView.resume();
		syncQRFrameSettings(true);
		if(handlerThread==null) {
			handlerThread = new DecodeThread(this);
			handlerThread.start();
			//handler = new QRActivityHandler(this, cameraManager);
			// The prefs can't change while the thread is running, so pick them up once here.
			//dMan.tess.resetZxingArgs();
			if (UIData.previewView.isAvailable()) {
				startPreview(null);
			} else {
				UIData.previewView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
					@Override
					public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
						startPreview(surface);
					}
					@Override
					public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
					}
					@Override
					public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
						CMN.debug("onSurfaceTextureDestroyed");
						return false;
					}
					@Override
					public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
					}
				});
			}
		}
		else {
			resumeCamera();
		}
	}
	
	public QRActivityHandler getHandler() {
		if(handlerThread==null) {
			handlerThread = new DecodeThread(this);
			handlerThread.start();
		}
		while (handler==null) {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) { }
		}
		return handler;
	};
	
	
	public void resumeCamera() {
		if(!suspensed && UIData.previewView.isAvailable()) {
			if(cameraManager!=null && cameraManager.isOpen()) {
				try {
					cameraManager.camera.setPreviewTexture(UIData.previewView.getSurfaceTexture());
				} catch (Exception e) {
					CMN.Log("re-opening::");
					cameraManager.close();
				}
			}
			startPreview(null);
			UIData.frameView.drawLocations = isRealTime();
			UIData.frameView.resume();
		}
	}
	
	public void pauseCamera() {
		//if(surfaceView!=null)
		{
			if (cameraManager != null) {
				cameraManager.pause();
			}
			if(handler!=null) {
				handler.pause();
			}
			UIData.frameView.pause();
		}
	}
	
	
	String PostResultDisplay;
	public static ObjectAnimator tada(View view) {
		return tada(view, 1f);  }
	
	public static ObjectAnimator tada(View view, float shakeFactor) {
		PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
				Keyframe.ofFloat(0f, 1f),
				Keyframe.ofFloat(.1f, .9f),
				Keyframe.ofFloat(.2f, .9f),
				Keyframe.ofFloat(.3f, 1.1f),
				Keyframe.ofFloat(.4f, 1.1f),
				Keyframe.ofFloat(.5f, 1.1f),
				Keyframe.ofFloat(.6f, 1.1f),
				Keyframe.ofFloat(1f, 1f)
		);
		PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
				Keyframe.ofFloat(0f, 1f),
				Keyframe.ofFloat(.1f, .9f),
				Keyframe.ofFloat(.2f, .9f),
				Keyframe.ofFloat(.3f, 1.1f),
				Keyframe.ofFloat(.4f, 1.1f),
				Keyframe.ofFloat(.5f, 1.1f),
				Keyframe.ofFloat(.6f, 1.1f),
				Keyframe.ofFloat(1f, 1f)
		);
		PropertyValuesHolder pvhRotate = PropertyValuesHolder.ofKeyframe(View.ROTATION,
				Keyframe.ofFloat(0f, 0f),
				Keyframe.ofFloat(.1f, -3f * shakeFactor),
				Keyframe.ofFloat(.2f, -3f * shakeFactor),
				Keyframe.ofFloat(.3f, 3f * shakeFactor),
				Keyframe.ofFloat(.4f, -3f * shakeFactor),
				Keyframe.ofFloat(.5f, 3f * shakeFactor),
				Keyframe.ofFloat(.6f, -3f * shakeFactor),
				Keyframe.ofFloat(1f, 0)
		);
		return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhRotate).
				setDuration(900);  }
	
	private ObjectAnimator tada;
	Runnable PostDisplayResult = () -> {
		//showT(PostResultDisplay);
		UIData.toastTv.setTextSize(PostResultDisplay.length()>25?14:17);
		UIData.toastTv.setText(PostResultDisplay);
		if(tada==null) {
			tada=tada(UIData.cameraBtn);
		}
		//tada.pause();
		//tada.setCurrentPlayTime(0);
		if(!tada.isRunning()) {
			tada.setCurrentPlayTime(0);
			tada.start();
		}
	};
	
	public void startPreview(SurfaceTexture surface) {
		if(!cameraManager.isOpen()) {
			try {
				cameraManager.open();
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		CMN.debug("startPreview::相机已经打开！", UIData.previewView.getSurfaceTexture());
		if(cameraManager.isOpen()) {
			try {
				cameraManager.startPreview(UIData.previewView.getSurfaceTexture());
				if(handler != null) {
					handler.ready();
					cameraManager.requestPreviewFrame();
				}
				cameraManager.resumeSensor();
			} catch (Exception e) {
				CMN.debug("预览失败！", UIData.previewView.getSurfaceTexture(), e);
				//failed = true;
				if (surface != null) { // 我说开启就开启
					pauseCamera();
					mWordCamera.settingsLayout.postDelayed(() -> resumeCamera(), 100);
				}
			}
			//qr_frame.setBitmap(null);
		}
	}
	
	/** 处理画面拉伸，从我的视频播放器项目复制过来的。 */
	private final int Match_Width=0;
	private final int Match_Height=1;
	private final int Match_Auto=2;
	private final int Match_None=3;
	
	int pendingMatchType = -1;
	int preferedMatchType = -1;
	
	boolean mVideoWidthReq;
	
	public int sWidth;
	public int sHeight;
	
	/** overview mode */
	private final static boolean videoMode=false;
	
	public void fitPreview(int width, int height, boolean rotate, boolean fitCenter, boolean isPhoto) {
		CMN.Log("onNewVideoViewLayout", width, height, isPortrait);
		if(rotate && isPortrait) {
			int tmp = width;
			width = height;
			height = tmp;
		}
		if(width==-1 && height==-1){
			width=dm.widthPixels;
			height=dm.heightPixels;
		}
		if (width * height == 0) return;
		//if(mVideoWidthReq || mVideoWidth != width || mVideoHeight != height){
		//CMN.Log("----refreshSVLayout", getScreenRotation());
		//if(!suspensed)
		{
			int screenRotation = this.screenRotation;
			cameraManager.screenRotation = screenRotation;
			float scaleX=1, scaleY=1;
			if (screenRotation == Surface.ROTATION_180
				||screenRotation == Surface.ROTATION_270) {
				scaleX = scaleY = -1;
			}
			UIData.previewView.setScaleX(scaleX);
			UIData.previewView.setScaleY(scaleY);
			//CMN.Log("----refreshSVLayout", screenRotation, scaleX, scaleY);
			PhotoView view = UIData.photoView;
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
			view.setCameraMode(isPhoto?null:cameraManager.camera);
			params.gravity= Gravity.START|Gravity.TOP;
			params.height=-1;
			params.width=-1;
			dm = opt.dm;
			int w = dm.widthPixels;
			int h = dm.heightPixels;
			
			float newW = w;
			float newH = h;
			float transX = 0;
			float transY = 0;
			pendingMatchType = -1;
			int type=Match_Auto;
			type=Match_Width;
			switch(type){
				case Match_Auto:
					pendingMatchType=3;
				case Match_Width:
					//CMN.Log("Match_Width");
					OUT: {
						params.width = w;
						newH = 1.f*w*height/width;
						params.height = (int) newH;
						if(newH<=h) {
							if(!videoMode&&pendingMatchType==3) break OUT;
						} else {
							if(videoMode&&pendingMatchType==3) break OUT;
						}
						if(fitCenter)
							transY = -(newH - h) / 2;
						pendingMatchType=Match_Width;
						break;
					}
				case Match_Height:
					//CMN.Log("Match_Height");
					newH = h; transY = 0;
					params.height = h;
					newW = 1.f * newH * width / height;
					params.width = (int) newW;
					if(fitCenter)
						transX = -(newW - w) / 2;
					pendingMatchType=Match_Height;
					break;
				case Match_None:
					break;
			}
			view.setLayoutParams(params);
			view.setTranslationX(transX);
			view.setTranslationY(transY);
			view.setScaleX(1);
			view.setScaleY(1);
			//vTranslate.set(pendingTransX, pendingTransY);
			sWidth=width;
			sHeight=height;
			dMan.fitScale=params.width*1.0f/width;
		}
		mVideoWidthReq=false;
		//}
	}
	
	void applyPreviewSize() {
		if(cameraManager.parameters!=null) {
			Camera.Size previewSize = cameraManager.parameters.getPreviewSize();
			fitPreview(previewSize.width, previewSize.height, true, true, false);
			UIData.imageView.setVisibility(View.GONE);
		}
	}
	
	void applyImageSize(boolean fitCenter) {
		if(bitmap!=null) {
			fitPreview(bitmap.getWidth(), bitmap.getHeight(), false, fitCenter, true);
			UIData.imageView.setVisibility(View.VISIBLE);
		}
	}
	
	public void onDecodeSuccess(String text) {
//		Intent intent = new Intent();
//		intent.putExtra(Intent.EXTRA_TEXT, text);
//		//CMN.Log("sendText", CMN.id(text), text);
//		setResult(RESULT_OK, intent);
		if(opt.getOneShotAndReturn()) {
//			finish();
		} else {
			PostResultDisplay = text;
			root.removeCallbacks(PostDisplayResult);
			root.post(PostDisplayResult);
		}
	}
	
	public void onDestroy() {
		//if(surfaceView!=null)
		{
			if(handler!=null)
				handler.stop();
			try {
				cameraManager.destroy();
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
	}
	
	Bitmap bitmap;
	RectF imgTranslate;
	
	public void openImage(Uri data) {
		Bitmap bitmap = null;
		try {
			suspensed = true;
			pauseCamera();
			bitmap = decodeBitmap(activity, data, -1, -1);
			//bitmap = decodeBitmap(context, data, DEFAULT_REQ_WIDTH, DEFAULT_REQ_HEIGHT);
		} catch (Exception e) {
			//CMN.Log(e);
		}
		if(bitmap!=null) {
			setImage(bitmap, false);
		}
	}
	
	private void setImage(Bitmap bitmap, boolean center) {
		pauseCamera();
		this.bitmap = bitmap;
		UIData.imageView.setImageBitmap(bitmap);
		applyImageSize(center);
		//UIData.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		UIData.frameView.setOutsideTouchMode(1);
		UIData.frameView.drawLocations = false;
		UIData.frameView.textRects = null;
		setRect(getFramingRect(true));
		UIData.toast.setVisibility(View.VISIBLE);
//		UIData.cameraBtn.setVisibility(View.GONE);
//		UIData.playBtn.setVisibility(View.VISIBLE);
		viewingImg = true;
	}
	
	public static final int DEFAULT_REQ_WIDTH = 450;
	public static final int DEFAULT_REQ_HEIGHT = 800;
	
	private static Bitmap decodeBitmap(Context context, Uri input, int reqWidth, int reqHeight) throws IOException {
		//from zxing-lite
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		ContentResolver res = context.getContentResolver();
		InputStream is;
		BitmapFactory.decodeStream(is=res.openInputStream(input), null, newOpts);
		is.close();
		float width = newOpts.outWidth;
		float height = newOpts.outHeight;
		int wSize = 1;
		if (reqWidth>0 && width > reqWidth) {
			wSize = (int) (width / reqWidth);
		}
		int hSize = 1;
		if (reqHeight>0 && height > reqHeight) {
			hSize = (int) (height / reqHeight);
		}
		int size = Math.max(wSize,hSize);
		if (size <= 0)
			size = 1;
		newOpts.inSampleSize = size;
		newOpts.inJustDecodeBounds = false;
		Bitmap ret = BitmapFactory.decodeStream(is=res.openInputStream(input), null, newOpts);
		is.close();
		return ret;
	}
	
	public boolean toggleRealTime() {
		if (cameraManager != null) {
			boolean val = cameraManager.realtime = !cameraManager.realtime;
			//UIData.frameView.possibleResultPoints = null;
			if (cameraManager.isPreviewing()) {
				if (val) {
					startPreview(null);
				}
				UIData.frameView.drawLocations = val;
				UIData.frameView.textRects = null;
				UIData.frameView.postInvalidate();
			}
			return val;
		}
		return false;
	}
	
	public boolean toggleAutoSch() {
		return autoSch = !autoSch;
	}
	
	public final boolean isRealTime() {
		return cameraManager!=null && cameraManager.realtime;
	}
	
	public interface OnSetViewRect{
		boolean onSetViewRect(RectF rect);
	}
	
	OnSetViewRect mOnSetViewRect;
	public WordCamera mWordCamera;
	
	public void setWordCamera(WordCamera wordCamera) {
		this.mOnSetViewRect = wordCamera;
		this.mWordCamera = wordCamera;
	}
	
	/** Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus. 计算这个条形码的扫描框；便于声明的同时，也强制用户通过改变距离来扫描到整个条形码
	 *
	 * @return The rectangle to draw on screen in window coordinates. */
	public synchronized RectF getFramingRect(boolean recalculate) {
		if (recalculate && (mOnSetViewRect==null || !mOnSetViewRect.onSetViewRect(framingRect))) {
			int width = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.7);
			int height = (int) (Math.min(dm.widthPixels, dm.heightPixels)*0.7);
			int leftOffset = (dm.widthPixels - width) / 2;
			int topOffset = (dm.heightPixels - height) / ((isPortrait)?3:2);
			framingRect.set(leftOffset, topOffset, leftOffset + width, topOffset + height);
			CMN.debug("Calculated framing rect: " + framingRect);
		}
		return framingRect;
	}
	
	public void setRect(RectF rect) {
		framingRect = rect;
		dMan.framingRect = framingRect;
	}
	
	/** Allows third party apps to specify the scanning rectangle dimensions,
	 * rather than determine them automatically based on screen resolution. */
	public synchronized void applyManualFramingRect(int width, int height) {
		if (width > dm.widthPixels) {
			width = dm.widthPixels;
		}
		if (height > dm.heightPixels) {
			height = dm.heightPixels;
		}
		int leftOffset = (dm.widthPixels - width) / 2;
		int topOffset = (dm.heightPixels - height) / 2;
		framingRect.set(leftOffset, topOffset, leftOffset + width, topOffset + height);
		//CMN.Log("Calculated manual framing rect: " + framingRect);
	}
	
	public void stopTessRealTimeDecoding() throws Exception {
		//CMN.debug("中断解析……");
		dMan.tess.stop();
		//CMN.debug("中断解析成功");
	}
	
	public void setRequestCode(int permissionCode, int openCode) {
		this.permissionCode = permissionCode;
		this.openCode = openCode;
	}
	
	public void dispose() {
		pauseCamera();
		try {
			cameraManager.pauseSensor();
			cameraManager.camera.stopPreview();
		} catch (Exception e) {
			CMN.debug(e);
		}
		try {
			cameraManager.camera.release();
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
}


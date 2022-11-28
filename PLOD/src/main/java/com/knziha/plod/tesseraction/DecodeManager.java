package com.knziha.plod.tesseraction;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DecodeManager {
	public RectF framingRect;
	public float fitScale;
	public boolean isPortrait=true;
	public int screenRotation;
	public boolean flipX;
	public boolean flipY;
	Tesseraction tess = new Tesseraction();
	final Manager mManager;
	
	/** 0=ocr; 1=qr; */
	int decodeType;
	private int failInc;
	
	public DecodeManager(Manager manager) {
		mManager = manager;
	}
	
	public String decodeOCR(byte[] data, int sWidth, int sHeight) throws Exception {
		RectF rect = framingRect;
		// 源图像显示后的缩放比，比如0.5、0.9
		FrameLayout view = mManager.UIData.photoView;
		float scale = fitScale * 1;//view.getScaleX();
		int left=(int)((rect.left - view.getTranslationX())/scale);
		int top=(int)((rect.top - view.getTranslationY())/scale);
		
		int widthwidth=(int)(rect.width()/scale);
		int heightheight=(int)(rect.height()/scale);
//		flipX = true;
//		flipY = true;
		
		if(flipX) { // x轴是经过翻转的
			left = sWidth - left - widthwidth;
		}
		if(flipY) { // y轴是经过翻转的
			top = sHeight - top - heightheight;
		}
		boolean rotated = isPortrait;
		boolean rotate = false;
		if(rotated) { //竖屏模式下，图像的数据其实还是横屏的老样子。所以交换一下宽高。
			int tmp=sWidth;
			sWidth = sHeight;
			sHeight = tmp;
			
			tmp = widthwidth;
			widthwidth = heightheight;
			heightheight = tmp;
			
			tmp=left;
			left=top;
			top=sHeight-tmp-heightheight;
		}
		
		if(left<0) left=0; if(top<0) top=0;
		if(widthwidth+left>=sWidth) widthwidth=sWidth-1-left;
		if(heightheight+top>=sHeight) heightheight=sHeight-1-top;
		
		if(widthwidth<=0||heightheight<=0) {
			return null;
		}
		int size=heightheight*widthwidth;
		byte[] rotatedData = acquireTmpData(size);
		if (screenRotation == Surface.ROTATION_90) {
			for(int i=0; i<widthwidth; i++ )
			{
				for(int j=0; j<heightheight; j++ )
				{
					rotatedData[i+widthwidth*j] = data[(i+left)+sWidth*(j+top)];
				}
			}
			setImage(rotatedData, widthwidth, heightheight, 1, widthwidth);
		}
		else if (screenRotation == Surface.ROTATION_270) {
			for(int i=0; i<widthwidth; i++ )
			{
				for(int j=0; j<heightheight; j++ )
				{
					//rotatedData[i+widthwidth*j] = data[widthwidth-i+widthwidth*(heightheight-j)];
					rotatedData[i+widthwidth*j] = data[(widthwidth-i+left)+sWidth*(heightheight-j+top)];
				}
			}
			setImage(rotatedData, widthwidth, heightheight, 1, widthwidth);
		}
		else if (screenRotation == Surface.ROTATION_0) {
			// 逆时针旋转90°（顺时针270°）
			for(int i=0; i<heightheight; i++ )
			{
				for(int j=0; j<widthwidth; j++ )
				{
					rotatedData[i+heightheight*j] = data[(j+left)+sWidth*(heightheight-1-i+top)];
				}
			}
			rotate=true;
		}
		else if (screenRotation == Surface.ROTATION_180) {
			// 逆时针旋转90°（顺时针270°）
			for(int i=0; i<heightheight; i++ )
			{
				for(int j=0; j<widthwidth; j++ )
				{
					//rotatedData[i+widthwidth*j] = data[widthwidth-i+widthwidth*(heightheight-j)];
					rotatedData[i+heightheight*j] = data[(heightheight-j+left)+sWidth*(heightheight-1-widthwidth+i+top)];
				}
			}
			rotate=true;
		}
		if(rotate) {
			int tmp = widthwidth;
			widthwidth = heightheight;
			heightheight = tmp;
		}
		
		setImage(rotatedData, widthwidth, heightheight, 1, widthwidth);
		
		try {
			ArrayList<Rect> word_rects = tess.getWordRects();
			if(word_rects!=null && word_rects.size()>0) {
				Manager m = mManager;
				m.UIData.frameView.textRects = word_rects;
//				m.UIData.qrFrame.postInvalidate();
				int cX=widthwidth/2, cY=heightheight/2, dist=Integer.MAX_VALUE, boxIdx=-1, boxDist;
				boolean aggressive=false;
				Rect rc;
				for (int i = 0; i < word_rects.size(); i++) {
					rc = word_rects.get(i);
					if(rc.contains(cX, cY)) {
						boxIdx = i;
						//m.UIData.qrFrame.possibleTextRects = Collections.singletonList(rc);
						break;
					}
					if(aggressive) {
						int d = distSQ(rc.centerX()-cX, rc.centerY()-cY);
						if(d<dist) {
							dist = d;
							boxIdx = i;
						}
					}
				}
				
				if(boxIdx<0)
					return null;
				rc=word_rects.get(boxIdx);
//				CMN.Log("decoding_ocr_rect::", rc.left, rc.top, rc.width()+"/"+widthwidth, rc.height()+"/"+heightheight);
				if(rc.width() * rc.height() >= 500*500) {
					return null;
				}

//				Pix pix = words.getPix(boxIdx);
//				CMN.Log("decoding_ocr_rect::", pix.getData().length);
//				handler.setImage(pix);
//				pix.recycle();
				
				final int pad = 20;
				int wordWidth = rc.width()+pad*2;
				int wordHeight = rc.height()+pad*2;
				left = rc.left-pad;
				top = rc.top-pad;
				if(left<0) left=0; if(top<0) top=0;
				if(left+wordWidth>=widthwidth) wordWidth=widthwidth-left-1;
				if(top+wordHeight>=heightheight) wordHeight=heightheight-top-1;
//				byte[] wordData = new byte[wordWidth*wordHeight];
//				for(int i=0; i<wordWidth; i++ )
//				{
//					for(int j=0; j<wordHeight; j++ )
//					{
//						wordData[i+wordWidth*j] = rotatedData[(left+i)+widthwidth*(top+j)];
//					}
//				}
//				setImage(wordData, wordWidth, wordHeight, 1, wordWidth);
				if(wordWidth>0 && wordHeight>0) {
					tess.setRectangle(left, top, wordWidth, wordHeight);
					String hoc = tess.getHOCRText(0);
					String utf8 = tess.getUTF8Text();
					if (!TextUtils.isEmpty(utf8)) {
						return utf8;
					}
				}
			}
			//Pix.createFromPix(wordData, wordWidth, wordHeight)
		} catch (Exception e) {
			CMN.Log(e);
		}
		return null;
	}
	
	static class LastTessImage{
		//Bitmap bm;
		long id;
		Rect rect=new Rect();
		ArrayList<Rect> words;
		ArrayList<Rect> words1;
	}
	
	final LastTessImage lastImg = new LastTessImage();
	
	/** Set OCR Image */
	private void setImage(byte[] data, int w, int h, int bpp, int bpl) throws Exception {
		//getTess().stop();
		getTess().setImage(data, w, h, bpp, bpl);
		if(lastImg.id!=0) {
			lastImg.id=0;
			lastImg.words=null;
			lastImg.words1=null;
		}
	}
	
	/** Set OCR Image */
	private void setImage(Bitmap bitmap) throws Exception {
		if(lastImg.id!=CMN.id(bitmap)) {
			getTess().setImage(bitmap);
			lastImg.id=CMN.id(bitmap);
			lastImg.words=null;
			lastImg.words1=null;
		}
	}
	
	private Tesseraction getTess() throws Exception {
		if(!tess.inited) {
			Manager m = mManager;
			CMN.rt();
			tess.init(m.context);
			tess.initTessdata(null, "chi_sim+eng"); //chi_sim +chi_sim
			CMN.pt("初始化1::");CMN.rt();
//					tess = new TessBaseAPI();
//					tess.init(null, "eng+chi_sim"); //chi_sim
//					CMN.pt("初始化2::");
//					tess.init(dataPath, "chi_sim"); //chi_sim
			//setImage(data, width, height, );
			//String text = tess.getUTF8Text();
		}
		return tess;
	}
	
	private int distSQ(int x, int y) {
		return x*x+y*y;
	}
	
	public void setScrOrient(int screenRotation, boolean isPortrait) {
		this.screenRotation = screenRotation;
		this.isPortrait = isPortrait;
		flipX = flipY = screenRotation==Surface.ROTATION_180||screenRotation==Surface.ROTATION_270;
	}
	
	void decodeWord() throws Exception {
		Bitmap bitmap = mManager.bitmap;
		int sWidth=bitmap.getWidth();
		int sHeight=bitmap.getHeight();
		int left, top, widthwidth, heightheight;
		FrameLayout view = mManager.UIData.photoView;
		float scale = fitScale * view.getScaleX();
		boolean crop=mManager.UIData.frameView.isCropping();
		if(crop) {
			RectF rect = framingRect;
			left=(int)((rect.left - view.getTranslationX())/scale);
			top=(int)((rect.top - view.getTranslationY())/scale);
			widthwidth=(int)(rect.width()/scale);
			heightheight=(int)(rect.height()/scale);
			if(left<0) left=0; if(top<0) top=0;
			if(widthwidth+left>=sWidth) widthwidth=sWidth-1-left;
			if(heightheight+top>=sHeight) heightheight=sHeight-1-top;
		} else {
			left = top = 0;
			widthwidth = sWidth-1;
			heightheight = sHeight-1;
		}
		if(widthwidth<=0||heightheight<=0) {
			return;
		}
		
		//tess.stop();
		setImage(bitmap);
		ArrayList<Rect> rects = crop?lastImg.words:lastImg.words1;
		if(rects!=null && crop && !lastImg.rect.contains(left, top, widthwidth, heightheight)) {
			lastImg.words=null;
		}
		if(rects==null) {
			tess.setRectangle(left, top, widthwidth, heightheight);
			rects = tess.getWordRects();
			if(crop) {
				lastImg.words=rects;
				lastImg.rect.set(left, top, widthwidth, heightheight);
			} else {
				lastImg.words1=rects;
			}
		}
		
		int cX=(int) ((mManager.UIData.frameView.lastX-view.getTranslationX())/scale)-left
				, cY=(int)((mManager.UIData.frameView.lastY-view.getTranslationY())/scale)-top
				, boxIdx=-1;
		Rect rc;
		for (int i = 0; i < rects.size(); i++) {
			rc = rects.get(i);
			if(rc.contains(cX, cY) && rc.width()*rc.height()<500*500) {
				boxIdx = i;
				//break;
			}
		}
		if (boxIdx>=0) {
			rc=rects.get(boxIdx);
			mManager.UIData.frameView.setTextRect(rc);
			final int pad = 20;
			int wordWidth = rc.width()+pad*2;
			int wordHeight = rc.height()+pad*2;
			int wordLeft = rc.left-pad;
			int wordTop = rc.top-pad;
			if(wordLeft<0) wordLeft=0; if(wordTop<0) wordTop=0;
			if(wordLeft+wordWidth>=widthwidth) wordWidth=widthwidth-wordLeft-1;
			if(wordTop+wordHeight>=heightheight) wordHeight=heightheight-wordTop-1;
			if(wordWidth>0 && wordHeight>0) {
				tess.setRectangle(wordLeft+left, wordTop+top, wordWidth, wordHeight);
				//tess.getHOCRText(0);
				mManager.onDecodeSuccess(tess.getUTF8Text());
			}
		} else {
			mManager.UIData.frameView.setTextRect(null);
		}
	}
	
	private void decodeBitmap(Bitmap bitmap) throws Exception {
		int sWidth=bitmap.getWidth();
		int sHeight=bitmap.getHeight();
		int left, top, widthwidth, heightheight;
		if(mManager.UIData.frameView.isCropping()) {
			RectF rect = framingRect;
			FrameLayout view = mManager.UIData.photoView;
			float scale = fitScale * view.getScaleX();
			left=(int)((rect.left - view.getTranslationX())/scale);
			top=(int)((rect.top - view.getTranslationY())/scale);
			widthwidth=(int)(rect.width()/scale);
			heightheight=(int)(rect.height()/scale);
			if(left<0) left=0; if(top<0) top=0;
			if(widthwidth+left>=sWidth) widthwidth=sWidth-1-left;
			if(heightheight+top>=sHeight) heightheight=sHeight-1-top;
		} else {
			left = top = 0;
			widthwidth = sWidth-1;
			heightheight = sHeight-1;
		}
		CMN.Log("decodeBitmap::", widthwidth, heightheight);
		if(widthwidth<=0||heightheight<=0) {
			return;
		}
		String res = null;
		CMN.Log("decodeBitmap::", decodeType, sWidth, sHeight, "rc=", left, top, widthwidth, heightheight);
		
		if(decodeType==0) {
			CMN.rt();
			setImage(bitmap);
			CMN.pt("setImage::"); CMN.rt();
			ArrayList<Rect> wds = tess.getWordRects();
			CMN.pt("取得words::", wds.size());
			tess.setRectangle(left, top, widthwidth, heightheight);
			res = tess.getUTF8Text();
			CMN.pt("识别::", wds.size());
		}
		else if(decodeType==1) {
			try {
				res = tess.decodeQrBitmap(bitmap);
				//CMN.Log(bitmap);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		if (res != null) { // Don't log the barcode contents for security.
			mManager.onDecodeSuccess(res);
			//CMN.Log("fatal poison", "Found_barcode_in " + (end - start) + " ms");
		}
		mManager.cameraManager.requestPreviewFrame(); // fast restart
	}
	
	/**Build and decode the appropriate LuminanceSource object.
	 * @param data A preview frame.
	 * @param sWidth Source Width
	 * @param sHeight Source Height
	 * //@param rotated In portrait mode, Camera Display Is rotated, but data remains the same.
	 * //               	Just swap the dimensions to get real image size and framing rect.
	 * @param rotate Whether to try again with rotated data for orientation-sensitive bar-codes such as ISBN.
	 *   二维码不必旋转、翻转
	 * */
	public String decodeQR(byte[] data, int sWidth, int sHeight, boolean rotate, boolean invert) throws Exception {
		RectF rect = framingRect;
		// 源图像显示后的缩放比，比如0.5、0.9
		FrameLayout view = mManager.UIData.photoView;
		float scale = fitScale * 1;//view.getScaleX();
		int left=(int)((rect.left - view.getTranslationX())/scale);
		int top=(int)((rect.top - view.getTranslationY())/scale);
		//CMN.Log("getScaleX::", view.getScaleX(), view.getScaleY());
		
		int widthwidth=(int)(rect.width()/scale);
		int heightheight=(int)(rect.height()/scale);
//		left=top=0;
//		widthwidth=sWidth;
//		heightheight=sHeight;

//		flipX = true;
//		flipY = true;
		if(flipX) { // x轴是经过翻转的
			left = sWidth - left - widthwidth;
		}
		if(flipY) { // y轴是经过翻转的
			top = sHeight - top - heightheight;
		}
		boolean rotated = isPortrait;
		return tess.decodeQrData(data, sWidth, sHeight, left, top, widthwidth, heightheight, rotate, invert, rotated);
	}
	
	/** Decode the data within the viewfinder rectangle, and time how long it
	 * took. For efficiency, reuse the same reader object.
	 * @param data The YUV preview frame.  */
	private void decode(byte[] data) throws Exception {
		Manager m = mManager;
		int width=m.sWidth;
		int height=m.sHeight;
		CMN.rt();
		String rawResult = null;
		//CMN.Log("decode data……", decodeType, CMN.id(data));
		if(decodeType==0) {
			// 测试OCR插件！！！
			try {
				rawResult = decodeOCR(data, width, height);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		else if(decodeType==1) {
			try {
				rawResult = decodeQR(data, width, height, false, false); //不旋转
			} catch (Exception e) {// continue
				CMN.Log(e);
			}
			if(rawResult==null && true) { // getTryAgainWithInverted
				try {
					rawResult = decodeQR(data, width, height, false, true); //反色
				} catch (Exception e) {// continue
					//CMN.Log(e);
				}
			}
			if(rawResult==null && true) { //for isbn getTryAgainWithRotatedData
				failInc++;
				if(false||failInc>=5) { // getTryAgainImmediately
					try {
						rawResult = decodeQR(data, width, height, true, false); //不旋转;
					} catch (Exception e) {
						//CMN.Log(e);
					}
					failInc=0;
				}
			}
			tess.resetQrDecoder();
		}
		if (rawResult != null) { // Don't log the barcode contents for security.
			m.onDecodeSuccess(rawResult);
			//CMN.Log("fatal poison", "Found_barcode_in " + (end - start) + " ms");
		}
		m.cameraManager.requestPreviewFrame(); // fast restart
	}
	
	
	/** 此Handler位于异步线程，这样不会卡UI */
	static class QRActivityHandler extends Handler {
		public final WeakReference<Manager> manager;
		private boolean running = true;
		//构造
		public QRActivityHandler(Manager manager) {
			//super(Looper.myLooper());
			manager.handler=this;
			this.manager = new WeakReference<>(manager);
		}
		
		@Override
		public void handleMessage(@NonNull Message message) {
			if (running && message.what==R.id.decode) {
				try {
					decode(message);
				} catch (Exception e) {
					CMN.debug(e);
				}
			} else if(message.what==R.id.quit) {
				Looper.myLooper().quit();
			}
		}
		
		private void decode(Message msg) throws Exception {
			Manager m = manager.get();
			if(m==null) {
				stop();
				return;
			}
			DecodeManager d = m.dMan;
			if(msg.arg1==R.id.decode2) {
				d.decodeBitmap((Bitmap) msg.obj);
			} else if(msg.arg1==R.id.decode3){
				d.decodeWord();
			} else {
				d.decode((byte[]) msg.obj);
			}
		}
		
		public void stop() {
			pause();
			removeMessages(R.id.decode);
			sendEmptyMessage(R.id.quit);
		}
		
		public synchronized void pause() {
			running=false;
		}
		
		public synchronized QRActivityHandler ready() {
			running=true;
			return this;
		}
	}
	
	/**酱油线程*/
	static class DecodeThread extends Thread {
		WeakReference<Manager> manager;
		public DecodeThread(Manager manager) {
			this.manager = new WeakReference<>(manager);
		}
		@Override
		public void run() {
			Looper.prepare();
			new QRActivityHandler(manager.get());
			manager=null;
			//CMN.Log("thread_run……");
			Looper.loop();
			//CMN.Log("thread_run_ended……");
		}
	}
	
	WeakReference<byte[]> TmpData = new WeakReference<>(null);
	/**复用临时数据*/
	public byte[] acquireTmpData(int size) {
		byte[] ret = TmpData.get();
		if(ret==null||ret.length<size)
		{
			TmpData.clear();
			TmpData = new WeakReference<>(ret=new byte[size]);
		}
		//else CMN.Log("reusing……", ret.length, size);
		return ret;
	}
}

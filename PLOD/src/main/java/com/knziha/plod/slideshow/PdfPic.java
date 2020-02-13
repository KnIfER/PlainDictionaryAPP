package com.knziha.plod.slideshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

import com.knziha.plod.dictionary.Utils.IU;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class PdfPic {
	final String path;
	int Page;
	PdfiumCore pdfiumCore;
	PdfDocument pdf;
	public final static int MaxBitmapRam=45*1024*1024;

	public PdfPic(String key, Object...Tags) {
		if(key.startsWith("/pdfimg/")){
			key=key.substring("/pdfimg/".length());
			int idx = key.lastIndexOf("#");
			if(idx>0){
				Page = IU.parsint(key.substring(idx+1));
				key=key.substring(0, idx);
				if(Tags.length==2){
					pdfiumCore = ((PdfiumCore) Tags[0]);
					pdf = ((HashMap<String, PdfDocument>) Tags[1]).get(key);
				}else{
					pdfiumCore = new PdfiumCore((Context) Tags[0]);
					try {
						pdf = pdfiumCore.newDocument(ParcelFileDescriptor.open(new File(key), ParcelFileDescriptor.MODE_READ_ONLY));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		this.path = key;
	}

	public Bitmap createBitMap() {
		if(pdfiumCore !=null && pdf !=null){
			pdfiumCore.openPage(pdf, Page);
			float shrinkage=1f;
			int width = pdfiumCore.getPageWidth(pdf, Page);
			int height = pdfiumCore.getPageHeight(pdf, Page);
			float mBitmapRam = (width * height * 2);
			if(mBitmapRam>MaxBitmapRam){
				shrinkage = MaxBitmapRam/mBitmapRam;
			}
			width*=shrinkage;
			height*=shrinkage;
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			//CMN.rt();
			pdfiumCore.renderPageBitmap(pdf, bitmap, Page, 0, 0, width, height);
			//CMN.pt("解码耗时 : "); CMN.rt();
			//CMN.Log(bitmap.getByteCount(), width, height, bitmap.getByteCount()/(width*height));
			return bitmap;
		}
		return null;
	}
}
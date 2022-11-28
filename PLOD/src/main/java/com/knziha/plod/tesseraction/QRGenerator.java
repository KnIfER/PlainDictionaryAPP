package com.knziha.plod.tesseraction;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class QRGenerator extends Activity {
	private View root;
	private ImageView qr_frame;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_qr1);
//		qr_frame = findViewById(R.id.qr_frame);
//		root = findViewById(R.id.root);
//		root.setOnClickListener(v -> finish());
//		Intent intent = getIntent();
//		String text = intent==null?null:intent.getStringExtra(Intent.EXTRA_TEXT);
//		if(TextUtils.isEmpty(text)) text = "QRCode";
//		try {
//			Bitmap bm = generateQRCode(text);
//			qr_frame.setImageBitmap(bm);
//		} catch (Exception e) {
//			CMN.Log(e);
//		}
	}
	
}

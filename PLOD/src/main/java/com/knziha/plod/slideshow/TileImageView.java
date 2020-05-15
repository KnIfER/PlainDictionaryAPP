package com.knziha.plod.slideshow;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class TileImageView extends ImageView {
	public TileImageView(Context context) {
		super(context);
	}
	
	public TileImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TileImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public int sx;
	public int sy;
}

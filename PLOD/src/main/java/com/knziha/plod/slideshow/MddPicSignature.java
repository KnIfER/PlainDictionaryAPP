package com.knziha.plod.slideshow;

import com.bumptech.glide.load.Key;

import java.io.File;
import java.security.MessageDigest;

public class MddPicSignature implements Key {
	private final String file;

	public MddPicSignature(String path) {
		this.file = path;
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest) {
		byte[] bs = file.getBytes();
		messageDigest.update(bs, 0, bs.length);
	}
}
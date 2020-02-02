package com.knziha.plod.dictionarymanager.files;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class CachedDirectory extends File{
	Boolean exists;
	public CachedDirectory(@NonNull String pathname) {
		super(pathname);
	}

	public CachedDirectory(@Nullable String parent, @NonNull String child) {
		super(parent, child);
	}

	public CachedDirectory(@Nullable File parent, @NonNull String child) {
		super(parent, child);
	}

	public CachedDirectory(@NonNull URI uri) {
		super(uri);
	}

	public boolean cachedExists() {
		if(exists==null) exists=super.exists();
		return exists;
	}

	@Override
	public boolean isDirectory() {
		return exists=super.isDirectory();
	}

	@Override
	public boolean exists() {
		return exists=super.exists();
	}

	@Override
	public boolean mkdirs() {
		return exists==super.mkdirs();
	}
}

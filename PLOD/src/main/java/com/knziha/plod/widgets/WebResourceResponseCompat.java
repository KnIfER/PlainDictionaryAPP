package com.knziha.plod.widgets;

import android.webkit.WebResourceResponse;

import java.io.InputStream;
import java.util.Map;

public class WebResourceResponseCompat extends WebResourceResponse {
	private Map<String, String> mResponseHeaders;
	public WebResourceResponseCompat(String mimeType, String encoding, InputStream data) {
		super(mimeType, encoding, data);
	}
	
	public void setResponseHeaders(Map<String, String> headers) {
		mResponseHeaders = headers;
	}
	
	public Map<String, String> getResponseHeaders() {
		return mResponseHeaders;
	}
}
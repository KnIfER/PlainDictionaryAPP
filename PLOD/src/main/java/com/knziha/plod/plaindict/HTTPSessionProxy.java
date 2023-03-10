package com.knziha.plod.plaindict;

import android.webkit.WebResourceRequest;

import org.nanohttpd.protocols.http.HTTPSession;

import java.util.HashMap;

public class HTTPSessionProxy extends HTTPSession {
	final static HashMap<String, String> hdr =  new HashMap<>();
	static {
		hdr.put("user-agent", "");
	}
	
	public final WebResourceRequest request;
	
	public HTTPSessionProxy(String uri, WebResourceRequest request) {
		int parmsIdx = uri.indexOf("?");
		if(parmsIdx>0) {
			this.uri = uri.substring(0, parmsIdx);
			this.parms = new HashMap<>();
			decodeParms(uri.substring(parmsIdx), parms);
		} else {
			this.uri = uri;
		}
		this.request = request;
		this.headers = hdr;
		this.isProxy = true;
	}
}
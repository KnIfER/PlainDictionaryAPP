package com.knziha.polymer.wget.info.ex;

public class DownloadRetry extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DownloadRetry() {
        
    }

    public DownloadRetry(Throwable e) {
        super(e);
    }

    public DownloadRetry(String msg) {
        super(msg);
    }
}

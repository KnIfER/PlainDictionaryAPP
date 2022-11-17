package com.knziha.polymer.wget;

import com.knziha.polymer.wget.info.DownloadInfo;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Direct {
	public static boolean shouldDownload=true;
	
    File target = null;

    DownloadInfo info;

    /**
     * connect socket timeout
     */
    static public final int CONNECT_TIMEOUT = 10000;

    /**
     * read socket timeout
     */
    static public final int READ_TIMEOUT = 10000;

    /**
     * size of read buffer
     */
    static public final int BUF_SIZE = 4 * 1024;

    /**
     * 
     * @param info
     *            download file information
     * @param target
     *            target file
     * @param stop
     *            multithread stop command
     * @param notify
     *            progress notify call
     */
    public Direct(DownloadInfo info, File target) {
        this.target = target;
        this.info = info;
    }

    abstract public void download(AtomicBoolean stop, Runnable notify);

}

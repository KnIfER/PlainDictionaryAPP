package com.knziha.plod.PlainUI;

import static com.knziha.plod.PlainUI.PageMenuHelper.PageMenuType.Nav_main;

import android.os.Looper;
import android.util.SparseArray;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PageMenuHelper {
	final MainActivityUIBase a;
	SparseArray<int[]> utils = new SparseArray<>();
	public boolean lnk_info_fetched = false;
	public String lnk_href;
	public PageMenuType mType;
	public WebViewmy mWebView;
	
	public PageMenuHelper(MainActivityUIBase a) {
		this.a = a;
	}
	
	public enum PageMenuType{
		Nav_main
		, Nav_WEB
		, LNK
		, LNK_IMG
		, LNK_WEB
	}
	
	int[] getPageUtils(PageMenuType type, WebViewmy mWebView) {
		int intType = type.ordinal();
		int[] ret = utils.get(intType);
		if (ret == null) {
			switch (type) {
				case Nav_main:
					ret = new int[]{
						R.layout.page_nav_util
						,R.string.bmAdd
						,R.string.page_fuzhi
						,R.string.page_dakai
						,R.layout.page_dopt_refresh
						,0
						,R.string.page_ucc
					};
				break;
				case Nav_WEB:
					ret = new int[] {
						R.layout.page_nav_util
						,R.string.bmAdd
						,R.string.page_fuzhi
						,R.string.page_dakai
						,R.string.refresh
						,R.string.page_nav
						,R.string.page_rukou
						,R.string.page_ucc
					};
				break;
				case LNK:
					ret = new int[] {
						R.string.page_lianjie
						, R.string.page_sel
						, R.layout.page_lnk_fanyi
						, R.layout.page_lnk_apply
						, R.string.page_ucc
						, R.string.page_dakai
					};
				break;
				case LNK_WEB:
					ret = new int[] {
						R.string.page_lianjie
						, R.string.page_sel
						, R.layout.page_lnk_fanyi
						, R.layout.page_lnk_apply
						, R.string.page_ucc
						, R.string.page_rukou
						, R.string.page_dakai
					};
				break;
				case LNK_IMG:
					ret = new int[] {
						 R.string.page_save_img
						//, R.string.page_sel
					};
				break;
			}
			utils.put(type.ordinal(), ret);
		}
		if(type==Nav_main) {
			ret[5] = mWebView.merge?R.string.pageOpt:0;
		}
		return ret;
	}
	
	public PopupMenuHelper showPageMenu(PageMenuType type, @NonNull WebViewmy mWebView, View v, int ox, int oy) {
		this.mType = type;
		this.mWebView = mWebView;
		PopupMenuHelper popupMenu = a.getPopupMenu();
		popupMenu.initLayout(getPageUtils(type, mWebView), a);
		int[] vLocationOnScreen = new int[2];
		v.getLocationOnScreen(vLocationOnScreen);
		int x,y;
		if (v == mWebView) {
			x=(int)mWebView.lastX;
			y=(int)mWebView.lastY;
		} else {
			x=(int)mWebView.weblistHandler.pageSlider.OrgX;
			y=(int)mWebView.weblistHandler.pageSlider.OrgY;
			v = mWebView.weblistHandler.pageSlider;
		}
		popupMenu.show(v, x+vLocationOnScreen[0], y+vLocationOnScreen[1], ox, oy);
		ViewUtils.preventDefaultTouchEvent(v, x, y);
		popupMenu.tag1 = mWebView;
		return popupMenu;
	}
	
	public void saveImage() {
		final String url = lnk_href;
		final String path = a.opt.pathToMainFolder().append("Download").toString();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL requestURL = new URL(url);
					//File pathDownload = new File("/storage/emulated/0/download");
					File pathDownload = new File(path);
					pathDownload.mkdirs();
					if(pathDownload.isDirectory()) {
						File path;
						int idx = url.indexOf("?");
						path=new File(pathDownload, new File(idx>0?url.substring(0, idx):url).getName());
						String msg;
						if(path.exists())
							msg="文件已存在！";
						else {
							String error=null;
							HttpURLConnection urlConnection = null;
							InputStream input = null;
							FileOutputStream fout = null;
							try {
								int schemaIdx = url.indexOf(":");
								boolean mdbr = url.regionMatches(schemaIdx+3, "mdbr", 0, 4) && url.length()>12;
								if (mdbr) {
									//HTTPSession req = new MdictServerMobile.HTTPSessionProxy(url.substring(schemaIdx+7+4), null);
									//Response ret = a.getMdictServer().handle(req);
									WebResourceResponse resp = a.shouldInterceptRequestCompat(mWebView, url, null, null, null, null);
									if (resp != null) {
										input = resp.getData();
									}
								}
								if (input == null) {
									try {
										// nimp
										//SSLContext sslcontext = SSLContext.getInstance("TLS");
										//sslcontext.init(null, new TrustManager[]{new bookPresenter_web.MyX509TrustManager()}, new java.security.SecureRandom());
										//HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
									} catch (Exception ignored) { }
									urlConnection = (HttpURLConnection) requestURL.openConnection();
									urlConnection.setRequestMethod("GET");
									urlConnection.setConnectTimeout(35000);
									//urlConnection.setRequestProperty("Charset", "UTF-8");
									//urlConnection.setRequestProperty("Connection", "Keep-Alive");
									urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Linux; Android 9; VTR-AL00 Build/HUAWEIVTR-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36");
									urlConnection.connect();
									
									input = urlConnection.getInputStream();
								}
								byte[] buffer = new byte[4096];
								int len;
								while ((len = input.read(buffer)) > 0) {
									if (fout == null)
										fout = new FileOutputStream(path);
									fout.write(buffer, 0, len);
								}
							} catch (Exception e) {
								CMN.debug(e);
								error = e.toString();
							} finally {
								try {
									if (fout != null)
										fout.close();
									if (urlConnection != null)
										urlConnection.disconnect();
									if (input != null)
										input.close();
								} catch (Exception e) {
									CMN.debug(e);
								}
							}
							msg = error==null?"下载完成":("发生错误："+error);
						}
						Looper.prepare();
						Toast.makeText(a , msg , Toast.LENGTH_LONG).show();
						Looper.loop();
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		}).start();
	}
}

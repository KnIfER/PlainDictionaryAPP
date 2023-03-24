package com.knziha.plod.PlainUI;

import static com.knziha.plod.PlainUI.PageMenuHelper.PageMenuType.LNK;
import static com.knziha.plod.PlainUI.PageMenuHelper.PageMenuType.LNK_IMG;
import static com.knziha.plod.PlainUI.PageMenuHelper.PageMenuType.LNK_WEB;
import static com.knziha.plod.PlainUI.PageMenuHelper.PageMenuType.Nav_main;

import android.os.Build;
import android.os.Looper;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class PageMenuHelper {
	final MainActivityUIBase a;
	SparseArray<int[]> utils = new SparseArray<>();
	public boolean lnk_info_fetched = false;
	public String lnk_href;
	public PageMenuType mType;
	public WebViewmy mWebView;
	
	private void handleLnkRepopup(WebViewmy mWebView, BookPresenter invoker, String url, boolean popup, boolean isLongClick) {
		if (url.startsWith(MainActivityUIBase.entryTag)) {
			url = url.substring(MainActivityUIBase.entryTag.length());
		} else {
			//  转换为contentUrl词条跳转 "http://mdbr.com/base/dOED/entry/词条名";
			int schemaIdx = url.indexOf(":");
			boolean mdbr = url.regionMatches(schemaIdx+3, "mdbr", 0, 4) && url.length()>schemaIdx+8;
			if(mdbr) {
				if (url.regionMatches(schemaIdx+12, "base", 0, 4)) {
					int idx = schemaIdx + 12 + 5;
					if (url.charAt(idx) == 'd') { // base/d0/entry/...
						int idxEd = url.indexOf("/", idx);
						invoker = a.getMdictServer().md_getByURLPath(url, idx, idxEd);
						url = url.substring(url.indexOf("/", idxEd+1)+1);
					}
				}
			}
		}
		String msg = a.handleEntryJump(url, mWebView, invoker, !popup && isLongClick, popup, false);
		if (msg!=null) {
			a.showT(url+"\n"+msg);
		}
	}
	
	public void handleLnkUtils(PopupMenuHelper popupMenuHelper, int id, WebViewmy mWebView, boolean isLongClick) {
		popupMenuHelper.dismiss();
		if (id==R.string.page_sel) {
			int type = mType==LNK_IMG?1:0;
			SelectHtmlObject(a, mWebView, type);
			return;
		}
		if (id==R.string.page_lnk_situ || id==R.string.page_lnk_pop) {
			String url = a.pageMenuHelper.lnk_href;
			try {
				url = URLDecoder.decode(url, "UTF-8");
			} catch (Exception ignored) { }
			if (mWebView.merge) {
				String finalUrl = url;
				mWebView.evaluateJavascript("window._touchtarget.href", value -> {
					try {
						value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
						if (value.startsWith("http")) {
							handleLnkRepopup(mWebView, mWebView.presenter, finalUrl, id==R.string.page_lnk_pop, isLongClick);
							return;
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
					handleLnkRepopup(mWebView, mWebView.presenter, finalUrl, id == R.string.page_lnk_pop, isLongClick);
				});
			} else {
				handleLnkRepopup(mWebView, mWebView.presenter, url, id == R.string.page_lnk_pop, isLongClick);
			}
			return;
		}
		mWebView.evaluateJavascript(BookPresenter.touchTargetLoader_getText, new ValueCallback<String>() {
			public void onReceiveValue(String value) {
				try {
					value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
					if (id==R.id.page_lnk_tapSch) {
						a.popupWord(value, null, isLongClick?-100:-1, null, false);
					}
					else if (id==R.id.page_lnk_sch) {
						// 查词
						a.popupWord(value, null, -1, mWebView, true);
						a.wordPopup.forceDirectSchInNewWindow = true;
					}
					else if (id==R.id.page_lnk_fye) {
						a.JumpToPeruseModeWithWord(value);
					}
					else if (id==R.string.page_fuzhi) {
						a.copyText(value, true);
					}
					else if (id==R.string.page_ucc) {
						a.getVtk().setInvoker(null, null, null, value);
						a.getVtk().onClick(null);
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		});
	}
	
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
						, R.string.page_fuzhi
						, R.layout.page_dopt_refresh
						, R.string.dict_opt
						//, R.string.page_dakai
						, R.string.page_ucc
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
				// 长按链接
				case LNK:
					ret = new int[] {
						//, R.string.page_lianjie
						R.string.page_fuzhi
						, R.string.page_sel
						, R.layout.page_lnk_fanyi
						, R.layout.page_lnk_apply
						//, R.string.close_pop
						, R.string.page_ucc
						//, R.string.page_dakai
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
			ret[4] = mWebView.merge?R.string.pageOpt:R.string.dict_opt;
		}
		return ret;
	}
	
	public PopupMenuHelper showPageMenu(PageMenuType type, @NonNull WebViewmy mWebView, View v, int ox, int oy) {
		this.mType = type;
		this.mWebView = mWebView;
		PopupMenuHelper popupMenu = a.getPopupMenu();
		popupMenu.initLayout(getPageUtils(type, mWebView), a);
		popupMenu.tag = type.ordinal();
		popupMenu.tag2 = type;
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
	
	public boolean isLnkUtils(PopupMenuHelper popupMenuHelper) {
		return popupMenuHelper.tag>=LNK.ordinal()
				&& popupMenuHelper.tag<=LNK_WEB.ordinal()
				&& popupMenuHelper.tag2 instanceof PageMenuType;
	}
	
	public static void SelectHtmlObject(MainActivityUIBase a, WebViewmy wv, int source) {
		wv.evaluateJavascript(BookPresenter.touchTargetLoader+"("+source+")", new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				int len = IU.parsint(value, 0);
				boolean fakePopHandles = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;
				if(len>0) {
					/* bring in action mode by a fake click on the programmatically  selected text. */
					if(fakePopHandles) {
						//wv.forbidLoading=true;
						//wv.getSettings().setJavaScriptEnabled(false);
						//wv.getSettings().setJavaScriptEnabled(false);
						MotionEvent te = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, wv.lastX, wv.lastY, 0);
						wv.lastSuppressLnkTm = CMN.now();
						wv.dispatchTouchEvent(te);
						te.setAction(MotionEvent.ACTION_UP);
						wv.dispatchTouchEvent(te);
						te.recycle();
						/* restore href attribute */
					}
				} else {
					a.showT("选择失败");
				}
//				if(fakePopHandles) {
//					wv.postDelayed(() -> {
//						wv.forbidLoading=false;
//						//wv.getSettings().setJavaScriptEnabled(true);
//						//wv.evaluateJavascript("restoreTouchtarget()", null);
//					}, 300);
//				} else {
//					//wv.evaluateJavascript("restoreTouchtarget()", null);
//				}
			}
		});
	}
	
}

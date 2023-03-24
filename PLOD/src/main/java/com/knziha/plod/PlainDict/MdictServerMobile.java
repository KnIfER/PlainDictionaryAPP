package com.knziha.plod.plaindict;

import static com.knziha.plod.PlainUI.HttpRequestUtil.DO_NOT_VERIFY;
import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.knziha.plod.PlainUI.FloatBtn;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.mdictRes_asset;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.imaging.BufferedImage;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.ManagedImageBufferedImageFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.xiph.speex.ByteArrayRandomOutputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Mdict Server
 * @author KnIfER
 * date 2020/06/01
 */

//@StripMethods(strip=!BuildConfig.isDebug, keys={"getRemoteServerRes"})
public class MdictServerMobile extends MdictServer {
	
	private static HashMap<String, Object>  mTifConfig;
	
	public MdictServerMobile(int port, MainActivityUIBase app) throws IOException {
		super(port, app);
		app.serverHosts=new ConcurrentHashMap<>();
		if(app.serverHostsHolder!=null) {
			for (PlainWeb pw: app.serverHostsHolder) {
				app.serverHosts.putAll(pw.jinkeSheaths);
			}
		}
		webResHandler = new PlainWeb(new File("/ASSET2/plate.web"), app);
		webResHandler.jinkeSheaths = app.serverHosts;
		try {
			MdbResource = new mdictRes_asset(new File(CMN.AssetTag, "MdbR.mdd"),2, app);
		} catch (IOException e) { SU.Log(e); }
		setOnMirrorRequestListener((uri, mirror) -> {
			if(uri==null)uri="";
			String[] arr = uri.split("&");
			HashMap<String, String> args = new HashMap<>(arr.length);
			for (int i = 0; i < arr.length; i++) {
				try {
					String[] lst = arr[i].split("=");
					args.put(lst[0], lst[1]);
				} catch (Exception ignored) { }
			}
			int pos=IU.parsint(args.get("POS"), app.adaptermy.lastClickedPos);
			int dx=IU.parsint(args.get("DX"), app.dictPicker.adapter_idx);
			String key= app.etSearch.getText().toString();
			try {
				key= URLDecoder.decode(args.get("KEY"),"UTF-8");
			}catch(Exception ignored) {}
			String records=null;
			if(!mirror)
				records=args.get("CT");
			if(records==null)
				records=record_for_mirror();
			CMN.Log("sending1..."+records);
			{
				try {
					records=URLDecoder.decode(records, "UTF-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			CMN.Log("sending2...");
			CMN.Log("sending2..."+records);
			return newFixedLengthResponse(constructDerivedHtml(key, pos, dx,records));
		});
	}
	
	protected InputStream convert_tiff_img(InputStream restmp) throws Exception {
		BufferedImage image = Imaging.getBufferedImage(restmp, getTifConfig());
		//CMN.pt("解码耗时 : "); CMN.rt();
		ByteArrayRandomOutputStream bos = new ByteArrayRandomOutputStream((int) (restmp.available()*2.5));
		image.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		return new ByteArrayInputStream(bos.bytes());
	}
	
	private String record_for_mirror() {
		return null;
	}
	
	@Override
	protected void handle_search_event(Map<String, List<String>> parameters, InputStream inputStream) {
		CMN.debug("接到了接到了", "parameters = [" + parameters + "], inputStream = [" + inputStream + "]");
		List<String> target = parameters.get("f");
		byte[] data;
		try {
			data = new byte[inputStream.available()];
			inputStream.read(data);
		} catch (IOException e) {
			return;
		}
		String text = new String(data);
		if (target != null && target.size() > 0) {
			int sharetype = IU.parsint(target.get(0));
			//CMN.Log("sharetype", sharetype);
			app.root.post(() -> {
				if (sharetype == 2) {
					//app.execVersatileShare(text, opt.getSendToShareTarget()); todo impl
				} else {
					switch (PDICMainAppOptions.getSendToAppTarget()) {
						case 0:
							app.execVersatileShare(text, 0);
							break;
						case 1: {
							app.etSearch.setText(text);
							app.etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
							if (!app.focused) {
								ActivityManager manager = (ActivityManager) app.getSystemService(Context.ACTIVITY_SERVICE);
								if (manager != null)
									manager.moveTaskToFront(app.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
							}
						}
						break;
						case 2: {
							app.JumpToFloatSearch(text);
						}
						break;
						case 3:
							app.execVersatileShare(text, 1);
						break;
					}
				}
			});
			CMN.Log("启动搜索 : ", text);
		}
		else if (parameters.get("textClip")!=null) {
			app.onReceivedText(text);
		}
	}
	
	public static HashMap<String, Object> getTifConfig() {
		if(mTifConfig==null) {
			mTifConfig = new HashMap<>(1);
			mTifConfig.put(ImagingConstants.BUFFERED_IMAGE_FACTORY, new ManagedImageBufferedImageFactory());
		}
		return mTifConfig;
	}
	
	public static String remoteDebugServer;
	
	public static InputStream getRemoteServerRes(String key, boolean check) {
		InputStream ret = null;
		if(hasRemoteDebugServer/* && PDICMainAppOptions.debug()*/) {
			try {
				return testDebugServer(check?"192.168.0.100":remoteDebugServer, key, check);
			} catch (Exception e) {
				if (check) {
					try {
						return testDebugServer("192.168.0.102", key, check);
					} catch (Exception ex) {
						CMN.debug("getRemoteServerRes failed::"+e);
						hasRemoteDebugServer = false;
					}
				}
			}
		}
		return ret;
	}
	
	private static InputStream testDebugServer(String ipAddress, String key, boolean check) throws IOException {
		if(check) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		String uri = "http://"+ipAddress+":8080/base/李白全集" + URLEncoder.encode(key.replace("\\", "/"));
		HttpURLConnection urlConnection = (HttpURLConnection) new URL(uri).openConnection();
		if(check)
			CMN.Log("OpenMdbResourceByName", uri);
		if (urlConnection instanceof HttpsURLConnection) {
			((HttpsURLConnection) urlConnection).setHostnameVerifier(DO_NOT_VERIFY);
		}
		urlConnection.setRequestProperty("Accept-Charset", "utf-8");
		urlConnection.setRequestProperty("connection", "Keep-Alive");
		urlConnection.setRequestMethod("GET");
		urlConnection.setConnectTimeout(500);
		urlConnection.setUseCaches(true);
		urlConnection.setDefaultUseCaches(true);
		//if(host!=null) urlConnection.setRequestProperty("Host", host);
		urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
		urlConnection.connect();
		final InputStream input = urlConnection.getInputStream();
//				String val = BU.StreamToString(input);
//				//input = new AutoCloseNetStream(input, urlConnection);
//				return new ByteArrayInputStream(val.getBytes(StandardCharsets.UTF_8));
		if (urlConnection.getResponseCode()==HTTP_NO_CONTENT) {
			return null;
		}
		CMN.debug("请求的是本机调试资源…", key);
		if(check)
			remoteDebugServer=ipAddress;
		return input;
	}
	
	
	//@StripMethods(strip=!BuildConfig.isDebug, keys={"getRemoteServerRes"})
	@Override
	protected InputStream OpenMdbResourceByName(String key) throws IOException {
		if(hasRemoteDebugServer)
		{
			InputStream ret = getRemoteServerRes(key, false);
			if(ret!=null) return ret;
		}
		if (BuildConfig.DEBUG || !BuildConfig.isDevBuild)
		{
			try {
				return app.getAssets().open(key.substring(1).replace("\\", "/"));
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		return super.OpenMdbResourceByName(key);
	}
	
	public String getClipboard() {
		// todo 处理悬浮状态
		if (app.isFloatingApp()) {
		
		}
		int fg = MainActivityUIBase.foreground;
		if (fg > 0) {
			int cc=0;
			while (fg > 0) {
				if((fg&1)>0) {
					MainActivityUIBase actor = AgentApplication.activities[cc].get();
					//CMN.debug("actor::", actor);
					if (actor != null) {
						View focus;
						if (actor.settingsPanel != null && actor.settingsPanel.isVisible()) {
							focus = actor.settingsPanel.getCurrentFocus();
						} else {
							focus = actor.getCurrentFocus();
						}
						if (focus != null) {
							CMN.debug("focus::", focus);
							if (focus instanceof TextView) {
								TextView tv = ((TextView) focus);
								if (tv.hasSelection()) {
									int st=tv.getSelectionStart(), ed=tv.getSelectionEnd();
									if (st > ed) {
										int tmp = st; st=ed; ed=tmp;
									}
									return tv.getText().subSequence(st, ed).toString();
								}
							}
							else if (focus instanceof WebViewmy) {
								WebViewmy wv = ((WebViewmy) focus);
								if (wv.bIsActionMenuShown||wv.weblistHandler!=null && ViewUtils.isVisibleV2(wv.weblistHandler.toolsBtn)) {
									FloatBtn.sClipboard = null;
									actor.hdl.post(new Runnable() {
										@Override
										public void run() {
											wv.evaluateJavascript("getSelection().toString()", value -> {
												CMN.debug("value =::", value);
												if (value.length() > 2) {
													value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
													//CMN.debug("initQuickTranslatorsBar::getSelection=", StringEscapeUtils.escapeJava(value));
													if (wv.presenter.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_PDF) {
														value = value.replaceAll("-\n", "");
														value = value.replaceAll("\n(?!\n)", " ");
													}
													FloatBtn.sClipboard = value;
												} else {
													FloatBtn.sClipboard = "";
												}
											});
										}
									});
									int tWait = 0;
									while(FloatBtn.sClipboard==null && tWait<1000) {
										try {
											Thread.sleep(100);
											tWait+=100;
										} catch (Exception e) {
											break;
										}
									}
									if (!TextUtils.isEmpty(FloatBtn.sClipboard)) {
										return FloatBtn.sClipboard;
									}
								}
							}
						}
					}
					break;
				}
				fg>>=1;
				cc++;
			}
		}
		CharSequence tmp = app.getFloatBtn().getPrimaryClip();
		String ret = tmp==null?null:tmp.toString();
		CMN.debug("getClipboard", ret, TextUtils.isEmpty(ret));
		if (TextUtils.isEmpty(ret) && (app.isFloating()|| app.getFloatBtn().isFloating())) {
			Intent newTask = new Intent(Intent.ACTION_MAIN);
			newTask.setType(Intent.CATEGORY_DEFAULT);
			newTask.putExtra(FloatBtn.EXTRA_FROMPASTE, true);
			newTask.putExtra(FloatBtn.EXTRA_GETTEXT, true);
			newTask.putExtra(FloatBtn.EXTRA_FETCHTEXT, true);
			newTask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			newTask.setClass(app.getApplicationContext(), PasteActivity.class);
			FloatBtn.sClipboard = null;
			app.getApplicationContext().startActivity(newTask);
			int tWait = 0;
			while(FloatBtn.sClipboard==null && tWait<1000) {
				try {
					Thread.sleep(100);
					tWait+=100;
				} catch (Exception e) {
					break;
				}
			}
			ret = FloatBtn.sClipboard;
		}
		return ret;
	}
	
	public void start(Context context) throws IOException {
		super.start();
		isServerRunning = true;
		if(PDICMainAppOptions.getNotificationEnabled() || PDICMainAppOptions.getAutoEnableNotification()) {
			ServiceEnhancer.SendSetUpDaemon(context);
		}
	}
	
	public void stop(Context context) {
		super.stop();
		isServerRunning = false;
		if(PDICMainAppOptions.getNotificationEnabled() || PDICMainAppOptions.getAutoEnableNotification()) {
			if(!PDICMainAppOptions.getNotificationEnabled()) {
				AU.stopService(context, ServiceEnhancer.class);
			} else {
				ServiceEnhancer.SendSetUpDaemon(context);
			}
		}
	}
}
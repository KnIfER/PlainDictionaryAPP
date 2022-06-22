package com.knziha.plod.plaindict;

import static com.knziha.plod.PlainUI.HttpRequestUtil.DO_NOT_VERIFY;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.view.inputmethod.EditorInfo;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.mdictRes_asset;

import org.apache.commons.imaging.BufferedImage;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.ManagedImageBufferedImageFactory;
import org.knziha.metaline.StripMethods;
import org.nanohttpd.protocols.http.HTTPSession;
import org.xiph.speex.ByteArrayRandomOutputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

import javax.net.ssl.HttpsURLConnection;

/**
 * Mdict Server
 * @author KnIfER
 * date 2020/06/01
 */

//@StripMethods(strip=!BuildConfig.isDebug, keys={"getRemoteServerRes"})
public class MdictServerMobile extends MdictServer {
	private MainActivityUIBase a;
	
	private static HashMap<String, Object>  mTifConfig;
	
	public MdictServerMobile(int port, MainActivityUIBase _a, PDICMainAppOptions _opt, MainActivityUIBase.LoadManager loadManager) throws IOException {
		super(port, _opt, loadManager);
		a = _a;
		a.serverHosts=new ConcurrentHashMap<>();
		if(a.serverHostsHolder!=null) {
			for (PlainWeb pw:a.serverHostsHolder) {
				a.serverHosts.putAll(pw.jinkeSheaths);
			}
		}
		webResHandler = new PlainWeb(new File(CMN.AssetTag+"plate.web"), _a);
		webResHandler.jinkeSheaths = a.serverHosts;
		try {
			MdbResource = new mdictRes_asset(new File(CMN.AssetTag, "MdbR.mdd"),2, a);
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
			int pos=IU.parsint(args.get("POS"), a.adaptermy.lastClickedPos);
			int dx=IU.parsint(args.get("DX"), a.dictPicker.adapter_idx);
			String key=a.etSearch.getText().toString();
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
		//CMN.Log("接到了接到了");
		List<String> target = parameters.get("f");
		if(target!=null && target.size()>0) {
			int sharetype = IU.parsint(target.get(0));
			//CMN.Log("sharetype", sharetype);
			byte[] data;
			try {
				data = new byte[inputStream.available()];
				inputStream.read(data);
			} catch (IOException e) {
				return;
			}
			String text = new String(data);
			a.root.post(() -> {
				if(sharetype==2) {
					a.execVersatileShare(text, opt.getSendToShareTarget());
				} else {
					switch (PDICMainAppOptions.getSendToAppTarget())
					{
						case 0:
							a.execVersatileShare(text, 0);
						break;
						case 1:
						{
							a.etSearch.setText(text);
							a.etSearch.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
							if(!a.focused) {
								ActivityManager manager = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
								if(manager!=null) manager.moveTaskToFront(a.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
							}
						} break;
						case 2:
						{
							a.JumpToFloatSearch(text);
						} break;
						case 3:
							a.execVersatileShare(text, 1);
						break;
					}
				}
			});
			CMN.Log("启动搜索 : ", text);
		}
	}
	
	public static HashMap<String, Object> getTifConfig() {
		if(mTifConfig==null) {
			mTifConfig = new HashMap<>(1);
			mTifConfig.put(ImagingConstants.BUFFERED_IMAGE_FACTORY, new ManagedImageBufferedImageFactory());
		}
		return mTifConfig;
	}
	
	public static InputStream getRemoteServerRes(String key, boolean check) {
		InputStream ret = null;
		if(hasRemoteDebugServer && PDICMainAppOptions.debug()) {
			try {
				if(check) {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(policy);
				}
				CMN.Log("OpenMdbResourceByName   http://192.168.0.100:8080/base/3" + key.replace("\\", "/"));
				String uri = "http://192.168.0.100:8080/base/3" + URLEncoder.encode(key.replace("\\", "/"));
				HttpURLConnection urlConnection = (HttpURLConnection) new URL(uri).openConnection();
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
				InputStream input = urlConnection.getInputStream();
//				String val = BU.StreamToString(input);
//				//input = new AutoCloseNetStream(input, urlConnection);
//				return new ByteArrayInputStream(val.getBytes(StandardCharsets.UTF_8));
				CMN.Log("请求的是本机调试资源…", key);
				return input;
			} catch (Exception e) {
				//CMN.Log(e);
				if (check) {
					hasRemoteDebugServer = false;
				}
			}
		}
		return ret;
	}
	
	
	//@StripMethods(strip=!BuildConfig.isDebug, keys={"getRemoteServerRes"})
	@Override
	protected InputStream OpenMdbResourceByName(String key) throws IOException {
		if(hasRemoteDebugServer)
		{
			InputStream ret = getRemoteServerRes(key, false);
			if(ret!=null) return ret;
		}
		//if ("\\MdbR\\subpage.html".equals(key))
		{
			try {
				return a.getAssets().open(key.substring(1).replace("\\", "/"));
			} catch (IOException e) {
				CMN.debug(e);
			}
		}
		return super.OpenMdbResourceByName(key);
	}
	
	public String strOpt;
	
	@Override
	public String getSettings() {
		String ret = strOpt;
		if (ret==null) {
			JSONObject json = JSONObject.parseObject(a.opt.getString("opt", "{}"));
			json.put("bg", SU.toHexRGB(CMN.GlobalPageBackground));
			json.put("bgr", SU.toHexRGB(CMN.AppBackground));
			ret = strOpt = json.toString();
		}
		return ret;
	}
}
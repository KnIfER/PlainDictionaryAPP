package com.knziha.plod.PlainDict;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.inputmethod.EditorInfo;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.mdictRes_asset;

import org.apache.commons.imaging.BufferedImage;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.ManagedImageBufferedImageFactory;
import org.xiph.speex.ByteArrayRandomOutputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

/**
 * Mdict Server
 * @author KnIfER
 * date 2020/06/01
 */
	
public class MdictServerMobile extends MdictServer {
	private PDICMainActivity a;
	private static HashMap<String, Object>  mTifConfig;
	
	public MdictServerMobile(int port, PDICMainActivity _a, PDICMainAppOptions _opt) throws IOException {
		super(port, _opt);
		a = _a;
		MdbServerLet = _a;
		MdbResource = new mdictRes_asset(new File(CMN.AssetTag, "MdbR.mdd"),2, a);
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
			int dx=IU.parsint(args.get("DX"), a.adapter_idx);
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
}
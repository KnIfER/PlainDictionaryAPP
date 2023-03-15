package com.knziha.plod.dictionarymodels;

import static com.knziha.plod.plaindict.CMN.AssetTag;

import android.webkit.WebView;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.dictionary.GetRecordAtInterceptor;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.MdictServer;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.widgets.WebViewmy;

import org.knziha.metaline.Metaline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PlainPDF extends DictionaryAdapter {
	mdictRes_asset INTERNAL_RES;
	public String[] pdf_index;
	boolean alphabetic;
	
	/**
	var outlines = window.PDFViewerApplication.pdfOutlineViewer.outline;
	var contents = new Array(outlines.length);

	function extractContents(idx){
		if(idx>=outlines.length) {harvest(idx);return;}
		var item=outlines[idx]==null?null:outlines[idx].dest[0];
		if(item!=null && item["num"]!=null)
			window.PDFViewerApplication.pdfDocument.getPageIndex(item).then(
				function(o){
					contents[idx]=o;
					try{ extractContents(idx+1); } catch(e){ harvest(idx);return; }
				}
			).catch(function(){harvest(idx)});
		else
			extractContents(idx+1);
	}

	function harvest(idx){
		for(var i=0;i<outlines.length;i++){
			if(contents[i]!=null)
				contents[i]=outlines[i].title.replace("\n", "")+":"+contents[i];
			else
				contents[i]=outlines[i].title.replace("\n", "");
		}
		//console.log(contents.join("\n"));
		if(window.app)
			window.app.parseContent(idx, outlines.length, contents.join("\n"));
	}

	extractContents(0);
	 */
	@Metaline
	final static String parseCatalogue = "CONTENTS";
	
	/**
	var _df=document.getElementById('_df');
	if(!_df.tx) _df.tx = _df.innerText;
	_df.innerText = '';
	 */
	@Metaline
	public final static String debugFontOff = "";
	
	/**
	var _df=document.getElementById('_df');
	if(_df.tx) _df.innerText = _df.tx;
	 */
	@Metaline
	public final static String debugFontOn = "";

	private int targetPage;

	//构造
	public PlainPDF(File fn, MainActivityUIBase _a) throws IOException {
		super(fn, _a);
		try {
			INTERNAL_RES=new mdictRes_asset(new File(AssetTag +"pdf.mdd"), 2, _a);
		} catch (Exception e) {
			CMN.debug("fail to init PlainPDF::", e);
		}
		_num_entries = 1;
		
		File path = new File(_a.getExternalFilesDir(".PDF_INDEX"), _Dictionary_fName);
		if(path.exists()){
			String data = BU.fileToString(path);
			int idTmp=data.lastIndexOf("\r\n");
			if(idTmp!=-1){
				String jdata = data.substring(idTmp+2);
				try {
					JSONObject JO = JSONObject.parseObject(jdata);
					alphabetic = JO.getBooleanValue("SORTED");
					String SIP = JO.getString("ALPHABETICINDEX");
					if(SIP!=null){

					}
					SIP = JO.getString("KEYWORDINDEX");
					if(SIP!=null){

					}
					data=data.substring(0, idTmp);
				} catch (Exception e) { e.printStackTrace(); }
			}
			pdf_index = data.split("\n");
		}
		mType = DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_PDF;
	}

	public void parseContent(WebView mWebView) {
		mWebView.evaluateJavascript(parseCatalogue, null);
	}
	
	@Override
	public String getCharsetName() {
		return "PDF";
	}
	
	//@Override
	//public void renderContentAt(float initialScale, int SelfIdx, int frameAt, WebViewmy mWebView, int... position) {
	//	targetPage=position[0];
	//	if(mWebView==null)
	//		mWebView=this.mWebView;
	//	Object youname=mWebView.getTag(R.id.dictName);
	//	boolean proceed=!(_Dictionary_fName.equals(youname) &&(mWebView.getTag(R.id.virtualID)==(Integer)0));
	//	if(proceed){
	//		super.renderContentAt(-1, SelfIdx, frameAt, mWebView, 0);
	//		mWebView.setTag(R.id.dictName, _Dictionary_fName);
	//		mWebView.setTag(R.id.virtualID, 0);
	//		mWebView.setTag(R.id.positionID, targetPage);
	//	}
	//	if(initialScale!=111){
	//		if(targetPage>0 && pdf_index!=null){
	//			if(targetPage<=pdf_index.length){
	//				initialScale=111;
	//				String entry = pdf_index[targetPage-1];
	//				int pdf_idx = entry.lastIndexOf(":");
	//				if(pdf_idx!=-1&&pdf_idx<entry.length()-1){
	//					int page_idx = IU.parsint(entry.substring(pdf_idx+1), -1);
	//					if(page_idx!=-1) targetPage=page_idx+1;
	//					entry=entry.substring(0, pdf_idx);
	//				}
	//				StringBuilder title_builder = AcquireStringBuffer(64);
	//				toolbar_title.setText(title_builder.append(entry.trim()).append(" - ").append(_Dictionary_fName).toString());
	//			}
	//		}else{
	//			targetPage=-1;
	//		}
	//	}
	//	if(initialScale==111)
	//		mWebView.evaluateJavascript("window.PDFViewerApplication.page=" + targetPage, null);
	//}

	@Override
	public String getEntryAt(long position) {
		return getDictionaryName();
	}

	@Override
	public String getEntryAt(long position, Flag mflag) {
		return getEntryAt(position);
	}

	@Override
	public int lookUp(String keyword, boolean isSrict) {
		//if()
		return -1;
	}

	@Override
	public String getRecordAt(long position, GetRecordAtInterceptor getRecordAtInterceptor, boolean allowJump) throws IOException {
		if (MdictServer.hasRemoteDebugServer) {
			try {
				MainActivityUIBase a = AgentApplication.activities[0].get();
				String ret = a.fileToString("/ASSET/pdfjs/web/"+"index");
				if (ret != null) return ret;
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		return new String(INTERNAL_RES.getRecordData(INTERNAL_RES.lookUp("\\index")), StandardCharsets.UTF_8);
	}

	@Override
	public InputStream getResourceByKey(String key) {
		CMN.debug("getResourceByKey::", key);
		if (MdictServer.hasRemoteDebugServer) {
			try {
				MainActivityUIBase a = AgentApplication.activities[0].get();
				InputStream ret = a.fileToStream(new File("/ASSET/pdfjs/web" + key.replace("\\", "/")));
				if (ret != null) return ret;
			} catch (Exception e) {
				CMN.debug(key, e);
			}
		}
		int id= INTERNAL_RES.lookUp(key);
		if (id >= 0) {
			try {
				return INTERNAL_RES.getResourseAt(id);
			} catch (IOException e) {
				CMN.Log(e);
			}
		} else {
			CMN.debug("找不到::", key);
		}
		return null;
	}
	
	@Override
	public String getVirtualRecordsAt(Object presenter, long[] list2) throws IOException {
		return getRecordsAt(null, 0);
	}
	
	@Override
	public boolean hasVirtualIndex() {
		return true;
	}
	
	@Override
	public boolean hasMdd() {
		return true;
	}
	
	@Override
	public String getVirtualTextValidateJs(Object presenter, Object mWebView, long position) {
		return "PDFViewerApplication?1:0";
	}
	
	@Override
	public String getVirtualTextEffectJs(Object presenter, long[] positions) {
		return null;
	}
	
	public void onPageFinished(BookPresenter bookPresenter, Object wv, String url, boolean updateTitle) {
		CMN.debug("PDF", "web  - onPageFini_NWPshed", url, getDictionaryName());
		WebViewmy mWebView = (WebViewmy) wv;
		mWebView.evaluateJavascript(PDICMainAppOptions.debugPDFFont()?debugFontOn:debugFontOff, null);
	}
}

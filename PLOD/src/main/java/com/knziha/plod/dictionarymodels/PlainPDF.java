package com.knziha.plod.dictionarymodels;

import android.webkit.WebView;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;

import org.adrianwalker.multilinestring.Multiline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.knziha.plod.plaindict.CMN.AssetTag;

public class PlainPDF extends DictionaryAdapter {
	mdictRes_asset _INTERNAL_PDFJS;
	public String[] pdf_index;
	boolean alphabetic;

	/**
	 <script>
		window.addEventListener('click',wrappedClickFunc);
	 	function wrappedClickFunc(e){
			if(e.srcElement!=document.documentElement){
				var s = window.getSelection();
				if(s.isCollapsed && s.anchorNode){ // don't bother with user selection
					s.modify('extend', 'forward', 'word'); // first attempt
					//if(1) return;
					var an=s.anchorNode;
					//console.log(s.anchorNode); console.log(s);  console.log(s.getRangeAt(0)); console.log(s+'');
					fixPdfForword(s);
					if(s.baseNode != document.body) {// immunize blank area
						var scer=s.getRangeAt(0);
						if(scer.startOffset==0){
							scer=scer.startContainer;
							if(scer.nodeType==3){
								scer=scer.parentNode;
							}
							if(scer.previousSibling==null){
								console.log(e.clientX-scer.offsetLeft, e.clientY-scer.offsetTop);
								if(Math.abs(e.clientX-scer.offsetLeft)>50||Math.abs(e.clientY-scer.offsetTop)>50){
									s.empty();
									scer=null;
								}
							}
						}
						if(scer){
							scer=0;
							var text=s.toString(); // for word made up of just one character
							if(text.length>0){
								var re=/[\u4e00-\u9fa5]/g;
								tillNext=text;
								if(re.test(text.trim())){
									scer=1;
								}else{
									var range = s.getRangeAt(0);  // first attempt, forward range
									//console.log(range);
									var lb='lineboundary';
									s.collapseToStart();
									s.modify('extend', 'forward', lb);
									tillNextLine=s.toString();
									var eN='word';
									var eB='word';
									if(tillNextLine.trim()!="") {
										s.collapseToStart();
										s.modify('extend', 'backward', 'word');
										tillNext=s.toString();
										s.collapseToEnd();
										s.modify('extend', 'backward', lb);
										tillNextLine=s.toString();

										// !!!  !!! sometime in the pdf
										// a lot of tags till be wrongly treated as in one line.
										if(tillNextLine.length<tillNext.length){
											var code=tillNextLine.charAt(0);
											if(code.toUpperCase()===code && code.toLowerCase!=code) eB=lb;
										}

										s.empty(); s.addRange(range);
										s.collapseToStart();


										s.modify('move', 'backward', eB);

										s.modify('extend', 'forward', 'word');
										tillNext=s.toString();
										s.collapseToStart();
										s.modify('extend', 'forward', lb);
										tillNextLine=s.toString();

										// !!!  !!! sometime in the pdf
										// a lot of tags till be wrongly treated as in one line.
										if(tillNextLine.length<tillNext.length){
											var code=tillNext.charAt(tillNextLine.length);
											if(code.toUpperCase()==code && code.toLowerCase()!=code) eN=lb;
										}
										s.collapseToStart();

										s.modify('extend', 'forward', eN);

										text=s.toString();
										scer=1;
									}
								}
								if(scer){
									console.log(text); // final output
								}
							}
						}
					}
					//s.empty();
				}
			}
	 	}
		function fixPdfForword(s){
			var r = s.getRangeAt(0);
			if(r.startContainer!=r.endContainer){
				r.setEndAfter(r.startContainer);
			}
		}
		function fixPdfBackword(s){
			var r = s.getRangeAt(0);
			if(r.startContainer!=r.endContainer){
				r.setStartBefore(r.endContainer);
			}
		}
	 </script>
	 */
	@Multiline
	public final static String js="SUBPAGE";
	
	/**";</script><script src="pdfviewer.js"></script></head>*/
	@Multiline
	final static String tailing = "TAIL";

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
	@Multiline
	final static String parseCatalogue = "CONTENTS";

	private int targetPage;

	//构造
	public PlainPDF(File fn, MainActivityUIBase _a) throws IOException {
		super(fn, _a);
		_INTERNAL_PDFJS=new mdictRes_asset(new File(AssetTag +"pdf.mdd"), 2, _a);
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
	}

	public void parseContent(WebView mWebView) {
		mWebView.evaluateJavascript(parseCatalogue, null);
	}

	@Override
	public String getSimplestInjection() {
		return js;
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
	public String getEntryAt(int position) {
		return getDictionaryName();
	}

	@Override
	public String getEntryAt(int position, Flag mflag) {
		return getEntryAt(position);
	}

	@Override
	public int lookUp(String keyword, boolean isSrict) {
		//if()
		return -1;
	}

	@Override
	public String getRecordsAt(int... positions) throws IOException {
		return new String(_INTERNAL_PDFJS.getRecordData(_INTERNAL_PDFJS.lookUp("index")), StandardCharsets.UTF_8)+f.getAbsolutePath()+tailing;
	}

	@Override
	public InputStream getResourceByKey(String key) {
		int id=_INTERNAL_PDFJS.lookUp(key);
		if(id>=0) {
			try {
				return _INTERNAL_PDFJS.getResourseAt(id);
			} catch (IOException e) {
				CMN.Log(e);
			}
		}
		return null;
	}
	
	@Override
	public String getVirtualRecordsAt(int[] list2) throws IOException {
		return getRecordsAt(0);
	}
	
	@Override
	public boolean hasVirtualIndex() {
		return true;
	}
	
	@Override
	public String getVirtualTextValidateJs() {
		return "";
	}
	
	@Override
	public String getVirtualTextEffectJs(int[] positions) {
		return "";
	}
	
	@Override
	public boolean hasMdd() {
		return true;
	}
	
	public void toggleFavor() {
	
	}
}

/*  Copyright 2018 KnIfER Zenjio-Kang

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	
	Mdict-Java Query Library
*/
package com.knziha.plod.PlainDict;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.mdBase;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.rbtree.RBTree_additive;
import com.knziha.rbtree.additiveMyCpr1;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.xiph.speex.ByteArrayRandomOutputStream;
import org.xiph.speex.manyclass.JSpeexDec;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

//import fi.iki.elonen.NanoHTTPD;
//import fi.iki.elonen.NanoHTTPD.Response.Status;


/**
 * Mdict Server
 * @author KnIfER
 * @date 2018/09/19
 */

public abstract class MdictServer extends NanoHTTPD {
	interface AppOptions {
		boolean isCombinedSearching();
		int getSendToShareTarget();
	}
	final Pattern nautyUrlRequest = Pattern.compile("src=(['\"])?(file://)?/");
	final String SepWindows = "\\";
	final AppOptions opt;
	
	String baseHtml;
	public ArrayList<mdict> currentFilter = new ArrayList<>();
	
	protected mdBase MdbResource;
	protected MdictServerLet MdbServerLet;
	private int md_size;
	
	public MdictServer(int port, AppOptions _opt) {
		super(port);
		opt=_opt;
	}
	
	@Override
	public Response handle(IHTTPSession session) throws IOException {
		int adapter_idx_ = 0;
		String uri = session.getUri();
		SU.Log("serving with honor : ", uri);
		Map<String, String> headerTags = session.getHeaders();
		String Acc = headerTags.get("accept");
		if(Acc==null) Acc= StringUtils.EMPTY;
		String usr = headerTags.get("user-agent");
		String key = uri.replace("/", SepWindows);
		if(usr==null) return null;
		
		if(uri.startsWith("/MdbR/")) {
			//SU.Log("[fetching internal res : ]", uri);
			//InputStream candi = MdictServer.class.getResourceAsStream("Mdict-browser"+uri);
			InputStream candi = OpenMdbResourceByName(uri.replace("/", "\\"));
			if(candi!=null) {
				String mime="*/*";
				if(uri.contains(".css")) mime = "text/css";
				if(uri.contains(".js")) mime = "text/js";
				try {
					return newFixedLengthResponse(Status.OK,mime,  candi, candi.available());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(uri.startsWith("/MIRROR.jsp")) {
			if(om!=null)
				return om.onMirror(session.getQueryParameterString(), true);
			return newFixedLengthResponse(getBaseHtml());
		}
		if(uri.startsWith("/READ.jsp")) {
			if(om!=null) {
				return om.onMirror(session.getQueryParameterString(), false);
			}
			return newFixedLengthResponse(getBaseHtml());
		}
		
		if(uri.startsWith("/MdbRSingleQuery/")) {
			uri = uri.substring("/MdbRSingleQuery/".length());
			//SU.Log("MdbRSingleQuery: "+uri);
			String[] list = uri.split("/");
			adapter_idx_ = Integer.parseInt(list[0]);
			SU.Log("/MdbRSingleQuery/:",uri.substring(list[0].length()+1),md_get(adapter_idx_).lookUp(Reroute(uri.substring(list[0].length()+1))));
			return newFixedLengthResponse(Integer.toString(md_get(adapter_idx_).lookUp(Reroute(uri.substring(list[0].length()+1)),false)));
		}
		else if(uri.startsWith("/MdbRJointQuery/")) {
			uri = Reroute(uri.substring("/MdbRJointQuery/".length()));
			//SU.Log("MdbRJointQuery: "+uri);
			RBTree_additive combining_search_tree_ = new RBTree_additive();
			StringBuilder sb_ = new StringBuilder();
			for(int i=0;i<md_size();i++){
				try {
					md_get(i).size_confined_lookUp5(uri,combining_search_tree_,i,30);
				} catch (Exception e) {
					SU.Log(md_getName(i), e);
				}
			}
			ArrayList<additiveMyCpr1> combining_search_result = combining_search_tree_.flatten();
			for(int i=0;i<combining_search_result.size();i++) {
				additiveMyCpr1 resI = combining_search_result.get(i);
				sb_.append(resI.key).append("\r");
				ArrayList<Integer> result = (ArrayList<Integer>) resI.value;
				int lastIdx=-1;
				for(int ii=0;ii<result.size();ii+=2) {
					int currIdx=result.get(ii);
					if(lastIdx!=currIdx) {
						if(lastIdx!=-1)
							sb_.append("&");
						sb_.append(currIdx);
					}
					lastIdx = currIdx;
					sb_.append("@").append(result.get(ii+1));
				}
				if(i!=combining_search_result.size()-1)
					sb_.append("\n");
			}
			SU.Log(sb_.toString());
			return newFixedLengthResponse(sb_.toString());
		}
		////////////////////////////////////////////////////////
		boolean ReceiveText=Acc.contains("text/html");
		boolean IsCustomer=!usr.contains("Java");
		if(uri.startsWith("/base/")) {
			SU.Log("requesting ifram", uri);
			uri = uri.substring("/base/".length());
			String[] list = uri.split("/");
			adapter_idx_ = Integer.parseInt(list[0]);
			uri = uri.substring(list[0].length());
			key = uri.replace("/", SepWindows);
			mdict mdTmp = md_get(adapter_idx_);
			if(list.length==1){
				try {
					int index = mdTmp.lookUp("index");
					return newFixedLengthResponse(md_get(adapter_idx_).getRecordsAt(index>=0?index:0));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(list[1].equals("@@@")) {//  /base/0/@@@/name
				key = uri.substring(list[0].length()+1+3);
				SU.Log("rerouting..."+key);
				try {
					return newFixedLengthResponse(mdTmp.getRecordsAt(md_get(adapter_idx_).lookUp(key)));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			else if(list[1].equals("VI") && mdTmp.hasVirtualIndex()) {//  /base/0/VI/0
				try {
					int VI = IU.parsint(list[2],-1);
					//SU.Log("virtual content..."+VI, mdTmp.getVirtualRecordAt(VI));
					return newFixedLengthResponse(mdTmp.getVirtualRecordAt(VI));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//else SU.Log("!!! !!! !!!", uri);
		
		final String entry="/entry/";
		final String raw="/raw/";
		boolean b1 ,b2;
		if((b1=uri.startsWith(entry)) || uri.startsWith(raw))  {
			try {
				key=key.substring(b1?entry.length():raw.length());
				if(key.contains("#")){
					key=key.substring(key.indexOf("#"));
				}
				if(key.endsWith("\\"))
					key=key.substring(0, key.length()-1);
				SU.Log("jumping...", key);
				mdict mdTmp = md_get(adapter_idx_);
				String res = mdTmp.getRecordsAt(mdTmp.lookUp(key));
				return newFixedLengthResponse(constructMdPage(mdTmp, Integer.toString(adapter_idx_), res, b1));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return emptyResponse;
		}
		
		
		try {
			if(uri.toLowerCase().endsWith(".js")) {
				//SU.Log("candi",uri);
				File candi = new File(new File(md_get(adapter_idx_).getPath()).getParentFile(),new File(uri).getName());
				if(candi.exists())
					return newFixedLengthResponse(Status.OK,"application/x-javascript",  new FileInputStream(candi), candi.length());
				
			}
			else if(uri.toLowerCase().endsWith(".css")) {
				File candi = new File(new File(md_get(adapter_idx_).getPath()).getParentFile(),new File(uri).getName());
				SU.Log("candi_css",uri,candi.getAbsolutePath(), candi.exists());
				if(candi.exists()) {
					return newFixedLengthResponse(Status.OK,"text/css",  new FileInputStream(candi), candi.length());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if(key.equals("\\dicts.json")) {
			if(md_size()>0) {
				StringBuilder sb = new StringBuilder();
				
				
				for (int i = 0; i < md_size; i++) {
					mdict mdx = md_get(i);
					sb.append(mdx==null?"null":mdx.getDictionaryName());
					
					//sb.append(md_getName(i));
					//if(mdx.hasVirtualIndex())
					//	sb.append(":VI&VI");
					sb.append("\n");
				}
				sb.setLength(sb.length()-1);
				return newFixedLengthResponse(sb.toString()) ;
			}
			return emptyResponse;
		}
		else if(key.startsWith("\\MdbRSize\\")) {
			if(md_size()>0) {
				key = key.substring(10);
				try {
					//key = URLDecoder.decode(key, "UTF-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
				long ret=0;
				for (int i = 0; i < md_size; i++) {
					mdict mdx = md_get(i);
					if(mdx!=null && mdx.getDictionaryName().equals(key))
						ret=mdx.getNumberEntries();
				}
				SU.Log("MdbRSize ret: "+ret+key);
				return newFixedLengthResponse(ret+"") ;
			}
			return emptyResponse;
		}
		else if(key.startsWith("\\MdbRGetEntries\\")) {
			if(md_size()>0) {
				key = key.substring(16);
				//SU.Log(key);
				String[] l = key.split("\\\\");
				StringBuilder ret= new StringBuilder();
				for (int i = 0; i < md_size; i++) {
					mdict mdx = md_get(i);
					if(mdx._Dictionary_fName.equals(l[0])) {
						StringBuilder sb = new StringBuilder();
						//SU.Log("capacity "+l[2]);
						int capacity=Integer.parseInt(l[2]);
						int base = Integer.parseInt(l[1]);
						for(int j=0;j<capacity;j++) {
							ret.append(mdx.getEntryAt(base + j));
							if(j<capacity-1)
								ret.append("\n");
							//sb.append(mdx.getEntryAt(base+i)).append("\n");
						}
						//sb.setLength(sb.length()-1);
						//ret = sb.toString();
						break;
					}
				}
				
				return newFixedLengthResponse(ret.toString()) ;
			}
			return emptyResponse;
		}
		
		if(uri.startsWith("/sound/")) {
			key=uri.substring(6).replace("/","\\");
		}
		
		if(uri.equals("/")) {
			return newFixedLengthResponse(getBaseHtml());
		}
		
		if(uri.startsWith("/PLOD/")) {
			//SU.Log("about received : ", uri);
			handle_search_event(session.getParameters(), session.getInputStream());
			return emptyResponse;
		}
		
		if(uri.startsWith("/about/")) {
			//SU.Log("about received : ", uri);
			uri = uri.substring(7);
			try {
				mdict mdTmp = md_get(Integer.parseInt(uri));
				return newFixedLengthResponse(mdTmp.getAboutHtml());
			} catch (Exception ignored) { }
		}
		
		if(uri.startsWith("/content/")) {
			SU.Log("content received : ", uri); //  /content/1@6
			uri = uri.substring(9);
			String[] list = uri.split("@");
			if(!list[0].equals("")) {
				String lit=list[list.length - 1];
				int lid = lit.lastIndexOf(":");
				if(lid!=-1){
					list[list.length - 1]=lit.substring(0,lid);
					lid=IU.parsint(lit.substring(lid+1), -1);
				}
				try {
					adapter_idx_ = Integer.parseInt(list[0]);
					mdict mdTmp = md_get(adapter_idx_);
					int[] list2 = new int[list.length-1];
					for(int i=0;i<list.length-1;i++)
						list2[i]=Integer.parseInt(list[i+1]);
					return newFixedLengthResponse(constructMdPage(mdTmp, list[0],lid!=-1?mdTmp.getVirtualRecordsAt(list2):mdTmp.getRecordsAt(list2), true));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return newFixedLengthResponse(constructMdPage(md_get(adapter_idx_), 0+"","<div>ERROR FETCHING CONTENT:"+uri+"</div>", true));
		}
		
		key = mdict.requestPattern.matcher(key).replaceAll("");
		mdict mdTmp = md_get(adapter_idx_);
		InputStream restmp = mdTmp.getResourceByKey(key);
		SU.Log("-----> /dictionary/", adapter_idx_, mdTmp._Dictionary_fName, key, restmp==null?-1:restmp.available());
		
		if(restmp==null){
			return emptyResponse;
		}
		
		if(Acc.contains("javascript/") || uri.endsWith(".js")) {
			return newFixedLengthResponse(Status.OK,"application/x-javascript",restmp,restmp.available());
		}
		
		if(Acc.contains("/css") || uri.endsWith(".css")) {
			return newFixedLengthResponse(Status.OK,"text/css", restmp, restmp.available());
		}
		
		if(Acc.contains("/mp3") || uri.endsWith(".mp3")) {
			//SU.Log("mp3 : "+uri);
			return newFixedLengthResponse(Status.OK,"audio/mpeg", restmp, restmp.available());
		}
		
		if(uri.contains(".pdf")) {
			return newFixedLengthResponse(Status.OK,"application/pdf", restmp, restmp.available());
		}
		
		if(uri.endsWith(".spx")) {
			//SU.Log("spx : "+uri);
			ByteArrayRandomOutputStream bos = new ByteArrayRandomOutputStream();
			JSpeexDec decoder = new JSpeexDec();
			try {
				decoder.decode(new DataInputStream(restmp) , bos, JSpeexDec.FILE_FORMAT_WAVE);
				return newFixedLengthResponse(Status.OK,"text/x-wav", new ByteArrayInputStream(bos.toByteArray()) , bos.size());
			} catch (Exception e) { e.printStackTrace(); }
			return newFixedLengthResponse(Status.OK,"audio/mpeg", restmp, restmp.available());
		}
		
		if(Acc.contains("image/") ) {
			SU.Log("Image request : ",Acc,key,mdTmp._Dictionary_fName);
			if(uri.endsWith(".tif")||uri.endsWith(".tiff"))
				try {
					restmp = convert_tiff_img(restmp);
					//CMN.pt("再编码耗时 : ");
				} catch (Exception e) { e.printStackTrace(); }
			
			return newFixedLengthResponse(Status.OK,(IsCustomer&&ReceiveText)?"text/plain":"image/*", restmp, restmp.available());
		}
		
		return newFixedLengthResponse(Status.OK,"*/*", restmp, restmp.available());
	}
	
	protected abstract InputStream convert_tiff_img(InputStream restmp) throws Exception;
	
	protected abstract void handle_search_event(Map<String, List<String>> text, InputStream inputStream);
	
	private String md_getName(int pos) {
		return MdbServerLet.md_getName(pos);
	}
	
	private mdict md_get(int pos) {
		return MdbServerLet.md_get(pos);
	}
	
	private int md_size() {
		return md_size=MdbServerLet.md_getSize();
	}
	
	protected InputStream OpenMdbResourceByName(String key) throws IOException {
		InputStream ret = null;
		if(MdbResource instanceof com.knziha.plod.dictionary.mdict) {
			ret = ((com.knziha.plod.dictionary.mdict)MdbResource).getResourceByKey(key);
		} else {
			int id = MdbResource.lookUp(key);
			if(id>=0) {
				ret = MdbResource.getResourseAt(id);
			}
		}
		return ret;
	}
	
	private String Reroute(String currentText) {
		SU.Log(currentFilter.size(), "Reroute", currentText);
		try {
			for (mdict mdTmp:currentFilter) {
				Object rerouteTarget = mdTmp.ReRoute(currentText);
				if(rerouteTarget instanceof String){
					String text = (String) rerouteTarget;
					SU.Log("Reroute",mdTmp._Dictionary_fName, text);
					if(text.trim().length()>0){
						currentText=text;
						break;
					}
				}
			}
		} catch (IOException ignored) { }
		return currentText;
	}
	
	/**
	 <script>
	 var postInit;
	 if(false)
	 if(window.addEventListener){
	 window.addEventListener('load',wrappedOnLoadFunc,false);
	 window.addEventListener('click',wrappedClickFunc);
	 }else if(window.attachEvent){ //ie
	 window.addEventListener('onload',wrappedOnLoadFunc);
	 window.addEventListener('onclick',wrappedClickFunc);
	 }else{
	 window.onload=wrappedOnLoadFunction;
	 window.onclick=wrappedClickFunc;
	 }
	 function wrappedOnLoadFunc(){
	 document.getElementById('view1').setAttribute('content', 'width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no');
	 if(postInit) postInit();
	 }
	 
	 var audio;
	 var regHttp=new RegExp("^https?://");
	 var regEntry=new RegExp("^entry://");
	 var regPdf=new RegExp("^pdf://");
	 var regSound=new RegExp("^sound://");
	 
	 function hiPlaySound(e){
	 var cur=e.ur1?e:e.srcElement;
	 //console.log("hijacked sound playing : "+cur.ur1);
	 if(audio)
	 audio.pause();
	 else
	 audio = new Audio();
	 audio.src = cur.ur1;
	 audio.play();
	 }
	 
	 function loadVI(pos){
	 console.log('loadVI/'+pos);
	 var req=new XMLHttpRequest();
	 req.open('GET','/VI/'+pos);
	 req.responseType='json';
	 req.onreadystatechange=function(e) {
	 if(req.readyState == 4 && req.status==200) {
	 console.log(req.responseText);
	 }
	 }
	 }
	 
	 function wrappedClickFunc(e){
	 var cur=e.srcElement;
	 if(cur.href){
	 //console.log("1! found link : "+cur.href+" : "+regSound.test(cur.href));
	 if(regEntry.test(cur.href))
	 cur.href="entry/"+cur.href.substring(8);
	 else if(regSound.test(cur.href)){//拦截 sound 连接
	 var link="sound/"+cur.href.substring(8);
	 cur.href=link;
	 if(cur.onclick==undefined){
	 //console.log("1! found internal sound link : "+cur.href);
	 cur.ur1=cur.href;
	 cur.removeAttribute("href");
	 cur.onclick=hiPlaySound;
	 hiPlaySound(cur);
	 return false;
	 }
	 }
	 }
	 else if(cur.src && regEntry.test(cur.src)){
	 console.log("2! found link : "+cur.src);
	 return false;
	 }
	 else if(false && e.srcElement!=document.documentElement){ // immunize blank area out of body ( in html )
	 //console.log(e.srcElement+'')
	 //console.log(e)
	 var s = window.getSelection();
	 if(s.isCollapsed && s.anchorNode){ // don't bother with user selection
	 s.modify('extend', 'forward', 'word'); // first attempt
	 var an=s.anchorNode;
	 //console.log(s.anchorNode); console.log(s);
	 //if(true) return;
	 
	 if(s.baseNode != document.body) {// immunize blank area
	 var text=s.toString(); // for word made up of just one character
	 var range = s.getRangeAt(0);
	 
	 s.collapseToStart();
	 s.modify('extend', 'forward', 'lineboundary');
	 
	 if(s.toString().length>=text.length){
	 s.empty();
	 s.addRange(range);
	 
	 s.modify('move', 'backward', 'word'); // could be next line
	 s.modify('extend', 'forward', 'word');
	 
	 if(s.getRangeAt(0).endContainer===range.endContainer&&s.getRangeAt(0).endOffset===range.endOffset){
	 // 字未央
	 text=s.toString();
	 }
	 
	 console.log(text); // final output
	 }
	 }
	 //s.collapseToStart();
	 return false;
	 }
	 }
	 return true;
	 };
	 </script>
	 
	 <base href='/base/*/
	@Multiline(trim = false)
	String SimplestInjection="SimplestInj easdas  glaofssssssdsactionasdasd";
	/** /'/>
	 <base target="_self" />
	 */
	@Multiline(trim=false)
	String SimplestInjectionEnd="SimplestInsjectionEnd";
	
	int MdPageBaseLen=-1;
	String MdPage_fragment1,MdPage_fragment2, MdPage_fragment3="</html>";
	int MdPageLength=0;
	private String constructMdPage(mdict mdTmp, String dictIdx, String record, boolean b1) {
		if(b1 && mdict.fullpagePattern.matcher(record).find())
			b1=false;
		//b1=true;
		if(b1) {
			StringBuilder MdPageBuilder = new StringBuilder(MdPageLength + record.length() + 5);
			//String MdPage_fragment1 = null, MdPage_fragment2 = null, MdPage_fragment3 = "</html>";
			//int MdPageBaseLen = -1; //rrr
			if (MdPageBaseLen == -1) {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(OpenMdbResourceByName("\\MdbR\\subpage.html")));
					String line;
					while ((line = in.readLine()) != null) {
						MdPageBuilder.append(line).append("\r\n");
						if (MdPage_fragment1 == null) {
							if (line.equals("<base href='/base//'/>")) {
								MdPageBuilder.setLength(MdPageBuilder.length() - 6);
								MdPage_fragment1 = MdPageBuilder.toString();
								MdPageBuilder.setLength(0);
								MdPageBuilder.append("/'/>\r\n");
							}
						}
					}
					MdPage_fragment2 = MdPageBuilder.toString();
					MdPageBuilder.setLength(0);
					MdPageLength = MdPage_fragment1.length() + MdPage_fragment2.length() + MdPage_fragment3.length();
					MdPageBaseLen = MdPage_fragment1.length();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			MdPageBuilder.append(MdPage_fragment1);
			
			//作为建议，mdd资源请求应使用相对路径以减少处理过程。
			record = nautyUrlRequest.matcher(record).replaceAll("src=$1");
			
			MdPageBuilder
					.append(dictIdx)// "/base/0"
					.append(MdPage_fragment2)
					.append("<link rel='stylesheet' type='text/css' href='").append(mdTmp._Dictionary_fName).append(".css'/>")
					.append("</head>")
					.append(record)
					.append(MdPage_fragment3)
			;
			MdPageBuilder.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
			if(mdTmp.hasMdd()) MdPageBuilder.append("<p class='MddExist'/>");
			MdPageBuilder.append("</div>");
			return MdPageBuilder.toString();
		}
		else{
			if(true){
				int idx = record.indexOf("<head>");
				StringBuilder MdPageBuilder = new StringBuilder(MdPageLength + record.length() + 5);
				String start = idx==-1?"<head>":record.substring(0, idx+6);
				String end = idx==-1?record:record.substring(idx+6);
				
				MdPageBuilder
						.append(start)
						.append(SimplestInjection)
						.append(dictIdx)// "/base/0"
						.append(SimplestInjectionEnd)
						.append(idx==-1?"</head>":"")
						.append(end);
				return MdPageBuilder.toString();
			}
			return record;
		}
	}
	Response emptyResponse = newFixedLengthResponse(Status.NO_CONTENT,"*/*", "");
	
	interface OnMirrorRequestListener{
		public Response onMirror(String uri, boolean mirror);
	}OnMirrorRequestListener om;
	
	public void setOnMirrorRequestListener(OnMirrorRequestListener om_) {
		om = om_;
	}
	
	
	
	
	int derBaseLen = -1;
	String restFragments;
	
	public String constructDerivedHtml(String key,int pos,int dictionaryIndice,String iframes) {
		StringBuilder derivedHtmlBase;
		if(true || derBaseLen == -1) {//rrr
			String insertsionPoint = "var postInit = function(){";
			String baseHtml = getBaseHtml();
			int idx1=baseHtml.indexOf(insertsionPoint);
			//int idx2=baseHtml.indexOf("onscroll='dismiss_menu();'>",idx1);
			String f1 = baseHtml.substring(0,idx1+insertsionPoint.length()+1);
			//String f2 = baseHtml.substring(baseHtml.indexOf("}",idx1+insertsionPoint.length()),idx2);
			//String f3 = baseHtml.substring(baseHtml.indexOf("</div>",idx2));
			restFragments = baseHtml.substring(baseHtml.indexOf("/**/}",idx1+insertsionPoint.length()));
			derivedHtmlBase = new StringBuilder(f1);
			//SU.Log("f1"+f1);
			derBaseLen = f1.length();
		}
		derivedHtmlBase.setLength(derBaseLen);
		if(opt.isCombinedSearching()){
			derivedHtmlBase.append("document.getElementById('fileBtn').onclick();");
		}
		/** win10 ie 去掉\t会发生量子波动Bug */
		derivedHtmlBase.append("\teditText.value=\"").append(key.replace("\"", "\\\"")).append("\";");
		derivedHtmlBase.append("loookup();");
		if(iframes!=null){
			derivedHtmlBase.append("handleMirror('"+iframes.replace("\r","\\r")+"');");
		}
		//SU.Log("pengDingPos"+pos);
		derivedHtmlBase.append(restFragments);
		//SU.Log(derivedHtmlBase.toString());
		return derivedHtmlBase.toString();
	}
	
	private String getBaseHtml() {
		if(true || baseHtml==null) {//rrr
			try {
				SU.Log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
				InputStream fin = OpenMdbResourceByName("\\mdict_browser.html");
				byte[] data = new byte[fin.available()];
				fin.read(data);
				baseHtml = new String(data);
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return baseHtml;
	}
}
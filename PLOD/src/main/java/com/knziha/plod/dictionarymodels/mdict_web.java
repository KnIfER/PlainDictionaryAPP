package com.knziha.plod.dictionarymodels;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymanager.files.CachedDirectory;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.X509TrustManager;

import db.MdxDBHelper;

import static com.knziha.plod.dictionarymodels.mdict_transient.goodNull;

/*
 Mdict point to online website.
 date:2019.11.28
 author:KnIfER
*/
public class mdict_web extends mdict {
	String host;
	String hostName;
	String index;
	String jsLoader;
	String onstart;
	String onload;
	String search;
	String currentUrl;
	boolean abSearch;
	boolean excludeAll;
	public boolean canExcludeUrl;
	public boolean canSaveResource;
	public boolean noImage;
	public boolean computerFace;
	//public boolean banJs;
	//public boolean reEnableJs;
	/** 缓存的关键词 */
	ArrayList<String> searchKeys = new ArrayList<>();
	/** Json中的入口点 */
	ArrayList<String> entrance = new ArrayList<>();
	/** for excludeAll */
	HashSet<String> hosts=new HashSet<>();
	/** for SVG */
	public String[] svgKeywords;
	/** 兴趣文件后缀 */
	public String[] cacheExtensions;
	/** 无兴趣文件后缀(仅针对数据库) */
	public String[] cleanExtensions;
	private static final String[] default_cleanExtensions = new String[]{".jpg",".png",".gif",".mp4",".mp3",".m3u8",".json",".js",".css"};
	public Pattern keyPattern;

	/**
	 	const w=window;
		if(!w.PLODKit){
	 	const style = document.createElement("style");
		style.class = '_PDict';
		style.innerHTML = "mark{background:yellow;}mark.current{background:orange;}";
		document.head.appendChild(style);
	 	w.addEventListener('load',wrappedOnLoadFunc,false);
		w.addEventListener('click',wrappedClickFunc);
		w.addEventListener('touchstart',wrappedOnDownFunc);
		function wrappedOnLoadFunc(){
			//console.log('mdpage loaded');
			document.body.contentEditable=!1;
		}
		function wrappedOnDownFunc(e){
	 		if(e.touches.length==1){
	 			w._touchtarget = e.touches[0].target;
	 		}
			//console.log('fatal wrappedOnDownFunc ' +w._touchtarget);
		}
		function selectTouchtarget(){
	 		var tt = w._touchtarget;
	 		if(tt){
	 			w._touchtarget_href = tt.getAttribute("href");
	 			tt.removeAttribute("href");
				var sel = w.getSelection();
				var range = document.createRange();
				range.selectNodeContents(tt);
				sel.removeAllRanges();
				sel.addRange(range);
	 		}
		}
		function restoreTouchtarget(){
	 		var tt = w._touchtarget;
	 		if(tt){
	 			tt.setAttribute("href", w._touchtarget_href);
	 		}
		}
	 	function wrappedClickFunc(e){
	 		console.log('fatal wrappedClickFunc'+e);
	 		var curr=e.srcElement;
			if(curr!=document.documentElement  && e.srcElement.nodeName!='INPUT' && w.rcsp&0x20){
				var s = w.getSelection();
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

							s.modify('move', 'backward', 'word'); // now could noway be next line
							s.modify('extend', 'forward', 'word');

							if(s.getRangeAt(0).endContainer===range.endContainer&&s.getRangeAt(0).endOffset===range.endOffset){
								// for word made up of multiple character
								text=s.toString();
							}

							console.log(text); // final output
							if(app)app.popupWord(text, e.clientX, e.screenY, w.frameAt);
						}
					}
					s.empty();
				}
			}
	 	}

		//!!!高亮开始
		w.bOnceHighlighted;
		w.MarkLoad;
		w.MarkInst;
		w.results=[];
	 	w.current;
	 	w.currentIndex = 0;
		w.currentClass = "current";
	 	w.frameAt;

		w.jumpTo = function(d, desiredOffset, frameAt, HlightIdx, reset, topOffset_frameAt) {
			if (w.results.length) {
	 			if(reset) resetLight(d);
				//console.log('jumpTo received reset='+reset+' '+frameAt+'->'+HlightIdx+' '+(w.currentIndex+d)+'/'+(w.results.length)+' dir='+d);
				var np=w.currentIndex+d;
				var max=w.results.length - 1;
				if (w.currentIndex > max) w.currentIndex=0;
				if(desiredOffset>=0){
					np=0;
					if(frameAt<HlightIdx) return d;
					var baseOffset=topOffset_frameAt;
					for(;np>=0&&np<=max;np+=d){
						if(baseOffset+pw_topOffset(w.results[np])>=desiredOffset)
							break;
					}
					desiredOffset=-1;
				}
				if (np < 0) return -1;
				if (np > max) return 1;
				w.currentIndex=np;
				if(w.current) removeClass(w.current, w.currentClass);
				w.current = w.results[w.currentIndex];
				if(w.current){
					addClass(w.current, w.currentClass);
					var position = topOffset(w.current);
	 				app.scrollHighlight(position, d);
	 				return ''+w.currentIndex;
				}
			}
			return d;
	 	};

		w.pw_topOffset = function(value){
			var top=0;
			while(value && value!=document.body){
				top+=value.offsetTop;
				value=value.offsetParent;
			}
			return top;
		};

		 w.topOffset = function(elem){
			var top=0;
			var add=1;
			while(elem && elem!=document.body){
				if(elem.style.display=='none' || elem.style.display=='' && document.defaultView.getComputedStyle(elem,null).display=='none'){
					elem.style.display='block';
				}
				if(add){
					top+=elem.offsetTop;
					var tmp = elem.offsetParent;
					if(!tmp) add=0;
					else elem=tmp;
				}
				if(!add) elem=elem.parentNode;
			}
			return !add&&top==0?-1:top;
		};

		w.quenchLight = function (){
			if(w.current) removeClass(w.current, w.currentClass);
		};

		w.resetLight = function (d){
			if(d==1) w.currentIndex=-1;
			else if(d==-1) w.currentIndex=w.results.length;
			quenchLight();
		};

		w.setAsEndLight = function (){
			w.currentIndex=w.results.length-1;
		};

		w.setAsStartLight = function(){
			w.currentIndex=0;
		};

		w.addClass = function(elem, className) {
			if (!className) return;
			const els = Array.isArray(elem) ? elem : [elem];
			for(var i=0;i<els.length;i++){
				els[i].className+=className;
			}
		};

		 w.removeClass = function(elem, className) {
			if (!className) return;
			const els = Array.isArray(elem) ? elem : [elem];
			for(var i=0;i<els.length;i++){
				els[i].className=els[i].className.replace(className, '');
			}
		};

		w.clearHighlights = function (){
			if(w.bOnceHighlighted && w.MarkInst && w.MarkLoad)
			w.MarkInst.unmark({
				done: function() {
					w.results=[];
					w.bOnceHighlighted=false;
				}
			});
		};
		w.highlight = function(keyword){
			var b1=keyword==null;
			if(b1)
				keyword=app.getCurrentPageKey();
			if(keyword==null||b1&&keyword.trim().length==0)
				return;
			if(!w.MarkLoad){
				loadJs('mdbr://mark.js', function(){
					w.MarkLoad=true;
					do_highlight(keyword);
				});
			}else
				do_highlight(keyword);
		};
	 	w.do_highlight = function(keyword){
			if(!w.MarkInst)
				w.MarkInst = new Mark(document);
	 		w.bOnceHighlighted=false;
			w.MarkInst.unmark({
				done: function() {
	 				var rcsp=w.rcsp;
					keyword=decodeURIComponent(keyword);
	 				console.log('highlighting...'+keyword+((rcsp&0x1)!=0));
	 				if(rcsp&0x1)
					w.MarkInst.markRegExp(new RegExp(keyword, (rcsp&0x2)?'m':'im'), {
						done: done_highlight
					});
					else
					w.MarkInst.mark(keyword, {
						separateWordSearch: (rcsp&0x4)!=0,'wildcards':(rcsp&0x10)?(rcsp&0x8)?'enabled':'withSpaces':'disabled',done: done_highlight,
						caseSensitive:(rcsp&0x2)!=0
					});
				}
			});
		};

		w.done_highlight = function(){
			 w.bOnceHighlighted=true;
			 w.results = document.getElementsByTagName("mark");
			 w.currentIndex=-1;
			 if(app) app.onHighlightReady(w.frameAt, w.results.length);
		 };

		w.loadJs = function(url,callback){
			var script=document.createElement('script');
			script.type="text/javascript";
			if(typeof(callback)!="undefined"){
				if(script.readyState){
					script.onreadystatechange=function(){
						if(script.readyState == "loaded" || script.readyState == "complete"){
							script.onreadystatechange=null;
							callback();
						}
					}
				}else{
					script.onload=function(){
						callback();
					}
				}
			}
			script.src=url;
			document.body.appendChild(script);
		};
		w.PLODKit=1;
		}
		w.highlight(null);
	 */
	@Multiline(trim = false)
	public final static String js="SUB PAGE";
	private final JSONObject website;
	private ObjectAnimator progressProceed;
	private ObjectAnimator progressTransient;
	private boolean isListDirty;

	//构造
	public mdict_web(String fn, MainActivityUIBase _a) throws IOException {
		super(goodNull(fn), _a);
		a=_a;
		opt=a.opt;
		website = JSONObject.parseObject(BU.fileToString(f));
		String _host = website.getString("host");
		if(_host==null) _host=getRandomHost();
		if(_host.endsWith("/")) _host=_host.substring(0, _host.length()-1);
		host=_host;
		index = website.getString("index");
		if(index==null) index="";
		jsLoader = website.getString("js");
		onstart = website.getString("onstart");
		onload = website.getString("onload");
		search = website.getString("search");
		abSearch = search!=null&&search.startsWith("http");
		excludeAll = website.getBooleanValue("excludeAll");
		String _extensions  = website.getString("cacheRes");
		String _exclude_db_save_extensions  = website.getString("CDROPT");
		noImage = website.getBooleanValue("noImage");
		computerFace = website.getBooleanValue("cpau");
		String svg = website.getString("svg");
		String _keyPattern = website.getString("keyPattern");
		if(_keyPattern!=null){
			try {
				keyPattern = Pattern.compile(_keyPattern);
			} catch (Exception e) { /*CMN.Log(e);*/ }
		}
		/*CMN.Log("_keyPattern", _keyPattern, keyPattern);*/
		//banJs = json.getBooleanValue("banJs");
		//reEnableJs = json.getBooleanValue("reEnableJs");
		String _entrance = website.getString("entrance");
		if(_entrance!=null){
			entrance = new ArrayList<>(Arrays.asList(_entrance.split("\n")));
		}
		_Dictionary_fName=new File(fn).getName();
		_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
		_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");

		htmlBuilder=new StringBuilder();

		readInConfigs(false);

		if(bgColor==null)
			bgColor= CMN.GlobalPageBackground;

		if(excludeAll){
			canExcludeUrl=true;
			hosts.add(host);
			for(String sI:entrance){
				int idx = sI.indexOf("://");
				idx++; if(idx>0) idx+=2;
				idx = sI.indexOf("/", idx);
				if(idx>0)
					sI = sI.substring(0, idx);
				//CMN.Log("hosts :: ", sI);
				hosts.add(sI);
			}
		}

		bNeedCheckSavePathName=true;

		if(_extensions!=null){
			cacheExtensions = _extensions.split("\\|");
			canSaveResource=true;
			InternalResourcePath = new CachedDirectory(opt.pathToDatabases().append(_Dictionary_fName_Internal).append("/Caches/").toString());
			if(!InternalResourcePath.exists()) InternalResourcePath.mkdirs();
		}

		if(_exclude_db_save_extensions!=null){
			if(_exclude_db_save_extensions.equalsIgnoreCase("All")){
				cleanExtensions = default_cleanExtensions;
			} else {
				cleanExtensions = _exclude_db_save_extensions.split("\\|");
			}
		}

		if(svg!=null){
			svgKeywords = svg.split("\r");
		}

		int idx = host.indexOf("://");
		hostName = idx>0?host.substring(idx+3):host;
		idx = host.indexOf("/");
		if(idx>0) hostName = host.substring(0, idx);
	}

	//粗暴地排除
	public boolean shouldExcludeUrl(String url){
		if(hosts==null)
			return false;
		int idx = url.indexOf("://");
		idx++; if(idx>0) idx+=2;
		idx = url.indexOf("/", idx);
		if(idx>0)
			url = url.substring(0, idx);
		CMN.Log("shouldExcludeUrl :: ", url, !hosts.contains(url));
		return !hosts.contains(url);
	}

	/** 简便起见，直接将修改的配置存于HashMap */
	@Override
	public void readInConfigs(boolean check) throws IOException {
		if(!check){
			firstFlag = website.getLongValue("MFF");
			if(website.containsKey("BG"))
				bgColor = website.getIntValue("BG");
			internalScaleLevel = website.getIntValue("TextZoom");

			lvPos = website.getIntValue("lvPos");
			lvClickPos = website.getIntValue("lvClk");
			lvPosOff = website.getIntValue("lvOff");

			initArgs = new int[]{website.getIntValue("argx"), website.getIntValue("argy")};
			webScale = website.getIntValue("args");
		}

		if(opt.ChangedMap!=null){
			Long newVal = opt.ChangedMap.remove(getPath());
			if(newVal!=null && newVal!=firstFlag){
				website.put("MFF", firstFlag = newVal);
				CMN.Log("保存！！！"+this);
				dumpViewStates();
				refresh_eidt_kit(getContentEditable(), getEditingContents(), true);
			}
		}
	}

	@Override
	protected void WriteConfigFF() {
		dumpViewStates();
	}

	/** 保存网站定义 */
	@Override
	public void dumpViewStates() {
		try {
			website.put("BG", bgColor);
			website.put("TextZoom", internalScaleLevel);
			website.put("lvPos", lvPos);
			website.put("lvClk", lvClickPos);
			website.put("lvOff", lvPosOff);
			int ex=0,e=0;
			if(viewsHolderReady && mWebView!=null) {
				ex=mWebView.getScrollX();
				e=mWebView.getScrollY();
			}else if(initArgs!=null && initArgs.length==2){
				ex=initArgs[0];
				e=initArgs[1];
			}
			website.put("argx", ex);
			website.put("argy", e);
			website.put("args", webScale);
			website.put("MFF", firstFlag);
			if(entrance.size()>0)
				website.put("entrance", StringUtils.join(entrance, '\n'));
			if(cacheExtensions!=null)
				website.put("cacheRes", StringUtils.join(cacheExtensions, '|'));
			//if(cleanExtensions!=null)
			//	website.put("CDROPT", StringUtils.join(cleanExtensions, '|'));

			FileOutputStream fo = new FileOutputStream(f);
			String v=website.toString();
			v=v.replace("\\n", "\n");
			fo.write(v.getBytes());
			fo.flush();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Todo let's make dice
	String getRandomHost() {
		return "";
	}

	@Override
	protected void initLogically() {
		_num_record_blocks=-1;
		String fn = (String) SU.UniversalObject;
		fn = new File(fn).getAbsolutePath();
		f = new File(fn);
		_Dictionary_fName = f.getName();
	}

	@Override
	protected String getSaveUrl(WebViewmy mWebView) {
		String url=mWebView.getUrl();
		if (url!=null && url.startsWith(host))
			url = url.substring(host.length());
		return url;
	}

	@Override
	protected void onPageSaved() {
		super.onPageSaved();
		a.notifyDictionaryDatabaseChanged(mdict_web.this);
	}

	@Override
	protected String getSaveNameForUrl(String finalUrl) {
		boolean needTrim=!finalUrl.contains(".php");//动态资源需要保留参数
		int start = 0;
		int end = needTrim?finalUrl.indexOf("?"):finalUrl.length();
		if(end<0) end=finalUrl.length();
		String name=finalUrl.substring(start, end);
		name=URLDecoder.decode(name);
		if(!needTrim) name=name.replaceAll("[=?&|:*<>]", "_");
		if(name.length()>64){
			name=name.substring(0, 56)+"_"+name.length()+"_"+name.hashCode();
		}
		return name;
	}

	public long saveCurrentUrl(MdxDBHelper con, String url) {
		if(url!=null){
			con.enssureUrlTable();
			if(url.startsWith(host))
				url=url.substring(host.length());
			if(url.length()>0) {
				CMN.Log("saveCurrentUrl", url);
				return con.putUrl(currentDisplaying, url);
			}
		}
		return -1;
	}

	public boolean containsCurrentUrl(MdxDBHelper con,String url) {
		con.enssureUrlTable();
		if(url.startsWith(host))
			url=url.substring(host.length());
		CMN.Log("containsCurrentUrl", url, con.containsUrl(url));
		return con.containsUrl(url);
	}

	public boolean removeCurrentUrl(MdxDBHelper con,String url) {
		con.enssureUrlTable();
		if(url.startsWith(host))
			url=url.substring(host.length());
		return con.removeUrl(url)!=-1;
	}

	@Override
	String getSimplestInjection() {
		return js;
	}

	/*** Page constituents:<br/>
	 * 1. Index <br/>
	 * 2~n. Search History [Optional] Extra entrances [Optional]<br/>
	 * n~m. Book marks [not implemented]<br/>
	 * m~x. Page overrides [Optional]<br/>
	 */
	@Override
	public long getNumberEntries() {
		long size =  1+searchKeys.size()+entrance.size();
		getCon(false);
		if(con!=null && PageCursor==null){
			con.enssurePageTable();
			PageCursor = con.getDB().rawQuery("select * from t2 ", null);
		}
		if(PageCursor!=null){
			size += PageCursor.getCount();
		}
		return size;
	}

	@Override
	public void renderContentAt(float initialScale, int SelfIdx, int frameAt, WebViewmy mWebView, int...position) {
		if(mWebView==null)
			mWebView=this.mWebView;
		boolean resposibleForThisWeb = mWebView==this.mWebView;
		if(SelfIdx!=-1){
			preloading(mWebView, frameAt, SelfIdx, position);
			if(resposibleForThisWeb && mWebView.fromCombined==1  && PDICMainAppOptions.getTmpIsCollapsed(tmpIsFlag)){/* 自动折叠 */
				mWebView.awaiting = true;
				mWebView.setVisibility(View.GONE);
				return;
			}
		}
		mWebView.fromCombined=4;
		String key = null;
		String url=null;
		int pos=mWebView.currentRendring[0];
		if(pos>0) {
			pos-=1;
			if(pos<searchKeys.size()){
				if(search!=null){
					key=searchKeys.get(pos);
				}
			}
			pos-=searchKeys.size();
			//CMN.Log(pos, entrance.size(), entrance.get(0));
			if(key==null && entrance.size()>0){
				if(pos>=0 && pos<entrance.size()){
					url = entrance.get(pos);
					String[] arr = url.split("\r");
					if(arr.length>1){
						url = arr[0];
					}
					if(!url.contains("://"))
						url=host+url;
				}
			}
			pos-=entrance.size();
			if (url==null && PageCursor != null) {
				if (pos>=0 && pos < PageCursor.getCount()) {
					PageCursor.moveToPosition(pos);
					url = PageCursor.getString(0);
					CMN.Log("数据库取出的数据：", URLDecoder.decode(url));
					if(!url.contains("://"))
						url=host+url;
					if(resposibleForThisWeb)
						toolbar_title.setText(PageCursor.getString(1));
				}
				pos -= PageCursor.getCount();
			}
		}
		else{
			if(searchKey!=null) {
				if (searchKey.startsWith("http"))
					url = searchKey;
				else if (search != null) {
					key = searchKey;
					/* 接管网页历史纪录 */
					searchKeys.remove(key);
					searchKeys.add(0, key);
					a.adaptermy.notifyDataSetChanged();
					searchKey = null;
				}
			}
		}
		if(url==null){
			if(key!=null && search!=null){
				if(search.contains("%s"))
					url=search.replaceAll("%s", key);
				else
					url=search+key;
				if(!abSearch)
					url=host+url;
				if(resposibleForThisWeb)
					toolbar_title.setText(key);
			}else{
				url=host+index;
			}
		}
		if(mWebView==this.mWebView) {
			if(a.opt.getHideScroll1()&&resposibleForThisWeb)
				a.mBar.setVisibility(View.GONE);
			else {
				a.mBar.setDelimiter("< >", mWebView);
			}
		}

		mWebView.loadUrl(url);
	}

	@Override
	public void initViewsHolder(MainActivityUIBase a) {
		super.initViewsHolder(a);
		mWebView.addJavascriptInterface(mWebBridge, "app");
		View.OnClickListener voyager = v -> {
			int isRecess = v.getId()==R.id.recess?-1:1;
			if (mWebView.canGoBackOrForward(isRecess)) {
				preloading(mWebView, -1, -1, null);
				mWebView.goBackOrForward(isRecess);
			}
		};
		recess.setOnClickListener(voyager);
		forward.setOnClickListener(voyager);
		setIcSaveLongClickListener(ic_save);
		setWebLongClickListener(mWebView, a);
		if(computerFace)
			mWebView.getSettings().setUserAgentString("PC");
	}

	@Override
	public void setIcSaveLongClickListener(View v) {
		if(savelcl == null){
			savelcl = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					WebViewmy _mWebView = mWebView;
					if(v!=ic_save){
						if(a.PeruseView!=null){
							_mWebView = a.PeruseView.mWebView;
						} else {
							return true;
						}
					}
					String url = _mWebView.getUrl();
					WebViewmy final_mWebView = _mWebView;
					AlertDialog.Builder builder3 = new AlertDialog.Builder(a);
					builder3.setSingleChoiceItems(new String[] {}, 0,
							(dialog, pos) -> {
								switch(pos) {
									case 0:{//查看原网页
										final_mWebView.setTag(R.id.save, false);
										final_mWebView.reload();
									} break;
									case 1:{//删除重载页面
										getCon(true).enssurePageTable();
										String _url = final_mWebView.getUrl();
										if(_url!=null){
											if(_url.startsWith(host))
												_url = _url.substring(host.length());
											con.removePage(_url);
											if(PageCursor!=null)
												PageCursor.close();
											PageCursor = con.getPageCursor();
											a.notifyDictionaryDatabaseChanged(mdict_web.this);
										}
										final_mWebView.reload();
									} break;
									case 2:{//新增入口
										String _url = final_mWebView.getUrl();
										if(_url!=null){
											String uI;
											for (int i = 0; i < entrance.size(); i++) {
												uI=entrance.get(i);
												if(uI.startsWith(_url)){
													if(uI.length()==_url.length()||uI.charAt(_url.length())=='\r'){
														entrance.remove(i);
														//TODO 保存配置
														break;
													}
												}
											}
											entrance.remove(_url);
											entrance.add(0, _url+"\r"+ final_mWebView.getTitle());
											a.notifyDictionaryDatabaseChanged(mdict_web.this);
										}
									} break;
								}
								dialog.dismiss();
							});
					SpannableStringBuilder ssb = new SpannableStringBuilder("页面选项");
					int start = ssb.length();

					ssb.append(url);
					int end=ssb.length();

					ssb.setSpan(new RelativeSizeSpan(0.63f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					ssb.setSpan(new ClickableSpan() {
						@Override
						public void onClick(@NonNull View widget) {//打开链接
							AlertDialog.Builder builder3 = new AlertDialog.Builder(a);
							builder3.setTitle("更多选项");
							builder3.setSingleChoiceItems(new String[]{}, 0,
									(dialog, pos) -> {
										switch (pos) {
											case 0: {//保存网页源代码
												final_mWebView.evaluateJavascript(preview_js, v1 -> {
													v1 =StringEscapeUtils.unescapeJava(v1.substring(1, v1.length()-1));
													v1 =RemoveApplicationTags(v1);
													StringBuffer sb = opt.pathToMainFolder().append("downloads/").append(final_mWebView.word)
															.append(".");
													int len = sb.length();
													int cc=0;
													sb.append("html");
													while(new File(sb.toString()).exists()){
														sb.setLength(len);
														sb.append(IU.a2r(++cc)).append(".html");
													}
													BU.printFile(v1.getBytes(), sb.toString());
													a.showT(sb.append(" 已保存! "));
												});
											}
											break;
											case 1: {//浏览器中打开
												try {
													Intent intent = new Intent();
													intent.setData(Uri.parse(url));
													intent.setAction(Intent.ACTION_VIEW);
													a.startActivity(intent);
												} catch (Exception e) { }
											}
											break;
											case 2: {
											}
											break;
											case 3://词典设置
											break;
										}
									});
							String[] Menus = a.getResources().getStringArray(
									R.array.lexical_page_url_options2);
							List<String> arrMenu = Arrays.asList(Menus);
							AlertDialog dd = builder3.create();
							dd.show();

							dd.getListView().setAdapter(new ArrayAdapter<>(a,
									R.layout.singlechoice_plain, android.R.id.text1, arrMenu));
						}
					}, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					builder3.setTitle(ssb);

					String[] Menus = a.getResources().getStringArray(
							R.array.config_web_pages);
					List<String> arrMenu = Arrays.asList(Menus);
					AlertDialog dd = builder3.create();
					dd.show();
					dd.setOnDismissListener(dialog -> {
					});
					dd.getListView().setAdapter(new ArrayAdapter<>(a,
							R.layout.singlechoice_plain, android.R.id.text1, arrMenu) );

					TextView titleView = dd.getWindow().getDecorView().findViewById(R.id.alertTitle);
					titleView.setSingleLine(false);
					titleView.setMovementMethod(LinkMovementMethod.getInstance());
					return false;
				}
			};
		}
		v.setOnLongClickListener(savelcl);
	}

	public void searchFor(String key) {
		String url=host+index;
		if(search!=null){
			if(search.contains("%s"))
				url+=search.replaceAll("%s", key);
			else url=url+search+key;
		}
		mWebView.loadUrl(url);
	}

	public void preloading(WebViewmy mWebView, int frameAt, int SelfIdx, int[] position) {
		currentDisplaying="";
		if(frameAt!=-1 || SelfIdx!=-1){
			mWebView.stopLoading();
			mWebView.setTag(mWebView.SelfIdx=SelfIdx);
			mWebView.frameAt=frameAt;
			mWebView.currentRendring = position;
			if(mWebView.wvclient!=a.myWebClient) {
				mWebView.setWebChromeClient(a.myWebCClient);
				mWebView.setWebViewClient(a.myWebClient);
			}
			if(mWebView.getTag(R.drawable.popup_background)!=f){
				WebSettings settings = mWebView.getSettings();
				if (Build.VERSION.SDK_INT >= 21) {
					settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
				}
				settings.setBlockNetworkImage(noImage);
				//settings.setJavaScriptEnabled(!banJs);
				mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
				mWebView.setTag(R.drawable.popup_background, f);
			}
		}
		if(mWebView.fromCombined!=2)
			a.checkW10_full();
	}

	@Override
	public String getEntryAt(int position) {
		if(position==0)
			return "index";
		position-=1;
		try {
			if(position>=0 && position<searchKeys.size())
				return searchKeys.get(position);
			position-=searchKeys.size();
			if(entrance.size()>0){
				if(position>=0 && position<entrance.size()){
					String url = entrance.get(position);
					String[] arr = url.split("\r");
					if(arr.length>1){
						return arr[1];
					}
					return url;
				}
			}
			position-=entrance.size();
			if(PageCursor!=null){
				if(position>=0 && position<PageCursor.getCount()){
					PageCursor.moveToPosition(position);
					return PageCursor.getString(1);
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
			return "Error!!!"+e;
		}
		return "Error!!!";
	}

	public boolean hasCover() {
		return false;
	}

	public void onPageStarted(WebView view, String url, boolean updateTitle) {
		if(view==this.mWebView){
			if(updateTitle)
				toolbar_title.setText(currentDisplaying=view.getTitle());
			if(progressProceed !=null) {
				progressProceed.cancel();
			}
			toolbar.getBackground().setLevel(0);
			((LayerDrawable)toolbar.getBackground()).getDrawable(1).setAlpha(255);
			onProgressChanged(mWebView, 5);
		}
		currentUrl=view.getUrl();
		if(jsLoader!=null || onstart!=null) {
			String jsCode = "window.rcsp=" + MakeRCSP(opt) + ";";
			if (jsLoader != null) jsCode += jsLoader;
			if (onstart != null) jsCode += onstart;
			view.evaluateJavascript(jsCode, null);
		}
		CMN.Log("onPageStarted\n\nonPageStarted -- ", url);CMN.rt();CMN.stst_add=0;
	}

	public void onProgressChanged(WebViewmy view, int newProgress) {
		if(view == mWebView){
			Drawable d = toolbar.getBackground().mutate();
			if(progressProceed !=null) {
				progressProceed.cancel();
				progressProceed.setIntValues(d.getLevel(), newProgress*100);
			} else {
				progressProceed = ObjectAnimator.ofInt(d,"level", d.getLevel(), newProgress*100);
				progressProceed.setDuration(100);
			}
			progressProceed.start();
		}
		if(newProgress>=99){
			fadeOutProgressbar(view, false);
		}
	}

	/** 接管进入黑暗模式、编辑模式 */
	public void onPageFinished(WebView view, String url, boolean updateTitle) {
		//CMN.Log("chromium", "web  - onPageFinished", currentUrl);
		fadeOutProgressbar(view, updateTitle);
		currentUrl=view.getUrl();
		String jsCode = "window.rcsp=" + MakeRCSP(opt) + ";" + js;
		if (jsLoader != null) jsCode += jsLoader;
		if (onload != null) jsCode += onload;
		if (onstart != null) jsCode += onstart;
		if(GlobalOptions.isDark) jsCode+=MainActivityUIBase.DarkModeIncantation;
		if(getContentEditable() && getEditingContents() && view!=a.popupWebView) jsCode+=MainActivityUIBase.ce_on;
		view.evaluateJavascript(jsCode, null);
	}

	private void fadeOutProgressbar(WebView view, boolean updateTitle) {
		if(view==this.mWebView){
			if(updateTitle) toolbar_title.setText(((WebViewmy)view).word=currentDisplaying=view.getTitle());
			Drawable d = ((LayerDrawable) toolbar.getBackground()).getDrawable(1);
			if(progressTransient!=null){
				progressTransient.cancel();
				progressTransient.setFloatValues(d.getAlpha(), 0);
			} else {
				progressTransient = ObjectAnimator.ofInt(d, "alpha", d.getAlpha(), 0);
				progressTransient.setDuration(200);
			}
			progressTransient.start();
		}
	}

	@Override
	public String getLexicalEntryAt(int position) {
		return "第"+position+"页";
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
		return "";
	}

	@Override
	public InputStream getResourceByKey(String key) {
		return null;
	}

	@Override
	public boolean hasMdd() {
		return true;
	}

	@Override
	protected InputStream mOpenInputStream(){
		return null;
	}

	@Override
	protected boolean StreamAvailable() {
		return false;
	}

	/** combined search page constituents :
	 * from key word [just one,
	 * 		and should be ( I. during combined search mode ) added to searchKeys after you've clicked that entry.
	 * 		or it's displaying contents directly and meanwhile 'enter' is typed.
	 * 		( II. during single search mode ) added to searchKeys after typing 'enter'.
	 * 		] ;
	 *
	 * from mimicking others' results [multiple. ]*/
	@Override
	public void size_confined_lookUp5(String keyword, RBTree_additive combining_search_tree, int SelfAtIdx, int theta) {
		//searchKeys.remove(keyword);
		//searchKeys.add(0, keyword);
		ArrayList<myCpr<String, Integer>> _combining_search_list = combining_search_list;
		if(combining_search_tree!=null)
			combining_search_tree.insert(keyword,SelfAtIdx,0);
		else
			_combining_search_list.add(new myCpr(keyword,0));
		isListDirty=true;
	}

	@Override
	public void flowerFindAllContents(String key, int selfAtIdx, AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException {
	}

	@Override
	public void flowerFindAllKeys(String key, int SelfAtIdx, AbsAdvancedSearchLogicLayer SearchLauncher) {
	}

	public InputStream getPage(String url) {
		if(getContentEditable()){
			if(url.startsWith(host))
				url=url.substring(host.length());
			getCon(true).enssurePageTable();
			return con.getPageStream(url);
		}
		return null;
	}

	@Override
	public CachedDirectory getInternalResourcePath(boolean create) {
		return InternalResourcePath;
	}

	@Override
	public String getAboutString() {
		return _Dictionary_fName+"<br>"+website.toString();
	}

	/** Accept given key keyword or not. */
	public boolean takeWord(String key) {
		if(keyPattern!=null){
			return keyPattern.matcher(key).find();
		}
		return false;
	}

	public static class MyX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate certificates[], String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] ax509certificate,String s) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[] {};
		}
	}
}

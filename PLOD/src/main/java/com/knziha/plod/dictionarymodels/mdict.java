package com.knziha.plod.dictionarymodels;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;

import com.alibaba.fastjson.JSONObject;
import com.knziha.filepicker.utils.FU;
import com.knziha.plod.PlainDict.AgentApplication;
import com.knziha.plod.PlainDict.BasicAdapter;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.MainActivityUIBase.UniCoverClicker;
import com.knziha.plod.PlainDict.PDICMainActivity;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.PlaceHolder;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymanager.files.CachedDirectory;
import com.knziha.plod.settings.DictOpitonContainer;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.widgets.AdvancedNestScrollLinerView;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.widgets.XYTouchRecorder;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import db.MdxDBHelper;

/*
 UI side of mdict
 date:2018.07.30
 author:KnIfER
*/
public class mdict extends com.knziha.plod.dictionary.mdict
		implements ValueCallback<String>, OnClickListener
		, mdict_manageable
		{
	public final static String FileTag = "file://";
	public final static String baseUrl = "file:///";
	public final static String  _404 = "<span style='color:#ff0000;'>PlainDict 404 Error:</span> ";
	
	/**</style><script class="_PDict" src="mdbr://SUBPAGE.js"></script>*/
	@Multiline()
	public final static String js="SUBPAGE";
	
	/** const w=window;
	 	w.addEventListener('load',wrappedOnLoadFunc,false);
		w.addEventListener('click',wrappedClickFunc);
		w.addEventListener('touchstart',wrappedOnDownFunc);
		function wrappedOnLoadFunc(){
			var ws = w.document.body.style;
			console.log('mdpage loaded dark:'+(w.rcsp&0x40));
	 		document.body.contentEditable=!1;
	 		highlight(null);
			var vi = document.getElementsByTagName('video');
			for(var i=0;i<vi.length;i++){if(!vi[i]._fvwhl){vi[i].addEventListener("webkitfullscreenchange", wrappedFscrFunc, false);vi[i]._fvwhl=1;}}
		}
	 	function wrappedFscrFunc(e){
			//console.log('begin fullscreen!!!');
			var se = e.srcElement;
			//if(se.webkitDisplayingFullscreen&&app) app.onRequestFView(se.videoWidth, se.videoHeight);
			if(app)se.webkitDisplayingFullscreen?app.onRequestFView(se.videoWidth, se.videoHeight):app.onExitFView()
		}
		function wrappedOnDownFunc(e){
	 		//console.log('fatal wrappedOnDownFunc');
	 		if(!w._touchtarget_lck && e.touches.length==1){
	 			w._touchtarget = e.touches[0].target;
	 		}
			//console.log('fatal wrappedOnDownFunc' +w._touchtarget);
		}
		function selectTouchtarget(e){
	 		var ret = selectTouchtarget_internal(e);
			if(ret<=0||e==1) {
				w._touchtarget_lck=!1;
	 			w._touchtarget=null;
			}
	 		return ret;
		}
		function NoneSelectable(e){
	 		return getComputedStyle(e).userSelect=='none'
	 	}
		function selectTouchtarget_internal(e){
			w._touchtarget_lck=!0;
	 		var tt = w._touchtarget;
	 		var t0 = tt;
	 		if(tt){
				var fc = 0;
	 			tt.userSelect='text';
				while(tt.tagName!="A"&&(w==0||tt.tagName!="IMG")) {
					tt = tt.parentElement;
					if(tt==null||++fc>=9)return -3;
	 				tt.userSelect='text';
				}
	 			if(NoneSelectable(tt)) {
					var sty = document.createElement("style");
					sty.innerHTML = "*{user-select:text !important}";
					document.head.appendChild(sty);
					if(NoneSelectable(tt)) {
						return -1;
					}
				}
				if(fc>0) {
					w._touchtarget=tt;
				}
	 			if(e==0)w._touchtarget_href = tt.getAttribute("href");
				var sel = w.getSelection();
				var range = document.createRange();
				range.selectNodeContents(t0);
				sel.removeAllRanges();
				sel.addRange(range);
	 			var ret = sel.toString().length;
	 			if(e==0&&ret>0)tt.removeAttribute("href");
	 			return ret;
	 		}
	 		return -2;
		}
		function restoreTouchtarget(){
	 		var tt = w._touchtarget;
	 		if(tt){
	 			tt.setAttribute("href", w._touchtarget_href);
	 		}
	 		w._touchtarget_lck=!1;
		}
	 	function wrappedClickFunc(e){
	 		//console.log('fatal wrappedClickFunc');
	 		var curr=e.srcElement;
	 		if(w.webx){
				if(curr.tagName=='IMG'){
					var img=curr;
					if(img.src && !img.onclick && !(img.parentNode&&img.parentNode.tagName=="A")){
						var lst = [];
						var current=0;
						var all = document.getElementsByTagName("img");
						for(var i=0;i<all.length;i++){
							if(all[i].src){
								lst.push(all[i].src);
								if(all[i]==img)
									current=i;
							}
						}
						if(lst.length==0)
							lst.push(img.src);
						app.openImage(current, lst);
					}
				}
				else if(curr.tagName=='A'){
					//console.log('fatal in wcl : '+curr.href);
					var href=curr.href+'';
					if(curr.href && href.startsWith('file:///#')){
						curr.href='entry://#'+href.substring(9);
						return false;
					}
	 			}
			}
	 		console.log('popuping...');
	 		console.log(w.rcsp);
			if(curr!=document.documentElement && curr.nodeName!='INPUT' && curr.nodeName!='BUTTON' && w.rcsp&0x20 && !curr.noword){
				//todo document.activeElement.tagName
				var s = w.getSelection();
				if(s.isCollapsed && s.anchorNode){ // don't bother with user selection
					s.modify('extend', 'forward', 'word'); // first attempt
					var an=s.anchorNode;
					//console.log(s.anchorNode); console.log(s);
					//if(true) return;

					if(s.baseNode != document.body) {// immunize blank area
						var text=s.toString(); // for word made up of just one character
						var range = s.getRangeAt(0);
	 
						var rrect = range.getBoundingClientRect();
	 					var pX = rrect.x;
	 					var pY = rrect.y;
	 					var pW = rrect.width;
	 					var pH = rrect.height;
	 					var cprY = e.clientY;
	 					var cprX = e.clientX;

	 
						if(1||(cprY>pY-5 && cprY<pY+pH+5 && cprX>pX-5 && cprX<pX+pW+15)){
							s.collapseToStart();
							s.modify('extend', 'forward', 'lineboundary');
		 
							if(s.toString().length>=text.length){
								s.empty();
								s.addRange(range);
	
								s.modify('move', 'backward', 'word'); // now could noway be next line
								s.modify('extend', 'forward', 'word');
	
								var range1 = s.getRangeAt(0);
								if(range1.endContainer===range.endContainer&&range1.endOffset===range.endOffset){
									// for word made up of multiple character
									text=s.toString();
									rrect = range1.getBoundingClientRect();
									pX = rrect.x;
									pY = rrect.y;
									pW = rrect.width;
									pH = rrect.height;
								}
	 
								//网页内部的位置，与缩放无关
								//console.log(rrect);
								console.log(pX+' ~~ '+pY+' ~~ '+pW+' ~~ '+pH);
								console.log(cprX+' :: '+cprY);
	 
								console.log(text); // final output
								if(app){
									app.popupWord(text, frameAt, w.document.documentElement.scrollLeft+pX, w.document.documentElement.scrollTop+pY, pW, pH);
									w.popup=1;
									s.empty();
									return true;
								}
							}
	 					}
					}

					//点击空白关闭点译弹窗
					if(w.popup){
						app.popupClose();
						w.popup=0;
					}
					s.empty();
				}
			}
	 	}

		//!!!高亮开始
		var MarkLoad,MarkInst;
		var results=[], current,currentIndex = 0;
		var currentClass = "current";
	 	var frameAt;

		function jumpTo(d, desiredOffset, frameAt, HlightIdx, reset, topOffset_frameAt) {
			if (results.length) {
	 			if(reset) resetLight(d);
				//console.log('jumpTo received reset='+reset+' '+frameAt+'->'+HlightIdx+' '+(currentIndex+d)+'/'+(results.length)+' dir='+d);
				var np=currentIndex+d;
				var max=results.length - 1;
				if (currentIndex > max) currentIndex=0;
				if(desiredOffset>=0){
					np=0;
					if(frameAt<HlightIdx) return d;
					var baseOffset=topOffset_frameAt;
					for(;np>=0&&np<=max;np+=d){
						if(baseOffset+pw_topOffset(results[np])>=desiredOffset)
							break;
					}
					desiredOffset=-1;
				}
				if (np < 0) return -1;
				if (np > max) return 1;
				currentIndex=np;
				if(current) removeClass(current, currentClass);
				current = results[currentIndex];
				if(current){
					addClass(current, currentClass);
					var position = topOffset(current);
	 				app.scrollHighlight(position, d);
	 				return ''+currentIndex;
				}
			}
			return d;
	 	}

		function pw_topOffset(value){
			var top=0;
			while(value && value!=document.body){
				top+=value.offsetTop;
				value=value.offsetParent;
			}
			return top;
		}

		function topOffset(elem){
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
		}

		function quenchLight(){
			if(current) removeClass(current, currentClass);
		}

		function resetLight(d){
			if(d==1) currentIndex=-1;
			else if(d==-1) currentIndex=results.length;
			quenchLight();
		}

		function setAsEndLight(){
			currentIndex=results.length-1;
		}

		function setAsStartLight(){
			currentIndex=0;
		}

		function addClass(elem, className) {
			if (!className) return;
			const els = Array.isArray(elem) ? elem : [elem];
			for(var i=0;i<els.length;i++){
				els[i].className+=className;
			}
		}

		function removeClass(elem, className) {
			if (!className) return;
			const els = Array.isArray(elem) ? elem : [elem];
			for(var i=0;i<els.length;i++){
				els[i].className=els[i].className.replace(className, '');
			}
		}

		function clearHighlights(){
			if(w.bOnceHighlighted && MarkInst && MarkLoad)
			MarkInst.unmark({
				done: function() {
					results=[];
					w.bOnceHighlighted=false;
				}
			});
		}

		function highlight(keyword){
			var b1=keyword==null;
			if(b1)
				keyword=app.getCurrentPageKey();
			if(keyword==null||b1&&keyword.trim().length==0)
				return;
			if(!MarkLoad){
				loadJs('mdbr://mark.js', function(){
					MarkLoad=true;
					do_highlight(keyword);
				});
			}else
				do_highlight(keyword);
		}
		function do_highlight(keyword){
			if(!MarkInst)
				MarkInst = new Mark(document);
	 		w.bOnceHighlighted=false;
			MarkInst.unmark({
				done: function() {
	 				var rcsp=w.rcsp;
					keyword=decodeURIComponent(keyword);
	 				console.log('highlighting...'+keyword+((rcsp&0x1)!=0));
	 				if(rcsp&0x1)
					MarkInst.markRegExp(new RegExp(keyword, (rcsp&0x2)?'m':'im'), {
						done: done_highlight
					});
					else
					MarkInst.mark(keyword, {
						separateWordSearch: (rcsp&0x4)!=0,'wildcards':(rcsp&0x10)?(rcsp&0x8)?'enabled':'withSpaces':'disabled',done: done_highlight,
						caseSensitive:(rcsp&0x2)!=0
					});
				}
			});
		}

		 function done_highlight(){
			 w.bOnceHighlighted=true;
			 results = document.getElementsByTagName("mark");
			 currentIndex=-1;
			 if(app) app.onHighlightReady(frameAt, results.length);
		 }

		function loadJs(url,callback){
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
		}*/
	@Multiline()
	public final static byte[] jsBytes=SU.EmptyBytes;

	/**
	 	<script class="_PDict">
			window.addEventListener('click',wrappedClickFunc);
			function wrappedClickFunc(e){
				if(e.srcElement!=document.documentElement && e.srcElement.nodeName!='INPUT' && !curr.noword){
					var s = window.getSelection();
					if(s.isCollapsed && s.anchorNode){ // don't bother with user selection
						s.modify('extend', 'forward', 'word'); // first attempt
						var an=s.anchorNode;

						if(s.baseNode != document.body) {// immunize blank area
							var text=s.toString(); // for word made up of just one character
							if(text.length>0){
								var range = s.getRangeAt(0);

								s.collapseToStart();
								s.modify('extend', 'forward', 'lineboundary');

								if(s.toString().length>=text.length){
									s.empty();
									s.addRange(range);

									s.modify('move', 'backward', 'word'); // now could noway be next line
									s.modify('extend', 'forward', 'word');

									if(s.getRangeAt(0).endContainer===range.endContainer&&s.getRangeAt(0).endOffset===range.endOffset){
										// for word made up of just multiple character
										text=s.toString();
									}

									console.log(text); // final output
									if(app)app.popupWord(text, e.clientX, e.clientY, frameAt);
								}
							}
						}
						s.empty();
					}
				}
				return false;
			}
	 	</script>
	 */
	@Multiline
	public final static String SimplestInjection="SUBPAGE";

	/**
	 var styleObj= document.styleSheets[0].cssRules[3].style;
	 styleObj.setProperty("border", "1px solid #FF0000");
	 */
	@Multiline
	public final static String hl_border ="hl_border";

	//public String _Dictionary_fName_Internal;
	public WebViewmy mWebView;
    public ViewGroup rl;

	public Drawable cover;

	public static float def_zoom=1;
	public static int def_fontsize = 100;
	public static int optimal100;
	public int tmpIsFlag;
	long FFStamp;
	long firstFlag;
	public AppHandler mWebBridge;
	public SparseArray<ScrollerRecord> avoyager = new SparseArray<>();

	/**
	 	var saveTag=document.getElementsByTagName('PLODSAVE');
	 	if(saveTag.length==0){
	 		saveTag=document.createElement('PLODSAVE');
	 		document.body.appendChild(saveTag);
	 		saveTag.innerText='0';
	 	}else{
			saveTag=saveTag[0];
	 		try{
	 			saveTag.innerText=''+(1+parseInt(saveTag.innerText))
	 		}catch(e){saveTag.innerText='1'}
	 	}
	    saveTag.style.display='none';
	 	document.documentElement.outerHTML
	 */
	@Multiline
	public final static String save_js="ONSAVE";

	public final static String preview_js="document.documentElement.outerHTML";

	public String searchKey;
	protected Cursor PageCursor;
	protected CachedDirectory InternalResourcePath;
	protected boolean bNeedCheckSavePathName;
	public boolean isDirty;
	public boolean editingState=true;
	public static int _req_fvw;
	public static int _req_fvh;
	private CachedDirectory DataBasePath;
	
	@Override
	public int getCaseStrategy() {
		return (int) (firstFlag&3);
	}
	@Override
	public void setCaseStrategy(int val) {
		firstFlag&=~3;
		firstFlag|=val;
	}
	public boolean getIsolateImages(){
		return (firstFlag & 0x2) != 0;
	}
	public void setIsolateImages(boolean val){
		firstFlag&=~0x2;
		if(val) firstFlag|=0x2;
	}
	/** 内容可重载（检查数据库或文件系统中的重载内容） */
	public boolean getContentEditable(){
		return (firstFlag & 0x80) != 0;
	}
	public void setContentEditable(boolean val){
		firstFlag&=~0x80;
		if(val) firstFlag|=0x80;
	}
	/** 内容可编辑（处于编辑状态） */
	public boolean getEditingContents(){
		return (firstFlag & 0x100) != 0;
	}
	
	@Deprecated
	public void setIsDedicatedFilter(boolean val){
		firstFlag&=~0x40;
		if(val) firstFlag|=0x40;
	}
	
	public boolean getUseInternalBG(){
		return (firstFlag & 0x20) != 0;
	}
	public boolean getUseInternalFS(){
		return (firstFlag & 0x10) != 0;
	}
	public boolean getUseInternalTBG(){
		return (firstFlag & 0x200) != 0;
	}
	public boolean getOnlyContainsImg(){
		return (firstFlag & 0x800) != 0;
	}
	public boolean getEntryJumpList(){
		return (firstFlag & 0x40000) != 0;
	}
	
	public boolean getPopEntry(){
		return (firstFlag & 0x80000) != 0;
	}
	
//	public boolean getStarLevel(){
//		0x100000~0x400000
//	}
	
	public boolean getSavePageToDatabase(){
		return true;
	}
	public int bmCBI=0,bmCCI=-1;
	
	public PhotoBrowsingContext IBC = new PhotoBrowsingContext();
    public Integer bgColor=null;
    public int TIBGColor;
    public int TIFGColor;
			
	public final static String htmlBase="<!DOCTYPE html><html><meta name='viewport' content='initial-scale=1,user-scalable=yes' class=\"_PDict\"><head><style class=\"_PDict\">html,body{width:auto;height:auto;}img{max-width:100%;}mark{background:yellow;}mark.current{background:orange;border:0px solid #FF0000}";
	public final static String htmlHeadEndTag = "</head>";
	public final static String htmlEnd="</html>";

    public MainActivityUIBase a;
	protected PDICMainAppOptions opt;

	protected View.OnLongClickListener savelcl;

	//构造
	public mdict(@NonNull File fn, MainActivityUIBase _a, int pseudoInit, Object tag) throws IOException {
		super(fn, pseudoInit, _a==null?null:_a.MainStringBuilder, tag);
		if(_a!=null){
			a = _a;
			opt = _a.opt;
		}
		if(pseudoInit==1) {
			return;
		}
		
		//init(getStreamAt(0)); // MLSN
		
        File p = f.getParentFile();
        if(p!=null && p.exists()) {
			StringBuilder fName_builder = getCleanDictionaryNameBuilder();
			int bL = fName_builder.length();
			/* 外挂同名css */
			File externalFile = new File(p, fName_builder.append(".css").toString());
	        if(externalFile.exists()) {
	        	//todo 插入 同名 css 文件？
	        }
	        fName_builder.setLength(bL);
			externalFile = new File(p, fName_builder.append(".png").toString());
			/* 同名png图标 */
	        if(externalFile.exists()) {
	        	cover = Drawable.createFromPath(externalFile.getPath());
	        }
        }

        readInConfigs(a.UIProjects);
	}
	
	protected boolean viewsHolderReady =  false;
	public FlowTextView toolbar_title;
	View ic_undo;
	View ic_save;
	View ic_redo;
	ViewGroup toolbar;
	public View recess;
	public View forward;
	ImageView toolbar_cover;
	private UniCoverClicker ucc;
	public void initViewsHolder(final MainActivityUIBase a){
		if(!viewsHolderReady) {
			rl=(ViewGroup)a.getLayoutInflater().inflate(R.layout.contentview_item, a.webholder, false);
	        if(mWebView==null){
	        	webScale = def_zoom;
	           	AdvancedNestScrollWebView _mWebView = rl.findViewById(R.id.webviewmy);
				_mWebView.setNestedScrollingEnabled(PDICMainAppOptions.getEnableSuperImmersiveScrollMode());
				a.initWebScrollChanged();//Strategy: use one webscroll listener
				if(!(this instanceof mdict_pdf))
					_mWebView.setOnScrollChangedListener(a.onWebScrollChanged);
	            //_mWebView.setPadding(0, 0, 18, 0);
				mWebBridge = new AppHandler(this);
				_mWebView.addJavascriptInterface(mWebBridge, "app");
				mWebView = _mWebView;
	        }
			ic_undo=rl.findViewById(R.id.undo);
			ic_save=rl.findViewById(R.id.save);
			ic_redo=rl.findViewById(R.id.redo);
			refresh_eidt_kit(getContentEditable(), getEditingContents(), false);
			OnClickListener clicker = new OnClickListener(){
				@Override
				public void onClick(View v) {
					switch (v.getId()){
						case R.id.undo:
							mWebView.evaluateJavascript("document.execCommand('Undo')", null);
						break;
						case R.id.save:
							saveCurrentPage(mWebView);
						break;
						case R.id.redo:
							mWebView.evaluateJavascript("document.execCommand('Redo')", null);
						break;
					}
				}
			};
			ic_undo.setOnClickListener(clicker);
			ic_save.setOnClickListener(clicker);
			ic_redo.setOnClickListener(clicker);
			setSaveIconLongClick(ic_save);
			setWebLongClickListener(mWebView, a);

			toolbar = rl.findViewById(R.id.lltoolbar);
			mWebView.IBC = IBC;
			mWebView.titleBar = (AdvancedNestScrollLinerView) toolbar;
			mWebView.FindBGInTitle(toolbar);
			mWebView.toolbarBG.setColors(mWebView.ColorShade);
			
			//toolbarBG.setColors(ColorSolid);
			mWebView.toolbar_title = toolbar_title = toolbar.findViewById(R.id.toolbar_title);
			toolbar_cover = toolbar.findViewById(R.id.cover);
			if(cover!=null)
				toolbar_cover.setImageDrawable(cover);
			ucc = a.getUcc();
			toolbar_cover.setOnClickListener(this);
			toolbar_title.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					CMN.Log("toolbar_title onClick");
					if(mWebView.getVisibility()!=View.VISIBLE) {
						mWebView.setAlpha(1);
						mWebView.setVisibility(View.VISIBLE);
						if(mWebView.awaiting){
							mWebView.awaiting=false;
							renderContentAt(-1, -1, -1, null, mWebView.currentRendring);
						}
					}//((View)rl.getParent()).getId()==R.id.webholder
					else if(rl.getParent()==a.webholder) {
						mWebView.setVisibility(View.GONE);
					}
					else {
						toolbar_cover.performClick();
					}

					mWebView.post(new Runnable() {
						@Override
						public void run() {
							//a.mBar.isWebHeld=true;
							if(a.mBar.timer!=null) a.mBar.timer.cancel();
							//a.mBar.fadeIn();
							a.mBar.setMax(a.webholder.getMeasuredHeight()-((View) a.webholder.getParent()).getMeasuredHeight());
							a.mBar.setProgress(((View) a.webholder.getParent()).getScrollY());
							//a.mBar.onTouch(null, MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,0,0,0));
						}
					});
				}});
			//toolbar.setTitle(this._Dictionary_FName.split(".mdx")[0]);
			recess = toolbar.findViewById(R.id.recess);
			forward=toolbar.findViewById(R.id.forward);
			toolbar_title.setText(_Dictionary_fName);
			//vvv
			OnClickListener voyager = new OnClickListener() {
				@Override
				public void onClick(View v) {
					CMN.Log("voyager onClick");
					boolean isRecess = v.getId() == R.id.recess;
					if (false) {
						if (isRecess) if (mWebView.canGoBack()) mWebView.goBack();
						else if (mWebView.canGoForward()) mWebView.goForward(); return;
					}
					//CMN.Log("这是网页的前后导航" ,isRecess, mWebView.HistoryVagranter, mWebView.History.size());
					if (isRecess && mWebView.HistoryVagranter > 0 || !isRecess&&mWebView.HistoryVagranter<=mWebView.History.size() - 2) {
						ViewGroup root = (ViewGroup) rl.getParent();
						boolean fromCombined = root.getId() == R.id.webholder;
						try {
							ScrollerRecord PageState=null;
							if(fromCombined) {
								a.main_progress_bar.setVisibility(View.VISIBLE);
								mWebView.toTag="===???";/* OPF监听器中由recCom接管 */
							}

							if(System.currentTimeMillis()-a.lastClickTime>300 && !mWebView.isloading) {//save our postion
								if (!fromCombined || a.recCom.scrolled)
									PageState = mWebView.saveHistory(fromCombined ? a.WHP : null, a.lastClickTime);
								if (!isRecess && mWebView.HistoryVagranter == 0 && PageState != null) {
									if (fromCombined) {
										a.adaptermy2.avoyager.put(a.adaptermy2.lastClickedPosBeforePageTurn, PageState);
									} else {
										HistoryOOP.put(mWebView.currentPos, PageState);
									}
								}
							}

							a.lastClickTime = System.currentTimeMillis();

							int th = isRecess ? --mWebView.HistoryVagranter : ++mWebView.HistoryVagranter;

							int pos = IU.parsint(mWebView.History.get(th).key, -1);
							PageState = mWebView.History.get(th).value;
							float initialScale = mdict.def_zoom;
							if (PageState != null) {
								mWebView.expectedPos = PageState.y;
								mWebView.expectedPosX = PageState.x;
								initialScale = PageState.scale;
							}

							//a.showT(CMN.Log(initialScale+" :: "+th+" :: "+pos+" :: expectedPos" + (isRecess ? " <- " : " -> ") + mWebView.expectedPos));

							if (pos != -1) {
								boolean render = mWebView.currentPos != pos || mWebView.isloading;
								setCurrentDis(mWebView, pos, 0);
								if (render) {
									//CMN.Log("/*BUG::多重结果变成成单一结果*/");
									renderContentAt_internal(mWebView,initialScale, fromCombined, false, rl.getLayoutParams().height>0, pos);
								} else {
									//CMN.Log("还是在这个页面");
									mWebView.isloading = true;
									mWebView.onFinishedPage();
								}
							} else {
								mWebView.loadUrl(mWebView.History.get(mWebView.HistoryVagranter).key);//
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			recess.setOnClickListener(voyager);
			forward.setOnClickListener(voyager);
			viewsHolderReady=true;
		}
		//recess.setVisibility(View.GONE);
		//forward.setVisibility(View.GONE);
	}
	
	static void SelectHtmlObject(MainActivityUIBase a, WebViewmy final_mWebView, int source) {
		final_mWebView.evaluateJavascript("selectTouchtarget("+source+")", new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				CMN.Log("selectTouchtarget", value);
				int len = IU.parsint(value, 0);
				boolean fakePopHandles = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;
				if(len>0) {
					/* bring in action mode by a fake click on the programmatically  selected text. */
					if(fakePopHandles) {
						final_mWebView.forbidLoading=true;
						final_mWebView.getSettings().setJavaScriptEnabled(false);
						final_mWebView.getSettings().setJavaScriptEnabled(false);
						MotionEvent te = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, final_mWebView.lastX, final_mWebView.lastY, 0);
						final_mWebView.dispatchTouchEvent(te);
						te.setAction(MotionEvent.ACTION_UP);
						final_mWebView.dispatchTouchEvent(te);
						te.recycle();
						/* restore href attribute */
					}
				} else {
					a.showT("选择失败");
				}
				if(fakePopHandles) {
					final_mWebView.postDelayed(() -> {
						final_mWebView.forbidLoading=false;
						final_mWebView.getSettings().setJavaScriptEnabled(true);
						final_mWebView.evaluateJavascript("restoreTouchtarget()", null);
					}, 300);
				} else {
					final_mWebView.evaluateJavascript("restoreTouchtarget()", null);
				}
				
			}
		});
	}
	
	static class OptionListHandler extends ClickableSpan implements DialogInterface.OnClickListener {
		MainActivityUIBase a;
		WebViewmy final_mWebView;
		String url;
		public OptionListHandler(MainActivityUIBase a, WebViewmy mWebView, String extra) {
			this.a = a;
			this.final_mWebView = mWebView;
			this.url = extra;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int pos) {
			pos+=IU.parsint(((AlertDialog)dialog).getListView().getTag());
			switch(pos) {
				/* 复制链接 */
				case 0:{
					try {
						ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
						if(cm!=null){
							cm.setPrimaryClip(ClipData.newPlainText(null, url));
							a.showT("已复制");
						}
					} catch (Exception e) {
						a.showT(e+"");
					}
				} break;
				/* 复制链接文本 */
				case 1:{
					if(url!=null) {
						final_mWebView.evaluateJavascript("window._touchtarget?window._touchtarget.innerText:''", new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
								value= StringEscapeUtils.unescapeJava(value.substring(1,value.length()-1));
								ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
								if(cm!=null){
									cm.setPrimaryClip(ClipData.newPlainText(null, value));
									a.showT(value);
								}
							}
						});
					}
				} break;
				/* 选择链接文本 */
				case 2:{
					if(url!=null) {
						SelectHtmlObject(a, final_mWebView, 0);
					}
				} break;
			}
			dialog.dismiss();
		}
		
		@Override
		public void onClick(@NonNull View widget) {
			/* 打开链接 */
			boolean webUrl = url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp") || url.startsWith("file");
			if(webUrl){
				try {
					Intent intent = new Intent();
					intent.setData(Uri.parse(url));
					intent.setAction(Intent.ACTION_VIEW);
					a.startActivity(intent);
				} catch (Exception ignored) {  }
			} else {
			
			}
		}
	}
	
	class OptionListHandlerDyn extends OptionListHandler {
		public OptionListHandlerDyn(MainActivityUIBase a, WebViewmy mWebView, String extra) {
			super(a, mWebView, extra);
		}
		
		@Override
		public void onClick(DialogInterface dialog, int _pos) {
			int pos=_pos+IU.parsint(((AlertDialog)dialog).getListView().getTag());
			switch (pos) {
				/* 查看原网页 */
				case 0:{
					editingState=false;
					try {
						renderContentAt(-1, final_mWebView.SelfIdx, final_mWebView.frameAt, final_mWebView, final_mWebView.currentRendring);
					} catch (Exception ignored) { }
					editingState=true;
				} break;
				/* 删除重载页面 */
				case 1:
					if(final_mWebView.currentRendring!=null && final_mWebView.currentRendring.length>1){
						a.showT("错误：多重词条内容不可保存");
						break;
					}
				case 21:{
					getCon(true).enssurePageTable();
					String _url = getSaveUrl(final_mWebView);
					if(_url!=null){
						con.removePage(_url);
						if(PageCursor!=null) PageCursor.close();
						PageCursor = con.getPageCursor();
						a.notifyDictionaryDatabaseChanged(mdict.this);
					}
					if(pos==1) {
						renderContentAt(-1, final_mWebView.SelfIdx, final_mWebView.frameAt, final_mWebView, final_mWebView.currentRendring);
					} else {
						final_mWebView.reload();
					}
				} break;
				/* 打开中枢 */
				case 2:{
					ucc.setInvoker(null, null, null, url);
					ucc.onClick(null);
				} break;
				/* 保存网页源代码 */
				case 10:
				case 30:{
					final_mWebView.evaluateJavascript(preview_js, v1 -> {
						v1 =StringEscapeUtils.unescapeJava(v1.substring(1, v1.length()-1));
						v1 =RemoveApplicationTags(v1);
						StringBuffer sb = opt.pathToMainFolder().append("downloads/").append(final_mWebView.word)
								.append(".");
						if(pos==10) {
							sb.append( StringUtils.join(final_mWebView.currentRendring, '|')).append(".");
						}
						int len = sb.length();
						int cc=0;
						sb.append("html");
						while(new File(sb.toString()).exists()) {
							sb.setLength(len);
							sb.append(IU.a2r(++cc)).append(".html");
						}
						BU.printFile(v1.getBytes(), sb.toString());
						a.showT(sb.append(" 已保存! "));
					});
				}
				break;
				/* 查看原网页代码 */
				case 11: {
				}
				break;
				/* 添加书签 */
				case 12: {
				}
				break;
				/* 词典设置 */
				case 13:
				break;
			}
			dialog.dismiss();
		}
		
		@Override
		public void onClick(@NonNull View widget) {
			int pos = IU.parsint(widget.getTag(), 0);
			if(pos==0) {
				buildStandardOptionListDialog(a, R.string.abc_action_menu_overflow_description
						, R.array.lexical_page_url_options, this, null, null, 10);
			} else if(pos==20) {
				buildStandardOptionListDialog(a, R.string.abc_action_menu_overflow_description
						, R.array.lexical_page_url_options2
						, this, null, null, 30);
			}
		}
	}
	
	static class HtmlObjectHandler implements View.OnLongClickListener {
		MainActivityUIBase a;
		public HtmlObjectHandler(MainActivityUIBase a) {
			this.a = a;
		}
		@Override
		public boolean onLongClick(View v) {
			WebViewmy _mWebView = (WebViewmy) v;
			WebViewmy.HitTestResult result = _mWebView.getHitTestResult();
			if (null == result) return false;
			int type = result.getType();
			//CMN.Log("getHitTestResult", type, result.getExtra());
			switch (type) {
				/* 长按下载图片 */
				case WebViewmy.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
				case WebViewmy.HitTestResult.IMAGE_TYPE:{
					String url = result.getExtra();
					AlertDialog.Builder builder3 = new AlertDialog.Builder(a);
					builder3.setSingleChoiceItems(new String[] {}, 0,
							(dialog, pos) -> {
								switch(pos) {
									case 0:{
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													java.net.URL requestURL = new URL(url);
													File pathDownload = new File("/storage/emulated/0/download");
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
															HttpURLConnection urlConnection;
															InputStream is;
															FileOutputStream fout = null;
															try {
																try {
																	SSLContext sslcontext = SSLContext.getInstance("TLS");
																	sslcontext.init(null, new TrustManager[]{new mdict_web.MyX509TrustManager()}, new java.security.SecureRandom());
																	HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
																} catch (Exception ignored) { }
																urlConnection = (HttpURLConnection) requestURL.openConnection();
																urlConnection.setRequestMethod("GET");
																urlConnection.setConnectTimeout(35000);
																//urlConnection.setRequestProperty("Charset", "UTF-8");
																//urlConnection.setRequestProperty("Connection", "Keep-Alive");
																urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Linux; Android 9; VTR-AL00 Build/HUAWEIVTR-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36");
																urlConnection.connect();
																is = urlConnection.getInputStream();
																byte[] buffer = new byte[4096];
																int len;
																while ((len = is.read(buffer)) > 0) {
																	if (fout == null)
																		fout = new FileOutputStream(path);
																	fout.write(buffer, 0, len);
																}
																fout.flush();
																fout.close();
																urlConnection.disconnect();
																is.close();
															} catch (Exception e) {
																error = e.toString();
															}
															msg = error==null?"下载完成":("发生错误："+error);
														}
														if(msg!=null){
															Looper.prepare();
															a.showT(msg);
															Looper.loop();
														}
													}
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}).start();
									} break;
									case 1:{
										SelectHtmlObject(a, _mWebView, 1);
									} break;
								}
								dialog.dismiss();
							});
					SpannableStringBuilder ssb = new SpannableStringBuilder();
					int start = ssb.length();
					
					ssb.append(url);
					int end=ssb.length();
					
					ssb.setSpan(new RelativeSizeSpan(0.63f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					ssb.setSpan(new ClickableSpan() {
						@Override
						public void onClick(@NonNull View widget) {//打开链接
							Intent intent = new Intent();
							intent.setData(Uri.parse(url));
							intent.setAction(Intent.ACTION_VIEW);
							a.startActivity(intent);
						}
					}, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					builder3.setTitle(ssb);
					
					String[] Menus = a.getResources().getStringArray(
							R.array.config_images);
					List<String> arrMenu = Arrays.asList(Menus);
					AlertDialog dd = builder3.create();
					dd.show();
					dd.setOnDismissListener(dialog -> {
					});
					dd.getListView().setAdapter(new ArrayAdapter<>(a,
							R.layout.singlechoice_plain, android.R.id.text1, arrMenu) );
					View dcv = dd.getWindow().getDecorView();
					TextView titleView = dcv.findViewById(R.id.alertTitle);
					//titleView.setSingleLine(false);
					dcv.findViewById(R.id.topPanel).getLayoutParams().height=titleView.getHeight()/2;
					titleView.setMovementMethod(LinkMovementMethod.getInstance());
				}
				return true;
				/* 长按anchor */
				case WebViewmy.HitTestResult.SRC_ANCHOR_TYPE:{
					String url = result.getExtra();
					OptionListHandler olh = new OptionListHandler(a, _mWebView, url);
					buildStandardOptionListDialog(a, 0
							, R.array.config_links
							, olh, url, olh, 0);
				}
				return true;
			}
			return false;
		}
	}
	
	public static void setWebLongClickListener(WebViewmy mWebView, MainActivityUIBase a) {
		if(a.mdict_web_lcl==null) {
			a.mdict_web_lcl=new HtmlObjectHandler(a);
		}
		mWebView.setOnLongClickListener(a.mdict_web_lcl);
	}
	
	public static void buildStandardOptionListDialog(MainActivityUIBase a, int title, int arrayId, DialogInterface.OnClickListener onItemSelected, String titletail, ClickableSpan clickableSpan, int tag) {
		CharSequence titleStr = a.getString(title==0?R.string.empty__:title);
		if(titletail!=null) {
			SpannableStringBuilder ssb = new SpannableStringBuilder(titleStr);
			int start = ssb.length();
			ssb.append(titletail);
			int end=ssb.length();
			ssb.setSpan(new RelativeSizeSpan(0.63f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			titleStr = ssb;
		}
		
		AlertDialog dd = new AlertDialog.Builder(a)
				.setSingleChoiceLayout(R.layout.singlechoice_plain)
				.setSingleChoiceItems(arrayId, 0, onItemSelected)
				.setTitle(titleStr).create();
		
		dd.show();
		
		Window win = dd.getWindow();
		
		if(win!=null) {
			View dcv = win.getDecorView();
			TextView titleView = dcv.findViewById(R.id.alertTitle);
			if(titletail!=null) {
				titleView.setMovementMethod(LinkMovementMethod.getInstance());
			}
			if(title==0) {
				dcv.findViewById(R.id.topPanel).getLayoutParams().height=titleView.getHeight()/2;
			}
			titleView.setSingleLine(title==0);
			titleView.setTag(tag);
		}
		dd.getListView().setTag(tag);
	}

	public void setSaveIconLongClick(View v) {
		if(savelcl == null){
			savelcl = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					WebViewmy _mWebView = mWebView;
					String url = currentDisplaying;
					if(v!=ic_save){
						if(a.PeruseView!=null){
							_mWebView = a.PeruseView.mWebView;
							url = a.PeruseView.currentDisplaying;
						} else {
							return true;
						}
					}
					OptionListHandlerDyn olhd = new OptionListHandlerDyn(a, _mWebView, url);
					buildStandardOptionListDialog(a, R.string.page_options, R.array.config_pages
							, olhd, url, olhd, 0);
				return false;
			}
		};
		};
		v.setOnLongClickListener(savelcl);
	}

	public void saveCurrentPage(WebViewmy mWebView) {
		if(mWebView.currentRendring!=null && mWebView.currentRendring.length>1){
			a.showT("错误：多重词条内容不可保存");
			return;
		}
		if(getSavePageToDatabase())
			getCon(true).enssurePageTable();
		String url=getSaveUrl(mWebView);
		if(url!=null && url.length()>0)
		mWebView.evaluateJavascript(save_js, v -> {
			if(v!=null && v.startsWith("\"")) {
				v=StringEscapeUtils.unescapeJava(v.substring(1,v.length()-1));
				v=RemoveApplicationTags(v);
				CMN.Log("结果Html，", v);
				//CMN.Log("结果长度，", v.length()); CMN.Log("");
				String title=currentDisplaying;
				if(mWebView!=this.mWebView && a.PeruseView!=null)
					title=a.PeruseView.currentDisplaying;
				SaveCurrentPage_Internal(v, url, title);
			}
		});
	}

	String RemoveApplicationTags(String v) {
		String toFind = "class=\"_PDict\">";
		int bwLimit = 256;
		int lastEnd = 0;
		int lastFindEnd = 0;
		int firstFound = v.indexOf(toFind);
		if(firstFound==-1) return v;
		int vLen = v.length();
		StringBuffer sb = new StringBuffer(vLen);
		while(firstFound!=-1 && lastFindEnd<vLen){
			//CMN.Log("1", v.substring(firstFound, Math.min(firstFound+100, v.length())));
			int start = firstFound;
			int end=-1;
			int endEstimate = firstFound+toFind.length();
			for (; start >= lastFindEnd && (firstFound-start)<bwLimit; start--) {/* 后退找Tag开始 */
				//CMN.Log(v.charAt(start) ,start, lastFindEnd);
				if(v.charAt(start)=='<'){
					String tagName=null;
					int tagIdx = start+1;
					//CMN.Log("2", v.substring(start+1, Math.min(start+100, v.length())));
					for (; tagIdx < firstFound; tagIdx++) {/* 前进找Tag名称的结尾 */
						if(v.charAt(tagIdx)==' '){
							tagName = v.substring(start+1, tagIdx);
							break;
						}
					}
					if(tagName!=null){/* 完整地找到 */
						if(tagName.equals("meta")){ /* 没有结束标签 */
							end = endEstimate;
						} else {
							tagName = "</"+tagName+">";
							end = v.indexOf(tagName, endEstimate);
							//CMN.Log("tagName", tagName, end, v.substring(firstFound, firstFound+100));
							if(end!=-1){
								end += tagName.length();
							}
						}
					}
					break;
				}
			}
			if(end!=-1){
				sb.append(v, lastEnd, start);
				lastEnd = lastFindEnd = end;
			} else {
				lastFindEnd = endEstimate;
			}
			firstFound = v.indexOf(toFind, lastFindEnd);
		}
		if(lastEnd<vLen){
			sb.append(v, lastEnd, vLen);
		}
		return sb.toString();
	}

	protected String getSaveUrl(WebViewmy mWebView) {
		return Integer.toString(mWebView.currentPos);
	}

	protected void onPageSaved() {
		if (PageCursor != null){
			PageCursor.close();
			PageCursor = con.getPageCursor();
		}
	}

	protected String getSaveNameForUrl(String url) {
		return url;
	}

	/** Prefer saving to  database due to the possible complexity of url */
	private void SaveCurrentPage_Internal(String data, String url, String name) {
		/* save to database */
		boolean proceed=true;
		if(getSavePageToDatabase()) {
			proceed=false;
			try {
				try {
					if (con.putPage(url, name, data) != -1) {
						String succStr = true?a.getResources().getString(R.string.saved_with_size, data.length()):a.getResources().getString(R.string.saved);
						a.showT(succStr);
						onPageSaved();
					} else
						a.showT("保存失败 ");
				} catch (Exception e) {
					if (e instanceof MdxDBHelper.DataTooLargeException) {
						proceed=true;
					} else {
						a.showT("保存失败 " + e);
					}
				}
			}
			catch (Exception e) {
				CMN.Log(e);
			}
		}
		/* save to file-system */
		if(proceed){
			CachedDirectory pathSave = getInternalResourcePath(true);
			/* 每次保存前检查一遍文件夹是否真的存在 */
			if(!pathSave.cachedExists() || !pathSave.isDirectory()) pathSave.mkdirs();
			if(pathSave.cachedExists()) {
				name=getSaveNameForUrl(url);
				pathSave=new CachedDirectory(pathSave, name);
				boolean mNeedCheckSavePathName = bNeedCheckSavePathName && name.contains("/");
				if(mNeedCheckSavePathName){
					File path = pathSave.getParentFile();
					path.mkdirs();
					proceed = pathSave.isDirectory();
				}
				if(proceed)
				{
					try {
						BU.printFile(data.getBytes(), pathSave);
					} catch (Exception e) {
						a.showT("保存失败 " + e);
					}
					String succStr = true?a.getResources().getString(R.string.saved_with_size, data.length()):a.getResources().getString(R.string.saved);
					a.showT(succStr);
				}
			}
		}
	}

	protected void refresh_eidt_kit(boolean overwritable, boolean editable, boolean updateWeb) {
		if(mWebView!=null){
			editable &= overwritable;
			ic_undo.setVisibility(editable?View.VISIBLE:View.GONE);
			ic_redo.setVisibility(editable?View.VISIBLE:View.GONE);
			ic_save.setVisibility(overwritable?View.VISIBLE:View.GONE);
			if(updateWeb && rl.getParent()!=null)
				mWebView.evaluateJavascript(editable ? MainActivityUIBase.ce_on : MainActivityUIBase.ce_off, null);
		}
	}

	public boolean isJumping = false;

	//public int currentPos;
	public int internalScaleLevel=-1;
	public int lvPos,lvClickPos,lvPosOff;
	/** Current Page Historian */
	public SparseArray<ScrollerRecord> HistoryOOP = new SparseArray<>();

    @SuppressLint("JavascriptInterface")
	public void setCurrentDis(WebViewmy mWebView, int idx, int... flag) {
		if(flag==null || flag.length==0) {//书签跳转等等
			mWebView.addHistoryAt(idx);
		}
		/* 回溯 或 前瞻， 不改变历史 */
		mWebView.word = currentDisplaying = getEntryAt(mWebView.currentPos = idx);
		if(hasVirtualIndex()){
			int tailIdx=currentDisplaying.lastIndexOf(":");
			if(tailIdx>0)
				currentDisplaying=currentDisplaying.substring(0, tailIdx);
		}
		StringBuilder title_builder = AcquireStringBuffer(64);
    	toolbar_title.setText(title_builder.append(currentDisplaying.trim()).append(" - ").append(_Dictionary_fName).toString());

		if(mWebView.History.size()>2){
			recess.setVisibility(View.VISIBLE);
			forward.setVisibility(View.VISIBLE);
		}
	}
	
	
	protected byte[] AcquireCompressedBlockOfSize(int compressedSize) {
		return a.AcquireCompressedBlockOfSize(compressedSize, Math.max(maxComKeyBlockSize, maxComRecSize));
	}
	
	protected byte[] AcquireDeCompressedKeyBlockOfSize(int BlockSize) {
		return a.AcquireDeCompressedKeyBlockOfSize(BlockSize, maxDecomKeyBlockSize);
	}
	
	@Override
	protected boolean isMainThread() {
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}
	
	public void checkTint() {
		if(mWebView!=null) {
			tintBackground(mWebView);
		}
	}
	
	public void tintBackground(WebViewmy mWebView) {
    	//CMN.rt();
		int globalPageBackground = a.GlobalPageBackground;
		boolean useInternal = getUseInternalBG();
		boolean isDark = GlobalOptions.isDark;
		int myWebColor = useInternal?bgColor:globalPageBackground;
		if (isDark) {
			myWebColor = ColorUtils.blendARGB(myWebColor, Color.BLACK, a.ColorMultiplier_Web2);
		}
		a.guaranteeBackground(globalPageBackground);
		mWebView.setBackgroundColor((getIsolateImages()||useInternal||Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT)?myWebColor:Color.TRANSPARENT);
		/* check and set colors for toolbar title Background*/
		if(mWebView==this.mWebView){
			mWebView.titleBar.fromCombined = mWebView.fromCombined;
		}
		FlowTextView toolbar_title = mWebView.toolbar_title;
		if(toolbar_title!=null) {
			int StarLevel =  PDICMainAppOptions.getDFFStarLevel(firstFlag);
			toolbar_title.setStarLevel(StarLevel);
			if(StarLevel>0) {
				toolbar_title.setStarDrawables(a.getActiveStarDrawable(), toolbar_title==a.popupIndicator?a.getRatingDrawable():null);
			}
		}
		GradientDrawable toolbarBG = mWebView.toolbarBG;
		if(toolbarBG!=null) {
			useInternal = getUseInternalTBG();
			myWebColor = isDark?Color.BLACK:useInternal?TIBGColor:PDICMainAppOptions.getTitlebarUseGlobalUIColor()?a.MainBackground:opt.getTitlebarBackgroundColor();
			CMN.Log("使用内置标题栏颜色：", useInternal, _Dictionary_fName, isDark, Integer.toHexString(myWebColor));
			int colorTop = PDICMainAppOptions.getTitlebarUseGradient()?ColorUtils.blendARGB(myWebColor, Color.WHITE, 0.08f):myWebColor;
			int[] ColorShade = mWebView.ColorShade;
			if(ColorShade[1]!=myWebColor||ColorShade[0]!=colorTop){
				ColorShade[1] = myWebColor;
				ColorShade[0] = colorTop;
				if(useInternal) {
					toolbarBG = mWebView.MutateBGInTitle();
				}
				toolbarBG.setColors(ColorShade);
				CMN.Log("设置了?");
			}
			myWebColor = isDark?Color.WHITE:useInternal?TIFGColor:opt.getTitlebarForegroundColor();
			mWebView.setTitlebarForegroundColor(myWebColor);
		}
		//CMN.pt("设置颜色：");
	}

	//todo frameAt=-1
    public void renderContentAt(float initialScale, int SelfIdx, int frameAt, WebViewmy mWebView, int... position){
    	CMN.Log("renderContentAt!!!...", _Dictionary_fName);
    	isJumping=false;
    	if(mWebView==null) {
    		mWebView=this.mWebView;
    	} else if(a.PeruseView != null && mWebView==a.PeruseView.mWebView){
			setSaveIconLongClick(a.PeruseView.ic_save);
			a.PeruseView.refresh_eidt_kit(getContentEditable(), getEditingContents());
		}
		boolean resposibleForThisWeb=mWebView==this.mWebView;
	
		int from = mWebView.fromCombined;
		mWebView.fromNet=false;
		boolean fromCombined = from==1;
		boolean fromPopup = from==2;

		boolean mIsolateImages=/*resposibleForThisWeb&&*/from==0&&getIsolateImages();
		/* 为了避免画面层叠带来的过度重绘，网页背景保持透明？。 */
		tintBackground(mWebView);

		if(SelfIdx!=-1){
			mWebView.setTag(mWebView.SelfIdx=SelfIdx);
			if(resposibleForThisWeb) rl.setTag(SelfIdx);
			//todo 是否记忆临时的折叠状态？
			//todo 是否为常规的页面开放连续的历史纪录？
			mWebView.clearIfNewADA(resposibleForThisWeb?-100:SelfIdx);
			mWebView.currentPos=position[0];
			mWebView.currentRendring=position;
			mWebView.frameAt = frameAt;
			mWebView.awaiting = false;
			if(/*resposibleForThisWeb && */fromCombined && (PDICMainAppOptions.getTmpIsCollapsed(tmpIsFlag) || frameAt>0&&(PDICMainAppOptions.getDelaySecondPageLoading()||PDICMainAppOptions.getOnlyExpandTopPage()))){/* 自动折叠 */
				mWebView.awaiting = true;
				mWebView.setVisibility(View.GONE);
				setCurrentDis(mWebView, mWebView.currentPos);
				CMN.Log("折叠！！！");
				return;
			}
		}
		else {
			position = mWebView.currentRendring;
			initialScale = -1;
			frameAt = mWebView.frameAt;
		}
		if(mWebView.getVisibility()!=View.VISIBLE)
			mWebView.setVisibility(View.VISIBLE);

		if(getFontSize()!=mWebView.getSettings().getTextZoom())
			mWebView.getSettings().setTextZoom(getFontSize());

		if(resposibleForThisWeb) {
	    	setCurrentDis(mWebView, position[0]);
			if(fromCombined) {
				if(rl.getLayoutParams()!=null)
					rl.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				mWebView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			}
			else {
				if(mIsolateImages){
					/* 只有在单本阅读的时候才可以进入图文分离模式。包括三种情况。 */
					//PDICMainAppOptions.getIsoImgLimitTextHight()  强定高度
					rl.getLayoutParams().height = (int) (150*opt.dm.density);//文本限高模式
					a.webSingleholder.setTag(R.id.image,false);
					a.webSingleholder.setBackgroundColor(Color.TRANSPARENT);
					a.PageSlider.TurnPageEnabled=false;
					mWebView.setBackgroundColor(Color.TRANSPARENT);
					a.initPhotoViewPager();
				} else {
					rl.getLayoutParams().height = LayoutParams.MATCH_PARENT;
					mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				}
				a.RecalibrateWebScrollbar(mWebView);
			}
		}


    	//mWebView.setVisibility(View.VISIBLE);
   	    //a.showT("mWebView:"+mWebView.isHardwareAccelerated());
		if(!fromPopup) PlayWithToolbar(a.hideDictToolbar,a);
    	if(mWebView.wvclient!=a.myWebClient) {
			mWebView.setWebChromeClient(a.myWebCClient);
	   	    mWebView.setWebViewClient(a.myWebClient);
    	}

		if(!fromPopup) {
			mWebView.clearHistory = true;
		}

		renderContentAt_internal(mWebView, initialScale, fromCombined, fromPopup, mIsolateImages, position);
    }
			
	public StringBuilder AcquirePageBuilder() {
		StringBuilder sb = AcquireStringBuffer(512);
		sb.append(htmlBase);
		//todo 插入 同名 css 文件？
		sb.append(js);
		return sb;
	}
	
	public void renderContentAt_internal(WebViewmy mWebView,float initialScale, boolean fromCombined, boolean fromPopup, boolean mIsolateImages, int...position) {
		mWebView.isloading=true;
		mWebView.currentPos = position[0];
		//if(!a.AutoBrowsePaused&&a.background&&PDICMainAppOptions.getAutoBrowsingReadSomething())
		//	mWebView.resumeTimers();
    	String htmlCode = null ,JS=null;
		try {
			if(virtualIndex!=null)
				try {
					JSONObject vc = JSONObject.parseObject(virtualIndex.getRecordAt(position[0]));
					Integer AI = vc.getIntValue("I");
					JS=vc.getString("JS");
					if(mWebView.getTag(R.id.virtualID)!=AI){
						htmlCode = getVirtualRecordsAt(position);
						mWebView.setTag(R.id.virtualID, AI);
					}
				} catch (Exception ignored) { }
			else{
				htmlCode = getRecordsAt(position);
			}
		} catch (Exception e) {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(s));
			htmlCode=_404+e.getLocalizedMessage()+"<br>"+s;
			CMN.Log(s);
		}

    	//CMN.Log("缩放是", initialScale);
		if(initialScale!=-1)
			mWebView.setInitialScale((int) (100*(initialScale/mdict.def_zoom)*opt.dm.density));//opt.dm.density
		else {
			//尝试重置页面缩放
			if(false && Build.VERSION.SDK_INT<=23) {
				//mWebView.zoomBy(0.02f);
				mWebView.setTag(R.id.toolbar_action3,true);
			}else
				mWebView.setInitialScale(0);//opt.dm.density
		}
		
		StringBuilder htmlBuilder = AcquirePageBuilder();
		
		//todo may allow ?
		mWebView.getSettings().setSupportZoom(!fromCombined);
		if(!fromCombined) {
			mWebView.setTag(R.id.toolbar_action5, a.hasCurrentPageKey() ? false : null);
		}
		mWebView.isloading = true;
		if(htmlCode!=null)
			if(!htmlCode.startsWith(fullpageString)) {
				AddPlodStructure(mWebView, htmlBuilder, fromPopup, mIsolateImages);
				LoadPagelet(mWebView, htmlBuilder, htmlCode);
			}
			else{
				CMN.Log("fullpageString");
				int headidx = htmlCode.indexOf("<head>");
				boolean b1=headidx==-1;
				if(true){
					if(b1) {
						htmlBuilder.append("<head>");
					} else {
						htmlBuilder.append(htmlCode, 0, headidx+6);
					}
					AddPlodStructure(mWebView, htmlBuilder, fromPopup, mIsolateImages);
					htmlBuilder.append(getSimplestInjection());
					if(b1) {
						htmlBuilder.append("</head>");
						htmlBuilder.append(htmlCode);
					} else {
						htmlBuilder.append(htmlCode, headidx+6, htmlCode.length());
					}
					htmlCode = htmlBuilder.toString();
				}
				mWebView.loadDataWithBaseURL(baseUrl,htmlCode,null, "UTF-8", null);
			}
		else if(JS!=null){
			mWebView.evaluateJavascript(JS, null);
		}
	}
	
	/**
	 @font-face
	 {
		 font-family: 'Kingsoft Phonetic Plain';
		 src: url('android_asset/fonts/myfontmyfont.ttf');
	 }
	 */
	@Multiline
	final static String cssfont = "sss";

	/** Let's call and call and call and call!!! */
	public void AddPlodStructure(WebViewmy mWebView, StringBuilder htmlBuilder, boolean fromPopup, boolean mIsolateImages) {
    	//CMN.Log("MakeRCSP(opt)??", MakeRCSP(opt),MakeRCSP(opt)>>5);
		htmlBuilder.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
		if(mdd!=null && mdd.size()>0) htmlBuilder.append("<p class='MddExist'/>");
		htmlBuilder.append("</div>");
		boolean styleOpened=false;
		if (fromPopup) {
			htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;
			htmlBuilder.append("body{ margin-top:0px; margin-bottom:").append(30).append("px }");
		}
		if (a.fontFaces!=null) {
			if(!styleOpened) htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;
			htmlBuilder.append(a.fontFaces);
		}
		if(mIsolateImages) {
			if(!styleOpened){ htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;}
			htmlBuilder.append("body{color:#fff;text-shadow: 5px 2.5px 10px #000;}img{display:none}");
		}
		if(styleOpened) {
			htmlBuilder.append("</style>");
		}

		if (GlobalOptions.isDark) {
			htmlBuilder.append(MainActivityUIBase.DarkModeIncantation_l);
		}

		htmlBuilder.append("<script class=\"_PDict\">");
		int rcsp = MakeRCSP(opt);
		if(mWebView==a.popupWebView) rcsp|=1<<5;
		htmlBuilder.append("rcsp=").append(rcsp).append(";");
		htmlBuilder.append("frameAt=").append(mWebView.frameAt).append(";");
		if(!(this instanceof mdict_web)) {
			htmlBuilder.append("webx=").append(1).append(";");
		}

		if (PDICMainAppOptions.getInPageSearchHighlightBorder()) {
			htmlBuilder.append(hl_border);
		}
		htmlBuilder.append("</script>");
	}

	public void LoadPagelet(WebViewmy mWebView, StringBuilder htmlBuilder, String records) {
		mWebView.loadDataWithBaseURL(baseUrl,
				htmlBuilder.append(htmlHeadEndTag)
						.append(records)
						.append(htmlEnd).toString(), null, "UTF-8", null);
	}

	String getSimplestInjection() {
		return SimplestInjection;
	}

	public static int MakeRCSP(PDICMainAppOptions opt) {
		return opt.FetUseRegex3()|
				opt.FetPageCaseSensitive()|
				opt.FetPageWildcardSplitKeywords()|
				opt.FetPageWildcardMatchNoSpace()|
				opt.FetInPageSearchUseWildcard()|
				opt.FetClickSearchEnabled()|
				opt.FetIsDark()
				;
	}

	public void PlayWithToolbar(boolean hideDictToolbar,Context a) {
		if(mWebView==null) return;
		View sp = (View) mWebView.getParent().getParent();
		if(sp==null) return;
		int val = IU.parsint(mWebView.getTag(R.id.toolbar_action4));
		if(hideDictToolbar) {
			if(sp.getId()==R.id.webholder) {
				if(val!=1) {
					toolbar.setVisibility(View.VISIBLE);
					LayoutParams lp = toolbar.getLayoutParams();
					lp.height=10;
					toolbar.setLayoutParams(lp);
					toolbar_cover.setVisibility(View.INVISIBLE);
					toolbar_title.setVisibility(View.INVISIBLE);
					mWebView.setTag(R.id.toolbar_action4,1);
				}
			}else if(val!=2){
				toolbar.setVisibility(View.GONE);
				mWebView.setTag(R.id.toolbar_action4,2);
			}
		}else if(val!=3){
			toolbar.setVisibility(View.VISIBLE);
			LayoutParams lp = toolbar.getLayoutParams();
			lp.height=(int) a.getResources().getDimension(R.dimen.dictitle);
			toolbar.setLayoutParams(lp);
			toolbar.setVisibility(View.VISIBLE);
			toolbar_cover.setVisibility(View.VISIBLE);
			toolbar_title.setVisibility(View.VISIBLE);
			mWebView.setTag(R.id.toolbar_action4,3);
		}
	}

    @Override
    public String getRecordsAt(int... positions) throws IOException {
		return positions[0]==-1? new StringBuilder(getAboutString())
				.append("<BR>").append("<HR>")
				.append(getDictInfo()).toString(): super.getRecordsAt(positions);
    }

	@Override
	public String getRecordAt(int position) throws IOException {
    	if(editingState && getContentEditable()){//Todo save and retrieve via sql database
			CachedDirectory cf = getInternalResourcePath(false);
			boolean ce =  cf.cachedExists();
			File p = ce?new File(cf, Integer.toString(position)):null;
			boolean pExists = ce && p.exists();
			//retrieve page from database
    		if(getSavePageToDatabase()){
    			String url=Integer.toString(position);
				getCon(true).enssurePageTable();
				con.preparePageContain();
				if(con.containsPage(url)){
					if(!pExists) return con.getPageString(url, StandardCharsets.UTF_8);
					else{
						Object[] results=con.getPageAndTime(url);
						if(results!=null) {
							/* 删除文件，除非文件最近修改过。 */
							/* 只在此处删除文件。 */
							/* xian baocun dao shujuku ->  baocun dao wenjian  */
							/* --> canliu laojiu de shuju. Duqu shi, bu yu li hui.*/
							/* xian baocun dao wenjian -> baocun dao shujuku  */
							/* --> canliu laojiu de wenjian. Duqu shi shanchu */
							if ((long) results[1] > p.lastModified()) {
								p.delete();
								return new String((byte[])results[0]);
							}
						}
					}
				}
			}
			//retrieve page from file system
			if(pExists){
				return BU.fileToString(p);
			}
		}
		return super.getRecordAt(position);
	}

	public String getAboutString() {
		//return Build.VERSION.SDK_INT>=24?Html.fromHtml(_header_tag.get("Description"),Html.FROM_HTML_MODE_COMPACT).toString():Html.fromHtml(_header_tag.get("Description")).toString();
		String ret=_header_tag==null?"":_header_tag.get("Description");
		if(ret==null) ret="";
		return StringEscapeUtils.unescapeHtml3(ret);
	}
	
	public MdxDBHelper con;
	public MdxDBHelper getCon(boolean open) {
		if(con==null){
			if(DataBasePath==null) {
				DataBasePath = CachedPathSubToDBStorage("bookmarks.sql");
			}
			if(!open && !DataBasePath.cachedExists()) {
				return null;
			}
			File parent = DataBasePath.getParentFile();
			if(parent!=null){
				parent.mkdirs();
			}
			con = new MdxDBHelper(a,DataBasePath.getPath(),opt);
		}
		return con;
	}

	public void closeCon() {
		MdxDBHelper con1=con;
		con=null;
		if(con1!=null) {
			con1.close();
		}
	}

	public boolean containsResourceKey(String skey) {
		if(mdd!=null)
		for(mdictRes mddTmp:mdd){
			if(mddTmp.lookUp(skey)>=0)
				return true;
		}
		return  false;
	}

	public String getLexicalEntryAt(int position) {
		return position>=0&&position<_num_entries?getEntryAt(position):"Error!!!";
	}

	public boolean hasCover() {
		return cover!=null;
	}

	@Override
	public boolean isMddResource() {
		return isResourceFile;
	}

	@Override
	public void setTmpIsFlag(int val) {
		tmpIsFlag = val;
	}

	@Override
	public void checkFlag(Activity context) {
		if(FFStamp!=firstFlag || isDirty)
			dumpViewStates(a.UIProjects);
	}

	@Override
	public long getFirstFlag() {
		return firstFlag;
	}
	
	@Override
	public void setFirstFlag(long val){
		IBC.firstFlag = firstFlag = val;
		checkTint();
	}

	@Override
	public PDICMainAppOptions getOpt() {
		return opt;
	}

	public Object[] getSoundResourceByName(String canonicalName) throws IOException {
		if(isResourceFile){
			int idx = lookUp(canonicalName, false);
			if(idx>=0){
				String matched=getEntryAt(idx);
				if(matched.regionMatches(true,0, canonicalName, 0, canonicalName.length())){
					String spx = "spx";
					return new Object[]{matched.regionMatches(true,canonicalName.length(), spx, 0, spx.length()), getResourseAt(idx)};
				}
			}
		}else{
			if(mdd!=null && mdd.size()>0){
				for(mdictRes mddTmp:mdd){
					int idx = mddTmp.lookUp(canonicalName, false);
					if(idx>=0) {
						String matched=mddTmp.getEntryAt(idx);
						//SU.Log("getSoundResourceByName", matched, canonicalName);
						if(matched.regionMatches(true,0, canonicalName, 0, canonicalName.length())){
							String spx = "spx";
							return new Object[]{matched.regionMatches(true,canonicalName.length(), spx, 0, spx.length()), mddTmp.getResourseAt(idx)};
						}
					}
					//else SU.Log("chrochro inter_ key is not find:",_Dictionary_fName,canonicalName, idx);
				}
			}
		}
		return null;
	}
	
	public void setToolbarTitleAt(int pos) {
		String entry = pos>=-1?getEntryAt(pos).trim():currentDisplaying;
		StringBuilder sb = AcquireStringBuffer(entry.length()+_Dictionary_fName.length()+5);
		sb.append(entry).append(" - ");
		appendCleanDictionaryName(sb);
		toolbar_title.setText(sb.toString());
	}
	
	public String getCharsetName() {
		return _charset.name();
	}
	
	@SuppressWarnings("unused")
    public static class AppHandler {
		mdict mdx;

        @JavascriptInterface
        public void log(String val) {
        	CMN.Log(val);
        }

        float scale;
        DisplayMetrics dm;

        public AppHandler(mdict _mdx) {
			mdx=_mdx;
            scale = mdx.a.getResources().getDisplayMetrics().density;
    		dm = mdx.opt.dm;
        }

        @JavascriptInterface
        public void openImage(int position, String... img) {
        	//CMN.Log(position, img, mdx._Dictionary_fName_Internal);
			MainActivityUIBase aa = mdx.a;
			AgentApplication app = ((AgentApplication) aa.getApplication());
			app.mdd = mdx.mdd;
			app.IBC = mdx.IBC;
			app.opt = mdx.opt;
			app.Imgs = img;
			app.currentImg = position;
			aa.root.postDelayed(aa.getOpenImgRunnable(), 100);
		}

        @JavascriptInterface
        public void scrollHighlight(int o, int d) {
			mdx.a.scrollHighlight(o, d);
        }

        @JavascriptInterface
        public String getCurrentPageKey() {
			return mdx.a.getCurrentPageKey();
		}

        @JavascriptInterface
        public int getDeviceHeight(){
			mdx.a.getWindowManager().getDefaultDisplay().getMetrics(dm);
    		//return (int) (1.0f*dm.heightPixels/ scale + 0.5f);
    		return (int) (dm.heightPixels/scale*1.5);
    		//return (int) (dm.heightPixels);
        }
        @JavascriptInterface
        public float getDeviceRatio(){
    		DisplayMetrics dm = new DisplayMetrics();
			mdx.a.getWindowManager().getDefaultDisplay().getMetrics(dm);
    		return 1.0f*dm.heightPixels/dm.widthPixels;
        }

        @JavascriptInterface
        public void showTo(int val) {
        	if(true) return;
        }

        @JavascriptInterface
        public void onAudioPause() {
			if(!mdx.a.opt.supressAudioResourcePlaying)
				mdx.a.onAudioPause();
        }

        @JavascriptInterface
        public void onAudioPlay() {
        	if(!mdx.a.opt.supressAudioResourcePlaying)
        		mdx.a.onAudioPlay();
        }

        @JavascriptInterface
        public void jumpHighlight(int d) {
			mdx.a.jumpHighlight(d, true);
        }

        @JavascriptInterface
        public void onHighlightReady(int idx, int number) {
			mdx.a.onHighlightReady(idx, number);
        }

        @JavascriptInterface
        public void popupWord(String key, int frameAt, float pX, float pY, float pW, float pH) {
        	MainActivityUIBase a = mdx.a;
			a.popupWord(key, -1, frameAt);
			if(frameAt>=0 && pH!=0){
				if(pW==0) pW=pH;
				if(RLContainerSlider.lastZoomTime == 0 || System.currentTimeMillis() - RLContainerSlider.lastZoomTime > 500){
					/* 只管去兮不管来 */
					float density = dm.density;
					//Utils.setFloatTextBG(new Random().nextInt());
					WebViewmy mWebView = a.PeruseViewAttached()?a.PeruseView.mWebView:mdx.mWebView;
					if(mWebView!=null){
						mWebView.highRigkt_set(pX*density, pY*density, (pX+pW)*density, (pY+pH)*density);
					}
				}
			}
        }

        @JavascriptInterface
        public void popupClose() {
        	if(this!=mdx.a.popuphandler)
				mdx.a.postDetachClickTranslator();
        }

		@JavascriptInterface
		public void parseContent(int processed, int total, String contents) {
			if(mdx instanceof mdict_pdf && mdx.a instanceof PDICMainActivity){
				CMN.Log("parseContent", contents, mdx._Dictionary_fName);
				//((PDICMainActivity)mdx.a).parseContent(mdx, contents);
				mdict_pdf pdx = ((mdict_pdf) mdx);
				pdx.pdf_index=contents.split("\n");
				mdx.a.lv.post(new Runnable() {
					@Override
					public void run() {
						((BasicAdapter)mdx.a.lv.getAdapter()).notifyDataSetChanged();
						mdx.a.showT("目录提取完成！("+processed+"/"+total+")");
					}
				});
			}
		}

		@JavascriptInterface
		public void banJs(boolean banIt) {
			WebViewmy wv = mdx.mWebView;
			wv.getSettings().setJavaScriptEnabled(!banIt);
		}

		@JavascriptInterface
		public void collectWord(String word) {
			mdx.a.showT(word+" 已收藏");
		}

		@JavascriptInterface
		public void ReadText(String word) {
			mdx.a.ReadText(word, this==mdx.a.popuphandler?mdx.a.popupWebView:mdx.mWebView);
		}

		public void setDict(mdict ccd) {
        	if(ccd!=null)
				mdx=ccd;
		}

		@JavascriptInterface
		public void setTTS() {
			mdx.a.root.post(() -> mdx.a.showTTS());
		}

		@JavascriptInterface
		public void snack(String val) {
			mdx.a.root.post(() -> mdx.a.showContentSnack(val));
		}

		@JavascriptInterface
		public void onRequestFView(int w, int h) {
        	//CMN.Log("onRequestFView", w, h, w>h);
			_req_fvw=w;
			_req_fvh=h;
			if(mdx.a.opt.getFullScreenLandscapeMode()==2)
				((Handler)mdx.a.hdl).sendEmptyMessage(7658942);
		}
		
		@JavascriptInterface
		public void onExitFView() {
        	//CMN.Log("onExitFView");
			MainActivityUIBase.CustomViewHideTime = System.currentTimeMillis();
			((Handler)mdx.a.hdl).sendEmptyMessageDelayed(7658941, 600);
		}
	}

	public boolean renameFileTo(Context c, File newF) {
		//tofo_tofo
//		File fP = newF.getParentFile();
//		fP.mkdirs();
//		boolean ret = false;
//		boolean pass = !f.exists();
//		String _Dictionary_fName_InternalOld = "."+_Dictionary_fName;
//		if(fP.exists() && fP.isDirectory()) {
//			int retret = FU.rename(a, f, newF);
//			Log.d("XXX-ret",""+retret);
//			//Log.e("XXX-ret",f.getParent()+"sad"+newF.getParent());
//			String oldName = _Dictionary_fName;
//			if(retret==0) {
//				_Dictionary_fName = newF.getName();
//				/* 重命名资源文件 */
//				ret = true;
//		    	if(mdd!=null)
//				for(mdictRes mddTmp:mdd){
//					File mddF = mddTmp.f();
//					String fn = BU.unwrapMddName(mddF.getName());
//					if(fn.startsWith(_Dictionary_fName)){
//						fn=fn.substring(_Dictionary_fName.length());
//						File newMdd = new File(fP,_Dictionary_fName+fn+".mdd");
//						if(mddF.exists()) {
//							int ret1 = FU.rename(a, mddF, newMdd);
//							if (ret1 == 0 && mdd != null) {
//								mddTmp.updateFile(newMdd);
//							}
//						}
//					}
//				}
//				else if(new File(fP,_Dictionary_fName+".mdd").exists()) {
//					try {
//						mdd = Collections.singletonList(new mdictRes(new File(fP, _Dictionary_fName + ".mdd")));
//						a.showT("找到了匹配的mdd！");
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}else if(retret==-123) {
//				a.showT("错误：不恰当的路径分隔符");
//			}
//		}
//		if(ret || pass) {
//			f=newF;
//			String fn = newF.getAbsolutePath();
//		}
//		new File(opt.pathToDatabases().append(_Dictionary_fName_InternalOld).toString()).renameTo(new File(opt.pathToDatabases().append("."+_Dictionary_fName).toString()));
//
//		if(a.currentDictionary==this)
//			a.setLastMdFn(_Dictionary_fName);
//		return ret;
		return false;
	}

	@Override
	public boolean exists() {
		return f.exists();
	}

	@Override
	public boolean equalsToPlaceHolder(PlaceHolder placeHolder) {
		String ThisPath = f.getPath();
		String LibPath = opt.lastMdlibPath.getPath();
		String OtherPath = placeHolder.pathname;
		if(!OtherPath.startsWith("/") && ThisPath.startsWith(LibPath)){
			return ThisPath.regionMatches(LibPath.length()+1, OtherPath, 0, OtherPath.length());
		}
		return placeHolder.getPath(opt).equals(f);
	}

	public boolean moveFileTo(Context c, File newF) {
		File fP = newF.getParentFile();
		fP.mkdirs();
		boolean ret = false;
		if(fP.exists() && fP.isDirectory()) {
			int retret = FU.move3(a, f, newF);
			CMN.Log("XXX-ret",""+retret);
			if(retret>=0) {
				_Dictionary_fName = newF.getName();
				String clean_Dictionary_fName = getCleanDictionaryName(_Dictionary_fName);
				/* 移动资源文件 */
				ret = true;
				if(mdd!=null)
					for(mdictRes mddTmp:mdd){
						File mddF = mddTmp.f();
						String fn = mddF.getName();
						if(fn.startsWith(clean_Dictionary_fName)){
							fn=fn.substring(clean_Dictionary_fName.length());
							File newMdd = new File(fP,clean_Dictionary_fName+fn);
							if(mddF.exists()) {
								int ret1 = FU.rename(a, mddF, newMdd);
								if (ret1 == 0 && mdd != null) {
									mddTmp.updateFile(newMdd);
								}
							}
						}
					}
				else {
					File mddNew = new File(fP,clean_Dictionary_fName+".mdd");
					if(mddNew.exists()) {
						try {
							mdd = Collections.singletonList(new mdictRes(mddNew));
							a.showT("找到了匹配的mdd！");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				f=newF;
			}
			else if(retret==-123) {
				a.showT("错误：不恰当的分隔符");
			}
		}
		if(a.currentDictionary==this){
			a.setLastMdFn(_Dictionary_fName);
		}
		return ret;
	}

	public void unload() {
		if (a!=null && isDirty)
			dumpViewStates(a.UIProjects);
		if(mWebView!=null) {
			mWebView.shutDown();
    		mWebView=null;
		}
		if(viewsHolderReady) {
			ucc=null;
			toolbar_cover.setOnClickListener(null);
		}
		a=null;
	}

	@Override
	public Drawable getCover() {
		return cover;
	}

	@Override
	public int getTmpIsFlag() {
		return tmpIsFlag;
	}

	public void dumpViewStates(HashMap<CharSequence,byte[]> UIProjects) {
		setIsDedicatedFilter(false);
		try {
			DataOutputStream data_out;
			byte[] data;
			
			String save_name = _Dictionary_fName;
			
			ReusableByteOutputStream bos = new ReusableByteOutputStream(UIProjects.get(save_name), MainActivityUIBase.ConfigSize + MainActivityUIBase.ConfigExtra);
			data = bos.getBytes();
			bos.precede(MainActivityUIBase.ConfigExtra);
			data_out = new DataOutputStream(bos);
			
			data_out.writeByte((int) (255*IBC.doubleClickXOffset));
			data_out.writeByte((int) (255*IBC.doubleClickPresetXOffset));
			data_out.writeByte(0);
			data_out.writeInt(bgColor);
			data_out.writeInt(internalScaleLevel);
			data_out.writeInt(lvPos);
			data_out.writeInt(lvClickPos);
			data_out.writeInt(lvPosOff);
			//CMN.Log("保存列表位置",lvPos,lvClickPos,lvPosOff, _Dictionary_fName);
			ScrollerRecord record = avoyager.get(lvClickPos);
			if(record!=null){
				data_out.writeInt(record.x);
				data_out.writeInt(record.y);
				data_out.writeFloat(record.scale);
			} else {
				data_out.writeInt(0);
				data_out.writeInt(0);
				data_out.writeFloat(webScale);
			}
			//CMN.Log(_Dictionary_fName+"保存页面位置",expectedPosX,expectedPos,webScale);
			data_out.writeLong(firstFlag);
			data_out.writeInt(TIBGColor);
			data_out.writeInt(TIFGColor);
			data_out.writeFloat(IBC.doubleClickZoomRatio);
			data_out.writeFloat(IBC.doubleClickZoomLevel1);
			data_out.writeFloat(IBC.doubleClickZoomLevel2);
			
			data_out.close();
			
			/* Just mark as dirty. */
			if(!CMN.bForbidOneSpecFile && a!=null){
				CMN.Log("putted", save_name);
				a.dirtyMap.add(save_name);
			}
			UIProjects.put(save_name, data);
			isDirty = false;
		} catch (Exception e) { if(GlobalOptions.debug) CMN.Log(e); }
	}

	public void putSates() throws IOException {
		ReusableByteOutputStream bos = new ReusableByteOutputStream(50);
		DataOutputStream fo = new DataOutputStream(bos);
		fo.writeShort(12);
		fo.writeByte(0);

		fo.writeInt(bgColor);
		fo.writeInt(internalScaleLevel);

		fo.writeInt(lvPos);
		fo.writeInt(lvClickPos);
		fo.writeInt(lvPosOff);
		//CMN.Log("保存列表位置",lvPos,lvClickPos,lvPosOff, _Dictionary_fName);

		int ex=0,e=0;
		ScrollerRecord record = avoyager.get(lvClickPos);
		if(record!=null){
			fo.writeInt(record.x);
			fo.writeInt(record.y);
			fo.writeFloat(record.scale);
		} else {
			fo.writeInt(0);
			fo.writeInt(0);
			fo.writeFloat(webScale);
		}
		//CMN.Log(_Dictionary_fName+"保存页面位置",expectedPosX,expectedPos,webScale);

		fo.writeLong(firstFlag);


		byte[] data = bos.getBytes();
		byte[] oldData = new byte[data.length + MainActivityUIBase.ConfigExtra];
		for (int k = 0; k < 4; k++) {
			oldData[k] = 0;
		}
		System.arraycopy(data, 0, oldData, MainActivityUIBase.ConfigExtra, data.length);

		AgentApplication app = ((AgentApplication) a.getApplication());
		app.UIProjects.put(f.getName(), oldData);
	}

	public int getFontSize() {
		if(getUseInternalFS())
			return internalScaleLevel>0?internalScaleLevel:(internalScaleLevel=def_fontsize);
		return def_fontsize;
	}

	public float webScale=0;

	public void readInConfigs(HashMap<CharSequence,byte[]> UIProjects) throws IOException {
		DataInputStream data_in1 = null;
		try {
			CMN.rt();
			byte[] data = UIProjects.get(f.getName());
			if(data!=null){
				int extra = MainActivityUIBase.ConfigExtra;
				data_in1 = new DataInputStream(new ByteArrayInputStream(data, extra, data.length-extra));
			}
			if(data_in1!=null){
				//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
				IBC.doubleClickXOffset = ((float)Math.round(((float)data_in1.read())/255*1000))/1000;
				IBC.doubleClickPresetXOffset = ((float)Math.round(((float)data_in1.read())/255*1000))/1000;
				byte _firstFlag = data_in1.readByte();
				firstFlag=0;
				if(_firstFlag!=0){
					firstFlag |= _firstFlag;
				}
				bgColor = data_in1.readInt();
				internalScaleLevel = data_in1.readInt();
				lvPos = data_in1.readInt();
				lvClickPos = data_in1.readInt();
				lvPosOff = data_in1.readInt();
				//CMN.Log(_Dictionary_fName,"列表位置",lvPos,lvClickPos,lvPosOff);
				if(data_in1.available()>0) {
					ScrollerRecord record = new ScrollerRecord(data_in1.readInt(), data_in1.readInt(), webScale = data_in1.readFloat());
					avoyager.put(lvClickPos, record);
				}
				firstFlag |= data_in1.readLong();
				TIBGColor = data_in1.readInt();
				TIFGColor = data_in1.readInt();
				IBC.doubleClickZoomRatio = data_in1.readFloat();
				IBC.doubleClickZoomLevel1  = data_in1.readFloat();
				IBC.doubleClickZoomLevel2  = data_in1.readFloat();
			}
			//CMN.pt(_Dictionary_fName+" 单典配置加载耗时");
		} catch (Exception e) {
			if(GlobalOptions.debug) CMN.Log(e);
		} finally{
			FFStamp = firstFlag;
			if(data_in1!=null) data_in1.close();
		}
		IBC.firstFlag = firstFlag;
		if(bgColor==null) bgColor=CMN.GlobalPageBackground;
		if(IBC.doubleClickZoomRatio==0) {
			/* initialise values */
			IBC.doubleClickZoomRatio=2.25f;
			TIBGColor = PDICMainAppOptions.getTitlebarUseGlobalUIColor()?CMN.MainBackground:opt.getTitlebarBackgroundColor();
			TIFGColor = opt.getTitlebarForegroundColor();
		}
	}

	public interface ViewLayoutListener{
		void onLayoutDone(int size);
	}public ViewLayoutListener vll;

	@Override
	public void onReceiveValue(String value) {
		Log.e("fatal_onReceiveValue", value);
		if(value==null) return;
		int val;
		try {
			val = Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return;
		}
		if(rl.getParent() !=null && ((View)rl.getParent()).getId()==R.id.webholder) {
			if(mWebView.getLayoutParams().height!=ViewGroup.LayoutParams.WRAP_CONTENT) {
				mWebView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
				//mWebView.setLayoutParams(mWebView.getLayoutParams());
			}
		}

		if(vll!=null)
			vll.onLayoutDone(val);
	}

	public CachedDirectory getInternalResourcePath(boolean create) {
		if(InternalResourcePath==null) {
			InternalResourcePath = CachedPathSubToDBStorage("Edits");
			if (create && !InternalResourcePath.exists()) InternalResourcePath.mkdirs();
		}
		return InternalResourcePath;
	}
	
	boolean unwrapSuffix = true;
			
	protected String SubPathToDBStorage(String extra) {
		String full_Dictionary_fName = _Dictionary_fName;
		int end = full_Dictionary_fName.length();
		StringBuilder sb = AcquireStringBuffer(end+10);
		sb.append(".");
		if(unwrapSuffix){
			int idx = full_Dictionary_fName.lastIndexOf(".");
			if(idx>0){
				end=idx;
			}
		}
		sb.append(full_Dictionary_fName,0, end);
		if(extra!=null){
			sb.append("/").append(extra);
		}
		return sb.toString();
	}
	
	protected String getCleanDictionaryName(String full_Dictionary_fName) {
		if(unwrapSuffix){
			int idx = full_Dictionary_fName.lastIndexOf(".");
			if(idx>0){
				return full_Dictionary_fName.substring(0, idx);
			}
		}
		return full_Dictionary_fName;
	}
	
	protected StringBuilder getCleanDictionaryNameBuilder() {
		String full_Dictionary_fName = _Dictionary_fName;
		int end = full_Dictionary_fName.length();
		StringBuilder sb = AcquireStringBuffer(end+10);
		if(unwrapSuffix){
			int idx = _Dictionary_fName.lastIndexOf(".");
			if(idx>0){
				end=idx;
			}
		}
		sb.append(full_Dictionary_fName,0, end);
		return sb;
	}
	
	public StringBuilder appendCleanDictionaryName(StringBuilder input) {
		if(input==null){
			input = a.MainStringBuilder;
			input.setLength(0);
		}
		String full_Dictionary_fName = _Dictionary_fName;
		if(unwrapSuffix){
			int idx = full_Dictionary_fName.lastIndexOf(".");
			if(idx>0){
				return input.append(full_Dictionary_fName, 0, idx);
			}
		}
		return input.append(full_Dictionary_fName);
	}
	
	protected File PathSubToDBStorage(String extra) {
		return new File(opt.fileToDatabases(), SubPathToDBStorage(extra));
	}
	
	protected CachedDirectory CachedPathSubToDBStorage(String extra) {
		return new CachedDirectory(opt.fileToDatabases(), SubPathToDBStorage(extra));
	}
			
			/** Show Per-Dictionary settings dialog via peruseview, normal view. */
	public static void showDictTweaker(WebViewmy view, Activity context, mdict_manageable...md) {
		if(md.length==0) return;
		mdict_manageable mdTmp = md[0];
		String[] DictOpt = context.getResources().getStringArray(R.array.dict_spec);
		final String[] Coef = DictOpt[0].split("_");
		final View dv = LayoutInflater.from(context).inflate(R.layout.dialog_about,null);
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = dv.findViewById(R.id.title);
		title.setText(R.string.dictOpt1);//词典设定
		title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
		
		title.setTextSize(20f);
		//title.getPaint().setFakeBoldText(true);
		int topad = (int) context.getResources().getDimension(R.dimen._18_);
		((ViewGroup)title.getParent()).setPadding(topad*3/5, topad/2, 0, 0);

		if(GlobalOptions.isLarge) tv.setTextSize(tv.getTextSize());
		tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

		init_clickspan_with_bits_at(view, tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 5, 1,1,md);//背景
		ssb.append("  ");
		init_clickspan_with_bits_at(view, tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1, 4, 1,2,md);//缩放
		
		ssb.append("\r\n\r\n");
		init_clickspan_with_bits_at(view, tv, ssb, DictOpt, 5, Coef, 4, 0, 0x1, 8, 1,4,md);//内容可编辑
		ssb.append("  ");
		init_clickspan_with_bits_at(view, tv, ssb, DictOpt, 4, Coef, 4, 0, 0x1, 7, 1,3,md);//内容可重载
		
		ssb.append("\r\n\r\n");
		init_clickspan_with_bits_at(view, tv, ssb, DictOpt, 6, Coef, 4, 0, 0x1, 1, 1,0,md);//图文分离
		ssb.append("  ");
		init_clickspan_with_bits_at(view, tv, ssb, DictOpt, 3, Coef, 4, 0, 0x1, 10, 1,5,md);//双击放大
		
		ssb.append("\r\n\r\n");
		int start = ssb.length();
		ssb.append("[").append(DictOpt[7]).append("]");
		if(context instanceof AppCompatActivity)
		ssb.setSpan(new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				DictOpitonContainer dialog = new DictOpitonContainer();
				AppCompatActivity a = ((AppCompatActivity)context);
				a.getWindow().getDecorView().setTag(md);
				dialog.show(a.getSupportFragmentManager(), "");
			}},start,ssb.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		tv.setTextSize(17f);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		XYTouchRecorder xyt = mdTmp.getOpt().XYTouchRecorder();
		tv.setOnClickListener(xyt);
		tv.setOnTouchListener(xyt);
		AlertDialog.Builder builder2 = new AlertDialog.Builder(context,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);
		builder2.setView(dv);
		final AlertDialog d = builder2.create();
		d.setCanceledOnTouchOutside(true);

		d.setOnDismissListener(dialog -> {
			boolean doit=true;
			for (mdict_manageable mI : md) {
				mI.checkFlag(context);
				if(doit && mI instanceof mdict){
					((mdict)mI).a.invalidAllPagers();
					doit=false;
				}
			}
		});
		dv.findViewById(R.id.cancel).setOnClickListener(v -> d.dismiss());
		//d.getWindow().setDimAmount(0);
    	//d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		d.show();
		//tofo
		android.view.WindowManager.LayoutParams lp = d.getWindow().getAttributes();  //获取对话框当前的参数值
		lp.height = -2;
		d.getWindow().setAttributes(lp);
	}

	private static void init_clickspan_with_bits_at(WebViewmy view, TextView tv, SpannableStringBuilder text,
													String[] dictOpt, int titleOff, String[] coef, int coefOff, int coefShift, int mask, int flagPosition, int flagMax
			, int processId, mdict_manageable... md) {
		CMN.Log("init_clickspan_with_bits_at", md[0]);
		mdict_manageable mdTmp = md[0];
		boolean isSingle=true;
		int val = (int) ((mdTmp.getFirstFlag()>>flagPosition)&mask);
		for (int i = 1; i < md.length; i++) {
			if(((md[i].getFirstFlag()>>flagPosition)&mask)!=val){
				isSingle=false;
				break;
			}
		}
		int start = text.length();
		int now = start+dictOpt[titleOff].length();
		text.append("[").append(dictOpt[titleOff]);
		if(coef!=null){
			text.append(" :").append(isSingle?coef[coefOff+(val+coefShift)%(flagMax+1)]:"**");
		}
		text.append("]");
		text.setSpan(new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				if(coef==null){
					ConfigSomething(tv, processId, md);
				} else {
					int val = (int) ((mdTmp.getFirstFlag()>>flagPosition)&mask);
					val=(val+1)%(flagMax+1);
					for (mdict_manageable mmTmp : md) {
						mmTmp.validifyValueForFlag(view, val, mask, flagPosition, processId);
					}
					int fixedRange = indexOf(text, ':', now);
					text.delete(fixedRange+1, indexOf(text, ']', fixedRange));
					text.insert(fixedRange+1,coef[coefOff+(val+coefShift)%(flagMax+1)]);
					tv.setText(text);
				}
			}},start,text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	private static void ConfigSomething(TextView tv, int processId, mdict_manageable[] md) {
		if(processId==6){
		
		}
	}
			
	@Override
	public void validifyValueForFlag(WebViewmy mWebView, int val, int mask, int flagPosition, int processId) {
		firstFlag &= ~(mask << flagPosition);
		firstFlag |= (val << flagPosition);
		if(mWebView==null)
			mWebView = this.mWebView;
		isDirty=true;
		if(mWebView!=null) {
			switch(processId){
				case 1:
					if (getUseInternalBG()) {
						if(PDICMainAppOptions.getInheritGlobleWebcolorBeforeSwichingToInternal())
							bgColor = a.GlobalPageBackground;
						if (!(a.isCombinedViewAvtive() && getIsolateImages()))
							mWebView.setBackgroundColor(bgColor);
					} else if (!(a.isCombinedViewAvtive() && getIsolateImages()))
						mWebView.setBackgroundColor(a.GlobalPageBackground);
				break;
				case 2:
					mWebView.getSettings().setTextZoom(getFontSize());
				break;
				case 3:
				case 4:
					refresh_eidt_kit(getContentEditable(), getEditingContents(), true);
				break;
				case 5:
					IBC.firstFlag=firstFlag;
				break;
			}
		}
	}


	public static int indexOf(SpannableStringBuilder text, char cc, int now) {
		for (int i = now; i < text.length(); i++) {
			if(text.charAt(i)==cc){
				return i;
			}
		}
		return -1;
	}

	public boolean isViewInitialized() {
		return viewsHolderReady;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.cover:
				if(false){
					showDictTweaker(mWebView, a, this);
					break;
				}
				if(v.getTag(R.id.toolbar_action1)!=null) {//add It!
					boolean resposible=con==null;
					if(getCon(true).insertUpdate(mWebView.currentPos)!=-1)
						v.setTag(R.id.toolbar_action2,CMN.OccupyTag);
					if(resposible) closeCon();
					v.setTag(R.id.toolbar_action1,null);
					break;
				}
				if(ucc!=null) {//sanity check.
					ucc.setInvoker(this, mWebView, null, null);
					ucc.onClick(v);
				}
			break;
		}
	}

	@Override
	protected ExecutorService OpenThreadPool(int thread_number) {
		return Executors.newFixedThreadPool(thread_number);
		//return Executors.newCachedThreadPool();
		//return Executors.newScheduledThreadPool(thread_number);
		//return Executors.newWorkStealingPool();
	}

	@Override
	protected boolean handleDebugLines(String line) {
		if(!super.handleDebugLines(line)){
			//CMN.Log(line);
			if(line.startsWith("[")){
				line=line.substring(1);
				if(line.equals("图文分离"))
					setIsolateImages(true);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void MoveOrRenameResourceLet(mdictRes md, String token, String pattern, File newPath) {
		File f = md.f();
		String tokee = f().getName();
		if(tokee.startsWith(token) && tokee.charAt(Math.min(token.length(), tokee.length()))=='.'){
			String suffix = tokee.substring(token.length());
			String np = f.getParent();
			if(np!=null && np.equals(np=newPath.getParent())){ //重命名
				File mnp=new File(np, pattern+suffix);
				if(FU.rename5(a, f, mnp)>=0)
					md.Rebase(mnp);
			} else {
				File mnp=new File(np, f.getName());
				if(FU.move3(a, f, mnp)>=0)
					md.Rebase(mnp);
			}
		}
	}

	@Override
	public void Reload() {
		super.Reload();
		if(mWebView!=null) {
			mWebView.setTag(null);
			mWebView.loadUrl("about:blank");
			mWebView.clearCache(false);
			try {
				Field f_mKeyedTags = View.class.getDeclaredField("mKeyedTags");
				f_mKeyedTags.setAccessible(true);
				SparseArray tags = (SparseArray) f_mKeyedTags.get(mWebView);
				tags.clear();
			} catch (Exception e) { CMN.Log(e); }
			if(a.currentDictionary==this) {
				a.adaptermy.notifyDataSetChanged();
				if(a.ActivedAdapter==a.adaptermy) {
					mWebView.awaiting = true;
					mWebView.setVisibility(View.GONE);
					toolbar_title.performClick();
				}
			}
		}
	}

	public void setNestedScrollingEnabled(boolean enabled) {
		if(mWebView!=null){
			((AdvancedNestScrollWebView)mWebView).setNestedScrollingEnabled(enabled);
		}
	}
	
// My life shouldn't waste on these android shits.
// MLSN
	
//	String externalstorage = "content://com.android.externalstorage.documents/document/primary%3A";
//
//	String storagepath = "/storage/emulated/0/";
//
//	@Override
//	protected InputStream mOpenInputStream() throws IOException {
//		//return super.mOpenInputStream()
//		Uri url = Uri.parse(externalstorage+f.getPath().substring(storagepath.length()).replace("/", FU.SLANT));
//
//		url = Uri.parse(externalstorage+Uri.encode(f.getPath().substring(storagepath.length())));
//
//		//url = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2F%E8%8B%B1%E8%AF%AD%E6%9E%84%E8%AF%8D%E6%B3%95.mdx");
//
//		return a.getContentResolver().openInputStream(url);
//	}
//
//	@Override
//	protected boolean StreamAvailable() {
//		return a!=null;
//	}
}

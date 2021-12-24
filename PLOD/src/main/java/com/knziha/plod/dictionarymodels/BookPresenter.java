package com.knziha.plod.dictionarymodels;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;

import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.db.MdxDBHelper;
import com.knziha.plod.dictionary.GetRecordAtInterceptor;
import com.knziha.plod.dictionary.SearchResultBean;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymanager.BookManager;
import com.knziha.plod.dictionarymanager.files.CachedDirectory;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.MainActivityUIBase.UniCoverClicker;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.plaindict.databinding.ContentviewItemBinding;
import com.knziha.plod.widgets.AdvancedNestScrollLinerView;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.Utils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.widgets.XYTouchRecorder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.knziha.metaline.Metaline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.knziha.plod.db.LexicalDBHelper.TABLE_BOOK_NOTE_v2;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_BOOK_v2;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHTYPE_SEARCHINNAMES;
import static com.knziha.plod.dictionary.mdBase.fullpageString;
import static com.knziha.plod.plaindict.MainActivityUIBase.DarkModeIncantation;

/*
 UI side of books / dictionaries
 date:2018.07.30
 author:KnIfER
*/
public class BookPresenter
		implements ValueCallback<String>, OnClickListener, mngr_agent_manageable, View.OnLongClickListener {
	public UniversalDictionaryInterface bookImpl;
	
	public ArrayList<SearchResultBean>[] combining_search_tree2; // 收集词条名称
	public ArrayList<SearchResultBean>[] combining_search_tree_4; // 收集词条文本
	
	public final static String FileTag = "file://";
	public final static String baseUrl = "file:///";
	public final static String  _404 = "<span style='color:#ff0000;'>PlainDict 404 Error:</span> ";
	
	/**</style><script class="_PDict" src="mdbr://SUBPAGE.js"></script>*/
	@Metaline()
	public final static String js="SUBPAGE";
	
	/**
	 	var w=window, d=document;
		var LoadMark, frameAt;
		function _log(...e){console.log('fatal web::'+e)};
	 	w.addEventListener('load',function(e){
			//_log('wrappedOnLoadFunc...');
			var ws = d.body.style;
			_log('mdpage loaded dark:'+(w.rcsp&0x40));
	 		d.body.contentEditable=!1;
	 		_highlight(null);
			var vi = d.getElementsByTagName('video');
			function f(e){
				//_log('begin fullscreen!!! wrappedFscrFunc');
				var se = e.srcElement;
				//if(se.webkitDisplayingFullscreen&&app) app.onRequestFView(se.videoWidth, se.videoHeight);
				if(app)se.webkitDisplayingFullscreen?app.onRequestFView(se.videoWidth, se.videoHeight):app.onExitFView()
			}
			for(var i=0;i<vi.length;i++){if(!vi[i]._fvwhl){vi[i].addEventListener("webkitfullscreenchange", f, false);vi[i]._fvwhl=1;}}
		},false);
		function _highlight(keyword){
			var b1=keyword==null;
			if(b1)
				keyword=app.getCurrentPageKey();
			if(keyword==null||b1&&keyword.trim().length==0)
				return;
			if(!LoadMark) {
	 			function cb(){LoadMark=1;highlight(keyword);}
				try{loadJs('mdbr://markloader.js', cb)}catch(e){w.loadJsCb=cb;app.loadJs(sid.get(),'markloader.js');}
			} else highlight(keyword);
		}
		w.addEventListener('touchstart',function(e){
	 		//_log('fatal wrappedOnDownFunc');
	 		if(!w._touchtarget_lck && e.touches.length==1){
	 			w._touchtarget = e.touches[0].target;
	 		}
			//_log('fatal wrappedOnDownFunc' +w._touchtarget);
		});
		function loadJs(url,callback){
			var script=d.createElement('script');
			script.type="text/javascript";
			if(typeof(callback)!="undefined"){
				script.onload=function(){
					callback();
				}
			}
			script.src=url;
			d.body.appendChild(script);
		}
	 */
	@Metaline(trim = false,compile = false)
	public static byte[] jsBytes=SU.EmptyBytes;
	
	static {
		if (Build.VERSION.SDK_INT<=20) {
			String tmp = new String(jsBytes);
			tmp = tmp.replaceFirst("_log\\(...e\\)", "_log(e)");
			jsBytes = tmp.getBytes();
		}
	}
	
	/** var w=window,d=document;if(!(w.rcsp&0xF00)){w.addEventListener('click',function(e){
	 		//_log('wrappedClickFunc', e.srcElement.id);
	 		var curr=e.srcElement;
	 		if(w.webx){
				if(curr.tagName=='IMG'){
					var img=curr;
					if(img.src && !img.onclick && !(img.parentNode&&img.parentNode.tagName=="A")){
						var lst = [];
						var current=0;
						var all = d.getElementsByTagName("img");
						for(var i=0;i<all.length;i++){
							if(all[i].src){
								lst.push(all[i].src);
								if(all[i]==img)
									current=i;
							}
						}
						if(lst.length==0)
							lst.push(img.src);
						app.openImage(current, e.offsetX/img.offsetWidth, e.offsetY/img.offsetHeight, lst);
					}
				}
				else if(curr.tagName=='A'){
					//_log('fatal in wcl : '+curr.href);
					var href=curr.href+'';
					if(curr.href && href.startsWith('file:///#')){
						curr.href='entry://#'+href.substring(9);
						return false;
					}
	 			}
			}
			_log('popuping...'+w.rcsp);
			if(curr!=d.documentElement && curr.nodeName!='INPUT' && curr.nodeName!='BUTTON' && w.rcsp&0x20 && !curr.noword){
	 			if(w._NWP) {
	 				var p=curr; while((p=p.parentElement))
	 				if(_NWP.indexOf(p)>=0) break;
	 			}
	 			if(w._YWPC) {
	 				var p=curr; while((p=p.parentElement))
	 				if(_YWPC.indexOf(p.className)>=0) break;
	 				if(!p) return;
	 			}
				//todo d.activeElement.tagName
				var s = w.getSelection();
				if(s.isCollapsed && s.anchorNode){ // don't bother with user selection
					s.modify('extend', 'forward', 'word'); // first attempt
					var an=s.anchorNode;
					//_log(s.anchorNode); _log(s);
					//if(true) return;

					if(s.baseNode != d.body) {// immunize blank area
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
								//_log(rrect);
								_log(pX+' ~~ '+pY+' ~~ '+pW+' ~~ '+pH);
								_log(cprX+' :: '+cprY);
	 
								_log(text); // final output
								if(app){
									app.popupWord(sid.get(), text, frameAt, d.documentElement.scrollLeft+pX, d.documentElement.scrollTop+pY, pW, pH);
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
	 	})}
	 */
	@Metaline()
	public final static String tapTranslateLoader=StringUtils.EMPTY;
	
	/**var w=window,d=document;
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
					var sty = d.createElement("style");
					sty.innerHTML = "*{user-select:text !important}";
					d.head.appendChild(sty);
					if(NoneSelectable(tt)) {
						return -1;
					}
				}
				if(fc>0) {
					w._touchtarget=tt;
				}
	 			if(e==0)w._touchtarget_href = tt.getAttribute("href");
				var sel = w.getSelection();
				var range = d.createRange();
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
	 */
	@Metaline()
	public final static String touchTargetLoader=StringUtils.EMPTY;
	
	/** var w=window,d=document;//!!!高亮开始
		var MarkLoad,MarkInst;
		var results=[], current,currentIndex = 0;
		var currentClass = "current";
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
			while(value && value!=d.body){
				top+=value.offsetTop;
				value=value.offsetParent;
			}
			return top;
		}
		function topOffset(elem){
			var top=0;
			var add=1;
			while(elem && elem!=d.body){
				if(!w.webx)if(elem.style.display=='none' || elem.style.display=='' && d.defaultView.getComputedStyle(elem,null).display=='none'){
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
	 		if(!MarkLoad) MarkLoad|=w.MarkLoad;
			if(!MarkLoad){
	 			function cb(){MarkLoad=true; do_highlight(keyword);}
				try{loadJs('mdbr://mark.js', cb)}catch(e){w.loadJsCb=cb;app.loadJs(sid.get(),'mark.js');}
			} else do_highlight(keyword);
		}
		function do_highlight(keyword){
			if(!MarkInst)
				MarkInst = new Mark(d);
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
			 results = d.getElementsByTagName("mark");
			 currentIndex=-1;
			 if(app) app.onHighlightReady(frameAt, results.length);
		 }
	 */
	@Metaline()
	public final static byte[] markJsLoader=SU.EmptyBytes;

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
									if(app)app.popupWord(sid.get(), text, e.clientX, e.clientY, frameAt);
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
	@Metaline
	public final static String SimplestInjection="SUBPAGE";

	/**
	 var styleObj= document.styleSheets[0].cssRules[3].style;
	 styleObj.setProperty("border", "1px solid #FF0000");
	 */
	@Metaline
	public final static String hl_border ="hl_border";

	//public String bookImpl.getFileName()_Internal;
	public WebViewmy mWebView;
    public ViewGroup rl;

	public Drawable cover;

	public static float def_zoom=1;
	public static int def_fontsize = 100;
	public static int optimal100;
	public int tmpIsFlag;
	public ArrayList<myCpr<String, Long>> range_query_reveiver;
	public PlaceHolder placeHolder;
	long FFStamp;
	long firstFlag;
	byte firstVersionFlag;
	public AppHandler mWebBridge;
	public SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
	
	public String currentDisplaying;
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
	@Metaline
	public final static String save_js="ONSAVE";

	public final static String preview_js="document.documentElement.outerHTML";
	
	protected String searchKey;
	protected Cursor PageCursor;
	protected CachedDirectory InternalResourcePath;
	protected boolean bNeedCheckSavePathName;
	public boolean isDirty;
	public boolean editingState=true;
	public final boolean bAutoRecordHistory;
	public static int _req_fvw;
	public static int _req_fvh;
	private CachedDirectory DataBasePath;
	
	final DictionaryAdapter.PLAIN_BOOK_TYPE mType;
	private ContentviewItemBinding mPageView;
	public boolean bSupressingEditing;
	public boolean bViewSource;
	private short maxMatchChars;
	private short minMatchChars;
	private short minParagraphWords;
	protected boolean bReadConfig;
	protected int bIsManagerAgent;
	
	/**
var succ = 0;
function debug(e){console.log(e)};
    debug(111); 123
*/ @Metaline(compile = false)
	private final static String testVal="";
	
	/**几乎肯定是段落，不是单词或成语。**/
	public static boolean testIsParagraph(String searchText, int paragraphWords) {
		if (searchText.length()>15) {
			int words=0;
			int ppos=-1;
			char c;
			boolean white=false;
			for (int v = 0; v < searchText.length(); v++) {
				c=searchText.charAt(v);
				if(c<=' ') {
					if (v>ppos+1) words++;
					ppos=v;
					if(!white&&words>0) white=true;
				}
				if(c>=0x4e00&&c<=0x9fbb) {
					words++;
					if(!white) white=true;
				}
				if (words>=paragraphWords && white) {
					return true;
				}
			}
		}
		return false;
	}
	
	public int getCaseStrategy() {
		return (int) (firstFlag&3);
	}
	
	public void setCaseStrategy(int val) {
		firstFlag&=~3;
		firstFlag|=val;
		bookImpl.setCaseStrategy(val);
	}
	
	public boolean getIsolateImages(){
		//return (firstFlag & 0x2) != 0;
		return false;
	}
	
	public void setIsolateImages(boolean val){
		firstFlag&=~0x2;
		if(val) firstFlag|=0x2;
	}
	/** 内容可重载（检查数据库或文件系统中的重载内容） */
	@Metaline(flagPos=7) public boolean getContentEditable(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=7) public void setContentEditable(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	/** 内容可编辑（处于编辑状态） */
	@Metaline(flagPos=8) public boolean getEditingContents(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=8) public void setEditingContents(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Deprecated
	public void setIsDedicatedFilter(boolean val){
		firstFlag&=~0x40;
		if(val) firstFlag|=0x40;
	}
	
	@Metaline(flagPos=5) public boolean getUseInternalBG(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=5) public void setUseInternalBG(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=4) public boolean getUseInternalFS(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=9) public boolean getUseInternalTBG(){ firstFlag=firstFlag; throw new RuntimeException(); }
	//@Metaline(flagPos=10) public boolean getDoubleClickZoomPage(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=11) public boolean getImageOnly(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	// 12~20
	
	@Metaline(flagPos=21) public boolean getEntryJumpList(){ firstFlag=firstFlag; throw new RuntimeException(); }
	/** Show entry:// target in the popup window (词条跳转到点译弹窗) */
	@Metaline(flagPos=22) public boolean getPopEntry(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=23) public boolean getContentFixedHeightWhenCombined(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=24) public boolean getNoScrollRect(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=25) public boolean getShowToolsBtn(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=25) public void setShowToolsBtn(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=26, shift=1) public boolean getRecordHiKeys(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=27) public boolean getOfflineMode(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=28) public boolean getLimitMaxMinChars(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=29) public boolean getAcceptParagraph(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=29) public void setAcceptParagraph(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=30) public boolean getUseInternalParagraphWords(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=30) public void setUseInternalParagraphWords(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=31, shift=1) public boolean getImageBrowsable(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=31, shift=1) public void setImageBrowsable(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=32) public boolean getAutoFold(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=33) public boolean getDrawHighlightOnTop(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=33) public void setDrawHighlightOnTop(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	
//	public boolean getStarLevel(){
//		0x100000~0x400000
//	}
	
	public boolean getSavePageToDatabase(){
		return true;
	}
	public int bmCBI=0,bmCCI=-1;
	
	public PhotoBrowsingContext IBC = new PhotoBrowsingContext();
	
	private int bgColor;
    private int TIBGColor;
    private int TIFGColor;
	
	
	public int getBgColor() {
		if (!bReadConfig&&bgColor==0)
			return CMN.GlobalPageBackground;
		return bgColor;
	}
	
	public void setBgColor(int value) {
		if (bgColor!=value) {
			bgColor = value;
			isDirty = true;
		}
	}
	
	public int getTitleBGColor() {
		return TIBGColor;
	}
	
	public void setTitleBGColor(int value) {
		if (TIBGColor!=value) {
			TIBGColor = value;
			isDirty = true;
		}
	}
	
	public int getTitleFGColor() {
		return TIFGColor;
	}
	
	public void setTitleFGColor(int value) {
		if (TIFGColor!=value) {
			TIFGColor = value;
			isDirty = true;
		}
	}
	
	
	public short getMaxMatchChars() {
		return maxMatchChars;
	}
	
	public void setMaxMatchChars(short value) {
		if (maxMatchChars!=value) {
			maxMatchChars = value;
			isDirty = true;
		}
	}
	
	public short getMinMatchChars() {
		return minMatchChars;
	}
	
	public void setMinMatchChars(short value) {
		if (minMatchChars!=value) {
			minMatchChars = value;
			isDirty = true;
		}
	}
	
	public short getMinParagraphWords() {
		return minParagraphWords;
	}
	
	public void setMinParagraphWords(short value) {
		if (minParagraphWords!=value) {
			minParagraphWords = value;
			isDirty = true;
		}
	}
	
	public float getDoubleClickZoomRatio() {
		return IBC.doubleClickZoomRatio;
	}
	
	public void setDoubleClickZoomRatio(float value) {
		if (IBC.doubleClickZoomRatio!=value) {
			IBC.doubleClickZoomRatio = value;
			isDirty = true;
		}
	}
	
	public float getDoubleClickOffsetX() {
		return IBC.doubleClickXOffset;
	}
	
	public void setDoubleClickOffsetX(float value) {
		if (IBC.doubleClickXOffset!=value) {
			IBC.doubleClickXOffset = value;
			isDirty = true;
		}
	}
	
	public float getImgDoubleClickZoomLv1() {
		return IBC.doubleClickZoomLevel1;
	}
	
	public void setImgDoubleClickZoomLv1(float value) {
		if (IBC.doubleClickZoomLevel1!=value) {
			IBC.doubleClickZoomLevel1 = value;
			isDirty = true;
		}
	}
	
	public float getImgDoubleClickZoomLv2() {
		return IBC.doubleClickZoomLevel2;
	}
	
	public void setImgDoubleClickZoomLv2(float value) {
		if (IBC.doubleClickZoomLevel2!=value) {
			IBC.doubleClickZoomLevel2 = value;
			isDirty = true;
		}
	}
	
	public float getImgPresetOffsetX() {
		return IBC.doubleClickPresetXOffset;
	}
	
	public void setImgPresetOffsetX(float value) {
		if (IBC.doubleClickPresetXOffset!=value) {
			IBC.doubleClickPresetXOffset = value;
			isDirty = true;
		}
	}
	
	public final static String htmlBase="<!DOCTYPE html><html><meta name='viewport' content='initial-scale=1,user-scalable=yes' class=\"_PDict\"><head><style class=\"_PDict\">html,body{width:auto;height:auto;}img{max-width:100%;}mark{background:yellow;}mark.current{background:orange;border:0px solid #FF0000}";
	public final static String htmlHeadEndTag = "</head>";
	public final static String htmlEnd="</html>";

    public MainActivityUIBase a;
	protected PDICMainAppOptions opt;
	
	public static int hashCode(String toHash, int start) {
		int h=0;
		int len = toHash.length();
		for (int i = start; i < len; i++) {
			h = 31 * h + Character.toLowerCase(toHash.charAt(i));
		}
		return h;
	}
	
	public static ConcurrentHashMap<Long, UniversalDictionaryInterface> bookImplsMap = new ConcurrentHashMap<>();
	
	public static ConcurrentHashMap<String, Long> bookImplsNameMap = new ConcurrentHashMap<>();
	
	//构造
	public BookPresenter(@NonNull File fullPath, MainActivityUIBase THIS, int pseudoInit, Object tag) throws IOException {
		bookImpl = getBookImpl(THIS, fullPath, pseudoInit);
		
		int type = 0;
		if (bookImpl!=null)
		{
			type = bookImpl.getType();
			if (type<0 || type>3)
			{
				type = 0;
			}
		} else if(pseudoInit==0) {
			throw new RuntimeException("Failed To Create Book! "+fullPath);
		}
		mType = DictionaryAdapter.PLAIN_BOOK_TYPE.values()[type];
		
		bAutoRecordHistory = mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB;
		
		if(THIS!=null){
			a = THIS;
			opt = THIS.opt;
		}
		
		if(pseudoInit==1) {
			return;
		}
		//init(getStreamAt(0)); // MLSN
		
		File p = fullPath.getParentFile();
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
		
		if(THIS!=null) {
			readConfigs(THIS, THIS.prepareHistoryCon());
		}
	}
	
	public static void keepBook(MainActivityUIBase THIS, UniversalDictionaryInterface bookImpl) {
		File fullPath = bookImpl.getFile();
		String name = fullPath.getName();
		long bid;
		Long bid_ = bookImplsNameMap.get(name);
		if (bid_==null) {
			bid = THIS.prepareHistoryCon().getBookID(fullPath.getPath(), name);
			if(bid!=-1) bookImplsNameMap.put(name, bid);
		} else {
			bid = bid_;
		}
		if(bid!=-1) bookImplsMap.put(bid, bookImpl);
	}
	
	public static UniversalDictionaryInterface getBookImpl(MainActivityUIBase THIS, File fullPath, int pseudoInit) throws IOException {
		UniversalDictionaryInterface bookImpl = null;
		String pathFull = fullPath.getPath();
		long bid = -1;
		String name = fullPath.getName();
		LexicalDBHelper dataBase = THIS!=null?THIS.prepareHistoryCon():LexicalDBHelper.getInstance();
		if (dataBase!=null) {
			Long bid_ = bookImplsNameMap.get(name);
			if (bid_==null) {
				bid = dataBase.getBookID(fullPath.getPath(), name);
				//CMN.Log("新标识::", bid, name);
				if(bid!=-1) bookImplsNameMap.put(name, bid);
			} else {
				bid = bid_;
			}
			bookImpl = bookImplsMap.get(bid);
		}
		//CMN.Log("getBookImpl", fullPath, bookImpl);
		if ((pseudoInit&3)==0 && bookImpl==null) {
			int sufixp = pathFull.lastIndexOf(".");
			if (sufixp<pathFull.length()-name.length()) sufixp=-1;
			int hash = hashCode(sufixp<0?name:pathFull, sufixp+1);
//			if(sufixp>=0) name = pathFull;
//			int hash=0,i = sufixp+1,len = name.length();
//			for (; i < len; i++) hash = (pseudoInit>>2) * hash + Character.toLowerCase(name.charAt(i));
			switch(hash){
				case 107969:
				case 107949:
				case 3645348:
					if (pathFull.startsWith("/ASSET"))
						bookImpl = new PlainMdictAsset(fullPath, pseudoInit&3, THIS==null?null:THIS.MainStringBuilder, THIS);
					else
						bookImpl = new PlainMdict(fullPath, pseudoInit&3, THIS==null?null:THIS.MainStringBuilder, null, hash==107949);
					break;
				case 117588:
					bookImpl = new PlainWeb(fullPath, THIS);
					break;
				case 110834:
					bookImpl = new PlainPDF(fullPath, THIS);
					break;
				case 115312:
					bookImpl = new PlainText(fullPath, THIS);
					break;
				case 3222:
					bookImpl = new PlainDSL(fullPath, THIS, THIS.taskRecv);
					break;
				case 99773:
					bookImpl = new PlainDSL(fullPath, THIS, THIS.taskRecv);
					break;
				//case 96634189:
				default:
					bookImpl = new DictionaryAdapter(fullPath, THIS);
					break;
				//case 120609:
				//return new mdict_zip(fullPath, THIS);
				//case 3088960:
				//return new mdict_docx(fullPath, THIS);
			}
			if (bookImpl!=null) {
				bookImpl.setBooKID(bid);
				if ((pseudoInit&3)==0 && THIS.getUsingDataV2()) {
					if(bid!=-1) bookImplsMap.put(bid, bookImpl);
				}
			}
		}
		return bookImpl;
	}
	
	protected boolean viewsHolderReady =  false;
	public FlowTextView toolbar_title;
	ViewGroup toolbar;
	public View recess;
	public View forward;
	ImageView toolbar_cover;
	private UniCoverClicker ucc;
	public void initViewsHolder(final MainActivityUIBase a){
		this.a=a;
		ucc = a.getUcc();
		if(!viewsHolderReady) {
			ContentviewItemBinding pageView = ContentviewItemBinding.inflate(a.getLayoutInflater(), a.webholder, false);
			mPageView = pageView;
			rl = (ViewGroup) pageView.getRoot();
	        {
	        	webScale = def_zoom;
	           	AdvancedNestScrollWebView _mWebView = pageView.webviewmy;
				rl.setTag(_mWebView);
				_mWebView.presenter = this;
				_mWebView.setNestedScrollingEnabled(PDICMainAppOptions.getEnableSuperImmersiveScrollMode());
				a.initWebScrollChanged();//Strategy: use one webscroll listener
				//if(!(this instanceof bookPresenter_pdf))
					_mWebView.setOnScrollChangedListener(a.onWebScrollChanged);
	            _mWebView.setPadding(0, 0, 18, 0);
				if(mWebBridge==null) {
					mWebBridge = new AppHandler(this);
				}
				_mWebView.addJavascriptInterface(mWebBridge, "app");
				mWebView = mPageView.webviewmy;
	        }
			refresh_eidt_kit(pageView, mTBtnStates, bSupressingEditing, false);
			setWebLongClickListener(mWebView, a);

			toolbar = pageView.lltoolbar;
			Utils.setOnClickListenersOneDepth(toolbar, this, 999, null);
			
			mWebView.IBC = IBC;
			mWebView.titleBar = (AdvancedNestScrollLinerView) toolbar;
			mWebView.FindBGInTitle(toolbar);
			mWebView.toolbarBG.setColors(mWebView.ColorShade);
			
			//toolbarBG.setColors(ColorSolid);
			mWebView.toolbar_title = toolbar_title = pageView.toolbarTitle;
			toolbar_cover = pageView.cover;
			if(cover!=null)
				toolbar_cover.setImageDrawable(cover);
			//toolbar.setTitle(this.bookImpl.getFileName().split(".mdx")[0]);
			recess = pageView.recess;
			forward = pageView.forward;
			toolbar_title.setText(bookImpl.getDictionaryName());
			viewsHolderReady=true;
		}
		//recess.setVisibility(View.GONE);
		//forward.setVisibility(View.GONE);
	}
	
	
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			case R.id.cover:
				break;
			case R.id.undo:
			case R.id.redo:
				refresh_eidt_kit(mPageView, mTBtnStates, bSupressingEditing = !bSupressingEditing, true);
				break;
			case R.id.save:
			case R.id.tools:
				WebViewmy _mWebView = mWebView;
				String url = currentDisplaying;
				if(v.getParent()!=toolbar){
					if(a.peruseView !=null){
						_mWebView = a.peruseView.mWebView;
						url = a.peruseView.currentDisplaying();
					} else {
						return true;
					}
				}
				OptionListHandlerDyn olhd = new OptionListHandlerDyn(a, _mWebView, url);
				int[] utils = null;
				if (bookImpl instanceof DictionaryAdapter) {
					utils = ((DictionaryAdapter) bookImpl).getPageUtils(false);
				}
				if (utils==null) {
					utils = new int[]{
							R.string.page_yuan
							,R.string.page_source
							,R.string.page_del
							,R.string.page_ucc
					};
				}
				buildStandardOptionListDialog(a, R.string.page_options, 0, utils
						, olhd, url, olhd, 0);
				break;
		}
		return false;
	}
	
	//click
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.cover:
				if(false){
					showDictTweaker(mWebView, a, this);
					break;
				}
				if(ucc!=null) {//sanity check.
					ucc.setInvoker(this, mWebView, null, null);
					ucc.onClick(v);
				}
				break;
			case R.id.undo:
				if(v.getAlpha()==1)mWebView.evaluateJavascript("document.execCommand('Undo')", null);
				break;
			case R.id.redo:
				if(v.getAlpha()==1)mWebView.evaluateJavascript("document.execCommand('Redo')", null);
				break;
			case R.id.save:
				saveCurrentPage(mWebView);
				break;
			case R.id.tools:
				mPageView.save.performLongClick();
				break;
			case R.id.toolbar_title:
				CMN.Log("toolbar_title onClick");
				if(mWebView.getVisibility()!=View.VISIBLE) {
					mWebView.setAlpha(1);
					mWebView.setVisibility(View.VISIBLE);
					if(mWebView.awaiting){
						mWebView.awaiting=false;
						renderContentAt(-1, RENDERFLAG_NEW, -1, null, mWebView.currentRendring);
					}
				}//((View)rl.getParent()).getId()==R.id.webholder
				else if(rl.getParent()==a.webholder) {
					mWebView.setVisibility(View.GONE);
				}
				else {
					toolbar_cover.performClick();
				}
				
				CMN.Log("//tofo 该做的事情");
				mWebView.post(new Runnable() {
					@Override
					public void run() {
						//a.mBar.isWebHeld=true;
						if(a.mBar.timer!=null) a.mBar.timer.cancel();
						//a.mBar.fadeIn();
						a.mBar.setMax(a.webholder.getMeasuredHeight()-a.WHP.getMeasuredHeight());
						a.mBar.setProgress(a.WHP.getScrollY());
						//a.mBar.onTouch(null, MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,0,0,0));
					}
				});
				break;
			case R.id.recess:
			case R.id.forward:
				boolean isGoBack = v.getId() == R.id.recess;
				if (mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
					if (isGoBack) if (mWebView.canGoBack()) mWebView.goBack();
					else if (mWebView.canGoForward()) mWebView.goForward(); return;
				} else {
					mWebView.voyage(isGoBack);
				}
				break;
		}
	}
	
	static void SelectHtmlObject(MainActivityUIBase a, WebViewmy final_mWebView, int source) {
		final_mWebView.evaluateJavascript(touchTargetLoader+"selectTouchtarget("+source+")", new ValueCallback<String>() {
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
						final_mWebView.evaluateJavascript(touchTargetLoader+"restoreTouchtarget()", null);
					}, 300);
				} else {
					final_mWebView.evaluateJavascript(touchTargetLoader+"restoreTouchtarget()", null);
				}
				
			}
		});
	}
	
	public void setDictionaryName(String toString) {
		// nimp
	}
	
	public void updateFile(File f) {
		// nimp
	}
	
	public String GetSearchKey() {
		return searchKey;
	}
	
	public String GetAppSearchKey() {
		return a.getSearchTerm();
	}
	
	public void SetSearchKey(String key) {
		searchKey = key;
	}
	
	public long GetSearchKeyId(String key) {
		return a.GetAddHistory(key);
	}
	
	public int QueryByKey(String keyword, SearchType searchType, boolean isParagraph, int paragraphWords)
	{
		// todo 添加“纯单词”、“段落搜索”词典选项。
		if (!getAcceptParagraph()) {
			if (paragraphWords!=minParagraphWords) {
				isParagraph = testIsParagraph(keyword, minParagraphWords);
			}
			if (isParagraph) {
				return -1;
			}
		}
		if (!isParagraph && getLimitMaxMinChars() && (keyword.length()<minMatchChars||keyword.length()>maxMatchChars)) {
			return -1;
		}
		if (searchType==SearchType.Normal) {
			return bookImpl.lookUp(keyword, true);
		}
		else if (searchType==SearchType.LooseMatch) {
			return bookImpl.lookUp(keyword, false);
		}
		else if (searchType==SearchType.Range) {
			return bookImpl.lookUpRange(keyword, range_query_reveiver, null, bookImpl.getBooKID(),15);
		}
		return -1;
	}
	
	public long getId() {
		return bookImpl.getBooKID();
	}
	
	public PlainWeb getWebx() {
		if (mType == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB)
			return (PlainWeb) bookImpl;
		return null;
	}
	
	public boolean getIsWebx() {
		return mType == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB;
	}
	
	public int getIsManagerAgent() {
		return bIsManagerAgent;
	}
	
	static class OptionListHandler extends ClickableSpan implements DialogInterface.OnClickListener {
		MainActivityUIBase a;
		WebViewmy mWebView;
		String url;
		public OptionListHandler(MainActivityUIBase a, WebViewmy mWebView, String extra) {
			this.a = a;
			this.mWebView = mWebView;
			this.url = extra;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int pos) {
			pos+=IU.parsint(((AlertDialog)dialog).getListView().getTag());
			switch(pos) {
				/* 复制链接 */
				case 0:{
					a.FuzhiText(url);
				} break;
				/* 复制链接文本 */
				case 1:{
					if(url!=null) {
						mWebView.evaluateJavascript("window._touchtarget?window._touchtarget.innerText:''", new ValueCallback<String>() {
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
						SelectHtmlObject(a, mWebView, 0);
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
		public void onClick(DialogInterface dialog, int pos) {
			if (bookImpl instanceof DictionaryAdapter
					&& ((DictionaryAdapter) bookImpl).handlePageUtils(BookPresenter.this, mWebView, pos)) {
				dialog.dismiss();
				return;
			}
			switch (pos) {
				/* 查看原网页 */
				case R.string.page_yuan:{
					editingState=false;
					try {
						renderContentAt(-1, RENDERFLAG_NEW, mWebView.frameAt, mWebView, mWebView.currentRendring);
					} catch (Exception ignored) { }
					editingState=true;
				} break;
				/* 删除重载页面 */
				case R.string.page_del:
					if(mWebView.currentRendring!=null && mWebView.currentRendring.length>1){
						a.showT("错误：多重词条内容不可保存");
						break;
					}
				case 21: {
					String url = getSaveUrl(mWebView);
					if(a.getUsingDataV2()) {
						a.prepareHistoryCon().removePage(bookImpl.getBooKID(), url);
						if(mWebView.fromNet) {
							mWebView.reload();
						} else {
							renderContentAt(-1, RENDERFLAG_NEW, mWebView.frameAt, mWebView, mWebView.currentRendring);
						}
					}
					else {
						getCon(true).enssurePageTable();
						if(url!=null){
							con.removePage(url);
							if(PageCursor!=null) PageCursor.close();
							PageCursor = con.getPageCursor();
							a.notifyDictionaryDatabaseChanged(BookPresenter.this);
						}
						if(pos==1) {
							renderContentAt(-1, RENDERFLAG_NEW, mWebView.frameAt, mWebView, mWebView.currentRendring);
						} else {
							mWebView.reload();
						}
					}
				} break;
				/* 打开中枢 */
				case R.string.page_ucc:{
					mWebView.evaluateJavascript("window.getSelection().isCollapsed", new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							boolean hasSelectionNot = "true".equals(value);
							if (hasSelectionNot) {
								ucc.setInvoker(null, null, null, url);
								ucc.onClick(null);
							} else {
								try {
									View cover = mWebView.titleBar.findViewById(R.id.cover);
									cover.setTag(1);
									cover.performClick();
								} catch (Exception e) { }
							}
						}
					});
				} break;
				/* 保存网页源代码 */
				case R.string.page_baocun:{
					mWebView.evaluateJavascript(preview_js, v1 -> {
						v1 =StringEscapeUtils.unescapeJava(v1.substring(1, v1.length()-1));
						v1 =RemoveApplicationTags(v1);
						StringBuffer sb = opt.pathToMainFolder().append("downloads/").append(mWebView.word)
								.append(".");
						if(pos==10) {
							sb.append( StringUtils.join(mWebView.currentRendring, '|')).append(".");
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
				case R.string.page_source:
					bViewSource=true;
					renderContentAt(-1, RENDERFLAG_NEW, mWebView.frameAt, mWebView, mWebView.currentRendring);
				break;
				/* 添加书签 */
				case R.string.bmAdd: {
					mWebView.presenter.toggleBookMark(mWebView, null, true);
				}
				break;
				/* 词典设置 */
				case 13:
				break;
				default:
				break;
			}
			dialog.dismiss();
		}
		
		@Override
		public void onClick(@NonNull View widget) {
			int[] utils = null;
			if (bookImpl instanceof DictionaryAdapter) {
				utils = ((DictionaryAdapter) bookImpl).getPageUtils(true);
			}
			if (utils==null) {
				utils = new int[]{
					R.string.bmAdd
					,R.string.page_del
					,R.string.page_fuzhi
					,R.string.page_baocun
					,R.string.peruse_mode
				};
			}
			buildStandardOptionListDialog(a, R.string.abc_action_menu_overflow_description
					, 0, utils, this, null, null, 10);
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
			_mWebView.lastLongSX = _mWebView.getScrollX();
			_mWebView.lastLongSY = _mWebView.getScrollY();
			_mWebView.lastLongScale = _mWebView.webScale;
			_mWebView.lastLongX = _mWebView.lastX;
			_mWebView.lastLongY = _mWebView.lastY;
			WebViewmy.HitTestResult result = _mWebView.getHitTestResult();
			if (null == result) return false;
			int type = result.getType();
			CMN.Log("getHitTestResult", type, result.getExtra());
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
													URL requestURL = new URL(url);
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
							, null, olh, url, olh, 0);
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
	
	public static void buildStandardOptionListDialog(MainActivityUIBase a, int title, int array, int[] strIds, DialogInterface.OnClickListener onItemSelected, String titletail, ClickableSpan clickableSpan, int tag) {
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
				.setSingleChoiceItems(array, 0, onItemSelected)
				.setSingleChoiceItems(strIds, 0, onItemSelected)
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

	public void saveCurrentPage(WebViewmy mWebView) {
//		if (true) {
//			a.showT("功能关闭，请等待5.0版本。");
//			return;
//		}
		if(mWebView.currentRendring!=null && mWebView.currentRendring.length>1){
			a.showT("错误：多重词条内容不可保存");
			return;
		}
		if(getSavePageToDatabase()) {
			if(!a.getUsingDataV2()) {
				getCon(true).enssurePageTable();
			}
		}
		String url=getSaveUrl(mWebView);
		if(url!=null && url.length()>0)
		mWebView.evaluateJavascript(save_js, v -> {
			if(v!=null && v.startsWith("\"")) {
				v=StringEscapeUtils.unescapeJava(v.substring(1,v.length()-1));
				v=RemoveApplicationTags(v);
				CMN.Log("结果Html，", v);
				//CMN.Log("结果长度，", v.length()); CMN.Log("");
				String title=currentDisplaying;
				if(mWebView!=this.mWebView && a.peruseView !=null)
					title=a.peruseView.currentDisplaying();
				SaveCurrentPage_Internal(v, url, mWebView.currentPos, title);
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

	@Nullable public String getSaveUrl(WebViewmy mWebView) {
		String url;
		if(a.getUsingDataV2()) {
			if (mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
				url=mWebView.getUrl();
				String host = ((PlainWeb)bookImpl).host;
				if (url!=null && url.startsWith(host)) {
					url = url.substring(host.length());
				}
			} else {
				url=bookImpl.getEntryAt(mWebView.currentPos);
			}
		} else {
			url = ""+mWebView.currentPos;
		}
		return url;
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
	private void SaveCurrentPage_Internal(String data, String url, long position, String name) {
		/* save to database */
		CMN.Log("SavePage::", url);
		boolean proceed=true;
		if(getSavePageToDatabase()) {
			proceed=false;
			try {
				try {
					if(a.getUsingDataV2()) {
						long nid=a.prepareHistoryCon().putPage(bookImpl.getBooKID(), url, position, name, data);
						if (nid != -1) {
							String succStr = true?a.getResources().getString(R.string.saved_with_size, data.length())
									:a.getResources().getString(R.string.saved);
							a.showT(succStr);
							onPageSaved();
							a.putRecentBookMark(bookImpl.getBooKID(), nid, position);
						} else {
							a.showT("保存失败 ");
						}
					} else {
						if (con.putPage(url, name, data) != -1) {
							String succStr = true?a.getResources().getString(R.string.saved_with_size, data.length()):a.getResources().getString(R.string.saved);
							a.showT(succStr);
							onPageSaved();
						} else {
							a.showT("保存失败 ");
						}
					}
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
	
	byte mTBtnStates;
	
	final static byte TBTN_UNDOREDO=0x1;
	final static byte TBTN_SAVE=0x2;
	final static byte TBTN_TOOL=0x4;
	final static byte TBTN_SUPRESSEDIT=0x8;
	protected void refresh_eidt_kit(ContentviewItemBinding pageView, byte btnStates, boolean supressingEditing, boolean updateWeb) {
		if(pageView!=null) {
			byte targetStats = 0;//(byte) 0xFF;
			if (getEditingContents()&&getContentEditable()) targetStats|=TBTN_UNDOREDO;
			if (getContentEditable()) targetStats|=TBTN_SAVE;
			if (getShowToolsBtn()) targetStats|=TBTN_TOOL;
			if (supressingEditing) targetStats|=TBTN_SUPRESSEDIT;
			boolean editable = (targetStats&TBTN_UNDOREDO)!=0;
			if (btnStates!=targetStats) {
				pageView.undo.setVisibility(editable?View.VISIBLE:View.GONE);
				pageView.redo.setVisibility(editable?View.VISIBLE:View.GONE);
				if (editable && (supressingEditing==(pageView.undo.getAlpha()==1))) {
					pageView.undo.setAlpha(supressingEditing?0.5f:1);
					pageView.redo.setAlpha(supressingEditing?0.5f:1);
				}
				pageView.save.setVisibility((targetStats&TBTN_SAVE)!=0?View.VISIBLE:View.GONE);
				pageView.tools.setVisibility((targetStats&TBTN_TOOL)!=0?View.VISIBLE:View.GONE);
				if (pageView==this.mPageView) {
					mTBtnStates = targetStats;
				}
				else if(a.peruseView !=null) {
					a.peruseView.mTBtnStates = targetStats;
				}
				if(updateWeb && pageView.getRoot().getParent()!=null)
					pageView.webviewmy.evaluateJavascript(editable&&!supressingEditing ? MainActivityUIBase.ce_on : MainActivityUIBase.ce_off, null);
			}
		}
	}

	public boolean isJumping = false;

	//public int currentPos;
	public int internalScaleLevel=-1;
	public int lvPos,lvClickPos,lvPosOff;
	/** Current Page Historian */
	public SparseArray<ScrollerRecord> HistoryOOP = new SparseArray<>();

    @SuppressLint("JavascriptInterface")
	public void setCurrentDis(WebViewmy mWebView, long idx, int... flag) {
		if(flag==null || flag.length==0) {//书签跳转等等
			mWebView.addHistoryAt(idx);
		}
		/* 回溯 或 前瞻， 不改变历史 */
		mWebView.word = currentDisplaying = StringUtils.trim(bookImpl.getEntryAt(mWebView.currentPos = idx));
		
		if(bookImpl.hasVirtualIndex()){
			if (idx==0 && mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB && searchKey!=null) {
				currentDisplaying = searchKey;
			} else {
				int tailIdx=currentDisplaying.lastIndexOf(":");
				if(tailIdx>0)
					currentDisplaying=currentDisplaying.substring(0, tailIdx);
			}
		}
		StringBuilder title_builder = bookImpl.AcquireStringBuffer(64);
    	toolbar_title.setText(title_builder.append(currentDisplaying.trim()).append(" - ").append(bookImpl.getDictionaryName()).toString());

		if(mWebView.History.size()>2){
			recess.setVisibility(View.VISIBLE);
			forward.setVisibility(View.VISIBLE);
		}
	}
	
	
	public void checkTint() {
		if(mWebView!=null) {
			tintBackground(mWebView);
			refresh_eidt_kit(mPageView, mTBtnStates, bSupressingEditing, false);
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
			CMN.Log("使用内置标题栏颜色：", useInternal, bookImpl.getDictionaryName(), isDark, Integer.toHexString(myWebColor));
			int colorTop = PDICMainAppOptions.getTitlebarUseGradient()?ColorUtils.blendARGB(myWebColor, Color.WHITE, 0.08f):myWebColor;
			int[] ColorShade = mWebView.ColorShade;
			if(ColorShade[1]!=myWebColor||ColorShade[0]!=colorTop){
				ColorShade[1] = myWebColor;
				ColorShade[0] = colorTop;
				if(useInternal) {
					toolbarBG = mWebView.MutateBGInTitle();
				}
				toolbarBG.setColors(ColorShade);
				//CMN.Log("设置了?");
			}
			myWebColor = isDark?Color.WHITE:useInternal?TIFGColor:opt.getTitlebarForegroundColor();
			mWebView.setTitlebarForegroundColor(myWebColor);
		}
		//CMN.pt("设置颜色：");
	}
	
	public final static int RENDERFLAG_NEW=0x1;

	//todo frameAt=-1
    public void renderContentAt(float initialScale, int fRender, int frameAt, WebViewmy mWebView, long... position){
    	CMN.Log("renderContentAt!!!...", bookImpl.getDictionaryName());
    	if (a==null) {
    		// safe check
    		return;
		}
    	isJumping=false;
    	if(mWebView==null) {
    		mWebView=this.mWebView;
			refresh_eidt_kit(mPageView, mTBtnStates, bSupressingEditing, true);
		} else if(a.peruseView != null && mWebView==a.peruseView.mWebView) {
			a.peruseView.mPageView.save.setOnLongClickListener(this);
			refresh_eidt_kit(a.peruseView.mPageView, a.peruseView.mTBtnStates, a.peruseView.bSupressingEditing, false);
		}
		boolean resposibleForThisWeb=mWebView==this.mWebView;
    	
    	if (!resposibleForThisWeb && mWebView.presenter!=this) {
			mWebView.presenter = this;
		}
	
		int from = mWebView.fromCombined;
		mWebView.fromNet=mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB;
		mWebView.active=true;
		boolean fromCombined = from==1;
		boolean fromPopup = from==2;

		boolean mIsolateImages=/*resposibleForThisWeb&&*/from==0&&getIsolateImages();
		/* 为了避免画面层叠带来的过度重绘，网页背景保持透明？。 */
		tintBackground(mWebView);

		if((fRender&RENDERFLAG_NEW)!=0){
			//mWebView.SelfIdx=SelfIdx;
			//mWebView.setTag(mWebView.SelfIdx=SelfIdx);
			//if(resposibleForThisWeb) rl.setTag(bookImpl);
			//todo 是否记忆临时的折叠状态？
			//todo 是否为常规的页面开放连续的历史纪录？
			mWebView.clearIfNewADA(resposibleForThisWeb?null:this); // todo ???
			mWebView.currentPos=position[0];
			mWebView.currentRendring=position;
			if(frameAt>=0) mWebView.frameAt = frameAt;
			//CMN.Log("折叠？？？", frameAt, mWebView.frameAt, getDictionaryName());
			mWebView.awaiting = false;
			if(/*resposibleForThisWeb && */fromCombined && frameAt>=0
					&& (PDICMainAppOptions.getTmpIsCollapsed(tmpIsFlag) || getAutoFold()
							|| /*frameAt>0 && */PDICMainAppOptions.getDelaySecondPageLoading()
											|| PDICMainAppOptions.getOnlyExpandTopPage() && frameAt+1>=opt.getExpandTopPageNum()    )){/* 自动折叠 */
				mWebView.awaiting = true;
				mWebView.setVisibility(View.GONE);
				setCurrentDis(mWebView, mWebView.currentPos);
				CMN.Log("折叠！！！", mWebView.frameAt);
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
				if (getContentFixedHeightWhenCombined()) {
					mWebView.getLayoutParams().height = a.root.getHeight();
				} else {
					mWebView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				}
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
	
		//todo 处理 fromCombined && opt.getAlwaysShowScrollRect()
		mWebView.SetupScrollRect(!getNoScrollRect() && (!fromCombined&&opt.getAlwaysShowScrollRect() || fromCombined&&getContentFixedHeightWhenCombined()));

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
		StringBuilder sb = bookImpl.AcquireStringBuffer(512);
		sb.append(htmlBase);
		//todo 插入 同名 css 文件？
		sb.append(js);
		return sb;
	}
	boolean test = true;
	public void renderContentAt_internal(WebViewmy mWebView,float initialScale, boolean fromCombined, boolean fromPopup, boolean mIsolateImages, long...position) {
		mWebView.isloading=true;
		mWebView.currentPos = position[0];
		//if(!a.AutoBrowsePaused&&a.background&&PDICMainAppOptions.getAutoBrowsingReadSomething())
		//	mWebView.resumeTimers();
    	String htmlCode = null ,JS=null;
		try {
			if(bookImpl.hasVirtualIndex())
				try {
					String validifier = getOfflineMode()&&getIsWebx()?null:bookImpl.getVirtualTextValidateJs(this, mWebView, position[0]);
					//CMN.Log("validifier::", validifier, GetSearchKey(), mWebView.getTag());
					if (validifier == null
							//|| true // 用于调试直接网页加载
							|| "forceLoad".equals(mWebView.getTag())) {
						htmlCode = bookImpl.getVirtualRecordsAt(this, position);
						mWebView.setTag(null);
						CMN.Log("htmlCode::", htmlCode);
						if (htmlCode!=null && htmlCode.startsWith("http")) {
							// 如果是加载网页
							mWebView.loadUrl(htmlCode);
							htmlCode = null;
						}
					} else {
						mWebView.evaluateJavascript(validifier, new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
								//CMN.Log("validifier::onReceiveValue::", value);
								if ("1".equals(value) || "true".equals(value)) {
									String effectJs = bookImpl.getVirtualTextEffectJs(position);
									if (effectJs!=null) mWebView.evaluateJavascript(effectJs, null);
									//a.showT("免重新加载生效！");
									vartakelayaTowardsDarkMode(mWebView);
								}  else if("2".equals(value) && getIsWebx()) { // apply js modifier first, then do search
									if (!"schVar".equals(mWebView.getTag())) {
										mWebView.setTag("schVar");
										SetSearchKey(GetAppSearchKey());
										renderContentAt_internal(mWebView, initialScale, fromCombined, fromPopup, mIsolateImages, 0);
										mWebView.setTag(null);
									}
								} else {
									mWebView.setTag("forceLoad");
									renderContentAt_internal(mWebView, initialScale, fromCombined, fromPopup, mIsolateImages, position);
								}
							}
						});
//						mWebView.evaluateJavascript(testVal, new ValueCallback<String>() {
//							@Override
//							public void onReceiveValue(String value) {
//								CMN.Log("validifier::onReceiveValue1::", value);
//							}
//						});
						return;
					}
				} catch (Exception e) {
					CMN.Log(e);
				}
			else
			{
				htmlCode = bookImpl.getRecordsAt(mBookRecordInteceptor, position);
				if (bViewSource) {
					htmlCode = StringEscapeUtils.escapeHtml3(htmlCode); // 调试，查看源码
					bViewSource = false;
				}
			}
		}
		catch (Exception e) {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(s));
			htmlCode=_404+e.getLocalizedMessage()+"<br>"+s;
			CMN.Log(s);
		}

    	//CMN.Log("缩放是", initialScale);
		if(initialScale!=-1)
			mWebView.setInitialScale((int) (100*(initialScale/ BookPresenter.def_zoom)*opt.dm.density));//opt.dm.density
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
		if(htmlCode!=null) {
			if (!htmlCode.startsWith(fullpageString)) {
				AddPlodStructure(mWebView, htmlBuilder, fromPopup, mIsolateImages);
				LoadPagelet(mWebView, htmlBuilder, htmlCode);
			}
			else {
				CMN.Log("fullpageString");
				int headidx = htmlCode.indexOf("<head>");
				boolean b1 = headidx == -1;
				if (true) {
					if (b1) {
						htmlBuilder.append("<head>");
					} else {
						htmlBuilder.append(htmlCode, 0, headidx + 6);
					}
					AddPlodStructure(mWebView, htmlBuilder, fromPopup, mIsolateImages);
					htmlBuilder.append(getSimplestInjection());
					if (b1) {
						htmlBuilder.append("</head>");
						htmlBuilder.append(htmlCode);
					} else {
						htmlBuilder.append(htmlCode, headidx + 6, htmlCode.length());
					}
					htmlCode = htmlBuilder.toString();
				}
				mWebView.loadDataWithBaseURL(baseUrl, htmlCode, null, "UTF-8", null);
			}
		} else if(JS!=null) {
			mWebView.evaluateJavascript(JS, null);
		}
	}
	
	public void EvaluateValidifierJs(String validifier1, WebViewmy mWebView) {
		mWebView.evaluateJavascript(validifier1, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				//CMN.Log("validifier::onReceiveValue::1", value);
				if ("1".equals(value) || "true".equals(value)) {
					String effectJs = bookImpl.getVirtualTextEffectJs(mWebView.currentRendring);
					if (effectJs!=null) mWebView.evaluateJavascript(effectJs, null);
					//a.showT("免重新加载生效！");
					vartakelayaTowardsDarkMode(mWebView);
				}  else if("2".equals(value) && getIsWebx()) { // apply js modifier first, then do search
					if (!"schVar".equals(mWebView.getTag())) {
						mWebView.setTag("schVar");
						SetSearchKey(GetAppSearchKey());
						renderContentAt_internal(mWebView, -1, mWebView.fromCombined==1, mWebView==a.popupWebView, false, 0);
						mWebView.setTag(null);
					}
				}
			}
		});
	}
	
	public void vartakelayaTowardsDarkMode(WebViewmy mWebView) {
		if(mWebView==null) mWebView=this.mWebView;
		boolean dark = GlobalOptions.isDark||opt.getInDarkMode();
		String GetById = "document.getElementById('_PDict_Darken')";
		WebViewmy webview = mWebView;
		mWebView.evaluateJavascript(GetById + "?1:0", new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				if (dark ^ "1".equals(value)) {
					tintBackground(webview);
					if (dark) {
						webview.evaluateJavascript(DarkModeIncantation, null);
					} else {
						webview.evaluateJavascript(GetById+".remove()", null);
					}
				}
			}
		});
	}
	
	public void TakeHistoryRecord(String key) {
		if (mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
			((PlainWeb)bookImpl).takeHistoryRecord(this, key);
		}
	}
	
	/**
	 @font-face
	 {
		 font-family: 'Kingsoft Phonetic Plain';
		 src: url('android_asset/fonts/myfontmyfont.ttf');
	 }
	 */
	@Metaline
	final static String cssfont = "sss";

	/** Let's call and call and call and call!!! */
	public void AddPlodStructure(WebViewmy mWebView, StringBuilder htmlBuilder, boolean fromPopup, boolean mIsolateImages) {
    	//CMN.Log("MakeRCSP(opt)??", MakeRCSP(opt),MakeRCSP(opt)>>5);
		htmlBuilder.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
		if(bookImpl.hasMdd()) htmlBuilder.append("<p class='MddExist'/>");
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
		htmlBuilder.append("window.rcsp=").append(rcsp).append(";");
		htmlBuilder.append("frameAt=").append(mWebView.frameAt).append(";");
		//nimp
		//if(!(this instanceof bookPresenter_web))
		{
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
		if (bookImpl instanceof DictionaryAdapter)
			return ((DictionaryAdapter) bookImpl).getSimplestInjection();
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
	
	public GetRecordAtInterceptor mBookRecordInteceptor = new GetRecordAtInterceptor() {
		@Override
		public String getRecordAt(UniversalDictionaryInterface bookImpl, long position) {
			if(position==-1) {
				return new StringBuilder(getAboutString())
						.append("<BR>").append("<HR>")
						.append(bookImpl.getDictInfo()).toString();
			}
			if(editingState && getContentEditable()){//Todo save and retrieve via sql database
				CachedDirectory cf = getInternalResourcePath(false);
				boolean ce =  cf.cachedExists();
				File p = ce?new File(cf, Long.toString(position)):null;
				boolean pExists = ce && p.exists();
				//retrieve page from database
				if(getSavePageToDatabase()) {
					String url=Long.toString(position);
					if(a.getUsingDataV2()) {
						url = bookImpl.getEntryAt(position);
						long note_id = a.prepareHistoryCon().containsPage(bookImpl.getBooKID(), url);
						if(note_id!=-1) {
							//a.showT("有笔记！！！");
							String ret = a.prepareHistoryCon().getPageString(bookImpl.getBooKID(), url);
							//CMN.Log(ret);
							return ret;
//						if(!pExists) return con.getPageString(url, StandardCharsets.UTF_8);
//						else {
//							Object[] results=con.getPageAndTime(url);
//							if(results!=null) {
//								/* 删除文件，除非文件最近修改过。 */
//								/* 只在此处删除文件。 */
//								/* xian baocun dao shujuku ->  baocun dao wenjian  */
//								/* --> canliu laojiu de shuju. Duqu shi, bu yu li hui.*/
//								/* xian baocun dao wenjian -> baocun dao shujuku  */
//								/* --> canliu laojiu de wenjian. Duqu shi shanchu */
//								if ((long) results[1] > p.lastModified()) {
//									p.delete();
//									return new String((byte[])results[0]);
//								}
//							}
//						}
						}
					} else {
						getCon(true).enssurePageTable();
						con.preparePageContain();
						if(con.containsPage(url)) {
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
				}
				//retrieve page from file system
				if(pExists){
					return BU.fileToString(p);
				}
			}
			return null;
		}
	};
	
	public String getAboutString() {
		//return Build.VERSION.SDK_INT>=24?Html.fromHtml(_header_tag.get("Description"),Html.FROM_HTML_MODE_COMPACT).toString():Html.fromHtml(_header_tag.get("Description")).toString();
		String ret=bookImpl.getRichDescription();
		if(ret==null) ret="";
		return StringEscapeUtils.unescapeHtml3(ret);
	}
	
	public InputStream getWebPage(String url) {
		if (getContentEditable() && mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
			PlainWeb webx = (PlainWeb) bookImpl;
			if(url.startsWith(webx.host)) {
				url=url.substring(webx.host.length());
			}
			CMN.Log("getWebPage::", url);
			return a.prepareHistoryCon().getPageStream(getId(), url);
		}
		return null;
	}
	
	public String getWebPageString(String url) {
		if (getContentEditable() && mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
			PlainWeb webx = (PlainWeb) bookImpl;
			if(url.startsWith(webx.host)) {
				url=url.substring(webx.host.length());
			}
			CMN.Log("getWebPage::", url);
			return a.prepareHistoryCon().getPageString(getId(), url);
		}
		return null;
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

	// not used
	//public boolean containsResourceKey(String skey) {
	//	if(mdd!=null)
	//	for(mdictRes mddTmp:mdd){
	//		if(mddTmp.lookUp(skey)>=0)
	//			return true;
	//	}
	//	return  false;
	//}

	public String getLexicalEntryAt(int position) {
		return position>=0&&position<bookImpl.getNumberEntries()?bookImpl.getEntryAt(position):"Error!!!";
	}

	public boolean hasCover() {
		return cover!=null;
	}

	@Override
	public boolean isMddResource() {
		return bookImpl.getIsResourceFile();
	}

	@Override
	public void checkFlag(Toastable_Activity context) {
		if(FFStamp!=firstFlag || isDirty)
			saveStates(context, context.prepareHistoryCon());
	}

	@Override
	public long getFirstFlag() {
		return firstFlag;
	}
	
	@Override
	public void setFirstFlag(long val){
		CMN.Log("setFirstFlag::");
		if (firstFlag!=val) {
			IBC.firstFlag = firstFlag = val;
			checkTint();
			isDirty = true;
		}
	}

	@Override
	public PDICMainAppOptions getOpt() {
		return opt;
	}

	public Object[] getSoundResourceByName(String canonicalName) throws IOException {
		return bookImpl.getSoundResourceByName(canonicalName);
	}
	
	public void setToolbarTitleAt(long pos) {
		String entry = pos>=-1?bookImpl.getEntryAt(pos).trim():currentDisplaying;
		StringBuilder sb = bookImpl.AcquireStringBuffer(entry.length()+bookImpl.getDictionaryName().length()+5);
		sb.append(entry).append(" - ");
		appendCleanDictionaryName(sb);
		toolbar_title.setText(sb.toString());
	}
	
	public String getCharsetName() {
		return bookImpl.getCharsetName();
	}
	
	@SuppressWarnings("unused")
    public static class AppHandler {
		BookPresenter presenter;

        @JavascriptInterface
        public void log(String val) {
        	CMN.Log(val);
        }
        
        @JavascriptInterface
        public void loadJs(long sid, String name) {
			WebViewmy wv = presenter.findWebview(sid);
			CMN.Log("loadJs::", name, wv!=null);
			if (wv!=null) {
				wv.post(new Runnable() {
					@Override
					public void run() {
						wv.evaluateJavascript(presenter.a.getCommonAsset(name), new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
								wv.evaluateJavascript("if(window.loadJsCb)loadJsCb()", null);
							}
						});
					}
				});
			}
        }

        float scale;
        DisplayMetrics dm;

        public AppHandler(BookPresenter presenter) {
			try {
				this.presenter = presenter;
				scale = GlobalOptions.density;
				if (presenter!=null) {
					dm = presenter.a.dm;
				}
			} catch (Exception ignored) { }
			if (scale==0) scale = 1;
		}

        @JavascriptInterface
        public void openImage(int position, float offsetX, float offsetY, String... img) {
			if(presenter==null || !presenter.getImageBrowsable()) return;
        	//CMN.Log(position, img, mdx.bookImpl.getFileName()_Internal);
        	//CMN.Log("openImage::", offsetX, offsetY);
			MainActivityUIBase aa = presenter.a;
			AgentApplication app = ((AgentApplication) aa.getApplication());
			app.mdd = null;
			if (presenter.bookImpl instanceof mdict) {
				app.mdd = ((mdict) presenter.bookImpl).getMdd();
			}
			app.IBC = presenter.IBC;
			app.opt = presenter.opt;
			app.Imgs = img;
			app.currentImg = position;
			//todo
			WebViewmy mWebView = presenter.mWebView;
			app.IBC.lastX = offsetX;
			app.IBC.lastY = offsetY;
			aa.root.postDelayed(aa.getOpenImgRunnable(), 100);
		}

        @JavascriptInterface
        public void scrollHighlight(int o, int d) {
        	if(presenter==null) return;
			presenter.a.scrollHighlight(o, d);
        }

        @JavascriptInterface
        public String getCurrentPageKey() {
			if(presenter==null) return "";
			return presenter.a.getCurrentPageKey();
		}

        @JavascriptInterface
        public int getDeviceHeight(){
			if(presenter==null) return 100;
			presenter.a.getWindowManager().getDefaultDisplay().getMetrics(dm);
    		//return (int) (1.0f*dm.heightPixels/ scale + 0.5f);
    		return (int) (dm.heightPixels/scale*1.5);
    		//return (int) (dm.heightPixels);
        }
        @JavascriptInterface
        public float getDeviceRatio(){
			if(presenter==null) return 1;
    		DisplayMetrics dm = new DisplayMetrics();
			presenter.a.getWindowManager().getDefaultDisplay().getMetrics(dm);
    		return 1.0f*dm.heightPixels/dm.widthPixels;
        }

        @JavascriptInterface
        public void showTo(int val) {
        	if(true) return;
        }

        @JavascriptInterface
        public void onAudioPause() {
			if(presenter==null) return;
			if(!presenter.a.opt.supressAudioResourcePlaying)
				presenter.a.onAudioPause();
        }

        @JavascriptInterface
        public void onAudioPlay() {
			if(presenter==null) return;
        	if(!presenter.a.opt.supressAudioResourcePlaying)
        		presenter.a.onAudioPlay();
        }

        @JavascriptInterface
        public void jumpHighlight(int d) {
			if(presenter==null) return;
			presenter.a.jumpHighlight(d, true);
        }

        @JavascriptInterface
        public void onHighlightReady(int idx, int number) {
			if(presenter==null) return;
			presenter.a.onHighlightReady(idx, number);
        }

        @JavascriptInterface
        public void popupWord(long sid, String key, int frameAt, float pX, float pY, float pW, float pH) {
			if(presenter==null) return;
        	if(WebViewmy.supressNxtClickTranslator) {
        		return;
			}
        	MainActivityUIBase a = presenter.a;
			a.popupWord(key, null, frameAt);
			if(frameAt>=0 && pH!=0){
				if(pW==0) pW=pH;
				if(RLContainerSlider.lastZoomTime == 0 || System.currentTimeMillis() - RLContainerSlider.lastZoomTime > 500){
					//Utils.setFloatTextBG(new Random().nextInt());
					WebViewmy wv = presenter.findWebview(sid);
					//CMN.Log("只管去兮不管来", wv!=null);
					if(wv!=null){
						float density = dm.density;
						wv.highRigkt_set(pX*density, pY*density, (pX+pW)*density, (pY+pH)*density);
					}
				}
			}
        }

        @JavascriptInterface
        public void popupClose() {
			if(presenter==null) return;
        	if(this!= presenter.a.popuphandler)
				presenter.a.postDetachClickTranslator();
        }

		@JavascriptInterface
		public void parseContent(int processed, int total, String contents) {
			if(presenter==null) return;
        	//nimp
			//if(mdx instanceof bookPresenter_pdf && mdx.a instanceof PDICMainActivity){
			//	CMN.Log("parseContent", contents, mdx.bookImpl.getDictionaryName());
			//	//((PDICMainActivity)mdx.a).parseContent(mdx, contents);
			//	bookPresenter_pdf pdx = ((bookPresenter_pdf) mdx);
			//	pdx.pdf_index=contents.split("\n");
			//	mdx.a.lv.post(new Runnable() {
			//		@Override
			//		public void run() {
			//			((BasicAdapter)mdx.a.lv.getAdapter()).notifyDataSetChanged();
			//			mdx.a.showT("目录提取完成！("+processed+"/"+total+")");
			//		}
			//	});
			//}
		}

		@JavascriptInterface
		public void banJs(boolean banIt) {
			if(presenter==null) return;
			WebViewmy wv = presenter.mWebView;
			//wv.getSettings().setJavaScriptEnabled(!banIt);
		}

		@JavascriptInterface
		public void collectWord(String word) {
			if(presenter==null) return;
			presenter.a.showT(word+" 已收藏");
		}

		@JavascriptInterface
		public void ReadText(long sid, String word) {
			if(presenter==null) return;
			presenter.a.ReadText(word, presenter.findWebview(sid));
		}

		public void setDict(BookPresenter ccd) {
        	if(ccd!=null) {
				presenter =ccd;
				if(dm==null) {
					dm=ccd.a.dm;
				}
			}
		}

		@JavascriptInterface
		public void setTTS() {
			if(presenter==null) return;
			presenter.a.root.post(() -> presenter.a.showTTS());
		}

		@JavascriptInterface
		public void snack(String val) {
			if(presenter==null) return;
			presenter.a.root.post(() -> presenter.a.showContentSnack(val));
		}

		@JavascriptInterface
		public void onRequestFView(int w, int h) {
			if(presenter==null) return;
        	//CMN.Log("onRequestFView", w, h, w>h);
			_req_fvw=w;
			_req_fvh=h;
			if(presenter.a.opt.getFullScreenLandscapeMode()==2)
				((Handler) presenter.a.hdl).sendEmptyMessage(7658942);
		}
		
		@JavascriptInterface
		public void onExitFView() {
			if(presenter==null) return;
        	//CMN.Log("onExitFView");
			MainActivityUIBase.CustomViewHideTime = System.currentTimeMillis();
			((Handler) presenter.a.hdl).sendEmptyMessageDelayed(7658941, 600);
		}
		
		// sendup
		@JavascriptInterface
		public void knock(long sid) {
			//if(layout==a.currentViewImpl)
			{
				//upsended = true;
				WebViewmy view = presenter.findWebview(sid);
//				view.postDelayed(new Runnable() {
//					@Override
//					public void run() {
						long time = CMN.now();
						float lastX = (view.lastLongX + view.lastLongSX)/view.lastLongScale*view.webScale - view.getScrollX();
						float lastY = (view.lastLongY + view.lastLongSY)/view.lastLongScale*view.webScale - view.getScrollY();
						MotionEvent evt = MotionEvent.obtain(time, time,MotionEvent.ACTION_DOWN, lastX, lastY, 0);
						view.dispatchTouchEvent(evt);
						evt.setAction(MotionEvent.ACTION_UP);
						view.dispatchTouchEvent(evt);
						//view.dispatchTouchEvent(evt);
						evt.recycle();
//					}
//				}, 0);
			}
		}
		
		@JavascriptInterface
		public void knock1(long sid, int x, int y) {
			//if(layout==a.currentViewImpl)
			{
				//upsended = true;
				WebViewmy view = presenter.findWebview(sid);
//				view.postDelayed(new Runnable() {
//					@Override
//					public void run() {
						long time = CMN.now();
						float lastX = x*view.webScale - view.getScrollX();
						float lastY = y*view.webScale - view.getScrollY();
						lastX=20;
						lastY+=35;
						CMN.Log("knock1", x, y, lastX, lastY);
						MotionEvent evt = MotionEvent.obtain(time, time,MotionEvent.ACTION_DOWN, lastX, lastY, 0);
						
						evt.setAction(MotionEvent.ACTION_DOWN);
						view.dispatchTouchEvent(evt);
						evt.setAction(MotionEvent.ACTION_UP);
						view.dispatchTouchEvent(evt);
						
						
						evt.setAction(MotionEvent.ACTION_DOWN);
						view.dispatchTouchEvent(evt);
						evt.setAction(MotionEvent.ACTION_UP);
						view.dispatchTouchEvent(evt);
						view.dispatchTouchEvent(evt);
						evt.recycle();
//					}
//				}, 1200);
			}
		}
	}
	
	private WebViewmy findWebview(long sid) {
		if (mWebView!=null && mWebView.getSimpleIdentifier()==sid)
			return mWebView;
		if (a.PeruseViewAttached() && a.peruseView.mWebView.getSimpleIdentifier()==sid) {
			return a.peruseView.mWebView;
		}
		if (a.popupWebView!=null && a.popupWebView.getSimpleIdentifier()==sid) {
			return a.popupWebView;
		}
		return mWebView;
	}
	
	public boolean renameFileTo(Context c, File newF) {
		//tofo_tofo
//		File fP = newF.getParentFile();
//		fP.mkdirs();
//		boolean ret = false;
//		boolean pass = !f.exists();
//		String bookImpl.getFileName()_InternalOld = "."+bookImpl.getFileName();
//		if(fP.exists() && fP.isDirectory()) {
//			int retret = FU.rename(a, f, newF);
//			Log.d("XXX-ret",""+retret);
//			//Log.e("XXX-ret",f.getParent()+"sad"+newF.getParent());
//			String oldName = bookImpl.getFileName();
//			if(retret==0) {
//				bookImpl.getFileName() = newF.getName();
//				/* 重命名资源文件 */
//				ret = true;
//		    	if(mdd!=null)
//				for(mdictRes mddTmp:mdd){
//					File mddF = mddTmp.f();
//					String fn = BU.unwrapMddName(mddF.getName());
//					if(fn.startsWith(bookImpl.getFileName())){
//						fn=fn.substring(bookImpl.getFileName().length());
//						File newMdd = new File(fP,bookImpl.getFileName()+fn+".mdd");
//						if(mddF.exists()) {
//							int ret1 = FU.rename(a, mddF, newMdd);
//							if (ret1 == 0 && mdd != null) {
//								mddTmp.updateFile(newMdd);
//							}
//						}
//					}
//				}
//				else if(new File(fP,bookImpl.getFileName()+".mdd").exists()) {
//					try {
//						mdd = Collections.singletonList(new mdictRes(new File(fP, bookImpl.getFileName() + ".mdd")));
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
//		new File(opt.pathToDatabases().append(bookImpl.getFileName()_InternalOld).toString()).renameTo(new File(opt.pathToDatabases().append("."+bookImpl.getFileName()).toString()));
//
//		if(a.currentDictionary==this)
//			a.setLastMdFn(bookImpl.getFileName());
//		return ret;
		return false;
	}

	@Override
	public boolean exists() {
		return bookImpl.getFile().exists();
	}

	@Override
	public boolean equalsToPlaceHolder(PlaceHolder placeHolder) {
		String ThisPath = bookImpl.getFile().getPath();
		String LibPath = opt.lastMdlibPath.getPath();
		String OtherPath = placeHolder.pathname;
		if(!OtherPath.startsWith("/") && ThisPath.startsWith(LibPath)){
			return ThisPath.regionMatches(LibPath.length()+1, OtherPath, 0, OtherPath.length());
		}
		return placeHolder.getPath(opt).equals(bookImpl.getFile());
	}

	public boolean moveFileTo(Context c, File newF) {
		boolean ret = false;
		//File fP = newF.getParentFile();
		//fP.mkdirs();
		//if(fP.exists() && fP.isDirectory()) {
		//	int retret = FU.move3(a, f, newF);
		//	CMN.Log("XXX-ret",""+retret);
		//	if(retret>=0) {
		//		bookImpl.getFileName() = newF.getName();
		//		String cleanbookImpl.getFileName() = getCleanDictionaryName(bookImpl.getFileName());
		//		/* 移动资源文件 */
		//		ret = true;
		//		if(mdd!=null)
		//			for(mdictRes mddTmp:mdd){
		//				File mddF = mddTmp.f();
		//				String fn = mddF.getName();
		//				if(fn.startsWith(cleanbookImpl.getFileName())){
		//					fn=fn.substring(cleanbookImpl.getFileName().length());
		//					File newMdd = new File(fP,cleanbookImpl.getFileName()+fn);
		//					if(mddF.exists()) {
		//						int ret1 = FU.rename(a, mddF, newMdd);
		//						if (ret1 == 0 && mdd != null) {
		//							mddTmp.updateFile(newMdd);
		//						}
		//					}
		//				}
		//			}
		//		else {
		//			File mddNew = new File(fP,cleanbookImpl.getFileName()+".mdd");
		//			if(mddNew.exists()) {
		//				try {
		//					mdd = Collections.singletonList(new mdictRes(mddNew));
		//					a.showT("找到了匹配的mdd！");
		//				} catch (IOException e) {
		//					e.printStackTrace();
		//				}
		//			}
		//		}
		//		f=newF;
		//	}
		//	else if(retret==-123) {
		//		a.showT("错误：不恰当的分隔符");
		//	}
		//}
		//if(a.currentDictionary==this){
		//	opt.putLastMdFn("LastPlanName", bookImpl.getFileName());
		//}
		return ret;
	}

	public void unload() {
		//CMN.Log("unload::", this);
		if (a!=null && isDirty)
			saveStates(a, a.prepareHistoryCon());
		if(mWebView!=null) {
			mWebView.shutDown();
    		mWebView=null;
		}
		if(viewsHolderReady) {
			viewsHolderReady =  false;
			ucc=null;
			toolbar_cover.setOnClickListener(null);
			toolbar_cover = null;
			toolbar_title = null;
			toolbar = null;
			recess = null;
			forward = null;
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
	
	@Override
	public void setTmpIsFlag(int val) {
		tmpIsFlag = val;
	}
	
	public void saveStates(Context context, LexicalDBHelper historyCon) {
		if(mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_EMPTY) return;
		setIsDedicatedFilter(false);
		try {
			DataOutputStream data_out;
			byte[] data;
			
			String save_name = bookImpl.getDictionaryName();
			
			ReusableByteOutputStream bos = new ReusableByteOutputStream(bookImpl.getOptions(), MainActivityUIBase.ConfigSize + MainActivityUIBase.ConfigExtra);
			data = bos.getBytes();
			bos.precede(MainActivityUIBase.ConfigExtra);
			data_out = new DataOutputStream(bos);
			
			data_out.writeByte(0);
			data_out.writeByte(0);
//			data_out.writeByte((int) (255*IBC.doubleClickXOffset));
//			data_out.writeByte((int) (255*IBC.doubleClickPresetXOffset));
			data_out.writeByte(0);
			data_out.writeInt(bgColor);
			data_out.writeInt(internalScaleLevel);
			// 调试
			boolean d = PDICMainAppOptions.getSimpleMode()||true;
			data_out.writeInt(d?0:lvPos);
			data_out.writeInt(lvClickPos);
			data_out.writeInt(d?0:lvPosOff);
			//CMN.Log("保存列表位置",lvPos,lvClickPos,lvPosOff, bookImpl.getDictionaryName());
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
			//CMN.Log(bookImpl.getDictionaryName()+"保存页面位置",record.x,record.y,webScale);
			data_out.writeLong(firstFlag);
			data_out.writeInt(TIBGColor);
			data_out.writeInt(TIFGColor);
			data_out.writeFloat(IBC.doubleClickZoomRatio);
			data_out.writeFloat(IBC.doubleClickZoomLevel1);
			data_out.writeFloat(IBC.doubleClickZoomLevel2);
			
			data_out.writeByte(firstVersionFlag);
			
			data_out.writeShort(maxMatchChars);
			data_out.writeShort(minMatchChars);
			data_out.writeShort(minParagraphWords);
			
			//CMN.Log("saved::minMatchChars::", minMatchChars, maxMatchChars);
			
			data_out.writeFloat(IBC.doubleClickXOffset);
			data_out.writeFloat(IBC.doubleClickPresetXOffset);
			
			data_out.close();
			
			/* Just mark as dirty. */
			//if(!CMN.bForbidOneSpecFile && a!=null){
			//	CMN.Log("putted", save_name);
			//	a.dirtyMap.add(save_name);
			//}
			//UIProjects.put(save_name, data);
			putBookOptions(context, historyCon, bookImpl.getBooKID(), bos.getBytesLegal(MainActivityUIBase.ConfigSize), bookImpl.getFile().getPath(), save_name);
			isDirty = false;
		} catch (Exception e) { if(GlobalOptions.debug) CMN.Log(e); }
	}
	
	public float webScale=0;

	public void readConfigs(Context context, LexicalDBHelper historyCon) throws IOException {
		DataInputStream data_in1 = null;
		if(context==null) return;
		try {
			CMN.rt();
			byte[] data = bookImpl.getOptions();
			if(data==null) data=getBookOptions(context, historyCon, bookImpl.getBooKID(), bookImpl.getFile().getPath(), bookImpl.getDictionaryName());
			if(data!=null) {
				bookImpl.setOptions(data);
				int extra = MainActivityUIBase.ConfigExtra;
				data_in1 = new DataInputStream(new ByteArrayInputStream(data, extra, data.length-extra));
			} else {
				bookImpl.setOptions(new byte[MainActivityUIBase.ConfigSize]);
			}
			if(data_in1!=null) {
				//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
				//IBC.doubleClickXOffset = ((float)Math.round(((float)data_in1.read())/255*1000))/1000;
				//IBC.doubleClickPresetXOffset = ((float)Math.round(((float)data_in1.read())/255*1000))/1000;
				data_in1.read();
				data_in1.read();
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
				ScrollerRecord record = new ScrollerRecord(data_in1.readInt()
						, data_in1.readInt()
						, webScale = data_in1.readFloat());
				avoyager.put(lvClickPos, record);
				firstFlag |= data_in1.readLong();
				CMN.Log(bookImpl.getDictionaryName(), firstFlag, "列表位置",lvPos,lvClickPos,lvPosOff);
				//CMN.Log(bookImpl.getDictionaryName()+"页面位置",record.x,record.y,webScale);
				TIBGColor = data_in1.readInt();
				TIFGColor = data_in1.readInt();
				IBC.doubleClickZoomRatio = data_in1.readFloat();
				IBC.doubleClickZoomLevel1  = data_in1.readFloat();
				IBC.doubleClickZoomLevel2  = data_in1.readFloat();
				firstVersionFlag = data_in1.readByte();
				// 3 + (9+4)*4 + 8 + 1 = 64
				maxMatchChars = data_in1.readShort();
				minMatchChars = data_in1.readShort();
				minParagraphWords = data_in1.readShort();
				// 70
				IBC.doubleClickXOffset = data_in1.readFloat();
				IBC.doubleClickPresetXOffset = data_in1.readFloat();
				// 78
			}
			CMN.pt(bookImpl.getDictionaryName()+" id="+bookImpl.getBooKID()+" "+data+" 单典配置加载耗时");
		} catch (Exception e) {
			CMN.Log(e);
			//firstFlag = 0;
		} finally {
			FFStamp = firstFlag;
			if(data_in1!=null) data_in1.close();
		}
		if(PDICMainAppOptions.getSimpleMode()||true) {
			// 调试
			lvPos=0;lvPosOff=0;
		}
		bReadConfig = true;
		IBC.firstFlag = firstFlag;
		boolean b1=IBC.doubleClickZoomRatio==0;
		if(b1) {
			/* initialise values */
			IBC.doubleClickZoomRatio=2.25f;
			TIBGColor = PDICMainAppOptions.getTitlebarUseGlobalUIColor()?CMN.MainBackground:opt.getTitlebarBackgroundColor();
			TIFGColor = opt.getTitlebarForegroundColor();
		}
		if ((firstVersionFlag&0x1)==0)
		{
			TIBGColor = PDICMainAppOptions.getTitlebarUseGlobalUIColor()?CMN.MainBackground:opt.getTitlebarBackgroundColor();
			TIFGColor = opt.getTitlebarForegroundColor();
			CMN.Log("初始化词典设置");
			if (getIsWebx()) {
				setShowToolsBtn(true);
				setImageBrowsable(false);
				setAcceptParagraph(getWebx().getIsTranslator());
				setDrawHighlightOnTop(getWebx().getDrawHighlightOnTop());
			}
			if(b1)IBC.setPresetZoomAlignment(3);
			IBC.doubleClickPresetXOffset = 0.12f;
			IBC.doubleClickXOffset = 0.12f;
			minMatchChars = 1;
			maxMatchChars = 20;
			minParagraphWords = 8;
			bgColor=CMN.GlobalPageBackground;
			
			firstVersionFlag|=0x1;
			isDirty = true;
		}
		if (getIsWebx()) {
			if (PDICMainAppOptions.bCheckVersionBefore_5_4)
			{
				setDrawHighlightOnTop(getWebx().getDrawHighlightOnTop());
				isDirty = true;
			}
		}
	}
	
	public static byte[] getBookOptions(Context context, LexicalDBHelper db, long book_id, String path, String name) {
		if (db.testDBV2) {
			if (book_id==-1) {
				book_id = db.getBookID(path, name);
			}
			db.preparedGetBookOptions.bindLong(1, book_id);
			try {
				ParcelFileDescriptor fd = db.preparedGetBookOptions.simpleQueryForBlobFileDescriptor();
				if(fd==null) {
					CMN.Log("THIS FILE DESCRIPTOR IS NULL!!!", book_id, path, name);
					return null;
				}
				FileInputStream fin = new FileInputStream(fd.getFileDescriptor());
				ReusableByteOutputStream out = new ReusableByteOutputStream(Math.max(fin.available(), MainActivityUIBase.ConfigSize));
				out.write(fin, true);
				fin.close();
				//CMN.Log("getBytesLegal::", out.getBytesLegal()==out.getBytes(), out.getBytesLegal().length, out.getBytes().length);
				return out.getBytesLegal(MainActivityUIBase.ConfigSize);
			} catch (Exception e) {
				CMN.Log("THIS  IS NULL!!!", book_id, path, name);
				CMN.Log(e);
			}
		}
		return ((AgentApplication)context.getApplicationContext()).BookProjects.get(name);
	}
	
	public static void putBookOptions(Context context, LexicalDBHelper db, long book_id, byte[] options, String path, String name) {
		if (db.testDBV2) {
			try {
				SQLiteDatabase db_ = db.getDB();
				ContentValues values = new ContentValues();
				values.put("options", options);
				values.put("path", path);
				if (book_id==-1) {
					book_id = db.getBookID(path, name);
				}
				db_.update(TABLE_BOOK_v2, values, "id=?", new String[]{""+book_id});
				CMN.Log("已存放::", book_id, name);
				return;
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		((AgentApplication)context.getApplicationContext()).BookProjects.put(name, options);
	}
	
	public int getFontSize() {
		if(getUseInternalFS())
			return internalScaleLevel>0?internalScaleLevel:(internalScaleLevel=def_fontsize);
		return def_fontsize;
	}
	
	public interface ViewLayoutListener{
		void onLayoutDone(int size);
	}
	
	public ViewLayoutListener vll;

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
		String fullFileName = bookImpl.getDictionaryName();
		int end = fullFileName.length();
		StringBuilder sb = bookImpl.AcquireStringBuffer(end+10);
		sb.append(".");
		if(unwrapSuffix){
			int idx = fullFileName.lastIndexOf(".");
			if(idx>0){
				end=idx;
			}
		}
		sb.append(fullFileName,0, end);
		if(extra!=null){
			sb.append("/").append(extra);
		}
		return sb.toString();
	}
	
	protected String getCleanDictionaryName(String fullFileName) {
		if(unwrapSuffix){
			int idx = fullFileName.lastIndexOf(".");
			if(idx>0){
				return fullFileName.substring(0, idx);
			}
		}
		return fullFileName;
	}
	
	protected StringBuilder getCleanDictionaryNameBuilder() {
		String fullFileName = bookImpl.getDictionaryName();
		int end = fullFileName.length();
		StringBuilder sb = bookImpl.AcquireStringBuffer(end+10);
		if(unwrapSuffix){
			int idx = bookImpl.getDictionaryName().lastIndexOf(".");
			if(idx>0){
				end=idx;
			}
		}
		sb.append(fullFileName,0, end);
		return sb;
	}
	
	public StringBuilder appendCleanDictionaryName(StringBuilder input) {
		if(input==null){
			input = a.MainStringBuilder;
			input.setLength(0);
		}
		String fullFileName = bookImpl.getDictionaryName();
		if(unwrapSuffix){
			int idx = fullFileName.lastIndexOf(".");
			if(idx>0){
				return input.append(fullFileName, 0, idx);
			}
		}
		return input.append(fullFileName);
	}
	
	protected File PathSubToDBStorage(String extra) {
		return new File(opt.fileToDatabases(), SubPathToDBStorage(extra));
	}
	
	protected CachedDirectory CachedPathSubToDBStorage(String extra) {
		return new CachedDirectory(opt.fileToDatabases(), SubPathToDBStorage(extra));
	}
			
	/** Show Per-Dictionary settings dialog via peruseview, normal view. */
	public static void showDictTweaker(WebViewmy view, Toastable_Activity context, BookPresenter...md) {
		if(md.length==0) return;
		mngr_agent_manageable mdTmp = md[0];
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
		ssb.setSpan(new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				if(context instanceof MainActivityUIBase) ((MainActivityUIBase) context).showBookPreferences(md);
				else if(context instanceof BookManager) ((BookManager) context).showBookPreferences(md);
			}},start,ssb.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		tv.setTextSize(17f);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		XYTouchRecorder xyt = mdTmp.getOpt().XYTouchRecorderInstance();
		tv.setOnClickListener(xyt);
		tv.setOnTouchListener(xyt);
		AlertDialog.Builder builder2 = new AlertDialog.Builder(context,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);
		builder2.setView(dv);
		final AlertDialog d = builder2.create();
		d.setCanceledOnTouchOutside(true);

		d.setOnDismissListener(dialog -> {
			MainActivityUIBase a=null;
			for (BookPresenter datum : md) {
				datum.checkFlag(context);
				if (a==null && datum.getIsManagerAgent()==0) {
					a=datum.a;
				}
			}
			if(a!=null){
				a.invalidAllPagers();
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
			, int processId, mngr_agent_manageable... md) {
		//CMN.Log("init_clickspan_with_bits_at", md[0]);
		mngr_agent_manageable mdTmp = md[0];
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
					for (mngr_agent_manageable mmTmp : md) {
						mmTmp.getFirstFlag();
						mmTmp.validifyValueForFlag(view, val, mask, flagPosition, processId);
					}
					int fixedRange = indexOf(text, ':', now);
					text.delete(fixedRange+1, indexOf(text, ']', fixedRange));
					text.insert(fixedRange+1,coef[coefOff+(val+coefShift)%(flagMax+1)]);
					tv.setText(text);
				}
			}},start,text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	private static void ConfigSomething(TextView tv, int processId, mngr_agent_manageable[] md) {
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
					refresh_eidt_kit(mPageView, mTBtnStates, bSupressingEditing, true);
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
	
	public boolean hasBookmark(WebViewmy mWebView) {
		String entryName = getSaveUrl(mWebView);
		if (entryName==null) return false;
		SQLiteStatement stat = a.prepareHistoryCon().preparedHasBookmarkForEntry;
		stat.bindString(1, entryName);
		stat.bindLong(2, getId());
		try {
			stat.simpleQueryForLong();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void deleteBookMark(int pos, OnClickListener notifier) {
		long bid = bookImpl.getBooKID();
		long id=-1;
		try {
			SQLiteDatabase database = a.prepareHistoryCon().getDB();
			boolean hasNotes=false;
			String[] where = new String[]{""+bid, ""+pos};
			boolean insertNew=true;
			Cursor c = database.rawQuery("select id,notes is not null from "+TABLE_BOOK_NOTE_v2+" where bid=? and pos=?", where);
			if(c.moveToFirst()) {
				insertNew = false;
				id = c.getLong(0);
				hasNotes = c.getInt(1)==1;
			}
			c.close();
			
			if(insertNew) {
				id=-1;
			} else {
				String id_ = ""+id;
				if (hasNotes) {
					id=0;
					new AlertDialog.Builder(a)
							.setMessage("该书签含有重载笔记内容，确认删除？")
							.setPositiveButton("确认", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									sayToggleBookMarkResult(database.delete(TABLE_BOOK_NOTE_v2, "id=?", new String[]{id_})>0?-2:-1, notifier);
								}
							})
							.setNegativeButton("取消", null)
							.show();
				} else {
					id=database.delete(TABLE_BOOK_NOTE_v2, "id=?", new String[]{id_})>0?-2:-1;
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		sayToggleBookMarkResult((int) id, notifier);
	}
	
	public void toggleBookMark(WebViewmy mWebView, OnClickListener notifier, boolean forceAdd) {
		long id=-1;
		if (mWebView!=null) {
			long bid = bookImpl.getBooKID();
			CMN.rt();
			String url = getSaveUrl(mWebView);
			if(url==null) return;
			CMN.Log("toggleBookMark::url::", url);
			try {
				SQLiteDatabase database = a.prepareHistoryCon().getDB();
				boolean hasNotes=false;
				String[] where = new String[]{""+url, ""+bid};
				boolean insertNew=true;
				Cursor c = database.rawQuery("select id,notes is not null from "+TABLE_BOOK_NOTE_v2+" where lex = ? and bid = ? limit 1", where);
				if(c.moveToFirst()) {
					insertNew = false;
					id = c.getLong(0);
					hasNotes = c.getInt(1)==1;
				}
				c.close();
				
				if(insertNew||forceAdd) {
					if (insertNew) {
						ContentValues values = new ContentValues();
						values.put("lex", url);
						values.put("bid", bid);
						values.put("pos", mWebView.currentPos);
						long now = CMN.now();
						values.put("last_edit_time", now);
						values.put("creation_time", now);
						id = database.insert(TABLE_BOOK_NOTE_v2, null, values);
					}
					if (forceAdd) {
						a.showT(R.string.bmAdded);
					}
					a.putRecentBookMark(bid, id, mWebView.currentPos);
				} else {
					String id_ = ""+id;
					if (hasNotes) {
						id=0;
						new AlertDialog.Builder(a)
								.setMessage("该书签含有重载笔记内容，确认删除？")
								.setPositiveButton("确认", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										sayToggleBookMarkResult(database.delete(TABLE_BOOK_NOTE_v2, "id=?", new String[]{id_})>0?-2:-1, notifier);
									}
								})
								.setNegativeButton("取消", null)
								.show();
					} else {
						id=database.delete(TABLE_BOOK_NOTE_v2, "id=?", new String[]{id_})>0?-2:-1;
					}
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		sayToggleBookMarkResult((int) id, notifier);
	}
	
	private void sayToggleBookMarkResult(int ret, OnClickListener notifier) {
		if (ret>=1) {
			a.showT("已保存书签");
		} else if(ret==-2) {
			a.showT("已删除书签");
		} else if(ret==-1){
			a.showT("操作失败！");
			return;
		} else if(ret==0){
			return;
		}
		if (notifier!=null) {
			notifier.onClick(null);
		}
	}
	
	//@Override
	//protected boolean handleDebugLines(String line) {
	//	if(!super.handleDebugLines(line)){
	//		//CMN.Log(line);
	//		if(line.startsWith("[")){
	//			line=line.substring(1);
	//			if(line.equals("图文分离"))
	//				setIsolateImages(true);
	//			return true;
	//		}
	//	}
	//	return false;
	//}

	public void Reload(Object context) {
		bookImpl.Reload(context);
		if(mWebView!=null) {
			if(opt.getReloadWebView()) {
				ViewGroup rl = this.rl;
				WebViewmy wv = this.mWebView;
				viewsHolderReady = false;
				mTBtnStates = 0;
				initViewsHolder(a);
				mWebView.fromCombined = wv.fromCombined;
				if (Utils.replaceView(this.rl, rl).getParent()!=null) {
					renderContentAt(-1, RENDERFLAG_NEW, wv.frameAt, null, wv.currentRendring);
				}
				checkTint();
				wv.shutDown();
			} else {
				mWebView.setTag(null);
				mWebView.loadUrl("about:blank");
				//mWebView.clearCache(false);
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
			// todo peruse view
		}
	}

	public void setNestedScrollingEnabled(boolean enabled) {
		if(mWebView!=null){
			((AdvancedNestScrollWebView)mWebView).setNestedScrollingEnabled(enabled);
		}
	}
	
// My life shouldn't waste on these android stuffs.
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
	
 	public DictionaryAdapter.PLAIN_BOOK_TYPE getType(){
		return mType;
	};
	
	@Override
	public File f() {
		return bookImpl.getFile();
	}
	
	
	@Override
	public String getPath() {
		return bookImpl.getFile().getPath();
	}
	
	@Override
	public String getDictionaryName() {
		return bookImpl.getDictionaryName();
	}
	
	public void purgeSearch(int searchType) {
		if (bookImpl instanceof mdict) {
			((mdict)bookImpl).searchCancled = false;
		}
		ArrayList<SearchResultBean>[] searchTree = searchType == SEARCHTYPE_SEARCHINNAMES ? combining_search_tree2 : combining_search_tree_4;
		if (searchTree != null) {
			for (int ti = 0; ti < searchTree.length; ti++) {//遍历搜索结果
				if (searchTree[ti] != null)
					searchTree[ti].clear();
			}
		}
	}
	
	public void findAllNames(String searchTerm, int adapter_idx, PDICMainActivity.AdvancedSearchLogicLayer searchLayer) throws IOException {
		bookImpl.flowerFindAllKeys(searchTerm, adapter_idx, searchLayer);
	}
	
	public void findAllTexts(String searchTerm, int adapter_idx, PDICMainActivity.AdvancedSearchLogicLayer searchLayer) throws IOException {
		bookImpl.flowerFindAllContents(searchTerm, adapter_idx, searchLayer);
	}
}

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
import android.text.Html;
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
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.db.MdxDBHelper;
import com.knziha.plod.dictionary.GetRecordAtInterceptor;
import com.knziha.plod.dictionary.SearchResultBean;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.AutoCloseInputStream;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.Base64;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.SubStringKey;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymanager.BookManager;
import com.knziha.plod.dictionarymanager.files.CachedDirectory;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.MainActivityUIBase.VerseKit;
import com.knziha.plod.plaindict.MdictServer;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.plaindict.databinding.ContentviewItemBinding;
import com.knziha.plod.widgets.AdvancedNestScrollLinerView;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.ViewUtils;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.InflaterOutputStream;

import static com.knziha.plod.db.LexicalDBHelper.TABLE_BOOK_NOTE_v2;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_BOOK_v2;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHTYPE_SEARCHINNAMES;
import static com.knziha.plod.dictionary.mdBase.fullpageString;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_DSL;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_EMPTY;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_MDICT;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_TEXT;
import static com.knziha.plod.plaindict.MainActivityUIBase.DarkModeIncantation;

import io.noties.markwon.Markwon;

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
	//public final static String baseUrl = "file:///";
	public final static String baseUrl = "http://mdbr.com/base.html";
	public final String mBaseUrl;
	public final String idStr;
	public final String idStr10;
	public final static String  _404 = "<span style='color:#ff0000;'>PlainDict 404 Error:</span> ";
	
	/**</style><script class="_PDict" src="//mdbr/SUBPAGE.js"></script>*/
	@Metaline()
	public final static String js="SUBPAGE";
	
	/**
	 var imgs = document.getElementsByTagName('IMG');
	 for(var i=0;i<imgs.length;i++){
		 if(imgs[i].src.startsWith("file://"))
		 imgs[i].src = imgs[i].src.substring(7);
		 if(imgs[i].src.startsWith("/"))
		 imgs[i].src = imgs[i].src.substring(1);
	 }*/
	@Metaline()
	public final static String jsFileTag="";
	
	
	/**app.viewChanged(sid.get(), entryKey, frameAt);*/
	@Metaline()
	public final static String jsViewChanged ="";
	
	/**
	 	var w=window, d=document;
		var LoadMark, frameAt;
		function _log(a,b,c){console.log('fatal web::',a,b,c)};
	 	w.addEventListener('load',function(e){
			_log('wrappedOnLoadFunc...');
			var ws = d.body.style;
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
				try{loadJs('//mdbr/markloader.js', cb)}catch(e){w.loadJsCb=cb;app.loadJs(sid.get(),'markloader.js');}
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
	@Metaline()
	public static byte[] jsBytes=SU.EmptyBytes;
	
	public final static String tapTranslateLoader=StringUtils.EMPTY;
	
	/**if(!window.webpc)window.addEventListener('click',window.webpc=function(e) {
		//_log('wrappedClickFunc 1', e.srcElement.id);
		if(e.srcElement.tagName==='IMG'){
			var img=e.srcElement;
			if(img.src && !img.onclick && !(img.parentNode&&img.parentNode.tagName=="A")){
				var lst = [];
				var current=0;
				var all = this.document.getElementsByTagName("img");
				for(var i=0;i<all.length;i++) {
					if(all[i].src) {
						lst.push(all[i].src);
						if(all[i]==img)
							current=i;
					}
				}
				if(lst.length==0)
					lst.push(img.src);
				app.openImage(sid.get(), current, e.offsetX/img.offsetWidth, e.offsetY/img.offsetHeight, lst);
			}
		}
	 }, false, true)*/
	@Metaline()
	public final static String imgLoader =StringUtils.EMPTY;
	
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
	 			if(w.subw) w=w.subw;
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
	public static float max_zoom=1;
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
	protected boolean suppressingLongClick;
	public boolean isDirty;
	public boolean editingState=true;
	public final boolean bAutoRecordHistory;
	public static int _req_fvw;
	public static int _req_fvh;
	private CachedDirectory DataBasePath;
	
	final DictionaryAdapter.PLAIN_BOOK_TYPE mType;
	public final PlainWeb mWebx;
	public final PlainMdict mMdict;
	public final boolean isWebx;
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
	
	public boolean isHasExtStyle() {
		return hasExtStyle;
	}
	
	private boolean hasExtStyle;
	
	
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
	@Metaline(flagPos=9) public boolean getUseTitleBackground(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=9) public void setUseTitleBackground(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=10) public boolean getUseTitleForeground(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=10) public void setUseTitleForeground(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=11) public boolean getImageOnly(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	// 12~20

//	public boolean getStarLevel(){
//		0x100000~0x400000  20~22
//	}
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
	
	
	@Metaline(flagPos=34, shift=1) public boolean checkVersionBefore_5_4() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=34, shift=1) public void uncheckVersionBefore_5_4(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}


	@Metaline(flagPos=35) public boolean isMergedBook() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=35) public void isMergedBook(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=36) public boolean getEntryJumpList(){ firstFlag=firstFlag; throw new RuntimeException(); }
	/** 词条跳转到点译弹窗 (entry://) */
	@Metaline(flagPos=37) public boolean getPopEntry(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=38) public boolean hasFilesTag() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=38) public void hasFilesTag(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}

	@Metaline(flagPos=39, shift=1) public boolean getUseHosts() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=39, shift=1) public void setUseHosts(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}

	@Metaline(flagPos=40) public boolean getUseMirrors() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=40) private void setUseMirrorsInternal(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}

	@Metaline(flagPos=41, flagSize=5) public int getMirrorIdx() { firstFlag=firstFlag; throw new RuntimeException();}

	
	public boolean getSavePageToDatabase(){
		return true;
	}
	public int bmCBI=0,bmCCI=-1;
	
	public PhotoBrowsingContext IBC = new PhotoBrowsingContext();
	
	private int bgColor;
    private int tbgColor;
    private int tfgColor;
	
	
	public int getContentBackground() {
		if (!bReadConfig&&bgColor==0)
			return CMN.GlobalPageBackground;
		return bgColor;
	}
	
	public void setContentBackground(int value) {
		if (bgColor!=value) {
			bgColor = value;
			isDirty = true;
		}
	}
	
	public int getInternalTitleBackground() {
		return tbgColor;
	}
	
	public int getTitleBackground() {
		return getUseTitleBackground()?tbgColor:0;
	}
	
	public void setTitleBackground(int value) {
		if (tbgColor!=value) {
			tbgColor = value;
			isDirty = true;
		}
	}
	
	public int getInternalTitleForeground() {
		return tfgColor;
	}
	
	public int getTitleForeground() {
		return getUseTitleBackground()?tfgColor:0;
	}
	
	public void setTitleForeground(int value) {
		if (tfgColor!=value) {
			tfgColor = value;
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
		return IBC.tapZoomRatio;
	}
	
	public void setDoubleClickZoomRatio(float value) {
		if (IBC.tapZoomRatio !=value) {
			IBC.tapZoomRatio = value;
			isDirty = true;
		}
	}
	
	public float getDoubleClickOffsetX() {
		return IBC.tapZoomXOffset;
	}
	
	public void setDoubleClickOffsetX(float value) {
		if (IBC.tapZoomXOffset !=value) {
			IBC.tapZoomXOffset = value;
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
	public PDICMainAppOptions opt;
	
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
	public BookPresenter(@NonNull File fullPath, MainActivityUIBase THIS, int pseudoInit) throws IOException {
		bookImpl = getBookImpl(THIS, fullPath, pseudoInit);
		
		int type = 0;
		if (bookImpl!=null)
		{
			type = bookImpl.getType();
			if (type<0 || type>5)
			{
				type = 0;
			}
		} else if(pseudoInit==0) {
			throw new RuntimeException("Failed To Create Book! "+fullPath);
		}
		mType = DictionaryAdapter.PLAIN_BOOK_TYPE.values()[type];
		bAutoRecordHistory = isWebx = mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB;
		
		if (isWebx) {
			mWebx = (PlainWeb) bookImpl;
		} else {
			mWebx =null;
		}
		mMdict = isMdict() ? (PlainMdict) bookImpl : null;
		
		if(THIS!=null){
			a = THIS;
			opt = THIS.opt;
		}
		
		if((pseudoInit&1)==0) {
			//init(getStreamAt(0)); // MLSN
			File p = fullPath.getParentFile();
			if(p!=null && p.exists()) {
				StringBuilder buffer = getCleanDictionaryNameBuilder();
				int bL = buffer.length();
				File externalFile;
				/* 外挂同名css */
				if(PDICMainAppOptions.getAllowPlugCss()) {
					externalFile = new File(p, buffer.append(".css").toString());
					if(externalFile.exists()) {
						//todo 插入 同名 css 文件？
						hasExtStyle = true;
					}
				}
				buffer.setLength(bL);
				externalFile = new File(p, buffer.append(".png").toString());
				/* 同名png图标 */
				if(externalFile.exists()) {
					cover = Drawable.createFromPath(externalFile.getPath());
				}
			}
		}
		
		if(THIS!=null) {
			readConfigs(THIS, THIS.prepareHistoryCon());
		}
		
		StringBuilder sb = new StringBuilder(32);
		sb.append("d");
		long id = bookImpl==null?0:getId();
		idStr = IU.NumberToText_SIXTWO_LE(id, sb).toString();
		sb.setLength(0);
		idStr10 = sb.append(id).append(".com").toString();
		sb.setLength(0);
		mBaseUrl = sb.append("http://mdbr.").append("d").append(idStr10).append("/base.html").toString();
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
		//if (THIS!=null && THIS.systemIntialized) // 会变空白…
		try {
			LexicalDBHelper dataBase = THIS != null ? THIS.prepareHistoryCon() : LexicalDBHelper.getInstance();
			if (dataBase != null) {
				Long bid_ = bookImplsNameMap.get(name);
				if (bid_ == null) {
					bid = dataBase.getBookID(fullPath.getPath(), name);
					//CMN.Log("新标识::", bid, name);
					if (bid != -1) bookImplsNameMap.put(name, bid);
				} else {
					bid = bid_;
				}
				bookImpl = bookImplsMap.get(bid);
			}
		} catch (Exception e) { } // 读取quanxian时可能未获取权限，无法打开数据库。
		//CMN.Log("getBookImpl", fullPath, bookImpl);
		if ((pseudoInit&2)==0 && bookImpl==null) {
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
	public ViewGroup toolbar;
	ImageView toolbar_cover;
	private VerseKit ucc;
	public void initViewsHolder(final MainActivityUIBase a){
		this.a=a;
		ucc = a.getVtk(); //todo
		if(!viewsHolderReady) {
			ContentviewItemBinding pageData = ContentviewItemBinding.inflate(a.getLayoutInflater()
					, a.weblistHandler.getViewGroup(), false);
			mPageView = pageData;
			rl = (ViewGroup) pageData.getRoot();
	        {
	        	webScale = def_zoom;
	           	AdvancedNestScrollWebView _mWebView = pageData.webviewmy;
				rl.setTag(_mWebView);
				_mWebView.presenter = this;
				_mWebView.weblistHandler = a.weblistHandler;
				_mWebView.setNestedScrollingEnabled(PDICMainAppOptions.getEnableSuperImmersiveScrollMode());
				//if(!(this instanceof bookPresenter_pdf))
				_mWebView.setWebChromeClient(a.myWebCClient);
				_mWebView.setWebViewClient(a.myWebClient);
					_mWebView.setOnScrollChangedListener(a.getWebScrollChanged());
	            _mWebView.setPadding(0, 0, 18, 0);
				_mWebView.addJavascriptInterface(getWebBridge(), "app");
				if(isMergedBook())
					getWebBridge().mergeView = _mWebView;
				mWebView = mPageView.webviewmy;
	        }
			refresh_eidt_kit(pageData, mTBtnStates, bSupressingEditing, false);
			setWebLongClickListener(mWebView, a);

			toolbar = pageData.lltoolbar;
			ViewUtils.setOnClickListenersOneDepth(toolbar, this, 999, null);
			
			mWebView.pBc = IBC;
			mWebView.titleBar = (AdvancedNestScrollLinerView) toolbar;
			mWebView.FindBGInTitle(a, toolbar);
			mWebView.toolbarBG.setColors(mWebView.ColorShade);
			
			//toolbarBG.setColors(ColorSolid);
			mWebView.toolbar_title = toolbar_title = pageData.toolbarTitle;
			toolbar_cover = pageData.cover;
			if(cover!=null)
				toolbar_cover.setImageDrawable(cover);
			//toolbar.setTitle(this.bookImpl.getFileName().split(".mdx")[0]);
			mWebView.recess = pageData.recess;
			mWebView.forward = pageData.forward;
			mWebView.rl = rl;
			if (bookImpl!=null) {
				toolbar_title.setText(bookImpl.getDictionaryName());
			}
			toolbar_title.setMaxLines(1);
			toolbar_title.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getActionMasked()==MotionEvent.ACTION_DOWN) {
						//CMN.Log("down!!!");
						if (mWebView.AlwaysCheckRange!=0) {
							mWebView.AlwaysCheckRange=0;
							//mWebView.weblistHandler.pageSlider.bZoomOut=true;
						}
					}
					if (event.getActionMasked()==MotionEvent.ACTION_UP) {
						//CMN.Log("up!!!");
					}
					return false;
				}
			});
			viewsHolderReady=true;
			
			ViewUtils.removeView(pageData.recess);
			ViewUtils.removeView(pageData.forward);
			ViewUtils.removeView(pageData.redo);
			ViewUtils.removeView(pageData.save);
			ViewUtils.removeView(pageData.tools);
			//toolbar_cover.setId(R.id.lltoolbar);
			toolbar_cover.setOnClickListener(this);
			
			if(cover==null){
				toolbar_cover.setBackground(null);
//				toolbar_cover.setVisibility(View.GONE);
				toolbar.setOnClickListener(this);
				toolbar_title.setPadding((int) (15*GlobalOptions.density), 0, toolbar_title.getPaddingRight(), 0);
			}
			//recess.setVisibility(View.GONE);
			//forward.setVisibility(View.GONE);
			if (isWebx) {
//				mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
//				mWebView.getSettings().setUseWideViewPort(true);
//				mWebView.getSettings().setLoadWithOverviewMode(true);
//				mWebView.getSettings().setMediaPlaybackRequiresUserGesture(true);
//				mWebView.getSettings().setSupportMultipleWindows(false);
				String webSetttings = getWebx().getField("webSetttings");
				if (webSetttings!=null) {
					try {
						ViewUtils.execSimple(webSetttings, null, mWebView.getSettings());
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			}
		}
	}
	
	public AppHandler getWebBridge() {
		if(mWebBridge==null) {
			mWebBridge = new AppHandler(this);
		}
		return mWebBridge;
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
				if (isMergedBook()) {
					a.showDictTweaker(mWebView.weblistHandler);
					break;
				}
				a.getVtk().setInvoker(this, mWebView, null, null);
				a.getVtk().onClick(null);
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
				CMN.debug("toolbar_title onClick");
				mWebView.weblistHandler.pageSlider.bSuppressNxtTapZoom = CMN.now();
				if(mWebView.getVisibility()!=View.VISIBLE) {
					mWebView.setAlpha(1);
					mWebView.setVisibility(View.VISIBLE);
					if(mWebView.awaiting){
						mWebView.awaiting=false;
						renderContentAt(-1, RENDERFLAG_NEW, -1, null, mWebView.currentRendring);
					}
				}//((View)rl.getParent()).getId()==R.id.webholder
				else if(!mWebView.weblistHandler.isViewSingle()) {
					mWebView.setVisibility(View.GONE);
				}
				else {
					toolbar_cover.performClick();
				}
				break;
			case R.id.recess:
			case R.id.forward:
				boolean isGoBack = v.getId() == R.id.recess;
				if (isGoBack) mWebView.goBack();
				else mWebView.goForward();
				break;
		}
	}
	
	static void SelectHtmlObject(MainActivityUIBase a, WebViewmy wv, int source) {
		wv.evaluateJavascript(touchTargetLoader+"selectTouchtarget("+source+")", new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				CMN.debug("selectTouchtarget", value);
				int len = IU.parsint(value, 0);
				boolean fakePopHandles = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;
				if(len>0) {
					/* bring in action mode by a fake click on the programmatically  selected text. */
					if(fakePopHandles) {
						wv.forbidLoading=true;
						wv.getSettings().setJavaScriptEnabled(false);
						wv.getSettings().setJavaScriptEnabled(false);
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
				if(fakePopHandles) {
					wv.postDelayed(() -> {
						wv.forbidLoading=false;
						wv.getSettings().setJavaScriptEnabled(true);
						wv.evaluateJavascript(touchTargetLoader+"restoreTouchtarget()", null);
					}, 300);
				} else {
					wv.evaluateJavascript(touchTargetLoader+"restoreTouchtarget()", null);
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
	
	public final String GetSearchKey() {
		return searchKey;
	}
	
	public String GetAppSearchKey() {
		return a.getSearchTerm();
	}
	
	public final void SetSearchKey(String key) {
		searchKey = key;
	}
	
	public long GetSearchKeyId(String key, WebViewListHandler wlh) {
		return a.GetAddHistory(key, wlh);
	}
	
	public boolean getAcceptParagraph(String keyword, boolean isParagraph, int paragraphWords) {
		if (!getAcceptParagraph()) {
			if (paragraphWords!=minParagraphWords) {
				isParagraph = testIsParagraph(keyword, minParagraphWords);
			}
			if (isParagraph) {
				return false;
			}
		}
		return true;
	}
	
	public int QueryByKey(String keyword, SearchType searchType, boolean isParagraph, int paragraphWords, AtomicBoolean task)
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
			if (range_query_reveiver==null) {
				range_query_reveiver = new ArrayList<>();
			}
			return bookImpl.lookUpRange(keyword, range_query_reveiver, null, bookImpl.getBooKID(),15, task);
		}
		return -1;
	}
	
	public final long getId() {
		return bookImpl.getBooKID();
	}
	
	public PlainWeb getWebx() {
		return mWebx;
	}
	
	public final boolean getIsWebx() {
		return isWebx;
	}
	
	public final boolean getIsWebSch() {
		return isWebx && getWebx().searchable;
	}
	
	public boolean isMdict() {
		return mType == PLAIN_TYPE_MDICT;
	}
	
	public PlainMdict getMdict() {
		return mMdict;
	}
	
	public final boolean getHasVidx() {
		return bookImpl.hasVirtualIndex();
	}
	
	public int getIsManagerAgent() {
		return bIsManagerAgent;
	}
	
	public JSONObject getDictInfo(JSONObject json) {
		if(json==null)json = new JSONObject();
		else json.clear();
		json.put("name", getDictionaryName());
		json.put("tag", true);
		json.put("id", idStr);
		json.put("tbg", SU.toHexRGB(getTitleBackground()));
		json.put("tfg", SU.toHexRGB(getTitleForeground()));
		json.put("bg", getUseInternalBG()?SU.toHexRGB(getContentBackground()):null);
		json.put("img", getImageBrowsable() && bookImpl.hasMdd());
		PlainWeb webx = getWebx();
		if(webx!=null) {
			json.put("isWeb", 1);
			if(webx.hasField("synthesis"))
				json.put("synth", 1);
			String sch = webx.getSearchUrl();
			if(!sch.contains("%s")) sch=sch+"%s";
			json.put("sch", sch);
		}
		return json;
	}
	
	public static ConcurrentHashMap<String, Integer> debuggingSlots;
	
	public InputStream getDebuggingResource(String uri) throws IOException {
		if(debuggingSlots==null) {
			debuggingSlots=new ConcurrentHashMap<>(32);
		}
		Integer val = debuggingSlots.get(uri);
		if (val != null || debuggingSlots.size() < 24) {
			String p = getPath();
			p = p.substring(0, p.lastIndexOf(File.separator));
			File file = new File(p + uri).getCanonicalFile();
			CMN.debug("getDebuggingResource::", file, file.exists(), p, val);
			if (file.getPath().startsWith(p)) {
				if (val == null) {
					debuggingSlots.put(uri, val = file.exists() ? 1 : 0);
				}
				if (val == 1) {
					return new AutoCloseInputStream(new FileInputStream(file));
				}
			}
		} else {
			CMN.debug("getDebuggingResource::rejected", val, debuggingSlots.size());
		}
		return null;
	}
	
	public boolean getIsNonSortedEntries() {
		return mType!=PLAIN_TYPE_MDICT && mType!=PLAIN_TYPE_DSL;
	}
	
	public boolean store(int pos) {
		return true;
	}
	
	public boolean hasPreview() {
		return mType==PLAIN_TYPE_MDICT || mType== PLAIN_TYPE_DSL || mType== PLAIN_TYPE_TEXT;
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
						if(mWebView.fromNet()) {
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
			if(_mWebView.presenter.suppressingLongClick)
				return false;
			if(_mWebView.weblistHandler.pageSlider.twiceDetected)
				return true;
			_mWebView.lastLongSX = _mWebView.getScrollX();
			_mWebView.lastLongSY = _mWebView.getScrollY();
			_mWebView.lastLongScale = _mWebView.webScale;
			_mWebView.lastLongX = _mWebView.lastX;
			_mWebView.lastLongY = _mWebView.lastY;
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
							try {
								Intent intent = new Intent();
								intent.setData(Uri.parse(url));
								intent.setAction(Intent.ACTION_VIEW);
								a.startActivity(intent);
							} catch (Exception e) {
								a.showT(""+e); //todo
							}
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

    @SuppressLint("JavascriptInterface")
	public void setCurrentDis(WebViewmy mWebView, long idx) {
		mWebView.setPresenter(this);
		String word = StringUtils.trim(bookImpl.getEntryAt(mWebView.currentPos = idx));
		if(bookImpl.hasVirtualIndex()){
			if (idx==0 && mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB && searchKey!=null) {
				word = searchKey;
			} else {
				int tailIdx=word.lastIndexOf(":");
				if(tailIdx>0)
					word=word.substring(0, tailIdx);
			}
		}
		if (mWebView.weblistHandler==a.weblistHandler) {
			currentDisplaying = word;
		}
		if (mWebView.weblistHandler.isViewSingle()) {
			mWebView.weblistHandler.setStar(word);
		}
		if (mWebView.toolbar_title!=null) {
			mWebView.toolbar_title.setText(bookImpl.AcquireStringBuffer(64).append(word.trim()).append(" - ").append(bookImpl.getDictionaryName()).toString());
			if(mWebView.titleBar!=null)
				mWebView.titleBar.setVisibility(View.VISIBLE);
		}
		mWebView.word = word;
	}
	
	@SuppressLint("JavascriptInterface")
	public void setCurrentStar(WebViewmy mWebView, String word) {
		WebViewListHandler wlh = mWebView.weblistHandler;
		if (wlh ==a.weblistHandler) {
			currentDisplaying = word;
		}
		if (wlh.isViewSingle()) {
			wlh.setStar(word);
		}
		if (wlh.isPopupShowing()) {
			wlh.alloydPanel.toolbar.setTitle(word);
		}
		else if (mWebView.toolbar_title!=null) {
			mWebView.toolbar_title.setText(bookImpl.AcquireStringBuffer(64).append(word.trim()).append(" - ").append(bookImpl.getDictionaryName()).toString());
		}
		mWebView.word = word;
	}
	
	public void checkTint() {
		if(mWebView!=null) {
			tintBackground(mWebView);
			refresh_eidt_kit(mPageView, mTBtnStates, bSupressingEditing, false);
		}
	}
	
	public void tintBackground(WebViewmy mWebView) {
    	//CMN.rt();
		int globalPageBackground = CMN.GlobalPageBackground;
		boolean useInternal = getUseInternalBG();
		boolean isDark = GlobalOptions.isDark;
		int myWebColor = useInternal?bgColor:globalPageBackground;
		if (isDark) {
			myWebColor = ColorUtils.blendARGB(myWebColor, Color.BLACK, a.ColorMultiplier_Web2);
		}
		a.guaranteeBackground(globalPageBackground);
		int bg = (getIsolateImages()||useInternal||Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT||mWebView.weblistHandler.bDataOnly)?myWebColor:Color.TRANSPARENT;
		if(bg==0&&mWebView.weblistHandler.bShowingInPopup) bg = a.AppWhite;
		mWebView.setBackgroundColor(bg);
		/* check and set colors for toolbar title Background*/
		if(mWebView==this.mWebView){
			mWebView.titleBar.fromCombined = mWebView.fromCombined==1;
		}
		FlowTextView toolbar_title = mWebView.toolbar_title;
		if(toolbar_title!=null) {
			int StarLevel =  PDICMainAppOptions.getDFFStarLevel(firstFlag);
			toolbar_title.setStarLevel(StarLevel);
			if(StarLevel>0) {
				toolbar_title.setStarDrawables(a.getActiveStarDrawable(), toolbar_title==a.wordPopup.indicator ?a.getRatingDrawable():null);
			}
		}
		GradientDrawable toolbarBG = mWebView.toolbarBG;
		if(toolbarBG!=null) {
			useInternal = getUseTitleBackground();
			myWebColor = isDark?Color.BLACK
					:useInternal? tbgColor
					:PDICMainAppOptions.getTitlebarUseGlobalUIColor()?a.MainBackground
					:opt.getTitlebarBackgroundColor();
			CMN.debug("使用内置标题栏颜色：", this, useInternal, bookImpl.getDictionaryName(), isDark, Integer.toHexString(myWebColor));
			int colorTop = PDICMainAppOptions.getTitlebarUseGradient()?ColorUtils.blendARGB(myWebColor, Color.WHITE, 0.08f):myWebColor;
			int[] ColorShade = mWebView.ColorShade;
			if(ColorShade[1]!=myWebColor||ColorShade[0]!=colorTop)
			{
				ColorShade[1] = myWebColor;
				ColorShade[0] = colorTop;
				toolbarBG.setColors(ColorShade);
			}
			myWebColor = isDark?Color.WHITE:getUseTitleForeground()? tfgColor :opt.getTitlebarForegroundColor();
			mWebView.setTitlebarForegroundColor(myWebColor);
		}
		//CMN.pt("设置颜色：");
	}
	
	public final static int RENDERFLAG_NEW=0x1;

	//todo frameAt=-1
    public void renderContentAt(float initialScale, int fRender, int frameAt, WebViewmy mWebView, long...position){
    	CMN.debug("renderContentAt!!!...", bookImpl.getDictionaryName());
    	if (a==null) {
    		// safe check
    		return;
		}
		if (initialScale==-2) {
			initialScale=-1;
			ScrollerRecord pPos = opt.getRemPos()?avoyager.get((int) position[0]):null;
			if(pPos!=null) {
				//a.showT(""+currentDictionary.expectedPos);
				mWebView.expectedPos = pPos.y;
				mWebView.expectedPosX = pPos.x;
				initialScale = pPos.scale;
				//CMN.Log(avoyager.size()+"~"+position+"~取出旧值"+webview.expectedPos+" scale:"+pPos.scale);
			} else {
				mWebView.expectedPos=0;
				mWebView.expectedPosX=0;
			}
		}
		isJumping = false;
    	if(mWebView==null) {
    		mWebView=this.mWebView;
			refresh_eidt_kit(mPageView, mTBtnStates, bSupressingEditing, true);
		} else if(a.peruseView != null && mWebView==a.peruseView.mWebView) {
//			a.peruseView.mPageView.save.setOnLongClickListener(this); //111
//			refresh_eidt_kit(a.peruseView.mPageView, a.peruseView.mTBtnStates, a.peruseView.bSupressingEditing, false);
		}
		boolean resposibleForThisWeb=mWebView==this.mWebView;
    	
    	if (!resposibleForThisWeb && mWebView.presenter!=this) {
			mWebView.presenter = this;
			mWebView.pBc = this.IBC;
			//mWebView.History = this.HistoryOOP;
		}
	
		mWebView.active=true;
		boolean fromCombined = mWebView.fromCombined==1;

		boolean mIsolateImages=false;///*resposibleForThisWeb&&*/from==0&&getIsolateImages();
		/* 为了避免画面层叠带来的过度重绘，网页背景保持透明？。 */
		tintBackground(mWebView);
		
		if (ViewUtils.ViewIsId((View) mWebView.rl.getParent(), R.id.webSingleholder)) {
			mWebView.weblistHandler.setScrollFocus(mWebView, frameAt);
		}
	
		if((fRender&RENDERFLAG_NEW)!=0){
			//mWebView.SelfIdx=SelfIdx;
			//mWebView.setTag(mWebView.SelfIdx=SelfIdx);
			//if(resposibleForThisWeb) rl.setTag(bookImpl);
			//todo 是否记忆临时的折叠状态？
			//todo 是否为常规的页面开放连续的历史纪录？
			mWebView.currentPos=position[0];
			mWebView.currentRendring=position;
			mWebView.jointResult=mWebView.weblistHandler.multiDisplaying();
			if(frameAt>=0) mWebView.frameAt = frameAt;
			CMN.debug("折叠？？？", frameAt, mWebView.frameAt, getDictionaryName());
			mWebView.awaiting = false;
			if(/*resposibleForThisWeb && */fromCombined && frameAt>=0
					&& (frameAt>0 && PDICMainAppOptions.getDelaySecondPageLoading()
						|| getNeedsAutoFolding(frameAt))){/* 自动折叠 */
				mWebView.awaiting = true;
				mWebView.setVisibility(View.GONE);
				setCurrentDis(mWebView, mWebView.currentPos);
				CMN.debug("折叠！！！", mWebView.frameAt);
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
	
		setCurrentDis(mWebView, position[0]);
		
		if(resposibleForThisWeb) {
			if(fromCombined) {
				if(rl.getLayoutParams()!=null)
					rl.getLayoutParams().height = -1;//LayoutParams.WRAP_CONTENT;
				if (getContentFixedHeightWhenCombined()) {
					mWebView.getLayoutParams().height = a.root.getHeight();
				} else {
					mWebView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				}
				//mWebView.getLayoutParams().height = -1;
			}
			else {
				if(mIsolateImages){
					/* 只有在单本阅读的时候才可以进入图文分离模式。包括三种情况。 */
					//PDICMainAppOptions.getIsoImgLimitTextHight()  强定高度
//					rl.getLayoutParams().height = (int) (150*opt.dm.density);//文本限高模式
//					a.webSingleholder.setTag(R.id.image,false);
//					a.webSingleholder.setBackgroundColor(Color.TRANSPARENT);
//					a.contentUIData.PageSlider.slideTurn =false;
//					mWebView.setBackgroundColor(Color.TRANSPARENT);
//					a.initPhotoViewPager(); todo 123
				} else {
					rl.getLayoutParams().height = LayoutParams.MATCH_PARENT;
					mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
				}
				//???
				//mWebView.weblistHandler.resetScrollbar(mWebView, false, false);
			}
		}
		
		if(!fromCombined)
			mWebView.weblistHandler.resetScrollbar(mWebView, false, false);
	
		//todo 处理 fromCombined && opt.getAlwaysShowScrollRect()
		mWebView.SetupScrollRect(!getNoScrollRect() && (!fromCombined&&opt.getAlwaysShowScrollRect() || fromCombined&&getContentFixedHeightWhenCombined()));

    	//mWebView.setVisibility(View.VISIBLE);
   	    //a.showT("mWebView:"+mWebView.isHardwareAccelerated());
		
		//if(!fromPopup) PlayWithToolbar(a.hideDictToolbar,a);
		
    	if(mWebView.wvclient!=a.myWebClient) {
			mWebView.setWebChromeClient(a.myWebCClient);
	   	    mWebView.setWebViewClient(a.myWebClient);
    	}

		renderContentAt_internal(mWebView, initialScale, fromCombined, mIsolateImages, position);
    }
	
	public boolean getNeedsAutoFolding(int frameAt) {
		return PDICMainAppOptions.getTmpIsCollapsed(tmpIsFlag) || getAutoFold()
						|| PDICMainAppOptions.getOnlyExpandTopPage() && frameAt>=opt.getExpandTopPageNum()    ;
	}
	
	public StringBuilder AcquirePageBuilder() {
		StringBuilder sb = bookImpl.AcquireStringBuffer(512);
		sb.append(htmlBase);
		sb.append(js);
		plugCssWithSameFileName(sb);
		return sb;
	}
	
	public void plugCssWithSameFileName(StringBuilder sb) {
		if(isHasExtStyle()) {
			CMN.Log("外挂同名 css");
			String fullFileName = bookImpl.getDictionaryName();
			int end = fullFileName.length();
			if (unwrapSuffix) {
				int idx = bookImpl.getDictionaryName().lastIndexOf(".");
				if (idx > 0) end = idx;
			}
			sb.append("<link rel='stylesheet' type='text/css' href='")
					.append(fullFileName, 0, end)
					.append(".css?f=auto'/>");
		}
	}
	
	public void renderContentAt_internal(WebViewmy mWebView, float initialScale, boolean fromCombined, boolean mIsolateImages, long... position) {
		mWebView.isloading=true;
		mWebView.currentPos = position[0];
		//if(!a.AutoBrowsePaused&&a.background&&PDICMainAppOptions.getAutoBrowsingReadSomething())
		//	mWebView.resumeTimers();
    	String htmlCode = null ,JS=null;
    	boolean loadUrl=opt.alwaysloadUrl()
				|| opt.popuploadUrl()&&mWebView.weblistHandler.bShowingInPopup
				|| mWebView.weblistHandler.bDataOnly
				//|| mWebView.weblistHandler.getSrc()==SearchUI.TapSch.MAIN && true
				;
		//loadUrl = true;
    	//CMN.debug("loadUrl::", loadUrl);
		try {
			if(bookImpl.hasVirtualIndex())
				try {
					String validifier = getOfflineMode()&&getIsWebx()?null:bookImpl.getVirtualTextValidateJs(this, mWebView, position[0]);
//					CMN.Log("validifier::", validifier, GetSearchKey(), mWebView.getTag());
					if (validifier == null
							//|| true // 用于调试直接网页加载
							|| "forceLoad".equals(mWebView.getTag())
							|| mWebView.getUrl()==null) {
						htmlCode = bookImpl.getVirtualRecordsAt(this, position);
						mWebView.setTag(null);
						CMN.debug("htmlCode::", htmlCode, GetSearchKey());
						if (htmlCode!=null && (
								htmlCode.startsWith("http")
								|| htmlCode.startsWith("file")
								)) {
							// 如果是加载网页
							mWebView.loadUrl(htmlCode);
							htmlCode = null;
						}
					} else {
						mWebView.evaluateJavascript(validifier, new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
//								CMN.Log("validifier::onReceiveValue::", value);
								if ("1".equals(value) || "true".equals(value)) {
									String effectJs = bookImpl.getVirtualTextEffectJs(position);
									if (effectJs!=null) mWebView.evaluateJavascript(effectJs, null);
									//a.showT("免重新加载生效！");
									vartakelayaTowardsDarkMode(mWebView);
									// 注意，这里会多次调用OPF中eval的脚本！
									mWebView.bPageStarted=true;
									mWebView.postFinishedAbility.run();
								}  else if("2".equals(value) && getIsWebx()) { // apply js modifier first, then do search
									if (!"schVar".equals(mWebView.getTag())) {
										mWebView.setTag("schVar");
										SetSearchKey(GetAppSearchKey());
										renderContentAt_internal(mWebView, initialScale, fromCombined, mIsolateImages, 0);
										mWebView.setTag(null);
									}
								} else {
									mWebView.setTag("forceLoad");
									renderContentAt_internal(mWebView, initialScale, fromCombined, mIsolateImages, position);
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
			else if(loadUrl) {
				StringBuilder mergedUrl = new StringBuilder("http://mdbr.com/content/");
				mergedUrl.append("d");
				IU.NumberToText_SIXTWO_LE(getId(), mergedUrl);
				for(long p:position) {
					mergedUrl.append("_");
					IU.NumberToText_SIXTWO_LE(p, mergedUrl);
				}
				if (mWebView.toTag!=null) {
					mergedUrl.append("#").append(mWebView.toTag);
					mWebView.toTag=null;
				}
				htmlCode = mergedUrl.toString();
			}
			else
			{
				htmlCode = bookImpl.getRecordsAt(mBookRecordInteceptor, position);
				if (bViewSource) {
					htmlCode = StringEscapeUtils.escapeHtml3(htmlCode); // 调试，查看源码
					bViewSource = false;
				}
				else {
					if(hasFilesTag()){
						htmlCode = htmlCode.replace("file://", "");
					}
				}
			}
		}
		catch (Exception e) {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(s));
			htmlCode=_404+e.getLocalizedMessage()+"<br>"+s;
			CMN.Log(s);
		}
		
		mWebView.expectedZoom = initialScale;
//    	CMN.Log("renderContentAt_internal::缩放是", initialScale);
//		if(initialScale!=-1)
//			mWebView.setInitialScale((int) (100*(initialScale/ BookPresenter.def_zoom)*opt.dm.density));//opt.dm.density
//		else {
//			//尝试重置页面缩放
//			mWebView.setInitialScale(0);//opt.dm.density
//		}
		
		StringBuilder htmlBuilder = AcquirePageBuilder();
		
		mWebView.getSettings().setSupportZoom(!fromCombined);
		mWebView.isloading = true;
		if(htmlCode!=null) {
			if(loadUrl) {
				//mWebView.presenter = mWebView.weblistHandler.getMergedBook();
				mWebView.loadUrl(htmlCode);
			}
			else if (!htmlCode.startsWith(fullpageString)) {
				AddPlodStructure(mWebView, htmlBuilder, mIsolateImages);
				LoadPagelet(mWebView, htmlBuilder, htmlCode);
			}
			else {
				int headidx = htmlCode.indexOf("<head>");
				boolean b1 = headidx == -1;
				if (true) {
					if (b1) {
						htmlBuilder.append("<head>");
					} else {
						htmlBuilder.append(htmlCode, 0, headidx + 6);
					}
					AddPlodStructure(mWebView, htmlBuilder, mIsolateImages);
					htmlBuilder.append(getSimplestInjection());
					if (b1) {
						htmlBuilder.append("</head>");
						htmlBuilder.append(htmlCode);
					} else {
						htmlBuilder.append(htmlCode, headidx + 6, htmlCode.length());
					}
					htmlCode = htmlBuilder.toString();
				}
				mWebView.loadDataWithBaseURL(mBaseUrl, htmlCode, null, "UTF-8", null);
			}
		} else if(JS!=null) {
			mWebView.evaluateJavascript(JS, null);
		}
	}
	
	public void ApplySearchKey(WebViewmy mWebView) {
		//CMN.Log("OPF::EvaluateValidifierJs::", GetSearchKey());
		String validifier1 = getOfflineMode()&&getIsWebx()?null:bookImpl.getVirtualTextValidateJs(this, mWebView, mWebView.currentPos);
		if (validifier1!=null) {
			//CMN.Log("OPF::EvaluateValidifierJs::");
			EvaluateValidifierJs(validifier1, mWebView);
		}
		SetSearchKey(null);
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
						renderContentAt_internal(mWebView, -1, mWebView.fromCombined==1, false, 0);
						mWebView.setTag(null);
					}
				}
			}
		});
	}
	
	public void vartakelayaTowardsDarkMode(WebViewmy mWebView) {
		if(mWebView==null) mWebView=this.mWebView;
		if(mWebView==null) return;
		boolean dark = GlobalOptions.isDark;
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
	public void AddPlodStructure(WebViewmy mWebView, StringBuilder htmlBuilder, boolean mIsolateImages) {
    	//CMN.Log("MakeRCSP(opt)??", MakeRCSP(opt),MakeRCSP(opt)>>5);
		htmlBuilder.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
		if(bookImpl.hasMdd()) htmlBuilder.append("<p class='MddExist'/>");
		htmlBuilder.append("</div>");
		boolean styleOpened=false;
		if (mWebView.weblistHandler.bDataOnly) {
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
//		int rcsp = MakeRCSP(mWebView.weblistHandler, opt);
//		if(mWebView==a.wordPopup.mWebView) rcsp|=1<<5; //todo
		htmlBuilder.append("window.shzh=").append(mWebView.weblistHandler.tapSch?1:0).append(";");
		htmlBuilder.append("frameAt=").append(mWebView.frameAt).append(";");
		htmlBuilder.append("entryKey='").append(mWebView.word).append("';");
		//htmlBuilder.append("hasFiles='").append(hasFilesTag()).append("';");
		
		/** see {@link AppHandler#view} */
		// 此处代码的加载略慢于图片资源
		htmlBuilder.append("app.view(sid.get(),")
				.append(getId())
				.append(",").append(mWebView.currentPos)
				.append(",").append(hasFilesTag())
				.append(");");
		
		//htmlBuilder.append("webx=").append(getIsWebx()?1:0).append(";");
		htmlBuilder.append("</script>");
	}

	public void LoadPagelet(WebViewmy mWebView, StringBuilder htmlBuilder, String records) {
		mWebView.loadDataWithBaseURL(mBaseUrl,
				htmlBuilder.append(htmlHeadEndTag)
						.append(records)
						.append(htmlEnd).toString(), null, "UTF-8", null);
	}

	String getSimplestInjection() {
		if (bookImpl instanceof DictionaryAdapter)
			return ((DictionaryAdapter) bookImpl).getSimplestInjection();
		return SimplestInjection;
	}

	public static int MakePageFlag(WebViewListHandler wlh, PDICMainAppOptions opt) {
		final int ret =
				(wlh.tapSch?1:0)
				| 8
				| ( (PDICMainAppOptions.pageSchUseRegex()?0x10:0)
				|   (PDICMainAppOptions.pageSchCaseSensitive()?0x20:0)
				|   (PDICMainAppOptions.pageSchSplitKeys()?0x40:0)
				|   (PDICMainAppOptions.pageSchWildMatchNoSpace()?0x80:0)
				|   (PDICMainAppOptions.pageSchWild()?0x100:0)
				|   (PDICMainAppOptions.pageSchDiacritic()?0x200:0)
				)
				//opt.FetIsDark()
				;
		// CMN.debug("rcsp::", Integer.toBinaryString(ret), ret);
		return ret;
	}
	
	public static void SavePageFlag(int sz) {
		sz = sz>>4;
		PDICMainAppOptions.pageSchUseRegex((sz&1)!=0);
		PDICMainAppOptions.pageSchCaseSensitive((sz&2)!=0);
		PDICMainAppOptions.pageSchSplitKeys((sz&4)!=0);
		PDICMainAppOptions.pageSchWildMatchNoSpace((sz&8)!=0);
		PDICMainAppOptions.pageSchWild((sz&0x10)!=0);
		PDICMainAppOptions.pageSchDiacritic((sz&0x20)!=0);
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
		try {
			if (getContentEditable() && mType==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
				PlainWeb webx = (PlainWeb) bookImpl;
				if(url.startsWith(webx.host)) {
					url=url.substring(webx.host.length());
				}
				CMN.Log("getWebPage::", url);
				return a.prepareHistoryCon().getPageStream(getId(), url);
			}
		} catch (Exception e) {
			CMN.Log(e);
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

	public String getBookEntryAt(int pos) {
		if (pos<0 || pos>=bookImpl.getNumberEntries()) {
			if (pos==-1) {
				return "←";
			}
			if (pos==bookImpl.getNumberEntries()) {
				return "→";
			}
			return "Error!!!";
		}
		return bookImpl.getEntryAt(pos);
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
	
	public String getCharsetName() {
		return bookImpl.getCharsetName();
	}
	
	@SuppressWarnings("unused")
    public static class AppHandler {
		BookPresenter presenter;
		public WebViewmy mergeView;
		
		private WebViewmy findWebview(int sid) {
			if(mergeView!=null && sid==mergeView.simpleId()) {
				return mergeView;
			}
			return presenter==null?null:presenter.findWebview(sid);
		}

        @JavascriptInterface
        public void log(String val) {
        	CMN.Log(val);
        }
		
		@JavascriptInterface
		public void setStar(int sid, String val) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					presenter.a.hdl.post(() -> presenter.setCurrentStar(wv, val));
				}
			}
		}
	
		@JavascriptInterface
        public String decodeExp(String val) {
			String ret = "";
			if (presenter!=null) {
				try {
					if ("fake".equals(val)) {
						ret = presenter.a.fakedExp;
					} else {
						byte[] data = Base64.decode(val, Base64.NO_WRAP);
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						InflaterOutputStream def = new InflaterOutputStream(out);
						def.write(data, 0, data.length);
						def.close();
						data = out.toByteArray();
						ret = new String(data, StandardCharsets.UTF_8);
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
			return ret;
        }
		
        @JavascriptInterface
        public void SaveDopt(int sid, String val) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					presenter.getWebx().saveDopt(presenter.a, val);
				}
			}
        }
		
        @JavascriptInterface
        public void saveOpt(int sid, String val) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null && wv.weblistHandler.isMergingFrames()) {
					//CMN.Log("saveOpt::", val, JSON.parse(val));
					presenter.a.opt.putString("opt", val);
					presenter.a.getMdictServer().strOpt = null;
					presenter.a.strOpt = null;
				}
			}
        }
		
        @JavascriptInterface
		public String markdown(String value) {
			if (presenter!=null) {
				Markwon markwon = Markwon.create(presenter.a);
				return Html.toHtml(markwon.toMarkdown(value));
			}
			return "";
		}
		
        @JavascriptInterface
		public String showT(String value) {
			if (presenter!=null) {
				presenter.a.hdl.post(() -> presenter.a.showT(value));
			}
			return "";
		}
		
        @JavascriptInterface
        public void suppressTurnPage(int sid, boolean val, boolean hideUI) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					final WebViewListHandler weblist = wv.weblistHandler;
					if (weblist!=null) {
						if (val) {
							weblist.pageSlider.slideTurn = false;
							weblist.slideDirty = true;
						} else {
							weblist.pageSlider.quoTapZoom();
						}
						final SeekBar entrySeek = weblist.entrySeek;
						if (weblist.isMergingFrames() && hideUI && entrySeek.getVisibility()!=View.GONE) {
							presenter.a.hdl.post(() -> {
								entrySeek.setVisibility(val?View.INVISIBLE:View.VISIBLE);
								entrySeek.setEnabled(!val);
							});
						}
					}
				}
			}
        }
		
		@JavascriptInterface
		public void view(int sid, long bid, long pos, boolean hasFiles) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv!=null) {
					//CMN.debug("view::", presenter, presenter.a.getBookByIdNoCreation(bid).getDictionaryName(), pos, presenter.hasFilesTag());
					boolean changed = presenter.getId()!=bid || wv.presenter!=presenter;
					if (changed) {
						setBook(presenter.a.getBookByIdNoCreation(bid));
						wv.setPresenter(presenter);
						wv.changed = 1;
					}
					if (wv.currentPos!=pos) {
						wv.currentPos = pos;
						if (!changed) {
							changed = true;
							wv.changed = 2;
						}
					}
					if (changed) {
						//CMN.debug("view::changed!!!", wv.changed, presenter.hasFilesTag(), hasFiles);
					}
					if (presenter.hasFilesTag() && !hasFiles) {
						//wv.hasFilesTag = true;
					}
					//wv.initPos();
					//wv.initScale();
				}
			}
		}
		
		@JavascriptInterface
		public void viewChanged(int sid, String entryKey, int frameAt) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					//CMN.debug("view::changed!!!", entryKey);
					wv.word = entryKey;
					wv.frameAt = frameAt;
				}
			}
		}
		
		@JavascriptInterface
		public void banDbclk(int sid) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				wv.weblistHandler.pageSlider.bSuppressNxtTapZoom = CMN.now();
				//CMN.Log("textMenu!!!");
			}
		}
		@JavascriptInterface
		public void textMenu(int sid, boolean show) {
			if (presenter!=null && PDICMainAppOptions.toolsBoost()) {
				WebViewmy wv = findWebview(sid);
				wv.post(() -> wv.weblistHandler.textMenu(show?wv:null));
				//CMN.Log("textMenu!!!");
			}
		}
		
		@JavascriptInterface
        public int rcsp(int sid) {
			try {
				WebViewmy wv = findWebview(sid);
				WebViewListHandler wlh = wv.weblistHandler;
				//CMN.debug("Flag::web::rcsp", wlh.getSrc());
				if ((wlh.shezhi&8)!=0) {
					return wlh.shezhi;
				}
				return MakePageFlag(wlh, wlh.opt);
			} catch (Exception e) {
				CMN.debug(e);
				return 0;
			}
        }
        
        @JavascriptInterface
        public String loadJson(String url) {
			CMN.debug("loadJson::", url);
			MainActivityUIBase a = presenter.a;
			MdictServer server = a.getMdictServer();
			try {
				if(url.startsWith("/dicts.json")) {
					StringBuilder sb = new StringBuilder();
					sb.append("[");
					JSONObject json = new JSONObject();
					if(url.startsWith("?", 11)) {
						//JSONArray jsonArray = new JSONArray();
						StringTokenizer array = new StringTokenizer(url, ",");
						boolean st=true;
						while(array.hasMoreTokens()) {
							String ln = array.nextToken();
							if(st) {
								ln = ln.substring(16);
								st=false;
							}
							BookPresenter book = server.md_getByURL(ln);
							if(book!=null) {
								if(sb.length()>1)sb.append(",");
								sb.append(book.getDictInfo(json));
								//jsonArray.add(book.getDictInfo());
							}
						}
						sb.append("]");
						CMN.Log("dicts::", url, sb.toString());
						return sb.toString();
					} else {
						for (int i = 0; i < a.loadManager.md_size; i++) {
							BookPresenter book = a.loadManager.md_get(i);
							if(sb.length()>1)sb.append(",");
							if(book!=a.EmptyBook) {
								sb.append(book.getDictInfo(json));
							} else {
								json.clear();
								json.put("name", a.loadManager.md_getName(i, -1));
								sb.append(book.getDictInfo(json));
							}
						}
					}
				}
				else if(url.startsWith("/settings.json")) {
					return a.getWebSettings();
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
			return "{}";
        }
        
        @JavascriptInterface
        public void banLongClick(int sid, boolean suppress) {
			if (presenter!=null) {
				presenter.suppressingLongClick = suppress;
			}
		}
		
        @JavascriptInterface
        public int cs() {
			return presenter.a.weblist.getScrollHandType();
		}
		
        @JavascriptInterface
        public void sc(int sid, int max, int y) {
			try {
				WebViewmy wv = findWebview(sid);
				if(wv!=null) {
					WebViewListHandler weblistHandler = wv.weblistHandler;
					DragScrollBar _mBar = weblistHandler.contentUIData.dragScrollBar;
					if(_mBar.isHidden()) {
						if(Math.abs(_mBar.progress()-y)>=10*dm.density)
							_mBar.postfin();
					}
					if(!_mBar.isHidden()){
						if(!_mBar.isWebHeld)
							_mBar.hiJackScrollFinishedFadeOut();
						if(!_mBar.isDragging){
							_mBar.setMax(max);
							_mBar.postprog(y);
						}
					}
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		
        @JavascriptInterface
        public void updateIndicator(int sid, String did, int keyIdx, int keyz, int total) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				WebViewListHandler wlh = wv.weblistHandler;
				wlh.updateInPageSch(did, keyIdx, keyz, total);
			}
		}
		
		/** 页面滚动Y值 */
		@JavascriptInterface
		public int getScrollY(int sid) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				return (int) (wv.getScrollY() / def_zoom);
			}
			return 0;
		}
		
		/** 保存在线翻译器语种 */
		@JavascriptInterface
		public void putTransval(int sid, String value, int index) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				WebViewListHandler wlh = wv.weblistHandler;
				wlh.putTransval(index, value);
			}
		}
		
		/** 获取保存的在线翻译器语种 */
		@JavascriptInterface
		public String getTransval(int sid, int index) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				WebViewListHandler wlh = wv.weblistHandler;
				return wlh.getTransval(index);
			}
			return "";
		}
		
		/** 保存在线翻译器状态 false翻译 true不翻译 */
        @JavascriptInterface
        public void putTranslate(int sid, boolean val, int index) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				WebViewListHandler wlh = wv.weblistHandler;
				wlh.putTranslate(index, val);
			}
		}
		
		/** 获取保存的在线翻译器状态 false不翻译 true翻译 */
		@JavascriptInterface
		public boolean getTranslate(int sid, int index) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				WebViewListHandler wlh = wv.weblistHandler;
				return !wlh.getTranslate(index);
			}
			return true;
		}
		
        @JavascriptInterface
        public String getSearchWord(int sid) {
			if (presenter!=null) {
				return presenter.a.getSearchTerm();
			}
			return "";
		}
        
        @JavascriptInterface
        public void loadJs(int sid, String name) {
			WebViewmy wv = findWebview(sid);
			CMN.debug("loadJs::", name, wv!=null);
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
		
		
		@JavascriptInterface
		public void showUcc(String id, String text) {
        	MainActivityUIBase a = presenter.a;
			a.weblistHandler.mMergedFrame.post(() -> {
				a.getVtk().setInvoker(a.getBookById(IU.TextToNumber_SIXTWO_LE(id)), a.weblistHandler.mMergedFrame, null, text);
				a.weblistHandler.mMergedFrame.setTag(0);
				a.getVtk().onClick(a.weblistHandler.mMergedFrame);
			});
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
        public void openImage(int sid, int position, float offsetX, float offsetY, String...img) {
			BookPresenter book = ViewUtils.getBookFromImageUrl(presenter, img, false);
			if(book==null) return;
			String url = img[0];
			CMN.debug("openImage::", url);
			if (book.getImageBrowsable()) {
				MainActivityUIBase a = book.a;
				AgentApplication app = ((AgentApplication) a.getApplication());
				app.book = book;
				app.opt = book.opt;
				app.Imgs = img;
				app.currentImg = position;
				a.root.postDelayed(a.getOpenImgRunnable(), 100);
				WebViewmy wv = findWebview(sid);
				if (wv!=null) {
					wv.lastX = offsetX;
					wv.lastY = offsetY;
					a.weblist = wv.weblistHandler;
					wv.weblistHandler.scrollFocus = wv;
				}
			}
		}

        @JavascriptInterface
        public void scrollHighlight(int sid, int o, int d) {
        	if(presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if(wv!=null) {
					WebViewListHandler weblistHandler = wv.weblistHandler;
					weblistHandler.scrollHighlight(o, d);
				}
			}
        }

        @JavascriptInterface
        public String getCurrentPageKey(int sid) {
        	String ret=null;
			if(presenter!=null)
			try {
				WebViewmy wv = findWebview(sid);
				if(wv.weblistHandler.pageSchBar !=null && wv.weblistHandler.pageSchBar.getParent()!=null) {
					return wv.weblistHandler.pageSchEdit.getText().toString();
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
			return ret==null?"":ret;
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
		public String getHost() {
			CMN.Log("getHost::", presenter);
			if (presenter!=null && presenter.getIsWebx()) {
				return presenter.getWebx().getHost();
			}
			return "";
		}
		
		@JavascriptInterface
		public void suppressLnk(int sid) {
			if(presenter==null) return;
			WebViewmy wv = findWebview(sid);
			if (wv!=null) {
				wv.lastSuppressLnkTm = CMN.now();
			}
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
//			presenter.a.jumpHighlight(d, true);//333
        }

        @JavascriptInterface
        public void onHighlightReady(int sid, int idx, int number) {
			if(presenter==null) return;
			WebViewmy wv = findWebview(sid);
			wv.weblistHandler.onHighlightReady(idx, number);
        }

        @JavascriptInterface
        public void popupWord(int sid, String key, int frameAt, float pX, float pY, float pW, float pH) {
			try {
				if(presenter==null) return;
				if(WebViewmy.supressNxtClickTranslator) {
					return;
				}
				MainActivityUIBase a = presenter.a;
				WebViewmy wv = findWebview(sid);
				//CMN.debug("popupWord::ivk::", presenter, wv, mergeView);
				a.popupWord(key, null, frameAt, wv);
				a.wordPopup.tapped = true;
				if (false) {
					if(frameAt>=0 && pH!=0){
						if(pW==0) pW=pH;
						if(RLContainerSlider.lastZoomTime == 0 || System.currentTimeMillis() - RLContainerSlider.lastZoomTime > 500){
							//Utils.setFloatTextBG(new Random().nextInt());
							//CMN.Log("popupWord::", pX, pY, pW, pH);
							//CMN.Log("只管去兮不管来", wv!=null);
							if(wv!=null){
								float density = dm.density;
								wv.highRigkt_set(pX*density, pY*density, (pX+pW)*density, (pY+pH)*density);
							}
						}
					}
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
		}

        @JavascriptInterface
        public void popupClose(int sid) {
			if(presenter!=null) {
				CMN.debug("popuping...popupClose", sid);
				if(this!=presenter.a.wordPopup.popuphandler)
					presenter.a.postDetachClickTranslator();
				if (false) {
					WebViewmy wv = findWebview(sid);
					if(wv!=null && wv.drawRect){
						wv.drawRect=false;
						wv.postInvalidate();
					}
				}
			}
        }
		
		
		@JavascriptInterface
		public boolean entryPopup(int sid, String bid) {
			CMN.debug("entryPopup", sid, bid);
			if(presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if(wv!=null){
					return bid.length()==0?PDICMainAppOptions.popViewEntryOne():PDICMainAppOptions.popViewEntry();
					//return true;
				}
			}
			return false;
		}
		
		@JavascriptInterface
		public boolean shouldPopupEntry() {
			return PDICMainAppOptions.popViewEntry();
		}
		
        @JavascriptInterface
        public void popupEntry(int sid, String url) {
			if(presenter!=null) {
				CMN.debug("popupEntry", sid, url);
				WebViewmy wv = findWebview(sid);
				if(wv!=null){
					wv.post(() -> {
						WebViewListHandler wlh = presenter.a.getRandomPageHandler(true, true, null);
						WebViewmy mWebView = wlh.getMergedFrame();
						wlh.setViewMode(null, 0, mWebView);
						wlh.viewContent();
						wlh.bShowInPopup=true;
						wlh.bMergeFrames=0;
						wlh.initMergedFrame(0, true, false);
						wlh.popupContentView(null, url);
						mWebView.loadUrl(url);
						wlh.pageSlider.setWebview(mWebView, null);
						wlh.resetScrollbar();
					});
				}
			}
        }
		
        @JavascriptInterface
        public boolean isLowEnd(int sid) {
			return Build.VERSION.SDK_INT<21;
		}
		
        @JavascriptInterface
        public String getRandomPage(int sid) {
			if(presenter!=null) {
				if (presenter.a.refreshingRandom) {
					presenter.a.refreshingRandom = false;
					return null;
				}
				WebViewmy wv = findWebview(sid);
				if(wv!=null){
					return presenter.a.prepareHistoryCon().getPageString(presenter.getId(), "randx");
				}
			}
			return null;
        }
		
        @JavascriptInterface
        public void saveRandomPage(int sid, String content) {
			try {
				WebViewmy wv = findWebview(sid);
				long ret = presenter.a.prepareHistoryCon().putPage(presenter.getId(), "randx", -100, null, content);
			} catch (Exception e) {
				CMN.debug(e);
			}
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
		public void ReadText(int sid, String word) {
			if(presenter==null) return;
			presenter.a.ReadText(word, findWebview(sid));
		}

		public void setBook(BookPresenter bk) {
        	if(bk!=null) {
				presenter=bk;
				if(dm==null) {
					dm=bk.a.dm;
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
		public void wordtoday(String val) {
			//presenter.a.root.post(() -> presenter.a.showContentSnack(val));
			presenter.a.randomPageHandler.dismissPopup();
			presenter.a.root.post(() -> presenter.a.setSearchTerm(val));
			
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
		public void knock(int sid) {
			//if(layout==a.currentViewImpl)
			{
				//upsended = true;
				WebViewmy view = findWebview(sid);
				//view.lastSuppressLnkTm = CMN.now();
				CMN.Log("knock", view.weblistHandler);
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
		public void knockX(int sid) {
			WebViewmy view = findWebview(sid);
			view.lastSuppressLnkTm = CMN.now();
			CMN.debug("knockX::");
		}
		
		@JavascriptInterface
		public void knock1(int sid, int x, int y) {
			//if(layout==a.currentViewImpl)
			{
				//upsended = true;
				WebViewmy view = findWebview(sid);
				//view.lastSuppressLnkTm = CMN.now();
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
		
		@JavascriptInterface
		public void setFlag(int val) {
		}
		@JavascriptInterface
		public int getFlag() {
			return 0;
		}
		@JavascriptInterface
		public void setPos(String pos) {
		}
		@JavascriptInterface
		public void setLastMd(String val) {
		}
		@JavascriptInterface
		public String getCurrentPageKey(boolean bAppendFlag){
			return null;
		}
		@JavascriptInterface
		public String getKeyWord() {
			return "";
		}
		@JavascriptInterface
		public void recordRecords(String record){
		}
		@JavascriptInterface
		public void setCombinedSeaching(boolean val){
		}
		@JavascriptInterface
		public void saveCurrent(String val){
		}
		@JavascriptInterface
		public void InPageSearch(String text){
		}
		@JavascriptInterface
		public boolean getInPageSearch(String text){
			return false;
		}
		@JavascriptInterface
		public void handleWebLink(String bid, String url){
			if (presenter!=null) {
				BookPresenter book = presenter;//presenter.a.getBookByIdNoCreation(IU.TextToNumber_SIXTWO_LE(bid));
				book.a.hdl.post(new Runnable() {
					@Override
					public void run() {
						BookPresenter presenter = book.a.webxford.get(SubStringKey.new_hostKey(url));
						CMN.debug("handleWebLink::", url, presenter, presenter!=null&&presenter.isWebx);
						if (presenter!=null) {
							try {
								presenter.getWebx().getVirtualRecordAt(presenter, 0); //todo opt webx
							} catch (IOException e) {
								CMN.debug(e);
							}
						} else {
							presenter = book;
						}
						WebViewListHandler wlh = book.a.getRandomPageHandler(true, false, presenter);
						WebViewmy randomPage = wlh.getMergedFrame(presenter);
						wlh.setStar(null);
						randomPage.loadUrl(url);
					}
				});
			}
		}
		@JavascriptInterface
		public void handleWebSearch(String url, int slot){
		}
		@JavascriptInterface
		public void handleLvResize(int size){
		}
		@JavascriptInterface
		public void reloadDict(int idx){
		}
		@JavascriptInterface
		public void openFolder(int idx){
		}
	}
	
	private WebViewmy findWebview(int sid) {
		if (mWebView!=null && mWebView.simpleId()==sid)
			return mWebView;
		if (a.PeruseViewAttached() && a.peruseView.mWebView.simpleId()==sid) {
			return a.peruseView.mWebView;
		}
		if (a.wordPopup.mWebView !=null && a.wordPopup.mWebView.simpleId()==sid) {
			return a.wordPopup.mWebView;
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
		if (mWebBridge!=null) {
			mWebBridge.presenter = null;
		}
		if(viewsHolderReady) {
			viewsHolderReady =  false;
			ucc=null;
			toolbar_cover.setOnClickListener(null);
			toolbar_cover = null;
			toolbar_title = null;
			toolbar = null;
			if(isMergedBook() && mWebBridge!=null)
				mWebBridge.mergeView=null;
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
		if(mType==PLAIN_TYPE_EMPTY) return;
		setIsDedicatedFilter(false);
		try {
			DataOutputStream data_out;
			
			String save_name = bookImpl.getDictionaryName();
			
			ReusableByteOutputStream bos = new ReusableByteOutputStream(bookImpl.getOptions(), MainActivityUIBase.ConfigSize + MainActivityUIBase.ConfigExtra);
			bos.precede(MainActivityUIBase.ConfigExtra);
			data_out = new DataOutputStream(bos);
			
			data_out.writeByte(0);
			data_out.writeByte(0);
			data_out.writeByte(0);
			
			data_out.writeInt(bgColor);
			data_out.writeInt(internalScaleLevel);
			data_out.writeInt(lvPos);
			data_out.writeInt(lvClickPos);
			data_out.writeInt(lvPosOff);
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
			data_out.writeInt(tbgColor);
			data_out.writeInt(tfgColor);
			data_out.writeFloat(IBC.tapZoomRatio);
			data_out.writeFloat(IBC.doubleClickZoomLevel1);
			data_out.writeFloat(IBC.doubleClickZoomLevel2);
			
			data_out.writeByte(firstVersionFlag);
			
			data_out.writeShort(maxMatchChars);
			data_out.writeShort(minMatchChars);
			data_out.writeShort(minParagraphWords);
			
			//CMN.Log("saved::minMatchChars::", minMatchChars, maxMatchChars);
			
			data_out.writeFloat(IBC.tapZoomXOffset);
			data_out.writeFloat(IBC.doubleClickPresetXOffset);
			
			data_out.close();
			
			/* Just mark as dirty. */
			//if(!CMN.bForbidOneSpecFile && a!=null){
			//	CMN.Log("putted", save_name);
			//	a.dirtyMap.add(save_name);
			//}
			//UIProjects.put(save_name, data);
			putBookOptions(context, historyCon, bookImpl.getBooKID(), bos.getArray(MainActivityUIBase.ConfigSize), bookImpl.getFile().getPath(), save_name);
			isDirty = false;
			
			readConfigs(a, a.prepareHistoryCon());
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	public float webScale=0;

	public void readConfigs(Context context, LexicalDBHelper historyCon) throws IOException {
		DataInputStream data_in1 = null;
		if(context==null) return;
		try {
			CMN.rt();
			byte[] data = bookImpl.getOptions();
			if(data==null) data=getBookOptions(context, historyCon, bookImpl.getBooKID(), bookImpl.getFile().getPath(), bookImpl.getDictionaryName());
			int extra = MainActivityUIBase.ConfigExtra;
			if(data!=null) {
				bookImpl.setOptions(data);
				data_in1 = new DataInputStream(new ByteArrayInputStream(data, extra, data.length-extra));
			} else {
				bookImpl.setOptions(new byte[extra+MainActivityUIBase.ConfigSize]);
			}
			if(data_in1!=null) {
				//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
				//IBC.doubleClickXOffset = ((float)Math.round(((float)data_in1.read())/255*1000))/1000;
				//IBC.doubleClickPresetXOffset = ((float)Math.round(((float)data_in1.read())/255*1000))/1000;
				data_in1.read();
				data_in1.read();
				firstFlag = data_in1.readByte();
				
				bgColor = data_in1.readInt();
				internalScaleLevel = data_in1.readInt();
				lvPos = data_in1.readInt();
				lvClickPos = data_in1.readInt();
				lvPosOff = data_in1.readInt();
				int scoll = data_in1.readInt(), csoll=data_in1.readInt();
				float scale=data_in1.readFloat();
				ScrollerRecord record = new ScrollerRecord(0,0,BookPresenter.def_zoom);
				avoyager.put(lvClickPos, record);
				firstFlag |= data_in1.readLong();
				//CMN.Log(bookImpl.getDictionaryName(), firstFlag, "列表位置",lvPos,lvClickPos,lvPosOff);
				//CMN.Log(bookImpl.getDictionaryName()+"页面位置",record.x,record.y,webScale);
				tbgColor = data_in1.readInt();
				tfgColor = data_in1.readInt();
				IBC.tapZoomRatio = data_in1.readFloat();
				IBC.doubleClickZoomLevel1  = data_in1.readFloat();
				IBC.doubleClickZoomLevel2  = data_in1.readFloat();
				firstVersionFlag = data_in1.readByte();
				// 3 + (9+4)*4 + 8 + 1 = 64
				maxMatchChars = data_in1.readShort();
				minMatchChars = data_in1.readShort();
				minParagraphWords = data_in1.readShort();
				// 70
				IBC.tapZoomXOffset = data_in1.readFloat();
				IBC.doubleClickPresetXOffset = data_in1.readFloat();
				// 78
			}
			//CMN.pt(bookImpl.getDictionaryName()+" id="+bookImpl.getBooKID()+" "+data+" 单典配置加载耗时");
			//CMN.pt(bookImpl.getDictionaryName()+" lvClickPos="+lvPos+" lvPosOff="+lvPosOff);
		} catch (Exception e) {
			CMN.Log(e);
			//firstFlag = 0;
		} finally {
			FFStamp = firstFlag;
			if(data_in1!=null) data_in1.close();
		}
		if(PDICMainAppOptions.getSimpleMode()) {
			// 调试
			lvPos=0;lvPosOff=0;
		}
		bReadConfig = true;
		IBC.firstFlag = firstFlag;
		boolean b1=IBC.tapZoomRatio ==0;
		// todo 变色
		int MainBackground = context instanceof MainActivityUIBase?((MainActivityUIBase)context).MainBackground:Color.GRAY;
		if(b1) {
			/* initialise values */
			IBC.tapZoomRatio =2.25f;
			tbgColor = PDICMainAppOptions.getTitlebarUseGlobalUIColor()?MainBackground:opt.getTitlebarBackgroundColor();
			tfgColor = opt.getTitlebarForegroundColor();
		}
		if ((firstVersionFlag&0x1)==0)
		{
			tbgColor = PDICMainAppOptions.getTitlebarUseGlobalUIColor()?MainBackground:opt.getTitlebarBackgroundColor();
			tfgColor = opt.getTitlebarForegroundColor();
			CMN.debug("初始化词典设置", this);
			if (getIsWebx()) {
				setShowToolsBtn(true);
				setImageBrowsable(false);
				setAcceptParagraph(getWebx().getIsTranslator());
				setDrawHighlightOnTop(getWebx().getDrawHighlightOnTop());
				int mirror = getWebx().getJson().getIntValue("defUseMirror");
				setUseMirrors(mirror>1 || mirror==-1 && ViewUtils.littleCat);
			}
			if(b1)IBC.setPresetZoomAlignment(3);
			IBC.doubleClickPresetXOffset = 0.12f;
			IBC.tapZoomXOffset = 0.12f;
			minMatchChars = 1;
			maxMatchChars = 20;
			minParagraphWords = 8;
			bgColor=CMN.GlobalPageBackground;
			
			firstVersionFlag|=0x1;
			isDirty = true;
		}
		if (getIsWebx()) {
			setImageBrowsable(false);
			if (checkVersionBefore_5_4())
			{
				setDrawHighlightOnTop(getWebx().getDrawHighlightOnTop());
				uncheckVersionBefore_5_4(false);
			}
			setDrawHighlightOnTop(getWebx().getDrawHighlightOnTop());
			
			if (getUseMirrors()) getWebx().setMirroredHost(getMirrorIdx());
			if(a!=null) a.registerWebx(this);
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
					//CMN.debug("THIS FILE DESCRIPTOR IS NULL!!!", book_id, path, name);
					return null;
				}
				//CMN.debug("HAS OPTIONS::", book_id, path, name);
				FileInputStream fin = new FileInputStream(fd.getFileDescriptor());
				ReusableByteOutputStream out = new ReusableByteOutputStream(MainActivityUIBase.ConfigSize);
				out.write(fin, true);
				fin.close();
				//CMN.Log("getBytesLegal::", out.getBytesLegal()==out.getBytes(), out.getBytesLegal().length, out.getBytes().length);
				return out.getArray(MainActivityUIBase.ConfigSize);
			} catch (Exception e) {
				//CMN.debug("THIS IS NULL!!!", book_id, path, name);
				CMN.debug(e);
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
			val = Integer.parseInt(value);
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
		ssb.append("\r\n\r\n");
		init_clickspan_with_bits_at(view, tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1, 4, 1,2,md);//缩放
		ssb.append("\r\n\r\n");
		int start = ssb.length();
		ssb.append("[").append(DictOpt[3]).append("]");
		ssb.setSpan(new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				if(context instanceof MainActivityUIBase) ((MainActivityUIBase) context).showBookPreferences(context, md);
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
			// 翻页设置可能变化
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
						mmTmp.onValueChanged(view, val, mask, flagPosition, processId);
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
	public void onValueChanged(WebViewmy mWebView, int val, int mask, int flagPosition, int processId) {
		CMN.Log("onValueChanged::", mWebView, processId);
		firstFlag &= ~(mask << flagPosition);
		firstFlag |= (val << flagPosition);
		if(mWebView==null)
			mWebView = this.mWebView;
		isDirty=true;
		if(mWebView!=null) {
			switch(processId){
				case 1:{
					if (getUseInternalBG()) {
						if(PDICMainAppOptions.getInheritGlobleWebcolorBeforeSwichingToInternal())
							bgColor = CMN.GlobalPageBackground;
						if (!(a.isCombinedViewAvtive() && getIsolateImages()))
							mWebView.setBackgroundColor(bgColor);
					} else if (!(a.isCombinedViewAvtive() && getIsolateImages()))
						mWebView.setBackgroundColor(CMN.GlobalPageBackground);
					
				} break;
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
		CMN.Log("hasBookmark::", entryName);
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
		if(debuggingSlots!=null) {
			debuggingSlots.clear();
		}
		if(mWebView!=null) {
			if(opt.getReloadWebView()) {
				ViewGroup rl = this.rl;
				WebViewmy wv = this.mWebView;
				viewsHolderReady = false;
				mTBtnStates = 0;
				initViewsHolder(a);
				mWebView.fromCombined = wv.fromCombined;
				if (ViewUtils.replaceView(this.rl, rl).getParent()!=null) {
					renderContentAt(-1, RENDERFLAG_NEW, wv.frameAt, null, wv.currentRendring);
				}
				checkTint();
				wv.shutDown();
			} else {
				mWebView.setTag(null);
				mWebView.loadUrl("about:blank");
				//mWebView.clearCache(false);
				try { // clear SparseArray tags
					ViewUtils.execSimple("$.mKeyedTags.clear()", ViewUtils.reflectionPool, mWebView);
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
		if(isWebx)
			a.registerWebx(this);
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
		String ret=bookImpl.getDictionaryName();
		return ret==null?"":ret;
	}
	
	public String getInListName() {
		String ret=bookImpl.getDictionaryName();
		return ret.endsWith(".mdx") ? ret.substring(0, ret.length() - 4) : ret;
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
	
	public void findAllNames(String searchTerm, BookPresenter book, PDICMainActivity.AdvancedSearchInterface searchLayer) throws IOException {
		bookImpl.flowerFindAllKeys(searchTerm, book, searchLayer);
	}
	
	public void findAllTexts(String searchTerm, BookPresenter book, PDICMainActivity.AdvancedSearchInterface searchLayer) throws IOException {
		bookImpl.flowerFindAllContents(searchTerm, book, searchLayer);
	}
	
	@Override
	public String toString() {
		return "BookPresenter{" +
				"bookImpl=" + (bookImpl==null?"":bookImpl.getDictionaryName()) +
				", id=" + idStr +
				", isMergedBook=" + isMergedBook() +
				'}';
	}
	
	public void invokeToolsBtn(WebViewmy mWebView, int quickAction) {
		a.weblist = mWebView.weblistHandler;
		a.getVtk().setInvoker(this, mWebView, null, null);
		if (quickAction!=-1) {
			// force quick action without showing the dialog!
			a.getVtk().bFromWebView = true;
			a.getVtk().onItemClick(null, null, 0, PDICMainAppOptions.toolsQuickAction(), false, false);
		} else {
			a.getVtk().onClick(/*trust webview selection*/a.anyView(R.id.tools));
		}
	}
	
	public void ApplyPadding(WebViewmy mWebView) {
		if (PDICMainAppOptions.padBottom())
		{
			if (CMN.GlobalPagePadding==null) {
				CMN.GlobalPagePadding = opt.getString("GPP", "50px");
			}
			mWebView.evaluateJavascript("document.body.style.paddingBottom='"+CMN.GlobalPagePadding+"'", null);
		}
	}
	
	public PlaceHolder getPlaceHolder() {
		return placeHolder;
	}
	
	public void setUseMirrors(boolean val) {
		setUseMirrorsInternal(val);
		if (getIsWebx()) {
			a.unregisterWebx(this);
			getWebx().setMirroredHost(val?-2:-1);
			a.registerWebx(this);
		}
	}
}

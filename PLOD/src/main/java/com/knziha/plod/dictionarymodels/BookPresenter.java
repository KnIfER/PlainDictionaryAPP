package com.knziha.plod.dictionarymodels;

import static com.knziha.plod.PlainUI.PageMenuHelper.SelectHtmlObject;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_BOOK_ANNOT_v2;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_BOOK_NOTE_v2;
import static com.knziha.plod.db.LexicalDBHelper.TABLE_BOOK_v2;
import static com.knziha.plod.dictionary.SearchResultBean.SEARCHTYPE_SEARCHINNAMES;
import static com.knziha.plod.dictionary.mdBase.fullpageString;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_DSL;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_EMPTY;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_MDICT;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_PDF;
import static com.knziha.plod.dictionarymodels.DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_TEXT;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
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
import android.webkit.WebSettings;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.PlainUI.PageMenuHelper;
import com.knziha.plod.PlainUI.PopupMenuHelper;
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
import com.knziha.plod.plaindict.CharSequenceKey;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.MainActivityUIBase.VerseKit;
import com.knziha.plod.plaindict.MdictServer;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.plaindict.VersionUtils;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.plaindict.databinding.ContentviewItemBinding;
import com.knziha.plod.searchtasks.lucene.WordBreakFilter;
import com.knziha.plod.widgets.AdvancedNestFrameView;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.widgets.XYTouchRecorder;
import com.knziha.text.BreakIteratorHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.knziha.metaline.Metaline;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.InflaterOutputStream;

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
	
	public final static String js="</style><script class=\"_PDict\" src=\"//mdbr/SUBPAGE.js\"></script>";
	
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
	
	/**console.log('webpc loading!!!');if(!window.webpc)window.addEventListener('click',window.webpc=function(e) {
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
	 }, false, false);console.log('webpc loaded!!!', window.webpc);*/
	@Metaline()
	public final static String imgLoader =StringUtils.EMPTY;
	
	/**(function(src){var w=window,d=document,sty,tt,t0,sty;
		function NoneSel(e){
	 		return getComputedStyle(e).userSelect=='none'
	 	}
		function selectInternal(e){
			w._touchtarget_lck=!0;
	 		tt = w._touchtarget; t0 = tt; sty=0;
	 		if(tt){
				var fc = 0;
	 			tt.userSelect='text';
				while(tt.tagName!="A"&&(w==0||tt.tagName!="IMG")) {
					tt = tt.parentElement;
					if(tt==null||++fc>=9)return -3;
	 				tt.userSelect='text';
				}
	 			if(NoneSel(tt)) {
					sty = d.createElement("style");
	 				sty.id = "tmpSel";
					sty.innerHTML = "*{user-select:text !important}";
					d.head.appendChild(sty);
					if(NoneSel(tt)) {
						return -1;
					}
				}
				if(fc>0) {
					w._touchtarget=tt;
				}
				var sel = (w.subw||w).getSelection();
				var range = d.createRange();
				range.selectNodeContents(t0);
				sel.empty();
				sel.addRange(range);
	 			var ret = 0;
	 			if(!sel.isCollapsed){
	 				if(e==0) {
						tt.tmpHref = tt.getAttribute("href");
						tt.removeAttribute("href");
				 	}
	 				ret = 1;
	 			}
	 			return ret;
	 		}
	 		return -2;
		}
		function restore(){
	 		if(tt && tt.tmpHref) tt.setAttribute("href", tt.tmpHref);
	 		w._touchtarget=tt=0;
	 		w._touchtarget_lck=!1;
	 		if(sty) sty.reomve();
		}
		function selectTouchtarget(e){
	 		var ret = selectInternal(e);
			if(ret<=0||e==1) restore();
	 		else setTimeout(restore, 300);
	 		return ret;
		}
	 	return selectTouchtarget(src)
	 })
	 */
	@Metaline()
	public final static String touchTargetLoader=StringUtils.EMPTY;
	
	public final static String touchTargetLoader_getText="window._touchtarget?window._touchtarget.innerText:''";
	
	/** var w=window,d=document;//!!!高亮开始
		var MarkLoad,MarkInst;
		var results=[], current,currentIndex = 0;
		var currentClass = "current";
	 */
	@Metaline()
	public final static byte[] markJsLoader=SU.EmptyBytes;
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
	protected String lastSch;
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
	
	/** set by {@link PDICMainAppOptions#getAllowPlugRes} */
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
	
	@Metaline(flagPos=6) public boolean getIsDedicatedFilter(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=6) public void setIsDedicatedFilter(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	/** 内容可重载（检查数据库或文件系统中的重载内容） */
	@Metaline(flagPos=7) public boolean getContentEditable(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=7) public void setContentEditable(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	/** 内容可编辑（处于编辑状态） */
	@Metaline(flagPos=8) public boolean getEditingContents(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=8) public void setEditingContents(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	
	
	@Metaline(flagPos=5) public boolean getUseInternalBG(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=5) public void setUseInternalBG(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=4) public boolean getUseInternalFS(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=4) public void setUseInternalFS(boolean val){ firstFlag=firstFlag; throw new RuntimeException(); }
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
	@Deprecated @Metaline(flagPos=26, shift=1) public boolean getRecordHiKeys(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=27) public boolean getOfflineMode(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=28) public boolean getLimitMaxMinChars(){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=29) public boolean getAcceptParagraph(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=29) public void setAcceptParagraph(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=30) public boolean getUseInternalParagraphWords(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=30) public void setUseInternalParagraphWords(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=31, shift=1) public boolean getImageBrowsable(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=31, shift=1) public void setImageBrowsable(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=32) public boolean getAutoFold(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=32) public void setAutoFold(boolean val){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	@Metaline(flagPos=33) public boolean getDrawHighlightOnTop(){ firstFlag=firstFlag; throw new RuntimeException(); }
	@Metaline(flagPos=33) public void setDrawHighlightOnTop(boolean value){ firstFlag=firstFlag; throw new RuntimeException(); }
	
	
	@Metaline(flagPos=34, shift=1) public boolean checkVersionBefore_5_4() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=34, shift=1) public void uncheckVersionBefore_5_4(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}


	@Metaline(flagPos=35) public boolean isMergedBook_real() { firstFlag=firstFlag; throw new RuntimeException();}
	public boolean isMergedBook() { return isMergedBook_real()||getDictionaryName().equals("empty"); }
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
	
	@Metaline(flagPos=46) public boolean padSet() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=46) public void padSet(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=47, shift=1) public boolean padLeft() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=47, shift=1) public void padLeft(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=48, shift=1) public boolean padRight() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=48, shift=1) public void padRight(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=49) public boolean hasWebEntrances() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=49) public void hasWebEntrances(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=50) public boolean padBottom() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=50) public void padBottom(boolean val) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=51, shift=1) public boolean tapschWebStandalone() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=51, shift=1) public void tapschWebStandalone(boolean v) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=52) public boolean tapschWebStandaloneSet() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=52) public void tapschWebStandaloneSet(boolean v) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=53, flagSize=4) public int zhoAny() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=53) public boolean zhoVer() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=54) public boolean zhoHor() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=55) public boolean zhoHigh() { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=56) public boolean verTex() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=56) public void verTex(boolean value) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=57) public boolean verTexSt() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=57) public void verTexSt(boolean value) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=58) public boolean allowPlugCss() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=58) public void allowPlugCss(boolean value) { firstFlag=firstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=59) public boolean allowFZero() { firstFlag=firstFlag; throw new RuntimeException();}
	@Metaline(flagPos=59) public void allowFZero(boolean value) { firstFlag=firstFlag; throw new RuntimeException();}
	
	
	public boolean getSavePageToDatabase(){
		return true;
	}
	public int bmCBI=0,bmCCI=-1;
	
	public PhotoBrowsingContext IBC = new PhotoBrowsingContext();
	
	private int bgColor;
    private int tbgColor;
	public int tfgColor;
	
	
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
	
	public int getTitleBackground(Toastable_Activity a) {
		final boolean useInternal = getUseTitleBackground();
		if(useInternal) return tbgColor;
		return GlobalOptions.isDark?opt.getTitlebarBackgroundColor(Color.BLACK)
				:opt.getTitlebarBackgroundColor(a.MainBackground);
	}
	
	public int getTitleForeground(Toastable_Activity a) {
		final boolean useInternal = getUseTitleBackground();
		if(useInternal) return tfgColor;
		return GlobalOptions.isDark?opt.getTitlebarForegroundColor(Color.WHITE)
				: opt.getTitlebarForegroundColor(a.tintListFilter.sForeground);
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
	
	public final static String htmlBase="<!DOCTYPE html><html><meta name='viewport' content='initial-scale=1,user-scalable=yes' class=\"_PDict\"><head><style class=\"_PDict\">html,body{width:auto;height:auto;}img{max-width:100%;}mark{background:yellow;}mark.current{background:orange;border:0px solid #FF0000}.PLOD_UL{border-bottom:2.5px solid black}";
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
		
		if(THIS!=null) {
			readConfigs(THIS, THIS.prepareHistoryCon());
			if (isWebx && hasWebEntrances()) {
				getWebx().readEntrances(false);
			}
			if (allowFZero()) {
				bookImpl.plugFZero(true, true, true);
			}
		}
		
		if((pseudoInit&1)==0) {
			//init(getStreamAt(0)); // MLSN
			File p = fullPath.getParentFile();
			if(p!=null && p.exists()) {
				StringBuilder buffer = getCleanDictionaryNameBuilder();
				int bL = buffer.length();
				File externalFile;
				/* 外挂同名css */
				if(PDICMainAppOptions.getAllowPlugCss() || allowPlugCss()) {
					externalFile = new File(p, buffer.append(".css").toString());
					if(externalFile.exists()) {
						hasExtStyle = true;
					}
					//CMN.debug("插入 同名 css 文件::", hasExtStyle);
				}
				buffer.setLength(bL);
				externalFile = new File(p, buffer.append(".png").toString());
				/* 同名png图标 */
				if(externalFile.exists()) {
					cover = Drawable.createFromPath(externalFile.getPath());
				}
			}
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
	public WebViewmy initViewsHolder(final MainActivityUIBase a){
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

			toolbar = pageData.titleBar;
			ViewUtils.setOnClickListenersOneDepth(toolbar, this, 999, null);
			
			mWebView.pBc = IBC;
			mWebView.titleBar = (AdvancedNestFrameView) toolbar;
			mWebView.titleBar.appbar = a.appbar;
			mWebView.FindBGInTitle(a, toolbar);
			//mWebView.toolbarBG.setColors(mWebView.ColorShade);
			
			mWebView.toolbar_title = toolbar_title = pageData.toolbarTitle;
			toolbar_title.earHintAhead = "内容标题：";
			toolbar_cover = pageData.cover;
			if(cover!=null) toolbar_cover.setImageDrawable(cover);
			mWebView.rl = rl;
			if (bookImpl!=null) {
				toolbar_title.setText(bookImpl.getDictionaryName());
			}
			toolbar_title.setMaxLines(1);
			toolbar_title.setOnLongClickListener(this);
			viewsHolderReady=true;
			
			mWebView.toolbar_cover = toolbar_cover;
			
			if(cover==null){
				toolbar_cover.setBackground(null);
			}
			toolbar_cover.setClickable(false);
			if (isWebx) {
				String webSetttings = getWebx().getField("webSetttings");
				WebSettings settings = mWebView.getSettings();
				settings.setMediaPlaybackRequiresUserGesture(true);
				if (webSetttings!=null) {
					try {
						ViewUtils.execSimple(webSetttings, null, settings);
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			}
			if (getType()==PLAIN_TYPE_PDF) {
				mWebView.getSettings().setMinimumFontSize(1);
				mWebView.getSettings().setTextZoom(100);
			}
		}
		return mWebView;
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
			case R.id.toolbar_title:
				showMoreToolsPopup(null, v);
				break;
			case R.id.save:
			case R.id.tools:
//				WebViewmy _mWebView = mWebView;
//				String url = currentDisplaying;
//				if(v.getParent()!=toolbar){
//					if(a.peruseView !=null){
//						_mWebView = a.peruseView.mWebView;
//						url = a.peruseView.currentDisplaying();
//					} else {
//						return true;
//					}
//				}
//				OptionListHandlerDyn olhd = new OptionListHandlerDyn(a, _mWebView, url);
//				int[] utils = null;
//				if (bookImpl != null) {
//					utils = ((DictionaryAdapter) bookImpl).getPageUtils(false);
//				}
//				if (utils==null) {
//					utils = new int[]{
//						R.string.page_source
//						,R.string.bmAdd
////						,R.string.page_yuan
////						,R.string.page_del
//						,R.string.page_ucc
//					};
//				}
//				buildStandardOptionListDialog(a, R.string.page_options, 0, utils
//						, olhd, url, olhd, 0);
				break;
		}
		return false;
	}
	
	public PopupMenuHelper showPopupMenu(PageMenuHelper.PageMenuType type, WebViewmy mWebView, View v, int x, int y) {
		try {
			if (mWebView == null) {
				initViewsHolder(a);
				mWebView = this.mWebView;
			}
			a.weblist = mWebView.weblistHandler;
			return a.pageMenuHelper.showPageMenu(type, mWebView, v, x, y);
		} catch (Exception e) {
			CMN.debug(e);
		}
		return null;
	}
	
	public void showMoreToolsPopup(WebViewmy mWebView, View v) {
		PageMenuHelper.PageMenuType type = PageMenuHelper.PageMenuType.Nav_main;
		if (getIsWebx()) {
			type = PageMenuHelper.PageMenuType.Nav_WEB;
		}
		PopupMenuHelper popupMenu;
		if (v.getId() == R.id.dopt) {
			// ViewUtils.getNthParentNonNull(v, 1)
			popupMenu = showPopupMenu(type, mWebView, v, -v.getWidth(), v.getHeight()/2);
		} else {
			popupMenu = showPopupMenu(type, mWebView, v, 0, 0);
		}
		if (popupMenu!=null) {
			mWebView = (WebViewmy) popupMenu.tag1;
			boolean b1=mWebView.canGoBack();
			v = popupMenu.popRoot.findViewById(R.id.nav_back);
			v.setEnabled(b1);
			v.setAlpha(b1?1:0.35f);
			b1=mWebView.canGoForward();
			v = popupMenu.popRoot.findViewById(R.id.nav_forward);
			v.setEnabled(b1);
			v.setAlpha(b1?1:0.35f);
		}
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
						mWebView.evaluateJavascript(touchTargetLoader_getText, new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
								a.copyText(StringEscapeUtils.unescapeJava(value.substring(1,value.length()-1)), true);
							}
						});
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
	
	//click
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
//			case R.id.cover:
////				CMN.debug("toolbar_cover onClick", isMergedBook());
//				if(false) {
//					showDictTweaker(mWebView, a, this);
//					break;
//				}
//				a.showDictTweakerMain(this, mWebView);
//				break;
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
				onLongClick(a.anyView(R.id.save));
				break;
			case R.id.dopt:
				showMoreToolsPopup(null, v);
				break;
			case R.id.titleBar:
			case R.id.toolbar_title:
//				CMN.debug("toolbar_title onClick");
				mWebView.weblistHandler.pageSlider.bSuppressNxtTapZoom = CMN.now();
				if(mWebView.getVisibility()!=View.VISIBLE) {
					mWebView.setAlpha(1);
					mWebView.setVisibility(View.VISIBLE);
					if(mWebView.awaiting){
						mWebView.awaiting=false;
						renderContentAt(-1, RENDERFLAG_NEW, -1, null, mWebView.currentRendring);
					}
				}
				//else if(!mWebView.weblistHandler.isViewSingle()) {
				else if(ViewUtils.getNthParentNonNull(mWebView.rl, 1).getId()==R.id.webholder) {
					mWebView.setVisibility(View.GONE);
				} else {
					//toolbar_cover.performClick();
					a.showDictTweakerMain(this, mWebView);
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
	
	public void setDictionaryName(String toString) {
		// nimp
	}
	
	public void updateFile(File f) {
		// nimp
	}
	
	public String GetAppSearchKey() {
		return a.getSearchTerm();
	}
	
	public final String GetSearchKey() {
		return searchKey;
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
			int res = bookImpl.lookUpRange(keyword, range_query_reveiver, null, bookImpl.getBooKID(), 15, task, false);
			if (res!=0) {
				for (int i = 0; i < a.forms.size(); i++) {
					UniversalDictionaryInterface forma = a.forms.get(i);
					int alternate = forma.guessRootWord(bookImpl, keyword);
					if (true && alternate>=0) {
						String alter = bookImpl.getEntryAt(alternate);
						CMN.debug("alternate::", bookImpl.getEntryAt(alternate));
						bookImpl.lookUpRangeQuick(alternate, alter, range_query_reveiver, null, bookImpl.getBooKID(), 7, task, false);
					}
				}
			}
			return res;
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
		json.put("tbg", SU.toHexRGB(getTitleBackground(a)));
		json.put("tfg", SU.toHexRGB(getTitleForeground(a)));
		json.put("bg", getUseInternalBG()?SU.toHexRGB(getContentBackground()):null);
		json.put("img", getImageBrowsable() && bookImpl.hasMdd());
		if(cover!=null) json.put("ico", true);
		if(verTex()) json.put("ver", true);
		PlainWeb webx = getWebx();
		if(webx!=null) {
			json.put("isWeb", 1);
			if(webx.hasField("synthesis") && PDICMainAppOptions.allowMergeSytheticalPage())
				json.put("synth", 1);
			String sch = webx.getSearchUrl();
			json.put("sch", sch);
		}
		return json;
	}
	
	public ConcurrentHashMap<String, Integer> debuggingSlots;
	
	public InputStream getDebuggingResource(String uri) throws IOException {
		if(debuggingSlots==null) {
			debuggingSlots=new ConcurrentHashMap<>(32);
		}
		Integer val = debuggingSlots.get(uri);
		if (val != null || debuggingSlots.size() < 24) {
			String p = getPath();
			p = p.substring(0, p.lastIndexOf(File.separator));
			File file = new File(p + uri).getCanonicalFile();
			CMN.debug("getDebuggingResource::", file, "exists="+file.exists(), p, val);
			if (file.getPath().startsWith(p)) {
				if (val == null) {
					debuggingSlots.put(uri, val = file.exists() ? 1 : 0);
				}
				if (val == 1) {
					CMN.debug("getDebuggingResource!!!");
					return new AutoCloseInputStream(new FileInputStream(file));
				}
			}
		} else {
			CMN.debug("getDebuggingResource::rejected", val, "size="+debuggingSlots.size());
		}
		return null;
	}
	
	public boolean getIsNonSortedEntries() {
		return mType!=PLAIN_TYPE_MDICT && mType!=PLAIN_TYPE_DSL;
	}
	
	public boolean store(int pos) {
		if (getType()==PLAIN_TYPE_PDF) {
			return false;
		}
		return true;
	}
	
	public boolean hasPreview() {
		return mType==PLAIN_TYPE_MDICT || mType== PLAIN_TYPE_DSL || mType== PLAIN_TYPE_TEXT;
	}
	
	
	static class HtmlObjectHandler implements View.OnLongClickListener {
		MainActivityUIBase a;
		public HtmlObjectHandler(MainActivityUIBase a) {
			this.a = a;
		}
		@Override
		public boolean onLongClick(View v) {
			WebViewmy wv = (WebViewmy) v;
			if(wv.presenter.suppressingLongClick)
				return false;
			if(wv.weblistHandler.pageSlider.twiceDetected)
				return true;
			wv.lastLongSX = wv.getScrollX();
			wv.lastLongSY = wv.getScrollY();
			wv.lastLongScale = wv.webScale;
			wv.lastLongX = wv.lastX;
			wv.lastLongY = wv.lastY;
			WebViewmy.HitTestResult result = wv.getHitTestResult();
			if (null == result) return false;
			int type = result.getType();
			CMN.debug("getHitTestResult", type, result.getExtra());
			a.pageMenuHelper.lnk_href = result.getExtra();
			//ViewUtils.preventDefaultTouchEvent(wv.rl, (int) wv.lastX, (int) wv.lastY);
			switch (type) {
				/* 长按下载图片 */
				case WebViewmy.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
				case WebViewmy.HitTestResult.IMAGE_TYPE:{
					wv.presenter.showPopupMenu(PageMenuHelper.PageMenuType.LNK_IMG, wv, (View) wv.getParent(), 0, 0);
					if(true) return true;
					String url = result.getExtra();
					AlertDialog.Builder builder3 = new AlertDialog.Builder(a);
					builder3.setSingleChoiceItems(new String[] {}, 0,
							(dialog, pos) -> {
								switch(pos) {
									case 0:{
									} break;
									case 1:{
										SelectHtmlObject(a, wv, 1);
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
					
					String[] Menus = null;
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
					a.pageMenuHelper.lnk_href = result.getExtra();
					wv.presenter.showPopupMenu(wv.presenter.getIsWebx()?PageMenuHelper.PageMenuType.LNK_WEB:PageMenuHelper.PageMenuType.LNK, wv, wv, 0, 0);
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
			//if(!a.getUsingDataV2()) getCon(true).enssurePageTable();
		}
		String url= getSaveUrl(mWebView); // deprecated
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

	/** get bookmark save url */
	@Nullable
	public String getSaveUrl(WebViewmy mWebView) {
		String url=mWebView.url;
		CMN.debug("getSaveUrl::", url);
		if (a == null || url==null) return null;
		int schemaIdx = url.indexOf(":");
		boolean mdbr = url.regionMatches(schemaIdx+3, "mdbr", 0, 4);
		if (mdbr) {
			if (url.regionMatches(schemaIdx + 12, "view", 0, 4)) {
			
			} else {
				url=bookImpl.getEntryAt(mWebView.currentPos);
			}
		} else {
			if (isWebx) {
				PlainWeb webx = getWebx();
				if (url!=null && url.startsWith(webx.host) && !webx.host.equals(webx.host0)) {
					url = webx.host0+url.substring(webx.host.length());
				}
			}
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
//				pageView.undo.setVisibility(editable?View.VISIBLE:View.GONE);
//				pageView.redo.setVisibility(editable?View.VISIBLE:View.GONE);
//				if (editable && (supressingEditing==(pageView.undo.getAlpha()==1))) {
//					pageView.undo.setAlpha(supressingEditing?0.5f:1);
//					pageView.redo.setAlpha(supressingEditing?0.5f:1);
//				}
//				pageView.save.setVisibility((targetStats&TBTN_SAVE)!=0?View.VISIBLE:View.GONE);
//				pageView.tools.setVisibility((targetStats&TBTN_TOOL)!=0?View.VISIBLE:View.GONE);
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
			if (isWebx && idx<=getWebx().entrance.size()) {
				word = mWebView.weblistHandler.getSearchKey();
			} else {
				int tailIdx=word.lastIndexOf(":");
				if(tailIdx>0)
					word=word.substring(0, tailIdx);
			}
		}
		//CMN.debug("setCurrentDis", word);
		if (mWebView.weblistHandler==a.weblistHandler) {
			currentDisplaying = word;
		}
		if (mWebView.weblistHandler.isViewSingle()) {
			mWebView.weblistHandler.setStar(word);
		}
		if (mWebView.toolbar_title!=null) {
			mWebView.toolbar_title.setText(bookImpl.AcquireStringBuffer(64).append(word.trim()).append(" - ").append(bookImpl.getDictionaryName()).toString());
			if(mWebView.titleBar!=null) {
				mWebView.showTitleBar(true);
				if(mWebView.weblistHandler == a.weblistHandler && a.newTitlebar.isActived) {
					a.newTitlebar.setTitlebar(mWebView);
				}
				//mWebView.titleBar.setAccessibilityPaneTitle(mWebView.toolbar_title.getText());
			}
		}
		mWebView.word(word);
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
		mWebView.word(word);
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
		if(bg==0&&mWebView.weblistHandler.bShowingInPopup) bg = a.AppWhite; // todo opt
		if(mWebView.weblistHandler.bShowingInPopup) bg = myWebColor;
		mWebView.setBackgroundColor(bg);
		/* check and set colors for toolbar title Background*/
		if(mWebView==this.mWebView){
			mWebView.titleBar.fromCombined = mWebView.fromCombined==1;
		} else if(mWebView.toolbar_cover!=null) {
			mWebView.toolbar_cover.setImageDrawable(cover);
		}
		FlowTextView toolbar_title = mWebView.toolbar_title;
		if(toolbar_title!=null) {
			//todo impl start level
//			int StarLevel =  PDICMainAppOptions.getDFFStarLevel(firstFlag);
//			toolbar_title.setStarLevel(StarLevel);
//			if(StarLevel>0) {
//				toolbar_title.setStarDrawables(a.getActiveStarDrawable()
//						, toolbar_title==a.wordPopup.entryTitle() ?a.getRatingDrawable():null);
//			}
		}
//		int defTH = 0;
//		if (mWebView.fromCombined!=1 && PDICMainAppOptions.customTitlebarHeight()) {
//			defTH = (int) (opt.getInt("ttH", 0)  * GlobalOptions.density);
//		}
//		mWebView.titleBarHeight(defTH);
		if (mWebView.fromCombined==1) {
			mWebView.titleBarHeight(0);
		}
		GradientDrawable toolbarBG = mWebView.toolbarBG;
		if(toolbarBG!=null) {
			useInternal = getUseTitleBackground();
			myWebColor = useInternal? tbgColor
					:isDark?opt.getTitlebarBackgroundColor(Color.BLACK)
					:opt.getTitlebarBackgroundColor(a.MainBackground);
			CMN.debug("使用内置标题栏颜色："+getUseTitleBackground(), this, bookImpl.getDictionaryName(), isDark, Integer.toHexString(myWebColor));
			int colorTop = PDICMainAppOptions.getTitlebarUseGradient()?ColorUtils.blendARGB(myWebColor, Color.WHITE, 0.08f):myWebColor;
			int[] ColorShade = mWebView.ColorShade;
			if(ColorShade[1]!=myWebColor||ColorShade[0]!=colorTop)
			{
				ColorShade[1] = myWebColor;
				ColorShade[0] = colorTop;
				toolbarBG.setColors(ColorShade);
				//mWebView.toolbar_title.invalidate();
			}
			myWebColor = getUseTitleForeground()? tfgColor
					: isDark?opt.getTitlebarForegroundColor(Color.WHITE)
					: opt.getTitlebarForegroundColor(a.tintListFilter.sForeground);
			mWebView.setTitlebarForegroundColor(myWebColor);
		}
		//CMN.pt("设置颜色：");
	}
	
	public final static int RENDERFLAG_NEW=0x1;
	public final static int RENDERFLAG_LOADURL=0x1;

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
		} else if(resposibleForThisWeb && mWebView.getParent()!=rl && mWebView.weblistHandler==a.weblistHandler) {
			ViewUtils.addViewToParent(mWebView, rl, 1);
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
						|| getNeedsAutoFolding(frameAt)) && !bViewSource){/* 自动折叠 */
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
//			int minHeight = 0;
			if(fromCombined) {
				if(rl.getLayoutParams()!=null)
					rl.getLayoutParams().height = -1;//LayoutParams.WRAP_CONTENT;
				if (verTex()) {
					mWebView.getLayoutParams().height = a.root.getHeight()/2;
				}
				else if (getContentFixedHeightWhenCombined()) {
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
//			if (rl.getMinimumHeight()!=minHeight) {
//				CMN.debug("setMinimumHeight::", minHeight);
//				mWebView.setMinimumHeight(minHeight);
//			}
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
		if (getType()!=PLAIN_TYPE_PDF) {
			sb.append(htmlBase);
			sb.append(js);
			plugCssWithSameFileName(sb);
		}
		return sb;
	}
	
	public void plugCssWithSameFileName(StringBuilder sb) {
		if(isHasExtStyle()) {
			String fullFileName = bookImpl.getDictionaryName();
			if (PDICMainAppOptions.debugCss()) {
				StringBuilder buffer = getCleanDictionaryNameBuilder();
				File externalFile = new File(f().getParent(), buffer.append(".css").toString());
				sb.append("<style>")
						.append(BU.fileToString(externalFile))
						.append("/style>");
			} else {
				int end = fullFileName.length();
				if (unwrapSuffix) {
					int idx = bookImpl.getDictionaryName().lastIndexOf(".");
					if (idx > 0) end = idx;
				}
				sb.append("<link rel='stylesheet' type='text/css' href='")
						.append(fullFileName, 0, end)
						.append(".css?f=auto'/>"); //
			}
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
//		loadUrl = true;
//    	CMN.debug("loadUrl::", loadUrl);
		try {
			if(bookImpl.hasVirtualIndex())
				try {
					String lastSch = searchKey;
					if (lastSch!=null) {
						this.lastSch = lastSch;
					}
					String validifier = getOfflineMode()&&getIsWebx()?null:bookImpl.getVirtualTextValidateJs(this, mWebView, position[0]);
//					CMN.Log("validifier::", validifier, GetSearchKey(), mWebView.getTag());
					if (validifier == null
							//|| true // 用于调试直接网页加载
							|| "forceLoad".equals(mWebView.getTag())
							|| mWebView.getUrl()==null) {
						htmlCode = bookImpl.getVirtualRecordsAt(this, position);
						mWebView.setTag(null);
						///CMN.debug("htmlCode::", htmlCode, GetSearchKey());
						if (htmlCode!=null && (
								htmlCode.startsWith("http")
								|| htmlCode.startsWith("file")
								)) {
							// 如果是加载网页
							SetSearchKey(BookPresenter.this.lastSch);
							mWebView.loadUrl(htmlCode);
							htmlCode = null;
						}
					} else {
						if (lastSch!=null) {
							this.lastSch = lastSch;
						}
						mWebView.evaluateJavascript(validifier, new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
//								CMN.Log("validifier::onReceiveValue::", value);
								if ("1".equals(value) || "true".equals(value)) {
									String effectJs = bookImpl.getVirtualTextEffectJs(BookPresenter.this, position);
									if (effectJs!=null) mWebView.evaluateJavascript(effectJs, null);
									//a.showT("免重新加载生效！");
									vartakelayaTowardsDarkMode(mWebView);
									try {
										bookImpl.getVirtualRecordAt(BookPresenter.this, position[0]);
									} catch (IOException e) {
										CMN.debug(e);
									}
									// 注意，这里会多次调用OPF中eval的脚本！
									mWebView.bPageStarted=true;
									mWebView.postFinishedAbility.run();
								}  else if("2".equals(value) && getIsWebx()) { // apply js modifier first, then do search
									if (!"schVar".equals(mWebView.getTag())) {
										mWebView.setTag("schVar");
										SetSearchKey(BookPresenter.this.lastSch);
										renderContentAt_internal(mWebView, initialScale, fromCombined, mIsolateImages, 0);
										mWebView.setTag(null);
									}
								} else {
									mWebView.setTag("forceLoad");
									SetSearchKey(BookPresenter.this.lastSch);
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
			CMN.debug("render::startsWith fullpage=", htmlCode.startsWith(fullpageString));
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
					//htmlBuilder.append(getSimplestInjection());
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
					String effectJs = bookImpl.getVirtualTextEffectJs(BookPresenter.this, mWebView.currentRendring);
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
					a.opt.DarkModeIncantation(a);
					tintBackground(webview);
					if (dark) {
						webview.evaluateJavascript(a.opt.mDarkModeJs, null);
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
		boolean styleOpened=false;
		if (mWebView.weblistHandler.bDataOnly) {
			htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;
			htmlBuilder.append("body{ margin-top:0px; margin-bottom:").append(30).append("px }");
		}
		if (a.fontFaces!=null) {
			if(!styleOpened) htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;
			htmlBuilder.append(a.fontFaces);
		}
		if (a.plainCSS!=null) {
			String plainCSS = a.plainCSS;
			if (PDICMainAppOptions.debugCss()) {
				File cssFile = new File(opt.pathToMainFolder().append("plaindict.css").toString());
				plainCSS = BU.fileToString(cssFile);
			}
			if(!styleOpened) htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;
			htmlBuilder.append(a.plainCSS);
		}
		if(mIsolateImages) {
			if(!styleOpened){ htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;}
			htmlBuilder.append("body{color:#fff;text-shadow: 5px 2.5px 10px #000;}img{display:none}");
		}
		if (padLeft() || padRight()) {
			if(!styleOpened){ htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;}
			htmlBuilder.append("body{");
			ApplyPadding(htmlBuilder);
			htmlBuilder.append("}");
		}
		if ((zhoAny()&~(1L<<55))!=0) {
			if(!styleOpened){ htmlBuilder.append("<style class=\"_PDict\">"); styleOpened=true;}
			htmlBuilder.append("html{min-height:").append(zhoHigh() ? "92%" : "100%")
					.append(";display:flex;")
					.append(zhoVer() ? "align-items:center;" : "")
					.append(zhoHor() ? "justify-content:center;" : "")
					.append(verTex() ? (verTexSt()?"writing-mode:vertical-lr;":"writing-mode:vertical-rl;") : "")
					.append("}");
			if (verTex()) {
				htmlBuilder.append(".PLOD_UL{");
				htmlBuilder.append(verTexSt()?"border-right":"border-left");
				htmlBuilder.append(": 2.5px solid rgba(0 0 0 / 58%);border-bottom:unset}");
			}
		}
		if(styleOpened) {
			htmlBuilder.append("</style>");
		}
		htmlBuilder.append("<script class=\"_PDict\">");
//		int rcsp = MakeRCSP(mWebView.weblistHandler, opt);
//		if(mWebView==a.wordPopup.mWebView) rcsp|=1<<5; //todo
		CMN.debug("view::bid=", getId(), IU.NumberToText_SIXTWO_LE(getId(), null));
		htmlBuilder.append("window.bid='").append(idStr).append("';");
		htmlBuilder.append("window.shzh=").append((mWebView.weblistHandler.tapSch?1:0)|mWebView.weblistHandler.tapSel).append(";");
		htmlBuilder.append("window.frameAt=").append(mWebView.frameAt).append(";");
		htmlBuilder.append("window.entryKey='").append(mWebView.word).append("';");
		htmlBuilder.append("window.currentPos=").append(mWebView.currentPos).append(";");
		htmlBuilder.append("window._combo=").append(mWebView.fromCombined==1).append(";");
		htmlBuilder.append("window._posid='"); IU.NumberToText_SIXTWO_LE(mWebView.currentPos, htmlBuilder);
		htmlBuilder.append("';");
		//htmlBuilder.append("hasFiles='").append(hasFilesTag()).append("';");
		
		/** see {@link AppHandler#view} */
		// 此处代码的加载略慢于图片资源
		htmlBuilder.append("app.view(sid.get(),")
				.append("bid")
				.append(",").append(mWebView.currentPos)
				.append(",").append(hasFilesTag())
				.append(");");
		
		// save the old code
		if (mWebView.currentRendring.length>1) {
			htmlBuilder.append("window._mdbrUrl=\"http://mdbr.com/content/d");
			// save the old code.
			IU.NumberToText_SIXTWO_LE(getId(), htmlBuilder);
			for (int i = 0; i < mWebView.currentRendring.length; i++) {
				htmlBuilder.append("_");
				IU.NumberToText_SIXTWO_LE(mWebView.currentRendring[i], htmlBuilder);
			}
			htmlBuilder.append("\";");
		}
		
		if (GlobalOptions.isDark) {
			//htmlBuilder.append(MainActivityUIBase.DarkModeIncantation_l);
			opt.DarkModeIncantation(a);
			mWebView.evaluateJavascript(opt.mDarkModeJs, null);
			htmlBuilder.append(opt.mDarkModeJs);
		}
		
		//htmlBuilder.append("webx=").append(getIsWebx()?1:0).append(";");
		htmlBuilder.append("</script>");
	}

	public void LoadPagelet(WebViewmy mWebView, StringBuilder htmlBuilder, String records) {
//		if (getType()==PLAIN_TYPE_MDICT) {
//			htmlBuilder.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
//			if(bookImpl.hasMdd()) htmlBuilder.append("<p class='MddExist' id='MddExist'/>");
//			htmlBuilder.append("</div>");
//		}
		htmlBuilder.append(htmlHeadEndTag).append(records);
		if (getType()==PLAIN_TYPE_MDICT) {
			htmlBuilder.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
			if(bookImpl.hasMdd()) htmlBuilder.append("<p class='MddExist' id='MddExist'/>");
			htmlBuilder.append("</div>");
		}
		htmlBuilder.append(htmlEnd);
		mWebView.loadDataWithBaseURL(mBaseUrl, htmlBuilder.toString(), null, "UTF-8", null);
	}

	public static int MakePageFlag(WebViewListHandler wlh, PDICMainAppOptions opt) {
		final int ret =
				(wlh.tapSch?1:0)
				| wlh.tapSel
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
	
	public String getRowTextAt(int pos) {
		if (pos<0 || pos>=bookImpl.getNumberEntries() || isWebx && pos<=getWebx().entrance.size()) {
			return null;
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
	public boolean checkFlag(Toastable_Activity context) {
		boolean ret = FFStamp!=firstFlag || isDirty;
		if(ret) {
			saveStates(context, context.prepareHistoryCon());
		}
		if(isWebx) bookImpl.saveConfigs(this);
		return ret;
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
		public void tintBackground(int sid) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					presenter.tintBackground(wv);
				}
			}
		}
		
		
		@JavascriptInterface
		public String tbg() {
			if (presenter!=null) {
				return SU.toHexRGB(GlobalOptions.isDark?presenter.opt.getTitlebarBackgroundColor(Color.BLACK)
						:presenter.opt.getTitlebarBackgroundColor(presenter.a.MainBackground));
			}
			return "";
		}
		
		@JavascriptInterface
		public String tfg() {
			if (presenter!=null) {
				return SU.toHexRGB(GlobalOptions.isDark?presenter.opt.getTitlebarForegroundColor(Color.WHITE)
						: presenter.opt.getTitlebarForegroundColor(presenter.a.tintListFilter.sForeground));
			}
			return "";
		}
		
		@JavascriptInterface
		public void maySound(int sid, boolean maybe) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					wv.bMaybeHasSoundOnPage = maybe;
				}
			}
		}
		
		@JavascriptInterface
		public void scrollLck(int sid, int scrollLck) {
			//CMN.debug("scrollLck::", scrollLck);
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					wv.scrollLck = scrollLck;
				}
			}
		}
		
		@JavascriptInterface
		public void scrollLckVer(int sid, boolean scrollLck) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					wv.scrollLckVer = scrollLck;
				}
			}
		}
		
		
		@JavascriptInterface
		public void randxLoaded(int sid) {
			if (presenter!=null) {
				CMN.debug("randxLoaded");
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					wv.postDelayed(new Runnable() {
						@Override
						public void run() {
							wv.weblistHandler.getViewGroup().setAlpha(1);
						}
					}, 250);
				}
			}
		}
		
		public static HashMap<String, WeakReference<String>> AjaxData = new HashMap<>();
	
		@JavascriptInterface
		public void pushAjax(int sid, String requestId, String body) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					CMN.debug("pushAjax::", requestId, body);
					AjaxData.put(requestId, new WeakReference<>(body));
				}
			}
		}
		
		/** detect char has implicit word boudary.
		 * see https://en.wikipedia.org/wiki/Category:Writing_systems_without_word_boundaries
		 * */
		@JavascriptInterface
		public boolean hexie(char ch) {
			return WordBreakFilter.isBigram(ch);
		}
		
		@JavascriptInterface
		public long probeWord(int sid, String paragraph, String text) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv != null) {
					CMN.debug("probeWord::", paragraph, text);
					BreakIteratorHelper helper = new BreakIteratorHelper();
					helper.setText(paragraph);
					int now = text.length();
					int st = helper.preceding(now);
					int ed = helper.following(st);
					if (ed <= now) {
						st = now;
						ed = helper.following(now);
					}
					try {
						CMN.debug("probeWord=", st, ed);
						long ret = (((long)st)<<32)|(long)ed;
						CMN.debug("probeWord=", paragraph.substring(st, ed), ret);
						return ret;
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			}
			return 0;
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
					//presenter.a.getMdictServer().strOpt = null;
					presenter.a.strOpt = null;
				}
			}
        }
		
		@JavascriptInterface
		public String annotRaw(int sid, String nid) {
			String ret = null;
			try {
				if (presenter!=null) {
					WebViewmy mWebView = findWebview(sid);
					if (mWebView != null) {														/*, "+LexicalDBHelper.FIELD_CREATE_TIME+"*/
						Cursor cursor = presenter.a.prepareHistoryCon().getDB().rawQuery("select lex from " + TABLE_BOOK_ANNOT_v2 + " where id=?", new String[]{nid});
						if (cursor.moveToNext()) {
							ret = cursor.getString(0);
							//CMN.Log("annotRaw::", ret, formatter.format(cs.getLong(1)));
						}
						cursor.close();
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
			return ret;
		}
		
		@JavascriptInterface
		public String editNode(int sid, String nid) {
			String ret = null;
			try {
				if (presenter!=null) {
					WebViewmy wv = findWebview(sid);
					if (wv != null) {
						wv.post(() -> wv.weblistHandler.a.annotText(wv, -1, nid));
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
			return ret;
		}
		
        @JavascriptInterface
        public long annot(int sid, String text, String annot, String entry, int pos, int tPos, int type, int color, String note, String did) {
			if (presenter!=null) {
				WebViewmy mWebView = findWebview(sid);
				if (mWebView != null) {
					CMN.debug("annot::marking", text, annot, "pos="+pos, "tPos="+tPos, mWebView.presenter, did);
					CMN.debug("annot::marking", mWebView.title, mWebView.url, presenter.a.lastNotes);
					try {
						LexicalDBHelper.increaseAnnotDbVer();
						if (presenter.a.getUsingDataV2()) {
							BookPresenter book = mWebView.presenter;
							if(did!=null) {
								book = book.a.getMdictServer().md_getByURL(did);
								if(book==null) return -1;
							}
							if (entry == null) {
								entry = mWebView.word();
							}
							if (book.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_PDF) {
								text = text.replaceAll("-\n", "");
								text = text.replaceAll("\n(?!\n)", " ");
							}
							ContentValues values = new ContentValues();
							
							String url = mWebView.url;
							CMN.debug("annot::url::", url);
							
							int schemaIdx = url.indexOf(":");
							boolean mdbr = url.regionMatches(schemaIdx+3, "mdbr", 0, 4);
							boolean view = mdbr && url.regionMatches(schemaIdx + 12, "view", 0, 4);
							boolean web = view || url.startsWith("http") && !mdbr;
							if (web) {
								url = book.getSaveUrl(mWebView); // 在线页面的标记  获取虚拟pos
								entry = mWebView.title;
								long vPos=mWebView.marked; if(vPos==-1) vPos=presenter.ensureBookMark(mWebView);
								//long vPos = book.toggleBookMark(mWebView, book.a, true);
								CMN.debug("vPos=", vPos);
								values.put("pos", vPos);
							} else {
								url = null;
//								if(mWebView.marked==-1)
//									presenter.ensureBookMark(mWebView);
								values.put("pos", pos);
							}
							
							values.put("bid", book.getId());
							values.put("entry", entry);
							values.put("lex", text);
							values.put("annot", annot);
							values.put("type", type);
							values.put("color", color);
							values.put("tPos", tPos);
							values.put("web", web);
							String notes = presenter.a.lastNotes;
							if (notes != null) {
								values.put("notes", notes);
								presenter.a.lastNotes = null;
							}
							long now = CMN.now();
							values.put(LexicalDBHelper.FIELD_EDIT_TIME, now);
							values.put(LexicalDBHelper.FIELD_CREATE_TIME, now);
							
								JSONObject json = new JSONObject();
								json.put("x", mWebView.getScrollX());
								json.put("y", mWebView.getScrollY());
								json.put("s", mWebView.webScale);
								if (url != null) {
									json.put("url", url);
								}
								values.put(LexicalDBHelper.FIELD_PARAMETERS, json.toString().getBytes());
							
							if(VersionUtils.AnnotOff) {
								presenter.a.hdl.post(()->{
									showT("笔记功能仍处于测试中，下一版本开启！");
								});
								return 0;
							}
							final long id = presenter.a.prepareHistoryCon().getDB().insert(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, null, values);
							CMN.debug("annot.id=", id);
							return id;
						}
					} catch (Exception e) { CMN.debug(e); }
				}
			}
			return -1;
        }
		
		private StringBuilder getMarksByPos(WebViewmy mWebView, StringBuilder sb, long bid, long position) {
			//if(!wv.presenter.idStr.regionMatches(0, url, idx, ed-idx))
			if(VersionUtils.AnnotOff) return sb;
			String[] where = new String[]{""+bid, ""+position};
			Cursor cursor = presenter.a.prepareHistoryCon().getDB().rawQuery("select id,annot,notes,last_edit_time==0 from "+LexicalDBHelper.TABLE_BOOK_ANNOT_v2+" where bid=? and pos=? order by tPos", where);
			//CMN.debug("cursor::", position, cursor.getCount());
//			cursor.moveToLast();
//			while (cursor.moveToPrevious()) {
			while (cursor.moveToNext()) {
				boolean bkmk = cursor.getInt(3)==1;
				final long id = cursor.getLong(0);
				// CMN.debug("bkmk::", bkmk, id);
				if (bkmk) {
					if (mWebView.merge) {
					} else {
						mWebView.marked = id;
					}
				} else {
					String annot = cursor.getString(0);
					if(annot!=null) {
						String notes = cursor.getString(2);
						if(sb==null) sb = new StringBuilder();
						// todo check input and enhance safty
						sb.append(cursor.getString(1)).append("\0"); // {}
						sb.append(id).append("\0"); // note id
						sb.append(notes==null?"":notes).append("\0"); // notes
					}
				}
			}
			cursor.close();
			//CMN.debug("remark=", sb);
			return sb;
		}
		
        @JavascriptInterface
        public String remarkByUrl(int sid, String url, long position) { // 网页版会调用，
			CMN.debug("annot:::remarkByUrl::", url);
			StringBuilder sb = null;
			if (presenter!=null) {
				WebViewmy mWebView = findWebview(sid);
				if (mWebView != null) {
					long bid = presenter.getId();
					int schemaIdx = url.indexOf(":");
					if (url.regionMatches(schemaIdx + 12, "base", 0, 4)) {
						// 包括词条弹出 ://mdbr.com/base/dMWDO3nW/entry/Heracleum
						int slashIdx = url.indexOf("/", schemaIdx+17);
						//bid = IU.TextToNumber_SIXTWO_LE(url.substring(schemaIdx+18, slashIdx));
						sb = getMarksByPos(mWebView, sb, bid, position);
					}
					else if (url.regionMatches(schemaIdx + 12, "content", 0, 7)) {
						int idx = url.indexOf('_', schemaIdx + 20), ed = url.length();
						boolean multi = url.indexOf('_', idx + 1) > 0;
						CharSequenceKey key = new CharSequenceKey(url, idx + 1);
						bid = presenter.a.getMdictServer().getBookIdByURLPath(url, schemaIdx + 20, idx);
						if (multi) {
							idx++;
							do {
								int nxt = url.indexOf('_', idx + 1);
								if (nxt == -1) nxt = ed;
								if (nxt > idx) {
									key.reset(idx, nxt);
									position = IU.TextToNumber_SIXTWO_LE(key);
									int len = sb == null ? 0 : sb.length();
									sb = getMarksByPos(mWebView, sb, bid, position);
									if (sb != null && sb.length() > len) {
										sb.append("\t\n\0"); // \t 作为分隔符，简单
										sb.append(position);
										sb.append("\t\n\0"); // \t 作为分隔符，简单
									}
								}
								idx = nxt + 1;
							} while (idx < ed);
						} else {
							position = IU.TextToNumber_SIXTWO_LE(key);
							sb = getMarksByPos(mWebView, sb, bid, position);
						}
					}
					else if (url.regionMatches(schemaIdx + 12, "view", 0, 4)) {
						// 查看剪贴板
						long vPos = presenter.getBookMarkForWebUrl(url);
						if (vPos!=-1) {
							sb = getMarksByPos(mWebView, sb, bid, vPos);
						}
					}
				}
			}
			if (sb!=null) {
				return sb.toString();
			}
			return "";
		}
		
		@JavascriptInterface
        public String remark(int sid, int position) {
			if(MainActivityUIBase.debugging_annot) CMN.debug("annot:::remark::position=", position);
			if (presenter!=null) {
				WebViewmy mWebView = findWebview(sid);
				if (mWebView != null) {
					if(MainActivityUIBase.debugging_annot) CMN.debug("annot::restore::remark::", position, mWebView.presenter);
					try {
						StringBuilder sb = getMarksByPos(mWebView, null, mWebView.presenter.getId(), position);
						if (sb!=null) {
							return sb.toString();
						}
					} catch (Exception e) { CMN.debug(e); }
				}
			}
			return "";
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
		public void view(int sid, String bidStr, long pos, boolean hasFiles) {
			if (presenter!=null) {
				WebViewmy wv = findWebview(sid);
				if (wv!=null) {
					boolean changed = !presenter.idStr.equals(bidStr) || wv.presenter!=presenter;
					if (changed) {
						long bid = IU.TextToNumber_SIXTWO_LE(new CharSequenceKey(bidStr, 1));
						CMN.debug("view::", presenter, presenter.a.getBookByIdNoCreation(bid).getDictionaryName(), pos, presenter.hasFilesTag());
						CMN.debug("view::", wv.presenter, IU.NumberToText_SIXTWO_LE(wv.presenter.getId(), null)+"=?="+IU.NumberToText_SIXTWO_LE(bid, null));
						CMN.debug("view::", wv.presenter, wv.presenter.getId()+"=?="+bid);
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
						CMN.debug("view::changed!!!", wv.changed, presenter.hasFilesTag(), hasFiles);
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
					wv.word(entryKey);
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
						CMN.Log("dicts::", url, sb.toString());
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
					sb.append("]");
					return sb.toString();
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
				wlh.updateInPageSch(wv, did, keyIdx, keyz, total);
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
		
		/** 页面滚动Y值 */
		@JavascriptInterface
		public void setScrollY(int sid, int offset) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				WebViewListHandler whl = wv.weblistHandler;
				View frame = ViewUtils.getParentByClass(wv, LinearLayout.class);
				//CMN.debug("frame::", frame);
				if (!whl.isViewSingle() && frame!=null) {
					whl.contentUIData.WHP.scrollTo(0, (int) (frame.getTop()
						+ offset* GlobalOptions.density) //todo 校准调优
					);
				}
			}
		}
		
		/** 页面滚动X值 */
		@JavascriptInterface
		public int getScrollX(int sid) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				return (int) (wv.getScrollX() / def_zoom);
			}
			return 0;
		}
		
		/** 页面滚动Y值 */
		@JavascriptInterface
		public float getZoom(int sid) {
			WebViewmy wv = findWebview(sid);
			if(wv!=null) {
				return wv.webScale / def_zoom;
			}
			return 1;
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
			CMN.debug("openImage::", url, book);
			if (book.getImageBrowsable() && PDICMainAppOptions.EnableImageBrowser()) {
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
        public void lockScroll(int sid, boolean lock) {
			if(presenter==null) return;
			WebViewmy wv = findWebview(sid);
			if (wv!=null){
				wv.scrollLocked = lock;
				wv.weblistHandler.pageSlider.scrollLocked = lock;
				if (wv.weblistHandler.pageSlider.getWebContext()!=wv) {
					wv.weblistHandler.pageSlider.setWebview(wv, null);
				}
			}
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
				a.popupWord(key, null, frameAt, wv, wv.weblistHandler.tapDef && wv.weblistHandler.tapSch);
				a.wordPopup.tapped = true; // todo redu
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
				if(wv!=null) {
					return bid.length()==0?PDICMainAppOptions.entryInNewWindowSingle():PDICMainAppOptions.entryInNewWindowMerge();
					//return true;
				}
			}
			return false;
		}
		
		@JavascriptInterface
		public boolean shouldPopupEntry() {
			return PDICMainAppOptions.entryInNewWindowMerge();
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
			presenter.a.ttsHub.ReadText(word, findWebview(sid));
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
			presenter.a.root.post(() -> presenter.a.ttsHub.showTTS());
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
				CMN.debug("knock", view.weblistHandler);
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
		
		// sendup
		@JavascriptInterface
		public void knock0(int sid) {
			//if(layout==a.currentViewImpl)
			{
				//upsended = true;
				WebViewmy view = findWebview(sid);
				//view.lastSuppressLnkTm = CMN.now();
				CMN.debug("knock", view.weblistHandler);
//				view.postDelayed(new Runnable() {
//					@Override
//					public void run() {
						long time = CMN.now();
						float lastX = view.lastX;
						float lastY = view.lastY;
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
		public void knock2(int sid, int x, int y) {
			//upsended = true;
			WebViewmy wv = findWebview(sid);
			View view = (View) findWebview(sid).getParent();
			//view.lastSuppressLnkTm = CMN.now();
//				view.postDelayed(new Runnable() {
//					@Override
//					public void run() {
					long time = CMN.now();
					float lastX = (float) (x*wv.webScale - wv.getScrollX() + Math.random()*10);
					float lastY = (float) (y*wv.webScale - wv.getScrollY() + Math.random()*7);
					lastX+=1;
					lastY+=2;
					CMN.debug("knock2", wv.webScale, x, y, lastX, lastY);
					MotionEvent evt = MotionEvent.obtain(time, time,MotionEvent.ACTION_DOWN, lastX, lastY, 0);
					view.dispatchTouchEvent(evt);
					evt.recycle();
			
			float finalLastX = (float) (lastX+Math.random()*10);
			float finalLastY = (float) (lastY+Math.random()*7);
//				view.postDelayed(new Runnable() {
//					@Override
//					public void run() {
					long time1 = CMN.now();
//					MotionEvent evt1 = MotionEvent.obtain(time, time1-10, MotionEvent.ACTION_DOWN, finalLastX-10, finalLastY, 0);
//
//					evt1.setAction(MotionEvent.ACTION_MOVE);
//					view.dispatchTouchEvent(evt1);
					
					MotionEvent evt2 = MotionEvent.obtain(time, time1, MotionEvent.ACTION_UP, finalLastX, finalLastY, 0);
					view.dispatchTouchEvent(evt2);
					evt2.recycle();
//					}
//				}, (long) (299+Math.random()*99));
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
						CMN.debug("knock1", x, y, lastX, lastY);
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
		//CMN.debug("unload::", this);
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
	
	public void saveStates(Toastable_Activity context, LexicalDBHelper historyCon) {
		if(mType==PLAIN_TYPE_EMPTY) return;
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
			
			readConfigs(context, context.prepareHistoryCon());
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
			tbgColor = opt.getTitlebarBackgroundColor(MainBackground);
			tfgColor = ColorUtils.calculateLuminance(MainBackground)>0.5?Color.BLACK:Color.WHITE;
		}
		if ((firstVersionFlag&0x1)==0)
		{
			tbgColor = opt.getTitlebarBackgroundColor(MainBackground);
			tfgColor = ColorUtils.calculateLuminance(MainBackground)>0.5?Color.BLACK:Color.WHITE;
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
		if (!padSet())
		{
			boolean set = mType==PLAIN_TYPE_TEXT || mType==PLAIN_TYPE_MDICT || mType==PLAIN_TYPE_DSL;
			padLeft(set);
			padRight(set);
			padBottom(set);
			padSet(true);
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
				CMN.debug("已存放::", book_id, name);
				return;
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		((AgentApplication)context.getApplicationContext()).BookProjects.put(name, options);
	}
	
	public int getFontSize() {
		if(mType==PLAIN_TYPE_PDF)
			return 100;
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
		XYTouchRecorder xyt = PDICMainAppOptions.setAsLinkedTextView(tv, false, true);
		//xyt.clickInterceptor = (view1, span) -> { }
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
	
	public long hasBookmark(WebViewmy mWebView, String lex) {
		String entryName = lex==null?getSaveUrl(mWebView):lex;
		if (entryName==null) return -1;
		CMN.debug("hasBookmark::", entryName);
		SQLiteStatement stat;
		try {
			if (mWebView!=null && entryName.startsWith("http") && entryName.indexOf("://")>0) {
				stat = a.prepareHistoryCon().preparedHasWebBookmarkForEntry;
				stat.bindString(1, entryName);
			} else {
				stat = a.prepareHistoryCon().preparedHasBookmarkForEntry;
				stat.bindString(1, entryName);
				stat.bindLong(2, getId());
			}
			return stat.simpleQueryForLong();
		} catch (Exception e) {
			return -1;
		}
	}
	
	public long hasBookmarkAtV2(int pos) {
		String entryName = getSaveUrl(mWebView);
		if (entryName==null) return -1;
		CMN.Log("hasBookmark::", entryName);
		SQLiteStatement stat = a.prepareHistoryCon().preparedGetBookmarkForPos;
		try {
			stat.bindLong(1, getId());
			stat.bindLong(2, pos);
			return stat.simpleQueryForLong();
		} catch (Exception e) {
			return -1;
		}
	}
	
	public long hasBookmarkV2(WebViewmy mWebView) {
		String entryName = getSaveUrl(mWebView);
		if (entryName==null) return -1;
		CMN.Log("hasBookmarkV2::", entryName);
		SQLiteStatement stat = a.prepareHistoryCon().preparedHasBookmarkForUrl;
		stat.bindString(1, entryName);
		stat.bindLong(2, getId());
		try {
			return stat.simpleQueryForLong();
		} catch (Exception e) {
			return -1;
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
	
	// todo 当删除笔记时，需要删除冗余的“书签”记录
	private long getBookMarkForWebUrl(String url) {
		long ret = -1;
		SQLiteDatabase database = a.prepareHistoryCon().getDB();
		String lex = getSaveUrl(mWebView);
		SQLiteStatement stat = a.prepareHistoryCon().preparedHasBookmarkForUrl;
		try {
			stat.bindString(1, lex);
			stat.bindLong(2, getId());
			ret = stat.simpleQueryForLong();
		} catch (Exception e) {
			//CMN.debug(e);
		}
		return ret;
	}
	
	// only for web url
	public long ensureBookMark(WebViewmy mWebView) {
		long ret = -1;
		SQLiteDatabase database = a.prepareHistoryCon().getDB();
		String lex = getSaveUrl(mWebView);
		//String sql = "select id from "+TABLE_BOOK_ANNOT_v2+" where url = ? limit 1";
		//String[] where = new String[]{lex};
		//Cursor c = database.rawQuery(sql, new String[]{lex});
		SQLiteStatement stat = a.prepareHistoryCon().preparedHasBookmarkForUrl;
		try {
			stat.bindString(1, lex);
			stat.bindLong(2, getId());
			ret = stat.simpleQueryForLong();
		} catch (Exception e) {
			//CMN.debug(e);
		}
		if (ret==-1) {
			long pos = -1;
			try {
				stat = a.prepareHistoryCon().predictNextBookmarkId;
				if (stat != null) pos = stat.simpleQueryForLong() + 1;
			} catch (Exception e) {
				//CMN.debug(e);
			}
			long bid = bookImpl.getBooKID();
			ContentValues values = new ContentValues();
			values.put("url", lex);
			values.put("bid", bid);
			values.put("pos", pos);
			values.put("lex", mWebView.title);
			
			values.put("last_edit_time", 0);
			values.put("creation_time", CMN.now());
			ret = database.insert(TABLE_BOOK_ANNOT_v2, null, values);
			CMN.debug("predicted=", ret, pos);
			if (ret != pos) {
				values.put("pos", ret);
				database.update(TABLE_BOOK_ANNOT_v2, values, "id=?", new String[]{""+ret});
			}
		}
		return ret;
	}
	
	public long toggleBookMark(WebViewmy mWebView, OnClickListener notifier, boolean forceAdd) {
		long id=-1;
		boolean say = notifier!=a;
		long bid = bookImpl.getBooKID();
		if (getType()==PLAIN_TYPE_PDF) {
			mWebView.evaluateJavascript("PDFViewerApplication.page", new ValueCallback<String>() {
				@Override
				public void onReceiveValue(String value) {
					int pos = IU.parsint(value, 0);
					//long added = hasBookmarkAtV2(pos);
					long added = hasBookmark(null, ""+pos);
					SQLiteDatabase database = a.prepareHistoryCon().getDB();
					if(forceAdd && added!=-1) {
						if(say) sayToggleBookMarkResult((int) added, notifier);
						return;
					}
					if (added!=-1) {
						int ret = database.delete(TABLE_BOOK_NOTE_v2, "id=?", new String[]{added+""})>0?-2:-1;
						if(say) sayToggleBookMarkResult(ret, notifier);
					} else {
						ContentValues values = new ContentValues();
						values.put("lex", ""+pos);
						values.put("bid", bid);
						values.put("pos", pos);
						values.put("last_edit_time", CMN.now());
						values.put("creation_time", CMN.now());
						added = database.insert(TABLE_BOOK_NOTE_v2, null, values);
						if(say) sayToggleBookMarkResult((int) added, notifier);
					}
				}
			});
			return -2;
		}
		if (mWebView!=null) {
			CMN.rt();
			String lex = getSaveUrl(mWebView);
			if(lex==null) return id;
			CMN.debug("toggleBookMark::url::", lex);
			try {
				SQLiteDatabase database = a.prepareHistoryCon().getDB();
				boolean hasNotes=false;
				boolean insertNew=true;
				Cursor c;
				boolean b1 = lex.startsWith("http") && lex.indexOf("://")>0;
				if (b1) {
					String sql = "select id,notes is not null from "+TABLE_BOOK_NOTE_v2+" where lex = ? limit 1";
					c = database.rawQuery(sql, new String[]{lex});
				} else {
					String[] where = new String[]{lex, ""+bid};
					String sql = "select id,notes is not null from "+TABLE_BOOK_NOTE_v2+" where lex = ? and bid = ? limit 1";
					c = database.rawQuery(sql, where);
				}
				if(c.moveToFirst()) {
					insertNew = false;
					id = c.getLong(0);
					hasNotes = c.getInt(1)==1;
				}
				c.close();
				
				if(insertNew||forceAdd) {
					if (insertNew) {
						ContentValues values = new ContentValues();
						values.put("lex", lex);
						values.put("bid", bid);
						values.put("pos", mWebView.currentPos);
						if (b1) {
							values.put("miaoshu", mWebView.title);
						}
						long now = CMN.now();
						values.put("last_edit_time", now);
						values.put("creation_time", now);
						id = database.insert(TABLE_BOOK_NOTE_v2, null, values);
					}
					if (say && forceAdd) {
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
										int ret = database.delete(TABLE_BOOK_NOTE_v2, "id=?", new String[]{id_}) > 0 ? -2 : -1;
										if(say) sayToggleBookMarkResult(ret, notifier);
									}
								})
								.setNegativeButton("取消", null)
								.show();
					} else if (say) {
						id=database.delete(TABLE_BOOK_NOTE_v2, "id=?", new String[]{id_})>0?-2:-1;
					}
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		if (say) {
			sayToggleBookMarkResult((int) id, notifier);
		}
		return id;
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
	
	// store
	
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
				", int(id)=" + (bookImpl==null?"NaN":getId()) +
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
	
	public void ApplyPadding(StringBuilder sb) {
		if (PDICMainAppOptions.padLeft() && padLeft()) {
			if (CMN.GlobalPagePaddingLeft==null)
				CMN.GlobalPagePaddingLeft = opt.getString("GPL", "3%");
			sb.append("padding-left:").append(CMN.GlobalPagePaddingLeft).append(";");
			//else mWebView.evaluateJavascript("document.body.style.paddingLeft='"+CMN.GlobalPagePaddingLeft+"'", null);
		}
		if (PDICMainAppOptions.padRight() && padRight()) {
			if (CMN.GlobalPagePaddingRight==null)
				CMN.GlobalPagePaddingRight = opt.getString("GPR", "3%");
			sb.append("padding-right:").append(CMN.GlobalPagePaddingRight).append(";");
			//else mWebView.evaluateJavascript("document.body.style.paddingRight='"+CMN.GlobalPagePaddingRight+"'", null);
		}
	}
	
	public void ApplyPadding(WebViewmy mWebView, boolean reset) {
		if (PDICMainAppOptions.padLeft() && padLeft() || reset) {
			if (CMN.GlobalPagePaddingLeft==null)
				CMN.GlobalPagePaddingLeft = opt.getString("GPL", "3%");
			mWebView.evaluateJavascript("document.body.style.paddingLeft='"+(!padLeft()?"":CMN.GlobalPagePaddingLeft)+"'", null);
		}
		if (PDICMainAppOptions.padRight() && padRight() || reset) {
			if (CMN.GlobalPagePaddingRight==null)
				CMN.GlobalPagePaddingRight = opt.getString("GPR", "3%");
			mWebView.evaluateJavascript("document.body.style.paddingRight='"+(!padRight()?"":CMN.GlobalPagePaddingRight)+"'", null);
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
	
	public void checkExtStyle() {
		hasExtStyle = false;
		if(PDICMainAppOptions.getAllowPlugCss() || allowPlugCss()) {
			StringBuilder buffer = getCleanDictionaryNameBuilder();
			File externalFile = new File(f().getParent(), buffer.append(".css").toString());
			if(externalFile.exists()) {
				hasExtStyle = true;
			}
			CMN.debug("插入 同名 css 文件::", hasExtStyle);
		}
	}
	
	public Response getIconResponse() {
		if (cover != null) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			((BitmapDrawable) cover).getBitmap().compress(Bitmap.CompressFormat.PNG, 99, bout);
			return Response.newFixedLengthResponse(Status.OK, "image/*", bout.toByteArray());
		}
		return null;
	}
}

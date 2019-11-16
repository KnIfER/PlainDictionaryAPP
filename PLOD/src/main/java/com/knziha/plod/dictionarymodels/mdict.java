package com.knziha.plod.dictionarymodels;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;

import com.knziha.filepicker.utils.FU;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.MainActivityUIBase.UniCoverClicker;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.slideshow.PhotoViewActivity;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.widgets.WebViewmy;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import db.MdxDBHelper;

/*
 UI side of mdict
 date:2018.07.30
 author:KnIfER
*/

public class mdict extends com.knziha.plod.dictionary.mdict implements ValueCallback<String>, OnClickListener{
	public final static String FileTag = "file://";
	public final static String baseUrl = "file:///";
	public final static String  _404 = "<span style='color:#ff0000;'>Mdict 404 Error:</span> ";
	/**
	 </style>
	 <script>
	 	window.addEventListener('load',wrappedOnLoadFunc,false);
		window.addEventListener('click',wrappedClickFunc);
		function wrappedOnLoadFunc(){
			//console.log('mdpage loaded');
			highlight(null);
		}
	 	function wrappedClickFunc(e){
	 		if(e.srcElement.tagName=='IMG'){
	 			var img=e.srcElement;
				if(img.src && !img.onclick && !(img.parentNode&&img.parentNode.tagName=="A")){
					var lst = [];
					var current=0;
	 				var all = document.getElementsByTagName("img");
					for(var i=0;i<all.length;i++){
	 					lst.push(all[i].src);
	 					if(all[i]==img)
							current=i;
	 				}
	 				if(lst.length==0)
	 					lst.push(img.src);
					app.openImage(current, lst);
				}
	 		}
	 	}

		//!!!高亮开始
		var bOnceHighlighted;
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
			if(bOnceHighlighted && MarkInst && MarkLoad)
			MarkInst.unmark({
				done: function() {
					results=[];
					bOnceHighlighted=false;
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
				loadJs('/MdbR/mark.js', function(){
					MarkLoad=true;
					do_highlight(keyword);
				});
			}else
				do_highlight(keyword);
		}
		var rcsp=0;
		function do_highlight(keyword){
			if(!MarkInst)
				MarkInst = new Mark(document);
	 		bOnceHighlighted=false;
			MarkInst.unmark({
				done: function() {
					keyword=decodeURIComponent(keyword);
	 				//console.log('highlighting...'+keyword);
	 				if(rcsp&0x1)
					MarkInst.markRegExp(new RegExp(keyword, (rcsp&0x2)?'m':'im'), {
						done: done_highlight
					});
					else
					MarkInst.mark(keyword, {
						separateWordSearch: (rcsp&0x4)!=0,'wildcards':(rcsp&0x8)?'enabled':'withSpaces',done: done_highlight,
						caseSensitive:(rcsp&0x2)!=0
					});
				}
			});
		}

		 function done_highlight(){
			 bOnceHighlighted=true;
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
		}
	 </script>
	 */
	@Multiline
	public final static String js="SUBPAGE";

	/**
	 var styleObj= document.styleSheets[0].cssRules[3].style;
	 styleObj.setProperty("border", "1px solid #FF0000");
	 */
	@Multiline
	public final static String border="border";

	public String _Dictionary_fName_Internal;
	public WebViewmy mWebView;
    public ViewGroup rl;
    LayoutInflater inflater;

	public Drawable cover;
	
	public static float def_zoom=1;
	public static int def_fontsize = 100;
	public static int optimal100;
	public int internalScaleLevel=-1;
	public boolean tmpIsFilter;
	byte FFStamp;
	byte firstFlag;
	@Override
	public int getCaseStrategy() {
		return firstFlag&3;
	}
	@Override
	public void setCaseStrategy(int val) {
		firstFlag&=~3;
		firstFlag|=val;
	}

	@Override
	public boolean getIsDedicatedFilter(){
		return (firstFlag & 0x40) == 0x40;
	}
	@Override
	public void setIsDedicatedFilter(boolean val){
		firstFlag&=~0x40;
		if(val) firstFlag|=0x40;
	}
	public boolean getUseInternalBG(){
		return (firstFlag & 0x20) == 0x20;
	}
	public boolean getUseInternalBG(byte firstFlag){
		return (firstFlag & 0x20) == 0x20;
	}
	public void setUseInternalBG(boolean val){
		firstFlag&=~0x20;
		if(val) firstFlag|=0x20;
	}
	public boolean getUseInternalFS(){
		return (firstFlag & 0x10) == 0x10;
	}
	public boolean getUseInternalFS(byte firstFlag){
		return (firstFlag & 0x10) == 0x10;
	}
	public void setUseInternalFS(boolean val){
		firstFlag&=~0x10;
		if(val) firstFlag|=0x10;
	}
	//int WebSingleLayerType=3;//0 None 1 Software 2 Hardware


	@Override
	protected boolean getUseJoniRegex(int id) {
		return id==-1?PDICMainAppOptions.getUseRegex1():PDICMainAppOptions.getUseRegex2();
	}

	@Override
	protected boolean getRegexAutoAddHead() {
		return PDICMainAppOptions.getRegexAutoAddHead();
	}

	public int bmCBI=0,bmCCI=-1;
    public Integer bgColor=null;
    
	private final static String htmlBase="<!DOCTYPE html><html><meta name='viewport' content='initial-scale=1,user-scalable=yes'><head><style>html,body{width:auto;height:auto;}img{max-width:100%;}mark{background:yellow;}mark.current{background:orange;border:0px solid #FF0000}";
	public final static String htmlHeadEndTag = "</head>";
	public final static String htmlEnd="</html>";
	public int htmlBaseLen;
	//TODO lazy load
    public StringBuilder htmlBuilder;

    public boolean bContentEditable=false;
	
    public MainActivityUIBase a;
	PDICMainAppOptions opt;

	//构造
	public mdict(String fn, MainActivityUIBase _a) throws IOException {
		super(fn);
		if(_num_record_blocks==-1) return;
		a = _a;
		inflater = _a.inflater;
		opt = _a.opt;
		fn =f.getAbsolutePath();
		_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
		_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");

		htmlBuilder=new StringBuilder(htmlBase);
        File p = f.getParentFile();
        if(p!=null) {
			/* 外挂同名css */
            StringBuilder sb = new StringBuilder(p.getAbsolutePath()).append("//").append(_Dictionary_fName);
            int fileNameLen=sb.length();
	        String cssPath = sb.append(".css").toString();
	        if(new File(cssPath).exists()) {
	        	BufferedReader in = new BufferedReader(new FileReader(cssPath));
		        String line;
		        while((line = in.readLine())!=null)
					htmlBuilder.append(line).append("\r\n");
		        in.close();
	        }
			/* 同名png图标 */
	        sb.setLength(fileNameLen);
	        pngPath = sb.append(".png").toString();
	        if(new File(pngPath).exists()) {
	        	cover = Drawable.createFromPath(pngPath);
	        }
        }
		htmlBuilder.append(js);
		htmlBaseLen=htmlBuilder.length();

        readInConfigs();
        
    	if(bgColor==null)
    		bgColor=CMN.GlobalPageBackground;
	}


	public String pngPath;
	
	private boolean viewsHolderReady =  false;
	public TextView toolbar_title;
	ViewGroup toolbar;
	public View recess;
	public View forward;
	ImageView toolbar_cover;
	private UniCoverClicker ucc;
	public void initViewsHolder(final MainActivityUIBase a){
		if(!viewsHolderReady) {
			rl=(ViewGroup)inflater.inflate(R.layout.contentview_item,null);
			
	        if(mWebView==null){
	        	webScale = def_zoom;
	           	mWebView = rl.findViewById(R.id.webviewmy);
	            a.initWebScrollChanged();//Strategy: use one webscroll listener
	            mWebView.setOnSrollChangedListener(a.onWebScrollChanged);
	            mWebView.setPadding(0, 0, 18, 0);
				AppHandler mWebBridge = new AppHandler(a);
				mWebView.addJavascriptInterface(mWebBridge, "app");
	            //mWebView.setOnTouchListener(a);
	        }
	        rl.findViewById(R.id.undo).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mWebView.evaluateJavascript("document.execCommand('Undo')", null);
				}});
	        rl.findViewById(R.id.save).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {//document.getElementsByTagName('html')[0].outerHTML;  document.documentElement.innerHTML
					mWebView.evaluateJavascript("document.documentElement.innerHTML", new ValueCallback<String>(){//'<body style=\"\">';
						@Override
						public void onReceiveValue(String v) {
							if(v!=null) {
							   //v=removeUTFCharacters(v.substring(1,v.length()-1));
							   v= StringEscapeUtils.unescapeJava(v.substring(1,v.length()-1));
							   //a.showT(v);
		                       try {
		                    		FileOutputStream outputChannel = new FileOutputStream(new File("/sdcard/123.html"));
		                    	    outputChannel.write(v.getBytes());//StringEscapeUtils.unescapeHtml()
		                    	    outputChannel.flush();
		                    	    outputChannel.close();
		                       } catch (Exception e) {}
		                       
							}
								
						}});
				}});
	        rl.findViewById(R.id.redo).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mWebView.evaluateJavascript("document.execCommand('Redo')", null);
				}});
	        
			toolbar = rl.findViewById(R.id.lltoolbar);
			toolbar_title = toolbar.findViewById(R.id.toolbar_title);
			toolbar_cover = toolbar.findViewById(R.id.cover);
			if(cover!=null)
				toolbar_cover.setImageDrawable(cover);
			ucc = a.getUcc();
			toolbar_cover.setOnClickListener(this);
			toolbar_title.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if(mWebView.getVisibility()!=View.VISIBLE) {
						mWebView.setAlpha(1);
						mWebView.setVisibility(View.VISIBLE); 
					}//((View)rl.getParent()).getId()==R.id.webholder
					else if(rl.getParent()==a.webholder) mWebView.setVisibility(View.GONE);
					else toolbar_cover.performClick();
					
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
			recess.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//CMN.show(""+HistoryVagranter+"asd"+mWebView.canGoBack());mWebView.goBack();
					if(false) {
						if(mWebView.canGoBack()) {
							mWebView.goBack();
						}return;
					}
					if(HistoryVagranter>0) {
						try {							
			    			ScrollerRecord PageState;
							if(!mWebView.isloading && System.currentTimeMillis()-a.lastClickTime>300) {//save our postion
								PageState = HistoryOOP.get(currentPos);
								if(PageState==null)
				    				HistoryOOP.put(currentPos, PageState=new ScrollerRecord());
				    			PageState.x=mWebView.getScrollX();
				    			PageState.y=mWebView.getScrollY();
				    			PageState.scale=mWebView.webScale;
							}
							a.lastClickTime=System.currentTimeMillis();
							//if(!mWebView.isloading)
							//	if(HistoryVagranter>=0) History.get(HistoryVagranter).value=mWebView.getScrollY();
							int pos=-1;
							try {
								pos = Integer.valueOf(History.get(--HistoryVagranter).key);
							} catch (NumberFormatException e) {
								//e.printStackTrace();
							}
							PageState = HistoryOOP.get(pos);
							float initialScale = mdict.def_zoom;
							if(PageState!=null) {
								expectedPos = PageState.y;
								expectedPosX = PageState.x;
								initialScale = PageState.scale;
							}
							//CMN.Log(123,PageState,pos,PageState.scale);
							if(pos!=-1) {
								setCurrentDis(pos, 0);
				    			htmlBuilder.setLength(htmlBaseLen);
				    			mWebView.setInitialScale((int) (100*(initialScale/mdict.def_zoom)*opt.dm.density));//(int) (100*(mWebView.webScale/mdict.def_zoom)*opt.dm.density)
								mWebView.loadDataWithBaseURL(baseUrl,
										 htmlBuilder.append((GlobalOptions.isDark)? MainActivityUIBase.DarkModeIncantation_l:"")
													.append(mdict.htmlHeadEndTag)
										 			.append(getRecordsAt(pos))
													.append(htmlEnd).toString()
										,null, "UTF-8", null);
							}else {
								mWebView.loadUrl(History.get(HistoryVagranter).key);//
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}});
			forward=toolbar.findViewById(R.id.forward);
			forward.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(false) {
						if(mWebView.canGoForward())
							mWebView.goForward();
						return;
					}
					//CMN.show(""+HistoryVagranter);
					if(HistoryVagranter<=History.size()-2) {
						try {
							if(!mWebView.isloading)
								if(HistoryVagranter>=0) History.get(HistoryVagranter).value=mWebView.getScrollY();
							
							ScrollerRecord PageState;
							if(!mWebView.isloading && System.currentTimeMillis()-a.lastClickTime>300) {//save our postion
								PageState = HistoryOOP.get(currentPos);
								if(PageState==null)
				    				HistoryOOP.put(currentPos, PageState=new ScrollerRecord());
				    			PageState.x=mWebView.getScrollX();
				    			PageState.y=mWebView.getScrollY();
				    			PageState.scale=mWebView.webScale;
							}
							a.lastClickTime=System.currentTimeMillis();
							//if(!mWebView.isloading)
							//	if(HistoryVagranter>=0) History.get(HistoryVagranter).value=mWebView.getScrollY();
			    			int pos=-1;
							try {
								pos = Integer.valueOf(History.get(++HistoryVagranter).key);
							} catch (NumberFormatException e) {
								//e.printStackTrace();
							}
							PageState = HistoryOOP.get(pos);
							float initialScale = mdict.def_zoom;
							if(PageState!=null) {
								expectedPos = PageState.y;
								expectedPosX = PageState.x;
								initialScale = PageState.scale;
							}
							//CMN.Log(PageState+" --- "+pos+"  "+PageState.scale);
							//expectedPos = History.get(HistoryVagranter).value;
							//a.showT("expectedPos"+expectedPos);
							if(pos!=-1) {
								setCurrentDis(pos, 0);
				    			htmlBuilder.setLength(htmlBaseLen);
				    			mWebView.setInitialScale((int) (100*(initialScale/mdict.def_zoom)*opt.dm.density));
								mWebView.loadDataWithBaseURL(baseUrl,
										 htmlBuilder.append((GlobalOptions.isDark)? MainActivityUIBase.DarkModeIncantation_l:"")
												 .append(mdict.htmlHeadEndTag)
												 .append(getRecordsAt(pos))
												 .append(htmlEnd).toString()
										,null, "UTF-8", null);
							}else {
								mWebView.loadUrl(History.get(HistoryVagranter).key);//
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}});
			toolbar_title.setText(_Dictionary_fName);
			viewsHolderReady=true;
		}
		recess.setVisibility(View.GONE);
		forward.setVisibility(View.GONE);
	}

	public boolean isJumping = false;

	public int currentPos;
	public int lvPos,lvClickPos,lvPosOff;
	public int expectedPos=-1;
	public int expectedPosX=-1;
	public ArrayList<myCpr<String,Integer>> History = new ArrayList<>();
	
	public int HistoryVagranter=-1;
	public SparseArray<ScrollerRecord> HistoryOOP = new SparseArray<>();//CurrentPageHistorian
	
    @SuppressLint("JavascriptInterface")
	public void setCurrentDis(int idx, int... flag) {
		currentPos = idx;
		currentDisplaying = getEntryAt(currentPos);
    	toolbar_title.setText(new StringBuilder(currentDisplaying.trim()).append(" - ").append(_Dictionary_fName).toString());

		if(flag==null || flag.length==0) {//书签跳转等等
			History.add(++HistoryVagranter,new myCpr<>(String.valueOf(idx),expectedPos));
			for(int i=History.size()-1;i>=HistoryVagranter+1;i--)
				History.remove(i);
		}else {//回溯 或 前瞻， 不改变历史
			//取回
		}
	}

    public void renderContentAt(float initialScale, int SelfIdx, int frameAt, WebViewmy mWebView, int... position){
    	isJumping=false;
    	HistoryVagranter=-1;
    	History.clear();
    	boolean resposibleForThisWeb = false;
    	if(mWebView==null) {
    		mWebView=this.mWebView;
    		resposibleForThisWeb=true;
    	}
    	mWebView.setTag(SelfIdx);
		if(getFontSize()!=mWebView.getSettings().getTextZoom())
			mWebView.getSettings().setTextZoom(getFontSize());

		if(mWebView==this.mWebView) {
	    	setCurrentDis(position[0]);
			if(((View) rl.getParent()).getId()==R.id.webholder) {
				if(rl.getLayoutParams()!=null) {
					rl.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
				}
				if(Build.VERSION.SDK_INT<=19) {
					mWebView.getLayoutParams().height = 100;//on 4.4-Kitkat, height wont shrink down
				}else
					mWebView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
			}
			else {
				if(a.opt.getHideScroll1()&&resposibleForThisWeb || !resposibleForThisWeb&&a.opt.getHideScroll3())
					a.mBar.setVisibility(View.GONE);
				else {
					a.mBar.setDelimiter("< >");
		    		a.mBar.scrollee=mWebView;
				}
			}
		}
		else {
			mWebView.setTag(R.id.position,false);
		}
    	//mWebView.setVisibility(View.VISIBLE);
   	    //a.showT("mWebView:"+mWebView.isHardwareAccelerated());
		PlayWithToolbar(a.hideDictToolbar,a);
    	if(mWebView.wvclient!=a.myWebClient) {
			mWebView.setWebChromeClient(a.myWebCClient);
	   	    mWebView.setWebViewClient(a.myWebClient);
    	}
    	if(getUseInternalBG()) {
    		int myWebColor = bgColor;
    		if(GlobalOptions.isDark)
    			myWebColor=ColorUtils.blendARGB(myWebColor, Color.BLACK, a.ColorMultiplier_Web2);
			mWebView.setBackgroundColor(myWebColor);
    	}else
    		mWebView.setBackgroundColor(Color.TRANSPARENT);
    	mWebView.setBackground(null);
		mWebView.clearHistory();

		String htmlCode;
		try {
	        htmlCode = getRecordsAt(position);
		} catch (Exception e) {
			htmlCode=_404+e.getLocalizedMessage();
		}


		mWebView.isloading=true;

		//CMN.Log("缩放是", initialScale);
		if(initialScale!=-1)
			mWebView.setInitialScale((int) (100*(initialScale/mdict.def_zoom)*opt.dm.density));//opt.dm.density
		else {
			if(false && Build.VERSION.SDK_INT<=23) {
				//mWebView.zoomBy(0.02f);
				mWebView.setTag(R.id.toolbar_action3,true);
			}else
				mWebView.setInitialScale(0);//opt.dm.density
		}

		boolean fromCombined = ((View)mWebView.getParent().getParent()).getId()==R.id.webholder;
		if(!fromCombined) {
			mWebView.setTag(R.id.toolbar_action5, a.hasCurrentPageKey() ? false : null);
		}

		htmlBuilder.setLength(htmlBaseLen);
		if(GlobalOptions.isDark)
			htmlBuilder.append(MainActivityUIBase.DarkModeIncantation_l);

		htmlBuilder.append("<script>");
		htmlBuilder.append("rcsp=").append(MakeRCSP()).append(";");
		htmlBuilder.append("frameAt=").append(frameAt).append(";");
		if(PDICMainAppOptions.getInPageSearchHighlightBorder())
			htmlBuilder.append(border);
		htmlBuilder.append("</script>");
		mWebView.loadDataWithBaseURL(baseUrl,
				htmlBuilder.append(htmlHeadEndTag)
							.append(htmlCode)
							.append(htmlEnd).toString(),null, "UTF-8", null);
	}

	private int MakeRCSP() {
		return opt.FetUseRegex3()|
				opt.FetPageCaseSensitive()<<1|
				opt.FetPageWildcardSplitKeywords()<<2|
				opt.FetPageWildcardMatchNoSpace()<<3
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



	public boolean try_goBackOrForward(int SelfIdx,int target) {
		WebBackForwardList Lst = mWebView.copyBackForwardList();
		int delta=target - currentPos;
		String tStr = String.valueOf(target);
		int ti = Lst.getCurrentIndex()+delta;
		WebHistoryItem TargetItem ;//= Lst.getItemAtIndex(ti);
		//if(TargetItem!=null && tStr.equals(TargetItem.getTitle())){
		//	if(mWebView.canGoBackOrForward(delta)) {
		//		mWebView.goBackOrForward(delta);
		//		setCurrentDis(target);
		//		return true;
		//	}
		//}else 
		for(int i=0;i<Lst.getSize();i++) {
			TargetItem = Lst.getItemAtIndex(i);
			if(Lst.getCurrentIndex()==i) continue;
			if(TargetItem!=null && tStr.equals(TargetItem.getTitle())){
				if(mWebView.canGoBackOrForward(i-Lst.getCurrentIndex())) {
					mWebView.goBackOrForward(i-Lst.getCurrentIndex());
					CMN.Log("goBackOrForward"+(i-Lst.getCurrentIndex())+"<<delta "+TargetItem.getTitle()+"="+tStr);
					setCurrentDis(target);
					return true;
				}
			}
		}
		
		CMN.Log("currentPos"+currentPos+"+"+delta+"="+(currentPos+delta));
		return false;
	}
    
    @Override
    public String getRecordsAt(int... positions) throws IOException {
		return positions[0]==-1? new StringBuilder(getAboutString())
				.append("<BR>").append("<HR>")
				.append(getDictInfo()).toString(): super.getRecordsAt(positions);
    }

    public String getAboutString() {
		//return Build.VERSION.SDK_INT>=24?Html.fromHtml(_header_tag.get("Description"),Html.FROM_HTML_MODE_COMPACT).toString():Html.fromHtml(_header_tag.get("Description")).toString();
		return StringEscapeUtils.unescapeHtml3(_header_tag.get("Description"));
	}
	
	public MdxDBHelper con;
	public MdxDBHelper getCon() {
		if(con==null)
			con = new MdxDBHelper(a,_Dictionary_fName_Internal+"/bookmarks",opt);
		return con;
	}
	public void closeCon() {
		MdxDBHelper con1=con;
		con=null;
		if(con1!=null) {
			con1.close();
		}
	}
   
	private final Handler mHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					int fint = (int) msg.obj;
		        	a.showT("showed to " + fint);
		        	//mWebView.scrollTo(0, fint);
		        	mWebView.setScrollY(fint);
		        	//mWebView.setLayoutParams(mWebView.getLayoutParams());
				break;
				case 2:
					//a.showT("dopagef");
					//for KitKat
					mWebView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
		        	mWebView.setLayoutParams(mWebView.getLayoutParams());
					//mWebView.setAlpha(0.998f);
				break;
			}
		}
	};

	public boolean hasMdd() {
		return mdd!=null && mdd.size()>0;
	}

	public boolean containsResourceKey(String skey) {
		if(mdd!=null)
		for(mdictRes mddTmp:mdd){
			if(mddTmp.lookUp(skey)>=0)
				return true;
		}
		return  false;
	}

	@SuppressWarnings("unused")
    public class AppHandler {
        @JavascriptInterface
        public void dopagef() {
        	if(rl.getParent()!=null)
        	if(Build.VERSION.SDK_INT<=19 && ((View) rl.getParent()).getId()==R.id.webholder) {
	        	mHandle.removeMessages(2);
	        	mHandle.sendEmptyMessage(2);
        	}
        	//a.showT("scrolled"+expectedPos);
        }

        @JavascriptInterface
        public void li(String val) {
        	Log.i("WEBCALL", "li"+val);
        }

        @JavascriptInterface
        public void log(String val) {
        	CMN.Log(val);
        }
        
        private Context context;
        float scale;
        DisplayMetrics dm;
        
        public AppHandler(Context context) {
            this.context = context;
            scale = context.getResources().getDisplayMetrics().density;
    		dm = new DisplayMetrics();
        }

        @JavascriptInterface
        public void openImage(int position, String... img) {
        	CMN.Log(position, img);
            Intent intent = new Intent();
            intent.putExtra("images", img);
			intent.putExtra("current", position);
            PhotoViewActivity.mdd = mdd;
            intent.setClass(context, PhotoViewActivity.class);
            context.startActivity(intent);
        }

        @JavascriptInterface
        public void scrollHighlight(int o, int d) {
        	a.scrollHighlight(o, d);
        }

        @JavascriptInterface
        public String getCurrentPageKey() {
			try {
				return  a.getCurrentPageKey();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}

        @JavascriptInterface 
        public int getDeviceHeight(){
    		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
    		//return (int) (1.0f*dm.heightPixels/ scale + 0.5f);
    		return (int) (dm.heightPixels/scale*1.5);
    		//return (int) (dm.heightPixels);
        }
        @JavascriptInterface 
        public float getDeviceRatio(){
    		DisplayMetrics dm = new DisplayMetrics();
    		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
    		return 1.0f*dm.heightPixels/dm.widthPixels;
        }
        
        @JavascriptInterface
        public void showTo(int val) {
        	if(true) return;
        	//a.showT("showed to " + val);
        	//mWebView
        	Message msg = new Message();
        	msg.what=1;msg.obj=val;
        	mHandle.removeMessages(1);
        	mHandle.sendMessage(msg);
        }
        
        @JavascriptInterface
        public void pageshow() {

        }

        @JavascriptInterface
        public void onAudioPause() {
			opt.isAudioActuallyPlaying=false;
			a.transitAAdjustment();
        }

        @JavascriptInterface
        public void onAudioPlay() {
			opt.isAudioActuallyPlaying=opt.isAudioPlaying=true;
        	a.removeAAdjustment();
        }

        @JavascriptInterface
        public void jumpHighlight(int d) {
        	a.jumpHighlight(d, true);
        }

        @JavascriptInterface
        public void onHighlightReady(int idx, int number) {
        	a.onHighlightReady(idx, number);
        }
    }

	public boolean renameFileTo(File newF) {
		File fP = newF.getParentFile();
		fP.mkdirs();
		boolean ret = false;
		boolean pass = !f.exists();
		String _Dictionary_fName_InternalOld = _Dictionary_fName_Internal;
		if(fP.exists() && fP.isDirectory()) {
			int retret = FU.rename(a, f, newF);
			Log.d("XXX-ret",""+retret);
			//Log.e("XXX-ret",f.getParent()+"sad"+newF.getParent());
			String oldName = _Dictionary_fName;
			if(retret==0) {
				_Dictionary_fName = newF.getName();
		    	int tmpIdx = _Dictionary_fName.lastIndexOf(".");
		    	if(tmpIdx!=-1) {
			    	_Dictionary_fSuffix = _Dictionary_fName.substring(tmpIdx+1);
			    	_Dictionary_fName = _Dictionary_fName.substring(0, tmpIdx);
		    	}
				ret = true;
		    	if(mdd!=null)
				for(mdictRes mddTmp:mdd){
					File mddF = mddTmp.f();
					String fn = BU.unwrapMddName(mddF.getName());
					if(fn.startsWith(_Dictionary_fName)){
						fn=fn.substring(_Dictionary_fName.length());
						File newMdd = new File(fP,_Dictionary_fName+fn+".mdd");
						if(mddF.exists()) {
							int ret1 = FU.rename(a, mddF, newMdd);
							if (ret1 == 0 && mdd != null) {
								mddTmp.updateFile(newMdd);
							}
						}
					}
				}
				else if(new File(fP,_Dictionary_fName+".mdd").exists()) {
					try {
						mdd = Collections.singletonList(new mdictRes(new File(fP, _Dictionary_fName + ".mdd").getAbsolutePath()));
						a.showT("找到了匹配的mdd！");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else if(retret==-123) {
				a.showT("错误：不恰当的路径分隔符");
			}
		}
		if(ret || pass) {
			f=newF;
			String fn = newF.getAbsolutePath();
			_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
			_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");
		}
		new File(opt.pathTo().append(_Dictionary_fName_InternalOld).toString()).renameTo(new File(opt.pathTo().append(_Dictionary_fName_Internal).toString()));
		
		if(a.currentDictionary==this)
			opt.putLastMd(_Dictionary_fName);
		return ret;
	}
	
	public boolean moveFileTo(File newF) {
		File fP = newF.getParentFile();
		fP.mkdirs();
		boolean ret = false;
		boolean pass = !f.exists();
		String _Dictionary_fName_InternalOld = _Dictionary_fName_Internal;
		if(fP.exists() && fP.isDirectory()) {
			int retret = FU.move(a, f, newF);
			Log.d("XXX-ret",""+retret);
			//Log.e("XXX-ret",f.getParent()+"sad"+newF.getParent());
			if(retret==0) {
				_Dictionary_fName = newF.getName();
		    	int tmpIdx = _Dictionary_fName.lastIndexOf(".");
		    	if(tmpIdx!=-1) {
			    	_Dictionary_fSuffix = _Dictionary_fName.substring(tmpIdx+1);
			    	_Dictionary_fName = _Dictionary_fName.substring(0, tmpIdx);
		    	}
				ret = true;
				if(mdd!=null)
					for(mdictRes mddTmp:mdd){
						File mddF = mddTmp.f();
						String fn = BU.unwrapMddName(mddF.getName());
						if(fn.startsWith(_Dictionary_fName)){
							fn=fn.substring(_Dictionary_fName.length());
							File newMdd = new File(fP,_Dictionary_fName+fn+".mdd");
							if(mddF.exists()) {
								int ret1 = FU.rename(a, mddF, newMdd);
								if (ret1 == 0 && mdd != null) {
									mddTmp.updateFile(newMdd);
								}
							}
						}
					}
				else if(new File(fP,_Dictionary_fName+".mdd").exists()) {
					try {
						mdd = Collections.singletonList(new mdictRes(new File(fP, _Dictionary_fName + ".mdd").getAbsolutePath()));
						a.showT("找到了匹配的mdd！");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else if(retret==-123) {
				a.showT("错误：不恰当的路径分隔符");
			}
		}
		if(ret || pass) {
			f=newF;
			String fn = newF.getAbsolutePath();
			_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
			_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");
		}
		new File(opt.pathTo().append(_Dictionary_fName_InternalOld).toString()).renameTo(new File(opt.pathTo().append(_Dictionary_fName_Internal).toString()));
		
		if(a.currentDictionary==this)
			opt.putLastMd(_Dictionary_fName);
		return ret;
	}
	
	public boolean renameFile(String string) {
		return renameFileTo(new File(f.getParent(),string+".mdx"));
	}
	

	public void unload() {
		if(mWebView!=null) {
    		mWebView.setWebChromeClient(null);
    		mWebView.setWebViewClient(null);
    		mWebView.setOnSrollChangedListener(null);
    		mWebView.setOnTouchListener(null);
    		mWebView.removeAllViews();
    		mWebView.destroy();
    		mWebView=null;
		}
		if(viewsHolderReady) {
			ucc=null;
			toolbar_cover.setOnClickListener(null);
		}
		a=null;
	}



	public void dumpViewStates() {
		try {
			long time = System.currentTimeMillis();
	    	File SpecificationFile = new File(opt.pathTo().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
	    	if(!SpecificationFile.getParentFile().exists())
	    		SpecificationFile.getParentFile().mkdirs();
	    	DataOutputStream fo = new DataOutputStream(new FileOutputStream(SpecificationFile));
			fo.writeShort(12);
			fo.writeByte(firstFlag);
			
			fo.writeInt(bgColor);
			fo.writeInt(internalScaleLevel);
			
			fo.writeInt(lvPos);
			fo.writeInt(lvClickPos);
			fo.writeInt(lvPosOff);
			//CMN.Log("保存列表位置",lvPos,lvClickPos,lvPosOff);
			
			if(viewsHolderReady && mWebView!=null) {
				expectedPosX=mWebView.getScrollX();
				expectedPos=mWebView.getScrollY();
			}
			fo.writeInt(expectedPosX);
			fo.writeInt(expectedPos);
			fo.writeFloat(webScale);
			//CMN.Log(_Dictionary_fName+"保存页面位置",expectedPosX,expectedPos,webScale);
			
			fo.flush();
			fo.close();
			//CMN.Log(_Dictionary_fName+"单典配置保存耗时",System.currentTimeMillis()-time);
		} catch (Exception e) { e.printStackTrace(); }
    	
	}
	
	
	protected void WriteConfigFF() {
		//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
		 try {
	        	File SpecificationFile = new File(opt.pathTo().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
	        	if(!SpecificationFile.getParentFile().exists())
		    		SpecificationFile.getParentFile().mkdirs();
	        	RandomAccessFile outputter = new RandomAccessFile(SpecificationFile, "rw");
	        	outputter.writeShort(12);
	        	outputter.writeByte(FFStamp=firstFlag);
	        	outputter.close();
	        } catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public int getFontSize() {
		if(getUseInternalFS())
			return internalScaleLevel>0?internalScaleLevel:(internalScaleLevel=def_fontsize);
		return def_fontsize;
	}
	
	public final int[] CONFIGPOS= {3,7};
	public float webScale=0;
	public void WriteConfigInt(int off, int val) {
		//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
		 try {
	        	File SpecificationFile = new File(opt.pathTo().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
	        	if(!SpecificationFile.getParentFile().exists())
		    		SpecificationFile.getParentFile().mkdirs();
	        	RandomAccessFile outputter = new RandomAccessFile(SpecificationFile, "rw");
	        	outputter.seek(off);
	        	outputter.writeInt(val);
	        	outputter.close();
	        } catch (IOException e) {
				e.printStackTrace();
			}
	}

	protected void readInConfigs() throws IOException {
		DataInputStream data_in1 = null;
		try {
			File SpecificationFile = new File(opt.pathTo().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
			if(SpecificationFile.exists()) {
				long time=System.currentTimeMillis();
				//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
				data_in1 = new DataInputStream(new FileInputStream(SpecificationFile));
				int size = data_in1.readShort();
				if(size!=12) {
					data_in1.close();
					SpecificationFile.delete();
					return;
				}
				FFStamp = firstFlag = data_in1.readByte();
				//readinoptions
				//a.showT("getUseInternalBG():"+getUseInternalBG()+" getUseInternalFS():"+getUseInternalFS()+" KeycaseStrategy:"+KeycaseStrategy);
				if(data_in1.available()>4) {
					bgColor = data_in1.readInt();
					internalScaleLevel = data_in1.readInt();


					lvPos = data_in1.readInt();
					lvClickPos = data_in1.readInt();
					lvPosOff = data_in1.readInt();

					//CMN.Log(_Dictionary_fName+"列表位置",lvPos,lvClickPos,lvPosOff);

					expectedPosX = data_in1.readInt();
					expectedPos = data_in1.readInt();
					webScale = data_in1.readFloat();
				}

				//CMN.Log(_Dictionary_fName+"页面位置",expectedPosX,expectedPos,webScale);


				data_in1.close();
				//CMN.Log(_Dictionary_fName+"单典配置加载耗时",System.currentTimeMillis()-time);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(data_in1!=null) data_in1.close();
		}
	}

	protected int ReadConfigInt(int off) {
		//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
		int ret=0;
		try {
        	File SpecificationFile = new File(opt.pathTo().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
        	if(!SpecificationFile.getParentFile().exists())
	    		SpecificationFile.getParentFile().mkdirs();
        	RandomAccessFile outputter = new RandomAccessFile(SpecificationFile, "r");
        	outputter.seek(off);
        	ret = outputter.readInt();
        	outputter.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
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
	

	public static void showDictTweaker(Activity context, mdict...md) {
		if(md.length==0) return;
		mdict mdTmp = md[0];
		String[] DictOpt = context.getResources().getStringArray(R.array.dict_spec);
		final String[] Coef = DictOpt[0].split("_");
		final View dv = LayoutInflater.from(context).inflate(R.layout.dialog_about,null);
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = dv.findViewById(R.id.title);
		title.setText(R.string.dictOpt1);//词典设定
		title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
		
		if(mdTmp.opt.isLarge) tv.setTextSize(tv.getTextSize());
		tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0x1, 5, 1,md);//0x20
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 2, Coef, 0, 0x1, 4, 1,md);//0x10
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 3, new String[]{Coef[0], Coef[2], Coef[3]}, 0, 0x3, 0, 2,md);
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 4, Coef, 4, 0x1, 6, 1,md);//0x40

		ssb.delete(ssb.length()-4,ssb.length());
		tv.setText(ssb);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		AlertDialog.Builder builder2 = new AlertDialog.Builder(context,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);
		builder2.setView(dv);
		final AlertDialog d = builder2.create();
		d.setCanceledOnTouchOutside(true);

		d.setOnDismissListener(dialog -> {
			for (int i = 0; i < md.length; i++) {
				mdict mI = md[i];
				if(mI.firstFlag != mI.FFStamp) {
					mI.WriteConfigFF();
				}
			}
		});
		dv.findViewById(R.id.cancel).setOnClickListener(v -> d.dismiss());
        d.getWindow().setBackgroundDrawableResource(GlobalOptions.isDark?R.drawable.popup_shadow_d:R.drawable.popup_shadow_l);
		//d.getWindow().setDimAmount(0);
    	//d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		d.show();
		android.view.WindowManager.LayoutParams lp = d.getWindow().getAttributes();  //获取对话框当前的参数值
		lp.height = -2;
		d.getWindow().setAttributes(lp);
	}

	private static void init_clickspan_with_bits_at(TextView tv, SpannableStringBuilder text,
		 String[] dictOpt, int titleOff, String[] coef, int coefOff, int mask, int flagPosition, int flagMax
			, mdict...md) {
		mdict mdTmp = md[0];
		boolean isSingle=true;
		int val = (mdTmp.firstFlag>>flagPosition)&mask;
		for (int i = 1; i < md.length; i++) {
			if(((md[i].firstFlag>>flagPosition)&mask)!=val){
				isSingle=false;
				break;
			}
		}
		int start = text.length();
		int now = start+dictOpt[titleOff].length();
		text.append("[").append(dictOpt[titleOff]).append(isSingle?coef[coefOff+val]:tv.getContext().getString(R.string.multiple_vals)).append("]");
		text.setSpan(new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				byte FFStamp = mdTmp.firstFlag;
				int val = (mdTmp.firstFlag>>flagPosition)&mask;
				val=(val+1)%(flagMax+1);
				for (com.knziha.plod.dictionarymodels.mdict mdict : md) {
					mdict.firstFlag &= ~(mask << flagPosition);
					mdict.firstFlag |= (val << flagPosition);
					mdict.validifyStates(FFStamp);
				}
				int fixedRange = indexOf(text, ':', now);
				text.delete(fixedRange+1, indexOf(text, ']', fixedRange));
				text.insert(fixedRange+1,coef[coefOff+val]);
				tv.setText(text);
			}},start,text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.append("\r\n").append("\r\n");
	}

	private void validifyStates(byte ffStamp) {
		if(ffStamp!=firstFlag){
			boolean bool;
			if(getUseInternalBG(ffStamp)!=(bool=getUseInternalBG())){
				if(bool) {
					bgColor=ReadConfigInt(CONFIGPOS[0]);
					mWebView.setBackgroundColor(bgColor);
				}else
					mWebView.setBackgroundColor(a.GlobalPageBackground);
			}
			if(getUseInternalFS(ffStamp)!=(bool=getUseInternalFS())){
				if(bool)  internalScaleLevel=ReadConfigInt(CONFIGPOS[1]);
				mWebView.getSettings().setTextZoom(getFontSize());
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
				if(v.getTag(R.id.toolbar_action1)!=null) {//add It!
					boolean resposible=con==null;
					if(getCon().insertUpdate(currentPos)!=-1)
						v.setTag(R.id.toolbar_action2,CMN.OccupyTag);
					if(resposible) closeCon();
					v.setTag(R.id.toolbar_action1,null);
					break;
				}
				if(ucc!=null) {//sanity check.
					ucc.setInvoker(this);
					ucc.onClick(v);
				}
			break;
		}
	}

	@Override
	protected ExecutorService OpenThreadPool(int thread_number) {
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		//	return Executors.newWorkStealingPool();
		//}
		return Executors.newFixedThreadPool(thread_number);
	}
}

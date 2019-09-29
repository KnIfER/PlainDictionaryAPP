package com.knziha.plod.dictionarymodels;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.MainActivityUIBase;
import com.knziha.plod.PlainDict.PhotoBrowserActivity;
import com.knziha.plod.PlainDict.R;
import com.knziha.filepicker.utils.FU;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.dictionary.myCpr;
import com.knziha.plod.PlainDict.MainActivityUIBase.UniCoverClicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import db.MdxDBHelper;

/*
 ui side of mdict
 date:2018.07.30
 author:KnIfER
*/


public class mdict extends com.knziha.plod.dictionary.mdict implements ValueCallback<String>, OnClickListener{
	public static String FileTag = "file://";
	public final String baseUrl = "file:///";
	public mdict(){};
	public String _Dictionary_fName_Internal;
	public WebViewmy mWebView;
    public ViewGroup rl;
    //RelativeLayout.LayoutParams lp;
    float zoomLevel=1;
    LayoutInflater inflater;

    //boolean isWebHidden=false;
	boolean isZooming=false;
	public float flingDelta = 100;
	float svx1 = 0,svy1=0,svx2=0,svy2=0;
	float svFlingvelocityX;
	public Drawable cover;
	//static boolean isWebHeld;
	
	public static float def_zoom=1;
	public static int def_fontsize = 100;
	public static int optimal100;
	public int internalScaleLevel=-1;
	public boolean bUseInternalBG;
	public boolean bUseInternalFS;
	int WebSingleLayerType=3;//0 None 1 Software 2 Hardware

    public int bmCBI=0,bmCCI=-1,CBI,CCI;
    public Integer bgColor=null;
    
	public String js="";
	public String htmlHeader="";
	public String htmlTailer="";
    public StringBuilder htmlBuilder;

    public boolean bContentEditable=false;
	
    public MainActivityUIBase a;
	PDICMainAppOptions opt;

	int SelfAtIdx=-1;
	Timer timer;
	public final static String htmlTitleEndTag="</title>";;
	public final static String htmlHeader2 = "</head> <body onpageshow=\"window.imagelistener.pageshow();onpageshown();\" >";
	//构造
	public mdict(String fn, MainActivityUIBase a_) throws IOException {
		a=a_;
		init(fn);
		fn = new File(fn).getAbsolutePath();
		opt=a_.opt;
		_Dictionary_fName_Internal = fn.startsWith(opt.lastMdlibPath)?fn.substring(opt.lastMdlibPath.length()):fn;
		_Dictionary_fName_Internal = _Dictionary_fName_Internal.replace("/", ".");
		inflater=a_.inflater;
		if(KeycaseStrategy==1) {
			
		}
		else {//TODO
			
		}
		js = "<script type=\"text/javascript\">var objs = document.getElementsByTagName(\"img\");for(var i=0;i<objs.length;i++){var img=objs[i];img.style.maxWidth = '100%';img.style.height='auto';if(img.onclick==null && img.parentNode.onclick==null)img.onclick=function(){window.imagelistener.openImage(this.src);}}</script>";
		//objs = document.getElementsByTagName(\"video\");for(var i=0;i<objs.length;i++){var v=objs[i];v.play();}
		//js="";
		htmlHeader = "<meta name=\"viewport\" content=\"initial-scale=1, user-scalable=yes,\">  <style>html,body{ width: auto; height: auto;} img{max-width:100%;}</style> <head><title>";
		htmlTailer="<div class='bd_body'/></body>";
		
        File p = f.getParentFile();
        if(p!=null) {
            StringBuilder sb = new StringBuilder();//外挂css
	        String cssPath = sb.append(p.getAbsolutePath()).append("//").append(_Dictionary_fName).append(".css").toString();
	        if(new File(cssPath).exists()) {
	        	sb.setLength(0);
	        	sb.append("<meta name=\"viewport\" content=\"initial-scale=1, user-scalable=yes,\">  <style>html,body{ width: auto; height: auto;} img{max-width:100%;}");
	        	BufferedReader in = new BufferedReader(new FileReader(cssPath));
		        String line = in.readLine();
		        while(line!=null){
		        	sb.append(line = in.readLine());
		        }
		        in.close();
		        htmlHeader=sb.append("</style><head><title>").toString();
		        //htmlTailer = "<div class='bd_body'/></body>";
	        }
	        sb.setLength(0);
	        pngPath = sb.append(p.getAbsolutePath()).append("//").append(_Dictionary_fName).append(".png").toString();
	        if(new File(pngPath).exists()) {
	        	cover = Drawable.createFromPath(pngPath);
	        }
        }

        htmlBuilder = new StringBuilder(htmlHeader);
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
        	byte firstFlag = data_in1.readByte();
        	bUseInternalBG = (firstFlag & 32) == 32;
        	bUseInternalFS = (firstFlag & 16) == 16;
    		KeycaseStrategy = firstFlag & 3;
    		WebSingleLayerType = (firstFlag & 12) >> 2;
    		//readinoptions
    		//a.showT("bUseInternalBG:"+bUseInternalBG+" bUseInternalFS:"+bUseInternalFS+" KeycaseStrategy:"+KeycaseStrategy);
			bgColor=data_in1.readInt();
			internalScaleLevel=data_in1.readInt();

			
			lvPos = data_in1.readInt();
			lvClickPos = data_in1.readInt();
			lvPosOff = data_in1.readInt();
			
			CMN.Log(_Dictionary_fName+"列表位置",lvPos,lvClickPos,lvPosOff);
			
			expectedPosX = data_in1.readInt();
			expectedPos = data_in1.readInt();
			webScale = data_in1.readFloat();
			
			CMN.Log(_Dictionary_fName+"页面位置",expectedPosX,expectedPos,webScale);
			

    		data_in1.close();
			CMN.Log(_Dictionary_fName+"单典配置加载耗时",System.currentTimeMillis()-time);
        }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally{
        	if(data_in1!=null) data_in1.close();
        }
        
    	if(bgColor==null) bgColor=CMN.GlobalPageBackground;
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
	           	//mWebView = new WebViewmy(a);
	        	webScale = def_zoom;
	           	mWebView = rl.findViewById(R.id.webviewmy);
	           	//mWebView.setTag(this);
	            a.initWebScrollChanged();//Strategy: use one webscroll listener
	            mWebView.setOnSrollChangedListener(a.onWebScrollChanged);
	            
	            mWebView.setPadding(0, 0, 18, 0);

	           	//mWebView.setOnTouchListener(a.mBar);
	            mWebView.setOnTouchListener(a);
	           	
	            //lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
	            //lp.addRule(RelativeLayout.BELOW, R.id.toolbar);
	            //lp.setMargins(0, 0, 18, 0);
	            /////lp.setMargins(15, 20, 10, 15);
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
							   v=StringEscapeUtils.unescapeJavaScript(v.substring(1,v.length()-1));
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
	        
			toolbar = (ViewGroup)rl.findViewById(R.id.toolbar);
			toolbar_title = ((TextView)toolbar.findViewById(R.id.toolbar_title));
			toolbar_cover = (ImageView)toolbar.findViewById(R.id.cover);
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
							a.mBar.isWebHeld=true;
							if(a.mBar.timer!=null) a.mBar.timer.cancel();
							a.mBar.fadeIn();
							a.mBar.setMax(a.webholder.getMeasuredHeight()-((View) a.webholder.getParent()).getMeasuredHeight());
							a.mBar.setProgress(((View) a.webholder.getParent()).getScrollY());
							a.mBar.onTouch(null, MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,0,0,0));
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
				    			htmlBuilder.setLength(htmlHeader.length());
				    			mWebView.setInitialScale((int) (100*(initialScale/mdict.def_zoom)*opt.dm.density));//(int) (100*(mWebView.webScale/mdict.def_zoom)*opt.dm.density)
								mWebView.loadDataWithBaseURL(baseUrl,
										 htmlBuilder.append(mdict.htmlTitleEndTag).append((GlobalOptions.isDark)? MainActivityUIBase.DarkModeIncantation_l:"").append(mdict.htmlHeader2)
										 			.append(getRecordsAt(pos))
													.append(js)
													.append(mdd!=null?"<div class='MddExist'/>":"")
													.append(htmlTailer).toString()
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
				    			htmlBuilder.setLength(htmlHeader.length());
				    			mWebView.setInitialScale((int) (100*(initialScale/mdict.def_zoom)*opt.dm.density));
								mWebView.loadDataWithBaseURL(baseUrl,
										 htmlBuilder.append(mdict.htmlTitleEndTag).append((GlobalOptions.isDark)? MainActivityUIBase.DarkModeIncantation_l:"").append(mdict.htmlHeader2)
										 			.append(getRecordsAt(pos))
													.append(js)
													.append(mdd!=null?"<div class='MddExist'/>":"")
													.append(htmlTailer).toString()
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
	
	long baseTime;
	public boolean isJumping = false;
	public void clearWebview(){
		if(mWebView!=null){
			//CMN.show("asd");
			mWebView.findAllAsync(null);
			//mWebView.setLayoutParams(mWebView.getLayoutParams());
			//mWebView.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
			//mWebView.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 1, 0));
			//mWebView.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 2, 0));
			//mWebView.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 3, 0));
			//mWebView.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 4, 0));
			//mWebView.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 5, 0));
			//mWebView.getSettings().setLoadWithOverviewMode(false); //very hacky,very android... solution found on the SO.cheers!
			//mWebView.loadUrl("about:blank");
			//mWebView.clearCache(false);
			//mWebView.loadDataWithBaseURL(baseUrl,"\n\n\n\n\n\n\n\n",null, "UTF-8", null);
		}
	}
	//public void refreshWebview(){
		//if(mWebView!=null) {
			//mWebView.getSettings().setLoadWithOverviewMode(true);//very hacky,very android... solution found on the SO.cheers!
			//mWebView.loadUrl("about:blank");
		//}
		//mWebView.setInitialScale(0);
		//mWebView.df.reset();
		//mWebView.pageUp(true);
	//}
	



	public int currentPos;
	public int lvPos,lvClickPos,lvPosOff;
	public int expectedPos=-1;
	public int expectedPosX=-1;
	public ArrayList<myCpr<String,Integer>> History = new ArrayList<myCpr<String,Integer>>();
	
	HashMap<Integer,MJavascriptInterface> ImageHistory = new HashMap<>();
	public int HistoryVagranter=-1;
	public SparseArray<ScrollerRecord> HistoryOOP = new SparseArray<>();//CurrentPageHistorian
	
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
        	MJavascriptInterface js = ImageHistory.get(HistoryVagranter);
        	if(js!=null)
        	mWebView.removeJavascriptInterface("imagelistener");
		    mWebView.addJavascriptInterface(js, "imagelistener");
		}
	}
    
    Pattern htmlPattern;
	private float ScaleMultiplier;
    
    public void renderContentAt(float initialScale,int SelfIdx, WebViewmy mWebView,int ...position){
		//a.showT("bUseInternalFS:"+bUseInternalFS+"-"+internalScaleLevel+"\r\n"+"bUseInternalBG:"+bUseInternalBG+"-"+Integer.toHexString(bgColor));	
    	isJumping=false;
    	HistoryVagranter=-1;
    	History.clear();
    	ImageHistory.clear();

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
			}else {
				if(a.opt.getHideScroll1()&&resposibleForThisWeb || !resposibleForThisWeb&&a.opt.getHideScroll3())
					a.mBar.setVisibility(View.GONE);
				else {
					a.mBar.setDelimiter("< >");
		    		a.mBar.scrollee=mWebView;
				}
	    		//mWebView.getSettings().setSupportZoom(true);
			}
		}else {
			mWebView.setTag(R.id.position,false);
		}
    	//mWebView.setVisibility(View.VISIBLE);
   	    //a.showT("mWebView:"+mWebView.isHardwareAccelerated());
		PlayWithToolbar(a.hideDictToolbar,a);
		
    	if(mWebView.wvclient!=a.myWebClient) {
			mWebView.setWebChromeClient(a.myWebCClient);
	   	    mWebView.setWebViewClient(a.myWebClient);
    	}
    	
    	if(bUseInternalBG) {
    		int myWebColor = bgColor;
    		if(GlobalOptions.isDark)
    			myWebColor=ColorUtils.blendARGB(myWebColor, Color.BLACK, a.ColorMultiplier_Web2);
			mWebView.setBackgroundColor(myWebColor);
    	}else
    		mWebView.setBackgroundColor(Color.TRANSPARENT);
    	
    	mWebView.setBackground(null);
    	
    	//CMN.Log(mWebView.getBackground());


		mWebView.clearHistory();
    	//mWebView.clearMatches();
    	//mWebView.findAllAsync(null);

    	
		String htmlCode=null;
		String _404 = "<span style='color:#ff0000;'>Mdict 404 Error:</span> ";
		try {
	        htmlCode = getRecordsAt(position);
		} catch (Exception e) {_404+=e.getLocalizedMessage();}
		
		if(htmlCode==null) htmlCode=_404;
		
	        
        //if(htmlCode.indexOf("<body>")!=-1)//剥离 body
        //	htmlCode = htmlCode.substring(htmlCode.indexOf("<body>")+6,htmlCode.indexOf("</body>"));
        /*if(lastHeight!=-1) {
        	int estimatedH = (int) (lastHeight * (htmlCode.length()*1.f/lastLength));
        	if(estimatedH<lastLength) {
	        	//mWebView.getLayoutParams().height = estimatedH;
	        	//a.showT("lastHeight"+htmlCode.length());
	        	//mWebView.setLayoutParams(mWebView.getLayoutParams());
        	}
		}
        lastLength=htmlCode.length();
        */
		
	    final String HeaderTag = "<head>";
	    if(false && htmlCode.indexOf(HeaderTag)!=-1){
	    	//showToast("!");
			mWebView.loadDataWithBaseURL(baseUrl,htmlCode,null, "UTF-8", null);
	    }else {
	    	htmlBuilder.setLength(htmlHeader.length());
	    	mWebView.isloading=true;
	    	//mWebView.loadUrl("file:///sdcard/fond.mhtml");
	    	if(false)mWebView.loadUrl("file:///sdcard/123.html");
	    	CMN.Log("缩放是", initialScale);
	    	if(initialScale!=-1)
	    		mWebView.setInitialScale((int) (100*(initialScale/mdict.def_zoom)*opt.dm.density));//opt.dm.density
	    	else {
	    		if(false && Build.VERSION.SDK_INT<=23) {
	    			//mWebView.zoomBy(0.02f);
	    			mWebView.setTag(R.id.toolbar_action3,true);
	    		}else
	    			mWebView.setInitialScale(0);//opt.dm.density
	    	}

	    	//(int) (100 *opt.dm.density)+1
	    	
	    	//CMN.Log(initialScale+" :"+(int) (100 *opt.dm.density));

    		//mWebView.setInitialScale((int) (100*(2)));//opt.dm.density
    		
			mWebView.loadDataWithBaseURL(baseUrl,//.append(mWebView==this.mWebView?"":(SelfIdx+":"))    .append(position[0])
					htmlBuilder.append(htmlTitleEndTag).append(GlobalOptions.isDark? MainActivityUIBase.DarkModeIncantation_l:"").append(htmlHeader2)
								.append(htmlCode)
								.append(js)
								.append(mdd!=null?"<div class='MddExist'/>":"")
								.append(htmlTailer).toString(),null, "UTF-8", null);
	    }
		//mWebView.loadDataWithBaseURL(null,"<body>"+htmlCode+"</body>",null, "UTF-8", null);
	    //////a.mBar.fadeOut();
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
    	String ret;
		if(positions[0]==-1) {
			 //渲染封皮...
			//Log.e("xdda",Build.VERSION.SDK_INT>=21?Html.fromHtml(_header_tag.get("Description"),Html.FROM_HTML_MODE_COMPACT).toString():Html.fromHtml(_header_tag.get("Description")).toString());
			String cover = getAboutString();
			
			ret = new StringBuilder()
				.append(cover)
				.append("<BR>").append("<HR>")
				.append(getDictInfo()).toString();
			//return ret;
		}else
			ret = super.getRecordsAt(positions);
		//扫入图片
		if(htmlPattern==null)
    		htmlPattern = Pattern.compile("<img\\b[^>]*\\bsrc\\b\\s*=\\s*('|\")?([^'\"\n\r\f>]+(\\.jpg|\\.bmp|\\.eps|\\.gif|\\.mif|\\.miff|\\.png|\\.tif|\\.tiff|\\.svg|\\.wmf|\\.jpe|\\.jpeg|\\.dib|\\.ico|\\.tga|\\.cut|\\.pic|\\b)\\b)[^>]*>", Pattern.CASE_INSENSITIVE);
        List<String> imageSrcList = new ArrayList<String>();
        Matcher m = htmlPattern.matcher(ret);
        String quote = null;
        String src = null;
        while (m.find()) {
            quote = m.group(1);
            src = (quote == null || quote.trim().length() == 0) ? m.group(2).split("\\s+")[0] : m.group(2);
            imageSrcList.add(src);
        }
        if(mWebView!=null) {
	        //if (imageSrcList.size() >= 0) {
	        	MJavascriptInterface js = new MJavascriptInterface(a,imageSrcList.toArray(new String[] {}));
	        	mWebView.removeJavascriptInterface("imagelistener");
			    mWebView.addJavascriptInterface(js, "imagelistener");
			    ImageHistory.put(HistoryVagranter, js);
	        //}//else
	        	//mWebView.removeJavascriptInterface("imagelistener");
        }
        
	    return ret;
    }
    

    public String getAboutString() {
		//return Build.VERSION.SDK_INT>=24?Html.fromHtml(_header_tag.get("Description"),Html.FROM_HTML_MODE_COMPACT).toString():Html.fromHtml(_header_tag.get("Description")).toString();
		return StringEscapeUtils.unescapeHtml(_header_tag.get("Description"));
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
	
    public class MJavascriptInterface {
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
        
        private Context context;
        private String [] imageUrls;
        float scale;
        DisplayMetrics dm;
        
        public MJavascriptInterface(Context context,String[] imageUrls) {
            this.context = context;
            this.imageUrls = imageUrls;
            scale = context.getResources().getDisplayMetrics().density;
            //showToast(scale+"scale");
    		dm = new DisplayMetrics();
        }

        @JavascriptInterface
        public void openImage(String img) {
            Intent intent = new Intent();
            intent.putExtra("imageUrls", imageUrls);
            try {
            	//CMN.show(img+"asd"+imageUrls[0]);
				intent.putExtra("curImageUrl", img);
			} catch (Exception e) {
				e.printStackTrace();
			}
            PhotoBrowserActivity.mdd = mdd;
            intent.setClass(context, PhotoBrowserActivity.class);
            context.startActivity(intent);
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
        	CMN.Log("pageshow");
        	final int lalaX=IU.parsint(mWebView.getTag(R.id.toolbar_action1));
			final int lalaY=IU.parsint(mWebView.getTag(R.id.toolbar_action2));
			if(lalaY!=-1 && lalaX!=-1) {
	    		//CMN.Log("initial_push2: ",lalaX,lalaY);
	    		//mWebView.scrollTo(lalaX, lalaY);
			}
        	if(false)
        	mWebView.post(new Runnable() {
				@Override
				public void run() {
		        	//mWebView.evaluateJavascript("document.getElementsByTagName('body')[0].style.scale="+5, null);

					//mWebView.getSettings().setSupportZoom(true);
				}});
        	
        	if(true) return;
			//mWebView.setAlpha(1.0f);
        	//mWebView.setVisibility(View.VISIBLE);
        	//mWebView.isloading=false;
        	//a.showT("showed " + System.currentTimeMillis());

        	//mWebView.setScrollY(expectedPos);
			
        	if(false)
        	mWebView.postDelayed(new Runnable() {
				@Override
				public void run() {
		    		//mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
					mWebView.setAlpha(1.0f);
		        	mWebView.setVisibility(View.VISIBLE);
		        	mWebView.isloading=false;
				}},0);
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
			if(retret==0) {
				_Dictionary_fName = newF.getName();
		    	int tmpIdx = _Dictionary_fName.lastIndexOf(".");
		    	if(tmpIdx!=-1) {
			    	_Dictionary_fSuffix = _Dictionary_fName.substring(tmpIdx+1);
			    	_Dictionary_fName = _Dictionary_fName.substring(0, tmpIdx);
		    	}
				ret = true;
				File mddF = new File(fP,BU.unwrapMdxName(f.getName())+".mdd");
				File newMdd = new File(fP,BU.unwrapMdxName(newF.getName())+".mdd");
				if(mddF.exists()) {
					int ret1 = FU.rename(a, mddF, newMdd);
					if(ret1==0 && mdd!=null) {
						mdd.updateFile(newMdd);
					}
				}
				if(mdd==null && newMdd.exists()) {
					try {
						mdd = new mdictRes(newMdd.getAbsolutePath());
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
				File mddF = new File(f.getParentFile(),BU.unwrapMdxName(f.getName())+".mdd");
				File newMdd = new File(fP,BU.unwrapMdxName(newF.getName())+".mdd");
				if(mddF.exists()) {
					int ret1 = FU.move(a, mddF, newMdd);
					if(ret1==0 && mdd!=null) {
						mdd.updateFile(newMdd);
					}
				}
				if(mdd==null && newMdd.exists()) {
					try {
						mdd = new mdictRes(newMdd.getAbsolutePath());
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
	public File f() {
		return f;
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
	    	byte firstFlag=0;
			firstFlag |= KeycaseStrategy;
			firstFlag |= (WebSingleLayerType << 2);
			if(bUseInternalFS)
				firstFlag |= 16;
			if(bUseInternalBG)
				firstFlag |= 32;
			fo.writeShort(12);
			fo.writeByte(firstFlag);
			
			fo.writeInt(bgColor);
			fo.writeInt(internalScaleLevel);
			
			fo.writeInt(lvPos);
			fo.writeInt(lvClickPos);
			fo.writeInt(lvPosOff);
			CMN.Log("保存列表位置",lvPos,lvClickPos,lvPosOff);
			
			if(viewsHolderReady && mWebView!=null) {
				expectedPosX=mWebView.getScrollX();
				expectedPos=mWebView.getScrollY();
			}
			fo.writeInt(expectedPosX);
			fo.writeInt(expectedPos);
			fo.writeFloat(webScale);
			CMN.Log(_Dictionary_fName+"保存页面位置",expectedPosX,expectedPos,webScale);
			
			fo.flush();
			fo.close();
			CMN.Log(_Dictionary_fName+"单典配置保存耗时",System.currentTimeMillis()-time);
		} catch (Exception e) { e.printStackTrace(); }
    	
	}
	
	
	protected void WriteConfigFF() {
		//FF(len) [|||| |color |zoom ||case]  int.BG int.ZOOM
		 try {
	        	File SpecificationFile = new File(opt.pathTo().append(_Dictionary_fName_Internal).append("/spec.bin").toString());
	        	if(!SpecificationFile.getParentFile().exists())
		    		SpecificationFile.getParentFile().mkdirs();
		        byte firstFlag=0;
				firstFlag |= KeycaseStrategy;
				firstFlag |= (WebSingleLayerType << 2);
				if(bUseInternalFS)
					firstFlag |= 16;
				if(bUseInternalBG)
					firstFlag |= 32;
	        	RandomAccessFile outputter = new RandomAccessFile(SpecificationFile, "rw");
	        	outputter.seek(2);
	        	outputter.writeByte(firstFlag);
	        	outputter.close();
	        } catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public int getFontSize() {
		if(bUseInternalFS)
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
	
	
	
	protected void xxInitializeBinary(File SpecificationFile) throws IOException {
		DataOutputStream do_ = new DataOutputStream(new FileOutputStream(SpecificationFile));
		byte firstFlag=0;
		firstFlag |= KeycaseStrategy;
		firstFlag |= (WebSingleLayerType << 2);
		if(bUseInternalFS)
			firstFlag |= 16;
		if(bUseInternalBG)
			firstFlag |= 32;
		do_.writeShort(11);
		do_.writeByte(firstFlag);
		do_.writeInt(bgColor==null?(bgColor=CMN.GlobalPageBackground):bgColor);
		do_.writeInt(internalScaleLevel==-1?(internalScaleLevel=def_fontsize):internalScaleLevel);
		do_.flush();
		do_.close();
	}
	


	
	public interface ViewLayoutListener{
		void onLayoutDone(int size);
	}public ViewLayoutListener vll;
	
	int lastLength,lastHeight=-1;
	@Override
	public void onReceiveValue(String value) {
		Log.e("fatal_onReceiveValue", value);
		if(value==null) return;
		int val = 0;
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
		//mWebView.invalidate(); 
		
		//mWebView.setTag(R.id.position,lastHeight=val);
		
		if(vll!=null)
			vll.onLayoutDone(val);
	}
	

	boolean isDirty=false;
	public void showDictTweaker(MainActivityUIBase dict_Activity_ui_base) {
		String[] DictOpt = a.getResources().getStringArray(R.array.dict_spec);
		final String[] Coef = DictOpt[0].split("_");
		final View dv = a.inflater.inflate(R.layout.dialog_about,null);
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = ((TextView)dv.findViewById(R.id.title));
		title.setText("词典设定");//"词典设定"
		title.setTextColor(a.AppBlack);
		
		if(opt.isLarge) tv.setTextSize(tv.getTextSize());
		tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		ssb.append("[").append(DictOpt[1]).append(bUseInternalBG?Coef[1]:Coef[0]).append("]");
		ssb.setSpan(new ClickableSpan() {//背景色
			@Override
			public void onClick(View widget) {
				bUseInternalBG=!bUseInternalBG;
				String now = ssb.toString();
				int fixedRange = now.indexOf(":");
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,bUseInternalBG?Coef[1]:Coef[0]);
				tv.setText(ssb);
				isDirty=true;
				//WriteConfigFF();
				if(bUseInternalBG) {
					bgColor=ReadConfigInt(CONFIGPOS[0]);
					mWebView.setBackgroundColor(bgColor);
				}else
					mWebView.setBackgroundColor(a.GlobalPageBackground);
			}},0,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");
		
		final int start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[2]).append(bUseInternalFS?Coef[1]:Coef[0]).append("]");
		ssb.setSpan(new ClickableSpan() {//字体缩放
			@Override
			public void onClick(View widget) {
				bUseInternalFS=!bUseInternalFS;
				String now = ssb.toString();
				int fixedRange = now.indexOf(":", now.indexOf(":")+1);
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1, bUseInternalFS?Coef[1]:Coef[0]);
				tv.setText(ssb);
				isDirty=true;
				//WriteConfigFF();
				if(bUseInternalFS) internalScaleLevel=ReadConfigInt(CONFIGPOS[1]);
				mWebView.getSettings().setTextZoom(getFontSize());
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		final int start2=ssb.toString().length();
		ssb.append("[").append(DictOpt[3]).append((KeycaseStrategy>0)?((KeycaseStrategy==1)?Coef[2]:Coef[3]):Coef[0]).append("]");
		ssb.setSpan(new ClickableSpan() {//大小写转换策略
			@Override
			public void onClick(View widget) {
				KeycaseStrategy+=1;
				KeycaseStrategy%=3;
				String now = ssb.toString();
				int fixedRange = now.indexOf(":", now.indexOf(":",now.indexOf(":")+1)+1);
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1, (KeycaseStrategy>0)?((KeycaseStrategy==1)?Coef[2]:Coef[3]):Coef[0]);
				tv.setText(ssb);
				isDirty=true;
				//WriteConfigFF();
			}},start2,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		tv.setText(ssb);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		AlertDialog.Builder builder2 = new AlertDialog.Builder(a,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);
		builder2.setView(dv);
		final AlertDialog d = builder2.create();
		d.setCanceledOnTouchOutside(true);

		d.setOnDismissListener(dialog -> {
			if(isDirty) {
				//FF(len) [|color |zoom ||case]  int.BG int.ZOOM
				WriteConfigFF();
			}
		});
		dv.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}});
        d.getWindow().setBackgroundDrawableResource(GlobalOptions.isDark?R.drawable.popup_shadow_d:R.drawable.popup_shadow_l);
		//d.getWindow().setDimAmount(0);
    	//d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		d.show();
		android.view.WindowManager.LayoutParams lp = d.getWindow().getAttributes();  //获取对话框当前的参数值
		lp.height = -2;
		d.getWindow().setAttributes(lp);
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
	
	public static String removeUTFCharacters(String data) {
        Pattern p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
        Matcher m = p.matcher(data);
        StringBuffer buf = new StringBuffer(data.length()-2);
        while (m.find()) {
            String ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
            m.appendReplacement(buf, Matcher.quoteReplacement(ch));
        }
        m.appendTail(buf);
        return buf.toString();
    }
}

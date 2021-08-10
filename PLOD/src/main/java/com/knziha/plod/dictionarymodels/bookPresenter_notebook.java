package com.knziha.plod.dictionarymodels;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.knziha.filepicker.utils.FU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymanager.files.CachedDirectory;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.net.ssl.X509TrustManager;

import db.MdxDBHelper;

/*
 Mdict point to online website.
 date:2019.11.28
 author:KnIfER
*/
public class bookPresenter_notebook extends BookPresenter {
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
	public boolean canRerouteUrl;
	public boolean butReadonly;
	public boolean andEagerForParms;
	public boolean noImage;
	public boolean computerFace;
	public boolean forceText;
	//public boolean banJs;
	//public boolean reEnableJs;
	/** 缓存的关键词 */
	ArrayList<String> searchKeys = new ArrayList<>();
	/** Json中的入口点 */
	ArrayList<String> entrance = new ArrayList<>();
	/** 重定向超链接的点击 */
	ArrayList<String> routefrom = new ArrayList<>();
	ArrayList<String> routeto = new ArrayList<>();
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
	
	/**if(!window.PLODKit) {
		 const style = document.createElement("style");
		 style.class = '_PDict';
		 style.innerHTML = "mark{background:yellow;}mark.current{background:orange;}";
		 
		 var script = document.createElement('script');
		 script.type = 'text/javascript';
		 script.src = 'mdbr://SUBPAGE.js';
		 
		 document.head.appendChild(style);
		 document.head.appendChild(script);
		 window.PLODKit=1;
	 }
	 */
	@Multiline
	static final String loadJs = StringUtils.EMPTY;
	
	/**
	 	//if(!window._fvwhl){
			var vi = document.getElementsByTagName('video');
			for(var i=0;i<vi.length;i++){if(!vi[i]._fvwhl){vi[i].addEventListener("webkitfullscreenchange", wrappedFscrFunc, true);vi[i]._fvwhl=1;}}
			function wrappedFscrFunc(e){
				//console.log('begin fullscreen!!!');
				var se = e.srcElement;
				if(se.webkitDisplayingFullscreen&&app) app.onRequestFView(se.videoWidth, se.videoHeight);
	 			return false;
			}
	 		window._fvwhl=1;
	 	//}
	 */
	@Multiline(trim = false)
	public final static String projs="SUB PAGE";
	private JSONObject website;
	private ObjectAnimator progressProceed;
	private ObjectAnimator progressTransient;
	private boolean isListDirty;
	private String jsCode;
	private boolean bNeedSave;
	private static SerializerFeature[] SerializerFormat = new SerializerFeature[]{SerializerFeature.PrettyFormat, SerializerFeature.MapSortField, SerializerFeature.QuoteFieldNames};
	
	//构造
	public bookPresenter_notebook(File fn, MainActivityUIBase _a) throws IOException {
		super(fn, _a, 1, null);
		a=_a;
		opt=a.opt;
		
		_num_record_blocks=-1;
		unwrapSuffix=false;
		
		readInConfigs(a.UIProjects);
	}

	@Override
	protected String getSaveUrl(WebViewmy mWebView) {
		return currentDisplaying;
	}

	@Override
	protected void onPageSaved() {
		super.onPageSaved();
		a.notifyDictionaryDatabaseChanged(bookPresenter_notebook.this);
	}
	
	/*** Page constituents:<br/>
	 * 1. Index <br/>
	 * 2~n. Extra entrances [Optional].  Search History [Optional]. <br/>
	 * n~m. Book marks [not implemented]<br/>
	 * m~x. Page overrides [Optional]<br/>
	 */
	@Override
	public long getNumberEntries() {
		return PageCursor.getCount();
	}
	
	@Override
	public String getEntryAt(int position) {
		if(position==0)
			return "index";
		position-=1;
		try {
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
	
	@Override
	public void renderContentAt(float initialScale, int SelfIdx, int frameAt, WebViewmy mWebView, int...position) {
		if(mWebView==null)
			mWebView=this.mWebView;
		boolean resposibleForThisWeb = mWebView==this.mWebView;

		if(resposibleForThisWeb||a.PeruseView!=null&&a.PeruseView.mWebView==mWebView)
			tintBackground(mWebView);

		if(SelfIdx!=-1){
			preloading(mWebView, frameAt, SelfIdx, position);
			if(resposibleForThisWeb && mWebView.fromCombined==1  && PDICMainAppOptions.getTmpIsCollapsed(tmpIsFlag)){/* 自动折叠 */
				mWebView.awaiting = true;
				mWebView.setVisibility(View.GONE);
				return;
			}
		}
		
		int pos = position[0];
		
		PageCursor.moveToPosition(pos);
		currentDisplaying = PageCursor.getString(0);
		if(resposibleForThisWeb)
			toolbar_title.setText(PageCursor.getString(1));
		
		if(mWebView==this.mWebView) {
			if(a.opt.getHideScroll1()&&resposibleForThisWeb)
				a.mBar.setVisibility(View.GONE);
			else {
				a.mBar.setDelimiter("< >", mWebView);
			}
		}
		
		StringBuilder sb = AcquireStringBuffer(8196).append("window.rcsp=")
				.append(MakeRCSP(opt)).append(";")
				.append(loadJs);
		if (jsLoader != null) sb.append(jsLoader);
		if (onload != null) sb.append(onload);
		if (onstart != null) sb.append(onstart);
		if(GlobalOptions.isDark) sb.append(MainActivityUIBase.DarkModeIncantation);
		if(getContentEditable() && getEditingContents() && mWebView!=a.popupWebView)
			sb.append(MainActivityUIBase.ce_on);
		
		jsCode = sb.toString();
		
		//mWebView.loadUrl(currentUrl=url);
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
		mWebView.MutateBGInTitle();
		
		recess.setOnClickListener(voyager);
		forward.setOnClickListener(voyager);
		setSaveIconLongClick(ic_save);
		setWebLongClickListener(mWebView, a);
		if(computerFace) {
			mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36");
		}
	}
	
	class OptionListHandlerDynWeb extends OptionListHandlerDyn {
		public OptionListHandlerDynWeb(MainActivityUIBase a, WebViewmy mWebView, String extra) {
			super(a, mWebView, extra);
		}
		@Override
		public void onClick(DialogInterface dialog, int _pos) {
			int pos=_pos+IU.parsint(((AlertDialog)dialog).getListView().getTag());
			switch(pos) {
				/* 查看原网页 */
				case 20:{
					final_mWebView.setTag(R.id.save, false);
					final_mWebView.reload();
				} break;
				/* 新增入口 */
				case 22:{
					String _url = final_mWebView.getUrl();
					if(_url!=null) {
						EditText etNew = FU.buildStandardTopETDialog(a, false);
						etNew.setText(final_mWebView.getTitle());
						View btn_Done = ((View)etNew.getTag());
						View btn_exec = ((View)btn_Done.getTag());
						btn_Done.setTag(btn_exec.getTag());
						btn_exec.setVisibility(View.GONE);
						btn_Done.setOnClickListener(v1 -> {
							String __url = _url;
							String name = etNew.getText().toString();
							if(name.length()>0) __url += "\r"+etNew.getText();
							if(entrance==null) entrance=new ArrayList<>();
							entrance.add(__url);
							bNeedSave = true;
							a.notifyDictionaryDatabaseChanged(bookPresenter_notebook.this);
							((Dialog)v1.getTag()).dismiss();
						});
					}
				} break;
				/* 浏览器中打开 */
				case 31: {
					try {
						Intent intent = new Intent();
						intent.setData(Uri.parse(url));
						intent.setAction(Intent.ACTION_VIEW);
						a.startActivity(intent);
					} catch (Exception e) { }
				}
				break;
				/* 复制链接 */
				case 32: { }
				break;
				/* 复制文本 */
				case 33:
				break;
				default:
					super.onClick(dialog, _pos);
				return;
			}
			dialog.dismiss();
		}
	}
	
	@Override
	public void setSaveIconLongClick(View v) {
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
					if(url==null) {
						url = currentUrl;
					}
					OptionListHandlerDynWeb olhdw = new OptionListHandlerDynWeb(a, _mWebView, url);
					buildStandardOptionListDialog(a, R.string.page_options
							, R.array.config_web_pages, olhdw, url, olhdw, 20);
					return false;
				}
			};
		}
		v.setOnLongClickListener(savelcl);
	}
	
	@Override
	public void unload() {
		if(bNeedSave) { // 保存 json
			if(entrance!=null) {
				String _entrance = StringUtils.EMPTY;
				int size = entrance.size();
				if(size>0) {
					StringBuilder sb = AcquireStringBuffer((int) (size*entrance.get(0).length()*1.5));
					for (int i = 0; i < size; i++) {
						sb.append(entrance.get(i));
						if(i<size-1) {
							sb.append("\n");
						}
					}
					_entrance = sb.toString();
				}
				website.put("entrance", _entrance);
			}
			
			String webout = website.toString(SerializerFormat);
			
			webout = webout.replaceAll("\t\"(.*)\":", " $1:").replace("\\n", "\n");
			
			CMN.Log(webout);
			
			BU.printFile(webout.getBytes(), f);
			
			bNeedSave=false;
		}
		super.unload();
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
	public void Reload() {
		super.Reload();
	}
	
	public boolean hasCover() {
		return false;
	}

	public void onPageStarted(WebView view, String url, boolean updateTitle) {
		WebViewmy mWebView = (WebViewmy) view;
		if(updateTitle)
			mWebView.toolbar_title.setText(currentDisplaying=view.getTitle());
		if(progressProceed !=null) {
			progressProceed.cancel();
		}
		if(mWebView.titleBar!=null) {
			mWebView.titleBar.getBackground().setLevel(1500);
			((LayerDrawable)mWebView.titleBar.getBackground()).getDrawable(1).setAlpha(255);
		}
		//onProgressChanged(mWebView, 5);
		
		currentUrl=view.getUrl();
//		if(jsLoader!=null || onstart!=null) {
//			String jsCode = "window.rcsp=" + MakeRCSP(opt) + ";";
//			if (jsLoader != null) jsCode += jsLoader;
//			if (onstart != null) jsCode += onstart;
//			////view.evaluateJavascript(jsCode, null);
//		}
		//CMN.Log("onPageStarted\n\nonPageStarted -- ", url);CMN.rt();CMN.stst_add=0;
	}

	public void onProgressChanged(WebViewmy mWebView, int newProgress) {
		CMN.Log("onProgressChanged", newProgress);
		Drawable d = mWebView.titleBar.getBackground();
		int start = d.getLevel();
		int end = newProgress*100;
		if(end<start) end=start+10;
		if(progressProceed !=null) {
			progressProceed.cancel();
			progressProceed.setIntValues(start, end);
		} else {
			progressProceed = ObjectAnimator.ofInt(d,"level", start, end);
			progressProceed.setDuration(100);
		}
		progressProceed.start();
		if(newProgress>85){
			mWebView.evaluateJavascript(jsCode, null);
		}
		if(newProgress>=98){
			CMN.Log("newProgress>=98", newProgress);
			fadeOutProgressbar(mWebView, newProgress>=99);
		}
		if(newProgress>=20){
			//view.evaluateJavascript(projs, null);
		}
	}

	/** 接管进入黑暗模式、编辑模式 */
	public void onPageFinished(WebView view, String url, boolean updateTitle) {
		CMN.Log("chromium", "web  - onPageFinished", currentUrl);
		fadeOutProgressbar((WebViewmy) view, updateTitle);
		currentUrl=view.getUrl();
		view.evaluateJavascript(jsCode, null);
	}

	private void fadeOutProgressbar(WebViewmy mWebView, boolean updateTitle) {
		if(updateTitle) mWebView.toolbar_title.setText(mWebView.word=currentDisplaying=mWebView.getTitle());
		Drawable d = ((LayerDrawable) mWebView.titleBar.getBackground()).getDrawable(1);
		if(d.getAlpha()!=255) {
			return;
		}
		if(progressTransient!=null){
			progressTransient.cancel();
			progressTransient.setFloatValues(d.getAlpha(), 0);
		} else {
			progressTransient = ObjectAnimator.ofInt(d, "alpha", d.getAlpha(), 0);
			progressTransient.setDuration(200);
		}
		progressTransient.start();
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
			MdxDBHelper con = getCon(false);
			if(con!=null){
				if(url.startsWith(host)) {
					url=url.substring(host.length());
				}
				//CMN.Log("web enssurePageTable");
				con.enssurePageTable();
				return con.getPageStream(url);
			}
		}
		return null;
	}

	@Override
	public CachedDirectory getInternalResourcePath(boolean create) {
		if(create && InternalResourcePath!=null){
			InternalResourcePath.mkdirs();
		}
		return InternalResourcePath;
	}

	@Override
	public String getAboutString() {
		return _Dictionary_fName+"<br>"
				+ website.toString(SerializerFormat)
					.replaceAll("\n\t\"(.*)\":", "<br>\t$1:").replace("\\n", "\n");
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
			return new X509Certificate[] {};
		}
	}
	
	@Override
	public String getCharsetName() {
		return "online";
	}
}

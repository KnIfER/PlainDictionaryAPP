package com.knziha.plod.widgets;

import java.lang.reflect.Field;

import com.knziha.plod.PlainDict.R;
import com.knziha.plod.PlainDict.CMN;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.PopupWindow;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewmy extends WebView {
    public Context context;
    int ContentHeight=0;
    
    

    //@Override computeHorizontalScrollRange()
    //protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //    int mExpandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
    //    super.onMeasure(widthMeasureSpec, mExpandSpec);
    //}
    //会导致滚动不了
    public interface PlayFinish{
        void doit();
        void reset();
    }
    public PlayFinish df;
    public void setDf(PlayFinish playFinish) {
        this.df = playFinish;
    }
    public WebViewmy(Context context) {
        super(context);
        this.context=context;
        init();
        //Log.e("WebViewmy","con1");
    }
    public WebViewmy(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        init();
        //Log.e("WebViewmy","con2");
    }
    
    
    private void init(){
        //setBackgroundColor(Color.parseColor("#C7EDCC"));
        //setBackgroundColor(0);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        //setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        
        //网页设置初始化
    	final WebSettings settings = getSettings();
    	
    	//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    	//	settings.setSafeBrowsingEnabled(false);
        //}
    	
    	settings.setSupportZoom(true);
    	settings.setBuiltInZoomControls(true);  
    	settings.setDisplayZoomControls(false);
    	settings.setDefaultTextEncodingName("UTF-8");
        
    	settings.setNeedInitialFocus(false);
    	//settings.setDefaultFontSize(40);
    	//settings.setTextZoom(100);
    	//setInitialScale(25);
    	
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        
		//settings.setUseWideViewPort(true);//设定支持viewport
		//settings.setLoadWithOverviewMode(true);
    	settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); 
    	//settings.setSupportZoom(support);
    	
		settings.setAllowUniversalAccessFromFileURLs(true);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 
        	//mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
		
		//settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //设置 缓存模式

		p2 = new Paint();
        p3 = new Paint();
        p2.setColor(Color.parseColor("#ffffff"));
        p2.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        p3.setColor(Color.YELLOW);
        p3.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
		//setLayerType(View.LAYER_TYPE_HARDWARE, null); 
        webScale=getResources().getDisplayMetrics().density;
    }
    public int getContentHeight(){
    	//if(ContentHeight==0)
    		ContentHeight = computeVerticalScrollRange();
		return ContentHeight;
    }
    public int getContentOffset(){
    	return this.computeVerticalScrollOffset();
    }

    Paint p2,p3;

    //onDraw表示显示完毕
    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawRect(0,0,200,200, p3);
        //canvas.drawPath(path, paint);
        super.onDraw(canvas);
        if(df!=null)
    	df.doit();
       // canvas.drawRect(0,0,200,200, p2);

    }

    
    //Checks each time the bar is laid out. If there are few enough view that
    //they all fit on the screen then the bar is hidden. If a view is added which doesn't fit on
    //the screen then the bar is unhidden.
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
    
    @Override
    public void scrollTo(int x, int y) {
    	//CMN.Log("webscrollTo "+x+" TO "+y);
    	super.scrollTo(x, y);
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
		if(SrollChanged!=null)
			SrollChanged.onScrollChange(this,l,t,oldl,oldt);
    }
	public void setOnSrollChangedListener(ListViewmy.OnScrollChangeListener onSrollChangedListener) {
		SrollChanged=onSrollChangedListener;
	}
	ListViewmy.OnScrollChangeListener SrollChanged;

	@Override
    public void clearMatches() {
		super.clearMatches();
        //checkThread();
        //mProvider.clearMatches();
    }
	public boolean isloading=false;
	@Override
	public void loadDataWithBaseURL(String baseUrl,String data,String mimeType,String encoding,String historyUrl) {
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		//if(!baseUrl.equals("about:blank"))
			isloading=true;
    }
	
    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        //CMN.Log("onSizeChanged  ");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //CMN.Log("onMeasure  ");
    }
    
    
	@Override
	public void loadUrl(String url) {
		super.loadUrl(url);
		//CMN.show(""+url.equals("about:blank"));
		//if(!url.equals("about:blank"))
			isloading=true;
			
    }
	
	@Override
	public void setWebViewClient(WebViewClient client){
		super.setWebViewClient(wvclient=client);
	}
	public WebViewClient wvclient;
	protected boolean MyMenuinversed;
	
	 @Override
	protected void onCreateContextMenu(ContextMenu menu){
		//Toast.makeText(getContext(), "ONCCM", 0).show();
		super.onCreateContextMenu(menu);
	}
	
	 public boolean bIsActionMenuShown;
	 public callbackme callmeback;
	 private class callbackme extends ActionMode.Callback2{
	 	ActionMode.Callback callback;
	 	public callbackme callhere(ActionMode.Callback callher) {callback=callher;return this;}
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			return bIsActionMenuShown=callback.onCreateActionMode(mode, menu);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return callback.onPrepareActionMode(mode, menu);
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()) {
				case R.id.toolbar_action0:
					evaluateJavascript(getHighLightIncantation()+"if(window.getSelection)sel.removeAllRanges();",new ValueCallback<String>() {
								@Override
								public void onReceiveValue(String value) {
									invalidate();
								}});
				     
				     MyMenuinversed=!MyMenuinversed;
				return true;
				case R.id.toolbar_action1://工具复用，我真厉害啊啊啊啊！
					//evaluateJavascript("document.execCommand('selectAll'); console.log('dsadsa')",null);
					View cover=((ViewGroup) getParent()).getChildAt(0).findViewById(R.id.cover);
					cover.setTag(0);
					cover.performClick();
				return false;
			}
			return callback.onActionItemClicked(mode, item);
		}
		PopupWindow mPopup;
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			bIsActionMenuShown=false;
			//Toast.makeText(getContext(),"onDestroyActionMode", 0).show();
		}
		
		@Override
		public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
			if(ActionMode.Callback2.class.isInstance(callback))
				((ActionMode.Callback2)callback).onGetContentRect(mode, view, outRect);
			else
				super.onGetContentRect(mode, view, outRect);
			//Toast.makeText(getContext(),"onGetContentRect"+(view==WebViewmy.this), 0).show();
		}		
   }
   //Viva Marshmallow!
   @Override
   public ActionMode startActionMode(ActionMode.Callback callback, int type) {
	    MyMenuinversed=false;
	    if(callmeback==null) callmeback =new callbackme();
        ActionMode mode = super.startActionMode(callmeback.callhere(callback),type);
        //if(true) return mode;
       //Toast.makeText(getContext(), mode.getTag()+"ONSACTM"+mode.hashCode(), 0).show();
       //if(true) return mode;
       //mode.setTag(110);
       final Menu menu = mode.getMenu();
       int gid=0;
       if(menu.size()>0) {
    	   MenuItem item0 = menu.getItem(0);
    	   if(item0.getTitle().toString().startsWith("地") || item0.getTitle().toString().startsWith("Map"))
    		   menu.removeItem(item0.getItemId());
    	   if(menu.size()>0) gid=menu.getItem(0).getGroupId();
       }
       
       int highlightColor=Color.YELLOW;
       String ColorCurse = String.format("%06X", highlightColor&0xFFFFFF);
       Spanned text = Html.fromHtml("<span style='background:#"+ColorCurse+"; color:#"+ColorCurse+";'>高亮</span>");
       
       MenuItem MyMenu = menu.add(gid, R.id.toolbar_action0, 0, text);
       
       //Toast.makeText(getContext(),""+MyMenu.view,0).show();
       MyMenu = null;
       //MyMenu.get
       
       //Toast.makeText(getContext(),"asd"+menu.findItem(android.R.id.),0).show();
       //Toast.makeText(getContext(), MyMenu.getIntent()+""+MyMenu.getTitle()+" "+MyMenu.getItemId()+getResources().getString(android.R.string.share),0).show();
       //Toast.makeText(getContext(), ""+getResources().getString(getReflactField("com.android.internal.R$string", "share")),0).show();
       //Toast.makeText(getContext(),menu.getItem(3).getItemId()+"="+menu_share_id+"finding menu_share:"+menu.findItem(menu_share_id)+"="+android.R.id.shareText,0).show();

       String shareText="分享";
       int ShareString_Id=Resources.getSystem().getIdentifier("share","string", "android");
       if(ShareString_Id!=0)
    	   shareText=getResources().getString(ShareString_Id);
       String SelectAllText=getResources().getString(android.R.string.selectAll);
       int findCount=2;
       int ToolsOrder=0;
       for(int i=0;i<menu.size();i++) {
    	   String title = menu.getItem(i).getTitle().toString();
    	   if(title.equals(shareText)) {
    		   menu.removeItem(menu.getItem(i).getItemId());//移除 分享
    		   i--;
    		   findCount--;
    	   }else if(title.equals(SelectAllText)) {
    		   ToolsOrder=menu.getItem(i).getOrder();
    		   menu.removeItem(menu.getItem(i).getItemId());//移除 全选
    		   i--;
    		   findCount--;
    	   }
    	   if(findCount==0) break;
       }
       
	   MyMenu=menu.add(gid,R.id.toolbar_action1,ToolsOrder+1,R.string.tools);

       
       
       //Toast.makeText(menu.getItem(0).getTitle()).show();
       return mode;
   }
   
   private final static String commonIcan = "function getNextNode(b){var a=b.firstChild;if(a){return a}while(b){if((a=b.nextSibling)){return a}b=b.parentNode}}function getNodesInRange(c){var b=[];var f=c.startContainer;var a=c.endContainer;var d=c.commonAncestorContainer;var e;for(e=f.parentNode;e;e=e.parentNode){b.push(e);if(e==d){break}}b.reverse();for(e=f;e;e=getNextNode(e)){b.push(e);if(e==a){break}}return b}function getNodeIndex(b){var a=0;while((b=b.previousSibling)){++a}return a}function insertAfter(d,b){var a=b.nextSibling,c=b.parentNode;if(a){c.insertBefore(d,a)}else{c.appendChild(d)}return d}function splitDataNode(c,a){var b=c.cloneNode(false);b.deleteData(0,a);c.deleteData(a,c.length-a);insertAfter(b,c);return b}function isCharacterDataNode(b){var a=b.nodeType;return a==3||a==4||a==8}function splitRangeBoundaries(b){var f=b.startContainer,e=b.startOffset,c=b.endContainer,a=b.endOffset;var d=(f===c);if(isCharacterDataNode(c)&&a>0&&a<c.length){splitDataNode(c,a)}if(isCharacterDataNode(f)&&e>0&&e<f.length){f=splitDataNode(f,e);if(d){a-=e;c=f}else{if(c==f.parentNode&&a>=getNodeIndex(f)){++a}}e=0}b.setStart(f,e);b.setEnd(c,a)}function getTextNodesInRange(b){var f=[];var a=getNodesInRange(b);for(var c=0,e,d;e=a[c++];){if(e.nodeType==3){f.push(e)}}return f}function surroundRangeContents(b,g){splitRangeBoundaries(b);var f=getTextNodesInRange(b);if(f.length==0){return}for(var c=0,e,d;e=f[c++];){if(e.nodeType==3){d=g.cloneNode(false);e.parentNode.insertBefore(d,e);d.appendChild(e)}}b.setStart(f[0],0);var a=f[f.length-1];b.setEnd(a,a.length)};";
   private String HighLightIncantation;
   private String DeHighLightIncantation;
   public float webScale=0;
   public String getHighLightIncantation() {
	   if(HighLightIncantation==null)
		   HighLightIncantation=commonIcan+"if(window.getSelection){var spanner=document.createElement(\"span\");spanner.className=\"PLOD_HL\";spanner.style=\"background:#ffaaaa;\";var sel=window.getSelection();var ranges=[];var range;for(var i=0,len=sel.rangeCount;i<len;++i){ranges.push(sel.getRangeAt(i))}/*sel.removeAllRanges();*/i=ranges.length;while(i--){range=ranges[i];surroundRangeContents(range,spanner)}};";
	   return HighLightIncantation;
   }
   public String getDeHighLightIncantation() {
	   if(DeHighLightIncantation==null)
		   DeHighLightIncantation=commonIcan+"function recurseDeWrap(b){if(b){for(var e=b.length-1,d;e>=0;e--){d=b[e];if(d.className==\"PLOD_HL\"){var c=0;for(var f=d.childNodes.length-1;f>=0;f--){var a=d.childNodes[f];if(!c){c=d}d.parentNode.insertBefore(a,c);c=a}d.parentNode.removeChild(d)}}}}if(window.getSelection){var spanner=document.createElement(\"span\");spanner.className=\"highlight\";var sel=window.getSelection();var ranges=[];var range;for(var i=0,len=sel.rangeCount;i<len;++i){ranges.push(sel.getRangeAt(i))}/*sel.removeAllRanges();*/i=ranges.length;while(i--){range=ranges[i];var nodes=getNodesInRange(range);recurseDeWrap(nodes)}};";
	   return DeHighLightIncantation;
   }
   
public static int getReflactField(String className,String fieldName){
       int result = 0;
       try {
           Class<?> clz = Class.forName(className);
           Field field = clz.getField(fieldName);
           field.setAccessible(true);
           result = field.getInt(null); 
       } catch (Exception e) {
           e.printStackTrace();
       }
       return result;
}
  
public static int[] getReflactIntArray(String className,String fieldName){
	int[] result = null;
    try {
        Class<?> clz = Class.forName(className);
        Field field = clz.getField(fieldName);
        field.setAccessible(true);
        result = (int[]) field.get(null); 
    } catch (Exception e) {
        e.printStackTrace();
    }
    return result;
}

@Override
public boolean zoomOut(){
	CMN.Log("zoomOut");
	return false;
}

}
package com.knziha.plod.plaindict;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.jaredrummler.colorpicker.ColorPickerDialogListener;
import com.knziha.ankislicer.customviews.WahahaTextView;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.filepicker.view.GoodKeyboardDialog;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.paging.AppIconCover.AppIconCover;
import com.knziha.paging.AppIconCover.AppInfoBean;
import com.knziha.plod.PlainUI.AlloydPanel;
import com.knziha.plod.PlainUI.AnnotAdapter;
import com.knziha.plod.PlainUI.AppUIProject;
import com.knziha.plod.PlainUI.BookmarkAdapter;
import com.knziha.plod.PlainUI.BottombarTweakerAdapter;
import com.knziha.plod.PlainUI.BuildIndexInterface;
import com.knziha.plod.PlainUI.DBUpgradeHelper;
import com.knziha.plod.PlainUI.FloatApp;
import com.knziha.plod.PlainUI.FloatBtn;
import com.knziha.plod.PlainUI.MenuGrid;
import com.knziha.plod.PlainUI.NightModeSwitchPanel;
import com.knziha.plod.PlainUI.PlainAppPanel;
import com.knziha.plod.PlainUI.QuickBookSettingsPanel;
import com.knziha.plod.PlainUI.SearchbarTools;
import com.knziha.plod.PlainUI.WeakReferenceHelper;
import com.knziha.plod.PlainUI.WordPopup;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.db.MdxDBHelper;
import com.knziha.plod.db.SearchUI;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.Bag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.MyPair;
import com.knziha.plod.dictionary.Utils.ReusableBufferedInputStream;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.SubStringKey;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymanager.BookManager;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.SparseArrayMap;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.dictionarymodels.resultRecorderScattered;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.searchtasks.AsyncTaskWrapper;
import com.knziha.plod.searchtasks.CombinedSearchTask;
import com.knziha.plod.settings.BookOptionsDialog;
import com.knziha.plod.settings.Multiview;
import com.knziha.plod.settings.NightMode;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.settings.TapTranslator;
import com.knziha.plod.settings.Misc_exit_dialog;
import com.knziha.plod.slideshow.PhotoViewActivity;
import com.knziha.plod.widgets.AppIconsAdapter;
import com.knziha.plod.widgets.CustomShareAdapter;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.EditTextmy;
import com.knziha.plod.widgets.FlowCheckedTextView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.PageSlide;
import com.knziha.plod.widgets.ListSizeConfiner;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.MultiplexLongClicker;
import com.knziha.plod.widgets.OnScrollChangedListener;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.TwoColumnAdapter;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.text.ColoredHighLightSpan;
import com.knziha.text.ColoredTextSpan1;
import com.knziha.text.ScrollViewHolder;
import com.knziha.text.SelectableTextView;
import com.knziha.text.SelectableTextViewBackGround;
import com.knziha.text.SelectableTextViewCover;
import com.knziha.text.TTSMoveToucher;

import org.apache.commons.imaging.BufferedImage;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.knziha.metaline.Metaline;
import org.knziha.metaline.StripMethods;
import org.nanohttpd.protocols.http.HTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.xiph.speex.ByteArrayRandomOutputStream;
import org.xiph.speex.manyclass.JSpeexDec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knziha.plod.dictionary.Utils.IU.NumberToText_SIXTWO_LE;
import static com.knziha.plod.dictionary.mdBase.markerReg;
import static com.knziha.plod.dictionarymodels.BookPresenter.baseUrl;
import static com.knziha.plod.plaindict.CMN.AssetTag;
import static com.knziha.plod.plaindict.CMN.EmptyRef;
import static com.knziha.plod.plaindict.CMN.GlobalPageBackground;
import static com.knziha.plod.plaindict.DeckListAdapter.DB_FAVORITE;
import static com.knziha.plod.plaindict.DeckListAdapter.DB_HISTORY;
import static com.knziha.plod.plaindict.MainShareActivity.SingleTaskFlags;
import static com.knziha.plod.plaindict.MdictServerMobile.getTifConfig;
import static com.knziha.plod.widgets.WebViewmy.getWindowManagerViews;

/** 程序基础类<br/>
 *  Class for all dictionary activities. <br/>
 * Created by KnIfER on 2018
 */
@SuppressLint({"ResourceType", "SetTextI18bbn","Registered", "ClickableViewAccessibility","PrivateApi","DiscouragedPrivateApi"})
@StripMethods(strip=true, keys={"setMagicNumber", "setWebDebug", "getRemoteServerRes", "hasRemoteDebugServer"})
public abstract class MainActivityUIBase extends Toastable_Activity implements OnTouchListener,
		OnLongClickListener,
		OnClickListener,
		OnMenuItemClickListener, OnDismissListener,
		MenuItem.OnMenuItemClickListener,
		OptionProcessor,
		SettingsPanel.FlagAdapter {
	
	public int schuiMainPeruse;
	public int schuiMainSchs;
	public int schuiMain;
	public int schuiList;
	public List<View> wViews;
	protected WeakReference[] WeakReferencePool = new WeakReference[WeakReferenceHelper.poolSize];
	public mdict.AbsAdvancedSearchLogicLayer taskRecv;
	
	//private static final String RegExp_VerbatimDelimiter = "[ ]{1,}|\\pP{1,}|((?<=[\\u4e00-\\u9fa5])|(?=[\\u4e00-\\u9fa5]))";
	private static final Pattern RegImg = Pattern.compile("(png$)|(jpg$)|(jpeg$)|(tiff$)|(tif$)|(bmp$)|(webp$)", Pattern.CASE_INSENSITIVE);
	private static Pattern bookMarkEntryPattern = Pattern.compile("entry://@[0-9]*");
	public static final String SEARCH_ACTION   = "plaindict.intent.action.SEARCH";
	public static final String EXTRA_QUERY    = "EXTRA_QUERY";
	public static final String EXTRA_FULLSCREEN = "EXTRA_FULLSCREEN";
	public static final String EXTRA_HIDE_NAVIGATION = "EXTRA_HIDE_NAV";
	public static final String EXTRA_HEIGHT   = "EXTRA_HEIGHT";
	public static final String EXTRA_WIDTH    = "EXTRA_WIDTH";
	public static final String EXTRA_GRAVITY   = "EXTRA_GRAVITY";
	public static final String EXTRA_MARGIN_LEFT  = "EXTRA_MARGIN_LEFT";
	public static final String EXTRA_MARGIN_TOP   = "EXTRA_MARGIN_TOP";
	public static final String EXTRA_MARGIN_BOTTOM  = "EXTRA_MARGIN_BOTTOM";
	public static final String EXTRA_MARGIN_RIGHT  = "EXTRA_MARGIN_RIGHT";
	public String extraText =null;//世           界     你好 happy呀happy\"人\"’。，、？
	public String extraInvoker = null;
	public String hardSearchKey;
	public static boolean bSkipNxtExtApp;
	public static final KeyEvent BackEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
	final static String entryTag = "entry://";
	final static String soundTag = "sound://";
	protected final static String soundsTag = "sounds://";
	public boolean hideDictToolbar=false;
	public boolean isBrowsingImgs;
	public OnLongClickListener mdict_web_lcl;
	public int defbarcustpos;
	public int cbar_key;
	public StringBuilder MainStringBuilder;
	Runnable execSearchRunnable;
	CharSequence search_cs;
	int search_count;
	boolean focused;
	protected MdictServerMobile server;
	public Map<SubStringKey, String>  serverHosts;
	public ArrayList<PlainWeb>  serverHostsHolder=new ArrayList();
	public FrameLayout lvHeaderView;
	public FloatApp floatApp;
	public final Bag bNeverBlink = new Bag(false);
	/** |0x1=xuyao store| |0x2=zhuanhuan le str| |0x4==刚刚点开搜索框|  */
	public int textFlag =0;
	final public TextWatcher tw1 = new TextWatcher() { //tw
		public void onTextChanged(CharSequence cs, int start, int before, int count) {
			if (isContentViewAttached() && ActivedAdapter!=null && (textFlag &0x4)!=0) {
				ActivedAdapter.SaveVOA();
				textFlag &= ~0x4;
			}
			if(SU.isNotEmpty(cs)) {
				etSearch_ToToolbarMode(3);
				root.removeCallbacks(execSearchRunnable);
				search_cs=cs;
				search_count=count;
				if(start==before&&before==-1) {
					execSearchRunnable.run();
				} else {
					root.postDelayed(execSearchRunnable, 150);
					if (textFlag !=1) {
						textFlag =1;
					}
				}
				if(etTools.isVisible())
					etTools.dismiss();
			} else {
				if(PDICMainAppOptions.getSimpleMode()) adaptermy.notifyDataSetChanged();
				lv2.setVisibility(View.INVISIBLE);
				//if(searchbarTools!=null && true) searchbarTools.toggle();
			}
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		public void afterTextChanged(Editable s) {
			if (s.length() != 0) {
				//ivDeleteText.setVisibility(View.VISIBLE);
			}
		}
	};
	boolean bShowLoadErr=true;
	public boolean restLastSch;
	public boolean isCombinedSearching;
	public String CombinedSearchTask_lastKey;
	//public HashMap<CharSequence,byte[]> mBookProjects;
	//public HashSet<CharSequence> dirtyMap;
	protected ImageView browser_widget1;
	
	public boolean drawerOpen;
	public Drawer drawerFragment;
	public DictPicker dictPicker;
	public ViewGroup main;
	public ViewGroup mainF;
	
	public ContentviewBinding contentUIData;
	public WebViewListHandler weblistHandler;
	/** 点击设置按钮、设置时更新这个变量。 */
	public WebViewListHandler weblist;
	public WebViewListHandler randomPageHandler;
	public boolean refreshingRandom;
	public ViewGroup webSingleholder;
	protected WindowManager wm;
	public FloatBtn floatBtn;

	protected String lastEtString;
	public ViewGroup mainframe;

	public ListViewmy lv,lv2, mlv1, mlv2;
	protected ViewGroup mlv;
	public BasicAdapter adaptermy;
	public BasicAdapter adaptermy2;
	public BasicAdapter adaptermy3;
	public ListViewAdapter2 adaptermy4;
	public ListViewAdapter2 adaptermy5;
	public BasicAdapter ActivedAdapter;
	public BaseHandler hdl;
	public int  CurrentViewPage = 1;
	public String fontFaces;
	
	public MenuBuilder AllMenus;
	public List<MenuItemImpl> AllMenusStamp;
	MenuItem menuSearchMode;
	List<MenuItemImpl> MainMenu;
	List<MenuItemImpl> LEFTMenu;
	List<MenuItemImpl> SingleContentMenu;
	List<MenuItemImpl> Multi_ContentMenu;
	
	MenuItem PeruseListModeMenu;
	
	ViewGroup snack_holder;
	public BookPresenter EmptyBook;
	@NonNull public BookPresenter currentDictionary;
	HashSet<String> mdlibsCon;
	public ArrayList<BookPresenter> md = new ArrayList<>();//Collections.synchronizedList(new ArrayList<mdict>());
	
	public resultRecorderDiscrete EmptySchResults;
	
	public Dialog taskd;
	DArrayAdapter AppFunAdapter;
	BufferedWriter output;
	BufferedWriter output2;
	public final static String ce_on="document.body.contentEditable=!0";
	public final static String ce_off="document.body.contentEditable=!1";
	/**if(!window.randx) {
	 	//console.log('randxClick...init');
		window.randx=1;
	 	window.addEventListener('click', function(e){
			var p=e.path,t=e.srcElement;
			if(!p && e.composedPath) p=e.composedPath();
			if(p) for(var i=0;(t=p[i])&&i++<5;) if(t.tagName==='A') {break;}
			//console.log('randxClick...', t);
			if(t && t.tagName==='A') {
				 console.log(t.innerText);
	 			if(window.randx_mode!=0){
					 e.preventDefault();
					 e.stopPropagation();
				 	if(randx_mode==1) {
	 					app.wordtoday(t.innerText);
					 } else {
						 var br = t.getBoundingClientRect();
						 var pX = br.left;
						 var pY = br.top;
						 var pW = br.width;
						 var pH = br.height;
						 
						 //app.popupWord(sid.get(), t.innerText, e.clientX, e.clientY, -1);
						 app.popupWord(sid.get(), t.innerText, -1, document.documentElement.scrollLeft+pX, document.documentElement.scrollTop+pY, pW, pH);
					 }
					return true;
				 }
			} else { window._touchtarget = t; app.suppressLnk(sid.get()) }
		},true,true);
	 } */
	@Metaline
	public final static String randx_on="";
	/**var t=window._touchtarget;if(t) {
		 var br = t.getBoundingClientRect();
		 var pX = br.left;
		 var pY = br.top;
		 var pW = br.width;
		 var pH = br.height;
		 app.popupWord(sid.get(), t.innerText, -1, document.documentElement.scrollLeft+pX, document.documentElement.scrollTop+pY, pW, pH);
	 } */
	@Metaline
	public final static String randx_remake="";
	public final static String randx_off="window.randx_mode=0";
	LexicalDBHelper favoriteCon;//public LexicalDBHelper getFDB(){return favoriteCon;};
	/** Use a a filename-directory map to keep a record of lost files so that users could add them back without the need of restore every specific directory.  */
	HashMap<String,String> lostFiles;

	public PeruseView peruseView;
	/** 主程有 */
	public @Nullable ViewGroup bottombar;
	public boolean bRequestedCleanSearch;
	public boolean bRequestedLvHideIM;
	public boolean bWantsSelection;
	public boolean 来一发;
	public boolean bIsFirstLaunch=true;
	/** 长按上下切换词典，进入速览模式 */
	public boolean fastPreview=true;
	
	protected int DockerMarginL,DockerMarginR,DockerMarginT,DockerMarginB;

	boolean isFragInitiated = false;
	AsyncTaskWrapper lianHeTask;
	public int[] pendingLv2Pos;
	public int pendingLv2ClickPos=-1;
	public int split_dict_thread_number;
	public static final ListSizeConfiner mListsizeConfiner = new ListSizeConfiner();
	private Runnable NaughtyJumpper;
	volatile long jumpNaughtyTimeToken;

	public AtomicInteger poolEUSize = new AtomicInteger(0);


	
	WeakReference<AlertDialog> setchooser = ViewUtils.DummyRef;
	private WeakReference<BottomSheetDialog> bottomPlaylist = ViewUtils.DummyRef;
	WeakReference<AlertDialog> ChooseFavorDialog = ViewUtils.DummyRef;
	WeakReference<DBroswer> DBrowserHolder = ViewUtils.DummyRef;
	DBroswer DBrowser;
	DeckListAdapter.DeckListData[] DBrowserDatas = new DeckListAdapter.DeckListData[2];
	
	ViewGroup PhotoPagerHolder;
	ViewPager PhotoPager;
	ImageView PhotoCover;
	ArrayList<String> PhotoUrls = new ArrayList<>();
	PagerAdapter PhotoAdapter;
	String new_photo;

	public int AutoBrowseTimeOut = 500;
	public int AutoBrowseCount;
	public boolean AutoBrowsePaused=true;
	public boolean bThenReadContent;
	public WebViewmy pendingWebView;
	public int mThenReadEntryCount;
	public static int foreground;
	protected boolean click_handled_not;

	protected TextView TTSController_tvHandle;
	protected TTSMoveToucher TTSController_moveToucher;
	protected TextView TTSController_indicator;
	protected SelectableTextView TTSController_tv;
	public ImageView TTSController_popIvBack;
	public ImageView TTSController_playBtn;
	protected ViewGroup TTSController_;
	protected ViewGroup TTSController_toolbar, TTSController_bottombar;
	protected CircleCheckBox TTSController_ck1;
	protected CircleCheckBox TTSController_ck2;
	Runnable PhotoRunnable=new Runnable() {
		@Override
		public void run() {
			if(new_photo!=null) {
				int idx = PhotoUrls.lastIndexOf(new_photo);
				if (idx == -1) {
					idx = PhotoUrls.size();
					PhotoUrls.add(new_photo);
				}
				PhotoAdapter.notifyDataSetChanged();
				PhotoPager.setCurrentItem(idx, false);
			}
		}
	};
	public int mActionModeHeight;
	private int[] LocationFetcher = new int[2];
	private Runnable mFeetHeightScalerRunnable;
	
	public int app_panel_bottombar_height;
	
	//xo
	public String LastPlanName = "LastPlanName";
	public String LastMdFn = "LastMdFn";
	/**
	 var panel=document.createElement("div");
	 panel.style="position:absolute;top:0px;left:0px;width:100%;height:100%;background-color:rgba(218,218,218,0.5);z-index:1000;text-align:center;";
	 panel.class="_PDict";
	 panel.id="_PDict_info_panel";
	 var info=document.createElement("p");
	 info.style="position:relative;top:10px;font-size:28px;color:#FFFFFF;";
	 info.innerText="无匹配";
	 panel.noword=!0;
	 info.noword=!0;
	 panel.onclick=function(e){document.body.removeChild(e.srcElement)};
	 panel.appendChild(info);
	 document.body.appendChild(panel);
	 */
	@Metaline()
	private static final String js_no_match="js_no_match";
	public boolean bHasDedicatedSeachGroup;
	private File fontlibs;
	public static int PreferredToolId=-1;
	private Runnable mOpenImgRunnable;
	private Drawable mActiveDrawable;
	private Drawable mRatingDrawable;
	private int CurrentDictInfoIdx;
	private int StarLevelStamp;
	private static HashMap<String, String> CrossFireHeaders = new HashMap<>();
	static {
		CrossFireHeaders.put("Content-Type", "");
		CrossFireHeaders.put("Access-Control-Allow-Origin", "*");
	}
	
	private static final WebResourceResponse emptyResponse = new WebResourceResponse("", "", null);
	private long SecordTime;
	private long SecordPime;
	private String LastMd;
	
	SplitView.PageSliderInf inf;
	public ActType thisActType;
	public int thisActMask;
	public boolean awaiting;
	Runnable postTask;
	
	public View favoriteBtn() {
		return contentUIData.browserWidget8;
	}
	
	/** -2=auto;0=false;1=true;2=foldingScreen see {@link WebViewListHandler#bMergeFrames}*/
	public final int mergeFrames() {
		int ret=opt.multiViewMode();
		return ret==1?opt.mergeUrlMore()?-2:1
				:ret;
	}
	
	public void invalidAllListsReal() {
		if(adaptermy!=null) adaptermy.notifyDataSetChanged();
		if(adaptermy2!=null) adaptermy2.notifyDataSetChanged();
		if(adaptermy3!=null) adaptermy3.notifyDataSetChanged();
		if(adaptermy4!=null) adaptermy4.notifyDataSetChanged();
		if(adaptermy5!=null) adaptermy5.notifyDataSetChanged();
	}
	
	public enum ActType{
		PlainDict
		, FloatSearch
		, MultiShare
	}
	protected boolean lv_matched;
	private Animation CTANIMA;
	public ViewGroup collectFavoriteView;
	protected boolean lastBackBtnAct;
	
	public boolean checkWebSelection() {
		WebViewmy wv = null;
		boolean doCheck = false;
		if(wordPopup.popupContentView!=null && wordPopup.mWebView.bIsActionMenuShown) {
			wv = wordPopup.mWebView;
			doCheck = opt.getUseBackKeyClearWebViewFocus();
		} else {
			View view = getCurrentFocus();
			if(view != null) {
				if (view.getId()==R.id.webviewmy) {
					wv = ((WebViewmy)getCurrentFocus());
					if (wv!=null && wv.bIsActionMenuShown) doCheck = opt.getUseBackKeyClearWebViewFocus();
				}
				else if (view instanceof TextView) {
					TextView tv = ((TextView) view);
					if (tv.hasSelection()) {
						tv.clearFocus();
						return true;
					}
				}
			}
		}
		if(doCheck) {
			wv.clearFocus();
			if(wv.bIsActionMenuShown) {
				wv.evaluateJavascript("getSelection().collapseToStart()", null);
			}
			return true;
		}
		return false;
	}

	public HashMap<String, BookPresenter> mdict_cache = new HashMap<>();
	@Metaline
	private static final String PDFPage="PAGE";
	public resultRecorderCombined recCom/* = EmptyResult*/; //todo
	protected boolean forbidVolumeAjustmentsForTextRead;
	private ColoredHighLightSpan timeHLSpan;
	private static final float[] TTS_LEVLES_SPEED = new float[]{0.25f, 0.75f, 1f, 1.25f, 1.75f, 2f, 2.5f, 2.75f, 4f};
	private int TTSSpeed = 2;
	private int TTSPitch = 2;
	private float TTSVolume = 1.f;
	private ViewGroup TTSController_controlBar;
	private WebViewmy mCurrentReadContext;
	public String fakedExp;

	public void jump(int pos, BookPresenter md) {

	}

	public int scale(float value) {
		return scale(dm.widthPixels,dm.heightPixels, value);
	}
	public static int scale(int displayWidth, int displayHeight, float pxValue) {
		float scale = 1;
		try {
			float scaleWidth = (float) displayWidth / 720;
			float scaleHeight = (float) displayHeight / 1280;
			scale = Math.min(scaleWidth, scaleHeight);
		} catch (Exception ignored) {
		}
		return Math.round(pxValue * scale * 0.5f);
	}
	
	public boolean switch_Dict(int i, boolean invalidate, boolean putName, AcrossBoundaryContext prvNxtABC) {
		boolean prvNxt = prvNxtABC!=null;
		int size=loadManager.md_size;
		if (bIsFirstLaunch) {
			bIsFirstLaunch = false;
		}
		BookPresenter result = EmptyBook;
		if(size>0) {
			if(i<0||i>=size) {
				if(prvNxt) {
					boolean rejected=false;
					String msg = "没有更多了";
					if(++prvNxtABC.沃壳积>=loadManager.md_size) {
						msg = "真的"+msg;
						rejected=true;
					} else {
						if(i<0) {
							msg = "⬆ "+msg;
							i=size-1;
						}
						else {
							msg = msg+" ⬇";
							i=0;
						}
						if(prvNxtABC.collide()) { // pass
							CMN.debug("collide");
						} else { // snack and return
							rejected=true;
						}
					}
					if(rejected) {
						prvNxtABC.dump();
						showTopSnack(null, msg, 0.75f, -1, Gravity.CENTER, 0);
						return true;
					}
				}
				else {
					if(i<0) i=0;
					else i=size-1;
				}
			}
			boolean bShowErr=bShowLoadErr;
			bShowLoadErr=bShowErr&&!prvNxt;
			result = loadManager.md_get(dictPicker.adapter_idx=i);
			bShowLoadErr=bShowErr;
			
			if (CurrentViewPage == 1) {
				currentDictionary = result;
				if (adaptermy != null) {
					adaptermy.setPresenter(result);
					if (invalidate) {
						adaptermy.notifyDataSetChanged();
						postPutName(550);
						if (result != EmptyBook) {
							if (TextUtils.getTrimmedLength(etSearch.getText()) > 0
									&& (dictPicker.autoSchPDict()
									|| this instanceof FloatSearchActivity
									|| dictPicker.underlined != null && dictPicker.underlined.contains(currentDictionary.getId()))
							) {
								//CMN.Log("auto_search!......");
								lv_matched = false;
								if (prvNxt && opt.getDimScrollbarForPrvNxt()) {
									ViewUtils.dimScrollbar(lv, false);
								}
								boolean tmp = isCombinedSearching;
								isCombinedSearching = false;
								tw1.onTextChanged(etSearch.getText(), -1, -1, -100);
								isCombinedSearching = tmp;
								//lv.setFastScrollEnabled(true);
								if (prvNxt && opt.getPrvNxtDictSkipNoMatch()) {
									return lv_matched;
								}
							} else {
								//lv.setSelection(currentDictionary.lvPos);
								lv.setSelectionFromTop(result.lvPos, result.lvPosOff);
							}
						} else if (prvNxt) {
							return false;
						}
					}
				}
			}
			else {
				resultRecorderScattered results = (resultRecorderScattered) (CurrentViewPage == 0 ? adaptermy3 : adaptermy4).results;
				HashSet<Long> booksSet = results.booksSet;
				if (booksSet!=null && booksSet.contains(result.getId())) {
					lv = CurrentViewPage == 0 ? mlv1 : mlv2;
					lv.setSelectionFromTop(results.findFirstBookPos(result.getId()), 0);
				}
			}
			
			boolean showBuildIndex=false;
			if(result==EmptyBook && EmptyBook.placeHolder instanceof PlaceHolder) {
				if(((PlaceHolder) EmptyBook.placeHolder).NeedsBuildIndex()) {
					AddIndexingBookIdx(0, i);
					showBuildIndex=true;
				}
			}
			showBuildIndexInterface(showBuildIndex);
		}
		return true;
	}
	
	public void AddIndexingBookIdx(int position, int idx) {
		for (int j = 0; j < IndexingBooks.size(); j++) {
			if((int)(long)IndexingBooks.get(j)==idx) {
				IndexingBooks.remove(j);
				break;
			}
		}
		if(position==-1)
			position=IndexingBooks.size();
		IndexingBooks.add(position, (0x1L<<32)|idx);
	}
	
	ArrayList<Long> IndexingBooks = new ArrayList();
	WeakReference<BuildIndexInterface> buildIndexPane = EmptyRef;
	
	protected void showBuildIndexInterface(boolean showBuildIndex) {
		BuildIndexInterface buildIndex = buildIndexPane.get();
		if(showBuildIndex) {
			if(lvHeaderView==null) {
				lvHeaderView = new FrameLayout(this);
				lvHeaderView.setLayoutParams(new LayoutParams(-1, -2));
			}
			ViewUtils.addViewToParent(lvHeaderView, (ViewGroup)mlv.getParent(), 0);
			
			if(buildIndex==null) {
				buildIndex = new BuildIndexInterface(this, IndexingBooks);
				buildIndexPane = new WeakReference<>(buildIndex);
			}
			ViewUtils.addViewToParent(buildIndex.buildIndexLayout, lvHeaderView);
			//double rootHeight = root.getHeight();
			//if(rootHeight==0) rootHeight=dm.heightPixels*5.8/6;
			buildIndex.buildIndexLayout.getLayoutParams().height = -1;
			lvHeaderView.getLayoutParams().height = -1;
			buildIndex.notifyDataSetChanged();
			//buildIndex.buildIndexLayout.getLayoutParams().height = (int) Math.max(root.getHeight()*4.5/6, GlobalOptions.density*50);
		} else if(buildIndex!=null && ViewUtils.removeIfParentBeOrNotBe(buildIndex.buildIndexLayout, lvHeaderView, true)) {
			lvHeaderView.getLayoutParams().height = -2;
		}
	}
	
	public void switchToSearchModeDelta(int i) {
	
	}
	
	private void md_set_firstFlag_internal(CharSequence name, long val) {
		byte[] data = BookProjects_getNonNull(name);
		int pad = 8*4+3+ConfigExtra;
		if(data.length>pad+8){
			IU.writeLong(data, pad, val);
		}
		if (prepareHistoryCon().testDBV2) {
			BookPresenter.putBookOptions(this, prepareHistoryCon(), -1, data, null, name.toString());
		}
	}
	
	private long md_get_firstFlag_internal(CharSequence name) {
		byte[] data = BookPresenter.getBookOptions(this, prepareHistoryCon(), -1, null, name.toString());
		int pad = 8*4+3+ConfigExtra;
//		CMN.Log("md_get_firstFlag name", name, data );
//		if(name.equals("chinese_YGYL_2012.mdd")) {
//			CMN.Log("md_get_firstFlag chinese_YGYL_2012.mdd", data , data==null?-1:PDICMainAppOptions.getDFFStarLevel(IU.readLong(data, pad)));
//		}
		if(data!=null && data.length>pad+8){
			return IU.readLong(data, pad);
		}
		return 0;
	}
	
	private byte[] BookProjects_getNonNull(CharSequence name) {
		byte[] data = BookPresenter.getBookOptions(this, prepareHistoryCon(), -1, null, name.toString());
		if(data==null) {
			data = new byte[ConfigSize + ConfigExtra];
			if (!prepareHistoryCon().testDBV2) {
				((AgentApplication)getApplicationContext()).BookProjects.put(name.toString(), data);
			}
		}
		return data;
	}
	

	public String getSearchTerm(){
		return etSearch.getText().toString();
	}

	public final EditText getEdit(){
		return etSearch;
	}

	public void setSearchTerm(String text){
		etSearch.setText(text);
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		onActionModeStarted(mode, null);
	}
	
	public void onActionModeStarted(ActionMode mode, Dialog dlg) {
		View v = dlg != null && dlg.isShowing() ? dlg.getCurrentFocus() : getCurrentFocus();
		CMN.debug("-->onActionModeStarted", v);
		Menu menu;
		if(v instanceof WebViewmy && Build.VERSION.SDK_INT<=Build.VERSION_CODES.M) {
			mode.setTitle(null);
			mode.setSubtitle(null);
			WebViewmy wv = ((WebViewmy) v);

			menu = mode.getMenu();

			String websearch = null;
			int websearch_id = Resources.getSystem().getIdentifier("websearch", "string", "android");
			if (websearch_id != 0)
				websearch = getResources().getString(websearch_id);

			String copy = getResources().getString(android.R.string.copy);
			int findCount = 2;
			MenuItem item1 = null;
			MenuItem item2 = null;
			MenuItem item;
			for (int i = 0; i < menu.size(); i++) {
				item = menu.getItem(i);
				String title = item.getTitle().toString();
				if (title.equals(copy)) {
					item1 = item;
					findCount--;
				} else if (title.equalsIgnoreCase(websearch)) {
					item2 = item;
					findCount--;
				}
				if (findCount == 0) break;
			}
			menu.clear();
			int ToolsOrder = 0;
			int af=MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT;
			if (item1 != null) {
				item = menu.add(0, item1.getItemId(), ++ToolsOrder, item1.getTitle()).setOnMenuItemClickListener(wv)
				.setIcon(item1.getIcon())
				.setShowAsActionFlags(af);
			}
			if (item2 != null) {
				item = menu.add(0, item2.getItemId(), ++ToolsOrder, item2.getTitle()).setOnMenuItemClickListener(wv)
				.setIcon(item2.getIcon())
				.setShowAsActionFlags(af);
			}
			Drawable hld = getResources().getDrawable(R.drawable.round_corner_dot);
			hld.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
			menu.add(0, R.id.toolbar_action0, ++ToolsOrder, "高亮").setOnMenuItemClickListener(wv).setShowAsActionFlags(af).setIcon(hld);
			menu.add(0, R.id.toolbar_action1, ++ToolsOrder, R.string.tools).setOnMenuItemClickListener(wv).setShowAsActionFlags(af).setIcon(R.drawable.ic_tune_black_24dp);
			menu.add(0, R.id.toolbar_action3, ++ToolsOrder, "TTS").setOnMenuItemClickListener(wv).setShowAsActionFlags(af).setIcon(R.drawable.voice_ic_big);
		}

		super.onActionModeStarted(mode);

		if(v instanceof TextView){
			menu = mode.getMenu();
			MenuItem item = menu.add(0, R.id.text_tools, 1000, R.string.tools)
					.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
					.setIcon(R.drawable.ic_tune_black_24dp);
			item.setOnMenuItemClickListener(this);
			if(ucc!=null && v==ucc.mTextView) {
				ucc.bNeedClearTextSelection = true;
			}
			for (int i = 0; i < menu.size(); i++) {
				MenuItem itemI = menu.getItem(i);
				Intent intent = itemI.getIntent();
				if(intent!=null && intent.getComponent()!=null){
					//CMN.Log(intent.getComponent().getPackageName());
					if("com.knziha.plod.plaindict".equals(intent.getComponent().getPackageName())){
						itemI.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
					}
				}
			}
			if(mFeetHeightScalerRunnable==null){
				mFeetHeightScalerRunnable = new Runnable() {
					@Override
					public void run() {
						List<View> views = getWindowManagerViews();
						for(View vI:views){
							if(vI.getClass().toString().endsWith("PopupDecorView")){
								if(vI instanceof ViewGroup){
									ViewGroup vg = (ViewGroup) vI;
									if(vg.getChildCount()>0
											&& vg.getChildAt(0).getClass().toString().endsWith("PopupBackgroundView")){
										boolean proceed=true;
										View vvI = ((ViewGroup)vg.getChildAt(0)).getChildAt(0);
										if(vvI instanceof LinearLayout){
											vg = (ViewGroup) vvI;
											vvI = vg.getChildAt(0);
											if(vvI instanceof RelativeLayout){
												proceed=false;
												vvI.getLocationOnScreen(LocationFetcher);
												mActionModeHeight = LocationFetcher[1];
			
											}
										}
										if(proceed){
											LayoutParams lp = vI.getLayoutParams();
											if(lp instanceof WindowManager.LayoutParams){
												mActionModeHeight = ((WindowManager.LayoutParams)lp).y;
											}
										}
									}
			
								}
							}
						}
					}
				};
			}
			root.postDelayed(mFeetHeightScalerRunnable, 350);
		}
		//if(menu!=null)
	}

	@Nullable
	@Override
	public View getCurrentFocus() {
		if(PeruseViewAttached())
			return peruseView.mDialog.getCurrentFocus();
		return super.getCurrentFocus();
	}

	public void handleTextTools() {
		View v = getCurrentFocus();
		if(v instanceof TextView){
			TextView tv = ((TextView) v);
			getVtk().setInvoker(null, null, tv, null);
			getVtk().onClick(tv);
		}
	}

	/*
	* 多维分享 ( MDCCSP Handling A Share Intent )
	*  {p:程序包名 m:活动名称 a:举措名称 t:MIME类型 k1:字段1键名 v1:字段1键值…}x8
	*  7 8 9 10   11 12 13 14
	* */
	void handleIntentShare(String conent, ArrayList<String> data) {
		try {
			String action  = data.get(2);
			if(action==null) action = Intent.ACTION_VIEW;
			Intent intent = new Intent(action);
			if(data.get(0)!=null && data.get(1)!=null)//optional
				intent.setClassName(data.get(0), data.get(1));
			else if(data.get(0)!=null)//optional
				intent.setPackage(data.get(0));

			//intent.setAction(action);
			String mime = data.get(3);
			if(mime!=null)
				intent.setType(mime);
			intent.addCategory(Intent.CATEGORY_DEFAULT);

			boolean bCreateChooser = false;
			boolean bMatchDefault = false;
			boolean bQueryChooser = false;
			boolean setFlags = false;
			int mFlags = 0;
			String val;
			for (int i = 4; i < data.size()-1; i+=2) {
				val = data.get(i);
				if(val!=null){
					String content = data.get(i+1);
					content = content==null?conent:content.replace("%s", conent);
					if(val.equals("_data")){
						intent.setData(Uri.parse(content));
					}
					else if(val.equals("_chooser")){
						String[] arr = content.split("/");
						for(String arrI:arr){
							if(arrI.length()==1){
								switch (Character.toLowerCase(arrI.charAt(0))){
									case 'c':
										bCreateChooser=true;
									break;
									case 'q':
										bQueryChooser=true;
									break;
									case 'd':
										bMatchDefault=true;
									break;
								}
							}
						}
					}
					else if(val.equals("_flags")){
						String[] arr = content.split("/");
						for(String arrI:arr){
							switch (arrI){
								case "0":
									setFlags=true;
								break;
								case "n":
									mFlags|=Intent.FLAG_ACTIVITY_NEW_TASK;
								break;
								case "t":
									mFlags|=Intent.FLAG_ACTIVITY_SINGLE_TOP;
								break;
								case "prev":
									mFlags|=Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
								break;
								case "re-ord":
									mFlags|=Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
								break;
								case "clr":
									mFlags|=Intent.FLAG_ACTIVITY_CLEAR_TASK;
								break;
							}
						}
					}
					else {
						intent.putExtra(val, content);
					}
				}
			}

			if(setFlags)
				intent.setFlags(mFlags);
			else if(mFlags!=0){
				intent.addFlags(mFlags);
			}

			int intentFalgs = 0;
			if(bCreateChooser) intentFalgs|=1;
			if(bQueryChooser) intentFalgs|=1<<1;
			if(bMatchDefault) intentFalgs|=1<<2;

			if(intent.hasExtra("ClipData") && getVtk().mWebView!=null){
				int finalIntentFalgs = intentFalgs;
				getVtk().mWebView.evaluateJavascript(WebViewmy.CollectHtml, word -> {
					if (word.length() > 2) {
						word = MakeCompatibleHtmlWord(word);
						ClipData cd = ClipData.newHtmlText("HTML", Html.fromHtml(word).toString(), word);
						intent.setClipData(cd);
						handleStartIntent(intent, finalIntentFalgs);
					}
				});
			} else {
				handleStartIntent(intent, intentFalgs);
			}
		} catch (Exception e) {
			showT("启动失败 "+e);
			CMN.debug(e);
		}
	}

	public String MakeCompatibleHtmlWord(String word) {
		word = StringEscapeUtils./*say:eat kit-kat!*/unescapeJava(word.substring(1, word.length() - 1)/*say:eat ice-cream!*/);
		Pattern p = Pattern.compile(" rgb\\(([0-9]*), ([0-9]*), ([0-9]*)\\)");
		Matcher m = p.matcher(word);
		StringBuffer sb=null;
		while(m.find()/*say:eat Marsh-mallow!*/){
			if(sb==null) sb = new StringBuffer(word.length());
			m.appendReplacement(sb, "#");
			for (int i = 1; i <= 3; i++)
				sb.append(String.format("%02x", IU.parsint(m.group(i))));
		}
		if(sb!=null/*say:eat Color-Palette!*/){
			m.appendTail(sb);
			word = sb.toString();
		}
		/*say:User's finger saved!!! balabala!!!*/
		return word;
	}

	private void handleStartIntent(Intent intent, int intentFalgs) {
		if(intent.hasExtra(Intent.EXTRA_HTML_TEXT)&&!intent.hasExtra(Intent.EXTRA_TEXT)){
			intent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_HTML_TEXT));
			intent.removeExtra(Intent.EXTRA_HTML_TEXT);
		}
		if((intentFalgs&0x1)!=0) {
			if((intentFalgs&0x2)!=0) {
				/* bypass the system default */
				PackageManager pm = getPackageManager();
				List<Intent> candidates = new ArrayList<>();
				List<ResolveInfo> activities = pm.queryIntentActivities(intent, (intentFalgs&0x4)!=0?PackageManager.MATCH_DEFAULT_ONLY:PackageManager.MATCH_ALL);
				for (ResolveInfo currentInfo : activities) {
					Intent candidate = new Intent(intent);
					String packageName = currentInfo.activityInfo.packageName;
					candidate.setPackage(packageName);
					candidate.setClassName(packageName, currentInfo.activityInfo.name);
					candidates.add(candidate);
				}
				if (candidates.size() == 0)
					candidates.add(intent);
				Intent chooserIntent = Intent.createChooser(candidates.remove(0), "title");
				if (candidates.size() > 0)
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, candidates.toArray(new Parcelable[]{}));
				startActivity(chooserIntent);
			}
			else {
				intent = Intent.createChooser(intent, "chooser");
				startActivity(intent);
			}
		}
		else {
			startActivity(intent);
		}
	}

	void HandleLocateTextInPage(String content) {
		if(PeruseViewAttached()) {
			peruseView.prepareInPageSearch(content, true);
		} else {
			prepareInPageSearch(content, true);
		}
	}

	void HandleSearch(String content) {
		if(PeruseViewAttached())
			peruseView.etSearch.setText(content);
		else
			etSearch.setText(content);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   /*if(opt.FirstFlag()==null)
		   showT("first flag");
       ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
       	  List<RunningTaskInfo> ri = manager.getRunningTasks(100);
       	  int cc=0;
       	  for(int i=0;i<ri.size();i++)
       		  cc+=ri.get(i).numActivities;
       	  showT(""+cc);
       	  */
		CMN.mid = SU.mid = Thread.currentThread().getId();
	    //CMN.Log("instanceCount", CMN.instanceCount);
		thisActMask = 1 << thisActType.ordinal();
		super.onCreate(savedInstanceState);
		ViewGroup tmp =  new ScrollViewmy(this);
		if(shunt) return;
		wordPopup.opt = opt;
		CMN.instanceCount++;
		MainStringBuilder = new StringBuilder(40960);
		WebView.setWebContentsDebuggingEnabled(PDICMainAppOptions.getEnableWebDebug());
		//ViewUtils.setWebDebug(this);
		if (BuildConfig.isDebug) {
			CMN.debug("android.os.Build.MODEL", android.os.Build.MODEL);
			CMN.debug("mid", CMN.mid, getClass());
			CMN.debug("sdk", Build.VERSION.SDK_INT);
			CMN.debug("den="+GlobalOptions.density
					, "size="+GlobalOptions.width+"x"+GlobalOptions.height
					, "realSz="+GlobalOptions.realWidth+"x"+GlobalOptions.realHeight
					, "isLarge="+GlobalOptions.isLarge
					, "isSmall="+GlobalOptions.isSmall
			);
		}
	}

	public void onAudioPause() {
		CMN.debug("onAudioPause", !AutoBrowsePaused, opt.isAudioActuallyPlaying, mThenReadEntryCount, bThenReadContent);
		if(!AutoBrowsePaused && PDICMainAppOptions.getAutoBrowsingReadSomething()){
			if(true || opt.isAudioActuallyPlaying || 0==(foreground&(1<<thisActType.ordinal()))){
				if(mThenReadEntryCount>0){
					//CMN.Log(root.post(this::performReadEntry), "posting...");
					CMN.debug("posting...3322123");
					hdl.sendEmptyMessage(3322123);
				} else {
					opt.isAudioActuallyPlaying=false;
					CMN.debug("posting...3322124");
					hdl.sendEmptyMessage(3322124);
					//root.post(this::enqueueNextAutoReadProcess);
				}
			}
		} else {
			if(bThenReadContent){
				opt.isAudioActuallyPlaying=false;
				CMN.debug("posting...3322124");
				hdl.sendEmptyMessage(3322124);
			} else {
				opt.isAudioActuallyPlaying=false;
				transitAAdjustment();
			}
		}
	}

	public void onAudioPlay() {
		CMN.debug("onAudioPlay", forbidVolumeAjustmentsForTextRead, !AutoBrowsePaused && PDICMainAppOptions.getAutoBrowsingReadSomething());
		if(!AutoBrowsePaused && PDICMainAppOptions.getAutoBrowsingReadSomething()){
			opt.isAudioActuallyPlaying=opt.isAudioPlaying=true;
			removeAAdjustment();
			return;
		}
		if(!opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed()) return;
		if(forbidVolumeAjustmentsForTextRead){
			forbidVolumeAjustmentsForTextRead =false;
		} else {
			opt.isAudioActuallyPlaying=opt.isAudioPlaying=true;
		}
		removeAAdjustment();
	}

	private final Runnable resetVolumeAdjustment = () -> opt.isAudioPlaying=false;

	public void removeAAdjustment() {
		webSingleholder.removeCallbacks(resetVolumeAdjustment);
	}

	public void transitAAdjustment() {
		if(!opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed()) return;
		removeAAdjustment();
		if(!opt.isAudioActuallyPlaying)
			webSingleholder.postDelayed(resetVolumeAdjustment, 800);
	}

	public void shuntAAdjustment() {
		opt.isAudioActuallyPlaying=opt.isAudioPlaying=false;
		removeAAdjustment();
	}
	
	public int getVisibleHeight() {
		return root.getChildAt(0).getHeight();
	}

	public void fix_dm_color() {
		//CMN.Log("fix_dm_color");
		boolean isDark = GlobalOptions.isDark;
		boolean nii=contentUIData.browserWidget12.getTag(R.id.image)==null;
		ViewGroup[] holders = new ViewGroup[]{webSingleholder, weblistHandler.getViewGroup()};
		for (ViewGroup hI : holders) {
			for (int i = 0; i < hI.getChildCount(); i++) {
				Object tag = hI.getChildAt(i).getTag();
				if(tag instanceof Integer){
					int selfAtIdx = (int) tag;
					if(selfAtIdx>=0&&selfAtIdx<loadManager.md_size) {
						BookPresenter mdTmp = loadManager.md_getAt(selfAtIdx);
						if (mdTmp!=null) {
							WebViewmy wv = mdTmp.mWebView;
							if(wv!=null) {
								wv.evaluateJavascript(isDark ? DarkModeIncantation : DeDarkModeIncantation, null);
								mdTmp.tintBackground(wv);
							}
						}
					}
				}
			}
		}
		if(peruseView !=null) {
			peruseView.refreshUIColors(MainBackground);
		}
	}

	public WordPopup wordPopup = new WordPopup(this);
	
	public void fix_pw_color() {
		bottomPlaylist.clear();
		ChooseFavorDialog.clear();
		wordPopup.refresh();
	}

	public void popupWord(final String key, BookPresenter forceStartId, int frameAt, WebViewmy wv) {
		wordPopup.popupWord(wv, key, forceStartId, frameAt);
	}
	
	public boolean DetachClickTranslator() {
		if(wordPopup.isVisible()) {
			wordPopup.dismiss();
			return true;
		}
		return false;
	}

	public void postDetachClickTranslator() {
		root.post(this::DetachClickTranslator);
	}

	public @Nullable String ReRouteKey(String key, boolean bNullable){
		int size = loadManager.lazyMan.filterCount;
		if (size>0) {
			//CMN.Log("ReRouteKey ??" , key);
			for (int i = 0; i < size; i++) {
				BookPresenter mdTmp = loadManager.getFilterAt(i);
				if(mdTmp!=EmptyBook)
				try {
					Object rerouteTarget = mdTmp.bookImpl.ReRoute(key);
					//CMN.Log(key, " >> " , rerouteTarget, mdTmp.getName(), mdTmp.getIsDedicatedFilter());
					if (rerouteTarget instanceof String)
						return (String) rerouteTarget;
				} catch (IOException ignored) { }
			}
		}
		return bNullable?null:key;
	}

	public void initPhotoViewPager() {
		if(PhotoPager==null) {
			PhotoPagerHolder = (ViewGroup) getLayoutInflater().inflate(R.layout.photo_pager_view, root, false);
			ViewPager mPhotoPager = PhotoPagerHolder.findViewById(R.id.photo_viewpager);
			PhotoCover = PhotoPagerHolder.findViewById(R.id.photo_background);
			mPhotoPager.setOffscreenPageLimit(10);
			mPhotoPager.setPageTransformer(false, new DepthPageTransformer(), View.LAYER_TYPE_NONE);
			mPhotoPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
			mPhotoPager.setAdapter(PhotoAdapter = new PagerAdapter() {
				@Override
				public int getCount() {
					return PhotoUrls.size();
				}

				@Override
				public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
					return view == object;
				}

				@NonNull
				@Override
				public Object instantiateItem(@NonNull ViewGroup container, int position) {
//					PhotoView pv;
//					if (mViewCache.size() > 0) {
//						pv = mViewCache.removeFirst();
//					} else {
//						pv = new PhotoView(container.getContext());
//						pv.setScaleType(ImageView.ScaleType.CENTER_CROP);
////					pv.setOnClickListener(PhotoViewActivity.this);
////					pv.setOnLongClickListener(PhotoViewActivity.this);
//					}
//					if (pv.getParent() != null)
//						((ViewGroup) pv.getParent()).removeView(pv);
//					container.addView(pv);
//					pv.setTag(position);
//					pv.IBC = currentDictionary.IBC;
//					String key = PhotoUrls.get(position);
//					try {
//						Glide.with(MainActivityUIBase.this)
//								.asBitmap()
//								.load(key.startsWith("/pdfimg/")?new PdfPic(key, pdfiumCore, cached_pdf_docs):new MddPic(currentDictionary.getMdd(), key))
//								.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//								.diskCacheStrategy(DiskCacheStrategy.NONE)
//								.listener(new RequestListener<Bitmap>() {
//									@Override
//									public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//										ImageView medium_thumbnail = ((ImageViewTarget<?>) target).getView();
//										medium_thumbnail.setImageResource(R.drawable.load_error);
//										return true;
//									}
//									@Override
//									public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//										if(PhotoCover.getTag()==null){
//											if(resource.getByteCount()< MaxBitmapRam){
//												PhotoCover.setImageBitmap(blurByGauss(resource, 50));
//											}else{
//												PhotoCover.setBackgroundColor(MainBackground);
//											}
//											PhotoCover.setTag(false);
//										}
//										return false;
//									}
//								}).into(pv);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					return pv;
					return null;
				}


				@Override
				public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
					//container.removeView((View) object);
					//mViewCache.add((PhotoView) object);
				}
			});
			mPhotoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

				}

				@Override
				public void onPageSelected(int position) {
					//curPosition = position;
				}

				@Override
				public void onPageScrollStateChanged(int state) {

				}
			});
			PhotoPager=mPhotoPager;
		}
		if(PhotoPagerHolder.getParent()==null)
			root.addView(PhotoPagerHolder, 1, contentview.getLayoutParams());
		contentUIData.webcontentlister.canClickThrough=true;
		checkW10(PDICMainAppOptions.getIsoImgClickThrough());
	}

	public void guaranteeBackground(int globalPageBackground) {
		contentUIData.webcontentlister.canClickThrough=false;
		if(webSingleholder.getTag(R.id.image)!=null || globalPageBackground!=GlobalPageBackground){
			webSingleholder.setTag(R.id.image,null);
			webSingleholder.setBackgroundColor(globalPageBackground);
		}
		if(contentUIData.browserWidget12.getTag(R.id.image)!=null){
			contentUIData.browserWidget12.setTag(R.id.image, null);
			contentUIData.browserWidget12.setImageResource(R.drawable.voice_ic);
		}
	}

	void toggleClickThrough() {
		checkW10(PDICMainAppOptions.setIsoImgClickThrough(!PDICMainAppOptions.getIsoImgClickThrough()));
	}

	private void checkW10(boolean clickThrough) {
		Integer target = clickThrough?R.drawable.ic_image_black_24dp:R.drawable.ic_comment_black_24dp;
		if(contentUIData.browserWidget12.getTag(R.id.image)!=target){
			contentUIData.browserWidget12.setTag(R.id.image, target);
			contentUIData.browserWidget12.setImageResource(target);
		}
	}

	public void checkW10_full() {
		Integer target = R.drawable.ic_fullscreen_black_96dp;
		if(contentUIData.browserWidget12.getTag(R.id.image)!=target){
			contentUIData.browserWidget12.setTag(R.id.image, target);
			contentUIData.browserWidget12.setImageResource(target);
		}
	}

	public void notifyDictionaryDatabaseChanged(BookPresenter mdx) {
		if(currentDictionary==mdx && adaptermy!=null){
			lv.post(new Runnable() {
				@Override
				public void run() {
					adaptermy.notifyDataSetChanged();
				}
			});
		}
	}

	public boolean isCombinedViewAvtive() {
		return ActivedAdapter==adaptermy2;
	}

	public void TransientIntoSingleExplanation() {
		webSingleholder.setVisibility(View.VISIBLE);
		weblistHandler.shutUp();
		if(contentUIData.navBtns.getVisibility()==View.VISIBLE) {
			contentUIData.navBtns.setVisibility(View.GONE);
		}
	}

	/** Batch save all */
	public void  dumpViewStates() {
//		AgentApplication app = ((AgentApplication) getApplication());
//		HashMap<CharSequence, byte[]> mdict_projects = app.BookProjects;
//		try {
//			File SpecificationFile = opt.SpecificationFile;
//			FileOutputStream fout = new FileOutputStream(SpecificationFile);
//			ReusableByteOutputStream bos = new ReusableByteOutputStream(SpecificationBlockSize);
//			byte[] source = bos.getBytes();
//			int count=0;
//			boolean init=false;
//			byte[] name, data;
//			for(CharSequence kI:mdict_projects.keySet()){
//				//CMN.Log("写入……kI=", kI);
//				if(!init){
//					name = Integer.toString(ConfigSize).getBytes(StandardCharsets.UTF_8);
//					data = "/\n".getBytes();
//					bos.write(name);
//					bos.write(data);
//					count+=name.length+data.length;
//					init = true;
//				}
//				name = kI.toString().getBytes(StandardCharsets.UTF_8);
//				data = mdict_projects.get(kI);
//				if(data!=null) {
//					if(data.length-ConfigExtra< ConfigSize){
//						byte[] newData = new byte[ConfigSize + ConfigExtra];
//						System.arraycopy(data, 0, newData, 0, data.length);
//						mdict_projects.put(kI, data = newData);
//					}
//					int ReadOffset=count+name.length+targetCount;
//					int size = name.length + data.length + 2 - ConfigExtra;
//					if (count + size > SpecificationBlockSize) { //溢出写入
//						Arrays.fill(source, count, SpecificationBlockSize, (byte) 0);
//						fout.write(source);
//						fout.flush();
//						bos.reset();
//						count = 0;
//					}
//					bos.write(name);
//					bos.write("/".getBytes());
//					bos.write(data, ConfigExtra, data.length-ConfigExtra);
//					bos.write("\n".getBytes());
//					for (int k = 0; k < 4; k++) {
//						data[k] = (byte) (ReadOffset>>(k*8)&0xff);
//					}
//					data[5]=0;
//					count += size;
//				}
//			}
//			if(count>0 && count<=SpecificationBlockSize) { //正常写入
//				fout.write(source ,0, count);
//				fout.flush();
//				fout.close();
//			}
//			mConfigSize=ConfigSize;
//			CMN.LastConfigReadTime = System.currentTimeMillis();
//		}
//		catch (Exception e){
//			CMN.Log(e);
//		}
	}

	/** Save just one. */
	public boolean SolveOneUIProject(CharSequence fname) {
//		CMN.Log("solveOnUIProject");
//		try {
//			byte[] name = fname.toString().getBytes(StandardCharsets.UTF_8);
//			byte[] data = mBookProjects.get(fname);
//			if(data!=null){
//				File SpecificationFile = opt.SpecificationFile;
//				if(SpecificationFile.length()==0) return false;
//				int RealOffset=0;
//				for (int k = 0; k < 4; k++)
//					RealOffset |= (data[k]&0xff)<<(k*8);
//				if(RealOffset<1){ //append
//					CMN.Log("追加成功???");
//					FileOutputStream fout = new FileOutputStream(SpecificationFile, true);
//					int size = name.length + data.length + 2;
//					long count = SpecificationFile.length();
//					long left = SpecificationBlockSize - count%SpecificationBlockSize;
//					if(left<size)//填充0
//						for (int i = 0; i < left; i++) fout.write(0);
//					fout.write(name);
//					fout.write(target);
//					count += name.length + targetCount;
//					for (int k = 0; k < 4; k++)
//						data[k] = (byte) (count>>(k*8)&0xff);
//					fout.write(data, ConfigExtra, data.length-ConfigExtra);
//					fout.write(separator);
//					CMN.Log("追加成功！");
//					CMN.LastConfigReadTime = System.currentTimeMillis();
//					return true;
//				}
//				else {  //in place re-write
//					RandomAccessFile raf = new RandomAccessFile(SpecificationFile, "rw");
//					raf.seek(RealOffset-1);
//					if(raf.read()==(target[targetCount-1]&0xff)){//sanity check A
//						raf.write(data, ConfigExtra, data.length-ConfigExtra);
//						CMN.Log("写入成功???");
//						if(raf.read()==(separator[0]&0xff)){//sanity check B
//							CMN.Log("写入成功!!!");
//							CMN.LastConfigReadTime = System.currentTimeMillis();
//							return true;
//						}
//					}
//					raf.close();
//				}
//			}
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		CMN.Log("override !!!");
		return false;
	}
	
	private ObjectAnimator mAutoReadProgressAnimator;
	private View mAutoReadProgressView;
	boolean animateProgress = true;
	boolean bRequestingAutoReading;
	public void startAutoReadProcess(){
		CMN.debug("发起 startAutoReadProcess");
		bThenReadContent=false;
		bRequestingAutoReading=true;
		//animation delay
		if(getAnimateProgress()){
			if(mAutoReadProgressView==null){
				mAutoReadProgressView = LayoutInflater.from(this).inflate(R.layout.progressbar_item, contentview, false);
			}
			LayerDrawable d = (LayerDrawable) mAutoReadProgressView.getBackground();
			d.setLevel(0);
			if(mAutoReadProgressAnimator!=null) {
				mAutoReadProgressAnimator.pause();
				mAutoReadProgressAnimator.setIntValues(0, 10000);
			} else {
				mAutoReadProgressAnimator = ObjectAnimator.ofInt(d,"level", 0, 10000);
				mAutoReadProgressAnimator.setDuration(AutoBrowseTimeOut);
				mAutoReadProgressAnimator.addListener(new ViewUtils.BaseAnimatorListener() {
					@Override public void onAnimationEnd(Animator animation) {
						performAutoReadProcess();
					}
				});
			}
			if(mAutoReadProgressView.getParent()==null){
				d.findDrawableByLayerId(android.R.id.background).setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
				d.findDrawableByLayerId(android.R.id.progress).setColorFilter(ColorUtils.blendARGB(MainBackground, Color.BLACK, 0.28f), PorterDuff.Mode.SRC_IN);
			}
			if(PeruseViewAttached()) {
				peruseView.prepareProgressBar(mAutoReadProgressView);
			} else {
				weblistHandler.contentviewAddView(mAutoReadProgressView, 0);//111
			}
			mAutoReadProgressAnimator.start();
		}
		else {
			hdl.sendEmptyMessage(332211123);
		}
	}
	
	private WindowManager mWindowManager;
	private View mOnePixelView;
	private static final int WINDOW_SIZE = 100;
	private static final int WINDOW_COLOR = Color.RED;
	
	private void addOnePixelView() {
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else {
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}
		params.format = PixelFormat.RGBA_8888;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		params.gravity = Gravity.START | Gravity.TOP;
		params.x = 0;
		params.y = 0;
		params.width = WINDOW_SIZE;
		params.height = WINDOW_SIZE;
		
		mOnePixelView = new View(this);
		mOnePixelView.setBackgroundColor(WINDOW_COLOR);
		mWindowManager.addView(mOnePixelView, params);
	}
	
	void do_test_project_Test_Background_Loop() {
		addOnePixelView();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					//CMN.Log("fatal log "+System.currentTimeMillis());
					try {
						hdl.sendEmptyMessage(111222333);
						Thread.sleep(600);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		if (!FcfrtAppBhUtils.isIgnoringBatteryOptimizations(this)){
			FcfrtAppBhUtils.requestIgnoreBatteryOptimizations(this);
		}
	}
	
	public void stopAutoReadProcess(){
		showT("已停止自动浏览");
		bThenReadContent=
		bRequestingAutoReading=false;
		AutoBrowsePaused=true;
		hdl.removeMessages(332211);
		if(mAutoReadProgressAnimator!=null){
			mAutoReadProgressAnimator.pause();
			ViewUtils.removeIfParentBeOrNotBe(mAutoReadProgressView, null, false);
		}
	}

	public void interruptAutoReadProcess(boolean forceClose){
		hdl.removeMessages(332211);
		if(TTSController_engine!=null){
			if(forceClose){
				TTSController_engine.setOnUtteranceProgressListener(null);
				TTSController_engine.stop();
			}
		}
		if(mAutoReadProgressAnimator!=null && mAutoReadProgressAnimator.isRunning()){
			mAutoReadProgressAnimator.pause();
			try {
				mAutoReadProgressView.getBackground().setLevel(0);
			} catch (Exception ignored) {  }
		}
	}

	long lastreadtime;
	public void performAutoReadProcess() {
		CMN.debug("自动读下一个", opt.isAudioActuallyPlaying, bRequestingAutoReading, AutoBrowsePaused);
		long now = System.currentTimeMillis();
		if(now-lastreadtime<AutoBrowseTimeOut){
			CMN.debug("abuse detected!!!!");
			return;
		}
		lastreadtime = now;
		if(bRequestingAutoReading){
			AutoBrowsePaused=false;
			AutoBrowseCount=0;
			bRequestingAutoReading=false;

		}
		boolean ar = PDICMainAppOptions.getAutoBrowsingReadSomething();
		if(!(ar&&opt.isAudioActuallyPlaying))
		if(ActivedAdapter!=null){
			if(!AutoBrowsePaused){
				++AutoBrowseCount;
				interruptAutoReadProcess(true);
				if(mAutoReadProgressView!=null) mAutoReadProgressView.getBackground().setLevel(0);
				ActivedAdapter.onItemClick(ActivedAdapter.lastClickedPos+1);
				if(!ar){
					enqueueNextAutoReadProcess();
				}
			}
		}
	}

	void enqueueNextAutoReadProcess() {
		CMN.debug("enqueueNextAutoReadProcess");
		if(bThenReadContent && pendingWebView!=null){
			performReadContent(pendingWebView);
			bThenReadContent=false;
		} else {
			if (getAnimateProgress()) {
				if(mAutoReadProgressAnimator != null){
					if (mAutoReadProgressAnimator.isPaused()) {
						mAutoReadProgressAnimator.removeAllListeners();
						mAutoReadProgressAnimator.setTarget(null);
						mAutoReadProgressAnimator.cancel();
						mAutoReadProgressAnimator = null;
						CMN.debug("如果能重来");
						startAutoReadProcess();
					} else {
						CMN.debug("2");
						mAutoReadProgressAnimator.pause();
						mAutoReadProgressAnimator.setIntValues(0, 10000);
						mAutoReadProgressAnimator.start();
					}
				}
			} else {
				hdl.sendEmptyMessageDelayed(332211, AutoBrowseTimeOut);
			}
		}
	}
	
	private boolean getAnimateProgress() {
		return animateProgress;
	}
	
	public SparseArrayMap jnFanMap;
	public SparseArrayMap fanJnMap;
	public void ensureTSHanziSheet(PDICMainActivity.AdvancedSearchInterface SearchLayer) {
		if(jnFanMap==null) {
			try {
				SparseArrayMap _jnFanMap = new SparseArrayMap(2670);
				fanJnMap = new SparseArrayMap(2780);
				InputStream fin = getResources().getAssets().open("Kasemap.ali");
				byte[] buffer = new byte[4096];
				Charset _charset = StandardCharsets.UTF_8;
				byte[] linebreak = "\n".getBytes(_charset);
				int breakIdx;
				int now;
				int blockSize;
				while ((blockSize = fin.read(buffer)) > 0) {
					now = 0;
					while ((breakIdx = mdict.indexOf(buffer, 0, blockSize, linebreak, 0, linebreak.length, now)) > 0) {
						String val = new String(buffer, now, breakIdx - now, _charset);
						_jnFanMap.put(val.charAt(0), val);
						int len = val.length();
						for (int i = 1; i < len; i++) {
							fanJnMap.put(val.charAt(i), val);
						}
						now = breakIdx + linebreak.length;
					}
				}
				jnFanMap=_jnFanMap;
			} catch (IOException e) { CMN.debug(e); }
		}
		if (SearchLayer!=null) {
			if(jnFanMap==null) {
				PDICMainAppOptions.setEnableFanjnConversion(false);
			} else {
				SearchLayer.jnFanMap = jnFanMap;
				SearchLayer.fanJnMap = fanJnMap;
			}
		}
	}

	void closeIfNoActionView(MenuItemImpl mi) {
		if(mi!=null && !mi.isActionButton()) {
			mi.mMenu.close();
		}
	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
	
	@Override
	protected void scanSettings() {
		GlobalPageBackground = GlobalPageBackground = opt.getGlobalPageBackground();
		mdict.bGlobalUseClassicalKeycase = PDICMainAppOptions.getClassicalKeycaseStrategy();
		opt.getLastPlanName(LastPlanName);
		new File(opt.pathToDatabases().toString()).mkdirs();
		opt.CheckFileToDefaultMdlibs();
		wordPopup.loadManager = loadManager = new LoadManager(dictPicker);
	}

	public static byte[] target = "/".getBytes(StandardCharsets.UTF_8);
	public static byte[] separator = "\n".getBytes(StandardCharsets.UTF_8);
	public static int separatorCount = separator.length;
	public static int targetCount = target.length;
	public static int ConfigSize = 78;
	public static int ConfigExtra = 0; // 5
	public static int SpecificationBlockSize = 4096;
	//public static int PseudoInitCode = 31<<2;
	public static long SessionFlag;

	public int mConfigSize = 0;
	
	public static String byte2HexFormatted(byte[] arr) {
		StringBuilder str = new StringBuilder(arr.length * 2);
		for (int i = 0; i < arr.length; i++) {
			String h = Integer.toHexString(arr[i]);
			int l = h.length();
			if (l == 1) h = "0" + h;
			if (l > 2) h = h.substring(l - 2, l);
			str.append(h.toUpperCase());
			if (i < (arr.length - 1)) str.append(':');
		}
		return str.toString();
	}
	
	void setMagicNumberForHash() {
		opt.setPseudoInitCode(31);
		//showT("setMagicNumberForHash！");
	}
	
//	public native boolean testPakVal(String pakNam);
//
//	public native int getPseudoCode(int sigHash);


//	/**{android.app.ActivityThread}.sPackageManager = null
//var pm = $.getPackageManager()
//var packageName = $.getPackageName()
//var info = pm.getPackageInfo[, int](packageName, 0x40)
//var signature0 = info.signatures[0];
//var hashcode = signature0.hashCode();*/
//	@Metaline(trim=false)
//	String testVerifyCode = "";

//	/**{android.app.ActivityThread}.sPackageManager = null
//var packageName = $.getPackageName()
//var info = $.getPackageManager().getPackageInfo[, int](packageName, 0x40).signatures[0].hashCode()
//*/
//	@Metaline(trim=false)
//	String testVerifyCode = "";
	
	String testVerifyCode = "{android.app.ActivityThread}.sPackageManager=null;n=$.getPackageName();n=$.getPackageManager().getPackageInfo[,int](n,0x40).signatures[0].hashCode()";
	
	@StripMethods()
	@Override
	protected void further_loading(Bundle savedInstanceState) {
		super.further_loading(savedInstanceState);
		opt.fileToDatabases();
		BookPresenter.def_zoom=dm.density;
		BookPresenter.max_zoom=5*BookPresenter.def_zoom;
		BookPresenter.optimal100 = GlobalOptions.isLarge?150:125;
		BookPresenter.def_fontsize = opt.getDefaultFontScale(BookPresenter.optimal100);
		
		// CMN.debug("floatBtn::", PDICMainAppOptions.floatBtn(opt.SixthFlag() >> (30 + thisActType.ordinal())), PDICMainAppOptions.floatBtn(), thisActType);
		if (PDICMainAppOptions.floatBtn(opt.SixthFlag()>>(30+thisActType.ordinal()))) {
			getFloatBtn().reInitBtn(0);
		}
//		try {
//			Integer verifyCode = (Integer) ViewUtils.execSimple(testVerifyCode, null, this);
//			CMN.Log("testVerifyCode::", verifyCode);
//			ViewUtils.execSimple("p={com.knziha.plod.plaindict.PDICMainAppOptions};n=p.calcPseudoCode[int]($);p.setPseudoInitCode[int](n)", null, 1721624788);
//			CMN.Log("setPseudoInitCode::", PDICMainAppOptions.getPseudoInitCodeEu());
//		} catch (Exception e) {
//			CMN.Log("testVerifyCode::", e);
//		}
		
		setMagicNumberForHash();
		
		try {
			loadManager.EmptyBook = currentDictionary = EmptyBook = new BookPresenter(new File("empty"), this, 1);
		} catch (Exception e) {
			CMN.Log(e);
		}
		EmptySchResults = new resultRecorderDiscrete(this);
		
		File ConfigFile = opt.fileToConfig();
		
		ConfigFile.mkdirs();

		AgentApplication app = ((AgentApplication) getApplication());
		CMN.rt();
		File fontlibs = new File(opt.getFontLibPath());
		if(fontlibs.isDirectory()) {
			HashMap<String, String> fontNames = app.fontNames;
			fontlibs.listFiles(new FilenameFilter() {
				ReusableBufferedInputStream bin = null;
				@Override public boolean accept(File dir, String fName) {
					if (!fontNames.containsKey(fName))
						try {
							if (fName.regionMatches(true, fName.length() - 4, ".ttf", 0, 4)) {
								File fI = new File(dir, fName);
								FileInputStream fin = new FileInputStream(fI);
								if (bin == null) bin = new ReusableBufferedInputStream(fin, 4096);
								else bin.reset(fin);
								String name = BU.parseFontName(bin);
								if (name != null)
									fontNames.put(fName, name);
								//CMN.Log("fontName:", name, System.currentTimeMillis());
							}
						} catch (Exception e) {
							CMN.debug(e);
						}
					return false;
				}
			});
			if(fontNames.size()>0) {
				StringBuilder mFontFaces=new StringBuilder(512);
				for (Map.Entry<String, String> entry : fontNames.entrySet()) {
					mFontFaces.append("@font-face{")
							.append("font-family: '").append(entry.getValue()).append("';")
							.append("src: url('font://").append(entry.getKey()).append("');}");
				}
				fontFaces = mFontFaces.toString();
			}
			this.fontlibs=fontlibs;
		}

		// todo 删除?
		File SpecificationFile =  opt.SpecificationFile = new File(opt.pathToDatabases().append("/.spec.bin").toString());
		// CMN.rt();
		// CMN.debug(CMN.LastConfigReadTime, SpecificationFile.lastModified());
		if(SpecificationFile.exists()){ // 读取词典配置。
			if(CMN.LastConfigReadTime<SpecificationFile.lastModified()) {
				if(!(CMN.bForbidOneSpecFile = SpecificationFile.isDirectory()))
				try {
					FileInputStream fin = new FileInputStream(SpecificationFile);
					int i, j, sourceOffset, sourceCount, max, end, RealOffset, trimStart;
					byte first = target[0];
					int prevBlock = 0;
					byte[] oldData = null;
					byte[] source = new byte[SpecificationBlockSize];
					//4k对齐读取。
					ReadBlocks:
					while ((sourceCount = fin.read(source)) > 0) {
						sourceOffset = 0;
						FindItems:
						while (sourceOffset < sourceCount) {
							//CMN.Log("开查……", sourceOffset);
							/* 先查 /，前进固定长度(第一行所定义的[50 Bytes])，得遇 \n。  */
							max = sourceCount - targetCount;
							for (i = sourceOffset; i <= max; i++) {
								/* Look for first character. */
								if (source[i] != first) {
									while (++i <= max && source[i] != first) ;
								}
								/* Found first character, now look at the rest of v2 */
								if (i <= max) {
									j = i + 1;
									end = j + targetCount - 1;
									for (int k = 1; j < end && source[j] == target[k]; j++, k++) ;
									if (j == end) {
										/* Found whole string. */
										//return i - sourceOffset;
										end = i + targetCount;
										j = end + mConfigSize;
										if (j < sourceCount && source[j] == separator[0]) {
											trimStart = 0;
											while ((source[sourceOffset + 1] & 0xff) == 0) {
												++trimStart;
												++sourceOffset;
											}
											if (oldData != null) {
												oldData[4] = (byte) trimStart;
											}
											String fn = new String(source, sourceOffset, i - sourceOffset, StandardCharsets.UTF_8);
											if (mConfigSize == 0) {
												mConfigSize = IU.parseInt(fn);
												if (mConfigSize < 50) {
													break ReadBlocks;
												} else if (mConfigSize != ConfigSize) {//tofo
													//dirtyMap.add("1");
													//dirtyMap.add("2");
												}
												CMN.debug("配置大小 =", mConfigSize);
											} else {
												i += targetCount;
												RealOffset = prevBlock * SpecificationBlockSize + i;
												//CMN.Log("读取!!!", fn, RealOffset, new String(source, i - 1, 1));
												byte[] data = new byte[ConfigExtra + mConfigSize];
												for (int k = 0; k < 4; k++) {
													data[k] = (byte) (RealOffset >> (k * 8) & 0xff);
												}
												System.arraycopy(source, i, data, ConfigExtra, mConfigSize);
												oldData = data;
												String name = new File(fn).getName();
												long book_id = prepareHistoryCon().getBookID(null, name);
												if (BookPresenter.getBookOptions(this, prepareHistoryCon(), book_id, null, name)==null) {
													BookPresenter.putBookOptions(this, prepareHistoryCon(), book_id, data, null, name);
												}
											}
											end = j + separatorCount;
										}
										sourceOffset = end;
										continue FindItems;
									}
								}
							}
							break;
						}
						++prevBlock;
						if (sourceCount < SpecificationBlockSize)
							break;
					}
					fin.close();
					CMN.LastConfigReadTime = System.currentTimeMillis();
				} catch (Exception e) {
					CMN.debug(e);
				}
				if (mConfigSize < 50) {
					SpecificationFile.delete();
				}
			}
		}
		// CMN.pt(app.BookProjects.size()+"个配置读取时间");
		
//		CMN.rt();
//		historyCon = prepareHistroyCon();
//		Cursor cursor = historyCon.getDB().rawQuery("select * from book", null);
//		app.BookProjects.clear();
//		while (cursor.moveToNext()) {
//			app.BookProjects.put(cursor.getString(1), cursor.getBlob(3));
//		}
//		cursor.close();
//		CMN.pt(app.BookProjects.size()+"个配置读取时间V2");
		
		final File def = getStartupFile(ConfigFile);      //!!!原配
		final boolean retrieve_all=!def.exists();
		
		
		inf = new SplitView.PageSliderInf() {//这是底栏的动画特效
			int height;
			@Override
			public void onPreparePage(int val) {
				View IMPageCover_ = contentUIData.cover;
				LayoutParams lpp = IMPageCover_.getLayoutParams();
				if(PeruseViewAttached())
					IMPageCover_ = peruseView.contentUIData.cover;
				//showT("onPreparePage"+System.currentTimeMillis());
				height=val;
				lpp.height=val;
				IMPageCover_.setLayoutParams(lpp);
				IMPageCover_.setAlpha(1.f);
				//IMPageCover_.setImageBitmap(null);
				if(IMPageCover_.getTag()==null) {
					if(Build.VERSION.SDK_INT>23 && IMPageCover_.getForeground()!=null) {
						IMPageCover_.getForeground().setTint(MainBackground);
					}
					if(IMPageCover_.getBackground()!=null)
						IMPageCover_.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
					IMPageCover_.setTag(false);
				}
				IMPageCover_.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onMoving(SplitView webcontentlist,float val) {
				//showT("onMoving"+System.currentTimeMillis());
				View IMPageCover_ = contentUIData.cover;
				View PageSlider_ = contentUIData.PageSlider;
				if(PeruseViewAttached()) {
					IMPageCover_= peruseView.contentUIData.cover;
					PageSlider_= peruseView.contentUIData.PageSlider;
				}
				IMPageCover_.setVisibility(View.VISIBLE);
//				if(!webcontentlist.decided)
//					IMPageCover_.setTranslationY(val);
//				else {//贴底
//					IMPageCover_.setTranslationY(webcontentlist.multiplier==-1?0:PageSlider_.getHeight()-height);
//				}
			}
			
			@Override
			public void onPageTurn(SplitView webcontentlist) {
				//showT("onPageTurn"+System.currentTimeMillis());
				View IMPageCover_ = contentUIData.cover;
				boolean bPeruseIncharge = PeruseViewAttached() && (peruseView.contentview.getParent()== peruseView.slp || peruseView.contentview.getParent()== peruseView.mlp);
				if(PeruseViewAttached())
					IMPageCover_= peruseView.contentUIData.cover;
				IMPageCover_.setVisibility(View.GONE);
				
				if(bPeruseIncharge)
					opt.setPeruseBottombarOnBottom(webcontentlist.getChildAt(0).getId()!=R.id.bottombar2);
				else
					opt.setBottombarOnBottom(webcontentlist.getChildAt(0).getId()!=R.id.bottombar2);
				
				if(opt.getNavigationBtnType()==2)
					locateNaviIcon();
			}
			
			@Override
			public void onHesitate() {
				//showT("onHesitate"+System.currentTimeMillis());
				PageSlide IMPageCover_ = contentUIData.cover;
				if(peruseView !=null && peruseView.getView()!=null && peruseView.getView().getParent()!=null)
					IMPageCover_= peruseView.contentUIData.cover;
				if(IMPageCover_!=null)
					IMPageCover_.setVisibility(View.GONE);
			}
			
			@Override
			public void SizeChanged(int newSize, float delta) {}
			
			@Override
			public void onDrop(int size) {
			}
			
			@Override
			public int preResizing(int size) {
				return size;
			}
		};
		
		if(thisActType==ActType.MultiShare) {
			return;
		}
		
		ArrayList<PlaceHolder> CC = lazyLoadManager().placeHolders;
		if(loadManager.md_size==0){
			populateDictionaryList(def, CC, retrieve_all);
		}
		
		if(opt.getCheckMdlibs()){
			File rec = opt.fileToDecords(ConfigFile);
			ReadInMdlibs(rec);
			File mdlib = opt.lastMdlibPath;
			if(mdlib.isDirectory() && mdlib.canRead())
			{
				//todo 交换顺序，先新建，再写入（新建失败的也写入）。
				File [] arr = mdlib.listFiles(pathname -> {
					if(pathname.isFile()) {
						String fn = pathname.getName();
						if(fn.toLowerCase().endsWith(".mdx")) {
							if(retrieve_all || !mdlibsCon.contains(fn)) {                                            //!!!新欢
								try {
									if(output2==null) {
										output2 = new BufferedWriter(new FileWriter(def,true));
										output = new BufferedWriter(new FileWriter(rec,true));
										if (retrieve_all) {
											for (int i = 0; i < loadManager.md_size; i++) {
												BookPresenter book = loadManager.md_get(i);
												if (book!=EmptyBook) {
													output2.write(book.getPath());
													output2.write("\n");
												}
											}
										}
									}
									mdlibsCon.add(fn);//记录的时相对路径
									output.write(fn);
									output.write("\n");
									
									output2.write(fn);
									output2.write("\n");
								} catch (Exception ignored) {}//IOxE
								return true;
							}
						}
					}
					return false;
				});
				if(arr!=null) {
					for (final File i : arr) {
						try {
							loadManager.addBook(new BookPresenter(i, this, 0), new PlaceHolder(i.getName(), CC));
						} catch (Exception e) { /*CMN.Log(e);*/ }
					}
				}
				try {
					if(output2!=null) {
						output2.flush();
						output2.close();
						output.flush();
						output.close();
					}
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		switch_Dict(dictPicker.adapter_idx, false, false, null);
		
		findFurtherViews();
		
		boolean b1=thisActType==ActType.PlainDict;
		etTools = new SearchbarTools(MainActivityUIBase.this, etSearch
				, b1?((PDICMainActivity)this).UIData.etSchBar:null, mainframe, true);
		etTools.initWay = this;
		etTools.schSql = "src&"+(schuiMain|schuiMainSchs)+"!=0";
		
		toolbar.setOnMenuItemClickListener(this);
		ivDeleteText.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		if(isCombinedSearching) {
			AllMenus.findItem(R.id.toolbar_action1).setIcon(R.drawable.ic_btn_multimode);
		}
		String debugMsg = "";
		if (PDICMainAppOptions.debug()) {
			MdictServerMobile.getRemoteServerRes("/李白全集.0.txt", true);
			if (MdictServerMobile.hasRemoteDebugServer) {
				debugMsg += "已连接调试服务器！";
			}
		}
		if (PDICMainAppOptions.getEnableWebDebug()) {
			debugMsg += "已开启网页调试！";
		}
		if (debugMsg.length()>0) {
			showT(debugMsg);
		}
		//if(opt.isShowDirectSearch()) ((MenuItem)toolbar.getMenu().findItem(R.id.toolbar_action2)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if(opt.getLockStartOrientation()) {
			//CMN.Log("锁定!!", opt.getUserOrientation());
			opt.setTmpUserOrientation(opt.getUserOrientation());
			setRequestedOrientation(ScreenOrientation[opt.getUserOrientation()]);
		}
		
		((AgentApplication)getApplication()).handles[thisActType.ordinal()]=hdl;
	}
	
	@CallSuper
	protected void findFurtherViews() {
		if(contentUIData==null) {
			contentUIData = ContentviewBinding.inflate(getLayoutInflater());
		}
		if(contentview==null) {
			weblist = weblistHandler = new WebViewListHandler(this, contentUIData, schuiMain);
			if (bottombar!=null) {
				bottombar.setTag(weblist);
			}
			weblistHandler.setBottomNavWeb(PDICMainAppOptions.bottomNavWeb1());
			AllMenus.tag = weblistHandler;
			contentview = contentUIData.webcontentlister;
			webSingleholder = contentUIData.webSingleholder;
		}
		menuSearchMode = AllMenus.findItem(R.id.toolbar_action1);
		
		weblistHandler.setUpContentView(cbar_key);
		
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		actionBarSize = (int) mResource.getDimension(R.dimen.barSize);
		setContentDetachType(1);
		
		if (etSearch!=null) {
			((EditTextmy)etSearch).bNeverBlink = bNeverBlink;
			if (toolbar!=null) {
				toolbar.setOnDragListener(new View.OnDragListener() {
					@Override
					public boolean onDrag(View v, DragEvent event) {
						if(event.getAction()== DragEvent.ACTION_DROP){
							try {
								ClipData textdata = event.getClipData();
								if(textdata.getItemCount()>0){
									etSearch.setText(textdata.getItemAt(0).getText());
								}
								return true;
							} catch (Exception e) { }
						}
						return true;
					}
				});
			}
		}
		if (root != null) {
			mViewRootHolder.view = root.getRootView();
		}
	}
	
	protected void populateDictionaryList() {
		final File def = getStartupFile(opt.fileToConfig());      //!!!原配
		if(loadManager.md_size==0){
			populateDictionaryList(def, lazyLoadManager().placeHolders, !def.exists());
		}
	}
	
	final String[] defDicts = new String[]{
			CMN.AssetTag + "李白全集.mdx"
			,"/ASSET2/谷歌翻译.web"
			,"/ASSET2/维基词典.web"
			,"/ASSET2/彩云小译.web"
			,"/ASSET2/应用社区.web"
	};
	
	protected void populateDictionaryList(File def, ArrayList<PlaceHolder> CC, boolean retrieve_all) {
		BookPresenter book = null;
		try {
			book = new_book(defDicts[1], this);
		} catch (Exception ignored) {  }
		if(retrieve_all) {
			try {
				String lastName = opt.getLastMdFn("LastMdFn"); //???!!!
				for (String path:defDicts) {
					PlaceHolder placeHolder = new PlaceHolder(path, CC);
					loadManager.addBook(new_book(placeHolder, this), placeHolder);
					if(TextUtils.equals(new File(path).getName(), lastName)) {
						dictPicker.adapter_idx = loadManager.md_size-1;
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		else try {
			boolean lazyLoad = opt.getLazyLoadDicts();
			loadManager.LoadLazySlots(def, lazyLoad, opt.getLastPlanName(LastPlanName));
			loadManager.buildUpDictionaryList(lazyLoad, null);
		} catch (Exception e) { CMN.debug(e); }
	}
	
	protected File getStartupFile(File ConfigFile){
		File def = null;
		if (thisActType==ActType.PlainDict && opt.getCacheCurrentGroup()) {
			def = new File(getExternalFilesDir(null),"default.txt");
			if(def.length()<=0) def=null;
		}
		if(def==null){
			def = opt.fileToSet(ConfigFile, opt.getLastPlanName(LastPlanName));
		}
		//CMN.debug("getStartupFile::", def);
		return def;
	}
	
	public static class LazyLoadManager {
		public ArrayList<PlaceHolder> placeHolders = new ArrayList<>();
		private HashMap<String, Integer> map = new HashMap<>();
		public int[] CosyChair = ArrayUtils.EMPTY_INT_ARRAY;
		public int[] CosySofa = ArrayUtils.EMPTY_INT_ARRAY;
		public int chairCount;
		public int filterCount;
		//public ArrayList<PlaceHolder>[] PlaceHolders = new ArrayList[]{CosyChair, CosySofa, HiddenDicts};
		public int lastCheckedPos;
		long currMdlTime;
		boolean lazyLoaded;
		public String lastLoadedModule;
		
		public void newChair() {
			if (CosyChair.length<=chairCount) {
				int[] newChairs = new int[(int) Math.max(chairCount*1.5f, chairCount+1)];
				if(CosyChair!=null)
					System.arraycopy(CosyChair, 0, newChairs, 0, chairCount);
				CosyChair = newChairs;
			}
			CosyChair[chairCount++] = placeHolders.size()-1;
		}
		
		public void do_LoadLazySlots(ReusableBufferedReader in) throws IOException {
			String line;
			int cc=0;
			placeHolders.clear();
			chairCount=0;
			filterCount=0;
			map.clear();
			HashSet<String> map = new HashSet<>(); //todo map
			ReadLines:
			while((line = in.readLine())!=null) {
				int flag = 0;
				boolean chair = true;
				if(line.startsWith("[:")){
					int idx = line.indexOf("]",2);
					if(idx>=2){
						String[] arr = line.substring(2, idx).split(":");
						line = line.substring(idx+1);
						for (String pI:arr) {
							switch (pI){
								case "F":
									flag|=0x1;
									filterCount++;
									chair = false;
									break;
								case "C":
									flag|=0x2;
									break;
								case "A":
									flag|=0x4;
									break;
								case "H":
									flag|=0x8;
									//lazyMan.hiddenCount++;
									chair = false;
									break;
								case "Z":
									flag|=0x10;
									break;
								case "S":
									int size = IU.parsint(line);
									if(size>0) placeHolders.ensureCapacity(size);
									continue ReadLines;
							}
						}
					}
				}
				if (line.endsWith("liba.mdx") && line.startsWith(CMN.AssetTag)) {
					line = CMN.AssetTag + "李白全集.mdx";
				}
				if (map.add(line)) { // 避免重复
					PlaceHolder phI = new PlaceHolder(line);
					phI.lineNumber = cc++;
					phI.tmpIsFlag = flag;
					placeHolders.add(phI);
					if (chair) {
						chairCount++;
					}
				}
			}
			in.close();
		}
		
		public LazyLoadManager clone() {
			LazyLoadManager other = this;
			LazyLoadManager ret = new LazyLoadManager();
			ret.placeHolders.addAll(other.placeHolders);
			ret.CosyChair = ArrayUtils.clone(other.CosyChair);
			ret.CosySofa = ArrayUtils.clone(other.CosySofa);
			ret.chairCount = other.chairCount;
			ret.filterCount = other.filterCount;
			ret.lastCheckedPos = other.lastCheckedPos;
			ret.currMdlTime = other.currMdlTime;
			ret.lazyLoaded = other.lazyLoaded;
			ret.lastLoadedModule = other.lastLoadedModule;
			return ret;
		}
	}
	
	/** 分离词典加载逻辑以使不同界面可加载不同分组。 */
	public /*static*/ class LoadManager {  // todo make static and separate file
		public int md_size;
		public final ArrayList<BookPresenter> md = new ArrayList<>();
		public LazyLoadManager lazyMan;
		public final DictPicker dictPicker;
		
		final String LastMdFn;// = "LastMdFn";
		final PDICMainAppOptions opt;
		public BookPresenter EmptyBook;
		
		LoadManager(DictPicker dictPicker) {
			this.dictPicker = dictPicker;
			this.LastMdFn = MainActivityUIBase.this.LastMdFn;
			this.opt = MainActivityUIBase.this.opt;
			this.lazyMan = new LazyLoadManager();
			if (dictPicker.loadManager==null) {
				dictPicker.loadManager = this;
			}
		}
		
		private LoadManager(LoadManager other) {
			this.dictPicker = other.dictPicker;
			this.LastMdFn = other.LastMdFn;
			this.opt = other.opt;
			this.lazyMan = other.lazyMan.clone();
			this.EmptyBook = other.EmptyBook;
			this.md_size = other.md_size;
			this.md.addAll(other.md);
		}
		
		public LoadManager clone() {
			return new LoadManager(this);
		}
		
		public PlaceHolder getPlaceHolderAt(int idx) {
			if(idx>=0 && idx<lazyMan.chairCount)
				return lazyMan.placeHolders.get(lazyMan.CosyChair[idx]);
			return null;
		}
		
		public int getPlaceFlagAt(int idx) {
			PlaceHolder tmp = getPlaceHolderAt(idx);
			if(tmp!=null)
				return tmp.tmpIsFlag;
			return 0;
		}
		
		public int refreshSlots(boolean moduleChanged) {
			CMN.debug("refreshSlots:::", moduleChanged);
			int pickerIdx=-1;
			int filterCount=0;
			int chairCount=0;
			PlaceHolder ph;
			String lastName = opt.getLastMdFn(LastMdFn);
			for (int i = 0; i < lazyMan.placeHolders.size(); i++) {
				ph = lazyMan.placeHolders.get(i);
				ph.lineNumber = i | (ph.lineNumber & 0x80000000);
				if (moduleChanged) {
					BookPresenter md = mdict_cache.get(ph.getPath(opt).getName());
					if (md!=null) {
						md.tmpIsFlag = ph.tmpIsFlag;
						md.placeHolder = ph;
					}
				}
				final int flag = ph.tmpIsFlag;
				if (!PDICMainAppOptions.getTmpIsHidden(flag)) {
					if (PDICMainAppOptions.getTmpIsFiler(flag)) {
						filterCount++;
					} else {
						if (pickerIdx==-1 && md_name_match(lastName, md.get(i), ph)){
							pickerIdx = chairCount;
						}
						chairCount++;
					}
				}
			}
			lazyMan.chairCount=chairCount;
			lazyMan.filterCount=filterCount;
			if(lazyMan.CosyChair.length<chairCount)lazyMan.CosyChair=new int[chairCount];
			if(lazyMan.CosySofa.length<filterCount)lazyMan.CosySofa=new int[filterCount];
			filterCount=0;
			chairCount=0;
			for (int i = 0; i < lazyMan.placeHolders.size(); i++) {
				ph = lazyMan.placeHolders.get(i);
				final int flag = ph.tmpIsFlag;
				if (!PDICMainAppOptions.getTmpIsHidden(flag)) {
					if (PDICMainAppOptions.getTmpIsFiler(flag)) {
						lazyMan.CosySofa[filterCount++]=i;
					} else {
						lazyMan.CosyChair[chairCount++]=i;
					}
				}
			}
			md_size = chairCount;
			return pickerIdx;
		}
		
		//parseList
		void buildUpDictionaryList(boolean lazyLoad, HashMap<String, BookPresenter> mdict_cache) {
			ArrayList<PlaceHolder> all =  lazyMan.placeHolders;
			md.ensureCapacity(all.size());
			md.clear();
			int pickerIdx = -1;
			PlaceHolder phI;
			String lastName = opt.getLastMdFn(LastMdFn);
			int filterCount=0,chairCount=0;
			CMN.debug("buildUpDictionaryList::", lazyLoad, lazyMan.chairCount);
			if(lazyMan.CosyChair.length<lazyMan.chairCount)lazyMan.CosyChair=new int[lazyMan.chairCount];
			if(lazyMan.CosySofa.length<lazyMan.filterCount)lazyMan.CosySofa=new int[lazyMan.filterCount];
			for (int i = 0; i < all.size(); i++) {
				phI = all.get(i);
				//get path put
				BookPresenter mdTmp = mdict_cache==null?null:mdict_cache.get(phI.getPath(opt).getName());
				if ((phI.tmpIsFlag&0x8)!=0){
					//lazyMan.HiddenDicts[lazyMan.hiddenCount++]=i;  /* 隐·1 */
					md.add(null);
					continue;
				}
				if(mdTmp==null && !lazyLoad) { // 大家看 这里有个老实人
					try {
						mdTmp = new_book(phI, MainActivityUIBase.this);
					} catch (Exception e) {
						phI.tmpIsFlag|=0x8;
						//lazyMan.HiddenDicts[lazyMan.hiddenCount++]=i;  /* 兵解轮回 */
						CMN.debug(e);
						if (trialCount == -1) if (bShowLoadErr)
							show(R.string.err, phI.getName(), phI.pathname, e.getLocalizedMessage());
						mdTmp = null;
					}
				}
				if(mdTmp!=null)
					mdTmp.tmpIsFlag = phI.tmpIsFlag;
				if ((phI.tmpIsFlag&0x1)!=0) {
					//CMN.Log("发现构词库！！！", phI.name, phI.tmpIsFlag);
					//currentFilter.add(mdTmp);
					lazyMan.CosySofa[filterCount++]=i;
					//lazyMan.HiddenDicts[lazyMan.hiddenCount++]=i; /* 隐·2 */
				} else {
					if (pickerIdx==-1 && md_name_match(lastName, mdTmp, phI)){
						pickerIdx = chairCount;
					}
					lazyMan.CosyChair[chairCount++]=i;
				}
				md.add(mdTmp);
			}
			dictPicker.adapter_idx = pickerIdx;
			md_size = lazyMan.chairCount;
			//lazyMan.lazyLoaded = lazyLoad;
			//CMN.Log("buildUpDictionaryList", lastName, adapter_idx);
		}
		
		private boolean md_name_match(String lastName, BookPresenter mdTmp, PlaceHolder phI) {
			if(lastName!=null){
				if(mdTmp!=null){
					return mdTmp.getDictionaryName().equals(lastName);
				}
				if(phI!=null){
					String pN = phI.pathname;
					if(pN.endsWith(lastName)) {
						int len = lastName.length();
						int lenN = pN.length();
						return lenN==len||pN.charAt(lenN-len-1)==File.separatorChar;
					}
				}
			}
			return false;
		}
		
		public void LoadLazySlots(File modulePath, boolean lazyLoad, String moduleName) throws IOException {
			long lm = modulePath.lastModified();
			boolean fromManager = lazyMan.chairCount==-1;
			if(!fromManager
					&& lm==lazyMan.currMdlTime
					&& lazyMan.lazyLoaded==lazyLoad
					&& moduleName.equals(lazyMan.lastLoadedModule)
			){
				md.clear();
				md.addAll(Arrays.asList(new BookPresenter[lazyMan.chairCount]));
				return;
			}
			CMN.debug("LoadLazySlots…",  moduleName);
			AgentApplication app = ((AgentApplication) getApplication());
			ReusableBufferedReader in = new ReusableBufferedReader(new FileReader(modulePath), app.get4kCharBuff(), 4096);
			lazyMan.do_LoadLazySlots(in);
			lazyMan.currMdlTime=lm;
			lazyMan.lastLoadedModule=moduleName;
			lazyMan.lazyLoaded=lazyLoad;
			app.set4kCharBuff(in.cb);
			if (fromManager) {
				md.clear();
				for (int i = 0; i < lazyMan.placeHolders.size(); i++) {
					PlaceHolder ph = lazyMan.placeHolders.get(i);
					md.add(mdict_cache.get(ph.getPath(opt).getName()));
				}
				if(lazyMan.CosyChair.length<lazyMan.chairCount)lazyMan.CosyChair=new int[lazyMan.chairCount];
				if(lazyMan.CosySofa.length<lazyMan.filterCount)lazyMan.CosySofa=new int[lazyMan.filterCount];
			}
		}
		
		public long getBookIdAt(int i) {
			if (getUsingDataV2()) {
				try {
					if (i>=0 && i<lazyMan.chairCount) {
						i = lazyMan.CosyChair[i];
						BookPresenter presenter = md.get(i);
						if (presenter != null) return presenter.getId();
						String name = new File(lazyMan.placeHolders.get(i).pathname).getName();
						return prepareHistoryCon().getBookID(null, name);
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
			return -1;
		}
		
		@NonNull public BookPresenter md_getNoCreate(int i, long bid) {
			BookPresenter ret = null;
			if (bid!=-1) {
				ret = getBookByIdNoCreation(bid);
			} else {
				if (i>=0 && i<lazyMan.chairCount) {
					i = lazyMan.CosyChair[i];
					ret = md.get(i);
				}
			}
			if(ret==null) {
				ret = EmptyBook;
			}
			return ret;
		}
		
		private void reload_dict_at(int i) {
			try {
				if (i>=0 && i<lazyMan.chairCount) {
					i = lazyMan.CosyChair[i];
					BookPresenter mdTmp = md.get(i);
					if(mdTmp!=null) {
						mdTmp.Reload(this);
					} else {
						PlaceHolder phTmp = lazyMan.placeHolders.get(i);
						md.set(i, new_book(phTmp, MainActivityUIBase.this));
					}
					showT("重新加载!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Nullable public BookPresenter md_getAt(int i) {
			if (i>=0 && i<lazyMan.chairCount) {
				return md.get(lazyMan.CosyChair[i]);
			}
			return null;
		}
		
		public int md_find(BookPresenter book) {
			// todo opt with binary search  ( book.placeHolder.lineNumer -> CosyChair -> index )
			return Arrays.asList(lazyMan.CosyChair).indexOf(md.indexOf(book));
		}
		
		@NonNull public final BookPresenter getBookById(long bid) {
			return MainActivityUIBase.this.getBookById(bid);
		}
		
		@NonNull public BookPresenter md_get(int i) {
			BookPresenter ret = null;
			PlaceHolder phTmp = null;
			if (i>=0 && i<lazyMan.chairCount) {
				i = lazyMan.CosyChair[i];
				ret = md.get(i);
				if (ret==null) {
					phTmp = lazyMan.placeHolders.get(i);
					try {
						md.set(i, ret = new_book(phTmp, MainActivityUIBase.this));
					} catch (Exception e) {
						if(GlobalOptions.debug) CMN.debug(phTmp, e);
						phTmp.ErrorMsg = e.getLocalizedMessage();
//						if (bShowLoadErr && isOnMainThread()) {
//							if(!phTmp.NeedsBuildIndex())
//								show(R.string.err);
//							//else showT("需要建立索引！");
//						}
						//todo 123
					}
				}
			}
			if(ret==null) {
				ret = EmptyBook;
				EmptyBook.placeHolder = phTmp;
			}
			return ret;
		}
		
		@NonNull public BookPresenter md_getByName(String name) {
			return getBookByName(name);
		}
		
		public String md_getName(long bid) {
			BookPresenter book = getBookById(bid);
			return book.getDictionaryName();
		}
		
		public String md_getName(int i, long bid) {
			if (bid!=-1) {
				BookPresenter book = getBookById(bid);
				return book.getDictionaryName();
			}
			if (i>=0 && i<lazyMan.chairCount) {
				i = lazyMan.CosyChair[i];
				String name = null;
				BookPresenter mdTmp = md.get(i);
				if(mdTmp!=null) {
					name = mdTmp.getPath();
					if (name.startsWith(AssetTag)) name = CMN.getAssetName(name);
					else name = mdTmp.getDictionaryName();
				} else {
					PlaceHolder placeHolder = lazyMan.placeHolders.get(i);
					name = placeHolder.pathname;
					if (name.startsWith(AssetTag)) name = CMN.getAssetName(name);
					else name = placeHolder.getName().toString();
				}
				if(name!=null) {
					return name;
				}
			}
			return "Error!!!";
		}
		
		public void md_set_StarLevel(int i, int val) {
			long flag=0;
			if (i>=0 && i<lazyMan.chairCount) {
				i = lazyMan.CosyChair[i];
				BookPresenter mdTmp = md.get(i);
				if(mdTmp!=null) {
					flag = mdTmp.getFirstFlag();
					mdTmp.setFirstFlag(flag=PDICMainAppOptions.setDFFStarLevel(flag, val));
					mdTmp.saveStates(MainActivityUIBase.this, prepareHistoryCon());
				} else {
					PlaceHolder placeHolder = lazyMan.placeHolders.get(i);
					CharSequence name = placeHolder.getName();
					flag =  md_get_firstFlag_internal(name);
					flag = PDICMainAppOptions.setDFFStarLevel(flag, val);
					md_set_firstFlag_internal(name, flag);
				}
				CMN.debug("新的星级", md_getName(i, -1), PDICMainAppOptions.getDFFStarLevel(flag), val);
			}
		}
		
		public int md_get_StarLevel(int i, long bid) {
			long flag = 0;
			if (bid!=-1) {
				BookPresenter book = getBookById(bid);
				flag = book.getFirstFlag();
			}
			else if (i>=0 && i<lazyMan.chairCount) {
				i = lazyMan.CosyChair[i];
				BookPresenter mdTmp = md.get(i);
				if(mdTmp!=null) {
					flag =  mdTmp.getFirstFlag();
				} else {
					PlaceHolder placeHolder = lazyMan.placeHolders.get(i);
					flag =  md_get_firstFlag_internal(placeHolder.getSoftName());
				}
			}
			return PDICMainAppOptions.getDFFStarLevel(flag);
		}
		
		@SuppressWarnings("All")
		public CharSequence md_getAbout_Trim(int i) {
//			if (i>=0 && i<lazyMan.chairCount) {
//				i = lazyMan.CosyChair[i];
//				PlaceHolder placeHolder = lazyMan.placeHolders.get(i);
//
//			}
//			ArrayList<PlaceHolder> placeHolders = getPlaceHolders();
//			PlaceHolder phTmp = placeHolders.get(i);
//			BookPresenter presenter = md.get(i);
//			String msg = phTmp.ErrorMsg;
//			boolean show_info_extra = true||msg!=null;
//			boolean show_info_codec = true;
//			boolean show_info_reload = true;
//			boolean show_info_path = true;
//			SpannableStringBuilder sb=null;
//			if(show_info_extra) {
//				sb = new SpannableStringBuilder();
//				if(show_info_reload) {
//					sb.append("[重新加载");
//				}
//				if(show_info_codec && presenter!=null) {
//					if(!show_info_reload) {
//						sb.append("编码");
//					}
//					sb.append("：");
//					sb.append(String.valueOf(presenter.getCharsetName()));
//				}
//				if(show_info_reload) {
//					sb.append("]");
//				}
//				sb.append("\n");
//				if(show_info_reload) {
//					sb.setSpan(new ClickableSpan() {
//						@Override public void onClick(@NonNull View widget) {
//							reload_dict_at(i);
//							//showAboutDictDialogAt(i);
//						}
//					}, 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//				}
//			}
//
//			CharSequence ret=presenter==null? "未加载"
//					:HtmlCompat.fromHtml(presenter.getAboutString(), HtmlCompat.FROM_HTML_MODE_COMPACT);
//			int len = ret.length();
//			int st = 0;
//			while((st < len) && ret.charAt(st) <= ' '){
//				st++;
//			}
//			while ((st < len) && (ret.charAt(len - 1) <= ' ')) {
//				len--;
//			}
//
//			if(show_info_extra) {
//				sb.append(ret, st, len);
//				ret=sb;
//				if(msg!=null) {
//					sb.append("\n\n").append("错误信息：");
//					sb.append(msg);
//				}
//				if(presenter!=null && presenter.bookImpl.hasMdd()) {
//					sb.append("\n\n").append("资源文件：");
//					sb.append(presenter.bookImpl.getResourcePaths());
//				}
//				if(show_info_path) {
//					sb.append("\n");
//					sb.append("路径").append("：");
//					st = sb.length();
//					sb.append(phTmp.pathname);
//					sb.setSpan(new ClickableSpan() {
//						@Override public void onClick(@NonNull View widget) {
//							showT("路径!");
//						}
//					}, st, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//				}
//			} else {
//				ret=ret.subSequence(st, len);
//			}
//			return ret;
			return null;
		}
		
		public Drawable md_getCover(int i, long bid) {
			if (bid!=-1) {
				BookPresenter book = getBookById(bid);
				return book.getCover();
			}
			if (i>=0 && i<lazyMan.chairCount) {
				i = lazyMan.CosyChair[i];
				BookPresenter mdTmp = md.get(i);
				if(mdTmp!=null) return mdTmp.cover;
				// todo
			}
			return null;
		}
		
		public BookPresenter getFilterAt(int i) {
			if (i>=0 && i<lazyMan.filterCount) {
				i = lazyMan.CosySofa[i];
				BookPresenter mdTmp = md.get(i);
				if(mdTmp==null){
					PlaceHolder placeHolder = lazyMan.placeHolders.get(i);
					try {
						md.set(i, mdTmp= new_book(placeHolder, MainActivityUIBase.this));
						mdTmp.tmpIsFlag=placeHolder.tmpIsFlag;
					} catch (Exception e) { CMN.debug(e); }
				}
				if(mdTmp!=null){
					return mdTmp;
				}
			}
			return EmptyBook;
		}
		
		public void addBook(BookPresenter book, PlaceHolder placeHolder) {
			lazyMan.placeHolders.add(placeHolder);
			if (book!=null) {
				book.placeHolder=placeHolder;
			}
			md.add(book);
			lazyMan.newChair();
			md_size=lazyMan.chairCount;
		}
		
		public HashMap<String, Integer> map() {
			if (lazyMan.map.size() == 0) {
				for (int i = 0; i < md_size; i++) {
					String bookName = md_getName(i, -1);
					lazyMan.map.put(bookName, i);
				}
			}
			return lazyMan.map;
		}
	}
	
//	static LazyLoadManager lazyLoadManagerMain = new LazyLoadManager();
//	static LazyLoadManager lazyLoadManagerFloat = new LazyLoadManager();
//	static LazyLoadManager lazyLoadManagerMulti = new LazyLoadManager();
	public LoadManager loadManager;
	public final LazyLoadManager lazyLoadManager(){ return loadManager.lazyMan; };
	
	
	public final PlaceHolder getPlaceHolderAt(int idx) {
		return loadManager.getPlaceHolderAt(idx);
	}
	
	void ReadInMdlibs(File rec) {
		if(rec==null){
			rec = opt.fileToDecords(null);
		}
		if(mdlibsCon==null){
			mdlibsCon = new HashSet<>(loadManager.md_size*3);
			lostFiles = new HashMap<>();
			if(rec.exists())
				try {
					BufferedReader in = new BufferedReader(new FileReader(rec));
					String line;
					while((line=in.readLine())!=null){
						mdlibsCon.add(line);															   //!!!旧爱
						File check;
						if(!line.startsWith("/"))
							check=new File(opt.lastMdlibPath,line);
						else
							check=new File(line);
						if(!line.startsWith("/ASSET/") && !check.exists())
							lostFiles.put(check.getName(),check.getPath());
					}
					in.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			else {
				try {
					rec.getParentFile().mkdirs();
					rec.createNewFile();
				} catch (IOException ignored) {}
			}
		}
	}


	int etSearch_toolbarMode=0;
	private OnScrollChangedListener onWebScrollChanged;
	//Strategy: use one webscroll listener
	public OnScrollChangedListener getWebScrollChanged() {
		if(onWebScrollChanged==null) {
			onWebScrollChanged = (v, x, y, oldx, oldy) -> {
				WebViewmy webview = (WebViewmy) v;
				if(CustomViewHideTime>0 && System.currentTimeMillis()-CustomViewHideTime<350){
					webview.SafeScrollTo(oldx, oldy);
					return;
				}
				if(layoutScrollDisabled)
				{
					final int lalaX=webview.expectedPosX;//IU.parsint(v.getTag(R.id.toolbar_action1));
					final int lalaY=webview.expectedPos;//IU.parsint(v.getTag(R.id.toolbar_action2));
					if(lalaY!=-1 && lalaX!=-1) {
						v.scrollTo(lalaX, lalaY);
						v.setLayoutParams(v.getLayoutParams());
						//CMN.Log("scrolling to: "+lalaY);
					}
				}
				
				DragScrollBar mBar=webview.weblistHandler.mBar;
				float currentScale = webview.webScale;
				
				if(webview.weblistHandler.src==SearchUI.Fye.MAIN
						|| !isCombinedSearching
						|| webview==weblistHandler.mMergedFrame
						|| (ActivedAdapter!=null && !(ActivedAdapter.results instanceof resultRecorderCombined))) {
					if(mBar.isHidden()){
						if(Math.abs(oldy-y)>=10*dm.density)
							mBar.fadeIn();
					}
					if(!mBar.isHidden()){
						if(!mBar.isWebHeld)
							mBar.hiJackScrollFinishedFadeOut();
						if(!mBar.isDragging){
							mBar.setMax(webview.getContentHeight()-webview.getHeight());
							mBar.progress(webview.getContentOffset());
						}
					}
				}
			};
		}
		return onWebScrollChanged;
	}

	/** 0-搜索  1-返回  2-删除  4-撤销   */
	boolean etSearch_ToToolbarMode(int mode) {
		//CMN.Log("etSearch_ToToolbarMode::", mode);
		if(thisActType==ActType.PlainDict) {
//			ViewUtils.findInMenu(AllMenusStamp, R.id.toolbar_action2)
//					.setIcon(!isContentViewAttached()?R.drawable.ic_search_24k:R.drawable.ic_back_material);
			int id  = isContentViewAttached()?R.id.ivBack:R.id.multiline;
			if(id!=ivBack.getId()) {
				ivBack.setImageResource(id==R.id.multiline?R.drawable.ic_menu_material:R.drawable.back_toolbar);
				ivBack.setId(id);
			}
			return true;
		}
		switch(mode){
			case 0:{//图标:搜索
				if((etSearch_toolbarMode&1)!=0) {
					etSearch_toolbarMode&=2;
					ivBack.setImageResource(R.drawable.search_toolbar);
				}/*
				iItem_FolderAll.setVisible(false);//折叠
				iItem_InPageSearch.setVisible(false);
				if (iItem_ClickSearch != null)
					iItem_ClickSearch.setVisible(false);
				applyMainMenu();*/
			} return true;
			case 1:{//图标:返回
				if((etSearch_toolbarMode&1)!=1) {
					etSearch_toolbarMode|=1;
					ivBack.setImageResource(R.drawable.back_toolbar);
				}
			} return true;
			case 3:{//图标:删除
				if((etSearch_toolbarMode&2)!=0) {
					etSearch_toolbarMode&=1;
					ivDeleteText.setImageResource(R.drawable.close_toobar);
				}
			} return true;
			case 4:{//图标:撤销
				if((etSearch_toolbarMode&2)!=2) {
					etSearch_toolbarMode|=2;
					ivDeleteText.setImageResource(R.drawable.undo_toolbar);
				}
			} return true;
			default:return false;
		}
	}
	
	void applyMainMenu() {
		((MenuBuilder)toolbar.getMenu()).setItems(CurrentViewPage==1?MainMenu:LEFTMenu);
	}
	
	public boolean checkDicts() {
		if(loadManager.md_size>0) {
			if(currentDictionary==null || currentDictionary==EmptyBook){
				int checkIdx = dictPicker.adapterIdx();
				currentDictionary = loadManager.md_get(checkIdx);
				adaptermy.setPresenter(currentDictionary);
			}
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		foreground|=(1<<thisActType.ordinal());
	}

	@Override
	protected void onPause() {
		super.onPause();
		foreground&=~(1<<thisActType.ordinal());
		if(TTSController_engine!=null && !opt.getTTSBackgroundPlay()){
			pauseTTS();
			pauseTTSCtrl(true);
		}
		if(peruseView !=null) {
			peruseView.dismissDialogOnly();
		}
		if((!AutoBrowsePaused || bRequestingAutoReading) && !opt.getTTSBackgroundPlay()){
			stopAutoReadProcess();
		}
		if(ViewUtils.mNestedScrollingChildHelper!=null){
			ViewUtils.mNestedScrollingChildHelper.setCurrentView(null);
		}
	}

	@Override
	protected void onDestroy(){
		foreground&=~(1<<thisActType.ordinal());
		if(!shunt) {
			CMN.instanceCount--;
			if(systemIntialized) {
				if(!AutoBrowsePaused || bRequestingAutoReading){ //avoid leak ServiceConnection
					stopAutoReadProcess();
				}
				for (BookPresenter mdTmp : mdict_cache.values()) {
					if (mdTmp != null) {
						mdTmp.unload();
					}
				}
				mdict_cache.clear();
				if(webSingleholder!=null) {
					webSingleholder.removeAllViews();
					weblistHandler.removeAllViews();
				}
				
				if(ucc!=null) {
					ucc.invoker=null;
					ucc=null;
				}
				if(TTSController_engine !=null){
					TTSController_engine.stop();
					TTSController_engine.shutdown();
				}
				if(CMN.instanceCount<=0){
					((AgentApplication)getApplication()).closeDataBases();
				}
				if(hdl!=null) {
					hdl.clearActivity();
				}
				WeakReference[] holders = new WeakReference[]{wordPopup.popupCrdCloth, wordPopup.popupCmnCloth, setchooser, bottomPlaylist};
				for(WeakReference hI:holders){
					if(hI!=null)
						hI.clear();
				}
				for(DeckListAdapter.DeckListData dI: DBrowserDatas){
					if(dI!=null) dI.close();
				}
			}
			if(CMN.instanceCount<0) CMN.instanceCount=0;
			if (floatBtn!=null) {
				floatBtn.close();
			}
		}
		super.onDestroy();
	}

	public void notifyGLobalFSChanged(int targetLevel) {
		BookPresenter.def_fontsize=targetLevel;
		ViewGroup webholder = weblistHandler.getViewGroup();
		for(int i=0;i<webholder.getChildCount();i++) {
			View cv = webholder.getChildAt(i);
			if(cv!=null) {
				if(cv.getTag()!=null) {
					try {
						BookPresenter mdTmp = ((WebViewmy) cv.getTag()).presenter;
						if(!mdTmp.getUseInternalFS())
							mdTmp.mWebView.getSettings().setTextZoom(targetLevel);
					} catch (Exception ignored) {}
				}
			}
		}
		opt.putDefaultFontScale(BookPresenter.def_fontsize);
	}

	protected int actionBarSize;
	
	@Override
	protected ViewGroup onShowSnack(ViewGroup parentView) {
		boolean findHolder = parentView==null;
		int bottom = 0;//bottombar.getHeight();
		if(isContentViewAttached()) {
			if(findHolder) parentView = contentUIData.PageSlider;
			bottom = 0;
		}
		if(settingsPanel!=null) {
			if(settingsPanel instanceof AlloydPanel) {
				if(findHolder) parentView = ((AlloydPanel)settingsPanel).handler.contentUIData.PageSlider;
				bottom = 0;//(AlloydPanel)settingsPanel).contentUIData.bottombar2.getHeight();
			} else {
				if(findHolder) parentView = (ViewGroup)settingsPanel.settingsLayout.getParent();
				if(settingsPanel.getLastShowType()==0) {
					bottom = settingsPanel.bottomPadding;
				} else {
					bottom = 0;
				}
			}
		}
		else if(DBrowser!=null && DBrowser.UIData!=null) {
			if(findHolder) parentView = DBrowser.UIData.root;
			bottom = 0;
		}
		if(findHolder) {
			if(parentView!=null) {
				bottom = 0;//bottombar.getHeight();
			} else {
				parentView = snack_holder;
				if (bottombar!=null) {
					bottom = bottombar.getHeight();
				}
				if(bottom==0) {
					bottom = (int) mResource.getDimension(R.dimen.barSzBot);
				}
			}
		}
		topsnack.setBottomMargin(bottom);
		return parentView;
	}
	
	void setContentBow(boolean bContentBow) {
		//actionBarSize=toolbar.getHeight();
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) contentview.getLayoutParams();
		int targetTop = 0;
		if(!PDICMainAppOptions.getEnableSuperImmersiveScrollMode()){
			targetTop = bContentBow?toolbar.getHeight():0;
		}
		if(lp.topMargin!=targetTop){
			lp.setMargins(0,targetTop, 0, 0);
			contentview.requestLayout();
		}
	}
	
	void refreshContentBow(boolean bContentBow, int actionBarSize) {
		if(bContentBow&&!PDICMainAppOptions.getEnableSuperImmersiveScrollMode()) {
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) contentview.getLayoutParams();
			lp.setMargins(0,actionBarSize, 0, 0);
			contentview.requestLayout();
		}
	}

	static void decorateBackground(View v) {
		boolean bNoKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
		Drawable background = v.getBackground();
		if(bNoKitKat){
			if(GlobalOptions.isDark){
				background.setColorFilter(GlobalOptions.NEGATIVE);
			} else{
				background.clearColorFilter();
			}
		}else{
			if(GlobalOptions.isDark){
				v.setTag(R.id.drawer_layout, background);
				v.setBackground(null);
			} else{
				v.setBackground((Drawable) v.getTag(R.id.drawer_layout));
			}
		}
	}
	
	public LexicalDBHelper prepareFavoriteCon() {
		if(favoriteCon!=null) return favoriteCon;
		if (getUsingDataV2()) {
			favoriteCon = prepareHistoryCon();
			return favoriteCon;
		}
		ArrayList<MyPair<String, LexicalDBHelper>> slots = ((AgentApplication) getApplication()).AppDatabases;
		String name = opt.getCurrFavoriteDBName();
		int selectedPos=-1;
		int candidate=-1;
		boolean fading = false;
		String dateBaseName = "favorite.sql";
		if(slots.size()>0){
			for (int i = 0; i < slots.size() & selectedPos==-1; i++) {
				if(name!=null && slots.get(i).key.equals(name)){
					selectedPos = i;
				}
				if(slots.get(i).value != null){
					candidate = i;
				}
			}
			if(selectedPos==-1){
				selectedPos=candidate;
				fading=true;
			}
		}
		LexicalDBHelper _favoriteCon = null;
		if(selectedPos!=-1){
			dateBaseName = slots.get(selectedPos).key;
			_favoriteCon = slots.get(selectedPos).value;
			if(_favoriteCon!=null){
				if(!new File(_favoriteCon.pathName).exists())
					_favoriteCon=null;
			}
		}
		if(fading || selectedPos==-1){
			opt.putCurrFavoriteDBName(dateBaseName);
		}
		if(_favoriteCon==null) {
			CMN.debug("打开数据库", dateBaseName, opt.getCurrFavoriteDBName());
			CheckInternalDataBaseDirExist(false);
			_favoriteCon = new LexicalDBHelper(getApplicationContext(), opt, dateBaseName, false);
			if(selectedPos!=-1){
				slots.get(selectedPos).value = _favoriteCon;
			} else {
				slots.add(new MyPair<>(dateBaseName, _favoriteCon));
			}
		}
		return favoriteCon = _favoriteCon;
	}
	
	protected long lastInsertedId;
	
	protected String lastInsertedKey;
	
	public void addHistory(String key, int source, WebViewListHandler weblist, SearchbarTools etTools) {
		CMN.debug("addHistroy::", key, source);
		if(source>=0/* && TextUtils.getTrimmedLength(key)>0*/) {
			key = key.trim();
			if (key.length()>0) {
				if (!PDICMainAppOptions.storeNothing() || PDICMainAppOptions.storeNothingButSch() && source>=SearchUI.schMin) {
					long ivkAppId = -1;
					lastInsertedId = prepareHistoryCon().updateHistoryTerm(this, key, source, weblist);
					lastInsertedKey = key;
				}
				if (etTools!=null && source>=SearchUI.schMin) {
					etTools.addHistory(key);
				}
			}
		}
	}
	
	public long GetAddHistory(String key, WebViewListHandler wlh) {
		if(key!=null) {
			key = key.trim();
		}
		if (getUsingDataV2() && key.length()>0) {
			if (TextUtils.equals(lastInsertedKey, key)) return lastInsertedId;
			else {
				try {
					lastInsertedId = prepareHistoryCon().updateHistoryTerm(this, key, 0, wlh);
					lastInsertedKey = key;
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		}
		return -1;
	}
	
	public static BookPresenter new_book(String pathFull, MainActivityUIBase THIS) throws IOException {
		File fullPath = pathFull.startsWith("/")?new File(pathFull):new File(THIS.opt.lastMdlibPath, pathFull);
		return new_book(fullPath, THIS);
	}
	
	public static BookPresenter new_book(PlaceHolder phI, MainActivityUIBase THIS) throws IOException {
		BookPresenter ret = new_book(phI.getPath(THIS.opt), THIS);
		ret.tmpIsFlag = phI.tmpIsFlag;
		ret.placeHolder = phI;
		return ret;
	}
	
	public static BookPresenter new_book(File fullPath, MainActivityUIBase THIS) throws IOException {
		BookPresenter ret = THIS.mdict_cache.get(fullPath.getName());
		if (ret!=null) {
			return ret;
		}
		ret = new BookPresenter(fullPath, THIS, THIS.opt.getPseudoInitCode(0));
		THIS.mdict_cache.put(fullPath.getName(), ret);
		return ret;
	}
	
	public void NotifyComboRes(int size) {
		if(PDICMainAppOptions.getNotifyComboRes()) {
			float fval = 0.8f;
			String val = recCom.allWebs?"回车以搜索网络词典！":getResources().getString(R.string.cbflowersnstr,opt.lastMdPlanName,loadManager.md_size,size);
			showTopSnack(null, val, fval, -1, -1, 0);
		}
	}

	public void restoreLv2States() {
		if(pendingLv2Pos!=null){
			int[] arr = pendingLv2Pos;
			//lv2.post(() -> lv2.setSelectionFromTop(arr[0], arr[1]));
			lv2.setSelectionFromTop(arr[0], arr[1]);
			pendingLv2Pos=null;
		}
		if(pendingLv2ClickPos!=-1){
			adaptermy2.onItemClick(pendingLv2ClickPos);
			pendingLv2ClickPos=-1;
		}
	}
	
	public Runnable getOpenImgRunnable() {
		if(mOpenImgRunnable==null){
			mOpenImgRunnable = () -> {
				boolean abort = false;
				if(RLContainerSlider.lastZoomTime >0) {
					if((System.currentTimeMillis()-RLContainerSlider.lastZoomTime)<500){
						abort=true;
					} else {
						RLContainerSlider.lastZoomTime=0;
					}
				}
				if(isBrowsingImgs) {
					abort=true;
				}
				if(abort){
					((AgentApplication)getApplication()).clearTdata();
				} else {
					isBrowsingImgs=true;
					startActivityForResult(new Intent()
							.setClass(MainActivityUIBase.this
									, PhotoViewActivity.class),0);
				}
			};
		}
		return mOpenImgRunnable;
	}
	
	public Drawable getActiveStarDrawable() {
		if(mActiveDrawable!=null) return mActiveDrawable;
		return mActiveDrawable=getResources().getDrawable(R.drawable.star_ic_solid);
	}
	
	public Drawable getRatingDrawable() {
		if(mRatingDrawable==null) {
			mRatingDrawable=getResources().getDrawable(R.drawable.star_ic_grey);
			mRatingDrawable.setAlpha(128);
		}
		return mRatingDrawable;
	}
	
	public void showAboutDictDialogAt(int id) {
		//todo 123
//		if(id<0||id>=md.size()){
//			show(R.string.endendr);
//			return;
//		}
//		boolean create=true;
//		Window window;
//		TextView tv=null;
//		FlowTextView ftv=null;
//		Dialog d = this.d;
//		if(d!=null) {
//			window = d.getWindow();
//			if(window!=null) {
//				Object tag = window.getDecorView().getTag();
//				if(tag instanceof TextView){
//					tv = (TextView) tag;
//					tag = tv.getTag();
//					if(tag instanceof FlowTextView){
//						ftv = (FlowTextView) tag;
//						CheckStars(ftv.getStarLevel());
//						create=false;
//						if(dictPicker !=null) {
//							//pickDictDialog.mRecyclerView.smoothScrollToPosition(id);
//							//pickDictDialog.lman.smoothScrollToPosition(pickDictDialog.mRecyclerView, null, id);
//							dictPicker.lman.scrollToPositionWithOffset(id, 0);
//						}
//						tag = ftv.getTag();
//						if(tag instanceof ScrollView) {
//							ScrollView sv = (ScrollView) tag;
//							sv.scrollTo(0,  0);
//						}
//						d.show();
//					}
//				}
//			}
//			if(create){
//				d.dismiss();
//			}
//		}
//		if(create){
//			ViewGroup dv = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_about_star,null);
//
//			dv.findViewById(R.id.about_popIvBack).setOnClickListener(this);
//			dv.findViewById(R.id.about_popLstDict).setOnClickListener(this);
//			dv.findViewById(R.id.about_popNxtDict).setOnClickListener(this);
//
//			ftv = dv.findViewById(R.id.subtext);
//			ftv.mRatingDrawable = getRatingDrawable();
//			ftv.setCompoundDrawables(mActiveDrawable, null, null, null);
//
//			XYTouchRecorder xyt = opt.XYTouchRecorderInstance();
//			ftv.setOnTouchListener(xyt);
//			ftv.setOnClickListener(xyt);
//
//			TextView title = dv.findViewById(R.id.title);
//			title.setText("词典信息");
//			tv = dv.findViewById(R.id.resultN);
//			if(GlobalOptions.isLarge) tv.setTextSize(tv.getTextSize());
//			tv.setTextIsSelectable(true);
//
//			tv.setMovementMethod(LinkMovementMethod.getInstance());
//			FlowTextView finalFtv = ftv;
//			d = new android.app.AlertDialog.Builder(this)
//					.setView(dv)
//					.setOnDismissListener(dialog -> {
//						CheckStars(finalFtv.getStarLevel());
//						StarLevelStamp = -1;
//						this.d=null;
//					})
//					.create();
//			dv.findViewById(R.id.cancel).setOnClickListener(this);
//			window = d.getWindow();
//			window.setDimAmount(0);
//			tv.setTag(ftv);
//			ftv.setTag(dv.getChildAt(1));
//			d.setCanceledOnTouchOutside(true);
//			d.show();
//			window.getDecorView().setTag(tv);
//			if(GlobalOptions.isDark) {
//				window.getDecorView().setBackgroundColor(0xff333333);
//				ftv.setTextColor(Color.WHITE);
//				tv.setTextColor(Color.WHITE);
//			}
//			this.d = d;
//		}
//
//		CurrentDictInfoIdx = id;
//
//		ftv.setStarLevel(StarLevelStamp = md_get_StarLevel(id));
//
//		ftv.setText(md_getName(id, -1));
//
//		tv.setText(md_getAbout_Trim(id));
	}
	
	private void CheckStars(int newLevel) {
		int oldId = CurrentDictInfoIdx;
		if(oldId>=0&&oldId<loadManager.md_size&&StarLevelStamp>=0&&newLevel!=StarLevelStamp) {
			loadManager.md_set_StarLevel(oldId, newLevel);
			if(dictPicker !=null) {
				dictPicker.adapter().notifyItemChanged(oldId);
				//pickDictDialog.notifyDataSetChanged();
			}
		}
	}
	
	/** see{@link #collectDisplayingBooks} */
	public String retrieveDisplayingBooks(String books) {
		String ret = "";
		if (!TextUtils.isEmpty(books)) {
			String[] booksArr = books.split(";");
			int cc=0;
			boolean needDunhao = false;
			for (int i = 0; i < booksArr.length && cc<15; i++) {
				if (needDunhao) {
					ret += "、 ";
					needDunhao = false;
				}
				try {
					String bookName = getBookNameById(Long.parseLong(booksArr[i]));
					if (bookName!=null) {
						if (bookName.endsWith(".mdx")) {
							bookName = bookName.substring(0, bookName.length()-4);
						}
						bookName = bookName.replaceAll("\\(.*\\)", "");
					}
					ret += bookName;
					cc++;
					needDunhao = true;
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		}
		return ret;
	}
	
	private String getBookNameById(long bid) {
		UniversalDictionaryInterface impl = BookPresenter.bookImplsMap.get(bid);
		if (impl!=null) {
			return impl.getDictionaryName();
		}
		return prepareHistoryCon().getBookName(bid);
	}
	
	
	/** see{@link #retrieveDisplayingBooks} todo 排除 empty 限制最大长度 */
	public String collectDisplayingBooks(String books, WebViewListHandler wlh) {
		String ret=books==null?"":books;
		if (wlh.isViewSingle()) {
			WebViewmy wv = wlh.getWebContext();
			String url = wv.getUrl();
			if (url!=null) {
				wv.recUrl(url);
				long bid = -1;
				if(wv.mdbr) {
					if (wv.merge) {
						for (BookPresenter bp:wv.frames) {
							String thisIs = bp.getId() + ";";
							if (books!=null) {
								if (!ret.startsWith(thisIs) && !ret.contains(";"+thisIs)) {
									ret += thisIs;
								}
							} else {
								ret += thisIs;
							}
						}
					}
					else {
						int schemaIdx = url.indexOf(":");
						if (url.regionMatches(schemaIdx + 12, "content", 0, 7)) {
							int idx = schemaIdx + 12 + 7 + 1, ed=url.indexOf("_", idx);
							//if(!wv.presenter.idStr.regionMatches(0, url, idx, ed-idx))
							bid = getMdictServer().getBookIdByURLPath(url, idx, ed);
						}
						else if (url.regionMatches(schemaIdx + 12, "base", 0, 4)) {
							int idx = schemaIdx + 12 + 5, ed=url.indexOf("/", idx+1);
							if (url.charAt(idx) == '/') { // base/d0/entry/...
								//if(!wv.presenter.idStr.regionMatches(0, url, idx, ed-idx))
								bid = getMdictServer().getBookIdByURLPath(url, idx+1, ed);
							}
						}
					}
				}
				else {
					// 检查在线词典
					BookPresenter webx = webxford.get(SubStringKey.new_hostKey(url)); //todo opt
					if (webx!=null && webx.isWebx) {
						bid = webx.getId();
					}
				}
				if (bid!=-1) {
					String thisIs = bid + ";";
					if (books!=null) {
						if (!ret.startsWith(thisIs) && !ret.contains(";"+thisIs)) {
							ret += thisIs;
						}
					} else {
						ret += thisIs;
					}
				}
			}
		}
		else {
			ViewGroup webholder = wlh.getViewGroup();
			if (webholder!=null) {
				for (int i = 0, len=webholder.getChildCount(); i < len; i++) {
					View child = webholder.getChildAt(i);
					if (child!=null) {
						WebViewmy wv = child.findViewById(R.id.webviewmy);
						if (wv!=null) {
							if (books!=null) {
								String thisIs = wv.presenter.bookImpl.getBooKID() + ";";
								if (!ret.startsWith(thisIs) && !ret.contains(";"+thisIs)) {
									ret += thisIs;
								}
							} else {
								ret += wv.presenter.bookImpl.getBooKID() + ";";
							}
						}
					}
				}
			}
		}
		//CMN.debug("collectDisplayingBooks::ret", ret);
		return ret;
	}
	
	public BookPresenter getBookByName(String name) {
		Long bid = BookPresenter.bookImplsNameMap.get(name);
		if(bid==null) {
			if (getUsingDataV2()) {
				bid = prepareHistoryCon().getBookID(null, name);
			}
		}
		return getBookById(bid);
	}
	
	public BookPresenter getBookById(long bid) {
		BookPresenter ret = null;
		try {
			if (getUsingDataV2()) {
				String fileName = null;
				UniversalDictionaryInterface impl = BookPresenter.bookImplsMap.get(bid);
				try {
//					CMN.Log("getDictionaryById::", bid, impl, currentDictionary.bookImpl.getDictionaryName(),
//							prepareHistoryCon().getBookID(null, currentDictionary.bookImpl.getDictionaryName())
//							, currentDictionary.bookImpl.getBooKID());
				} catch (Exception e) {
					CMN.debug(e);
				}
				if (impl!=null) {
					fileName = impl.getFile().getPath();
				} else {
					fileName = prepareHistoryCon().getBookPath(bid);
				}
				if (fileName!=null) {
					ret = new_book(fileName, this);
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		if(ret==null)
			ret=EmptyBook;
		return ret;
	}
	
	public boolean getHasVidxByIdNoCreation(long bid) {
		try {
			UniversalDictionaryInterface impl = BookPresenter.bookImplsMap.get(bid);
			if (impl!=null) {
				return impl.hasVirtualIndex();
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return false;
	}
	
	public BookPresenter getBookByIdNoCreation(long bid) {
		BookPresenter ret = null;
		try {
			if (getUsingDataV2()) {
				String fileName = null;
				UniversalDictionaryInterface impl = BookPresenter.bookImplsMap.get(bid);
				try {
//					CMN.Log("getDictionaryById::", bid, impl, currentDictionary.bookImpl.getDictionaryName(),
//							prepareHistoryCon().getBookID(null, currentDictionary.bookImpl.getDictionaryName())
//							, currentDictionary.bookImpl.getBooKID());
				} catch (Exception e) {
					CMN.debug(e);
				}
				if (impl!=null) {
					fileName = impl.getFile().getPath();
					if (fileName!=null) {
						ret = new_book(fileName, this);
					}
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		if(ret==null)
			ret=EmptyBook;
		return ret;
	}
	
	public String getBookNameByIdNoCreation(long bid) {
		try {
			if (getUsingDataV2()) {
				String fileName = null;
				UniversalDictionaryInterface impl = BookPresenter.bookImplsMap.get(bid);
				try {
//					CMN.Log("getDictionaryById::", bid, impl, currentDictionary.bookImpl.getDictionaryName(),
//							prepareHistoryCon().getBookID(null, currentDictionary.bookImpl.getDictionaryName())
//							, currentDictionary.bookImpl.getBooKID());
				} catch (Exception e) {
					CMN.debug(e);
				}
				if (impl!=null) {
					return impl.getFile().getPath();
				}
				return prepareHistoryCon().getBookName(bid);
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return null;
	}
	
	private void putCurrFavoriteNoteBookId(long value) {
		opt.putCurrFavoriteNoteBookId(value);
		prepareHistoryCon().setFavoriteFolderId(value);
	}
	
	public boolean GetIsFavoriteTerm(@NonNull String text) {
		return prepareFavoriteCon().GetIsFavoriteTerm(text, (!getUsingDataV2()||opt.getFavoritePerceptsAll())?-1:-2);
	}
	
	public void removeFavoriteTerm(String text) {
		prepareFavoriteCon().remove(text, (!getUsingDataV2()||opt.getFavoritePerceptsRemoveAll())?-1:-2);
	}
	
	public void putRecentBookMark(long bid, long nid, long pos) {
		final int UnitMax=5+12*3;
		String BKHistroryVagranter = opt.getString("bkHVgrts", "");
		int size = opt.getInt("bkHSize", 0);
		StringBuilder sb = new StringBuilder();
		NumberToText_SIXTWO_LE(bid, sb).append(",");
		NumberToText_SIXTWO_LE(nid, sb).append(",");
		String rec = NumberToText_SIXTWO_LE(pos, sb).append(";").toString();
		sb.append(BKHistroryVagranter.replace(rec, ""));
		while(size>20)
		{
			int newIdx=sb.lastIndexOf(";", sb.length()-1);
			if(newIdx==-1) break;
			sb.setLength(newIdx+1);
			size--;
		}
		opt.putter().putString("bkHVgrts", sb.toString()).putInt("bkHSize", size).apply();
	}
	
	public final class VerseKit implements OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
		boolean isWeb;
		BookPresenter invoker;
		WebViewmy mWebView;
		TextView mTextView;
		MdxDBHelper con;
		AlertDialog d;
		ImageView iv_switch;
		ImageView tools_lock;
		ImageView iv_settings;
		ImageView iv_color;
		String CurrentSelected="";
		boolean bNeedStopScrollEffect;
		public boolean bNeedClearTextSelection;
		private int last_switch_cl_id;
		private CircleCheckBox cb;
		/** 0=nothing <br/>
		 * 1=to pick 、long-repress to use. <br/>
		 * 2=to use 、 long-press to pick. */
		public int bPicking;
		
		VerseKit(){
			arrayTweakDict = new int[]{
				R.string.bmAdd
				,R.string.bookmarkL
				,R.string.annotL
				,R.string.dict_opt
				,R.string.f_scale_up
				,R.string.f_scale_down
			};
			arraySelUtils = new int[]{
				R.string.favor_sel
				,R.string.select_all
				,R.string.hi_color
				,R.string.highlight
				,R.string.dehighlight
				,R.string.underline
				,R.string.deunderline
				,R.string.share_1
				,R.string.send_dot
				,R.string.share_2
				,R.string.share_3
			};
			arraySelUtils2 = new int[]{
				R.string.favor_sel
				,R.string.send_inpage
				,R.string.tts
				,R.string.pop_sch
				,R.string.peruse_sch
				,R.string.send_etsch
				,R.string.fapp_name
				,R.string.share_4
				,R.string.send_dot
				,R.string.share_5
				,R.string.share_6
			};
			arrayTextUtils = new int[]{
				R.string.favor_sel
				,R.string.send_inpage
				,R.string.tts
				,R.string.pop_sch
				,R.string.peruse_sch
				,R.string.send_etsch
				,R.string.fapp_name
				,R.string.share_1
				,R.string.send_dot
				,R.string.share_2
				,R.string.share_3
			};
			lastInDark = GlobalOptions.isDark;
		}
		
		public void setInvoker(BookPresenter presenter, WebViewmy _mWebView, TextView _tv, String text) {
			invoker=presenter;
			mWebView=_mWebView;
			mTextView =_tv;
			if (bPicking!=0) bPicking=0;
			isWeb = invoker!=null && invoker.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB;
			if(_tv!=null){
				int start = _tv.getSelectionStart();
				int end = _tv.getSelectionEnd();
				if(start*end>=0) {
					if(start>end){
						int startTmp = start;
						start=end;
						end = startTmp;
					}
					CurrentSelected = ViewUtils.getTextInView(_tv).subSequence(start, end).toString().trim();
				} else {
					CurrentSelected = StringUtils.EMPTY;
				}
				bFromTextView=true;
			}
			else if(!TextUtils.isEmpty(text)){
				CurrentSelected=text;
				bFromTextView=true;
			} else {
				CurrentSelected=null;
				bFromTextView=false;
			}
			hardSearchKey = CurrentSelected;
		}
		//boolean bResposibleForCon=false;
		//boolean doCloseOnDiss=true;
		int[] arrayTweakDict;
		public int[] arraySelUtils;
		public int[] arraySelUtils2;
		int[] arrayTextUtils;
		public boolean bFromWebView;
		boolean bFromTextView;
		boolean bLastFromWebView;
		ViewGroup bottomView;
		RecyclerView twoColumnView;
		TwoColumnAdapter twoColumnAda;
		boolean lastInDark;
		protected ObjectAnimator objectAnimator;
		int lastBookMarkPosition;
		/** @param v widget view. Or null to detect webview selection; none null to trust webview selection. */
		@Override
		public void onClick(View v) {
			int id;
			if(v!=null)
			switch(id=v.getId()) {
				case R.id.iv_switch:{
					if(bFromWebView){
						boolean val = opt.setToTextShare(!opt.getToTextShare());
						iv_switch.setColorFilter(getResources().getColor(val?R.color.DeapDanger:R.color.ThinHeaderBlue), PorterDuff.Mode.SRC_IN);
						if(twoColumnAda!=null){
							twoColumnAda.setItems(val? arraySelUtils2 : arraySelUtils);
						}
					} else {
						boolean val = opt.setToTextShare2(!opt.getToTextShare2());
						iv_switch.setColorFilter(getResources().getColor(val?R.color.colorAccent:R.color.ThinAccent), PorterDuff.Mode.SRC_IN);
						if(twoColumnAda!=null){
							twoColumnAda.setItems(val? arrayTextUtils : arraySelUtils2);
						}
					}
				} return;
				case R.id.color:{
					String msg;
					if(invoker.getUseInternalBG()) {
						//正在为词典  <![CDATA[<%1$s>]]> 指定背景颜色...
						msg=getResources().getString(R.string.BGMSG,invoker.getDictionaryName());
					}
					else {
						msg=getResources().getString(R.string.BGMSG2);
					}
					//DialogSnack(d,msg);
					root.post(() -> showTopSnack(null, msg, 0.8f, -1, -1, 0x4));
					
					d.hide();
					ColorPickerDialog asd =
							ColorPickerDialog.newBuilder()
									.setDialogId(123123)
									.setInitialColor(invoker.getUseInternalBG()?invoker.getContentBackground():GlobalPageBackground)
									.create();
					asd.setColorPickerDialogListener(new ColorPickerDialogListener() {
						@Override
						public void onColorSelected(ColorPickerDialog dialogInterface, int color) {
							//CMN.Log("onColorSelected");
							if(invoker.getUseInternalBG()) {
								invoker.setContentBackground(color);
								invoker.saveStates(MainActivityUIBase.this, prepareHistoryCon());
							} else {
								CMN.debug("应用全局颜色变更中…");
								opt.putGlobalPageBackground(GlobalPageBackground=color);
							}
							int ManFt_invoker_bgColor=invoker.getContentBackground();
							int ManFt_GlobalPageBackground=GlobalPageBackground;
							if(GlobalOptions.isDark) {
								ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web);
								ManFt_GlobalPageBackground=ColorUtils.blendARGB(ManFt_GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
							};
							//boolean apply = true;//bFromPeruseView || contentUIData.browserWidget12.getTag(R.id.image)==null;
							WebViewmy mWebView= VerseKit.this.mWebView;
							if (mWebView != null) {
								if(invoker.getUseInternalBG()) {
									invoker.saveStates(MainActivityUIBase.this, prepareHistoryCon());
									mWebView.setBackgroundColor(ManFt_invoker_bgColor);
								} else {
									webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									mWebView.weblistHandler.setBackgroundColor(ManFt_GlobalPageBackground);
									mWebView.weblistHandler.contentUIData.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									if(Build.VERSION.SDK_INT<21)
										mWebView.setBackgroundColor(ManFt_GlobalPageBackground);
								}
							}
						}
						@Override
						public void onPreviewSelectedColor(ColorPickerDialog dialogInterface, int color) {
							if (GlobalOptions.isDark)
								color = ColorUtils.blendARGB(color, Color.BLACK, ColorMultiplier_Web);
							WebViewmy mWebView= VerseKit.this.mWebView;
							if (mWebView != null) {
								if (invoker.getUseInternalBG()) {
									mWebView.setBackgroundColor(color);
								} else {
									mWebView.weblistHandler.contentUIData.webSingleholder.setBackgroundColor(color);
									if (Build.VERSION.SDK_INT<21 && mWebView != null)
										mWebView.setBackgroundColor(color);
								}
							}
						}
						@Override
						public void onDialogDismissed(ColorPickerDialog dialogInterface, int color) {
							CMN.debug("onDialogDismissed");
							d.show();
							WebViewmy mWebView= VerseKit.this.mWebView;
							int ManFt_invoker_bgColor=invoker.getContentBackground();
							int ManFt_GlobalPageBackground=GlobalPageBackground;
							if(GlobalOptions.isDark) {
								ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web);
								ManFt_GlobalPageBackground=ColorUtils.blendARGB(ManFt_GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
							}
							if (!isDirty && mWebView!=null) { //fall back
								if(invoker.getUseInternalBG()) {
									mWebView.setBackgroundColor(ManFt_invoker_bgColor);
								} else {
									webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									weblistHandler.setBackgroundColor(ManFt_GlobalPageBackground);
									mWebView.weblistHandler.contentUIData.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									if(Build.VERSION.SDK_INT<21)
										mWebView.setBackgroundColor(ManFt_GlobalPageBackground);
								}
							}
						}
						boolean isDirty;
					});
					asd.show(getSupportFragmentManager(),"color-picker-dialog");
				} return;
				case R.id.settings:{
					BookPresenter.showDictTweaker(weblist.dictView, MainActivityUIBase.this, invoker);
				} return;
				case R.id.appsettings:{
					showAppTweaker();
				} return;
				case R.id.lock:{
					opt.setTurnPageEnabled(!opt.getTurnPageEnabled());
					boolean enabled = opt.getTurnPageEnabled();
					SearchUI.tapZoomV++;
					tools_lock.setImageResource(enabled?R.drawable.un_locked:R.drawable.locked);
					opt.putFirstFlag();
					showTopSnack(null, enabled?R.string.PT1:R.string.PT2
							, 0.8f, LONG_DURATION_MS, -1, 0);
				} return;
				case R.id.check1: //tofo checker
				case R.id.check2:
				case R.id.check3: {
					CircleCheckBox cb = (CircleCheckBox) v;
					cb.toggle();
					boolean val = cb.isChecked();
					if(id==R.id.check1) {
						if(thisActType==ActType.MultiShare) {
							opt.setPinVSDialog(val);
							showT(val?"钉住面板":"使用一次后退出");
						}
						else  opt.setPinDialog(val);
					} else if(id==R.id.check2){
						opt.setRememberVSPanelGo(val);
						showT(val?"记忆最近使用项，今后直接跳转":"每一次都重新进入面板");
					} else {
						opt.setVSPanelGOTransient(val);
					}
				} return;
			}
			hideKeyboard();
			if(!bFromTextView){
				if(bPicking!=0) bFromWebView=true;
			}
			// nimp
			//if (!bFromWebView && mWebView!=null && invoker instanceof bookPresenter_pdf) {
			//	mWebView.evaluateJavascript(PDFPage, value -> {
			//		mWebView.currentPos = IU.parsint(value);
			//		build_further_dialog();
			//	});
			//} else
			{
				//build_further_dialog();
			}
			if (v != null || mWebView!=null) {
				if (v != null || mWebView.bIsActionMenuShown) {
					bFromWebView = true;
					build_further_dialog();
				} else if (v == null) {
					mWebView.evaluateJavascript("getSelection().isCollapsed", new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							bFromWebView = "false".equals(value);
							build_further_dialog();
						}
					});
				}
			} else {
				bFromWebView = false;
				build_further_dialog();
			}
		}
		
		private boolean hasText() {
			return bFromWebView||bFromTextView;
		}
		
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if(parent==d.getListView()&&(bFromWebView||bFromTextView)){
				return true;
			}
			return onItemClick(parent, view, position, id, true, true);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			onItemClick(parent, view, position, id, false, true);
		}

		public boolean onItemClick(AdapterView<?> parent, @Nullable View view, int position, long id, boolean isLongClicked, boolean isUserClick) {
			if(position<0) return true;
			int dissmisstype=0;
			try {
				//if(isLongClicked) CMN.Log("长按开始……");
				if(!hasText()) {
					WebViewmy tkWebv = mWebView;
					if(tkWebv==null) tkWebv=invoker.mWebView;
					if(tkWebv==null) {
						showT("错误!!! 网页找不到了");
						return true;
					}
					switch ((int) id) {
						/* 书签 */
						case R.string.bmAdd: {
							if (isLongClicked) return false;
							if (getUsingDataV2()) {
								invoker.toggleBookMark(tkWebv, new OnClickListener(){
									@Override
									public void onClick(View v) {
										statHasBookmark();
									}
								}, false);
							} else {
								showT("已弃用旧版数据库，请尽快升级。");
							}
							if (mBookMarkAdapter != null) { // todo why
								mBookMarkAdapter.clear();
							}
						} break;
						/* 书签列表 */
						case R.string.bookmarkL: {
							if (isLongClicked) return false;
							//stst = System.currentTimeMillis();
							//ArrayList<Integer> result = new ArrayList<>();

							//Cursor cr;
							//cr = con.getDB().rawQuery("select * from t1 ", null);
							//SparseArray<String> DataRecord = new SparseArray<>();

							//cr = con.getDB().query("t1", null,null,null,null,null,"path");
							//while(cr.moveToNext()){
							//	result.add(0,cr.getInt(0));
							//}
							//cr.close();
							if (con == null && !getUsingDataV2()) {
								showT("没有书签", 0);
								break;
							}
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUIBase.this, lastInDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line);//,R.style.DialogStyle2Line);
							builder.setTitle(invoker.appendCleanDictionaryName(MainStringBuilder).append(" -> ").append(getString(R.string.bm)).toString());
							builder.setItems(new String[]{}, null);
							builder.setNeutralButton(R.string.delete, null);
							AlertDialog d = builder.show();
							ListView list = d.getListView();
							list.setAdapter(getUsingDataV2()?getBookMarkAdapterV2(lastInDark, invoker):getBookMarkAdapter(lastInDark, invoker, con));
							//d.getListView().setFastScrollEnabled(true);

							//tofo
							if (lastBookMarkPosition != -1) list.setSelection(lastBookMarkPosition);
							
							list.setOnItemClickListener((parent1, view1, position1, id1) -> {
								if (prepareHistoryCon().testDBV2) {
									BookmarkAdapter.BookmarkDatabaseReader reader = (BookmarkAdapter.BookmarkDatabaseReader) mBookMarkAdapter.getItem(position1);
									if (reader!=null) {
										WebViewmy webview = this.mWebView;
										if (invoker.getIsWebx()) {
											String url = reader.entryName;
											if (url.startsWith("/")) {
												url = ((PlainWeb)webview.presenter.bookImpl).getHost()+url;
											}
											webview.loadUrl(url);
										} else {
											UniversalDictionaryInterface book = webview.presenter.bookImpl;
											int pos = reader.position;
											if (!TextUtils.equals(mdict.processText(reader.entryName), mdict.processText(book.getEntryAt(pos)))) {
												int tmp_pos = book.lookUp(reader.entryName);
												if (tmp_pos>=0) {
													pos = tmp_pos;
												}
											}
											myWebClient.shouldOverrideUrlLoading(webview, "entry://@" + pos);
										}
										//opt.putString("bkmk", invoker.f().getAbsolutePath() + "/?Pos=" + id11);
										//if(!cb.isChecked())
									}
									// todo notify error
								} else {
									showT("已弃用旧版数据库，请尽快升级。");
								}
								d.dismiss();
							});
							list.addOnLayoutChangeListener(MainActivityUIBase.mListsizeConfiner.setMaxHeight((int) (root.getHeight() - root.getPaddingTop() - 2.8 * getResources().getDimension(R.dimen._50_))));
							//d.getListView().setTag(con);
							d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v13 -> {
								mBookMarkAdapter.showDelete = !mBookMarkAdapter.showDelete;
								mBookMarkAdapter.notifyDataSetChanged();
							});
							d.setOnDismissListener(dialog -> {
								lastBookMarkPosition = list.getFirstVisiblePosition();
								mBookMarkAdapter.clear();
							});
						} break;
						/* 笔记列表 */
						case R.string.annotL: {
							if (isLongClicked) return false;
							if (!getUsingDataV2()) {
								showT("仅支持数据库v2", 0);
								break;
							}
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUIBase.this, lastInDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line);//,R.style.DialogStyle2Line);
							builder.setTitle(invoker.appendCleanDictionaryName(MainStringBuilder).append(" -> ").append(getString(R.string.bm)).toString());
							builder.setItems(new String[]{}, null);
							//builder.setNeutralButton(R.string.delete, null);
							AlertDialog d = builder.show();
							ListView list = d.getListView();
							list.setAdapter(getAnnotationAdapter(lastInDark, invoker));
							//d.getListView().setFastScrollEnabled(true);
							list.setOnItemClickListener((parent1, view1, position1, id1) -> {
								if (prepareHistoryCon().testDBV2) {
									AnnotAdapter.AnnotationReader reader = (AnnotAdapter.AnnotationReader) mAnnotAdapter.getItem(position1);
									if (reader!=null) {
										WebViewmy webview = this.mWebView;
										if (invoker.getIsWebx()) {
											String url = reader.entryName;
											if (url.startsWith("/")) {
												url = ((PlainWeb)webview.presenter.bookImpl).getHost()+url;
											}
											webview.loadUrl(url);
										} else {
											UniversalDictionaryInterface book = webview.presenter.bookImpl;
											int pos = (int) reader.position;
											if (!TextUtils.equals(mdict.processText(reader.entryName), mdict.processText(book.getEntryAt(pos)))) {
												int tmp_pos = book.lookUp(reader.entryName);
												if (tmp_pos>=0) {
													pos = tmp_pos;
												}
											}
//											webview.expectedPos = pagerec.y;
//											webview.expectedPosX = pagerec.x;
											myWebClient.shouldOverrideUrlLoading(webview, "entry://@" + pos);
										}
										//opt.putString("bkmk", invoker.f().getAbsolutePath() + "/?Pos=" + id11);
										//if(!cb.isChecked())
									}
									// todo notify error
								}
								d.dismiss();
							});
							
							list.addOnLayoutChangeListener(MainActivityUIBase.mListsizeConfiner.setMaxHeight((int) (root.getHeight() - root.getPaddingTop() - 2.8 * getResources().getDimension(R.dimen._50_))));
							//d.getListView().setTag(con);
						} break;
						/* 词典设置 */
						case R.string.dict_opt:{
							showBookPreferences(invoker);
						} break;
						/* 文字缩放级别 */
						case R.string.f_scale_up:
						case R.string.f_scale_down: {
							boolean inter = invoker.getUseInternalFS() && 1!=tkWebv.weblistHandler.bMergingFrames;
							if (isLongClicked) {
								int targetLevel = invoker.getFontSize();
								if (id==R.string.f_scale_up) {
									if (targetLevel < BookPresenter.optimal100) {
										tkWebv.getSettings().setTextZoom(targetLevel = BookPresenter.optimal100);
									} else if (targetLevel < 500) {
										tkWebv.getSettings().setTextZoom(targetLevel = 500);
									} else
										targetLevel = -1;
								} else {
									if (targetLevel > BookPresenter.optimal100) {
										tkWebv.getSettings().setTextZoom(targetLevel = BookPresenter.optimal100);
									} else if (targetLevel > 10) {
										tkWebv.getSettings().setTextZoom(targetLevel = 10);
									} else
										targetLevel = -2;
								}
								if (targetLevel > 0) {
									if (inter) {
										showT((invoker.internalScaleLevel = targetLevel) + "%", 0);
										invoker.saveStates(MainActivityUIBase.this, prepareHistoryCon());
									} else {
										notifyGLobalFSChanged(targetLevel);
										showT("(全局) " + (targetLevel) + "%", 0);//"(全局) "
									}
								} else if (targetLevel == -2)
									showT("min scale level", 0);
								else
									showT("max level reached", 0);
								return true;
							} else {
								int targetLevel = inter ? tkWebv.getSettings().getTextZoom() : BookPresenter.def_fontsize;
								if (id==R.string.f_scale_up) targetLevel += 10;
								else targetLevel -= 10;
								targetLevel = targetLevel > 500 ? 500 : targetLevel;
								targetLevel = targetLevel < 10 ? 10 : targetLevel;
								tkWebv.getSettings().setTextZoom(targetLevel);
								if (inter) {
									showT((invoker.internalScaleLevel = targetLevel) + "%", 0);
									invoker.saveStates(MainActivityUIBase.this, prepareHistoryCon());
								} else {
									notifyGLobalFSChanged(targetLevel);
									showT("(" + getResources().getString(R.string.GL) + ") " + (targetLevel) + "%", 0);//全局
								}
							}
						} break;
					}
				}
				else {
					if (bPicking!=0) {
						if (bPicking==1 ^ isLongClicked) {
							// to pick
							int idx = ArrayUtils.indexOf(arraySelUtils, (int) id);
							if(idx==-1) {
								idx = ArrayUtils.indexOf(arraySelUtils2, (int) id);
								if(idx==-1) idx = 0;
								else {
									showTopSnack(arraySelUtils2[idx]);
									idx+=arraySelUtils.length;
								}
							} else {
								showTopSnack(arraySelUtils[idx]);
							}
							PDICMainAppOptions.toolsQuickAction(idx);
							d.dismiss();
							return true;
						}
						// to use ...
						isLongClicked = false;
					}
					if ((id&0xffff0000)==0) {
						if (id<arraySelUtils.length) {
							id = arraySelUtils[(int) id];
						}
						else {
							id -= arraySelUtils.length;
							if (id<arraySelUtils2.length) {
								id = arraySelUtils2[(int) id];
							}
						}
					}
					PreferredToolId = (int) id;
					if(thisActType==ActType.MultiShare && opt.getRememberVSPanelGo()
							&& isUserClick && !isLongClicked) {
						opt.putLastVSGoNumber(PreferredToolId);
					}
					switch (PreferredToolId) {//xx
						/* 收藏 */
						case R.string.favor_sel: {
							// to impl
							if (bFromTextView) {
								if (CurrentSelected.length() > 0 && prepareFavoriteCon().insert(MainActivityUIBase.this, CurrentSelected, -1, null) > 0)
									showT(CurrentSelected + " 已收藏");
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										if (prepareFavoriteCon().insert(MainActivityUIBase.this, StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)), -1, mWebView.weblistHandler) > 0)
											showT(word + " 已收藏");
									}
								});
							}
							if(thisActType==ActType.MultiShare) {
								checkMultiVSTGO();
							}
						} break;
						/* 全选 */
						case R.string.select_all: {
							if (isLongClicked) {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										ReadText(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)), mWebView);
									}
								});
								return true;
							} else {
								mWebView.evaluateJavascript(WebViewmy.SelectAll, null);
							}
						} break;
						/* 颜色 */
						case R.string.hi_color:
							break;
						/* 高亮 */
						case R.string.highlight: {
							Annot(mWebView, R.string.highlight);
						} break;
						/* 清除高亮 */
						case R.string.dehighlight: {
							mWebView.evaluateJavascript(mWebView.getDeHighLightIncantation().toString(), null);
						} break;
						/* 下划线 */
						case R.string.underline: {
							Annot(mWebView, R.string.underline);
						} break;
						/* 清除下划线 */
						case R.string.deunderline: {
							mWebView.evaluateJavascript(mWebView.getDeUnderlineIncantation().toString(), null);
						} break;
						case R.string.send_dot:
						case R.string.share_1:
						case R.string.share_2:
						case R.string.share_3:
						case R.string.share_4:
						case R.string.share_5:
						case R.string.share_6:
						{
							if(execVersatileShare(isLongClicked, PreferredToolId)) {
								return true;
							}
						} break;
						/* 页内搜索 */
						case R.string.send_inpage: {
							if (isLongClicked) return false;
							if (bFromTextView) {
								if (CurrentSelected.length() > 0)
									HandleLocateTextInPage(CurrentSelected);
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										HandleLocateTextInPage(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)));
									}
								});
							}
							dissmisstype=1;
						} break;
						/* TTS */
						case R.string.tts: {
							if (isLongClicked) return false;
							if (bFromTextView) {
								if (CurrentSelected.length() > 0)
									ReadText(CurrentSelected, null);
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										ReadText(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)), mWebView);
									}
								});
							}
							if(thisActType==ActType.MultiShare) {
								checkMultiVSTGO();
							}
							dissmisstype=2;
						} break;
						/* 点译 */
						case R.string.pop_sch: {
							if(thisActType==ActType.MultiShare) {
								populateDictionaryList();
							}
							if (isLongClicked) return false;
							if (bFromTextView) {
								if (CurrentSelected.length() > 0) {
									popupWord(CurrentSelected, null, -1, mWebView);
								}
								bNeedClearTextSelection=true;
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									mWebView.simulateScrollEffect();
									bNeedStopScrollEffect=true;
									if (word.length() > 2) {
										popupWord(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)), null, mWebView.frameAt, mWebView);
									}
								});
							}
							dissmisstype=1;
						} break;
						/* 翻阅模式 */
						case R.string.peruse_sch: {
							if(thisActType==ActType.MultiShare) {
								populateDictionaryList();
							}
							if (isLongClicked) return false;
							if (bFromTextView) {
								if (CurrentSelected.length() > 0)
									JumpToPeruseModeWithWord(CurrentSelected);
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										JumpToPeruseModeWithWord(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)));
									}
								});
							}
							dissmisstype=1;
						} break;
						/* 搜索框 */
						case R.string.send_etsch: {
							if (isLongClicked) return false;
							if(thisActType==ActType.MultiShare) {
								AgentApplication app = (AgentApplication) getApplication();
								if (app.floatApp != null && app.floatApp.isFloating()) {
									app.floatApp.getFloatBtn().search(extraText, false);
								} else {
									Intent newTask = new Intent(Intent.ACTION_MAIN);
									newTask.putExtra(Intent.EXTRA_TEXT, extraText);
									//newTask.putExtra(Intent.EXTRA_SHORTCUT_ID,ShareTarget);
									newTask.setClass(getBaseContext(),PDICMainActivity.class);
									newTask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(newTask);
								}
								dissmisstype=0;
								checkMultiVSTGO();
							} else {
								if (bFromTextView) {
									if (CurrentSelected.length() > 0)
										HandleSearch(CurrentSelected);
								} else {
									mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
										if (word.length() > 2) {
											HandleSearch(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)));
										}
									});
								}
								dissmisstype=1;
							}
						} break;
						/* 浮动搜索 */
						case R.string.fapp_name: {
							if (isLongClicked) return false;
							if (bFromTextView) {
								if (CurrentSelected.length() > 0)
									JumpToFloatSearch(CurrentSelected);
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										JumpToFloatSearch(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)));
									}
								});
							}
							dissmisstype=1;
							if(thisActType==ActType.MultiShare) {
								checkMultiVSTGO();
							}
						}
						break;
					}
				}
				if(d!=null) {
					if(cb!=null) {
						if(!cb.isChecked())
							dissmisstype=1;
						if(dissmisstype==1 || dissmisstype==2 && !getPinVSDialog()) {
							if(thisActType==ActType.MultiShare) {
								d.setOnDismissListener(null);
								d.dismiss();
								d.setOnDismissListener(MainActivityUIBase.this);
							} else {
								d.dismiss();
							}
						}
					}
				}
			} catch (Exception e){
				CMN.debug(e);
			}
			
			clearTextFocus();
			
			return false;
		}
		
		public void clearTextFocus() {
			TextView TV = mTextView;
			if(false)
			if(TV!=null && bNeedClearTextSelection) {
				TV.clearFocus();
				TV.setEnabled(false);
				TV.postDelayed(() -> TV.setEnabled(true), 350);
				bNeedClearTextSelection = false;
			}
		}
		
		private int mapVShareToPostion(int id) {
			switch (id) {
				case R.string.share_1:
					return 0;
				case R.string.share_2:
					return 2;
				case R.string.share_3:
					return 3;
				case R.string.share_4:
					return 11;
				case R.string.share_5:
					return 13;
				case R.string.share_6:
					return 14;
				default:
					return 1;
			}
		}
		
		boolean execVersatileShare(boolean isLongClicked, int id) {
			if (isLongClicked && id==R.string.send_dot) {
				return true;
			}
			int position = mapVShareToPostion(id);
			JSONObject json = opt.getDimensionalSharePatternByIndex(position);
			boolean putDefault = json == null;
			if (putDefault) {
				json = new JSONObject();
			} else {
				putDefault = json.has("b")||json.length()==0;
			}
			if (putDefault) {
				putDefaultSharePattern(json, id);
			}
			/* 将 json 散列为数组。 */
			ArrayList<String> data = new ArrayList<>(8);
			serializeSharePattern(json, data);
			if (!isLongClicked) {
				HandleShareIntent(data);
			}
			/* 对话框定义多维分享 */
			/* Customizable parts of MDCCSP ( from Share#0-Share#5 )*/
			else {
				Context context = MainActivityUIBase.this;
				AlertController.RecycleListView customList = new AlertController.RecycleListView(context);
				customList.mMaxHeight = (int) (root.getHeight() - root.getPaddingTop() - 3.8 * getResources().getDimension(R.dimen._50_));

				CustomShareAdapter csa = new CustomShareAdapter(data);
				customList.setAdapter(csa);
				customList.setDivider(null);
				
				AlertDialog.Builder builder2 = new AlertDialog.Builder(context, GlobalOptions.isDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line);
				builder2.setTitle("制定分享目标");
				builder2.setNeutralButton("添加字段", null);
				builder2.setNegativeButton("测试", null);
				builder2.setPositiveButton("保存", null);
				
				FrameLayout dv = new FrameLayout(context);
				/* 键盘能够弹出 */
				dv.addView(customList);
				dv.addView(new EditText(context), new LinearLayout.LayoutParams(0, 0));
				csa.nameWidth = (int) ((TextView) csa.getView(0, null, customList).findViewById(R.id.text2)).getPaint().measureText(getResources().getString(R.string.extra_key_value, 1001));
				builder2.setView(dv);
				
				AlertDialog dTmp = builder2.show();
				
				OnClickListener mClicker = new OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean isLongClicked = v.getTag(R.id.long_clicked) != null;
						switch (v.getId()) {
							case android.R.id.button1://+
								if (isLongClicked) {
									android.app.AlertDialog.Builder builder21 = new android.app.AlertDialog.Builder(getLayoutInflater().getContext());
									android.app.AlertDialog d1 = builder21.setTitle("确认删除并恢复默认值？")
											.setPositiveButton(R.string.confirm, (dialog, which) -> {
												opt.putDimensionalSharePatternByIndex(position, null);
												JSONObject json = new JSONObject();
												data.clear();
												putDefaultSharePattern(json, id);
												serializeSharePattern(json, data);
												csa.notifyDataSetChanged();
											})
											.create();
									d1.show();
								}
								else try {
									JSONObject neo = packoutNeoJson(data);
									JSONObject original = new JSONObject();
									putDefaultSharePattern(original, id);
									if (baseOnDefaultSharePattern(neo, original)) {
										neo = packoutNeoJson(data);
									}
									opt.putDimensionalSharePatternByIndex(position, neo);
									showT("保存成功！");
									dTmp.dismiss();
								}
								catch (Exception e) {
									CMN.debug(e);
									showT("保存失败！" + e);
								}
								break;
							case android.R.id.button2://-
								if (isLongClicked) break;
								HandleShareIntent(data);
								break;
							case android.R.id.button3://|
								if (isLongClicked) break;
								data.add(null);
								data.add(null);
								csa.notifyDataSetChanged();
								break;
						}
					}
				};
				
				Button btnTmp = dTmp.getButton(DialogInterface.BUTTON_POSITIVE);
				btnTmp.setOnClickListener(mClicker);
				btnTmp.setOnLongClickListener(new MultiplexLongClicker());
				dTmp.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(mClicker);
				dTmp.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(mClicker);
			}
			return false;
		}
		
		void HandleShareIntent(ArrayList<String> data) {
			if(bFromTextView){
				handleIntentShare(CurrentSelected, data);
				if(thisActType==ActType.MultiShare) {
					checkMultiVSTGO();
				}
			} else {
				if(data.contains(Intent.EXTRA_HTML_TEXT)){
					if(data.contains(Intent.EXTRA_TEXT)){
						mWebView.evaluateJavascript(WebViewmy.CollectWord, v -> {
							if (v.length() > 2) {
								v = StringEscapeUtils.unescapeJava(v.substring(1, v.length() - 1));
								data.add(Intent.EXTRA_TEXT);
								data.add(v);
								FetchTextOrHtmlThen_handleIntentShare(data, true);
							}
						});
					} else {
						FetchTextOrHtmlThen_handleIntentShare(data, true);
					}
				} else {
					FetchTextOrHtmlThen_handleIntentShare(data, false);
				}
			}
		}

		void FetchTextOrHtmlThen_handleIntentShare(ArrayList<String> data, boolean parseHtml) {
			String FetchWord = parseHtml ? WebViewmy.CollectHtml : WebViewmy.CollectWord;
			mWebView.evaluateJavascript(FetchWord, v -> {
				if (v.length() > 2) {
					v = StringEscapeUtils.unescapeJava(v.substring(1, v.length() - 1));
					//CMN.Log("Fetched Page Part : ", v);
					handleIntentShare(v, data);
				}
			});
		}

		protected void build_further_dialog(){
			boolean needRecreate=bLastFromWebView!=hasText();
			if(!bFromWebView && mWebView!=null) {
				statHasBookmark();
			}
			ListView dialogList;
			//CMN.Log("重建对话???", hasText());
			if(GlobalOptions.isDark!=lastInDark || d==null)
			{
				CMN.debug("重建对话框…");
				needRecreate=false;
				d = new AlertDialog.Builder(MainActivityUIBase.this
						,GlobalOptions.isDark?R.style.DialogStyle3Line
						:R.style.DialogStyle4Line)
						.setItems(ArrayUtils.EMPTY_STRING_ARRAY,null)
						.create();
				bottomView = (ViewGroup) getLayoutInflater().inflate(R.layout.checker2, dialogList = d.getListView(), false);
				
				if(thisActType==ActType.MultiShare) {
					opt.setVSPanelGOTransient(false);
					d.setOnDismissListener(MainActivityUIBase.this);
					int id=R.id.check2;
					decorateCheckBox((CircleCheckBox) ((ViewStub)bottomView.findViewById(id)).inflate(), opt.getRememberVSPanelGo(), 1.25f).setId(id);
					CircleCheckBox cb = decorateCheckBox((CircleCheckBox) ((ViewStub)bottomView.findViewById(id=R.id.check3)).inflate(), opt.getVSPanelGOTransient(), 2.5f);
					cb.setDrawable(0, mResource.getDrawable(R.drawable.ic_exit_app).mutate());
					cb.mHintSurrondingPad *= 0.15;
					cb.mHintSurrondingPad/=2;
					cb.setId(id);
					cb.setVisibility(View.GONE);
				}
				cb = decorateCheckBox(bottomView.findViewById(R.id.check1), getPinVSDialog(), 0);
				
				Object[] items = new Object[]{R.id.iv_switch, R.id.color, R.id.settings, R.id.lock};
				ViewUtils.setOnClickListenersOneDepth(bottomView, this, 999, 0, items);
				iv_switch = (ImageView) items[0];
				iv_color = (ImageView) items[1];
				iv_settings = (ImageView) items[2];
				tools_lock  = (ImageView) items[3];
				
				bottomView.setOnLongClickListener(v1 -> {
					View vv = d.getWindow().getDecorView();
					float ali = vv.getAlpha();
					if(objectAnimator!=null) objectAnimator.cancel();
					float target = ali < 0.9 ? 1 : (bFromWebView?0.5f:0.2f);
					objectAnimator = ObjectAnimator.ofFloat(vv,"alpha",ali,target);
					objectAnimator.setDuration(120);
					objectAnimator.start();
					return true;
				});
				
				if(contentUIData!=null && contentUIData.PageSlider!=null) {
					if(!opt.getTurnPageEnabled())
						tools_lock.setImageResource(R.drawable.locked);
				} else {
					tools_lock.setVisibility(View.GONE);
				}

				dialogList.addFooterView(bottomView);

				if(twoColumnAda!=null)  twoColumnAda.notifyDataSetChanged();
			}
			ViewUtils.ensureWindowType(d, MainActivityUIBase.this, MainActivityUIBase.this);
			
			int switch_cl_id=bFromWebView?(opt.getToTextShare()?R.color.DeapDanger:R.color.ThinHeaderBlue)
					:opt.getToTextShare2()?R.color.colorAccent:R.color.ThinAccent;
			
			if(switch_cl_id!=last_switch_cl_id) {
				iv_switch.setColorFilter(mResource.getColor(last_switch_cl_id=switch_cl_id), PorterDuff.Mode.SRC_IN);
			}

			iv_switch.setVisibility(hasText()?View.VISIBLE:View.GONE);

			int targetVis = bFromTextView?View.GONE:View.VISIBLE;
			tools_lock.setVisibility(targetVis);
			iv_settings.setVisibility(targetVis);
			iv_color.setVisibility(targetVis);

			dialogList = d.getListView();
			boolean toText = bFromTextView || opt.getToTextShare();
			int[] items = hasText()?
					toText ? (bFromTextView && opt.getToTextShare2()? arrayTextUtils : arraySelUtils2) : arraySelUtils
					:arrayTweakDict;
			if(twoColumnView==null) {
				RecyclerView footRcyView = new RecyclerView(bottomView.getContext());
				footRcyView.setClipToPadding(false);
				GridLayoutManager lman;
				footRcyView.setLayoutManager(lman = new GridLayoutManager(bottomView.getContext(), 2));
				lman.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
					@Override
					public int getSpanSize(int position) {
						if (!hasText()) return 2;
						if (position == 0) return 2;
						return 1;
					}
				});
				TwoColumnAdapter RcyAda = new TwoColumnAdapter(items);
				RcyAda.setOnItemClickListener(this);
				RcyAda.setOnItemLongClickListener(this);
				footRcyView.setAdapter(RcyAda);
				twoColumnAda = RcyAda;
				twoColumnView = footRcyView;
			}
			if(twoColumnView.getParent()!=dialogList){
				dialogList.removeFooterView(bottomView);
				dialogList.removeFooterView(twoColumnView);
				dialogList.addFooterView(twoColumnView);
				dialogList.addFooterView(bottomView);
			}
			twoColumnAda.setItems(items);
			
			// 设置标题
			if(!bFromTextView && invoker!=null) {
				StringBuilder sb = invoker.appendCleanDictionaryName(null);
				String text = mWebView==null?null:mWebView.word;
				if(!TextUtils.isEmpty(text)) {
					sb.append(" - ").append(text);
				}
				d.setTitle(sb.toString());
			} else {
				d.setTitle(R.string.text_operation);
			}
			
			ViewUtils.setVisibility(bottomView.findViewById(R.id.appsettings), false);
			
			if(d.getWindow()!=null) {
				d.getWindow().getAttributes().width = -2;
			}
			
			d.show();
			
			((ViewGroup)d.findViewById(R.id.action_bar_root).getParent()).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					double coord = 1.2*(bottom - top);
					if(right-left >= coord) {
						WindowManager.LayoutParams NaughtyDialogAttr = d.getWindow().getAttributes();
						NaughtyDialogAttr.width = (int) coord;
						d.getWindow().setAttributes(d.getWindow().getAttributes());
					}
				}
			});

			//if(getCurrentFocus()!=null && !(getCurrentFocus() instanceof WebView))
			//	getCurrentFocus().clearFocus();

			d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			
			lastInDark=GlobalOptions.isDark;

			bLastFromWebView=bFromWebView||bFromTextView;
			firstCreateUcc=false;
		}
		
		private void statHasBookmark() {
			int resId=R.string.bmAdd;
			if(getUsingDataV2()) {
				if(invoker.hasBookmark(mWebView)){
					resId=R.string.bmSub;
				}
			}
			else if(!bFromTextView) con = invoker.getCon(false);
			if (arrayTweakDict[0]!=resId) {
				arrayTweakDict[0] = resId;
				if (twoColumnAda!=null) {
					twoColumnAda.notifyItemChanged(0);
				}
			}
		}
		
		private CircleCheckBox decorateCheckBox(CircleCheckBox cb, boolean checked, float marginLeft) {
			if(GlobalOptions.isDark) cb.drawInnerForEmptyState=true;
			else cb.circle_shrinkage=GlobalOptions.density/3*2;
			cb.setChecked(checked, false);
			if(marginLeft>0) {
				((ViewGroup.MarginLayoutParams)cb.getLayoutParams()).leftMargin*=marginLeft;
			}
			return cb;
		}
		
		public boolean detached() {
			return d==null||!d.isShowing()|| ViewUtils.isWindowDetached(d.getWindow());
		}
		
		public void showDictTweaker(BookPresenter presenter, WebViewmy wv) {
			setInvoker(presenter, wv, null, null);
			onClick(null);
		}
	}
	
	public static long hashKey(String value) {
		long h = BookPresenter.hashCode(value, 0);
		h |= ((long)value.length())<<32;
		return h;
	}
	
	public static String digestKey(String value, int length) {
		return value.length()>length?value.substring(0, length):value;
	}
	
	public void Annot(WebViewmy mWebView, int type) {
		boolean record = mWebView.presenter.getRecordHiKeys();
		String js = (type==R.string.highlight?mWebView.getHighLightIncantation(record)
				:mWebView.getUnderlineIncantation(record)).toString();
		//CMN.Log("Annot_js=", js);
		mWebView.evaluateJavascript(js, record?new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				if(value!=null && value.length()>=3 && value.charAt(0)=='"')
				try {
					value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
					if (getUsingDataV2()) {
						String entry = mWebView.word;
						PlainWeb webx = mWebView.presenter.getWebx();
						if (webx!=null) {
							entry = mWebView.getUrl();
							if (entry.startsWith(webx.getHost()))
								entry = entry.substring(webx.getHost().length());
						}
						long entryHash = hashKey(entry);
						String lex = value;
						long lexHash = hashKey(lex);
						ContentValues values = new ContentValues();
						values.put("bid", mWebView.presenter.getId());
						values.put("entry", entry);
						values.put("entryHash", entryHash);
						values.put("entryDig", digestKey(entry, 3));
						values.put("lex", lex);
						values.put("lexHash", lexHash);
						values.put("lexDig", digestKey(lex, 2));
						values.put("pos", mWebView.currentPos);
						long now = CMN.now();
						values.put(LexicalDBHelper.FIELD_EDIT_TIME, now);
						values.put(LexicalDBHelper.FIELD_CREATE_TIME, now);
						JSONObject json = new JSONObject();
						json.put("x", mWebView.getScrollX());
						json.put("y", mWebView.getScrollY());
						json.put("s", mWebView.webScale);
						values.put(LexicalDBHelper.FIELD_PARAMETERS, json.toString().getBytes());
						prepareHistoryCon().getDB().insert(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, null, values);
						//showT(id+", "+value);
					}
				} catch (JSONException e) { CMN.debug(e); }
			}
		}:null);
	}
	
	public boolean getPinVSDialog() {
		return thisActType==ActType.MultiShare ?opt.getPinVSDialog():opt.getPinDialog();
	}
	
	private void checkMultiVSTGO() {
		CMN.debug("checkMultiVSTGO...", ((MultiShareActivity)MainActivityUIBase.this).NewIntentCalled , opt.getVSPanelGOTransient());
		if(((MultiShareActivity)MainActivityUIBase.this).NewIntentCalled && !getPinVSDialog()) {
			root.postDelayed(()->moveTaskToBack(false), 200);
			//moveTaskToBack(false);
		}
	}
	
	static int[] VersatileShareSlots = new int[]{7,9,10,18,20,21};
	
	public void execVersatileShare(String text, int id) {
		CMN.debug("execVersatileShare", id);
		id=0;
		if(id==0) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
			clipboard.setPrimaryClip(ClipData.newPlainText("PLOD", text));
			showT("已复制！");
			return;
		}
		if(id==1) {
			startActivity(new Intent(this, MultiShareActivity.class)
					.setFlags(SingleTaskFlags).putExtra(Intent.EXTRA_TEXT, text));
			return;
		}
		id-=2;
		if(id>=0&&id<=5)
		{
			getVtk().setInvoker(null, null, null, text);
			getVtk().execVersatileShare(false, VersatileShareSlots[id]);
		}
	}
	
	private JSONObject packoutNeoJson(ArrayList<String> data) throws JSONException {
		JSONObject neo = new JSONObject();
		neo.put("p", data.get(0));
		neo.put("m", data.get(1));
		neo.put("a", data.get(2));
		neo.put("t", data.get(3));
		for (int i = 4; i+1 < data.size(); i+=2) {
			if(data.get(i)!=null){
				String val = Integer.toString((i-4)/2+1);
				neo.put("k"+val, data.get(i));
				neo.put("v"+val, data.get(i+1));
			}
		}
		return neo;
	}

	boolean baseOnDefaultSharePattern(JSONObject neo, JSONObject original) {
		OUT:
		try {
			/* 只能多不能少 */
			boolean b1;
			if((b1 = original.has("p")) && !neo.has("p"))
				break OUT;
			if(b1) b1 = neo.getString("p").equals(original.getString("p"));
			boolean b2;
			if((b2 = original.has("m")) && !neo.has("m"))
				break OUT;
			if(b2) b2 = neo.getString("m").equals(original.getString("m"));
			boolean b3;
			if((b3 = original.has("a")) && !neo.has("a"))
				break OUT;
			if(b3) b3 = neo.getString("a").equals(original.getString("a"));
			boolean b4;
			if((b4 = original.has("t")) && !neo.has("t"))
				break OUT;
			if(b4) b4 = neo.getString("t").equals(original.getString("t"));
			int cc=(original.length()-4);
			ArrayList<String> duplicatedKeys = new  ArrayList<>(cc);
			cc=0;
			while (++cc>0){
				boolean b5;
				String key="k"+cc;
				b5 = original.has(key);
				if(!b5) break;
				if(!neo.has(key))
					break OUT;
				b5 = neo.getString(key).equals(original.getString(key));
				if(b5) duplicatedKeys.add(key);

				key="v"+cc;
				b5 = original.has(key);
				if(!b5) continue;
				if(!neo.has(key))
					break OUT;
				b5 = neo.getString(key).equals(original.getString(key));
				if(b5) duplicatedKeys.add(key);
			}
			if(b1) neo.remove("p");
			if(b2) neo.remove("m");
			if(b3) neo.remove("a");
			if(b4) neo.remove("t");
			for(String dkI:duplicatedKeys){
				neo.remove(dkI);
			}
			if(neo.length()>0){
				neo.put("b", 1);
			}
			CMN.debug("好哎！！！",neo.toString());
		} catch (Exception e) {
			return true;
		}
		return false;
	}

	void serializeSharePattern(JSONObject json, ArrayList<String> data) {
		for (int i = 0; i < 4; i++) data.add(null);
		try {
			if(json.has("p")) data.set(0, json.getString("p"));
			if(json.has("m")) data.set(1, json.getString("m"));
			if(json.has("a")) data.set(2, json.getString("a"));
			if(json.has("t")) data.set(3, json.getString("t"));
			int cc=0;
			while (++cc>0){
				String key="k"+cc;
				if(!json.has(key)) break;
				data.add(json.getString(key));
				String vI = "v"+cc;
				data.add(json.has(vI)?json.getString(vI):"%s");
			}
		} catch (JSONException e) {
			CMN.debug(e);
		}
	}

	void putDefaultSharePattern(JSONObject json, int id) {
		try {
			switch (id){
				/* 分享#2 */
				case R.string.share_2:
				/* 分享#1 */
				case R.string.share_1:
					if(!json.has("k1")) json.put("k1", "_data");
					if(!json.has("v1")) json.put("v1", id==R.string.share_2?"https://translate.google.cn/#view=home&op=translate&sl=auto&tl=zh-CN&text=%s":"https://www.baidu.com/s?wd=%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", id==R.string.share_2?"c/q":"/");
				break;
				/* 分享#3 */
				case R.string.share_3:
					if(!json.has("k1")) json.put("k1", "_data");
					if(!json.has("v1")) json.put("v1", "https://cn.bing.com/search?q=%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "/");
				break;
				case R.string.send_dot:
				case R.string.share_6:
					if(!json.has("k1")) json.put("k1", Intent.EXTRA_TEXT);
					if(!json.has("v1")) json.put("v1", "%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "c/q");
					if(!json.has("t")) json.put("t", "text/plain");
					if(!json.has("a")) json.put("a", Intent.ACTION_SEND);
				break;
				case R.string.share_4:
					if(!json.has("a")) json.put("a", getResources().getString(R.string.colorfuldayswithbeautifulgirl));
					if(!json.has("k1")) json.put("k1", EXTRA_QUERY);
					if(!json.has("v1")) json.put("v1", "%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "/");
				break;
				case R.string.share_5:
					if(!json.has("a")) json.put("a", Intent.ACTION_PROCESS_TEXT);
					if(!json.has("k1")) json.put("k1", Intent.EXTRA_PROCESS_TEXT);
					if(!json.has("v1")) json.put("v1", "%s");
					if(!json.has("t")) json.put("t", "text/plain");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "/");
				break;
			}
			if(!json.has("k3")) json.put("k3", "_flags");
			if(!json.has("v3")) json.put("v3", "n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void hideKeyboard() {
		View ht = getCurrentFocus();
		if(ht==null) ht = main;
		imm.hideSoftInputFromWindow(ht.getWindowToken(),0);
	}

	public void JumpToFloatSearch(String content) {
		Intent popup = new Intent().setClass(this, FloatActivitySearch.class).putExtra("EXTRA_QUERY", content);
		popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(popup);
	}

	boolean firstCreateUcc=true;
	public VerseKit ucc;
	
	public abstract void fix_full_screen(@Nullable View decorView);
	
	public static void fix_full_screen_global(@NonNull View decorView, boolean fullScreen, boolean hideNavigation) {
		int uiOptions = 0;
		if(fullScreen) uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		if(hideNavigation) uiOptions|=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_IMMERSIVE;
		if(uiOptions!=0){
			decorView.setSystemUiVisibility(uiOptions);
		}
	}
	
	BookmarkAdapter mBookMarkAdapter;
	public ListAdapter getBookMarkAdapter(boolean darkMode, BookPresenter invoker, MdxDBHelper con) {
		if(mBookMarkAdapter==null)
			mBookMarkAdapter=new BookmarkAdapter(this,R.layout.drawer_list_item,R.id.text1,invoker,con.getDB(),0,getUsingDataV2());
		else
			mBookMarkAdapter.refresh(invoker,con.getDB());
		mBookMarkAdapter.darkMode=darkMode;
		return mBookMarkAdapter;
	}
	
	public ListAdapter getBookMarkAdapterV2(boolean darkMode, BookPresenter invoker) {
		if(mBookMarkAdapter==null)
			mBookMarkAdapter=new BookmarkAdapter(this,R.layout.drawer_list_item,R.id.text1,invoker, prepareHistoryCon().getDB(),0,getUsingDataV2());
		else
			mBookMarkAdapter.refresh(invoker, prepareHistoryCon().getDB());
		mBookMarkAdapter.darkMode=darkMode;
		return mBookMarkAdapter;
	}
	
	private AnnotAdapter mAnnotAdapter;
	public ListAdapter getAnnotationAdapter(boolean darkMode, BookPresenter invoker) {
		if(mAnnotAdapter==null)
			mAnnotAdapter=new AnnotAdapter(this,R.layout.drawer_list_item,R.id.text1,invoker, prepareHistoryCon().getDB(),0);
		else
			mAnnotAdapter.refresh(invoker, prepareHistoryCon().getDB());
		mAnnotAdapter.darkMode=darkMode;
		return mAnnotAdapter;
	}

	@Override
	public void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val) {
		switch (processId){
			case R.string.relaunch:
			case R.string.warn_exit0: {
				if(opt.getDeletHistoryOnExit()) {
					deleteHistory();
				}
				boolean svm = opt.getShuntDownVMOnExit();
				if(processId==R.string.relaunch) {
					opt.setLastMdlibPath((String)SU.UniversalObject);
					SU.UniversalObject=null;
				} else if(svm) {
					if(checkFlagsChanged()) {
						opt.setFlags(null, 2);
					}
				}
				ViewUtils.CleanExitApp(this, false/*restart*/&&PDICMainAppOptions.getRestartVMOnExit(), opt.getClearTasksOnExit(), svm);
			} break;
			case R.string.shutdown_vm:
				checkFlags();
			break;
			case 0:
				if(drawerFragment!=null) {
					drawerFragment.sw1.setChecked(val==1);
				}
			break;
			case 1:
				if(drawerFragment!=null) {
					drawerFragment.sw2.setChecked(val==1);
				}
			break;
			case 2:
			case 25:
				if (val==1!=GlobalOptions.isDark) {
					toggleDarkMode();
				}
				//CMN.Log("黑暗？", val==1, opt.getInDarkMode(), GlobalOptions.isDark);
			break;
			case 3:
				if(drawerFragment!=null) {
					drawerFragment.sw5.setChecked(val==0);
				}
			break;
			case 4:
				locateNaviIcon();
			break;
			case 8:
				setGlobleCE(val==1);
			break;
			case 9:
				launchSettings(10, 0);
			break;
			case 10:
				toggleTTS();
			break;
			case 11:{
				if(TTSController_!=null){
					((CircleCheckBox)TTSController_bottombar.findViewById(R.id.ttsHighlight)).setChecked(opt.getTTSHighlightWebView(), false);
				}
			} break;
			case 12:{
				toggleClickSearch(opt.tapSch());
			} break;
			case 15:
				try {
					Intent intent = new Intent();
					intent.setAction("com.android.settings.TTS_SETTINGS");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					this.startActivity(intent);
				} catch (Exception ignored) { }
			break;
			/* 天运之子，层类无穷，衍生不尽！ */
			case 16:{
				showScrollSet();
			} break;
			/* 翻阅模式 */
			case 20:
				peruseView.entryAdapter.notifyDataSetChanged();
			break;
			case 21:
				peruseView.lv2.setFastScrollEnabled(val==1);
			break;
			case 22:
				peruseView.lv1.setFastScrollEnabled(val==1);
			break;
			case 23:
				peruseView.lv1.setFastScrollEnabled(val==1);
				peruseView.lv2.setFastScrollEnabled(val==1);
			break;
			case 24:
				peruseView.toggleSearchPage();
			break;
			case 26:
				peruseView.entryAdapter.notifyDataSetChanged();
				peruseView.bmsAdapter.notifyDataSetChanged();
			break;
		}
	}
	
	/** 滚动条 [在右/在左/无/系统滚动条] see {@link WebViewListHandler#resetScrollbar}
	 *<br>  see {@link BookPresenter.AppHandler#cs}
	 *<br>  see {@link QuickBookSettingsPanel#initScrollHandle}
	 * */
	public void showScrollSet() {
		String title = "设置网页滚动条 - ";
		int flagPos=0;
		WebViewmy wv = weblist.mWebView;
		if(wv!=null && wv==wordPopup.mWebView) {
			title += "点译模式";
			flagPos = 6;
		}
		else if(PeruseViewAttached() && wv==peruseView.mWebView){
			title += "翻阅模式";
			flagPos = 4;
		} else if(thisActType==ActType.FloatSearch){
			title+="浮动搜索";
			flagPos = 2;
		} else {
			title+="主程序";
			flagPos = 0;
		}
		androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(this);
		int finalFlagPos = flagPos;
		builder2.setSingleChoiceItems(R.array.web_scroll_style, opt.getTypeFlag_11_AtQF(flagPos), (dialog12, which) -> {
			if(opt.getScrollTypeApplyToAll()){
				for (int i = 0; i <= 6; i+=2)
					opt.setTypeFlag_11_AtQF(which, i);
			} else {
				opt.setTypeFlag_11_AtQF(which, finalFlagPos);
			}
			if (weblist==wordPopup.weblistHandler) {
				wordPopup.resetScrollbar();
			} else {
				weblist.resetScrollbar(weblist.mWebView, weblist.isMergingFrames(), true);
			}
			dialog12.dismiss();
		})
			.setSingleChoiceLayout(R.layout.select_dialog_singlechoice_material_holo)
		;
		androidx.appcompat.app.AlertDialog dTmp = builder2.create();
		dTmp.show();
		Window win = dTmp.getWindow();
		win.setBackgroundDrawable(null);
		win.setDimAmount(0.35f);
		win.getDecorView().setBackgroundResource(R.drawable.dm_dslitem_dragmy);
		win.getDecorView().getBackground().setColorFilter(GlobalOptions.NEGATIVE);
		win.getDecorView().getBackground().setAlpha(128);
		ViewGroup pp =  win.findViewById(R.id.parentPanel);
		pp.addView(getLayoutInflater().inflate(R.layout.circle_checker_item_menu_titilebar,null),0);
		((ViewGroup)pp.getChildAt(0)).removeViewAt(0);
		((ViewGroup)pp.getChildAt(0)).removeViewAt(1);
		TextView titlebar = ((TextView) ((ViewGroup) pp.getChildAt(0)).getChildAt(0));
		titlebar.setGravity(GravityCompat.START);
		titlebar.setPadding((int) (10*dm.density), (int) (6*dm.density),0,0);

		CheckedTextView cb0 = (CheckedTextView) getLayoutInflater().inflate(R.layout.select_dialog_multichoice_material, null);
		//ViewGroup cb3_tweaker = (ViewGroup) getLayoutInflater().inflate(R.layout.select_dialog_multichoice_material_seek_tweaker,null);
		cb0.setText(R.string.webscroll_apply_all);
		cb0.setId(R.string.webscroll_apply_all);
		cb0.setChecked(opt.getScrollTypeApplyToAll());
		CheckableDialogClicker mVoutClicker = new CheckableDialogClicker(opt);
		cb0.setOnClickListener(mVoutClicker);

		pp.addView(cb0, 3);

		titlebar.setText(title);

		dTmp.setCanceledOnTouchOutside(true);
		dTmp.getListView().setPadding(0,0,0,0);

		int maxHeight = (int) (root.getHeight() - 3.5 * getResources().getDimension(R.dimen._50_));
		if(getResources().getDimension(R.dimen.item_height)*(4+2)>=maxHeight)
			dTmp.getListView().getLayoutParams().height=maxHeight;
		dTmp.getListView().setTag(titlebar);
		//webcontentlist.judger = false;
	}
	
	@Deprecated
	private void anyDialog() {
		AlertDialog d = new AlertDialog.Builder(this)
				.setTitle("确认退出？")
				.setPositiveButton(R.string.confirm, null)
				.create();
		d.show();
		CMN.recurseLogCascade(d.getWindow().getDecorView());
		TextView tv = ((TextView)d.getWindow().findViewById(R.id.alertTitle));
		tv.setTextSize(60.f/opt.dm.density);
		tv.setTextSize(20);
		CMN.debug(60.f/opt.dm.density);
		CMN.debug(tv.getPaddingTop());
		CMN.debug(((ViewGroup)tv.getParent()).getPaddingTop());
	}
	
	
	public void showAppTweaker() {
		String[] DictOpt = getResources().getStringArray(R.array.app_spec);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		
		TextView tv = buildStandardConfigDialog(this, true, null, R.string.AppOpt);
		Dialog configurableDialog = (Dialog) tv.getTag();

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 16, 1, 1, 0, false);//opt.isFullScreen()//全屏
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 2, Coef, 0, 1, 0x1, 17, 1, 1, 1, false);//opt.isContentBow()//
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 3, Coef, 0, 0, 0x1, 18, 1, 1, 2, false);//opt.getInDarkMode()//
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 4, Coef, 0, 0, 0x1, 46, 1, 1, 3, false);//opt.getUseVolumeBtn()//
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 5, null, 0, 0, 0x1, 0, 1, 1, -1, false);//隐藏标题
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 6, Coef, 0, 0, 0x1, 0, 1, 2, -1, false);//opt.getInheritePageScale()//
		String[] Coef2 = new String[]{Coef[0], Coef[1], Coef[2]};
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 7, Coef2, 0, 0, 0x3, 0, 2, 2, 4, false);//opt.getNavigationBtnType()//
//		ssb.append("\r\n").append("\r\n");
//		init_clickspan_with_bits_at(tv, ssb, DictOpt, 8, Coef, 0, 0, 0x1, 3, 1, 2, 5, false);//opt.getHideScroll1()//
//		init_clickspan_with_bits_at(tv, ssb, DictOpt, 9, Coef, 0, 0, 0x1, 4, 1, 2, 6, false);//opt.getHideScroll2()//
//		init_clickspan_with_bits_at(tv, ssb, DictOpt, 10, Coef, 0, 0, 0x1, 5, 1, 2, 7, false);//opt.getHideScroll3()//
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 11, Coef, 0, 0, 0x1, 6, 1, 2, -1, false);//opt.getPageTurn1()//
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 12, Coef, 0, 0, 0x1, 7, 1, 2, -1, false);//opt.getPageTurn2()//
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 13, Coef, 0, 0, 0x1, 33, 1, 3, -1, false);//opt.getPageTurn3()//
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 15, Coef, 0, 1, 0x1, 28, 1, 3, 8, false);//opt.getAllowContentEidt()//编辑页面
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 14, Coef, 0, 0, 0x1, 9, 1, 2, 10, false);//opt.setHistoryStrategy0()//关闭历史纪录
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 16, null, 0, 0, 0x1, 0, 1, 2, 9, false);//历史纪录规则
		//CMN.Log("ssb len:", ssb.length());
		tv.setTag(null);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		configurableDialog.show();
	}
	
	public void showExitDialog(boolean restart) {
		String[] DictOpt = getResources().getStringArray(R.array.app_exit);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		int title = restart?R.string.relaunch:R.string.warn_exit0;
		
		SpannableStringBuilder titleX = new SpannableStringBuilder(mResource.getString(title));
		titleX.append("   ");
		int start = titleX.length();
		titleX.append("[设置]");
		int end=titleX.length();
		titleX.setSpan(new RelativeSizeSpan(0.63f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		titleX.setSpan(new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				launchSettings(Misc_exit_dialog.id, 0);
			}
		}, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView tv = buildStandardConfigDialog(this, false, title, 0, titleX);
		restart = false;
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 42, 1, 4, -1, true);//清除历史
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1, 40, 1, 4, R.string.shutdown_vm, true);//彻底退出
		if(!restart) {
			init_clickspan_with_bits_at(tv, ssb, DictOpt, 3, Coef, 0, 0, 0x1, 49, 1, 4, -1, true);//clear tasks
		} else {
			PDICMainAppOptions.setRestartVMOnExit(true);
			init_clickspan_with_bits_at(tv, ssb, DictOpt, 4, Coef, 0, 0, 0x1, 0, 1, 100, -1, true);//restart
		}
		
		ssb.delete(ssb.length()-4,ssb.length());
		
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		AlertDialog dialog = ((AlertDialog) tv.getTag());
		
		dialog.show();
		if(restart) {
			dialog.setCanceledOnTouchOutside(false);
		}
		if (isFloating()) {
			Button btn = dialog.findViewById(R.id.cancel);
			LinearLayout btm = new LinearLayout(this);
			ViewUtils.replaceView(btm, btn);
			Button btn1 = new Button(this, null, android.R.attr.buttonBarButtonStyle);
			btn1.setText("退出小窗模式");
			btn1.setId(R.id.max);
			View pad = new View(this);
			btm.addView(btn1);
			btm.addView(pad);
			btm.addView(btn);
			btn1.getLayoutParams().height = WRAP_CONTENT;
			btn1.getLayoutParams().width = WRAP_CONTENT;
			btm.getLayoutParams().height = WRAP_CONTENT;
			btm.getLayoutParams().width = MATCH_PARENT;
			((ViewGroup.MarginLayoutParams)btm.getLayoutParams()).leftMargin = 0;
			((ViewGroup.MarginLayoutParams)btm.getLayoutParams()).bottomMargin /= 2;
			pad.getLayoutParams().height = MATCH_PARENT;
			pad.getLayoutParams().width = 0;
			((LinearLayout.LayoutParams)pad.getLayoutParams()).weight = 1;
			btn.getLayoutParams().height = WRAP_CONTENT;
			btn.getLayoutParams().width = WRAP_CONTENT;
			btn1.setTextColor(MainAppBackground);
			btn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					floatApp.toggle(true);
					dialog.dismiss();
				}
			});
		}
		tv.setTag(null);
	}
	
	public void showSoundTweaker() {
		String[] DictOpt = getResources().getStringArray(R.array.sound_spec);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		
		TextView tv = buildStandardConfigDialog(this, true, null, R.string.SoundOpt);
		Dialog configurableDialog = (Dialog) tv.getTag();

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 38, 1, 3, -1, true);//opt.getAutoReadEntry()//
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 12, null, 0, 0, 0x1, 0, 1, 4, 12, false);//暂停
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1, 48, 1, 4, -1, true);//opt.getThenAutoReadContent()//
		
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 3, Coef, 0, 0, 0x1, 37, 1, 3, -1, true);//opt.getHintTTSReading()//
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 4, Coef, 0, 1, 0x1, 42, 1, 3, -1, true);//opt.getTTSBackgroundPlay()//

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 5, Coef, 0, 1, 0x1, 35, 1, 3, 12, true);//opt.getClickSearchEnabled()//
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 6, null, 0, 1, 0x1, 0, 1, 3, 15, false);

		boolean flagCase = PeruseViewAttached();
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 7, Coef, 0, 0, 0x1, flagCase?37:46, 1, flagCase?2:1, -1, true);//opt.getUseVolumeBtn()
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 8, Coef, 0, 1, 0x1, 39, 1, 3, -1, true);//opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed()//

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 9, null, 0, 0, 0x1, 44, 1, 3, 16, false);//滚动条
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 10, Coef, 0, 0, 0x1, 44, 1, 3, 11, true);//opt.getTTSHighlightWebView()//

		init_clickspan_with_bits_at(tv, ssb, DictOpt, 11, null, 0, 0, 0x1, 0, 1, 3, 10, false);//

		ssb.delete(ssb.length()-4, ssb.length());
		
		tv.setTag(null);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		configurableDialog.show();
	}

	void changeToDarkMode() {
		CMN.debug("changeToDarkMode");
		try {
			getReferenceObject(WeakReferenceHelper.quick_settings).clear();
			boolean dark=GlobalOptions.isDark;
			AppBlack = dark?Color.WHITE:Color.BLACK;
			AppWhite = dark?Color.BLACK:Color.WHITE;
			MainAppBackground = dark?ColorUtils.blendARGB(MainBackground, Color.BLACK, 0.9f):MainBackground;
			if(drawerFragment!=null){
				drawerFragment.mDrawerListLayout.setBackgroundColor(dark?Color.BLACK:0xffe2e2e2);
				drawerFragment.HeaderView.setBackgroundColor(AppWhite);
				drawerFragment.FooterView.setBackgroundColor(AppWhite);
				drawerFragment.myAdapter.notifyDataSetChanged();
				if(isFragInitiated && dictPicker !=null) dictPicker.adapter().notifyDataSetChanged();
			}
			if(DBrowser!=null) {
				DBrowser.checkColors();
			}
			wordPopup.refresh();
			if(adaptermy==null) {
				return;
			}
			adaptermy.notifyDataSetChanged();
			adaptermy2.notifyDataSetChanged();
			if(adaptermy3!=null){
				adaptermy3.notifyDataSetChanged();
				adaptermy4.notifyDataSetChanged();
			}
			setchooser.clear();
			if(ActivedAdapter!=null){
				webviewHolder = ActivedAdapter.webviewHolder;
				if(webviewHolder!=null) {
					for(int i=0;i<webviewHolder.getChildCount();i++) {
						View ca = webviewHolder.getChildAt(i);
						if (ca!=null && ca.getTag() instanceof WebViewmy) {
							BookPresenter mdTmp = ((WebViewmy) ca.getTag()).presenter;
							mdTmp.vartakelayaTowardsDarkMode(null);
						}
					}
				}
			}
			animateUIColorChanges();
		} catch (Exception e) {
			CMN.debug(e);
		}
	}

	public abstract void animateUIColorChanges();

	public VerseKit getVtk() {
		if(ucc==null) ucc = new VerseKit();
		return ucc;
	}
	
	/** 0=hide; 1=remove */
	int contentviewDetachType = -1;
	
	/** 0=hide; 1=remove */
	public void setContentDetachType(int type) {
		if(contentviewDetachType!=type) {
			if(type==1 && (contentviewDetachType==-1 || contentview.getVisibility()!=View.VISIBLE)) {
				contentview.setVisibility(View.VISIBLE);
				ViewUtils.removeView(contentview);
			}
			if(type==0 && (contentviewDetachType==-1 || contentview.getParent()==null)) {
				contentview.setVisibility(View.GONE);
				if(thisActType==ActType.PlainDict) {
					boolean animate = opt.getAnimateContents();
					opt.setAnimateContents(false);
					((PDICMainActivity)this).AttachContentView(false);
					opt.setAnimateContents(animate);
				} else if(thisActType==ActType.FloatSearch){
				
				}
			}
			contentviewDetachType=type;
		}
	}
	
	public boolean isContentViewAttached() {
		if(contentviewDetachType==0)
			return contentview.getVisibility()==View.VISIBLE; // float
		return contentview.getParent()!=null;
	}
	
	void DetachContentView(boolean leaving) {
		//ViewUtils.removeView(contentview);  multishare
	}
	
	void AttachDBrowser(int type) {
		if(DBrowser==null || opt.debuggingDBrowser()>0) {
			DBrowser = DBrowserHolder.get();
			if(DBrowser==null || opt.debuggingDBrowser()>0){
				// CMN.debug("重建收藏夹历史记录视图");
				DBrowserHolder = new WeakReference<>(DBrowser = new DBroswer());
			}
		}
		boolean retry = type<0;
		if (retry) type = -type;
		// CMN.debug("AttachDBrowser::", type==DB_HISTORY, Integer.toHexString(weblist.src), CMN.idStr(weblist));
		DBrowser.setType(this, type, false);
		int showType = DBrowser.preShow(weblist);
		if(showType==1) {
			try {
				DBrowser.show(getSupportFragmentManager(), "DBrowser");
			} catch (Exception e) {
				CMN.debug(e);
				if (isFloating() && e instanceof IllegalStateException) {
					if (!retry) {
						// fix android Can not perform this action after onSaveInstanceState
						int finalType = type;
						postTask = new Runnable(){
							@Override
							public void run() {
								AttachDBrowser(-finalType);
								floatApp.expand(true);
								postTask = null;
							}
						};
						moveTaskToFront();
					}
				}
			}
		}
		else if(showType==2) {
			ViewGroup target = mainF;
			if(!DBrowser.isAdded()) {
				FragmentManager fragmentManager = getSupportFragmentManager();
				fragmentManager.beginTransaction()
						.setCustomAnimations(R.anim.history_enter, R.anim.history_enter)
						.add(R.id.mainF, DBrowser)
						.commit();
			} else {
				View view = DBrowser.getView();
				if(ViewUtils.removeIfParentBeOrNotBe(view, target, false)) {
					target.addView(view);
				}
			}
		}
	}
	
	public void DetachDBrowser() {
		if(DBrowser.try_goBack()!=0)
			return;
		//File newFavor = DBrowser.items.get(DBrowser.lastChecked);
		//xxx
//			if(!(DBrowser instanceof DHBroswer))
//				if(!newFavor.equals(new File(favoriteCon.pathName))) {//或许需要重载收藏夹
//					favoriteCon.close();
//					favoriteCon = new LexicalDBHelper(this, newFavor);
//					String name = new File(favoriteCon.pathName).getName();
//					//opt.putString("currFavoriteDBName", opt.currFavoriteDBName=);
//					opt.putCurrFavoriteDBName(favorTag+name.substring(0,name.length()-4));
//					show(R.string.currFavor, DBrowser.boli(newFavor.getName()));
//				}
		//weblistHandler.removeAllViews();
		DBrowser.detach();
		DBrowser = null;
	}
	
	class AcrossBoundaryContext {
		int initiatorIdx;
		int initiatorDir;
		int rejectIdx;
		int rejectDir;
		int 沃壳积;
		public boolean collide() {
			return initiatorIdx==rejectIdx&&initiatorDir==rejectDir;
		}
		public void dump() {
			rejectIdx=initiatorIdx;
			rejectDir=initiatorDir;
		}
	}
	AcrossBoundaryContext PrvNxtABC = new AcrossBoundaryContext();
	
	//click
	@Override
	public void onClick(View v) {
		if(!systemIntialized) {
			return;
		}
		if (mInterceptorListener!=null) {
			mInterceptorListenerHandled = false;
			mInterceptorListener.onClick(v);
			if (mInterceptorListenerHandled) {
				click_handled_not = false;
				return;
			}
		}
		click_handled_not = true;
		int id=v.getId();
		
		switch (id){
			default:return;
			case R.drawable.ic_menu_24dp: {
				showMenuGrid(v);
			} break;
			case R.drawable.ic_exit_app:{
				v.getBackground().jumpToCurrentState();
				moveTaskToBack(false);
			} break;
			case R.drawable.customize_bars: {
				showIconCustomizator();
			} break;
			case R.drawable.abc_ic_menu_share_mtrl_alpha: {
				shareUrlOrText();
			} break;
			case R.drawable.ic_baseline_nightmode_24: {
				showNightModeSwitch();
			} break;
			case R.drawable.ic_options_toolbox:
			case R.drawable.ic_options_toolbox_small: {
				if(v.getParent()==null || id==R.drawable.ic_options_toolbox_small) findWebList(v);
				showBookSettings();
			} break;
			//收藏和历史纪录
			case R.drawable.favoriteg: {// get5:
				findWebList(v);
				dismissPopup();
				//if(mainF.getChildCount()==0)
				AttachDBrowser(DB_FAVORITE);
			} break;
			case R.drawable.historyg: { // get6:
				findWebList(v);
				dismissPopup();
				//todo to favorite
				//boolean fromPeruseView = PeruseViewAttached();
				//ViewGroup target = mainF;//fromPeruseView? peruseView.peruseF:mainF;
				//if(target.getChildCount()==0)
				AttachDBrowser(DB_HISTORY);
			} break;
			case R.drawable.ic_keyboard_show_24: {
				etSearch.setSelectAllOnFocus(true);
				etSearch.requestFocus();
				imm.showSoftInput(etSearch, 0);
				etSearch.setSelectAllOnFocus(false);
				//imm.showin(InputMethodManager.SHOW_FORCED, 0);
			} break;
			case R.drawable.ic_prv_dict_chevron:
			case R.drawable.ic_nxt_dict_chevron: {
				if(ViewUtils.isVisible(lv2)) {
					ViewUtils.setVisibility(lv2, false);
				}
				if(CurrentViewPage==1) {
					int delta = (id == R.drawable.ic_prv_dict_chevron ? -1 : 1);
					int testVal = dictPicker.adapter_idx;
					PrvNxtABC.initiatorIdx=testVal;
					PrvNxtABC.initiatorDir=delta;
					PrvNxtABC.沃壳积 =0;
					boolean b1=dictPicker.autoSchPDict() && isContentViewAttached();
					if(b1) bIsFirstLaunch=true;
					while(!switch_Dict(dictPicker.adapter_idx+delta, true, false, PrvNxtABC));
					if(PrvNxtABC.collide() && testVal!=dictPicker.adapter_idx) { // rejected
						switch_Dict(testVal, true, false, null);
					} else {
						getTopSnackView().setNextOffsetScale(0.24f);
						showTopSnack(currentDictionary.getDictionaryName());
						if(dictPicker.pinned()) {
							dictPicker.scrollThis();
							dictPicker.mAdapter.notifyItemChanged(testVal);
							dictPicker.mAdapter.notifyItemChanged(dictPicker.adapter_idx);
						}
					}
					if(b1) bIsFirstLaunch=false;
					if(testVal!=dictPicker.adapter_idx&&PrvNxtABC.rejectIdx>=0) { // not rejected
						PrvNxtABC.rejectIdx=-1;
					}
					if(currentDictionary!=EmptyBook) {
						postPutName(1000);
					}
				}
			} break;
			case R.id.ttsPin: {
				CircleCheckBox checker = (CircleCheckBox) v;
				checker.toggle();
				opt.setTTSCtrlPinned(checker.isChecked());
				TTSController_controlBar.setVisibility(checker.isChecked()?View.VISIBLE:View.GONE);
			} break;
			case R.id.ttsHighlight: {
				CircleCheckBox checker = (CircleCheckBox) v;
				checker.toggle(false);
				opt.setTTSHighlightWebView(checker.isChecked());
			} break;
			case R.id.tts_settings: {
				try {
					Intent intent = new Intent();
					intent.setAction("com.android.settings.TTS_SETTINGS");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} break;
			case R.id.tts_popIvBack: {
				if(TTSController_.getParent()!=null){
					hideTTS();
				}
			} break;
			case R.id.tts_expand: {
				if(opt.setTTSExpanded(!opt.getTTSExpanded())){
					TTSController_.getLayoutParams().height=TTSController_moveToucher.FVH_UNDOCKED;
					((ImageView)v).setImageResource(R.drawable.ic_substrct_black_24dp);
				} else {
					TTSController_.getLayoutParams().height= (int) getResources().getDimension(R.dimen._45_);
					((ImageView)v).setImageResource(R.drawable.ic_add_black_24dp);
				}
				TTSController_.requestLayout();
			} break;
			case R.id.tts_play: {
				if(speakPool==null) break;
				if(v.getTag()==null){
					if(speakPoolEndIndex+1>=speakPool.length){
						speakPoolEndIndex=-1;
					}
					mPullReadTextRunnable.run();
				} else {
					v.setTag(null);
					TTSController_engine.stop();
					TTSController_playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				}
			} break;
			case R.id.tts_NxtUtterance:
			case R.id.tts_LstUtterance: {
				if(speakPool==null) break;
				int delta = (id==R.id.tts_LstUtterance?-1:1);
				TTSController_engine.stop();
				int target = speakPoolIndex + delta;
				while(target<speakPool.length && target>=0 && speakPool[target].trim().length()==0){
					target += delta;
				}
				speakPoolEndIndex = target - 1;
				mPullReadTextRunnable.run();
			} break;
			/* 清零 */
			case R.id.ivDeleteText:{
				if(v.getTag()!=null){
					return;
				}
				if((etSearch_toolbarMode&2)==0) {//delete
					String SearchTmp = ViewUtils.getTextInView(etSearch).trim();
					if(SearchTmp.equals("")) {
						ivDeleteText.setVisibility(View.GONE);
					} else {
						lastEtString=SearchTmp;
						CombinedSearchTask_lastKey=null;
						etSearch.setText(null);
						etSearch_ToToolbarMode(4);
						if(true) {
							DetachContentView(false);
						}
					}
				} else {//undo
					etSearch.setText(lastEtString);
					//etSearch_ToToolbarMode(3);
				}
			}break;
			/* 选择.删除.收藏夹 */
			case R.id.ivDeleteText_ADA: {
				View p = (View) v.getParent();
				DArrayAdapter.ViewHolder vh = (DArrayAdapter.ViewHolder) p.getTag();
				int position = vh.position;
				int id_ = ((ViewGroup)p.getParent()).getId();
				if (getUsingDataV2()) {
					MyPair<String, Long> item = AppFunAdapter.notebooksV2.get(position);
					if(p.getParent() instanceof ViewGroup)
						//选择收藏夹
						if(id_==R.id.favorList) {
							putCurrFavoriteNoteBookId(item.value);
							AppFunAdapter.notifyDataSetChanged();
						}
						//删除收藏夹
						else if(id_==R.id.click_remove) {
							if (prepareHistoryCon().removeFolder(item.value)>=0) {
								AppFunAdapter.remove(position);
							}
						}
				} else {
					MyPair<String, LexicalDBHelper> item = AppFunAdapter.notebooks.get(position);
					if(p.getParent() instanceof ViewGroup)
						//选择收藏夹
						if(id_==R.id.favorList) {
							//CMN.Log("选择!!!");
							opt.putCurrFavoriteDBName(item.key);
							favoriteCon = null;
							prepareFavoriteCon();
							AppFunAdapter.notifyDataSetChanged();
						}
						//删除收藏夹
						else if(id_==R.id.click_remove) {
							LexicalDBHelper vI = item.value;
							if(vI!=null){
								if(vI==favoriteCon)
									favoriteCon=null;
								vI.close();
								item.value=null;
							}
							File fi = opt.fileToFavoriteDatabases(item.key, false);
							fi.delete();
							new File(fi.getPath()+"-journal").delete();
							AppFunAdapter.remove(position);
						}
				}
			}break;
			//返回
			case R.id.browser_widget7:{
				if(PeruseViewAttached()){
					peruseView.try_go_back();
					break;
				}
				exitTime=0;
				boolean b1=thisActType==ActType.PlainDict;
				if(b1) {
					v.setTag(false);
					PDICMainActivity THIS = (PDICMainActivity) this;
					THIS.UIData.drawerLayout.closeDrawer(GravityCompat.START);
					if (THIS.drawerFragment.d != null) {
						THIS.drawerFragment.d.dismiss();
					}
				}
				if(PeruseViewAttached() && peruseView.removeContentViewIfAttachedToRoot()) {
					break;
				}
				// todo yes
				if(b1){
					lastBackBtnAct = true;
					onBackPressed();
					lastBackBtnAct = false;
				} else {
					if(thisActType!=ActType.MultiShare) {
						etSearch_ToToolbarMode(0);
					}
					contentUIData.webcontentlister.setVisibility(View.GONE);
				}
			} break;
			//左右翻页
			case R.id.browser_widget10:
			case R.id.browser_widget11:{//左zuo
				int delta = (id==R.id.browser_widget10?-1:1);
				//if(((ScrollViewmy)WHP).touchFlag!=null)((ScrollViewmy)WHP).touchFlag.first=true;
				//adaptermy2.combining_search_result.expectedPos=0;
				//webholder.removeOnLayoutChangeListener(((resultRecorderCombined)adaptermy2.combining_search_result).OLCL);
				layoutScrollDisabled=false;
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				findWebList(v);
				boolean slided = v.getTag()==v; /** see{@link #getPageListener} */
				if (slided) v.setTag(null);
				if (!slided && weblist.bottomNavWeb()) {
					weblist.NavWeb(delta);
				} else {
					if(!AutoBrowsePaused&&PDICMainAppOptions.getAutoBrowsingReadSomething()){
						interruptAutoReadProcess(true);
					}
					if (slided && weblist.isFoldingScreens() && weblist.multiDicts && PDICMainAppOptions.slidePageFd()) {
						int toPos = weblist.multiRecord.jointResult.LongestStartWithSeqLength;
						if (toPos>0) toPos = delta;
						else toPos = delta-toPos;
						if (toPos>=0 && toPos<weblist.frames.size()) {
							weblist.renderFoldingScreen(toPos);
							break;
						}
					}
					if (weblist==weblistHandler) {
						if (ActivedAdapter!=null) {
							int toPos = ActivedAdapter.lastClickedPos+delta;
							ActivedAdapter.onItemClick(toPos);
						}
					} else if (peruseView!=null && weblist==peruseView.weblistHandler) {
						if (peruseView.ActivedAdapter!=null) {
							int toPos = peruseView.ActivedAdapter.lastClickedPos+delta;
							peruseView.ActivedAdapter.onItemClick(toPos);
						}
					}  else if (DBrowser!=null && weblist==DBrowser.weblistHandler) {
						DBrowser.NavList(delta);
					} else  {
						if (!weblist.isMultiRecord()) {
							// 就当你是弹出的 entry:// ！
							WebViewmy wv = weblist.dictView;
							int pos = (int) (wv.currentPos + delta);
							if (pos < 0 || pos >= wv.presenter.bookImpl.getNumberEntries()) {
								showTopSnack(null, R.string.endendr, -1, -1, -1, 0);
							} else {
								wv.presenter.renderContentAt(-1, BookPresenter.RENDERFLAG_NEW, 0, wv, wv.currentPos + delta);
								if (wv.getBackgroundColor() == 0)
									wv.setBackgroundColor(GlobalPageBackground); //todo optimize
							}
						} else {
						
						}
					}
				}
			} break;
			/* 发音 */
			case R.id.browser_widget12:{
				findWebList(v);
				performReadEntry();
//				if(webSingleholder.getChildCount()==1 && v.getTag(R.id.image)!=null){
//					// todo
//					if((int)v.getTag(R.id.image)==R.drawable.ic_fullscreen_black_96dp){
//						if(thisActType==ActType.PlainDict)
//							((PDICMainActivity)this).forceFullscreen(!PDICMainAppOptions.isFullScreen());
//					} else {
//						toggleClickThrough();
//					}
//				} else {
//					performReadEntry();
//				}
			} break;
			/* 切换收藏 */
			case R.drawable.star_ic:
			case R.id.browser_widget8: {//favorite
				findWebList(v);
				String key = weblist.displaying;
				if(DBrowser!=null && weblist==DBrowser.weblistHandler){
					DBrowser.toggleFavor();
				} else {
					if (key!=null) {
						if(GetIsFavoriteTerm(key)) {
							removeFavoriteTerm(key);
							v.setActivated(false);
							show(R.string.removed);
						} else {
							if (prepareFavoriteCon().insert(this, key, opt.getCurrFavoriteNoteBookId(), weblist) >= 0) {
								v.setActivated(true);
								//show(R.string.added);
								showT(key + " 收藏成功");
							} else {
								showT("收藏失败！");
							}
						}
					}
				}
			} break;
			/* 跳转 */
			case R.id.browser_widget9:{//view outlinexxx
				findWebList(v);
				if(weblist.isMultiRecord()) {
					weblist.showJumpListDialog();
				}
				else {
					if(weblist.contentUIData.browserWidget12.getTag(R.id.image)!=null){
						float alpha = contentview.getAlpha();// 0.5 0.25 0 1
						if(alpha==0) alpha=1;
						else if(alpha==1) alpha=0.37f;
						else  alpha=0;
						weblist.contentUIData.webcontentlister.setAlpha(alpha);
					} else {
						//showT("无效操作, 请尝试长按");
						v.performLongClick();
					}
				}
			} break;
			/* 自动浏览 */
			case R.drawable.ic_autoplay:{
				if(bRequestingAutoReading || !AutoBrowsePaused){
					stopAutoReadProcess();
				} else {
					showT("自动浏览");
					opt.isAudioActuallyPlaying=false;
					startAutoReadProcess();
				}
			} break;
			/* 全文朗读 */
			case R.drawable.ic_fulltext_reader:{
				showMT("  全文朗读  ");
				performReadContent(getCurrentWebContext());
			} break;
			/* 上一词典信息 */
			case R.id.about_popLstDict:{
				showAboutDictDialogAt(CurrentDictInfoIdx-1);
			} break;
			/* 下一词典信息 */
			case R.id.about_popNxtDict:{
				showAboutDictDialogAt(CurrentDictInfoIdx+1);
			} break;
			case R.id.about_popIvBack:
			case R.id.cancel:{
				if(d!=null){
					d.dismiss();
				}
			} break;
			case R.id.schDropdown:{
				etTools.drpdn = PDICMainAppOptions.historyShow();
				etTools.flowBtn = toolbar.findViewById(R.id.action_menu_presenter);
				if(thisActType==MainActivityUIBase.ActType.PlainDict)
					etTools.topbar = appbar;

			} break;
		}
		click_handled_not = false;
	}
	
	public void shareUrlOrText() {
		if (weblist!=null) {
			int shareWhat = PDICMainAppOptions.shareTextOrUrl();
			MenuGrid menuGrid = (MenuGrid) getReferencedObject(WeakReferenceHelper.menu_grid);
			if (shareWhat>1 && menuGrid!=null && !ViewUtils.ViewIsId(menuGrid.bottombar, R.id.bottombar2)) {
				shareWhat = 1;
			}
			if (shareWhat>1) {
				shareUrlOrText(weblist.getShareUrl(shareWhat>3), null, shareWhat);
			} else {
				WebViewmy wv = null;
				View view = getCurrentFocus();
				if (view!=null) {
					if (view.getId()==R.id.webviewmy) {
						wv = ((WebViewmy)view);
					}
					else if (view instanceof TextView) {
						TextView tv = ((TextView) getCurrentFocus());
						if (tv.hasSelection()) {
							String text = tv.getText().toString();
							int st = tv.getSelectionStart(), ed=tv.getSelectionEnd();
							try {
								shareUrlOrText(null, text.substring(Math.min(st, ed), Math.max(st, ed)), shareWhat);
								return;
							} catch (Exception e) { }
						}
					}
				}
				if (wv==null) {
					if(wordPopup.popupContentView!=null && wordPopup.mWebView.bIsActionMenuShown) {
						wv = wordPopup.mWebView;
					}
				}
				if (wv==null) {
					wv = weblist.getWebContext();
				}
				if (wv!=null) {
					if (wv != null && wv.bIsActionMenuShown) {
						int fineShare = shareWhat;
						wv.evaluateJavascript("getSelection().toString()", value -> {
							String newKey = "";
							if (value.length() > 2) {
								value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
								if (value.length() > 0) {
									newKey = value;
								}
							}
							shareUrlOrText(null, newKey, fineShare);
						});
					} else {
						shareUrlOrText(null, wv.weblistHandler.displaying, shareWhat);
					}
				}
			}
		}
	}
	
	public void shareUrlOrText(String url, String text, int shareWhat) {
		//CMN.Log("menu_icon6menu_icon6");
		//CMN.rt("分享链接……");
		if (url!=null || text!=null) {
			int id = WeakReferenceHelper.share_dialog;
			BottomSheetDialog dlg = (BottomSheetDialog) getReferencedObject(id);
			AppIconsAdapter shareAdapter;
			if(dlg==null) {
				shareAdapter=new AppIconsAdapter(this);
				putReferencedObject(id, dlg=shareAdapter.shareDialog);
			} else {
				shareAdapter = (AppIconsAdapter) dlg.tag;
			}
			//CMN.pt("新建耗时：");
			if (url==null) {
				//url = currentWebView.getUrl();
			}
			shareAdapter.pullAvailableApps(this, url, text, shareWhat);
			//shareAdapter.pullAvailableApps(this, null, "happy");
			//CMN.pt("拉取耗时：");
		}
	}
	
	public boolean getUsingDataV2() {
		return prepareHistoryCon().testDBV2;
	}
	
	public void activateDataV2() {
		opt.setUseDatabaseV2(true);
		LexicalDBHelper dbCon;
		dbCon = favoriteCon;
		AgentApplication app = ((AgentApplication) getApplication());
		if(dbCon!=null && !dbCon.testDBV2) {
			favoriteCon = null;
			dbCon.close();
		}
		dbCon = historyCon;
		boolean bNeedUpdate = false;
		if(dbCon!=null && !dbCon.testDBV2) {
			app.historyCon = historyCon = null;
			dbCon.close();
			bNeedUpdate = true;
		}
		DBrowserHolder.clear();
		for(DeckListAdapter.DeckListData dI: DBrowserDatas){
			if(dI!=null) dI.close();
			DBrowserDatas = new DeckListAdapter.DeckListData[2];
		}
		dbCon = prepareHistoryCon();
		//if(bNeedUpdate)
		{
			// prepare book id for updating
			HashSet<File> mdns = new HashSet<>();
			ArrayList<BookPresenter> mds = new ArrayList<>(md);
			mds.addAll(mdict_cache.values());
			for(BookPresenter bookPresenter:md) {
				if (bookPresenter!=null && bookPresenter.bookImpl.getBooKID()==-1) {
					BookPresenter.keepBook(this, bookPresenter.bookImpl);
					CMN.debug("激活DBV2::", bookPresenter.bookImpl);
					mdns.add(bookPresenter.bookImpl.getFile());
				}
			}
			File ConfigFile = opt.fileToConfig();
			File rec = opt.fileToDecords(ConfigFile);
			if(rec.exists())
			try {
				BufferedReader in = new BufferedReader(new FileReader(rec));
				String line;
				while((line=in.readLine())!=null){
					File check;
					if(!line.startsWith("/"))
						check=new File(opt.lastMdlibPath,line);
					else
						check=new File(line);
					mdns.add(check);
				}
				in.close();
			} catch (Exception e2) {
				CMN.debug(e2);
			}
			File[] mdnsArr = mdns.toArray(new File[mdns.size()]);
			for(File bookName:mdnsArr) {
				CMN.debug("激活DBV2::", bookName);
				String path = bookName.getPath();
				String name = bookName.getName();
				try {
					path = bookName.getCanonicalPath();
				} catch (IOException e) {
					CMN.debug(e);
				}
				dbCon.getBookID(path, name);
			}
			DArrayAdapter favAdapter = new DArrayAdapter(this, true);
			for(MyPair<String, LexicalDBHelper> cp:favAdapter.notebooks)
			{
				String name = cp.key;
				LexicalDBHelper favCon = cp.value;
				if (favCon==null) {
					favCon = new LexicalDBHelper(getApplicationContext(), opt, name, false);
				}
				long folder = dbCon.ensureNoteBookByName(name);
				if (folder==-1) {
					folder = 0;
				}
				DBUpgradeHelper.upgradeFavToFavFolder(favCon, dbCon, folder);
				cp.value = null;
				favCon.close();
			}
			LexicalDBHelper historyCon = new LexicalDBHelper(getApplicationContext(), opt, null, false);
			DBUpgradeHelper.upgradeHistoryToDBV2(historyCon, dbCon);
			historyCon.close();
		}
	}
	
	private Runnable putNameRunnable = () -> {
		BookPresenter mdTmp = currentDictionary;
		String name = mdTmp==EmptyBook?new File(getPlaceHolderAt(dictPicker.adapterIdx()).pathname).getName()
				:mdTmp.getDictionaryName();
		opt.putLastMdFn(LastMdFn, name);
	};
	
	protected void postPutName(long delayMilliSeconds) {
		root.removeCallbacks(putNameRunnable);
		root.postDelayed(putNameRunnable, delayMilliSeconds);
	}
	
	public void dismissPopup() {
		DetachClickTranslator();
	}
	
	void scrollToWebChild(View childAt) {
		//CMN.Log("scrollToWebChild");
		if(childAt!=null) {
			View postTarget = null;
			int delay = 100;
			if(PDICMainAppOptions.getScrollAutoExpand())
			try {
				BookPresenter mdTmp = ((WebViewmy) childAt.getTag()).presenter;
				if (mdTmp != null && mdTmp.mWebView.getVisibility() != View.VISIBLE) {
					postTarget = mdTmp.mWebView;
					mdTmp.toolbar_title.performClick();
					if (mdTmp.mWebView.awaiting)
						delay = 350;
				}
			} catch (Exception ignored) { }
			if (postTarget != null) {
				postTarget.postDelayed(() -> {
					scrollToPageTop(childAt);
				}, delay);
			} else {
				scrollToPageTop(childAt);
			}
		}
	}

	private void scrollToPageTop(View childAt) {
		if(PDICMainAppOptions.getScrollAnimation())
			weblistHandler.WHP.smoothScrollTo(0, childAt.getTop());
		else
			weblistHandler.WHP.scrollTo(0, childAt.getTop());
	}

	void scrollToPageBottom(View ca) {
		if(ca!=null) {
			int offset = ca.getBottom();
			if(PDICMainAppOptions.getScrollAnimation())
				weblistHandler.WHP.smoothScrollTo(0, offset);
			else
				weblistHandler.WHP.scrollTo(0, offset);
		}
	}
	
	@Deprecated
	public void showChooseTTSDialog() { }

	public final void showChooseDictDialog(int reason) {
		dictPicker.toggle();
	}
	
	public void toggleStar(String key, ImageView futton, boolean toast, WebViewListHandler webviewholder) {
		key = key.trim();
		if(GetIsFavoriteTerm(key)) {
			removeFavoriteTerm(key);
			futton.setActivated(false);
			if(toast)show(R.string.removed);
		} else {
			if (favoriteCon.insert(this, key, opt.getCurrFavoriteNoteBookId(), weblist) >= 0) {
				futton.setActivated(true);
				if (toast) show(R.string.added);
			} else {
				if(toast)showT("收藏失败");
			}
		}
	}
	
	public boolean deleteHistory() {
		try {
			return prepareHistoryCon().wipeData();
		} catch (Exception ignored) { }
		return false;
	}
	
	
	public void showDictTweaker(WebViewListHandler weblist) {
		VerseKit tk = getVtk();
		if (weblist.isMultiRecord()) {
			if (weblist.isMergingFrames()) {
				weblist.getMergedFrame().evaluateJavascript("scrollFocus.src", new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String val) {
						CMN.debug("onReceiveValue::", val);
						try {
							val = val.substring(1, val.length() - 1);
							String[] arr=val.split("_");
							val = arr[0];
							long id = IU.TextToNumber_SIXTWO_LE(val.substring(1));
							val = arr[1];
							long pos = IU.TextToNumber_SIXTWO_LE(val);
							BookPresenter book = getBookById(id);
							weblist.getMergedFrame().currentPos = pos;
							book.currentDisplaying = book.getBookEntryAt((int) pos); // 权宜之计
							tk.showDictTweaker(book, weblist.getMergedFrame());
						} catch (Exception e) {
							CMN.debug(e);
						}
					}
				});
			} else {
				tk.showDictTweaker(weblist.scrollFocus.presenter, weblist.scrollFocus);
			}
		} else {
			tk.showDictTweaker(weblist.getWebContext().presenter, weblist.getWebContext());
		}
	}
	
	//longclick
	@Override @SuppressLint("ResourceType")
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			/* long-click exit */
			case R.drawable.ic_exit_app:{
				showExitDialog(false);
				//anyDialog();
			} return true;
			case R.id.browser_widget8:{
				String text=null;
				if(PeruseViewAttached())
					text= peruseView.currentDisplaying();
				else if(DBrowser!=null)
					text=DBrowser.currentDisplaying;
				else if(ActivedAdapter!=null)
					text=ActivedAdapter.currentKeyText;
				if(text!=null)
					showMultipleCollection(text, ActivedAdapter==null?null:ActivedAdapter.webviewHolder);
			} return true;
			/* long-click view outline */
			case R.id.browser_widget9:{
				findWebList(v);
				showDictTweaker(weblist);
			} return true;
			/* 页面导航模式 */
			case R.id.browser_widget10:
			case R.id.browser_widget11:{
				findWebList(v);
				boolean bPeruseIncharge = PeruseViewAttached();
				AlertDialog dTmp = new AlertDialog.Builder(this)
						.setSingleChoiceItems(R.array.btm_navmode
						, weblist.getBottomNavWeb()?1:0, (d, which) -> {
							weblist.setBottomNavWeb(which==1);
							d.dismiss();
						})
						.setSingleChoiceLayout(R.layout.select_dialog_singlechoice_material_holo)
						.show();
				Window win = dTmp.getWindow();
				win.setBackgroundDrawable(null);
				win.setDimAmount(0.35f);
				win.getDecorView().setBackgroundResource(R.drawable.dm_dslitem_dragmy);
				win.getDecorView().getBackground().setColorFilter(GlobalOptions.NEGATIVE);
				win.getDecorView().getBackground().setAlpha(128);
				ViewGroup pp =  win.findViewById(R.id.parentPanel);
				pp.addView(getLayoutInflater().inflate(R.layout.circle_checker_item_menu_titilebar,null),0);
				((ViewGroup)pp.getChildAt(0)).removeViewAt(0);
				((ViewGroup)pp.getChildAt(0)).removeViewAt(1);
				TextView titlebar = ((TextView) ((ViewGroup) pp.getChildAt(0)).getChildAt(0));
				titlebar.setGravity(GravityCompat.START);
				titlebar.setPadding((int) (10*dm.density), (int) (6*dm.density),0,0);
				titlebar.setText("设置按钮功能");
				dTmp.setCanceledOnTouchOutside(true);
				dTmp.getListView().setPadding(0,0,0,0);
				
				if(false)
				if(!bPeruseIncharge) {
					CheckedTextView cb0 = (CheckedTextView) getLayoutInflater().inflate(R.layout.select_dialog_multichoice_material, null);
					//ViewGroup cb3_tweaker = (ViewGroup) getLayoutInflater().inflate(R.layout.select_dialog_multichoice_material_seek_tweaker,null);
					cb0.setText(R.string.backkey_web_goback);
					cb0.setId(R.string.backkey_web_goback);
					cb0.setChecked(bPeruseIncharge ? opt.getUseBackKeyGoWebViewBack1() : opt.getUseBackKeyGoWebViewBack());
					if (bPeruseIncharge)
						cb0.setTag(false);
					CheckableDialogClicker mVoutClicker = new CheckableDialogClicker(opt);
					cb0.setOnClickListener(mVoutClicker);

					pp.addView(cb0, 3);
				}

				int maxHeight = (int) (root.getHeight() - 3.5 * getResources().getDimension(R.dimen._50_));
				if(getResources().getDimension(R.dimen.item_height)*(4+2)>=maxHeight)
					dTmp.getListView().getLayoutParams().height=maxHeight;
				dTmp.getListView().setTag(titlebar);
				contentUIData.webcontentlister.judger = false;
			} return true;
			/* 语音控制器 */
			case R.id.browser_widget12:{
				findWebList(v);
				CMN.debug("weblist::", weblist==wordPopup.weblistHandler);
				showSoundTweaker();
				contentUIData.webcontentlister.judger = false;
			} return true;
		}
		return false;
	}
	
	public View anyView(int id) {
		View v = new View(this);
		v.setId(id);
		return v;
	}
	
	/** see {@link #getMenuGridRootViewForPanel} */
	private View findWebList(View v) {
		View btm = null;
		btm = (View) v.getParent();
		//CMN.Log("findWebList::", v.getParent(), ((View) v.getParent()).getTag());
		if(btm==null)  {
			btm = contentUIData.bottombar2;
			app_panel_bottombar_height = btm.getHeight();
			if(weblist!=null) return btm;
		} else {
			app_panel_bottombar_height = btm.getHeight();
		}
		WebViewListHandler weblist = (WebViewListHandler) btm.getTag();
		if(weblist!=null) {
			this.weblist=weblist;
		} else if(this.weblist==null) {
			this.weblist=weblistHandler;
		}
		//CMN.Log("findWebList::", btm, weblist);
		return btm;
	}
	
	MenuItemImpl dummyMenuImpl;
	protected MenuItemImpl getDummyMenuImpl(int id) {
		if (dummyMenuImpl == null) {
			dummyMenuImpl = new MenuItemImpl(AllMenus, 0, id, 0, 0, null, 0);
		}
		return dummyMenuImpl;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();
		MenuItemImpl mmi = item instanceof MenuItemImpl?(MenuItemImpl)item:getDummyMenuImpl(id);
		MenuBuilder menu = (MenuBuilder) mmi.mMenu;
		boolean isLongClicked= mmi!=null && mmi.isLongClicked;
		WebViewListHandler wlh = (WebViewListHandler) menu.tag;
		if (weblist!=wlh) weblist = wlh;
		/* 长按事件默认不处理，因此长按时默认返回false，且不关闭menu。 */
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		switch(id){
			case R.id.wordtoday:{
				showRandomShuffles(false);
			} return true;
			case R.id.refreshRandom:{
				showRandomShuffles(true);
			} break;
			case R.id.refresh:{
				wlh.getMergedFrame().reload();
			} break;
			case R.drawable.ic_settings_black_24dp:{
				launchSettings(0, 0);
			} break;
			case R.id.viewMode:{
				MenuItemImpl tagHolder = getMenuSTd(mmi);
				CMN.debug("onClick::1::", weblistHandler.contentUIData.webholder.getChildCount());
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(tagHolder.tag);
				if(dd==null) {
					DialogInterface.OnClickListener	listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//CMN.Log("onClick::", weblistHandler.contentUIData.webholder.getChildCount());
							if(which>=0 && which<=2) {
								//which=(which+1)%2;
								if(weblistHandler.isMultiRecord()) {
									resetMerge(which, true);
									if (PDICMainAppOptions.remMultiview()) {
										PDICMainAppOptions.multiViewMode(which);
									}
								}
								else return;
							} else if(which==-1){
								launchSettings(Multiview.id, Multiview.requestCode);
							}
							dialog.dismiss();
						}
					};
					dd = new AlertDialog.Builder(this)
						.setSingleChoiceLayout(R.layout.singlechoice_plain)
						.setSingleChoiceItems(new String[]{
								"切换旧版本多页面视图列表"
								, "切换新版合并的多页面模式"
								, "切换新版屏风模式"
						}, 0, listener)
						.setTitleBtn(R.drawable.ic_settings, listener)
						//.setWikiText(getString(R.string.wikiMultiViewMode), null)
						.setTitle("多页面设置").show();
					dd.mAlert.wikiBtn.getLayoutParams().width=GlobalOptions.btnMaxWidth;
					dd.mAlert.wikiBtn.getLayoutParams().height=GlobalOptions.btnMaxWidth*2/3;
					dd.mAlert.wikiBtn.setPadding(0,0,0,0);
					dd.mAlert.wikiSep.getLayoutParams().width=GlobalOptions.btnMaxWidth/5;
					((LinearLayout.LayoutParams)dd.mAlert.wikiSep.getLayoutParams()).weight=0;
					tagHolder.tag = null;
				}
				showMenuDialog(tagHolder, mmi.mMenu, dd);
			}  break;
			case R.id.setToSingleMode:{
				if(isCombinedSearching) {
					toggleBatchSearch();
				}
			}  break;
			case R.id.setToMultiMode:{
				if(!isCombinedSearching) {
					toggleBatchSearch();
				}
			}  break;
			/* 即点即译 */
			case R.id.tapSch:{
				if(isLongClicked){
					popupWord(null, null, 0, null);
					closeMenu=ret=true;
				} else {
					opt.tapSch(wlh.togTapSch());
					item.setChecked(wlh.tapSch);
				}
			} break;
			case R.id.translate:{
				MenuItemImpl tagHolder = getMenuSTd(mmi);
				AlertDialog dd = (AlertDialog)ViewUtils.getWeakRefObj(tagHolder.tag);
				if(dd==null) {
					dd = new AlertDialog.Builder(this)
						.setSingleChoiceLayout(R.layout.singlechoice_plain)
						.setSingleChoiceItems(new String[]{
								"使用谷歌翻译"
								, "使用彩云小译"
								, "关闭"
						}, 0, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Object tag = ((AlertDialog) dialog).tag;
								WebViewListHandler weblistHandler;
								if (tag instanceof WordPopup) {
									weblistHandler = ((WordPopup)tag).weblistHandler;
								}
								else {
									weblistHandler = (WebViewListHandler) ((MenuBuilder)tag).tag;
								}
								if(weblistHandler!=null)
									doTranslation(weblistHandler, which, dialog);
							}
						})
						//.setSingleChoiceItems(strIds, 0, null)
						.setTitle("翻译当前页面").create();
					tagHolder.tag = null;
				}
				showMenuDialog(tagHolder, mmi.mMenu, dd);
			}  break;
			case R.id.fetchWord: {
				if(wlh!=null)
					wlh.setFetchWord(-2, null);
			}  break;
			/* 跳转翻阅模式 */
			case R.id.peruseMode:{
				ret = closeMenu = true;
				isLongClicked &= ActivedAdapter!=null;
				String nowKey=isLongClicked?null:(ActivedAdapter.currentKeyText());
				boolean proceed=true;
				if(true && !isLongClicked){
					WebViewmy currentWebFocus;
					if(getCurrentFocus() instanceof WebViewmy)
						currentWebFocus = (WebViewmy) getCurrentFocus();
					else{
						currentWebFocus = getCurrentWebContext();
					}
					if(currentWebFocus != null && currentWebFocus.bIsActionMenuShown) {
						proceed = false;
						currentWebFocus.evaluateJavascript("getSelection().toString()", value -> {
							String newKey = nowKey;
							if (value.length() > 2) {
								value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
								if (value.length() > 0) {
									newKey = value;
								}
							}
							JumpToPeruseModeWithWord(newKey);
						});
					}
				}
				if(proceed && ActivedAdapter!=null){
					JumpToPeruseModeWithWord(nowKey);
				}
			} break;
		}
		if(closeMenu)
			closeIfNoActionView(mmi);
		return ret;
	}
	
	private void resetMerge(int which, boolean dlg) {
		WebViewListHandler weblist = weblistHandler;
		if (weblist.isMultiRecord()) {
			if(which==-1) which=mergeFrames();
			boolean bUseMergedUrl = which!=0;
			if(which!=weblist.bMergeFrames) {
				weblist.setViewMode(weblist.multiRecord, opt.multiViewMode(), weblist.dictView);
				weblist.bMergeFrames = which;
				weblist.webHolderSwapHide = true;
				// 旧版本切换新版本出现闪黑，
				//boolean delay = opt.getDelayContents();
				//boolean animate = opt.getAnimateContents();
				//if(delay||animate) {
				//	opt.setDelayContents(false);
				//	opt.setAnimateContents(false);
				//}
				viewContent(weblist);
				//if(delay||animate) {
				//	opt.setDelayContents(delay);
				//	opt.setAnimateContents(animate);
				//}
				
				// only handle popup
				//weblist.initMergedFrame(weblist.bMergingFrames, false, false);
				if(recCom!=null)
					recCom.renderContentAt(-2, MainActivityUIBase.this, null, weblist);
			} else {
				if (dlg) {
					showT("已经是了");
				} else {
					weblist.setViewMode(weblist.multiRecord, which, weblist.dictView);
				} 
			}
		}
	}
	
	public void toggleBatchSearch() {
		boolean result = isCombinedSearching = !isCombinedSearching
				, b=isContentViewAttached();
		if (bIsFirstLaunch) {
			bIsFirstLaunch = false;
		}
		if(thisActType==ActType.PlainDict) {
			opt.setCombinedSearching(result);
		} else if(thisActType==ActType.FloatSearch){
			opt.setFloatCombinedSearching(result);
		}
		// switch cs mode will interrupt the user's reading process.
		CombinedSearchTask_lastKey = null;
		if(result) {
			//xxx lv.setVisibility(View.VISIBLE);
			if(b) adaptermy2.currentKeyText=null;
//			if(action1!=null)
//				action1.setIcon((getResources().getDrawable(R.drawable.ic_btn_multimode)));
			lv2.setVisibility(View.VISIBLE);
			menuSearchMode.setIcon(R.drawable.ic_btn_multimode);
		} else {
			if(b) adaptermy.currentKeyText=null;
//			if(action1!=null)
//				action1.setIcon((getResources().getDrawable(R.drawable.ic_btn_siglemode)));
			lv2.setVisibility(View.GONE);
			// nimp
			//if(currentDictionary instanceof bookPresenter_web)
			//	adaptermy.notifyDataSetChanged();
			menuSearchMode.setIcon(R.drawable.ic_btn_siglemode);
		}
		//AllMenus.getItem(0).setIcon(isCombinedSearching?R.drawable.ic_btn_multimode:R.drawable.ic_btn_siglemode);
		
		if(opt.auto_seach_on_switch)
			tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
	}
	
	@NonNull
	public final MenuItemImpl getMenuSTd(MenuItemImpl m) {
		return m.mMenu==AllMenus?m:getMenuSTd(m.getItemId());
	}
	
	@NonNull
	public final MenuItemImpl getMenuSTd(int id) {
		MenuItemImpl ret = (MenuItemImpl) ViewUtils.findInMenu(AllMenusStamp, id);
		if (ret==null) {
			if (AllMenus==null) {
				AllMenus = new MenuBuilder(this);
				AllMenus.tag = weblistHandler;
			}
			if (!(AllMenusStamp instanceof ArrayList)) {
				AllMenusStamp = new ArrayList<>(AllMenusStamp);
			}
			AllMenusStamp.add(ret=new MenuItemImpl(AllMenus, 0, id, 0, 0, null, 0));
		}
		return ret;
	}
	
	protected AlertDialog showMenuDialog(MenuItemImpl tagHolder, Object invoker, AlertDialog menuDialog) {
		if(tagHolder.tag==null) {
			tagHolder.tag=new WeakReference<>(menuDialog);
		}
		menuDialog.tag = invoker;
		ViewUtils.ensureWindowType(menuDialog, this, null);
		menuDialog.show();
		menuDialog.getWindow().setDimAmount(0);
		return menuDialog;
	}
	
	public WebViewListHandler getRandomPageHandler(boolean initPopup, boolean random, BookPresenter presenter) {
		WebViewListHandler weblistHandler = randomPageHandler;
		if(weblistHandler==null)
			weblistHandler = randomPageHandler = new WebViewListHandler(this, ContentviewBinding.inflate(getLayoutInflater()), schuiMain);
		if(initPopup) {
			WebViewmy randomPage = weblistHandler.getMergedFrame();
			weblistHandler.setUpContentView(cbar_key);
			weblistHandler.popupContentView(null, random?"随机页面":null);
			if (presenter == null) {
				randomPage.setPresenter(weblistHandler.mMergedBook);
				weblistHandler.setViewMode(null, 1, randomPage);
				weblistHandler.initMergedFrame(1, true, false);
			} else {
				randomPage.setPresenter(presenter);
				weblistHandler.setViewMode(null, 1, randomPage);
				weblistHandler.viewContent();
				weblistHandler.bShowInPopup=true;
				weblistHandler.bMergeFrames=0;
				weblistHandler.initMergedFrame(1, true, false);
				weblistHandler.pageSlider.setWebview(randomPage, null);
				weblistHandler.resetScrollbar();
			}
			if (!random) {
				randomPageHandler.setFetchWord(0, null);
			}
			weblistHandler.setBottomNavWeb(PDICMainAppOptions.bottomNavWeb());
			randomPage.isloading = true;
			randomPage.active = true;
			randomPage.jointResult = null;
		}
		return randomPageHandler;
	}
	
	// loadWordToday
	public void showRandomShuffles(boolean refresh) {
		try {
			getMdictServer();
			WebViewmy randomPage = getRandomPageHandler(true, true, null).getMergedFrame();
			randomPage.presenter = new_book(defDicts[2], this); //todo generalize
			BookPresenter wikibook = randomPage.presenter;
			PlainWeb webx = wikibook.getWebx();
			randomPageHandler.getMergedFrame(wikibook);
			String testUrl=webx.getHost()+"/randx";
			refreshingRandom = refresh;
			if(refresh || randomPageHandler.fetchWord == 0)
			{
				randomPageHandler.setFetchWord(-1, null);
				randomPage.loadUrl(testUrl);
				randomPageHandler.resetScrollbar(randomPage, false, false);
			}
			randomPageHandler.setViewMode(null, 0, randomPage);
			randomPageHandler.viewContent();
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	public void onSettingsChanged(SettingsActivity settingsActivity, Preference preference) {
		if (TextUtils.equals("dbv2_up", preference.getKey())) {
			DBUpgradeHelper.showUpgradeDlg(settingsActivity, this, false);
		}
	}
	
	public void launchSettings(int id, int result) {
		CMN.pHandler = new WeakReference<>(this);
		Intent intent = new Intent()
				.putExtra("realm", id)
				.setClass(this, SettingsActivity.class);
		if (id==Multiview.id) {
			intent.putExtra("where", weblistHandler.isMultiRecord()?weblistHandler.bMergingFrames:-1);
		}
		if (result==0) {
			startActivity(intent);
		} else {
			startActivityForResult(intent, result);
		}
	}

	void toggleClickSearch(boolean val) {
	}

	ViewGroup webviewHolder;
	Toolbar Searchbar;
	


	void evalJsAtFrame(int index, String exp) {
		if(webviewHolder!=null && webviewHolder.getChildAt(index) instanceof LinearLayout){
			ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
			if(webHolder.getChildAt(1) instanceof WebView){
				((WebView)webHolder.getChildAt(1))
						.evaluateJavascript(exp,null);
			}
		}
	}


	private void setGlobleCE(boolean editable) {
		ViewGroup webviewHolder=ActivedAdapter.webviewHolder;
		if(webviewHolder!=null)
			for (int index = 0; index < webviewHolder.getChildCount(); index++) {
				if(webviewHolder.getChildAt(index) instanceof LinearLayout){
					ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
					if(webHolder.getChildAt(1) instanceof WebViewmy){
						WebViewmy mWebView = ((WebViewmy) webHolder.getChildAt(1));
						if(editable)
							mWebView.evaluateJavascript(ce_off,null);
						else if(mWebView.presenter.getContentEditable())
							mWebView.evaluateJavascript(ce_on,null);
					}
				}
			}
	}

	public void prepareInPageSearch(String key, boolean bNeedBringUp) {
		if(weblistHandler.pageSchEdit ==null){
			weblistHandler.MainPageSearchetSearchStartWord=key;
		}else{
			weblistHandler.pageSchEdit.setText(key);
			bNeedBringUp=bNeedBringUp&&weblistHandler.pageSchBar.getParent()==null;
		}
		if(bNeedBringUp){
			weblistHandler.togSchPage();
		}
	}

	public void showChooseSetDialog() {//切换分组
		AlertDialog dTmp = setchooser.get();
		Bag bag;
		if(dTmp==null) {
			SecordTime = SecordPime = 0;
			CMN.debug("重建对话框……");
			ArrayList<String> scanInList = new ArrayList<>();
			bag = new Bag(opt.getTwoColumnSetView());
			{
				DialogInterface.OnClickListener listener = (dialog, pos) -> {
					if(pos==-1) {
						opt.setTwoColumnSetView(bag.val=!bag.val);
						((BaseAdapter)bag.tag).notifyDataSetChanged();
						setchooser.get().getListView().setSelection(bag.val?lazyLoadManager().lastCheckedPos/2:lazyLoadManager().lastCheckedPos);
					}
					else try {
						lazyLoadManager().lastCheckedPos = pos;
//						loadManager.currentFilter.clear();
//						for (BookPresenter mdTmp : md) {
//							if(mdTmp!=null)
//								mdict_cache.put(mdTmp.getDictionaryName(), mdTmp);
//						}
//						for (BookPresenter mdTmp : loadManager.currentFilter) {
//							if(mdTmp!=null)
//								mdict_cache.put(mdTmp.getDictionaryName(), mdTmp);
//						}
						//todo 123
						String setName = scanInList.get(pos);
						File newf = opt.fileToSet(null, setName);
						boolean lazyLoad = PDICMainAppOptions.getLazyLoadDicts();
						loadManager.LoadLazySlots(newf, lazyLoad, setName);
						loadManager.buildUpDictionaryList(lazyLoad, mdict_cache);
						//todo 延时清空 X
						//mdict_cache.clear();
						//分组切换
						opt.putLastPlanName(LastPlanName, setName);
						if (dictPicker.adapter_idx<0) {
							switch_Dict(0, true, false, null);
						} else if(loadManager.md_getAt(dictPicker.adapterIdx())!=currentDictionary){
							switch_Dict(dictPicker.adapter_idx, true, false, null);
						}
						dialog.dismiss();
						invalidAllLists();
						//show(R.string.loadsucc);
						showTopSnack(null, R.string.loadsucc, -1, -1, Gravity.CENTER, 0);
						if(thisActType==ActType.PlainDict && opt.getCacheCurrentGroup()) {
							// todo 干掉缓冲组
							File def1 = new File(getExternalFilesDir(null), "default.txt");
							if(def1.length()>0) {
								FileOutputStream fout = new FileOutputStream(def1);
								fout.flush();
								fout.close();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						showT(e.getLocalizedMessage());
					}
				};
				dTmp = new AlertDialog.Builder(this, GlobalOptions.isDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line)
						.setTitle(R.string.loadconfig)
						.setTitleBtn(R.drawable.ic_two_column, listener)
						.setSingleChoiceItems(ArrayUtils.EMPTY_STRING_ARRAY, -1, null) //new String[]{}
						.setAdapter((BaseAdapter) (bag.tag=new BaseAdapter() {
								@Override public int getCount() { return bag.val?(int)Math.ceil(scanInList.size()/2.f):scanInList.size(); }
								@Override public Object getItem(int position) { return null; }
								@Override public long getItemId(int position) { return 0; }
								@Override public int getViewTypeCount() { return 2; }
								@Override public int getItemViewType(int position) {
									return bag.val?1:0;
								}
								final View.OnClickListener twoColumnLis = new OnClickListener() {
									public void onClick(View v) {
										ViewGroup sp=(ViewGroup)v.getParent();
										listener.onClick(setchooser.get(), IU.parsint(sp.getTag(), 0)*2+sp.indexOfChild(v));
									}
								};
								@NonNull @Override
								public View getView(int position, View convertView, @NonNull ViewGroup parent) {
									if(bag.val) {
										ViewGroup ret;
										if(convertView!=null){
											ret = (ViewGroup) convertView;
										} else {
											ret = (ViewGroup) getLayoutInflater().inflate(R.layout.singlechoice_two_column, parent, false);
										}
										for (int i = 2; i < ret.getChildCount(); i++) {
											FlowCheckedTextView mFlowTextView = (FlowCheckedTextView) ret.getChildAt(i);
											FlowTextView tv = mFlowTextView.mFlowTextView;
											if(tv.fixedTailTrimCount!=4)
											{
												tv.fixedTailTrimCount=4;
												ret.getChildAt(i-2).setOnClickListener(twoColumnLis);
											}
											tv.setTextColor(AppBlack);
											int pos = position*2+i-2;
											String item=pos<scanInList.size()?scanInList.get(pos):"";
											tv.setText(item);
//											((FlowCheckedTextView)ret.getChildAt(i)).setActivated(false);
											mFlowTextView.setChecked(TextUtils.equals(item, lazyLoadManager().lastLoadedModule));
										}
										ret.setActivated(false);
										ret.postInvalidateOnAnimation();
										ret.setTag(position);
										convertView = ret;
									} else {
										FlowCheckedTextView ret;
										if(convertView!=null){
											ret = (FlowCheckedTextView) convertView;
										} else {
											ret = (FlowCheckedTextView) getLayoutInflater().inflate(R.layout.singlechoice_w, parent, false);
											ret.setMinimumHeight((int) getResources().getDimension(R.dimen._50_));
											ret.mFlowTextView.fixedTailTrimCount=4;
										}
										FlowTextView tv = ret.mFlowTextView;
										tv.setTextColor(AppBlack);
										String item=position<scanInList.size()?scanInList.get(position):"";
										tv.setText(item);
										ret.setChecked(TextUtils.equals(item, lazyLoadManager().lastLoadedModule));
										convertView = ret;
									}
									return convertView;
								}
							})
						, listener)
						.create();
				//dTmp.mAlert.wikiBtn.getDrawable().setColorFilter(0xFF5f5f5f, PorterDuff.Mode.SRC_IN);
				//dTmp.mAlert.wikiBtn.setBackgroundResource(R.drawable.surrtrip1);
			}
			
			ListView dlv = dTmp.getListView();
			dTmp.show();
			dlv.setChoiceMode(ListView.CHOICE_MODE_NONE);
			
			Window window = dTmp.getWindow();
			window.setDimAmount(0);
			
			dlv.setTag(scanInList);
			setchooser = new WeakReference<>(dTmp);
			dTmp.tag = bag;
		}
		else {
//			if(dTmp.getWindow().getAttributes().type!=mDialogType){
//				setchooser.clear();
//				dTmp.dismiss();
//				return;
//			}
			bag = (Bag)dTmp.tag;
			if(ViewUtils.DGShowing(dTmp)){
				return;
			}
		}
		ViewUtils.ensureWindowType(dTmp, MainActivityUIBase.this, null);
		
		File ConfigFile = opt.fileToConfig();
		File def = opt.fileToSecords(ConfigFile);
		long l1=def.lastModified();
		long l2=def.getParentFile().lastModified();
		boolean b1 = l1!=SecordTime;
		ListView lv = dTmp.getListView();
		float pad = 2.8f * getResources().getDimension(R.dimen._50_) * (dm.widthPixels>GlobalOptions.realWidth?1:1.45f);
		//CMN.debug("pad::", pad, root.getHeight());
		((AlertController.RecycleListView) lv).mMaxHeight = root.getHeight()>=2*pad?(int) (root.getHeight() - root.getPaddingTop() - pad):0;
		if(b1 || l2!=SecordPime) {
			int lastCheckedPos=-1;
			ArrayList<String> scanInList = (ArrayList) lv.getTag();
			final HashSet<String> con = new HashSet<>();
			if(b1) {
				CMN.debug("扫描1……");
				scanInList.clear();
				try {
					AgentApplication app = ((AgentApplication) getApplication());
					ReusableBufferedReader in = new ReusableBufferedReader(new FileReader(def), app.get4kCharBuff(), 4096);
					String line = in.readLine();
					while (line != null) {
						line = SU.legacySetFileName(line);
						if (con.add(line) && opt.fileToSet(ConfigFile, line).exists()) {
							scanInList.add(line);
							if (line.equals(opt.lastMdPlanName)) {
								lastCheckedPos = scanInList.size() - 1;
							}
						}
						line = in.readLine();
					}
					in.close();
				}
				catch (Exception e) {  CMN.debug(e);  }
				SecordTime = l1;
			}
			if(l2!=SecordPime) {
				if(!b1) {
					con.addAll(scanInList);
				}
				String[] names= ConfigFile.list();
				int start = scanInList.size();
				if(names!=null) {
					for (String name:names) {
						if(!SU.isNotGroupSuffix(name) && con.add(name)) {
							scanInList.add(name);
						}
					}
					if(scanInList.size()>start) {
						CMN.debug("扫描2……");
						String addition = StringUtils.join(scanInList.subList(start, scanInList.size()), "\n");
						BU.appendToFile(def, "\n", addition, "\n");
					}
				}
				SecordPime = l2;
			}
			CMN.debug("扫描分组……", scanInList.size(), def.getParentFile().lastModified());
			((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
			if(lastCheckedPos>=0) {
				dTmp.getListView().setSelection(bag.val?lastCheckedPos/2:lastCheckedPos);
				lazyLoadManager().lastCheckedPos = lastCheckedPos;
			}
		}
		
		dTmp.show();
	}

	abstract public void invalidAllLists();

	@Override
	public void onDismiss(DialogInterface dialog) {
		d=null;
	}
	
	public static boolean layoutScrollDisabled;
	protected WebViewmy NaugtyWeb;
	boolean bShowCustomView;
	public static long CustomViewHideTime;
	private int mOldOri;
	
	@SuppressLint("SourceLockedOrientationActivity")
	public void fixVideoFullScreen(){
		if(bShowCustomView){
			mOldOri = mConfiguration.orientation;
			int mode = opt.getFullScreenLandscapeMode();
			boolean bSwitch = mode==0;
			if(mode==2 && BookPresenter._req_fvw!=0 && BookPresenter._req_fvh!=0){
				bSwitch = BookPresenter._req_fvw> BookPresenter._req_fvh;
			}
			if(bSwitch)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			bShowCustomView =false;
		}
	}
	@SuppressLint("SourceLockedOrientationActivity")
	public WebChromeClient myWebCClient = new WebChromeClient() {
		private File filepickernow;
		Dialog d; View cv;
		
		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			CMN.debug("onJsPrompt 0");
			result.confirm();
			return true;
		}
		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
			CMN.debug("onJsPrompt 1");
			result.confirm();
			return true;
		}
		//@Override
		//public boolean onJsTimeout() {
		//	return false;
		//}
		
//		@Override
//		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//			return true;
//		}
		
		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
			CMN.debug("onJsPrompt 2");
			result.confirm(defaultValue);
			return true;//super.onJsPrompt(view, url, message, defaultValue, result);
		}
		
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			CMN.debug("onShowCustomView", BookPresenter._req_fvw, BookPresenter._req_fvh);
			bShowCustomView = true;
			if(opt.getFullScreenLandscapeMode()!=2)fixVideoFullScreen();
			if(d ==null){
				d = new Dialog(MainActivityUIBase.this);
				d.setCancelable(false);
				d.setCanceledOnTouchOutside(false);
			}

			d.show();

			Window window = d.getWindow();
			window.setDimAmount(1);
			WindowManager.LayoutParams layoutParams = window.getAttributes();
			layoutParams.width = MATCH_PARENT;
			layoutParams.height = MATCH_PARENT;
			layoutParams.horizontalMargin = 0;
			layoutParams.verticalMargin = 0;
			window.setAttributes(layoutParams);

			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

			window.getDecorView().setBackground(null);
			window.getDecorView().setPadding(0,0,0,0);
			if(view!=null) d.setContentView(cv=view);
		}
		
		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			//((WebView.WebViewTransport)resultMsg.obj).setWebView(randomPage);
			//resultMsg.sendToTarget(); New WebView for popup window must not have been  previously navigated. ???
			// https://stackoverflow.com/questions/9654529/handle-url-from-oncreatewindow-webview
			Message href = view.getHandler().obtainMessage();
			view.requestFocusNodeHref(href);
			String url = href.getData().getString("url");
			CMN.debug("onCreateWindow::", url);
			if (url == null) {
				return false;
			}
			BookPresenter presenter = webxford.get(SubStringKey.new_hostKey(url));
			String urlKey = null;
			if (presenter == null) {
				int idx = url.indexOf("/entry/");
				if (idx > 0) urlKey = URLDecoder.decode(url.substring(idx + 7));
				else {
					idx = url.indexOf("entry://");
					if (idx > 0) urlKey = URLDecoder.decode(url.substring(idx + 8));
				}
				presenter = ((WebViewmy) view).presenter;
			} else {
				try {
					presenter.getWebx().getVirtualRecordAt(presenter, 0); //todo opt webx
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
			// getRandomPageHandler(true, false, null);
			WebViewListHandler wlh = getRandomPageHandler(true, false, presenter);
			WebViewmy randomPage = wlh.getMergedFrame();
			wlh.setStar(urlKey);
			try {
				href.recycle(); // This message cannot be recycled because it is still in use. ???
			} catch (Exception e) {
				CMN.debug(e);
			}
			randomPage.loadUrl(url);
			return true;
		}
		
		@Override
		public void onHideCustomView() {
			bShowCustomView = false;
			if (mOldOri==Configuration.ORIENTATION_PORTRAIT&&mConfiguration.orientation==Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			if(d !=null){
				d.hide();
			}
			if(cv!=null && cv.getParent()!=null){
				((ViewGroup)cv.getParent()).removeView(cv);
				cv=null;
			}
			CustomViewHideTime = System.currentTimeMillis();
		}
		
		@Override
		public void onProgressChanged(final WebView view, int newProgress) {
			CMN.debug("ProgressChanged", newProgress);
			WebViewmy mWebView = (WebViewmy) view;
			if (mWebView.bPageStarted) {
				//todo undo changes made to webview by web dictionaries.
				//if(mWebView.fromCombined==4){
				if (mWebView.fromNet()) {
					final BookPresenter invoker = mWebView.presenter;
					invoker.getWebx().onProgressChanged(invoker, mWebView, newProgress);
				}
			}
			
			if(GlobalOptions.isDark && newProgress>5) {
				if (Build.VERSION.SDK_INT>=21) { //todo webview版本 23 未测试
					mWebView.evaluateJavascript("document._pdkn||app.loadJs(sid.get(), 'dk.js')", null);
				} else {
					mWebView.evaluateJavascript(DarkModeIncantation, null);
				}
			}
			
			if (newProgress>=90) {
				if(ViewUtils.littleCat) {
					ViewUtils.KitPatch(MainActivityUIBase.this, mWebView);
				}
			}
		}

		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
			//CMN.Log(fileChooserParams.getAcceptTypes());
			DialogProperties properties = new DialogProperties();
			properties.selection_mode = DialogConfigs.SINGLE_MULTI_MODE;
			properties.selection_type = DialogConfigs.FILE_SELECT;
			properties.root = new File("/");
			properties.error_dir = Environment.getExternalStorageDirectory();
			properties.offset = filepickernow!=null?filepickernow:opt.lastMdlibPath;
			properties.opt_dir=new File(opt.pathToDatabases()+"favorite_dirs/");
			properties.opt_dir.mkdirs();
			properties.extensions = new HashSet<>();
			properties.extensions = null;
			properties.title_id = R.string.addd;
			properties.isDark = AppWhite==Color.BLACK;
			FilePickerDialog dialog = new FilePickerDialog(MainActivityUIBase.this, properties);
			dialog.setDialogSelectionListener(new DialogSelectionListener() {
				@Override
				public void onSelectedFilePaths(String[] files,File now) {
					filepickernow=now;
					ArrayList<Uri> urls = new ArrayList<>(files.length);
					for (int i = 0; i < files.length; i++) {
						urls.add(Uri.fromFile(new File(files[i])));
					}
					filePathCallback.onReceiveValue(urls.toArray(new Uri[files.length]));
				}
				@Override
				public void onEnterSlideShow(Window win, int delay) { }
				@Override
				public void onExitSlideShow() { }
				@Override
				public Activity getDialogActivity() {
					return null;
				}
				@Override
				public void onDismiss() {
					filePathCallback.onReceiveValue(null);
				}
			});
			dialog.show();
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

			return true;
		}
	};
	
	void doTranslation(WebViewListHandler weblistHandler, int which, DialogInterface dialog) {
		WebViewmy mWebView = weblistHandler.getWebContext();
		final int szTranslators = 2;
		CMN.debug("doTranslation::", mWebView, weblistHandler.bMergingFrames);
		if(mWebView!=null) {
			boolean off;
			if(which==szTranslators) {
				off = true;
				which = mWebView.translating;
				if(which==-1) which = new Random().nextInt(szTranslators);
				mWebView.translating = -1;
			} else {
				off = false;
				mWebView.translating = which;
			}
			if(which<=szTranslators) {
				try {
					BookPresenter gTrans = new_book(defDicts[which==0?1:3], this);
					PlainWeb webx = gTrans.getWebx();
					if(webx.getHasModifiers())
					{
						weblistHandler.moders.remove(webx);
						weblistHandler.moders.add(webx);
					}
					mWebView.evaluateJavascript(webx.getPageTranslator(off), null);
					if(dialog!=null) dialog.dismiss();
				} catch (Exception e) { CMN.debug(e); }
			}
		}
	}
	
	protected boolean delayedAttaching;
	
	public WebViewClient myWebClient = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			WebViewmy mWebView = (WebViewmy) view;
			// CMN.debug("onPageStarted::"+mWebView.wvclient);
			if(mWebView.wvclient!=null) {
				mWebView.bPageStarted=true;
				final BookPresenter invoker = mWebView.presenter;
				if(invoker.getIsWebx()) {
					((PlainWeb)invoker.bookImpl).onPageStarted(invoker, view, url, true);
				}
				mWebView.initScale();
				mWebView.recUrl(url);
			}
			if(view==wordPopup.mWebView) {
				wordPopup.onPageStart(url);
			}
		}
		
		public void onPageFinished(WebView view, String url) {
			final WebViewmy mWebView = (WebViewmy) view;
			CMN.debug("onPageFinished::", "st="+mWebView.bPageStarted, url, "st1="+mWebView.isloading, "scl="+mWebView.webScale, "web="+mWebView.presenter.isWebx, "aud="+mWebView.bRequestedSoundPlayback);
			if (false) {
				mWebView.initPos();
			}
			if (mWebView.bPageStarted) {
				mWebView.bPageStarted = false;
				mWebView.recUrl(url);
			} else {
				return;
			}
			if("about:blank".equals(url)/* || !mWebView.active&&!mWebView.fromNet*/) {
				return;
			}
			//CMN.debug("chromium: OPF ==> ", url, mWebView.isloading, view.getProgress(), view.getTag(R.drawable.voice_ic));
			
			//if(!mWebView.isloading && !mWebView.fromNet) return;
			final WebViewListHandler wlh = mWebView.weblistHandler;
			
			BookPresenter invoker = mWebView.presenter, book=invoker;
			
			if(wlh.isViewSingle())
				invoker.ApplyPadding(mWebView);
			if (mWebView.translating>=0) {
				doTranslation(wlh, mWebView.translating, null);
			}
			if(delayedAttaching && mWebView.presenter==currentDictionary) { // todo same replace (mWebView.SelfIdx==adapter_idx, ->)
				((PDICMainActivity)MainActivityUIBase.this).AttachContentViewDelayed(10);
			}
			int schemaIdx = url.indexOf(":");
			boolean mdbr = url.regionMatches(schemaIdx+3, "mdbr", 0, 4), baseUrl=false;
			if (wlh.fetchWord!=0 /*&& !url.endsWith("/randx")*/) { // for random page
				mWebView.evaluateJavascript("window.randx_mode="+wlh.fetchWord, null);
				mWebView.evaluateJavascript(randx_on, null);
			}
			if(mdbr) {
				if (url.regionMatches(schemaIdx+12, "merge", 0, 5)) {
					invoker = wlh.getMergedBook();
					int idx = url.lastIndexOf("&did=");
					long did = IU.parseLong(url.substring(idx+5), -1);
					if (did!=mWebView.currentPos || mWebView.presenter!=invoker) {
						mWebView.presenter=invoker;
						idx = url.indexOf("q=", schemaIdx+12+5+1)+2;
						int ed = url.indexOf("&", idx); if(ed<0) ed=url.length();
						wlh.setStar(/*mWebView.word = */URLDecoder.decode(url.substring(idx, ed)));
						mWebView.currentPos = did;
						//CMN.debug("view::merged::changed!!!", wlh.displaying);
						wlh.changeViewMode(mWebView, url);
					}
				}
				else if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
					int idx=schemaIdx+12+7+1;
					invoker = getMdictServer().md_getByURLPath(url, idx, url.indexOf("_", idx));
				}
				else if (url.regionMatches(schemaIdx+12, "base", 0, 4)) {
					int idx=schemaIdx+12+5;
					if (url.charAt(idx)=='/') { // base/d0/entry/...
						idx++;
						invoker = getMdictServer().md_getByURLPath(url, idx, url.indexOf("/", idx));
					}
					else if (url.charAt(idx)=='.') { // base.html
						baseUrl = true; /** see {@link #baseUrl} */
						//mWebView.clearHistory();
					}
				}
				CMN.debug("ivk::raw::", invoker);
//				else if (url.regionMatches(schemaIdx+12, "content", 0, 7)) {
//					int st = schemaIdx+12+7+1;
//					int ed = url.indexOf("-", st);
//					if (ed<0) ed = url.indexOf("#", st);
//					if (ed<0) ed = url.length();
//					long pos = IU.TextToNumber_SIXTWO_LE(new CharSequenceKey(url, st, ed));
//					CMN.Log("content!!!", url.substring(st), pos, IU.NumberToText_SIXTWO_LE(pos, null));
//					if (mWebView.currentPos!=pos) {
//					}
//					mWebView.toolbar_title.setText(invoker.getBookEntryAt((int) pos));
//				}
//				else if (url.regionMatches(schemaIdx+12, "base/", 0, 5)) {
//					int idx=url.indexOf("/", schemaIdx+12+5)+1;
//					if (url.regionMatches(idx, "entry", 0, 5)) {
//						idx=idx+5+1;
//						CMN.Log("content!!! entry", url.substring(idx));
//						mWebView.toolbar_title.setText(url.substring(idx));
//					}
//				}
			}
			if(invoker.getIsWebx())
			{
				((PlainWeb)invoker.bookImpl).onPageFinished(invoker, mWebView, url, true);
			}
			/* 优先级： #页面锚点， 页内搜索🔍， 记忆位置🧲 */
			else if(mWebView.isloading){
				//if(baseUrl)
				{
					String toHash = mWebView.toTag;
					// naive reset zoom : view.zoomBy(0.02f);
					if(toHash!=null)
					{
						CMN.debug("toHash::", toHash);
						mWebView.toTag=null;
						View rl = (View) mWebView.getParent();
						mWebView.expectedPos = -100;
						if(mWebView.fromCombined==1){
							wlh.WHP.touchFlag.first=true;
								if (toHash.equals("===000")) {
									//showT("正在跳往子页面顶端…" + invoker.rl.getTop() + "?" + weblistHandler.WHP.getChildAt(0).getHeight());
									wlh.WHP.post(() -> {
										wlh.WHP.smoothScrollTo(0, rl.getTop());
									});
								} else {
									view.evaluateJavascript("var item=document.getElementsByName(\"" + toHash + "\")[0];item?item.getBoundingClientRect().top:-1;"
										, v -> {
											int to = (int)(Float.valueOf(v) * getResources().getDisplayMetrics().density);
											if(to>=0)
												wlh.WHP.smoothScrollTo(0, rl.getTop() - toolbar.getHeight() + to);
										});
								}
						}
						else {
							//CMN.debug("toTag::", toTag);
							if("===???".equals(toHash))
								recCom.handleNavJump(MainActivityUIBase.this, mWebView);
							else if(!toHash.equals("===000")) {
//								view.evaluateJavascript("location.replace(\"#" + toHash + "\");", null);
								view.evaluateJavascript("var h='"+toHash+"',d=document;(d.getElementById(h)||d.getElementsByName(h)[0]).scrollIntoView()", null);
							}
						}
					}
					else if(PDICMainAppOptions.schPageAutoTurn() && wlh.schPage(mWebView))
					{ //toHighLight
						jumpNaughtyFirstHighlight(mWebView);
					}
					else if (mWebView.weblistHandler.isViewSingle() && !mWebView.weblistHandler.bDataOnly)
					{
						if (mWebView.expectedPos >= 0) {
							lastClickTime = System.currentTimeMillis();
							//layoutScrollDisabled=true;
							CMN.debug("initial_push: ", mWebView.expectedPosX, mWebView.expectedPos);
							//mWebView.zoomBy(mWebView.expectedZoom/mWebView.webScale);
							mWebView.scrollTo(mWebView.expectedPosX, mWebView.expectedPos);
							NaugtyWeb = mWebView;
							if (hdl != null)
								hdl.sendEmptyMessage(778899);
							layoutScrollDisabled = true;
						}
					}
					//CMN.debug("expectedPos::", mWebView.expectedPos, mWebView.getScrollY(), "toHighLight="+toHighLight);
				}
				// 非 #baseUrl 方式加载，通过 /content/ /base/ 加载的，只需处理
			}
			
			mWebView.isloading = false;
			
			if(invoker.getImageBrowsable() && invoker.bookImpl.hasMdd())
				mWebView.evaluateJavascript(BookPresenter.imgLoader, null);
			
			if (mWebView.hasFilesTag) {
				mWebView.evaluateJavascript(BookPresenter.jsFileTag, null);
				mWebView.hasFilesTag = false;
			}
			
			if (mWebView.changed!=0) {
				BookPresenter finalInvoker = invoker;
				if (mWebView.changed==1) {
					finalInvoker.tintBackground(mWebView);
				}
				wlh.changeViewMode(mWebView, url);
				mWebView.evaluateJavascript(BookPresenter.jsViewChanged, new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						finalInvoker.setCurrentDis(mWebView, mWebView.currentPos);
						final WebViewListHandler wlh = mWebView.weblistHandler;
						if (finalInvoker.isMergedBook() || mWebView.isViewSingle() && wlh.scrollFocus==mWebView) {
							int frame = mWebView.frameAt;
							if (wlh.getFrameAt(frame)!=mWebView.presenter) {
								frame = wlh.entrySeek.getProgress();
							}
							wlh.setScrollFocus(mWebView, frame);
						}
					}
				});
				mWebView.changed = 0;
			}
			
			if(mWebView.bRequestedSoundPlayback) readEntry(mWebView);
			
			if(invoker.GetSearchKey()!=null)
				invoker.ApplySearchKey(mWebView);
			
			if (wlh.tapSch && !invoker.getImageOnly())
				ViewUtils.TapSch(MainActivityUIBase.this, mWebView);
			
			if(PDICMainAppOptions.toolsBoost())
				ViewUtils.toolsBoost(mWebView);
			
			if(mWebView.fromCombined==1 && awaiting) loadNext(mWebView);
		}


		public void  onScaleChanged(WebView view, float oldScale,float newScale) {
			//CMN.Log(oldScale, "-", newScale, " newScale");
			WebViewmy mWebView = ((WebViewmy)view);
			BookPresenter invoker = mWebView.presenter;
			super.onScaleChanged(view, oldScale,newScale);
			mWebView.webScale=newScale;
			if(view==invoker.mWebView) {
				invoker.webScale = newScale; //sync
			}
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			//CMN.Log("onReceivedError::", errorCode, failingUrl);
//			if (errorCode==ERROR_HOST_LOOKUP ) {
//
//			}
			//view.loadUrl("mdbr://load.html");
			if(GlobalOptions.isDark && Build.VERSION.SDK_INT<21) { //todo webview 版本
				view.loadDataWithBaseURL(baseUrl, "<div>\n" +
						"hello world!\n" +
						"</div>", null, "UTF-8", null);
			}
		}
		
		@Override
		public void onLoadResource(WebView view, String url) {

		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			final WebViewmy mWebView = (WebViewmy) view;
			CMN.debug("chromium SOUL::",url,view.getTag());
			if(mWebView.forbidLoading) {
				return true;
			}
			final BookPresenter invoker = mWebView.presenter;
			if(invoker==null) return false;
			WebViewListHandler wlh = mWebView.weblistHandler;
			boolean fromPopup = view==wordPopup.mWebView;
			if (mWebView.lastSuppressLnkTm >0) {
				if (CMN.now()- mWebView.lastSuppressLnkTm <350) {
					if (wlh.fetchWord>0) {
						mWebView.evaluateJavascript(randx_remake, null);
					}
					return true;
				}
				mWebView.lastSuppressLnkTm = 0;
			}
			if(invoker.getIsWebx()) {
				PlainWeb webx = (PlainWeb) invoker.bookImpl;
				try {
					if (!url.startsWith("http") && !url.startsWith("https") && !url.startsWith("ftp") && !url.startsWith("file")) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				boolean ret = webx.hasExcludedUrl && webx.shouldExcludeUrl(url);
				if(!ret){
					if(webx.hasReroutedUrl) {
						String urlnew = webx.shouldRerouteUrl(url);
						if(urlnew!=null) {
							view.loadUrl(urlnew);
							ret = true;
						}
					}
					if (fromPopup) {
//						wordPopup.popupHistory.add(++wordPopup.popupHistoryVagranter, new myCpr<>(wordPopup.displaying, new int[]{wordPopup.CCD_ID, wordPopup.currentPos}));
//						if (wordPopup.popupHistory.size() > wordPopup.popupHistoryVagranter + 1) {
//							wordPopup.popupHistory.subList(wordPopup.popupHistoryVagranter + 1, wordPopup.popupHistory.size()).clear();
//						}
					}
				}
				return ret;
			}
			//CMN.Log("chromium shouldOverrideUrlLoading_",url);
			//TODO::改写历史纪录方式，统一操作。
			String msg=null;
			if (url.startsWith("file:///#")) {
				url = entryTag+url.substring(8);
			}
			if (url.startsWith("pdf://")) {
				int end = url.lastIndexOf("#");
				int pageId = -1;
				if (end != -1) {
					pageId = IU.parseInt(url.substring(end + 1));
				}
				Intent it = new Intent(Intent.ACTION_VIEW);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				File f = new File("/sdcard/PLOD/PDFs/李白全集.pdf");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					//StrictMode.setVmPolicy(StrictMode.getVmPolicy());
					StrictMode.setVmPolicy(new VmPolicy.Builder().build());
				}
				if (pageId != -1)
					it.putExtra("page", pageId);
				it.setDataAndType(Uri.fromFile(f), "application/pdf");
				startActivity(it);
				return true;
			}
			if (url.startsWith("http://") || url.startsWith("https://")) {
				//CMN.Log("shouldOverrideUrlLoading_http",url);
				int schemaIdx = url.indexOf(":");
				CMN.debug("invoker.isMergedBook()::", invoker.isMergedBook(), url.regionMatches(schemaIdx+12, "base", 0, 4), !wlh.bShowingInPopup && opt.popViewEntry(), url.regionMatches(url.indexOf("/", schemaIdx+12+1+4+2)+1, "entry", 0, 5));
				if(invoker.isMergedBook() || url.indexOf("mdbr.com")>0) {
					//todo
					return false;
				}
				if (opt.bShouldUseExternalBrowserApp) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					return true;
				}
			}
			else if (url.startsWith(soundTag)) {
				try {
					if (invoker.bookImpl.hasMdd()) {
						//CMN.Log("hijacked sound playing : ",url);
						String soundUrl = URLDecoder.decode(url.substring(soundTag.length()), "UTF-8");
						if(PDICMainAppOptions.getCacheSoundResInAdvance()){
							playCachedSoundFile(mWebView, soundUrl, invoker, false);
							return true;
						}
						String Incan = playsoundscript + ("('" + soundUrl + "')");
						view.evaluateJavascript(Incan, null);
					} else
						view.evaluateJavascript("var hrefs = document.getElementsByTagName('a'); for(var i=0;i<hrefs.length;i++){ if(hrefs[i].attributes['href']){ if(hrefs[i].attributes['href'].value=='" + URLDecoder.decode(url, "UTF-8") + "'){ hrefs[i].removeAttribute('href'); hrefs[i].click(); break; } } }", null);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
			else if (url.startsWith(entryTag)) {
				try {
					url = URLDecoder.decode(url, "UTF-8");
				} catch (Exception ignored) { }
				boolean fromCombined = !wlh.isViewSingle();
				boolean pop = !wlh.bShowingInPopup // popup new content displayer
						&& (fromCombined && opt.popViewEntryMulti()
								|| !fromCombined && opt.popViewEntryOne());
				/* 页内跳转 */
				if (url.startsWith("entry://#")) {
					//Log.e("chromium inter_ entry3", url);
					if (!fromCombined) {// 三种情况：normal，peruseview， popup查询。
						/* '代管'之含义：在网页前进/后退之时，不使用系统缓存，而是手动加载、手动定位。 */
						ScrollerRecord PageState;
						mWebView.expectedPos = -100;
						((WebViewmy) view).isloading = true;
//						view.evaluateJavascript("location.replace(\"" + url.substring(entryTag.length()) + "\");", null);
						view.evaluateJavascript("var h='"+url.substring(entryTag.length())+"',d=document;(d.getElementById(h)||d.getElementsByName(h)[0]).scrollIntoView()", null);
					}
					else {//TODO 精确之
						view.evaluateJavascript("var item=document.getElementsByName(\"" + url.substring(entryTag.length() + 1) + "\")[0]; item?item.getBoundingClientRect().top:-1;"
								, v -> {
									int to=(int)(Float.valueOf(v) * getResources().getDisplayMetrics().density);
									if(to>=0)
										weblistHandler.WHP.smoothScrollTo(0, invoker.rl.getTop() - toolbar.getHeight() + to);
								});
					}
					//invoker.setCurrentDis(mWebView, mWebView.currentPos);
					return true;
				}
				/* 书签跳转 */
				else if (bookMarkEntryPattern.matcher(url).matches()) {
					//Log.e("chromium inter_ entry3", url);
					try {
						//CMN.a.ActivedAdapter.onItemClick(lookUp(URLDecoder.decode(url,"UTF-8")));
						//Log.e("chromium inter entry2",URLDecoder.decode(url,"UTF-8"));
						//if(!opt.isCombinedSearching)
						//	a.jumpHistory.add(new Pair(currentDisplaying, mWebView.getScrollY()));
						//a.jump(url, mdict.this);
						Integer pos = IU.parseInt(url.substring(entryTag.length() + 1));
						if (pos == null) return true;
						//nimp
						//if (invoker instanceof bookPresenter_pdf) {
						//	view.evaluateJavascript("window.PDFViewerApplication.page=" + pos, null);
						//	return true;
						//}
						//todo delay save states
						// invoker.isJumping = true;
						// invoker.setCurrentDis(mWebView, pos);
						view.loadDataWithBaseURL(baseUrl,
								invoker.AcquirePageBuilder().append((AppWhite == Color.BLACK) ? MainActivityUIBase.DarkModeIncantation_l : "")
										.append(BookPresenter.htmlHeadEndTag)
										.append(invoker.bookImpl.getRecordsAt(invoker.mBookRecordInteceptor, pos))
										.append(invoker.htmlEnd).toString()
								, null, "UTF-8", null);
						jump(pos, invoker);
						return true;
					} catch (Exception ignored) {
					}
				}
				/* 普通的词条跳转 */
				else {
					//CMN.Log("chromium inter_ entry2", url);
					url = url.substring(entryTag.length());
					try {
						boolean popup = invoker.getPopEntry();
						if(popup) {
							wordPopup.init();
							wordPopup.mWebView.frameAt = mWebView.frameAt;
							//popupWebView.SelfIdx = mWebView.SelfIdx;
							//mWebView = wordPopup.mWebView;
						}
						int tagIdx = url.indexOf("#");
						if (tagIdx > 0) {
							mWebView.toTag = url.substring(tagIdx + 1);
							url = url.substring(0, tagIdx);
						}
						else if(mWebView.toTag!=null) mWebView.toTag = null;
						if(url.endsWith("/")) url=url.substring(0, url.length()-1);
						url = URLDecoder.decode(url, "UTF-8");
						if(popup){
							popupWord(url, mWebView.presenter, mWebView.frameAt, mWebView);
							return true;
						}
						else {
							/* 查询跳转目标 */
							int idx = invoker.bookImpl.lookUp(url, true);
							CMN.debug("查询跳转目标 : ", idx, URLDecoder.decode(url,"UTF-8"), (URLDecoder.decode(url,"UTF-8")));
							if (idx >= 0) {//idx != -1
								if(pop) { // 新窗口打开词条跳转
									wlh = getRandomPageHandler(true, true, null);
									if (mWebView.toTag!=null) {
										wlh.getMergedFrame().toTag = mWebView.toTag;
										mWebView.toTag = null;
									}
									WebViewmy wv = wlh.getMergedFrame();
									wlh.viewContent();
									wlh.setViewMode(null, 0, wv);
									wlh.bShowInPopup=true;
									wlh.bMergeFrames=0;
									wlh.initMergedFrame(0, true, false);
									wlh.popupContentView(null, url);
									invoker.renderContentAt(-1,BookPresenter.RENDERFLAG_NEW,0,wv, idx);
									wlh.pageSlider.setWebview(wv, null);
									wlh.resetScrollbar();
									if(wv.getBackgroundColor()==0) wv.setBackgroundColor(GlobalPageBackground); //todo optimize
									return true;
								}
								else if(!fromPopup) {
									//peruseView.isJumping = true;
									//invoker.isJumping = true;
									//peruseView.setCurrentDis(invoker, idx);
									//invoker.setCurrentDis(mWebView, idx);
									if (mWebView.toTag == null) {
										/* 跳转至顶部 */
										mWebView.toTag = "===000";
										mWebView.expectedPosX = 0;
										mWebView.expectedPos = 0;
									} else {
										/* 什么都不要做 */
										mWebView.expectedPos = -100;
									}
								}
//								invoker.renderContentAt(-1, RENDERFLAG_NEW, mWebView.frameAt, mWebView, idx);
								float initialScale = BookPresenter.def_zoom;
								mWebView.setInitialScale((int) (100 * (initialScale / BookPresenter.def_zoom) * opt.dm.density));
								mWebView.isloading = true;
								invoker.setCurrentDis(mWebView, idx);
								StringBuilder htmlBuilder = invoker.AcquirePageBuilder();
								invoker.AddPlodStructure(mWebView, htmlBuilder, invoker.rl==mWebView.getParent()&&invoker.rl.getLayoutParams().height>0);
								String htmlCode = invoker.bookImpl.getRecordsAt(null, idx);
								if(invoker.hasFilesTag()){
									htmlCode = htmlCode.replace("file://", "");
								}
								invoker.LoadPagelet(mWebView, htmlBuilder, htmlCode);
								return true;
							}
						}
					}
					catch (Exception e) {
						//TODO !!!
						msg=e.toString();
						if(true) CMN.debug(e);
					}
				}
				/* 跳转失败 */
				String showError = getResources().getString(R.string.jumpfail) + url;
				if(msg!=null) showError+=" "+msg;
				showT(showError);
				return true;
			}
			if(fromPopup){
//				wordPopup.popupHistory.add(++wordPopup.popupHistoryVagranter, new myCpr<>(wordPopup.displaying, new int[]{wordPopup.CCD_ID, wordPopup.currentPos}));
//				if (wordPopup.popupHistory.size() > wordPopup.popupHistoryVagranter + 1) {
//					wordPopup.popupHistory.subList(wordPopup.popupHistoryVagranter + 1, wordPopup.popupHistory.size()).clear();
//				}
			}
			try {
				if (!url.startsWith("http") && !url.startsWith("https") && !url.startsWith("ftp") && !url.startsWith("file")) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					if (isInstall(intent)) {
						startActivity(intent);
						return true;
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
			return false;
		}
		private boolean isInstall(Intent intent) {
			return getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
		}

		@Nullable @Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String Url) {
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
				return null;
			return shouldInterceptRequestCompat(view, Url, null, null, null, null);
		}

		public WebResourceResponse shouldInterceptRequest (final WebView view, WebResourceRequest request) {
			if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP)
				return null;
			Map<String, String> keyset = request.getRequestHeaders();
			//for (String key:keyset.keySet()) CMN.Log(request.getUrl(), "keyset : ", key, " :: ",keyset.get(key),request.getMethod());
			return shouldInterceptRequestCompat(view, request.getUrl().toString(), keyset.get("Accept"), keyset.get("Referer"), keyset.get("Origin"), request);
		}

		private WebResourceResponse shouldInterceptRequestCompat(WebView view, String url, String accept, String refer, String origin, WebResourceRequest request) {
			//CMN.debug("chromium shouldInterceptRequest???",url,view.getTag());
			//if(true) return null;
			if(url.startsWith("data:")) return null;
			
			WebViewmy mWebView = (WebViewmy) view;
			BookPresenter invoker = mWebView.presenter;
			if(invoker==null) return null;
			
			WebViewListHandler wlh = mWebView.weblistHandler;
			
			String key=null;
			int schemaIdx = url.indexOf(":");
			WebResourceResponse fakedDomainResponse = null;
			if(schemaIdx==-1){
				if(url.startsWith("./"))
					key=url.substring(1);
			} else {
				if(url.startsWith("file://")) {
					// fix for images not loading when nav back/forward.
					invoker.hasFilesTag(true);
					invoker.isDirty = true;
				}
				else if (url.startsWith("http")) {
					if (wlh==randomPageHandler && url.endsWith("/randx")) { // for random page
						BookPresenter webx = webxford.get(SubStringKey.new_hostKey(url)); //todo opt
						if (webx!=null && webx.isWebx) {
							return new WebResourceResponse("text/html", "utf8", new ByteArrayInputStream(webx.getWebx().getRandx().getBytes()));
						}
					}
					boolean mdbr = url.regionMatches(schemaIdx+3, "mdbr", 0, 4) && url.length()>12;
					boolean merge = invoker.isMergedBook() || mWebView.merge;
					if (mdbr) {
						int slashIdx = url.indexOf("/", schemaIdx+7);
						if(slashIdx<0) slashIdx = url.length();
						if (url.charAt(schemaIdx+8)=='d') {
							// loaded with base url
							if (!url.regionMatches(schemaIdx+9, invoker.idStr10, 0, slashIdx-schemaIdx-9)) {
								invoker = getBookById(IU.parseLong(url.substring(schemaIdx+9, slashIdx)));
								//CMN.Log("view::changed::res::", invoker);
							}
						}
						if (slashIdx==schemaIdx+7 || url.regionMatches(slashIdx+1, "MdbR", 0, 4)) {
							try { // 内置资源
								if (slashIdx == schemaIdx + 7) {
									/** see{@link PlainWeb#loadJs} */
									key = "MdbR" + url.substring(schemaIdx + 7);
								} else {
									key=url.substring(slashIdx+1);
								}
								CMN.debug("[fetching internal res : ]", key);
								String mime="*/*";
								if(key.endsWith(".css")) mime = "text/css";
								if(key.endsWith(".js")) mime = "text/js";
								return new WebResourceResponse(mime, "UTF-8", loadCommonAsset(key));
							} catch (Exception e) {
								CMN.debug(e);
							}
						}
						if (!merge && (url.regionMatches(schemaIdx+12, "content", 0, 7)
								|| url.regionMatches(schemaIdx+12, "base", 0, 4)
								|| url.regionMatches(schemaIdx+12, "merge", 0, 5)
						)) {
							merge = true;
							/*mWebView.presenter = */invoker = mWebView.weblistHandler.getMergedBook();
						}
						key = url.substring(slashIdx);
					}
					else {
						//CMN.debug("wlh.moders::", wlh.moders);
						for (PlainWeb book : wlh.moders) { // java.util.ConcurrentModificationException
							WebResourceResponse resp = book.modifyRes(MainActivityUIBase.this, url, false);
							if (resp != null) {
								CMN.debug("修改了::http::", url);
								return resp;
							}
						}
					}
					if(merge) {
						if(mdbr) {
							CMN.debug("mdbr::", url);
							try {
								HTTPSession req = new MdictServerMobile.HTTPSessionProxy(url.substring(schemaIdx+7+4), request);
								Response ret = getMdictServer().handle(req);
								if(ret!=null) {
									//CMN.debug("WebResourceResponse::", ret.getMimeType());
									String mime = ret.getMimeType();
									int idx=mime.indexOf(";");
									if(idx>0) mime = mime.substring(0, idx);
									return new WebResourceResponse(mime, "UTF-8", ret.getData());
								}
							} catch (Exception e) {
								CMN.debug(url, e);
							}
						}
						else {
							try { //todo opt
								if (mWebView.merge) {
									for (BookPresenter wb:mWebView.webx_frames) {
										PlainWeb webx = wb.getWebx();
										if (webx.shouldUseClientResponse(url)) {
											return (WebResourceResponse) webx.getClientResponse(null, url, origin, null, request==null?null:request.getRequestHeaders(), false);
										}
										WebResourceResponse resp = webx.modifyRes(MainActivityUIBase.this, url, true);
										if(resp!=null) { //todo opt
											// requesting web data on merged page. or...
											CMN.debug("修改了::on merged page::", url);
											WebResourceResponse webResourceResponse;
											return resp;
										}
									}
									//if(server!=null && server.webResHandler!=null && server.webResHandler.hasHosts()) {
									//	//CMN.Log("shouldUseClientResponse::", url, webResHandler.shouldUseClientResponse(url), webResHandler.jinkeSheaths);
									//	if(server.webResHandler.shouldUseClientResponse(url)) {
									//		return (WebResourceResponse) server.webResHandler.getClientResponse(null, url, origin, null, request==null?null:request.getRequestHeaders(), false);
									//	}
									//}
								}
							} catch (Exception e) {
								CMN.debug(url,"\n",e);
							}
						}
						//return null;
					}
					//CMN.Log("chromium shouldInterceptRequest invoker",invoker);
					if(invoker.getIsWebx()) {
						PlainWeb webx = (PlainWeb) invoker.bookImpl;
						if (webx.hasExcludedResV4 && Build.VERSION.SDK_INT<21) {
							if (webx.shouldExcludedResV4(url)) {
								//CMN.Log("排除::"+url);
								return emptyResponse;
							}
						}
						if(accept==null || accept.contains("text/html")) {
							if(view.getTag(R.id.save)==null && (url.startsWith("http")||url.startsWith("file"))){
								boolean proceed = true;
								String[] shWebsite = webx.cleanExtensions;
								if(shWebsite!=null){
									for (int i = 0; i < shWebsite.length; i++) {
										if(url_contains(url, shWebsite[i])){
											proceed=false;
											break;
										}
									}
								}
			
								if(proceed && mWebView.bShouldOverridePageResource) {
									//CMN.debug("accept", accept, url);
									InputStream overridePage = invoker.getWebPage(url);
									if(overridePage!=null){
										//CMN.tp(0, "webx getPage :: ", invoker.getWebPageString(url), url);
										//BU.recordString(invoker.getWebPageString(url), "/sdcard/test.html");
										return new WebResourceResponse("text/html","UTF-8",overridePage);
									}
								}
								if(webx.canSaveResource) {
									try {
										shWebsite = webx.cacheExtensions;
										for (int i = 0; i < shWebsite.length; i++) {
											//CMN.Log(url, webx.cacheExtensions[i], url.contains(webx.cacheExtensions[i]));
											if(url.contains(shWebsite[i])){
												File pathDownload = invoker.getInternalResourcePath(true);
												if(!pathDownload.exists()) pathDownload.mkdirs();
												if(pathDownload.isDirectory()) {
													boolean needTrim=!((webx.andEagerForParms&&!url.contains(".js"))||url.contains(".php"));//动态资源需要保留参数
													File path;
													int start = url.indexOf("://");
													if(start<0) start=0; else start+=3;
													start = Math.max(url.indexOf("/", start)+1, start);
													int end = needTrim?url.indexOf("?"):-1;
													if(end<0) end=url.length();
													String name=url.substring(start, end);
													try {
														name=URLDecoder.decode(name, "utf8");
													} catch (Exception e) { }
													if(!needTrim) name=name.replaceAll("[=?&|:*<>]", "_");
													if(name.length()==0){
														name = "plod-index";
													}
													path=new File(pathDownload, name);
													CMN.debug("pathDownload", path);
													pathDownload = path.getParentFile();
													boolean saveit = !webx.butReadonly;
													if(saveit && !pathDownload.exists()) pathDownload.mkdirs();
													if(pathDownload.isDirectory())
													{
														name=path.getName();
														if(name.length()>64){
															name=name.substring(0, 56)+"_"+name.length()+"_"+name.hashCode();
															path=new File(pathDownload, name);
														}
														/* 下载 */
														if(saveit && !path.exists()) {
															CMN.debug("shouldInterceptRequest 下载中...！", url);
															CMN.debug("shouldInterceptRequest 下载目标: ", name);
															try {
																ViewUtils.downloadToStream(url, new OutputStream[]{null}, path.getPath()
																		, accept, refer, origin, request, webx);
																CMN.debug("shouldInterceptRequest 已下载！", url);
															} catch (Exception e) {
																CMN.debug(e);
																path.delete();
																return emptyResponse;
															}
															// 预处理
														}
														/* 再构 */
														if(path.isFile()){
															String mime=accept;
															if(mime!=null){
																int idx = mime.indexOf(",");
																if(idx>0) mime=mime.substring(0, idx);
															}else{
																mime="*/*";
															}
															if(mime.startsWith("image") && webx.svgKeywords!=null){
																for (int j = 0; j < webx.svgKeywords.length; j++) {
																	if(url.contains(webx.svgKeywords[i])){
																		mime="image/svg+xml";
																		break;
																	}
																}
															}
			
															WebResourceResponse ret = new WebResourceResponse(mime, "UTF-8", new FileInputStream(path));//BU.fileToBytes(path)
															if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
																ret.setResponseHeaders(CrossFireHeaders);
															}
															return ret;
														}
													}
												}
			
												break;
											}
										}
									}
									catch (Exception e) {
										CMN.debug(e);
									}
								}
							}
							else{
								view.setTag(R.id.save, null);
							}
						}
						if (url.endsWith(".js")) {
							if (webx.getShouldReplaceLetToVar(url)) {
								try {
									return ViewUtils.KikLetToVar(url , accept, refer, origin, request, webx);
								} catch (Exception e) { CMN.debug("kiklet 转化失败::", e); }
							}
						}
						if(invoker.getUseHosts() && webx.shouldUseClientResponse(url)) {
							// hosts
							return (WebResourceResponse) webx.getClientResponse(MainActivityUIBase.this, url, origin, null, request==null?null:request.getRequestHeaders(), false);
						}
						if(url.startsWith("http://mdbr.com")) {
							try {
								HTTPSession req = new MdictServerMobile.HTTPSessionProxy(url.substring(schemaIdx+7+4), request);
								Response ret = getMdictServer().handle(req);
								if(ret!=null) {
									//CMN.debug("WebResourceResponse::", ret.getMimeType());
									String mime = ret.getMimeType();
									int idx=mime.indexOf(";");
									if(idx>0) mime = mime.substring(0, idx);
									return new WebResourceResponse(mime, "UTF-8", ret.getData());
								}
							} catch (Exception e) {
								CMN.debug(url, e);
								return emptyResponse;
							}
						}
					}
					if(!mdbr) {
						return invoker.getOfflineMode()?emptyResponse:null;
					}
					//CMN.Log("漏网之鱼::", url);
				}
				else {
					// custom schemas
					if(url.startsWith(soundTag)) {
						opt.supressAudioResourcePlaying=false;
						key = url.substring(soundTag.length());
					}
					else if(url.startsWith(soundsTag)) {
						url = url.substring(soundsTag.length());
						try {
							url = URLDecoder.decode(url,"UTF-8");
						} catch (Exception ignored) { }
						String soundKey="\\"+url+".";
						CMN.debug("接收到发音任务！", soundKey, "::", invoker.getDictionaryName());
						InputStream restmp=null;
						WebResourceResponse ret=null;
						BookPresenter mdTmp;
						for (int i = 0; i < loadManager.md_size; i++) {
							mdTmp = findPronouncer(i, invoker);
							if(mdTmp!=EmptyBook){
								Boolean spx=false;
								try {
									Object[] result=mdTmp.getSoundResourceByName(soundKey);
									if(result!=null) {
										spx = (Boolean) result[0];
										restmp = (InputStream) result[1];
									}
								} catch (IOException ignored) { }
								if(restmp!=null && spx!=null) {
									if (spx) {
										try {
											ret = decodeSpxStream(restmp);
											if (ret != null) break;
										} catch (Exception e) {
											CMN.debug(e);
										}
									}
									ret = new WebResourceResponse("audio/mpeg", "UTF-8", restmp);
									break;
								}
							}
						}
						if(ret!=null){
							CMN.debug("返回音频");
							return ret;
						} else {
							ReadEntryPlanB(mWebView, url);
						}
						//return emptyResponse;
						return null;
					}
					else if(url.startsWith("font:")){
						if (fontlibs!=null) {
							url=url.substring(7);
							try {
								return new WebResourceResponse("font/*", "UTF-8", new FileInputStream(new File(fontlibs, url)));
							} catch (Exception ignored) {  }
						}
						return null;
					}
				}
				if (key==null) {
					key = url.substring(schemaIdx+3);
				}
			}
			//CMN.Log("chrochro_inter_0",url);
			final String SepWindows = "\\";
			try {
				key = URLDecoder.decode(key,"UTF-8");
			} catch (Exception ignored) { }
			key=key.replace("/", SepWindows);
			CMN.debug("chrochro_inter_key is",key, fakedDomainResponse);
			if(!key.startsWith(SepWindows)) key=SepWindows+key;
			if(key.endsWith(SepWindows)) key=key.substring(0, key.length()-1);
			
			int suffixIdx = key.lastIndexOf(".");
			String suffix = null;
			String mime = null;
			if(suffixIdx>=0){
				suffix = key.substring(suffixIdx).toLowerCase();
				suffixIdx = key.indexOf("?");
				if(suffixIdx!=-1)suffix = key.substring(0, suffixIdx);
			}
			if(suffix!=null)
			switch (suffix) {
				case ".ini":
				case ".js":
					mime = "text/x-javascript";
				break;
				case ".png":
					mime = "image/png";
				break;
				case ".css":
					mime = "text/css";
				break;
			}
			String parms=null;
			int parmIdx = key.indexOf("?");
			//key= mdict.requestPattern.matcher(key).replaceAll("");
			if(parmIdx>0) {
				parms = key.substring(parmIdx+1);
				key = key.substring(0, parmIdx);
			}
			boolean shouldLoadFiles = PDICMainAppOptions.getAllowPlugRes()||invoker.isHasExtStyle();
			//检查后缀，js，ini,png,css,直接路径。
			if(shouldLoadFiles && (!PDICMainAppOptions.getAllowPlugResNone()||!invoker.bookImpl.hasMdd()||parms!=null&&parms.contains("f=a"))) {
				WebResourceResponse ret = getPlugRes(invoker, key);
				if(ret!=null) return ret;
				shouldLoadFiles = false;
			}

			if(!invoker.bookImpl.hasMdd())
				return fakedDomainResponse;
			if(mWebView.fromCombined==0 && !mWebView.fromNet() && invoker.getIsolateImages() && RegImg.matcher(key).find()){
				//CMN.Log("Isolating Images...");
				new_photo = key;
				PhotoPager.removeCallbacks(PhotoRunnable);
				PhotoPager.post(PhotoRunnable);
				return fakedDomainResponse;
			}
			try {
				InputStream restmp=invoker.bookImpl.getResourceByKey(key);
				CMN.debug("getResourceByKey::",key, restmp);
				if(restmp==null) {
					if(shouldLoadFiles) {
						WebResourceResponse ret = getPlugRes(invoker, key);
						if(ret!=null) return ret;
					}
					if(true) return fakedDomainResponse; //!!!
					//CMN.Log("chrochro inter_ key is not find: ",key);
					if(url.startsWith("http://")) {
						URL uurrll = new URL(url);
						HttpURLConnection conn = (HttpURLConnection) uurrll.openConnection();
						conn.setConnectTimeout(3 * 1000);
						conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

						conn.setRequestMethod("GET");
						InputStream inStream = conn.getInputStream();
						//conn.setRequestProperty("lfwywxqyh_token",toekn);
						byte[] buffer = new byte[1024];
						int len;
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						while((len = inStream.read(buffer)) != -1) {
							bos.write(buffer, 0, len);
						}
						bos.close();
						restmp = new ByteArrayInputStream(bos.toByteArray());
						//return new WebResourceResponse("","UTF-8",inStream);
					}
					else return null;
				}

				if(mime==null && suffix!=null)
				switch (suffix){
					case ".mp4":
						mime = "video/mp4";
					break;
					case ".mp3":
						mime = "audio/mpeg";
					break;
					case ".spx":{
						mime = "audio/*";
						WebResourceResponse ret = decodeSpxStream(restmp);
						CMN.debug(url, "spx : ", ret);
						if(ret!=null) return ret;
					} break;
					case ".tif":
					case ".tiff":{
						mime = "image/*";
						try {
							BufferedImage image = Imaging.getBufferedImage(restmp, getTifConfig());
							//CMN.pt("解码耗时 : "); CMN.rt();
							ByteArrayRandomOutputStream bos = new ByteArrayRandomOutputStream((int) (restmp.available()*2.5));
							image.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
							//CMN.pt("再编码耗时 : ");
							return new WebResourceResponse("image/jpeg","UTF-8",new ByteArrayInputStream(bos.toByteArray()));
						} catch (Exception e) { e.printStackTrace(); }
					} break;
					case ".jpg":
					case ".gif":
					case ".jpeg":
						mime = "audio/mpeg";
					break;
				}
				if(mime==null)
					mime="";
				return new WebResourceResponse(mime,"UTF-8",restmp);
			}
			catch (IOException e) {
				CMN.debug(e);
				return super.shouldInterceptRequest(view, url);
			}
		}
		
		private WebResourceResponse getPlugRes(BookPresenter presenter, String uri) {
			try {
				SU.Log("getPlugRes!", presenter.isHasExtStyle() , uri, presenter.getDictionaryName());
				if(uri.length()<32 && uri.length()>3 && uri.lastIndexOf("\\")==0) {
					int sid = uri.lastIndexOf(".");
					if(sid>0 && sid<uri.length()-2) {
						//SU.Log("同名CSS!", presenter.isHasExtStyle() , uri, presenter.getDictionaryName());
						if(presenter.isHasExtStyle()
								&& uri.endsWith(".css")
								&& uri.regionMatches(1, presenter.getDictionaryName(), 0, sid-1))
						{
							return new WebResourceResponse("text/css", "UTF-8"
									, presenter.getDebuggingResource("/"+uri.substring(1)));
						}
						if(PDICMainAppOptions.getAllowPlugRes()) {
							if(PDICMainAppOptions.getAllowPlugResSame()) {
								String p = presenter.getPath();
								int sep = p.lastIndexOf(File.separator, p.lastIndexOf(File.separator)-1)+1;
								if(sep>0) {
									String d = presenter.getDictionaryName();
									if(p.regionMatches(true, sep, d, 0, Math.min(d.length(), 3))) {
										//SU.Log("同名目录!");
										p=null;
									}
								}
								if(p!=null) {
									return emptyResponse;
								}
							}
							SU.Log("文件", uri);
							int mid="jscssjpgpngwebpicosvgini".indexOf(uri.substring(sid+1));
							if(mid>=0) {
								InputStream input = presenter.getDebuggingResource("/"+uri.substring(1));
								if(input!=null) {
									String MIME = mid==0?"application/x-javascript"
											:mid==2?"text/css"
											:mid>=5&&mid<18?"img/*"
											:mid==18?"img/svg" //todo
											:"*/*"
											;
									return new WebResourceResponse(MIME, "UTF-8", input);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
			return emptyResponse;
		}
		
		@Override
		public void onReceivedSslError(WebView view, final SslErrorHandler mHandler, SslError error) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUIBase.this);
			builder.setTitle("SSL Certificate Error");
			builder.setMessage("code"+error.getPrimaryError()+"\ndo u want to continue anyway?");
			builder.setPositiveButton(R.string.continue_, (dialog, which) -> mHandler.proceed());
			builder.setNegativeButton(R.string.cancel, (dialog, which) -> mHandler.cancel());
			builder.setOnKeyListener((dialog, keyCode, event) -> {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					mHandler.cancel();
					dialog.dismiss();
					return true;
				}
				return false;
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	};
	
	private int fastFrameIndexOf(ViewGroup webholder, WebViewmy mWebView, int frameAt) {
		View ca = webholder.getChildAt(frameAt);
		if(ca!=null && ca.getTag()==mWebView) {
			return frameAt;
		}
		for (int i = 0, len = webholder.getChildCount(); i < len; i++) {
			ca = webholder.getChildAt(i);
			if(ca!=null && ca.getTag()==mWebView) {
				return i;
			}
		}
		return frameAt;
	}
	
	private void ReadEntryPlanB(WebViewmy mWebView, String url) {
		opt.supressAudioResourcePlaying=!AutoBrowsePaused;
		CMN.debug("ReadEntryPlanB");
		if(AutoBrowsePaused /*自动读时绕过*/ && PDICMainAppOptions.getUseSoundsPlaybackFirst() && hasMddForWeb(mWebView)){
			root.post(() -> mWebView.evaluateJavascript(WebviewSoundJS, value -> {
				if (!"10".equals(value)) {
					ReadEntryPlanB_internal(url);
				}
			}));
		} else {
			ReadEntryPlanB_internal(url);
		}
	}

	private boolean hasMddForWeb(WebViewmy mWebView) {
		BookPresenter mdTmp = mWebView.presenter;
		return mdTmp!=null && mdTmp.bookImpl.hasMdd();
	}

	/**  进化的路线充满了崎岖与坎坷，但我仍要致君尧舜上，登辉煌、临绝巅。 */
	private void ReadEntryPlanB_internal(String url) {
		String msg = "找不到音频";
		if(!AutoBrowsePaused/*自动读时强制*/ || opt.getUseTTSToReadEntry()){
			ReadText(url, null);
			msg = opt.getHintTTSReading()?("正在使用 TTS"):null;
		}
		if(msg!=null){
			hdl.sendMessage(Message.obtain(hdl, 2020, msg));
		}
	}

	private BookPresenter findPronouncer(int i, BookPresenter invoker) {
		BookPresenter mdTmp = loadManager.md_getAt(i);
		if(mdTmp!=null) {;
			if(mdTmp==invoker)
				return mdTmp;
			return PDICMainAppOptions.getTmpIsAudior(mdTmp.tmpIsFlag)?mdTmp:EmptyBook;
		}
		else {
			PlaceHolder phTmp = loadManager.getPlaceHolderAt(i);
			if (phTmp!=null && PDICMainAppOptions.getTmpIsAudior(phTmp.tmpIsFlag)) {
				return loadManager.md_get(i);
			}
		}
		return EmptyBook;
	}

	private void playCachedSoundFile(WebViewmy mWebView, String soundUrl, BookPresenter invoker, boolean findInAudioLibs) throws IOException {
		String soundKey = soundUrl;

		if(findInAudioLibs){
			soundKey = "\\"+soundKey+".";
		} else {
			if(!soundUrl.startsWith("/"))
				soundUrl = "/"+soundUrl;
		}

		File tmpPath = new File(getExternalCacheDir(), "audio"+(findInAudioLibs?("/"+soundUrl+".mp3"):soundUrl));
		if(!tmpPath.exists()) {
			if(findInAudioLibs){
				BookPresenter mdTmp;
				for (int i = 0; i < loadManager.md_size; i++) {
					mdTmp = findPronouncer(i, invoker);
					if(mdTmp!=EmptyBook){
						Boolean spx=false;
						InputStream restmp=null;
						try {
							Object[] result=mdTmp.getSoundResourceByName(soundKey);
							if(result!=null) {
								spx = (Boolean) result[0];
								restmp = (InputStream) result[1];
							}
						} catch (IOException ignored) { }
						if(restmp!=null && spx!=null) {
							if (spx) {
								decodeSpxFile(restmp, tmpPath);
							} else {
								BU.printFileStream(restmp, tmpPath);
							}
							break;
						}
					}
				}
				if(tmpPath.exists()){
					playCachedSoundFile_internal(tmpPath);
				} else {
					try {
						soundUrl = URLDecoder.decode(soundUrl, "UTF-8");
					} catch (Exception ignored) { }
					ReadEntryPlanB(mWebView, soundUrl);
				}
			}
			else {
				Object[] res = invoker.getSoundResourceByName(soundUrl.replace("/", "\\"));
				if(res!=null){
					InputStream ins = (InputStream) res[1];
					BU.printFileStream(ins, tmpPath);
					playCachedSoundFile_internal(tmpPath);
				}
			}
		} else {
			playCachedSoundFile_internal(tmpPath);
		}
	}

	private void playCachedSoundFile_internal(File tmpPath) throws IOException {
		//CMN.Log("读取音频缓存……", tmpPath.getAbsolutePath());
		if(tmpPath.exists()) {
			MediaPlayer mAudioPlayer = audioPlayer;
			if (mAudioPlayer != null) {
				mAudioPlayer.stop();
				mAudioPlayer.release();
			}
			mAudioPlayer = new MediaPlayer();
			mAudioPlayer.setDataSource(tmpPath.getPath());
			mAudioPlayer.prepare();
			mAudioPlayer.start();
			mAudioPlayer.setOnCompletionListener(getSimpleMediaCompleteListener());
			audioPlayer = mAudioPlayer;
		}
	}

	private MediaPlayer audioPlayer;
	MediaPlayer.OnCompletionListener mSimpleMediaCompleteListener;
	private MediaPlayer.OnCompletionListener getSimpleMediaCompleteListener() {
		if(mSimpleMediaCompleteListener==null)
		mSimpleMediaCompleteListener = mp -> {
			audioPlayer = null;
			mp.stop();
			mp.release();
			mp.setOnCompletionListener(null);
		};
		return mSimpleMediaCompleteListener;
	}

	/** 阻止误判 */
	private boolean url_contains(String url, String pattern) {
		int idx=0;
		int PL = pattern.length();
		if(pattern.length()>0) {
			int UL = url.length();
			while ((idx = url.indexOf(pattern, idx)) > 0) {
				if (idx >= UL - PL || url.charAt(idx + PL) == '?') return true;
				idx += pattern.length();
			}
		}
		return false;
	}

	private WebResourceResponse decodeSpxStream(InputStream restmp) throws IOException {
		ByteArrayRandomOutputStream bos = new ByteArrayRandomOutputStream(restmp.available()*2);
		JSpeexDec decoder = new JSpeexDec();
		try {
			decoder.decode(new DataInputStream(restmp) , bos, JSpeexDec.FILE_FORMAT_WAVE);
			return new WebResourceResponse("audio/mpeg","UTF-8",new ByteArrayInputStream(bos.bytes(),0,bos.size()));
		} catch (Exception e) { CMN.debug(e); }
		return null;
	}

	private WebResourceResponse decodeSpxFile(InputStream restmp, File f) throws IOException {
		//CMN.Log("decodeSpxFile……");
		JSpeexDec decoder = new JSpeexDec();
		try {
			decoder.decode(new DataInputStream(restmp) , f, JSpeexDec.FILE_FORMAT_WAVE);
		} catch (Exception e) { CMN.debug(e); }
		return null;
	}

	void jumpNaughtyFirstHighlight(WebViewmy mWebView) {
		CMN.debug("jumpNaughtyFirstHighlight...");
		long mJumpNaughtyTimeToken = jumpNaughtyTimeToken = System.currentTimeMillis();
		NaughtyJumpper=()-> mWebView.evaluateJavascript("window.bOnceHighlighted", value -> {
			if(mJumpNaughtyTimeToken!=jumpNaughtyTimeToken)
				return;
			if(!"true".equals(value)){
				do_jumpNaughtyFirstHighlight(mWebView);
			} else {
				weblistHandler.jumpHighlight(1, false);
			}
		});
		do_jumpNaughtyFirstHighlight(mWebView);
	}

	private void do_jumpNaughtyFirstHighlight(WebViewmy mWebView) {
		mWebView.removeCallbacks(NaughtyJumpper);
		mWebView.post(NaughtyJumpper);
	}

	public void locateNaviIcon(){
		View widget13=contentUIData.browserWidget13,widget14=contentUIData.browserWidget14;
		if(opt.getNavigationBtnType()==0 || (opt.getNavigationBtnType()==2 && !opt.getBottombarOnBottom())) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) widget13.getLayoutParams();
			//if((lp.gravity&Gravity.TOP) != 0 ) return;
			lp.gravity=Gravity.TOP|Gravity.END;
			FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams) widget14.getLayoutParams();
			lp1.topMargin=lp.bottomMargin;
			lp.topMargin=lp1.bottomMargin;
			lp1.gravity=Gravity.TOP|Gravity.END;
			widget13.setLayoutParams(lp);
			widget14.setLayoutParams(lp1);
		} else {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) widget13.getLayoutParams();
			//if((lp.gravity&Gravity.BOTTOM) != 0 ) return;
			lp.gravity=Gravity.BOTTOM|Gravity.END;
			widget13.setLayoutParams(lp);
			lp = (FrameLayout.LayoutParams) widget14.getLayoutParams();
			lp.gravity=Gravity.BOTTOM|Gravity.END;
			widget14.setLayoutParams(lp);
		}
	}

	/**
	 var ssc=document.getElementById('_PDict_Darken');
	 if(ssc){
	 	ssc.parentNode.removeChild(ssc);
	 }
	 */
	@Metaline
	final static String DeDarkModeIncantation = "DARK";

	/**
	 <style id="_PDict_Darken" class="_PDict">html {
		-webkit-filter: invert(1);
		filter: invert(1);
		-moz-filter:invert(1);
		-o-filter:invert(1);
		-ms-filter:invert(1);
	 }
	 body {
	 	background:#00000000!important;
	 	background-color:#00000000!important;
	 }
	 </style>
	 */
	@Metaline
	public final static String DarkModeIncantation_l = "DARK";

	/**
	 var css = 'html {-webkit-filter: invert(100%);\
					 -moz-filter: invert(100%);\
					 -o-filter: invert(100%);\
					 -ms-filter: invert(100%);background:#ffffff}\
				 ', d=document,
	 head = d.getElementsByTagName('head')[0],
	 sty = d.createElement('style');
	 sty.id = "_PDict_Darken";
	 if(!d.getElementById(sty.id))
	 {
		 sty.class = "_PDict";
		 sty.type = 'text/css';
	 	 sty.innerText = css;
//		 if (sty.styleSheet){
//		 	style.styleSheet.cssText = css;
//		 } else {
//		 	sty.appendChild(d.createTextNode(css));
//		 }
		 head.appendChild(sty);
	 }
	 if(d.body){d.body.style.background='#00000000';d._pdkn=1}
	 */
	@Metaline
	public final static String DarkModeIncantation ="DARK";
	
	/**
	 (function(key){
	 	if(!document || !document.body) return 1;
		var audioTag = document.getElementById("myAudio");
		if(audioTag){
	 		audioTag.pause();
		}
		else {
			audioTag = document.createElement("AUDIO");
			audioTag.class='_PDict';
	 		audioTag.addEventListener('pause', function () {
	 			app.onAudioPause();
	 		}, false);
	 		audioTag.addEventListener('play', function () {
	 			app.onAudioPlay();
	 		}, false);
			audioTag.id="myAudio";
			document.body.appendChild(audioTag);
		}
		audioTag.setAttribute("src", key);
		audioTag.play();
	 	return 0;
	 })
	 */
	@Metaline
	final static String playsoundscript="AUDIO";


	static String delimiter = "[,.?!;，。？！；\r\n]";
	int utteranceCacheSize = 1;
	private int highLightBG = Color.YELLOW;//Color.YELLOW;

	TextToSpeech TTSController_engine;
	volatile boolean TTSReady;
	String[] speakPool;
	int[] speakScaler;
	Object speakText;
	volatile int speakPoolIndex;
	volatile int speakPoolEndIndex;
	volatile int speakCacheEndIndex;
	
	//Runnable forceNextTextRunnable = this::onAudioPause;
	Runnable forceNextTextRunnable = new Runnable() {
		@Override
		public void run() {
			CMN.debug("forceNextTextRunnable");
			onAudioPause();
		}
	};

	/** 提交语句给TTS引擎 */
	Runnable mPullReadTextRunnable = new Runnable() {
		@Override
		public void run() {
			if(TTSController_engine==null)
				return;
			int start = Math.max(0 ,Math.min(speakPool.length, speakPoolEndIndex+1));
			int end = Math.min(start+utteranceCacheSize, speakPool.length);
			speakCacheEndIndex = end;
			if(start>=speakPool.length){
				pauseTTSCtrl(true);
			}
			else {
				pauseTTSCtrl(false);
				for (int i = start; i < end; i++) {
					HashMap<String, String> parms = new HashMap<>();
					parms.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(i));
					parms.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, Float.toString(TTSVolume));
					TTSController_engine.speak(speakPool[i].trim(),TextToSpeech.QUEUE_ADD, parms);
					CMN.debug("缓存了句子", i, speakPool[i]);
					if(!AutoBrowsePaused){
						root.postDelayed(forceNextTextRunnable, 800);
					}
				}
				if(TTSController_!=null && TTSController_.getParent()!=null && speakText instanceof SpannableString){
					TTSController_tvHandle.setText(speakPool[start]);
					if(speakScaler ==null){
						speakScaler = new int[speakPool.length];
						for (int j = 0; j < speakPool.length; j++) {
							speakScaler[j]=(j>0?speakScaler[j-1]+1:0)+speakPool[j].length();
						}
					}
					if(timeHLSpan==null){
						timeHLSpan = new ColoredHighLightSpan(highLightBG, 9f, 1);
					}
					try {
						SpannableString baseSpan = (SpannableString) speakText;
						end = 1 + speakScaler[start];
						start = start>0? 1 + speakScaler[start-1]:0;
						baseSpan.setSpan(timeHLSpan, Math.min(start, baseSpan.length()-1), Math.min(end, baseSpan.length()), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	};

	void pauseTTSCtrl(boolean pause) {
		if(TTSController_playBtn!=null) {
			if (pause) {
				TTSController_playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				TTSController_playBtn.setTag(null);
			} else if(TTSController_playBtn.getTag()==null){
				TTSController_playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
				TTSController_playBtn.setTag(false);
			}
		}
	}

	Runnable mUpdateTextRunnable = new Runnable() {
		@Override
		public void run() {
			if(TTSController_!=null && speakText instanceof String) {
				TTSController_tv.setText(new SpannableStringBuilder((String) speakText).append("\n\n\n\n"), TextView.BufferType.SPANNABLE);
				speakText = TTSController_tv.getText();
				TTSController_tv.clearSelection();
			}
		}
	};

	/** 重新开始TTS读网页句子。 */
	public void ReadText(String text, WebViewmy mWebView){
		if(!AutoBrowsePaused && PDICMainAppOptions.getAutoBrowsingReadSomething())
			interruptAutoReadProcess(true);
		if(text!=null) {
			speakText = text;
			speakPool = text.split(delimiter);
			speakPoolIndex = 0;
			speakPoolEndIndex = -1;
			speakScaler = null;
			if(TTSController_!=null && TTSController_.getParent()!=null){
				if(Thread.currentThread()!= Looper.getMainLooper().getThread())
					root.post(mUpdateTextRunnable);
				else
					mUpdateTextRunnable.run();
			}
			mCurrentReadContext = mWebView;
		}
		if(TTSController_engine != null) {
			TTSController_engine.stop();
		}
		pauseTTS();
		boolean mTTSReady = TTSReady;
		
		if(true || TTSController_engine==null) {
			mTTSReady = TTSReady = false;
			TTSController_engine = new TextToSpeech(this, status -> {
				TTSReady = true;
				mPullReadTextRunnable.run();
			}, null); //"com.google.android.tts"
			TTSController_engine.setSpeechRate(TTS_LEVLES_SPEED[TTSSpeed]);
			TTSController_engine.setPitch(TTS_LEVLES_SPEED[TTSPitch]);

			TTSController_engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				@Override
				public void onStart(String utteranceId) {
					speakPoolIndex = IU.parsint(utteranceId);
					root.removeCallbacks(forceNextTextRunnable);
					CMN.debug("tts onStart" ,speakPoolIndex);
					if(opt.getTTSHighlightWebView()) {
						WebViewmy CRC = mCurrentReadContext;
						if(CRC!=null)
							CRC.post(() -> CRC.findAllAsync(speakPool[speakPoolIndex]));
					}
					onAudioPlay();
				}

				@Override
				public void onDone(String utteranceId) {
					speakPoolEndIndex = IU.parsint(utteranceId);
					int speakPoolIndex = speakPoolEndIndex+1;
					CMN.debug("tts onDone" ,speakPoolIndex, speakCacheEndIndex);
					if(speakPoolIndex>=speakCacheEndIndex){
						mPullReadTextRunnable.run();
					}
					if(speakPoolIndex>=speakPool.length){
						onAudioPause();
						CMN.debug("tts onPause");
					}
					//onAudioPause();
				}

				@Override
				public void onError(String utteranceId) {
					//CMN.Log("tts onError" ,utteranceId);
				}

				@Override
				public void onError(String utteranceId, int code) {
					CMN.debug("tts onError" ,utteranceId, code);
				}
			});
		}

		if(text!=null) {
			speakPoolIndex = 0;
			speakPoolEndIndex = -1;
		}

		if(mTTSReady)
			mPullReadTextRunnable.run();
	}

	/** 暂停TTS */
	public void pauseTTS(){
		if(TTSController_engine !=null){
			TTSController_engine.stop();
			speakPoolEndIndex=speakPoolIndex-1;
		}
	}

	/**
	  engines  | voices  | Languages
	  speed | pitch
	 */
	/** TTS controller */
	public void showTTS() {
		ViewGroup targetRoot = root;
		if(PeruseViewAttached())
			targetRoot = peruseView.root;
		boolean isNewHolder=false;
		boolean isInit=false;
		// 初始化核心组件
		if(TTSController_tv == null){
			isInit=isNewHolder=true;
			TTSController_ = (ViewGroup) getLayoutInflater()
					.inflate(R.layout.float_tts_basic, root, false);
			TTSController_.setOnClickListener(ViewUtils.DummyOnClick);
			TTSController_toolbar = (ViewGroup) TTSController_.getChildAt(0);
			ImageView TTSController_expand = TTSController_toolbar.findViewById(R.id.tts_expand);
			TTSController_expand.setOnClickListener(this);
			if(opt.getTTSExpanded()) {
				TTSController_expand.setImageResource(R.drawable.ic_substrct_black_24dp);
			}
			ViewGroup middle = (ViewGroup) TTSController_.getChildAt(1);
			SelectableTextView tv = TTSController_.findViewById(R.id.text1);
			TTSController_controlBar = (ViewGroup) TTSController_.getChildAt(2);
			if(!opt.getTTSCtrlPinned())
				TTSController_controlBar.setVisibility(View.GONE);
			SeekBar.OnSeekBarChangeListener controller_controller=new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					seekBar.setTag(false);
					switch (seekBar.getId()){
						case R.id.sb1:
							TTSVolume=progress*1.f/100;
							TTSController_tvHandle.setText("音量："+ progress);
						break;
						case R.id.sb2:
							float _TTS_Pitch=TTS_LEVLES_SPEED[TTSPitch=progress];
							TTSController_engine.setPitch(_TTS_Pitch);
							TTSController_tvHandle.setText("音调："+_TTS_Pitch);
						break;
						case R.id.sb3:
							float _TTS_Speed=TTS_LEVLES_SPEED[TTSSpeed=progress];
							TTSController_engine.setSpeechRate(_TTS_Speed);
							TTSController_tvHandle.setText("语速："+_TTS_Speed);
						break;
					}
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) { }
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					if(seekBar.getTag()==null){
						onProgressChanged(seekBar, seekBar.getProgress(), false);
					} else {
						pauseTTS();
						switch (seekBar.getId()) {
							case R.id.sb1:
								mPullReadTextRunnable.run();
							break;
							case R.id.sb2:
							case R.id.sb3:
								ReadText(null, mCurrentReadContext);
							break;
						}
					}
					seekBar.setTag(null);
				}
			};
			SeekBar TTS_sb1 = (SeekBar) TTSController_controlBar.getChildAt(0);
			TTS_sb1.setProgress((int) (TTSVolume *100));
			TTS_sb1.setOnSeekBarChangeListener(controller_controller);
			TTS_sb1 = (SeekBar) TTSController_controlBar.getChildAt(1);
			TTS_sb1.setProgress(TTSPitch);
			TTS_sb1.setMax(TTS_LEVLES_SPEED.length-1);
			TTS_sb1.setOnSeekBarChangeListener(controller_controller);
			TTS_sb1 = (SeekBar) TTSController_controlBar.getChildAt(2);
			TTS_sb1.setProgress((int) (TTSSpeed));
			TTS_sb1.setMax(TTS_LEVLES_SPEED.length-1);
			TTS_sb1.setOnSeekBarChangeListener(controller_controller);
			TTSController_bottombar = (ViewGroup) TTSController_.getChildAt(3);
			tv.setBackgroundColor(Color.TRANSPARENT);
			ScrollViewHolder svmy = middle.findViewById(R.id.sv);
			SelectableTextViewCover textCover = middle.findViewById(R.id.cover);
			SelectableTextViewBackGround textCover2 = middle.findViewById(R.id.cover2);
			tv.instantiate(textCover, textCover2, svmy, null);
			tv.setTextViewListener(selectableTextView -> {
				//TimetaskHolder tk = new TimetaskHolder(2);
				//timer.schedule(tk, 110);
			});
			TTSController_playBtn = TTSController_toolbar.findViewById(R.id.tts_play);
			TTSController_playBtn.setOnClickListener(this);
			TTSController_popIvBack = TTSController_toolbar.findViewById(R.id.tts_popIvBack);
			TTSController_popIvBack.setOnClickListener(MainActivityUIBase.this);


			//TTSController_bottombar.findViewById(R.id.popIvRecess).setOnClickListener(MainActivityUIBase.this);
			//TTSController_bottombar.findViewById(R.id.popIvForward).setOnClickListener(MainActivityUIBase.this);
			TTSController_bottombar.findViewById(R.id.tts_settings).setOnClickListener(MainActivityUIBase.this);

			TTSController_ck1 = TTSController_bottombar.findViewById(R.id.ttsPin);
			TTSController_ck1.setOnClickListener(MainActivityUIBase.this);
			TTSController_ck2 = TTSController_bottombar.findViewById(R.id.ttsHighlight);
			TTSController_ck2.setOnClickListener(MainActivityUIBase.this);
			TTSController_tvHandle = TTSController_toolbar.findViewById(R.id.popupText1);
			TTSController_indicator = TTSController_bottombar.findViewById(R.id.popupText2);
			TTSController_tvHandle.setOnClickListener(MainActivityUIBase.this);
			View popupNxtD, popupLstD;
			(popupNxtD= TTSController_toolbar.findViewById(R.id.tts_NxtUtterance)).setOnClickListener(MainActivityUIBase.this);
			(popupLstD= TTSController_toolbar.findViewById(R.id.tts_LstUtterance)).setOnClickListener(MainActivityUIBase.this);
			//TTSController_indicator.setOnClickListener(MainActivityUIBase.this);

			// 移动逻辑
			TTSController_tvHandle.setOnTouchListener(TTSController_moveToucher = new TTSMoveToucher(MainActivityUIBase.this, TTSController_tvHandle, TTSController_, opt));
			TTSController_indicator.setOnTouchListener(TTSController_moveToucher);
			popupNxtD.setOnTouchListener(TTSController_moveToucher);
			popupLstD.setOnTouchListener(TTSController_moveToucher);
			// 缩放逻辑
			TTSController_tv = tv;
		}

		TTSController_ck1.drawInnerForEmptyState=
		TTSController_ck2.drawInnerForEmptyState=GlobalOptions.isDark;
		if(!GlobalOptions.isDark){
			TTSController_ck1.circle_shrinkage=2;
			TTSController_ck2.circle_shrinkage=2;
		}
		TTSController_ck1.setChecked(opt.getTTSCtrlPinned());
		TTSController_ck2.setChecked(opt.getTTSHighlightWebView());

		TTSController_tv.setTheme(0xFFffffff, Color.BLACK, 0x883b53f1, 0x883b53f1);

		if(isNewHolder){
			fix_pw_color();
			FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) TTSController_.getLayoutParams());
			lp.height = TTSController_moveToucher.FVH_UNDOCKED=(int)(dm.heightPixels*5.0/12-getResources().getDimension(R.dimen._20_));
			if(!opt.getTTSExpanded()) lp.height = TTSController_moveToucher._45_;
			TTSController_.setTranslationY(targetRoot.getHeight()-getResources().getDimension(R.dimen._50_)-getResources().getDimension(R.dimen._50_));
			lp.height= TTSController_.getLayoutParams().height;
		}

		ViewGroup svp = (ViewGroup) TTSController_.getParent();
		if(svp!=targetRoot){
			if(svp!=null) svp.removeView(wordPopup.popupGuarder);
			if(TTSController_moveToucher.FVDOCKED && TTSController_moveToucher.Maximized){
				TTSController_moveToucher.Dedock();
			}

			targetRoot.addView(TTSController_);
			fix_full_screen(null);
		}
		mUpdateTextRunnable.run();
	}

	public void hideTTS() {
		((ViewGroup)TTSController_.getParent()).removeView(TTSController_);
		checkFlags();
	}

	public void toggleTTS() {
		if(TTSController_==null || TTSController_.getParent()==null)
			showTTS();
		else{
			hideTTS();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		//CMN.debug("onActivityResult::", requestCode, resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 0: {
				isBrowsingImgs = false;
			} break;
			case TapTranslator.requestCode:{
				wordPopup.set(resultCode==requestCode);
				
			} break;
			case Multiview.requestCode:{
				resetMerge(-1, false);
			} break;
			case NightMode.requestCode:{
				boolean dark = GlobalOptions.isDark;
				opt.getInDarkMode();
				if(Build.VERSION.SDK_INT>=29){
					GlobalOptions.isSystemDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
					if (opt.darkSystem()) {
						GlobalOptions.isDark = GlobalOptions.isSystemDark;
					}
				}
				if (dark!=GlobalOptions.isDark) {
					changeToDarkMode();
				}
			} break;
		}
	}

	public static Bitmap blurByGauss(Bitmap srcBitmap, int radius) {
		//Bitmap bitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
		Matrix matrix = new Matrix(); matrix.preScale(0.35f, 0.35f);
		Bitmap bitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, false);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int[] r = new int[wh];
		int[] g = new int[wh];
		int[] b = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int[] vmin = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int temp = 256 * divsum;
		int[] dv = new int[temp];
		for (i = 0; i < temp; i++) {
			dv[i] = (i / divsum);
		}
		yw = yi = 0;
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
				pix[yi] = 0xfaffffff & pix[yi];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return bitmap;
	}

	public void performReadEntry() {
		WebViewListHandler wlh = weblist;
		mThenReadEntryCount--;
		
		WebViewmy wv = weblist.scrollFocus;
		BookPresenter reader = wv==null?EmptyBook:wv.presenter;
		String target = wlh.displaying;
		// CMN.debug("performReadEntry PRE "+reader.getDictionaryName()+"-"+wv+"-"+target);
		if(reader!=EmptyBook && wv!=null && target!=null) {
			if (reader.isMddResource() && target.length() > 1) {
				int end = target.lastIndexOf(".");
				if (end < target.length() - 6) end = -1;
				target = target.substring(1, end > 0 ? end : target.length());
			}
			String finalTarget = target;
			if(PDICMainAppOptions.getUseSoundsPlaybackFirst()){
				requestSoundPlayBack(finalTarget, reader, wv);
			}
			else if (AutoBrowsePaused /*自动读时绕过*/ && reader.bookImpl.hasMdd()) {
				/* 倾向于已经制定发音按钮 */
				BookPresenter finalMCurrentDictionary = reader;
				wv.evaluateJavascript(WebviewSoundJS, value -> {
					//CMN.Log("WebviewSoundJS", value);
					if (!"10".equals(value)) {
						requestSoundPlayBack(finalTarget, finalMCurrentDictionary, wv);
					}
				});
			} else {
				requestSoundPlayBack(finalTarget, reader, wv);
			}
		}
	}

	public void postReadEntry() {
		CMN.debug("postReadEntry");
		if(opt.getUseTTSToReadEntry()){
			pauseTTS();
		}
		//bottombar2.findViewById(R.id.browser_widget12).performClick();
		//root.postDelayed(() -> performReadEntry(), 100);
		hdl.sendEmptyMessage(3322123);
	}

	public void postReadContent(WebViewmy mWebView) {
		//CMN.Log("postReadEntry");
		if(opt.getUseTTSToReadEntry()){
			pauseTTS();
		}
		root.postDelayed(() -> performReadContent(mWebView), 100);
	}

	private void performReadContent(WebViewmy mWebView) {
		CMN.debug("bThenReadContent");
		if(mWebView!=null){
			mWebView.evaluateJavascript("document.documentElement.innerText", value -> {
				value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
				ReadText(value, mWebView);
			});
		}
	}
	
	public WebViewmy getCurrentWebContext() {
		return null;
	}

	/**
	 //console.log("fatal "+"???");
	 var hrefs = document.getElementsByTagName('a');
	 for(var i=0;i<hrefs.length;i++){
		 if(hrefs[i].attributes['href']){
			 if(hrefs[i].attributes['href'].value.indexOf('sound')==0){
	 			// console.log("fatal "+"!!!");
				 hrefs[i].click();
				 10;
	 			 break;
			 }
		 }
	 }
	 */
	@Metaline
	private String WebviewSoundJS=StringUtils.EMPTY;

	private void requestSoundPlayBack(String finalTarget, BookPresenter invoker, WebViewmy wv) {
		String soundKey = null;
		try {
			soundKey = URLEncoder.encode(finalTarget, "UTF-8");
		} catch (Exception ignored) { }
		if(PDICMainAppOptions.getCacheSoundResInAdvance()){
			try {
				playCachedSoundFile(wv, soundKey, invoker, true);
			} catch (IOException e) {
				CMN.debug(e);
			}
		} else {
			opt.supressAudioResourcePlaying=false;
			soundKey=soundsTag+soundKey; /* CMN.Log("发音任务丢给资源拦截器", soundKey); */
			wv.evaluateJavascript(playsoundscript + ("(\"" + soundKey + "\")") , null);
		}
	}

	public boolean PeruseViewAttached() {
		if(peruseView !=null){
			return peruseView.isAttached();
		}
		return false;
	}

	public boolean PeruseSearchAttached() {
		if(peruseView !=null){
			return peruseView.isAttached() && peruseView.PerusePageSearchetSearch!=null;
		}
		return false;
	}

	PeruseView getPeruseView() {
		if(peruseView ==null) {
			peruseView = new PeruseView(MainBackground);
			peruseView.spsubs = opt.defaultReader.getFloat("spsubs", 0.706f);
			peruseView.dm = dm;
			peruseView.opt = opt;
			peruseView.density = dm.density;
		}
		return peruseView;
	}

	void AttachPeruseView(boolean newSch) {
		try {
			if(peruseView ==null) return;
			if(!peruseView.isAdded()) {
				//CMN.Log("AttachPeruseView 1 ", bRefresh);
				peruseView.bCallViewAOA=true;
				/* catch : Can not perform this action after onSaveInstanceState */
				peruseView.show(getSupportFragmentManager(), "PeruseView");
			} else if(peruseView.mDialog!=null){
				//CMN.Log("AttachPeruseView 2 ", bRefresh);
				boolean resetDlg = !peruseView.mDialog.isShowing();
				peruseView.mDialog.show();
				if (resetDlg) peruseView.resetDlg();
				peruseView.onViewAttached(this, newSch);
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		if (isFloating()) {
			if ((foreground & (1 << thisActType.ordinal())) == 0) {
				postTask = new Runnable(){
					@Override
					public void run() {
						AttachPeruseView(newSch);
						floatApp.expand(true);
						postTask = null;
					}
				};
				moveTaskToFront();
			} else {
				floatApp.expand(true);
			}
		}
	}

	/** 接管跳转翻阅。来自包括分享中枢、get12菜单。*/
	public void JumpToPeruseModeWithWord(String content) {
		//CMN.Log(" Jump To Peruse ...  ", content);
		JumpToPeruseMode(content, null, -1, content!=null);
	}
	
	public void JumpToPeruseMode(String text, ArrayList<Long> bookIds, long bookId, boolean refresh) {
		if(bookId==-1) {
			bookId = currentDictionary.getId();
		} else if(bookId==-2){
			bookId = bookIds!=null && bookIds.size()>0?bookIds.get(0):-1;
		}
		getPeruseView().prepareJump(this, text, bookIds, bookId);
		AttachPeruseView(refresh);
	}

	private SoundPool mSoundPool;
	private void createSoundPoolIfNeeded() {
		if (mSoundPool == null) {
			// 5.0 及 之后
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				AudioAttributes audioAttributes = null;
				audioAttributes = new AudioAttributes.Builder()
						.setUsage(AudioAttributes.USAGE_MEDIA)
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
						.build();

				mSoundPool = new SoundPool.Builder()
						.setMaxStreams(16)
						.setAudioAttributes(audioAttributes)
						.build();
			} else { // 5.0 以前
				mSoundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);  // 创建SoundPool
			}
			//mSoundPool.setOnLoadCompleteListener(this);  // 设置加载完成监听
		}
	}

	protected DArrayAdapter FavoriteNoteBooksAdapter() {
		if(AppFunAdapter ==null)
			AppFunAdapter = new DArrayAdapter(this, false);
		return AppFunAdapter;
	}

	protected void showCreateNewFavoriteDialog(int width) {
		ViewGroup dv = (ViewGroup) getLayoutInflater().inflate(R.layout.fp_edittext, root, false);
		EditText etNew = dv.findViewById(R.id.edt_input);
		View btn_Done = dv.findViewById(R.id.done);
		dv.findViewById(R.id.toolbar_action1).setVisibility(View.GONE);
		Dialog dd = new GoodKeyboardDialog(MainActivityUIBase.this);
		dd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dd.setContentView(dv);
		btn_Done.setOnClickListener(v121 -> {
			AppFunAdapter.createNewDatabase(etNew.getText().toString());
			dd.dismiss();
		});
		etNew.setOnEditorActionListener((v1212, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_DONE ||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
				btn_Done.performClick();
				return true;
			}
			return false;
		});
		Window win = dd.getWindow();
		win.setGravity(Gravity.TOP);
		win.getAttributes().width=width;
		win.setAttributes(win.getAttributes());
		win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		dd.show();
		if(true){
			dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			etNew.requestFocus();
		}
	}

	public void showMultipleCollection(String text, ViewGroup webviewholder) {
		//showT("功能关闭中，请等待5.0版本");
		//showT(text);
		BottomSheetDialog _bottomPlaylist = bottomPlaylist.get();
		if(_bottomPlaylist==null) {
			CMN.debug("重建底部弹出");
			bottomPlaylist = new WeakReference<>(_bottomPlaylist = new BottomSheetDialog(this));
			View ll = LayoutInflater.from(this).inflate(R.layout.favorite_bottom_sheet, null);
			ListView lv = ll.findViewById(R.id.favorList);
			lv.setAdapter(FavoriteNoteBooksAdapter());
			lv.setOnItemClickListener((parent, view, position, id) -> {
				CheckedTextView tv = view.findViewById(android.R.id.text1);
				tv.toggle();
				tv.jumpDrawablesToCurrentState();
				AppFunAdapter.setChecked(position, tv.isChecked());
			});
			BottomSheetDialog final_bottomPlaylist = _bottomPlaylist;
			OnClickListener clicker = v -> {
				switch (v.getId()) {
					case R.id.cancel:
						final_bottomPlaylist.dismiss();
					break;
					case R.id.confirm:
						if(getUsingDataV2()) {
							Long[] selectionArr = AppFunAdapter.selectedPositionsArr;
							ArrayList<MyPair<String, Long>> items = AppFunAdapter.notebooksV2;
							HashSet<Long> selection = AppFunAdapter.selectedPositions;
							if(selection.size()>0) {
								int delCnt=0, delNum=0, addCnt=0, addNum=0;
								for(Long oldFav:selectionArr) {
									if (!selection.contains(oldFav)) {
										delNum++;
										try {
											if(prepareHistoryCon().remove(text, oldFav)>=0) {
												delCnt++;
											}
										} catch (Exception e) { CMN.debug(e); }
									}
								}
								selection.removeAll(Arrays.asList(selectionArr));
								addNum = selection.size();
								selectionArr = selection.toArray(new Long[addNum]);
								for(Long newFav:selectionArr) {
									try {
										if(prepareHistoryCon().insert(this, text, newFav, weblist)>=0){
											addCnt++;
										}
									} catch (Exception e) { CMN.debug(e); }
								}
								String msg = "";
								if (addNum>0) {
									msg += " 添加完毕！(" + addCnt + "/" + addNum + ")";
								}
								if (delNum>0) {
									if (!TextUtils.isEmpty(msg)) {
										msg += "\t";
									}
									msg += " 移除完毕！(" + delCnt + "/" + delNum + ")";
								}
								if (!TextUtils.isEmpty(msg)) {
									showT(msg);
								}
								selection.clear();
							}
						}
						else {
							ArrayList<MyPair<String, LexicalDBHelper>> items = AppFunAdapter.notebooks;
							HashSet<Long> selection = AppFunAdapter.selectedPositions;
							if(selection.size()>0) {
								int cc=0;
								for (int i = 0; i < items.size(); i++) {
									if (selection.contains((long)i)) {
										try {
											MyPair<String, LexicalDBHelper> iI = items.get(i);
											LexicalDBHelper db = iI.value;
											if (db == null) {
												db = iI.value = new LexicalDBHelper(getApplicationContext(), opt, iI.key, false);
											}
											if(db.insertUpdate(this, text, weblist)>0){
												cc++;
											}
										} catch (Exception ignored) { }
									}
								}
								showT("添加完毕！("+cc+"/"+selection.size()+")");
							}
						}
						final_bottomPlaylist.dismiss();
					break;
					case R.id.new_folder:
						showCreateNewFavoriteDialog((int) (ll.getWidth() - getResources().getDimension(R.dimen._35_)));
					break;
				}
			};
			ll.findViewById(R.id.cancel).setOnClickListener(clicker);
			ll.findViewById(R.id.confirm).setOnClickListener(clicker);
			ll.findViewById(R.id.new_folder).setOnClickListener(clicker);
			_bottomPlaylist.setContentView(ll);
			_bottomPlaylist.getWindow().setDimAmount(0.2f);
			//bottomPlaylist.getWindow().setBackgroundDrawable(null);
			//bottomPlaylist.getWindow().getDecorView().setBackground(null);
			////bottomPlaylist.getWindow().findViewById(R.id.design_bottom_sheet).setBackground(null);
			//fix_full_screen(bottomPlaylist.getWindow().getDecorView());
			_bottomPlaylist.getWindow().getDecorView().setTag(lv);
			//CMN.recurseLogCascade(lv);
			_bottomPlaylist.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);// 展开
			if(GlobalOptions.isDark) {
				ll.setBackgroundColor(Color.BLACK);
				((TextView)ll.findViewById(R.id.title)).setTextColor(Color.WHITE);
				ll.findViewById(R.id.bottombar).getBackground().setColorFilter(GlobalOptions.NEGATIVE);
			}
		}
		if (getUsingDataV2()) {
			FavoriteNoteBooksAdapter().adaptToMultipleCollections(text);
		}
		View v = (View) _bottomPlaylist.getWindow().getDecorView().getTag();
		DisplayMetrics dm2 = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm2);
		v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * _bottomPlaylist.getBehavior().getHalfExpandedRatio() - getResources().getDimension(R.dimen._45_) * 1.75);
		v.requestLayout();
		_bottomPlaylist.show();
	}

	/** @param reason: 0=切换收藏夹; 1=切换收藏夹(收藏夹视图); 2=移动收藏 */
	public void showChooseFavorDialog(int reason) {
		AlertDialog d = ChooseFavorDialog.get();
		if(d==null){
			CMN.debug("重建选择器……");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.pickfavor);
			builder.setPositiveButton(R.string.newfc, null);
			builder.setNeutralButton(R.string.delete, null);
			builder.setItems(new String[] {},null);
			d=builder.create();
			d.show();
			ListView litsView = d.getListView();
			litsView.setId(R.id.click_remove);
			////////////// 列表适配器
			litsView.setAdapter(FavoriteNoteBooksAdapter());
			////////////// 收藏夹列表
			AlertDialog finalD = d;
			litsView.setOnItemClickListener((parent, view, position, id) -> {
				int reason1 = (int) parent.getTag();
				String name;
				MyPair<String, Long> nb = AppFunAdapter.notebooksV2.get(position);
				name = nb.key;
				long NID = nb.value;
				if (DBrowser!=null) {
					if(reason1!=2 && DBrowser.type==DB_FAVORITE) {
						//CMN.Log("// 加载收藏夹", NID);
						putCurrFavoriteNoteBookId(NID);
						DBrowser.Selection.clear();
						DBrowser.loadInAll(this);
					} else if(reason1==2 && DBrowser!=null) {
						// 移动收藏夹
						DBrowser.moveSelectedCardsToFolder(name, NID);
					}
				} else {
					putCurrFavoriteNoteBookId(NID);
				}
				if(reason1!=2) {
					view.post(() -> {
						finalD.dismiss();
						if(reason1==0)
							show(R.string.currFavor,CMN.unwrapDatabaseName(name));
					});
				}
			});
			d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v1 -> {
				AppFunAdapter.showDelete = !AppFunAdapter.showDelete;
				AppFunAdapter.notifyDataSetChanged();
			});
			d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MainActivityUIBase.this,R.color.colorHeaderBlue));
			d.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED);
			d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
				showCreateNewFavoriteDialog(finalD.getListView().getWidth());
			});
			ChooseFavorDialog = new WeakReference<>(d);
		}
		else {
			d.show();
		}
		d.getWindow().setLayout((int) (dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor)), -2);
		d.getListView().setTag(reason);
		if(reason==2)
			d.setTitle("移动到…");
		else
			d.setTitle(R.string.pickfavor);
	}
	
	//定制工具条
	public AppUIProject contentbar_project;
	public AppUIProject peruseview_project;
	
	public int ForegroundTint = 0xffffffff;
	public PorterDuffColorFilter ForegroundFilter;

	void showIconCustomizator() {
		int pos = defbarcustpos;
		if(PeruseViewAttached()){
			pos = 2;
		} else if(thisActType==ActType.PlainDict && contentview.getParent()!=null){
			pos = 1;
		}
		int jd = WeakReferenceHelper.app_bar_customize_dlg;
		BottombarTweakerAdapter ada = (BottombarTweakerAdapter) getReferencedObject(jd);
		if (ada==null) {
			ada = new BottombarTweakerAdapter(this, pos);
			putReferencedObject(jd, ada);
		} else {
			ada.onClick(pos);
		}
		ada.show();
		float pad = 4 * getResources().getDimension(R.dimen._50_);
		int rH = isFloating() ? dm.heightPixels : root.getHeight();
		ada.main_list.mMaxHeight = (rH>=2*pad)?(int) (rH - root.getPaddingTop() - pad):0;
	}
	
	private static final ConcurrentHashMap<String, byte[]> CommonAssets = new ConcurrentHashMap<>(10);
	private static final ConcurrentHashMap<String, String> CommonAssetsStr = new ConcurrentHashMap<>(10);
	static {
		//CommonAssets.put("SUBPAGE.js", BookPresenter.jsBytes);
		//CommonAssets.put("markloader.js", BookPresenter.markJsLoader);
		CommonAssets.put("dk.js", DarkModeIncantation.getBytes());
		CommonAssets.put("imgLoader.js", BookPresenter.imgLoader.getBytes());
		//CommonAssetsStr.put("tapSch.js", BookPresenter.tapTranslateLoader);
	}
	
	public InputStream loadCommonAsset(String key) throws IOException {
		if(MdictServerMobile.hasRemoteDebugServer) {
			try {
				InputStream input = ViewUtils.fileToStream(this, new File("/ASSET2/" + key));
				if(input!=null) return input;
			} catch (Exception e) {
				//CMN.debug(e);
			}
		}
		byte[] data = CommonAssets.get(key);
		if(data==null){
			InputStream input = getResources().getAssets().open(key);
			data = new byte[input.available()];
			input.read(data);
			CommonAssets.put(key, data);
		}
		//CMN.Log(new String(data), data.length);
		return new ByteArrayInputStream(data);
	}
	
	public String getCommonAsset(String key) {
		String ret=CommonAssetsStr.get(key);
		if (ret==null) {
			byte[] data = CommonAssets.get(key);
			if(data==null){
				try {
					InputStream input = getResources().getAssets().open(key);
					data = new byte[input.available()];
					input.read(data);
				} catch (IOException e) { }
				if (data!=null) {
					CommonAssets.put(key, data);
				}
			}
			if (data!=null) {
				CommonAssetsStr.put(key, ret = new String(data));
			}
		}
		if (ret==null) {
			CMN.debug("空的::", key);
		}
		return ret==null?"":ret;
	}
	
	void execSingleSearch(CharSequence cs, int count) {
		try {
			String key = cs.toString().trim();
			//首先，搜索到第一个match，然后尝试变形，两者向下再搜索
			int normal_idx=currentDictionary.bookImpl.lookUp(key, true);
			if(normal_idx==0 && currentDictionary.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
				currentDictionary.SetSearchKey(key);
				if(bIsFirstLaunch||bWantsSelection||来一发) {
					//CMN.debug("自动显示内容::", bIsFirstLaunch, bWantsSelection, 来一发);
					bRequestedCleanSearch=bIsFirstLaunch;
					adaptermy.onItemClick(null, null, normal_idx, 0);
				}
				bIsFirstLaunch=false;
				return;
			}
			int formation_idx=-1;
			
			boolean bFetchMoreContents = true;
			String formation_key=null;
			if(bFetchMoreContents || normal_idx<0 && PDICMainAppOptions.getSearchUseMorphology()) {
				formation_key = ReRouteKey(key, true);
				if(formation_key!=null) {
					formation_idx = currentDictionary.bookImpl.lookUp(formation_key, true);
				}
				if(normal_idx<=-1&&formation_idx>=0) { //treat formation result as normal.
					normal_idx=formation_idx;
					formation_idx=-1;
					key=formation_key;
				}
			}
			
			CMN.debug("单本搜索 ： ", normal_idx, normal_idx==-1?"":currentDictionary.bookImpl.getEntryAt(normal_idx<0?(-normal_idx-3):normal_idx), formation_key);
			if(normal_idx!=-1) {
				int tmpIdx = normal_idx;
				if(normal_idx<0) {
					tmpIdx = -tmpIdx - 3;
					if(tmpIdx<currentDictionary.bookImpl.getNumberEntries()
							&& mdict.processText(currentDictionary.bookImpl.getEntryAt(tmpIdx+1)).startsWith(mdict.processText(key))) {
						tmpIdx++;
					}
				} else {
					lv_matched = true;
					if(bFetchMoreContents) {
						MergeSingleResults(key, normal_idx, formation_idx);
					}
				}
				lv.setSelection(tmpIdx);
				if(bIsFirstLaunch||bWantsSelection||来一发) {
					if(normal_idx>=0) {
						boolean proceed = true;
						if(count>=0) {
							if(thisActType==ActType.PlainDict) {
								if(isContentViewAttached())
									proceed = currentDictionary.lvClickPos!=normal_idx;
							} else {
								if(contentUIData.webcontentlister.getVisibility()==View.VISIBLE) {//webSingleholder.getChildCount()!=1
									String keyTmp = mdict.processText(cs.toString());
									proceed = (adaptermy.currentKeyText == null || !keyTmp.equals(adaptermy.currentKeyText.trim()));
								}
							}
						}
						if(proceed) {
							bRequestedCleanSearch=bIsFirstLaunch;
							/* 接管历史纪录 */
							adaptermy.onItemClick(null, null, normal_idx, 0);
						}
					}
				}
				bIsFirstLaunch=false;
			}
			if(normal_idx<0 && !fastPreview) {
				DetachContentView(false);
			}
		} catch (Exception e) { CMN.debug(e); }
	}
	
	void execBatchSearch(CharSequence cs) {
		if(lianHeTask!=null) {
			lianHeTask.cancel(false);
		}
		//if(lv2.getVisibility()==View.INVISIBLE)
		//	lv2.setVisibility(View.VISIBLE);
		String key = cs.toString().trim();
		if(!key.equals(CombinedSearchTask_lastKey))
			lianHeTask = (AsyncTaskWrapper) new CombinedSearchTask(MainActivityUIBase.this).execute(key);
		else {
			if(bIsFirstLaunch) {
				/* 接管历史纪录 */
				bRequestedCleanSearch=bIsFirstLaunch;
				bIsFirstLaunch=false;
				if(recCom.allWebs || !isContentViewAttached() && mdict.processText(key).equals(mdict.processText(String.valueOf(adaptermy2.results.getResAt(this, 0)))))
				{
					adaptermy2.onItemClick(null, adaptermy2.getView(0, null, null), 0, 0);
				}
			}
			ViewUtils.setVisibleV3(lv2, true);
		}
	}
	
	LongSparseArray<Integer> mergedKeyHeaders = new LongSparseArray();
	private int TotalMergedKeyCount;
	private int ConfidentMergeShift;
	private int ConfidentMergeStart;
	
	private void MergeSingleResults(String key, int normal_idx, int formation_idx) {
		ClearMerges();
		MergeSingleResultsAfter(null, formation_idx);
		MergeSingleResultsAfter(key, ConfidentMergeStart=normal_idx);
		if(ConfidentMergeShift ==-1) ConfidentMergeShift =0;
		CMN.debug("mergedKeyHeaders", mergedKeyHeaders, ConfidentMergeShift);
	}
	
	private void ClearMerges() {
		mergedKeyHeaders.clear();
		TotalMergedKeyCount=0;
		ConfidentMergeShift =-1;
	}
	
	private void MergeSingleResultsAfter(String key, int startIdx) {
		if(startIdx>=0) {
			BookPresenter current = currentDictionary;
			int theta=8;
			long maxID=current.bookImpl.getNumberEntries()-1;
			String startKey = mdict.processText(current.bookImpl.getEntryAt(startIdx));
			int i = 0;
			int idx=startIdx;
			while(true) {
				++i;
				idx++;
				String currentKey;
				if(i>theta || idx>maxID || !startKey.equals(mdict.processText(currentKey=current.bookImpl.getEntryAt(idx)))) {
					break;
				}
				if(key!=null && ConfidentMergeShift ==-1 && currentKey.startsWith(key)) {
					ConfidentMergeShift =i;
				}
			}
			if(i>1) {
				mergedKeyHeaders.put(startIdx, i);
				TotalMergedKeyCount+=i;
			}
		}
	}
	
	/** 仿效 GoldenDict 返回尽可能多的结果 */
	long[] getMergedClickPositions(long pos) {
		long[] ret;
		LongSparseArray<Integer> KeyHeaders = mergedKeyHeaders;
		int mergeCount = KeyHeaders.get(pos, 0); // , 0
		int MergedStartShift = pos==ConfidentMergeStart?ConfidentMergeShift :0;
		if(mergeCount>0) {
			ret = new long[TotalMergedKeyCount];
			for (int i=0; i < mergeCount; i++) {
				ret[i]=pos+(MergedStartShift+i)%mergeCount;
			}
			int mergeSize = KeyHeaders.size();
			if(mergeSize>1) {
				int mergeIdx = mergeCount;
				for (int i = 0; i < mergeSize; i++) {
					long mergeKey = KeyHeaders.keyAt(i);
					if(mergeKey!=pos) {
						mergeCount = KeyHeaders.get(mergeKey);
						for (int j = 0; j < mergeCount; j++) {
							ret[mergeIdx+j]=mergeKey+j;
						}
						mergeIdx+=mergeCount;
					}
				}
			}
		} else {
			ret = new long[]{pos};
		}
		return ret;
	}
	
	Animation loadCTAnimation() {
		if(CTANIMA==null) {
			CTANIMA = AnimationUtils.loadAnimation(this, R.anim.content_in);
		}
		CTANIMA.reset();
		return CTANIMA;
	}
	
	class SaveAndRestorePagePosDelegate {
		public float SaveAndRestoreSinglePageForAdapter(WebViewmy webview, long pos, BasicAdapter ADA) {
			//CMN.Log("SaveAndRestoreSinglePageForAdapter...");
			float ret = -1;
			ScrollerRecord pagerec;
			OUT: //save our postion
			if(System.currentTimeMillis()-lastClickTime>300/*400*/ && !webview.isloading
					&& ADA.lastClickedPosBefore>=0
					&& (ADA.webviewHolder.getChildCount()!=0 /*|| false todo 开放连续的历史纪录 ?*/)) {
				if(webview.webScale==0) {
					webview.webScale=dm.density;//sanity check
				}
				//avoyager.set(avoyagerIdx,(int) (current_webview.getScrollY()/(current.webScale/dm.density)));
				
				pagerec = ADA.avoyager.get(ADA.lastClickedPosBefore);
				if(pagerec==null) {
					if(webview.getScrollX()!=0 || webview.getScrollY()!=0 ||webview.webScale!= BookPresenter.def_zoom) {
						pagerec=new ScrollerRecord();
						ADA.avoyager.put(ADA.lastClickedPosBefore, pagerec);
					} else {
						break OUT;
					}
				}
				
				pagerec.set(webview.getScrollX(), webview.getScrollY(),webview.webScale);
				//showT("保存位置");
				//CMN.Log("保存位置 "+ webview.getScrollY());
			}
			
			lastClickTime=System.currentTimeMillis();
			
			pagerec = ADA.avoyager.get((int) pos); //todo
			if(pagerec!=null) {
				webview.expectedPos = pagerec.y;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				webview.expectedPosX = pagerec.x;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				ret=pagerec.scale;
				//CMN.Log(avoyager.size()+"~"+pos+"~取出旧值"+current.expectedPos+" scale:"+avoyager.get(pos).scale);
			} else {
				webview.expectedPos=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
				webview.expectedPosX=0;///dm.density/(avoyager.get(avoyagerIdx).scale/mdict.def_zoom)
			}
			//showT(""+current.expectedPos);
			return ret;
		}
		
		public void SaveAndRestorePagesForAdapter(BasicAdapter ADA, int pos) {
			resultRecorderDiscrete combining_search_result = ADA.results;
			ScrollerRecord pagerec;
			OUT:
			if (((resultRecorderCombined) combining_search_result).scrolled
					&& ADA.lastClickedPosBefore >= 0
					&& System.currentTimeMillis() - lastClickTime > 300) {
				//CMN.debug("save our postion", lastClickedPosBefore, WHP.getScrollY());
				pagerec = ADA.avoyager.get(ADA.lastClickedPosBefore);
				if (pagerec == null) {
					if (weblistHandler.WHP.getScrollY() != 0) {
						pagerec = new ScrollerRecord();
						ADA.avoyager.put(ADA.lastClickedPosBefore, pagerec);
					} else
						break OUT;
				}
				pagerec.set(0, weblistHandler.WHP.getScrollY(), 1);
				//CMN.Log("保存位置", lastClickedPosBefore);
			}
			
			lastClickTime = System.currentTimeMillis();
			
			pagerec = ADA.avoyager.get(pos);
			if (pagerec != null) {
				combining_search_result.expectedPos = pagerec.y;
				//currentDictionary.mWebView.setScrollY(currentDictionary.expectedPos);
				//CMN.Log("取出旧值", combining_search_result.expectedPos, pos, avoyager.size());
			} else {
				combining_search_result.expectedPos = 0;
				//CMN.Log("新建", combining_search_result.expectedPos, pos);
			}
		}
		
		public void SaveVOA(WebViewmy webview, BasicAdapter ADA) {
			if (webview==null) {
				showT("BUG!!!发生空指针错误(webview)");
				return;
			}
			ScrollerRecord pPos;
			if (ADA == adaptermy2) {
				int selection = (int) ADA.lastClickedPos;
				if (System.currentTimeMillis()-lastClickTime > 400 && selection >= 0) {
					//avoyager.set(avoyagerIdx, WHP.getScrollY());
					pPos = ADA.avoyager.get(selection);
					if (pPos == null && weblistHandler.WHP.getScrollY() != 0) {
						pPos = new ScrollerRecord();
						ADA.avoyager.put(selection, pPos);
					}
					if (pPos != null) {
						pPos.set(0, weblistHandler.WHP.getScrollY(), 1);
					}
					//CMN.Log("保存位置(回退)", lastClickedPos, WHP.getScrollY());
				}
			}
			else {
				if (webview != null
						&& !webview.isloading //
						&& ADA.lastClickedPos >= 0
						&& ADA.webviewHolder.getChildCount() != 0 ) {
					final int selection = (int) webview.currentPos;
					if (webview.webScale == 0) webview.webScale = dm.density;//sanity check
					pPos = ADA.avoyager.get(selection);
					if (webview.shouldStorePagePos(pPos)
							&& System.currentTimeMillis()-lastClickTime > 300) {
						pPos = webview.storePagePos(pPos);
						if (pPos!=null) {
							ADA.avoyager.put(selection, pPos);
						}
					}
					//CMN.Log("回退前暂存位置 ", current_webview.getScrollX(), current_webview.getScrollY(), currentDictionary.webScale);
				}
			}
			lastClickTime = System.currentTimeMillis();
		}
	}
	
	SaveAndRestorePagePosDelegate saveAndRestorePagePosDelegate;
	
	public SaveAndRestorePagePosDelegate DelegateSaveAndRestorePagePos() {
		if(saveAndRestorePagePosDelegate==null)
			saveAndRestorePagePosDelegate=new SaveAndRestorePagePosDelegate();
		return saveAndRestorePagePosDelegate;
	}
	
	public float prepareSingleWebviewForAda(BookPresenter current, WebViewmy wv, long pos, BasicAdapter Ada, boolean remPos, boolean remScale) {
		if(wv==null) {
			wv = current.mWebView;
		}
		float ret=-1;
		if(remPos && !Ada.shunt) {
			ret = DelegateSaveAndRestorePagePos().SaveAndRestoreSinglePageForAdapter(wv, pos, Ada);
		} else {
			wv.expectedPos=0;
			wv.expectedPosX=0;
			bRequestedCleanSearch=false;
		}
		
		if(opt.getAutoReadEntry() && !PDICMainAppOptions.getTmpIsAudior(current.tmpIsFlag)
				||!AutoBrowsePaused&&PDICMainAppOptions.getAutoBrowsingReadSomething())
			wv.bRequestedSoundPlayback=true;
		
		if(wv.fromCombined!=3) {
			wv.fromCombined=0;
		}
		
		if(remScale) {
			float bef=ret;
			ret=wv.webScale;
			if (bef!=0) {
				float scale=ret/bef;
				if (wv.expectedPos!=0) wv.expectedPos*=scale;
				if (wv.expectedPosX!=0) wv.expectedPosX*=scale;
			}
		}
		
		return ret;
	}
	
	
	
	
	@Override
	public long Flag(int flagIndex) {
		if(flagIndex==100) return 0;
		if(flagIndex==101) {
			//CMN.debug("Flag::get", flagIndex, weblist.src);
			return weblist.shezhi;
		}
		return opt.Flag(flagIndex);
	}
	
	@Override
	public void Flag(int flagIndex, long val) {
		//CMN.debug("Flag::set", flagIndex, weblist==null?-1:weblist.src);
		if(flagIndex==100) {}
		else if(flagIndex==101){
			weblist.shezhi = (int) val;
		}
		else opt.Flag(flagIndex, val);
	}
	
	@Override
	public int getDynamicFlagIndex(int flagIdx) {
		return (flagIdx==100||flagIdx==101)?flagIdx:0;
	}
	
	@Override
	public void pickDelegateForSection(int flagIdx, int pickIndex) {
	
	}
	
	@Override
	public void onBackPressed() {
		if(!PerFormBackPrevention(lastBackBtnAct)) {
			if(DetachClickTranslator()) {
				return;
			}
			super.onBackPressed();
		}
	}
	
	
	protected boolean PerFormBackPrevention(boolean bBackBtn) {
		if(!bBackBtn && checkWebSelection())
			return true;
		if(!AutoBrowsePaused || bRequestingAutoReading){
			stopAutoReadProcess();
			return true;
		}
		if(settingsPanel!=null && settingsPanel!=wordPopup) {
			CMN.debug("PerFormBackPrevention", settingsPanel);
			hideSettingsPanel(settingsPanel);
			CMN.debug("PerFormBackPrevention done", settingsPanel);
			return true;
		}
		if(opt.getUseBackKeyGoWebViewBack() && !bBackBtn) {
			//CMN.Log("/* 检查返回键倒退网页 */", view, view==null?false:view.canGoBack());
		}
		if(wordPopup.popupContentView!=null) { //todo
			ViewGroup SVP = (ViewGroup) wordPopup.popupContentView.getParent();
			if((!opt.getPinTapTranslator() || SVP!=null && wordPopup.popupContentView.getTranslationY()<mainF.getHeight()-wordPopup.toolbar.getHeight()) && DetachClickTranslator()) {
				return true;
			}
		}
		if(DBrowser != null) {
			DetachDBrowser();
			return true;
		}
		return false;
	}
	
	public ArrayList<PlainAppPanel> settingsPanels = new ArrayList<>(10);
	public PlainAppPanel settingsPanel;
	public PopupWindow   settingsPopup;
	public View.OnClickListener mInterceptorListener;
	public boolean mInterceptorListenerHandled;
	
	public SearchbarTools etTools;
	
	public void hideSettingsPanel(@NonNull SettingsPanel panel) {
		if(settingsPanel==panel) {
			//CMN.Log("hideSettingsPanel", panel, settingsPanel);
			if (settingsPanel!=null) {
				settingsPanel.dismiss(); // 反调
				if(settingsPanel==panel) {
					settingsPanels.remove(settingsPanel);
					settingsPanel = ViewUtils.getLast(settingsPanels);
					if(settingsPanel!=null) settingsPopup = settingsPanel.pop;
				}
			}
			else if(settingsPopup!=null) settingsPopup = null;
		}
	}
	
	public void embedPopInCoordinatorLayout(PlainAppPanel panel, PopupWindow pop, int padbot, ViewGroup root) {
		if(padbot!=0) {
			if(panel.bottombar!=null) {
				padbot = panel.bottombar.getHeight();
			} else {
				padbot = bottombar!=null?bottombar.getHeight():app_panel_bottombar_height;
			}
			panel.bottomPadding = padbot;
			panel.settingsLayout.setPadding(0,0,0,0);
		}
		settingsPopup = pop;
		pop.setWidth(dm.widthPixels);
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		pop.setBackgroundDrawable(null);
		int[] vLocation = new int[2];
		if (root==null) {
			root = this.root;
		}
		root.getLocationInWindow(vLocation);
		int topY = vLocation[1];
		int h = root.getHeight();
		pop.setWidth(-1);
		pop.setHeight(h-padbot);
		//CMN.debug("embedPopInCoordinatorLayout::", h-padbot, h, padbot, panel.bottombar);
		if (pop.isShowing()) {
			pop.update(0, topY, -1, h-padbot);
		} else {
			if (PeruseViewAttached()) {
				root = peruseView.root;
			}
			pop.showAtLocation(root, Gravity.TOP, 0, topY);
		}
	}
	
	public void HideSelectionWidgets(boolean hideSel) {
//		WebFrameLayout layout = this.currentViewImpl;
//		if (layout!=null) {
//			layout.suppressSelection(hideSel);
//		}
	}
	
	public void fixFocusHiddenSelectionWidgets(Object d) {
//		WebFrameLayout layout = this.currentViewImpl;
//		if (layout!=null && layout.hasSelection()) {
//			layout.focusFucker.add(d);
//		}
	}
	
	public void showMenuGrid(View btn) {
		int jd = WeakReferenceHelper.menu_grid;
		MenuGrid menuGrid = (MenuGrid) getReferencedObject(jd);
		View btm = findWebList(btn);
		if (menuGrid==null) {
			menuGrid = new MenuGrid(this);
			putReferencedObject(jd, menuGrid);
			//CMN.Log("新建MenuGrid...");
		} else {
			menuGrid.refresh();
		}
		menuGrid.bottombar = btm;
		ViewGroup root=null;
		if(peruseView!=null && btm==peruseView.contentUIData.bottombar2)
		{
			root = peruseView.root;
		}
		if (!menuGrid.isVisible()) {
			boolean hasContent = ViewUtils.ViewIsId(btm, R.id.bottombar2);
			if (!hasContent) {
				if (getCurrentFocus() instanceof TextView && ((TextView) getCurrentFocus()).hasSelection()) {
					hasContent = true;
				}
			}
			menuGrid.show(root, hasContent, -1);
		}
	}
	
	public Object getReferencedObject(int id) {
		if(WeakReferencePool[id] == null) {
			return null;
		}
		return WeakReferencePool[id].get();
	}
	
	public WeakReference getReferenceObject(int id) {
		if(WeakReferencePool[id] == null) {
			return ViewUtils.DummyRef;
		}
		return WeakReferencePool[id];
	}
	
	public void putReferencedObject(int id, Object object) {
		WeakReferencePool[id] = new WeakReference(object);
	}
	
	final int[] ScreenOrientation = new int[]{
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
			,ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
			,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
			,ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
			,ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
			,ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
			,ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
			,ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
			,ActivityInfo.SCREEN_ORIENTATION_LOCKED
	};
	
	public void setScreenOrientation(int idx) {
		CMN.debug("setScreenOrientation::", idx, ScreenOrientation[idx]);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		if(opt.getLockStartOrientation()) {
			opt.setUserOrientation(idx);
		}
		opt.setTmpUserOrientation1(idx);
		setRequestedOrientation(ScreenOrientation[idx]);
	}
	
	public void showBookPreferences(BookPresenter...books) {
		int jd = WeakReferenceHelper.dict_opt;
		BookOptionsDialog dialog = (BookOptionsDialog) getReferencedObject(jd);
		if (dialog==null) {
			dialog = new BookOptionsDialog();
			putReferencedObject(jd, dialog);
		}
		dialog.bookOptions.setData(books);
		try {
			if (!dialog.isAdded()) {
				dialog.show(getSupportFragmentManager(), "");
			} else {
				dialog.getDialog().show();
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	private ViewGroup getMenuGridRootViewForPanel(PlainAppPanel panel) {
		ViewGroup root = null;
		if(peruseView!=null && weblist==peruseView.weblistHandler)
		{
			root = peruseView.root;
		}
		try {
			panel.bottombar = ViewUtils.isVisibleV2(weblist.contentUIData.webcontentlister)
					?weblist.contentUIData.bottombar2:bottombar;
		} catch (Exception e) {
			CMN.debug(e);
			panel.bottombar = null;
		}
		return root;
	}
	
	public void showBookSettings() {
		WebViewListHandler weblist = this.weblist;
		int jd = WeakReferenceHelper.quick_settings;
		QuickBookSettingsPanel quickSettings
				= (QuickBookSettingsPanel) getReferencedObject(jd);
		if (quickSettings==null) {
			quickSettings = new QuickBookSettingsPanel(this);
			putReferencedObject(jd, quickSettings);
			CMN.debug("重建QuickBrowserSettingsPanel...");
		}
		quickSettings.refresh();
		quickSettings.toggle(getMenuGridRootViewForPanel(quickSettings), (SettingsPanel) getReferencedObject(WeakReferenceHelper.menu_grid), -2);
	}
	
	public void showNightModeSwitch() {
		int jd = WeakReferenceHelper.night_mode;
		NightModeSwitchPanel quickSettings
				= (NightModeSwitchPanel) getReferencedObject(jd);
		if (quickSettings==null) {
			quickSettings = new NightModeSwitchPanel(this);
			putReferencedObject(jd, quickSettings);
			//CMN.Log("重建NightModeSwitchPanel...");
		}
		quickSettings.refresh();
		boolean vis = quickSettings.toggle(getMenuGridRootViewForPanel(quickSettings), (SettingsPanel) getReferencedObject(WeakReferenceHelper.menu_grid), -2);
	}
	
	
	public void toggleDarkMode() {
		if (thisActType==ActType.PlainDict) {
			((PDICMainActivity) this).drawerFragment.sw4.toggle();
		} else {
			opt.setInDarkMode(!GlobalOptions.isDark);
			changeToDarkMode();
		}
	}
	
	public MdictServerMobile getMdictServer() {
		try {
			if(server==null) {
				if(Thread.currentThread().getId()!=CMN.mid) {
					CMN.debug("在异常建立MdictServer！");
				}
				server = new MdictServerMobile(8080, this, opt, this.loadManager);
				((AgentApplication)getApplication()).mServer = server;
			}
		} catch (IOException e) {
			CMN.debug(e);
		}
		return server;
	}
	
	public void showDictionaryManager() {
		ReadInMdlibs(null);
		AgentApplication app = ((AgentApplication) getApplication());
		app.mdict_cache = mdict_cache;
		//todo remove???
//		for(BookPresenter mdTmp:md) {
//			if(mdTmp!=null){
//				//get path put
//				mdict_cache.put(mdTmp.getDictionaryName(),mdTmp);
//			}
//		}
//		for(BookPresenter mdTmp:loadManager.currentFilter) {
//			if(mdTmp!=null){
//				mdict_cache.put(mdTmp.getDictionaryName(),mdTmp);
//			}
//		}
//		app.slots=CosyChair;
		app.opt=opt;
		app.loadManager=loadManager;
		app.mdlibsCon=mdlibsCon;
//		app.mdict_cache=mdict_cache;
//		CosySofa.clear();
//		HdnCmfrt.clear();
		//todo 123
		// lazyLoadManager().lastLoadedModule=null;
		Intent intent = new Intent();
		intent.setClass(MainActivityUIBase.this, BookManager.class);
		startActivityForResult(intent, BookManager.id);
	}
	
	/** ensure content visibility */
	public void viewContent(WebViewListHandler wlh) {
		wlh.viewContent();
	}
	
	String tw1StrP;
	public boolean storeLv1(String text) {
		if ((textFlag &0x1)!=0) {
			if((textFlag &0x2)==0) {
				tw1StrP = mdict.processText(etSearch.getText());
				textFlag |=0x2;
			}
			if(Math.abs(tw1StrP.length()-text.length())<15 && mdict.processText(text).equals(tw1StrP)) {
				textFlag &=~0x1;
				return true;
			}
		}
		return false;
	}
	
	public void checkFloatBtn() {
		if (PDICMainAppOptions.floatBtn(opt.SixthFlag()>>(30+thisActType.ordinal()))) {
			getFloatBtn().reInitBtn(0);
		} else if(floatBtn!=null) {
			floatBtn.close();
		}
	}
	
	void handleFloatMessage(Message msg) {
		View focus = getCurrentFocus();
		FloatBtn fb = (FloatBtn) msg.obj;
		if (focus instanceof WebView) {
			((WebView) focus).evaluateJavascript("getSelection().toString()", new ValueCallback<String>() {
				@Override
				public void onReceiveValue(String value) {
					String newKey = "";
					if (value.length() > 2) {
						value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
						if (value.length() > 0) {
							newKey = value;
						}
					}
					if (newKey.length()>0) {
						fb.search(newKey, false);
					} else {
						fb.search(null, false);
					}
				}
			});
			return;
		}
		if (focus instanceof TextView) {
			TextView tv = (TextView) focus;
			try {
				String newKey = tv.getText().subSequence(tv.getSelectionStart(), tv.getSelectionEnd()).toString();
				if (newKey.length()>0) {
					fb.search(newKey, false);
					return;
				}
			} catch (Exception ignored) { }
		}
		fb.search(null, false);
		//CMN.Log("focus::", focus);
	}
	
	PageSlide.Pager pager;
	/** 页面滑动切换管理器 */
	public PageSlide.Pager getPageListener() {
		return pager==null?pager=new PageSlide.Pager() {
			long currentPos=0;
			@Override
			public void onMoving(float val, PageSlide page) {
				WebViewListHandler wPage = page.weblist;
				if (val==Integer.MAX_VALUE) {
					//CMN.Log("onPreparePage!!!");
					if (page.weblist.bDataOnly) {
						page.weblist.dictView.setBackgroundColor(AppWhite);
					}
					currentPos=0;
					page.setTextColor(AppBlack);
					page.setBackgroundColor(ColorUtils.blendARGB(AppWhite, Color.GRAY, 0.2f));
				} else {
					//boolean turn = Math.abs(val)>20*dm.density;
					int pos = val<0?1:-1;
					if (currentPos!=pos)
					{
						currentPos=pos;
						page.setGravity(Gravity.CENTER_VERTICAL|(val>0?Gravity.LEFT:Gravity.RIGHT));
						if (wPage.isFoldingScreens() && wPage.multiDicts) {
							if (PDICMainAppOptions.slidePageFd()) {
								ArrayList<BookPresenter> fms = wPage.frames;
								int toPos = wPage.batchDisplaying().LongestStartWithSeqLength;
								if (toPos>=0) toPos = pos;
								else toPos = pos-toPos;
								if (toPos>=0 && toPos<fms.size()) {
									page.setText(fms.get(toPos).getDictionaryName());
									return;
								} else if(!PDICMainAppOptions.slidePage1D()) {
									page.setText(toPos<0?"←":"→");
								}
							}
						}
						if (DBrowser!=null && wPage==DBrowser.weblistHandler) {
							page.setText(DBrowser.getEntryAt(DBrowser.currentPos+pos));
						} else {
							if (wPage == weblistHandler) {
								if (ActivedAdapter!=null) {
									page.setText(ActivedAdapter.getEntry(pos+ActivedAdapter.lastClickedPos));
								}
							}
							else if (wPage.multiRecord!=null) {
								resultRecorderCombined rec = wPage.multiRecord;
								if (rec!=null) {
									page.setText(rec.getResAt(MainActivityUIBase.this, rec.viewingPos+pos));
								}
							}
							else if(wPage.dictView!=null){
								page.setText(wPage.dictView.presenter.getBookEntryAt((int) (pos+wPage.dictView.currentPos)));
							}
						}
					}
				}
			}
			@Override
			public void slidePage(int Dir, PageSlide page) {
				//IMPageCover.getBackground().setAlpha(0);
				WebViewListHandler wPage = page.weblist;
				CharSequence t = page.getText();
				if (t.length() == 1 && (TextUtils.equals(t, "←") || TextUtils.equals(t, "→"))) {
					page.decided = 0;
				}
				View v = Dir==-1?wPage.browserWidget11:Dir==1?wPage.browserWidget10:null;
				if (v!=null) {
					v.setTag(v);
					v.performClick();
				}
			}
		}:pager;
	}
	
	/* 自动播放声音自动播报 */
	private void readEntry(WebViewmy mWebView) {
		mWebView.bRequestedSoundPlayback=false;
		weblist = mWebView.weblistHandler;
		
		// CMN.debug("readEntry!!!", AutoBrowsePaused, PDICMainAppOptions.getAutoBrowsingReadSomething(), opt.getThenAutoReadContent());
		
		if(AutoBrowsePaused||(!PDICMainAppOptions.getAutoBrowsingReadSomething())){
			postReadEntry();
			if(bThenReadContent=opt.getThenAutoReadContent()){
				pendingWebView=mWebView;
			}
		} else {
			//faking opf
			mThenReadEntryCount=2;
			if(mThenReadEntryCount==0){
				bThenReadContent=PDICMainAppOptions.getAutoBrowsingReadContent();
			}
			if(TTSController_engine!=null){
				TTSController_engine.setOnUtteranceProgressListener(null);
				TTSController_engine.stop();
			}
			if(PDICMainAppOptions.getAutoBrowsingReadEntry()){
				postReadEntry();
			} else if(bThenReadContent){
				bThenReadContent = false;
				postReadContent(mWebView);
			}
			pendingWebView=mWebView;
		}
	}
	
	/** 延迟加载 */
	private void loadNext(WebViewmy mWebView) {
		if(PDICMainAppOptions.getDelaySecondPageLoading()
				&& !(PDICMainAppOptions.getOnlyExpandTopPage() && mWebView.frameAt+1>=opt.getExpandTopPageNum()) ){
			int next = fastFrameIndexOf(weblistHandler, mWebView, mWebView.frameAt) + 1;
			//CMN.debug("/* 延迟加载 */ ?????? ", next, weblistHandler.getChildCount(), PDICMainAppOptions.getOnlyExpandTopPage(), mWebView.frameAt, opt.getExpandTopPageNum());
			if(next < weblistHandler.getChildCount()){
				View childAt;
				try {
					while ((childAt=weblistHandler.getChildAt(next++))!=null) {
						if (childAt.getTag() instanceof WebViewmy) {
							mWebView = ((WebViewmy) childAt.getTag());
							BookPresenter presenter = mWebView.presenter;
							if(presenter.mWebView.awaiting
									&& !PDICMainAppOptions.getTmpIsCollapsed(presenter.tmpIsFlag)
									&& !presenter.getAutoFold()
							){
								//CMN.debug("/* 延迟加载 */", presenter.getDictionaryName());
								presenter.toolbar_title.performClick();
								break;
							}
						}
					}
				} catch (Exception ignored) {  }
			}
		}
	}
	
	Drawable.ConstantState titleDrawableCS;
	public final Drawable titleDrawable() {
		if(titleDrawableCS==null)
			titleDrawableCS = mResource.getDrawable(R.drawable.progressbar).mutate().getConstantState();
		return titleDrawableCS.newDrawable();
	}
	
	public boolean isPanelDecorView(View view) {
		for (int i = settingsPanels.size()-1; i >= 0; i--) {
			PlainAppPanel panel = settingsPanels.get(i);
			if (panel.getLastShowType()!=0 && ViewUtils.ViewIsChildOf(panel.settingsLayout, view)) {
				return true;
			}
		}
		return false;
	}
	
	public final boolean isMultiShare() {
		return thisActType == ActType.MultiShare;
	}
	
	public final boolean isFloating() {
		return floatApp!=null && floatApp.isFloating();
	}
	
	public final boolean isFloatingApp() {
		return floatApp!=null && floatApp.isAppFloating();
	}
	
	public FloatBtn getFloatBtn() {
		if (floatBtn==null) {
			floatBtn = new FloatBtn(this, getApplication());
//			if (floatApp==null && thisActType==ActType.PlainDict) {
//				floatApp = new FloatApp((PDICMainActivity) this);
//			} ???
		}
		return floatBtn;
	}
	
	@Override
	public void startActivity(Intent intent) {
		intent.putExtra(FloatBtn.EXTRA_INVOKER, BuildConfig.APPLICATION_ID);
		super.startActivity(intent);
	}
	
	public Map<SubStringKey, BookPresenter> webxford = new HashMap<>();
	public void registerWebx(BookPresenter presenter) {
		PlainWeb webx = presenter.getWebx();
		webxford.put(SubStringKey.new_hostKey(webx.getHost()), presenter);
	}
	
	public WahahaTextView.ViewRootHolder mViewRootHolder = new WahahaTextView.ViewRootHolder();
	
	public OnTouchListener lineRightClicker = new OnTouchListener() {
		int x0,y0;
		int scrollY;
		int ln;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			if(action ==MotionEvent.ACTION_DOWN){
				TextView preview = (TextView) v;
				x0= (int) event.getX();
				y0= (int) event.getY();
				Layout layout = preview.getLayout();
				ln = layout.getLineForVertical(y0);
				float ed = layout.getLineRight(ln);
				CMN.debug("ln::", ln, ed, x0);
				if (x0 - GlobalOptions.density*2 > ed) {
					ListView lv = (ListView) ViewUtils.getParentByClass(v, ListView.class);
					scrollY = lv.getScrollY();
				} else {
					scrollY = -1;
				}
			}
			else if(action==MotionEvent.ACTION_UP/*|| action ==MotionEvent.ACTION_CANCEL*/){
				if (scrollY >= 0) {
					TextView preview = (TextView) v;
					int x= (int) event.getX();
					int y= (int) event.getY();
					Layout layout = preview.getLayout();
					ln = layout.getLineForVertical(y);
					float ed = layout.getLineRight(ln);
					if (x > ed && Math.sqrt((x0-x)*(x0-x)+(y0-y)*(y0-y)) < 35*GlobalOptions.density) {
						CMN.debug("ln::", ln, ed, x);;
						ListView lv = (ListView) ViewUtils.getParentByClass(v, ListView.class);
						if (scrollY == v.getScrollY()) {
							ViewHolder vh = (ViewHolder) ViewUtils.getViewHolderInParents(v, ViewHolder.class);
							vh.itemView.performClick();
						}
					}
				}
			}
			return false;
		}
	};
	
	public static class ViewHolder {
		public boolean lastSch;
		int position;
		final View itemView;
		WahahaTextView title;
		WahahaTextView subtitle;
		WahahaTextView preview;
		ImageView loader;
		
		public boolean selectable;
		
		public ViewHolder(MainActivityUIBase a, int resId, ViewGroup parent) {
			itemView = LayoutInflater.from(a).inflate(resId, parent, false);
			itemView.setId(R.id.lvitems);
			title = itemView.findViewById(R.id.text);
			subtitle = itemView.findViewById(R.id.subtext);
			preview = itemView.findViewById(R.id.preview);
			title.mR = a.mViewRootHolder;
			if (subtitle!=null) subtitle.mR = a.mViewRootHolder;
			if (preview!=null){
				preview.mR = a.mViewRootHolder;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					preview.setOnScrollChangeListener(ViewUtils.getNoScrollListener());
			}
			itemView.setTag(this);
		}
		
		public ImageView getLoadTarget() {
			if (loader==null) {
				loader = new ImageView(itemView.getContext());
				((ViewGroup)itemView).addView(loader);
				loader.getLayoutParams().width=10;
				loader.getLayoutParams().height=10;
			}
			return loader;
		}
	}
	
	static class PreviewData extends Drawable {
		final CharSequence preview;
		PreviewData(CharSequence preview) {
			this.preview = preview;
		}
		
		@Override
		public void draw(@NonNull Canvas canvas) {
		
		}
		
		@Override
		public void setAlpha(int alpha) {
		
		}
		
		@Override
		public void setColorFilter(@Nullable ColorFilter colorFilter) {
		
		}
		
		@Override
		public int getOpacity() {
			return 0;
		}
	}
	static class PreviewBean extends AppInfoBean {
		final BookPresenter book;
		final long pos;
		public PreviewBean(BookPresenter book, int pos) {
			this.book = book;
			this.pos = pos;
		}
		public Drawable load() {
			Drawable ret = null;
			try {
				return new PreviewData(getPreviewForBook(book, pos));
			} catch (Exception e) {
				CMN.debug(e);
			}
			return ret;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			PreviewBean that = (PreviewBean) o;
			return pos == that.pos && Objects.equals(book, that.book);
		}
		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), book, pos);
		}
	}
	RequestBuilder<Drawable> previewLoader;
	
	public static CharSequence getPreviewForBook(BookPresenter book, long pos) throws IOException {
		String record = book.bookImpl.getRecordAt(pos, null, false);
//		if(record.length()>1024) {
//			record = record.substring(0, 1024);
//			CMN.debug("裁剪了！", record);
//		}
		String text = Jsoup.parse(record).text();
		if (book.isMdict() && book.getMdict().hasStyleSheets() && text.contains("`")) {
			text = markerReg.matcher(text).replaceAll("").trim();
		}
		 if (true)
			 return text;
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append(book.getInListName());
		ssb.setSpan(new ColoredTextSpan1(0xFFb0b0b0), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		//ssb.append(" - ");
		String entry = book.bookImpl.getEntryAt(pos);
		if (!text.startsWith(" ")) {
			ssb.append(" ");
		}
		ssb.append(text, text.startsWith(entry)?entry.length():0, text.length());
		return ssb;
	}
	
	public CharSequence getPreviewFor(ViewHolder vh, BookPresenter book, int pos) throws IOException {
		//BookPresenter book = getBookById(id);
		if (book != EmptyBook) {
			boolean async = true;
			if (async) {
				if (previewLoader==null) {
					RequestOptions options = new RequestOptions()
							.skipMemoryCache(false)
							//.diskCacheStrategy(DiskCacheStrategy.NONE)
							.fitCenter()
							//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							;
					previewLoader = Glide.with(this)
							.load(new AppIconCover(new PreviewBean(book, pos), false))
							.listener(new RequestListener<Drawable>() {
								@Override
								public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
									ImageView iv = ((ImageViewTarget<?>) target).getView();
									MainActivityUIBase.ViewHolder vh = (MainActivityUIBase.ViewHolder) ViewUtils.getViewHolderInParents(iv, MainActivityUIBase.ViewHolder.class);
									vh.preview.setText("加载失败！"+e);
									return false;
								}
								@Override
								public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
									ImageView iv = ((ImageViewTarget<?>) target).getView();
									MainActivityUIBase.ViewHolder vh = (MainActivityUIBase.ViewHolder) ViewUtils.getViewHolderInParents(iv, MainActivityUIBase.ViewHolder.class);
									PreviewData data = (PreviewData) resource;
									vh.preview.setText(data.preview);
									return false;
								}
							})
							.apply(options);
				}
				previewLoader.load(new AppIconCover(new PreviewBean(book, pos), false))
						.into(vh.getLoadTarget());
				return ViewUtils.WAIT;
			} else {
				return getPreviewForBook(book, pos);
			}
		}
		return null;
	}
	
	public TextView listName(int i) {
		return new TextView(this);
	}
	
	public void switchSearchEngineLst(boolean schEgn) {
	}
}
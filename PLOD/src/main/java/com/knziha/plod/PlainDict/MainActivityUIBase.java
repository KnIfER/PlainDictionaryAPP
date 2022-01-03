package com.knziha.plod.plaindict;

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
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.jaredrummler.colorpicker.ColorPickerDialogListener;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.filepicker.view.GoodKeyboardDialog;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.PlainUI.AnnotAdapter;
import com.knziha.plod.PlainUI.AppUIProject;
import com.knziha.plod.PlainUI.BookmarkAdapter;
import com.knziha.plod.PlainUI.BottombarTweakerAdapter;
import com.knziha.plod.PlainUI.BuildIndexInterface;
import com.knziha.plod.PlainUI.DBUpgradeHelper;
import com.knziha.plod.PlainUI.MenuGrid;
import com.knziha.plod.PlainUI.NightModeSwitchPanel;
import com.knziha.plod.PlainUI.QuickBookSettingsPanel;
import com.knziha.plod.PlainUI.WeakReferenceHelper;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.db.MdxDBHelper;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.AutoCloseInputStream;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.MyPair;
import com.knziha.plod.dictionary.Utils.ReusableBufferedInputStream;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymanager.BookManager;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.SparseArrayMap;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.PlainPDF;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.searchtasks.CombinedSearchTask;
import com.knziha.plod.settings.BookOptionsDialog;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.slideshow.PhotoViewActivity;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.BottomNavigationBehavior;
import com.knziha.plod.widgets.CustomShareAdapter;
import com.knziha.plod.widgets.DragScrollBar;
import com.knziha.plod.widgets.FlowCheckedTextView;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.IMPageSlider;
import com.knziha.plod.widgets.ListSizeConfiner;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.MultiplexLongClicker;
import com.knziha.plod.widgets.OnScrollChangedListener;
import com.knziha.plod.widgets.PopupGuarder;
import com.knziha.plod.widgets.PopupMoveToucher;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.TwoColumnAdapter;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.widgets.XYTouchRecorder;
import com.knziha.text.ColoredHighLightSpan;
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
import org.knziha.metaline.Metaline;
import org.knziha.metaline.StripMethods;
import org.xiph.speex.ByteArrayRandomOutputStream;
import org.xiph.speex.manyclass.JSpeexDec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.util.Util.isOnMainThread;
import static com.knziha.plod.PlainUI.AppUIProject.ContentbarBtnIcons;
import static com.knziha.plod.PlainUI.AppUIProject.RebuildBottombarIcons;
import static com.knziha.plod.dictionary.Utils.IU.NumberToText_SIXTWO_LE;
import static com.knziha.plod.dictionarymodels.BookPresenter.RENDERFLAG_NEW;
import static com.knziha.plod.dictionarymodels.BookPresenter.baseUrl;
import static com.knziha.plod.plaindict.CMN.AssetTag;
import static com.knziha.plod.plaindict.MainShareActivity.SingleTaskFlags;
import static com.knziha.plod.plaindict.MdictServerMobile.getTifConfig;
import static com.knziha.plod.widgets.WebViewmy.getWindowManagerViews;

/** 程序基础类<br/>
 *  Class for all dictionary activities. <br/>
 * Created by KnIfER on 2018
 */
@SuppressLint({"ResourceType", "SetTextI18bbn","Registered", "ClickableViewAccessibility","PrivateApi","DiscouragedPrivateApi"})
@StripMethods(strip=!BuildConfig.isDebug, keys={"setMagicNumber", "setWebDebug"})
public abstract class MainActivityUIBase extends Toastable_Activity implements OnTouchListener,
		OnLongClickListener,
		OnClickListener,
		OnMenuItemClickListener, OnDismissListener,
		MenuItem.OnMenuItemClickListener,
		OptionProcessor,
		SettingsPanel.FlagAdapter,
		MdictServerLet {
	
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
	protected String debugString=null;//世           界     你好 happy呀happy\"人\"’。，、？
	public static final KeyEvent BackEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
	final static String entryTag = "entry://";
	final static String soundTag = "sound://";
	protected final static String soundsTag = "sounds://";
	public boolean hideDictToolbar=false;
	public int pickTarget;
	public boolean isBrowsingImgs;
	public OnLongClickListener mdict_web_lcl;
	public int defbarcustpos;
	public int cbar_key;
	public StringBuilder MainStringBuilder;
	Runnable execSearchRunnable;
	CharSequence search_cs;
	int search_count;
	public FrameLayout lvHeaderView;
	protected TextWatcher tw1 = new TextWatcher() { //tw
		public void onTextChanged(CharSequence cs, int start, int before, int count) {
			if(SU.isNotEmpty(cs)) {
				etSearch_ToToolbarMode(3);
				root.removeCallbacks(execSearchRunnable);
				search_cs=cs;
				search_count=count;
				if(start==before&&before==-1) {
					execSearchRunnable.run();
				} else {
					root.postDelayed(execSearchRunnable, 150);
				}
			} else {
				if(PDICMainAppOptions.getSimpleMode()) adaptermy.notifyDataSetChanged();
				lv2.setVisibility(View.INVISIBLE);
			}
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		public void afterTextChanged(Editable s) {
			//if (s.length() == 0) ivDeleteText.setVisibility(View.GONE);
			//else  ivDeleteText.setVisibility(View.VISIBLE);
			if (s.length() != 0) ivDeleteText.setVisibility(View.VISIBLE);
		}
	};
	boolean bShowLoadErr=true;
	public boolean isCombinedSearching;
	public String CombinedSearchTask_lastKey;
	//public HashMap<CharSequence,byte[]> mBookProjects;
	//public HashSet<CharSequence> dirtyMap;
	
	public ContentviewBinding contentUIData;
	
	public Drawer drawerFragment;
	public DictPicker pickDictDialog;
	public int GlobalPageBackground=-1;
	public DragScrollBar mBar;
	protected FrameLayout.LayoutParams mBar_layoutParmas;
	public ViewGroup main;
	public ViewGroup mainF;
	public ViewGroup webholder;
	public ScrollViewmy WHP;
	public ViewGroup webSingleholder;
	protected WindowManager wm;

	protected String lastEtString;
	public ViewGroup main_succinct;

	public ListViewmy lv,lv2;
	protected ViewGroup mlv;
	public BasicAdapter adaptermy;
	public BasicAdapter adaptermy2;
	public BasicAdapter adaptermy3;
	public PDICMainActivity.ListViewAdapter2 adaptermy4;
	public BasicAdapter PrevActivedAdapter;
	public BasicAdapter ActivedAdapter;
	public BaseHandler hdl;
	public int  CurrentViewPage = 1;
	public String fontFaces;
	
	MenuBuilder AllMenus;
	List<MenuItemImpl> MainMenu;
	List<MenuItemImpl> LEFTMenu;
	List<MenuItemImpl> SingleContentMenu;
	List<MenuItemImpl> Multi_ContentMenu;
	
	ViewGroup dialogHolder;
	boolean dismissing_dh;
	ViewGroup snack_holder;
	public BookPresenter EmptyBook;
	@NonNull public BookPresenter currentDictionary;
	public ArrayList<BookPresenter> currentFilter = new ArrayList<>();
	public int adapter_idx;
	HashSet<String> mdlibsCon;
	public ArrayList<BookPresenter> md = new ArrayList<>();//Collections.synchronizedList(new ArrayList<mdict>());

	public Dialog taskd;
	DArrayAdapter AppFunAdapter;
	BufferedWriter output;
	BufferedWriter output2;
	public final static String ce_on="document.body.contentEditable=!0";
	public final static String ce_off="document.body.contentEditable=!1";
	LexicalDBHelper favoriteCon;//public LexicalDBHelper getFDB(){return favoriteCon;};
	/** Use a a filename-directory map to keep a record of lost files so that users could add them back without the need of restore every specific directory.  */
	HashMap<String,String> lostFiles;


	SplitView webcontentlist;
	protected IMPageSlider IMPageCover;
	public PeruseView peruseView;
	public ViewGroup bottombar2;
	/** 主程有 */
	public @Nullable ViewGroup bottombar;
	
	ImageView favoriteBtn;
	public ImageView widget7;
	public ImageView widget10;
	public ImageView widget11;
	public ImageView widget12;

	public boolean bRequestedCleanSearch;
	public boolean bWantsSelection;
	public boolean 来一发;
	public boolean bIsFirstLaunch=true;

	public RLContainerSlider PageSlider;

	public View widget13,widget14;

	public ProgressBar main_progress_bar;
	protected int DockerMarginL,DockerMarginR,DockerMarginT,DockerMarginB;

	boolean isFragInitiated = false;

	Canvas mPageCanvas = new Canvas();
	Matrix HappyMatrix = new Matrix();
	BitmapDrawable mPageDrawable;
	ColorDrawable mPageColorDrawable;

	AsyncTask lianHeTask;
	public int[] pendingLv2Pos;
	public int pendingLv2ClickPos=-1;
	public int split_dict_thread_number;
	public static final ListSizeConfiner mListsizeConfiner = new ListSizeConfiner();
	private Runnable NaughtyJumpper;
	volatile long jumpNaughtyTimeToken;

	public AtomicInteger poolEUSize = new AtomicInteger(0);

	Runnable mPopupRunnable;
	String popupKey;
	int popupFrame;
	BookPresenter popupForceId;
	protected TextView popupTextView;
	protected PopupMoveToucher popupMoveToucher;
	public FlowTextView popupIndicator;
	public RLContainerSlider PopupPageSlider;
	public WebViewmy popupWebView;
	public BookPresenter.AppHandler popuphandler;
	public ImageView popIvBack;
	public View popCover;
	protected ViewGroup popupContentView;
	protected ImageView popupStar;
	protected ViewGroup popupToolbar, popupBottombar;
	protected CircleCheckBox popupChecker;
	WeakReference<ViewGroup> popupCrdCloth;
	WeakReference<ViewGroup> popupCmnCloth;
	WeakReference<AlertDialog> setchooser;
	int lastCheckedPos = -1;
	private WeakReference<BottomSheetDialog> bottomPlaylist;
	WeakReference<DBroswer> DBrowser_holder = new WeakReference<>(null);
	WeakReference<DBroswer> DHBrowser_holder = new WeakReference<>(null);
	WeakReference<AlertDialog> ChooseFavorDialog;
	DBroswer DBrowser;
	public PopupGuarder popupGuarder;
	public String currentClickDisplaying;
	public int currentClickDictionary_currentPos;
	public int currentClick_adapter_idx;
	public int CCD_ID;
	@NonNull public BookPresenter CCD;
	ArrayList<myCpr<String, int[]>> popupHistory = new ArrayList<>();
	int popupHistoryVagranter=-1;
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
	public boolean background;
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
	private boolean bHasDedicatedSeachGroup;
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
	private boolean read_click_search;
	protected ActType thisActType;
	public boolean awaiting;
	
	enum ActType{
		PlainDict
		, FloatSearch
		, MultiShare
	}
	protected boolean lv_matched;
	private Animation CTANIMA;
	private ViewGroup collectFavoriteView;
	protected boolean lastBackBtnAct;
	
	public boolean checkWebSelection() {
		WebViewmy wv = null;
		boolean doCheck = false;
		if(popupContentView!=null && popupWebView.bIsActionMenuShown) {
			wv = popupWebView;
			doCheck = opt.getUseBackKeyClearWebViewFocus();
		} else if(getCurrentFocus() instanceof WebViewmy) {
			wv = ((WebViewmy)getCurrentFocus());
			if (wv!=null && wv.bIsActionMenuShown) doCheck = opt.getUseBackKeyClearWebViewFocus();
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
	protected int filter_count;
	protected int hidden_count;
	@Metaline
	private static final String PDFPage="PAGE";
	public resultRecorderCombined recCom;
	protected boolean forbidVolumeAjustmentsForTextRead;
	private ColoredHighLightSpan timeHLSpan;
	private static final float[] TTS_LEVLES_SPEED = new float[]{0.25f, 0.75f, 1f, 1.25f, 1.75f, 2f, 2.5f, 2.75f, 4f};
	private int TTSSpeed = 2;
	private int TTSPitch = 2;
	private float TTSVolume = 1.f;
	private ViewGroup TTSController_controlBar;
	private WebViewmy mCurrentReadContext;
	protected boolean updateAI=true;

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
	
	
	private void reload_dict_at(int i) {
		try {
			ArrayList<PlaceHolder> CosyChair = getLazyCC();
			PlaceHolder phTmp = CosyChair.get(i);
			BookPresenter mdTmp = md.get(i);
			if(mdTmp!=null) {
				mdTmp.Reload(this);
			} else {
				md.set(i, new_book(phTmp, this));
			}
			showT("重新加载!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean switch_To_Dict_Idx(int i, boolean invalidate, boolean putName, AcrossBoundaryContext prvNxtABC) {
		updateAI = true;
		boolean prvNxt = prvNxtABC!=null;
		int size=md.size();
		if(size>0) {
			if(i<0||i>=size) {
				if(prvNxt) {
					boolean rejected=false;
					String msg = "没有更多了";
					if(++prvNxtABC.沃壳积>=md.size()) {
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
							CMN.Log("collide");
						} else { // snack and return
							rejected=true;
						}
					}
					if(rejected) {
						prvNxtABC.dump();
						ViewGroup sv = getContentviewSnackHolder();
						if(thisActType==ActType.PlainDict && !isContentViewAttached()) {
							sv = main_succinct;
						}
						showTopSnack(sv, msg, 0.75f, -1, Gravity.CENTER, 0);
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
			currentDictionary = md_get(adapter_idx=i);
			bShowLoadErr=bShowErr;
			
			if (adaptermy!=null) {
				adaptermy.setPresenter(currentDictionary);
				if (invalidate) {
					adaptermy.notifyDataSetChanged();
					postPutName(550);
					if (currentDictionary != EmptyBook) {
						if (/*!isCombinedSearching && */(opt.getPicDictAutoSer()||this instanceof FloatSearchActivity)) {
							CMN.Log("auto_search!......");
							lv_matched=false;
							if(prvNxt && opt.getDimScrollbarForPrvNxt()) {
								ViewUtils.dimScrollbar(lv, false);
							}
							tw1.onTextChanged(etSearch.getText(), -1, -1, -100);
							//lv.setFastScrollEnabled(true);
							if(prvNxt && opt.getPrvNxtDictSkipNoMatch()) {
								return lv_matched;
							}
						} else {
							//lv.setSelection(currentDictionary.lvPos);
							lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
						}
					} else if(prvNxt) {
						return false;
					}
				}
			}
			
			boolean showBuildIndex=false;
			if(currentDictionary==EmptyBook && EmptyBook.placeHolder instanceof PlaceHolder) {
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
	WeakReference<BuildIndexInterface> buildIndexPane = CMN.EmptyRef;
	
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
			double rootHeight = root.getHeight();
			if(rootHeight==0) rootHeight=dm.heightPixels*5.8/6;
			buildIndex.buildIndexLayout.getLayoutParams().height = (int) (rootHeight*4.5/6);
			buildIndex.notifyDataSetChanged();
			//buildIndex.buildIndexLayout.getLayoutParams().height = (int) Math.max(root.getHeight()*4.5/6, GlobalOptions.density*50);
		} else if(buildIndex!=null) {
			ViewUtils.removeIfParentBeOrNotBe(buildIndex.buildIndexLayout, lvHeaderView, true);
		}
	}
	
	public void switchToSearchModeDelta(int i) {
	
	}
	
	public int md_getSize(){
		return md.size();
	}
	
	@NonNull public BookPresenter md_get(int i) {
		BookPresenter ret = null;
		PlaceHolder phTmp = null;
		try {
			ret = md.get(i);
			if(ret==null) {
				ArrayList<PlaceHolder> CosyChair = getLazyCC();
				if(i<CosyChair.size()) {
					phTmp = CosyChair.get(i);
					if (phTmp != null) {
						try {
							md.set(i, ret = new_book(phTmp, this));
						} catch (Exception e) {
							if(GlobalOptions.debug) CMN.Log(e);
							if (bShowLoadErr && isOnMainThread()) {
								phTmp.ErrorMsg = e.getLocalizedMessage();
								if(!phTmp.NeedsBuildIndex())
									show(R.string.err);
								//else showT("需要建立索引！");
							}
						}
					}
				}
			}
		} catch (Exception e) { CMN.Log(e); }
		if(ret==null) {
			ret = EmptyBook;
			EmptyBook.placeHolder = phTmp;
		}
		return ret;
	}

	public String md_getName(int i) {
		if(i>=0 && i<md.size()) {
			String name = null;
			BookPresenter mdTmp = md.get(i);
			if(mdTmp!=null) {
				name = mdTmp.getPath();
				if (name.startsWith(AssetTag)) name = CMN.getAssetName(name);
				else name = mdTmp.getDictionaryName();
			} else {
				ArrayList<PlaceHolder> CosyChair = getLazyCC();
				if(i<CosyChair.size()){
					PlaceHolder placeHolder = CosyChair.get(i);
					if(placeHolder!=null) {
						name = placeHolder.pathname;
						if (name.startsWith(AssetTag)) name = CMN.getAssetName(name);
						else name = placeHolder.getName().toString();
					}
				}
			}
			if(name!=null) {
				return name;
			}
		}
		return "Error!!!";
	}
	
	public void md_set_StarLevel(int i, int val) {
		long flag=0;
		if(i>=0 && i<md.size()){
			BookPresenter mdTmp = md.get(i);
			if(mdTmp!=null) {
				flag = mdTmp.getFirstFlag();
				mdTmp.setFirstFlag(flag=PDICMainAppOptions.setDFFStarLevel(flag, val));
				mdTmp.saveStates(this, prepareHistoryCon());
			} else {
				ArrayList<PlaceHolder> CosyChair = getLazyCC();
				if(i<CosyChair.size()) {
					PlaceHolder placeHolder = CosyChair.get(i);
					if(placeHolder!=null) {
						CharSequence name = placeHolder.getName();
						flag =  md_get_firstFlag_internal(name);
						flag = PDICMainAppOptions.setDFFStarLevel(flag, val);
						md_set_firstFlag_internal(name, flag);
					}
				}
			}
			CMN.Log("新的星级", md_getName(i), PDICMainAppOptions.getDFFStarLevel(flag), val);
		}
	}
	
	
	public int md_get_StarLevel(int i) {
		long flag = 0;
		if(i>=0 && i<md.size()){
			BookPresenter mdTmp = md.get(i);
			if(mdTmp!=null) {
				flag =  mdTmp.getFirstFlag();
			} else {
				ArrayList<PlaceHolder> CosyChair = getLazyCC();
				if(i<CosyChair.size()) {
					PlaceHolder placeHolder = CosyChair.get(i);
					if(placeHolder!=null) {
						flag =  md_get_firstFlag_internal(placeHolder.getSoftName());
					}
				}
			}
		}
		return PDICMainAppOptions.getDFFStarLevel(flag);
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
	
	@SuppressWarnings("All")
	public CharSequence md_getAbout_Trim(int i) {
		ArrayList<PlaceHolder> placeHolders = getLazyCC();
		PlaceHolder phTmp = placeHolders.get(i);
		BookPresenter presenter = md.get(i);
		String msg = phTmp.ErrorMsg;
		boolean show_info_extra = true||msg!=null;
		boolean show_info_codec = true;
		boolean show_info_reload = true;
		boolean show_info_path = true;
		SpannableStringBuilder sb=null;
		if(show_info_extra) {
			sb = new SpannableStringBuilder();
			if(show_info_reload) {
				sb.append("[重新加载");
			}
			if(show_info_codec && presenter!=null) {
				if(!show_info_reload) {
					sb.append("编码");
				}
				sb.append("：");
				sb.append(String.valueOf(presenter.getCharsetName()));
			}
			if(show_info_reload) {
				sb.append("]");
			}
			sb.append("\n");
			if(show_info_reload) {
				sb.setSpan(new ClickableSpan() {
					@Override public void onClick(@NonNull View widget) {
						reload_dict_at(i);
						showAboutDictDialogAt(i);
					}
				}, 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		
		CharSequence ret=presenter==null? "未加载"
				:HtmlCompat.fromHtml(presenter.getAboutString(), HtmlCompat.FROM_HTML_MODE_COMPACT);
		int len = ret.length();
		int st = 0;
		while((st < len) && ret.charAt(st) <= ' '){
			st++;
		}
		while ((st < len) && (ret.charAt(len - 1) <= ' ')) {
			len--;
		}
		
		if(show_info_extra) {
			sb.append(ret, st, len);
			ret=sb;
			if(msg!=null) {
				sb.append("\n\n").append("错误信息：");
				sb.append(msg);
			}
			if(presenter!=null && presenter.bookImpl.hasMdd()) {
				sb.append("\n\n").append("资源文件：");
				sb.append(presenter.bookImpl.getResourcePaths());
			}
			if(show_info_path) {
				sb.append("\n");
				sb.append("路径").append("：");
				st = sb.length();
				sb.append(phTmp.pathname);
				sb.setSpan(new ClickableSpan() {
					@Override public void onClick(@NonNull View widget) {
						showT("路径!");
					}
				}, st, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		} else {
			ret=ret.subSequence(st, len);
		}
		return ret;
	}
	
	public Drawable md_getCover(int i) {
		BookPresenter mdTmp = md.get(i);
		if(mdTmp!=null) return mdTmp.cover;
		ArrayList<PlaceHolder> CosyChair = getLazyCC();
		if(i<CosyChair.size()) {}
		return null;
	}

	public String getSearchTerm(){
		return etSearch.getText().toString();
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		View v = getCurrentFocus();
		CMN.Log("-->onActionModeStarted", v);
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
			getUcc().setInvoker(null, null, tv, null);
			getUcc().onClick(tv);
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

			if(intent.hasExtra("ClipData") && getUcc().mWebView!=null){
				int finalIntentFalgs = intentFalgs;
				getUcc().mWebView.evaluateJavascript(WebViewmy.CollectHtml, word -> {
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
			CMN.Log(e);
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
		
		CMN.mid = Thread.currentThread().getId();
	    //CMN.Log("instanceCount", CMN.instanceCount);
		super.onCreate(savedInstanceState);
		if(shunt) return;
		CMN.instanceCount++;
		snackWorker = () -> {
			animationSnackOut=false;
			hdl.sendEmptyMessage(6657);
			hdl.removeMessages(6658);
			int height = topsnack.getHeight();
			if(height>0){
				if(topsnack.offset>0 || topsnack.offset<-height)
					topsnack.offset=-height;
				else
					topsnack.offset=Math.min(-height/3, topsnack.offset);
				topsnack.setTranslationY(topsnack.offset);
				topsnack.setVisibility(View.VISIBLE);
				hdl.animator=0.1f;
				hdl.animatorD=0.08f*height;
				hdl.sendEmptyMessage(6657);
			}
			//ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(topsnack, "translationY", -height, 0);
			//fadeInContents.start();
		};
		MainStringBuilder = new StringBuilder(40960);
		//WebView.setWebContentsDebuggingEnabled(PDICMainAppOptions.getEnableWebDebug());
		//ViewUtils.setWebDebug(this);
		if (BuildConfig.isDebug) {
			CMN.debug("mid", CMN.mid, getClass());
			CMN.debug("sdk", Build.VERSION.SDK_INT);
			CMN.debug("dens", GlobalOptions.density);
		}
		if (bShouldCheckApplicationValid) {
			try {
				System.loadLibrary("PDict");
				if (!testPakVal(getPackageName()))
					showT("请使用正版软件！");
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}

	public void onAudioPause() {
		CMN.Log("onAudioPause", !AutoBrowsePaused, opt.isAudioActuallyPlaying, mThenReadEntryCount, bThenReadContent);
		if(!AutoBrowsePaused && PDICMainAppOptions.getAutoBrowsingReadSomething()){
			if(true || opt.isAudioActuallyPlaying || background){
				if(mThenReadEntryCount>0){
					//CMN.Log(root.post(this::performReadEntry), "posting...");
					CMN.Log("posting...3322123");
					hdl.sendEmptyMessage(3322123);
				} else {
					opt.isAudioActuallyPlaying=false;
					CMN.Log("posting...3322124");
					hdl.sendEmptyMessage(3322124);
					//root.post(this::enqueueNextAutoReadProcess);
				}
			}
		} else {
			if(bThenReadContent){
				opt.isAudioActuallyPlaying=false;
				CMN.Log("posting...3322124");
				hdl.sendEmptyMessage(3322124);
			} else {
				opt.isAudioActuallyPlaying=false;
				transitAAdjustment();
			}
		}
	}

	public void onAudioPlay() {
		CMN.Log("onAudioPlay", forbidVolumeAjustmentsForTextRead, !AutoBrowsePaused && PDICMainAppOptions.getAutoBrowsingReadSomething());
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
	
	protected int getVisibleHeight() {
		return root.getChildAt(0).getHeight();
	}

	public void fix_dm_color() {
		//CMN.Log("fix_dm_color");
		boolean isDark = GlobalOptions.isDark;
		boolean nii=widget12.getTag(R.id.image)==null;
		ViewGroup[] holders = new ViewGroup[]{webSingleholder, webholder};
		for (ViewGroup hI : holders) {
			for (int i = 0; i < hI.getChildCount(); i++) {
				Object tag = hI.getChildAt(i).getTag();
				if(tag instanceof Integer){
					int selfAtIdx = (int) tag;
					if(selfAtIdx>=0&&selfAtIdx<md.size()) {
						BookPresenter mdTmp = md.get(selfAtIdx);
						WebViewmy wv = mdTmp.mWebView;
						if(wv!=null) {
							wv.evaluateJavascript(isDark ? DarkModeIncantation : DeDarkModeIncantation, null);
							mdTmp.tintBackground(wv);
						}
					}
				}
			}
		}
		if(peruseView !=null) {
			peruseView.refreshUIColors(MainBackground);
		}
	}

	public void fix_pw_color() {
		if(bottomPlaylist!=null){
			bottomPlaylist.clear();
			bottomPlaylist=null;
		}
		if(ChooseFavorDialog!=null){
			ChooseFavorDialog.clear();
			ChooseFavorDialog=null;
		}
		if(popupWebView!=null)
		if(GlobalOptions.isDark){
			popupContentView.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
			popupBottombar.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
			popIvBack.setImageResource(R.drawable.abc_ic_ab_white_material);
			popIvBack.setTag(false);
		} else if(popIvBack.getTag()!=null){
			popupContentView.getBackground().clearColorFilter();
			popupBottombar.getBackground().clearColorFilter();
			popIvBack.setImageResource(R.drawable.abc_ic_ab_back_material_simple_compat);
			popIvBack.setTag(null);
		}
	}

	public void popupWord(final String key, BookPresenter forceStartId, int frameAt) {
		CMN.Log("popupWord_frameAt", frameAt, key, md.size(), WebViewmy.supressNxtClickTranslator);
		if(key==null || mdict.processText(key).length()>0) {
			popupKey = key;
			popupFrame = frameAt;
			popupForceId = forceStartId;
			if(mPopupRunnable==null) {
				mPopupRunnable = new Runnable() {
					@Override
					public void run() {
						if(RLContainerSlider.lastZoomTime > 0){
							if (System.currentTimeMillis() - RLContainerSlider.lastZoomTime < 500){
								return;
							}
							RLContainerSlider.lastZoomTime=0;
						}
						boolean bPeruseViewAttached = PeruseViewAttached();
						//CMN.Log("\nmPopupRunnable run!!!");
						ViewGroup targetRoot = bPeruseViewAttached? peruseView.root:root;
						int size = md.size();
						if (size <= 0) return;
						//CMN.Log("popupWord", popupKey, x, y, frameAt);
						boolean isNewHolder;
						boolean isInit;
						// 初始化核心组件
						isInit = isNewHolder = popupWebView == null||popupWebView.fromCombined!=2;
						init_popup_view();
						// 给你换身衣裳
						WeakReference<ViewGroup> holder = (PDICMainAppOptions.getImmersiveClickSearch() ? popupCrdCloth : popupCmnCloth);
						ViewGroup mPopupContentView = popupContentView;
						popupContentView = holder == null ? null : holder.get();
						boolean b1 = popupContentView == null;
						isNewHolder = isNewHolder || b1;
						if (b1 || popupContentView instanceof LinearLayout ^ PopupPageSlider.getParent() instanceof LinearLayout) {
							if (popupToolbar.getParent() != null)
								((ViewGroup) popupToolbar.getParent()).removeView(popupToolbar);
							if (PopupPageSlider.getParent() != null)
								((ViewGroup) PopupPageSlider.getParent()).removeView(PopupPageSlider);
							if (popupBottombar.getParent() != null)
								((ViewGroup) popupBottombar.getParent()).removeView(popupBottombar);
							if (mPopupContentView != null && mPopupContentView.getParent() != null)
								((ViewGroup) mPopupContentView.getParent()).removeView(mPopupContentView);
							if (PDICMainAppOptions.getImmersiveClickSearch()) {
								popupContentView = (popupCrdCloth != null && popupCrdCloth.get() != null) ? popupCrdCloth.get()
										: (popupCrdCloth = new WeakReference<>((ViewGroup) getLayoutInflater()
										.inflate(R.layout.float_contentview_coord, root, false))).get();
								((ViewGroup) popupContentView.getChildAt(0)).addView(popupToolbar);
								popupContentView.addView(PopupPageSlider);
								popupContentView.addView(popupBottombar);
								((CoordinatorLayout.LayoutParams) popupBottombar.getLayoutParams()).gravity = Gravity.BOTTOM;
								((CoordinatorLayout.LayoutParams) popupBottombar.getLayoutParams()).setBehavior(new BottomNavigationBehavior(popupContentView.getContext(), null));
								((CoordinatorLayout.LayoutParams) PopupPageSlider.getLayoutParams()).setBehavior(new AppBarLayout.ScrollingViewBehavior(popupContentView.getContext(), null));
								((CoordinatorLayout.LayoutParams) PopupPageSlider.getLayoutParams()).height = LayoutParams.MATCH_PARENT;
								((AppBarLayout.LayoutParams) popupToolbar.getLayoutParams()).setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
							} else {
								popupContentView = (popupCmnCloth != null && popupCmnCloth.get() != null) ? popupCmnCloth.get()
										: (popupCmnCloth = new WeakReference<>((ViewGroup) getLayoutInflater()
										.inflate(R.layout.float_contentview_basic_outer, root, false))).get();
								popupContentView.addView(popupToolbar);
								popupContentView.addView(PopupPageSlider);
								popupContentView.addView(popupBottombar);
								popupToolbar.setTranslationY(0);
								PopupPageSlider.setTranslationY(0);
								popupBottombar.setTranslationY(0);
								((LinearLayout.LayoutParams) PopupPageSlider.getLayoutParams()).weight = 1;
								((LinearLayout.LayoutParams) PopupPageSlider.getLayoutParams()).height = 0;
							}
						}

						popupChecker.setChecked(PDICMainAppOptions.getClickSearchPin(), false);
						popupGuarder.popupToGuard = popupContentView;
						popupGuarder.setVisibility(View.VISIBLE);

						if (isNewHolder) {
							popupWebView.fromCombined = 2;
							fix_pw_color();
							popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
							FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) popupContentView.getLayoutParams());
							lp.height = popupMoveToucher.FVH_UNDOCKED = (int) (dm.heightPixels * 5.0 / 12 - getResources().getDimension(R.dimen._20_));
							if (mPopupContentView != null && !isInit) {
								popupContentView.setTranslationY(mPopupContentView.getTranslationY());
								lp.height = mPopupContentView.getLayoutParams().height;
							}
						}

						int idx = -1, cc = 0;
						if (popupKey != null) {
							popupTextView.setText(popupKey);
							String keykey;
							CCD_ID = currentClick_adapter_idx = Math.min(currentClick_adapter_idx, size-1);
							if(popupForceId!=null) {
								CCD = popupForceId;
								CCD_ID = md.indexOf(popupForceId);
								if(CCD_ID<0) {
									CCD_ID = md.size();
									md.add(popupForceId); // todo check???
								}
							}
							//轮询开始
							//nimp
							BookPresenter webx = null;
							boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
							int SearchMode = PDICMainAppOptions.getClickSearchMode();
							CMN.Log("SearchMode", SearchMode);
							boolean bForceJump = false;
							if (SearchMode == 2) {/* 仅搜索当前词典 */
								CCD = md_get(CCD_ID);
								if (CCD != EmptyBook) {
									if(CCD.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
										webx = CCD;
										if (!((PlainWeb)webx.bookImpl).takeWord(popupKey)) {
											webx = null;
										}
									} else  {
										idx = CCD.bookImpl.lookUp(popupKey, true);
										if (idx < -1 && use_morph) {
											keykey = ReRouteKey(popupKey, true);
											if (keykey != null) idx = CCD.bookImpl.lookUp(keykey, true);
										}
									}
								}
							}
							else {
								boolean proceed = true;
								if (SearchMode == 1) {/* 仅搜索指定点译词典 */
									bHasDedicatedSeachGroup=false;
									BookPresenter firstAttemp = null;
									FindCSD:
									while(true) {
										BookPresenter mdTmp;
										int CSID;
										for (int i = 0; i < md.size(); i++) {
											mdTmp = null;
											CSID = (i + CCD_ID) % md.size();
											ArrayList<PlaceHolder> CosyChair = getLazyCC();
											if (CSID < CosyChair.size()) {
												PlaceHolder phTmp = CosyChair.get(CSID);
												if (phTmp != null) {
													if (PDICMainAppOptions.getTmpIsClicker(phTmp.tmpIsFlag)) {
														mdTmp = md.get(CSID);
														if (mdTmp == null) {
															try {
																md.set(CSID, mdTmp = new_book(phTmp, MainActivityUIBase.this));
															} catch (Exception e) { }
														}
													}
												}
											}
											if (mdTmp != null) {
												if (!bForceJump && firstAttemp == null)
													firstAttemp = mdTmp;
												bHasDedicatedSeachGroup=true;
												proceed=false;
												if(mdTmp.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
													webx = mdTmp;
													if (bForceJump || ((PlainWeb)webx.bookImpl).takeWord(popupKey)) {
														break;
													}
													webx = null;
												}
												else
												{
													idx = mdTmp.bookImpl.lookUp(popupKey, true);
													if (idx < -1 && use_morph) {
														keykey = ReRouteKey(popupKey, true);
														if (keykey != null)
															idx = mdTmp.bookImpl.lookUp(keykey, true);
													}
													if(idx<0 && bForceJump){
														idx = -1-idx;
													}
													if (idx >= 0) {
														CCD_ID = (i + CCD_ID) % md.size();
														CCD = mdTmp;
														break FindCSD;
													}
													if(bForceJump){
														break FindCSD;
													}
												}
											}
										}
										if (firstAttemp != null && md.size()>0) {
											bForceJump=true;
											firstAttemp=null;
										} else {
											break;
										}
									}

								}
								boolean reject_morph = false;
								if (proceed)/* 未指定点译词典 */
									while (true) {
										if (cc > md.size())
											break;
										CCD_ID = CCD_ID % md.size();
										CCD = md.get(CCD_ID);
										if (CCD == null) {
											ArrayList<PlaceHolder> CosyChair = getLazyCC();
											if (CCD_ID < CosyChair.size()) {
												PlaceHolder phTmp = CosyChair.get(CCD_ID);
												if (phTmp != null) {
													try {
														md.set(CCD_ID, CCD = new_book(phTmp, MainActivityUIBase.this));
													} catch (Exception e) {
														CMN.Log(e);
													}
												}
											}
										}
										if (CCD == null) {
											CCD = EmptyBook;
										}
										if(CCD.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
											webx = CCD;
											if (((PlainWeb)webx.bookImpl).takeWord(popupKey)) {
												break;
											}
											webx = null;
										} else
										if (CCD != EmptyBook) {
											idx = CCD.bookImpl.lookUp(popupKey, true);
											if (idx < 0) {
												if (!reject_morph && use_morph) {
													keykey = ReRouteKey(popupKey, true);
													if (keykey != null)
														idx = CCD.bookImpl.lookUp(keykey, true);
													else
														reject_morph = true;
												}
											}
											if (idx >= 0)
												break;
										}
										CCD_ID++;
										cc++;
									}
							}
							popupIndicator.setText(md_getName(CCD_ID));

							if (webx != null) {
								webx.SetSearchKey(popupKey);
								idx = 0;
							}

							//CMN.Log(CCD, "应用轮询结果", webx, idx);

							if (idx >= 0 && CCD != EmptyBook) {
								if(bForceJump && SearchMode==1)
									popupWebView.setTag(R.id.js_no_match, false);
								popupHistory.add(++popupHistoryVagranter, new myCpr<>(popupKey, new int[]{CCD_ID, idx}));
								if (popupHistory.size() > popupHistoryVagranter + 1) {
									popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
								}
								popuphandler.setDict(CCD);
								if (PDICMainAppOptions.getClickSearchAutoReadEntry())
									popupWebView.bRequestedSoundPlayback=true;
								popupWebView.IBC = CCD.IBC;
								PopupPageSlider.invalidateIBC();
								CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, popupWebView, currentClickDictionary_currentPos = idx);
							}

							currentClickDisplaying = popupKey;
							decorateContentviewByKey(popupStar, currentClickDisplaying);
							if (!PDICMainAppOptions.getHistoryStrategy0()
									&& PDICMainAppOptions.getHistoryStrategy7())
								insertUpdate_histroy(popupKey, 0, PopupPageSlider);
						}

						// 初次添加请指明方位
						ViewGroup svp = (ViewGroup) popupContentView.getParent();
						if (svp != targetRoot) {
							if(svp!=null){
								svp.removeView(popupContentView);
							}
							if (popupMoveToucher.FVDOCKED && popupMoveToucher.Maximized && PDICMainAppOptions.getResetMaxClickSearch()) {
								popupMoveToucher.Dedock();
							}
							CMN.Log("poping up ::: ", ActivedAdapter);
							if (popupKey!=null && (PDICMainAppOptions.getResetPosClickSearch() || isInit) && !popupMoveToucher.FVDOCKED) {
								float ty = 0;
								float now = 0;
								if (ActivedAdapter != null || popupFrame<0) {
									//CMN.Log("???", y, targetRoot.getHeight()-popupGuarder.getResources().getDimension(R.dimen.halfpopheader));
									if(popupFrame==-1){
										now = mActionModeHeight;
										CMN.Log(now, targetRoot.getHeight() / 2);
									}
									else if(bPeruseViewAttached){
										now = peruseView.getWebTouchY();
									}
									else if (ActivedAdapter == adaptermy || ActivedAdapter == adaptermy3 || ActivedAdapter == adaptermy4) {
										if (webSingleholder.getChildAt(0) instanceof LinearLayout) {
											LinearLayout sv = (LinearLayout) webSingleholder.getChildAt(0);
											WebViewmy mWebView = sv.findViewById(R.id.webviewmy);
											if (mWebView != null) {
												now = mWebView.lastY;
											}
											//now -= sv.getChildAt(1).getScrollY();
											now += sv.getChildAt(0).getHeight();
											//CMN.Log("now",sv.getChildAt(0).getHeight(), ((ViewGroup.MarginLayoutParams) getContentviewSnackHolder().getLayoutParams()).topMargin);
										}
									}
									else if (ActivedAdapter == adaptermy2) {
										if (webholder.getChildAt(popupFrame) instanceof LinearLayout) {
											LinearLayout sv = (LinearLayout) webholder.getChildAt(popupFrame);
											WebViewmy mWebView = sv.findViewById(R.id.webviewmy);
											now = sv.getTop() + mWebView.lastY + sv.getChildAt(0).getHeight() - WHP.getScrollY();
										}
									}
									if(thisActType!=ActType.MultiShare) {
										if(PDICMainAppOptions.getEnableSuperImmersiveScrollMode()){
											now += webcontentlist.getTop();
										} else {
											now += ((ViewGroup.MarginLayoutParams) getContentviewSnackHolder().getLayoutParams()).topMargin;
										}
									}
									float pad = 56 * dm.density;
									if (MainActivityUIBase.this instanceof FloatSearchActivity)
										now += ((FloatSearchActivity) MainActivityUIBase.this).getPadHoldingCS();
									CMN.Log("now",now);
									if (now < targetRoot.getHeight() / 2) {
										ty = now + pad;
									} else {
										ty = now - popupMoveToucher.FVH_UNDOCKED - pad;
									}
								}
								//CMN.Log("min", getVisibleHeight()-popupMoveToucher.FVH_UNDOCKED-((ViewGroup.MarginLayoutParams)popupContentView.getLayoutParams()).topMargin*2);
								popupContentView.setTranslationY(Math.min(getVisibleHeight() - popupMoveToucher.FVH_UNDOCKED - ((ViewGroup.MarginLayoutParams) popupContentView.getLayoutParams()).topMargin * 2, Math.max(0, ty)));
							}
							svp = (ViewGroup) popupGuarder.getParent();
							if(svp!=targetRoot){
								if(svp!=null) svp.removeView(popupGuarder);
								targetRoot.addView(popupGuarder, new FrameLayout.LayoutParams(-1, -1));
							}
							//if(idx>=0){
							targetRoot.addView(popupContentView);
							CMN.Log("111", targetRoot, popupContentView.getParent());
							fix_full_screen(null);
							//}
						}
						//else popupWebView.loadUrl("about:blank");
						//CMN.recurseLog(popupContentView, null);
					}
				};
			}
			root.removeCallbacks(mPopupRunnable);
			//root.post(mPopupRunnable);
			root.postDelayed(mPopupRunnable, 75);
		}
	}
	
	protected void init_popup_view() {
		if (popupWebView == null) {
			popupContentView = (ViewGroup) getLayoutInflater()
					.inflate(R.layout.float_contentview_basic, root, false);
			popupContentView.setOnClickListener(ViewUtils.DummyOnClick);
			popupToolbar = (ViewGroup) popupContentView.getChildAt(0);
			PopupPageSlider = (RLContainerSlider) popupContentView.getChildAt(1);
			WebViewmy mPopupWebView = (WebViewmy) PopupPageSlider.getChildAt(0);
			mPopupWebView.fromCombined = 2;
			PopupPageSlider.WebContext = mPopupWebView;
			popupBottombar = (ViewGroup) popupContentView.getChildAt(2);
			popuphandler = new BookPresenter.AppHandler(currentDictionary);
			mPopupWebView.addJavascriptInterface(popuphandler, "app");
			mPopupWebView.setBackgroundColor(Color.TRANSPARENT);
			((AdvancedNestScrollWebView)mPopupWebView).setNestedScrollingEnabled(true);
			popCover = PopupPageSlider.getChildAt(1);
			popCover.setOnClickListener(MainActivityUIBase.this);
			popIvBack = popupToolbar.findViewById(R.id.popIvBack);
			popIvBack.setOnClickListener(MainActivityUIBase.this);
			popupStar = popupToolbar.findViewById(R.id.popIvStar);
			popupStar.setOnClickListener(MainActivityUIBase.this);
			popupBottombar.findViewById(R.id.popIvRecess).setOnClickListener(MainActivityUIBase.this);
			popupBottombar.findViewById(R.id.popIvForward).setOnClickListener(MainActivityUIBase.this);
			popupBottombar.findViewById(R.id.popIvSettings).setOnClickListener(MainActivityUIBase.this);
			popupChecker = popupBottombar.findViewById(R.id.popChecker);
			popupChecker.setOnClickListener(MainActivityUIBase.this);
			popupTextView = popupToolbar.findViewById(R.id.popupText1);
			mPopupWebView.IBC = new PhotoBrowsingContext();
			mPopupWebView.toolbar_title = popupIndicator = popupBottombar.findViewById(R.id.popupText2);
			popupTextView.setOnClickListener(MainActivityUIBase.this);
			View popupNxtD, popupLstD;
			(popupNxtD = popupToolbar.findViewById(R.id.popNxtDict)).setOnClickListener(MainActivityUIBase.this);
			(popupLstD = popupToolbar.findViewById(R.id.popLstDict)).setOnClickListener(MainActivityUIBase.this);
			popupBottombar.findViewById(R.id.popNxtE).setOnClickListener(MainActivityUIBase.this);
			popupBottombar.findViewById(R.id.popLstE).setOnClickListener(MainActivityUIBase.this);
			popupIndicator.setOnClickListener(MainActivityUIBase.this);
			if(GlobalOptions.isDark) {
				popupTextView.setTextColor(Color.WHITE);
				popupIndicator.setTextColor(Color.WHITE);
			}
			
			// 点击背景
			popupGuarder = new PopupGuarder(getBaseContext());
			if(thisActType==ActType.MultiShare) {
				popupGuarder.onPopupDissmissed = this;
			}
			popupGuarder.setId(R.id.popupBackground);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				popupGuarder.setElevation(5 * dm.density);
			}
			//popupGuarder.setBackgroundColor(Color.BLUE);
			root.addView(popupGuarder, new FrameLayout.LayoutParams(-1, -1));
			// 弹窗搜索移动逻辑， 类似于浮动搜索。
			popupTextView.setOnTouchListener(popupMoveToucher = new PopupMoveToucher(MainActivityUIBase.this, popupTextView));
			popupNxtD.setOnTouchListener(popupMoveToucher);
			popupLstD.setOnTouchListener(popupMoveToucher);
			popupStar.setOnTouchListener(popupMoveToucher);
			// 缩放逻辑
			popupWebView = mPopupWebView;
		}
		if (GlobalOptions.isDark) {
			popupChecker.drawInnerForEmptyState = true;
			popupChecker.circle_shrinkage = 0;
		}
		else {
			popupChecker.drawInnerForEmptyState = false;
			popupChecker.circle_shrinkage = 2;
		}
	}
	
	public boolean DetachClickTranslator() {
		if(popupContentView!=null) {
			ViewGroup svp = (ViewGroup) popupContentView.getParent();
			if(svp!=null) {
				svp.removeView(popupContentView);
				popupContentView = null;
				popupGuarder.setVisibility(View.GONE);
				return true;
			}
		}
		return false;
	}

	public void postDetachClickTranslator() {
		root.post(() -> DetachClickTranslator());
	}

	public @Nullable String ReRouteKey(String key, boolean bNullable){
		int size = currentFilter.size();
		if (size>0) {
			//CMN.Log("ReRouteKey ??" , key);
			for (int i = 0; i < size; i++) {
				BookPresenter mdTmp = currentFilter.get(i);
				if(mdTmp==null){
					ArrayList<PlaceHolder> CosySofa = getLazyCS();
					if(i<CosySofa.size()){
						PlaceHolder phI = CosySofa.get(i);
						try {
							currentFilter.set(i, mdTmp= new_book(phI, this));
							mdTmp.tmpIsFlag=phI.tmpIsFlag;
						} catch (Exception e) { CMN.Log(e); }
					}
				}
				if(mdTmp!=null)
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
		webcontentlist.canClickThrough=true;
		checkW10(PDICMainAppOptions.getIsoImgClickThrough());
	}

	public void guaranteeBackground(int globalPageBackground) {
		webcontentlist.canClickThrough=false;
		if(webSingleholder.getTag(R.id.image)!=null || globalPageBackground!=GlobalPageBackground){
			webSingleholder.setTag(R.id.image,null);
			webSingleholder.setBackgroundColor(globalPageBackground);
		}
		if(widget12.getTag(R.id.image)!=null){
			widget12.setTag(R.id.image, null);
			widget12.setImageResource(R.drawable.voice_ic);
		}
	}

	void toggleClickThrough() {
		checkW10(PDICMainAppOptions.setIsoImgClickThrough(!PDICMainAppOptions.getIsoImgClickThrough()));
	}

	private void checkW10(boolean clickThrough) {
		Integer target = clickThrough?R.drawable.ic_image_black_24dp:R.drawable.ic_comment_black_24dp;
		if(widget12.getTag(R.id.image)!=target){
			widget12.setTag(R.id.image, target);
			widget12.setImageResource(target);
		}
	}

	public void checkW10_full() {
		Integer target = R.drawable.ic_fullscreen_black_96dp;
		if(widget12.getTag(R.id.image)!=target){
			widget12.setTag(R.id.image, target);
			widget12.setImageResource(target);
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
		if(WHP.getVisibility()==View.VISIBLE) {
			if(webholder.getChildCount()!=0)
				webholder.removeAllViews();
			WHP.setVisibility(View.GONE);
		}
		if(widget14.getVisibility()==View.VISIBLE) {
			widget13.setVisibility(View.GONE);
			widget14.setVisibility(View.GONE);
		}
	}

	public abstract PlaceHolder getPlaceHolderAt(int idx);

	public abstract ArrayList<PlaceHolder> getPlaceHolders();

	/**  0=在右; 1=在左; 2=无; 3=系统滚动条  */
	public void RecalibrateWebScrollbar(WebViewmy mWebView){
		int vis = View.VISIBLE;
		boolean vsi = false;
		int gt = 0;
		int type = mWebView!=null?0:2;
		if(this instanceof FloatSearchActivity)
			type+=4;
		switch (opt.getTypeFlag_11_AtQF(type)){
			case 0:
				gt=Gravity.END;
			break;
			case 1:
				gt=Gravity.START;
			break;
			case 2:
				vis=View.GONE;
			break;
			case 3:
				vis=View.GONE;
				vsi=true;
			break;
		}
		if(mBar.getVisibility()!=vis)
			mBar.setVisibility(vis);
		if(gt!=0 && mBar_layoutParmas.gravity!=gt){
			mBar_layoutParmas.gravity=gt;
			mBar.requestLayout();
		}
		if(mWebView!=null) {
			mWebView.setVerticalScrollBarEnabled(vsi);
			if(vis==View.VISIBLE)
				mBar.setDelimiter("< >", mWebView);
		}else{
			WHP.setVerticalScrollBarEnabled(vsi);
			if(vis==View.VISIBLE)
				initWebHolderScrollChanged();
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
		CMN.Log("发起 startAutoReadProcess");
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
				contentviewAddView(mAutoReadProgressView, 0);
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
		CMN.Log("自动读下一个", opt.isAudioActuallyPlaying, bRequestingAutoReading, AutoBrowsePaused);
		long now = System.currentTimeMillis();
		if(now-lastreadtime<AutoBrowseTimeOut){
			CMN.Log("abuse detected!!!!");
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
		CMN.Log("enqueueNextAutoReadProcess");
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
						CMN.Log("如果能重来");
						startAutoReadProcess();
					} else {
						CMN.Log("2");
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
	public void ensureTSHanziSheet(PDICMainActivity.AdvancedSearchLogicLayer SearchLayer) {
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
			} catch (IOException e) { CMN.Log(e); }
		}
		if(jnFanMap==null) {
			PDICMainAppOptions.setEnableFanjnConversion(false);
		} else {
			SearchLayer.jnFanMap = jnFanMap;
			SearchLayer.fanJnMap = fanJnMap;
		}
	}

	void closeIfNoActionView(MenuItemImpl mi) {
		if(mi!=null && !mi.isActionButton()) AllMenus.close();
	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
	
	@Override
	protected void scanSettings() {
		CMN.GlobalPageBackground = GlobalPageBackground = opt.getGlobalPageBackground();
		mdict.bGlobalUseClassicalKeycase = PDICMainAppOptions.getClassicalKeycaseStrategy();
		opt.getLastPlanName(LastPlanName);
		new File(opt.pathToDatabases().toString()).mkdirs();
		opt.CheckFileToDefaultMdlibs();
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
	public boolean bShouldCheckApplicationValid = true;
	
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
	
	public native boolean testPakVal(String pakNam);
	
	public native int getPseudoCode(int sigHash);
	
	
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
	
	
	@Override
	protected void further_loading(Bundle savedInstanceState) {
		super.further_loading(savedInstanceState);
		VersionUtils.checkVersion(opt);
		opt.fileToDatabases();
		BookPresenter.def_zoom=dm.density;
		BookPresenter.optimal100 = GlobalOptions.isLarge?150:125;
		BookPresenter.def_fontsize = opt.getDefaultFontScale(BookPresenter.optimal100);
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
			currentDictionary = EmptyBook = new BookPresenter(new File("empty"), this, 0, null);
		} catch (IOException ignored) { }
		
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
							CMN.Log(e);
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

		File SpecificationFile =  opt.SpecificationFile = new File(opt.pathToDatabases().append("/.spec.bin").toString());
		CMN.rt();
		CMN.Log(CMN.LastConfigReadTime, SpecificationFile.lastModified());
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
												CMN.Log("配置大小 =", mConfigSize);
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
					CMN.Log(e);
				}
				if (mConfigSize < 50) {
					SpecificationFile.delete();
				}
			}
		}
		CMN.pt(app.BookProjects.size()+"个配置读取时间");
		
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
				View IMPageCover_ = getIMPageCover();
				LayoutParams lpp = IMPageCover_.getLayoutParams();
				if(PeruseViewAttached())
					IMPageCover_ = peruseView.IMPageCover;
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
				View IMPageCover_ = getIMPageCover();
				View PageSlider_ = PageSlider;
				if(PeruseViewAttached()) {
					IMPageCover_= peruseView.IMPageCover;
					PageSlider_= peruseView.PageSlider;
				}
				IMPageCover_.setVisibility(View.VISIBLE);
				if(!webcontentlist.decided)
					IMPageCover_.setTranslationY(val);
				else {//贴底
					IMPageCover_.setTranslationY(webcontentlist.multiplier==-1?0:PageSlider_.getHeight()-height);
				}
			}
			
			@Override
			public void onPageTurn(SplitView webcontentlist) {
				//showT("onPageTurn"+System.currentTimeMillis());
				View IMPageCover_ = getIMPageCover();
				boolean bPeruseIncharge = PeruseViewAttached() && (peruseView.contentview.getParent()== peruseView.slp || peruseView.contentview.getParent()== peruseView.mlp);
				if(PeruseViewAttached())
					IMPageCover_= peruseView.IMPageCover;
				IMPageCover_.setVisibility(View.GONE);
				
				if(bPeruseIncharge)
					opt.setPeruseBottombarOnBottom(webcontentlist.getChildAt(0).getId()!=R.id.bottombar2);
				else
					opt.setBottombarOnBottom(webcontentlist.getChildAt(0).getId()!=R.id.bottombar2);
				
				if(opt.getNavigationBtnType()==2)
					locateNaviIcon(widget13,widget14);
			}
			
			@Override
			public void onHesitate() {
				//showT("onHesitate"+System.currentTimeMillis());
				IMPageSlider IMPageCover_ = IMPageCover;
				if(peruseView !=null && peruseView.getView()!=null && peruseView.getView().getParent()!=null)
					IMPageCover_= peruseView.IMPageCover;
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
				boolean bPeruseIncahrge = PeruseViewAttached() && (peruseView.contentview.getParent()== peruseView.slp || peruseView.contentview.getParent()== peruseView.mlp);
				int ret = (int) Math.max(((bPeruseIncahrge&&!opt.getPeruseBottombarOnBottom())?30:20)*dm.density, Math.min(getResources().getDimension(R.dimen._bottombarheight_), size));//50*dm.density
				CMN.Log(ret);
				if(bPeruseIncahrge) {
					peruseView.CachedBBSize = ret;
				}else{
					CachedBBSize = ret;
					webcontentlist.isDirty=true;
				}
				return ret;
			}
		};
		
		if(thisActType==ActType.MultiShare) {
			return;
		}
		
		ArrayList<PlaceHolder> CC = getLazyCC();
		
		if(md.size()==0){
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
							BookPresenter mdtmp = new BookPresenter(i, this, 0, null);
							md.add(mdtmp);
							CC.add(mdtmp.placeHolder=new PlaceHolder(i.getName(), CC));
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
		
		switch_To_Dict_Idx(adapter_idx, false, false, null);
		
		findFurtherViews();
		
		toolbar.setOnMenuItemClickListener(this);
		ivDeleteText.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		if(isCombinedSearching) {
			AllMenus.findItem(R.id.toolbar_action1).setIcon(R.drawable.ic_btn_multimode);
		}
		//if(opt.isShowDirectSearch()) ((MenuItem)toolbar.getMenu().findItem(R.id.toolbar_action2)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	@CallSuper
	protected void findFurtherViews() {
		if (webSingleholder==null) {
			contentview = contentUIData.webcontentlister;
			webcontentlist = contentUIData.webcontentlister;
			PageSlider = contentUIData.PageSlider;
			bottombar2 = contentUIData.bottombar2;
			
			webSingleholder = contentUIData.webSingleholder;
			WHP = contentUIData.WHP;
			webholder = contentUIData.webholder;
			IMPageCover = contentUIData.cover;
			mBar = contentUIData.dragScrollBar;
			(widget13=contentUIData.browserWidget13).setOnClickListener(this);
			(widget14=contentUIData.browserWidget14).setOnClickListener(this);
		}
		
		//CMN.Log("findFurtherViews...", webholder);
		
		mBar_layoutParmas = (FrameLayout.LayoutParams) mBar.getLayoutParams();
		mBar.setOnProgressChangedListener(_mProgress -> {
			if(PageSlider==null) return;
			PageSlider.TurnPageSuppressed = _mProgress==-1;
		});
		
		webcontentlist.multiplier=-1;
		webcontentlist.isSlik=true;
		
		CachedBBSize=(int)Math.max(20*dm.density, Math.min(CachedBBSize, getResources().getDimension(R.dimen._bottombarheight_)));
		webcontentlist.setPrimaryContentSize(CachedBBSize,true);
		
		webcontentlist.setPageSliderInf(inf);
		
		bottombar2.setBackgroundColor(MainBackground);
		
		boolean tint = PDICMainAppOptions.getTintIconForeground();
		for (int i = 0; i <= 5; i++) {
			ImageView iv = (ImageView) bottombar2.getChildAt(i);
			ContentbarBtns[i]=iv;
			iv.setOnClickListener(this);
			if(tint) iv.setColorFilter(ForegroundTint, PorterDuff.Mode.SRC_IN);
			iv.setOnLongClickListener(this);
		}
		widget7=ContentbarBtns[0];
		favoriteBtn=ContentbarBtns[1];
		widget10=ContentbarBtns[3];
		widget11=ContentbarBtns[4];
		widget12=ContentbarBtns[5];
		String contentkey = "ctnp#"+cbar_key;
		String appproject = opt.getAppContentBarProject(contentkey);
		if(appproject==null) appproject="0|1|2|3|4|5";
		contentbar_project = new AppUIProject(contentkey, ContentbarBtnIcons, appproject, bottombar2, ContentbarBtns);
		contentbar_project.type = cbar_key;
		RebuildBottombarIcons(this, contentbar_project, mConfiguration);
		
		
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		actionBarSize = TypedValue.complexToDimensionPixelSize(typedValue.data, dm);
		if(actionBarSize<=0) actionBarSize=(int) (56*dm.density);
	}
	
	protected void populateDictionaryList() {
		final File def = getStartupFile(opt.fileToConfig());      //!!!原配
		if(md.size()==0){
			populateDictionaryList(def, getLazyCC(), !def.exists());
		}
	}
	
	final String[] defDicts = new String[]{
			CMN.AssetTag + "liba.mdx"
			,"/ASSET2/谷歌翻译.web"
	};
	
	protected void populateDictionaryList(File def, ArrayList<PlaceHolder> CC, boolean retrieve_all) {
		BookPresenter book = null;
		try {
			book = new_book(defDicts[1], this);
		} catch (Exception ignored) {  }
		if(retrieve_all) {
			try {
				if(CC==null) {
					CC = new ArrayList<>();
					if(this instanceof FloatSearchActivity) FloatSearchActivity.mCosyChair = CC;
					if(this instanceof PDICMainActivity) PDICMainActivity.CosyChair = CC;
				}
				String lastName = opt.getLastMdFn("LastMdFn");
				for (String path:defDicts) {
					PlaceHolder placeHolder = new PlaceHolder(path, CC);
					CC.add(placeHolder);
					if(TextUtils.equals(new File(path).getName(), lastName)) {
						adapter_idx = md.size();
					}
					md.add(book=new_book(placeHolder, this));
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		else try {
			boolean lazyLoad = opt.getLazyLoadDicts();
			LoadLazySlots(def, lazyLoad, opt.getLastPlanName(LastPlanName));
			buildUpDictionaryList(lazyLoad, null);
		} catch (Exception e) { CMN.Log(e); }
	}
	
	protected View getIMPageCover() {
		return IMPageCover;
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
		return def;
	}

	void buildUpDictionaryList(boolean lazyLoad, HashMap<String, BookPresenter> mdict_cache) {
		boolean actMain = thisActType==ActType.PlainDict;;
		ArrayList<PlaceHolder> CosyChair =  actMain?PDICMainActivity.CosyChair:getLazyCC();
		ArrayList<PlaceHolder> CosySofa =  actMain?PDICMainActivity.CosySofa:getLazyCS();
		ArrayList<PlaceHolder> HdnCmfrt =  actMain?PDICMainActivity.HdnCmfrt:getLazyHC();
		currentFilter.ensureCapacity(filter_count);
		currentFilter.clear();
		md.ensureCapacity(CosyChair.size());
		md.clear();
		adapter_idx=-1;
		PlaceHolder phI;
		String lastName = opt.getLastMdFn("LastMdFn");
		for (int i = 0; i < CosyChair.size(); i++) {
			phI = CosyChair.get(i);
			//get path put
			BookPresenter mdTmp = mdict_cache==null?null:mdict_cache.get(phI.getPath(opt).getName());
			if ((phI.tmpIsFlag&0x8)!=0){
				HdnCmfrt.add(phI); /* 隐·1 */
				CosyChair.remove(i--);
				continue;
			}
			if(mdTmp==null && !lazyLoad) { // 大家看 这里有个老实人
				try {
					mdTmp = new_book(phI, this);
				} catch (Exception e) {
					phI.tmpIsFlag|=0x8;
					HdnCmfrt.add(phI); /* 兵解轮回 */
					CosyChair.remove(i--);
					CMN.Log(e);
					if (trialCount == -1) if (bShowLoadErr)
						show(R.string.err, phI.getName(), phI.pathname, e.getLocalizedMessage());
					continue;
				}
			}
			if(mdTmp!=null)
				mdTmp.tmpIsFlag = phI.tmpIsFlag;
			if ((phI.tmpIsFlag&0x1)!=0) {
				//CMN.Log("发现构词库！！！", phI.name, phI.tmpIsFlag);
				currentFilter.add(mdTmp);
				HdnCmfrt.add(phI); /* 隐·2 */
				CosySofa.add(phI);
				CosyChair.remove(i--);
			} else {
				if (md_name_match(lastName, mdTmp, phI)){
					adapter_idx = md.size();
				}
				md.add(mdTmp);
			}
		}
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
	
	protected abstract void LoadLazySlots(File def, boolean lazyLoad, String moduleName) throws IOException;

	protected void do_LoadLazySlots(ReusableBufferedReader in, ArrayList<PlaceHolder> CosyChair) throws IOException {
		String line;
		int cc=0;
		CosyChair.clear();
		ReadLines:
		while((line = in.readLine())!=null){
			int flag = 0;
			if(line.startsWith("[:")){
				int idx = line.indexOf("]",2);
				if(idx>=2){
					String[] arr = line.substring(2, idx).split(":");
					line = line.substring(idx+1);
					for (String pI:arr) {
						switch (pI){
							case "F":
								flag|=0x1;
								filter_count++;
							break;
							case "C":
								flag|=0x2;
							break;
							case "A":
								flag|=0x4;
							break;
							case "H":
								flag|=0x8;
								hidden_count++;
							break;
							case "Z":
								flag|=0x10;
							break;
							case "S":
								int size = IU.parsint(line);
								if(size>0) CosyChair.ensureCapacity(size);
							continue ReadLines;
						}
					}
				}
			}
			PlaceHolder phI = new PlaceHolder(line);
			phI.lineNumber = cc++;
			phI.tmpIsFlag = flag;
			CosyChair.add(phI);
		}
		in.close();
	}
	
	void ReadInMdlibs(File rec) {
		if(rec==null){
			rec = opt.fileToDecords(null);
		}
		if(mdlibsCon==null){
			mdlibsCon = new HashSet<>(md.size()*3);
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
	public OnScrollChangedListener onWebScrollChanged;
	public OnScrollChangedListener onWebHolderScrollChanged;
	public void initWebScrollChanged() {
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
				
				boolean fromPeruseView=webview.fromCombined==3;
				DragScrollBar _mBar=mBar;
				float currentScale = webview.webScale;
				//tofo
				if(currentScale>= BookPresenter.def_zoom) {
					if(fromPeruseView) {
						_mBar= peruseView.mBar;
					}
				}

				if(fromPeruseView || !isCombinedSearching || (ActivedAdapter!=null && !(ActivedAdapter.combining_search_result instanceof resultRecorderCombined))) {
					if(_mBar.isHidden()){
						if(Math.abs(oldy-y)>=10*dm.density)
							_mBar.fadeIn();
					}
					if(!_mBar.isHidden()){
						if(!_mBar.isWebHeld)
							_mBar.hiJackScrollFinishedFadeOut();
						if(!_mBar.isDragging){
							_mBar.setMax(webview.getContentHeight()-webview.getHeight());
							_mBar.setProgress(webview.getContentOffset());
						}
					}
				}
			};
		}
	}

	public void initWebHolderScrollChanged() {
		if(mBar.getVisibility()==View.VISIBLE){
			if(onWebHolderScrollChanged==null){
				WHP.scrollbar2guard=mBar;
				WHP.setScrollViewListener(onWebHolderScrollChanged=(v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
					if(mBar.isHidden()){
						if(Math.abs(oldScrollY-scrollY)>=10*dm.density)
							mBar.fadeIn();
					}
					if(!mBar.isHidden()){
						if(!mBar.isWebHeld)
							mBar.hiJackScrollFinishedFadeOut();
						if(!mBar.isDragging){
							mBar.setMax(webholder.getMeasuredHeight()-WHP.getMeasuredHeight());
							mBar.setProgress(WHP.getScrollY());
						}
					}
				});
			}
			mBar.fadeOut();
		}
		mBar.setDelimiter("|||", WHP);
	}

	/** 0-搜索  1-返回  2-删除  4-撤销   */
	boolean etSearch_ToToolbarMode(int mode) {
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
		if(md.size()>0) {
			if(currentDictionary==null || currentDictionary==EmptyBook){
				if((adapter_idx<0||adapter_idx>=md.size())) adapter_idx=0;
				currentDictionary = md.get(adapter_idx);
				if(currentDictionary==null){
					PlaceHolder phI = getPlaceHolderAt(adapter_idx);
					if(phI!=null) {
						try {
							md.set(adapter_idx, currentDictionary = new_book(phI, this));
						} catch (Exception ignored) { }
					}
				}
				if(currentDictionary==null){
					currentDictionary = EmptyBook;
				}
				adaptermy.setPresenter(currentDictionary);
			}
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		background=false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		background=true;
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
					webholder.removeAllViews();
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
				WeakReference[] holders = new WeakReference[]{popupCrdCloth, popupCmnCloth, setchooser, bottomPlaylist, DBrowser_holder, DHBrowser_holder};
				for(WeakReference hI:holders){
					if(hI!=null)
						hI.clear();
				}
			}
			if(CMN.instanceCount<0) CMN.instanceCount=0;
		}
		super.onDestroy();
	}

	public void notifyGLobalFSChanged(int targetLevel) {
		BookPresenter.def_fontsize=targetLevel;
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

	Toolbar MainPageSearchbar;
	EditText MainPageSearchetSearch;
	TextView MainPageSearchindicator;
	String MainPageSearchetSearchStartWord;
	boolean HiFiJumpRequested;
	void RecalibrateContentSnacker(boolean bContentBow) {
		if(snack_holder!=null){
			CMN.Log("RecalibrateContentSnacker");
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) snack_holder.getLayoutParams();
			lp.setMargins(0,((bContentBow&&!PDICMainAppOptions.getEnableSuperImmersiveScrollMode())?actionBarSize:0)+(MainPageSearchbar==null||MainPageSearchbar.getParent()==null?0:MainPageSearchbar.getHeight()), 0, 0);
			snack_holder.requestLayout();
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
	
	public void decorateContentviewByKey(ImageView futton, String key) {
		if(futton==null) futton=this.favoriteBtn;
		if(futton!=null) futton.setActivated(GetIsFavoriteTerm(key));
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
			CMN.Log("打开数据库", dateBaseName, opt.getCurrFavoriteDBName());
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
	
	protected void insertUpdate_histroy(String key, int source, ViewGroup webviewholder) {
		if(key!=null) {
			key = key.trim();
		}
		if(key.length()>0) {
			if (getUsingDataV2()) {
				lastInsertedKey = key;
				lastInsertedId = prepareHistoryCon().updateHistoryTerm(this, key, webviewholder);
			} else {
				prepareHistoryCon().insertUpdate(this, key, null);
			}
		}
	}
	
	public long GetAddHistory(String key) {
		if(key!=null) {
			key = key.trim();
		}
		if (getUsingDataV2() && key.length()>0) {
			if (TextUtils.equals(lastInsertedKey, key)) return lastInsertedId;
			else {
				try {
					lastInsertedId = prepareHistoryCon().updateHistoryTerm(this, key, ActivedAdapter!=null?ActivedAdapter.webviewHolder:null);
					lastInsertedKey = key;
				} catch (Exception e) {
					CMN.Log(e);
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
		return ret;
	}
	
	public static BookPresenter new_book(File fullPath, MainActivityUIBase THIS) throws IOException {
		BookPresenter ret = THIS.mdict_cache.get(fullPath.getName());
		if (ret!=null) {
			return ret;
		}
		ret = new BookPresenter(fullPath, THIS, THIS.opt.getPseudoInitCode(0), null);
		THIS.mdict_cache.put(fullPath.getName(), ret);
		return ret;
	}
	
	boolean checkAllWebs(resultRecorderDiscrete combining_search_result, View view, int pos) {
		if(combining_search_result instanceof resultRecorderCombined && pos==0 && view==null){
			if(combining_search_result.checkAllWebs(this, md)){
				CMN.Log("驳回！！！");
				return true;
			}
		}
		return false;
	}

	public void NotifyComboRes(int size) {
		if(PDICMainAppOptions.getNotifyComboRes()) {
			float fval = 0.5f;
			ViewGroup sv;
			if(bIsFirstLaunch||bWantsSelection) {
				sv=getContentviewSnackHolder();
				fval=.8f;
			}else {
				sv=main_succinct;
			}
			String val = recCom.allWebs?"回车以搜索网络词典！":getResources().getString(R.string.cbflowersnstr,opt.lastMdPlanName,md.size(),size);
			showTopSnack(sv, val, fval, -1, -1, 0);
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
	
	public boolean isContentViewAttachedForDB() {
		//CMN.Log("isContentViewAttachedForDB", contentview.getParent());
		return ViewUtils.ViewIsId((View) contentview.getParent(), PeruseViewAttached()?R.id.peruseF:R.id.second_holder);
	}
	
	
	public abstract void AttachContentViewForDB();
	
	public void invalidAllPagers() {
	
	}
	
	public Runnable getOpenImgRunnable() {
		if(mOpenImgRunnable==null){
			mOpenImgRunnable = new Runnable(){
				@Override
				public void run() {
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
						((AgentApplication)getApplication()).clearNonsenses();
					} else {
						isBrowsingImgs=true;
						startActivityForResult(new Intent()
								.setClass(MainActivityUIBase.this
										, PhotoViewActivity.class),0);
					}
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
		if(id<0||id>=md.size()){
			show(R.string.endendr);
			return;
		}
		boolean create=true;
		Window window;
		TextView tv=null;
		FlowTextView ftv=null;
		Dialog d = this.d;
		if(d!=null) {
			window = d.getWindow();
			if(window!=null) {
				Object tag = window.getDecorView().getTag();
				if(tag instanceof TextView){
					tv = (TextView) tag;
					tag = tv.getTag();
					if(tag instanceof FlowTextView){
						ftv = (FlowTextView) tag;
						CheckStars(ftv.getStarLevel());
						create=false;
						if(pickDictDialog!=null) {
							//pickDictDialog.mRecyclerView.smoothScrollToPosition(id);
							//pickDictDialog.lman.smoothScrollToPosition(pickDictDialog.mRecyclerView, null, id);
							pickDictDialog.lman.scrollToPositionWithOffset(id, 0);
						}
						tag = ftv.getTag();
						if(tag instanceof ScrollView) {
							ScrollView sv = (ScrollView) tag;
							sv.scrollTo(0,  0);
						}
						d.show();
					}
				}
			}
			if(create){
				d.dismiss();
			}
		}
		if(create){
			ViewGroup dv = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_about_star,null);
			
			dv.findViewById(R.id.about_popIvBack).setOnClickListener(this);
			dv.findViewById(R.id.about_popLstDict).setOnClickListener(this);
			dv.findViewById(R.id.about_popNxtDict).setOnClickListener(this);
			
			ftv = dv.findViewById(R.id.subtext);
			ftv.mRatingDrawable = getRatingDrawable();
			ftv.setCompoundDrawables(mActiveDrawable, null, null, null);
			
			XYTouchRecorder xyt = opt.XYTouchRecorderInstance();
			ftv.setOnTouchListener(xyt);
			ftv.setOnClickListener(xyt);
			
			TextView title = dv.findViewById(R.id.title);
			title.setText("词典信息");
			tv = dv.findViewById(R.id.resultN);
			if(GlobalOptions.isLarge) tv.setTextSize(tv.getTextSize());
			tv.setTextIsSelectable(true);
			
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			FlowTextView finalFtv = ftv;
			d = new android.app.AlertDialog.Builder(this)
					.setView(dv)
					.setOnDismissListener(dialog -> {
						CheckStars(finalFtv.getStarLevel());
						StarLevelStamp = -1;
						this.d=null;
					})
					.create();
			dv.findViewById(R.id.cancel).setOnClickListener(this);
			window = d.getWindow();
			window.setDimAmount(0);
			tv.setTag(ftv);
			ftv.setTag(dv.getChildAt(1));
			d.setCanceledOnTouchOutside(true);
			d.show();
			window.getDecorView().setTag(tv);
			if(GlobalOptions.isDark) {
				window.getDecorView().setBackgroundColor(0xff333333);
				ftv.setTextColor(Color.WHITE);
				tv.setTextColor(Color.WHITE);
			}
			this.d = d;
		}
		
		CurrentDictInfoIdx = id;
		
		ftv.setStarLevel(StarLevelStamp = md_get_StarLevel(id));
		
		ftv.setText(md_getName(id));
		
		tv.setText(md_getAbout_Trim(id));
	}
	
	private void CheckStars(int newLevel) {
		int oldId = CurrentDictInfoIdx;
		if(oldId>=0&&oldId<md.size()&&StarLevelStamp>=0&&newLevel!=StarLevelStamp) {
			md_set_StarLevel(oldId, newLevel);
			if(pickDictDialog!=null) {
				pickDictDialog.adapter().notifyItemChanged(oldId);
				//pickDictDialog.notifyDataSetChanged();
			}
		}
	}
	
	public void OnPeruseDetached() {
	
	}
	
	public String retrieveDisplayingBooks(String books) {
		String ret = "";
		if (!TextUtils.isEmpty(books)) {
			String[] booksArr = books.split(";");
			int cc=0;
			boolean needDunhao = false;
			for (int i = 0; i < booksArr.length && cc<3; i++) {
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
				} catch (Exception e) { }
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
	
	public String collectDisplayingBooks(String books, ViewGroup webviewholder) {
		String ret=books==null?"":books;
		ViewGroup webholder = webviewholder;
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
		return ret;
	}
	
	public long getBookIdAt(int i) {
		if (getUsingDataV2()) {
			try {
				BookPresenter presenter = md.get(i);
				if (presenter!=null) return presenter.getId();
				String name = new File(getPlaceHolders().get(i).pathname).getName();
				return prepareHistoryCon().getBookID(null, name);
			} catch (Exception e) {
				CMN.Log(e);
			}
		} else {
			return i;
		}
		return -1;
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
					CMN.Log(e);
				}
				if (impl!=null) {
					fileName = impl.getFile().getPath();
				} else {
					fileName = prepareHistoryCon().getBookPath(bid);
				}
				if (fileName!=null) {
					ret = new_book(fileName, this);
				}
			} else {
				if (bid>=0&&bid<md.size())
					ret = md.get((int) bid);
			}
		} catch (IOException e) {
			CMN.Log(e);
		}
		if(ret==null)
			ret=EmptyBook;
		return ret;
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
					e.printStackTrace();
				}
				if (impl!=null) {
					fileName = impl.getFile().getPath();
					if (fileName!=null) {
						ret = new_book(fileName, this);
					}
				}
			} else {
				if (bid>=0&&bid<md.size())
					ret = md.get((int) bid);
			}
		} catch (IOException e) {
			CMN.Log(e);
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
					e.printStackTrace();
				}
				if (impl!=null) {
					return impl.getFile().getPath();
				}
				return prepareHistoryCon().getBookName(bid);
			} else {
				if (bid>=0&&bid<md.size()&&md.get((int) bid)!=null)
					return md.get((int) bid).getDictionaryName();
				return getPlaceHolderAt((int) bid).pathname;
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return null;
	}
	
	
	private void putCurrFavoriteNoteBookId(long value) {
		opt.putCurrFavoriteNoteBookId(value);
		prepareHistoryCon().setFavoriteFolderId(value);
	}
	
	public boolean GetIsFavoriteTerm(String text) {
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
	
	public final class UniCoverClicker implements OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
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
		
		UniCoverClicker(){
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
			} else if(text!=null){
				CurrentSelected=text;
				bFromTextView=true;
			} else {
				bFromTextView=false;
			}
		}
		//boolean bResposibleForCon=false;
		//boolean doCloseOnDiss=true;
		int[] arrayTweakDict;
		int[] arraySelUtils;
		int[] arraySelUtils2;
		int[] arrayTextUtils;
		public boolean bFromWebView;
		public boolean bFromPeruseView;
		boolean bFromTextView;
		boolean bLastFromWebView;
		ViewGroup bottomView;
		RecyclerView twoColumnView;
		TwoColumnAdapter twoColumnAda;
		boolean lastInDark;
		protected ObjectAnimator objectAnimator;
		int lastBookMarkPosition;
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
					ViewGroup target=bFromPeruseView? peruseView.contentview:getContentviewSnackHolder();
					root.post(() -> showTopSnack(target, msg, 0.8f, -1, -1, 0x4));
					
					d.hide();
					ColorPickerDialog asd =
							ColorPickerDialog.newBuilder()
									.setDialogId(123123)
									.setInitialColor(invoker.getUseInternalBG()?invoker.getBgColor():GlobalPageBackground)
									.create();
					asd.setColorPickerDialogListener(new ColorPickerDialogListener() {
						@Override
						public void onColorSelected(ColorPickerDialog dialogInterface, int color) {
							//CMN.Log("onColorSelected");
							if(invoker.getUseInternalBG())
								invoker.setBgColor(color);
							else{
								GlobalPageBackground=CMN.GlobalPageBackground=color;
							}
							WebViewmy mWebView=bFromPeruseView? peruseView.mWebView:invoker.mWebView;
							int ManFt_invoker_bgColor=invoker.getBgColor();
							int ManFt_GlobalPageBackground=GlobalPageBackground;
							if(GlobalOptions.isDark) {
								ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web);
								ManFt_GlobalPageBackground=ColorUtils.blendARGB(ManFt_GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
							};
							boolean apply = bFromPeruseView || widget12.getTag(R.id.image)==null;
							if(invoker.getUseInternalBG()) {
								invoker.saveStates(MainActivityUIBase.this, prepareHistoryCon());
								if(apply && mWebView!=null)
									mWebView.setBackgroundColor(ManFt_invoker_bgColor);
							}else {
								CMN.Log("应用全局颜色变更中…");
								GlobalPageBackground=CMN.GlobalPageBackground;
								if(apply) webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
								WHP.setBackgroundColor(ManFt_GlobalPageBackground);
								if(bFromPeruseView) peruseView.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
								opt.putGlobalPageBackground(CMN.GlobalPageBackground);
								if(Build.VERSION.SDK_INT<21 && apply && mWebView!=null)
									mWebView.setBackgroundColor(ManFt_GlobalPageBackground);
							}
						}
						@Override
						public void onPreviewSelectedColor(ColorPickerDialog dialogInterface, int color) {
							if(bFromPeruseView || widget12.getTag(R.id.image)==null) {
								if (GlobalOptions.isDark)
									color = ColorUtils.blendARGB(color, Color.BLACK, ColorMultiplier_Web);
								WebViewmy mWebView = bFromPeruseView ? peruseView.mWebView : invoker.mWebView;
								ViewGroup webSingleholder = bFromPeruseView ? peruseView.webSingleholder : MainActivityUIBase.this.webSingleholder;
								if (invoker.getUseInternalBG()) {
									if (mWebView != null)
										mWebView.setBackgroundColor(color);
								} else {
									webSingleholder.setBackgroundColor(color);
									if (Build.VERSION.SDK_INT<21 && mWebView != null)
										mWebView.setBackgroundColor(color);
								}
							}
						}
						@Override
						public void onDialogDismissed(ColorPickerDialog dialogInterface, int color) {
							CMN.Log("onDialogDismissed");
							d.show();
							WebViewmy mWebView=bFromPeruseView? peruseView.mWebView:invoker.mWebView;
							int ManFt_invoker_bgColor=invoker.getBgColor();
							int ManFt_GlobalPageBackground=GlobalPageBackground;
							if(GlobalOptions.isDark) {
								ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web);
								ManFt_GlobalPageBackground=ColorUtils.blendARGB(ManFt_GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
							};
							boolean apply = bFromPeruseView || widget12.getTag(R.id.image)==null;
							if(!isDirty){//fall back
								if(invoker.getUseInternalBG()) {
									if(apply && mWebView!=null)
										mWebView.setBackgroundColor(ManFt_invoker_bgColor);
								} else {
									if(apply) webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									WHP.setBackgroundColor(ManFt_GlobalPageBackground);
									if(bFromPeruseView) {
										peruseView.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									}
									if(Build.VERSION.SDK_INT<21 && apply && mWebView!=null)
										mWebView.setBackgroundColor(ManFt_GlobalPageBackground);
								}
							}
						}
						boolean isDirty;
					});
					asd.show(getSupportFragmentManager(),"color-picker-dialog");
				} return;
				case R.id.settings:{
					BookPresenter.showDictTweaker(bFromPeruseView? peruseView.mWebView:null, MainActivityUIBase.this, invoker);
				} return;
				case R.id.appsettings:{
					showAppTweaker();
				} return;
				case R.id.lock:{
					boolean enabled;
					if(bFromPeruseView){
						enabled= peruseView.toggleTurnPageEnabled();
					}else{
						enabled=opt.setTurnPageEnabled(!opt.getTurnPageEnabled());
						PageSlider.TurnPageEnabled=enabled;
					}
					tools_lock.setImageResource(enabled?R.drawable.un_locked:R.drawable.locked);
					opt.putFirstFlag();
					showTopSnack((ViewGroup) d.getListView().getRootView(), enabled?R.string.PT1:R.string.PT2
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
			Object tag = v==null?null:v.getTag();
			if(!bFromTextView &&  tag instanceof Integer){
				Integer ftag = (Integer) tag;
				bFromWebView=(ftag&1)!=0;
				bFromPeruseView=(ftag&2)!=0;
			}
			// nimp
			//if (!bFromWebView && mWebView!=null && invoker instanceof bookPresenter_pdf) {
			//	mWebView.evaluateJavascript(PDFPage, value -> {
			//		mWebView.currentPos = IU.parsint(value);
			//		build_further_dialog();
			//	});
			//} else
			{
				build_further_dialog();
			}
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
					WebViewmy _mWebView = mWebView;
					if(_mWebView==null) _mWebView=invoker.mWebView;
					if(_mWebView==null) {
						showT("错误!!! 网页找不到了");
						return true;
					}
					switch ((int) id) {
						/* 书签 */
						case R.string.bmAdd: {
							if (isLongClicked) return false;
							if (getUsingDataV2()) {
								_mWebView.presenter.toggleBookMark(_mWebView, new OnClickListener(){
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
										WebViewmy webview = (bFromPeruseView ? peruseView.mWebView : invoker.mWebView);
										if (invoker.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
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
										WebViewmy webview = (bFromPeruseView ? peruseView.mWebView : invoker.mWebView);
										if (invoker.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
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
							showBookPreferences(_mWebView.presenter);
						} break;
						/* 文字缩放级别 */
						case R.string.f_scale_up:
						case R.string.f_scale_down: {
							if (isLongClicked) {
								int targetLevel = invoker.getFontSize();
								if (id==R.string.f_scale_up) {
									if (targetLevel < BookPresenter.optimal100) {
										_mWebView.getSettings().setTextZoom(targetLevel = BookPresenter.optimal100);
									} else if (targetLevel < 500) {
										_mWebView.getSettings().setTextZoom(targetLevel = 500);
									} else
										targetLevel = -1;
								} else {
									if (targetLevel > BookPresenter.optimal100) {
										_mWebView.getSettings().setTextZoom(targetLevel = BookPresenter.optimal100);
									} else if (targetLevel > 10) {
										_mWebView.getSettings().setTextZoom(targetLevel = 10);
									} else
										targetLevel = -2;
								}
								if (targetLevel > 0) {
									if (invoker.getUseInternalFS()) {
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
								int targetLevel = invoker.getUseInternalFS() ? _mWebView.getSettings().getTextZoom() : BookPresenter.def_fontsize;
								if (id==R.string.f_scale_up) targetLevel += 10;
								else targetLevel -= 10;
								targetLevel = targetLevel > 500 ? 500 : targetLevel;
								targetLevel = targetLevel < 10 ? 10 : targetLevel;
								_mWebView.getSettings().setTextZoom(targetLevel);
								if (invoker.getUseInternalFS()) {
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
					PreferredToolId = (int) id;
					if(isUserClick) {
						if(!isLongClicked && thisActType==ActType.MultiShare && opt.getRememberVSPanelGo()) {
							opt.putLastVSGoNumber(PreferredToolId);
						}
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
										if (prepareFavoriteCon().insert(MainActivityUIBase.this, StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)), -1, (ViewGroup) mWebView.getParent()) > 0)
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
									popupWord(CurrentSelected, null, -1);
								}
								bNeedClearTextSelection=true;
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									mWebView.simulateScrollEffect();
									bNeedStopScrollEffect=true;
									if (word.length() > 2) {
										popupWord(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)), null, mWebView.frameAt);
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
								Intent newTask = new Intent(Intent.ACTION_MAIN);
								newTask.putExtra(Intent.EXTRA_TEXT, debugString);
								//newTask.putExtra(Intent.EXTRA_SHORTCUT_ID,ShareTarget);
								newTask.setClass(getBaseContext(),PDICMainActivity.class);
								newTask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(newTask);
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
				CMN.Log(e);
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
									CMN.Log(e);
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
				CMN.Log("重建对话框…");
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
				
				if(PageSlider!=null) {
					if(!opt.getTurnPageEnabled()&&!bFromPeruseView||bFromPeruseView&&opt.getPageTurn3())
						tools_lock.setImageResource(R.drawable.locked);
				} else {
					tools_lock.setVisibility(View.GONE);
				}

				dialogList.addFooterView(bottomView);

				if(twoColumnAda!=null)  twoColumnAda.notifyDataSetChanged();
			}

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
			if(!bFromTextView) {
				StringBuilder sb = invoker.appendCleanDictionaryName(null);
				String text = bFromPeruseView ? peruseView.currentDisplaying() : invoker.currentDisplaying;
				if(!TextUtils.isEmpty(text)) {
					sb.append(" - ").append(text);
				}
				d.setTitle(sb.toString());
			} else {
				d.setTitle(R.string.text_operation);
			}
			
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
		
		private boolean hasText() {
			return bFromWebView||bFromTextView;
		}
		
		private void statHasBookmark() {
			int resId=R.string.bmAdd;
			if(getUsingDataV2()) {
				if(mWebView.presenter.hasBookmark(mWebView)){
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
				} catch (JSONException e) { CMN.Log(e); }
			}
		}:null);
	}
	
	public boolean getPinVSDialog() {
		return thisActType==ActType.MultiShare ?opt.getPinVSDialog():opt.getPinDialog();
	}
	
	private void checkMultiVSTGO() {
		CMN.Log("checkMultiVSTGO...", ((MultiShareActivity)MainActivityUIBase.this).NewIntentCalled , opt.getVSPanelGOTransient());
		if(((MultiShareActivity)MainActivityUIBase.this).NewIntentCalled && !getPinVSDialog()) {
			root.postDelayed(()->moveTaskToBack(false), 200);
			//moveTaskToBack(false);
		}
	}
	
	static int[] VersatileShareSlots = new int[]{7,9,10,18,20,21};
	
	public void execVersatileShare(String text, int id) {
		CMN.Log("execVersatileShare", id);
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
			getUcc().setInvoker(null, null, null, text);
			getUcc().execVersatileShare(false, VersatileShareSlots[id]);
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
			CMN.Log("好哎！！！",neo.toString());
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
			CMN.Log(e);
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
	public UniCoverClicker ucc;
	public int CachedBBSize=-1;
	
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
				switch_dark_mode(val==1);
				//CMN.Log("黑暗？", val==1, opt.getInDarkMode(), GlobalOptions.isDark);
			break;
			case 3:
				if(drawerFragment!=null) {
					drawerFragment.sw5.setChecked(val==0);
				}
			break;
			case 4:
				locateNaviIcon(widget13,widget14);
			break;
			case 5:
				if(val==1)
					mBar.setVisibility(View.GONE);
				else
					mBar.setVisibility(View.VISIBLE);
			break;
			case 6:
				if(val==1)
					mBar.setVisibility(View.GONE);
				else
					mBar.setVisibility(View.VISIBLE);
			break;
			case 7:
				if(peruseView !=null&& peruseView.getView()!=null) {
					if(val==1)
						peruseView.mBar.setVisibility(View.GONE);
					else
						peruseView.mBar.setVisibility(View.VISIBLE);
				}
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
				toggleClickSearch(opt.getClickSearchEnabled());
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
			case 16:{//滚动条 [在右/在左/无/系统滚动条] 翻阅/主程序1.2/浮动搜索1.2
				String title = "设置网页滚动条 - ";
				boolean wh = webholder.getChildCount()>0;
				int flagPos=0;
				if(PeruseViewAttached()){
					title += "翻阅模式";
					flagPos = 8;
				} else if(thisActType==ActType.PlainDict){
					title+="主程序"+"/"+(wh?"联合搜索":"单本阅读");
					flagPos = wh?2:0;
				} else {
					title+="浮动搜索"+"/"+(wh?"联合搜索":"单本阅读");
					flagPos = wh?6:4;
				}
				androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(this);
				int finalFlagPos = flagPos;
				builder2.setSingleChoiceItems(R.array.web_scroll_style, opt.getTypeFlag_11_AtQF(flagPos), (dialog12, which) -> {
					if(opt.getScrollTypeApplyToAll()){
						for (int i = 0; i < 9; i+=2)
							opt.setTypeFlag_11_AtQF(which, i);
					} else {
						opt.setTypeFlag_11_AtQF(which, finalFlagPos);
					}
					if(PeruseViewAttached())
						peruseView.RecalibrateWebScrollbar();
					RecalibrateWebScrollbar(webSingleholder.getChildCount()>0?webSingleholder.findViewById(R.id.webviewmy):null);
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
			} break;
			/* 翻阅模式 */
			case 20:
				peruseView.leftLexicalAdapter.notifyDataSetChanged();
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
				peruseView.toggleInPageSearch(false);
			break;
			case 26:
				peruseView.leftLexicalAdapter.notifyDataSetChanged();
				peruseView.bookMarkAdapter.notifyDataSetChanged();
			break;
		}
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
		CMN.Log(60.f/opt.dm.density);
		CMN.Log(tv.getPaddingTop());
		CMN.Log(((ViewGroup)tv.getParent()).getPaddingTop());
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
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 8, Coef, 0, 0, 0x1, 3, 1, 2, 5, false);//opt.getHideScroll1()//
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 9, Coef, 0, 0, 0x1, 4, 1, 2, 6, false);//opt.getHideScroll2()//
		init_clickspan_with_bits_at(tv, ssb, DictOpt, 10, Coef, 0, 0, 0x1, 5, 1, 2, 7, false);//opt.getHideScroll3()//
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
	
	void showAppExit(boolean restart) {
		String[] DictOpt = getResources().getStringArray(R.array.app_exit);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		int title = restart?R.string.relaunch:R.string.warn_exit0;
		TextView tv = buildStandardConfigDialog(this, false, title, title);
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

	abstract void switch_dark_mode(boolean val);

	void changeToDarkMode() {
		//CMN.Log("changeToDarkMode");
		try {
			getReferenceObject(WeakReferenceHelper.quick_settings).clear();
			boolean dark=GlobalOptions.isDark||opt.getInDarkMode();
			AppBlack = dark?Color.WHITE:Color.BLACK;
			AppWhite = dark?Color.BLACK:Color.WHITE;
			MainAppBackground = dark?ColorUtils.blendARGB(MainBackground, Color.BLACK, 0.9f):MainBackground;
			if(drawerFragment!=null){
				drawerFragment.mDrawerListLayout.setBackgroundColor(dark?Color.BLACK:0xffe2e2e2);
				drawerFragment.HeaderView.setBackgroundColor(AppWhite);
				drawerFragment.FooterView.setBackgroundColor(AppWhite);
				drawerFragment.myAdapter.notifyDataSetChanged();
				if(isFragInitiated && pickDictDialog!=null)pickDictDialog.adapter().notifyDataSetChanged();
			}
			if(DBrowser!=null) {
				DBrowser.checkColor();
			}
			if(popupIndicator!=null) {
				popupTextView.setTextColor(dark?AppBlack:Color.GRAY);
				popupIndicator.setTextColor(dark?AppBlack:0xff2b43c1);
			}
			if(adaptermy==null) {
				return;
			}
			adaptermy.notifyDataSetChanged();
			adaptermy2.notifyDataSetChanged();
			if(adaptermy3!=null){
				adaptermy3.notifyDataSetChanged();
				adaptermy4.notifyDataSetChanged();
			}
			if(setchooser!=null){
				setchooser.clear();
				setchooser=null;
			}
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
			CMN.Log(e);
		}
	}

	public abstract void animateUIColorChanges();

	public UniCoverClicker getUcc() {
		if(ucc==null) ucc = new UniCoverClicker();
		return ucc;
	}
	
	public boolean isContentViewAttached() {
		return contentview.getParent()!=null;
	}
	
	abstract void DetachContentView(boolean leaving);
	
	void AttachDBrowser() {
		if(DBrowser!=null){
			boolean fromPeruseView = PeruseViewAttached();
			ViewGroup target = fromPeruseView? peruseView.peruseF:mainF;
			if(!DBrowser.isAdded()) {
				FragmentManager fragmentManager = fromPeruseView? peruseView.getChildFragmentManager():getSupportFragmentManager();
				fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.history_enter, R.anim.history_enter)
				.add(fromPeruseView?R.id.peruseF:R.id.mainF, DBrowser)
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
		if(webholder!=null) {
			webholder.removeAllViews();
		}
		DBrowser.getFragmentManager()
				.beginTransaction()
				.remove(DBrowser)
				.commit();
		ViewUtils.removeView(DBrowser.getView());
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
				showMenuGrid();
			} break;
			case R.drawable.ic_exit_app:{
				v.getBackground().jumpToCurrentState();
				moveTaskToBack(false);
			} break;
			case R.drawable.customize_bars: {
				showIconCustomizator();
			} break;
			case R.drawable.ic_baseline_nightmode_24: {
				showNightModeSwitch();
			} break;
			case R.drawable.ic_options_toolbox: {
				showBookSettings();
			} break;
			//收藏和历史纪录
			case R.drawable.favoriteg: {// get5:
				dismissPopup();
				if(mainF.getChildCount()!=0) return;
				if(DBrowser==null) {
					if(DBrowser_holder!=null) DBrowser=DBrowser_holder.get();
					if(DBrowser==null){
						CMN.Log("重建收藏夹");
						DBrowser_holder = new WeakReference<>(DBrowser = new DBroswer());
					}
					AttachDBrowser();
				}
			} break;
			case R.drawable.historyg: { // get6:
				dismissPopup();
				//todo to favorite
				boolean fromPeruseView = PeruseViewAttached();
				ViewGroup target = fromPeruseView? peruseView.peruseF:mainF;
				if(target.getChildCount()==0){ //if(DBrowser==null)
					if(DHBrowser_holder!=null) DBrowser=DHBrowser_holder.get();
					if(DBrowser==null){
						//CMN.Log("重建历史纪录");
						DHBrowser_holder = new WeakReference<>(DBrowser = new DHBroswer());
					}
					AttachDBrowser();
				}  else if(isContentViewAttachedForDB()){
					DetachContentView(true);
				}
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
				if(isCombinedSearching) {
				
				} else {
					if(CurrentViewPage==1) {
						int delta = (id == R.drawable.ic_prv_dict_chevron ? -1 : 1);
						int testVal = adapter_idx;
						PrvNxtABC.initiatorIdx=testVal;
						PrvNxtABC.initiatorDir=delta;
						PrvNxtABC.沃壳积 =0;
						while(!switch_To_Dict_Idx(adapter_idx+delta, true, false, PrvNxtABC));
						if(PrvNxtABC.collide() && testVal!=adapter_idx) { // rejected
							switch_To_Dict_Idx(testVal, true, false, null);
						}
						if(testVal!=adapter_idx&&PrvNxtABC.rejectIdx>=0) { // not rejected
							PrvNxtABC.rejectIdx=-1;
							cancleSnack();
						}
						if(currentDictionary!=EmptyBook) {
							//showTopSnack(currentDictionary._Dictionary_fName);
							postPutName(1000);
						}
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
			case R.id.cover: {
				if(v==popCover){
					getUcc().setInvoker(CCD, popupWebView, null, null);
					getUcc().onClick(v);
				}
			} break;
			case R.id.popNxtE:
			case R.id.popLstE: {
				if(CCD==null)
					CCD=currentDictionary;
				int np = currentClickDictionary_currentPos+(id==R.id.popNxtE?1:-1);
				if(np>=0&&np<CCD.bookImpl.getNumberEntries()){
					popupTextView.setText(currentClickDisplaying=CCD.bookImpl.getEntryAt(np));
					popupHistory.add(++popupHistoryVagranter,new myCpr<>(currentClickDisplaying,new int[]{CCD_ID, np}));
					if (popupHistory.size() > popupHistoryVagranter + 1) {
						popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
					}
					popuphandler.setDict(CCD);
					if(PDICMainAppOptions.getClickSearchAutoReadEntry())
						popupWebView.bRequestedSoundPlayback=true;
					popupWebView.fromCombined=2;
					CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, popupWebView, currentClickDictionary_currentPos=np);
					decorateContentviewByKey(popupStar, currentClickDisplaying);
					if(!PDICMainAppOptions.getHistoryStrategy0() && PDICMainAppOptions.getHistoryStrategy8()==0)
						insertUpdate_histroy(currentClickDisplaying, 0, PopupPageSlider);
				}
			} break;
			case R.id.popNxtDict:
			case R.id.popLstDict:{
				int idx=-1, cc=0;
				String key= ViewUtils.getTextInView(popupTextView).trim();
				if(key.length()>0) {
					ArrayList<PlaceHolder> ph = getLazyCC();
					String keykey;
					int OldCCD=CCD_ID;
					boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
					int SearchMode = PDICMainAppOptions.getClickSearchMode();
					boolean hasDedicatedSeachGroup = SearchMode==1&&bHasDedicatedSeachGroup;
					boolean reject_morph = false;
					//轮询开始
					while(true){
						if(id==R.id.popNxtDict){
							CCD_ID++;
						}else{
							CCD_ID--;
							if(CCD_ID<0)CCD_ID+=md.size();
						}
						CCD_ID=CCD_ID%md.size();

						if(hasDedicatedSeachGroup && CCD_ID<ph.size() && !PDICMainAppOptions.getTmpIsClicker(ph.get(CCD_ID).tmpIsFlag))
							continue;
						CCD=md_get(CCD_ID);
						cc++;
						if(cc>md.size())
							break;
						
						if (CCD!=EmptyBook) {
							if(CCD.getType()==DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
								PlainWeb webx = (PlainWeb) CCD.bookImpl;
								if(webx.takeWord(key)) {
									CCD.SetSearchKey(key);
									idx=0;
								}
							} else  {
								idx=CCD.bookImpl.lookUp(key, true);
								if(idx<0){
									if(!reject_morph&&use_morph){
										keykey=ReRouteKey(key, true);
										if(keykey!=null)
											idx=CCD.bookImpl.lookUp(keykey, true);
										else
											reject_morph=true;
									}
								}
							}
						}

						if(idx>=0 || hasDedicatedSeachGroup && CCD!=EmptyBook ||  !PDICMainAppOptions.getSkipClickSearch())
							break;
					}

					//应用轮询结果
					if(OldCCD!=CCD_ID && CCD!=EmptyBook){
						if(PDICMainAppOptions.getSwichClickSearchDictOnNav()){
							currentClick_adapter_idx = CCD_ID;
						}
						popupIndicator.setText(CCD.getDictionaryName());

						if(idx<0 && hasDedicatedSeachGroup){
							idx = -1-idx;
							popupWebView.setTag(R.id.js_no_match, false);
						}
						if (idx >= 0) {
							popupHistory.add(++popupHistoryVagranter,new myCpr<>(currentClickDisplaying,new int[]{CCD_ID, idx}));
							if (popupHistory.size() > popupHistoryVagranter + 1) {
								popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
							}
							popuphandler.setDict(CCD);
							if(PDICMainAppOptions.getClickSearchAutoReadEntry())
								popupWebView.bRequestedSoundPlayback=true;
							popupWebView.fromCombined=2;
							CCD.renderContentAt(-1, RENDERFLAG_NEW, -1, popupWebView, currentClickDictionary_currentPos=idx);
						}
					}
				}
			} break;
			//in page search navigation
			case R.id.recess:
			case R.id.forward:{
				if(v.getTag()!=null){
					return;
				}
				boolean next=id==R.id.recess;
				//CMN.Log("下一个");
				if(PDICMainAppOptions.getInPageSearchAutoHideKeyboard()){
					imm.hideSoftInputFromWindow((PeruseSearchAttached()? peruseView.PerusePageSearchetSearch:MainPageSearchetSearch).getWindowToken(), 0);
				}
				jumpHighlight(next?1:-1, true);
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
				}else {//undo
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
			case R.id.popIvBack:{
				DetachClickTranslator();
			} break;
			//返回
			case R.id.popIvRecess:{
				popNav(true);
			} break;
			case R.id.popIvForward:{
				popNav(false);
			} break;
			case R.id.popIvSettings:{
				launchSettings(9, 999);
			} break;
			case R.id.popChecker:{
				CircleCheckBox checker = (CircleCheckBox) v;
				checker.toggle();
				PDICMainAppOptions.setClickSearchPin(checker.isChecked());
			} break;
			case R.id.popIvStar:{
				collectFavoriteView = popupContentView;
				toggleStar(currentClickDisplaying, (ImageView) v, false, PopupPageSlider);
				collectFavoriteView = null;
			} break;
			case R.id.popupText1:{
				if(PDICMainAppOptions.getSwichClickSearchDictOnTop())
					showChooseDictDialog(1);
			} break;
			case R.id.popupText2:{
				if(PDICMainAppOptions.getSwichClickSearchDictOnBottom())
					showChooseDictDialog(1);
			} break;
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
					webcontentlist.setVisibility(View.GONE);
				}
			} break;
			//左右翻页
			case R.id.browser_widget10:
			case R.id.browser_widget11:{//左zuo
				int delta = (id==R.id.browser_widget10?-1:1);
				if(ActivedAdapter==null||isContentViewAttachedForDB()) {
					if(DBrowser!=null) {
						if(delta<0)DBrowser.goBack();
						else DBrowser.goQiak();
					}
					break;
				}
				//if(((ScrollViewmy)WHP).touchFlag!=null)((ScrollViewmy)WHP).touchFlag.first=true;
				//adaptermy2.combining_search_result.expectedPos=0;
				//webholder.removeOnLayoutChangeListener(((resultRecorderCombined)adaptermy2.combining_search_result).OLCL);
				layoutScrollDisabled=false;
				imm.hideSoftInputFromWindow(main.getWindowToken(),0);
				boolean bPeruseInCharge = PeruseViewAttached();
				if(!AutoBrowsePaused&&PDICMainAppOptions.getAutoBrowsingReadSomething()){
					interruptAutoReadProcess(true);
				}
				/* 正常翻页 */
				if(bPeruseInCharge&&opt.getBottomNavigationMode1()==0 || !bPeruseInCharge&&opt.getBottomNavigationMode()==0){
					int toPos = ActivedAdapter.lastClickedPos+delta;
					if(CurrentViewPage==1 && !bPeruseInCharge){
						if(lv2.getVisibility()!=View.VISIBLE){
							webholder.removeAllViews();
						}
					}
					ActivedAdapter.onItemClick(toPos);
				}
				/* 前进后退 */
				else{
					webviewHolder=ActivedAdapter.webviewHolder;
					GoBackOrForward(webviewHolder, delta);
				}
			} break;
			/* 发音 */
			case R.id.browser_widget12:{
				if(webSingleholder.getChildCount()==1 && v.getTag(R.id.image)!=null){
					// todo
					if((int)v.getTag(R.id.image)==R.drawable.ic_fullscreen_black_96dp){
						if(thisActType==ActType.PlainDict)
							((PDICMainActivity)this).forceFullscreen(!PDICMainAppOptions.isFullScreen());
					} else {
						toggleClickThrough();
					}
				} else {
					performReadEntry();
				}
			} break;
			/* 切换收藏 */
			case R.drawable.star_ic:
			case R.id.browser_widget8: {//favorite
				if(ActivedAdapter==null){
					if(DBrowser!=null) {
						DBrowser.toggleFavor();
					}
				} else {
					CMN.Log("ActivedAdapter", ActivedAdapter);
					if(webSingleholder.getChildAt(0)==contentview) {
						//todo 泛化
						WebViewmy mWebView = webSingleholder.findViewById(R.id.webviewmy);
						if (mWebView != null) {
							BookPresenter presenter = mWebView.presenter;
							if (presenter.bookImpl instanceof PlainPDF) {
								((PlainPDF)presenter.bookImpl).toggleFavor();
								break;
							}
						}
					}
					String key = ActivedAdapter.currentKeyText;
					if(GetIsFavoriteTerm(key)) {
						removeFavoriteTerm(key);
						v.setActivated(false);
						show(R.string.removed);
					} else {
						favoriteCon.insert(this, key, opt.getCurrFavoriteNoteBookId(), ActivedAdapter.webviewHolder);
						v.setActivated(true);
						show(R.string.added);
					}
				}
			} break;
			/* 跳转 */
			case R.id.browser_widget9:{//view outlinexxx
				if(ActivedAdapter instanceof com.knziha.plod.plaindict.PeruseView.LeftViewAdapter) {
					v.performLongClick();
					break;
				}
				if(webholder.getChildCount()!=0) {
					imm.hideSoftInputFromWindow(main.getWindowToken(),0);
					int selectedPos=-1;
					final int currentHeight=WHP.getScrollY();
					for(int i=0;i<webholder.getChildCount();i++) {
						View CI = webholder.getChildAt(i);
						if(CI.getBottom() > currentHeight) {
							selectedPos=i;
							break;
						}
					}
					int finalSelectedPos = selectedPos;
					AlertDialog dTmp = new AlertDialog.Builder(this/*,R.style.DialogStyle*/)
							.setTitle("跳转")
					.setAdapter(new BaseAdapter() {
						@Override public int getCount() { return webholder.getChildCount(); }
						
						@Override public Object getItem(int position) { return null; }
						
						@Override public long getItemId(int position) { return 0; }
						
						@NonNull
						@Override
						public View getView(int position, View convertView, @NonNull ViewGroup parent) {
							FlowCheckedTextView ret;
							if(convertView!=null){
								ret = (FlowCheckedTextView) convertView;
							} else {
								ret = (FlowCheckedTextView) getLayoutInflater().inflate(R.layout.singlechoice_w, parent, false);
								ret.setMinimumHeight((int) getResources().getDimension(R.dimen._50_));
							}
							
							View ca = webholder.getChildAt(position);
							if (ca!=null && ca.getTag() instanceof WebViewmy) {
								BookPresenter mdTmp = ((WebViewmy) ca.getTag()).presenter;
								if (mdTmp!=null) {
									FlowTextView tv = ret.mFlowTextView;
									tv.setCompoundDrawables(getActiveStarDrawable(), null, null, null);
									tv.setCover(mdTmp.getCover());
									tv.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
									tv.setStarLevel(PDICMainAppOptions.getDFFStarLevel(mdTmp.getFirstFlag()));
									
									ret.setChecked(position == finalSelectedPos);
									
									ret.setText(mdTmp.getDictionaryName());
									return ret;
								}
							}
							
							ret.setText("Error!!!");
							return ret;
						}
					}, (dialog, pos) -> {
						View childAt = webholder.getChildAt(pos);
						if(childAt!=null) {
							scrollToWebChild(childAt);
							recCom.scrollTo(childAt, MainActivityUIBase.this);
						}
						dialog.dismiss();
					}).create();
					
					dTmp.setCanceledOnTouchOutside(true);

					dTmp.show();
					
					if(!GlobalOptions.isLarge) {
						dTmp.getWindow().setLayout((int) (dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor)), -2);
					}
					
					if(GlobalOptions.isDark) {
						dTmp.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					}
					//d.getWindow().getDecorView().setBackgroundResource(R.drawable.popup_shadow_l);
					//d.getWindow().getDecorView().getBackground().setColorFilter(GlobalOptions.NEGATIVE);
					//d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
				}
				else {
					if(widget12.getTag(R.id.image)!=null){
						float alpha = contentview.getAlpha();// 0.5 0.25 0 1
						if(alpha==0) alpha=1;
						else if(alpha==1) alpha=0.37f;
						else  alpha=0;
						contentview.setAlpha(alpha);
					} else {
						showX(R.string.try_longpress,0);
					}
				}
			} break;
			/* 上下导航 */
			case R.id.browser_widget13:
			case R.id.browser_widget14:{
				boolean nxt = id == R.id.browser_widget14;
				final int currentHeight=WHP.getScrollY();
				int cc=webholder.getChildCount();
				int childAtIdx=cc;
				int top;
				for(int i=0;i<cc;i++) {
					top = webholder.getChildAt(i).getTop();
					if(top>=currentHeight){
						childAtIdx=i;
						if(top!=currentHeight && !nxt) --childAtIdx;
						break;
					}
				}
				childAtIdx+=nxt?-1:1;
				if(childAtIdx>=cc){
					scrollToPagePosition(webholder.getChildAt(cc-1).getBottom());
				} else {
					scrollToWebChild(webholder.getChildAt(childAtIdx));
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
				performReadContent(getCurrentWebContext(false));
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
		}
		click_handled_not = false;
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
			DBrowser_holder.clear();
			favoriteCon = null;
			dbCon.close();
		}
		dbCon = historyCon;
		boolean bNeedUpdate = false;
		if(dbCon!=null && !dbCon.testDBV2) {
			DHBrowser_holder.clear();
			app.historyCon = historyCon = null;
			dbCon.close();
			bNeedUpdate = true;
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
					CMN.Log("激活DBV2::", bookPresenter.bookImpl);
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
				CMN.Log(e2);
			}
			File[] mdnsArr = mdns.toArray(new File[mdns.size()]);
			for(File bookName:mdnsArr) {
				CMN.Log("激活DBV2::", bookName);
				String path = bookName.getPath();
				String name = bookName.getName();
				try {
					path = bookName.getCanonicalPath();
				} catch (IOException e) {
					CMN.Log(e);
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
		String name = mdTmp==EmptyBook?new File(getLazyCC().get(adapter_idx).pathname).getName()
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
	
	private void scrollToWebChild(View childAt) {
		CMN.Log("scrollToWebChild");
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
			WHP.smoothScrollTo(0, childAt.getTop());
		else
			WHP.scrollTo(0, childAt.getTop());
	}

	private void scrollToPagePosition(int offset) {
		if(PDICMainAppOptions.getScrollAnimation())
			WHP.smoothScrollTo(0, offset);
		else
			WHP.scrollTo(0, offset);

	}

	void GoBackOrForward(ViewGroup webviewHolder, int delta) {
		if(webviewHolder!=null){
			View tianming=null;
			int cc = webviewHolder.getChildCount();
			if(cc>0){
				if(cc==1)
					tianming=webviewHolder.getChildAt(0);
				else {
					// todo 第二种模式
					int currentHeight = WHP.getScrollY();
					int selectedPos = -1;
					for (int i = 0; i < cc; i++) {
						if (webviewHolder.getChildAt(i) instanceof LinearLayout) {
							View webHolder = webviewHolder.getChildAt(i);
							if (webHolder.getBottom() >= currentHeight) {
								selectedPos = i;
								break;
							}
						}
					}
					//应用中线法则
					currentHeight = currentHeight + WHP.getHeight() / 2;
					if (selectedPos >= 0) {
						while (selectedPos + 1 < webholder.getChildCount() && webholder.getChildAt(selectedPos).getBottom() < currentHeight) {
							selectedPos++;
						}
						tianming = webviewHolder.getChildAt(selectedPos);
					}
				}
				if(tianming!=null) {
					tianming = tianming.findViewById(delta>0?R.id.forward:R.id.recess);
				}
				if(tianming!=null) {
					tianming.performClick();
				}
			}
		}
	}

	protected boolean popNav(boolean isGoBack) {
		long tm;
		if((!isGoBack && !popupWebView.isloading && popupHistoryVagranter<popupHistory.size()-1 && popupWebView.canGoForward()
				|| isGoBack && popupHistoryVagranter>0  && popupWebView.canGoBack())
			&& (tm=System.currentTimeMillis())-lastClickTime>300) {
			
			if (isGoBack) {
				popupWebView.goBack();
			} else {
				popupWebView.goForward();
			}
			
			try {
				myCpr<String, int[]> record = popupHistory.get(popupHistoryVagranter+=(isGoBack?-1:1));
				popupTextView.setText(currentClickDisplaying=record.key);
				//popupWebView.SelfIdx = CCD_ID = record.value[0];
				currentClickDictionary_currentPos = record.value[1];
				popupIndicator.setText((CCD=md.get(CCD_ID)).getDictionaryName());
				popuphandler.setDict(CCD);
			} catch (Exception e) { CMN.Log(e); }
			
			lastClickTime=tm;
			return true;
		}
		return false;
	}

	@Deprecated
	public void showChooseTTSDialog() { }

	/**
	 *  Show choose dictionary dialog
	 * @param reason 0=pick; 1=pick click dict.
	 * */
	abstract void showChooseDictDialog(int reason);

	void toggleStar(String key, ImageView futton, boolean toast, ViewGroup webviewholder) {
		key = key.trim();
		if(GetIsFavoriteTerm(key)) {
			removeFavoriteTerm(key);
			futton.setActivated(false);
			if(toast)show(R.string.removed);
		} else {
			favoriteCon.insert(this, key, opt.getCurrFavoriteNoteBookId(), webviewholder);
			futton.setActivated(true);
			if(toast)show(R.string.added);
		}
	}
	
	public boolean deleteHistory() {
		try {
			return prepareHistoryCon().wipeData();
		} catch (Exception ignored) { }
		return false;
	}
	
	//longclick
	@Override @SuppressLint("ResourceType")
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			/* long-click exit */
			case R.drawable.ic_exit_app:{
				showAppExit(false);
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
				if(PeruseViewAttached()) {
					peruseView.toolbar_cover.performClick();
					break;
				}
				if((isCombinedSearching && DBrowser!=null) ||ActivedAdapter==adaptermy2) {
					resultRecorderCombined res;
					int idx = 0;

					if(DBrowser!=null)
						res = DBrowser.rec;
					else {
						res = (resultRecorderCombined) adaptermy2.combining_search_result;
						idx = adaptermy2.lastClickedPos;
						if(idx<0 || idx>=res.list().size())
							return true;
					}

					int totalHeight=0;
					int selectedPos=-1;
					final int currentHeight=WHP.getScrollY();
					View itemTo = null;
					for(int i=0;i<webholder.getChildCount();i+=1) {
						itemTo = webholder.getChildAt(i);
						totalHeight+=itemTo.getMeasuredHeight();
						if(totalHeight>currentHeight) {
							selectedPos=i;
							break;
						}
					}
					if(selectedPos!=-1)
						itemTo.findViewById(R.id.cover).performClick();

				}
				else
					currentDictionary.rl.findViewById(R.id.cover).performClick();
				webcontentlist.judger = false;
			} return true;
			/* 页面导航模式 */
			case R.id.browser_widget10:
			case R.id.browser_widget11:{
				boolean bPeruseIncharge = PeruseViewAttached();
				androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(this);
				builder2.setSingleChoiceItems(R.array.btm_navmode, bPeruseIncharge?opt.getBottomNavigationMode1():opt.getBottomNavigationMode(), (dialog12, which) -> {
					TextView tv  = (TextView) ((AlertDialog) dialog12).getListView().getTag();
					if(bPeruseIncharge)
						peruseView.setBottomNavigationType(opt.setBottomNavigationMode1(which), tv);
					else
						setBottomNavigationType(opt.setBottomNavigationMode(which), tv);
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
				titlebar.setText("设置按钮功能");
				dTmp.setCanceledOnTouchOutside(true);
				dTmp.getListView().setPadding(0,0,0,0);
				

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
				webcontentlist.judger = false;
			} return true;
			/* 语音控制器 */
			case R.id.browser_widget12:{
				showSoundTweaker();
				webcontentlist.judger = false;
			} return true;
		}
		return false;
	}
	
	@Override
	public abstract boolean onMenuItemClick(MenuItem item);

	protected void toggleFoldAll() {
		int targetVis=View.VISIBLE;
		int cc=webholder.getChildCount();
		if(cc>0) {
			for (int i = 0; i < cc; i++) {
				if (webholder.getChildAt(i).findViewById(R.id.webviewmy).getVisibility() != View.GONE) {
					targetVis = View.GONE;
					break;
				}
			}
			if(targetVis==View.GONE) {
				awaiting = false;
			}
			for (int i = 0; i < cc; i++) {
				View childAt = webholder.getChildAt(i);
				WebViewmy targetView = childAt.findViewById(R.id.webviewmy);
				if(targetVis==View.GONE) {
					targetView.setVisibility(targetVis);
				} else if(targetView.getVisibility()!=View.VISIBLE){
					childAt.findViewById(R.id.toolbar_title).performClick();
				}
			}
		}
	}
	
	public void onSettingsChanged(SettingsActivity settingsActivity, Preference preference) {
		if (TextUtils.equals("dbv2_up", preference.getKey())) {
			DBUpgradeHelper.showUpgradeDlg(settingsActivity, this, false);
		}
	}
	
	void launchSettings(int fragmentId, int result) {
		CMN.pHandler = new WeakReference<>(this);
		Intent intent = new Intent()
				.putExtra("realm", fragmentId)
				.setClass(this, SettingsActivity.class);
		if (result==0) {
			startActivity(intent);
		} else {
			startActivityForResult(intent, result);
		}
	}

	void toggleClickSearch(boolean val) {
		evalJsAtAllFrames(val?"window.rcsp|=0x20;(rcsp&0xF00)||loadJs(sid.get(),'tapTrans.js')":"window.rcsp&=~0x20");
//		currentDictionary.mWebView.evaluateJavascript(currentDictionary.getWebx().jsLoader, new ValueCallback<String>() {
//			@Override
//			public void onReceiveValue(String value) {
//				CMN.Log("onReceiveValue::rcsp::", value);
//			}
//		});
	}

	void toggleInPageSearch(boolean isLongClicked) {
		if(isLongClicked){
			launchSettings(7, 0);
		}
		else {
			if (MainPageSearchbar == null) {
				Toolbar searchbar = (Toolbar) getLayoutInflater().inflate(R.layout.searchbar, null);
				searchbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);//abc_ic_ab_back_mtrl_am_alpha
				EditText etSearch = searchbar.findViewById(R.id.etSearch);
				//etSearch.setBackgroundColor(Color.TRANSPARENT);
				searchbar.setNavigationOnClickListener(v1 -> {
					toggleInPageSearch(false);
					if (etSearch.hasFocus())
						imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					cancleSnack();
				});
				etSearch.setText(MainPageSearchetSearchStartWord);
				etSearch.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

					}

					@Override
					public void afterTextChanged(Editable s) {
						String text = etSearch.getText().toString().replace("\\", "\\\\");
						HiFiJumpRequested=PDICMainAppOptions.getPageAutoScrollOnType();
						SearchInPage(text);
					}
				});

				View vTmp = searchbar.getChildAt(searchbar.getChildCount() - 1);
				if (vTmp != null && vTmp.getClass() == AppCompatImageButton.class) {
					AppCompatImageButton NavigationIcon = (AppCompatImageButton) vTmp;
					ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) NavigationIcon.getLayoutParams();
					//lp.setMargins(-10,-10,-10,-10);
					lp.width = (int) (45 * dm.density);
					NavigationIcon.setLayoutParams(lp);
				}

				searchbar.setContentInsetsAbsolute(0, 0);
				searchbar.setLayoutParams(toolbar.getLayoutParams());
				searchbar.setBackgroundColor(AppWhite==Color.WHITE?MainBackground:Color.BLACK);
				searchbar.findViewById(R.id.ivDeleteText).setOnClickListener(v -> etSearch.setText(null));
				View.OnDragListener searchbar_stl = (v, event) -> {
					if(event.getAction()== DragEvent.ACTION_DROP){
						ClipData textdata = event.getClipData();
						if(textdata.getItemCount()>0){
							if(textdata.getItemAt(0).getText()!=null)
								etSearch.setText(textdata.getItemAt(0).getText());
						}
						return false;
					}
					return true;
				};
				this.MainPageSearchbar = searchbar;
				this.MainPageSearchetSearch = etSearch;
				this.MainPageSearchindicator = searchbar.findViewById(R.id.indicator);
				View viewTmp=searchbar.findViewById(R.id.recess);
				viewTmp.setOnDragListener(searchbar_stl);
				viewTmp.setOnClickListener(this);
				viewTmp=searchbar.findViewById(R.id.forward);
				viewTmp.setOnDragListener(searchbar_stl);
				viewTmp.setOnClickListener(this);
			}
			boolean b1=MainPageSearchbar.getParent()==null;
			if (b1) {
				contentviewAddView(MainPageSearchbar, 0);
				MainPageSearchbar.findViewById(R.id.etSearch).requestFocus();
				MainPageSearchbar.setTag(MainPageSearchetSearch.getText());
				SearchInPage(null);
			}
			else {
				((ViewGroup) MainPageSearchbar.getParent()).removeView(MainPageSearchbar);
				clearLights();
				MainPageSearchbar.setTag(null);
			}

			if(thisActType==ActType.PlainDict)
				opt.setInPageSearchVisible(b1);
			else{
				PDICMainAppOptions.setInFloatPageSearchVisible(b1);
			}
			root.post(() -> RecalibrateContentSnacker(opt.isContentBow()));
		}
	}

	abstract void contentviewAddView(View v, int i);

	void SearchInPage(String text) {
		if(ActivedAdapter!=null){
			if(text!=null)
			try {
				text=true?text:URLEncoder.encode(text,"utf8");
			} catch (UnsupportedEncodingException ignored) { }
			String val = text==null?"_highlight(null)":"_highlight(\""+ text.replace("\"","\\\"")+"\")";
			webviewHolder=ActivedAdapter.webviewHolder;
			if(webviewHolder!=null){
				int cc = webviewHolder.getChildCount();
				for (int i = 0; i < cc; i++) {
					if(webviewHolder.getChildAt(i) instanceof LinearLayout){
						ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(i);
						if(webHolder.getChildAt(1) instanceof WebView){
							((WebView)webHolder.getChildAt(1))
									.evaluateJavascript(val,null);
						}
					}
				}
			}
			if(popupContentView!=null && popupContentView.getParent()!=null){
				popupWebView.evaluateJavascript(val,null);
			}
		}
	}

	Toolbar Searchbar;
	ViewGroup webviewHolder;
	int cc;
	boolean inlineJump;
	private WebView jumper;

	public void jumpHighlight(int d, boolean calcIndicator){
		try {
			cc=0;
			inlineJump=true;
			do_jumpHighlight(d, calcIndicator);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** 汉星照耀，汉水长流！ */
	private void do_jumpHighlight(int d, boolean calcIndicator) {
		//CMN.Log("jumpHighlight... dir="+d+" framePos="+ActivedAdapter.HlightIdx);
		webviewHolder=ActivedAdapter.webviewHolder;
		int max = webviewHolder.getChildCount();
		cancleSnack();
		boolean b1=ActivedAdapter.HlightIdx>=max,b2=ActivedAdapter.HlightIdx<0;
		if(b1||b2) {
			ActivedAdapter.AcrArivAcc++;
			if(b1&&d==-1) {
				ActivedAdapter.HlightIdx=max-1;
				b1=false;
			}
			else if(b2&&d==1){
				ActivedAdapter.HlightIdx=0;
				b2=false;
			}
			if(ActivedAdapter.AcrArivAcc<=2){
				//CMN.Log("do_jumpHighlight", PDICMainAppOptions.getInPageSearchShowNoNoMatch(), calcIndicator);
				if(PDICMainAppOptions.getInPageSearchShowNoNoMatch() || calcIndicator) {
					String msg = getResources().getString(R.string.search_end, d < 0 ? "⬆" : "", d > 0 ? "⬇" : "");
					showTopSnack(getContentviewSnackHolder(), msg, 0.75f, -1, Gravity.CENTER, 0);
				}
				return;
			}else{
				ActivedAdapter.AcrArivAcc=0;
			}
		}else{
			ActivedAdapter.AcrArivAcc =0;
		}
		if(b1){
			resetLights(d);
			ActivedAdapter.HlightIdx=0;
			if(d==-1){
				evalJsAtFrame(max,"setAsEndLight("+d+");");
			}
		}
		else if(b2){
			resetLights(d);
			ActivedAdapter.HlightIdx=max-1;
			if(d==1){
				if(ActivedAdapter.HlightIdx>=0) evalJsAtFrame(0,"setAsStartLight("+d+");");
			}
		}
		if(ActivedAdapter.HlightIdx<0)ActivedAdapter.HlightIdx=0;
		if(webviewHolder.getChildAt(ActivedAdapter.HlightIdx) instanceof LinearLayout){
			ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(ActivedAdapter.HlightIdx);
			View wv = webHolder.getChildAt(1);
			if(wv instanceof WebView){
				if(jumper!=null && jumper!=wv){
					jumper.evaluateJavascript("quenchLight()",null);
				}
				jumper=(WebView) wv;
				if(cc>0) inlineJump=false;
				//CMN.Log("jumpHighlight_evaluating...", inlineJump);
				jumper.evaluateJavascript(new StringBuilder(28).append("jumpTo(")
						.append(d).append(',')//direction
						.append(-1).append(',')//desired offset
						.append(0).append(',')//frameAt
						.append(0).append(',')//HlightIdx
						.append(cc>0).append(',')//need reset
						.append(0)//topOffset_frameAt
						.append(");").toString(), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						//CMN.Log("jumpHighlight_delta_yield : ", value);
						if(value!=null) {
							int d = 0; boolean b1;
							if(!(b1=value.startsWith("\"")))
								d = IU.parsint(value, 0);
							if (d != 0) {
								ActivedAdapter.HlightIdx += d;
								if (ActivedAdapter.HlightIdx < 0 || ActivedAdapter.HlightIdx >= max) {
									ActivedAdapter.AcrArivAcc++;
								}
								do_jumpHighlight(d, calcIndicator);
							}
							else if(calcIndicator && b1 && ActivedAdapter.webviewHolder!=null) {
								int all=0;
								int preAll=IU.parsint(value.substring(1,value.length()-1),0);
								if(preAll>=0) {
									for (int i = 0; i < ActivedAdapter.webviewHolder.getChildCount(); i++) {
										View v = ActivedAdapter.webviewHolder.getChildAt(i);
										if (v != null) {
											if (i == ActivedAdapter.HlightIdx)
												preAll += all;
											all += IU.parseInteger(v.getTag(R.id.numberpicker), 0);
										}
									}
									(PeruseSearchAttached()? peruseView.PerusePageSearchindicator:MainPageSearchindicator).setText((preAll+1)+"/"+all);
								}
							}
						}
					}
				});
				cc++;
			}
		}
	}

	void resetLights(int d) {
		if(webviewHolder!=null) {
			int max = webviewHolder.getChildCount();
			String exp = "resetLight(" + d + ")";
			for (int index = 0; index < max; index++) {
				if (webviewHolder.getChildAt(index) instanceof LinearLayout) {
					ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
					if (webHolder.getChildAt(1) instanceof WebView) {
						((WebView) webHolder.getChildAt(1))
								.evaluateJavascript(exp, null);
					}
				}
			}
		}
	}

	private void clearLights(){
		if(webviewHolder!=null){
			int max=webviewHolder.getChildCount();
			String exp="clearHighlights()";
			for (int index = 0; index < max; index++) {
				if(webviewHolder.getChildAt(index) instanceof LinearLayout){
					ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
					if(webHolder.getChildAt(1) instanceof WebView){
						((WebView)webHolder.getChildAt(1))
								.evaluateJavascript(exp,null);
					}
				}
			}
		}
	}

	private void evalJsAtFrame(int index, String exp) {
		if(webviewHolder!=null && webviewHolder.getChildAt(index) instanceof LinearLayout){
			ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
			if(webHolder.getChildAt(1) instanceof WebView){
				((WebView)webHolder.getChildAt(1))
						.evaluateJavascript(exp,null);
			}
		}
	}

	private void evalJsAtAllFrames(String exp) {
		evalJsAtAllFrames_internal(webSingleholder, exp);
		evalJsAtAllFrames_internal(webholder, exp);
		if(peruseView !=null && peruseView.mWebView!=null){
			peruseView.mWebView.evaluateJavascript(exp,null);
		}
		if(popupWebView!=null){
			popupWebView.evaluateJavascript(exp,null);
		}
	}

	private void evalJsAtAllFrames_internal(ViewGroup webviewHolder, String exp) {
		for (int index = 0; index < webviewHolder.getChildCount(); index++) {
			if(webviewHolder.getChildAt(index) instanceof LinearLayout){
				ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(index);
				if(webHolder.getChildAt(1) instanceof WebView){
					((WebView)webHolder.getChildAt(1))
							.evaluateJavascript(exp,null);
				}
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

	public void scrollHighlight(int o, int d) {
		//CMN.Log("scrollHighlight",o,d,inlineJump);
		if(webviewHolder!=null && webviewHolder.getChildAt(ActivedAdapter.HlightIdx) instanceof LinearLayout){
			ViewGroup webHolder = (ViewGroup) webviewHolder.getChildAt(ActivedAdapter.HlightIdx);
			View wv = webHolder.getChildAt(1);
			if(wv instanceof WebView){
				int pad=(int) (25*dm.density);
				if(ActivedAdapter.webviewHolder==webholder){
					//CMN.Log("???");
					WHP.performLongClick();
					WHP.onTouchEvent(MotionEvent.obtain( 1000,/*小*/
							1000,/*样，*/
							MotionEvent.ACTION_UP,/*我还*/
							0,/*治*/
							0,/*不了*/
							0));/*你？*/
					if(o==-1){
						if(d==-1 || inlineJump) {
							return;
						}
					}
					o=(int)(o*dm.density);
					o+=webHolder.getTop()+wv.getTop();
					//CMN.Log("??????", o);
					if(o<=WHP.getScrollY() || o+pad>=WHP.getScrollY()+WHP.getHeight()){
						//CMN.Log("do_scrollHighlight",o,d,o-pad);
						WHP.smoothScrollTo(0, o-pad);
					}
				}
				else{
					if(o==-1){
						if(d==-1 || inlineJump) {
							return;
						}
					}
					o=(int)(o*dm.density);
					if(o<=wv.getScrollY() || o+pad>=wv.getScrollY()+wv.getHeight()){
						int finalO=o-pad;
						//CMN.Log("scrolling !!!", finalO, wv.getScrollY(), wv.getScrollY()+wv.getHeight());
						wv.post(() -> {
							//CMN.Log("do scrolling !!!");
							MainActivityUIBase.layoutScrollDisabled=false;
							wv.scrollTo(0, finalO);
							wv.requestLayout();
							NaugtyWeb=(WebViewmy) wv;
							if(hdl!=null)
								hdl.sendEmptyMessage(778899);
						});
					}
				}
			}
		}
	}

	public String getCurrentPageKey() {
		if (MainPageSearchbar!=null && MainPageSearchbar.getParent()!=null) {
			return ViewUtils.getTextInView(MainPageSearchetSearch);
		}
		return null;
		//URLEncoder.encode(key, "utf8");
	}

	public boolean hasCurrentPageKey() {
		if (MainPageSearchbar!=null && MainPageSearchbar.getParent()!=null) {
			Editable psk = MainPageSearchetSearch.getText();
			return psk!=null && psk.toString().trim().length()>0;
		}
		return false;
	}

	public void onHighlightReady(int idx, int number) {
		ViewGroup vg = ActivedAdapter.webviewHolder;
		View v = vg.getChildAt(idx);
		if(v!=null){
			v.setTag(R.id.numberpicker, number);
		}
		int all=0;
		for (int i = 0; i < vg.getChildCount(); i++) {
			all+=IU.parseInteger(vg.getChildAt(i).getTag(R.id.numberpicker),0);
		}
		String finalAll = all==0?"":""+all;
		MainPageSearchindicator.post(() -> MainPageSearchindicator.setText(finalAll));
		if (v != null && HiFiJumpRequested && idx == 0) {
			jumpNaughtyFirstHighlight(v.findViewById(R.id.webviewmy));
			HiFiJumpRequested = false;
		}
	}

	public void prepareInPageSearch(String key, boolean bNeedBringUp) {
		if(MainPageSearchetSearch==null){
			MainPageSearchetSearchStartWord=key;
		}else{
			MainPageSearchetSearch.setText(key);
			bNeedBringUp=bNeedBringUp&&MainPageSearchbar.getParent()==null;
		}
		if(bNeedBringUp){
			toggleInPageSearch(false);
		}
	}

	public void showChooseSetDialog() {//切换分组
		AlertDialog dTmp;
		
		if(setchooser==null||(dTmp=setchooser.get())==null) {
			SecordTime = SecordPime = 0;
			CMN.Log("重建对话框……");
			ArrayList<String> scanInList = new ArrayList<>();
			dTmp = new AlertDialog.Builder(this, GlobalOptions.isDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line)
						.setTitle(R.string.loadconfig)
					.setSingleChoiceItems(ArrayUtils.EMPTY_STRING_ARRAY, -1, null) //new String[]{}
					.setAdapter(new BaseAdapter() {
						@Override public int getCount() { return scanInList.size(); }
						
						@Override public Object getItem(int position) { return null; }
						
						@Override public long getItemId(int position) { return 0; }
						
						@NonNull
						@Override
						public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
							tv.setText(scanInList.get(position));
							return ret;
						}
					}
					, (dialog, pos) -> {
						try {
							currentFilter.clear();
							for (BookPresenter mdTmp : md) {
								if(mdTmp!=null)
									mdict_cache.put(mdTmp.getDictionaryName(), mdTmp);
							}
							for (BookPresenter mdTmp : currentFilter) {
								if(mdTmp!=null)
									mdict_cache.put(mdTmp.getDictionaryName(), mdTmp);
							}
							String setName = scanInList.get(pos);
							File newf = opt.fileToSet(null, setName);
							boolean lazyLoad = PDICMainAppOptions.getLazyLoadDicts();
							LoadLazySlots(newf, lazyLoad, setName);
							buildUpDictionaryList(lazyLoad, mdict_cache);
							//todo 延时清空 X
							//mdict_cache.clear();
							//分组切换
							opt.putLastPlanName(LastPlanName, setName);
							if (adapter_idx<0) {
								switch_To_Dict_Idx(0, true, false, null);
							} else if(md.get(adapter_idx)!=currentDictionary){
								switch_To_Dict_Idx(adapter_idx, true, false, null);
							}
							dialog.dismiss();
							invalidAllLists();
							//show(R.string.loadsucc);
							showTopSnack(main_succinct, R.string.loadsucc
									, -1, -1, Gravity.CENTER, 0);
							if(thisActType==ActType.PlainDict
								&& opt.getCacheCurrentGroup()) {
								// todo 干掉缓冲组
								File def1 = new File(getExternalFilesDir(null), "default.txt");
								if(def1.length()>0) {
									FileOutputStream fout = new FileOutputStream(def1);
									fout.flush();
									fout.close();
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							showT(e.getLocalizedMessage());
						}
					})
					.create();
			
			ListView dlv = dTmp.getListView();
			
			((AlertController.RecycleListView) dlv)
					.mMaxHeight = (int) (root.getHeight() - root.getPaddingTop() - 2.8 * getResources().getDimension(R.dimen._50_));

			dTmp.show();
			
			dlv.setTag(scanInList);

			setchooser = new WeakReference<>(dTmp);
		}
		else if(ViewUtils.DGShowing(dTmp)){
			return;
		}
		
		File ConfigFile = opt.fileToConfig();
		File def = opt.fileToSecords(ConfigFile);
		long l1=def.lastModified();
		long l2=def.getParentFile().lastModified();
		boolean b1 = l1!=SecordTime;
		if(b1 || l2!=SecordPime) {
			ListView lv = dTmp.getListView();
			ArrayList<String> scanInList = (ArrayList) lv.getTag();
			final HashSet<String> con = new HashSet<>();
			if(b1) {
				CMN.Log("扫描1……");
				scanInList.clear();
				lastCheckedPos = -1;
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
				catch (Exception e) {  CMN.Log(e);  }
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
						if(!SU.isNoneSetFileName(name) && con.add(name)) {
							scanInList.add(name);
						}
					}
					if(scanInList.size()>start) {
						CMN.Log("扫描2……");
						String addition = StringUtils.join(scanInList.subList(start, scanInList.size()), "\n");
						BU.appendToFile(def, "\n", addition, "\n");
					}
				}
				SecordPime = l2;
			}
			CMN.Log("扫描分组……", scanInList.size(), def.getParentFile().lastModified());
			((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
			dTmp.getListView().setItemChecked(lastCheckedPos, true);
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
			CMN.Log("onJsPrompt 0");
			result.confirm();
			return true;
		}
		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
			CMN.Log("onJsPrompt 1");
			result.confirm();
			return true;
		}
		//@Override
		//public boolean onJsTimeout() {
		//	return false;
		//}
		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
			CMN.Log("onJsPrompt 2");
			result.confirm(defaultValue);
			return true;//super.onJsPrompt(view, url, message, defaultValue, result);
		}
		
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			CMN.Log("onShowCustomView", BookPresenter._req_fvw, BookPresenter._req_fvh);
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
			layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.horizontalMargin = 0;
			layoutParams.verticalMargin = 0;
			window.setAttributes(layoutParams);

			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

			window.getDecorView().setBackground(null);
			window.getDecorView().setPadding(0,0,0,0);
			if(view!=null) d.setContentView(cv=view);
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
			//CMN.Log("ProgressChanged", newProgress);
			WebViewmy mWebView = (WebViewmy) view;
			if (mWebView.bPageStarted) {
				//todo undo changes made to webview by web dictionaries.
				//if(mWebView.fromCombined==4){
				if (mWebView.fromNet) {
					final BookPresenter invoker = mWebView.presenter;
					if (invoker.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB) {
						((PlainWeb) invoker.bookImpl).onProgressChanged(invoker, mWebView, newProgress);
					}
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
	
	protected boolean delayedAttaching;
	
	public WebViewClient myWebClient = new WebViewClient() {
		public void onPageFinished(WebView view, String url) {
			WebViewmy mWebView = (WebViewmy) view;
			if (mWebView.bPageStarted) {
				mWebView.bPageStarted = false;
			} else {
				return;
			}
			if("about:blank".equals(url) || !mWebView.active&&!mWebView.fromNet) {
				return;
			}
			//CMN.Log("chromium page finished ==> ", url, mWebView.isloading, view.getProgress(), CMN.stst_add, PDICMainAppOptions.getClickSearchAutoReadEntry(), view.getTag(R.drawable.voice_ic));
			if(!mWebView.isloading && !mWebView.fromNet) return;
			int from = mWebView.fromCombined;
			mWebView.isloading = false;
			mWebView.AlwaysCheckRange = 0;
			
			BookPresenter invoker = mWebView.presenter;
			
			if(delayedAttaching && mWebView.presenter==currentDictionary) { // todo same replace (mWebView.SelfIdx==adapter_idx, ->)
				((PDICMainActivity)MainActivityUIBase.this).AttachContentViewDelayed(10);
			}
			
			//if(true) return;
			/* I、接管页面缩放及(跳转)位置(0, 1, 3)
			*  II、进入黑暗模式([0,1,2,3],[4])、编辑模式(0,1,3,[4])。*/
			String toTag = mWebView.toTag;
			OUT:
			if(invoker.getIsWebx()){
				((PlainWeb)invoker.bookImpl).onPageFinished(invoker, mWebView, url, true);
			} else {
			if(invoker.getImageBrowsable() && invoker.bookImpl.hasMdd()) {
				mWebView.evaluateJavascript(BookPresenter.imgAndEntryLoader, null);
			}
			if(from!=2){
				if(invoker==null){
					//root.post(() -> showT("OPF 错误!!!"));
					return;
				}
				boolean editing = invoker.getContentEditable() && invoker.getEditingContents();

				if(mWebView.webScale!= BookPresenter.def_zoom) {
					//if(Build.VERSION.SDK_INT>=21)
					//	view.zoomBy(0.02f);//reset zoom!
					//if(fromPeruseView)PeruseView.webScale=mdict.def_zoom;else invoker.webScale=mdict.def_zoom;
				}
				boolean proceed=true;
				boolean fromCombined = from==1;
				if(view.getTag(R.id.toolbar_action3)!=null) {
					//CMN.Log("变小了吗？");
					if(((WebViewmy)view).webScale!=opt.dm.density){
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							//CMN.Log("变小了");
							view.zoomBy(0.02f);
						}
					}
					view.setTag(R.id.toolbar_action3,null);
				}

				if(toTag!=null){
					mWebView.toTag=null;
					if(!toTag.equals("===???")) {
						proceed=false;
						mWebView.expectedPos = -100;
						if(fromCombined){
							WHP.touchFlag.first=true;
								if (toTag.equals("===000")) {
									//showT("正在跳往子页面顶端…" + invoker.rl.getTop() + "?" + WHP.getChildAt(0).getHeight());
									WHP.post(() -> {
										WHP.smoothScrollTo(0, invoker.rl.getTop());
									});
								} else
									view.evaluateJavascript("var item=document.getElementsByName(\"" + toTag + "\")[0];item?item.getBoundingClientRect().top:-1;"
											, v -> {
												int to = (int)(Float.valueOf(v) * getResources().getDisplayMetrics().density);
												if(to>=0)
													WHP.smoothScrollTo(0, invoker.rl.getTop() - toolbar.getHeight() + to);
											});

						}
						else {
							CMN.Log("toTag::", toTag);
							if(!toTag.equals("===000"))
								view.evaluateJavascript("location.replace(\"#" + toTag + "\");", null);
						}
					}
				}
				
				CMN.Log("expectedPos::", mWebView.expectedPos, mWebView.getScrollY());
				boolean toHighLight = PDICMainAppOptions.getPageAutoScrollOnTurnPage() && view.getTag(R.id.toolbar_action5) != null;

				if(proceed) {
					if (!fromCombined) {
						lastClickTime = System.currentTimeMillis();

						if (mWebView.expectedPos >= 0 && !toHighLight) {
							//layoutScrollDisabled=true;
							CMN.Log("initial_push: ", mWebView.expectedPosX, mWebView.expectedPos);
							mWebView.scrollTo(mWebView.expectedPosX, mWebView.expectedPos);

							NaugtyWeb = mWebView;
							if (hdl != null)
								hdl.sendEmptyMessage(778899);

							layoutScrollDisabled = true;
						}
					}
					else {
						if("===???".equals(toTag)){
							/* 接管同一词典不同页面的网页前后跳转 */
							WHP.touchFlag.first=false;
							//WHP.post(() -> {
							//WHP.smoothScrollTo(0, mWebView.expectedPos);
							//WHP.smoothScrollTo(mWebView.expectedPosX, mWebView.expectedPos);
							recCom.expectedPos=mWebView.expectedPos;
							recCom.LHGEIGHT=0;
							recCom.scrolled=false;
							ViewUtils.addOnLayoutChangeListener(webholder, recCom.OLCL);
							recCom.OLCL.onLayoutChange(webholder,0, webholder.getTop(),0,webholder.getBottom(),0,0,0,0);
							//});
						}
					}
				}

				if (editing && mWebView.currentRendring!=null && mWebView.currentRendring.length==1) {
					if (!(mWebView.fromCombined==3?getPeruseView().bSupressingEditing:invoker.bSupressingEditing)) {
						mWebView.evaluateJavascript(ce_on, null);
					}
				}

				if(toHighLight){
					jumpNaughtyFirstHighlight(mWebView);
				}
			}
			else {
				if(!mWebView.fromNet) {
					if (mWebView.getTag(R.id.js_no_match) != null) {
						mWebView.evaluateJavascript(js_no_match, null);
						mWebView.setTag(R.id.js_no_match, null);
					} else if (toTag != null && !toTag.equals("===000")) {
						mWebView.toTag = null;
						view.evaluateJavascript("location.replace(\"#" + toTag + "\");", null);
					}
				}
			}
			}
			/* 自动播放声音自动播报 */
			if(mWebView.bRequestedSoundPlayback){
				mWebView.bRequestedSoundPlayback=false;
				read_click_search = mWebView==popupWebView;
				if(AutoBrowsePaused||(!PDICMainAppOptions.getAutoBrowsingReadSomething())){
					postReadEntry();
					CMN.Log("hey!!!", opt.getThenAutoReadContent());
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

			if(mWebView.clearHistory) {
				mWebView.clearHistory=false;
				mWebView.clearHistory();
			}
			
			if(invoker.GetSearchKey()!=null) {
				String validifier1 = invoker.getOfflineMode()&&invoker.getIsWebx()?null:invoker.bookImpl.getVirtualTextValidateJs(invoker, mWebView, mWebView.currentPos);
				if (validifier1!=null) {
					invoker.EvaluateValidifierJs(validifier1, mWebView);
				}
				invoker.SetSearchKey(null);
			}
			
			if (opt.getClickSearchEnabled() && !invoker.getImageOnly()) {
				mWebView.evaluateJavascript(BookPresenter.tapTranslateLoader, null);
			}
			
			/* 延迟加载 */
			if(awaiting && from==1 && PDICMainAppOptions.getDelaySecondPageLoading() && !(PDICMainAppOptions.getOnlyExpandTopPage() && mWebView.frameAt+1>=opt.getExpandTopPageNum()) ){
				int next = fastFrameIndexOf(webholder, mWebView, mWebView.frameAt) + 1;
				//CMN.Log("/* 延迟加载 */ ?????? ", next, webholder.getChildCount(), PDICMainAppOptions.getOnlyExpandTopPage(), mWebView.frameAt, opt.getExpandTopPageNum());
 				if(next < webholder.getChildCount()){
					View childAt;
					try {
						while ((childAt=webholder.getChildAt(next++))!=null) {
							if (childAt.getTag() instanceof WebViewmy) {
								mWebView = ((WebViewmy) childAt.getTag());
								BookPresenter presenter = mWebView.presenter;
								if(presenter.mWebView.awaiting
										&& !PDICMainAppOptions.getTmpIsCollapsed(presenter.tmpIsFlag)
										&& !presenter.getAutoFold()
								){
									CMN.Log("/* 延迟加载 */", presenter.getDictionaryName());
									presenter.toolbar_title.performClick();
									break;
								}
							}
						}
					} catch (Exception ignored) {  }
				}
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			WebViewmy mWebView = (WebViewmy) view;
			mWebView.bPageStarted=true;
			final BookPresenter invoker = mWebView.presenter;
			if(invoker.getType() == DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_WEB){
				((PlainWeb)invoker.bookImpl).onPageStarted(invoker, view, url, true);
			}
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
			WebViewmy mWebView = (WebViewmy) view;
			if(mWebView.forbidLoading) {
				return true;
			}
			CMN.Log("chromium shouldOverrideUrlLoading_???",url,view.getTag(), mWebView.fromCombined);
			final BookPresenter invoker = mWebView.presenter;
			if(invoker==null) return false;
			boolean fromPopup = view==popupWebView;
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
						popupHistory.add(++popupHistoryVagranter, new myCpr<>(currentClickDisplaying, new int[]{CCD_ID, currentClickDictionary_currentPos}));
						if (popupHistory.size() > popupHistoryVagranter + 1) {
							popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
						}
					}
				}
				return ret;
			}
			//CMN.Log("chromium shouldOverrideUrlLoading_",url);
			//TODO::改写历史纪录方式，统一操作。
			int from = mWebView.fromCombined;
			boolean fromPeruseView = from==3;
			boolean fromCombined = from==1;
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
				if (opt.bShouldUseExternalBrowserApp) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					return true;
				} else {
					if (fromPeruseView) {
						//TODO fix
//						if (PeruseView.mWebView.HistoryVagranter >= 0)
//							PeruseView.mWebView.History.get(PeruseView.mWebView.HistoryVagranter).value = PeruseView.mWebView.getScrollY();
//						PeruseView.mWebView.History.add(++PeruseView.mWebView.HistoryVagranter, new myCpr<>(url, PeruseView.expectedPos = 0));
//						for (int i = PeruseView.History.size() - 1; i >= PeruseView.HistoryVagranter + 1; i--)
//							PeruseView.History.remove(i);
//
//						PeruseView.recess.setVisibility(View.VISIBLE);
//						PeruseView.forward.setVisibility(View.VISIBLE);
					} else {
						//TODO fix
//						if(mWebView.HistoryVagranter>=0) invoker.History.get(mWebView.HistoryVagranter).value=invoker.mWebView.getScrollY();
//						invoker.History.add(++mWebView.HistoryVagranter,new myCpr<>(url,invoker.expectedPos=0));
//						for(int i=invoker.History.size()-1;i>=mWebView.HistoryVagranter+1;i--)
//							invoker.History.remove(i);
//
//						invoker.recess.setVisibility(View.VISIBLE);
//						invoker.forward.setVisibility(View.VISIBLE);
					}
					return false;
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
				/* 页内跳转 */
				if (url.startsWith("entry://#")) {
					//Log.e("chromium inter_ entry3", url);
					if (!fromCombined) {// 三种情况：normal，peruseview， popup查询。
						/* '代管'之含义：在网页前进/后退之时，不使用系统缓存，而是手动加载、手动定位。 */
						ScrollerRecord PageState;
						if (mWebView.HistoryVagranter >= 0) {
							(PageState = mWebView.History.get(mWebView.HistoryVagranter).value).set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
							if (mWebView.HistoryVagranter == 0) {
								invoker.HistoryOOP.put((int)mWebView.currentPos, PageState); //todo
							}
						}
						mWebView.expectedPos = -100;
						((WebViewmy) view).isloading = true;
						view.evaluateJavascript("location.replace(\"" + url.substring(entryTag.length()) + "\");", null);
					}
					else {//TODO 精确之
						if (mWebView.HistoryVagranter >= 0) {
							mWebView.History.get(mWebView.HistoryVagranter).value.set(0, WHP.getScrollY(), mWebView.webScale);
						}
						view.evaluateJavascript("var item=document.getElementsByName(\"" + url.substring(entryTag.length() + 1) + "\")[0]; item?item.getBoundingClientRect().top:-1;"
								, v -> {
									int to=(int)(Float.valueOf(v) * getResources().getDisplayMetrics().density);
									if(to>=0)
										WHP.smoothScrollTo(0, invoker.rl.getTop() - toolbar.getHeight() + to);
								});
					}
					if(fromPeruseView)
						peruseView.setCurrentDis(invoker, mWebView.currentPos);
					else if(mWebView.fromCombined<=1)
						invoker.setCurrentDis(mWebView, mWebView.currentPos);
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
						if (fromPeruseView) {
							peruseView.recess.setVisibility(View.VISIBLE);
							peruseView.forward.setVisibility(View.VISIBLE);
							peruseView.isJumping = true;
							if (mWebView.HistoryVagranter >= 0)
								mWebView.History.get(peruseView.mWebView.HistoryVagranter).value.set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
							peruseView.setCurrentDis(invoker, pos);
						} else {
							invoker.isJumping = true;
							if (mWebView.HistoryVagranter >= 0)
								mWebView.History.get(mWebView.HistoryVagranter).value.set(mWebView.getScrollX(),mWebView.getScrollY(), mWebView.webScale);
							invoker.setCurrentDis(mWebView, pos);
						}
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
				else{
					//CMN.Log("chromium inter_ entry2", url);
					url = url.substring(entryTag.length());
					try {
						boolean popup = invoker.getPopEntry();
						if(popup){
							init_popup_view();
							popupWebView.frameAt = mWebView.frameAt;
							//popupWebView.SelfIdx = mWebView.SelfIdx;
							mWebView = popupWebView;
						}
						mWebView.toTag = null;
						int tagIdx = url.indexOf("#");
						if (tagIdx > 0) {
							mWebView.toTag = url.substring(tagIdx + 1);
							url = url.substring(0, tagIdx);
						}
						if(url.endsWith("/")) url=url.substring(0, url.length()-1);
						url = URLDecoder.decode(url, "UTF-8");
						if(popup){
							popupWord(url, popupWebView.presenter, mWebView.frameAt);
							return true;
						}
						else {
							/* 查询跳转目标 */
							int idx = invoker.bookImpl.lookUp(url, true);
							//CMN.Log("查询跳转目标 : ", idx, URLDecoder.decode(url,"UTF-8"), processText(URLDecoder.decode(url,"UTF-8")));
							if (idx >= 0) {//idx != -1
								if(!fromPopup) {
									/* 除点译弹窗与网络词典，代管所有网页的前进/后退。 */
									if (fromPeruseView) {
										peruseView.isJumping = true;
										if (mWebView.HistoryVagranter >= 0)
											mWebView.History.get(mWebView.HistoryVagranter).value.set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
										peruseView.setCurrentDis(invoker, idx);
									}
									else {
										invoker.isJumping = true;
										if (!fromCombined) {
											ScrollerRecord PageState = null;
											if (mWebView.HistoryVagranter >= 0)
												(PageState = mWebView.History.get(mWebView.HistoryVagranter).value).set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
											if (mWebView.HistoryVagranter == 0)
												invoker.HistoryOOP.put((int) mWebView.currentPos, PageState); // todo
										} else if (mWebView.HistoryVagranter >= 0) {
											mWebView.History.get(mWebView.HistoryVagranter).value.set(0, WHP.getScrollY(), mWebView.webScale);
										}
										invoker.setCurrentDis(mWebView, idx);
									}
									if (mWebView.toTag == null) {
										/* 跳转至顶部 */
										mWebView.toTag = "===000";
										mWebView.expectedPosX = 0;
										mWebView.expectedPos = 0;
									} else {
										/* 什么都不要做 */
										mWebView.expectedPos = -100;
									}
								} else {
									mWebView.currentPos = idx;
								}
								float initialScale = BookPresenter.def_zoom;
								mWebView.setInitialScale((int) (100 * (initialScale / BookPresenter.def_zoom) * opt.dm.density));
								mWebView.isloading = true;
								StringBuilder htmlBuilder = invoker.AcquirePageBuilder();
								if(fromPopup){
									popupHistory.add(++popupHistoryVagranter, new myCpr<>(CCD.getLexicalEntryAt(idx), new int[]{CCD_ID, idx}));
									if (popupHistory.size() > popupHistoryVagranter + 1) {
										popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
									}
								}
								invoker.AddPlodStructure(mWebView, htmlBuilder ,mWebView==popupWebView, invoker.rl==mWebView.getParent()&&invoker.rl.getLayoutParams().height>0);
								invoker.LoadPagelet(mWebView, htmlBuilder, invoker.bookImpl.getRecordsAt(null, idx));
								return true;
							}
						}
					}
					catch (Exception e) {
						//TODO !!!
						msg=e.toString();
						if(true) CMN.Log(e);
					}
				}
				/* 跳转失败 */
				String showError = getResources().getString(R.string.jumpfail) + url;
				if(msg!=null) showError+=" "+msg;
				showT(showError);
				return true;
			}
			if(fromPopup){
				popupHistory.add(++popupHistoryVagranter, new myCpr<>(currentClickDisplaying, new int[]{CCD_ID, currentClickDictionary_currentPos}));
				if (popupHistory.size() > popupHistoryVagranter + 1) {
					popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
				}
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
				e.printStackTrace();
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
			//CMN.Log("chromium shouldInterceptRequest???",url,view.getTag());
			//if(true) return null;
			if(url.startsWith("data:")) return null;
			
			WebViewmy mWebView = (WebViewmy) view;
			BookPresenter invoker = mWebView.presenter;
			if(invoker==null) return null;
			
			if(url.startsWith("mdbr://")
					|| url.startsWith("https://mdbr/")
			){
				try {
					if(url.startsWith("mdbr://")) url=url.substring(7);
					else url=url.substring(13);
					CMN.Log("[fetching internal res : ]", url);
					String mime="*/*";
					if(url.endsWith(".css")) mime = "text/css";
					if(url.endsWith(".js")) mime = "text/js";
					return new WebResourceResponse(mime, "UTF-8", loadCommonAsset(url));
				} catch (Exception e) {
					CMN.Log(e);
				}
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
//				if (webx.fastGStaticFonts && Build.VERSION.SDK_INT<21) {
//					if (url.endsWith(".woff")) {
//						InputStream resTmp = ViewUtils.fileToStream(invoker.a, new File(AssetTag.substring(0, 6)+"2/"+url.substring(url.lastIndexOf("/") + 1)));
//						if(resTmp!=null) return new WebResourceResponse("*/*", "UTF-8", resTmp);
//					}
//				}
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
						
	//					if (url.contains("apis.google")) return emptyResponse;
	
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
											CMN.Log("pathDownload", path);
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
													CMN.Log("shouldInterceptRequest 下载中...！", url);
													CMN.Log("shouldInterceptRequest 下载目标: ", name);
													try {
														ViewUtils.downloadToStream(url, new OutputStream[]{null}, path.getPath()
																, accept, refer, origin, request, webx);
														CMN.Log("shouldInterceptRequest 已下载！", url);
													} catch (Exception e) {
														CMN.Log(e);
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
								CMN.Log(e);
							}
						}
					}
					else{
						view.setTag(R.id.save, null);
					}
				}
				if (webx.getReplaceLetToVar(url)) {
					try {
						return ViewUtils.KikLetToVar(url , accept, refer, origin, request, webx);
					} catch (Exception e) { CMN.debug("kiklet 转化失败::", e); }
				}
				return null;
			}
			if(url.startsWith("http")) {
				if(url.endsWith(".mp3"))
					return null;
				if (invoker.getOfflineMode())
					return emptyResponse;
			}
			//CMN.Log("漏网之鱼::", url);

			if(url.startsWith("font://") && fontlibs!=null){
				url=url.substring(7);
				try {
					return new WebResourceResponse("font/*", "UTF-8", new FileInputStream(new File(fontlibs, url)));
				} catch (Exception ignored) {  }
				return null;
			}
			//CMN.Log("chrochro_inter_0",url);

			if(url.startsWith(soundTag)) {
				opt.supressAudioResourcePlaying=false;
				url = url.substring(soundTag.length());
			}
			else if(url.startsWith(soundsTag)) {
				url = url.substring(soundsTag.length());
				try {
					url = URLDecoder.decode(url,"UTF-8");
				} catch (Exception ignored) { }
				String soundKey="\\"+url+".";
				CMN.Log("接收到发音任务！", soundKey, "::", invoker.getDictionaryName());
				InputStream restmp=null;
				WebResourceResponse ret=null;
				BookPresenter mdTmp;
				for (int i = 0; i < md.size(); i++) {
					mdTmp = findPronouncer(i, invoker);
					if(mdTmp!=null){
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
									CMN.Log(e);
								}
							}
							ret = new WebResourceResponse("audio/mpeg", "UTF-8", restmp);
							break;
						}
					}
				}
				if(ret!=null){
					CMN.Log("返回音频");
					return ret;
				} else {
					ReadEntryPlanB(mWebView, url);
				}
				return null;
			}

			String SepWindows = "\\";

			String key=url;
			try {
				key = URLDecoder.decode(url,"UTF-8");
			} catch (Exception ignored) { }

			int start = key.indexOf(BookPresenter.FileTag);
			if(start==-1){
				if(key.startsWith("./"))
					key=key.substring(1);
			} else {
				key = key.substring(start+ BookPresenter.FileTag.length());
			}
			if(url.startsWith("/MdbR/")) {
				try {
					url=url.substring(6);
					CMN.Log("[fetching internal res : ]", url);
					String mime="*/*";
					if(url.endsWith(".css")) mime = "text/css";
					if(url.endsWith(".js")) mime = "text/js";
					return new WebResourceResponse(mime, "UTF-8", loadCommonAsset(url));
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
			else if(key.startsWith("/pdfimg/")) {
//				String urlkey=key.substring("/pdfimg/".length());
//				int idx = urlkey.lastIndexOf("#");
//				int page = 0;
//				if(idx>0){
//					page=IU.parsint(urlkey.substring(idx+1));
//					urlkey=urlkey.substring(0, idx);
//				}
//				OUTPDF:
//				try{
//					if(pdfiumCore==null){
//						pdfiumCore = new PdfiumCore(getBaseContext());
//						cached_pdf_docs=new HashMap<>();
//					}
//					PdfDocument pdf = cached_pdf_docs.get(urlkey);
//					if(pdf==null){
//						File path = new File(urlkey);
//						if(!path.exists()) break OUTPDF;
//						pdf = pdfiumCore.newDocument(ParcelFileDescriptor.open(path, ParcelFileDescriptor.MODE_READ_ONLY));
//						cached_pdf_docs.put(urlkey, pdf);
//					}
//					if(mWebView.fromCombined==0  && !mWebView.fromNet && invoker.getIsolateImages()){
//						//CMN.Log("Pdf Isolating Images...");
//						new_photo = key;
//						PhotoPager.removeCallbacks(PhotoRunnable);
//						PhotoPager.post(PhotoRunnable);
//						return super.shouldInterceptRequest(view, url);
//					}
//					CMN.rt();
//
//					pdfiumCore.openPage(pdf, page);
//
//					CMN.pt("文档打开耗时 : "); CMN.rt();
//
//
//					float shrinkage=1f;
//					int width = pdfiumCore.getPageWidth(pdf, page);
//					int height = pdfiumCore.getPageHeight(pdf, page);
//					float mBitmapRam = (width * height * 2);
//					if(mBitmapRam>MaxBitmapRam){
//						shrinkage = MaxBitmapRam/mBitmapRam;
//					}
//					width*=shrinkage;
//					height*=shrinkage;
//					Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//					CMN.pt("分配内存耗时 : "); CMN.rt();
//					pdfiumCore.renderPageBitmap(pdf, bitmap, page, 0, 0, width, height);
//
//					CMN.pt("解码耗时 : "); CMN.rt();
//
//					ByteArrayOutputStream bos = new ByteArrayOutputStream(bitmap.getByteCount());
//					bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bos);
//					CMN.pt("再编码耗时 : "); CMN.rt();
//					return new WebResourceResponse("image/jpeg","UTF-8",new ByteArrayInputStream(bos.toByteArray()));
//				} catch(Exception ex) {
//					ex.printStackTrace();
//				}
			}

			key=key.replace("/", SepWindows);

			//CMN.Log("chrochro_inter_key is",key);

			if(!key.startsWith(SepWindows)){
				key=SepWindows+key;
			}
			if(key.endsWith(SepWindows)){
				key=key.substring(0, key.length()-1);
			}

			int suffixIdx = key.lastIndexOf(".");
			String suffix = null;
			String mime = null;
			if(suffixIdx!=-1){
				suffix = key.substring(suffixIdx).toLowerCase();
				suffixIdx = key.indexOf("?");
				if(suffixIdx!=-1){
					suffix = key.substring(suffixIdx);
				}
			}
			if(suffix!=null)
			switch (suffix){
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
			//检查后缀，js，ini,png,css,直接路径。
			if(mime!=null && key.lastIndexOf(SepWindows)==0) {
				File candi = new File(invoker.f().getParentFile(),new File(url).getName());
				CMN.debug("外挂CSS/JS资源::", candi, url, "$.getAbsolutePath()", "$.exists()");
				if(candi.exists()) try {
					return new WebResourceResponse(mime, "UTF-8", new AutoCloseInputStream(new FileInputStream(candi)));
				} catch (FileNotFoundException ignored) { }
			}

			if(!invoker.bookImpl.hasMdd())
				return null;
			key= mdict.requestPattern.matcher(key).replaceAll("");
			if(mWebView.fromCombined==0 && !mWebView.fromNet && invoker.getIsolateImages() && RegImg.matcher(key).find()){
				//CMN.Log("Isolating Images...");
				new_photo = key;
				PhotoPager.removeCallbacks(PhotoRunnable);
				PhotoPager.post(PhotoRunnable);
				return null;
			}
			try {
				InputStream restmp=invoker.bookImpl.getResourceByKey(key);
				if(restmp==null) {
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
						CMN.Log(url, "spx : ", ret);
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
				CMN.Log(e);
				return super.shouldInterceptRequest(view, url);
			}
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
		CMN.Log("ReadEntryPlanB");
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
		BookPresenter mdTmp = md.get(i);
		if(mdTmp==invoker)
			return mdTmp;
		if(mdTmp!=null)
			return PDICMainAppOptions.getTmpIsAudior(mdTmp.tmpIsFlag)?mdTmp:null;
		else {
			ArrayList<PlaceHolder> CosyChair = getLazyCC();
			if(i<CosyChair.size()) {
				PlaceHolder phTmp = CosyChair.get(i);
				if (PDICMainAppOptions.getTmpIsAudior(phTmp.tmpIsFlag)) {
					try {
						md.set(i, mdTmp = new_book(phTmp, MainActivityUIBase.this));
					} catch (Exception ignored) { }
				}
			}
			return mdTmp;
		}
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
				for (int i = 0; i < md.size(); i++) {
					mdTmp = findPronouncer(i, invoker);
					if(mdTmp!=null){
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
		} catch (Exception e) { CMN.Log(e); }
		return null;
	}

	private WebResourceResponse decodeSpxFile(InputStream restmp, File f) throws IOException {
		//CMN.Log("decodeSpxFile……");
		JSpeexDec decoder = new JSpeexDec();
		try {
			decoder.decode(new DataInputStream(restmp) , f, JSpeexDec.FILE_FORMAT_WAVE);
		} catch (Exception e) { CMN.Log(e); }
		return null;
	}

	void jumpNaughtyFirstHighlight(WebViewmy mWebView) {
		long mJumpNaughtyTimeToken = jumpNaughtyTimeToken = System.currentTimeMillis();
		NaughtyJumpper=()-> mWebView.evaluateJavascript("window.bOnceHighlighted", value -> {
			if(mJumpNaughtyTimeToken!=jumpNaughtyTimeToken)
				return;
			if(!"true".equals(value)){
				do_jumpNaughtyFirstHighlight(mWebView);
			}else{
				jumpHighlight(1, false);
			}
		});
		do_jumpNaughtyFirstHighlight(mWebView);
	}

	private void do_jumpNaughtyFirstHighlight(WebViewmy mWebView) {
		mWebView.removeCallbacks(NaughtyJumpper);
		mWebView.post(NaughtyJumpper);
	}

	public void locateNaviIcon(View widget13,View widget14){
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
		}else {
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
					 -ms-filter: invert(100%);}\
				body {background:#00000000}', d=document,
	 head = d.getElementsByTagName('head')[0],
	 sty = d.createElement('style');
	 sty.id = "_PDict_Darken";
	 if(!d.getElementById(sty.id)) {
		 sty.class = "_PDict";
		 sty.type = 'text/css';
		 if (sty.styleSheet){
		 	style.styleSheet.cssText = css;
		 } else {
		 	sty.appendChild(d.createTextNode(css));
		 }
		 //injecting the css to the head
		 head.appendChild(sty);
		 if(d.body){d.body.style.background='#00000000';d._pdkn=1}
	 }
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
			CMN.Log("forceNextTextRunnable");
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
					CMN.Log("缓存了句子", i, speakPool[i]);
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
					CMN.Log("tts onStart" ,speakPoolIndex);
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
					CMN.Log("tts onDone" ,speakPoolIndex, speakCacheEndIndex);
					if(speakPoolIndex>=speakCacheEndIndex){
						mPullReadTextRunnable.run();
					}
					if(speakPoolIndex>=speakPool.length){
						onAudioPause();
						CMN.Log("tts onPause");
					}
					//onAudioPause();
				}

				@Override
				public void onError(String utteranceId) {
					//CMN.Log("tts onError" ,utteranceId);
				}

				@Override
				public void onError(String utteranceId, int code) {
					CMN.Log("tts onError" ,utteranceId, code);
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
			if(svp!=null) svp.removeView(popupGuarder);
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
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 0: {
				isBrowsingImgs = false;
			} break;
			case 999:
				if(PDICMainAppOptions.getImmersiveClickSearch()!=PDICMainAppOptions.getImmersiveClickSearch(TFStamp))
					popupWord(null,null, 0);
			break;
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
		mThenReadEntryCount--;
		BookPresenter mCurrentDictionary = null;
		WebViewmy wv;
		String target;
		
		if(read_click_search && CCD!=EmptyBook) {
			mCurrentDictionary = CCD;
			target = currentClickDisplaying;
			wv = popupWebView;
		} else {
			if(PeruseViewAttached()){
				mCurrentDictionary = peruseView.currentDictionary;
				wv = peruseView.mWebView;
			}
			else {
				View SV;
				if (webholder!=null && webholder.getChildCount() != 0) {
					int selectedPos = -1;
					/* 当前滚动高度 */
					int currentHeight = WHP.getScrollY();
					for (int i = 0; i < webholder.getChildCount(); i++) {
						if (webholder.getChildAt(i).getBottom() > currentHeight) {
							/* Frames累加高度 首次超出 滚动高度 */
							selectedPos = i;
							break;
						}
					}
					//应用中线法则
					currentHeight = currentHeight + WHP.getHeight() / 2;
					if (selectedPos >= 0) {
						while (selectedPos + 1 < webholder.getChildCount() && webholder.getChildAt(selectedPos).getBottom() < currentHeight) {
							selectedPos++;
						}
					}
					SV = webholder.getChildAt(selectedPos);
				} else {//单本阅读
					SV = webSingleholder.getChildAt(0);
				}
				
				if (SV!=null && SV.getTag() instanceof WebViewmy) {
					mCurrentDictionary = ((WebViewmy) SV.getTag()).presenter;
				}
				
				if (mCurrentDictionary != null) {
					wv = mCurrentDictionary.mWebView;
				} else {
					wv = null;
				}
			}
			target = wv==null?null:wv.word;
		}
		//showT("PRE", mCurrentDictionary.getName()+"-"+wv+"-"+target);
		if(mCurrentDictionary!=null && wv!=null && target!=null) {
			if (mCurrentDictionary.isMddResource() && target.length() > 1) {
				int end = target.lastIndexOf(".");
				if (end < target.length() - 6) end = -1;
				target = target.substring(1, end > 0 ? end : target.length());
			}
			String finalTarget = target;
			if(PDICMainAppOptions.getUseSoundsPlaybackFirst()){
				requestSoundPlayBack(finalTarget, mCurrentDictionary, wv);
			}
			else if (AutoBrowsePaused /*自动读时绕过*/ && mCurrentDictionary.bookImpl.hasMdd()) {
				/* 倾向于已经制定发音按钮 */
				BookPresenter finalMCurrentDictionary = mCurrentDictionary;
				wv.evaluateJavascript(WebviewSoundJS, value -> {
					//CMN.Log("WebviewSoundJS", value);
					if (!"10".equals(value)) {
						requestSoundPlayBack(finalTarget, finalMCurrentDictionary, wv);
					}
				});
			} else {
				requestSoundPlayBack(finalTarget, mCurrentDictionary, wv);
			}
		}
	}

	public void postReadEntry() {
		CMN.Log("postReadEntry");
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
		CMN.Log("bThenReadContent");
		if(mWebView!=null){
			mWebView.evaluateJavascript("document.documentElement.innerText", value -> {
				value = StringEscapeUtils.unescapeJava(value.substring(1, value.length() - 1));
				ReadText(value, mWebView);
			});
		}
	}
	
	
	//public WebViewmy getCurrentWebContext() {
	//	if(PeruseViewAttached())
	//		return PeruseView.mWebView;
	//	BookPresenter mdTmp = getCurrentReadContext();
	//	return mdTmp==null?null:mdTmp.mWebView;
	//}
	
	public WebViewmy getCurrentWebContext(boolean getVisibleBackable) {
		View SV = null;
		ScrollViewmy WHP = this.WHP;
		ViewGroup webholder = this.webholder;
		ViewGroup webSingleholder = this.webSingleholder;
		int webholder_childCount = webholder.getChildCount();
		if(popupContentView!=null) {
			ViewGroup SVP = (ViewGroup) popupContentView.getParent();
			//showT(popupContentView.getTranslationY()+" "+SVP.getHeight());
			if (SVP!=null && popupContentView.getTranslationY()<mainF.getHeight()-popupToolbar.getHeight()) {
				return popupWebView;
			}
		}
		if (!isContentViewAttached()) {
			return null;
		}
		if(webholder_childCount !=0) {
			int selectedPos=-1;
			/* 当前滚动高度 */
			int currentHeight= WHP.getScrollY();
			for(int i = 0; i<webholder_childCount; i++) {
				if(webholder.getChildAt(i).getBottom()>currentHeight) {
					/* Frames累加高度 首次超出 滚动高度 */
					selectedPos=i;
					break;
				}
			}
			
			//获得返回之抵消
			if (getVisibleBackable) {
				for(int i = selectedPos; i<webholder_childCount; i++) {
					SV = webholder.getChildAt(i);
					if (SV!=null) {
						if (SV.getTag() instanceof WebViewmy && ((WebViewmy) SV.getTag()).voyagable(true)) {
							return (WebViewmy) SV.getTag();
						}
						if(SV.getTop()>=currentHeight + WHP.getHeight()) {
							/* Frames累加高度 首次超出 显示范围 */
							break;
						}
					}
				}
			}
			
			//应用中线法则
			currentHeight = currentHeight+ WHP.getHeight()/2;
			if(selectedPos>=0){
				while(selectedPos+1<webholder_childCount && webholder.getChildAt(selectedPos).getBottom()<currentHeight){
					selectedPos++;
				}
			}
			
			//UniversalDictionaryInterface bookImpl = (UniversalDictionaryInterface) webholder.getChildAt(selectedPos).getTag();
			//return mdict_cache.get(bookImpl.getDictionaryName());
			SV = webholder.getChildAt(selectedPos);
		}
		else {//单本阅读
			SV = webSingleholder.getChildAt(0);
			//UniversalDictionaryInterface bookImpl = (UniversalDictionaryInterface) SV.getTag();
			//return currentDictionary.bookImpl==bookImpl?currentDictionary
			//		:mdict_cache.get(bookImpl.getDictionaryName());
			//sv = webholder.getChildAt(selectedPos);
		}
		if (SV!=null && SV.getTag() instanceof WebViewmy) {
			return (WebViewmy) SV.getTag();
		}
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
				CMN.Log(e);
			}
		} else {
			opt.supressAudioResourcePlaying=false;
			soundKey=soundsTag+soundKey; /* CMN.Log("发音任务丢给资源拦截器", soundKey); */
			wv.evaluateJavascript(playsoundscript + ("(\"" + soundKey + "\")") , null);
		}
	}

	void setBottomNavigationType(int type, TextView tv) {
		switch(type){
			case 0:
				widget10.setImageResource(R.drawable.chevron_left);
				widget11.setImageResource(R.drawable.chevron_right);
				if(tv!=null) tv.setText(getResources().getTextArray(R.array.btm_navmode)[0]);
			break;
			case 1:
				widget10.setImageResource(R.drawable.chevron_recess);
				widget11.setImageResource(R.drawable.chevron_forward);
				if(tv!=null) tv.setText(getResources().getTextArray(R.array.btm_navmode)[1]);
			break;
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
			peruseView = new PeruseView();
			peruseView.spsubs = opt.defaultReader.getFloat("spsubs", 0.706f);
			peruseView.dm = dm;
			peruseView.opt = opt;
			peruseView.density = dm.density;
			peruseView.addAll = opt.getPeruseAddAll();
		}
		return peruseView;
	}

	void AttachPeruseView(boolean bRefresh) {
		try {
			if(peruseView ==null) return;
			if(!peruseView.isAdded()) {
				//CMN.Log("AttachPeruseView 1 ", bRefresh);
				peruseView.bCallViewAOA=true;
				/* catch : Can not perform this action after onSaveInstanceState */
				peruseView.show(getSupportFragmentManager(), "PeruseView");
			} else if(peruseView.mDialog!=null){
				//CMN.Log("AttachPeruseView 2 ", bRefresh);
				peruseView.mDialog.show();
				peruseView.onViewAttached(this, bRefresh);
			}
		} catch (Exception e) { e.printStackTrace(); }
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
	
	abstract ArrayList<PlaceHolder> getLazyCC();
	abstract ArrayList<PlaceHolder> getLazyCS();
	abstract ArrayList<PlaceHolder> getLazyHC();

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
		BottomSheetDialog _bottomPlaylist = bottomPlaylist==null?null:bottomPlaylist.get();
		if(_bottomPlaylist==null) {
			CMN.Log("重建底部弹出");
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
										} catch (Exception ignored) { CMN.Log(ignored); }
									}
								}
								selection.removeAll(Arrays.asList(selectionArr));
								addNum = selection.size();
								selectionArr = selection.toArray(new Long[addNum]);
								for(Long newFav:selectionArr) {
									try {
										if(prepareHistoryCon().insert(this, text, newFav, webviewholder)>=0){
											addCnt++;
										}
									} catch (Exception ignored) { CMN.Log(ignored); }
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
											if(db.insertUpdate(this, text, null)>0){
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
		AlertDialog d = ChooseFavorDialog==null?null:ChooseFavorDialog.get();
		if(d==null){
			CMN.Log("重建选择器……");
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
				if(getUsingDataV2()) {
					MyPair<String, Long> nb = AppFunAdapter.notebooksV2.get(position);
					name = nb.key;
					long NID = nb.value;
					if (DBrowser!=null) {
						if(reason1!=2 && DBrowser.getFragmentId()==1) {
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
				}
				else {
					MyPair<String, LexicalDBHelper> item = AppFunAdapter.notebooks.get(position);
					name = item.key;
					LexicalDBHelper _favoriteCon = item.value;
					if(_favoriteCon==null){
						_favoriteCon = item.value = new LexicalDBHelper(getApplicationContext(), opt, name, false);
					}
					if(reason1!=2 && DBrowser!=null && DBrowser.getFragmentId()==1){
						// 加载收藏夹
						opt.putCurrFavoriteDBName(name);
						favoriteCon = _favoriteCon;
						DBrowser.loadInAll(this);
					} else if(reason1==2 && DBrowser!=null){
						DBrowser.moveSelectedardsToDataBase(_favoriteCon);
					}
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
	public ImageView[] ContentbarBtns = new ImageView[ContentbarBtnIcons.length];
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
			ada.show();
			ada.onClick(pos);
		}
		ada.main_list.mMaxHeight = (int) (root.getHeight() - root.getPaddingTop() - 4 * getResources().getDimension(R.dimen._50_));
	}
	
	private static final ConcurrentHashMap<String, byte[]> CommonAssets = new ConcurrentHashMap<>(10);
	private static final ConcurrentHashMap<String, String> CommonAssetsStr = new ConcurrentHashMap<>(10);
	static {
		CommonAssets.put("SUBPAGE.js", BookPresenter.jsBytes);
		CommonAssets.put("markloader.js", BookPresenter.markJsLoader);
		CommonAssets.put("dk.js", DarkModeIncantation.getBytes());
		CommonAssetsStr.put("tapTrans.js", BookPresenter.tapTranslateLoader);
	}
	
	public InputStream loadCommonAsset(String key) throws IOException {
		byte[] data = CommonAssets.get(key);
		if(data==null){
			InputStream input = getResources().getAssets().open(key);
			data = new byte[input.available()];
			input.read(data);
			CommonAssets.put(key, data);
		}
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
				CommonAssets.put(key, data);
			}
			if (data!=null) {
				CommonAssetsStr.put(key, ret = new String(data));
			}
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
					bRequestedCleanSearch=bIsFirstLaunch;
					/* 接管历史纪录 */
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
			
			CMN.Log("单本搜索 ： ", normal_idx, normal_idx==-1?"":currentDictionary.bookImpl.getEntryAt(normal_idx<0?(-normal_idx-3):normal_idx), formation_key);
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
								if(isContentViewAttached()&&!isContentViewAttachedForDB())
									proceed = currentDictionary.lvClickPos!=normal_idx;
							} else {
								if(webcontentlist.getVisibility()==View.VISIBLE) {//webSingleholder.getChildCount()!=1
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
			if(normal_idx<0) {
				DetachContentView(false);
			}
		} catch (Exception e) { CMN.Log(e); }
	}
	
	void execBatchSearch(CharSequence cs) {
		if(lianHeTask!=null) {
			lianHeTask.cancel(false);
		}
		//if(lv2.getVisibility()==View.INVISIBLE)
		//	lv2.setVisibility(View.VISIBLE);
		String key = cs.toString();
		if(!key.equals(CombinedSearchTask_lastKey))
			lianHeTask = new CombinedSearchTask(MainActivityUIBase.this).execute(key);
		else {
			if(bIsFirstLaunch) {
				/* 接管历史纪录 */
				bRequestedCleanSearch=bIsFirstLaunch;
				bIsFirstLaunch=false;
				if(recCom.allWebs || !isContentViewAttached() && mdict.processText(key).equals(mdict.processText(String.valueOf(adaptermy2.combining_search_result.getResAt(this, 0)))))
				{
					adaptermy2.onItemClick(null, adaptermy2.getView(0, null, null), 0, 0);
				}
			}
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
		CMN.Log("mergedKeyHeaders", mergedKeyHeaders, ConfidentMergeShift);
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
					&& ADA.lastClickedPosBeforePageTurn>=0
					&& (ADA.webviewHolder.getChildCount()!=0 /*|| false todo 开放连续的历史纪录 ?*/)) {
				if(webview.webScale==0) {
					webview.webScale=dm.density;//sanity check
				}
				//avoyager.set(avoyagerIdx,(int) (current_webview.getScrollY()/(current.webScale/dm.density)));
				
				pagerec = ADA.avoyager.get(ADA.lastClickedPosBeforePageTurn);
				if(pagerec==null) {
					if(webview.getScrollX()!=0 || webview.getScrollY()!=0 ||webview.webScale!= BookPresenter.def_zoom) {
						pagerec=new ScrollerRecord();
						ADA.avoyager.put(ADA.lastClickedPosBeforePageTurn, pagerec);
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
			resultRecorderDiscrete combining_search_result = ADA.combining_search_result;
			ScrollerRecord pagerec;
			OUT:
			if (((resultRecorderCombined) combining_search_result).scrolled
					&& ADA.lastClickedPosBeforePageTurn >= 0
					&& System.currentTimeMillis() - lastClickTime > 300) {
				//CMN.Log("save our postion", lastClickedPosBeforePageTurn, WHP.getScrollY());
				pagerec = ADA.avoyager.get(ADA.lastClickedPosBeforePageTurn);
				if (pagerec == null) {
					if (WHP.getScrollY() != 0) {
						pagerec = new ScrollerRecord();
						ADA.avoyager.put(ADA.lastClickedPosBeforePageTurn, pagerec);
					} else
						break OUT;
				}
				pagerec.set(0, WHP.getScrollY(), 1);
				//CMN.Log("保存位置", lastClickedPosBeforePageTurn);
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
			ScrollerRecord pagerec;
			long deltaT = System.currentTimeMillis() - lastClickTime;
			int lastClickedPos = (int) webview.currentPos; // ADA.lastClickedPos
			if (ADA == adaptermy2) {
				if (deltaT > 400 && lastClickedPos >= 0) {
					//avoyager.set(avoyagerIdx, WHP.getScrollY());
					pagerec = ADA.avoyager.get(lastClickedPos);
					if (pagerec == null && WHP.getScrollY() != 0) {
						pagerec = new ScrollerRecord();
						ADA.avoyager.put(lastClickedPos, pagerec);
					}
					if (pagerec != null) {
						pagerec.set(0, WHP.getScrollY(), 1);
					}
					//CMN.Log("保存位置(回退)", lastClickedPos, WHP.getScrollY());
				}
			}
			else {
				if (webview != null
						&& deltaT > 300
						&& !webview.isloading //
						&& ADA.lastClickedPos >= 0
						&& ADA.webviewHolder.getChildCount() != 0 ) {
					if (webview.webScale == 0) webview.webScale = dm.density;//sanity check
					pagerec = ADA.avoyager.get(lastClickedPos);
					if(webview.SavePagePosIfNeeded(pagerec)) {
						ADA.avoyager.put(lastClickedPos, pagerec);
					}
					//CMN.Log("回退前暂存位置 ", current_webview.getScrollX(), current_webview.getScrollY(), currentDictionary.webScale);
				}
			}
			lastClickTime = System.currentTimeMillis();
		}
	}
	
	public float prepareSingleWebviewForAda(BookPresenter current, WebViewmy current_webview, long pos, BasicAdapter Ada) {
		if(current_webview==null) {
			current_webview = current.mWebView;
		}
		float ret=-1;
		if(opt.getRemPos() && !Ada.shunt) {
			ret = new SaveAndRestorePagePosDelegate().SaveAndRestoreSinglePageForAdapter(current_webview, pos, Ada);
		} else {
			current_webview.expectedPos=0;
			current_webview.expectedPosX=0;
			bRequestedCleanSearch=false;
		}
		
		if(opt.getAutoReadEntry() && !PDICMainAppOptions.getTmpIsAudior(current.tmpIsFlag)
				||!AutoBrowsePaused&&PDICMainAppOptions.getAutoBrowsingReadSomething())
			current_webview.bRequestedSoundPlayback=true;
		
		if(current_webview.fromCombined!=3) {
			current_webview.fromCombined=0;
		}
		
		if(opt.getInheritePageScale())
			ret=current_webview.webScale;
		
		return ret;
	}
	
	
	
	
	@Override
	public long Flag(int flagIndex) {
		return 0;
	}
	
	@Override
	public void Flag(int flagIndex, long val) {
	
	}
	
	@Override
	public int getDynamicFlagIndex(int flagIdx) {
		return 0;
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
		if(settingsPanel!=null) {
			hideSettingsPanel();
			return true;
		}
		if(!AutoBrowsePaused || bRequestingAutoReading){
			stopAutoReadProcess();
			return true;
		}
		if(!bBackBtn && checkWebSelection())
			return true;
		if(opt.getUseBackKeyGoWebViewBack() && !bBackBtn) {
			//CMN.Log("/* 检查返回键倒退网页 */", view, view==null?false:view.canGoBack());
			WebViewmy view = getCurrentWebContext(true);
			if (view==popupWebView) {
				if (popNav(true))
					return true;
			} else if(view!=null && view.voyagable(true)) {
				view.voyage(true);
				return true;
			}
		}
		if(popupContentView!=null) {
			ViewGroup SVP = (ViewGroup) popupContentView.getParent();
			if((!opt.getClickSearchPin() || SVP!=null && popupContentView.getTranslationY()<mainF.getHeight()-popupToolbar.getHeight()) && DetachClickTranslator()) {
				return true;
			}
		}
		if(DBrowser != null) {
			DetachDBrowser();
			return true;
		}
		return false;
	}
	
	public SettingsPanel settingsPanel;
	public PopupWindow   settingsPopup;
	public View.OnClickListener mInterceptorListener;
	public boolean mInterceptorListenerHandled;
	
	public void hideSettingsPanel() {
		if (settingsPanel!=null) {
			settingsPanel.dismiss();
			settingsPanel = null;
		}
		if(settingsPopup!=null) settingsPopup = null;
	}
	
	public void embedPopInCoordinatorLayout(PopupWindow pop) {
		if(bottombar!=null)
			app_panel_bottombar_height = bottombar.getHeight();
		settingsPopup = pop;
		pop.setWidth(dm.widthPixels);
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		pop.setBackgroundDrawable(null);
		int[] vLocation = new int[2];
		ViewGroup root = this.root;
		root.getLocationInWindow(vLocation);
		int topY = vLocation[1];
		int h = root.getHeight();
		pop.setWidth(-1);
		pop.setHeight(h-app_panel_bottombar_height);
		if (pop.isShowing()) {
			pop.update(0, topY, -1, h-app_panel_bottombar_height);
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
	
	public void showMenuGrid() {
		if(bottombar!=null)
			app_panel_bottombar_height = bottombar.getHeight();
		int jd = WeakReferenceHelper.menu_grid;
		MenuGrid menuGrid = (MenuGrid) getReferencedObject(jd);
		if (menuGrid==null) {
			menuGrid = new MenuGrid(this);
			putReferencedObject(jd, menuGrid);
			//CMN.Log("新建MenuGrid...");
		} else {
			menuGrid.refresh();
		}
		if (!menuGrid.isVisible()) {
			menuGrid.toggle(root, null);
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
			return CMN.DummyRef;
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
		//CMN.Log("setScreenOrientation::", ScreenOrientation[idx]);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
			CMN.Log(e);
		}
	}
	
	public void showBookSettings() {
		int jd = WeakReferenceHelper.quick_settings;
		QuickBookSettingsPanel quickSettings
				= (QuickBookSettingsPanel) getReferencedObject(jd);
		if (quickSettings==null) {
			quickSettings = new QuickBookSettingsPanel(this);
			putReferencedObject(jd, quickSettings);
			CMN.Log("重建QuickBrowserSettingsPanel...");
		} else {
			quickSettings.refresh();
		}
		boolean vis = quickSettings.toggle(root, (SettingsPanel) getReferencedObject(WeakReferenceHelper.menu_grid));
		settingsPanel = vis?quickSettings:null;
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
		boolean vis = quickSettings.toggle(root, (SettingsPanel) getReferencedObject(WeakReferenceHelper.menu_grid));
		settingsPanel = vis?quickSettings:null;
	}
	
	
	public void toggleDarkMode() {
		if (thisActType==ActType.PlainDict) {
			((PDICMainActivity) this).drawerFragment.dayNightSwitch.toggle();
		} else {
			boolean isDark = opt.getInDarkMode();
			GlobalOptions.isDark = false;
			opt.setInDarkMode(!isDark);
			changeToDarkMode();
		}
	}
	
	static String lastLoadedModule;
	public void showDictionaryManager() {
		ReadInMdlibs(null);
		AgentApplication app = ((AgentApplication) getApplication());
		app.mdict_cache = mdict_cache;
		//todo remove???
		for(BookPresenter mdTmp:md) {
			if(mdTmp!=null){
				//get path put
				mdict_cache.put(mdTmp.getDictionaryName(),mdTmp);
			}
		}
		for(BookPresenter mdTmp:currentFilter) {
			if(mdTmp!=null){
				mdict_cache.put(mdTmp.getDictionaryName(),mdTmp);
			}
		}
		ArrayList<PlaceHolder> CosyChair = getLazyCC();
		ArrayList<PlaceHolder> CosySofa = getLazyCS();
		ArrayList<PlaceHolder> HdnCmfrt = getLazyHC();
		/* 合符而继统 */
		for(PlaceHolder phI:HdnCmfrt) {
			if(!CosyChair.contains(phI))//todo opt
				CosyChair.add(Math.min(phI.lineNumber, CosyChair.size()), phI);
		}
		app.slots=CosyChair;
		app.opt=opt;
		app.mdlibsCon=mdlibsCon;
		app.mdict_cache=mdict_cache;
		CosySofa.clear();
		HdnCmfrt.clear();
		lastLoadedModule=null;
		Intent intent = new Intent();
		intent.setClass(MainActivityUIBase.this, BookManager.class);
		startActivityForResult(intent, 110);
	}
}
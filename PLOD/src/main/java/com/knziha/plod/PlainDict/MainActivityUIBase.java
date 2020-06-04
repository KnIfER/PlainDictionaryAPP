package com.knziha.plod.PlainDict;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
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
import android.os.ParcelFileDescriptor;
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
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.MyPair;
import com.knziha.plod.dictionary.Utils.ReusableBufferedInputStream;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.SparseArrayMap;
import com.knziha.plod.dictionarymodels.PhotoBrowsingContext;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_asset;
import com.knziha.plod.dictionarymodels.mdict_dsl;
import com.knziha.plod.dictionarymodels.mdict_pdf;
import com.knziha.plod.dictionarymodels.mdict_txt;
import com.knziha.plod.dictionarymodels.mdict_web;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.settings.SettingsActivity;
import com.knziha.plod.slideshow.PhotoViewActivity;
import com.knziha.plod.widgets.AdvancedNestScrollWebView;
import com.knziha.plod.widgets.ArrayAdaptermy;
import com.knziha.plod.widgets.BottomNavigationBehavior;
import com.knziha.plod.widgets.CheckedTextViewmy;
import com.knziha.plod.widgets.CustomShareAdapter;
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
import com.knziha.plod.widgets.SamsungLikeScrollBar;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.TableLayout;
import com.knziha.plod.widgets.TwoColumnAdapter;
import com.knziha.plod.widgets.Utils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.widgets.XYTouchRecorder;
import com.knziha.text.ColoredHighLightSpan;
import com.knziha.text.ScrollViewHolder;
import com.knziha.text.SelectableTextView;
import com.knziha.text.SelectableTextViewBackGround;
import com.knziha.text.SelectableTextViewCover;
import com.knziha.text.TTSMoveToucher;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.imaging.BufferedImage;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.ManagedImageBufferedImageFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import db.LexicalDBHelper;
import db.MdxDBHelper;

import static com.bumptech.glide.util.Util.isOnMainThread;
import static com.knziha.plod.PlainDict.CMN.AssetMap;
import static com.knziha.plod.PlainDict.CMN.AssetTag;
import static com.knziha.plod.PlainDict.MdictServerMobile.getTifConfig;
import static com.knziha.plod.dictionarymodels.mdict.indexOf;
import static com.knziha.plod.slideshow.PdfPic.MaxBitmapRam;
import static com.knziha.plod.widgets.WebViewmy.getWindowManagerViews;

/** 程序基类<br/>
 *  Class for all dictionary activities. <br/>
 * Created by KnIfER on 2018
 */
@SuppressLint({"ResourceType", "SetTextI18bbn","Registered", "ClickableViewAccessibility","PrivateApi","DiscouragedPrivateApi"})
public abstract class MainActivityUIBase extends Toastable_Activity implements OnTouchListener,
		OnLongClickListener,
		OnClickListener,
		OnMenuItemClickListener, OnDismissListener,
		MenuItem.OnMenuItemClickListener,
		OptionProcessor,
		MdictServerLet {
	//private static final String RegExp_VerbatimDelimiter = "[ ]{1,}|\\pP{1,}|((?<=[\\u4e00-\\u9fa5])|(?=[\\u4e00-\\u9fa5]))";
	private static final Pattern RegImg = Pattern.compile("(png$)|(jpg$)|(jpeg$)|(tiff$)|(tif$)|(bmp$)|(webp$)", Pattern.CASE_INSENSITIVE);
	private static Pattern bookMarkEntryPattern = Pattern.compile("entry://@[0-9]*");
	public static final String SEARCH_ACTION   = "colordict.intent.action.SEARCH";
	public static final String SEARCH_ACTION_   = "plaindict.intent.action.SEARCH";
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
	public static final KeyEvent BackEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
	final String entryTag = "entry://";
	final String soundTag = "sound://";
	protected final String soundsTag = "sounds://";
	public boolean hideDictToolbar=false;
	public int pickTarget;
	public boolean isBrowsingImgs;
	public OnLongClickListener mdict_web_lcl;
	public int defbarcustpos;
	public int cbar_key;
	public StringBuilder MainStringBuilder = new StringBuilder(40960);
	boolean bShowLoadErr=true;
	public boolean isCombinedSearching;
	public String CombinedSearchTask_lastKey;
	public HashMap<CharSequence,byte[]> UIProjects;
	public HashSet<CharSequence> dirtyMap;

	public Drawer drawerFragment;
	public DictPicker pickDictDialog;
	public int GlobalPageBackground=-1;
	public SamsungLikeScrollBar mBar;
	private FrameLayout.LayoutParams mBar_layoutParmas;
	public ViewGroup main;
	public ViewGroup second_holder;
	public ViewGroup mainF;
	public ViewGroup webholder;
	public ScrollViewmy WHP;
	public ViewGroup webSingleholder;
	protected WindowManager wm;

	protected String lastEtString;
	public ViewGroup main_succinct;

	public ListViewmy lv,lv2;
	public BasicAdapter adaptermy;
	public BasicAdapter adaptermy2;
	public BasicAdapter adaptermy3;
	public PDICMainActivity.ListViewAdapter2 adaptermy4;
	public BasicAdapter PrevActivedAdapter;
	public BasicAdapter ActivedAdapter;
	public BaseHandler hdl;
	public int  CurrentViewPage = 0;
	public String fontFaces;

	ViewGroup dialogHolder;
	boolean dismissing_dh;
	ViewGroup snack_holder;
	public mdict currentDictionary;
	public ArrayList<mdict> currentFilter = new ArrayList<>();
	public int adapter_idx;
	HashSet<String> mdlibsCon;
	public ArrayList<mdict> md = new ArrayList<>();//Collections.synchronizedList(new ArrayList<mdict>());

	public Dialog taskd;
	DArrayAdapter AppDataAdapter;
	BufferedWriter output;
	BufferedWriter output2;
	public final static String ce_on="document.body.contentEditable=!0";
	public final static String ce_off="document.body.contentEditable=!1";
	LexicalDBHelper favoriteCon;//public LexicalDBHelper getFDB(){return favoriteCon;};
	LexicalDBHelper historyCon;
	/** Use a a filename-directory map to keep a record of lost files so that users could add them back without the need of restore every specific directory.  */
	HashMap<String,String> checker;

	ImageView favoriteBtn;

	SplitView webcontentlist;
	protected IMPageSlider IMPageCover;
	public PeruseView PeruseView;
	public ViewGroup bottombar2;
	public ViewGroup bottombar;

	public ImageView widget7;
	public ImageView widget10;
	public ImageView widget11;
	public ImageView widget12;


	public boolean bRequestedCleanSearch;
	public boolean bWantsSelection;
	public boolean bIsFirstLaunch=true;

	public RLContainerSlider PageSlider;

	public View widget13,widget14;

	public ProgressBar main_progress_bar;
	protected int DockerMarginL,DockerMarginR,DockerMarginT,DockerMarginB;

	boolean isFragInitiated = false;
	MenuItem iItem_FolderAll;
	MenuItem iItem_InPageSearch;
	MenuItem iItem_ClickSearch;

	Canvas mPageCanvas=new Canvas();
	Matrix HappyMatrix = new Matrix();
	BitmapDrawable mPageDrawable;

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
	int popupForceId;
	protected TextView popupTextView;
	protected PopupMoveToucher popupMoveToucher;
	public FlowTextView popupIndicator;
	public RLContainerSlider PopupPageSlider;
	public WebViewmy popupWebView;
	public mdict.AppHandler popuphandler;
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
	WeakReference<DBroswer> DBrowser_holder;
	WeakReference<DBroswer> DHBrowser_holder;
	WeakReference<AlertDialog> ChooseFavorDialog;
	DBroswer DBrowser;
	public PopupGuarder popupGuarder;
	public String currentClickDisplaying;
	public int currentClickDictionary_currentPos;
	public int currentClick_adapter_idx;
	public int CCD_ID;
	public mdict CCD;
	ArrayList<myCpr<String, int[]>> popupHistory = new ArrayList<>();
	int popupHistoryVagranter=-1;
	ViewGroup PhotoPagerHolder;
	ViewPager PhotoPager;
	ImageView PhotoCover;
	ArrayList<String> PhotoUrls = new ArrayList<>();
	PagerAdapter PhotoAdapter;
	LinkedList<PhotoViewActivity.PhotoHolder> mViewCache = new LinkedList<>();
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
	private Runnable mFeetHeightScalerRunnable=new Runnable() {
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
	@Multiline()
	private static final String js_no_match="js_no_match";
	private boolean bHasDedicatedSeachGroup;
	private File fontlibs;
	public static int PreferredToolId=-1;
	private Runnable mOpenImgRunnable;
	private Drawable mActiveDrawable;
	private Drawable mRatingDrawable;
	private int CurrentDictInfoIdx;
	private int StarLevelStamp;
	
	public boolean checkWebSelection() {
		if(getCurrentFocus() instanceof WebViewmy && opt.getUseBackKeyClearWebViewFocus()){
			WebViewmy wv = ((WebViewmy)getCurrentFocus());
			if(wv.bIsActionMenuShown){
				wv.clearFocus();
				return true;
			}
		}
		return false;
	}

	public HashMap<String,mdict> mdict_cache = new HashMap<>();
	protected int filter_count;
	protected int hidden_count;
	@Multiline
	private static final String PDFPage="PAGE";
	public resultRecorderCombined recCom;
	private PdfiumCore pdfiumCore;
	private HashMap<String, PdfDocument> cached_pdf_docs;
	protected boolean forbidVolumeAjustmentsForTextRead;
	private ColoredHighLightSpan timeHLSpan;
	private static final float[] TTS_LEVLES_SPEED = new float[]{0.25f, 0.75f, 1f, 1.25f, 1.75f, 2f, 2.5f, 2.75f, 4f};
	private int TTSSpeed = 2;
	private int TTSPitch = 2;
	private float TTSVolume = 1.f;
	private ViewGroup TTSController_controlBar;
	private WebViewmy mCurrentReadContext;
	protected boolean updateAI=true;

	public void jump(int pos,mdict md) {

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
			mdict mdTmp = md.get(i);
			if(mdTmp!=null) {
				mdTmp.Reload();
			} else {
				md.set(i, new_mdict(phTmp.getPath(opt), this));
			}
			showT("重新加载!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void switch_To_Dict_Idx(int i, boolean invalidate, boolean putName) {
		updateAI = true;
		if(md.size()>0) {
			if(i<0) i=0;
			if(i>=md.size()) i=md.size()-1;
			currentDictionary = md.get(adapter_idx=i);
			if (currentDictionary == null) {
				ArrayList<PlaceHolder> CosyChair = getLazyCC();
				if (i < CosyChair.size()) {
					PlaceHolder phTmp = CosyChair.get(i);
					try {
						md.set(i, currentDictionary = new_mdict(phTmp.getPath(opt), this));
						currentDictionary.tmpIsFlag = phTmp.tmpIsFlag;
					} catch (Exception e) {
						if(GlobalOptions.debug) CMN.Log(e);
						invalidate = systemIntialized;
						if (bShowLoadErr) {
							phTmp.ErrorMsg = e.getLocalizedMessage();
							show(R.string.err);
						}
					}
				}
			}
			if (invalidate) {
				adaptermy.notifyDataSetChanged();
				if (currentDictionary != null) {
					if(putName) {
						setLastMdFn(currentDictionary.getDictionaryName());
					}
					//lv.setSelection(currentDictionary.lvPos);
					lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
				}
			}
		}
	}

	public void switchToSearchModeDelta(int i) {
	
	}
	
	public int md_getSize(){
		return md.size();
	}
	
	public mdict md_get(int i) {
		try {
			mdict ret = md.get(i);
			if(ret==null){
				ArrayList<PlaceHolder> CosyChair = getLazyCC();
				if(i<CosyChair.size()) {
					PlaceHolder phTmp = CosyChair.get(i);
					if (phTmp != null) {
						File path = phTmp.getPath(opt);
						try {
							md.set(i, ret = new_mdict(path, this));
							ret.tmpIsFlag = phTmp.tmpIsFlag;
						} catch (Exception e) {
							if (bShowLoadErr && isOnMainThread())
								show(R.string.err, phTmp.getName(), path, e.getLocalizedMessage());
						}
					}
				}
			}
			return ret;
		} catch (Exception e) { CMN.Log(e); }
		return null;
	}

	public String md_getName(int i) {
		if(i>=0 && i<md.size()){
			mdict mdTmp = md.get(i);
			if(mdTmp!=null) return mdTmp.getDictionaryName();
			ArrayList<PlaceHolder> CosyChair = getLazyCC();
			if(i<CosyChair.size()){
				PlaceHolder placeHolder = CosyChair.get(i);
				if(placeHolder!=null) {
					String name = placeHolder.pathname;
					if(name!=null && name.startsWith(AssetTag) && AssetMap.containsKey(name)) {
						name = AssetMap.get(name);
					}
					return name;
				}
			}
		}
		return "Error!!!";
	}
	
	public void md_set_StarLevel(int i, int val) {
		long flag=0;
		if(i>=0 && i<md.size()){
			mdict mdTmp = md.get(i);
			if(mdTmp!=null) {
				flag = mdTmp.getFirstFlag();
				mdTmp.setFirstFlag(flag=PDICMainAppOptions.setDFFStarLevel(flag, val));
				mdTmp.dumpViewStates(UIProjects);
			} else {
				ArrayList<PlaceHolder> CosyChair = getLazyCC();
				if(i<CosyChair.size()) {
					PlaceHolder placeHolder = CosyChair.get(i);
					if(placeHolder!=null) {
						CharSequence name = placeHolder.getName();
						flag =  md_get_firstFlag_internal(name);
						flag = PDICMainAppOptions.setDFFStarLevel(flag, val);
						md_set_firstFlag_internal(name, flag);
						md_mark_dirty(name);
					}
				}
			}
			CMN.Log("新的星级", md_getName(i), PDICMainAppOptions.getDFFStarLevel(flag), val);
		}
	}
	
	private void md_mark_dirty(CharSequence name) {
		dirtyMap.add(name);
	}
	
	
	public int md_get_StarLevel(int i) {
		long flag = 0;
		if(i>=0 && i<md.size()){
			mdict mdTmp = md.get(i);
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
		byte[] data = UIProjects_get(name);
		int pad = 8*4+3+ConfigExtra;
		if(data.length>pad+8){
			IU.writeLong(data, pad, val);
		}
	}
	
	private long md_get_firstFlag_internal(CharSequence name) {
		byte[] data = UIProjects.get(name);
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
	
	private byte[] UIProjects_get(CharSequence name) {
		byte[] data = UIProjects.get(name);
		if(data==null){
			data = new byte[ConfigSize + ConfigExtra];
			UIProjects.put(name.toString(), data);
		}
		return data;
	}
	
	public CharSequence md_getAbout_Trim(int i) {
		ArrayList<PlaceHolder> placeHolders = getLazyCC();
		PlaceHolder phTmp = placeHolders.get(i);
		mdict mdTmp = md.get(i);
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
			if(show_info_codec && mdTmp!=null) {
				if(!show_info_reload) {
					sb.append("编码");
				}
				sb.append("：");
				sb.append(mdTmp.getCharsetName());
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
		
		CharSequence ret=mdTmp==null? "未加载"
				:HtmlCompat.fromHtml(mdTmp.getAboutString(), HtmlCompat.FROM_HTML_MODE_COMPACT);
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
				sb.append("\t").append("错误信息：");
				sb.append(msg);
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
		mdict mdTmp = md.get(i);
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
		Menu menu = null;
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
			root.postDelayed(mFeetHeightScalerRunnable, 350);
		}

		//if(menu!=null)
	}

	@Nullable
	@Override
	public View getCurrentFocus() {
		if(PeruseViewAttached())
			return PeruseView.mDialog.getCurrentFocus();
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
						word = MakeCompatableHtmlWord(word);
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

	public String MakeCompatableHtmlWord(String word) {
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
		if(PeruseViewAttached())
			PeruseView.prepareInPageSearch(content, true);
		else
			prepareInPageSearch(content, true);
	}

	void HandleSearch(String content) {
		if(PeruseViewAttached())
			PeruseView.etSearch.setText(content);
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
	    CMN.instanceCount++;
	    //CMN.Log("instanceCount", CMN.instanceCount);
		super.onCreate(savedInstanceState);
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
		if(PDICMainAppOptions.getEnableWebDebug()){
			WebView.setWebContentsDebuggingEnabled(true);
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

	protected abstract int getVisibleHeight();

	public void fix_dm_color() {
		CMN.Log("fix_dm_color");
		boolean isDark = GlobalOptions.isDark;
		boolean nii=widget12.getTag(R.id.image)==null;
		ViewGroup[] holders = new ViewGroup[]{webSingleholder, webholder};
		for (ViewGroup hI : holders) {
			for (int i = 0; i < hI.getChildCount(); i++) {
				Object tag = hI.getChildAt(i).getTag();
				if(tag instanceof Integer){
					int selfAtIdx = (int) tag;
					if(selfAtIdx>=0&&selfAtIdx<md.size()) {
						mdict mdTmp = md.get(selfAtIdx);
						WebViewmy wv = mdTmp.mWebView;
						if(wv!=null) {
							wv.evaluateJavascript(isDark ? DarkModeIncantation : DeDarkModeIncantation, null);
							mdTmp.tintBackground(wv);
						}
					}
				}
			}
		}
		if(PeruseView!=null) {
			PeruseView.refreshUIColors(MainBackground);
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

	public void popupWord(final String key, int forceStartId, int frameAt) {
		//CMN.Log("popupWord frameAt", frameAt);
		if(key==null || mdict.processText(key).length()>0) {
			popupKey = key;
			popupFrame = frameAt;
			popupForceId = forceStartId;
			if(mPopupRunnable==null){
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
						ViewGroup targetRoot = bPeruseViewAttached?PeruseView.root:root;
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
							popupContentView.setOnClickListener(Utils.DummyOnClick);
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
							if(popupForceId>=0 && popupForceId<size)
								CCD_ID = popupForceId;
							//轮询开始
							mdict_web webx = null;
							boolean use_morph = PDICMainAppOptions.getClickSearchUseMorphology();
							int SearchMode = PDICMainAppOptions.getClickSearchMode();
							CMN.Log("SearchMode", SearchMode);
							boolean bForceJump = false;
							if (SearchMode == 2) {/* 仅搜索当前词典 */
								CCD = md_get(CCD_ID);
								if (CCD != null) {
									if (CCD instanceof mdict_web) {
										webx = (mdict_web) CCD;
										if (!webx.takeWord(popupKey)) {
											webx = null;
										}
									} else {
										idx = CCD.lookUp(popupKey, true);
										if (idx < -1 && use_morph) {
											keykey = ReRouteKey(popupKey, true);
											if (keykey != null) idx = CCD.lookUp(keykey, true);
										}
									}
								}
							}
							else {
								boolean proceed = true;
								if (SearchMode == 1) {/* 仅搜索指定点译词典 */
									bHasDedicatedSeachGroup=false;
									mdict firstAttemp = null;
									FindCSD:
									while(true) {
										mdict mdTmp;
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
																md.set(CSID, mdTmp = new_mdict(phTmp.getPath(opt), MainActivityUIBase.this));
																mdTmp.tmpIsFlag = phTmp.tmpIsFlag;
															} catch (Exception e) {
															}
														}
													}
												}
											}
											if (mdTmp != null) {
												if (!bForceJump && firstAttemp == null)
													firstAttemp = mdTmp;
												bHasDedicatedSeachGroup=true;
												proceed=false;
												if (mdTmp instanceof mdict_web) {
													webx = (mdict_web) mdTmp;
													if (bForceJump || webx.takeWord(popupKey)) {
														break;
													}
													webx = null;
												} else {
													idx = mdTmp.lookUp(popupKey, true);
													if (idx < -1 && use_morph) {
														keykey = ReRouteKey(popupKey, true);
														if (keykey != null)
															idx = mdTmp.lookUp(keykey, true);
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
														md.set(CCD_ID, CCD = new_mdict(phTmp.getPath(opt), MainActivityUIBase.this));
														CCD.tmpIsFlag = phTmp.tmpIsFlag;
													} catch (Exception e) {
														CMN.Log(e);
													}
												}
											}
										}
										if (CCD instanceof mdict_web) {
											webx = (mdict_web) CCD;
											if (webx.takeWord(popupKey)) {
												break;
											}
											webx = null;
										} else if (CCD != null) {
											idx = CCD.lookUp(popupKey, true);
											if (idx < 0) {
												if (!reject_morph && use_morph) {
													keykey = ReRouteKey(popupKey, true);
													if (keykey != null)
														idx = CCD.lookUp(keykey, true);
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
								webx.searchKey = popupKey;
								idx = 0;
							}

							//CMN.Log(CCD, "应用轮询结果", webx, idx);

							if (idx >= 0 && CCD != null) {
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
								CCD.renderContentAt(-1, CCD_ID, -1, popupWebView, currentClickDictionary_currentPos = idx);
							}

							currentClickDisplaying = popupKey;
							decorateContentviewByKey(popupStar, currentClickDisplaying);
							if (!PDICMainAppOptions.getHistoryStrategy0() && PDICMainAppOptions.getHistoryStrategy10())
								insertUpdate_histroy(popupKey);
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
							//CMN.Log("poping up ::: ", ActivedAdapter);
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
										now = PeruseView.getWebTouchY();
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
									if(PDICMainAppOptions.getEnableSuperImmersiveScrollMode()){
										now += webcontentlist.getTop();
									} else {
										now += ((ViewGroup.MarginLayoutParams) getContentviewSnackHolder().getLayoutParams()).topMargin;
									}
									float pad = 56 * dm.density;
									if (MainActivityUIBase.this instanceof FloatSearchActivity)
										now += ((FloatSearchActivity) MainActivityUIBase.this).getPadHoldingCS();
									//CMN.Log("now",now);
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
	
	private void init_popup_view() {
		if (popupWebView == null) {
			popupContentView = (ViewGroup) getLayoutInflater()
					.inflate(R.layout.float_contentview_basic, root, false);
			popupContentView.setOnClickListener(Utils.DummyOnClick);
			popupToolbar = (ViewGroup) popupContentView.getChildAt(0);
			PopupPageSlider = (RLContainerSlider) popupContentView.getChildAt(1);
			WebViewmy mPopupWebView = (WebViewmy) PopupPageSlider.getChildAt(0);
			mPopupWebView.fromCombined = 2;
			PopupPageSlider.WebContext = mPopupWebView;
			popupBottombar = (ViewGroup) popupContentView.getChildAt(2);
			popuphandler = new mdict.AppHandler(currentDictionary);
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
			
			// 点击背景
			popupGuarder = new PopupGuarder(getBaseContext());
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
	
	public void DetachClickTranslator() {
		if(popupContentView!=null){
			ViewGroup svp = (ViewGroup) popupContentView.getParent();
			if(svp!=null) {
				svp.removeView(popupContentView);
				popupContentView = null;
				popupGuarder.setVisibility(View.GONE);
			}
		}
	}

	public void postDetachClickTranslator() {
		root.post(() -> DetachClickTranslator());
	}

	public @Nullable String ReRouteKey(String key, boolean bNullable){
		int size = currentFilter.size();
		if (size>0) {
			//CMN.Log("ReRouteKey ??" , key);
			for (int i = 0; i < size; i++) {
				mdict mdTmp = currentFilter.get(i);
				if(mdTmp==null){
					ArrayList<PlaceHolder> CosySofa = getLazyCS();
					if(i<CosySofa.size()){
						PlaceHolder phI = CosySofa.get(i);
						try {
							currentFilter.set(i, mdTmp=new_mdict(phI.getPath(opt), this));
							mdTmp.tmpIsFlag=phI.tmpIsFlag;
						} catch (Exception e) { CMN.Log(e); }
					}
				}
				if(mdTmp!=null)
				try {
					Object rerouteTarget = mdTmp.ReRoute(key);
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

	public void notifyDictionaryDatabaseChanged(mdict mdx) {
		if(currentDictionary==mdx){
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
		if(webSingleholder.getVisibility()!=View.VISIBLE)webSingleholder.setVisibility(View.VISIBLE);
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
		AgentApplication app = ((AgentApplication) getApplication());
		HashMap<CharSequence, byte[]> mdict_projects = app.UIProjects;
		try {
			File SpecificationFile = opt.SpecificationFile;
			FileOutputStream fout = new FileOutputStream(SpecificationFile);
			ReusableByteOutputStream bos = new ReusableByteOutputStream(SpecificationBlockSize);
			byte[] source = bos.getBytes();
			int count=0;
			boolean init=false;
			byte[] name, data;
			for(CharSequence kI:mdict_projects.keySet()){
				//CMN.Log("写入……kI=", kI);
				if(!init){
					name = Integer.toString(ConfigSize).getBytes(StandardCharsets.UTF_8);
					data = "/\n".getBytes();
					bos.write(name);
					bos.write(data);
					count+=name.length+data.length;
					init = true;
				}
				name = kI.toString().getBytes(StandardCharsets.UTF_8);
				data = mdict_projects.get(kI);
				if(data!=null) {
					if(data.length-ConfigExtra< ConfigSize){
						byte[] newData = new byte[ConfigSize + ConfigExtra];
						System.arraycopy(data, 0, newData, 0, data.length);
						mdict_projects.put(kI, data = newData);
					}
					int ReadOffset=count+name.length+targetCount;
					int size = name.length + data.length + 2 - ConfigExtra;
					if (count + size > SpecificationBlockSize) { //溢出写入
						Arrays.fill(source, count, SpecificationBlockSize, (byte) 0);
						fout.write(source);
						fout.flush();
						bos.reset();
						count = 0;
					}
					bos.write(name);
					bos.write("/".getBytes());
					bos.write(data, ConfigExtra, data.length-ConfigExtra);
					bos.write("\n".getBytes());
					for (int k = 0; k < 4; k++) {
						data[k] = (byte) (ReadOffset>>(k*8)&0xff);
					}
					data[5]=0;
					count += size;
				}
			}
			if(count>0 && count<=SpecificationBlockSize) { //正常写入
				fout.write(source ,0, count);
				fout.flush();
				fout.close();
			}
			mConfigSize=ConfigSize;
			CMN.LastConfigReadTime = System.currentTimeMillis();
		}
		catch (Exception e){
			CMN.Log(e);
		}
	}

	/** Save just one. */
	public boolean SolveOneUIProject(CharSequence fname) {
		CMN.Log("solveOnUIProject");
		try {
			byte[] name = fname.toString().getBytes(StandardCharsets.UTF_8);
			byte[] data = UIProjects.get(fname);
			if(data!=null){
				File SpecificationFile = opt.SpecificationFile;
				if(SpecificationFile.length()==0) return false;
				int RealOffset=0;
				for (int k = 0; k < 4; k++)
					RealOffset |= (data[k]&0xff)<<(k*8);
				if(RealOffset<1){ //append
					CMN.Log("追加成功???");
					FileOutputStream fout = new FileOutputStream(SpecificationFile, true);
					int size = name.length + data.length + 2;
					long count = SpecificationFile.length();
					long left = SpecificationBlockSize - count%SpecificationBlockSize;
					if(left<size)//填充0
						for (int i = 0; i < left; i++) fout.write(0);
					fout.write(name);
					fout.write(target);
					count += name.length + targetCount;
					for (int k = 0; k < 4; k++)
						data[k] = (byte) (count>>(k*8)&0xff);
					fout.write(data, ConfigExtra, data.length-ConfigExtra);
					fout.write(separator);
					CMN.Log("追加成功！");
					CMN.LastConfigReadTime = System.currentTimeMillis();
					return true;
				} else {  //in place re-write
					RandomAccessFile raf = new RandomAccessFile(SpecificationFile, "rw");
					raf.seek(RealOffset-1);
					if(raf.read()==(target[targetCount-1]&0xff)){//sanity check A
						raf.write(data, ConfigExtra, data.length-ConfigExtra);
						CMN.Log("写入成功???");
						if(raf.read()==(separator[0]&0xff)){//sanity check B
							CMN.Log("写入成功!!!");
							CMN.LastConfigReadTime = System.currentTimeMillis();
							return true;
						}
					}
					raf.close();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		CMN.Log("override !!!");
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
				mAutoReadProgressAnimator.addListener(new Animator.AnimatorListener() {
					@Override public void onAnimationStart(Animator animation) {  }
					@Override public void onAnimationCancel(Animator animation) {  }
					@Override public void onAnimationRepeat(Animator animation) { }
					@Override public void onAnimationEnd(Animator animation) {
						performAutoReadProcess();
					}
				});
			}
			if(mAutoReadProgressView.getParent()==null){
				d.findDrawableByLayerId(android.R.id.background).setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
				d.findDrawableByLayerId(android.R.id.progress).setColorFilter(ColorUtils.blendARGB(MainBackground, Color.BLACK, 0.28f), PorterDuff.Mode.SRC_IN);
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
			if(mAutoReadProgressView.getParent()!=null)
				((ViewGroup)mAutoReadProgressView.getParent()).removeView(mAutoReadProgressView);
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
	public void performAutoReadProcess(){
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
		if(mi!=null && !mi.isActionButton()) toolbar.getMenu().close();
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
		getLastPlanName();
		new File(opt.pathToDatabases().toString()).mkdirs();
		opt.CheckFileToDefaultMdlibs();
	}

	public static byte[] target = "/".getBytes(StandardCharsets.UTF_8);
	public static byte[] separator = "\n".getBytes(StandardCharsets.UTF_8);
	public static int separatorCount = separator.length;
	public static int targetCount = target.length;
	public static int ConfigSize = 64;
	public static int ConfigExtra = 5;
	public static int SpecificationBlockSize = 4096;

	public int mConfigSize = 0;

	@Override
	protected void further_loading(Bundle savedInstanceState) {
		super.further_loading(savedInstanceState);
		GlobalOptions.isLarge = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=3 ;
		//CMN.show("isLarge"+isLarge);
		mdict.def_zoom=dm.density;
		mdict.optimal100 = GlobalOptions.isLarge ?150:125;
		mdict.def_fontsize = opt.getDefaultFontScale(mdict.optimal100);
		
		File ConfigFile = opt.fileToConfig();
		
		ConfigFile.mkdirs();
		
		iItem_InPageSearch.setVisible(false);
		if(iItem_ClickSearch!=null) {
			iItem_ClickSearch.setVisible(false);
		}
		

		AgentApplication app = ((AgentApplication) getApplication());
		UIProjects = app.UIProjects;
		dirtyMap = app.dirtyMap;
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
			if(fontNames.size()>0){
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
		CMN.Log(CMN.LastConfigReadTime, SpecificationFile.lastModified());
		if(SpecificationFile.exists()){ // 读取逐词典配置。
			if(CMN.LastConfigReadTime<SpecificationFile.lastModified()) {
				if(!(CMN.bForbidOneSpecFile = SpecificationFile.isDirectory()))
				try {
					//todo 文件大于 Integer.MAX_VALUE 时，应用银河软件标准。
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
													dirtyMap.add("1");
													dirtyMap.add("2");
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
												app.UIProjects.put(fn, data);
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

		final File def = getStartupFile(ConfigFile);      //!!!原配
		final boolean retrieve_all=!def.exists();

		ArrayList<PlaceHolder> CC = getLazyCC();
		if(retrieve_all) {
			try {
				String path = CMN.AssetTag + "liba.mdx";
				md.add(new mdict_asset(new File(path), this));
				CC.add(new PlaceHolder(path, CC));
			} catch (IOException e) {
				CMN.Log(e);
			}
		}
		else try {
			//todo 实现词典懒加载
			boolean lazyLoad = opt.getLazyLoadDicts();
			LoadLazySlots(def, lazyLoad, getLastPlanName());
			buildUpDictionaryList(lazyLoad, null);
		} catch (IOException e2) { e2.printStackTrace(); }

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
							mdict mdtmp = new mdict(i, this, 0, null);
							md.add(mdtmp);
							CC.add(new PlaceHolder(i.getName(), CC));
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

		switch_To_Dict_Idx(adapter_idx, false, false);

		root = findViewById(R.id.root);
		mainF = findViewById(R.id.mainF);
		bottombar = findViewById(R.id.bottombar);
		findViewById(R.id.toolbar_action1).setOnClickListener(this);
		toolbar.setOnMenuItemClickListener(this);
		
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		actionBarSize = TypedValue.complexToDimensionPixelSize(typedValue.data, dm);
		if(actionBarSize<=0) actionBarSize=(int) (56*dm.density);

		webholder = contentview.findViewById(R.id.webholder);
		WHP = (ScrollViewmy) webholder.getParent();
		webSingleholder = contentview.findViewById(R.id.webSingleholder);

		mBar = contentview.findViewById(R.id.dragScrollBar);
		mBar_layoutParmas = (FrameLayout.LayoutParams) mBar.getLayoutParams();
		mBar.setOnProgressChangedListener(_mProgress -> {
			if(PageSlider==null) return;
			PageSlider.TurnPageSuppressed = _mProgress==-1;
		});

		webcontentlist.multiplier=-1;
		webcontentlist.isSlik=true;

		CachedBBSize=(int)Math.max(20*dm.density, Math.min(CachedBBSize, getResources().getDimension(R.dimen._bottombarheight_)));
		webcontentlist.setPrimaryContentSize(CachedBBSize,true);

		webcontentlist.setPageSliderInf(new SplitView.PageSliderInf() {//这是底栏的动画特效
			int height;
			@Override
			public void onPreparePage(int val) {
				View IMPageCover_ = getIMPageCover();
				LayoutParams lpp = IMPageCover_.getLayoutParams();
				if(PeruseViewAttached())
					IMPageCover_ = PeruseView.IMPageCover;
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
					IMPageCover_=PeruseView.IMPageCover;
					PageSlider_=PeruseView.PageSlider;
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
				boolean bPeruseIncharge = PeruseViewAttached() && (PeruseView.contentview.getParent()==PeruseView.slp || PeruseView.contentview.getParent()==PeruseView.mlp);
				if(PeruseViewAttached())
					IMPageCover_=PeruseView.IMPageCover;
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
				if(PeruseView!=null && PeruseView.getView()!=null && PeruseView.getView().getParent()!=null)
					IMPageCover_=PeruseView.IMPageCover;
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
				boolean bPeruseIncahrge = PeruseViewAttached() && (PeruseView.contentview.getParent()==PeruseView.slp || PeruseView.contentview.getParent()==PeruseView.mlp);
				int ret = (int) Math.max(((bPeruseIncahrge&&!opt.getPeruseBottombarOnBottom())?30:20)*dm.density, Math.min(getResources().getDimension(R.dimen._bottombarheight_), size));//50*dm.density
				CMN.Log(ret);
				if(bPeruseIncahrge) {
					PeruseView.CachedBBSize = ret;
				}else{
					CachedBBSize = ret;
					webcontentlist.isDirty=true;
				}
				return ret;
			}
		});

		bottombar2.setBackgroundColor(MainBackground);

		ivDeleteText = findViewById(R.id.ivDeleteText);
		ivBack = findViewById(R.id.ivBack);
		ivDeleteText.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		findViewById(R.id.pad).setOnClickListener(Utils.DummyOnClick);

		boolean tint = PDICMainAppOptions.getTintIconForeground();
		for (int i = 0; i < 6; i++) {
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
		if(appproject!=null) {
			contentbar_project = new AppUIProject(contentkey, ContentbarBtnIcons, ContentbarBtnIds, appproject, bottombar2, ContentbarBtns);
			contentbar_project.type = cbar_key;
			RebuildBottombarIcons(contentbar_project, mConfiguration);
		}

		(widget13=PageSlider.findViewById(R.id.browser_widget13)).setOnClickListener(this);
		(widget14=PageSlider.findViewById(R.id.browser_widget14)).setOnClickListener(this);

		if(isCombinedSearching)
			toolbar.getMenu().findItem(R.id.toolbar_action1).setIcon((ContextCompat.getDrawable(this,R.drawable.ic_btn_multimode)));
		//if(opt.isShowDirectSearch()) ((MenuItem)toolbar.getMenu().findItem(R.id.toolbar_action2)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

		etSearch = findViewById(R.id.etSearch);

		main_succinct = findViewById(R.id.mainframe);
		main_progress_bar = findViewById(R.id.main_progress_bar);
	}

	protected abstract View getIMPageCover();

	protected File getStartupFile(File ConfigFile){
		File def = new File(getExternalFilesDir(null),"default.txt");
		if(def.length()<=0){
			def = opt.fileToSet(ConfigFile, opt.getLastPlanName());
		}
		return def;
	}

	protected abstract String getLastPlanName();

	protected abstract void setLastPlanName(String setName);

	protected abstract String getLastMdFn();

	public abstract void setLastMdFn(String setName);

	void buildUpDictionaryList(boolean lazyLoad, HashMap<String, mdict> mdict_cache) {
		ArrayList<PlaceHolder> CosyChair = this instanceof PDICMainActivity?PDICMainActivity.CosyChair:getLazyCC();
		ArrayList<PlaceHolder> CosySofa = this instanceof PDICMainActivity?PDICMainActivity.CosySofa:getLazyCS();
		ArrayList<PlaceHolder> HdnCmfrt = this instanceof PDICMainActivity?PDICMainActivity.HdnCmfrt:getLazyHC();
		currentFilter.ensureCapacity(filter_count);
		currentFilter.clear();
		md.ensureCapacity(CosyChair.size());
		md.clear();
		adapter_idx=-1;
		PlaceHolder phI;
		String lastName = getLastMdFn();
		for (int i = 0; i < CosyChair.size(); i++) {
			phI = CosyChair.get(i);
			mdict mdTmp = mdict_cache==null?null:mdict_cache.get(phI.getPath(opt));
			if ((phI.tmpIsFlag&0x8)!=0){
				HdnCmfrt.add(phI); /* 隐·1 */
				CosyChair.remove(i--);
				continue;
			}
			if(mdTmp==null && !lazyLoad) { // 大家看 这里有个老实人
				try {
					mdTmp = new_mdict(phI.getPath(opt), this);
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
	
	private boolean md_name_match(String lastName, mdict mdTmp, PlaceHolder phI) {
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
			checker = new HashMap<>();
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
							checker.put(check.getName(),check.getPath());
					}
					in.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			else {
				rec.getParentFile().mkdirs();
				try {
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
					final int lalaX=IU.parsint(v.getTag(R.id.toolbar_action1));
					final int lalaY=IU.parsint(v.getTag(R.id.toolbar_action2));
					if(lalaY!=-1 && lalaX!=-1) {
						v.scrollTo(lalaX, lalaY);
						v.setLayoutParams(v.getLayoutParams());
						//CMN.Log("scrolling to: "+lalaY);
					}
				}
				
				boolean fromPeruseView=webview.fromCombined==3;
				SamsungLikeScrollBar _mBar=mBar;
				float currentScale = webview.webScale;
				//tofo
				if(currentScale>=mdict.def_zoom) {
					if(fromPeruseView) {
						_mBar= PeruseView.mBar;
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
				}
				iItem_FolderAll.setVisible(false);//折叠
				iItem_InPageSearch.setVisible(false);
				if (iItem_ClickSearch != null)
					iItem_ClickSearch.setVisible(false);
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

	public boolean checkDicts() {
		if(md.size()>0) {
			if(currentDictionary==null){
				currentDictionary=md.get(adapter_idx=(adapter_idx<0||adapter_idx>=md.size())?0:adapter_idx);
				if(currentDictionary==null){
					PlaceHolder phI = getPlaceHolderAt(adapter_idx);
					if(phI!=null) {
						try {
							md.set(adapter_idx, currentDictionary=MainActivityUIBase.new_mdict(phI.getPath(opt), this));
							currentDictionary.tmpIsFlag = phI.tmpIsFlag;
						} catch (Exception ignored) { }
					}
				}
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
		if((!AutoBrowsePaused || bRequestingAutoReading) && !opt.getTTSBackgroundPlay()){
			stopAutoReadProcess();
		}
		if(Utils.mNestedScrollingChildHelper!=null){
			Utils.mNestedScrollingChildHelper.setCurrentView(null);
		}
	}

	@Override
	protected void onDestroy(){
		CMN.instanceCount--;
		if(systemIntialized) {
			for(mdict mdTmp:md) {
				if(mdTmp!=null) {
					if (mdTmp.isDirty)
						mdTmp.dumpViewStates(UIProjects);
					mdTmp.unload();
				}
			}
			md.clear();
			webSingleholder.removeAllViews();
			webholder.removeAllViews();

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
			hdl.clearActivity();
			WeakReference[] holders = new WeakReference[]{popupCrdCloth, popupCmnCloth, setchooser, bottomPlaylist, DBrowser_holder, DHBrowser_holder};
			for(WeakReference hI:holders){
				if(hI!=null)
					hI.clear();
			}
		}
		if(CMN.instanceCount<0) CMN.instanceCount=0;
		super.onDestroy();
	}

	public void notifyGLobalFSChanged(int targetLevel) {
		mdict.def_fontsize=targetLevel;
		for(int i=0;i<webholder.getChildCount();i++) {
			View cv = webholder.getChildAt(i);
			if(cv!=null) {
				if(cv.getTag()!=null) {
					try {
						int dictIdx = (int)cv.getTag();
						mdict mdTmp = md.get(dictIdx);
						if(!mdTmp.getUseInternalFS())
							mdTmp.mWebView.getSettings().setTextZoom(targetLevel);
					} catch (Exception ignored) {}
				}
			}
		}
		opt.putDefaultFontScale(mdict.def_fontsize);
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
		futton.setActivated(prepareFavoriteCon().contains(key));
	}
	
	private void CheckInternalDataBaseDirExist() {
		File checkDirs = opt.fileToDatabaseFavorites();
		checkDirs.mkdirs();
	}
	
	protected LexicalDBHelper prepareFavoriteCon() {
		if(favoriteCon!=null) return favoriteCon;
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
			CheckInternalDataBaseDirExist();
			_favoriteCon = new LexicalDBHelper(getApplicationContext(), opt, dateBaseName);
			if(selectedPos!=-1){
				slots.get(selectedPos).value = _favoriteCon;
			} else {
				slots.add(new MyPair<>(dateBaseName, _favoriteCon));
			}
		}
		return favoriteCon = _favoriteCon;
	}
	
	protected LexicalDBHelper prepareHistroyCon() {
		if(historyCon!=null) return historyCon;
		AgentApplication app = ((AgentApplication) getApplication());
		LexicalDBHelper _historyCon = app.historyCon;
		if(_historyCon!=null){
			if(!new File(_historyCon.pathName).exists())
				_historyCon=null;
		}
		if(_historyCon==null){
			CheckInternalDataBaseDirExist();
			app.historyCon = _historyCon = new LexicalDBHelper(getApplicationContext(),opt);
		}
		return historyCon = _historyCon;
	}

	protected void insertUpdate_histroy(String key) {
		prepareHistroyCon().insertUpdate(key);
	}

	Field FastScrollField;
	Field TrackDrawableField;
	Field ThumbImageViewField;
	Field ScrollCacheField;
	Field ScrollBarDrawableField;
	public void listViewStrictScroll(ListView lv,boolean whetherStrict) {
		try {
			if(FastScrollField==null) {
				Class FastScrollerClass = Class.forName("android.widget.FastScroller");
				FastScrollField = AbsListView.class.getDeclaredField("mFastScroll");
				FastScrollField.setAccessible(true);
				TrackDrawableField = FastScrollerClass.getDeclaredField("mTrackDrawable");
				TrackDrawableField.setAccessible(true);

				ThumbImageViewField = FastScrollerClass.getDeclaredField("mThumbImage");
				ThumbImageViewField.setAccessible(true);
			}


			Object FastScroller = FastScrollField.get(lv);

			if(whetherStrict && TrackDrawableField!=null)//ok,being lazy...
				TrackDrawableField.set(FastScroller, null);

			if(ScrollCacheField==null) {
				ScrollCacheField = View.class.getDeclaredField("mScrollCache");
				ScrollCacheField.setAccessible(true);

				ScrollBarDrawableField = Class.forName("android.view.View$ScrollabilityCache").getDeclaredField("scrollBar");
				ScrollBarDrawableField.setAccessible(true);
			}
		} catch (Exception ignored) {}
	}


	String versionname = null;
	public String packageName() {
		if(versionname!=null) return versionname;
		PackageManager manager = getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			versionname = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return versionname;
	}
	
	public static mdict new_mdict(String pathFull, MainActivityUIBase THIS) throws IOException {
		File fullPath = pathFull.startsWith("/")?new File(pathFull):new File(THIS.opt.lastMdlibPath, pathFull);
		return new_mdict(fullPath, THIS);
	}
	
	public static mdict new_mdict(File fullPath, MainActivityUIBase THIS) throws IOException {
		String pathFull = fullPath.getPath();
		if(pathFull.startsWith(CMN.AssetTag)) {
			if(CMN.AssetMap.containsKey(pathFull))
				return new mdict_asset(fullPath,THIS);
		}
		int sufixp = pathFull.lastIndexOf(".");
		if(sufixp>0){
			int hash = hashCode(pathFull, sufixp+1);
			switch(hash){
				case 107969:
				case 107949:
					return new mdict(fullPath, THIS, 0, null);
				case 117588:
					return new mdict_web(fullPath, THIS);
				case 110834:
					return new mdict_pdf(fullPath, THIS);
				case 115312:
					return new mdict_txt(fullPath, THIS);
				case 99773:
					return new mdict_dsl(fullPath, THIS);
				//case 120609:
				//return new mdict_zip(fullPath, THIS);
				//case 3088960:
				//return new mdict_docx(fullPath, THIS);
			}
		}
		return null;
	}
	
	public static int hashCode(String toHash, int start) {
		int h=0;
		int len = toHash.length();
		for (int i = start; i < len; i++) {
			h = 31 * h + Character.toLowerCase(toHash.charAt(i));
		}
		return h;
	}
	
	boolean checkAllWebs(resultRecorderDiscrete combining_search_result, View view, int pos) {
		if(combining_search_result instanceof resultRecorderCombined && pos==0 && view==null){
			if(combining_search_result.checkAllWebs(md)){
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
			showTopSnack(sv, val, fval, -1, -1, false);
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
	
	public static void showStandardConfigDialog(TextView tv, SpannableStringBuilder ssb) {
		int length = ssb.length();
		ssb.delete(length-4,length);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		((AlertDialog) tv.getTag()).show();
		tv.setTag(null);
	}
	
	public abstract boolean isContentViewAttachedForDB();
	
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
			ViewGroup dv = (ViewGroup) inflater.inflate(R.layout.dialog_about_star,null);
			
			dv.findViewById(R.id.about_popIvBack).setOnClickListener(this);
			dv.findViewById(R.id.about_popLstDict).setOnClickListener(this);
			dv.findViewById(R.id.about_popNxtDict).setOnClickListener(this);
			
			ftv = dv.findViewById(R.id.subtext);
			ftv.mRatingDrawable = getRatingDrawable();
			ftv.setCompoundDrawables(mActiveDrawable, null, null, null);
			
			XYTouchRecorder xyt = opt.XYTouchRecorder();
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
			window.getDecorView().setTag(tv);
			tv.setTag(ftv);
			ftv.setTag(dv.getChildAt(1));
			d.setCanceledOnTouchOutside(true);
			d.show();
			this.d = d;
		}
		
		CurrentDictInfoIdx = id;
		
		ftv.setStarLevel(StarLevelStamp = md_get_StarLevel(id));
		
		ftv.setText(md_getName(id));
		
		tv.setText(md_getAbout_Trim(id));
	}
	
	private void CheckStars(int newLevel) {
		int oldId = CurrentDictInfoIdx;
		if(oldId>=0&&oldId<md.size()&&StarLevelStamp>=0&&newLevel!=StarLevelStamp){
			md_set_StarLevel(oldId, newLevel);
			if(pickDictDialog!=null) {
				pickDictDialog.adapter().notifyItemChanged(oldId);
				//pickDictDialog.notifyDataSetChanged();
			}
		}
	}
	
	public final class UniCoverClicker implements OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
		boolean isWeb;
		mdict invoker;
		WebViewmy mWebView;
		TextView mTextView;
		MdxDBHelper con;
		AlertDialog d;
		CircleCheckBox cb;
		ImageView iv_switch;
		ImageView tools_lock;
		ImageView iv_settings;
		ImageView iv_app_settings;
		ImageView iv_color;
		String CurrentSelected="";

		UniCoverClicker(){
			final Resources r = getResources();
			itemsA = r.getStringArray(R.array.dict_tweak_arr);
			itemsB = r.getStringArray(R.array.dict_tweak_arr2);
			itemsD = r.getStringArray(R.array.text_tweak_arr);
			itemsE = r.getStringArray(R.array.text_tweak_arr2);
			bmAdd=itemsA[0];
			lastInDark = GlobalOptions.isDark;
		}
		
		public void setInvoker(mdict mdict, WebViewmy _mWebView, TextView _tv, String text) {
			invoker=mdict;
			mWebView=_mWebView;
			mTextView =_tv;
			isWeb = invoker instanceof mdict_web;
			if(_tv!=null){
				int start = _tv.getSelectionStart();
				int end = _tv.getSelectionEnd();
				if(start>end){
					int startTmp = start;
					start=end;
					end = startTmp;
				}
				CurrentSelected = _tv.getText().subSequence(start, end).toString().trim();
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
		String[] itemsA;
		String[] itemsB;
		String[] itemsC = new String[0];
		String[] itemsD;
		String[] itemsE;
		String bmAdd;
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
			switch(v.getId()) {
				case R.id.iv_switch:{
					if(bFromWebView){
						boolean val = opt.setToTextShare(!opt.getToTextShare());
						iv_switch.setColorFilter(getResources().getColor(val?R.color.DeapDanger:R.color.ThinHeaderBlue), PorterDuff.Mode.SRC_IN);
						if(twoColumnAda!=null){
							twoColumnAda.setItems(val?itemsD:itemsB);
						}
					} else {
						boolean val = opt.setToTextShare2(!opt.getToTextShare2());
						iv_switch.setColorFilter(getResources().getColor(val?R.color.colorAccent:R.color.ThinAccent), PorterDuff.Mode.SRC_IN);
						if(twoColumnAda!=null){
							twoColumnAda.setItems(val?itemsE:itemsD);
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
					ViewGroup target=bFromPeruseView?PeruseView.contentview:getContentviewSnackHolder();
					root.post(() -> showTopSnack(target, msg, 0.8f, -1, -10, false));
					
					d.hide();
					ColorPickerDialog asd =
							ColorPickerDialog.newBuilder()
									.setDialogId(123123)
									.setInitialColor(invoker.getUseInternalBG()?(invoker.bgColor==null?(invoker.bgColor=CMN.GlobalPageBackground):invoker.bgColor):GlobalPageBackground)
									.create();
					asd.setColorPickerDialogListener(new ColorPickerDialogListener() {
						@Override
						public void onColorSelected(ColorPickerDialog dialogInterface, int color) {
							//CMN.Log("onColorSelected");
							if(invoker.getUseInternalBG())
								invoker.bgColor=color;
							else{
								GlobalPageBackground=CMN.GlobalPageBackground=color;
							}
							WebViewmy mWebView=bFromPeruseView?PeruseView.mWebView:invoker.mWebView;
							int ManFt_invoker_bgColor=invoker.bgColor;
							int ManFt_GlobalPageBackground=GlobalPageBackground;
							if(GlobalOptions.isDark) {
								ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web);
								ManFt_GlobalPageBackground=ColorUtils.blendARGB(ManFt_GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
							};
							boolean apply = bFromPeruseView || widget12.getTag(R.id.image)==null;
							if(invoker.getUseInternalBG()) {
								invoker.dumpViewStates(UIProjects);
								if(apply && mWebView!=null)
									mWebView.setBackgroundColor(ManFt_invoker_bgColor);
							}else {
								CMN.Log("应用全局颜色变更中…");
								GlobalPageBackground=CMN.GlobalPageBackground;
								if(apply) webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
								WHP.setBackgroundColor(ManFt_GlobalPageBackground);
								if(bFromPeruseView)PeruseView.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
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
								WebViewmy mWebView = bFromPeruseView ? PeruseView.mWebView : invoker.mWebView;
								ViewGroup webSingleholder = bFromPeruseView ? PeruseView.webSingleholder : MainActivityUIBase.this.webSingleholder;
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
							WebViewmy mWebView=bFromPeruseView?PeruseView.mWebView:invoker.mWebView;
							int ManFt_invoker_bgColor=invoker.bgColor;
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
								}else {
									if(apply) webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									WHP.setBackgroundColor(ManFt_GlobalPageBackground);
									if(bFromPeruseView)PeruseView.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									if(Build.VERSION.SDK_INT<21 && apply && mWebView!=null)
										mWebView.setBackgroundColor(ManFt_GlobalPageBackground);
								}
							}
						}
						boolean isDirty;
					});
					asd.show(getSupportFragmentManager(),"color-picker-dialog");
				}
				return;
				case R.id.settings:{
					mdict.showDictTweaker(bFromPeruseView?PeruseView.mWebView:null, MainActivityUIBase.this, invoker);
				} return;
				case R.id.appsettings:{
					showAppTweaker();
				} return;
				case R.id.lock:{
					boolean enabled;
					if(bFromPeruseView){
						enabled=PeruseView.toggleTurnPageEnabled();
					}else{
						enabled=opt.setTurnPageEnabled(!opt.getTurnPageEnabled());
						PageSlider.TurnPageEnabled=enabled;
					}
					tools_lock.setImageResource(enabled?R.drawable.un_locked:R.drawable.locked);
					opt.putFirstFlag();
					showTopSnack((ViewGroup) d.getListView().getRootView(), enabled?R.string.PT1:R.string.PT2
							, 0.8f, LONG_DURATION_MS, -1, false);
				} return;
			}
			hideKeyboard();
			Object tag = v.getTag();
			if(!bFromTextView &&  tag instanceof Integer){
				Integer ftag = (Integer) tag;
				bFromWebView=(ftag&1)!=0;
				bFromPeruseView=(ftag&2)!=0;
			}
			if (!bFromWebView && mWebView!=null && invoker instanceof mdict_pdf) {
				mWebView.evaluateJavascript(PDFPage, value -> {
					mWebView.currentPos = IU.parsint(value);
					build_further_dialog();
				});
			} else {
				build_further_dialog();
			}
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if(parent==d.getListView()&&(bFromWebView||bFromTextView)){
				return true;
			}
			return onItemClick(parent, view, position, id, true);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			onItemClick(parent, view, position, id, false);
		}

		public boolean onItemClick(AdapterView<?> parent, @Nullable View view, int position, long id, boolean isLongClicked) {
			//CMN.Log("数据库 ", itemsA[0], getString(R.string.bmSub), itemsA[0].equals(getString(R.string.bmSub)));
			try {
				//if(isLongClicked) CMN.Log("长按开始……");
				if(!bFromWebView && !bFromTextView) {
					WebViewmy _mWebView = mWebView;
					if(_mWebView==null) _mWebView=invoker.mWebView;
					if(_mWebView==null) {
						showT("错误!!! 网页找不到了");
						return true;
					}
					switch (position) {
						/* 书签 */
						case 0: {
							if (isLongClicked) return false;
							if (itemsA[0].equals(getString(R.string.bmSub))) {
								boolean succ = false;
								if (con != null) {
									if (isWeb) {
										mdict_web webx = ((mdict_web) invoker);
										if (webx.removeCurrentUrl(con, _mWebView.getUrl())) {
											succ = true;
										}
									} else if (con.remove(_mWebView.currentPos) > 0) {
										succ = true;
									}
								}
								if (succ) {
									if(view!=null)((TextView) view.findViewById(android.R.id.text1)).setText(itemsA[0] = bmAdd);
									showX(R.string.delDone, 0);
								} else
									showT("删除失败,数据库出错...", 0);
							} else {// add
								boolean succ = false;
								if (con == null) con = invoker.getCon(true);
								try {
									if (isWeb) {
										mdict_web webx = ((mdict_web) invoker);
										if (webx.saveCurrentUrl(con, _mWebView.getUrl()) != -1)
											succ = true;
									} else {
										if (con.insert(_mWebView.currentPos) != -1) {
											succ = true;
											int BKHistroryVagranter = opt.getInt("bkHVgrt", -1);
											BKHistroryVagranter = (BKHistroryVagranter + 1) % 20;
											String rec = invoker.getDictionaryName() + "/?Pos=" + _mWebView.currentPos;
											opt.putter()
													.putString("bkh" + BKHistroryVagranter, rec)
													.putInt("bkHVgrt", BKHistroryVagranter)
													.apply();
										}
									}
								} catch (Exception e) {
									CMN.Log(e);
								}
								if (succ) {
									showX(R.string.bmAdded, 0);
									if(view!=null)((TextView) view.findViewById(android.R.id.text1)).setText(itemsA[0] = getString(R.string.bmSub));
								} else {
									showT("添加失败,数据库出错...", 0);
								}
							}
							if (mBookMarkAdapter != null)
								if (mBookMarkAdapter.cr != null) {
									mBookMarkAdapter.cr.close();
									mBookMarkAdapter.cr = null;
								}
						}
						break;
						/* 书签列表 */
						case 1: {//书签列表
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
							if (con == null) {
								showT("没有书签", 0);
								break;
							}
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUIBase.this, lastInDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line);//,R.style.DialogStyle2Line);
							builder.setTitle(invoker.appendCleanDictionaryName(MainStringBuilder).append(" -> ").append(getString(R.string.bm)).toString());
							builder.setItems(new String[]{}, null);
							builder.setNeutralButton(R.string.delete, null);
							AlertDialog d = builder.show();
							ListView list = d.getListView();
							list.setAdapter(getBookMarkAdapter(lastInDark, invoker, con));
							//d.getListView().setFastScrollEnabled(true);

							//tofo
							if (lastBookMarkPosition != -1) list.setSelection(lastBookMarkPosition);
							
							list.setOnItemClickListener((parent1, view1, position1, id1) -> {
								int id11 = (int) mBookMarkAdapter.getItem(position1);
								myWebClient.shouldOverrideUrlLoading(bFromPeruseView ? PeruseView.mWebView : invoker.mWebView, "entry://@" + id11);
								opt.putString("bkmk", invoker.f().getAbsolutePath() + "/?Pos=" + id11);
								//if(!cb.isChecked())
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
						}
						break;
						/* 文字缩放级别 */
						case 2:
						case 3: {
							if (isLongClicked) {
								int targetLevel = invoker.getFontSize();
								switch (position) {
									case 2:
										if (targetLevel < mdict.optimal100) {
											_mWebView.getSettings().setTextZoom(targetLevel = mdict.optimal100);
										} else if (targetLevel < 500) {
											_mWebView.getSettings().setTextZoom(targetLevel = 500);
										} else
											targetLevel = -1;
										break;
									case 3:
										if (targetLevel > mdict.optimal100) {
											_mWebView.getSettings().setTextZoom(targetLevel = mdict.optimal100);
										} else if (targetLevel > 10) {
											_mWebView.getSettings().setTextZoom(targetLevel = 10);
										} else
											targetLevel = -2;
										break;
								}
								if (targetLevel > 0) {
									if (invoker.getUseInternalFS()) {
										showT((invoker.internalScaleLevel = targetLevel) + "%", 0);
										invoker.dumpViewStates(UIProjects);
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
								int targetLevel = invoker.getUseInternalFS() ? _mWebView.getSettings().getTextZoom() : mdict.def_fontsize;
								if (position == 2) targetLevel += 10;
								else targetLevel -= 10;
								targetLevel = targetLevel > 500 ? 500 : targetLevel;
								targetLevel = targetLevel < 10 ? 10 : targetLevel;
								_mWebView.getSettings().setTextZoom(targetLevel);
								if (invoker.getUseInternalFS()) {
									showT((invoker.internalScaleLevel = targetLevel) + "%", 0);
									invoker.dumpViewStates(UIProjects);
								} else {
									notifyGLobalFSChanged(targetLevel);
									showT("(" + getResources().getString(R.string.GL) + ") " + (targetLevel) + "%", 0);//全局
								}
							}
						}
						break;
					}
				}
				else {
					PreferredToolId = position;
					if((bFromTextView && (position<7||!opt.getToTextShare2()) || !bFromTextView && opt.getToTextShare()))
						position+=11;
					switch (position) {//xx
						/* 收藏 */
						case 11:
						case 0: {
							if (bFromTextView) {
								if (CurrentSelected.length() > 0 && prepareFavoriteCon().insertUpdate(CurrentSelected) > 0)
									showT(CurrentSelected + " 已收藏");
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										if (prepareFavoriteCon().insertUpdate(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1))) > 0)
											showT(word + " 已收藏");
									}
								});
							}
						}
						break;
						/* 全选 */
						case 1: {
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
						}
						break;
						/* 颜色 */
						case 2:
							break;
						/* 高亮 */
						case 3: {
							mWebView.evaluateJavascript(mWebView.getHighLightIncantation().toString(), null);
						}
						break;
						/* 清除高亮 */
						case 4: {
							mWebView.evaluateJavascript(mWebView.getDeHighLightIncantation().toString(), null);
						}
						break;
						/* 下划线 */
						case 5: {
							mWebView.evaluateJavascript(mWebView.getUnderlineIncantation().toString(), null);
						}
						break;
						/* 清除下划线 */
						case 6: {
							mWebView.evaluateJavascript(mWebView.getDeUnderlineIncantation().toString(), null);
						}
						break;
						case 18://分享#4
						case 19://分享'
						case 20://分享#5
						case 21://分享#6
						case 7://分享#1
						case 8://分享
						case 9://分享#2
						case 10: //分享#3
						{
							if (isLongClicked && (position == 19 || position == 8)) {
								return true;
							}
							JSONObject json = opt.getDimensionalSharePatternByIndex(position - 7);
							boolean putDefault = json == null;
							if (putDefault) {
								json = new JSONObject();
							} else {
								putDefault = json.has("b")||json.length()==0;
							}
							if (putDefault) {
								putDefaultSharePattern(json, position);
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
								AlertController.RecycleListView customNameList = new AlertController.RecycleListView(context);
								customNameList.mMaxHeight = customList.mMaxHeight = (int) (root.getHeight() - root.getPaddingTop() - 3.8 * getResources().getDimension(R.dimen._50_));
								customNameList.setTag(false);
								customNameList.setVerticalScrollBarEnabled(false);

								CustomShareAdapter csa = new CustomShareAdapter(data);
								customList.setAdapter(csa);
								customNameList.setAdapter(csa);
								customList.setDivider(null);
								customNameList.setDivider(null);

								AlertDialog.Builder builder2 = new AlertDialog.Builder(context, GlobalOptions.isDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line);
								builder2.setTitle("制定分享目标");
								builder2.setNeutralButton("添加字段", null);
								builder2.setNegativeButton("测试", null);
								int finalPosition = position;
								builder2.setPositiveButton("保存", null);

								TableLayout dv = new TableLayout(context);

								dv.setOrientation(LinearLayout.HORIZONTAL);
								dv.addView(customNameList);
								View delimiter = new View(context);
								/* 分割线 */
								dv.addView(delimiter, new LinearLayout.LayoutParams((int) (1 * dm.density), LayoutParams.MATCH_PARENT));
								/* 键盘能够弹出 */
								dv.addView(new EditText(context), new LinearLayout.LayoutParams(0, 0));
								delimiter.setBackgroundColor(0xffcccccc);
								LinearLayout.LayoutParams lpmy;
								dv.addView(customList, lpmy = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT));
								lpmy.weight = 1;
								customNameList.getLayoutParams().width
										= (int) ((TextView) csa.getView(0, null, customNameList)).getPaint().measureText(getResources().getString(R.string.extra_key_value, 1001));
								builder2.setView(dv);

								AlertDialog dTmp = builder2.show();

								OnClickListener mClicker = new OnClickListener() {
									@Override
									public void onClick(View v) {
										boolean isLongClicked = v.getTag(R.id.long_clicked) != null;
										switch (v.getId()) {
											case android.R.id.button1://+
												if (isLongClicked) {
													android.app.AlertDialog.Builder builder21 = new android.app.AlertDialog.Builder(inflater.getContext());
													android.app.AlertDialog d1 = builder21.setTitle("确认删除并恢复默认值？")
															.setPositiveButton(R.string.confirm, (dialog, which) -> {
																opt.putDimensionalSharePatternByIndex(finalPosition - 7, null);
																JSONObject json = new JSONObject();
																data.clear();
																putDefaultSharePattern(json, finalPosition);
																serializeSharePattern(json, data);
																csa.notifyDataSetChanged();
															})
															.create();
													d1.show();
												} else try {
													JSONObject neo = packoutNeoJson(data);
													JSONObject original = new JSONObject();
													putDefaultSharePattern(original, finalPosition);
													if (baseOnDefaultSharePattern(neo, original)) {
														neo = packoutNeoJson(data);
													}
													opt.putDimensionalSharePatternByIndex(finalPosition - 7, neo);
													showT("保存成功！");
													dTmp.dismiss();
												} catch (Exception e) {
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
						}
						break;
						/* 页内搜索 */
						case 12: {
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
							d.dismiss();
						}
						break;
						/* TTS */
						case 13: {
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
							if (!opt.getPinDialog()) d.dismiss();
						}
						break;
						/* 点译 */
						case 14: {
							if (isLongClicked) return false;
							if (bFromTextView) {
								if (CurrentSelected.length() > 0)
									popupWord(CurrentSelected, -1, -1);
							} else {
								mWebView.evaluateJavascript(WebViewmy.CollectWord, word -> {
									if (word.length() > 2) {
										popupWord(StringEscapeUtils.unescapeJava(word.substring(1, word.length() - 1)), -1, mWebView.frameAt);
									}
								});
							}
							d.dismiss();
						}
						break;
						/* 翻阅模式 */
						case 15: {
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
							d.dismiss();
						}
						break;
						/* 搜索框 */
						case 16: {
							if (isLongClicked) return false;
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
							d.dismiss();
						}
						break;
						/* 浮动搜索 */
						case 17: {
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
							d.dismiss();
						}
						break;
					}
				}
				if(!cb.isChecked())
					d.dismiss();
			} catch (Exception e){
				CMN.Log(e);
			}
			return false;
		}

		void HandleShareIntent(ArrayList<String> data) {
			if(bFromTextView){
				handleIntentShare(CurrentSelected, data);
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
			boolean needRecreate=bLastFromWebView!=bFromWebView||bFromTextView;
			if(!bFromWebView && mWebView!=null){
				if(!bFromTextView)
					con = invoker.getCon(false);
				itemsA[0]=bmAdd;
				if(con!=null){
					if(isWeb){
						if(((mdict_web)invoker).containsCurrentUrl(con, mWebView.getUrl()))
							itemsA[0]=getString(R.string.bmSub);
					} else if(con.containsRaw(mWebView.currentPos)){
						itemsA[0]=getString(R.string.bmSub);
					}
				}
			}

			if(GlobalOptions.isDark!=lastInDark || needReCreateUcc || d==null) {
				CMN.Log("重建对话框…");
				needRecreate=false;
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUIBase.this,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);//,
				builder.setItems((bFromWebView||bFromTextView)?itemsC:itemsA,null);
				d = builder.create();

				bottomView = (ViewGroup) inflater.inflate(R.layout.checker2,null);
				//ImageView toolbar_settings = (ImageView)bottomView.findViewById(R.id.settings);
				//ImageView toolbar_colors = (ImageView)bottomView.findViewById(R.id.color);
				iv_settings = bottomView.findViewById(R.id.settings);
				iv_app_settings = bottomView.findViewById(R.id.appsettings);
				iv_color = bottomView.findViewById(R.id.color);
				iv_switch = bottomView.findViewById(R.id.iv_switch);

				iv_settings.setOnClickListener(this);
				iv_app_settings.setOnClickListener(this);
				iv_color.setOnClickListener(this);
				iv_switch.setOnClickListener(this);

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

				cb = bottomView.findViewById(R.id.checker);
				tools_lock  = bottomView.findViewById(R.id.lock);
				if(GlobalOptions.isDark) cb.drawInnerForEmptyState=true;
				else cb.circle_shrinkage=2;
				cb.setOnClickListener(v12 -> {
					cb.toggle();
					opt.setPinDialog(cb.isChecked());
				});

				if(PageSlider!=null) {
					if(!opt.getTurnPageEnabled()&&!bFromPeruseView||bFromPeruseView&&opt.getPageTurn3())
						tools_lock.setImageResource(R.drawable.locked);
					tools_lock.setOnClickListener(this);
				}else
					tools_lock.setVisibility(View.GONE);

				ListView dialogList = d.getListView();
				dialogList.setOnItemClickListener(this);
				dialogList.setOnItemLongClickListener(this);

				dialogList.addFooterView(bottomView);

				if(twoColumnAda!=null)  twoColumnAda.notifyDataSetChanged();
			}

			cb.setChecked(opt.getPinDialog(), false);

			boolean hasText = bFromWebView || bFromTextView;

			if(bFromWebView)
				iv_switch.setColorFilter(getResources().getColor(opt.getToTextShare()?R.color.DeapDanger:R.color.ThinHeaderBlue), PorterDuff.Mode.SRC_IN);
			else if(bFromTextView)
				iv_switch.setColorFilter(getResources().getColor(opt.getToTextShare2()?R.color.colorAccent:R.color.ThinAccent), PorterDuff.Mode.SRC_IN);

			iv_switch.setVisibility(hasText?View.VISIBLE:View.GONE);

			int targetVis = bFromTextView?View.GONE:View.VISIBLE;
			tools_lock.setVisibility(targetVis);
			iv_settings.setVisibility(targetVis);
			iv_color.setVisibility(targetVis);

			ListView dialogList = d.getListView();
			boolean toText = bFromTextView || opt.getToTextShare();
			if(hasText){
				String[] items = toText ? (bFromTextView && opt.getToTextShare2()?itemsE:itemsD) : itemsB;
				if(twoColumnView==null) {
					RecyclerView footRcyView = new RecyclerView(bottomView.getContext());
					footRcyView.setClipToPadding(false);
					GridLayoutManager lman;
					footRcyView.setLayoutManager(lman = new GridLayoutManager(bottomView.getContext(), 2));
					lman.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
						@Override
						public int getSpanSize(int position) {
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
					dialogList.addFooterView(twoColumnView);
					dialogList.addFooterView(bottomView);
				}
				twoColumnAda.setItems(items);
			}
			else if(twoColumnView!=null && twoColumnView.getParent()!=null){
				dialogList.removeFooterView(twoColumnView);
			}

			if(!bFromTextView)
				d.setTitle(invoker.appendCleanDictionaryName(MainStringBuilder).append(" - ").append((bFromPeruseView?PeruseView.currentDisplaying:invoker.currentDisplaying)).toString());
			else
				d.setTitle("文本操作");

			d.show();

			//if(getCurrentFocus()!=null && !(getCurrentFocus() instanceof WebView))
			//	getCurrentFocus().clearFocus();

			d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			
			lastInDark=GlobalOptions.isDark;

			if(needRecreate && !needReCreateUcc) { /* dynamically change items! */
				try {
					ArrayAdapter<CharSequence> DialogArryAda = ((ArrayAdapter<CharSequence>) ((HeaderViewListAdapter) d.getListView().getAdapter()).getWrappedAdapter());
					Field field = ArrayAdapter.class.getDeclaredField("mObjects");
					field.setAccessible(true);
					field.set(DialogArryAda, Arrays.asList((bFromWebView||bFromTextView) ? itemsC : itemsA));
					DialogArryAda.notifyDataSetChanged();
					//CMN.Log("列表动态更新成功！");
					bLastFromWebView=bFromWebView||bFromTextView;
				} catch (Exception e) {
					needReCreateUcc = true;
					CMN.Log("列表动态更新失败", e);
				}
			}else{
				bLastFromWebView=bFromWebView||bFromTextView;
			}
			firstCreateUcc=false;
		};
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

	void putDefaultSharePattern(JSONObject json, int position) {
		try {
			switch (position){
				/* 分享#2 */
				case 9:
				/* 分享#1 */
				case 7:
					if(!json.has("k1")) json.put("k1", "_data");
					if(!json.has("v1")) json.put("v1", position==9?"https://translate.google.cn/#view=home&op=translate&sl=auto&tl=zh-CN&text=%s":"https://www.baidu.com/s?wd=%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", position==9?"c/q":"/");
				break;
				/* 分享#3 */
				case 10:
					if(!json.has("k1")) json.put("k1", "_data");
					if(!json.has("v1")) json.put("v1", "https://cn.bing.com/search?q=%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "/");
				break;
				case 21:
				case 19:
				case 8:
					if(!json.has("k1")) json.put("k1", Intent.EXTRA_TEXT);
					if(!json.has("v1")) json.put("v1", "%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", position==21?"c/q":"/");
					if(!json.has("t")) json.put("t", "text/plain");
					if(!json.has("a")) json.put("a", Intent.ACTION_SEND);
				break;
				case 18:
					if(!json.has("a")) json.put("a", SEARCH_ACTION);
					if(!json.has("k1")) json.put("k1", EXTRA_QUERY);
					if(!json.has("v1")) json.put("v1", "%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "/");
				break;
				case 20:
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
	boolean needReCreateUcc=false;
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
	
	ArrayAdaptermy mBookMarkAdapter;
	public ListAdapter getBookMarkAdapter(boolean darkMode, mdict invoker, MdxDBHelper con) {
		if(mBookMarkAdapter==null)
			mBookMarkAdapter=new ArrayAdaptermy(getApplicationContext(),R.layout.drawer_list_item,R.id.text1,invoker,con,0);
		else
			mBookMarkAdapter.refresh(invoker,con);
		mBookMarkAdapter.darkMode=darkMode;
		return mBookMarkAdapter;
	}

	public static void init_clickspan_with_bits_at(OptionProcessor optprs, TextView tv, SpannableStringBuilder text,
									 String[] dictOpt, int titleOff, String[] coef, int coefOff,
									 int coefShift, long mask,
									 int flagPosition, int flagMax, int flagIndex,
									 int processId, boolean addColon) {
		PDICMainAppOptions opt = optprs.getOpt();
		int start = text.length();
		int now = start+dictOpt[titleOff].length();
		text.append("[").append(dictOpt[titleOff]);
		if(addColon) text.append(" :");
		if(coef!=null){
			long val = (opt.Flag(flagIndex)>>flagPosition)&mask;
			text.append(coef[coefOff+(int) ((val)+coefShift)%(flagMax+1)]);
		}
		text.append("]");
		text.setSpan(new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				if(coef==null){
					optprs.processOptionChanged(this, widget, processId , -1);
					return;
				}
				long flag = opt.Flag(flagIndex);
				long val = (flag>>flagPosition)&mask;
				val=(val+1)%(flagMax+1);
				flag &= ~(mask << flagPosition);
				flag |= (val << flagPosition);
				opt.Flag(flagIndex, flag);
				int fixedRange = indexOf(text, ':', now);
				text.delete(fixedRange+1, indexOf(text, ']', fixedRange));
				text.insert(fixedRange+1,coef[coefOff+(int) ((val)+coefShift)%(flagMax+1)]);
				tv.setText(text);
				optprs.processOptionChanged(this, widget, processId , (int) val);
			}},start,text.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.append("\r\n").append("\r\n");
	}
	
	@Override
	public void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val) {
		switch (processId){
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
				if(PeruseView!=null&&PeruseView.getView()!=null) {
					if(val==1)
						PeruseView.mBar.setVisibility(View.GONE);
					else
						PeruseView.mBar.setVisibility(View.VISIBLE);
				}
			break;
			case 8:
				setGlobleCE(val==1);
			break;
			case 9:
				Intent intent = new Intent();
				intent.putExtra("realm", 10);
				intent.setClass(getBaseContext(), SettingsActivity.class);
				startActivity(intent);
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
					intent = new Intent();
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
				} else if(this instanceof PDICMainActivity){
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
						PeruseView.RecalibrateWebScrollbar();
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
				AlertDialogLayout pp =  win.findViewById(R.id.parentPanel);
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
				PeruseView.leftLexicalAdapter.notifyDataSetChanged();
			break;
			case 21:
				PeruseView.lv2.setFastScrollEnabled(val==1);
			break;
			case 22:
				PeruseView.lv1.setFastScrollEnabled(val==1);
			break;
			case 23:
				PeruseView.lv1.setFastScrollEnabled(val==1);
				PeruseView.lv2.setFastScrollEnabled(val==1);
			break;
			case 24:
				PeruseView.toggleInPageSearch(false);
			break;
			case 26:
				PeruseView.leftLexicalAdapter.notifyDataSetChanged();
				PeruseView.bookMarkAdapter.notifyDataSetChanged();
			break;
			case 10090:
				checkFlags();
			break;
		}
	}
	
	@Override
	public PDICMainAppOptions getOpt() {
		return opt;
	}
	
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
	
	TextView buildStandardConfigDialog(Context context, boolean centerText, OnClickListener onclick, int title_id, Object...title_args) {
		final View dv = inflater.inflate(R.layout.dialog_about,null);
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = dv.findViewById(R.id.title);
		if(title_args.length>0){
			title.setText(getResources().getString(title_id, title_args));
		} else {
			title.setText(title_id);
		}
		title.setTextSize(18f);
		title.setTextColor(AppBlack);
		//title.getPaint().setFakeBoldText(true);
		
		int topad = (int) getResources().getDimension(R.dimen._18_);
		((ViewGroup)title.getParent()).setPadding(topad*3/5, topad/2, 0, 0);
		//((ViewGroup)title.getParent()).setClipToPadding(false);
		//((ViewGroup.MarginLayoutParams)title.getLayoutParams()).setMarginStart(-topad/4);
		
		opt.setAsLinkedTextView(tv, centerText);
		
		final AlertDialog configurableDialog =
				new AlertDialog.Builder(context,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line)
						.setView(dv)
						.create();
		configurableDialog.setCanceledOnTouchOutside(true);
		
		dv.findViewById(R.id.cancel).setOnClickListener(v -> {
			if(onclick!=null) onclick.onClick(v);
			configurableDialog.dismiss();
		});
		
		tv.setTag(configurableDialog);
		
		return tv;
	}
	
	public void showAppTweaker() {
		String[] DictOpt = getResources().getStringArray(R.array.app_spec);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		
		TextView tv = buildStandardConfigDialog(this, true, null, R.string.AppOpt);
		Dialog configurableDialog = (Dialog) tv.getTag();

		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 16, 1, 1, 0, false);//opt.isFullScreen()//全屏
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 2, Coef, 0, 1, 0x1, 17, 1, 1, 1, false);//opt.isContentBow()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 3, Coef, 0, 0, 0x1, 18, 1, 1, 2, false);//opt.getInDarkMode()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 4, Coef, 0, 0, 0x1, 46, 1, 1, 3, false);//opt.getUseVolumeBtn()//
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 5, null, 0, 0, 0x1, 0, 1, 1, -1, false);//隐藏标题
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 6, Coef, 0, 0, 0x1, 0, 1, 2, -1, false);//opt.getInheritePageScale()//
		String[] Coef2 = new String[]{Coef[0], Coef[1], Coef[2]};
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 7, Coef2, 0, 0, 0x3, 0, 2, 2, 4, false);//opt.getNavigationBtnType()//
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 8, Coef, 0, 0, 0x1, 3, 1, 2, 5, false);//opt.getHideScroll1()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 9, Coef, 0, 0, 0x1, 4, 1, 2, 6, false);//opt.getHideScroll2()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 10, Coef, 0, 0, 0x1, 5, 1, 2, 7, false);//opt.getHideScroll3()//
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 11, Coef, 0, 0, 0x1, 6, 1, 2, -1, false);//opt.getPageTurn1()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 12, Coef, 0, 0, 0x1, 7, 1, 2, -1, false);//opt.getPageTurn2()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 13, Coef, 0, 0, 0x1, 33, 1, 3, -1, false);//opt.getPageTurn3()//
		ssb.append("\r\n").append("\r\n");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 15, Coef, 0, 1, 0x1, 28, 1, 3, 8, false);//opt.getAllowContentEidt()//编辑页面
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 14, Coef, 0, 0, 0x1, 9, 1, 2, 10, false);//opt.setHistoryStrategy0()//关闭历史纪录
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 16, null, 0, 0, 0x1, 0, 1, 2, 9, false);//历史纪录规则
		//CMN.Log("ssb len:", ssb.length());
		tv.setTag(null);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		//d.getWindow().setDimAmount(0);
		//d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		configurableDialog.show();
		//tofo
	}
	
	void showAppExit() {
		String[] DictOpt = getResources().getStringArray(R.array.app_exit);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		
		TextView tv = buildStandardConfigDialog(this, false, v12 -> {
			if(opt.getDeletHistoryOnExit())
				deleteHistory();
			finish();
			if(opt.getShuntDownVMOnExit()){
				System.exit(0);
			}
		}, R.string.warn_exit0);
		
		init_clickspan_with_bits_at(this, tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 42, 1, 4, -1, true);//清除历史
		init_clickspan_with_bits_at(this, tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1, 40, 1, 4, 10090, true);//彻底退出
		ssb.delete(ssb.length()-4,ssb.length());
		
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		((AlertDialog) tv.getTag()).show();
		tv.setTag(null);
	}
	
	public void showSoundTweaker() {
		String[] DictOpt = getResources().getStringArray(R.array.sound_spec);
		final String[] Coef = DictOpt[0].split("_");
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		
		TextView tv = buildStandardConfigDialog(this, true, null, R.string.SoundOpt);
		Dialog configurableDialog = (Dialog) tv.getTag();

		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 38, 1, 3, -1, true);//opt.getAutoReadEntry()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 2, Coef, 0, 0, 0x1, 48, 1, 4, -1, true);//opt.getThenAutoReadContent()//
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 3, Coef, 0, 0, 0x1, 37, 1, 3, -1, true);//opt.getHintTTSReading()//
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 4, Coef, 0, 1, 0x1, 42, 1, 3, -1, true);//opt.getTTSBackgroundPlay()//

		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 5, Coef, 0, 1, 0x1, 35, 1, 3, 12, true);//opt.getClickSearchEnabled()//
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 6, null, 0, 1, 0x1, 0, 1, 3, 15, false);

		boolean flagCase = PeruseViewAttached();
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 7, Coef, 0, 0, 0x1, flagCase?37:46, 1, flagCase?2:1, -1, true);//opt.getUseVolumeBtn()
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 8, Coef, 0, 1, 0x1, 39, 1, 3, -1, true);//opt.getMakeWayForVolumeAjustmentsWhenAudioPlayed()//

		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 9, null, 0, 0, 0x1, 44, 1, 3, 16, false);//滚动条
		ssb.delete(ssb.length()-4, ssb.length()); ssb.append("  ");
		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 10, Coef, 0, 0, 0x1, 44, 1, 3, 11, true);//opt.getTTSHighlightWebView()//

		init_clickspan_with_bits_at(this,tv, ssb, DictOpt, 11, null, 0, 0, 0x1, 0, 1, 3, 10, false);//

		ssb.delete(ssb.length()-4, ssb.length());
		
		tv.setTag(null);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		configurableDialog.show();
	}

	abstract void switch_dark_mode(boolean val);

	void changeToDarkMode() {
		CMN.Log("changeToDarkMode");
		try {
			boolean dark=GlobalOptions.isDark||opt.getInDarkMode();
			AppBlack=dark?Color.WHITE:Color.BLACK;
			AppWhite=dark?Color.BLACK:Color.WHITE;
			if(drawerFragment!=null){
				drawerFragment.mDrawerListLayout.setBackgroundColor(dark?Color.BLACK:0xffe2e2e2);
				drawerFragment.HeaderView.setBackgroundColor(AppWhite);
				drawerFragment.FooterView.setBackgroundColor(AppWhite);
				drawerFragment.myAdapter.notifyDataSetChanged();
				if(isFragInitiated && pickDictDialog!=null)pickDictDialog.adapter().notifyDataSetChanged();
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
			animateUIColorChanges();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void animateUIColorChanges();

	public UniCoverClicker getUcc() {
		if(ucc==null) ucc = new UniCoverClicker();
		return ucc;
	}
	
	boolean isContentViewAttached() {
		return contentview.getParent()!=null;
	}
	
	abstract void DetachContentView();
	
	void AttachDBrowser() {
		if(DBrowser!=null){
			if(!DBrowser.isAdded()) {
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				transaction.setCustomAnimations(R.anim.history_enter, R.anim.history_enter);
				transaction.add(R.id.mainF, DBrowser);
				transaction.commit();
			} else {
				mainF.addView(DBrowser.getView());
			}
		}
	}
	
	//click
	@Override
	public void onClick(View v) {
		click_handled_not = true;
		int id=v.getId();
		switch (id){
			default:return;
			case R.id.bottombar:{
				showIconCustomizator();
			} break;
			case R.id.go_history:
			case R.id.browser_widget6:{
				//todo to favorite
				if(DBrowser==null) {
					if(mainF.getChildCount()==0){
						if(DHBrowser_holder!=null) DBrowser=DHBrowser_holder.get();
						if(DBrowser==null){
							//CMN.Log("重建历史纪录");
							DHBrowser_holder = new WeakReference<>(DBrowser = new DHBroswer());
						}
						AttachDBrowser();
					}
				} else if(isContentViewAttachedForDB()){
					DetachContentView();
				}
			} break;
			case R.id.ttsPin:{
				CircleCheckBox checker = (CircleCheckBox) v;
				checker.toggle();
				opt.setTTSCtrlPinned(checker.isChecked());
				TTSController_controlBar.setVisibility(checker.isChecked()?View.VISIBLE:View.GONE);
			} break;
			case R.id.ttsHighlight:{
				CircleCheckBox checker = (CircleCheckBox) v;
				checker.toggle(false);
				opt.setTTSHighlightWebView(checker.isChecked());
			} break;
			case R.id.tts_settings:{
				try {
					Intent intent = new Intent();
					intent.setAction("com.android.settings.TTS_SETTINGS");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} break;
			case R.id.tts_popIvBack:{
				if(TTSController_.getParent()!=null){
					hideTTS();
				}
			} break;
			case R.id.tts_expand:{
				if(opt.setTTSExpanded(!opt.getTTSExpanded())){
					TTSController_.getLayoutParams().height=TTSController_moveToucher.FVH_UNDOCKED;
					((ImageView)v).setImageResource(R.drawable.ic_substrct_black_24dp);
				} else {
					TTSController_.getLayoutParams().height= (int) getResources().getDimension(R.dimen._45_);
					((ImageView)v).setImageResource(R.drawable.ic_add_black_24dp);
				}
				TTSController_.requestLayout();
			} break;
			case R.id.tts_play:{
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
			case R.id.tts_LstUtterance:{
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
			case R.id.cover:{
				if(v==popCover){
					getUcc().setInvoker(CCD, popupWebView, null, null);
					getUcc().onClick(v);
				}
			} break;
			case R.id.popNxtE:
			case R.id.popLstE:{
				if(CCD==null)
					CCD=currentDictionary;
				int np = currentClickDictionary_currentPos+(id==R.id.popNxtE?1:-1);
				if(np>=0&&np<CCD.getNumberEntries()){
					popupTextView.setText(currentClickDisplaying=CCD.getEntryAt(np));
					popupHistory.add(++popupHistoryVagranter,new myCpr<>(currentClickDisplaying,new int[]{CCD_ID, np}));
					if (popupHistory.size() > popupHistoryVagranter + 1) {
						popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
					}
					popuphandler.setDict(CCD);
					if(PDICMainAppOptions.getClickSearchAutoReadEntry())
						popupWebView.bRequestedSoundPlayback=true;
					popupWebView.fromCombined=2;
					CCD.renderContentAt(-1, CCD_ID, -1, popupWebView, currentClickDictionary_currentPos=np);
					decorateContentviewByKey(popupStar, currentClickDisplaying);
					if(!PDICMainAppOptions.getHistoryStrategy0() && PDICMainAppOptions.getHistoryStrategy10() && PDICMainAppOptions.getHistoryStrategy11())
						insertUpdate_histroy(currentClickDisplaying);
				}
			} break;
			case R.id.popNxtDict:
			case R.id.popLstDict:{
				int idx=-1, cc=0;
				String key=popupTextView.getText().toString().trim();
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
						if(CCD instanceof mdict_web){
							mdict_web webx = (mdict_web) CCD;
							if(webx.takeWord(key)){
								webx.searchKey=key;
								idx=0;
							}
						}
						else if (CCD!=null) {
							idx=CCD.lookUp(key, true);
							if(idx<0){
								if(!reject_morph&&use_morph){
									keykey=ReRouteKey(key, true);
									if(keykey!=null)
										idx=CCD.lookUp(keykey, true);
									else
										reject_morph=true;
								}
							}
						}

						if(idx>=0 || hasDedicatedSeachGroup && CCD!=null ||  !PDICMainAppOptions.getSkipClickSearch())
							break;
					}

					//应用轮询结果
					if(OldCCD!=CCD_ID && CCD!=null){
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
							CCD.renderContentAt(-1, CCD_ID, -1, popupWebView, currentClickDictionary_currentPos=idx);
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
					imm.hideSoftInputFromWindow((PeruseSearchAttached()?PeruseView.PerusePageSearchetSearch:MainPageSearchetSearch).getWindowToken(), 0);
				}
				jumpHighlight(next?1:-1, true);
			} break;
			/* 清零 */
			case R.id.ivDeleteText:{
				if(v.getTag()!=null){
					return;
				}
				if((etSearch_toolbarMode&2)==0) {//delete
					String SearchTmp = etSearch.getText().toString().trim();
					if(SearchTmp.equals("")) {
						ivDeleteText.setVisibility(View.GONE);
					}else {
						lastEtString=SearchTmp;
						CombinedSearchTask_lastKey=null;
						etSearch.setText(null);
						etSearch_ToToolbarMode(4);
					}
				}else {//undo
					etSearch.setText(lastEtString);
					//etSearch_ToToolbarMode(3);
				}
			}break;
			/* 删除/选择收藏夹 */
			case R.id.ivDeleteText_ADA:{
				View p = (View) v.getParent();
				DArrayAdapter.ViewHolder vh = (DArrayAdapter.ViewHolder) p.getTag();
				int position = vh.position;
				int id_ = ((ViewGroup)p.getParent()).getId();
				MyPair<String, LexicalDBHelper> item = AppDataAdapter.items.get(position);
				if(p.getParent() instanceof ViewGroup)
				if(id_==R.id.favorList){//选择
					//CMN.Log("选择!!!");
					opt.putCurrFavoriteDBName(item.key);
					favoriteCon = null;
					prepareFavoriteCon();
					AppDataAdapter.notifyDataSetChanged();
				} else if(id_==R.id.click_remove){//删除
					LexicalDBHelper vI = item.value;
					if(vI!=null){
						if(vI==favoriteCon)
							favoriteCon=null;
						vI.close();
						item.value=null;
					}
					File fi = opt.fileToFavoriteDatabases(item.key);
					fi.delete();
					new File(fi.getPath()+"-journal").delete();
					AppDataAdapter.remove(position);
				}
			}break;
			//返回
			case R.id.popIvBack:{
				DetachClickTranslator();
			} break;
			//返回
			case R.id.popIvRecess:{
				long cm;
				if(popupHistoryVagranter>0 && (cm=System.currentTimeMillis())-lastClickTime>300 && popupWebView.canGoBack()) {
					popupWebView.goBack();
					popNav(-1);
					lastClickTime=cm;
				}
			} break;
			case R.id.popIvForward:{
				long cm;
				if(!popupWebView.isloading && popupHistoryVagranter<popupHistory.size()-1 && (cm=System.currentTimeMillis())-lastClickTime>300 && popupWebView.canGoForward()) {
					popupWebView.goForward();
					popNav(1);
					lastClickTime=cm;
				}
			} break;
			case R.id.popIvSettings:{
				startActivityForResult(new Intent().putExtra("realm", 9).setClass(this, SettingsActivity.class), 999);
			} break;
			case R.id.popChecker:{
				CircleCheckBox checker = (CircleCheckBox) v;
				checker.toggle();
				PDICMainAppOptions.setClickSearchPin(checker.isChecked());
			} break;
			case R.id.popIvStar:{
				toggleStar(currentClickDisplaying, (ImageView) v, false);
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
					PeruseView.try_go_back();
					break;
				}
				exitTime=0;
				boolean b1=this instanceof PDICMainActivity;
				if(b1) {
					v.setTag(false);
					PDICMainActivity THIS = (PDICMainActivity) this;
					THIS.mDrawerLayout.closeDrawer(GravityCompat.START);
					if (THIS.drawerFragment.d != null) {
						THIS.drawerFragment.d.dismiss();
					}
				}
				if(PeruseViewAttached()) {
					View _contentview = PeruseView.contentview;
					ViewGroup _p_contentview = (ViewGroup) _contentview.getParent();
					if(_p_contentview==PeruseView.slp || _p_contentview==PeruseView.mlp) {
						_p_contentview.removeView(_contentview);
						PeruseView.cvpolicy=false;
						PeruseView.ActivedAdapter=null;
						break;
					}
				}
				if(b1){
					onBackPressed();
				} else {
					etSearch_ToToolbarMode(0);
					webcontentlist.setVisibility(View.GONE);
				}
			} break;
			//左右翻页
			case R.id.browser_widget10:
			case R.id.browser_widget11:{//左zuo
				int delta = (id==R.id.browser_widget10?-1:1);
				if(ActivedAdapter==null) {
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
				if(webSingleholder.getChildCount()==1 && widget12.getTag(R.id.image)!=null){
					if((int)widget12.getTag(R.id.image)==R.drawable.ic_fullscreen_black_96dp){
						if(this instanceof PDICMainActivity)
							((PDICMainActivity)this).forceFullscreen(!PDICMainAppOptions.isFullScreen());
					}else{
						toggleClickThrough();
					}
				} else {
					performReadEntry();
				}
			} break;
			/* 切换收藏 */
			case R.id.browser_widget8:{//favorite
				if(ActivedAdapter==null){
					if(DBrowser!=null)
						DBrowser.toggleFavor();
				} else {
					String key = ActivedAdapter.currentKeyText.trim();
					if (prepareFavoriteCon().contains(key)) {
						favoriteCon.remove(key);
						favoriteBtn.setActivated(false);
						show(R.string.removed);
					} else {
						favoriteCon.insert(key);
						favoriteBtn.setActivated(true);
						show(R.string.added);
					}
				}
			} break;
			/* 跳转 */
			case R.id.browser_widget9:{//view outlinexxx
				if(ActivedAdapter instanceof com.knziha.plod.PlainDict.PeruseView.LeftViewAdapter) {
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
							View CI = webholder.getChildAt(position);
							int currIdx = (int) CI.getTag();
							mdict mdTmp = md.get(currIdx);
							
							FlowCheckedTextView ret;
							if(convertView!=null){
								ret = (FlowCheckedTextView) convertView;
							} else {
								ret = (FlowCheckedTextView) inflater.inflate(R.layout.singlechoice_w, parent, false);
								ret.setMinimumHeight((int) getResources().getDimension(R.dimen._50_));
							}
							if(mdTmp==null){
								ret.setText("Error!!!");
								return ret;
							}
							FlowTextView tv = ret.mFlowTextView;
							tv.setCompoundDrawables(getActiveStarDrawable(), null, null, null);
							tv.setCover(mdTmp.getCover());
							tv.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
							tv.setStarLevel(0);
							
							ret.setChecked(position == finalSelectedPos);
							
							ret.setText(mdTmp.getDictionaryName());
							
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
					
					dTmp.getWindow().setLayout((int) (dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor)), -2);

					if(GlobalOptions.isDark) dTmp.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

					if(selectedPos!=-1) {
						dTmp.getListView().setSelection(selectedPos);
						dTmp.getListView().setItemChecked(selectedPos, true);
					}
					//d.getWindow().getDecorView().setBackgroundResource(R.drawable.popup_shadow_l);
					//d.getWindow().getDecorView().getBackground().setColorFilter(GlobalOptions.NEGATIVE);
					//d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
				}
				else{
					if(widget12.getTag(R.id.image)!=null){
						float alpha = contentview.getAlpha();// 0.5 0.25 0 1
						if(alpha==0) alpha=1;
						else if(alpha==1) alpha=0.37f;
						else  alpha=0;
						contentview.setAlpha(alpha);
					}else{
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
			case R.id.auto_browse:{
				if(bRequestingAutoReading || !AutoBrowsePaused){
					stopAutoReadProcess();
				} else {
					showT("自动浏览");
					opt.isAudioActuallyPlaying=false;
					startAutoReadProcess();
				}
			} break;
			/* 全文朗读 */
			case R.id.tts_readAll:{
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
		}
		click_handled_not = false;
	}

	private void scrollToWebChild(View childAt) {
		CMN.Log("scrollToWebChild");
		if(childAt!=null) {
			View postTarget = null;
			int delay = 100;
			if(PDICMainAppOptions.getScrollAutoExpand())
			try {
				mdict mdTmp = md.get((int) childAt.getTag());
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

	private void popNav(int delta) {
		try {
			myCpr<String, int[]> record = popupHistory.get(popupHistoryVagranter+=delta);
			popupTextView.setText(currentClickDisplaying=record.key);
			popupWebView.SelfIdx = CCD_ID = record.value[0];
			currentClickDictionary_currentPos = record.value[1];
			popupIndicator.setText((CCD=md.get(CCD_ID)).getDictionaryName());
			popuphandler.setDict(CCD);
		} catch (Exception e) { CMN.Log(e); }
	}

	@Deprecated
	public void showChooseTTSDialog() { }

	/**
	 *  Show choose dictionary dialog
	 * @param reason 0=pick; 1=pick click dict.
	 * */
	abstract void showChooseDictDialog(int reason);

	void toggleStar(String key, ImageView futton, boolean toast) {
		key = key.trim();
		if(prepareFavoriteCon().contains(key)) {
			favoriteCon.remove(key);
			futton.setActivated(false);
			if(toast)show(R.string.removed);
		}else {
			favoriteCon.insert(key);
			futton.setActivated(true);
			if(toast)show(R.string.added);
		}
	}
	
	public boolean deleteHistory() {
		try {
			return prepareHistroyCon().wipeData();
		} catch (Exception ignored) { }
		return false;
	}
	
	//longclick
	@Override @SuppressLint("ResourceType")
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			/* long-click exit */
			case R.id.exitApp:{
				showAppExit();
				//anyDialog();
			} return true;
			case R.id.browser_widget8:{
				String text=null;
				if(PeruseViewAttached())
					text=PeruseView.currentDisplaying;
				else if(DBrowser!=null)
					text=DBrowser.currentDisplaying;
				else if(ActivedAdapter!=null)
					text=ActivedAdapter.currentKeyText;
				if(text!=null)
					showMultipleCollection(text);
			} return true;
			/* long-click view outline */
			case R.id.browser_widget9:{
				if(PeruseViewAttached()) {
					PeruseView.toolbar_cover.performClick();
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
						PeruseView.setBottomNavigationType(opt.setBottomNavigationMode1(which), tv);
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
				AlertDialogLayout pp =  win.findViewById(R.id.parentPanel);
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
			for (int i = 0; i < cc; i++) {
				View childAt = webholder.getChildAt(i);
				View targetView = childAt.findViewById(R.id.webviewmy);
				if(targetVis==View.GONE)
					targetView.setVisibility(targetVis);
				else if(targetView.getVisibility()!=View.VISIBLE){
					childAt.findViewById(R.id.toolbar_title).performClick();
				}
			}
		}
	}

	void launchSettings(int fragmentId) {
		startActivity(new Intent().putExtra("realm", fragmentId).setClass(this, SettingsActivity.class));
	}

	void toggleClickSearch(boolean val) {
		evalJsAtAllFrames(val?"window.rcsp|=0x20":"window.rcsp&=~0x20");
	}

	void toggleInPageSearch(boolean isLongClicked) {
		if(isLongClicked){
			launchSettings(7);
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

			if(this instanceof PDICMainActivity)
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
			String val = text==null?"highlight(null)":"highlight(\""+ text.replace("\"","\\\"")+"\")";
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
					showTopSnack(getContentviewSnackHolder(), msg, 0.75f, -1, Gravity.CENTER, false);
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
									(PeruseSearchAttached()?PeruseView.PerusePageSearchindicator:MainPageSearchindicator).setText((preAll+1)+"/"+all);
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
		if(PeruseView!=null && PeruseView.mWebView!=null){
			PeruseView.mWebView.evaluateJavascript(exp,null);
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
						else if(md.get(mWebView.SelfIdx).getContentEditable())
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
		if(MainPageSearchbar==null || MainPageSearchbar.getParent()==null)
			return null;
		return MainPageSearchetSearch.getText().toString();//URLEncoder.encode(key, "utf8");
	}

	public boolean hasCurrentPageKey() {
		return MainPageSearchbar!=null && MainPageSearchbar.getParent()!=null && MainPageSearchetSearch.getText().toString().trim().length()>0;
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
			ArrayList<String> scanInList = new ArrayList<>();
			dTmp = new AlertDialog.Builder(this, GlobalOptions.isDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line)
						.setTitle(R.string.loadconfig)
					.setSingleChoiceItems(new String[]{}, -1, (dialog, pos) -> {
						try {
							currentFilter.clear();
							for (mdict mdTmp : md) {
								if(mdTmp!=null)
									mdict_cache.put(mdTmp.getPath(), mdTmp);
							}
							for (mdict mdTmp : currentFilter) {
								if(mdTmp!=null)
									mdict_cache.put(mdTmp.getPath(), mdTmp);
							}
							String setName = scanInList.get(pos);
							File newf = opt.fileToSet(null, setName);
							boolean lazyLoad = PDICMainAppOptions.getLazyLoadDicts();
							LoadLazySlots(newf, lazyLoad, setName);
							buildUpDictionaryList(lazyLoad, mdict_cache);
							//todo 延时清空
							//mdict_cache.clear();
							//分组切换
							setLastPlanName(setName);
							if (adapter_idx<0) {
								switch_To_Dict_Idx(0, true, false);
							} else if(md.get(adapter_idx)!=currentDictionary){
								switch_To_Dict_Idx(adapter_idx, true, false);
							}
							dialog.dismiss();
							invalidAllLists();
							//show(R.string.loadsucc);
							showTopSnack(main_succinct, R.string.loadsucc
									, -1, -1, Gravity.CENTER, false);
							if(this instanceof PDICMainActivity) {
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
			
			((AlertController.RecycleListView) dTmp.getListView())
					.mMaxHeight = (int) (root.getHeight() - root.getPaddingTop() - 2.8 * getResources().getDimension(R.dimen._50_));

			dTmp.show();
			
			ListView dlv = dTmp.getListView();
			
			dlv.setTag(scanInList);
			
			dlv.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
					R.layout.singlechoice, android.R.id.text1, scanInList) {
				@NonNull
				@Override
				public View getView(int position, View convertView, @NonNull ViewGroup parent) {
					View ret = super.getView(position, convertView, parent);
					CheckedTextViewmy tv;
					if (ret.getTag() == null)
						ret.setTag(tv = ret.findViewById(android.R.id.text1));
					else
						tv = (CheckedTextViewmy) ret.getTag();
					tv.setTextColor(AppBlack);
					tv.setText(scanInList.get(position));
					return ret;
				}
			});

			if(lastCheckedPos!=-1) {
				dlv.setItemChecked(lastCheckedPos, true);
			}
			setchooser = new WeakReference<>(dTmp);
		}
		else if(dTmp.isShowing()){
			return;
		}
		
		ArrayList<String> scanInList = (ArrayList<String>) dTmp.getListView().getTag();
		
		scanInList.clear();
		
		File ConfigFile = opt.fileToConfig();
		File def = opt.fileToSecords(ConfigFile);
		final HashSet<String> con = new HashSet<>();
		lastCheckedPos = -1;
		try {
			AgentApplication app = ((AgentApplication) getApplication());
			ReusableBufferedReader in = new ReusableBufferedReader(new FileReader(def), app.get4kCharBuff(), 4096);
			String line = in.readLine();
			while (line != null) {
				if (!con.contains(line))
					if (opt.fileToSet(ConfigFile, line).exists()) {
						scanInList.add(line);
						if (line.equals(opt.lastMdPlanName)){
							lastCheckedPos = scanInList.size() - 1;
						}
					}
				con.add(line);
				line = in.readLine();
			}
			in.close();
		}
		catch (Exception ignored) {  }
		CMN.Log("扫描分组……", scanInList.size());
		((BaseAdapter)dTmp.getListView().getAdapter()).notifyDataSetChanged();
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
			if(mode==2 && mdict._req_fvw!=0 && mdict._req_fvh!=0){
				bSwitch = mdict._req_fvw>mdict._req_fvh;
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
		public void onShowCustomView(View view, CustomViewCallback callback) {
			//CMN.Log("onShowCustomView", mdict._req_fvw, mdict._req_fvh);
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
			//CMN.Log("ProgressChanged");
			WebViewmy mWebView = (WebViewmy) view;
			//todo undo changes made to webview by web dictionaries.
			//if(mWebView.fromCombined==4){
			if(mWebView.fromNet){
				int selfAtIdx = mWebView.SelfIdx;
				if(selfAtIdx>=md.size() || selfAtIdx<0) return;
				final mdict invoker = md.get(selfAtIdx);
				if(invoker instanceof mdict_web){
					((mdict_web)invoker).onProgressChanged(mWebView, newProgress);
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

	public WebViewClient myWebClient = new WebViewClient() {
		public void onPageFinished(WebView view, String url) {
			WebViewmy mWebView = (WebViewmy) view;
			CMN.Log("chromium page finished ==> ", url, view.getProgress(), CMN.stst_add, PDICMainAppOptions.getClickSearchAutoReadEntry(), view.getTag(R.drawable.voice_ic));
			if(!mWebView.isloading && !mWebView.fromNet) return;
			int from = mWebView.fromCombined;
			mWebView.isloading = false;
			mWebView.AlwaysCheckRange = 0;
			
			//if(true) return;
			/* I、接管页面缩放及(跳转)位置(0, 1, 3)
			*  II、进入黑暗模式([0,1,2,3],[4])、编辑模式(0,1,3,[4])。*/
			String toTag = mWebView.toTag;
			OUT:
			if(from!=2){
				int selfAtIdx = mWebView.SelfIdx;
				if(selfAtIdx>=md.size() || selfAtIdx<0) return;
				final mdict invoker = md.get(selfAtIdx);
				if(invoker instanceof mdict_web){
					((mdict_web)invoker).onPageFinished(view, url, true);
					break OUT;
				}
				if(invoker==null){
					//root.post(() -> showT("OPF 错误!!!"));
					return;
				}
				boolean editing = invoker.getContentEditable() && invoker.getEditingContents();

				if(mWebView.webScale!=mdict.def_zoom) {
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
						else{
							if(!toTag.equals("===000"))
								view.evaluateJavascript("location.replace(\"#" + toTag + "\");", null);
						}
					}
				}

				boolean toHighLight = PDICMainAppOptions.getPageAutoScrollOnTurnPage() && view.getTag(R.id.toolbar_action5) != null;

				if(proceed) {
					if (!fromCombined) {
						lastClickTime = System.currentTimeMillis();

						if (mWebView.expectedPos >= 0 && !toHighLight) {
							int lalaX, lalaY;
							mWebView.setTag(R.id.toolbar_action1, lalaX =  mWebView.expectedPosX);
							mWebView.setTag(R.id.toolbar_action2, lalaY = mWebView.expectedPos);
							//layoutScrollDisabled=true;
							//CMN.Log("initial_push: ", lalaX, lalaY);
							mWebView.scrollTo(lalaX, lalaY);

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
							webholder.addOnLayoutChangeListener(recCom.OLCL);
							recCom.OLCL.onLayoutChange(webholder ,0, webholder.getTop(),0,webholder.getBottom(),0,0,0,0);
							//});
						}
					}
				}

				if (editing && mWebView.currentRendring!=null && mWebView.currentRendring.length==1) {
					mWebView.evaluateJavascript(ce_on, null);
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
			/* 自动播放声音自动播报 */
			if(mWebView.bRequestedSoundPlayback){
				mWebView.bRequestedSoundPlayback=false;
				if(mWebView==popupWebView)
					widget12.setTag(R.drawable.ic_click_search, false);

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

			if(mWebView.clearHistory){
				mWebView.clearHistory=false;
				mWebView.clearHistory();
			}
			
			/* 延迟加载 */
			if(from==1 && PDICMainAppOptions.getDelaySecondPageLoading() && !PDICMainAppOptions.getOnlyExpandTopPage()){
				int next = mWebView.frameAt+1;
 				if(next < webholder.getChildCount()){
					View childAt;
					try {
						while ((childAt=webholder.getChildAt(next++))!=null) {
							mdict invoker = md.get((int) childAt.getTag());
							if(!PDICMainAppOptions.getTmpIsCollapsed(invoker.tmpIsFlag) && invoker.mWebView.awaiting){
								CMN.Log("/* 延迟加载 */", invoker.getDictionaryName());
								invoker.toolbar_title.performClick();
								break;
							}
						}
					} catch (Exception ignored) {  }
				}
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			WebViewmy mWebView = (WebViewmy) view;
			int selfAtIdx = mWebView.SelfIdx;
			if(selfAtIdx>=md.size() || selfAtIdx<0) return;
			final mdict invoker = md.get(selfAtIdx);
			if(invoker instanceof mdict_web){
				((mdict_web)invoker).onPageStarted(view, url, false);
			}
		}

		public void  onScaleChanged(WebView view, float oldScale,float newScale)
		{
			//CMN.Log(oldScale, "-", newScale, " newScale");
			int selfAtIdx = (int)view.getTag();
			if(selfAtIdx>=md.size() || selfAtIdx<0) return;
			mdict invoker = md.get(selfAtIdx);
			super.onScaleChanged(view, oldScale,newScale);
			WebViewmy mWebView = ((WebViewmy)view);
			mWebView.webScale=newScale;
			if(view==invoker.mWebView) {
				invoker.webScale = newScale;
			}
		}

		@Override
		public void onLoadResource(WebView view, String url) {

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			WebViewmy mWebView = (WebViewmy) view;
			int selfAtIdx = mWebView.SelfIdx;
			//CMN.Log("chromium shouldOverrideUrlLoading_???",url,view.getTag(), md.get(selfAtIdx).getDictionaryName(), selfAtIdx);
			if(selfAtIdx>=md.size() || selfAtIdx<0) return false;
			final mdict invoker = md.get(selfAtIdx);
			boolean fromPopup = view==popupWebView;
			if(invoker instanceof mdict_web){
				mdict_web webx = ((mdict_web) invoker);
				try {
					if (!url.startsWith("http") && !url.startsWith("https") && !url.startsWith("ftp") && !url.startsWith("file")) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				boolean ret = webx.canExcludeUrl && webx.shouldExcludeUrl(url);
				if(!ret && fromPopup){
					popupHistory.add(++popupHistoryVagranter, new myCpr<>(currentClickDisplaying, new int[]{CCD_ID, currentClickDictionary_currentPos}));
					if (popupHistory.size() > popupHistoryVagranter + 1) {
						popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
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
					if (invoker.hasMdd()) {
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
								invoker.HistoryOOP.put(mWebView.currentPos, PageState);
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
						PeruseView.setCurrentDis(invoker, mWebView.currentPos);
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
						if (invoker instanceof mdict_pdf) {
							view.evaluateJavascript("window.PDFViewerApplication.page=" + pos, null);
							return true;
						}
						//todo delay save states
						if (fromPeruseView) {
							PeruseView.recess.setVisibility(View.VISIBLE);
							PeruseView.forward.setVisibility(View.VISIBLE);
							PeruseView.isJumping = true;
							if (mWebView.HistoryVagranter >= 0)
								mWebView.History.get(PeruseView.mWebView.HistoryVagranter).value.set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
							PeruseView.setCurrentDis(invoker, pos);
						} else {
							invoker.isJumping = true;
							if (mWebView.HistoryVagranter >= 0)
								mWebView.History.get(mWebView.HistoryVagranter).value.set(mWebView.getScrollX(),mWebView.getScrollY(), mWebView.webScale);
							invoker.setCurrentDis(mWebView, pos);
						}
						view.loadDataWithBaseURL(invoker.baseUrl,
								invoker.AcquirePageBuilder().append((AppWhite == Color.BLACK) ? MainActivityUIBase.DarkModeIncantation_l : "")
										.append(mdict.htmlHeadEndTag)
										.append(invoker.getRecordsAt(pos))
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
					boolean popup = invoker.getPopEntry();
					if(popup){
						init_popup_view();
						popupWebView.frameAt = mWebView.frameAt;
						popupWebView.SelfIdx = mWebView.SelfIdx;
						mWebView = popupWebView;
					}
					try {
						mWebView.toTag = null;
						int tagIdx = url.indexOf("#");
						if (tagIdx > 0) {
							mWebView.toTag = url.substring(tagIdx + 1);
							url = url.substring(0, tagIdx);
						}
						if(url.endsWith("/")) url=url.substring(0, url.length()-1);
						url = URLDecoder.decode(url, "UTF-8");
						if(popup){
							popupWord(url, popupWebView.SelfIdx, mWebView.frameAt);
							return true;
						}
						else {
							/* 查询跳转目标 */
							int idx = invoker.lookUp(url, true);
							//CMN.Log("查询跳转目标 : ", idx, URLDecoder.decode(url,"UTF-8"), processText(URLDecoder.decode(url,"UTF-8")));
							if (idx >= 0) {//idx != -1
								if(!fromPopup) {
									/* 除点译弹窗与网络词典，代管所有网页的前进/后退。 */
									if (fromPeruseView) {
										PeruseView.isJumping = true;
										if (mWebView.HistoryVagranter >= 0)
											mWebView.History.get(mWebView.HistoryVagranter).value.set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
										PeruseView.setCurrentDis(invoker, idx);
									}
									else {
										invoker.isJumping = true;
										if (!fromCombined) {
											ScrollerRecord PageState = null;
											if (mWebView.HistoryVagranter >= 0)
												(PageState = mWebView.History.get(mWebView.HistoryVagranter).value).set(mWebView.getScrollX(), mWebView.getScrollY(), mWebView.webScale);
											if (mWebView.HistoryVagranter == 0)
												invoker.HistoryOOP.put(mWebView.currentPos, PageState);
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
								float initialScale = mdict.def_zoom;
								mWebView.setInitialScale((int) (100 * (initialScale / mdict.def_zoom) * opt.dm.density));
								mWebView.isloading = true;
								StringBuilder htmlBuilder = invoker.AcquirePageBuilder();
								if(fromPopup){
									popupHistory.add(++popupHistoryVagranter, new myCpr<>(CCD!=null?CCD.getLexicalEntryAt(idx):currentClickDisplaying, new int[]{CCD_ID, idx}));
									if (popupHistory.size() > popupHistoryVagranter + 1) {
										popupHistory.subList(popupHistoryVagranter + 1, popupHistory.size()).clear();
									}
								}
								invoker.AddPlodStructure(mWebView, htmlBuilder ,mWebView==popupWebView, invoker.rl==mWebView.getParent()&&invoker.rl.getLayoutParams().height>0);
								invoker.LoadPagelet(mWebView, htmlBuilder, invoker.getRecordsAt(idx));
								return true;
							}
						}
					} catch (Exception e) {
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
				return super.shouldInterceptRequest(view, Url);
			return shouldInterceptRequestCompat(view, Url, null, null, null, null);
		}

		public WebResourceResponse shouldInterceptRequest (final WebView view, WebResourceRequest request) {
			if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP)
				return super.shouldInterceptRequest(view, request);
			Map<String, String> keyset = request.getRequestHeaders();
			//for (String key:keyset.keySet()) CMN.Log(request.getUrl(), "keyset : ", key, " :: ",keyset.get(key),request.getMethod());
			return shouldInterceptRequestCompat(view, request.getUrl().toString(), keyset.get("Accept"), keyset.get("Referer"), keyset.get("Origin"), request);
		}

		private WebResourceResponse shouldInterceptRequestCompat(WebView view, String url, String accept, String refer, String origin, WebResourceRequest request) {
			//CMN.Log("chromium shouldInterceptRequest???",url,view.getTag());
			//if(true) return null;
			if(url.startsWith("data:")) return null;

			if(url.startsWith("mdbr://")){
				try {
					url=url.substring(7);
					CMN.Log("[fetching internal res : ]", url);
					String mime="*/*";
					if(url.endsWith(".css")) mime = "text/css";
					if(url.endsWith(".js")) mime = "text/js";
					return new WebResourceResponse(mime, "UTF-8", loadCommonAsset(url));
				} catch (Exception e) {
					CMN.Log(e);
				}
			}

			WebViewmy mWebView = (WebViewmy) view;
			int selfAtIdx = mWebView.SelfIdx;
			if(selfAtIdx>=md.size() || selfAtIdx<0) return null;
			mdict invoker = md.get(selfAtIdx);
			//CMN.Log("chromium shouldInterceptRequest invoker",invoker);
			if(invoker==null) return null;
			if(invoker instanceof mdict_web){
				if(view.getTag(R.id.save)==null && (url.startsWith("http")||url.startsWith("file"))){
					mdict_web webx = ((mdict_web) invoker);
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

					if(proceed){
						InputStream overridePage = webx.getPage(url);
						//CMN.tp(stst, "webx getPage :: ", overridePage, url);
						if(overridePage!=null){
							return new WebResourceResponse("*","UTF-8",overridePage);
						}
					}

					if(webx.canSaveResource){
						try {
							shWebsite = webx.cacheExtensions;
							for (int i = 0; i < shWebsite.length; i++) {
								//CMN.Log(url, webx.cacheExtensions[i], url.contains(webx.cacheExtensions[i]));
								if(url.contains(shWebsite[i])){
									File pathDownload = webx.getInternalResourcePath(true);
									if(!pathDownload.exists()) pathDownload.mkdirs();
									if(pathDownload.isDirectory()) {
										boolean needTrim=!url.contains(".php");//动态资源需要保留参数
										File path;
										int start = url.indexOf("://");
										if(start<0) start=0; else start+=3;
										start = Math.max(url.indexOf("/", start)+1, start);
										int end = needTrim?url.indexOf("?"):url.length();
										if(end<0) end=url.length();
										String name=url.substring(start, end);
										name=URLDecoder.decode(name);
										if(!needTrim) name=name.replaceAll("[=?&|:*<>]", "_");
										if(name.length()==0){
											name = "plod-index";
										}
										path=new File(pathDownload, name);
										pathDownload = path.getParentFile();
										if(!pathDownload.exists()) pathDownload.mkdirs();
										if(pathDownload.isDirectory())
										{
											name=path.getName();
											if(name.length()>64){
												name=name.substring(0, 56)+"_"+name.length()+"_"+name.hashCode();
												path=new File(pathDownload, name);
											}
											/* 下载 */
											if(!path.exists()) {
												CMN.Log("shouldInterceptRequest 下载中...！", url);
												CMN.Log("shouldInterceptRequest 下载目标: ", name);
												URL requestURL = new URL(url);
												HttpURLConnection urlConnection = null;
												FileOutputStream fout = null;
												try {
													try {
														SSLContext sslcontext = SSLContext.getInstance("TLS");
														sslcontext.init(null, new TrustManager[]{new mdict_web.MyX509TrustManager()}, new java.security.SecureRandom());
														HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
													} catch (Exception ignored) {
													}
													urlConnection = (HttpURLConnection) requestURL.openConnection();
													urlConnection.setRequestMethod("GET");
													urlConnection.setConnectTimeout(10000);
													if(accept!=null)urlConnection.setRequestProperty("Accept",accept);
													if(refer!=null) urlConnection.setRequestProperty("Refer", refer);
													if(origin!=null) urlConnection.setRequestProperty("Origin", origin);
													if(request!=null && Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
														Map<String, String> headers = request.getRequestHeaders();
														urlConnection.setRequestProperty("X-Requested-With", headers.get("X-Requested-With"));
														urlConnection.setRequestProperty("Content-Type", headers.get("Content-Type"));
														urlConnection.setRequestMethod(request.getMethod());
													}
													urlConnection.setRequestProperty("Charset", "UTF-8");
													urlConnection.setRequestProperty("Connection", "Keep-Alive");
													urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 9; VTR-AL00 Build/HUAWEIVTR-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36");
													urlConnection.connect();
													InputStream is = urlConnection.getInputStream();
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
													CMN.Log("shouldInterceptRequest 已下载！", url);
												} catch (Exception e) {
													CMN.Log(e);
													path.delete();
												}
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
												if(origin!=null && request!=null && Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
													Map<String, String> headers = request.getRequestHeaders();
													Map<String, String> keyset = ret.getResponseHeaders();
													if(keyset==null){
														keyset = new HashMap<>();
														ret.setResponseHeaders(keyset);
													}
													keyset.put("Content-Type", headers.get("Content-Type"));
													keyset.put("Access-Control-Allow-Origin", origin);
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
				return null;
			}
			if(url.startsWith("http") && url.endsWith(".mp3")) {
				return null;
			}

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
				mdict mdTmp;
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

			int start = key.indexOf(mdict.FileTag);
			if(start==-1){
				if(key.startsWith("./"))
					key=key.substring(1);
			}else{
				key = key.substring(start+mdict.FileTag.length());
			}
			if(url.startsWith("/MdbR/")){
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
			else if(key.startsWith("/pdfimg/")){
				String urlkey=key.substring("/pdfimg/".length());
				int idx = urlkey.lastIndexOf("#");
				int page = 0;
				if(idx>0){
					page=IU.parsint(urlkey.substring(idx+1));
					urlkey=urlkey.substring(0, idx);
				}
				OUTPDF:
				try{
					if(pdfiumCore==null){
						pdfiumCore = new PdfiumCore(getBaseContext());
						cached_pdf_docs=new HashMap<>();
					}
					PdfDocument pdf = cached_pdf_docs.get(urlkey);
					if(pdf==null){
						File path = new File(urlkey);
						if(!path.exists()) break OUTPDF;
						pdf = pdfiumCore.newDocument(ParcelFileDescriptor.open(path, ParcelFileDescriptor.MODE_READ_ONLY));
						cached_pdf_docs.put(urlkey, pdf);
					}
					if(mWebView.fromCombined==0  && !mWebView.fromNet && invoker.getIsolateImages()){
						//CMN.Log("Pdf Isolating Images...");
						new_photo = key;
						PhotoPager.removeCallbacks(PhotoRunnable);
						PhotoPager.post(PhotoRunnable);
						return super.shouldInterceptRequest(view, url);
					}
					CMN.rt();

					pdfiumCore.openPage(pdf, page);

					CMN.pt("文档打开耗时 : "); CMN.rt();


					float shrinkage=1f;
					int width = pdfiumCore.getPageWidth(pdf, page);
					int height = pdfiumCore.getPageHeight(pdf, page);
					float mBitmapRam = (width * height * 2);
					if(mBitmapRam>MaxBitmapRam){
						shrinkage = MaxBitmapRam/mBitmapRam;
					}
					width*=shrinkage;
					height*=shrinkage;
					Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
					CMN.pt("分配内存耗时 : "); CMN.rt();
					pdfiumCore.renderPageBitmap(pdf, bitmap, page, 0, 0, width, height);

					CMN.pt("解码耗时 : "); CMN.rt();

					ByteArrayOutputStream bos = new ByteArrayOutputStream(bitmap.getByteCount());
					bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bos);
					CMN.pt("再编码耗时 : "); CMN.rt();
					return new WebResourceResponse("image/jpeg","UTF-8",new ByteArrayInputStream(bos.toByteArray()));
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}

			key=key.replace("/", SepWindows);

			//CMN.Log("chrochro_inter_key is",key);

			if(!key.startsWith(SepWindows)){
				key=SepWindows+key;
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
			if(mime!=null && key.lastIndexOf(SepWindows)==0){
				File candi = new File(invoker.f().getParentFile(),new File(url).getName());
				//CMN.Log("candi_csssj",url, candi.getAbsolutePath(), candi.exists());
				if(candi.exists()) try {
					return new WebResourceResponse(mime,"UTF-8",new FileInputStream(candi));
				} catch (FileNotFoundException ignored) { }
			}

			if(!invoker.hasMdd())
				return null;
			key=mdict.requestPattern.matcher(key).replaceAll("");
			if(mWebView.fromCombined==0 && !mWebView.fromNet && invoker.getIsolateImages() && RegImg.matcher(key).find()){
				//CMN.Log("Isolating Images...");
				new_photo = key;
				PhotoPager.removeCallbacks(PhotoRunnable);
				PhotoPager.post(PhotoRunnable);
				return super.shouldInterceptRequest(view, url);
			}
			try {
				InputStream restmp=invoker.getResourceByKey(key);
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
					else return super.shouldInterceptRequest(view, url);
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
				e.printStackTrace();
				return super.shouldInterceptRequest(view, url);
			}
		}

		HashMap<String, byte[]> CommonAssets = new HashMap<>();
		private InputStream loadCommonAsset(String key) throws IOException {
			byte[] data = CommonAssets.get(key);
			if(data==null){
				InputStream input = getResources().getAssets().open(key);
				data = new byte[input.available()];
				input.read(data);
				CommonAssets.put(key, data);
			}
			return new ByteArrayInputStream(data);
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
		if(mWebView.SelfIdx>0 && mWebView.SelfIdx<md.size()){
			mdict mdTmp = md.get(mWebView.SelfIdx);
			if(mdTmp!=null) return mdTmp.hasMdd();
		}
		return false;
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

	private mdict findPronouncer(int i, mdict invoker) {
		mdict mdTmp = md.get(i);
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
						md.set(i, mdTmp = new_mdict(phTmp.getPath(opt), MainActivityUIBase.this));
						mdTmp.tmpIsFlag = phTmp.tmpIsFlag;
					} catch (Exception ignored) { }
				}
			}
			return mdTmp;
		}
	}

	private void playCachedSoundFile(WebViewmy mWebView, String soundUrl, mdict invoker, boolean findInAudioLibs) throws IOException {
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
				mdict mdTmp;
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
	@Multiline
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
	 	background:transparent!important;
	 	background-color:transparent!important;
	 }
	 </style>
	 */
	@Multiline
	public final static String DarkModeIncantation_l = "DARK";

	/**
	 var css = 'html {-webkit-filter: invert(100%);\
					 -moz-filter: invert(100%);\
					 -o-filter: invert(100%);\
					 -ms-filter: invert(100%);}',
	 head = document.getElementsByTagName('head')[0],
	 style = document.createElement('style');
	 style.class = "_PDict";
	 style.id = "_PDict_Darken";
	 style.type = 'text/css';
	 if (style.styleSheet){
		 style.styleSheet.cssText = css;
	 } else {
	 	style.appendChild(document.createTextNode(css));
	 }
	 //injecting the css to the head
	 head.appendChild(style);
	 window.document.body.style.background='transparent';
	 */
	@Multiline
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
	@Multiline
	final static String playsoundscript="AUDIO";


	String delimiter = "[,.?!;，。？！；\r\n]";
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
		if(true || TTSController_engine ==null) {
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
						if(mCurrentReadContext!=null)
							mCurrentReadContext.post(new Runnable() {
							@Override
							public void run() {
								mCurrentReadContext.findAllAsync(speakPool[speakPoolIndex]);
							}
						});
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
			targetRoot = PeruseView.root;
		boolean isNewHolder=false;
		boolean isInit=false;
		// 初始化核心组件
		if(TTSController_tv == null){
			isInit=isNewHolder=true;
			TTSController_ = (ViewGroup) getLayoutInflater()
					.inflate(R.layout.float_tts_basic, root, false);
			TTSController_.setOnClickListener(Utils.DummyOnClick);
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
					popupWord(null,-1, 0);
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
		mdict mCurrentDictionary;
		WebViewmy wv;
		String target;
		if(widget12.getTag(R.drawable.ic_click_search)==null) {
			if(PeruseViewAttached()){
				mCurrentDictionary = PeruseView.currentDictionary;
				wv = PeruseView.mWebView;
			}
			else {
				if (webholder.getChildCount() != 0) {
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
					mCurrentDictionary = md.get((Integer) webholder.getChildAt(selectedPos).getTag());
				} else {//单本阅读
					View SV = webSingleholder.getChildAt(0);
					int id = SV == null ? -1 : IU.parsint(SV.getTag(), -1);
					if (id >= 0 && id < md.size()) {
						mCurrentDictionary = md.get(id);
					} else {
						mCurrentDictionary = currentDictionary;
					}
				}
				if (mCurrentDictionary != null) {
					wv = mCurrentDictionary.mWebView;
				} else {
					wv = null;
				}
			}
			target = wv==null?null:wv.word;
		}
		else {
			widget12.setTag(R.drawable.ic_click_search, null);
			mCurrentDictionary = CCD;
			target = currentClickDisplaying;
			wv = popupWebView;
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
			else if (AutoBrowsePaused /*自动读时绕过*/ && mCurrentDictionary.hasMdd()) {
				/* 倾向于已经制定发音按钮 */
				wv.evaluateJavascript(WebviewSoundJS, value -> {
					//CMN.Log("WebviewSoundJS", value);
					if (!"10".equals(value)) {
						requestSoundPlayBack(finalTarget, mCurrentDictionary, wv);
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

	private mdict getCurrentReadContext() {
		if(webholder.getChildCount()!=0){
			int selectedPos=-1;
			/* 当前滚动高度 */
			int currentHeight=WHP.getScrollY();
			for(int i=0;i<webholder.getChildCount();i++) {
				if(webholder.getChildAt(i).getBottom()>currentHeight) {
					/* Frames累加高度 首次超出 滚动高度 */
					selectedPos=i;
					break;
				}
			}
			//应用中线法则
			currentHeight = currentHeight+WHP.getHeight()/2;
			if(selectedPos>=0){
				while(selectedPos+1<webholder.getChildCount() && webholder.getChildAt(selectedPos).getBottom()<currentHeight){
					selectedPos++;
				}
			}
			return md.get((Integer) webholder.getChildAt(selectedPos).getTag());
		}
		else{//单本阅读
			View SV = webSingleholder.getChildAt(0);
			int id = SV==null?-1:IU.parsint(SV.getTag(), -1);
			if(id>=0 && id<md.size()){
				return md.get(id);
			} else {
				return currentDictionary;
			}
		}
	}

	public WebViewmy getCurrentWebContext() {
		if(PeruseViewAttached())
			return PeruseView.mWebView;
		mdict mdTmp = getCurrentReadContext();
		return mdTmp==null?null:mdTmp.mWebView;
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
	@Multiline
	private String WebviewSoundJS="AUAUAU";

	private void requestSoundPlayBack(String finalTarget, mdict invoker, WebViewmy wv) {
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
		if(PeruseView!=null){
			return PeruseView.isAttached();
		}
		return false;
	}

	public boolean PeruseSearchAttached() {
		if(PeruseView!=null){
			return PeruseView.isAttached() && PeruseView.PerusePageSearchetSearch!=null;
		}
		return false;
	}

	PeruseView getPeruseView() {
		if(PeruseView==null) {
			PeruseView = new PeruseView();
			PeruseView.spsubs = opt.defaultReader.getFloat("spsubs", 0.706f);
			PeruseView.dm = dm;
			PeruseView.opt = opt;
			PeruseView.density = dm.density;
			PeruseView.addAll = opt.getPeruseAddAll();
		}
		return PeruseView;
	}

	void AttachPeruseView(boolean bRefresh) {
		try {
			if(PeruseView==null) return;
			if(!PeruseView.isAdded()) {
				//CMN.Log("AttachPeruseView 1 ", bRefresh);
				PeruseView.bCallViewAOA=true;
				/* catch : Can not perform this action after onSaveInstanceState */
				PeruseView.show(getSupportFragmentManager(), "PeruseView");
			} else if(PeruseView.mDialog!=null){
				//CMN.Log("AttachPeruseView 2 ", bRefresh);
				PeruseView.mDialog.show();
				PeruseView.onViewAttached(this,bRefresh);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	/** 接管跳转翻阅。来自包括分享中枢、get12菜单。*/
	public void JumpToPeruseModeWithWord(String content) {
		//CMN.Log(" Jump To Peruse ...  ", content);
		getPeruseView().prepareJump(this, content, null, 0);
		AttachPeruseView(content!=null);
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

	protected ListAdapter AppDataAdapter() {
		if(AppDataAdapter==null)
			AppDataAdapter = new DArrayAdapter(this);
		return AppDataAdapter;
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
			AppDataAdapter.createNewDatabase(etNew.getText().toString());
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

	public void showMultipleCollection(String text) {
		//showT(text);
		BottomSheetDialog _bottomPlaylist = bottomPlaylist==null?null:bottomPlaylist.get();
		if(_bottomPlaylist==null) {
			CMN.Log("重建底部弹出");
			bottomPlaylist = new WeakReference<>(_bottomPlaylist = new BottomSheetDialog(this));
			View ll = LayoutInflater.from(this).inflate(R.layout.favorite_bottom_sheet, null);
			ListView lv = ll.findViewById(R.id.favorList);
			lv.setAdapter(AppDataAdapter());
			lv.setOnItemClickListener((parent, view, position, id) -> {
				CheckedTextView tv = view.findViewById(android.R.id.text1);
				tv.toggle();
				tv.jumpDrawablesToCurrentState();
				AppDataAdapter.setChecked(position, tv.isChecked());
			});
			BottomSheetDialog final_bottomPlaylist = _bottomPlaylist;
			OnClickListener clicker = v -> {
				switch (v.getId()) {
					case R.id.cancel:
						final_bottomPlaylist.dismiss();
					break;
					case R.id.confirm:
						ArrayList<MyPair<String, LexicalDBHelper>> items = AppDataAdapter.items;
						HashSet<Integer> selection = AppDataAdapter.selectedPositions;
						if(selection.size()>0) {
							int cc=0;
							for (int i = 0; i < items.size(); i++) {
								if (selection.contains(i)) {
									try {
										MyPair<String, LexicalDBHelper> iI = items.get(i);
										LexicalDBHelper db = iI.value;
										if (db == null) {
											db = iI.value = new LexicalDBHelper(getApplicationContext(), opt, iI.key);
										}
										if(db.insertUpdate(text)>0){
											cc++;
										}
									} catch (Exception ignored) { }
								}
							}
							showT("添加完毕！("+cc+"/"+selection.size()+")");
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
			if(GlobalOptions.isDark){
				ll.setBackgroundColor(Color.BLACK);
				((TextView)ll.findViewById(R.id.title)).setTextColor(Color.WHITE);
				ll.findViewById(R.id.bottombar).getBackground().setColorFilter(GlobalOptions.NEGATIVE);
			}
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
			litsView.setAdapter(AppDataAdapter());
			AlertDialog finalD = d;
			litsView.setOnItemClickListener((parent, view, position, id) -> {
				int reason1 = (int) parent.getTag();
				MyPair<String, LexicalDBHelper> item = AppDataAdapter.items.get(position);
				String name = item.key;
				LexicalDBHelper _favoriteCon = item.value;
				if(_favoriteCon==null){
					_favoriteCon = item.value = new LexicalDBHelper(getApplicationContext(), opt, name);
				}
				if(reason1!=2 && DBrowser!=null && DBrowser.getFragmentId()==1){
					// 加载收藏夹
					opt.putCurrFavoriteDBName(name);
					favoriteCon = _favoriteCon;
					DBrowser.loadInAll(this);
				} else if(reason1==2 && DBrowser!=null){
					DBrowser.moveSelectedardsToDataBase(_favoriteCon);
				}
				if(reason1!=2)
					view.post(() -> {
						finalD.dismiss();
						if(reason1==0)
							show(R.string.currFavor,CMN.unwrapDatabaseName(name));
					});
			});
			d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v1 -> {
				AppDataAdapter.showDelete = !AppDataAdapter.showDelete;
				AppDataAdapter.notifyDataSetChanged();
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


	static HashSet<Integer> LongclickableMap = new HashSet<>();
	static class IconData{
		int number;
		/** 0=hide; 1=normal; 2=only in horizontal mode */
		int tmpIsFlag;
		final static int tmpFlagLen=2;
		IconData(int _number, int _tmpIsFlag){
			number = _number;
			tmpIsFlag = _tmpIsFlag;
			//CMN.Log("最大值：", 1<<6-1);
		}
		@Override
		public String toString() {
			String ret = Integer.toString(number);
			if(tmpIsFlag==0)
				ret = "\\"+ret;
			else if(tmpIsFlag==2)
				ret = "\\\\"+ret;
			return ret;
		}

		public void addString(StringBuilder sb) {
			if(tmpIsFlag==0)
				sb.append("\\");
			else if(tmpIsFlag==2)
				sb.append("\\\\");
			sb.append(number);
		}
	}

	static class AppUIProject{
		public int type=-1;
		public final String key;
		public boolean bNeedCheckOrientation;
		String currentValue;
		ViewGroup bottombar;
		ImageView[] btns;
		int[] icons;
		int[] ids;

		ArrayList<IconData> iconData;
		String[] titles;

		public AppUIProject(String _key, int[] _icons, int[] _ids, String customize_str, ViewGroup _bottombar, ImageView[] _btns) {
			key = _key;
			icons = _icons;
			ids = _ids;
			currentValue = customize_str;
			bottombar = _bottombar;
			btns = _btns;
		}
		
		public AppUIProject(int idx, PDICMainAppOptions opt, int[] _icons, int[] _ids, ViewGroup _bottombar, ImageView[] _btns) {
			key = "ctnp#"+idx;
			type = idx;
			icons = _icons;
			ids = _ids;
			currentValue = opt.getAppContentBarProject(key);
			//CMN.Log("重新读取", key);
			bottombar = _bottombar;
			btns = _btns;
		}
		
		public void instantiate(String[] _titles) {
			if(_titles!=null){
				titles = _titles;
			}
			int projectSize = icons.length;
			ArrayList<IconData> _iconData = new ArrayList<>(projectSize);
			if(currentValue!=null) {
				String[] arr = currentValue.split("\\|");
				for (int i = 0; i < arr.length; i++) {
					String val = arr[i];
					int start = 0;
					int end = val.length();
					if (end > 0) {
						while (start < end && val.charAt(start) == '\\') ++start;
						if (start > 0) val = val.substring(start, end);
						int id = IU.parsint(val, -1);
						_iconData.add(new IconData(id, start==0?1:start==1?0:start));
					}
				}
			}
			if(_iconData.size()<projectSize){// 查漏补缺
				ArrayList<Integer> iconDataExtra = new ArrayList<>();
				for (int i = 0; i < projectSize; i++) {
					iconDataExtra.add(i);
				}
				for (int i = 0; i < _iconData.size(); i++) {
					iconDataExtra.remove((Integer) _iconData.get(i).number);
				}
				boolean initialise = _iconData.size()==0;
				for (int i = 0; i < iconDataExtra.size(); i++) {
					int number = iconDataExtra.get(i);
					int start = initialise&&number<6?0:1;
					_iconData.add(new IconData(number, start==0?1:0));
				}
			}
			iconData = _iconData;
		}

		public boolean getTint() {
			return false;
		}

		public void clear(MainActivityUIBase a) {
			if(iconData!=null){
				iconData=null;
				if(a!=null){
					currentValue=null;
					a.RebuildBottombarIcons(this, a.mConfiguration);
				}
			}
		}
	}
	int ForegroundTint = 0xffffffff;
	PorterDuffColorFilter ForegroundFilter;
	//iii
	ImageView[] ContentbarBtns = new ImageView[20];
	/**  定制底栏一：<br/>
	 * 返回列表7 收藏词条8 跳转词典9 上一词条10 下一词条11 发音按钮12 <br/>
	 * 退离程序13 打开侧栏14 随机词条15 上一词典16 下一词典17  自动浏览18 全文朗读19 进入收藏20 进入历史21 调整亮度22 夜间模式23 切换横屏24 定制颜色25 定制底栏26 切换沉浸 切换全屏 多维分享 空格键 方向键⬅ 方向键➡ 方向键⬆ 方向键⬇ W键 A键 S键 D键 C键 Z键 CTRL键 SHIFT键 鼠标左击 鼠标右击 <br/>*/
	int[] ContentbarBtnIcons = new int[]{
			R.drawable.back_ic,
			R.drawable.star_ic,
			R.drawable.list_ic,
			R.drawable.chevron_left,
			R.drawable.chevron_right,
			R.drawable.voice_ic,
			R.drawable.ic_exit_to_app,//13
			R.drawable.ic_menu_grey_24dp,//14
			R.drawable.ic_shuffle,//15
			R.drawable.chevron_grey_top22,//16
			R.drawable.chevron_grey_bottom22,//17
			R.drawable.ic_skip_next_white,//18
			R.drawable.ic_fulltext_reader,//19
			R.drawable.favoriteg,//20
			R.drawable.historyg,//21
			R.drawable.ic_brightness_low_black_bk,//22
			R.drawable.dark_mode_small,//23
			R.drawable.ic_swich_landscape_black_24dp,//24
			R.drawable.color_grey_sheet,//25
			R.drawable.customizebtmbr,//26
	};
	static{
		LongclickableMap.addAll(Arrays.asList(1, 4, 6, 106, 107, 108, 109, 110, 111, 112, 114, 118, 119));
	}
	int[] ContentbarBtnIds = new int[]{
			R.id.browser_widget7,
			R.id.browser_widget8,
			R.id.browser_widget9,
			R.id.browser_widget10,
			R.id.browser_widget11,
			R.id.browser_widget12,
			R.id.exitApp,
			R.id.drawer_layout,
			R.id.random_entry,
			R.id.prv_dict,
			R.id.nxt_dict,
			R.id.auto_browse,
			R.id.tts_readAll,
			R.id.go_favorite,
			R.id.go_history,
			R.id.brightness,
			R.id.night_mode,
			R.id.switch_landscape,
			R.id.colorize,
			R.id.bottombar,
	};

	/** Rebuild Bottom Icons<br/>
	 * 定制底栏：一  见 {@link PDICMainActivity#BottombarBtnIcons}<br/>
	 * 定制底栏：二 见 {@link MainActivityUIBase#ContentbarBtnIcons}<br/>
	 */
	void RebuildBottombarIcons(AppUIProject bottombar_project, Configuration config) {
		ViewGroup bottombar;
		if(bottombar_project==null || (bottombar = bottombar_project.bottombar)==null) {
			return;
		}
		String appproject = bottombar_project.currentValue;
		boolean tint = bottombar_project.getTint();
		if(tint&&ForegroundFilter==null)
			ForegroundFilter = new PorterDuffColorFilter(ForegroundTint, PorterDuff.Mode.SRC_IN);
		if(appproject==null) appproject="0|1|2|3|4|5";
		//appproject="0|1|2|3|4|5|6|7|8|9|10|11|13|14|\\\\15";
		//appproject="0|1|2|3|4|5|6";
		//appproject="9|10|11|13|14|15";
		int idStart=0;
		bottombar.removeAllViews();
		if(bottombar.getId()==R.id.bottombar2)
			idStart=107;
		boolean isHorizontal = config.orientation==Configuration.ORIENTATION_LANDSCAPE;
		String[] arr = appproject.split("\\|");
		ImageView[] BottombarBtns = bottombar_project.btns;
		int[] BottombarBtnIcons = bottombar_project.icons;
		CMN.rt();
		for (int i = 0; i < arr.length; i++) {
			String val = arr[i];
			int start = 0;
			int end = val.length();
			if(end>0) {
				while (start<end && val.charAt(start)=='\\') {
					++start;
				}
				if(start>0){
					val = val.substring(start, end);
					if(start==2) bottombar_project.bNeedCheckOrientation=true;
				}
				if(start==0||start==2&&isHorizontal){
					int id = IU.parsint(val, -1);
					if (id >= 0 && id < BottombarBtns.length) {
						ImageView iv = BottombarBtns[id];
						if (iv == null) {
							iv = new ImageView(this);
							iv.setImageResource(BottombarBtnIcons[id]);
							iv.setBackgroundResource(R.drawable.surrtrip1);
							iv.setLayoutParams(widget10.getLayoutParams());
							iv.setId(bottombar_project.ids[id]);
							iv.setOnClickListener(this);
							if(tint) iv.setColorFilter(ForegroundFilter);
							if (LongclickableMap.contains(idStart+i)){
								iv.setOnLongClickListener(this);
							} else {
								iv.setLongClickable(false);
							}
							BottombarBtns[id] = iv;
						}
						else {
							ViewGroup svp = (ViewGroup) iv.getParent();
							if (svp != null) svp.removeView(iv);
						}
						bottombar.addView(iv);
					}
				}
			}
		}
		CMN.pt("重排耗时");
	}

	AppUIProject contentbar_project;
	
	AppUIProject peruseview_project;
	
	WeakReference<BottombarTweakerAdapter> WR_BottombarTweaker;

	void showIconCustomizator() {
		int pos = defbarcustpos;
		if(PeruseViewAttached()){
			pos = 2;
		} else if(this instanceof PDICMainActivity && contentview.getParent()!=null){
			pos = 1;
		}
		
		BottombarTweakerAdapter ada;
		if(WR_BottombarTweaker==null || WR_BottombarTweaker.get()==null){
			ada = new BottombarTweakerAdapter(this, pos);
			WR_BottombarTweaker = new WeakReference<>(ada);
		} else {
			ada = WR_BottombarTweaker.get();
			ada.show();
			ada.onClick(pos);
		}
		
		ada.main_list.mMaxHeight = (int) (root.getHeight() - root.getPaddingTop() - 4 * getResources().getDimension(R.dimen._50_));
	}
}
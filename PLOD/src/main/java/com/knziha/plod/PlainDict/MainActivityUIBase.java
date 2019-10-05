package com.knziha.plod.PlainDict;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.jaredrummler.colorpicker.ColorPickerDialogListener;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.myCpr;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_asset;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.dictionarymanager.files.BooleanSingleton;
import com.knziha.plod.widgets.ArrayAdaptermy;
import com.knziha.plod.widgets.CheckedTextViewmy;
import com.knziha.plod.widgets.IMPageSlider;
import com.knziha.plod.widgets.ListViewmy;
import com.knziha.plod.widgets.ListSizeConfiner;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.plod.widgets.ScrollViewmy;
import com.knziha.plod.widgets.SplitView;
import com.knziha.plod.widgets.SumsungLikeScrollBar;
import com.knziha.plod.widgets.WebViewmy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import db.LexicalDBHelper;
import db.MdxDBHelper;

/**
 * Created by KnIfER on 2018
 */
@SuppressLint("Registered")
public class MainActivityUIBase extends Toastable_Activity implements OnTouchListener, OnLongClickListener, OnClickListener, OnMenuItemClickListener, OnDismissListener  {
	//private static final String RegExp_VerbatimDelimiter = "[ ]{1,}|\\pP{1,}|((?<=[\\u4e00-\\u9fa5])|(?=[\\u4e00-\\u9fa5]))";
	public static final KeyEvent BackEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);

	public boolean hideDictToolbar=false;
	boolean bShowLoadErr=true;
	public boolean isCombinedSearching;

	public int GlobalPageBackground=-1;
	public SumsungLikeScrollBar mBar;
	public ViewGroup main;
	public ViewGroup webholder;
	public ScrollViewmy WHP;
	public ViewGroup webSingleholder;
	protected WindowManager wm;

	protected String lastEtString;
	public ViewGroup main_succinct;

	public ListViewmy lv,lv2;
	BasicAdapter adaptermy;
	public BasicAdapter adaptermy2;
	public BasicAdapter adaptermy3;
	public BasicAdapter PrevActivedAdapter;
	public BasicAdapter ActivedAdapter;
	public Handler hdl;

	public mdict currentDictionary;
	public mdict currentFilter;
	public int adapter_idx;
	HashSet<String> mdlibsCon;
	public List<mdict> md = new ArrayList<>();//Collections.synchronizedList(new ArrayList<mdict>());

	BufferedWriter output;
	BufferedWriter output2;
	final String favorTag = "favorites/";
	LexicalDBHelper favoriteCon;//public LexicalDBHelper getFDB(){return favoriteCon;};
	LexicalDBHelper historyCon;
	HashMap<String,String> checker;

	ImageView favoriteBtn;
	Drawable star_ic;
	Drawable star;

	SplitView webcontentlist;
	protected IMPageSlider IMPageCover;
	protected PeruseView PeruseView;
	public ViewGroup bottombar2;
	public boolean bWantsSelection;
	public boolean bIsFirstLaunch=true;

	public RLContainerSlider PageSlider;
	public boolean TurnPageEnabled;

	public View widget13,widget14;

	public ProgressBar main_progress_bar;
	protected int DockerMarginL,DockerMarginR,DockerMarginT,DockerMarginB;

	boolean isFragInitiated = false;

	Canvas mPageCanvas=new Canvas();
	Matrix HappyMatrix = new Matrix();
	BitmapDrawable mPageDrawable;

	AsyncTask lianHeTask;
	public int[] pendingLv2Pos;
	public int pendingLv2ClickPos=-1;
	public int split_dict_thread_number;
	public static final ListSizeConfiner mListsizeConfiner = new ListSizeConfiner();

	public void countDelta(int delta) {
		Lock lock = new ReentrantLock();
		lock.lock();
		poolEUSize+=delta;
		lock.unlock();
	}
	public volatile int poolEUSize;

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
	public void switchToDictIdx(int i) {
		currentDictionary = md.get(adapter_idx = i);
		opt.putLastMd(currentDictionary._Dictionary_fName);
		adaptermy.notifyDataSetChanged();
		//lv.setSelection(currentDictionary.lvPos);
		lv.setSelectionFromTop(currentDictionary.lvPos, currentDictionary.lvPosOff);
	}
	public void switchToSearchModeDelta(int i) {

	}


	public String getSearchTerm() {
		return "happy";
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
		super.onCreate(savedInstanceState);
	}


	void closeIfNoActionView(MenuItemImpl mi) {
		if(!mi.isActionButton()) toolbar.getMenu().close();
	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	@Override
	protected void scanSettings() {
		CMN.GlobalPageBackground = GlobalPageBackground = opt.getGlobalPageBackground();
		mdict.bGlobalUseClassicalKeycase = opt.getClassicalKeycaseStrategy();
		opt.getLastPlanName();
	}



	@Override
	protected void further_loading(Bundle savedInstanceState) {
		//stst = System.currentTimeMillis();
		super.further_loading(savedInstanceState);
		opt.isLarge = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=3 ;
		//CMN.show("isLarge"+isLarge);
		mdict.def_zoom=dm.density;
		mdict.optimal100 = opt.isLarge?150:100;
		mdict.def_fontsize = opt.getDefaultFontScale(mdict.optimal100);

		opt.currFavoriteDBName = opt.getCurrFavoriteDBName();
		if(opt.currFavoriteDBName==null || !opt.currFavoriteDBName.startsWith(favorTag))
			opt.currFavoriteDBName = "favorites/favorite";
		new File(opt.pathToInternal().append("favorites").toString()).mkdirs();
		new File(opt.pathToMain()+"CONFIG").mkdirs();

		favoriteCon = new LexicalDBHelper(this,opt.currFavoriteDBName);
		historyCon = new LexicalDBHelper(this,"history");

		new File(opt.pathToMain()).mkdirs();

		final File def = new File(getExternalFilesDir(null),"default.txt");      //!!!原配
		File rec = new File(opt.pathToMain()+"CONFIG/mdlibs.txt");
		final boolean retrieve_all=!def.exists();

		if(retrieve_all) {
			try {
				md.add(new mdict_asset("liba.mdx", this));
			} catch (IOException e) {
				Log.e("ffatal", "sda");
			}
		}
		else try {
				BufferedReader in = new BufferedReader(new FileReader(def));
				String line = in.readLine();
				while(line!=null){
					try {
						boolean isFilter=false;
						if(line.startsWith("[:F]")){
							line = line.substring(4);
							isFilter=true;
						}
						if(!line.startsWith("/"))
							line=opt.lastMdlibPath+"/"+line;
						mdict mdtmp = new_mdict(line,this);
						if(mdtmp._Dictionary_fName.equals(opt.getLastMdFn()))
							adapter_idx = md.size();
						md.add(mdtmp);
						if(isFilter)
							currentFilter =mdtmp;
					} catch (Exception e) {
						e.printStackTrace();
						if(trialCount==-1)if(bShowLoadErr)
							show(R.string.err, new File(line).getName(),line,e.getLocalizedMessage());
					}
					line = in.readLine();
				}
				in.close();
			} catch (IOException e2) { e2.printStackTrace(); }
		//stst = System.currentTimeMillis();

		//dbCon = new DBWangYiLPController(this,true);   getExternalFilesDir(null).getAbsolutePath()
		mdlibsCon = new HashSet<>();
		checker = new HashMap<>();
		if(rec.exists())
			try {
				BufferedReader in = new BufferedReader(new FileReader(rec));

				String line = in.readLine();
				while(line!=null){
					mdlibsCon.add(line);															   //!!!旧爱
					File check;
					if(!line.startsWith("/"))
						check=new File(opt.lastMdlibPath,line);
					else
						check=new File(line);
					if(!check.exists())
						checker.put(check.getName(),check.getAbsolutePath());// check for mdict_noexists
					line = in.readLine();
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

		try {
			output = new BufferedWriter(new FileWriter(rec,true));
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		//	mdlibsCon.clear();
		//dbCon.prepareContain();
		File mdlib = new File(opt.lastMdlibPath);
		if(mdlib.exists() && mdlib.isDirectory() && mdlib.canRead())
		{
			File [] arr = mdlib.listFiles(pathname -> {
				if(pathname.isFile()) {
					String fn = pathname.getName();
					if(fn.toLowerCase().endsWith(".mdx")) {
						if(retrieve_all || !mdlibsCon.contains(fn)) {                                            //!!!新欢
							try {
								if(output2==null) output2 = new BufferedWriter(new FileWriter(def,true));
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
			if(arr!=null)
				for(final File i:arr){
					try{
						mdict mdtmp = new mdict(i.getAbsolutePath(),this);
						md.add(mdtmp);
					}catch (Exception e){
						e.printStackTrace();
						//show(R.string.err,i.getName(),i.getAbsolutePath(),e.getLocalizedMessage());
					}
				}
			try {
				if(output2!=null) {
					output2.flush();
					output2.close();
				}
				output.flush();
				output.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}


		if(adapter_idx<0)
			adapter_idx=0;
		if(md.size()>0)
			currentDictionary = md.get(adapter_idx);
		//dbCon.refresh();
		//Log.d("!!! dbTest111", (System.currentTimeMillis()-stst)+"");

		//dbCon.getDB().execSQL("show index from mdictlib");


		//String sql = "select * from " + dbCon.TABLE_MDXES;

		//Cursor cursor = dbCon.getDB().rawQuery(sql, new String[]{});
		//Log.e("dbTest", cursor.getCount()+"");
		//while(cursor.moveToNext())
		//	Log.e("dbTest", cursor.getString(0)+"");

		//CMN.show("regTime: "+(System.currentTimeMillis()-st22));

		root = findViewById(R.id.root);

		findViewById(R.id.toolbar_action1).setOnClickListener(this);
		toolbar.setOnMenuItemClickListener(this);
		toolbar.setOnLongClickListener(v -> {
			//md.showToast(""+v.getId());
			return false;
		});

		R.styleable.single[0] = android.R.attr.actionBarSize;
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
		TypedArray array = obtainStyledAttributes(typedValue.resourceId, R.styleable.single);
		actionBarSize = array.getDimensionPixelSize(0, (int) (56*dm.density));
		array.recycle();

		webholder = contentview.findViewById(R.id.webholder);
		WHP = (ScrollViewmy) webholder.getParent();
		webSingleholder = contentview.findViewById(R.id.webSingleholder);

		mBar = contentview.findViewById(R.id.dragScrollBar);
		mBar.setOnProgressChangedListener(_mProgress -> {
			if(PageSlider==null) return;
			if(_mProgress==-1) {
				PageSlider.TurnPageEnabled=false;
			}else if(_mProgress==-2) {
				PageSlider.TurnPageEnabled=TurnPageEnabled;
			}
		});


		webcontentlist.multiplier=-1;
		webcontentlist.isSlik=true;

		CachedBBSize=(int)Math.max(20*dm.density, Math.min(CachedBBSize, 50*dm.density));
		webcontentlist.setPrimaryContentSize(CachedBBSize,true);

		webcontentlist.setPageSliderInf(new SplitView.PageSliderInf() {//这是底栏的动画特效
			int height;
			@Override
			public void onPreparePage(int val) {
				IMPageSlider IMPageCover_ = IMPageCover;
				if(PeruseView!=null && PeruseView.getView().getParent() !=null)
					IMPageCover_=PeruseView.IMPageCover;
				//showT("onPreparePage"+System.currentTimeMillis());
				height=val;
				LayoutParams lpp = IMPageCover.getLayoutParams();
				lpp.height=val;
				IMPageCover_.setLayoutParams(lpp);
				IMPageCover_.setAlpha(1.f);
				IMPageCover_.setImageBitmap(null);
				if(IMPageCover_.getTag()==null) {
					if(Build.VERSION.SDK_INT>23) {
						IMPageCover_.getForeground().setTint(MainBackground);
					}
					IMPageCover_.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
					IMPageCover_.setTag(false);
				}
				IMPageCover_.setVisibility(View.VISIBLE);

			}

			@Override
			public void onMoving(SplitView webcontentlist,float val) {
				//showT("onMoving"+System.currentTimeMillis());
				IMPageSlider IMPageCover_ = IMPageCover;
				RLContainerSlider PageSlider_ = PageSlider;
				if(PeruseView!=null && PeruseView.getView().getParent() !=null) {
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
				IMPageSlider IMPageCover_ = IMPageCover;
				boolean bPeruseIncharge = PeruseView!=null && PeruseView.getView()!=null && PeruseView.getView().getParent()!=null && (PeruseView.contentview.getParent()==PeruseView.slp || PeruseView.contentview.getParent()==PeruseView.mlp);
				if(PeruseView!=null && PeruseView.getView().getParent()!=null)
					IMPageCover_=PeruseView.IMPageCover;
				IMPageCover_.setVisibility(View.GONE);
				if(bPeruseIncharge) {
					opt.setPeruseBottombarOnBottom(webcontentlist.getChildAt(0).getId()!=R.id.bottombar2);
				}else {
					opt.setBottombarOnBottom(webcontentlist.getChildAt(0).getId()!=R.id.bottombar2);
				}
				if(opt.getNavigationBtnType()==2) {
					locateNaviIcon(widget13,widget14);
				}
			}

			@Override
			public void onHesitate() {
				//showT("onHesitate"+System.currentTimeMillis());
				IMPageSlider IMPageCover_ = IMPageCover;
				if(PeruseView!=null && PeruseView.getView()!=null && PeruseView.getView().getParent()!=null)
					IMPageCover_=PeruseView.IMPageCover;
				IMPageCover_.setVisibility(View.GONE);
			}

			@Override
			public void SizeChanged(int newSize, float delta) {}

			@Override
			public void onDrop(int size) {
			}

			@Override
			public int preResizing(int size) {
				boolean bPeruseIncahrge = PeruseView!=null && PeruseView.getView().getParent() !=null && (PeruseView.contentview.getParent()==PeruseView.slp || PeruseView.contentview.getParent()==PeruseView.mlp);
				int ret = (int) Math.max(((bPeruseIncahrge&&!opt.getPeruseBottombarOnBottom())?30:20)*dm.density, Math.min(50*dm.density, size));
				if(bPeruseIncahrge) {
					PeruseView.CachedBBSize = ret;
				}else
					CachedBBSize = ret;
				return ret;
			}
		});


		bottombar2.setBackgroundColor(MainBackground);

		ivDeleteText = findViewById(R.id.ivDeleteText);
		ivBack = findViewById(R.id.ivBack);
		ivDeleteText.setOnClickListener(this);
		ivBack.setOnClickListener(this);

		bottombar2.findViewById(R.id.browser_widget7).setOnClickListener(this);
		bottombar2.findViewById(R.id.browser_widget7).setOnLongClickListener(this);
		favoriteBtn = bottombar2.findViewById(R.id.browser_widget8);
		favoriteBtn.setOnClickListener(this);
		favoriteBtn.setOnLongClickListener(this);
		bottombar2.findViewById(R.id.browser_widget9).setOnLongClickListener(this);
		bottombar2.findViewById(R.id.browser_widget9).setOnClickListener(this);
		bottombar2.findViewById(R.id.browser_widget10).setOnClickListener(this);
		bottombar2.findViewById(R.id.browser_widget11).setOnClickListener(this);
		bottombar2.findViewById(R.id.browser_widget12).setOnClickListener(this);


		(widget13=PageSlider.findViewById(R.id.browser_widget13)).setOnClickListener(this);
		(widget14=PageSlider.findViewById(R.id.browser_widget14)).setOnClickListener(this);

		if(isCombinedSearching)
			toolbar.getMenu().findItem(R.id.toolbar_action1).setIcon((ContextCompat.getDrawable(this,R.drawable.ic_btn_multimode)));
		//if(opt.isShowDirectSearch()) ((MenuItem)toolbar.getMenu().findItem(R.id.toolbar_action2)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);


		etSearch = findViewById(R.id.etSearch);
		/*try {//修改光标的颜色（反射）
			Field field;// Get the cursor resource id
			field = TextView.class.getDeclaredField("mEditor");// Get the etSearch_editor
			field.setAccessible(true);
			etSearch_editor = field.get(etSearch);
			et_ed_field_mCursorDrawable = etSearch_editor.getClass().getDeclaredField("mCursorDrawable");// Get the drawable and set a color filter
			et_ed_field_mCursorDrawable.setAccessible(true);
			editext_cursor_oval = ContextCompat.getDrawable(etSearch.getContext(), R.drawable.editext_cursor);
			//mCursorDrawable_Raw = (Drawable[]) et_ed_field_mCursorDrawable.get(etSearch_editor);
			mCursorDrawable_RawLet = ContextCompat.getDrawable(etSearch.getContext(), R.drawable.editext_cursor_raw);

			//field = editor.getClass().getDeclaredField("mSelectHandleCenter");
			//field.setAccessible(true);
			//Drawable mSelectHandleCenter = (Drawable) field.get(editor);
			//mSelectHandleCenter.setColorFilter(Color.parseColor("#2b4381"), PorterDuff.Mode.SRC_IN);
			//field.set(editor, mSelectHandleCenter);
		} catch (Exception e) {}*/

		main_succinct = findViewById(R.id.mainframe);
		main_progress_bar = findViewById(R.id.main_progress_bar);

	}


	int etSearch_toolbarMode=0;
	public ListViewmy.OnScrollChangeListener onWebScrollChanged;
	public void initWebScrollChanged() {
		if(onWebScrollChanged==null) {
			onWebScrollChanged= (v, l, t, oldl, oldt) -> {
				WebViewmy webview = (WebViewmy) v;
				if(layoutScrollDisabled )
				{
					final int lalaX=IU.parsint(v.getTag(R.id.toolbar_action1));
					final int lalaY=IU.parsint(v.getTag(R.id.toolbar_action2));
					if(lalaY!=-1 && lalaX!=-1) {
						v.scrollTo(lalaX, lalaY);
						v.setLayoutParams(v.getLayoutParams());
						//CMN.Log("scrolling to: "+lalaY);
					}
				}

				boolean fromPeruseView=v.getTag(R.id.position)!=null;
				SumsungLikeScrollBar mBar_=mBar;
				float currentScale;
				RLContainerSlider PageSlider_;
				if(!fromPeruseView) {
					Integer selfAtIdx = IU.parseInt(v.getTag());
					if(selfAtIdx==null) return;
					currentScale = md.get(selfAtIdx).webScale;
					PageSlider_=PageSlider;
				}else {
					currentScale = PeruseView.webScale;
					PageSlider_=PeruseView.PageSlider;
				}

				if(currentScale>=mdict.def_zoom) {
					if(fromPeruseView) {
						mBar_= PeruseView.mBar;
						if(opt.getZoomedInCanSlideTurnPage()) {
							PageSlider_.LeftEndReached=l==0;
							PageSlider_.RightEndReached=(v.getScrollX()+3+v.getMeasuredWidth())*dm.density+0.5f   >=  (int)(v.getMeasuredWidth()*currentScale);
							//CMN.Log("page position: "+((v.getScrollX()+3+v.getMeasuredWidth())*dm.density+0.5f )  +":"+   (int)(v.getMeasuredWidth()*currentScale));
						}
					}else {
						if(opt.getZoomedInCanSlideTurnPage()) {
							PageSlider_.LeftEndReached=l==0;
							PageSlider_.RightEndReached=(v.getScrollX()+3+v.getMeasuredWidth())*dm.density+0.5f   >= (int)(v.getMeasuredWidth()*currentScale);
							//CMN.Log("page position: "+((v.getScrollX()+3+v.getMeasuredWidth())*dm.density+0.5f )  +":"+   (int)(v.getMeasuredWidth()*currentScale));
						}
					}
				}else {
					PageSlider_.LeftEndReached=false;
					PageSlider_.RightEndReached=false;
				}

				if(fromPeruseView || !isCombinedSearching || (ActivedAdapter!=null && !(ActivedAdapter.combining_search_result instanceof resultRecorderCombined))) {
					mBar_.updateScrollState(v);
					if(!mBar_.isHeld()){
						mBar_.setMax(webview.getContentHeight()-v.getHeight());
						mBar_.setProgress(webview.getContentOffset());
					}
					mBar_.fadeIn();
				}
			};
		}
	}

	boolean etSearch_ToToolbarMode(int mode) {
		switch(mode){
			case 0://图标:搜索
				if((etSearch_toolbarMode&1)!=0) {
					etSearch_toolbarMode&=2;
					ivBack.setImageResource(R.drawable.search_toolbar);
				}
				return true;
			case 1://图标:返回
				if((etSearch_toolbarMode&1)!=1) {
					etSearch_toolbarMode|=1;
					ivBack.setImageResource(R.drawable.back_toolbar);
				}
				return true;
			case 3://图标:删除
				if((etSearch_toolbarMode&2)!=0) {
					etSearch_toolbarMode&=1;
					ivDeleteText.setImageResource(R.drawable.close_toobar);
				}
				return true;
			case 4://图标:撤销
				if((etSearch_toolbarMode&2)!=2) {
					etSearch_toolbarMode|=2;
					ivDeleteText.setImageResource(R.drawable.undo_toolbar);
				}
				return true;
			default:return false;
		}
	}

	public boolean checkDicts() {
		if(md.size()>0) {
			if(currentDictionary==null)
				currentDictionary=md.get((adapter_idx<0||adapter_idx>=md.size())?0:adapter_idx);
			return true;
		}
		return false;
	}

	@Override
	protected void onDestroy(){
		if(systemIntialized) {
			for(mdict mdTmp:md) {
				mdTmp.unload();
			}
			md.clear();
			if(favoriteCon!=null) favoriteCon.close();
			if(historyCon!=null)  historyCon.close();
			webSingleholder.removeAllViews();
			webholder.removeAllViews();

			if(ucc!=null) {
				ucc.invoker=null;
				ucc=null;
			}
		}
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
						if(!mdTmp.bUseInternalFS)
							mdTmp.mWebView.getSettings().setTextZoom(targetLevel);
					} catch (Exception ignored) {}
				}
			}
		}
		opt.putDefaultFontScale(mdict.def_fontsize);
	}



	protected int actionBarSize;
	void setContentBow(boolean bContentBow) {
		//actionBarSize=toolbar.getHeight();
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) contentview.getLayoutParams();
		if(lp.topMargin==0) {
			if(bContentBow) {
				lp.setMargins(0,actionBarSize, 0, 0);
				contentview.setLayoutParams(lp);
			}
		}else {
			if(!bContentBow) {
				lp.setMargins(0,0, 0, 0);
				contentview.setLayoutParams(lp);
			}
		}
	}

	void refreshContentBow(boolean bContentBow) {
		if(bContentBow) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) contentview.getLayoutParams();
			lp.setMargins(0,actionBarSize, 0, 0);
			contentview.setLayoutParams(lp);
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

	public void decorateContentviewByKey(ImageView favoriteBtn,String key) {
		if(favoriteBtn==null) favoriteBtn=this.favoriteBtn;
		favoriteCon.prepareContain();
		if(favoriteCon.contains(key)) {
			if(star_ic==null) {
				star_ic = getResources().getDrawable(R.drawable.star_ic_solid);
				star = favoriteBtn.getDrawable();
			}
			favoriteBtn.setImageDrawable(star_ic);
		}else if(star!=null)
			favoriteBtn.setImageDrawable(star);
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


	public static mdict new_mdict(String fn, MainActivityUIBase THIS) throws IOException {
		if(fn.startsWith("/ASSET/")) {
			if(CMN.AssetMap.containsKey(fn))
				return new mdict_asset(fn.substring(7),THIS);
		}
		return new mdict(fn,THIS);
	}

	public void NotifyComboRes(int size) {
		if(opt.getNotifyComboRes()) {
			float fval = 0.5f;
			ViewGroup sv;
			if(bIsFirstLaunch||bWantsSelection) {
				sv=contentview;
				fval=.8f;
			}else {
				sv=main_succinct;
			}
			showTopSnack(sv, getResources().getString(R.string.cbflowersnstr,opt.lastMdPlanName,md.size(),size), fval, -1, -1);
		}
	}

	void Snack(ViewGroup vg, float alpha, String msg, int len) {
		//snack=TSnackbar.makeraw(vg, msg, len);
		//snack.getView().setAlpha(alpha);
		//snack.show();
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


	public final class UniCoverClicker implements OnClickListener{
		mdict invoker;
		MdxDBHelper con;
		AlertDialog d;
		CircleCheckBox cb;
		ImageView tools_lock;
		UniCoverClicker(){
			final Resources r = getResources();
			itemsA = r.getStringArray(R.array.dict_tweak_arr);
			itemsB = r.getStringArray(R.array.dict_tweak_arr2);
			bmAdd=itemsA[0];
			lastInDark= GlobalOptions.isDark;
		}
		public void setInvoker(mdict mdict) {
			invoker=mdict;
		}
		boolean bResposibleForCon=false;
		boolean doCloseOnDiss=true;
		String[] itemsA;
		String[] itemsB;
		String bmAdd;
		boolean bFromWebView;
		boolean bFromPeruseView;
		ViewGroup bottomView;
		private int currentPos;
		boolean lastInDark;
		protected ObjectAnimator objectAnimator;
		int lastBookMarkPosition;
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.color:{
					String msg;
					if(invoker.bUseInternalBG) {
						//正在为词典  <![CDATA[<%1$s>]]> 指定背景颜色...
						msg=getResources().getString(R.string.BGMSG,invoker._Dictionary_fName);
					}
					else {
						msg=getResources().getString(R.string.BGMSG2);
					}
					//DialogSnack(d,msg);
					d.hide();
					ViewGroup target=bFromPeruseView?PeruseView.contentview:contentview;
					showTopSnack(target, msg, 0.8f, -1, -1);

					ColorPickerDialog asd =
							ColorPickerDialog.newBuilder()
									.setDialogId(123123)
									.setInitialColor(invoker.bUseInternalBG?(invoker.bgColor==null?(invoker.bgColor=CMN.GlobalPageBackground):invoker.bgColor):GlobalPageBackground)
									.create();
					asd.setColorPickerDialogListener(new ColorPickerDialogListener() {
						@Override
						public void onColorSelected(ColorPickerDialog dialogInterface, int Color) {
							if(invoker.bUseInternalBG)
								invoker.bgColor=Color;
							else
								CMN.GlobalPageBackground=Color;
							isDirty=true;
						}

						@Override
						public void onPreviewSelectedColor(ColorPickerDialog dialogInterface, int color) {
							if(GlobalOptions.isDark)
								color=ColorUtils.blendARGB(color, Color.BLACK, ColorMultiplier_Web);
							WebViewmy mWebView=bFromPeruseView?PeruseView.mWebView:invoker.mWebView;
							ViewGroup webSingleholder=bFromPeruseView?PeruseView.webSingleholder: MainActivityUIBase.this.webSingleholder;
							if(invoker.bUseInternalBG) {
								color=ColorUtils.blendARGB(color, Color.BLACK, ColorMultiplier_Web2);
								if(mWebView!=null)
									mWebView.setBackgroundColor(color);
							}else {
								webSingleholder.setBackgroundColor(color);
							}
						}

						@Override
						public void onDialogDismissed(ColorPickerDialog dialogInterface, int color) {
							d.show();
							WebViewmy mWebView=bFromPeruseView?PeruseView.mWebView:invoker.mWebView;
							int ManFt_invoker_bgColor=invoker.bgColor;
							int ManFt_GlobalPageBackground=GlobalPageBackground;
							if(GlobalOptions.isDark) {
								ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web);
								ManFt_GlobalPageBackground=ColorUtils.blendARGB(ManFt_GlobalPageBackground, Color.BLACK, ColorMultiplier_Web);
							};
							if(isDirty) {
								if(invoker.bUseInternalBG) {
									ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web2);
									invoker.WriteConfigInt(invoker.CONFIGPOS[0],invoker.bgColor);
									if(mWebView!=null)
										mWebView.setBackgroundColor(ManFt_invoker_bgColor);
								}else {
									GlobalPageBackground=CMN.GlobalPageBackground;
									webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									WHP.setBackgroundColor(ManFt_GlobalPageBackground);
									if(bFromPeruseView)PeruseView.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									opt.putGlobalPageBackground(CMN.GlobalPageBackground);
								}
							}else {//fall back
								if(invoker.bUseInternalBG) {
									ManFt_invoker_bgColor=ColorUtils.blendARGB(ManFt_invoker_bgColor, Color.BLACK, ColorMultiplier_Web2);
									if(mWebView!=null)
										mWebView.setBackgroundColor(ManFt_invoker_bgColor);
								}else {
									webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
									WHP.setBackgroundColor(ManFt_GlobalPageBackground);
									if(bFromPeruseView)PeruseView.webSingleholder.setBackgroundColor(ManFt_GlobalPageBackground);
								}
							}
						}

						boolean isDirty;
					});
					asd.show(getSupportFragmentManager(),"color-picker-dialog");

				}
				return;
				case R.id.settings:
					invoker.showDictTweaker(MainActivityUIBase.this);
				return;
				case R.id.appsettings:
					showAppTweaker();
				return;
				case R.id.lock:
					if(PageSlider.TurnPageEnabled=!PageSlider.TurnPageEnabled)
						tools_lock.setImageResource(R.drawable.un_locked);
					else
						tools_lock.setImageResource(R.drawable.locked);
					opt.setTurnPageEnabled(TurnPageEnabled=PageSlider.TurnPageEnabled);
					opt.putFirstFlag();
					showTopSnack((ViewGroup) d.getListView().getRootView(), PageSlider.TurnPageEnabled?R.string.PT1:R.string.PT2
							, 0.8f, LONG_DURATION_MS, -1);
				return;
			}
			imm.hideSoftInputFromWindow(main.getWindowToken(),0);
			bFromWebView=v.getTag()!=null;
			bFromPeruseView=v.getTag(R.id.position)!=null;
			v.setTag(null);
			currentPos=bFromPeruseView?PeruseView.currentPos:invoker.currentPos;

			if(invoker.con==null) bResposibleForCon=true;
			con = invoker.getCon();
			if(con.containsRaw(currentPos)) {
				itemsA[0]=getString(R.string.bmSub);
			}else
				itemsA[0]=bmAdd;
			if(GlobalOptions.isDark!=lastInDark || needReCreateUcc || d==null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUIBase.this,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);//,
				builder.setItems(bFromWebView?itemsB:itemsA,null);
				d = builder.create();

				bottomView = (ViewGroup) inflater.inflate(R.layout.checker2,null);
				//ImageView toolbar_settings = (ImageView)bottomView.findViewById(R.id.settings);
				//ImageView toolbar_colors = (ImageView)bottomView.findViewById(R.id.color);

				bottomView.findViewById(R.id.settings).setOnClickListener(this);
				bottomView.findViewById(R.id.appsettings).setOnClickListener(this);
				bottomView.findViewById(R.id.color).setOnClickListener(this);
				//if(false)
				bottomView.setOnLongClickListener(v1 -> {
					View vv = d.getWindow().getDecorView();
					float ali = vv.getAlpha();
					if(objectAnimator!=null) objectAnimator.cancel();
					objectAnimator = ObjectAnimator.ofFloat(vv,"alpha",ali,ali<0.9?1:0.2f);
					objectAnimator.setDuration(120);
					if(false) {
						if(ali==1) {
							d.setCanceledOnTouchOutside(false);
							d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
						}else {
							d.setCanceledOnTouchOutside(true);
							d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
						}
					}
					objectAnimator.start();
					return true;
				});

				cb = bottomView.findViewById(R.id.checker);
				tools_lock  = bottomView.findViewById(R.id.lock);
				if(GlobalOptions.isDark)
					cb.drawInnerForEmptyState=true;
				else
					cb.circle_shrinkage=2;
				cb.setChecked(opt.getPinDialog());
				cb.setOnClickListener(v12 -> {
					cb.toggle();
					opt.setPinDialog(cb.isChecked());
				});

				d.setOnDismissListener(dialog -> {
					if(doCloseOnDiss && bResposibleForCon) {
						invoker.closeCon();
					}
				});

				if(PageSlider!=null) {
					if(!PageSlider.TurnPageEnabled)
						tools_lock.setImageResource(R.drawable.locked);
					tools_lock.setOnClickListener(this);
				}else
					tools_lock.setVisibility(View.GONE);


				AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if(!bFromWebView)
							switch(position) {
								case 0:
									if(itemsA[0].equals(getString(R.string.bmSub)))
										if(con.remove(currentPos)>0) {
											showX(R.string.delDone,0);//
											((TextView)view.findViewById(android.R.id.text1)).setText(itemsA[0]=bmAdd);
										}
										else
											showT("删除失败,数据库出错...",0);
									else// add
										if(con.insert(currentPos)!=-1) {
											showX(R.string.bmAdded,0);
											int BKHistroryVagranter = opt.getInt("bkHVgrt",-1);
											BKHistroryVagranter = (BKHistroryVagranter+1)%20;
											String rec = invoker.f().getAbsolutePath()+"/?Pos="+currentPos;
											opt.putter()//.putString("bkmk", rec)
													.putString("bkh"+BKHistroryVagranter, rec)
													.putInt("bkHVgrt", BKHistroryVagranter)
													.commit();
											((TextView)view.findViewById(android.R.id.text1)).setText(itemsA[0]=getString(R.string.bmSub));
										}
										else
											showT("添加失败,数据库出错...",0);
									if(mBookMarkAdapter!=null)if(mBookMarkAdapter.cr!=null) {
										mBookMarkAdapter.cr.close();
										mBookMarkAdapter.cr=null;
									}
									break;
								case 1://书签列表
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

									AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUIBase.this,lastInDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);//,R.style.DialogStyle2Line);
									builder.setTitle(invoker._Dictionary_fName+" -> "+getString(R.string.bm));
									builder.setItems(new String[] {},null);
									builder.setNeutralButton(R.string.delete, null);
									final AlertDialog d = builder.create();

									d.getWindow().setBackgroundDrawableResource(lastInDark?R.drawable.popup_shadow_d:R.drawable.popup_shadow_l);
									d.show();
									d.getListView().setAdapter(getBookMarkAdapter(lastInDark,invoker,con));
									//d.getListView().setFastScrollEnabled(true);

									if(lastBookMarkPosition!=-1) {
										d.getListView().setSelection(lastBookMarkPosition);
									}


									d.getListView().setOnItemClickListener((parent1, view1, position1, id1) -> {
										int id11 = (int) mBookMarkAdapter.getItem(position1);
										myWebClient.shouldOverrideUrlLoading(bFromPeruseView?PeruseView.mWebView:invoker.mWebView, "entry://@"+id11);
										opt.putString("bkmk", invoker.f().getAbsolutePath()+"/?Pos="+id11);
										//if(!cb.isChecked())
										d.dismiss();
									});
									d.getListView().addOnLayoutChangeListener(MainActivityUIBase.mListsizeConfiner.setMaxHeight((int) (root.getHeight()-root.getPaddingTop()-2.8*getResources().getDimension(R.dimen._50_))));
									d.getListView().setTag(con);
									d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v13 -> {
										mBookMarkAdapter.showDelete = !mBookMarkAdapter.showDelete;
										mBookMarkAdapter.notifyDataSetChanged();
									});
									d.setOnDismissListener(dialog -> {
										lastBookMarkPosition=d.getListView().getFirstVisiblePosition();
										if(!doCloseOnDiss && bResposibleForCon) {
											invoker.closeCon();
											mBookMarkAdapter.clear();
											mBookMarkAdapter.cr.close();
											mBookMarkAdapter.cr=null;
										}
										doCloseOnDiss=true;
									});
									if(!cb.isChecked())
										doCloseOnDiss=false;
									//CMN.show("jianbiaoshijian" + ( System.currentTimeMillis()-stst));
									break;
								case 2:
								case 3:
									int targetLevel=invoker.bUseInternalFS?invoker.mWebView.getSettings().getTextZoom():invoker.def_fontsize;
									if(position==2) targetLevel+=10;
									else targetLevel-=10;
									targetLevel=targetLevel>500?500:targetLevel;
									targetLevel=targetLevel<10?10:targetLevel;
									WebView wv = bFromPeruseView?PeruseView.mWebView:invoker.mWebView;
									wv.getSettings().setTextZoom(targetLevel);
									if(invoker.bUseInternalFS) {
										invoker.WriteConfigInt(invoker.CONFIGPOS[1], targetLevel);
										showT((invoker.internalScaleLevel=targetLevel)+"%",0);
									}else {
										notifyGLobalFSChanged(targetLevel);
										showT("("+getResources().getString(R.string.GL)+") "+(targetLevel)+"%",0);//全局
									}
									break;
							}else
							switch(position) {//xx
								case 0://收藏
									break;
								case 1://高亮
									invoker.mWebView.evaluateJavascript(invoker.mWebView.getHighLightIncantation(), null);
									break;
								case 2://下划线
									break;
								case 3://清除高亮
									invoker.mWebView.evaluateJavascript(invoker.mWebView.getDeHighLightIncantation(), null);
									break;
								case 4://清除下划线
									break;
							}
						if(!cb.isChecked())
							d.dismiss();
					}
				};

				d.getListView().setOnItemClickListener(listener);
				d.getListView().setOnItemLongClickListener((parent, view, position, id) -> {
					if(!bFromWebView)
						if(position==2 || position==3) {
							int targetLevel=invoker.getFontSize();
							switch(position) {
								case 2:
									if(targetLevel<mdict.optimal100) {
										invoker.mWebView.getSettings().setTextZoom(targetLevel=mdict.optimal100);
									}else if(targetLevel<500) {
										invoker.mWebView.getSettings().setTextZoom(targetLevel=500);
									}
									else
										targetLevel=-1;
									break;
								case 3:
									if(targetLevel>mdict.optimal100) {
										invoker.mWebView.getSettings().setTextZoom(targetLevel=mdict.optimal100);
									}else if(targetLevel>10) {
										invoker.mWebView.getSettings().setTextZoom(targetLevel=10);
									}else
										targetLevel=-2;
									break;
							}
							if(targetLevel>0) {
								if(invoker.bUseInternalFS) {
									invoker.WriteConfigInt(invoker.CONFIGPOS[1], targetLevel);
									showT((invoker.internalScaleLevel=targetLevel)+"%",0);
								}else {
									notifyGLobalFSChanged(targetLevel);
									showT("(全局) "+(targetLevel)+"%",0);//"(全局) "
								}
							}
							else if(targetLevel==-2)
								showT("min scale level",0);
							else
								showT("max level reached",0);
							return true;
						}
					return false;
				});

				d.getListView().addFooterView(bottomView);
			}
			d.setTitle(invoker._Dictionary_fName+" - "+(bFromPeruseView?PeruseView.currentDisplaying:invoker.currentDisplaying));

			d.show();

			if(getCurrentFocus()!=null)
				getCurrentFocus().clearFocus();

			d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

			if(lastInDark=GlobalOptions.isDark) {
				d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_d);
			}else {
				d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
			}



			//d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);

			if(!needReCreateUcc)
				try {//dynamically change items!
					ArrayAdapter<CharSequence> DialogArryAda = ((ArrayAdapter<CharSequence>)((HeaderViewListAdapter)d.getListView().getAdapter()).getWrappedAdapter());
					Field field = ArrayAdapter.class.getDeclaredField("mObjects");
					field.setAccessible(true);
					field.set(DialogArryAda, Arrays.asList(bFromWebView?itemsB:itemsA));
					DialogArryAda.notifyDataSetChanged();
				} catch (Exception e) {
					needReCreateUcc=true;
					e.printStackTrace();
					Log.d("123123","123123");
				}
			firstCreateUcc=false;
			//}
		}
	}
	boolean firstCreateUcc=true;
	boolean needReCreateUcc=false;
	public UniCoverClicker ucc;
	public int CachedBBSize=-1;


	ArrayAdaptermy mBookMarkAdapter;
	public ListAdapter getBookMarkAdapter(boolean darkMode, mdict invoker, MdxDBHelper con) {
		if(mBookMarkAdapter==null)
			mBookMarkAdapter=new ArrayAdaptermy(getApplicationContext(),R.layout.drawer_list_item,R.id.text1,invoker,con,0);
		else
			mBookMarkAdapter.refresh(invoker,con);
		mBookMarkAdapter.darkMode=darkMode;
		return mBookMarkAdapter;
	}

	public void showAppTweaker() {
		String[] DictOpt = getResources().getStringArray(R.array.app_spec);
		final String[] Coef = DictOpt[0].split("_");
		final View dv = inflater.inflate(R.layout.dialog_about,null);
		final SpannableStringBuilder ssb = new SpannableStringBuilder();
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = dv.findViewById(R.id.title);
		title.setText("程序设定");//"词典设定"
		title.setTextColor(AppBlack);
		final BooleanSingleton bFlag = new BooleanSingleton(false);

		if(opt.isLarge) tv.setTextSize(tv.getTextSize());
		tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		ssb.append("[").append(DictOpt[1]).append(opt.isFullScreen()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//全屏
			@Override
			public void onClick(@NonNull View widget) {
				boolean val = opt.setFullScreen(!opt.isFullScreen());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
				if(MainActivityUIBase.this.getClass()== PDICMainActivity.class) {
					((PDICMainActivity) MainActivityUIBase.this).drawerFragment.sw1.setChecked(val);
				}
			}},0,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		int start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[2]).append(!opt.isContentBow()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//页面覆盖
			@Override
			public void onClick(@NonNull View widget) {
				boolean val = !opt.setContentBow(!opt.isContentBow());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;

				if(MainActivityUIBase.this.getClass()== PDICMainActivity.class) {
					((PDICMainActivity) MainActivityUIBase.this).drawerFragment.sw2.setChecked(val);
				}
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[3]).append(opt.getInDarkMode()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//黑暗模式
			@Override
			public void onClick(@NonNull View widget) {
				boolean val = opt.setInDarkMode(!opt.getInDarkMode());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				switch_dark_mode(val);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[4]).append(opt.get_use_volumeBtn()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//音量键
			@Override
			public void onClick(@NonNull View widget) {
				boolean val = opt.set_use_volumeBtn(!opt.get_use_volumeBtn());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;

				if(MainActivityUIBase.this.getClass()== PDICMainActivity.class) {
					((PDICMainActivity) MainActivityUIBase.this).drawerFragment.sw5.setChecked(val);
				}
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		ssb.append("\r\n").append("\r\n");


		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[5]).append("]");
		ssb.setSpan(new ClickableSpan() {//隐藏标题
			@Override
			public void onClick(@NonNull View widget) {
				hideDictToolbar=!hideDictToolbar;
				for(mdict mdTmp:md) {
					mdTmp.PlayWithToolbar(hideDictToolbar, MainActivityUIBase.this);
				}
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[6]).append(opt.getInheritePageScale()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//沿用缩放
			@Override
			public void onClick(@NonNull View widget) {
				boolean val = opt.setInheritePageScale(!opt.getInheritePageScale());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[7]).append(Coef[opt.getNavigationBtnType()]).append("]");
		ssb.setSpan(new ClickableSpan() {//按钮放到顶部
			@Override
			public void onClick(@NonNull View widget) {
				int val=opt.getNavigationBtnType();
				val+=1;
				val%=3;
				opt.setNavigationBtnType(val);
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,Coef[val]);
				tv.setText(ssb);
				locateNaviIcon(widget13,widget14);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");
		ssb.append("\r\n").append("\r\n");


		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[8]).append(opt.getHideScroll1()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//隐藏1
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHideScroll1(!opt.getHideScroll1());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
				if(val)
					mBar.setVisibility(View.GONE);
				else
					mBar.setVisibility(View.VISIBLE);
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[9]).append(opt.getHideScroll2()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//2
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHideScroll2(!opt.getHideScroll2());
				opt.setNavigationBtnType(opt.getNavigationBtnType());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
				if(val)
					mBar.setVisibility(View.GONE);
				else
					mBar.setVisibility(View.VISIBLE);
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[10]).append(opt.getHideScroll3()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//3
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHideScroll3(!opt.getHideScroll3());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
				if(PeruseView!=null&&PeruseView.getView()!=null) {
					if(val)
						PeruseView.mBar.setVisibility(View.GONE);
					else
						PeruseView.mBar.setVisibility(View.VISIBLE);
				}
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[11]).append(opt.getPageTurn1()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//禁用翻页1
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setPageTurn1(!opt.getPageTurn1());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[12]).append(opt.getPageTurn2()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//2
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setPageTurn2(!opt.getPageTurn2());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[13]).append(opt.getHideScroll3()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//3
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setPageTurn2(!opt.getPageTurn2());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[14]).append(opt.getHistoryStrategy0()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//历史纪录策略0
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHistoryStrategy0(!opt.getHistoryStrategy0());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");


		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[15]).append(opt.getHistoryStrategy1()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//1
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHistoryStrategy1(!opt.getHistoryStrategy1());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[16]).append(opt.getHistoryStrategy2()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//2
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHistoryStrategy2(!opt.getHistoryStrategy2());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[17]).append(opt.getHistoryStrategy3()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//3
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHistoryStrategy3(!opt.getHistoryStrategy3());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[18]).append(opt.getHistoryStrategy4()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//4
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHistoryStrategy4(!opt.getHistoryStrategy4());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[19]).append("]");
		ssb.setSpan(new ClickableSpan() {//5
			@Override
			public void onClick(@NonNull View widget) {

			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[20]).append(opt.getHistoryStrategy6()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//6
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHistoryStrategy6(!opt.getHistoryStrategy6());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[21]).append(opt.getHistoryStrategy7()?Coef[0]:Coef[1]).append("]");
		ssb.setSpan(new ClickableSpan() {//7
			@Override
			public void onClick(@NonNull View widget) {
				boolean val=opt.setHistoryStrategy7(!opt.getHistoryStrategy7());
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val?Coef[0]:Coef[1]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		start1=ssb.toString().length();
		ssb.append("[").append(DictOpt[22]).append(opt.getHistoryStrategy8()<2?Coef[opt.getHistoryStrategy8()]:Coef[3]).append("]");
		ssb.setSpan(new ClickableSpan() {//8
			@Override
			public void onClick(@NonNull View widget) {
				int val=opt.getHistoryStrategy8();
				val+=1;
				val%=3;
				opt.setHistoryStrategy8(val);
				String now = ssb.toString();
				int fixedRange = now.indexOf(":",ssb.getSpanStart(this));
				ssb.delete(fixedRange+1, now.indexOf("]",fixedRange));
				ssb.insert(fixedRange+1,val<2?Coef[opt.getHistoryStrategy8()]:Coef[3]);
				tv.setText(ssb);
				bFlag.first=true;
			}},start1,ssb.toString().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.append("\r\n").append("\r\n");

		tv.setText(ssb);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);
		builder2.setView(dv);
		final AlertDialog d = builder2.create();
		d.setCanceledOnTouchOutside(true);
		//d.setCanceledOnTouchOutside(false);

		d.setOnDismissListener(dialog -> {
			if(bFlag.first)
				opt.putFlags();
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

	protected void switch_dark_mode(boolean val) {

	}

	public UniCoverClicker getUcc() {
		if(ucc==null) ucc = new UniCoverClicker();
		return ucc;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem arg0) {
		return false;
	}

	int lastCheckedPos = -1;
	public void showChooseSetDialog() {//切换分组
		if(d!=null) {
			d.dismiss();
			d=null;
		}
		File def = new File(opt.pathToMain()+"CONFIG/AllModuleSets.txt");      //!!!原配
		final ArrayList<String> scanInList = new ArrayList<>();
		final HashSet<String> con = new HashSet<>();
		lastCheckedPos = -1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(def));
			String line = in.readLine();
			while(line!=null){
				if(!con.contains(line))
					if(new File(opt.pathToMain()+"CONFIG/"+line+".set").exists()) {
						scanInList.add(line);
						if(line.equals(opt.lastMdPlanName))
							lastCheckedPos=scanInList.size()-1;
					}
				con.add(line);
				line = in.readLine();
			}
			in.close();
		} catch (Exception ignored) { }

		new File(opt.pathToMain()+"CONFIG").list((dir,name) -> {
			if(name.endsWith(".set")) {
				name = name.substring(0,name.length()-4);
				if(!con.contains(name)) {
					scanInList.add(name);
					con.add(name);
					if(name.equals(opt.lastMdPlanName))
						lastCheckedPos=scanInList.size()-1;
				}
			}
			return false;
		});

		AlertDialog.Builder builder2 = new AlertDialog.Builder(this,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line);//
		builder2.setTitle(R.string.loadconfig).setSingleChoiceItems(new String[] {}, 0, (dialog, pos) -> {
							try {
								HashMap<String,mdict> mdict_cache = new HashMap<>();
								for(mdict mdTmp:md) {
									mdict_cache.put(mdTmp._Dictionary_fName, mdTmp);
								}
								md.clear();
								File newf = new File(opt.pathToMain()+"CONFIG/"+scanInList.get(pos)+".set");
								BufferedReader in = new BufferedReader(new FileReader(newf));
								String line = in.readLine();
								String lastMdName = opt.getLastMdFn();
								int lastMdPos = 0;
								while(line!=null){
									if(!line.trim().equals("")){
										if(!line.startsWith("/"))
											line=opt.lastMdlibPath+"/"+line;
										String fnId = new File(line).getAbsolutePath();
										try {
											if(mdict_cache.containsKey(fnId))
												md.add(mdict_cache.get(fnId));
											else
												md.add(new_mdict(line, MainActivityUIBase.this));

											if(md.get(md.size()-1)._Dictionary_fName.equals(lastMdName))
												lastMdPos=md.size()-1;
										} catch (Exception e) {
											e.printStackTrace();
											show(R.string.err,new File(line).getName(),line,e.getLocalizedMessage());
										}
									}
									line = in.readLine();
								}
								in.close();
								mdict_cache.clear();
								opt.putLastPlanName(scanInList.get(pos));
								if(md.size()>0) {
									currentDictionary = md.get(adapter_idx=lastMdPos);
								}else{
									currentDictionary = null;
								}
								dialog.dismiss();
								invalidAllLists();
								show(R.string.loadsucc);
								File def1 = new File(getExternalFilesDir(null),"default.txt");
								FileChannel inputChannel = null;
								FileChannel outputChannel = null;
								try {
									inputChannel = new FileInputStream(newf).getChannel();
									def1.delete();
									outputChannel = new FileOutputStream(def1).getChannel();
									outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
									inputChannel.close();
									outputChannel.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}catch(Exception e) {
								e.printStackTrace();
							}

						});

		AlertDialog dTmp = builder2.create();
		dTmp.getWindow().setBackgroundDrawableResource(GlobalOptions.isDark?R.drawable.popup_shadow_d:R.drawable.popup_shadow_l);

		dTmp.show();
		//d=dTmp;
		dTmp.setOnDismissListener(this);

		dTmp.getListView().setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				R.layout.singlechoice, android.R.id.text1, scanInList) {
			@NonNull @Override
			public View getView(int position, View convertView, @NonNull ViewGroup parent) {
				View ret =  super.getView(position, convertView, parent);
				CheckedTextViewmy tv;
				if(ret.getTag()==null)
					ret.setTag(tv = ret.findViewById(android.R.id.text1));
				else
					tv = (CheckedTextViewmy)ret.getTag();
				tv.setTextColor(AppBlack);
				tv.setText(scanInList.get(position));
				return ret;
			}
		});

		dTmp.getListView().addOnLayoutChangeListener(MainActivityUIBase.mListsizeConfiner.setMaxHeight((int) (root.getHeight()-root.getPaddingTop()-2.8*getResources().getDimension(R.dimen._50_))));

		if(lastCheckedPos!=-1) {
			dTmp.getListView().setSelection(lastCheckedPos);
			dTmp.getListView().setItemChecked(lastCheckedPos, true);
		}
	}

	public void invalidAllLists() {

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		d=null;
	}

	public WebChromeClient myWebCClient = new WebChromeClient() {
		///显示自定义视图，无此方法视频不能播放
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			super.onShowCustomView(view, callback);
		}

		@Override
		public void onProgressChanged(final WebView v, int newProgress) {
			//CMN.Log("ProgressChanged");
			super.onProgressChanged(v, newProgress);
		}
	};
	public static boolean layoutScrollDisabled;
	protected WebViewmy NaugtyWeb;

	public WebViewClient myWebClient = new WebViewClient() {
		public void onPageFinished(WebView view, String url) {
			//CMN.Log("onPageFinished");
			//if(true) return;
			Integer selfAtIdx = IU.parseInt(view.getTag());
			if(selfAtIdx==null || selfAtIdx>=md.size() || selfAtIdx<0) return;
			final mdict invoker = md.get(selfAtIdx);
			final WebViewmy mWebView = (WebViewmy) view;
			boolean fromPeruseView=view.getTag(R.id.position)!=null;

			if(fromPeruseView?PeruseView.webScale!=mdict.def_zoom:invoker.webScale!=mdict.def_zoom) {
				//if(Build.VERSION.SDK_INT>=21)
				//	view.zoomBy(0.02f);//reset zoom!
				//if(fromPeruseView)PeruseView.webScale=mdict.def_zoom;else invoker.webScale=mdict.def_zoom;
			}
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

			lastClickTime=System.currentTimeMillis();

			String PagedoneIncantation = null;

			if(CMN.editAll || invoker.bContentEditable) {
				if(PagedoneIncantation==null)
					PagedoneIncantation="";
				PagedoneIncantation+="document.body.contentEditable=true;";
				ViewGroup VG = (ViewGroup) mWebView.getParent().getParent();
				if(VG!=null && VG.getId()==R.id.webholder)
					VG.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			}
			if(PagedoneIncantation!=null)
				mWebView.evaluateJavascript(PagedoneIncantation, null);

			//document.body.contentEditable=true;

			int lalaX,lalaY;
			mWebView.setTag(R.id.toolbar_action1,lalaX=fromPeruseView?PeruseView.expectedPosX:invoker.expectedPosX);
			mWebView.setTag(R.id.toolbar_action2,lalaY=fromPeruseView?PeruseView.expectedPos:invoker.expectedPos);
			//layoutScrollDisabled=true;
			//CMN.Log("initial_push: ",lalaX, lalaY);
			mWebView.scrollTo(lalaX, lalaY);


			NaugtyWeb=mWebView;
			if(hdl!=null)
				hdl.sendEmptyMessage(778899);

			layoutScrollDisabled=true;

			mWebView.isloading=false;

			//super.onPageFinished(view, url);
			if(!invoker.isJumping) {
				//v.clearHistory();
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		public void  onScaleChanged(WebView view, float oldScale,float newScale)
		{
			//CMN.Log(oldScale, "-", newScale, " newScale");
			Integer selfAtIdx = IU.parseInt(view.getTag());
			if(selfAtIdx==null || selfAtIdx>=md.size() || selfAtIdx<0) return;
			mdict invoker = md.get(selfAtIdx);
			boolean fromPeruseView=view.getTag(R.id.position)!=null;

			super.onScaleChanged(view, oldScale,newScale);
			((WebViewmy)view).webScale=newScale;
			if(fromPeruseView) {
				if((PeruseView.webScale = newScale)>mdict.def_zoom)
					PeruseView.PageSlider.TurnPageEnabled=false;
				else
					PeruseView.PageSlider.TurnPageEnabled=PeruseView.TurnPageEnabled;
			}else {
				invoker.webScale = newScale;
				if(PageSlider!=null) {
					if(invoker.webScale>mdict.def_zoom)
						PageSlider.TurnPageEnabled=false;
					else
						PageSlider.TurnPageEnabled=TurnPageEnabled;
				}
			}
		}
		final String entryTag = "entry://";
		final String soundTag = "sound://";

		@Override
		public void onLoadResource(WebView view, String url) {

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Integer selfAtIdx = IU.parseInt(view.getTag());
			if(selfAtIdx==null || selfAtIdx>=md.size() || selfAtIdx<0) return false;
			final mdict invoker = md.get(selfAtIdx);
			//CMN.Log("chromium shouldOverrideUrlLoading_",url);
			boolean fromPeruseView=view.getTag(R.id.position)!=null;
			if(url.startsWith("pdf://")) {
				int end = url.lastIndexOf("#");
				int pageId=-1;
				if(end!=-1) {
					pageId = IU.parseInt(url.substring(end + 1, url.length()));
				}
				Intent it=new Intent(Intent.ACTION_VIEW);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				File f = new File("/sdcard/PLOD/PDFs/李白全集.pdf");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					//StrictMode.setVmPolicy(StrictMode.getVmPolicy());
					StrictMode.setVmPolicy(new VmPolicy.Builder().build());
				}
				if(pageId!=-1)
					it.putExtra("page", pageId);
				it.setDataAndType(Uri.fromFile(f), "application/pdf");
				startActivity(it);
				return true;
			}
			if(url.startsWith("http://") || url.startsWith("https://") ) {
				//CMN.Log("shouldOverrideUrlLoading_http",url);

				if(opt.bShouldUseExternalBrowserApp) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);

					return true;
				}else {
					if(fromPeruseView) {
						if(PeruseView.HistoryVagranter>=0) PeruseView.History.get(PeruseView.HistoryVagranter).value=PeruseView.mWebView.getScrollY();
						PeruseView.History.add(++PeruseView.HistoryVagranter,new myCpr<>(url,PeruseView.expectedPos=0));
						for(int i=PeruseView.History.size()-1;i>=PeruseView.HistoryVagranter+1;i--)
							PeruseView.History.remove(i);

						PeruseView.recess.setVisibility(View.VISIBLE);
						PeruseView.forward.setVisibility(View.VISIBLE);
					}else {
						if(invoker.HistoryVagranter>=0) invoker.History.get(invoker.HistoryVagranter).value=invoker.mWebView.getScrollY();
						invoker.History.add(++invoker.HistoryVagranter,new myCpr<>(url,invoker.expectedPos=0));
						for(int i=invoker.History.size()-1;i>=invoker.HistoryVagranter+1;i--)
							invoker.History.remove(i);

						invoker.recess.setVisibility(View.VISIBLE);
						invoker.forward.setVisibility(View.VISIBLE);
					}
					return false;
				}
			}
			else if(url.startsWith(soundTag)) {
				try {
					if(invoker.mdd!=null) {
						//CMN.Log("shouldOverrideUrlLoading_sound",url);
						view.evaluateJavascript("var audioTag = document.getElementById(\"myAudio\");if(audioTag){audioTag.pause();}else {audioTag = document.createElement(\"AUDIO\");audioTag.id=\"myAudio\";document.body.appendChild(audioTag);} audioTag.setAttribute(\"src\", \""+URLDecoder.decode(url.substring(soundTag.length()),"UTF-8")+"\");audioTag.play();", null);
					}else
						view.evaluateJavascript("var hrefs = document.getElementsByTagName('a'); for(var i=0;i<hrefs.length;i++){ if(hrefs[i].attributes['href']){ if(hrefs[i].attributes['href'].value=='"+URLDecoder.decode(url,"UTF-8")+"'){ hrefs[i].removeAttribute('href'); hrefs[i].click(); break; } } }", null);
					return true;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return true;
			}
			else if (url.startsWith(entryTag)) {
				if (url.startsWith("entry://#")) {//页内跳转
					Log.e("chromium inter_ entry3",url);
					if(!isCombinedSearching) {
						//view.evaluateJavascript("window.location.href = \""+""+"\";", null);   window.location.hash
						invoker.expectedPos=-100;
						view.evaluateJavascript("location.replace(\""+url.substring(entryTag.length())+"\");", null);
					}else {//TODO 精确之
						view.evaluateJavascript("document.getElementsByName(\""+url.substring(entryTag.length()+1)+"\")[0].getBoundingClientRect().top;"
								, v -> {
									//CMN.show(v);
									((ScrollView)webholder.getParent()).smoothScrollTo(0, invoker.rl.getTop()-toolbar.getHeight()+(int)(float)(Float.valueOf(v)*getResources().getDisplayMetrics().density));//
								});
					}
					return true;
				}else if (url.startsWith("entry://@")) {//书签跳转
					Log.e("chromium inter_ entry3",url);
					try {
						//CMN.a.ActivedAdapter.onItemClick(lookUp(URLDecoder.decode(url,"UTF-8")));
						//Log.e("chromium inter entry2",URLDecoder.decode(url,"UTF-8"));
						//if(!opt.isCombinedSearching)
						//	a.jumpHistory.add(new Pair(currentDisplaying, mWebView.getScrollY()));
						//a.jump(url, mdict.this);
						Integer pos = IU.parseInt(url.substring(entryTag.length()+1));
						if(pos==null) return true;
						if(fromPeruseView) {
							PeruseView.recess.setVisibility(View.VISIBLE);
							PeruseView.forward.setVisibility(View.VISIBLE);
							PeruseView.isJumping=true;
							invoker.htmlBuilder.setLength(invoker.htmlHeader.length());
							if(PeruseView.HistoryVagranter>=0) PeruseView.History.get(PeruseView.HistoryVagranter).value=PeruseView.mWebView.getScrollY();
							PeruseView.setCurrentDis(invoker, pos);
						}else {
							invoker.recess.setVisibility(View.VISIBLE);
							invoker.forward.setVisibility(View.VISIBLE);
							invoker.isJumping=true;
							invoker.htmlBuilder.setLength(invoker.htmlHeader.length());
							if(invoker.HistoryVagranter>=0) invoker.History.get(invoker.HistoryVagranter).value=invoker.mWebView.getScrollY();
							invoker.setCurrentDis(pos);
						}

						view.loadDataWithBaseURL(invoker.baseUrl,
								invoker.htmlBuilder.append(mdict.htmlTitleEndTag).append((AppWhite==Color.BLACK)? MainActivityUIBase.DarkModeIncantation_l:"").append(mdict.htmlHeader2)
										.append(invoker.getRecordsAt(pos))
										.append(invoker.js)
										.append(invoker.mdd!=null?"<div class='MddExist'/>":"")
										.append(invoker.htmlTailer).toString()
								,null, "UTF-8", null);
						jump(pos,invoker);
						return true;
					} catch (Exception ignored) {}
				}
				Log.e("chromium inter_ entry2",url);
				url = url.substring(entryTag.length());
				try {
					//CMN.a.ActivedAdapter.onItemClick(lookUp(URLDecoder.decode(url,"UTF-8")));
					//Log.e("chromium inter entry2",URLDecoder.decode(url,"UTF-8"));
					//if(!opt.isCombinedSearching)
					//	a.jumpHistory.add(new Pair(currentDisplaying, mWebView.getScrollY()));
					//a.jump(url, mdict.this);
					if(url.indexOf("#")!=-1) {
						String tailDial = url.substring(url.indexOf("#"));
						url = url.substring(0, url.indexOf("#"));
					}
					int idx = invoker.lookUp(invoker.processMyText(URLDecoder.decode(url,"UTF-8")),true);
					//Log.e("entry_jump_",URLDecoder.decode(url,"UTF-8")+" "+(idx)+processText(URLDecoder.decode(url,"UTF-8")));
					if(idx!=-1) {
						if(fromPeruseView) {
							PeruseView.recess.setVisibility(View.VISIBLE);
							PeruseView.forward.setVisibility(View.VISIBLE);
							PeruseView.isJumping=true;
							if(PeruseView.HistoryVagranter>=0) PeruseView.History.get(PeruseView.HistoryVagranter).value=PeruseView.mWebView.getScrollY();
							PeruseView.setCurrentDis(invoker,idx);
						}else {
							invoker.recess.setVisibility(View.VISIBLE);
							invoker.forward.setVisibility(View.VISIBLE);
							invoker.isJumping=true;
							if(invoker.HistoryVagranter>=0) invoker.History.get(invoker.HistoryVagranter).value=invoker.mWebView.getScrollY();
							ScrollerRecord PageState = invoker.HistoryOOP.get(invoker.currentPos);
							if(PageState==null)
								invoker.HistoryOOP.put(invoker.currentPos, PageState=new ScrollerRecord());
							PageState.x=invoker.mWebView.getScrollX();
							PageState.y=invoker.mWebView.getScrollY();
							PageState.scale=invoker.mWebView.webScale;
							invoker.setCurrentDis(idx);
						}

						invoker.htmlBuilder.setLength(invoker.htmlHeader.length());
						view.loadDataWithBaseURL(invoker.baseUrl,
								invoker.htmlBuilder.append(mdict.htmlTitleEndTag).append((AppWhite==Color.BLACK)? MainActivityUIBase.DarkModeIncantation_l:"").append(mdict.htmlHeader2)
										.append(invoker.getRecordsAt(idx))
										.append(invoker.js)
										.append(invoker.mdd!=null?"<div class='MddExist'/>":"")
										.append(invoker.htmlTailer).toString()
								,null, "UTF-8", null);

						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				show(R.string.jumpfail);
				return true;
			}
			return super.shouldOverrideUrlLoading(view, url);
		}



		@Override
		public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {
    	/*if(url.startsWith(entryTag)){
    		Log.e("chromium inter_entry",url);
    		url = url.substring(entryTag.length());
    		try {
				Log.e("chromium inter_entry",URLDecoder.decode(url,"UTF-8"));
				//CMN.a.ActivedAdapter.onItemClick(lookUp(URLDecoder.decode(url,"UTF-8")));
				return null;//new WebResourceResponse("text/html","UTF-8",new ByteArrayInputStream(getRecordAt(lookUp(URLDecoder.decode(url,"UTF-8"))).getBytes()));
    		} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	*/
			Integer selfAtIdx = IU.parseInt(view.getTag());
			if(selfAtIdx==null || selfAtIdx>=md.size() || selfAtIdx<0) return null;
			mdict invoker = md.get(selfAtIdx);
			//CMN.Log("chromium shouldInterceptRequest__",url);
			//boolean fromPeruseView=view.getTag(R.id.position)!=null;
			//WebViewmy mWebView = (WebViewmy) view;
			if(url.startsWith("http") && url.endsWith("mp3")) {
				if(true)
					return super.shouldInterceptRequest(view, url);
				try {
					HttpURLConnection conn;
					URL webUrl = new URL(url);
					conn = (HttpURLConnection)webUrl.openConnection();
					conn.setConnectTimeout(2000);
					conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
					InputStream inputStream = conn.getInputStream();

					return new WebResourceResponse("audio/mpeg","UTF-8",inputStream);
				} catch (IOException e) {
					//CMN.Log("shouldInterceptRequest__ffffff",url);
					e.printStackTrace();
				}
			}
			try {
				if(url.toLowerCase().contains(".js")) {
					Log.e("candi",url);
					File candi = new File(invoker.f().getParentFile(),new File(url).getName());
					if(candi.exists())
						return new WebResourceResponse("text/x-javascript","UTF-8",new FileInputStream(candi));

				}
				if(url.toLowerCase().endsWith(".css")) {
					Log.e("candi",url);
					File candi = new File(invoker.f().getParentFile(),new File(url).getName());
					if(candi.exists())
						return new WebResourceResponse("text/css","UTF-8",new FileInputStream(candi));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}


			if(invoker.mdd==null)
				return null;
			//Looper.prepare();
			//showToast(url);
			//Looper.loop();StringEscapeUtils.escapeHtml(
			if(url.startsWith(soundTag)) {
				url = url.substring(soundTag.length());
			}
			try {
				url = URLDecoder.decode(url,"UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			Log.e("chrochro inter_0",url);
			String SepWindows = "\\";
			String key=url;

			int start = key.indexOf(invoker.FileTag);
			if(start==-1){
				if(key.startsWith("./"))
					key = key.substring(1).replace("/", SepWindows);
			}else{
				key = key.substring(start+invoker.FileTag.length()).replace("/", SepWindows);
			}


			if(!key.startsWith(SepWindows)){
				key=SepWindows+key;
			}
			//CMN.Log("chrochro_inter_key is",key);

			try {
				int idx = invoker.mdd.lookUp(key);
				if(idx==-1) {
					//CMN.Log("chrochro inter_ key is not find: ",key);
					//return super.shouldInterceptRequest(view, url);
				}
				byte[] restmp=invoker.mdd.getRecordAt(idx);

				if(restmp==null) {
					if(url.startsWith("http://")) {
						URL uurrll = new URL(url);
						HttpURLConnection conn = (HttpURLConnection) uurrll.openConnection();
						conn.setConnectTimeout(3 * 1000);
						conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");


						conn.setRequestMethod("GET");
						InputStream inStream = conn.getInputStream();
						//conn.setRequestProperty("lfwywxqyh_token",toekn);
						byte[] buffer = new byte[1024];
						int len = 0;
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						while((len = inStream.read(buffer)) != -1) {
							bos.write(buffer, 0, len);
						}
						bos.close();
						restmp =  bos.toByteArray();
						//return new WebResourceResponse("","UTF-8",inStream);
					}else
						return super.shouldInterceptRequest(view, url);
				}

				if(restmp==null) return super.shouldInterceptRequest(view, url);

				if(url.contains(".mp4"))
					return new WebResourceResponse("video/mp4","UTF-8",new ByteArrayInputStream(restmp));

				if(url.contains(".mp3"))
					return new WebResourceResponse("audio/mpeg","UTF-8",new ByteArrayInputStream(restmp));

				if(url.contains(".png") || url.contains(".jpg") || url.contains(".gif") || url.contains(".jpeg") ||url.contains(".tif")) {
					//CMN.Log("chrochrochrochrochrochro inter_ key is",key);
					//BU.printFile(restmp,0,restmp.length,"/sdcard/0tmp/"+key.replace("\\", "_"));
					return new WebResourceResponse("image/jpg","UTF-8",new ByteArrayInputStream(restmp));
				}

				if(url.contains(".js"))
					return new WebResourceResponse("application/x-javascript","UTF-8",new ByteArrayInputStream(restmp));


				return new WebResourceResponse("","UTF-8",new ByteArrayInputStream(restmp));


			} catch (IOException e) {
				e.printStackTrace();
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

	public void locateNaviIcon(View widget13,View widget14){
		if(opt.getNavigationBtnType()==0 || (opt.getNavigationBtnType()==2 && !opt.getBottombarOnBottom())) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) widget13.getLayoutParams();
			//if((lp.gravity&Gravity.TOP) != 0 ) return;
			lp.gravity=Gravity.TOP|Gravity.RIGHT;
			FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams) widget14.getLayoutParams();
			lp1.topMargin=lp.bottomMargin;
			lp.topMargin=lp1.bottomMargin;
			lp1.gravity=Gravity.TOP|Gravity.RIGHT;
			widget13.setLayoutParams(lp);
			widget14.setLayoutParams(lp1);
		}else {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) widget13.getLayoutParams();
			//if((lp.gravity&Gravity.BOTTOM) != 0 ) return;
			lp.gravity=Gravity.BOTTOM|Gravity.RIGHT;
			widget13.setLayoutParams(lp);
			lp = (FrameLayout.LayoutParams) widget14.getLayoutParams();
			lp.gravity=Gravity.BOTTOM|Gravity.RIGHT;
			widget14.setLayoutParams(lp);
		}
	}

	public OnLayoutChangeListener OLCL;
	final static String DeDarkModeIncantation = "ssc=document.getElementsByTagName('style');for(var i=0;i<ssc.length;i++){if(ssc[i].innerHTML){if(new RegExp('^'+'html {-webkit-filter').test(ssc[i].innerHTML)){document.getElementsByTagName('head')[0].removeChild(ssc[i]);break;}}}";
	public final static String DarkModeIncantation_l = "<style>html {-webkit-filter: invert(100%);"
			+"-moz-filter:invert(100%);"
			+"-o-filter:invert(100%);"
			+"-ms-filter:invert(100%);}"
			+ "</style>";
	final static String DarkModeIncantation ="var css = 'html {-webkit-filter: invert(100%);' +"
			+"    '-moz-filter: invert(100%);' + "
			+"    '-o-filter: invert(100%);' + "
			+"    '-ms-filter: invert(100%); }',"

			+"head = document.getElementsByTagName('head')[0],"
			+"style = document.createElement('style');"

			//+"if (!window.counter) { window.counter = 1;} else  { window.counter ++;"
			//+"if (window.counter % 2 == 0) { var css ='html {-webkit-filter: invert(0%); -moz-filter:    invert(0%); -o-filter: invert(0%); -ms-filter: invert(0%); }'}"
			//+"};"

			+"style.type = 'text/css';"
			+"if (style.styleSheet){"
			+"style.styleSheet.cssText = css;"
			+"} else {"
			+"style.appendChild(document.createTextNode(css));"
			+"}"
			//injecting the css to the head
			+"head.appendChild(style);";

}
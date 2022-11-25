package com.knziha.plod.plaindict;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;

import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.Bag;
import com.knziha.plod.ebook.Utils.BU;
import com.knziha.plod.widgets.SimpleDialog;
import com.knziha.plod.widgets.SimpleTextNotifier;
import com.knziha.plod.widgets.ViewUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import static com.knziha.plod.dictionarymodels.BookPresenter.indexOf;

import io.noties.markwon.Markwon;

public class Toastable_Activity extends AppCompatActivity {
	public boolean systemIntialized;
	protected long initializeTm;
	protected final static String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

	private boolean skipCheckLog;
	private static boolean MarginChecked;
	private static int DockerMarginL;
	private static int DockerMarginR;
	private static int DockerMarginT;
	private static int DockerMarginB;
	public ViewGroup root;
	//public dictionary_App_Options opt;
    //public List<mdict> md = new ArrayList<mdict>();//Collections.synchronizedList(new ArrayList<mdict>());
	protected boolean shunt;
	public PDICMainAppOptions opt;
	public DisplayMetrics dm;
	//public LayoutInflater inflater;
	public InputMethodManager imm;
	protected int trialCount=-1;
	
	public int mDialogType = WindowManager.LayoutParams.TYPE_APPLICATION;
	
	public long lastClickTime=0;

	public long[] flags = new long[8];
	protected long layoutFlagStamp;
	public int MainAppBackground = 0xFF03A9F4;
	public int MainBackground = 0xFF03A9F4;
	public int MainPageBackground;
	public int AppBlack;
	public int AppWhite;
    public float ColorMultiplier_Wiget=0.9f;
    public float ColorMultiplier_Web=1;
    public float ColorMultiplier_Web2=1;

	public ViewGroup contentview;
	protected ViewGroup dialog_;
    public Toolbar toolbar;
    public ViewGroup appbar;
    protected EditText etSearch;
	protected int etSearchPadRight;
    protected ImageView ivDeleteText;
    public ImageView ivBack;
	protected String lastPastedText;

	public Dialog d;
	public View dv;
	public Configuration mConfiguration;
	boolean isDarkStamp;

	SimpleTextNotifier topsnack;
	private Animator.AnimatorListener topsnackListener;
	static final int FLASH_DURATION_MS = 800;
	static final int SHORT_DURATION_MS = 1500;
	static final int LONG_DURATION_MS = 2355;
	int NextSnackLength;
	protected ViewGroup DefaultTSView;
	long exitTime = 0;
	public Resources mResource;
	protected int btnMaxWidth;
	
	LexicalDBHelper historyCon;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(shunt)
			return;
		if (opt==null) opt = new PDICMainAppOptions(this);
	   opt.dm = dm = new DisplayMetrics();
	   mResource = getResources();
	   mConfiguration = new Configuration(mResource.getConfiguration());
	   Display display = getWindowManager().getDefaultDisplay();
	   opt.fillFlags(flags);
	   display.getRealMetrics(dm);
	   if (GlobalOptions.realWidth <= 0) {
		   readSizeConfigs();
		   GlobalOptions.realWidth = Math.min(dm.widthPixels, dm.heightPixels);
		   GlobalOptions.realHeight = Math.max(dm.widthPixels, dm.heightPixels);
		   GlobalOptions.density = dm.density;
		   //GlobalOptions.densityDpi = dm.densityDpi;
		   opt.getInDarkMode();
		   if(Build.VERSION.SDK_INT>=29 && !PDICMainAppOptions.systemDarked()){
			   PDICMainAppOptions.systemDarked(true);
			   GlobalOptions.isSystemDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
		   }
	   }
	   VersionUtils.checkVersion(opt);
	   if (opt.darkSystem() && Build.VERSION.SDK_INT>=29) {
			GlobalOptions.isDark = GlobalOptions.isSystemDark;
	   }
	   if (PDICMainAppOptions.checkVersionBefore_5_0())
	   { // 升级数据库对话框
		   //DBUpgradeHelper.showUpgradeDlg(null, this, true);
//			   opt.setUseDatabaseV2(true);
//			   opt.setUseBackKeyGoWebViewBack(true);
//			   opt.setAnimateContents(Build.VERSION.SDK_INT>=21);
//			   PDICMainAppOptions.uncheckVersionBefore_5_0(false);
//			   PDICMainAppOptions.uncheckVersionBefore_4_0(true); // revert & recycle the flag bits
//			   PDICMainAppOptions.uncheckVersionBefore_4_9(true); // revert & recycle the flag bits
	   }
	   if (PDICMainAppOptions.checkVersionBefore_5_2())
	   {
		   //CMN.Log("强制数据库2.0");
//			   opt.setUseDatabaseV2(true);
//			   PDICMainAppOptions.uncheckVersionBefore_5_2(false);
	   }
	   if (PDICMainAppOptions.checkVersionBefore_5_3())
	   {
//			   opt.setPinVSDialog(true);
//			   opt.setRememberVSPanelGo(false);
//			   PDICMainAppOptions.uncheckVersionBefore_5_3(false);
	   }
	   if(PDICMainAppOptions.checkVersionBefore_5_4()) {
//			   PDICMainAppOptions.bCheckVersionBefore_5_4=true;
//			   PDICMainAppOptions.setHideFloatFromRecent(false);
//			   PDICMainAppOptions.uncheckVersionBefore_5_4(false);
	   } else {
		   PDICMainAppOptions.bCheckVersionBefore_5_4=false;
	   }
		MdictServer.hasRemoteDebugServer &= PDICMainAppOptions.debug();
	   //inflater=getLayoutInflater();
       imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	   //btnMaxWidth=GlobalOptions.btnMaxWidth;
		//CMN.show("isLarge"+isLarge);
	   isDarkStamp = GlobalOptions.isDark;
	   AppBlack=GlobalOptions.isDark?Color.WHITE:Color.BLACK;
	   AppWhite=GlobalOptions.isDark?Color.BLACK:Color.WHITE;

	   if(opt.getUseCustomCrashCatcher()){
		   CrashHandler.getInstance(this, opt).TurnOff();
		   CrashHandler.getInstance(this, opt).register(getApplicationContext());
	   }

	   if(PDICMainAppOptions.getKeepScreen()){
		   getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	   }

	   checkLanguage();
   }
	
	protected void readSizeConfigs() {
		int szMsk = mConfiguration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		GlobalOptions.isLarge = szMsk >= 3;
		GlobalOptions.isSmall = szMsk == 1 ;
		GlobalOptions.width=(int)(dm.widthPixels/GlobalOptions.density);
		GlobalOptions.height=(int)(dm.heightPixels/GlobalOptions.density);
		GlobalOptions.btnMaxWidth=Math.min((int)mResource.getDimension(R.dimen.btnMaxWidth), dm.widthPixels/8);
		CMN.debug("MaxWidth::", (int)mResource.getDimension(R.dimen.btnMaxWidth), GlobalOptions.btnMaxWidth);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(!shunt) {
			CrashHandler.getInstance(this, opt).TurnOn();
		}
		systemIntialized=false;
	}

	@Override
	protected void onResume() {
		try {
			super.onResume();
		} catch (Exception ignored) { /*无敌*/ }
	}

	@Override
	public File getDatabasePath(String pathname){
		return new File(pathname);
	}

	protected void checkLog(Bundle savedInstanceState){
		Bag flag = new Bag(false);
		// skipCheckLog = true;
		if(!skipCheckLog && opt.getLogToFile()){
			try {
				File log=new File(CrashHandler.getInstance(this, opt).getLogFile());
				File lock=new File(log.getParentFile(),"lock");
				if(lock.exists())
				{
					if (lock.length() > 0) {
						String oldVersion = BU.fileToString(lock);
						//oldVersion = "v6.0";
						boolean b1=oldVersion!=null && oldVersion.startsWith("v");
						lock.delete();
						if (b1 || !log.exists()) {
							if (b1) {
								String nowName = BuildConfig.VERSION_NAME;
								if(!oldVersion.startsWith("v")) nowName = "v" + nowName;
								if (!oldVersion.equals(nowName)) {
									((MainActivityUIBase)this).showUpdateInfos(oldVersion);
									final File target = new File(getExternalCacheDir(), "apks/"+nowName+".apk");
									target.delete();
								}
							}
							throw new RuntimeException();
						}
					}
					flag.val=true;
					setStatusBarColor(getWindow(), Constants.DefaultMainBG);
					CrashHandler.getInstance(this, opt).showErrorMessage(this, (dialog, whichButton) -> {
						lock.delete();
						checkLaunch(savedInstanceState);
					}, false);
				}
			} catch (Exception e) { CMN.debug(e); } finally {
				if(!flag.val)
					checkLaunch(savedInstanceState);
			}
		} else {
			checkLaunch(savedInstanceState);
		}
	}
	
	public static void setStatusBarColor(Window window, int color){
		if (window!=null) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			if(Build.VERSION.SDK_INT>=21) {
				//color = ColorUtils.blendARGB(color, Color.BLACK, 0.14f);
				window.setStatusBarColor(color);
				if(true){
					//color = ColorUtils.blendARGB(color, Color.BLACK, 0.14f);
					window.setNavigationBarColor(color);
					//window.setNavigationBarColor(0xff2b4381);
				}
			}
		}
	}

	public static void setWindowsPadding(@NonNull View decorView) {
		decorView.setPadding(DockerMarginL, DockerMarginT, DockerMarginR, DockerMarginB);
	}

	protected void checkFlags() {
		if (opt.checkModified(flags, false)) {
			opt.fillFlags(flags);
		}
	}

	protected void checkLanguage() {
		PDICMainAppOptions.locale =null;
		String language=opt.getLocale();
		if(language!=null){
			Locale locale = null;
			if(language.length()==0){
				locale=Locale.getDefault();
			} else try {
				if(language.contains("-r")){
					String[] arr=language.split("-r");
					if(arr.length==2){
						locale=new Locale(arr[0], arr[1]);
					}
				} else {
					locale=new Locale(language);
				}
			} catch (Exception ignored) { }
			//CMN.Log("language is : ", language, locale);
			if(locale!=null) {
				forceLocale(this, locale);
				forceLocale(getApplicationContext(), locale);
			}
		}
	}
	
	protected static void forceLocale(Context context, Locale locale) {
		Configuration conf = context.getResources().getConfiguration();
		conf.setLocale(locale);
		context.getResources().updateConfiguration(conf, context.getResources().getDisplayMetrics());
	}
	
	protected void checkLaunch(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//大于 23 时
			if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
				setStatusBarColor(getWindow(), Constants.DefaultMainBG);
				File trialPath = getExternalFilesDir("Trial");
				if(trialPath!=null){
					if(trialPath.isDirectory()) {
						File[] fs = trialPath.listFiles();
						for(File mf:fs) {
							if(!mf.isDirectory()) {
								String fn=mf.getName();
								if(!fn.contains("."))
									try {
										trialCount=Integer.parseInt(fn);
										break;
									}catch(NumberFormatException ignored){}
							}
						}
					}
					if(trialCount>=2) {
						opt.rootPath=trialPath;
						if(Build.VERSION.SDK_INT<=30) {
							showT("存储受限，持续试用中……");
						}
						pre_further_loading(savedInstanceState);
						return;
					}
				}
				dialogRequestPermissionTips();
			}else {pre_further_loading(savedInstanceState);}
		}else {pre_further_loading(savedInstanceState);}
	}



	// 动态获取权限
	@RequiresApi(api = Build.VERSION_CODES.M)
	protected void dialogRequestPermissionTips() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.stg_require)
				.setMessage(R.string.stg_statement)
				.setPositiveButton(R.string.stg_grantnow, (dialog, which) -> requestPermissions(permissions, 321))
				.setWikiText("", (dialog, which) -> dialogPermissionDetails(this))
				.setNegativeButton(R.string.cancel, (dialog, which) -> EnterTrialMode()).setCancelable(false).show().getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
	}
	
	public void dialogPermissionDetails(Activity context) {
		AlertDialog d = new AlertDialog.Builder(context)
				.setTitle("权限列表")
				.setMessage("")
				.setPositiveButton(R.string.confirm, null)
				.show();
		Markwon markwon = Markwon.create(context);
		TextView tv = d.findViewById(android.R.id.message);
		opt.setAsLinkedTextView(tv, false, true);
		tv.setTextSize(GlobalOptions.isLarge?20:15);
		markwon.setMarkdown(tv, ViewUtils.fileToString(context, new File(CMN.AssetTag, "quanxian.md")));
	}
	
	protected void EnterTrialMode() {
		File trialPath = getExternalFilesDir("Trial");
		int trialCount=-1;
		boolean b1=trialPath.exists()&&trialPath.isDirectory();
		if(b1) {
			File[] fs = trialPath.listFiles();
			if(fs!=null) for(File mf:fs) {
				if(!mf.isDirectory()) {
					String fn=mf.getName();
					if(!fn.contains("."))
						try {
							trialCount=Integer.valueOf(fn);
							break;
						}catch(NumberFormatException ignored){}
				}
			}
		}
		if(b1 || trialPath.mkdirs()) {
			opt.rootPath=getExternalFilesDir("");
			try {
				if(trialCount!=-1) {
					new File(trialPath,String.valueOf(trialCount))
							.renameTo(new File(trialPath,String.valueOf(++trialCount)));
				}else
					new File(trialPath,String.valueOf(trialCount=0)).createNewFile();
			} catch (IOException ignored) {}
			pre_further_loading(null);
			showT(getResources().getString(R.string.trialin)+trialCount);
		}
		else {
			Toast.makeText(Toastable_Activity.this, R.string.stgerr_fail, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	protected void scanSettings() {

	}
	
	protected void pre_further_loading(final Bundle savedInstanceState) {//determine our opt.rootPath
		if(opt.rootPath!=null) {
			further_loading(savedInstanceState);
			return;
		}
		File trialPath = getExternalFilesDir("Trial");
		if(trialPath.exists()) {
			File[] fs = trialPath.listFiles();
			for(File mf:fs) {
				if(!mf.isDirectory())
					mf.delete();
		}}
				
		final File SdcardStorage = Environment.getExternalStorageDirectory();
		final File IdlDwlng = new File(SdcardStorage, CMN.BrandName);
		if(!IdlDwlng.isDirectory()) {
			File InternalAppStorage = getExternalFilesDir("");
			final File subfolder = new File(InternalAppStorage, CMN.BrandName);
			if(PDICMainAppOptions.lastUsingInternalStorage() && subfolder.exists() && subfolder.isDirectory()) {
				/* look if the an Internal files system created. */
				opt.rootPath=InternalAppStorage;
				further_loading(savedInstanceState);
				return;
			}
			new AlertDialog.Builder(this)//ask the user
				.setTitle(getResources().getString(R.string.AAllow))
				.setPositiveButton(R.string.yes, (dialog, which) -> {
					if(IdlDwlng.mkdirs() || IdlDwlng.isDirectory()) {//建立文件夹
						opt.rootPath=SdcardStorage;
						further_loading(savedInstanceState);
						PDICMainAppOptions.lastUsingInternalStorage(false);
					} else { showT("未知错误：配置存储目录建立失败！");finish(); }
				})
			.setNegativeButton(R.string.no, (dialog, which) -> {
				if((subfolder.exists()&&subfolder.isDirectory()) || subfolder.mkdirs()) {//建立文件夹
					showT("配置存储在标准目录");
					opt.rootPath=InternalAppStorage;
					further_loading(savedInstanceState);
					PDICMainAppOptions.lastUsingInternalStorage(true);
				} else { showT("未知错误：配置存储目录建立失败！");finish(); }
			}).setCancelable(false).show();
		} else {
			/* Ideal Dwelling (/sdcard/PLOD) already exists. nothing to ask for. */
			further_loading(savedInstanceState);
		}
	}
	
	protected void further_loading(Bundle savedInstanceState) {
		
//if(true) { // 测试调试混淆信息
//	savedInstanceState.putInt("test", 1/DockerMarginR);
//}
		
		scanSettings();
	}
   
	Toast m_currentToast;
	TextView toastTv;
	View toastV;
	public void showX(int ResId,int len, Object...args) {
		String text = StringUtils.EMPTY;
		try {
			text = getResources().getString(ResId, args);
		} catch (Exception ignored) {  }
		showT(text,len);
	}
	public void show(int ResId,Object...args) {
		showX(ResId,Toast.LENGTH_SHORT, args);
	}
	public void showT(Object text)
	{
		showT(text,Toast.LENGTH_LONG);
	}
	public void showT(Object obj,int len)
	{
		CharSequence text = obj instanceof Integer? getResources().getText((Integer) obj):String.valueOf(obj);
		if(m_currentToast == null || PDICMainAppOptions.getRebuildToast()){
			if(m_currentToast!=null)
				m_currentToast.cancel();
			if(toastTv==null) {
				toastV = getLayoutInflater().inflate(R.layout.toast, null);
				toastTv = toastV.findViewById(R.id.message);
			}else if(toastV.getParent() instanceof ViewGroup){
				((ViewGroup)toastV.getParent()).removeView(toastV);
			}
			m_currentToast = new Toast(this);
			m_currentToast.setView(toastV);
		}
		m_currentToast.setGravity(Gravity.BOTTOM, 0, 135);
		if(toastV.getBackground() instanceof GradientDrawable){
			GradientDrawable drawable = (GradientDrawable) toastV.getBackground();
			drawable.setCornerRadius(PDICMainAppOptions.getToastRoundedCorner()?dm.density*15:0);
			drawable.setColor(opt.getToastBackground());
		}
		m_currentToast.setDuration(len);
		toastTv.setText(text);
		toastTv.setTextColor(opt.getToastColor());
		m_currentToast.show();
	}
	public void showMT(Object text){
		showT(text);
		m_currentToast.setGravity(Gravity.CENTER, 0, 0);
	}
	public void cancelToast(){
		if(m_currentToast!=null)
			m_currentToast.cancel();
	}

	public void showTopSnack(Object messageVal){
		showTopSnack(null, messageVal, 0.8f, -1, -1, 0);
	}

	public void showContentSnack(Object messageVal){
		showTopSnack(null, messageVal, 0.6f, -1, -1, 0);
	}
	
	public void showTopSnack(ViewGroup parentView, Object messageVal) {
		showTopSnack(parentView, messageVal, 0.5f, -1, -1, 0);
	}
	
	protected SimpleTextNotifier getTopSnackView() {
		SimpleTextNotifier topsnack = this.topsnack;
		if(topsnack==null) {
			topsnack = new SimpleTextNotifier(getBaseContext());
			topsnack.setTextSize(18f);
			Resources res = getResources();
			topsnack.setTextColor(Color.WHITE);
			topsnack.setBackgroundColor(res.getColor(R.color.colorHeaderBlueT));
			int pad = (int) res.getDimension(R.dimen.design_snackbar_padding_horizontal);
			topsnack.setPadding(pad,pad/2,pad,pad/2);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				topsnack.setElevation(res.getDimension(R.dimen.design_snackbar_elevation));
			}
			topsnack.setLayoutParams(new FrameLayout.LayoutParams(-1,-2));
			this.topsnack = topsnack;
		}
		return topsnack;
	}
	
	/** Show Top Snack
	 * @param layoutFlags ltr : bSingleLine bWrapContentWidth bPostSnack
	 *  */
	void showTopSnack(ViewGroup parentView, Object messageVal, float alpha, int duration, int gravity, int layoutFlags) {
		SimpleTextNotifier topsnack = getTopSnackView();
		topsnack.setNextOffsetScale(0.25f);
		try {
			parentView = onShowSnack(parentView);
		} catch (Exception e) {
			CMN.debug(e); // ViewRootImpl cannot be cast to android.view.ViewGroup
		}
		if(duration<0) {
			duration = SHORT_DURATION_MS;
		}
		topsnack.setDuration(duration);
		topsnack.getBackground().setAlpha((int) (alpha*255));
		topsnack.setAlpha(1);
		topsnack.setSingleLine((layoutFlags&0x1)!=0);
		if(messageVal instanceof Integer) {
			topsnack.setText(topsnack.msg = (int) messageVal);
			topsnack.setTag(messageVal);
		} else {
			topsnack.setText(String.valueOf(messageVal));
			topsnack.setTag(null);
		}
		topsnack.setGravity(gravity<0?Gravity.CENTER:gravity);
		View snackView = topsnack.getSnackView();
		if(ViewUtils.addViewToParent(snackView, parentView) || layoutFlags!=layoutFlagStamp) {
			topsnack.setVisibility(View.INVISIBLE);
			ViewGroup.LayoutParams lp = topsnack.getLayoutParams();
			boolean bWrapContentWidth = ((layoutFlags&0x2)!=0);
			lp.height=-2;
			lp.width=bWrapContentWidth?-2:-1;
			topsnack.postShow();
			layoutFlagStamp = layoutFlags;
		} else {
			if((layoutFlags&0x4)!=0) topsnack.postShow();
			else topsnack.show();
		}
		ViewGroup.LayoutParams lp = snackView.getLayoutParams();
		if(lp instanceof FrameLayout.LayoutParams) {
			((FrameLayout.LayoutParams)lp).gravity=Gravity.BOTTOM;
		} else if (lp instanceof RelativeLayout.LayoutParams) {
			((RelativeLayout.LayoutParams)lp).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		}
		if (lp instanceof ViewGroup.MarginLayoutParams) {
			((ViewGroup.MarginLayoutParams) lp).setMargins(0,0,0,topsnack.getBottomMargin());
		}
	}
	
	protected ViewGroup onShowSnack(ViewGroup parentView) { return parentView; }
	
	protected void fadeSnack() {
		if(topsnack!=null) {
			topsnack.fadeOut();
		}
	}

	public static void checkMargin(Activity a) {
		if(!MarginChecked) {
			//File additional_config = new File(opt.pathToMainFolder()+"appsettings.txt");
			File additional_config = new File(Environment.getExternalStorageDirectory(), "PLOD/appsettings.txt");
			if (additional_config.exists()) {
				try {
					BufferedReader in = new BufferedReader(new FileReader(additional_config));
					String line;
					while ((line = in.readLine()) != null) {
						String[] arr = line.split(":", 2);
						if (arr.length == 2) {
							switch (arr[0]) {
								case "window margin":
								case "窗体边框":
									arr = arr[1].split(" ");
									if (arr.length == 4) {
										try {
											DockerMarginL = Integer.valueOf(arr[2]);
											DockerMarginR = Integer.valueOf(arr[3]);
											DockerMarginT = Integer.valueOf(arr[0]);
											DockerMarginB = Integer.valueOf(arr[1]);
										} catch (Exception e) {//CMN.Log(e);
										}
									}
									break;
							}
						}
					}
				} catch (Exception ignored) {
				}
			}
			MarginChecked=true;
		}
		View targetView;
		if(a instanceof Toastable_Activity)
			targetView=((Toastable_Activity)a).root;
		else
			targetView=a.findViewById(R.id.root);
		if(targetView != null && (DockerMarginL!=0 || DockerMarginR!=0 || DockerMarginT!=0 || DockerMarginB!=0)){
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) targetView.getLayoutParams();
			lp.leftMargin = DockerMarginL;
			lp.rightMargin = DockerMarginR;
			lp.topMargin = DockerMarginT;
			lp.bottomMargin = DockerMarginB;
			targetView.setTag(false);
			targetView.setLayoutParams(lp);
		}
	}
	
	protected void CheckInternalDataBaseDirExist(boolean testDBV2) {
		File checkDirs = opt.fileToDatabaseFavorites(testDBV2);
		checkDirs.mkdirs();
	}
	
	public LexicalDBHelper prepareHistoryCon() {
		if(historyCon!=null) return historyCon;
		AgentApplication app = ((AgentApplication) getApplication());
		LexicalDBHelper _historyCon = app.historyCon;
		if(_historyCon!=null){
			if(!new File(_historyCon.pathName).exists())
				_historyCon=null;
		}
		if(_historyCon==null){
			CheckInternalDataBaseDirExist(opt.getUseDatabaseV2());
			app.historyCon = _historyCon = new LexicalDBHelper(getApplicationContext(), opt, opt.getUseDatabaseV2());
		}
		_historyCon.setFavoriteFolderId(opt.getCurrFavoriteNoteBookId());
		return historyCon = _historyCon;
	}
	
	public void FuzhiText(String url) {
		try {
			ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			if(cm!=null){
				cm.setPrimaryClip(ClipData.newPlainText(null, url));
				showT("已复制");
			}
		} catch (Exception e) { }
	}
	
	public TextView buildStandardConfigDialog(OptionProcessor optprs
			, boolean centerText
			, Object btnIdListener
			, int title_id
			, Object... title_args) {
		final View dv = getLayoutInflater().inflate(R.layout.dialog_about,null);
		final TextView tv = dv.findViewById(R.id.resultN);
		TextView title = dv.findViewById(R.id.title);
		if (title_id!=0) {
			if(title_args.length>0){
				title.setText(mResource.getString(title_id, title_args));
			} else {
				title.setText(title_id);
			}
		} else if(title_args.length>0 && title_args[0] instanceof CharSequence){
			title.setText((CharSequence) title_args[0]);
			if(title_args[0] instanceof CharSequence) {
				//opt.setAsLinkedTextView(title, false);
				title.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
			}
		}
		title.setTextSize(GlobalOptions.isLarge?19f:18f);
		title.setTextColor(AppBlack);
		//title.getPaint().setFakeBoldText(true);
		
		int topad = (int) mResource.getDimension(R.dimen._18_);
		((ViewGroup)title.getParent()).setPadding(topad*3/5, topad/2, 0, 0);
		//((ViewGroup)title.getParent()).setClipToPadding(false);
		//((ViewGroup.MarginLayoutParams)title.getLayoutParams()).setMarginStart(-topad/4);
		
		opt.setAsLinkedTextView(tv, centerText, true);
		
		final androidx.appcompat.app.AlertDialog configurableDialog =
				new androidx.appcompat.app.AlertDialog.Builder(this,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line)
						.setView(dv)
//						.setPositiveButton(R.string.confirm, null)
//						.setNegativeButton(R.string.cancel, null)
						.create();
		configurableDialog.setCanceledOnTouchOutside(true);
		ViewUtils.ensureWindowType(configurableDialog, (MainActivityUIBase) this, null);
		
		dv.findViewById(R.id.cancel).setOnClickListener(v -> {
			if(btnIdListener instanceof Integer) optprs.processOptionChanged(null, null, (Integer) btnIdListener, 0);
			else if(btnIdListener instanceof View.OnClickListener) ((View.OnClickListener) btnIdListener).onClick(v);
			configurableDialog.dismiss();
		});
		tv.setTag(configurableDialog);
		configurableDialog.tag = new Object[]{opt, optprs};
		
		return tv;
	}
	
	public static void init_clickspan_with_bits_at(TextView tv, SpannableStringBuilder text,
												   String[] dictOpt, int titleOff, String[] coef, int coefOff,
												   int coefShift, long mask,
												   int flagPosition, int flagMax, int flagIndex,
												   int processId, boolean addColon) {
		Object[] tags;
		if (tv.getTag() instanceof androidx.appcompat.app.AlertDialog) {
			tags = (Object[]) ((androidx.appcompat.app.AlertDialog)tv.getTag()).tag;
		} else {
			tags = (Object[]) tv.getTag();
		}
		PDICMainAppOptions opt = (PDICMainAppOptions) tags[0];
		OptionProcessor optprs = (OptionProcessor) tags[1];
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
			}},start,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.append("\r\n").append("\r\n");
	}
	
	public static void showStandardConfigDialog(TextView tv, SpannableStringBuilder ssb) {
		int length = ssb.length();
		ssb.delete(length-4,length);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
		((androidx.appcompat.app.AlertDialog) tv.getTag()).show();
		((androidx.appcompat.app.AlertDialog) tv.getTag()).tag=null;
		tv.setTag(null);
	}
	
	public void moveTaskToFront() {
		ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		if (manager != null)
			manager.moveTaskToFront(getTaskId(), 0);
	}
}





package com.knziha.plod.PlainDict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.widgets.SimpleTextNotifier;
import com.knziha.plod.widgets.Utils;
import com.sun.tools.internal.xjc.reader.gbind.ElementSets;

import org.apache.commons.imaging.formats.tiff.photometricinterpreters.PhotometricInterpreterBiLevel;
import org.apache.commons.lang3.StringUtils;

public class Toastable_Activity extends AppCompatActivity {
	public boolean systemIntialized;
	protected final static String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

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
	
	public long lastClickTime=0;

	protected long FFStamp;
	protected long SFStamp;
	protected long TFStamp;
	protected long QFStamp;
	protected long layoutFlagStamp;
	public int MainBackground;
	public int AppBlack;
	public int AppWhite;
    public float ColorMultiplier_Wiget=0.9f;
    public float ColorMultiplier_Web=1;
    public float ColorMultiplier_Web2=1;

	public ViewGroup contentview;
	protected ViewGroup dialog_;
    protected Toolbar toolbar;
    protected EditText etSearch;
    protected ImageView ivDeleteText;
    protected ImageView ivBack;

	protected ObjectAnimator objectAnimator;
    
	public Dialog d;
	public View dv;
	Configuration mConfiguration;
	boolean isDarkStamp;

	boolean animationSnackOut;
	SimpleTextNotifier topsnack;
	Runnable snackWorker;
	Runnable snackRemover;
	private Animator.AnimatorListener topsnackListener;
	static final int FLASH_DURATION_MS = 800;
	static final int SHORT_DURATION_MS = 1500;
	static final int LONG_DURATION_MS = 2355;
	int NextSnackLength;
	protected ViewGroup DefaultTSView;
	long exitTime = 0;
	protected Resources mResource;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
       if(!shunt) {
		   opt = new PDICMainAppOptions(this);
		   opt.dm = dm = new DisplayMetrics();
		   Display display = getWindowManager().getDefaultDisplay();
		   if (GlobalOptions.realWidth <= 0) {
			   display.getRealMetrics(dm);
			   GlobalOptions.realWidth = Math.min(dm.widthPixels, dm.heightPixels);
			   GlobalOptions.density = dm.density;
			   //GlobalOptions.densityDpi = dm.densityDpi;
		   }
		   display.getMetrics(dm);
		   GlobalOptions.density = dm.density;
		   FFStamp = opt.getFirstFlag();
		   SFStamp = opt.getSecondFlag();
		   TFStamp = opt.getThirdFlag();
		   QFStamp = opt.getFourthFlag();
	   }
	   super.onCreate(savedInstanceState);
       if(shunt) return;
		snackRemover = () -> {
			if(topsnack!=null && topsnack.getParent()!=null)
				((ViewGroup)topsnack.getParent()).removeView(topsnack);
		};
	   //inflater=getLayoutInflater();
       imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
       mResource = getResources();
       
	   mConfiguration = new Configuration(mResource.getConfiguration());
	   GlobalOptions.isLarge = (mConfiguration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=3 ;
		//CMN.show("isLarge"+isLarge);
	   if(Build.VERSION.SDK_INT>=29){
		   GlobalOptions.isDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
	   } else {
		   GlobalOptions.isDark = false;
	   }
	   opt.getInDarkMode();
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
   
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(!shunt) {
			CrashHandler.getInstance(this, opt).TurnOn();
		}
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
		boolean[] launching=new boolean[]{false};
		if(DoesActivityCheckLog()){
			if(opt.getLogToFile()){
				try {
					File log=new File(CrashHandler.getInstance(this, opt).getLogFile());
					File lock;
					if(log.exists()){
						if((lock=new File(log.getParentFile(),"lock")).exists()){
							byte[] buffer = new byte[Math.min((int) log.length(), 4096)];
							int len = new FileInputStream(log).read(buffer);
							String message=new String(buffer,0,len);
							launching[0]=true;
							setStatusBarColor(getWindow(), Constants.DefaultMainBG);
							new androidx.appcompat.app.AlertDialog.Builder(this)
								.setMessage(message)
								.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
									lock.delete();
									dialog.dismiss();
									checkLaunch(savedInstanceState);
								})
								.setTitle("天哪，崩溃了……")
								.setCancelable(false)
								.show();
						}
					}
				} catch (Exception e) { CMN.Log(e); }finally {
					if(!launching[0])
						checkLaunch(savedInstanceState);
				}
			}else{
				checkLaunch(savedInstanceState);
			}
		}else{
			File lock;
			File log=new File(CrashHandler.getInstance(this, opt).getLogFile());
			if((lock=new File(log.getParentFile(),"lock")).exists())
				lock.delete();
		}
	}
	
	public static void setStatusBarColor(Window window, int color){
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
	
	protected boolean DoesActivityCheckLog() {
		return true;
	}

	public static void setWindowsPadding(@NonNull View decorView) {
		decorView.setPadding(DockerMarginL, DockerMarginT, DockerMarginR, DockerMarginB);
	}

	protected void checkFlags() {
		if(checkFlagsChanged()){
			notifyFlagChanged();
			opt.setFlags(null, 1);
			FFStamp=opt.FirstFlag();
			SFStamp=opt.SecondFlag();
			TFStamp=opt.ThirdFlag();
			QFStamp=opt.FourthFlag();
		}
	}

	protected void notifyFlagChanged() { }

	protected boolean checkFlagsChanged() {
		return FFStamp!=opt.FirstFlag() || SFStamp!=opt.SecondFlag() || TFStamp!=opt.getThirdFlag()|| QFStamp!=opt.getFourthFlag();
	}

	protected void checkLanguage() {
		PDICMainAppOptions.locale =null;
		String language=opt.getLocale();
		if(language!=null){
			Locale locale = null;
			if(language.length()==0){
				locale=Locale.getDefault();
			}else try {
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
				showDialogTipUserRequestPermission();
			}else {pre_further_loading(savedInstanceState);}
		}else {pre_further_loading(savedInstanceState);}
	}



	// 动态获取权限
	@RequiresApi(api = Build.VERSION_CODES.M)
	protected void showDialogTipUserRequestPermission() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.stg_require)
				.setMessage(R.string.stg_statement)
				.setPositiveButton(R.string.stg_grantnow, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestPermissions(permissions, 321);
					}
				})
				.setNegativeButton(R.string.cancel, (dialog, which) -> EnterTrialMode()).setCancelable(false).show().getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
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
			if(subfolder.exists() && subfolder.isDirectory()) {
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
	
					}else {showT("未知错误：配置存储目录建立失败！");finish();}
				})
			.setNegativeButton(R.string.no, (dialog, which) -> {
				if((subfolder.exists()&&subfolder.isDirectory()) || subfolder.mkdirs()) {//建立文件夹
					showT("配置存储在标准目录");
					opt.rootPath=InternalAppStorage;
					further_loading(savedInstanceState);

				}else {showT("未知错误：配置存储目录建立失败！");finish();}
			}).setCancelable(false).show();
			
		}else{
			/* Ideal Dwelling (/sdcard/PLOD) already exists. nothing to ask for. */
			further_loading(savedInstanceState);
		}
	}
	
	protected void further_loading(Bundle savedInstanceState) {
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
	public void cancleToast(){
		if(m_currentToast!=null)
			m_currentToast.cancel();
	}

	ViewGroup getContentviewSnackHolder() {
		return contentview;
	}

	public void showTopSnack(Object messageVal){
		showTopSnack(DefaultTSView, messageVal, 0.8f, -1, -1, 0);
	}

	public void showContentSnack(Object messageVal){
		showTopSnack(getContentviewSnackHolder(), messageVal, 0.6f, -1, -1, 0);
	}

	void modifyTopSnack(Object messageVal){
		if(messageVal instanceof Integer) {
			topsnack.setText((int) messageVal);
		}else {
			topsnack.setText(String.valueOf(messageVal));
		}
	}

	/** Show Top Snack
	 * @param layoutFlags ltr : bSingleLine bWrapContentWidth bPostSnack
	 *  */
	void showTopSnack(ViewGroup parentView, Object messageVal, float alpha, int duration, int gravity, int layoutFlags) {
		if(objectAnimator!=null){
			objectAnimator.cancel();
			objectAnimator=null;
		}
		if(topsnack==null){
			topsnack = new SimpleTextNotifier(getBaseContext());
			Resources res = getResources();
			topsnack.setTextColor(Color.WHITE);
			topsnack.setBackgroundColor(res.getColor(R.color.colorHeaderBlueT));
			int pad = (int) res.getDimension(R.dimen.design_snackbar_padding_horizontal);
			topsnack.setPadding(pad,pad/2,pad,pad/2);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				topsnack.setElevation(res.getDimension(R.dimen.design_snackbar_elevation));
			}
		}
		else{
			topsnack.removeCallbacks(snackRemover);
			topsnack.setAlpha(1);
		}
		NextSnackLength=duration<0?SHORT_DURATION_MS:duration;
		topsnack.getBackground().setAlpha((int) (alpha*255));
		topsnack.setSingleLine((layoutFlags&0x1)!=0);
		if(messageVal instanceof Integer) {
			topsnack.setText((int) messageVal);
			topsnack.setTag(messageVal);
		} else {
			topsnack.setText(String.valueOf(messageVal));
			topsnack.setTag(null);
		}
		topsnack.setGravity(gravity<0?Gravity.CENTER:gravity);
		ViewGroup sp = (ViewGroup) topsnack.getParent();
		if(sp!=parentView || layoutFlags!=layoutFlagStamp) {
			if(sp!=null) sp.removeView(topsnack);
			topsnack.setVisibility(View.INVISIBLE);
			parentView.addView(topsnack);
			ViewGroup.LayoutParams lp = topsnack.getLayoutParams();
			boolean bWrapContentWidth = ((layoutFlags&0x2)!=0);
			lp.height=-2;
			lp.width=bWrapContentWidth?-2:-1;
			topsnack.post(snackWorker);
			layoutFlagStamp = layoutFlags;
		} else {
			topsnack.removeCallbacks(snackWorker);
			if((layoutFlags&0x4)!=0) topsnack.post(snackWorker);
			else snackWorker.run();
		}
	}

	protected void cancleSnack() {
		if(topsnack!=null && topsnack.getParent()!=null) {
			if(objectAnimator!=null){
				objectAnimator.removeAllListeners();
				objectAnimator.cancel();
			}
			//if(R.string.warn_exit== IU.parseInteger(topsnack.getTag(),0))
			//	exitTime=0;
			objectAnimator = ObjectAnimator.ofFloat(topsnack,"alpha",topsnack.getAlpha(),0f);
            objectAnimator.setDuration(240);
            objectAnimator.start();
            if(topsnackListener==null){
				topsnackListener = new Animator.AnimatorListener() {
					@Override public void onAnimationStart(Animator animation) { }
					@Override public void onAnimationEnd(Animator animation) {
						removeSnackView();
					}
					@Override public void onAnimationCancel(Animator animation) { }
					@Override public void onAnimationRepeat(Animator animation) { }
				};
			}
			objectAnimator.addListener(topsnackListener);
		}
	}

	void removeSnackView(){
		topsnack.removeCallbacks(snackRemover);
		topsnack.postDelayed(snackRemover, 2000);
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

}





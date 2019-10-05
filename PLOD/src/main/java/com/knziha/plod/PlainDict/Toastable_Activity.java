package com.knziha.plod.PlainDict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.knziha.plod.widgets.SimpleTextNotifier;

public class Toastable_Activity extends AppCompatActivity {
	public boolean systemIntialized;
	protected String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

	protected int DockerMarginL;
	protected int DockerMarginR;
	protected int DockerMarginT;
	protected int DockerMarginB;
	protected ViewGroup root;
	//public dictionary_App_Options opt;
    //public List<mdict> md = new ArrayList<mdict>();//Collections.synchronizedList(new ArrayList<mdict>());

	public PDICMainAppOptions opt;
	public DisplayMetrics dm;
	public LayoutInflater inflater;
	public InputMethodManager imm;
	protected int trialCount=-1;
	
	public long lastClickTime=0;

	long FFStamp;
	long SFStamp;
	long TFStamp;
	protected int MainBackground;
	public int AppBlack;
	public int AppWhite;
    public float ColorMultiplier_Wiget=0.9f;
    public float ColorMultiplier_Web=1;
    public float ColorMultiplier_Web2=1;

	public ViewGroup contentview;
	protected ViewGroup dialogHolder;
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
	Runnable snackRemover= () -> {
		if(topsnack!=null && topsnack.getParent()!=null)
			((ViewGroup)topsnack.getParent()).removeView(topsnack);
	};
	private Animator.AnimatorListener topsnackListener;
	static final int SHORT_DURATION_MS = 1500;
	static final int LONG_DURATION_MS = 2355;
	int NextSnackLength;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
       opt = new PDICMainAppOptions(this);
       opt.dm = dm = new DisplayMetrics();
	   getWindowManager().getDefaultDisplay().getMetrics(dm);
	   super.onCreate(savedInstanceState);
	   FFStamp=opt.getFirstFlag();
	   SFStamp=opt.getSecondFlag();
	   //TFStamp=opt.getThirdFlag();
	   inflater=getLayoutInflater();
       imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

	   mConfiguration = new Configuration(getResources().getConfiguration());
	   if(Build.VERSION.SDK_INT>=29){
		   GlobalOptions.isDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
	   }else
		   GlobalOptions.isDark = false;
	   opt.getInDarkMode();
	   isDarkStamp = GlobalOptions.isDark;
	   AppBlack=GlobalOptions.isDark?Color.WHITE:Color.BLACK;
	   AppWhite=GlobalOptions.isDark?Color.BLACK:Color.WHITE;

	   if(opt.getUseCustomCrashCatcher()){
		   CrashHandler.getInstance(this, opt).TurnOff();
		   CrashHandler.getInstance(this, opt).register(getApplicationContext());
	   }

	   if(PDICMainAppOptions.getKeepScreen())
		   getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	   checkLanguage();
   }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CrashHandler.getInstance(this, opt).TurnOn();
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
							Dialog d = new androidx.appcompat.app.AlertDialog.Builder(this)
									.setMessage(message)
									.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
										lock.delete();
										dialog.dismiss();
										checkLaunch(savedInstanceState);
									})
									.setTitle("检测到异常捕获。（如发现仍不能启动，可尝试重新初始化）")
									.setCancelable(false)
									.show();
							//.create();
							((TextView)d.findViewById(R.id.alertTitle)).setSingleLine(false);
							((TextView)d.findViewById(android.R.id.message)).setTextIsSelectable(true);
							//FilePickerDialog.stylize_simple_message_dialog(d, getApplicationContext());
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

	protected boolean DoesActivityCheckLog() {
		return true;
	}

	public void fix_full_screen(@Nullable View decorView) {
		if(opt.isFullScreen() && false) {//opt.isFullscreenHideNavigationbar()
			if(decorView==null) decorView=getWindow().getDecorView();
			//int options = decorView.getSystemUiVisibility();
			int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LOW_PROFILE
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE
					;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	protected void checkFlags() {
		if(checkFlagsChanged()){
			opt.setFlags(null, 1);
			FFStamp=opt.FirstFlag();
			SFStamp=opt.SecondFlag();
		}
	}

	protected boolean checkFlagsChanged() {
		return FFStamp!=opt.FirstFlag() || SFStamp!=opt.SecondFlag();// || TFStamp!=opt.getThirdFlag();
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
				}else
					locale=new Locale(language);
			} catch (Exception ignored) { }
			CMN.Log("language is : ", language, locale);
			if(locale!=null)
				forceLocale(this, locale);
		}
	}

	protected void forceLocale(Context context, Locale locale) {
		Configuration conf = context.getResources().getConfiguration();
		conf.setLocale(locale);
		context.getResources().updateConfiguration(conf, context.getResources().getDisplayMetrics());
	}

	protected void checkLaunch(Bundle savedInstanceState) {
   	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//大于 23 时
		if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
			File trialPath = getExternalFilesDir("Trial");
			boolean b1=trialPath.exists()&&trialPath.isDirectory();
			if(b1) {
				File[] fs = trialPath.listFiles();
				for(File mf:fs) {
					if(!mf.isDirectory()) {
						String fn=mf.getName();
						if(fn.indexOf(".")==-1)
						try {
							trialCount=Integer.valueOf(fn);
							break;
						}catch(NumberFormatException e){}
					}
				}
			}
			if(trialCount>=2) {
				opt.rootPath=trialPath.getAbsolutePath();
				showT("存储受限，持续试用中……");
				pre_further_loading(savedInstanceState);
				return;
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
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
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
							opt.rootPath=getExternalFilesDir("").getAbsolutePath();
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
				}).setCancelable(false).show().getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
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
				
		final String path = Environment.getExternalStorageDirectory().getPath();
		final File mainfolder = new File(path+"/PLOD/");
		if(!(mainfolder.exists()&&mainfolder.isDirectory())) {
			final File subfolder = new File(getExternalFilesDir("")+"/PLOD/");
			if(subfolder.exists() && subfolder.isDirectory()) {//look if the an Internal files system created.
				opt.rootPath=getExternalFilesDir("").getAbsolutePath();
				further_loading(savedInstanceState);
				return;
			}
			new AlertDialog.Builder(this)//ask the user
			.setTitle(getResources().getString(R.string.AAllow))
			.setPositiveButton(R.string.yes, (dialog, which) -> {
				if((mainfolder.exists()&&mainfolder.isDirectory()) || mainfolder.mkdirs()) {//建立文件夹
					opt.rootPath=path;
					further_loading(savedInstanceState);

				}else {showT("未知错误：SD卡:文件夹建立失败");finish();}
			})
			.setNegativeButton(R.string.no, (dialog, which) -> {
				if((subfolder.exists()&&subfolder.isDirectory()) || subfolder.mkdirs()) {//建立文件夹
					showT("配置存储在 标准 缓存目录");
					opt.rootPath=getExternalFilesDir("").getAbsolutePath();
					further_loading(savedInstanceState);

				}else {showT("未知错误：内部存储:文件夹建立失败");finish();}
			}).setCancelable(false).show();
			
		}else//Folder /sdcard/PLOD already exists. nothing to ask for.
			further_loading(savedInstanceState);
	}
	
	protected void further_loading(Bundle savedInstanceState) {
		scanSettings();
	}
   
	Toast m_currentToast;
	TextView toastTv;
	View toastV;
	public DictPicker pickDictDialog;
	public void showX(int ResId,int len, Object...args) {
		showT(getResources().getString(ResId,args),len);
	}
	public void show(int ResId,Object...args) {
		showT(getResources().getString(ResId,args),Toast.LENGTH_SHORT);
	}
	public void showT(Object text)
	{
		showT(String.valueOf(text),Toast.LENGTH_LONG);
	}
	public void showT(String text,int len)
	{
		if(m_currentToast != null) {}//cancel个什么劲？？m_currentToast.cancel();
		else {
			toastV = getLayoutInflater().inflate(R.layout.toast,null);
			toastTv = ((TextView) toastV.findViewById(R.id.message));
			m_currentToast = new Toast(this);

			m_currentToast.setGravity(Gravity.BOTTOM, 0, 135);
			m_currentToast.setView(toastV);
		}
		m_currentToast.setDuration(len);
		//m_currentToast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
		toastTv.setText(text);
		m_currentToast.show();
	}





	void showTopSnack(ViewGroup parentView, Object messageVal, float alpha, int duration, int gravity) {
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
		}else{
			topsnack.removeCallbacks(snackRemover);
			topsnack.setAlpha(1);
		}
		NextSnackLength=duration<0?SHORT_DURATION_MS:duration;
		topsnack.getBackground().setAlpha((int) (alpha*255));
		if(messageVal instanceof Integer)
			topsnack.setText((int)messageVal);
		else
			topsnack.setText(String.valueOf(messageVal));
		topsnack.setGravity(gravity==-1?Gravity.CENTER:gravity);
		ViewGroup sp = (ViewGroup) topsnack.getParent();
		if(sp!=null && sp!=parentView) sp.removeView(topsnack);
		if(topsnack.getParent()!=parentView) {
			topsnack.setVisibility(View.INVISIBLE);
			parentView.addView(topsnack);
			topsnack.getLayoutParams().height=-2;
			topsnack.post(snackWorker);
		}else{
			topsnack.removeCallbacks(snackWorker);
			snackWorker.run();
		}
	}

	protected void cancleSnack() {
		if(topsnack!=null && topsnack.getParent()!=null) {
			if(objectAnimator!=null){
				objectAnimator.removeAllListeners();
				objectAnimator.cancel();
			}
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

	protected void checkMargin() {
		//File additional_config = new File(opt.pathToMain()+"appsettings.txt");
		File additional_config = new File(Environment.getExternalStorageDirectory(),"PLOD/appsettings.txt");
		if(additional_config.exists()) {
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
										ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
										DockerMarginL = lp.leftMargin = Integer.valueOf(arr[2]);
										DockerMarginR = lp.rightMargin = Integer.valueOf(arr[3]);
										DockerMarginT = lp.topMargin = Integer.valueOf(arr[0]);
										DockerMarginB = lp.bottomMargin = Integer.valueOf(arr[1]);
										if(root!=null){
											root.setTag(false);
											root.setLayoutParams(lp);
										}
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
	}
}





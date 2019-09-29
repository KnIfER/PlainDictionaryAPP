package com.knziha.plod.PlainDict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import com.androidadvance.topsnackbar.TSnackbar;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.settings.SettingsActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
	public TSnackbar snack;
    
	public Dialog d;
	public View dv;
	Configuration mConfiguration;
	boolean isDarkStamp;

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




	protected void cancleSnack() {
		if(snack!=null) {
			if(objectAnimator!=null) objectAnimator.cancel();
			objectAnimator = ObjectAnimator.ofFloat(snack.getView(),"alpha",snack.getView().getAlpha(),0f);
            objectAnimator.setDuration(240);
            objectAnimator.start();
		}
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





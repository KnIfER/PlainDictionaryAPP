package com.knziha.plod.PlainDict;

import java.io.File;
import java.io.IOException;

import com.androidadvance.topsnackbar.TSnackbar;
import com.knziha.plod.PlainDict.R;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
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
	protected String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};


	//public dictionary_App_Options opt;
    //public List<mdict> md = new ArrayList<mdict>();//Collections.synchronizedList(new ArrayList<mdict>());

	public PDICMainAppOptions opt;
	public DisplayMetrics dm;
	public LayoutInflater inflater;
	public InputMethodManager imm;
	protected int trialCount=-1;
	
	public long lastClickTime=0;
	protected boolean systemIntialized=false;

	protected int MainBackground;
	public int AppBlack;
	public int AppWhite;
    public float ColorMultiplier_Wiget=0.9f;
    public float ColorMultiplier_Web=1;
    public float ColorMultiplier_Web2=1;

	public ViewGroup contentview;
	protected ViewGroup root;
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
	
   @Override
    protected void onCreate(Bundle savedInstanceState) {
       opt = new PDICMainAppOptions(this);
       opt.dm = dm = new DisplayMetrics();
	   getWindowManager().getDefaultDisplay().getMetrics(dm);
	   super.onCreate(savedInstanceState);
       opt.getFirstFlag();
       opt.getSecondFlag();
	   inflater=getLayoutInflater();
       imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

	   AppBlack=opt.getInDarkMode()?Color.WHITE:Color.BLACK;
	   AppWhite=opt.getInDarkMode()?Color.BLACK:Color.WHITE;
       
   }

   
	@Override
    protected void onResume() {
		 super.onResume();
	}
	
	@Override
	public File getDatabasePath(String pathname){
		return new File(pathname);
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
   
	// 提示用户该请求权限的弹出框
	protected void showDialogTipUserRequestPermission() {
		new AlertDialog.Builder(this)
				.setTitle("请赋予存储权限")
				.setMessage("如您不赋予，则无法加载词典。(但是可以进入试用模式体验)")
				.setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestPermissions(permissions, 321);
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						File trialPath = getExternalFilesDir("Trial");
						int trialCount=-1;
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
						if(b1 || trialPath.mkdirs()) {
							opt.rootPath=trialPath.getAbsolutePath();
							try {
								if(trialCount!=-1) {
									new File(trialPath,String.valueOf(trialCount))
										.renameTo(new File(trialPath,String.valueOf(++trialCount)));
								}else
									new File(trialPath,String.valueOf(trialCount=0)).createNewFile();
							} catch (IOException e) {}
							pre_further_loading(null);
							showT("试用中…… #"+trialCount);
						}
						else {
							Toast.makeText(Toastable_Activity.this, "抱歉，无法在您的设备上使用", Toast.LENGTH_SHORT).show();
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
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if((mainfolder.exists()&&mainfolder.isDirectory()) || mainfolder.mkdirs()) {//建立文件夹
						opt.rootPath=path;
						further_loading(savedInstanceState);
						
					}else {showT("未知错误：SD卡:文件夹建立失败");finish();}
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if((subfolder.exists()&&subfolder.isDirectory()) || subfolder.mkdirs()) {//建立文件夹
						showT("配置存储在 标准 缓存目录");
						opt.rootPath=getExternalFilesDir("").getAbsolutePath();
						further_loading(savedInstanceState);
						
					}else {showT("未知错误：内部存储:文件夹建立失败");finish();}
				}
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


	
}





package com.knziha.plod.settings;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.CrashHandler;
import com.knziha.plod.PlainDict.Toastable_Activity;

import java.io.File;

public class SettingsActivity extends Toastable_Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
	  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
	  requestWindowFeature(Window.FEATURE_NO_TITLE);
	  Window win = getWindow();
	  if(opt.isFullScreen()){
		  win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				  WindowManager.LayoutParams.FLAG_FULLSCREEN);
	  }
	  win.getDecorView().setBackgroundColor(GlobalOptions.isDark? Color.BLACK:Color.WHITE);
	  root=win.getDecorView().findViewById(android.R.id.content);
	  checkMargin();
	  if(Build.VERSION.SDK_INT>=21) {
		  win.setStatusBarColor(CMN.MainBackground);
		  win.setNavigationBarColor(CMN.MainBackground);
	  }

	  File log=new File(CrashHandler.getInstance(opt).getLogFile());
	  File lock=new File(log.getParentFile(),"lock");
	  if(lock.exists()) lock.delete();

      this.getSupportFragmentManager().beginTransaction()
          .replace(android.R.id.content, new SettingsFragment())
          .commit();
  }
  
  
  
  

}

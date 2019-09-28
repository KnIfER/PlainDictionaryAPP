package com.knziha.plod.settings;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.Toastable_Activity;

public class SettingsActivity extends Toastable_Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
	  Window window = getWindow();

      if(Build.VERSION.SDK_INT>=21) {
	        window.setStatusBarColor(CMN.MainBackground);
	        window.setNavigationBarColor(CMN.MainBackground);  
      }
      
      this.getSupportFragmentManager().beginTransaction()
          .replace(android.R.id.content, new SettingsFragment())
          .commit();
      
  }
  
  
  
  

}

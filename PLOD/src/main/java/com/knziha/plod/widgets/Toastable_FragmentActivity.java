package com.knziha.plod.widgets;

import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.PlainDict.DictPicker;

import androidx.fragment.app.FragmentActivity;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by KnIfER on 2019.AppCompat
 */
public class Toastable_FragmentActivity extends FragmentActivity {

	//public dictionary_App_Options opt;
    //public List<mdict> md = new ArrayList<mdict>();//Collections.synchronizedList(new ArrayList<mdict>());
	public PDICMainAppOptions opt;
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
	public void showT(String text)
	{
		showT(text,Toast.LENGTH_LONG);
	}

	public void showT(String text,int len)
	{
		if(opt==null)
			opt=new PDICMainAppOptions(this);
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
			m_currentToast.setGravity(Gravity.BOTTOM, 0, 135);
			m_currentToast.setView(toastV);
		}
		if(toastV.getBackground() instanceof GradientDrawable){
			GradientDrawable drawable = (GradientDrawable) toastV.getBackground();
			drawable.setCornerRadius(PDICMainAppOptions.getToastRoundedCorner()?opt.dm.density*15:0);
			drawable.setColor(opt.getToastBackground());
		}
		m_currentToast.setDuration(len);
		toastTv.setText(text);
		toastTv.setTextColor(opt.getToastColor());
		m_currentToast.show();
	}
	
	
	
}





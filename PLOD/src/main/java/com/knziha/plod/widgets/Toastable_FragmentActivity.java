package com.knziha.plod.widgets;

import com.knziha.plod.PlainDict.R;
import com.knziha.plod.PlainDict.DictPicker;

import androidx.fragment.app.FragmentActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by KnIfER on 2019.AppCompat
 */
public class Toastable_FragmentActivity extends FragmentActivity {

	//public dictionary_App_Options opt;
    //public List<mdict> md = new ArrayList<mdict>();//Collections.synchronizedList(new ArrayList<mdict>());

    
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
	
	
	
}





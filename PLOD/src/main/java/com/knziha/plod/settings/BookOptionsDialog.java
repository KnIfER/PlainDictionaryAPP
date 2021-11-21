package com.knziha.plod.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.fragment.app.DialogFragment;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.Toastable_Activity;

public class BookOptionsDialog extends DialogFragment {
	public BookOptions bookOptions = new BookOptions();
	FrameLayout layout;
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (layout==null) {
			layout = new FrameLayout(inflater.getContext());
			layout.setId(android.R.id.content);
		}
		else if(layout.getParent()!=null) {
			((ViewGroup)layout.getParent()).removeView(layout);
		}
		getChildFragmentManager().beginTransaction()
				.add(android.R.id.content, bookOptions)
				.commit();
		return layout;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (getDialog()!=null) {
			Window win = getDialog().getWindow();
			if (win!=null) {
				win.setBackgroundDrawableResource(GlobalOptions.isDark? androidx.appcompat.R.drawable.popup_shadow_d: androidx.appcompat.R.drawable.popup_shadow_l);
				win.getDecorView().setPadding(0, 0, 0, 0);
				win.getDecorView().addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
					DisplayMetrics dm = getResources().getDisplayMetrics();
					WindowManager.LayoutParams params = win.getAttributes();
					int w = (int) (0.95*dm.widthPixels);
					int h = (int) (0.95*dm.heightPixels);
					if (h>w) {
						h=(int) (0.8*h);
						w = (int) (0.99*dm.widthPixels);
					}
					else w=(int) (0.85*w);
					params.width = w;
					params.height = h;
					win.setAttributes(params);
				});
				win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_FRAME, 0);
	}
	
	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		//todo 在此调试一下配置存放
		if (getActivity() instanceof Toastable_Activity) {
			MainActivityUIBase a=null;
			for (BookPresenter datum : bookOptions.data) {
				datum.checkFlag((Toastable_Activity) getActivity());
				if (a==null && datum.getIsManagerAgent()==0) {
					a=datum.a;
				}
			}
			if(a!=null){
				a.invalidAllPagers();
			}
		}
	}
}
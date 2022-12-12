package com.knziha.plod.settings;

import android.app.Dialog;
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
import androidx.appcompat.view.VU;
import androidx.fragment.app.DialogFragment;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.ViewUtils;

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
	
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setStyle(STYLE_NO_TITLE, 0);
//	}  // crash on sdk 21  :     requestFeature() must be called before adding content
	
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View v = ViewUtils.getNthChildNonNull(getDialog().getWindow().getDecorView(), 2);
		if (v != null && v.getId() == android.R.id.title) {
			VU.removeView(v);
		}
	}
	
	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		//todo 在此调试一下配置存放
		if (getActivity() instanceof Toastable_Activity) {
			MainActivityUIBase a=null;
			boolean set = false;
			for (BookPresenter datum : bookOptions.data) {
				if(datum.checkFlag((Toastable_Activity) getActivity()))
					set = true;
				if (a==null && datum.getIsManagerAgent()==0) {
					a=datum.a;
				}
			}
			if (getActivity() instanceof MainActivityUIBase) {
				a = (MainActivityUIBase) getActivity();
			}
			((Toastable_Activity)getActivity()).onBookOptionsSet(set);
		}
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		Dialog ret = super.onCreateDialog(savedInstanceState);
		if (getActivity() instanceof MainActivityUIBase)
			ViewUtils.ensureWindowType(ret, (MainActivityUIBase) getActivity(), this);
		
		Window win = ret.getWindow();
		if (win!=null) {
			win.setBackgroundDrawableResource(GlobalOptions.isDark? androidx.appcompat.R.drawable.popup_shadow_d: androidx.appcompat.R.drawable.popup_shadow_l);
			win.getDecorView().setPadding(0, 0, 0, 0);
			View.OnLayoutChangeListener layout = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
				DisplayMetrics dm = getResources().getDisplayMetrics();
				WindowManager.LayoutParams params = win.getAttributes();
				int w = (int) (0.95*dm.widthPixels);
				int h = (int) (0.95*dm.heightPixels);
				if (h>w) {
					h=(int) (0.8*h);
					w = -1;
				}
				else w=(int) (0.85*w);
				params.width = w;
				params.height = h;
				win.setAttributes(params);
			};
			win.getDecorView().addOnLayoutChangeListener(layout);
			layout.onLayoutChange(null, 0, 0, 0, 0, 0, 0, 0, 0);
			win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		}
		
		return ret;
	}
}
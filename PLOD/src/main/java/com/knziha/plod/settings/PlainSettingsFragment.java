package com.knziha.plod.settings;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.filepicker.settings.SettingsFragmentBase;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

public class PlainSettingsFragment extends SettingsFragmentBase implements Toolbar.OnMenuItemClickListener {
	@SuppressLint("ResourceType")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View ret = super.onCreateView(inflater, container, savedInstanceState);
		navBar.inflateMenu(R.xml.menu_search);
		navBar.setOnMenuItemClickListener(this);
		if (GlobalOptions.isDark) {
		} else {
			ActionMenuItemView iv = navBar.findViewById(R.id.search);
			iv.getIcon().mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
		}
		return ret;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		getSettingActivity().showSearchSettingsDlg();
		return false;
	}
	
	public SettingsActivity getSettingActivity() {
		return (SettingsActivity) getActivity();
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SettingsActivity a = getSettingActivity();
		if(!fh)
			handleFocus(a);
	}
	
	boolean fh;
	PreferenceGroupAdapter adapter;
	
	@Override
	protected void onBindPreferences() {
		adapter = (PreferenceGroupAdapter) mList.getAdapter();
	}
	
	public void handleFocus(SettingsActivity a) {
		fh = true;
		String pid = a.getIntent().getStringExtra("focus");
		//CMN.debug("pid::", pid);
		if (!TextUtils.isEmpty(pid)) {
			if(mList==null || adapter==null) return;
			final LinearLayoutManager lMan = ((LinearLayoutManager) mList.getLayoutManager());
			if(lMan==null) return;
			for (int i = 0; i < adapter.getItemCount(); i++) {
				if (pid.equals(adapter.getItem(i).getKey())) {
					//CMN.debug("pid::1::", i);
					int finalI = i;
					mList.post(() -> {
						mList.scrollToPosition(finalI);
						lMan.scrollToPositionWithOffset(finalI, mList.getHeight()/5);
						mList.postDelayed(() -> {
							int fvp = lMan.findFirstVisibleItemPosition();
							View child = mList.getChildAt(finalI - fvp);
							if (child!=null) {
								MotionEvent evt = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, -100, -100, 0);
								child.dispatchTouchEvent(evt);
								mList.postDelayed(() -> ViewUtils.preventDefaultTouchEvent(child, -100, -100), 800);
								evt.recycle();
							}
						}, 180);
					});
					break;
				}
			}
		}
	}
}

package com.knziha.plod.plaindict;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.PlainUI.InfiniteAdapter;
import com.knziha.plod.db.FFDB;

import java.util.ArrayList;

/**
 * Created by KnIfER on 2023
 */
public class InifiniteActivity extends Toastable_Activity implements View.OnClickListener {
	private String debugString;
	static ActivityManager.AppTask hiddenId;
	public final static int SingleTaskFlags = Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP;
	public static boolean launched;
	
	final ArrayList<RecyclerView> viewList = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_infinite);
		root = findViewById(R.id.root);
		//setTheme(R.style.AppTheme);
		toolbar = root.findViewById(R.id.toolbar);
		ViewGroup viewgroup = findViewById(R.id.viewpager);
		ProcessIntent(getIntent());
		//finish();
		launched=true;
		
		toolbar.setTitle("无限历史记录");
		
		View page = getLayoutInflater().inflate(R.layout.inifinite_viewpage, root, false);
		
		Toastable_Activity a = this;
		ListView tagList = page.findViewById(R.id.listview);
		RecyclerView lv = page.findViewById(R.id.recycler_view);
		lv.setLayoutManager(new LinearLayoutManager(a));
		lv.setBackgroundColor(Color.BLACK);
		//RecyclerView.RecycledViewPool pool = viewList[i].getRecycledViewPool();
		//pool.setMaxRecycledViews(0,10);
		
		DividerItemDecoration divider = new DividerItemDecoration(a, LinearLayout.VERTICAL);
		Drawable drawable = a.mResource.getDrawable(R.drawable.divider4);
		drawable.setAlpha(50);
		divider.setDrawable(drawable);
		lv.addItemDecoration(divider);
		
		//取消更新item时闪烁
		RecyclerView.ItemAnimator anima = lv.getItemAnimator();
		if(anima instanceof DefaultItemAnimator)
			((DefaultItemAnimator)anima).setSupportsChangeAnimations(false);
		anima.setChangeDuration(0);
		anima.setAddDuration(0);
		anima.setMoveDuration(0);
		anima.setRemoveDuration(0);
		
		InfiniteAdapter ada = new InfiniteAdapter(this, FFDB.getInstance(this).getDB(), "bilibili_history", null, lv);
		ada.setAdapter(lv, tagList);
		
		viewList.add(lv);
		
		viewgroup.addView(page);
		
		if (GlobalOptions.isSystemDark || GlobalOptions.isDark) {
			setStatusBarColor(getWindow(), Color.BLACK);
			toolbar.setBackgroundColor(Color.BLACK);
		} else {
			setStatusBarColor(getWindow(), 0xfffa87a9);
		}
		
		toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
		toolbar.setNavigationOnClickListener(this);
		
	}

	public void ProcessIntent(Intent thisIntent) {
	
	}
	
	@Override
	public void onClick(View v) {
		moveTaskToBack(false);
	}
}
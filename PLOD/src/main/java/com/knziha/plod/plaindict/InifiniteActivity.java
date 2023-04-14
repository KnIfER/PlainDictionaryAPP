package com.knziha.plod.plaindict;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

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
public class InifiniteActivity extends Toastable_Activity {
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
		//setTheme(R.style.AppTheme);
		toolbar = findViewById(R.id.toolbar);
		ViewGroup viewgroup = findViewById(R.id.viewpager);
		ProcessIntent(getIntent());
		//finish();
		launched=true;
		
		toolbar.setTitle("无限历史记录");
		
		
		Toastable_Activity a = this;
		RecyclerView lv = new RecyclerView(new ContextThemeWrapper(a, R.style.RecyclerViewStyle));
		lv.setLayoutManager(new LinearLayoutManager(a));
		//RecyclerView.RecycledViewPool pool = viewList[i].getRecycledViewPool();
		//pool.setMaxRecycledViews(0,10);
		
		DividerItemDecoration divider = new DividerItemDecoration(a, LinearLayout.VERTICAL);
		divider.setDrawable(a.mResource.getDrawable(R.drawable.divider4));
		lv.addItemDecoration(divider);
		
		//取消更新item时闪烁
		RecyclerView.ItemAnimator anima = lv.getItemAnimator();
		if(anima instanceof DefaultItemAnimator)
			((DefaultItemAnimator)anima).setSupportsChangeAnimations(false);
		anima.setChangeDuration(0);
		anima.setAddDuration(0);
		anima.setMoveDuration(0);
		anima.setRemoveDuration(0);
		
		lv.setAdapter(new InfiniteAdapter(this, FFDB.getInstance(this).getDB(), "bilibili_history", null, lv));
		
		viewList.add(lv);
		
		viewgroup.addView(lv);
	}

	public void ProcessIntent(Intent thisIntent) {
	
	}
}
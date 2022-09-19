package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.NiceDrawerLayout;

public class BookNotes extends PlainAppPanel implements DrawerLayout.DrawerListener{
	MainActivityUIBase a;
	NiceDrawerLayout drawer;
	int drawerStat;
	boolean drawerOpen;
	ListView list;
	View bar;
	Toolbar toolbar;
	
	public BookNotes(MainActivityUIBase a) {
		super(a, true);
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		this.bAnimate = false;
		setShowInDialog();
	}
	
	@Override
	public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
		//CMN.debug("onDrawerSlide::", slideOffset); // 0.085
		if (drawerOpen) {
			if (slideOffset < 0.01 && drawerStat==DrawerLayout.STATE_SETTLING) {
				dismissImmediate();
			}
		} else if (slideOffset > 0.01 && drawerStat==DrawerLayout.STATE_SETTLING) {
			drawerOpen = true;
		}
	}
	
	@Override
	public void onDrawerOpened(@NonNull View drawerView) {
		drawerOpen = true;
	}
	
	@Override
	public void onDrawerClosed(@NonNull View drawerView) {
		drawerOpen = false;
		dismissImmediate();
	}
	
	@Override
	public void onDrawerStateChanged(int newState) {
		drawerStat = newState;
	}
	
	@SuppressLint("ResourceType")
	@Override
	public void init(Context context, ViewGroup root) {
		if(a==null) {
			a=(MainActivityUIBase) context;
			mBackgroundColor = 0;
			setShowInDialog();
		}
		if (settingsLayout==null) {
			drawer = (NiceDrawerLayout) a.getLayoutInflater().inflate(R.layout.book_notes_view, a.root, false);
			drawer.addDrawerListener(this);
			list = drawer.findViewById(R.id.lst);
			list.setAdapter(getAnnotationAdapter(false, a.currentDictionary));
			bar = drawer.findViewById(R.id.bar);
			toolbar = drawer.findViewById(R.id.toolbar);
			toolbar.setNavigationIcon(com.knziha.filepicker.R.drawable.abc_ic_ab_back_material);
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					drawer.close();
				}
			});
			settingsLayout = drawer;
		}
	}
	
	private AnnotAdapter mAnnotAdapter;
	public ListAdapter getAnnotationAdapter(boolean darkMode, BookPresenter invoker) {
		if(mAnnotAdapter==null)
			mAnnotAdapter=new AnnotAdapter(a,R.layout.drawer_list_item,R.id.text1,invoker, a.prepareHistoryCon().getDB(),0);
		else
			mAnnotAdapter.refresh(invoker, a.prepareHistoryCon().getDB());
		mAnnotAdapter.darkMode=darkMode;
		return mAnnotAdapter;
	}
	
	
	@Override
	protected void onShow() {
		//drawer.open();
		//drawer.openDrawer(GravityCompat.START);
		drawerOpen = false;
		drawer.post(() -> drawer.open());
		refresh();
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		drawerOpen = false;
		drawer.closeDrawer(GravityCompat.START, false);
	}
	
	@Override
	public void refresh() {
		if (MainAppBackground != a.MainAppBackground) {
			// 刷新颜色变化（黑暗模式或者设置更改）
			toolbar.setTitleTextColor(a.AppWhite);
			MainAppBackground = a.MainAppBackground;
			bar.setBackgroundColor(MainAppBackground);
		}
	}
}
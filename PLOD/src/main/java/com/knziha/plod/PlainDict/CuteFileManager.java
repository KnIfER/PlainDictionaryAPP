package com.knziha.plod.PlainDict;

import java.io.File;
import java.util.ArrayList;

import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.widgets.NoScrollViewPager;

import android.app.AlertDialog;
//import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.app.FragmentManager;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class CuteFileManager extends Toastable_Activity implements OnClickListener, OnLongClickListener, OnMenuItemClickListener{


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(mConfiguration.orientation!=newConfig.orientation) {
			mConfiguration = getResources().getConfiguration();
		}
	}

	private final Handler mHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {}
		}};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//showT("asdasd"+event);
		switch (keyCode) {}
		return super.onKeyDown(keyCode, event);
	}



	Configuration mConfiguration;
	private View cb1;


	ArrayList<FilePickerDialog> viewList;
	NoScrollViewPager viewPager;


	ActionBarDrawerToggle mDrawerToggle;
	DrawerLayout mDrawerLayout;

	String lastPastedContent = "";
	public Drawer drawerFragment;
	private int CurrentViewPage;
	private PagerAdapter PagerAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mConfiguration = getResources().getConfiguration();
		super.onCreate(null);

		setTheme(R.style.PlainAppTheme);
		//disable keyboard auto-coming up feature
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//getWindow().setNavigationBarColor(Color.BLACK);

		setContentView(R.layout.main_cute);

		setStatusBarColor();


		checkLaunch(savedInstanceState);

	}



	protected void further_loading(final Bundle savedInstanceState) {
		super.further_loading(savedInstanceState);

		contentview = findViewById(R.id.webcontent);
		viewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
		findViewById(R.id.toolbar).setVisibility(View.GONE);

		final DialogProperties properties = new DialogProperties();
		properties.selection_mode = DialogConfigs.MULTI_MODE;
		properties.selection_type = DialogConfigs.FILE_SELECT;
		properties.root = new File("/");
		properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
		//!!!
		properties.offset = new File(
				opt.lastMdlibPath
		);
		properties.opt_dir=new File(opt.pathToDatabases()+"favorite_dirs/");
		properties.opt_dir.mkdirs();
		//properties.extensions = new String[] {"mdx"};
		properties.title_id = 0;


		viewList = new ArrayList<FilePickerDialog>();
		FilePickerDialog dialog = new FilePickerDialog(this, properties);
		dialog.bDontAttach=true;
		dialog.init();
		dialog.getView().findViewById(R.id.footer).setVisibility(View.GONE);
		dialog.title.setText("高维宇宙异度空间CeShi");
		viewList.add(dialog);

		findViewById(R.id.browser_widget6).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FilePickerDialog dialog = new FilePickerDialog(CuteFileManager.this, properties);
				dialog.bDontAttach=true;
				dialog.init();
				dialog.getView().findViewById(R.id.footer).setVisibility(View.GONE);
				dialog.title.setText("高维宇宙异度空间CeShi"+viewList.size());
				viewList.add(dialog);
				PagerAdapter.notifyDataSetChanged();
				viewPager.setCurrentItem(viewList.size()-1);
			}});



		PagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			@Override
			public int getCount() {
				return viewList.size();
			}
			@Override
			public void destroyItem(ViewGroup container, int position,
									Object object) {
				//showT("destroyItem");
				container.removeView(viewList.get(position).getView());
			}
			@Override
			public int getItemPosition(Object object) {
				return super.getItemPosition(object);
			}
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(viewList.get(position).getView());
				return viewList.get(position).getView();
			}
		};
		viewPager.setAdapter(PagerAdapter);
		viewPager.setCurrentItem(CurrentViewPage = 0);
		viewPager.setNoScroll(false);


		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
		mDrawerToggle.syncState();// 添加按钮

	}





	TextWatcher tw1=new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

		public void beforeTextChanged(CharSequence s, int start, int count,  int after) {}

		public void afterTextChanged(Editable s) {
			//if (s.length() == 0) ivDeleteText.setVisibility(View.GONE);
			//else ivDeleteText.setVisibility(View.VISIBLE);
			if (s.length() != 0) ivDeleteText.setVisibility(View.VISIBLE);
		}
	};


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);

	}



	Drawable full_search_drawable;
	Drawable fuzzy_search_drawable;
	Drawable full_search_drawable_pressed;
	Drawable fuzzy_search_drawable_pressed;


	private void setStatusBarColor(){
		Window window = getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		if(Build.VERSION.SDK_INT>=21) {
			window.setStatusBarColor(Color.TRANSPARENT);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}

	@Override
	protected void onDestroy(){
		//CMN.show("onDestroy");
		dumpSettiings();
		for(FilePickerDialog dia:viewList) {
			dia.cancel();
		}
		viewList.clear();
		viewPager.removeAllViews();
		super.onDestroy();
		System.gc();
	}

	@Override
	protected void scanSettings(){
		super.scanSettings();
		CMN.MainBackground = MainBackground = opt.getMainBackground();

		opt.getLastMdlibPath();
		if(opt.lastMdlibPath==null || !new File(opt.lastMdlibPath).exists()) {
			opt.lastMdlibPath = opt.pathToMainFolder()+"mdicts";
			new File(opt.lastMdlibPath).mkdirs();
		}

	}

	private void dumpSettiings(){}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.menu, menu);
		showT("onCreateOptionsMenu");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.toolbar_action1:
				// do something
				return true;
			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//removeBlack();
	}



	@Override
	protected void onResume() {
		super.onResume();
		if(false) {
			ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData data = cm.getPrimaryClip();
			if(data!=null) {
				ClipData.Item item = data.getItemAt(0);
				String content = item.getText().toString();
				if(!lastPastedContent.equals(content)) {
					drawerFragment.etAdditional.setText(lastPastedContent = content);
				}
			}
		}
		if(systemIntialized) {
			if(CMN.MainBackground != MainBackground) {
				MainBackground=CMN.MainBackground;
				refreshUIColors();
			}
		}
	}

	void refreshUIColors() {
		boolean isHalo=AppWhite==Color.WHITE;
		int filteredColor = isHalo?MainBackground:ColorUtils.blendARGB(MainBackground, Color.BLACK, ColorMultiplier_Wiget);//CU.MColor(MainBackground,ColorMultiplier);
		viewPager.setBackgroundColor(AppWhite);
		root.setBackgroundColor(filteredColor);


		if(!isHalo) {
			dialog_.setBackgroundResource(R.drawable.popup_shadow_l);
			dialog_.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
			MarginLayoutParams lp = (MarginLayoutParams) cb1.getLayoutParams();
			lp.topMargin=(int) (13*dm.density);
			cb1.setLayoutParams(lp);
		}else {
			dialog_.setBackgroundResource(R.drawable.popup_background3);
			MarginLayoutParams lp = (MarginLayoutParams) cb1.getLayoutParams();
			lp.topMargin=(int) (10*dm.density);
			cb1.setLayoutParams(lp);
		}

	}

	static class viewHolder{
		private TextView title;
		private TextView subtitle;
	}



	@Override
	public void onClick(View v) {}

	Toolbar searchbar;





	@Override
	public boolean onLongClick(View v) {
		return systemIntialized;
	}


	@Override
	public boolean onMenuItemClick(MenuItem m) {
		boolean longclick=false;
		if(longclick) return false;
		return systemIntialized;
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent duco) {
		super.onActivityResult(requestCode, resultCode, duco);
		switch (requestCode) {
			case 123:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					int i = checkSelfPermission(permissions[0]);
					if (i != PackageManager.PERMISSION_GRANTED) {
						showDialogTipUserGoToAppSettting();
					} else {
						if (d != null && d.isShowing()) {
							d.dismiss();
							d = null;
						}
						Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
						pre_further_loading(null);
					}
				}
				break;
		}
		//TODO seal it
		//CMN.a = null;
	}


	// 提示用户 去设置界面 手动开启权限
	private void showDialogTipUserGoToAppSettting() {
		//动态申请不成功，转为手动开启权限
		d = new AlertDialog.Builder(this)
				.setTitle("存储权限不可用")
				.setMessage("请在-应用设置-权限-中，允许歌词助手使用存储权限来保存用户数据")
				.setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 跳转到应用设置界面
						Intent intent = new Intent();

						intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						Uri uri = Uri.fromParts("package", getPackageName(), null);
						intent.setData(uri);

						startActivityForResult(intent, 123);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setCancelable(false).show();

	}
	//权限申请回调
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == 321) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					// 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
					boolean b = shouldShowRequestPermissionRationale(permissions[0]);
					if (!b) {
						// 用户还是想用我的 APP 的
						// 提示用户去应用设置界面手动开启权限
						showDialogTipUserGoToAppSettting();
					} else
						finish();
				} else {
					Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
					pre_further_loading(null);
				}
			}
		}
	}







}





package com.knziha.plod.dictionarymanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.ActionMenuView.LayoutParams;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.PlainDict.AgentApplication;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.PlaceHolder;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedWriter;
import com.knziha.plod.dictionarymanager.files.mAssetFile;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_manageable;
import com.knziha.plod.dictionarymodels.mdict_transient;
import com.knziha.plod.dictionarymodels.mdict_prempter;
import com.knziha.plod.widgets.Toastable_FragmentActivity;
import com.knziha.rbtree.RashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class dict_manager_activity extends Toastable_FragmentActivity implements OnMenuItemClickListener
{
	HashMap<String,mdict_transient> mdict_cache = new HashMap<>();
	Intent intent = new Intent();
	private PopupWindow mPopup;
	public ArrayList<PlaceHolder> slots;
	private ArrayList<Fragment> fragments;

	public interface transferRunnable{
		boolean transfer(File to);
		void afterTransfer();
	}

	private boolean bIsNoNeedToTakeAfterPW=Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1;
    private ViewGroup toastmaker;
    private Toolbar toolbar;
    String dictQueryWord;
    boolean isSearching;
    private SearchView searchView;
    protected Menu toolbarmenu;
    dict_manager_main f1;
    dict_manager_modules f2;
    dict_Manager_folderlike f3;
	dict_manager_websites f4;
    ViewPager viewPager;  //对应的viewPager
    TabLayout mTabLayout;
	LayoutInflater inflater;

	public ArrayList<mdict_transient> mdmng;
    public HashSet<String> mdlibsCon;
	protected int CurrentPage;
	//MainActivity a;
	private boolean isDirty=false;

	@Override
	public void onBackPressed() {
		if(mPopup!=null){
			mPopup.dismiss();
			mPopup=null;
			return;
		}

		int item = viewPager.getCurrentItem();
		if(item<fragments.size() && fragments.get(item) instanceof dict_manager_base.SelectableFragment){
			if(((dict_manager_base.SelectableFragment)fragments.get(item)).exitSelectionMode()){
				return;
			}
		}

		if(f1.mDslv!=null) {
			f1.mDslv.noDraw=true;
			f1.refreshDicts(f1.mDslv.bUnfinished=false);
		}

		checkAll();
		//CMN.Log("terminating...", intent, intent.getBooleanExtra("changed", false));

		super.onBackPressed();
	}

	private void checkAll() {
		AgentApplication app = ((AgentApplication)getApplication());
		if(f1.isDirty) {
			intent.putExtra("result", true);
			int size = mdmng.size();
			boolean identical = size==slots.size();
			int i;
			for (i = 0; i < size; i++) {
				mdict_transient mmTmp = mdmng.get(i);
				mmTmp.mPhI.lineNumber=i;
				if(identical){
					if(!mmTmp.equalsToPlaceHolder(slots.get(i)))
						identical=false;
				}
			}
			if(identical){
				CMN.Log("一成不变");
			} else {
				intent.putExtra("changed", true);
				slots.clear();
				for (mdict_manageable mmTmp:mdmng) {
					slots.add(((mdict_transient)mmTmp).mPhI);
				}
				try {
					File def = new File(getExternalFilesDir(null), "default.txt");
					ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(def), app.get4kCharBuff(), 4096);
					String parent = new File(opt.lastMdlibPath).getAbsolutePath() + "/";
					output.write("[:S]");
					output.write(Integer.toString(mdmng.size() - f1.rejector.size()));
					output.write("\n");
					for (mdict_manageable mmTmp : mdmng) {
						writeForOneLine(output, mmTmp, parent);
					}
					output.flush();
					output.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			f1.isDirty=false;
		}

		if(f3.isDirty) {
			try {
				mFile rec = new mFile(opt.pathToMainFolder().append("CONFIG/mdlibs.txt").toString());
				ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(rec), app.get4kCharBuff(), 4096);
				String parent = new File(opt.lastMdlibPath).getAbsolutePath()+"/";
				for(mFile mdTmp:f3.data.getList()) {
					if(mdTmp.getClass()==mAssetFile.class) continue;
					if(mdTmp.isDirectory()) continue;
					String name = mdTmp.getPath();
					if(name.startsWith(parent))
						name = name.substring(parent.length());
					output.write(name);
					output.write("\n");
				}
				output.flush();
				output.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			f3.isDirty=false;
		}

		if(f2.isDirty) {
			intent.putExtra("result2", true);
			File def1 = new File(opt.pathToMainFolder().append("CONFIG/AllModuleSets.txt").toString());      //!!!原配
			try {
				ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(def1), app.get4kCharBuff(), 4096);
				for(int i=0;i<f2.adapter.getCount();i++) {
					String fn = f2.adapter.getItem(i);
					output.write(fn);
					output.write("\n");
				}
				output.flush();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			f2.isDirty=false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		checkAll();
	}

	private void writeForOneLine(Writer out, mdict_manageable mmTmp, String parent) throws IOException {
		String name = mmTmp.getPath();
		if(name.startsWith(parent))
			name = name.substring(parent.length());
		int tmpIsFlag = mmTmp.getTmpIsFlag();
		if(tmpIsFlag!=0)
			out.write("[:");
		int tif = mmTmp.getTmpIsFlag();
		if(PDICMainAppOptions.getTmpIsFiler(tif))
			out.write("F");
		else if(PDICMainAppOptions.getTmpIsAudior(tif))
			out.write("A");
		if(PDICMainAppOptions.getTmpIsClicker(tif))
			out.write(":C");
		if(PDICMainAppOptions.getTmpIsCollapsed(tif))
			out.write(":Z");
		if(PDICMainAppOptions.getTmpIsHidden(tif))
			out.write(":H");
		if(tmpIsFlag!=0)
			out.write("]");
		out.write(name);
		out.write("\n");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(null);
		AgentApplication agent = ((AgentApplication)getApplication());
		opt=agent.opt;
		slots = agent.slots;
		mdlibsCon=agent.mdlibsCon;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dict_manager_main);
		Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  
                        
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        if(Build.VERSION.SDK_INT>=21) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.parseColor("#8f8f8f"));
	        window.setNavigationBarColor(Color.parseColor("#8f8f8f"));  
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			View decorView = window.getDecorView();
			int vis = decorView.getSystemUiVisibility();
			if (false) {
				vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			} else {
				vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			}
			decorView.setSystemUiVisibility(vis);
		}
    	//a = ((MainActivity)CMN.a);
    	
		//TypedValue tval = new TypedValue();
		//if (a.getTheme().resolveAttribute(android.R.attr.actionBarSize, tval, true))
		//	CMN.actionBarHeight = TypedValue.complexToDimensionPixelSize(tval.data,getResources().getDisplayMetrics());
		
        getWindowManager().getDefaultDisplay().getMetrics(opt.dm);

        findViewById(R.id.drawer_layout).setBackgroundColor(GlobalOptions.isDark?Color.BLACK: CMN.MainBackground);
        
        viewPager = findViewById(R.id.viewpager);
        mTabLayout = findViewById(R.id.mTabLayout);
		inflater=LayoutInflater.from(getApplicationContext());
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.dict_manager);

        
 		toolbarmenu = toolbar.getMenu();
 		
		fragments= new ArrayList<>();
		
	    String[] tabTitle = {getResources().getString(R.string.currentPlan,0),getResources().getString(R.string.allPlans), "网络词典", "全部词典"};
	    mFile.parentPath=opt.lastMdlibPath.toLowerCase();
	    
		f1 = new dict_manager_main();
		f2 = new dict_manager_modules();
		f4 = new dict_manager_websites();
		f3 = new dict_Manager_folderlike();
		f1.a=this;
		f2.a=this;
		f4.a=this;
		f3.a=this;
		fragments.add(f1);
		fragments.add(f2);
		fragments.add(f4);
		fragments.add(f3);

		f3.oes = new dict_Manager_folderlike.OnEnterSelectionListener() {
			public void onEnterSelection(){
				toolbarmenu.getItem(7).setVisible(true);
				toolbarmenu.getItem(8).setVisible(true);
				toolbarmenu.getItem(9).setVisible(true);
				toolbarmenu.getItem(10).setVisible(true);
				toolbarmenu.getItem(11).setVisible(true);
				toolbarmenu.getItem(12).setVisible(true);
				toolbarmenu.getItem(13).setVisible(false);
				toolbarmenu.getItem(14).setVisible(false);
				toolbarmenu.getItem(15).setVisible(false);
			}
			public int addIt(final File fn) {
				boolean found=false;
				for(int i=0;i<f1.adapter.getCount();i++) {
					if(f1.adapter.getItem(i).getPath().equals(fn.getAbsolutePath())) {
						if(f1.rejector.contains(fn.getAbsolutePath())) {
							f1.rejector.remove(fn.getAbsolutePath());
							f1.adapter.notifyDataSetChanged();
							return 1;
						}
						found=true;
						break;
					}
				}
				if(!found) {
					//show("adding new!"+fn.getAbsolutePath());
					f3.mDslv.post(() -> {
						mdict_transient mdTmp = new mdict_transient(fn.getPath(), opt, 0);
						PDICMainAppOptions.setTmpIsFiler(mdTmp, mdTmp.getIsDedicatedFilter());
						f1.adapter.add(mdTmp);
						f1.refreshSize();
						f1.adapter.notifyDataSetChanged();
						f1.isDirty=true;
					});
					return 1;
				}else
					return 0;
			};
		};
		FragAdapter adapterf = new FragAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(adapterf);
	    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout) {
	    	@Override
	    	public void onPageSelected(int page) {
				Fragment fI = fragments.get(page);
				viewPager.setOffscreenPageLimit(Math.max(viewPager.getOffscreenPageLimit(), Math.max(1+page, 1)));
	    		if(fI==f1) {
	    			toolbarmenu.getItem(0).setVisible(true);
	    			toolbarmenu.getItem(1).setVisible(true);
	    			toolbarmenu.getItem(2).setVisible(true);
	    			toolbarmenu.getItem(3).setVisible(true);
	    			toolbarmenu.getItem(4).setVisible(true);
	    			toolbarmenu.getItem(5).setVisible(true);
	    			toolbarmenu.getItem(6).setVisible(true);
	    			toolbarmenu.getItem(7).setVisible(false);
	    			toolbarmenu.getItem(8).setVisible(false);
	    			toolbarmenu.getItem(9).setVisible(false);
	    			toolbarmenu.getItem(10).setVisible(false);
	    			toolbarmenu.getItem(11).setVisible(false);
	    			toolbarmenu.getItem(12).setVisible(false);
	    			toolbarmenu.getItem(13).setVisible(false);
	    			toolbarmenu.getItem(14).setVisible(false);
					toolbarmenu.getItem(15).setVisible(false);
	    		}
	    		else if(fI==f2) {
	    			toolbarmenu.getItem(0).setVisible(true);
	    			toolbarmenu.getItem(1).setVisible(false);
	    			toolbarmenu.getItem(2).setVisible(false);
	    			toolbarmenu.getItem(3).setVisible(true);
	    			toolbarmenu.getItem(4).setVisible(false);
	    			toolbarmenu.getItem(5).setVisible(false);
	    			toolbarmenu.getItem(6).setVisible(false);
	    			toolbarmenu.getItem(7).setVisible(false);
	    			toolbarmenu.getItem(8).setVisible(false);
	    			toolbarmenu.getItem(9).setVisible(false);
	    			toolbarmenu.getItem(10).setVisible(false);
	    			toolbarmenu.getItem(11).setVisible(false);
	    			toolbarmenu.getItem(12).setVisible(false);
	    			toolbarmenu.getItem(13).setVisible(false);
	    			toolbarmenu.getItem(14).setVisible(false);
					toolbarmenu.getItem(15).setVisible(true);
	    		}
	    		else if(fI==f3){
	    			toolbarmenu.getItem(0).setVisible(false);
	    			toolbarmenu.getItem(1).setVisible(false);
	    			toolbarmenu.getItem(2).setVisible(false);
	    			toolbarmenu.getItem(3).setVisible(false);
	    			toolbarmenu.getItem(4).setVisible(false);
	    			toolbarmenu.getItem(5).setVisible(false);
	    			toolbarmenu.getItem(6).setVisible(false);
	    			boolean setter=f3.SelectionMode;
	    			toolbarmenu.getItem(7).setVisible(setter);
	    			toolbarmenu.getItem(8).setVisible(setter);
	    			toolbarmenu.getItem(9).setVisible(setter);
	    			toolbarmenu.getItem(10).setVisible(setter);
	    			toolbarmenu.getItem(11).setVisible(setter);
	    			toolbarmenu.getItem(12).setVisible(setter);
	    			toolbarmenu.getItem(13).setVisible(!setter);
	    			toolbarmenu.getItem(14).setVisible(!setter);
					toolbarmenu.getItem(15).setVisible(false);
	    		}
				//mTabLayout.getTabAt(page).select();
	    		if(viewPager.getTag()==null)
	    			viewPager.setTag(false);
	    		else
	    			isDirty=true;
				super.onPageSelected(CurrentPage = page);
				opt.setDictManagerTap(CurrentPage);
	    	}
	    });

	    for (int i=0; i<tabTitle.length; i++)
            mTabLayout.addTab(mTabLayout.newTab().setText(tabTitle[i]));
		mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}
			@Override public void onTabUnselected(TabLayout.Tab tab) {}
			@Override public void onTabReselected(TabLayout.Tab tab) {}
        });
		
	    mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#2b4381"));
	    mTabLayout.setSelectedTabIndicatorHeight(3);
	    
	    viewPager.setCurrentItem(CurrentPage = opt.getDictManagerTap());
	    //if(CurrentPage==2)
		viewPager.setOffscreenPageLimit(Math.max(viewPager.getOffscreenPageLimit(), 1+CurrentPage));

	    toastmaker =  findViewById(R.id.toastmaker);
 
		if(GlobalOptions.isDark)
			toastmaker.setBackgroundColor(Color.BLACK);
			
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_flag_24dp);

        View vTmp = toolbar.getChildAt(toolbar.getChildCount()-1);
        if(vTmp!=null && vTmp.getClass()==AppCompatImageButton.class) {
			AppCompatImageButton NavigationIcon = (AppCompatImageButton) vTmp;
			NavigationIcon.setOnClickListener(v -> {
				View vTmp1 = LayoutInflater.from(dict_manager_activity.this).inflate(R.layout.simple_add_menu,null);
				mPopup=new PopupWindow(vTmp1, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
				vTmp1.setOnClickListener(v1 -> {
					mPopup.dismiss(); mPopup=null;
					onBackPressed();
					finish();
				});
				TextView tv = vTmp1.findViewById(R.id.text1);
				tv.setText(R.string.exit);
				tv.setTextColor(Color.WHITE);
				mPopup.setHeight(v.getHeight()*2/3);
				mPopup.setWidth(v.getWidth());
				if(bIsNoNeedToTakeAfterPW) {
					//mPopup.setTouchModal(true);
					mPopup.setFocusable(true);
					mPopup.setOutsideTouchable(false);
				}else{
					mPopup.setFocusable(false);
					mPopup.setOutsideTouchable(false);
				}
				mPopup.showAsDropDown(v, 0, -v.getHeight()*5/6, Gravity.TOP|Gravity.START);
				mPopup.update(v, 0, -v.getHeight()*5/6, -1, -1);
			});
        }
        
        toolbar.setTitle(R.string.manager);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

 		MenuItem searchItem = toolbarmenu.getItem(16);
 		searchItem.setShowAsAction(2);
 		searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
 		//searchView.setIconified(true);//设置搜索框直接展开显示。左侧有放大镜(在搜索框中) 右侧有叉叉 可以关闭搜索框
 		//searchView.onActionViewExpanded();// 当展开无输入内容的时候，没有关闭的图标
 		//searchView.setIconifiedByDefault(true);//默认为true在框内，设置false则在框外
 		searchView.setSubmitButtonEnabled(false);//显示提交按钮
        searchView.setOnSearchClickListener(v -> {
			 LayoutParams lp = new LayoutParams(getResources().getDisplayMetrics().widthPixels-200,-1);//500
			 lp.setMargins(0, 0, 50, 0);
			 searchView.setLayoutParams(lp);
			 });
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
	        @Override
	        public boolean onQueryTextSubmit(String query) {
	            dictQueryWord = query.trim().toLowerCase();
	            if(dictQueryWord.equals(""))
					isSearching = false;
	            else
	            	isSearching = true;
	            if(f1.adapter!=null)
	            	f1.adapter.notifyDataSetChanged();
	            if(f3.adapter!=null)
	            	f3.adapter.notifyDataSetChanged();

        		int cc=0;
	            switch(viewPager.getCurrentItem()){
	            	case 0:
	            		for(int i=0;i<f1.adapter.getCount();i++) {
	            			if(f1.adapter.getItem(i).getName().toLowerCase().contains(dictQueryWord))
	            				cc++;
	            		}
            		break;
	            	case 2:
	            		for(int i=0;i<f3.adapter.getCount();i++) {
	            			if(f3.adapter.getItem(i).getName().toLowerCase().contains(dictQueryWord))
	            				cc++;
	            		}
            		break;
	            }
//xxx
        		//if(cc>0)
        		//	TSnackbar.makeraw(toastmaker  , getResources().getString(R.string.fc,cc),TSnackbar.LENGTH_SHORT).show();
        		//else
        		//	TSnackbar.makeraw(toastmaker  , getResources().getString(R.string.fn) ,TSnackbar.LENGTH_SHORT).show();



	            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        	    return true;
	        }

	        @Override
	        public boolean onQueryTextChange(String newText) {
	            return true;
	        }
	    });
		
		searchView.setOnCloseListener(() -> {
			 LayoutParams lp = new LayoutParams(-2,-1);
			 searchView.setLayoutParams(lp);
			isSearching = false;
			if(f1.adapter!=null)
				f1.adapter.notifyDataSetChanged();
			if(f3.adapter!=null)
				f3.adapter.notifyDataSetChanged();
			return false;
		});
		try {//设置字体颜色 隐藏搜索框内放大镜图标
			EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
			searchEditText.setTextColor(Color.WHITE);
			
		    Field mDrawable = SearchView.class.getDeclaredField("mSearchHintIcon");
		    mDrawable.setAccessible(true);
		    Drawable drawable =  (Drawable)mDrawable.get(searchView);
		    drawable.setAlpha(0);
		    
		    //SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
		    //searchAutoComplete.styl
		    //searchAutoComplete.setEnabled(false);
		} catch (Exception e) {
		    e.printStackTrace();
		}
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  
                        
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);  
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(Build.VERSION.SDK_INT>=21) {
	        window.setStatusBarColor(Color.TRANSPARENT);  
	        window.setNavigationBarColor(Color.BLACK);  
        }
		setResult(RESULT_OK, intent);
//		viewPager.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				CMN.Log(viewPager.getChildCount(), viewPager.getOffscreenPageLimit());
//			}
//		}, 1000);

	}
	//onCreate结束
	
	

	
	
	
	protected void showRenameDialog(final String lastPlanName,final transferRunnable tr) {//哈哈这么长的代码。。。
		View dialog = getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
        final ListView lv = dialog.findViewById(R.id.lv);
        final EditText et = dialog.findViewById(R.id.et);
        ImageView iv = dialog.findViewById(R.id.confirm);
        File fSearchFile = new File(opt.pathToMainFolder().append("CONFIG/").append(lastPlanName).append(".set").toString());//查找旧plan
        final String fSearch = lastPlanName+".set";//查找旧plan
        final myCpr<Boolean,Boolean> args = new myCpr<>(false,false);
        File[] sets = new File(opt.pathToMainFolder().append("CONFIG").toString()).listFiles(pathname -> {
			String name = pathname.getName();
			if(name.endsWith(".set")) {
				if(!args.value)
				if(fSearch.equals(pathname.getName())) {
					args.value=true;
					return false;
				}
				return true;
			}
			return false;
		});
        
        final ArrayList<File> setsArr = new ArrayList<>(Arrays.asList(sets));
        
        if(args.value)
        	setsArr.add(0,fSearchFile);//让它出现在第一项
        else
            et.setText(lastPlanName);//否则码上去~

        for(int i=0;i<setsArr.size();i++) {
        	File fi = setsArr.get(i);
        	if(fSearch.equals(fi)) {
        		break;
        	}
		}
        
        lv.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() {
				return setsArr.size();
			}

			@Override
			public Object getItem(int position) {
				return setsArr.get(position);
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}
			OnClickListener myClicker = new OnClickListener() {
				@Override
				public void onClick(View v) {
					int id = v.getId();
					if(id==R.id.itemview) {//it is a name-fetcher
						myHolder holder = (myHolder)(v.getTag());
						if(!Selection.contains(holder.positition))
							et.setText(holder.tv.getText());
						return;
					}
					
					ViewGroup itemView = (ViewGroup) v.getParent();
					myHolder holder = ((myHolder)(itemView.getTag()));
					int position = holder.positition;
					switch(id) {
						case R.id.remove:
							if(Selection.contains(position)) {
								Selection.remove(position);
								String name = setsArr.get(position).getName();
								holder.tv.setText(name.substring(0,name.length()-4));
								decorateByViewHolder(holder);
							}
							else if(setsArr.get(position).delete()) {//删除
								Selection.remove(position);
								notifyFileRemoved(setsArr.get(position),null);
								setsArr.remove(position);
		                    	((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
							}
						break;
						case R.id.modify:
							if(Selection.contains(position)) {//重命名
								File oldf = setsArr.get(position);
								File newf = new File(oldf.getParentFile(),holder.tv.getText()+".set");
								if(!newf.equals(oldf) && newf.exists()) {
									show(R.string.renamefail);
								}else {
									oldf.renameTo(newf);
									notifyFileRemoved(oldf,newf);
									Selection.remove(position);
									decorateByViewHolder(holder);
								}
							}else {
								Selection.add(position);
								decorateByViewHolder(holder);
							}
						break;
					}
				}

				
			};
			class myHolder{
				TextView tv;ImageView remove;ImageView modify;
				int positition;
				public myHolder(View v1, View v2, View v3) {
					tv=(TextView) v1;remove=(ImageView) v2;modify=(ImageView) v3;
				}
			}
			public HashSet<Integer> Selection = new HashSet<>();
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView==null)
					convertView =  getLayoutInflater().inflate(R.layout.list_item3, parent, false);
				myHolder holder;
				if(convertView.getTag()==null) {
					convertView.setTag(holder=new myHolder(convertView.findViewById(R.id.text),convertView.findViewById(R.id.remove),convertView.findViewById(R.id.modify)));
		            holder.modify.setOnClickListener(myClicker);
		            holder.remove.setOnClickListener(myClicker);
		            convertView.setOnClickListener(myClicker);
		            convertView.setClickable(true);
				}else
					holder = (myHolder) convertView.getTag();

	            holder.positition = position;
	            
				decorateByViewHolder(holder);
				
	            
	            String name = setsArr.get(position).getName();
	            holder.tv.setText(name.substring(0,name.length()-4));
				return convertView;
			}
			ColorStateList filter_blue = new ColorStateList(new int[][]{new int[0]}, new int[]{0xff0000ff});
			ColorStateList filter_red = new ColorStateList(new int[][]{new int[0]}, new int[]{0xffff0000});
			private void decorateByViewHolder(myHolder holder) {
				if(args.key)
	            	holder.remove.setVisibility(View.VISIBLE);
	            else
	            	holder.remove.setVisibility(View.GONE);
				//holder.tv.setEnabled(true);
				if(Selection.contains(holder.positition)) {
					holder.tv.setEnabled(true);
					holder.remove.setVisibility(View.VISIBLE);
					//ViewCompat.setBackgroundTintList(holder.remove,filter_blue);

					holder.remove.setColorFilter(Color.BLUE);
					holder.modify.setColorFilter(Color.BLUE);
					//ViewCompat.setBackgroundTintList(holder.modify,filter_blue);
					//holder.modify.setBackground(a.getResources().getDrawable(R.drawable.ic_keyboard_return_black_24dp));
					holder.modify.setImageResource(R.drawable.ic_keyboard_return_black_24dp);
				}else {
					holder.tv.setEnabled(false);
					//ViewCompat.setBackgroundTintList(holder.remove,filter_red);
					//ViewCompat.setBackgroundTintList(holder.modify,null);
					holder.remove.setColorFilter(Color.RED);
					holder.modify.setColorFilter(Color.TRANSPARENT);
					//holder.modify.setBackground(a.getResources().getDrawable(R.drawable.ic_mode_edit_24dp));
					holder.modify.setImageResource(R.drawable.ic_mode_edit_24dp);
				}
			}});
        AlertDialog.Builder builder = new AlertDialog.Builder(dict_manager_activity.this);
        builder.setView(dialog);
        builder.setIcon(R.mipmap.ic_directory_parent);
        builder.setNeutralButton(R.string.delete,null);
        builder.setPositiveButton(R.string.cancel,null);
        final AlertDialog d = builder.create();
        
  
        d.setOnDismissListener(dialog1 -> tr.afterTransfer());
        d.show();
        iv.setOnClickListener(v -> {
			String newName = SU.trimStart(et.getText().toString());
			if(newName.equals("")) {
				show(R.string.renamefail0);
				return;
			}
			final File newf = new File(opt.pathToMainFolder().append("CONFIG/").append(newName).append(".set").toString());
			if(!fSearch.equals(newf) && newf.exists()) {//覆盖
				View dialog12 = getLayoutInflater().inflate(R.layout.dialog_about,null);
AlertDialog.Builder builder1 = new AlertDialog.Builder(dict_manager_activity.this);
TextView tvtv = dialog12.findViewById(R.id.title);
tvtv.setText(R.string.wenj_fugai);
tvtv.setPadding(50, 50, 0, 0);
builder1.setView(dialog12);
final AlertDialog dd = builder1.create();
dialog12.findViewById(R.id.cancel).setOnClickListener(v12 -> {
if(tr.transfer(newf)) {
								if(lastPlanName.equals(opt.getLastPlanName())) {
									String name = newf.getName();
									opt.putLastPlanName(f2.LastSelectedPlan=name.substring(0,name.length()-4));
								}
								d.dismiss();
								dd.dismiss();
							}else {
								showT("文件写入失败_file_write_failure");
							}
});
if(Build.VERSION.SDK_INT<22) {//为什么：低版本不支持点击外部dimiss
SpannableStringBuilder ssb = new SpannableStringBuilder(tvtv.getText());
ssb.append("\n(否)");
int idxNo = ssb.toString().indexOf("\n(否)");
ssb.setSpan(new ClickableSpan() {
						@Override
						public void onClick(View widget) {
							dd.dismiss();
							}},idxNo,idxNo+"\n(否)".length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

tvtv.setText(ssb);
tvtv.setMovementMethod(LinkMovementMethod.getInstance());
				}
dd.show();

			}else {
					if(tr.transfer(newf)) {
						if(lastPlanName.equals(opt.getLastPlanName())) {
							String name = newf.getName();
							opt.putLastPlanName(f2.LastSelectedPlan=name.substring(0,name.length()-4));
						}
						d.dismiss();
					}else {
						showT("文件写入失败_file_write_failure");
					}
			}
		});
        d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
			args.key = !args.key;
			((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
		});
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> d.dismiss());
	}
		
    public void showT(String text){
        if(m_currentToast != null)
        m_currentToast.cancel();
        m_currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        m_currentToast.show();
    }Toast m_currentToast;
	public boolean isDebug=false;
	boolean ThisIsDirty;

    public class FragAdapter extends FragmentPagerAdapter {

    	private List<Fragment> mFragments;
    	
    	public FragAdapter(FragmentManager fm,List<Fragment> fragments) {
    		super(fm);
    		mFragments=fragments;
    	}

    	@Override
    	public Fragment getItem(int arg0) {
    		return mFragments.get(arg0);
    	}

    	@Override
    	public int getCount() {
    		return mFragments.size();
    	}

    }

    /** 另存当前配置 */
    boolean try_write_configureLet(File newf) {
		try {
			ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(newf,false), ((AgentApplication)getApplication()).get4kCharBuff(), 4096);
			output.write("[:S]");
			output.write(Integer.toString(mdmng.size()-f1.rejector.size()));
			output.write("\n");
			for(mdict_manageable mmTmp:mdmng) {
				String fn = mmTmp.getPath();
				String parent = new File(opt.lastMdlibPath).getAbsolutePath()+"/";
				if (!f1.rejector.contains(fn)) {
					writeForOneLine(output, mmTmp, parent);
				}
			}
			output.flush();
			output.close();
			((AgentApplication)getApplication()).set4kCharBuff(output.cb);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void notifyFileRemoved(File oldf, File newf) {
    	String name = oldf.getName();
    	name = name.substring(0,name.length()-4);
    	if(newf!=null) {
    		int idx=-1;
    		for(int i=0;i<f2.adapter.getCount();i++) {
    			if(f2.adapter.getItem(i).equals(name))
    				{idx=i;break;}
    		}
    		if(idx!=-1) {
        		f2.adapter.remove(name);
        		name = newf.getName();
            	name = name.substring(0,name.length()-4);
            	f2.adapter.insert(name, idx);
    		}
    	}else
    		f2.adapter.remove(name);
    	f2.isDirty = true;
	}

	public void scrollTo(int i) {
		viewPager.setCurrentItem(0);
		mTabLayout.getTabAt(0).select();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		MenuItemImpl mmi = (MenuItemImpl)item;
		boolean isLongClicked=mmi.isLongClicked;
		boolean ret=isLongClicked;
		boolean closeMenu=!isLongClicked;
		AlertDialog d;
		switch (item.getItemId()) {
			case R.id.toolbar_action0:{//提交
				if(isLongClicked) {
					f1.performLastItemLongClick();
					ret=true; break;
				}
				try {
					String name = opt.getLastPlanName();
					File to = new File(opt.pathToMainFolder().append("CONFIG/").append(name).append(".set").toString());
					boolean shouldInsert = false;
					if(!to.exists())
						shouldInsert=true;
					BufferedWriter output = new BufferedWriter(new FileWriter(to,false));

					String parent = new File(opt.lastMdlibPath).getAbsolutePath()+"/";
					for(mdict_manageable mmTmp:mdmng) {
						if(!f1.rejector.contains(mmTmp.getPath())){
							//String pathname = mFile.tryDeScion(new File(md.getPath()), opt.lastMdlibPath);
							writeForOneLine(output, mmTmp, parent);
						}
					}

					output.flush();
					output.close();
					show(R.string.savedone,name);
					if(shouldInsert) f2.adapter.add(String.valueOf(name));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} break;
            case R.id.toolbar_action1:{//刷新
				if(isLongClicked){
					for(mdict_manageable mmTmp:mdmng) {
						f1.selector.add(mmTmp.getPath());
					}
					f1.adapter.notifyDataSetChanged();
				}else {
					f1.refreshDicts(true);
					f1.refreshSize();
				}
			} break;
            case R.id.toolbar_action2:{//重置
				if(isLongClicked){
					f1.selector.clear();
					f1.adapter.notifyDataSetChanged();
				}else {
					ThisIsDirty = true;
					try {
						String name = opt.getLastPlanName();
						File from = new File(opt.pathToMainFolder().append("CONFIG/").append(name).append(".set").toString());
						if (from.exists()) {
							AgentApplication app = ((AgentApplication) getApplication());
							ReusableBufferedReader in = new ReusableBufferedReader(new FileReader(from), app.get4kCharBuff(), 4096);
							f1.rejector.clear();
							f1.adapter.clear();
							do_Load_managee(in);
							f1.isDirty=true;
							f1.refreshSize();
							f1.adapter.notifyDataSetChanged();
							show(R.string.loadsucc2, name);
						} else {
							show(R.string.loadfail2, name);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} break;
            case R.id.toolbar_action3:{//另存为
				if(isLongClicked) {ret=false; break;}
				final String oldFn = opt.getLastPlanName();
				showRenameDialog(oldFn,new transferRunnable() {
					@Override
					public boolean transfer(File to) {
						if(to.exists())//文件覆盖已预先处理。
							f2.adapter.remove(to.getName().substring(0,to.getName().length()-4));
						boolean ret = try_write_configureLet(to);
						if(ret) {
							String name = to.getName();
							f2.adapter.add(name.substring(0,name.length()-4));
							show(R.string.saveas_done);
						}
						return ret;
					}

					@Override
					public void afterTransfer() {
					}
				});
			} break;
            case R.id.toolbar_action4:{//禁用全部
				f1.isDirty=true;
            	if(isLongClicked){
					if(opt.getDictManager1MultiSelecting()){
						f1.rejector.addAll(f1.selector);
						f1.refreshSize();
						f1.adapter.notifyDataSetChanged();
					}
				}else{
					for(mdict_manageable mmTmp:mdmng) {
						f1.rejector.add(mmTmp.getPath());
					}
					ThisIsDirty=true;
					f1.adapter.notifyDataSetChanged();
					f1.refreshSize();
				}
			} break;
            case R.id.toolbar_action5:{//启用全部
				if(isLongClicked){
					if(opt.getDictManager1MultiSelecting()){
						f1.rejector.removeAll(f1.selector);
						f1.refreshSize();
						f1.adapter.notifyDataSetChanged();
					}
				}else {
					f1.rejector.clear();
					f1.adapter.notifyDataSetChanged();
					f1.isDirty = true;
					ThisIsDirty = true;
					f1.refreshSize();
				}
			} break;
            case R.id.toolbar_action6:{//显示全部
				if(isLongClicked){
					if(opt.getDictManager1MultiSelecting()){
						int[] positions = f1.lastClickedPos;
						if(positions[0]!=-1 && positions[1]!=-1){
							int start=Math.min(positions[0], positions[1]);
							int end=Math.max(positions[0], positions[1]);
							for (int i = start; i < end; i++) {
								f1.selector.add(mdmng.get(i).getPath());
							}
						}
					}
				}else {
					File rec = new File(opt.pathToMainFolder().append("CONFIG/mdlibs.txt").toString());

					try {
						BufferedReader in = new BufferedReader(new FileReader(rec));
						String line;
						while ((line = in.readLine()) != null) {
							if (!line.startsWith("/"))
								line = opt.lastMdlibPath + "/" + line;
							line = new File(line).getAbsolutePath();
							if (!mdict_cache.containsKey(line)) {
								mdict_transient m = mdict_cache.get(line);
								if (m == null)
									m = new mdict_transient(line, opt);
								f1.add(m);
								f1.rejector.add(line);
								mdict_cache.put(line, m);
							}
						}
						//mTabLayout.getTabAt(0).setText(getResources().getString(R.string.currentPlan,md.size()-f1.rejector.size()));
						in.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				f1.adapter.notifyDataSetChanged();
			} break;
            case R.id.toolbar_action13:{//折叠
				if(isLongClicked) {ret=false; break;}
				ArrayList<mFile> list = f3.data.getList();
				for(int i=0;i<list.size();i++) {
					mFile mdTmp = list.get(i);
					if(mdTmp.isDirectory()) {
						f3.hiddenParents.insert(mdTmp);
						mdTmp.shrinked=0;
						for(i++;i<list.size();i++) {
							if(!mFile.isDirScionOf(list.get(i), mdTmp)) {break;}
							if(list.get(i).isDirectory()) {break;}
							mdTmp.shrinked++;
						}
						i--;
					}
				}
				f3.adapter.notifyDataSetChanged();
			} break;
            case R.id.toolbar_action14:{//展开
				if(isLongClicked) {ret=false; break;}
				ArrayList<mFile> list1 = f3.data.getList();
				for(int i=0;i<list1.size();i++) {
					f3.hiddenParents.clear();
					mFile mdTmp = list1.get(i);
					if(mdTmp.isDirectory()) {
						mdTmp.shrinked=0;
					}
				}
				f3.adapter.notifyDataSetChanged();
			} break;
            case R.id.toolbar_action7:{//全部选择
            	if(isLongClicked){//间选
					int[] positions = f3.lastClickedPos;
					if(positions[0]!=-1 && positions[1]!=-1){
						int start=Math.min(positions[0], positions[1]);
						int end=Math.max(positions[0], positions[1]);
						for (int i = start; i < end; i++) {
							f3.Selection.put(f3.data.getList().get(i).getAbsolutePath());
						}
					}
				}
            	else if(f3.alreadySelectedAll) {
					f3.Selection.clear();
					f3.alreadySelectedAll=false;
				}else {
					for(int i=0;i<f3.data.size();i++) {
						f3.Selection.put(f3.data.getList().get(i).getAbsolutePath());
					}
					f3.alreadySelectedAll=true;
				}
				f3.adapter.notifyDataSetChanged();
			} break;
            case R.id.toolbar_action8:{//全选失效项
				if(isLongClicked) {ret=false; break;}
				f3.Selection.clear();
				f3.alreadySelectedAll=false;
				for(int i=0;i<f3.data.size();i++) {
					mFile fI = f3.data.getList().get(i);
					if(!fI.exists())
						f3.Selection.put(fI.getAbsolutePath());
				}
				f3.adapter.notifyDataSetChanged();
			} break;
            case R.id.toolbar_action9:{//添加
				if(isLongClicked) {//添加到第几行
					closeMenu=true;
					AlertDialog.Builder builder2 = new AlertDialog.Builder(dict_manager_activity.this);

					View dv = getLayoutInflater().inflate(R.layout.dialog_move_to_line, null);
					NumberPicker np = dv.findViewById(R.id.numberpicker);
					np.setMaxValue(mdmng.size());

					AlertDialog dTmp = builder2.setView(dv).create();
					Window win = dTmp.getWindow();
					win.setBackgroundDrawableResource(GlobalOptions.isDark?R.drawable.popup_shadow_ld:R.drawable.popup_shadow_l);
					dTmp.show();
					ViewGroup dvp = win.getDecorView().findViewById(R.id.dialog);
					dvp.setPadding(0,0,0,0);
					dv.setPadding((int) (15*opt.dm.density), 0,0,(int) (10*opt.dm.density));
				}else{
					int cc = 0;
					ArrayList<String> arr = f3.Selection.flatten();
					for(int i=0;i<arr.size();i++) {
						File fn=new File(arr.get(i));//f3.adapter.getItem();
						if(fn.isDirectory()) continue;
						String key = fn.getPath();
						if(!mdict_cache.containsKey(key)) {
							mdict_transient m = new mdict_transient(key, opt);
							cc++;
							f1.isDirty=true;
							mdict_cache.put(key, m);
						}else if(f1.rejector.contains(fn.getAbsolutePath())) {
							f1.rejector.remove(fn.getAbsolutePath());
							cc++;
						}
					}
					showT("添加完毕!("+cc+"/"+arr.size()+")");
					f1.refreshSize();
					ThisIsDirty=true;
					f1.adapter.notifyDataSetChanged();
				}
			} break;
            case R.id.toolbar_action10:{//移除
				int cc1 = 0;
				for(int i=0;i<mdmng.size();i++) {
					if(f3.Selection.contains(f1.adapter.getItem(i).getPath())) {
						f1.rejector.add(f1.adapter.getItem(i).getPath());
						cc1++;
						f1.isDirty=true;
					}
				}
				ThisIsDirty=true;
				f1.refreshSize();
				showT("移除完毕!("+cc1+"/"+f3.Selection.size()+")");
				f1.adapter.notifyDataSetChanged();
			} break;
            case R.id.toolbar_action11:{//清除记录
				final View dv = inflater.inflate(R.layout.dialog_sure_and_all,null);
				AlertDialog.Builder builder2 = new AlertDialog.Builder(dict_manager_activity.this);
				builder2.setView(dv).setTitle(getResources().getString(R.string.surerrecords,f3.Selection.size()))
						.setPositiveButton(R.string.confirm, (dialog, which) -> {
							HashSet<String> removePool=new HashSet<>();
							ArrayList<String> arr1 = f3.Selection.flatten();
							for(int i = 0; i< arr1.size(); i++) {
								removePool.add(arr1.get(i));
							}
							for(int i=0;i<f3.data.size();i++) {
								mFile item1 = f3.data.getList().get(i);
								if(item1.getClass()==mAssetFile.class)
									continue;
								if(item1.isDirectory())
									continue;
								if(removePool.contains(item1.getAbsolutePath())) {
									//if(item.getClass()==mAssetFile.class) {
									//	removePool.remove(item.getAbsolutePath());
									//	continue;
									//}
									f3.data.getList().remove(i);i--;
									mdlibsCon.remove(mFile.tryDeScion(item1, opt.lastMdlibPath));
									f3.isDirty=true;
									mFile p = item1.getParentFile();
									if(p!=null) {
										int idx=f3.data.indexOf(p);
										if(idx!=-1)
											if(idx==f3.data.size()-1 ||!mFile.isDirScionOf(f3.data.getList().get(idx+1), p))
												f3.data.getList().remove(idx);
									}
								}
							}
							f3.adapter.notifyDataSetChanged();
							onMenuItemClick(toolbarmenu.getItem(10));


							ArrayList<File> moduleFullScannerArr;
							if(((CheckBox)dv.findViewById(R.id.ck)).isChecked()) {
								File[] moduleFullScanner = new File(opt.pathToMainFolder().append("CONFIG").toString()).listFiles(new FileFilter() {
									@Override
									public boolean accept(File pathname) {
										String name = pathname.getName();
										if(name.endsWith(".set")) {
											return true;
										}
										return false;
									}});
								moduleFullScannerArr = new ArrayList<File>(Arrays.asList(moduleFullScanner));
							}else
								moduleFullScannerArr = new ArrayList<>();

							if(f3.isDirty)
								moduleFullScannerArr.add(new File(opt.pathToMainFolder().append("CONFIG/mdlibs.txt").toString()));
							for(File fI:moduleFullScannerArr) {
								InputStreamReader reader = null;
								StringBuffer sb= new StringBuffer("");
								String line = "";

								try {
									reader = new InputStreamReader(new FileInputStream(fI));
									BufferedReader br = new BufferedReader(reader);
									while((line = br.readLine()) != null) {
										try {
											String key=line.startsWith("/")?line:opt.lastMdlibPath+"/"+line;
											if(removePool.contains(key) ||
													removePool.contains(new File(key).getCanonicalPath())){
												//System.out.println(line);
												continue;
												//System.out.println(line);
											}}catch(Exception e) {}
										sb.append(line).append("\n");
									}
									br.close();
									reader.close();

									OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fI));
									BufferedWriter bw = new BufferedWriter(writer);
									bw.write(sb.toString());
									bw.flush();
									bw.close();
									writer.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							showT("移除完毕!");
						})
						.setNeutralButton(R.string.cancel, (dialog, which) -> {

						});
				d = builder2.create();
				d.show();
			} break;
            case R.id.toolbar_action12:{//移动文件
				DialogProperties properties = new DialogProperties();
				properties.selection_mode = DialogConfigs.SINGLE_MODE;
				properties.selection_type = DialogConfigs.DIR_SELECT;
				properties.root = new File("/");
				properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
				properties.offset = new File(opt.lastMdlibPath);
				properties.opt_dir=new File(opt.pathToDatabases()+"favorite_dirs/");
				properties.opt_dir.mkdirs();
				FilePickerDialog dialog = new FilePickerDialog(this, properties);
				dialog.setTitle(R.string.pickdestineFolder);
				dialog.setDialogSelectionListener(new DialogSelectionListener() {
					@Override
					public void
					onSelectedFilePaths(String[] files, String n) {
						File p=new File(files[0]);//新家
						if(p.isDirectory()) {
							ArrayList<String> arr = f3.Selection.flatten();
							RashSet<String> renameList = new RashSet<>();
							ArrayList<String> renameListe;
							HashMap<String, mdict> mdict_cache = new HashMap<>(mdmng.size());
							for(mdict_manageable mmTmp:mdmng) {
								if(mmTmp instanceof mdict)
									mdict_cache.put(mmTmp.getPath(), (mdict) mmTmp);
							}
							int cc=0;
							for(String sI:arr) {//do actual rename. rename a lot of files..
								mFile mF = new mFile(sI).init();
								//ommitting directory.
								//if(sI.startsWith("/ASSET/") && CMN.AssetMap.containsKey(sI)) continue;
								if(mF.isDirectory()) continue;
								if(f3.data.get(mF).isDirectory()) continue;
								mdict_manageable mmTmp = mdict_cache.get(sI);
								if(mmTmp==null) {
									try {
										mmTmp=new mdict_prempter(sI, opt);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								File OldF = mmTmp.f();
								File toF = new File(p, OldF.getName());
								boolean ret = mmTmp.moveFileTo(toF);//厉害 存在的移动了
								if(ret) {
									mdlibsCon.remove(mFile.tryDeScion(OldF, opt.lastMdlibPath));
									mdlibsCon.add(mFile.tryDeScion(toF, opt.lastMdlibPath));
									f3.Selection.remove(sI);//移出f3的选择
									renameList.put(sI);//然后记录
									cc++;
								}
							}
							mdict_cache.clear();
							f1.isDirty=true;
							renameListe = renameList.flatten();
							for(String fnI:renameListe) {
								mFile fOld = new mFile(fnI).init();
								int idx = f3.data.remove(fOld);
								if(idx!=-1) {
									mFile p2 = fOld.getParentFile().init();
									if(p2!=null) {
										int idx2=f3.data.indexOf(p2);
										if(idx2!=-1) {//如有必要，移除多余的父文件夹
											if(idx2==f3.data.size()-1 ||!mFile.isDirScionOf(f3.data.getList().get(idx2+1), p2))
												f3.data.getList().remove(idx2);
											f3.data.OverFlow.remove(p2);
										}
										//showT(System.currentTimeMillis()+" "+idx2);
									}else {
										f3.data.OverFlow.remove(p2);
										//f3.data.OverFlow.clear();
									}
									//f3.data.OverFlow.clear();

									mFile val = new mFile(p, new File(fnI).getName());
									f3.data.insert(val.init());
									f3.Selection.insert(val.getAbsolutePath());
									if(!mFile.isDirScionOf(val, opt.lastMdlibPath))
										f3.data.insertOverFlow(val.getParentFile().init());
								}
							}
							f3.adapter.notifyDataSetChanged();
							f3.isDirty=true;

							ArrayList<File> moduleFullScannerArr;

							File[] moduleFullScanner = new File(opt.pathToMainFolder().append("CONFIG").toString()).listFiles(pathname -> {
								String name = pathname.getName();
								return name.endsWith(".set");
							});
							moduleFullScannerArr = new ArrayList<>(Arrays.asList(moduleFullScanner));
							moduleFullScannerArr.add(new File(getExternalFilesDir(null),"default.txt"));
							moduleFullScannerArr.add(new File(opt.pathToMainFolder().append("CONFIG/mdlibs.txt").toString()));
							HashSet<String> mdlibs = new HashSet<>();
							for(File fI:moduleFullScannerArr) {
								boolean modified=false;
								mdlibs.clear();
								StringBuffer sb= new StringBuffer();
								String line;

								try {
									BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fI)));
									while((line = br.readLine()) != null) {
										boolean isFilter=false;
										boolean isClicker=false;
										boolean isAudio=false;
										if(line.startsWith("[:")){
											int idx = line.indexOf("]",2);
											if(idx>=2){
												String[] arr2 = line.substring(2, idx).split(":");
												line = line.substring(idx+1);
												for (String pI:arr2) {
													switch (pI){
														case "F":
															isFilter=true;
														break;
														case "C":
															isClicker=true;
														break;
														case "A":
															isAudio=true;
														break;
														case "Z":
															//flag|=0x10;
														break;
													}
												}
											}
										}
										try {
											String key=line.startsWith("/")?line:opt.lastMdlibPath+"/"+line;
											if(renameList.contains(key) || renameList.contains(new File(key).getCanonicalPath())){
												modified=true;
												line = mFile.tryDeScion(new File(p, new File(key).getName()), opt.lastMdlibPath);//搬到新家
											}}catch(Exception ignored) {}
										//todo wtf???
										if(!mdlibs.contains(line)) {//避免重复
											sb.append(line).append("\n");
											String prefix="";
											if(isFilter) prefix+=":F";
											if(isClicker) prefix+=":C";
											if(isAudio) prefix+=":A";
											if(prefix.length()>0)
												line="["+prefix+"]"+line;
											mdlibs.add(line);
										}
									}
									br.close();

									if(modified){
										OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fI));
										BufferedWriter bw = new BufferedWriter(writer);
										bw.write(sb.toString());
										bw.flush();
										bw.close();
										writer.close();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							renameList.clear();
							renameList=null;
							renameListe.clear();
							renameListe=null;
						}
					}

					@Override
					public void onEnterSlideShow(Window win, int delay) {

					}

					@Override
					public void onExitSlideShow() {

					}

					@Override
					public Activity getDialogActivity() {
						return null;
					}

					@Override
					public void onDismiss() {

					}
				});
				dialog.show();

			} break;
            case R.id.toolbar_action15:{//新建
				ViewGroup dv1 = (ViewGroup) getLayoutInflater().inflate(R.layout.fp_edittext, null);
				final EditText etNew = dv1.findViewById(R.id.edt_input);
				final View btn_Done = dv1.findViewById(R.id.done);
				AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(dv1);
				final AlertDialog dd = builder.create();
				btn_Done.setOnClickListener(v -> {
					File source  = new File(opt.pathToMainFolder().append("CONFIG/").append(etNew.getText()).append(".set").toString());
					if(!mFile.isDirScionOf(source, opt.pathToMainFolder().append("CONFIG/").toString())) {
						showT("名称非法！");
						return;
					}
					if(source.exists()) {
						showT("错误：文件已经存在！");
						return;
					}
					try {
						source.createNewFile();
						f2.adapter.add(etNew.getText().toString());
						dd.dismiss();
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
					showT("未知错误");
				});
				etNew.setOnEditorActionListener((v, actionId, event) -> {
					if(actionId == EditorInfo.IME_ACTION_DONE ||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
						btn_Done.performClick();
						return true;
					}
					return false;
				});
				//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				//imm.showSoftInput(etNew, InputMethodManager.SHOW_FORCED);
				dd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

				dd.show();
			} break;
            default:
			break;
        }

		if(closeMenu && !mmi.isActionButton())
			toolbar.getMenu().close();
		return ret;
	}


	protected void do_Load_managee(ReusableBufferedReader in) throws IOException {
		String line;
		int cc=0;
		mdmng.clear();
		ReadLines:
		while((line = in.readLine())!=null){
			int flag = 0;
			if(line.startsWith("[:")){
				int idx = line.indexOf("]",2);
				if(idx>=2){
					String[] arr = line.substring(2, idx).split(":");
					line = line.substring(idx+1);
					for (String pI:arr) {
						switch (pI){
							case "F":
								flag|=0x1;
							break;
							case "C":
								flag|=0x2;
							break;
							case "A":
								flag|=0x4;
							break;
							case "H":
								flag|=0x8;
							break;
							case "Z":
								flag|=0x10;
							break;
							case "S":
								int size = IU.parsint(line);
								if(size>0) mdmng.ensureCapacity(size);
							continue ReadLines;
						}
					}
				}
			}
			if (!line.startsWith("/"))
				line = opt.lastMdlibPath + "/" + line;
			mdict_transient mmtmp = mdict_cache.get(line);
			if (mmtmp == null)
				mmtmp = new mdict_transient(line, opt, 0);
			if(!mmtmp.isMddResource()) flag&=~0x4;
			mmtmp.setTmpIsFlag(flag);
			mdmng.add(mmtmp);
		}
		in.close();
	}
}



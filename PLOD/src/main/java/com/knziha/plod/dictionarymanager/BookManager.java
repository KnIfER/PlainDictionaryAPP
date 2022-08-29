package com.knziha.plod.dictionarymanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.ActionMenuView.LayoutParams;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.knziha.filepicker.utils.FU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedWriter;
import com.knziha.plod.dictionarymanager.files.mAssetFile;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.MagentTransient;
import com.knziha.plod.dictionarymodels.mngr_agent_manageable;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.settings.BookOptionsDialog;
import com.knziha.plod.widgets.ViewUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class BookManager extends Toastable_Activity implements OnMenuItemClickListener
{
	public final static int id = 110;
	HashMap<String, BookPresenter> mdict_cache = new HashMap<>();
	Intent intent = new Intent();
	private PopupWindow mPopup;
	//public ArrayList<PlaceHolder> slots;
	public MainActivityUIBase.LoadManager loadMan;
	private ArrayList<Fragment> fragments;
	public HashMap<String, BookPresenter> app_mdict_cache;
	//public HashMap<CharSequence,byte[]> UIProjects;
	public HashSet<CharSequence> dirtyMap;
	
	public File ConfigFile;
	
	public File fRecord;
	
	public File DefordFile;
	
	public File SecordFile;
	
	private boolean deleting;
	
	public MenuBuilder AllMenus;
	public List<MenuItemImpl> Menu1;
	public List<MenuItemImpl> Menu2;
	public List<MenuItemImpl> Menu3;
	public List<MenuItemImpl> Menu3Sel;
	private String initialModuleName;
	private boolean initialModuleChanged;
	public boolean initialModuleSwitched;
	private String lastLoadedModule;
	
	public BookManager() { }
	
	public File fileToSet(String name) {
		return opt.fileToSet(ConfigFile, name);
	}
	
	BookOptionsDialog bookOptionsDialog;
	public void showBookPreferences(BookPresenter...books) {
		if (bookOptionsDialog==null) bookOptionsDialog = new BookOptionsDialog();
		bookOptionsDialog.bookOptions.setData(books);
		try {
			if (!bookOptionsDialog.isAdded()) {
				bookOptionsDialog.show(getSupportFragmentManager(), "");
			} else {
				bookOptionsDialog.getDialog().show();
			}
		} catch (Exception ignored) { }
	}
	
	public void markDirty() {
		if (toolbar.mNavButtonView==toolbar.mNavButtonLayout) {
			ImageButton navBtn = toolbar.mNavButtonView;
			ImageButton cross = new ImageButton(this);
			cross.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final View dv = inflater.inflate(R.layout.dialog_sure_and_all, null);
					CheckBox ck = dv.findViewById(R.id.ck);
					ck.setChecked(PDICMainAppOptions.getRevertExitManager());
					TextView tv = dv.findViewById(R.id.title);
					tv.setOnClickListener(v1 -> ck.toggle());
					tv.setText("并退出");
					AlertDialog.Builder builder2 = new AlertDialog.Builder(BookManager.this);
					builder2.setView(dv).setTitle("确认取消修改吗？")
							.setPositiveButton(R.string.confirm, (dialog, which) -> {
								revertModule();
								PDICMainAppOptions.setRevertExitManager(ck.isChecked());
								f1.dataSetChanged();
								if (ck.isChecked()) {
									checkAll();
									finish();
								}
							})
							.setNeutralButton(R.string.cancel, null);
					d = builder2.create();
					d.show();
				}
			});
			ViewGroup.LayoutParams lp = navBtn.getLayoutParams();
			cross.setLayoutParams(lp);
			cross.setBackground(navBtn.getBackground().getConstantState().newDrawable());
			cross.setImageResource(R.drawable.ic_baseline_clear_24);
			LinearLayout fram = new LinearLayout(this);
			lp.width = -2;
			fram.setLayoutParams(lp);
			fram.setOrientation(LinearLayout.HORIZONTAL);
			fram.setPadding(0,0, (int) (GlobalOptions.density*2.5),0);
			ViewUtils.replaceView(toolbar.mNavButtonLayout=fram, navBtn, false);
			ViewUtils.addViewToParent(navBtn, fram);
			ViewUtils.addViewToParent(cross, fram);
		}
	}
	
	public interface transferRunnable{
		boolean transfer(File to);
		void afterTransfer();
	}

    private ViewGroup toastmaker;
    private Toolbar toolbar;
    static String dictQueryWord;
    private SearchView searchView;
    BookManagerMain f1;
    BookManagerModules f2;
    BookManagerFolderlike f3;
	BookManagerWebsites f4;
    ViewPager viewPager;  //对应的viewPager
    TabLayout mTabLayout;
	LayoutInflater inflater;

    public HashSet<String> mdlibsCon;
	protected int CurrentPage;

	@Override
	public void onBackPressed() {
		if(mPopup!=null){
			mPopup.dismiss();
			mPopup=null;
			return;
		}

		int item = viewPager.getCurrentItem();
		if(item<fragments.size() && fragments.get(item) instanceof BookManagerFragment.SelectableFragment){
			if(((BookManagerFragment.SelectableFragment)fragments.get(item)).exitSelectionMode()){
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

	private void checkF1() {
		if(f1.isDirty) {
			intent.putExtra("result", true);
			int result = checkModuleDirty(true);
			boolean isInitialModule = initialModuleName !=null && initialModuleName.equals(loadMan.lazyMan.lastLoadedModule);
			boolean identical = (result&0x1)!=0 && isInitialModule;
			if (isInitialModule && !initialModuleChanged) {
				CMN.debug("一成不变");
			} else {
				CMN.debug("变化了", (result&0x1)!=0, isInitialModule);
				intent.putExtra("changed", true);
				intent.putExtra("identical", identical);
				intent.putExtra("moduleChanged", initialModuleSwitched);
			}
			f1.isDirty=false;
		}
	}
	
	/** 放弃修改。 */
	public void revertModule() {
		ArrayList<BookPresenter> md = f1.manager_group();
		PlaceHolder[] nda = f1.markDataDirty(f1.isDataDirty());
		MainActivityUIBase.LazyLoadManager neoLM = loadMan.lazyMan;
		ArrayList<PlaceHolder> da = neoLM.placeHolders; // 现有列表列表
		if (f1.dirtyAttrArray.size()>0) {
			for (Map.Entry<PlaceHolder, Integer> node : f1.dirtyAttrArray.entrySet()) {
				int orgVal = node.getValue();
				if (orgVal!=node.getKey().tmpIsFlag) {
					node.getKey().tmpIsFlag = orgVal;
					neoLM.chairCount += PDICMainAppOptions.getTmpIsHidden(orgVal)?-1:1;
				}
			}
			f1.dirtyAttrArray.clear();
		}
		if (nda!=null) {
			da.clear();
			md.clear();
			for (PlaceHolder ph : nda) {
				String key = ph.getPath(opt).toString();
				BookPresenter ret = mdict_cache.get(key);
				if (ret!=null && ret.getClass()!=BookPresenter.class)
					ret = null;
				da.add(ph);
				md.add(ret);
			}
			neoLM.chairCount=0;
			neoLM.filterCount=0;
			for (int i = 0; i < nda.length; i++) {
				PlaceHolder ph = da.get(i);
				ph.lineNumber = i | (ph.lineNumber & 0x80000000);
				final int flag = ph.tmpIsFlag;
				if (!PDICMainAppOptions.getTmpIsHidden(flag)) {
					neoLM.chairCount++;
					if (PDICMainAppOptions.getTmpIsClicker(flag)) {
						neoLM.filterCount++;
					}
				}
			}
		}
		f1.refreshSize();
		f1.markDataDirty(false);
	}
	
	/** 检查模组是否有结构变化或属性变化，并保存。 */
	public int checkModuleDirty(boolean exit) {
		int i;
		ArrayList<BookPresenter> md = f1.manager_group();
		BookPresenter bp;
		int size = md.size(); // 现有列表，可能被修改了！包括结构修改与属性修改！
		MainActivityUIBase.LazyLoadManager neoLM = loadMan.lazyMan;
		ArrayList<PlaceHolder> da = neoLM.placeHolders; // 现有列表列表
		PlaceHolder[] nda = f1.markDataDirty(f1.isDataDirty()); // 看看比对原列表，是否变化了？
		boolean identical = nda==null || size==nda.length;
		boolean rolesChanged = false;
		if (!exit) {
			initialModuleSwitched=true;
		}
		if (f1. dirtyAttrArray.size()>0) { // 属性变化啦！
			if (exit && !initialModuleSwitched) {
				for (Map.Entry<PlaceHolder, Integer> node : f1.dirtyAttrArray.entrySet()) {
					PlaceHolder ph = node.getKey();
					if (ph.tmpIsFlag != node.getValue()) {
						if (!rolesChanged) rolesChanged = true;
						bp = mdict_cache.get(ph.getPath(opt).getPath());
						if (bp!=null) {
							bp.tmpIsFlag = ph.tmpIsFlag;
						}
					}
				}
			} else {
				for (Map.Entry<PlaceHolder, Integer> node : f1.dirtyAttrArray.entrySet()) {
					if (node.getKey().tmpIsFlag != node.getValue()) {
						rolesChanged = true;
						break;
					}
				}
			}
		}
		if (neoLM.chairCount>neoLM.CosyChair.length
				|| neoLM.filterCount>neoLM.CosySofa.length) {
			identical = false;
		}
		CMN.debug("checkModuleDirty::", nda == null ? -1 : nda.length, size, loadMan.lazyMan.placeHolders.size());
		CMN.debug("checkModuleDirty::", lastLoadedModule, loadMan.lazyMan.lastLoadedModule);
		CMN.debug("checkModuleDirty::", neoLM.chairCount, neoLM.CosyChair.length, neoLM.filterCount, neoLM.CosySofa.length);
		if (nda!=null) { // 结构变化啦！
			neoLM.chairCount=0;
			neoLM.filterCount=0;
			for (i = 0; i < size; i++) {
				PlaceHolder ph = da.get(i);
				ph.lineNumber = i | (ph.lineNumber & 0x80000000);
				final int flag = ph.tmpIsFlag;
				if (!PDICMainAppOptions.getTmpIsHidden(flag)) {
					neoLM.chairCount++;
					if (PDICMainAppOptions.getTmpIsClicker(flag)) {
						neoLM.filterCount++;
					}
				}
				if(identical) {
					BookPresenter mdTmp = getMagentAt(i, false);
					if(mdTmp!=loadMan.EmptyBook && !mdTmp.equalsToPlaceHolder(ph)
							|| !ph.getPath(opt).equals(nda[i].getPath(opt)))
						identical = false;
					else if(!rolesChanged && ph.tmpIsFlag != nda[i].tmpIsFlag) {
						rolesChanged = true;
					}
				}
			}
		}
		if (rolesChanged || !identical) {
			if (initialModuleName != null && initialModuleName.equals(lastLoadedModule)) {
				initialModuleChanged = true;
			}
			try {
				final File def = opt.getCacheCurrentGroup() ? new File(getExternalFilesDir(null), "default.txt")
						: opt.fileToSet(ConfigFile, opt.getLastPlanName("LastPlanName"));      //!!!原配
				ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(def), ((AgentApplication) getApplication()).get4kCharBuff(), 4096);
				String parent = opt.lastMdlibPath.getPath();
				output.write("[:S]");
				output.write(Integer.toString(md.size()));
				output.write("\n");
				for (i = 0; i < md.size(); i++) {
					writeForOneLine(output, i, parent);
				}
				output.flush();
				output.close();
				f1.markDataDirty(false);
				if (!exit) showT("配置<"+lastLoadedModule+">已保存！");
			} catch (Exception e) {
				CMN.debug(e);
				if (!exit) showT("配置<"+lastLoadedModule+">保存失败！\n"+e.getLocalizedMessage());
			}
		} else {
			f1.markDataDirty(false);
		}
		return (identical?1:0) | (rolesChanged?2:0);
	}
	
	private void checkAll() {
		AgentApplication app = ((AgentApplication)getApplication());
		CMN.debug("肮脏的一群！", f1.isDirty, f2.isDirty, f3.isDirty);
		checkF1();

		if(f3.isDirty) {
//			try {
//				ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(fRecord), app.get4kCharBuff(), 4096);
//				String parent = opt.lastMdlibPath.getPath()+File.separator;
//				for(mFile mdTmp:f3.data.getList()) { dataTree
//					if(mdTmp.getClass()==mAssetFile.class) continue;
//					if(mdTmp.isDirectory()) continue;
//					String name = mdTmp.getPath();
//					if(name.startsWith(parent))
//						name = name.substring(parent.length());
//					output.write(name);
//					output.write("\n");
//				}
//				output.flush();
//				output.close();
//			} catch (IOException e2) {
//				e2.printStackTrace();
//			}
			f3.isDirty=false;
		}

		if(f2.isDirty) {
			intent.putExtra("result2", true);
			try {
				ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(SecordFile), app.get4kCharBuff(), 4096);
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
		viewPager.clearOnPageChangeListeners();
		mTabLayout.clearOnTabSelectedListeners();
	}

	private void writeForOneLine(Writer out, int position, String parent) throws IOException {
		String name = f1.getPathAt(position);
		if(name.startsWith(parent) && name.length()>parent.length())
			name = name.substring(parent.length()+1);
		int flag = f1.getPlaceFlagAt(position);
		if(flag!=0) {
			out.write("[:");
			if(PDICMainAppOptions.getTmpIsFiler(flag))
				out.write("F");
			else if(PDICMainAppOptions.getTmpIsAudior(flag))
				out.write("A");
			if(PDICMainAppOptions.getTmpIsClicker(flag))
				out.write(":C");
			if(PDICMainAppOptions.getTmpIsCollapsed(flag))
				out.write(":Z");
			if(PDICMainAppOptions.getTmpIsHidden(flag))
				out.write(":H");
			out.write("]");
		}
		out.write(name);
		out.write("\n");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(null);
		AgentApplication agent = ((AgentApplication)getApplication());
		app_mdict_cache=agent.mdict_cache;
		//UIProjects=agent.BookProjects;
		dirtyMap=agent.dirtyMap;
		opt=agent.opt;
		loadMan = agent.loadManager;
		mdlibsCon=agent.mdlibsCon;
		
		ConfigFile = opt.fileToConfig();
		
		fRecord = opt.fileToDecords(ConfigFile);
		
		SecordFile = opt.fileToSecords(ConfigFile);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dict_manager_main);
		
        getWindowManager().getDefaultDisplay().getMetrics(opt.dm);

        findViewById(R.id.drawer_layout).setBackgroundColor(GlobalOptions.isDark?Color.BLACK:opt.getMainBackground());
        
        viewPager = findViewById(R.id.viewpager);
        mTabLayout = findViewById(R.id.mTabLayout);
		inflater=LayoutInflater.from(getApplicationContext());
        toolbar = findViewById(R.id.toolbar);
		toolbar_setTitle(initialModuleName =loadMan.lazyMan.lastLoadedModule);
		final long resId = R.xml.menu_dict_manager;
        toolbar.inflateMenu((int)resId);
		AllMenus = (MenuBuilder) toolbar.getMenu();
		MenuCompat.setGroupDividerEnabled(AllMenus, true);
		AllMenus.mOverlapAnchor = PDICMainAppOptions.menuOverlapAnchor();
		Menu1 = ViewUtils.MapNumberToMenu(AllMenus, 0, 2, 1, 19, 18, 5, 20, 3, 6, 4, 16);
		Menu2 = ViewUtils.MapNumberToMenu(AllMenus, 0, 3, 15, 16);
		Menu3 = ViewUtils.MapNumberToMenu(AllMenus, 7, 8, 13, 14, 16);
		Menu3Sel = ViewUtils.MapNumberToMenu(AllMenus, 7, 17, 8, 9, 10, 11, 12, 13, 14, 16);
		AllMenus.setItems(Menu1);
  
		fragments= new ArrayList<>();
		
	    String[] tabTitle = {getResources().getString(R.string.currentPlan,0),getResources().getString(R.string.allPlans), "网络词典", "全部词典"};
	    
		fragments.addAll(Arrays.asList(f1 = new BookManagerMain(), f2 = new BookManagerModules(), f4 = new BookManagerWebsites(), f3 = new BookManagerFolderlike()));
		f1.a=f2.a=f4.a=f3.a=this;

		f3.oes = f4.oes = new BookManagerFolderlike.OnEnterSelectionListener() {
			public void onEnterSelection(BookManagerFolderAbs f, boolean enter){
				AllMenus.setItems(enter?Menu3Sel:Menu3);
			}
			public int addIt(BookManagerFolderAbs f, final mFile fn) {
				boolean found=false;
				String path = fn.getRealPath().getAbsolutePath();
				for(int i=0;i<f1.manager_group().size();i++) {
					if(f1.getPathAt(i).equals(path)) {
						if(f1.getPlaceRejected(i)) {
							f1.setPlaceRejected(i, false);
							f1.dataSetChanged();
							return 1;
						}
						found=true;
						break;
					}
				}
				if(!found) {
					//show("adding new!"+fn.getAbsolutePath());
					f.mDslv.post(() -> f1.add(path));
					return 1;
				}
				else return 0;
			}
		};
		FragAdapter adapterf = new FragAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(adapterf);
	    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout) {
	    	@Override public void onPageSelected(int page) {
				Fragment fI = fragments.get(page);
				viewPager.setOffscreenPageLimit(Math.max(viewPager.getOffscreenPageLimit(), Math.max(1+page, 1)));
				List<MenuItemImpl> menu;
				if(fI==f1) {
					menu = Menu1;
					if (loadMan.lazyMan.lastLoadedModule!=lastLoadedModule) {
						toolbar_setTitle(loadMan.lazyMan.lastLoadedModule);
					}
	    		} else if(fI==f2) {
					menu = Menu2;
				} else if (fI == f3) {
					menu = f3.SelectionMode ? Menu3Sel : Menu3;
				} else {
					menu = f4.SelectionMode ? Menu3Sel : Menu3;
				}
				AllMenus.setItems(menu);
				super.onPageSelected(CurrentPage = page);
				opt.setDictManagerTap(CurrentPage);
	    	}
	    });
		
		for (String s : tabTitle) {
			mTabLayout.addTab(mTabLayout.newTab().setText(s));
		}
		mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}
			@Override public void onTabUnselected(TabLayout.Tab tab) {}
			@Override public void onTabReselected(TabLayout.Tab tab) {}
        });
		
	    mTabLayout.setSelectedTabIndicatorColor(0xe0000000|ViewUtils.getComplementaryColor(opt.getMainBackground())&0x00ffffff);
//	    mTabLayout.setSelectedTabIndicatorColor(ViewUtils.getComplementaryColor(Color.BLUE));
	    //mTabLayout.setSelectedTabIndicatorColor(ColorUtils.blendARGB(bg, Color.BLACK, 0.28f));
	    mTabLayout.setSelectedTabIndicatorHeight((int) (0.7*mResource.getDimension(R.dimen._14_)));
	    
	    //tofo
	    //viewPager.setCurrentItem(CurrentPage = opt.getDictManagerTap());
	    viewPager.setCurrentItem(CurrentPage = 0); // 暂时关闭
		
	    viewPager.setOffscreenPageLimit(Math.max(viewPager.getOffscreenPageLimit(), 1+CurrentPage));

	    toastmaker =  findViewById(R.id.toastmaker);
		
		if(GlobalOptions.isDark) toastmaker.setBackgroundColor(0xff303030);
	    
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material); //ic_flag_24dp
		toolbar.setNavigationOnClickListener(v1 -> {
			onBackPressed();
		});
        
 		MenuItem searchItem = ViewUtils.findInMenu(Menu3, R.id.action_search);
 		searchItem.setShowAsAction(2);
 		searchView = (SearchView) searchItem.getActionView();
 		searchView.setSubmitButtonEnabled(false);
        searchView.setOnSearchClickListener(v -> {
        	LayoutParams lp = (LayoutParams) searchView.getLayoutParams();
			lp.width=getResources().getDisplayMetrics().widthPixels*2/3;
			lp.setMargins(0, 0, 50, 0);
			searchView.requestLayout();
        });
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
	        @Override
	        public boolean onQueryTextSubmit(String query) {
				query = query.trim().toLowerCase();
	            if(query.equals("")) query=null;
				dictQueryWord=query;
	            if(f1.adapter!=null)
	            	f1.dataSetChanged();
	            if(f3.adapter!=null)
	            	f3.adapter.notifyDataSetChanged();
				if(dictQueryWord!=null){
					int cc=0;
					Fragment fI = fragments.get(viewPager.getCurrentItem());
					if (fI==f1) {
						for (int i = 0; i < f1.manager_group().size(); i++) {
							if (f1.getNameAt(i).toString().toLowerCase().indexOf(query)>0)
								cc++;
						}
					} else if (fI==f3) {
						for (int i = 0; i < f3.adapter.getCount(); i++) {
							if (f3.adapter.getItem(i).getName().toLowerCase().contains(query))
								cc++;
						}
					}
					((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					showTopSnack(toastmaker, cc==0?getResources().getString(R.string.fn):("找到"+cc+"项"));
				}
        	    return true;
	        }

	        @Override
	        public boolean onQueryTextChange(String newText) {
	            return true;
	        }
	    });
		searchView.setOnCloseListener(() -> {
			dictQueryWord=null;
			searchView.getLayoutParams().width=-2;
			searchView.requestLayout();
			if(f1.adapter!=null) f1.dataSetChanged();
			if(f3.adapter!=null) f3.adapter.notifyDataSetChanged();
			return false;
		});
		if(dictQueryWord!=null){
			searchView.mSearchSrcTextView.setText(dictQueryWord);
			searchView.onSearchClicked(false);
		}
		searchView.mSearchSrcTextView.setTextColor(Color.WHITE);
		searchView.mSearchSrcTextView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		Window win = getWindow();
		win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
        win.getDecorView().setSystemUiVisibility(
        		View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        if(Build.VERSION.SDK_INT>=21) {
			win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	        win.setStatusBarColor(Color.TRANSPARENT);
	        win.setNavigationBarColor(Color.BLACK);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				View decorView = win.getDecorView();
				int vis = decorView.getSystemUiVisibility();
				if (false) {
					vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
				} else {
					vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
				}
				decorView.setSystemUiVisibility(vis);
			}
        }
		setResult(RESULT_OK, intent);
        agent.clearTdata();
	}
	//onCreate结束
	
	private void toolbar_setTitle(String lastLoadedModule) {
		if (lastLoadedModule!=null) {
			this.lastLoadedModule=lastLoadedModule;
			toolbar.setTitle(lastLoadedModule.endsWith(".set")?lastLoadedModule.substring(0,lastLoadedModule.length()-4):lastLoadedModule);
		}
	}
	
	protected void showRenameDialog(final String lastPlanName,final transferRunnable tr) {//哈哈这么长的代码。。。
		View dialog = getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
        final ListView lv = dialog.findViewById(R.id.lv);
        final EditText et = dialog.findViewById(R.id.et);
        ImageView iv = dialog.findViewById(R.id.confirm);
        File fSearchFile = new File(ConfigFile, lastPlanName);//查找旧plan
        final String fSearch = lastPlanName;//查找旧plan
        boolean found=false;
		
		String names[] = ConfigFile.list();
		final ArrayList<File> setsArr = new ArrayList<>(names.length);
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if(!SU.isNotGroupSuffix(name)) {
				if(!found && fSearch.equals(name)) {
					found=true;
				}
				setsArr.add(new File(ConfigFile, name));
			}
		}
        
        if(found) {
			setsArr.add(0,fSearchFile);//让它出现在第一项
		} else {
			et.setText(lastPlanName);//否则码上去~
		}
        
        lv.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() { return setsArr.size(); }

			@Override
			public Object getItem(int position) { return setsArr.get(position); }

			@Override
			public long getItemId(int position) { return 0;
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
								File newf = new File(oldf.getParentFile(), SU.legacySetFileName(holder.tv.getText().toString()));
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
			private void decorateByViewHolder(myHolder holder) {
				holder.remove.setVisibility(deleting?View.VISIBLE:View.GONE);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(BookManager.this);
        builder.setView(dialog);
        builder.setIcon(R.mipmap.ic_directory_parent);
        builder.setNeutralButton(R.string.delete,null);
        builder.setPositiveButton(R.string.cancel,null);
        final AlertDialog d = builder.create();

        d.setOnDismissListener(dialog1 -> tr.afterTransfer());
        d.show();
        iv.setOnClickListener(v -> {
			String newName = SU.trimStart(et.getText().toString());
			if(newName.equals("") || newName.contains("/")) {
				show(R.string.renamefail0);
				return;
			}
			newName = SU.legacySetFileName(newName);
			final File newf = new File(ConfigFile, newName);
			if(!fSearch.equals(newf) && newf.exists()) {//覆盖
				View dialog12 = getLayoutInflater().inflate(R.layout.dialog_about,null);
				AlertDialog.Builder builder1 = new AlertDialog.Builder(BookManager.this);
				TextView tvtv = dialog12.findViewById(R.id.title);
				tvtv.setText(R.string.wenj_fugai);
				tvtv.setPadding(50, 50, 0, 0);
				builder1.setView(dialog12);
				final AlertDialog dd = builder1.create();
				dialog12.findViewById(R.id.cancel).setOnClickListener(v12 -> {
					if(tr.transfer(newf)) {
						d.dismiss();
						dd.dismiss();
					}else {
						showT("文件写入失败_file_write_failure");
					}
				});
				dd.show();
			}else {
				if(tr.transfer(newf)) {
					d.dismiss();
				} else {
					showT("文件写入失败_file_write_failure");
				}
			}
		});
        d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
			deleting = !deleting;
			((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
		});
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> d.dismiss());
	}
	
	
	public boolean isDebug=false;

    public static class FragAdapter extends FragmentPagerAdapter {
    	private List<Fragment> mFragments;
    	public FragAdapter(FragmentManager fm,List<Fragment> fragments) {
    		super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    		mFragments=fragments;
    	}

    	@NonNull
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
			AgentApplication app = ((AgentApplication) getApplication());
			ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(newf,false), app.get4kCharBuff(), 4096);
			output.write("[:S]");
			int sz = f1.manager_group().size();
			output.write(Integer.toString(sz));
			output.write("\n");
			String parent = opt.lastMdlibPath.getPath();
			for (int i = 0; i < sz; i++) {
				writeForOneLine(output, i, parent);
			}
			output.flush();
			output.close();
			app.set4kCharBuff(output.cb);
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
		viewPager.setCurrentItem(i);
		mTabLayout.getTabAt(i).select();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		MenuItemImpl mmi = (MenuItemImpl)item;
		boolean isLongClicked=mmi.isLongClicked;
		boolean ret=isLongClicked;
		boolean closeMenu=!isLongClicked;
		AlertDialog d;
		BookManagerFolderAbs f3=CurrentPage==2?f4:this.f3;
		ArrayList<mFile> list = f3.dataTree;
		int szf1 = f1.manager_group().size(), cnt=0;
		boolean sf1 = !opt.getDictManager1MultiSelecting() && !isLongClicked && szf1>0;
		switch (item.getItemId()) {
			/* 词典选项 */
			case R.id.toolbar_action0:{
				if(!isLongClicked) f1.performLastItemLongClick();
			} break;
			/* 全选 | 全不选, f1 */
            case R.id.toolbar_action2:{
				closeMenu = false;
				if(sf1) opt.setDictManager1MultiSelecting(true);
				if(isLongClicked){
					for (int i = 0; i < szf1; i++) {
						f1.setPlaceSelected(i, false);
					}
				}
				else {
					for (int i = 0; i < szf1; i++) {
						f1.setPlaceSelected(i, true);
					}
				}
				f1.dataSetChanged();
			} break;
			/* 间选 | 间不选, f1 */
			case R.id.toolbar_action1:{
				closeMenu = false;
				int[] positions = f1.lastClickedPos;
				if(positions[0]!=-1 && positions[1]!=-1){
					int start=positions[0];
					int end=positions[1];
					if(end<start){
						int tmp=end;
						end=start;
						start=tmp;
					}
					for (int i = start; i <= end; i++) {
						f1.setPlaceSelected(i, !isLongClicked);
						if(sf1) {
							opt.setDictManager1MultiSelecting(true);
							sf1 = false;
						}
					}
				}
			} break;
			/* 选择失效项, f1 */
			case R.id.inval_sel:{
				closeMenu = false;
				for (int i = 0; i < szf1; i++) {
					String path = f1.getPathAt(i);
					if (!path.startsWith(CMN.Assets) && !new File(path).exists()) {
						f1.setPlaceSelected(i, !isLongClicked);
						cnt++;
					}
				}
				if (cnt>0) {
					if (sf1) opt.setDictManager1MultiSelecting(true);
					f1.dataSetChanged();
					showT("已处理"+cnt+"项");
				}
			} break;
			/* 选择禁用, f1 */
			case R.id.inena_sel:{
				closeMenu = false;
				for (int i = 0; i < szf1; i++) {
					if (f1.getPlaceRejected(i)) {
						f1.setPlaceSelected(i, !isLongClicked);
						cnt++;
					}
				}
				if (cnt>0) {
					if (sf1) opt.setDictManager1MultiSelecting(true);
					f1.dataSetChanged();
					showT("已处理"+cnt+"项");
				}
			} break;
			/* 删除记录, f1 */
			case R.id.del_rec:{
				new AlertDialog.Builder(BookManager.this)
						.setTitle(mResource.getString(R.string.surerrecords, f1.Selection.size()))
						.setMessage("从当前分组删除记录，不会删除文件或全部词典记录，但不可撤销。")
						.setPositiveButton(R.string.confirm, (dialog, which) -> {
							for (int i = szf1-1; i >= 0; i--) {
								if (f1.getPlaceSelected(i)) {
									f1.remove(i);
								}
							}
							f1.refreshSize();
							f1.dataSetChanged();
							dialog.dismiss();
						})
						.create().show();
			} break;
			/* 另存为 */
            case R.id.toolbar_action3:{
				if(isLongClicked) {ret=false; break;}
				final String oldFn = opt.getLastPlanName("LastPlanName");
				showRenameDialog(oldFn,new transferRunnable() {
					@Override
					public boolean transfer(File to) {
						String newItem = to.getName().substring(0,to.getName().length()-4);
						boolean append=false;
						if(to.exists()) {//文件覆盖已预先处理。
							append = f2.adapter.getPosition(newItem)==-1;
						}
						
						boolean ret = try_write_configureLet(to);
						if(ret) {
							if(append) f2.adapter.add(newItem);
							show(R.string.saveas_done);
						}
						return ret;
					}

					@Override
					public void afterTransfer() {
					}
				});
			} break;
			/* 禁用选中项 | 启用选中项 */
            case R.id.toolbar_action5:{
				if(opt.getDictManager1MultiSelecting()){
					for (int i = 0; i < szf1; i++) {
						if(f1.getPlaceSelected(i))
							f1.setPlaceRejected(i, !isLongClicked);
					}
				}
				f1.refreshSize();
				f1.dataSetChanged();
			} break;
			/* 禁用全部 | 启用全部 | 添加全部词典 */
            case R.id.toolbar_action4:
			case R.id.remPagePos:
			{
				disenaddAll(item.getTitle().toString(), isLongClicked, item.getItemId()==R.id.toolbar_action4, PDICMainAppOptions.getWarnDisenaddAll());
			} break;
			/* 折叠全部 */
            case R.id.toolbar_action13:{
				if(isLongClicked) {ret=false; break;}
				for(int i=0;i<list.size();i++) {
					mFile mdTmp = list.get(i);
					if(mdTmp.isDirectory()) {
						f3.hiddenParents.add(mdTmp);
						for(i++;i<list.size();i++) {
							if(!mFile.isDirScionOf(list.get(i), mdTmp)) {break;}
							if(list.get(i).isDirectory()) {break;}
							mdTmp.children.add(list.remove(i));
							i--;
						}
						i--;
					}
				}
				f3.adapter.notifyDataSetChanged();
			} break;
			/* 展开全部 */
            case R.id.tapSch:{
				if(isLongClicked) {ret=false; break;}
				for(int i=0;i<list.size();i++) {
					f3.hiddenParents.clear();
					mFile mdTmp = list.get(i);
					if(mdTmp.isDirectory()) {
						list.addAll(i + 1, mdTmp.children);
						i += mdTmp.children.size();
						mdTmp.children.clear();
					}
				}
				f3.adapter.notifyDataSetChanged();
			} break;
			/* 全选 | 全不选, f3 & f4 */
            case R.id.toolbar_action7:{
				if (isLongClicked) {
					f3.Selection.clear();
				} else {
					f3.enterSelectionMode();
					closeMenu = false;
					for(int i=0;i<list.size();i++) {
						f3.Selection.add(list.get(i).getRealPath());
					}
				}
				f3.adapter.notifyDataSetChanged();
			} break;
			/* 间选 */
            case R.id.inter_sel:{
				closeMenu = false;
				int[] positions = f3.lastClickedPos;
				if (positions[0] != -1 && positions[1] != -1) {
					int start = positions[0];
					int end = positions[1];
					if (end < start) {
						int tmp = end;
						end = start;
						start = tmp;
					}
					for (int i = start; i <= end; i++) {
						if (!list.get(i).getIsDirectory()) {
							if (!isLongClicked) {
								f3.Selection.add(list.get(i).getRealPath());
							} else {
								f3.Selection.remove(list.get(i).getRealPath());
							}
						}
					}
				}
				f3.adapter.notifyDataSetChanged();
			} break;
			/* 全选失效项 */
            case R.id.toolbar_action8:{
				closeMenu = false;
				for(int i=0;i<list.size();i++) {
					mFile fI = list.get(i);
					if(fI.webAsset==null && !fI.getPath().startsWith(CMN.Assets)
							&& !fI.exists()
							&& !fI.getIsDirectory()
					) {
						if (!isLongClicked) {
							f3.Selection.add(fI.getRealPath());
							f3.enterSelectionMode();
						} else {
							f3.Selection.remove(fI.getRealPath());
						}
						cnt++;
					}
				}
				if (cnt>0) {
					f3.adapter.notifyDataSetChanged();
					showT("已处理"+cnt+"项");
				}
			} break;
			/* 添加 */
            case R.id.toolbar_action9:{ // add_selection_to_set
				if(isLongClicked) {/* 添加到第几行 */
					AlertDialog.Builder builder2 = new AlertDialog.Builder(BookManager.this);
					View dv = getLayoutInflater().inflate(R.layout.dialog_move_to_line, null);
					NumberPicker np = dv.findViewById(R.id.numberpicker);
					CheckBox checker1 = dv.findViewById(R.id.check1);
					CheckBox checker2 = dv.findViewById(R.id.check2);
					np.setMaxValue(szf1);
					AlertDialog dTmp = builder2.setView(dv).create();
					dv.findViewById(R.id.confirm).setOnClickListener(v -> {
						int toPos = Math.min(szf1, np.getValue());
						int cc = 0;
						mFile[] arr = f3.Selection.toArray(new mFile[0]);
						Arrays.sort(arr);
						final boolean insert = checker1.isChecked();
						final boolean select = checker2.isChecked();
						HashSet<String> map = new HashSet<>(arr.length);
						ArrayList<mFile> files = new ArrayList<>(arr.length);
						for (int i = 0; i < arr.length; i++) {
							mFile fn = arr[i];
							if (!fn.getIsDirectory()) {
								files.add(fn);
								String key = fn/*.getRealPath()*/.getPath();
								if(insert||select) map.add(key);
							}
						}
						if(insert) {
							f1.markDirty(-1);
							for (int i = 0; i < f1.manager_group().size(); i++) {
								if (map.contains(f1.getPathAt(i))) {
									f1.setPlaceRejected(i, false);
									f1.replace(i, -1); // remove first
									i--;
								}
							}
							for (int i = 0; i < files.size(); i++) {
								mFile fn = files.get(i);
								if (fn.exists() || fn.webAsset!=null) {
									cc++;
								}
								String key = fn.getRealPath().getPath();
								//loadMan.lazyMan.newChair();
								BookPresenter mdTmp = mdict_cache.get(key);
								PlaceHolder placeHolder;
								if (mdTmp!=null && mdTmp.getClass()!=BookPresenter.class) {
									mdTmp = null;
								}
								if (mdTmp==null) {
									//m = new_MagentTransient(key, opt, 0, false);
									placeHolder = new PlaceHolder(key);
								} else {
									placeHolder = mdTmp.placeHolder;
								}
								loadMan.md.add(toPos+i, mdTmp);
								loadMan.lazyMan.placeHolders.add(toPos+i, placeHolder);
								loadMan.lazyMan.chairCount++;
								if(select){
									f1.setPlaceSelected(toPos+i, true);
								}
							}
							f1.refreshSize();
						}
						else if (select) {
							for (int i = 0; i < szf1; i++) {
								if (map.contains(f1.getPathAt(i))){
									f1.setPlaceSelected(i, true);
									cc++;
								}
							}
						}
						f1.dataSetChanged();
						if(insert) {
							f1.getListView().setSelectionFromTop(toPos, 0);
						}
						viewPager.setCurrentItem(0);
						showT((insert?"添加完毕!(":"已选中!(")+cc+"/"+files.size()+")");
						toolbar.getMenu().close();
						dTmp.dismiss();
					});

					Window win = dTmp.getWindow();
					win.setBackgroundDrawableResource(GlobalOptions.isDark?R.drawable.popup_shadow_ld:R.drawable.popup_shadow_l);
					dTmp.show();
					ViewGroup dvp = win.getDecorView().findViewById(R.id.dialog);
					dvp.setPadding(0,0,0,0);
					dv.setPadding((int) (15*opt.dm.density), 0,0,(int) (10*opt.dm.density));
					closeMenu=false;
				}
				else { // 添加到末尾
					int cc = 0;
					mFile[] arr = f3.Selection.toArray(new mFile[0]);
					Arrays.sort(arr);
					int count=arr.length;
					
					HashMap<String, Integer> map = new HashMap<>(szf1);
					for (int i = 0; i < szf1; i++) {
						map.put(f1.getPathAt(i), i);
					}
					for(int i=0;i<arr.length;i++) {
						mFile fn=arr[i];
						if(fn.isDirectory()) {
							count--;
							continue;
						}
						String key = fn/*.getRealPath()*/.getPath();
						Integer idx = map.get(key);
						if(idx!=null) { // 已经有了
							f1.setPlaceRejected(idx, false);
						}
						else {
							f1.markDirty(-1);
							//loadMan.lazyMan.newChair();
							BookPresenter mdTmp = mdict_cache.get(key);
							PlaceHolder placeHolder;
							if (mdTmp!=null && mdTmp.getClass()!=BookPresenter.class) {
								mdTmp = null;
							}
							if (mdTmp==null) {
								placeHolder = new PlaceHolder(key);
							} else {
								placeHolder = mdTmp.placeHolder;
							}
							loadMan.md.add(mdTmp);
							loadMan.lazyMan.placeHolders.add(placeHolder);
							loadMan.lazyMan.chairCount++;
							cc++;
						}
					}
					showT("添加完毕!("+cc+"/"+count+")");
					f1.refreshSize();
					f1.dataSetChanged();
				}
			} break;
			/* 清理 */
            case R.id.toolbar_action10:{
				if(isLongClicked) {
					final View dv = inflater.inflate(R.layout.dialog_sure_and_all, null);
					CheckBox ck = dv.findViewById(R.id.ck);
					ck.setChecked(PDICMainAppOptions.getDelRecApplyAll());
					dv.findViewById(R.id.title).setOnClickListener(v -> ck.toggle());
					AlertDialog.Builder builder2 = new AlertDialog.Builder(BookManager.this);
					builder2.setView(dv).setTitle(getResources().getString(R.string.surerrecords, f3.Selection.size()))
							.setMessage("从分组和全部词典记录中彻底清理记录，不可撤销！")
							.setPositiveButton(R.string.confirm, (dialog, which) -> {
								mFile[] arr = f3.Selection.toArray(new mFile[0]);
								HashSet<mFile> removePool = new HashSet<>(Arrays.asList(arr));
								int s2 = list.size();
								for (int i = 0; i < s2; i++) {
									mFile fn = list.get(i)/*.getRealPath()*/;
									if (fn.webAsset==null) {
										if (fn instanceof mAssetFile)
											continue;
										if (fn.isDirectory())
											continue;
										if (removePool.contains(fn)) {
											list.remove(i--);
											s2--;
											mdlibsCon.remove(mFile.tryDeScion(fn, opt.lastMdlibPath));
											f3.isDirty = true;
											mFile p = fn.getParentFile();
											if (p != null) {
												mFile pTmp = i<list.size()?list.get(i):null;
												if (p.equals(pTmp) && pTmp.children.size()==0
														&& (i+1>=list.size() || !mFile.isDirScionOf(list.get(i+1), p))) {
													list.remove(i--);
													s2--;
												}
											}
										}
									}
								}
								PDICMainAppOptions.setDelRecApplyAll(ck.isChecked());
								deleteRecordsHard(f3, false);
								if (!PDICMainAppOptions.getDebuggingRemoveRec()) {
									ArrayList<File> moduleFullScannerArr = ScanInModlueFiles(PDICMainAppOptions.getDelRecApplyAll(), f3.isDirty);
									AgentApplication app = ((AgentApplication) getApplication());
									char[] cb = app.get4kCharBuff();
									boolean bNeedRewrite;
									for (File fI : moduleFullScannerArr) {
										StringBuilder sb = new StringBuilder();
										String line;
										try {
											ReusableBufferedReader br = new ReusableBufferedReader(new FileReader(fI), cb, 4096);
											bNeedRewrite = false;
											while ((line = br.readLine()) != null) {
												try {
													String key = line;
													if(key.startsWith("[:")){
														int idx = key.indexOf("]",2);
														if(idx>=2) key = key.substring(idx+1);
													}
													key = key.startsWith("/") ? key : opt.lastMdlibPath + "/" + key;
													//CMN.debug("rewrite::", key, removePool.contains(new mFile(key)));
													if (removePool.contains(new mFile(key)) || removePool.contains(new mFile(new File(key).getCanonicalPath()))) {
														bNeedRewrite = true;
														continue;
													}
												} catch (Exception e) {
													CMN.debug(e);
												}
												sb.append(line).append("\n");
											}
											br.close();
											cb = br.cb;
											if (bNeedRewrite) {
												CMN.debug("rewrite::", fI);
												ReusableBufferedWriter bw = new ReusableBufferedWriter(new FileWriter(fI), cb, 4096);
												bw.write(sb.toString());
												bw.flush();
												bw.close();
												cb = br.cb;
											}
										} catch (Exception e) {
											CMN.debug(e);
										}
									}
									app.set4kCharBuff(cb);
								}
								showT("移除完毕!");
							})
							.setNeutralButton(R.string.cancel, null);
					d = builder2.create();
					d.show();
				} else {
					int cc1 = 0;
					for(int i = 0; i< szf1; i++) {
						if(f3.Selection.contains(new mFile(f1.getPathAt(i)))) {
							f1.markDirty(-1);
							f1.setPlaceRejected(i, true);
							cc1++;
						}
					}
					f1.refreshSize();
					showT("移除完毕!("+cc1+"/"+f3.calcSelectionSz()+")");
					f1.dataSetChanged();
				}
			} break;
			/* 删除记录 */
            case R.id.tintList:{
				if(isLongClicked) {
					ret=false;break;
				}
				else {
					new AlertDialog.Builder(BookManager.this)
							.setTitle(mResource.getString(R.string.surerrecords, f3.calcSelectionSz()))
							.setMessage("从当前分组删除记录，不会删除文件或全部词典记录，但不可撤销。")
							.setPositiveButton(R.string.confirm, (dialog, which) -> {
								deleteRecordsHard(f3, true);
								dialog.dismiss();
							})
							.create().show();
				}
			} break;
			/* 移动文件 */
            case R.id.peruseMode:{
            	if (true) {
            		showT("功能关闭，请等待6.0版本");
					closeMenu = false;
					ret=false;break;
				}
//				if(isLongClicked) {
//					ret=false;break;
//				}
//				else {
//					DialogProperties properties = new DialogProperties();
//					properties.selection_mode = DialogConfigs.SINGLE_MODE;
//					properties.selection_type = DialogConfigs.DIR_SELECT;
//					properties.root = new File("/");
//					properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
//					properties.offset = opt.lastMdlibPath;
//					properties.opt_dir = new File(opt.pathToDatabases() + "favorite_dirs/");
//					properties.opt_dir.mkdirs();
//					FilePickerDialog dialog = new FilePickerDialog(this, properties);
//					dialog.setTitle(R.string.pickdestineFolder);
//					dialog.setDialogSelectionListener(new DialogSelectionListener() {
//						@Override
//						public void
//						onSelectedFilePaths(String[] files, File n) {
//							File p = new File(files[0]);//新家
//							if (p.isDirectory()) {
//								ArrayList<String> arr = f3.Selection.flatten();
//								RashSet<String> renameList = new RashSet<>();
//								ArrayList<String> renameListe;
//								HashMap<String, mngr_agent_manageable> mdict_cache = new HashMap<>(f1.manager_group().size());
//								for(int i=0;i<f1.manager_group().size();i++) {
//									if (loadMan.md.get(i)!=null) {
//										mdict_cache.put(f1.getPathAt(i), loadMan.md.get(i));
//									}
//								}
////								for (mngr_agent_manageable mmTmp : mdmng) {
////									//if (mmTmp instanceof mdict)
////
////								}
//								//todo 保证mdict移动文件的同时性。
//								int cc = 0;
//								for (String sI : arr) {//do actual rename. rename a lot of files..
//									mFile mF = new mFile(sI).init(opt);
//									//ommitting directory.
//									//if(sI.startsWith("/ASSET/") && CMN.AssetMap.containsKey(sI)) continue;
//									if (mF.isDirectory()) continue;
//									if (f3.data.get(mF).isDirectory()) continue;
//									mngr_agent_manageable mmTmp = mdict_cache.get(sI);
//									if (mmTmp == null) {
//										mmTmp = new_MagentTransient(sI, opt, null, true);
//									}
//									File OldF = mmTmp.f();
//									String OldFName = mmTmp.getDictionaryName();
//									File toF = new File(p, OldF.getName());
//									boolean ret = mmTmp.moveFileTo(BookManager.this, toF);
//									//CMN.Log("移动？？？", ret, toF);
//									if (ret) {
//										RebasePath(OldF, OldFName, toF, null, OldF.getName());
//										mdlibsCon.remove(mFile.tryDeScion(OldF, opt.lastMdlibPath));
//										mdlibsCon.add(mFile.tryDeScion(toF, opt.lastMdlibPath));
//										f3.Selection.remove(sI);//移出f3的选择
//										renameList.put(sI);//然后记录
//										cc++;
//									}
//								}
//								mdict_cache.clear();
//								f1.markDirty();
//								renameListe = renameList.flatten();
//								for (String fnI : renameListe) {
//									mFile fOld = new mFile(fnI).init(opt);
//									int idx = f3.data.remove(fOld);
//									if (idx != -1) {
//										mFile p2 = fOld.getParentFile().init(opt);
//										if (p2 != null) {
//											int idx2 = f3.data.indexOf(p2);
//											if (idx2 != -1) {//如有必要，移除多余的父文件夹
//												if (idx2 == f3.data.size() - 1 || !mFile.isDirScionOf(f3.data.getList().get(idx2 + 1), p2))
//													f3.data.getList().remove(idx2);
//												f3.data.OverFlow.remove(p2);
//											}
//											//showT(System.currentTimeMillis()+" "+idx2);
//										} else {
//											f3.data.OverFlow.remove(p2);
//											//f3.data.OverFlow.clear();
//										}
//										//f3.data.OverFlow.clear();
//										mFile val = new mFile(p, new File(fnI).getName());
//										f3.data.insert(val.init(opt));
//										f3.Selection.insert(val.getAbsolutePath());
//										if (!mFile.isDirScionOf(val, opt.lastMdlibPath))
//											f3.data.insertOverFlow(val.getParentFile().init(opt));
//									}
//								}
//								f3.adapter.notifyDataSetChanged();
//								f3.isDirty = true;
//
//								ArrayList<File> moduleFullScannerArr = ScanInModlueFiles(true, true);
//								if(DefordFile.length()>0) moduleFullScannerArr.add(DefordFile);
//								HashSet<String> mdlibs = new HashSet<>();
//								AgentApplication app = ((AgentApplication) getApplication());
//								char[] cb = app.get4kCharBuff();
//								for (File fI : moduleFullScannerArr) {
//									boolean modified = false;
//									mdlibs.clear();
//									StringBuilder sb = new StringBuilder();
//									String line;
//									try {
//										ReusableBufferedReader br = new ReusableBufferedReader(new FileReader(fI), cb, 4096);
//										while ((line = br.readLine()) != null) {
//											String key = line;
//											String prefix = null;
//											try {
//												if(key.startsWith("[:")){
//													int idx = key.indexOf("]",2);
//													if(idx>=2) {
//														idx+=1;
//														prefix = key.substring(0, idx);
//														key = key.substring(idx);
//													}
//												}
//												line = key;
//												key = key.startsWith("/") ? key : (opt.lastMdlibPath + "/" + key);
//												if (renameList.contains(key)) {// 搬到新家
//													modified = true;
//													key = mFile.tryDeScion(new File(p, new File(key).getName()), opt.lastMdlibPath);
//												} else { //复原
//													key = line;
//												}
//											} catch (Exception ignored) { }
//											if (!mdlibs.contains(key)) {
//												mdlibs.add(key);
//												if (prefix!=null)
//													key = prefix + key;
//												sb.append(key).append("\n");
//											} else {
//												modified = true;
//											}
//										}
//										br.close();
//										cb = br.cb;
//										if (modified) {
//											ReusableBufferedWriter bw = new ReusableBufferedWriter(new FileWriter(fI), cb, 4096);
//											bw.write(sb.toString());
//											bw.flush(); bw.close();
//											cb = br.cb;
//										}
//									}
//									catch (IOException e) {
//										CMN.Log(e);
//									}
//								}
//								app.set4kCharBuff(cb);
//								renameList.clear();
//								renameListe.clear();
//							}
//						}
//
//						@Override
//						public void onEnterSlideShow(Window win, int delay) {
//
//						}
//
//						@Override
//						public void onExitSlideShow() {
//
//						}
//
//						@Override
//						public Activity getDialogActivity() {
//							return null;
//						}
//
//						@Override
//						public void onDismiss() {
//
//						}
//					});
//					dialog.show();
//				}
			} break;
			/* 新建 */
            case R.id.toolbar_action15:{
				ViewGroup dv1 = (ViewGroup) getLayoutInflater().inflate(R.layout.fp_edittext, null);
				final EditText etNew = dv1.findViewById(R.id.edt_input);
				final View btn_Done = dv1.findViewById(R.id.done);
				AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(dv1);
				final AlertDialog dd = builder.create();
				btn_Done.setOnClickListener(v -> {
					File source  = new File(ConfigFile, SU.legacySetFileName(etNew.getText().toString()));
					if(!mFile.isDirScionOf(source, ConfigFile)) {
						showT("名称无效！");
						return;
					}
					if(source.exists()) {
						showT("错误：文件已经存在！");
						return;
					}
					try {
						source.createNewFile();
						f2.add(etNew.getText().toString());
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
	
	// 缝合怪
	private void disenaddAll(String title, boolean isLongClicked, boolean dis, boolean warnDisenaddAll) {
		if (!warnDisenaddAll) {
			int szf1 = f1.manager_group().size();
			if (isLongClicked) {
				if (!dis) {
					try {
						BufferedReader in = new BufferedReader(new FileReader(fRecord));
						String line;
						HashMap<String, mngr_agent_manageable> mdict_cache = new HashMap<>(szf1);
						for (int i = 0; i < szf1; i++) {
							mdict_cache.put(f1.getPathAt(i), null);
						}
						while ((line = in.readLine()) != null) {
							if (!line.startsWith("/"))
								line = opt.lastMdlibPath + "/" + line;
							line = new File(line).getAbsolutePath();
							if (!mdict_cache.containsKey(line)) {
								f1.add(line);
								f1.setPlaceRejected(f1.manager_group().size() - 1, true);
							}
						}
						//mTabLayout.getTabAt(0).setText(getResources().getString(R.string.currentPlan,md.size()-f1.rejector.size()));
						in.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			} else {
				for (int i = 0; i < szf1; i++) {
					f1.setPlaceRejected(i, dis);
				}
			}
			f1.dataSetChanged();
			f1.refreshSize();
		} else {
			final View dv = inflater.inflate(R.layout.dialog_sure_and_all, null);
			CheckBox ck = dv.findViewById(R.id.ck);
			ck.setChecked(!PDICMainAppOptions.getWarnDisenaddAll());
			TextView tv = dv.findViewById(R.id.title);
			tv.setOnClickListener(v1 -> ck.toggle());
			tv.setText("重启前不再提示");
			AlertDialog.Builder builder2 = new AlertDialog.Builder(BookManager.this);
			int sep = title.indexOf("|");
			if (sep>0) title=isLongClicked?title.substring(sep + 1):title.substring(0, sep);
			builder2.setView(dv).setTitle("确认"+title+"吗？")
					.setPositiveButton(R.string.confirm, (dialog, which) -> {
						disenaddAll(null, isLongClicked, dis, false);
						PDICMainAppOptions.setWarnDisenaddAll(!ck.isChecked());
					})
					.setNeutralButton(R.string.cancel, null);
			d = builder2.create();
			d.show();
		}
	}
	
	public MagentTransient new_MagentTransient(Object key, PDICMainAppOptions opt, Integer isF, boolean bIsPreempter) {
		try {
			return new MagentTransient(this, key, opt, isF, bIsPreempter);
		} catch (IOException e) {
			throw new RuntimeException(MagentTransient.class.toString());
		}
	}
	
	public ArrayList<File> ScanInModlueFiles(boolean all, boolean addAllLibs) {
		CMN.debug("ScanInModlueFiles::", all, "addAllLibs="+addAllLibs);
		ArrayList<File> ret = new ArrayList<>();
		if (all) {
			String[] names = ConfigFile.list();
			if(names!=null) {
				ret.ensureCapacity(names.length);
				for (int i = 0; i < names.length; i++) {
					String name = names[i];
					if(!SU.isNotGroupSuffix(name)) {
						ret.add(new File(ConfigFile, name));
					}
				}
			}
		}
		if (addAllLibs) {
			ret.add(fRecord);
		}
		return ret;
	}
	
	private void deleteRecordsHard(BookManagerFolderAbs f3, boolean toast) {
		int cc1 = 0;
		int size = f1.manager_group().size();
		int total = f3.calcSelectionSz();
		for(int i=0;i<size;i++) {
			if(f3.Selection.remove(new mFile(f1.getPathAt(i)))) {
				f1.markDirty(-1);
				f1.replace(i--, -1);
				size--;
				cc1++;
			}
		}
		f1.refreshSize();
		if (toast) {
			showT("移除完毕!("+cc1+"/"+total+")");
		}
		f1.dataSetChanged();
		f3.adapter.notifyDataSetChanged();
	}
	
	public void RebasePath(File oldPath, String OldFName, File newPath, String MoveOrRename, String oldName){
    	if (true) {
    		showT("功能关闭，请等待5.0版本");
    		return;
		}
		BookPresenter mdTmp = app_mdict_cache.remove(oldPath.getPath());
		File p = oldPath.getParentFile();
		File p2 = newPath.getParentFile();
		boolean move = MoveOrRename==null;

		if(p!=null){
			int cc=0;
			File f2;
			if(move){//移动
				CMN.Log("//移动ASDASD", new File(p, OldFName+"."+(cc++)+".mdd"));
				String fName;
				while((f2 = new File(p, fName=OldFName+(cc==0?"":"."+cc)+".mdd")).exists()){
					if(FU.move3(this, f2, new File(p2, fName))<0)
						break;
					++cc;
				}
			} else {
				//CMN.Log("//重命名ASDASD", new File(p, OldFName+((cc==0?"":"."+cc)+".mdd")).exists());
				//CMN.Log("//重命名ASDASD", FU.exsists(this, f2 = new File(p, OldFName+((cc==0?"":"."+cc)+".mdd"))), f2);
				String fName;
				while((f2 = new File(p, OldFName+(fName=(cc==0?"":"."+cc)+".mdd"))).exists()){
					if(FU.rename5(this, f2, new File(p2, MoveOrRename+fName))<0)
						break;
					++cc;
				}
			}
		}
		if(mdTmp!=null){
			CMN.Log("RebasePath!!!");
			// nmg
			//mdTmp.Rebase(newPath);
			app_mdict_cache.put(newPath.getPath(), mdTmp);
		}
		if(!move){
			//byte[] data = UIProjects.remove(oldName);
			//if(data!=null){
			//	String name = newPath.getName();
			//	UIProjects.put(name, data);
			//	dirtyMap.add(name);
			//}
		}
	}
	
	@NonNull
	public BookPresenter getMagentAt(int position, boolean create) {
		BookPresenter ret = loadMan.md.get(position);
		if (ret==null) {
			PlaceHolder ph = loadMan.lazyMan.placeHolders.get(position);
			String key = ph.getPath(opt).toString();
			ret = mdict_cache.get(key);
			if (ret==null) {
				ret = create?new_MagentTransient(key, opt, null, true):loadMan.EmptyBook;
				if (ret!=null) {
					ret.tmpIsFlag = ph.tmpIsFlag;
				}
			}
		}
		return ret;
	}
	
	public BookPresenter getMagentAt(int position) {
		return getMagentAt(position, true);
	}
}



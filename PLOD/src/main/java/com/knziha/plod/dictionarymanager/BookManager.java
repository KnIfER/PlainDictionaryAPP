package com.knziha.plod.dictionarymanager;

import static com.knziha.plod.db.LexicalDBHelper.Hzh_filesize;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.ListFragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.knziha.filepicker.utils.FU;
import com.knziha.plod.PlainUI.PasteBinHub;
import com.knziha.plod.PlainUI.PopupMenuHelper;
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
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ViewUtils;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class BookManager extends Toastable_Activity implements OnMenuItemClickListener, OnClickListener, PopupMenuHelper.PopupMenuListener {
	public final static int id = 110;
	HashMap<String, BookPresenter> mdict_cache = new HashMap<>();
	Intent intent = new Intent();
	private PopupWindow mPopup;
	//public ArrayList<PlaceHolder> slots;
	public MainActivityUIBase.LoadManager loadMan;
	ArrayList<ListFragment> fragments;
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
	public Toolbar searchbar;
	private TextView schIndicator;
	public View schLstBtn;
	private int lastAddToPos;
	private boolean bReturning;
	private int f1_dirty;
	
	public BookManager() {
		mFile.bmRef = new WeakReference<>(this);
	}
	
	public File fileToSet(String name) {
		return opt.fileToSet(ConfigFile, name);
	}
	
	BookOptionsDialog bookOptionsDialog;
	public void showBookPreferences(BookPresenter...books) {
		if (bookOptionsDialog==null) bookOptionsDialog = new BookOptionsDialog();
		bookOptionsDialog.bookOptions.setContext(this);
		bookOptionsDialog.bookOptions.setData(books);
		try {
			if (!bookOptionsDialog.isAdded()) {
				bookOptionsDialog.show(getSupportFragmentManager(), "");
			} else {
				bookOptionsDialog.getDialog().show();
			}
		} catch (Exception ignored) { }
	}
	
	public void onBookOptionsSet(boolean set) {
		if (set) {
			f1.dataSetChanged(false);
		}
	}
	
	public void markDirty() {
		if (toolbar.mNavButtonView==toolbar.mNavButtonLayout) {
			ImageButton navBtn = toolbar.mNavButtonView;
			ImageButton cross = new ImageButton(this);
			cross.setContentDescription("取消修改");
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
								f1.dataSetChanged(true);
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
			cross.setColorFilter(tintListFilter.sForegroundFilter);
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
	
    private Toolbar toolbar;
    static String dictQueryWord;
    //private SearchView searchView;
    BookManagerMain f1;
    BookManagerModules f2;
    BookManagerFolderlike f3;
	BookManagerWebsites f4;
	int lastPage = 0;
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
		ListFragment frame = getFragment();
		if (frame != null && !bReturning) {
			if(((BookManagerFragment.SelectableFragment)frame).exitSelectionMode()){
				showT("已清除选择");
				return;
			}
			try {
				listPos[viewPager.getCurrentItem()] = ViewUtils.saveListPos(frame.getListView());
			} catch (Exception e) {
				CMN.debug(e);
			}
		}

		checkAll(); // 返回
		//CMN.Log("terminating...", intent, intent.getBooleanExtra("changed", false));
		super.onBackPressed();
	}

	private void checkF1() {
		f1.checkTweakedDict();
		if(f1.isDirty) {
			intent.putExtra("result", true);
			int result = checkModuleDirty(true);
			boolean isInitialModule = initialModuleName !=null && initialModuleName.equals(loadMan.lazyMan.lastLoadedModule);
			boolean identical = (result&0x1)!=0 && isInitialModule;
			if (isInitialModule && !initialModuleChanged) {
				CMN.debug("一成不变");
			} else {
				CMN.debug("变化了", (result&0x1)!=0, "isInitialModule="+isInitialModule);
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
		f1.checkTweakedDict();
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
		if (f1.dirtyAttrArray.size()>0) { // 属性变化啦！
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
		if (opt==null) return;
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
		mFile.bmRef.clear();
		checkAll(); // 退出
		if (viewPager!=null) {
			viewPager.clearOnPageChangeListeners();
			mTabLayout.clearOnTabSelectedListeners();
		}
	}

	private void writeForOneLine(Writer out, int position, String parent) throws IOException {
		String name = f1.getPathAt(position);
		if(name.startsWith(parent) && name.length()>parent.length())
			name = name.substring(parent.length()+1);
		int flag = f1.getPlaceFlagAt(position);
		if(flag!=0) {
			out.write("[");
			if(PDICMainAppOptions.getTmpIsFiler(flag))
				out.write(":F");
			else if(PDICMainAppOptions.getTmpIsAudior(flag))
				out.write(":A");
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
		tintListFilter = VU.sTintListFilter;
		
		AgentApplication agent = ((AgentApplication)getApplication());
		app_mdict_cache=agent.mdict_cache;
		//UIProjects=agent.BookProjects;
		dirtyMap=agent.dirtyMap;
		opt=agent.opt;
		loadMan = agent.loadManager;
		mdlibsCon=agent.mdlibsCon;
		
		if (opt == null) {
			finish();
			return;
		}
		
		ConfigFile = opt.fileToConfig();
		
		fRecord = opt.fileToDecords(ConfigFile);
		
		SecordFile = opt.fileToSecords(ConfigFile);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dict_manager_main);
		
        getWindowManager().getDefaultDisplay().getMetrics(opt.dm);

		root = findViewById(R.id.drawer_layout);
		MainAppBackground = GlobalOptions.isDark?Color.BLACK:opt.getMainBackground();
		root.setBackgroundColor(MainAppBackground);
		MainLumen = ColorUtils.calculateLuminance(MainAppBackground);
		
		searchbar = (Toolbar) VU.findViewById(root, R.id.searchbar);
		searchbar.inflateMenu(R.xml.menu_dict_manager_bottom);
		searchbar.setOnMenuItemClickListener(this);
		int barSzBot = (int) mResource.getDimension(R.dimen.barSzBot);//opt.getBottombarSize();
		searchbar.getChildAt(0).getLayoutParams().height = barSzBot;
		//searchbar.setNavigationIcon(R.drawable.ic_baseline_double_arrow_24);
		ImageView enterBtn = (ImageView) searchbar.findViewById(R.id.enter);
		enterBtn.setContentDescription("弹出显示搜索结果列表");
		enterBtn.setImageResource(R.drawable.ic_menu_drawer_24dp);
		enterBtn.setOnLongClickListener(new View.OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				PopupMenuHelper popup = getPopupMenu();
				popup.initLayout(new int[]{
					R.string.sel_filtered
					,R.string.sel_filtered_no
				}, null);
				View rv = root;
				int rw = rv.getWidth(), rh = rv.getHeight();
				popup.showAt(v, 0, (int) (v.getHeight()/2 + GlobalOptions.density*5), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
				return true;
			}
		});
		
		searchbar.setNavigationIcon(R.drawable.dragneo);
		VU.setOnClickListenersOneDepth(searchbar, this, 999, null);
		searchbar.mNavButtonView.setContentDescription("切换显示拖拽排序按钮");
		etSearch = searchbar.findViewById(R.id.etSearch);
		etSearch.setText(dictQueryWord);
		etSearch.setFilters(new InputFilter[] {
				new InputFilter.AllCaps() {
					@Override
					public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
						return String.valueOf(source).toLowerCase();
					}
				}
		});
		etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
					Fragment frame = getFragment();
					if (frame == f1 || frame == f2) {
						String query = v.getText().toString().trim()/*.toLowerCase()*/;
						dictQueryWord = query;
						int f1_cc = 0, f2_cc = 0;
						if (f1 != null) {
							f1_cc = f1.schFilter(dictQueryWord, true);
						}
						if (f3 != null && f3.adapter != null) {
							f2_cc = f3.schFilter(dictQueryWord, true);
						}
						showTopSnack(toastmaker, (f1_cc + f2_cc) == 0 ? getResources().getString(R.string.fn)
								: ((f1_cc == 0 ? "" : "当前分组找到" + f1_cc + "项") + (f1_cc == 0 ? "" : "，") + (f2_cc == 0 ? "" : "全部记录找到" + f2_cc + "项")));
						f1_cc += f2_cc;
					} else {
						searchbar.findViewById(R.id.recess).performClick();
					}
					schIndicator_setText(filtered);
				}
				return true;
			}
		});
		PDICMainAppOptions.schDictManager(true);
		VU.setVisible(searchbar, PDICMainAppOptions.schDictManager());
		schIndicator = searchbar.findViewById(R.id.indicator);
		schLstBtn = searchbar.findViewById(R.id.enter);
        
        viewPager = findViewById(R.id.viewpager);
        mTabLayout = findViewById(R.id.mTabLayout);
		inflater=LayoutInflater.from(getApplicationContext());
        toolbar = findViewById(R.id.toolbar);
		toolbar_setTitle(initialModuleName =loadMan.lazyMan.lastLoadedModule);
		final long resId = R.xml.menu_dict_manager;
        toolbar.inflateMenu((int)resId);
		AllMenus = (MenuBuilder) toolbar.getMenu();
		AllMenus.longclickable = false;
		AllMenus.contentDescriptor = new MenuBuilder.ContentDescriptor(){
			public CharSequence describe(MenuItemImpl menuItem, CharSequence value){
				if (menuItem.isCheckable()) {
					value = menuItem.getTitle() + (menuItem.isChecked()?" 已激活":" 未激活");
					if (menuItem.isChecked()) {
						if (menuItem.getItemId()==R.string.multi_select && f1.selected_size()>0) {
							value += " 当前分组已选" + f1.selected_size() + "项";
						}
					}
				}
				if (menuItem.getItemId()==R.id.clearSel) {
					int page = viewPager.getCurrentItem();
					value = menuItem.getTitle()+" ";
					if (page == 0) {
						value += f1.selected_size()==0? "当前分组无选中项": ("当前分组已选" + f1.selected_size() + "项");
					} else if (page == 1){
						value += f2.selected_size()==0?"未选分组" : ("已选" + f2.selected_size() + "个分组");
					}  else if (page == 3){
						value += f4.selected_size()==0?"未选网络词典" : ("已选" + f4.selected_size() + "个网络词典");
					}   else if (page == 4){
						value += f3.selected_size()==0?"全部词典无选中项" : ("全部词典已选" + f3.selected_size() + "项");
					}
				}
				return value;
			}
		};
		MenuCompat.setGroupDividerEnabled(AllMenus, true);
		AllMenus.mOverlapAnchor = PDICMainAppOptions.menuOverlapAnchor();
		AllMenus.checkActDrawable = mResource.getDrawable(PDICMainAppOptions.useOldColorsMode()?R.drawable.frame_checked_whiter:R.drawable.check_frame_transparent);
		AllMenus.checkActDrawable.setAlpha(10);
		AllMenus.multiColumn = 1;
		AllMenus.twoColumnFlipped = true;
		Menu1 = ViewUtils.MapNumberToMenu(AllMenus, 22, 24, 23, 0, 2, 1, 19, 18, 5, 20, 27, 3, 6, 21);
		Menu2 = ViewUtils.MapNumberToMenu(AllMenus, 24, 23, 0, 3, 15);
		Menu3 = ViewUtils.MapNumberToMenu(AllMenus, 28, 24, 26, 13, 14, 7, 8);
		Menu3Sel = ViewUtils.MapNumberToMenu(AllMenus, 28, 24, 26, 13, 14, 7, 17, 8, 9, 10, 11, 12);
		AllMenus.setItems(Menu1);
		
		if (PDICMainAppOptions.dictManagerClickPopup()) {
			ViewUtils.findInMenu(Menu1, R.id.popup).setChecked(true);
		}
		if (PDICMainAppOptions.dictManagerClickPopup1()) {
			ViewUtils.findInMenu(Menu3, R.id.popup1).setChecked(true);
		}
		if (PDICMainAppOptions.dictManager1MultiSelecting()) {
			ViewUtils.findInMenu(Menu1, R.string.multi_select).setChecked(true);
		}
  
		fragments= new ArrayList<>();
		
	    String[] tabTitle = {getResources().getString(R.string.currentPlan,0),getResources().getString(R.string.allPlans), "网络词典", "全部词典"};
		
		thisActType = MainActivityUIBase.ActType.BookManage;
		
		fragments.addAll(Arrays.asList(f1 = new BookManagerMain(), f2 = new BookManagerModules(), f4 = new BookManagerWebsites(), f3 = new BookManagerFolderlike()));
		f1.a=f2.a=f4.a=f3.a=this;
		filtered = f1.filtered;
		
		f3.oes = f4.oes = new BookManagerFolderlike.OnEnterSelectionListener() {
			public void onEnterSelection(BookManagerFolderAbs f, boolean enter){
				//AllMenus.setItems(enter?Menu3Sel:Menu3);
			}
			public int addIt(BookManagerFolderAbs f, final mFile fn) {
				boolean found=false;
				String path = fn.getRealPath().getAbsolutePath();
				for(int i=0;i<f1.manager_group().size();i++) {
					if(f1.getPathAt(i).equals(path)) {
						if(f1.getPlaceRejected(i)) {
							f1.setPlaceRejected(i, false);
							f1.dataSetChanged(false);
							return 1;
						}
						found=true;
						break;
					}
				}
				if(!found) {
					//show("adding new!"+fn.getAbsolutePath());
					f.listView.post(() -> f1.add(path));
					return 1;
				}
				else return 0;
			}
		};
		FragAdapter adapterf = new FragAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(adapterf);
	    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout) {
	    	@Override public void onPageSelected(int page) {
				ListFragment frame = fragments.get(lastPage);
				if (frame != null) {
					try {
						listPos[lastPage] = ViewUtils.saveListPos(frame.getListView());
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
				frame = fragments.get(page);
				viewPager.setOffscreenPageLimit(Math.max(viewPager.getOffscreenPageLimit(), Math.max(1+page, 1)));
				List<MenuItemImpl> menu;
				if(frame==f1) {
					menu = Menu1;
					if (loadMan.lazyMan.lastLoadedModule!=lastLoadedModule) {
						toolbar_setTitle(loadMan.lazyMan.lastLoadedModule);
					}
					filtered = f1.filtered;
					if ((f1_dirty&0x1)!=0) { // 重新检查
						f1_dirty&=~0x1;
						f1.checkDuplication(false);
					}
	    		} else if(frame==f2) {
					menu = Menu2;
					filtered = f2.filtered;
				} else if (frame == f3) {
					menu = f3.SelectionMode ? Menu3Sel : Menu3;
					filtered = f3.filtered;
				} else {
					menu = f4.SelectionMode ? Menu3Sel : Menu3;
					filtered = f4.filtered;
				}
				if (popup!=null) {
					popup.dismiss();
				}
				AllMenus.setItems(menu);
				super.onPageSelected(CurrentPage = page);
				opt.setDictManagerTap(CurrentPage);
				schIndicator_setText(filtered);
				lastPage = page;
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
		
		int indicatorColor = 0xe0000000|ViewUtils.getComplementaryColor(opt.getMainBackground())&0x00ffffff;
	    double lumen1 = ColorUtils.calculateLuminance(indicatorColor);
		double lumen2 = ColorUtils.calculateLuminance(opt.getMainBackground());
		if (Math.abs(lumen1-lumen2)<0.4) {
			indicatorColor = ColorUtils.blendARGB(indicatorColor, lumen2>0.5?Color.BLACK:Color.WHITE, 0.2f);
		}
	    mTabLayout.setSelectedTabIndicatorColor(indicatorColor);
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
		toolbar.setNavigationOnClickListener(this);
		toolbar.getNavigationBtn().setContentDescription("退出词典管理");
		toolbar.setAccessibilityDelegate(new View.AccessibilityDelegate(){
			@Override
			public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
				super.onPopulateAccessibilityEvent(host, event);
				CMN.debug("onPopulateAccessibilityEvent", host, event);
			}
		});
// 		MenuItem searchItem = ViewUtils.findInMenu(Menu3, R.id.action_search);
// 		searchItem.setShowAsAction(2);
// 		searchView = (SearchView) searchItem.getActionView();
// 		searchView.setSubmitButtonEnabled(false);
//        searchView.setOnSearchClickListener(v -> {
//        	LayoutParams lp = (LayoutParams) searchView.getLayoutParams();
//			lp.width=getResources().getDisplayMetrics().widthPixels*2/3;
//			lp.setMargins(0, 0, 50, 0);
//			searchView.requestLayout();
//        });
//		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//	        @Override
//	        public boolean onQueryTextSubmit(String query) {
//				dictQueryWord=query;
//        	    return true;
//	        }
//
//	        @Override
//	        public boolean onQueryTextChange(String newText) {
//	            return true;
//	        }
//	    });
//		searchView.setOnCloseListener(() -> {
//			dictQueryWord=null;
//			searchView.getLayoutParams().width=-2;
//			searchView.requestLayout();
//			if(f1.adapter!=null) f1.dataSetChanged();
//			if(f3.adapter!=null) f3.dataSetChanged();
//			return false;
//		});
//		if(dictQueryWord!=null){
//			searchView.mSearchSrcTextView.setText(dictQueryWord);
//			searchView.onSearchClicked(false);
//		}
//		searchView.mSearchSrcTextView.setTextColor(Color.WHITE);
//		searchView.mSearchSrcTextView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		Window win = getWindow();
		win.setSoftInputMode(MainActivityUIBase.softModeResize);

        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
        win.getDecorView().setSystemUiVisibility(
        		View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        if(Build.VERSION.SDK_INT>=21) {
			win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	        win.setStatusBarColor(Color.TRANSPARENT);
	        win.setNavigationBarColor(Color.BLACK);
        }
		intent.putExtra("id", CMN.id(loadMan));
		setResult(RESULT_OK, intent);
        agent.clearTmp();
		
		int color = tintListFilter.sForeground;
		ViewUtils.setForegroundColor(toolbar,    tintListFilter);
		ViewUtils.setForegroundColor(mTabLayout, tintListFilter);
		ViewUtils.setForegroundColor(searchbar,  tintListFilter);
		
		resetStatusForeground(null);
	}
	//onCreate结束
	
	
	// click
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.home:
				if (v==searchbar.getNavigationBtn()) {
					PDICMainAppOptions.sortDictManager(!PDICMainAppOptions.sortDictManager());
					if(f1!=null) f1.dataSetChanged(false);
					if(f2!=null) f2.dataSetChanged(false);
					if (PDICMainAppOptions.sortDictManager()) {
						showT("已在第一和第二管页面显示拖拽排序按钮\n当前处于第"+(viewPager.getCurrentItem()+1)+"页面");
					} else {
						showT("已收起拖拽排序按钮");
					}
					break;
				}
				bReturning = true;
				onBackPressed();
				bReturning = false;
			break;
			case R.id.enter: {
				showFilteredEntries(v);
			} break;
			case R.id.ivDeleteText: {
				if(etSearch.getText()!=null)
					etSearch.getText().clear();
			} break;
			case R.id.forward:
			case R.id.recess: {
				String str = etSearch.getText().toString().trim();
				BookManager.dictQueryWord = str;
				Fragment view = getFragment();
				if (view instanceof BookManagerFolderAbs) {
					((BookManagerFolderAbs) view).schPrvNxt(str, v.getId() == R.id.recess);
				} else {
					((BookManagerFragment) view).schPrvNxt(str, v.getId() == R.id.recess);
				}
			} break;
		}
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		return false;
	}
	
	public void schIndicator_setText(SparseArray<String> filtered) {
		if (schIndicator != null) {
			schIndicator.setText(filtered.size()==0?"":filtered.size()+"");
		}
	}
	
	SparseArray<String> filtered;
	PopupMenuHelper popup;
	BaseAdapter popupAdapter;
	public long[] popupPos = new long[4];
	public static long[] listPos = new long[4];
	
	private void showFilteredEntries(View v)
	{
		if (filtered.size() == 0) {
			showTopSnack("当前页面无搜索结果");
			if((f1_dirty&0x2)!=0 && getFragment()==f1) {
				f1_dirty&=~0x2;
				f1.checkDuplication(false);
			}
			return;
		}
		if(popup==null)
			popup = new PopupMenuHelper(BookManager.this, null, null);
		if (popup.getListener() != this || !(popup.tag1 instanceof ListView)) {
			popup.initLayout(ArrayUtils.EMPTY_INT_ARRAY, this);
			ListView lv = new ListView(BookManager.this);
			popup.lv.addView(lv);
			popup.tag1 = lv;
			lv.setAdapter(popupAdapter=new BaseAdapter() {
				@Override public int getCount() { return filtered.size(); }
				@Override public Object getItem(int position) { return null; }
				@Override public long getItemId(int position) { return 0;	}
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
//					MainActivityUIBase.ViewHolder vh;
//					if (convertView == null) {
//						vh = new MainActivityUIBase.ViewHolder(BookManager.this, R.layout.listview_item01, parent);
//						VU.setVisible(vh.preview, false);
//						VU.setVisible(vh.subtitle, false);
//					} else {
//						vh = (MainActivityUIBase.ViewHolder) convertView.getTag();
//					}
//
//					vh.title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
//					vh.title.setText(filtered.valueAt(position));
					
//					return vh.itemView;
					int realPos = filtered.keyAt(position);
					Fragment frame = getFragment();
					View ret = null;
					try {
						if (frame instanceof BookManagerFragment) {
							ret = ((BookManagerFragment) frame).adapter.getView(realPos, convertView, lv);
						} else {
							ret = ((BookManagerFolderAbs) frame).adapterAll.getView(realPos, convertView, lv);
						}
					} catch (Exception e) {
						TextView tv = new TextView(getBaseContext());
						tv.setText("ERROR!!! "+e);
						return tv;
					}
					if (ret.getTag() instanceof BookViewHolder) {
						((BookViewHolder)ret.getTag()).position = position;
					}
					else if (ret.getTag() instanceof BookManagerFolderAbs.ViewHolder) {
						((BookManagerFolderAbs.ViewHolder)ret.getTag()).position = position;
					}
					return ret;
				}
			});
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						Fragment frame = getFragment();
						if (frame instanceof BookManagerFragment) {
							((BookManagerFragment) frame).selectPos(filtered.keyAt(position), false);
						}
						if (frame instanceof BookManagerFolderAbs) {
							((BookManagerFolderAbs) frame).selectFilteredPos(filtered.keyAt(position));
						}
						popup.dismiss();
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			});
			lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					Fragment frame = getFragment();
					if(frame instanceof BookManagerFragment) {
						((BookManagerFragment) frame).pressedPos = filtered.keyAt(position);
						((BookManagerFragment) frame).pressedV = view;
						((BookManagerFragment) frame).showPopup(view, root);
					}
					if(frame instanceof BookManagerFolderAbs) {
						((BookManagerFolderAbs) frame).pressedPos = filtered.keyAt(position);
						((BookManagerFolderAbs) frame).pressedV = view;
						((BookManagerFolderAbs) frame).showPopup(view, root);
					}
					return true;
				}
			});
			popup.mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					popupPos[viewPager.getCurrentItem()] = ViewUtils.saveListPos(lv);
				}
			});
		}
		popup.sv.getBackground().setColorFilter(GlobalOptions.isDark?GlobalOptions.NEGATIVE_1:null);
		ListView lv = (ListView) popup.tag1;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) lv.getLayoutParams();
		((BaseAdapter) ((ListView) popup.tag1).getAdapter()).notifyDataSetChanged();
		View rv = root;
		int rw = rv.getWidth(), rh = rv.getHeight();
		lp.width = rw*2/3;
		lp.height = rh/2;
		popup.showAt(v, 0, (int) (v.getHeight()/2 + GlobalOptions.density*5), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		ViewUtils.restoreListPos(lv, popupPos[viewPager.getCurrentItem()]);
	}
	
	private void toolbar_setTitle(String lastLoadedModule) {
		if (lastLoadedModule!=null) {
			this.lastLoadedModule=lastLoadedModule;
			toolbar.setTitle(lastLoadedModule.endsWith(".set")?lastLoadedModule.substring(0,lastLoadedModule.length()-4):lastLoadedModule);
		}
	}
	
	protected void showRenameDialog(final String lastPlanName,final transferRunnable tr) {//哈哈这么长的代码。。。
		View dialog = getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
        final AlertController.RecycleListView lv = dialog.findViewById(R.id.lv);
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
				if (!found && fSearch.equals(name)) {
					found = true;
				} else {
					setsArr.add(new File(ConfigFile, name));
				}
			}
		}
        
        if(found) {
			setsArr.add(0,fSearchFile);//让它出现在第一项
		} else {
			et.setText(lastPlanName);//否则码上去~
		}
        
        lv.setAdapter(new BaseAdapter() {
			@Override public int getCount() { return setsArr.size(); }
			@Override public Object getItem(int position) { return setsArr.get(position); }
			@Override public long getItemId(int position) { return 0; }
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
	            holder.tv.setTextColor(AppBlack);
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
		final AlertDialog d = new AlertDialog.Builder(BookManager.this)
				.setView(dialog)
				.setIcon(R.mipmap.ic_directory_parent)
				.setNeutralButton(R.string.delete,null)
				.setPositiveButton("保存", null)
				.setNegativeButton(R.string.cancel,null)
				.create();
		// d.findViewById(android.R.id.button1)//todo opt

        d.setOnDismissListener(dialog1 -> tr.afterTransfer());
        d.show();
        iv.setOnClickListener(v ->
		{
			String newName = SU.trimStart(et.getText().toString());
			if(newName.equals("") || newName.contains("/"))
			{
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
			}
			else {
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
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> iv.performClick());
        // d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> d.dismiss());
		
		float pad = 2.8f * getResources().getDimension(R.dimen._50_) * (dm.widthPixels>GlobalOptions.realWidth?1:1.45f);
		lv.mMaxHeight = root.getHeight()>=2*pad?(int) (root.getHeight() - root.getPaddingTop() - pad):0;
		
	}
	
	
	public boolean isDebug=false;

    public static class FragAdapter extends FragmentPagerAdapter {
    	private List<? extends Fragment> mFragments;
    	public FragAdapter(FragmentManager fm,List<? extends Fragment> fragments) {
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
		boolean isLongClicked=mmi.isLongClicked!=0, longclickFx = PDICMainAppOptions.dictManagerFlipMenuCloumn()?!isLongClicked:isLongClicked;
		boolean ret=isLongClicked;
		boolean closeMenu=!isLongClicked;
		AlertDialog d;
		BookManagerFolderAbs f3=CurrentPage==2?f4:this.f3;
		ArrayList<mFile> list = f3.data.getList();
		int szf1 = f1.manager_group().size(), cnt=0;
		boolean sf1 = !opt.dictManager1MultiSelecting() && !isLongClicked && szf1>0;
		switch (item.getItemId()) {
			/* 搜索词典 */
			case R.id.action_search:{
				boolean show = !VU.isVisible(searchbar);
				VU.setVisible(searchbar, show);
				PDICMainAppOptions.schDictManager(show);
				if (show) {
					etSearch.requestFocus();
					searchbar.postDelayed(() -> imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED), 200);
				} else {
					etSearch.clearFocus();
					imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
				}
			} break;
			/* 切换f1多选模式 */
			case R.string.multi_select:{
				if(isLongClicked) {ret=false; break;}
				item.setChecked(!item.isChecked());
				PDICMainAppOptions.dictManager1MultiSelecting(item.isChecked());
				f1.dataSetChanged(false);
				showT(item.isChecked()?"多选模式已开启":"多选模式已关闭");
			} break;
			case R.id.refresh:{
				if(isLongClicked) {ret=false; break;}
				viewPager.setCurrentItem(0);
				f1.resumePos();
			} break;
			case R.id.clearSel:{
				if(isLongClicked) {ret=false; break;}
				try {
					Fragment frame = getFragment();
					if (frame instanceof BookManagerFragment.SelectableFragment) {
						((BookManagerFragment.SelectableFragment) frame).exitSelectionMode();
					}
					showT("已清空选择");
				} catch (Exception e) {
					CMN.debug(e);
				}
			} break;
			/* 切换all点击弹出菜单 */
			case R.id.popup: {
				if(isLongClicked) {ret=false; break;}
				item.setChecked(!item.isChecked());
				PDICMainAppOptions.dictManagerClickPopup(item.isChecked());
				showT(!item.isChecked()?"点击列表项"+(viewPager.getCurrentItem()==0?"切换启用和禁用状态"
						:viewPager.getCurrentItem()==1?"加载分组":"切换选中状态") //+ "\n长按弹出菜单"
						:"点击列表项弹出菜单"); // ，长按排序
			} break;
			case R.id.popup1:{
				if(isLongClicked) {ret=false; break;}
				item.setChecked(!item.isChecked());
				PDICMainAppOptions.dictManagerClickPopup1(item.isChecked());
				showT(item.isChecked()?"点击列表右侧按钮弹出菜单":"右侧按钮直接添加");
			} break;
			/* 词典选项 */
			case R.id.toolbar_action0:{
				if(!isLongClicked) f1.performLastItemLongClick();
			} break;
			/* 全选 | 全不选, f1 */
            case R.id.toolbar_action2:{
				closeMenu = true;
				if(sf1) opt.dictManager1MultiSelecting(true);
				for (int i = 0; i < szf1; i++) {
					f1.setPlaceSelected(i, !longclickFx);
				}
				f1.dataSetChanged(false);
			} break;
			/* 间选 | 间不选, f1 */
			case R.id.toolbar_action1:{
				closeMenu = true;
				//selectBetween(!isLongClicked, sf1);
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
						f1.setPlaceSelected(i, !longclickFx);
						if(sf1) {
							opt.dictManager1MultiSelecting(true);
							sf1 = false;
						}
					}
					f1.dataSetChanged(false);
				}
			} break;
			/* 选择失效项, f1 */
			case R.id.inval_sel:{
				closeMenu = true;
				for (int i = 0; i < szf1; i++) {
					String path = f1.getPathAt(i);
					if (!path.startsWith(CMN.Assets) && !new File(path).exists()) {
						f1.setPlaceSelected(i, !longclickFx);
						cnt++;
					}
				}
				if (cnt > 0) {
					if (sf1) opt.dictManager1MultiSelecting(true);
					f1.dataSetChanged(false);
					showT((longclickFx ? "不选当前词典分组失效项" : "选择当前词典分组失效项") + "\n已处理" + cnt + "项，当前已选" + f1.Selection.size() + "项");
				} else {
					showT("当前分组无失效词典。");
				}
			} break;
			/* 选择禁用, f1 */
			case R.id.inena_sel:{
				closeMenu = true;
				for (int i = 0; i < szf1; i++) {
					if (f1.getPlaceRejected(i)) {
						f1.setPlaceSelected(i, !longclickFx);
						cnt++;
					}
				}
				if (cnt>0) {
					if (sf1) opt.dictManager1MultiSelecting(true);
					f1.dataSetChanged(false);
					showT((longclickFx?"不选当前词典分组禁用项":"选择当前词典分组禁用项")+"\n已处理"+cnt+"项，当前已选"+f1.Selection.size()+"项");
				} else
					showT("当前分组未禁用词典，点击列表可禁用或启用。");
			} break;
			/* 禁用选中项 | 删除选中项, f1 */
			case R.id.del_rec:{
				if (longclickFx) {
					f1.deleteSelOrOne(false, false);
				} else {
					f1.disEna(true, true, -1, false);
				}
			} break;
			/* 启用网络词典 | 禁用网络词典, f1 */
			case R.id.webDisen:{
				closeMenu = true;
				int cc=0;
				for (int i = 0; i < szf1; i++) {
					if (CMN.getSuffix(f1.getPathAt(i)).equals(".web")) {
						f1.setPlaceRejected(i, longclickFx);
						cc++;
					}
				}
				if (cc > 0) {
					showT((longclickFx ? "已禁用" : "已启用") + cc + "个网络词典");
				} else {
					showT("当前词典无网络词典");
				}
				f1.dataSetChanged(false);
			} break;
			/* 另存为 */
            case R.id.toolbar_action3:{
				if(isLongClicked) {ret=false; break;}
				final String oldFn = opt.getLastPlanName("LastPlanName");
				showRenameDialog(oldFn,new transferRunnable() {
					@Override
					public boolean transfer(File to) {
						String newItem = to.getName().substring(0,to.getName().length()-4);
						boolean append=true;
						if(to.exists()) {//文件覆盖已预先处理。
							append = f2.adapter.getPosition(newItem)==-1;
						}
						
						boolean ret = try_write_configureLet(to);
						if(ret) {
							if(append) f2.adapter.add(to.getName());
							show(R.string.saveas_done);
						}
						return ret;
					}

					@Override
					public void afterTransfer() {
					}
				});
			} break;
			/* 启用选中项 */
            case R.id.toolbar_action5:{
				//disenSel(false, szf1, sf1);
				f1.disEna(true, false, -1, false);
			} break;
			/* 启用全部 | 禁用全部 */
            case R.id.disenAll:
			{
				disenaddAll(isLongClicked?"禁用全部":"启用全部", false, longclickFx, PDICMainAppOptions.getWarnDisenaddAll());
			} break;
			case R.id.addAll:
			{
				disenaddAll(item.getTitle().toString(), true, false, PDICMainAppOptions.getWarnDisenaddAll());
			} break;
			/* 折叠全部 */
            case R.id.toolbar_action13:{
				if(isLongClicked) {ret=false; break;}
				final ArrayList<mFile> listTree = f3.dataTree;
				for(int i=0;i<listTree.size();i++) {
					mFile mdTmp = listTree.get(i);
					if(mdTmp.isDirectory()) {
						f3.hiddenParents.add(mdTmp);
						for(i++;i<listTree.size();i++) {
							if(!mFile.isDirectChildrenOf(listTree.get(i), mdTmp)) {break;}
							if(listTree.get(i).isDirectory()) {break;}
							mdTmp.children.add(listTree.remove(i));
							i--;
						}
						i--;
					}
				}
				f3.dataSetChanged(false);
			} break;
			/* 展开全部 */
            case R.id.tapSch:{
				if(isLongClicked) {ret=false; break;}
				final ArrayList<mFile> listTree = f3.dataTree;
				for(int i=0;i<listTree.size();i++) {
					f3.hiddenParents.clear();
					mFile mdTmp = listTree.get(i);
					if(mdTmp.isDirectory()) {
						listTree.addAll(i + 1, mdTmp.children);
						i += mdTmp.children.size();
						mdTmp.children.clear();
					}
				}
				f3.dataSetChanged(false);
			} break;
			/* 全选 | 全不选, f3 & f4 */
            case R.id.toolbar_action7:{
				if (longclickFx) {
					f3.Selection.clear();
				} else {
					f3.enterSelectionMode();
					closeMenu = true;
					for(int i=0;i<list.size();i++) {
						f3.Selection.add(list.get(i).getRealPath());
					}
				}
				f3.dataSetChanged(false);
			} break;
			/* 间选 */
            case R.id.inter_sel:{
				closeMenu = true;
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
							if (isLongClicked) {
								f3.Selection.add(list.get(i).getRealPath());
							} else {
								f3.Selection.remove(list.get(i).getRealPath());
							}
						}
					}
				}
				f3.dataSetChanged(false);
			} break;
			/* 全选失效项 */
            case R.id.toolbar_action8:{
				closeMenu = true;
				for(int i=0;i<list.size();i++) {
					mFile fI = list.get(i);
					if(fI.webAsset==null && !fI.getPath().startsWith(CMN.Assets)
							&& !fI.exists()
							&& !fI.getIsDirectory()
					) {
						if (!longclickFx) {
							f3.Selection.add(fI.getRealPath());
							f3.enterSelectionMode();
						} else {
							f3.Selection.remove(fI.getRealPath());
						}
						cnt++;
					}
				}
				if (cnt > 0) {
					f3.dataSetChanged(false);
					showT((longclickFx ? "不选全部词典记录失效项" : "选择全部词典记录失效项") + "\n已处理" + cnt + "项，当前已选" + f3.Selection.size() + "项");
				} else {
					showT("全部词典无失效记录。");
				}
			} break;
			/* 插入至… */
            case R.id.toolbar_action9: { // add_selection_to_set
				if (getFragment() instanceof BookManagerFolderAbs) {
					BookManagerFolderAbs frame = (BookManagerFolderAbs) getFragment();
					addElementsToF1(frame, null, true, longclickFx, -1, null);
					if(longclickFx) {
						closeMenu=false;
					}
				}
			} break;
			/* f3->f1禁用选中项目/f3->f1删除选中项目 */
            case R.id.toolbar_action10:{
				if(longclickFx) {
					deleteFromF3Sel(f3);
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
					showT("禁用完毕!("+cc1+"/"+f3.calcSelectionSz()+")");
					f1.dataSetChanged(false);
				}
			} break;
			/* 删除全部记录, f3 */
			case R.string.remove_records:{
				if(isLongClicked) {
					closeMenu = false;
				} else {
					deleteFromF3SelHardInAll(f3);
				}
			} break;
			/* 移动文件 */
            case R.id.peruseMode:{
            	if (true) {
            		showT("功能关闭"); // ，请等待6.0版本
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
//								f3.dataSetChanged();
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
					String groupName = etNew.getText().toString();
					File source  = new File(ConfigFile, SU.legacySetFileName(groupName));
					if(!mFile.isDirectChildrenOf(source, ConfigFile)) {
						showT("名称无效！");
						return;
					}
					if(source.exists()) {
						showT("错误：文件已经存在！");
						return;
					}
					try {
						source.createNewFile();
						f2.add(groupName);
						dd.dismiss();
						return;
					} catch (Exception e) {
						CMN.debug(e);
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
	
	public void addElementsToF1(BookManagerFolderAbs f3, mFile[] set
			, boolean useSelection, boolean pickPosition, int initToPos
			, SparseIntArray rejectMap) {
		if((set==null || set.length==0) && (f3==null || f3.adapter==null || useSelection && f3.selected_size()==0)) {
			showT((f3==null?"":f3.getName())+"无选中项");
			return;
		}
		int szf1 = f1.manager_group().size(), cnt=0, tempSz = 1;
		String anyPreview = "";
		try {
			if (set != null) {
				anyPreview = set[0].getName();
				tempSz = set.length;
			} else {
				anyPreview = ((FlowTextView) f3.pressedV.findViewById(R.id.text)).getText();
				if (useSelection) {
					tempSz = f3.selected_size();
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		if (anyPreview.length() > 7) {
			anyPreview = anyPreview.substring(0, 7);
		}
		if (pickPosition) {
			/* 添加到第几行 */
			AlertDialog.Builder builder2 = new AlertDialog.Builder(BookManager.this).setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					showT("已取消");
				}
			});
			String msg = "将插入"+anyPreview+(tempSz==1?"":("等"+tempSz+"本词典"))+"，确认吗？";
			//builder2.setTitle(msg);
			showT(msg);
			View dv = getLayoutInflater().inflate(GlobalOptions.isDark?R.layout.dialog_move_to_line_d:R.layout.dialog_move_to_line, null);
			NumberPicker np = dv.findViewById(R.id.numberpicker);
			CheckBox checker1 = dv.findViewById(R.id.check1);
			CheckBox checker2 = dv.findViewById(R.id.check2);
			CheckBox checker3 = dv.findViewById(R.id.check3);
			CheckBox checker4 = dv.findViewById(R.id.check4);
			checker2.setChecked(PDICMainAppOptions.dictManagerTianXuan());
			checker4.setChecked(PDICMainAppOptions.dictManagerTianJinXuan());
			np.setMaxValue(szf1);
			np.setValue(initToPos<0?lastAddToPos:Math.min(initToPos, szf1));
			AlertDialog dTmp = builder2.setView(dv).create();
			dv.findViewById(R.id.confirm).setOnClickListener(v -> {
				dTmp.setOnDismissListener(null);
				long fvp = initToPos>=0 ? ViewUtils.saveListPos(f1.listView) : 0;
				int toPos = lastAddToPos = Math.min(szf1, np.getValue());
				int cc = 0;
				mFile[] arr;
				if (set != null) {
					arr = set;
					ArrayUtils.reverse(arr);
				} else {
					arr = f3.getElements(useSelection);
					Arrays.sort(arr);
				}
				final boolean insert = checker1.isChecked();
				final boolean select = checker2.isChecked();
				/** to select only those will be inserted. */
				final boolean jinxuan = select && checker4.isChecked();
				final boolean reset = checker3.isChecked();
				HashMap<String, mFile> map = new HashMap<>(arr.length);
				ArrayList<mFile> files = new ArrayList<>(arr.length);
				for (int i = 0; i < arr.length; i++) {
					mFile fn = arr[i];
					if (!fn.getIsDirectory()) {
						files.add(fn);
						String key = fn.getRealPath().getPath();
						if(insert||select) map.put(key, fn);
					}
				}
				if(insert) {
					boolean rejected;
					f1.markDirty(-1);
					f1_dirty = 0x3;
					for (int i = f1.manager_group().size()-1; i >= 0; i--) {
						mFile fn = map.get(f1.getPathAt(i));
						if (fn != null) {
							//CMN.debug("remove first", f1.getPathAt(i));
							if (reset) { // 重新添加
								f1.replace(i, -2);
								if(i < toPos) {
									toPos--;
								}
								if (initToPos>=0 && i<(int)fvp) {
									fvp--;
								}
							} else {
								f1.setPlaceRejected(i, false);
								if(select) f1.setPlaceSelected(i, true);
								files.remove(fn);
							}
						} else if(jinxuan) {
							f1.setPlaceSelected(i, false);
						}
					}
					for (int i = 0; i < files.size(); i++) {
						rejected = rejectMap!=null && rejectMap.indexOfKey(i)>=0;
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
						toPos = Math.max(0, Math.min(toPos++, loadMan.md.size()));
						loadMan.md.add(toPos, mdTmp);
						loadMan.lazyMan.placeHolders.add(toPos, placeHolder);
						loadMan.lazyMan.chairCount++;
						if(select){
							f1.setPlaceSelected(toPos, true);
						}
						if(rejected){
							f1.setPlaceRejected(toPos, true);
						}
					}
					f1.refreshSize();
				}
				else if (select) {
					for (int i = 0; i < szf1; i++) {
						if (map.containsKey(f1.getPathAt(i))){
							f1.setPlaceSelected(i, true);
							cc++;
						}
						else if(jinxuan) {
							f1.setPlaceSelected(i, false);
						}
					}
				}
				f1.dataSetChanged(insert);
				if(insert) {
					if (initToPos >= 0) {
						ViewUtils.restoreListPos(f1.listView, fvp);
					} else {
						f1.getListView().setSelectionFromTop(toPos, 0);
					}
				}
				viewPager.setCurrentItem(0);
				showT((insert?"添加完毕!(":"已选中!(")+cc+"/"+files.size()+")");
				toolbar.getMenu().close();
				dTmp.dismiss();
				if (set != null && settingsPanel instanceof PasteBinHub) {
					settingsPanel.dismissImmediate();
				}
			});
			Window win = dTmp.getWindow();
			win.setBackgroundDrawableResource(GlobalOptions.isDark?R.drawable.popup_shadow_ld:R.drawable.popup_shadow_l);
			dTmp.show();
			win.setDimAmount(0);
			ViewGroup dvp = win.getDecorView().findViewById(R.id.dialog);
			dvp.setPadding(0,0,0,0);
			dv.setPadding((int) (15*opt.dm.density), 0,0,(int) (10*opt.dm.density));
		}
		else if(f3!=null){
			/* 添加到末尾 */
			int cc = 0;
			mFile[] arr = f3.getElements(useSelection);
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
				String key = fn.getRealPath().getPath();
				Integer idx = map.get(key);
				if(idx!=null) { // 已经有了
					f1.setPlaceRejected(idx, false);
				}
				else {
					f1.markDirty(-1);
					f1_dirty = 0x3;
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
			f1.dataSetChanged(cc>0);
		}
	}
	
	private void disenSel(boolean disable, int szf1, boolean sf1) {
		int cnt=0;
		if(opt.dictManager1MultiSelecting()){
			for (int i = 0; i < szf1; i++) {
				if(f1.getPlaceSelected(i)) {
					f1.setPlaceRejected(i, disable);
					cnt++;
				}
			}
		}
		if (cnt > 0) {
			f1.refreshSize();
			if (sf1) opt.dictManager1MultiSelecting(true);
			f1.dataSetChanged(false);
			showT((disable ? "禁用当前词典选中项" : "启用当前词典选中项") + "\n已处理" + cnt + "项，当前已启用" + loadMan.lazyMan.chairCount + "项");
		} else {
			showT("当前分组无选中词典。");
		}
	}
	
	
	public ListFragment getFragment() {
		return fragments.get(viewPager.getCurrentItem());
	}
	
	public void deleteFromF3Sel(BookManagerFolderAbs f3) {
		new AlertDialog.Builder(BookManager.this)
				.setTitle(mResource.getString(R.string.surerrecords, f3.calcSelectionSz()).replace("彻底", ""))
				.setMessage("从当前分组删除记录，不会删除文件或全部词典记录，但不可撤销。")
				.setPositiveButton(R.string.confirm, (dialog, which) -> {
					deleteRecordsHard(f3, true, false);
					dialog.dismiss();
				})
				.create().show();
	}
	
	private void doDeleteFromF3SelHardInAll(
			final BookManagerFolderAbs folderMangager
			, final boolean deleteFromAllGroup
	) {
		final HashSet<String> removePool = folderMangager.getSelectedPaths();
	 	final ArrayList<mFile> folderAllFiles =  folderMangager.data.getList();
		BookManagerFolderAbs another_folderLike = folderMangager == f3 ? f4 : f3;
		boolean anotherChanged = false;
		for (int i = folderAllFiles.size()-1; i >= 0; i--) {
			mFile fn = folderAllFiles.get(i)/*.getRealPath()*/;
			if (removePool.contains(fn.getPath()) // selected
					&& fn.webAsset==null // not embedded webx
					&& !(fn instanceof mAssetFile) // not embedded asset
					&& !fn.isDirectory() // not directory
			) {
				anotherChanged |= folderMangager.removeFileInDataAt(i, true);
				mdlibsCon.remove(mFile.removeFolderPrefix(fn, opt.lastMdlibPath));
			}
		}
		deleteRecordsHard(folderMangager, false, true);
		if (another_folderLike!=null && anotherChanged) {
			another_folderLike.selFolders.clear();
			another_folderLike.Selection.clear();
			another_folderLike.rebuildDataTree();
		}
		int cc=0;
		if (!PDICMainAppOptions.debuggingRemoveRecSkipWrite())
		{
			//PDICMainAppOptions.setDelRecApplyAll(deleteFromAllGroup);
			ArrayList<File> moduleFilesArr = ScanInModlueFiles(deleteFromAllGroup, true); // folderMangager.isDirty
			AgentApplication app = ((AgentApplication) getApplication());
			char[] cb = app.get4kCharBuff();
			boolean bNeedRewrite;
			for (File fI : moduleFilesArr) {
				CMN.debug("removePool::", removePool);
				CMN.debug("removePool::", fI);
				StringBuilder sb = new StringBuilder();
				String line;
				try {
					ReusableBufferedReader br = new ReusableBufferedReader(new FileReader(fI), cb, 4096);
					bNeedRewrite = false;
					while ((line = br.readLine()) != null) {
						CMN.debug("line::", line);
						try {
							String key = line;
							if(key.startsWith("[:")) {
								int idx = key.indexOf("]",2);
								if(idx>=2) key = key.substring(idx+1);
							}
							key = key.startsWith("/") ? key : (opt.lastMdlibPath + "/" + key);
							CMN.debug("rewrite::", key, removePool.contains(key));
							if (removePool.contains(key)
									|| removePool.contains(  new File(key).getCanonicalPath() )  ) {
								bNeedRewrite = true;
								cc++;
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
		showT("移除完毕! " +cc);
	}
	
	public void deleteFromF3SelHardInAll(final BookManagerFolderAbs f3) {
		final View dv = inflater.inflate(R.layout.dialog_sure_and_all, null);
		final CheckBox ck = dv.findViewById(R.id.ck);
		final int sz = f3.calcSelectionSz();
		ck.setChecked(PDICMainAppOptions.getDelRecApplyAll());
		dv.findViewById(R.id.title).setOnClickListener(v -> ck.toggle());
		d = new AlertDialog.Builder(BookManager.this)
			.setView(dv)
			.setTitle(  mResource.getString(R.string.surerrecords, sz)  )
			.setMessage("从分组和全部词典记录中彻底清理记录，不可撤销！！")
			.setPositiveButton(R.string.confirm
					, (dialog, which) ->
							doDeleteFromF3SelHardInAll(f3, ck.isChecked()))
			.setNeutralButton(R.string.cancel, null)
			.show();
	}
	
	// 缝合怪
	private void disenaddAll(String title, boolean addAll, boolean dis, boolean warnDisenaddAll) {
		if (!warnDisenaddAll) {
			int szf1 = f1.manager_group().size();
			if (addAll) {
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
						CMN.debug(e2);
					}
				}
			}
			else { // reject or add all
				for (int i = 0; i < szf1; i++) {
					f1.setPlaceRejected(i, dis);
				}
			}
			f1.dataSetChanged(false);
			f1.refreshSize();
		} else {
			final View dv = inflater.inflate(R.layout.dialog_sure_and_all, null);
			CheckBox ck = dv.findViewById(R.id.ck);
			ck.setChecked(!PDICMainAppOptions.getWarnDisenaddAll());
			TextView tv = dv.findViewById(R.id.title);
			tv.setOnClickListener(v1 -> ck.toggle());
			tv.setText("重启前不再提示");
			AlertDialog.Builder builder2 = new AlertDialog.Builder(BookManager.this);
			builder2.setView(dv).setTitle("确认"+title+"吗？")
					.setPositiveButton(R.string.confirm, (dialog, which) -> {
						disenaddAll(null, addAll, dis, false);
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
		CMN.debug("ScanInModlueFiles::", all, "addAllLibs="+addAllLibs, ret);
		return ret;
	}
	
	 /* Delete file records from folderlike manager
			in current dictionary group. */
	private void deleteRecordsHard(BookManagerFolderAbs folderLike, boolean showToast, boolean folderStuctureChange) {
		int count = 0;
		int f1_size = f1.manager_group().size();
		int total = folderLike.calcSelectionSz();
		for(int i=0;i<f1_size;i++)
		{
			if(folderLike.Selection.remove(new mFile(f1.getPathAt(i))))
			{
				f1.markDirty(-1);
				f1.replace(i--, -1);
				f1_size--;
				count++;
			}
		}
		f1.refreshSize();
		f1.dataSetChanged(count>0);
		folderLike.selFolders.clear();
		folderLike.Selection.clear();
		folderLike.dataSetChanged(folderStuctureChange);
		if (folderStuctureChange) {
			folderLike.rebuildDataTree();
		}
		if (showToast)
			showT("移除完毕!("+count+"/"+total+")");
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
					if (create) {
						mdict_cache.put(key, ret);
						try {
							ret.readConfigs(this, prepareHistoryCon());
						} catch (Exception e) {
							CMN.debug(e);
						}
					}
				}
			}
		}
		if (ret==null) ret = loadMan.EmptyBook;
		return ret;
	}
	
	public BookPresenter getMagentAt(int position) {
		return getMagentAt(position, true);
	}
	
	byte[] Hzh;
	
	/** 获取常规汉字的拼音首字母 */
	public byte[] getHzh(){ // todo optimize...
		if (Hzh==null) {
			try {
				InputStream input = getAssets().open("Hzh.dat");
				Hzh = new byte[Hzh_filesize];
				input.read(Hzh);
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		return Hzh;
	}
}



package com.knziha.plod.dictionarymanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.ActionMenuView.LayoutParams;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.utils.FU;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.BaseHandler;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedWriter;
import com.knziha.plod.dictionarymanager.files.mAssetFile;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.mngr_agent_manageable;
import com.knziha.plod.dictionarymodels.mngr_presenter_nonexist;
import com.knziha.plod.dictionarymodels.mngr_agent_prempter;
import com.knziha.plod.dictionarymodels.mngr_agent_transient;
import com.knziha.plod.widgets.SimpleTextNotifier;
import com.knziha.plod.widgets.Toastable_FragmentActivity;
import com.knziha.rbtree.RashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class dict_manager_activity extends Toastable_FragmentActivity implements OnMenuItemClickListener
{
	HashMap<String, mngr_agent_transient> mdict_cache = new HashMap<>();
	Intent intent = new Intent();
	private PopupWindow mPopup;
	public ArrayList<PlaceHolder> slots;
	private ArrayList<Fragment> fragments;
	public HashMap<String, BookPresenter> app_mdict_cache;
	public HashMap<CharSequence,byte[]> UIProjects;
	public HashSet<CharSequence> dirtyMap;
	
	public File ConfigFile;
	
	public File DecordFile;
	
	public File DefordFile;
	
	public File SecordFile;
	
	mngr_presenter_nonexist mninstance = new mngr_presenter_nonexist(new File("/N/A"));
	private boolean deleting;
	
	public dict_manager_activity() throws IOException { }
	
	public File fileToSet(String name) {
		return opt.fileToSet(ConfigFile, name);
	}
	
	public interface transferRunnable{
		boolean transfer(File to);
		void afterTransfer();
	}

    private ViewGroup toastmaker;
    private Toolbar toolbar;
    static String dictQueryWord;
    private SearchView searchView;
    protected Menu toolbarmenu;
    dict_manager_main f1;
    dict_manager_modules f2;
    dict_Manager_folderlike f3;
	dict_manager_websites f4;
    ViewPager viewPager;  //对应的viewPager
    TabLayout mTabLayout;
	LayoutInflater inflater;

	public ArrayList<mngr_agent_transient> mdmng;
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
		CMN.Log("肮脏的一群！", f1.isDirty, f2.isDirty, f3.isDirty);
		if(f1.isDirty) {
			intent.putExtra("result", true);
			int size = mdmng.size();
			boolean identical = size==slots.size();
			int i;
			for (i = 0; i < size; i++) {
				mngr_agent_transient mmTmp = mdmng.get(i);
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
				for (mngr_agent_manageable mmTmp:mdmng) {
					slots.add(((mngr_agent_transient)mmTmp).mPhI);
				}
				try {
					File def = new File(getExternalFilesDir(null), "default.txt");
					ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(def), app.get4kCharBuff(), 4096);
					String parent = opt.lastMdlibPath.getPath();
					output.write("[:S]");
					output.write(Integer.toString(mdmng.size()));
					output.write("\n");
					for (mngr_agent_manageable mmTmp : mdmng) {
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
				ReusableBufferedWriter output = new ReusableBufferedWriter(new FileWriter(DecordFile), app.get4kCharBuff(), 4096);
				String parent = opt.lastMdlibPath.getPath()+File.separator;
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
		hdl.clearActivity();
		viewPager.clearOnPageChangeListeners();
		mTabLayout.clearOnTabSelectedListeners();
	}

	private void writeForOneLine(Writer out, mngr_agent_manageable mmTmp, String parent) throws IOException {
		String name = mmTmp.getPath();
		if(name.startsWith(parent) && name.length()>parent.length())
			name = name.substring(parent.length()+1);
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
		app_mdict_cache=agent.mdict_cache;
		UIProjects=agent.UIProjects;
		dirtyMap=agent.dirtyMap;
		opt=agent.opt;
		slots = agent.slots;
		mdlibsCon=agent.mdlibsCon;
		
		ConfigFile = opt.fileToConfig();
		
		DecordFile = opt.fileToDecords(ConfigFile);
		
		SecordFile = opt.fileToSecords(ConfigFile);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dict_manager_main);
		
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
	    
		fragments.addAll(Arrays.asList(f1 = new dict_manager_main(), f2 = new dict_manager_modules(), f4 = new dict_manager_websites(), f3 = new dict_Manager_folderlike()));
		f1.a=f2.a=f4.a=f3.a=this;

		f3.oes = new dict_Manager_folderlike.OnEnterSelectionListener() {
			public void onEnterSelection(){
				for (int i = 7; i <= 15; i++) toolbarmenu.getItem(i).setVisible(i<=12);
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
						mngr_agent_transient mmTmp = new mngr_agent_transient(dict_manager_activity.this, fn.getPath(), opt, 0, mninstance);
						f1.adapter.add(mmTmp);
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
	    	@Override public void onPageSelected(int page) {
				Fragment fI = fragments.get(page);
				viewPager.setOffscreenPageLimit(Math.max(viewPager.getOffscreenPageLimit(), Math.max(1+page, 1)));
	    		if(fI==f1) {
					for (int i = 0; i <= 15; i++) toolbarmenu.getItem(i).setVisible(i<=6);
	    		}
	    		else if(fI==f2) {
					for (int i = 0; i <= 15; i++) toolbarmenu.getItem(i).setVisible(i==0||i==3||i==15);
	    		}
	    		else if(fI==f3){
					for (int i = 0; i <= 6; i++) toolbarmenu.getItem(i).setVisible(false);
	    			boolean setter=f3.SelectionMode;
					for (int i = 7; i <= 14; i++) toolbarmenu.getItem(i).setVisible((i <= 12) == setter);
					toolbarmenu.getItem(15).setVisible(false);
	    		}
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
		
	    mTabLayout.setSelectedTabIndicatorColor(ColorUtils.blendARGB(CMN.MainBackground, Color.BLACK, 0.28f));
	    
	    mTabLayout.setSelectedTabIndicatorHeight((int) (3.8*opt.dm.density));
	    
	    //tofo
	    viewPager.setCurrentItem(CurrentPage = opt.getDictManagerTap());
		
	    viewPager.setOffscreenPageLimit(Math.max(viewPager.getOffscreenPageLimit(), 1+CurrentPage));

	    toastmaker =  findViewById(R.id.toastmaker);
	    
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitleTextColor(Color.WHITE);
		toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material); //ic_flag_24dp
		toolbar.setNavigationOnClickListener(v1 -> {
			onBackPressed();
		});
        
        toolbar.setTitle(R.string.manager);
        
 		MenuItem searchItem = toolbarmenu.getItem(16);
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
	            	f1.adapter.notifyDataSetChanged();
	            if(f3.adapter!=null)
	            	f3.adapter.notifyDataSetChanged();
				if(dictQueryWord!=null){
					int cc=0;
					Fragment fI = fragments.get(viewPager.getCurrentItem());
					if (fI==f1) {
						for (int i = 0; i < f1.adapter.getCount(); i++) {
							if (f1.adapter.getItem(i).getDictionaryName().toLowerCase().contains(query))
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
			if(f1.adapter!=null) f1.adapter.notifyDataSetChanged();
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
        agent.clearNonsenses();
	}
	//onCreate结束
	
	

	
	
	
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
			if(!SU.isNoneSetFileName(name)) {
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
			if(newName.equals("") || newName.contains("/")) {
				show(R.string.renamefail0);
				return;
			}
			newName = SU.legacySetFileName(newName);
			final File newf = new File(ConfigFile, newName);
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
		
    public void showT(String text){
        if(m_currentToast != null)
        m_currentToast.cancel();
        m_currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        m_currentToast.show();
    }Toast m_currentToast;
	public boolean isDebug=false;
	boolean ThisIsDirty;

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
			output.write(Integer.toString(mdmng.size()));
			output.write("\n");
			String parent = opt.lastMdlibPath.getPath();
			for(mngr_agent_manageable mmTmp:mdmng) {
				writeForOneLine(output, mmTmp, parent);
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
		switch (item.getItemId()) {
			case R.id.toolbar_action0:{//提交
				if(isLongClicked) {
					f1.performLastItemLongClick();
					ret=true; break;
				}
				try {
					String name = opt.getLastPlanName("LastPlanName");
					File to = new File(ConfigFile, name);
					boolean shouldInsert = false;
					if(!to.exists())
						shouldInsert=true;
					BufferedWriter output = new BufferedWriter(new FileWriter(to,false));
					
					String parent = opt.lastMdlibPath.getPath();
					for(mngr_agent_manageable mmTmp:mdmng) {
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
					for(mngr_agent_manageable mmTmp:mdmng) {
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
						String name = opt.getLastPlanName("LastPlanName");
						File from = new File(ConfigFile, name);
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
            case R.id.toolbar_action4:{//禁用全部
				f1.isDirty=true;
            	if(isLongClicked){
					if(opt.getDictManager1MultiSelecting()){
						f1.rejector.addAll(f1.selector);
						f1.refreshSize();
						f1.adapter.notifyDataSetChanged();
					}
				}else{
					for(mngr_agent_manageable mmTmp:mdmng) {
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
					if(opt.getDictManager1MultiSelecting()){//间选1
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
								f1.selector.add(mdmng.get(i).getPath());
							}
						}
					}
				}else {
					try {
						BufferedReader in = new BufferedReader(new FileReader(DecordFile));
						String line;
						while ((line = in.readLine()) != null) {
							if (!line.startsWith("/"))
								line = opt.lastMdlibPath + "/" + line;
							line = new File(line).getAbsolutePath();
							if (!mdict_cache.containsKey(line)) {
								mngr_agent_transient m = mdict_cache.get(line);
								if (m == null)
									m = new mngr_agent_transient(dict_manager_activity.this, line, opt, mninstance);
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
			/* 全部选择 */
            case R.id.toolbar_action7:{
            	if(isLongClicked){//间选
					int[] positions = f3.lastClickedPos;
					if(positions[0]!=-1 && positions[1]!=-1){
						int start=positions[0];
						int end=positions[1];
						if(end<start){
							int tmp=end;
							end=start;
							start=tmp;
						}
						ArrayList<mFile> data = f3.data.getList();
						for (int i = start; i <= end; i++) {
							f3.Selection.put(data.get(i).getPath());
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
			/* 全选失效项 */
            case R.id.toolbar_action8:{
				f3.Selection.clear();
				ArrayList<mFile> data = f3.data.getList();
				f3.alreadySelectedAll=false;
				if(!isLongClicked) {
					for(int i=0;i<f3.data.size();i++) {
						mFile fI = data.get(i);
						if(!fI.exists())
							f3.Selection.put(fI.getAbsolutePath());
					}
				}
				f3.adapter.notifyDataSetChanged();
			} break;
			/* 添加 */
            case R.id.toolbar_action9:{
				ArrayList<mngr_agent_transient> data = f1.manager_group;
				if(isLongClicked) {/* 添加到第几行 */
					AlertDialog.Builder builder2 = new AlertDialog.Builder(dict_manager_activity.this);
					View dv = getLayoutInflater().inflate(R.layout.dialog_move_to_line, null);
					NumberPicker np = dv.findViewById(R.id.numberpicker);
					CheckBox checker1 = dv.findViewById(R.id.check1);
					CheckBox checker2 = dv.findViewById(R.id.check2);
					np.setMaxValue(mdmng.size());
					AlertDialog dTmp = builder2.setView(dv).create();
					dv.findViewById(R.id.confirm).setOnClickListener(v -> {
						int toPos = Math.min(data.size(), np.getValue());
						int cc = 0;
						ArrayList<String> arr = f3.Selection.flatten();
						int count=arr.size();
						boolean insert = checker1.isChecked();
						if(insert) {
							for (int i = 0; i < arr.size(); i++) {
								File fn = new File(arr.get(i));
								if (fn.isDirectory()) {
									count--;
									continue;
								}
								String key = fn.getPath();
								mngr_agent_transient m = null;
								if (f1.rejector.contains(key)) {
									f1.rejector.remove(key);
								}
								for (int j = 0; j < data.size(); j++) {
									if (data.get(j).getPath().equals(key)) {
										m = data.remove(j);
										break;
									}
								}
								if (m == null)
									m = mdict_cache.get(key);
								if (m == null) {
									m = new mngr_agent_transient(dict_manager_activity.this, key, opt, mninstance);
									mdict_cache.put(key, m);
								}
								data.add(Math.min(data.size(), toPos++), m);
								cc++;
								f1.isDirty = true;
							}
							f1.refreshSize();
							ThisIsDirty = true;
						}
						if(checker2.isChecked()){//选中
							//f1.selector.clear();
							f1.selector.addAll(arr);
						}
						f1.adapter.notifyDataSetChanged();
						if(insert) {
							f1.getListView().setSelectionFromTop(toPos - cc, 0);
						}
						viewPager.setCurrentItem(0);
						showT("添加完毕!("+cc+"/"+count+")");
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
				else {
					int cc = 0;
					ArrayList<String> arr = f3.Selection.flatten();
					int count=arr.size();
					for(int i=0;i<arr.size();i++) {
						File fn=new File(arr.get(i));
						if(fn.isDirectory()) {
							count--;
							continue;
						}
						String key = fn.getPath();
						if(f1.rejector.contains(fn.getAbsolutePath())) {
							f1.rejector.remove(fn.getAbsolutePath());
							cc++;
						}
						else {
							mngr_agent_transient m=null;
							for (int j = 0; j < data.size(); j++) {
								if(data.get(j).getPath().equals(key)){
									m=data.get(j);
									break;
								}
							}
							if(m==null){
								m = mdict_cache.get(key);
								if(m==null){
									m = new mngr_agent_transient(dict_manager_activity.this, key, opt, mninstance);
									mdict_cache.put(key, m);
								}
								data.add(m);
								cc++;
								f1.isDirty=true;
							}
						}
					}
					showT("添加完毕!("+cc+"/"+count+")");
					f1.refreshSize();
					ThisIsDirty=true;
					f1.adapter.notifyDataSetChanged();
				}
			} break;
			/* 移除 */
            case R.id.toolbar_action10:{
				if(isLongClicked) {
					new AlertDialog.Builder(dict_manager_activity.this)
							.setTitle(R.string.surerrecords)
							.setPositiveButton(R.string.confirm, (dialog, which) -> {
								deleteRecordsHard();
								dialog.dismiss();
							})
					.create().show();
				} else {
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
				}
			} break;
			/* 删除记录 */
            case R.id.toolbar_action11:{
				if(isLongClicked) {
					ret=false;break;
				}
				else {
					final View dv = inflater.inflate(R.layout.dialog_sure_and_all, null);
					AlertDialog.Builder builder2 = new AlertDialog.Builder(dict_manager_activity.this);
					builder2.setView(dv).setTitle(getResources().getString(R.string.surerrecords, f3.Selection.size()))
							.setPositiveButton(R.string.confirm, (dialog, which) -> {
								HashSet<String> removePool = new HashSet<>();
								ArrayList<String> arr1 = f3.Selection.flatten();
								for (int i = 0; i < arr1.size(); i++) {
									removePool.add(arr1.get(i));
								}
								ArrayList<mFile> list = f3.data.getList();
								int s2 = list.size();
								for (int i = 0; i < s2; i++) {
									mFile item1 = list.get(i);
									if (item1 instanceof mAssetFile)
										continue;
									if (item1.isDirectory())
										continue;
									if (removePool.contains(item1.getAbsolutePath())) {
										list.remove(i--);
										s2--;
										mdlibsCon.remove(mFile.tryDeScion(item1, opt.lastMdlibPath));
										f3.isDirty = true;
										mFile p = item1.getParentFile();
										if (p != null) {
											int idx = f3.data.indexOf(p);
											if (idx != -1)
												if (idx == s2 - 1 || !mFile.isDirScionOf(list.get(idx + 1), p)){
													list.remove(i--);
													s2--;
												}
										}
									}
								}
								deleteRecordsHard();

								ArrayList<File> moduleFullScannerArr = ScanInModlueFiles(((CheckBox) dv.findViewById(R.id.ck)).isChecked(), f3.isDirty);

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
												if (removePool.contains(key) || removePool.contains(new File(key).getCanonicalPath())) {
													bNeedRewrite = true;
													continue;
												}
											} catch (Exception ignored) {
											}
											sb.append(line).append("\n");
										}
										br.close();
										cb = br.cb;
										if (bNeedRewrite) {
											ReusableBufferedWriter bw = new ReusableBufferedWriter(new FileWriter(fI), cb, 4096);
											bw.write(sb.toString());
											bw.flush();
											bw.close();
											cb = br.cb;
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								app.set4kCharBuff(cb);
								showT("移除完毕!");
							})
							.setNeutralButton(R.string.cancel, null);
					d = builder2.create();
					d.show();
				}
			} break;
			/* 移动文件 */
            case R.id.toolbar_action12:{
            	if (true) {
            		showT("功能关闭，请等待5.0版本");
					ret=false;break;
				}
				if(isLongClicked) {
					ret=false;break;
				}
				else {
					DialogProperties properties = new DialogProperties();
					properties.selection_mode = DialogConfigs.SINGLE_MODE;
					properties.selection_type = DialogConfigs.DIR_SELECT;
					properties.root = new File("/");
					properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
					properties.offset = opt.lastMdlibPath;
					properties.opt_dir = new File(opt.pathToDatabases() + "favorite_dirs/");
					properties.opt_dir.mkdirs();
					FilePickerDialog dialog = new FilePickerDialog(this, properties);
					dialog.setTitle(R.string.pickdestineFolder);
					dialog.setDialogSelectionListener(new DialogSelectionListener() {
						@Override
						public void
						onSelectedFilePaths(String[] files, File n) {
							File p = new File(files[0]);//新家
							if (p.isDirectory()) {
								ArrayList<String> arr = f3.Selection.flatten();
								RashSet<String> renameList = new RashSet<>();
								ArrayList<String> renameListe;
								HashMap<String, mngr_agent_manageable> mdict_cache = new HashMap<>(mdmng.size());
								for (mngr_agent_manageable mmTmp : mdmng) {
									//if (mmTmp instanceof mdict)
										mdict_cache.put(mmTmp.getPath(), mmTmp);
								}
								//todo 保证mdict移动文件的同时性。
								int cc = 0;
								for (String sI : arr) {//do actual rename. rename a lot of files..
									mFile mF = new mFile(sI).init(opt);
									//ommitting directory.
									//if(sI.startsWith("/ASSET/") && CMN.AssetMap.containsKey(sI)) continue;
									if (mF.isDirectory()) continue;
									if (f3.data.get(mF).isDirectory()) continue;
									mngr_agent_manageable mmTmp = mdict_cache.get(sI);
									if (mmTmp == null) {
										try {
											mmTmp = new mngr_agent_prempter(dict_manager_activity.this, sI, opt, mninstance);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									File OldF = mmTmp.f();
									String OldFName = mmTmp.getDictionaryName();
									File toF = new File(p, OldF.getName());
									boolean ret = mmTmp.moveFileTo(dict_manager_activity.this, toF);
									//CMN.Log("移动？？？", ret, toF);
									if (ret) {
										RebasePath(OldF, OldFName, toF, null, OldF.getName());
										mdlibsCon.remove(mFile.tryDeScion(OldF, opt.lastMdlibPath));
										mdlibsCon.add(mFile.tryDeScion(toF, opt.lastMdlibPath));
										f3.Selection.remove(sI);//移出f3的选择
										renameList.put(sI);//然后记录
										cc++;
									}
								}
								mdict_cache.clear();
								f1.isDirty = true;
								renameListe = renameList.flatten();
								for (String fnI : renameListe) {
									mFile fOld = new mFile(fnI).init(opt);
									int idx = f3.data.remove(fOld);
									if (idx != -1) {
										mFile p2 = fOld.getParentFile().init(opt);
										if (p2 != null) {
											int idx2 = f3.data.indexOf(p2);
											if (idx2 != -1) {//如有必要，移除多余的父文件夹
												if (idx2 == f3.data.size() - 1 || !mFile.isDirScionOf(f3.data.getList().get(idx2 + 1), p2))
													f3.data.getList().remove(idx2);
												f3.data.OverFlow.remove(p2);
											}
											//showT(System.currentTimeMillis()+" "+idx2);
										} else {
											f3.data.OverFlow.remove(p2);
											//f3.data.OverFlow.clear();
										}
										//f3.data.OverFlow.clear();
										mFile val = new mFile(p, new File(fnI).getName());
										f3.data.insert(val.init(opt));
										f3.Selection.insert(val.getAbsolutePath());
										if (!mFile.isDirScionOf(val, opt.lastMdlibPath))
											f3.data.insertOverFlow(val.getParentFile().init(opt));
									}
								}
								f3.adapter.notifyDataSetChanged();
								f3.isDirty = true;

								ArrayList<File> moduleFullScannerArr = ScanInModlueFiles(true, true);
								if(DefordFile.length()>0) moduleFullScannerArr.add(DefordFile);
								HashSet<String> mdlibs = new HashSet<>();
								AgentApplication app = ((AgentApplication) getApplication());
								char[] cb = app.get4kCharBuff();
								for (File fI : moduleFullScannerArr) {
									boolean modified = false;
									mdlibs.clear();
									StringBuilder sb = new StringBuilder();
									String line;
									try {
										ReusableBufferedReader br = new ReusableBufferedReader(new FileReader(fI), cb, 4096);
										while ((line = br.readLine()) != null) {
											String key = line;
											String prefix = null;
											try {
												if(key.startsWith("[:")){
													int idx = key.indexOf("]",2);
													if(idx>=2) {
														idx+=1;
														prefix = key.substring(0, idx);
														key = key.substring(idx);
													}
												}
												line = key;
												key = key.startsWith("/") ? key : (opt.lastMdlibPath + "/" + key);
												if (renameList.contains(key)) {// 搬到新家
													modified = true;
													key = mFile.tryDeScion(new File(p, new File(key).getName()), opt.lastMdlibPath);
												} else { //复原
													key = line;
												}
											} catch (Exception ignored) { }
											if (!mdlibs.contains(key)) {
												mdlibs.add(key);
												if (prefix!=null)
													key = prefix + key;
												sb.append(key).append("\n");
											} else {
												modified = true;
											}
										}
										br.close();
										cb = br.cb;
										if (modified) {
											ReusableBufferedWriter bw = new ReusableBufferedWriter(new FileWriter(fI), cb, 4096);
											bw.write(sb.toString());
											bw.flush(); bw.close();
											cb = br.cb;
										}
									}
									catch (IOException e) {
										CMN.Log(e);
									}
								}
								app.set4kCharBuff(cb);
								renameList.clear();
								renameListe.clear();
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
				}
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
						showT("名称非法！");
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
	
	public ArrayList<File> ScanInModlueFiles(boolean all, boolean addAllLibs) {
		ArrayList<File> ret = new ArrayList<>();
		if (all) {
			String[] names = ConfigFile.list();
			if(names!=null) {
				ret.ensureCapacity(names.length);
				for (int i = 0; i < names.length; i++) {
					String name = names[i];
					if(!SU.isNoneSetFileName(name)) {
						ret.add(new File(ConfigFile, name));
					}
				}
			}
		}
		if (addAllLibs) {
			ret.add(DecordFile);
		}
		return ret;
	}
	
	private void deleteRecordsHard() {
		int cc1 = 0;
		int size = mdmng.size();
		int total = f3.Selection.size();
		for(int i=0;i<size;i++) {
			if(f3.Selection.remove(mdmng.get(i).getPath())) {
				mdmng.remove(i--);
				size--;
				cc1++;
				f1.isDirty=true;
			}
		}
		if(f3.Selection.size()>0){
			ArrayList<mFile> list = f3.data.getList();
			int s2 = list.size();
			for (int i = 0; i < s2; i++) {
				if(f3.Selection.contains(list.get(i).getPath()))
					total--;
			}
		}
		ThisIsDirty=true;
		f1.refreshSize();
		showT("移除完毕!("+cc1+"/"+total+")");
		f1.adapter.notifyDataSetChanged();
		f3.adapter.notifyDataSetChanged();
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
			mngr_agent_transient mmtmp = mdict_cache.get(line);
			if (mmtmp == null)
				mmtmp = new mngr_agent_transient(dict_manager_activity.this, line, opt, 0, mninstance);
			if(!mmtmp.isMddResource()) flag&=~0x4;
			mmtmp.setTmpIsFlag(flag);
			mdmng.add(mmtmp);
		}
		in.close();
	}
	
	void showTopSnack(ViewGroup parentView, Object messageVal) {
		if(topsnack==null){
			topsnack = new SimpleTextNotifier(getBaseContext());
			Resources res = getResources();
			topsnack.setTextColor(Color.WHITE);
			topsnack.setBackgroundColor(res.getColor(R.color.colorHeaderBlueT));
			int pad = (int) res.getDimension(R.dimen.design_snackbar_padding_horizontal);
			topsnack.setPadding(pad,pad/2,pad,pad/2);
			topsnack.getBackground().setAlpha((int) (0.5*255));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				topsnack.setElevation(res.getDimension(R.dimen.design_snackbar_elevation));
			}
		}
		else{
			topsnack.removeCallbacks(snackRemover);
			topsnack.setAlpha(1);
		}
		if(messageVal instanceof Integer) {
			topsnack.setText((int) messageVal);
			topsnack.setTag(messageVal);
		}else {
			topsnack.setText(String.valueOf(messageVal));
			topsnack.setTag(null);
		}
		topsnack.setGravity(Gravity.CENTER);
		ViewGroup sp = (ViewGroup) topsnack.getParent();
		if(sp!=parentView) {
			if(sp!=null) sp.removeView(topsnack);
			topsnack.setVisibility(View.INVISIBLE);
			parentView.addView(topsnack);
			ViewGroup.LayoutParams lp = topsnack.getLayoutParams();
			lp.height=-2;
			topsnack.post(snackWorker);
		}else{
			topsnack.removeCallbacks(snackWorker);
			snackWorker.run();
		}
	}
	
	public BaseHandler hdl = new MyHandler(this);
	boolean animationSnackOut;
	SimpleTextNotifier topsnack;
	final int NextSnackLength = 1500;
	
	Runnable snackWorker = () -> {
		hdl.sendEmptyMessage(6657);
		hdl.removeMessages(6658);
		int height = topsnack.getHeight();
		if(height>0){
			if(topsnack.offset>0 || topsnack.offset<-height)
				topsnack.offset=-height;
			else
				topsnack.offset=Math.min(-height/3, topsnack.offset);
			topsnack.setTranslationY(topsnack.offset);
			topsnack.setVisibility(View.VISIBLE);
			hdl.animator=0.1f;
			hdl.animatorD=0.08f*height;
			hdl.sendEmptyMessage(6657);
		}
	};
	
	Runnable snackRemover= () -> {
		if(topsnack!=null && topsnack.getParent()!=null)
			((ViewGroup)topsnack.getParent()).removeView(topsnack);
	};
	
	void removeSnackView(){
		topsnack.removeCallbacks(snackRemover);
		topsnack.postDelayed(snackRemover, 2000);
	}
	
	private static class MyHandler extends BaseHandler {
		private final WeakReference<dict_manager_activity> activity;
		MyHandler(dict_manager_activity a) { this.activity = new WeakReference<>(a); }
		@Override public void clearActivity() { activity.clear(); }
		@Override public void handleMessage(@NonNull Message msg) {
			dict_manager_activity a = activity.get();
			if(a==null) return;
			if (msg.what == 6657) {
				removeMessages(6657);
				a.topsnack.offset += animatorD;
				if (a.topsnack.offset < 0)
					sendEmptyMessage(6657);
				else {
					a.topsnack.offset = 0;
					a.animationSnackOut = true;
					sendEmptyMessageDelayed(6658, a.NextSnackLength);
				}
				a.topsnack.setTranslationY(a.topsnack.offset);
			} else if (msg.what == 6658) {
				removeMessages(6658);
				if (a.animationSnackOut) {
					a.topsnack.offset -= animatorD;
					if (a.topsnack.offset > -(a.topsnack.getHeight() + 8 * a.opt.dm.density))
						sendEmptyMessage(6658);
					else {
						a.removeSnackView();
						return;
					}
					a.topsnack.setTranslationY(a.topsnack.offset);
				}
			}
		}
	}
	
	public void RebasePath(File oldPath, String OldFName, File newPath, String MoveOrRename, String oldName){
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
			byte[] data = UIProjects.remove(oldName);
			if(data!=null){
				String name = newPath.getName();
				UIProjects.put(name, data);
				dirtyMap.add(name);
			}
		}
	}
}



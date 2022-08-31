package com.knziha.plod.PlainUI;

import static com.knziha.plod.preference.SettingsPanel.BIT_STORE_VIEW;
import static com.knziha.plod.preference.SettingsPanel.makeDynInt;

import android.text.TextPaint;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymanager.BookManagerMain;
import com.knziha.plod.dictionarymanager.files.SparseArrayMap;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.searchtasks.IndexBuildingTask;
import com.knziha.plod.widgets.DescriptiveImageView;
import com.knziha.plod.widgets.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

//for menu list
public class SearchToolsMenu extends BaseAdapter implements TwoWayAdapterView.OnItemClickListener
{
	MainActivityUIBase a;
	private TextPaint menu_grid_painter;
	ArrayList<String> menuList = new ArrayList<>();
	public static class MenuItemViewHolder {
		public final DescriptiveImageView tv;
		public MenuItemViewHolder(View convertView) {
			tv = convertView.findViewById(R.id.text);
		}
	}
	
	public SearchToolsMenu(MainActivityUIBase a, TwoWayGridView mainMenuLst) {
		menuList.add("繁简转换");
		menuList.add("繁简选字");
		//menuList.add("繁简通搜");
		
		menuList.add("全文索引");
		menuList.add("搜索引擎");
		
//		menuList.add("翻阅模式");
//		menuList.add("多行编辑");
		
		//menuList.add("二维扫描");
		//menuList.add("文字识别");
		
		this.a = a;
		
		mainMenuLst.setHorizontalSpacing(0);
		mainMenuLst.setVerticalSpacing(0);
		mainMenuLst.setHorizontalScroll(true);
		mainMenuLst.setStretchMode(GridView.NO_STRETCH);
		mainMenuLst.setAdapter(this);
		mainMenuLst.setOnItemClickListener(this);
		mainMenuLst.setScrollbarFadingEnabled(false);
		mainMenuLst.setSelector(a.mResource.getDrawable(R.drawable.listviewselector0));
		mainMenuLst.setBackgroundColor(a.MainAppBackground);
		menu_grid_painter = DescriptiveImageView.createTextPainter(false);
	}
	
	@Override
	public int getCount() {
		return menuList.size();
	}
	
	@Override
	public View getItem(int position) {
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuItemViewHolder holder;
		if(convertView==null) {
			convertView = a.getLayoutInflater().inflate(R.layout.menu_item, parent, false);
			convertView.setTag(holder=new MenuItemViewHolder(convertView));
			holder.tv.textPainter = menu_grid_painter;
		} else {
			holder = (MenuItemViewHolder) convertView.getTag();
		}
		holder.tv.setText(menuList.get(position));
		return convertView;
	}
	
	@Override
	public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case 0:{
				//CMN.Log("繁简转换!!!");
				a.ensureTSHanziSheet(null);
				SparseArrayMap map=null;
				String text = a.getSearchTerm().trim();
				for (int i = 0; i < text.length(); i++) {
					char c=text.charAt(i);
					if(a.fanJnMap.get(c)!=null) {
						map = a.fanJnMap;
						break;
					}
					if(a.jnFanMap.get(c)!=null) {
						map = a.jnFanMap;
						break;
					}
				}
				String newText="";
				if(map!=null) {
					for (int i = 0; i < text.length(); i++) {
						char c=text.charAt(i);
						String cs=map.get(c);
						if(cs!=null) {
							for (int j = 0; j < cs.length(); j++) {
								char c1 = cs.charAt(j);
								if(c!=c1) {
									c=c1;
									break;
								}
							}
						}
						newText+=c;
					}
					a.setSearchTerm(newText);
				}
			} break;
			case 1:
			{
				//CMN.Log("繁简选字!!!");
				//String text = "happy happy happy恒心努力毅力决心";
				String text = a.getSearchTerm().trim();
				int len = text.length();
				a.ensureTSHanziSheet(null);
				int mRowHeight = 0;
				int cols=3;
				SparseIntArray chs = new SparseIntArray(len);
				SparseIntArray neo = new SparseIntArray(len);
				int sz=0;
				for (int i = 0; i < len; i++) {
					boolean skip=false;
					char c=text.charAt(i);
					String str=a.fanJnMap.get(c);
					if(str==null)str=a.jnFanMap.get(c);
					if(str!=null){
						mRowHeight = Math.max(mRowHeight, str.length());
					}
					else {
						int gc = Character.getType(c);
						if(gc!=Character.OTHER_LETTER) { //跳过
							skip=true;
						}
					}
					if(!skip) {
						chs.put(sz++, i);
					}
				}
				if(mRowHeight==0) {
					a.showTopSnack("无候选");
					break;
				}
				mRowHeight = (int) ((mRowHeight+1)*(new RadioSwitchButton(a).getTextSize()+15*GlobalOptions.density)*1.25f);
				
				int h=0;
				int[] rhs = new int[(int) Math.ceil(sz*1.f/cols)];
				int c=0;
				for (int i = 0; i < sz+1; i++) {
					if(i<sz) {
						c=text.charAt(chs.get(i));
						String str=a.fanJnMap.get(c);
						if(str==null)str=a.jnFanMap.get(c);
						c = i / cols;
						rhs[c] = Math.max(rhs[c], str==null?1:str.length());
					}
					if((i+1)%cols==0 || i==sz/*不圆满者亦有运数*/) {
						rhs[c] = (int) ((rhs[c]+1)*(new RadioSwitchButton(a).getTextSize()+15*GlobalOptions.density)*1.25f);
						h += rhs[c];
						if(i==sz-1)
							break;
					}
				}
				
				//
				TwoWayGridView mainMenuLst = new TwoWayGridView(a);
				mainMenuLst.setHorizontalSpacing(0);
				mainMenuLst.setVerticalSpacing(0);
				mainMenuLst.setNumColumns(cols);
				//mainMenuLst.setRowHeight(500);
				mainMenuLst.setHorizontalScroll(false);
				mainMenuLst.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
				//mainMenuLst.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
				SettingsPanel[] views = new SettingsPanel[sz];
				int rh = mRowHeight;
				int Sz = sz;
				SettingsPanel.ActionListener lis = new SettingsPanel.ActionListener() {
					@Override
					public boolean onAction(SettingsPanel p, int flagIdxSection, int flagPos, boolean dynamic, boolean val, int storageInt) {
						int pos = IU.parsint(p.settingsLayout.getTag(),0);
						CMN.Log(pos, flagPos);
						int sz=p.linearLayout.getChildCount();
						if(sz==2) {
							p.actView.setChecked(true);
						} else {
							neo.put(pos, ((String)p.tag).charAt(flagPos));
							for (int i = 0; i < sz; i++) {
								View c = p.linearLayout.getChildAt(i);
								if (c instanceof RadioSwitchButton && c!=p.actView) {
									((RadioSwitchButton) c).setChecked(false);
								}
							}
						}
						return false;
					}
					@Override public void onPickingDelegate(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, int lastX, int lastY) { }
				};
				mainMenuLst.setAdapter(new BaseAdapter() {
					@Override
					public int getCount() {
						return Sz;
					}
					@Override
					public Object getItem(int position) {
						return null;
					}
					@Override
					public long getItemId(int position) {
						return 0;
					}
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						SettingsPanel view = views[position];
						if(view==null) {
							int c = text.charAt(chs.get(position));
							String vars = a.jnFanMap.get(c);
							if(vars==null) vars = a.fanJnMap.get(c);
							if(vars==null) vars = ((char)c)+"";
//							view = views[position] = new SettingsPanel(a, a.opt
//									, new String[][]{new String[]{((char)c)+"：", "术", "術", "朮"}}
//									, new int[][]{new int[]{Integer.MAX_VALUE
//									, makeDynInt(position, 0, true)
//									, makeDynInt(position, 1, true)
//									, makeDynInt(position, 2, true)
//							}}, null);
							String[] ts = new String[vars.length()+1];//{, "术", "術", "朮"};
							ts[0] = ((char) c) + "：";
							for (int i = 0; i < ts.length-1; i++) {
								ts[i+1] = vars.substring(i, i+1);
							}
							int[] vs = new int[vars.length()+1];
							vs[0] = Integer.MAX_VALUE;
							for (int i = 0; i < vs.length-1; i++) {
								vs[i+1] = makeDynInt(100, i, vars.charAt(i)==c)|BIT_STORE_VIEW;
							}
							view = views[position] = new SettingsPanel(a, a.opt
									, new String[][]{ts}
									, new int[][]{vs}, null);
							view.setActionListener(lis);
							view.init(a, parent);
							view.settingsLayout.setTag(position);
							if(vars.length()>1)
								view.tag = vars;
						}
						view.settingsLayout.setLayoutParams(new TwoWayGridView.LayoutParams(-2, rhs[position/cols]));
						return view.settingsLayout;
					}
				});
				mainMenuLst.setSelector(a.mResource.getDrawable(R.drawable.listviewselector0));
				new AlertDialog.Builder(a)
						.setView(mainMenuLst)
						.setTitle("繁简选字")
						.setPositiveButton("确认组合", (dialog, which) -> {
							StringBuilder sb = new StringBuilder(len);
							int lastIdx=0;
							for (int i=0,cp; i < Sz; i++) {
								cp = chs.get(i);
								int n = neo.get(i, -1);
								if (n>=0) {
									sb.append(text, lastIdx, cp);
									sb.append((char)n);
									lastIdx = cp+1;
								}
							}
							sb.append(text, lastIdx, len);
							//a.showT(sb);
							a.setSearchTerm(sb.toString());
						})
						.setNegativeButton("取消", null)
						.show();
				
				mainMenuLst.maxHeight = h;
			} break;
			case 2:
			{
				MainActivityUIBase.LoadManager loadMan = a.loadManager;  // 建立全文索引
				AlertDialog dTmp = indexBuilderDlg.get();
				indexBuilderSet.clear();
				if (!ViewUtils.isVisibleV2(a.lv2))
				{
					indexBuilderSet.add(a.currentDictionary.placeHolder);
				}
				if (dTmp==null) {
					View.OnClickListener onClickListener = new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (v.getId() == android.R.id.button1) {
								startBuildIndexes();
							} else {
								BookManagerMain.ViewHolder vh
										= (BookManagerMain.ViewHolder) ViewUtils.getViewHolderInParents(v, BookManagerMain.ViewHolder.class);
								if (vh != null) {
									PlaceHolder ph = a.loadManager.getPlaceHolderAt(vh.position);
									if (ph != null) {
										boolean val = !vh.ck.isChecked();
										vh.ck.setChecked(val);
										if (val) indexBuilderSet.add(ph);
										else indexBuilderSet.remove(ph);
									}
								}
							}
						}
					};
					dTmp = new AlertDialog.Builder(a)
							.setTitle("建立全文索引")
							.setSingleChoiceItems(new CharSequence[]{}, 0, null)
							.setAdapter(new BaseAdapter() {
								@Override
								public int getCount() {
									return loadMan.lazyMan.chairCount;
								}
								@Override
								public Object getItem(int position) {
									return loadMan.lazyMan.CosyChair[position];
								}
								@Override
								public long getItemId(int position) {
									return 0;
								}
								@Override
								public View getView(int position, View convertView, ViewGroup parent) {
									BookManagerMain.ViewHolder vh;
									if (convertView == null) {
										convertView = a.getLayoutInflater().inflate(R.layout.dict_manager_dslitem, parent, false);
										vh = new BookManagerMain.ViewHolder(convertView);
										vh.title.setOnClickListener(onClickListener);
										vh.ck.setOnClickListener(onClickListener);
									} else {
										vh = (BookManagerMain.ViewHolder) convertView.getTag();
									}
									vh.position = position;
									PlaceHolder ph = a.loadManager.getPlaceHolderAt(position);
									vh.title.setText(a.loadManager.md_getName(position, -1));
									vh.ck.setChecked(indexBuilderSet.contains(ph));
									vh.handle.setVisibility(View.GONE);
									return convertView;
								}
							}, null)
							.setPositiveButton("开始！", null)
							.setNegativeButton("取消", null)
							.show();
					dTmp.findViewById(android.R.id.button1).setOnClickListener(onClickListener);
					indexBuilderDlg = new WeakReference<>(dTmp);
				}
				dTmp.show();
				ViewUtils.ensureWindowType(dTmp, a, null);
				ListView lv = dTmp.getListView();
				float pad = 2.8f * a.mResource.getDimension(R.dimen._50_) * (a.dm.widthPixels>GlobalOptions.realWidth?1:1.45f);
				View root = a.root;
				((AlertController.RecycleListView) lv).mMaxHeight = root.getHeight()>=2*pad?(int) (root.getHeight() - root.getPaddingTop() - pad):0;
			} break;
			case 3:
			{
				if (luceneHelper==null) {
					luceneHelper = new LuceneHelper((PDICMainActivity) a, this);
				}
				MainActivityUIBase.LoadManager loadMan = a.loadManager;  // 进行全文搜索
				AlertDialog dTmp = indexSchDlg.get();
				if (dTmp==null) {
					dTmp = new AlertDialog.Builder(a)
							.setPositiveButton("搜索", null)
							.setNegativeButton("取消", null)
							.setView(luceneHelper.getSchView())
							.show();
					dTmp.findViewById(android.R.id.button1).setOnClickListener(luceneHelper);
					indexSchDlg = new WeakReference<>(dTmp);
				}
				dTmp.show();
				ViewUtils.ensureWindowType(dTmp, a, null);
				ListView lv = dTmp.getListView();
				float pad = 2.8f * a.mResource.getDimension(R.dimen._50_) * (a.dm.widthPixels>GlobalOptions.realWidth?1:1.45f);
				View root = a.root;
				//((AlertController.RecycleListView) lv).mMaxHeight = root.getHeight()>=2*pad?(int) (root.getHeight() - root.getPaddingTop() - pad):0;
			} break;
		}
	}
	
	WeakReference<AlertDialog> indexSchDlg = ViewUtils.DummyRef;
	WeakReference<AlertDialog> indexBuilderDlg = ViewUtils.DummyRef;
	public HashSet<PlaceHolder> indexBuilderSet = new HashSet<>();
	
	private void startBuildIndexes() {
		if (indexBuilderSet.size()>0) {
			new IndexBuildingTask((PDICMainActivity) a).execute(indexBuilderSet);
		}
	}
	
	LuceneHelper luceneHelper;
}

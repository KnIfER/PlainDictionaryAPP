package com.knziha.plod.PlainUI;

import static com.knziha.plod.preference.SettingsPanel.BIT_STORE_VIEW;
import static com.knziha.plod.preference.SettingsPanel.makeDynInt;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.jess.ui.TwoWayAbsListView;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymanager.files.SparseArrayMap;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.DescriptiveImageView;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;

//for menu list
public class SearchToolsMenu extends BaseAdapter implements TwoWayAdapterView.OnItemClickListener
{
	public final ViewGroup rootPanel;
	MainActivityUIBase a;
	private TextPaint menu_grid_painter;
	ArrayList<String> menuList = new ArrayList<>();
	private int menu_width;
	
	public static class MenuItemViewHolder {
		public final DescriptiveImageView tv;
		public MenuItemViewHolder(View convertView) {
			tv = convertView.findViewById(R.id.text);
		}
	}
	
	int[] menu_ids = new int[]{
			R.string.ts_convert
			, R.string.ts_pick
//			, R.string.ts_table
			, 0
			, R.string.lucene_idx
			, R.string.lucene_search
			, 0
			, R.string.book_notes
			//, R.string.write_sth
			, 0
//			, R.string.qr_scan
//			, R.string.text_recog
			, -1
	};
	
	public SearchToolsMenu(MainActivityUIBase a, ViewGroup rootPanel) {
		for (int i = 0, id; (id = menu_ids[i++]) != -1; ) {
			menuList.add(id == 0 ? "" : a.mResource.getString(id));
		}
		menu_width =  (int) a.mResource.getDimension(R.dimen._65_);
//		menuList.add("繁简转换");
//		menuList.add("繁简选字");
//		menuList.add("繁简对照表");
//		//menuList.add("繁简通搜");
//
//		menuList.add("全文索引");
//		menuList.add("搜索引擎");
//
////		menuList.add("翻阅模式");
////		menuList.add("多行编辑");
//
//		//menuList.add("二维扫描");
//		//menuList.add("文字识别");
		
		this.a = a;
		
		this.rootPanel = rootPanel;
		TwoWayGridView mainMenuLst = rootPanel.findViewById(R.id.schtools);
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
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			mainMenuLst.setHorizontalScrollbarThumbDrawable(new ColorDrawable(0x45555555));
		}
	}
	
	static class TopThumb extends ColorDrawable {
		int pad;
		public TopThumb(int c, int pad) {
			super(c);
			this.pad = pad;
		}
		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			//super.setBounds(left, 0, right, (bottom-top)/2);
			super.setBounds(left, top + pad, right, bottom - pad);
		}
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		final int id = menu_ids[position];
		return id==0?1:0;
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
		int id = menu_ids[position];
		if (id == 0) {
			if(convertView==null) {
				convertView = new View(a);
				TwoWayAbsListView.LayoutParams lp = new TwoWayAbsListView.LayoutParams((int) (1.5 * GlobalOptions.density), menu_width);
				convertView.setLayoutParams(lp);
				convertView.setBackground(new TopThumb(0x9fffffff, (int) (8*GlobalOptions.density)));
			}
		} else {
			MenuItemViewHolder holder;
			if(convertView==null) {
				convertView = a.getLayoutInflater().inflate(R.layout.menu_item, parent, false);
				convertView.setTag(holder=new MenuItemViewHolder(convertView));
				holder.tv.textPainter = menu_grid_painter;
			} else {
				holder = (MenuItemViewHolder) convertView.getTag();
			}
			//convertView.getLayoutParams().width = id==0?1:menu_width;
			int tid = menu_ids[position];
			holder.tv.setText(menuList.get(position));
			int did = R.drawable.ic_view_comfy_2_black_24dp;
			if (tid==R.string.ts_convert) {
				did = R.drawable.ic_translate_ts;
			}
			else if (tid==R.string.book_notes) {
				did = R.drawable.ic_edit_booknotes;
			}
			holder.tv.setImageResource(did);
		}
		return convertView;
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
		final int mid = menu_ids[position];
		switch (mid) {
			case R.string.ts_convert:{
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
			case R.string.ts_pick:
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
					public boolean onAction(View v, SettingsPanel p, int flagIdxSection, int flagPos, boolean dynamic, boolean val, int storageInt) {
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
				AlertDialog d = new AlertDialog.Builder(a)
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
				ViewUtils.ensureWindowType(d, a, null);
				ViewUtils.ensureTopmost(d, a, null);
				mainMenuLst.maxHeight = h;
			} break;
			case R.string.lucene_idx:
			{
				getLuceneHelper().showBuildIndexDlg();
			} break;
			case R.string.lucene_search:
			{
				getLuceneHelper().showSearchEngineDlg();
			} break;
			case R.string.book_notes:
			{
				a.showBookNotes(0);
			} break;
		}
	}
	
	public LuceneHelper getLuceneHelper() {
		if (luceneHelper==null) {
			luceneHelper = new LuceneHelper((PDICMainActivity) a, this);
		}
		return luceneHelper;
	}
	
	LuceneHelper luceneHelper;
	
	public void refresh() {
		if (luceneHelper != null) {
			luceneHelper.indexBuilderDlg.clear();
			luceneHelper.indexSchDlg.clear();
		}
	}
}

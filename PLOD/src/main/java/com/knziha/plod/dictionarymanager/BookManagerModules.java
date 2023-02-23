package com.knziha.plod.dictionarymanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymanager.BookManager.transferRunnable;
import com.knziha.plod.widgets.ViewUtils;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookManagerModules extends BookManagerFragment<String> implements BookManagerFragment.SelectableFragment
		, View.OnClickListener, View.OnLongClickListener{
	String LastSelectedPlan;
	private ArrayList<String> scanInList;

	public BookManagerModules(){
		super();
		checkChanged=(buttonView, isChecked) -> {
			try {
				BookViewHolder vh = (BookViewHolder) ((View) buttonView.getParent()).getTag();
				int pos = vh.position;
				ListView lv = (ListView) ViewUtils.getParentByClass(vh.itemView, ListView.class);
				if (lv != this.listView) {
					pos = filtered.keyAt(pos);
				}
				lastClickedPos[(++lastClickedPosIndex) % 2] = pos;
				String key = scanInList.get(pos);
				if (isChecked)
					selector.add(key);
				else
					selector.remove(key);
				if (ViewUtils.getParentOf(vh.itemView, ListView.class) != listView) {
					dataSetChangedAt(pos);
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		};
	}

	@Override
	public void setListAdapter() {
		//String[] array = getResources().getStringArray(R.array.jazz_artist_names);
		//ArrayList<String> list = new ArrayList<String>(Arrays.asList(array));
		File def = a.SecordFile;      //!!!原配
		scanInList = new ArrayList<>();
		final HashSet<String> con = new HashSet<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(def));
			String line = in.readLine();
			while(line!=null){
				line = SU.legacySetFileName(line);
				if(!con.add(line)) {
					isDirty=true;
				} else {
					if(new File(a.ConfigFile, line).exists()) {
						scanInList.add(line);
					} else {
						isDirty=true;
					}
				}
				line = in.readLine();
			}
			in.close();
		} catch (Exception ignored) {  }
		
		String[] names= a.ConfigFile.list();
		if(names!=null) {
			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				if(!SU.isNotGroupSuffix(name)) {
					if(con.add(name)) {
						scanInList.add(name);
						isDirty=true;
					}
				}
			}
		}

		adapter = new MyAdapter(scanInList);
		setListAdapter(adapter);
		if(a!=null)  ViewUtils.restoreListPos(listView, BookManager.listPos[a.fragments.indexOf(this)]);
	}

	@Override
	public DragSortController buildController(DragSortListView dslv) {
		return new MyDSController(dslv);
	}

	@Override
	public boolean exitSelectionMode() {
		if(selector.size()>0){
			selector.clear();
			dataSetChanged(false);
			return true;
		}
		return false;
	}
	
	public void add(String filename) {
		adapter.add(filename);
		isDirty=true;
	}
	
	private class MyAdapter extends ArrayAdapter<String> {
		public MyAdapter(List<String> artists) {
			super(getActivity(), getItemLayout(), R.id.text, artists);
		}

		@Override
		public int getCount() {
			return scanInList.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			BookViewHolder vh;
			if (parent != listView && convertView!=null) {
				CMN.debug("他乡异客");
				if (!(convertView.getTag() instanceof BookViewHolder)) {
					convertView = null;
				}
			}
			boolean access = a.accessMan.isEnabled();
			if(convertView==null){
				convertView = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(), parent, false);
				vh = new BookViewHolder(convertView);
				vh.title.fixedTailTrimCount = 4;
				vh.title.setOnClickListener(BookManagerModules.this);
				vh.title.setOnLongClickListener(BookManagerModules.this);
				vh.title.setAccessibilityDelegate(acessAgent);
				vh.title.earHintAhead = "分组";
				if(parent != listView) {
					ViewUtils.setVisible(vh.handle, false);
					convertView.setBackground(null);
				}
				if (access) {
					ViewUtils.removeView(vh.handle);
					ViewUtils.addViewToParent(vh.handle, (ViewGroup) vh.itemView, 1);
				}
			} else {
				vh = (BookViewHolder) convertView.getTag();
			}
			vh.handle.setFocusable(access);
			vh.title.setClickable(access);
			vh.title.setLongClickable(access);
			vh.position = position;
			//v.getBackground().setLevel(1000);
			if(scanInList.get(position).equals(LastSelectedPlan)) {
				//((TextView)v.findViewById(R.id.text)).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorHeaderBlue));
				vh.title.setTextColor(GlobalOptions.isDark?0xFFc17d33:Color.BLUE);
				//((TextView)v.findViewById(R.id.text)).setText("✲"+((TextView)v.findViewById(R.id.text)).getText());
			} else
				vh.title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
			vh.title.setPadding((int) (GlobalOptions.density*25),0,0,0);
			
			if (parent == listView) {
				if (query != null && filtered.get(position) != null)
					vh.title.setBackgroundResource(GlobalOptions.isDark ? R.drawable.xuxian2_d : R.drawable.xuxian2);
				else
					vh.title.setBackground(null);
				if(ViewUtils.setVisibleV4(vh.handle, PDICMainAppOptions.sortDictManager()?0:1)) {
					((ViewGroup.MarginLayoutParams)vh.title.getLayoutParams()).leftMargin = PDICMainAppOptions.sortDictManager()?0: (int) (GlobalOptions.density * 6);
				}
				if(GlobalOptions.isDark) {
					convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
				}
			}

			String key = scanInList.get(position);
			vh.title.setText(key);
			
			if(true){
				vh.ck.setVisibility(View.VISIBLE);
				vh.ck.setOnCheckedChangeListener(null);
				vh.ck.setChecked(selector.contains(key));
				vh.ck.setOnCheckedChangeListener(checkChanged);
				vh.tweakCheck();
			} else
				vh.ck.setVisibility(View.GONE);
			
			return convertView;
		}
	}
	
	
	private View.AccessibilityDelegate acessAgent = new View.AccessibilityDelegate() {
		@Override
		public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
			super.onPopulateAccessibilityEvent(host, event);
			try {
				if (host.getId() == R.id.text) {
					BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(host, BookViewHolder.class);
					if (vh != null) {
						String name = scanInList.get(vh.position);
						if (selector.contains(name)) {
							event.getText().add("已选中");
						}
						if (name.equals(LastSelectedPlan)) {
							event.getText().add("是当前分组");
						}
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	};
	
	
	@Override
	public boolean onLongClick(View v) {
		if (v.getId()==R.id.text) {
			BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(v, BookViewHolder.class);
			if (vh != null) {
				return listView.getOnItemLongClickListener().onItemLongClick(listView, vh.itemView, vh.position + listView.getHeaderViewsCount(), 0);
			}
			return true;
		}
		return false;
	}
	
	// click
	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.text) {
			BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(v, BookViewHolder.class);
			if (vh != null) {
				listView.getOnItemClickListener().onItemClick(listView, vh.itemView, vh.position + listView.getHeaderViewsCount(), 0);
			}
		}
	}

	private class MyDSController extends DragSortController {

		DragSortListView mDslv;
		public MyDSController(DragSortListView dslv) {
			super(dslv);
			setDragHandleId(R.id.drag_handle);
			mDslv = dslv;
			//mDslv.setPadding(0, CMN.actionBarHeight, 0, 0);
		}

		@Override
		public View onCreateFloatView(int position) {
			View v = adapter.getView(position, null, mDslv);
			BookViewHolder vh = ((BookViewHolder) v.getTag());
			vh.ck.jumpDrawablesToCurrentState();
			//v.getBackground().setLevel(20000);
			mDslv.setFloatAlpha(1.0f);
			if(GlobalOptions.isDark) vh.title.setTextColor(Color.WHITE);
			v.setBackgroundColor(GlobalOptions.isDark?0xFFc17d33:0xFFffff00);//TODO: get primary color
			isDirty=true;
			if (a.accessMan.isEnabled()) {
				a.root.announceForAccessibility("正在拖拽 分组"+getNameAt(vh.position)+" 当前处于列表第"+vh.position+"项");
			}
			return v;
		}
		
		@Override
		public void onDestroyFloatView(View floatView) {
			//do nothing; block super from crashing
		}

	}
	
	private String getNameAt(int position) {
		String ret = scanInList.get(position);
		int idx = ret.indexOf(".");
		if (idx >= 0) {
			ret = ret.substring(0, idx);
		}
		return ret;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(BookManager) getActivity();
		setListAdapter();
		LastSelectedPlan = a.loadMan.lazyMan.lastLoadedModule; // opt.getLastPlanName("LastPlanName")
		listView.setOnItemClickListener((parent, v, position, id) -> {
			if(position >= listView.getHeaderViewsCount()) {
				pressedPos = position - listView.getHeaderViewsCount();
				pressedV = v;
				if (PDICMainAppOptions.dictManagerClickPopup()) {
					showPopup(v, null);
				} else {
					showLoadModuleDlg(PDICMainAppOptions.getWarnLoadModule(), pressedPos);
				}
			}
		});
		listView.setOnItemLongClickListener((parent, v, position, id) -> {
			if(position>= listView.getHeaderViewsCount()) {
				pressedPos = position - listView.getHeaderViewsCount();
				pressedV = v;
				if (PDICMainAppOptions.dictManagerClickPopup() && true) {
//					boolean start = mController.startDrag(position,0, v.getHeight()/2);
//					return false;
				
				} else {
					showPopup(v, null);
				}
			}
			return true;
		});
	}
	
	public PopupMenuHelper getPopupMenu() {
		if (mPopup==null) {
			mPopup  = new PopupMenuHelper(getActivity(), null, null);
			mPopup.initLayout(new int[]{
					R.string.rename
					, R.string.delete
					, R.string.duplicate
					, R.string.qiehan_sel
					, R.string.load
			}, new PopupMenuHelper.PopupMenuListener() {
				@Override
				public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View view, boolean isLongClick) {
					int position = pressedPos;
					ListView lv = (ListView) ViewUtils.getParentOf(pressedV, ListView.class);
					boolean b1 = lv!=listView;
					final String name = scanInList.get(position);
					boolean isOnSelected = !b1 && selector.size() > 0 && selector.contains(name);
					if (isLongClick) {
						//if (view.getId() == R.id.disable) {
						//	popupMenuHelper.dismiss();
						//}
						return false;
					}
					switch (view.getId()) {
						/* 选中 / 取消选中 */
						case R.string.qiehan_sel: {
							if (!selector.remove(name))
								selector.add(name);
							dataSetChangedAt(position);
						} break;
						/* 加载分组 */
						case R.string.load: {
							showLoadModuleDlg(false, position);
						} break;
						/* 重命名分组 */
						case R.string.rename: {
							((BookManager) getActivity()).showRenameDialog(name, new transferRunnable() {
								@Override
								public boolean transfer(File to) {
									File p = a.ConfigFile;
									try {
										if (!to.getParentFile().getCanonicalFile().getAbsolutePath().equals(p.getCanonicalFile().getAbsolutePath()))
											return false;
									} catch (IOException e) {
										return false;
									}
									String fn = name;
									boolean doNothingToList = false;
									if (to.exists()) {//文件覆盖已预先处理。
										//adapter.remove(to.getName().substring(0,to.getName().length()-4));
										if (to.getName().equals(fn))
											return true;
										doNothingToList = true;
									}
									
									boolean ret = new File(p, fn).renameTo(to);
									if (ret) {
										scanInList.set(position, to.getName());
										dataSetChangedAt(position);
										isDirty = true;
										a.show(R.string.renD);
										if (b1) {
											if (lv != null) {
												((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
											}
										}
									}
									return ret;
								}
								@Override
								public void afterTransfer() {
									//当架构崩盘
									if (position >= adapter.getCount() || !adapter.getItem(position).equals(name)) {
										//d.dismiss();
									}
								}
							});
						} break;
						/* 删除 */
						case R.string.delete: {
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
									.setTitle(isOnSelected ? getResources().getString(R.string.warnDeleteMultiple, selector.size())
											: getResources().getString(R.string.warnDelete, name)
									)
									.setMessage("不可撤销！但会备份分组文件至PLOD文件夹，可手动恢复。")
									.setWikiText("打开备份文件夹", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											File folder = a.fileToSet("beifen");
											StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
											try {
												startActivity(new Intent(Intent.ACTION_VIEW)
														.setDataAndType(Uri.fromFile(folder), "resource/folder")
														.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
												);
											} catch (Exception e) {
											
											}
											getBookManager().showT(folder.getPath());
										}
									})
									.setPositiveButton("确认", (dialog, which) -> {
										if (isOnSelected) {
											for (String nI : selector) {
												try_delete_configureLet(nI);
												scanInList.remove(nI);
											}
											selector.clear();
										} else if (try_delete_configureLet(name)) {
											selector.remove(name);
											scanInList.remove(name);
										}
										dataSetChanged(true);
										isDirty = true;
										dialog.dismiss();
										a.show(R.string.delD);
									});
							final AlertDialog dd = builder.create();
							dd.show();
							dd.getWindow().setDimAmount(0);
						} break;
						/* 复制 */
						case R.string.duplicate: {
							CMN.debug("duplicate");
							try {
								File source = new File(a.ConfigFile, name);
								String newName = name.substring(0, name.length()-4);
								int try_idx = 0;
								Pattern p = Pattern.compile("_[0-9]+$");
								Matcher m = p.matcher(newName);
								if (m.find()) {
									try {
										String numfix = m.group(0);
										newName = newName.substring(0, newName.length() - numfix.length());
										try_idx = IU.parseInt(numfix);
									} catch (Exception e) {
										CMN.debug(e);
									}
								}
								File dest;
								while (true) {
									dest = new File(source.getParent(), newName + "_" + try_idx + ".set");
									if (!dest.exists() || dest.isDirectory())
										break;
									try_idx++;
								}
								FileChannel inputChannel = new FileInputStream(source).getChannel();
								FileChannel outputChannel = new FileOutputStream(dest).getChannel();
								outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
								inputChannel.close();
								outputChannel.close();
								a.show(R.string.dupicateD);
								scanInList.add(pressedPos+1, dest.getName());
								dataSetChanged(true);
							} catch (Exception e) {
								CMN.debug(e);
							}
							isDirty = true;
						} break;
					}
					popupMenuHelper.dismiss();
					if (b1) {
						dataSetChangedAt(position);
						if (lv != null) {
							((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
						}
					}
					return true;
				}
			});
		}
		return mPopup;
	}
	
	private void showLoadModuleDlg(boolean warn, int position) {
		if (warn && !PDICMainAppOptions.debug()) {
			final View dv = getActivity().getLayoutInflater().inflate(R.layout.dialog_sure_and_all, null);
			CheckBox ck = dv.findViewById(R.id.ck);
			TextView tv = dv.findViewById(R.id.title);
			tv.setOnClickListener(v -> ck.toggle());
			ck.setChecked(!PDICMainAppOptions.getWarnLoadModule());
			ck.setOnCheckedChangeListener((buttonView, isChecked) -> PDICMainAppOptions.setWarnLoadModule(!buttonView.isChecked()));
			tv.setText("重启前不再提示");
			AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
			builder2.setView(dv).setTitle("是否确认加载 " + adapter.getItem(position) + "?")
					.setPositiveButton(R.string.confirm, (dialog, which) -> {
						showLoadModuleDlg(false, position);
					})
					.setNeutralButton(R.string.cancel, null);
			AlertDialog dlg = builder2.create();
			builder2.create().show();
			dlg.getWindow().setDimAmount(0);
		} else {
			String name = adapter.getItem(position);
			File newf = new File(a.ConfigFile, SU.legacySetFileName(name));
			int cc=0;
			a.checkModuleDirty(false);
			BookManager.listPos[0] = 0;
			try {
				BookManagerMain f1 = a.f1;
				f1.markDirty(-1);
				for (int i = 0, sz=f1.manager_group().size(); i < sz; i++) {
					f1.setPlaceSelected(i, false);
				}
				a.loadMan.lazyMan.chairCount = -1;
				a.loadMan.LoadLazySlots(newf, true, name);
				f1.refreshSize();
				f1.markDataDirty(false);
				((BookManager)getActivity()).scrollTo(0);
				try {
					String plan = a.loadMan.dictPicker.planSlot;
					a.opt.putLastPlanName(plan, LastSelectedPlan = name);
				} catch (Exception e) {
					CMN.debug(e);
				}
				dataSetChanged(false);
				f1.dataSetChanged(true);
				//a.show(R.string.pLoadDone,name,cc,f1.manager_group().size());
				MainActivityUIBase.LazyLoadManager neoLM = a.loadMan.lazyMan;
				CMN.debug("LoadLazySlots::", neoLM.chairCount, neoLM.CosyChair.length, neoLM.filterCount, neoLM.CosySofa.length);
			} catch (Exception e) {
				CMN.debug(e);
				a.showT("加载异常!LOAD ERRO: "+e.getLocalizedMessage());
			}
		}
	}
	
	private char[] get4kCharBuff() {
		if(a==null) return null;
		return ((AgentApplication)a.getApplication()).get4kCharBuff();
	}

	private boolean try_delete_configureLet(String name) {
		File importantFile =  a.fileToSet(name);
		File backup =  a.fileToSet("/beifen/"+name);
		try {
			backup.getParentFile().mkdirs();
			FileChannel inputChannel = new FileInputStream(importantFile).getChannel();
			FileChannel outputChannel = new FileOutputStream(backup).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			inputChannel.close();
			outputChannel.close();
		} catch (Exception e) {
			CMN.debug(e);
		}
		// todo trim backup
		return importantFile.delete();
	}

	DragSortListView.DropListener getDropListener() {
//		DragSortListView.DropListener onDrop =
//				(from, to) -> {
//					if (from != to) {
//						String item = adapter.getItem(from);
//						adapter.remove(item);
//						adapter.insert(item, to);
//					}
//				};
		return (from, to) -> {
			//CMN.Log("to", to);
			//if(true) return;
			int pos=-1, top=0;
			int cc=0, initialTo=to;
			BookViewHolder vh = (BookViewHolder) ViewUtils.getViewHolderInParents(listView.getChildAt(0), BookViewHolder.class);
			if (vh != null) {
				pos = vh.position;
				top = ViewUtils.getNthParentNonNull(vh.itemView, 1).getTop();
			}
			String fromPath = scanInList.get(from);
			if(selector.contains(fromPath)){
				ArrayList<String> md_selected = new ArrayList<>(selector.size());
				if(to>from) to++;
				for (int i = scanInList.size()-1; i >= 0; i--) {
					String mmTmp = scanInList.get(i);
					if(selector.contains(mmTmp)){
						md_selected.add(0, scanInList.remove(i));
						if(i<to) to--;
						cc++;
					}
					if (i < pos) {
						pos--;
					}
				}
				scanInList.addAll(to, md_selected);
				dataSetChanged(true);
			}
			else if (from != to) {
				String mdTmp = scanInList.remove(from);
				scanInList.add(to, mdTmp);
				dataSetChanged(true);
				if (from < pos) {
					pos--;
				}
				cc=1;
			}
			if (pos>=0) {
				listView.setSelectionFromTop(pos + listView.getHeaderViewsCount(), top);
			}
			
			if (a.accessMan.isEnabled()) {
				if (cc > 0) {
					int finalCc = cc, fvp = 0;
					for (int i = 0, sz = scanInList.size(); i < sz; i++) {
						if (scanInList.get(i).equals(fromPath)) {
							fvp = i;
							break;
						}
					}
					View child = ViewUtils.findChild(listView, fvp + listView.getHeaderViewsCount());
					if (child != null) {
						((BookViewHolder) child.getTag()).handle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
					}
					a.accessMan.interrupt();
					a.root.postDelayed(() -> {
						a.accessMan.interrupt();
						a.root.announceForAccessibility("已经拖拽 " + finalCc + "个分组，从" + from + "到" + initialTo);
					}, 250);
				} else {
					View child = ViewUtils.findChild(listView, from+listView.getHeaderViewsCount());
					if (child != null) {
						((BookViewHolder)child.getTag()).handle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
					}
				}
			}
		};
	}
	
	public int schFilter(String query, boolean shouldInval) {
		if (!query.equals(this.query)) {
			getBookManager().popupPos[1] = 0;
			this.query = query;
		}
		int sz = filtered.size();
		filtered.clear();
		if (!TextUtils.isEmpty(query)) {
			for (int i = 0; i < scanInList.size(); i++) {
				String name = scanInList.get(i);
				int suffixIdx = name.lastIndexOf("."), sch=name.toLowerCase().indexOf(query);
				if (sch>=0 && (suffixIdx==-1 || sch<suffixIdx)) {
					filtered.put(i, name);
				}
			}
		}
		if (shouldInval && !(sz==0 && filtered.size()==0)) {
			dataSetChanged(false);
		}
		return filtered.size();
	}
	
	@Override
	public int selected_size() {
		return selector.size();
	}
	
}

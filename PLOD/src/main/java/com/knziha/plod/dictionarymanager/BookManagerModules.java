package com.knziha.plod.dictionarymanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymanager.BookManager.transferRunnable;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.ArrayAdapterHardCheckMark;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class BookManagerModules extends BookManagerFragment<String> implements BookManagerFragment.SelectableFragment {
	String LastSelectedPlan;
	private ArrayList<String> scanInList;

	public BookManagerModules(){
		super();
		checkChanged=(buttonView, isChecked) -> {
			BookManagerMain.ViewHolder vh = (BookManagerMain.ViewHolder) ((View)buttonView.getParent()).getTag();
			lastClickedPos[(++lastClickedPosIndex)%2]=vh.position;
			String key = scanInList.get(vh.position);
			if(isChecked)
				selector.add(key);
			else
				selector.remove(key);
		};
	}

	@Override
	public int getItemLayout() {
		return R.layout.dict_manager_dslitem;
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
	}

	@Override
	public DragSortController buildController(DragSortListView dslv) {
		return new MyDSController(dslv);
	}

	@Override
	public boolean exitSelectionMode() {
		if(selector.size()>0){
			selector.clear();
			adapter.notifyDataSetChanged();
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
			BookManagerMain.ViewHolder vh;
			if(convertView==null){
				convertView = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(), parent, false);
				vh = new BookManagerMain.ViewHolder(convertView);
				vh.title.fixedTailTrimCount = 4;
			} else {
				vh = (BookManagerMain.ViewHolder) convertView.getTag();
			}
			vh.position = position;
			//v.getBackground().setLevel(1000);
			if(scanInList.get(position).equals(LastSelectedPlan)) {
				//((TextView)v.findViewById(R.id.text)).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorHeaderBlue));
				vh.title.setTextColor(GlobalOptions.isDark?0xFFc17d33:Color.BLUE);
				//((TextView)v.findViewById(R.id.text)).setText("✲"+((TextView)v.findViewById(R.id.text)).getText());
			} else
				vh.title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
			vh.title.setPadding((int) (GlobalOptions.density*25),0,0,0);
			
			if(query!=null && filtered.get(position)!=null)
				vh.title.setBackgroundResource(R.drawable.xuxian2);
			else
				vh.title.setBackground(null);
			
			if(GlobalOptions.isDark) {
				convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
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
			BookManagerMain.ViewHolder vh = ((BookManagerMain.ViewHolder) v.getTag());
			vh.ck.jumpDrawablesToCurrentState();
			//v.getBackground().setLevel(20000);
			mDslv.setFloatAlpha(1.0f);
			if(GlobalOptions.isDark) vh.title.setTextColor(Color.WHITE);
			v.setBackgroundColor(GlobalOptions.isDark?0xFFc17d33:0xFFffff00);//TODO: get primary color
			isDirty=true;
			return v;
		}

		@Override
		public void onDestroyFloatView(View floatView) {
			//do nothing; block super from crashing
		}

	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(BookManager) getActivity();
		setListAdapter();
		LastSelectedPlan = a.opt.getLastPlanName("LastPlanName");
		listView.setOnItemClickListener((parent, v, position, id) -> {
			if(position>= listView.getHeaderViewsCount()) {
				pressedPos = position - listView.getHeaderViewsCount();
				pressedV = v;
				if (PDICMainAppOptions.dictManagerClickPopup()) {
					showPopup(v);
				} else {
					showLoadModuleDlg(PDICMainAppOptions.getWarnLoadModule(), position);
				}
			}
		});
		listView.setOnItemLongClickListener((parent, v, position, id) -> {
			if(position>= listView.getHeaderViewsCount()) {
				pressedPos = position - listView.getHeaderViewsCount();
				pressedV = v;
				showPopup(v);
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
					final String name = scanInList.get(position);
					boolean isOnSelected = selector.size() > 0 && selector.contains(name);
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
							adapter.notifyDataSetChanged();
						} break;
						/* 加载分组 */
						case R.string.load: {
							showLoadModuleDlg(true, position);
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
										adapter.remove(name);
										if (!doNothingToList) {
											int newPos = position;
											//if(newPos>1)
											//if(adapter.getItem(newPos-1).equals(name))
											//	newPos--;
											String name = to.getName();
											newPos = newPos > adapter.getCount() ? adapter.getCount() : newPos;
											adapter.insert(name.substring(0, name.length() - 4), newPos);
										}
										isDirty = true;
										a.show(R.string.renD);
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
							View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.dialog_about, null);
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							TextView tvtv = dialog1.findViewById(R.id.title);
							tvtv.setText(isOnSelected ? getResources().getString(R.string.warnDeleteMultiple, selector.size())
									: getResources().getString(R.string.warnDelete, name)
							);
							tvtv.setPadding(50, 50, 0, 0);
							builder.setView(dialog1);
							final AlertDialog dd = builder.create();
							dialog1.findViewById(R.id.cancel).setOnClickListener(v -> {
								if (isOnSelected) {
									for (String nI : selector) {
										try_delete_configureLet(nI);
										scanInList.remove(nI);
									}
									selector.clear();
									adapter.notifyDataSetChanged();
								} else if (try_delete_configureLet(name)) {
									selector.remove(name);
									adapter.remove(name);
								}
								isDirty = true;
								dd.dismiss();
								a.show(R.string.delD);
							});
							dd.show();
						} break;
						/* 复制 */
						case R.string.duplicate: {
							CMN.debug("duplicate");
							try {
								String newName = name.substring(0, name.length()-4);
								File source = new File(a.ConfigFile, name);
								int try_idx = 0;
								File dest;
								while (true) {
									dest = new File(source.getParent(), newName + "." + try_idx + ".set");
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
								adapter.notifyDataSetChanged();
							} catch (Exception e) {
								CMN.debug(e);
							}
							isDirty = true;
						} break;
					}
					popupMenuHelper.dismiss();
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
			builder2.create().show();
		} else {
			String name = adapter.getItem(position);
			File newf = new File(a.ConfigFile, SU.legacySetFileName(name));
			int cc=0;
			a.checkModuleDirty(false);
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
				a.opt.putLastPlanName("LastPlanName", LastSelectedPlan = name);
				dataSetChanged();
				f1.dataSetChanged();
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
		return a.fileToSet(name).delete();
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
			BookManagerMain.ViewHolder vh = (BookManagerMain.ViewHolder) ViewUtils.getViewHolderInParents(listView.getChildAt(0), BookManagerMain.ViewHolder.class);
			if (vh != null) {
				pos = vh.position;
				top = ViewUtils.getNthParentNonNull(vh.itemView, 1).getTop();
			}
			if(selector.contains(scanInList.get(from))){
				ArrayList<String> md_selected = new ArrayList<>(selector.size());
				if(to>from) to++;
				for (int i = scanInList.size()-1; i >= 0; i--) {
					String mmTmp = scanInList.get(i);
					if(selector.contains(mmTmp)){
						md_selected.add(0, scanInList.remove(i));
						if(i<to) to--;
					}
					if (i < pos) {
						pos--;
					}
				}
				scanInList.addAll(to, md_selected);
				adapter.notifyDataSetChanged();
			}
			else if (from != to) {
				String mdTmp = scanInList.remove(from);
				scanInList.add(to, mdTmp);
				adapter.notifyDataSetChanged();
				if (from < pos) {
					pos--;
				}
			}
			if (pos>=0) {
				listView.setSelectionFromTop(pos + listView.getHeaderViewsCount(), top);
			}
		};
	}
	
	public int schFilter(String query) {
		int prvSz = filtered.size();
		this.query = query;
		filtered.clear();
		if (!TextUtils.isEmpty(query)) {
			for (int i = 0; i < scanInList.size(); i++) {
				String name = scanInList.get(i);
				if (name.toLowerCase().indexOf(query)>0) {
					filtered.put(i, name);
				}
			}
		}
		if (!(filtered.size()==0 && prvSz==0)) {
			adapter.notifyDataSetChanged();
		}
		return filtered.size();
	}
	
}

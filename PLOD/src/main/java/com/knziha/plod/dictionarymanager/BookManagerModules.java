package com.knziha.plod.dictionarymanager;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymanager.BookManager.transferRunnable;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.widgets.ArrayAdapterHardCheckMark;
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
				if(!SU.isNoneSetFileName(name)) {
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
				convertView.setTag(vh = new BookManagerMain.ViewHolder(convertView));
			} else {
				vh = (BookManagerMain.ViewHolder) convertView.getTag();
			}
			vh.position = position;
			//v.getBackground().setLevel(1000);
			if(scanInList.get(position).equals(LastSelectedPlan)) {
				//((TextView)v.findViewById(R.id.text)).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorHeaderBlue));
				vh.title.setTextColor(Color.BLUE);
				//((TextView)v.findViewById(R.id.text)).setText("✲"+((TextView)v.findViewById(R.id.text)).getText());
			}else
				vh.title.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);

			if(GlobalOptions.isDark) {
				convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}

			String key = scanInList.get(position);
			vh.title.setText(key);

			if(a.opt.getDictManager1MultiSelecting()){
				vh.ck.setVisibility(View.VISIBLE);
				vh.ck.setOnCheckedChangeListener(null);
				vh.ck.setChecked(selector.contains(key));
				vh.ck.setOnCheckedChangeListener(checkChanged);
			}else
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
			((BookManagerMain.ViewHolder)v.getTag()).ck.jumpDrawablesToCurrentState();
			//v.getBackground().setLevel(20000);
			mDslv.setFloatAlpha(1.0f);
			v.setBackgroundColor(Color.parseColor("#ffff00"));//TODO: get primary color
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

		mDslv.setOnItemClickListener((parent, view, position, id) -> {
			if(position>=mDslv.getHeaderViewsCount()) {
				position = position - mDslv.getHeaderViewsCount();
				String name = adapter.getItem(position);
				File newf = new File(a.ConfigFile, SU.legacySetFileName(name));
				int cc=0;
				try {
					BookManagerMain f1 = ((BookManager)getActivity()).f1;
					f1.isDirty=true;
					ReusableBufferedReader in = new ReusableBufferedReader(new FileReader(newf), get4kCharBuff(), 4096);
					a.ThisIsDirty=true;
					f1.rejector.clear();
					a.do_Load_managee(in);
					f1.refreshSize();
					((BookManager)getActivity()).scrollTo(0);
					a.opt.putLastPlanName("LastPlanName", LastSelectedPlan = name);
					f1.isDirty=true;
					adapter.notifyDataSetChanged();
					f1.adapter.notifyDataSetChanged();
					a.show(R.string.pLoadDone,name,cc,a.mdmng.size());
				} catch (Exception e2) {
					e2.printStackTrace();
					a.showT("加载异常!LOAD ERRO: "+e2.getLocalizedMessage());
				}
			}
		});

		mDslv.setOnItemLongClickListener(new OnItemLongClickListener() {
			AlertDialog d;
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if(position>=mDslv.getHeaderViewsCount()) {
					AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
					final int actualPosition = position - mDslv.getHeaderViewsCount();
					builder2.setTitle(R.string.setOpt);
					builder2.setSingleChoiceItems(new String[] {}, 0,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int pos) {
									final String name = adapter.getItem(actualPosition);
									switch(pos) {
										case 0://模块的 重命名
											((BookManager)getActivity()).showRenameDialog(name, new transferRunnable() {
												@Override
												public boolean transfer(File to) {
													File p=a.ConfigFile;
													try {
														if(!to.getParentFile().getCanonicalFile().getAbsolutePath().equals(p.getCanonicalFile().getAbsolutePath()))
															return false;
													} catch (IOException e) {return false;}

													String fn = name;
													boolean doNothingToList=false;
													if(to.exists()) {//文件覆盖已预先处理。
														//adapter.remove(to.getName().substring(0,to.getName().length()-4));
														if(to.getName().equals(fn))
															return true;
														doNothingToList=true;
													}

													boolean ret = new File(p,fn).renameTo(to);
													if(ret) {
														d.dismiss();
														adapter.remove(name);
														if(!doNothingToList) {
															int newPos = actualPosition;
															//if(newPos>1)
															//if(adapter.getItem(newPos-1).equals(name))
															//	newPos--;
															String name = to.getName();
															newPos=newPos>adapter.getCount()?adapter.getCount():newPos;
															adapter.insert(name.substring(0,name.length()-4), newPos);
														}
														isDirty=true;
														a.show(R.string.renD);
													}
													return ret;
												}
												@Override
												public void afterTransfer() {
													//当架构崩盘
													if(actualPosition>=adapter.getCount() || !adapter.getItem(actualPosition).equals(name)) {
														d.dismiss();
													}
												}});
											break;
										case 1:
											boolean deleteMultiple = a.opt.getDictManager1MultiSelecting() && selector.size()>0 && selector.contains(name);
											View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.dialog_about,null);
											AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
											TextView tvtv = dialog1.findViewById(R.id.title);
											tvtv.setText(deleteMultiple?getResources().getString(R.string.warnDeleteMultiple,selector.size())
													:getResources().getString(R.string.warnDelete,name)
													);
											tvtv.setPadding(50, 50, 0, 0);
											builder.setView(dialog1);
											final AlertDialog dd = builder.create();
											dialog1.findViewById(R.id.cancel).setOnClickListener(v -> {
												if(deleteMultiple){
													for(String nI:selector){
														try_delete_configureLet(nI);
														scanInList.remove(nI);
													}
													selector.clear();
													adapter.notifyDataSetChanged();
												} else if(try_delete_configureLet(name)) {
													selector.remove(name);
													adapter.remove(name);
												}
												isDirty=true;
												d.dismiss();
												dd.dismiss();
												a.show(R.string.delD);
											});
											dd.show();
											break;
										case 2://复制;
											File source  = new File(a.ConfigFile, name+".set");
											int try_idx=0;
											File dest;
											while(true) {
												dest = new File(source.getParent(),name+"."+try_idx+".set");
												if(!dest.exists() || dest.isDirectory())
													break;
												try_idx++;
											}
											FileChannel inputChannel = null;
											FileChannel outputChannel = null;
											try {
												inputChannel = new FileInputStream(source).getChannel();
												outputChannel = new FileOutputStream(dest).getChannel();
												outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
												inputChannel.close();
												outputChannel.close();
												a.show(R.string.dupicateD);
												adapter.add(name+"("+try_idx+")");
											} catch (Exception e) {
												e.printStackTrace();
											}
											isDirty=true;
										break;
									}
								}
							});

					d = builder2.create();
					String[] Menus = getResources().getStringArray(
							R.array.module_sets_option);
					List<String> arrMenu = Arrays.asList(Menus);
					d.show();
					d.getListView().setAdapter(new ArrayAdapterHardCheckMark<>(getActivity(),
							R.layout.singlechoice, android.R.id.text1, arrMenu));
				}
				return true;
			}});
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
			if(a.opt.getDictManager1MultiSelecting() && selector.contains(scanInList.get(from))){
				ArrayList<String> md_selected = new ArrayList<>(selector.size());
				if(to>from) to++;
				for (int i = scanInList.size()-1; i >= 0; i--) {
					String mmTmp = scanInList.get(i);
					if(selector.contains(mmTmp)){
						md_selected.add(0, scanInList.remove(i));
						if(i<to) to--;
					}
				}
				scanInList.addAll(to, md_selected);
				adapter.notifyDataSetChanged();
			}
			else if (from != to) {
				String mdTmp = scanInList.remove(from);
				scanInList.add(to, mdTmp);
				adapter.notifyDataSetChanged();
			}
		};
	}
}

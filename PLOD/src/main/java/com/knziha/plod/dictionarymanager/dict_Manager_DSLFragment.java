package com.knziha.plod.dictionarymanager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_nonexist;
import com.knziha.plod.dictionarymodels.mdict_prempter;
import com.knziha.plod.widgets.CheckedTextViewmy;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class dict_Manager_DSLFragment extends dict_manager_DSLFragmenr_base<mdict> {
	HashMap<String,mdict> mdict_cache = new HashMap<>();
	mdict currentFilter;
	Intent currentFilterNotifier = new Intent();
	HashSet<String> rejector = new HashSet<>();
	dict_manager_activity aaa;

	//构造
	public dict_Manager_DSLFragment(){
		super();

	}

	public void refreshSize(){
		a.mTabLayout.getTabAt(0).setText(getResources().getString(R.string.currentPlan,a.md.size()-rejector.size()));
	}

	@Override
	public int getItemLayout() {
		return R.layout.dict_manager_dslitem;
	}

	@Override
	public void setListAdapter() {
		List<mdict> list = a.md;
		adapter = new MyAdapter(list);
		setListAdapter(adapter);
	}

	@Override
	public DragSortController buildController(DragSortListView dslv) {
		return new MyDSController(dslv);
	}


	private class MyAdapter extends ArrayAdapter<mdict> {
		public MyAdapter(List<mdict> mdicts) {
			super(getActivity(), getItemLayout(), R.id.text, mdicts);
		}

		@NonNull
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			ViewHolder vh = v.getTag()==null?new ViewHolder(v):(ViewHolder) v.getTag();
			//v.getBackground().setLevel(1000);
			//position = position - mDslv.getHeaderViewsCount();
			mdict mdTmp = adapter.getItem(position);
			if(mdTmp.cover!=null) {
				SpannableStringBuilder ssb = new SpannableStringBuilder("| ").append(aaa.isDebug?mdTmp.getPath():mdTmp._Dictionary_fName);
				mdTmp.cover.setBounds(0, 0, 50, 50);
				ssb.setSpan(new ImageSpan(mdTmp.cover), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				vh.title.setText(ssb);
			}else
				vh.title.setText(aaa.isDebug?mdTmp.getPath():mdTmp._Dictionary_fName);//
			if(aaa.isSearching && mdTmp._Dictionary_fName.toLowerCase().contains(aaa.dictQueryWord))
				vh.title.setBackgroundResource(R.drawable.xuxian2);
			else
				vh.title.setBackground(null);

			//((TextView)v.findViewById(R.id.text)).setTextColor(Color.parseColor("#000000"));
			StringBuilder rgb = new StringBuilder("#");

			if(rejector.contains(adapter.getItem(position).getPath()))
				rgb.append("aaaaaa");//一样的亮兰色aafafa
			else
				rgb.append(GlobalOptions.isDark?"EEEEEE":"000000");
			if(mdict_nonexist.class==adapter.getItem(position).getClass())
				rgb.insert(1, "ff");
			rgb.setLength(7);

			vh.title.setTextColor(Color.parseColor(rgb.toString()));
			if(currentFilter==mdTmp){
				if(v.getTag(R.drawable.filter)==null){
					Drawable d = getResources().getDrawable(R.drawable.filter);
					int h = vh.title.getLineHeight();
					d.setBounds(new Rect(0,0, h, h));
					vh.title.setCompoundDrawables(d, null, null, null);
					v.setTag(R.drawable.filter, false);
				}
			} else if(v.getTag(R.drawable.filter)!=null){
				vh.title.setCompoundDrawables(null, null, null, null);
				v.setTag(R.drawable.filter, null);
			}
			if(GlobalOptions.isDark) {
				v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}
			return v;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(dict_manager_activity) getActivity();
		for(mdict mdTmp:a.md) {
			mdict_cache.put(mdTmp.getPath(),mdTmp);
		}

		File def = new File(a.getExternalFilesDir(null),"default.txt");
		try {
			BufferedReader in = new BufferedReader(new FileReader(def));
			String line = in.readLine();
			int idx=0;
			while(line!=null){
				boolean isFilter=false;
				if(line.startsWith("[:F]")){
					line = line.substring(4);
					isFilter=true;
				}
				if(!line.startsWith("/"))
					line=a.opt.lastMdlibPath+"/"+line;
				if(!mdict_cache.containsKey(line)) {
					if(idx<=a.md.size())
						a.md.add(idx,new mdict_nonexist(line,a.opt));
				}
				if(isFilter){
					if(currentFilter==null)
						currentFilter=a.md.get(idx);
					else
						isDirty=true;
				}
				idx++;
				line = in.readLine();
			}
			in.close();

		} catch (IOException e2) {
			e2.printStackTrace();
		}


		aaa = (dict_manager_activity) getActivity();
		mDslv.setOnItemClickListener((parent, view, position, id) -> {
			//CMN.show(""+(adapter==null)+" "+(((dict_manager_activity)getActivity()).f1.adapter==null));
			isDirty=true;
			//adapter.getItem(position).value = !adapter.getItem(position).value;//TODO optimize
			if(position>=mDslv.getHeaderViewsCount()) {
				position = position - mDslv.getHeaderViewsCount();

				if(rejector.contains(adapter.getItem(position).getPath()))
					rejector.remove(adapter.getItem(position).getPath());
				else
					rejector.add(adapter.getItem(position).getPath());
				adapter.notifyDataSetChanged();
				isDirty=true;
				refreshSize();
			}
		});

		mDslv.setOnItemLongClickListener(new OnItemLongClickListener() {
			AlertDialog d;
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				if(position>=mDslv.getHeaderViewsCount()) {
					AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
					final int actualPosition = position - mDslv.getHeaderViewsCount();
					SpannableStringBuilder ssb = new SpannableStringBuilder(getResources().getString(R.string.dictOpt)).append("\r\n").append(a.md.get(actualPosition).f().getAbsolutePath());
					ssb.setSpan(new RelativeSizeSpan(0.63f), ssb.toString().indexOf("…"), ssb.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					builder2.setTitle(ssb);
					builder2.setSingleChoiceItems(new String[] {}, 0,
							(dialog, pos) -> {
								mdict mdTmp = a.md.get(actualPosition);
								switch(pos) {
									case 0:{
										View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
										final ListView lv = dialog1.findViewById(R.id.lv);
										final EditText et = dialog1.findViewById(R.id.et);
										ImageView iv = dialog1.findViewById(R.id.confirm);
										et.setText(a.md.get(actualPosition)._Dictionary_fName);

										AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
										builder.setView(dialog1);
										builder.setIcon(R.mipmap.ic_directory_parent);
										final AlertDialog dd = builder.create();

										iv.setOnClickListener(v -> {
											String oldEUFn = mdTmp.getPath();
											String oldFn = mFile.tryDeScion(new File(oldEUFn), a.opt.lastMdlibPath);
											File to = new File(mdTmp.f().getParent(),et.getText().toString()+".mdx");
											//Log.e("XXX-filepath", to.getAbsolutePath());
											try {
												to=to.getCanonicalFile();
											} catch (IOException e1) {
												e1.printStackTrace();
											}
											//Log.e("XXX-filepath2", to.getAbsolutePath());
											String toFn = to.getAbsolutePath();
											if(toFn.startsWith(a.opt.lastMdlibPath))
												toFn = toFn.substring(a.opt.lastMdlibPath.length()+1);

											boolean suc = false;
											if(to.equals(mdTmp.f())) {//就是自己
												suc=true;
											}
											else if(new File(mdTmp.getPath()).exists()) {//正常重命名
												if(to.exists()) {
													a.showT("文件已存在，重命名失败！");
												}
												else if(mdTmp.renameFileTo(to)) {//正常重命名成功
													suc=true;
												}
											}else {
												if(to.exists() && !mdict_cache.containsKey(to.getAbsolutePath())) {//关联已存在的文件
													mdTmp.renameFileTo(to);
													CMN.Log("重命名",mdTmp._Dictionary_fName_Internal);
													adapter.remove(mdTmp);
													adapter.insert(new mdict_prempter(to.getAbsolutePath(),a.opt), actualPosition);
													suc=true;
												}
											}
											if(suc) {
												if(mdTmp == currentFilter)
													((dict_manager_activity)getActivity()).intent.putExtra(":F", currentFilter.f().getAbsolutePath());
												if(rejector.contains(oldEUFn)) {
													rejector.remove(oldEUFn);
													rejector.add(to.getAbsolutePath());
												}
												adapter.notifyDataSetChanged();
												isDirty=true;
												d.dismiss();
												dd.dismiss();
												a.show(R.string.renD);
												File[] moduleFullScanner = new File(a.opt.pathToMain()+"CONFIG").listFiles(pathname -> {
													String name = pathname.getName();
													if(name.endsWith(".set")) {
														return true;
													}
													return false;
												});
												ArrayList<File> moduleFullScannerArr = new ArrayList<File>(Arrays.asList(moduleFullScanner));
												moduleFullScannerArr.add(new File(a.opt.pathToMain()+"CONFIG/mdlibs.txt"));
												for(File fI:moduleFullScannerArr) {
													InputStreamReader reader = null;
													StringBuffer sb= new StringBuffer("");
													String line = "";

													try {
														reader = new InputStreamReader(new FileInputStream(fI));
														BufferedReader br = new BufferedReader(reader);
														while((line = br.readLine()) != null) {
															try {
																if(line.equals(oldFn) || new File(line).getCanonicalPath().equals(oldFn)){
																	//System.out.println(line);
																	line = toFn;
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
												dict_Manager_folderlike_DSLFragment f3 = ((dict_manager_activity)getActivity()).f3;
												if(f3.dataPrepared) {
													//int idx = f3.data.indexOf(new mFile(oldEUFn));
													int idx = f3.data.remove(new mFile(oldEUFn));
													if(idx!=-1) {
														f3.data.insert(new mFile(to).init());
													}
												}
											}else {
												a.showT("重命名失败!");
											}
										});
										dd.show();
									} break;
									case 1:{//多选

									} break;
									case 2:{//移至顶部
										isDirty=true;
										a.md.add(0,mdTmp);
										d.dismiss();
										adapter.notifyDataSetChanged();
										getListView().setSelection(0);
									} break;
									case 3:{//移至底部
										isDirty=true;
										a.md.add(a.md.size(),mdTmp);
										d.dismiss();
										adapter.notifyDataSetChanged();
										getListView().setSelection(a.md.size()-1);
									} break;
									case 4:{//设为滤器
										isDirty=true;
										d.dismiss();
										if(currentFilter != mdTmp){
											currentFilter = mdTmp;
										}else{
											currentFilter = null;
										}
										((dict_manager_activity)getActivity()).intent.putExtra(":F", currentFilter==null?":N":currentFilter.f().getAbsolutePath());
										adapter.notifyDataSetChanged();
									} break;
								}
							});

					String[] Menus = getResources().getStringArray(
							R.array.dicts_option);
					List<String> arrMenu = Arrays.asList(Menus);
					d = builder2.create();
					d.show();

					d.getListView().setAdapter(new ArrayAdapter<String>(getActivity(),
							R.layout.singlechoice, android.R.id.text1, arrMenu) {
						@Override
						public int getCount() {
							return super.getCount();
						}

						@NonNull @Override
						public View getView(int position,  View convertView, @NonNull ViewGroup parent) {
							View ret =  super.getView(position, convertView, parent);
							CheckedTextViewmy tv;
							if(ret.getTag()==null)
								ret.setTag(tv = ret.findViewById(android.R.id.text1));
							else
								tv = (CheckedTextViewmy)ret.getTag();

							tv.setCheckMarkDrawable(null);

							return ret;
						}
					});









				}


				return true;
			}});
		setListAdapter();
		refreshSize();
	}

	private class MyDSController extends DragSortController {
		DragSortListView mDslv;
		public MyDSController(DragSortListView dslv) {
			super(dslv);
			setDragHandleId(R.id.drag_handle);
			mDslv = dslv;

		}

		@Override
		public View onCreateFloatView(int position) {
			isDirty=true;
			View v = adapter.getView(position, null, mDslv);
			//v.getBackground().setLevel(500);
			mDslv.setFloatAlpha(1.0f);
			v.setBackgroundColor(Color.parseColor("#ffff00"));//TODO: get primary color
			return v;
		}

		@Override
		public void onDestroyFloatView(View floatView) {
			//do nothing; block super from crashing
		}

	}

	private static class ViewHolder{
		private ImageView handle;
		private TextView title;

		public ViewHolder(View v) {
			handle = v.findViewById(R.id.handle);
			title = v.findViewById(R.id.text);
			v.setTag(this);
		}
	}

	public void refreshDicts(boolean bUnfinished) {
		HashSet<String> acceptor = new HashSet<>();
		for(int idxTmp=a.md.size()-1;idxTmp>=0;idxTmp--) {
			mdict mdTmp = a.md.get(idxTmp);
			if(rejector.contains(mdTmp.getPath()) || acceptor.contains(mdTmp.getPath())) {
				mdTmp.unload();
				a.md.remove(mdTmp);
				continue;
			}
			if(mdict_prempter.class==mdTmp.getClass()) {
        		/*try {
        			a.md.set(idxTmp, mdTmp=new mdict(mdTmp.getPath(),CMN.a));//实化
        			mdict_cache.put(mdTmp.getPath(), mdTmp);
				} catch (Exception e) {
					//e.printStackTrace();
					//a.md.remove(mdTmp);
					a.md.set(idxTmp, new mdict_nonexist(mdTmp.getPath(),a.opt));//打回
					a.showT("词典 "+new File(mdTmp.getPath()).getName()+"加载失败！ @"+mdTmp.getPath()+e.getLocalizedMessage());
	        		continue;
				}*/
				mdict getTmp = mdict_cache.get(mdTmp.getPath());
				if(getTmp!=null)
					a.md.set(idxTmp, getTmp);
			}
			acceptor.add(mdTmp.getPath());
		}
		rejector.clear();
		acceptor.clear();
		if(bUnfinished) {
			adapter.notifyDataSetChanged();
		}
	}


}

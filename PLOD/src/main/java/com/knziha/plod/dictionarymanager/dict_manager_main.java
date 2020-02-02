package com.knziha.plod.dictionarymanager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.PlainDict.AgentApplication;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.PlainDict.PlaceHolder;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_manageable;
import com.knziha.plod.dictionarymodels.mdict_prempter;
import com.knziha.plod.dictionarymodels.mdict_transient;
import com.knziha.plod.widgets.ArrayAdapterHardCheckMark;
import com.knziha.plod.widgets.CheckedTextViewmy;
import com.knziha.plod.widgets.DictionaryTitle;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class dict_manager_main extends dict_manager_base<mdict_transient>
		implements dict_manager_base.SelectableFragment, OnItemLongClickListener {
	HashSet<String> rejector = new HashSet<>();
	dict_manager_activity aaa;
	private boolean bDictTweakerOnceShowed;
	public ArrayList<mdict_transient> manager_group;
	AlertDialog d;

	public dict_manager_main(){
		super();
		checkChanged=(buttonView, isChecked) -> {
			ViewHolder vh = (ViewHolder) ((View)buttonView.getParent()).getTag();
			lastClickedPos[(++lastClickedPosIndex)%2]=vh.position;
			mdict_manageable mdTmp = adapter.getItem(vh.position);
			String key = mdTmp.getPath();
			if(isChecked)
				selector.add(key);
			else
				selector.remove(key);
		};
	}

	public void refreshSize(){
		a.mTabLayout.getTabAt(0).setText(getResources().getString(R.string.currentPlan,manager_group.size()-rejector.size()));
	}

	@Override
	public int getItemLayout() {
		return R.layout.dict_manager_dslitem;
	}

	@Override
	public void setListAdapter() {
		adapter = new MyAdapter(manager_group);
		setListAdapter(adapter);
	}

	@Override
	public DragSortController buildController(DragSortListView dslv) {
		return new MyDSController(dslv);
	}

	public void add(mdict_transient mmTmp) {
		manager_group.add(mmTmp);
		isDirty=true;
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

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (position >= mDslv.getHeaderViewsCount()) {//词典选项
			AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
			final int actualPosition = position - mDslv.getHeaderViewsCount();
			SpannableStringBuilder ssb = new SpannableStringBuilder(getResources().getString(R.string.dictOpt)).append("");
			int start = ssb.length();

			final mdict_transient mmTmp = manager_group.get(actualPosition);
			boolean isOnSelected = a.opt.getDictManager1MultiSelecting() && selector.contains(mmTmp.getPath());
			if (isOnSelected) ssb.append("…");
			ssb.append(mmTmp.getPath());
			if (isOnSelected) ssb.append("…");
			int end = ssb.length();

			ssb.setSpan(new RelativeSizeSpan(0.63f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(new ClickableSpan() {
				@Override
				public void onClick(@NonNull View widget) {//更多选项
					AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
					builder3.setTitle("更多选项");
					builder3.setSingleChoiceItems(new String[]{}, 0,
							(dialog, pos) -> {
								//mdict_manageable mmTmp = manager_group.get(actualPosition);
								switch (pos) {
									case 0: {//在外部管理器打开路径
										StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
										try {
											startActivity(new Intent(Intent.ACTION_VIEW)
													.setDataAndType(Uri.fromFile(mmTmp.f().getParentFile()), "resource/folder")
													.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
											);
										} catch (Exception e) {
											a.show(R.string.no_suitable_app);
										}
									}
									break;
									case 1: {//在内置管理器打开路径
										DialogProperties properties = new DialogProperties();
										properties.selection_mode = DialogConfigs.SINGLE_MULTI_MODE;
										properties.selection_type = DialogConfigs.FILE_SELECT;
										properties.root = new File("/");
										properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
										properties.offset = mmTmp.f().getParentFile();
										properties.opt_dir = new File(a.opt.pathToDatabases() + "favorite_dirs/");
										properties.dedicatedTarget = mmTmp.f().getName();
										properties.opt_dir.mkdirs();
										properties.extensions = new HashSet<>();
										properties.extensions.add(".mdx");
										properties.extensions.add(".mdd");
										properties.title_id = R.string.app_name;
										properties.isDark = GlobalOptions.isDark;
										FilePickerDialog fdialog = new FilePickerDialog(a, properties);
										fdialog.show();
									}
									break;
									case 2://词典设置
										if (isOnSelected) {
											mdict_manageable[] mdTmps = new mdict_manageable[selector.size()];
											int cc = 0;
											for (mdict_manageable mI : manager_group) {
												if (selector.contains(mI.getPath()))
													mdTmps[cc++] = mI;
											}
											mdict.showDictTweaker(null, getActivity(), mdTmps);
										} else {
											mdict.showDictTweaker(null, getActivity(), mmTmp);
										}
										bDictTweakerOnceShowed = true;
										break;
								}
							});

					String[] Menus = getResources().getStringArray(
							R.array.dicts_option1);
					List<String> arrMenu = Arrays.asList(Menus);
					AlertDialog dd = builder3.create();
					if(GlobalOptions.isDark) {
						dd.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_d);
					}
					dd.show();
					dd.setOnDismissListener(dialog -> {
						if (bDictTweakerOnceShowed) {
							adapter.notifyDataSetChanged();
							bDictTweakerOnceShowed = false;
						}
					});
					dd.getListView().setAdapter(new ArrayAdapterHardCheckMark<>(getActivity(),
							R.layout.singlechoice, android.R.id.text1, arrMenu));
				}
			}, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			builder2.setTitle(ssb);
			builder2.setSingleChoiceItems(new String[]{}, 0,
					(dialog, pos) -> {
						switch (pos) {
							case 0: {
								View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
								final ListView lv = dialog1.findViewById(R.id.lv);
								final EditText et = dialog1.findViewById(R.id.et);
								ImageView iv = dialog1.findViewById(R.id.confirm);
								et.setText(manager_group.get(actualPosition).getName());

								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
								builder.setView(dialog1);
								builder.setIcon(R.mipmap.ic_directory_parent);
								final AlertDialog dd = builder.create();

								iv.setOnClickListener(v -> {
									String oldEUFn = mmTmp.getPath();
									String oldFn = mFile.tryDeScion(new File(oldEUFn), a.opt.lastMdlibPath);
									File to = new File(mmTmp.f().getParent(), et.getText().toString() + ".mdx");
									//Log.e("XXX-filepath", to.getAbsolutePath());
									try {
										to = to.getCanonicalFile();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									//Log.e("XXX-filepath2", to.getAbsolutePath());
									String toFn = to.getAbsolutePath();
									if (toFn.startsWith(a.opt.lastMdlibPath))
										toFn = toFn.substring(a.opt.lastMdlibPath.length() + 1);

									boolean suc = false;
									if (to.equals(mmTmp.f())) {//就是自己
										suc = true;
									} else if (new File(mmTmp.getPath()).exists()) {//正常重命名
										if (to.exists()) {
											a.showT("文件已存在，重命名失败！");
										} else if (mmTmp.renameFileTo(to)) {//正常重命名成功
											suc = true;
										}
									} else {
										if (to.exists() && !a.mdict_cache.containsKey(to.getAbsolutePath())) {//关联已存在的文件
											mmTmp.renameFileTo(to);
											CMN.Log("重命名", mmTmp.getName());
											adapter.remove(mmTmp);
											try {
												adapter.insert(new mdict_prempter(to.getAbsolutePath(), a.opt), actualPosition);
											} catch (IOException e) {
												e.printStackTrace();
											}
											suc = true;
										}
									}
									if (suc) {
										if (rejector.contains(oldEUFn)) {
											rejector.remove(oldEUFn);
											rejector.add(to.getAbsolutePath());
										}
										adapter.notifyDataSetChanged();
										isDirty = true;
										d.dismiss();
										dd.dismiss();
										a.show(R.string.renD);
										File[] moduleFullScanner = new File(a.opt.pathToMainFolder().append("CONFIG").toString()).listFiles(pathname -> {
											String name = pathname.getName();
											if (name.endsWith(".set")) {
												return true;
											}
											return false;
										});
										ArrayList<File> moduleFullScannerArr = new ArrayList<File>(Arrays.asList(moduleFullScanner));
										moduleFullScannerArr.add(new File(a.opt.pathToMainFolder().append("CONFIG/mdlibs.txt").toString()));
										for (File fI : moduleFullScannerArr) {
											InputStreamReader reader = null;
											StringBuffer sb = new StringBuffer("");
											String line = "";

											try {
												reader = new InputStreamReader(new FileInputStream(fI));
												BufferedReader br = new BufferedReader(reader);
												while ((line = br.readLine()) != null) {
													try {
														if (line.equals(oldFn) || new File(line).getCanonicalPath().equals(oldFn)) {
															//System.out.println(line);
															line = toFn;
															//System.out.println(line);
														}
													} catch (Exception e) {
													}
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
										dict_Manager_folderlike f3 = ((dict_manager_activity) getActivity()).f3;
										if (f3.dataPrepared) {
											//int idx = f3.data.indexOf(new mFile(oldEUFn));
											int idx = f3.data.remove(new mFile(oldEUFn));
											if (idx != -1) {
												f3.data.insert(new mFile(to).init());
											}
										}
									} else {
										a.showT("重命名失败!");
									}
								});
								dd.show();
							}
							break;
							case 1: {//多选
								a.opt.setDictManager1MultiSelecting(!a.opt.getDictManager1MultiSelecting());
								adapter.notifyDataSetChanged();
								a.f2.adapter.notifyDataSetChanged();
								d.dismiss();
							}
							break;
							case 2: {//移至顶部
								isDirty = true;
								manager_group.remove(actualPosition);
								manager_group.add(0, mmTmp);
								d.dismiss();
								adapter.notifyDataSetChanged();
								getListView().setSelection(0);
							}
							break;
							case 3: {//移至底部
								isDirty = true;
								manager_group.remove(actualPosition);
								manager_group.add(manager_group.size(), mmTmp);
								d.dismiss();
								adapter.notifyDataSetChanged();
								getListView().setSelection(manager_group.size() - 1);
							}
							break;
							/* 设为滤器等等… */
							case 4: {
								AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
								builder3.setTitle("更多选项");
								builder3.setSingleChoiceItems(
										mmTmp.isMddResource() ? R.array.dicts_option2 : R.array.dicts_option3, -1,
										(dialog3, pos3) -> {
											switch (pos3) {
												/* 设为滤器 */
												case 0: {
													isDirty = true;
													boolean isF = PDICMainAppOptions.toggleTmpIsFiler(mmTmp);
													if (isOnSelected) {
														for (int i = 0; i < manager_group.size(); i++) {
															if (selector.contains(manager_group.get(i).getPath()) && !manager_group.get(i).isMddResource())
																PDICMainAppOptions.setTmpIsFiler(manager_group.get(i), isF);
														}
													}
													adapter.notifyDataSetChanged();
													a.showT(isF ? "已设为构词库" : "已取消构词库");
												}
												break;
												/* 设为点译词库 */
												case 1: {
													isDirty = true;
													boolean isCS = PDICMainAppOptions.toggleTmpIsClicker(mmTmp);
													if (isOnSelected) {
														for (int i = 0; i < manager_group.size(); i++) {
															if (selector.contains(manager_group.get(i).getPath()))
																PDICMainAppOptions.setTmpIsClicker(manager_group.get(i), isCS);
														}
													}
													adapter.notifyDataSetChanged();
													a.showT(isCS ? "已设为译词库" : "已取消点译词库");
												}
												break;
												/* 设为默认折叠 */
												case 2: {
													isDirty = true;
													boolean isCL = PDICMainAppOptions.toggleTmpIsCollapsed(mmTmp);
													if (isOnSelected) {
														for (int i = 0; i < manager_group.size(); i++) {
															if (selector.contains(manager_group.get(i).getPath()))
																PDICMainAppOptions.setTmpIsCollapsed(manager_group.get(i), isCL);
														}
													}
													adapter.notifyDataSetChanged();
													a.showT(isCL ? "已设为默认折叠" : "已取消默认折叠");
												}
												break;
												/* 设为发音库( mdd 专有 ) */
												case 3: {
													if (mmTmp.isMddResource()) {
														isDirty = true;
														boolean isCS = PDICMainAppOptions.toggleTmpIsAudior(mmTmp);
														if (isCS)
															PDICMainAppOptions.setTmpIsFiler(mmTmp, false);
														if (isOnSelected) {
															for (int i = 0; i < manager_group.size(); i++) {
																if (selector.contains(manager_group.get(i).getPath())) {
																	if (manager_group.get(i).isMddResource()) {
																		PDICMainAppOptions.setTmpIsAudior(manager_group.get(i), isCS);
																		if (isCS)
																			PDICMainAppOptions.setTmpIsFiler(manager_group.get(i), false);
																	}
																}
															}
														}
														adapter.notifyDataSetChanged();
														a.showT(isCS ? "已设为发音库" : "已取消发音库");
													}
												}
												break;
											}
											dialog3.dismiss();
										});
								AlertDialog dd = builder3.create();
								if(GlobalOptions.isDark) {
									dd.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_d);
								}
								dd.show();
							}
							break;
						}
					});

			String[] Menus = getResources().getStringArray(
					R.array.dicts_option);
			List<String> arrMenu = Arrays.asList(Menus);
			d = builder2.create();
			if(GlobalOptions.isDark) {
				d.getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_d);
			}
			d.show();

			TextView titleView = d.getWindow().getDecorView().findViewById(R.id.alertTitle);
			titleView.setSingleLine(false);
			titleView.setMovementMethod(LinkMovementMethod.getInstance());
			if (!a.opt.isLarge) titleView.setMaxLines(5);

			d.getListView().setAdapter(new ArrayAdapterHardCheckMark<>(getActivity(),
					R.layout.singlechoice, android.R.id.text1, arrMenu));
		}
		return true;
	}

	public void performLastItemLongClick() {
		if(adapter.getCount()>0){
			int idx = lastClickedPos[lastClickedPosIndex%2];
			if(idx<0||idx>=adapter.getCount())
				idx = 0;
			onItemLongClick(null, null, idx, 0);
		}
	}

	private class MyAdapter extends ArrayAdapter<mdict_transient> {
		public MyAdapter(List<mdict_transient> mdicts) {
			super(getActivity(), getItemLayout(), R.id.text, mdicts);
		}

		@NonNull
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			ViewHolder vh;
			if(convertView==null){
				convertView = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(), parent, false);
				convertView.setTag(vh = new ViewHolder(convertView));
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			vh.position=position;
			//v.getBackground().setLevel(1000);
			//position = position - mDslv.getHeaderViewsCount();
			mdict_manageable mdTmp = adapter.getItem(position);
			vh.title.isFold = PDICMainAppOptions.getTmpIsCollapsed(mdTmp.getTmpIsFlag());
			Drawable cover = mdTmp.getCover();
			String tail="\t—<>—";
			if(cover!=null) {
				SpannableStringBuilder ssb = new SpannableStringBuilder("| ").append(aaa.isDebug?mdTmp.getPath():mdTmp.getName());
				cover.setBounds(0, 0, 50, 50);
				ssb.setSpan(new ImageSpan(cover), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				vh.title.setText(ssb);
			}else{
				String val = aaa.isDebug?mdTmp.getPath():mdTmp.getName();
				vh.title.setText(val);
			}

			if(aaa.isSearching && mdTmp.getName().toLowerCase().contains(aaa.dictQueryWord))
				vh.title.setBackgroundResource(R.drawable.xuxian2);
			else
				vh.title.setBackground(null);

			String key = mdTmp.getPath();

			if(a.opt.getDictManager1MultiSelecting()){
				vh.ck.setVisibility(View.VISIBLE);
				vh.ck.setOnCheckedChangeListener(null);
				vh.ck.setChecked(selector.contains(key));
				vh.ck.setOnCheckedChangeListener(checkChanged);
			}else
				vh.ck.setVisibility(View.GONE);

			StringBuilder rgb = new StringBuilder("#");
			if(rejector.contains(key))
				rgb.append("aaaaaa");//一样的亮兰色aafafa
			else
				rgb.append(GlobalOptions.isDark?"EEEEEE":"000000");
			if(!mdTmp.exists())
				rgb.insert(1, "ff");
			rgb.setLength(7);
			vh.title.setTextColor(Color.parseColor(rgb.toString()));

			Drawable[] ds = vh.title.getCompoundDrawables();
			Drawable leftDrawableSlot=ds[0];
			Drawable rightSlot=ds[1];
			boolean needSet=false;
			if(PDICMainAppOptions.getTmpIsFiler(mdTmp.getTmpIsFlag())){
				if(convertView.getTag(R.drawable.filter)!=(Integer)R.drawable.filter) {
					needSet = true;
					leftDrawableSlot = getResources().getDrawable(R.drawable.filter).mutate();
					int h = vh.title.getLineHeight();
					leftDrawableSlot.setBounds(new Rect(0, 0, h, h));
					if (mdTmp.getIsDedicatedFilter()) {
						leftDrawableSlot.setColorFilter(0xFFFFEB3B, PorterDuff.Mode.SRC_IN);
					} else
						leftDrawableSlot.clearColorFilter();
					convertView.setTag(R.drawable.filter, R.drawable.filter);
				}
			}
			else if(PDICMainAppOptions.getTmpIsAudior(mdTmp.getTmpIsFlag())){
				if(convertView.getTag(R.drawable.filter)!=(Integer)R.drawable.voice_ic_big) {
					needSet = true;
					leftDrawableSlot = getResources().getDrawable(R.drawable.voice_ic_big).mutate();
					int h = vh.title.getLineHeight();
					leftDrawableSlot.setBounds(new Rect(0, 0, h, h));
					convertView.setTag(R.drawable.filter, R.drawable.voice_ic_big);
				}
			}
			else if(convertView.getTag(R.drawable.filter)!=null){
				needSet=true;
				leftDrawableSlot=null;
				convertView.setTag(R.drawable.filter, null);
			}

			if(PDICMainAppOptions.getTmpIsClicker(mdTmp.getTmpIsFlag())){
				//if(v.getTag(R.drawable.ic_click_search)==null) {//发现量子波动bug，故释放之。
					needSet=true;
					rightSlot = getResources().getDrawable(R.drawable.ic_click_search).mutate();
					int h = vh.title.getLineHeight();
					rightSlot.setBounds(new Rect(0, 0, h, h));
					convertView.setTag(R.drawable.ic_click_search, false);
				//}
			}
			else if(convertView.getTag(R.drawable.ic_click_search)!=null){
				needSet=true;
				rightSlot=null;
				convertView.setTag(R.drawable.ic_click_search, null);
			}

			

			if(needSet)
				vh.title.setCompoundDrawables(leftDrawableSlot, null, rightSlot, null);

			if(GlobalOptions.isDark) {
				convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}
			return convertView;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(dict_manager_activity) getActivity();
		AgentApplication app = ((AgentApplication) a.getApplication());
		if(app.mdict_cache!=null) {
			ArrayList<PlaceHolder> slots = a.slots;
			manager_group = a.mdmng = new ArrayList<>(a.mdict_cache.size());

			//TODO 省去这一步的IO？
			//策略：缓存 placeholder，建立transient管理中间体时直接引用 placeholder。
			a.mdict_cache.clear();
			manager_group.ensureCapacity(slots.size());
			for (PlaceHolder phI : slots) {
				mdict_transient mmTmp = new mdict_transient(phI, a.opt);
				if (!mmTmp.isMddResource()) PDICMainAppOptions.setTmpIsAudior(mmTmp, false);
				if(PDICMainAppOptions.getTmpIsHidden(mmTmp.getTmpIsFlag()))
					rejector.add(mmTmp.getPath());
				manager_group.add(mmTmp);
				a.mdict_cache.put(phI.getPath(a.opt), mmTmp);
			}

			aaa = (dict_manager_activity) getActivity();
			mDslv.setOnItemClickListener((parent, view, position, id) -> {
				//CMN.show(""+(adapter==null)+" "+(((dict_manager_activity)getActivity()).f1.adapter==null));
				isDirty = true;
				//adapter.getItem(position).value = !adapter.getItem(position).value;//TODO optimize
				if (position >= mDslv.getHeaderViewsCount()) {
					position = position - mDslv.getHeaderViewsCount();
					String key = adapter.getItem(position).getPath();
					if (!rejector.remove(key))
						rejector.add(key);
					adapter.notifyDataSetChanged();
					isDirty = true;
					refreshSize();
				}
			});

			mDslv.setOnItemLongClickListener(this);
			setListAdapter();
			refreshSize();
			AgentApplication agent = ((AgentApplication) a.getApplication());
			agent.clearNonsenses();
		}
	}

	@Override
	DragSortListView.DropListener getDropListener() {
		DragSortListView.DropListener onDrop =
				(from, to) -> {
					//CMN.Log("to", to);
					//if(true) return;
					if(a.opt.getDictManager1MultiSelecting() && selector.contains(manager_group.get(from).getPath())){
						ArrayList<mdict_transient> md_selected = new ArrayList<>(selector.size());
						if(to>from) to++;
						for (int i = manager_group.size()-1; i >= 0; i--) {
							mdict_manageable mmTmp = manager_group.get(i);
							if(selector.contains(mmTmp.getPath())){
								md_selected.add(0, manager_group.remove(i));
								if(i<to) to--;
							}
						}
						manager_group.addAll(to, md_selected);
						adapter.notifyDataSetChanged();
					}
					else if (from != to) {
						mdict_transient mdTmp = manager_group.remove(from);
						manager_group.add(to, mdTmp);
						adapter.notifyDataSetChanged();
					}
				};
		return onDrop;
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
			View v=adapter.getView(position, null, mDslv);
			((ViewHolder)v.getTag()).ck.jumpDrawablesToCurrentState();
			//v.getBackground().setLevel(500);
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

	protected static class ViewHolder{
		public int position;
		ImageView handle;
		DictionaryTitle title;
		CheckBox ck;

		public ViewHolder(View v) {
			handle = v.findViewById(R.id.handle);
			title = v.findViewById(R.id.text);
			ck = v.findViewById(R.id.check1);
			v.setTag(this);
		}
	}

	public void refreshDicts(boolean bUnfinished) {
		int idxTmp=manager_group.size();
		HashSet<String> acceptor = new HashSet<>(idxTmp);
		for(--idxTmp;idxTmp>=0;idxTmp--) {
			mdict_manageable mmTmp = manager_group.get(idxTmp);
			boolean hidden = PDICMainAppOptions.setTmpIsHidden(mmTmp, rejector.contains(mmTmp.getPath()));
			if(hidden && ((bUnfinished || !PDICMainAppOptions.getAllowHiddenRecords())) || acceptor.contains(mmTmp.getPath())) {
				manager_group.remove(mmTmp);
				mmTmp.unload();
				continue;
			}
			acceptor.add(mmTmp.getPath());
		}
		rejector.clear();
		acceptor.clear();
		if(bUnfinished) {
			adapter.notifyDataSetChanged();
		}
	}


}

package com.knziha.plod.dictionarymanager;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedWriter;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.MagentTransient;
import com.knziha.plod.dictionarymodels.mngr_agent_manageable;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CharSequenceKey;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.ArrayAdapterHardCheckMark;
import com.knziha.plod.widgets.FlowTextView;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class BookManagerMain extends BookManagerFragment<BookPresenter>
		implements BookManagerFragment.SelectableFragment, OnItemLongClickListener {
	//HashSet<String> rejector = new HashSet<>();
	BookManager aaa;
	private boolean bDictTweakerOnceShowed;
	public MainActivityUIBase.LoadManager loadMan;
	AlertDialog d;
	private Drawable mActiveDrawable;
	private Drawable mFilterDrawable;
	private Drawable mAudioDrawable;
	private Drawable mRightDrawable;
	private int selected_size;
	
	public BookManagerMain(){
		super();
		checkChanged=(buttonView, isChecked) -> {
			ViewHolder vh = (ViewHolder) ((View)buttonView.getParent()).getTag();
			if(lastClickedPos[lastClickedPosIndex%2]!=vh.position) {
				lastClickedPos[(++lastClickedPosIndex) % 2] = vh.position;
			}
			setPlaceSelected(vh.position, !getPlaceSelected(vh.position));
		};
	}

	public void refreshSize(){
		a.mTabLayout.getTabAt(0).setText(getResources().getString(R.string.currentPlan,loadMan.lazyMan.chairCount));
	}

	@Override
	public int getItemLayout() {
		return R.layout.dict_manager_dslitem;
	}

	@Override
	public void setListAdapter() {
		adapter = new MyAdapter(loadMan.md);
		setListAdapter(adapter);
	}

	@Override
	public DragSortController buildController(DragSortListView dslv) {
		return new MyDSController(dslv);
	}

	public void add(String mmTmp) {
		loadMan.md.add(null);
		loadMan.lazyMan.placeHolders.add(new PlaceHolder(mmTmp));
		loadMan.lazyMan.chairCount++;
		dataSetChanged();
		refreshSize();
		isDirty=true;
	}

	@Override
	public boolean exitSelectionMode() {
		if(selected_size>0){
			selected_size = 0;
			for (int i = 0; i < loadMan.md.size(); i++) {
				setPlaceSelected(i, false);
			}
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

			final BookPresenter magent = getMagentAt(actualPosition);
			boolean isOnSelected = a.opt.getDictManager1MultiSelecting() && getPlaceSelected(actualPosition);
			if (isOnSelected) ssb.append("…");
			ssb.append(magent.getPath());
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
								//mdict_manageable magent = manager_group.get(actualPosition);
								switch (pos) {
									case 0: {//在外部管理器打开路径
										StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
										try {
											startActivity(new Intent(Intent.ACTION_VIEW)
													.setDataAndType(Uri.fromFile(magent.f().getParentFile()), "resource/folder")
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
										properties.offset = magent.f().getParentFile();
										properties.opt_dir = new File(a.opt.pathToDatabases() + "favorite_dirs/");
										properties.dedicatedTarget = magent.f().getName();
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
											ArrayList<BookPresenter> mdTmps = new ArrayList<>(selected_size);
											int cc = 0;
											for (BookPresenter mI : manager_group()) {
												if (getPlaceSelected(cc++))
													mdTmps.add(mI);
											}
											BookPresenter[] data = mdTmps.toArray(new BookPresenter[0]);
											BookPresenter.showDictTweaker(null, (Toastable_Activity) getActivity(), data);
										} else {
											BookPresenter.showDictTweaker(null, (Toastable_Activity) getActivity(), magent);
										}
										bDictTweakerOnceShowed = true;
										break;
									case 3://词典设置
										if (isOnSelected) {
											ArrayList<BookPresenter> mdTmps = new ArrayList<>(selected_size);
											int cc = 0;
											for (BookPresenter mI : manager_group()) {
												if (getPlaceSelected(cc++))
													mdTmps.add(mI);
											}
											BookPresenter[] data = mdTmps.toArray(new BookPresenter[0]);
											getBookManager().showBookPreferences(data);
										} else {
											getBookManager().showBookPreferences(magent);
										}
										break;
								}
							});

					String[] Menus = getResources().getStringArray(
							R.array.dicts_option1);
					List<String> arrMenu = Arrays.asList(Menus);
					AlertDialog dd = builder3.show();
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
								if (true) {
									a.showT("功能关闭，请等待5.0版本");
									break;
								}
								View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
								final ListView lv = dialog1.findViewById(R.id.lv);
								final EditText et = dialog1.findViewById(R.id.et);
								ImageView iv = dialog1.findViewById(R.id.confirm);
								et.setText(getMagentAt(actualPosition).getDictionaryName());

								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
								builder.setView(dialog1);
								builder.setIcon(R.mipmap.ic_directory_parent);
								final AlertDialog dd = builder.create();

								iv.setOnClickListener(v -> {
									boolean suc = false;
									String newName = et.getText().toString();
									String newPath = newName;
									if(!newPath.contains("/")){
										if(newName.endsWith(".mdx"))
											newName = newName.substring(0, newName.length()-4);
										else if(magent.isMdict())
											newPath+=".mdx";
										if(newName.length()>0){
											String oldPath = magent.getPath();
											File oldf = magent.f();
											String oldFn = oldf.getName();
											String OldFName = magent.getDictionaryName();
											int oldFnLen = oldFn.length();

											File to = new File(magent.f().getParent(), newPath);
											String toFn = a.opt.tryGetDomesticFileName(to.getPath());
											if (to.equals(magent.f())) {//就是自己
												suc = true;
											} else if (new File(magent.getPath()).exists()) {//正常重命名
												if (to.exists()) {
													a.showT("文件已存在，重命名失败！");
												} else if (magent.renameFileTo(getActivity(), to)) {//正常重命名成功
													suc = true;
												}
											} else {
												if (to.exists() && !a.mdict_cache.containsKey(to.getAbsolutePath())) {//关联已存在的文件
													magent.renameFileTo(getActivity(), to);
													CMN.Log("重命名", magent.getDictionaryName());
													MagentTransient mdTmp = a.new_MagentTransient(to.getAbsolutePath(), a.opt, null, true);
													loadMan.md.set(actualPosition, mdTmp);
													loadMan.lazyMan.placeHolders.set(actualPosition, mdTmp.getPlaceHolder());
													suc = true;
												}
											}
											if (suc) {
												a.RebasePath(oldf, OldFName, to, newName, oldFn);
												adapter.notifyDataSetChanged();
												isDirty = true;
												d.dismiss();
												dd.dismiss();
												a.show(R.string.renD);
												ArrayList<File> moduleFullScannerArr = a.ScanInModlueFiles(true, true);
												AgentApplication app = ((AgentApplication) getActivity().getApplication());
												char[] cb = app.get4kCharBuff();
												for (File fI : moduleFullScannerArr) {
													StringBuilder sb = new StringBuilder();
													String line;

													try {
														ReusableBufferedReader br = new ReusableBufferedReader(new FileReader(fI), cb, 4096);
														boolean bNeedReWrite = false;
														while ((line = br.readLine()) != null) {
															/* 含名俱重 */
															try {
																if (line.endsWith(oldFn)) {
																	int ll = line.length();
																	if (ll == oldFnLen || line.charAt(ll - oldFnLen) == '/') {
																		line = ll == oldFnLen ? toFn : (line.substring(0, ll - oldFnLen) + toFn);
																		bNeedReWrite = true;
																	}
																}
															} catch (Exception ignored) {
															}
															sb.append(line).append("\n");
														}
														br.close();
														cb = br.cb;
														if (bNeedReWrite) {
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
												BookManagerFolderlike f3 = getBookManager().f3;
												if (f3.dataPrepared) {
													int idx = f3.data.remove(new mFile(oldPath));
													if (idx != -1) {
														f3.data.insert(new mFile(to).init(a.opt));
													}
												}
											}
										}
									}
									if(!suc) {
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
								replace(actualPosition, 0);
								d.dismiss();
								adapter.notifyDataSetChanged();
								getListView().setSelection(0);
							}
							break;
							case 3: {//移至底部
								isDirty = true;
								int last = manager_group().size() - 1;
								replace(actualPosition, last);
								d.dismiss();
								adapter.notifyDataSetChanged();
								getListView().setSelection(last);
							}
							break;
							/* 设为滤器等等… */
							case 4: {
								AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
								builder3.setTitle("更多选项");
								builder3.setSingleChoiceItems(
										magent.isMddResource() ? R.array.dicts_option2 : R.array.dicts_option3, -1,
										(dialog3, pos3) -> {
											switch (pos3) {
												/* 设为滤器 */
												case 0: {
													isDirty = true;
													boolean isF = PDICMainAppOptions.toggleTmpIsFiler(magent);
													if (isOnSelected) {
														for (int i = 0; i < manager_group().size(); i++) {
															if (getPlaceSelected(i) && !manager_group().get(i).isMddResource())
																PDICMainAppOptions.setTmpIsFiler(manager_group().get(i), isF);
														}
													}
													adapter.notifyDataSetChanged();
													a.showT(isF ? "已设为构词库" : "已取消构词库");
												}
												break;
												/* 设为点译词库 */
												case 1: {
													isDirty = true;
													boolean isCS = PDICMainAppOptions.toggleTmpIsClicker(magent);
													if (isOnSelected) {
														for (int i = 0; i < manager_group().size(); i++) {
															if (getPlaceSelected(i))
																PDICMainAppOptions.setTmpIsClicker(getMagentAt(i), isCS);
														}
													}
													adapter.notifyDataSetChanged();
													a.showT(isCS ? "已设为译词库" : "已取消点译词库");
												}
												break;
												/* 设为默认折叠 */
												case 2: {
													isDirty = true;
													boolean isCL = PDICMainAppOptions.toggleTmpIsCollapsed(magent);
													if (isOnSelected) {
														for (int i = 0; i < manager_group().size(); i++) {
															if (getPlaceSelected(i))
																PDICMainAppOptions.setTmpIsCollapsed(getMagentAt(i), isCL);
														}
													}
													adapter.notifyDataSetChanged();
													a.showT(isCL ? "已设为默认折叠" : "已取消默认折叠");
												}
												break;
												/* 设为发音库( mdd 专有 ) */
												case 3: {
													if (magent.isMddResource()) {
														isDirty = true;
														boolean isCS = PDICMainAppOptions.toggleTmpIsAudior(magent);
														if (isCS)
															PDICMainAppOptions.setTmpIsFiler(magent, false);
														if (isOnSelected) {
															for (int i = 0; i < manager_group().size(); i++) {
																if (getPlaceSelected(i)) {
																	BookPresenter mdTmp = getMagentAt(i);
																	if (mdTmp.isMddResource()) {
																		PDICMainAppOptions.setTmpIsAudior(mdTmp, isCS);
																		if (isCS)
																			PDICMainAppOptions.setTmpIsFiler(mdTmp, false);
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
								AlertDialog dd = builder3.show();
							}
							break;
						}
					});

			String[] Menus = getResources().getStringArray(
					R.array.dicts_option);
			List<String> arrMenu = Arrays.asList(Menus);
			d = builder2.show();

			TextView titleView = d.getWindow().getDecorView().findViewById(R.id.alertTitle);
			titleView.setSingleLine(false);
			titleView.setMovementMethod(LinkMovementMethod.getInstance());
			if (!GlobalOptions.isLarge) titleView.setMaxLines(5);

			d.getListView().setAdapter(new ArrayAdapterHardCheckMark<>(getActivity(),
					R.layout.singlechoice, android.R.id.text1, arrMenu));
		}
		return true;
	}

	public void performLastItemLongClick() {
		if(adapter.getCount()>0){
			int idx = lastClickedPos[(lastClickedPosIndex+1)%2];
			if(idx<0||idx>=adapter.getCount()){
				idx = getListView().getHeaderViewsCount();
			}
			onItemLongClick(null, null, idx, 0);
		}
	}
	
	private class MyAdapter extends ArrayAdapter<BookPresenter> {
		public MyAdapter(List<BookPresenter> mdicts) {
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
			//mngr_agent_manageable mdTmp = adapter.getItem(position);
			

//			if(BookManager.dictQueryWord!=null && mdTmp.getDictionaryName().toLowerCase().contains(aaa.dictQueryWord))
//				vh.title.setBackgroundResource(R.drawable.xuxian2);
//			else
//				vh.title.setBackground(null);

			String key = getPathAt(position);

			if(a.opt.getDictManager1MultiSelecting()){
				vh.ck.setVisibility(View.VISIBLE);
				vh.ck.setOnCheckedChangeListener(null);
				vh.ck.setChecked(getPlaceSelected(position));
				vh.ck.setOnCheckedChangeListener(checkChanged);
			} else {
				vh.ck.setVisibility(View.GONE);
			}

			StringBuilder rgb = new StringBuilder("#");
			if(getPlaceRejected(position))
				rgb.append("aaaaaa");//一样的亮兰色aafafa
			else
				rgb.append(GlobalOptions.isDark?"EEEEEE":"000000");
			if(!new File(key).exists())
				rgb.insert(1, "ff");
			rgb.setLength(7);
			vh.title.setTextColor(Color.parseColor(rgb.toString()));
			
			int tmpFlag = getPlaceFlagAt(position);
			
			Drawable mLeftDrawable=null;
			if(PDICMainAppOptions.getTmpIsFiler(tmpFlag)){
				mLeftDrawable=mFilterDrawable;
			} else if(PDICMainAppOptions.getTmpIsAudior(tmpFlag)){
				mLeftDrawable=mAudioDrawable;
			}
			
			BookPresenter thereYouAre = a.app_mdict_cache.get(new File(key).getName());
			
			vh.title.setCover(thereYouAre==null?null:thereYouAre.getCover());
			
			vh.title.setCompoundDrawables(mActiveDrawable,
					mLeftDrawable,
					PDICMainAppOptions.getTmpIsClicker(tmpFlag)?mRightDrawable:null,
					PDICMainAppOptions.getTmpIsCollapsed(tmpFlag)?"<>":null);
			
			vh.title.setText(CMN.getAssetName(key));
			
			vh.title.setStarLevel(0);
			
			if(GlobalOptions.isDark) {
				convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}
			return convertView;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(BookManager) getActivity();
		if(a!=null) {
			Resources resource = a.getResources();
			mActiveDrawable = resource.getDrawable(R.drawable.star_ic_solid);
			mFilterDrawable = resource.getDrawable(R.drawable.filter);
			mAudioDrawable = resource.getDrawable(R.drawable.voice_ic_big);
			mRightDrawable = resource.getDrawable(R.drawable.ic_click_search);
			loadMan = a.loadMan;
			//TODO 省去这一步的IO？
			//策略：缓存 placeholder，建立transient管理中间体时直接引用 placeholder。
			a.mdict_cache.clear();
//			for (PlaceHolder phI : slots) {
//				MagentTransient magent = a.new_MagentTransient(phI, a.opt, null, false);
//				if (!magent.isMddResource()) PDICMainAppOptions.setTmpIsAudior(magent, false);
//				if(PDICMainAppOptions.getTmpIsHidden(magent.getTmpIsFlag()))
//					rejector.add(magent.getPath());
//				manager_group.add(magent);
//				a.mdict_cache.put(phI.getPath(a.opt).getPath(), magent);
//			}

			aaa = (BookManager) getActivity();
			mDslv.setOnItemClickListener((parent, view, position, id) -> {
				//CMN.show(""+(adapter==null)+" "+(((dict_manager_activity)getActivity()).f1.adapter==null));
				isDirty = true;
				//adapter.getItem(position).value = !adapter.getItem(position).value;//TODO optimize
				if (position >= mDslv.getHeaderViewsCount()) {
					position = position - mDslv.getHeaderViewsCount();
					setPlaceFlagAt(position
							, PDICMainAppOptions.setTmpIsHidden(getPlaceFlagAt(position), !getPlaceRejected(position)));
					adapter.notifyDataSetChanged();
					isDirty = true;
					refreshSize();
				}
			});

			mDslv.setOnItemLongClickListener(this);
			setListAdapter();
			refreshSize();
		}
	}
	
	@Override
	DragSortListView.DropListener getDropListener() {
		return (from, to) -> {
			//CMN.Log("to", to);
			//if(true) return;
			if(a.opt.getDictManager1MultiSelecting() && getPlaceSelected(from)){
				ArrayList<BookPresenter> md_selected = new ArrayList<>(selected_size);
				ArrayList<PlaceHolder> ph_selected = new ArrayList<>(selected_size);
				if(to>from) to++;
				for (int i = loadMan.md.size()-1; i >= 0; i--) {
					if(getPlaceSelected(i)){
						md_selected.add(0, loadMan.md.remove(i));
						ph_selected.add(0, loadMan.lazyMan.placeHolders.remove(i));
						if(i<to) to--;
					}
				}
				loadMan.md.addAll(to, md_selected);
				loadMan.lazyMan.placeHolders.addAll(to, ph_selected);
				adapter.notifyDataSetChanged();
			}
			else if (from != to) {
				replace(from, to);
				adapter.notifyDataSetChanged();
			}
		};
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
		FlowTextView title;
		CheckBox ck;

		public ViewHolder(View v) {
			handle = v.findViewById(R.id.drag_handle);
			title = v.findViewById(R.id.text);
			ck = v.findViewById(R.id.check1);
			v.setTag(this);
		}
	}

	public void refreshDicts(boolean bUnfinished) {
		if(bUnfinished) {
			adapter.notifyDataSetChanged();
		}
	}

	boolean getPlaceSelected(int position) {
		return (loadMan.lazyMan.placeHolders.get(position).lineNumber & 0x80000000)!=0;
	}
	
	void setPlaceSelected(int position, boolean val) {
		PlaceHolder ph = loadMan.lazyMan.placeHolders.get(position);
		ph.lineNumber &= ~0x80000000;
		if(val) ph.lineNumber |= 0x80000000;
	}
	
	boolean getPlaceRejected(int position) {
		return PDICMainAppOptions.getTmpIsHidden(getPlaceFlagAt(position));
	}
	
	void setPlaceRejected(int position, boolean val) {
		setPlaceFlagAt(position, PDICMainAppOptions.setTmpIsHidden(getPlaceFlagAt(position),val));
		loadMan.lazyMan.chairCount += val?-1:1;
	}
	
	final ArrayList<BookPresenter> manager_group() {
		return loadMan.md;
	}
	
	public void replace(int from, int to) {
		if (to < 0) {
			BookPresenter rmd = loadMan.md.remove(from);
			if(rmd!=null) a.mdict_cache.put(rmd.getPath(), rmd);
			loadMan.lazyMan.placeHolders.remove(from);
			loadMan.lazyMan.chairCount--;
		} else {
			loadMan.md.add(to, loadMan.md.remove(from));
			loadMan.lazyMan.placeHolders.add(to, loadMan.lazyMan.placeHolders.remove(from));
		}
	}
	
	private BookPresenter getMagentAt(int position) {
		return getBookManager().getMagentAt(position);
	}
	
	public String getPathAt(int position) {
		BookPresenter mdTmp = loadMan.md.get(position);
		if (mdTmp!=null) {
			return mdTmp.getPath();
		}
		return loadMan.lazyMan.placeHolders.get(position).getPath(getBookManager().opt).toString();
	}
	
	public int getPlaceFlagAt(int position) {
		BookPresenter mdTmp = loadMan.md.get(position);
		if (mdTmp!=null) {
			return mdTmp.tmpIsFlag;
		}
		mdTmp = getBookManager().getMagentAt(position, false);
		if (mdTmp!=loadMan.EmptyBook) {
			return mdTmp.tmpIsFlag;
		}
		return loadMan.lazyMan.placeHolders.get(position).tmpIsFlag;
	}
	
	private void setPlaceFlagAt(int position, int flag) {
		BookPresenter mdTmp = loadMan.md.get(position);
		if (mdTmp!=null) {
			mdTmp.tmpIsFlag = flag;
		}
		mdTmp = getBookManager().getMagentAt(position, false);
		if (mdTmp!=loadMan.EmptyBook) {
			mdTmp.tmpIsFlag = flag;
		}
		loadMan.lazyMan.placeHolders.get(position).tmpIsFlag = flag;
	}
	
	public CharSequence getNameAt(int position) {
		String path = getPathAt(position);
		int idx = path.lastIndexOf('/');
		if(idx>0)
			return new CharSequenceKey(path, idx+1);
		return path;
	}
}

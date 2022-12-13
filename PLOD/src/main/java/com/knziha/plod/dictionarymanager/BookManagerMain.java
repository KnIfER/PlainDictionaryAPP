package com.knziha.plod.dictionarymanager;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.Gravity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedWriter;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.MagentTransient;
import com.knziha.plod.plaindict.AgentApplication;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.CharSequenceKey;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.PlaceHolder;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.ViewUtils;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BookManagerMain extends BookManagerFragment<BookPresenter>
		implements BookManagerFragment.SelectableFragment, OnItemLongClickListener, DragSortListView.DropListener {
	public static int lastViewPos;
	public static int lastViewTop;
	HashSet<PlaceHolder> Selection = new HashSet<>();
	BookManager aaa;
	private boolean bDictTweakerOnceShowed;
	public MainActivityUIBase.LoadManager loadMan;
	AlertDialog d;
	private Drawable mActiveDrawable;
	private Drawable mFilterDrawable;
	private Drawable mAudioDrawable;
	private Drawable mWebDrawable;
	private Drawable mPDFDrawable;
	private Drawable mRightDrawable;
	private boolean tweakedDict;
	
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
		markDirty(-1);
		loadMan.md.add(null);
		loadMan.lazyMan.placeHolders.add(new PlaceHolder(mmTmp));
		loadMan.lazyMan.chairCount++;
		dataSetChanged();
		refreshSize();
	}

	@Override
	public boolean exitSelectionMode() {
		if((this!=getBookManager().f1 || PDICMainAppOptions.dictManager1MultiSelecting()) && selected_size()>0){
			for (PlaceHolder ph : Selection) {
				setPlaceSelectedInter(ph, false);
			}
			Selection.clear();
			adapter.notifyDataSetChanged();
//			for (int i = 0; i < loadMan.md.size(); i++) {
//				setPlaceSelected(i, false);
//			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (position >= listView.getHeaderViewsCount()) {//词典选项
			pressedPos = position - listView.getHeaderViewsCount();
			if (view!=null) pressedV = view;
			if (PDICMainAppOptions.dictManagerClickPopup() && false) {
				boolean start = mController.startDrag(position,0, view.getHeight()/2);
				return false;
			} else {
				showPopup(view);
			}
		}
		return true;
	}
	
	public void performLastItemLongClick() {
		if(adapter.getCount()>0){
			int idx = lastClickedPos[(lastClickedPosIndex+1)%2];
			int hc = listView.getHeaderViewsCount();
			// CMN.debug("performLastItemLongClick::", idx, hc);
			if(idx<hc||idx>=adapter.getCount()){
				idx = hc;
				ViewHolder vh = (ViewHolder) ViewUtils.getViewHolderInParents(listView.getChildAt(0), ViewHolder.class);
				if (vh != null) {
					idx = vh.position + hc;
				}
			}
			onItemLongClick(null, null, idx, 0);
		}
	}
	
	@Override
	DragSortListView.DropListener getDropListener() {
		return this;
	}
	
	@Override
	public void drop(int from, int to) {
		//CMN.Log("to", to);
		//if(true) return;
		boolean b1 = to<0;
		if(b1) to=-to;
		int pos=-1, top=0;
		ViewHolder vh = (ViewHolder) ViewUtils.getViewHolderInParents(listView.getChildAt(0), ViewHolder.class);
		if (vh != null) {
			pos = vh.position;
			top = ViewUtils.getNthParentNonNull(vh.itemView, 1).getTop();
		}
		if(a.opt.dictManager1MultiSelecting() && (getPlaceSelected(from) || b1)){
			ArrayList<BookPresenter> md_selected = new ArrayList<>(selected_size());
			ArrayList<PlaceHolder> ph_selected = new ArrayList<>(selected_size());
			if(to>from || b1) to++;
			for (int i = loadMan.md.size()-1; i >= 0; i--) {
				if(getPlaceSelected(i)){
					md_selected.add(0, loadMan.md.remove(i));
					ph_selected.add(0, loadMan.lazyMan.placeHolders.remove(i));
					if(i<to) {
						to--;
					}
					if (i < pos) {
						pos--;
					}
				}
			}
			loadMan.md.addAll(to, md_selected);
			loadMan.lazyMan.placeHolders.addAll(to, ph_selected);
			adapter.notifyDataSetChanged();
		}
		else if (from != to && !b1) {
			replace(from, to);
			adapter.notifyDataSetChanged();
			if (from < pos) {
				pos--;
			}
		}
		if (pos>=0) {
			listView.setSelectionFromTop(pos + listView.getHeaderViewsCount(), top);
		}
	}
	
	public void deleteSelOrOne(boolean one) {
		int szf1 = manager_group().size();
		new AlertDialog.Builder(getBookManager())
				.setTitle(getBookManager().mResource.getString(R.string.surerrecords, one?1:Selection.size()))
				.setMessage("从当前分组删除记录，不会删除文件或全部词典记录，但不可撤销。")
				.setPositiveButton(R.string.confirm, (dialog, which) -> {
					if (one) {
						remove(pressedPos);
					} else {
						for (int i = szf1 - 1; i >= 0; i--) {
							if (getPlaceSelected(i)) {
								remove(i);
							}
						}
					}
					refreshSize();
					dataSetChanged();
					dialog.dismiss();
				})
				.create().show();
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
				vh = new ViewHolder(convertView);
				vh.title.trimStart = false;
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			vh.position=position;
			//v.getBackground().setLevel(1000);
			//position = position - mDslv.getHeaderViewsCount();
			//mngr_agent_manageable mdTmp = adapter.getItem(position);
			
			if(query!=null && filtered.get(position)!=null)
				vh.title.setBackgroundResource(GlobalOptions.isDark?R.drawable.xuxian2_d:R.drawable.xuxian2);
			else
				vh.title.setBackground(null);

			final String key = getPathAt(position);
			final String name = CMN.getAssetName(key);
			final String suffix = CMN.getSuffix(key);

			if(a.opt.dictManager1MultiSelecting()){
				vh.ck.setVisibility(View.VISIBLE);
				vh.ck.setOnCheckedChangeListener(null);
				vh.ck.setChecked(getPlaceSelected(position));
				vh.ck.setOnCheckedChangeListener(checkChanged);
				vh.tweakCheck();
			} else {
				vh.ck.setVisibility(View.GONE);
			}
			ViewUtils.setVisibility(vh.handle, PDICMainAppOptions.sortDictManager());

			StringBuilder rgb = new StringBuilder("#");
			boolean disabled = getPlaceRejected(position);
			if(disabled)
				rgb.append("aaaaaa");//一样的亮兰色aafafa
			else
				rgb.append(GlobalOptions.isDark?"EEEEEE":"000000");
			if(!key.startsWith("/ASSET") && !new File(key).exists())
				rgb.insert(1, "ff");
			rgb.setLength(7);
			vh.title.setTextColor(Color.parseColor(rgb.toString()));
			vh.title.setPadding((int) (GlobalOptions.density*25),0,0,0);
			
			//int tmpFlag = getPlaceFlagAt(position);
			
			BookPresenter magent = getMagentAt(position);
			
			Drawable mLeftDrawable=null;
//			if(PDICMainAppOptions.getTmpIsFiler(tmpFlag)){
//				mLeftDrawable=mFilterDrawable;
//			}
//			if(PDICMainAppOptions.getTmpIsAudior(tmpFlag)){
//				mLeftDrawable=mAudioDrawable;
//			}
			if(suffix.equals(".web")){
				mLeftDrawable=mWebDrawable;
			}
			else if(suffix.equals(".pdf")){
				mLeftDrawable=mPDFDrawable;
			}
			
			BookPresenter thereYouAre = a.app_mdict_cache.get(new File(key).getName());
			
			vh.title.setCover(thereYouAre==null?null:thereYouAre.getCover());
			
			vh.title.setCompoundDrawables(mActiveDrawable,
					mLeftDrawable,
					magent.getIsDedicatedFilter()?mRightDrawable:null,
					magent.getAutoFold()?"<>":null);
			
			if (disabled) {
				vh.title.setText("// "+name);
			} else {
				vh.title.setText(name);
			}
			
			vh.title.setStarLevel(0);
			
			if(GlobalOptions.isDark) {
				convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}
			return convertView;
		}
	}
	
	public PopupMenuHelper getPopupMenu() {
		if (mPopup==null) {
			mPopup  = new PopupMenuHelper(getActivity(), null, null);
			mPopup.initLayout(new int[]{
					R.layout.poplist_shewei_gouciku
			}, new PopupMenuHelper.PopupMenuListener() {
				@Override
				public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View view, boolean isLongClick) {
					int position = pressedPos;
					boolean isOnSelected = getPlaceSelected(position);
					BookPresenter magent;
					if (isLongClick) {
						if (view.getId() == R.id.disable) {
							deleteSelOrOne(!isOnSelected);
							popupMenuHelper.dismiss();
						}
						return false;
					}
					switch (view.getId()) {
						/* 启用 禁用 */
						case R.id.enable:
						case R.id.disable:  {
							disEna(isOnSelected, view.getId()==R.id.disable, position);
						} break;
						case R.string.rename: {
							renameFile();
						} break;
						case R.id.move_sel: {
							int from = -1;
							if (PDICMainAppOptions.dictManager1MultiSelecting()) {
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										from = i;
										break;
									}
								}
							}
							if (from == -1) {
								a.showT("无操作");
							} else {
								drop(from, -pressedPos);
							}
						} break;
						case R.string.multi_select: {//多选
							a.opt.dictManager1MultiSelecting(!a.opt.dictManager1MultiSelecting());
							adapter.notifyDataSetChanged();
							a.f2.adapter.notifyDataSetChanged();
							d.dismiss();
						} break;
						case R.id.jianxuan: {//间选
							MenuItemImpl menu = (MenuItemImpl) ViewUtils.findInMenu(a.Menu1, R.id.toolbar_action1);
							menu.isLongClicked = PDICMainAppOptions.dictManagerFlipMenuCloumn();
							a.onMenuItemClick(menu);
						} break;
						case R.string.move_top: {//移至顶部
							markDirty(-1);
							replace(position, 0);
							adapter.notifyDataSetChanged();
							getListView().setSelection(0);
						} break;
						case R.string.move_bottom: {//移至底部
							markDirty(-1);
							int last = manager_group().size() - 1;
							replace(position, last);
							adapter.notifyDataSetChanged();
							getListView().setSelection(last);
						} break;
						case R.id.openFolder: {//在外部管理器打开路径
							magent = getMagentAt(position);
							StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
							try {
								startActivity(new Intent(Intent.ACTION_VIEW)
										.setDataAndType(Uri.fromFile(magent.f().getParentFile()), "resource/folder")
										.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								);
							} catch (Exception e) {
								a.show(R.string.no_suitable_app);
							}
						} break;
						case R.id.openFolderInner: {//在内置管理器打开路径
							magent = getMagentAt(position);
							DialogProperties properties = new DialogProperties();
							properties.selection_mode = DialogConfigs.SINGLE_MULTI_MODE;
							properties.selection_type = DialogConfigs.FILE_SELECT;
							properties.root = new File("/");
							properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
							properties.offset = magent.f().getParentFile();
							properties.opt_dir = new File(getOpt().pathToDatabases() + "favorite_dirs/");
							properties.dedicatedTarget = magent.f().getName();
							properties.opt_dir.mkdirs();
							properties.extensions = new HashSet<>();
							properties.extensions.add(".mdx");
							properties.extensions.add(".mdd");
							properties.title_id = R.string.app_name;
							properties.isDark = GlobalOptions.isDark;
							FilePickerDialog fdialog = new FilePickerDialog(a, properties);
							fdialog.show();
						} break;
						case R.id.tweak1://词典设置
							magent = getMagentAt(position);
							if (isOnSelected) {
								ArrayList<BookPresenter> arr = new ArrayList<>(selected_size());
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										arr.add(getMagentAt(i));
									}
								}
								BookPresenter[] data = arr.toArray(new BookPresenter[0]);
								BookPresenter.showDictTweaker(null, (Toastable_Activity) getActivity(), data);
							} else {
								BookPresenter.showDictTweaker(null, (Toastable_Activity) getActivity(), magent);
							}
							bDictTweakerOnceShowed = true;
							break;
						case R.id.tweak2://词典设置
							if (isOnSelected) {
								ArrayList<BookPresenter> arr = new ArrayList<>(selected_size());
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										arr.add(getMagentAt(i));
									}
								}
								BookPresenter[] data = arr.toArray(new BookPresenter[0]);
								getBookManager().showBookPreferences(data);
							} else {
								getBookManager().showBookPreferences(getMagentAt(position));
							}
							break;
						/* —— 设为滤器等等… —— */
						/* 设为点译词库 */
						case R.id.tapSch: {
							magent = getMagentAt(position);
							boolean isF = !magent.getIsDedicatedFilter();
							magent.setIsDedicatedFilter(isF);
							int cc=0;
							if (isOnSelected) {
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i) && !getPathAt(i).endsWith(".mdd")) {
										// setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsFiler(getPlaceFlagAt(i), isF));
										getMagentAt(i).setIsDedicatedFilter(isF);
										cc++;
									}
								}
							} else {
								cc=1;
							}
							tweakedDict = true;
							adapter.notifyDataSetChanged();
							if (cc > 1) {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "等" + cc + "个词典为点译词库" : "已取消" + magent.getDictionaryName() + "等" + cc + "个点译库");
							} else {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "为点译词库" : "已取消" + magent.getDictionaryName() + "点译词库");
							}
						} break;
						/* 设为默认折叠 */
						case R.id.fold: {
							magent = getMagentAt(position);
							boolean isF = !magent.getAutoFold();
							magent.setAutoFold(isF);
							int cc=0;
							if (isOnSelected) {
								for (int i = 0, sz = manager_group().size(); i < sz; i++) {
									if (getPlaceSelected(i)) {
										getMagentAt(i).setAutoFold(isF);
										cc++;
									}
								}
							} else {
								cc=1;
							}
							tweakedDict = true;
							adapter.notifyDataSetChanged();
							if (cc > 1) {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "等" + cc + "个词典为默认折叠" : "已取消" + magent.getDictionaryName() + "等" + cc + "默认折叠");
							} else {
								a.showT(isF ? "已设置" + magent.getDictionaryName() + "为默认折叠" : "已取消" + magent.getDictionaryName() + "默认折叠");
							}
						} break;
						/* 设为点译词库 */
//						case 1: {
//							markDirty(actualPosition);
//							boolean isCS = !PDICMainAppOptions.getTmpIsClicker(getPlaceFlagAt(actualPosition));
//							setPlaceFlagAt(actualPosition, PDICMainAppOptions.setTmpIsClicker(getPlaceFlagAt(actualPosition), isCS));
//							if (isOnSelected) {
//								for (int i = 0; i < manager_group().size(); i++) {
//									if (getPlaceSelected(i))
//										setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsClicker(getPlaceFlagAt(i), isCS));
//								}
//							}
//							adapter.notifyDataSetChanged();
//							a.showT(isCS ? "已设为译词库" : "已取消点译词库");
//						} break;
						/* 设为发音库( mdd 专有 ) */
//						case 3: {
//							if (isMddResourceAt(actualPosition)) {
//								markDirty(actualPosition);
//								boolean isCS = !PDICMainAppOptions.getTmpIsAudior(getPlaceFlagAt(actualPosition));
//								setPlaceFlagAt(actualPosition, PDICMainAppOptions.setTmpIsAudior(getPlaceFlagAt(actualPosition), isCS));
//								if(isCS) setPlaceFlagAt(actualPosition, PDICMainAppOptions.setTmpIsFiler(getPlaceFlagAt(actualPosition), false));
//								if (isOnSelected) {
//									for (int i = 0; i < manager_group().size(); i++) {
//										if (getPlaceSelected(i)) {
//											setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsAudior(getPlaceFlagAt(i), isCS));
//											if(isCS) setPlaceFlagAt(i, PDICMainAppOptions.setTmpIsFiler(getPlaceFlagAt(actualPosition), false));
//										}
//									}
//								}
//								adapter.notifyDataSetChanged();
//								a.showT(isCS ? "已设为发音库" : "已取消发音库");
//							}
//						} break;
					}
					popupMenuHelper.dismiss();
					return true;
				}
			});
		}
		return mPopup;
	}
	
	public void disEna(boolean useSelection, boolean off, int position) {
		int cc=0;
		if (useSelection) {
			for (int i = 0, sz = manager_group().size(); i < sz; i++) {
				if (getPlaceSelected(i)) {
					markDirty(i);
					setPlaceRejected(i, off);
					cc++;
					if(position==-1) position = i;
				}
			}
		} else if(position!=-1){
			markDirty(position);
			setPlaceRejected(position, off);
			cc = 1;
		}
		adapter.notifyDataSetChanged();
		refreshSize();
		if (cc > 1) {
			a.showT(!off ? "已启用" + getNameAt(position) + "等" + cc + "个词典" : "已禁用" + getNameAt(position) + "等" + cc + "个词典");
		} else if (cc > 0) {
			a.showT(!off ? "已启用" + getNameAt(position) : "已禁用" + getNameAt(position));
		} else {
			a.showT("当前分组无选中词典。");
		}
	}
	
	private void renameFile() {
		BookPresenter magent = getMagentAt(pressedPos);
		boolean isOnSelected = getPlaceSelected(pressedPos);
		
		View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.settings_dumping_dialog, null);
		final ListView lv = dialog1.findViewById(R.id.lv);
		final EditText et = dialog1.findViewById(R.id.et);
		ImageView iv = dialog1.findViewById(R.id.confirm);
		et.setText(getNameAt(pressedPos));
		
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
					String toFn = getOpt().tryGetDomesticFileName(to.getPath());
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
							MagentTransient mdTmp = a.new_MagentTransient(to.getAbsolutePath(), getOpt(), null, true);
							loadMan.md.set(pressedPos, mdTmp);
							loadMan.lazyMan.placeHolders.set(pressedPos, mdTmp.getPlaceHolder());
							suc = true;
						}
					}
					if (suc) {
						a.RebasePath(oldf, OldFName, to, newName, oldFn);
						adapter.notifyDataSetChanged();
						markDirty(-1);
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
								f3.data.insert(new mFile(to).init(getOpt()));
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
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(BookManager) getActivity();
		if(a!=null) {
			Resources resource = a.getResources();
			mActiveDrawable = resource.getDrawable(R.drawable.star_ic_solid);
			mFilterDrawable = resource.getDrawable(R.drawable.filter);
			mAudioDrawable = resource.getDrawable(R.drawable.voice_ic_big);
			mWebDrawable = resource.getDrawable(R.drawable.ic_web);
			mPDFDrawable = resource.getDrawable(R.drawable.file_pdf_box);
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
			listView.setOnItemClickListener((parent, v, position, id) -> {
				//CMN.show(""+(adapter==null)+" "+(((dict_manager_activity)getActivity()).f1.adapter==null));
				//adapter.getItem(position).value = !adapter.getItem(position).value;//TODO optimize
				if (position >= listView.getHeaderViewsCount()) {
					pressedPos = position - listView.getHeaderViewsCount();
					pressedV = v;
					if (PDICMainAppOptions.dictManagerClickPopup()) {
						showPopup(v);
					} else {
						markDirty(pressedPos);
						setPlaceRejected(pressedPos, !getPlaceRejected(pressedPos));
						adapter.notifyDataSetChanged();
						refreshSize();
					}
				}
			});

			listView.setOnItemLongClickListener(this);
			setListAdapter();
			refreshSize();
			listView.setSelectionFromTop(lastViewPos, lastViewTop);
		}
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
			v.setBackgroundColor(GlobalOptions.isDark?0xFFc17d33:0xFFffff00);//TODO: get primary color
			markDirty(-1);
			return v;
		}

		@Override
		public void onDestroyFloatView(View floatView) {
			//do nothing; block super from crashing
		}

	}
	
	public static class ViewHolder{
		public int position;
		public View itemView;
		public ImageView handle;
		public FlowTextView title;
		public CheckBox ck;
		boolean isDark;

		public ViewHolder(View v) {
			itemView = v;
			handle = v.findViewById(R.id.drag_handle);
			title = v.findViewById(R.id.text);
			ck = v.findViewById(R.id.check1);
			v.setTag(this);
		}
		
		public void tweakCheck() {
			if (isDark!=GlobalOptions.isDark) {
				isDark = isDark!=GlobalOptions.isDark;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					ck.getButtonDrawable().setColorFilter(isDark?GlobalOptions.NEGATIVE_1:null);
				}
			}
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
		setPlaceSelectedInter(ph, val);
		if (val) {
			Selection.add(ph);
		} else {
			Selection.remove(ph);
		}
	}
	
	private void setPlaceSelectedInter(PlaceHolder ph, boolean val) {
		ph.lineNumber &= ~0x80000000;
		if(val) ph.lineNumber |= 0x80000000;
	}
	
	boolean getPlaceRejected(int position) {
		return PDICMainAppOptions.getTmpIsHidden(getPlaceFlagAt(position));
	}
	
	void setPlaceRejected(int position, boolean val) {
		int flag = getPlaceFlagAt(position);
		if (PDICMainAppOptions.getTmpIsHidden(flag)!=val) {
			setPlaceFlagAt(position, PDICMainAppOptions.setTmpIsHidden(flag,val));
			loadMan.lazyMan.chairCount += val?-1:1;
		}
	}
	
	final ArrayList<BookPresenter> manager_group() {
		return loadMan.md;
	}
	
	final ArrayList<PlaceHolder> place_group() {
		return loadMan.lazyMan.placeHolders;
	}
	
	public int selected_size() {
		return Selection.size();
	}
	
	public void remove(int i) {
		markDirty(-1);
		replace(i, -1);
	}
	
	public void replace(int from, int to) {
		if (to < 0) {
			BookPresenter rmd = loadMan.md.remove(from);
			if(rmd!=null) a.mdict_cache.put(rmd.getPath(), rmd);
			PlaceHolder ph = loadMan.lazyMan.placeHolders.remove(from);
			loadMan.lazyMan.chairCount--;
			Selection.remove(ph);
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
		return loadMan.lazyMan.placeHolders.get(position).getPath(getOpt()).toString();
	}
	
	public int getPlaceFlagAt(int position) {
//		BookPresenter mdTmp = loadMan.md.get(position);
//		if (mdTmp!=null) {
//			return mdTmp.tmpIsFlag;
//		}
//		mdTmp = getBookManager().getMagentAt(position, false);
//		if (mdTmp!=loadMan.EmptyBook) {
//			return mdTmp.tmpIsFlag;
//		}
		return loadMan.lazyMan.placeHolders.get(position).tmpIsFlag;
	}
	
	private void setPlaceFlagAt(int position, int flag) {
		markDirty(position);
//		BookPresenter mdTmp = loadMan.md.get(position);
//		if (mdTmp!=null) {
//			mdTmp.tmpIsFlag = flag;
//		}
//		mdTmp = getBookManager().getMagentAt(position, false);
//		if (mdTmp!=loadMan.EmptyBook) {
//			mdTmp.tmpIsFlag = flag;
//		}
		loadMan.lazyMan.placeHolders.get(position).tmpIsFlag = flag;
	}
	
	public CharSequence getNameAt(int position) {
		String path = getPathAt(position);
		int idx = path.lastIndexOf('/');
		if(idx>0)
			return new CharSequenceKey(path, idx+1);
		return path;
	}
	
	private boolean isMddResourceAt(int actualPosition) {
		String path = getPathAt(actualPosition);
		return path.length()>4 && path.substring(path.length()-4).equalsIgnoreCase(".mdd");
	}
	
	public void markDirty(int attrPos) {
		if (attrPos >= 0/* && !isDataDirty()*/) {
			PlaceHolder ph = loadMan.lazyMan.placeHolders.get(attrPos);
			Integer val = dirtyAttrArray.get(ph);
			if (val==null) {
				dirtyAttrArray.put(ph, ph.tmpIsFlag);
			}
		}
		else markDataDirty(true);
		if (!isDirty) {
			isDirty = true;
			a.markDirty();
		}
	}
	
	public final boolean isDataDirty(){ return placeArray!=null; };
	private PlaceHolder[] placeArray;
	HashMap<PlaceHolder, Integer> dirtyAttrArray = new HashMap<>();
	public PlaceHolder[] markDataDirty(boolean dirty) {
		if (dirty!=isDataDirty()) {
			if (dirty) {
				placeArray = new PlaceHolder[loadMan.lazyMan.placeHolders.size()];
				for (int i = 0; i < placeArray.length; i++) {
					placeArray[i] = loadMan.lazyMan.placeHolders.get(i).clone();
				}
			} else {
				placeArray = null;
				dirtyAttrArray.clear();
			}
		}
		return placeArray;
	}
	
	public int schFilter(String query) {
		int prvSz = filtered.size();
		this.query = query;
		filtered.clear();
		if (!TextUtils.isEmpty(query)) {
			for (int i = 0; i < manager_group().size(); i++) {
				String name = getNameAt(i).toString();
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
	
	
	public void checkTweakedDict() {
		CMN.debug("checkTweakedDict", tweakedDict);
		if (tweakedDict) {
			for (int i = 0, sz = manager_group().size(); i < sz; i++) {
				a.getMagentAt(i, false).checkFlag(a);
			}
			tweakedDict = false;
		}
	}
}

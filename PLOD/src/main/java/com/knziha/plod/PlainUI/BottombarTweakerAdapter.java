package com.knziha.plod.PlainUI;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.ankislicer.customviews.ShelfLinearLayout;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.FloatSearchActivity;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import static com.knziha.plod.PlainUI.ButtonUIData.BottombarBtnIcons;
import static com.knziha.plod.plaindict.MainActivityUIBase.init_clickspan_with_bits_at;
import static com.knziha.plod.PlainUI.ButtonUIProject.ContentbarBtnIcons;
import static com.knziha.plod.PlainUI.ButtonUIProject.RebuildBottombarIcons;
import static com.knziha.plod.plaindict.暂未实现帮助类.没有实现的_工具栏_点击事件不完全列表;

public class BottombarTweakerAdapter extends BaseAdapter implements View.OnClickListener, DragSortListView.DragSortListener, View.OnLongClickListener{
	private final AlertDialog dialog;
	private final MainActivityUIBase a;
	private final PDICMainAppOptions opt;
	private final String[] toolbarNames;
	public final DragSortListView main_list;
	public final ShelfLinearLayout sideBar;
	
	public ButtonUIProject projectContext;
	public boolean isDirty;
	public Drawable switch_landscape;
	public boolean isDark;
	PorterDuffColorFilter darkMask = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
	private View simView;
	
	public BottombarTweakerAdapter(MainActivityUIBase _a, int desiredTab) {
		opt = _a.opt;
		a = _a;
		switch_landscape = _a.getResources().getDrawable(R.drawable.ic_screen_rotation_black_24dp);
		toolbarNames = a.getResources().getStringArray(R.array.bottombar_types);
		
		dialog = new AlertDialog.Builder(_a).setView(R.layout.customise_btns)
				.setTitle("定制工具")
				.setIcon(R.drawable.settings)
				.setPositiveButton(R.string.confirm, null)
				.setNegativeButton(R.string.cancel, null)
				.show();
		
		sideBar = dialog.findViewById(R.id.sideBar);
		main_list = dialog.findViewById(R.id.main_list);
		((SimpleFloatViewManager)main_list.mFloatViewManager).mFloatBGColor=0xff3185F7;
		
		sideBar.selectToolIndex(0);
		sideBar.setSCC(sideBar.ShelfDefaultGray=0xFF4F7FDF);
		
		//ada.projectContext = bottombar_project;
		main_list.setAdapter(this);
		main_list.setDragListener(this);
		setBtnsListener(sideBar
				, dialog.getButton(DialogInterface.BUTTON_POSITIVE)
				, dialog.getButton(DialogInterface.BUTTON_NEGATIVE));
		
		sideBar.getChildAt(desiredTab).performClick();
		sideBar.postDelayed(() -> sideBar.selectToolIndex(desiredTab), 350);
		
		isDark = GlobalOptions.isDark;
	}

	public String MakeProject(){
		int size = projectContext.iconData.size()-1;
		StringBuilder sb = new StringBuilder(64);
		projectContext.bNeedCheckOrientation=false;
		for (int i = 0; i <= size; i++) {
			AppIconData icI = projectContext.iconData.get(i);
			icI.addString(sb);
			if(icI.tmpIsFlag==2){
				projectContext.bNeedCheckOrientation=true;
			}
			if(i!=size) sb.append("|");
		}
		return sb.toString();
	}

	@Override
	public int getCount() {
		return projectContext==null?0:projectContext.iconData.size();
	}

	@Override
	public Object getItem(int position) {
		return projectContext.iconData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return projectContext.iconData.get(position).number;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh=convertView==null?new ViewHolder(a, parent, this):(ViewHolder)convertView.getTag();
		AppIconData item = projectContext.iconData.get(position);
		int id = item.number;
		Resources res = parent.getResources();
		vh.tv.setText(projectContext.titles[id]);
		vh.tv.setTextColor(a.AppBlack);
		int ID = projectContext.icons[id];
		if (ID == 0) {
			vh.iconBtn.setImageDrawable(null);
		} else {
			vh.iconBtn.setImageDrawable(res.getDrawable(ID));
			vh.iconBtn.setColorFilter(isDark?0xffffffff:0xff3185F7, PorterDuff.Mode.SRC_IN);
		}
		vh.togBtn.setChecked(item.tmpIsFlag,false);
		ViewUtils.setVisibleV3(vh.option, hasOpt(ID));
		vh.position = position;
		vh.itemView.getBackground().setAlpha(isDark?15:255);
		if (没有实现的_工具栏_点击事件不完全列表.contains(ID)) {
			vh.itemView.setAlpha(0.2f);
		} else {
			vh.itemView.setAlpha(1);
		}
		return vh.itemView;
	}

	// long-click
	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		switch (id){
			default:
				/* 拷贝配置 */
				int currentType = projectContext.type;
				if(currentType>=0){
					int bottombar_copy_from=0;
					if(id == R.id.customise_peruse_bar){
						bottombar_copy_from=1;
					} else if(id == R.id.customise_float_bar){
						bottombar_copy_from=2;
					}
					if(bottombar_copy_from!=currentType){
						String copy_from = toolbarNames[bottombar_copy_from+1];
						String copy_to = toolbarNames[currentType+1];
						int final_Bottombar_copy_from = bottombar_copy_from;
						
						String[] DictOpt = a.getResources().getStringArray(R.array.appbar_conf);
						final String[] Coef = DictOpt[0].split("_");
						final SpannableStringBuilder ssb = new SpannableStringBuilder();
						
						TextView tv = a.buildStandardConfigDialog(a, false, (View.OnClickListener)v12 -> {
							opt.linkContentbarProject(currentType, final_Bottombar_copy_from);
							projectContext.currentValue = opt.getAppContentBarProject(currentType);
							isDirty = false;
							projectContext.instantiate();
							notifyDataSetChanged();
							a.showX(opt.getLinkContentBarProj()?R.string.linkedft:R.string.copyedft, Toast.LENGTH_LONG, copy_to, copy_from);
						}, R.string.warn_copyconfrom, copy_from);
						
						init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 41, 1, 4, -1, true);
						
						MainActivityUIBase.showStandardConfigDialog(tv, ssb);
					}
				}
			break;
			case android.R.id.button1:{
				/* 恢复默认 */
				String[] DictOpt = a.getResources().getStringArray(R.array.appbar_all);
				final String[] Coef = DictOpt[0].split("_");
				final SpannableStringBuilder ssb = new SpannableStringBuilder();
				
				TextView tv = a.buildStandardConfigDialog(a, false, (View.OnClickListener)v13 -> {
					opt.clearAppProjects(projectContext.key);
					if(opt.getRestoreAllBottombarProj()){
						if(a instanceof PDICMainActivity){
							((PDICMainActivity)a).bottombar_project.clear(a);
						}
						if (a.contentbar_project!=null) {
							a.contentbar_project.clear(a);
						}
						if (a.peruseview_project!=null) {
							a.peruseview_project.clear(a);
						}
					}
					projectContext.clear(a);
					projectContext.instantiate();
					notifyDataSetChanged();
				}, R.string.warn_reconf);
				
				init_clickspan_with_bits_at(tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 43, 1, 4, -1, true);
				
				MainActivityUIBase.showStandardConfigDialog(tv, ssb);
			} break;
			case android.R.id.button2:{
				/* 三态/二态切换 */
				//ColorPickerDialog.showTopToast(v.getContext(), "StateCount#"+StateCount);
			} break;
		}
		return true;
	}
	
	// click
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			default: {
				if(isDirty){
					checkCurrentProject(v);
					break;
				}
				int toolbarIdx=0;
				ButtonUIProject projectContext=null;
				if(id == R.id.customise_main_bar) {
					if(a instanceof PDICMainActivity){
						PDICMainActivity aa = (PDICMainActivity) a;
						projectContext = aa.bottombar_project;
						if(projectContext==null){
							aa.bottombar_project = projectContext = new ButtonUIProject(a, "btmprj", BottombarBtnIcons, R.array.customize_btm, opt.getAppBottomBarProject(), aa.bottombar, aa.BottombarBtns);
						}
						if(projectContext.iconData==null){
							projectContext.instantiate();
						}
					}
					else {
						//tofo 跳转至主界面？
					}
				}
				else if(id == R.id.customise_wp_top){
					a.wordPopup.init();
					projectContext = a.wordPopup.toolbarProject;
					if(projectContext.iconData==null){
						projectContext.instantiate();
					}
					toolbarIdx=4;
				}
				else if(id == R.id.customise_wp_bot){
					a.wordPopup.init();
					projectContext = a.wordPopup.bottombarProject;
					if(projectContext.iconData==null){
						projectContext.instantiate();
					}
					toolbarIdx=5;
				}
				else {
					if(id == R.id.customise_peruse_bar){
						toolbarIdx=1;
					} else if(id == R.id.customise_float_bar){
						toolbarIdx=2;
					}
					boolean isProjHost = false;
					projectContext = toolbarIdx==1?a.peruseview_project:
							(isProjHost = toolbarIdx==0?a instanceof PDICMainActivity:a instanceof FloatSearchActivity)?
									a.contentbar_project:null;
					
					if(projectContext==null) {
						projectContext = new ButtonUIProject(a, toolbarIdx, a.opt, ContentbarBtnIcons, R.array.customize_ctn, null, null);
						if(toolbarIdx==1){
							/* fyms */
							a.peruseview_project = projectContext;
							if(a.peruseView != null/* && projectContext.btns==null*/){
								projectContext.addBar(a.peruseView.contentUIData.bottombar2, a.peruseView.weblistHandler.ContentbarBtns);
							}
						} else if(isProjHost){
							projectContext.addBar(a.contentUIData.bottombar2, a.weblistHandler.ContentbarBtns);
							a.contentbar_project = projectContext;
						}
					}

					if(projectContext.iconData==null){
						projectContext.instantiate();
					}
					toolbarIdx++;
				}
				if(projectContext!=null){
					this.projectContext=projectContext;
					((ShelfLinearLayout) v.getParent()).selectToolView(v);
					dialog.setTitle("定制工具 - "+ toolbarNames[toolbarIdx]);
					notifyDataSetChanged();
				}
			} break;
			case android.R.id.button1:{
				if(projectContext==null) return;
				checkCurrentProjectInternal(null);
				dialog.dismiss();
			} break;
			case android.R.id.button2: {
				if(isDirty && projectContext!=null) {
					DialogInterface.OnClickListener ocl = (dialog, which) -> {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							clearCurrentProject();
						} else if (which == DialogInterface.BUTTON_NEUTRAL) {
							checkCurrentProjectInternal(null);
						} else {
							dialog.dismiss();
							return;
						}
						dialog.dismiss();
						v.performClick();
					};
					AlertDialog dd = new AlertDialog.Builder(a)
							.setTitle("是否应用已更改的配置？")
							.setPositiveButton("忽略更改x", ocl)
							.setNegativeButton("取消", ocl)
							.setNeutralButton("应用√", ocl)
							.create();
					ViewUtils.ensureWindowType(dd, a, null);
					dd.show();
				} else {
					dialog.dismiss();
				}
			} break;
			case android.R.id.text1:
			case R.id.check1:{
				isDirty=true;
				ViewGroup vp = ((ViewGroup) v.getParent());
				ViewHolder vh = (ViewHolder) vp.getTag();
				CircleCheckBox ccb_toggle = (CircleCheckBox) vp.getChildAt(0);
				ccb_toggle.setProgress(0);
				//if(StateCount==2) ccb_toggle.iterate();
				//if(id==R.id.check1) ccb_toggle.iterate();
				ccb_toggle.toggle();
				projectContext.iconData.get(vh.position).tmpIsFlag=ccb_toggle.getChecked();
			} break;
			case R.id.check2:{
				ViewGroup vp = ((ViewGroup) v.getParent());
				ViewHolder vh = (ViewHolder) vp.getTag();
				int ID = projectContext.icons[projectContext.iconData.get(vh.position).number];
				//if (hideAfterSim(ID))
					dialog.hide();
				try {
					//CircleCheckBox ccb_icon = (CircleCheckBox) v;
					//ccb_icon.setProgress(0);
					//ccb_icon.addAnim(true);
					if(simView==null)
						simView = new View(a);
					simView.setId(ID);
					a.onClick(simView);
				} catch (Exception e) {
					a.showT(e.getLocalizedMessage());
					dialog.show();
				}
			} break;
		}
	}
	
	boolean hideAfterSim(int id) {
		switch (id) {
			default: return true;
//			case R.drawable.:
//				return false;
		}
	}
	
	boolean hasOpt(int id) {
		switch (id) {
			default: return false;
			case R.drawable.ic_prv_dict_chevron:
			case R.drawable.ic_nxt_dict_chevron:
				return true;
		}
	}
	
	void checkCurrentProject(View v) {
		String val = MakeProject();
		if(v!=null && projectContext!=null && !val.equals(projectContext.currentValue)){
			DialogInterface.OnClickListener ocl = (dialog, which) -> {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					clearCurrentProject();
				} else if (which == DialogInterface.BUTTON_NEUTRAL) {
					checkCurrentProjectInternal(val);
				} else {
					dialog.dismiss();
					return;
				}
				dialog.dismiss();
				v.performClick();
			};
			AlertDialog dd = new AlertDialog.Builder(a)
				.setTitle("是否应用已更改的配置？")
				.setPositiveButton("忽略更改x", ocl)
				.setNegativeButton("取消", ocl)
				.setNeutralButton("应用√", ocl)
				.create();
			ViewUtils.ensureWindowType(dd, a, null);
			dd.show();
		} else {
			checkCurrentProjectInternal(val);
		}
	}

	void clearCurrentProject() {
		if(isDirty && projectContext!=null){
			ButtonUIProject _projectContext = projectContext;
			projectContext=null;
			_projectContext.clear(null);
		}
		isDirty=false;
	}

	void checkCurrentProjectInternal(String newVal) {
		if(isDirty){
			if(newVal==null) newVal = MakeProject();
			boolean bNeedSave = !newVal.equals(projectContext.currentValue);
			if(bNeedSave){
				CMN.debug("保存配置……", projectContext.key);
				projectContext.currentValue=newVal;
				RebuildBottombarIcons(a, projectContext, a.mConfiguration);
				opt.putAppProject(projectContext);
				if(projectContext.type>=0){
					checkReferncedChange(a.contentbar_project, newVal);
					checkReferncedChange(a.peruseview_project, newVal);
				}
			}
			isDirty=false;
		}
	}
	
	private void checkReferncedChange(ButtonUIProject checkNow, String newVal) {
		if(checkNow!=null && checkNow!=projectContext && opt.isAppContentBarProjectReferTo(checkNow.key, projectContext.type)){
			CMN.debug(checkNow.key,  "refer to >> ", projectContext.key);
			checkNow.currentValue=newVal;
			checkNow.clear(null);
			RebuildBottombarIcons(a, checkNow, a.mConfiguration);
		}
	}
	
	@Override
	public void drag(int from, int to) {

	}

	@Override
	public void drop(int from, int to) {
		if (from != to) {
			isDirty=true;
			dialog.setCanceledOnTouchOutside(false);
			AppIconData item = projectContext.iconData.remove(from);
			projectContext.iconData.add(to, item);
			notifyDataSetChanged();
		}
	}
	@Override
	public void remove(int which) {  }
	
	public void setBtnsListener(View...views) {
		for (int i = 0; i < views.length; i++) {
			views[i].setOnClickListener(this);
			views[i].setOnLongClickListener(this);
		}
	}
	
	public void editToolbar(int pos) {
		onClick(sideBar.getChildAt(pos));
	}
	
	public void showDialog() {
		ViewUtils.ensureWindowType(dialog, a, null);
		dialog.show();
		dialog.setCanceledOnTouchOutside(true);
	}
	
	static class ViewHolder{
		ViewHolder(MainActivityUIBase a, ViewGroup v, BottombarTweakerAdapter ta){
			CircleCheckBox b;
			itemView=(ViewGroup)a.getLayoutInflater().inflate(R.layout.circle_checkers_btn_config, v, false);
			itemView.setTag(this);
			iconBtn =  (ImageView) itemView.getChildAt(1);
			iconBtn.setOnClickListener(ta);
			togBtn = b = (CircleCheckBox) itemView.getChildAt(0);
			b.drawIconForEmptyState=false;
			b.circle_shrinkage=0.75f*GlobalOptions.density;
			b.addStateWithDrawable(ta.switch_landscape);
			b.setOnClickListener(ta);
			if(GlobalOptions.isLarge)
				b.setSize((int) a.mResource.getDimension(R.dimen._35_));
			tv = (TextView) itemView.getChildAt(2);
			tv.setOnClickListener(ta);
			option = itemView.getChildAt(3);
		}
		private final ViewGroup itemView;
		public int position;
		/** 点击图标 */
		ImageView iconBtn;
		/** 打勾 */
		CircleCheckBox togBtn;
		View option;
		TextView tv;
	}
}

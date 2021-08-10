package com.knziha.plod.PlainUI;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.knziha.ankislicer.customviews.ShelfLinearLayout;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.FloatSearchActivity;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import static com.knziha.plod.plaindict.MainActivityUIBase.init_clickspan_with_bits_at;
import static com.knziha.plod.PlainUI.AppUIProject.ContentbarBtnIcons;
import static com.knziha.plod.PlainUI.AppUIProject.RebuildBottombarIcons;
import static com.knziha.plod.plaindict.暂未实现帮助类.没有实现的_工具栏_点击事件不完全列表;

public class BottombarTweakerAdapter extends BaseAdapter implements View.OnClickListener, DragSortListView.DragSortListener, View.OnLongClickListener{
	private final AlertDialog dialog;
	private final MainActivityUIBase a;
	private final PDICMainAppOptions opt;
	private final String[] bottombar_types;
	public final DragSortListView main_list;
	private final ShelfLinearLayout sideBar;
	
	public AppUIProject projectContext;
	public static int StateCount=2;
	public boolean isDirty;
	public Drawable switch_landscape;
	PorterDuffColorFilter darkMask = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
	private String[] customize_ctn;
	
	public BottombarTweakerAdapter(MainActivityUIBase _a, int desiredTab) {
		opt = _a.opt;
		a = _a;
		switch_landscape = _a.getResources().getDrawable(R.drawable.ic_screen_rotation_black_24dp);
		bottombar_types = a.getResources().getStringArray(R.array.bottombar_types);
		
		dialog = new AlertDialog.Builder(_a).setView(R.layout.customise_btns)
				.setTitle("定制底栏")
				.setIcon(R.drawable.settings)
				.setPositiveButton(R.string.confirm, null)
				.setNegativeButton(R.string.cancel, null)
				.show();
		
		sideBar = dialog.findViewById(R.id.sideBar);
		main_list = dialog.findViewById(R.id.main_list);
		((SimpleFloatViewManager)main_list.mFloatViewManager).mFloatBGColor=0xff3185F7;
		
		sideBar.setRbyPos(0);
		sideBar.setSCC(sideBar.ShelfDefaultGray=0xFF4F7FDF);
		
		//ada.projectContext = bottombar_project;
		main_list.setAdapter(this);
		main_list.setDragListener(this);
		setLongOnClickListener(sideBar, dialog.getButton(DialogInterface.BUTTON_POSITIVE), dialog.getButton(DialogInterface.BUTTON_NEGATIVE));
		
		sideBar.getChildAt(desiredTab).performClick();
		sideBar.postDelayed(() -> sideBar.setRbyPos(desiredTab), 350);
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
		ViewHolder vh=convertView==null?new ViewHolder(parent, this):(ViewHolder)convertView.getTag();
		AppIconData item = projectContext.iconData.get(position);
		int id = item.number;
		Resources res = parent.getResources();
		vh.tv.setText(projectContext.titles[id]);
		vh.ccb_icon.setDrawable(0, res.getDrawable(projectContext.icons[id]));
		vh.ccb_toggle.setChecked(item.tmpIsFlag,false);
		vh.position = position;
		vh.itemView.getBackground().setAlpha(GlobalOptions.isDark?15:255);
		if (没有实现的_工具栏_点击事件不完全列表.contains(projectContext.icons[id])) {
			vh.itemView.setAlpha(0.2f);
		} else {
			vh.itemView.setAlpha(1);
		}
		return vh.itemView;
	}

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
						String copy_from = bottombar_types[bottombar_copy_from+1];
						String copy_to = bottombar_types[currentType+1];
						int final_Bottombar_copy_from = bottombar_copy_from;
						
						String[] DictOpt = a.getResources().getStringArray(R.array.appbar_conf);
						final String[] Coef = DictOpt[0].split("_");
						final SpannableStringBuilder ssb = new SpannableStringBuilder();
						
						TextView tv = a.buildStandardConfigDialog(a, false, v12 -> {
							opt.linkContentbarProject(currentType, final_Bottombar_copy_from);
							projectContext.currentValue = opt.getAppContentBarProject(currentType);
							isDirty = false;
							projectContext.instantiate(customize_ctn);
							notifyDataSetChanged();
							a.showX(opt.getLinkContentBarProj()?R.string.linkedft:R.string.copyedft, Toast.LENGTH_LONG, copy_to, copy_from);
						}, R.string.warn_copyconfrom, copy_from);
						
						init_clickspan_with_bits_at(a, tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 41, 1, 4, -1, true);
						
						MainActivityUIBase.showStandardConfigDialog(tv, ssb);
					}
				}
			break;
			case android.R.id.button1:{
				/* 恢复默认 */
				String[] DictOpt = a.getResources().getStringArray(R.array.appbar_all);
				final String[] Coef = DictOpt[0].split("_");
				final SpannableStringBuilder ssb = new SpannableStringBuilder();
				
				TextView tv = a.buildStandardConfigDialog(a, false, v13 -> {
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
					projectContext.instantiate(null);
					notifyDataSetChanged();
				}, R.string.warn_reconf);
				
				init_clickspan_with_bits_at(a, tv, ssb, DictOpt, 1, Coef, 0, 0, 0x1, 43, 1, 4, -1, true);
				
				MainActivityUIBase.showStandardConfigDialog(tv, ssb);
			} break;
			case android.R.id.button2:{
				/* 三态/二态切换 */
				if(StateCount==2){
					StateCount=1;
				} else {
					StateCount=2;
				}
				ColorPickerDialog.showTopToast(v.getContext(), "StateCount#"+StateCount);
			} break;
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			default: {
				if(isDirty){
					checkCurrentProject(v);
					break;
				}
				int bottombar_from=0;
				AppUIProject projectContext=null;
				if(id == R.id.customise_main_bar){
					if(a instanceof PDICMainActivity){
						PDICMainActivity aa = (PDICMainActivity) a;
						projectContext = aa.bottombar_project;
						if(projectContext==null){
							aa.bottombar_project = projectContext = new AppUIProject("btmprj", aa.BottombarBtnIcons, opt.getAppBottomBarProject(), aa.bottombar, aa.BottombarBtns);
						}
						if(projectContext.iconData==null){
							projectContext.instantiate(aa.getResources().getStringArray(R.array.customize_btm));
						}
					}
					else {
						//tofo 跳转至主界面？
					}
				}
				else {
					if(id == R.id.customise_peruse_bar){
						bottombar_from=1;
					} else if(id == R.id.customise_float_bar){
						bottombar_from=2;
					}
					boolean isProjHost = false;
					projectContext = bottombar_from==1?a.peruseview_project:
							(isProjHost = bottombar_from==0?a instanceof PDICMainActivity:a instanceof FloatSearchActivity)?
									a.contentbar_project:null;
					
					if(projectContext==null) {
						projectContext = new AppUIProject(bottombar_from, a.opt, ContentbarBtnIcons, null, null);
						if(bottombar_from==1){
							/* fyms */
							a.peruseview_project = projectContext;
							if(a.PeruseView != null && projectContext.btns==null){
								projectContext.bottombar = a.PeruseView.bottombar2;
								projectContext.btns = a.PeruseView.ContentbarBtns;
							}
						} else if(isProjHost){
							projectContext.bottombar = a.bottombar2;
							projectContext.btns = a.ContentbarBtns;
							a.contentbar_project = projectContext;
						}
					}

					if(projectContext.iconData==null){
						if(customize_ctn==null)
							customize_ctn=a.getResources().getStringArray(R.array.customize_ctn);
						projectContext.instantiate(customize_ctn);
					}
					bottombar_from++;
				}
				if(projectContext!=null){
					this.projectContext=projectContext;
					((ShelfLinearLayout) v.getParent()).setRbyView(v);
					dialog.setTitle("定制底栏 - "+bottombar_types[bottombar_from]);
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
							checkCurrentProjectInternal(null);
						} else if (which == DialogInterface.BUTTON_NEGATIVE) {
							clearCurrentProject();
						}
						dialog.dismiss();
						v.performClick();
					};
					new AlertDialog.Builder(a)
							.setTitle("配置已更改，是否应用？")
							.setPositiveButton(R.string.confirm, ocl)
							.setNegativeButton(R.string.no, ocl)
							.setNeutralButton(R.string.cancel, ocl)
							.create().show();
				} else {
					dialog.dismiss();
				}
			} break;
			case android.R.id.text1:
			case R.id.check1:{
				isDirty=true;
				ViewGroup vp = ((ViewGroup) v.getParent());
				ViewHolder vh = (ViewHolder) vp.getTag();
				CircleCheckBox ccb_toggle = (CircleCheckBox) vp.getChildAt(1);
				ccb_toggle.setProgress(0);
				if(StateCount==2){
					ccb_toggle.iterate();
				} else {
					ccb_toggle.toggle();
				}
				projectContext.iconData.get(vh.position).tmpIsFlag=ccb_toggle.getChecked();
			} break;
			case R.id.check2:{
				CircleCheckBox ccb_icon = (CircleCheckBox) v;
				ccb_icon.setProgress(0);
				ccb_icon.addAnim(true);
			} break;
		}
	}

	void checkCurrentProject(View v) {
		String val = MakeProject();
		if(v!=null && projectContext!=null && !val.equals(projectContext.currentValue)){
			DialogInterface.OnClickListener ocl = (dialog, which) -> {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					checkCurrentProjectInternal(val);
				} else if (which == DialogInterface.BUTTON_NEGATIVE) {
					clearCurrentProject();
				}
				dialog.dismiss();
				v.performClick();
			};
			new AlertDialog.Builder(a)
				.setTitle("配置已更改，是否应用？")
				.setPositiveButton(R.string.confirm, ocl)
				.setNegativeButton(R.string.no, ocl)
				.setNeutralButton(R.string.cancel, ocl)
				.create().show();
		} else {
			checkCurrentProjectInternal(val);
		}
	}

	void clearCurrentProject() {
		if(isDirty && projectContext!=null){
			AppUIProject _projectContext = projectContext;
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
				//CMN.Log("保存配置……", projectContext.key);
				projectContext.currentValue=newVal;
				if(projectContext.bottombar!=null){
					RebuildBottombarIcons(a, projectContext, a.mConfiguration);
				}
				opt.putAppProject(projectContext);
				if(projectContext.type>=0){
					checkReferncedChange(a.contentbar_project, newVal);
					checkReferncedChange(a.peruseview_project, newVal);
				}
			}
			isDirty=false;
		}
	}
	
	private void checkReferncedChange(AppUIProject checkNow, String newVal) {
		if(checkNow!=null && checkNow!=projectContext && opt.isAppContentBarProjectReferTo(checkNow.key, projectContext.type)){
			CMN.Log(checkNow.key,  "refer to >> ", projectContext.key);
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
	
	public void setLongOnClickListener(View...views) {
		for (int i = 0; i < views.length; i++) {
			views[i].setOnClickListener(this);
			views[i].setOnLongClickListener(this);
		}
	}
	
	public void onClick(int pos) {
		onClick(sideBar.getChildAt(pos));
	}
	
	public void show() {
		dialog.show();
		dialog.setCanceledOnTouchOutside(true);
	}
	
	static class ViewHolder{
		ViewHolder(ViewGroup v, BottombarTweakerAdapter biantai){
			itemView=(ViewGroup)LayoutInflater.from(v.getContext()).inflate(R.layout.circle_checker_item, v, false);
			itemView.setTag(this);
			ccb_icon = (CircleCheckBox) itemView.getChildAt(0);
			ccb_icon.drawIconForEmptyState = true;
			ccb_icon.drawInnerForEmptyState=false;
			ccb_icon.noTint=true;
			ccb_icon.setProgress(1);
			ccb_icon.setOnClickListener(biantai);

			ccb_toggle = (CircleCheckBox) itemView.getChildAt(1);
			ccb_toggle.drawIconForEmptyState=false;
			ccb_toggle.circle_shrinkage=0.75f*GlobalOptions.density;
			ccb_toggle.addStateWithDrawable(biantai.switch_landscape);
			ccb_toggle.setOnClickListener(biantai);

			tv = (TextView) itemView.getChildAt(2);
			tv.setOnClickListener(biantai);
		}
		private final ViewGroup itemView;
		public int position;
		CircleCheckBox ccb_icon;
		CircleCheckBox ccb_toggle;
		TextView tv;
	}
}

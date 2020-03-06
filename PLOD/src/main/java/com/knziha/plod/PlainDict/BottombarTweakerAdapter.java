package com.knziha.plod.PlainDict;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.knziha.ankislicer.customviews.ShelfLinearLayout;
import com.knziha.filepicker.widget.CircleCheckBox;
import com.mobeta.android.dslv.DragSortListView;

import static com.knziha.plod.PlainDict.Toastable_Activity.FLASH_DURATION_MS;

class BottombarTweakerAdapter extends BaseAdapter implements View.OnClickListener, DragSortListView.DragSortListener, View.OnLongClickListener {
	private final AlertDialog dialog;
	private final MainActivityUIBase a;
	private final PDICMainAppOptions opt;
	public PDICMainActivity.AppUIProject projectContext;
	public static int StateCount=2;
	public boolean isDirty;

	public BottombarTweakerAdapter(AlertDialog d, MainActivityUIBase _a) {
		dialog = d;
		opt = _a.opt;
		a = _a;
	}

	public String MakeProject(){
		int size = projectContext.iconData.size()-1;
		StringBuilder sb = new StringBuilder(64);
		projectContext.bNeedCheckOrientation=false;
		for (int i = 0; i <= size; i++) {
			PDICMainActivity.IconData icI = projectContext.iconData.get(i);
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
		PDICMainActivity.IconData item = projectContext.iconData.get(position);
		int id = item.number;
		Resources res = parent.getResources();
		vh.tv.setText(projectContext.titles[id]);
		vh.ccb_icon.setDrawable(0, res.getDrawable(projectContext.icons[id]));
		vh.ccb_toggle.setChecked(item.tmpIsFlag,false);
		vh.position = position;
		return vh.itemView;
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()){
			case DialogInterface.BUTTON_POSITIVE:{

			} break;
			case DialogInterface.BUTTON_NEGATIVE:{
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
		switch (v.getId()){
			case R.id.customise_main_bar:{
				if(isDirty){
					checkCurrentProject(v);
					break;
				}
				ShelfLinearLayout sideBar = (ShelfLinearLayout) v.getParent();
				if(a instanceof PDICMainActivity){
					PDICMainActivity aa = (PDICMainActivity) a;
					if(aa.bottombar_project==null)
						aa.bottombar_project = new MainActivityUIBase.AppUIProject("btmprj", aa.BottombarBtnIcons, aa.BottombarBtnIds, opt.getAppBottomBarProject(), aa.bottombar, aa.BottombarBtns);
					if(aa.bottombar_project.iconData==null)
						aa.bottombar_project.instantiate(aa.getResources().getStringArray(R.array.customize_btm));
					projectContext=aa.bottombar_project;
					sideBar.setRbyView(v);
					a.showTopSnack(dialog.findViewById(R.id.snack_holder), "主程序", 0.6f, FLASH_DURATION_MS, -1,true);
					notifyDataSetChanged();
				} else {

				}
			} break;
			case R.id.customise_main_content_bar:{
				if(isDirty){
					checkCurrentProject(v);
					break;
				}
				ShelfLinearLayout sideBar = (ShelfLinearLayout) v.getParent();
				if(a.contentbar_project==null)
					a.contentbar_project = new MainActivityUIBase.AppUIProject(a.cbar_key, a.ContentbarBtnIcons, a.ContentbarBtnIds, opt.getAppContentBarProject(a.cbar_key), a.bottombar2, a.ContentbarBtns);
				if(a.contentbar_project.iconData==null)
					a.contentbar_project.instantiate(a.getResources().getStringArray(R.array.customize_ctn));
				projectContext=a.contentbar_project;
				sideBar.setRbyView(v);
				a.showTopSnack(dialog.findViewById(R.id.snack_holder), "主程序-解释页面", 0.6f, FLASH_DURATION_MS, -1,true);
				notifyDataSetChanged();
			} break;
			case DialogInterface.BUTTON_POSITIVE:{
				if(projectContext==null) return;
				checkCurrentProjectInternal();
				dialog.dismiss();
			} break;
			case DialogInterface.BUTTON_NEGATIVE:{
				clearCurrentProject();
				dialog.dismiss();
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
		if(v!=null && projectContext!=null && !MakeProject().equals(projectContext.currentValue)){
			DialogInterface.OnClickListener ocl = (dialog, which) -> {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					checkCurrentProjectInternal();
					v.performClick();
				} else if (which == DialogInterface.BUTTON_NEGATIVE) {
					clearCurrentProject();
					v.performClick();
				}
				dialog.dismiss();
			};
			new AlertDialog.Builder(a)
				.setTitle("配置已更改，是否应用？")
				.setPositiveButton(R.string.confirm, ocl)
				.setNegativeButton(R.string.no, ocl)
				.setNeutralButton(R.string.cancel, ocl)
				.create().show();
		} else {
			checkCurrentProjectInternal();
		}
	}

	void clearCurrentProject() {
		if(isDirty && projectContext!=null){
			MainActivityUIBase.AppUIProject _projectContext = projectContext;
			projectContext=null;
			_projectContext.clear();
		}
		isDirty=false;
	}

	void checkCurrentProjectInternal() {
		if(isDirty){
			String val = MakeProject();
			boolean bNeedSave = !val.equals(projectContext.currentValue);
			if(bNeedSave){
				projectContext.currentValue=val;
				a.RebuildBottombarIcons(projectContext, a.mConfiguration);
				opt.putAppProject(projectContext);
			}
			isDirty=false;
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
			PDICMainActivity.IconData item = projectContext.iconData.remove(from);
			projectContext.iconData.add(to, item);
			notifyDataSetChanged();
		}
	}
	@Override
	public void remove(int which) {  }

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
			ccb_toggle.addStateWithDrawable(itemView.getResources().getDrawable(R.drawable.ic_screen_rotation_black_24dp).mutate());
			ccb_toggle.drawIconForEmptyState=false;
			ccb_toggle.circle_shrinkage=0.75f*biantai.opt.dm.density;
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

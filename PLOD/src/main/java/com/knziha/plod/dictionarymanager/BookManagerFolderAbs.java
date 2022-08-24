package com.knziha.plod.dictionarymanager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.fragment.app.ListFragment;

import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionarymanager.files.ArrayListBookTree;
import com.knziha.plod.dictionarymanager.files.mAssetFile;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import org.jcodings.constants.PosixBracket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class BookManagerFolderAbs extends ListFragment
		implements BookManagerFragment.SelectableFragment, View.OnClickListener {
	int type=0;
	String parentFile;
	public boolean SelectionMode=false;
	HashSet<mFile> selFolders = new HashSet<>();
	public HashSet<mFile> Selection = new HashSet<mFile>(){
		@Override
		public boolean add(mFile mFile) {
			boolean ret = super.add(mFile);
			if (ret && mFile.getIsDirectory()) {
				selFolders.add(mFile);
			}
			return ret;
		}
		
		@Override
		public boolean removeAll(@NonNull Collection<?> c) {
			return super.removeAll(c);
		}
	};
	
	public int calcSelectionSz() {
		int ret = Selection.size(), sfz=selFolders.size();
		if (sfz>0) {
			if (ret > 0) {
				for (Iterator<mFile> i = selFolders.iterator(); i.hasNext(); ){
					mFile fn = i.next();
					if (Selection.contains(fn)) {
						ret--;
					} else {
						i.remove();
					}
				}
			} else {
				selFolders.clear();
			}
		}
		return ret;
	}
	
	HashSet<mFile> hiddenParents=new HashSet<>();
	
	ArrayListBookTree<mFile> data=new ArrayListBookTree<>();
	ArrayList<mFile> dataTree=new ArrayList<>();
	
	protected ListView mDslv;
	ArrayAdapter<mFile> adapter;
	boolean isDirty = false;
	BookManager a;
	int[] lastClickedPos=new int[]{-1, -1};
	int lastClickedPosIndex=0;
	
	OnEnterSelectionListener oes;
	
	boolean dataPrepared;
	
	public interface OnEnterSelectionListener{
		void onEnterSelection(BookManagerFolderAbs f, boolean enter);
		int addIt(BookManagerFolderAbs f, mFile fn);
	}
	
	public void enterSelectionMode() {
		if (!SelectionMode) {
			SelectionMode = true;
			if (oes!=null) oes.onEnterSelection(this, true);
		}
	}
	
	@Override
	public boolean exitSelectionMode() {
		if(SelectionMode) {
			SelectionMode = false;
			if (Selection.size() > 0) {
				Selection.clear();
				lastClickedPos[0] = -1;
				lastClickedPos[1] = -1;
				if(oes!=null) oes.onEnterSelection(this, false);
			}
			adapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		BookManagerFolderlike.ViewHolder vh = (BookManagerFolderlike.ViewHolder) ViewUtils.getViewHolderInParents(v, BookManagerFolderlike.ViewHolder.class);
		switch (v.getId()) {
			case R.id.folderIcon:
			{
				mFile mdTmp = vh.dataLet;
				int pos = vh.position;
				if(mdTmp.children.size()==0) {
					hiddenParents.add(mdTmp);
					for(int i=pos+1;i<dataTree.size();i++) {
						mFile item = dataTree.get(i);
						if(!mFile.isDirScionOf(item, mdTmp))
							break;
						if(item.isDirectory())
							break;
						mdTmp.children.add(dataTree.remove(i));
						i--;
					}
				} else {
					dataTree.addAll(pos+1, mdTmp.children);
					mdTmp.children.clear();
					hiddenParents.remove(mdTmp);
				}
				adapter.notifyDataSetChanged();
			} break;
			case R.id.drag_handle:
			{
				mFile mdTmp = vh.dataLet;
				if(!mdTmp.isDirectory()) {
					if(oes.addIt(this, mdTmp)==1)
						a.showT("添加成功!");
					else if(oes.addIt(this, mdTmp)==0) // wtf???
						a.showT("已存在");
				}
			} break;
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a = (BookManager) getActivity();
		parentFile=a.opt.lastMdlibPath.getPath();
		// setListview
		mDslv = getListView();
		mDslv.setChoiceMode(mDslv.CHOICE_MODE_MULTIPLE);
		View v = getActivity().getLayoutInflater().inflate(R.layout.pad_five_dp, null);
		mDslv.addHeaderView(v);
		mDslv.setOnItemClickListener((parent, view, position, id) -> {
			if(position>=mDslv.getHeaderViewsCount()) {
				position = position - mDslv.getHeaderViewsCount();
				//mFile p = data.getList().get(position);
				ViewHolder vh = (ViewHolder)view.getTag();
				mFile mdTmp = vh.dataLet;
				if(SelectionMode) {
					int pos = vh.position;
					//CMN.Log(pos+" ?= "+position);
					if(Selection.remove(mdTmp.getRealPath())) {
						if(mdTmp.isDirectory()){
							for(int i=pos+1;i<dataTree.size();i++) {
								mFile item = dataTree.get(i);
								if(!mFile.isDirScionOf(item, mdTmp))
									break;
								if(item.isDirectory())
									break;
								Selection.remove(item.getRealPath());
							}
						}
						for(int i=0;i<mdTmp.children.size();i++) {
							Selection.remove(mdTmp.children.get(i).getRealPath());
						}
					}
					else {
						lastClickedPos[(++lastClickedPosIndex)%2]=position;
						Selection.add(mdTmp.getRealPath());
						if(mdTmp.isDirectory()){
							if (hiddenParents.remove(mdTmp)) {
								dataTree.addAll(pos + 1, mdTmp.children);
								for(int i=0;i<mdTmp.children.size();i++) {
									Selection.add(mdTmp.children.get(i).getRealPath());
								}
								mdTmp.children.clear();
							} else {
								for(int i=pos+1;i<dataTree.size();i++) {
									mFile item = dataTree.get(i);
									if(!mFile.isDirScionOf(item, mdTmp))
										break;
									if(item.isDirectory())
										break;
									Selection.add(item.getRealPath());
								}
							}
						}
					}
//					if(lastClickedPos[lastClickedPosIndex%2]!=position) {
//						lastClickedPos[(++lastClickedPosIndex) % 2] = position;
//					}
					adapter.notifyDataSetChanged();
				}else {
					if(mdTmp.isDirectory()) {
						((ViewHolder)view.getTag()).folderIcon.performClick();
						//adapter.notifyDataSetChanged();
					}
				}
			}
		});
		mDslv.setOnItemLongClickListener((parent, view, position, id) -> {
			if(position>=mDslv.getHeaderViewsCount()) {
				position = position - mDslv.getHeaderViewsCount();
				SelectionMode=true;
				if(oes!=null) oes.onEnterSelection(this, true);
				//Selection.put(adapter.getItem(position).getAbsolutePath());
				mDslv.getOnItemClickListener().onItemClick(parent, view, position+mDslv.getHeaderViewsCount(), id);
				adapter.notifyDataSetChanged();
			}
			return true;
		});
		
		List<mFile> list = data.getList();
		dataTree = new ArrayList<>(list);
		adapter = new MyAdapter(dataTree);
	}
	
	protected class MyAdapter extends ArrayAdapter<mFile> {
		public MyAdapter(List<mFile> mdicts) {
			super(getActivity(), R.layout.dict_manager_dslitem2, R.id.text, mdicts);
		}
		
		public View getView(int pos, View convertView, ViewGroup parent) {
			View v = super.getView(pos, convertView, parent);
			if(v.getTag()==null) {
				//Log.e("新建",""+pos);
				final BookManagerFolderlike.ViewHolder vh=new BookManagerFolderlike.ViewHolder();
				vh.ck = v.findViewById(R.id.ck);
				vh.folderIcon = (v.findViewById(R.id.folderIcon));
				vh.folderIcon.setOnClickListener(BookManagerFolderAbs.this);
				vh.text= v.findViewById(R.id.text);
				vh.splitterIcon= v.findViewById(R.id.splitterIcon);
				vh.drag_handle=v.findViewById(R.id.drag_handle);
				vh.drag_handle.setOnClickListener(BookManagerFolderAbs.this);
				v.setTag(vh);
			}
			BookManagerFolderlike.ViewHolder vh=(BookManagerFolderlike.ViewHolder) v.getTag();
			vh.position = pos;
			final mFile mdTmp = getItem(pos);
			vh.dataLet=mdTmp;
			//vh.position=pos;
			//vh.dataLet=data.getList().get(pos);
			if(SelectionMode) {
				vh.ck.setVisibility(View.VISIBLE);
				if(Selection.contains(mdTmp.getRealPath()))
					vh.ck.setChecked(true);
				else
					vh.ck.setChecked(false);
			} else {
				vh.ck.setChecked(false);
				vh.ck.setVisibility(View.GONE);
			}
			
			//if(mdTmp.cover!=null) {
			//	SpannableStringBuilder ssb = new SpannableStringBuilder("| ").append(mdTmp._Dictionary_fName);
			//	mdTmp.cover.setBounds(0, 0, 50, 50);
			//	ssb.setSpan(new ImageSpan(mdTmp.cover), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			//	((TextView)v.findViewById(R.id.text)).setText(ssb);
			//}else
			String displayName = null;
			if(mdTmp.getClass()==mAssetFile.class)
				displayName = CMN.getAssetName(mdTmp.getAbsolutePath());
			if(mdTmp.webAsset!=null || (displayName!=null) || mdTmp.getIsDirectory() || mdTmp.exists())
				vh.text.setTextColor(GlobalOptions.isDark? Color.WHITE:Color.BLACK);
			else
				vh.text.setTextColor(Color.RED);
			
			if(BookManager.dictQueryWord!=null && mdTmp.getName().toLowerCase().contains(BookManager.dictQueryWord))
				vh.text.setBackgroundResource(R.drawable.xuxian2);
			else
				vh.text.setBackground(null);
			
			if(mdTmp.isDirectory()) {//目录 todo opt
				if (type==0) {
					if (displayName != null) {
						vh.text.setText(displayName);
					} else
						vh.text.setText(mFile.tryDeScion(mdTmp, parentFile));
				} else {
					vh.text.setText(mdTmp.getPath());
				}
				vh.folderIcon.setVisibility(View.VISIBLE);
				vh.drag_handle.setVisibility(View.GONE);
				vh.splitterIcon.setVisibility(View.GONE);
				vh.text.setSingleLine(false);
			} else {//路径
				if(displayName!=null) {
					vh.text.setText(displayName);
				} else if(mFile.isScionOf(mdTmp,parentFile)) {
					vh.text.setPadding((int) (9*GlobalOptions.density), 0, 0, 0);
					vh.text.setText(a.isDebug?mdTmp.getPath(): BU.unwrapMdxName(mdTmp.getName()));//BU.unwrapMdxName(mdTmp.getName())
				} else {
					vh.text.setPadding(0, 0, 0, 0);
					vh.text.setText(mdTmp.getAbsolutePath());
				}
				mFile p = data.get(mdTmp.getParentFile().init(a.opt)); //todo opt
				if(p!=null) {//有父文件夹节点
					vh.text.setPadding(5, 0, 0, 0);
					vh.text.setText(BU.unwrapMdxName(mdTmp.getName()));
					vh.splitterIcon.setVisibility(View.VISIBLE);
				} else {
					//((TextView)v.findViewById(R.id.text)).setPadding((int) (9*getActivity().getResources().getDisplayMetrics().density), 0, 0, 0);
					((View)v.findViewById(R.id.splitterIcon)).setVisibility(View.GONE);
				}
				vh.drag_handle.setVisibility(View.VISIBLE);
				vh.folderIcon.setVisibility(View.GONE);
			}
			//((TextView)v.findViewById(R.id.text)).setTextColor(Color.parseColor("#000000"));
			
			if(GlobalOptions.isDark) {
				v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}
			return v;
		}
	}
	
	public static class ViewHolder{
		int position;
		public mFile dataLet;
		public View folderIcon;
		public View splitterIcon;
		public View drag_handle;
		public TextView text;
		public CheckBox ck;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mDslv = (ListView) inflater.inflate(R.layout.dict_dsl_main2, null);
		mDslv.setDividerHeight(0);
		//mDslv.setOnTouchListener(mController);
		//mDslv.setDragEnabled(dragEnabled);
		//CMN.show("onCreateView");
		return mDslv;
	}
}

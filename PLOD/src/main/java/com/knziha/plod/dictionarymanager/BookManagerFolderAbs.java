package com.knziha.plod.dictionarymanager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
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

import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionarymanager.files.ArrayListBookTree;
import com.knziha.plod.dictionarymanager.files.mAssetFile;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@SuppressLint("ResourceType")
public abstract class BookManagerFolderAbs extends ListFragment
		implements BookManagerFragment.SelectableFragment, View.OnClickListener, PopupMenuHelper.PopupMenuListener {
	int type=0;
	String parentFile;
	public boolean SelectionMode=true;
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
	
	protected View pressedV;
	protected int pressedPos;
	PopupMenuHelper mPopup;
	
	protected void showPopup(View v, View rootV) {
		PopupMenuHelper popupMenu = getPopupMenu();
		int[] vLocationOnScreen = new int[2];
		if (v == null) v = listView;
		v.getLocationOnScreen(vLocationOnScreen);
		popupMenu.showAt(rootV!=null?rootV:v, vLocationOnScreen[0], vLocationOnScreen[1]+v.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
	}
	
	public PopupMenuHelper getPopupMenu() {
		if (mPopup==null) {
			mPopup  = new PopupMenuHelper(getActivity(), null, null);
			mPopup.initLayout(new int[]{
					//R.string.switch_pick
					 R.string.tianjia
					, R.string.addTo
					, R.string.addToPrv
			}, this);
			mPopup.lv.findViewById(R.string.addToPrv).getLayoutParams().height = 0;
		}
		return mPopup;
	}
	
	public mFile[] getElements(boolean useSelection) {
		if (useSelection) {
			return Selection.toArray(new mFile[0]);
		}
		BookManagerFolderlike.ViewHolder vh = (BookManagerFolderlike.ViewHolder) ViewUtils.getViewHolderInParents(pressedV, BookManagerFolderlike.ViewHolder.class);
		return new mFile[]{vh.dataLet.getRealPath()};
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		BookManagerFolderlike.ViewHolder vh = (BookManagerFolderlike.ViewHolder) ViewUtils.getViewHolderInParents(pressedV, BookManagerFolderlike.ViewHolder.class);
		switch (v.getId()) {
			case R.string.switch_pick:{
				vh.ck.performClick();
			} break;
			case R.string.tianjia:{
				//addIt(vh);
				getBookManager().addElementsToF1(this, null, Selection.contains(vh.dataLet.getRealPath()), false, -1, null);
			} break;
			case R.string.addTo:{
				getBookManager().addElementsToF1(this, null, Selection.contains(vh.dataLet.getRealPath()), true, -1, null);
			} break;
			case R.string.addToPrv:{
			
			} break;
		}
		popupMenuHelper.dismiss();
		return true;
	}
	
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
	
	protected ListView listView;
	ArrayAdapter<mFile> adapter;
	boolean isDirty = false;
	BookManager a;
	int[] lastClickedPos=new int[]{-1, -1};
	int lastClickedPosIndex=0;
	
	OnEnterSelectionListener oes;
	String mName;
	boolean dataPrepared;
	
	public String getName() {
		return mName;
	}
	
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
			//SelectionMode = false;
			if (Selection.size() > 0) {
				Selection.clear();
				lastClickedPos[0] = -1;
				lastClickedPos[1] = -1;
				if(oes!=null) oes.onEnterSelection(this, false);
				dataSetChanged(false);
				return true;
			}
		}
		return false;
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		BookManagerFolderlike.ViewHolder vh = (BookManagerFolderlike.ViewHolder) ViewUtils.getViewHolderInParents(v, BookManagerFolderlike.ViewHolder.class);
		switch (v.getId()) {
			case R.id.folderIcon: {
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
				dataSetChanged(true);
			} break;
			case R.id.drag_handle:
			{
				if (PDICMainAppOptions.dictManagerClickPopup1()) {
					pressedV = vh.itemView;
					pressedPos = vh.position;
					showPopup(vh.itemView, null);
				} else {
					addIt(vh);
				}
			} break;
			case R.id.ck: {
				vh.selecting = true;
				listView.getOnItemClickListener().onItemClick(listView, vh.itemView, vh.position+listView.getHeaderViewsCount(), 0);
				vh.selecting = false;
			} break;
		}
	}
	
	private void addIt(BookManagerFolderlike.ViewHolder vh) {
		mFile mdTmp = vh.dataLet;
		if(!mdTmp.isDirectory()) {
			if(oes.addIt(this, mdTmp)==1)
				a.showT("添加成功!");
			else if(oes.addIt(this, mdTmp)==0) // wtf???
				a.showT("已存在");
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a = (BookManager) getActivity();
		parentFile=a.opt.lastMdlibPath.getPath();
		// setListview
		listView = getListView();
		listView.setChoiceMode(listView.CHOICE_MODE_MULTIPLE);
		View v = getActivity().getLayoutInflater().inflate(R.layout.pad_five_dp, null);
		listView.addHeaderView(v);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			if(position>= listView.getHeaderViewsCount()) {
				pressedPos = position = position - listView.getHeaderViewsCount();
				pressedV = view;
				//mFile p = data.getList().get(position);
				ViewHolder vh = (ViewHolder)view.getTag();
				mFile mdTmp = vh.dataLet;
				if(SelectionMode/* && (!PDICMainAppOptions.dictManagerClickPopup() || vh.selecting)*/) {
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
					dataSetChanged(false);
				}
				else {
					if(mdTmp.isDirectory()) {
						((ViewHolder)view.getTag()).folderIcon.performClick();
						//adapter.notifyDataSetChanged();
					}
				}
			}
		});
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			if(position>= listView.getHeaderViewsCount()) {
				pressedPos = position - listView.getHeaderViewsCount();
				pressedV = view;
//				SelectionMode=true;
//				if(oes!=null) oes.onEnterSelection(this, true);
//				//Selection.put(adapter.getItem(position).getAbsolutePath());
//				listView.getOnItemClickListener().onItemClick(parent, view, position+ listView.getHeaderViewsCount(), id);
//				adapter.notifyDataSetChanged();
				showPopup(view, null);
			}
			return true;
		});
		
		List<mFile> list = data.getList();
		dataTree = new ArrayList<>(list);
		adapter = new MyAdapter(dataTree);
		//ViewUtils.restoreListPos(listView, BookManager.framePos[a.fragments.indexOf(this)]);
	}
	
	protected class MyAdapter extends ArrayAdapter<mFile> {
		public MyAdapter(List<mFile> mdicts) {
			super(getActivity(), R.layout.dict_manager_dslitem2, R.id.text, mdicts);
		}
		
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (parent != listView && convertView!=null) {
				if (!(convertView.getTag() instanceof BookManagerFolderlike.ViewHolder)) {
					CMN.debug("他乡异客");
					convertView = null;
				}
			}
			View v = super.getView(pos, convertView, parent);
			BookManagerFolderlike.ViewHolder vh;
			if (v.getTag() == null) {
				//Log.e("新建",""+pos);
				vh = new BookManagerFolderlike.ViewHolder(v, BookManagerFolderAbs.this);
				if(parent != listView) {
					ViewUtils.setVisible(vh.folderIcon, false);
					ViewUtils.setVisible(vh.splitterIcon, false);
					vh.itemView.setBackground(null);
				}
			} else {
				vh = (BookManagerFolderlike.ViewHolder) v.getTag();
			}
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
				vh.tweakCheck();
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
			vh.text.setMaxLines(1);
			
			
			if (parent == listView) {
				if (!TextUtils.isEmpty(query)
						//&& mdTmp.getName().toLowerCase().contains(query)
						&& filtered.get(pos) != null
				)
					vh.text.setBackgroundResource(GlobalOptions.isDark ? R.drawable.xuxian2_d : R.drawable.xuxian2);
				else
					vh.text.setBackground(null);
				if(GlobalOptions.isDark) {
					v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
				}
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
			}
			
			//((TextView)v.findViewById(R.id.text)).setTextColor(Color.parseColor("#000000"));
			
			return v;
		}
	}
	
	public static class ViewHolder{
		public boolean selecting;
		int position;
		public mFile dataLet;
		public View itemView;
		public View folderIcon;
		public View splitterIcon;
		public View drag_handle;
		public TextView text;
		public CheckBox ck;
		boolean isDark;
		public ViewHolder(View v, BookManagerFolderAbs m) {
			ck = v.findViewById(R.id.ck);
			ck.setOnClickListener(m);
			itemView = v;
			folderIcon = (v.findViewById(R.id.folderIcon));
			folderIcon.setOnClickListener(m);
			text=v.findViewById(R.id.text);
			splitterIcon= v.findViewById(R.id.splitterIcon);
			drag_handle=v.findViewById(R.id.drag_handle);
			drag_handle.setOnClickListener(m);
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		listView = (ListView) inflater.inflate(R.layout.dict_dsl_main2, null);
		listView.setDividerHeight(0);
		//mDslv.setOnTouchListener(mController);
		//mDslv.setDragEnabled(dragEnabled);
		//CMN.show("onCreateView");
		return listView;
	}
	
	String query;
	SparseArray<String> filtered = new SparseArray<>();
	SparseArray<mFile[]> filteredHelper = new SparseArray<>();
	
	public void selectFilteredPos(int position) {
		int lstPos = realFilterPos(position);
		int h;
		if(listView.getChildAt(0)!=null) h = listView.getChildAt(0).getHeight() / 4;
		else h = 18;
		listView.setSelectionFromTop(lstPos, listView.getHeight()/2 - h);
	}
	
	private int realFilterPos(int dataPos) {
		mFile[] folders = filteredHelper.get(dataPos);
		for (int i = 0; i < folders.length; i++) {
			if (folders[i].isHidden()) {
				dataPos -= folders[i].children.size();
			}
		}
		return dataPos;
	}
	
	public void dataSetChanged(boolean structChanged) {
		if (structChanged) {
			String query = this.query;
			if (!TextUtils.isEmpty(query)) {
				this.query = "";
				schFilter(query, false);
				if (getBookManager().popup != null) {
					getBookManager().popup.dismiss();
				}
			}
		}
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	public int schFilter(String query, boolean shouldInval) {
		int sz = filtered.size();
		filtered.clear();
		if (!query.equals(this.query)) {
			getBookManager().popupPos[this==getBookManager().f3?2:3] = 0;
			this.query = query;
		}
		if (!TextUtils.isEmpty(query)) {
			mFile item;
			ArrayList<mFile> folders = new ArrayList<>();
			if (adapter != null) {
				for (int i = 0; i < data.size(); i++) {
					item = data.getList().get(i);
					if (item.isDirectory()) {
						folders.add(item);
					} else {
						String name = item.getName();
						int suffixIdx = name.lastIndexOf("."), sch=name.toLowerCase().indexOf(query);
						if (sch>=0 && (suffixIdx==-1 || sch<suffixIdx)) {
							filtered.put(i, name);
							filteredHelper.put(i, folders.toArray(new mFile[0]));
						}
					}
				}
			}
		}
		if (shouldInval && !(sz==0 && filtered.size()==0)) {
			dataSetChanged(false);
		}
		return filtered.size();
	}
	
	public void schPrvNxt(String query, boolean next) {
		try {
			if (!query.equals(this.query)) {
				schFilter(query, true);
				a.schIndicator_setText(filtered);
			}
			if (filtered.size() > 0) {
				View child = ViewUtils.findCenterYChild(listView);
				ViewHolder vh = (ViewHolder) ViewUtils.getViewHolderInParents(child, ViewHolder.class);
				int fvp = (vh==null?0:vh.position), found = -1, lstPos=0;
				for (int i = 0; i < filtered.size(); i++) {
					lstPos = realFilterPos(filtered.keyAt(i));
					if (next) {
						if (lstPos > fvp) {
							found = lstPos;
							break;
						}
					} else {
						if (lstPos < fvp) {
							found = lstPos;
						} else {
							break;
						}
					}
				}
				if (found != -1) {
					ViewUtils.stopScroll(listView, -100, -100);
					listView.setSelectionFromTop(found + listView.getHeaderViewsCount(), listView.getHeight() / 2 - child.getHeight() / 4);
				}
			}
			if ("".equals(query)) {
				listView.setSelectionFromTop(next?adapter.getCount():0, 0);
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	public final BookManager getBookManager() {
		if(a==null) a = ((BookManager) getActivity());
		return a;
	}
	
	public int selected_size() {
		return Selection.size();
	}
}

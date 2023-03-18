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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@SuppressLint({"ResourceType", "NonConstantResourceId"})
public abstract class BookManagerFolderAbs extends ListFragment
		implements BookManagerFragment.SelectableFragment, View.OnClickListener, PopupMenuHelper.PopupMenuListener {
	int type=0;
	String parentFile;
	public boolean SelectionMode=true;
	HashSet<mFile> selFolders = new HashSet<>();
	private final HashSet<String> _realSelection = new HashSet<>();
	public final HashSet<mFile> Selection = new HashSet<mFile>(){
		public boolean add(mFile mFile) {
			boolean ret;
			if (mFile.getIsDirectory()) {
				ret = selFolders.add(mFile);
			} else {
				ret = _realSelection.add(mFile.getPath());
			}
			return ret;
		}
		public void clear() {
			_realSelection.clear();
			selFolders.clear();
			super.clear();
		}
		public int size() {
			return _realSelection.size();
		}
		@NonNull
		@Override
		public <T> T[] toArray(@NonNull T[] a) {
			try {
				return (T[]) toArray();
			} catch (Exception e) {
				return super.toArray(a);
			}
		}
		public Object[] toArray() {
			mFile[] ret = new mFile[_realSelection.size()];
			Iterator<String> iter = _realSelection.iterator();
			for (int i = 0; i < ret.length && iter.hasNext(); i++) {
				ret[i] = new mFile(iter.next());
			}
			CMN.debug("toArray::",  Arrays.toString(ret));
			return ret;
		}
		public boolean contains(@Nullable Object o) {
			if (o instanceof mFile) {
				mFile mFile = (mFile) o;
				return _realSelection.contains(mFile.getPath())
						 || selFolders.contains(mFile);
			}
			return false;
		}
		public boolean remove(@Nullable Object o) {
			if (o instanceof mFile) {
				mFile mFile = (mFile) o;
				boolean ret = _realSelection.remove(mFile.getPath());
				if (ret && mFile.getIsDirectory()) {
					selFolders.add(mFile);
				}
				return ret;
			}
			return false;
		}
	};
	
	protected View pressedV;
	protected int pressedPos;
	PopupMenuHelper mPopup;
	protected HashSet<mFile> topParent = new HashSet<>();
	
	protected void showPopup(View v, View rootV) {
		PopupMenuHelper popupMenu = getPopupMenu();
		int[] vLocationOnScreen = new int[2];
		ListView lv = ((ListView) ViewUtils.getParentByClass(v, ListView.class));
		if (lv!=listView) {
			v = listView;
		}
		v.getLocationOnScreen(vLocationOnScreen);
		popupMenu.showAt(rootV!=null?rootV:v, vLocationOnScreen[0], vLocationOnScreen[1]+v.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
	}
	
	public PopupMenuHelper getPopupMenu() {
		if (mPopup==null) {
			mPopup  = new PopupMenuHelper(getActivity(), null, null);
		}
		mPopup.initLayout(new int[]{
				//R.string.switch_pick
				R.string.tianjia
				, R.string.addTo
				, R.string.addToPrv
				, R.string.copyFileNamace
		}, this);
		mPopup.lv.findViewById(R.string.addToPrv).getLayoutParams().height = 0;
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
			case R.string.copyFileNamace:{
				getBookManager().copyText(vh.dataLet.getName(), true);
			} break;
		}
		popupMenuHelper.dismiss();
		return true;
	}
	
	public int calcSelectionSz() {
		return Selection.size();
	}
	
	HashSet<mFile> hiddenParents=new HashSet<>();
	
	ArrayListBookTree<mFile> data=new ArrayListBookTree<>();
	ArrayList<mFile> dataTree=new ArrayList<>();
	
	protected ListView listView;
	ArrayAdapter<mFile> adapter;
	ArrayAdapter<mFile> adapterAll;
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
	
	public HashSet<String> getSelectedPaths() {
		return new HashSet<>(_realSelection);
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
			if ((Selection.size()+selFolders.size()) > 0) {
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
				toggleFolder(mdTmp, pos);
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
	
	private void toggleFolder(mFile folder, int pos) {
		if (pos == -1) {
			pos = dataTree.indexOf(folder);
			if (pos == -1) return;
		}
		if(folder.children.size()==0) {
			hiddenParents.add(folder);
			for(int i=pos+1;i<dataTree.size();i++) {
				mFile item = dataTree.get(i);
				if(!mFile.isDirectChildrenOf(item, folder))
					break;
				if(item.isDirectory())
					break;
				folder.children.add(dataTree.remove(i));
				i--;
			}
		} else {
			dataTree.addAll(pos+1, folder.children);
			folder.children.clear();
			hiddenParents.remove(folder);
		}
		dataSetChanged(true);
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
		listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		View v = getActivity().getLayoutInflater().inflate(R.layout.pad_five_dp, null);
		listView.addHeaderView(v);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			if(position>= listView.getHeaderViewsCount()) {
				pressedPos = position = position - listView.getHeaderViewsCount();
				pressedV = view;
				//mFile p = data.getList().get(position);
				ViewHolder vh = (ViewHolder)view.getTag();
				mFile filelet = vh.dataLet;
				if(SelectionMode && (!filelet.isDirectory() || vh.selecting)
					/* && (!PDICMainAppOptions.dictManagerClickPopup() || vh.selecting)*/) {
					int pos = vh.position;
					//CMN.Log(pos+" ?= "+position);
					if(Selection.remove(filelet.getRealPath())) {
						if(filelet.isDirectory()){
							for(int i=pos+1;i<dataTree.size();i++) {
								mFile item = dataTree.get(i);
								if(!mFile.isDirectChildrenOf(item, filelet))
									break;
								if(item.isDirectory())
									break;
								Selection.remove(item.getRealPath());
							}
						}
						for(int i=0;i<filelet.children.size();i++) {
							Selection.remove(filelet.children.get(i).getRealPath());
						}
					}
					else {
						lastClickedPos[(++lastClickedPosIndex)%2]=position;
						Selection.add(filelet.getRealPath());
						if(filelet.isDirectory()){
							if (hiddenParents.remove(filelet)) {
								dataTree.addAll(pos + 1, filelet.children);
								for(int i=0;i<filelet.children.size();i++) {
									Selection.add(filelet.children.get(i).getRealPath());
								}
								filelet.children.clear();
							} else {
								for(int i=pos+1;i<dataTree.size();i++) {
									mFile item = dataTree.get(i);
									if(!mFile.isDirectChildrenOf(item, filelet))
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
					if(filelet.isDirectory()) {
						((ViewHolder)view.getTag()).folderIcon.performClick();
						//adapter.notifyDataSetChanged();
					}
				}
			}
		});
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			if(position>= listView.getHeaderViewsCount()) {
//				SelectionMode=true;
//				if(oes!=null) oes.onEnterSelection(this, true);
//				//Selection.put(adapter.getItem(position).getAbsolutePath());
//				listView.getOnItemClickListener().onItemClick(parent, view, position+ listView.getHeaderViewsCount(), id);
//				adapter.notifyDataSetChanged();
				ViewHolder vh = (ViewHolder)view.getTag();
				mFile filelet = vh.dataLet;
				if (!filelet.isDirectory()) {
					pressedPos = position - listView.getHeaderViewsCount();
					pressedV = view;
					showPopup(view, null);
				}
			}
			return true;
		});
		
		List<mFile> list = data.getList();
		dataTree = new ArrayList<>(list);
		adapter = new MyAdapter(dataTree);
		adapterAll = new MyAdapter(list);
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
			// todo fix crash, after del two books?
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
			final mFile lstFile = getItem(pos);
			vh.dataLet=lstFile;
			//vh.position=pos;
			//vh.dataLet=data.getList().get(pos);
			if(SelectionMode) {
				vh.ck.setVisibility(View.VISIBLE);
				if(Selection.contains(lstFile.getRealPath()))
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
			if(lstFile.getClass()==mAssetFile.class)
				displayName = CMN.getAssetName(lstFile.getAbsolutePath());
			if(lstFile.webAsset!=null || (displayName!=null) || lstFile.getIsDirectory() || lstFile.exists())
				vh.text.setTextColor(GlobalOptions.isDark? Color.WHITE:Color.BLACK);
			else
				vh.text.setTextColor(Color.RED);
			vh.text.setMaxLines(1);
			
			if (parent == listView) { // if for fragment itself
				if (!TextUtils.isEmpty(query) && filteredFilesHelper.get(data.indexOf(lstFile)) == lstFile)
					vh.text.setBackgroundResource(GlobalOptions.isDark ? R.drawable.xuxian2_d : R.drawable.xuxian2);
				else
					vh.text.setBackground(null);
				if (GlobalOptions.isDark) {
					v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
				}
				int leftPadding = 0;
				String text;
				if (lstFile.isDirectory()) {//目录 todo opt
					if (type == 0) {
						if (displayName != null)
							text = displayName;
						else
							text = mFile.removeFolderPrefix(lstFile, parentFile);
					} else {
						text = lstFile.getPath();
					}
					vh.folderIcon.setVisibility(View.VISIBLE);
					vh.drag_handle.setVisibility(View.GONE);
					vh.splitterIcon.setVisibility(View.GONE);
					vh.text.setSingleLine(false);
				} else {//文件
					if (displayName != null) {
						text = displayName;
					} else if (mFile.isChildrenOf(lstFile, parentFile)) {
						leftPadding = (int) (9 * GlobalOptions.density);
						text = a.isDebug ? lstFile.getPath() : BU.removeMdxSuffix(lstFile.getName());
					} else {
						text = lstFile.getAbsolutePath();
					}
					mFile p = getParentFolderInData(lstFile);
					if (p != null && !topParent.contains(p)) {//有父文件夹节点
						leftPadding = 5;
						text = BU.removeMdxSuffix(lstFile.getName());
						vh.splitterIcon.setVisibility(View.VISIBLE);
						// debug path
						//vh.text.setSingleLine(false); vh.text.setMaxLines(9);
						//text = BU.unwrapMdxName(mdTmp.getName())+"\n"+p;
					} else {
						//leftPadding = (int) (9 * GlobalOptions.density);
						v.findViewById(R.id.splitterIcon).setVisibility(View.GONE);
					}
					if (a.isDebug) {
						vh.text.setSingleLine(false); vh.text.setMaxLines(9);
						text = lstFile.getPath();
					}
					vh.text.setEllipsize( TextUtils.TruncateAt.MIDDLE );
					vh.drag_handle.setVisibility(View.VISIBLE);
					vh.folderIcon.setVisibility(View.GONE);
				}
				if (leftPadding != vh.text.getPaddingLeft()) {
					int padTop = (int) (5 * GlobalOptions.density);
					vh.text.setPadding(leftPadding, padTop, 0, padTop);
				}
				PDICMainAppOptions opt = getBookManager().opt;
				if (text.startsWith(opt.lastMdlibPath.getPath())) {
					text = text.substring(opt.lastMdlibPath.getPath().length()+1);
				}
				if (text.startsWith(GlobalOptions.extPath)) {
					text = "/sdcard" + text.substring(GlobalOptions.extPath.length());
				}
				vh.text.setText( text );
			} else { // if for searched list
				vh.text.setText(lstFile.getName());
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
				isDark = !isDark;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					ck.getButtonDrawable().setColorFilter(isDark?GlobalOptions.NEGATIVE_1:null);
				}
			}
		}
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
	SparseArray<mFile> filteredFilesHelper = new SparseArray<>();
	
	public void selectFilteredPos(int position) {
		mFile file = filteredFilesHelper.get(position);
		mFile parent = getParentFolderInData(file);
		if (parent!=null && parent.children.size()>0) {
			toggleFolder(parent, -1);
		}
		int lstPos = data.indexOf(dataTree, file);
		//int lstPos = realFilterPos(position);
		int h;
		if(listView.getChildAt(0)!=null) h = listView.getChildAt(0).getHeight() / 4;
		else h = 18;
		listView.setSelectionFromTop(lstPos, listView.getHeight()/2 - h);
	}
	
	private int realFilterPos(int dataPos) {
		mFile file = filteredFilesHelper.get(dataPos);
		mFile parent = getParentFolderInData(file);
		if (parent!=null && parent.children.size()>0) {
			return data.indexOf(dataTree, parent);
		}
		return dataTree.indexOf(file);
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
	
	public void rebuildDataTree() {
		ArrayList<mFile> rawData = data.getList();
		mFile fileAt, hidden_child; List<mFile> hidden;
		dataTree = new ArrayList<>(rawData);
		for (int i = rawData.size()-1; i >= 0; i--) {
			fileAt = rawData.get(i);
			hidden = fileAt.children;
			if (hidden.size() > 0) {
				final ArrayList<mFile> children = new ArrayList<>();
				for (int j = 0; j < hidden.size(); j++) {
					hidden_child = hidden.get(j);
					int idx = data.indexOf(dataTree, hidden_child);
					if (idx >= 0) {
						dataTree.remove(idx);
						children.add(hidden_child);
					}
				}
				fileAt.children = children;
			}
		}
		adapter = new MyAdapter(dataTree);
		long savedPos = ViewUtils.saveListPos(listView);
		super.setListAdapter(adapter);
		ViewUtils.restoreListPos(listView, savedPos);
	}
	
	public int schFilter(String query, boolean shouldInval) {
		int sz = filtered.size();
		filtered.clear();
		filteredFilesHelper.clear();
		if (!query.equals(this.query)) {
			getBookManager().popupPos[this==getBookManager().f3?2:3] = 0;
			this.query = query;
		}
		if (!TextUtils.isEmpty(query)) {
			mFile item;
			if (adapter != null) {
				for (int i = 0; i < data.size(); i++) {
					item = data.getList().get(i);
					if (!item.isDirectory()) { // if file
						String name = item.getName();
						int suffixIdx = name.lastIndexOf("."), sch=name.toLowerCase().indexOf(query);
						if (sch>=0 && (suffixIdx==-1 || sch<suffixIdx)) // if match
						{
							filtered.put(i, name);
							filteredFilesHelper.put(i, item);
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
	
	private mFile getParentFolderInData(mFile file) {
		file = file.getParentFile();
		if(file==null) return file;
		return data.get(file.init(a.opt)); //todo opt
	}
	
	public boolean removeFileInData(mFile file) {
		BookManager bm = getBookManager();
		int idx = data.indexOf(file.init(bm.opt));
		return removeFileInDataAt(idx, false);
	}
	
	public boolean removeFileInDataAt(int i, boolean delOther) {
		final ArrayList<mFile> lstViewFiles = data.getList();
		boolean anotherChanged = false;
		if (i<0 || i>=lstViewFiles.size()) {
			return anotherChanged;
		}
		final mFile fn = lstViewFiles.get(i)/*.getRealPath()*/;
		lstViewFiles.remove(i);
		isDirty = true;
		mFile p = fn.getParentFile(); // check for folder
		BookManager bm = getBookManager();
		BookManagerFolderAbs another_folderLike = delOther ? (this == bm.f3 ? bm.f4 : bm.f3) : null;
		if(another_folderLike!=null)
			anotherChanged |= another_folderLike.removeFileInData(fn);
		else
			anotherChanged = true;
		if (p != null) {
			mFile prev = i-1 >= 0 ? lstViewFiles.get(i-1) : null;
			if (prev!=null && p.getPath().equals(prev.getPath()) // found
					&& prev.children.size()==0 // no other children
					&& (  i>=lstViewFiles.size() || !mFile.isDirectChildrenOf(lstViewFiles.get(i), p))  ) {
				lstViewFiles.remove(--i);
			}
		}
		return anotherChanged;
	}
}

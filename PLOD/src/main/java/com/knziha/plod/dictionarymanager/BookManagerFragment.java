package com.knziha.plod.dictionarymanager;

import com.knziha.plod.PlainUI.PopupMenuHelper;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import android.os.Bundle;

import androidx.fragment.app.ListFragment;

import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.HashSet;


public abstract class BookManagerFragment<T> extends ListFragment {
	HashSet<String> selector = new HashSet<>();
	public interface SelectableFragment{
		boolean exitSelectionMode();
	}
	public CompoundButton.OnCheckedChangeListener checkChanged;
	int[] lastClickedPos=new int[]{-1, -1};
	int lastClickedPosIndex=0;
	protected View pressedV;
	protected int pressedPos;
	
	PopupMenuHelper mPopup;
	
	public abstract PopupMenuHelper getPopupMenu();
	
	protected void showPopup(View v) {
		PopupMenuHelper popupMenu = getPopupMenu();
		int[] vLocationOnScreen = new int[2];
		if (v == null) v = listView;
		v.getLocationOnScreen(vLocationOnScreen);
		popupMenu.showAt(v, vLocationOnScreen[0], vLocationOnScreen[1]+v.getHeight()/2, Gravity.TOP|Gravity.CENTER_HORIZONTAL);
	}
	
    ArrayAdapter<T> adapter;
    boolean isDirty = false;
	BookManager a;
    private DragSortListView.RemoveListener onRemove = 
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                    adapter.remove(adapter.getItem(which));
                }
            };

    protected int getLayout() {
        // this DSLV xml declaration does not call for the use
        // of the default DragSortController; therefore,
        // DSLVFragment has a buildController() method.
        return R.layout.dict_dsl_main;
    }
    
    /**
     * Return list item layout resource passed to the ArrayAdapter.
     */
    protected int getItemLayout() {
        /*if (removeMode == DragSortController.FLING_LEFT_REMOVE || removeMode == DragSortController.SLIDE_LEFT_REMOVE) {
            return R.layout.list_item_handle_right;
        } else */

            return 1;

    }

    protected DragSortListView listView;
    private DragSortController mController;

    public int dragStartMode = DragSortController.ON_DOWN;
    public boolean removeEnabled = false;
    public int removeMode = DragSortController.FLING_REMOVE;
    public boolean sortEnabled = true;
    public boolean dragEnabled = true;

    public DragSortController getController() {
        return mController;
    }

    /**
     * Called from DSLVFragment.onActivityCreated(). Override to
     * set a different adapter.
     */
    public void setListAdapter() {

    }

    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv) {
        // defaults are
        //   dragStartMode = onDown
        //   removeMode = flingRight
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setClickRemoveId(R.id.click_remove);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        return controller;
    }


    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        listView = (DragSortListView) inflater.inflate(getLayout(), null);
        
        mController = buildController(listView);
        listView.setFloatViewManager(mController);
        listView.setOnTouchListener(mController);
        listView.setDragEnabled(dragEnabled);
        //CMN.show("onCreateView");
        return listView;
    }
	
	PDICMainAppOptions opt;
	
	protected PDICMainAppOptions getOpt() {
		if (opt == null) {
			if (getActivity() != null) {
				opt = getBookManager().opt;
			}
			opt = new PDICMainAppOptions(getContext());
		}
		return opt;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //CMN.show("onActivityCreated");
        listView = (DragSortListView) getListView();

        listView.setDropListener(getDropListener());
        listView.setRemoveListener(onRemove);

        View v = getActivity().getLayoutInflater().inflate(R.layout.pad_five_dp, null);
        listView.addHeaderView(v);
	
		opt = getBookManager().opt;
    }

    abstract DragSortListView.DropListener getDropListener();
	
	public void dataSetChanged() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	public final BookManager getBookManager() {
		return ((BookManager) getActivity());
	}
	
	String query;
	SparseArray<String> filtered = new SparseArray<>();
	
	public abstract int schFilter(String query);
	
	public void schPrvNxt(String query, boolean next) {
		try {
			if (!query.equals(this.query)) {
				schFilter(query);
			}
			if (filtered.size() > 0) {
				View child = ViewUtils.findCenterYChild(listView);
				BookManagerMain.ViewHolder  vh = (BookManagerMain.ViewHolder) ViewUtils.getViewHolderInParents(child, BookManagerFolderAbs.ViewHolder.class);
				int fvp = (vh==null?0:vh.position), found = -1;
				if (next) {
					for (int i = 0; i < filtered.size(); i++) {
						if (filtered.keyAt(i) > fvp) {
							found = i;
							break;
						}
					}
					if (found != -1) {
						ViewUtils.stopScroll(listView, -100, -100);
						listView.setSelectionFromTop(filtered.keyAt(found)+ listView.getHeaderViewsCount(), listView.getHeight()/2-child.getHeight()/4);
					}
				} else {
					for (int i = 0; i < filtered.size(); i++) {
						if (filtered.keyAt(i) < fvp) {
							found = i;
						} else {
							break;
						}
					}
					if (found != -1) {
						ViewUtils.stopScroll(listView, -100, -100);
						getListView().setSelectionFromTop(filtered.keyAt(found)+listView.getHeaderViewsCount(), listView.getHeight()/2-child.getHeight()/4);
					}
				}
			}
			if ("".equals(query)) {
				listView.setSelectionFromTop(next?adapter.getCount():0, 0);
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
}

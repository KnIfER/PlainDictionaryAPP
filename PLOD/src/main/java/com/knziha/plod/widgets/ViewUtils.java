package com.knziha.plod.widgets;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import androidx.databinding.ViewDataBinding;
import androidx.preference.Preference;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.Toastable_Activity;

import org.knziha.metaline.StripMethods;

@StripMethods(strip=!BuildConfig.isDebug, keys={"setWebDebug"})
public class ViewUtils {
	@StripMethods(strip = !BuildConfig.isDebug)
	public static void setWebDebug(Toastable_Activity a) {
		WebView.setWebContentsDebuggingEnabled(true);
		a.showT("调试网页！");
	}
	
	public static class ViewDataHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder{
		public T data;
		public long position;
		public Object tag;
		public ViewDataHolder(T data){
			super(data.getRoot());
			itemView.setTag(this);
			this.data = data;
		}
	}
	
    /**
     * Get center child in X Axes
     */
    public static View getCenterXChild(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterX(recyclerView, child)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Get position of center child in X Axes
     */
    public static int getCenterXChildPosition(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterX(recyclerView, child)) {
                    return recyclerView.getChildAdapterPosition(child);
                }
            }
        }
        return ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    }
    
    /**
     * Get center child in Y Axes
     */
    public static View getCenterYChild(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterY(recyclerView, child)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Get position of center child in Y Axes
     */
    public static int getCenterYChildPosition(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (isChildInCenterY(recyclerView, child)) {
                    return recyclerView.getChildAdapterPosition(child);
                }
            }
        }
        return childCount;
    }
	
    public static boolean isChildInCenterX(RecyclerView recyclerView, View view) {
        int childCount = recyclerView.getChildCount();
        int[] lvLocationOnScreen = new int[2];
        int[] vLocationOnScreen = new int[2];
        recyclerView.getLocationOnScreen(lvLocationOnScreen);
        int middleX = lvLocationOnScreen[0] + recyclerView.getWidth() / 2;
        if (childCount > 0) {
            view.getLocationOnScreen(vLocationOnScreen);
            if (vLocationOnScreen[0] <= middleX && vLocationOnScreen[0] + view.getWidth() >= middleX) {
                return true;
            }
        }
        return false;
    }

    public static boolean isChildInCenterY(RecyclerView recyclerView, View view) {
        int childCount = recyclerView.getChildCount();
        int[] lvLocationOnScreen = new int[2];
        int[] vLocationOnScreen = new int[2];
        recyclerView.getLocationOnScreen(lvLocationOnScreen);
        int middleY = lvLocationOnScreen[1] + recyclerView.getHeight() / 2;
        if (childCount > 0) {
            view.getLocationOnScreen(vLocationOnScreen);
            if (vLocationOnScreen[1] <= middleY && vLocationOnScreen[1] + view.getHeight() >= middleY) {
                return true;
            }
        }
        return false;
    }
	
	public static void notifyAPPSettingsChanged(Activity activity, Preference preference) {
    	if (activity instanceof APPSettingsActivity) {
			((APPSettingsActivity) activity).notifyChanged(preference);
		}
	}
	
	public static void notifyDataSetChanged(ListAdapter adapter) {
		if (adapter instanceof BaseAdapter) {
			((BaseAdapter) adapter).notifyDataSetChanged();
		} else if (adapter instanceof WrapperListAdapter) {
			notifyDataSetChanged(((WrapperListAdapter) adapter).getWrappedAdapter());
		}
	}
	
	public static void addOnLayoutChangeListener(View view, View.OnLayoutChangeListener layoutChangeListener) {
		if (layoutChangeListener!=null)
			view.addOnLayoutChangeListener(layoutChangeListener);
	}
}
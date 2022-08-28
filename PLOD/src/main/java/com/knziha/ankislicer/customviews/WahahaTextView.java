package com.knziha.ankislicer.customviews;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.EditTextmy;

public class WahahaTextView extends TextView  implements MenuItem.OnMenuItemClickListener{
	private boolean bIsActionMenuShown;
	private callbackme callmeback;
	
	public static class ViewRootHolder{
		public View view;
	}
	
	public ViewRootHolder mR;

	public WahahaTextView(Context context) {
		this(context, null);
	}
	public WahahaTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WahahaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
    public View getRootView() {
		return mR != null && mR.view != null ? mR.view : super.getRootView();
    }

//	@RequiresApi(api = Build.VERSION_CODES.M)
//	@Override
//	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
//		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
//			if (callmeback == null) callmeback = new callbackme();
//			ActionMode mode = super.startActionMode(callmeback.callhere(callback), type);
//			mode.getMenu().clear();
//			return mode;
//		}
//		return super.startActionMode(callback, type);
//	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}


	@RequiresApi(api = Build.VERSION_CODES.M)
	private class callbackme extends ActionMode.Callback2 implements OnLongClickListener{
		ActionMode.Callback callback;
		public WahahaTextView.callbackme callhere(ActionMode.Callback callher) {
			if(callher!=null)
				callback=callher;
			return this;
		}
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			return bIsActionMenuShown=callback.onCreateActionMode(mode, menu);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return callback.onPrepareActionMode(mode, menu);
		}

		@Override
		public boolean onLongClick(View v) {
			switch(v.getId()) {
				case R.id.toolbar_action0: {
					//PopupDecorView s;
				}
				break;
			}
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return WahahaTextView.this.onMenuItemClick(item);
		}


		PopupWindow mPopup;
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			bIsActionMenuShown=false;
			//CMN.Log("onDestroyActionMode");
		}

		@Override
		public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
			if(ActionMode.Callback2.class.isInstance(callback))
				((ActionMode.Callback2)callback).onGetContentRect(mode, view, outRect);
			else
				super.onGetContentRect(mode, view, outRect);
			//CMN.Log("onGetContentRect", (view==wahahaTextView.this));
		}
	}
	
	@Override
	public TextPaint getPaint() {
		if(PDICMainAppOptions.getHackDisableMagnifier()){
			if(EditTextmy.hackTp==null){
				EditTextmy.hackTp = new TextPaint();
				EditTextmy.hackTp.setTextSize(1000);
			};
			return EditTextmy.hackTp;
		}
		return super.getPaint();
	}
}

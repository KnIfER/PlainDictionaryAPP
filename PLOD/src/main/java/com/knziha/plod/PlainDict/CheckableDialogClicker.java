package com.knziha.plod.PlainDict;

import android.view.View;
import android.widget.CheckedTextView;

class CheckableDialogClicker implements View.OnClickListener {
	private final PDICMainAppOptions opt;

	public CheckableDialogClicker(PDICMainAppOptions _opt) {
		opt=_opt;
	}

	@Override
	public void onClick(View v) {
		if(v instanceof CheckedTextView) {
			boolean PeruseIncharge = v.getTag(R.id.position) != null;
			CheckedTextView cb = (CheckedTextView) v;
			cb.toggle();
			boolean val = cb.isChecked();
			switch (cb.getId()){
				case R.string.backkey_web_goback:
					if(PeruseIncharge)
						opt.setUseBackKeyGoWebViewBack1(val);
					else
						opt.setUseBackKeyGoWebViewBack(val);
				break;
				case R.string.webscroll_apply_all:
					opt.setScrollTypeApplyToAll(val);
				break;
			}
		}
	}
}

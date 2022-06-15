package com.knziha.plod.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.knziha.plod.dictionary.Utils.Bag;
import com.knziha.plod.dictionary.Utils.F1ag;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;

/**
 * Created by KnIfER on 2018/4/20.
 */
public class EditTextmy extends EditText {
	public Bag bNeverBlink;
    public EditTextmy(Context context) {
        super(context);
    }

    public EditTextmy(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextmy(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return isEnabled();
    }
	
	public static TextPaint hackTp;
	@Override
	public TextPaint getPaint() {
    	if(PDICMainAppOptions.getEtSearchNoMagnifier()){
			if(hackTp==null){
				hackTp = new TextPaint();
				hackTp.setTextSize(1000);
			};
			return hackTp;
		}
		return super.getPaint();
	}
	
	
	@Override
	public Editable getText() {
		Editable ret = super.getText();
		if (ret==null) {
			ret = new SpannableStringBuilder("");
		}
		return ret;
	}
	
	@Override
	public boolean postDelayed(Runnable action, long delayMillis) {
		//CMN.debug("postDelayed", action);
		if (bNeverBlink!=null && bNeverBlink.val) {
			String name = action.getClass().getName();
			if (name.contains("Blink") || name.contains("Float")) {
				return false;
			}
		}
		return super.postDelayed(action, delayMillis);
	}
}

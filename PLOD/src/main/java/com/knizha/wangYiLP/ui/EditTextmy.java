package com.knizha.wangYiLP.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * Created by KnIfER on 2018/4/20.
 * SOLVE: ET  is keeping intercepting touch events,even it is disabled.
 * when a ET is disabled,it is expected to behave the same way as a TV
 */

public class EditTextmy extends androidx.appcompat.widget.AppCompatEditText{


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
    
 
    
}

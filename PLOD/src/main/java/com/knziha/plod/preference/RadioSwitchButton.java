package com.knziha.plod.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.knziha.plod.plaindict.R;

public class RadioSwitchButton extends CompoundButton {
    
    public RadioSwitchButton(Context context) {
        this(context, null);
    }
    
    public RadioSwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.radioButtonStyle);
    }

    public RadioSwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * {@inheritDoc}
     * <p>
     * If the radio button is already checked, this method will not toggle the radio button.
     */
    @Override
    public void toggle() {
		super.toggle();
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return RadioSwitchButton.class.getName();
    }
	
	@Override
	public void setActivated(boolean activated) {
		super.setChecked(activated);
	}
}

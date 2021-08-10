package com.knziha.plod.plaindict;

import android.text.style.ClickableSpan;
import android.view.View;

public interface OptionProcessor{
	void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val);
	PDICMainAppOptions getOpt();
}
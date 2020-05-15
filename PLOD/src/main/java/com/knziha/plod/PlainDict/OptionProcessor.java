package com.knziha.plod.PlainDict;

import android.text.style.ClickableSpan;
import android.view.View;

public interface OptionProcessor{
	void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val);
	PDICMainAppOptions getOpt();
}
package com.knziha.plod.widgets;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public class SimpleClickableSpan extends ClickableSpan {
	int color;
	public SimpleClickableSpan(int _color) {
		color=_color;
	}

	@Override
	public void onClick(@NonNull View widget) {

	}

	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setColor(color);
	}
}

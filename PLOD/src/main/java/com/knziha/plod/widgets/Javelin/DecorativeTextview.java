/** licence : GPL3 + 四绝协议 */
package com.knziha.plod.widgets.Javelin;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Spanned;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.knziha.ankislicer.customviews.WahahaTextView;
import com.knziha.plod.plaindict.CMN;

public class DecorativeTextview extends WahahaTextView {
	public TextViewDecorator textDecorator = new TextViewDecorator("", "");
	public DecorativeTextview(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (getText() instanceof Spanned && getLayout() != null) {
			try {
				textDecorator.draw(canvas, (Spanned) getText(), getLayout());
			} catch (Exception e) {
				CMN.debug(getText(), e);
			}
		}
		super.onDraw(canvas);
	}
}

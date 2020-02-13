package com.knziha.plod.widgets;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class XYTouchRecorder implements View.OnTouchListener, View.OnClickListener {
	public float x0;
	public float y0;
	public float x;
	public float y;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			x0=event.getX();
			y0=event.getY();
		}
		x=event.getX();
		y=event.getY();
		return false;
	}

	public double distance() {
		return Math.sqrt((x0-x)*(x0-x)+(y0-y)*(y0-y));
	}

	@Override
	public void onClick(View v) {
		if(distance()<35*v.getResources().getDisplayMetrics().density) {
			TextView widget = (TextView) v;
			CharSequence text = widget.getText();
			//CMN.Log("onClickonClick", text instanceof Spannable);
			if(text instanceof Spannable) {
				Spannable span = (Spannable) text;
				int x = (int) this.x;
				int y = (int) this.y;

				x -= widget.getTotalPaddingLeft();
				y -= widget.getTotalPaddingTop();

				x += widget.getScrollX();
				y += widget.getScrollY();

				Layout layout = widget.getLayout();
				int line = layout.getLineForVertical(y);
				int off = layout.getOffsetForHorizontal(line, x);
				ClickableSpan[] link = span.getSpans(off, off, ClickableSpan.class);
				if (link.length > 0) {
					link[0].onClick(widget);
					Selection.setSelection(span,
							span.getSpanStart(link[0]),
							span.getSpanEnd(link[0]));
				}
			}
		}
	}
}

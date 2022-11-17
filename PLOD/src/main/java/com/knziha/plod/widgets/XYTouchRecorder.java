package com.knziha.plod.widgets;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.knziha.plod.plaindict.CMN;

public class XYTouchRecorder implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
	public float x0;
	public float y0;
	public float x;
	public float y;
	public float scrollY;
	public float scrollX;
	public ClickableSpan span;
	public View.OnLongClickListener longClick;
	public SpanInterceptor clickInterceptor;
	
	public interface SpanInterceptor{
		boolean onClick(TextView view, ClickableSpan span);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			CMN.debug("down!!!");
			x0=event.getX();
			y0=event.getY();
			scrollX = v.getScrollX();
			scrollY = v.getScrollY();
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
		ClickableSpan touching = getTouchingSpan(v);
		if (touching!=null) {
			TextView widget = (TextView) v;
			Spannable span = (Spannable) widget.getText();
			if (clickInterceptor != null && clickInterceptor.onClick(widget, touching)) {
				// intentionally blank
			} else {
				touching.onClick(v);
			}
			Selection.setSelection(span,
					span.getSpanStart(touching),
					span.getSpanEnd(touching));
		}
	}
	
	public ClickableSpan getTouchingSpan(View v) {
		if(distance()<35*v.getResources().getDisplayMetrics().density
				&& scrollX == v.getScrollX() && scrollY == v.getScrollY()) {
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
				if (layout != null) {
					int line = layout.getLineForVertical(y);
					int off = layout.getOffsetForHorizontal(line, x);
					ClickableSpan[] link = span.getSpans(off, off, ClickableSpan.class);
					if (link.length > 0) {
						return link[0];
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean onLongClick(View v) {
		ClickableSpan touching = getTouchingSpan(v);
		Spannable span = (Spannable) ((TextView) v).getText();
		if (touching!=null) {
			Selection.setSelection(span,
					span.getSpanStart(touching),
					span.getSpanEnd(touching));
			this.span = touching;
			if(longClick.onLongClick(v)) {
				return true;
			}
			this.span = null;
		}
		return false;
	}
}

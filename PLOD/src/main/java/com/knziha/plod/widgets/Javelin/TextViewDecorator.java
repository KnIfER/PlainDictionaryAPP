/** licence : GPL3 + 四绝协议 */
package com.knziha.plod.widgets.Javelin;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Annotation;
import android.text.Layout;
import android.text.Spanned;


public class TextViewDecorator extends Annotation{
	public int     horizontalPadding;
	public int     verticalPadding;
	public Drawable drawable;
	public Drawable drawableLeft;
	public Drawable drawableMid;
	public Drawable drawableRight;
	
	public Paint paintUnderline = new Paint();
	public int type;
	public float thickness;
	public float lineOffset;
	
	public TextViewDecorator(String key, String value) {
		super(key, value);
	}
	
	protected int getLineTop(Layout layout, int line) {
		return layout.getLineTop(line) - verticalPadding; // getLineTopWithoutPadding
	}
	
	protected int getLineBottom(Layout layout, int line) {
		return layout.getLineBottom(line) + verticalPadding; // WithoutPadding
	}
	
	void drawSingleLine(Canvas canvas, Layout layout, int startLine, int endLine, int startOffset, int endOffset) {
		int lineTop = getLineTop(layout, startLine);
		int lineBottom = getLineBottom(layout, startLine);
		// get min of start/end for left, and max of start/end for right since we don't
		// the language direction
		int left = Math.min(startOffset, endOffset);
		int right = Math.max(startOffset, endOffset);
		if (drawable!=null) {
			drawable.setBounds(left, lineTop, right, lineBottom);
			drawable.draw(canvas);
		}
		if ((type & 2)!=0) {
			canvas.drawRect(left, lineBottom-thickness-lineOffset, right, lineBottom-lineOffset, paintUnderline);
		}
	}
	
	private void drawStart(Canvas canvas, int start, int top, int end, int bottom) {
		if (drawable != null) {
			if (start > end) {
				drawableRight.setBounds(end, top, start, bottom);
				drawableRight.draw(canvas);
			} else {
				drawableLeft.setBounds(start, top, end, bottom);
				drawableLeft.draw(canvas);
			}
		} else if ((type & 2)!=0) {
			if (start > end) {
				canvas.drawRect(end, bottom-thickness-lineOffset, start, bottom-lineOffset, paintUnderline);
			} else {
				canvas.drawRect(start, bottom-thickness-lineOffset, end, bottom-lineOffset, paintUnderline);
			}
		}
	}
	
	private void drawEnd(Canvas canvas, int start, int top, int end, int bottom) {
		if (drawable != null) {
			if (start > end) {
				drawableLeft.setBounds(end, top, start, bottom);
				drawableLeft.draw(canvas);
			} else {
				drawableRight.setBounds(start, top, end, bottom);
				drawableRight.draw(canvas);
			}
		} else if ((type & 2)!=0) {
			if (start > end) {
				canvas.drawRect(start, bottom-thickness-lineOffset, end, bottom-lineOffset, paintUnderline);
			} else {
				canvas.drawRect(end, bottom-thickness-lineOffset, start, bottom-lineOffset, paintUnderline);
			}
		}
	}
	
	void drawMultiLine(Canvas canvas, Layout layout, int startLine, int endLine, int startOffset, int endOffset) {
		// draw the first line
		int paragDir = layout.getParagraphDirection(startLine);
		int lineEndOffset = (int) (paragDir == Layout.DIR_RIGHT_TO_LEFT ?
				layout.getLineLeft(startLine) - horizontalPadding
				: layout.getLineRight(startLine) + horizontalPadding);
		
		int lineBottom = getLineBottom(layout, startLine);
		int lineTop = getLineTop(layout, startLine);
		drawStart(canvas, startOffset, lineTop, lineEndOffset, lineBottom);
		
		// for the lines in the middle draw the mid drawable
		for (int line = startLine + 1; line < endLine; line++) {
			lineTop = getLineTop(layout, line);
			lineBottom = getLineBottom(layout, line);
			if (drawable != null) {
				drawableMid.setBounds(
						((int) layout.getLineLeft(line) - horizontalPadding),
						lineTop,
						((int) layout.getLineRight(line) + horizontalPadding),
						lineBottom
				);
				drawableMid.draw(canvas);
			} else if ((type & 2)!=0) {
				canvas.drawRect(((int) layout.getLineLeft(line) - horizontalPadding)
					, lineBottom-thickness-lineOffset, ((int) layout.getLineRight(line) + horizontalPadding), lineBottom-lineOffset, paintUnderline);
			}
		}
		
		
		int lineStartOffset = (int) (paragDir == Layout.DIR_RIGHT_TO_LEFT ?
				layout.getLineRight(startLine) + horizontalPadding
				: layout.getLineLeft(startLine) - horizontalPadding);
		
		// draw the last line
		lineBottom = getLineBottom(layout, endLine);
		lineTop = getLineTop(layout, endLine);
		
		drawEnd(canvas, lineStartOffset, lineTop, endOffset, lineBottom);
	}
	
	void draw(Canvas canvas, Spanned text, Layout layout) {
		// ideally the calculations here should be cached since they are not cheap. However, proper
		// invalidation of the cache is required whenever anything related to text has changed.
		Annotation[] spans = text.getSpans(0, text.length(), Annotation.class);
		for (Annotation span:spans) {
			if (span==this || span.getValue().equals("rounded")) {
				int spanStart = text.getSpanStart(span);
				int spanEnd = text.getSpanEnd(span);
				int startLine = layout.getLineForOffset(spanStart);
				int endLine = layout.getLineForOffset(spanEnd);
				
				// start can be on the left or on the right depending on the language direction.
				int startOffset = (int) (layout.getPrimaryHorizontal(spanStart)
						+ -1 * layout.getParagraphDirection(startLine) * horizontalPadding);
				// end can be on the left or on the right depending on the language direction.
				int endOffset = (int) (layout.getPrimaryHorizontal(spanEnd)
						+ layout.getParagraphDirection(endLine) * horizontalPadding);
				if (startLine == endLine) {
					drawSingleLine(canvas, layout, startLine, endLine, startOffset, endOffset);
				} else {
					drawMultiLine(canvas, layout, startLine, endLine, startOffset, endOffset);
				}
			}
		}
	}
}

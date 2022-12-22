package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.databinding.DbCardListItemBinding;

/** TimelineDecoration. org. author: Endeavor et al. date: 2018/9/25
 *  https://www.jianshu.com/p/8a51039d9e68
 * 已无限拓展 by KnIfER., https://blog.csdn.net/sinat_27171121/article/details/128352676
 * date: 2022年12月17日 14点33分 */
public class TitleItemDecoration extends RecyclerView.ItemDecoration {
	public float paddingLeft;
	public int textBackground;
	public float textCorner;
	private int titleHeight;
	private int titleFontSz;
	public boolean bPinTitle = true;
	public boolean bPinTitleSlide = false;
	public final Paint bgPaint = new Paint();
	public final Paint bgPaint1 = new Paint();
	public final Paint textPaint = new Paint();
	public final Paint textPaint1 = new Paint();
	//public final Rect textRect = new Rect();
	public final RectF tmpRect = new RectF();
	private TitleDecorationCallback callback;
	
	public interface TitleDecorationCallback {
		boolean isSameGroup(int prevPos, int thePos);
		String getGroupName(int position);
		void postDrawText(Canvas canvas, float top, float bottom, float x, float y, float labelSz, Paint textPaint, int position, String name);
	}

    public TitleItemDecoration(Context context
			, TitleDecorationCallback callback
		    , int textColor
		    , int bgColor
	) {
        this.callback = callback;
		titleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
		//final int titleFontSz = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics());
		titleFontSz = titleHeight*2/3;
		
        textPaint.setTextSize(titleFontSz);
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
	
		textPaint1.setTextSize(titleFontSz);
		textPaint1.setAntiAlias(true);
	
		//descent = (int) textPaint.getFontMetrics().descent;

		bgPaint.setAntiAlias(true);
        bgPaint.setColor(bgColor);
    }

    // 这个方法用于给item隔开距离，类似直接给item设padding
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getBindingAdapterPosition(); // important here. not getViewLayoutPosition
        if (isFirst(position)) {
			//ViewUtils.ViewDataHolder<DbCardListItemBinding> vh = (ViewUtils.ViewDataHolder<DbCardListItemBinding>) ViewUtils.getViewHolderInParents(view);
			//CMN.debug("getItemOffsets::", position, vh.data.text1.getText());
            outRect.top = titleHeight;
        } else {
            outRect.top = 1;
        }
    }

    /** 绘制分栏标题 */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
		final  int childCount = parent.getChildCount();
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
		View view, viewAbove=null; RecyclerView.LayoutParams params;
        for (int i = 0, position; i < childCount; i++) {
            view = parent.getChildAt(i);
            params = (RecyclerView.LayoutParams) view.getLayoutParams();
            position = params.getViewLayoutPosition();
            if (isFirst(position)) {
				float bottom = view.getTop();
				if(viewAbove!=null && bottom-viewAbove.getBottom()<titleHeight/2) {
					CMN.debug("beforeView clash!!!", bottom-viewAbove.getBottom(), titleHeight);
					parent.invalidateItemDecorations();
					break;
				}
				final String name = callback.getGroupName(position);
                final float top = bottom - titleHeight;
				float x = view.getPaddingLeft() + paddingLeft;
				float y = top + titleHeight/2 - (textPaint.descent() + textPaint.ascent()) / 2;
				
				
                canvas.drawRect(left, top, right, bottom, bgPaint);
				float labelSz = -1;
				if (textBackground!=0) {
					bgPaint1.setColor(textBackground);
					float pad = 5f * GlobalOptions.density;
					float padY = titleHeight/8;
					labelSz = textPaint.measureText(name)+pad;
					if (textCorner != 0) {
						tmpRect.set(x-pad, top+padY, x+labelSz, bottom-padY);
						canvas.drawRoundRect(tmpRect
								, textCorner, textCorner, bgPaint1);
					} else {
						canvas.drawRect(x-pad, top+padY, x+labelSz, bottom-padY, bgPaint1);
					}
				}
				
				canvas.drawText(name, x, y, textPaint);
	
				// todo 这样调用真的好吗，抑或无感
				callback.postDrawText(canvas, top, bottom, x, y, labelSz, textPaint, position, name);
            }
			viewAbove = view;
        }
    }
	
	/** 绘制悬浮停靠效果 */
    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
		if (bPinTitle) {
			RecyclerView.ViewHolder vh = ViewUtils.getFirstViewHolder(parent);
			int position = vh==null?-1:vh.getLayoutPosition();
			if (position <= -1 || position >= parent.getAdapter().getItemCount() - 1) { // sanity check
				return;
			}
			View firstView = vh.itemView;
			
			final int left = parent.getPaddingLeft();
			final int right = parent.getWidth() - parent.getPaddingRight();
			int top = parent.getPaddingTop();
			int bottom = top + titleHeight;
			String name = callback.getGroupName(position);
			
			if (isFirst(position + 1)) {
				if (bPinTitleSlide) {
					if (firstView.getBottom() < titleHeight) {
						// 这里有bug,mTitleHeight过高时 滑动有问题 【原注】
						int d = firstView.getHeight() - titleHeight;
						top = firstView.getTop() + d;
						bottom = firstView.getBottom();
					}
				} else if(firstView.getBottom() < titleHeight*2/3){
					// 直接替换
					name = callback.getGroupName(position+1);
				}
			}
			canvas.drawRect(left, top, right, bottom, bgPaint);
			
			float x = left + firstView.getPaddingLeft() + paddingLeft;
			float y = top + titleHeight/2 - (textPaint.descent() + textPaint.ascent()) / 2;
			
			float labelSz = -1;
			if (textBackground!=0) {
				bgPaint1.setColor(textBackground);
				float pad = 5f * GlobalOptions.density;
				float padY = titleHeight/8;
				labelSz = textPaint.measureText(name)+pad;
				if (textCorner != 0) {
					tmpRect.set(x-pad, top+padY, x+labelSz, bottom-padY);
					canvas.drawRoundRect(tmpRect
							, textCorner, textCorner, bgPaint1);
				} else {
					canvas.drawRect(x-pad, top+padY, x+labelSz, bottom-padY, bgPaint);
				}
			}
			
			canvas.drawText(name, x, y, textPaint);
			
			callback.postDrawText(canvas, top, bottom, x, y, labelSz, textPaint, position, name);
		}
    }

    /** 判断是否是同一组的第一个item */
    private boolean isFirst(int position) {
		return position == 0 || !callback.isSameGroup(position - 1, position);
    }
	
	public int getHeight() {
		return titleHeight;
	}
	
	public float drawLabel(Canvas canvas, String text, float x, float y, float top, float bottom, int textColor, int background) {
		textPaint1.setColor(textColor);
		float pad = 5f * GlobalOptions.density;
		float padY = titleHeight/8;
		float labelSz = textPaint1.measureText(text) + pad;
		if (background != 0) {
			bgPaint1.setColor(background);
			if (textCorner != 0) {
				tmpRect.set(x-pad, top+padY, x+labelSz, bottom-padY);
				canvas.drawRoundRect(tmpRect
						, textCorner, textCorner, bgPaint1);
			} else {
				canvas.drawRect(x-pad, top+padY, x+labelSz, bottom-padY, bgPaint1);
			}
		}
		canvas.drawText(text, x, y, textPaint1);
		return x+labelSz;
	}
	
}
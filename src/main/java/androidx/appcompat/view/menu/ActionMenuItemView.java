/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.appcompat.view.menu;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import static androidx.appcompat.app.GlobalOptions.shouldRecordMenuView;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RestrictTo;
import androidx.appcompat.R;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.ForwardingListener;
import androidx.appcompat.widget.TooltipCompat;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class ActionMenuItemView extends AppCompatTextView
        implements MenuView.ItemView,  ActionMenuView.ActionMenuChildView ,
        View.OnClickListener,View.OnLongClickListener{

    private static final String TAG = "ActionMenuItemView";

    MenuItemImpl mItemData;
    private CharSequence mTitle;
    private Drawable mIcon;
    MenuBuilder.ItemInvoker mItemInvoker;
    private ForwardingListener mForwardingListener;
    PopupCallback mPopupCallback;

    private boolean mAllowTextWithIcon;
    private boolean mExpandedFormat;
    private int mMinWidth;
    private int mSavedPaddingLeft;

    private static final int MAX_ICON_SIZE = 32; // dp
    private int mMaxIconSize;

    public ActionMenuItemView(Context context) {
        this(context, null);
    }

    public ActionMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources res = context.getResources();
        mAllowTextWithIcon = shouldAllowTextWithIcon();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ActionMenuItemView, defStyle, 0);
        mMinWidth = a.getDimensionPixelSize(
                R.styleable.ActionMenuItemView_android_minWidth, 0);
        a.recycle();

        final float density = res.getDisplayMetrics().density;
        mMaxIconSize = (int) (MAX_ICON_SIZE * density + 0.5f);

        setOnClickListener(this);
        setOnLongClickListener(this);

        mSavedPaddingLeft = -1;
        setSaveEnabled(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mAllowTextWithIcon = shouldAllowTextWithIcon();
        updateTextButtonVisibility();
    }

    /**
     * Whether action menu items should obey the "withText" showAsAction flag. This may be set to
     * false for situations where space is extremely limited. -->
     */
    private boolean shouldAllowTextWithIcon() {
        final Configuration config = getContext().getResources().getConfiguration();
        final int widthDp = config.screenWidthDp;
        final int heightDp = config.screenHeightDp;

        return widthDp >= 480 || (widthDp >= 640 && heightDp >= 480)
                || config.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void setPadding(int l, int t, int r, int b) {
        mSavedPaddingLeft = l;
        super.setPadding(l, t, r, b);
    }

    @Override
    public MenuItemImpl getItemData() {
        return mItemData;
    }

    @Override
    public void initialize(MenuItemImpl itemData, int menuType) {
        mItemData = itemData;

        setIcon(itemData.getIcon());
        setTitle(itemData.getTitleForItemView(this)); // Title only takes effect if there is no icon
        setId(itemData.getItemId());

        setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);
        setEnabled(itemData.isEnabled());
        if (itemData.hasSubMenu()) {
            if (mForwardingListener == null) {
                mForwardingListener = new ActionMenuItemForwardingListener();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mItemData.hasSubMenu() && mForwardingListener != null
                && mForwardingListener.onTouch(this, e)) {
            return true;
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void onClick(View v) {
        if (mItemInvoker != null) {
			boolean srmv = shouldRecordMenuView;
			if(srmv) {
				mItemData.actionView = this;
			}
            mItemInvoker.invokeItem(mItemData);
			if(srmv) {
				mItemData.actionView = null;
			}
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mItemInvoker != null) {
			boolean srmv = shouldRecordMenuView;
			if(srmv) {
				mItemData.actionView = this;
			}
			boolean ret = mItemInvoker.pushItem(mItemData);
			if(srmv) {
				mItemData.actionView = null;
			}
            return ret;
        }
        return false;
    }

    public void setItemInvoker(MenuBuilder.ItemInvoker invoker) {
        mItemInvoker = invoker;
    }

    public void setPopupCallback(PopupCallback popupCallback) {
        mPopupCallback = popupCallback;
    }

    @Override
    public boolean prefersCondensedTitle() {
        return true;
    }

    @Override
    public void setCheckable(boolean checkable) {
        // TODO Support checkable action items
    }

    @Override
    public void setChecked(boolean checked) {
        // TODO Support checkable action items
    }

    public void setExpandedFormat(boolean expandedFormat) {
        if (mExpandedFormat != expandedFormat) {
            mExpandedFormat = expandedFormat;
            if (mItemData != null) {
                mItemData.actionFormatChanged();
            }
        }
    }

    private void updateTextButtonVisibility() {
        boolean visible = !TextUtils.isEmpty(mTitle);
        visible &= mIcon == null ||
                (mItemData.showsTextAsAction() && (mAllowTextWithIcon || mExpandedFormat));

        setText(visible ? mTitle : null);

        // Show the tooltip for items that do not already show text.
        final CharSequence contentDescription = mItemData.getContentDescription();
        if (TextUtils.isEmpty(contentDescription)) {
            // Use the uncondensed title for content description, but only if the title is not
            // shown already.
            setContentDescription(visible ? null : mItemData.getTitle());
        } else {
            setContentDescription(contentDescription);
        }

        final CharSequence tooltipText = mItemData.getTooltipText();
        if (Build.VERSION.SDK_INT >= 26)
        if (TextUtils.isEmpty(tooltipText)) {
            // Use the uncondensed title for tooltip, but only if the title is not shown already.
            TooltipCompat.setTooltipText(this, visible ? null : mItemData.getTitle());
        } else {
            TooltipCompat.setTooltipText(this, tooltipText);
        }
    }

    @Override
    public void setIcon(Drawable icon) {
        mIcon = icon;
        if (icon != null) {
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > mMaxIconSize) {
                final float scale = (float) mMaxIconSize / width;
                width = mMaxIconSize;
                height = (int) (height * scale);
            }
            if (height > mMaxIconSize) {
                final float scale = (float) mMaxIconSize / height;
                height = mMaxIconSize;
                width = (int) (width * scale);
            }
            icon.setBounds(0, 0, width, height);
        }
        setCompoundDrawables(icon, null, null, null);

        updateTextButtonVisibility();
    }

    public Drawable getIcon(){
        return mIcon;
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(getText());
    }

    @Override
    public void setShortcut(boolean showShortcut, char shortcutKey) {
        // Action buttons don't show text for shortcut keys.
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;

        updateTextButtonVisibility();
    }

    @Override
    public boolean showsIcon() {
        return true;
    }

    @Override
    public boolean needsDividerBefore() {
        return hasText() && mItemData.getIcon() == null;
    }

    @Override
    public boolean needsDividerAfter() {
        return hasText();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final boolean textVisible = hasText();
        if (textVisible && mSavedPaddingLeft >= 0) {
            super.setPadding(mSavedPaddingLeft, getPaddingTop(),
                    getPaddingRight(), getPaddingBottom());
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int oldMeasuredWidth = getMeasuredWidth();
        final int targetWidth = widthMode == MeasureSpec.AT_MOST ? Math.min(widthSize, mMinWidth)
                : mMinWidth;

        if (widthMode != MeasureSpec.EXACTLY && mMinWidth > 0 && oldMeasuredWidth < targetWidth) {
            // Remeasure at exactly the minimum width.
            super.onMeasure(MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY),
                    heightMeasureSpec);
        }

        if (!textVisible && mIcon != null) {
            // TextView won't center compound drawables in both dimensions without
            // a little coercion. Pad in to center the icon after we've measured.
            final int w = getMeasuredWidth();
            final int dw = mIcon.getBounds().width();
            super.setPadding((w - dw) / 2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

    private class ActionMenuItemForwardingListener extends ForwardingListener {
        public ActionMenuItemForwardingListener() {
            super(ActionMenuItemView.this);
        }

        @Override
        public ShowableListMenu getPopup() {
            if (mPopupCallback != null) {
                return mPopupCallback.getPopup();
            }
            return null;
        }

        @Override
        protected boolean onForwardingStarted() {
            // Call the invoker, then check if the expected popup is showing.
            if (mItemInvoker != null && mItemInvoker.invokeItem(mItemData)) {
                final ShowableListMenu popup = getPopup();
                return popup != null && popup.isShowing();
            }
            return false;
        }

        // Do not backport the framework impl here.
        // The framework's ListPopupWindow uses an animation before performing the item click
        // after selecting an item. As AppCompat doesn't use an animation, the popup is
        // dismissed and thus null'ed out before onForwardingStopped() has been called.
        // This messes up ActionMenuItemView's onForwardingStopped() impl since it will now
        // return false and make ListPopupWindow think it's still forwarding.
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        // This might get called with the state of ActionView since it shares the same ID with
        // ActionMenuItemView. Do not restore this state as ActionMenuItemView never saved it.
        super.onRestoreInstanceState(null);
    }

    public static abstract class PopupCallback {
        public abstract ShowableListMenu getPopup();
    }
}

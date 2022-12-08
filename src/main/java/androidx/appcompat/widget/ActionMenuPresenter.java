/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.appcompat.widget;

import static androidx.appcompat.view.menu.MenuPopupHelper.fastPopupMenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.R;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.ActionBarPolicy;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.view.menu.BaseMenuPresenter;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.appcompat.view.menu.SubMenuBuilder;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ActionProvider;
import androidx.core.view.GravityCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * MenuPresenter for building action menus as seen in the action bar and action modes.
 */
public class ActionMenuPresenter extends BaseMenuPresenter
        implements ActionProvider.SubUiVisibilityListener {

    private static final String TAG = "ActionMenuPresenter";

    OverflowMenuButton mOverflowButton;
    private Drawable mPendingOverflowIcon;
    private boolean mPendingOverflowIconSet;
    private boolean mReserveOverflow;
    private boolean mReserveOverflowSet;
    private int mWidthLimit;
    private int mActionItemWidthLimit;
    private int mMaxItems;
    private boolean mMaxItemsSet;
    private boolean mStrictWidthLimit;
    private boolean mWidthLimitSet;
    private boolean mExpandedActionViewsExclusive;

    private int mMinCellSize;

    // Group IDs that have been added as actions - used temporarily, allocated here for reuse.
    private final SparseBooleanArray mActionButtonGroups = new SparseBooleanArray();

    OverflowPopup mOverflowPopup;
    ActionButtonSubmenu mActionButtonPopup;

    OpenOverflowRunnable mPostedOpenRunnable;
    private ActionMenuPopupCallback mPopupCallback;

    final PopupPresenterCallback mPopupPresenterCallback = new PopupPresenterCallback();
    int mOpenSubMenuId;

    public ActionMenuPresenter(Context context) {
        super(context, R.layout.abc_action_menu_layout, R.layout.abc_action_menu_item_layout);
    }

    @Override
    public void initForMenu(@NonNull Context context, @Nullable MenuBuilder menu) {
        super.initForMenu(context, menu);

        final Resources res = context.getResources();

        final ActionBarPolicy abp = ActionBarPolicy.get(context);
        if (!mReserveOverflowSet) {
            mReserveOverflow = abp.showsOverflowMenuButton();
        }

        if (!mWidthLimitSet) {
            mWidthLimit = abp.getEmbeddedMenuWidthLimit();
        }

        // Measure for initial configuration
        if (!mMaxItemsSet) {
            mMaxItems = abp.getMaxActionButtons();
        }

        int width = mWidthLimit;
        if (mReserveOverflow) {
            if (mOverflowButton == null) {
                mOverflowButton = new OverflowMenuButton(mSystemContext);
                if (mPendingOverflowIconSet) {
                    mOverflowButton.setImageDrawable(mPendingOverflowIcon);
                    mPendingOverflowIcon = null;
                    mPendingOverflowIconSet = false;
                }
                final int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                mOverflowButton.measure(spec, spec);
            }
            width -= mOverflowButton.getMeasuredWidth();
        } else {
            mOverflowButton = null;
        }

        mActionItemWidthLimit = width;

        mMinCellSize = (int) (ActionMenuView.MIN_CELL_SIZE * res.getDisplayMetrics().density);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!mMaxItemsSet) {
            mMaxItems = ActionBarPolicy.get(mContext).getMaxActionButtons();
        }
        if (mMenu != null) {
            mMenu.onItemsChanged(true);
        }
    }

    public void setWidthLimit(int width, boolean strict) {
        mWidthLimit = width;
        mStrictWidthLimit = strict;
        mWidthLimitSet = true;
    }

    public void setReserveOverflow(boolean reserveOverflow) {
        mReserveOverflow = reserveOverflow;
        mReserveOverflowSet = true;
    }

    public void setItemLimit(int itemCount) {
        mMaxItems = itemCount;
        mMaxItemsSet = true;
    }

    public void setExpandedActionViewsExclusive(boolean isExclusive) {
        mExpandedActionViewsExclusive = isExclusive;
    }

    public void setOverflowIcon(Drawable icon) {
        if (mOverflowButton != null) {
            mOverflowButton.setImageDrawable(icon);
        } else {
            mPendingOverflowIconSet = true;
            mPendingOverflowIcon = icon;
        }
    }

    public Drawable getOverflowIcon() {
        if (mOverflowButton != null) {
            return mOverflowButton.getDrawable();
        } else if (mPendingOverflowIconSet) {
            return mPendingOverflowIcon;
        }
        return null;
    }

    @Override
    public MenuView getMenuView(ViewGroup root) {
        MenuView oldMenuView = mMenuView;
        MenuView result = super.getMenuView(root);
        if (oldMenuView != result) {
            ((ActionMenuView) result).setPresenter(this);
        }
        return result;
    }

    @Override
    public View getItemView(final MenuItemImpl item, View convertView, ViewGroup parent) {
        View actionView = item.getActionView();
        if (actionView == null || item.hasCollapsibleActionView()) {
            actionView = super.getItemView(item, convertView, parent);
        }
        actionView.setVisibility(item.isActionViewExpanded() ? View.GONE : View.VISIBLE);
	
		if (mMenu.checkActDrawable!=null && actionView.getClass()==ActionMenuItemView.class) {
			// 画出选中or启用的效果
			ActionMenuItemView view = (ActionMenuItemView) actionView;
			Drawable d = view.getIcon();
			// actionView.setAlpha(0.5f);
			if(item.isChecked() ^ (d!=view.getItemData().getIcon())) {
				d = view.getItemData().getIcon();
				if (item.isChecked()) {
					LayerDrawable ld = new LayerDrawable(
							new Drawable[]{mMenu.checkActDrawable, d}
					);
					d = ld;
				}
				view.setIcon(d);
			}
		}
		if(item.isEnabled() ^ (actionView.getAlpha()==1)) {
			actionView.setAlpha(item.isEnabled() ? 1 : 0.5f);
		}
		
        final ActionMenuView menuParent = (ActionMenuView) parent;
        final ViewGroup.LayoutParams lp = actionView.getLayoutParams();
        if (!menuParent.checkLayoutParams(lp)) {
            actionView.setLayoutParams(menuParent.generateLayoutParams(lp));
        }
        return actionView;
    }

    @Override
    public void bindItemView(MenuItemImpl item, MenuView.ItemView itemView) {
        itemView.initialize(item, 0);

        final ActionMenuView menuView = (ActionMenuView) mMenuView;
        final ActionMenuItemView actionItemView = (ActionMenuItemView) itemView;
        actionItemView.setItemInvoker(menuView);

        if (mPopupCallback == null) {
            mPopupCallback = new ActionMenuPopupCallback();
        }
        actionItemView.setPopupCallback(mPopupCallback);
    }

    @Override
    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        return item.isActionButton();
    }

    @Override
    public void updateMenuView(boolean cleared) {
        super.updateMenuView(cleared);

        ((View) mMenuView).requestLayout();

        if (mMenu != null) {
            final ArrayList<MenuItemImpl> actionItems = mMenu.getActionItems();
            final int count = actionItems.size();
            for (int i = 0; i < count; i++) {
                final ActionProvider provider = actionItems.get(i).getSupportActionProvider();
                if (provider != null) {
                    provider.setSubUiVisibilityListener(this);
                }
            }
        }

        final ArrayList<MenuItemImpl> nonActionItems = mMenu != null ?
                mMenu.getNonActionItems() : null;

        boolean hasOverflow = false;
        if (mReserveOverflow && nonActionItems != null) {
            final int count = nonActionItems.size();
            if (count == 1) {
                hasOverflow = !nonActionItems.get(0).isActionViewExpanded();
            } else {
                hasOverflow = count > 0;
            }
        }

        if (hasOverflow) {
            if (mOverflowButton == null) {
                mOverflowButton = new OverflowMenuButton(mSystemContext);
            }
            ViewGroup parent = (ViewGroup) mOverflowButton.getParent();
            if (parent != mMenuView) {
                if (parent != null) {
                    parent.removeView(mOverflowButton);
                }
                ActionMenuView menuView = (ActionMenuView) mMenuView;
				ActionMenuView.LayoutParams lp = menuView.generateOverflowButtonLayoutParams();
				menuView.addView(mOverflowButton, lp);
				if(GlobalOptions.isSmall)
					lp.width=GlobalOptions.btnMaxWidth;
            }
        } else if (mOverflowButton != null && mOverflowButton.getParent() == mMenuView) {
            ((ViewGroup) mMenuView).removeView(mOverflowButton);
        }

        ((ActionMenuView) mMenuView).setOverflowReserved(mReserveOverflow);
    }

    @Override
    public boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        if (parent.getChildAt(childIndex) == mOverflowButton) return false;
        return super.filterLeftoverView(parent, childIndex);
    }

    @Override
    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) return false;

        SubMenuBuilder topSubMenu = subMenu;
        while (topSubMenu.getParentMenu() != mMenu) {
            topSubMenu = (SubMenuBuilder) topSubMenu.getParentMenu();
        }
        View anchor = findViewForItem(topSubMenu.getItem());
        if (anchor == null) {
            // This means the submenu was opened from an overflow menu item, indicating the
            // MenuPopupHelper will handle opening the submenu via its MenuPopup. Return false to
            // ensure that the MenuPopup acts as presenter for the submenu, and acts on its
            // responsibility to display the new submenu.
            return false;
        }

        mOpenSubMenuId = subMenu.getItem().getItemId();

        boolean preserveIconSpacing = false;
        final int count = subMenu.size();
        for (int i = 0; i < count; i++) {
            MenuItem childItem = subMenu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                preserveIconSpacing = true;
                break;
            }
        }

        mActionButtonPopup = new ActionButtonSubmenu(mContext, subMenu, anchor);
        mActionButtonPopup.setForceShowIcon(preserveIconSpacing);
        mActionButtonPopup.show();

        super.onSubMenuSelected(subMenu);
        return true;
    }

    private View findViewForItem(MenuItem item) {
        final ViewGroup parent = (ViewGroup) mMenuView;
        if (parent == null) return null;

        final int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            if (child instanceof MenuView.ItemView &&
                    ((MenuView.ItemView) child).getItemData() == item) {
                return child;
            }
        }
        return null;
    }
	
	WeakReference<OverflowPopup> popupRef_1 = new WeakReference<>(null);
    /**
     * Display the overflow menu if one is present.
     * @return true if the overflow menu was shown, false otherwise.
     */
	public boolean showOverflowMenu() {
		//CMN.Log("showOverflowMenu???");
		if (mReserveOverflow && !isOverflowMenuShowing() && mMenu != null && mMenuView != null
				&& mPostedOpenRunnable == null && !mMenu.getNonActionItems().isEmpty()) {
			//CMN.Log("showOverflowMenu!!!");
			OverflowPopup popup = fastPopupMenu?popupRef_1.get():null;
			if(popup==null) {
				popup = new OverflowPopup(mContext, mMenu, mOverflowButton, true);
				if(fastPopupMenu)  popupRef_1 = new WeakReference<>(popup);
			}
			mPostedOpenRunnable = new OpenOverflowRunnable(popup);
			// Post this for later; we might still need a layout for the anchor to be right.
			// ((View) mMenuView).post(mPostedOpenRunnable);
			if(fastPopupMenu)  mPostedOpenRunnable.run();
			else ((View) mMenuView).post(mPostedOpenRunnable);
		
			// ActionMenuPresenter uses null as a callback argument here
			// to indicate overflow is opening.
			//super.onSubMenuSelected(null); // todo ????
		
			return true;
		}
		return false;
	}

    /**
     * Hide the overflow menu if it is currently showing.
     *
     * @return true if the overflow menu was hidden, false otherwise.
     */
    public boolean hideOverflowMenu() {
		// CMN.Log("mPostedOpenRunnable!!!", mPostedOpenRunnable);
        if (mPostedOpenRunnable != null && mMenuView != null) {
            ((View) mMenuView).removeCallbacks(mPostedOpenRunnable);
            mPostedOpenRunnable = null;
            return true;
        }

        MenuPopupHelper popup = mOverflowPopup;
		// CMN.Log("dismissed!!!", popup);
        if (popup != null) {
            popup.dismiss();
            return true;
        }
        return false;
    }

    /**
     * Dismiss all popup menus - overflow and submenus.
     * @return true if popups were dismissed, false otherwise. (This can be because none were open.)
     */
    public boolean dismissPopupMenus() {
        boolean result = hideOverflowMenu();
        result |= hideSubMenus();
        return result;
    }

    /**
     * Dismiss all submenu popups.
     *
     * @return true if popups were dismissed, false otherwise. (This can be because none were open.)
     */
    public boolean hideSubMenus() {
        if (mActionButtonPopup != null) {
            mActionButtonPopup.dismiss();
            return true;
        }
        return false;
    }

    /**
     * @return true if the overflow menu is currently showing
     */
    public boolean isOverflowMenuShowing() {
        return mOverflowPopup != null && mOverflowPopup.isShowing();
    }

    public boolean isOverflowMenuShowPending() {
        return mPostedOpenRunnable != null || isOverflowMenuShowing();
    }

    /**
     * @return true if space has been reserved in the action menu for an overflow item.
     */
    public boolean isOverflowReserved() {
        return mReserveOverflow;
    }

    @Override
    public boolean flagActionItems() {
        final ArrayList<MenuItemImpl> visibleItems;
        final int itemsSize;
        if (mMenu != null) {
            visibleItems = mMenu.getVisibleItems();
            itemsSize = visibleItems.size();
        } else {
            visibleItems = null;
            itemsSize = 0;
        }

        int maxActions = mMaxItems;
        int widthLimit = mActionItemWidthLimit;
        final int querySpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final ViewGroup parent = (ViewGroup) mMenuView;

        int requiredItems = 0;
        int requestedItems = 0;
        int firstActionWidth = 0;
        boolean hasOverflow = false;
        for (int i = 0; i < itemsSize; i++) {
            MenuItemImpl item = visibleItems.get(i);
            if (item.requiresActionButton()) {
                requiredItems++;
            } else if (item.requestsActionButton()) {
                requestedItems++;
            } else {
                hasOverflow = true;
            }
            if (mExpandedActionViewsExclusive && item.isActionViewExpanded()) {
                // Overflow everything if we have an expanded action view and we're
                // space constrained.
                maxActions = 0;
            }
        }

        // Reserve a spot for the overflow item if needed.
        if (mReserveOverflow &&
                (hasOverflow || requiredItems + requestedItems > maxActions)) {
            maxActions--;
        }
        maxActions -= requiredItems;

        final SparseBooleanArray seenGroups = mActionButtonGroups;
        seenGroups.clear();

        int cellSize = 0;
        int cellsRemaining = 0;
        if (mStrictWidthLimit) {
            cellsRemaining = widthLimit / mMinCellSize;
            final int cellSizeRemaining = widthLimit % mMinCellSize;
            cellSize = mMinCellSize + cellSizeRemaining / cellsRemaining;
        }

        // Flag as many more requested items as will fit.
        for (int i = 0; i < itemsSize; i++) {
            MenuItemImpl item = visibleItems.get(i);

            if (item.requiresActionButton()) {
                View v = getItemView(item, null, parent);
                if (mStrictWidthLimit) {
                    cellsRemaining -= ActionMenuView.measureChildForCells(v,
                            cellSize, cellsRemaining, querySpec, 0);
                } else {
                    v.measure(querySpec, querySpec);
                }
                final int measuredWidth = v.getMeasuredWidth();
                widthLimit -= measuredWidth;
                if (firstActionWidth == 0) {
                    firstActionWidth = measuredWidth;
                }
                final int groupId = item.getGroupId();
                if (groupId != 0) {
                    seenGroups.put(groupId, true);
                }
                item.setIsActionButton(true);
            } else if (item.requestsActionButton()) {
                // Items in a group with other items that already have an action slot
                // can break the max actions rule, but not the width limit.
                final int groupId = item.getGroupId();
                final boolean inGroup = seenGroups.get(groupId);
                boolean isAction = (maxActions > 0 || inGroup) && widthLimit > 0 &&
                        (!mStrictWidthLimit || cellsRemaining > 0);

                if (isAction) {
                    View v = getItemView(item, null, parent);
                    if (mStrictWidthLimit) {
                        final int cells = ActionMenuView.measureChildForCells(v,
                                cellSize, cellsRemaining, querySpec, 0);
                        cellsRemaining -= cells;
                        if (cells == 0) {
                            isAction = false;
                        }
                    } else {
                        v.measure(querySpec, querySpec);
                    }
                    final int measuredWidth = v.getMeasuredWidth();
                    widthLimit -= measuredWidth;
                    if (firstActionWidth == 0) {
                        firstActionWidth = measuredWidth;
                    }

                    if (mStrictWidthLimit) {
                        isAction &= widthLimit >= 0;
                    } else {
                        // Did this push the entire first item past the limit?
                        isAction &= widthLimit + firstActionWidth > 0;
                    }
                }

                if (isAction && groupId != 0) {
                    seenGroups.put(groupId, true);
                } else if (inGroup) {
                    // We broke the width limit. Demote the whole group, they all overflow now.
                    seenGroups.put(groupId, false);
                    for (int j = 0; j < i; j++) {
                        MenuItemImpl areYouMyGroupie = visibleItems.get(j);
                        if (areYouMyGroupie.getGroupId() == groupId) {
                            // Give back the action slot
                            if (areYouMyGroupie.isActionButton()) maxActions++;
                            areYouMyGroupie.setIsActionButton(false);
                        }
                    }
                }

                if (isAction) maxActions--;

                item.setIsActionButton(isAction);
            } else {
                // Neither requires nor requests an action button.
                item.setIsActionButton(false);
            }
        }
        return true;
    }

    @Override
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        dismissPopupMenus();
        super.onCloseMenu(menu, allMenusAreClosing);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.openSubMenuId = mOpenSubMenuId;
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            return;
        }

        SavedState saved = (SavedState) state;
        if (saved.openSubMenuId > 0) {
            MenuItem item = mMenu.findItem(saved.openSubMenuId);
            if (item != null) {
                SubMenuBuilder subMenu = (SubMenuBuilder) item.getSubMenu();
                onSubMenuSelected(subMenu);
            }
        }
    }

    @Override
    public void onSubUiVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            // Not a submenu, but treat it like one.
            super.onSubMenuSelected(null);
        } else if (mMenu != null) {
            mMenu.close(false /* closeAllMenus */);
        }
    }

    public void setMenuView(ActionMenuView menuView) {
        mMenuView = menuView;
        menuView.initialize(mMenu);
    }

    @SuppressLint("BanParcelableUsage")
    private static class SavedState implements Parcelable {
        public int openSubMenuId;

        SavedState() {
        }

        SavedState(Parcel in) {
            openSubMenuId = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(openSubMenuId);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
	
	public class OverflowMenuButton extends AppCompatImageView
            implements ActionMenuView.ActionMenuChildView {
		
		public OnClickListener beforeOpen;
        public OverflowMenuButton(Context context) {
            super(context, null, R.attr.actionOverflowButtonStyle);
            setId(R.id.action_menu_presenter);
            setClickable(true);
            setFocusable(true);
            setVisibility(VISIBLE);
            setEnabled(true);

            TooltipCompat.setTooltipText(this, getContentDescription());

            setOnTouchListener(new ForwardingListener(this) {
                @Override
                public ShowableListMenu getPopup() {
                    if (mOverflowPopup == null) {
                        return null;
                    }

                    return mOverflowPopup.getPopup();
                }

                @Override
                public boolean onForwardingStarted() {
                    showOverflowMenu();
                    return true;
                }

                @Override
                public boolean onForwardingStopped() {
                    // Displaying the popup occurs asynchronously, so wait for
                    // the runnable to finish before deciding whether to stop
                    // forwarding.
                    if (mPostedOpenRunnable != null) {
                        return false;
                    }

                    hideOverflowMenu();
                    return true;
                }
            });
        }

        @Override
        public boolean performClick() {
            if (super.performClick()) {
                return true;
            }
			if(beforeOpen !=null)
				beforeOpen.onClick(this); // todo use ... indicate overflow is opening instead
            playSoundEffect(SoundEffectConstants.CLICK);
            showOverflowMenu();
            return true;
        }

        @Override
        public boolean needsDividerBefore() {
            return false;
        }

        @Override
        public boolean needsDividerAfter() {
            return false;
        }

        @Override
        protected boolean setFrame(int l, int t, int r, int b) {
            final boolean changed = super.setFrame(l, t, r, b);

            // Set up the hotspot bounds to be centered on the image.
            final Drawable d = getDrawable();
            final Drawable bg = getBackground();
            if (d != null && bg != null) {
                final int width = getWidth();
                final int height = getHeight();
                final int halfEdge = Math.max(width, height) / 2;
                final int offsetX = getPaddingLeft() - getPaddingRight();
                final int offsetY = getPaddingTop() - getPaddingBottom();
                final int centerX = (width + offsetX) / 2;
                final int centerY = (height + offsetY) / 2;
                DrawableCompat.setHotspotBounds(bg, centerX - halfEdge, centerY - halfEdge,
                        centerX + halfEdge, centerY + halfEdge);
            }

            return changed;
        }
		
		public final ActionMenuPresenter getPresenter() {
			return ActionMenuPresenter.this;
		}
		
		public void clearPopup() {
			if(getPresenter().mOverflowPopup!=null) {
				getPresenter().mOverflowPopup.popupRef_2.clear();
			}
		}
    }

    private class OverflowPopup extends MenuPopupHelper {
        public OverflowPopup(Context context, MenuBuilder menu, View anchorView,
                boolean overflowOnly) {
            super(context, menu, anchorView, overflowOnly, R.attr.actionOverflowMenuStyle);
            setGravity(GravityCompat.END);
            setPresenterCallback(mPopupPresenterCallback);
        }

        @Override
        protected void onDismiss() {
            if (mMenu != null) {
                mMenu.close();
            }
            // mOverflowPopup = null;

            super.onDismiss();
        }
    }

    private class ActionButtonSubmenu extends MenuPopupHelper {
        public ActionButtonSubmenu(Context context, SubMenuBuilder subMenu, View anchorView) {
            super(context, subMenu, anchorView, false, R.attr.actionOverflowMenuStyle);

            MenuItemImpl item = (MenuItemImpl) subMenu.getItem();
            if (!item.isActionButton()) {
                // Give a reasonable anchor to nested submenus.
                setAnchorView(mOverflowButton == null ? (View) mMenuView : mOverflowButton);
            }

            setPresenterCallback(mPopupPresenterCallback);
        }

        @Override
        protected void onDismiss() {
            mActionButtonPopup = null;
            mOpenSubMenuId = 0;

            super.onDismiss();
        }
    }

    private class PopupPresenterCallback implements Callback {
        PopupPresenterCallback() {
        }

        @Override
        public boolean onOpenSubMenu(@NonNull MenuBuilder subMenu) {
            if (subMenu == mMenu) return false;

            mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
            final Callback cb = getCallback();
            return cb != null ? cb.onOpenSubMenu(subMenu) : false;
        }

        @Override
        public void onCloseMenu(@NonNull MenuBuilder menu, boolean allMenusAreClosing) {
            if (menu instanceof SubMenuBuilder) {
                menu.getRootMenu().close(false /* closeAllMenus */);
            }
            final Callback cb = getCallback();
            if (cb != null) {
                cb.onCloseMenu(menu, allMenusAreClosing);
            }
        }
    }

    private class OpenOverflowRunnable implements Runnable {
        private OverflowPopup mPopup;

        public OpenOverflowRunnable(OverflowPopup popup) {
            mPopup = popup;
        }

        @Override
        public void run() {
            if (mMenu != null) {
                mMenu.changeMenuMode();
            }
            final View menuView = (View) mMenuView;
            if (menuView != null && menuView.getWindowToken() != null && mPopup.tryShow()) {
                mOverflowPopup = mPopup;
            }
            mPostedOpenRunnable = null;
        }
    }

    private class ActionMenuPopupCallback extends ActionMenuItemView.PopupCallback {
        ActionMenuPopupCallback() {
        }

        @Override
        public ShowableListMenu getPopup() {
            return mActionButtonPopup != null ? mActionButtonPopup.getPopup() : null;
        }
    }
}

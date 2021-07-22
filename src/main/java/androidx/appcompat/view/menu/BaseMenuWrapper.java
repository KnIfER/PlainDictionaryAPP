/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.collection.SimpleArrayMap;
import androidx.core.internal.view.SupportMenuItem;
import androidx.core.internal.view.SupportSubMenu;

abstract class BaseMenuWrapper {

    final Context mContext;

    private SimpleArrayMap<SupportMenuItem, MenuItem> mMenuItems;
    private SimpleArrayMap<SupportSubMenu, SubMenu> mSubMenus;

    BaseMenuWrapper(Context context) {
        mContext = context;
    }

    final MenuItem getMenuItemWrapper(final MenuItem menuItem) {
        if (menuItem instanceof SupportMenuItem) {
            final SupportMenuItem supportMenuItem = (SupportMenuItem) menuItem;

            // Instantiate Map if null
            if (mMenuItems == null) {
                mMenuItems = new SimpleArrayMap<>();
            }

            // First check if we already have a wrapper for this item
            MenuItem wrappedItem = mMenuItems.get(menuItem);

            if (null == wrappedItem) {
                // ... if not, create one and add it to our map
                wrappedItem = new MenuItemWrapperICS(mContext, supportMenuItem);
                mMenuItems.put(supportMenuItem, wrappedItem);
            }

            return wrappedItem;
        }
        return menuItem;
    }

    final SubMenu getSubMenuWrapper(final SubMenu subMenu) {
        if (subMenu instanceof SupportSubMenu) {
            final SupportSubMenu supportSubMenu = (SupportSubMenu) subMenu;

            // Instantiate Map if null
            if (mSubMenus == null) {
                mSubMenus = new SimpleArrayMap<>();
            }

            SubMenu wrappedMenu = mSubMenus.get(supportSubMenu);

            if (null == wrappedMenu) {
                wrappedMenu = new SubMenuWrapperICS(mContext, supportSubMenu);
                mSubMenus.put(supportSubMenu, wrappedMenu);
            }
            return wrappedMenu;
        }
        return subMenu;
    }


    final void internalClear() {
        if (mMenuItems != null) {
            mMenuItems.clear();
        }
        if (mSubMenus != null) {
            mSubMenus.clear();
        }
    }

    final void internalRemoveGroup(final int groupId) {
        if (mMenuItems == null) {
            return;
        }
        for (int i = 0; i < mMenuItems.size(); i++) {
            if (mMenuItems.keyAt(i).getGroupId() == groupId) {
                mMenuItems.removeAt(i);
                i--;
            }
        }
    }

    final void internalRemoveItem(final int id) {
        if (mMenuItems == null) {
            return;
        }
        for (int i = 0; i < mMenuItems.size(); i++) {
            if (mMenuItems.keyAt(i).getItemId() == id) {
                mMenuItems.removeAt(i);
                break;
            }
        }
    }
}

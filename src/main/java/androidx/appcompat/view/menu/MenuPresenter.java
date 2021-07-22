/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * A MenuPresenter is responsible for building views for a Menu object. It takes over some
 * responsibility from the old style monolithic MenuBuilder class.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public interface MenuPresenter {

    /**
     * Called by menu implementation to notify another component of open/close events.
     */
    interface Callback {
        /**
         * Called when a menu is closing.
         * @param menu
         * @param allMenusAreClosing
         */
        void onCloseMenu(@NonNull MenuBuilder menu, boolean allMenusAreClosing);

        /**
         * Called when a submenu opens. Useful for notifying the application
         * of menu state so that it does not attempt to hide the action bar
         * while a submenu is open or similar.
         *
         * @param subMenu Submenu currently being opened
         * @return true if the Callback will handle presenting the submenu, false if
         *         the presenter should attempt to do so.
         */
        boolean onOpenSubMenu(@NonNull MenuBuilder subMenu);
    }

    /**
     * Initializes this presenter for the given context and menu.
     * <p>
     * This method is called by MenuBuilder when a presenter is added. See
     * {@link MenuBuilder#addMenuPresenter(MenuPresenter)}.
     *
     * @param context the context for this presenter; used for view creation
     *                and resource management, must be non-{@code null}
     * @param menu the menu to host, or {@code null} to clear the hosted menu
     */
    void initForMenu(Context context, MenuBuilder menu);

    /**
     * Retrieve a MenuView to display the menu specified in
     * {@link #initForMenu(Context, MenuBuilder)}.
     *
     * @param root Intended parent of the MenuView.
     * @return A freshly created MenuView.
     */
    MenuView getMenuView(ViewGroup root);

    /**
     * Update the menu UI in response to a change. Called by
     * MenuBuilder during the normal course of operation.
     *
     * @param cleared true if the menu was entirely cleared
     */
    void updateMenuView(boolean cleared);

    /**
     * Set a callback object that will be notified of menu events
     * related to this specific presentation.
     * @param cb Callback that will be notified of future events
     */
    void setCallback(Callback cb);

    /**
     * Called by Menu implementations to indicate that a submenu item
     * has been selected. An active Callback should be notified, and
     * if applicable the presenter should present the submenu.
     *
     * @param subMenu SubMenu being opened
     * @return true if the the event was handled, false otherwise.
     */
    boolean onSubMenuSelected(SubMenuBuilder subMenu);

    /**
     * Called by Menu implementations to indicate that a menu or submenu is
     * closing. Presenter implementations should close the representation
     * of the menu indicated as necessary and notify a registered callback.
     *
     * @param menu the menu or submenu that is closing
     * @param allMenusAreClosing {@code true} if all displayed menus and
     *                           submenus are closing, {@code false} if only
     *                           the specified menu is closing
     */
    void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing);

    /**
     * Called by Menu implementations to flag items that will be shown as actions.
     * @return true if this presenter changed the action status of any items.
     */
    boolean flagActionItems();

    /**
     * Called when a menu item with a collapsible action view should expand its action view.
     *
     * @param menu Menu containing the item to be expanded
     * @param item Item to be expanded
     * @return true if this presenter expanded the action view, false otherwise.
     */
    boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item);

    /**
     * Called when a menu item with a collapsible action view should collapse its action view.
     *
     * @param menu Menu containing the item to be collapsed
     * @param item Item to be collapsed
     * @return true if this presenter collapsed the action view, false otherwise.
     */
    boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item);

    /**
     * Returns an ID for determining how to save/restore instance state.
     * @return a valid ID value.
     */
    int getId();

    /**
     * Returns a Parcelable describing the current state of the presenter.
     * It will be passed to the {@link #onRestoreInstanceState(Parcelable)}
     * method of the presenter sharing the same ID later.
     * @return The saved instance state
     */
    Parcelable onSaveInstanceState();

    /**
     * Supplies the previously saved instance state to be restored.
     * @param state The previously saved instance state
     */
    void onRestoreInstanceState(Parcelable state);
}

/*
 * Copyright (C) 2013 The Android Open Source Project
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

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.R;
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.core.view.ActionProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

/**
 * This class is a view for choosing an activity for handling a given {@link Intent}.
 * <p>
 * The view is composed of two adjacent buttons:
 * <ul>
 * <li>
 * The left button is an immediate action and allows one click activity choosing.
 * Tapping this button immediately executes the intent without requiring any further
 * user input. Long press on this button shows a popup for changing the default
 * activity.
 * </li>
 * <li>
 * The right button is an overflow action and provides an optimized menu
 * of additional activities. Tapping this button shows a popup anchored to this
 * view, listing the most frequently used activities. This list is initially
 * limited to a small number of items in frequency used order. The last item,
 * "Show all..." serves as an affordance to display all available activities.
 * </li>
 * </ul>
 * </p>
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class ActivityChooserView extends ViewGroup implements
        ActivityChooserModel.ActivityChooserModelClient {
    /**
     * An adapter for displaying the activities in an {@link android.widget.AdapterView}.
     */
    final ActivityChooserViewAdapter mAdapter;

    /**
     * Implementation of various interfaces to avoid publishing them in the APIs.
     */
    private final Callbacks mCallbacks;

    /**
     * The content of this view.
     */
    private final View mActivityChooserContent;

    /**
     * Stores the background drawable to allow hiding and latter showing.
     */
    private final Drawable mActivityChooserContentBackground;

    /**
     * The expand activities action button;
     */
    final FrameLayout mExpandActivityOverflowButton;

    /**
     * The image for the expand activities action button;
     */
    private final ImageView mExpandActivityOverflowButtonImage;

    /**
     * The default activities action button;
     */
    final FrameLayout mDefaultActivityButton;

    /**
     * The image for the default activities action button;
     */
    private final ImageView mDefaultActivityButtonImage;

    /**
     * The maximal width of the list popup.
     */
    private final int mListPopupMaxWidth;

    /**
     * The ActionProvider hosting this view, if applicable.
     */
    ActionProvider mProvider;

    /**
     * Observer for the model data.
     */
    final DataSetObserver mModelDataSetObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mAdapter.notifyDataSetInvalidated();
        }
    };

    private final OnGlobalLayoutListener mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (isShowingPopup()) {
                if (!isShown()) {
                    getListPopupWindow().dismiss();
                } else {
                    getListPopupWindow().show();
                    if (mProvider != null) {
                        mProvider.subUiVisibilityChanged(true);
                    }
                }
            }
        }
    };

    /**
     * Popup window for showing the activity overflow list.
     */
    private ListPopupWindow mListPopupWindow;

    /**
     * Listener for the dismissal of the popup/alert.
     */
    PopupWindow.OnDismissListener mOnDismissListener;

    /**
     * Flag whether a default activity currently being selected.
     */
    boolean mIsSelectingDefaultActivity;

    /**
     * The count of activities in the popup.
     */
    int mInitialActivityCount = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_DEFAULT;

    /**
     * Flag whether this view is attached to a window.
     */
    private boolean mIsAttachedToWindow;

    /**
     * String resource for formatting content description of the default target.
     */
    private int mDefaultActionButtonContentDescription;

    /**
     * Create a new instance.
     *
     * @param context The application environment.
     */
    public ActivityChooserView(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Create a new instance.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    public ActivityChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Create a new instance.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public ActivityChooserView(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray attributesArray = context.obtainStyledAttributes(attrs,
                R.styleable.ActivityChooserView, defStyle, 0);
        ViewCompat.saveAttributeDataForStyleable(this, context,
                R.styleable.ActivityChooserView, attrs, attributesArray, defStyle, 0);

        mInitialActivityCount = attributesArray.getInt(
                R.styleable.ActivityChooserView_initialActivityCount,
                ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_DEFAULT);

        Drawable expandActivityOverflowButtonDrawable = attributesArray.getDrawable(
                R.styleable.ActivityChooserView_expandActivityOverflowButtonDrawable);

        attributesArray.recycle();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.abc_activity_chooser_view, this, true);

        mCallbacks = new Callbacks();

        mActivityChooserContent = findViewById(R.id.activity_chooser_view_content);
        mActivityChooserContentBackground = mActivityChooserContent.getBackground();

        mDefaultActivityButton = findViewById(R.id.default_activity_button);
        mDefaultActivityButton.setOnClickListener(mCallbacks);
        mDefaultActivityButton.setOnLongClickListener(mCallbacks);
        mDefaultActivityButtonImage = (ImageView) mDefaultActivityButton.findViewById(R.id.image);

        final FrameLayout expandButton = findViewById(R.id.expand_activities_button);
        expandButton.setOnClickListener(mCallbacks);
        expandButton.setAccessibilityDelegate(new AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                AccessibilityNodeInfoCompat.wrap(info).setCanOpenPopup(true);
            }
        });
        expandButton.setOnTouchListener(new ForwardingListener(expandButton) {
            @Override
            public ShowableListMenu getPopup() {
                return getListPopupWindow();
            }

            @Override
            protected boolean onForwardingStarted() {
                showPopup();
                return true;
            }

            @Override
            protected boolean onForwardingStopped() {
                dismissPopup();
                return true;
            }
        });
        mExpandActivityOverflowButton = expandButton;
        mExpandActivityOverflowButtonImage =
            (ImageView) expandButton.findViewById(R.id.image);
        mExpandActivityOverflowButtonImage.setImageDrawable(expandActivityOverflowButtonDrawable);

        mAdapter = new ActivityChooserViewAdapter();
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateAppearance();
            }
        });

        Resources resources = context.getResources();
        mListPopupMaxWidth = Math.max(resources.getDisplayMetrics().widthPixels / 2,
                resources.getDimensionPixelSize(R.dimen.abc_config_prefDialogWidth));
    }

    /**
     * @hide
     * {@inheritDoc}
     */
    @RestrictTo(LIBRARY)
    @Override
    public void setActivityChooserModel(ActivityChooserModel dataModel) {
        mAdapter.setDataModel(dataModel);
        if (isShowingPopup()) {
            dismissPopup();
            showPopup();
        }
    }

    /**
     * Sets the background for the button that expands the activity
     * overflow list.
     *
     * <strong>Note:</strong> Clients would like to set this drawable
     * as a clue about the action the chosen activity will perform. For
     * example, if a share activity is to be chosen the drawable should
     * give a clue that sharing is to be performed.
     *
     * @param drawable The drawable.
     */
    public void setExpandActivityOverflowButtonDrawable(Drawable drawable) {
        mExpandActivityOverflowButtonImage.setImageDrawable(drawable);
    }

    /**
     * Sets the content description for the button that expands the activity
     * overflow list.
     *
     * description as a clue about the action performed by the button.
     * For example, if a share activity is to be chosen the content
     * description should be something like "Share with".
     *
     * @param resourceId The content description resource id.
     */
    public void setExpandActivityOverflowButtonContentDescription(int resourceId) {
        CharSequence contentDescription = getContext().getString(resourceId);
        mExpandActivityOverflowButtonImage.setContentDescription(contentDescription);
    }

    /**
     * Set the provider hosting this view, if applicable.
     * @hide Internal use only
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public void setProvider(ActionProvider provider) {
        mProvider = provider;
    }

    /**
     * Shows the popup window with activities.
     *
     * @return True if the popup was shown, false if already showing.
     */
    public boolean showPopup() {
        if (isShowingPopup() || !mIsAttachedToWindow) {
            return false;
        }
        mIsSelectingDefaultActivity = false;
        showPopupUnchecked(mInitialActivityCount);
        return true;
    }

    /**
     * Shows the popup no matter if it was already showing.
     *
     * @param maxActivityCount The max number of activities to display.
     */
    void showPopupUnchecked(int maxActivityCount) {
        if (mAdapter.getDataModel() == null) {
            throw new IllegalStateException("No data model. Did you call #setDataModel?");
        }

        getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

        final boolean defaultActivityButtonShown =
                mDefaultActivityButton.getVisibility() == VISIBLE;

        final int activityCount = mAdapter.getActivityCount();
        final int maxActivityCountOffset = defaultActivityButtonShown ? 1 : 0;
        if (maxActivityCount != ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED
                && activityCount > maxActivityCount + maxActivityCountOffset) {
            mAdapter.setShowFooterView(true);
            mAdapter.setMaxActivityCount(maxActivityCount - 1);
        } else {
            mAdapter.setShowFooterView(false);
            mAdapter.setMaxActivityCount(maxActivityCount);
        }

        ListPopupWindow popupWindow = getListPopupWindow();
        if (!popupWindow.isShowing()) {
            if (mIsSelectingDefaultActivity || !defaultActivityButtonShown) {
                mAdapter.setShowDefaultActivity(true, defaultActivityButtonShown);
            } else {
                mAdapter.setShowDefaultActivity(false, false);
            }
            final int contentWidth = Math.min(mAdapter.measureContentWidth(), mListPopupMaxWidth);
            popupWindow.setContentWidth(contentWidth);
            popupWindow.show();
            if (mProvider != null) {
                mProvider.subUiVisibilityChanged(true);
            }
            popupWindow.getListView().setContentDescription(getContext().getString(
                    R.string.abc_activitychooserview_choose_application));
            popupWindow.getListView().setSelector(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * Dismisses the popup window with activities.
     *
     * @return True if dismissed, false if already dismissed.
     */
    public boolean dismissPopup() {
        if (isShowingPopup()) {
            getListPopupWindow().dismiss();
            ViewTreeObserver viewTreeObserver = getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
            }
        }
        return true;
    }

    /**
     * Gets whether the popup window with activities is shown.
     *
     * @return True if the popup is shown.
     */
    public boolean isShowingPopup() {
        return getListPopupWindow().isShowing();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ActivityChooserModel dataModel = mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.registerObserver(mModelDataSetObserver);
        }
        mIsAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ActivityChooserModel dataModel = mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.unregisterObserver(mModelDataSetObserver);
        }
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
        }
        if (isShowingPopup()) {
            dismissPopup();
        }
        mIsAttachedToWindow = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = mActivityChooserContent;
        // If the default action is not visible we want to be as tall as the
        // ActionBar so if this widget is used in the latter it will look as
        // a normal action button.
        if (mDefaultActivityButton.getVisibility() != VISIBLE) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),
                    MeasureSpec.EXACTLY);
        }
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mActivityChooserContent.layout(0, 0, right - left, bottom - top);
        if (!isShowingPopup()) {
            dismissPopup();
        }
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    public ActivityChooserModel getDataModel() {
        return mAdapter.getDataModel();
    }

    /**
     * Sets a listener to receive a callback when the popup is dismissed.
     *
     * @param listener The listener to be notified.
     */
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    /**
     * Sets the initial count of items shown in the activities popup
     * i.e. the items before the popup is expanded. This is an upper
     * bound since it is not guaranteed that such number of intent
     * handlers exist.
     *
     * @param itemCount The initial popup item count.
     */
    public void setInitialActivityCount(int itemCount) {
        mInitialActivityCount = itemCount;
    }

    /**
     * Sets a content description of the default action button. This
     * resource should be a string taking one formatting argument and
     * will be used for formatting the content description of the button
     * dynamically as the default target changes. For example, a resource
     * pointing to the string "share with %1$s" will result in a content
     * description "share with Bluetooth" for the Bluetooth activity.
     *
     * @param resourceId The resource id.
     */
    public void setDefaultActionButtonContentDescription(int resourceId) {
        mDefaultActionButtonContentDescription = resourceId;
    }

    /**
     * Gets the list popup window which is lazily initialized.
     *
     * @return The popup.
     */
    ListPopupWindow getListPopupWindow() {
        if (mListPopupWindow == null) {
            mListPopupWindow = new ListPopupWindow(getContext());
            mListPopupWindow.setAdapter(mAdapter);
            mListPopupWindow.setAnchorView(ActivityChooserView.this);
            mListPopupWindow.setModal(true);
            mListPopupWindow.setOnItemClickListener(mCallbacks);
            mListPopupWindow.setOnDismissListener(mCallbacks);
        }
        return mListPopupWindow;
    }

    /**
     * Updates the buttons state.
     */
    void updateAppearance() {
        // Expand overflow button.
        if (mAdapter.getCount() > 0) {
            mExpandActivityOverflowButton.setEnabled(true);
        } else {
            mExpandActivityOverflowButton.setEnabled(false);
        }
        // Default activity button.
        final int activityCount = mAdapter.getActivityCount();
        final int historySize = mAdapter.getHistorySize();
        if (activityCount == 1 || (activityCount > 1 && historySize > 0)) {
            mDefaultActivityButton.setVisibility(VISIBLE);
            ResolveInfo activity = mAdapter.getDefaultActivity();
            PackageManager packageManager = getContext().getPackageManager();
            mDefaultActivityButtonImage.setImageDrawable(activity.loadIcon(packageManager));
            if (mDefaultActionButtonContentDescription != 0) {
                CharSequence label = activity.loadLabel(packageManager);
                String contentDescription = getContext().getString(
                        mDefaultActionButtonContentDescription, label);
                mDefaultActivityButton.setContentDescription(contentDescription);
            }
        } else {
            mDefaultActivityButton.setVisibility(View.GONE);
        }
        // Activity chooser content.
        if (mDefaultActivityButton.getVisibility() == VISIBLE) {
            mActivityChooserContent.setBackgroundDrawable(mActivityChooserContentBackground);
        } else {
            mActivityChooserContent.setBackgroundDrawable(null);
        }
    }

    /**
     * Interface implementation to avoid publishing them in the APIs.
     */
    private class Callbacks implements AdapterView.OnItemClickListener,
            View.OnClickListener, View.OnLongClickListener, PopupWindow.OnDismissListener {

        Callbacks() {
        }

        // AdapterView#OnItemClickListener
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ActivityChooserViewAdapter adapter = (ActivityChooserViewAdapter) parent.getAdapter();
            final int itemViewType = adapter.getItemViewType(position);
            switch (itemViewType) {
                case ActivityChooserViewAdapter.ITEM_VIEW_TYPE_FOOTER: {
                    showPopupUnchecked(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                } break;
                case ActivityChooserViewAdapter.ITEM_VIEW_TYPE_ACTIVITY: {
                    dismissPopup();
                    if (mIsSelectingDefaultActivity) {
                        // The item at position zero is the default already.
                        if (position > 0) {
                            mAdapter.getDataModel().setDefaultActivity(position);
                        }
                    } else {
                        // If the default target is not shown in the list, the first
                        // item in the model is default action => adjust index
                        position = mAdapter.getShowDefaultActivity() ? position : position + 1;
                        Intent launchIntent = mAdapter.getDataModel().chooseActivity(position);
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            getContext().startActivity(launchIntent);
                        }
                    }
                } break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        // View.OnClickListener
        @Override
        public void onClick(View view) {
            if (view == mDefaultActivityButton) {
                dismissPopup();
                ResolveInfo defaultActivity = mAdapter.getDefaultActivity();
                final int index = mAdapter.getDataModel().getActivityIndex(defaultActivity);
                Intent launchIntent = mAdapter.getDataModel().chooseActivity(index);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    getContext().startActivity(launchIntent);
                }
            } else if (view == mExpandActivityOverflowButton) {
                mIsSelectingDefaultActivity = false;
                showPopupUnchecked(mInitialActivityCount);
            } else {
                throw new IllegalArgumentException();
            }
        }

        // OnLongClickListener#onLongClick
        @Override
        public boolean onLongClick(View view) {
            if (view == mDefaultActivityButton) {
                if (mAdapter.getCount() > 0) {
                    mIsSelectingDefaultActivity = true;
                    showPopupUnchecked(mInitialActivityCount);
                }
            } else {
                throw new IllegalArgumentException();
            }
            return true;
        }

        // PopUpWindow.OnDismissListener#onDismiss
        @Override
        public void onDismiss() {
            notifyOnDismissListener();
            if (mProvider != null) {
                mProvider.subUiVisibilityChanged(false);
            }
        }

        private void notifyOnDismissListener() {
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss();
            }
        }
    }

    /**
     * Adapter for backing the list of activities shown in the popup.
     */
    private class ActivityChooserViewAdapter extends BaseAdapter {

        public static final int MAX_ACTIVITY_COUNT_UNLIMITED = Integer.MAX_VALUE;

        public static final int MAX_ACTIVITY_COUNT_DEFAULT = 4;

        private static final int ITEM_VIEW_TYPE_ACTIVITY = 0;

        private static final int ITEM_VIEW_TYPE_FOOTER = 1;

        private static final int ITEM_VIEW_TYPE_COUNT = 3;

        private ActivityChooserModel mDataModel;

        private int mMaxActivityCount = MAX_ACTIVITY_COUNT_DEFAULT;

        private boolean mShowDefaultActivity;

        private boolean mHighlightDefaultActivity;

        private boolean mShowFooterView;

        ActivityChooserViewAdapter() {
        }

        public void setDataModel(ActivityChooserModel dataModel) {
            ActivityChooserModel oldDataModel = mAdapter.getDataModel();
            if (oldDataModel != null && isShown()) {
                oldDataModel.unregisterObserver(mModelDataSetObserver);
            }
            mDataModel = dataModel;
            if (dataModel != null && isShown()) {
                dataModel.registerObserver(mModelDataSetObserver);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (mShowFooterView && position == getCount() - 1) {
                return ITEM_VIEW_TYPE_FOOTER;
            } else {
                return ITEM_VIEW_TYPE_ACTIVITY;
            }
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_VIEW_TYPE_COUNT;
        }

        @Override
        public int getCount() {
            int activityCount = mDataModel.getActivityCount();
            if (!mShowDefaultActivity && mDataModel.getDefaultActivity() != null) {
                activityCount--;
            }
            int count = Math.min(activityCount, mMaxActivityCount);
            if (mShowFooterView) {
                count++;
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            final int itemViewType = getItemViewType(position);
            switch (itemViewType) {
                case ITEM_VIEW_TYPE_FOOTER:
                    return null;
                case ITEM_VIEW_TYPE_ACTIVITY:
                    if (!mShowDefaultActivity && mDataModel.getDefaultActivity() != null) {
                        position++;
                    }
                    return mDataModel.getActivity(position);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int itemViewType = getItemViewType(position);
            switch (itemViewType) {
                case ITEM_VIEW_TYPE_FOOTER:
                    if (convertView == null || convertView.getId() != ITEM_VIEW_TYPE_FOOTER) {
                        convertView = LayoutInflater.from(getContext()).inflate(
                                R.layout.abc_activity_chooser_view_list_item, parent, false);
                        convertView.setId(ITEM_VIEW_TYPE_FOOTER);
                        TextView titleView = (TextView) convertView.findViewById(R.id.title);
                        titleView.setText(getContext().getString(
                                R.string.abc_activity_chooser_view_see_all));
                    }
                    return convertView;
                case ITEM_VIEW_TYPE_ACTIVITY:
                    if (convertView == null || convertView.getId() != R.id.list_item) {
                        convertView = LayoutInflater.from(getContext()).inflate(
                                R.layout.abc_activity_chooser_view_list_item, parent, false);
                    }
                    PackageManager packageManager = getContext().getPackageManager();
                    // Set the icon
                    ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
                    ResolveInfo activity = (ResolveInfo) getItem(position);
                    iconView.setImageDrawable(activity.loadIcon(packageManager));
                    // Set the title.
                    TextView titleView = (TextView) convertView.findViewById(R.id.title);
                    titleView.setText(activity.loadLabel(packageManager));
                    // Highlight the default.
                    if (mShowDefaultActivity && position == 0 && mHighlightDefaultActivity) {
                        convertView.setActivated(true);
                    } else {
                        convertView.setActivated(false);
                    }
                    return convertView;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public int measureContentWidth() {
            // The user may have specified some of the target not to be shown but we
            // want to measure all of them since after expansion they should fit.
            final int oldMaxActivityCount = mMaxActivityCount;
            mMaxActivityCount = MAX_ACTIVITY_COUNT_UNLIMITED;

            int contentWidth = 0;
            View itemView = null;

            final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            final int count = getCount();

            for (int i = 0; i < count; i++) {
                itemView = getView(i, itemView, null);
                itemView.measure(widthMeasureSpec, heightMeasureSpec);
                contentWidth = Math.max(contentWidth, itemView.getMeasuredWidth());
            }

            mMaxActivityCount = oldMaxActivityCount;

            return contentWidth;
        }

        public void setMaxActivityCount(int maxActivityCount) {
            if (mMaxActivityCount != maxActivityCount) {
                mMaxActivityCount = maxActivityCount;
                notifyDataSetChanged();
            }
        }

        public ResolveInfo getDefaultActivity() {
            return mDataModel.getDefaultActivity();
        }

        public void setShowFooterView(boolean showFooterView) {
            if (mShowFooterView != showFooterView) {
                mShowFooterView = showFooterView;
                notifyDataSetChanged();
            }
        }

        public int getActivityCount() {
            return mDataModel.getActivityCount();
        }

        public int getHistorySize() {
            return mDataModel.getHistorySize();
        }

        public ActivityChooserModel getDataModel() {
            return mDataModel;
        }

        public void setShowDefaultActivity(boolean showDefaultActivity,
                boolean highlightDefaultActivity) {
            if (mShowDefaultActivity != showDefaultActivity
                    || mHighlightDefaultActivity != highlightDefaultActivity) {
                mShowDefaultActivity = showDefaultActivity;
                mHighlightDefaultActivity = highlightDefaultActivity;
                notifyDataSetChanged();
            }
        }

        public boolean getShowDefaultActivity() {
            return mShowDefaultActivity;
        }
    }

    /**
     * Allows us to set the background using TintTypedArray
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static class InnerLayout extends LinearLayout {

        private static final int[] TINT_ATTRS = {
                android.R.attr.background
        };

        public InnerLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS);
            setBackgroundDrawable(a.getDrawable(0));
            a.recycle();
        }
    }
}

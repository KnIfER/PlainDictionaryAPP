/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.R;
import androidx.core.graphics.ColorUtils;

/**
 * @hide
 */
@RestrictTo(LIBRARY)
public class ThemeUtils {
    private static final String TAG = "ThemeUtils";

    private static final ThreadLocal<TypedValue> TL_TYPED_VALUE = new ThreadLocal<>();

    static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    static final int[] FOCUSED_STATE_SET = new int[]{android.R.attr.state_focused};
    static final int[] ACTIVATED_STATE_SET = new int[]{android.R.attr.state_activated};
    static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
    static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};
    static final int[] SELECTED_STATE_SET = new int[]{android.R.attr.state_selected};
    static final int[] NOT_PRESSED_OR_FOCUSED_STATE_SET = new int[]{
            -android.R.attr.state_pressed, -android.R.attr.state_focused};
    static final int[] EMPTY_STATE_SET = new int[0];

    private static final int[] TEMP_ARRAY = new int[1];

    /**
     * Creates a color state list from the provided colors.
     *
     * @param textColor Regular text color.
     * @param disabledTextColor Disabled text color.
     * @return Color state list.
     */
    @NonNull
    public static ColorStateList createDisabledStateList(int textColor, int disabledTextColor) {
        // Now create a new ColorStateList with the default color, and the new disabled
        // color
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        // Disabled state
        states[i] = DISABLED_STATE_SET;
        colors[i] = disabledTextColor;
        i++;

        // Default state
        states[i] = EMPTY_STATE_SET;
        colors[i] = textColor;
        i++;

        return new ColorStateList(states, colors);
    }

    /**
     * Resolves the color from the provided theme attribute.
     *
     * @param context Context. Must be non-null.
     * @param attr Theme attribute for resolving color.
     * @return Resolved color.
     */
    public static int getThemeAttrColor(@NonNull Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    /**
     * Resolves the color state list from the provided theme attribute.
     *
     * @param context Context. Must be non-null.
     * @param attr Theme attribute for resolving color state list.
     * @return Resolved color state list.
     */
    @Nullable
    public static ColorStateList getThemeAttrColorStateList(@NonNull Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            return a.getColorStateList(0);
        } finally {
            a.recycle();
        }
    }

    /**
     * Resolves the disabled color from the provided theme attribute.
     *
     * @param context Context. Must be non-null.
     * @param attr Theme attribute for resolving disabled color.
     * @return Resolved disabled color.
     */
    public static int getDisabledThemeAttrColor(@NonNull Context context, int attr) {
        final ColorStateList csl = getThemeAttrColorStateList(context, attr);
        if (csl != null && csl.isStateful()) {
            // If the CSL is stateful, we'll assume it has a disabled state and use it
            return csl.getColorForState(DISABLED_STATE_SET, csl.getDefaultColor());
        } else {
            // Else, we'll generate the color using disabledAlpha from the theme

            final TypedValue tv = getTypedValue();
            // Now retrieve the disabledAlpha value from the theme
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, tv, true);
            final float disabledAlpha = tv.getFloat();

            return getThemeAttrColor(context, attr, disabledAlpha);
        }
    }

    private static TypedValue getTypedValue() {
        TypedValue typedValue = TL_TYPED_VALUE.get();
        if (typedValue == null) {
            typedValue = new TypedValue();
            TL_TYPED_VALUE.set(typedValue);
        }
        return typedValue;
    }

    static int getThemeAttrColor(@NonNull Context context, int attr, float alpha) {
        final int color = getThemeAttrColor(context, attr);
        final int originalAlpha = Color.alpha(color);
        return ColorUtils.setAlphaComponent(color, Math.round(originalAlpha * alpha));
    }

    /**
     * Checks that the specific view (which should be an AppCompat widget) is
     * using a {@link Context} that is an AppCompat theme or its descendant.
     */
    public static void checkAppCompatTheme(@NonNull View view, @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(R.styleable.AppCompatTheme);

        try {
            // Same check as in AppCompatDelegateImpl - do not allow using AppCompat widgets
            // without a top-level AppCompat theme (or its descendant). For now flag this as
            // an error-level log message.
            if (!a.hasValue(R.styleable.AppCompatTheme_windowActionBar)) {
                android.util.Log.e(TAG, "View " + view.getClass()
                        + " is an AppCompat widget that can only be used with a "
                        + "Theme.AppCompat theme (or descendant).");
            }
        } finally {
            a.recycle();
        }
    }

    private ThemeUtils() {
    }
}

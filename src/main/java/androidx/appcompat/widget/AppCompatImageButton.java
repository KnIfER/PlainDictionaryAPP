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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.R;
import androidx.core.view.TintableBackgroundView;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.TintableImageSourceView;

/**
 * A {@link ImageButton} which supports compatible features on older versions of the platform,
 * including:
 * <ul>
 *     <li>Allows dynamic tint of its background via the background tint methods in
 *     {@link androidx.core.view.ViewCompat}.</li>
 *     <li>Allows setting of the background tint using {@link R.attr#backgroundTint} and
 *     {@link R.attr#backgroundTintMode}.</li>
 *     <li>Allows dynamic tint of its image via the image tint methods in
 *     {@link ImageViewCompat}.</li>
 *     <li>Allows setting of the image tint using {@link R.attr#tint} and
 *     {@link R.attr#tintMode}.</li>
 * </ul>
 *
 * <p>This will automatically be used when you use {@link ImageButton} in your layouts
 * and the top-level activity / dialog is provided by
 * <a href="{@docRoot}topic/libraries/support-library/packages.html#v7-appcompat">appcompat</a>.
 * You should only need to manually use this class when writing custom views.</p>
 */
public class AppCompatImageButton extends ImageButton implements TintableBackgroundView,
        TintableImageSourceView {

    private final AppCompatBackgroundHelper mBackgroundTintHelper;
    private final AppCompatImageHelper mImageHelper;

    public AppCompatImageButton(@NonNull Context context) {
        this(context, null);
    }

    public AppCompatImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public AppCompatImageButton(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);

        ThemeUtils.checkAppCompatTheme(this, getContext());

        mBackgroundTintHelper = new AppCompatBackgroundHelper(this);
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);

        mImageHelper = new AppCompatImageHelper(this);
        mImageHelper.loadFromAttributes(attrs, defStyleAttr);
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        // Intercept this call and instead retrieve the Drawable via the image helper
        mImageHelper.setImageResource(resId);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mImageHelper != null) {
            mImageHelper.applySupportImageTint();
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (mImageHelper != null) {
            mImageHelper.applySupportImageTint();
        }
    }

    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
        if (mImageHelper != null) {
            mImageHelper.applySupportImageTint();
        }
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        }
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable background) {
        super.setBackgroundDrawable(background);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundDrawable(background);
        }
    }

    /**
     * This should be accessed via
     * {@link androidx.core.view.ViewCompat#setBackgroundTintList(android.view.View, ColorStateList)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.setSupportBackgroundTintList(tint);
        }
    }

    /**
     * This should be accessed via
     * {@link androidx.core.view.ViewCompat#getBackgroundTintList(android.view.View)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    @Nullable
    public ColorStateList getSupportBackgroundTintList() {
        return mBackgroundTintHelper != null
                ? mBackgroundTintHelper.getSupportBackgroundTintList() : null;
    }

    /**
     * This should be accessed via
     * {@link androidx.core.view.ViewCompat#setBackgroundTintMode(android.view.View, PorterDuff.Mode)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.setSupportBackgroundTintMode(tintMode);
        }
    }

    /**
     * This should be accessed via
     * {@link androidx.core.view.ViewCompat#getBackgroundTintMode(android.view.View)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    @Nullable
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        return mBackgroundTintHelper != null
                ? mBackgroundTintHelper.getSupportBackgroundTintMode() : null;
    }
    /**
     * This should be accessed via
     * {@link androidx.core.widget.ImageViewCompat#setImageTintList(ImageView, ColorStateList)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    public void setSupportImageTintList(@Nullable ColorStateList tint) {
        if (mImageHelper != null) {
            mImageHelper.setSupportImageTintList(tint);
        }
    }

    /**
     * This should be accessed via
     * {@link androidx.core.widget.ImageViewCompat#getImageTintList(ImageView)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    @Nullable
    public ColorStateList getSupportImageTintList() {
        return mImageHelper != null
                ? mImageHelper.getSupportImageTintList() : null;
    }

    /**
     * This should be accessed via
     * {@link androidx.core.widget.ImageViewCompat#setImageTintMode(ImageView, PorterDuff.Mode)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    public void setSupportImageTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mImageHelper != null) {
            mImageHelper.setSupportImageTintMode(tintMode);
        }
    }

    /**
     * This should be accessed via
     * {@link androidx.core.widget.ImageViewCompat#getImageTintMode(ImageView)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Override
    @Nullable
    public PorterDuff.Mode getSupportImageTintMode() {
        return mImageHelper != null
                ? mImageHelper.getSupportImageTintMode() : null;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySupportBackgroundTint();
        }
        if (mImageHelper != null) {
            mImageHelper.applySupportImageTint();
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return mImageHelper.hasOverlappingRendering() && super.hasOverlappingRendering();
    }
}

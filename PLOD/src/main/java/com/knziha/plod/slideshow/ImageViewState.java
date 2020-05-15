package com.knziha.plod.slideshow;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Wraps the scale, center and orientation of a displayed image for easy restoration on screen rotate.
 */
@SuppressWarnings("WeakerAccess")
public class ImageViewState implements Serializable {

    private final float scale;

    private final float centerX;

    private final float centerY;

    private final float rotation;

    public ImageViewState(float scale, @NonNull PointF center, float orientation) {
        this.scale = scale;
        this.centerX = center.x;
        this.centerY = center.y;
        this.rotation = orientation;
    }

    public float getScale() {
        return scale;
    }

    @NonNull public PointF getCenter() {
        return new PointF(centerX, centerY);
    }

    public float getRotation() {
        return rotation;
    }

}

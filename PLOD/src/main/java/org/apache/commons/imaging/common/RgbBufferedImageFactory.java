/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.imaging.common;

import android.graphics.Bitmap;

import org.apache.commons.imaging.BufferedImage;

public class RgbBufferedImageFactory implements BufferedImageFactory {
    @Override
    public BufferedImage getColorBufferedImage(final int width, final int height,
            final boolean hasAlpha) {
        if (hasAlpha) {
            return new BufferedImage(Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888));
        }
		return new BufferedImage(Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565));
    }

    @Override
    public BufferedImage getGrayscaleBufferedImage(final int width, final int height,
            final boolean hasAlpha) {
        // always use color.
        return getColorBufferedImage(width, height, hasAlpha);
    }
}

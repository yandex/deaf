/**
 * Copyright 2015 YA LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.yandex.subtitles.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class BitmapUtils {

    /**
     * The most useful answer about text drawing ever. http://stackoverflow.com/a/32081250
     */
    @Nullable
    public static Bitmap createTypefaceBitmap(final Context context, @NonNull final String text,
                                              final int color, final float textSizePx) {
        final Typeface robotoMedium = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(robotoMedium);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(textSizePx);

        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Bitmap bitmap = null;
        if (!bounds.isEmpty()) {
            final int width = bounds.width();
            final int height = bounds.height();

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

            final float x = bounds.left;
            final float y = height - bounds.bottom;

            final Canvas canvas = new Canvas(bitmap);
            canvas.drawText(text, x, y, paint);
        }

        return bitmap;
    }

    private BitmapUtils() {
    }

}

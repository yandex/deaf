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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import ru.yandex.subtitles.R;

public final class ViewUtils {

    @SuppressWarnings("unchecked")
    public static <V extends View> V findView(final Activity activity, final int id) {
        V v = null;
        try {
            v = (V) activity.findViewById(id);
        } catch (final ClassCastException cce) {
            // Ignore it
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    public static <V extends View> V findView(final View root, final int id) {
        V v = null;
        try {
            v = (V) root.findViewById(id);
        } catch (final ClassCastException cce) {
            // Ignore it
        }
        return v;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackgroundCompat(final View v, final Drawable drawable) {
        if (ApplicationUtils.hasJellyBean()) {
            v.setBackground(drawable);
        } else {
            v.setBackgroundDrawable(drawable);
        }
    }

    public static void showSoftwareKeyboard(@Nullable final View v) {
        showSoftwareKeyboard(v, false);
    }

    public static void showSoftwareKeyboard(@Nullable final View v, final boolean forceShow) {
        if (v != null) {
            final InputMethodManager imManager = getInputMethodManager(v.getContext());
            if (imManager != null) {
                final int flags = (forceShow ? InputMethodManager.SHOW_FORCED : InputMethodManager.SHOW_IMPLICIT);
                imManager.showSoftInput(v, flags);
            }
        }
    }

    public static boolean hideSoftwareKeyboard(@Nullable final View v) {
        boolean isHided = false;
        if (v != null) {
            final InputMethodManager imManager = getInputMethodManager(v.getContext());
            if (imManager != null) {
                isHided = imManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }
        }
        return isHided;
    }

    private static InputMethodManager getInputMethodManager(final Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static int getNavigationDrawerWidth(final Resources resources) {
        return resources.getDimensionPixelSize(R.dimen.navigation_drawer_width);
    }

    private ViewUtils() {
    }

}

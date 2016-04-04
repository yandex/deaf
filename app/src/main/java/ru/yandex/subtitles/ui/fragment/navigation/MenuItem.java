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
package ru.yandex.subtitles.ui.fragment.navigation;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public class MenuItem {

    private int mId;
    private int mTitleResId;
    private int mDrawableResId;

    public MenuItem(final int id, @StringRes final int titleResId) {
        this(id, titleResId, 0);
    }

    public MenuItem(final int id, @StringRes final int titleResId, @DrawableRes final int drawableResId) {
        mId = id;
        mTitleResId = titleResId;
        mDrawableResId = drawableResId;
    }

    public int getId() {
        return mId;
    }

    @StringRes
    public int getTitleResId() {
        return mTitleResId;
    }

    @DrawableRes
    public int getDrawableResId() {
        return mDrawableResId;
    }

}

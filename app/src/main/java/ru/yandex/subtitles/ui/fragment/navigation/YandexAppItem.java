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

public class YandexAppItem {

    private int mIconResId;
    private int mTitleResId;
    private int mPackageNameResId;

    public YandexAppItem(@DrawableRes final int iconResId, @StringRes final int titleResId,
                         @StringRes final int packageNameResId) {
        mIconResId = iconResId;
        mTitleResId = titleResId;
        mPackageNameResId = packageNameResId;
    }

    @DrawableRes
    public int getIconResId() {
        return mIconResId;
    }

    @StringRes
    public int getTitleResId() {
        return mTitleResId;
    }

    @StringRes
    public int getPackageNameResId() {
        return mPackageNameResId;
    }

}

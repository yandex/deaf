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
package ru.yandex.subtitles.ui.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * NoRemainingSpaceListView provides the ability to stretch footer view to fit
 * the whole empty space at the bottom if there is only few items. Otherwise
 * footer list view draws as usually.
 * Please make sure that your first footer view is bottom-aligned.
 */
public class NoRemainingSpaceListView extends ListView {

    private int mOldCount = 0;

    public NoRemainingSpaceListView(final Context context) {
        super(context);
    }

    public NoRemainingSpaceListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public NoRemainingSpaceListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        checkOrRecalculateFooterHeight();
        super.onDraw(canvas);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("NewApi")
    private void checkOrRecalculateFooterHeight() {
        int count = getCount();
        if (count != mOldCount) {
            mOldCount = count;
            if (count > getChildCount()) {
                // Fast return if not all items are visible
                return;
            }

            // Calculate headers height
            final int headersCount = getHeaderViewsCount();
            final boolean hasHeaderDividers = isPreKitKat() || areHeaderDividersEnabled();
            final int headersHeight = calculateChildHeight(0, headersCount, hasHeaderDividers);

            // Calculate footers height (except first footer)
            final int footersCount = getFooterViewsCount();
            final boolean hasFooterDividers = isPreKitKat() || areFooterDividersEnabled();
            final int firstFooterViewPosition = count - footersCount;
            final int footersHeight = calculateChildHeight(firstFooterViewPosition + 1, count, hasFooterDividers);

            // Calculate items height
            final int itemsHeight = calculateChildHeight(headersCount, firstFooterViewPosition, true);

            final View firstFooterView = getChildAt(firstFooterViewPosition);
            int firstFooterHeight = firstFooterView.getMeasuredHeight();
            int firstFooterNewHeight = getHeight() - headersHeight - itemsHeight - footersHeight;
            if (firstFooterHeight < firstFooterNewHeight) {
                ViewGroup.LayoutParams params = firstFooterView.getLayoutParams();
                params.height = firstFooterNewHeight;
                firstFooterView.setLayoutParams(params);
            }
        }
    }

    private boolean isPreKitKat() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT);
    }

    private int calculateChildHeight(final int start, final int end, final boolean hasDividers) {
        int dividerHeight = getDividerHeight();
        if (!hasDividers) {
            dividerHeight = 0;
        }

        int height = 0;
        for (int i = start; i < end; i++) {
            View child = getChildAt(i);
            if (child != null) {
                height += child.getMeasuredHeight() + dividerHeight;
            }
        }
        return height;
    }

}

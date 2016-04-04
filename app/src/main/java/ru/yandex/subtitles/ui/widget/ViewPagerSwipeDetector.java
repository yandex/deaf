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

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;

public class ViewPagerSwipeDetector implements ViewPager.OnPageChangeListener {

    public interface OnViewPagerSwipeListener {

        void onPageSelected(final int position);

        void onPageSwipedLeft();

        void onPageSwipedRight();

    }

    private int mPosition = 0;
    private boolean mDragged = false;

    private OnViewPagerSwipeListener mSwipeListener;

    public ViewPagerSwipeDetector(@NonNull final OnViewPagerSwipeListener swipeListener) {
        mSwipeListener = swipeListener;
    }

    @Override
    public void onPageSelected(final int position) {
        mSwipeListener.onPageSelected(position);
        if (mDragged) {
            if (mPosition < position) {
                mSwipeListener.onPageSwipedRight();
            } else if (mPosition > position) {
                mSwipeListener.onPageSwipedLeft();
            }
        }
        mPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        mDragged = (state != ViewPager.SCROLL_STATE_IDLE &&
                (mDragged || state == ViewPager.SCROLL_STATE_DRAGGING));
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset,
                               final int positionOffsetPixels) {
    }

}

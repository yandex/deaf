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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class InterceptableToolbar extends Toolbar {

    private OnTouchListener mIntersectionTouchListener;

    public InterceptableToolbar(final Context context) {
        this(context, null);
    }

    public InterceptableToolbar(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.toolbarStyle);
    }

    public InterceptableToolbar(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setIntersectionTouchListener(final OnTouchListener touchListener) {
        mIntersectionTouchListener = touchListener;
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        super.onTouchEvent(event);
        return (mIntersectionTouchListener != null && mIntersectionTouchListener.onTouch(this, event));
    }

}

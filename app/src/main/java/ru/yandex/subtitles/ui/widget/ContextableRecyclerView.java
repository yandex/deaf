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
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;

public class ContextableRecyclerView extends RecyclerView {

    private RecyclerContextMenuInfo mRecyclerContextMenuInfo;

    public ContextableRecyclerView(final Context context) {
        this(context, null);
    }

    public ContextableRecyclerView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContextableRecyclerView(final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void createRecyclerContextMenuInfo(final Object e) {
        mRecyclerContextMenuInfo = new RecyclerContextMenuInfo(e);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mRecyclerContextMenuInfo;
    }

    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

        private final Object mData;

        public RecyclerContextMenuInfo(@NonNull final Object data) {
            mData = data;
        }

        @NonNull
        public Object getData() {
            return mData;
        }

    }

}
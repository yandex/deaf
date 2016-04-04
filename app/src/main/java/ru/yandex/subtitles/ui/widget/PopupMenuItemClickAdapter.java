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
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;

public class PopupMenuItemClickAdapter<E> implements PopupMenu.OnMenuItemClickListener {

    public interface OnPopupMenuItemClickListener<E> {

        boolean onPopupMenuItemClick(final MenuItem item, final E entity);

    }

    private final E mEntity;
    private final OnPopupMenuItemClickListener<E> mListener;

    public PopupMenuItemClickAdapter(@NonNull final E entity,
                                     @NonNull final OnPopupMenuItemClickListener<E> listener) {
        mEntity = entity;
        mListener = listener;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        return mListener.onPopupMenuItemClick(item, mEntity);
    }

}

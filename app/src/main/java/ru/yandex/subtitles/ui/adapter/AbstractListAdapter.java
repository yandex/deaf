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
package ru.yandex.subtitles.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Base adapter class that simplifies working with {@link android.widget.ListView}.
 */
public abstract class AbstractListAdapter<E> extends BaseAdapter {

    private final List<E> mItems;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public AbstractListAdapter(final Context context) {
        mContext = context;
        mItems = new ArrayList<E>();
        mInflater = LayoutInflater.from(mContext);
    }

    public Context getContext() {
        return mContext;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    public void addAll(@NonNull final List<E> objects) {
        mItems.addAll(objects);
    }

    public void add(@NonNull final E object) {
        mItems.add(object);
    }

    public void remove(@NonNull final E object) {
        mItems.remove(object);
    }

    public void clear() {
        mItems.clear();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public E getItem(final int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(final int pos) {
        return pos;
    }

    public List<E> getItems() {
        return mItems;
    }

}

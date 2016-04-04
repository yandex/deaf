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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Base adapter class that simplifies working with {@link android.support.v4.view.ViewPager}.
 */
public abstract class AbstractFragmentViewPagerAdapter<E> extends FragmentPagerAdapter {

    private final Context mContext;
    private final List<E> mData = new ArrayList<E>();

    public AbstractFragmentViewPagerAdapter(@NonNull final Context context,
                                            @NonNull final FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = context;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    public void setData(@NonNull final List<E> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public E getData(final int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

}

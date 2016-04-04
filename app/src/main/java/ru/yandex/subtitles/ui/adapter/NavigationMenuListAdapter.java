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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.fragment.navigation.MenuItem;
import ru.yandex.subtitles.utils.ViewUtils;

public class NavigationMenuListAdapter extends AbstractListAdapter<MenuItem> {

    public NavigationMenuListAdapter(final Context context) {
        super(context);
    }

    @Override
    public long getItemId(final int pos) {
        return getItem(pos).getId();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = getInflater().inflate(R.layout.list_item_navigation_menu, parent, false);

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bindView(getItem(position));

        return convertView;
    }

    private static class ViewHolder {

        private final TextView mTitleView;
        private final ImageView mImageView;

        public ViewHolder(final View root) {
            mTitleView = ViewUtils.findView(root, R.id.title);
            mImageView = ViewUtils.findView(root, R.id.icon);
        }

        public void bindView(final MenuItem item) {
            mTitleView.setText(item.getTitleResId());
            mImageView.setImageResource(item.getDrawableResId());
        }

    }

}

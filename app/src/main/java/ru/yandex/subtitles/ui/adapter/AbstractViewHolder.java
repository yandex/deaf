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
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.yandex.subtitles.utils.ViewUtils;

/**
 * Base {@link android.support.v7.widget.RecyclerView.ViewHolder} class that works in conjunction
 * with {@link AbstractRecyclerViewAdapter}.
 */
public abstract class AbstractViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    /* package */ interface OnViewHolderClickListener {

        void onViewHolderClick(final View view, final int position);

    }

    /* package */ static final int DEFAULT_CLICKABLE_VIEW_ID = 0;
    /* package */ static final int[] DEFAULT_CLICKABLE_VIEW_IDS = new int[] { DEFAULT_CLICKABLE_VIEW_ID };

    private OnViewHolderClickListener mOnViewHolderClickListener;

    public AbstractViewHolder(final View itemView) {
        super(itemView);
    }

    public Context getContext() {
        return itemView.getContext();
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public void setEnabled(final boolean enabled, final int[] clickableViewIds) {
        for (final int clickableViewId : clickableViewIds) {
            final View clickableView;
            if (clickableViewId == DEFAULT_CLICKABLE_VIEW_ID) {
                clickableView = itemView;
            } else {
                clickableView = findView(clickableViewId);
            }

            if (clickableView != null) {
                clickableView.setEnabled(enabled);
                clickableView.setOnClickListener(enabled ? this : null);
                clickableView.setClickable(enabled);
            }
        }
    }

    public void setOnViewHolderClickListener(final OnViewHolderClickListener onViewHolderClickListener) {
        mOnViewHolderClickListener = onViewHolderClickListener;
    }

    @Override
    public void onClick(final View v) {
        if (mOnViewHolderClickListener != null) {
            mOnViewHolderClickListener.onViewHolderClick(v, getAdapterPosition());
        }
    }

    public <V extends View> V findView(final int id) {
        return ViewUtils.findView(itemView, id);
    }

    public void onViewRecycled() {
    }

}
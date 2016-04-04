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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.data.Phrase;

public class QuickResponsesAdapter extends AbstractRecyclerViewAdapter<Phrase, QuickResponsesAdapter.ViewHolder> {

    private int mItemHeight = 0;

    public QuickResponsesAdapter(final Context context) {
        super(context);
    }

    public void setItemHeight(final int itemHeight) {
        mItemHeight = itemHeight;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = getLayoutInflater().inflate(R.layout.list_item_quick_response, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        holder.bind(getItem(position), mItemHeight);
    }

    /* package */ static class ViewHolder extends AbstractViewHolder {

        private TextView mPhraseView;

        public ViewHolder(final View itemView) {
            super(itemView);
            mPhraseView = findView(R.id.phrase);
        }

        public void bind(@NonNull final Phrase phrase, final int itemHeight) {
            final ViewGroup.LayoutParams lp = mPhraseView.getLayoutParams();
            lp.height = itemHeight;
            mPhraseView.setLayoutParams(lp);

            mPhraseView.setText(phrase.getText());
        }

    }

}
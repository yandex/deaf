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
import android.widget.TextView;

import ru.yandex.subtitles.R;

public class AnswersAdapter extends AbstractRecyclerViewAdapter<String, AnswersAdapter.ViewHolder> {

    public AnswersAdapter(final Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = getLayoutInflater().inflate(R.layout.list_item_answer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        holder.bindAnswer(getItem(position));
    }

    /* package */ static class ViewHolder extends AbstractViewHolder {

        private final TextView mAnswerView;

        public ViewHolder(final View itemView) {
            super(itemView);
            mAnswerView = findView(R.id.answer);
        }

        public void bindAnswer(final String answer) {
            mAnswerView.setText(answer);
        }

    }

}

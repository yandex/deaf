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
package ru.yandex.subtitles.ui.widget.keyboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.ui.adapter.AbstractRecyclerViewAdapter;
import ru.yandex.subtitles.ui.adapter.QuickResponsesAdapter;
import ru.yandex.subtitles.ui.widget.NoFadeItemAnimator;
import ru.yandex.subtitles.utils.ViewUtils;

public class QuickResponsesPopupWindow extends OverKeyboardPopupWindow {

    public QuickResponsesPopupWindow(final Context context, @NonNull final View rootView) {
        super(context, rootView);
    }

    private QuickResponsesAdapter mAdapter;

    @NonNull
    @Override
    public View onCreateView(final LayoutInflater inflater) {
        return inflater.inflate(R.layout.popup_quick_responses, null, false);
    }

    @Override
    public void onViewCreated(final View view) {
        final Context context = getContext();

        mAdapter = new QuickResponsesAdapter(context);

        final RecyclerView recyclerView = ViewUtils.findView(view, R.id.recycler);
        final int spanCount = context.getResources().getInteger(R.integer.quick_responses_span_count);
        recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));
        recyclerView.setItemAnimator(new NoFadeItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    public void setOnItemClickListener(final AbstractRecyclerViewAdapter.OnItemClickListener<Phrase> listener) {
        mAdapter.setOnItemClickListener(listener);
    }
    
    public void setQuickResponses(@NonNull final List<Phrase> quickResponses) {
        mAdapter.setItems(quickResponses);
    }

    @Override
    public void showAtBottom() {
        super.showAtBottom();
        final int rowCount = getContext().getResources().getInteger(R.integer.quick_responses_row_count);
        final int itemHeight = getKeyboardHeight() / rowCount;
        mAdapter.setItemHeight(itemHeight);
    }

}

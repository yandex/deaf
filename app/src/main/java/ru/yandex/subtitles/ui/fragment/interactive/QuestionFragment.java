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
package ru.yandex.subtitles.ui.fragment.interactive;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Question;
import ru.yandex.subtitles.ui.adapter.AbstractRecyclerViewAdapter;
import ru.yandex.subtitles.ui.adapter.AnswersAdapter;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.ui.widget.NoFadeItemAnimator;

public class QuestionFragment extends AbstractFragment<QuestionFragment.OnAnswerClickListener>
        implements AbstractRecyclerViewAdapter.OnItemClickListener<String> {

    public interface OnAnswerClickListener {

        void onAnswerClick(@NonNull final Question question);

    }

    public static final String TAG = "QuestionFragment";

    private static final String EXTRA_QUESTION = "question";

    public static QuestionFragment newInstance(@NonNull final Question question) {
        final QuestionFragment fragment = new QuestionFragment();

        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_QUESTION, question);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle args = getArguments();
        final Question question = args.getParcelable(EXTRA_QUESTION);
        if (question == null) {
            throw new IllegalArgumentException("Question should not be null");
        }

        final TextView titleView = findView(R.id.title);
        titleView.setText(question.getTitle());

        final Context context = getActivity();
        final RecyclerView recycler = findView(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setItemAnimator(new NoFadeItemAnimator());

        final AnswersAdapter adapter = new AnswersAdapter(context);
        adapter.setOnItemClickListener(this);
        final String[] options = getResources().getStringArray(question.getOptions());
        adapter.setItems(Arrays.asList(options));
        recycler.setAdapter(adapter);
    }

    @Override
    public void onItemClick(final View view, final int position, final String item) {
        final Bundle args = getArguments();
        final Question question = args.getParcelable(EXTRA_QUESTION);
        if (question == null) {
            throw new IllegalArgumentException("Question should not be null");
        }
        question.setAnswer(position);

        mController.onAnswerClick(question);
    }

}
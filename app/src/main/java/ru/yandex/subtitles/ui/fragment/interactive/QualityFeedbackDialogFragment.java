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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.analytics.Quality;
import ru.yandex.subtitles.ui.adapter.AbstractRecyclerViewAdapter;
import ru.yandex.subtitles.ui.adapter.QualityFeedbackAdapter;
import ru.yandex.subtitles.ui.fragment.AbstractAlertDialogFragment;
import ru.yandex.subtitles.ui.widget.NoFadeItemAnimator;

public class QualityFeedbackDialogFragment extends AbstractAlertDialogFragment
        implements AbstractRecyclerViewAdapter.OnItemClickListener<Quality> {

    public static final String TAG = "QualityFeedbackDialogFragment";

    public static QualityFeedbackDialogFragment newInstance() {
        return new QualityFeedbackDialogFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.quality_feedback_dialog_title);
        setNegative(R.string.close);
    }

    @NonNull
    @Override
    public View onCreateDialogView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_quality_feedback, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity();

        final QualityFeedbackAdapter adapter = new QualityFeedbackAdapter(context);
        adapter.setItems(createQualityItems());
        adapter.setOnItemClickListener(this);

        final RecyclerView recycler = findView(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setItemAnimator(new NoFadeItemAnimator());
        recycler.setAdapter(adapter);
    }

    @NonNull
    private List<Quality> createQualityItems() {
        final List<Quality> qualities = new ArrayList<Quality>();
        qualities.add(new Quality(R.string.quality_feedback_excellent_title, R.string.quality_feedback_excellent_subtitle));
        qualities.add(new Quality(R.string.quality_feedback_good_title, R.string.quality_feedback_good_subtitle));
        qualities.add(new Quality(R.string.quality_feedback_not_good_title, R.string.quality_feedback_not_good_subtitle));
        qualities.add(new Quality(R.string.quality_feedback_bad_title, R.string.quality_feedback_bad_subtitle));
        return qualities;
    }

    @Override
    public void onItemClick(final View view, final int position, final Quality item) {
        Analytics.sendQualityFeedback(getActivity(), item);
        dismissAllowingStateLoss();
    }

    @Override
    public void onNegativeButtonClick() {
        super.onNegativeButtonClick();
        dismissAllowingStateLoss();
    }

}
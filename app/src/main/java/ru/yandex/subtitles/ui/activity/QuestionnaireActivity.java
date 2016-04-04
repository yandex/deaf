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
package ru.yandex.subtitles.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.Preferences;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.analytics.Question;
import ru.yandex.subtitles.ui.fragment.interactive.QuestionFragment;
import ru.yandex.subtitles.utils.ApplicationUtils;

public class QuestionnaireActivity extends AbstractActivity
        implements FragmentManager.OnBackStackChangedListener,
        QuestionFragment.OnAnswerClickListener {

    private static final String KEY_TOTAL_QUESTIONS = "total_questions";
    private static final String KEY_ANSWERED = "answered";

    public static void start(final Context context) {
        final Intent intent = new Intent(context, QuestionnaireActivity.class);
        context.startActivity(intent);
    }

    private int mTotalQuestions = 4;
    private List<Question> mAnswered = new ArrayList<Question>();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        final Toolbar toolbar = findView(R.id.toolbar);
        if (ApplicationUtils.hasLollipop()) {
            toolbar.setElevation(0.f);
        }
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (savedInstanceState == null) {
            mTotalQuestions = 4;

            final Question impairmentQuestion = new Question();
            impairmentQuestion.setTitle(R.string.question_impairment);
            impairmentQuestion.setOptions(R.array.questionnaire_impairment);

            showFragmentNoAnimate(QuestionFragment.newInstance(impairmentQuestion), QuestionFragment.TAG);

        } else {
            mTotalQuestions = savedInstanceState.getInt(KEY_TOTAL_QUESTIONS);
            mAnswered = savedInstanceState.getParcelableArrayList(KEY_ANSWERED);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TOTAL_QUESTIONS, mTotalQuestions);
        outState.putParcelableArrayList(KEY_ANSWERED, new ArrayList<Question>(mAnswered));
    }

    @Override
    public void onAnswerClick(@NonNull final Question question) {
        mAnswered.add(question);

        final Question next;

        final int answer = question.getAnswer();
        switch (question.getTitle()) {
            case R.string.question_impairment:
                next = new Question();
                if (answer == 2) {
                    // You have no impairment
                    mTotalQuestions = 3;

                    next.setTitle(R.string.question_person);
                    next.setOptions(R.array.questionnaire_yesno);

                } else {
                    // You have full or partial impairment
                    mTotalQuestions = 4;

                    next.setTitle(R.string.question_lipreading);
                    next.setOptions(R.array.questionnaire_lipreading);
                }
                showQuestionFragment(next);
                break;

            case R.string.question_person:
                if (answer == 1) {
                    // You will not use the app
                    finishQuestionnaire();

                } else {
                    // You will use the app
                    mTotalQuestions = 3;

                    next = new Question();
                    next.setTitle(R.string.question_hearing2deaf);
                    next.setOptions(R.array.questionnaire_frequency);
                    showQuestionFragment(next);
                }
                break;

            case R.string.question_hearing2deaf:
                finishQuestionnaire();
                break;

            case R.string.question_lipreading:
                mTotalQuestions = 4;

                next = new Question();
                next.setTitle(R.string.question_articulation);
                next.setOptions(R.array.questionnaire_articulation);
                showQuestionFragment(next);
                break;

            case R.string.question_articulation:
                mTotalQuestions = 4;

                next = new Question();
                next.setTitle(R.string.question_deaf2hearing);
                next.setOptions(R.array.questionnaire_frequency);
                showQuestionFragment(next);
                break;

            case R.string.question_deaf2hearing:
                finishQuestionnaire();
                break;
        }
    }

    private void showQuestionFragment(@NonNull final Question question) {
        showFragment(QuestionFragment.newInstance(question), QuestionFragment.TAG);
    }

    private void finishQuestionnaire() {
        Analytics.onQuestionnaireCompleted(this, mAnswered);
        Preferences.getInstance().setFirstLaunch(false);
        finish();
    }

    @Override
    public void onBackPressedAction() {
        super.onBackPressedAction();
        mAnswered.remove(mAnswered.size() - 1);
    }

    @Override
    public void finish() {
        // It's not the better way to start next activity
        QuickStartActivity.start(this);
        super.finish();
    }

    @Override
    public void onBackStackChanged() {
        final int currentQuestion = getSupportFragmentManager().getBackStackEntryCount();
        if (currentQuestion == 1) {
            mTotalQuestions = 4;
        }
        setTitle(getString(R.string.questionnaire_progress, currentQuestion, mTotalQuestions));
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

}

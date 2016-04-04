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

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.analytics.QualityPrecondition;
import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.dao.ThreadsDAO;
import ru.yandex.subtitles.content.data.Thread;
import ru.yandex.subtitles.utils.AsyncAutoCompleteHandler;
import ru.yandex.subtitles.utils.IntArrayList;

/**
 * Helper class that handles rules for showing tutorial and quality dialogs.
 */
public class DialogAppearanceHelper implements AsyncAutoCompleteHandler.OnHandleEventListener<Void> {

    public interface OnShowInteractiveDialogListener {

        void onShowQualityFeedback();

        void onShowTutorial();

    }

    private static final String NAME = "DialogAppearanceHelper";

    private static final int QUALITY_THREADS_FREQUENCY = 4;
    private static final int QUALITY_THREAD_MIN_MESSAGES_COUNT = 4;

    private static final IntArrayList WHEN_SHOW_TUTORIAL = new IntArrayList();

    static {
        WHEN_SHOW_TUTORIAL.add(1);
        WHEN_SHOW_TUTORIAL.add(3);
        WHEN_SHOW_TUTORIAL.add(6);
        WHEN_SHOW_TUTORIAL.add(10);
        WHEN_SHOW_TUTORIAL.add(15);
    }

    private final OnShowInteractiveDialogListener mOnShowInteractiveDialogListener;

    private final ThreadsDAO mThreadsDao;
    private final MessagesDAO mMessagesDao;

    private final AsyncAutoCompleteHandler<Void> mHandler;
    private int mThreadsCount = -1;
    private int mActiveThreadsCount = -1;

    public DialogAppearanceHelper(final Context context,
                                  @NonNull final OnShowInteractiveDialogListener onShowInteractiveDialogListener) {
        mOnShowInteractiveDialogListener = onShowInteractiveDialogListener;

        mHandler = new AsyncAutoCompleteHandler<Void>(NAME);
        mHandler.setOnHandleEventListener(this);

        mThreadsDao = new ThreadsDAO(context);
        mMessagesDao = new MessagesDAO(context);
    }

    public void start() {
        mHandler.post(null, true);
    }

    @Override
    public void onHandleEvent(final Void event) {
        final List<QualityPrecondition> data = loadPreconditions();
        final int threadsCount = data.size();
        final int activeThreadsCount = getActiveThreadsCount(data);

        if (mThreadsCount < 0 || mActiveThreadsCount < 0) {
            mThreadsCount = threadsCount;
            mActiveThreadsCount = activeThreadsCount;

        } else if (mThreadsCount < threadsCount) {
            mThreadsCount = threadsCount;

            final boolean shouldShowQualityFeedback = (mActiveThreadsCount < activeThreadsCount
                    && activeThreadsCount > 0 && (activeThreadsCount % QUALITY_THREADS_FREQUENCY) == 0);
            final boolean shouldShowTutorial = WHEN_SHOW_TUTORIAL.contains(threadsCount);

            if (shouldShowQualityFeedback) {
                mActiveThreadsCount = activeThreadsCount;
                mOnShowInteractiveDialogListener.onShowQualityFeedback();
            }

            if (shouldShowTutorial) {
                if (shouldShowQualityFeedback) {
                    WHEN_SHOW_TUTORIAL.add(threadsCount + 1);
                } else {
                    mOnShowInteractiveDialogListener.onShowTutorial();
                }
            }
        }
    }

    @NonNull
    public List<QualityPrecondition> loadPreconditions() {
        final List<QualityPrecondition> preconditions = new ArrayList<QualityPrecondition>();

        final List<Thread> threads = mThreadsDao.getAll();
        for (final Thread thread : threads) {
            final long threadId = thread.getId();
            final int messagesCount = (thread.isDeleted() ? 0 : mMessagesDao.getCountByThreadId(threadId));
            preconditions.add(new QualityPrecondition(threadId, messagesCount));
        }

        return preconditions;
    }

    private int getActiveThreadsCount(final List<QualityPrecondition> data) {
        int activeThreadsCount = 0;
        for (final QualityPrecondition precondition : data) {
            activeThreadsCount += (precondition.getMessagesCount() >= QUALITY_THREAD_MIN_MESSAGES_COUNT ? 1 : 0);
        }
        return activeThreadsCount;
    }

}

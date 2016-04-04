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
package ru.yandex.subtitles.content.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.yandex.subtitles.analytics.ThreadAnalytics;
import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.content.MultipleUrisChangeObserver;
import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.dao.ThreadsDAO;
import ru.yandex.subtitles.content.data.Member;

public class ThreadAnalyticsLoader extends AbstractLoader<ThreadAnalytics> {

    private static final String EXTRA_THREAD = "thread_id";

    @NonNull
    public static Bundle forThreadId(final long threadId) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_THREAD, threadId);
        return args;
    }

    private final ThreadsDAO mThreadsDao;
    private final MessagesDAO mMessagesDao;
    private final long mThreadId;

    public ThreadAnalyticsLoader(final Context context, @NonNull final Bundle args) {
        super(context);

        mThreadsDao = new ThreadsDAO(context);
        mMessagesDao = new MessagesDAO(context);
        mThreadId = args.getLong(EXTRA_THREAD);
    }

    @Nullable
    @Override
    protected Subscribeable onCreateContentObserver() {
        final MultipleUrisChangeObserver observer = new MultipleUrisChangeObserver(this);
        observer.add(ThreadsDAO.CONTENT_URI);
        observer.add(MessagesDAO.CONTENT_URI);
        return observer;
    }

    @Override
    public ThreadAnalytics loadInBackground() {
        final ThreadAnalytics threadAnalytics = new ThreadAnalytics();
        threadAnalytics.setConversationsCount(mThreadsDao.getCountExcludeDeleted());
        threadAnalytics.setOwnerStatementCount(mMessagesDao.getCountByThreadIdAndUserId(mThreadId, Member.DEVICE_OWNER));
        threadAnalytics.setVisavisStatementCount(mMessagesDao.getCountByThreadIdAndUserId(mThreadId, Member.VISAVIS));
        return threadAnalytics;
    }

}

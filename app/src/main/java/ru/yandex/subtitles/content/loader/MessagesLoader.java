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

import java.util.List;

import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.data.Message;

public class MessagesLoader extends AbstractContentProviderLoader<List<Message>> {

    private static final String EXTRA_THREAD = "thread_id";

    @NonNull
    public static Bundle forThreadId(final long threadId) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_THREAD, threadId);
        return args;
    }

    private final MessagesDAO mMessagesDao;
    private final long mThreadId;

    public MessagesLoader(final Context context, @NonNull final Bundle args) {
        super(context, MessagesDAO.CONTENT_URI);

        mMessagesDao = new MessagesDAO(context);
        mThreadId = args.getLong(EXTRA_THREAD);
    }

    @Override
    public List<Message> loadInBackground() {
        return mMessagesDao.getAllByThreadId(mThreadId);
    }

}

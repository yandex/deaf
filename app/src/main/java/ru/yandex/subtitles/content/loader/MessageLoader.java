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

import ru.yandex.subtitles.analytics.MessageAnalytics;
import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.dao.PhrasesDAO;
import ru.yandex.subtitles.content.data.Message;

public class MessageLoader extends AbstractLoader<MessageAnalytics> {

    private static final String EXTRA_MESSAGE = "message_id";

    @NonNull
    public static Bundle forMessageId(final long messageId) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_MESSAGE, messageId);
        return args;
    }

    private final MessagesDAO mMessagesDao;
    private final PhrasesDAO mPhrasesDao;
    private final long mMessageId;

    public MessageLoader(final Context context, @NonNull final Bundle args) {
        super(context);

        mMessagesDao = new MessagesDAO(context);
        mPhrasesDao = new PhrasesDAO(context);
        mMessageId = args.getLong(EXTRA_MESSAGE);
    }

    @Nullable
    @Override
    protected Subscribeable onCreateContentObserver() {
        return null;
    }

    @Override
    public MessageAnalytics loadInBackground() {
        final Message message = mMessagesDao.get(mMessageId);
        final String text = (message != null ? message.getText() : "");
        final boolean isOpeningPhrase = mPhrasesDao.isStartingPhrase(text);
        return new MessageAnalytics(message, isOpeningPhrase);
    }

}

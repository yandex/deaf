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
import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.analytics.ZoomedMessage;
import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.data.Message;

public class ZoomedMessagesLoader extends AbstractContentProviderLoader<List<ZoomedMessage>> {

    private static final String EXTRA_THREAD = "thread_id";
    private static final String EXTRA_USER = "user_id";

    @NonNull
    public static Bundle forThreadIdAndUserId(final long threadId, final long userId) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_THREAD, threadId);
        args.putLong(EXTRA_USER, userId);
        return args;
    }

    private final MessagesDAO mMessagesDao;
    private final LongSparseArray<ZoomedMessage> mZoomedMessages = new LongSparseArray<ZoomedMessage>();

    private final long mThreadId;
    private final long mUserId;

    public ZoomedMessagesLoader(final Context context, @NonNull final Bundle args) {
        super(context, MessagesDAO.CONTENT_URI);
        mMessagesDao = new MessagesDAO(context);
        mThreadId = args.getLong(EXTRA_THREAD);
        mUserId = args.getLong(EXTRA_USER);
    }

    @Override
    public List<ZoomedMessage> loadInBackground() {
        final List<ZoomedMessage> zoomedMessages = new ArrayList<ZoomedMessage>();

        final List<Message> messages = mMessagesDao.getAllByThreadIdAndUser(mThreadId, mUserId);
        for (final Message message : messages) {
            final long messageId = message.getId();
            ZoomedMessage zoomedMessage = mZoomedMessages.get(messageId);
            if (zoomedMessage == null) {
                zoomedMessage = new ZoomedMessage();
            }
            zoomedMessage.setMessage(message);
            zoomedMessage.getMetadata().setText(message.getText());
            mZoomedMessages.put(messageId, zoomedMessage);

            zoomedMessages.add(zoomedMessage);
        }

        return zoomedMessages;
    }

}

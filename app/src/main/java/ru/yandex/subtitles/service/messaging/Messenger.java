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
package ru.yandex.subtitles.service.messaging;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.dao.ThreadsDAO;
import ru.yandex.subtitles.content.data.Member;
import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.content.data.Thread;
import ru.yandex.subtitles.utils.AsyncAutoCompleteHandler;
import ru.yandex.subtitles.utils.DateTimeUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;

/**
 * Class that handles message-related events async.
 */
public class Messenger implements AsyncAutoCompleteHandler.OnHandleEventListener<ConversationEvent> {

    private static final String LOG_TAG = "Messenger";

    private static final String MESSAGES_QUEUE = "MessagesQueue";

    private final Context mContext;
    private final AsyncAutoCompleteHandler<ConversationEvent> mQueue;

    private final ThreadsDAO mThreadsDao;
    private final MessagesDAO mMessagesDao;

    public Messenger(final Context context) {
        mContext = context;

        mQueue = new AsyncAutoCompleteHandler<ConversationEvent>(MESSAGES_QUEUE);
        mQueue.setShutdownTimeout(TimeUnit.SECONDS.toMillis(10));
        mQueue.setOnHandleEventListener(this);

        mThreadsDao = new ThreadsDAO(context);
        mMessagesDao = new MessagesDAO(context);
    }

    public void createConversation(@Nullable final String phrase) {
        mQueue.post(ConversationEvent.createConversation(phrase));
    }

    public void clearEmptyConversations() {
        mQueue.post(ConversationEvent.clearEmptyConversations());
    }

    public void pinConversation(final long threadId) {
        mQueue.post(ConversationEvent.pinConversation(threadId));
    }

    public void unpinConversation(final long threadId) {
        mQueue.post(ConversationEvent.unpinConversation(threadId));
    }

    public void deleteConversation(final long threadId) {
        mQueue.post(ConversationEvent.deleteConversation(threadId));
    }

    public void onConversationOpened(final long threadId) {
        mQueue.post(ConversationEvent.onConversationOpened(threadId));
    }

    public void sendMessage(final long threadId, final long memberId, final String text) {
        mQueue.post(ConversationEvent.sendMessage(threadId, memberId, text));
    }

    public void pinMessage(final long messageId) {
        mQueue.post(ConversationEvent.pinMessage(messageId));
    }

    public void unpinMessage(final long messageId) {
        mQueue.post(ConversationEvent.unpinMessage(messageId));
    }

    public void deleteMessage(final long messageId) {
        mQueue.post(ConversationEvent.deleteMessage(messageId));
    }

    @Override
    public void onHandleEvent(final ConversationEvent event) {
        switch (event.getType()) {
            case ConversationEvent.CREATE_CONVERSATION:
                onHandleCreateConversation(event.getText());
                break;

            case ConversationEvent.SEND_MESSAGE:
                onHandleSendMessage(event.getThreadId(), event.getMemberId(), event.getText());
                break;

            case ConversationEvent.CLEAR_EMPTY_CONVERSATIONS:
                onHandleClearEmptyConversations();
                break;

            case ConversationEvent.PIN_CONVERSATION:
                onHandlePinConversation(event.getThreadId());
                break;

            case ConversationEvent.UNPIN_CONVERSATION:
                onHandleUnpinConversation(event.getThreadId());
                break;

            case ConversationEvent.DELETE_CONVERSATION:
                onHandleDeleteConversation(event.getThreadId());
                break;

            case ConversationEvent.CONVERSATION_OPENED:
                onHandleConversationOpened(event.getThreadId());
                break;

            case ConversationEvent.PIN_MESSAGE:
                onHandlePinMessage(event.getMessageId());
                break;

            case ConversationEvent.UNPIN_MESSAGE:
                onHandleUnpinMessage(event.getMessageId());
                break;

            case ConversationEvent.DELETE_MESSAGE:
                onHandleDeleteMessage(event.getMessageId());
                break;
        }
    }

    private void onHandleCreateConversation(@Nullable final String phrase) {
        final Thread thread = new Thread();
        thread.setDeleted(false);
        thread.setPinned(false);
        thread.setLastOpeningTime(System.currentTimeMillis());
        thread.setOpeningCount(1);
        thread.setPinnedMessageCount(0);

        final Uri threadUri = mThreadsDao.insert(thread);
        final Long threadId = mThreadsDao.parseId(threadUri);
        if (threadId == null) {
            CreateConversationBroadcastReceiver.broadcastConversationError(mContext,
                    ConversationError.KIND_DATABASE);
            return;
        }

        Long messageId = null;
        Long memberId = null;
        if (!TextUtilsExt.isEmpty(phrase)) {
            final Message message = new Message();
            message.setUserId(Member.DEVICE_OWNER);
            message.setThreadId(threadId);
            message.setText(phrase);
            message.setTime(System.currentTimeMillis());
            message.setTimezone(DateTimeUtils.getTimezoneCode());
            message.setPinned(false);

            final Uri messageUri = mMessagesDao.insert(message);
            messageId = mMessagesDao.parseId(messageUri);
            memberId = message.getUserId();
        }

        CreateConversationBroadcastReceiver.broadcastConversationCreated(mContext, threadId,
                messageId, memberId);
    }

    private void onHandleSendMessage(final long threadId, final long memberId, final String text) {
        final Message message = new Message();
        message.setUserId(memberId);
        message.setThreadId(threadId);
        message.setText(text);
        message.setTime(System.currentTimeMillis());
        message.setTimezone(DateTimeUtils.getTimezoneCode());
        message.setPinned(false);

        final Uri messageUri = mMessagesDao.insert(message);
        if (messageUri == null) {
            Log.e(LOG_TAG, "Failed to send message to thread=" + threadId);

        }
    }

    private void onHandleClearEmptyConversations() {
        final List<Thread> threads = mThreadsDao.getAllExcludeDeleted();
        for (final Thread thread : threads) {
            final Long threadId = thread.getId();
            final int messagesCount = mMessagesDao.getCountByThreadId(threadId);
            if (messagesCount == 0) {
                Log.d(LOG_TAG, "Thread=" + threadId + " has been marked as deleted");
                mThreadsDao.setDeleted(threadId, true);
            }
        }
    }

    private void onHandlePinConversation(final long threadId) {
        final Thread thread = mThreadsDao.get(threadId);
        if (thread != null && mThreadsDao.setPinned(threadId, true) > 0) {
            Log.d(LOG_TAG, "Thread=" + threadId + " has been pinned");
            Analytics.onConversationPinned(thread);
        }
    }

    private void onHandleUnpinConversation(final long threadId) {
        if (mThreadsDao.setPinned(threadId, false) > 0) {
            Log.d(LOG_TAG, "Thread=" + threadId + " has been unpinned");
        }
    }

    private void onHandleDeleteConversation(final long threadId) {
        final Thread thread = mThreadsDao.get(threadId);
        if (thread != null && mThreadsDao.setDeleted(threadId, true) > 0) {
            Log.d(LOG_TAG, "Thread=" + threadId + " has been marked as deleted");
            Analytics.onConversationDeleted(thread);
        }
    }

    private void onHandleConversationOpened(final long threadId) {
        final Thread thread = mThreadsDao.get(threadId);
        if (thread != null) {
            if (mThreadsDao.setOpeningTimeAndCount(threadId, System.currentTimeMillis(), thread.getOpeningCount() + 1)) {
                Log.d(LOG_TAG, "Thread=" + threadId + " has been opened");
                Analytics.onHistoryConversationOpened(thread);
            }
        }
    }

    private void onHandlePinMessage(final long messageId) {
        final Message message = mMessagesDao.get(messageId);
        if (message != null && mMessagesDao.setPinned(messageId, true) > 0) {
            Log.i(LOG_TAG, "Message=" + messageId + " has been pinned");

            final long threadId = message.getThreadId();
            final Thread thread = mThreadsDao.get(threadId);
            if (thread != null) {
                if (mThreadsDao.setPinnedMessageCount(threadId, thread.getPinnedMessageCount() + 1)) {
                    Log.i(LOG_TAG, "Pinned messages count has been changed for thread=" + threadId);
                    Analytics.onPinMessage(message);
                }
            }
        }
    }

    private void onHandleUnpinMessage(final long messageId) {
        final Message message = mMessagesDao.get(messageId);
        if (message != null && mMessagesDao.setPinned(messageId, false) > 0) {
            Log.i(LOG_TAG, "Message=" + messageId + " has been unpinned");

            final long threadId = message.getThreadId();
            final Thread thread = mThreadsDao.get(threadId);
            if (thread != null) {
                if (mThreadsDao.setPinnedMessageCount(threadId, thread.getPinnedMessageCount() - 1)) {
                    Log.i(LOG_TAG, "Pinned messages count has been changed for thread=" + threadId);
                }
            }
        }
    }

    private void onHandleDeleteMessage(final long messageId) {
        final Message message = mMessagesDao.get(messageId);
        if (message != null && mMessagesDao.delete(messageId) > 0) {
            Analytics.onDeleteMessage(message);

            final long threadId = message.getThreadId();
            final Thread thread = mThreadsDao.get(threadId);
            if (message.isPinned() && thread != null) {
                if (mThreadsDao.setPinnedMessageCount(threadId, thread.getPinnedMessageCount() - 1)) {
                    Log.i(LOG_TAG, "Pinned messages count has been changed for thread=" + threadId);
                }
            }
        }
    }

}

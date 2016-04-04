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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.utils.IntentUtils;

public class CreateConversationBroadcastReceiver extends BroadcastReceiver
        implements Subscribeable {

    public interface OnCreateConversationListener {

        void onConversationCreated(final long threadId, @Nullable final Long messageId, @Nullable final Long memberId);

        void onConversationCreateError(final int kind);

    }

    private static final String ACTION_CONVERSATION_CREATED = "CreateConversationBroadcastReceiver.ACTION_CONVERSATION_CREATED";
    private static final String ACTION_CONVERSATION_ERROR = "CreateConversationBroadcastReceiver.ACTION_CONVERSATION_ERROR";

    private static final IntentFilter INTENT_FILTER = new IntentFilter();

    static {
        INTENT_FILTER.addAction(ACTION_CONVERSATION_CREATED);
        INTENT_FILTER.addAction(ACTION_CONVERSATION_ERROR);
    }

    private static final String EXTRA_CONVERSATION = "thread_id";
    private static final String EXTRA_MESSAGE = "message_id";
    private static final String EXTRA_MEMBER = "member_id";
    private static final String EXTRA_KIND = "kind";

    public static void broadcastConversationCreated(final Context context,
                                                    final long threadId,
                                                    @Nullable final Long messageId,
                                                    @Nullable final Long memberId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                CreateConversationBroadcastReceiver.class, ACTION_CONVERSATION_CREATED);
        intent.putExtra(EXTRA_CONVERSATION, threadId);
        intent.putExtra(EXTRA_MESSAGE, messageId);
        intent.putExtra(EXTRA_MEMBER, memberId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void broadcastConversationError(final Context context, final int kind) {
        final Intent intent = IntentUtils.createActionIntent(context,
                CreateConversationBroadcastReceiver.class, ACTION_CONVERSATION_ERROR);
        intent.putExtra(EXTRA_KIND, kind);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private final OnCreateConversationListener mOnCreateConversationListener;

    public CreateConversationBroadcastReceiver(@NonNull final OnCreateConversationListener onCreateConversationListener) {
        mOnCreateConversationListener = onCreateConversationListener;
    }

    @Override
    public void subscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, INTENT_FILTER);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = (intent != null ? intent.getAction() : null);
        if (ACTION_CONVERSATION_CREATED.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_CONVERSATION, -1);
            final Long messageId = (Long) intent.getSerializableExtra(EXTRA_MESSAGE);
            final Long memberId = (Long) intent.getSerializableExtra(EXTRA_MEMBER);
            mOnCreateConversationListener.onConversationCreated(threadId, messageId, memberId);

        } else if (ACTION_CONVERSATION_ERROR.equals(action)) {
            final int kind = intent.getIntExtra(EXTRA_KIND, ConversationError.KIND_UNKNOWN);
            mOnCreateConversationListener.onConversationCreateError(kind);

        }
    }

    @Override
    public void unsubscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

}

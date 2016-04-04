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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ConversationEvent {

    public static final int CREATE_CONVERSATION = 1;
    public static final int SEND_MESSAGE = 2;
    public static final int CLEAR_EMPTY_CONVERSATIONS = 3;
    public static final int PIN_CONVERSATION = 4;
    public static final int UNPIN_CONVERSATION = 5;
    public static final int DELETE_CONVERSATION = 6;
    public static final int CONVERSATION_OPENED = 7;
    public static final int PIN_MESSAGE = 8;
    public static final int UNPIN_MESSAGE = 9;
    public static final int DELETE_MESSAGE = 10;

    @NonNull
    public static ConversationEvent createConversation(@Nullable final String phrase) {
        final ConversationEvent event = new ConversationEvent(CREATE_CONVERSATION);
        event.mText = phrase;
        return event;
    }

    @NonNull
    public static ConversationEvent clearEmptyConversations() {
        return new ConversationEvent(CLEAR_EMPTY_CONVERSATIONS);
    }

    @NonNull
    public static ConversationEvent pinConversation(final long threadId) {
        final ConversationEvent event = new ConversationEvent(PIN_CONVERSATION);
        event.mThreadId = threadId;
        return event;
    }

    @NonNull
    public static ConversationEvent unpinConversation(final long threadId) {
        final ConversationEvent event = new ConversationEvent(UNPIN_CONVERSATION);
        event.mThreadId = threadId;
        return event;
    }

    @NonNull
    public static ConversationEvent deleteConversation(final long threadId) {
        final ConversationEvent event = new ConversationEvent(DELETE_CONVERSATION);
        event.mThreadId = threadId;
        return event;
    }

    @NonNull
    public static ConversationEvent onConversationOpened(final long threadId) {
        final ConversationEvent event = new ConversationEvent(CONVERSATION_OPENED);
        event.mThreadId = threadId;
        return event;
    }

    @NonNull
    public static ConversationEvent sendMessage(final long threadId, final long memberId,
                                                final String text) {
        final ConversationEvent event = new ConversationEvent(SEND_MESSAGE);
        event.mThreadId = threadId;
        event.mMemberId = memberId;
        event.mText = text;
        return event;
    }

    @NonNull
    public static ConversationEvent pinMessage(final long messageId) {
        final ConversationEvent event = new ConversationEvent(PIN_MESSAGE);
        event.mMessageId = messageId;
        return event;
    }

    @NonNull
    public static ConversationEvent unpinMessage(final long messageId) {
        final ConversationEvent event = new ConversationEvent(UNPIN_MESSAGE);
        event.mMessageId = messageId;
        return event;
    }

    @NonNull
    public static ConversationEvent deleteMessage(final long messageId) {
        final ConversationEvent event = new ConversationEvent(DELETE_MESSAGE);
        event.mMessageId = messageId;
        return event;
    }

    private int mType;
    private long mThreadId = -1;
    private long mMessageId = -1;
    private long mMemberId = -1;
    private String mText;

    private ConversationEvent(final int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    @Nullable
    public String getText() {
        return mText;
    }

    public long getThreadId() {
        return mThreadId;
    }

    public long getMessageId() {
        return mMessageId;
    }

    public long getMemberId() {
        return mMemberId;
    }
    
}

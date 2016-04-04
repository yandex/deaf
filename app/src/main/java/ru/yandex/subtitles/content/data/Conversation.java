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
package ru.yandex.subtitles.content.data;

import android.support.annotation.NonNull;

public class Conversation implements Comparable<Conversation> {

    private final Thread mThread;
    private final Message mLastMessage;

    public Conversation(@NonNull final Thread thread, @NonNull final Message lastMessage) {
        mThread = thread;
        mLastMessage = lastMessage;
    }

    @NonNull
    public Thread getThread() {
        return mThread;
    }

    @NonNull
    public Message getLastMessage() {
        return mLastMessage;
    }

    @Override
    public int hashCode() {
        return mThread.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;

        } else if (o == null || getClass() != o.getClass()) {
            return false;

        } else {
            final Conversation that = (Conversation) o;
            return mThread.equals(that.mThread);
        }
    }

    @Override
    public int compareTo(@NonNull final Conversation another) {
        final long lhs = another.getLastMessage().getTime();
        final long rhs = mLastMessage.getTime();
        return (lhs < rhs ? -1 : (lhs == rhs ? 0 : 1));
    }

}
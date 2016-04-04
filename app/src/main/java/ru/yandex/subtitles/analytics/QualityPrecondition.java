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
package ru.yandex.subtitles.analytics;

import android.os.Parcel;
import android.os.Parcelable;

public class QualityPrecondition implements Parcelable {

    private final long mThreadId;
    private final int mMessagesCount;

    public QualityPrecondition(final long threadId, final int messagesCount) {
        mThreadId = threadId;
        mMessagesCount = messagesCount;
    }

    /* package */ QualityPrecondition(final Parcel src) {
        mThreadId = src.readLong();
        mMessagesCount = src.readInt();
    }

    public long getThreadId() {
        return mThreadId;
    }

    public int getMessagesCount() {
        return mMessagesCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(mThreadId);
        dest.writeInt(mMessagesCount);
    }

    public static final Creator<QualityPrecondition> CREATOR = new Creator<QualityPrecondition>() {

        @Override
        public QualityPrecondition createFromParcel(final Parcel source) {
            return new QualityPrecondition(source);
        }

        @Override
        public QualityPrecondition[] newArray(final int size) {
            return new QualityPrecondition[size];
        }

    };
}

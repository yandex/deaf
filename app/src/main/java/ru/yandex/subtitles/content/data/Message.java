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

import android.os.Parcel;
import android.os.Parcelable;

import ru.yandex.subtitles.content.dao.Identify;

public class Message implements Identify<Long>, Parcelable {

    private Long mId;
    private long mUserId;
    private long mThreadId;
    private String mText;
    private long mTime;
    private String mTimezone;
    private boolean mPinned;

    public Message() {
    }

    /* package */ Message(final Parcel src) {
        mId = (Long) src.readValue(Long.class.getClassLoader());
        mUserId = src.readLong();
        mThreadId = src.readLong();
        mText = (String) src.readValue(String.class.getClassLoader());
        mTime = src.readLong();
        mTimezone = (String) src.readValue(String.class.getClassLoader());
        mPinned = (src.readInt() == 1);
    }

    @Override
    public Long getId() {
        return mId;
    }

    public void setId(final Long id) {
        mId = id;
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(final long userId) {
        mUserId = userId;
    }

    public long getThreadId() {
        return mThreadId;
    }

    public void setThreadId(final long threadId) {
        mThreadId = threadId;
    }

    public String getText() {
        return mText;
    }

    public void setText(final String text) {
        mText = text;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(final long time) {
        mTime = time;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(final String timezone) {
        mTimezone = timezone;
    }

    public boolean isPinned() {
        return mPinned;
    }

    public void setPinned(final boolean pinned) {
        mPinned = pinned;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;

        } else if (o == null || getClass() != o.getClass()) {
            return false;

        } else {
            final Message message = (Message) o;
            return (mId.equals(message.mId) &&
                    mUserId == message.mUserId &&
                    mThreadId == message.mThreadId);
        }
    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + (int) (mUserId ^ (mUserId >>> 32));
        result = 31 * result + (int) (mThreadId ^ (mThreadId >>> 32));
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeValue(mId);
        dest.writeLong(mUserId);
        dest.writeLong(mThreadId);
        dest.writeValue(mText);
        dest.writeLong(mTime);
        dest.writeValue(mTimezone);
        dest.writeInt(mPinned ? 1 : 0);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

        @Override
        public Message createFromParcel(final Parcel src) {
            return new Message(src);
        }

        @Override
        public Message[] newArray(final int size) {
            return new Message[size];
        }

    };

}

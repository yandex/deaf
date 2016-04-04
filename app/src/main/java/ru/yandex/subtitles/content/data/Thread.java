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

public class Thread implements Identify<Long>, Parcelable {

    private Long mId;
    private boolean mDeleted;
    private boolean mPinned;
    private long mLastOpeningTime;
    private int mOpeningCount;
    private int mPinnedMessageCount;

    public Thread() {
    }

    /* package */ Thread(final Parcel src) {
        mId = (Long) src.readValue(Long.class.getClassLoader());
        mDeleted = (src.readInt() == 1);
        mPinned = (src.readInt() == 1);
        mLastOpeningTime = src.readLong();
        mOpeningCount = src.readInt();
        mPinnedMessageCount = src.readInt();
    }

    @Override
    public Long getId() {
        return mId;
    }

    public void setId(final Long id) {
        mId = id;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(final boolean deleted) {
        mDeleted = deleted;
    }

    public boolean isPinned() {
        return mPinned;
    }

    public void setPinned(final boolean pinned) {
        mPinned = pinned;
    }

    public long getLastOpeningTime() {
        return mLastOpeningTime;
    }

    public void setLastOpeningTime(final long lastOpeningTime) {
        mLastOpeningTime = lastOpeningTime;
    }

    public int getOpeningCount() {
        return mOpeningCount;
    }

    public void setOpeningCount(final int openingCount) {
        mOpeningCount = openingCount;
    }

    public int getPinnedMessageCount() {
        return mPinnedMessageCount;
    }

    public void setPinnedMessageCount(final int pinnedMessageCount) {
        mPinnedMessageCount = pinnedMessageCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;

        } else if (o == null || getClass() != o.getClass()) {
            return false;

        } else {
            final Thread thread = (Thread) o;
            return mId.equals(thread.mId);
        }
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeValue(mId);
        dest.writeInt(mDeleted ? 1 : 0);
        dest.writeInt(mPinned ? 1 : 0);
        dest.writeLong(mLastOpeningTime);
        dest.writeInt(mOpeningCount);
        dest.writeInt(mPinnedMessageCount);
    }

    public static final Parcelable.Creator<Thread> CREATOR = new Parcelable.Creator<Thread>() {

        @Override
        public Thread createFromParcel(final Parcel src) {
            return new Thread(src);
        }

        @Override
        public Thread[] newArray(final int size) {
            return new Thread[size];
        }

    };

}

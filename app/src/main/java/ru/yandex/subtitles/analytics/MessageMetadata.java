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

public class MessageMetadata implements Parcelable {

    private String mText;
    private boolean mOpeningPhrase;
    private boolean mWasVocalized;
    private long mDuration; // in seconds

    public MessageMetadata() {
    }

    /* package */ MessageMetadata(final Parcel src) {
        mText = (String) src.readValue(String.class.getClassLoader());
        mOpeningPhrase = (src.readInt() == 1);
        mWasVocalized = (src.readInt() == 1);
        mDuration = src.readLong();
    }

    public String getText() {
        return mText;
    }

    public void setText(final String text) {
        mText = text;
    }

    public boolean isOpeningPhrase() {
        return mOpeningPhrase;
    }

    public void setOpeningPhrase(final boolean openingPhrase) {
        mOpeningPhrase = openingPhrase;
    }

    public boolean wasVocalized() {
        return mWasVocalized;
    }

    public void setWasVocalized(final boolean wasVocalized) {
        mWasVocalized = wasVocalized;
    }

    public long getDurationInSeconds() {
        return mDuration;
    }

    public void setDurationInSeconds(final long duration) {
        mDuration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeValue(mText);
        dest.writeInt(mOpeningPhrase ? 1 : 0);
        dest.writeInt(mWasVocalized ? 1 : 0);
        dest.writeLong(mDuration);
    }

    public static final Parcelable.Creator<MessageMetadata> CREATOR = new Parcelable.Creator<MessageMetadata>() {

        public MessageMetadata createFromParcel(final Parcel in) {
            return new MessageMetadata(in);
        }

        public MessageMetadata[] newArray(final int size) {
            return new MessageMetadata[size];
        }

    };

}

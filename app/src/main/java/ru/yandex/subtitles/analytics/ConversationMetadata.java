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
import android.support.annotation.Nullable;

public class ConversationMetadata implements Parcelable {

    private int mOwnerStatementCount = 0;
    private int mVisavisStatementCount = 0;
    private int mConversationsCount = 0;
    private int mZoomedMessageCount = 0;
    private int mVocalizedMessageCount = 0;
    private String mFirstPhrase;
    private long mDuration = 0;
    private boolean mFromWidget = false;
    private boolean mReversePortrait = false;

    public ConversationMetadata() {
    }

    /* package */ ConversationMetadata(final Parcel in) {
        mOwnerStatementCount = in.readInt();
        mVisavisStatementCount = in.readInt();
        mConversationsCount = in.readInt();
        mFirstPhrase = (String) in.readValue(String.class.getClassLoader());
        mDuration = in.readLong();
        mFromWidget = (in.readInt() == 1);
        mReversePortrait = (in.readInt() == 1);
    }

    public int getOwnerStatementCount() {
        return mOwnerStatementCount;
    }

    public void setOwnerStatementCount(final int ownerStatementCount) {
        mOwnerStatementCount = ownerStatementCount;
    }

    public int getVisavisStatementCount() {
        return mVisavisStatementCount;
    }

    public void setVisavisStatementCount(final int visavisStatementCount) {
        mVisavisStatementCount = visavisStatementCount;
    }

    public void setConversationsCount(final int conversationsCount) {
        mConversationsCount = conversationsCount;
    }

    public int getConversationsCount() {
        return mConversationsCount;
    }

    public int getZoomedMessageCount() {
        return mZoomedMessageCount;
    }

    public int getVocalizedMessageCount() {
        return mVocalizedMessageCount;
    }

    public void increaseVocalizedMessageCount() {
        mVocalizedMessageCount++;
    }

    public void increaseZoomedMessageCount() {
        mZoomedMessageCount++;
    }

    @Nullable
    public String getFirstPhrase() {
        return mFirstPhrase;
    }

    public void setFirstPhrase(final String firstPhrase) {
        mFirstPhrase = firstPhrase;
    }

    public long getDurationInSeconds() {
        return mDuration;
    }

    public void setDurationInSeconds(final long duration) {
        mDuration = duration;
    }

    public void setFromWidget(final boolean fromWidget) {
        mFromWidget = fromWidget;
    }

    public boolean isFromWidget() {
        return mFromWidget;
    }

    public void setReversePortrait(final boolean reversePortrait) {
        mReversePortrait = reversePortrait;
    }

    public boolean isReversePortrait() {
        return mReversePortrait;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mOwnerStatementCount);
        dest.writeInt(mVisavisStatementCount);
        dest.writeInt(mConversationsCount);
        dest.writeValue(mFirstPhrase);
        dest.writeLong(mDuration);
        dest.writeInt(mFromWidget ? 1 : 0);
        dest.writeInt(mReversePortrait ? 1 : 0);
    }

    public static final Parcelable.Creator<ConversationMetadata> CREATOR = new Parcelable.Creator<ConversationMetadata>() {

        public ConversationMetadata createFromParcel(final Parcel in) {
            return new ConversationMetadata(in);
        }

        public ConversationMetadata[] newArray(final int size) {
            return new ConversationMetadata[size];
        }

    };

}

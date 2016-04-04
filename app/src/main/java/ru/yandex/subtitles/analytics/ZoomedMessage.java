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

import ru.yandex.subtitles.content.data.Message;

public class ZoomedMessage implements Parcelable {

    private Message mMessage;
    private MessageMetadata mMetadata;

    public ZoomedMessage() {
        mMetadata = new MessageMetadata();
    }

    /* package */ ZoomedMessage(final Parcel src) {
        mMessage = (Message) src.readValue(Message.class.getClassLoader());
        mMetadata = (MessageMetadata) src.readValue(MessageMetadata.class.getClassLoader());
    }

    public void setMessage(final Message message) {
        mMessage = message;
    }

    public Message getMessage() {
        return mMessage;
    }

    public MessageMetadata getMetadata() {
        return mMetadata;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeValue(mMessage);
        dest.writeValue(mMetadata);
    }

    public static final Creator<ZoomedMessage> CREATOR = new Creator<ZoomedMessage>() {

        @Override
        public ZoomedMessage createFromParcel(final Parcel source) {
            return new ZoomedMessage(source);
        }

        @Override
        public ZoomedMessage[] newArray(final int size) {
            return new ZoomedMessage[size];
        }

    };

}

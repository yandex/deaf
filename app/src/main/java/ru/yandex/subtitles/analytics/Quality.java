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
import android.support.annotation.StringRes;

public class Quality implements Parcelable {

    private final int mTitle;
    private final int mSubtitle;

    public Quality(@StringRes final int title, @StringRes final int subtitle) {
        mTitle = title;
        mSubtitle = subtitle;
    }

    @StringRes
    public int getTitle() {
        return mTitle;
    }

    @StringRes
    public int getSubtitle() {
        return mSubtitle;
    }

    /* package */ Quality(final Parcel src) {
        mTitle = src.readInt();
        mSubtitle = src.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mTitle);
        dest.writeInt(mSubtitle);
    }

    public static final Creator<Quality> CREATOR = new Creator<Quality>() {

        @Override
        public Quality createFromParcel(final Parcel source) {
            return new Quality(source);
        }

        @Override
        public Quality[] newArray(final int size) {
            return new Quality[size];
        }

    };

}

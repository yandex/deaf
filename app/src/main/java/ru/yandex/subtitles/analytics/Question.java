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
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;

public class Question implements Parcelable {

    private int mTitle;
    private int mOptions;
    private int mAnswer = -1;

    public Question() {
    }

    /* package */ Question(final Parcel src) {
        mTitle = src.readInt();
        mOptions = src.readInt();
        mAnswer = src.readInt();
    }

    public void setTitle(@StringRes final int title) {
        mTitle = title;
    }

    @StringRes
    public int getTitle() {
        return mTitle;
    }

    public void setOptions(@ArrayRes final int options) {
        mOptions = options;
    }

    @ArrayRes
    public int getOptions() {
        return mOptions;
    }

    public void setAnswer(final int answer) {
        mAnswer = answer;
    }

    public int getAnswer() {
        return mAnswer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mTitle);
        dest.writeInt(mOptions);
        dest.writeInt(mAnswer);
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {

        @Override
        public Question createFromParcel(final Parcel source) {
            return new Question(source);
        }

        @Override
        public Question[] newArray(final int size) {
            return new Question[size];
        }

    };

}
